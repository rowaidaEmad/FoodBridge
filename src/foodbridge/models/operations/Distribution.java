package foodbridge.models.operations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import foodbridge.models.donations.FoodDonation;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.users.Volunteer;

/** Manual delivery record. Quantity retains the existing UML's donation-level meaning. */
public class Distribution {
    private int id;
    private FoodDonation donation;
    private Beneficiary beneficiary;
    private Volunteer volunteer;
    private LocalDate date = LocalDate.now();
    private BigDecimal quantity;
    public Distribution() { }
    public Distribution(FoodDonation donation, Beneficiary beneficiary, Volunteer volunteer, LocalDate date, BigDecimal quantity) {
        setDonation(donation); setBeneficiary(beneficiary); setVolunteer(volunteer); setDate(date); setQuantity(quantity);
    }
    public int getId() { return id; }
    public void setId(int value) { id = value; }
    public FoodDonation getDonation() { return donation; }
    public void setDonation(FoodDonation value) { donation = Objects.requireNonNull(value, "Donation is required."); }
    public Beneficiary getBeneficiary() { return beneficiary; }
    public void setBeneficiary(Beneficiary value) { beneficiary = Objects.requireNonNull(value, "Beneficiary is required."); }
    public Volunteer getVolunteer() { return volunteer; }
    public void setVolunteer(Volunteer value) { volunteer = value; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate value) { date = Objects.requireNonNull(value, "Date is required."); }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal value) {
        if (value == null || value.signum() <= 0 || value.stripTrailingZeros().scale() > 3 || value.compareTo(new BigDecimal("100000000000")) >= 0)
            throw new IllegalArgumentException("Quantity must be positive, below 100000000000, with at most 3 decimal places.");
        quantity = value;
    }
}
