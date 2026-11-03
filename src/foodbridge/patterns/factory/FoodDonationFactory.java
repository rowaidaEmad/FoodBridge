package foodbridge.patterns.factory;

import java.util.List;

import foodbridge.models.donations.Donation;
import foodbridge.models.donations.DonationDetails;
import foodbridge.models.donations.FoodDonation;
import foodbridge.models.donations.FoodItem;
import foodbridge.models.users.Donor;

// Factory Method pattern - ConcreteCreator
/**
 * FACTORY METHOD - ConcreteCreator. Builds a FoodDonation with its 1..* FoodItems.
 */
public class FoodDonationFactory extends DonationFactory {
    @Override
    public Donation createDonation(Donor donor, DonationDetails details) {
        List<FoodItem> items = details.getItems();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("A food donation needs at least one item");
        }
        FoodDonation donation = new FoodDonation(donor);
        for (FoodItem item : items) {
            donation.addItem(item);
        }
        donation.setExpiryDate(details.getExpiryDate());
        donation.setPickupWindow(details.getPickupWindow());
        return donation;
    }
}
