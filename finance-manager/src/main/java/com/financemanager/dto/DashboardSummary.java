package com.financemanager.dto;

import com.financemanager.datastructures.BudgetAlertHeap;
import com.financemanager.model.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Dashboard summary DTO — aggregates all data the dashboard view needs
 * into a single object. Avoids multiple model attributes in the controller.
 */
public record DashboardSummary(
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal netBalance,
    BigDecimal savingsRate,                // (income - expense) / income * 100
    List<Transaction> recentTransactions,  // last 5
    List<BudgetAlertHeap.BudgetAlert> budgetAlerts,
    Map<String, BigDecimal> topCategories  // top 5 expense categories
) {
    public boolean isPositiveBalance() {
        return netBalance.compareTo(BigDecimal.ZERO) >= 0;
    }
}
