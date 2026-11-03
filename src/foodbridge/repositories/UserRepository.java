package foodbridge.repositories;

import java.util.List;

import foodbridge.models.users.User;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for User (connection from DatabaseManager).
 */
public class UserRepository implements Repository<User> {
    private DatabaseManager db;

    public UserRepository() {
        // TODO: implement
    }

    @Override
    public void add(User item) {
        // TODO: implement
    }

    @Override
    public User findById(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<User> findAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(User item) {
        // TODO: implement
    }

    @Override
    public void delete(int id) {
        // TODO: implement
    }
}
