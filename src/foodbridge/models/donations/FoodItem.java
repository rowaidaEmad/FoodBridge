package foodbridge.models.donations;

import java.math.BigDecimal;
import java.time.LocalDate;

import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;

/**
 * Core domain. Belongs to a FoodCategory, measured in a Unit.
 * Saved and loaded only together with its FoodDonation (composition), by DonationRepository.
 */
public class FoodItem {
    private int id;
    private String name;
    private double quantity;
    private String allergens;
    private LocalDate expiryDate;
    private FoodCategory category;
    private Unit unit;

    public FoodItem() {
    }

    public int getId() {
        return id;
    }

    /** Set by DonationRepository when the item is saved or loaded. */
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name is required");
        }
        this.name = name.trim();
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        this.quantity = quantity;
    }

    public String getAllergens() {
        return allergens;
    }

    public void setAllergens(String allergens) {
        this.allergens = allergens;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public FoodCategory getCategory() {
        return category;
    }

    public void setCategory(FoodCategory category) {
        this.category = category;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    /** e.g. "Rice - 10 kg - Grains (allergens: none)". */
    public String getInfo() {
        return name + " - " + formatQuantity(quantity) + " " + (unit == null ? "" : unit.getName())
                + " - " + (category == null ? "Uncategorized" : category.getName())
                + " (allergens: " + (allergens == null || allergens.isBlank() ? "none" : allergens) + ")";
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /** 10.0 -> "10", 2.5 -> "2.5". */
    static String formatQuantity(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
