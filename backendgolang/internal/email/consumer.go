package email

import (
	"context"
	"encoding/json"
	"strings"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/codes"
	"go.uber.org/zap"

	"github.com/dianaglobal/painel-autor-go/internal/shared/observability"
	"github.com/dianaglobal/painel-autor-go/internal/shared/rabbitmq"
)

var tracer = otel.Tracer("email-worker")

// ChargeEmailEvent is the payload for billing events.
type ChargeEmailEvent struct {
	EventType   string   `json:"event_type"`
	UserName    string   `json:"user_name"`
	UserEmail   string   `json:"user_email"`
	ChargeMonth int      `json:"charge_month"`
	ChargeYear  int      `json:"charge_year"`
	Amount      float64  `json:"amount"`
	DueDate     string   `json:"due_date"`
	PixCode     string   `json:"pix_code,omitempty"`
	PixImageURL string   `json:"pix_image_url,omitempty"`
	ConfirmedAt string   `json:"confirmed_at,omitempty"`
	DaysOverdue int64    `json:"days_overdue,omitempty"`
	AdminEmails []string `json:"admin_emails,omitempty"`
}

// AuthEmailEvent is the payload for auth events.
type AuthEmailEvent struct {
	EventType string `json:"event_type"`
	UserName  string `json:"user_name"`
	UserEmail string `json:"user_email"`
	Token     string `json:"token,omitempty"`
	ActionURL string `json:"action_url,omitempty"`
}

const (
	billingQueueName = "email.billing.events"
	authQueueName    = "email.auth.events"
	dlxName          = "painel.events.dlx"
)

// Consumer listens to RabbitMQ and dispatches email sending.
type Consumer struct {
	conn    *amqp.Connection
	ch      *amqp.Channel
	sender  *SMTPSender
	mlogger *MongoLogger
	log     *zap.Logger
}

// NewConsumer creates a Consumer and sets up queues/bindings.
func NewConsumer(
	conn *amqp.Connection,
	ch *amqp.Channel,
	sender *SMTPSender,
	mlogger *MongoLogger,
	log *zap.Logger,
) (*Consumer, error) {
	// Declare DLX
	if err := ch.ExchangeDeclare(dlxName, "fanout", true, false, false, false, nil); err != nil {
		return nil, err
	}

	// Billing queue
	if _, err := rabbitmq.DeclareQueue(ch, billingQueueName, dlxName); err != nil {
		return nil, err
	}
	for _, key := range []string{
		"billing.charge.created",
		"billing.payment.confirmed",
		"billing.charge.overdue",
	} {
		if err := rabbitmq.BindQueue(ch, billingQueueName, key); err != nil {
			return nil, err
		}
	}

	// Auth queue
	if _, err := rabbitmq.DeclareQueue(ch, authQueueName, dlxName); err != nil {
		return nil, err
	}
	for _, key := range []string{
		"auth.password.reset",
		"auth.account.confirm",
		"auth.welcome",
	} {
		if err := rabbitmq.BindQueue(ch, authQueueName, key); err != nil {
			return nil, err
		}
	}

	// Set QoS
	if err := ch.Qos(10, 0, false); err != nil {
		return nil, err
	}

	return &Consumer{
		conn:    conn,
		ch:      ch,
		sender:  sender,
		mlogger: mlogger,
		log:     log,
	}, nil
}

// Start begins consuming from both queues in separate goroutines.
// It blocks until ctx is cancelled.
func (c *Consumer) Start(ctx context.Context) error {
	billingMsgs, err := c.ch.Consume(billingQueueName, "email-billing-consumer", false, false, false, false, nil)
	if err != nil {
		return err
	}
	authMsgs, err := c.ch.Consume(authQueueName, "email-auth-consumer", false, false, false, false, nil)
	if err != nil {
		return err
	}

	go c.consumeBilling(ctx, billingMsgs)
	go c.consumeAuth(ctx, authMsgs)

	<-ctx.Done()
	return nil
}

func (c *Consumer) consumeBilling(ctx context.Context, msgs <-chan amqp.Delivery) {
	for {
		select {
		case <-ctx.Done():
			return
		case msg, ok := <-msgs:
			if !ok {
				return
			}
			c.handleBillingMessage(ctx, msg)
		}
	}
}

func (c *Consumer) consumeAuth(ctx context.Context, msgs <-chan amqp.Delivery) {
	for {
		select {
		case <-ctx.Done():
			return
		case msg, ok := <-msgs:
			if !ok {
				return
			}
			c.handleAuthMessage(ctx, msg)
		}
	}
}

