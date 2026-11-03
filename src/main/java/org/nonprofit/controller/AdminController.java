package org.nonprofit.controller;

import org.nonprofit.service.AdminService;

import java.sql.SQLException;
import java.util.Set;

public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    //check if user is an admin
    public boolean isAdmin(long adminId) throws SQLException{
        return service.isAdmin(adminId);
    }

    //Get admin permissions
    public Set<String> getPermissions(long adminId)
            throws SQLException {
        return service.getPermissions(adminId);
    }

    // Check a specific permission
    public boolean hasPermission(long adminId, String permission
    ) throws SQLException {
        return service.hasPermission(adminId, permission);
    }

    // Add permission
    public boolean addPermission(long adminId, String permission
    ) throws SQLException {

        return service.addPermission(adminId, permission);
    }

    // Remove permission
    public boolean removePermission(
            long adminId,
            String permission
    ) throws SQLException {

        return service.removePermission(adminId, permission);
    }

    // Count permissions
    public int getPermissionCount(long adminId)
            throws SQLException {
        return service.getPermissionCount(adminId);
    }

    // Check multiple permissions
    public boolean hasAnyPermission(
            long adminId,
            Set<String> permissions
    ) throws SQLException {

        return service.hasAnyPermission(adminId, permissions);
    }
}