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
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.BooleanResult;
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
import static com.example.shashank_pc.trial.GenericFunctions.secondaryEvents;
import static com.example.shashank_pc.trial.LandingPageActivity.allContactNames;
import static com.example.shashank_pc.trial.LandingPageActivity.isBroadcastingLocation;
import static com.example.shashank_pc.trial.LandingPageActivity.unknownUser;
import static com.example.shashank_pc.trial.LandingPageActivity.userProfilePics;
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

    //These varaibles are related to the user's marker
    private MapFragment mMapFrag;       //Mapfragment to hold Map
    private GoogleMap mMap;             //GoogleMap to contain the Map class and do modifications on Map
    private LocationManager locationManager;        //Location Manager to get user's current location (not needed as stated earlier)
    private LocationListener locationListener;          //Location listener to listen to user's current location
    private Marker mlocationMarker=null;        //user's location marker
    private Boolean mlocationsetProfilePic;     //Flag to know whether profile pic of user has been set
    private LatLng curr_loc;



    //These variables are related to the entity
    private String mUserID;             //String UserID
    private String mUserName;
    private String mEntityName;         //Entity name (Group/Event/Contact name)
    private String mEntityID;           //Entity ID     (Group/Event/Contact ID)
    private char mType;                 //type of entity (Group/Event/Contact)
    private boolean mapFlag;            //flag to know if map has been initialized or not


    //These variables are related to contact
    private BroadcastReceiver contactBroadcastReceiver;     //Broadcast receiver to recieve whether contact
                                                            //is broadcasting location or not
    private boolean isLocationAllowed=false;                //Flag for whether contact is broadcasting location or not
    private boolean wasLocationAllowed=false;               //Flag for whether contacct WAS broadcasting location or not
    private Marker mContactMarker=null;                 //Contact marker
    private DatabaseReference contactLatLong;               //Reference to Database containing Contact's location
    private ValueEventListener contactLatLongEventListener;         //Value event listener to listen to contact's location

    private List<mMapContact> mMembersList=null;
    private List<Marker> mMarkersList=null;
    private HashMap<String,Integer> mMembersHashMap=null;


    //Variables related to places (Not needed at this point)
    private Map<String,Marker> placesMarkers;
    private GeoQuery placesQuery;


    //Variables related to group members
    private Handler memberHandler;      //A handler to listen to member's locations every 2.5 seconds
    private Runnable runnable;          //A runnable that runs within the handler and listens to member's locations every 2.5 seconds
    private Map<String,Marker> mMarkersMap;     //A hashmap containing the group member's ID mapped with the marker
    private Map<String,Boolean> mMemberSetProfilePicFlag;       //A map to know whether the profile pic of group member has been set.

    //These variables are related to Events (Not explained at this point)
    private GeoQuery eventmembersQuery;
    Double radius=0.2;
    private boolean locationAvailableFlag=false;
    private Map<String,Boolean> memberPresentFlags;
    double gl_lat;
    double gl_long;


    //These variables are related to secondary Events (Events inside group)
    GeoQuery secondaryEventMembersQuery;
    Double secondaryEventRadius=0.2;
    Runnable secondaryEventRunnable;
    Handler secondaryEventHandler;
    private Map<String,Marker> secondaryEventMarkerMap;
    private Map<String,Boolean> secondaryEventMemberPresentFlag;

    //Button for group markers
    Button callButton;      //A button above marker to start phone call
    Button chatButton;         //A button above marker to start chat (disabled and not needed over here because chat tab is already there)

    String current_number="";       //The number that has been selected currently
    boolean markerClickflag=false;      //Flag to know whether a marker has been clicked or not
    Map <String,String> titleToNumber;      //A Hashmap to get title of marker and map it to the phone number (Used for the phone call fn)


    public static String chosenEvent="";


    public void passUserDetails(String userID, String userName, String entityName, String entityID, char type)
    {
        /*
        Function called by SingleEntityActivity to pass crucial variables to the tab.
         */
        mUserID= userID;
        mUserName=userName;
        mEntityName=entityName;
        mEntityID=entityID;
        mType=type;
    }


    private void contactMarkerinit(String contactFirebaseAddress)
    {
        /*
        Function to initialize contact Marker given the Firebase Address of the
        location coordinates of the contact broadcasting location
         */

        //Get the Database Reference of the contact's coordinates from Firebase
        if(contactLatLong==null) {

            contactLatLong = Generic.database.getReference(contactFirebaseAddress);
            //      Toast.makeText(getContext(),contactFirebaseAddress,Toast.LENGTH_SHORT).show();
        }

        //Event listener added to listen for changes in contact's address
        contactLatLongEventListener=contactLatLong.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LatLng contactLatLng=null;
                double latitude=10000, longtitude=-10000;
                int i=0;
                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    //Retrieving latitude and longitude from Datasnapshot

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
                //Setting latitude and longitude to a temporary LatLng object
                contactLatLng= new LatLng(latitude,longtitude);
                if(mMap!=null) {


                    if(mContactMarker!=null)       //Not the first time location is initialized
                    {
                        //Just change position of contact
                        mContactMarker.setPosition(contactLatLng);

                    }
                    else {                          //First time location is initialized

                        //Create marker and set location
                        mContactMarker = mMap.addMarker(new MarkerOptions().position(contactLatLng).
                                title(mEntityName).
                                icon(BitmapDescriptorFactory.fromBitmap(unknownUser)).anchor(0.5f,0.5f));



                    }

                    //If profile picture of the contact is avaialble, then set marker as profile picture
                    if(userProfilePics.containsKey(mEntityID))
                    {
                        mContactMarker.setIcon(BitmapDescriptorFactory.fromBitmap(
                                userProfilePics.get(mEntityID)
                        ));
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
        /*
        Function to initialize contact marker
         */
        final String contactFirebaseAddress= "Users/"+mEntityID+"/Loc";

        if(isBroadcastingLocation.containsKey(mEntityID) && isBroadcastingLocation.get(mEntityID)==true)
        {
            //Initialize the marker because already the Contact is broadcasting his/her location
            contactMarkerinit(contactFirebaseAddress);
            isLocationAllowed=true;
        }

        if(contactBroadcastReceiver==null)
        {

            //Initialize the broadcast reciever. This listens to LandingPageActivity to know
            // whether a contact is broadcasting or not.
            contactBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    wasLocationAllowed=isLocationAllowed;       //Current isLocationAllowed flag is no longer the currrent state. Put it in wasLocation allowed.
                    isLocationAllowed=intent.getBooleanExtra(mEntityID,false);  //Flag to determine whether contact is currently broadcasting or not

                    if(isLocationAllowed)
                    {
                        //Contact is Broadcasting now

                        if(mContactMarker==null)
                        {
                            //First time initialize contact marker
                            contactMarkerinit(contactFirebaseAddress);

                        }
                        else if(wasLocationAllowed==false)
                        {
                            //Contact was already initialized but location broadcast of contact was turned off

                            //Set the listener (which listens to location of contact) to the database
                            //reference (which contains the address of contact's location in Firebase)
                            contactLatLong.addValueEventListener(contactLatLongEventListener);
                            mContactMarker.setVisible(true);

                        }

                    }
                    else
                    {
                        //Contact is no longer broadcasting

                        if(wasLocationAllowed==true)
                        {
                            //Contact was broadcasting before but not now
                            contactLatLong.removeEventListener(contactLatLongEventListener);
                            mContactMarker.setVisible(false);
                        }
                    }

                }
            };
        }
        getContext().registerReceiver(contactBroadcastReceiver,new IntentFilter("contact_listener"));
        //register the receiver.


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

    private void initSecondaryEventMembers()
    {
        /*
        Function to initialize the secondary event's members (Events inside group functionality, not needed here)
         */
        secondaryEventMarkerMap= new HashMap<>();
        secondaryEventMemberPresentFlag= new HashMap<>();
        secondaryEventHandler= new Handler();

        secondaryEventRunnable= new Runnable() {
            private String lastEvent="";
            private String currEvent="";

            private String getChosenSecondaryEvent()
            {

                //TODO Change logic based on selected event
          //      if(secondaryEvents!=null && secondaryEvents.size()>=1)
            //        return secondaryEvents.get(0).getID();
                return chosenEvent;
            }

            @Override
            public void run() {


                currEvent=getChosenSecondaryEvent();
                if(!lastEvent.equals(currEvent))
                {
                    //Preferred secondary event changed

                    //Remove all markers and flags; Reset event members

                    secondaryEventMemberPresentFlag.clear();
                    for(Map.Entry<String,Marker> marker: secondaryEventMarkerMap.entrySet())
                    {
                        marker.getValue().remove();
                    }
                    secondaryEventMarkerMap.clear();
                }
                if(!currEvent.equals(""))
                {
                    //eventID is got



                    for(Map.Entry<String,Boolean> isMemberPresentFlag: secondaryEventMemberPresentFlag.entrySet())
                    {
                        if(!isMemberPresentFlag.getValue())
                        {
                            //Member not present. Remove marker
                            secondaryEventMarkerMap.get(isMemberPresentFlag.getKey()).remove();
                            secondaryEventMarkerMap.remove(isMemberPresentFlag.getKey());
                        }
                        else
                        {
                            //Member present. Set marker to false
                            isMemberPresentFlag.setValue(false);
                        }
                    }




//                    Toast.makeText(getContext(),Double.toString(secondaryEventRadius),Toast.LENGTH_SHORT).show();

                    getSecondaryEventMembers(currEvent);

                    //ending statement
                    lastEvent=currEvent;

                }
                secondaryEventHandler.postDelayed(this,2500);
            }
        };

        secondaryEventHandler.postDelayed(secondaryEventRunnable,2500);
    }

    private void getSecondaryEventMembers(String eID)
    {
        String membersDatabaseAddress = "Loc/"+eID;
        double lat=gl_lat;
        double lon=gl_long;

        DatabaseReference reference = database.getReference(membersDatabaseAddress);

        GeoFire geoFire= new GeoFire(reference);

        secondaryEventMembersQuery = geoFire.queryAtLocation(new GeoLocation(lat,lon), secondaryEventRadius);

        secondaryEventMembersQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            private int count=0;

            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if(!key.equals(mUserID) && !mMarkersMap.containsKey(key)) {

                    if (!secondaryEventMarkerMap.containsKey(key)) {
                        //Member is present


                        //New key
                        String name = key;
                        if (allContactNames.containsKey(name))
                            name = allContactNames.get(name);

                        LatLng memberLatLng = new LatLng(location.latitude, location.longitude);
                        secondaryEventMarkerMap.put(key,
                                mMap.addMarker(new MarkerOptions().position(memberLatLng).
                                        title(name).
                                        icon(BitmapDescriptorFactory.fromBitmap(unknownUser))
                                ));

                    }
                    else
                    {
                        //Present before,Present now
                        secondaryEventMarkerMap.get(key).setPosition(new LatLng(
                                location.latitude,location.longitude
                        ));
                    }

                }
                else if(mMarkersMap.containsKey(key))
                {
                    if(secondaryEventMarkerMap.containsKey(key))
                    {
                        //Conflict because both maps contain key

                        //Remove key from secondary map
                        secondaryEventMarkerMap.get(key).remove();
                        secondaryEventMarkerMap.remove(key);
                    }
                    mMarkersMap.get(key).setVisible(true);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if(count<10)
                {
                    if(secondaryEventRadius<20)
                        secondaryEventRadius+=1;
                }
                else if(count>15)
                {
                    if(secondaryEventRadius>0.1)
                        secondaryEventRadius-=0.1;
                }
                secondaryEventMembersQuery.removeAllListeners();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void initEventMembers()
    {
        /*
        Function to initialize markers for event members. Not needed at this point
         */
        if(mMarkersMap==null)
            mMarkersMap = new HashMap<>();
        memberPresentFlags= new HashMap<>();
        memberHandler=new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                for(Map.Entry<String,Boolean> isMemberPresentFlag: memberPresentFlags.entrySet())
                {
                    if(!isMemberPresentFlag.getValue())
                    {
                        //Member not present. Remove marker
                        mMarkersMap.get(isMemberPresentFlag.getKey()).remove();
                        mMarkersMap.remove(isMemberPresentFlag.getKey());
                    }
                    else
                    {
                        //Member present. Set marker to false
                        isMemberPresentFlag.setValue(false);
                    }
                }

                getEventMembers();
                Toast.makeText(getContext(),Double.toString(radius),Toast.LENGTH_SHORT).show();
                memberHandler.postDelayed(this,2500);
            }
        } ;

        memberHandler.postDelayed(runnable,2500);

    }

    private void getEventMembers()
    {
        /*
        Function to get the members of events from database to Map. Not needed at this point
         */
        String membersDatabaseAddress = "Loc/"+mEntityID;
        double lat=gl_lat;
        double lon=gl_long;

        DatabaseReference reference = database.getReference(membersDatabaseAddress);

        GeoFire geoFire= new GeoFire(reference);



        eventmembersQuery = geoFire.queryAtLocation(new GeoLocation(lat,lon),radius);

        eventmembersQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            int count=0;

            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if(!key.equals(mUserID)) {

                    if (!mMarkersMap.containsKey(key)) {
                        //Member is present


                        //New key
                        String name = key;
                        if (allContactNames.containsKey(name))
                            name = allContactNames.get(name);

                        LatLng memberLatLng = new LatLng(location.latitude, location.longitude);
                        mMarkersMap.put(key,
                                mMap.addMarker(new MarkerOptions().position(memberLatLng).
                                        title(name).
                                        icon(BitmapDescriptorFactory.fromBitmap(unknownUser))
                                ));

                    } else {
                        //Present before,Present now
                        mMarkersMap.get(key).setPosition(new LatLng(
                                location.latitude,location.longitude
                        ));
                    }

                    memberPresentFlags.put(key, true);
                    if (membersProfilePic != null && membersProfilePic.containsKey(key)) {

                        mMarkersMap.get(key).setIcon(BitmapDescriptorFactory.fromBitmap(
                                membersProfilePic.get(key)
                        ));
                        mMarkersMap.get(key).setAnchor(0.5f,0.5f);
                    }

                    count++;
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if(count<10)
                {
                    if(radius<2)
                        radius+=0.1;
                }
                else if(count>15)
                {
                    if(radius>0.1)
                        radius-=0.1;
                }
                eventmembersQuery.removeAllListeners();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }


    private void getMembersCoordinates()
    {
        /*
        Function to get coordinates of group members and put them in markers
         */
        //Get address of the group members coordinates in Firebase
        String membersDatabaseAddress= "Loc/"+mEntityID;

        //Get Firebase RealtimeDB Reference
        DatabaseReference reference= database.getReference(membersDatabaseAddress);

        //Listen only once to the existing coordinates (This runs every 2.5 seconds or so. The coordinates
        //are taken in bulk inorder to reduce overhead costs.
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Put dataSnapshot in MemberMap
                Map<String,ArrayList<Double>> memberMap = (Map) dataSnapshot.getValue();

                /*
                Note: The Algo used here is nearly the same as the one used for fetching members from Database
                 in the membersInit() function in SingleEntityActivity. Only here, we have the marker's map as the
                 existing hashmap of markers of members and the memberMap as the map fetched from the database.
                 */
                for(Map.Entry<String,Marker> entry:mMarkersMap.entrySet())
                {
                    //Iterate through marker map
                    String memberID= entry.getKey();
                    if(memberMap!=null && memberMap.containsKey(memberID))
                    {
                        //If memberMap contains the key of current element of MarkerMap, then
                        //Already marker in Markermap and needs to be updated only

                        if(!memberID.equals(mUserID)) {
                            //If group member is NOT the user himself
                            //then get coordinates and store in LatLng object

                            Double latitude = memberMap.get(memberID).get(0);
                            Double longitude = memberMap.get(memberID).get(1);
                            LatLng memberLatLng = new LatLng(latitude, longitude);
                            entry.getValue().setPosition(memberLatLng);
                            entry.getValue().setVisible(true);

                            //If marker has been selected, then centre camera there
                            if(current_number.equals("+91"+entry.getKey())) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
                            }

                        }

                        //Set isMemberBroadcastingLocation hashmap to true. This shows
                        //in the members tab that the member is broadcasting location
                        isMemberBroadcastingLocation.put(memberID,true);

                        //refresh member tab
                        if(mMembersTab!=null)
                           mMembersTab.refresh();

                        if(membersProfilePic!=null &&
                                membersProfilePic.containsKey(memberID) && mMemberSetProfilePicFlag.containsKey(memberID)
                                && !mMemberSetProfilePicFlag.get(memberID))
                        {
                            //Change Member marker to ProfilePic

                            Bitmap memberProfilePic= membersProfilePic.get(entry.getKey());
                            mMarkersMap.get(entry.getKey()).setAnchor(0.5f,0.5f);
                            mMarkersMap.get(entry.getKey()).setIcon(BitmapDescriptorFactory.fromBitmap(
                                    memberProfilePic));
                        }

                        //Remove member from the MemberMap because he/she has been updated on map
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

                    //Iterate through memberMap now. All the entries remaining are of those members who
                    //were not broadcasting before (and hence absent from MarkersMap) but are doing so now
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
                            //If member is not the current User, then get Latitude and Longitude
                            //and store it in LatLng object

                            Double latitude = entry.getValue().get(0);
                            Double longitude = entry.getValue().get(1);

                            String current_num_set=title;

                            if (allContactNames.containsKey(title))
                                title = allContactNames.get(title);

                            if (allContactNames != null && allContactNames.containsKey(title))
                                title = allContactNames.get(title);

                            titleToNumber.put(title,current_num_set);
                            LatLng memberLatLng = new LatLng(latitude, longitude);

                            //Initialize the new marker for the member
                            mMarkersMap.put(entry.getKey(),
                                    mMap.addMarker(new MarkerOptions().position(memberLatLng).
                                            title(title).
                                            icon(BitmapDescriptorFactory.fromBitmap(unknownUser)))
                            );

                            //Change marker to profile picture if available
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
        /*
        Function to initialize markers of members.
         */
        /*
        Initialize MarkersMap. Start a handler that contains a runnable that calls the function getMembersCoordinates every 2.5 seconds
         */
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
            titleToNumber= new HashMap<>();

            mapFlag=true;
            mMapFrag.getMapAsync(this);

            callButton = (Button) getActivity().findViewById(R.id.se_callButton);
            callButton.setVisibility(View.INVISIBLE);
            callButton.setClickable(false);

            chatButton = (Button) getActivity().findViewById(R.id.se_chatButton);
            chatButton.setVisibility(View.INVISIBLE);
            chatButton.setClickable(false);

            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    /*
                    Start a phone call based on number selected
                     */

                    try {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", current_number, null));
                        startActivity(intent);
                    }
                    catch (Exception e)
                    {

                    }
                }
            });



        }





 //       Log.d("Tag1", "onViewCreated: ");

        //create locationListener
        locationListener= new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                double Latitude= location.getLatitude();    //Get latitude
                gl_lat=Latitude;

                double Longitude = location.getLongitude();     //Get longitude
                gl_long=Longitude;

                LatLng latLng= new LatLng(Latitude, Longitude);

                //Map latitude and longitude
                if(mMap!=null) {

                    if (mlocationMarker != null)       //Not the first time location is initialized
                    {
                        mlocationMarker.setPosition(latLng);

                    } else {                          //First time location is initialized

                        mlocationMarker = mMap.addMarker(new MarkerOptions().position(latLng).
                                title("Me").
                                icon(BitmapDescriptorFactory.fromResource(R.drawable.my_location)));

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                    }

                    if (userProfilePics != null &&
                            userProfilePics.containsKey(mUserID) && !mlocationsetProfilePic) {
                        Bitmap bitmap = userProfilePics.get(mUserID);
                        mlocationMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                        mlocationMarker.setAnchor(0.5f, 0.5f);
                        mlocationsetProfilePic = true;
                    }


                    if (mType == 'G') {
                        //Testing

                        if (placesQuery == null)
                            getNearbyPlaces(Latitude, Longitude);
                        else {
                            placesQuery.setCenter(new GeoLocation(Latitude, Longitude));
                        }

                        if(!locationAvailableFlag)
                        {
                            locationAvailableFlag=true;
                            initSecondaryEventMembers();
                        }
                    }

                    if (mType == 'E') {
                        //Events: Get members based on proximity

                        if (!locationAvailableFlag) {
                            locationAvailableFlag = true;
                            initEventMembers();
                        }
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
        /*
        Function to init places. Not needed now
         */

        for(Map.Entry<String,Place> placeEntry: placesMap.entrySet())
        {
            addPlace(placeEntry.getKey(),placeEntry.getValue());
        }

    }

    public void addPlace(String key, Place place)
    {
        /*
        Function to add place. Not needed now
         */
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
                        icon(BitmapDescriptorFactory.fromBitmap(unknownUser))));
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
        /*
        Function to remove place. Not needed now.
         */
        if(placesMarkers.containsKey(key))
        {
            placesMarkers.get(key).remove();
            placesMarkers.remove(key);

        }
    }


    private void addGeoData(String key, double lat, double lon)
    {
        /*
        Testing GeoFire by adding data. Not needed now
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
        Function to Test Geoquery. Not needed now
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
            else if (mType == 'G') {
                membersInit();

                //If Map is ready, then Initialize places
                placesInit();
                setMarkerListener();
                setMapClickListener();
            }




        }

    }

    private void showMarkerButtons(boolean showMarkerButtons)
    {
        /*
        Function to show/hide marker buttons
         */
        if(showMarkerButtons)
        {
            callButton.setVisibility(View.VISIBLE);
            callButton.setClickable(true);
        //    chatButton.setVisibility(View.VISIBLE);
         //   chatButton.setClickable(true);
        }
        else
        {
        //    chatButton.setVisibility(View.INVISIBLE);
        //    chatButton.setClickable(false);
            callButton.setVisibility(View.INVISIBLE);
            callButton.setClickable(false);
        }
    }

    private void setMarkerListener(){


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {


                current_number="+91"+titleToNumber.get(marker.getTitle());
                //On Marker Click, get the phonenumber of marker

                markerClickflag=true;       //Set marker Click flag for true

                return false;
            }
        });
    }

    private void setMapClickListener()
    {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //Any click on the map should hide all buttons and make marketClickFlag false;
                showMarkerButtons(false);
                current_number="";
                markerClickflag=false;
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                //If marker click flag is rue(marker is clicked), show the buttons.
                if(markerClickflag)
                    showMarkerButtons(true);

                //Once camera adjusts to the marker, make the marker click flag false. Any newmovement of camera will cause
                //buttons to disappear.
                markerClickflag=false;
            }
        });



        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                //If marker is clicked (i.e. marker click flag is set), then do not do anthying
                //otherwise, if marker click flag is false, hide the buttons when camera is moved.
                if(!markerClickflag)
                    showMarkerButtons(false);
            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /*
        Call back function to clear all variable once tab is destroyed
         */
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

            if(secondaryEventHandler!=null)
            {
                if(secondaryEventRunnable!=null)
                {
                    secondaryEventHandler.removeCallbacks(secondaryEventRunnable);
                    secondaryEventRunnable=null;
                }
                secondaryEventHandler=null;
            }
            if(secondaryEventMarkerMap!=null)
            {
                for(Map.Entry<String,Marker> currentMarkers: secondaryEventMarkerMap.entrySet() )
                {
                    currentMarkers.getValue().remove();
                }
                secondaryEventMarkerMap.clear();
            }

            if(secondaryEventMembersQuery!=null)
            {
                secondaryEventMembersQuery.removeAllListeners();
            }

            if(mMarkersMap!=null)
            {
                mMarkersMap.clear();
            }
            if(chosenEvent!=null)
            {
                chosenEvent="";
            }

            if(titleToNumber!=null)
                titleToNumber.clear();




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
