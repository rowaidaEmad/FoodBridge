package foodbridge.repositories;

import java.util.List;

import foodbridge.models.operations.EventRegistration;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for EventRegistration (connection from DatabaseManager).
 */
public class EventRegistrationRepository implements Repository<EventRegistration> {
    private DatabaseManager db;

    public EventRegistrationRepository() {
        // TODO: implement
    }

    @Override
    public void add(EventRegistration item) {
        // TODO: implement
    }

    @Override
    public EventRegistration findById(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<EventRegistration> findAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(EventRegistration item) {
        // TODO: implement
    }

    @Override
    public void delete(int id) {
        // TODO: implement
    }
}
