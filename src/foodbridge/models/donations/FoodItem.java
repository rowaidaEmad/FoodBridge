package foodbridge.models.donations;

import java.math.BigDecimal;
import java.time.LocalDate;

import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;

/**
 * Core domain. Belongs to a FoodCategory, measured in a Unit.
 */
public class FoodItem {
    private int id;
    private String name;
    private BigDecimal quantity;
    private String allergens;
    private LocalDate expiryDate;
    private FoodCategory category;
    private Unit unit;

    public FoodItem() {
        // TODO: implement
    }

    public BigDecimal getQuantity() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean isExpired() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public String getInfo() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
