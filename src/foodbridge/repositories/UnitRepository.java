package foodbridge.repositories;

import java.util.List;

import foodbridge.models.lookups.Unit;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for Unit (connection from DatabaseManager).
 */
public class UnitRepository implements Repository<Unit> {
    private DatabaseManager db;

    public UnitRepository() {
        // TODO: implement
    }

    @Override
    public void add(Unit item) {
        // TODO: implement
    }

    @Override
    public Unit findById(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Unit> findAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(Unit item) {
        // TODO: implement
    }

    @Override
    public void delete(int id) {
        // TODO: implement
    }
}
