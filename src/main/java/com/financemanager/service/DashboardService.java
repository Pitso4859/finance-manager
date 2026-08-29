package com.financemanager.service;

import com.financemanager.datastructures.*;
import com.financemanager.dto.DashboardSummary;
import com.financemanager.model.*;
import com.financemanager.repository.*;

import java.math.*;
import java.time.LocalDate;
import java.util.*;

public final class DashboardService {
    private final TransactionRepository transactions;
    private final BudgetRepository budgets;
    public DashboardService(TransactionRepository transactions, BudgetRepository budgets) {
        this.transactions = transactions; this.budgets = budgets;
    }

    public DashboardSummary build(User user) {
        LocalDate now = LocalDate.now(); LocalDate start = now.withDayOfMonth(1);
        List<Transaction> monthly = transactions.findByUserIdAndDateRange(user.getId(), start, now);
        TransactionLedger ledger = new TransactionLedger(); monthly.forEach(ledger::addLast);
        BigDecimal income = BigDecimal.ZERO, expenses = BigDecimal.ZERO;
        for (Transaction tx : ledger) {
            if (tx.getType() == TransactionType.INCOME) income = income.add(tx.getAmount());
            else expenses = expenses.add(tx.getAmount());
        }
        BigDecimal net = income.subtract(expenses);
        BigDecimal savings = income.signum() > 0
                ? net.divide(income, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        CategorySpendingMap spending = new CategorySpendingMap(); spending.loadFromTransactions(monthly);
        Map<String, BigDecimal> top = new LinkedHashMap<>(); spending.topN(5).forEach(e -> top.put(e.getKey().name(), e.getValue()));
        BudgetAlertHeap heap = new BudgetAlertHeap();
        for (Budget budget : budgets.findByUserIdAndPeriod(user.getId(), now.getMonthValue(), now.getYear())) {
            BigDecimal spent = spending.getTotal(budget.getCategory());
            double ratio = spent.divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP).doubleValue();
            if (ratio >= .8) heap.insert(new BudgetAlertHeap.BudgetAlert(budget.getCategory().name(), spent, budget.getLimitAmount(), ratio));
        }
        return new DashboardSummary(income, expenses, net, savings,
                transactions.findRecentByUserId(user.getId(), 5), heap.drainAll(), top);
    }
}
