package foodbridge.services;

import java.util.List;

import foodbridge.models.operations.Event;
import foodbridge.models.operations.EventRegistration;
import foodbridge.models.users.Volunteer;
import foodbridge.patterns.observer.EventPublisher;
import foodbridge.patterns.observer.VolunteerObserver;
import foodbridge.repositories.EventRegistrationRepository;
import foodbridge.repositories.EventRepository;
import foodbridge.repositories.NotificationRepository;
import foodbridge.repositories.VolunteerRepository;

/**
 * OBSERVER - Client. Updates Events, creates EventRegistrations, publishes via EventPublisher.
 */
public class EventService {
    private final EventRepository repository = new EventRepository();
    private final EventRegistrationRepository registrationRepository = new EventRegistrationRepository();
    private final VolunteerRepository volunteerRepository = new VolunteerRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final EventPublisher publisher = new EventPublisher();

    /** Saves the new details and tells the registered volunteers. */
    public void updateEvent(int id, Event data) {
        requireEvent(id);
        data.setId(id);
        requireDetails(data);
        repository.update(data);
        announceChange(data, "The event details have changed.");
    }

    public void registerVolunteer(int eventId, int volunteerId) {
        Event event = requireEvent(eventId);
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
    }

    public List<EventRegistration> listRegistrations(int volunteerId) {
        return registrationRepository.findByVolunteer(volunteerId);
    }

    public List<EventRegistration> listRegistrationsForEvent(int eventId) {
        return registrationRepository.findByEvent(eventId);
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

    public long create(Event value) {
        requireDetails(value);
        repository.add(value);
        return value.getId();
    }

    public Event findById(long id) {
        return requireEvent(Math.toIntExact(id));
    }

    public List<Event> list() {
        return repository.findAll();
    }

    public void announceChange(Event e, String msg) {
        if (msg == null || msg.isBlank()) {
            throw new IllegalArgumentException("Enter a message to announce.");
        }
        notifyRegistered(e, msg.trim(), registrationRepository.findByEvent(e.getId()));
    }

    /** Returns false if the event no longer exists. */
    public boolean update(Event event) {
        requireDetails(event);
        if (repository.findById(event.getId()) == null) {
            return false;
        }
        repository.update(event);
        return true;
    }

    /**
     * Deletes the event, which also removes its registrations, then tells the volunteers who were registered.
     * Returns false if the event no longer exists.
     */
    public boolean cancelEvent(long eventId) {
        int id = Math.toIntExact(eventId);
        Event event = repository.findById(id);
        if (event == null) {
            return false;
        }
        List<EventRegistration> registrations = registrationRepository.findByEvent(id);
        repository.delete(id);
        notifyRegistered(event, "This event has been cancelled.", registrations);
        return true;
    }

    private void notifyRegistered(Event event, String message, List<EventRegistration> registrations) {
        publisher.clearObservers(event.getId());
        for (EventRegistration registration : registrations) {
            publisher.registerObserver(event.getId(),
                    new VolunteerObserver(registration.getVolunteer().getId(), notificationRepository));
        }
        publisher.updateEvent(event, message);
    }

    private void requireDetails(Event event) {
        if (event == null || event.getTitle() == null || event.getTitle().isBlank()) {
            throw new IllegalArgumentException("Enter the event title.");
        }
        if (event.getDate() == null) {
            throw new IllegalArgumentException("Enter the event date.");
        }
        if (event.getType() == null) {
            throw new IllegalArgumentException("Choose an event type.");
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
