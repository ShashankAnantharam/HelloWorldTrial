package com.example.shashank_pc.trial;

import android.app.FragmentManager;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

/**
 * Created by shashank-pc on 8/26/2017.
 */

public class SEMapTab extends Fragment implements OnMapReadyCallback{



    private View rootView;
    MapFragment mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView!=null){
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
                parent.removeView(rootView);
        }
        try
        {
            rootView = inflater.inflate(R.layout.single_entity_map, container, false);
        }
        catch (InflateException e)
        {

        }


        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        if(mMap==null) {
            super.onViewCreated(view, savedInstanceState);
            FragmentManager fragment = getActivity().getFragmentManager();

            mMap = (MapFragment) fragment.findFragmentById(R.id.map);


            mMap.getMapAsync(this);
        }



    }


    @Override
    public void onMapReady(GoogleMap googleMap)
    {

        GoogleMap mMap = googleMap;

        LatLng home= new LatLng(17.442139, 78.504638);

        mMap.addCircle(new CircleOptions().center(home).fillColor(Color.BLUE).radius(10));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home,18));

    }
}
