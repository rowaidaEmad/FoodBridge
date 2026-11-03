package foodbridge.controllers;

import java.util.List;

import foodbridge.models.lookups.EventType;
import foodbridge.models.operations.Event;
import foodbridge.models.operations.EventRegistration;
import foodbridge.patterns.observer.EventObserver;
import foodbridge.repositories.EventTypeRepository;
import foodbridge.services.EventService;

/**
 * Events and volunteer registration (uses EventService).
 * Also holds the FoodBridgeColdStart EventController / EventRegistrationController operations.
 */
public class EventController {
    private EventService eventService;
    private EventTypeRepository eventTypeRepository;

    public EventController() {
        eventService = new EventService();
        eventTypeRepository = new EventTypeRepository();
    }

    public long createEvent(Event event) {
        return eventService.create(event);
    }

    public void updateEvent(int id, Event data) {
        eventService.updateEvent(id, data);
    }

    public void registerVolunteer(int eventId, int volunteerId) {
        eventService.registerVolunteer(eventId, volunteerId);
    }

    public boolean cancelEvent(long eventId) {
        return eventService.cancelEvent(eventId);
    }

    public List<Event> listEvents() {
        return eventService.list();
    }

    public Event getEvent(long id) {
        return eventService.findById(id);
    }

    public void announceChange(Event event, String message) {
        eventService.announceChange(event, message);
    }

    public List<EventRegistration> listRegistrations(int eventId) {
        return eventService.listRegistrations(eventId);
    }

    public List<EventRegistration> listVolunteerRegistrations(int volunteerId) {
        return eventService.listVolunteerRegistrations(volunteerId);
    }

    public List<EventType> listEventTypes() {
        return eventTypeRepository.findAll();
    }

    public void subscribe(long eventId, EventObserver observer) {
        eventService.subscribe(eventId, observer);
    }

    public void unsubscribe(long eventId, EventObserver observer) {
        eventService.unsubscribe(eventId, observer);
    }
}
