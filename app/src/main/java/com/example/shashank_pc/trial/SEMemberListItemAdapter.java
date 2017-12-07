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


import static com.example.shashank_pc.trial.SingleEntityActivity.isMemberBroadcastingLocation;

/**
 * Created by shashank-pc on 10/9/2017.
 */

public class SEMemberListItemAdapter<T> extends BaseAdapter {

    Context context;
    List<T> rowItems;
    String userID;

    public SEMemberListItemAdapter(Context context, List<T> rowItems, String userID)
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
        //Class to hold the member item
        TextView main_text;
        TextView subtitle;
        ImageView isMemberBroadcastingFlag;

    }






    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        //Initialize the MemberListItem
        ViewHolder holder=null;


        LayoutInflater mInflater= (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        holder = new ViewHolder();



        if(convertView==null)
        {
            convertView=mInflater.inflate(R.layout.single_entity_member_view_item, null);

            holder.main_text= (TextView) convertView.findViewById(R.id.se_member_text);
            holder.subtitle= (TextView) convertView.findViewById(R.id.se_member_subtitle);
            holder.isMemberBroadcastingFlag = (ImageView) convertView.findViewById(R.id.is_member_broadcasting_gps);

            convertView.setTag(holder);

        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }


        //Get the rowItem based on position
        final T rowItem= rowItems.get(position);

        String main_text;
        String subtitle;
        boolean isMemberBroadcastingLocationFlag=false;

        if(rowItem instanceof User)
        {
            //If row item is user, get the name of user, the chat message and whether he/she is broadcasting
            main_text=((User) rowItem).getName();
            subtitle=((User) rowItem).getLastChatMessage();

            if(isMemberBroadcastingLocation.containsKey(((User) rowItem).getNumber()) &&
                    isMemberBroadcastingLocation.get(((User) rowItem).getNumber())==true)
            {
                isMemberBroadcastingLocationFlag=true;
            }

        }
        else
        {
            main_text="NA";
            subtitle="";
        }

        holder.main_text.setText(main_text);            //Set Name of member list view item
        holder.subtitle.setText(subtitle);    //Set subtitle of member list view item




        if(isMemberBroadcastingLocationFlag)
        {
            //if Member is broadcasting GPS
            holder.isMemberBroadcastingFlag.setImageDrawable(
                    convertView.getResources().getDrawable(R.drawable.contact_loc_broadcasting_on)
            );
        }
        else
        {
            //Member is not broadcasting GPS
            holder.isMemberBroadcastingFlag.setImageDrawable(
                    convertView.getResources().getDrawable(R.drawable.contact_loc_broadcasting_off)
            );
        }



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
