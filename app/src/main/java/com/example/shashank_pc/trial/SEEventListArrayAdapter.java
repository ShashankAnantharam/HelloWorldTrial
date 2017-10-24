package com.example.shashank_pc.trial;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import java.util.List;

import static com.example.shashank_pc.trial.GenericFunctions.secondaryEvents;
import static com.example.shashank_pc.trial.SingleEntityActivity.secondaryEventsClickFlag;

/**
 * Created by shashank-pc on 10/24/2017.
 */

public class SEEventListArrayAdapter<T> extends LPListItemAdapter<T> {


    @Override
    public void settextColorListener(ViewHolder viewHolder, View convertView, T rowItem)
    {
        if(rowItem instanceof Event) {
            if(secondaryEventsClickFlag!=null && secondaryEventsClickFlag.containsKey(((Event) rowItem).getID())
                    && secondaryEventsClickFlag.get( ((Event) rowItem).getID()))
            {
                viewHolder.main_text.setTextColor(Color.BLUE);

            }
            else
            {
                viewHolder.main_text.setTextColor(Color.BLACK);
            }
        }

    }

    public SEEventListArrayAdapter(Context context, List<T> rowItems, String userID)
    {
        super(context,rowItems,userID);
    }
}
