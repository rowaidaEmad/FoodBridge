package foodbridge.controllers;

import java.util.List;

import foodbridge.models.operations.PickupTask;
import foodbridge.models.users.Volunteer;
import foodbridge.repositories.DistributionRepository;
import foodbridge.repositories.PickupTaskRepository;

/**
 * Volunteer pickup tasks and distributions.
 */
public class PickupController {
    private PickupTaskRepository pickupTaskRepository;
    private DistributionRepository distributionRepository;

    public PickupController() {
        // TODO: implement
    }

    public List<PickupTask> listTasks() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void assignVolunteer(int taskId, Volunteer volunteer) {
        // TODO: implement
    }

    public void markPickedUp(int taskId) {
        // TODO: implement
    }

    public void markDelivered(int taskId) {
        // TODO: implement
    }
}
