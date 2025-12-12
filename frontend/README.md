# Author Dashboard - Publishing House

Web dashboard for managing authors, orders, payments, deliveries, and metrics developed in Angular.

## 📋 About the Project

Complete management system for publishing house authors, allowing visualization of metrics, order management, delivery control, payment tracking, and communication with customers via email.

## 🚀 Technologies

- **Angular** 20.3.0
- **TypeScript** 5.9.2
- **Tailwind CSS** 3.4.17
- **RxJS** 7.8.0
- **ECharts** 6.0.0 (charts and visualizations)
- **Angular Signals** (reactive state management)

## 📁 Project Structure

```
src/
├── app/
│   ├── core/                    # Shared core functionalities
│   │   ├── components/          # Reusable components
│   │   │   ├── author-metrics-dashboard/  # Looker Studio Dashboard
│   │   │   └── export-buttons/            # Export buttons
│   │   ├── guards/              # Route guards (auth, role)
│   │   ├── interceptors/       # HTTP interceptors (auth)
│   │   ├── models/             # TypeScript interfaces and types
│   │   ├── services/           # Shared services
│   │   └── utils/              # Utilities
│   ├── features/               # Feature modules
│   │   ├── admin/              # Administrative functionalities
│   │   ├── auth/               # Authentication and authorization
│   │   ├── dashboard/           # Dashboard with charts
│   │   └── user/               # User functionalities
│   └── layout/                 # Layout components
│       ├── components/         # Sidebar, Topbar, Footer
│       └── layouts/            # Layouts (admin, user, public)
└── environments/               # Environment configurations
```

## ✨ Main Features

### 🔐 Authentication and Authorization

- Login with JWT
- Password recovery
- Account confirmation via email
- Route guards by role (USER, ADMIN)
- HTTP Interceptor to automatically add tokens

### 👤 User Management (Admin)

- User creation and editing
- Role and permission management
- Administrator list visualization
- Configuration of `author_id` and Looker Studio URLs

### 💰 Payments

- Payment summary visualization
- Sales funnel (Total, Confirmed, Cancelled)
- Recent orders list with real values (after fees)
- Export to PDF/CSV

### 📧 Emails

- Customer email visualization
- Transfer emails (PIX/Card)
- Applied coupon information
- Order statistics per customer
- Export to PDF/CSV

### 📊 Metrics

- Integrated dashboard with Looker Studio
- Metrics visualization per author
- Multi-tenant support (each author sees only their own metrics)
- Author selection for administrators

### 🚚 Deliveries

- Active deliveries list
- Shipping status (WAITING, SENT, REFUSED, DELIVERED)
- Tracking code update
- Status update modal
- Filter between active and archived deliveries

### 📦 Archived Orders

- Visualization of orders with DELIVERED status
- Same interface as deliveries, filtered for archived items
- Specific export for archived orders

### 🎫 Tickets

- Support ticket creation
- Open and closed tickets visualization
- Ticket details
- Export to PDF/CSV

### 💳 Monthly Charges

- Monthly charges visualization
- PIX codes for payment
- Payment status (PENDING, PAID, OVERDUE, CANCELLED)
- Charge history
- Export to PDF/CSV

### 📈 Dashboard

- Main KPIs (Revenue, Orders, Cancelled, Delivery Status)
- Brazil map with sales distribution
- Sales funnel
- Payment method metrics
- Best-selling products

## 🔧 Configuration

### Prerequisites

- Node.js 18+
- npm or yarn

### Installation

1. Clone the repository:

```bash
git clone <repository-url>
cd frontend
```

2. Install dependencies:

```bash
npm install
```

3. Configure environment variables:

```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com',
  backendPort: 8000,
};
```

4. Start the development server:

```bash
npm start
# or
ng serve
```

5. Access `http://localhost:4200`

## 🏗️ Architecture

### Multi-Tenancy

The system implements multi-tenant isolation based on `author_id`:

- Each author sees only their own data
- The `author_id` is automatically extracted from the JWT token
- It is not necessary to pass `author_id` as a parameter in requests
- Backend guarantees complete data isolation

### Design Patterns

- **Open/Closed Principle (OCP)**: Extensible components without modification
- **Single Responsibility**: Each service/component has a single responsibility
- **Dependency Injection**: Extensive use of Angular's `inject()`
- **Signals**: Reactive state with Angular Signals
- **Strategy Pattern**: Mapping Looker Studio URLs by `authorId`

### Main Services

#### AuthService

- Authentication management
- JWT token storage
- User profile management
- Login, logout, password recovery methods

#### EmailService

- Customer and transfer email fetching
- Coupon information aggregation
- Complete email panel

#### PaymentService

- Payment summary
- Sales funnel
- Recent orders

#### DeliveryService (EntregaService)

- Deliveries list
- Shipping status update
- Caching with `shareReplay`

#### ExportService

- Export to PDF/CSV
- Support for multiple modules (emails, deliveries, charges, tickets, metrics)
- Automatic filename generation

#### MonthlyChargeService

- Monthly charge management
- PIX codes
- Payment status

#### TicketService

- Ticket creation and listing
- Ticket details

## 🎨 Design System

### Theme

- Dark mode design with gradients
- Main colors: Sky Blue (#38bdf8, #2563eb)
- Blur and glassmorphism effects
- Responsive (mobile-first)

### Reusable Components

- **ExportButtonsComponent**: Export buttons with dropdown (PDF/CSV)
- **AuthorMetricsDashboardComponent**: Looker Studio embed per author
- Standardized Cards, Modals, Tables

## 📤 Export

The system supports export in multiple formats:

- **PDF**: Formatted reports
- **CSV**: Tabular data
- **JSON**: Structured data (some endpoints)

Modules with export:

- Emails
- Deliveries (active and archived)
- Charges
- Tickets
- Metrics

## 🔒 Security

- JWT Authentication
- HTTP Interceptor for automatically adding tokens
- Route guards to protect authenticated routes
- Role validation (USER, ADMIN)
- URL sanitization for iframes (DomSanitizer)
- Multi-tenant isolation guaranteed by the backend

## 📱 Responsiveness

- Mobile-first design
- Tailwind breakpoints (sm, md, lg, xl)
- Responsive tables (desktop) / Cards (mobile)
- Adaptive modals
- Optimized mobile navigation

## 🚀 Production Build

```bash
ng build --configuration=production
```

Compiled files will be in `dist/dashboard-author-editora-frontend/browser/`

## 🧪 Tests

```bash
ng test
```

## 📝 Available Scripts

- `npm start` / `ng serve`: Development server
- `npm run build`: Production build
- `npm test`: Run tests
- `ng generate component`: Generate new component

## 🔗 Backend API

**Base URL**: `https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com`

### Main Endpoints

- `/api/v1/auth/*` - Authentication
- `/api/v1/author/*` - Author endpoints
- `/api/v1/admin/*` - Administrative endpoints
- `/api/v1/entregas/*` - Deliveries
- `/api/v1/cobrancas/*` - Charges
- `/api/v1/tickets/*` - Tickets
- `/api/v1/emails/*` - Emails
- `/api/v1/*/export` - Exports

## 📚 Additional Resources

- [Angular Documentation](https://angular.dev)
- [Tailwind CSS](https://tailwindcss.com)
- [ECharts](https://echarts.apache.org)
- [RxJS](https://rxjs.dev)

## 👥 Contribution

This is a private project for the publishing house. For contributions, please contact the development team.

## 📄 License

Owner - All rights reserved

---

**Developed with ❤️ for the Publishing House**
