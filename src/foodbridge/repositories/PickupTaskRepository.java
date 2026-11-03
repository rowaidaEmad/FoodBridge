package foodbridge.repositories;

import java.util.List;

import foodbridge.models.operations.PickupTask;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for PickupTask (connection from DatabaseManager).
 */
public class PickupTaskRepository implements Repository<PickupTask> {
    private DatabaseManager db;

    public PickupTaskRepository() {
        // TODO: implement
    }

    @Override
    public void add(PickupTask item) {
        // TODO: implement
    }

    @Override
    public PickupTask findById(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<PickupTask> findAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(PickupTask item) {
        // TODO: implement
    }

    @Override
    public void delete(int id) {
        // TODO: implement
    }
}
