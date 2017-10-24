package com.example.shashank_pc.trial;

import android.view.View;
import android.widget.AdapterView;

import static com.example.shashank_pc.trial.GenericFunctions.secondaryEvents;
import static com.example.shashank_pc.trial.SEMapTab.chosenEvent;
import static com.example.shashank_pc.trial.SingleEntityActivity.secondaryEventsClickFlag;

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

    @Override
    public void setListItemOnClickListener()
    {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Event event=(Event)listView.getItemAtPosition(position);
                String name = event.getName();
                String description= event.getDescription();
                String eventID=event.getID();

                if(!secondaryEventsClickFlag.get(eventID)) {
                    //Change broadcasting secondary event to curr event
                    secondaryEventsClickFlag.put(eventID, true);
                    if(!chosenEvent.equals(""))
                        secondaryEventsClickFlag.put(chosenEvent,false);
                    chosenEvent=eventID;
                }
                else {
                    secondaryEventsClickFlag.put(eventID, false);
                    chosenEvent="";
                }
                refresh();
            }
        });
    }


}
