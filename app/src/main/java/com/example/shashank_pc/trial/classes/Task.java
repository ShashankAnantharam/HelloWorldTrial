package com.example.shashank_pc.trial.classes;

import java.util.HashMap;
import java.util.Map;

public class Task extends Alert {

    private String description;
    private CreatedBy createdBy;
    private Long completedAt;
    private String completedBy;
    private Long deadline;
    private boolean hasDeadline;
    private Map<String,AlertContact> selectedContacts = new HashMap<>();


    public CreatedBy getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(CreatedBy createdBy) {
        this.createdBy = createdBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(String completedBy) {
        this.completedBy = completedBy;
    }

    public Long getDeadline() {
        return deadline;
    }

    public void setDeadline(Long deadline) {
        this.deadline = deadline;
    }

    public boolean isHasDeadline() {
        return hasDeadline;
    }

    public void setHasDeadline(boolean hasDeadline) {
        this.hasDeadline = hasDeadline;
    }

    public Map<String, AlertContact> getSelectedContacts() {
        return selectedContacts;
    }

    public void setSelectedContacts(Map<String, AlertContact> selectedContacts) {
        this.selectedContacts = selectedContacts;
    }
}
