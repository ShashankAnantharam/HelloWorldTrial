package com.example.shashank_pc.trial;

import android.Manifest;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
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

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.example.shashank_pc.trial.Generic.database;
import static com.example.shashank_pc.trial.Generic.firestore;
import static com.example.shashank_pc.trial.LandingPageActivity.allContactNames;
import static com.example.shashank_pc.trial.LandingPageActivity.isBroadcastingLocation;
import static com.example.shashank_pc.trial.SingleEntityActivity.Members;
import static com.example.shashank_pc.trial.SingleEntityActivity.isMemberBroadcastingLocation;
import static com.example.shashank_pc.trial.SingleEntityActivity.mMembersTab;
import static com.example.shashank_pc.trial.SingleEntityActivity.membersProfilePic;
import static com.example.shashank_pc.trial.SingleEntityActivity.placesMap;

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
    private Boolean mlocationsetProfilePic;
    private LatLng curr_loc;



    private String mUserID;
    private String mUserName;
    private String mEntityName;
    private String mEntityID;
    private char mType;
    private boolean mapFlag;


    private BroadcastReceiver contactBroadcastReceiver;
    private boolean isLocationAllowed=false;
    private boolean wasLocationAllowed=false;
    private Marker mContactMarker=null;
    private DatabaseReference contactLatLong;
    private ValueEventListener contactLatLongEventListener;

    private List<mMapContact> mMembersList=null;
    private List<Marker> mMarkersList=null;
    private HashMap<String,Integer> mMembersHashMap=null;


    private Map<String,Marker> placesMarkers;
    private GeoQuery placesQuery;


    private Handler memberHandler;
    private Runnable runnable;
    private Map<String,Marker> mMarkersMap;
    private Map<String,Boolean> mMemberSetProfilePicFlag;




    public void passUserDetails(String userID, String userName, String entityName, String entityID, char type)
    {
        mUserID= userID;
        mUserName=userName;
        mEntityName=entityName;
        mEntityID=entityID;
        mType=type;
    }


    private void contactMarkerinit(String contactFirebaseAddress)
    {
        if(contactLatLong==null) {

            contactLatLong = Generic.database.getReference(contactFirebaseAddress);
            //      Toast.makeText(getContext(),contactFirebaseAddress,Toast.LENGTH_SHORT).show();
        }

        contactLatLongEventListener=contactLatLong.addValueEventListener(new ValueEventListener() {
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
 //                       Toast.makeText(getContext(),Double.toString(temp),Toast.LENGTH_SHORT).show();
                    }
                    else if(i==1)
                    {
                        longtitude=temp;
 //                       Toast.makeText(getContext(),Double.toString(temp),Toast.LENGTH_SHORT).show();
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
    private void mContactInit()
    {
        final String contactFirebaseAddress= "Users/"+mEntityID+"/Loc";

        if(isBroadcastingLocation.containsKey(mEntityID) && isBroadcastingLocation.get(mEntityID)==true)
        {
            //Initialize the marker because already the Contact is broadcasting his/her location
            contactMarkerinit(contactFirebaseAddress);
            isLocationAllowed=true;
        }

        if(contactBroadcastReceiver==null)
        {

            contactBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    wasLocationAllowed=isLocationAllowed;
                    isLocationAllowed=intent.getBooleanExtra(mEntityID,false);

                    if(isLocationAllowed)
                    {
                        //Contact is Visible

                        if(mContactMarker==null)
                        {
                            //First time initialize contact marker
                            contactMarkerinit(contactFirebaseAddress);

                        }
                        else if(wasLocationAllowed==false)
                        {
                            //GPS already initialized but location broadcast of contact was turned off

                            contactLatLong.addValueEventListener(contactLatLongEventListener);
                            mContactMarker.setVisible(true);

                        }

                    }
                    else
                    {
                        //Contact is invisible

                        if(wasLocationAllowed==true)
                        {
                            //Location was allowed before but not now
                            contactLatLong.removeEventListener(contactLatLongEventListener);
                            mContactMarker.setVisible(false);
                        }
                    }

                }
            };
        }
        getContext().registerReceiver(contactBroadcastReceiver,new IntentFilter("contact_listener"));


    }

    private class mMapContact{

        /*
        Class that represents a single contact of a group/event.
         */
        boolean flag;
        DatabaseReference ref;
        String name;
        ValueEventListener valueEventListener;

        public mMapContact(boolean flag, DatabaseReference ref, String name, ValueEventListener valueEventListener)
        {
            this.flag=flag;
            this.ref=ref;
            this.name=name;
            this.valueEventListener=valueEventListener;

        }

        public mMapContact(){}
    }


    private void getMembersCoordinates()
    {
        String membersDatabaseAddress= "Loc/"+mEntityID;

        DatabaseReference reference= database.getReference(membersDatabaseAddress);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,ArrayList<Double>> memberMap = (Map) dataSnapshot.getValue();

                for(Map.Entry<String,Marker> entry:mMarkersMap.entrySet())
                {
                    String memberID= entry.getKey();
                    if(memberMap!=null && memberMap.containsKey(memberID))
                    {
                        //Already marker in hashmap and needs to be updated only

                        if(!memberID.equals(mUserID)) {
                            Double latitude = memberMap.get(memberID).get(0);
                            Double longitude = memberMap.get(memberID).get(1);
                            LatLng memberLatLng = new LatLng(latitude, longitude);
                            entry.getValue().setPosition(memberLatLng);
                            entry.getValue().setVisible(true);

                        }

                        isMemberBroadcastingLocation.put(memberID,true);
                        if(mMembersTab!=null)
                           mMembersTab.refresh();

                        if(membersProfilePic!=null &&
                                membersProfilePic.containsKey(memberID) && mMemberSetProfilePicFlag.containsKey(memberID)
                                && !mMemberSetProfilePicFlag.get(memberID))
                        {
                            //First time initialize the memberID marker

                            Bitmap memberProfilePic= membersProfilePic.get(entry.getKey());
                            mMarkersMap.get(entry.getKey()).setAnchor(0.5f,0.5f);
                            mMarkersMap.get(entry.getKey()).setIcon(BitmapDescriptorFactory.fromBitmap(
                                    memberProfilePic));
                        }

                        memberMap.remove(memberID);

                    }
                    else
                    {
                        //Marker exist in hashmap but not in datasnapshot. i.e. member stopped broadcasting location

                        if(!memberID.equals(mUserID))
                             entry.getValue().setVisible(false);
                        isMemberBroadcastingLocation.put(memberID,false);
                        if(mMembersTab!=null)
                            mMembersTab.refresh();
                    }


                }

                try {
                    Iterator<Map.Entry<String, ArrayList<Double>>> it = memberMap.entrySet().iterator();

                    while (it.hasNext()) {
                        //All new members who were not available before


                        Map.Entry<String, ArrayList<Double>> entry = it.next();
                        Marker marker = null;
                        String title = entry.getKey();
                        isMemberBroadcastingLocation.put(title,true);
                        if(mMembersTab!=null)
                            mMembersTab.refresh();

    //                    Toast.makeText(getContext(),"Changed",Toast.LENGTH_SHORT).show();

                        if (!title.equals(mUserID)) {
                            Double latitude = entry.getValue().get(0);
                            Double longitude = entry.getValue().get(1);

                            if (allContactNames.containsKey(title))
                                title = allContactNames.get(title);

                            if (allContactNames != null && allContactNames.containsKey(title))
                                title = allContactNames.get(title);

                            LatLng memberLatLng = new LatLng(latitude, longitude);

                            mMarkersMap.put(entry.getKey(),
                                    mMap.addMarker(new MarkerOptions().position(memberLatLng).
                                            title(title).
                                            icon(BitmapDescriptorFactory.fromResource(R.drawable.friend_location)))
                            );

                            if(membersProfilePic!=null && membersProfilePic.containsKey(entry.getKey()))
                            {
                                Toast.makeText(getContext(),entry.getKey(),Toast.LENGTH_SHORT).show();
                                Bitmap memberProfilePic= membersProfilePic.get(entry.getKey());
                                mMarkersMap.get(entry.getKey()).setAnchor(0.5f,0.5f);
                                mMarkersMap.get(entry.getKey()).setIcon(BitmapDescriptorFactory.fromBitmap(
                                        memberProfilePic));
                                mMemberSetProfilePicFlag.put(entry.getKey(),true);
                            }
                            else
                            {
                                mMemberSetProfilePicFlag.put(entry.getKey(),false);
                            }

                        }
                        else
                        {
                            mMarkersMap.put(mUserID,null);
                        }

                    }
                }
                catch (Exception e)
                {
                    //If no one is broadcasting, an exception is thrown
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private void membersInit()
    {
        mMarkersMap = new HashMap<>();
        memberHandler= new Handler();
        runnable= new Runnable() {
            @Override
            public void run() {
               getMembersCoordinates();
 //               Toast.makeText(getContext(),"T",Toast.LENGTH_SHORT).show();

                memberHandler.postDelayed(this,2500);
            }
        };

        memberHandler.postDelayed(runnable,2500);
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

        mapFlag=false;
        mlocationsetProfilePic=false;
        mMemberSetProfilePicFlag=new HashMap<>();
        placesMarkers= new HashMap<>();



        if (mMapFrag == null) {
            super.onViewCreated(view, savedInstanceState);

 //           Toast.makeText(getContext(),mEntityID,Toast.LENGTH_SHORT).show();

            FragmentManager fragment = getActivity().getFragmentManager();

            mMapFrag = (MapFragment) fragment.findFragmentById(R.id.map);

            mapFlag=true;
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

                    if(membersProfilePic!=null &&
                            membersProfilePic.containsKey(mUserID) && !mlocationsetProfilePic)
                    {
                        Bitmap bitmap= membersProfilePic.get(mUserID);
                        mlocationMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                        mlocationMarker.setAnchor(0.5f,0.5f);
                        mlocationsetProfilePic=true;
                    }



                }

                if(mType=='G')
                {
                    //Testing

                    if(placesQuery==null)
                        getNearbyPlaces(Latitude,Longitude);
                    else
                    {
                        placesQuery.setCenter(new GeoLocation(Latitude,Longitude));
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








    }



    private void placesInit()
    {

        for(Map.Entry<String,Place> placeEntry: placesMap.entrySet())
        {
            addPlace(placeEntry.getKey(),placeEntry.getValue());
        }

    }

    public void addPlace(String key, Place place)
    {
        if(mMap!=null) {
            Double lon, lat;
            lat = Double.parseDouble(place.getLat());
            lon = Double.parseDouble(place.getLon());
            String name = place.getName();
            String type = place.getType();
            LatLng placeLatLng = new LatLng(lat, lon);

            if (placesMarkers.containsKey(key)) {
                //Already Marker is present,

                //Just update the marker
                placesMarkers.get(key).setPosition(placeLatLng);
                placesMarkers.get(key).setTitle(name);


            } else {
                //Adding marker first time

                //Create marker with necessary variables
                placesMarkers.put(key, mMap.addMarker(new MarkerOptions().position(placeLatLng).
                        title(name).
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.friend_location))));
            }

            //Set the icon of marker based on type

            if (type.equals("home")) {
                placesMarkers.get(key).setIcon(
                        BitmapDescriptorFactory.fromResource(R.drawable.place_home)
                );
            } else if (type.equals("date")) {
                placesMarkers.get(key).setIcon(
                        BitmapDescriptorFactory.fromResource(R.drawable.place_dating)
                );
            } else if (type.equals("education")) {
                placesMarkers.get(key).setIcon(
                        BitmapDescriptorFactory.fromResource(R.drawable.place_education)
                );
            }
        }
    }

    public void removePlace(String key)
    {
        if(placesMarkers.containsKey(key))
        {
            placesMarkers.get(key).remove();
            placesMarkers.remove(key);

        }
    }


    private void addGeoData(String key, double lat, double lon)
    {
        /*
        Testing GeoFire by adding data
         */
        String type="";
        if(mType=='E')
            type="Events/";
        else if(mType=='G')
            type="Groups/";

        DatabaseReference placeRef= database.getReference(type+mEntityID+"/places");
        GeoFire geoFire= new GeoFire(placeRef);
        geoFire.setLocation(key,new GeoLocation(lat,lon));

    }

    public void getNearbyPlaces(double Lat, double Long)
    {
        /*
        Function to Test Geoquery
         */
        String type="";
        if(mType=='E')
            type="Events/";
        else if(mType=='G')
            type="Groups/";

        final DatabaseReference placeRef= database.getReference(type+mEntityID+"/places");
        final GeoFire geoFire= new GeoFire(placeRef);

        double R=0.05;
        placesQuery = geoFire.queryAtLocation(new GeoLocation(Lat,Long),R);
        placesQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            double R=1.0;
            int count=0;
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                try {
                    Toast.makeText(getContext(), key+"added", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e)
                {

                }
                count++;
            }

            @Override
            public void onKeyExited(String key) {

                try {
                    Toast.makeText(getContext(), key+"removed", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e)
                {

                }

                count--;

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                placesQuery.removeAllListeners();

        /*        if (count < 2 && R < 25) {
                    R += 1;
                    placesQuery.setRadius(R);
                    try {
                        Toast.makeText(getContext(),Double.toString(R),Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception e)
                    {

                    }
                } else if (count > 20) {

                    R-=1;
                    placesQuery.setRadius(R);

                }
                */
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap=googleMap;



        if(mapFlag) {
            //First time map opened. Init members of entity

            mapFlag=false;
            if (mType == 'U')
                mContactInit();
            else if (mType == 'G' || mType == 'E') {
                membersInit();
            }

            if(mType=='G')
            {
                //If Map is ready, then Initialize places
                placesInit();

            }


        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mlocationsetProfilePic=false;


        if(mType=='C') {
            getContext().unregisterReceiver(contactBroadcastReceiver);
            if(contactLatLong!=null)
                contactLatLong.removeEventListener(contactLatLongEventListener);
        }
        if(mType=='G' || mType=='E') {
            if(mMembersList!=null)
               mMembersList.clear();
            if(mMembersHashMap!=null)
                mMembersHashMap.clear();
            if(mMarkersList!=null)
                mMarkersList.clear();
            if(placesMarkers!=null)
                placesMarkers.clear();

            if(mMemberSetProfilePicFlag!=null)
                mMemberSetProfilePicFlag.clear();

            if(memberHandler!=null) {
                if(runnable!=null) {
                    memberHandler.removeCallbacks(runnable);
                    runnable=null;
                }
                memberHandler=null;
            }

            if(mMarkersMap!=null)
            {
                mMarkersMap.clear();
            }




        }

        if(mlocationMarker!=null)
            mlocationMarker=null;

        if(placesQuery!=null)
        {
            placesQuery.removeAllListeners();
            placesQuery=null;
        }


    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {

            super.setMenuVisibility(menuVisible);
        try {
            if (menuVisible) {
                getActivity().getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.map_background));
            }
        }
        catch (Exception e)
        {

        }

    }
}
