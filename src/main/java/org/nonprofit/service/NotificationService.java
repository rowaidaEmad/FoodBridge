package org.nonprofit.service;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.repository.NotificationRepository;
import org.nonprofit.model.Notification;
public class NotificationService {
    private final NotificationRepository repository;
    public NotificationService(NotificationRepository repository) {this.repository=repository;}
    public long create(Notification value) throws SQLException {return repository.save(value);}
    public Optional<Notification> findById(long id) throws SQLException {return repository.findById(id);}
    public List<Notification> list() throws SQLException {return repository.findAll();}
}

