package com.example.shashank_pc.trial;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Created by shashank-pc on 8/22/2017.
 */


public class Generic {

    protected String mName;     //Max only 25 characters allowed for name
    protected String mID;
    protected String mDescription;
    protected boolean mBroadcastLocation;

    public static FirebaseDatabase database;
    public static FirebaseFirestore firestore;


    public Generic(String name, String description)
    {
        mName=name;
        mDescription=description;
        mBroadcastLocation=false;
    }

    public Generic(String name, String description, String ID)
    {
        this(name,description);
        mID=ID;
    }

    public String getName(){return mName;}

    public String getDescription(){return mDescription;}

    public String getID(){return mID;}

    @Override
    public String toString() {
        return mName+ " : " +mDescription;
    }

    public void initBroadcastLocationFlag(boolean BroadcastLocation)
    {this.mBroadcastLocation = BroadcastLocation;}


    public void setBroadcastLocationFlag(boolean BroadcastLocation, String userID){

        this.mBroadcastLocation=BroadcastLocation;

        /*
        Start Firebase Code here
         */

        String address;
        if(mID.charAt(0)=='G')
            address="Groups/";
        else
            address="Events/";

        address+=mID;

        DatabaseReference ref = database.getReference(address); //go to that reference location

        if(BroadcastLocation)
        {
                ref.child("Mem").child(userID).setValue("1");
            /*
            If Button is Clicked, then attach UserID string as key, "" as value to the node mID
          Groups
            |
            |
            |_ mID
               |
               |
               |__ UserID: ""

            https://firebase.google.com/docs/database/android/read-and-write (Add user function to be used to add user to group node)
             */
        }
        else
        {
            ref.child("Mem").child(userID).setValue("0");            /*
            If Button is not clicked, then remove child UserID from node

            Groups
              |
              |
              |
              |__mID


            Use ref.child(key).remove(); to remove nodes
             */
        }


        /*
        Stop Firebase code here
         */

    }

    public boolean getBroadcastLocationFlag(){return mBroadcastLocation;}


}
