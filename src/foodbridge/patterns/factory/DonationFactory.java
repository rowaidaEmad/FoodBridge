package foodbridge.patterns.factory;

import foodbridge.models.donations.Donation;
import foodbridge.models.donations.DonationDetails;
import foodbridge.models.users.Donor;

/**
 * FACTORY METHOD - Creator.
 */
public abstract class DonationFactory {
    public Donation registerDonation(Donor donor, DonationDetails details) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public abstract Donation createDonation(Donor donor, DonationDetails details);
}
