# 📊 Painel do Autor Backend - Plataforma de Gestão de Autores e Métricas

Plataforma completa de gestão de autores, métricas, pagamentos, entregas, cobranças e emails com autenticação e controle de acesso, construída em Spring Boot e PostgreSQL.

## 🎯 O Que Faz

Uma plataforma completa de **gestão de autores** que fornece:
- 📊 **Dashboard de métricas** e análises por autor
- 💰 **Gestão de pagamentos e repasses** com histórico detalhado
- 📦 **Sistema de entregas** com rastreamento de pedidos
- 💳 **Cobranças mensais** e controle financeiro
- 📧 **Sistema de emails** (clientes e repasses) com informações de cupons
- 🎫 **Sistema de tickets** para suporte
- 📄 **Exportação PDF/CSV** para todos os módulos
- 👥 **Gestão de usuários** com controle administrativo
- 🔐 **Autenticação e autorização** robusta com JWT
- 🏢 **Multi-tenancy** com isolamento de dados por autor

A plataforma suporta múltiplos métodos de autenticação, controle de acesso baseado em roles, integração com bancos de dados de e-commerce externos e um sistema completo de emails em português.

## ✨ Key Features

### 🔑 Authentication & Authorization
- ✅ **Local Authentication** - Email/password login with JWT tokens
- ✅ **Google OAuth2 Integration** - Social login (requires pre-created accounts)
- ✅ **JWT Token Management** - Stateless authentication with refresh token rotation
- ✅ **Role-Based Access Control** - ADMIN and USER roles with granular permissions
- ✅ **CSRF Protection** - Built-in CSRF token validation
- ✅ **Session Management** - Server-side refresh tokens with revocation
- ✅ **Password Reset** - Secure password recovery via email tokens

### 👥 User Management
- ✅ **Admin-Only Registration** - Strict control: only admins create accounts
- ✅ **User Creation API** - Admins can create users with custom roles
- ✅ **Password Management** - Secure password hashing, reset, and change flows
- ✅ **Email Verification** - Account confirmation with rate limiting
- ✅ **Email Change** - Secure email change with dual confirmation
- ✅ **Profile Management** - User profile with photo and personal information
- ✅ **Author ID Assignment** - Each user linked to an author_id for data isolation

### 💰 Payment Management
- ✅ **Payment Dashboard** - Complete payment overview per author
- ✅ **Payment History** - Detailed payment history with filters
- ✅ **Payout Tracking** - Track all payouts and their status
- ✅ **Revenue Analytics** - Total revenue, paid amounts, and pending calculations
- ✅ **Recent Sales** - Latest confirmed orders and sales
- ✅ **Export PDF/CSV** - Export payment data in multiple formats

### 📧 Email System
- ✅ **Client Emails** - Aggregated email data from orders with statistics
- ✅ **Payout Emails** - Email history of payout notifications
- ✅ **Coupon Information** - Track coupon usage and discounts
- ✅ **Email Statistics** - Total orders, confirmed orders, transferred amounts
- ✅ **Export PDF/CSV** - Export email data in multiple formats
- ✅ **Account Confirmation** - Email verification for new accounts
- ✅ **Password Recovery** - Secure password reset via email
- ✅ **Email Change Notifications** - Security alerts for email changes
- ✅ **Rate Limiting** - Throttling for email resend operations
- ✅ **Portuguese Localization** - All email templates in Portuguese

### 📦 Delivery Management (Entregas)
- ✅ **Order Shipping Tracking** - Track order delivery status
- ✅ **Shipping Status** - AGUARDANDO, ENVIADO, RECUSADO, ENTREGUE
- ✅ **Tracking Codes** - Store and manage tracking codes
- ✅ **Delivery History** - Complete delivery history per author
- ✅ **Archived Orders** - Filter and manage delivered orders (ENTREGUE)
- ✅ **Export PDF/CSV** - Export delivery data (all or archived only)
- ✅ **Order Details** - Complete order information with items and addresses

### 💳 Monthly Charges (Cobranças)
- ✅ **Monthly Billing** - Monthly charge management per author
- ✅ **PIX Integration** - PIX payment codes and expiration
- ✅ **Payment Status** - Track pending, paid, and overdue charges
- ✅ **Due Date Tracking** - Monitor payment deadlines
- ✅ **Overdue Calculation** - Automatic overdue days calculation
- ✅ **Open Ticket Check** - Export and listing indicate if a charge has an open support ticket
- ✅ **Payment Confirmed Email** - Author receives email notification when admin confirms payment
- ✅ **Export PDF/CSV** - Export charges data in multiple formats

