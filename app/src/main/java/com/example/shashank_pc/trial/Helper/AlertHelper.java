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

    public static boolean shouldCheckAlert(Alert alert, Map<String,String> contactMap, String userID)
    {
        Long currTime= System.currentTimeMillis();
        DailyDateConversion(currTime,((Lookout) alert).getFromTime());

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



        }
        else if(alert instanceof Task)
        {

        }

        return true;
    }
}
