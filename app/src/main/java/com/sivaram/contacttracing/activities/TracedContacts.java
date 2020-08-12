package com.sivaram.contacttracing.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

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
import java.util.List;

public class TracedContacts extends ListActivity {

    private ListView contactsListView;
    private Button backButton;
    private TextView tipTextView;
    private ProgressBar tracedContactsProgressBar;
    private DatabaseReference databaseReferenceTraces;
    private String loggedUserContact = Constants.EMPTY;
    private List<String> tracedContacts = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traced_contacts);
        contactsListView = (ListView)findViewById(android.R.id.list);
        tipTextView = findViewById(R.id.tip_textView);
        backButton = findViewById(R.id.btn_go_back);
        tracedContactsProgressBar = findViewById(R.id.progressBar_traced_contacts);
        loggedUserContact = SharedPref.getStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_CONTACT,Constants.EMPTY);
        databaseReferenceTraces = FirebaseDatabase.getInstance().getReference("traces").child(loggedUserContact);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,tracedContacts);
    }
    @Override
    protected void onResume() {
        super.onResume();
        tracedContactsProgressBar.setVisibility(View.VISIBLE);
        tipTextView.setText(getResources().getString(R.string.no_traces));
        databaseReferenceTraces.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    tipTextView.setText(getResources().getString(R.string.tip_text));
                    tracedContacts.clear();
                    for(DataSnapshot eachSnapsshot : snapshot.getChildren()){
                        String contact = eachSnapsshot.getKey();
                        tracedContacts.add(contact);
                        Log.d("Check","Each traced contact : "+contact);
                    }
                    adapter.notifyDataSetChanged();
                    contactsListView.setAdapter(adapter);
                }else{
                    tipTextView.setText(getResources().getString(R.string.no_traces));
                }
                tracedContactsProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Check",error.getMessage());
                tracedContactsProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        //ContactTracingApplication.toastMessage(contactsListView.getItemAtPosition(position).toString());
        String selectedContact = contactsListView.getItemAtPosition(position).toString();
        Intent detailTraces = new Intent(ContactTracingApplication.getInstance(), DetailedTracesActivity.class);
        detailTraces.putExtra(Constants.DETAILED_TRACE_CONTACT,selectedContact);
        startActivity(detailTraces);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}