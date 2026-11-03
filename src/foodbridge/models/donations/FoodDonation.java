package foodbridge.models.donations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import foodbridge.models.users.Donor;

// Factory Method pattern - ConcreteProduct
/**
 * FACTORY METHOD - ConcreteProduct. Contains 1..* FoodItem (composition): DonationRepository saves
 * the items with the donation, and deleting the donation deletes its items (ON DELETE CASCADE).
 */
public class FoodDonation extends Donation {
    private List<FoodItem> items = new ArrayList<>();
    private LocalDate expiryDate;
    private String pickupWindow;

    public FoodDonation(Donor donor) {
        super(donor);
    }

    /**
     * Id-only reference used when another record points at a donation (e.g. DistributionRepository).
     * It has no donor, so DonationRepository refuses to save it; new donations use FoodDonation(Donor).
     */
    public FoodDonation() {
    }

    public void addItem(FoodItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item is required");
        }
        items.add(item);
    }

    /** Removes a saved item; its row is deleted the next time the donation is saved. */
    public void removeItem(int itemId) {
        if (!items.removeIf(item -> item.getId() == itemId && itemId != 0)) {
            throw new IllegalArgumentException("No item with id " + itemId + " in this donation");
        }
    }

    public List<FoodItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getPickupWindow() {
        return pickupWindow;
    }

    public void setPickupWindow(String window) {
        this.pickupWindow = window;
    }

    public double getTotalQuantity() {
        double total = 0;
        for (FoodItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    @Override
    public String getType() {
        return "food";
    }

    /** Shown in the beneficiary delivery form, e.g. "#12 - Rice - 10 kg - ...". */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder("#" + id);
        if (items.isEmpty()) {
            return text.append(" - Food donation").toString();
        }
        for (int i = 0; i < items.size(); i++) {
            text.append(i == 0 ? " - " : ", ").append(items.get(i).getInfo());
        }
        return text.toString();
    }

    /** e.g. "Food donation - 20 meals - Posted", or "... - 3 items - ..." when the units differ. */
    @Override
    public String getDetails() {
        String unit = items.isEmpty() || items.get(0).getUnit() == null ? null : items.get(0).getUnit().getName();
        for (FoodItem item : items) {
            if (item.getUnit() == null || !item.getUnit().getName().equals(unit)) {
                unit = null;
                break;
            }
        }
        String amount = unit != null
                ? FoodItem.formatQuantity(getTotalQuantity()) + " " + unit
                : items.size() + (items.size() == 1 ? " item" : " items");
        return "Food donation - " + amount + " - " + statusName();
    }
}
