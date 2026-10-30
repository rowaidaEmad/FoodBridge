package foodbridge.models.donations;

import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;

public class FoodDonation extends Donation {
    private List<FoodItem> items = new ArrayList<>();
    private LocalDate expiryDate;
    private String pickupWindow;

    public void addItem(FoodItem item) {
        // TODO
    }

    public boolean isExpired() {
        // TODO
        return false;
    }
}
