package com.example.shashank_pc.trial.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.shashank_pc.trial.GPS_Service;
import com.example.shashank_pc.trial.GeoLocationService;
import com.example.shashank_pc.trial.Helper.BasicHelper;
import com.example.shashank_pc.trial.Helper.TestHelper;

public class AlarmBroadcastReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if(BasicHelper.getServiceStatus(context)) {
            Intent gpsIntent = new Intent(context, GeoLocationService.class);     //Intent to gps service class
            context.startService(gpsIntent);
        }
    }
}
