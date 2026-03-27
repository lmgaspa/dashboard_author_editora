# 📊 Painel do Autor — Author Dashboard Platform

> A complete author management platform for publishers. Authors access their sales, payments, deliveries, billing, and support through a single dashboard. Built as a multi-service system with strict per-author data isolation and full observability.

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.11-6DB33F?logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)
![Go](https://img.shields.io/badge/Go-1.24-00ADD8?logo=go&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-20-DD0031?logo=angular&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-336791?logo=postgresql&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?logo=rabbitmq&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?logo=mongodb&logoColor=white)
![OpenTelemetry](https://img.shields.io/badge/OpenTelemetry-000000?logo=opentelemetry&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?logo=grafana&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?logo=prometheus&logoColor=white)

---

## 🧭 How the System Works

The platform serves two types of users — **authors** and **administrators** — through a publisher's branded portal.

**Authors** log in and see only their own data: books sold, payments received, pending deliveries, monthly subscription charges, and support tickets. All data is pulled from that author's specific e-commerce database, keeping every author completely isolated from others.

**Administrators** manage the platform: create user accounts, issue monthly charges with PIX payment codes, confirm payments, respond to tickets, and monitor system health.

Every user action flows through the Java API. Async tasks (sending emails, running daily billing jobs, generating export files) are handled by dedicated Go microservices. Every service ships traces and metrics to Grafana Cloud via OpenTelemetry.

---

## 🏛️ System Architecture

```
                    ┌─────────────────────────────┐
                    │      Angular Frontend        │
                    │  Author panel · Admin panel  │
                    └──────────────┬──────────────┘
                                   │ HTTPS REST
                    ┌──────────────▼──────────────┐
                    │    Java Spring Backend       │
                    │  Java 25 · Spring Boot 3.4   │
                    │  Auth · Payments · Tickets   │
                    │  Billing · Deliveries · API  │
                    └───┬─────────────────┬───────┘
                        │ RabbitMQ        │ gRPC
              ┌─────────▼──────┐  ┌──────▼──────────────┐
              │  Email Worker  │  │   Export Service     │
              │  Billing Jobs  │  │  PDF · CSV generator │
              │  (Go workers)  │  │  (Go gRPC server)    │
              └────────────────┘  └─────────────────────┘
                        │                 │
                        └────────┬────────┘
                    ┌────────────▼────────────────┐
                    │       Grafana Cloud          │
                    │  Tempo · Mimir · Dashboards  │
                    │  Traces · Metrics · Alerts   │
                    └─────────────────────────────┘

Databases:
  ├── PostgreSQL        → Main platform data (users, charges, tickets)
  ├── PostgreSQL (×N)   → One per author — e-commerce orders & payments
  ├── MongoDB           → Email delivery logs
  └── Redis             → Distributed locks for billing jobs
```

---

## 📦 Services

### 🌐 Frontend — `frontend/`

Angular 20 single-page application hosted on Vercel.

- **Author Panel**: Dashboard with charts, payment history, delivery tracking, monthly charges, ticket system, email export
- **Admin Panel**: User management, billing control, system monitoring
- Charts powered by **ECharts**, styled with **TailwindCSS**
- Communicates exclusively with the Java REST API

### ☕ Java Spring Backend — `backendjavaspring/`

The main REST API. Every feature of the platform is exposed here.

- **Authentication**: JWT (15-min tokens + rotation), BCrypt passwords, admin-only registration
- **Multi-tenancy**: Each author maps to their own e-commerce JDBC database; all queries filter by `author_id`
- **Payments panel**: Reads sales, payouts, and revenue from the author's e-commerce DB
- **Monthly billing**: Admins create charges → EFI generates PIX QR codes → author pays → admin confirms → email sent
- **Ticket system**: Auto-priority classification by keyword scoring, threaded replies, internal admin notes
- **Delivery tracking**: Reads confirmed orders from e-commerce DB, tracks shipping status
- **Email system**: Publishes events to RabbitMQ; Go worker handles delivery
- **PDF/CSV export**: Delegates to Go gRPC export service; falls back to Apache PDFBox locally
- **Flyway migrations**: V1–V16, including a Java migration that seeds the initial admin account from env vars
- **Observability**: Traces + metrics pushed to Grafana Cloud via OTLP every 30s

### 🐹 Go Microservices — `backendgolang/`

Three independent binaries built for async and scheduled work:

| Binary | What it does |
|---|---|
| `email-worker` | Consumes RabbitMQ events and sends transactional emails via SMTP. Logs every delivery to MongoDB. |
| `billing-worker` | Runs two cron jobs: generates monthly charges at 08:00 and detects overdue charges at 06:00. Uses Redis to prevent duplicate processing. |
| `export-service` | gRPC server on port 50051. Receives raw record data from the Java backend and returns PDF or CSV binary. |

---

## 📡 Observability Stack

All services are fully instrumented with **OpenTelemetry**, **Prometheus metrics**, and **Grafana Cloud** dashboards. No self-hosted infrastructure required — metrics and traces are pushed directly to Grafana Cloud via OTLP.

```
Java Spring Boot                Go microservices
─────────────────               ─────────────────
Micrometer OTLP Registry   →   OTel SDK (metrics + traces)
OTel Tracing Bridge        →   prometheus/client_golang
     │                              │
     └──────────── OTLP ────────────┘
                       │
              Grafana Cloud
         ┌─────────────────────┐
         │  Tempo   (traces)   │
         │  Mimir   (metrics)  │
         │  Grafana (dashboards│
         └─────────────────────┘
```

### What We Collect

#### Java Spring Boot
| Metric | Description |
|---|---|
| `auth_login_success_total` | Total successful logins |
| `auth_login_failure_total` | Total failed login attempts |
| `auth_password_reset_requested_total` | Password reset requests |
| `tickets_created_total` | Support tickets opened |
| `tickets_resolved_total` | Support tickets resolved |
| `charges_created_total` | Monthly charges issued |
| `payments_confirmed_total` | Payments confirmed by admins |
| `users_created_total` | User accounts created |
| `exports_requested_total` | Export requests (PDF/CSV/JSON) |
| `http_server_requests_seconds` | HTTP request rate, latency p50/p95/p99, error rate |
| `jvm_memory_used_bytes` | JVM heap and non-heap usage |
| `jvm_threads_live_threads` | Active JVM thread count |
| `jvm_gc_pause_seconds` | Garbage collection pause duration |

#### Go Microservices
| Metric | Description |
|---|---|
| `emails_sent_total{event_type}` | Emails delivered by type |
| `emails_failed_total{event_type}` | Email delivery failures |
| `rabbitmq_messages_consumed_total{queue,status}` | RabbitMQ ack/nack per queue |
| `billing_charges_created_total` | Monthly charges generated by billing job |
| `billing_overdue_detected_total` | Overdue charges detected and published |
| `billing_job_duration_seconds{job}` | Duration of each billing cron run |
| `exports_generated_total{type,format}` | Export files generated |
| `exports_failed_total{type}` | Export failures by type |
| `export_duration_seconds{type,format}` | Export generation latency p50/p95 |

#### Distributed Tracing
Every HTTP request, RabbitMQ handler, gRPC call, billing job, and export operation is traced end-to-end with spans propagated via W3C Trace Context. Traces are stored in **Grafana Tempo** and linkable from dashboard panels.

### Grafana Dashboard

A ready-to-import dashboard JSON is included at `grafana-dashboard.json`. It covers all metrics above, organized into rows:

- **Overview** — 6 stat panels with key business totals
- **Auth** — login rate, failure rate, password resets
- **Business** — tickets, charges, payments timeline
- **HTTP API** — request rate, error rate, p50/p95/p99 latency
- **JVM** — heap memory, threads, GC pauses
- **Email Worker** — emails by type, failures, RabbitMQ throughput
- **Billing Worker** — charges created, overdue, job duration
- **Export Service** — exports by type, failures, generation latency

To import: **Grafana Cloud → Dashboards → Import → Upload `grafana-dashboard.json`**

---

## 🔄 Key Data Flows

### Author Login & Data Access

```
Author logs in → Java validates credentials → JWT issued
Author requests payment panel → Java reads author_id from JWT
→ Fetches author's e-commerce DB credentials from users table
→ Opens JDBC connection to author's private DB
→ Queries orders/payouts filtered by author_id
→ Returns aggregated data
```

### Monthly Billing

```
08:00 daily → Go billing-worker creates charge in PostgreSQL
→ Publishes billing.charge.created to RabbitMQ
→ Go email-worker sends charge notification with PIX code to author

Author pays via PIX → Admin confirms in platform
→ Java publishes billing.payment.confirmed to RabbitMQ
→ Go email-worker sends payment confirmed email to author
```

### Overdue Detection

```
06:00 daily → Go overdue job finds unpaid charges past due date
→ Checks if ticket already exists for that charge
→ If not: creates support ticket automatically in PostgreSQL
→ Publishes billing.charge.overdue to RabbitMQ
→ Go email-worker sends overdue alert to author AND all admins
```

### PDF/CSV Export

```
Author requests export → Java collects records from DB
→ If gRPC enabled: sends records to Go export-service via gRPC
→ Go generates PDF (gofpdf, landscape A4) or CSV
→ Returns binary to Java → Java streams to browser
```

---

## 🔐 Security Model

- **No self-registration** — admins create all accounts
- **JWT stateless auth** — no server-side sessions
- **Author isolation** — every query enforces `author_id` filtering; a user cannot read another author's data
- **Per-user e-commerce credentials** — stored in the `users` table, never shared
- **BCrypt** password hashing
- **CORS** restricted to known origins (Vercel + `paineldavia.com.br`)
- **Rate limiting** on email resend operations

---

## 🗄️ Databases

| Database | Engine | Used By | Data |
|---|---|---|---|
| Main platform DB | PostgreSQL | Java, Go billing | Users, charges, tickets, deliveries, tokens |
| Author e-commerce DBs | PostgreSQL (×N) | Java | Orders, payouts, books — one DB per author |
| Email logs | MongoDB | Go email-worker | Delivery history, status, timestamps |
| Job locks | Redis | Go billing-worker | Distributed locks (1h TTL) |

---

## ⚙️ Environment Variables

### Java Spring Backend

| Variable | Description |
|---|---|
| `DATABASE_URL` | Main PostgreSQL JDBC URL |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | DB credentials |
| `JWT_SECRET` | HS256 signing secret |
| `RABBITMQ_HOST/PORT/USERNAME/PASSWORD/VHOST` | RabbitMQ connection |
| `MAIL_HOST/PORT/USERNAME/PASSWORD` | SMTP credentials |
| `EFI_CLIENT_ID/SECRET/PIX_KEY` | EFI payment gateway |
| `EXPORT_GRPC_ENABLED/HOST/PORT` | gRPC export service toggle |
| `ADMIN_EMAIL/USERNAME/PASSWORD` | Initial admin account |
| `FRONTEND_BASE_URL` | CORS + email link base URL |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | Grafana Cloud OTLP base URL |
| `OTEL_BASE` | `Basic <base64>` authorization header for OTLP |

### Go Microservices

| Variable | Description |
|---|---|
| `RABBITMQ_URL` | RabbitMQ AMQP connection string |
| `MONGODB_URL` / `MONGODB_DB` | Email log storage |
| `REDIS_URL` | Distributed lock store |
| `POSTGRES_URL` | Main platform DB |
| `SMTP_HOST/PORT/USERNAME/PASSWORD` | Email delivery |
| `GRPC_PORT` | Export service gRPC port |
| `METRICS_PORT` | Prometheus metrics HTTP port |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | Grafana Cloud OTLP base URL |
| `OTEL_EXPORTER_OTLP_HEADERS` | `Authorization=Basic <base64>` |
| `OTEL_EXPORTER_OTLP_PROTOCOL` | `http/protobuf` |

---

## 📁 Repository Structure

```
Dashboard_Editora/
├── backendjavaspring/   # Java 25 + Spring Boot 3.4.11 REST API
├── backendgolang/       # Go 1.24 microservices (email, billing, export)
├── frontend/            # Angular 20 SPA (Vercel)
└── grafana-dashboard.json  # Grafana Cloud dashboard — import directly
```

---

## 👥 Built by

**Andescore Software** — private and proprietary.
