package foodbridge.models.users;

import foodbridge.models.lookups.Area;
import foodbridge.models.operations.PickupTask;

public class Volunteer extends User {
    private String availability;
    private Area area;
    private boolean hasTransport;
    private double totalHours;

    public void acceptTask(PickupTask task) {
        // TODO
    }

    public void declineTask(PickupTask task) {
        // TODO
    }

    public void logHours(double hours) {
        // TODO
    }
}
