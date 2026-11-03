package org.nonprofit.service;


import java.sql.SQLException;
import java.util.*;

import org.nonprofit.model.EventRegistration;
import org.nonprofit.patterns.observer.EventPublisher;
import org.nonprofit.patterns.observer.VolunteerObserver;
import org.nonprofit.repository.EventRegistrationRepository;
import org.nonprofit.repository.EventRepository;
import org.nonprofit.model.Event;
import org.nonprofit.patterns.observer.EventObserver;

public class EventService {

    private final EventRepository repository;
    private final EventRegistrationRepository registrationRepository;
    private final EventPublisher publisher;

    public EventService(EventRepository repository, EventPublisher publisher,
                        EventRegistrationRepository registrationRepository){

        this.repository = repository;
        this.registrationRepository = registrationRepository;
        this.publisher = publisher;
    }

    public long create(Event value) throws SQLException {
        return repository.save(value);
    }
    public Optional<Event> findById(long id) throws SQLException {
        return repository.findById(id);
    }
    public List<Event> list() throws SQLException {
        return repository.findAll();
    }


    public void announceChange(org.nonprofit.model.Event event, String message) {
        publisher.notifyObservers(event,message);
    }

    public boolean update(Event event) throws SQLException {
        boolean updated = repository.update(event);

        if (updated) {
            publisher.notifyObservers(event, "Event details have been updated!!!!");
        }
        return updated;
    }


    public boolean cancelEvent(long eventId) throws SQLException {
        Optional<Event> optionalEvent = repository.findById(eventId);

        if (optionalEvent.isEmpty()) {
            return false;
        }
        Event event = optionalEvent.get();

        boolean cancelled = repository.cancel(eventId);
        if (cancelled) {
            publisher.notifyObservers(event,"This event has been cancelled!!!!");
        }

        return cancelled;
    }

    // subscribe an observer to a specific event
    public void subscribe(long eventId, EventObserver observer) {
        publisher.subscribe(eventId, observer);
    }

    // unsubscribe an observer from a specific event
    public void unsubscribe(long eventId, EventObserver observer) {
        publisher.unsubscribe(eventId, observer);
    }

    public org.nonprofit.patterns.observer.EventPublisher publisher() {
        return publisher;
    }

    public void loadEventObservers() throws SQLException {

        // Get all registrations from PostgreSQL
        List<EventRegistration> registrations = registrationRepository.findAll();

        for (EventRegistration registration : registrations) {

            long eventId = registration.getEventId();
            long volunteerId = registration.getVolunteerId();

            // Create an observer for this volunteer
            VolunteerObserver observer =  new VolunteerObserver(volunteerId);

            // Subscribe the volunteer to their event
            publisher.subscribe(eventId, observer);
        }

        System.out.println("Loaded " + registrations.size() + " event observers.");
    }
}

