package com.prempk.personalai

import android.content.Context
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Downloads the Hindi IndicConformer CTC model on explicit user request.
 * The ONNX model is pinned to an immutable Hugging Face revision and SHA-256.
 * It is stored in app-private files and is used offline after installation.
 */
object HindiOfflineModelInstaller {
    const val MODEL_DIRECTORY = "pk-hindi-indicconformer"
    const val MODEL_SHA256 = "915c71e04dd7e5378a4057fdebb252b3a587188e4e99db6d7ce0909ad5ad05fa"
    const val MODEL_REVISION = "9721eb71eea141fae0982cfcdb9dd2e3d4953c4a"
    const val MODEL_URL =
        "https://huggingface.co/parismitaglobalsolutions/indicconformer-sherpa-onnx/resolve/$MODEL_REVISION/hi/model.int8.onnx"
    const val TOKENS_URL =
        "https://huggingface.co/parismitaglobalsolutions/indicconformer-sherpa-onnx/resolve/$MODEL_REVISION/tokens.txt"

    private const val MODEL_FILE = "model.int8.onnx"
    private const val TOKENS_FILE = "tokens.txt"
    private const val MARKER_FILE = ".install-complete"
    private const val MARKER_CONTENT = "$MODEL_REVISION:$MODEL_SHA256"

    fun modelDirectory(context: Context): File = File(context.filesDir, MODEL_DIRECTORY)

    fun isInstalled(context: Context): Boolean = isModelDirectoryReady(modelDirectory(context))

    internal fun isModelDirectoryReady(directory: File): Boolean {
        val model = File(directory, MODEL_FILE)
        val tokens = File(directory, TOKENS_FILE)
        val marker = File(directory, MARKER_FILE)
        return directory.isDirectory && model.isFile && model.length() > 0L &&
            tokens.isFile && tokens.length() > 0L &&
            marker.isFile && runCatching { marker.readText() == MARKER_CONTENT }.getOrDefault(false)
    }

    @Synchronized
    fun install(context: Context, onProgress: (String, Int) -> Unit = { _, _ -> }): File {
        val destination = modelDirectory(context)
        if (isModelDirectoryReady(destination)) return destination

        val staging = File(context.filesDir, "$MODEL_DIRECTORY.installing")
        staging.deleteRecursively()
        check(staging.mkdirs()) { "Could not create Hindi model staging directory." }
        val modelPart = File(staging, "$MODEL_FILE.part")
        val tokensPart = File(staging, "$TOKENS_FILE.part")

        try {
            download(MODEL_URL, modelPart) { percent ->
                onProgress("Downloading Hindi offline model…", percent)
            }
            onProgress("Verifying Hindi model integrity…", 95)
            check(sha256(modelPart) == MODEL_SHA256) {
                "Hindi model SHA-256 verification failed. The incomplete download was discarded."
            }
            download(TOKENS_URL, tokensPart) { percent ->
                onProgress("Downloading Hindi vocabulary…", percent)
            }
            check(tokensPart.length() in 1L..1_000_000L) {
                "Hindi vocabulary file is empty or unexpectedly large."
            }

            check(modelPart.renameTo(File(staging, MODEL_FILE))) { "Could not finalize Hindi model file." }
            check(tokensPart.renameTo(File(staging, TOKENS_FILE))) { "Could not finalize Hindi vocabulary file." }
            File(staging, MARKER_FILE).writeText(MARKER_CONTENT, Charsets.UTF_8)

            check(HindiModelDirectoryCommit.promote(staging, destination)) {
                "Could not safely finalize Hindi model installation."
            }
            check(isModelDirectoryReady(destination)) { "Hindi model installation verification failed." }
            onProgress("Hindi offline model is ready.", 100)
            return destination
        } catch (error: Exception) {
            staging.deleteRecursively()
            throw error
        }
    }

    private fun download(url: String, destination: File, onProgress: (Int) -> Unit) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 60_000
            instanceFollowRedirects = true
            requestMethod = "GET"
        }
        try {
            check(connection.responseCode in 200..299) {
                "Model download failed with HTTP ${connection.responseCode}."
            }
            val total = connection.contentLengthLong
            BufferedInputStream(connection.inputStream).use { input ->
                FileOutputStream(destination).use { output ->
                    val buffer = ByteArray(128 * 1024)
                    var downloaded = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        downloaded += count
                        if (total > 0L) onProgress(((downloaded * 100L / total).coerceIn(0L, 100L)).toInt())
                    }
                    output.fd.sync()
                }
            }
            check(destination.isFile && destination.length() > 0L) { "Downloaded file is empty." }
        } finally {
            connection.disconnect()
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(128 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
