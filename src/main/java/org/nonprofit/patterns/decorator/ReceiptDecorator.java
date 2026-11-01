package org.nonprofit.patterns.decorator;


public abstract class ReceiptDecorator implements ReceiptComponent {
    protected final ReceiptComponent wrapped;
    protected ReceiptDecorator(ReceiptComponent wrapped) {this.wrapped=wrapped;}
    public String generate() {return wrapped.generate();}
}

