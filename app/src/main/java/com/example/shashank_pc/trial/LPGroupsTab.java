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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by shashank-pc on 8/22/2017.
 */

public class LPGroupsTab extends Fragment {

    //List is used because it is faster than Vector because it is asynchronous
    List<Group> mGroups;

    private View rootView;

    private String mUserID;
    private String mUserName;


    SharedPreferences preferences;




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
            mGroups=getAllGroups();

        //Populate listview with Groups
        LPListItemAdapter<Group> arrayAdapter= new LPListItemAdapter<Group>(getContext(),
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


    }

    public List<Group> getAllGroups()
    {

        //TODO Change function in order to get Groups from Database
        /*
        Function to get All groups from the Database. HardCoded now
         */

        List <Group> mAllGroups = new ArrayList<>();

        if(mUserID=="")
            return mAllGroups;



        Group temp;
        boolean mBroadcastLocationFlag;
        temp = new Group("Founders","We are the founders of the app","G00000000002");
        mBroadcastLocationFlag=preferences.getBoolean("G00000000002",false);
        temp.initBroadcastLocationFlag(mBroadcastLocationFlag);
        mAllGroups.add(temp);
        temp = new Group("Family","Plot No 7, Road No 49", "G00000000001");
        mBroadcastLocationFlag=preferences.getBoolean("G00000000001",false);
        temp.initBroadcastLocationFlag(mBroadcastLocationFlag);
        mAllGroups.add(temp);

        return mAllGroups;
    }
}
