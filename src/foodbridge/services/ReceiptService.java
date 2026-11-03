package foodbridge.services;

import foodbridge.models.donations.Receipt;
import foodbridge.patterns.decorator.ReceiptOptions;
import foodbridge.repositories.ReceiptRepository;
import foodbridge.repositories.DonationRepository;
import foodbridge.models.donations.Donation;
import foodbridge.models.donations.MoneyDonation;
import foodbridge.models.lookups.DonationStatus;
import foodbridge.patterns.decorator.BasicReceipt;
import foodbridge.patterns.decorator.ReceiptComponent;
import foodbridge.patterns.decorator.ThankYouDecorator;
import foodbridge.patterns.decorator.TaxInfoDecorator;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DECORATOR - Client. Creates Receipts and builds their text.
 */
public class ReceiptService {
    private ReceiptRepository repository;

    public ReceiptService() {
        repository = new ReceiptRepository();
    }

    public Receipt createReceipt(int donationId) {
        Donation donation = new DonationRepository().findById(donationId);
        if (donation == null) {
            throw new IllegalArgumentException("No donation with id " + donationId);
        }
        if (donation.getStatus() != null
                && DonationStatus.CANCELLED.equals(donation.getStatus().getName())) {
            throw new IllegalArgumentException("Cancelled donations cannot receive a receipt");
        }
        if (donation instanceof MoneyDonation money && !money.isPaid()) {
            throw new IllegalArgumentException("Pay the money donation before creating a receipt");
        }
        Receipt existing = repository.findByDonation(donationId);
        if (existing != null) return existing;
        return repository.createIfAbsent(new Receipt(donation, "FB-" + UUID.randomUUID(), LocalDate.now()));
    }

    public String generateReceiptText(int receiptId, ReceiptOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("Receipt options are required");
        }
        Receipt receipt = repository.findById(receiptId);
        if (receipt == null) {
            throw new IllegalArgumentException("No receipt with id " + receiptId);
        }
        ReceiptComponent component = new BasicReceipt(receipt);
        if (options.isThankYou()) component = new ThankYouDecorator(component);
        if (options.isTaxInfo()) component = new TaxInfoDecorator(component);
        return component.generate();
    }
}
