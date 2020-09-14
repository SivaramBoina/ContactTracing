package com.sivaram.contacttracing.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sivaram.contacttracing.ContactTracingApplication;
import com.sivaram.contacttracing.R;
import com.sivaram.contacttracing.helpers.TracedContact;
import com.sivaram.contacttracing.helpers.User;
import com.sivaram.contacttracing.sharedpref.SharedPref;
import com.sivaram.contacttracing.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends ListActivity {

    private ListView adminContactsListView;
    private Button adminBackButton;
    private TextView adminTipTextView;
    private ProgressBar adminContactsProgressBar;
    private DatabaseReference databaseAdminReferenceTraces;
    private String loggedUserContact = Constants.EMPTY;
    private List<String> adminTracedUsers = new ArrayList<>();
    private ArrayAdapter<String> adminAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traced_contacts);
        adminContactsListView = (ListView)findViewById(android.R.id.list);
        adminTipTextView = findViewById(R.id.tip_textView);
        adminBackButton = findViewById(R.id.btn_go_back);
        adminContactsProgressBar = findViewById(R.id.progressBar_traced_contacts);
        loggedUserContact = SharedPref.getStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_CONTACT,Constants.EMPTY);
        databaseAdminReferenceTraces = FirebaseDatabase.getInstance().getReference("users");
        adminAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,adminTracedUsers){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                // Cast the list view each item as text view
                TextView item = (TextView) super.getView(position,convertView,parent);
                //set background
                item.setBackground(getResources().getDrawable(R.drawable.rounded_shape));


                // Set the item text style to bold
                item.setTypeface(item.getTypeface(), Typeface.BOLD);

                // Change the item text size
                item.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);

                // return the view
                return item;
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        adminContactsProgressBar.setVisibility(View.VISIBLE);
        adminTipTextView.setText(getResources().getString(R.string.no_traces));
        databaseAdminReferenceTraces.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    adminTipTextView.setText(getResources().getString(R.string.tip_text));
                    adminTracedUsers.clear();
                    for(DataSnapshot eachSnapsshot : snapshot.getChildren()){
                        String username = eachSnapsshot.child("username").getValue(String.class);
                        adminTracedUsers.add(username);
                        Log.d("Check","Each username : "+username);
                    }
                    adminAdapter.notifyDataSetChanged();
                    adminContactsListView.setAdapter(adminAdapter);
                }else{
                    adminTipTextView.setText(getResources().getString(R.string.no_traces));
                }
                adminContactsProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Check",error.getMessage());
                adminContactsProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        adminBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ContactTracingApplication.toastMessage(adminContactsListView.getItemAtPosition(position).toString());
        String selectedUsername = adminContactsListView.getItemAtPosition(position).toString();
        //Get Contact of selected username
        adminContactsProgressBar.setVisibility(View.VISIBLE);
        DatabaseReference databaseContactRef = FirebaseDatabase.getInstance().getReference("users");
        Query query = databaseContactRef.orderByChild("username").equalTo(selectedUsername);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot eachSnapshot: snapshot.getChildren()) {
                        try {
                            String contact = eachSnapshot.child("contact").getValue(String.class);
                            Intent tracedContacts = new Intent(ContactTracingApplication.getInstance(), TracedContacts.class);
                            tracedContacts.putExtra("IFADMIN",contact);
                            startActivity(tracedContacts);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    adminTipTextView.setText(getResources().getString(R.string.no_traces));
                }
                ContactTracingApplication.toastMessage("No traces found!!!");
                adminContactsProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Check",error.getMessage());
                adminContactsProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}