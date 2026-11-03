package foodbridge.patterns.factory;

import foodbridge.models.donations.Donation;
import foodbridge.models.donations.DonationDetails;
import foodbridge.models.donations.MoneyDonation;
import foodbridge.models.users.Donor;

// Factory Method pattern - ConcreteCreator
/**
 * FACTORY METHOD - ConcreteCreator. Builds an unpaid MoneyDonation; payment is the Strategy's job.
 */
public class MoneyDonationFactory extends DonationFactory {
    @Override
    public Donation createDonation(Donor donor, DonationDetails details) {
        if (details.getAmount() == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        MoneyDonation donation = new MoneyDonation(donor);
        donation.setAmount(details.getAmount().doubleValue());
        donation.setMethod(details.getMethod());
        return donation;
    }
}
