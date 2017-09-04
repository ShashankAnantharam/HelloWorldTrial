package com.example.shashank_pc.trial;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class TestActivity extends AppCompatActivity {

    TextView testTextView1;
    TextView testTextView2;
    TextView testTextView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        /*
        Code to set layout
         */
        testTextView1=(TextView) findViewById(R.id.test_text1);
        testTextView2=(TextView) findViewById(R.id.test_text2);
        testTextView3=(TextView) findViewById(R.id.test_text3);


        /*
        userID is the string that you have to fill with the final UserID from WhatsApp
         */

        String userID= "55554";


        /*
        Start your code below this comment
         */

        //TODO Bharath Kota/ Mehtab Ahmad Code here. Do NOT change any other file. Work only on this file





        /*
        End your code above this comment
         */




        /*
        Code to display the UserID that you got
         */

        testTextView1.setText("UserID is:");
        testTextView2.setText(userID);


    }
}
