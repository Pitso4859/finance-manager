package com.financemanager.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class UiTheme {
    public static final Color BACKGROUND = new Color(244, 246, 248);
    public static final Color SURFACE = Color.WHITE;
    public static final Color SIDEBAR = new Color(31, 41, 55);
    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color TEXT = new Color(31, 41, 55);
    public static final Color MUTED = new Color(107, 114, 128);
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Color SUCCESS = new Color(22, 101, 52);
    public static final Color DANGER = new Color(185, 28, 28);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font HEADING = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font SUBHEADING = new Font("Segoe UI", Font.BOLD, 16);

    private UiTheme() {}

    public static void install() {
        UIManager.put("Label.font", BODY);
        UIManager.put("Button.font", BODY);
        UIManager.put("TextField.font", BODY);
        UIManager.put("PasswordField.font", BODY);
        UIManager.put("ComboBox.font", BODY);
        UIManager.put("Table.font", BODY);
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("OptionPane.messageFont", BODY);
        UIManager.put("OptionPane.buttonFont", BODY);
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 16, 9, 16));
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(SURFACE);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 15, 8, 15)));
        return button;
    }

    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(18, 18, 18, 18)));
        return panel;
    }
}
