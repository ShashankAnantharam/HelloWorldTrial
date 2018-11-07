package com.example.shashank_pc.trial.Helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.shashank_pc.trial.classes.Alert;
import com.example.shashank_pc.trial.classes.Location;
import com.example.shashank_pc.trial.classes.Lookout;
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

    public static List<Alert> populateAlerts(Map<String,Alert> alertMap)
    {
        List<Alert> alerts = new ArrayList<>();
        for (Map.Entry<String, Alert> entry : alertMap.entrySet()) {
            alerts.add(entry.getValue());
        }
        return alerts;
    }

    public static void setFixLocation(Context context, android.location.Location location)
    {
        SharedPreferences fixed_values = context.getSharedPreferences("FIXED_VALUES", MODE_PRIVATE);
        SharedPreferences.Editor edit = fixed_values.edit();
        String locationString = new Gson().toJson(location);
        edit.putString("FIX_LOCATION",locationString);
    }

    public static android.location.Location getFixLocation(Context context)
    {
        SharedPreferences fixed_values = context.getSharedPreferences("FIXED_VALUES", MODE_PRIVATE);
        return new Gson().fromJson(
                fixed_values.getString("FIX_LOCATION",""),
                android.location.Location.class);
    }

    public static void setFixTime(Context context, Long timestamp)
    {
        SharedPreferences fixed_values = context.getSharedPreferences("FIXED_VALUES", MODE_PRIVATE);
        SharedPreferences.Editor edit = fixed_values.edit();
        edit.putLong("FIX_TIME",timestamp);
    }

    public static Long getFixTime(Context context) {
        SharedPreferences fixed_values = context.getSharedPreferences("FIXED_VALUES", MODE_PRIVATE);
        return fixed_values.getLong("FIX_TIME",System.currentTimeMillis());
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
        SharedPreferences locationPref = context.getSharedPreferences("LOCATION", MODE_PRIVATE);
        SharedPreferences.Editor edit = locationPref.edit();
        String locationString = new Gson().toJson(location);
        edit.putString("LOCATION",locationString);
    }

    public static android.location.Location getLocationFromLocal(Context context)
    {
        SharedPreferences locationPref = context.getSharedPreferences("LOCATION", MODE_PRIVATE);
        String locationString = locationPref.getString("LOCATION","");
        return new Gson().fromJson(locationString, android.location.Location.class);
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
        if(value)
            edit.putString("IS_FOREGROUND","1");
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

    private static boolean getDatabaseConnectionStatus(Context context){
        SharedPreferences preferences = context.getSharedPreferences("IS_FIREBASE_ONLINE", MODE_PRIVATE);
        return preferences.getBoolean("IS_FIREBASE_ONLINE", true);
    }

    public static void setDatabaseConnectionStatus(Context context, boolean flag){
        SharedPreferences sharedPreferences = context.getSharedPreferences("IS_FIREBASE_ONLINE", MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("IS_FIREBASE_ONLINE",flag);
        edit.commit();
    }

    public static void turnOnFirebaseDatabases(Context context){
        if(!getDatabaseConnectionStatus(context)){
            FirebaseDatabase.getInstance().goOnline();
            FirebaseFirestore.getInstance().enableNetwork();
            setDatabaseConnectionStatus(context,true);
        }
    }

    public static void turnOffFirebaseDatabases(Context context, boolean isAppInForeground){
        if(getDatabaseConnectionStatus(context) && !isAppInForeground){
            FirebaseDatabase.getInstance().goOffline();
            FirebaseFirestore.getInstance().disableNetwork();
            setDatabaseConnectionStatus(context,false);
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
