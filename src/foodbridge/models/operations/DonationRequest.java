package foodbridge.models.operations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;
import foodbridge.models.users.Beneficiary;

/** A beneficiary's food request; approval does not imply delivery. */
public class DonationRequest {
    private int id;
    private Beneficiary beneficiary;
    private FoodCategory category;
    private BigDecimal quantity;
    private Unit unit;
    private LocalDate date = LocalDate.now();
    private String status = "PENDING";
    public DonationRequest() { }
    public DonationRequest(Beneficiary beneficiary, FoodCategory category, BigDecimal quantity, Unit unit) {
        setBeneficiary(beneficiary); setCategory(category); setQuantity(quantity); setUnit(unit);
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Beneficiary getBeneficiary() { return beneficiary; }
    public void setBeneficiary(Beneficiary value) { beneficiary = Objects.requireNonNull(value, "Beneficiary is required."); }
    public FoodCategory getCategory() { return category; }
    public void setCategory(FoodCategory value) { category = Objects.requireNonNull(value, "Category is required."); }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal value) {
        if (value == null || value.signum() <= 0 || value.stripTrailingZeros().scale() > 3 || value.compareTo(new BigDecimal("100000000000")) >= 0)
            throw new IllegalArgumentException("Quantity must be positive, below 100000000000, with at most 3 decimal places.");
        quantity = value;
    }
    public Unit getUnit() { return unit; }
    public void setUnit(Unit value) { unit = Objects.requireNonNull(value, "Unit is required."); }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate value) { date = Objects.requireNonNull(value, "Date is required."); }
    public String getStatus() { return status; }
    public void updateStatus(String value) {
        // FULFILLED is set when a volunteer delivers food for an approved request.
        if (value == null || !Set.of("PENDING", "APPROVED", "REJECTED", "FULFILLED").contains(value))
            throw new IllegalArgumentException("Status must be PENDING, APPROVED, REJECTED or FULFILLED.");
        status = value;
    }
}
