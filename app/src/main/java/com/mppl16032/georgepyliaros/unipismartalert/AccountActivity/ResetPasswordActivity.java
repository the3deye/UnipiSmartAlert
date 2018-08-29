package com.mppl16032.georgepyliaros.unipismartalert.AccountActivity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.mppl16032.georgepyliaros.unipismartalert.R;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText inputEmail;
    private Button btnReset, btnBack;
    private FirebaseAuth auth;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        inputEmail = findViewById(R.id.email);

        btnReset = findViewById(R.id.btn_reset_password);
        btnBack = findViewById(R.id.btn_back);

        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();


        //---------------------------------------BUTTON LISTENERS--------------------------------------------------------------------------------//
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().trim();
                CharSequence email_sequence = inputEmail.getText().toString();
                if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email_sequence).matches()) {        //We request the user to provide us with his login email
                    inputEmail.setError(getString(R.string.correct_email));
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {                                                          //if firebase checks that it's the right one it sends an email to the user with instructions
                                if (task.isSuccessful()) {                                                          //on how to rest his password
                                    Toast.makeText(ResetPasswordActivity.this, "Check your email! We have sent you instructions on how to reset your password!", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(ResetPasswordActivity.this, "Wrong email entered!", Toast.LENGTH_SHORT).show();
                                }

                                progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        });
        //-----------------------------------------------------------------------------------------------------------------------------------------------//

    }
    //-----------------------------------------------------------------END OF onCreate()-----------------------------------------------------------------------//
}