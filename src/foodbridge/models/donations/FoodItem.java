package foodbridge.models.donations;

import java.math.BigDecimal;
import java.time.LocalDate;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;

/** A food item within a donation; quantities are not allocated by this module. */
public class FoodItem {
    private int id;
    private String name;
    private BigDecimal quantity;
    private String allergens;
    private LocalDate expiryDate;
    private FoodCategory category;
    private Unit unit;
    public FoodItem() { }
    public int getId() { return id; }
    public void setId(int value) { id = value; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal value) { quantity = value; }
    public String getAllergens() { return allergens; }
    public void setAllergens(String value) { allergens = value; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate value) { expiryDate = value; }
    public FoodCategory getCategory() { return category; }
    public void setCategory(FoodCategory value) { category = value; }
    public Unit getUnit() { return unit; }
    public void setUnit(Unit value) { unit = value; }
    public boolean isExpired() { return expiryDate != null && expiryDate.isBefore(LocalDate.now()); }
    public String getInfo() { return name + " (" + quantity + " " + (unit == null ? "" : unit.getName()) + ")"; }
}
