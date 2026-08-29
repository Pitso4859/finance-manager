package com.financemanager;

import com.financemanager.app.AppServices;
import com.financemanager.dto.DashboardSummary;
import com.financemanager.dto.ReportData;
import com.financemanager.model.Category;
import com.financemanager.model.TransactionType;
import com.financemanager.model.User;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

public final class ServiceIntegrationTest {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("finance-manager-test-");
        AppServices services = new AppServices(directory);

        User user = services.auth.register(
                "Test Developer",
                "developer@example.com",
                "StrongPass123".toCharArray(),
                "StrongPass123".toCharArray()
        );
        services.auth.login("developer@example.com", "StrongPass123".toCharArray());

        services.transactions.create(user, "Monthly salary", "20000.00", TransactionType.INCOME,
                Category.SALARY, LocalDate.now().toString(), "Integration test income");
        services.transactions.create(user, "Groceries", "1500.00", TransactionType.EXPENSE,
                Category.FOOD, LocalDate.now().toString(), "Integration test expense");
        services.budgets.create(user, Category.FOOD, "1600.00", LocalDate.now().getMonthValue(), LocalDate.now().getYear());

        DashboardSummary dashboard = services.dashboard.build(user);
        assert dashboard.income().compareTo(new BigDecimal("20000.00")) == 0;
        assert dashboard.expenses().compareTo(new BigDecimal("1500.00")) == 0;
        assert dashboard.budgetAlerts().size() == 1;

        ReportData report = services.reports.generate(user, "summary",
                LocalDate.now().withDayOfMonth(1), LocalDate.now());
        assert report.net().compareTo(new BigDecimal("18500.00")) == 0;

        AppServices reloaded = new AppServices(directory);
        User reloadedUser = reloaded.auth.login("developer@example.com", "StrongPass123".toCharArray());
        assert reloaded.transactions.list(reloadedUser).size() == 2;
        assert reloaded.budgets.list(reloadedUser).size() == 1;

        System.out.println("Service integration test passed.");
    }
}
