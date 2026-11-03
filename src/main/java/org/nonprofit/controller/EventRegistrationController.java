package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.service.EventRegistrationService;
import org.nonprofit.model.EventRegistration;
public class EventRegistrationController {

    private final EventRegistrationService service;

    public EventRegistrationController(EventRegistrationService service) {
        this.service=service;
    }
    //create new event
    //through excepion in case query not implemented
    public long create(EventRegistration value) throws SQLException {
        return service.create(value);
    }
    // fetch one EventReg by it's id
    public List<EventRegistration> get(long id) throws SQLException {
        return service.findById(id);
    }
    //fetch all events
    public List<EventRegistration> list() throws SQLException {
        return service.list();
    }
}

