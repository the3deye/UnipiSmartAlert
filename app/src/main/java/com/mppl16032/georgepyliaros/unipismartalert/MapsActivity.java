package com.mppl16032.georgepyliaros.unipismartalert;

import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference dref;
    private ValueEventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        String uid = current_user.getUid();
        dref = FirebaseDatabase.getInstance().getReference(uid);
        eventListener = dref.orderByChild("Date").limitToLast(10).addValueEventListener(new ValueEventListener() {          //limitToLast to get only the last 10 events (by date)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()) {                                         //again as in almost all activities we add a firebase listener that updates our app
                    String latLong = uniqueKeySnapshot.child("Location").getValue().toString();                             //whenever a new event occurs
                    int mid = latLong.length()/2;
                    String latitude1 = latLong.substring(0,mid);                                                        //Because in our database the Location entry contains some letters and the coordinates
                    String latitude2= latitude1.replaceAll("[^\\.0123456789]","");                      //this was a good chance to get used to manipulating strings
                    double latitude = Double.parseDouble(latitude2);                                                      //so what we do is 1)cut the string in half 2) remove all chars that are not numbers
                                                                                                                            //3) change string to double
                    String longitude1 = latLong.substring(mid);                                                             //4)do the same process fo the other half of the string
                    String longitude2 = longitude1.replaceAll("[^\\.0123456789]","");
                    double longitude = Double.parseDouble(longitude2);

                    String event = uniqueKeySnapshot.child("Event").getValue().toString();
                    String date = uniqueKeySnapshot.child("Date").getValue().toString();
                    mMap.addMarker(new MarkerOptions()                                          //create a marker using the coordinates of the event
                            .position(new LatLng(latitude,longitude))                           //giving the marker a title that is the events name pulled from our database
                            .title(event)                                                       //and adding some info for the marker which in this case is the date of the event
                            .snippet(date)
                    );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // move the camera close to home
        LatLng myPlace = new LatLng(38.0153491, 23.7920243);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myPlace));
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);


    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
        dref.removeEventListener(eventListener);
    }
}
