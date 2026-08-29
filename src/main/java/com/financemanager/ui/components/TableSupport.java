package com.financemanager.ui.components;

import com.financemanager.ui.UiTheme;
import javax.swing.*;
import java.awt.*;

public final class TableSupport {
    private TableSupport() {}
    public static void style(JTable table) {
        table.setRowHeight(30); table.setShowVerticalLines(false); table.setGridColor(UiTheme.BORDER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
        table.getTableHeader().setReorderingAllowed(false);
    }
}
