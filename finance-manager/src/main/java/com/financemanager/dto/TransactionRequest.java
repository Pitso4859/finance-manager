package com.financemanager.dto;

import com.financemanager.model.Transaction;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * TransactionRequest DTO — separates the API contract from the domain model.
 * Using Java records for immutability and conciseness.
 *
 * SE Practice: DTOs prevent over-posting attacks and decouple the persistence
 * model from the presentation layer.
 */
public record TransactionRequest(

    @NotBlank(message = "Description is required")
    @Size(min = 2, max = 255, message = "Description must be 2-255 characters")
    String description,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Invalid amount format")
    BigDecimal amount,

    @NotNull(message = "Transaction type is required")
    Transaction.TransactionType type,

    @NotNull(message = "Category is required")
    Transaction.Category category,

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    LocalDate transactionDate,

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    String notes

) {}
