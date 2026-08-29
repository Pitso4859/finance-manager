package com.financemanager.datastructures;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class BudgetAlertHeap {
    public record BudgetAlert(String category, BigDecimal spent, BigDecimal limit, double ratio) {}
    private final List<BudgetAlert> heap = new ArrayList<>();

    public void insert(BudgetAlert alert) {
        heap.add(alert);
        int index = heap.size() - 1;
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (priority(heap.get(parent)) >= priority(heap.get(index))) break;
            swap(parent, index);
            index = parent;
        }
    }

    public BudgetAlert peek() {
        return heap.isEmpty() ? null : heap.get(0);
    }

    public List<BudgetAlert> drainAll() {
        List<BudgetAlert> result = new ArrayList<>();
        while (!heap.isEmpty()) result.add(removeRoot());
        return result;
    }

    public int size() { return heap.size(); }

    private BudgetAlert removeRoot() {
        BudgetAlert root = heap.get(0);
        BudgetAlert last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            int index = 0;
            while (true) {
                int left = index * 2 + 1;
                int right = index * 2 + 2;
                int largest = index;
                if (left < heap.size() && priority(heap.get(left)) > priority(heap.get(largest))) largest = left;
                if (right < heap.size() && priority(heap.get(right)) > priority(heap.get(largest))) largest = right;
                if (largest == index) break;
                swap(index, largest);
                index = largest;
            }
        }
        return root;
    }

    private double priority(BudgetAlert alert) { return alert.ratio(); }
    private void swap(int a, int b) {
        BudgetAlert temp = heap.get(a);
        heap.set(a, heap.get(b));
        heap.set(b, temp);
    }
}
