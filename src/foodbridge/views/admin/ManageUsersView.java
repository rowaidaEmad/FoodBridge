package foodbridge.views.admin;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import foodbridge.controllers.UserController;
import foodbridge.models.users.User;
import foodbridge.views.ModuleView;

/**
 * Screen; talks only to UserController. Lists every account; edits contact details and activates or deactivates.
 */
public class ManageUsersView extends ModuleView {
    private final UserController userController;
    private final int currentUserId;
    private final DefaultTableModel rows = tableModel("ID", "Name", "Email", "Role", "Phone", "Address", "Active");
    private final JTable table = table(rows);
    private final JTextField email = new JTextField();
    private final JTextField phone = new JTextField();
    private final JTextField address = new JTextField();

    public ManageUsersView(UserController userController, int currentUserId) {
        super("FoodBridge - Manage users", 900, 560);
        this.userController = userController;
        this.currentUserId = currentUserId;

        JPanel form = form();
        field(form, "Email", email);
        field(form, "Phone", phone);
        field(form, "Address", address);
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.add(button("Save details", this::saveDetails));
        actions.add(button("Activate / deactivate", this::toggleActive));
        actions.add(button("Refresh", this::refresh));
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(form, BorderLayout.CENTER);
        bottom.add(actions, BorderLayout.SOUTH);
        content.add(bottom, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> fillForm());
    }

    @Override
    protected void refresh() {
        task(userController::listUsers, this::showUsers);
    }

    private void showUsers(List<User> users) {
        rows.setRowCount(0);
        for (User u : users) {
            rows.addRow(new Object[] { u.getId(), u.getName(), u.getEmail(), u.getClass().getSimpleName(),
                    u.getPhone(), u.getAddress(), u.isActive() ? "Yes" : "No" });
        }
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        email.setText(String.valueOf(rows.getValueAt(modelRow, 2)));
        phone.setText(rows.getValueAt(modelRow, 4) == null ? "" : String.valueOf(rows.getValueAt(modelRow, 4)));
        address.setText(rows.getValueAt(modelRow, 5) == null ? "" : String.valueOf(rows.getValueAt(modelRow, 5)));
    }

    private int selectedRow() {
        int row = table.getSelectedRow();
        if (row < 0) {
            throw new IllegalArgumentException("Select a user first.");
        }
        return table.convertRowIndexToModel(row);
    }

    private void saveDetails() {
        int id = (Integer) rows.getValueAt(selectedRow(), 0);
        String newEmail = email.getText().trim();
        String newPhone = phone.getText().trim();
        String newAddress = address.getText().trim();
        task(() -> {
            userController.changeEmail(id, newEmail);
            userController.updateContactInfo(id, newPhone, newAddress);
            return userController.listUsers();
        }, this::showUsers);
    }

    private void toggleActive() {
        int row = selectedRow();
        int id = (Integer) rows.getValueAt(row, 0);
        if (id == currentUserId) {
            throw new IllegalArgumentException("You cannot deactivate your own account.");
        }
        boolean active = "No".equals(rows.getValueAt(row, 6));
        task(() -> {
            userController.setActive(id, active);
            return userController.listUsers();
        }, this::showUsers);
    }
}
