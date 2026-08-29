# Architecture

## Architectural style

Finance Manager uses a layered desktop architecture with explicit dependencies.

```text
FinanceManagerApplication
          |
          v
     AppServices
          |
   +------+------+----------------+
   |             |                |
   v             v                v
Swing UI      Services         Security
                 |
        +--------+---------+
        |                  |
        v                  v
    Patterns         Data Structures
        |
        v
   Repositories
        |
        v
   FileDataStore
        |
        v
 finance-manager.dat
```

## Presentation layer

Package: `com.financemanager.ui`

Responsibilities:

- Display application state.
- Capture user input.
- Call service methods.
- Display success, validation, and failure messages.
- Navigate between dashboard, transactions, budgets, and reports.

The presentation layer must not:

- Read the data file.
- Hash passwords.
- Calculate business metrics.
- Decide transaction ownership.

## Service layer

Package: `com.financemanager.service`

### AuthService

Coordinates registration and login. It delegates password hashing to `PasswordHasher` and account persistence to `UserRepository`.

### TransactionService

Owns transaction validation and ownership rules. It publishes events after successful creates, updates, and deletes.

### BudgetService

Owns monthly budget rules and duplicate prevention.

### DashboardService

Combines repository data with the custom data structures to produce `DashboardSummary`.

### ReportService

Selects report strategies, retrieves transactions for a date range, and exports report results.

## Repository layer

Package: `com.financemanager.repository`

Repositories translate business-oriented queries into operations over `FileDataStore`.

Every transaction or budget lookup contains a user ID. This prevents accidental cross-user access even though all application data is stored in one state file.

## Persistence layer

Package: `com.financemanager.persistence`

`FileDataStore` owns the loaded application state.

Read flow:

```text
Repository -> read lock -> in-memory AppState -> result
```

Write flow:

```text
Repository -> write lock -> mutate AppState -> temporary file -> atomic replace
```

This design avoids repeatedly deserializing the entire data file for every screen refresh.

## Security architecture

### Password storage

`PasswordHasher` stores values in the following logical format:

```text
iterations:base64-salt:base64-derived-key
```

PBKDF2WithHmacSHA256 is used with a cryptographically secure random salt.

### Session

`SessionManager` stores only the currently authenticated `User` object in memory. Signing out clears it.

### Authorization

Authorization is enforced through user-scoped repository operations. A transaction ID by itself is not enough to retrieve a record; the current user's ID must also match.

## Design patterns

### Observer

Classes:

- `TransactionEventPublisher`
- `TransactionObserver`
- `AuditLogObserver`

Purpose:

Transaction writes should not directly know how audit logging is implemented. The service publishes an event, and observers handle side effects independently.

Extension example:

A future notification observer can subscribe without changing transaction persistence logic.

### Strategy

Classes:

- `ReportStrategy`
- `SummaryReportStrategy`
- `ExpenseReportStrategy`

Purpose:

Report algorithms vary independently from the report screen and report service.

Extension example:

A future cash-flow report can implement `ReportStrategy` and be registered in `AppServices`.

## Custom data structures

### TransactionLedger

Implementation: doubly linked list.

Use:

- Ordered transaction traversal.
- O(1) insertion at the head or tail.
- Demonstrates direct linked-list implementation rather than relying exclusively on collection classes.

### CategorySpendingMap

Implementation: EnumMap.

Use:

- Expense accumulation by fixed category enum.
- Efficient category lookup without hash collisions.

### BudgetAlertHeap

Implementation: max heap by budget usage ratio.

Use:

- Keep the most urgent overused budget at the root.
- Dashboard alerts are returned from highest to lowest usage ratio.

## Data model

### User

- id
- fullName
- email
- passwordHash
- createdAt

### Transaction

- id
- userId
- description
- amount
- type
- category
- transactionDate
- notes
- createdAt
- updatedAt

### Budget

- id
- userId
- category
- limitAmount
- month
- year
- createdAt

## Error handling

Domain and infrastructure failures use focused runtime exceptions:

- `ValidationException`
- `AuthenticationException`
- `ResourceNotFoundException`
- `DataAccessException`

The GUI catches failures at user-action boundaries and converts them into readable dialogs.

## Threading

Swing controls must run on the Event Dispatch Thread. `FinanceManagerApplication` starts the UI using `SwingUtilities.invokeLater`.

The persistence layer is additionally protected using a `ReentrantReadWriteLock`, allowing safe future use from background workers.

## Performance characteristics

Startup:

- One application-state file is loaded once.

Reads:

- Performed against in-memory collections.

Writes:

- Update in-memory state and persist the complete state file.

This is efficient for personal-finance data volumes. If transaction volume grows to enterprise scale, replace the repository implementation with indexed SQL storage.
