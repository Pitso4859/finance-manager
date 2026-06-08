package com.financemanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction entity — represents any financial movement (income or expense).
 *
 * Design decisions:
 * - BigDecimal for money: never use float/double for currency (precision loss).
 * - Enum for type/category: prevents invalid data at the model level.
 * - Soft audit fields (createdAt) for traceability.
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_tx_user_date", columnList = "user_id, transaction_date"),
    @Index(name = "idx_tx_category", columnList = "category")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String description;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type; // INCOME or EXPENSE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @NotNull
    @Column(nullable = false)
    private LocalDate transactionDate;

    private String notes;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ── Enums ────────────────────────────────────────────────────────────────

    public enum TransactionType { INCOME, EXPENSE }

    public enum Category {
        // Income categories
        SALARY, FREELANCE, INVESTMENT, GIFT, OTHER_INCOME,
        // Expense categories
        FOOD, TRANSPORT, HOUSING, UTILITIES, HEALTHCARE,
        EDUCATION, ENTERTAINMENT, CLOTHING, SAVINGS, OTHER_EXPENSE
    }
}
