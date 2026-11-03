package foodbridge.views.admin;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import foodbridge.controllers.AdminController;
import foodbridge.models.users.Admin;
import foodbridge.views.ModuleView;

/**
 * Screen; talks only to AdminController. The logged-in admin's permissions (FoodBridgeColdStart admin module).
 */
public class AdminPermissionsView extends ModuleView {
    private final AdminController adminController;
    private final Admin admin;
    private final DefaultListModel<String> permissions = new DefaultListModel<>();
    private final JList<String> list = new JList<>(permissions);
    private final JTextField newPermission = new JTextField(20);

    public AdminPermissionsView(AdminController adminController, Admin admin) {
        super("FoodBridge - My permissions", 520, 420);
        this.adminController = adminController;
        this.admin = admin;

        JPanel add = new JPanel();
        add.add(new JLabel("Permission"));
        add.add(newPermission);
        add.add(button("Add", this::addPermission));

        JPanel actions = new JPanel();
        actions.add(button("Remove selected", this::removePermission));
        actions.add(button("Refresh", this::refresh));

        content.add(add, BorderLayout.NORTH);
        content.add(new JScrollPane(list), BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
    }

    @Override
    protected void refresh() {
        task(() -> {
            List<String> sorted = new ArrayList<>(adminController.getPermissions(admin.getId()));
            sorted.sort(null);
            return sorted;
        }, found -> {
            permissions.clear();
            found.forEach(permissions::addElement);
        });
    }

    private void addPermission() {
        String text = newPermission.getText();
        task(() -> adminController.addPermission(admin.getId(), text), added -> {
            newPermission.setText("");
            message(added ? "Permission added." : "You already have this permission.");
            refresh();
        });
    }

    private void removePermission() {
        String selected = list.getSelectedValue();
        if (selected == null) {
            throw new IllegalArgumentException("Select a permission first.");
        }
        task(() -> adminController.removePermission(admin.getId(), selected), removed -> refresh());
    }
}
