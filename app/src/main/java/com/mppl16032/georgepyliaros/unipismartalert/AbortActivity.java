package com.mppl16032.georgepyliaros.unipismartalert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class AbortActivity extends AppCompatActivity {

    private Button btn_back3, btn_credentials;
    private EditText email2, password2;
    private FirebaseUser user;
    private ProgressBar progressBar2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abort);

        user = FirebaseAuth.getInstance().getCurrentUser();

        btn_back3 = findViewById(R.id.btn_back3);
        btn_credentials = findViewById(R.id.btn_credentials);
        email2 = findViewById(R.id.email2);
        password2 = findViewById(R.id.password2);

        progressBar2 = findViewById(R.id.progressBar2);

        progressBar2.setVisibility(View.GONE);

        //------------------------------BTN_LISTENERS---------------------------------------------//
        btn_credentials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = email2.getText().toString();
                CharSequence email_sequence = email2.getText().toString();                                          //We make all the required checking as alwatys...
                String password = password2.getText().toString();                                                    // check if the provided input is empty, if the email is in an email format
                                                                                                                    //if password length is not correct
                progressBar2.setVisibility(View.VISIBLE);

                if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email_sequence).matches()) {
                    Toast.makeText(AbortActivity.this, "Enter your email address!", Toast.LENGTH_SHORT).show();
                    progressBar2.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(password) || password.length() < 6 || password.length() > 12) {
                    Toast.makeText(AbortActivity.this, "Enter your password!", Toast.LENGTH_SHORT).show();
                    progressBar2.setVisibility(View.GONE);
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(email, password);                       //after checking user input we use firebase method reauthenticate which checks if user
                user.reauthenticate(credential)                                                                     //input is the same as his account credentials
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {                                              // if reauthentication succeeds sendSms() method runs, this activity closes as well as the last
                                        sendSms();                                                             //activity in the stack and MainActivity starts
                                        progressBar2.setVisibility(View.GONE);
                                        Intent abortIntent = new Intent(AbortActivity.this, MainActivity.class);
                                        abortIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        finish();
                                        startActivity(abortIntent);
                                    } else {
                                        Toast.makeText(AbortActivity.this, "Authentication failed! Reenter your email and password...", Toast.LENGTH_SHORT).show();
                                        progressBar2.setVisibility(View.GONE);
                                    }
                                }
                        });

            }
        });

        btn_back3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    //----------------------------------------------------------END OF onCreate()--------------------------------------------------------------------------//


    private void sendSms () {

        SmsManager smsManager = SmsManager.getDefault();                                    //for each saved contact that is saved a simple sms is sent and the user is informed about it through toast
        SharedPreferences sharedPreferences = getSharedPreferences("contactsInfo", Context.MODE_PRIVATE);
        if(sharedPreferences.contains("first_contact") || sharedPreferences.contains("second_contact") || sharedPreferences.contains("third contact")) {
            if(sharedPreferences.contains("first_contact")) {
                String first = sharedPreferences.getString("first_contact", "");
                String phoneNo1 = "+30"+first;
                String message = "Άκυρος ο συναγερμός. Όλα καλά!";
                smsManager.sendTextMessage(phoneNo1, null, message, null, null);
                Toast.makeText(getApplicationContext(), "First contact SMS sent", Toast.LENGTH_SHORT).show();
            }
            if(sharedPreferences.contains("second_contact")) {
                String second = sharedPreferences.getString("second_contact", "");
                String phoneNo2 = "+30"+second;
                String message = "Άκυρος ο συναγερμός. Όλα καλά!";
                smsManager.sendTextMessage(phoneNo2, null, message, null, null);
                Toast.makeText(getApplicationContext(), "Second contact SMS sent", Toast.LENGTH_SHORT).show();
            }
            if(sharedPreferences.contains("third_contact")) {
                String third = sharedPreferences.getString("third_contact", "");
                String phoneNo3 = "+30"+third;
                String message = "Άκυρος ο συναγερμός. Όλα καλά!";
                smsManager.sendTextMessage(phoneNo3,null, message, null, null);
                Toast.makeText(getApplicationContext(), "Third contact SMS sent", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(),"No contacts added!",Toast.LENGTH_SHORT).show();         //in case no contacts are saved
        }
    }

}
