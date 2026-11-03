package foodbridge.controllers;

import java.util.List;

import foodbridge.models.operations.Distribution;
import foodbridge.models.operations.DonationRequest;
import foodbridge.repositories.DistributionRepository;
import foodbridge.repositories.DonationRequestRepository;

/**
 * Donation requests and received distributions.
 */
public class BeneficiaryController {
    private DonationRequestRepository requestRepository;
    private DistributionRepository distributionRepository;

    public BeneficiaryController() {
        // TODO: implement
    }

    public void submitRequest(DonationRequest request) {
        // TODO: implement
    }

    public void updateRequestStatus(int requestId, String status) {
        // TODO: implement
    }

    public List<Distribution> listDistributions(int beneficiaryId) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
