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

    public void updateEvent(int id, Event data) {
        // TODO: implement
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
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
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
        publisher.clearObservers(e.getId());
        for (EventRegistration registration : registrationRepository.findByEvent(e.getId())) {
            publisher.registerObserver(e.getId(),
                    new VolunteerObserver(registration.getVolunteer().getId(), notificationRepository));
        }
        publisher.updateEvent(e, msg.trim());
    }

    public boolean update(Event event) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean cancelEvent(long eventId) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private Event requireEvent(int id) {
        Event event = repository.findById(id);
        if (event == null) {
            throw new IllegalArgumentException("No event with id " + id);
        }
        return event;
    }
}
