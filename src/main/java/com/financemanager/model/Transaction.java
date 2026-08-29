package com.financemanager.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Transaction implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private final String id;
    private final String userId;
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private Category category;
    private LocalDate transactionDate;
    private String notes;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Transaction(String userId, String description, BigDecimal amount, TransactionType type,
                       Category category, LocalDate transactionDate, String notes) {
        this(UUID.randomUUID().toString(), userId, description, amount, type, category,
                transactionDate, notes, LocalDateTime.now(), LocalDateTime.now());
    }

    public Transaction(String id, String userId, String description, BigDecimal amount,
                       TransactionType type, Category category, LocalDate transactionDate,
                       String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.description = Objects.requireNonNull(description);
        this.amount = Objects.requireNonNull(amount);
        this.type = Objects.requireNonNull(type);
        this.category = Objects.requireNonNull(category);
        this.transactionDate = Objects.requireNonNull(transactionDate);
        this.notes = notes == null ? "" : notes;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; touch(); }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; touch(); }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; touch(); }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; touch(); }
    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; touch(); }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes == null ? "" : notes; touch(); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    private void touch() { updatedAt = LocalDateTime.now(); }
}
