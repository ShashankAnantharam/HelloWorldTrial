package com.example.shashank_pc.trial;

import android.widget.Toast;

import static com.example.shashank_pc.trial.R.id.password;

/**
 * Created by shashank-pc on 8/31/2017.
 */

public class GenericFunctions {

    public static String mEncoding = "";
    public static void initEncoding()
    {
        mEncoding="";
        char c[] = new char[100];
        for(int i=0;i<10;i++) {
            c[i] += i+48;
            mEncoding+=c[i];
        }

        for(int i=10;i<36;i++) {
            c[i] += 65 + (i - 10);
            mEncoding+=c[i];
        }

        for(int i=36;i<62;i++) {
            c[i] += 97 + (i - 36);
            mEncoding+=c[i];
        }

        char tmp[] = {33,37,38,42,43,45,60,61,62,63,64,94,124,126,161,162,163,164,165,166,167,169,170,172,174,176,181,182,186,191,198,215,222,223,230,247,248,254};

        for(int i=0;i<38;i++) {
            c[62 + i] = tmp[i];
            mEncoding+=c[62+i];
        }

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
            return ("C"+u2+" "+u1);
        else
            return ("C"+u1+ " "+u2);
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
}


/*
FAQs:

1) App not broadcasting GPS when it is paused or phone is locked?
Ans) Phone is in power saving mode. App MUST broadcast GPS when it is paused or phone is locked.
 */