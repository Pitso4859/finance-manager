package com.financemanager.datastructures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for BudgetAlertHeap (min-heap).
 * Verifies heap property and priority ordering.
 */
@DisplayName("BudgetAlertHeap (Min-Heap)")
class BudgetAlertHeapTest {

    private BudgetAlertHeap heap;

    @BeforeEach
    void setUp() { heap = new BudgetAlertHeap(); }

    private BudgetAlertHeap.BudgetAlert alert(String cat, double ratio) {
        BigDecimal limit = BigDecimal.valueOf(1000);
        BigDecimal spent = BigDecimal.valueOf(ratio * 1000);
        return new BudgetAlertHeap.BudgetAlert(cat, spent, limit, ratio);
    }

    @Test
    @DisplayName("Empty heap returns null on peek")
    void emptyHeapPeekIsNull() {
        assertThat(heap.peek()).isNull();
    }

    @Test
    @DisplayName("Most over-budget category surfaces first")
    void mostOverBudgetSurfacesFirst() {
        heap.insert(alert("FOOD", 0.85));
        heap.insert(alert("TRANSPORT", 1.20));  // most over
        heap.insert(alert("UTILITIES", 0.95));

        assertThat(heap.peek().category()).isEqualTo("TRANSPORT");
    }

    @Test
    @DisplayName("drainAll returns alerts sorted by urgency descending")
    void drainAllSortsByUrgency() {
        heap.insert(alert("FOOD", 0.82));
        heap.insert(alert("TRANSPORT", 1.15));
        heap.insert(alert("ENTERTAINMENT", 0.91));
        heap.insert(alert("HOUSING", 1.30));  // highest

        List<BudgetAlertHeap.BudgetAlert> sorted = heap.drainAll();

        assertThat(sorted).hasSize(4);
        assertThat(sorted.get(0).category()).isEqualTo("HOUSING");
        assertThat(sorted.get(1).category()).isEqualTo("TRANSPORT");
        // Heap is now empty
        assertThat(heap.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("isOverBudget is true when ratio > 1.0")
    void isOverBudgetFlag() {
        BudgetAlertHeap.BudgetAlert over = alert("FOOD", 1.05);
        BudgetAlertHeap.BudgetAlert under = alert("FOOD", 0.99);
        assertThat(over.isOverBudget()).isTrue();
        assertThat(under.isOverBudget()).isFalse();
    }

    @Test
    @DisplayName("isWarning is true when ratio is 80%-100%")
    void isWarningFlag() {
        assertThat(alert("FOOD", 0.80).isWarning()).isTrue();
        assertThat(alert("FOOD", 0.95).isWarning()).isTrue();
        assertThat(alert("FOOD", 1.01).isWarning()).isFalse();
        assertThat(alert("FOOD", 0.79).isWarning()).isFalse();
    }
}
