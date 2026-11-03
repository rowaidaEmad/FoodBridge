package foodbridge.models.operations;

import java.math.BigDecimal;
import java.time.LocalDate;

import foodbridge.models.donations.FoodDonation;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.users.Volunteer;

/**
 * Core domain. Records a FoodDonation delivered by a Volunteer to a Beneficiary.
 */
public class Distribution {
    private int id;
    private FoodDonation donation;
    private Beneficiary beneficiary;
    private Volunteer volunteer;
    private LocalDate date;
    private BigDecimal quantity;

    public Distribution() {
    }

    public Distribution(FoodDonation donation, Beneficiary beneficiary, Volunteer volunteer, LocalDate date) {
        this.donation = donation;
        this.beneficiary = beneficiary;
        this.volunteer = volunteer;
        this.date = date;
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

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
