package foodbridge.controllers;

import java.util.List;

import foodbridge.models.lookups.Area;
import foodbridge.models.operations.PickupTask;
import foodbridge.repositories.AreaRepository;
import foodbridge.repositories.VolunteerRepository;
import foodbridge.services.PickupTaskService;

/**
 * Volunteer pickup tasks.
 */
public class PickupController {
    private final PickupTaskService pickupTaskService = new PickupTaskService();

    public List<PickupTask> listTasks() {
        return pickupTaskService.listPending();
    }

    /** areaId null = all areas. */
    public List<PickupTask> listAvailable(int volunteerId, Integer areaId) {
        return pickupTaskService.viewAvailableTasks(volunteerId, areaId);
    }

    public List<Area> listAreas() {
        return new AreaRepository().findAll();
    }

    public Area volunteerArea(int volunteerId) {
        return new VolunteerRepository().findById(volunteerId).getArea();
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
