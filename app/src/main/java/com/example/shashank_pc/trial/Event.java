package com.example.shashank_pc.trial;

/**
 * Created by shashank-pc on 8/22/2017.
 */

public class Event extends Generic {

    private int ID;
    private static int idGenerator=0;
    public Event(String name, String description, String eID)
    {
        super(name, description, eID);
        idGenerator++;
        ID=idGenerator;
    }
    public Event(String name)
    {
        super(name,"");
    }

    @Override
    public String toString() {
        return super.toString();
    }


}
