package com.aidlab.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun EKGChart(ecgSamples: List<Float>) {
    val minSample = ecgSamples.minOrNull() ?: 0f
    val maxSample = ecgSamples.maxOrNull() ?: 0f

    val buffer = (maxSample - minSample) * 0.1f
    val adjustedMinSample = minSample - buffer
    val adjustedMaxSample = maxSample + buffer

    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val maxSamples = 2000
        val sampleSpacing = size.width / maxSamples

        fun scaleSample(sample: Float): Float {
            return (sample - adjustedMinSample) / (adjustedMaxSample - adjustedMinSample) * size.height
        }

        var lastX = 0f
        ecgSamples.takeLast(maxSamples).forEachIndexed { index, sample ->
            if (index > 0) {
                val startX = lastX
                val startY = size.height - scaleSample(ecgSamples[index - 1])
                val stopX = startX + sampleSpacing
                val stopY = size.height - scaleSample(sample)

                drawLine(
                    color = Color.Blue,
                    start = Offset(startX, startY),
                    end = Offset(stopX, stopY),
                    strokeWidth = Stroke.DefaultMiter,
                )
            }
            lastX += sampleSpacing
        }
    }
}
