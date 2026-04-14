package com.example.stadiumflow.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Zone {
    @Id
    private String id;
    private String name;
    private int waitTime;
    private int density;

    // Default Constructor for JPA
    public Zone() {}

    public Zone(String id, String name, int waitTime, int density) {
        this.id = id;
        this.name = name;
        this.waitTime = waitTime;
        this.density = density;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getWaitTime() { return waitTime; }
    public void setWaitTime(int waitTime) { this.waitTime = waitTime; }
    public int getDensity() { return density; }
    public void setDensity(int density) { this.density = density; }
}
