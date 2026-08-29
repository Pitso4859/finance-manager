package com.financemanager.ui.panels;

import com.financemanager.dto.DashboardSummary;
import com.financemanager.model.Transaction;
import com.financemanager.model.User;
import com.financemanager.service.DashboardService;
import com.financemanager.ui.UiTheme;
import com.financemanager.ui.components.MetricCard;
import com.financemanager.ui.components.TableSupport;
import com.financemanager.util.Money;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;

@SuppressWarnings("serial")
public final class DashboardPanel extends JPanel {
    private final DashboardService service;
    private final User user;
    private final MetricCard income=new MetricCard("Income this month"), expenses=new MetricCard("Expenses this month"), net=new MetricCard("Net balance"), savings=new MetricCard("Savings rate");
    private final DefaultTableModel recentModel=new DefaultTableModel(new Object[]{"Date","Description","Type","Category","Amount"},0){public boolean isCellEditable(int r,int c){return false;}};
    private final JTable recentTable=new JTable(recentModel);
    private final JTextArea insights=new JTextArea();

    public DashboardPanel(DashboardService service, User user){
        this.service=service;this.user=user;setLayout(new BorderLayout(0,16));setBackground(UiTheme.BACKGROUND);setBorder(BorderFactory.createEmptyBorder(22,22,22,22));
        JPanel cards=new JPanel(new GridLayout(1,4,12,0));cards.setOpaque(false);cards.add(income);cards.add(expenses);cards.add(net);cards.add(savings);add(cards,BorderLayout.NORTH);
        JPanel center=new JPanel(new GridLayout(1,2,14,0));center.setOpaque(false);
        JPanel recent=UiTheme.card();recent.setLayout(new BorderLayout(0,10));JLabel rt=new JLabel("Recent transactions");rt.setFont(UiTheme.SUBHEADING);recent.add(rt,BorderLayout.NORTH);TableSupport.style(recentTable);recent.add(new JScrollPane(recentTable),BorderLayout.CENTER);center.add(recent);
        JPanel analysis=UiTheme.card();analysis.setLayout(new BorderLayout(0,10));JLabel at=new JLabel("Spending and budget insights");at.setFont(UiTheme.SUBHEADING);analysis.add(at,BorderLayout.NORTH);insights.setEditable(false);insights.setLineWrap(true);insights.setWrapStyleWord(true);insights.setBackground(UiTheme.SURFACE);insights.setForeground(UiTheme.TEXT);insights.setFont(UiTheme.BODY);analysis.add(new JScrollPane(insights),BorderLayout.CENTER);center.add(analysis);add(center,BorderLayout.CENTER);
    }

    public void refresh(){
        DashboardSummary s=service.build(user);income.setValue(Money.format(s.income()));expenses.setValue(Money.format(s.expenses()));net.setValue(Money.format(s.netBalance()));savings.setValue(s.savingsRate()+"%");
        recentModel.setRowCount(0);for(Transaction tx:s.recentTransactions())recentModel.addRow(new Object[]{tx.getTransactionDate(),tx.getDescription(),tx.getType(),tx.getCategory(),Money.format(tx.getAmount())});
        StringBuilder text=new StringBuilder();if(s.topCategories().isEmpty())text.append("No expense activity has been recorded this month.\n");else{ text.append("Top expense categories\n\n");for(Map.Entry<String, BigDecimal> e:s.topCategories().entrySet())text.append(e.getKey()).append(": ").append(Money.format(e.getValue())).append('\n');}
        text.append("\nBudget alerts\n\n");if(s.budgetAlerts().isEmpty())text.append("No budgets are at or above 80% usage.");else s.budgetAlerts().forEach(a->text.append(a.category()).append(": ").append(String.format("%.0f%% used",a.ratio()*100)).append(" (spent ").append(Money.format(a.spent())).append(" of ").append(Money.format(a.limit())).append(")\n"));insights.setText(text.toString());insights.setCaretPosition(0);
    }
}
