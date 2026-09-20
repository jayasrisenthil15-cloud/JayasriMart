# ANNA UNIVERSITY — REGULATION R2025
## DEPARTMENT OF COMPUTER SCIENCE AND ENGINEERING
### SEMESTER III — PROJECT WORK REPORT

---

# JAYASRIMART
### AN ENTERPRISE MULTI-SELLER E-COMMERCE PLATFORM USING JAVA SERVLETS, JDBC & HIKARICP ARCHITECTURE

**Project Title:** JayasriMart Multi-Seller E-Commerce Web Application  
**Curriculum Regulation:** Anna University R2025  
**Course:** Advanced Java Web Development (Java Servlets, JDBC, MVC Architecture)  
**Author / Developer:** Jayasri Senthil  
**Technology Stack:** Java 17 LTS, Jakarta EE 9 / Servlet 5.0, JSP/JSTL, HikariCP, H2 Database, Eclipse Jetty 11, Google Gemini AI  

---

## 1. ABSTRACT

Electronic commerce platforms have transformed modern trade by connecting distributed sellers with retail buyers. The objective of the **JayasriMart** project is to design, engineer, and validate an enterprise-grade multi-tenant e-commerce system built entirely on fundamental Java enterprise specifications (**Jakarta Servlet 5.0**, **Java Database Connectivity (JDBC)**, and **HikariCP** connection pooling) without relying on heavy abstracted application frameworks like Spring Boot.

The platform provides role-segregated experiences for three personas: **Buyers**, **Sellers**, and **System Administrators**, backed by a resilient security layer featuring **BCrypt password hashing**, **Role-Based Access Control (RBAC)** filters, and **Mapped Diagnostic Context (MDC)** request tracing. To enhance buyer engagement, an **AI Shopping Assistant (JayaBot)** powered by the **Google Gemini REST API** is integrated with intelligent fallback to local grounded rule engines. The system was validated against a comprehensive test suite of 112 unit and integration tests, achieving zero Checkstyle violations and full SpotBugs compliance.

---

## 2. SYSTEM REQUIREMENTS SPECIFICATION

### 2.1 Hardware Requirements
- **Processor:** Intel Core i3 / AMD Ryzen 3 or higher (x64 architecture).
- **RAM:** Minimum 4 GB (8 GB recommended for concurrent testing and in-memory test databases).
- **Storage:** Minimum 500 MB free hard disk space for H2 file mode database storage and build artifacts.

### 2.2 Software & Development Environment
- **Operating System:** Windows 10/11, macOS, or Linux.
- **Java Platform:** Java Development Kit (JDK) 17 LTS.
- **Build Tool:** Apache Maven 3.8+.
- **Servlet Specification:** Jakarta Servlet 5.0 / Jakarta Server Pages (JSP) 3.0.
- **Embedded Web Server:** Eclipse Jetty 11.0.18 (configured via `jetty-maven-plugin`).
- **Database Engine:** H2 Database 2.2.224 (File mode `jdbc:h2:file:./data/jayasrimart;AUTO_SERVER=TRUE` for runtime persistence; In-Memory mode `jdbc:h2:mem:*` for automated tests).
- **Connection Pool:** HikariCP 5.1.0.
- **Testing Libraries:** JUnit 5.10.2, Mockito 5.11.0.

---

## 3. SYSTEM ARCHITECTURE & DESIGN

### 3.1 Tiered Architectural Overview

JayasriMart employs a strict **Three-Tier MVC Architecture** combined with the **Data Access Object (DAO) Pattern** and **Strategy Pattern**:

```
+-------------------------------------------------------------------------+
|                        PRESENTATION TIER (JSP / CSS / JS)               |
|   Buyer Views  |  Seller Portal  |  Admin Console  |  AI JayaBot Widget  |
+-------------------------------------------------------------------------+
                                    |
                                    v [HTTP Request / Response]
+-------------------------------------------------------------------------+
|                        CONTROLLER & SECURITY FILTER TIER                |
|  RequestIdFilter -> AuthFilter -> RoleFilter -> Jakarta WebServlets     |
+-------------------------------------------------------------------------+
                                    |
                                    v [DTOs & Request Envelopes]
+-------------------------------------------------------------------------+
|                         BUSINESS LOGIC SERVICE TIER                     |
|  AuthService | ProductService | CartService | OrderService              |
|  ReviewService | SellerService | AdminService | WishlistService         |
|  PaymentStrategy (UPI, Card, COD) | AiServiceProvider (Gemini, Mock)    |
+-------------------------------------------------------------------------+
                                    |
                                    v [Domain Entities / Models]
+-------------------------------------------------------------------------+
|                          DATA ACCESS LAYER (DAO)                        |
|  UserDAO | ProductDAO | CartDAO | OrderDAO | ReviewDAO | WishlistDAO     |
+-------------------------------------------------------------------------+
                                    |
                                    v [PreparedStatement JDBC / SQL]
+-------------------------------------------------------------------------+
|                       PERSISTENCE & POOLING TIER                        |
|             HikariCP 5.1.0 Connection Pool -> H2 Database Engine        |
+-------------------------------------------------------------------------+
```

