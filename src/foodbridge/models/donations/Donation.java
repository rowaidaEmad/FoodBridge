package foodbridge.models.donations;

import java.time.LocalDate;

import foodbridge.models.lookups.Area;
import foodbridge.models.lookups.DonationStatus;
import foodbridge.models.users.Donor;

// Factory Method pattern - Product
/**
 * FACTORY METHOD - Product. Made by a Donor, has a DonationStatus, located in an Area, has 0..1 Receipt.
 * Saved and loaded by DonationRepository.
 */
public abstract class Donation {
    protected int id;
    protected Donor donor;
    protected LocalDate date;
    protected DonationStatus status;
    protected Area area;

    /** For id-only references loaded by other modules (no donor); never saved. */
    protected Donation() {
    }

    protected Donation(Donor donor) {
        if (donor == null) {
            throw new IllegalArgumentException("A donation needs a donor");
        }
        this.donor = donor;
        this.date = LocalDate.now();
    }

    public int getId() {
        return id;
    }

    /** Set by DonationRepository when the donation is saved or loaded. */
    public void setId(int id) {
        this.id = id;
    }

    public Donor getDonor() {
        return donor;
    }

    public LocalDate getDate() {
        return date;
    }

    /** Set by DonationRepository when loading; new donations are dated today. */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public DonationStatus getStatus() {
        return status;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public void updateStatus(DonationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        this.status = status;
    }

    /**
     * Sets the status to the given Cancelled status, only while the donation is still Posted.
     * Load it with DonationStatusRepository.findByName(DonationStatus.CANCELLED).
     */
    public boolean cancel(DonationStatus cancelled) {
        if (cancelled == null || !DonationStatus.CANCELLED.equals(cancelled.getName())) {
            throw new IllegalArgumentException("Pass the '" + DonationStatus.CANCELLED + "' status");
        }
        if (status == null || !DonationStatus.POSTED.equals(status.getName())) {
            return false;
        }
        updateStatus(cancelled);
        return true;
    }

    public String getDetails() {
        String type = getType();
        return Character.toUpperCase(type.charAt(0)) + type.substring(1) + " donation - " + statusName();
    }

    public abstract String getType();

    protected String statusName() {
        return status == null ? "New" : status.getName();
    }
}
