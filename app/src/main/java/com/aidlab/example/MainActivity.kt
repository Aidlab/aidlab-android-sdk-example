/**
 * Created by Michal Baranski on 03.11.2016.
 * Copyright (c) 2016-2024 Aidlab. MIT License.
 */

package com.aidlab.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.edit
import com.aidlab.sdk.ActivityType
import com.aidlab.sdk.AidlabManager
import com.aidlab.sdk.AidlabManagerDelegate
import com.aidlab.sdk.BodyPosition
import com.aidlab.sdk.DataType
import com.aidlab.sdk.Device
import com.aidlab.sdk.DeviceDelegate
import com.aidlab.sdk.DisconnectReason
import com.aidlab.sdk.Exercise
import com.aidlab.sdk.Logger
import com.aidlab.sdk.SyncState
import com.aidlab.sdk.WearState
import java.util.EnumSet

data class DeviceData(
    val name: MutableState<String>,
    val firmwareRevision: MutableState<String>,
    val hardwareRevision: MutableState<String>,
    val address: MutableState<String>,
    val battery: MutableState<Int?>,
    val wearState: MutableState<String?>,
    val heartRate: MutableState<Int?>,
    val rr: MutableState<Int?>,
    val respirationRate: MutableState<Int?>,
    val skinTemperature: MutableState<Float?>,
    val activity: MutableState<String?>,
    val steps: MutableState<Int>,
    val exercise: MutableState<String?>,
    val ecgSamples: MutableState<List<Float>>,
)

class MainActivity : ComponentActivity(), DeviceDelegate, AidlabManagerDelegate {
    private companion object {
        private const val PREFS_NAME = "aidlab_example"
        private const val KEY_LAST_CONNECTED_ADDRESS = "last_connected_address"
    }

    private lateinit var aidlabManager: AidlabManager

    private var connectedDevice = mutableStateOf<Device?>(null)
    private val detectedDevices = mutableStateListOf<Device>()

    private var deviceData: DeviceData? = null