### 3.2 Request Lifecycle & Filter Interception Sequence

```
Client Browser
      │
      ▼  [HTTP Request with Session Cookie]
┌─────────────────────────────────────────────────────────────────┐
│ RequestIdFilter: Generates UUID, sets X-Request-ID, attaches MDC│
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ AuthFilter: Checks session currentUser; redirects if unauthed   │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ RoleFilter: Inspects URL prefix (/admin, /seller, /buyer)       │
│             Verifies user role authorization; 403 if forbidden  │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ WebServlet: Parses parameters/JSON, invokes Service Layer       │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ Service Layer: Executes business rules, transactions & validation│
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ DAO Layer: Executes SQL statements via HikariCP Connections     │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. DATABASE DESIGN & ENTITY-RELATIONSHIP MODEL

### 4.1 Entity Relationship Diagram

```
 +--------------------+       1:N       +----------------------+
 |       USERS        | <-------------- |       PRODUCTS       |
 |--------------------|                 |----------------------|
 | id (PK)            |                 | id (PK)              |
 | email (UNIQUE)     |                 | seller_id (FK->USERS)|
 | password_hash      |                 | category_id (FK)     |
 | full_name          |                 | name, price, stock   |
 | role (BUYER/SELLER)|                 | avg_rating           |
 +--------------------+                 +----------------------+
      |         |                                  |
      | 1:N     | 1:N                              | 1:N
      v         v                                  v
+-----------+ +-----------+                  +-----------+
| CART_ITEMS| |  ORDERS   |                  |  REVIEWS  |
+-----------+ +-----------+                  +-----------+
                    | 1:N                          |
                    v                              |
              +-------------+                      |
              | ORDER_ITEMS | <--------------------+
              +-------------+ (Verified purchase verification)
