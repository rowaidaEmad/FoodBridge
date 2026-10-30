package foodbridge.models.donations;

import java.time.LocalDate;
import foodbridge.models.users.Donor;
import foodbridge.models.lookups.DonationStatus;
import foodbridge.models.lookups.Area;

public abstract class Donation {
    protected int id;
    protected Donor donor;
    protected LocalDate date;
    protected DonationStatus status;
    protected Area area;

    public void updateStatus(DonationStatus newStatus) {
        // TODO
    }

    public String getDetails() {
        // TODO
        return "";
    }
}
