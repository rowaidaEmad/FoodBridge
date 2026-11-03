package foodbridge.repositories;

import java.util.List;

import foodbridge.models.operations.Notification;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Notification (connection from DatabaseManager).
 */
public class NotificationRepository implements Repository<Notification> {
    private DatabaseManager db;

    public NotificationRepository() {
        // TODO: implement
    }

    @Override
    public void add(Notification item) {
        // TODO: implement
    }

    @Override
    public Notification findById(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Notification> findAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(Notification item) {
        // TODO: implement
    }

    @Override
    public void delete(int id) {
        // TODO: implement
    }
}
