package foodbridge.views.auth;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import foodbridge.controllers.UserController;
import foodbridge.models.lookups.Area;
import foodbridge.models.users.User;
import foodbridge.views.ModuleView;

/**
 * Screen; talks only to UserController. The logged-in user's email, phone, address and area.
 */
public class ProfileView extends ModuleView {
    private final UserController userController;
    private final User user;
    private final JTextField email = new JTextField(24);
    private final JTextField phone = new JTextField(24);
    private final JTextField address = new JTextField(24);
    private final JComboBox<Area> area = new JComboBox<>();

    public ProfileView(UserController userController, User user) {
        super("FoodBridge - Contact details", 460, 290);
        this.userController = userController;
        this.user = user;
        JPanel form = form();
        field(form, "Email", email);
        field(form, "Phone", phone);
        field(form, "Address", address);
        field(form, "Area", area);
        content.add(form, BorderLayout.NORTH);
        JPanel actions = new JPanel();
        actions.add(button("Save", this::save));
        content.add(actions, BorderLayout.SOUTH);
    }

    @Override
    protected void refresh() {
        email.setText(user.getEmail());
        phone.setText(user.getPhone());
        address.setText(user.getAddress());
        task(userController::listAreas, areas -> {
            choices(area, areas);
            for (Area a : areas) {
                if (user.getArea() != null && user.getArea().getId() == a.getId()) {
                    area.setSelectedItem(a);
                }
            }
        });
    }

    private void save() {
        String newEmail = email.getText().trim();
        String newPhone = phone.getText().trim();
        String newAddress = address.getText().trim();
        Area newArea = (Area) area.getSelectedItem();
        task(() -> {
            userController.changeEmail(user.getId(), newEmail);
            userController.updateContactInfo(user.getId(), newPhone, newAddress);
            userController.changeArea(user.getId(), newArea);
            return true;
        }, saved -> {
            // Keep the session's copy in step, so screens opened later show the new details.
            user.changeEmail(newEmail);
            user.updateContactInfo(newPhone, newAddress);
            user.setArea(newArea);
            message("Your details were saved.");
        });
    }
}
