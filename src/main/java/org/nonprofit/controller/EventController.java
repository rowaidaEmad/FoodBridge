package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.service.EventService;
import org.nonprofit.model.Event;
import org.nonprofit.patterns.observer.EventObserver;
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service=service;
    }
    //return sql execption if it's repo is not implemented
    public long create(Event value) throws SQLException {
        return service.create(value);
    }
    public Optional<Event> get(long id) throws SQLException {
        return service.findById(id);
    }
    public List<Event> list() throws SQLException {
        return service.list();
    }

    public void announceChange(Event event, String message) {
        service.announceChange(event, message);
    }
    public boolean update(Event event) throws SQLException {
        return service.update(event);
    }

    public boolean cancelEvent(long eventId) throws SQLException {
        return service.cancelEvent(eventId);
    }

    public void subscribe(long eventId, EventObserver observer) {
        service.subscribe(eventId, observer);
    }

    public void unsubscribe(long eventId, EventObserver observer) {
        service.unsubscribe(eventId, observer);
    }


}

