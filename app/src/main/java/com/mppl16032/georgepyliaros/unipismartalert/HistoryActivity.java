package com.mppl16032.georgepyliaros.unipismartalert;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView listView1;
    private Button btn_back4;
    private DatabaseReference dref;
    private SimpleAdapter sa;
    private ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();           //an arrayList of HashMaps
    private ValueEventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        listView1 = findViewById(R.id.listView1);

        btn_back4 = findViewById(R.id.btn_back4);
        btn_back4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                dref.removeEventListener(eventListener);
            }
        });


        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();                    //get the current user from firebase
        if(current_user != null) {
            final List<UniqueEvent> uniqueEventsList = new ArrayList<>();                   //make a list of "uniqueEvents" meaning unique entries in the users corresponding database branch
            String uid = current_user.getUid();                                                 //get the uid of the user to get access to his database branch to get the events
            dref = FirebaseDatabase.getInstance().getReference(uid);
            eventListener =dref.orderByChild("Date").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()) {                         //for each unique key(which is basically a separate event) get all the children (all the events infos
                        String date = uniqueKeySnapshot.child("Date").getValue().toString();
                        String event = uniqueKeySnapshot.child("Event").getValue().toString();
                        String value = uniqueKeySnapshot.child("Value").getValue().toString();
                        uniqueEventsList.add(new UniqueEvent(date,event,value));                    //save all infos to the list as triples
                    }

                    HashMap<String, String> item;                       //this HashMap will contain three strings in each entry
                    for(UniqueEvent myList : uniqueEventsList){                 //using the class created at the end of this code page we get easily get all the values saved in the uniqueEventsList
                        item = new HashMap<String, String>();
                        item.put( "line1", myList.date);
                        item.put( "line2", myList.event);
                        item.put( "line3", myList.value);
                        list.add( item );                       //ad the the HashMap entry to the arrayList of HashMaps already created
                    }
                    sa.notifyDataSetChanged();                    //notify the adapter of the changes that need to be implemented

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        sa = new SimpleAdapter(HistoryActivity.this, list,
                R.layout.list_view_elements,
                new String[] { "line1","line2","line3" },
                new int[] {R.id.singleElementsTextView1, R.id.singleElementsTextView2, R.id.singleElementsTextView3});
        listView1.setAdapter(sa);









    }
    //-----------------------------------------------END OF onCreate------------------------------------------------------//
    public class UniqueEvent {
        private String date;
        private String event;
        private String value;

        UniqueEvent(String a, String b, String c) {
            this.date = a;
            this.event = b;
            this.value = c;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();              //when user presses back close this activity and remover the eventListener of firebase because if not removed when a new event occurs the app crashes
        finish();
        dref.removeEventListener(eventListener);
    }
}
