package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public class EventType {
    private long id;
    private String name;
    public EventType() { }
    public long getId() { return id; }
    public void setId(long value) { this.id = value; }
    public String getName() { return name; }
    public void setName(String value) { this.name = value; }
}
