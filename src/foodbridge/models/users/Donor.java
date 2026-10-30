package foodbridge.models.users;

import java.util.ArrayList;
import java.util.List;
import foodbridge.models.donations.Donation;
import foodbridge.models.donations.Receipt;
import foodbridge.models.enums.DonorType;

public class Donor extends User {
    private DonorType donorType;
    private List<Donation> donations = new ArrayList<>();

    public void addDonation(Donation d) {
        // TODO
    }

    public List<Donation> viewDonations() {
        // TODO
        return null;
    }

    public List<Receipt> viewReceipts() {
        // TODO
        return null;
    }
}
