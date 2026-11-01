package org.nonprofit.patterns.decorator;


public class TaxInfoDecorator extends ReceiptDecorator {
    public TaxInfoDecorator(ReceiptComponent wrapped) {super(wrapped);}
    @Override public String generate() {return super.generate()+"\nTax details: sample only; not a tax certificate.";}
}

