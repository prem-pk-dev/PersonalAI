package com.prempk.personalai

import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineNemoEncDecCtcModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import java.io.Closeable
import java.io.File

/**
 * One-shot offline Hindi transcription using Sherpa-ONNX's NeMo CTC API.
 * Call from a worker thread; feed mono 16 kHz float PCM from the microphone.
 */
class HindiOfflineAsrEngine(
    modelDirectory: File,
    numThreads: Int = 2
) : Closeable {
    private val recognizer: OfflineRecognizer

    init {
        require(numThreads in 1..4) { "Hindi ASR thread count must be between 1 and 4." }
        val model = File(modelDirectory, "model.int8.onnx")
        val tokens = File(modelDirectory, "tokens.txt")
        require(model.isFile && model.length() > 0L) { "Hindi ASR model is not installed." }
        require(tokens.isFile && tokens.length() > 0L) { "Hindi ASR vocabulary is not installed." }

        recognizer = OfflineRecognizer(
            config = OfflineRecognizerConfig(
                featConfig = FeatureConfig(sampleRate = SAMPLE_RATE, featureDim = 80),
                modelConfig = OfflineModelConfig(
                    nemo = OfflineNemoEncDecCtcModelConfig(model = model.absolutePath),
                    tokens = tokens.absolutePath,
                    numThreads = numThreads,
                    provider = "cpu"
                ),
                decodingMethod = "greedy_search"
            )
        )
    }

    fun transcribe(samples: FloatArray, sampleRate: Int = SAMPLE_RATE): String {
        HindiPcmAudio.validateFloatSamples(samples, sampleRate)
        val stream = recognizer.createStream()
        return try {
            stream.acceptWaveform(samples, sampleRate)
            recognizer.decode(stream)
            recognizer.getResult(stream).text.trim()
        } finally {
            stream.release()
        }
    }

    override fun close() {
        recognizer.release()
    }

    companion object {
        const val SAMPLE_RATE = 16_000
    }
}
