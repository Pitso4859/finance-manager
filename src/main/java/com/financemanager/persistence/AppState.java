package com.financemanager.persistence;

import com.financemanager.model.Budget;
import com.financemanager.model.Transaction;
import com.financemanager.model.User;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public final class AppState implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private final ArrayList<User> users = new ArrayList<>();
    private final ArrayList<Transaction> transactions = new ArrayList<>();
    private final ArrayList<Budget> budgets = new ArrayList<>();

    public ArrayList<User> users() { return users; }
    public ArrayList<Transaction> transactions() { return transactions; }
    public ArrayList<Budget> budgets() { return budgets; }
}
