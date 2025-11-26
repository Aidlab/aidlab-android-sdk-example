package com.aidlab.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aidlab.sdk.Device

@Composable
fun DeviceScanScreen(
    isScanning: MutableState<Boolean>,
    detectedDevices: List<Device>,
    onScanClick: () -> Unit,
    onStopScanClick: () -> Unit,
    onDeviceClick: (Device) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.LightGray)
                .statusBarsPadding(),
    ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(detectedDevices) { device ->
                DeviceListItem(device = device, onDeviceClick = onDeviceClick)
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                onClick = {
                    if (isScanning.value) {
                        onStopScanClick()
                    } else {
                        onScanClick()
                    }
                    isScanning.value = !isScanning.value
                },
            ) {
                Text(text = if (isScanning.value) "Stop Scan" else "Start Scan")
            }
        }
    }
}
