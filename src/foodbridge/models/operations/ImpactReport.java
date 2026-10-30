package foodbridge.models.operations;

import java.util.List;
import java.util.Map;
import foodbridge.models.users.Volunteer;

public class ImpactReport {
    private String period;
    private int mealsDelivered;
    private int familiesServed;
    private Map<Volunteer, Integer> deliveriesPerVolunteer;

    public void generate(List<Distribution> records) {
        // TODO
    }
}
