package foodbridge.patterns.decorator;

/**
 * DECORATOR - Decorator. Wraps a ReceiptComponent.
 */
public abstract class ReceiptDecorator implements ReceiptComponent {
    protected final ReceiptComponent wrapped;

    public ReceiptDecorator(ReceiptComponent wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("A wrapped receipt is required");
        }
        this.wrapped = wrapped;
    }

    @Override
    public String generate() {
        return wrapped.generate();
    }
}
