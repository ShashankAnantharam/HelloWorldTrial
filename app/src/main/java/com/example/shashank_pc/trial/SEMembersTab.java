package com.example.shashank_pc.trial;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shashank-pc on 8/26/2017.
 */

public class SEMembersTab  extends LPContactsTab{


    @Override
    public List<User> getAllUsers() {
        List <User> mAllMembers = new ArrayList<>();

        User temp= new User("AkashKant","55554");
        temp.setBroadcastLocationFlag(false);
        mAllMembers.add(temp);
        temp = new User("Mohit Bajaj", "55554");
        mAllMembers.add(temp);
        temp = new User("Abhishek Gupta", "55554");
        temp.setBroadcastLocationFlag(true);
        mAllMembers.add(temp);
        temp = new User("Muthimon", "55554");
        temp.setBroadcastLocationFlag(true);
        mAllMembers.add(temp);

        return mAllMembers;
    }
}
