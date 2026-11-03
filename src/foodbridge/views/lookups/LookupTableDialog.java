package foodbridge.views.lookups;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import foodbridge.views.BackBar;

/**
 * Shared Swing dialog for the id/name lookup tables: list, add, rename, delete.
 * Each lookup view passes in its LookupController calls.
 */
class LookupTableDialog {
    private final JDialog dialog = new JDialog();
    private final DefaultTableModel model = new DefaultTableModel(new Object[] { "ID", "Name" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final String itemName;
    private final Supplier<List<Object[]>> rows;

    LookupTableDialog(String title, String itemName, Supplier<List<Object[]>> rows, Consumer<String> add,
            BiConsumer<Integer, String> rename, IntConsumer delete) {
        this.itemName = itemName;
        this.rows = rows;
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton addButton = new JButton("Add");
        JButton renameButton = new JButton("Rename");
        JButton deleteButton = new JButton("Delete");

        addButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(dialog, "New " + itemName + " name:");
            if (name != null) {
                run(() -> add.accept(name));
            }
        });
        renameButton.addActionListener(e -> {
            int row = selectedRow();
            if (row >= 0) {
                String name = JOptionPane.showInputDialog(dialog, "New name:", model.getValueAt(row, 1));
                if (name != null) {
                    run(() -> rename.accept((Integer) model.getValueAt(row, 0), name));
                }
            }
        });
        deleteButton.addActionListener(e -> {
            int row = selectedRow();
            if (row >= 0 && JOptionPane.showConfirmDialog(dialog, "Delete '" + model.getValueAt(row, 1) + "'?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                run(() -> delete.accept((Integer) model.getValueAt(row, 0)));
            }
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(addButton);
        buttons.add(renameButton);
        buttons.add(deleteButton);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        content.add(BackBar.create(dialog::dispose), BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);

        dialog.setTitle("FoodBridge - " + title);
        dialog.setModal(true);
        dialog.add(content);
        dialog.setSize(420, 360);
        dialog.setLocationRelativeTo(null);
    }

    void show() {
        if (reload()) {
            dialog.setVisible(true);
        }
    }

    private boolean reload() {
        try {
            model.setRowCount(0);
            for (Object[] row : rows.get()) {
                model.addRow(row);
            }
            return true;
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private int selectedRow() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(dialog, "Select a " + itemName + " first.");
        }
        return row;
    }

    private void run(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        reload();
    }
}
