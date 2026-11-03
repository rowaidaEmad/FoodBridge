package foodbridge.patterns.decorator;

/**
 * DECORATOR - ConcreteDecorator.
 */
public class TaxInfoDecorator extends ReceiptDecorator {
    public TaxInfoDecorator(ReceiptComponent wrapped) {
        super(wrapped);
    }

    @Override
    public String generate() {
        return super.generate() + "\n\nTax information: Keep this receipt for your records. "
                + "Tax eligibility depends on applicable rules; no deduction or valuation is calculated here.";
    }
}
