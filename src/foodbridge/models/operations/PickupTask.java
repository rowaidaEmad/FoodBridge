package foodbridge.models.operations;

import java.time.Duration;
import java.time.LocalDateTime;

import foodbridge.models.donations.FoodDonation;
import foodbridge.models.enums.PickupTaskStatus;
import foodbridge.models.lookups.Area;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.users.Volunteer;

/**
 * Core domain. A Volunteer picks up a FoodDonation and delivers it to a Beneficiary.
 */
public class PickupTask {
    private static final double MINIMUM_HOURS = 0.25;

    private int id;
    private FoodDonation donation;
    private Volunteer volunteer;
    private Beneficiary beneficiary;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private PickupTaskStatus status = PickupTaskStatus.PENDING;
    private double hoursSpent;
    private String requiredSkill;
    private String itemsSummary;
    private Area area;

    public PickupTask() {
    }

    public void assignVolunteer(Volunteer v) {
        if (status != PickupTaskStatus.PENDING) {
            throw new IllegalStateException("Only a pending task can be assigned.");
        }
        volunteer = v;
        status = PickupTaskStatus.ASSIGNED;
    }

    public void markPickedUp() {
        if (status != PickupTaskStatus.ASSIGNED) {
            throw new IllegalStateException("Only an assigned task can be picked up.");
        }
        pickupTime = LocalDateTime.now();
        status = PickupTaskStatus.PICKED_UP;
    }

    public void markDelivered() {
        if (status != PickupTaskStatus.PICKED_UP) {
            throw new IllegalStateException("Only a picked up task can be delivered.");
        }
        if (beneficiary == null) {
            throw new IllegalStateException("This task has no beneficiary to deliver to.");
        }
        deliveryTime = LocalDateTime.now();
        status = PickupTaskStatus.DELIVERED;
        hoursSpent = calculateHours();
    }

    public double calculateHours() {
        if (pickupTime == null || deliveryTime == null) {
            return 0;
        }
        return Math.max(MINIMUM_HOURS, Duration.between(pickupTime, deliveryTime).toMinutes() / 60.0);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public FoodDonation getDonation() {
        return donation;
    }

    public void setDonation(FoodDonation donation) {
        this.donation = donation;
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
    }

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(Beneficiary beneficiary) {
        this.beneficiary = beneficiary;
    }

    public LocalDateTime getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(LocalDateTime pickupTime) {
        this.pickupTime = pickupTime;
    }

    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(LocalDateTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public PickupTaskStatus getStatus() {
        return status;
    }

    public void setStatus(PickupTaskStatus status) {
        this.status = status;
    }

    public double getHoursSpent() {
        return hoursSpent;
    }

    public void setHoursSpent(double hoursSpent) {
        this.hoursSpent = hoursSpent;
    }

    public String getRequiredSkill() {
        return requiredSkill;
    }

    public void setRequiredSkill(String requiredSkill) {
        this.requiredSkill = requiredSkill;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getItemsSummary() {
        return itemsSummary;
    }

    public void setItemsSummary(String itemsSummary) {
        this.itemsSummary = itemsSummary;
    }
}
