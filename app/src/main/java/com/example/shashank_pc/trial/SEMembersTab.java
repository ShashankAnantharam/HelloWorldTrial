package com.example.shashank_pc.trial;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shashank-pc on 8/26/2017.
 */

public class SEMembersTab  extends LPContactsTab{



    private char mType;
    private String mContactName;
    private String mContactID;

    public void passUserDetails(String userID, String userName, String name, String contactID, char type)
    {
        super.passUserDetails(userID,userName);
        mContactName=name;
        mContactID=contactID;
        mType=type;
    }

    @Override
    public List<User> getAllUsers() {
        List <User> mAllMembers = new ArrayList<>();
        User temp;

 // For debugging       Toast.makeText(getContext(),mContactID,Toast.LENGTH_SHORT).show();

        if(mType=='U')
        {
            temp= new User(mContactName, mContactID);
            mAllMembers.add(temp);
            return mAllMembers;
        }


        temp = new User(mUserName, mUserID);    //TODO parameterize name
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
