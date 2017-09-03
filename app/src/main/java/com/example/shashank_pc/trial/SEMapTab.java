package com.example.shashank_pc.trial;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
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

public class SEMapTab extends Fragment implements OnMapReadyCallback {


    private View rootView;

    private MapFragment mMapFrag;

    private GoogleMap mMap;

    private LocationManager locationManager;


    private LatLng curr_loc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
                parent.removeView(rootView);
        }
        try {
            rootView = inflater.inflate(R.layout.single_entity_map, container, false);
        } catch (InflateException e) {

        }


        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (mMapFrag == null) {
            super.onViewCreated(view, savedInstanceState);
            FragmentManager fragment = getActivity().getFragmentManager();

            mMapFrag = (MapFragment) fragment.findFragmentById(R.id.map);


            mMapFrag.getMapAsync(this);


        }

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //check if network provider is network provider
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    double Latitude= location.getLatitude();    //Get latitude

                    double Longitude = location.getLongitude();     //Get longitude

                    LatLng latLng= new LatLng(Latitude, Longitude);

                    //Map latitude and longitude
                    mMap.addCircle(new CircleOptions().center(latLng).fillColor(Color.BLUE).radius(10));

                    //Zoom in to current location
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18));


                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });

        }
        //else if location manager is gps provider
        else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    double Latitude= location.getLatitude();    //Get latitude

                    double Longitude = location.getLongitude();     //Get longitude

                    LatLng latLng= new LatLng(Latitude, Longitude);

                    //Map latitude and longitude
                    mMap.addCircle(new CircleOptions().center(latLng).fillColor(Color.BLUE).radius(10));

                    //Zoom in to current location
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18));



                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
        else
        {
            LatLng latLng= new LatLng(0,0);

            //Map latitude and longitude
            mMap.addCircle(new CircleOptions().center(latLng).fillColor(Color.BLUE).radius(10));

            //Zoom in to current location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18));

        }



    }


    @Override
    public void onMapReady(GoogleMap googleMap)
    {

        GoogleMap mGMap = googleMap;


/*
        mMap.addCircle(new CircleOptions().center(curr_loc).fillColor(Color.BLUE).radius(10));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curr_loc,18));
*/

    }
}
