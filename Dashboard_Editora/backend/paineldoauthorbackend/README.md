# 📊 Painel do Autor Backend - Plataforma de Gestão de Autores e Métricas

Plataforma completa de gestão de autores, métricas, pagamentos e emails com autenticação e controle de acesso, construída em Spring Boot e PostgreSQL.

## 🎯 O Que Faz

Uma plataforma completa de **gestão de autores** que fornece:
- 📊 **Dashboard de métricas** e análises
- 💰 **Gestão de pagamentos e repasses** por autor
- 📧 **Sistema de emails** e notificações
- 🎫 **Sistema de tickets** para suporte
- 👥 **Gestão de usuários** com controle administrativo
- 🔐 **Autenticação e autorização** robusta com JWT

Apenas administradores podem criar contas de usuários, garantindo controle total sobre o acesso ao sistema. A plataforma suporta múltiplos métodos de autenticação, controle de acesso baseado em roles e um sistema completo de emails em português.

## ✨ Key Features

### 🔑 Authentication & Authorization
- ✅ **Local Authentication** - Email/password login with JWT tokens
- ✅ **Google OAuth2 Integration** - Social login (requires pre-created accounts)
- ✅ **JWT Token Management** - Stateless authentication with refresh token rotation
- ✅ **Role-Based Access Control** - ADMIN and USER roles with granular permissions
- ✅ **CSRF Protection** - Built-in CSRF token validation
- ✅ **Session Management** - Server-side refresh tokens with revocation

### 👥 User Management
- ✅ **Admin-Only Registration** - Strict control: only admins create accounts
- ✅ **User Creation API** - Admins can create users with custom roles
- ✅ **Password Management** - Secure password hashing, reset, and change flows
- ✅ **Email Verification** - Account confirmation with rate limiting
- ✅ **Email Change** - Secure email change with dual confirmation

### 📧 Email System
- ✅ **Account Confirmation** - Email verification for new accounts
- ✅ **Password Recovery** - Secure password reset via email
- ✅ **Email Change Notifications** - Security alerts for email changes
- ✅ **Rate Limiting** - Throttling for email resend operations
- ✅ **Portuguese Localization** - All email templates in Portuguese

### 🗄️ Database Management
- ✅ **Auto Database Creation** - Automatically creates PostgreSQL database if missing
- ✅ **Flyway Migrations** - Version-controlled schema management
- ✅ **Database Monitoring** - Real-time database health and statistics API
- ✅ **Admin Dashboard** - View all administrators and system info

### 🛡️ Security Features
- ✅ **BCrypt Password Hashing** - Industry-standard password security
- ✅ **Token Expiration** - Short-lived access tokens (15 minutes)
- ✅ **Token Rotation** - Refresh tokens rotated on each use
- ✅ **Input Validation** - Comprehensive validation on all endpoints
- ✅ **Secure Defaults** - No hardcoded credentials, all via environment variables

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

### Development Tools
- ⚡ **Lombok 1.18.40** - Boilerplate code reduction
- ✅ **Bean Validation** - Input validation framework
- 🔄 **Spring DevTools** - Hot reload for development

## 🏗️ Architecture

### Hexagonal Architecture (Ports & Adapters)
- **Domain Layer** - Business logic and entities (framework-agnostic)
- **Application Layer** - Use cases and service implementations
- **Adapter Layer** - Controllers, repositories, and external integrations
- **Config Layer** - Spring configuration and security setup

### Design Patterns
- **Repository Pattern** - Data access abstraction
- **Service Layer** - Business logic encapsulation
- **DTO Pattern** - Data transfer objects for API
- **Factory Pattern** - Entity creation and mapping

## 📊 Database Schema

### Core Tables
- `users` - User accounts with roles (ADMIN/USER) and authentication info
- `account_confirmation_tokens` - Email verification tokens
- `refresh_tokens` - JWT refresh token management
- `password_reset_tokens` - Password recovery tokens
- `email_change_tokens` - Email change verification tokens
- `confirm_resend_throttle` - Rate limiting for email operations
- `flyway_schema_history` - Migration tracking

### Migrations
- **V1** - Users table creation
- **V2** - Token tables creation (5 tables)
- **V4** - Admin user initialization (Java migration with env vars)
- **V5** - Schema fixes and optimizations

