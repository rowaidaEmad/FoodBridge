package foodbridge.repositories;

import java.util.List;

import foodbridge.models.lookups.Area;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Area (connection from DatabaseManager).
 */
public class AreaRepository implements Repository<Area> {
    private DatabaseManager db;

    public AreaRepository() {
        // TODO: implement
    }

    @Override
    public void add(Area item) {
        // TODO: implement
    }

    @Override
    public Area findById(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Area> findAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(Area item) {
        // TODO: implement
    }

    @Override
    public void delete(int id) {
        // TODO: implement
    }
}
