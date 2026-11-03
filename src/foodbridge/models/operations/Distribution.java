package foodbridge.models.operations;

import java.math.BigDecimal;
import java.time.LocalDate;

import foodbridge.models.donations.FoodDonation;
import foodbridge.models.users.Beneficiary;
import foodbridge.models.users.Volunteer;

/**
 * Core domain. Records a FoodDonation delivered by a Volunteer to a Beneficiary.
 */
public class Distribution {
    private int id;
    private FoodDonation donation;
    private Beneficiary beneficiary;
    private Volunteer volunteer;
    private LocalDate date;
    private BigDecimal quantity;

    public Distribution() {
        // TODO: implement
    }

    public BigDecimal getQuantity() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public LocalDate getDate() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
