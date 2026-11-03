package foodbridge.patterns.factory;

import foodbridge.models.donations.Donation;
import foodbridge.models.donations.DonationDetails;
import foodbridge.models.users.Donor;

/**
 * FACTORY METHOD - Creator.
 */
public abstract class DonationFactory {
    public Donation registerDonation(Donor donor, DonationDetails details) {
        if (donor == null) {
            throw new IllegalArgumentException("A donation needs a donor");
        }
        if (details == null) {
            throw new IllegalArgumentException("Donation details are required");
        }
        return createDonation(donor, details);
    }

    public abstract Donation createDonation(Donor donor, DonationDetails details);
}
