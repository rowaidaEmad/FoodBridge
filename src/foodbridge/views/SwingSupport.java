package foodbridge.views;

import java.awt.Component;
import java.awt.Dimension;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Small helpers shared by the Swing screens.
 */
public final class SwingSupport {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int ID_COLUMN_WIDTH = 50;

    private SwingSupport() {
    }

    /** Runs a screen action and shows any failure in a dialog. */
    public static void attempt(Component parent, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Could not complete", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static JTable readOnlyTable(String... columns) {
        JTable table = new JTable(new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        if ("ID".equals(columns[0])) {
            table.getColumnModel().getColumn(0).setMaxWidth(ID_COLUMN_WIDTH);
        }
        return table;
    }

    public static JScrollPane scroll(JTable table, int width, int height) {
        JScrollPane pane = new JScrollPane(table);
        pane.setPreferredSize(new Dimension(width, height));
        return pane;
    }

    public static void setRows(JTable table, List<Object[]> rows) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        rows.forEach(model::addRow);
    }

    /** The id in the first column of the selected row. */
    public static int selectedId(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            throw new IllegalStateException("Select a row first.");
        }
        return (Integer) table.getValueAt(row, 0);
    }

    public static double parseHours(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter the hours as a number.");
        }
    }

    public static String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FORMAT);
    }

    public static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    public static List<String> splitValues(String text) {
        return Arrays.asList(text.split(","));
    }
}
