package com.example.shashank_pc.trial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static android.R.attr.x;

/**
 * Created by shashank-pc on 8/22/2017.
 */

public class LPEventsTab extends Fragment {


    //List is used because it is faster than Vector because it is asynchronous
    List<Event> mEvents;

    private View rootView;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        /*
        Inflate the Event Tab View and return it.

         */


        if(rootView!=null){
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
                parent.removeView(rootView);
        }
        try {

            rootView = inflater.inflate(R.layout.tab_events, container, false);
        }
        catch (InflateException e)
        {

        }




        return rootView;
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState)
    {

        /*
        Function that executes after view is created
         */


        super.onViewCreated(rootView, savedInstanceState);

        //Initialize Listview
        final ListView listView;
        listView= (ListView) rootView.findViewById(R.id.section_list_event);


        //Get the Events from the database
        if(mEvents==null)
            mEvents=getAllEvents();

        //Populate listview with Events
        LPListItemAdapter<Event> arrayAdapter= new LPListItemAdapter<Event>(getContext(),
                mEvents);
        listView.setAdapter(arrayAdapter);

        //Set intent to enable switching to new activity
        final Intent listClickActivity= new Intent();
        listClickActivity.setClass(getContext(),SingleEntityActivity.class);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Function that executes when listView item is clicked

                Toast.makeText(getContext(),"Clicked on Event",Toast.LENGTH_SHORT).show();
                Event event=(Event)listView.getItemAtPosition(position);
                String name = event.getName();
                String description= event.getDescription();
                boolean isGPSBroadcast= event.getBroadcastLocationFlag();

                //Pass Name, Description, type(Event) and IsGPSBroadcast flag to next activity
                listClickActivity.putExtra("Name",name);
                listClickActivity.putExtra("Description",description);
                listClickActivity.putExtra("Type",'E');
                listClickActivity.putExtra("IsGPSBroadcast",isGPSBroadcast);

                //Start new activity
                startActivity(listClickActivity);

            }
        });



    }

    public List<Event> getAllEvents()
    {
        //TODO Change function in order to get Events from Database
        /*
        Function to get All events from the Database. HardCoded now
         */

        List <Event> mAllEvents = new ArrayList<>();
        for(int i=0;i<27;i++)
        {
            Event temp= new Event("Event"+Integer.toString(i),"Event_desc");
            mAllEvents.add(temp);
        }
        return mAllEvents;
    }
}
