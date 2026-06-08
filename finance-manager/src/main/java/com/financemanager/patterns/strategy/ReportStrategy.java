package com.financemanager.patterns.strategy;

import com.financemanager.model.Transaction;

import java.util.List;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║              DESIGN PATTERN — Strategy                               ║
 * ║                                                                      ║
 * ║  Problem: The system needs to generate reports in different formats  ║
 * ║  (monthly summary, category breakdown, yearly trend). Each format    ║
 * ║  has different aggregation logic.                                    ║
 * ║                                                                      ║
 * ║  Without Strategy: a giant if/else or switch in ReportService.      ║
 * ║  With Strategy: each algorithm is encapsulated in its own class.    ║
 * ║  ReportService programs to the interface — open for extension,       ║
 * ║  closed for modification (OCP).                                     ║
 * ║                                                                      ║
 * ║  Concrete strategies:                                                ║
 * ║    MonthlySummaryStrategy   — income vs expense per month           ║
 * ║    CategoryBreakdownStrategy — spending % by category               ║
 * ║    TrendAnalysisStrategy    — 6-month moving average                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */

public interface ReportStrategy {

    /** Human-readable name shown in the UI dropdown */
    String getName();

    /**
     * Execute the report algorithm.
     *
     * @param transactions raw transaction data for the period
     * @return a result containing title, data, and chart type
     */
    ReportResult generate(List<Transaction> transactions);

    /** Immutable result container */
    record ReportResult(
            String title,
            Map<String, Object> data,   // labels, datasets etc. for Chart.js
            String chartType            // "bar", "pie", "line"
    ) {}
}
