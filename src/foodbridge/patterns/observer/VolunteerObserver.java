package foodbridge.patterns.observer;

import foodbridge.models.operations.Event;
import foodbridge.models.operations.Notification;
import foodbridge.repositories.NotificationRepository;

/**
 * OBSERVER - ConcreteObserver. Leaves a notification for one volunteer.
 */
public class VolunteerObserver implements EventObserver {
    private final int volunteerId;
    private final NotificationRepository notificationRepository;

    public VolunteerObserver(int volunteerId, NotificationRepository notificationRepository) {
        this.volunteerId = volunteerId;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void update(Event event, String message) {
        notificationRepository.add(new Notification(volunteerId, event.getTitle() + ": " + message));
    }
}
