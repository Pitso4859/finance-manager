package com.financemanager.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Budget implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private final String id;
    private final String userId;
    private Category category;
    private BigDecimal limitAmount;
    private int month;
    private int year;
    private final LocalDateTime createdAt;

    public Budget(String userId, Category category, BigDecimal limitAmount, int month, int year) {
        this(UUID.randomUUID().toString(), userId, category, limitAmount, month, year, LocalDateTime.now());
    }

    public Budget(String id, String userId, Category category, BigDecimal limitAmount,
                  int month, int year, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.category = Objects.requireNonNull(category);
        this.limitAmount = Objects.requireNonNull(limitAmount);
        this.month = month;
        this.year = year;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
