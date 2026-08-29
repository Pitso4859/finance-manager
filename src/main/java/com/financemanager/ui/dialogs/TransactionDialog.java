package com.financemanager.ui.dialogs;

import com.financemanager.model.*;
import com.financemanager.service.TransactionService;
import com.financemanager.ui.UiTheme;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

@SuppressWarnings("serial")
public final class TransactionDialog extends JDialog {
    private final JTextField description = new JTextField(22);
    private final JTextField amount = new JTextField(22);
    private final JComboBox<TransactionType> type = new JComboBox<>(TransactionType.values());
    private final JComboBox<Category> category = new JComboBox<>(Category.values());
    private final JTextField date = new JTextField(LocalDate.now().toString(), 22);
    private final JTextArea notes = new JTextArea(4, 22);
    private boolean saved;

    public TransactionDialog(Window owner, TransactionService service, User user, Transaction existing) {
        super(owner, existing == null ? "Add Transaction" : "Edit Transaction", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(build(service, user, existing));
        if (existing != null) populate(existing);
        pack(); setResizable(false); setLocationRelativeTo(owner);
    }

    public boolean wasSaved() { return saved; }

    private JPanel build(TransactionService service, User user, Transaction existing) {
        JPanel root = new JPanel(new BorderLayout(0, 14)); root.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        JPanel form = new JPanel(new GridBagLayout()); GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6); c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        addRow(form,c,row++,"Description",description); addRow(form,c,row++,"Amount",amount);
        addRow(form,c,row++,"Type",type); addRow(form,c,row++,"Category",category); addRow(form,c,row++,"Date (YYYY-MM-DD)",date);
        notes.setLineWrap(true); notes.setWrapStyleWord(true); addRow(form,c,row,"Notes",new JScrollPane(notes));
        root.add(form, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT)); JButton cancel = UiTheme.secondaryButton("Cancel");
        JButton save = UiTheme.primaryButton("Save"); buttons.add(cancel); buttons.add(save); root.add(buttons,BorderLayout.SOUTH);
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> {
            try {
                if (existing == null) service.create(user,description.getText(),amount.getText(),(TransactionType)type.getSelectedItem(),
                        (Category)category.getSelectedItem(),date.getText(),notes.getText());
                else service.update(user,existing.getId(),description.getText(),amount.getText(),(TransactionType)type.getSelectedItem(),
                        (Category)category.getSelectedItem(),date.getText(),notes.getText());
                saved = true; dispose();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not save transaction", JOptionPane.ERROR_MESSAGE);
            }
        });
        return root;
    }

    private void populate(Transaction tx) {
        description.setText(tx.getDescription()); amount.setText(tx.getAmount().toPlainString()); type.setSelectedItem(tx.getType());
        category.setSelectedItem(tx.getCategory()); date.setText(tx.getTransactionDate().toString()); notes.setText(tx.getNotes());
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, Component input) {
        c.gridx=0; c.gridy=row; c.weightx=0; panel.add(new JLabel(label),c);
        c.gridx=1; c.weightx=1; panel.add(input,c);
    }
}
