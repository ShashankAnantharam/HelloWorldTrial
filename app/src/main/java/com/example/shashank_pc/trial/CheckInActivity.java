package com.example.shashank_pc.trial;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class CheckInActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        getDialogue();



    }

    public void getDialogue(){

        //Pop dialogue

        AlertDialog.Builder mBuilder= new AlertDialog.Builder(getApplicationContext());
        View mView= getLayoutInflater().inflate(R.layout.dialog_register, null);
        final EditText mPhone= (EditText) mView.findViewById(R.id.etPhone);
        final EditText mPass= (EditText) mView.findViewById(R.id.etPass);
        final EditText mRtPass= (EditText) mView.findViewById(R.id.etRtPass);
        Button mRegisterButton = (Button) mView.findViewById(R.id.btnRegister);

        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            /*    if(!GenericFunctions.validatePhone(mPhone.getText().toString()))
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
*/
            }
        });

        mBuilder.setView(mView);
        AlertDialog dialog= mBuilder.create();
        dialog.show();

    }
}
