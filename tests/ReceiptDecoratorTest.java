import java.time.LocalDate;
import java.util.Locale;

import foodbridge.models.donations.*;
import foodbridge.models.users.Donor;
import foodbridge.patterns.decorator.*;

/** Standalone checks; no database or external test library required. */
public class ReceiptDecoratorTest {
    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        Donor donor = new Donor();
        donor.setName("Receipt test donor");
        MoneyDonation money = new MoneyDonation(donor);
        money.setId(12);
        money.setAmount(150.25);
        money.setMethod("cash");
        money.markAsPaid("CASH-12");
        Receipt receipt = new Receipt(money, "FB-12", LocalDate.of(2026, 11, 3));
        ReceiptComponent basic = new BasicReceipt(receipt);
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            String plain = basic.generate();
            check(plain.contains("150.25") && plain.contains("CASH-12"), "Money and reference formatted");
            check(plain.contains("2026-11-03") && plain.contains(donor.getName()), "Receipt metadata formatted");
            check(!plain.contains("Thank you") && !plain.contains("Tax information"), "Base has no optional sections");
            String thankYou = new ThankYouDecorator(basic).generate();
            String tax = new TaxInfoDecorator(basic).generate();
            String both = new TaxInfoDecorator(new ThankYouDecorator(basic)).generate();
            check(thankYou.startsWith(plain) && !thankYou.contains("Tax information"), "Thank-you only");
            check(tax.startsWith(plain) && !tax.contains("Thank you"), "Tax only");
            check(both.startsWith(thankYou) && both.contains("Tax information"), "Decorators stack");
            check(basic.generate().equals(plain), "Wrapping does not mutate the base");
            check(both.equals(new TaxInfoDecorator(new ThankYouDecorator(basic)).generate()), "Repeatable output");
            FoodDonation food = new FoodDonation(donor);
            FoodItem item = new FoodItem();
            item.setName("Rice"); item.setQuantity(2.5); food.addItem(item);
            String foodText = new BasicReceipt(new Receipt(food, "FB-FOOD", LocalDate.now())).generate();
            check(foodText.contains("Rice") && foodText.contains("2.5"), "Food items formatted");
            check(!foodText.contains("Amount:"), "Food has no invented monetary value");
            try {
                new ThankYouDecorator(null);
                throw new AssertionError("Null wrapped receipt accepted");
            } catch (IllegalArgumentException expected) { }
            System.out.println("PASS: receipt decorator checks.");
        } finally {
            Locale.setDefault(previous);
        }
    }
}
