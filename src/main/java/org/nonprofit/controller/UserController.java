package org.nonprofit.controller;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.service.UserService;
import org.nonprofit.model.User;
public class UserController {
    private final UserService service;
    public UserController(UserService service) {this.service=service;}
    public long create(User value) throws SQLException {return service.create(value);}
    public Optional<User> get(long id) throws SQLException {return service.findById(id);}
    public List<User> list() throws SQLException {return service.list();}
}

