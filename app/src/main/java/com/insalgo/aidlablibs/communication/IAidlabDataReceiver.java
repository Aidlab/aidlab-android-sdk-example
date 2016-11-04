package com.insalgo.aidlablibs.communication;

/**
 *
 * IAidlabDataReceiver.java
 * Android-Example
 *
 * Created by Michal Baranski on 03.11.2016.
 * Copyright (c) 2016 Aidlab. MIT License.
 *
 */

/**
 *  Implement this interface to listen for device-specific events.
 *  All methods are called from an external thread.
 */
public interface IAidlabDataReceiver {

    /**
     *  Called after establishing connection with the device
     */
    void onConnected();

    /**
     *  Called after the connection was closed
     */
    void onDisconnected();

    /**
     *  Called when battery status data were received from the device
     *  @param  soc         Battery level in % (0 - 100)
     *  @param  inCharge    True if Aidlab is connected to a power source through USB cable
     */
    void onBatteryStatusReceived(float soc, boolean inCharge);

    /**
     *  Called when temperature data were received from the device
     *  @param  objectTemperature       Object temperature from directional sensor in °C
     *  @param  ambientTemperature      Ambient temperature from built-in hidden sensor in °C
     */
    void onTemperatureReceived(float objectTemperature, float ambientTemperature);

    /**
     *  Called when ECG stream data were received from the device
     *  @param  ecgValues     Subsequent ECG samples
     */
    void onECGReceived(float[] ecgValues);

    /**
     *  Called when Respiration stream data were received from the device
     *  @param  respirationValues     Subsequent Respiration samples
     */
    void onRespirationReceived(float[] respirationValues);
}
