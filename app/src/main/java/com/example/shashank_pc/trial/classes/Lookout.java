package com.example.shashank_pc.trial.classes;

import java.util.ArrayList;
import java.util.List;

public class Lookout extends Alert {
    private String createdBy;
    private String createdByName;
    private Long fromTime;
    private Long toTime;
    private boolean isEnabled;
    private List<AlertContact> selectedContacts = new ArrayList<>();

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public Long getFromTime() {
        return fromTime;
    }

    public void setFromTime(Long fromTime) {
        this.fromTime = fromTime;
    }

    public Long getToTime() {
        return toTime;
    }

    public void setToTime(Long toTime) {
        this.toTime = toTime;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public List<AlertContact> getSelectedContacts() {
        return selectedContacts;
    }

    public void setSelectedContacts(List<AlertContact> selectedContacts) {
        this.selectedContacts = selectedContacts;
    }
}
