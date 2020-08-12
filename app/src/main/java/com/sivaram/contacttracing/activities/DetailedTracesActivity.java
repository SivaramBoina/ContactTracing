package com.sivaram.contacttracing.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sivaram.contacttracing.ContactTracingApplication;
import com.sivaram.contacttracing.R;
import com.sivaram.contacttracing.adapters.ListAdapter;
import com.sivaram.contacttracing.helpers.TracedContact;
import com.sivaram.contacttracing.sharedpref.SharedPref;
import com.sivaram.contacttracing.utils.Constants;

import java.util.ArrayList;

public class DetailedTracesActivity extends AppCompatActivity {

    Button goBackDetailed;
    ListView detailedListView;
    DatabaseReference databaseReferenceTraces;
    String findDetailedTrace = Constants.EMPTY;
    ProgressBar detailedProgressBar;
    private TextView mDetailedListTextView;
    private ArrayList<TracedContact> tracedContacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_traces);
        detailedListView=findViewById(R.id.detailed_list);
        goBackDetailed=findViewById(R.id.btn_detailed_list_go_back);
        detailedProgressBar = findViewById(R.id.progressBar_detailed_traced_contacts);
        mDetailedListTextView = findViewById(R.id.texttview_detailed_list_tip);
    }

    @Override
    protected void onResume() {
        super.onResume();
        detailedProgressBar.setVisibility(View.VISIBLE);
        mDetailedListTextView.setText(getResources().getString(R.string.detailed_tip_text_error));
        findDetailedTrace = getIntent().getStringExtra(Constants.DETAILED_TRACE_CONTACT);
        String loggedUserContact = SharedPref.getStringParams(ContactTracingApplication.getInstance(), Constants.SESSION_CONTACT, Constants.EMPTY);
        databaseReferenceTraces = FirebaseDatabase.getInstance().getReference("traces").child(loggedUserContact).child(findDetailedTrace);
        databaseReferenceTraces.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    tracedContacts.clear();
                    mDetailedListTextView.setText(getResources().getString(R.string.detailed_tip_text_success));
                    for(DataSnapshot eachSnapshot : snapshot.getChildren()){
                        //String eachTime = eachSnapshot.child("time").getValue(String.class);
                        TracedContact eachTracedObject = eachSnapshot.getValue(TracedContact.class);
                        tracedContacts.add(eachTracedObject);
                    }
                    detailedListView.setAdapter(new ListAdapter(ContactTracingApplication.getInstance(),tracedContacts));

                    detailedProgressBar.setVisibility(View.INVISIBLE);
                }else{
                    mDetailedListTextView.setText(getResources().getString(R.string.detailed_tip_text_error));
                    detailedProgressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                detailedProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        goBackDetailed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}