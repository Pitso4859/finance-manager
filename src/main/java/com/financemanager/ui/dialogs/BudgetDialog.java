package com.financemanager.ui.dialogs;

import com.financemanager.model.Budget;
import com.financemanager.model.Category;
import com.financemanager.model.User;
import com.financemanager.service.BudgetService;
import com.financemanager.ui.UiTheme;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

@SuppressWarnings("serial")
public final class BudgetDialog extends JDialog {
    private final JComboBox<Category> category = new JComboBox<>(Category.values());
    private final JTextField amount = new JTextField(18);
    private final JSpinner month = new JSpinner(
            new SpinnerNumberModel(LocalDate.now().getMonthValue(), 1, 12, 1));
    private final JSpinner year = new JSpinner(
            new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2200, 1));
    private boolean saved;

    public BudgetDialog(Window owner, BudgetService service, User user, Budget existing) {
        super(owner, existing == null ? "Add Budget" : "Edit Budget", ModalityType.APPLICATION_MODAL);
        setContentPane(buildContent(service, user, existing));
        if (existing != null) {
            populate(existing);
        }
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public boolean wasSaved() {
        return saved;
    }

    private JPanel buildContent(BudgetService service, User user, Budget existing) {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.add(new JLabel("Category"));
        form.add(category);
        form.add(new JLabel("Limit amount"));
        form.add(amount);
        form.add(new JLabel("Month"));
        form.add(month);
        form.add(new JLabel("Year"));
        form.add(year);
        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = UiTheme.secondaryButton("Cancel");
        JButton save = UiTheme.primaryButton("Save");
        buttons.add(cancel);
        buttons.add(save);
        root.add(buttons, BorderLayout.SOUTH);

        cancel.addActionListener(event -> dispose());
        save.addActionListener(event -> saveBudget(service, user, existing));
        return root;
    }

    private void saveBudget(BudgetService service, User user, Budget existing) {
        try {
            if (existing == null) {
                service.create(
                        user,
                        (Category) category.getSelectedItem(),
                        amount.getText(),
                        (Integer) month.getValue(),
                        (Integer) year.getValue()
                );
            } else {
                service.update(
                        user,
                        existing.getId(),
                        (Category) category.getSelectedItem(),
                        amount.getText(),
                        (Integer) month.getValue(),
                        (Integer) year.getValue()
                );
            }
            saved = true;
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Could not save budget",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void populate(Budget budget) {
        category.setSelectedItem(budget.getCategory());
        amount.setText(budget.getLimitAmount().toPlainString());
        month.setValue(budget.getMonth());
        year.setValue(budget.getYear());
    }
}
