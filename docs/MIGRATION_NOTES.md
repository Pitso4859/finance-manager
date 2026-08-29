# Migration Notes

## Original implementation

The uploaded project used a web application architecture:

- Spring Boot application startup.
- Spring MVC controllers.
- Spring Security HTTP configuration.
- Spring Data JPA repositories.
- Thymeleaf templates.
- HTML pages.
- CSS styling.
- JavaScript behavior.
- PostgreSQL runtime dependency.
- Lombok code generation.

That architecture was valid for a browser-based application, but it did not satisfy the requirement that both the frontend and backend be implemented in Java with a GUI frontend.

## New implementation

The redesigned project uses:

- Java 21 application startup.
- Swing desktop GUI.
- Plain Java service layer.
- Plain Java repository layer.
- File-based local persistence.
- Java NIO atomic writes.
- JDK cryptography for password hashing.
- Explicit constructor-based dependency assembly.
- Java-only custom data structures.
- Java-only design pattern implementations.

## Removed files and technologies

The following application technologies were intentionally removed:

- `src/main/resources/templates`
- `src/main/resources/static/css`
- `src/main/resources/static/js`
- Thymeleaf
- HTML
- CSS
- JavaScript
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- PostgreSQL driver
- Lombok

## Why Spring was not retained

Keeping Spring while replacing only the view layer would leave unnecessary web and framework infrastructure in a local desktop application. It would also increase startup time, dependency count, configuration surface, and developer onboarding requirements.

The new design keeps the useful engineering ideas from the original project but uses technology appropriate for the requested desktop target.

## Preserved engineering concepts

The redesign preserves and strengthens these concepts:

- Layered architecture.
- User ownership checks.
- Secure password storage.
- Transaction validation.
- BigDecimal for monetary values.
- Custom transaction ledger.
- Category spending aggregation.
- Budget priority heap.
- Observer pattern.
- Strategy pattern.
- Audit logging.
- Testable business logic.

## Behavioral changes

### Authentication

Browser sessions were replaced with an in-memory desktop session.

### Persistence

Database persistence was replaced with local Java file persistence so the project has no runtime database requirement.

### Navigation

HTTP routes were replaced with a single desktop window using `CardLayout`.

### Forms

HTML forms were replaced with Swing dialogs and service-level validation.

### Reports

Browser report pages were replaced with Java tables, summary panels, and CSV export.

## Compatibility note

The new application does not automatically import the old PostgreSQL database. If migration of existing production data is required, add a one-time Java import utility that reads an exported CSV or SQL dataset and saves equivalent `User`, `Transaction`, and `Budget` records through the new repositories.
