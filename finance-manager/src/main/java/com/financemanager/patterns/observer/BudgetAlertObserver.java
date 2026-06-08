package com.financemanager.patterns.observer;

import com.financemanager.datastructures.BudgetAlertHeap;
import com.financemanager.model.Budget;
import com.financemanager.model.Transaction;
import com.financemanager.repository.BudgetRepository;
import com.financemanager.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Concrete Observer #1 — Budget Alert Checker.
 *
 * Reacts to CREATED/UPDATED expense transactions by:
 * 1. Loading the user's budget for that category/month.
 * 2. Computing the usage ratio using our custom BudgetAlertHeap.
 * 3. Logging a warning if the user is at ≥80% or over budget.
 *
 * In a production system, this would also trigger email notifications.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetAlertObserver implements TransactionObserver {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher publisher;

    @PostConstruct
    public void register() {
        publisher.subscribe(this);
    }

    @Override
    public void onTransactionEvent(TransactionEvent event) {
        Transaction tx = event.transaction();

        // Only react to expense events
        if (tx.getType() != Transaction.TransactionType.EXPENSE) return;
        if (event.eventType() == TransactionEvent.EventType.DELETED) return;

        int month = tx.getTransactionDate().getMonthValue();
        int year = tx.getTransactionDate().getYear();

        // Find relevant budget if it exists
        budgetRepository.findByUserAndCategoryAndMonthAndYear(
            tx.getUser(), tx.getCategory(), month, year
        ).ifPresent(budget -> checkBudget(tx, budget, month, year));
    }

    private void checkBudget(Transaction tx, Budget budget, int month, int year) {
        // Sum all expenses in this category/month
        BigDecimal totalSpent = transactionRepository
            .sumExpensesByCategoryAndMonthAndYear(tx.getUser(), tx.getCategory(), month, year);

        if (totalSpent == null) totalSpent = BigDecimal.ZERO;

        // Use our custom BudgetAlertHeap to compute and surface the alert
        BudgetAlertHeap heap = new BudgetAlertHeap();
        double ratio = totalSpent.divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP).doubleValue();

        heap.insert(new BudgetAlertHeap.BudgetAlert(
            tx.getCategory().name(),
            totalSpent,
            budget.getLimitAmount(),
            ratio
        ));

        BudgetAlertHeap.BudgetAlert alert = heap.peek();
        if (alert != null && alert.isOverBudget()) {
            log.warn("🚨 OVER BUDGET: User {} exceeded {} budget. Spent: {} / Limit: {}",
                tx.getUser().getEmail(), alert.category(), alert.spent(), alert.limit());
        } else if (alert != null && alert.isWarning()) {
            log.warn("⚠️  BUDGET WARNING: User {} at {}% of {} budget.",
                tx.getUser().getEmail(),
                String.format("%.0f", ratio * 100),
                alert.category());
        }
    }
}
