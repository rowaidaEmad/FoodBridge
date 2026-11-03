package foodbridge.patterns.observer;

import java.util.List;
import java.util.Map;

import foodbridge.models.operations.Event;

/**
 * OBSERVER - ConcreteSubject.
 */
public class EventPublisher implements Subject {
    private Map<Integer, List<EventObserver>> observersByEvent;

    public EventPublisher() {
        // TODO: implement
    }

    @Override
    public void registerObserver(EventObserver o) {
        // TODO: implement
    }

    @Override
    public void removeObserver(EventObserver o) {
        // TODO: implement
    }

    @Override
    public void notifyObservers() {
        // TODO: implement
    }

    public void updateEvent(Event event) {
        // TODO: implement
    }
}
