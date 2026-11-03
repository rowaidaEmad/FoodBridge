package foodbridge.patterns.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import foodbridge.models.operations.Event;

// Observer pattern - ConcreteSubject
/**
 * OBSERVER - ConcreteSubject.
 * Observers subscribe to one event (volunteers registered for it) or to every event (registerObserver,
 * e.g. NotificationLogger). Per-event subscriptions come from the FoodBridgeColdStart branch.
 */
public class EventPublisher implements Subject {
    /** Key 0 holds the observers of every event. */
    private static final int ALL_EVENTS = 0;

    private Map<Integer, List<EventObserver>> observersByEvent;
    private Event lastEvent;
    private String lastMessage;

    public EventPublisher() {
        this.observersByEvent = new HashMap<>();
    }

    /** Observes every event. */
    @Override
    public void registerObserver(EventObserver o) {
        subscribe(ALL_EVENTS, o);
    }

    @Override
    public void removeObserver(EventObserver o) {
        unsubscribe(ALL_EVENTS, o);
    }

    /** Re-sends the last change (see updateEvent / notifyObservers(Event, String)). */
    @Override
    public void notifyObservers() {
        if (lastEvent != null) {
            notifyObservers(lastEvent, lastMessage);
        }
    }

    public void updateEvent(Event event) {
        notifyObservers(event, "Event details have been updated");
    }

    public void subscribe(int eventId, EventObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer is required");
        }
        List<EventObserver> list = observersByEvent.computeIfAbsent(eventId, k -> new ArrayList<>());
        if (!list.contains(observer)) {
            list.add(observer);
        }
    }

    public void unsubscribe(int eventId, EventObserver observer) {
        observersByEvent.getOrDefault(eventId, Collections.emptyList()).remove(observer);
    }

    /** Notifies the event's own observers, then the observers of every event. */
    public void notifyObservers(Event event, String message) {
        lastEvent = event;
        lastMessage = message;
        List<EventObserver> targets = new ArrayList<>(observersByEvent.getOrDefault(event.getId(), List.of()));
        targets.addAll(observersByEvent.getOrDefault(ALL_EVENTS, List.of()));
        // Copy first: an observer may unsubscribe while being notified.
        for (EventObserver o : targets) {
            o.update(event, message);
        }
    }

    public int countObservers(int eventId) {
        return observersByEvent.getOrDefault(eventId, List.of()).size();
    }
}
