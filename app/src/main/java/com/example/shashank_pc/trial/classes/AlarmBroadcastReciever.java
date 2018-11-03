package com.example.shashank_pc.trial.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.shashank_pc.trial.GPS_Service;

public class AlarmBroadcastReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent gpsIntent = new Intent(context, GPS_Service.class);     //Intent to gps service class
        context.startService(gpsIntent);
    }
}
