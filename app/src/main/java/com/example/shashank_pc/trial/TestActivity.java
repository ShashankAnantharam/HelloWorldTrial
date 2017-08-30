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

        testTextView1=(TextView) findViewById(R.id.test_text1);
        testTextView2=(TextView) findViewById(R.id.test_text2);
        testTextView3=(TextView) findViewById(R.id.test_text3);

        Intent caller = getIntent();
        testTextView1.setText(caller.getStringExtra("Name"));
        testTextView2.setText(caller.getStringExtra("Description"));
        testTextView3.setText(Boolean.toString(caller.getBooleanExtra("IsGPSBroadcast",false)));

    }
}
