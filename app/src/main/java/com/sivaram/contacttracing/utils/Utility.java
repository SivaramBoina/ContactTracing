package com.sivaram.contacttracing.utils;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import androidx.core.content.ContextCompat;

import com.sivaram.contacttracing.ContactTracingApplication;
import com.sivaram.contacttracing.sharedpref.SharedPref;

import java.util.ArrayList;

public class Utility {

    public static String SERVICE_UUID="f55e5de6-52d7-400b-b4dc-230eb8812d2c";
    public static String DID_UUID="d51bd1ed-ad4b-41d2-a019-490f96cf8276";
    //public static ArrayList<Contact> deviceDB = new ArrayList<Contact>();

    public static boolean isLocationEnabled(Context mContext)
    {
        boolean isLocationEnabled=false;
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        try{
            isLocationEnabled=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception e) {
            e.printStackTrace();
        }
        return isLocationEnabled;
    }

    public static void removeSessionVariables() {
        SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_CONTACT,Constants.EMPTY);
        SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_USERNAME,Constants.EMPTY);
        SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_PASSWORD,Constants.EMPTY);
        SharedPref.setStringParams(ContactTracingApplication.getInstance(),Constants.SESSION_AUTO_LOGIN,Constants.NO);
    }

    public static boolean isBluetoothPermissionAvailable(Context mContext)
    {
        int permission1 = ContextCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH);
        int permission2 = ContextCompat.checkSelfPermission(mContext,Manifest.permission.BLUETOOTH_ADMIN);
        return (permission1== PackageManager.PERMISSION_GRANTED && permission2==PackageManager.PERMISSION_GRANTED);

    }
    public static boolean isBluetoothAvailable(){
        if(isBluetoothPermissionAvailable(ContactTracingApplication.instance.getContext())){
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            return bluetoothAdapter!=null && bluetoothAdapter.getState()==BluetoothAdapter.STATE_ON;
        }
        return false;
    }
}
