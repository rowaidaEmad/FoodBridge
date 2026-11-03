package foodbridge.models.users;

import java.util.Set;

/**
 * Core domain. Manages the system.
 */
public class Admin extends User {
    private Set<String> permissions;

    public Admin() {
        // TODO: implement
    }

    public boolean hasPermission(String p) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void addPermission(String p) {
        // TODO: implement
    }

    public void removePermission(String p) {
        // TODO: implement
    }
}
