package com.financemanager.dto;

import com.financemanager.datastructures.BudgetAlertHeap;
import com.financemanager.model.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardSummary(
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal netBalance,
        BigDecimal savingsRate,
        List<Transaction> recentTransactions,
        List<BudgetAlertHeap.BudgetAlert> budgetAlerts,
        Map<String, BigDecimal> topCategories
) {}
