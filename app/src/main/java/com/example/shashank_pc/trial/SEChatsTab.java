package com.example.shashank_pc.trial;

import android.database.DataSetObserver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

import static com.example.shashank_pc.trial.Generic.database;

/**
 * Created by shashank-pc on 8/26/2017.
 */

//TODO Make function for retrieving chats from Database

public class SEChatsTab  extends Fragment {

    private String mUserID;
    private String mUserName;
    private String mEntityName;
    private String mEntityID;
    private char mType;



    private ListView mChatList;
    private Button mSendButton;
    private EditText mChatText;
    private ChatAdapter chatAdapter;


    private DatabaseReference newComment;
    private String chatMessageAddress;


    public void passUserDetails(String userID, String userName, String entityName, String entityID, char type)
    {
        mUserID= userID;
        mUserName=userName;
        mEntityName=entityName;
        mEntityID=entityID;
        mType=type;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.single_entity_chats, container, false);




        mChatList= (ListView) rootView.findViewById(R.id.chat_list);

        mSendButton= (Button) rootView.findViewById(R.id.chat_send_button);

        mChatText= (EditText) rootView.findViewById(R.id.enter_chat_txt);

        chatAdapter = new ChatAdapter(getContext(), R.layout.chat_message_layout);
        mChatList.setAdapter(chatAdapter);





        if(mType=='E' || mType=='G')
        {
            // Event and group, get the reference

            chatMessageAddress = "ChtMsgs/" + mEntityID ;

        }


        chatAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mChatList.setSelection(chatAdapter.getCount()-1);
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chatText = mChatText.getText().toString();
                mChatText.setText("");
                ChatMessage nCM= new ChatMessage(false, chatText, "Me");
                chatAdapter.add(nCM);


                if(mType=='E' || mType=='G')
                {
                    newComment = database.getReference(chatMessageAddress).push();
                    Long tsLong = System.currentTimeMillis()/1000;
                    String ts = tsLong.toString();

                    newComment.child("TS").setValue(ts);
                    newComment.child("Creator").setValue(mUserID);
                    newComment.child("Msg").setValue(chatText);
                }

            }
        });


        return rootView;
    }


    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        try {
            if (menuVisible) {
                getActivity().getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_background));
            }
        }
        catch (Exception e)
        {

        }

    }
}
