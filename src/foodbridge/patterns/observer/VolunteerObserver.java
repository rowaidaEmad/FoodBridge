package foodbridge.patterns.observer;

import java.util.Objects;

import foodbridge.models.operations.Event;
import foodbridge.models.operations.Notification;
import foodbridge.repositories.NotificationRepository;

// Observer pattern - ConcreteObserver
/**
 * OBSERVER - ConcreteObserver. One per registered volunteer: saves a Notification the volunteer sees
 * in the Notifications screen.
 */
public class VolunteerObserver implements EventObserver {
    private int volunteerId;
    private NotificationRepository repository;

    public VolunteerObserver(int volunteerId) {
        this(volunteerId, new NotificationRepository());
    }

    public VolunteerObserver(int volunteerId, NotificationRepository repository) {
        this.volunteerId = volunteerId;
        this.repository = repository;
    }

    @Override
    public void update(Event event, String message) {
        System.out.println("Volunteer " + volunteerId + ", event " + event.getId() + ": " + message);
        repository.add(new Notification(volunteerId, "Event \"" + event.getTitle() + "\": " + message));
    }

    public int getVolunteerId() {
        return volunteerId;
    }

    /** Same volunteer = same observer, so a volunteer is never subscribed twice to one event. */
    @Override
    public boolean equals(Object other) {
        return other instanceof VolunteerObserver o && o.volunteerId == volunteerId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(volunteerId);
    }
}
