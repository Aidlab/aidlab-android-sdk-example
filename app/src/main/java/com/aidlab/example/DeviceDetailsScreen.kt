package com.aidlab.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun deviceDetailsScreen(
    device: DeviceData,
    onDisconnect: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            deviceInfoCard(label = "Name", value = device.name.value)
            deviceInfoCard(label = "Address", value = device.address.value)
            deviceInfoCard(label = "Firmware revision", value = device.firmwareRevision.value)
            deviceInfoCard(label = "Hardware revision", value = device.hardwareRevision.value)
            deviceInfoCard(label = "State of charge", value = device.battery.value?.toString() ?: "Unknown")
            deviceInfoCard(label = "Wear state", value = device.wearState.value ?: "Unknown")
            deviceInfoCard(label = "Activity", value = device.activity.value ?: "Unknown")
            deviceInfoCard(label = "Steps", value = device.steps.value.toString())
            deviceInfoCard(
                label = "Skin temperature",
                value = device.skinTemperature.value?.let { String.format("%.1f °C", it) } ?: "Unknown",
            )

            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                backgroundColor = Color.White,
                shape = RoundedCornerShape(8.dp),
                elevation = 4.dp,
            ) {
                Column(modifier = Modifier.padding(0.dp, 16.dp)) {
                    EKGChart(ecgSamples = device.ecgSamples.value)
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        Button(
            onClick = onDisconnect,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
        ) {
            Text("Disconnect")
        }
    }
}

@Composable
fun deviceInfoCard(
    label: String,
    value: String,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        backgroundColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.body2)
        }
    }
}
