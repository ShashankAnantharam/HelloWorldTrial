package com.example.shashank_pc.trial;

import android.content.Context;

import java.util.List;

/**
 * Created by shashank-pc on 10/24/2017.
 */

public class SEEventListArrayAdapter<T> extends LPListItemAdapter<T> {

    public SEEventListArrayAdapter(Context context, List<T> rowItems, String userID)
    {
        super(context,rowItems,userID);
    }
}
