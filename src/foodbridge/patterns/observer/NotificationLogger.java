package foodbridge.patterns.observer;

import foodbridge.models.operations.Event;
import foodbridge.repositories.NotificationRepository;

// Observer pattern - ConcreteObserver
/**
 * OBSERVER - ConcreteObserver. Logs every event change to the console (as in FoodBridgeColdStart).
 */
public class NotificationLogger implements EventObserver {
    private NotificationRepository repository;

    public NotificationLogger(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void update(Event event, String message) {
        System.out.println("[NOTIFICATION] Event " + event.getId() + " (" + event.getTitle() + "): " + message);
    }
}
