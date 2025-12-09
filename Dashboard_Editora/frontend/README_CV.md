# 📚 Dashboard Editora - Author Portal

A comprehensive full-stack web application designed for a publishing house, providing authors with real-time insights into their book sales, royalties, and audience engagement. This project demonstrates a modern, scalable architecture integrating a robust backend with a responsive, high-performance frontend.

## 🚀 Tech Stack

### Frontend (Client-Side)

- **Framework**: **Angular v20** - Utilizing the latest features (Signals, Standalone Components) for reactive state management and optimized build performance.
- **Styling**: **TailwindCSS v3** - Implemented a responsive, mobile-first design system with a custom dark mode aesthetic, glassmorphism effects, and fluid layouts.
- **Visualization**: **Looker Studio Integration** - Embedded secure, parametric iframes for advanced data analytics and reporting.
- **State Management**: RxJS & Angular Signals for handling asynchronous data streams and UI reactivity.
- **Performance**: Optimized rendering with OnPush strategies and lazy loading for feature modules (User/Admin).

### Backend (Server-Side)

- **Language**: **Kotlin** - Concise, safe, and expressive language running on the JVM.
- **Framework**: **Spring Boot** - REST API architecture with robust security and dependency injection.
- **ORM**: **Hibernate / JPA** - Managing complex relationships between Authors, Books, and Sales.
- **Database Migration**: **Flyway** - Version-controlled database schema evolution.
- **Database**: PostgreSQL.

## 🌟 Key Features

### 1. Unified Dashboard & Metrics

- **Responsive Analytics**: A dynamic metrics page (`/user/metrics`) featuring a responsive iframe integration with Looker Studio.
- **Adaptive Layouts**: Utilizing Tailwind's grid and flexbox to ensure data visualizations scale perfectly from mobile devices to large desktop screens (up to 80vh height).
- **Role-Based Views**: Custom logic to differentiate between **Admin** (global view) and **Author** (personal view) dashboards, automatically handling permission scopes and default views.

### 2. User & Profile Management

- **Security**: JWT-based authentication flow with secure local storage management.
- **Profile Customization**: Users can manage their profiles, upload avatars, and view their assigned Author IDs.
- **Admin Tools**: Administrative capabilities to view system-wide metrics without needing specific Author ID bindings (auto-resolves to Global View).

### 3. Modern UI/UX

- **Glassmorphism**: Use of translucent backgrounds (`bg-slate-900/60`, `backdrop-blur`) to create depth and hierarchy.
- **Micro-interactions**: Subtle hover states, transitions, and loading skeletons to enhance user experience.
- **Accessibility**: Semantic HTML structure and contrast-aware color palettes.

## 🔧 Architecture Highlights

- **Component-Based Architecture**: Modular components (e.g., `AuthorMetricsDashboard`) designed for reusability and isolation of concerns.
- **Service Layer Pattern**: Centralized services (`AuthService`, `MenuService`) handling business logic and API communication, keeping components lean.
- **Defensive Programming**: Robust error handling for missing data (e.g., handling `null` Author IDs for Admins gracefully) to prevent runtime crashes.

---

_Project developed by [Your Name/AndesCore Software]_
