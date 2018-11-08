package com.example.shashank_pc.trial;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shashank_pc.trial.Helper.BasicHelper;
import com.example.shashank_pc.trial.Helper.TestHelper;
import com.example.shashank_pc.trial.classes.AlarmBroadcastReciever;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.shashank_pc.trial.Generic.firestore;
import static com.example.shashank_pc.trial.GenericFunctions.addProfilePic;
import static com.example.shashank_pc.trial.GenericFunctions.decodeNumber;
import static com.example.shashank_pc.trial.GenericFunctions.encodeNumber;
import static com.example.shashank_pc.trial.GenericFunctions.getCircleBitmap;
import static com.example.shashank_pc.trial.GenericFunctions.initEncoding;
import static com.example.shashank_pc.trial.GenericFunctions.mEncoding;
import static com.example.shashank_pc.trial.GenericFunctions.resizeImage;
import static java.security.AccessController.getContext;

public class LandingPageActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private PowerManager.WakeLock wakeLock;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private Button viewNearbyEvents;

    private boolean regFlag=false;      //Flag to know if user is registered or not


    public static Map<String,Bitmap> userProfilePics;   //HashMap to store Profile Pics of contacts

    private static String mUserID="";       //UserID string

    private String mUserName="";        //Usernae string

    private LPEventsTab mEventLPTab;    //Events List Tab
    private LPGroupsTab mGroupLPTab;    //Groups List Tab
    private LPContactsTab mContactLPTab;    //Contacts List Tab
    public static LPMapTab mMapLPTab;       //MapTab

    public static List<User> contacts;  //List of contacts


    FirebaseFirestore firestore;        //Firestore database
    DocumentReference firestoneUserRef; //Reference to Firestore database location
    private String fEntityName;
    private String fEntityDesc;
    public static Bitmap unknownUser;       //Initialize the default profile picture (If no profile picture is available)


    private BroadcastReceiver locationBroadcastReceiver;

    private boolean gpsflag;        //flag for gps


    private DatabaseReference contactNodeRef;       //database Reference for Firebase Realtime DB To listen to which contacts are broadcasting
    private ChildEventListener contactNodeChildListener;  //child event listener for firebase database ref
    private Intent contactListner;          //Contact listner Intent
    public static HashMap<String,Boolean> isBroadcastingLocation;       //Hashmap to know if contact is broadcasting location

    public static Map<String,Integer> allEntities;      //All Entites hashmap

    public static Map<String,Boolean> allButtons;       //Hashmap for the values of isGPSBroadcast button of all entities
    public static Map<String,String> allContactNames;   //Hashmap connecting contact ID to contact Names



    public static String getUserID()
    {
        return mUserID;
    }       //Function to get userID





    @Override
    protected void onResume() {
        super.onResume();

        if(mGroupLPTab!=null) {
            mGroupLPTab.refresh();
        }

        if(mEventLPTab!=null) {
            mEventLPTab.refresh();
        }

        if(mContactLPTab!=null){
            mContactLPTab.refresh();
        }


 /*
 Receive and listen to the location from GPS_Service

        if(locationBroadcastReceiver==null)
        {

            locationBroadcastReceiver= new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    double latitude= intent.getDoubleExtra("Latitude",0);

//                    writeGPSLat.setValue(latitude);

                    double longitude= intent.getDoubleExtra("Longitude",0);
//                    writeGPSLong.setValue(longitude);

  //                  Toast.makeText(getApplicationContext(),latitude+" "+longitude,Toast.LENGTH_SHORT).show();


                }
            };

        }
        registerReceiver(locationBroadcastReceiver,new IntentFilter("location_update"));
*/
    }

    @Override
    protected void onDestroy() {
        /*
        Stop broadcast receiver and GPS Service once app is closed
         */
        super.onDestroy();

        Intent gpsIntent = new Intent(getApplicationContext(), GPS_Service.class);
//Toggle here
      //  stopService(gpsIntent);
        wakeLock.release();
        contactNodeRef.removeEventListener(contactNodeChildListener);
//        Toast.makeText(getApplicationContext(),"ON DESTROY CALLED",Toast.LENGTH_SHORT);
    }

    @Override
    protected void onPause() {
        super.onPause();
    //    Intent gpsIntent = new Intent(getApplicationContext(), GPS_Service.class);
   //     stopService(gpsIntent);

  //      Toast.makeText(getApplicationContext(),"ON PAUSE CALLED",Toast.LENGTH_SHORT);
    }

    public void startServiceUsingAlarm()
    {
        TestHelper.setLookouts(getApplicationContext());

        BasicHelper.turnOnFirebaseDatabases(getApplicationContext());
        Calendar cur_cal = Calendar.getInstance();
        cur_cal.setTimeInMillis(System.currentTimeMillis());
        cur_cal.add(Calendar.SECOND, 50);

        BasicHelper.setServiceStatus(getApplicationContext(),true);
        Toast.makeText(getApplicationContext(),Boolean.toString(
                BasicHelper.getServiceStatus(getApplicationContext())
        ),Toast.LENGTH_SHORT).show();
        Intent broadcastIntent = new Intent(getApplicationContext(), AlarmBroadcastReciever.class);
        PendingIntent pendingIntent= PendingIntent.getBroadcast(getApplicationContext(),0,broadcastIntent,0);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis(), 1*1000, pendingIntent);
        BasicHelper.setAppInForeground(getApplicationContext(),true);
        Intent gpsIntent = new Intent(getApplicationContext(), GeoLocationService.class);     //Intent to gps service class
        startService(gpsIntent);

     /*   Intent gpsIntent = new Intent(getApplicationContext(), GPS_Service.class);     //Intent to gps service class
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, gpsIntent, 0);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis(), 30*1000, pintent);
        //startService(gpsIntent);
        */
    }

    public void getDialogue(){
        /*
        Function to start a dialogue box to help user register.
        //TODO: Needs to be changed to incorporate firebase auth
         */

        //Pop dialogue to ask user to register (Note: This needs to be changed as Firebase Auth is not used here)

        //Initialize variables
        AlertDialog.Builder mBuilder= new AlertDialog.Builder(this);
        View mView= getLayoutInflater().inflate(R.layout.dialog_register, null);
        final EditText mPhone= (EditText) mView.findViewById(R.id.etPhone);
        final EditText mPass= (EditText) mView.findViewById(R.id.etPass);
        final EditText mName= (EditText) mView.findViewById(R.id.etName);
        final EditText mRtPass= (EditText) mView.findViewById(R.id.etRtPass);
        Button mRegisterButton = (Button) mView.findViewById(R.id.btnRegister);

        mBuilder.setView(mView);
        final AlertDialog dialog= mBuilder.create();
        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface D) {
                //If dialog is dismissed but user is not registered yet, then show the dialog; otherwise,
                //Load layout
                if(regFlag==false)
                    dialog.show();
                else {
                    loadLayout();
                    startServiceUsingAlarm();
                    gpsflag=true;
                }
            }

        });


        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //If register button is clicked, then check necessary preconditions for phone number and password
                String mPassword, mRetypePassword;
                mPassword=mPass.getText().toString();
                mRetypePassword=mRtPass.getText().toString();
                if(!GenericFunctions.validatePhone(mPhone.getText().toString()))
                {
                    Toast.makeText(getApplicationContext(),"Enter a valid Phone number", Toast.LENGTH_SHORT).show();
                }
                else if(mPassword.equals("")){
                    Toast.makeText(getApplicationContext(), "Enter a password", Toast.LENGTH_SHORT).show();
                }
                else if(!mPassword.equals(mRetypePassword)) {
                    Toast.makeText(getApplicationContext(), "Password and Retype Password dont match", Toast.LENGTH_SHORT).show();
                }
                else if(!GenericFunctions.isPasswordFine(mPassword))
                {
                    Toast.makeText(getApplicationContext(), "Password doesen't meet requirements : Upper case, lower case, special char, digit & atleast 6 charachters !", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //If all conditions match, then register the user and save his credentials in shared preferences.
                    SharedPreferences userDetails = getSharedPreferences("UserDetails",Context.MODE_PRIVATE);
                    SharedPreferences.Editor userDetailsEdit= userDetails.edit();

                    regFlag=true;

                    mUserID=mPhone.getText().toString();
                    userDetailsEdit.putString("UserID",mUserID);

                    mUserName=mName.getText().toString();
                    userDetailsEdit.putString("UserName",mUserName);

                    userDetailsEdit.putString("Password", mPass.getText().toString());

                    userDetailsEdit.commit();

                    dialog.dismiss();
                }

            }
        });



    }

    public void loadLayout()
    {
        /*
        Function to load the main layout of the activity (i.e. Events Tab, Groups Tab, Contacts Tab and LPMap Tab.
         */

        //Initialize variables
        initEncoding();
        allContactNames.put(mUserID,"Me");

        Generic.database = FirebaseDatabase.getInstance();
        Generic.storage = FirebaseStorage.getInstance();
        Generic.firestore= FirebaseFirestore.getInstance();
        userProfilePics = new HashMap<>();

        //Initialize the unknown user profile picture (also used for known users with no profile picture)
        unknownUser = BitmapFactory.decodeResource(getResources(),R.drawable.unknown);
        unknownUser = resizeImage(unknownUser);
        unknownUser = getCircleBitmap(unknownUser);

        addProfilePic(mUserID);

        //Start a wake lock to ensure that the app does not sleep.
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock=pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,"My Wakelock");
        wakeLock.acquire();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        /*
        The fab function can be removed. It was put in for testing purposes.
         */
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent gpsIntent = new Intent(getApplicationContext(), GPS_Service.class);     //Intent to gps service class
                if(gpsflag==true) {
                    Toast.makeText(getApplicationContext(),"Stop Service",Toast.LENGTH_SHORT);
                    stopService(gpsIntent);
                    gpsflag=false;
                }
                else {
                    Toast.makeText(getApplicationContext(),"Start Service",Toast.LENGTH_SHORT);
                    startService(gpsIntent);
                    gpsflag=true;
                }

            }
        });

        //Button at top right to view nearby events. Not needed at this point
        viewNearbyEvents = (Button) findViewById(R.id.view_nearby_events);

        viewNearbyEvents.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent viewEventsActivity = new Intent();
                        viewEventsActivity.setClass(getApplicationContext(),NearbyEventsActivity.class);
                        startActivity(viewEventsActivity);
                    }
                }
        );

        initContacts(); //Initialize contacts
        startContactListener(); //Start listening to contacts.

    }

    private void startContactListener(){
        isBroadcastingLocation = new HashMap<>();

        //Get the reference to the Firebase Realtime DB to know which contacts are broadcasting location
        contactNodeRef= Generic.database.getReference("Users/"+mUserID+"/Cts");

        //Contact listener intent initialized
        contactListner= new Intent("contact_listener");


        //Start the child listener. Note: In the realtime DB, whenever a contact is broadcasting location,
        //his/her phone number (UserID) attaches as a child to the current user's node's Cts branch.
        contactNodeChildListener=contactNodeRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Contact has started broadcasting location

              //  Update isBroadcastingLocation hashmap if contact is broadcasting location
                if(isBroadcastingLocation.containsKey(dataSnapshot.getKey()))
                {
                    isBroadcastingLocation.remove(dataSnapshot.getKey());
                }
                isBroadcastingLocation.put(dataSnapshot.getKey(),true);



                contactListner.putExtra(dataSnapshot.getKey(),true);
                sendBroadcast(contactListner);


                if(mContactLPTab!=null)
                {
//                    Contact LP Tab is already initialized. Then update the isContactBroadcastingLoc imageview there.
                    if(LPContactsTab.ContactListMap.containsKey(dataSnapshot.getKey()))
                    {
                        mContactLPTab.updateListAtPosition(LPContactsTab.ContactListMap.get(dataSnapshot.getKey()));
                    }
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                //Contact stopped broadcasting location

//                Toast.makeText(getApplicationContext(), dataSnapshot.getKey(),Toast.LENGTH_SHORT).show();
                //Update isBroadcastingLocation hashmap of contact
                if(isBroadcastingLocation.containsKey(dataSnapshot.getKey()))
                {
                    isBroadcastingLocation.remove(dataSnapshot.getKey());
                }
                isBroadcastingLocation.put(dataSnapshot.getKey(),false);

                contactListner.putExtra(dataSnapshot.getKey(),false);
                sendBroadcast(contactListner);

                if(mContactLPTab!=null)
                {
                    if(LPContactsTab.ContactListMap.containsKey(dataSnapshot.getKey()))
                    {

                        mContactLPTab.updateListAtPosition(LPContactsTab.ContactListMap.get(dataSnapshot.getKey()));

                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }




    private boolean initUserDetails()
    {

        SharedPreferences userDetails = getSharedPreferences("UserDetails",Context.MODE_PRIVATE);


        if(userDetails.getString("UserID","").equals(""))
            return false;

        mUserID = userDetails.getString("UserID","");
        mUserName=userDetails.getString("UserName","");

        Toast.makeText(getApplicationContext(),"Welcome "+mUserName+" "+mUserID+" "+userDetails.getString("Password",""),
                Toast.LENGTH_SHORT).show();





        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
        locationBroadcastReceiver=null;

        allEntities = new HashMap<>();
        allButtons = new HashMap<>();
        allContactNames = new HashMap<>();


        regFlag=initUserDetails();

        if(regFlag==false) {
            getDialogue();
        }
        else {
            loadLayout();
//            runtimePermissions();

            Intent gpsIntent = new Intent(getApplicationContext(), GPS_Service.class);  //Intent to GPS service class
            gpsflag=true;
            startServiceUsingAlarm();

                }
    }

 /*   private boolean runtimePermissions()
    {
        if(Build.VERSION.SDK_INT >=23 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);

            return true;
        }

        return false;
    }
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        else if(id == R.id.action_create_group)
        {
            Intent newGroupIntent= new Intent(LandingPageActivity.this, GroupCreateActivity.class);
            startActivity(newGroupIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private LPEventsTab eventLPTab;
        private LPGroupsTab groupLPTab;
        private LPContactsTab contactLPTab;
        private LPMapTab mapLPTab;


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a Landing Page (LP) Fragment (defined as classes).
            switch (position){
                case 0:
                    //return Common Map tab
                    if(mapLPTab==null)
                    {
                        mapLPTab = new LPMapTab();
                        mMapLPTab = mapLPTab;
                    }
                    return mapLPTab;
                case 1:
                    //Return events tab fragment
                    if(eventLPTab==null) {
                        eventLPTab = new LPEventsTab();
                        eventLPTab.passUserDetails(mUserID,mUserName);
                        mEventLPTab=eventLPTab;
                    }
                    return eventLPTab;
                case 2:
                    // Return groups tab fragment
                    if(groupLPTab==null) {
                        groupLPTab = new LPGroupsTab();
                        groupLPTab.passUserDetails(mUserID,mUserName);
                        mGroupLPTab=groupLPTab;
                    }
                    return groupLPTab;
                case 3:
                    //Return contacts tab fragment
                    if(contactLPTab==null) {
                        contactLPTab = new LPContactsTab();
                        contactLPTab.passUserDetails(mUserID,mUserName);
                        mContactLPTab=contactLPTab;
                    }
                    return contactLPTab;
            }

            return null;


        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "MAP";        //Page title of first Tab is Map
                case 1:
                    return "EVENTS";        //Page title of first Tab is Events
                case 2:
                    return "GROUPS";        //Page title of second Tab is Groups
                case 3:
                    return "CONTACTS";      //Page title of third Tab is Contacts
            }
            return null;
        }
    }

    public void initContacts()
    {
        /*
        Function to initialize contacts from firestore database
         */
        contacts = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        firestoneUserRef = firestore.collection("users").document(mUserID).collection("activities").document("contacts");

        firestoneUserRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if(e==null)
                {
                    Map<String,Object> userMap= new HashMap<>();
                    userMap = documentSnapshot.getData();

                    for(Map.Entry<String,Object> entry : userMap.entrySet())
                    {
                        Map<String,String> fContactDetails = (Map<String,String>) entry.getValue();
                        String fContactNumber= fContactDetails.get("ID");
                        String fContactName= fContactDetails.get("name");

                        if(!allEntities.containsKey(fContactNumber))
                        {
                            User user = new User(fContactName,fContactNumber);

                            if(mContactLPTab!=null)
                            {
                                allEntities.put(fContactNumber,mContactLPTab.getTotalContacts());
                                mContactLPTab.addContact(user);
                            }
                            else
                            {
                                contacts.add(user);
                                allContactNames.put(user.getNumber(),user.getName());
                                addProfilePic(user.getNumber());
                            }


                        }



                    }

                }
            }
        });
    }
}
