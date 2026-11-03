package foodbridge.controllers;

import java.util.List;

import foodbridge.models.donations.Donation;
import foodbridge.models.donations.DonationDetails;
import foodbridge.models.donations.DonationProcessingResult;
import foodbridge.models.donations.Receipt;
import foodbridge.models.lookups.Area;
import foodbridge.models.lookups.DonationStatus;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;
import foodbridge.patterns.decorator.ReceiptOptions;
import foodbridge.repositories.AreaRepository;
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
        receiptService = new ReceiptService();
    }

    /** type is "money" or "food"; DonationService picks the matching DonationFactory. */
    public int createDonation(String type, DonationDetails request) {
        return donationService.createDonation(type, request);
    }

    public List<Area> listAreas() {
        return new AreaRepository().findAll();
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
        return receiptService.createReceipt(donationId);
    }

    public String getReceiptText(int receiptId, ReceiptOptions options) {
        return receiptService.generateReceiptText(receiptId, options);
    }
}
