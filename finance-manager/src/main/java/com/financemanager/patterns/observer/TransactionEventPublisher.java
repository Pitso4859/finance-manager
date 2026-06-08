package com.financemanager.patterns.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The Subject in the Observer pattern.
 * Maintains a list of observers and notifies them when events occur.
 * Spring manages this as a singleton bean — all observers register at startup.
 */
@Component
@Slf4j
public class TransactionEventPublisher {

    private final List<TransactionObserver> observers = new ArrayList<>();

    public void subscribe(TransactionObserver observer) {
        observers.add(observer);
        log.info("Observer registered: {}", observer.getClass().getSimpleName());
    }

    public void unsubscribe(TransactionObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all registered observers.
     * Each observer handles the event independently — failure in one
     * does not block others (defensive try-catch per observer).
     */
    public void publish(TransactionEvent event) {
        log.debug("Publishing event: {} for transaction id={}",
            event.eventType(), event.transaction().getId());

        for (TransactionObserver observer : observers) {
            try {
                observer.onTransactionEvent(event);
            } catch (Exception e) {
                log.error("Observer {} failed on event {}: {}",
                    observer.getClass().getSimpleName(), event.eventType(), e.getMessage());
            }
        }
    }
}
