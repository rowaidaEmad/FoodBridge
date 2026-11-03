package foodbridge.patterns.decorator;

/**
 * DECORATOR - Decorator. Wraps a ReceiptComponent.
 */
public abstract class ReceiptDecorator implements ReceiptComponent {
    protected ReceiptComponent wrapped;

    public ReceiptDecorator(ReceiptComponent wrapped) {
        // TODO: implement
    }

    @Override
    public String generate() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
