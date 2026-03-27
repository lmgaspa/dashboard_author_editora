package export

import (
	"context"
	"fmt"

	"go.uber.org/zap"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"

	exportpb "github.com/dianaglobal/painel-autor-go/proto/export"
)

// Server implements the ExportService gRPC server.
type Server struct {
	exportpb.UnimplementedExportServiceServer
	log *zap.Logger
}

// NewServer creates a new export gRPC server.
func NewServer(log *zap.Logger) *Server {
	return &Server{log: log}
}

// ExportCharges generates a PDF or CSV export for charges.
func (s *Server) ExportCharges(ctx context.Context, req *exportpb.ExportRequest) (*exportpb.ExportResponse, error) {
	return s.export(ctx, req, "Cobranças")
}

// ExportTickets generates a PDF or CSV export for tickets.
func (s *Server) ExportTickets(ctx context.Context, req *exportpb.ExportRequest) (*exportpb.ExportResponse, error) {
	return s.export(ctx, req, "Tickets de Suporte")
}

// ExportDeliveries generates a PDF or CSV export for deliveries.
func (s *Server) ExportDeliveries(ctx context.Context, req *exportpb.ExportRequest) (*exportpb.ExportResponse, error) {
	return s.export(ctx, req, "Entregas")
}

func (s *Server) export(_ context.Context, req *exportpb.ExportRequest, title string) (*exportpb.ExportResponse, error) {
	if len(req.Records) == 0 {
		return nil, status.Error(codes.InvalidArgument, "records cannot be empty")
	}

	switch req.Format {
	case "pdf":
		data, err := GeneratePDF(req.Records, fmt.Sprintf("%s - Autor %s", title, req.AuthorId))
		if err != nil {
			s.log.Error("failed to generate PDF", zap.Error(err), zap.String("author_id", req.AuthorId))
			return nil, status.Errorf(codes.Internal, "pdf generation failed: %v", err)
		}
		return &exportpb.ExportResponse{
			Data:        data,
			Filename:    fmt.Sprintf("export_%s.pdf", req.AuthorId),
			ContentType: "application/pdf",
		}, nil

	case "csv":
		data, err := GenerateCSV(req.Records, title)
		if err != nil {
			s.log.Error("failed to generate CSV", zap.Error(err), zap.String("author_id", req.AuthorId))
			return nil, status.Errorf(codes.Internal, "csv generation failed: %v", err)
		}
		return &exportpb.ExportResponse{
			Data:        data,
			Filename:    fmt.Sprintf("export_%s.csv", req.AuthorId),
			ContentType: "text/csv",
		}, nil

	default:
		return nil, status.Errorf(codes.InvalidArgument, "unsupported format %q, use 'pdf' or 'csv'", req.Format)
	}
}
