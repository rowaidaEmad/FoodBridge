package foodbridge.patterns.observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import foodbridge.models.operations.Event;

/**
 * OBSERVER - ConcreteSubject.
 */
public class EventPublisher implements Subject {
    private static final int ALL_EVENTS = 0;

    private final Map<Integer, List<EventObserver>> observersByEvent = new HashMap<>();
    private Event currentEvent;
    private String currentMessage;

    @Override
    public void registerObserver(EventObserver o) {
        registerObserver(ALL_EVENTS, o);
    }

    public void registerObserver(int eventId, EventObserver o) {
        observersByEvent.computeIfAbsent(eventId, id -> new ArrayList<>()).add(o);
    }

    @Override
    public void removeObserver(EventObserver o) {
        observersByEvent.values().forEach(observers -> observers.remove(o));
    }

    public void clearObservers(int eventId) {
        observersByEvent.remove(eventId);
    }

    @Override
    public void notifyObservers() {
        notifyList(ALL_EVENTS);
        notifyList(currentEvent.getId());
    }

    public void updateEvent(Event event, String message) {
        currentEvent = event;
        currentMessage = message;
        notifyObservers();
    }

    private void notifyList(int key) {
        for (EventObserver observer : observersByEvent.getOrDefault(key, List.of())) {
            observer.update(currentEvent, currentMessage);
        }
    }
}
