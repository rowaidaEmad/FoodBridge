package foodbridge;

import javax.swing.SwingUtilities;

import foodbridge.controllers.AuthController;
import foodbridge.views.auth.LoginView;

/**
 * Entry point. Wire the views, controllers and services here once modules are implemented.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("FoodBridge started");
        // TODO: route to the right panel by role from HomeView.
        SwingUtilities.invokeLater(() -> new LoginView(new AuthController()).show());
    }
}
