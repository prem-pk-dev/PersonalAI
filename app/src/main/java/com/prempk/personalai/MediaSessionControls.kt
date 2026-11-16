package com.prempk.personalai

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.view.KeyEvent

/**
 * Discovers active sessions through user-granted notification-listener access.
 * Only controls explicitly advertised by a session's PlaybackState are dispatched.
 */
object MediaSessionControls {
    sealed class Result {
        data class Sent(val packageName: String) : Result()
        object AccessRequired : Result()
        object NoActiveSession : Result()
        object Unsupported : Result()
    }

    fun dispatch(context: Context, keyCode: Int): Result {
        val manager = context.getSystemService(MediaSessionManager::class.java)
            ?: return Result.NoActiveSession
        val sessions = try {
            manager.getActiveSessions(
                ComponentName(context, PkNotificationListenerService::class.java)
            )
        } catch (_: SecurityException) {
            return Result.AccessRequired
        }
        val controller: MediaController = sessions
            .filter { it.playbackState?.state != android.media.session.PlaybackState.STATE_NONE }
            .sortedBy {
                when (it.playbackState?.state) {
                    android.media.session.PlaybackState.STATE_PLAYING,
                    android.media.session.PlaybackState.STATE_BUFFERING -> 0
                    else -> 1
                }
            }
            .firstOrNull() ?: return Result.NoActiveSession

        val actions = controller.playbackState?.actions ?: 0L
        val requiredAction = when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY -> android.media.session.PlaybackState.ACTION_PLAY
            KeyEvent.KEYCODE_MEDIA_PAUSE -> android.media.session.PlaybackState.ACTION_PAUSE
            KeyEvent.KEYCODE_MEDIA_NEXT -> android.media.session.PlaybackState.ACTION_SKIP_TO_NEXT
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> android.media.session.PlaybackState.ACTION_SKIP_TO_PREVIOUS
            else -> return Result.Unsupported
        }
        if (actions and requiredAction == 0L) return Result.Unsupported

        return try {
            when (keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY -> controller.transportControls.play()
                KeyEvent.KEYCODE_MEDIA_PAUSE -> controller.transportControls.pause()
                KeyEvent.KEYCODE_MEDIA_NEXT -> controller.transportControls.skipToNext()
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> controller.transportControls.skipToPrevious()
            }
            Result.Sent(controller.packageName)
        } catch (_: SecurityException) {
            Result.AccessRequired
        } catch (_: IllegalStateException) {
            Result.NoActiveSession
        }
    }
}

/** Android calls this only after the user grants notification-listener access. */
class PkNotificationListenerService : android.service.notification.NotificationListenerService()
