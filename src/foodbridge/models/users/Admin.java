package foodbridge.models.users;

import java.util.HashSet;
import java.util.Set;

/**
 * Core domain. Manages the system.
 */
public class Admin extends User {
    private Set<String> permissions;

    public Admin() {
        permissions = new HashSet<>();
    }

    public boolean hasPermission(String p) {
        return permissions.contains(p);
    }

    public void addPermission(String p) {
        if (p == null || p.isBlank()) {
            throw new IllegalArgumentException("Permission is required");
        }
        permissions.add(p.trim());
    }

    public void removePermission(String p) {
        permissions.remove(p);
    }
}
