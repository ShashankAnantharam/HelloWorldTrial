package com.example.shashank_pc.trial;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.shashank_pc.trial.Generic.firestore;
import static com.example.shashank_pc.trial.Generic.storage;
import static com.example.shashank_pc.trial.GenericFunctions.addProfilePic;
import static com.example.shashank_pc.trial.GenericFunctions.getAttendingEvents;
import static com.example.shashank_pc.trial.GenericFunctions.getCircleBitmap;
import static com.example.shashank_pc.trial.GenericFunctions.resizeImage;
import static com.example.shashank_pc.trial.GenericFunctions.secondaryEvents;
import static java.security.AccessController.getContext;

public class SingleEntityActivity extends AppCompatActivity {

    /*
    Activity that starts whenever a user clicks on an event or a group
     */

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private String mEntityName;     //Name of Entity (Event/ Group)
    private String mEntityID;       //If of Entity (Event/Group)

    private char mType;             //Character to denote type of entity (E for event, G for group)

    private String mUserName;       //UserName
    private String mUserID;         //UserID
    private boolean isGPSBroadcastFlag;     //flag to denote whether user is broadcasting location to entity

    private TextView mTitle;        //Text view to show title of entity
    private Button isGPSBroadcast;      //Button for User to Broadcast location

    private Event mEvent;           //Event class
    private Group mGroup;               //Group class
    private User mContact;              //Contact class (Although, now contacts are not included within this activity)



    public static SEMapTab mMapTab=null;        //SEMapTab class for the tab dealing with the map
    public static SEMembersTab mMembersTab=null;        //SEMembersTab class to show all the members
    public static SEPlacesTab mPlacesTab=null;          //SEPlacesTab class to show the places

    public static HashMap<String,Boolean> isMemberBroadcastingLocation;     //boolean map to know if member is broadcasting location
    public static List<String> Members;         //List of members
    public static Map<String,String> mirrorMembersMap;      //Hashmap for Members (i.e. A unique ID of the group mapped to the member's phone no.)
    public static Map<String,Bitmap> membersProfilePic;         //Map of profile pics of group members
    private DocumentReference MemberRef;            //Reference to the Firestore database where the member's details are present

    private String type;

    public static Map<String, Place> placesMap;         //Map for places (Not need at this point)
    private DocumentReference placesRef;

    public static Map<String,Boolean> secondaryEventsClickFlag;         //Relating to events inside groups (not needed now)



    @Override
    protected void onDestroy()
    {
        //Clear al variables
        super.onDestroy();
        mMembersTab=null;
        Members.clear();
        isMemberBroadcastingLocation.clear();
        mirrorMembersMap.clear();
        membersProfilePic.clear();
        MemberRef=null;

        if(placesRef!=null) {
            placesRef = null;
        }
        if(placesMap!=null)
            placesMap.clear();
        //TODO remove MemberRef Listeners
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_entity);


        //Initialize all variables
        isMemberBroadcastingLocation= new HashMap<>();
        Members = new ArrayList<>();
        mirrorMembersMap = new HashMap<String,String>();
        membersProfilePic = new HashMap<>();

        //Using the intent to get the details of the entity (group/event) from the previous activity (LPMainActivity)
        Intent caller = getIntent();
        mEntityName= caller.getStringExtra("Name");
        mEntityID=caller.getStringExtra("ID");
        mType=caller.getCharExtra("Type", ' ');
        isGPSBroadcastFlag = getGPSBroadcastFLag(mEntityID);

        if(mType=='E') {
            //If type is E, then create event object with the details
            mEvent = new Event(mEntityName, "", mEntityID);
            mEvent.initBroadcastLocationFlag(isGPSBroadcastFlag);
        }
        else if(mType=='G'){
            //If type is G, then create group object with the details
            mGroup = new Group(mEntityName, "", mEntityID);
            mGroup.initBroadcastLocationFlag(isGPSBroadcastFlag);
        }
        else if(mType=='U'){
            //If type is U, then create contact object with the details (This is no longer valid as
            // contacts have been shifted to SingleContactActivity
            mContact = new User(mEntityName,mEntityID);
            mContact.initBroadcastLocationFlag(isGPSBroadcastFlag);
        }

