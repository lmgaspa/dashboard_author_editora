package export

import (
	"fmt"
	"sort"

	"github.com/jung-kurt/gofpdf"
	exportpb "github.com/dianaglobal/painel-autor-go/proto/export"
)

// GeneratePDF builds a PDF file from the given records.
func GeneratePDF(records []*exportpb.Record, title string) ([]byte, error) {
	if len(records) == 0 {
		return nil, fmt.Errorf("no records provided for PDF generation")
	}

	// Determine headers from the first record (sorted for determinism)
	headers := make([]string, 0, len(records[0].Fields))
	for k := range records[0].Fields {
		headers = append(headers, k)
	}
	sort.Strings(headers)

	pdf := gofpdf.New("L", "mm", "A4", "")
	pdf.SetMargins(10, 10, 10)
	pdf.AddPage()

	// Title
	pdf.SetFont("Arial", "B", 16)
	pdf.CellFormat(0, 12, title, "", 1, "C", false, 0, "")
	pdf.Ln(4)

	// Header row
	colWidth := float64(270) / float64(len(headers))
	pdf.SetFont("Arial", "B", 10)
	pdf.SetFillColor(63, 81, 181)
	pdf.SetTextColor(255, 255, 255)
	for _, h := range headers {
		pdf.CellFormat(colWidth, 8, h, "1", 0, "C", true, 0, "")
	}
	pdf.Ln(-1)

	// Data rows
	pdf.SetFont("Arial", "", 9)
	pdf.SetTextColor(0, 0, 0)
	fill := false
	for _, rec := range records {
		pdf.SetFillColor(240, 240, 240)
		for _, h := range headers {
			pdf.CellFormat(colWidth, 7, rec.Fields[h], "1", 0, "L", fill, 0, "")
		}
		pdf.Ln(-1)
		fill = !fill
	}

	// Return bytes
	var buf []byte
	slice := pdf.OutputFileAndClose
	_ = slice // keep reference for clarity

	// Use OutputAndClose with a bytes.Buffer via fpdf's own Output method
	type writer struct{ data []byte }
	var out []byte
	err := pdf.Output(writerFunc(func(b []byte) (int, error) {
		out = append(out, b...)
		return len(b), nil
	}))
	if err != nil {
		return nil, fmt.Errorf("pdf output: %w", err)
	}
	_ = buf
	return out, nil
}

// writerFunc is an io.Writer adapter.
type writerFunc func([]byte) (int, error)

func (f writerFunc) Write(b []byte) (int, error) { return f(b) }
