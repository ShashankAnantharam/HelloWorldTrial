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
        User temp;
        if(SingleEntityActivity.mType=='U')
        {
            temp= new User(SingleEntityActivity.mName, "55554");
            mAllMembers.add(temp);
            return mAllMembers;
        }


        temp = new User("Shashank Anantharam", LandingPageActivity.mUserID);    //TODO parameterize name
        temp.setBroadcastLocationFlag(false);
        mAllMembers.add(temp);
        temp= new User("Bharath Kota","55554");
        temp.setBroadcastLocationFlag(false);
        mAllMembers.add(temp);
        temp = new User("Mehtab Ahmed", "55554");
        mAllMembers.add(temp);
        temp = new User("Muthimon", "55554");
        temp.setBroadcastLocationFlag(true);
        mAllMembers.add(temp);

        return mAllMembers;
    }
}
