package foodbridge.models.operations;

import java.time.LocalDateTime;
import foodbridge.models.donations.FoodDonation;
import foodbridge.models.users.Volunteer;
import foodbridge.models.users.Beneficiary;

public class PickupTask {
    private int id;
    private FoodDonation donation;
    private Volunteer volunteer;
    private Beneficiary beneficiary;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private String status;

    public void assign(Volunteer v, Beneficiary b) {
        // TODO
    }

    public void markPickedUp() {
        // TODO
    }

    public Distribution markDelivered() {
        // TODO
        return null;
    }
}
