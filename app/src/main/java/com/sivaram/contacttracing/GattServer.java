package com.sivaram.contacttracing;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @author Niharika.Arora
 */
public class GattServer {
    private String TAG = this.getClass().getName();
    private Context mContext;

    private BluetoothLeAdvertiser advertiser;
    private BluetoothGattServer mBluetoothGattServer;
    private BluetoothManager mBluetoothManager;

    private AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.i("Advertisement","Success");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            if(errorCode==AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR)
            {
                Log.e("Advertisement","Failed due to ADVERTISE_FAILED_INTERNAL_ERROR");
            }else if(errorCode==AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED){
                Log.e("Advertisement","Failed due to ADVERTISE_FAILED_ALREADY_STARTED");
            }else if(errorCode==AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE){
                Log.e("Advertisement","Failed due to ADVERTISE_FAILED_DATA_TOO_LARGE");
            }else if(errorCode==ADVERTISE_FAILED_TOO_MANY_ADVERTISERS){
                Log.e("Advertisement","Failed due to ADVERTISE_FAILED_TOO_MANY_ADVERTISERS");
            }else{
                Log.e("Advertisement","Failed advertisement due to errorcode: "+errorCode);
            }
        }
    };

    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //do nothing
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //do nothing
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            //todo it wont be equal by default
            if (UUID.fromString(Utility.SERVICE_UUID).equals(characteristic.getUuid())) {
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, characteristic.getValue());
            } /*else if (UUID.fromString(BuildConfig.PINGER_UUID).equals(characteristic.getUuid())) {
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, characteristic.getValue());
            }*/
            else {
                // Invalid characteristic
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
            }
        }
    };


    public void onCreate(Context context) throws RuntimeException {
        mContext = context;
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public void advertise(int advertisementMode) {
        try {
            BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
            if (defaultAdapter == null) {
                return;
            }
            if (Constants.ADAPTER_NAME.isEmpty()) {
                return;
            }
            if (!Constants.ADAPTER_NAME.equalsIgnoreCase(defaultAdapter.getName())) {
                stopAdvertising();
            }
            defaultAdapter.setName(Constants.ADAPTER_NAME);
            advertiser = defaultAdapter.getBluetoothLeAdvertiser();
            AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(advertisementMode)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                    .setConnectable(true);

            ParcelUuid pUuid = new ParcelUuid(UUID.fromString(Utility.SERVICE_UUID));
            AdvertiseData data = new AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .addServiceUuid(pUuid)
                    .setIncludeTxPowerLevel(false).build();
            if (advertiser != null) {
                try {
                    //If previous advertisement is still performing
                    stopAdvertising();
                    startAdvertising(settingsBuilder, data, true);
                    Log.d(TAG,"Advertisement started with connectable true");
                } catch (Exception e) {
                    // Adding common exception just to retry this if anything goes wrong in the first time
                    // (Chinese devices facing some legacy data issue)
                    //Some OEM shows Advertising data too large exception,so not sending txPowerLevel
                    /*if (e instanceof IllegalArgumentException && !TextUtils.isEmpty(e.getMessage())
                            && e.getMessage().contains(Constants.LEGACY_ISSUE)) {
                        AnalyticsUtils.sendEvent(EventNames.ADVERTISING_LEGACY_ISSUE);
                    }*/
                    e.printStackTrace();
                    startAdvertising(settingsBuilder, data, false);
                    Log.d(TAG,"Advertisement started with connectable false");
                }
            }
        } catch (Exception ex) {
            //Reporting exception on Crashlytics if advertisement fails for other reason in devices and take corrective actions
            //CorUtilityKt.reportException(ex);
            ex.printStackTrace();
        }
    }

    private void startAdvertising(AdvertiseSettings.Builder settingsBuilder, AdvertiseData data, boolean isConnectable) {
        settingsBuilder.setConnectable(isConnectable);
        if (Utility.isBluetoothAvailable() && advertiser != null && advertisingCallback != null) {
            advertiser.startAdvertising(settingsBuilder.build(), data, advertisingCallback);
        } else {
            //do nothing
        }
    }



    public void addGattService() {
        if (Utility.isBluetoothAvailable() && isServerStarted()) {
            try {
                mBluetoothGattServer.addService(createGattService());
            } catch (Exception ex) {
                //Android version 7.0 (Redmi Note 4 & Huawei MediaPad T3 & Nova2Plus device issue) Android BLE characterstic add issue  https://github.com/iDevicesInc/SweetBlue/issues/394
            }
        }
    }

    private BluetoothGattService createGattService() {
        BluetoothGattService service = new BluetoothGattService(UUID.fromString(Utility.SERVICE_UUID), BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic uniqueIdChar = new BluetoothGattCharacteristic(UUID.fromString(Utility.DID_UUID),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        String username = SharedPref.getStringParams(ContactTracingApplication.getInstance(), Constants.USERNAME, Constants.EMPTY);
        uniqueIdChar.setValue(username);

        //Adding this for iOS continuous ping
        /*BluetoothGattCharacteristic pingerChar = new BluetoothGattCharacteristic(UUID.fromString(BuildConfig.PINGER_UUID),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        pingerChar.setValue(String.valueOf(true));*/

        service.addCharacteristic(uniqueIdChar);
        //service.addCharacteristic(pingerChar);

        return service;
    }

    public void onDestroy() {
        if (mContext != null) {
            if (Utility.isBluetoothAvailable()) {
                stopServer();
                stopAdvertising();
            }
        }
    }

    public void stopServer() {
        try {
            if (mBluetoothGattServer != null) {
                mBluetoothGattServer.clearServices();
                mBluetoothGattServer.close();
            }
        } catch (Exception e) {
            //Handle Bluetooth Gatt close internal bug
            Log.e(TAG, "GATT server can't be closed elegantly" + e.getMessage());
        }
    }

    private boolean isServerStarted() {
        mBluetoothGattServer = mBluetoothManager.openGattServer(mContext, mGattServerCallback);
        if (mBluetoothGattServer != null) {
            mBluetoothGattServer.clearServices();
            return true;
        } else {
            return false;
        }
    }

    public void stopAdvertising() {
        try {
            if (advertiser != null) {
                advertiser.stopAdvertising(advertisingCallback);
            }
        } catch (Exception ex) {
            //Handle StopAdvertisingSet Android Internal bug (Redmi Note 7 Pro Android 9)
        }
    }
}