```

### 4.2 Data Dictionary Summary

| Table | Primary Key | Foreign Keys | Key Constraints & Indexes |
| :--- | :--- | :--- | :--- |
| `users` | `id` (BIGINT AUTO) | None | `email` UNIQUE, `role` CHECK (BUYER, SELLER, ADMIN) |
| `categories`| `id` (BIGINT AUTO) | None | `slug` UNIQUE, `name` NOT NULL |
| `products` | `id` (BIGINT AUTO) | `seller_id` -> users(id), `category_id` -> categories(id) | `price` >= 0, `stock_quantity` >= 0, `avg_rating` DECIMAL(3,2) |
| `cart_items`| `id` (BIGINT AUTO) | `user_id` -> users(id), `product_id` -> products(id) | UNIQUE (`user_id`, `product_id`), `quantity` > 0 |
| `orders` | `id` (BIGINT AUTO) | `user_id` -> users(id) | `order_number` UNIQUE, `status` CHECK, `total_amount` >= 0 |
| `order_items`| `id` (BIGINT AUTO) | `order_id` -> orders(id), `product_id` -> products(id) | `unit_price` >= 0, `quantity` > 0 |
| `reviews` | `id` (BIGINT AUTO) | `user_id` -> users(id), `product_id` -> products(id), `order_id` -> orders(id) | `rating` BETWEEN 1 AND 5, UNIQUE (`user_id`, `product_id`) |
| `wishlist` | `id` (BIGINT AUTO) | `user_id` -> users(id), `product_id` -> products(id) | UNIQUE (`user_id`, `product_id`) |

---

## 5. CORE MODULE DESCRIPTIONS & IMPLEMENTATION

### 5.1 Module 1: Authentication, Session Management & RBAC
- **Password Protection:** Utilizes the `jBCrypt` library with a workload factor of 12 for password hashing and salting.
- **DTO Isolation:** `UserResponseDTO` is constructed via the Builder pattern and strictly excludes password hash attributes to prevent exposure.
- **Role Enforcement:** The declarative `RoleFilter` verifies URL route prefixes (`/seller/*` for `SELLER`/`ADMIN`, `/admin/*` for `ADMIN` only, and `/buyer/*` for `BUYER`/`ADMIN`).

### 5.2 Module 2: Product Catalog, Live Search & Pagination
- **Search & Filtering:** Dynamic SQL query generation in `ProductDaoImpl` supporting keyword text search, category filtering, min/max price boundaries, and stock availability toggles.
- **Pagination & Sorting:** Efficient database-level pagination with `LIMIT` and `OFFSET` clauses, supporting sorting by price, date created, and average rating.

### 5.3 Module 3: Persistent Shopping Cart & Multi-Channel Checkout
- **Database Cart Persistence:** Guest and authenticated cart operations persist in the database across user sessions.
- **Shipping Calculator:** Implements free shipping for orders exceeding ₹999.00 and a flat ₹50.00 delivery fee for sub-threshold orders.
- **Payment Strategy Pattern:** Decouples checkout processing through `PaymentStrategy`:
  - `UpiPaymentStrategy`: Regex validation of virtual payment addresses (`vpa@bank`).
  - `CardPaymentStrategy`: Luhn/16-digit card and MM/YY expiry verification.
  - `CodPaymentStrategy`: Zero upfront charge with instant simulated booking confirmation.

### 5.4 Module 4: Order Lifecycle & Inventory Integrity
- **Transactional Consistency:** Order placement executes in a single database transaction: verifies stock, decrements stock atomically, clears cart, and creates order/item snapshots.
- **State Machine Transitions:** Orders follow a verified lifecycle:
  `PENDING` -> `CONFIRMED` -> `SHIPPED` -> `DELIVERED` (or `CANCELLED`).
- **Inventory Restoration:** If an un-shipped order is cancelled by the buyer or seller, reserved inventory is automatically restored.

### 5.5 Module 5: Customer Reviews & Rating Engine
- **Verified Purchase Requirement:** A customer can only review a product if they have an order with `DELIVERED` status containing that product item.
- **Recalculation Trigger:** Adding, modifying, or deleting a review immediately recalculates the product's `avg_rating` and `review_count` in the database.

### 5.6 Module 6: Seller Portal & Multi-Vendor Hub
- **Seller Isolation:** Sellers can only view, edit, delete, and fulfill products and order items belonging directly to their seller ID.
- **Real-time Analytics:** Seller dashboard displays total sales revenue, active listing count, low stock warnings (< 5 units), and pending shipment orders.

### 5.7 Module 7: Admin Portal & Platform Governance
- **Global Governance:** System administrators can view overall gross merchandise value (GMV), total active sellers/buyers, toggle product activation status, and promote users across roles (`BUYER` <-> `SELLER`).

### 5.8 Module 8: Buyer Wishlist & Personalization
- **Instant Toggle:** AJAX-enabled 1-click wishlist toggle with immediate state feedback.
- **Category Recommendations:** Recommends trending top-rated items tailored to categories previously interacted with by the customer.

### 5.9 Module 9: AI Shopping Assistant (JayaBot)
- **Context Injection:** Extracts search terms from user queries, queries active catalog products from `ProductDAO`, and injects top product matches into the AI context window.
- **Fallback Architecture:** When `GEMINI_API_KEY` is not present, `MockAiServiceProvider` transparently answers FAQs on delivery fees, payment options, return rules, and seller registration.

---

## 6. SECURITY ARCHITECTURE & DEFENSIVE MEASURES

| Vulnerability Vector | Mitigation Mechanism in JayasriMart |
| :--- | :--- |
| **SQL Injection (SQLi)** | 100% Parameterized queries using `java.sql.PreparedStatement`. |
| **Cross-Site Scripting (XSS)** | JSP JSTL expression evaluation (`<c:out />`) escapes HTML characters. |
| **Broken Access Control** | URL-interception `AuthFilter` and `RoleFilter` running at the servlet container boundary. |
| **Password Exposure** | One-way adaptive BCrypt hashing; passwords never stored or logged in plain text. |
| **Broken Object Level Auth** | Service-level verification ensures sellers can only modify their own products/orders. |
| **Log Injection / Auditability** | SLF4J with parameterized logging and MDC Request-ID correlation for full request traceability. |

---

## 7. VERIFICATION, TESTING & QUALITY GATES

### 7.1 Automated Test Suite Summary
- **Total Test Cases Executed:** **112 Tests**
- **Test Pass Rate:** **100% (0 Failures, 0 Errors, 0 Skipped)**
- **Test Categories:**
  - DAO Layer Tests (with In-Memory H2 DB schema fixtures)
  - Service Layer Business Logic & Exception Tests (with Mockito)
  - Security & Authorization Filter Tests (`AuthFilter`, `RoleFilter`)
  - Health & Diagnostic Endpoint Tests (`HealthServlet`)
  - AI Assistant & Rule Engine Tests (`ChatbotService`, `MockAiServiceProvider`)
  - Payment Strategy Unit Tests (`UpiPaymentStrategy`, `CardPaymentStrategy`, `CodPaymentStrategy`)

### 7.2 Static Code Analysis & Compliance
- **Checkstyle Audit:** `mvn checkstyle:check` passed with **0 Checkstyle violations**.
- **SpotBugs Audit:** `mvn spotbugs:check` passed with zero blocker defects.
- **Maven Packaging:** Successfully compiles and builds standard deployable `.war` package via `mvn clean package`.

---

## 8. CONCLUSION & FUTURE SCOPE

The **JayasriMart** project successfully demonstrates a high-performance, robust, and maintainable multi-seller e-commerce web platform developed strictly on Java enterprise specifications (Jakarta Servlet 5, JDBC, HikariCP). By adhering to clean MVC separation, DAO patterns, and defense-in-depth security, the application meets all academic requirements and industry standards.

### Future Enhancements:
1. Integration of live payment gateways (e.g. Razorpay / Stripe Webhooks).
2. Automated PDF invoice generation upon order delivery.
3. WebSocket-based real-time order status notifications for buyers and sellers.
