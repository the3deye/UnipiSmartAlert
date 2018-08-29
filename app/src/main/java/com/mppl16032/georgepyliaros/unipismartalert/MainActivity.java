package com.mppl16032.georgepyliaros.unipismartalert;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mppl16032.georgepyliaros.unipismartalert.AccountActivity.AccountManagement;
import com.mppl16032.georgepyliaros.unipismartalert.AccountActivity.ContactsActivity;
import com.mppl16032.georgepyliaros.unipismartalert.AccountActivity.LoginActivity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    public static final int MY_PERMISSIONS_REQUEST_SMS = 0;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final long START_TIME_IN_MILLIS = 30000;
    private static final long START_TIME_IN_MILLIS2 = 10000;
    private static final long START_TIME_IN_MILLIS3 = 60000;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;

    private static final int TIME_LIMIT = 2000;     /* For second press of back button to exit application*/

    private static long backPressed;
    private long mTImeLeftInMillis = START_TIME_IN_MILLIS;
    private long mTImeLeftInMillis2 = START_TIME_IN_MILLIS2;
    private long mTImeLeftInMillis3 = START_TIME_IN_MILLIS3;
    private long mTImeLeftInMillis4 = START_TIME_IN_MILLIS;


    private Button btn_manage_account, btn_sign_out1, btn_sos, btn_abort, btn_abort2, btn_contacts, btn_history;
    private ImageButton imageButtonSpeakNow;
    private TextView z_acceleration, light_textView, timer_textView, speed_textView,gps_textView;
    private FirebaseAuth auth;
    private SensorManager sensorManager;
    private Sensor accelerometer, light;
    private CountDownTimer countDownTimer,countDownTimer2, countDownTimer3, countDownTimer4;
    private boolean mTimerRunning,mTimerRunning2, mTimerRunning3, mTimerRunning4;
    private TextToSpeech tts1,tts2;
    private LocationManager locationManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_manage_account = findViewById(R.id.btn_manage_account);
        btn_sign_out1 = findViewById(R.id.btn_sign_out1);
        btn_abort = findViewById(R.id.btn_abort);
        btn_abort2 = findViewById(R.id.btn_abort2);
        btn_sos = findViewById(R.id.btn_sos);
        btn_contacts = findViewById(R.id.btn_contacts);
        btn_history = findViewById(R.id.btn_history);

        imageButtonSpeakNow = findViewById(R.id.imageButtonSpeakNow);

        z_acceleration = findViewById(R.id.z_acceleration);
        light_textView = findViewById(R.id.light_textView);
        timer_textView = findViewById(R.id.timer_textView);
        speed_textView = findViewById(R.id.speed_textView);
        gps_textView = findViewById(R.id.gps_textView);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        this.onLocationChanged(null);

        auth = FirebaseAuth.getInstance();

        btn_abort.setVisibility(View.GONE);         /* Some buttons and textViews appear at certain conditions*/
        btn_abort2.setVisibility(View.GONE);
        gps_textView.setVisibility(View.GONE);




        //------------------------------PERMISSIONS----------------------------------------------//
        // When the app starts check if permissions to use location of user are granted...if they are not prompt the user (if possible) to grant the permission//
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 0, this);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                showGPSDisabledAlertToUser();
            }
        } else {
            checkLocationPermission();
        }
        //--------------------------------------------------------------------------------------//



        //---------------------------------SENSORS-------------------------------------------------------------//
        // Check availability of sensors if not supported inform the user through a text message in the corresponding area//

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            z_acceleration.setText(R.string.accel_not_supported);
        }

        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (light != null) {
            sensorManager.registerListener(lightSensorListener, light, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            light_textView.setText(R.string.light_not_supported);
        }
        //------------------------------------------------------------------------------------------------------//


        //---------------------------------BUTTON LISTENERS--------------------------------------//
        // Here all button listeners are organized!//

        //Open Account activity when you can make changes to your account//
        btn_manage_account.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, AccountManagement.class);
                    startActivity(intent);
                }
            });

        //Sign out Button//
        btn_sign_out1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        // Clicking on this button activates voice recognition//
        imageButtonSpeakNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckVoiceRecognition();
            }
        });

        // This button gives thew choice to the user to open his history activity in two ways either as a map of his last ten events or as list ordered by date//
        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setMessage("Open a map of your latest events or a list of all your events?")
                        .setNeutralButton("Maps",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if(isServicesOK()) {
                                            if (ContextCompat.checkSelfPermission(MainActivity.this,
                                                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                                                    == PackageManager.PERMISSION_GRANTED) {
                                                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(MainActivity.this, "Cant open map because you denied permission", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    }
                                });
                alertDialogBuilder.setPositiveButton("List",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                                startActivity(intent);
                            }
                        });
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
            }
        });

        //Button that user presses if he is in need of help
        btn_sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    showGPSDisabledAlertToUser();
                    return;
                }
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setMessage("Are you sure you want to send an SOS message?")
                        .setPositiveButton("YES",
                                new DialogInterface.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int id){
                                        sendSos();

                                    }
                                });
                alertDialogBuilder.setNegativeButton("NO",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
            }
        });

        //Opens contact activity where user can add up to three contacts to inform by message when he is in danger
        btn_contacts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
                    startActivity(intent);
                }
            });

        //---------------------------------------------------------------------------------------//


    }
    //----------------------------------------END OF onCreate()---------------------------------------------//


    //---------------------------------------signOut()------------------------------------------//
    //Signs out user
    public void signOut() {
        auth.signOut();
    }
    //-------------------------------------------------------------------------------------------//


    //-----------------------------------FIREBASE LISTENER---------------------------------------//
    /*listener that basically  checks if user signs out.
    When user presses the sign out button and the signOut() method runs this listener will clear the stack of open activities and move the user to the Login activity*/
    FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @SuppressLint("SetTextI18n") //suppress lint check warnings --not for users but for those pesky warnings we are getting
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // user auth state is changed - user is null
                // launch login activity
                Intent signOutIntent = new Intent(MainActivity.this, LoginActivity.class);
                signOutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(signOutIntent);
                finish();
            }
        }
    };
    //-------------------------------------------------------------------------------------------//


    //------------------------------------TIMERS--------------------------------------------------//
    //Here all timers are organized!

    /* This timer starts working when a fall event happens.It has an update mechanism that keeps track of the time left
     and shows it to the user. In every tick of the timer (every second) a sound is played through mediaPlayer and onFinish there is a clean up of several things
     like the return to default state of visibility of some of the buttons, the restart of the accelerometer and running some other methods */
    private void startTimer() {
        final MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.button_sound);
        countDownTimer = new CountDownTimer(mTImeLeftInMillis, 1000) {
            @Override
            public void onTick(long l) {

                mTImeLeftInMillis = l;
                updateCountDownText();
                mediaPlayer.start();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                mediaPlayer.stop();
                btn_manage_account.setVisibility(View.VISIBLE);
                btn_contacts.setVisibility(View.VISIBLE);
                btn_sos.setVisibility(View.VISIBLE);
                btn_abort2.setVisibility(View.GONE);
                sendSos2();
                resetTimer();
                if(accelerometer !=null) {
                    sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL );
                } else {
                    z_acceleration.setText(R.string.accel_not_supported);
                }
            }
        }.start();
    }
    //Basically stops the timer but in the above method we are saving in every tick the time left in the timer so in the end it acts as a pause!
    //When we start the timer again since we have not reset it the timer starts from where it was paused
    private void pauseTimer() {
        countDownTimer.cancel();
    }

    //This method runs when timer finishes and resets the timer. If this process did not run after the timer finished then the next it started the countdown
    //would start from zero seconds!
    private void resetTimer() {
        mTImeLeftInMillis = START_TIME_IN_MILLIS;
        updateCountDownText();
        countDownTimer.cancel();
        timer_textView.setVisibility(View.GONE);

    }

    //This method is responsible for updating the textView that shows the user how much time he has left to send an abort message (press the now visible abort button)
    private void updateCountDownText() {
        int minutes = (int) (mTImeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTImeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timer_textView.setVisibility(View.VISIBLE);
        timer_textView.setText(timeLeftFormatted);

    }

    //This timer starts when user is running with more that 80 km/h
    //when that happens the location listener is stopped for some time basically for the duration of this timer.
    //When the timer finishes the Location listener is reactivated.
    //This method exists in order too avoid flooding the database with the same event many times!
    private void startLocationTimer () {
        countDownTimer2 = new CountDownTimer(mTImeLeftInMillis2,1000) {
            @Override
            public void onTick(long l) {
                mTImeLeftInMillis2 =l;
            }

            @Override
            public void onFinish() {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 0, MainActivity.this);
                }
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    showGPSDisabledAlertToUser();
                }
                mTimerRunning2 = false;
                mTImeLeftInMillis2 = START_TIME_IN_MILLIS2;
                Toast.makeText(getApplicationContext(), "LocationTimer finished", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }
    private void pauseLocationTimer () {
        countDownTimer2.cancel();
    }

    //This timer starts when too much solar radiation is hitting the users mobile phone.
    // Just like the last timer it exists to restart the light sensor
    //In this and the last timer there is no resetTimer method because what's needed is done in the onFinish()
    private void startLightTimer () {
        countDownTimer3 = new CountDownTimer(mTImeLeftInMillis3,1000) {
            @Override
            public void onTick(long l) {
                mTImeLeftInMillis3 =l;
            }

            @Override
            public void onFinish() {
                mTimerRunning3 = false;
                sensorManager.registerListener(lightSensorListener, light, SensorManager.SENSOR_DELAY_NORMAL );
                mTImeLeftInMillis3 = START_TIME_IN_MILLIS3;
            }
        }.start();
    }

    private void pauseLightTimer () {
        countDownTimer3.cancel();
    }

    //This timer starts when the user presses the SOS button.
    //Until the timer finishes an abort button is visible for the user to press if he is ok after all and wants to inform his contacts about it
    private void startGeneralTimer () {
        countDownTimer4 = new CountDownTimer(mTImeLeftInMillis4,1000) {
            @Override
            public void onTick(long l) {
                mTImeLeftInMillis4 =l;
            }

            @Override
            public void onFinish() {
                mTimerRunning4 = false;
                mTImeLeftInMillis4 = START_TIME_IN_MILLIS;
                btn_abort.setVisibility(View.GONE);
            }
        }.start();
    }

    private void pauseGeneralTimer () {
        countDownTimer4.cancel();
    }


    //-------------------------------------------------------------------------------------------//

    //-----------------------------------------SENSORS--------------------------------------------------//
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //ACCELEROMETER
        //if the sensor event detected is of accelerometer type then the following process runs
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float z = sensorEvent.values[2]; //The values of the z axis.....since keeping track of the z axis depending on the orientation of the users phone proved futile this will have to suffice
            String z_string = Float.toString(z);

            z_acceleration.setText(z_string);
            if (z > 30) {
                mTimerRunning = true;
                startTimer();   //start the timer that unregisters the sensor listener
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)                   //check for location permissions
                        == PackageManager.PERMISSION_GRANTED) {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(location!=null) {                                            //if location permissions granted...
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String stringLatitude = String.valueOf(latitude);
                        String stringLongitude = String.valueOf(longitude);
                        String combineLocation = "Latitude = " + stringLatitude + " and Longitude = " + stringLongitude;

                        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                        if(current_user != null) {
                            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                            String uid = current_user.getUid();                                                 //We get the unique id of the user in order to save the event in his own
                            DatabaseReference fall_ref = FirebaseDatabase.getInstance().getReference(uid);      //personal "file" of events
                            String key = fall_ref.push().getKey();                                              //we get a random key generated by firebase so that we can easily keep each events
                            String combine = "Acceleration = " + z_string;                                      //informations in one place and easily get those infos afterwards
                            fall_ref.child(key).child("Date").setValue(currentDateTimeString);
                            fall_ref.child(key).child("Event").setValue("Fall Event");
                            fall_ref.child(key).child("Location").setValue(combineLocation);
                            fall_ref.child(key).child("Value").setValue(combine);
                        }
                    }
                    else {
                        //if gps is not working inform the user
                        Toast.makeText(MainActivity.this, "Fall event was not saved because GPS is not working!", Toast.LENGTH_SHORT).show();
                    }
                }

                btn_abort2.setVisibility(View.VISIBLE);
                btn_contacts.setVisibility(View.GONE);
                btn_manage_account.setVisibility(View.GONE);
                btn_sos.setVisibility(View.GONE);
                sensorManager.unregisterListener(MainActivity.this);
                abortNoMessage(); //activate abort button listener (this button is made visible inside the startTimer() methos
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    //I could keep going and use if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) but first
    //each time the sensor stops it would stop for all events and second this gave me the chance to
    // try this way too
    private final SensorEventListener lightSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
                double light_value = sensorEvent.values[0];
                light_textView.setText("" +light_value);

                if (light_value > 15000) {
                    light_textView.setTextColor(getResources().getColor(R.color.red));
                } else {
                    light_textView.setTextColor(getResources().getColor(R.color.black));
                }
                if (light_value >20000) {
                    sensorManager.unregisterListener(lightSensorListener);          //This time we unregister the light sensor here instead of the timer
                    TTS2();                                                     //text to speech method that informs the user to stay in a place with less light
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);         //Same process as in the first sensor
                        if(location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            String stringLatitude = String.valueOf(latitude);
                            String stringLongitude = String.valueOf(longitude);
                            String combineLocation = "Latitude = " + stringLatitude + " and Longitude = " + stringLongitude;

                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            if(current_user != null) {
                                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                                String uid = current_user.getUid();
                                DatabaseReference light_ref = FirebaseDatabase.getInstance().getReference(uid);
                                String key = light_ref.push().getKey();
                                String combine = "Light Value = " + light_value;
                                light_ref.child(key).child("Date").setValue(currentDateTimeString);
                                light_ref.child(key).child("Event").setValue("Light Event");
                                light_ref.child(key).child("Location").setValue(combineLocation);
                                light_ref.child(key).child("Value").setValue(combine);

                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Light event was not saved because GPS is not working!", Toast.LENGTH_SHORT).show();
                        }
                    }


                    mTimerRunning3 = true;
                    startLightTimer();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
    //---------------------------------------------------------------------------------------------//

    //------------------------------------ABORT BUTTONS------------------------------------------//

    //This abort button listener is activated when the mobile phone "falls"
    //until the timer ends if the user presses the button it opens the AbortActivityFall that
    //checks again his credentials and if correct does not send the sos message that will be sent when the timer ends
    private void abortNoMessage() {
        btn_abort2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AbortActivityFall.class);
                startActivity(intent);
            }
        });
    }

    //this abort button listener is activated when the user presses the SOS button and until the corresponding timer ends if pressed
    // it will open the abortActivity that will check his credentials again and if correct will send an abort message to his contacts
    private void abortMessage() {
        btn_abort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AbortActivity.class);
                startActivity(intent);
            }
        });
    }
    //-------------------------------------------------------------------------------------------//

    //--------------------------------------------------SEND SOS--------------------------------------------------------------------------------//
    //This method runs when the user presses the sos Button
    private void sendSos() {
        if(checkSMSPermission()) {              //Checks for sms permissions
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {                                                         //checks for location usage permissions
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);              //sets up location managers
                Location location2 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(location!=null || location2!=null)                           //if either manager is working
                {
                    SmsManager smsManager = SmsManager.getDefault();                    // sets up the sms manager
                    SharedPreferences sharedPreferences = getSharedPreferences("contactsInfo", Context.MODE_PRIVATE);   //gets the saved contacts of the user

                    if(location!=null) {    // if gps provider can give location
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String stringLatitude = String.valueOf(latitude);
                        String stringLongitude = String.valueOf(longitude);
                        String combineLocation = "Latitude = " + stringLatitude + " and Longitude = " + stringLongitude;

                        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                        if(current_user != null) {

                            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                            String uid = current_user.getUid();
                            DatabaseReference sosButton_ref = FirebaseDatabase.getInstance().getReference(uid);             //Saves the needed informations of the event the same way it happened earlier
                            String key = sosButton_ref.push().getKey();
                            sosButton_ref.child(key).child("Date").setValue(currentDateTimeString);
                            sosButton_ref.child(key).child("Event").setValue("SOS Button");
                            sosButton_ref.child(key).child("Location").setValue(combineLocation);
                            sosButton_ref.child(key).child("Value").setValue(combineLocation);

                        }
                        //Sends an sms to every (if any) saved contact of the user
                        if(sharedPreferences.contains("first_contact") || sharedPreferences.contains("second_contact") || sharedPreferences.contains("third_contact")) {
                            btn_abort.setVisibility(View.VISIBLE);
                            mTimerRunning4 = true;
                            startGeneralTimer();
                            abortMessage();
                            if(sharedPreferences.contains("first_contact")) {
                                String first = sharedPreferences.getString("first_contact", "");
                                String phoneNo1 = "+30"+first;
                                String message = "I need help! I am at https://www.google.com/maps/@"+stringLatitude+","+stringLongitude+",15z";    //a url that can be pressed and show location of user
                                smsManager.sendTextMessage(phoneNo1,null, message, null, null);
                                Toast.makeText(getApplicationContext(), "First contact SMS sent", Toast.LENGTH_SHORT).show();
                            }
                            if(sharedPreferences.contains("second_contact")) {
                                String second = sharedPreferences.getString("second_contact", "");
                                String phoneNo2 = "+30"+second;
                                String message = "I need help! I am at https://www.google.com/maps/@"+stringLatitude+","+stringLongitude+",15z";
                                smsManager.sendTextMessage(phoneNo2,null, message, null, null);
                                Toast.makeText(getApplicationContext(), "Second contact SMS sent", Toast.LENGTH_SHORT).show();
                            }
                            if(sharedPreferences.contains("third_contact")) {
                                String third = sharedPreferences.getString("third_contact", "");
                                String phoneNo3 = "+30"+third;
                                String message = "I need help! I am at https://www.google.com/maps/@"+stringLatitude+","+stringLongitude+",15z";
                                smsManager.sendTextMessage(phoneNo3,null, message, null, null);
                                Toast.makeText(getApplicationContext(), "Third contact SMS sent", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(),"No contacts added!", Toast.LENGTH_SHORT).show();
                        }

                    } else {            //else get the location from the network provider
                        double latitude = location2.getLatitude();
                        double longitude = location2.getLongitude();
                        String stringLatitude = String.valueOf(latitude);
                        String stringLongitude = String.valueOf(longitude);
                        String combineLocation = "Latitude = " + stringLatitude + " and Longitude = " + stringLongitude;

                        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                        if(current_user != null) {

                            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                            String uid = current_user.getUid();
                            DatabaseReference sosButton_ref = FirebaseDatabase.getInstance().getReference(uid);
                            String key = sosButton_ref.push().getKey();
                            sosButton_ref.child(key).child("Date").setValue(currentDateTimeString);
                            sosButton_ref.child(key).child("Event").setValue("SOS Button");
                            sosButton_ref.child(key).child("Location").setValue(combineLocation);
                            sosButton_ref.child(key).child("Value").setValue(combineLocation);

                        }

                        if(sharedPreferences.contains("first_contact") || sharedPreferences.contains("second_contact") || sharedPreferences.contains("third_contact")) {
                            btn_abort.setVisibility(View.VISIBLE);
                            mTimerRunning4 = true;
                            startGeneralTimer();
                            abortMessage();
                            if(sharedPreferences.contains("first_contact")) {
                                String first = sharedPreferences.getString("first_contact", "");
                                String phoneNo1 = "+30"+first;
                                String message = "I need help! I am at https://www.google.com/maps/@"+stringLatitude+","+stringLongitude+",15z";
                                smsManager.sendTextMessage(phoneNo1,null, message, null, null);
                                Toast.makeText(getApplicationContext(), "First contact SMS sent", Toast.LENGTH_SHORT).show();
                            }
                            if(sharedPreferences.contains("second_contact")) {
                                String second = sharedPreferences.getString("second_contact", "");
                                String phoneNo2 = "+30"+second;
                                String message = "I need help! I am at https://www.google.com/maps/@"+stringLatitude+","+stringLongitude+",15z";
                                smsManager.sendTextMessage(phoneNo2,null, message, null, null);
                                Toast.makeText(getApplicationContext(), "Second contact SMS sent", Toast.LENGTH_SHORT).show();
                            }
                            if(sharedPreferences.contains("third_contact")) {
                                String third = sharedPreferences.getString("third_contact", "");
                                String phoneNo3 = "+30"+third;
                                String message = "I need help! I am at https://www.google.com/maps/@"+stringLatitude+","+stringLongitude+",15z";
                                smsManager.sendTextMessage(phoneNo3,null, message, null, null);
                                Toast.makeText(getApplicationContext(), "Third contact SMS sent", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(),"No contacts added!",Toast.LENGTH_SHORT).show();
                        }
                    }
                    TTS();
                } else {
                    Toast.makeText(getApplicationContext(), "There is a problem with your GPS provider!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(),"Restart app and give permission to use GPS!",Toast.LENGTH_LONG).show();         //Inform the user of what problems occured or what he has to do
            }
        } else {
            Toast.makeText(getApplicationContext(),"Restart app and give permission to send sms!", Toast.LENGTH_LONG).show();
        }

    }
    //This method runs when a fall event occurs... its the same as the above but because the event information storing in the database was done elsewhere i had to create a separate method that
    //only sends messages to avoid some work
    private void sendSos2() {
        if(checkSMSPermission()) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Location location2 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(location!=null || location2!=null)
                {
                    SmsManager smsManager = SmsManager.getDefault();
                    SharedPreferences sharedPreferences = getSharedPreferences("contactsInfo", Context.MODE_PRIVATE);
                    if(location!=null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String stringLatitude = String.valueOf(latitude);
                        String stringLongitude = String.valueOf(longitude);

                        if(sharedPreferences.contains("first_contact") || sharedPreferences.contains("second_contact") || sharedPreferences.contains("third_contact")) {
                            if(sharedPreferences.contains("first_contact")) {
                                String first = sharedPreferences.getString("first_contact", "");
                                String phoneNo1 = "+30"+first;
                                String message = "I need help! I am at https://www.google.com/maps/@"+stringLatitude+","+stringLongitude+",15z";
                                smsManager.sendTextMessage(phoneNo1,null, message, null, null);
                                Toast.makeText(getApplicationContext(), "First contact SMS sent", Toast.LENGTH_SHORT).show();
                            }
                            if(sharedPreferences.contains("second_contact")) {
                                String second = sharedPreferences.getString("second_contact", "");
                                String phoneNo2 = "+30"+second;
                                String message = "I need help! I am at https://www.google.com/maps/@"+stringLatitude+","+stringLongitude+",15z";
                                smsManager.sendTextMessage(phoneNo2,null, message, null, null);
                                Toast.makeText(getApplicationContext(), "Second contact SMS sent", Toast.LENGTH_SHORT).show();
                            }
                            if(sharedPreferences.contains("third_contact")) {
                                String third = sharedPreferences.getString("third_contact", "");
                                String phoneNo3 = "+30"+third;
                                String message = "I need help! I am at https://www.google.com/maps/@"+stringLatitude+","+stringLongitude+",15z";
                                smsManager.sendTextMessage(phoneNo3,null, message, null, null);
                                Toast.makeText(getApplicationContext(), "Third contact SMS sent", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(),"No contacts added!",Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        double latitude = location2.getLatitude();
                        double longitude = location2.getLongitude();
                        String stringLatitude = String.valueOf(latitude);
                        String stringLongitude = String.valueOf(longitude);

                        if(sharedPreferences.contains("first_contact") || sharedPreferences.contains("second_contact") || sharedPreferences.contains("third_contact")) {
                            if(sharedPreferences.contains("first_contact")) {
                                String first = sharedPreferences.getString("first_contact", "");
                                String phoneNo1 = "+30"+first;
                                String message = "I need help! I am at https://www.google.com/maps/@"+stringLatitude+","+stringLongitude+",15z";
                                smsManager.sendTextMessage(phoneNo1,null, message, null, null);
                                Toast.makeText(getApplicationContext(), "First contact SMS sent", Toast.LENGTH_SHORT).show();
                            }
                            if(sharedPreferences.contains("second_contact")) {
                                String second = sharedPreferences.getString("second_contact", "");
                                String phoneNo2 = "+30"+second;
                                String message = "I need help! I am at https://www.google.com/maps/@"+stringLatitude+","+stringLongitude+",15z";
                                smsManager.sendTextMessage(phoneNo2,null, message, null, null);
                                Toast.makeText(getApplicationContext(), "Second contact SMS sent", Toast.LENGTH_SHORT).show();
                            }
                            if(sharedPreferences.contains("third_contact")) {
                                String third = sharedPreferences.getString("third_contact", "");
                                String phoneNo3 = "+30"+third;
                                String message = "I need help! I am at https://www.google.com/maps/@"+stringLatitude+","+stringLongitude+",15z";
                                smsManager.sendTextMessage(phoneNo3,null, message, null, null);
                                Toast.makeText(getApplicationContext(), "Third contact SMS sent", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(),"No contacts added!",Toast.LENGTH_SHORT).show();
                        }
                    }
                    TTS();
                } else {
                    Toast.makeText(getApplicationContext(), "There is a problem with your GPS provider!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(),"Restart app and give permission to use GPS!",Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(),"Restart app and give permission to send sms!", Toast.LENGTH_LONG).show();
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------//


    //--------------------------------------------------------TEXT TO SPEECH --VOICE RECOGNITION-----------------------------------------------------------------------------------------------//
    //Text to speech method
    public void TTS() {
        //--------------------------------TEXT TO SPEECH-----------------------------------------//
        tts1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) { //if during initialize no error occurs
                    int language = tts1.setLanguage(Locale.UK);         //set the language to UK english
                    if(language ==TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED) {      //if for any reason the language is not supported
                        Toast.makeText(MainActivity.this, "Language missing", Toast.LENGTH_SHORT).show();   //inform the user
                    } else {
                        final String text = "Keep Calm! Your contacts have been informed of your location. Help is on the way!     ";   //else create this message that is to be spoken
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            tts1.speak(text,TextToSpeech.QUEUE_ADD,null,null);
                            tts1.speak(text,TextToSpeech.QUEUE_ADD,null,null);          //Checks the android version of the mobile phone
                                                                                                            // if its Lollipop or after use the newer version of speak method
                        } else {
                            tts1.speak(text, TextToSpeech.QUEUE_ADD, null);                         //else use the deprecated one
                            tts1.speak(text, TextToSpeech.QUEUE_ADD, null);
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Text to Speech not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //---------------------------------------------------------------------------------------//
    }
    public void TTS2() {
        //same as above but for light event informing
        //--------------------------------TEXT TO SPEECH-----------------------------------------//
        tts2 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    int language = tts2.setLanguage(Locale.UK);
                    if (language == TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(MainActivity.this, "Language missing", Toast.LENGTH_SHORT).show();
                    } else {
                        final String text = "Too much solar radiation please avoid the sun for sometime!";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            tts2.speak(text, TextToSpeech.QUEUE_ADD, null, null);
                            tts2.speak(text, TextToSpeech.QUEUE_ADD, null, null);

                        } else {
                            tts2.speak(text, TextToSpeech.QUEUE_ADD, null);
                            tts2.speak(text, TextToSpeech.QUEUE_ADD, null);
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Text to Speech not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//------------------------------------------------VOICE RECOGNITION--------------------------------------------------------//

    public void CheckVoiceRecognition() {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0); //Check if voice recognition is supported in the users phone
        if(activities.size()==0) {
            Toast.makeText(MainActivity.this, "Voice recognizer not present!", Toast.LENGTH_LONG).show();
        }else {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);                                   //if its supported start the recognizer intent
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);       //as an extra in the intent the recognizer should use a specific language model
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);                                     //start the activity (different from normal startActivity because it returns something too)
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){                  //That something is requested here and if its returned the data gathered
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE){                                         //from the voice recognition activity are accessed
            if (resultCode == RESULT_OK){
                ArrayList<String> matches = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (matches.size() == 0) {                                                              //if there is nothing in the list
                    Toast.makeText(MainActivity.this, "Didn't hear anything", Toast.LENGTH_LONG).show();    //inform the user
                } else {
                    String mostLikelyThingHeard = matches.get(0);               //else check if the what the user said matches any of our cases
                    // toUpperCase() used to make string comparison equal
                    switch (mostLikelyThingHeard.toUpperCase()) {
                        case "CONTACTS":
                            startActivity(new Intent(MainActivity.this, ContactsActivity.class));
                            break;
                        case "HISTORY LIST":
                            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                            break;
                        case "HISTORY MAP":
                            startActivity(new Intent(MainActivity.this, MapsActivity.class));
                            break;
                        default:
                            Toast.makeText(MainActivity.this, "Wrong command", Toast.LENGTH_LONG).show(); //else inform user that he did not give the right command
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------//
//-----------------------------------------LOCATION LISTENER AND GPS CHECK--------------------------------------------------------------------------------------------------//


    @Override
    public void onLocationChanged(Location location) {
        double currentSpeed, latitude, longitude;
        int currentIntSpeed;

        if (location != null) {                             //basically if the gps provider can provide a location

            currentSpeed = location.getSpeed()*3.6;         //convert m/s to Km/h
            currentIntSpeed = (int) currentSpeed;
            speed_textView.setText(getString(R.string.current_speed,currentIntSpeed));

            if (currentSpeed >80) {                         //if speed is over 80 Km/h save the location coordinates in a string

                latitude = location.getLatitude();
                longitude = location.getLongitude();
                String stringLatitude = String.valueOf(latitude);
                String stringLongitude = String.valueOf(longitude);
                String combineLocation = "Latitude = " + stringLatitude + " and Longitude = " + stringLongitude;
                mTimerRunning2=true;
                locationManager.removeUpdates(MainActivity.this);

                FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                if(current_user != null) {
                    String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());         //save the location string along with some other infos in the unique child created for the
                    String uid = current_user.getUid();                                                         //specific user
                    DatabaseReference speed_ref = FirebaseDatabase.getInstance().getReference(uid);
                    String key = speed_ref.push().getKey();
                    String combine = "Speed = " +currentIntSpeed;
                    speed_ref.child(key).child("Date").setValue(currentDateTimeString);
                    speed_ref.child(key).child("Event").setValue("Speed Event");
                    speed_ref.child(key).child("Location").setValue(combineLocation);
                    speed_ref.child(key).child("Value").setValue(combine);

                }
                startLocationTimer();
            }

        }else {
            speed_textView.setText("-.-");
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void showGPSDisabledAlertToUser(){
       Toast.makeText(getApplicationContext(),R.string.gps_disabled,Toast.LENGTH_LONG).show();          //this class informs the user that gps is enabled and makes visible a textView that says the same thing
        gps_textView.setVisibility(View.VISIBLE);
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------------------//

    //---------------------------------------------------SMS PERMISSION CHECK--------------------------//

    public boolean checkSMSPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_sms_permission)
                        .setMessage(R.string.text_sms_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.SEND_SMS},
                                        MY_PERMISSIONS_REQUEST_SMS);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SMS);
            }
            return false;
        }
        return true;
    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_SMS);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SMS);
            }

        }
    }

    //-------------------------------------------------------------------------------------------------//

    //-------------------------------------------GOOGLE PLAY SERVICES-------------------------------------------------//
    public boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);   //check if google services are available in the users mobile phone
        if (available == ConnectionResult.SUCCESS) {
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)) {                     //check if the users problem is resolvable through for example an aupdate
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else {
            Toast.makeText(MainActivity.this, "You can't make map requests", Toast.LENGTH_LONG).show();     //if all fails inform user through toast message
        }
        return false;
    }
    //--------------------------------------------------------------------------------------------------//
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);            //every time activity starts start the authentication listener
    }

    public void onPause() {                             //whenever the activity pauses stop all listeners, timers and in general anything that can still run

        super.onPause();

        if(tts1 !=null) {
            tts1.stop();
            tts1.shutdown();
        }

        if(tts2 !=null) {
            tts2.stop();
            tts2.shutdown();
        }

        if(mTimerRunning) {
            pauseTimer();
        }

        if(mTimerRunning2) {
            pauseLocationTimer();
        }
        if(mTimerRunning3) {
            pauseLightTimer();
        }
        if(mTimerRunning4) {
            pauseGeneralTimer();
        }

        if(sensorManager!=null){
            sensorManager.unregisterListener(this);
            sensorManager.unregisterListener(lightSensorListener);
        }

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.removeUpdates(this);
        }
    }

    public void onResume() {

        super.onResume();
        //-------------------------------TIMER------------------------------//      //if timers should still be running restart them
        if(mTimerRunning) {
            startTimer();
        }
        if(mTimerRunning2) {
            startLocationTimer();
        }
        if(mTimerRunning3) {
            startLightTimer();
        }
        if(mTimerRunning4) {
            startGeneralTimer();
        }

        //-------------------------------------------------------------------//

        //----------------------------------SENSORS - LocationManager - PERMISSIONS----------------------//
        if(!mTimerRunning) {                                                            //if timers are not still running restart sensors etc
            if(accelerometer !=null) {
                sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL );
            } else {
                z_acceleration.setText(R.string.accel_not_supported);
            }
        if(!mTimerRunning3) {
            if(light !=null) {
                sensorManager.registerListener(lightSensorListener, light,SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                light_textView.setText(R.string.light_not_supported);
            }
        }
        if(!mTimerRunning2) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, this);
            }

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                showGPSDisabledAlertToUser();
            } else {
                gps_textView.setVisibility(View.GONE);
            }
        }
        }

        //----------------------------------------------------------------------------------------------------------//

    }


    public void onStop() {

        super.onStop();                                         //when the activity completely stops stop the auth listener too
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
        sensorManager.unregisterListener(this);

    }


    @Override
    public void onBackPressed() {                                               //when the user presses back for the first time a toast message appears if he presses the back button again
        if (TIME_LIMIT + backPressed > System.currentTimeMillis()) {            //fast enough (2 secs) the activity closes else the same message appears.
            signOut();                                                          //a nice idea to check if the user really wants to exit the application
        } else {
            Toast.makeText(getApplicationContext(), "Press back again to exit!", Toast.LENGTH_SHORT).show();
        }
        backPressed = System.currentTimeMillis();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {                                                                      //if the user pulls down his mobile phones notification tab and closes the gps this class will know
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){          //and make visible the textView which informs the user that the gps is closed
                gps_textView.setVisibility(View.VISIBLE);
            } else {
                gps_textView.setVisibility(View.GONE);
            }
        }
    }
}
//-------------------------------------------END OF MAIN ACTIVITY--------------------------------------------------------------------//
