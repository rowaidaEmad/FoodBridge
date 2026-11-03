package foodbridge.models.operations;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import foodbridge.models.users.Volunteer;

/** Aggregated delivery activity; a meal conversion rule has not been defined. */
public class ImpactReport {
    private String period = "";
    private int mealsDelivered = -1;
    private int familiesServed;
    private int deliveryCount;
    private Map<Volunteer,Integer> deliveriesPerVolunteer = Map.of();
    public ImpactReport() { }
    public ImpactReport(String period,int familiesServed,int deliveryCount,Map<Volunteer,Integer> deliveries) {
        this.period=period; this.familiesServed=familiesServed; this.deliveryCount=deliveryCount;
        this.deliveriesPerVolunteer=Collections.unmodifiableMap(new LinkedHashMap<>(deliveries));
    }
    public String getPeriod() { return period; }
    public boolean isMealCountAvailable() { return mealsDelivered>=0; }
    public int getMealsDelivered() {
        if(!isMealCountAvailable()) throw new IllegalStateException("A meal-count rule has not been defined.");
        return mealsDelivered;
    }
    public int getFamiliesServed() { return familiesServed; }
    public int getDeliveryCount() { return deliveryCount; }
    public Map<Volunteer,Integer> getDeliveriesPerVolunteer() { return deliveriesPerVolunteer; }
}
