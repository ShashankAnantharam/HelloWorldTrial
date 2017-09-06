package com.example.shashank_pc.trial;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class CheckInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        //Pop dialogue
/*
        AlertDialog.Builder mBuilder= new AlertDialog.Builder(getApplicationContext());
        View mView= getLayoutInflater().inflate(R.layout.dialog_register, null);
        final EditText mPhone= (EditText) findViewById(R.id.etPhone);
        final EditText mPass= (EditText) findViewById(R.id.etPass);
        final EditText mRtPass= (EditText) findViewById(R.id.etRtPass);
        Button mRegisterButton = (Button) findViewById(R.id.btnRegister);

        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!GenericFunctions.validatePhone(mPhone.getText().toString()))
                {
                    Toast.makeText(getApplicationContext(),"Enter a valid Phone number", Toast.LENGTH_SHORT).show();
                }
                else if(mPass.getText().toString()==""){
                    Toast.makeText(getApplicationContext(), "Enter a password", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(mPass.getText().toString()!=mRtPass.getText().toString())
                        Toast.makeText(getApplicationContext(), "Retype password", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mBuilder.setView(mView);
        AlertDialog dialog= mBuilder.create();
        dialog.show();
*/
    }
}
