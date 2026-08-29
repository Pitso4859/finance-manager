package com.financemanager.ui.panels;

import com.financemanager.dto.ReportData;
import com.financemanager.model.User;
import com.financemanager.patterns.strategy.ReportStrategy;
import com.financemanager.service.ReportService;
import com.financemanager.ui.UiTheme;
import com.financemanager.ui.components.TableSupport;
import com.financemanager.util.Money;
import com.financemanager.util.Validation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;

@SuppressWarnings("serial")
public final class ReportsPanel extends JPanel {
    private final ReportService service;
    private final User user;
    private final JComboBox<ReportStrategy> reportType = new JComboBox<>();
    private final JTextField startDate = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 10);
    private final JTextField endDate = new JTextField(LocalDate.now().toString(), 10);
    private final JTextArea summary = new JTextArea();
    private final DefaultTableModel categoriesModel = new DefaultTableModel(
            new Object[]{"Category", "Expense"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel monthlyModel = new DefaultTableModel(
            new Object[]{"Month", "Net"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private ReportData currentReport;

    public ReportsPanel(ReportService service, User user) {
        this.service = service;
        this.user = user;

        setLayout(new BorderLayout(0, 14));
        setBackground(UiTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        configureReportType();
        add(buildControls(), BorderLayout.NORTH);
        add(buildReportArea(), BorderLayout.CENTER);
    }

    public void refresh() {
        generateReport();
    }

    private void configureReportType() {
        for (ReportStrategy strategy : service.strategies()) {
            reportType.addItem(strategy);
        }
        reportType.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value instanceof ReportStrategy strategy ? strategy.displayName() : "");
                return this;
            }
        });
    }

    private JPanel buildControls() {
        JPanel controls = UiTheme.card();
        controls.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.add(new JLabel("Report"));
        controls.add(reportType);
        controls.add(new JLabel("Start"));
        controls.add(startDate);
        controls.add(new JLabel("End"));
        controls.add(endDate);

        JButton generate = UiTheme.primaryButton("Generate");
        JButton export = UiTheme.secondaryButton("Export CSV");
        controls.add(generate);
        controls.add(export);

        generate.addActionListener(event -> generateReport());
        export.addActionListener(event -> exportReport());
        return controls;
    }

    private JPanel buildReportArea() {
        JPanel center = new JPanel(new GridLayout(1, 2, 14, 0));
        center.setOpaque(false);

        JPanel summaryCard = UiTheme.card();
        summaryCard.setLayout(new BorderLayout(0, 10));
        JLabel summaryTitle = new JLabel("Report summary");
        summaryTitle.setFont(UiTheme.SUBHEADING);
        summaryCard.add(summaryTitle, BorderLayout.NORTH);
        summary.setEditable(false);
        summary.setLineWrap(true);
        summary.setWrapStyleWord(true);
        summary.setFont(UiTheme.BODY);
        summaryCard.add(new JScrollPane(summary), BorderLayout.CENTER);
        center.add(summaryCard);

        JPanel tables = new JPanel(new GridLayout(2, 1, 0, 12));
        tables.setOpaque(false);
        tables.add(createTableCard("Expense by category", new JTable(categoriesModel)));
        tables.add(createTableCard("Monthly net trend", new JTable(monthlyModel)));
        center.add(tables);
        return center;
    }

    private JPanel createTableCard(String title, JTable table) {
        JPanel card = UiTheme.card();
        card.setLayout(new BorderLayout(0, 8));
        JLabel label = new JLabel(title);
        label.setFont(UiTheme.SUBHEADING);
        card.add(label, BorderLayout.NORTH);
        TableSupport.style(table);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private void generateReport() {
        try {
            ReportStrategy strategy = (ReportStrategy) reportType.getSelectedItem();
            if (strategy == null) {
                return;
            }
            LocalDate start = Validation.date(startDate.getText(), "Start date");
            LocalDate end = Validation.date(endDate.getText(), "End date");
            currentReport = service.generate(user, strategy.key(), start, end);
            render(currentReport);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Could not generate report",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void render(ReportData report) {
        summary.setText(
                report.title() + "\n\n" +
                report.description() + "\n\n" +
                "Income: " + Money.format(report.income()) + "\n" +
                "Expenses: " + Money.format(report.expenses()) + "\n" +
                "Net: " + Money.format(report.net())
        );
        summary.setCaretPosition(0);

        categoriesModel.setRowCount(0);
        report.categoryExpenses().forEach((category, amount) ->
                categoriesModel.addRow(new Object[]{category, Money.format(amount)}));

        monthlyModel.setRowCount(0);
        report.monthlyNet().forEach((month, amount) ->
                monthlyModel.addRow(new Object[]{month, Money.format(amount)}));
    }

    private void exportReport() {
        if (currentReport == null) {
            generateReport();
            if (currentReport == null) {
                return;
            }
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("finance-report.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            service.exportCsv(currentReport, chooser.getSelectedFile().toPath());
            JOptionPane.showMessageDialog(
                    this,
                    "Report exported successfully.",
                    "Export complete",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Export failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
