package com.financemanager.service;

import com.financemanager.exception.ResourceNotFoundException;
import com.financemanager.model.*;
import com.financemanager.patterns.observer.TransactionEvent;
import com.financemanager.patterns.observer.TransactionEventPublisher;
import com.financemanager.repository.TransactionRepository;
import com.financemanager.util.Validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class TransactionService {
    private final TransactionRepository repository;
    private final TransactionEventPublisher publisher;

    public TransactionService(TransactionRepository repository, TransactionEventPublisher publisher) {
        this.repository = repository; this.publisher = publisher;
    }

    public Transaction create(User user, String description, String amount, TransactionType type,
                              Category category, String date, String notes) {
        String cleanDescription = Validation.requiredText(description, "Description", 120);
        BigDecimal cleanAmount = Validation.positiveAmount(amount, "Amount");
        LocalDate cleanDate = Validation.date(date, "Transaction date");
        validateCategory(type, category);
        Transaction tx = new Transaction(user.getId(), cleanDescription, cleanAmount, type, category,
                cleanDate, notes == null ? "" : notes.trim());
        repository.save(tx);
        publisher.publish(TransactionEvent.of(TransactionEvent.Type.CREATED, tx));
        return tx;
    }

    public Transaction update(User user, String id, String description, String amount, TransactionType type,
                              Category category, String date, String notes) {
        Transaction tx = get(user, id);
        tx.setDescription(Validation.requiredText(description, "Description", 120));
        tx.setAmount(Validation.positiveAmount(amount, "Amount"));
        tx.setType(type);
        validateCategory(type, category);
        tx.setCategory(category);
        tx.setTransactionDate(Validation.date(date, "Transaction date"));
        tx.setNotes(notes == null ? "" : notes.trim());
        repository.save(tx);
        publisher.publish(TransactionEvent.of(TransactionEvent.Type.UPDATED, tx));
        return tx;
    }

    public void delete(User user, String id) {
        Transaction tx = get(user, id);
        repository.deleteByIdAndUserId(id, user.getId());
        publisher.publish(TransactionEvent.of(TransactionEvent.Type.DELETED, tx));
    }

    public Transaction get(User user, String id) {
        return repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction was not found."));
    }

    public List<Transaction> list(User user) { return repository.findByUserId(user.getId()); }
    public List<Transaction> list(User user, LocalDate start, LocalDate end) {
        return repository.findByUserIdAndDateRange(user.getId(), start, end);
    }

    private void validateCategory(TransactionType type, Category category) {
        if (type == TransactionType.INCOME && !category.isIncomeCategory()) {
            throw new com.financemanager.exception.ValidationException("Select Salary, Freelance, Investment, or Other for income.");
        }
        if (type == TransactionType.EXPENSE && category != Category.OTHER && category.isIncomeCategory()) {
            throw new com.financemanager.exception.ValidationException("Select an expense category for an expense transaction.");
        }
    }
}
