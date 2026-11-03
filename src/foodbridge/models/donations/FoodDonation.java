package foodbridge.models.donations;

import java.time.LocalDate;
import java.util.List;

/**
 * FACTORY METHOD - ConcreteProduct. Contains 1..* FoodItem (composition).
 */
public class FoodDonation extends Donation {
    private List<FoodItem> items;
    private LocalDate expiryDate;
    private String pickupWindow;

    public FoodDonation() {
        // TODO: implement
    }

    public void addItem(FoodItem item) {
        // TODO: implement
    }

    public void removeItem(FoodItem item) {
        // TODO: implement
    }

    public boolean isExpired() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String getType() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
