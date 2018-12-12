package com.example.shashank_pc.trial.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.shashank_pc.trial.FacemapLocationService;
import com.example.shashank_pc.trial.Helper.BasicHelper;

public class AlarmBroadcastReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if(BasicHelper.getServiceStatus(context)) {
            Intent gpsIntent = new Intent(context, FacemapLocationService.class);     //Intent to gps service class
            context.startService(gpsIntent);

         //   Intent activityTypeIntent = new Intent(context, BackgroundDetectedActivitiesService.class);
         //   context.startService(activityTypeIntent);
        }
    }
}
