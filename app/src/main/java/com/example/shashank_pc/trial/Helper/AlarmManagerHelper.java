package com.example.shashank_pc.trial.Helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.example.shashank_pc.trial.FacemapLocationService;
//import com.example.shashank_pc.trial.classes.AlarmBroadcastReciever;
import com.example.shashank_pc.trial.classes.Alert;
import com.example.shashank_pc.trial.classes.TimelyBroadcastReciever;
import com.example.shashank_pc.trial.userStatusClasses.Constants;

import java.sql.Date;
import java.util.Calendar;

public class AlarmManagerHelper {

    public static long QUARTER_HOUR_MS = 15L*60L*1000L;

    //TODO Comment this 25Jan
 /*   private static void setNormalAlarmManager(Context context)
    {
        Calendar cur_cal = Calendar.getInstance();
        cur_cal.setTimeInMillis(System.currentTimeMillis());
        cur_cal.add(Calendar.SECOND, 50);

        Intent broadcastIntent = new Intent(context, AlarmBroadcastReciever.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, broadcastIntent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis(), 1 * 1000, pendingIntent);

    }
*/

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

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, timeBroadcastIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);


        if(shouldTriggerNow) {
            if(BasicHelper.getCompleteServiceStatus(context)) {
                if (!BasicHelper.getServiceStatus(context)) {

                    /*
                    If service can turn on for the day
                     */

                    //Set flag to 1 if service was closed earlier
                    SharedPreferences sharedPreferences = context.getSharedPreferences("FLAG", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putInt("FLAG", 1);
                    edit.commit();
                }

                BasicHelper.setServiceStatus(context, true);

                //TODO Comment this 25Jan
                //setNormalAlarmManager(context);

                Intent gpsIntent = new Intent(context, FacemapLocationService.class);     //Intent to gps service class
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(gpsIntent);
                } else {
                    context.startService(gpsIntent);
                }
                AlertHelper.alertUserOnFacemapStatus(context, 1);
            }
            else
            {
                /*
                Service cannot turn on for the day: Show notification instead.
                 */
                AlertHelper.alertUserOnFacemapStatus(context,-2);
            }
        }

    }

    public static boolean checkIfTheTimeHasPassed(long timeInMillis) {
        long nowTime = System.currentTimeMillis();
        return nowTime > timeInMillis;
    }
}
