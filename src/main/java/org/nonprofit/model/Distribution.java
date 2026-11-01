package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class Distribution {
    private long id;
    private long foodDonationId;
    private long beneficiaryId;
    private Long volunteerId;
    private Long pickupTaskId;
    private LocalDate date;
    private BigDecimal quantity;
    public Distribution() { }
    public long getId() { return id; }
    public void setId(long value) { this.id = value; }
    public long getFoodDonationId() { return foodDonationId; }
    public void setFoodDonationId(long value) { this.foodDonationId = value; }
    public long getBeneficiaryId() { return beneficiaryId; }
    public void setBeneficiaryId(long value) { this.beneficiaryId = value; }
    public Long getVolunteerId() { return volunteerId; }
    public void setVolunteerId(Long value) { this.volunteerId = value; }
    public Long getPickupTaskId() { return pickupTaskId; }
    public void setPickupTaskId(Long value) { this.pickupTaskId = value; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate value) { this.date = value; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal value) { this.quantity = value; }
}
