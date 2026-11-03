package foodbridge.repositories;

import java.util.List;

import foodbridge.models.lookups.EventType;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for EventType (connection from DatabaseManager).
 */
public class EventTypeRepository implements Repository<EventType> {
    private DatabaseManager db;

    public EventTypeRepository() {
        // TODO: implement
    }

    @Override
    public void add(EventType item) {
        // TODO: implement
    }

    @Override
    public EventType findById(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<EventType> findAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(EventType item) {
        // TODO: implement
    }

    @Override
    public void delete(int id) {
        // TODO: implement
    }
}