func (c *Consumer) handleBillingMessage(ctx context.Context, msg amqp.Delivery) {
	ctx, span := tracer.Start(ctx, "email.billing.handle")
	defer span.End()

	var event ChargeEmailEvent
	if err := json.Unmarshal(msg.Body, &event); err != nil {
		c.log.Error("failed to unmarshal billing event", zap.Error(err), zap.ByteString("body", msg.Body))
		observability.MessagesConsumedTotal.WithLabelValues(billingQueueName, "nack").Inc()
		span.RecordError(err)
		span.SetStatus(codes.Error, err.Error())
		_ = msg.Nack(false, false)
		return
	}

	span.SetAttributes(attribute.String("event.type", event.EventType))

	subject, htmlBody, err := renderChargeEmail(event.EventType, &event)
	if err != nil {
		c.log.Error("failed to render billing email template", zap.Error(err), zap.String("event_type", event.EventType))
		observability.MessagesConsumedTotal.WithLabelValues(billingQueueName, "nack").Inc()
		span.RecordError(err)
		span.SetStatus(codes.Error, err.Error())
		_ = msg.Nack(false, false)
		return
	}

	recipients := []string{event.UserEmail}
	if event.EventType == "billing.charge.overdue" && len(event.AdminEmails) > 0 {
		recipients = append(recipients, event.AdminEmails...)
	}

	sendErr := c.sender.Send(recipients, subject, htmlBody)
	logEntry := EmailLog{
		EventType: event.EventType,
		To:        strings.Join(recipients, ","),
		Subject:   subject,
		SentAt:    time.Now().UTC(),
	}
	if sendErr != nil {
		logEntry.Status = "failed"
		logEntry.Error = sendErr.Error()
		c.log.Error("failed to send billing email",
			zap.Error(sendErr),
			zap.String("event_type", event.EventType),
			zap.String("to", event.UserEmail),
		)
		observability.EmailsFailedTotal.WithLabelValues(event.EventType).Inc()
		observability.MessagesConsumedTotal.WithLabelValues(billingQueueName, "nack").Inc()
		span.RecordError(sendErr)
		span.SetStatus(codes.Error, sendErr.Error())
		_ = msg.Nack(false, false)
	} else {
		logEntry.Status = "sent"
		c.log.Info("billing email sent",
			zap.String("event_type", event.EventType),
			zap.String("to", event.UserEmail),
		)
		observability.EmailsSentTotal.WithLabelValues(event.EventType).Inc()
		observability.MessagesConsumedTotal.WithLabelValues(billingQueueName, "ack").Inc()
		_ = msg.Ack(false)
	}

	if logErr := c.mlogger.Log(ctx, logEntry); logErr != nil {
		c.log.Warn("failed to log email to MongoDB", zap.Error(logErr))
	}
}

func (c *Consumer) handleAuthMessage(ctx context.Context, msg amqp.Delivery) {
	ctx, span := tracer.Start(ctx, "email.auth.handle")
	defer span.End()

	var event AuthEmailEvent
	if err := json.Unmarshal(msg.Body, &event); err != nil {
		c.log.Error("failed to unmarshal auth event", zap.Error(err), zap.ByteString("body", msg.Body))
		observability.MessagesConsumedTotal.WithLabelValues(authQueueName, "nack").Inc()
		span.RecordError(err)
		span.SetStatus(codes.Error, err.Error())
		_ = msg.Nack(false, false)
		return
	}

	span.SetAttributes(attribute.String("event.type", event.EventType))

	subject, htmlBody, err := renderAuthEmail(event.EventType, &event)
	if err != nil {
		c.log.Error("failed to render auth email template", zap.Error(err), zap.String("event_type", event.EventType))
		observability.MessagesConsumedTotal.WithLabelValues(authQueueName, "nack").Inc()
		span.RecordError(err)
		span.SetStatus(codes.Error, err.Error())
		_ = msg.Nack(false, false)
		return
	}

	sendErr := c.sender.Send([]string{event.UserEmail}, subject, htmlBody)
	logEntry := EmailLog{
		EventType: event.EventType,
		To:        event.UserEmail,
		Subject:   subject,
		SentAt:    time.Now().UTC(),
	}
	if sendErr != nil {
		logEntry.Status = "failed"
		logEntry.Error = sendErr.Error()
		c.log.Error("failed to send auth email",
			zap.Error(sendErr),
			zap.String("event_type", event.EventType),
			zap.String("to", event.UserEmail),
		)
		observability.EmailsFailedTotal.WithLabelValues(event.EventType).Inc()
		observability.MessagesConsumedTotal.WithLabelValues(authQueueName, "nack").Inc()
		span.RecordError(sendErr)
		span.SetStatus(codes.Error, sendErr.Error())
		_ = msg.Nack(false, false)
	} else {
		logEntry.Status = "sent"
		c.log.Info("auth email sent",
			zap.String("event_type", event.EventType),
			zap.String("to", event.UserEmail),
		)
		observability.EmailsSentTotal.WithLabelValues(event.EventType).Inc()
		observability.MessagesConsumedTotal.WithLabelValues(authQueueName, "ack").Inc()
		_ = msg.Ack(false)
	}

	if logErr := c.mlogger.Log(ctx, logEntry); logErr != nil {
		c.log.Warn("failed to log email to MongoDB", zap.Error(logErr))
	}
}
