package com.insalgo.aidlablibs.communication;

import android.app.Activity;
import android.content.Intent;

/**
 *
 * CallbackManager.java
 * Android-Example
 *
 * Created by Michal Baranski on 03.11.2016.
 * Copyright (c) 2016 Aidlab. MIT License.
 *
 */

public class CallbackManager {

    private static final int REQUEST_CODE_BASE = 0x9afc;

    public static final int REQUEST_CHECK_BLUETOOTH_STATE = REQUEST_CODE_BASE + 1;
    public static final int REQUEST_CHECK_BLUETOOTH_PERMISSION = REQUEST_CODE_BASE + 2;

    private final AidlabDeviceManager deviceManager;

    public CallbackManager(AidlabDeviceManager deviceManager)
    {
        this.deviceManager = deviceManager;
    }

    public void onActivityResult(Activity currentActivity, int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CHECK_BLUETOOTH_STATE)
        {
            deviceManager.onBluetoothRequestCompleted(currentActivity, resultCode);
        }
    }

    public void onRequestPermissionsResult(Activity currentActivity, int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == REQUEST_CHECK_BLUETOOTH_PERMISSION)
        {
            deviceManager.onBluetoothPermissionRequestCompleted(currentActivity, permissions, grantResults);
        }
    }
}
