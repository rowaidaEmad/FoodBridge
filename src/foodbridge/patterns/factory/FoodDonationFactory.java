package foodbridge.patterns.factory;

import foodbridge.models.donations.Donation;
import foodbridge.models.donations.DonationDetails;
import foodbridge.models.donations.FoodDonation;
import foodbridge.models.users.Donor;

/**
 * FACTORY METHOD - ConcreteCreator.
 */
public class FoodDonationFactory extends DonationFactory {
    @Override
    public Donation createDonation(Donor donor, DonationDetails details) {
        if (details.getItems().isEmpty()) {
            throw new IllegalArgumentException("A food donation needs at least one item");
        }
        FoodDonation donation = new FoodDonation(donor);
        details.getItems().forEach(donation::addItem);
        donation.setExpiryDate(details.getExpiryDate());
        donation.setPickupWindow(details.getPickupWindow());
        donation.setArea(details.getArea());
        return donation;
    }
}
