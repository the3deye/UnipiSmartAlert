package com.mppl16032.georgepyliaros.unipismartalert;

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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AbortActivityFall extends AppCompatActivity {

    private Button btn_back4, btn_credentials2;
    private EditText email3, password3;
    private FirebaseUser user;
    private ProgressBar progressBar3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abort_fall);

        user = FirebaseAuth.getInstance().getCurrentUser();

        btn_back4 = findViewById(R.id.btn_back4);
        btn_credentials2 = findViewById(R.id.btn_credentials2);
        email3 = findViewById(R.id.email3);
        password3 = findViewById(R.id.password3);

        progressBar3 = findViewById(R.id.progressBar3);

        progressBar3.setVisibility(View.GONE);

        //------------------------------BTN_LISTENERS---------------------------------------------//
        btn_credentials2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = email3.getText().toString();
                String password = password3.getText().toString();                                                       //this activity is exactly the same as AbortActivity. Only difference
                CharSequence email3_sequence = email3.getText().toString();                                             //it does not send an sms.

                progressBar3.setVisibility(View.VISIBLE);

                if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email3_sequence).matches()) {
                    Toast.makeText(AbortActivityFall.this, "Enter your correct email address!", Toast.LENGTH_SHORT).show();
                    progressBar3.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(password) || password.length() < 6 || password.length() > 12) {
                    Toast.makeText(AbortActivityFall.this, "Enter your correct password!", Toast.LENGTH_SHORT).show();
                    progressBar3.setVisibility(View.GONE);
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progressBar3.setVisibility(View.GONE);
                                    Intent abortIntent = new Intent(AbortActivityFall.this, MainActivity.class);
                                    abortIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    finish();
                                    startActivity(abortIntent);
                                } else {
                                    Toast.makeText(AbortActivityFall.this, "Authentication failed! Reenter your email and password...", Toast.LENGTH_SHORT).show();
                                    progressBar3.setVisibility(View.GONE);

                                }
                            }
                        });

            }
        });

        btn_back4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
