package com.sivaram.contacttracing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.Fragment;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import com.sivaram.contacttracing.ui.main.LoginFragment;
import com.sivaram.contacttracing.ui.main.SignUpFragment;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;
import static com.sivaram.contacttracing.Constants.PERMISSION_REQUEST_CODE;
import static com.sivaram.contacttracing.ContactTracingApplication.toastMessage;

public class LauncherActivity extends AppCompatActivity {

    String isSignedUp = "no";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher_activity);
        isSignedUp=SharedPref.getStringParams(ContactTracingApplication.getInstance(), Constants.SIGNUP, Constants.EMPTY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Checking permissions for the app to perform its functionality
        if (!checkPermission()) {
            requestPermission();
        }
        if(isSignedUp.equals("yes")){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, LoginFragment.newInstance())
                    .commitNow();
        }else{
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, SignUpFragment.newInstance())
                    .commitNow();
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(ContactTracingApplication.instance.getContext(), ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(ContactTracingApplication.instance.getContext(), READ_PHONE_STATE);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
    }
    /* This function is called when the user accepts or decline the permission.
   Request Code is used to check which permission called this function.
   This request code is provided when the user is prompt for permission.*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean readPhoneStateAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && readPhoneStateAccepted)
                        toastMessage("Required permissions granted.");
                    else {
                        toastMessage("Please accept permission to continue!");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{ACCESS_FINE_LOCATION, READ_PHONE_STATE},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(LauncherActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}