package foodbridge.models.operations;

/**
 * FACTORY METHOD - ConcreteProduct.
 */
public class ParticipationCertificate extends Certificate {
    public static final String TYPE = "PARTICIPATION";

    @Override
    public String getType() {
        return TYPE;
    }
}
