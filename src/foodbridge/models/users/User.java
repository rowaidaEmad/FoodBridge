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
    protected boolean active;
    protected LocalDateTime createdAt;

    public int getId() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public String getEmail() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void updateContactInfo(String phone, String address) {
        // TODO: implement
    }

    public void changeEmail(String email) {
        // TODO: implement
    }

    public void setActive(boolean active) {
        // TODO: implement
    }
}
