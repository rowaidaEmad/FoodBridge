package foodbridge.services;

import java.util.HashMap;
import java.util.Map;

import foodbridge.models.donations.Donation;
import foodbridge.models.donations.DonationDetails;
import foodbridge.models.donations.DonationProcessingResult;
import foodbridge.models.donations.MoneyDonation;
import foodbridge.models.lookups.DonationStatus;
import foodbridge.patterns.factory.DonationFactory;
import foodbridge.patterns.factory.FoodDonationFactory;
import foodbridge.patterns.factory.MoneyDonationFactory;
import foodbridge.patterns.strategy.CashStrategy;
import foodbridge.patterns.strategy.CheckStrategy;
import foodbridge.patterns.strategy.DonationProcessingStrategy;
import foodbridge.patterns.strategy.OnlineStrategy;
import foodbridge.repositories.DonationRepository;
import foodbridge.repositories.DonationStatusRepository;

// Strategy pattern - Context
/**
 * STRATEGY - Context, and FACTORY METHOD client (uses 1..* DonationFactory).
 * Holds one DonationProcessingStrategy per payment method and delegates payment to it;
 * it never contains cash / check / online logic itself.
 */
public class DonationService {
    private final Map<String, DonationFactory> factories = new HashMap<>();
    private final Map<String, DonationProcessingStrategy> strategies = new HashMap<>();
    private final DonationRepository donationRepository;
    private final DonationStatusRepository donationStatusRepository = new DonationStatusRepository();

    public DonationService() {
        this(new DonationRepository());
    }

    public DonationService(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
        registerDefaultFactories();
        registerDefaultStrategies();
    }

    /** A new donation type needs only a new factory + product class and one line here. */
    private void registerDefaultFactories() {
        factories.put("money", new MoneyDonationFactory());
        factories.put("food", new FoodDonationFactory());
    }

    /** A new payment method needs only a new strategy class and one line here. */
    private void registerDefaultStrategies() {
        registerStrategy("cash", new CashStrategy());
        registerStrategy("check", new CheckStrategy());
        registerStrategy("online", new OnlineStrategy());
    }

    /** Adds or replaces the strategy for a payment method ("cash", "check", "online"). */
    public void registerStrategy(String method, DonationProcessingStrategy strategy) {
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy is required");
        }
        strategies.put(method.trim().toLowerCase(), strategy);
    }

    /** Creates the donation through the factory for its type ("money", "food") and saves it as Posted. */
    public int createDonation(String type, DonationDetails request) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Donation type is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("Donation details are required");
        }
        DonationFactory factory = factories.get(type.trim().toLowerCase());
        if (factory == null) {
            throw new IllegalArgumentException("Unknown donation type: " + type);
        }
        Donation donation = factory.registerDonation(request.getDonor(), request);
        donationRepository.save(donation);
        return donation.getId();
    }

    public DonationProcessingResult processMoneyDonation(int id) {
        Donation found = donationRepository.findById(id);
        if (found == null) {
            throw new IllegalArgumentException("No donation with id " + id);
        }
        if (!(found instanceof MoneyDonation donation)) {
            throw new IllegalArgumentException("Donation " + id + " is not a money donation");
        }
        if (donation.isPaid()) {
            throw new IllegalStateException("Donation is already paid");
        }
        // Only Posted donations can be paid (not Cancelled, Paid, Expired, ...).
        if (!DonationStatus.POSTED.equals(donation.getStatus().getName())) {
            throw new IllegalStateException(
                    "Only Posted donations can be paid; this one is " + donation.getStatus().getName());
        }
        DonationProcessingStrategy strategy = strategies.get(donation.getMethod());
        if (strategy == null) {
            throw new IllegalStateException("No payment strategy for method " + donation.getMethod());
        }
        // Loaded before charging, so a missing 'Paid' row can't leave a payment unrecorded.
        DonationStatus paidStatus = donationStatusRepository.findByName(DonationStatus.PAID);
        if (paidStatus == null) {
            throw new IllegalStateException("Donation status 'Paid' is missing; run database/seed.sql");
        }

        DonationProcessingResult result = strategy.process(donation);

        if (result.isSuccessful()) {
            donation.markAsPaid(result.getReference());
            donation.updateStatus(paidStatus);
            donationRepository.save(donation);
            // TODO (Decorator step): issue the receipt through ReceiptService.
        }
        return result;
    }
}
