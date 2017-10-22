package com.example.shashank_pc.trial;

import android.Manifest;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
    private DatabaseReference memberFlags;
    private ChildEventListener memberFlagsListener;

    private List<Marker> placesMarkers;
    private DocumentReference placesRef;
    private Map<String, Integer> placesMap;
    private GeoQuery placesQuery;


    private Handler memberHandler;
    private Runnable runnable;
    private Map<String,Marker> mMarkersMap;




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
                    if(memberMap.containsKey(memberID))
                    {
                        //Already marker in hashmap and needs to be updated only

                        Double latitude= memberMap.get(memberID).get(0);
                        Double longitude= memberMap.get(memberID).get(1);
                        LatLng memberLatLng= new LatLng(latitude,longitude);
                        entry.getValue().setPosition(memberLatLng);
                        entry.getValue().setVisible(true);
                        memberMap.remove(memberID);

                    }
                    else
                    {
                        //Marker exist in hashmap but not in datasnapshot. i.e. member stopped broadcasting location

                         entry.getValue().setVisible(false);
                    }
             

                }

                try {
                    Iterator<Map.Entry<String, ArrayList<Double>>> it = memberMap.entrySet().iterator();

                    while (it.hasNext()) {
                        //All new members who were not available before


                        Map.Entry<String, ArrayList<Double>> entry = it.next();
                        Marker marker = null;
                        String title = entry.getKey();
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
           //     Toast.makeText(getContext(),"T",Toast.LENGTH_SHORT).show();

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
        if (mMapFrag == null) {
            super.onViewCreated(view, savedInstanceState);

 //           Toast.makeText(getContext(),mEntityID,Toast.LENGTH_SHORT).show();

            FragmentManager fragment = getActivity().getFragmentManager();

            mMapFrag = (MapFragment) fragment.findFragmentById(R.id.map);

            mMapFrag.getMapAsync(this);
            mapFlag=true;


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




        if(mapFlag) {
            //First time map opened. Init members of entity
            if (mType == 'U')
                mContactInit();
            else if (mType == 'G' || mType == 'E') {
                membersInit();
            }

            if(mType=='G')
            {
                placesInit();

            }
        }



    }



    public void placesInit()
    {
        placesMap = new HashMap<>();
        placesMarkers= new ArrayList<>();

        String type="";
        if(mType=='E')
            type="events";
        else if(mType=='G')
            type="groups";

        placesRef = firestore.collection(type).document(mEntityID).collection("places").document("places");

        placesRef.addSnapshotListener(new com.google.firebase.firestore.EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                Map<String,Object> userMap= new HashMap<>();
                userMap = documentSnapshot.getData();

                for(Map.Entry<String,Object> entry : userMap.entrySet())
                {
                    //Iterate over all the places

                    Map<String,String> placeDetails= new HashMap<String, String>();
                    placeDetails = (Map<String,String>) entry.getValue();
                    double lat=0.0, lon=0.0;
                    String name="",type="";

                    for(Map.Entry<String,String> placeAttributeEntry : placeDetails.entrySet())
                    {
                        //Initialize place
                        String value,key;
                        key = placeAttributeEntry.getKey();
                        value=placeAttributeEntry.getValue();

                        // Get attributes of place

                        if(key.equals("lat"))
                        {
                              lat=Double.parseDouble(value);
                        }
                        else if(key.equals("long"))
                        {
                            lon=Double.parseDouble(value);
                        }
                        else if(key.equals("name"))
                        {
                            name=value;
                        }
                        else if(key.equals("type"))
                        {
                            type=value;
                        }

                    }

                    LatLng placeLatLng= new LatLng(lat,lon);

                    int index;
                    if(!placesMap.containsKey(entry.getKey()))
                    {
                        //Place not already present

                        placesMap.put(entry.getKey(),placesMarkers.size());
                        Marker marker=null;
                        placesMarkers.add(marker);
                        index= placesMarkers.size()-1;

                        placesMarkers.set(index,mMap.addMarker(new MarkerOptions().position(placeLatLng).
                                title(name).
                                icon(BitmapDescriptorFactory.fromResource(R.drawable.friend_location))));


                    }
                    else
                    {
                        //Place already present.
                        index = placesMap.get(entry.getKey());
                        placesMarkers.get(index).setPosition(placeLatLng);
                        placesMarkers.get(index).setTitle(name);



                    }
                    if(type.equals("home"))
                    {
                        placesMarkers.get(index).setIcon(
                                BitmapDescriptorFactory.fromResource(R.drawable.place_home)
                        );
                    }
                    else if(type.equals("date"))
                    {
                        placesMarkers.get(index).setIcon(
                                BitmapDescriptorFactory.fromResource(R.drawable.place_dating)
                        );
                    }
                    else if(type.equals("education"))
                    {
                        placesMarkers.get(index).setIcon(
                                BitmapDescriptorFactory.fromResource(R.drawable.place_education)
                        );
                    }

                    //Testing
                    addGeoData(entry.getKey(),lat,lon);

                }

            }
        });
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


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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
            if(memberFlags!=null)
                memberFlags.removeEventListener(memberFlagsListener);
            if(placesRef!=null)
                placesRef=null;
            if(placesMarkers!=null)
                placesMarkers.clear();

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
