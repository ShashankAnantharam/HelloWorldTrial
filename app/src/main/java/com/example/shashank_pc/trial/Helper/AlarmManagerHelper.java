package com.example.shashank_pc.trial.Helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.shashank_pc.trial.classes.TimelyBroadcastReciever;
import com.example.shashank_pc.trial.userStatusClasses.Constants;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Date;
import java.util.Calendar;

public class AlarmManagerHelper {

    public static long QUARTER_HOUR_MS = 15L*60L*1000L;

    public static void setMorningRepeatingTask(Context context) {



        //Starting based on time
        Intent timeBroadcastIntent = new Intent(context, TimelyBroadcastReciever.class);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 6); // For 6.30 AM
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        if (checkIfTheTimeHasPassed(calendar.getTimeInMillis()-QUARTER_HOUR_MS)) {
            calendar.add(Calendar.DATE, 1);
        }

        //TODO RemoveTesting
        FirebaseDatabase.getInstance().getReference("Testing/broadcastRecieve/"+
                Long.toString(System.currentTimeMillis())+"/alarmset/").setValue(
                DateTimeHelper.getDateTimeString(calendar.getTimeInMillis())
        );

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, timeBroadcastIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
    }

    public static boolean checkIfTheTimeHasPassed(long timeInMillis) {
        long nowTime = System.currentTimeMillis();
        return nowTime > timeInMillis;
    }
}
