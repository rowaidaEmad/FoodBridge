package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.service.EventService;
import org.nonprofit.model.Event;
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

}

