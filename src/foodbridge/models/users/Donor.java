package foodbridge.models.users;

/**
 * Core domain. Makes donations.
 */
public class Donor extends User {
    private String donorType;
    private String organizationName;

    public Donor() {
    }

    public String getDonorType() {
        return donorType;
    }

    public void setDonorType(String type) {
        this.donorType = type;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
}
