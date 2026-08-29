package com.financemanager.datastructures;

import com.financemanager.model.Category;
import com.financemanager.model.Transaction;
import com.financemanager.model.TransactionType;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class CategorySpendingMap {
    private final EnumMap<Category, BigDecimal> totals = new EnumMap<>(Category.class);

    public CategorySpendingMap() {
        for (Category category : Category.values()) totals.put(category, BigDecimal.ZERO);
    }

    public void loadFromTransactions(List<Transaction> transactions) {
        totals.replaceAll((category, ignored) -> BigDecimal.ZERO);
        for (Transaction tx : transactions) {
            if (tx.getType() == TransactionType.EXPENSE) {
                totals.merge(tx.getCategory(), tx.getAmount(), BigDecimal::add);
            }
        }
    }

    public BigDecimal getTotal(Category category) {
        return totals.getOrDefault(category, BigDecimal.ZERO);
    }

    public List<Map.Entry<Category, BigDecimal>> topN(int n) {
        return totals.entrySet().stream()
                .filter(entry -> entry.getValue().signum() > 0)
                .sorted(Map.Entry.<Category, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .limit(n)
                .toList();
    }
}
