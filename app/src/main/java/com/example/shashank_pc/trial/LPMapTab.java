package com.example.shashank_pc.trial;

import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import static com.example.shashank_pc.trial.Generic.database;
import static com.example.shashank_pc.trial.LandingPageActivity.allContactNames;
import static com.example.shashank_pc.trial.LandingPageActivity.getUserID;
import static com.example.shashank_pc.trial.LandingPageActivity.isBroadcastingLocation;
import static com.example.shashank_pc.trial.LandingPageActivity.unknownUser;
import static com.example.shashank_pc.trial.LandingPageActivity.userProfilePics;

/**
 * Created by shashank-pc on 11/5/2017.
 */

public class LPMapTab extends Fragment implements OnMapReadyCallback {


    private View rootView;

    private MapFragment mMapFrag;
    private GoogleMap mMap;

    private boolean mapFlag;
    private Map<String,Marker> allContactsMarkersMap;

    private Runnable commonMapRunnable;
    private Handler commanMapHandler;

    private boolean zoomFlag=false;
    private Marker userMarker;
    boolean mlocationsetProfilePic=false;
    SharedPreferences entityVisibleflag;

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

    private boolean isMapZoomed()
    {
        return zoomFlag;
    }

    public void setUserMarker(double latitude, double longitude)
    {
        if(mMap!=null) {
            if (userMarker == null) {
                userMarker = mMap.addMarker(new MarkerOptions().position(
                        new LatLng(latitude, longitude)).
                        title("Me").
                        icon(BitmapDescriptorFactory.fromBitmap(unknownUser)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(latitude, longitude)
                        , 18));

            } else {
                userMarker.setPosition(new LatLng(latitude, longitude));
            }


            if (userProfilePics != null &&
                    userProfilePics.containsKey(getUserID()) && !mlocationsetProfilePic) {
                Bitmap bitmap = userProfilePics.get(getUserID());
                userMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                userMarker.setAnchor(0.5f, 0.5f);
                mlocationsetProfilePic = true;
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        mapFlag=false;




        if (mMapFrag == null) {
            super.onViewCreated(view, savedInstanceState);

            //           Toast.makeText(getContext(),mEntityID,Toast.LENGTH_SHORT).show();

            FragmentManager fragment = getActivity().getFragmentManager();

            mMapFrag = (MapFragment) fragment.findFragmentById(R.id.map);

            mapFlag=true;
            mMapFrag.getMapAsync(this);



        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        allContactsMarkersMap= new HashMap<>();
        initCommonMapHandler();
    }

    private void initCommonMapHandler()
    {
        commanMapHandler= new Handler();
        entityVisibleflag = getContext().getSharedPreferences("DisplayFlags", Context.MODE_PRIVATE);
        commonMapRunnable= new Runnable() {
            @Override
            public void run() {
                getContacts();

                commanMapHandler.postDelayed(this,2500);
            }
        };

        commanMapHandler.postDelayed(commonMapRunnable,2500);
    }

    private void getContacts()
    {

        for(final Map.Entry<String,Boolean> entry: isBroadcastingLocation.entrySet())
        {

            if(entry.getValue() && entityVisibleflag.getBoolean(entry.getKey(),false))
            {
                DatabaseReference temp = database.getReference("Users/"+entry.getKey()+"/Loc");
                temp.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        double lat= (Double) dataSnapshot.child("Lat").getValue();
                        double lon= (Double) dataSnapshot.child("Long").getValue();

                        if(allContactsMarkersMap.containsKey(entry.getKey()))
                        {
                            //Marker already present. Just update location
                            allContactsMarkersMap.get(entry.getKey()).setPosition(
                                    new LatLng(lat,lon)
                            );
                            allContactsMarkersMap.get(entry.getKey()).setVisible(true);
                        }
                        else
                        {
                            //Marker not present. Need to create it
                            String title=entry.getKey();
                            if(allContactNames.containsKey(title))
                                title=allContactNames.get(title);
                            allContactsMarkersMap.put(entry.getKey(),
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lon)).
                                            title(title).
                                            icon(BitmapDescriptorFactory.fromBitmap(unknownUser)))
                            );
                            allContactsMarkersMap.get(entry.getKey()).setAnchor(0.5f,0.5f);

                        }

                        //Get profile picture to marker
                        if(userProfilePics.containsKey(entry.getKey()))
                        {
                            allContactsMarkersMap.get(entry.getKey()).setIcon(
                                    BitmapDescriptorFactory.fromBitmap(userProfilePics.get(
                                            entry.getKey()
                                    ))
                            );
                            allContactsMarkersMap.get(entry.getKey()).setAnchor(0.5f,0.5f);
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
            else
            {
                //Marker needs to be invisible
                if(allContactsMarkersMap.containsKey(entry.getKey()))
                {
                    allContactsMarkersMap.get(entry.getKey()).setVisible(false);
                }
            }





        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for(Map.Entry<String,Marker> entry: allContactsMarkersMap.entrySet())
            entry.getValue().remove();

        allContactsMarkersMap.clear();
    }
}
