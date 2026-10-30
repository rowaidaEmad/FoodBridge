package foodbridge.controllers;

import foodbridge.models.donations.FoodDonation;
import foodbridge.models.lookups.Area;
import foodbridge.models.operations.PickupTask;
import foodbridge.models.users.Volunteer;
import java.util.List;

public class PickupController {

    public List<FoodDonation> availablePickups(Area area) {
        // TODO
        return null;
    }

    public void assignTask(PickupTask task, Volunteer v) {
        // TODO
    }

    public void markPickedUp(PickupTask task) {
        // TODO
    }

    public void markDelivered(PickupTask task) {
        // TODO
    }
}
