package com.financemanager.datastructures;

import com.financemanager.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for TransactionLedger (doubly linked list).
 * Verifies correctness of all O(1) and O(n) operations.
 */
@DisplayName("TransactionLedger (Doubly Linked List)")
class TransactionLedgerTest {

    private TransactionLedger ledger;

    @BeforeEach
    void setUp() { ledger = new TransactionLedger(); }

    // ── Helper ────────────────────────────────────────────────────────

    private Transaction tx(Long id, String desc, BigDecimal amount, Transaction.TransactionType type) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setDescription(desc);
        t.setAmount(amount);
        t.setType(type);
        t.setCategory(type == Transaction.TransactionType.INCOME
            ? Transaction.Category.SALARY : Transaction.Category.FOOD);
        t.setTransactionDate(LocalDate.now());
        return t;
    }

    // ── Size & Empty ──────────────────────────────────────────────────

    @Test
    @DisplayName("New ledger is empty")
    void newLedgerIsEmpty() {
        assertThat(ledger.isEmpty()).isTrue();
        assertThat(ledger.getSize()).isZero();
    }

    @Test
    @DisplayName("addFirst increases size by 1")
    void addFirstIncrementsSize() {
        ledger.addFirst(tx(1L, "Salary", BigDecimal.valueOf(5000), Transaction.TransactionType.INCOME));
        assertThat(ledger.getSize()).isEqualTo(1);
        assertThat(ledger.isEmpty()).isFalse();
    }

    // ── Order ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("addFirst puts node at head — newest first")
    void addFirstMaintainsHeadOrder() {
        ledger.addFirst(tx(1L, "First",  BigDecimal.ONE, Transaction.TransactionType.EXPENSE));
        ledger.addFirst(tx(2L, "Second", BigDecimal.ONE, Transaction.TransactionType.EXPENSE));
        assertThat(ledger.peekFirst().getDescription()).isEqualTo("Second");
    }

    @Test
    @DisplayName("addLast puts node at tail")
    void addLastMaintainsTailOrder() {
        ledger.addLast(tx(1L, "First",  BigDecimal.ONE, Transaction.TransactionType.EXPENSE));
        ledger.addLast(tx(2L, "Second", BigDecimal.ONE, Transaction.TransactionType.EXPENSE));
        assertThat(ledger.peekLast().getDescription()).isEqualTo("Second");
    }

    // ── Balance Computation ───────────────────────────────────────────

    @Test
    @DisplayName("computeBalance correctly sums income minus expenses")
    void computeBalanceIsCorrect() {
        ledger.addLast(tx(1L, "Salary",  BigDecimal.valueOf(10000), Transaction.TransactionType.INCOME));
        ledger.addLast(tx(2L, "Rent",    BigDecimal.valueOf(4000),  Transaction.TransactionType.EXPENSE));
        ledger.addLast(tx(3L, "Groceries", BigDecimal.valueOf(1500), Transaction.TransactionType.EXPENSE));

        // 10000 - 4000 - 1500 = 4500
        assertThat(ledger.computeBalance()).isEqualByComparingTo(BigDecimal.valueOf(4500));
    }

    @Test
    @DisplayName("computeBalance on empty ledger returns ZERO")
    void computeBalanceOnEmpty() {
        assertThat(ledger.computeBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ── Remove ────────────────────────────────────────────────────────

    @Test
    @DisplayName("removeById removes the correct node and decrements size")
    void removeByIdWorks() {
        ledger.addLast(tx(1L, "A", BigDecimal.ONE, Transaction.TransactionType.EXPENSE));
        ledger.addLast(tx(2L, "B", BigDecimal.ONE, Transaction.TransactionType.EXPENSE));
        ledger.addLast(tx(3L, "C", BigDecimal.ONE, Transaction.TransactionType.EXPENSE));

        boolean removed = ledger.removeById(2L);
        assertThat(removed).isTrue();
        assertThat(ledger.getSize()).isEqualTo(2);

        // Verify forward traversal skips removed node
        List<String> remaining = new ArrayList<>();
        for (Transaction t : ledger) remaining.add(t.getDescription());
        assertThat(remaining).containsExactly("A", "C");
    }

    @Test
    @DisplayName("removeById returns false for non-existent id")
    void removeByIdReturnsFalseWhenNotFound() {
        ledger.addLast(tx(1L, "A", BigDecimal.ONE, Transaction.TransactionType.EXPENSE));
        assertThat(ledger.removeById(999L)).isFalse();
        assertThat(ledger.getSize()).isEqualTo(1);
    }

    // ── Reverse Iteration ─────────────────────────────────────────────

    @Test
    @DisplayName("reverseIterable traverses newest to oldest")
    void reverseIterableIsCorrect() {
        ledger.addLast(tx(1L, "Oldest", BigDecimal.ONE, Transaction.TransactionType.EXPENSE));
        ledger.addLast(tx(2L, "Middle", BigDecimal.ONE, Transaction.TransactionType.EXPENSE));
        ledger.addLast(tx(3L, "Newest", BigDecimal.ONE, Transaction.TransactionType.EXPENSE));

        List<String> reversed = new ArrayList<>();
        for (Transaction t : ledger.reverseIterable()) reversed.add(t.getDescription());

        assertThat(reversed).containsExactly("Newest", "Middle", "Oldest");
    }
}
