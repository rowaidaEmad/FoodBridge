package foodbridge.models.operations;

import java.time.LocalDate;
import foodbridge.models.donations.FoodDonation;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.users.Volunteer;

public class Distribution {
    private int id;
    private FoodDonation donation;
    private Beneficiary beneficiary;
    private Volunteer volunteer;
    private LocalDate date;
    private int quantity;

    public void record() {
        // TODO
    }
}
