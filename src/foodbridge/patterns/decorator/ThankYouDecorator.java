package foodbridge.patterns.decorator;

/**
 * DECORATOR - ConcreteDecorator.
 */
public class ThankYouDecorator extends ReceiptDecorator {
    public ThankYouDecorator(ReceiptComponent wrapped) {
        super(wrapped);
    }

    @Override
    public String generate() {
        return super.generate() + "\n\nThank you for your donation to FoodBridge!";
    }
}
