package foodbridge.patterns.observer;

/**
 * OBSERVER - Subject. Has 0..* EventObserver.
 */
public interface Subject {
    void registerObserver(EventObserver o);

    void removeObserver(EventObserver o);

    void notifyObservers();
}
