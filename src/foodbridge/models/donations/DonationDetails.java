package foodbridge.models.donations;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private List<FoodItem> items;
    private LocalDate expiryDate;
    private String pickupWindow;

    public DonationDetails() {
        // TODO: implement
    }
}
