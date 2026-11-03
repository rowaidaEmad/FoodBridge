package org.nonprofit.service;

import org.nonprofit.repository.Admin;
import java.sql.SQLException;
import java.util.*;

public class AdminService {

    private final Admin repository;

    public AdminService(Admin repository) {
        this.repository = repository;
    }

    public boolean isAdmin(long adminId) throws SQLException {
        return repository.isAdmin(adminId);
    }

    //get all admin's permissions
    public Set<String> getPermissions(long adminId)throws SQLException {

        validateAdmin(adminId);
        return repository.findPermissions(adminId);
    }

    public boolean hasPermission(long adminId, String permission)
            throws SQLException {

        validateAdmin(adminId);
        return repository.hasPermission(adminId, normalizePermission(permission));
    }

    public boolean addPermission(long adminId, String permission)
            throws SQLException {

        validateAdmin(adminId);
        return repository.addPermission(adminId, normalizePermission(permission)
        );
    }

    public boolean removePermission(long adminId, String permission)
            throws SQLException {

        validateAdmin(adminId);
        return repository.removePermission(adminId, normalizePermission(permission));
    }

    public int getPermissionCount(long adminId)
            throws SQLException {

        return getPermissions(adminId).size();
    }

    public boolean hasAnyPermission(long adminId, Set<String> permissions
    ) throws SQLException {

        validateAdmin(adminId);
        for (String permission : permissions) {
            if (repository.hasPermission(adminId, normalizePermission(permission))) {
                return true;
            }
        }
        return false;
    }

    // Validate that the admin exists and is active
    private void validateAdmin(long adminId)
            throws SQLException {

        if (!repository.isAdmin(adminId)) {
            throw new IllegalArgumentException("Active admin not found with ID: " + adminId);
        }
    }

    private String normalizePermission(String permission) {

        if (permission == null || permission.isBlank()) {
            throw new IllegalArgumentException("Permission cannot be empty");
        }
        String normalized = permission.trim().toUpperCase(Locale.ROOT);
        if(normalized.length() > 100){
            throw new IllegalArgumentException("Permission cannot exceed 100 characters");
        }

        return normalized;
    }
}