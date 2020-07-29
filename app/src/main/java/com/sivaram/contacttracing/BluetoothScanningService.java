package com.sivaram.contacttracing;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;
import static com.sivaram.contacttracing.Constants.NOTIF_ID;
import static com.sivaram.contacttracing.Utility.deviceDB;
import static com.sivaram.contacttracing.Utility.isBluetoothAvailable;


public class BluetoothScanningService extends Service {

    private static final int FIVE_MINUTES = 1 * 60 * 1000;
    private long searchTimestamp;

    public static boolean serviceRunning = false;
    private final GattServer mGattServer = new GattServer();
    private Timer timer;
    private BluetoothLeScanner mBluetoothLeScanner;
    private String loggedUsername=Constants.EMPTY,loggedPhoneNumber=Constants.EMPTY;
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy(HH.mm.ss)");

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG,"onScanResult entered");
            if (Utility.isBluetoothPermissionAvailable(ContactTracingApplication.instance)) {
                if (result != null && result.getDevice() != null && result.getDevice().getName() != null){
                    String deviceName = result.getDevice().getName();
                    String username = Constants.EMPTY;
                    List<ParcelUuid> uuid = result.getScanRecord().getServiceUuids();
                    String recUUID=uuid.get(0).toString();
                    String contact=Constants.EMPTY;
                    contact= parseContactFromUUID(recUUID);
                    String rssi = String.valueOf(result.getRssi());
                    Date date = new Date();
                    String time = sdf.format(date.getTime());
                    //clearList(); //todo handle this area for clearing the list
                    BluetoothModel bluetoothModel = new BluetoothModel();
                    //storeDetectedUserDeviceInDB(bluetoothModel);
                    bluetoothModel.setContact(contact);
                    bluetoothModel.setRssi(rssi);
                    bluetoothModel.setTime(time);
                    deviceDB.add(bluetoothModel);
                    Log.d(TAG, "onScanResult : Result updated to list");
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG, "onBatchScanResults : Devices : " + results.toString());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "onScanFailed : errorCode : " + errorCode);
            if(errorCode== ScanCallback.SCAN_FAILED_INTERNAL_ERROR)
            {
                Log.e(TAG,"onScanFailed : Failed due to SCAN_FAILED_INTERNAL_ERROR");
            }else if(errorCode==ScanCallback.SCAN_FAILED_ALREADY_STARTED){
                Log.e(TAG,"onScanFailed : Failed due to SCAN_FAILED_ALREADY_STARTED");
            }else if(errorCode==ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED){
                Log.e(TAG,"onScanFailed : Failed due to SCAN_FAILED_FEATURE_UNSUPPORTED");
            }else if(errorCode==ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED){
                Log.e(TAG,"onScanFailed : Failed due to SCAN_FAILED_APPLICATION_REGISTRATION_FAILED");
                BluetoothAdapter.getDefaultAdapter().disable();
                Log.d(TAG, "Bluetooth disabled as application registartion got failed!");
                Timer tm = new Timer();
                tm.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        BluetoothAdapter.getDefaultAdapter().enable();
                        Log.d(TAG, "Bluetooth enables to restart the functionality!");
                    }
                },500);
            }else{
                Log.e(TAG, "onScanFailed : errorCode : " + errorCode);
            }
        }
    };

    private String parseContactFromUUID(String recUUID) {

        String contact=Constants.EMPTY;
        contact=recUUID.replace("-","");
        contact=(contact.split("0a")[1]).substring(0,10);
        return contact;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public BluetoothScanningService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification notification = getNotification(Constants.NOTIFICATION_DESC);
        startForeground(NOTIF_ID, notification);
        searchTimestamp = System.currentTimeMillis();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceRunning=true;
        loggedUsername=SharedPref.getStringParams(ContactTracingApplication.getInstance(), Constants.USERNAME, Constants.EMPTY);
        loggedPhoneNumber=SharedPref.getStringParams(ContactTracingApplication.getInstance(), Constants.PHONE, Constants.EMPTY);
        Utility.SERVICE_UUID=preparedUuid();
        //Check parameters and update notification accordingly
        configureNotification();
        registerBluetoothStateListener();
        registerLocationStateListener();
        //return super.onStartCommand(intent, flags, startId);
        //mAdaptiveScanHelper = new AdaptiveScanHelper(this);todo Check this line
        mGattServer.onCreate(BluetoothScanningService.this);
        mGattServer.addGattService();
        advertiseAndScan();
        //startLocationUpdate();todo retrieve location
        Log.d(TAG, "onStartCommand service started");
        return START_STICKY;
    }

    private String preparedUuid() {
        int data_idx=0,uuid_idx=0;
        String realData = "0a".concat(loggedPhoneNumber);
        final int REAL_DATA_LENGTH = realData.length();
        String SERVICE_UUID="f55e5de6-52d7-400b-b4dc-230eb8812d2c";
        String updatedUUID=Constants.EMPTY;
        final int SERVICE_UUID_LENGTH=SERVICE_UUID.length();
        char[] uuidArray =SERVICE_UUID.toCharArray();
        while(uuid_idx < SERVICE_UUID_LENGTH){
            if(uuidArray[uuid_idx]!='-'){
                if(data_idx<REAL_DATA_LENGTH){
                    uuidArray[uuid_idx]=realData.charAt(data_idx);
                    data_idx=data_idx+1;
                }
            }
            uuid_idx=uuid_idx+1;
        }
        updatedUUID=new String(uuidArray);
        //updatedUUID = "0a630583-9271-400b-b4dc-sivaramredmi";
        //updatedUUID = "0a630583-9271-0a4s-i4va-4ra4mr4ed4mi";
        //updatedUUID = "0a630583-9271-400b-b4dc-230eb8812d2c";
        //updatedUUID = "0a6s3i0v5a8r3a9m2r7e1dmi";
        return updatedUUID;
    }

    @Override
    public void onLowMemory() {
        Log.d(TAG, "onLowMemory");
        super.onLowMemory();
        stopSelf();
        serviceRunning = false;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Info","Service Destroyed!");
        serviceRunning=false;
        try {
            if (mBluetoothStatusChangeReceiver != null) {
                unregisterReceiver(mBluetoothStatusChangeReceiver);
            }
            if (mLocationChangeListener != null) {
                unregisterReceiver(mLocationChangeListener);
            }
            stopForeground(true);

            if (mBluetoothLeScanner != null && isBluetoothAvailable()) {
                mBluetoothLeScanner.stopScan(mScanCallback);
            }
            if (timer != null) {
                timer.cancel();
            }
            mGattServer.onDestroy();
            //todo start from here
            /*if (mAdaptiveScanHelper != null) {
                mAdaptiveScanHelper.reset();
            }*/
        } catch (Exception ex) {
            //As this exception doesn't matter for user,service already destroying,so just logging this on firebase
            //CorUtilityKt.reportException(ex);
        }
    }

    /**
     * Method to restart advertisement and scanning in every 5 min.
     */
    private void advertiseAndScan() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

                                      @Override
                                      public void run() {
                                          if (isBluetoothAvailable()) {
                                              //mGattServer.advertise(mAdaptiveScanHelper.getAdvertisementMode());todo check this line
                                              mGattServer.advertise(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
                                              //discover(mAdaptiveScanHelper.getScanMode());
                                              discover(ScanSettings.SCAN_MODE_BALANCED);
                                          }
                                      }
                                  },
                0,
                FIVE_MINUTES);
        //mAdaptiveScanHelper.start();todo adaptivescanner
    }

    /**
     * Start scanning BLE devices with provided scan mode
     * @param scanMode
     */
    private void discover(int scanMode) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return;
        }
        mBluetoothLeScanner = adapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner == null) {
            return;
        }
        List<ScanFilter> filters = new ArrayList<>();

        ScanFilter filter = new ScanFilter.Builder()
                //.setServiceUuid(new ParcelUuid(UUID.fromString(Constants.SERVICE_UUID)))
                .setDeviceName("Trace")
                .build();
        filters.add(filter);
        ScanSettings.Builder settings = new ScanSettings.Builder()
                .setScanMode(scanMode);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            settings.setMatchMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setLegacy(false);
            settings.setPhy(BluetoothDevice.PHY_LE_1M);
        }
        try {
            if (isBluetoothAvailable()) {
                if(mBluetoothLeScanner!=null){
                    //Cancel scanning if any ongoing
                    //mBluetoothLeScanner.stopScan(mScanCallback);
                    mBluetoothLeScanner.startScan(filters, settings.build(), mScanCallback);
                }
            } else {
                Log.e(TAG, "startingScan failed : Bluetooth not available");
            }
        } catch (Exception ex) {
            //Handle Android internal exception for BT adapter not turned ON(Known Android bug)
            //CorUtilityKt.reportException(ex);
            ex.printStackTrace();
        }
    }

    /*void clearList() {
        //todo set this for clearing the list for duplicates
        long scanPollTime = FirebaseRemoteConfigUtil.getINSTANCE().getScanPollTime();
        long pollTime = scanPollTime * 1000;
        long difference = System.currentTimeMillis() - searchTimestamp;
        if (difference >= pollTime && !mData.isEmpty()) {
            searchTimestamp = System.currentTimeMillis();
            mData.clear();
        }
    }*/

    private void registerBluetoothStateListener() {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothStatusChangeReceiver, filter);
    }

    private void registerLocationStateListener() {
        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(mLocationChangeListener, filter);
    }

    private void configureNotification() {
        Notification notification;
        if (!Utility.isLocationEnabled(ContactTracingApplication.instance.getContext())) {
            notification = getNotification(Constants.PLEASE_ALLOW_LOCATION);
        } else if (!isBluetoothAvailable()) {
            notification = getNotification(Constants.PLEASE_ALLOW_BLUETOOTH);
        } else {
            notification = getNotification(Constants.NOTIFICATION_DESC);
        }
        startForeground(NOTIF_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            String channelId = Constants.NOTIFICATION_CHANNEL;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.enableLights(false);
            channel.setSound(null, null);
            channel.setShowBadge(false);
            channel.setDescription(Constants.NOTIFICATION_DESC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
            Notification notification = getNotification(Constants.NOTIFICATION_DESC);
            startForeground(NOTIF_ID, notification);
        }else{
            // Build the notification.
            Notification notification = getNotification(Constants.NOTIFICATION_DESC);
            startForeground(NOTIF_ID, notification);
        }
    }

    /*When user clicks on notification necessary action will be taken*/
    private Notification getNotification(String notificationDescText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{notificationIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? Constants.NOTIFICATION_CHANNEL : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(getResources().getString(R.string.app_name));
        bigTextStyle.bigText(notificationDescText);
        return notificationBuilder
                .setStyle(bigTextStyle)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(notificationDescText)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSound(null)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();
    }

    private BroadcastReceiver mLocationChangeListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Notification notification;
            if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(action)) {
                if (!Utility.isLocationEnabled(ContactTracingApplication.instance.getContext())) {
                    notification = getNotification(Constants.PLEASE_ALLOW_LOCATION);
                    updateNotification(notification);

                } else {
                    if (isBluetoothAvailable()) {
                        notification = getNotification(Constants.NOTIFICATION_DESC);
                        updateNotification(notification);
                    } else {
                        notification = getNotification(Constants.PLEASE_ALLOW_BLUETOOTH);
                        updateNotification(notification);
                    }
                }
            }
        }
    };

    private BroadcastReceiver mBluetoothStatusChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Notification notification;
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        mGattServer.stopServer();
                        break;

                    case BluetoothAdapter.STATE_OFF:
                        notification = getNotification(Constants.PLEASE_ALLOW_BLUETOOTH);
                        updateNotification(notification);
                        //mAdaptiveScanHelper.stop();// todo check this line
                        break;
                    case BluetoothAdapter.STATE_ON:
                        if (!Utility.isLocationEnabled(ContactTracingApplication.instance.getContext())) {
                            notification = getNotification(Constants.PLEASE_ALLOW_LOCATION);
                            updateNotification(notification);
                        } else {
                            notification = getNotification(Constants.NOTIFICATION_DESC);
                            updateNotification(notification);
                        }
                        mGattServer.addGattService();
                        advertiseAndScan();
                        break;
                }
            }
        }
    };

    private void updateNotification(Notification notification) {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(NOTIF_ID, notification);
        }
    }

}
