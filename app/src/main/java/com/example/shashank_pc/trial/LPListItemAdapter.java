package com.example.shashank_pc.trial;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.DragEvent;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.util.List;

import static com.example.shashank_pc.trial.R.id.container;
import static java.security.AccessController.getContext;

/**
 * Created by shashank-pc on 8/23/2017.
 */

public class LPListItemAdapter<T> extends BaseAdapter{

    Context context;
    List<T> rowItems;   //Rowitems (Event or Group)
    String userID;  //UserID

    public void settextColorListener(ViewHolder viewHolder, View convertView, T rowItem)
    {
            //Function overridden for Events inside group functionality later on. Not needed for now!
    }

    public LPListItemAdapter(Context context, List<T> rowItems, String userID)
    {
        //Constructor
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


    protected class ViewHolder{
        TextView main_text; //Textview to hold the name of the entity (eg.Family, Friends group, etc.)
        TextView subtitle;  //Textview to hold the subtitle of the entity
        Button locationBroadcastFlag;   //Button to hold the location broadcast flag and toggle the locationbraodcast flag

    }







    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder=null;


        LayoutInflater mInflater= (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        holder = new ViewHolder();



        if(convertView==null)
        {
            convertView=mInflater.inflate(R.layout.main_lp_view_item, null);

            //Initialize the objects
            holder.main_text= (TextView) convertView.findViewById(R.id.main_lp_text);
            holder.subtitle= (TextView) convertView.findViewById(R.id.main_lp_subtitle);
            holder.locationBroadcastFlag= (Button) convertView.findViewById(R.id.gps_broadcast_flag);

            convertView.setTag(holder);

        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }


        final T rowItem= rowItems.get(position);

        String main_text;
        String subtitle;
        boolean buttons;

        if(rowItem instanceof Event)    //If Row Item is an Event
        {
            main_text= ((Event) rowItem).getName();
            subtitle=  ((Event) rowItem).getDescription();

        }
        else if(rowItem instanceof Group)   //If Row Item is a group
        {
            main_text= ((Group) rowItem).getName();
            subtitle= ((Group) rowItem).getDescription();
        }
        else if(rowItem instanceof User)    //IF Row item is a contact (Not needed now because it is a part of LPContactListItemAdapter
        {
            main_text=((User) rowItem).getName();
            subtitle=((User) rowItem).getLastChatMessage();


        }
        else
        {
            main_text="NA";
            subtitle="";
        }

        holder.main_text.setText(main_text);            //Set title of list view item
        holder.subtitle.setText(subtitle);    //Set subtitle of list view item




        if(getflagstatus(rowItem)==true)    //If user's location is being broadcast to the entity
        {
            //set color blue

            holder.locationBroadcastFlag.setBackground(convertView.getResources().getDrawable(R.drawable.lp_list_button_blue));

        }
        else {

            //set color black

            holder.locationBroadcastFlag.setBackground(convertView.getResources().getDrawable(R.drawable.lp_list_button_black));

        }


        //Set onClickListener for locationBroadcastFlag button
        holder.locationBroadcastFlag.setOnClickListener(new View.OnClickListener() {
            boolean buttonClickFlag;

            @Override
            public void onClick(View v) {
                buttonClickFlag=getflagstatus(rowItem);

                //Toggle the locationBroadcastFlag on click
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


        settextColorListener(holder,convertView,rowItem);

    return convertView;

    }



    public boolean getflagstatus(T rowItem)
    {
        /*
        Function to get the location broadcast flag status of rowitem
         */
        SharedPreferences preferences = context.getSharedPreferences("LPLists", Context.MODE_PRIVATE);
        String mEntityID="";

        if(rowItem instanceof Event)
            mEntityID=((Event) rowItem).getID();
        else if(rowItem instanceof  Group)
            mEntityID=((Group) rowItem).getID();
        else if(rowItem instanceof User)
            mEntityID=((User) rowItem).getNumber();

        boolean flagStatus=preferences.getBoolean(mEntityID,false);

        return flagStatus;
    }
    public void locationBroadcastButtonOnClickActivity(T rowItem, boolean mLBflag, View convertView)
    {
        /*
        Function to start/Stop broadcasting location based on the button click
         */
        SharedPreferences preferences = context.getSharedPreferences("LPLists",Context.MODE_PRIVATE);
        SharedPreferences.Editor edit= preferences.edit();


        if(rowItem instanceof Event)    //If Row Item is an Event
        {
            ((Event) rowItem).setBroadcastLocationFlag(mLBflag, userID);
            edit.putBoolean(((Event) rowItem).getID(),mLBflag);
            edit.commit();
        }
        else if(rowItem instanceof Group) {     //If RowItem is a group

            //Set the broadcast flag (in the Firebase reatime DB). Function is present in Generic
            ((Group) rowItem).setBroadcastLocationFlag(mLBflag, userID);
            //Set the new flag in sharedPreferences
            edit.putBoolean(((Group) rowItem).getID(),mLBflag);
            edit.commit();

        }
        else if(rowItem instanceof User) {  //If Row Item is a contact (Not needed as it is moved to LPContactListItemAdapter
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
