package com.financemanager;

import com.financemanager.app.AppServices;
import com.financemanager.ui.LoginFrame;
import com.financemanager.ui.MainFrame;
import com.financemanager.ui.UiTheme;

import javax.swing.*;
import java.nio.file.Path;

public final class FinanceManagerApplication {
    private FinanceManagerApplication() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UiTheme.install();
            Path dataDirectory = resolveDataDirectory();
            AppServices services = new AppServices(dataDirectory);
            showLogin(services);
        });
    }

    private static void showLogin(AppServices services) {
        LoginFrame login = new LoginFrame(services, user -> {
            MainFrame main = new MainFrame(services, user, () -> showLogin(services));
            main.setVisible(true);
        });
        login.setVisible(true);
    }

    private static Path resolveDataDirectory() {
        String override = System.getProperty("finance.manager.data.dir");
        if (override != null && !override.isBlank()) return Path.of(override);
        return Path.of(System.getProperty("user.home"), ".finance-manager");
    }
}
