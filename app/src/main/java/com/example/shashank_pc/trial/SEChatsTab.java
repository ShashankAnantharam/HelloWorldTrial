package com.example.shashank_pc.trial;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import static com.example.shashank_pc.trial.Generic.database;
import static com.example.shashank_pc.trial.GenericFunctions.getContactChatID;

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
    private DatabaseReference commentListener;
    private String prefID;

    long length;


    public void passUserDetails(String userID, String userName, String entityName, String entityID, char type)
    {
        mUserID= userID;
        mUserName=userName;
        mEntityName=entityName;
        mEntityID=entityID;
        mType=type;
    }

    public void updateChatFlag(final DatabaseReference ref)
    {

        DatabaseReference updateRef = ref.child("views");
        long total = 0;
        if(mType=='U')
        {
            total=2;
        }
        else if(mType=='E' || mType=='G')
        {
            SharedPreferences preferences = getContext().getSharedPreferences(mEntityID,Context.MODE_PRIVATE);
            total = preferences.getLong("TotalMembers",100000);
        }


        updateRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                Long total = mutableData.getValue(Long.class);
                if(total==null)
                {
                    //If there are no views, add this download as one view
                    mutableData.setValue(1);
                }
                else if(total<total-1)
                {
                    //If the views are lesser than total, then increment the current views
                    mutableData.setValue(total+1);
                }
                else
                {
                    //If the download is the last one, delete the chat message from Firebase
                    ref.removeValue();
                }


                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if(databaseError!=null)
                    Toast.makeText(getContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void initCommentListener()
    {

        if(mType=='U')
        {
            //Generate contact ID and set as pref ID
            prefID=getContactChatID(mUserID,mEntityID);
        }
        else if(mType=='E' || mType=='G')
        {
            //Set Entity ID as pref ID for Events and Groups
            prefID=mEntityID;

            //Get length of current total members who will see chat message

            //TODO Move this later on to Service
            DatabaseReference fireStoreEventMemLength= database.getReference("MemLen/"+prefID);
            fireStoreEventMemLength.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long totalMembers = dataSnapshot.getValue(Long.class);
                    SharedPreferences preferences  = getContext().getSharedPreferences(
                            prefID,Context.MODE_PRIVATE
                    );
                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putLong("TotalMembers",totalMembers);

                    edit.commit();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        final SharedPreferences preferences = getContext().getSharedPreferences(prefID,
                Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();

        length = preferences.getLong("length",0);

        for(int i=1;i<=length;i++)
        {
            String key = preferences.getString(Integer.toString(i),"");
            if(!key.equals(""))
            {
                //Message already present in local

                String mTS = preferences.getString((key+ "_TS"),"");
                String chatText = preferences.getString((key+ "_msg"),"");
                String creator = preferences.getString((key+"_creator"),"");
                boolean isNotMyMessage = preferences.getBoolean((key+ "_isNotMyMsg"),false);
                ChatMessage nCM= new ChatMessage(isNotMyMessage, chatText, creator);
                chatAdapter.add(nCM);
            }

        }






        commentListener=database.getReference("ChtMsgs/"+prefID);
        commentListener.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();

                if(preferences.getString((key+"_TS"),"x").equals("x"))
                {
                    //Data Not already there. Add it to Local Storage

                    length++;
                    editor.putLong("length",length);  //Change length of local chat array
                    editor.putString(Long.toString(length),key); //Set index to key


                    //Get data from Firebase
                    String chatText = (String)dataSnapshot.child("Msg").getValue();
                    String creator = (String) dataSnapshot.child("Creator").getValue();
                    String tS= (String) dataSnapshot.child("TS").getValue();
                    boolean isNotMyMessage = true;
                    if(creator.equals(mUserID)) {
                        creator = "Me";
                        isNotMyMessage = false;
                    }

                    //Put the details of chat in shared preference
                    editor.putString((key+"_creator"),creator);
                    editor.putString((key+ "_msg"),chatText);
                    editor.putString((key+ "_TS"),tS);
                    editor.putBoolean((key+ "_isNotMyMsg"),isNotMyMessage);
                    editor.commit();

                    //Add chat to adapter
                    ChatMessage nCM= new ChatMessage(isNotMyMessage, chatText, creator);
                    chatAdapter.add(nCM);

                    //Update read status using transactions
                    updateChatFlag(dataSnapshot.getRef());

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


  /*  public void initCommentListener()
    {
        if(mType=='E' || mType=='G')
            commentListener = database.getReference("ChtMsgs/" + mEntityID);
  //      else if(mType=='U')
  //          commentListener=database.getReference("ChtMsgs/"+getContactChatID(mUserID,mEntityID));

        commentListener.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                String chatText = (String)dataSnapshot.child("Msg").getValue();
                String creator = (String) dataSnapshot.child("Creator").getValue();
                boolean isNotMyMessage = true;
                if(creator.equals(mUserID)) {
                    creator = "Me";
                    isNotMyMessage = false;
                }


                ChatMessage nCM= new ChatMessage(isNotMyMessage, chatText, creator);
                chatAdapter.add(nCM);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.single_entity_chats, container, false);




        mChatList= (ListView) rootView.findViewById(R.id.chat_list);

        mSendButton= (Button) rootView.findViewById(R.id.chat_send_button);

        mChatText= (EditText) rootView.findViewById(R.id.enter_chat_txt);

        chatAdapter = new ChatAdapter(getContext(), R.layout.chat_message_layout);
        mChatList.setAdapter(chatAdapter);



        initCommentListener();





        if(mType=='E' || mType=='G')
        {
            // Event and group, get the reference

            chatMessageAddress = "ChtMsgs/" + mEntityID ;

        }
        else if(mType=='U')
        {
            //User
            chatMessageAddress = "ChtMsgs/"+getContactChatID(mUserID,mEntityID);
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


                if(mType=='E' || mType=='G' || mType=='U')
                {

                    Long tsLong = System.currentTimeMillis()/1000;
                    String ts = tsLong.toString();

                    Map<String,String> firebaseMap= new HashMap<String, String>();
                    firebaseMap.put("TS",ts);
                    firebaseMap.put("Creator",mUserID);
                    firebaseMap.put("Msg",chatText);

                    database.getReference(chatMessageAddress).push().setValue(firebaseMap);




//                    newComment.child("TS").setValue(ts);
//                    newComment.child("Creator").setValue(mUserID);
//                    newComment.child("Msg").setValue(chatText);
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
