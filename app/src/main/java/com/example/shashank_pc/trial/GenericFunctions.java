package com.example.shashank_pc.trial;

import static com.example.shashank_pc.trial.R.id.password;

/**
 * Created by shashank-pc on 8/31/2017.
 */

public class GenericFunctions {

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