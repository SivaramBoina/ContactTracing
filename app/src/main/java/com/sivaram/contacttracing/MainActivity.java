package com.sivaram.contacttracing;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Trace;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import static com.sivaram.contacttracing.Constants.REQUEST_ENABLE_BT;
import static com.sivaram.contacttracing.ContactTracingApplication.toastMessage;

public class MainActivity extends AppCompatActivity {
    Button btnStartService, btnStopService,btnCheckTraces;
    TextView textViewUserName;
    String loginUsername=Constants.EMPTY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewUserName = (TextView) findViewById(R.id.main_txtview_username);
        btnStartService = (Button) findViewById(R.id.btn_startService);
        btnStopService = (Button) findViewById(R.id.btn_stopService);
        btnCheckTraces = (Button) findViewById(R.id.btn_checkTraces);

        //Setting listeners for UI elements
        btnStartService.setOnClickListener(onClickListener);
        btnStopService.setOnClickListener(onClickListener);
        btnCheckTraces.setOnClickListener(onClickListener);
        //Check if Bluetooth is enabled
        isBluetoothEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginUsername=SharedPref.getStringParams(ContactTracingApplication.getInstance(), Constants.USERNAME, Constants.EMPTY);
        if(!loginUsername.isEmpty()){
            textViewUserName.setText("Hello ".concat(loginUsername));
        }
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
        boolean isEnabled;
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
        }else{
            isEnabled=true;
        }
        //return isEnabled;
    }



    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId())
            {
                case R.id.btn_startService:{
                    Intent intent = new Intent(getApplicationContext(),BluetoothScanningService.class);
                    startBluetoothService(intent);
                }
                break;
                case R.id.btn_stopService:
                {
                    Intent myService = new Intent(MainActivity.this, BluetoothScanningService.class);
                    stopService(myService);
                }
                break;
                case R.id.btn_checkTraces:
                {
                    Intent traceActivityIntent = new Intent(MainActivity.this, TracedContacts.class);
                    if(Utility.deviceDB.size()>0){
                        startActivity(traceActivityIntent);
                    }else{
                        toastMessage("Not yet found any traces!");
                    }

                }
                break;
            }
        }
    };
}