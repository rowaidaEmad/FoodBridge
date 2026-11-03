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
import foodbridge.repositories.VolunteerRepository;

// Observer pattern - Client
/**
 * OBSERVER - Client. Updates Events, creates EventRegistrations, publishes via EventPublisher.
 * Every registered volunteer is subscribed to their event, so an update or cancellation notifies them.
 */
public class EventService {
    private final EventRepository repository;
    private final EventRegistrationRepository registrationRepository;
    private final VolunteerRepository volunteerRepository;
    private final NotificationRepository notificationRepository;
    private final EventPublisher publisher;

    public EventService() {
        repository = new EventRepository();
        registrationRepository = new EventRegistrationRepository();
        volunteerRepository = new VolunteerRepository();
        notificationRepository = new NotificationRepository();
        publisher = new EventPublisher();
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
        return requireEvent(Math.toIntExact(id));
    }

    public List<Event> list() {
        return repository.findAll();
    }

    /** Sends a custom message to everyone following the event. */
    public void announceChange(Event e, String msg) {
        if (msg == null || msg.isBlank()) {
            throw new IllegalArgumentException("Enter a message to announce.");
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
        int id = Math.toIntExact(eventId);
        Event event = repository.findById(id);
        if (event == null || !repository.cancel(id)) {
            return false;
        }
        event.setStatus(Event.CANCELLED);
        loadEventObservers();
        publisher.notifyObservers(event, "This event has been cancelled");
        return true;
    }

    public void registerVolunteer(int eventId, int volunteerId) {
        Event event = requireEvent(eventId);
        if (event.isCancelled()) {
            throw new IllegalStateException("This event has been cancelled.");
        }
        Volunteer volunteer = volunteerRepository.findById(volunteerId);
        if (volunteer == null) {
            throw new IllegalArgumentException("No volunteer with id " + volunteerId);
        }
        String skill = event.getRequiredSkill();
        if (skill != null && !volunteer.hasSkill(skill)) {
            throw new IllegalArgumentException(event.getTitle() + " requires the skill " + skill + ".");
        }
        if (registrationRepository.exists(eventId, volunteerId)) {
            throw new IllegalStateException(volunteer.getName() + " is already registered for " + event.getTitle() + ".");
        }
        registrationRepository.add(new EventRegistration(event, volunteer));
        subscribe(eventId, observerFor(volunteerId));
    }

    public List<EventRegistration> listRegistrationsForEvent(int eventId) {
        return registrationRepository.findByEvent(eventId);
    }

    public List<EventRegistration> listVolunteerRegistrations(int volunteerId) {
        return registrationRepository.findByVolunteer(volunteerId);
    }

    /** Attended hours are credited once; a registration already marked attended is left unchanged. */
    public void markAttendance(int registrationId, String status, double hours) {
        EventRegistration registration = registrationRepository.findById(registrationId);
        if (registration == null) {
            throw new IllegalArgumentException("No registration with id " + registrationId);
        }
        if (EventRegistration.ATTENDED.equals(registration.getAttendanceStatus())) {
            return;
        }
        registration.updateAttendance(status);
        if (!EventRegistration.ATTENDED.equals(status)) {
            registrationRepository.update(registration);
            return;
        }
        if (hours <= 0) {
            throw new IllegalArgumentException("Hours must be more than 0.");
        }
        registrationRepository.saveAttended(registration, hours);
    }

    public void subscribe(long eventId, EventObserver observer) {
        publisher.subscribe(Math.toIntExact(eventId), observer);
    }

    public void unsubscribe(long eventId, EventObserver observer) {
        publisher.unsubscribe(Math.toIntExact(eventId), observer);
    }

    public EventPublisher publisher() {
        return publisher;
    }

    private VolunteerObserver observerFor(int volunteerId) {
        return new VolunteerObserver(volunteerId, notificationRepository);
    }

    private static void validate(Event event) {
        if (event == null || event.getTitle() == null || event.getDate() == null || event.getType() == null) {
            throw new IllegalArgumentException("Title, date and type are required.");
        }
    }

    private Event requireEvent(int id) {
        Event event = repository.findById(id);
        if (event == null) {
            throw new IllegalArgumentException("No event with id " + id);
        }
        return event;
    }
}
