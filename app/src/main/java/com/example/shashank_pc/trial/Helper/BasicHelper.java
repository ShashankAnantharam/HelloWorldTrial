package com.example.shashank_pc.trial.Helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.shashank_pc.trial.classes.Alert;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
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


}