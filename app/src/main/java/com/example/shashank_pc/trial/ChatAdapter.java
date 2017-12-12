package com.example.shashank_pc.trial;

import android.content.Context;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shashank-pc on 8/31/2017.
 */


public class ChatAdapter extends ArrayAdapter<ChatMessage> {

    /*
    ChatAdapter is the arrayadapter to store chat messages
     */
    private List<ChatMessage> mChatList = new ArrayList<>();    //ArrayList containing ChatMessages

    Context mContext;

    public ChatAdapter(Context context, int resource)
    {
        super(context,resource);
        mContext=context;
    }

    @Override
    public void add(@Nullable ChatMessage object) {
        mChatList.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return mChatList.size();
    }

    @Nullable
    @Override
    public ChatMessage getItem(int position) {
        return mChatList.get(position);
    }


    private class ViewHolder{

        private TextView chatText;      //TextView to hold the chat message
        private TextView creator;       //Text View to hold the mesage creator name
        //Layouts of the entire chat (See the xml file)
        private RelativeLayout chatLayout;
        private LinearLayout chatMainLayout;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        /*
        Initialize chat and its display properties (to left or right, blue or purple, depending upon who said it)
         */

        ViewHolder holder = null;

        LayoutInflater inflator= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        holder = new ViewHolder();

        if(convertView==null)
        {

            //Initialize the chat objects
            convertView= inflator.inflate(R.layout.chat_message_layout, parent,false);
            holder.chatText= (TextView) convertView.findViewById(R.id.single_chat_text);
            holder.creator= (TextView) convertView.findViewById(R.id.message_creator);
            holder.chatLayout= (RelativeLayout) convertView.findViewById(R.id.single_chat_layout_id);

            convertView.setTag(holder);
        }
        else
        {
            holder=(ViewHolder) convertView.getTag();
        }



        String mMessage;
        boolean mPosition;
        String mCreator;

        //Get the chat message at the position
        ChatMessage mCM = getItem(position);

        mMessage=mCM.getMessage();
        mPosition=mCM.isPosition();
        mCreator=mCM.getCreator();

        //Set the chat message and creator
        holder.chatText.setText(mMessage);
        holder.creator.setText(mCreator);

        //Set the color of the chat message (Blue if user sent, purple otherwise)
        holder.chatLayout.setBackgroundResource(mPosition? R.drawable.single_chat_drawable_left_main : R.drawable.single_chat_drawable_right_main);

        LinearLayout.LayoutParams params= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);


        RelativeLayout.LayoutParams params_chatMessage= (RelativeLayout.LayoutParams) holder.chatText.getLayoutParams();
        RelativeLayout.LayoutParams params_chatCreator= (RelativeLayout.LayoutParams) holder.creator.getLayoutParams();

        holder.chatMainLayout= (LinearLayout) convertView.findViewById(R.id.single_chat_main_layout_id);

        if(mPosition==true) //If user is not creator of message
        {
            //Set chat at left side
//            chatLayout.setGravity(Gravity.LEFT);
            holder.chatMainLayout.setGravity(Gravity.LEFT);
            holder.chatText.setPadding(30,0,5,0);
            holder.creator.setPadding(30,0,5,0);
//            params_chatMessage.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

//            creator.setGravity(Gravity.LEFT);

        }
        else
        {
            //Set chat at right side
 //           chatLayout.setGravity(Gravity.RIGHT);
            holder.chatMainLayout.setGravity(Gravity.RIGHT);
            holder.chatText.setPadding(0,0,50,0);
            holder.creator.setPadding(0,0,50,0);
//           params_chatMessage.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//           params_chatCreator.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//            creator.setGravity(Gravity.RIGHT);

        }

        holder.chatLayout.setLayoutParams(params);

        return convertView;
    }
}
