package foodbridge.controllers;

import foodbridge.models.donations.DonationDetails;
import foodbridge.models.donations.DonationProcessingResult;
import foodbridge.models.donations.Receipt;
import foodbridge.patterns.decorator.ReceiptOptions;
import foodbridge.services.DonationService;
import foodbridge.services.ReceiptService;

/**
 * Donations, payments and receipts.
 */
public class DonationController {
    private DonationService donationService;
    private ReceiptService receiptService;

    public DonationController() {
        // TODO: implement
    }

    public int createDonation(String type, DonationDetails request) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public DonationProcessingResult payMoneyDonation(int donationId) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Receipt createReceipt(int donationId) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public String getReceiptText(int receiptId, ReceiptOptions options) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
