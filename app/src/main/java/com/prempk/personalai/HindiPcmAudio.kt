package com.prempk.personalai

/** Deterministic helpers for converting Android PCM16 microphone frames to Sherpa float PCM. */
internal object HindiPcmAudio {
    fun toFloatSamples(samples: ShortArray, length: Int = samples.size): FloatArray {
        require(length in 1..samples.size) { "PCM sample length is invalid." }
        return FloatArray(length) { index -> (samples[index].toInt() / 32768f).coerceIn(-1f, 1f) }
    }

    fun validateFloatSamples(samples: FloatArray, sampleRate: Int) {
        require(sampleRate == 16_000) { "Hindi ASR requires 16 kHz mono audio." }
        require(samples.isNotEmpty()) { "No audio was captured." }
        for (sample in samples) {
            require(sample.isFinite() && sample in -1f..1f) {
                "Audio samples must be finite normalized PCM values between -1.0 and 1.0."
            }
        }
    }

    fun rms(samples: ShortArray, length: Int = samples.size): Float {
        require(length in 1..samples.size) { "PCM sample length is invalid." }
        var sumSquares = 0.0
        for (index in 0 until length) {
            val normalized = samples[index].toDouble() / 32768.0
            sumSquares += normalized * normalized
        }
        return kotlin.math.sqrt(sumSquares / length).toFloat()
    }
}
