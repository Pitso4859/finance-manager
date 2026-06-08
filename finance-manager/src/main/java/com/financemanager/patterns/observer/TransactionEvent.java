package com.financemanager.patterns.observer;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║              DESIGN PATTERN — Observer (Event System)                ║
 * ║                                                                      ║
 * ║  Problem: When a transaction is saved, multiple subsystems need      ║
 * ║  to react — budget checker, notification sender, audit logger.       ║
 * ║  Hard-coding these calls in TransactionService creates tight         ║
 * ║  coupling (violates OCP and SRP).                                   ║
 * ║                                                                      ║
 * ║  Solution: Observer pattern decouples the transaction save from     ║
 * ║  its side effects. New observers can be added without touching       ║
 * ║  TransactionService at all.                                          ║
 * ║                                                                      ║
 * ║  Participants:                                                       ║
 * ║    TransactionEvent  — the event data                                ║
 * ║    TransactionObserver — subscriber interface                        ║
 * ║    TransactionEventPublisher — the observable subject                ║
 * ║    BudgetAlertObserver — concrete observer                           ║
 * ║    AuditLogObserver    — concrete observer                           ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */

import com.financemanager.model.Transaction;
import java.time.LocalDateTime;

/** Immutable event record carrying all context about a transaction */
public record TransactionEvent(
    Transaction transaction,
    EventType eventType,
    LocalDateTime occurredAt
) {
    public enum EventType { CREATED, UPDATED, DELETED }

    public static TransactionEvent created(Transaction t) {
        return new TransactionEvent(t, EventType.CREATED, LocalDateTime.now());
    }

    public static TransactionEvent updated(Transaction t) {
        return new TransactionEvent(t, EventType.UPDATED, LocalDateTime.now());
    }

    public static TransactionEvent deleted(Transaction t) {
        return new TransactionEvent(t, EventType.DELETED, LocalDateTime.now());
    }
}
