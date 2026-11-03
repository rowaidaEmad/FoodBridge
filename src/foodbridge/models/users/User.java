package foodbridge.models.users;

import java.time.LocalDateTime;

import foodbridge.models.lookups.Area;

/**
 * Core domain. Base class of Volunteer, Donor, Beneficiary, Admin. A user lives in one Area.
 */
public abstract class User {
    protected int id;
    protected String name;
    protected String email;
    protected String passwordHash;
    protected String phone;
    protected String address;
    protected Area area;
    protected boolean active = true;
    protected LocalDateTime createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void updateContactInfo(String phone, String address) {
        this.phone = phone;
        this.address = address;
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
