package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class DonationRequest {
    private long id;
    private long beneficiaryId;
    private long categoryId;
    private long unitId;
    private BigDecimal quantity;
    private LocalDate date;
    private String status;
    public DonationRequest() { }
    public long getId() { return id; }
    public void setId(long value) { this.id = value; }
    public long getBeneficiaryId() { return beneficiaryId; }
    public void setBeneficiaryId(long value) { this.beneficiaryId = value; }
    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long value) { this.categoryId = value; }
    public long getUnitId() { return unitId; }
    public void setUnitId(long value) { this.unitId = value; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal value) { this.quantity = value; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate value) { this.date = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
}
