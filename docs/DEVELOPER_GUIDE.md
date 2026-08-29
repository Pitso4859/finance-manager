# Developer Guide

## Development rules

1. Keep application source code in Java.
2. Do not add HTML, CSS, JavaScript, FXML, or browser-based views.
3. UI code belongs under `ui`.
4. Business rules belong under `service`.
5. Storage operations belong under `repository` and `persistence`.
6. Do not place password hashing or session logic inside UI classes.
7. Use `BigDecimal` for money.
8. Use `LocalDate` for transaction accounting dates.
9. Scope financial records by user ID.
10. Add a test whenever a business rule changes.

## Adding a new screen

1. Create a Swing panel in `ui.panels`.
2. Add required service dependencies through the constructor.
3. Add the panel to `MainFrame` using a new CardLayout key.
4. Add one sidebar navigation button.
5. Add a refresh method if the panel displays changing data.

Do not access `FileDataStore` from the panel.

## Adding a new report

1. Implement `ReportStrategy`.
2. Give it a stable `key()`.
3. Give it a readable `displayName()`.
4. Implement `generate()`.
5. Register the strategy in `AppServices`.

The report UI will automatically list the registered strategy.

## Adding a new transaction side effect

1. Implement `TransactionObserver`.
2. Register the observer with `TransactionEventPublisher` in `AppServices`.

Examples:

- Notification logging.
- Backup triggers.
- Analytics counters.

Do not add those side effects directly to `TransactionService`.

## Replacing file storage with SQL later

Recommended migration approach:

1. Keep model and service APIs stable.
2. Replace repository internals with JDBC-based implementations.
3. Add schema migrations.
4. Map UUID strings to database primary keys or preserve UUID columns.
5. Add indexes for `user_id`, transaction date, and budget period.
6. Keep user ownership checks in repository queries.

The UI should not require changes because it depends on services rather than persistence.

## UI conventions

- Use `UiTheme` for consistent colors, fonts, cards, and buttons.
- Use text labels on actions.
- Avoid decorative symbols and emojis.
- Keep tables non-editable; editing must happen in validated dialogs.
- Use confirmation dialogs for destructive operations.
- Keep validation messages specific and actionable.

## Testing conventions

The current repository has JDK-only executable tests. Run them with assertions enabled.

When third-party test dependencies become acceptable, migrate these scenarios to JUnit 5 while preserving the same behavioral coverage.

High-priority tests:

- Authentication success and failure.
- Duplicate registration rejection.
- Transaction category/type validation.
- User ownership enforcement.
- Budget duplicate detection.
- Dashboard arithmetic.
- Report arithmetic.
- File persistence reload.

## Code review checklist

Before merging a change, verify:

- The project compiles on Java 21.
- No web frontend files were introduced.
- No plain-text password storage was introduced.
- New money calculations use BigDecimal.
- New data operations are user-scoped where required.
- UI classes contain no persistence logic.
- Service rules have test coverage.
- User-visible text contains no emojis.
- Documentation is updated if architecture changes.
