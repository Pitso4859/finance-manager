package com.financemanager.ui.components;

import com.financemanager.ui.UiTheme;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public final class MetricCard extends JPanel {
    private final JLabel value = new JLabel("-");
    public MetricCard(String title) {
        setLayout(new BorderLayout(0, 8)); setBackground(UiTheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        JLabel heading = new JLabel(title); heading.setForeground(UiTheme.MUTED);
        value.setFont(new Font("Segoe UI", Font.BOLD, 22)); value.setForeground(UiTheme.TEXT);
        add(heading, BorderLayout.NORTH); add(value, BorderLayout.CENTER);
    }
    public void setValue(String text) { value.setText(text); }
}
