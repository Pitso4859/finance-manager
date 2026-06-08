package com.financemanager.patterns.factory;

import com.financemanager.dto.TransactionRequest;
import com.financemanager.model.Transaction;
import com.financemanager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TransactionFactory (Factory Pattern)")
class TransactionFactoryTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L).fullName("Pitso Nkotolane")
            .email("pitso@test.com").password("encoded")
            .build();
    }

    @Test
    @DisplayName("Creates transaction with all fields correctly mapped")
    void createMapsFieldsCorrectly() {
        TransactionRequest req = new TransactionRequest(
            "Monthly Salary", BigDecimal.valueOf(25000),
            Transaction.TransactionType.INCOME, Transaction.Category.SALARY,
            LocalDate.of(2025, 1, 15), "January salary");

        Transaction tx = TransactionFactory.createFromRequest(req, user);

        assertThat(tx.getDescription()).isEqualTo("Monthly Salary");
        assertThat(tx.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(25000));
        assertThat(tx.getType()).isEqualTo(Transaction.TransactionType.INCOME);
        assertThat(tx.getCategory()).isEqualTo(Transaction.Category.SALARY);
        assertThat(tx.getUser()).isEqualTo(user);
        assertThat(tx.getNotes()).isEqualTo("January salary");
    }

    @Test
    @DisplayName("Trims whitespace from description and notes")
    void trimsWhitespace() {
        TransactionRequest req = new TransactionRequest(
            "  Freelance work  ", BigDecimal.valueOf(5000),
            Transaction.TransactionType.INCOME, Transaction.Category.FREELANCE,
            LocalDate.now(), "  Some notes  ");

        Transaction tx = TransactionFactory.createFromRequest(req, user);

        assertThat(tx.getDescription()).isEqualTo("Freelance work");
        assertThat(tx.getNotes()).isEqualTo("Some notes");
    }

    @Test
    @DisplayName("Throws when income category assigned to EXPENSE type")
    void throwsOnIncomeCategoryWithExpenseType() {
        TransactionRequest req = new TransactionRequest(
            "Bad entry", BigDecimal.valueOf(100),
            Transaction.TransactionType.EXPENSE, Transaction.Category.SALARY, // ← invalid combo
            LocalDate.now(), null);

        assertThatThrownBy(() -> TransactionFactory.createFromRequest(req, user))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SALARY")
            .hasMessageContaining("EXPENSE");
    }

    @Test
    @DisplayName("Throws when expense category assigned to INCOME type")
    void throwsOnExpenseCategoryWithIncomeType() {
        TransactionRequest req = new TransactionRequest(
            "Bad entry", BigDecimal.valueOf(100),
            Transaction.TransactionType.INCOME, Transaction.Category.FOOD, // ← invalid combo
            LocalDate.now(), null);

        assertThatThrownBy(() -> TransactionFactory.createFromRequest(req, user))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("FOOD")
            .hasMessageContaining("INCOME");
    }
}
