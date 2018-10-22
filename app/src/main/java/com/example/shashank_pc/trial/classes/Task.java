package com.example.shashank_pc.trial.classes;

public class Task extends Alert {
    class CreatedBy{
        String id;
        String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private String description;
    private CreatedBy createdBy;
    private Long completedAt;
    private String completedBy;
    private Long deadline;
    private boolean hasDeadline;


    public CreatedBy getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(CreatedBy createdBy) {
        this.createdBy = createdBy;
    }
}
