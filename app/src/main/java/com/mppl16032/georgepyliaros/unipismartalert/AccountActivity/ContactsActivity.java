package com.mppl16032.georgepyliaros.unipismartalert.AccountActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mppl16032.georgepyliaros.unipismartalert.R;

import java.util.regex.Pattern;

public class ContactsActivity extends AppCompatActivity {

    private EditText firstContact_editText, secondContact_editText, thirdContact_editText;
    private TextView firstContact_textView, secondContact_textView, thirdContact_textView;
    private Button add_contacts_btn1, add_contacts_btn2, add_contacts_btn3, btn_back3, delete_contacts_btn1, delete_contacts_btn2, delete_contacts_btn3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);


        firstContact_editText = findViewById(R.id.firstContact_editText);
        secondContact_editText = findViewById(R.id.secondContact_editText);
        thirdContact_editText = findViewById(R.id.thirdContact_editText);

        firstContact_textView = findViewById(R.id.firstContact_textView);
        secondContact_textView = findViewById(R.id.secondContact_textView);
        thirdContact_textView = findViewById(R.id.thirdContact_textView);

        add_contacts_btn1 = findViewById(R.id.add_contacts_btn1);
        add_contacts_btn2 = findViewById(R.id.add_contacts_btn2);
        add_contacts_btn3 = findViewById(R.id.add_contacts_btn3);
        delete_contacts_btn1 = findViewById(R.id.delete_contacts_btn1);
        delete_contacts_btn2 = findViewById(R.id.delete_contacts_btn2);
        delete_contacts_btn3 = findViewById(R.id.delete_contacts_btn3);
        btn_back3 = findViewById(R.id.btn_back3);

        displayInfo();

        add_contacts_btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveInfo1();
                displayInfo();
            }
        });
        add_contacts_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                                        //Buttons that add or delete the corresponding saved contact
                saveInfo2();
                displayInfo();
            }
        });
        add_contacts_btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveInfo3();
                displayInfo();
            }
        });
        delete_contacts_btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("contactsInfo", Context.MODE_PRIVATE);
               if(sharedPreferences.contains("first_contact")) {
                   sharedPreferences.edit().remove("first_contact").apply();
                   displayInfo();
               }
            }
        });
        delete_contacts_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("contactsInfo", Context.MODE_PRIVATE);
                if(sharedPreferences.contains("second_contact")) {
                    sharedPreferences.edit().remove("second_contact").apply();
                    displayInfo();
                }
            }
        });
        delete_contacts_btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("contactsInfo", Context.MODE_PRIVATE);
                if(sharedPreferences.contains("third_contact")) {
                    sharedPreferences.edit().remove("third_contact").apply();
                    displayInfo();
                }
            }
        });
        btn_back3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                        //finish this activity and return in the previous activity that is in the activity stack
                finish();
            }
        });


    }
    //-----------------------------------------------END OF onCreate()---------------------------//

    private void saveInfo1() {
        SharedPreferences sharedPreferences = getSharedPreferences("contactsInfo", Context.MODE_PRIVATE);           //get (or create if it doesn't exist) the sharedPreferences file
        SharedPreferences.Editor editor = sharedPreferences.edit();                                                        //start editing that file
        CharSequence firstContact_sequence = firstContact_editText.getText().toString();                                    //char sequence is used in order to use the Patterns methods afterwards
        if("".equals(firstContact_editText.getText().toString())) {
            Toast.makeText(ContactsActivity.this, getString(R.string.first_contact), Toast.LENGTH_SHORT).show();
        } else {
            if(!Patterns.PHONE.matcher(firstContact_sequence).matches()) {
                Toast.makeText(ContactsActivity.this, getString(R.string.correct_phoneNumber), Toast.LENGTH_SHORT).show();
            } else {
                editor.putString("first_contact", firstContact_editText.getText().toString());
                editor.apply();
                Toast.makeText(ContactsActivity.this, getString(R.string.first_contact_added), Toast.LENGTH_SHORT).show();
                firstContact_editText.setText(R.string.empty_textView);
            }
        }
    }
    //same method as above for the second contact
    private void saveInfo2() {
        SharedPreferences sharedPreferences = getSharedPreferences("contactsInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        CharSequence secondContact_sequence = secondContact_editText.getText().toString();
        if ("".equals(secondContact_editText.getText().toString())) {
            Toast.makeText(ContactsActivity.this, getString(R.string.second_contact), Toast.LENGTH_SHORT).show();
        } else {
            if(!Patterns.PHONE.matcher(secondContact_sequence).matches()) {
                Toast.makeText(ContactsActivity.this, getString(R.string.correct_phoneNumber), Toast.LENGTH_SHORT).show();
            } else {
                editor.putString("second_contact", secondContact_editText.getText().toString());
                editor.apply();
                Toast.makeText(ContactsActivity.this, getString(R.string.second_contact_added), Toast.LENGTH_SHORT).show();
                secondContact_editText.setText(R.string.empty_textView);
            }
        }
    }
    //same method as above for the third contact
    private void saveInfo3() {
        SharedPreferences sharedPreferences = getSharedPreferences("contactsInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        CharSequence thirdContact_sequence = thirdContact_editText.getText().toString();
        if ("".equals(thirdContact_editText.getText().toString())) {
            Toast.makeText(ContactsActivity.this, getString(R.string.third_contact), Toast.LENGTH_SHORT).show();
        } else {
            if(!Patterns.PHONE.matcher(thirdContact_sequence).matches()) {
                Toast.makeText(ContactsActivity.this, getString(R.string.correct_phoneNumber), Toast.LENGTH_SHORT).show();
            } else {
                editor.putString("third_contact", thirdContact_editText.getText().toString());
                editor.apply();
                Toast.makeText(ContactsActivity.this, getString(R.string.third_contact_added), Toast.LENGTH_SHORT).show();
                thirdContact_editText.setText(R.string.empty_textView);
            }
        }
    }

    private void displayInfo() {
        //this method runs in the onCreate()
        //it sets the textViews texts with phone number of each saved contact if it exists
        SharedPreferences sharedPreferences = getSharedPreferences("contactsInfo", Context.MODE_PRIVATE);
        if(sharedPreferences.contains("first_contact")) {
            String first = sharedPreferences.getString("first_contact", "");
            firstContact_textView.setText(getString(R.string.added_contact,first));
        } else {
            firstContact_textView.setText(R.string.empty_textView);
        }
        if(sharedPreferences.contains("second_contact")) {
            String second = sharedPreferences.getString("second_contact", "");
            secondContact_textView.setText(getString(R.string.added_contact,second));
        }else {
            secondContact_textView.setText(R.string.empty_textView);
        }
        if(sharedPreferences.contains("third_contact")) {
            String third = sharedPreferences.getString("third_contact", "");
            thirdContact_textView.setText(getString(R.string.added_contact,third));
        }else {
            thirdContact_textView.setText(R.string.empty_textView);
        }
    }
}
