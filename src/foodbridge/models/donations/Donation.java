package foodbridge.models.donations;

import java.time.LocalDate;

import foodbridge.models.lookups.Area;
import foodbridge.models.lookups.DonationStatus;
import foodbridge.models.users.Donor;

/**
 * FACTORY METHOD - Product. Made by a Donor, has a DonationStatus, located in an Area, has 0..1 Receipt.
 */
public abstract class Donation {
    protected int id;
    protected Donor donor;
    protected LocalDate date;
    protected DonationStatus status;
    protected Area area;

    public int getId() {
        return id;
    }

    public void updateStatus(DonationStatus s) {
        status = s;
    }

    public void setId(int id) { this.id = id; }

    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) { this.date = date; }

    public abstract String getType();
}
