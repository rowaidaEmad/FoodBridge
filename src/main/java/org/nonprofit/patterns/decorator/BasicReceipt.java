package org.nonprofit.patterns.decorator;


import org.nonprofit.model.Receipt;
public class BasicReceipt implements ReceiptComponent {
    private final Receipt receipt;
    public BasicReceipt(Receipt receipt) {this.receipt=receipt;}
    public String generate() {return "FoodBridge Receipt "+receipt.getReceiptNumber()+" (donation #"+receipt.getDonationId()+")";}
}

