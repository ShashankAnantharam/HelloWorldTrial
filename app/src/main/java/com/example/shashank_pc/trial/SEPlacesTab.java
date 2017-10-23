package com.example.shashank_pc.trial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.shashank_pc.trial.SingleEntityActivity.placesMap;

/**
 * Created by shashank-pc on 10/23/2017.
 */

public class SEPlacesTab extends Fragment {

    private View rootView;
    private List<PlaceWrapperClass> mPlaces;
    private ListView listView;
    private ArrayAdapter arrayAdapter;

    private class PlaceWrapperClass
    {
        /*
        Wrapper class to store the Place as well as Place ID so that it is easy to perform operations via Listview
         */

        String PlaceID;
        Place place;

        public String getPlaceID() {
            return PlaceID;
        }

        public void setPlaceID(String placeID) {
            PlaceID = placeID;
        }

        public Place getPlace() {
            return place;
        }

        public void setPlace(Place place) {
            this.place = place;
        }

        public PlaceWrapperClass(String placeID, Place place) {
            PlaceID = placeID;
            this.place = place;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*
        Inflate the Contact Tab View and return it.

         */
        if(rootView!=null){
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
                parent.removeView(rootView);
        }
        try {

            rootView = inflater.inflate(R.layout.single_entity_places, container, false);
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

        listView = (ListView) rootView.findViewById(R.id.section_list_contact);


        //Get the contacts from the database
        if (mPlaces == null)
            mPlaces = new ArrayList<>();


        //Populate listview with contacts

        initArrayAdapter();
    }

    private void initArrayAdapter()
    {
        for(Map.Entry<String,Place> placeEntry: placesMap.entrySet())
        {
            addPlace(placeEntry.getKey(),placeEntry.getValue());
        }
    }

    public void addPlace(String placeID, Place place)
    {
        PlaceWrapperClass placeWrapperClass = new PlaceWrapperClass(placeID,place);

        mPlaces.add(placeWrapperClass);

    }

}
