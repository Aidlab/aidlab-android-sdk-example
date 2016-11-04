package com.insalgo.aidlablibs.communication;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 *
 * AidlabGattClient.java
 * Android-Example
 *
 * Created by Michal Baranski on 03.11.2016.
 * Copyright (c) 2016 Aidlab. MIT License.
 *
 */

public class AidlabGattClient extends BluetoothGattCallback {

    private final static UUID aidlabServiceUUID = UUID.fromString("44366e80-cf3a-11e1-9ab4-0002a5d5c51b");
    private final static UUID characteristicConfigDescriptorUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private final static UUID batteryCharacteristicUUID = UUID.fromString("47366e80-cf3a-11e1-9ab4-0002a5d5c51b");
    private final static UUID temperatureCharacteristicUUID = UUID.fromString("45366e80-cf3a-11e1-9ab4-0002a5d5c51b");
    private final static UUID ecgCharacteristicUUID = UUID.fromString("46366e80-cf3a-11e1-9ab4-0002a5d5c51b");
    private final static UUID respirationCharacteristicUUID = UUID.fromString("48366e80-cf3a-11e1-9ab4-0002a5d5c51b");

    private final static int streamDataPackageSize = 18;
    private final static int bytesPerSample = 3;

    private Handler handler = new Handler();
    private int subscriptionRefreshTimeout = 30000;

    private boolean connected = false;
    private final Queue<BluetoothGattCharacteristic> characteristicToSubscribe = new LinkedList<BluetoothGattCharacteristic>();
    private final List<BluetoothGattCharacteristic> subscribedCharacteristics = new ArrayList<BluetoothGattCharacteristic>();

    private final float[] ecgTempBuffer = new float[streamDataPackageSize/bytesPerSample];
    private final float[] respirationTempBuffer = new float[streamDataPackageSize/bytesPerSample];

    private IAidlabDataReceiver dataReceiver;

    public void setDataReceiver(IAidlabDataReceiver dataReceiver)
    {
        this.dataReceiver = dataReceiver;
    }

    public boolean isConnected()
    {
        return connected;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);

        characteristicToSubscribe.clear();
        subscribedCharacteristics.clear();

        if (newState == BluetoothProfile.STATE_CONNECTED)
        {
            connected = true;
            gatt.discoverServices();

            if (dataReceiver != null)
                dataReceiver.onConnected();
        }
        else
        {
            connected = false;
            gatt.disconnect();
            gatt.close();

            if (dataReceiver != null)
                dataReceiver.onDisconnected();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);

        BluetoothGattService aidlabService = gatt.getService(aidlabServiceUUID);
        if (aidlabService != null)
        {
            for (BluetoothGattCharacteristic characteristic : aidlabService.getCharacteristics())
                characteristicToSubscribe.add(characteristic);

            subscribeCharacteristic(gatt);
        }
        else
        {
            System.out.println("Aidlab service not found on device");
            gatt.disconnect();
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);

        //Remove characteristic from the queue if write succeeded
        if (status == BluetoothGatt.GATT_SUCCESS)
            subscribedCharacteristics.add(characteristicToSubscribe.remove());
        else {
            System.out.println("Failed to write descriptor: " + descriptor.getUuid() + " status: " + status);
            gatt.disconnect();
        }

        subscribeCharacteristic(gatt);
    }

    //Enables notifications from a signle characteristics from characteristicToSubscribe queue
    private void subscribeCharacteristic(BluetoothGatt gatt)
    {
        if (characteristicToSubscribe.isEmpty()) {
            final BluetoothGatt gatt1 = gatt;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshCharacteristicsSubscription(gatt1);
                }
            }, subscriptionRefreshTimeout);

            return;
        }

        BluetoothGattCharacteristic characteristic = characteristicToSubscribe.peek();

        gatt.setCharacteristicNotification(characteristic, true);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(characteristicConfigDescriptorUUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    private void refreshCharacteristicsSubscription(BluetoothGatt gatt)
    {
        characteristicToSubscribe.addAll(subscribedCharacteristics);
        subscribedCharacteristics.clear();

        subscribeCharacteristic(gatt);
        System.out.println("Characteristics notification subscription refreshed!");
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);

        final UUID uuid = characteristic.getUuid();
        final byte[] rawValue = characteristic.getValue();

        if (uuid.equals(batteryCharacteristicUUID)) {
            onBatteryDataReceived(rawValue);
        }
        else if (uuid.equals(temperatureCharacteristicUUID)) {
            onTemperatureDataReceived(rawValue);
        }
        else if (uuid.equals(ecgCharacteristicUUID)) {
            onECGDataReceived(rawValue);
        }
        else if (uuid.equals(respirationCharacteristicUUID)) {
            onRespirationDataReceived(rawValue);
        }
        else {
            System.out.println("Unsupported characteristic: "+characteristic.getUuid());
        }
    }

    //----------------------------------------Aidlab data parsing----------------------------------------
    private void onBatteryDataReceived(byte[] rawData)
    {
        if (rawData == null || rawData.length < 3)
        {
            System.out.println("Wrong battery raw data: "+rawData);
            return;
        }
        if (dataReceiver == null)
            return;

        float soc = ((rawData[1] & 0xFF) << 8 | (rawData[0] & 0xFF)) / 512.0f;
        boolean inCharge = (rawData[2] & 0xFF) != 0;

        dataReceiver.onBatteryStatusReceived(soc, inCharge);
    }

    private void onTemperatureDataReceived(byte[] rawData)
    {
        if (rawData == null || rawData.length < 4)
        {
            System.out.println("Wrong temperature raw data: "+rawData);
            return;
        }
        if (dataReceiver == null)
            return;

        float tObj = (float)((rawData[1] & 0xFF) << 8 | (rawData[0] & 0xFF));
        tObj *= 0.02f;
        tObj -= 273.15f;

        float tAmb = (float)((rawData[3] & 0xFF) << 8 | (rawData[2] & 0xFF));
        tAmb *= 0.02f;
        tAmb -= 273.15f;

        dataReceiver.onTemperatureReceived(tObj, tAmb);
    }

    private int toU2(byte byteA, byte byteB, byte byteC)
    {
        int out = ((byteA & 0x7F) << 16) | (byteB & 0xFF) << 8 | (byteC & 0xFF);

        if ((byteA & 0x80) == 0x80) { // If negative
            out -= 0x80 << 16;
        }

        return out;
    }

    private void decodeStreamData(byte[] rawData, float[] outBuffer)
    {
        int n=0;
        for (int i=0; i<rawData.length; i+= bytesPerSample)
        {
            float value = (float)toU2(rawData[i+2], rawData[i+1], rawData[i]);

            /// Transform to Volts
            value /= Math.pow(2,23);
            value *= 2.42f;

            outBuffer[n++] = value;
        }
    }

    private void onECGDataReceived(byte[] rawData)
    {
        if (rawData == null || rawData.length < streamDataPackageSize)
        {
            System.out.println("Wrong ECG raw data: "+rawData);
            return;
        }
        if (dataReceiver == null)
            return;

        decodeStreamData(rawData, ecgTempBuffer);

        dataReceiver.onECGReceived(ecgTempBuffer);
    }

    private void onRespirationDataReceived(byte[] rawData)
    {
        if (rawData == null || rawData.length < streamDataPackageSize)
        {
            System.out.println("Wrong Respiration raw data: "+rawData);
            return;
        }
        if (dataReceiver == null)
            return;

        decodeStreamData(rawData, respirationTempBuffer);

        dataReceiver.onRespirationReceived(respirationTempBuffer);
    }
}
