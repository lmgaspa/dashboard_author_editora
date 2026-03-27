package export

import (
	"bytes"
	"encoding/csv"
	"fmt"
	"sort"

	exportpb "github.com/dianaglobal/painel-autor-go/proto/export"
)

// GenerateCSV builds a CSV file from the given records.
// Column order is determined by sorting the keys of the first record.
func GenerateCSV(records []*exportpb.Record, title string) ([]byte, error) {
	if len(records) == 0 {
		return nil, fmt.Errorf("no records provided for CSV generation")
	}

	// Determine headers from the first record (sorted for determinism)
	headers := make([]string, 0, len(records[0].Fields))
	for k := range records[0].Fields {
		headers = append(headers, k)
	}
	sort.Strings(headers)

	var buf bytes.Buffer
	w := csv.NewWriter(&buf)

	// Write header row
	if err := w.Write(headers); err != nil {
		return nil, fmt.Errorf("csv write header: %w", err)
	}

	// Write data rows
	for _, rec := range records {
		row := make([]string, len(headers))
		for i, h := range headers {
			row[i] = rec.Fields[h]
		}
		if err := w.Write(row); err != nil {
			return nil, fmt.Errorf("csv write row: %w", err)
		}
	}

	w.Flush()
	if err := w.Error(); err != nil {
		return nil, fmt.Errorf("csv flush: %w", err)
	}

	return buf.Bytes(), nil
}
