package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class PickupTask {
    private long id;
    private long foodDonationId;
    private Long volunteerId;
    private Long beneficiaryId;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private PickupTaskStatus status;
    private double hoursSpent;
    public PickupTask() { }
    public long getId() { return id; }
    public void setId(long value) { this.id = value; }
    public long getFoodDonationId() { return foodDonationId; }
    public void setFoodDonationId(long value) { this.foodDonationId = value; }
    public Long getVolunteerId() { return volunteerId; }
    public void setVolunteerId(Long value) { this.volunteerId = value; }
    public Long getBeneficiaryId() { return beneficiaryId; }
    public void setBeneficiaryId(Long value) { this.beneficiaryId = value; }
    public LocalDateTime getPickupTime() { return pickupTime; }
    public void setPickupTime(LocalDateTime value) { this.pickupTime = value; }
    public LocalDateTime getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(LocalDateTime value) { this.deliveryTime = value; }
    public PickupTaskStatus getStatus() { return status; }
    public void setStatus(PickupTaskStatus value) { this.status = value; }
    public double getHoursSpent() { return hoursSpent; }
    public void setHoursSpent(double value) { this.hoursSpent = value; }
}
