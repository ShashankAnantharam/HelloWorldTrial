package com.example.shashank_pc.trial;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static com.example.shashank_pc.trial.LandingPageActivity.isBroadcastingLocation;

/**
 * Created by shashank-pc on 9/25/2017.
 */

public class LPContactListItemAdapter<T> extends BaseAdapter {

    Context context;
    List<T> rowItems;
    String userID;
    private SharedPreferences contactDisplay;

    public LPContactListItemAdapter(Context context, List<T> rowItems, String userID)
    {
        this.context=context;
        this.rowItems=rowItems;
        this.userID=userID;

    }

    @Override
    public int getCount()
    {
        return rowItems.size();
    }

    @Override
    public Object getItem(int position)
    {
        return rowItems.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return rowItems.indexOf(getItem(position));
    }


    private class ViewHolder{
        TextView main_text;
        TextView subtitle;
        Button locationBroadcastFlag;
        ImageView isContactBroadcastingFlag;
        Button visibilityFlag;

    }






    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder=null;


        LayoutInflater mInflater= (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        holder = new ViewHolder();



        if(convertView==null)
        {
            convertView=mInflater.inflate(R.layout.main_lp_contact_view_item, null);

            holder.main_text= (TextView) convertView.findViewById(R.id.main_lp_contact_text);
            holder.subtitle= (TextView) convertView.findViewById(R.id.main_lp_contact_subtitle);
            holder.locationBroadcastFlag= (Button) convertView.findViewById(R.id.contact_gps_broadcast_flag);
            holder.isContactBroadcastingFlag = (ImageView) convertView.findViewById(R.id.is_contact_broadcasting_gps);
            holder.visibilityFlag = (Button) convertView.findViewById(R.id.contact_display_flag);

            convertView.setTag(holder);

        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }


        final T rowItem= rowItems.get(position);

        String main_text;
        String subtitle;
        boolean buttons;
        boolean isContactBroadcastingLocationFlag=false;
        boolean visibilityFlag=false;


        if(rowItem instanceof User)
        {
            main_text=((User) rowItem).getName();
            subtitle=((User) rowItem).getLastChatMessage();

            if(isBroadcastingLocation.containsKey(((User) rowItem).getNumber()) &&
                    isBroadcastingLocation.get(((User) rowItem).getNumber())==true)
            {
                isContactBroadcastingLocationFlag=true;
            }

            //Getting display flag
            contactDisplay = context.getSharedPreferences("DisplayFlags",Context.MODE_PRIVATE);
            visibilityFlag = contactDisplay.getBoolean(((User) rowItem).getNumber(),
                    false);


        }
        else
        {
            main_text="NA";
            subtitle="";

        }

        holder.main_text.setText(main_text);            //Set title of list view item
        holder.subtitle.setText(subtitle);    //Set subtitle of list view item

        if(visibilityFlag)
        {
            //Set contact visibility button
            holder.visibilityFlag.setBackground(context.getDrawable(R.drawable.entity_visible));
        }
        else
        {
            //reset contact visibility button
            holder.visibilityFlag.setBackground(context.getDrawable(R.drawable.entity_invisible));

        }


        holder.visibilityFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(rowItem instanceof User)
                {
                    SharedPreferences.Editor edit= contactDisplay.edit();
                    String ID = ((User) rowItem).getNumber();
                    boolean curr_flag=contactDisplay.getBoolean(ID,false);
                    if(!curr_flag)
                    {
                        //Set contact visibility button
                        v.setBackground(context.getDrawable(R.drawable.entity_visible));
                        edit.putBoolean(ID,true);
                        edit.commit();
                    }
                    else
                    {
                        //reset contact visibility button
                        v.setBackground(context.getDrawable(R.drawable.entity_invisible));
                        edit.putBoolean(ID,false);
                        edit.commit();
                    }

                }
            }
        });


        if(getflagstatus(rowItem)==true)
        {

            holder.locationBroadcastFlag.setBackground(convertView.getResources().getDrawable(R.drawable.lp_list_button_blue));

        }
        else {

            holder.locationBroadcastFlag.setBackground(convertView.getResources().getDrawable(R.drawable.lp_list_button_black));

        }

        if(isContactBroadcastingLocationFlag)
        {
            //Contact is broadcasting GPS
            holder.isContactBroadcastingFlag.setImageDrawable(
                    convertView.getResources().getDrawable(R.drawable.contact_loc_broadcasting_on)
            );
        }
        else
        {
            //Contact is not broadcasting GPS
            holder.isContactBroadcastingFlag.setImageDrawable(
                    convertView.getResources().getDrawable(R.drawable.contact_loc_broadcasting_off)
            );
        }


        holder.locationBroadcastFlag.setOnClickListener(new View.OnClickListener() {
            boolean buttonClickFlag;

            @Override
            public void onClick(View v) {
                buttonClickFlag=getflagstatus(rowItem);

                if(buttonClickFlag==false)
                {
                    buttonClickFlag=true;
                    v.setBackground(v.getResources().getDrawable(R.drawable.lp_list_button_blue));

                }
                else
                {
                    v.setBackground(v.getResources().getDrawable(R.drawable.lp_list_button_black));
                    buttonClickFlag=false;
                }
                locationBroadcastButtonOnClickActivity(rowItem, buttonClickFlag,v);

            }
        });


        return convertView;

    }






    public boolean getflagstatus(T rowItem)
    {
        SharedPreferences preferences = context.getSharedPreferences("LPLists", Context.MODE_PRIVATE);
        String mEntityID="";
        if(rowItem instanceof User)
            mEntityID=((User) rowItem).getNumber();

        boolean flagStatus=preferences.getBoolean(mEntityID,false);

        return flagStatus;
    }
    public void locationBroadcastButtonOnClickActivity(T rowItem, boolean mLBflag, View convertView)
    {
        SharedPreferences preferences = context.getSharedPreferences("LPLists",Context.MODE_PRIVATE);
        SharedPreferences.Editor edit= preferences.edit();


        if(rowItem instanceof User) {
            ((User) rowItem).setBroadcastLocationFlag(mLBflag, userID);
            edit.putBoolean(((User) rowItem).getNumber(),mLBflag);
            edit.commit();
        }







        if(mLBflag==true)
            Toast.makeText(convertView.getContext(), "GPS broadcasting ON", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(convertView.getContext(), "GPS broadcasting OFF", Toast.LENGTH_SHORT).show();


    }

}
