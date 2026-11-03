package foodbridge.services;

import java.util.Set;

import foodbridge.models.users.Admin;
import foodbridge.repositories.AdminRepository;

/**
 * Admin permissions. Ported from the FoodBridgeColdStart branch: every call checks the admin is active,
 * and permissions are stored trimmed and upper-case (Admin.normalize).
 */
public class AdminService {
    private final AdminRepository repository;

    public AdminService() {
        this(new AdminRepository());
    }

    public AdminService(AdminRepository repository) {
        this.repository = repository;
    }

    public boolean isAdmin(int adminId) {
        return repository.isAdmin(adminId);
    }

    public Set<String> getPermissions(int adminId) {
        validateAdmin(adminId);
        return repository.findPermissions(adminId);
    }

    public boolean hasPermission(int adminId, String permission) {
        validateAdmin(adminId);
        return repository.hasPermission(adminId, Admin.normalize(permission));
    }

    public boolean addPermission(int adminId, String permission) {
        validateAdmin(adminId);
        return repository.addPermission(adminId, Admin.normalize(permission));
    }

    public boolean removePermission(int adminId, String permission) {
        validateAdmin(adminId);
        return repository.removePermission(adminId, Admin.normalize(permission));
    }

    public int getPermissionCount(int adminId) {
        return getPermissions(adminId).size();
    }

    public boolean hasAnyPermission(int adminId, Set<String> permissions) {
        validateAdmin(adminId);
        for (String permission : permissions) {
            if (repository.hasPermission(adminId, Admin.normalize(permission))) {
                return true;
            }
        }
        return false;
    }

    /** Copies the stored permissions into the Admin model. */
    public void loadPermissions(Admin admin) {
        for (String permission : getPermissions(admin.getId())) {
            admin.addPermission(permission);
        }
    }

    private void validateAdmin(int adminId) {
        if (!repository.isAdmin(adminId)) {
            throw new IllegalArgumentException("Active admin not found with ID: " + adminId);
        }
    }
}
