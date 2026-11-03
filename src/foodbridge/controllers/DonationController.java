package foodbridge.controllers;

import java.time.LocalDate;
import java.util.List;

import foodbridge.models.donations.Donation;
import foodbridge.models.donations.DonationDetails;
import foodbridge.models.donations.DonationProcessingResult;
import foodbridge.models.donations.FoodDonation;
import foodbridge.models.donations.FoodItem;
import foodbridge.models.donations.MoneyDonation;
import foodbridge.models.donations.Receipt;
import foodbridge.models.lookups.DonationStatus;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;
import foodbridge.models.users.Donor;
import foodbridge.patterns.decorator.ReceiptOptions;
import foodbridge.repositories.DonationRepository;
import foodbridge.repositories.DonationStatusRepository;
import foodbridge.repositories.FoodCategoryRepository;
import foodbridge.repositories.UnitRepository;
import foodbridge.services.DonationService;
import foodbridge.services.ReceiptService;

/**
 * Donations, payments and receipts.
 */
public class DonationController {
    private DonationService donationService;
    private ReceiptService receiptService;
    private DonationRepository donationRepository;
    private DonationStatusRepository donationStatusRepository;
    private FoodCategoryRepository foodCategoryRepository;
    private UnitRepository unitRepository;

    public DonationController() {
        donationRepository = new DonationRepository();
        donationService = new DonationService(donationRepository);
        donationStatusRepository = new DonationStatusRepository();
        foodCategoryRepository = new FoodCategoryRepository();
        unitRepository = new UnitRepository();
        // TODO (Decorator step): create the ReceiptService.
    }

    public int createDonation(String type, DonationDetails request) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // TODO (Factory step): replace with createDonation(...) through the DonationFactory;
    // the direct `new` below is temporary so the screens can be tested.
    public int createMoneyDonation(Donor donor, double amount, String method) {
        MoneyDonation donation = new MoneyDonation(donor);
        donation.setAmount(amount);
        donation.setMethod(method);
        donationRepository.save(donation);
        return donation.getId();
    }

    // TODO (Factory step): replace with createDonation(...) through the DonationFactory.
    public int createFoodDonation(Donor donor, List<FoodItem> items, LocalDate expiryDate, String pickupWindow) {
        FoodDonation donation = new FoodDonation(donor);
        for (FoodItem item : items) {
            donation.addItem(item);
        }
        donation.setExpiryDate(expiryDate);
        donation.setPickupWindow(pickupWindow);
        donationRepository.save(donation);
        return donation.getId();
    }

    public List<Donation> listDonations(int donorId) {
        return donationRepository.findByDonor(donorId);
    }

    public Donation getDonation(int donationId) {
        return donationRepository.findById(donationId);
    }

    /** Runs the payment Strategy for the donation's method (DonationService is the Context). */
    public DonationProcessingResult payMoneyDonation(int donationId) {
        return donationService.processMoneyDonation(donationId);
    }

    /** Returns false if the donation is no longer Posted. */
    public boolean cancelDonation(int donationId) {
        Donation donation = donationRepository.findById(donationId);
        if (donation == null) {
            throw new IllegalArgumentException("No donation with id " + donationId);
        }
        if (!donation.cancel(donationStatusRepository.findByName(DonationStatus.CANCELLED))) {
            return false;
        }
        donationRepository.save(donation);
        return true;
    }

    public List<FoodCategory> listFoodCategories() {
        return foodCategoryRepository.findAll();
    }

    public List<Unit> listUnits() {
        return unitRepository.findAll();
    }

    public Receipt createReceipt(int donationId) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public String getReceiptText(int receiptId, ReceiptOptions options) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
