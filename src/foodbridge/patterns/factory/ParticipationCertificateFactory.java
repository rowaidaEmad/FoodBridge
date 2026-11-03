package foodbridge.patterns.factory;

import foodbridge.models.operations.Certificate;
import foodbridge.models.operations.ParticipationCertificate;

/**
 * FACTORY METHOD - ConcreteCreator.
 */
public class ParticipationCertificateFactory extends CertificateFactory {
    @Override
    protected Certificate createCertificate() {
        return new ParticipationCertificate();
    }
}
