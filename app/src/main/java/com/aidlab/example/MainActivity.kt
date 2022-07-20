/**
 *
 * MainActivity.kt
 * Android-Example
 *
 * Created by Michal Baranski on 03.11.2016.
 * Copyright (c) 2016-2020 Aidlab. MIT License.
 *
 */

package com.aidlab.example

import com.aidlab.sdk.communication.*

import android.content.Intent
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.fixedRateTimer

class MainActivity : AppCompatActivity(), AidlabDelegate, AidlabSDKDelegate, AidlabSynchronizationDelegate {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView1 = findViewById(R.id.textView)
        textView2 = findViewById(R.id.textView2)
        textView3 = findViewById(R.id.textView3)
        textView4 = findViewById(R.id.textView4)
        textView5 = findViewById(R.id.textView5)
        textView6 = findViewById(R.id.textView6)
        chartView = findViewById(R.id.chartView)

        completeTextSpace()

        connectedDevice = null

        /// Start the bluetooth check process - Request necessary permissions and ask the user to
        /// enable Bluetooth if it's disabled
        textView1?.text = "Starting bluetooth..."
        aidlabSDK.checkBluetooth(this)

        startTimer()
    }

    private fun  startTimer() {

        if (timerTask != null) return

        timerTask = fixedRateTimer("timer", false, 0, 1000L / 20) {

            runOnUiThread {
                chartView?.update()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        /// Activity result has to be passed to Aidlab system's callback manager for the Bluetooth
        /// check process
        aidlabSDK.callbackManager.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        /// Permissions result has to be passed to Aidlab system's callback manager for the
        /// Bluetooth check process
        aidlabSDK.callbackManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    //-- AidlabSDKDelegate --------------------------------------------------------------------

    override fun onBluetoothStarted() {

        /// Bluetooth check process completed - start scanning for devices
        aidlabSDK.scanForDevices()
    }

    override fun onDeviceScanStarted() {

        textView1?.text = "Looking for devices..."
    }

    override fun onDeviceScanStopped() {

        if (connectedDevice == null) {
            aidlabSDK.clearDeviceList()
            aidlabSDK.scanForDevices()
        }
    }

    override fun onScanFailed(errorCode: Int) {}

    override fun onAidlabDetected(aidlab: Aidlab, rssi: Int) {

        /// Connect to first found device
        if (connectedDevice == null) {

            connectedDevice = aidlab
            lastConnectedDevice = aidlab

            aidlabSDK.stopDeviceScan()
            textView1?.text = "Detected Aidlab. Connecting..."

            /// Wait for the scan to finish and connect to the device
            val dataReceiver = this

            val signals = EnumSet.of(Signal.ecg,  Signal.respiration, Signal.temperature,
                    Signal.motion, Signal.battery, Signal.activity,
                    Signal.orientation, Signal.steps, Signal.heartRate)

            handler.postDelayed({
                connectedDevice?.connect(signals, true,  dataReceiver) /// Connect with all functions active
            }, 100)
        }
    }

    //-- Callbacks from Aidlab ---------------------------------------------------------------------

    override fun didConnectAidlab(aidlab: IAidlab) {
        println("MainActivity: Connected to Aidlab")

        if(connectedDevice == null)
            connectedDevice = lastConnectedDevice

        this.aidlab = aidlab

        runOnUiThread {
            textView1?.text = "Connected to ${connectedDevice?.address()}"
        }
    }

    override fun didDisconnectAidlab(aidlab: IAidlab, disconnectReason: DisconnectReason) {

        runOnUiThread {
            textView1?.text = "Disconnected"
        }
    }

    override fun didReceiveECG(aidlab: IAidlab, timestamp: Long, values: FloatArray) {

        runOnUiThread {
            textView2?.text = "ECG: ${values[0]}"
            values.forEach { chartView?.addECGSample(it) }
        }
    }

    override fun didReceiveRespiration(aidlab: IAidlab, timestamp: Long, values: FloatArray) {

        runOnUiThread {
            textView3?.text = "RESP: ${values[0]}"
            values.forEach { chartView?.addRespirationSample(it) }
        }
    }

    override fun didReceiveBatteryLevel(aidlab: IAidlab, stateOfCharge: Int) {

        runOnUiThread {
            textView4?.text = "Battery state of charge = $stateOfCharge"
        }
    }

    override fun didReceiveSkinTemperature(aidlab: IAidlab, timestamp: Long, value: Float) {

        runOnUiThread {
            textView5?.text = "Skin temperature $value"
        }
    }

    override fun didReceiveHeartRate(aidlab: IAidlab, timestamp: Long, heartRate: Int) {

        runOnUiThread {
            textView6?.text = String.format("HR $heartRate")
        }
    }

    override fun didReceiveHrv(aidlab: IAidlab, timestamp: Long, hrv: Int) {}

    override fun didReceiveAccelerometer(aidlab: IAidlab, timestamp: Long, ax: Float, ay: Float, az: Float) {}

    override fun didReceiveGyroscope(aidlab: IAidlab, timestamp: Long, qx: Float, qy: Float, qz: Float) {}

    override fun didReceiveMagnetometer(aidlab: IAidlab, timestamp: Long, mx: Float, my: Float, mz: Float) {}

    override fun didReceiveQuaternion(aidlab: IAidlab, timestamp: Long, qw: Float, qx: Float, qy: Float, qz: Float) {}

    override fun didReceiveOrientation(aidlab: IAidlab, timestamp: Long, roll: Float, pitch: Float, yaw: Float) {}

    override fun didReceiveBodyPosition(aidlab: IAidlab, timestamp: Long, bodyPosition: BodyPosition) {}

    override fun didReceiveActivity(aidlab: IAidlab, timestamp: Long, activity: ActivityType) {}

    override fun didReceiveSteps(aidlab: IAidlab, timestamp: Long, value: Long) {}

    override fun didReceiveRespirationRate(aidlab: IAidlab, timestamp: Long, value: Int) {}

    override fun didReceiveSignalQuality(aidlab: IAidlab, timestamp: Long, value: Int) {}

    override fun wearStateDidChange(aidlab: IAidlab, wearState: WearState) {}

    override fun didDetectExercise(aidlab: IAidlab, exercise: Exercise) {}

    override fun didReceiveSoundVolume(aidlab: IAidlab, timestamp: Long, value: Int) {}

    override fun didReceiveCommand(aidlab: IAidlab) {}

    override fun didReceiveError(error: String) {}

    override fun syncStateDidChange(aidlab: IAidlab, state: SyncState) {}

    override fun didReceivePastECG(aidlab: IAidlab, timestamp: Long, values: FloatArray) {}

    override fun didReceivePastRespiration(aidlab: IAidlab, timestamp: Long, values: FloatArray) {}

    override fun didReceivePastSkinTemperature(aidlab: IAidlab, timestamp: Long, value: Float) {}

    override fun didReceivePastHeartRate(aidlab: IAidlab, timestamp: Long, heartRate: Int) {}

    override fun didReceiveUnsynchronizedSize(aidlab: IAidlab, unsynchronizedSize: Int, syncBytesPerSecond: Float) {}

    override fun didReceivePastRespirationRate(aidlab: IAidlab, timestamp: Long, value: Int) {}

    override fun didReceivePastSignalQuality(aidlab: IAidlab, timestamp: Long, value: Int) {}

    override fun didReceivePastActivity(aidlab: IAidlab, timestamp: Long, activity: ActivityType) {}

    override fun didReceivePastSteps(aidlab: IAidlab, timestamp: Long, value: Long) {}

    override fun didDetectPastUserEvent(timestamp: Long) {}

    override fun didReceivePastAccelerometer(aidlab: IAidlab, timestamp: Long, ax: Float, ay: Float, az: Float) {}

    override fun didReceivePastBodyPosition(aidlab: IAidlab, timestamp: Long, bodyPosition: BodyPosition) {}

    override fun didReceivePastGyroscope(aidlab: IAidlab, timestamp: Long, qx: Float, qy: Float, qz: Float) {}

    override fun didReceivePastHrv(aidlab: IAidlab, timestamp: Long, hrv: Int) {}

    override fun didReceivePastMagnetometer(aidlab: IAidlab, timestamp: Long, mx: Float, my: Float, mz: Float) {}

    override fun didReceivePastOrientation(aidlab: IAidlab, timestamp: Long, roll: Float, pitch: Float, yaw: Float) {}

    override fun didReceivePastPressure(aidlab: IAidlab, timestamp: Long, values: IntArray) {}

    override fun didReceivePastQuaternion(aidlab: IAidlab, timestamp: Long, qw: Float, qx: Float, qy: Float, qz: Float) {}

    override fun didReceivePastSoundFeatures(aidlab: IAidlab, values: FloatArray) {}

    override fun didReceivePastSoundVolume(aidlab: IAidlab, timestamp: Long, value: Int) {}

    override fun didReceiveMessage(aidlab: IAidlab, process: String, message: String) {}

    //-- Private -----------------------------------------------------------------------------------
    private var timerTask: Timer? = null

    private var aidlab: IAidlab? = null

    private val aidlabSDK: AidlabSDK by lazy {
        AidlabSDK(this, this)
    }

    private var connectedDevice: Aidlab? = null

    private var lastConnectedDevice: Aidlab? = null

    private val handler = Handler(Looper.getMainLooper())

    private var textView1: TextView? = null
    private var textView2: TextView? = null
    private var textView3: TextView? = null
    private var textView4: TextView? = null
    private var textView5: TextView? = null
    private var textView6: TextView? = null
    private var chartView: ChartView? = null

    private fun completeTextSpace() {

        textView2?.text = "ECG  --"
        textView3?.text = "RESP --"
        textView4?.text = "Battery state of charge  --"
        textView5?.text = "Skin temperature --"
        textView6?.text = "HR --"
    }
}