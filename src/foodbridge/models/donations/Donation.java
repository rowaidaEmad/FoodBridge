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
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void updateStatus(DonationStatus s) {
        // TODO: implement
    }

    public abstract String getType();
}
