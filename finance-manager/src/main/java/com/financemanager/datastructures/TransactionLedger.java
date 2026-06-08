package com.financemanager.datastructures;

import com.financemanager.model.Transaction;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║              CUSTOM DATA STRUCTURE — TransactionLedger               ║
 * ║                                                                      ║
 * ║  A doubly-linked list purpose-built for financial transaction        ║
 * ║  history. Chosen because:                                            ║
 * ║                                                                      ║
 * ║  • O(1) prepend (newest transaction always at head)                  ║
 * ║  • O(1) append (for batch imports)                                   ║
 * ║  • Bidirectional traversal (forward = oldest→newest,                 ║
 * ║    backward = newest→oldest for "recent activity" views)             ║
 * ║  • O(n) search — acceptable for in-memory ledger slices              ║
 * ║  • No array resizing overhead vs ArrayList                           ║
 * ║                                                                      ║
 * ║  Time Complexity:                                                    ║
 * ║    addFirst  → O(1)                                                  ║
 * ║    addLast   → O(1)                                                  ║
 * ║    remove    → O(n)                                                  ║
 * ║    getTotal  → O(n)                                                  ║
 * ║    size      → O(1)                                                  ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
public class TransactionLedger implements Iterable<Transaction> {

    // ── Node (inner class) ─────────────────────────────────────────────
    private static class Node {
        Transaction data;
        Node prev;
        Node next;

        Node(Transaction data) {
            this.data = data;
        }
    }

    private Node head;
    private Node tail;
    @Getter private int size;

    // ── Add Operations ─────────────────────────────────────────────────

    /** O(1) — add newest transaction at the front */
    public void addFirst(Transaction t) {
        Node node = new Node(t);
        if (head == null) {
            head = tail = node;
        } else {
            node.next = head;
            head.prev = node;
            head = node;
        }
        size++;
    }

    /** O(1) — append for batch imports */
    public void addLast(Transaction t) {
        Node node = new Node(t);
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
        size++;
    }

    /** O(n) — remove by transaction id */
    public boolean removeById(Long id) {
        Node current = head;
        while (current != null) {
            if (current.data.getId().equals(id)) {
                unlink(current);
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /** O(1) — returns null if empty */
    public Transaction peekFirst() {
        return head == null ? null : head.data;
    }

    public Transaction peekLast() {
        return tail == null ? null : tail.data;
    }

    // ── Aggregation ────────────────────────────────────────────────────

    /**
     * O(n) — compute running balance:
     * income entries add, expense entries subtract.
     */
    public BigDecimal computeBalance() {
        BigDecimal balance = BigDecimal.ZERO;
        Node current = head;
        while (current != null) {
            Transaction t = current.data;
            if (t.getType() == Transaction.TransactionType.INCOME) {
                balance = balance.add(t.getAmount());
            } else {
                balance = balance.subtract(t.getAmount());
            }
            current = current.next;
        }
        return balance;
    }

    /**
     * O(n) — total spent in a given category
     */
    public BigDecimal totalByCategory(Transaction.Category category) {
        BigDecimal total = BigDecimal.ZERO;
        Node current = head;
        while (current != null) {
            Transaction t = current.data;
            if (t.getCategory() == category) {
                total = total.add(t.getAmount());
            }
            current = current.next;
        }
        return total;
    }

    // ── Reverse Iterator (newest → oldest) ────────────────────────────

    public Iterable<Transaction> reverseIterable() {
        return () -> new Iterator<>() {
            Node current = tail;

            @Override public boolean hasNext() { return current != null; }

            @Override
            public Transaction next() {
                if (current == null) throw new NoSuchElementException();
                Transaction data = current.data;
                current = current.prev;
                return data;
            }
        };
    }

    // ── Standard Iterator (oldest → newest) ───────────────────────────

    @Override
    public Iterator<Transaction> iterator() {
        return new Iterator<>() {
            Node current = head;

            @Override public boolean hasNext() { return current != null; }

            @Override
            public Transaction next() {
                if (current == null) throw new NoSuchElementException();
                Transaction data = current.data;
                current = current.next;
                return data;
            }
        };
    }

    // ── Private Helpers ────────────────────────────────────────────────

    private void unlink(Node node) {
        if (node.prev != null) node.prev.next = node.next;
        else head = node.next;

        if (node.next != null) node.next.prev = node.prev;
        else tail = node.prev;

        size--;
    }

    public boolean isEmpty() { return size == 0; }
}