### 🎫 Ticket System
- ✅ **Ticket Management** - Create and manage support tickets
- ✅ **Ticket Categories** - Categorized tickets (PAGAMENTO, TECNICO, etc.)
- ✅ **Message Threading** - Conversation threads within tickets
- ✅ **Internal Notes** - Private notes for admin use
- ✅ **Status Tracking** - OPEN, IN_PROGRESS, RESOLVED, CLOSED
- ✅ **Auto Ticket Creation** - Overdue charges automatically generate support tickets
- ✅ **Export PDF/CSV** - Export tickets data in multiple formats

### 📄 Export System
- ✅ **PDF Export** - Generate PDF reports for all modules
- ✅ **CSV Export** - Generate CSV files for data analysis
- ✅ **JSON Export** - Default JSON format for API consumption
- ✅ **Multi-Format Support** - Same endpoint supports PDF, CSV, JSON
- ✅ **Author Filtering** - Export data filtered by author_id
- ✅ **Archived Orders Export** - Special endpoint for archived deliveries

### 🏢 Multi-Tenancy Architecture
- ✅ **Author Isolation** - Each author has isolated data access
- ✅ **E-commerce Integration** - Connect to external e-commerce databases
- ✅ **Dynamic Database Connection** - Per-user database credentials
- ✅ **Data Filtering** - Automatic filtering by author_id in all queries
- ✅ **Secure Access** - Users can only access their own author data

### 🗄️ Database Management
- ✅ **Auto Database Creation** - Automatically creates PostgreSQL database if missing
- ✅ **Flyway Migrations** - Version-controlled schema management
- ✅ **Database Monitoring** - Real-time database health and statistics API
- ✅ **Admin Dashboard** - View all administrators and system info
- ✅ **Multi-Database Support** - Support for multiple e-commerce databases

### 🛡️ Security Features
- ✅ **BCrypt Password Hashing** - Industry-standard password security
- ✅ **Token Expiration** - Short-lived access tokens (15 minutes)
- ✅ **Token Rotation** - Refresh tokens rotated on each use
- ✅ **Input Validation** - Comprehensive validation on all endpoints
- ✅ **Secure Defaults** - No hardcoded credentials, all via environment variables
- ✅ **SQL Injection Protection** - Parameterized queries and prepared statements
- ✅ **Data Isolation** - Strict author_id filtering in all queries

## 🛠️ Technology Stack

### Core Framework
- ☕ **Java 24** - Latest Java LTS version
- 🌱 **Spring Boot 3.4.10** - Application framework
- 🔒 **Spring Security 6.4.11** - Security and authentication
- 📦 **Spring Data JPA** - Data persistence layer

### Database & Migrations
- 🐘 **PostgreSQL 18.0** - Relational database
- 🗂️ **Flyway 10.20.1** - Database migration tool
- 💾 **HikariCP 5.1.0** - High-performance connection pooling
- 🔌 **JDBC** - Direct database connections for e-commerce integration

### Security & Authentication
- 🎫 **JWT (jjwt 0.11.5)** - JSON Web Tokens for stateless auth
- 🔐 **BCrypt** - Password hashing with strength 10
- 🛡️ **Spring Security** - Access control and CSRF protection
- 🔑 **Google OAuth2** - Social authentication (optional)

### APIs & Documentation
- 📘 **Swagger/OpenAPI 3.1** - Interactive API documentation
- 🌐 **RESTful API** - Clean, RESTful endpoint design
- 📝 **SpringDoc OpenAPI 2.8.13** - API documentation generator

### Email & Communication
- 📧 **Spring Mail** - SMTP email sending
- 📨 **Jakarta Mail 2.0.4** - Email standards implementation
- 🎨 **HTML Email Templates** - Responsive, branded email designs

### PDF Generation
- 📄 **Apache PDFBox 3.0.3** - PDF document generation
- 📊 **CSV Export** - Native Java CSV generation

### Development Tools
- ⚡ **Lombok 1.18.40** - Boilerplate code reduction
- ✅ **Bean Validation** - Input validation framework
- 🔄 **Spring DevTools** - Hot reload for development

## 🏗️ Architecture

### Hexagonal Architecture (Ports & Adapters)
- **Domain Layer** - Business logic and entities (framework-agnostic)
- **Application Layer** - Use cases and service implementations
- **Adapter Layer** - Controllers, DTOs, repositories, and external integrations
- **Config Layer** - Spring configuration and security setup

