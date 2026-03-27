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

var billingTracer = otel.Tracer("billing-worker")

// BillingJob is the cron job that generates monthly billing charges.
type BillingJob struct {
	repo      *Repository
	publisher *Publisher
	redis     *redisclient.Client
	log       *zap.Logger
}

// NewBillingJob creates a new BillingJob.
func NewBillingJob(
	repo *Repository,
	publisher *Publisher,
	redis *redisclient.Client,
	log *zap.Logger,
) *BillingJob {
	return &BillingJob{
		repo:      repo,
		publisher: publisher,
		redis:     redis,
		log:       log,
	}
}

// Run is the cron entry point — scheduled at "0 0 8 * * *" (08:00 daily).
func (j *BillingJob) Run() {
	start := time.Now()
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Minute)
	defer cancel()
	defer func() {
		observability.BillingJobDurationSeconds.WithLabelValues("billing").Observe(time.Since(start).Seconds())
	}()

	ctx, span := billingTracer.Start(ctx, "billing.job.run")
	defer span.End()

	now := time.Now()
	month := int(now.Month())
	year := now.Year()

	j.log.Info("BillingJob started", zap.Int("month", month), zap.Int("year", year))

	users, err := j.repo.FindUsersWithAuthorId(ctx)
	if err != nil {
		j.log.Error("BillingJob: failed to fetch users", zap.Error(err))
		return
	}

	j.log.Info("BillingJob: processing users", zap.Int("count", len(users)))

	for _, user := range users {
		if err := j.processUser(ctx, user, month, year); err != nil {
			j.log.Error("BillingJob: error processing user",
				zap.String("author_id", user.AuthorID),
				zap.Error(err),
			)
		}
	}

	j.log.Info("BillingJob finished", zap.Int("month", month), zap.Int("year", year))
}

func (j *BillingJob) processUser(ctx context.Context, user User, month, year int) error {
	ctx, span := billingTracer.Start(ctx, "billing.job.process_user")
	defer span.End()
	lockKey := fmt.Sprintf("lock:billing:charge:%s:%d:%d", user.AuthorID, month, year)

	acquired, err := j.redis.AcquireLock(ctx, lockKey, time.Hour)
	if err != nil {
		return fmt.Errorf("acquire lock for author %s: %w", user.AuthorID, err)
	}
	if !acquired {
		j.log.Info("BillingJob: lock already held, skipping",
			zap.String("author_id", user.AuthorID),
			zap.Int("month", month),
			zap.Int("year", year),
		)
		return nil
	}
	defer func() {
		if releaseErr := j.redis.ReleaseLock(ctx, lockKey); releaseErr != nil {
			j.log.Warn("BillingJob: failed to release lock",
				zap.String("lock_key", lockKey),
				zap.Error(releaseErr),
			)
		}
	}()

	// Double-check in DB to avoid race condition
	exists, err := j.repo.ChargeExistsForMonth(ctx, user.AuthorID, month, year)
	if err != nil {
		return fmt.Errorf("check charge exists for author %s: %w", user.AuthorID, err)
	}
	if exists {
		j.log.Info("BillingJob: charge already exists, skipping",
			zap.String("author_id", user.AuthorID),
			zap.Int("month", month),
			zap.Int("year", year),
		)
		return nil
	}

	// Calculate due date: 10th of following month
	dueDate := time.Date(year, time.Month(month+1), 10, 0, 0, 0, 0, time.UTC)
	if month == 12 {
		dueDate = time.Date(year+1, time.January, 10, 0, 0, 0, 0, time.UTC)
	}

	// Default amount — in a real system this would come from author's contract
	const defaultMonthlyAmount = 0.0

	event := ChargeEmailEvent{
		UserName:    user.Name,
		UserEmail:   user.Email,
		ChargeMonth: month,
		ChargeYear:  year,
		Amount:      defaultMonthlyAmount,
		DueDate:     dueDate.Format("2006-01-02"),
	}

	if err := j.publisher.PublishChargeCreated(event); err != nil {
		span.RecordError(err)
		span.SetStatus(codes.Error, err.Error())
		return fmt.Errorf("publish charge.created for author %s: %w", user.AuthorID, err)
	}

	observability.BillingChargesCreatedTotal.Inc()
	j.log.Info("BillingJob: charge.created event published",
		zap.String("author_id", user.AuthorID),
		zap.String("email", user.Email),
		zap.Int("month", month),
		zap.Int("year", year),
	)
	return nil
}
