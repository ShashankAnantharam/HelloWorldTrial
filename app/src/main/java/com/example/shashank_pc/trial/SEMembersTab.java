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
    public void initUsers()
    {

    }
}
