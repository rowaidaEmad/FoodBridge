package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.service.NotificationService;
import org.nonprofit.model.Notification;
public class NotificationController {
    private final NotificationService service;
    public NotificationController(NotificationService service) {this.service=service;}
    public long create(Notification value) throws SQLException {return service.create(value);}
    public Optional<Notification> get(long id) throws SQLException {return service.findById(id);}
    public List<Notification> list() throws SQLException {return service.list();}
}

