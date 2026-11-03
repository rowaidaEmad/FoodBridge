package foodbridge.models.users;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Core domain. Manages the system.
 * Permissions are stored in admin_permissions and loaded by AdminRepository (ported from FoodBridgeColdStart).
 */
public class Admin extends User {
    private Set<String> permissions;

    public Admin() {
        this.permissions = new HashSet<>();
    }

    public boolean hasPermission(String p) {
        return permissions.contains(normalize(p));
    }

    public void addPermission(String p) {
        permissions.add(normalize(p));
    }

    public void removePermission(String p) {
        permissions.remove(normalize(p));
    }

    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    /** Trimmed, upper-case, at most 100 characters (the admin_permissions column size). */
    public static String normalize(String permission) {
        if (permission == null || permission.isBlank()) {
            throw new IllegalArgumentException("Permission cannot be empty");
        }
        String normalized = permission.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("Permission cannot exceed 100 characters");
        }
        return normalized;
    }
}
