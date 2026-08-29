package com.financemanager.ui;

import com.financemanager.app.AppServices;
import com.financemanager.model.User;
import com.financemanager.ui.panels.*;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
public final class MainFrame extends JFrame {
    public interface LogoutListener { void onLogout(); }

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, Runnable> refreshers = new LinkedHashMap<>();

    public MainFrame(AppServices services, User user, LogoutListener logoutListener) {
        super("Finance Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1280, 800);
        setLocationRelativeTo(null);

        DashboardPanel dashboard = new DashboardPanel(services.dashboard, user);
        TransactionsPanel transactions = new TransactionsPanel(services.transactions, user);
        BudgetsPanel budgets = new BudgetsPanel(services.budgets, user);
        ReportsPanel reports = new ReportsPanel(services.reports, user);

        content.add(dashboard, "dashboard");
        content.add(transactions, "transactions");
        content.add(budgets, "budgets");
        content.add(reports, "reports");
        refreshers.put("dashboard", dashboard::refresh);
        refreshers.put("transactions", transactions::refresh);
        refreshers.put("budgets", budgets::refresh);
        refreshers.put("reports", reports::refresh);

        setContentPane(buildShell(user, services, logoutListener));
        showPage("dashboard");
    }

    private JPanel buildShell(User user, AppServices services, LogoutListener logoutListener) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.BACKGROUND);
        root.add(buildSidebar(user, services, logoutListener), BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildSidebar(User user, AppServices services, LogoutListener logoutListener) {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(215, 0));
        sidebar.setBackground(UiTheme.SIDEBAR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 16, 18, 16));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("Finance Manager");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 19));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel name = new JLabel(user.getFullName());
        name.setForeground(new Color(203, 213, 225));
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(name);
        sidebar.add(Box.createVerticalStrut(30));

        sidebar.add(navButton("Dashboard", () -> showPage("dashboard")));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navButton("Transactions", () -> showPage("transactions")));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navButton("Budgets", () -> showPage("budgets")));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navButton("Reports", () -> showPage("reports")));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(navButton("Sign Out", () -> {
            services.auth.logout();
            logoutListener.onLogout();
            dispose();
        }));
        return sidebar;
    }

    private JButton navButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(Color.WHITE);
        button.setBackground(UiTheme.SIDEBAR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        button.addActionListener(e -> action.run());
        return button;
    }

    private void showPage(String key) {
        cards.show(content, key);
        Runnable refresher = refreshers.get(key);
        if (refresher != null) refresher.run();
    }
}
