package foodbridge.services;

import foodbridge.models.donations.Receipt;
import foodbridge.patterns.decorator.ReceiptOptions;
import foodbridge.repositories.ReceiptRepository;

/**
 * DECORATOR - Client. Creates Receipts and builds their text.
 */
public class ReceiptService {
    private ReceiptRepository repository;

    public ReceiptService() {
        // TODO: implement
    }

    public Receipt createReceipt(int donationId) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public String generateReceiptText(int receiptId, ReceiptOptions options) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
