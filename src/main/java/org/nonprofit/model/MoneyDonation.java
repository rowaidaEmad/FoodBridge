package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class MoneyDonation extends Donation {
    private BigDecimal amount;
    private String method;
    private String transactionId;
    private boolean paid;
    public MoneyDonation() { }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { this.amount = value; }
    public String getMethod() { return method; }
    public void setMethod(String value) { this.method = value; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String value) { this.transactionId = value; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean value) { this.paid = value; }
    @Override public String getType() { return "MONEY"; }
}
