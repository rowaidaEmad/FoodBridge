import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import foodbridge.controllers.DonationController;
import foodbridge.models.donations.*;
import foodbridge.models.lookups.DonationStatus;
import foodbridge.models.users.Donor;
import foodbridge.patterns.decorator.ReceiptOptions;
import foodbridge.patterns.singleton.DatabaseManager;
import foodbridge.repositories.*;

/** Integration tests; never run against application data. */
public class ReceiptModuleTest {
    private static int assertions;
    static void check(boolean ok, String message) {
        assertions++;
        if (!ok) throw new AssertionError(message);
    }
    static void rejects(Runnable action, String message) {
        assertions++;
        try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        try (Connection c = DatabaseManager.getInstance().getConnection()) {
            if (!"foodbridge_receipt_test".equals(c.getCatalog())) {
                throw new IllegalStateException("Refusing to modify a non-test database");
            }
        }
        ReceiptDecoratorTest.main(new String[0]);
        Donor donor = new Donor();
        donor.setName("Receipt test donor");
        donor.changeEmail("receipt@example.test");
        donor.setPasswordHash("test-only");
        new UserRepository().add(donor);
        DonationRepository donations = new DonationRepository();
        MoneyDonation money = new MoneyDonation(donor);
        money.setAmount(250.5); money.setMethod("cash"); donations.save(money);
        DonationController controller = new DonationController();
        rejects(() -> controller.createReceipt(money.getId()), "Unpaid donation rejected");
        money.markAsPaid("TEST-PAYMENT"); donations.save(money);
        Receipt receipt = controller.createReceipt(money.getId());
        check(receipt.getId() > 0, "Generated receipt ID returned");
        check(controller.createReceipt(money.getId()).getId() == receipt.getId(), "Repeated creation returns same receipt");
        ReceiptRepository repository = new ReceiptRepository();
        Receipt loaded = repository.findById(receipt.getId());
        check(loaded.getReceiptNumber().equals(receipt.getReceiptNumber()), "Receipt number persists");
        check(loaded.getIssueDate().equals(LocalDate.now()), "Issue date persists");
        check(loaded.getDonation() instanceof MoneyDonation, "Donation subtype restored");
        for (boolean thankYou : new boolean[] { false, true }) {
            for (boolean tax : new boolean[] { false, true }) {
                String text = controller.getReceiptText(receipt.getId(), new ReceiptOptions(thankYou, tax));
                check(text.contains("Thank you") == thankYou, "Thank-you option applied");
                check(text.contains("Tax information") == tax, "Tax option applied");
                check(text.contains("250.50") && text.contains("TEST-PAYMENT"), "Base output retained");
            }
        }
        rejects(() -> controller.createReceipt(Integer.MAX_VALUE), "Missing donation rejected");
        rejects(() -> controller.getReceiptText(Integer.MAX_VALUE, new ReceiptOptions(false, false)), "Missing receipt rejected");
        rejects(() -> controller.getReceiptText(receipt.getId(), null), "Null options rejected");

        FoodDonation food = new FoodDonation(donor);
        FoodItem item = new FoodItem(); item.setName("Rice"); item.setQuantity(3);
        item.setCategory(new FoodCategoryRepository().findAll().get(0));
        item.setUnit(new UnitRepository().findAll().get(0));
        food.addItem(item); donations.save(food);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<Receipt>> pending = new ArrayList<>();
            for (int i = 0; i < 4; i++) pending.add(pool.submit(() -> {
                start.await(); return controller.createReceipt(food.getId());
            }));
            start.countDown();
            Receipt first = pending.get(0).get(15, TimeUnit.SECONDS);
            for (Future<Receipt> future : pending) {
                Receipt result = future.get(15, TimeUnit.SECONDS);
                check(result.getId() == first.getId()
                        && result.getReceiptNumber().equals(first.getReceiptNumber()), "Concurrent creation is idempotent");
            }
        } finally { pool.shutdownNow(); }
        Receipt foodReceipt = repository.findByDonation(food.getId());
        check(controller.getReceiptText(foodReceipt.getId(), new ReceiptOptions(true, true)).contains("Rice"), "Food receipt generated");
        check(repository.findAll().size() == 2, "Exactly one receipt per donation");
        Receipt changed = new Receipt(food, "UPDATED-FOOD", LocalDate.of(2026, 1, 1));
        changed.setId(foodReceipt.getId()); repository.update(changed);
        check(repository.findById(changed.getId()).getReceiptNumber().equals("UPDATED-FOOD"), "Update persists");
        repository.delete(changed.getId());
        check(repository.findById(changed.getId()) == null, "Delete persists");
        repository.add(changed);
        check(repository.findById(changed.getId()).getIssueDate().equals(changed.getIssueDate()), "Direct add persists");
        rejects(() -> repository.add(new Receipt()), "Incomplete receipt rejected");
        food.updateStatus(new DonationStatusRepository().findByName(DonationStatus.CANCELLED));
        donations.save(food);
        rejects(() -> controller.createReceipt(food.getId()), "Cancelled donation rejected");
        System.out.println("PASS: " + assertions + " receipt service/database assertions.");
    }
}