        mUserName=caller.getStringExtra("Username");
        mUserID=caller.getStringExtra("UserID");

        mTitle=(TextView) findViewById(R.id.single_entity_title);
        mTitle.setText(mEntityName);

        //Initialize the Location Broadcast Button
        isGPSBroadcast = (Button) findViewById(R.id.single_entity_contact_gps_broadcast_flag);

        //Based on whether user has broadcast location or not, set the color of the GPSBroadcastButton
        setGPSBroadcastButtoncolor(isGPSBroadcastFlag);

        isGPSBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toggling the button to share/not share location with the entity (group/event)

                if(isGPSBroadcastFlag) {
                    //If isGPSBroadcast is true, then make it false
                    isGPSBroadcastFlag = false;
                    Toast.makeText(getApplicationContext(), "GPS broadcasting OFF", Toast.LENGTH_SHORT).show();
                }
                else {
                    //If isGPSBroadcast is false, make it true
                    isGPSBroadcastFlag = true;
                    Toast.makeText(getApplicationContext(), "GPS broadcasting ON", Toast.LENGTH_SHORT).show();
                }
                //Set the button color and the SharedPreferences value of isGPSBroadcastFlag accordingly
                    setGPSBroadcastButtoncolor(isGPSBroadcastFlag);
                    setGPSBroadcastSharedPreferences(isGPSBroadcastFlag);


            }
        });


        //Get details of Members
        membersInit();
        //Get Places (Not part of the MVP)
        getPlacesFromDB();

        if(mType=='G')
        {
            //Get secondary events if the entity is group (Not needed at this point. Events inside group functionality)
            secondaryEvents = new ArrayList<>();
            secondaryEventsClickFlag= new HashMap<>();
            getAttendingEvents(mType,mEntityID);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle(mEntityName);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);




      /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

    }

    boolean getGPSBroadcastFLag(String ID)
    {
        /*
        ID is the entity ID (ID of event, group)
        Given the entity ID, the shared preferences are scanned to get the status of location broadcast flag.
         */
        SharedPreferences preferences = getSharedPreferences("LPLists", Context.MODE_PRIVATE);
        return preferences.getBoolean(ID,false);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.single_entity_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setGPSBroadcastButtoncolor(boolean GPSBroadcastFlag) {
        //Function to set GPSBroadcastFlag color
        if(GPSBroadcastFlag)
            isGPSBroadcast.setBackground(getDrawable(R.drawable.single_entity_activity_button_on));
        else
            isGPSBroadcast.setBackground(getDrawable(R.drawable.single_entity_activity_button_off));
    }

    public void setGPSBroadcastSharedPreferences(boolean GPSBroadcastFlag) {
        //Function to set the location broadcast button state in shared preferences and alter the database
        SharedPreferences preferences = getSharedPreferences("LPLists", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit= preferences.edit();

        if(mType=='E')
        {
            //Event
            mEvent.setBroadcastLocationFlag(GPSBroadcastFlag,mUserID);      //Set the broadcast location flag in backend
            edit.putBoolean(mEntityID,GPSBroadcastFlag);            //Save the flag in shared preferences
            edit.commit();
        }
        else if(mType=='G')
        {
            //Group
            mGroup.setBroadcastLocationFlag(GPSBroadcastFlag,mUserID);  //Set the broadcast location flag in backend
            edit.putBoolean(mEntityID,GPSBroadcastFlag);        //Save the flag in shared preferences
            edit.commit();
        }
        else if(mType=='U')
        {
            //Contact
            mContact.setBroadcastLocationFlag(GPSBroadcastFlag,mUserID);
            edit.putBoolean(mEntityID,GPSBroadcastFlag);
            edit.commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SEMapTab mSEMapTab;
        SEChatsTab mSEChatsTab;
        SEMembersTab mSEMembersTab;
        SEPlacesTab mSEPlacesTab;
        SEEventsTab mSEEventsTab;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }





        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch(position)
            {
                //Initialize the tabs: SEMap, SEChats, SEMembers, SEPlaces, SEEvents
                case 0:
                    if(mSEMapTab==null) {
                        mSEMapTab = new SEMapTab();
                        mSEMapTab.passUserDetails(mUserID, mUserName, mEntityName, mEntityID, mType);
                        mMapTab=mSEMapTab;
                        return mSEMapTab;
                    }
                case 1:
                    if(mSEChatsTab==null) {
                        mSEChatsTab = new SEChatsTab();
                        mSEChatsTab.passUserDetails(mUserID, mUserName, mEntityName, mEntityID, mType);
                        return mSEChatsTab;
                    }
                case 2:
                    if(mSEMembersTab==null) {
                    /*
                    This is where testing code starts

                    Intent testActivity= new Intent();
                    testActivity.setClass(getApplicationContext(),TestActivity.class);
                    startActivity(testActivity);
                    /*
                    This is where testing code ends
                     */

                        mSEMembersTab = new SEMembersTab();
                        mSEMembersTab.passUserDetails(mUserID, mUserName, mEntityName, mEntityID, mType);
                        mMembersTab = mSEMembersTab;
                        return mSEMembersTab;
                    }
                case 3:
                    if(mSEPlacesTab==null)
                    {
                        mSEPlacesTab = new SEPlacesTab();
                        mPlacesTab= mSEPlacesTab;
                        return mSEPlacesTab;
                    }
                case 4:
                    if(mSEEventsTab==null)
                    {
                        mSEEventsTab= new SEEventsTab();
                        mSEEventsTab.passUserDetails(mUserID,mUserName);
                        return mSEEventsTab;
                    }
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            // Show 4 total pages for Events, 5 for groups.
            if(mType=='E')
                return 4;
            else if(mType=='G')
                return 5;
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                    case 0:
                        return "MAP";
                    case 1:
                        return "CHAT";
                    case 2:
                        return "MEMBERS";
                    case 3:
                        return "PLACES";
                    case 4:
                        return "EVENTS";

                }
            return null;
        }
    }

    private void membersInit()
    {
        /*
        Function to get the members from Firstore Backend to Android
         */
        String type="";

        if(mType=='E')
            type="events";
        else if(mType=='G')
            type="groups";

        MemberRef = firestore.collection(type).document(mEntityID).
                collection("members").
                document("members");

        //Add a realtime listener to the members document in Firebase Firestore
        MemberRef.
                addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        /*
                        Working of fetching code

                        Assume the case:
                        mirrorMembersMap (The current hashmap of members in our device):
                        9701420818
                        9989447799

                        firestoreMemberMap (Map that contains data fetched from database):
                        9701420818
                        9967887767

                        First traverse along mirrorMembersMap
                        9701420818 is there in both maps, i.e. he was a member before and still is a member.
                        Remove 9701420818 from firestoreMemberMap

                        Next, we get to 9989447799.
                        This member was present but is no longer present
                        Delete 9989447799 from MembersTab and from mirrorMembersMap

                        Therefore,
                        MirrorMembersMap:
                        9701420818

                        firestoreMemberMap:
                        9967887767

                       Now, firestoreMemberMap contains all members who were not part of group but are now part of group
                        traverse through firestoreMemberMap and initialize remaining members into group
                        Therefore,
                        mirrorMembersMap (final):
                        9701420818
                        9967887767

                         */

                        if (e == null) {
                            Map<String, Object> firestoreMemberMap = new HashMap<>();
                            firestoreMemberMap = documentSnapshot.getData();
                            //Backend data saved temporarily in firestoreMemberMap

                            List<String> entitiesToBeRemoved = new ArrayList<String>();

                            for(Map.Entry<String,String> memberEntityID: mirrorMembersMap.entrySet())
                            {
                                //Traverse along mirrorMembersMap that contains the members

                                if(firestoreMemberMap.containsKey(memberEntityID.getKey()))
                                {
                                    //Member was present and is still present in Group/Event

                                    //Then remove that person from imported hashmap
                                    firestoreMemberMap.remove(memberEntityID.getKey());


                                }
                                else
                                {
                                    //Member was present but is no longer present
                                    String memberID= memberEntityID.getValue();
                                    if(mMembersTab==null)
                                    {
                                        //Members Tab not yet initialized

                                        //Remove member form Members List
                                        Members.remove(Members.indexOf(memberID));

                                    }
                                    else
                                    {
                                        //Members Tab initialized

                                        //remove member from main Members tab
                                        mMembersTab.removeContact(memberID);

                                        //Remove member's profile Pic
                                        membersProfilePic.remove(memberID);
                                    }

                                    //Remove entity from mirror map hashtable
                                    entitiesToBeRemoved.add(memberEntityID.getKey());
                                }
                            }

                            for(String entityKey: entitiesToBeRemoved)
                            {
                                mirrorMembersMap.remove(entityKey);
                            }

                            for(Map.Entry<String,Object> newMember: firestoreMemberMap.entrySet())
                            {
                                //Iterate across members who are left, i.e. New members


                                //Add new member to Mirror Hash Map

                                String memberID= (String) newMember.getValue();
                                String key= newMember.getKey();
                                mirrorMembersMap.put(key,memberID);

                                addProfilePic(memberID);

                                if(mMembersTab==null)
                                {
                                    //If members tab not yet initialized

                                    //Add member to memberID
                                    Members.add(memberID);


                                }
                                else
                                {
                                    //If members tab has been initialized

                                    //Add member to main MembersArray
                                    User user = new User(memberID,memberID);
                                    mMembersTab.addContact(user);
                                }
                            }



                        }
                    }
                });
    }


    public void getPlacesFromDB()           //Part of places functionality, not needed at this point
    {
        placesMap = new HashMap<>();

        String type="";
        if(mType=='E')
            type="events";
        else if(mType=='G')
            type="groups";

        placesRef = firestore.collection(type).document(mEntityID).collection("places").document("places");

        placesRef.addSnapshotListener(new com.google.firebase.firestore.EventListener<DocumentSnapshot>() {

            private Map<String, Object> placesFromDB;

            private Place getPlace(String key)
            {
                Place place = new Place();


                Map<String,String> placeDetails = (Map<String,String>) placesFromDB.get(key);

                if(placeDetails==null)
                {
                    return place;
                }


                place.setName(placeDetails.get("name"));
                place.setLat(placeDetails.get("lat"));
                place.setLon(placeDetails.get("lon"));
                place.setType(placeDetails.get("type"));


                return place;
            }

            private void addPlace(String key)
            {


                try {

                Place place = getPlace(key);




                //Update value
                placesMap.put(key,place);



                    if (mMapTab != null)
                        mMapTab.addPlace(key, place);
                }
                catch (Exception e)
                {

                }


                //TODO      if(SEPlace!=null){ mPlaceTab.addPlace(key)}



            }

            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                try {
                    placesFromDB = documentSnapshot.getData();


                    List<String> placesToBeRemoved = new ArrayList<String>();
                    List<String> placesToBeModified = new ArrayList<String>();

                    for (Map.Entry<String, Place> placeEntry : placesMap.entrySet()) {
                        //Iterate over existing places

                        String key = placeEntry.getKey();
                        if (placesFromDB.containsKey(key)) {

                            //Place there initially and there now. Elements of it may have changed so update

                            //Updating place to new place
                            placesToBeModified.add(key);


                        } else {
                            //Place was deleted
                            placesToBeRemoved.add(key);
                        }

                        //Remove entry from downloaded Map

                    }

                    for (String placeID : placesToBeRemoved) {
                        //Remove all places that were deleted

                        placesMap.remove(placeID);
                        if (mMapTab != null)
                            mMapTab.removePlace(placeID);
                        placesFromDB.remove(placeID);

                    }

                    for (String placeID : placesToBeModified) {
                        addPlace(placeID);
                        placesFromDB.remove(placeID);
                    }

                    for (Map.Entry<String, Object> newPlace : placesFromDB.entrySet()) {
                        String key = newPlace.getKey();
                        if (!newPlace.getKey().equals("T")) {//New Place

                            //Add to PlacesMap
                            addPlace(key);
                        }

                    }

                    placesFromDB.clear();
                }
                catch (Exception exception)
                {

                }
            }

        });

    }


}
