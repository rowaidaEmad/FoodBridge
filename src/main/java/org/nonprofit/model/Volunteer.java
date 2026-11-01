package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class Volunteer extends User {
    private String availability;
    private boolean hasTransport;
    private double totalHours;
    public Volunteer() { }
    public String getAvailability() { return availability; }
    public void setAvailability(String value) { this.availability = value; }
    public boolean isHasTransport() { return hasTransport; }
    public void setHasTransport(boolean value) { this.hasTransport = value; }
    public double getTotalHours() { return totalHours; }
    public void setTotalHours(double value) { this.totalHours = value; }
    public void addHours(double hours) { if(hours < 0) throw new IllegalArgumentException("Negative hours"); totalHours += hours; }
}
