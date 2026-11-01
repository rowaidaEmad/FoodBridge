package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class ImpactReport {
    private String period;
    private int mealsDelivered;
    private int familiesServed;
    private Map<Long,Integer> deliveriesPerVolunteer = new HashMap<>();
    public ImpactReport() { }
    public String getPeriod() { return period; }
    public void setPeriod(String value) { this.period = value; }
    public int getMealsDelivered() { return mealsDelivered; }
    public void setMealsDelivered(int value) { this.mealsDelivered = value; }
    public int getFamiliesServed() { return familiesServed; }
    public void setFamiliesServed(int value) { this.familiesServed = value; }
    public Map<Long,Integer> getDeliveriesPerVolunteer() { return deliveriesPerVolunteer; }
    public void setDeliveriesPerVolunteer(Map<Long,Integer> value) { this.deliveriesPerVolunteer = value; }
}
