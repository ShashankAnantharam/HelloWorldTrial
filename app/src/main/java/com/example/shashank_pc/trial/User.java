package com.example.shashank_pc.trial;

import com.google.firebase.database.DatabaseReference;

import static com.example.shashank_pc.trial.LandingPageActivity.allButtons;

/**
 * Created by shashank-pc on 8/22/2017.
 */

public class User{
    protected String mName;         //Max only 25 characters allowed for name

    protected String mNumber;
    protected String mlastMessage;
    protected boolean mBroadcastLocation;

    public User(String name, String number)
    {
        mName=name;
        mNumber=number;
        mlastMessage="Last Chat Message";
        mBroadcastLocation=false;
    }

    @Override
    public String toString() {
        return mName;
    }

    public String getName(){return mName;}

    public String getNumber(){return mNumber;}

    public String getLastChatMessage() {return mlastMessage;}

    public void setBroadcastLocationFlag(boolean BroadcastLocation, String userID){

        this.mBroadcastLocation=BroadcastLocation;

        String address="Users/"+mNumber+"/Cts";

        DatabaseReference ref = Generic.database.getReference(address); //go to that reference location

        String contactRef="C"+mNumber;
        allButtons.put(contactRef,BroadcastLocation);
        if(!BroadcastLocation)
        {
            ref.child(userID).removeValue();
        }

    }

    public void setName(String name)
    {
        mName=name;
    }

    public void initBroadcastLocationFlag(boolean BroadcastLocation){this.mBroadcastLocation=BroadcastLocation;}

    public boolean getBroadcastLocationFlag(){return mBroadcastLocation;}
}
