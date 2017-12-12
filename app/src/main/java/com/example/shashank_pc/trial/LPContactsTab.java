package com.example.shashank_pc.trial;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static android.os.Build.ID;
import static com.example.shashank_pc.trial.Generic.firestore;
import static com.example.shashank_pc.trial.GenericFunctions.addProfilePic;
import static com.example.shashank_pc.trial.LandingPageActivity.allContactNames;
import static com.example.shashank_pc.trial.LandingPageActivity.allEntities;
import static com.example.shashank_pc.trial.LandingPageActivity.contacts;
import static com.example.shashank_pc.trial.LandingPageActivity.userProfilePics;

/**
 * Created by shashank-pc on 8/22/2017.
 */

public class LPContactsTab extends Fragment {

    /*
    Tab that holds the list of contacts
     */
    //List is used because it is faster than Vector because it is asynchronous
    protected List<User> mContacts; //List of Contacts

    private View rootView;

    protected String mUserID;       //UserID
    protected String mUserName;     //UserName

    private LPContactListItemAdapter<User> arrayAdapter;        //ArrayAdapter to hold contacts
    protected ListView listView;        //Listview

    SharedPreferences preferences;

    protected boolean hasInitContacts;  //hasinitContacts flag to ensure that contacts are initialized only once


    //TODO Change position of this map from here to another class
    public static HashMap<String,Integer> ContactListMap;

    public void refresh()
    {

        if(arrayAdapter!=null) {
            arrayAdapter.notifyDataSetChanged();
        }

    }


    public void addContact(User user)
    {
        /*
        Add contact to Contact Listview
         */
        boolean mBroadcastLocationFlag;
        mBroadcastLocationFlag=preferences.getBoolean(user.getNumber(),false);
        user.initBroadcastLocationFlag(mBroadcastLocationFlag);
        mContacts.add(user);
        ContactListMap.put(user.getNumber(),mContacts.size()-1);
        if(!allContactNames.containsKey(user.getNumber()))
            allContactNames.put(user.getNumber(),user.getName());
        if(!userProfilePics.containsKey(user.getNumber()))
            addProfilePic(user.getNumber());
        refresh();
    }

    public int getTotalContacts()
    {
        return mContacts.size();
    }

    public void replaceContact(int index, User contact)
    {
        /*
        Function to replace contacts
         */
        contact.initBroadcastLocationFlag(mContacts.get(index).getBroadcastLocationFlag());
        mContacts.remove(index);
        mContacts.add(index,contact);
        refresh();
    }

    public void updateListAtPosition(int position)
    {
        /*
        Function needs to be made better. Right now, it refreshes whole listview irrespective of position.
        The original intention of this function is to only update the listview item at a given position
         */
        if(listView.getCount()>position)
        {
            //No segmentation fault because of wrong indexing

            View v = listView.getChildAt(position -
            listView.getFirstVisiblePosition());

            if(v==null)
                return;

            //TODO Pragmatic solution. Make better
            refresh();

//            Toast.makeText(getContext(),"Updated View",Toast.LENGTH_SHORT).show();
        }
    }



    public void passUserDetails(String userID, String userName)
    {
        /*
        Function to pass userdetails from parent activity to LPContactsTab
         */
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

        preferences = getContext().getSharedPreferences("LPLists", Context.MODE_PRIVATE);
        if(ContactListMap==null)
           ContactListMap = new HashMap<>();
        return rootView;
    }

    public void initArrayAdapter()
    {
        arrayAdapter = new LPContactListItemAdapter<>(getContext(), mContacts, mUserID);
        listView.setAdapter(arrayAdapter);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState)
    {
        /*
        Function that executes after view is created
         */



        super.onViewCreated(rootView, savedInstanceState);



        //Initialize Listview

        listView = (ListView) rootView.findViewById(R.id.section_list_contact);


        //Get the contacts from the database
        if (mContacts == null)
            mContacts = new ArrayList<>();


        //Populate listview with contacts

        initArrayAdapter();






        //Set intent to enable switching to new activity
        final Intent listClickActivity = new Intent();
        listClickActivity.setClass(getContext(), SingleContactActivity.class);

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


        if(hasInitContacts==false) {
            // If contact not yet initialized (Initialize groups)
            initUsers();
        }


    }

    public void initUsers()
    {
        hasInitContacts=true;

        if(contacts!=null)
        {
            for(User contact: contacts)
            {
                allEntities.put(contact.getNumber(),getTotalContacts());
                addContact(contact);
            }

            contacts.clear();
        }


    }
}
