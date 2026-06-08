package com.financemanager.service.impl;

import com.financemanager.dto.TransactionRequest;
import com.financemanager.exception.ResourceNotFoundException;
import com.financemanager.model.Transaction;
import com.financemanager.model.User;
import com.financemanager.patterns.factory.TransactionFactory;
import com.financemanager.patterns.observer.TransactionEvent;
import com.financemanager.patterns.observer.TransactionEventPublisher;
import com.financemanager.repository.TransactionRepository;
import com.financemanager.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * TransactionService implementation.
 *
 * SE Practices demonstrated:
 * - @Transactional on write operations (atomicity)
 * - Ownership validation before mutations (security)
 * - Factory pattern for object creation
 * - Observer pattern for side effects (budget alerts, audit)
 * - Read-only transactions for performance
 * - SRP: this class only coordinates persistence + events
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher eventPublisher;

    @Override
    @Transactional
    public Transaction save(Transaction transaction, User user) {
        transaction.setUser(user);
        Transaction saved = transactionRepository.save(transaction);
        eventPublisher.publish(TransactionEvent.created(saved));
        return saved;
    }

    @Override
    @Transactional
    public Transaction create(TransactionRequest request, User user) {
        // Factory handles creation logic and category-type validation
        Transaction transaction = TransactionFactory.createFromRequest(request, user);
        Transaction saved = transactionRepository.save(transaction);
        eventPublisher.publish(TransactionEvent.created(saved));
        log.info("Created transaction id={} for user={}", saved.getId(), user.getEmail());
        return saved;
    }

    @Override
    @Transactional
    public Transaction update(Long id, TransactionRequest request, User user) {
        // Ownership check: user can only edit their own transactions
        Transaction existing = findByIdAndUser(id, user);
        TransactionFactory.updateFromRequest(existing, request);
        Transaction updated = transactionRepository.save(existing);
        eventPublisher.publish(TransactionEvent.updated(updated));
        return updated;
    }

    @Override
    @Transactional
    public void delete(Long id, User user) {
        Transaction transaction = findByIdAndUser(id, user);
        transactionRepository.delete(transaction);
        eventPublisher.publish(TransactionEvent.deleted(transaction));
        log.info("Deleted transaction id={} for user={}", id, user.getEmail());
    }

    @Override
    public Transaction findByIdAndUser(Long id, User user) {
        return transactionRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Transaction not found or access denied: id=" + id));
    }

    @Override
    public Page<Transaction> findPagedByUser(User user, Pageable pageable) {
        return transactionRepository.findByUserOrderByTransactionDateDesc(user, pageable);
    }

    @Override
    public List<Transaction> findByUserAndDateRange(User user, LocalDate start, LocalDate end) {
        return transactionRepository
            .findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(user, start, end);
    }

    @Override
    public BigDecimal sumByUserTypeAndPeriod(User user, Transaction.TransactionType type,
                                              LocalDate start, LocalDate end) {
        BigDecimal result = transactionRepository.sumByUserAndTypeAndDateRange(user, type, start, end);
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    public List<Transaction> findRecentByUser(User user) {
        return transactionRepository.findTop5ByUserOrderByTransactionDateDesc(user);
    }
}
