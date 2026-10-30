package foodbridge.models.users;

import java.util.List;
import foodbridge.models.enums.BeneficiaryType;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.operations.DonationRequest;
import foodbridge.models.operations.Distribution;

public class Beneficiary extends User {
    private BeneficiaryType beneficiaryType;
    private int householdSize;
    private String needs;

    public DonationRequest requestFood(FoodCategory category, int quantity) {
        // TODO
        return null;
    }

    public List<Distribution> viewReceived() {
        // TODO
        return null;
    }
}
