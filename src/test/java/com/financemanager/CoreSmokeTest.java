package com.financemanager;

import com.financemanager.datastructures.BudgetAlertHeap;
import com.financemanager.datastructures.TransactionLedger;
import com.financemanager.model.*;
import com.financemanager.security.PasswordHasher;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class CoreSmokeTest {
    public static void main(String[] args) {
        testPasswordHashing();
        testLedger();
        testBudgetHeap();
        System.out.println("All core smoke tests passed.");
    }

    private static void testPasswordHashing() {
        PasswordHasher hasher = new PasswordHasher();
        String hash = hasher.hash("Password123".toCharArray());
        assert hasher.verify("Password123".toCharArray(), hash);
        assert !hasher.verify("WrongPassword".toCharArray(), hash);
    }

    private static void testLedger() {
        TransactionLedger ledger = new TransactionLedger();
        ledger.addLast(new Transaction("u1", "Salary", new BigDecimal("1000.00"), TransactionType.INCOME,
                Category.SALARY, LocalDate.now(), ""));
        ledger.addLast(new Transaction("u1", "Food", new BigDecimal("250.00"), TransactionType.EXPENSE,
                Category.FOOD, LocalDate.now(), ""));
        assert ledger.size() == 2;
        assert ledger.computeNetBalance().compareTo(new BigDecimal("750.00")) == 0;
    }

    private static void testBudgetHeap() {
        BudgetAlertHeap heap = new BudgetAlertHeap();
        heap.insert(new BudgetAlertHeap.BudgetAlert("FOOD", new BigDecimal("900"), new BigDecimal("1000"), .9));
        heap.insert(new BudgetAlertHeap.BudgetAlert("TRANSPORT", new BigDecimal("1200"), new BigDecimal("1000"), 1.2));
        assert heap.peek().category().equals("TRANSPORT");
    }
}
