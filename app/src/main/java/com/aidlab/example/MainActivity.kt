/**
 *
 * MainActivity.kt
 * Android-Example
 *
 * Created by Michal Baranski on 03.11.2016.
 * Copyright (c) 2016-2019 Aidlab. MIT License.
 *
 */

package com.aidlab.example

import android.content.Intent
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

import com.aidlab.sdk.communication.Aidlab
import com.aidlab.sdk.communication.AidlabDelegate
import com.aidlab.sdk.communication.CentralManager
import com.aidlab.sdk.communication.CentralManagerDelegate

class MainActivity : AppCompatActivity(), AidlabDelegate, CentralManagerDelegate {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        textView1 = findViewById(R.id.textView)
        textView2 = findViewById(R.id.textView2)
        textView3 = findViewById(R.id.textView3)
        textView4 = findViewById(R.id.textView4)

        connectedAidlab = null
        centralManager = CentralManager(this, this)

        /// Start the Bluetooth check process - Request necessary permissions and ask the user to
        /// enable bluetooth if it's disabled
        centralManager?.checkBluetooth(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

        super.onActivityResult(requestCode, resultCode, data)

        /// Activity result has to be passed to Aidlab's system callback manager for the Bluetooth
        /// check process
        centralManager?.callbackManager?.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        /// Permissions result has to be passed to Aidlab's system callback manager for the
        /// Bluetooth check process
        centralManager?.callbackManager?.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {}

    //-- CentralManagerDelegate --------------------------------------------------------------------

    override fun onBluetoothStarted() {

        /// Bluetooth check process completed - start scanning for Aidlab
        centralManager?.scanForDevices()
    }

    override fun onAidlabDetected(aidlab: Aidlab) {

        /// Connect to first discovered Aidlab
        if (connectedAidlab == null) {

            centralManager?.stopDeviceScan()

            /// Wait for the scan to finish and connect to Aidlab
            val mainActivity = this
            Handler().postDelayed({
                connectedAidlab = aidlab
                aidlab.connect(mainActivity)
            }, 100)
        }
    }

    override fun onDeviceScanStarted() {}

    override fun onDeviceScanStopped() {}

    //-- AidlabDelegate ----------------------------------------------------------------------------

    override fun didConnectAidlab() {}

    override fun didDisconnectAidlab() {

        runOnUiThread {

            connectedAidlab = null
        }
    }

    override fun didReceiveECG(ecgSample: Float, heartRate: Int) {

        runOnUiThread { textView3?.text = "ECG: $ecgSample" }
    }

    override fun didReceiveRespiration(respirationSample: Float, respirationRate: Byte?) {

        runOnUiThread { textView4?.text = "RESP: $respirationSample" }
    }

    override fun didReceiveTemperature(objectTemperature: Float, ambientTemperature: Float, bodyTemperature: Float) {

        runOnUiThread { textView2?.text = "Tobj=$objectTemperature Tamb=$ambientTemperature" }
    }

    override fun didReceiveBatteryStatus(soc: Float) {

        runOnUiThread { textView1?.text = "soc: $soc" }
    }

    override fun aidlabPositioned(correctly: Boolean) {}

    override fun onMotionReceived(qw: Float, qx: Float, qy: Float, qz: Float, ax: Float, ay: Float, az: Float) {}

    override fun onJump() {}

    override fun onPushUp() {}

    override fun onSitUp() {}

    //-- Private -----------------------------------------------------------------------------------

    private var centralManager: CentralManager? = null

    private var connectedAidlab: Aidlab? = null

    private var textView1: TextView? = null
    private var textView2: TextView? = null
    private var textView3: TextView? = null
    private var textView4: TextView? = null
}
