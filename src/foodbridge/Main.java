package foodbridge;

import javax.swing.SwingUtilities;

import foodbridge.controllers.AuthController;
import foodbridge.views.auth.LoginView;

/**
 * Entry point. HomeView provides role-based navigation after login.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("FoodBridge started");
        SwingUtilities.invokeLater(() -> new LoginView(new AuthController()).show());
    }
}
