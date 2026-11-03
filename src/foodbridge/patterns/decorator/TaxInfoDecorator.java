package foodbridge.patterns.decorator;

/**
 * DECORATOR - ConcreteDecorator.
 */
public class TaxInfoDecorator extends ReceiptDecorator {
    public TaxInfoDecorator(ReceiptComponent wrapped) {
        super(wrapped);
        // TODO: implement
    }

    @Override
    public String generate() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
