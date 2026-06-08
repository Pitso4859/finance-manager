package com.financemanager.service;

import com.financemanager.dto.TransactionRequest;
import com.financemanager.model.Transaction;
import com.financemanager.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * TransactionService interface — defines the contract.
 *
 * SE Practice: Programming to an interface (Dependency Inversion Principle).
 * Controllers depend on this interface, not the concrete implementation.
 * This makes it trivial to swap implementations or mock in tests.
 */
public interface TransactionService {

    Transaction save(Transaction transaction, User user);

    Transaction create(TransactionRequest request, User user);

    Transaction update(Long id, TransactionRequest request, User user);

    void delete(Long id, User user);

    Transaction findByIdAndUser(Long id, User user);

    Page<Transaction> findPagedByUser(User user, Pageable pageable);

    List<Transaction> findByUserAndDateRange(User user, LocalDate start, LocalDate end);

    BigDecimal sumByUserTypeAndPeriod(User user, Transaction.TransactionType type,
                                      LocalDate start, LocalDate end);

    List<Transaction> findRecentByUser(User user);
}
