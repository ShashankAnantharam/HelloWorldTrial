package com.example.shashank_pc.trial.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.shashank_pc.trial.FacemapLocationService;
import com.example.shashank_pc.trial.Helper.AlarmManagerHelper;
import com.example.shashank_pc.trial.Helper.BasicHelper;
import com.example.shashank_pc.trial.Helper.DateTimeHelper;

public class TimelyBroadcastReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        AlarmManagerHelper.setMorningRepeatingTask(context);


    }
}
