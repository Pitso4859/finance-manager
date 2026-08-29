# Engineering Plan

## 1. Problem statement

The original project mixed a Java backend with a browser frontend built using Thymeleaf, HTML, CSS, and JavaScript. The requested target is a desktop application whose frontend and backend are implemented in Java.

The redesign therefore removes the web delivery model rather than trying to place a desktop GUI on top of Spring MVC.

## 2. Target architecture

The target is a single-user Java desktop application using a layered architecture:

1. Presentation layer: Swing frames, panels, dialogs, tables, and navigation.
2. Service layer: authentication, transactions, budgets, dashboard aggregation, and reports.
3. Domain layer: user, transaction, budget, category, and transaction type models.
4. Repository layer: storage-oriented interfaces represented by focused repository classes.
5. Persistence layer: a thread-safe file data store with atomic replacement.
6. Security layer: password hashing and authenticated session state.
7. Cross-cutting patterns: Observer for transaction side effects and Strategy for reports.
8. Algorithm layer: linked list, heap, and category aggregation structures.

## 3. Major migration decisions

### Remove Spring Boot

Reason:

- Spring Boot was primarily supporting web routing, templates, security filters, JPA, and framework dependency injection.
- Those concerns are not required for a local Swing desktop application.
- Removing it reduces startup cost, dependency size, framework complexity, and configuration overhead.

Replacement:

- Explicit Java object construction in `AppServices`.
- Service classes with constructor dependencies.
- File repositories instead of Spring Data JPA.
- Application session instead of HTTP session.

### Replace browser UI with Swing

Reason:

- Swing is part of the JDK.
- It keeps all UI implementation in Java source code.
- It does not require FXML, CSS, JavaScript, Node.js, a browser, or a local web server.

### Replace database dependency with local Java persistence

Reason:

- The application should run without installing PostgreSQL or MySQL.
- A portfolio reviewer should be able to start it with a JDK only.

Implementation:

- `FileDataStore` keeps `AppState` in memory.
- A read/write lock protects operations.
- Writes go to a temporary file and then replace the active data file atomically where supported.

Tradeoff:

- Java object serialization is suitable for this local portfolio project, not a distributed enterprise system.
- A production system should replace the persistence implementation with PostgreSQL or another durable database.

### Replace BCrypt dependency with JDK cryptography

Implementation:

- PBKDF2WithHmacSHA256.
- 210,000 iterations.
- 16-byte random salt.
- 256-bit derived key.
- Constant-time comparison.

Benefit:

- No external security library is required.

## 4. Functional plan

### Authentication

- Register.
- Normalize email addresses.
- Reject duplicate accounts.
- Enforce minimum password length.
- Hash passwords before persistence.
- Authenticate and create a local session.

### Transaction management

- Create, edit, delete, list.
- Use BigDecimal for money.
- Use LocalDate for accounting dates.
- Validate transaction category against transaction type.
- Publish domain events after successful writes.

### Budget management

- Create, edit, delete, list.
- Use one budget per expense category per month/year.
- Feed usage data into dashboard warnings.

### Dashboard

- Compute current-month income, expenses, net balance, and savings rate.
- Show recent transactions.
- Aggregate expense categories.
- Prioritize budget alerts.

### Reports

- Use Strategy pattern to select report algorithms.
- Support summary and expense-focused reports.
- Export report data to CSV.

## 5. Non-functional requirements

### Maintainability

- No UI class performs persistence.
- No repository performs presentation logic.
- Services own business rules.
- Utility classes own formatting and validation concerns.

### Security

- Never persist plain-text passwords.
- Do not expose data from another user ID.
- Clear password character arrays after authentication operations.
- Keep audit logging isolated as an observer.

### Performance

- Keep active state in memory after startup.
- Avoid loading files for every repository query.
- Use O(1) append operations in the ledger.
- Use EnumMap for category aggregation.
- Use a heap for budget risk prioritization.
- Persist only after write operations.

### Reliability

- Lock shared persistence state.
- Use temporary-file writes before replacement.
- Preserve unreadable data files as corrupt backups instead of silently overwriting them.
- Prevent observer failures from cancelling completed financial operations.

### Usability

- Use one consistent desktop shell.
- Keep navigation visible.
- Use text labels rather than unexplained icons.
- Keep forms small and direct.
- Provide clear validation dialogs.

## 6. Verification plan

### Compile verification

Compile every production source using Java 21 with no external classpath.

### Core smoke testing

Verify:

- Password hashing and verification.
- Linked-list balance calculation.
- Heap priority behavior.

### Service integration testing

Verify:

- Registration.
- Login.
- Income creation.
- Expense creation.
- Budget creation.
- Dashboard calculations.
- Budget alert generation.
- Report net calculation.

## 7. Future roadmap

Phase 1 completed:

- Java-only Swing conversion.
- Local persistence.
- Authentication.
- Transactions.
- Budgets.
- Dashboard.
- Reports.
- CSV export.
- Documentation.
- Tests.

Recommended Phase 2:

- Search and filter toolbar for transactions.
- Import transactions from CSV.
- Recurring transactions.
- Savings goals.
- User profile settings.
- Dark theme implemented in Java UI defaults.
- Printable report support.

Recommended Phase 3 for enterprise deployment:

- Database-backed repository implementation.
- Flyway or Liquibase migrations.
- JUnit 5 test suite.
- Structured logging.
- CI pipeline.
- Code coverage gates.
- Signed native installers using `jpackage`.
- Data encryption at rest.
