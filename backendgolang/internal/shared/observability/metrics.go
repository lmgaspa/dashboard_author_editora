package observability

import "github.com/prometheus/client_golang/prometheus"

// ── Email Worker ──────────────────────────────────────────────────────────────

var EmailsSentTotal = prometheus.NewCounterVec(
	prometheus.CounterOpts{
		Name: "emails_sent_total",
		Help: "Total number of emails successfully sent, by event type.",
	},
	[]string{"event_type"},
)

var EmailsFailedTotal = prometheus.NewCounterVec(
	prometheus.CounterOpts{
		Name: "emails_failed_total",
		Help: "Total number of failed email deliveries, by event type.",
	},
	[]string{"event_type"},
)

var MessagesConsumedTotal = prometheus.NewCounterVec(
	prometheus.CounterOpts{
		Name: "rabbitmq_messages_consumed_total",
		Help: "Total RabbitMQ messages consumed, by queue and status (ack/nack).",
	},
	[]string{"queue", "status"},
)

// ── Billing Worker ────────────────────────────────────────────────────────────

var BillingChargesCreatedTotal = prometheus.NewCounter(
	prometheus.CounterOpts{
		Name: "billing_charges_created_total",
		Help: "Total monthly charges created by the billing job.",
	},
)

var BillingOverdueDetectedTotal = prometheus.NewCounter(
	prometheus.CounterOpts{
		Name: "billing_overdue_detected_total",
		Help: "Total overdue charges detected and published.",
	},
)

var BillingJobDurationSeconds = prometheus.NewHistogramVec(
	prometheus.HistogramOpts{
		Name:    "billing_job_duration_seconds",
		Help:    "Duration of each billing cron job run.",
		Buckets: prometheus.DefBuckets,
	},
	[]string{"job"},
)

// ── Export Service ────────────────────────────────────────────────────────────

var ExportsGeneratedTotal = prometheus.NewCounterVec(
	prometheus.CounterOpts{
		Name: "exports_generated_total",
		Help: "Total export files generated, by type and format.",
	},
	[]string{"type", "format"},
)

var ExportsFailedTotal = prometheus.NewCounterVec(
	prometheus.CounterOpts{
		Name: "exports_failed_total",
		Help: "Total failed export attempts, by type.",
	},
	[]string{"type"},
)

var ExportDurationSeconds = prometheus.NewHistogramVec(
	prometheus.HistogramOpts{
		Name:    "export_duration_seconds",
		Help:    "Time to generate an export file.",
		Buckets: []float64{0.05, 0.1, 0.25, 0.5, 1, 2.5, 5},
	},
	[]string{"type", "format"},
)

func init() {
	prometheus.MustRegister(
		// email
		EmailsSentTotal,
		EmailsFailedTotal,
		MessagesConsumedTotal,
		// billing
		BillingChargesCreatedTotal,
		BillingOverdueDetectedTotal,
		BillingJobDurationSeconds,
		// export
		ExportsGeneratedTotal,
		ExportsFailedTotal,
		ExportDurationSeconds,
	)
}
