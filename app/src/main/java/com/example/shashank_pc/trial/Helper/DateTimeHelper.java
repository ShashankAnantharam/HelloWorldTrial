package com.example.shashank_pc.trial.Helper;

import android.content.Context;
import android.widget.Toast;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;

public class DateTimeHelper {

    public static boolean shouldCheckFlagFor6AM = false;

    public static String getDateTimeString(Long date)
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(date));
    }

    public static boolean shouldStartServiceBasedOnTime(Context context, Long currTime){

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,10);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);

        Long morningTime = calendar.getTime().getTime();

   //     Toast.makeText(context,"curr Time:"+ getDateTimeString(currTime) +" FixedTime: " + getDateTimeString(morningTime)
   //     , Toast.LENGTH_SHORT).show();


        // FirebaseDatabase.getInstance().getReference("Testing/firedBroadcastList/currT").setValue(getDateTimeString(currTime));
        // FirebaseDatabase.getInstance().getReference("Testing/firedBroadcastList/fixT").setValue(getDateTimeString(morningTime));
        // FirebaseDatabase.getInstance().getReference("Testing/firedBroadcastList/flag").setValue(shouldCheckFlagFor6AM);

        if(currTime >= morningTime)
        {
            if(shouldCheckFlagFor6AM)
            {
                shouldCheckFlagFor6AM = false;
                BasicHelper.setServiceStatus(context, true);
                return true;
            }
        }
        else{
            shouldCheckFlagFor6AM = true;
        }

        return false;
    }


}
