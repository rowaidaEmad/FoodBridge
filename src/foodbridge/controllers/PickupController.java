package foodbridge.controllers;

import java.util.List;

import foodbridge.models.operations.PickupTask;
import foodbridge.services.PickupTaskService;

/**
 * Volunteer pickup tasks.
 */
public class PickupController {
    private final PickupTaskService pickupTaskService = new PickupTaskService();

    public List<PickupTask> listTasks() {
        return pickupTaskService.listPending();
    }

    public List<PickupTask> listAvailable(int volunteerId) {
        return pickupTaskService.viewAvailableTasks(volunteerId);
    }

    public List<PickupTask> listMine(int volunteerId) {
        return pickupTaskService.viewMyTasks(volunteerId);
    }

    public boolean acceptTask(int taskId, int volunteerId) {
        return pickupTaskService.acceptTask(taskId, volunteerId);
    }

    public void declineTask(int taskId, int volunteerId) {
        pickupTaskService.declineTask(taskId, volunteerId);
    }

    public void assignVolunteer(int taskId, int volunteerId) {
        pickupTaskService.assignTask(taskId, volunteerId);
    }

    public void markPickedUp(int taskId, int volunteerId) {
        pickupTaskService.markPickedUp(taskId, volunteerId);
    }

    public void markDelivered(int taskId, int volunteerId) {
        pickupTaskService.markDelivered(taskId, volunteerId);
    }
}
