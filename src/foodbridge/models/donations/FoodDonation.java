package foodbridge.models.donations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.users.Donor;

/**
 * FACTORY METHOD - ConcreteProduct. Contains 1..* FoodItem (composition).
 */
public class FoodDonation extends Donation {
    private List<FoodItem> items = new ArrayList<>();
    private LocalDate expiryDate;
    private String pickupWindow;

    public FoodDonation(Donor donor) {
        this.donor = donor;
    }

    public String getPickupWindow() {
        return pickupWindow;
    }

    public void setPickupWindow(String window) {
        this.pickupWindow = window;
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
