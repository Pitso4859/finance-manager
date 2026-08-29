package com.financemanager.patterns.observer;

import com.financemanager.model.Transaction;

import java.time.LocalDateTime;

public record TransactionEvent(Type type, Transaction transaction, LocalDateTime occurredAt) {
    public enum Type { CREATED, UPDATED, DELETED }

    public static TransactionEvent of(Type type, Transaction transaction) {
        return new TransactionEvent(type, transaction, LocalDateTime.now());
    }
}
