package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class Admin extends User {
    private Set<String> permissions = new HashSet<>();
    public Admin() { }
    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> value) { this.permissions = value; }
    public boolean hasPermission(String permission) { return permissions.contains(permission); }
    public void addPermission(String permission) { permissions.add(permission); }
    public void removePermission(String permission) { permissions.remove(permission); }
}
