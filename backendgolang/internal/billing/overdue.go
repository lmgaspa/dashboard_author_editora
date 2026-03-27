package billing

import (
	"context"
	"fmt"
	"time"

	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/codes"
	"go.uber.org/zap"

	"github.com/dianaglobal/painel-autor-go/internal/shared/observability"
	redisclient "github.com/dianaglobal/painel-autor-go/internal/shared/redis"
)

var overdueTracer = otel.Tracer("billing-worker")

// OverdueChargeDetectionJob is the cron job that detects overdue charges and publishes events.
type OverdueChargeDetectionJob struct {
	repo      *Repository
	publisher *Publisher
	redis     *redisclient.Client
	log       *zap.Logger
}

// NewOverdueChargeDetectionJob creates a new OverdueChargeDetectionJob.
func NewOverdueChargeDetectionJob(
	repo *Repository,
	publisher *Publisher,
	redis *redisclient.Client,
	log *zap.Logger,
) *OverdueChargeDetectionJob {
	return &OverdueChargeDetectionJob{
		repo:      repo,
		publisher: publisher,
		redis:     redis,
		log:       log,
	}
}

// Run is the cron entry point — scheduled at "0 0 6 * * *" (06:00 daily).
func (j *OverdueChargeDetectionJob) Run() {
	start := time.Now()
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Minute)
	defer cancel()
	defer func() {
		observability.BillingJobDurationSeconds.WithLabelValues("overdue").Observe(time.Since(start).Seconds())
	}()

	ctx, span := overdueTracer.Start(ctx, "billing.overdue.run")
	defer span.End()

	j.log.Info("OverdueChargeDetectionJob started")

	charges, err := j.repo.FindOverdueCharges(ctx)
	if err != nil {
		j.log.Error("OverdueJob: failed to fetch overdue charges", zap.Error(err))
		return
	}

	adminEmails, err := j.repo.FindAdminEmails(ctx)
	if err != nil {
		j.log.Warn("OverdueJob: failed to fetch admin emails, proceeding without admins", zap.Error(err))
		adminEmails = []string{}
	}

	j.log.Info("OverdueJob: processing charges", zap.Int("count", len(charges)))

	for _, charge := range charges {
		if err := j.processCharge(ctx, charge, adminEmails); err != nil {
			j.log.Error("OverdueJob: error processing charge",
				zap.Int("charge_id", charge.ID),
				zap.String("author_id", charge.AuthorID),
				zap.Error(err),
			)
		}
	}

	j.log.Info("OverdueChargeDetectionJob finished")
}

func (j *OverdueChargeDetectionJob) processCharge(ctx context.Context, charge MonthlyCharge, adminEmails []string) error {
	ctx, span := overdueTracer.Start(ctx, "billing.overdue.process_charge")
	defer span.End()
	lockKey := fmt.Sprintf("lock:overdue:process:%d", charge.ID)

	acquired, err := j.redis.AcquireLock(ctx, lockKey, time.Hour)
	if err != nil {
		return fmt.Errorf("acquire overdue lock for charge %d: %w", charge.ID, err)
	}
	if !acquired {
		j.log.Info("OverdueJob: lock already held, skipping",
			zap.Int("charge_id", charge.ID),
			zap.String("author_id", charge.AuthorID),
		)
		return nil
	}
	defer func() {
		if releaseErr := j.redis.ReleaseLock(ctx, lockKey); releaseErr != nil {
			j.log.Warn("OverdueJob: failed to release lock",
				zap.String("lock_key", lockKey),
				zap.Error(releaseErr),
			)
		}
	}()

	// Check whether a ticket already exists for this charge
	ticketExists, err := j.repo.ExistsTicketForCharge(ctx, charge.AuthorID, charge.ID)
	if err != nil {
		return fmt.Errorf("check ticket for charge %d: %w", charge.ID, err)
	}
	if ticketExists {
		j.log.Info("OverdueJob: ticket already exists for charge, skipping",
			zap.Int("charge_id", charge.ID),
		)
		return nil
	}

	// Fetch author user info
	users, err := j.repo.FindUsersWithAuthorId(ctx)
	if err != nil {
		return fmt.Errorf("fetch users for charge %d: %w", charge.ID, err)
	}

	var authorUser *User
	for i := range users {
		if users[i].AuthorID == charge.AuthorID {
			authorUser = &users[i]
			break
		}
	}
	if authorUser == nil {
		return fmt.Errorf("author %s not found for charge %d", charge.AuthorID, charge.ID)
	}

	daysOverdue := int64(time.Since(charge.DueDate).Hours() / 24)
	if daysOverdue < 0 {
		daysOverdue = 0
	}

	event := ChargeEmailEvent{
		UserName:    authorUser.Name,
		UserEmail:   authorUser.Email,
		ChargeMonth: charge.Month,
		ChargeYear:  charge.Year,
		Amount:      charge.Amount,
		DueDate:     charge.DueDate.Format("2006-01-02"),
		DaysOverdue: daysOverdue,
		AdminEmails: adminEmails,
	}

	if err := j.publisher.PublishChargeOverdue(event); err != nil {
		span.RecordError(err)
		span.SetStatus(codes.Error, err.Error())
		return fmt.Errorf("publish charge.overdue for charge %d: %w", charge.ID, err)
	}

	observability.BillingOverdueDetectedTotal.Inc()

	j.log.Info("OverdueJob: charge.overdue event published",
		zap.Int("charge_id", charge.ID),
		zap.String("author_id", charge.AuthorID),
		zap.Int64("days_overdue", daysOverdue),
	)
	return nil
}
