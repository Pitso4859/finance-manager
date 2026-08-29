package com.financemanager.service;

import com.financemanager.dto.ReportData;
import com.financemanager.model.User;
import com.financemanager.patterns.strategy.ReportStrategy;
import com.financemanager.repository.TransactionRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

public final class ReportService {
    private final TransactionRepository transactions;
    private final Map<String, ReportStrategy> strategies = new LinkedHashMap<>();
    public ReportService(TransactionRepository transactions, List<ReportStrategy> strategies) {
        this.transactions = transactions; strategies.forEach(s -> this.strategies.put(s.key(), s));
    }
    public List<ReportStrategy> strategies() { return List.copyOf(strategies.values()); }
    public ReportData generate(User user, String key, LocalDate start, LocalDate end) {
        if (start.isAfter(end)) throw new com.financemanager.exception.ValidationException("Start date must be before or equal to end date.");
        ReportStrategy strategy = Optional.ofNullable(strategies.get(key)).orElseThrow(() -> new IllegalArgumentException("Unknown report type."));
        return strategy.generate(transactions.findByUserIdAndDateRange(user.getId(), start, end), start, end);
    }
    public void exportCsv(ReportData report, Path file) throws IOException {
        StringBuilder csv = new StringBuilder("Metric,Amount\n");
        append(csv, "Income", report.income()); append(csv, "Expenses", report.expenses()); append(csv, "Net", report.net());
        csv.append("\nCategory,Expense\n"); report.categoryExpenses().forEach((k,v) -> append(csv, k, v));
        csv.append("\nMonth,Net\n"); report.monthlyNet().forEach((k,v) -> append(csv, k.toString(), v));
        Files.writeString(file, csv.toString(), StandardCharsets.UTF_8);
    }
    private void append(StringBuilder csv, String key, BigDecimal value) {
        csv.append('"').append(key.replace("\"", "\"\"")).append("\",").append(value.toPlainString()).append('\n');
    }
}
