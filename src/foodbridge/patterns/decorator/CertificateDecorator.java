package foodbridge.patterns.decorator;

/**
 * DECORATOR - Decorator. Wraps a CertificateComponent.
 */
public abstract class CertificateDecorator implements CertificateComponent {
    protected final CertificateComponent wrapped;

    protected CertificateDecorator(CertificateComponent wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String generate() {
        return wrapped.generate();
    }
}
