package com.example.shashank_pc.trial;

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
}
