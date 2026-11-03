package foodbridge.models.operations;

import java.math.BigDecimal;
import java.time.LocalDate;

import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;
import foodbridge.models.users.Beneficiary;

/**
 * Core domain. Submitted by a Beneficiary; requests a FoodCategory, measured in a Unit.
 */
public class DonationRequest {
    private int id;
    private Beneficiary beneficiary;
    private FoodCategory category;
    private BigDecimal quantity;
    private Unit unit;
    private LocalDate date;
    private String status;

    public DonationRequest() {
        // TODO: implement
    }

    public void updateStatus(String s) {
        // TODO: implement
    }

    public BigDecimal getQuantity() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
