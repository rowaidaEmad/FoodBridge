package foodbridge.repositories;

import java.util.List;

import foodbridge.models.lookups.DonationStatus;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for DonationStatus (connection from DatabaseManager).
 */
public class DonationStatusRepository implements Repository<DonationStatus> {
    private DatabaseManager db;

    public DonationStatusRepository() {
        // TODO: implement
    }

    @Override
    public void add(DonationStatus item) {
        // TODO: implement
    }

    @Override
    public DonationStatus findById(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<DonationStatus> findAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(DonationStatus item) {
        // TODO: implement
    }

    @Override
    public void delete(int id) {
        // TODO: implement
    }
}
