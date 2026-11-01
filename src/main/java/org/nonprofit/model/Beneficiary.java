package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class Beneficiary extends User {
    private int householdSize;
    private String needs;
    public Beneficiary() { }
    public int getHouseholdSize() { return householdSize; }
    public void setHouseholdSize(int value) { this.householdSize = value; }
    public String getNeeds() { return needs; }
    public void setNeeds(String value) { this.needs = value; }
}
