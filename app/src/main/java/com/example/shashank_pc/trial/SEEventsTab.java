package com.example.shashank_pc.trial;

import static com.example.shashank_pc.trial.GenericFunctions.secondaryEvents;

/**
 * Created by shashank-pc on 10/24/2017.
 */

public class SEEventsTab extends LPEventsTab {

    boolean hasSecondaryEventsInit=false;

    @Override
    public void initEvents()
    {
        if(!hasSecondaryEventsInit) {
            for (Event event : secondaryEvents) {
                addEvent(event);
            }
            hasSecondaryEventsInit=true;
        }
    }


}
