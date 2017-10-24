package com.example.shashank_pc.trial;

import static com.example.shashank_pc.trial.GenericFunctions.secondaryEvents;

/**
 * Created by shashank-pc on 10/24/2017.
 */

public class SEEventsTab extends LPEventsTab {

    boolean hasSecondaryEventsInit=false;

    private SEEventListArrayAdapter<Event> arrayAdapter;

    @Override
    public void refresh()
    {
        if(arrayAdapter!=null) {
            arrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void initArrayAdapter()
    {
        arrayAdapter = new SEEventListArrayAdapter<>(getContext(),
                mEvents, mUserID);
        listView.setAdapter(arrayAdapter);
    }

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
