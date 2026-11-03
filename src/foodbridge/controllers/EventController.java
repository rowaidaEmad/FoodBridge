package foodbridge.controllers;

import java.util.List;

import foodbridge.models.operations.Event;
import foodbridge.services.EventService;

/**
 * Events and volunteer registration (uses EventService).
 */
public class EventController {
    private EventService eventService;

    public EventController() {
        // TODO: implement
    }

    public long createEvent(Event event) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void updateEvent(int id, Event data) {
        // TODO: implement
    }

    public void registerVolunteer(int eventId, int volunteerId) {
        // TODO: implement
    }

    public boolean cancelEvent(long eventId) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<Event> listEvents() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
