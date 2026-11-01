package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.service.EventRegistrationService;
import org.nonprofit.model.EventRegistration;
public class EventRegistrationController {
    private final EventRegistrationService service;
    public EventRegistrationController(EventRegistrationService service) {this.service=service;}
    public long create(EventRegistration value) throws SQLException {return service.create(value);}
    public Optional<EventRegistration> get(long id) throws SQLException {return service.findById(id);}
    public List<EventRegistration> list() throws SQLException {return service.list();}
}

