package foodbridge.services;

import java.util.List;

import foodbridge.models.operations.Event;
import foodbridge.patterns.observer.EventPublisher;
import foodbridge.repositories.EventRepository;

/**
 * OBSERVER - Client. Updates Events, creates EventRegistrations, publishes via EventPublisher.
 */
public class EventService {
    private EventRepository repository;
    private EventPublisher publisher;

    public EventService() {
        // TODO: implement
    }

    public void updateEvent(int id, Event data) {
        // TODO: implement
    }

    public void registerVolunteer(int eventId, int volunteerId) {
        // TODO: implement
    }

    public long create(Event value) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Event findById(long id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<Event> list() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void announceChange(Event e, String msg) {
        // TODO: implement
    }

    public boolean update(Event event) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean cancelEvent(long eventId) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
