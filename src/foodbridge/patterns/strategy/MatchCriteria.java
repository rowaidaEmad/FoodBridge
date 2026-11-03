package foodbridge.patterns.strategy;

/**
 * What a task or event needs from a volunteer. Activity is "Pickup" or an event type name.
 */
public class MatchCriteria {
    private final String activity;
    private final String requiredSkill;
    private final String availabilityKeyword;
    private final boolean needsTransport;

    public MatchCriteria(String activity, String requiredSkill, String availabilityKeyword, boolean needsTransport) {
        this.activity = activity;
        this.requiredSkill = requiredSkill;
        this.availabilityKeyword = availabilityKeyword;
        this.needsTransport = needsTransport;
    }

    public String getActivity() {
        return activity;
    }

    public String getRequiredSkill() {
        return requiredSkill;
    }

    public String getAvailabilityKeyword() {
        return availabilityKeyword;
    }

    public boolean needsTransport() {
        return needsTransport;
    }
}
