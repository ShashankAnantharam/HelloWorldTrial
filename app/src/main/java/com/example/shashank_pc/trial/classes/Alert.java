package com.example.shashank_pc.trial.classes;

import java.util.ArrayList;
import java.util.List;

public class Alert {
    private String address;
    private boolean isDaily;
    private Location location;
    private String name;
    private Double radius;
    private List<AlertContact> selectedContacts = new ArrayList<>();

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isDaily() {
        return isDaily;
    }

    public void setDaily(boolean daily) {
        isDaily = daily;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    public List<AlertContact> getSelectedContacts() {
        return selectedContacts;
    }

    public void setSelectedContacts(List<AlertContact> selectedContacts) {
        this.selectedContacts = selectedContacts;
    }
}
