package foodbridge.models.users;

import foodbridge.models.enums.Role;

public abstract class User {
    protected int id;
    protected String name;
    protected String email;
    protected String password;
    protected String phone;
    protected String address;
    protected Role role;

    public boolean login(String email, String password) {
        // TODO
        return false;
    }

    public void logout() {
        // TODO
    }

    public void updateProfile() {
        // TODO
    }
}
