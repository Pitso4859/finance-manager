package com.financemanager.patterns.decorator;

import com.financemanager.model.Transaction;
import com.financemanager.model.User;
import com.financemanager.service.TransactionService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║              DESIGN PATTERN — Decorator                              ║
 * ║                                                                      ║
 * ║  Problem: We want to add validation and logging behaviour to         ║
 * ║  TransactionService without subclassing it or modifying it.         ║
 * ║                                                                      ║
 * ║  Solution: The Decorator wraps the real service and adds behaviour   ║
 * ║  before/after delegation — just like Java's BufferedReader wraps     ║
 * ║  FileReader to add buffering.                                        ║
 * ║                                                                      ║
 * ║  This keeps TransactionServiceImpl clean and focused on business     ║
 * ║  logic. Cross-cutting concerns (audit, rate-limiting, extra          ║
 * ║  validation) live in decorators.                                     ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
@Slf4j
public class ValidatedTransactionService {

    private static final BigDecimal MAX_SINGLE_TRANSACTION = new BigDecimal("1_000_000");
    private static final LocalDate EARLIEST_ALLOWED_DATE = LocalDate.of(2000, 1, 1);

    private final TransactionService delegate;

    public ValidatedTransactionService(TransactionService delegate) {
        this.delegate = delegate;
    }

    /**
     * Decorated save — adds pre-save validation before delegating.
     * Throws IllegalArgumentException with user-friendly messages.
     */
    public Transaction save(Transaction transaction, User user) {
        preValidate(transaction);
        log.info("Saving transaction: {} [{}] R{} for user={}",
            transaction.getDescription(),
            transaction.getType(),
            transaction.getAmount(),
            user.getEmail());

        Transaction saved = delegate.save(transaction, user);

        log.info("Transaction saved successfully with id={}", saved.getId());
        return saved;
    }

    // ── Private Validators ────────────────────────────────────────────

    private void preValidate(Transaction t) {
        if (t.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero.");
        }
        if (t.getAmount().compareTo(MAX_SINGLE_TRANSACTION) > 0) {
            throw new IllegalArgumentException(
                "Single transaction amount exceeds the maximum allowed (R1,000,000).");
        }
        if (t.getTransactionDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Transaction date cannot be in the future.");
        }
        if (t.getTransactionDate().isBefore(EARLIEST_ALLOWED_DATE)) {
            throw new IllegalArgumentException("Transaction date cannot be before year 2000.");
        }
        if (t.getDescription() == null || t.getDescription().isBlank()) {
            throw new IllegalArgumentException("Transaction description is required.");
        }
    }
}
