# JayasriMart — Multi-Seller E-Commerce Web Platform

> **Anna University R2025 — Semester 3 Academic Project**  
> Enterprise-grade Multi-Vendor E-Commerce Web Application developed using pure **Java Servlets (Jakarta EE 9 / Servlet 5)**, **JDBC**, **HikariCP**, and **H2 Database**.

---

## 📑 Table of Contents
1. [Project Overview](#project-overview)
2. [Tech Stack](#tech-stack)
3. [Architecture & Design Patterns](#architecture--design-patterns)
4. [Role-Based Access Control (RBAC)](#role-based-access-control-rbac)
5. [Database Schema & Persistence](#database-schema--persistence)
6. [Seed Accounts & Credentials](#seed-accounts--credentials)
7. [AI Shopping Assistant (JayaBot)](#ai-shopping-assistant-jayabot)
8. [Installation & Running Locally](#installation--running-locally)
9. [Automated Testing & Code Quality](#automated-testing--code-quality)
10. [REST API Documentation](#rest-api-documentation)
11. [Security Highlights](#security-highlights)

---

## 🌟 Project Overview
**JayasriMart** is a full-featured, multi-tenant e-commerce marketplace catering to three distinct personas:
- **Buyers:** Browse products with multi-attribute filtering, live instant search, add to wishlist, manage persistent carts, complete simulated multi-channel checkout (UPI, Card, COD), track order lifecycles, and submit verified reviews.
- **Sellers:** Dedicated Seller Hub dashboard with revenue metrics, real-time catalog & inventory management, low-stock alerts, and order fulfillment status transitions.
- **Admins:** Global governance console with platform revenue analytics, user role administration, product catalog moderation, and platform-wide order oversight.
- **AI Shopping Assistant (JayaBot):** Grounded product recommendations, FAQ answering, and store policy lookup powered by Google Gemini API with offline mock fallback.

---

## 🛠 Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Language & Platform** | Java 17 LTS, Apache Maven |
| **Web Framework** | Jakarta Servlet 5.0 (`jakarta.servlet.*`), Jakarta Standard Taglib (JSTL) |
| **Server Engine** | Eclipse Jetty 11 (via `jetty-maven-plugin`) & Apache Tomcat 10 compatible |
| **Database & Pooling**| H2 Database (File mode for persistence + In-Memory for testing), HikariCP 5.1.0 |
| **View / Frontend** | JSP 3.0, Responsive CSS3 (CSS Variables, Mobile-First), Vanilla JavaScript (ES6+ Fetch) |
| **Security & Cryptography**| jBCrypt (Password Hashing with Salt), RBAC Filter Chains, Request Tracing MDC |
| **Serialization & AI** | Google Gson 2.10.1, Google Gemini REST API Client with rule-based fallback |
| **Logging & Quality** | SLF4J 2.0.12 + Logback Classic, Checkstyle, SpotBugs |
| **Testing Framework** | JUnit 5 (Jupiter), Mockito 5 (Subclass & MockMaker), H2 In-Memory DB fixtures |

---

## 🏛 Architecture & Design Patterns

The codebase strictly adheres to clean multi-tier enterprise Java architecture:

```
com.jayasrimart
├── config/         # Application bootstrap & HikariCP pool configuration
├── controller/     # Jakarta WebServlets handling HTTP requests & routing
├── dao/            # Data Access Object pattern with PreparedStatement JDBC
│   └── impl/       # Robust SQL implementations with connection closing
├── dto/            # Immutable Data Transfer Objects & Response Envelopes
├── exception/      # Domain & Validation Exception hierarchies
├── filter/         # RBAC, Authentication & MDC Request ID Filters
├── listener/       # AppContextListener managing schema migrations & pool lifecycle
├── model/          # Core Domain Entities (User, Product, Order, Review, Wishlist)
├── service/        # Business logic abstraction layer
│   ├── ai/         # Strategy pattern for AI Providers (Gemini & Mock)
│   ├── impl/       # Service implementations with transaction boundaries
│   └── payment/    # Strategy pattern for Payment simulations (Card, UPI, COD)
└── util/           # Security, DB, JSON, Validation, and Currency helpers
```

### Key Design Patterns Implemented:
1. **Model-View-Controller (MVC):** Servlets process actions, Services apply business rules, JSPs render responsive views.
2. **Data Access Object (DAO) & Factory Pattern:** Decouples database operations via `DaoFactory` and interface contracts.
3. **Strategy Pattern:**
   - `PaymentStrategy`: Decouples payment processors (`CardPaymentStrategy`, `UpiPaymentStrategy`, `CodPaymentStrategy`).
   - `AiServiceProvider`: Decouples AI backends (`GeminiAiServiceProvider`, `MockAiServiceProvider`).
4. **Builder Pattern:** Used for clean, immutable construction of `UserResponseDTO` and `ProductSearchCriteria`.
5. **Filter Interceptor Pattern:** `AuthFilter` and `RoleFilter` enforce RBAC before reaching servlets.

---

## 🔐 Role-Based Access Control (RBAC)

The platform enforces strict role separation across routes:

| Route Path | Allowed Roles | Description |
| :--- | :--- | :--- |
| `/` , `/products/**` , `/cart` | Anonymous, BUYER, SELLER, ADMIN | Public catalog, search, and cart viewing |
| `/checkout/**` , `/orders/**` | BUYER, ADMIN | Order placement, order tracking, order history |
| `/wishlist/**` , `/reviews/**` | BUYER, ADMIN | Wishlist management and verified product reviews |
| `/seller/**` | SELLER, ADMIN | Seller Portal: Dashboard, products CRUD, fulfillment |
| `/admin/**` | ADMIN | Admin Portal: Metrics, user roles, catalog moderation |

---

## 🗄 Database Schema & Persistence

Data is persisted using **H2 Database in File Mode**:
- **Connection URL:** `jdbc:h2:file:./data/jayasrimart;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1`
- **Auto-Initialization:** `schema.sql` automatically generates tables, foreign keys, check constraints, and indexes on application launch.
- **Auto-Seeding:** Default administrative, seller, and buyer accounts, categories, and initial catalog products are provisioned on first run.

### Core Tables:
- `users`: User identity, hashed passwords, roles (`BUYER`, `SELLER`, `ADMIN`), active status.
- `categories`: Product categories with SEO slugs.
- `products`: Multi-seller catalog with stock count, price, images, and average rating cache.
- `cart_items`: User-specific shopping cart rows with unit prices.
- `orders` & `order_items`: Order records, shipping addresses, payment info, and item snapshots.
- `reviews`: Star ratings (1-5), comments, verified purchase flags, and product rating triggers.
- `wishlist`: Customer saved items with instant toggle.

---

## 👥 Seed Accounts & Credentials

The system initializes with the following default credentials:

| Role | Email | Password | Access Area |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@jayasrimart.com` | `Admin@123` | Full Platform Access (`/admin/*`) |
| **Seller** | `seller@jayasrimart.com` | `Seller@123` | Seller Portal (`/seller/*`) |
| **Buyer** | `buyer@jayasrimart.com` | `Buyer@123` | Marketplace & Wishlist (`/buyer/*`, `/cart`) |

---

## 🤖 AI Shopping Assistant (JayaBot)

JayasriMart features an integrated AI Assistant:
- **Product Grounding:** Automatically extracts search keywords from customer inquiries and injects relevant catalog items into the AI prompt.
- **Provider Fallback:** If `GEMINI_API_KEY` is not provided, the assistant uses `MockAiServiceProvider` to seamlessly handle shipping policies, payment inquiries, return FAQs, seller registration guides, and catalog recommendations.
- **Interactive UI:** Floating widget with quick prompt chips, real-time chat history, and clickable recommendation cards with 1-click cart addition.

---

## 🚀 Installation & Running Locally

### Prerequisites:
- **Java Development Kit (JDK) 17** or higher
- **Apache Maven 3.8+**

### 1. Clone & Build
```bash
git clone https://github.com/jayasrisenthil15-cloud/JayasriMart.git
cd JayasriMart
mvn clean compile
```

### 2. Run with Embedded Jetty
```bash
mvn jetty:run
```
The application will start at **`http://localhost:8080`**.

### 3. Build Production WAR
```bash
mvn clean package
```
Generates `target/jayasrimart.war` ready for deployment to any Jakarta EE 9 / Tomcat 10+ server.

---

## 🧪 Automated Testing & Code Quality

The project features comprehensive unit, DAO, service, and security filter tests using JUnit 5 and Mockito.

### Run All Tests:
```bash
mvn test
```
*Current test suite: **112 passing unit & integration tests** across DAO, Service, Filter, Controller, and AI layers.*

### Run Checkstyle Audit:
```bash
mvn checkstyle:check
```
*Passes with **0 Checkstyle violations**.*

### Run SpotBugs Static Analysis:
```bash
mvn spotbugs:check
```

---

## 📡 REST API Documentation

| Method | Endpoint | Description | Response Format |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/health` | System & DB connectivity health check | JSON (`ApiResponse<HealthStatus>`) |
| `POST`| `/api/v1/chat` | AI Shopping Assistant chat completion | JSON (`ApiResponse<ChatResponseDTO>`) |
| `POST`| `/wishlist/toggle` | Add/Remove product from wishlist | JSON (`ApiResponse<WishlistToggleDTO>`) |
| `POST`| `/cart/add` | Add product to shopping cart | JSON / Redirect |
| `POST`| `/cart/update` | Update item quantity in cart | JSON / Redirect |
| `POST`| `/cart/delete` | Remove item from cart | JSON / Redirect |

---

## 🔒 Security Highlights

1. **Password Hashing:** Uses `jBCrypt` with adaptive salting. Passwords are never stored or logged in plain text.
2. **SQL Injection Defense:** 100% of database queries use parameterized `PreparedStatement`.
3. **RBAC Interceptor:** `RoleFilter` prevents privilege escalation across admin, seller, and buyer endpoints.
4. **Data Transfer Object Safety:** `UserResponseDTO` excludes sensitive password fields by design.
5. **Input Validation:** Centralized `ValidationUtil` rigorously validates email formats, 10-digit Indian phone numbers, 6-digit PIN codes, and price bounds before database execution.
6. **MDC Request Tracing:** Unique request IDs are assigned to every incoming HTTP request and tracked across SLF4J logs.

---

## 🎓 Academic Project Information
- **Institution:** Anna University
- **Regulation:** R2025
- **Semester:** 3
- **Course:** Full-Stack Web Development (Java Servlets + JDBC Architecture)
- **Project Name:** JayasriMart
- **Author:** Jayasri Senthil
