package foodbridge.models.operations;

import java.time.LocalDateTime;

import foodbridge.models.donations.FoodDonation;
import foodbridge.models.enums.PickupTaskStatus;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.users.Volunteer;

/**
 * Core domain. A Volunteer picks up a FoodDonation and delivers it to a Beneficiary.
 */
public class PickupTask {
    private int id;
    private FoodDonation donation;
    private Volunteer volunteer;   // optional until assigned
    private Beneficiary beneficiary;   // optional
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private PickupTaskStatus status;
    private double hoursSpent;

    public PickupTask() {
        // TODO: implement
    }

    public void assignVolunteer(Volunteer v) {
        // TODO: implement
    }

    public void markPickedUp() {
        // TODO: implement
    }

    public void markDelivered() {
        // TODO: implement
    }

    public double calculateHours() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
