package com.example.shashank_pc.trial;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
    private SEPlacesListItemAdapter arrayAdapter;

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

    public void refresh()
    {
        if(arrayAdapter!=null)
            arrayAdapter.notifyDataSetChanged();
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

        listView = (ListView) rootView.findViewById(R.id.section_list_places);


        //Get the contacts from the database
        if (mPlaces == null)
            mPlaces = new ArrayList<>();


        //Populate listview with contacts

        initArrayAdapter();
        if(placesMap!=null)
            initPlaces();

    }

    private void initArrayAdapter()
    {
        arrayAdapter = new SEPlacesListItemAdapter(getContext(),mPlaces);

        listView.setAdapter(arrayAdapter);

    }

    public void  initPlaces()
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
        refresh();

    }





    //Places Adapter for the class

    private class SEPlacesListItemAdapter extends BaseAdapter {

        Context context;
        List<PlaceWrapperClass> rowItems;

        public SEPlacesListItemAdapter(Context context, List<PlaceWrapperClass> rowItems)
        {
            this.context=context;
            this.rowItems=rowItems;

        }

        @Override
        public int getCount()
        {
            return rowItems.size();
        }

        @Override
        public Object getItem(int position)
        {
            return rowItems.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return rowItems.indexOf(getItem(position));
        }


        private class ViewHolder{
            TextView place_name;
            TextView place_type;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;


            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            holder = new ViewHolder();


            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.single_entity_place_view_item, null);

                holder.place_name = (TextView) convertView.findViewById(R.id.se_place_name);
                holder.place_type = (TextView) convertView.findViewById(R.id.se_place_type);


                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final PlaceWrapperClass rowItem= rowItems.get(position);

            String placeName="", placeType="";

            placeName= rowItem.getPlace().getName();
            placeType= rowItem.getPlace().getType();

            holder.place_name.setText(placeName);
            holder.place_type.setText(placeType);

            return convertView;
        }


    }

}
