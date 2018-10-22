package com.example.shashank_pc.trial.classes;

import android.location.Location;

public class Algorithm {

    public static boolean isWithinAlert(double latitude, double longitude, Alert alert)
    {
        Location alertLoc = new Location("alertLoc");
        alertLoc.setLatitude(alert.getLocation().getLatitude());
        alertLoc.setLongitude(alert.getLocation().getLongitude());
        Location userLoc = new Location("userLoc");
        userLoc.setLatitude(latitude);
        userLoc.setLongitude(longitude);

        //Distance in meters
        if(userLoc.distanceTo(alertLoc)<=alert.getRadius())
            return true;

        return false;

    }


    

}
