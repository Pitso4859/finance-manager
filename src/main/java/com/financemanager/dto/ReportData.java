package com.financemanager.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

public record ReportData(
        String title,
        String description,
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal net,
        Map<String, BigDecimal> categoryExpenses,
        Map<YearMonth, BigDecimal> monthlyNet
) {
    public ReportData {
        categoryExpenses = new LinkedHashMap<>(categoryExpenses);
        monthlyNet = new LinkedHashMap<>(monthlyNet);
    }
}
