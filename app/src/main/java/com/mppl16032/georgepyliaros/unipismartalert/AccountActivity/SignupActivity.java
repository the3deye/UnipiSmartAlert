package com.mppl16032.georgepyliaros.unipismartalert.AccountActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ProviderQueryResult;
import com.mppl16032.georgepyliaros.unipismartalert.R;


public class SignupActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputReEnterPassword;
    private Button btnSignIn, btnSignUp, btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);
        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignIn = findViewById(R.id.sign_in_button);
        btnSignUp = findViewById(R.id.sign_up_button);
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        inputReEnterPassword = findViewById(R.id.reenter_password);
        progressBar = findViewById(R.id.progressBar);
        btnResetPassword = findViewById(R.id.btn_reset_password);

        //----------------------------------BUTTON LISTENERS--------------------------------------------------------//

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, ResetPasswordActivity.class);
                finish();
                startActivity(intent);
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this , LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(intent);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = inputEmail.getText().toString().trim();
                CharSequence email_sequence = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString().trim();
                final String checkPassword = inputReEnterPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email_sequence).matches()) {
                    inputEmail.setError(getString(R.string.correct_email));
                    return;
                }
                if (TextUtils.isEmpty(password) || password.length() < 6 || password.length() > 12) {
                    inputPassword.setError(getString(R.string.minimum_password));
                    return;
                }
                if (TextUtils.isEmpty(checkPassword) || checkPassword.length() < 6 || checkPassword.length() > 12) {
                    inputReEnterPassword.setError(getString(R.string.minimum_password));
                    return;
                }
                if (!password.equals(checkPassword)) {
                    Toast.makeText(SignupActivity.this,"Password fields do not match!", Toast.LENGTH_SHORT).show();
                    return;
                }
                auth.fetchProvidersForEmail(email).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<ProviderQueryResult> task) {                                       //After making sure what the user provides is valid
                        boolean check = !task.getResult().getProviders().isEmpty();                                     //We use this firebase method to check if the mail is already in our database

                        if(check) {
                            Toast.makeText(getApplicationContext(), "Email is already used!", Toast.LENGTH_SHORT).show();
                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                            //create user
                            auth.createUserWithEmailAndPassword(email, password)                                    //If email is not used this firebase method creates a user with the provided credentials
                                    .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            progressBar.setVisibility(View.GONE);
                                            // If sign in fails, display a message to the user. If sign in succeeds
                                            // the auth state listener will be notified and logic to handle the
                                            // signed in user can be handled in the listener.
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(SignupActivity.this, "Authentication failed!" + task.getException(),
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(SignupActivity.this, "Successful!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);            //Clears the top of the activity stack...since the only activities that can exist in the
                                                startActivity(intent);                                      //activity stack at this point is this one and the login which closes automatically when stopped
                                                finish();                                                   //the FLAG_ACTIVITY_CLEAR_TOP is enough
                                            }
                                        }
                                    });
                        }
                    }
                });
            }
        });
        //---------------------------------------------------------------------------------------------------------------------------//
    }
    //---------------------------------------------------END OF onCreate()----------------------------------------------------------------//

}
