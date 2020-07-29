package com.sivaram.contacttracing;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Utility {

    public static String SERVICE_UUID="f55e5de6-52d7-400b-b4dc-230eb8812d2c";
    public static String DID_UUID="d51bd1ed-ad4b-41d2-a019-490f96cf8276";

    public static ArrayList<BluetoothModel> deviceDB = new ArrayList<BluetoothModel>();
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
