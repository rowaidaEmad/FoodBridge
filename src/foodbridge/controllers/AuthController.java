package foodbridge.controllers;

import foodbridge.models.users.User;
import foodbridge.repositories.UserRepository;

/**
 * Login / register / logout.
 */
public class AuthController {
    private UserRepository userRepository;

    public AuthController() {
        // TODO: implement
    }

    public User login(String email, String password) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void register(User user) {
        // TODO: implement
    }

    public void logout() {
        // TODO: implement
    }
}