### Design Patterns
- **Repository Pattern** - Data access abstraction
- **Service Layer** - Business logic encapsulation
- **DTO Pattern** - Data transfer objects for API
- **Factory Pattern** - Entity creation and mapping
- **Strategy Pattern** - Extensible query strategies
- **Builder Pattern** - Complex object construction

### Multi-Tenancy Pattern
- **Author-Based Isolation** - Data filtered by author_id
- **Dynamic Database Connection** - Per-user e-commerce database credentials
- **Secure Data Access** - Automatic author_id injection in queries

## 📊 Database Schema

### Core Tables
- `users` - User accounts with roles (ADMIN/USER), author_id, and e-commerce credentials
- `account_confirmation_tokens` - Email verification tokens
- `refresh_tokens` - JWT refresh token management
- `password_reset_tokens` - Password recovery tokens
- `email_change_tokens` - Email change verification tokens
- `confirm_resend_throttle` - Rate limiting for email operations
- `monthly_charges` - Monthly billing and charges
- `tickets` - Support ticket management
- `ticket_messages` - Ticket conversation threads
- `order_shipping` - Order delivery tracking and status

### E-commerce Integration
- Connects to external PostgreSQL databases per author
- Queries `orders`, `order_items`, `books`, `payment_payouts` tables
- Filters all queries by `author_id` from `books` table

### Migrations
- **V1** - Users table creation
- **V2** - Token tables creation (5 tables)
- **V4** - Admin user initialization (Java migration with env vars)
- **V5** - Schema fixes and optimizations
- **V6** - Change user_id to String (UUID)
- **V7** - Confirm all existing users
- **V8** - Add author_id to users
- **V9** - Add ecommerce_url to users
- **V10** - Add ecommerce_db_credentials to users
- **V11** - Add profile_photo_url to users
- **V12** - Create monthly_charges table
- **V13** - Create tickets tables
- **V14** - Create order_shipping table
- **V15** - Update shipping status (ENVIO_CONFIRMADO → ENTREGUE)

### Scheduled Jobs
- **BillingJob** (`0 0 8 * * *`) - Generates monthly charges on billing day and sends billing emails
- **OverdueChargeDetectionJob** (`0 0 6 * * *`) - Detects overdue charges, creates support tickets automatically, and emails author + all admins

## 🌐 API Endpoints

### Public Endpoints
- `POST /api/v1/auth/login` - Local authentication
- `POST /api/v1/auth/oauth/google` - Google OAuth login
- `POST /api/v1/auth/refresh-token` - Token refresh
- `POST /api/v1/auth/logout` - Logout and token revocation
- `POST /api/v1/auth/forgot-password` - Request password reset
- `POST /api/v1/auth/password/forgot` - Alternative password reset endpoint
- `POST /api/v1/auth/reset-password` - Reset password with token
- `POST /api/v1/auth/password/reset` - Alternative reset endpoint
- `GET/POST /api/v1/confirm-account?token=...` - Confirm account email
- `GET /api/v1/auth/profile` - Get current user profile (includes author_id)

### Admin Endpoints (Requires ADMIN role)
- `GET /api/admin/dashboard` - Admin dashboard
- `GET /api/admin/users` - List all users
- `POST /api/admin/users` - Create new user account
- `GET /api/admin/users/{identifier}` - Get user by ID or email
- `PUT /api/admin/users/{identifier}` - Update user
- `DELETE /api/admin/users/{identifier}` - Delete user
- `PUT /api/admin/users/{identifier}/confirm-email` - Confirm user email
- `PUT /api/admin/users/{identifier}/unconfirm-email` - Unconfirm user email
- `GET /api/admin/admin-info` - List all administrators
- `GET /api/admin/database/status` - Database health and statistics
- `GET /api/admin/authors/{authorId}/stats` - Get author statistics
- `GET /api/admin/payments/author/{authorId}/summary` - Get author payment summary

### User Endpoints (Requires USER or ADMIN role)
- `GET /api/user/dashboard` - User dashboard
- `GET /api/user/profile` - User profile information
- `PUT /api/user/profile` - Update user profile

### Payment Endpoints
- `GET /api/v1/autor/pagamentos/painel` - Payment dashboard for current author
- `GET /api/v1/payments/export?format=pdf|csv|json` - Export payments

### Email Endpoints
- `GET /api/v1/autor/emails/painel` - Email dashboard (clients and payouts)
- `GET /api/v1/emails/export?format=pdf|csv|json` - Export emails

