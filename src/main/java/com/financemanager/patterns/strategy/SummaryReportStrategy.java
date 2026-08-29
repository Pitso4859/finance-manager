package com.financemanager.patterns.strategy;

import com.financemanager.dto.ReportData;
import com.financemanager.model.Transaction;
import com.financemanager.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class SummaryReportStrategy implements ReportStrategy {
    @Override public String key() { return "summary"; }
    @Override public String displayName() { return "Summary"; }

    @Override
    public ReportData generate(List<Transaction> transactions, LocalDate start, LocalDate end) {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;
        Map<String, BigDecimal> categories = new TreeMap<>();
        Map<YearMonth, BigDecimal> monthly = new TreeMap<>();

        for (Transaction tx : transactions) {
            YearMonth ym = YearMonth.from(tx.getTransactionDate());
            BigDecimal signed = tx.getType() == TransactionType.INCOME ? tx.getAmount() : tx.getAmount().negate();
            monthly.merge(ym, signed, BigDecimal::add);
            if (tx.getType() == TransactionType.INCOME) {
                income = income.add(tx.getAmount());
            } else {
                expenses = expenses.add(tx.getAmount());
                categories.merge(tx.getCategory().name(), tx.getAmount(), BigDecimal::add);
            }
        }

        return new ReportData(
                "Financial Summary",
                "Summary for %s to %s".formatted(start, end),
                income,
                expenses,
                income.subtract(expenses),
                new LinkedHashMap<>(categories),
                new LinkedHashMap<>(monthly)
        );
    }
}
