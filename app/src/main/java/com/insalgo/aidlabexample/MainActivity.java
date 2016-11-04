package com.insalgo.aidlabexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.insalgo.aidlablibs.communication.AidlabDevice;
import com.insalgo.aidlablibs.communication.AidlabDeviceManager;
import com.insalgo.aidlablibs.communication.IAidlabDataReceiver;
import com.insalgo.aidlablibs.communication.IAidlabSystemListener;

/**
 *
 * MainActivity.java
 * Android-Example
 *
 * Created by Michal Baranski on 03.11.2016.
 * Copyright (c) 2016 Aidlab. MIT License.
 *
 */

public class MainActivity extends AppCompatActivity implements IAidlabSystemListener, IAidlabDataReceiver {

    private AidlabDeviceManager aidlabManager;

    private AidlabDevice connectedDevice;

    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println("MainActivity.onCreate()");

        textView1 = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        textView4 = (TextView) findViewById(R.id.textView4);

        connectedDevice = null;
        aidlabManager = new AidlabDeviceManager(this, this);

        //Start the bluetooth check process - Request necessary permissions and ask the user to enable bluetooth if it's disabled
        aidlabManager.checkBluetooth(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Activity result has to be passed to Aidlab system's callback manager for the bluetooth check process
        aidlabManager.getCallbackManager().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Permissions result has to be passed to Aidlab system's callback manager for the bluetooth check process
        aidlabManager.getCallbackManager().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    //----------------Callbacks from aidlab system----------------
    public void onBluetoothStarted()
    {
        System.out.println("MainActivity.onBluetoothStarted()");

        //Bluetooth check process completed - start scanning for devices
        aidlabManager.scanForDevices();
    }

    public void onDeviceDetected(AidlabDevice device)
    {
        System.out.println("MainActivity.onDeviceDetected("+device+")");

        //Connect to first found device
        if (connectedDevice == null)
        {
            aidlabManager.stopDeviceScan();

            //Wait for the scan to finish and connect to the device
            final AidlabDevice deviceToConnect = device;
            final IAidlabDataReceiver dataReceiver = this;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    connectedDevice = deviceToConnect;
                    deviceToConnect.setDataReceiver(dataReceiver);
                    deviceToConnect.connect();
                }
            }, 100);
        }
    }

    //----------------Callbacks from aidlab device----------------
    public void onConnected()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("MainActivity: Connected to aidlab device");
            }
        });
    }
    public void onDisconnected()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("MainActivity: Disconnected from aidlab device");
                connectedDevice = null;
            }
        });
    }

    public void onBatteryStatusReceived(float soc, boolean inCharge)
    {
        final float soc1 = soc;
        final boolean inCharge1 = inCharge;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView1.setText("soc="+soc1+" inCharge="+inCharge1);
                System.out.println("MainActivity: Battery data received: soc="+soc1+" inCharge="+inCharge1);
            }
        });
    }
    public void onTemperatureReceived(float objectTemperature, float ambientTemperature)
    {
        final float objectTemperature1 = objectTemperature;
        final float ambientTemperature1 = ambientTemperature;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView2.setText("Tobj="+objectTemperature1+" Tamb="+ambientTemperature1);
                System.out.println("MainActivity: Temperature data received: Object="+objectTemperature1+" Ambient="+ambientTemperature1);
            }
        });
    }
    public void onECGReceived(float[] ecgValues)
    {
        final int length = ecgValues.length;
        final float ecg = ecgValues[0];
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView3.setText("ECG: "+ecg);
                System.out.println("MainActivity: ECG stream data received: length="+length+" ecgValues[0]="+ecg);
            }
        });
    }
    public void onRespirationReceived(float[] respirationValues)
    {
        final int length = respirationValues.length;
        final float resp = respirationValues[0];
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView4.setText("RESP: "+resp);
                System.out.println("MainActivity: Respiration stream data received: length="+length+" respirationValues[0]="+resp);
            }
        });
    }
}
