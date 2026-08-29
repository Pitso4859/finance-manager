package com.financemanager.patterns.strategy;

import com.financemanager.dto.ReportData;
import com.financemanager.model.Transaction;
import com.financemanager.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class ExpenseReportStrategy implements ReportStrategy {
    @Override public String key() { return "expenses"; }
    @Override public String displayName() { return "Expense Analysis"; }

    @Override
    public ReportData generate(List<Transaction> transactions, LocalDate start, LocalDate end) {
        BigDecimal expenses = BigDecimal.ZERO;
        Map<String, BigDecimal> rawCategories = new TreeMap<>();
        Map<YearMonth, BigDecimal> monthlyExpense = new TreeMap<>();

        for (Transaction tx : transactions) {
            if (tx.getType() != TransactionType.EXPENSE) continue;
            expenses = expenses.add(tx.getAmount());
            rawCategories.merge(tx.getCategory().name(), tx.getAmount(), BigDecimal::add);
            monthlyExpense.merge(YearMonth.from(tx.getTransactionDate()), tx.getAmount(), BigDecimal::add);
        }

        Map<String, BigDecimal> categories = new LinkedHashMap<>();
        rawCategories.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> categories.put(entry.getKey(), entry.getValue()));

        Map<YearMonth, BigDecimal> monthly = new LinkedHashMap<>();
        monthlyExpense.forEach((month, value) -> monthly.put(month, value.negate()));

        return new ReportData(
                "Expense Analysis",
                "Expense-only analysis for %s to %s".formatted(start, end),
                BigDecimal.ZERO,
                expenses,
                expenses.negate(),
                categories,
                monthly
        );
    }
}
