package foodbridge.patterns.decorator;

/**
 * DECORATOR - ConcreteDecorator. Adds the FoodBridge signature.
 */
public class SignatureDecorator extends CertificateDecorator {
    public SignatureDecorator(CertificateComponent wrapped) {
        super(wrapped);
    }

    @Override
    public String generate() {
        return super.generate() + "\n\nSigned, the FoodBridge team";
    }
}
