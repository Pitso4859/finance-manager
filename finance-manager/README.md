# 💰 Personal Finance Manager

> A full-stack Spring Boot application demonstrating enterprise-grade software engineering practices — custom data structures, four design patterns, layered architecture, Spring Security, and comprehensive unit testing.

**Tech Stack:** Java 21 · Spring Boot 3.2 · Spring Security · MySQL · Thymeleaf · JPA/Hibernate · JUnit 5 · Lombok

---

## 🎯 Why This Project Exists

Most finance apps are CRUD wrappers. This one is engineered. Every architectural decision has a reason, and that reason is documented in the code. This project was built to demonstrate:

- How to apply **data structures** to real problems (not just in an interview whiteboard)
- How **design patterns** solve concrete engineering problems
- How to follow **SOLID principles** and the **Software Engineering process** in a real codebase

---

## 🏗️ Architecture

```
com.financemanager/
├── config/              # Spring Security, app configuration
├── controller/          # HTTP layer — thin, delegates to services
├── datastructures/      # Custom DS implementations (see below)
│   ├── TransactionLedger.java      ← Doubly Linked List
│   ├── BudgetAlertHeap.java        ← Min-Heap
│   └── CategorySpendingMap.java    ← EnumMap (O(1) aggregation)
├── dto/                 # Request/Response objects (API contract)
├── exception/           # Global exception handling
├── model/               # JPA entities
├── patterns/            # Design pattern implementations
│   ├── observer/        ← Observer (budget alerts)
│   ├── strategy/        ← Strategy (report generation)
│   ├── factory/         ← Factory Method (object creation)
│   └── decorator/       ← Decorator (validation wrapping)
├── repository/          # Spring Data JPA interfaces
└── service/             # Business logic layer
    └── impl/            # Concrete implementations
```

**Layered Architecture (Controller → Service → Repository):**
- Controllers handle only HTTP concerns (routing, model, view selection)
- Services own all business logic and are transaction-managed
- Repositories are interfaces — Spring Data generates the implementation

---

## 📊 Custom Data Structures

### 1. `TransactionLedger` — Doubly Linked List

**File:** `datastructures/TransactionLedger.java`

**Problem:** Transaction history needs to be traversed forward (oldest → newest for reports) *and* backward (newest → oldest for "recent activity"). An ArrayList wastes memory on resize; a singly linked list can't go backward.

**Solution:** Doubly linked list with sentinel-free head/tail pointers.

```java
TransactionLedger ledger = new TransactionLedger();
ledger.addFirst(transaction);              // O(1) — newest at head
BigDecimal balance = ledger.computeBalance(); // O(n) — income - expense
for (Transaction t : ledger.reverseIterable()) { ... } // newest → oldest
```

| Operation    | Complexity | Reason                          |
|--------------|------------|----------------------------------|
| `addFirst`   | O(1)       | Only head pointer changes        |
| `addLast`    | O(1)       | Only tail pointer changes        |
| `removeById` | O(n)       | Must scan to find by id          |
| `computeBalance` | O(n)   | Must visit all nodes             |
| `size`       | O(1)       | Maintained as a counter          |

---

### 2. `BudgetAlertHeap` — Min-Heap

**File:** `datastructures/BudgetAlertHeap.java`

**Problem:** After saving a transaction, we need to show the user which budget categories are most at risk. Sorting all categories every time is O(k log k) per transaction. We can do better.

**Solution:** Min-Heap keyed on overspend ratio (`spent / limit`). The most over-budget category is always at the root.

```java
BudgetAlertHeap heap = new BudgetAlertHeap();
heap.insert(new BudgetAlert("FOOD", spent, limit, 1.25));   // O(log n)
BudgetAlert mostUrgent = heap.peek();                        // O(1)
List<BudgetAlert> allSorted = heap.drainAll();               // O(n log n)
```

Array-backed heap: `parent(i) = (i-1)/2`, `left(i) = 2i+1`, `right(i) = 2i+2`

---

### 3. `CategorySpendingMap` — EnumMap

**File:** `datastructures/CategorySpendingMap.java`

**Problem:** Aggregate spending by category for the dashboard. `HashMap<Category, BigDecimal>` works, but has hashing overhead and no guaranteed iteration order.

**Solution:** `EnumMap` — backed by an ordinal-indexed array. Zero hash collisions, guaranteed O(1) get/put, deterministic iteration order.

```java
CategorySpendingMap map = new CategorySpendingMap();
map.loadFromTransactions(transactions);              // O(n)
BigDecimal foodTotal = map.getTotal(Category.FOOD); // O(1)
BigDecimal foodPct   = map.getPercentage(Category.FOOD); // O(1)
List<...> top5       = map.topN(5);                 // O(k log k)
```

---

## 🧩 Design Patterns

### 1. Observer Pattern — Budget Alert System

**Files:** `patterns/observer/`

**Problem:** When a transaction is saved, multiple subsystems need to react: check budgets, write audit logs, send notifications. Wiring all of this directly into `TransactionService` creates tight coupling and violates the **Single Responsibility Principle**.

