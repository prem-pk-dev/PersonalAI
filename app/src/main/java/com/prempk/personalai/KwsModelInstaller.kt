package com.prempk.personalai

import android.content.Context
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.KeywordSpotter
import com.k2fsa.sherpa.onnx.KeywordSpotterConfig
import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Installs the official, English-only mobile KWS model into app-private storage.
 * The archive is downloaded only when the user explicitly starts model setup.
 */
object KwsModelInstaller {
    const val MODEL_DIRECTORY = "sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01-mobile"
    const val ARCHIVE_SHA256 = "2e6ac2577310bfa2f4b6b5fab0478b868c9d0b2cb2c51b3e13b50581b588864d"
    const val ARCHIVE_URL =
        "https://github.com/k2-fsa/sherpa-onnx/releases/download/kws-models/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01-mobile.tar.bz2"

    private val requiredFiles = setOf(
        "encoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx",
        "decoder-epoch-12-avg-2-chunk-16-left-64.onnx",
        "joiner-epoch-12-avg-2-chunk-16-left-64.int8.onnx",
        "tokens.txt",
        "bpe.model",
        "keywords.txt"
    )

    fun modelDirectory(context: Context): File =
        File(context.filesDir, MODEL_DIRECTORY)

    fun isInstalled(context: Context): Boolean {
        val dir = modelDirectory(context)
        return dir.isDirectory && requiredFiles.all { File(dir, it).length() > 0L }
    }

    @Synchronized
    fun install(context: Context, onProgress: (String) -> Unit = {}): File {
        val destination = modelDirectory(context)
        if (isInstalled(context)) return destination

        val archive = File(context.cacheDir, "kws-model.tar.bz2.part")
        val staging = File(context.filesDir, "$MODEL_DIRECTORY.installing")
        archive.parentFile?.mkdirs()
        staging.deleteRecursively()
        staging.mkdirs()

        try {
            onProgress("Downloading offline wake-word model…")
            val connection = (URL(ARCHIVE_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 20_000
                readTimeout = 60_000
                instanceFollowRedirects = true
                requestMethod = "GET"
            }
            try {
                connection.inputStream.use { input ->
                    FileOutputStream(archive).use { output -> input.copyTo(output) }
                }
            } finally {
                connection.disconnect()
            }

            onProgress("Verifying model integrity…")
            check(sha256(archive) == ARCHIVE_SHA256) {
                "The downloaded wake-word model failed its SHA-256 integrity check."
            }

            onProgress("Extracting model files…")
            val extracted = mutableSetOf<String>()
            FileInputStream(archive).use { fileInput ->
                BufferedInputStream(fileInput).use { buffered ->
                    BZip2CompressorInputStream(buffered).use { bz ->
                        TarArchiveInputStream(bz).use { tar ->
                            while (true) {
                                val entry = tar.nextTarEntry ?: break
                                if (entry.isDirectory) continue
                                val name = entry.name.substringAfterLast('/')
                                if (name !in requiredFiles) continue
                                check(!entry.isSymbolicLink && !entry.isLink) {
                                    "Unexpected link entry in model archive."
                                }
                                val outputFile = File(staging, name)
                                BufferedOutputStream(FileOutputStream(outputFile)).use { output ->
                                    tar.copyTo(output)
                                }
                                extracted += name
                            }
                        }
                    }
                }
            }

            check(extracted.containsAll(requiredFiles)) {
                "The model archive is incomplete. Missing: ${requiredFiles - extracted}"
            }

            destination.deleteRecursively()
            check(staging.renameTo(destination)) { "Could not finalize offline model installation." }
            onProgress("Offline wake-word model is ready.")
            return destination
        } catch (error: Exception) {
            staging.deleteRecursively()
            throw error
        } finally {
            archive.delete()
        }
    }

    fun createKeywordSpotter(modelDirectory: File): KeywordSpotter {
        require(requiredFiles.all { File(modelDirectory, it).isFile }) {
            "Wake-word model files are missing. Install the model first."
        }
        val modelPath = modelDirectory.absolutePath
        val config = KeywordSpotterConfig(
            featConfig = FeatureConfig(sampleRate = 16_000, featureDim = 80),
            modelConfig = OnlineModelConfig(
                transducer = OnlineTransducerModelConfig(
                    encoder = "$modelPath/encoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx",
                    decoder = "$modelPath/decoder-epoch-12-avg-2-chunk-16-left-64.onnx",
                    joiner = "$modelPath/joiner-epoch-12-avg-2-chunk-16-left-64.int8.onnx"
                ),
                tokens = "$modelPath/tokens.txt",
                modelType = "zipformer2",
                modelingUnit = "bpe",
                bpeVocab = "$modelPath/bpe.model",
                numThreads = 2,
                provider = "cpu"
            ),
            keywordsFile = "$modelPath/keywords.txt",
            keywordsScore = 1.5f,
            keywordsThreshold = 0.25f
        )
        return KeywordSpotter(assetManager = null, config = config)
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
