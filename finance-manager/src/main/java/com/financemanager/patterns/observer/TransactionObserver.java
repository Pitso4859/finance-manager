package com.financemanager.patterns.observer;

/**
 * Observer interface — any class that wants to react to transaction events
 * implements this single-method contract.
 */
public interface TransactionObserver {
    void onTransactionEvent(TransactionEvent event);
}