    private var lastConnectedAddress: String? = null
    private var appIsInBackground: Boolean = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allPermissionsGranted = permissions.entries.all { it.value }
            if (allPermissionsGranted) {
                aidlabManager.scan()
            } else {
                Toast.makeText(this, "Bluetooth permission is required", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkAndStartScan() {
        val requiredPermissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        requestPermissionLauncher.launch(requiredPermissions)
    }

    @Composable
    @Suppress("FunctionName")
    fun MainActivityScreen(detectedDevices: List<Device>) {
        val isScanning = remember { mutableStateOf(false) }

        if (connectedDevice.value != null) {
            DeviceDetailsScreen(device = deviceData!!, onDisconnect = {
                connectedDevice.value?.disconnect()
            })
        } else {
            DeviceScanScreen(
                isScanning = isScanning,
                detectedDevices = detectedDevices,
                onScanClick = { checkAndStartScan() },
                onStopScanClick = {
                    aidlabManager.stopScan()
                },
                onDeviceClick = { device ->
                    rememberLastConnectedDevice(device.address())
                    device.connect(this@MainActivity) // errors via didReceiveError / didDisconnect
                    isScanning.value = false
                    aidlabManager.stopScan()
                },
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        aidlabManager = AidlabManager(this, this)
        lastConnectedAddress = loadLastConnectedAddress()

        setContent {
            MaterialTheme {
                MainActivityScreen(detectedDevices)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appIsInBackground = false
    }

    override fun onStop() {
        super.onStop()
        appIsInBackground = true
        if (connectedDevice.value == null && lastConnectedAddress != null) {
            // Best-effort background scan (requires permissions and, on some devices, a foreground service for reliability).
            aidlabManager.scan()
        }
    }

    // -- AidlabManagerDelegate --------------------------------------------------------------------

    override fun onDeviceScanStarted() {
        Logger.log("MainActivity.onDeviceScanStarted()")
    }

    override fun onDeviceScanStopped() {
        this.detectedDevices.clear()
    }

    override fun onDeviceScanFailed(errorCode: Int) {
        Toast.makeText(
            this,
            "Scan failed: $errorCode",
            Toast.LENGTH_SHORT,
        ).show()
    }

    override fun didDiscover(
        device: Device,
        rssi: Int,
    ) {
        if (appIsInBackground &&
            connectedDevice.value == null &&
            lastConnectedAddress != null &&
            device.address() == lastConnectedAddress
        ) {
            aidlabManager.stopScan()
            device.connect(this@MainActivity) // errors via didReceiveError / didDisconnect
            return
        }

        // Don't add duplicated devices (based on address)
        if (detectedDevices.none { it.address() == device.address() }) {
            detectedDevices.add(device)
        }
    }

    // -- DeviceDelegate ---------------------------------------------------------------------------

    override fun didConnect(device: Device) {
        rememberLastConnectedDevice(device.address())

        deviceData =
            DeviceData(
                name = mutableStateOf(device.name() ?: ""),
                firmwareRevision = mutableStateOf(device.firmwareRevision ?: ""),
                hardwareRevision = mutableStateOf(device.hardwareRevision ?: ""),
                address = mutableStateOf(device.address()),
                battery = mutableStateOf(null),
                wearState = mutableStateOf(null),
                heartRate = mutableStateOf(null),
                rr = mutableStateOf(null),
                respirationRate = mutableStateOf(null),
                skinTemperature = mutableStateOf(null),
                activity = mutableStateOf("Unknown"),
                steps = mutableStateOf(0),
                exercise = mutableStateOf(null),
                ecgSamples = mutableStateOf(emptyList()),
            )
        this.connectedDevice.value = device
        deviceData?.ecgSamples?.value = deviceData?.ecgSamples?.value?.takeLast(0)!!

        val dataTypes =
            EnumSet.of(
                DataType.ECG,
                DataType.HEART_RATE,
                DataType.RESPIRATION,
                DataType.RESPIRATION_RATE,
                DataType.RR,
                DataType.ORIENTATION,
                DataType.SKIN_TEMPERATURE,
                DataType.STEPS,
                DataType.ACTIVITY,
                DataType.MOTION,
            )
        device.collect(dataTypes, dataTypes)
    }

    override fun didDisconnect(
        device: Device,
        disconnectReason: DisconnectReason,
    ) {
        connectedDevice.value = null
        deviceData = null
        detectedDevices.clear()

        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show()

        if (appIsInBackground && lastConnectedAddress != null) {
            aidlabManager.scan()
        }
    }

    private fun loadLastConnectedAddress(): String? {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getString(KEY_LAST_CONNECTED_ADDRESS, null)
    }

    private fun rememberLastConnectedDevice(address: String) {
        lastConnectedAddress = address
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit {
            putString(KEY_LAST_CONNECTED_ADDRESS, address)
        }
    }

    override fun didReceiveECG(
        device: Device,
        timestamp: Long,
        value: Float,
    ) {
        deviceData?.ecgSamples?.value = (deviceData?.ecgSamples?.value!! + value).takeLast(2000)
    }

    override fun didReceiveRespiration(
        device: Device,
        timestamp: Long,
        value: Float,
    ) {}

    override fun didReceiveBatteryLevel(
        device: Device,
        stateOfCharge: Int,
    ) {
        deviceData?.battery?.value = stateOfCharge
    }

    override fun didReceiveSkinTemperature(
        device: Device,
        timestamp: Long,
        value: Float,
    ) {
        deviceData?.skinTemperature?.value = value
    }

    override fun didReceiveAccelerometer(
        device: Device,
        timestamp: Long,
        ax: Float,
        ay: Float,
        az: Float,
    ) {}

    override fun didReceiveGyroscope(
        device: Device,
        timestamp: Long,
        gx: Float,
        gy: Float,
        gz: Float,
    ) {}

    override fun didReceiveMagnetometer(
        device: Device,
        timestamp: Long,
        mx: Float,
        my: Float,
        mz: Float,
    ) {}

    override fun didReceiveQuaternion(
        device: Device,
        timestamp: Long,
        qw: Float,
        qx: Float,
        qy: Float,
        qz: Float,
    ) {}

    override fun didReceiveOrientation(
        device: Device,
        timestamp: Long,
        roll: Float,
        pitch: Float,
        yaw: Float,
    ) {}

    override fun didReceiveEDA(
        device: Device,
        timestamp: Long,
        conductance: Float,
    ) {
        // Example app currently does not visualize EDA – hook kept for completeness.
    }

    override fun didReceiveGPS(
        device: Device,
        timestamp: Long,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        speed: Float,
        heading: Float,
        hdop: Float,
    ) {
        // GPS samples are ignored in UI; add handling here if needed.
    }

    override fun didReceiveBodyPosition(
        device: Device,
        timestamp: Long,
        bodyPosition: BodyPosition,
    ) {
        deviceData?.wearState?.value = bodyPosition.toString()
    }

    override fun didReceiveActivity(
        device: Device,
        timestamp: Long,
        activity: ActivityType,
    ) {
        deviceData?.activity?.value = activity.toString()
    }

    override fun didReceiveSteps(
        device: Device,
        timestamp: Long,
        value: Long,
    ) {
        deviceData?.steps?.value?.let {
            deviceData?.steps?.value = it + value.toInt()
        }
    }

    override fun didReceiveHeartRate(
        device: Device,
        timestamp: Long,
        heartRate: Int,
    ) {
        deviceData?.heartRate?.value = heartRate
    }

    override fun didReceiveRr(
        device: Device,
        timestamp: Long,
        rr: Int,
    ) {
        deviceData?.rr?.value = rr
    }

    override fun didReceivePressure(
        device: Device,
        timestamp: Long,
        value: Int,
    ) {
    }

    override fun pressureWearStateDidChange(
        device: Device,
        wearState: WearState,
    ) {
    }

    override fun didReceiveRespirationRate(
        device: Device,
        timestamp: Long,
        value: Int,
    ) {
        deviceData?.respirationRate?.value = value
    }

    override fun wearStateDidChange(
        device: Device,
        wearState: WearState,
    ) {
        deviceData?.wearState?.value = wearState.toString()
    }

    override fun didDetectExercise(
        device: Device,
        exercise: Exercise,
    ) {
        deviceData?.exercise?.value = exercise.toString()
    }

    override fun didReceiveSoundVolume(
        device: Device,
        timestamp: Long,
        value: Int,
    ) {}

    override fun didReceiveError(
        device: Device,
        error: String,
    ) {
        Logger.debug("Error: $error")
    }

    override fun syncStateDidChange(
        device: Device,
        state: SyncState,
    ) {}

    override fun didReceiveUnsynchronizedSize(
        device: Device,
        unsynchronizedSize: Int,
        syncBytesPerSecond: Float,
    ) {}

    override fun didReceivePastECG(
        device: Device,
        timestamp: Long,
        value: Float,
    ) {}

    override fun didReceivePastRespiration(
        device: Device,
        timestamp: Long,
        value: Float,
    ) {}

    override fun didReceivePastSkinTemperature(
        device: Device,
        timestamp: Long,
        value: Float,
    ) {}

    override fun didReceivePastHeartRate(
        device: Device,
        timestamp: Long,
        heartRate: Int,
    ) {}

    override fun didReceivePastRr(
        device: Device,
        timestamp: Long,
        rr: Int,
    ) {}

    override fun didReceivePastRespirationRate(
        device: Device,
        timestamp: Long,
        value: Int,
    ) {}

    override fun didReceivePastActivity(
        device: Device,
        timestamp: Long,
        activity: ActivityType,
    ) {}

    override fun didReceivePastSteps(
        device: Device,
        timestamp: Long,
        value: Long,
    ) {}

    override fun didReceivePastAccelerometer(
        device: Device,
        timestamp: Long,
        ax: Float,
        ay: Float,
        az: Float,
    ) {}

    override fun didReceivePastGyroscope(
        device: Device,
        timestamp: Long,
        gx: Float,
        gy: Float,
        gz: Float,
    ) {}

    override fun didReceivePastMagnetometer(
        device: Device,
        timestamp: Long,
        mx: Float,
        my: Float,
        mz: Float,
    ) {}

    override fun didReceivePastQuaternion(
        device: Device,
        timestamp: Long,
        qw: Float,
        qx: Float,
        qy: Float,
        qz: Float,
    ) {}

    override fun didReceivePastOrientation(
        device: Device,
        timestamp: Long,
        roll: Float,
        pitch: Float,
        yaw: Float,
    ) {}

    override fun didReceivePastEDA(
        device: Device,
        timestamp: Long,
        conductance: Float,
    ) {}

    override fun didReceivePastGPS(
        device: Device,
        timestamp: Long,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        speed: Float,
        heading: Float,
        hdop: Float,
    ) {}

    override fun didReceivePastBodyPosition(
        device: Device,
        timestamp: Long,
        bodyPosition: BodyPosition,
    ) {}

    override fun didReceivePastSoundVolume(
        device: Device,
        timestamp: Long,
        value: Int,
    ) {}

    override fun didReceivePastPressure(
        device: Device,
        timestamp: Long,
        value: Int,
    ) {}

    override fun didDetectPastUserEvent(
        device: Device,
        timestamp: Long,
    ) {}

    override fun didReceivePastSignalQuality(
        device: Device,
        timestamp: Long,
        value: Int,
    ) {}

    override fun didReceivePayload(
        device: Device,
        process: String,
        payload: ByteArray,
    ) {
        Logger.debug("Payload from $process (${payload.size} B)")
    }

    override fun didReceiveSignalQuality(
        device: Device,
        timestamp: Long,
        value: Int,
    ) {}
}
