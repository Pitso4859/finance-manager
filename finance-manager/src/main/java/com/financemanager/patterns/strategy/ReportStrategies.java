package com.financemanager.patterns.strategy;

import com.financemanager.datastructures.CategorySpendingMap;
import com.financemanager.model.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

// ─────────────────────────────────────────────────────────────────────────────
//  Strategy 1: Monthly Income vs Expense Summary
// ─────────────────────────────────────────────────────────────────────────────

@Component("monthlySummaryStrategy")
class MonthlySummaryStrategy implements ReportStrategy {

    @Override
    public String getName() {
        return "Monthly Summary";
    }

    @Override
    public ReportResult generate(List<Transaction> transactions) {
        // Group by month, split by type
        Map<String, BigDecimal> incomeByMonth = new LinkedHashMap<>();
        Map<String, BigDecimal> expenseByMonth = new LinkedHashMap<>();

        transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDate))
                .forEach(t -> {
                    String monthKey = t.getTransactionDate().getMonth()
                            .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                            + " " + t.getTransactionDate().getYear();

                    if (t.getType() == Transaction.TransactionType.INCOME) {
                        incomeByMonth.merge(monthKey, t.getAmount(), BigDecimal::add);
                    } else {
                        expenseByMonth.merge(monthKey, t.getAmount(), BigDecimal::add);
                    }
                });

        List<String> labels = new ArrayList<>(new LinkedHashSet<>(incomeByMonth.keySet()));
        labels.addAll(expenseByMonth.keySet().stream()
                .filter(k -> !labels.contains(k)).toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("labels", labels);
        data.put("income", labels.stream().map(l -> incomeByMonth.getOrDefault(l, BigDecimal.ZERO)).toList());
        data.put("expense", labels.stream().map(l -> expenseByMonth.getOrDefault(l, BigDecimal.ZERO)).toList());

        return new ReportResult("Monthly Income vs Expense", data, "bar");
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Strategy 2: Category Breakdown (uses CategorySpendingMap data structure)
// ─────────────────────────────────────────────────────────────────────────────

@Component("categoryBreakdownStrategy")
class CategoryBreakdownStrategy implements ReportStrategy {

    @Override
    public String getName() {
        return "Category Breakdown";
    }

    @Override
    public ReportResult generate(List<Transaction> transactions) {
        // Use our custom O(1) EnumMap-backed data structure
        CategorySpendingMap spendingMap = new CategorySpendingMap();
        spendingMap.loadFromTransactions(transactions);

        List<Map.Entry<Transaction.Category, BigDecimal>> top = spendingMap.topN(10);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("labels", top.stream().map(e -> e.getKey().name()).toList());
        data.put("amounts", top.stream().map(Map.Entry::getValue).toList());
        data.put("percentages", top.stream()
                .map(e -> spendingMap.getPercentage(e.getKey()))
                .toList());

        return new ReportResult("Spending by Category", data, "pie");
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Strategy 3: 3-Month Rolling Average Trend
// ─────────────────────────────────────────────────────────────────────────────

@Component("trendAnalysisStrategy")
class TrendAnalysisStrategy implements ReportStrategy {

    @Override
    public String getName() {
        return "Spending Trend";
    }

    @Override
    public ReportResult generate(List<Transaction> transactions) {
        // Group expenses by month
        Map<String, BigDecimal> monthlyExpense = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .sorted(Comparator.comparing(Transaction::getTransactionDate))
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionDate().getYear() + "-"
                                + String.format("%02d", t.getTransactionDate().getMonthValue()),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        List<String> months = new ArrayList<>(monthlyExpense.keySet());
        List<BigDecimal> amounts = new ArrayList<>(monthlyExpense.values());

        // Compute 3-month rolling average
        List<BigDecimal> rollingAvg = new ArrayList<>();
        for (int i = 0; i < amounts.size(); i++) {
            int start = Math.max(0, i - 2);
            BigDecimal sum = BigDecimal.ZERO;
            for (int j = start; j <= i; j++) sum = sum.add(amounts.get(j));
            rollingAvg.add(sum.divide(BigDecimal.valueOf(i - start + 1), 2, java.math.RoundingMode.HALF_UP));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("labels", months);
        data.put("actual", amounts);
        data.put("rollingAvg", rollingAvg);

        return new ReportResult("3-Month Rolling Expense Trend", data, "line");
    }
}