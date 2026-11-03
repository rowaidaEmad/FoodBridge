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
        issueDate = LocalDate.now();
    }

    public Receipt(Donation donation, String receiptNumber, LocalDate issueDate) {
        if (donation == null || receiptNumber == null || receiptNumber.isBlank() || issueDate == null) {
            throw new IllegalArgumentException("Donation, receipt number and issue date are required");
        }
        this.donation = donation;
        this.receiptNumber = receiptNumber.trim();
        this.issueDate = issueDate;
    }

    public int getId() {
        return id;
    }

    /** Set by ReceiptRepository after saving or loading. */
    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public Donation getDonation() {
        return donation;
    }
}
