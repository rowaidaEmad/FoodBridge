package foodbridge.patterns.factory;

import foodbridge.models.operations.AchievementCertificate;
import foodbridge.models.operations.Certificate;

/**
 * FACTORY METHOD - ConcreteCreator.
 */
public class AchievementCertificateFactory extends CertificateFactory {
    @Override
    protected Certificate createCertificate() {
        return new AchievementCertificate();
    }
}
