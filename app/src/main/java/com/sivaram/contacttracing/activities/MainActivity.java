package com.sivaram.contacttracing.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sivaram.contacttracing.ContactTracingApplication;
import com.sivaram.contacttracing.R;
import com.sivaram.contacttracing.helpers.TracedContact;
import com.sivaram.contacttracing.service.BluetoothScanningService;
import com.sivaram.contacttracing.sharedpref.SharedPref;
import com.sivaram.contacttracing.utils.Constants;
import com.sivaram.contacttracing.utils.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.sivaram.contacttracing.utils.Constants.REQUEST_ENABLE_BT;
import static com.sivaram.contacttracing.ContactTracingApplication.toastMessage;

public class MainActivity extends AppCompatActivity {
    Button btnStartService, btnStopService,btnCheckTraces,btnLogOut,btnFetchUsers;
    TextView textViewUserName;

    //todo delete
    int check=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewUserName = (TextView) findViewById(R.id.main_txtview_username);
        btnStartService = (Button) findViewById(R.id.btn_startService);
        btnStopService = (Button) findViewById(R.id.btn_stopService);
        btnCheckTraces = (Button) findViewById(R.id.btn_checkTraces);
        btnLogOut=(Button)findViewById(R.id.btn_logout);
        btnFetchUsers=(Button)findViewById(R.id.btn_admin);

        //Setting listeners for UI elements
        btnStartService.setOnClickListener(onClickListener);
        btnStopService.setOnClickListener(onClickListener);
        btnCheckTraces.setOnClickListener(onClickListener);
        btnLogOut.setOnClickListener(onClickListener);
        btnFetchUsers.setOnClickListener(onClickListener);
        //Check if Bluetooth is enabled
        isBluetoothEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sessionContact = SharedPref.getStringParams(ContactTracingApplication.getInstance(), Constants.SESSION_CONTACT,Constants.EMPTY);
        String sessionUsername = SharedPref.getStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_USERNAME,Constants.EMPTY);
        if(!TextUtils.isEmpty(sessionUsername)){
            textViewUserName.setText("Hello "+sessionUsername);
            if(sessionContact.equals("8985373384")){
                btnFetchUsers.setVisibility(View.VISIBLE);
            }else{
                btnFetchUsers.setVisibility(View.INVISIBLE);
            }
        }else{
            ContactTracingApplication.toastMessage("Internal error occurred");
            SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_CONTACT,Constants.EMPTY);
            SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_USERNAME,Constants.EMPTY);
            gotoLauncherActivity();
        }
    }

    private void gotoLauncherActivity() {
        Intent launcherIntent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(launcherIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void startBluetoothService(Intent serviceIntent) {
        if (!BluetoothScanningService.serviceRunning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplicationContext().startForegroundService(serviceIntent);
            } else {
                getApplicationContext().startService(serviceIntent);
            }
        }
    }



    public void isBluetoothEnabled(){
        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter  = mBluetoothManager.getAdapter();

        if (mBluetoothAdapter == null && !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toastMessage("BLE is not supported");
            finish();
            //return false;
        }else{
            toastMessage("BLE is supported");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    public void startTraceService(){
        Intent intent = new Intent(ContactTracingApplication.getInstance(),BluetoothScanningService.class);
        startBluetoothService(intent);
    }

    public void stopTraceService(){
        Intent myService = new Intent(ContactTracingApplication.getInstance(), BluetoothScanningService.class);
        stopService(myService);
    }
    boolean foundDuplicate = false;
    boolean userNotExists = false;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId())
            {
                case R.id.btn_startService:{
                    startTraceService();
                }
                break;
                case R.id.btn_stopService:
                {
                    stopTraceService();
                }
                break;
                case R.id.btn_checkTraces:
                {
                    Intent tracedContacts = new Intent(ContactTracingApplication.getInstance(),TracedContacts.class);
                    startActivity(tracedContacts);
                }
                break;
                case R.id.btn_logout:
                {
                    Utility.removeSessionVariables();
                    stopTraceService();
                    Intent logOutIntent = new Intent(MainActivity.this, LoginLauncherActivity.class);
                    startActivity(logOutIntent);
                }
                break;
                case R.id.btn_admin:
                {
                    Intent adminIntent =  new Intent(MainActivity.this,AdminActivity.class);
                    startActivity(adminIntent);
                }
                break;
            }
        }
    };

    private void checkIfDuplicate(){
        foundDuplicate=false;
        userNotExists = false;
        final Date curDate = new Date();
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US);
        //check if exists
        /*Query query = FirebaseDatabase.getInstance().getReference("traces").child("9989773947").orderByChild("traced_contact").equalTo("8985373384");*/
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("traces").child("9989773947");
        Log.d("Check","Database  reference!!");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Log.d("Check","Trace exists!!");
                    userNotExists = false;
                    for (DataSnapshot eachSnapshot: snapshot.getChildren()) {
                        try{
                            String eachTime = eachSnapshot.child("time").getValue(String.class);
                            assert eachTime != null;
                            Date eachDate = dateFormatter.parse(eachTime);
                            if(Math.abs(curDate.getTime() - eachDate.getTime()) <= 10*60*1000){
                                Log.d("Check","Duplicate found!");
                                foundDuplicate = true;
                            }
                        }catch(Exception e){
                            Log.d("Check","Exception occurred");
                        }
                    }
                    if(foundDuplicate){
                        Log.d("Check","Found duplicate : Don't  add the trace");
                    }else{
                        Log.d("Check","No duplicate found : Add the trace");
                    }
                }else{
                    userNotExists = true;
                    Log.d("Check","Trace does not found : Add the trace");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Check","On Cancelled triggered with error : "+error.getMessage());
            }
        });
    }
   /* private boolean checkifduplicatech() {
        foundDuplicate=false;
        final Date curDate = new Date();
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US);
        //check if exists
        Query query = FirebaseDatabase.getInstance().getReference("traces").child("9989773947").orderByChild("traced_contact").equalTo("8985373384");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot eachSnapshot: dataSnapshot.getChildren()) {
                        try{
                            String eachTime = eachSnapshot.child("time").getValue(String.class);
                            Date eachDate = dateFormatter.parse(eachTime);
                            if(Math.abs(curDate.getTime() - eachDate.getTime()) <= 10*60*1000){
                                ContactTracingApplication.toastMessage("Found duplicate");
                                foundDuplicate = true;
                            }
                        }catch(Exception e){
                            ContactTracingApplication.toastMessage("Error while doing date comparison");
                        }
                    }
                }else{
                    ContactTracingApplication.toastMessage("No history found with 8985373384");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(Constants.TAG,databaseError.getMessage());
                ContactTracingApplication.toastMessage("Internal error occurred!");
            }
        });
        return foundDuplicate;
    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}