package foodbridge.models.lookups;

/**
 * Lookup table (CRUD). Saved and loaded by DonationStatusRepository.
 */
public class DonationStatus {
    public static final String POSTED = "Posted";
    public static final String PICKED_UP = "Picked up";
    public static final String DELIVERED = "Delivered";
    public static final String EXPIRED = "Expired";
    public static final String CANCELLED = "Cancelled";

    private int id;
    private String name;

    public DonationStatus() {
    }

    public DonationStatus(String name) {
        setName(name);
    }

    public DonationStatus(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    /** Set by DonationStatusRepository when the status is saved. */
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        this.name = name.trim();
    }
}
