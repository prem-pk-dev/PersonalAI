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
    private var worker: Thread? = null
    private var recorder: AudioRecord? = null
    private var spotter: KeywordSpotter? = null
    private var stream: OnlineStream? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopCapture()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    stopSelf(startId)
                    return START_NOT_STICKY
                }
                startForegroundCompat(buildNotification(getString(R.string.kws_notification_listening)))
                if (!running.get()) startCapture()
            }
            else -> {
                stopSelf(startId)
                return START_NOT_STICKY
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundCompat(notification: Notification) {
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startCapture() {
        running.set(true)
        worker = thread(name = "PK-KWS-Audio", start = true) {
            var localSpotter: KeywordSpotter? = null
            var localStream: OnlineStream? = null
            var localRecorder: AudioRecord? = null
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
                spotter = localSpotter
                stream = localStream
                recorder = localRecorder
                localRecorder.startRecording()
                val pcm = ShortArray(SAMPLE_RATE / 10)
                while (running.get()) {
                    val count = localRecorder.read(pcm, 0, pcm.size)
                    if (count > 0) {
                        val samples = FloatArray(count) { pcm[it] / 32768.0f }
                        localStream.acceptWaveform(samples, sampleRate = SAMPLE_RATE)
                        while (running.get() && localSpotter.isReady(localStream)) {
                            localSpotter.decode(localStream)
                            if (localSpotter.getResult(localStream).keyword.isNotBlank()) {
                                localSpotter.reset(localStream)
                                onWakeDetected()
                                return@thread
                            }
                        }
                    } else if (count < 0) {
                        error("AudioRecord.read failed with code $count")
                    }
                }
            } catch (error: Exception) {
                Log.e(TAG, "Offline wake-word listener stopped", error)
                updateNotification(getString(R.string.kws_notification_error))
            } finally {
                running.set(false)
                runCatching { localRecorder?.stop() }
                localRecorder?.release()
                localStream?.release()
                localSpotter?.release()
                recorder = null
                stream = null
                spotter = null
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
        val notification = buildNotification(getString(R.string.kws_notification_detected), pending)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
        sendBroadcast(Intent(ACTION_WAKE_DETECTED).setPackage(packageName))
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    private fun buildNotification(message: String, contentIntent: PendingIntent? = mainPendingIntent()): Notification {
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
                .setOngoing(running.get())
                .addAction(Notification.Action.Builder(null, getString(R.string.kws_notification_stop), stopIntent).build())
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setContentIntent(contentIntent)
                .setOngoing(running.get())
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
        running.set(false)
        runCatching { recorder?.stop() }
        worker?.interrupt()
        worker = null
    }

    override fun onDestroy() {
        stopCapture()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.prempk.personalai.action.START_KWS"
        const val ACTION_STOP = "com.prempk.personalai.action.STOP_KWS"
        const val ACTION_WAKE_DETECTED = "com.prempk.personalai.action.WAKE_DETECTED"
        const val EXTRA_WAKE_DETECTED = "wake_detected"
        private const val TAG = "PK-KWS"
        private const val CHANNEL_ID = "pk_wake_listener"
        private const val NOTIFICATION_ID = 4201
        private const val SAMPLE_RATE = 16_000
    }
}