## 🌐 API Endpoints

### Public Endpoints
- `POST /api/v1/auth/login` - Local authentication
- `POST /api/v1/auth/oauth/google` - Google OAuth login
- `POST /api/v1/auth/refresh-token` - Token refresh
- `POST /api/v1/auth/logout` - Logout and token revocation
- `POST /api/v1/auth/forgot-password` - Request password reset
- `POST /api/v1/auth/reset-password` - Reset password with token
- `GET/POST /api/v1/confirm-account?token=...` - Confirm account email

### Admin Endpoints (Requires ADMIN role)
- `GET /api/admin/dashboard` - Admin dashboard
- `POST /api/admin/users` - Create new user account
- `GET /api/admin/admin-info` - List all administrators
- `GET /api/admin/database/status` - Database health and statistics

### User Endpoints (Requires USER or ADMIN role)
- `GET /api/user/dashboard` - User dashboard
- `GET /api/user/profile` - User profile information

## 🔐 Security Model

### Access Control
- **Registration**: Only ADMIN can create users (`POST /api/admin/users`)
- **Google OAuth**: Does not auto-create users (admin must create first)
- **Role Enforcement**: Strict role-based access control (USER/ADMIN)

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

## 📧 Email System

All emails are sent in **Portuguese** with:
- ✉️ Account confirmation emails
- 🔄 Password reset emails
- 📝 Email change notifications
- 🔔 Security alerts
- 📨 Welcome messages

Features:
- **Rate Limiting** - Prevents email spam
- **HTML Templates** - Responsive, branded designs
- **Token-Based** - Secure, time-limited links

## 🚀 Performance & Scalability

- **Connection Pooling** - HikariCP for efficient database connections
- **JWT Stateless** - No server-side session storage
- **Database Indexes** - Optimized queries with proper indexing
- **Lazy Loading** - JPA lazy loading for relationships
- **Caching Ready** - Architecture supports caching layers

## 📖 API Documentation

Interactive API documentation available at:
- **Swagger UI**: `/swagger` - Visual API explorer
- **OpenAPI Spec**: `/api-docs` - Machine-readable API specification

## 🔍 Monitoring & Health

- **Database Status API** - Real-time database information
- **Admin Info Endpoint** - View all administrators
- **Flyway History** - Track all applied migrations
- **Spring Boot Actuator** - Health checks and metrics

## ⚙️ Configuration

All sensitive configuration via environment variables:
- Database credentials
- Email SMTP settings
- JWT secret key
- Admin credentials (never hardcoded)
- Google OAuth settings (optional)

## 🎯 Use Cases

### For Administrators
- Create and manage user accounts
- View system statistics and database health
- Monitor system activity
- Access admin dashboard

### For Users
- Login with email/password or Google
- Reset forgotten passwords
- Change email address
- View personal profile
- Access user dashboard

## 🌟 Highlights

- 🎯 **Enterprise Security** - Production-ready authentication system
- 🔒 **Admin-Controlled** - Full control over user creation
- 🚀 **Zero-Config Database** - Automatic database setup
- 📧 **Complete Email System** - Fully localized in Portuguese
- 🎨 **Clean Architecture** - Maintainable, testable codebase
- 📊 **Comprehensive APIs** - Full REST API with documentation
- 🛡️ **Security First** - Multiple layers of protection

## 📝 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/dianaglobal/paineldoauthorbackend/
│   │       ├── adapter/          # Controllers, DTOs, Repositories
│   │       ├── application/      # Use cases and services
│   │       ├── config/          # Spring configuration
│   │       ├── domain/          # Domain models
│   │       └── security/        # Security components
│   ├── resources/
│   │   ├── db/migration/       # SQL migrations
│   │   └── application.yml     # Configuration
│   └── java/db/migration/      # Java migrations (V4)
└── test/
```

## 📄 License

This project is private and proprietary.

## 👤 Author

**Andescore Software**
- 📧 Email: andescoresoftware@gmail.com
- 🌐 Website: https://www.andescoresoftware.com.br

---

**Built with ❤️ using Spring Boot and PostgreSQL**
