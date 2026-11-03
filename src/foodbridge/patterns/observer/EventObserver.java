package foodbridge.patterns.observer;

import foodbridge.models.operations.Event;

/**
 * OBSERVER - Observer.
 */
public interface EventObserver {
    void update(Event event, String message);
}
