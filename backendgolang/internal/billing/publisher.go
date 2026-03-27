package billing

import (
	"encoding/json"
	"fmt"

	amqp "github.com/rabbitmq/amqp091-go"
	"go.uber.org/zap"

	"github.com/dianaglobal/painel-autor-go/internal/shared/rabbitmq"
)

// ChargeEmailEvent is the payload published to RabbitMQ for billing events.
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

// Publisher publishes billing events to RabbitMQ.
type Publisher struct {
	ch  *amqp.Channel
	log *zap.Logger
}

// NewPublisher creates a new billing Publisher.
func NewPublisher(ch *amqp.Channel, log *zap.Logger) *Publisher {
	return &Publisher{ch: ch, log: log}
}

// PublishChargeCreated publishes a billing.charge.created event.
func (p *Publisher) PublishChargeCreated(event ChargeEmailEvent) error {
	return p.publish("billing.charge.created", event)
}

// PublishChargeOverdue publishes a billing.charge.overdue event.
func (p *Publisher) PublishChargeOverdue(event ChargeEmailEvent) error {
	return p.publish("billing.charge.overdue", event)
}

func (p *Publisher) publish(routingKey string, event ChargeEmailEvent) error {
	event.EventType = routingKey
	body, err := json.Marshal(event)
	if err != nil {
		return fmt.Errorf("marshal event %s: %w", routingKey, err)
	}
	if err := rabbitmq.Publish(p.ch, routingKey, body); err != nil {
		return fmt.Errorf("publish event %s: %w", routingKey, err)
	}
	p.log.Info("event published", zap.String("routing_key", routingKey))
	return nil
}
