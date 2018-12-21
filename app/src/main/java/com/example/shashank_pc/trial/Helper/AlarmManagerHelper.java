package com.example.shashank_pc.trial.Helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.shashank_pc.trial.FacemapLocationService;
import com.example.shashank_pc.trial.classes.AlarmBroadcastReciever;
import com.example.shashank_pc.trial.classes.TimelyBroadcastReciever;
import com.example.shashank_pc.trial.userStatusClasses.Constants;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Date;
import java.util.Calendar;

public class AlarmManagerHelper {

    public static long QUARTER_HOUR_MS = 15L*60L*1000L;


    private static void setNormalAlarmManager(Context context)
    {
        Calendar cur_cal = Calendar.getInstance();
        cur_cal.setTimeInMillis(System.currentTimeMillis());
        cur_cal.add(Calendar.SECOND, 50);

        Intent broadcastIntent = new Intent(context, AlarmBroadcastReciever.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, broadcastIntent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis(), 1 * 1000, pendingIntent);

    }


    private static void setMorningTriggerTime(Context context, Long value)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences("TIME", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putLong("MORNING_TRIGGER_TIME", value);
        edit.commit();

    }

    private static long getMorningTriggerTime(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences("TIME", Context.MODE_PRIVATE);
        return sharedPreferences.getLong("MORNING_TRIGGER_TIME",-1L);
    }

    private static boolean shouldTriggerMorningTimer(Context context, Long path)
    {
        Long currTime = System.currentTimeMillis();
        Long triggerTime = getMorningTriggerTime(context);

        FirebaseDatabase.getInstance().getReference("Testing/broadcastRecieve/"+
                Long.toString(path)+"/currTimeMinusTriggerTime/").setValue(
                Long.toString(currTime-triggerTime));



        /*
        If time has not been set OR time is not correct for trigger, return false
         */
        if(triggerTime==-1L || (currTime<triggerTime))
            return false;
        return true;
    }

    public static void setMorningRepeatingTask(Context context) {

        Long path = System.currentTimeMillis();

        boolean shouldTriggerNow = shouldTriggerMorningTimer(context,path);

        //TODO remove testing
        FirebaseDatabase.getInstance().getReference("Testing/broadcastRecieve/"+
                Long.toString(path)+"/shouldTriggerNow/").setValue(
                Boolean.toString(shouldTriggerNow));

        FirebaseDatabase.getInstance().getReference("Testing/broadcastRecieve/"+
                Long.toString(path)+"/currTime/").setValue(
                DateTimeHelper.getDateTimeString(path));

        //Starting based on time
        Intent timeBroadcastIntent = new Intent(context, TimelyBroadcastReciever.class);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 6); // For 6.30 AM
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        if (checkIfTheTimeHasPassed(calendar.getTimeInMillis()-QUARTER_HOUR_MS)) {
            calendar.add(Calendar.DATE, 1);
        }

        //Set time to trigger in local storage
        setMorningTriggerTime(context,calendar.getTimeInMillis());

        //TODO RemoveTesting
        FirebaseDatabase.getInstance().getReference("Testing/broadcastRecieve/"+
                Long.toString(path)+"/alarmset/").setValue(
                DateTimeHelper.getDateTimeString(calendar.getTimeInMillis())
        );

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, timeBroadcastIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);


        if(shouldTriggerNow) {
            if (!BasicHelper.getServiceStatus(context)) {
                //Set flag to 1 if service was closed earlier
                SharedPreferences sharedPreferences = context.getSharedPreferences("FLAG", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putInt("FLAG", 1);
                edit.commit();
            }

            BasicHelper.setServiceStatus(context, true);
            setNormalAlarmManager(context);
            Intent gpsIntent = new Intent(context, FacemapLocationService.class);     //Intent to gps service class
            context.startService(gpsIntent);
            AlertHelper.alertUserOnFacemapStatus(context,1);
        }
    }

    public static boolean checkIfTheTimeHasPassed(long timeInMillis) {
        long nowTime = System.currentTimeMillis();
        return nowTime > timeInMillis;
    }
}
