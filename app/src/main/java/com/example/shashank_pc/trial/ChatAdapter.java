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

public class ChatAdapter extends ArrayAdapter<ChatMessage> {

    private List<ChatMessage> mChatList = new ArrayList<>();
    private TextView chatText;
    private TextView creator;
    private RelativeLayout chatLayout;
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

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        if(mPosition==true)
        {
            //set chat at left side
            params.gravity= Gravity.LEFT;
        }
        else
        {
            params.gravity= Gravity.RIGHT;
        }

        chatText.setLayoutParams(params);


        return convertView;
    }
}
