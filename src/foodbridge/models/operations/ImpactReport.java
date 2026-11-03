package foodbridge.models.operations;

import java.util.Map;

import foodbridge.models.users.Volunteer;

/**
 * Core domain. Summarizes Distributions.
 */
public class ImpactReport {
    private String period;
    private int mealsDelivered;
    private int familiesServed;
    private Map<Volunteer, Integer> deliveriesPerVolunteer;

    public ImpactReport() {
        // TODO: implement
    }

    public int getMealsDelivered() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public int getFamiliesServed() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
