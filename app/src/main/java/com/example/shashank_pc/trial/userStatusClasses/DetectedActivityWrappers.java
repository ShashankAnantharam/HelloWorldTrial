package com.example.shashank_pc.trial.userStatusClasses;

import com.google.android.gms.location.DetectedActivity;

public class DetectedActivityWrappers {

    String activityType;
    Integer confidence;

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    DetectedActivityWrappers(){};

    DetectedActivityWrappers(DetectedActivity activity)
    {
        confidence = activity.getConfidence();

        switch (activity.getType()) {
            case DetectedActivity.IN_VEHICLE: {
                activityType = "In Vehicle";
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                activityType = "On Bicycle";
                break;
            }
            case DetectedActivity.ON_FOOT: {
                activityType = "On Foot";
                break;
            }
            case DetectedActivity.RUNNING: {
                activityType = "Running";
                break;
            }
            case DetectedActivity.STILL: {
                activityType = "Still";
                break;
            }
            case DetectedActivity.TILTING: {
                activityType = "Tilting";
                break;
            }
            case DetectedActivity.WALKING: {
                activityType = "Walking";
                break;
            }
            case DetectedActivity.UNKNOWN: {
                activityType = "Unknown";
                break;
            }
        }

    }

}
