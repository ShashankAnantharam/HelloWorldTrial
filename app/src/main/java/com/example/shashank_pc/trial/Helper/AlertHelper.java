package com.example.shashank_pc.trial.Helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.example.shashank_pc.trial.LandingPageActivity;
import com.example.shashank_pc.trial.R;
import com.example.shashank_pc.trial.classes.Alert;
import com.example.shashank_pc.trial.classes.Lookout;
import com.example.shashank_pc.trial.classes.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import static com.example.shashank_pc.trial.Helper.BasicHelper.DailyDateConversion;

public class AlertHelper {

    public static final long DAY_INTERVAL = 1000 * 60 * 60 * 24;
    public static final long TASK_REMINDER_INTERVAL = 1000 * 60 * 60;


    public static void alertTriggerNotification(Alert alert, Context context)
    {

        String title=alert.getName();
        String body = "";
        String sound = "";
        if(alert instanceof Lookout)
        {
            body = "You have reached lookout location.";
            sound="android.resource://" + context.getPackageName() + "/" + R.raw.andr_lookout_location;
        }
        else if(alert instanceof Task)
        {
            body = "You have a task nearby.";
            sound="android.resource://" + context.getPackageName() + "/" + R.raw.andr_task_location;
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setSound(Uri.parse(sound))
                        .setPriority(Notification.PRIORITY_HIGH);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int id = (int) System.currentTimeMillis();
        notificationManager.notify(id, mBuilder.build());
    }

    public static void triggerAlert(Alert alert, Context context) {
        Long currTime = System.currentTimeMillis();
        alertTriggerNotification(alert, context);
        if (alert instanceof Lookout) {

        } else if (alert instanceof Task){

        }
    }

    public static boolean checkTaskDeadline(Long deadline, Long currTime, Boolean isDaily)
    {
        if(isDaily)
        {
            Long todaysDeadline = DailyDateConversion(currTime,deadline);
            if(todaysDeadline< currTime)
                return false;
        }
        else
        {
            if(deadline<currTime)
                return false;
        }

        return true;
    }

    public static boolean shouldCheckAlert(Alert alert, Map<String,String> contactMap, String userID)
    {
        //TODO Cross check once

        Long currTime= System.currentTimeMillis();
        Long lastTime = -1L;

        for(int i=0;i<alert.getSelectedContacts().size();i++)
        {
            if(alert.getSelectedContacts().get(i).getId().equals(userID))
            {
                lastTime = alert.getSelectedContacts().get(i).getTimeStamp();
            }
        }
        FirebaseDatabase.getInstance().getReference("Debug/lastTime/"+alert.getId()).setValue(lastTime);

        if(alert instanceof Lookout)
        {
            if((!((Lookout) alert).isEnabled())         //Lookout is not enabled
                    ||
                    ((!((Lookout) alert).getCreatedBy().equals(userID)) //Lookout is not created by user himself
                            &&
                            (!contactMap.containsKey(((Lookout) alert).getCreatedBy()) ||
                                            !contactMap.get(((Lookout) alert).getCreatedBy()).equals("Y"))
                            /*
                            Lookout is either not created by a contact OR contact is not being broadcasted to
                             */
                    )
                    )
            {
                return false;
            }

            if(alert.isDaily())
            {
                Long realFromTime = DailyDateConversion(currTime,((Lookout) alert).getFromTime());
                Long realToTime = DailyDateConversion(currTime,((Lookout) alert).getToTime());

                if(realToTime < realFromTime)
                {
                    realToTime = realToTime + DAY_INTERVAL;
                }

                if(realFromTime>currTime || realToTime<currTime)    //If alert is not within given time range
                    return false;

                if(lastTime>=realFromTime)       //If alert already rang for given time range
                    return false;

                if(BasicHelper.compareDates(currTime, lastTime ))
                {
                    //If currDate <= completionDate (If task got completed today)
                    return false;
                }
            }
            else
            {
                if(lastTime!=-1L)        //One time lookout
                    return false;
            }
        }
        else if(alert instanceof Task)
        {
            if(!alert.isDaily() && ((Task) alert).getCompletedAt()> -1L)
                return false;       //Task already completed
            else if(((Task) alert).isHasDeadline() &&
                    !checkTaskDeadline(((Task) alert).getDeadline(), currTime, alert.isDaily())){
                //If task is not within deadline
                return false;
            }
            else{
                if(alert.isDaily())
                {
                    if(((Task) alert).getCompletedAt()>-1L)
                    {
                        //Task is daily and has been completed previously.
                        if(BasicHelper.compareDates(currTime, ((Task) alert).getCompletedAt() ))
                        {
                            //If currDate <= completionDate (If task got completed today)
                            return false;
                        }
                    }
                }
            }
            if(lastTime + TASK_REMINDER_INTERVAL < currTime)
            {
                //It is less than one hour since last reminder was sent
                return false;
            }
        }

        return true;
    }
}
