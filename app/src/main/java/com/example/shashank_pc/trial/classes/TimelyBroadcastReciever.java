package com.example.shashank_pc.trial.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.FileObserver;

import com.example.shashank_pc.trial.FacemapLocationService;
import com.example.shashank_pc.trial.Helper.AlarmManagerHelper;
import com.example.shashank_pc.trial.Helper.BasicHelper;
import com.example.shashank_pc.trial.Helper.DateTimeHelper;
import com.google.firebase.database.FirebaseDatabase;

public class TimelyBroadcastReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        AlarmManagerHelper.setMorningRepeatingTask(context);

        FirebaseDatabase.getInstance().getReference("Testing/timelyReceiver/"+Long.toString(System.currentTimeMillis())).setValue("");

        //TODO Add this
        if(BasicHelper.getServiceStatus(context)) {
            Intent gpsIntent = new Intent(context, FacemapLocationService.class);     //Intent to gps service class
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(gpsIntent);
            } else {
                context.startService(gpsIntent);
            }
        }

    }
}
