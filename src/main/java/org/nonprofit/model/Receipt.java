package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class Receipt {
    private long id;
    private long donationId;
    private String receiptNumber;
    private LocalDate issueDate;
    public Receipt() { }
    public long getId() { return id; }
    public void setId(long value) { this.id = value; }
    public long getDonationId() { return donationId; }
    public void setDonationId(long value) { this.donationId = value; }
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String value) { this.receiptNumber = value; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate value) { this.issueDate = value; }
}
