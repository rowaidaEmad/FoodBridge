package foodbridge.patterns.factory;

import foodbridge.models.donations.Donation;
import foodbridge.models.donations.DonationDetails;
import foodbridge.models.users.Donor;

// Factory Method pattern - Creator
/**
 * FACTORY METHOD - Creator.
 * registerDonation holds the steps shared by every donation; createDonation (the factory method)
 * is left to the subclasses, so callers never pick the concrete Donation class themselves.
 */
public abstract class DonationFactory {
    public Donation registerDonation(Donor donor, DonationDetails details) {
        if (donor == null) {
            throw new IllegalArgumentException("A donation needs a donor");
        }
        if (details == null) {
            throw new IllegalArgumentException("Donation details are required");
        }
        Donation donation = createDonation(donor, details);
        donation.setArea(details.getArea());
        return donation;
    }

    public abstract Donation createDonation(Donor donor, DonationDetails details);
}
