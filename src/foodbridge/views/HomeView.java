package foodbridge.views;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import foodbridge.controllers.AuthController;
import foodbridge.controllers.DonationController;
import foodbridge.controllers.LookupController;
import foodbridge.models.users.Admin;
import foodbridge.models.users.Donor;
import foodbridge.models.users.User;
import foodbridge.views.auth.LoginView;
import foodbridge.views.donor.AddDonationView;
import foodbridge.views.donor.PayDonationView;
import foodbridge.views.lookups.DonationStatusView;
import foodbridge.views.lookups.FoodCategoryView;
import foodbridge.views.lookups.UnitView;

/**
 * Shown after login; shows the screens for the user's role.
 * Donor: add donation, my donations (pay / cancel). Admin: lookup tables.
 * TODO: volunteer and beneficiary screens.
 */
public class HomeView implements View {
    private final JFrame frame = new JFrame("FoodBridge - Home");

    public HomeView(User user, AuthController authController, LoginView loginView) {
        String role = user.getClass().getSimpleName();
        JLabel welcome = new JLabel("Welcome, " + user.getName() + " (" + role + ")");

        JPanel actions = new JPanel(new GridLayout(0, 1, 8, 8));
        if (user instanceof Donor donor) {
            DonationController donationController = new DonationController();
            addButton(actions, "Add donation", () -> new AddDonationView(donationController, donor).show());
            addButton(actions, "My donations (pay / cancel)", () -> new PayDonationView(donationController, donor).show());
        } else if (user instanceof Admin) {
            LookupController lookupController = new LookupController();
            addButton(actions, "Donation statuses", () -> new DonationStatusView(lookupController).show());
            addButton(actions, "Food categories", () -> new FoodCategoryView(lookupController).show());
            addButton(actions, "Units", () -> new UnitView(lookupController).show());
        } else {
            actions.add(new JLabel("Screens for this role are coming soon."));
        }

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            authController.logout();
            frame.dispose();
            loginView.show();
        });

        JPanel panel = new JPanel(new BorderLayout(8, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.add(welcome, BorderLayout.NORTH);
        panel.add(actions, BorderLayout.CENTER);
        panel.add(logoutButton, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    @Override
    public void show() {
        frame.setVisible(true);
    }

    private static void addButton(JPanel panel, String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        panel.add(button);
    }
}
