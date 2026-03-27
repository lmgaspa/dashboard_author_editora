package main

import (
	"context"
	"os"
	"os/signal"
	"syscall"

	"go.uber.org/zap"

	"github.com/dianaglobal/painel-autor-go/internal/email"
	"github.com/dianaglobal/painel-autor-go/internal/shared/config"
	"github.com/dianaglobal/painel-autor-go/internal/shared/mongodb"
	"github.com/dianaglobal/painel-autor-go/internal/shared/observability"
	"github.com/dianaglobal/painel-autor-go/internal/shared/rabbitmq"
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
		tp, err := observability.InitTracer(ctx, "email-worker")
		if err != nil {
			log.Warn("failed to init tracer, continuing without tracing", zap.Error(err))
		} else {
			defer tp.Shutdown(context.Background())
		}
	}
	observability.StartMetricsServer(cfg.MetricsPort, log)

	// MongoDB
	mongoClient, err := mongodb.NewClient(ctx, cfg.MongoDBURL)
	if err != nil {
		log.Fatal("failed to connect to MongoDB", zap.Error(err))
	}
	defer mongodb.Disconnect(context.Background(), mongoClient)
	mongoDB := mongoClient.Database(cfg.MongoDB)

	// RabbitMQ
	conn, ch, err := rabbitmq.Connect(cfg.RabbitMQURL)
	if err != nil {
		log.Fatal("failed to connect to RabbitMQ", zap.Error(err))
	}
	defer ch.Close()
	defer conn.Close()

	// SMTP sender
	sender := email.NewSMTPSender(
		cfg.SMTPHost,
		cfg.SMTPPort,
		cfg.SMTPUsername,
		cfg.SMTPPassword,
		cfg.SMTPFrom,
	)

	// Mongo logger
	mlogger := email.NewMongoLogger(mongoDB)

	// Consumer
	consumer, err := email.NewConsumer(conn, ch, sender, mlogger, log)
	if err != nil {
		log.Fatal("failed to create email consumer", zap.Error(err))
	}

	log.Info("email-worker started",
		zap.Int("metrics_port", cfg.MetricsPort),
		zap.Bool("otel_enabled", cfg.OTELEnabled),
	)

	if err := consumer.Start(ctx); err != nil {
		log.Error("consumer error", zap.Error(err))
	}

	log.Info("email-worker shutting down")
}
