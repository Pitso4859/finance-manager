package com.financemanager.patterns.strategy;

import com.financemanager.dto.ReportData;
import com.financemanager.model.Transaction;

import java.time.LocalDate;
import java.util.List;

public interface ReportStrategy {
    String key();
    String displayName();
    ReportData generate(List<Transaction> transactions, LocalDate start, LocalDate end);
}
