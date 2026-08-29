package com.financemanager.ui.panels;

import com.financemanager.model.Budget;
import com.financemanager.model.User;
import com.financemanager.service.BudgetService;
import com.financemanager.ui.UiTheme;
import com.financemanager.ui.components.TableSupport;
import com.financemanager.ui.dialogs.BudgetDialog;
import com.financemanager.util.Money;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Month;
import java.util.List;

@SuppressWarnings("serial")
public final class BudgetsPanel extends JPanel {
    private final BudgetService service;
    private final User user;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Category", "Limit", "Month", "Year"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<Budget> rows = List.of();

    public BudgetsPanel(BudgetService service, User user) {
        this.service = service;
        this.user = user;
        setLayout(new BorderLayout(0, 14));
        setBackground(UiTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
    }

    public void refresh() {
        rows = service.list(user);
        model.setRowCount(0);
        for (Budget budget : rows) {
            model.addRow(new Object[]{
                    budget.getCategory(),
                    Money.format(budget.getLimitAmount()),
                    Month.of(budget.getMonth()),
                    budget.getYear()
            });
        }
    }

    private JPanel buildHeader() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Budgets");
        title.setFont(UiTheme.HEADING);
        top.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton add = UiTheme.primaryButton("Add Budget");
        JButton edit = UiTheme.secondaryButton("Edit");
        JButton delete = UiTheme.secondaryButton("Delete");
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        top.add(actions, BorderLayout.EAST);

        add.addActionListener(event -> openDialog(null));
        edit.addActionListener(event -> {
            Budget budget = selectedBudget();
            if (budget != null) {
                openDialog(budget);
            }
        });
        delete.addActionListener(event -> deleteSelected());
        return top;
    }

    private JPanel buildTable() {
        JPanel card = UiTheme.card();
        card.setLayout(new BorderLayout());
        TableSupport.style(table);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private Budget selectedBudget() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a budget first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return rows.get(table.convertRowIndexToModel(row));
    }

    private void openDialog(Budget budget) {
        BudgetDialog dialog = new BudgetDialog(SwingUtilities.getWindowAncestor(this), service, user, budget);
        dialog.setVisible(true);
        if (dialog.wasSaved()) {
            refresh();
        }
    }

    private void deleteSelected() {
        Budget budget = selectedBudget();
        if (budget == null) {
            return;
        }
        int result = JOptionPane.showConfirmDialog(
                this,
                "Delete the selected budget?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            service.delete(user, budget.getId());
            refresh();
        }
    }
}
