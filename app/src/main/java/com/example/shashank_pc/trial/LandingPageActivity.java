package com.example.shashank_pc.trial;

import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private boolean regFlag=false;

    public void getDialogue(){

        //Pop dialogue

        AlertDialog.Builder mBuilder= new AlertDialog.Builder(this);
        View mView= getLayoutInflater().inflate(R.layout.dialog_register, null);
        final EditText mPhone= (EditText) mView.findViewById(R.id.etPhone);
        final EditText mPass= (EditText) mView.findViewById(R.id.etPass);
        final EditText mRtPass= (EditText) mView.findViewById(R.id.etRtPass);
        Button mRegisterButton = (Button) mView.findViewById(R.id.btnRegister);

        mBuilder.setView(mView);
        final AlertDialog dialog= mBuilder.create();
        dialog.show();

        


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

//                Toast.makeText(getApplicationContext(), mPass.getEditableText().toString() + " "+mRtPass.getText().toString(), Toast.LENGTH_SHORT).show();

                else if(mPassword.equals("")){
                    Toast.makeText(getApplicationContext(), "Enter a password", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), mPassword +mRetypePassword, Toast.LENGTH_SHORT).show();
                    if(!mPassword.equals(mRetypePassword))
                        Toast.makeText(getApplicationContext(), "Retype password", Toast.LENGTH_SHORT).show();
                    else {
                        regFlag=true;
                        dialog.dismiss();
                    }


                }

            }
        });



    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);


        getDialogue();

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }


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
                    if(eventLPTab==null)
                        eventLPTab= new LPEventsTab();
                    return eventLPTab;
                case 1:
                    // Return groups tab fragment
                    if(groupLPTab==null)
                        groupLPTab= new LPGroupsTab();
                    return groupLPTab;
                case 2:
                    //Return contacts tab fragment
                    if(contactLPTab==null)
                        contactLPTab= new LPContactsTab();
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
