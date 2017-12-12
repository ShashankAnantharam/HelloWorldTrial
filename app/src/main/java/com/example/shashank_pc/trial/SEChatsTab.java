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
import static com.example.shashank_pc.trial.LandingPageActivity.allContactNames;

/**
 * Created by shashank-pc on 8/26/2017.
 */

//TODO Make function for retrieving chats from Database

public class SEChatsTab  extends Fragment {

    private String mUserID;     //UserID
    private String mUserName;       //UserName
    private String mEntityName;     //EntityName (Group,Contact,Event name)
    private String mEntityID;       //EntityID
    private char mType;             //Entity Type (Group, Contact or Event)



    private ListView mChatList;     //ChatList: A ListView containing the chat bubbles
    private Button mSendButton;         //Button to send messages
    private EditText mChatText;         //EditText to write messages
    private ChatAdapter chatAdapter;        //A ChatAdapter that stores the list in the listview


    private DatabaseReference newComment;
    private String chatMessageAddress;      //Address of Chat Message in the Firebase Realtime DB
    private DatabaseReference commentListener;      //DatabaseReference to address of the Chat messages in Firebase Realtime DB
    private ChildEventListener singleCommentListener;       //Listening to single chat messages in Firebase
    private  DatabaseReference fireStoreMemberLength;       //Realtime DB Reference which contains the total length of members
    private ValueEventListener firestoreMemLengthVal;       ///value event listener to listen to any change in the total length of members
    private String prefID;

    long length;   //total number of chat messages stored in local and downloaded from Realtime database
    long total; //total number of people who are in the group/event/contact chat bubbble


    public void passUserDetails(String userID, String userName, String entityName, String entityID, char type)
    {
        /*
        Function to pass crucial data to the Tab from the parent activity (SingleEntityActivity)
         */
        mUserID= userID;
        mUserName=userName;
        mEntityName=entityName;
        mEntityID=entityID;
        mType=type;
    }

    public void updateChatFlag(final DatabaseReference ref)
    {
        /*
        Function to update view count (and delete message if all people have seen it)
         */

        DatabaseReference updateRef = ref.child("views");
        total = 0;
        if(mType=='U')
        {
            //For Contact chats, only 2 people have to see the message
            total=2;
        }
        else if(mType=='E' || mType=='G')
        {
            //If group or event, get the total number of people who have to see the message
            SharedPreferences preferences = getContext().getSharedPreferences(mEntityID,Context.MODE_PRIVATE);
            total = preferences.getLong("TotalMembers",100000);
        }


        //Updating the viewcount using the transaction API of Firebase Realtime DB
        updateRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                Long views = Long.parseLong(mutableData.getValue(String.class));

                if(views<total-1)
                {
                    //If the views are lesser than total, then increment the current views
                    mutableData.setValue(Long.toString(views+1));
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
        /*
        Function to initialize the comment listeners
         */

        if(mType=='U')
        {
            //Generate contact ID and set as pref ID
            prefID=getContactChatID(mUserID,mEntityID);
        }
        else if(mType=='E' || mType=='G')
        {
            //Set Entity ID as pref ID for Events and Groups
            prefID=mEntityID;



            //TODO Move this later on to Service if necessary

            //Get the total number of members who will see the chat message from RealtimeDatabase (total no. of members in group/event)
            fireStoreMemberLength= database.getReference("MemLen/"+prefID);
            firestoreMemLengthVal= fireStoreMemberLength.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long totalMembers = dataSnapshot.getValue(Long.class);
                    SharedPreferences preferences  = getContext().getSharedPreferences(
                            prefID,Context.MODE_PRIVATE
                    );
                    //Save the total number of members in SharedPreferences
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

        //Get the total number of chat messages until now
        length = preferences.getLong("length",0);

        for(int i=1;i<=length;i++)
        {
            String key = preferences.getString(Integer.toString(i),"");
            if(!key.equals(""))
            {
                //Message is already present in local, display on chat box

                String mTS = preferences.getString((key+ "_TS"),"");
                String chatText = preferences.getString((key+ "_msg"),"");
                String creator = preferences.getString((key+"_creator"),"");
                if(allContactNames.containsKey(creator))
                {
                    //To display name as the creator of message instead of phone number
                    creator=allContactNames.get(creator);
                }
                boolean isNotMyMessage = preferences.getBoolean((key+ "_isNotMyMsg"),false);
                ChatMessage nCM= new ChatMessage(isNotMyMessage, chatText, creator);
                chatAdapter.add(nCM);
            }

        }





        //Listen to new messages from Realtime Database

        commentListener=database.getReference("ChtMsgs/"+prefID);
        singleCommentListener = commentListener.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();

                if(preferences.getString((key+"_TS"),"x").equals("x"))
                {
                    //Data Not already there. Add it to Local Storage

                    length++;
                    editor.putLong("length",length);  //Change length of local chat array
                    editor.putString(Long.toString(length),key); //Set index to key


                    //Get data from Firebase RealtimeDatabase
                    String chatText = (String)dataSnapshot.child("Msg").getValue();
                    String creator = (String) dataSnapshot.child("Creator").getValue();
                    String tS= (String) dataSnapshot.child("TS").getValue();
                    boolean isNotMyMessage = true;
                    if(creator.equals(mUserID)) {
                        creator = "Me";
                        isNotMyMessage = false;
                    }
                    else if(allContactNames.containsKey(creator))
                    {
                        creator=allContactNames.get(creator);
                    }

                    //Put the details of chat in shared preference
                    editor.putString((key+"_creator"),creator);
                    editor.putString((key+ "_msg"),chatText);
                    editor.putString((key+ "_TS"),tS);
                    editor.putBoolean((key+ "_isNotMyMsg"),isNotMyMessage);
                    editor.commit();

                    //Add chat to adapter and listview
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



        //Initialize objects

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
                /*
                Function to send typed chat message
                 */
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
                    firebaseMap.put("views",Integer.toString(0));

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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        commentListener.removeEventListener(singleCommentListener);
        if(mType=='E' || mType=='G')
            fireStoreMemberLength.removeEventListener(firestoreMemLengthVal);

    }
}
