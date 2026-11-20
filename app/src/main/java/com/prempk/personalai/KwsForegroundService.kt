package com.prempk.personalai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import com.k2fsa.sherpa.onnx.KeywordSpotter
import com.k2fsa.sherpa.onnx.OnlineStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * User-started offline PK keyword listener.
 *
 * The service is intentionally not auto-started. A visible Activity must start it
 * after RECORD_AUDIO permission and the model have been granted/installed.
 */
class KwsForegroundService : Service() {
    private val running = AtomicBoolean(false)
    private val captureStartGate = KwsCaptureStartGate()
    private var worker: Thread? = null
    private var recorder: AudioRecord? = null
    private var spotter: KeywordSpotter? = null
    private var stream: OnlineStream? = null
    private var cpuWakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putBoolean(KEY_KWS_ENABLED, false).apply()
                stopCapture()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START, null -> {
                val explicitlyEnabled = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .getBoolean(KEY_KWS_ENABLED, false)
                if (intent == null && !explicitlyEnabled) {
                    stopSelf(startId)
                    return START_NOT_STICKY
                }
                if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    stopSelf(startId)
                    return START_NOT_STICKY
                }
                createNotificationChannel()
                startForegroundCompat(buildNotification(getString(R.string.kws_notification_listening), ongoing = true))
                acquireCpuWakeLock()
                if (!running.get()) startCapture()
            }
            else -> {
                stopSelf(startId)
                return START_NOT_STICKY
            }
        }
        // The listener is user-enabled and notification-visible. Sticky restart lets
        // Android recreate it after reclaiming the process; a null restart intent is
        // accepted only while the user's persisted opt-in remains enabled.
        return START_STICKY
    }

    private fun startForegroundCompat(notification: Notification) {
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun acquireCpuWakeLock() {
        if (cpuWakeLock?.isHeld == true) return
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        cpuWakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "$packageName:PKWakeWord"
        ).apply {
            setReferenceCounted(false)
            acquire()
        }
    }

    private fun releaseCpuWakeLock() {
        runCatching {
            if (cpuWakeLock?.isHeld == true) cpuWakeLock?.release()
        }
        cpuWakeLock = null
    }

    private fun startCapture() {
        val session = captureStartGate.reset()
        running.set(true)
        worker = thread(name = "PK-KWS-Audio", start = true) {
            var localSpotter: KeywordSpotter? = null
            var localStream: OnlineStream? = null
            var localRecorder: AudioRecord? = null
            var captureFailed = false
            try {
                val modelDir = KwsModelInstaller.modelDirectory(this)
                check(KwsModelInstaller.isInstalled(this)) { "Offline wake-word model is not installed." }
                localSpotter = KwsModelInstaller.createKeywordSpotter(modelDir)
                localStream = localSpotter.createStream()
                val minBuffer = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                check(minBuffer > 0) { "AudioRecord minimum buffer size is unavailable." }
                checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ||
                    error("Microphone permission was revoked before audio capture started.")
                localRecorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    maxOf(minBuffer * 2, SAMPLE_RATE / 5 * 2)
                )
                check(localRecorder.state == AudioRecord.STATE_INITIALIZED) { "Microphone could not be initialized." }
                // Serialize the final start gate with Stop so a late model/recorder
                // initialization cannot start microphone capture after the user stops.
                val started = captureStartGate.startIfAllowed(session) {
                    if (running.get()) {
                        spotter = localSpotter
                        stream = localStream
                        recorder = localRecorder
                        localRecorder.startRecording()
                    }
                } && running.get()
                if (!started) return@thread
                val pcm = ShortArray(SAMPLE_RATE / 10)
                while (running.get() && captureStartGate.isCurrent(session)) {
                    val count = localRecorder.read(pcm, 0, pcm.size)
                    if (count > 0) {
                        val samples = FloatArray(count) { pcm[it] / 32768.0f }
                        localStream.acceptWaveform(samples, sampleRate = SAMPLE_RATE)
                        while (running.get() && captureStartGate.isCurrent(session) && localSpotter.isReady(localStream)) {
                            localSpotter.decode(localStream)
                            if (localSpotter.getResult(localStream).keyword.isNotBlank()) {
                                localSpotter.reset(localStream)
                                if (captureStartGate.isCurrent(session)) onWakeDetected()
                                return@thread
                            }
                        }
                    } else if (count < 0) {
                        error("AudioRecord.read failed with code $count")
                    }
                }
            } catch (error: Throwable) {
                // JNI initialization can fail with LinkageError (not Exception).
                // Keep that failure inside the isolated KWS process and surface it
                // through the foreground notification instead of taking down the UI.
                if (error is VirtualMachineError || error is ThreadDeath) throw error
                if (running.get()) {
                    captureFailed = true
                    Log.e(TAG, "Offline wake-word listener stopped", error)
                } else {
                    Log.d(TAG, "Wake-word capture stopped by request", error)
                }
            } finally {
                val ownsSession = captureStartGate.isCurrent(session)
                if (ownsSession) running.set(false)
                // Isolate JNI cleanup failures so every native resource is attempted.
                NativeResourceCleanup.releaseAll(
                    { localRecorder?.stop() },
                    { localRecorder?.release() },
                    { localStream?.release() },
                    { localSpotter?.release() }
                )
                captureStartGate.withLock {
                    if (recorder === localRecorder) recorder = null
                    if (stream === localStream) stream = null
                    if (spotter === localSpotter) spotter = null
                }
                if (ownsSession) releaseCpuWakeLock()
                if (captureFailed && ownsSession) {
                    // A failed capture must not leave an idle foreground microphone service
                    // or a persisted opt-in that falsely claims the listener is active.
                    Handler(Looper.getMainLooper()).post {
                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                            .putBoolean(KEY_KWS_ENABLED, false).apply()
                        updateNotification(getString(R.string.kws_notification_error))
                        stopForeground(STOP_FOREGROUND_DETACH)
                        stopSelf()
                    }
                }
            }
        }
    }

    private fun onWakeDetected() {
        running.set(false)
        val launch = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_WAKE_DETECTED, true)
        }
        val pending = PendingIntent.getActivity(
            this, 2, launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = buildNotification(
            getString(R.string.kws_notification_detected),
            pending,
            detected = true
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
        sendBroadcast(Intent(ACTION_WAKE_DETECTED).setPackage(packageName))
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    private fun buildNotification(
        message: String,
        contentIntent: PendingIntent? = mainPendingIntent(),
        detected: Boolean = false,
        ongoing: Boolean = running.get() && !detected
    ): Notification {
        val stopIntent = PendingIntent.getService(
            this, 3,
            Intent(this, KwsForegroundService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setContentIntent(contentIntent)
                .setOngoing(ongoing)
                .setAutoCancel(detected)
                .setCategory(Notification.CATEGORY_SERVICE)
                .addAction(Notification.Action.Builder(null, getString(R.string.kws_notification_stop), stopIntent).build())
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setContentIntent(contentIntent)
                .setOngoing(ongoing)
                .setAutoCancel(detected)
                .setPriority(if (detected) Notification.PRIORITY_HIGH else Notification.PRIORITY_LOW)
                .addAction(android.R.drawable.ic_media_pause, getString(R.string.kws_notification_stop), stopIntent)
                .build()
        }
    }

    private fun mainPendingIntent(): PendingIntent {
        val launch = PendingIntent.getActivity(
            this, 1, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return launch
    }

    private fun updateNotification(message: String) {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification(message))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID, getString(R.string.kws_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun stopCapture() {
        captureStartGate.stop {
            running.set(false)
            runCatching { recorder?.stop() }
            worker?.interrupt()
            worker = null
        }
        releaseCpuWakeLock()
    }

    override fun onDestroy() {
        stopCapture()
        super.onDestroy()
    }

    companion object {
        private const val PREFS_NAME = "pk_voice_preferences"
        private const val KEY_KWS_ENABLED = "background_kws_enabled"
        const val ACTION_START = "com.prempk.personalai.action.START_KWS"
        const val ACTION_STOP = "com.prempk.personalai.action.STOP_KWS"
        const val ACTION_WAKE_DETECTED = "com.prempk.personalai.action.WAKE_DETECTED"
        const val EXTRA_WAKE_DETECTED = "wake_detected"
        private const val TAG = "PK-KWS"
        private const val CHANNEL_ID = "pk_wake_listener_v3"
        private const val NOTIFICATION_ID = 4201
        private const val SAMPLE_RATE = 16_000
    }
}
