package foodbridge.repositories;

import java.util.List;

import foodbridge.models.operations.DonationRequest;
import foodbridge.patterns.singleton.DatabaseManager;

/**
 * Data access for DonationRequest (connection from DatabaseManager).
 */
public class DonationRequestRepository implements Repository<DonationRequest> {
    private DatabaseManager db;

    public DonationRequestRepository() {
        // TODO: implement
    }

    @Override
    public void add(DonationRequest item) {
        // TODO: implement
    }

    @Override
    public DonationRequest findById(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<DonationRequest> findAll() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void update(DonationRequest item) {
        // TODO: implement
    }

    @Override
    public void delete(int id) {
        // TODO: implement
    }
}
