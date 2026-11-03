package foodbridge.patterns.decorator;

/**
 * Helper (not drawn in the diagram): the 'options' passed to generateReceiptText.
 */
public class ReceiptOptions {
    private final boolean thankYou;
    private final boolean taxInfo;

    public ReceiptOptions(boolean thankYou, boolean taxInfo) {
        this.thankYou = thankYou;
        this.taxInfo = taxInfo;
    }

    public boolean isThankYou() {
        return thankYou;
    }

    public boolean isTaxInfo() {
        return taxInfo;
    }
}
