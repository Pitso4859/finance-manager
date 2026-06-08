package com.financemanager.patterns.factory;

import com.financemanager.dto.TransactionRequest;
import com.financemanager.model.Transaction;
import com.financemanager.model.User;

import java.time.LocalDateTime;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║              DESIGN PATTERN — Factory Method                         ║
 * ║                                                                      ║
 * ║  Problem: Transaction creation logic is scattered — DTOs need to     ║
 * ║  be validated, defaults applied, and the owner assigned.             ║
 * ║  Doing this inline in controllers couples them to entity creation.  ║
 * ║                                                                      ║
 * ║  Solution: TransactionFactory centralises creation logic.           ║
 * ║  Controllers call the factory; the factory decides how to build     ║
 * ║  the entity. This also makes testing trivial — mock the factory.    ║
 * ║                                                                      ║
 * ║  Additionally, the factory enforces business rules at object         ║
 * ║  creation time — e.g., income transactions cannot have expense       ║
 * ║  categories and vice versa.                                          ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
public class TransactionFactory {

    private TransactionFactory() {} // Utility class — prevent instantiation

    /**
     * Creates a new Transaction from a validated request DTO and the authenticated user.
     * Applies defaults and enforces category-type consistency.
     */
    public static Transaction createFromRequest(TransactionRequest request, User owner) {
        validateCategoryType(request);

        return Transaction.builder()
            .description(request.description().trim())
            .amount(request.amount())
            .type(request.type())
            .category(request.category())
            .transactionDate(request.transactionDate())
            .notes(request.notes() != null ? request.notes().trim() : null)
            .createdAt(LocalDateTime.now())
            .user(owner)
            .build();
    }

    /**
     * Creates a copy of an existing transaction with updated fields
     * (used for edit operations — preserves id and createdAt).
     */
    public static Transaction updateFromRequest(Transaction existing, TransactionRequest request) {
        validateCategoryType(request);

        existing.setDescription(request.description().trim());
        existing.setAmount(request.amount());
        existing.setType(request.type());
        existing.setCategory(request.category());
        existing.setTransactionDate(request.transactionDate());
        existing.setNotes(request.notes() != null ? request.notes().trim() : null);
        return existing;
    }

    /**
     * Business rule: income categories must not be assigned to expense transactions
     * and vice versa. Throws IllegalArgumentException at the factory level.
     */
    private static void validateCategoryType(TransactionRequest request) {
        boolean isIncomeCategory = switch (request.category()) {
            case SALARY, FREELANCE, INVESTMENT, GIFT, OTHER_INCOME -> true;
            default -> false;
        };

        boolean isExpenseType = request.type() == Transaction.TransactionType.EXPENSE;

        if (isIncomeCategory && isExpenseType) {
            throw new IllegalArgumentException(
                "Category " + request.category() + " cannot be assigned to an EXPENSE transaction.");
        }
        if (!isIncomeCategory && !isExpenseType) {
            throw new IllegalArgumentException(
                "Category " + request.category() + " cannot be assigned to an INCOME transaction.");
        }
    }
}
