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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static android.os.Build.ID;
import static com.example.shashank_pc.trial.Generic.firestore;

/**
 * Created by shashank-pc on 8/22/2017.
 */

public class LPContactsTab extends Fragment {

    //List is used because it is faster than Vector because it is asynchronous
    protected List<User> mContacts;

    private View rootView;

    protected String mUserID;
    protected String mUserName;

    private LPContactListItemAdapter<User> arrayAdapter;
    protected ListView listView;

    SharedPreferences preferences;

    protected boolean hasInitContacts;
    private DocumentReference firestoneUserRef;
    private String fContactName;
    private String fContactDesc;


    //TODO Change position of this map
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
        Add group
         */
        boolean mBroadcastLocationFlag;
        mBroadcastLocationFlag=preferences.getBoolean(user.getNumber(),false);
        user.initBroadcastLocationFlag(mBroadcastLocationFlag);
        mContacts.add(user);
        ContactListMap.put(user.getNumber(),mContacts.size()-1);
        refresh();
    }

    public void updateListAtPosition(int position)
    {
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

        firestore = FirebaseFirestore.getInstance();

        firestoneUserRef = firestore.collection("users").document(mUserID).collection("activities").document("contacts");

        firestoneUserRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Map<String,Object> userMap= new HashMap<>();
                userMap = documentSnapshot.getData();

                for(Map.Entry<String,Object> entry : userMap.entrySet())
                {
                    Map<String,String> fContactDetails = (Map<String,String>) entry.getValue();
                    String fContactNumber= fContactDetails.get("ID");
                    String fContactName= fContactDetails.get("name");
                    User user = new User(fContactName,fContactNumber);
                    addContact(user);


                }

            }
        });

    }
}
