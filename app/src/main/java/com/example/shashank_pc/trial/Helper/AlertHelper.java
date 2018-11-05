package com.example.shashank_pc.trial.Helper;

import com.example.shashank_pc.trial.classes.Alert;
import com.example.shashank_pc.trial.classes.Lookout;
import com.example.shashank_pc.trial.classes.Task;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import static com.example.shashank_pc.trial.Helper.BasicHelper.DailyDateConversion;

public class AlertHelper {

    public static final long DAY_INTERVAL = 1000 * 60 * 60 * 24;

    public static boolean shouldCheckAlert(Alert alert, Map<String,String> contactMap, String userID)
    {
        Long currTime= System.currentTimeMillis();

        Long lastTime = -1L;
        for(int i=0;i<alert.getSelectedContacts().size();i++)
        {
            if(alert.getSelectedContacts().get(i).equals(userID))
            {
                lastTime = alert.getSelectedContacts().get(i).getTimeStamp();
            }
        }

        //TODO Fill this
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
            }
            else
            {
                if(lastTime!=-1)        //One time lookout
                    return false;
            }


        }
        else if(alert instanceof Task)
        {

        }

        return true;
    }
}
