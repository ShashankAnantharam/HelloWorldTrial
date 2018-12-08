package com.example.shashank_pc.trial.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.shashank_pc.trial.GeoLocationService;
import com.example.shashank_pc.trial.Helper.BasicHelper;
import com.example.shashank_pc.trial.Helper.DateTimeHelper;
import com.google.firebase.database.FirebaseDatabase;

public class TimelyBroadcastReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        FirebaseDatabase.getInstance().getReference("Testing/broadcastRecieve/time").setValue(
                DateTimeHelper.getDateTimeString(System.currentTimeMillis())
        );
        BasicHelper.setServiceStatus(context,true);
        Intent gpsIntent = new Intent(context, GeoLocationService.class);     //Intent to gps service class
        context.startService(gpsIntent);
    }
}
