package org.nonprofit.model;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public abstract class User {
    private long id;
    private String name;
    private String email;
    private String passwordHash;
    private String phone;
    private String address;
    private Long areaId;
    private boolean active;
    private LocalDateTime createdAt;
    public User() { }
    public long getId() { return id; }
    public void setId(long value) { this.id = value; }
    public String getName() { return name; }
    public void setName(String value) { this.name = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { this.email = value; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String value) { this.passwordHash = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { this.phone = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { this.address = value; }
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long value) { this.areaId = value; }
    public boolean isActive() { return active; }
    public void setActive(boolean value) { this.active = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
