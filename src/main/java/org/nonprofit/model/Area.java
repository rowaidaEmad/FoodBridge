package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class Area {
    private long id;
    private String city;
    private String district;
    public Area() { }
    public long getId() { return id; }
    public void setId(long value) { this.id = value; }
    public String getCity() { return city; }
    public void setCity(String value) { this.city = value; }
    public String getDistrict() { return district; }
    public void setDistrict(String value) { this.district = value; }
}
