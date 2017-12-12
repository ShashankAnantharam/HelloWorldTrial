package com.example.shashank_pc.trial;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.shashank_pc.trial.Generic.database;
import static com.example.shashank_pc.trial.Generic.storage;
import static com.example.shashank_pc.trial.LandingPageActivity.userProfilePics;
import static com.example.shashank_pc.trial.R.id.password;
import static com.example.shashank_pc.trial.Generic.firestore;
import static com.example.shashank_pc.trial.SingleEntityActivity.membersProfilePic;
import static com.example.shashank_pc.trial.SingleEntityActivity.secondaryEventsClickFlag;

/**
 * Created by shashank-pc on 8/31/2017.
 */

public class GenericFunctions {

    public static List<Event> secondaryEvents;  //Function to get attending events (Events inside groups fn)
    public static String mEncoding = "";    //Encoding string
    public static Map <Character,Integer> mDecoding= new HashMap();     //Decoding hashmap

    public static void getAttendingEvents(Character type, String entityID)
    {
        /*
        Function to get attending events (Events inside groups; Not needed at this point)
         */
        DocumentReference attendingEvents=null;
        if(type=='G')
        {
            attendingEvents= firestore.collection("groups").document(entityID).collection("events")
                    .document("events");
        }

        attendingEvents.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                Map<String,Object> map = documentSnapshot.getData();

                for(Map.Entry<String,Object> entry: map.entrySet())
                {
                    String eventID= entry.getKey();

                    DatabaseReference tempRef= database.getReference("Details/"+eventID);
                    tempRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String ID = dataSnapshot.getKey();
                            String name = (String) dataSnapshot.child("name").getValue();
                            String desc = (String) dataSnapshot.child("desc").getValue();
                            Event event = new Event(name,desc,ID);
                            secondaryEvents.add(event);
                            secondaryEventsClickFlag.put(ID,false);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }



    public static void initEncoding()
    {
        /*
        Creation of the hash map function for encoding/decoding userIDs
        Encoding function for userID
        Basic idea is that we are converting base 10 into base 100
        For doing this, we only need to traverse two characters at a time, get the number from the character (a two digit no.),
        and make the necessary encoding
         */
        mEncoding="";
        char c[] = new char[100];

        //for numbers 0 to 9, the characters are ASCII values of 48,49,...,57
        for(int i=0;i<10;i++) {
            c[i] += i+48;
            mEncoding+=c[i];
            mDecoding.put(c[i],i);
        }

        //for numbers 10 to 35, the characters are A,B,C,...,Z
        for(int i=10;i<36;i++) {
            c[i] += 65 + (i - 10);
            mEncoding+=c[i];
            mDecoding.put(c[i],i);
        }

        //for numbers 26 to 61, the characters are a,b,c,...,z
        for(int i=36;i<62;i++) {
            c[i] += 97 + (i - 36);
            mEncoding+=c[i];
            mDecoding.put(c[i],i);
        }

        char tmp[] = {33,37,38,42,43,45,60,61,62,63,64,94,124,126,161,162,163,164,165,166,167,169,170,172,174,176,181,182,186,191,198,215,222,223,230,247,248,254};

        //for numbers 62 to 99, the characters are the ASCII value of tmp array.
        for(int i=0;i<38;i++) {
            c[62 + i] = tmp[i];
            mEncoding+=c[62+i];
            mDecoding.put(c[62+i],62+i);
        }

    }

    public static String decodeNumber(String encodedNumber)
    {
        /*
        Function to decode number (UserID) from encoded number
         */
        String number="";
        for(int i=0;i<encodedNumber.length();i++)       //Traverse the entire encoded number.
        {

            //Get the new_number by decoding the value of the encoded character
            String new_number= Integer.toString(mDecoding.get(encodedNumber.charAt(i)));

            //If new number is of length 1, Add "0" to the string beacuse we need 2 digits.
            if(new_number.length()==1)
                new_number="0"+new_number;

            //Append the new number to the already existing string for the number
            number+=new_number;
        }
        return number;
    }

    public static String encodeNumber(String number)
    {
        /*
        Function to encode number (User ID)
         */
        String encodedNum="";

        for(int i=number.length()-1;i>=0;i=i-2) {
            //Traversing two-two characters at a time
            String tmp="";

            //if i!=0, then take the prevous character also
            if (i - 1 != -1) {
                tmp += number.charAt(i - 1);
            }
            tmp+= number.charAt(i);

            //Encode it using the string hashmap
            encodedNum=mEncoding.charAt(Integer.parseInt(tmp))+encodedNum;

        }
        return encodedNum;
    }




    public static String getSubtitle(String text)
    {

        //TODO Correct this code
        /*
        Write function to screen the text length here
         */
        if(text.length()<48)        //If text is within prescribed length
            return text;

        String ans= text.substring(0,47);       //Else, put a "..."  after the required length of string
        ans+="...";
        return ans;
    }


    public static boolean compareUserIDs(String u1, String u2)
    {
        //If u1>u2, then true

        if(u2.length()>u1.length())
            return false;
        else if(u2.length()<u1.length())
            return true;

        for(int i=0;i<u1.length();i++)
        {
            if(u1.charAt(i)>u2.charAt(i))
                return true;
            else if(u1.charAt(i)<u2.charAt(i))
                return false;
        }

        return false;

    }

    public static String getContactChatID(String u1, String u2)
    {
        //Smaller userIDs first
        if(compareUserIDs(u1,u2))
            return ("C"+encodeNumber(u2)+"_"+encodeNumber(u1));
        else
            return ("C"+encodeNumber(u1)+ "_"+encodeNumber(u2));
    }

    public static void addProfilePic(final String memberID) {

        //Get profile pics into hashmap

        StorageReference ref= storage.getReference("ProfilePics").child(memberID+".jpg");
        final long ONE_MEGABYTE = 1024 * 1024;

        //fetch data from Firebase Cloud Storage
        ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {

                //           Toast.makeText(getApplicationContext(),"Downloaded",Toast.LENGTH_SHORT).show();


                try
                {
                    //Get profile pic in Bitmap format
                    Bitmap profilePic = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    //Reisize image of Profile Pic
                    profilePic= resizeImage(profilePic);

                    //Change shape of profile pic into a circle
                    profilePic= getCircleBitmap(profilePic);
                    //Bitmap round_img= getRoundedRectBitmap(small_img);

                    //Place it in the hashmap of profile pics.
                    if(membersProfilePic!=null && !membersProfilePic.containsKey(memberID))
                          membersProfilePic.put(memberID,profilePic);
                    if(!userProfilePics.containsKey(memberID))
                        userProfilePics.put(memberID,profilePic);




                }
                catch (Exception e)
                {

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors

                //              Toast.makeText(getApplicationContext(), exception.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static boolean validatePhone(String phone) //function to validate phone number (Bharath Kota)
    {
        if (phone.length()==10){    // Assume Indian Phone Number
            long check_number;
            try {
                check_number = Long.parseLong(phone);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }

            return false;

    }

    public static boolean isPasswordFine(String password)   //TODO Function to check if password is fine
    {
        //Please specify the conditions you set for the password in your comment (Length > 6, atleast one integer, etc.)
        return password.matches("^(?=.*[A-Z])(?=.*[!@#$&*%+=])(?=.*[0-9])(?=.*[a-z]).{6,}$");

    }


    public static Bitmap getCircleBitmap(Bitmap bitmap) {

        /*
        Function to change shape of Bitmap to circle
         */

        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    public static Bitmap resizeImage(Bitmap bitmap)
    {
        return Bitmap.createScaledBitmap(bitmap, 150, 150, false);
    }







}


/*
FAQs:

1) App not broadcasting GPS when it is paused or phone is locked?
Ans) Phone is in power saving mode. App MUST broadcast GPS when it is paused or phone is locked.
 */