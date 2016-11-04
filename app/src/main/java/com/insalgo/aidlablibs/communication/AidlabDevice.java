package com.insalgo.aidlablibs.communication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;

/**
 *
 * AidlabDevice.java
 * Android-Example
 *
 * Created by Michal Baranski on 03.11.2016.
 * Copyright (c) 2016 Aidlab. MIT License.
 *
 */

public class AidlabDevice {

    public static final String DEVICE_NAME = "Aidlab";

    private final Context context;
    private final BluetoothDevice device;
    private final AidlabGattClient client;

    private BluetoothGatt connectedGatt;

    public AidlabDevice(Context context, BluetoothDevice device)
    {
        if (device == null || device.getName() == null || !device.getName().equals(DEVICE_NAME))
            throw new IllegalArgumentException("Provided bluetooth device is not an aidlab device");

        this.context = context;
        this.device = device;
        this.client = new AidlabGattClient();
        this.connectedGatt = null;
    }

    public void setDataReceiver(IAidlabDataReceiver dataReceiver)
    {
        client.setDataReceiver(dataReceiver);
    }

    public boolean isConnected()
    {
        return connectedGatt != null && client.isConnected();
    }

    public void connect()
    {
        connectedGatt = device.connectGatt(context, false, client);
    }

    public void disconnect()
    {
        if (!isConnected())
            return;

        connectedGatt.disconnect();
        connectedGatt.close();
        connectedGatt = null;
    }

    public String getHardwareAddress()
    {
        return device.getAddress();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AidlabDevice))
            return false;

        return device.getAddress().equals(((AidlabDevice) obj).device.getAddress());
    }

    @Override
    public int hashCode() {
        return device.getAddress().hashCode();
    }

    @Override
    public String toString() {
        return "AidlabDevice["+getHardwareAddress()+","+(isConnected()?"Connected":"Disconnected")+"]";
    }
}
