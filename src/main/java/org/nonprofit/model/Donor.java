package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class Donor extends User {
    private String donorType;
    private String organizationName;
    public Donor() { }
    public String getDonorType() { return donorType; }
    public void setDonorType(String value) { this.donorType = value; }
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String value) { this.organizationName = value; }
}
