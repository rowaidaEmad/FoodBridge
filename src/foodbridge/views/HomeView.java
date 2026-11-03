package foodbridge.views;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import foodbridge.controllers.AuthController;
import foodbridge.models.users.User;
import foodbridge.views.auth.LoginView;

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
            frame.dispose();
            loginView.show();
        });

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.add(welcome, BorderLayout.CENTER);
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
}
