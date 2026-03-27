package main

import (
	"context"
	"fmt"
	"net"
	"os"
	"os/signal"
	"syscall"

	"go.opentelemetry.io/contrib/instrumentation/google.golang.org/grpc/otelgrpc"
	"go.uber.org/zap"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"

	"github.com/dianaglobal/painel-autor-go/internal/export"
	"github.com/dianaglobal/painel-autor-go/internal/shared/config"
	"github.com/dianaglobal/painel-autor-go/internal/shared/observability"
	exportpb "github.com/dianaglobal/painel-autor-go/proto/export"
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
		tp, err := observability.InitTracer(ctx, "export-service")
		if err != nil {
			log.Warn("failed to init tracer, continuing without tracing", zap.Error(err))
		} else {
			defer tp.Shutdown(context.Background())
		}
	}
	observability.StartMetricsServer(cfg.MetricsPort+1, log) // +1 to avoid port clash if co-located

	addr := fmt.Sprintf(":%d", cfg.GRPCPort)
	lis, err := net.Listen("tcp", addr)
	if err != nil {
		log.Fatal("failed to listen", zap.String("addr", addr), zap.Error(err))
	}

	// gRPC server with OTel stats handler for traces + metrics per call
	grpcServer := grpc.NewServer(
		grpc.StatsHandler(otelgrpc.NewServerHandler()),
	)
	exportpb.RegisterExportServiceServer(grpcServer, export.NewServer(log))
	reflection.Register(grpcServer)

	go func() {
		log.Info("export-service started",
			zap.String("grpc_addr", addr),
			zap.Int("metrics_port", cfg.MetricsPort+1),
			zap.Bool("otel_enabled", cfg.OTELEnabled),
		)
		if err := grpcServer.Serve(lis); err != nil {
			log.Error("gRPC server error", zap.Error(err))
		}
	}()

	<-ctx.Done()
	log.Info("export-service shutting down")
	grpcServer.GracefulStop()
}
