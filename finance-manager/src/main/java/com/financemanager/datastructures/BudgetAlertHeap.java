package com.financemanager.datastructures;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║              CUSTOM DATA STRUCTURE — BudgetAlertHeap                 ║
 * ║                                                                      ║
 * ║  A Min-Heap that surfaces the most over-budget categories first.     ║
 * ║                                                                      ║
 * ║  Why a Min-Heap?                                                     ║
 * ║  • We define "priority" as (spent / limit) — the overspend ratio.   ║
 * ║  • Min-Heap on INVERSE ratio → categories closest to/over limit     ║
 * ║    bubble to the top.                                                ║
 * ║  • O(log n) insert, O(log n) extract, O(1) peek.                    ║
 * ║  • Beats sorting all categories every time: O(n log n) → O(log n). ║
 * ║                                                                      ║
 * ║  Array-backed heap: parent(i) = (i-1)/2,                            ║
 * ║                      left(i) = 2i+1, right(i) = 2i+2               ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
public class BudgetAlertHeap {

    public record BudgetAlert(
        String category,
        BigDecimal spent,
        BigDecimal limit,
        double usageRatio   // spent / limit  (>1.0 = over budget)
    ) implements Comparable<BudgetAlert> {

        /** Higher ratio = higher urgency = smaller value in min-heap */
        @Override
        public int compareTo(BudgetAlert other) {
            return Double.compare(other.usageRatio, this.usageRatio); // reverse
        }

        public boolean isOverBudget() { return usageRatio > 1.0; }

        public boolean isWarning() { return usageRatio >= 0.8 && usageRatio <= 1.0; }
    }

    private final List<BudgetAlert> heap = new ArrayList<>();

    // ── Insert ────────────────────────────────────────────────────────

    /** O(log n) */
    public void insert(BudgetAlert alert) {
        heap.add(alert);
        siftUp(heap.size() - 1);
    }

    // ── Peek & Extract ────────────────────────────────────────────────

    /** O(1) — most urgent alert */
    public BudgetAlert peek() {
        if (heap.isEmpty()) return null;
        return heap.get(0);
    }

    /** O(log n) — remove and return most urgent */
    public BudgetAlert extractTop() {
        if (heap.isEmpty()) return null;
        BudgetAlert top = heap.get(0);
        int last = heap.size() - 1;
        heap.set(0, heap.get(last));
        heap.remove(last);
        if (!heap.isEmpty()) siftDown(0);
        return top;
    }

    /** O(n log n) — drain all alerts sorted by urgency */
    public List<BudgetAlert> drainAll() {
        List<BudgetAlert> sorted = new ArrayList<>();
        while (!heap.isEmpty()) sorted.add(extractTop());
        return sorted;
    }

    public int size() { return heap.size(); }
    public boolean isEmpty() { return heap.isEmpty(); }

    // ── Heap Operations ───────────────────────────────────────────────

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (heap.get(i).compareTo(heap.get(parent)) < 0) {
                swap(i, parent);
                i = parent;
            } else break;
        }
    }

    private void siftDown(int i) {
        int size = heap.size();
        while (true) {
            int left = 2 * i + 1, right = 2 * i + 2, smallest = i;
            if (left < size && heap.get(left).compareTo(heap.get(smallest)) < 0)
                smallest = left;
            if (right < size && heap.get(right).compareTo(heap.get(smallest)) < 0)
                smallest = right;
            if (smallest != i) { swap(i, smallest); i = smallest; }
            else break;
        }
    }

    private void swap(int a, int b) {
        BudgetAlert tmp = heap.get(a);
        heap.set(a, heap.get(b));
        heap.set(b, tmp);
    }
}
