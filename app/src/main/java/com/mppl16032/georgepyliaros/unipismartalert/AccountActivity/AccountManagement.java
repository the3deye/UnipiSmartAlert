package com.mppl16032.georgepyliaros.unipismartalert.AccountActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mppl16032.georgepyliaros.unipismartalert.MainActivity;
import com.mppl16032.georgepyliaros.unipismartalert.R;

public class AccountManagement extends AppCompatActivity {

    private Button btnChangePassword, btnRemoveUser, changePassword, signOut, remove, btn_back2;
    private TextView email;

    private EditText oldEmail, old_password, newPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //get current user
        user = FirebaseAuth.getInstance().getCurrentUser();


        email = findViewById(R.id.useremail);
        btnChangePassword = findViewById(R.id.change_password_button);
        btnRemoveUser = findViewById(R.id.remove_user_button);
        btn_back2 = findViewById(R.id.btn_back2);
        changePassword = findViewById(R.id.changePass);
        remove = findViewById(R.id.remove);
        signOut = findViewById(R.id.sign_out);

        oldEmail = findViewById(R.id.old_email);
        old_password = findViewById(R.id.old_password);
        newPassword = findViewById(R.id.newPassword);

        oldEmail.setVisibility(View.GONE);
        old_password.setVisibility(View.GONE);
        newPassword.setVisibility(View.GONE);
        changePassword.setVisibility(View.GONE);
        remove.setVisibility(View.GONE);

        progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        setDataToView(user);


        //-------------------------------BUTTON LISTENERS---------------------------------------------//
        btn_back2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show all invisible editTexts
                oldEmail.setVisibility(View.VISIBLE);
                old_password.setVisibility(View.VISIBLE);
                newPassword.setVisibility(View.VISIBLE);
                changePassword.setVisibility(View.VISIBLE);
            }
        });





        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email = oldEmail.getText().toString();
                CharSequence email_sequence = oldEmail.getText().toString();
                String password = old_password.getText().toString();
                //Through Patterns we can check if an input is en email address or phone number and many other things
                //TextUtils gives as many methods regarding the manipulation of text...in this case we can just check if the input is empty
                if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email_sequence).matches()) {
                    oldEmail.setError(getString(R.string.correct_email));   //Another way to show someting to the user instead of toast messages...we use this one because it persists
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(password) || old_password.getText().toString().trim().length() < 6 || old_password.getText().toString().trim().length() > 12) {
                    old_password.setError(getString(R.string.minimum_password));
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(email, password);   //after getting the users input
                user.reauthenticate(credential)                                                 //we use this method of firebase to check if the input is the same as the users credentials
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {                                              //if the task is successful user enters a new password
                                    if (newPassword.getText().toString().trim().equals("")) {
                                        newPassword.setError("Enter a new password");
                                        progressBar.setVisibility(View.GONE);
                                        return;
                                    }
                                    if (user != null && !newPassword.getText().toString().trim().equals("")) {
                                        if (newPassword.getText().toString().trim().length() < 6 || newPassword.getText().toString().trim().length() > 12) {
                                            newPassword.setError(getString(R.string.minimum_password));
                                            progressBar.setVisibility(View.GONE);
                                        } else {
                                            user.updatePassword(newPassword.getText().toString().trim())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(AccountManagement.this, "Password is updated, sign in with the new password!", Toast.LENGTH_LONG).show();
                                                                signOut();
                                                                progressBar.setVisibility(View.GONE);
                                                            } else {
                                                                Toast.makeText(AccountManagement.this, "Failed to update password!", Toast.LENGTH_LONG).show();
                                                                progressBar.setVisibility(View.GONE);
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                }  else {
                                    Toast.makeText(AccountManagement.this, "Authentication failed! Reenter your email and password...", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


        btnRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AccountManagement.this);
                alertDialogBuilder.setMessage("Are you sure you want to delete your account?")
                        .setPositiveButton("YES",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        progressBar.setVisibility(View.VISIBLE);                                        //After confirming through an alert dialog that user really wants to delete his account
                                        if (user != null) {                                                             //we use firebases delete() method and whatever the result inform the user of that result
                                            user.delete()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(AccountManagement.this, "Your profile is deleted:You can Create a new account now!", Toast.LENGTH_SHORT).show();
                                                                signOut();
                                                                progressBar.setVisibility(View.GONE);
                                                            } else {
                                                                Toast.makeText(AccountManagement.this, "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                                                                progressBar.setVisibility(View.GONE);
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });
                alertDialogBuilder.setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        //------------------------------------------------------------------------------------------------------------------------------------------------------------------------//
    }
    //--------------------------------------------------------------------------END OF onCreate()----------------------------------------------------------------------------------//


    @SuppressLint("SetTextI18n")
    private void setDataToView(FirebaseUser user) {

        email.setText(user.getEmail());


    }

    // this listener will be called when there is change in firebase user session
    FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @SuppressLint("SetTextI18n") //suppress lint check warnings
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // user auth state is changed - user is null
                // launch login activity
                Intent intent = new Intent(AccountManagement.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                setDataToView(user);

            }
        }


    };

    //sign out method
    public void signOut() {
        auth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }


}