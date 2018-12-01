package com.example.shashank_pc.trial.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.shashank_pc.trial.classes.Alert;
import com.example.shashank_pc.trial.classes.Location;
import com.example.shashank_pc.trial.classes.Lookout;
import com.example.shashank_pc.trial.userStatusClasses.DetectedActivityWrappers;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class BasicHelper {

    private static boolean wasAppInForground= false;

    public static void setWasAppInForground(boolean wasAppInForground) {
        BasicHelper.wasAppInForground = wasAppInForground;
    }

    public static boolean wasAppInForground() {
        return wasAppInForground;
    }

    public static void populateStates(Context context)
    {
        FirebaseDatabase.getInstance().getReference("Testing/ErrFlag").setValue(getErrorFlag(context));
        FirebaseDatabase.getInstance().getReference("Testing/serviceStatus").setValue(getServiceStatus(context));

    }

    public static List<Alert> populateAlerts(Map<String,Alert> alertMap)
    {
        List<Alert> alerts = new ArrayList<>();
        for (Map.Entry<String, Alert> entry : alertMap.entrySet()) {
            alerts.add(entry.getValue());
        }
        return alerts;
    }

    public static void setUserMovementState(Context context, DetectedActivityWrappers userState)
    {
        SharedPreferences fixed_values = context.getSharedPreferences("FIXED_VALUES", MODE_PRIVATE);
        SharedPreferences.Editor edit = fixed_values.edit();
        String userStateString = new Gson().toJson(userState);
        edit.putString("USER_STATE",userStateString);
        edit.commit();
    }

    public static DetectedActivityWrappers getUserMovementState(Context context)
    {
        SharedPreferences fixed_values = context.getSharedPreferences("FIXED_VALUES", MODE_PRIVATE);
        return new Gson().fromJson(
                fixed_values.getString("USER_STATE",""),
                DetectedActivityWrappers.class);
    }



    public static void setLastStillTime(Context context, Long timestamp)
    {
        SharedPreferences fixed_values = context.getSharedPreferences("FIXED_VALUES", MODE_PRIVATE);
        SharedPreferences.Editor edit = fixed_values.edit();
        edit.putLong("LAST_STILL_TIME",timestamp);
        edit.commit();
    }

    public static Long getLastStillTime(Context context) {
        SharedPreferences fixed_values = context.getSharedPreferences("FIXED_VALUES", MODE_PRIVATE);
        return fixed_values.getLong("LAST_STILL_TIME",System.currentTimeMillis());
    }

    public static boolean getErrorFlag(Context context){
        SharedPreferences fixed_values = context.getSharedPreferences("FIXED_VALUES", MODE_PRIVATE);
        if(fixed_values.getBoolean("ERROR_FLAG",false)){
            return false;
        }
        return true;
    }

    public static void setErrorFlag(Context context,Boolean value){
        SharedPreferences fixed_values = context.getSharedPreferences("FIXED_VALUES", MODE_PRIVATE);
        SharedPreferences.Editor edit = fixed_values.edit();
        edit.putBoolean("ERROR_FLAG",value);
        edit.commit();
    }

    public static void setLocationToLocal(Context context, android.location.Location location)
    {
        Location wrapper = new Location();
        wrapper.setLongitude(location.getLongitude());
        wrapper.setLatitude(location.getLatitude());

        SharedPreferences locationPref = context.getSharedPreferences("LOCATION", MODE_PRIVATE);
        SharedPreferences.Editor edit = locationPref.edit();
        String locationString = new Gson().toJson(wrapper);
        edit.putString("LOCATION",locationString);
        edit.commit();
    }

    public static android.location.Location getLocationFromLocal(Context context)
    {
        SharedPreferences locationPref = context.getSharedPreferences("LOCATION", MODE_PRIVATE);
        String locationString = locationPref.getString("LOCATION","");
        Location location = new Gson().fromJson(locationString, Location.class);
        android.location.Location returnLoc = new android.location.Location("Fused");
        returnLoc.setLatitude(location.getLatitude());
        returnLoc.setLongitude(location.getLongitude());
        return returnLoc;
    }


    public static boolean isAppInForeground(Context context){
        SharedPreferences isInForeground = context.getSharedPreferences("IS_FOREGROUND", MODE_PRIVATE);
        if(isInForeground.getString("IS_FOREGROUND","1").equals("0")){
            return false;
        }
        return true;
    }
    public static void setAppInForeground(Context context,Boolean value){
        SharedPreferences isInForeground = context.getSharedPreferences("IS_FOREGROUND", MODE_PRIVATE);
        SharedPreferences.Editor edit = isInForeground.edit();
        if(value) {
            edit.putString("IS_FOREGROUND", "1");
            setWasAppInForground(true);
        }
        else
            edit.putString("IS_FOREGROUND","0");
        edit.commit();
    }

    public static void setServiceStatus(Context context,Boolean flag){
        SharedPreferences preferences = context.getSharedPreferences("IS_SERVICE_APPROVED", MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean("IS_SERVICE_APPROVED",flag);
        edit.commit();
    }

    public static Boolean getServiceStatus(Context context){
        SharedPreferences preferences = context.getSharedPreferences("IS_SERVICE_APPROVED", MODE_PRIVATE);
        return preferences.getBoolean("IS_SERVICE_APPROVED", false);
    }


    public static void turnOnFirebaseDatabases(Context context){
            FirebaseDatabase.getInstance().goOnline();
            FirebaseFirestore.getInstance().enableNetwork();
    }

    public static void turnOffFirebaseDatabases(Context context, boolean isAppInForeground){
        if(!isAppInForeground){
            FirebaseDatabase.getInstance().goOffline();
            FirebaseFirestore.getInstance().disableNetwork();
        }
    }

    public static Boolean compareDates(Long leftTs, Long rightTs)
    {
        //true if left<=right

        Date leftTime = new Date();
        leftTime.setTime(leftTs);
        leftTime.setHours(0);
        leftTime.setMinutes(0);
        leftTime.setSeconds(0);

        Date rightTime = new Date();
        rightTime.setTime(rightTs);
        rightTime.setHours(0);
        rightTime.setMinutes(0);
        rightTime.setSeconds(0);

        if(leftTime.getTime()<=rightTime.getTime())
            return true;
        return false;

    }



    public static Long DailyDateConversion(Long currTs, Long otherTs)
    {
        Date currTime = new Date();
        currTime.setTime(currTs);
        Integer today = currTime.getDate();


        Timestamp otherTimestamp = new Timestamp(otherTs);
        Date otherDateOr = new Date(otherTimestamp.getTime());

        Date otherDate = new Date();

        otherDate.setYear(currTime.getYear());
        otherDate.setMonth(currTime.getMonth());
        otherDate.setDate(currTime.getDate());
        otherDate.setHours(otherDateOr.getHours());
        otherDate.setMinutes(otherDateOr.getMinutes());
        otherDate.setSeconds(otherDateOr.getSeconds());
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
        String formattedDate = sdf.format(otherDate);

        FirebaseDatabase.getInstance().getReference("Testing/ts").setValue(formattedDate);
        FirebaseDatabase.getInstance().getReference("Testing/tsOr").setValue(sdf.format(otherDateOr));

        return otherDate.getTime();
    }


}
