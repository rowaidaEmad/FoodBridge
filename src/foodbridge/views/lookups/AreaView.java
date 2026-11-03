package foodbridge.views.lookups;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import foodbridge.controllers.LookupController;
import foodbridge.models.lookups.Area;
import foodbridge.views.ModuleView;

/**
 * Screen; talks only to LookupController. Admin CRUD on areas (city and district).
 */
public class AreaView extends ModuleView {
    private final LookupController lookupController;
    private final DefaultTableModel rows = tableModel("ID", "City", "District");
    private final JTable table = table(rows);
    private final JTextField city = new JTextField();
    private final JTextField district = new JTextField();

    public AreaView(LookupController lookupController) {
        super("FoodBridge - Areas", 560, 460);
        this.lookupController = lookupController;

        JPanel form = form();
        field(form, "City", city);
        field(form, "District", district);
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.add(button("Add", this::add));
        actions.add(button("Save changes", this::saveChanges));
        actions.add(button("Delete", this::delete));
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(form, BorderLayout.CENTER);
        bottom.add(actions, BorderLayout.SOUTH);
        content.add(bottom, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                city.setText(String.valueOf(rows.getValueAt(modelRow, 1)));
                district.setText(String.valueOf(rows.getValueAt(modelRow, 2)));
            }
        });
    }

    @Override
    protected void refresh() {
        task(lookupController::listAreas, this::showAreas);
    }

    private void showAreas(List<Area> areas) {
        rows.setRowCount(0);
        for (Area a : areas) {
            rows.addRow(new Object[] { a.getId(), a.getCity(), a.getDistrict() });
        }
    }

    private int selectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            throw new IllegalArgumentException("Select an area first.");
        }
        return (Integer) rows.getValueAt(table.convertRowIndexToModel(row), 0);
    }

    private void add() {
        Area area = new Area(0, city.getText(), district.getText());
        task(() -> {
            lookupController.addArea(area);
            return lookupController.listAreas();
        }, this::showAreas);
    }

    private void saveChanges() {
        Area area = new Area(selectedId(), city.getText(), district.getText());
        task(() -> {
            lookupController.updateArea(area);
            return lookupController.listAreas();
        }, this::showAreas);
    }

    private void delete() {
        int id = selectedId();
        if (JOptionPane.showConfirmDialog(frame, "Delete this area?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        task(() -> {
            lookupController.deleteArea(id);
            return lookupController.listAreas();
        }, this::showAreas);
    }
}
