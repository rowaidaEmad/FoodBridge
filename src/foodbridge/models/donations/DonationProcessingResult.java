package foodbridge.models.donations;

/**
 * Returned by every DonationProcessingStrategy (helper, not a pattern role).
 */
public class DonationProcessingResult {
    private final boolean successful;
    private final String reference;
    private final String message;

    public DonationProcessingResult(boolean successful, String reference, String message) {
        this.successful = successful;
        this.reference = reference;
        this.message = message;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getReference() {
        return reference;
    }

    public String getMessage() {
        return message;
    }
}
