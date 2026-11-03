package foodbridge.controllers;

import java.util.List;

import foodbridge.models.donations.DonationDetails;
import foodbridge.models.donations.DonationProcessingResult;
import foodbridge.models.donations.Receipt;
import foodbridge.models.lookups.Area;
import foodbridge.models.lookups.FoodCategory;
import foodbridge.models.lookups.Unit;
import foodbridge.patterns.decorator.ReceiptOptions;
import foodbridge.repositories.AreaRepository;
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

    public DonationController() {
        donationService = new DonationService();
    }

    public int createDonation(String type, DonationDetails request) {
        return donationService.createDonation(type, request);
    }

    public List<Area> listAreas() {
        return new AreaRepository().findAll();
    }

    public List<FoodCategory> listCategories() {
        return new FoodCategoryRepository().findAll();
    }

    public List<Unit> listUnits() {
        return new UnitRepository().findAll();
    }

    public DonationProcessingResult payMoneyDonation(int donationId) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
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
