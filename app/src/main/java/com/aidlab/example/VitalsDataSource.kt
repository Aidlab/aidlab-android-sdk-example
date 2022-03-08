package com.aidlab.example
import java.nio.FloatBuffer

class VitalsDataSource(private val maxSamples: Int = 1000)  {

    //-- Public ------------------------------------------------------------------------------------

    fun add(sample: Float) {

        i++

        samples.add(sample)

        if( samples.size > maxSamples )
            samples.removeAt(0)
    }

    fun clear() {

        i = 0
        normalizedSamples.clear()
        samples.clear()
    }

    fun normalize(upperBound: Float = 1f, lowerBound: Float = -1f) {

        val currentMax = samples.max() ?: 0.0f
        val currentMin = samples.min() ?: 0.0f

        var maxmin = currentMax - currentMin
        if( maxmin == 0.0f ) maxmin = 1.0f

        for (i in 0 until samples.count()) {

            val sample = (((upperBound - lowerBound) * (samples[i] - currentMin)) / maxmin) + lowerBound
            normalizedSamples.put(i, sample)
        }
    }

    fun samples(): FloatBuffer? {

        return normalizedSamples
    }

    //-- Private -----------------------------------------------------------------------------------

    private var i: Int = 0
    private var normalizedSamples: FloatBuffer = FloatBuffer.allocate(maxSamples)
    private var samples: MutableList<Float> = ArrayList()
}
