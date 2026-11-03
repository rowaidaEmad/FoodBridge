package foodbridge.models.donations;

/**
 * Returned by every DonationProcessingStrategy.
 */
public class DonationProcessingResult {
    private boolean successful;
    private String reference;
    private String message;

    public DonationProcessingResult(boolean successful, String reference, String message) {
        // TODO: implement
    }

    public boolean isSuccessful() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public String getMessage() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
