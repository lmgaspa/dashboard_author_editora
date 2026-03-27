# ☕ Painel do Autor — Java Spring Backend

> REST API for the Author Dashboard platform. Handles authentication, payments, billing, deliveries, tickets, and e-commerce integrations for publishers.

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.11-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-336791?logo=postgresql&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?logo=rabbitmq&logoColor=white)
![gRPC](https://img.shields.io/badge/gRPC-1.62-244c5a?logo=grpc&logoColor=white)

---

## 🗺️ What This Service Does

This is the **core API** of the platform. Every action performed by an author or administrator flows through this service:

- Authors log in and access their personal panel filtered strictly by their `author_id`
- Admins create user accounts, manage billing, and monitor the system
- The service connects to each author's **separate e-commerce database** to pull their sales, payment, and delivery data
- Monthly charges are created with **PIX QR codes** via the EFI payment gateway
- Events (billing, auth) are published to **RabbitMQ** and consumed by the Go microservices
- PDF/CSV exports are delegated to the **Go export microservice** via gRPC

---

## 🏗️ Architecture

This service follows **Hexagonal Architecture** (Ports & Adapters), keeping business logic completely isolated from infrastructure.

```
┌────────────────────────────────────────────────┐
│                  HTTP Layer                     │
│         REST Controllers (Adapters In)          │
└───────────────────┬────────────────────────────┘
                    │
┌───────────────────▼────────────────────────────┐
│              Application Layer                  │
│     Use Cases · Services · Event Listeners      │
└───────────┬───────────────────┬────────────────┘
            │                   │
┌───────────▼──────┐  ┌─────────▼───────────────┐
│   Domain Layer   │  │    Adapters Out           │
│ Pure business    │  │  JPA · RabbitMQ · SMTP   │
│ models & logic   │  │  EFI (PIX) · gRPC Client │
└──────────────────┘  └─────────────────────────┘
```

### Layers

| Layer | Location | Responsibility |
|---|---|---|
| **Domain** | `domain/model/` | Pure business models, no framework dependencies |
| **Application** | `application/service/` | Use cases, service orchestration |
| **Adapter In** | `adapter/in/web/` | REST controllers, DTOs |
| **Adapter Out** | `adapter/out/` | JPA repos, messaging, email, payment, gRPC |
| **Config** | `config/` | Spring Security, CORS, AMQP, Mail, Swagger |

---

## ⚙️ Technology Stack

| Component | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 3.4.11 |
| Security | Spring Security 6 + JWT (jjwt 0.11.5) |
| Database | PostgreSQL + Spring Data JPA + Flyway |
| Messaging | RabbitMQ (Spring AMQP) |
| Payment | EFI SDK 1.2.2 (PIX/QR code) |
| Export | Apache PDFBox 3.0.3 + gRPC client |
| Documentation | Springdoc OpenAPI 2.8.13 |
| Utilities | Lombok, Bean Validation |

---

## 🔐 Authentication & Security

### How Authentication Works

```
Client → POST /api/v1/auth/login
       ← JWT access token (15 min) + HttpOnly refresh cookie

Client → Any protected endpoint
       → Authorization: Bearer <token>
       ← JwtAuthenticationFilter validates and injects SecurityContext

Client → POST /api/v1/auth/refresh-token
       ← New access token (token rotation on every refresh)
```

### Security Model

- **JWT**: HS256, 15-minute access tokens, stateless (no server-side sessions)
- **BCrypt**: Password hashing with adaptive strength
- **CSRF**: Disabled (stateless REST API)
- **CORS**: Configured for Vercel deployment + localhost dev
- **Rate Limiting**: Throttling on email resend operations
- **Data Isolation**: Every query is filtered by `author_id` — users cannot access other authors' data

### Roles

| Role | Access |
|---|---|
| `ADMIN` | Full access — user management, billing confirmation, all author data |
| `USER` | Own data only — filtered by `author_id` on every request |

> **Registration is admin-only.** There is no self-registration. Admins create all accounts.

---

## 🏢 Multi-Tenancy: Per-Author E-commerce Databases

Each author has their own e-commerce database. The system stores per-user JDBC credentials:

```
users table
├── ecommerce_db_url       → jdbc:postgresql://<host>/<db>
├── ecommerce_db_username
└── ecommerce_db_password
```

When an author makes a request, the service:
1. Retrieves their `author_id` from the JWT
2. Fetches their e-commerce DB credentials from `users`
3. Opens a JDBC connection to their specific database
4. Runs all queries with `WHERE books.author_id = ?`

This ensures **complete data isolation** — no shared tables between authors.

---

## 📦 Core Modules

### 💰 Payments (`/api/v1/autor/pagamentos`)
Reads from the author's e-commerce database. Aggregates:
- Total sold amount
- Total received (paid payouts)
- Pending payouts
- Recent confirmed orders
- Revenue breakdown

### 💳 Monthly Charges (`/api/v1/cobrancas`)
Platform subscription fees per author:
- Admin creates charges → EFI generates PIX QR code automatically
- Authors pay via PIX
- Admin confirms payment → triggers confirmation email to author
- Overdue detection runs daily at 06:00 → publishes overdue events to RabbitMQ

### 🎫 Support Tickets (`/api/v1/tickets`)
Full ticketing system:
- Authors open tickets (free text)
- **Automatic priority classification** via keyword scoring:
  - HIGH (≥5 pts): "urgente", "não funciona", "vencido", PAGAMENTO category
  - MEDIUM (≥3 pts): "problema", "erro", "dúvida"
  - LOW (<3 pts): "sugestão", "melhoria"
- Status flow: `OPEN → IN_PROGRESS → WAITING_* → RESOLVED → CLOSED`
- Threaded messages + admin-only internal notes
- Overdue charges **automatically open a ticket**

### 📦 Deliveries (`/api/v1/entregas`)
Reads from e-commerce database. Manages shipping status:
- `PENDING → PROCESSING → SHIPPED → DELIVERED → RETURNED`
- Per-order tracking codes
- Archived (ENTREGUE) orders filterable separately

### 📧 Emails Panel (`/api/v1/autor/emails`)
Email history pulled from e-commerce database:
- Client emails with coupon tracking
- Payout notification history
- Statistics: total orders, confirmed, transferred amounts

### 📄 Export System
All modules support `?format=pdf|csv|json`:
- JSON: served directly by this API
- PDF/CSV: delegated to the Go export microservice via gRPC (when `export.grpc.enabled=true`)
- Fallback: local PDFBox generation when gRPC is disabled

---

## 🔗 Integrations

### RabbitMQ — Event Publishing
This service **publishes** events; the Go email worker **consumes** them.

| Event | Trigger |
|---|---|
| `billing.charge.created` | New monthly charge created |
| `billing.payment.confirmed` | Admin confirms author payment |
| `billing.charge.overdue` | Daily job detects overdue charge |
| `auth.password.reset` | User requests password reset |
| `auth.account.confirm` | New account confirmation |
| `auth.welcome` | First login after account creation |

Exchange: `painel.events` (durable topic exchange)

### EFI Payment Gateway (PIX)
- Creates immediate PIX charges with 5-day expiry
- Returns QR code string and image URL
- Certificate-based P12 authentication
- Sandbox/production toggle via config

### gRPC Export Service
- Connects to Go export microservice at `EXPORT_GRPC_HOST:EXPORT_GRPC_PORT`
- Calls `exportCharges()`, `exportTickets()`, `exportDeliveries()`
- Passes raw record maps; Go service generates the binary file
- Toggled via `export.grpc.enabled`

---

## 🌐 API Overview

### Public

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Login → JWT token |
| `POST` | `/api/v1/auth/forgot-password` | Request password reset |
| `POST` | `/api/v1/auth/reset-password` | Reset with token |
| `GET/POST` | `/api/v1/confirm-account` | Email confirmation |

### Author (requires JWT)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/autor/pagamentos/painel` | Payment dashboard |
| `GET` | `/api/v1/autor/emails/painel` | Email dashboard |
| `GET` | `/api/v1/entregas` | Delivery list |
| `PUT` | `/api/v1/entregas/{id}/status` | Update shipping status |
| `GET` | `/api/v1/cobrancas` | Monthly charges |
| `GET` | `/api/v1/tickets` | Ticket list |
| `POST` | `/api/v1/tickets` | Open ticket |
| `POST` | `/api/v1/tickets/{id}/messages` | Reply to ticket |
| `GET` | `/api/v1/*/export?format=pdf\|csv\|json` | Export any module |

### Admin only

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/admin/users` | Create user |
| `GET` | `/api/v1/admin/users` | List all users |
| `PUT` | `/api/v1/admin/cobrancas/{id}/confirmar` | Confirm payment |
| `GET` | `/api/v1/admin/database/status` | DB health |
| `GET` | `/api/v1/admin/authors/{id}/stats` | Author stats |

Full interactive documentation at `/swagger`.

---

## 🗄️ Database Schema

Managed by Flyway (V1 → V16):

| Table | Purpose |
|---|---|
| `users` | Accounts with roles, author_id, e-commerce credentials |
| `monthly_charges` | Billing with PIX QR codes and status |
| `tickets` | Support tickets with auto-priority |
| `ticket_messages` | Threaded ticket replies |
| `order_shipping` | Delivery tracking per order |
| `password_reset_tokens` | Time-limited reset links |
| `email_change_tokens` | Dual-confirmation email change |
| `account_confirmation_tokens` | Email verification |
| `refresh_tokens` | JWT refresh with rotation |
| `confirm_resend_throttle` | Rate limiting on email resends |

---

## ⚙️ Environment Variables

| Variable | Description |
|---|---|
| `DATABASE_URL` | Main PostgreSQL JDBC URL |
| `DATABASE_USERNAME` | DB username |
| `DATABASE_PASSWORD` | DB password |
| `JWT_SECRET` | HS256 signing secret |
| `RABBITMQ_HOST/PORT/USERNAME/PASSWORD/VHOST` | RabbitMQ connection |
| `MAIL_HOST/PORT/USERNAME/PASSWORD` | SMTP credentials |
| `EFI_CLIENT_ID/SECRET/PIX_KEY` | EFI payment gateway |
| `EXPORT_GRPC_ENABLED/HOST/PORT` | gRPC export service |
| `ADMIN_EMAIL/USERNAME/PASSWORD` | Initial admin account (Flyway V4) |
| `FRONTEND_BASE_URL` | CORS + email link base URL |

---

## 📁 Project Structure

```
backendjavaspring/
├── pom.xml
└── src/main/
    ├── java/com/dianaglobal/paineldoauthorbackend/
    │   ├── adapter/
    │   │   ├── in/
    │   │   │   ├── web/          # 19 REST controllers
    │   │   │   └── dto/          # Request/response DTOs
    │   │   └── out/
    │   │       ├── persistence/  # JPA entities + repositories
    │   │       ├── messaging/    # RabbitMQ event publisher
    │   │       ├── mail/         # Email services (8 types)
    │   │       ├── efi/          # PIX payment integration
    │   │       └── grpc/         # Export service gRPC client
    │   ├── application/
    │   │   ├── service/          # 25+ service implementations
    │   │   ├── port/in/          # Use case interfaces
    │   │   ├── port/out/         # Repository interfaces
    │   │   └── event/            # Application event listeners
    │   ├── domain/model/         # 11 pure domain models
    │   └── config/               # Spring configuration classes
    ├── resources/
    │   ├── application.yml
    │   └── db/migration/         # V1–V16 Flyway SQL migrations
    └── proto/export/             # Protobuf definitions for gRPC
```

---

## 📝 License

Private and proprietary — **Andescore Software**
