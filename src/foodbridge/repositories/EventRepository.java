package foodbridge.repositories;

import java.util.List;

import foodbridge.models.operations.Event;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Event (connection from DatabaseManager).
 */
public class EventRepository implements Repository<Event> {
    private DatabaseManager db;

    public EventRepository() {
        // TODO: implement
    }

    @Override
    public void add(Event item) {
        // TODO: implement
    }

    @Override
    public Event findById(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Event> findAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(Event item) {
        // TODO: implement
    }

    @Override
    public void delete(int id) {
        // TODO: implement
    }
}
