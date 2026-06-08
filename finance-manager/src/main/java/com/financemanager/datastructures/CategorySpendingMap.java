package com.financemanager.datastructures;

import com.financemanager.model.Transaction;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║           CUSTOM DATA STRUCTURE — CategorySpendingMap                ║
 * ║                                                                      ║
 * ║  Hash Map built on top of Java's EnumMap for category aggregation.   ║
 * ║                                                                      ║
 * ║  Why EnumMap over HashMap<Category, ...>?                           ║
 * ║  • EnumMap uses ordinal-indexed array internally → O(1) get/put     ║
 * ║    with zero hash collision overhead.                                ║
 * ║  • Memory: array-backed, no Entry objects for each key.             ║
 * ║  • Iteration order guaranteed (enum declaration order).             ║
 * ║                                                                      ║
 * ║  Operations:                                                         ║
 * ║    accumulate  → O(1)                                                ║
 * ║    getTotal    → O(1)                                                ║
 * ║    topN        → O(k log k)  where k = number of categories         ║
 * ║    percentage  → O(1)                                                ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
public class CategorySpendingMap {

    private final EnumMap<Transaction.Category, BigDecimal> spendingMap =
        new EnumMap<>(Transaction.Category.class);

    @Getter
    private BigDecimal grandTotal = BigDecimal.ZERO;

    // ── Accumulate ────────────────────────────────────────────────────

    /** O(1) — add an amount to a category */
    public void accumulate(Transaction.Category category, BigDecimal amount) {
        spendingMap.merge(category, amount, BigDecimal::add);
        grandTotal = grandTotal.add(amount);
    }

    /** O(n) — bulk load from a list of transactions */
    public void loadFromTransactions(List<Transaction> transactions) {
        transactions.stream()
            .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
            .forEach(t -> accumulate(t.getCategory(), t.getAmount()));
    }

    // ── Query ─────────────────────────────────────────────────────────

    /** O(1) */
    public BigDecimal getTotal(Transaction.Category category) {
        return spendingMap.getOrDefault(category, BigDecimal.ZERO);
    }

    /** O(1) — spending as a % of grand total */
    public BigDecimal getPercentage(Transaction.Category category) {
        if (grandTotal.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getTotal(category)
                .multiply(BigDecimal.valueOf(100))
                .divide(grandTotal, 2, RoundingMode.HALF_UP);
    }

    /**
     * O(k log k) — top N categories by spend.
     * Used for the "Biggest Expenses" dashboard widget.
     */
    public List<Map.Entry<Transaction.Category, BigDecimal>> topN(int n) {
        return spendingMap.entrySet().stream()
            .sorted(Map.Entry.<Transaction.Category, BigDecimal>comparingByValue().reversed())
            .limit(n)
            .toList();
    }


    /** O(k) — all categories with non-zero spend */
    public Map<Transaction.Category, BigDecimal> allEntries() {
        return Collections.unmodifiableMap(spendingMap);
    }

    public boolean isEmpty() { return spendingMap.isEmpty(); }
    public int size() { return spendingMap.size(); }
}
