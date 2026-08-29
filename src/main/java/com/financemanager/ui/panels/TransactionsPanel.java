package com.financemanager.ui.panels;

import com.financemanager.model.Transaction;
import com.financemanager.model.User;
import com.financemanager.service.TransactionService;
import com.financemanager.ui.UiTheme;
import com.financemanager.ui.components.TableSupport;
import com.financemanager.ui.dialogs.TransactionDialog;
import com.financemanager.util.Money;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public final class TransactionsPanel extends JPanel {
    private final TransactionService service;
    private final User user;
    private final JTextField searchField = new JTextField(20);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Date", "Description", "Type", "Category", "Amount"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private List<Transaction> rows = List.of();

    public TransactionsPanel(TransactionService service, User user) {
        this.service = service;
        this.user = user;

        setLayout(new BorderLayout(0, 14));
        setBackground(UiTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        installSearch();
    }

    public void refresh() {
        rows = service.list(user);
        model.setRowCount(0);
        for (Transaction transaction : rows) {
            model.addRow(new Object[]{
                    transaction.getTransactionDate(),
                    transaction.getDescription(),
                    transaction.getType(),
                    transaction.getCategory(),
                    Money.format(transaction.getAmount())
            });
        }
    }

    private JPanel buildHeader() {
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);

        JLabel title = new JLabel("Transactions");
        title.setFont(UiTheme.HEADING);
        top.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(new JLabel("Search"));
        actions.add(searchField);

        JButton add = UiTheme.primaryButton("Add Transaction");
        JButton edit = UiTheme.secondaryButton("Edit");
        JButton delete = UiTheme.secondaryButton("Delete");
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        top.add(actions, BorderLayout.EAST);

        add.addActionListener(event -> openDialog(null));
        edit.addActionListener(event -> {
            Transaction transaction = selectedTransaction();
            if (transaction != null) {
                openDialog(transaction);
            }
        });
        delete.addActionListener(event -> deleteSelected());

        return top;
    }

    private JPanel buildTableCard() {
        JPanel card = UiTheme.card();
        card.setLayout(new BorderLayout());
        TableSupport.style(table);
        table.setRowSorter(sorter);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private void installSearch() {
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        if (text.isBlank()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        }
    }

    private Transaction selectedTransaction() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a transaction first.",
                    "No selection",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return rows.get(modelRow);
    }

    private void openDialog(Transaction transaction) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        TransactionDialog dialog = new TransactionDialog(owner, service, user, transaction);
        dialog.setVisible(true);
        if (dialog.wasSaved()) {
            refresh();
        }
    }

    private void deleteSelected() {
        Transaction transaction = selectedTransaction();
        if (transaction == null) {
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "Delete the selected transaction?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            service.delete(user, transaction.getId());
            refresh();
        }
    }
}
