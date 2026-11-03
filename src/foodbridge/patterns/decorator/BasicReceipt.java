package foodbridge.patterns.decorator;

import foodbridge.models.donations.Receipt;

/**
 * DECORATOR - ConcreteComponent. Formats a Receipt.
 */
public class BasicReceipt implements ReceiptComponent {
    private Receipt receipt;

    public BasicReceipt(Receipt receipt) {
        // TODO: implement
    }

    @Override
    public String generate() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
