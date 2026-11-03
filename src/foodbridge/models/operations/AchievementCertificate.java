package foodbridge.models.operations;

/**
 * FACTORY METHOD - ConcreteProduct.
 */
public class AchievementCertificate extends Certificate {
    public static final String TYPE = "ACHIEVEMENT";

    @Override
    public String getType() {
        return TYPE;
    }
}
