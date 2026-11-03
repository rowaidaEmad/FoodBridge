package foodbridge.views.auth;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import foodbridge.controllers.AuthController;
import foodbridge.models.users.User;
import foodbridge.views.HomeView;
import foodbridge.views.View;

/**
 * Screen; talks only to AuthController.
 */
public class LoginView implements View {
    private AuthController authController;
    private final JFrame frame = new JFrame("FoodBridge - Login");
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);

    public LoginView(AuthController authController) {
        this.authController = authController;

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Create account");
        loginButton.addActionListener(e -> onLogin());
        registerButton.addActionListener(e -> new RegisterView(authController).show());

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(registerButton);
        panel.add(loginButton);

        frame.getRootPane().setDefaultButton(loginButton);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    @Override
    public void show() {
        passwordField.setText("");
        frame.setVisible(true);
    }

    private void onLogin() {
        User user;
        try {
            user = authController.login(emailField.getText(), new String(passwordField.getPassword()));
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Login failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        frame.setVisible(false);
        new HomeView(user, authController, this).show();
    }
}
