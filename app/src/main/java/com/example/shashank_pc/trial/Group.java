package com.example.shashank_pc.trial;

/**
 * Created by shashank-pc on 8/22/2017.
 */

public class Group extends Generic {

    public Group(String name, String description)
    {
        super(name, description);
    }

    public Group(String name, String description, String ID){super(name, description, ID);}


    public Group(String name)
    {
        super(name,"");
    }

    @Override
    public String toString() {
        return super.toString();
    }
}