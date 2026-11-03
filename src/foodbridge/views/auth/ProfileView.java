package foodbridge.views.auth;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;

import foodbridge.controllers.UserController;
import foodbridge.models.users.User;
import foodbridge.views.ModuleView;

/**
 * Screen; talks only to UserController. The logged-in user's email, phone and address.
 */
public class ProfileView extends ModuleView {
    private final UserController userController;
    private final User user;
    private final JTextField email = new JTextField(24);
    private final JTextField phone = new JTextField(24);
    private final JTextField address = new JTextField(24);

    public ProfileView(UserController userController, User user) {
        super("FoodBridge - Contact details", 460, 260);
        this.userController = userController;
        this.user = user;
        JPanel form = form();
        field(form, "Email", email);
        field(form, "Phone", phone);
        field(form, "Address", address);
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
    }

    private void save() {
        String newEmail = email.getText().trim();
        String newPhone = phone.getText().trim();
        String newAddress = address.getText().trim();
        task(() -> {
            userController.changeEmail(user.getId(), newEmail);
            userController.updateContactInfo(user.getId(), newPhone, newAddress);
            return true;
        }, saved -> {
            // Keep the session's copy in step, so screens opened later show the new details.
            user.changeEmail(newEmail);
            user.updateContactInfo(newPhone, newAddress);
            message("Your details were saved.");
        });
    }
}
