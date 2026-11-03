package foodbridge.patterns.observer;

import foodbridge.models.operations.Event;
import foodbridge.repositories.NotificationRepository;

/**
 * OBSERVER - ConcreteObserver.
 */
public class NotificationLogger implements EventObserver {
    private NotificationRepository repository;

    public NotificationLogger(NotificationRepository repository) {
        // TODO: implement
    }

    @Override
    public void update(Event event, String message) {
        // TODO: implement
    }
}
