package org.nonprofit.patterns.decorator;


public class ThankYouDecorator extends ReceiptDecorator {
    public ThankYouDecorator(ReceiptComponent wrapped) {super(wrapped);}
    @Override public String generate() {return super.generate()+"\nThank you for supporting FoodBridge!";}
}

