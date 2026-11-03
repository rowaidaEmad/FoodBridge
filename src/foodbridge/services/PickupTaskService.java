package foodbridge.services;

import java.util.List;

import foodbridge.models.enums.PickupTaskStatus;
import foodbridge.models.operations.Notification;
import foodbridge.models.operations.PickupTask;
import foodbridge.models.users.Volunteer;
import foodbridge.repositories.NotificationRepository;
import foodbridge.repositories.PickupTaskRepository;
import foodbridge.repositories.VolunteerRepository;

/**
 * Accepting, assigning and completing pickup tasks.
 */
public class PickupTaskService {
    private final PickupTaskRepository pickupTaskRepository = new PickupTaskRepository();
    private final VolunteerRepository volunteerRepository = new VolunteerRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();

    public List<PickupTask> listPending() {
        pickupTaskRepository.openTasksForNewFoodDonations();
        return pickupTaskRepository.findAvailable();
    }

    /** Pending tasks the volunteer qualifies for; areaId filters by area, null shows every area. */
    public List<PickupTask> viewAvailableTasks(int volunteerId, Integer areaId) {
        Volunteer volunteer = requireVolunteer(volunteerId);
        pickupTaskRepository.openTasksForNewFoodDonations();
        List<PickupTask> pending = areaId == null
                ? pickupTaskRepository.findAvailable()
                : pickupTaskRepository.findAvailableInArea(areaId);
        return pending.stream()
                .filter(task -> isQualified(volunteer, task))
                .toList();
    }

    public List<PickupTask> viewMyTasks(int volunteerId) {
        return pickupTaskRepository.findByVolunteer(volunteerId);
    }

    /** Returns false if the task was already taken. */
    public boolean acceptTask(int taskId, int volunteerId) {
        PickupTask task = requireTask(taskId);
        Volunteer volunteer = requireVolunteer(volunteerId);
        requireQualified(volunteer, task);
        if (task.getStatus() != PickupTaskStatus.PENDING) {
            return false;
        }
        task.assignVolunteer(volunteer);
        return pickupTaskRepository.claim(taskId, volunteerId);
    }

    public void declineTask(int taskId, int volunteerId) {
        if (!pickupTaskRepository.release(taskId, volunteerId)) {
            throw new IllegalStateException("Only a task assigned to you and not yet picked up can be declined.");
        }
    }

    public void markPickedUp(int taskId, int volunteerId) {
        PickupTask task = requireOwnTask(taskId, volunteerId);
        task.markPickedUp();
        pickupTaskRepository.savePickedUp(task);
    }

    public void markDelivered(int taskId, int volunteerId) {
        PickupTask task = requireOwnTask(taskId, volunteerId);
        task.markDelivered();
        pickupTaskRepository.completeDelivery(task);
    }

    public void assignTask(int taskId, int volunteerId) {
        PickupTask task = requireTask(taskId);
        requireQualified(requireVolunteer(volunteerId), task);
        if (!pickupTaskRepository.claim(taskId, volunteerId)) {
            throw new IllegalStateException("This task is already taken.");
        }
        notificationRepository.add(new Notification(volunteerId, "You were assigned pickup task #" + taskId + "."));
    }

    private boolean isQualified(Volunteer volunteer, PickupTask task) {
        String skill = task.getRequiredSkill();
        return volunteer.hasTransport() && (skill == null || volunteer.hasSkill(skill));
    }

    private void requireQualified(Volunteer volunteer, PickupTask task) {
        if (!isQualified(volunteer, task)) {
            throw new IllegalArgumentException(volunteer.getName() + " does not meet this task's requirements.");
        }
    }

    private PickupTask requireTask(int taskId) {
        PickupTask task = pickupTaskRepository.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("No task with id " + taskId);
        }
        return task;
    }

    private PickupTask requireOwnTask(int taskId, int volunteerId) {
        PickupTask task = requireTask(taskId);
        if (task.getVolunteer() == null || task.getVolunteer().getId() != volunteerId) {
            throw new IllegalArgumentException("This task is not assigned to you.");
        }
        return task;
    }

    private Volunteer requireVolunteer(int volunteerId) {
        Volunteer volunteer = volunteerRepository.findById(volunteerId);
        if (volunteer == null) {
            throw new IllegalArgumentException("No volunteer with id " + volunteerId);
        }
        return volunteer;
    }
}
