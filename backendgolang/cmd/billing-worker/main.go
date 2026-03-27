package main

import (
	"context"
	"database/sql"
	"os"
	"os/signal"
	"syscall"

	"github.com/robfig/cron/v3"
	"go.uber.org/zap"

	"github.com/dianaglobal/painel-autor-go/internal/billing"
	"github.com/dianaglobal/painel-autor-go/internal/shared/config"
	"github.com/dianaglobal/painel-autor-go/internal/shared/observability"
	"github.com/dianaglobal/painel-autor-go/internal/shared/rabbitmq"
	redisclient "github.com/dianaglobal/painel-autor-go/internal/shared/redis"

	_ "github.com/lib/pq"
)

func main() {
	log, _ := zap.NewProduction()
	defer log.Sync()

	cfg, err := config.Load()
	if err != nil {
		log.Fatal("failed to load config", zap.Error(err))
	}

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	// Observability
	if cfg.OTELEnabled {
		tp, err := observability.InitTracer(ctx, "billing-worker")
		if err != nil {
			log.Warn("failed to init tracer, continuing without tracing", zap.Error(err))
		} else {
			defer tp.Shutdown(context.Background())
		}
	}
	observability.StartMetricsServer(cfg.MetricsPort, log)

	// PostgreSQL
	db, err := sql.Open("postgres", cfg.PostgresURL)
	if err != nil {
		log.Fatal("failed to open postgres connection", zap.Error(err))
	}
	defer db.Close()
	if err := db.Ping(); err != nil {
		log.Fatal("failed to ping postgres", zap.Error(err))
	}

	// RabbitMQ
	conn, ch, err := rabbitmq.Connect(cfg.RabbitMQURL)
	if err != nil {
		log.Fatal("failed to connect to RabbitMQ", zap.Error(err))
	}
	defer ch.Close()
	defer conn.Close()

	// Redis
	redisClient, err := redisclient.NewClient(cfg.RedisURL)
	if err != nil {
		log.Fatal("failed to connect to Redis", zap.Error(err))
	}
	defer redisClient.Close()

	// Repository and publisher
	repo := billing.NewRepository(db)
	publisher := billing.NewPublisher(ch, log)

	// Jobs
	billingJob := billing.NewBillingJob(repo, publisher, redisClient, log)
	overdueJob := billing.NewOverdueChargeDetectionJob(repo, publisher, redisClient, log)

	// Cron scheduler
	c := cron.New(cron.WithSeconds())
	if _, err := c.AddJob("0 0 8 * * *", billingJob); err != nil {
		log.Fatal("failed to schedule BillingJob", zap.Error(err))
	}
	if _, err := c.AddJob("0 0 6 * * *", overdueJob); err != nil {
		log.Fatal("failed to schedule OverdueChargeDetectionJob", zap.Error(err))
	}

	c.Start()
	log.Info("billing-worker started",
		zap.Int("metrics_port", cfg.MetricsPort),
		zap.Bool("otel_enabled", cfg.OTELEnabled),
	)

	<-ctx.Done()
	log.Info("billing-worker shutting down")

	stopCtx := c.Stop()
	<-stopCtx.Done()
	log.Info("billing-worker stopped")
}
