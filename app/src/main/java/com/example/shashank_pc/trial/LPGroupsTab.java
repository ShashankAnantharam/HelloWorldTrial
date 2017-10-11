package com.example.shashank_pc.trial;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import static com.example.shashank_pc.trial.Generic.firestore;

/**
 * Created by shashank-pc on 8/22/2017.
 */

public class LPGroupsTab extends Fragment {

    //List is used because it is faster than Vector because it is asynchronous
    private List<Group> mGroups;

    private View rootView;

    private String mUserID;
    private String mUserName;

    private LPListItemAdapter<Group> arrayAdapter;

    SharedPreferences preferences;

    private DocumentReference firestoneUserRef;
    private String fGroupName;
    private String fGroupDesc;
    private boolean hasInitGroups;




    public void refresh()
    {
        if(arrayAdapter!=null) {
            arrayAdapter.notifyDataSetChanged();
        }

    }

    public void addGroup(Group group)
    {
        /*
        Add group
         */
        boolean mBroadcastLocationFlag;
        mBroadcastLocationFlag=preferences.getBoolean(group.getID(),false);
        group.initBroadcastLocationFlag(mBroadcastLocationFlag);
        mGroups.add(group);
        refresh();
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
        Inflate the Groups Tab View and return it.

         */

        if(rootView!=null){
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
                parent.removeView(rootView);
        }
        try {

            rootView = inflater.inflate(R.layout.tab_groups, container, false);
        }
        catch (InflateException e)
        {

        }

        preferences = getContext().getSharedPreferences("LPLists", Context.MODE_PRIVATE);
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
        listView= (ListView) rootView.findViewById(R.id.section_list_group);

        //Get the Groups from the database
        if(mGroups==null)
            mGroups=new ArrayList<>();

        //Populate listview with Groups
        arrayAdapter= new LPListItemAdapter<Group>(getContext(),
                mGroups, mUserID);
        listView.setAdapter(arrayAdapter);

        //Set intent to enable switching to new activity
        final Intent listClickActivity= new Intent();
        listClickActivity.setClass(getContext(),SingleEntityActivity.class);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Function that executes when listView item is clicked

                Toast.makeText(getContext(),"Clicked on Group",Toast.LENGTH_SHORT).show();
                Group group=(Group)listView.getItemAtPosition(position);
                String name = group.getName();
                String description= group.getDescription();
                String groupID= group.getID();
                boolean isGPSBroadcast= group.getBroadcastLocationFlag();

                //Pass Name, Description, type(Group) and IsGPSBroadcast flag to next activity
                listClickActivity.putExtra("Name",name);
                listClickActivity.putExtra("ID", groupID);
                listClickActivity.putExtra("Description",description);
                listClickActivity.putExtra("Type",'G');
                listClickActivity.putExtra("IsGPSBroadcast",isGPSBroadcast);
                listClickActivity.putExtra("Username",mUserName);
                listClickActivity.putExtra("UserID",mUserID);

                //TODO broadcase Group ID

                //Start new activity
                startActivity(listClickActivity);

            }
        });

        if(hasInitGroups==false) {
            // If group notyet initialized (Initialize groups)
            initGroups();
        }

    }
    
    public void initGroups()
    {
        hasInitGroups=true;

        firestore = FirebaseFirestore.getInstance();

        firestoneUserRef = firestore.collection("users").document(mUserID).collection("activities").document("groups");

        firestoneUserRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                Map<String,Object> userMap= new HashMap<>();
                userMap = documentSnapshot.getData();

                for(Map.Entry<String,Object> entry : userMap.entrySet())
                {
                    if(entry.getKey().equals("list"))
                    {
                        List<String> fGroups = (List) entry.getValue();

                        for(final String fGroupID: fGroups)
                        {
                            fGroupName="";
                            fGroupDesc="";
//                            Toast.makeText(getApplicationContext(),fGroupID,Toast.LENGTH_SHORT).show();

                            DocumentReference fireStoreGroupRef= firestore.collection("groups").document(fGroupID);

                            fireStoreGroupRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                    Map<String,Object> groupMap= new HashMap<>();
                                    groupMap = documentSnapshot.getData();

                                    for(Map.Entry<String,Object> entry:groupMap.entrySet())
                                    {
                                        if(entry.getKey().equals("name"))
                                            fGroupName=(String)entry.getValue();
                                        else if(entry.getKey().equals("desc"))
                                            fGroupDesc=(String) entry.getValue();
                                    }

                                    Group group= new Group(fGroupName,fGroupDesc,fGroupID);
                                    addGroup(group);


                                }
                            });


                        }

                    }

                }
            }
        });




    }


}
