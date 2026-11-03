package foodbridge.models.users;

/**
 * Core domain. Receives distributions and submits donation requests.
 */
public class Beneficiary extends User {
    private int householdSize;
    private String needs;

    public Beneficiary() {
    }

    public int getHouseholdSize() {
        return householdSize;
    }

    public void setHouseholdSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Household size must be at least 1");
        }
        this.householdSize = size;
    }

    public String getNeeds() {
        return needs;
    }

    public void updateNeeds(String needs) {
        this.needs = needs;
    }
}
