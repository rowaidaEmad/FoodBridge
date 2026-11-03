package foodbridge.services;

import java.util.List;

import foodbridge.models.operations.Event;
import foodbridge.models.operations.EventRegistration;
import foodbridge.models.users.Volunteer;
import foodbridge.patterns.observer.EventObserver;
import foodbridge.patterns.observer.EventPublisher;
import foodbridge.patterns.observer.NotificationLogger;
import foodbridge.patterns.observer.VolunteerObserver;
import foodbridge.repositories.EventRegistrationRepository;
import foodbridge.repositories.EventRepository;
import foodbridge.repositories.NotificationRepository;

// Observer pattern - Client
/**
 * OBSERVER - Client. Updates Events, creates EventRegistrations, publishes via EventPublisher.
 * Every registered volunteer is subscribed to their event, so an update or cancellation notifies them.
 * Event logic ported from the FoodBridgeColdStart branch (EventService + EventRegistrationService).
 */
public class EventService {
    private EventRepository repository;
    private EventPublisher publisher;
    private EventRegistrationRepository registrationRepository;
    private NotificationRepository notificationRepository;

    public EventService() {
        this.repository = new EventRepository();
        this.registrationRepository = new EventRegistrationRepository();
        this.notificationRepository = new NotificationRepository();
        this.publisher = new EventPublisher();
        publisher.registerObserver(new NotificationLogger(notificationRepository));
        loadEventObservers();
    }

    /**
     * Subscribes every saved registration, since subscriptions only live in memory. Also called before each
     * notification so volunteers who registered from another window are included (duplicates are ignored).
     */
    private void loadEventObservers() {
        for (EventRegistration registration : registrationRepository.findAll()) {
            subscribe(registration.getEvent().getId(), observerFor(registration.getVolunteer().getId()));
        }
    }

    public long create(Event value) {
        validate(value);
        repository.add(value);
        return value.getId();
    }

    public Event findById(long id) {
        return repository.findById((int) id);
    }

    public List<Event> list() {
        return repository.findAll();
    }

    /** Sends a custom message to everyone following the event. */
    public void announceChange(Event e, String msg) {
        if (msg == null || msg.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }
        loadEventObservers();
        publisher.notifyObservers(e, msg.trim());
    }

    /** Saves the new details (scheduled events only) and notifies the registered volunteers. */
    public boolean update(Event event) {
        validate(event);
        repository.update(event);
        loadEventObservers();
        publisher.updateEvent(event);
        return true;
    }

    public void updateEvent(int id, Event data) {
        data.setId(id);
        update(data);
    }

    /** Returns false if the event is missing or already cancelled. */
    public boolean cancelEvent(long eventId) {
        Event event = repository.findById((int) eventId);
        if (event == null || !repository.cancel((int) eventId)) {
            return false;
        }
        event.setStatus(Event.CANCELLED);
        loadEventObservers();
        publisher.notifyObservers(event, "This event has been cancelled");
        return true;
    }

    public void registerVolunteer(int eventId, int volunteerId) {
        Event event = repository.findById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("No event with id " + eventId);
        }
        if (event.isCancelled()) {
            throw new IllegalStateException("This event has been cancelled");
        }
        Volunteer volunteer = new Volunteer();
        volunteer.setId(volunteerId);
        registrationRepository.add(new EventRegistration(event, volunteer));
        subscribe(eventId, observerFor(volunteerId));
    }

    public List<EventRegistration> listRegistrations(int eventId) {
        return registrationRepository.findByEvent(eventId);
    }

    public List<EventRegistration> listVolunteerRegistrations(int volunteerId) {
        return registrationRepository.findByVolunteer(volunteerId);
    }

    public void subscribe(long eventId, EventObserver observer) {
        publisher.subscribe((int) eventId, observer);
    }

    public void unsubscribe(long eventId, EventObserver observer) {
        publisher.unsubscribe((int) eventId, observer);
    }

    public EventPublisher publisher() {
        return publisher;
    }

    private VolunteerObserver observerFor(int volunteerId) {
        return new VolunteerObserver(volunteerId, notificationRepository);
    }

    private static void validate(Event event) {
        if (event.getTitle() == null || event.getDate() == null || event.getType() == null) {
            throw new IllegalArgumentException("Title, date and type are required");
        }
    }
}
