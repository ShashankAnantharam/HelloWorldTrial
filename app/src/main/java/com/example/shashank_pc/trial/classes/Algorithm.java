package com.example.shashank_pc.trial.classes;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import static java.lang.Math.signum;

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

    public static LatLng footOfPerpendicular(double x_curr, double y_curr, double x_prev, double y_prev, double x_alert, double y_alert)
    {
        //Getting line equation ax+by+c=0;
        double a = y_curr - y_prev;
        double b = x_prev - x_curr;
        double c = y_prev*(x_curr - x_prev) - x_prev*(y_curr-y_prev);

        if(a==0 && b==0)
        {
            return new LatLng(x_curr,y_curr);
        }

        //getting foot of perpendicular
        double T = (-1)*(a*x_alert + b*y_alert + c)/(a*a+b*b);
        double x_perp = x_alert + a*T;
        double y_perp = y_alert + b*T;

        return new LatLng(x_perp, y_perp);
    }


     public static boolean isInBetween(double x_curr, double y_curr, double x_prev, double y_prev, double x_perp, double y_perp)
    {
        //check whether perpendicular is between the current and previous position
        if((signum(x_curr - x_perp)==signum(x_perp - x_prev)) &&
                (signum(y_curr - y_perp)==signum(y_perp - y_prev)))
            return true;

        return false;
    }


    public static boolean shouldAlertTrigger(double x_curr, double y_curr, double x_prev, double y_prev, Alert alert)
    {
        //Breakdown into variables
        double x_alert = alert.getLocation().getLatitude();
        double y_alert = alert.getLocation().getLongitude();

        //Get foot of perpendicular
        LatLng perp = footOfPerpendicular(x_curr, y_curr, x_prev, y_prev, x_alert, y_alert);
        double x_perp = perp.latitude;
        double y_perp = perp.longitude;
        if(isInBetween(x_curr, y_curr, x_prev, y_prev, x_perp, y_perp))
        {
            //Perpendicular point is in between current and previous position
            //Check only for distance with perpendicular
            if(isWithinAlert(x_perp,y_perp,alert))
                return true;

        }
        else
        {
            //Perpendicular point is not in between current and previous position
            //Check for distance with current and previous position
            if(isWithinAlert(x_curr,y_curr,alert)|| isWithinAlert(x_prev,y_prev,alert))
            {
                return true;
            }

        }
        return false;
    }

    public static boolean isLookoutValid(Alert alert)
    {
        //TODO Later
        if(alert instanceof Lookout)
        {
            return true;
        }
        else if(alert instanceof Task)
        {
            return true;
        }

        return false;

    }







}
