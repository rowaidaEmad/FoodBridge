package foodbridge.models.operations;

import java.time.LocalDate;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.lookups.FoodCategory;

public class DonationRequest {
    private int id;
    private Beneficiary beneficiary;
    private FoodCategory category;
    private int quantity;
    private LocalDate date;
    private String status;

    public void submit() {
        // TODO
    }

    public void cancel() {
        // TODO
    }
}