**Solution:** `TransactionEventPublisher` (Subject) broadcasts events to registered `TransactionObserver` implementations. New observers can be added without touching `TransactionService`.

```java
// Adding a new side effect = zero changes to existing code
publisher.subscribe(new SMSNotificationObserver(...));  // just add an observer
```

---

### 2. Strategy Pattern — Report Generation

**Files:** `patterns/strategy/`

**Problem:** Three different report algorithms (monthly summary, category breakdown, trend analysis) would produce a messy switch statement in `ReportService`, violating the **Open/Closed Principle**.

**Solution:** Each algorithm is a `ReportStrategy` implementation. `ReportController` selects one at runtime with no if/else.

```java
// Adding a new report type = zero changes to existing code
// Just create a new class implementing ReportStrategy and annotate @Component
```

---

### 3. Factory Method — Transaction Creation

**File:** `patterns/factory/TransactionFactory.java`

**Problem:** Transaction creation involves validation (category-type consistency), field trimming, timestamp injection, and owner assignment. Doing this inline in the controller couples it to entity details.

**Solution:** `TransactionFactory.createFromRequest()` centralises all creation logic and enforces business rules at the boundary.

```java
// This one line handles validation, trimming, defaults, and ownership
Transaction tx = TransactionFactory.createFromRequest(request, user);
```

---

### 4. Decorator Pattern — Validated Transaction Service

**File:** `patterns/decorator/ValidatedTransactionService.java`

**Problem:** We want to add pre-save validation (max amount, future dates, etc.) without modifying `TransactionServiceImpl` or subclassing it.

**Solution:** `ValidatedTransactionService` wraps any `TransactionService` and adds validation before delegating — just like Java's `BufferedReader` wraps `FileReader`.

---

## 🔒 Software Engineering Practices

### SOLID Principles in Action

| Principle | Where Applied |
|-----------|--------------|
| **S** — Single Responsibility | Controllers handle HTTP only. Services own business logic. Factories own creation. |
| **O** — Open/Closed | New report types via Strategy. New observers via Observer. No existing code modified. |
| **L** — Liskov Substitution | `TransactionServiceImpl` can be swapped with any `TransactionService` implementation. |
| **I** — Interface Segregation | `TransactionService` defines only what clients need — no fat interfaces. |
| **D** — Dependency Inversion | Controllers depend on `TransactionService` interface, not `TransactionServiceImpl`. |

### Security
- BCrypt password hashing with cost factor 12
- CSRF protection enabled
- Route-level authorization (`@PreAuthorize`, `SecurityFilterChain`)
- Ownership validation before every read/write (`findByIdAndUser`)
- No sensitive data in `application.properties` (use env variables in production)

### Data Integrity
- `BigDecimal` for all monetary values (never `float`/`double`)
- `@Valid` + Bean Validation on all DTOs
- Database-level unique constraints and indexes
- `@Transactional` on all write operations
- Read-only transactions for queries (performance)

### Error Handling
- `GlobalExceptionHandler` — centralised, no stack traces to the user
- Meaningful exception messages at every layer
- PRG pattern (Post-Redirect-Get) on all forms — prevents duplicate submissions

---

## 🧪 Testing

```bash
mvn test
```

Tests cover:
- `TransactionLedger` — all linked list operations and edge cases
- `BudgetAlertHeap` — heap property, priority ordering, flag correctness
- `TransactionFactory` — field mapping, whitespace trimming, invalid combos

```
TransactionLedgerTest     ✅ 7 tests
BudgetAlertHeapTest       ✅ 5 tests
TransactionFactoryTest    ✅ 4 tests
```

---

## 🚀 Running Locally

### Prerequisites
- Java 21+
- Maven 3.9+
- MySQL 8+

### Steps

```bash
# 1. Clone
git clone https://github.com/Pitso4859/finance-manager.git
cd finance-manager

# 2. Create database
mysql -u root -p -e "CREATE DATABASE finance_manager;"

# 3. Set your credentials in application.properties
spring.datasource.username=root
spring.datasource.password=your_password

# 4. Run
mvn spring-boot:run

# 5. Open
http://localhost:8080/auth/register
```

---

## 📐 Database Schema

```
users
  id, full_name, email (unique), password, role, enabled, created_at

transactions
  id, description, amount, type, category, transaction_date, notes, created_at, user_id
  INDEX: (user_id, transaction_date)   ← optimises date range queries
  INDEX: (category)                    ← optimises category aggregation

budgets
  id, category, limit_amount, month, year, user_id
  UNIQUE: (user_id, category, month, year)
```

---

## 👤 Author

**Pitso Nkotolane** — Full-Stack Developer & Software Engineer  
📧 pitso@nkotolane.dev  
🌐 [nkotolane-pitso-portfolio.vercel.app](https://nkotolane-pitso-portfolio.vercel.app)  
💼 [LinkedIn](https://linkedin.com/in/pitso-nkotolane)  
🐙 [GitHub](https://github.com/Pitso4859)
