package com.financemanager.service.impl;

import com.financemanager.datastructures.BudgetAlertHeap;
import com.financemanager.datastructures.CategorySpendingMap;
import com.financemanager.datastructures.TransactionLedger;
import com.financemanager.dto.DashboardSummary;
import com.financemanager.model.Budget;
import com.financemanager.model.Transaction;
import com.financemanager.model.User;
import com.financemanager.repository.BudgetRepository;
import com.financemanager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DashboardService — aggregates all the metrics shown on the dashboard.
 *
 * This is where all three custom data structures work together:
 *
 * 1. TransactionLedger (doubly linked list) — loads transactions into an
 *    in-memory ledger for O(1) balance computation and recent-first traversal.
 *
 * 2. CategorySpendingMap (EnumMap) — O(1) per-category accumulation, used to
 *    find the top spending categories.
 *
 * 3. BudgetAlertHeap (min-heap) — surfaces the most over-budget categories
 *    first without sorting the entire list.
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    public DashboardSummary buildDashboard(User user) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);

        List<Transaction> monthlyTx = transactionRepository
                .findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(user, startOfMonth, now);

        TransactionLedger ledger = new TransactionLedger();
        for (Transaction tx : monthlyTx) ledger.addLast(tx);

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (Transaction tx : ledger) {
            if (tx.getType() == Transaction.TransactionType.INCOME) {
                income = income.add(tx.getAmount());
            } else {
                expense = expense.add(tx.getAmount());
            }
        }
        BigDecimal netBalance = income.subtract(expense);

        BigDecimal savingsRate = income.compareTo(BigDecimal.ZERO) > 0
                ? netBalance.divide(income, 4, RoundingMode.HALF_UP)
                  .multiply(BigDecimal.valueOf(100))
                  .setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        CategorySpendingMap spendingMap = new CategorySpendingMap();
        spendingMap.loadFromTransactions(monthlyTx);

        Map<String, BigDecimal> topCategories = new LinkedHashMap<>();
        spendingMap.topN(5).forEach(e -> topCategories.put(e.getKey().name(), e.getValue()));

        BudgetAlertHeap alertHeap = new BudgetAlertHeap();
        List<Budget> budgets = budgetRepository.findByUserAndMonthAndYear(
                user, now.getMonthValue(), now.getYear());

        for (Budget budget : budgets) {
            BigDecimal spent = spendingMap.getTotal(budget.getCategory());
            if (spent != null && budget.getLimitAmount() != null && budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {
                double ratio = spent.divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP).doubleValue();
                if (ratio >= 0.8) {
                    alertHeap.insert(new BudgetAlertHeap.BudgetAlert(
                            budget.getCategory().name(),
                            spent,
                            budget.getLimitAmount(),
                            ratio
                    ));
                }
            }
        }

        return new DashboardSummary(
                income,
                expense,
                netBalance,
                savingsRate,
                transactionRepository.findTop5ByUserOrderByTransactionDateDesc(user),
                alertHeap.drainAll(),
                topCategories
        );
    }
}