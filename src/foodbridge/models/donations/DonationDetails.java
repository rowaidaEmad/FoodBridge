package foodbridge.models.donations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import foodbridge.models.lookups.Area;
import foodbridge.models.users.Donor;

/**
 * Helper (not drawn in the diagram): the 'details' / 'request' data passed to the factories.
 */
public class DonationDetails {
    private Donor donor;
    private Area area;
    private BigDecimal amount;
    private String method;
    private List<FoodItem> items = new ArrayList<>();
    private LocalDate expiryDate;
    private String pickupWindow;

    public DonationDetails() {
    }

    public Donor getDonor() {
        return donor;
    }

    public void setDonor(Donor donor) {
        this.donor = donor;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<FoodItem> getItems() {
        return items;
    }

    public void setItems(List<FoodItem> items) {
        this.items = new ArrayList<>(items);
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

    public void setPickupWindow(String pickupWindow) {
        this.pickupWindow = pickupWindow;
    }
}
