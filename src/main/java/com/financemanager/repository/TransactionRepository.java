package com.financemanager.repository;

import com.financemanager.model.Transaction;
import com.financemanager.model.TransactionType;
import com.financemanager.persistence.FileDataStore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class TransactionRepository {
    private final FileDataStore store;

    public TransactionRepository(FileDataStore store) {
        this.store = store;
    }

    public Transaction save(Transaction transaction) {
        return store.write(state -> {
            state.transactions().removeIf(existing -> existing.getId().equals(transaction.getId()));
            state.transactions().add(transaction);
            return transaction;
        });
    }

    public Optional<Transaction> findByIdAndUserId(String id, String userId) {
        return store.read(state -> state.transactions().stream()
                .filter(tx -> tx.getId().equals(id) && tx.getUserId().equals(userId))
                .findFirst());
    }

    public List<Transaction> findByUserId(String userId) {
        return store.read(state -> state.transactions().stream()
                .filter(tx -> tx.getUserId().equals(userId))
                .sorted(Comparator.comparing(Transaction::getTransactionDate)
                        .thenComparing(Transaction::getCreatedAt).reversed())
                .toList());
    }

    public List<Transaction> findByUserIdAndDateRange(String userId, LocalDate start, LocalDate end) {
        return store.read(state -> state.transactions().stream()
                .filter(tx -> tx.getUserId().equals(userId))
                .filter(tx -> !tx.getTransactionDate().isBefore(start) && !tx.getTransactionDate().isAfter(end))
                .sorted(Comparator.comparing(Transaction::getTransactionDate)
                        .thenComparing(Transaction::getCreatedAt).reversed())
                .toList());
    }

    public List<Transaction> findRecentByUserId(String userId, int limit) {
        List<Transaction> all = findByUserId(userId);
        return new ArrayList<>(all.subList(0, Math.min(limit, all.size())));
    }

    public BigDecimal sum(String userId, TransactionType type, LocalDate start, LocalDate end) {
        return findByUserIdAndDateRange(userId, start, end).stream()
                .filter(tx -> tx.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void deleteByIdAndUserId(String id, String userId) {
        store.write(state -> {
            state.transactions().removeIf(tx -> tx.getId().equals(id) && tx.getUserId().equals(userId));
            return null;
        });
    }
}
