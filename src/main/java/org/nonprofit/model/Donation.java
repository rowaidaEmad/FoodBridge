package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public abstract class Donation {
    private long id;
    private long donorId;
    private Long areaId;
    private LocalDate date;
    private long statusId;
    public Donation() { }
    public long getId() { return id; }
    public void setId(long value) { this.id = value; }
    public long getDonorId() { return donorId; }
    public void setDonorId(long value) { this.donorId = value; }
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long value) { this.areaId = value; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate value) { this.date = value; }
    public long getStatusId() { return statusId; }
    public void setStatusId(long value) { this.statusId = value; }
    public abstract String getType();
}
