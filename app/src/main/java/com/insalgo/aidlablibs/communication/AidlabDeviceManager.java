package com.insalgo.aidlablibs.communication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * AidlabDeviceManager.java
 * Android-Example
 *
 * Created by Michal Baranski on 03.11.2016.
 * Copyright (c) 2016 Aidlab. MIT License.
 *
 */

public class AidlabDeviceManager implements BluetoothAdapter.LeScanCallback {

    private Activity appContext;
    private IAidlabSystemListener systemEventsListener;

    private CallbackManager callbackManager;
    private BluetoothAdapter bluetoothAdapter;

    private List<AidlabDevice> deviceList;
    private boolean deviceScanActive;
    private int deviceScanTimeout = 30000;

    public AidlabDeviceManager(Activity appContext, IAidlabSystemListener systemEventsListener)
    {
        this.appContext = appContext;
        this.systemEventsListener = systemEventsListener;

        this.callbackManager = new CallbackManager(this);
        this.deviceList = new ArrayList<AidlabDevice>();

        this.deviceScanActive = false;
    }

    public CallbackManager getCallbackManager()
    {
        return callbackManager;
    }

    //-----------------------------------Bluetooth availability helper-----------------------------------
    //Check flow: Permissions -> [Request permissions dialog] -> State -> [Enable bluetooth dialog] -> Create the adapter -> onBluetoothStarted()
    public boolean checkBluetooth(Activity currentActivity)
    {
        if (!checkBluetoothPermission(currentActivity))
            return false;
        if (!checkBluetoothState(currentActivity))
            return false;

        //All checks done, try to create the adapter
        bluetoothAdapter = ((BluetoothManager)currentActivity.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            System.out.println("Could not create bluetooth adapter");
            return false;
        }

        if (systemEventsListener != null)
            systemEventsListener.onBluetoothStarted();
        return true;
    }

    private boolean checkBluetoothState(Activity currentActivity)
    {
        final BluetoothAdapter adapter = ((BluetoothManager)currentActivity.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        if (adapter == null || !adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            currentActivity.startActivityForResult(enableBtIntent, CallbackManager.REQUEST_CHECK_BLUETOOTH_STATE);
            return false;
        }

        return true;
    }

    protected void onBluetoothRequestCompleted(Activity currentActivity, int resultCode)
    {
        if (resultCode == Activity.RESULT_OK) {
            checkBluetooth(currentActivity);
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            System.out.println("Bluetooth request flow cancelled by user");
        }
        else {
            System.out.println("Bluetooth request flow error: " + resultCode);
        }
    }

    private boolean checkBluetoothPermission(Activity currentActivity)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(currentActivity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, CallbackManager.REQUEST_CHECK_BLUETOOTH_PERMISSION);
                return false;
            }
        }

        return true;
    }

    protected void onBluetoothPermissionRequestCompleted(Activity currentActivity, String[] permissions, int[] grantResults)
    {
        boolean locationPermissionOK = false;

        //Scan through the permissions array and find required ones
        for (int i=0; i<permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionOK = true;
            }
        }

        if (locationPermissionOK) { //All permissions OK
            checkBluetooth(currentActivity);
        }
        else {
            System.out.println("Bluetooth access permissions not granted");
        }
    }


    //--------------------------------------Aidlab device discovery--------------------------------------
    public void scanForDevices()
    {
        if (deviceScanActive)
            return;

        final BluetoothAdapter.LeScanCallback scanCallback = this;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopDeviceScan();
            }
        }, deviceScanTimeout);

        deviceScanActive = true;
        bluetoothAdapter.startLeScan(scanCallback);
    }

    public void stopDeviceScan()
    {
        if (!deviceScanActive)
            return;

        bluetoothAdapter.stopLeScan(this);
        deviceScanActive = false;
    }

    public List<AidlabDevice> getDetectedDevices()
    {
        return new ArrayList<AidlabDevice>(deviceList);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        final BluetoothDevice dev = device;

        appContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dev.getName() != null && dev.getName().equals(AidlabDevice.DEVICE_NAME))
                {
                    AidlabDevice aidlab = new AidlabDevice(appContext, dev);
                    if (!deviceList.contains(aidlab)) // If a new aidlab was discovered
                    {
                        deviceList.add(aidlab);

                        if (systemEventsListener!=null)
                            systemEventsListener.onDeviceDetected(aidlab);
                    }
                }
            }
        });
    }
}
