package com.financemanager.repository;

import com.financemanager.model.Transaction;
import com.financemanager.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Basic ownership check — used in service before any operation
    Optional<Transaction> findByIdAndUser(Long id, User user);

    // Paginated list for the transactions table
    Page<Transaction> findByUserOrderByTransactionDateDesc(User user, Pageable pageable);

    // All transactions for a date range
    List<Transaction> findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(
        User user, LocalDate start, LocalDate end);

    // By type (INCOME / EXPENSE)
    List<Transaction> findByUserAndTypeOrderByTransactionDateDesc(
        User user, Transaction.TransactionType type);

    // Sum for dashboard totals
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.type = :type " +
           "AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumByUserAndTypeAndDateRange(
        @Param("user") User user,
        @Param("type") Transaction.TransactionType type,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    // For budget alert checking
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.type = 'EXPENSE' AND t.category = :category " +
           "AND MONTH(t.transactionDate) = :month AND YEAR(t.transactionDate) = :year")
    BigDecimal sumExpensesByCategoryAndMonthAndYear(
        @Param("user") User user,
        @Param("category") Transaction.Category category,
        @Param("month") int month,
        @Param("year") int year
    );

    // Recent transactions for dashboard widget
    List<Transaction> findTop5ByUserOrderByTransactionDateDesc(User user);
}
