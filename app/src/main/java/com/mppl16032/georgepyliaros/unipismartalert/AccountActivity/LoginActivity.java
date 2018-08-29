package com.mppl16032.georgepyliaros.unipismartalert.AccountActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.mppl16032.georgepyliaros.unipismartalert.MainActivity;
import com.mppl16032.georgepyliaros.unipismartalert.R;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar3;
    private Button btnSignup, btnLogin, btnReset;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int MY_PERMISSIONS_REQUEST_SMS = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*        checkLocationPermission();
        checkSMSPermission();*/
        setContentView(R.layout.activity_login);
        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {                //in case the user is logged in the activity closes and opens the MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS};    //create an array of the two permissions we need

        if(!hasPermissions(this, PERMISSIONS)) {                                                    //if those permissions are not granted request them
            ActivityCompat.requestPermissions(this, PERMISSIONS, MY_PERMISSIONS_REQUEST_LOCATION);
        }

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        progressBar3 = findViewById(R.id.progressBar3);
        btnSignup = findViewById(R.id.btn_signup);
        btnLogin = findViewById(R.id.btn_login);
        btnReset = findViewById(R.id.btn_reset_password);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence email_sequence = inputEmail.getText().toString();
                String email = inputEmail.getText().toString();

                final String password = inputPassword.getText().toString();

                progressBar3.setVisibility(View.VISIBLE);

                if (TextUtils.isEmpty(email_sequence) || !Patterns.EMAIL_ADDRESS.matcher(email_sequence).matches()) {
                    inputEmail.setError(getString(R.string.correct_email));
                    progressBar3.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(password) || password.length() < 6 || password.length() >12) {
                    inputPassword.setError(getString(R.string.minimum_password));
                    progressBar3.setVisibility(View.GONE);
                    return;
                }

                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    progressBar3.setVisibility(View.GONE);
                                } else {
                                    progressBar3.setVisibility(View.GONE);
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    finish();
                                    startActivity(intent);
                                }
                            }
                        });
            }
        });
    }//------------------------------------------------------END OF onCreate()-----------------------------------------------------------------------------------------------//


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {                                                   // if the permissions array is not empty (and the context)
            for (String permission : permissions) {                                                        //check each permission string individually
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {     //if those permission strings are not equal to the PackageManager.PERMISSION_GRANTED
                    return false;                                                                                       //then this method returns false else returns true
                }
            }
        }
        return true;
    }

}
