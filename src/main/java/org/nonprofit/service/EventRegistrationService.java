package org.nonprofit.service;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.repository.EventRegistrationRepository;
import org.nonprofit.model.EventRegistration;
public class EventRegistrationService {
    private final EventRegistrationRepository repository;
    public EventRegistrationService(EventRegistrationRepository repository) {this.repository=repository;}
    public long create(EventRegistration value) throws SQLException {return repository.save(value);}
    public Optional<EventRegistration> findById(long id) throws SQLException {return repository.findById(id);}
    public List<EventRegistration> list() throws SQLException {return repository.findAll();}
}

