package foodbridge.controllers;

import java.util.List;

import foodbridge.models.operations.Event;
import foodbridge.models.operations.EventRegistration;
import foodbridge.services.EventService;

/**
 * Events and volunteer registration (uses EventService).
 */
public class EventController {
    private final EventService eventService = new EventService();

    public long createEvent(Event event) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void updateEvent(int id, Event data) {
        // TODO: implement
    }

    public void registerVolunteer(int eventId, int volunteerId) {
        eventService.registerVolunteer(eventId, volunteerId);
    }

    public List<EventRegistration> listRegistrations(int volunteerId) {
        return eventService.listRegistrations(volunteerId);
    }

    public List<EventRegistration> listRegistrationsForEvent(int eventId) {
        return eventService.listRegistrationsForEvent(eventId);
    }

    public void markAttendance(int registrationId, String status, double hours) {
        eventService.markAttendance(registrationId, status, hours);
    }

    public void announceChange(int eventId, String message) {
        eventService.announceChange(eventService.findById(eventId), message);
    }

    public boolean cancelEvent(long eventId) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<Event> listEvents() {
        return eventService.list();
    }
}
