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
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by shashank-pc on 8/26/2017.
 */

public class SEMapTab extends Fragment implements OnMapReadyCallback {


    private View rootView;

    private MapFragment mMapFrag;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Marker mlocationMarker=null;
    private LatLng curr_loc;



    private String mUserID;
    private String mUserName;
    private String mEntityName;
    private String mEntityID;
    private char mType;


    private FirebaseDatabase database;
    private Marker mContactMarker=null;
    private DatabaseReference contactLatLong;

    private List<mMapContact> mGroupContacts=null;
    private List<Marker> mGroupMarkers=null;
    private HashMap<String,Integer> mGroupMap=null;
    private DatabaseReference groupFlags;

    public void passUserDetails(String userID, String userName, String entityName, String entityID, char type)
    {
        mUserID= userID;
        mUserName=userName;
        mEntityName=entityName;
        mEntityID=entityID;
        mType=type;
    }

    private void mContactInit()
    {
        String contactFirebaseAddress= "Users/"+mEntityID;
        if(contactLatLong==null) {

            contactLatLong = database.getReference(contactFirebaseAddress);
      //      Toast.makeText(getContext(),contactFirebaseAddress,Toast.LENGTH_SHORT).show();
        }

        contactLatLong.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LatLng contactLatLng=null;
                double latitude=10000, longtitude=-10000;
                int i=0;
                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    double temp= snapshot.getValue(Double.class);
                    if(i==0) {
                        latitude = temp;
                    }
                    else if(i==1)
                    {
                        longtitude=temp;
                    }
                    i++;

                }
                contactLatLng= new LatLng(latitude,longtitude);
                if(mMap!=null) {


                    if(mContactMarker!=null)       //Not the first time location is initialized
                    {
                        mContactMarker.setPosition(contactLatLng);

                    }
                    else {                          //First time location is initialized

                        mContactMarker = mMap.addMarker(new MarkerOptions().position(contactLatLng).
                                title(mEntityName).
                                icon(BitmapDescriptorFactory.fromResource(R.drawable.friend_location)));
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private class mMapContact{

        /*
        Class that represents a single contact of a group/event.
         */
        boolean flag;
        DatabaseReference ref;
        String name;
        ValueEventListener valueEventListener;
        boolean VELFlag;

        public mMapContact(boolean flag, DatabaseReference ref, String name, ValueEventListener valueEventListener)
        {
            this.flag=flag;
            this.ref=ref;
            this.name=name;
            this.valueEventListener=valueEventListener;
            VELFlag=true;  //When constructor called, the valueEventListener is added to ref

        }

        public mMapContact(){}
    }

    private void mGroupInit()
    {
        String groupFirebaseAddress = "Groups/"+mEntityID;

        groupFlags = database.getReference(groupFirebaseAddress);

        groupFlags.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //TODO CODE HERE
                if(mGroupMap==null) {
                    /*
                    First time initialize hashmap and arraylist
                     */

                    mGroupContacts = new ArrayList<mMapContact>();
                    mGroupMarkers = new ArrayList<Marker>();
                    mGroupMap = new HashMap<String, Integer>();
                }

                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                {

                    String mGroupContactID=snapshot.getKey();

                    if(mGroupMap.containsKey(mGroupContactID))
                    {
                        /*
                        If UserID already exists in Array
                         */
                        mGroupContacts.get(mGroupMap.get(mGroupContactID)).flag=true;


                    }
                    else if(!mGroupContactID.equals(mUserID))
                    {

                                                /*
                        Attach new UserID to group Hashmap
                         */

                        mGroupMap.put(mGroupContactID,mGroupContacts.size());

                        /*
                        Attach new marker to GroupMarkers
                         */

                        Marker tMarker=null;
                        mGroupMarkers.add(tMarker);

                        /*
                        New Map Contact
                         */
                        final mMapContact mapContact = new mMapContact();


                        mapContact.flag = true;

                        mapContact.name=mGroupContactID;


                        mapContact.ref= database.getReference("Users/"+mapContact.name);

//                        Toast.makeText(getContext(),mapContact.name,Toast.LENGTH_SHORT).show();

                        mapContact.valueEventListener= new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {


                                /*
                                Get Latitude and Longitude
                                 */
                                LatLng contactLatLng=null;
                                double latitude=10000, longtitude=-10000;
                                int i=0;
                                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                                {
                                    double temp= snapshot.getValue(Double.class);
                                    if(i==0) {
                                        latitude = temp;
                                    }
                                    else if(i==1)
                                    {
                                        longtitude=temp;
                                    }
                                    i++;

                                }
                                contactLatLng= new LatLng(latitude,longtitude);
                               /*
                               Set marker
                                */



                                if(mMap!=null) {

                                    if(mGroupMarkers.get(mGroupMap.get(mapContact.name))!=null)       //Not the first time location is initialized
                                    {
                                        mGroupMarkers.get(mGroupMap.get(mapContact.name)).setPosition(contactLatLng);
                                    }
                                    else {

                                        //First time location is initialized
                                        Marker currMarker = mMap.addMarker(new MarkerOptions().position(contactLatLng).
                                                title(mapContact.name).
                                                icon(BitmapDescriptorFactory.fromResource(R.drawable.friend_location)));
                                        //Set new marker to groupMap
                                        mGroupMarkers.set(mGroupMap.get(mapContact.name),currMarker);
                                    }

                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        };

                        mapContact.ref.addValueEventListener(mapContact.valueEventListener);





                        mGroupContacts.add(mapContact);


                    }

                    /*
                    Traverse list
                     */

                    for(mMapContact mapContact: mGroupContacts)
                    {

                        if(mapContact.flag==true)
                        {
                            /*
                            If flag is true, set to false
                             */
                            mapContact.flag=false;

 //                           Toast.makeText(getContext(),mapContact.name,Toast.LENGTH_SHORT).show();

                            if(mapContact.VELFlag==false)
                            {
                                                            /*
                            If value event listener was disabled,enable it
                             */

                                mapContact.ref.addValueEventListener(mapContact.valueEventListener);
                                mapContact.VELFlag=true;
                            }

                        }
                        else if(mapContact.flag==false)
                        {
                            /*
                            If flag is false, remove the value event listener
                             */
                            mapContact.ref.removeEventListener(mapContact.valueEventListener);
                            mapContact.VELFlag=false;
                        }

                    }





                }
            }




            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


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

            database = FirebaseDatabase.getInstance();
 //           Toast.makeText(getContext(),mEntityID,Toast.LENGTH_SHORT).show();

            FragmentManager fragment = getActivity().getFragmentManager();

            mMapFrag = (MapFragment) fragment.findFragmentById(R.id.map);

            mMapFrag.getMapAsync(this);


        }





 //       Log.d("Tag1", "onViewCreated: ");

        //create locationListener
        locationListener= new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                double Latitude= location.getLatitude();    //Get latitude


                double Longitude = location.getLongitude();     //Get longitude


                LatLng latLng= new LatLng(Latitude, Longitude);

                //Map latitude and longitude
                if(mMap!=null) {

                    if(mlocationMarker!=null)       //Not the first time location is initialized
                    {
                        mlocationMarker.setPosition(latLng);

                    }
                    else {                          //First time location is initialized

                        mlocationMarker = mMap.addMarker(new MarkerOptions().position(latLng).
                        title("Me").
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.my_location)));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18));
                    }



                }

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
        };



        //create locationManager
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

        //if location manager is gps provider
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        //else check if network provider is network provider
        else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }


        if(mType=='U')
            mContactInit();
        else if(mType=='G')
            mGroupInit();




    }


    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap=googleMap;


    }
}
