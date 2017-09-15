package com.example.shashank_pc.trial;

/**
 * Created by shashank-pc on 8/22/2017.
 */


public class Generic {

    protected String mName;     //Max only 25 characters allowed for name
    protected String mID;
    protected String mDescription;
    protected boolean mBroadcastLocation;


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

    public void setBroadcastLocationFlag(boolean BroadcastLocation){this.mBroadcastLocation=BroadcastLocation;}

    public boolean getBroadcastLocationFlag(){return mBroadcastLocation;}


}
