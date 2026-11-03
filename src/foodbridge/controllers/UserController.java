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
        userRepository = new UserRepository();
    }

    public void updateContactInfo(int userId, String phone, String address) {
        User user = requireUser(userId);
        user.updateContactInfo(phone, address);
        userRepository.updateAccount(user);
    }

    public void changeEmail(int userId, String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Enter an email address.");
        }
        User sameEmail = userRepository.findByEmail(email.trim());
        if (sameEmail != null && sameEmail.getId() != userId) {
            throw new IllegalArgumentException("Another account already uses this email.");
        }
        User user = requireUser(userId);
        user.changeEmail(email.trim());
        userRepository.updateAccount(user);
    }

    public void setActive(int userId, boolean active) {
        User user = requireUser(userId);
        user.setActive(active);
        userRepository.updateAccount(user);
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    private User requireUser(int userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("No user with id " + userId);
        }
        return user;
    }
}
