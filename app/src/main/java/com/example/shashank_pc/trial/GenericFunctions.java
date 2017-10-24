package com.example.shashank_pc.trial;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.shashank_pc.trial.R.id.password;
import static com.example.shashank_pc.trial.Generic.firestore;
import static com.example.shashank_pc.trial.SingleEntityActivity.secondaryEventsClickFlag;

/**
 * Created by shashank-pc on 8/31/2017.
 */

public class GenericFunctions {

    public static List<Event> secondaryEvents;
    public static String mEncoding = "";
    public static Map <Character,Integer> mDecoding= new HashMap();

    public static void getAttendingEvents(Character type, String entityID)
    {
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
                    Event event = new Event("ToDo","ToDo",eventID);

                    secondaryEvents.add(event);
                    secondaryEventsClickFlag.put(eventID,false);
                }
            }
        });
    }



    public static void initEncoding()
    {
        mEncoding="";
        char c[] = new char[100];
        for(int i=0;i<10;i++) {
            c[i] += i+48;
            mEncoding+=c[i];
            mDecoding.put(c[i],i);
        }

        for(int i=10;i<36;i++) {
            c[i] += 65 + (i - 10);
            mEncoding+=c[i];
            mDecoding.put(c[i],i);
        }

        for(int i=36;i<62;i++) {
            c[i] += 97 + (i - 36);
            mEncoding+=c[i];
            mDecoding.put(c[i],i);
        }

        char tmp[] = {33,37,38,42,43,45,60,61,62,63,64,94,124,126,161,162,163,164,165,166,167,169,170,172,174,176,181,182,186,191,198,215,222,223,230,247,248,254};

        for(int i=0;i<38;i++) {
            c[62 + i] = tmp[i];
            mEncoding+=c[62+i];
            mDecoding.put(c[62+i],62+i);
        }

    }

    public static String decodeNumber(String encodedNumber)
    {
        String number="";
        for(int i=0;i<encodedNumber.length();i++)
        {
            String new_number= Integer.toString(mDecoding.get(encodedNumber.charAt(i)));
            if(new_number.length()==1)
                new_number="0"+new_number;
            number+=new_number;
        }
        return number;
    }

    public static String encodeNumber(String number)
    {
        String encodedNum="";

        for(int i=number.length()-1;i>=0;i=i-2) {
            String tmp="";

            if (i - 1 != -1) {
                tmp += number.charAt(i - 1);
            }
            tmp+= number.charAt(i);

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