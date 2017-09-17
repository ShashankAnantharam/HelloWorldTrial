package com.example.shashank_pc.trial;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.app.ListFragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by shashank-pc on 8/22/2017.
 */

public class LPContactsTab extends Fragment {

    //List is used because it is faster than Vector because it is asynchronous
    List<User> mContacts;

    private View rootView;

    protected String mUserID;
    protected String mUserName;



    public void passUserDetails(String userID, String userName)
    {
        mUserID=userID;
        mUserName=userName;
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        /*
        Inflate the Contact Tab View and return it.

         */
        if(rootView!=null){
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
                parent.removeView(rootView);
        }
        try {

            rootView = inflater.inflate(R.layout.tab_contacts, container, false);
        }
        catch (InflateException e)
        {

        }



        return rootView;
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState)
    {
        /*
        Function that executes after view is created
         */



        super.onViewCreated(rootView, savedInstanceState);

        //Initialize Listview
        final ListView listView;
        listView = (ListView) rootView.findViewById(R.id.section_list_contact);


        //Get the contacts from the database
        if (mContacts == null)
            mContacts = getAllUsers();


        //Populate listview with contacts
        LPListItemAdapter<User> arrayAdapter = new LPListItemAdapter<>(getContext(), mContacts);
        listView.setAdapter(arrayAdapter);


        //Set intent to enable switching to new activity
        final Intent listClickActivity = new Intent();
        listClickActivity.setClass(getContext(), SingleEntityActivity.class);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    //Function that executes when listView item is clicked

                    Toast.makeText(getContext(), "Clicked on Contact", Toast.LENGTH_SHORT).show();
                    User user = (User) listView.getItemAtPosition(position);
                    String name = user.getName();
                    String number = user.getNumber();
                    String description = user.getLastChatMessage();
                    boolean isGPSBroadcast = user.getBroadcastLocationFlag();

                    //Pass Name, Description, type(User) and IsGPSBroadcast flag to next activity
                    listClickActivity.putExtra("Name", name);
                    listClickActivity.putExtra("ID", number);
                    listClickActivity.putExtra("Description", description);
                    listClickActivity.putExtra("Type", 'U');
                    listClickActivity.putExtra("IsGPSBroadcast", isGPSBroadcast);
                    listClickActivity.putExtra("Username",mUserName);
                    listClickActivity.putExtra("UserID",mUserID);

                    //Start new activity
                    startActivity(listClickActivity);

                }
            });




    }

    public List<User> getAllUsers()
    {
        //TODO Change function in order to get contacts from Database
        /*
        Function to get All contacts from the Database. HardCoded now
         */

        List <User> mAllContacts = new ArrayList<>();

        if(mUserID=="")
            return mAllContacts;

        User temp= new User(mUserName,mUserID);
        temp.setBroadcastLocationFlag(false);
        mAllContacts.add(temp);
        temp = new User("Shashank A","9701420818");
        mAllContacts.add(temp);
        temp = new User("Bharat Kota", "9177787179");
        mAllContacts.add(temp);
        temp = new User("Mehtab Ahmed", "9177787327");
        temp.setBroadcastLocationFlag(false);
        temp= new User("Phani", "9494426683");
        mAllContacts.add(temp);
 //       temp = new User("Abhinav", "9000377713");
//        mAllContacts.add(temp);
 //       temp = new User("Vishal", "9989182838");
 //       mAllContacts.add(temp);


        return mAllContacts;
    }
}
