package com.financemanager.datastructures;

import com.financemanager.model.Transaction;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class TransactionLedger implements Iterable<Transaction> {
    private Node head;
    private Node tail;
    private int size;

    public void addFirst(Transaction transaction) {
        Node node = new Node(transaction);
        node.next = head;
        if (head != null) head.previous = node;
        else tail = node;
        head = node;
        size++;
    }

    public void addLast(Transaction transaction) {
        Node node = new Node(transaction);
        node.previous = tail;
        if (tail != null) tail.next = node;
        else head = node;
        tail = node;
        size++;
    }

    public boolean removeById(String id) {
        for (Node current = head; current != null; current = current.next) {
            if (current.value.getId().equals(id)) {
                if (current.previous == null) head = current.next;
                else current.previous.next = current.next;
                if (current.next == null) tail = current.previous;
                else current.next.previous = current.previous;
                size--;
                return true;
            }
        }
        return false;
    }

    public BigDecimal computeNetBalance() {
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction tx : this) {
            balance = tx.getType() == com.financemanager.model.TransactionType.INCOME
                    ? balance.add(tx.getAmount()) : balance.subtract(tx.getAmount());
        }
        return balance;
    }

    public int size() { return size; }

    public Iterable<Transaction> reverseIterable() {
        return () -> new Iterator<>() {
            private Node current = tail;
            public boolean hasNext() { return current != null; }
            public Transaction next() {
                if (current == null) throw new NoSuchElementException();
                Transaction value = current.value;
                current = current.previous;
                return value;
            }
        };
    }

    @Override
    public Iterator<Transaction> iterator() {
        return new Iterator<>() {
            private Node current = head;
            public boolean hasNext() { return current != null; }
            public Transaction next() {
                if (current == null) throw new NoSuchElementException();
                Transaction value = current.value;
                current = current.next;
                return value;
            }
        };
    }

    private static final class Node {
        private final Transaction value;
        private Node previous;
        private Node next;
        private Node(Transaction value) { this.value = value; }
    }
}
