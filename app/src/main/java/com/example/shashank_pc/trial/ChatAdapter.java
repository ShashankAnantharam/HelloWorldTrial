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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shashank-pc on 8/31/2017.
 */

//TODO Enhance the layout of ChatAdapter List

public class ChatAdapter extends ArrayAdapter<ChatMessage> {

    private List<ChatMessage> mChatList = new ArrayList<>();
    private TextView chatText;
    private TextView creator;
    private RelativeLayout chatLayout;
    private LinearLayout chatMainLayout;
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

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView==null)
        {
            LayoutInflater inflator= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView= inflator.inflate(R.layout.chat_message_layout, parent,false);
        }

        chatText= (TextView) convertView.findViewById(R.id.single_chat_text);
        creator= (TextView) convertView.findViewById(R.id.message_creator);
        chatLayout= (RelativeLayout) convertView.findViewById(R.id.single_chat_layout_id);

        String mMessage;
        boolean mPosition;
        String mCreator;

        ChatMessage mCM = getItem(position);

        mMessage=mCM.getMessage();
        mPosition=mCM.isPosition();
        mCreator=mCM.getCreator();

        chatText.setText(mMessage);
        creator.setText(mCreator);

        chatLayout.setBackgroundResource(mPosition? R.drawable.single_chat_drawable_left : R.drawable.single_chat_drawable_right);

        LinearLayout.LayoutParams params= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);


        RelativeLayout.LayoutParams params_chatMessage= (RelativeLayout.LayoutParams) chatText.getLayoutParams();
        RelativeLayout.LayoutParams params_chatCreator= (RelativeLayout.LayoutParams) creator.getLayoutParams();

        chatMainLayout= (LinearLayout) convertView.findViewById(R.id.single_chat_main_layout_id);

        if(mPosition==true)
        {
            //Set chat at left side
//            chatLayout.setGravity(Gravity.LEFT);
            chatMainLayout.setGravity(Gravity.LEFT);
//            params_chatMessage.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

//            creator.setGravity(Gravity.LEFT);

        }
        else
        {
            //Set chat at right side
 //           chatLayout.setGravity(Gravity.RIGHT);
            chatMainLayout.setGravity(Gravity.RIGHT);
//           params_chatMessage.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//           params_chatCreator.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//            creator.setGravity(Gravity.RIGHT);

        }

        chatLayout.setLayoutParams(params);

        return convertView;
    }
}
