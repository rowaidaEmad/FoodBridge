package foodbridge.patterns.decorator;

/**
 * DECORATOR - ConcreteDecorator.
 */
public class ThankYouDecorator extends ReceiptDecorator {
    public ThankYouDecorator(ReceiptComponent wrapped) {
        super(wrapped);
        // TODO: implement
    }

    @Override
    public String generate() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
