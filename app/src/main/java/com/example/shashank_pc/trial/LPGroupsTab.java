package com.example.shashank_pc.trial;

import android.content.Intent;
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
                mGroups);
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
                boolean isGPSBroadcast= group.getBroadcastLocationFlag();

                //Pass Name, Description, type(Group) and IsGPSBroadcast flag to next activity
                listClickActivity.putExtra("Name",name);
                listClickActivity.putExtra("Description",description);
                listClickActivity.putExtra("Type",'G');
                listClickActivity.putExtra("IsGPSBroadcast",isGPSBroadcast);

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
        for(int i=0;i<18;i++)
        {
            Group temp= new Group("Group","Group_desc");
            mAllGroups.add(temp);
        }
        return mAllGroups;
    }
}
