package com.financemanager.repository;

import com.financemanager.model.Budget;
import com.financemanager.persistence.FileDataStore;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class BudgetRepository {
    private final FileDataStore store;

    public BudgetRepository(FileDataStore store) {
        this.store = store;
    }

    public Budget save(Budget budget) {
        return store.write(state -> {
            state.budgets().removeIf(existing -> existing.getId().equals(budget.getId()));
            state.budgets().add(budget);
            return budget;
        });
    }

    public Optional<Budget> findByIdAndUserId(String id, String userId) {
        return store.read(state -> state.budgets().stream()
                .filter(b -> b.getId().equals(id) && b.getUserId().equals(userId))
                .findFirst());
    }

    public List<Budget> findByUserId(String userId) {
        return store.read(state -> state.budgets().stream()
                .filter(b -> b.getUserId().equals(userId))
                .sorted(Comparator.comparingInt(Budget::getYear).reversed()
                        .thenComparing(Comparator.comparingInt(Budget::getMonth).reversed())
                        .thenComparing(b -> b.getCategory().name()))
                .toList());
    }

    public List<Budget> findByUserIdAndPeriod(String userId, int month, int year) {
        return store.read(state -> state.budgets().stream()
                .filter(b -> b.getUserId().equals(userId) && b.getMonth() == month && b.getYear() == year)
                .sorted(Comparator.comparing(b -> b.getCategory().name()))
                .toList());
    }

    public boolean existsForCategoryAndPeriod(String userId, String excludingId,
                                              com.financemanager.model.Category category,
                                              int month, int year) {
        return store.read(state -> state.budgets().stream()
                .anyMatch(b -> b.getUserId().equals(userId)
                        && !b.getId().equals(excludingId == null ? "" : excludingId)
                        && b.getCategory() == category
                        && b.getMonth() == month
                        && b.getYear() == year));
    }

    public void deleteByIdAndUserId(String id, String userId) {
        store.write(state -> {
            state.budgets().removeIf(b -> b.getId().equals(id) && b.getUserId().equals(userId));
            return null;
        });
    }
}
