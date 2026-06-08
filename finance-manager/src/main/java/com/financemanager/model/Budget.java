package com.financemanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;

/**
 * Budget entity — a monthly spending limit per category.
 *
 * A user sets a budget (e.g. R3000 for FOOD in January 2025).
 * The system tracks actual spending and alerts when thresholds are exceeded.
 */
@Entity
@Table(name = "budgets",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_budget_user_category_month_year",
        columnNames = {"user_id", "category", "month", "year"}
    )
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Transaction.Category category;

    @NotNull
    @DecimalMin("1.00")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal limitAmount;

    @NotNull
    @Column(nullable = false)
    private Integer month; // 1-12

    @NotNull
    @Column(nullable = false)
    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
