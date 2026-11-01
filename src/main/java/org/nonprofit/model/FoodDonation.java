package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class FoodDonation extends Donation {
    private LocalDate expiryDate;
    private String pickupWindow;
    private List<FoodItem> items = new ArrayList<>();
    public FoodDonation() { }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate value) { this.expiryDate = value; }
    public String getPickupWindow() { return pickupWindow; }
    public void setPickupWindow(String value) { this.pickupWindow = value; }
    public List<FoodItem> getItems() { return items; }
    public void setItems(List<FoodItem> value) { this.items = value; }
    @Override public String getType() { return "FOOD"; }
    public boolean isExpired() { return expiryDate != null && expiryDate.isBefore(LocalDate.now()); }
    public void addItem(FoodItem item) { items.add(Objects.requireNonNull(item)); }
}