### Delivery Endpoints (Entregas)
- `GET /api/v1/entregas` - List all deliveries for current author
- `GET /api/v1/entregas/{orderId}` - Get delivery details by order ID
- `PUT /api/v1/entregas/{orderId}/status` - Update delivery status
- `GET /api/v1/entregas/export?format=pdf|csv|json` - Export all deliveries
- `GET /api/v1/entregas/export/arquivados?format=pdf|csv|json` - Export archived deliveries (ENTREGUE)

### Charges Endpoints (Cobranças)
- `GET /api/v1/cobrancas` - List monthly charges for current author
- `PUT /api/v1/admin/cobrancas/{chargeId}/confirmar` - Admin confirms payment (triggers email to author)
- `GET /api/v1/cobrancas/export?format=pdf|csv|json` - Export charges (includes open ticket status)

### Ticket Endpoints
- `GET /api/v1/tickets` - List tickets for current author
- `POST /api/v1/tickets` - Create new ticket
- `GET /api/v1/tickets/{ticketId}` - Get ticket details
- `POST /api/v1/tickets/{ticketId}/messages` - Add message to ticket
- `PUT /api/v1/tickets/{ticketId}/status` - Update ticket status
- `GET /api/v1/tickets/export?format=pdf|csv|json` - Export tickets

## 🔐 Security Model

### Access Control
- **Registration**: Only ADMIN can create users (`POST /api/admin/users`)
- **Google OAuth**: Does not auto-create users (admin must create first)
- **Role Enforcement**: Strict role-based access control (USER/ADMIN)
- **Author Isolation**: Users can only access data from their own author_id
- **Multi-Database**: Each user has their own e-commerce database credentials

### Authentication Flow
1. **Login** → Receive JWT access token (15 min expiry)
2. **Refresh** → Use refresh token to get new access token
3. **Logout** → Revoke all tokens and clear cookies
4. **Token Rotation** → Refresh tokens rotate on each use

### Token Security
- **Access Tokens**: Short-lived (15 minutes), stateless
- **Refresh Tokens**: Long-lived, stored in HttpOnly cookies
- **CSRF Tokens**: Additional protection via custom header
- **Token Revocation**: Server-side tracking of revoked tokens

### Data Isolation
- **Author ID Filtering**: All queries automatically filter by author_id
- **Database Credentials**: Stored per-user, not globally
- **Query Security**: Parameterized queries prevent SQL injection
- **Access Validation**: Backend validates author_id on every request

## 📧 Email System

All emails are sent in **Portuguese** with:
- ✉️ Account confirmation emails
- 🔄 Password reset emails
- 📝 Email change notifications
- 🔔 Security alerts
- 📨 Welcome messages
- 💰 Payout notification emails
- ✅ Payment confirmed emails (sent to author when admin confirms payment)
- ⚠️ Overdue charge emails (sent to author and all admins when a charge becomes overdue)

Features:
- **Rate Limiting** - Prevents email spam
- **HTML Templates** - Responsive, branded designs
- **Token-Based** - Secure, time-limited links
- **Coupon Tracking** - Email includes coupon information when applicable

## 🚀 Performance & Scalability

- **Connection Pooling** - HikariCP for efficient database connections
- **JWT Stateless** - No server-side session storage
- **Database Indexes** - Optimized queries with proper indexing
- **Lazy Loading** - JPA lazy loading for relationships
- **Caching Ready** - Architecture supports caching layers
- **Optimized Queries** - Single-query joins for delivery data
- **Batch Operations** - Efficient bulk data processing

## 📖 API Documentation

Interactive API documentation available at:
- **Swagger UI**: `/swagger` - Visual API explorer
- **OpenAPI Spec**: `/api-docs` - Machine-readable API specification

## 🔍 Monitoring & Health

- **Database Status API** - Real-time database information
- **Admin Info Endpoint** - View all administrators
- **Flyway History** - Track all applied migrations
- **Spring Boot Actuator** - Health checks and metrics
- **Error Logging** - Comprehensive error logging with context

## ⚙️ Configuration

All sensitive configuration via environment variables:
- `DATABASE_URL` - Main PostgreSQL database URL
- `DATABASE_USERNAME` - Main database username
- `DATABASE_PASSWORD` - Main database password
- `MAIL_HOST` - SMTP server host
- `MAIL_PORT` - SMTP server port
- `MAIL_USERNAME` - SMTP username
- `MAIL_PASSWORD` - SMTP password
- `JWT_SECRET` - JWT signing secret key
- `ADMIN_USERNAME` - Admin account username
- `ADMIN_EMAIL` - Admin account email
- `ADMIN_PASSWORD` - Admin account password
- `FRONTEND_BASE_URL` - Frontend application URL
- Google OAuth settings (optional)

