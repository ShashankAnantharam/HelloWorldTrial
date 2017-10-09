package com.example.shashank_pc.trial;

import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.shashank_pc.trial.Generic.firestore;

/**
 * Created by shashank-pc on 8/26/2017.
 */

public class SEMembersTab  extends LPContactsTab{



    private char mType;
    private String mEntityName;
    private String mEntityID;
    private boolean isExistFlag;

    private long total_doc_number;
    private long curr_doc_number=0;
    private String type="";
    public static HashMap<String,Integer> MemberListMap;
    private DocumentReference MemberRef;

    
    private DocumentReference mMemberDocNumber;


    @Override
    public void addContact(User user)
    {
        boolean mBroadcastLocationFlag;
        mBroadcastLocationFlag=preferences.getBoolean(user.getNumber(),false);
        user.initBroadcastLocationFlag(mBroadcastLocationFlag);
        mContacts.add(user);
        MemberListMap.put(user.getNumber(),mContacts.size()-1);
        refresh();
    }

    public void passUserDetails(String userID, String userName, String name, String contactID, char type)
    {
        super.passUserDetails(userID,userName);
        mEntityName=name;
        mEntityID=contactID;
        mType=type;
    }

    

    @Override
    public void initUsers()
    {
        MemberListMap = new HashMap<>();
        hasInitContacts=true;
        curr_doc_number=0;

        firestore = FirebaseFirestore.getInstance();


        if(mType=='E')
            type="events";
        else if(mType=='G')
            type="groups";

        isExistFlag=true;



                        MemberRef = firestore.collection(type).document(mEntityID).
                                      collection("members").
                                      document("members");

                        MemberRef.
                                addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                if (e == null) {
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap = documentSnapshot.getData();
                                    for (Map.Entry<String, Object> entry : userMap.entrySet()) {
                                        if (entry.getKey().equals("list")) {
                                            List<String> members = (List) entry.getValue();
                                            for (String member : members) {
                                                if (!MemberListMap.containsKey(member)) {
                                                    //New member
                                                    User user = new User(member, member);

                                                    addContact(user);
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        });


    }
}
