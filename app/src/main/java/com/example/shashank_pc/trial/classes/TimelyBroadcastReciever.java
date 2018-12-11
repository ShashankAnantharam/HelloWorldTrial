package com.example.shashank_pc.trial.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.shashank_pc.trial.GeoLocationService;
import com.example.shashank_pc.trial.Helper.AlarmManagerHelper;
import com.example.shashank_pc.trial.Helper.BasicHelper;
import com.example.shashank_pc.trial.Helper.DateTimeHelper;
import com.google.firebase.database.FirebaseDatabase;

public class TimelyBroadcastReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //TODO Removetesting
        FirebaseDatabase.getInstance().getReference("Testing/broadcastRecieve/"+
                Long.toString(System.currentTimeMillis())+"/time/").setValue(
                DateTimeHelper.getDateTimeString(System.currentTimeMillis())
        );

        AlarmManagerHelper.setMorningRepeatingTask(context);

        if(!BasicHelper.getServiceStatus(context)) {
            //Set flag to 1 if service was closed earlier
            SharedPreferences sharedPreferences = context.getSharedPreferences("FLAG", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putInt("FLAG", 1);
            edit.commit();
        }

        BasicHelper.setServiceStatus(context,true);
        Intent gpsIntent = new Intent(context, GeoLocationService.class);     //Intent to gps service class
        context.startService(gpsIntent);
    }
}
