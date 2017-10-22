package com.example.shashank_pc.trial;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.shashank_pc.trial.Generic.firestore;
import static java.security.AccessController.getContext;

public class SingleEntityActivity extends AppCompatActivity {

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

    private String mEntityName;
    private String mEntityID;

    private char mType;

    private String mUserName;
    private String mUserID;
    private boolean isGPSBroadcastFlag;

    private TextView mTitle;
    private Button isGPSBroadcast;

    private Event mEvent;
    private Group mGroup;
    private User mContact;

    public static SEMembersTab mMembersTab=null;
    public static HashMap<String,Boolean> isMemberBroadcastingLocation;
    public static List<String> Members;
    public static Map<String,String> mirrorMembersMap;
    private DocumentReference MemberRef;

    private String type;


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mMembersTab=null;
        Members.clear();
        isMemberBroadcastingLocation.clear();
        mirrorMembersMap.clear();
        MemberRef=null;
        //TODO remove MemberRef Listeners
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_entity);


        isMemberBroadcastingLocation= new HashMap<>();
        Members = new ArrayList<>();
        mirrorMembersMap = new HashMap<String,String>();

        Intent caller = getIntent();
        mEntityName= caller.getStringExtra("Name");
        mEntityID=caller.getStringExtra("ID");
        mType=caller.getCharExtra("Type", ' ');
        isGPSBroadcastFlag = getGPSBroadcastFLag(mEntityID);

        if(mType=='E') {
            mEvent = new Event(mEntityName, "", mEntityID);
            mEvent.initBroadcastLocationFlag(isGPSBroadcastFlag);
        }
        else if(mType=='G'){
            mGroup = new Group(mEntityName, "", mEntityID);
            mGroup.initBroadcastLocationFlag(isGPSBroadcastFlag);
        }
        else if(mType=='U'){
            mContact = new User(mEntityName,mEntityID);
            mContact.initBroadcastLocationFlag(isGPSBroadcastFlag);
        }

        mUserName=caller.getStringExtra("Username");
        mUserID=caller.getStringExtra("UserID");

        mTitle=(TextView) findViewById(R.id.single_entity_title);
        mTitle.setText(mEntityName);

        isGPSBroadcast = (Button) findViewById(R.id.single_entity_contact_gps_broadcast_flag);

        setGPSBroadcastButtoncolor(isGPSBroadcastFlag);

        isGPSBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isGPSBroadcastFlag) {
                    isGPSBroadcastFlag = false;
                    Toast.makeText(getApplicationContext(), "GPS broadcasting OFF", Toast.LENGTH_SHORT).show();
                }
                else {
                    isGPSBroadcastFlag = true;
                    Toast.makeText(getApplicationContext(), "GPS broadcasting ON", Toast.LENGTH_SHORT).show();
                }
                    setGPSBroadcastButtoncolor(isGPSBroadcastFlag);
                    setGPSBroadcastSharedPreferences(isGPSBroadcastFlag);


            }
        });


        //Get details of Members
        membersInit();


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
        if(GPSBroadcastFlag)
            isGPSBroadcast.setBackground(getDrawable(R.drawable.single_entity_activity_button_on));
        else
            isGPSBroadcast.setBackground(getDrawable(R.drawable.single_entity_activity_button_off));
    }

    public void setGPSBroadcastSharedPreferences(boolean GPSBroadcastFlag) {
        SharedPreferences preferences = getSharedPreferences("LPLists", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit= preferences.edit();

        if(mType=='E')
        {
            //Event
            mEvent.setBroadcastLocationFlag(GPSBroadcastFlag,mUserID);
            edit.putBoolean(mEntityID,GPSBroadcastFlag);
            edit.commit();
        }
        else if(mType=='G')
        {
            //Group
            mGroup.setBroadcastLocationFlag(GPSBroadcastFlag,mUserID);
            edit.putBoolean(mEntityID,GPSBroadcastFlag);
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

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }





        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch(position)
            {
                case 0:
                    if(mSEMapTab==null) {
                        mSEMapTab = new SEMapTab();
                        mSEMapTab.passUserDetails(mUserID, mUserName, mEntityName, mEntityID, mType);
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
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
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
            }
            return null;
        }
    }

    private void membersInit()
    {
        String type="";

        if(mType=='E')
            type="events";
        else if(mType=='G')
            type="groups";

        MemberRef = firestore.collection(type).document(mEntityID).
                collection("members").
                document("members");

        MemberRef.
                addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e == null) {
                            Map<String, Object> firestoreMemberMap = new HashMap<>();
                            firestoreMemberMap = documentSnapshot.getData();

                            List<String> entitiesToBeRemoved = new ArrayList<String>();

                            for(Map.Entry<String,String> memberEntityID: mirrorMembersMap.entrySet())
                            {
                                //Traverse along mirrorMembersMap

                                if(firestoreMemberMap.containsKey(memberEntityID.getValue()))
                                {
                                    //Member was present and is still present in Group/Event

                                    //Then remove that person from imported hashmap
                                    firestoreMemberMap.remove(memberEntityID.getValue());


                                }
                                else
                                {
                                    //Member was present but is no longer present
                                    String memberID= memberEntityID.getValue();
                                    User member= new User(memberID,memberID);
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

                                String key= (String) newMember.getValue();
                                String memberID= newMember.getKey();
                                mirrorMembersMap.put(key,memberID);


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
}
