package com.insalgo.aidlablibs.communication;

/**
 *
 * IAidlabSystemListener.java
 * Android-Example
 *
 * Created by Michal Baranski on 03.11.2016.
 * Copyright (c) 2016 Aidlab. MIT License.
 *
 */

/**
 *  Implement this interface to listen for Aidlab system events
 */
public interface IAidlabSystemListener {

    /**
     *  Called after obtaining necessary permissions and enabling bluetooth
     */
    void onBluetoothStarted();

    /**
     *  Called after detecting a new aidlab device
     *  @param  device      Device that was detected
     */
    void onDeviceDetected(AidlabDevice device);
}
