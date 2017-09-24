package com.example.shashank_pc.trial;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    private boolean regFlag=false;


    private String mUserID="";

    private String mUserName="";

    private BroadcastReceiver locationBroadcastReceiver;

    private boolean gpsflag;


    private DatabaseReference contactNodeRef;
    private ChildEventListener contactNodeChildListener;


    private DatabaseReference writeGPSLat;
    private DatabaseReference writeGPSLong;

    @Override
    protected void onResume() {
        super.onResume();




 /*
 Receive and listen to the location from GPS_Service
  */
        if(locationBroadcastReceiver==null)
        {

            locationBroadcastReceiver= new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    double latitude= intent.getDoubleExtra("Latitude",0);

                    writeGPSLat.setValue(latitude);

                    double longitude= intent.getDoubleExtra("Longitude",0);
                    writeGPSLong.setValue(longitude);

  //                  Toast.makeText(getApplicationContext(),latitude+" "+longitude,Toast.LENGTH_SHORT).show();


                }
            };

        }
        registerReceiver(locationBroadcastReceiver,new IntentFilter("location_update"));

    }

    @Override
    protected void onDestroy() {
        /*
        Stop broadcast receiver and GPS Service once app is closed
         */
        super.onDestroy();
        if(locationBroadcastReceiver!=null)
        {
            unregisterReceiver(locationBroadcastReceiver);
        }
        Intent gpsIntent = new Intent(getApplicationContext(), GPS_Service.class);
        stopService(gpsIntent);
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

    public void getDialogue(){

        //Pop dialogue

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
                if(regFlag==false)
                    dialog.show();
                else {
                    loadLayout();
                    Intent gpsIntent = new Intent(getApplicationContext(), GPS_Service.class);     //Intent to gps service class
                    startService(gpsIntent);
                    gpsflag=true;
                }
            }

        });


        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

        Generic.database = FirebaseDatabase.getInstance();

        writeGPSLat= Generic.database.getReference("Users/"+mUserID+"/Loc/Lat");
        writeGPSLong= Generic.database.getReference("Users/"+mUserID+"/Loc/Long");

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

        startContactListener();

    }

    private void startContactListener(){
        contactNodeRef= Generic.database.getReference("Users/"+mUserID+"/Cts");

        final Intent contactListner= new Intent("contact_listener");



        contactNodeChildListener=contactNodeRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
               // Toast.makeText(getApplicationContext(), dataSnapshot.getKey(),Toast.LENGTH_SHORT).show();
                contactListner.putExtra(dataSnapshot.getKey(),true);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

//                Toast.makeText(getApplicationContext(), dataSnapshot.getKey(),Toast.LENGTH_SHORT).show();
                contactListner.putExtra(dataSnapshot.getKey(),false);
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


        if(userDetails.getString("UserID","")=="")
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



        regFlag=initUserDetails();

        if(regFlag==false) {
            getDialogue();
        }
        else {
            loadLayout();
//            runtimePermissions();

            Intent gpsIntent = new Intent(getApplicationContext(), GPS_Service.class);  //Intent to GPS service class
            gpsflag=true;
            startService(gpsIntent);

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

        LPEventsTab eventLPTab;
        LPGroupsTab groupLPTab;
        LPContactsTab contactLPTab;


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a Landing Page (LP) Fragment (defined as classes).
            switch (position){
                case 0:
                    //Return events tab fragment
                    if(eventLPTab==null) {
                        eventLPTab = new LPEventsTab();
                        eventLPTab.passUserDetails(mUserID,mUserName);
                    }
                    return eventLPTab;
                case 1:
                    // Return groups tab fragment
                    if(groupLPTab==null) {
                        groupLPTab = new LPGroupsTab();
                        groupLPTab.passUserDetails(mUserID,mUserName);
                    }
                    return groupLPTab;
                case 2:
                    //Return contacts tab fragment
                    if(contactLPTab==null) {
                        contactLPTab = new LPContactsTab();
                        contactLPTab.passUserDetails(mUserID,mUserName);
                    }
                    return contactLPTab;
            }

            return null;


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
                    return "EVENTS";        //Page title of first Tab is Events
                case 1:
                    return "GROUPS";        //Page title of second Tab is Groups
                case 2:
                    return "CONTACTS";      //Page title of third Tab is Contacts
            }
            return null;
        }
    }
}
