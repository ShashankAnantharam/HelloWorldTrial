package com.example.shashank_pc.trial.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.shashank_pc.trial.FacemapLocationService;
import com.example.shashank_pc.trial.Helper.BasicHelper;
import com.google.firebase.database.FirebaseDatabase;

//TODO Remove this 25 Jan
public class AlarmBroadcastReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        FirebaseDatabase.getInstance().getReference("Testing/alarmReceiver/"+Long.toString(System.currentTimeMillis())).setValue("");
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
