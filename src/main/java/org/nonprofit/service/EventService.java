package org.nonprofit.service;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.repository.EventRepository;
import org.nonprofit.model.Event;
public class EventService {

    private final EventRepository repository;
    public EventService(EventRepository repository) {this.repository=repository;}

    public long create(Event value) throws SQLException {return repository.save(value);}
    public Optional<Event> findById(long id) throws SQLException {return repository.findById(id);}
    public List<Event> list() throws SQLException {return repository.findAll();}

    private final org.nonprofit.patterns.observer.EventPublisher publisher = new org.nonprofit.patterns.observer.EventPublisher();

    public void announceChange(org.nonprofit.model.Event event, String message) {
        publisher.notifyObservers(event,message);
    }
    public org.nonprofit.patterns.observer.EventPublisher publisher() {return publisher;}
}

