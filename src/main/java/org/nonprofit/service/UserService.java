package org.nonprofit.service;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.repository.UserRepository;
import org.nonprofit.model.User;
public class UserService {
    private final UserRepository repository;
    public UserService(UserRepository repository) {this.repository=repository;}
    public long create(User value) throws SQLException {return repository.save(value);}
    public Optional<User> findById(long id) throws SQLException {return repository.findById(id);}
    public List<User> list() throws SQLException {return repository.findAll();}
}
