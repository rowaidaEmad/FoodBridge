package foodbridge.services;

import java.util.Map;

import foodbridge.models.donations.Donation;
import foodbridge.models.donations.DonationDetails;
import foodbridge.models.donations.DonationProcessingResult;
import foodbridge.models.donations.FoodDonation;
import foodbridge.patterns.factory.DonationFactory;
import foodbridge.patterns.factory.FoodDonationFactory;
import foodbridge.patterns.strategy.DonationProcessingStrategy;
import foodbridge.repositories.DonationRepository;
import foodbridge.repositories.PickupTaskRepository;
import foodbridge.repositories.UserRepository;

/**
 * STRATEGY - Context, and FACTORY METHOD client (uses 1..* DonationFactory).
 * Food donations are supported; money donations are not implemented yet.
 */
public class DonationService {
    public static final String FOOD = "FOOD";

    private Map<String, DonationFactory> factories;
    private Map<String, DonationProcessingStrategy> strategies;
    private DonationRepository repository;
    private final UserRepository userRepository = new UserRepository();
    private final PickupTaskRepository pickupTaskRepository = new PickupTaskRepository();

    public DonationService() {
        factories = Map.of(FOOD, new FoodDonationFactory());
        strategies = Map.of();
        repository = new DonationRepository();
    }

    /**
     * Saves the donation and, for food, opens a pickup task so volunteers can collect it.
     * The donor may be any user, such as a volunteer; they get a donor profile if they have none.
     */
    public int createDonation(String type, DonationDetails request) {
        if (request == null || request.getDonor() == null || request.getDonor().getId() == 0) {
            throw new IllegalArgumentException("Log in to donate.");
        }
        DonationFactory factory = type == null ? null : factories.get(type.toUpperCase());
        if (factory == null) {
            throw new UnsupportedOperationException("Donation type '" + type + "' is not supported yet.");
        }
        userRepository.ensureDonorProfile(request.getDonor().getId());
        Donation donation = factory.registerDonation(request.getDonor(), request);
        repository.save(donation);
        if (donation instanceof FoodDonation) {
            Integer areaId = donation.getArea() == null ? null : donation.getArea().getId();
            pickupTaskRepository.addForDonation(donation.getId(), areaId);
        }
        return donation.getId();
    }

    public DonationProcessingResult processMoneyDonation(int id) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
