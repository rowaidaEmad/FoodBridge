package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class FoodItem {
    private long id;
    private long foodDonationId;
    private String name;
    private BigDecimal quantity;
    private long categoryId;
    private long unitId;
    private String allergens;
    private LocalDate expiryDate;
    public FoodItem() { }
    public long getId() { return id; }
    public void setId(long value) { this.id = value; }
    public long getFoodDonationId() { return foodDonationId; }
    public void setFoodDonationId(long value) { this.foodDonationId = value; }
    public String getName() { return name; }
    public void setName(String value) { this.name = value; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal value) { this.quantity = value; }
    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long value) { this.categoryId = value; }
    public long getUnitId() { return unitId; }
    public void setUnitId(long value) { this.unitId = value; }
    public String getAllergens() { return allergens; }
    public void setAllergens(String value) { this.allergens = value; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate value) { this.expiryDate = value; }
    public boolean isExpired() { return expiryDate != null && expiryDate.isBefore(LocalDate.now()); }
}
