package foodbridge.views;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import foodbridge.controllers.AuthController;
import foodbridge.controllers.DonationController;
import foodbridge.controllers.EventController;
import foodbridge.controllers.NotificationController;
import foodbridge.controllers.PickupController;
import foodbridge.controllers.VolunteerController;
import foodbridge.models.users.Admin;
import foodbridge.models.users.Donor;
import foodbridge.models.users.User;
import foodbridge.models.users.Volunteer;
import foodbridge.views.admin.VolunteerAssignmentView;
import foodbridge.views.auth.LoginView;
import foodbridge.views.donor.AddDonationView;
import foodbridge.views.volunteer.VolunteerPanelView;

/**
 * Shown after login. Role screens (donor, volunteer, beneficiary, admin) will be linked from here.
 */
public class HomeView implements View {
    private final JFrame frame = new JFrame("FoodBridge - Home");

    public HomeView(User user, AuthController authController, LoginView loginView) {
        String role = user.getClass().getSimpleName();
        JLabel welcome = new JLabel("Welcome, " + user.getName() + " (" + role + ")");
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            authController.logout();
            for (Window window : Window.getWindows()) {
                window.dispose();
            }
            loginView.show();
        });

        JPanel actions = new JPanel(new GridLayout(0, 1, 8, 8));
        if (user instanceof Volunteer) {
            actions.add(roleButton("Volunteer Panel", () -> new VolunteerPanelView(user.getId(), new VolunteerController(),
                    new PickupController(), new EventController(), new NotificationController())));
        }
        if (user instanceof Volunteer || user instanceof Donor) {
            actions.add(roleButton("Donate Food", () -> new AddDonationView(user, new DonationController())));
        }
        if (user instanceof Admin) {
            actions.add(roleButton("Assign Volunteers", () -> new VolunteerAssignmentView(new VolunteerController(),
                    new PickupController(), new EventController())));
        }
        actions.add(logoutButton);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.add(welcome, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    @Override
    public void show() {
        frame.setVisible(true);
    }

    private JButton roleButton(String label, Supplier<View> screen) {
        JButton button = new JButton(label);
        button.addActionListener(e -> SwingSupport.attempt(frame, () -> screen.get().show()));
        return button;
    }
}
