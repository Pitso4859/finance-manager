package com.financemanager.patterns.observer;

@FunctionalInterface
public interface TransactionObserver {
    void onTransactionEvent(TransactionEvent event);
}
