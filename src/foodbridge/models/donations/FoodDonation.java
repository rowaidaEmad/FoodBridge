package foodbridge.models.donations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Factory Method concrete product. Contains food items. */
public class FoodDonation extends Donation {
    private final List<FoodItem> items = new ArrayList<>();
    private LocalDate expiryDate;
    private String pickupWindow;
    public FoodDonation() { }
    public void addItem(FoodItem item) { items.add(Objects.requireNonNull(item)); }
    public void removeItem(FoodItem item) { items.remove(item); }
    public List<FoodItem> getItems() { return List.copyOf(items); }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate date) { expiryDate = date; }
    public String getPickupWindow() { return pickupWindow; }
    public void setPickupWindow(String value) { pickupWindow = value; }
    public boolean isExpired() { return expiryDate != null && expiryDate.isBefore(LocalDate.now()); }
    @Override public String getType() { return "FOOD"; }
    @Override public String toString() {
        String contents = items.stream().map(FoodItem::getInfo).collect(Collectors.joining(", "));
        return "#" + id + (contents.isEmpty() ? " - Food donation" : " - " + contents);
    }
}
