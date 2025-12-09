# 📊 Painel do Autor & Dashboard Editora

Welcome to the **Painel do Autor** project. This is a full-stack solution designed for managing authors, metrics, payments, and deliveries for a publishing house.

## 🚀 Overview

The system allows authors to view their sales metrics, track payments, request payouts, and manage deliveries. It also provides a robust administration panel for the publisher to manage users, integrate with e-commerce databases, and oversee the entire operation.

The project is divided into two main applications:

- **Frontend**: A responsive Single Page Application (SPA) built with Angular.
- **Backend**: A robust REST API built with Java and Spring Boot.

---

## 🛠️ Technology Stack

### 🖥️ Frontend (Web App)

Located in `/frontend`

| Technology      | Version | Purpose                                  |
| --------------- | ------- | ---------------------------------------- |
| **Angular**     | v20+    | Core frontend framework                  |
| **TailwindCSS** | v3.4    | Utility-first CSS framework for styling  |
| **ECharts**     | v6.0    | Advanced data visualization and charts   |
| **RxJS**        | v7.8    | Reactive extensions for state management |
| **TypeScript**  | v5.9    | Type-safe JavaScript                     |

### ⚙️ Backend (API)

Located in `/backend`

| Technology            | Version | Purpose                                |
| --------------------- | ------- | -------------------------------------- |
| **Java**              | v24     | Programming language (Latest LTS)      |
| **Spring Boot**       | v3.4.11 | Web framework and dependency injection |
| **Spring Security**   | v6.4    | Auth/Authz (JWT, OAuth2, CSRF)         |
| **PostgreSQL**        | v18.0   | Primary relational database            |
| **Flyway**            | v10.20  | Database migration version control     |
| **Spring Data JPA**   | -       | ORM / Hibernate implementation         |
| **JWT**               | v0.11.5 | Stateless authentication tokens        |
| **OpenAPI / Swagger** | v3.1    | API documentation                      |
| **Apache PDFBox**     | v3.0    | PDF generation for reports             |

### 📊 Data & Analytics

| Technology           | Purpose                                          |
| -------------------- | ------------------------------------------------ |
| **BigQuery**         | Serverless Data Warehouse for scalable analytics |
| **Looker**           | Business Intelligence and data exploration       |
| **Google Analytics** | User tracking and web analytics                  |

---

## ✨ Key Features

- **Multi-Tenancy**: Data isolation per author.
- **Authentication**: Secure login via Email/Password (JWT) or Google OAuth2.
- **Dashboards**: Interactive charts for sales, revenue, and visitor metrics.
- **Financials**: Detailed tracking of monthly charges, payouts, and revenue share.
- **Logistics**: Order tracking system with status updates (Pending, Shipped, Delivered).
- **Support**: Internal ticket system for author support.
- **Data Warehouse**: Dedicated data warehouse integrating BigQuery for all publisher writers.
- **Exports**: Data export capabilities to PDF, CSV, and JSON.

---

## 🏁 Getting Started

### Prerequisites

- Node.js (v18 or higher)
- Java JDK 24
- Maven
- PostgreSQL running locally or via Docker

### 1. Backend Setup

```bash
cd backend/paineldoauthorbackend

# Install dependencies and build
mvn clean install

# Run the application
mvn spring-boot:run
```

- The API will start at `https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com`
- Swagger UI: `https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com/swagger-ui/index.html

### 2. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start the development server
npm start
```

- The application will start at `https:www.paineldavia.com.br`

---

## 🏗️ Architecture

The backend follows a **Hexagonal Architecture** (Ports & Adapters) to ensure business logic is decoupled from external concerns.

- **Domain**: Core business rules and entities.
- **Application**: Service layer implementing use cases.
- **Adapters**: Web controllers, persistence layers, and external APIs.

## 📄 License & Author

**Author**: AndesCore Software & Diana Global  
**License**: Private & Proprietary
