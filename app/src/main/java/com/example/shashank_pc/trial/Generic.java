package com.example.shashank_pc.trial;

/**
 * Created by shashank-pc on 8/22/2017.
 */


public class Generic {

    protected String mName;
    protected String mDescription;
    protected boolean mBroadcastLocation;


    public Generic(String name, String description)
    {
        mName=name;
        mDescription=description;
        mBroadcastLocation=false;
    }

    public String getName(){return mName;}

    public String getDescription(){return mDescription;}

    @Override
    public String toString() {
        return mName+ " : " +mDescription;
    }

    public void setBroadcastLocationFlag(boolean BroadcastLocation){this.mBroadcastLocation=BroadcastLocation;}

    public boolean getBroadcastLocationFlag(){return mBroadcastLocation;}


}
