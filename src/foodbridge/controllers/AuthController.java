package foodbridge.controllers;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import foodbridge.models.lookups.Area;
import foodbridge.models.users.User;
import foodbridge.repositories.AreaRepository;
import foodbridge.repositories.UserRepository;

/**
 * Login / register / logout.
 * Passwords are stored as salted PBKDF2 hashes: "iterations:salt:hash" (Base64).
 */
public class AuthController {
    private static final int ITERATIONS = 65536;
    private static final int KEY_BITS = 256;

    private UserRepository userRepository;
    private User currentUser;

    public AuthController() {
        userRepository = new UserRepository();
    }

    /** Returns the logged-in user, or throws IllegalArgumentException with a message for the screen. */
    public User login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Enter your email and password.");
        }
        User user = userRepository.findByEmail(email.trim());
        if (user == null || !matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Wrong email or password.");
        }
        if (!user.isActive()) {
            throw new IllegalArgumentException("This account has been deactivated.");
        }
        currentUser = user;
        return user;
    }

    public List<Area> listAreas() {
        return new AreaRepository().findAll();
    }

    public void register(User user, String password) {
        if (user.getName() == null || user.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (user.getEmail() == null || !user.getEmail().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        // Admins do not take part in area matching.
        if (!(user instanceof foodbridge.models.users.Admin) && user.getArea() == null) {
            throw new IllegalArgumentException("Choose your area.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        user.setPasswordHash(hash(password));
        userRepository.add(user);
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private static String hash(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, ITERATIONS);
        Base64.Encoder b64 = Base64.getEncoder();
        return ITERATIONS + ":" + b64.encodeToString(salt) + ":" + b64.encodeToString(hash);
    }

    private static boolean matches(String password, String stored) {
        String[] parts = stored == null ? new String[0] : stored.split(":");
        if (parts.length != 3) {
            return false;
        }
        Base64.Decoder b64 = Base64.getDecoder();
        byte[] expected = b64.decode(parts[2]);
        byte[] actual = pbkdf2(password, b64.decode(parts[1]), Integer.parseInt(parts[0]));
        return MessageDigest.isEqual(expected, actual);
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Password hashing is not available", e);
        }
    }
}
