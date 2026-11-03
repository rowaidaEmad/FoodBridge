package foodbridge.models.donations;

import java.time.LocalDate;

/**
 * Core domain. Receipt of one Donation; formatted by BasicReceipt (Decorator).
 */
public class Receipt {
    private int id;
    private String receiptNumber;
    private Donation donation;
    private LocalDate issueDate;

    public Receipt() {
        // TODO: implement
    }

    public String getReceiptNumber() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Donation getDonation() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
