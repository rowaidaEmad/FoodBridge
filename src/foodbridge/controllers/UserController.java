package foodbridge.controllers;

import java.util.List;

import foodbridge.models.users.User;
import foodbridge.repositories.UserRepository;

/**
 * Profiles and (admin) user management.
 */
public class UserController {
    private UserRepository userRepository;

    public UserController() {
        // TODO: implement
    }

    public void updateContactInfo(int userId, String phone, String address) {
        // TODO: implement
    }

    public void changeEmail(int userId, String email) {
        // TODO: implement
    }

    public void setActive(int userId, boolean active) {
        // TODO: implement
    }

    public List<User> listUsers() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
