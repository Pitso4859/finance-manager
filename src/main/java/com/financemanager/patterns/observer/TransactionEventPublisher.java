package com.financemanager.patterns.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class TransactionEventPublisher {
    private final List<TransactionObserver> observers = new CopyOnWriteArrayList<>();

    public void subscribe(TransactionObserver observer) {
        observers.add(observer);
    }

    public void publish(TransactionEvent event) {
        for (TransactionObserver observer : observers) {
            try {
                observer.onTransactionEvent(event);
            } catch (RuntimeException ignored) {
                // A side-effect observer must never break the core transaction operation.
            }
        }
    }
}
