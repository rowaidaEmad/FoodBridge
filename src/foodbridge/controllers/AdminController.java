package foodbridge.controllers;

import java.util.Set;

import foodbridge.services.AdminService;

/**
 * Admin permissions (uses AdminService). Ported from the FoodBridgeColdStart branch.
 */
public class AdminController {
    private final AdminService service;

    public AdminController() {
        this.service = new AdminService();
    }

    public boolean isAdmin(int adminId) {
        return service.isAdmin(adminId);
    }

    public Set<String> getPermissions(int adminId) {
        return service.getPermissions(adminId);
    }

    public boolean hasPermission(int adminId, String permission) {
        return service.hasPermission(adminId, permission);
    }

    public boolean addPermission(int adminId, String permission) {
        return service.addPermission(adminId, permission);
    }

    public boolean removePermission(int adminId, String permission) {
        return service.removePermission(adminId, permission);
    }

    public int getPermissionCount(int adminId) {
        return service.getPermissionCount(adminId);
    }

    public boolean hasAnyPermission(int adminId, Set<String> permissions) {
        return service.hasAnyPermission(adminId, permissions);
    }
}