**E-commerce Database Credentials**: Stored per-user in `users` table (not environment variables)

## 🎯 Use Cases

### For Administrators
- Create and manage user accounts
- View system statistics and database health
- Monitor system activity
- Access admin dashboard
- View all authors and their statistics
- Manage tickets and support requests

### For Authors (Users)
- Login with email/password or Google
- Reset forgotten passwords
- Change email address
- View personal profile
- Access payment dashboard
- View email statistics (clients and payouts)
- Manage order deliveries
- Track shipping status
- View monthly charges
- Create and manage support tickets
- Export data in PDF/CSV formats

## 🌟 Highlights

- 🎯 **Enterprise Security** - Production-ready authentication system
- 🔒 **Admin-Controlled** - Full control over user creation
- 🚀 **Zero-Config Database** - Automatic database setup
- 📧 **Complete Email System** - Fully localized in Portuguese
- 🎨 **Clean Architecture** - Maintainable, testable codebase
- 📊 **Comprehensive APIs** - Full REST API with documentation
- 🛡️ **Security First** - Multiple layers of protection
- 🏢 **Multi-Tenancy** - Secure data isolation per author
- 📦 **Complete Delivery System** - Full order tracking and management
- 💰 **Financial Management** - Payments, charges, and revenue tracking
- 📄 **Export Capabilities** - PDF/CSV export for all modules
- 🔗 **E-commerce Integration** - Seamless integration with external databases

## 📝 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/dianaglobal/paineldoauthorbackend/
│   │       ├── adapter/
│   │       │   ├── in/
│   │       │   │   ├── dto/          # Data Transfer Objects
│   │       │   │   │   ├── cobrancas/
│   │       │   │   │   ├── emails/
│   │       │   │   │   ├── entregas/
│   │       │   │   │   ├── pagamentos/
│   │       │   │   │   └── tickets/
│   │       │   │   └── web/          # REST Controllers
│   │       │   │       ├── AdminController.java
│   │       │   │       ├── EntregasController.java
│   │       │   │       ├── EmailsAutorController.java
│   │       │   │       ├── PagamentosAutorController.java
│   │       │   │       ├── TicketController.java
│   │       │   │       ├── MonthlyChargeController.java
│   │       │   │       └── *ExportController.java (PDF/CSV)
│   │       │   └── out/
│   │       │       ├── mail/         # Email services
│   │       │       └── persistence/   # JPA repositories
│   │       ├── application/
│   │       │   ├── port/
│   │       │   │   ├── in/           # Use case interfaces
│   │       │   │   └── out/           # Repository interfaces
│   │       │   └── service/          # Service implementations
│   │       │       ├── CurrentAuthorService.java
│   │       │       ├── EmailsAutorService.java
│   │       │       ├── PagamentosAutorService.java
│   │       │       ├── EntregasService.java
│   │       │       ├── ExportService.java
│   │       │       └── ...
│   │       ├── config/               # Spring configuration
│   │       │   ├── SecurityConfig.java
│   │       │   ├── MailConfig.java
│   │       │   └── ...
│   │       ├── domain/              # Domain models
│   │       │   ├── model/
│   │       │   │   ├── User.java
│   │       │   │   ├── Ticket.java
│   │       │   │   ├── OrderShipping.java
│   │       │   │   └── ShippingStatus.java
│   │       │   └── ...
│   │       └── security/            # Security components
│   ├── resources/
│   │   ├── db/migration/           # SQL migrations (V1-V15)
│   │   └── application.yml         # Configuration
│   └── java/db/migration/          # Java migrations (V4)
└── test/
```

## 🔄 Multi-Tenancy Flow

1. **User Login** → Backend retrieves `author_id` from `users` table
2. **Get E-commerce Credentials** → Backend retrieves `ecommerce_db_url`, `ecommerce_db_username`, `ecommerce_db_password`
3. **Connect to E-commerce DB** → Backend connects to author's specific database
4. **Query with Filter** → All queries include `WHERE books.author_id = ?`
5. **Data Isolation** → Each author only sees their own data

## 📄 License

This project is private and proprietary.

## 👤 Author

**Andescore Software**
- 📧 Email: andescoresoftware@gmail.com
- 🌐 Website: https://www.andescoresoftware.com.br

---

**Built with ❤️ using Spring Boot and PostgreSQL**
