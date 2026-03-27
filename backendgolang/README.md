# 🐹 Painel do Autor — Go Microservices

> Async workers and export microservice for the Author Dashboard platform. Handles email delivery, scheduled billing jobs, and PDF/CSV generation via gRPC.

![Go](https://img.shields.io/badge/Go-1.24-00ADD8?logo=go&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?logo=rabbitmq&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white)
![gRPC](https://img.shields.io/badge/gRPC-1.79-244c5a?logo=grpc&logoColor=white)

---

## 🗺️ What This Service Does

This Go backend is a collection of **three independent microservices** that offload async work from the Java Spring backend:

| Service | Binary | Responsibility |
|---|---|---|
| **Email Worker** | `cmd/email-worker` | Consumes RabbitMQ events and delivers emails via SMTP |
| **Billing Worker** | `cmd/billing-worker` | Cron jobs for monthly charge generation and overdue detection |
| **Export Service** | `cmd/export-service` | gRPC server that generates PDF and CSV files |

The Java backend **publishes events** and **calls gRPC** — this service handles the rest.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│          Java Spring Backend            │
│  Publishes events · Calls gRPC export   │
└──────┬──────────────────┬──────────────┘
       │ RabbitMQ          │ gRPC
       ▼                   ▼
┌─────────────┐   ┌─────────────────────┐
│Email Worker │   │   Export Service    │
│             │   │  PDF · CSV via gRPC │
│ SMTP sender │   └─────────────────────┘
│ MongoDB log │
└─────────────┘

┌──────────────────────────────────────────┐
│          Billing Worker (Cron)           │
│  06:00 → Overdue detection + publish     │
│  08:00 → Monthly charge creation + pub.  │
│  Redis distributed lock (idempotency)    │
└──────────────────────────────────────────┘
```

---

## 📦 Services

### ✉️ Email Worker — `cmd/email-worker`

Listens to two RabbitMQ queues and sends transactional emails:

**Queue: `email.billing.events`**
| Routing Key | Email Sent |
|---|---|
| `billing.charge.created` | New monthly charge notification with PIX code |
| `billing.payment.confirmed` | Payment confirmation to author |
| `billing.charge.overdue` | Overdue alert to author **and all admins** |

**Queue: `email.auth.events`**
| Routing Key | Email Sent |
|---|---|
| `auth.password.reset` | Password reset link |
| `auth.account.confirm` | Account confirmation link |
| `auth.welcome` | Welcome message |

Every email delivery attempt is logged to MongoDB (`email_logs` collection) with status, timestamp, event type, and recipient.

**SMTP Configuration:**
- Supports STARTTLS (port 587) and implicit TLS (port 465)
- HTML templates rendered dynamically with user data
- All templates written in **Portuguese (pt-BR)**

---

### 🕐 Billing Worker — `cmd/billing-worker`

Two cron jobs running on the same process:

#### Monthly Charge Job — runs at 08:00 daily
1. Queries PostgreSQL for all users with an `author_id`
2. Checks whether a charge already exists for the current month (idempotency)
3. Acquires a **Redis distributed lock** per `(author_id, month, year)` to prevent race conditions
4. Creates the monthly charge record in PostgreSQL
5. Publishes `billing.charge.created` event to RabbitMQ → email worker sends the notification

#### Overdue Detection Job — runs at 06:00 daily
1. Queries PostgreSQL for charges with status `OVERDUE` or `PENDING` past due date
2. Per charge, checks if a support ticket already exists (prevents duplicates)
3. Acquires Redis lock per `charge_id`
4. Calculates days overdue
5. Publishes `billing.charge.overdue` event → email worker sends alerts to author + all admins

**Redis locks** use 1-hour TTL and pattern `lock:billing:charge:{authorId}:{month}:{year}`.

---

### 📄 Export Service — `cmd/export-service`

A **gRPC server** (default port `50051`) that generates binary export files from raw record data sent by the Java backend.

**Proto contract:**
```protobuf
service ExportService {
  rpc ExportCharges(ExportRequest)   returns (ExportResponse);
  rpc ExportTickets(ExportRequest)   returns (ExportResponse);
  rpc ExportDeliveries(ExportRequest) returns (ExportResponse);
}

message ExportRequest {
  string format             // "pdf" or "csv"
  string author_id
  repeated Record records   // flexible field map
}

message ExportResponse {
  bytes  data               // binary file content
  string filename
  string content_type       // "application/pdf" or "text/csv"
}
```

**PDF output** (`gofpdf`):
- Landscape A4
- Dynamic columns from record field keys
- Alternating row colors, styled headers

**CSV output:**
- Alphabetically ordered columns for determinism
- Proper escaping

---

## 🔗 External Connections

| Service | Used By | Purpose |
|---|---|---|
| **RabbitMQ** | Email Worker, Billing Worker | Message queue for events |
| **PostgreSQL** | Billing Worker | Read users and charges |
| **MongoDB** | Email Worker | Log email delivery history |
| **Redis** | Billing Worker | Distributed locking for cron idempotency |
| **SMTP (Gmail)** | Email Worker | Transactional email delivery |

### RabbitMQ Topology

- **Exchange**: `painel.events` (durable, topic)
- **Dead-letter exchange**: `painel.events.dlx` (captures failed messages)
- **Message persistence**: all messages are durable
- **Consumer QoS**: prefetch count = 10

---

## ⚙️ Technology Stack

| Component | Library |
|---|---|
| Language | Go 1.24 |
| gRPC | `google.golang.org/grpc v1.79` |
| Protocol Buffers | `google.golang.org/protobuf v1.36` |
| RabbitMQ | `rabbitmq/amqp091-go v1.10` |
| MongoDB | `mongo-driver/v2 v2.5` |
| Redis | `redis/go-redis/v9 v9.18` |
| PostgreSQL | `lib/pq v1.12` |
| PDF Generation | `jung-kurt/gofpdf v1.16` |
| Cron Scheduling | `robfig/cron/v3 v3.0` |
| Configuration | `spf13/viper v1.21` |
| Logging | `uber-go/zap v1.27` |

---

## ⚙️ Environment Variables

All configuration is loaded via Viper from `.env`:

| Variable | Default | Description |
|---|---|---|
| `RABBITMQ_URL` | `amqp://guest:guest@localhost:5672/` | Full AMQP connection URL |
| `MONGODB_URL` | `mongodb://localhost:27017` | MongoDB connection string |
| `MONGODB_DB` | `painel_autor` | MongoDB database name |
| `REDIS_URL` | `redis://localhost:6379` | Redis connection URL |
| `SMTP_HOST` | `smtp.gmail.com` | SMTP server host |
| `SMTP_PORT` | `587` | SMTP server port |
| `SMTP_USERNAME` | — | SMTP login |
| `SMTP_PASSWORD` | — | SMTP password |
| `SMTP_FROM` | — | Sender email address |
| `POSTGRES_URL` | — | PostgreSQL connection URL |
| `GRPC_PORT` | `50051` | Export service listen port |
| `FRONTEND_BASE_URL` | `https://painel.andeseditora.com.br` | Used in email action links |

---

## 📁 Project Structure

```
backendgolang/
├── cmd/
│   ├── email-worker/main.go     # Email consumer entrypoint
│   ├── billing-worker/main.go   # Billing cron jobs entrypoint
│   └── export-service/main.go   # gRPC server entrypoint
├── internal/
│   ├── shared/
│   │   ├── config/config.go     # Viper configuration loader
│   │   ├── mongodb/client.go    # MongoDB connection
│   │   ├── rabbitmq/conn.go     # AMQP connection + utilities
│   │   └── redis/client.go      # Redis client + distributed lock
│   ├── email/
│   │   ├── consumer.go          # RabbitMQ message consumer
│   │   ├── sender.go            # SMTP email sender
│   │   ├── templates.go         # HTML email templates (pt-BR)
│   │   └── logger.go            # MongoDB email log writer
│   ├── billing/
│   │   ├── repository.go        # PostgreSQL queries
│   │   ├── job.go               # Monthly charge creation job
│   │   ├── overdue.go           # Overdue detection job
│   │   └── publisher.go         # RabbitMQ event publisher
│   └── export/
│       ├── server.go            # gRPC service implementation
│       ├── pdf.go               # PDF generation (gofpdf)
│       └── csv.go               # CSV generation
├── proto/export/export.proto    # Protobuf service definition
├── go.mod
└── .env
```

---

## 📝 License

Private and proprietary — **Andescore Software**
