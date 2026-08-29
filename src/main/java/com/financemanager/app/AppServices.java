package com.financemanager.app;

import com.financemanager.patterns.observer.*;
import com.financemanager.patterns.strategy.*;
import com.financemanager.persistence.FileDataStore;
import com.financemanager.repository.*;
import com.financemanager.security.*;
import com.financemanager.service.*;

import java.nio.file.Path;
import java.util.List;

public final class AppServices {
    public final SessionManager session;
    public final AuthService auth;
    public final TransactionService transactions;
    public final BudgetService budgets;
    public final DashboardService dashboard;
    public final ReportService reports;

    public AppServices(Path dataDirectory) {
        FileDataStore store = new FileDataStore(dataDirectory);
        UserRepository userRepository = new UserRepository(store);
        TransactionRepository transactionRepository = new TransactionRepository(store);
        BudgetRepository budgetRepository = new BudgetRepository(store);
        TransactionEventPublisher events = new TransactionEventPublisher();
        events.subscribe(new AuditLogObserver(dataDirectory));

        session = new SessionManager();
        auth = new AuthService(userRepository, new PasswordHasher(), session);
        transactions = new TransactionService(transactionRepository, events);
        budgets = new BudgetService(budgetRepository);
        dashboard = new DashboardService(transactionRepository, budgetRepository);
        reports = new ReportService(transactionRepository,
                List.of(new SummaryReportStrategy(), new ExpenseReportStrategy()));
    }
}
