package com.example.shashank_pc.trial.Helper;

import com.example.shashank_pc.trial.classes.Alert;
import com.example.shashank_pc.trial.classes.Lookout;
import com.example.shashank_pc.trial.classes.Task;

import java.util.Map;

public class AlertHelper {

    public static boolean shouldCheckAlert(Alert alert, Map<String,String> contactMap, String userID)
    {
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
