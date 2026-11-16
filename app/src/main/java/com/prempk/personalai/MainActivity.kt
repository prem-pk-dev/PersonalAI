package com.prempk.personalai

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Build
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.Gravity
import android.view.View
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var wakeMode = false
    private var awaitingCommand = false
    private var responseLanguageTag = "en-IN"
    private var recognitionDetectedLanguageTag: String? = null
    private lateinit var status: TextView
    private lateinit var transcript: TextView
    private lateinit var commandInput: EditText
    private lateinit var orb: SiriOrbView
    private lateinit var kwsSetupStatus: TextView
    private var modelInstallInProgress = false
    @Volatile private var hindiModelInstallInProgress = false
    @Volatile private var hindiAsrInProgress = false
    @Volatile private var hindiAsrCancelled = false
    private lateinit var hindiAsrStatus: TextView
    private var restartKwsAfterSpeech = false
    private var assistantRoleStatusView: TextView? = null
    private var accessibilityStatusView: TextView? = null

    private val backgroundColorValue = 0xFF08111F.toInt()
    private val panel = 0xFF14243A.toInt()
    private val cyan = 0xFF45D6E8.toInt()
    private val white = 0xFFF3F7FB.toInt()
    private val muted = 0xFFA9B8C9.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        buildUi()
        ensureAudioPermission()
        if (intent?.action == Intent.ACTION_ASSIST) {
            window.decorView.post { startWakeListening() }
        }
        if (intent?.getBooleanExtra(KwsForegroundService.EXTRA_WAKE_DETECTED, false) == true) {
            window.decorView.post { startCommandListeningAfterWake() }
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "Speech recognition unavailable on this device"
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(KwsForegroundService.EXTRA_WAKE_DETECTED, false)) {
            window.decorView.post { startCommandListeningAfterWake() }
        }
    }

    private fun buildUi() {
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()
        fun surface(color: Int, radius: Int, stroke: Int = 0) = GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != 0) setStroke(dp(1), stroke)
        }
        fun label(value: String, size: Float, color: Int, bold: Boolean = false) =
            TextView(this).apply {
                text = value
                textSize = size
                setTextColor(color)
                if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
                includeFontPadding = true
            }
        fun sectionTitle(value: String) = label(value, 11f, 0xFF7F9BB8.toInt(), true).apply {
            letterSpacing = 0.12f
            setPadding(dp(2), dp(2), 0, dp(9))
        }
        fun panelCard() = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(15), dp(16), dp(15))
            background = surface(0xE6122035.toInt(), 22, 0x333D6385)
        }

        window.statusBarColor = Color.rgb(5, 10, 22)
        window.navigationBarColor = Color.rgb(5, 10, 22)
        window.decorView.systemUiVisibility = 0

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(14), dp(20), dp(24))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(0xFF050A16.toInt(), 0xFF0B1930.toInt(), 0xFF07101F.toInt())
            )
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        // Premium brand header
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brandMark = TextView(this).apply {
            text = "PK"
            textSize = 19f
            setTextColor(0xFF071321.toInt())
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(0xFF61E4F0.toInt(), 0xFF8D83FF.toInt())
            ).apply { cornerRadius = dp(15).toFloat() }
        }
        header.addView(brandMark, LinearLayout.LayoutParams(dp(48), dp(48)).apply { marginEnd = dp(12) })
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brand.addView(label("PK", 23f, white, true))
        brand.addView(label("PERSONAL VOICE ASSISTANT", 9f, muted, true))
        header.addView(brand, LinearLayout.LayoutParams(0, dp(52), 1f))
        val languageButton = makeButton("AUTO · HI / EN") { }.apply {
            textSize = 10f
            isAllCaps = false
            isClickable = false
            background = surface(0x332D9FC1, 18, 0x6658D8F2)
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
        header.addView(languageButton)

        val heroEyebrow = label("YOUR DAY, HANDS-FREE", 10f, 0xFF64D8E8.toInt(), true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.18f
            setPadding(0, dp(24), 0, dp(5))
        }
        val greeting = label("Hello. How can I help?", 25f, white, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(5))
        }
        val subtitle = label("Say “PK” to wake me  ·  Hindi + English", 13f, muted).apply {
            gravity = Gravity.CENTER
        }

        orb = SiriOrbView(this).apply { contentDescription = "PK animated voice assistant orb" }
        val orbFrame = FrameLayout(this).apply {
            addView(orb, FrameLayout.LayoutParams(dp(250), dp(250), Gravity.CENTER))
        }

        status = label("●  READY", 11f, 0xFF7BE8F2.toInt(), true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.08f
            setPadding(dp(19), dp(11), dp(19), dp(11))
            background = surface(0x3324C6D8, 30, 0x5543D9E8)
        }
        val statusWrap = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            addView(status)
        }
        val heroCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(14), dp(12), dp(14), dp(16))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(0xCC10243B.toInt(), 0xAA0A1A30.toInt())
            ).apply {
                cornerRadius = dp(28).toFloat()
                setStroke(dp(1), 0x443B8FB7)
            }
        }
        listOf<View>(heroEyebrow, greeting, subtitle, orbFrame, statusWrap).forEachIndexed { index, view ->
            heroCard.addView(view, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                if (index == 3) dp(250) else LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = when (index) {
                    0 -> dp(2)
                    1 -> dp(5)
                    2 -> dp(5)
                    3 -> dp(0)
                    else -> dp(0)
                }
            })
        }

        // Conversation preview
        val conversationCard = panelCard()
        conversationCard.addView(sectionTitle("ASSISTANT"))
        transcript = label(
            "I’m here. Ask me to open an app, call a contact, or prepare a message.",
            15f, white
        ).apply {
            gravity = Gravity.CENTER_VERTICAL
            setLineSpacing(dp(3).toFloat(), 1f)
        }
        conversationCard.addView(transcript)

        // Message composer
        val inputCard = panelCard().apply { setPadding(dp(12), dp(10), dp(12), dp(10)) }
        commandInput = EditText(this).apply {
            hint = "Message PK…"
            setHintTextColor(0xFF8295B0.toInt())
            setTextColor(white)
            textSize = 15f
            isSingleLine = true
            background = null
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        val sendButton = makeButton("➤") {
            val command = commandInput.text.toString().trim()
            if (command.isNotEmpty()) { handleCommand(command); commandInput.text.clear() }
        }.apply {
            textSize = 20f
            background = surface(0xFF1599B2.toInt(), 18)
            setPadding(dp(14), dp(8), dp(14), dp(8))
            contentDescription = "Send command"
        }
        val inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(commandInput, LinearLayout.LayoutParams(0, dp(50), 1f))
            addView(sendButton, LinearLayout.LayoutParams(dp(52), dp(50)))
        }
        inputCard.addView(inputRow)

        // Primary voice controls
        val voiceHeading = sectionTitle("VOICE CONTROLS")
        val listenRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val listenButton = makeButton("🎙  Start listening") { startWakeListening() }.apply {
            background = surface(0xFF147FAE.toInt(), 18, 0x6657C9E8)
            setPadding(dp(12), dp(13), dp(12), dp(13))
            isAllCaps = false
            textSize = 14f
        }
        val stopButton = makeButton("Stop") { stopListening(); stopKwsService() }.apply {
            background = surface(0xFF1B2A40.toInt(), 18, 0x665B7897)
            setPadding(dp(12), dp(13), dp(12), dp(13))
            isAllCaps = false
            textSize = 14f
        }
        listenRow.addView(listenButton, LinearLayout.LayoutParams(0, dp(52), 1f).apply { marginEnd = dp(9) })
        listenRow.addView(stopButton, LinearLayout.LayoutParams(dp(88), dp(52)))

        // Background wake-word setup
        val backgroundCard = panelCard()
        val backgroundHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val backgroundTitle = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        backgroundTitle.addView(label("BACKGROUND LISTENER", 11f, 0xFF70DCEB.toInt(), true))
        backgroundTitle.addView(label("Wake word detection while you use other apps", 12f, muted))
        backgroundHeader.addView(backgroundTitle, LinearLayout.LayoutParams(0, -2, 1f))
        val livePill = label("ON DEVICE", 9f, 0xFF8FE8D2.toInt(), true).apply {
            setPadding(dp(9), dp(6), dp(9), dp(6))
            background = surface(0x2228C99D, 20, 0x5543CDAA)
        }
        backgroundHeader.addView(livePill)
        backgroundCard.addView(backgroundHeader)

        val kwsSetupRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, 0)
        }
        val installModelButton = makeButton("Install offline model") { installOfflineModel() }.apply {
            isAllCaps = false
            textSize = 11f
            background = surface(0xFF1A2B43.toInt(), 15, 0x665A8AA7)
            setPadding(dp(7), dp(12), dp(7), dp(12))
        }
        val startKwsButton = makeButton("Enable listener") { requestStartKwsSession() }.apply {
            isAllCaps = false
            textSize = 11f
            background = surface(0xFF087F91.toInt(), 15, 0x6654D5E5)
            setPadding(dp(7), dp(12), dp(7), dp(12))
        }
        kwsSetupRow.addView(installModelButton, LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginEnd = dp(8) })
        kwsSetupRow.addView(startKwsButton, LinearLayout.LayoutParams(0, dp(48), 1f))
        backgroundCard.addView(kwsSetupRow)

        kwsSetupStatus = label(
            if (KwsModelInstaller.isInstalled(this)) "✓  Offline model installed" else "Offline wake-word model not installed",
            11f, muted
        ).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(2), dp(11), 0, 0)
        }
        backgroundCard.addView(kwsSetupStatus)

        val hindiAsrRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        }
        val installHindiButton = makeButton("Install Hindi ASR · 198 MB") {
            installHindiOfflineModel()
        }.apply {
            isAllCaps = false
            textSize = 10f
            background = surface(0xFF26334B.toInt(), 15, 0x665A8AA7)
            setPadding(dp(7), dp(11), dp(7), dp(11))
        }
        val listenHindiButton = makeButton("Speak Hindi offline") {
            startHindiOfflineListening()
        }.apply {
            isAllCaps = false
            textSize = 10f
            background = surface(0xFF087F91.toInt(), 15, 0x6654D5E5)
            setPadding(dp(7), dp(11), dp(7), dp(11))
        }
        hindiAsrRow.addView(installHindiButton, LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginEnd = dp(8) })
        hindiAsrRow.addView(listenHindiButton, LinearLayout.LayoutParams(0, dp(48), 1f))
        backgroundCard.addView(hindiAsrRow)
        hindiAsrStatus = label(
            if (HindiOfflineModelInstaller.isInstalled(this)) "✓ Hindi offline model installed · runs on device"
            else "Hindi offline ASR not installed · optional 198 MB download",
            11f, muted
        ).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(2), dp(9), 0, 0)
        }
        backgroundCard.addView(hindiAsrStatus)
        val accessibilityButton = makeButton("Enable cross-app controls") {
            AlertDialog.Builder(this).setTitle("Allow PK accessibility controls?")
                .setMessage("PK uses Accessibility only after you enable it in Android Settings. It can scroll the current app and use Back/Home. It does not read screen text or send messages.")
                .setPositiveButton("Open Settings") { _, _ -> startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
                .setNegativeButton("Cancel", null).show()
        }.apply { isAllCaps = false; textSize = 12f; background = surface(0xFF1A2B43.toInt(), 15, 0x665A8AA7); setPadding(dp(8), dp(11), dp(8), dp(11)) }
        backgroundCard.addView(accessibilityButton, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)).apply { topMargin = dp(12) })
        accessibilityStatusView = label("", 11f, muted).apply {
            setPadding(dp(2), dp(8), 0, 0)
        }
        backgroundCard.addView(accessibilityStatusView)

        val assistantRoleButton = makeButton("Set PK as default assistant") {
            requestAssistantRole()
        }.apply {
            isAllCaps = false
            textSize = 12f
            background = surface(0xFF173A50.toInt(), 15, 0x6657C9E8)
            setPadding(dp(8), dp(11), dp(8), dp(11))
        }
        backgroundCard.addView(assistantRoleButton, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(48)
        ).apply { topMargin = dp(10) })
        assistantRoleStatusView = label("", 11f, muted).apply {
            setPadding(dp(2), dp(8), 0, 0)
        }
        backgroundCard.addView(assistantRoleStatusView)
        refreshAssistantRoleStatus(assistantRoleStatusView)
        refreshAccessibilityStatus()

        val footer = label("PRIVATE BY DESIGN  ·  YOU STAY IN CONTROL", 9f, 0xFF7187A4.toInt(), true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.08f
            setPadding(0, dp(20), 0, 0)
        }

        val views = listOf<View>(
            header, heroCard, conversationCard, inputCard, voiceHeading, listenRow, backgroundCard, footer
        )
        views.forEachIndexed { index, view ->
            val top = when (index) {
                1 -> dp(18)
                2 -> dp(16)
                3 -> dp(12)
                4 -> dp(20)
                5 -> dp(0)
                6 -> dp(16)
                else -> dp(0)
            }
            root.addView(view, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = top })
        }
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun makeButton(label: String, action: () -> Unit) =
        Button(this).apply {
            text = label
            textSize = 14f
            setTextColor(white)
            setBackgroundColor(0xFF087E91.toInt())
            setOnClickListener { action() }
        }

    private fun requestAssistantRole() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            respond("Android assistant role selection requires Android 10 or later.")
            return
        }
        val roleManager = getSystemService(android.app.role.RoleManager::class.java)
        if (roleManager == null || !roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_ASSISTANT)) {
            respond(localized(
                "This Android build does not expose the default assistant role.",
                "इस Android version में default assistant चुनने की सुविधा उपलब्ध नहीं है।"
            ))
            return
        }
        if (roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_ASSISTANT)) {
            respond(localized("PK is already your default assistant.", "PK पहले से आपका default assistant है।"))
            return
        }
        runCatching {
            startActivityForResult(
                roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_ASSISTANT),
                REQUEST_ASSISTANT_ROLE
            )
        }.onFailure {
            respond(localized(
                "Android could not open assistant selection. You can choose PK in Settings > Default apps > Digital assistant app.",
                "Assistant चयन नहीं खुल पाया। Settings > Default apps > Digital assistant app में PK चुनें।"
            ))
        }
    }

    private fun refreshAssistantRoleStatus(target: TextView? = null) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            target?.text = "Default assistant selection requires Android 10+."
            return
        }
        val held = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val manager = getSystemService(android.app.role.RoleManager::class.java)
            manager != null &&
                manager.isRoleAvailable(android.app.role.RoleManager.ROLE_ASSISTANT) &&
                manager.isRoleHeld(android.app.role.RoleManager.ROLE_ASSISTANT)
        } else {
            false
        }
        target?.text = if (held) "✓ PK is selected as the default assistant" else
            "Not the default assistant yet · Android will ask you to confirm"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ASSISTANT_ROLE) {
            val held = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val manager = getSystemService(android.app.role.RoleManager::class.java)
                manager != null &&
                    manager.isRoleAvailable(android.app.role.RoleManager.ROLE_ASSISTANT) &&
                    manager.isRoleHeld(android.app.role.RoleManager.ROLE_ASSISTANT)
            } else {
                false
            }
            respond(if (held) localized(
                "PK is now selected as your default assistant.",
                "अब PK आपका default assistant चुना गया है।"
            ) else localized(
                "PK was not selected as the default assistant.",
                "PK को default assistant के रूप में नहीं चुना गया।"
            ))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshAssistantRoleStatus(assistantRoleStatusView)
        refreshAccessibilityStatus()
    }

    private fun refreshAccessibilityStatus() {
        val expected = android.content.ComponentName(this, PkAccessibilityService::class.java)
            .flattenToString()
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty().split(':').any { it.equals(expected, ignoreCase = true) }
        accessibilityStatusView?.text = if (enabled) {
            "✓ PK cross-app controls are enabled"
        } else {
            "Cross-app controls are off · Enable PK in Accessibility Settings"
        }
    }

    private fun ensureAudioPermission() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionWithRationale(
                Manifest.permission.RECORD_AUDIO,
                REQUEST_AUDIO,
                "Microphone access",
                "PersonalAI uses the microphone only after you tap Start listening, so it can recognize your voice commands."
            )
        }
    }

    private fun requestPermissionWithRationale(
        permission: String,
        requestCode: Int,
        title: String,
        explanation: String
    ) {
        if (shouldShowRequestPermissionRationale(permission)) {
            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(explanation)
                .setPositiveButton("Continue") { _, _ ->
                    requestPermissions(arrayOf(permission), requestCode)
                }
                .setNegativeButton("Not now", null)
                .show()
        } else {
            requestPermissions(arrayOf(permission), requestCode)
        }
    }

    private fun installOfflineModel() {
        if (modelInstallInProgress) return
        modelInstallInProgress = true
        kwsSetupStatus.text = "Preparing offline wake-word model…"
        Thread {
            try {
                KwsModelInstaller.install(this) { message ->
                    runOnUiThread { kwsSetupStatus.text = message }
                }
                runOnUiThread {
                    modelInstallInProgress = false
                    kwsSetupStatus.text = "Offline model installed. Tap Enable background PK."
                }
            } catch (error: Exception) {
                runOnUiThread {
                    modelInstallInProgress = false
                    kwsSetupStatus.text = "Model setup failed: " + (error.localizedMessage ?: "unknown error")
                }
            }
        }.start()
    }

    private fun installHindiOfflineModel() {
        if (hindiModelInstallInProgress) return
        if (HindiOfflineModelInstaller.isInstalled(this)) {
            hindiAsrStatus.text = "Hindi offline model is already installed."
            return
        }
        hindiModelInstallInProgress = true
        hindiAsrStatus.text = "Starting optional Hindi model download (about 198 MB)…"
        Thread {
            try {
                HindiOfflineModelInstaller.install(this) { message, percent ->
                    runOnUiThread {
                        if (!isFinishing) hindiAsrStatus.text = "$message $percent%"
                    }
                }
                runOnUiThread {
                    hindiModelInstallInProgress = false
                    if (!isFinishing) hindiAsrStatus.text =
                        "✓ Hindi offline model installed and SHA-256 verified. Internet is not needed for transcription."
                }
            } catch (error: Exception) {
                runOnUiThread {
                    hindiModelInstallInProgress = false
                    if (!isFinishing) hindiAsrStatus.text =
                        "Hindi model install failed: ${error.localizedMessage ?: "unknown error"}"
                }
            }
        }.start()
    }

    /** Captures mono 16 kHz audio and transcribes locally without sharing the microphone with KWS. */
    private fun startHindiOfflineListening() {
        if (hindiAsrInProgress) return
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ensureAudioPermission()
            hindiAsrStatus.text = "Allow microphone access, then tap Speak Hindi offline again."
            return
        }
        if (!HindiOfflineModelInstaller.isInstalled(this)) {
            hindiAsrStatus.text = "Install the Hindi offline model first (about 198 MB)."
            return
        }
        hindiAsrCancelled = false
        hindiAsrInProgress = true
        stopListening()
        // Stop KWS without clearing the enabled preference; handleCommand resumes it afterwards.
        stopService(Intent(this, KwsForegroundService::class.java))
        status.text = "●  Hindi offline · preparing microphone"
        hindiAsrStatus.text = "Listening… speak one Hindi command (up to 9 seconds)."
        Thread {
            var recorder: AudioRecord? = null
            var engine: HindiOfflineAsrEngine? = null
            var commandDispatched = false
            try {
                Thread.sleep(250)
                val sampleRate = HindiOfflineAsrEngine.SAMPLE_RATE
                val minBuffer = AudioRecord.getMinBufferSize(
                    sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
                )
                check(minBuffer > 0) { "This device cannot create a 16 kHz microphone stream." }
                recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    maxOf(minBuffer, 8192)
                )
                check(recorder.state == AudioRecord.STATE_INITIALIZED) { "Microphone could not be initialized." }
                val chunk = ShortArray(2048)
                val captured = ArrayList<Short>(sampleRate * 6)
                recorder.startRecording()
                val startedAt = System.currentTimeMillis()
                var speechStartedAt = 0L
                var lastVoiceAt = 0L
                while (!hindiAsrCancelled && System.currentTimeMillis() - startedAt < 9_000L) {
                    val count = recorder.read(chunk, 0, chunk.size)
                    if (count == AudioRecord.ERROR_INVALID_OPERATION || count == AudioRecord.ERROR_BAD_VALUE || count < 0) {
                        throw IllegalStateException("Microphone read failed ($count).")
                    }
                    if (count == 0) continue
                    val now = System.currentTimeMillis()
                    if (HindiPcmAudio.rms(chunk, count) >= 0.012f) {
                        if (speechStartedAt == 0L) speechStartedAt = now
                        lastVoiceAt = now
                    }
                    for (i in 0 until count) captured.add(chunk[i])
                    if (speechStartedAt == 0L && now - startedAt >= 4_000L) break
                    if (speechStartedAt > 0L && now - lastVoiceAt >= 1_200L) break
                }
                check(!hindiAsrCancelled) { "Hindi listening was cancelled." }
                check(speechStartedAt > 0L && captured.size >= 3_200) {
                    "No clear speech was detected. Please try again closer to the microphone."
                }
                recorder.stop()
                recorder.release()
                recorder = null
                val pcm = ShortArray(captured.size) { captured[it] }
                engine = HindiOfflineAsrEngine(HindiOfflineModelInstaller.modelDirectory(this))
                val recognized = engine.transcribe(HindiPcmAudio.toFloatSamples(pcm), sampleRate)
                check(recognized.isNotBlank()) { "Hindi speech was not recognized. Please repeat the command." }
                commandDispatched = true
                runOnUiThread {
                    hindiAsrInProgress = false
                    if (!isFinishing && !hindiAsrCancelled) {
                        hindiAsrStatus.text = "Recognized offline: $recognized"
                        recognitionDetectedLanguageTag = "hi-IN"
                        handleCommand(recognized)
                    }
                }
            } catch (error: Exception) {
                runOnUiThread {
                    hindiAsrInProgress = false
                    if (!isFinishing) {
                        hindiAsrStatus.text = error.localizedMessage ?: "Hindi offline recognition failed."
                        status.text = "●  Ready when you are"
                    }
                }
            } finally {
                runCatching { recorder?.stop() }
                runCatching { recorder?.release() }
                runCatching { engine?.close() }
                hindiAsrInProgress = false
                // If capture/transcription failed or was cancelled, the success path
                // never reaches handleCommand(), so explicitly restore an opted-in
                // KWS listener after the microphone has been released.
                if (!commandDispatched) {
                    runOnUiThread {
                        if (!isFinishing && !hindiAsrCancelled &&
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                .getBoolean(KEY_KWS_ENABLED, false)) {
                            window.decorView.postDelayed({
                                if (!isFinishing && !hindiAsrInProgress &&
                                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                        .getBoolean(KEY_KWS_ENABLED, false)) {
                                    startKwsService()
                                }
                            }, 500)
                        }
                    }
                }
            }
        }.start()
    }

    private fun requestStartKwsSession() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ensureAudioPermission()
            kwsSetupStatus.text = "Allow microphone permission, then tap Enable background PK."
            return
        }
        if (!KwsModelInstaller.isInstalled(this)) {
            kwsSetupStatus.text = "Download the offline model first."
            return
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS)
            return
        }
        startKwsService()
    }

    private fun startKwsService() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putBoolean(KEY_KWS_ENABLED, true).apply()
        val serviceIntent = Intent(this, KwsForegroundService::class.java).setAction(KwsForegroundService.ACTION_START)
        try {
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(serviceIntent) else startService(serviceIntent)
            kwsSetupStatus.text = "Background PK listening started. Use notification to stop."
            status.text = "●  Listening for PK in the background"
        } catch (error: Exception) {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putBoolean(KEY_KWS_ENABLED, false).apply()
            kwsSetupStatus.text = "Could not start listener: " + (error.localizedMessage ?: "system restriction")
        }
    }

    private fun stopKwsService() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putBoolean(KEY_KWS_ENABLED, false).apply()
        startService(Intent(this, KwsForegroundService::class.java).setAction(KwsForegroundService.ACTION_STOP))
        kwsSetupStatus.text = "Background PK listening stopped."
    }

    private fun startCommandListeningAfterWake() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ensureAudioPermission()
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            respond("Speech recognition is unavailable.")
            return
        }
        wakeMode = true
        awaitingCommand = true
        orb.setListening(true)
        transcript.text = "PK detected. Tell me your command."
        status.text = "●  PK detected — listening for command"
        startRecognition()
    }

    private fun startWakeListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ensureAudioPermission()
            speak("Please allow microphone permission first.")
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            respond("Speech recognition is unavailable.")
            return
        }
        wakeMode = true
        orb.setListening(true)
        awaitingCommand = false
        transcript.text = "Listening for wake word: PK"
        startRecognition()
    }

    private fun startRecognition() {
        if (!wakeMode || isFinishing) return
        try {
            recognitionDetectedLanguageTag = null
            recognizer?.destroy()
            recognizer = if (android.os.Build.VERSION.SDK_INT >= 31 &&
                SpeechRecognizer.isOnDeviceRecognitionAvailable(this)) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(this)
            } else {
                SpeechRecognizer.createSpeechRecognizer(this)
            }
            recognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    status.text = "●  Listening… speak now"
                    orb.setListening(true)
                }
                override fun onBeginningOfSpeech() {
                    status.text = "●  I’m listening"
                    orb.setListening(true)
                }
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onError(error: Int) {
                    if (wakeMode) transcript.postDelayed({ startRecognition() }, 700)
                }
                override fun onResults(results: Bundle?) {
                    val spoken = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                    if (!spoken.isNullOrBlank()) processVoice(spoken)
                    if (wakeMode) transcript.postDelayed({ startRecognition() }, 500)
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                    if (!partial.isNullOrBlank()) transcript.text = partial
                }
                override fun onLanguageDetection(results: Bundle) {
                    if (android.os.Build.VERSION.SDK_INT >= 34) {
                        val detected = results.getString(SpeechRecognizer.DETECTED_LANGUAGE)
                        recognitionDetectedLanguageTag = VoiceLanguage.fromLanguageTag(detected)
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "und")
                if (android.os.Build.VERSION.SDK_INT >= 34) {
                    putExtra(RecognizerIntent.EXTRA_ENABLE_LANGUAGE_DETECTION, true)
                    putStringArrayListExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_DETECTION_ALLOWED_LANGUAGES,
                        arrayListOf("en-IN", "hi-IN")
                    )
                }
            }
            recognizer?.startListening(intent)
        } catch (exception: Exception) {
            wakeMode = false
            status.text = "Speech error: ${exception.localizedMessage ?: "unknown"}"
        }
    }

    private fun processVoice(raw: String) {
        transcript.text = "You said: $raw"
        responseLanguageTag = recognitionDetectedLanguageTag ?: VoiceLanguage.detect(raw)
        val normalized = raw.lowercase(Locale.ROOT)
        // Accept PK, and the common Hindi speech-to-text forms "पीके" / "पी के".
        val commandAfterWake = WakeWordParser.extractCommand(raw)
        if (commandAfterWake != null) {
            if (commandAfterWake.isBlank()) {
                awaitingCommand = true
                respond("Yes, tell me.")
            } else {
                awaitingCommand = false
                handleCommand(commandAfterWake)
            }
            return
        }
        // Allow the wake phrase and command to arrive as two separate speech results.
        if (awaitingCommand) {
            awaitingCommand = false
            handleCommand(normalized)
        }
    }

    private fun stopListening() {
        wakeMode = false
        awaitingCommand = false
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
        status.text = "●  Ready when you are"
        orb.setListening(false)
    }

    private fun handleCommand(raw: String) {
        // A recognized command is a one-shot voice interaction. Release the microphone
        // before launching another app, opening a system screen, or preparing a call/SMS.
        // The wake-only "PK" path does not call handleCommand, so it can still listen
        // for the follow-up command.
        stopListening()

        val command = raw.trim()
        responseLanguageTag = recognitionDetectedLanguageTag ?: VoiceLanguage.detect(command)
        val commandType = CommandParser.parse(command)
        // Avoid loading the native KWS model while TTS is speaking. This reduces
        // memory and CPU contention on lower-memory phones.
        val speaksAnswer = KwsSpeechLifecycle.shouldResumeAfterSpeech(
            commandType,
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_KWS_ENABLED, false)
        )
        restartKwsAfterSpeech = speaksAnswer &&
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_KWS_ENABLED, false)
        if (!speaksAnswer && getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(KEY_KWS_ENABLED, false)) startKwsService()

        when (commandType) {
            AssistantCommandType.OPEN_WHATSAPP ->
                openApp(listOf("com.whatsapp", "com.whatsapp.w4b"), "WhatsApp")
            AssistantCommandType.COMPOSE_WHATSAPP -> composeWhatsApp(command)
            AssistantCommandType.OPEN_YOUTUBE ->
                openApp(listOf("com.google.android.youtube"), "YouTube")
            AssistantCommandType.OPEN_CHROME ->
                openApp(listOf("com.android.chrome"), "Chrome")
            AssistantCommandType.OPEN_SETTINGS ->
                openIntent(Intent(Settings.ACTION_SETTINGS), localized("Opening Settings.", "सेटिंग्स खोल रहा हूँ।"))
            AssistantCommandType.OPEN_INSTALLED_APP -> openInstalledApp(command)
            AssistantCommandType.MEDIA_PLAY -> dispatchMediaKey(android.view.KeyEvent.KEYCODE_MEDIA_PLAY)
            AssistantCommandType.MEDIA_PAUSE -> dispatchMediaKey(android.view.KeyEvent.KEYCODE_MEDIA_PAUSE)
            AssistantCommandType.MEDIA_NEXT -> dispatchMediaKey(android.view.KeyEvent.KEYCODE_MEDIA_NEXT)
            AssistantCommandType.MEDIA_PREVIOUS -> dispatchMediaKey(android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            AssistantCommandType.VOLUME_UP -> adjustMediaVolume(AudioManager.ADJUST_RAISE)
            AssistantCommandType.VOLUME_DOWN -> adjustMediaVolume(AudioManager.ADJUST_LOWER)
            AssistantCommandType.VOLUME_MUTE -> adjustMediaVolume(AudioManager.ADJUST_MUTE)
            AssistantCommandType.CALL_CONTACT -> callContact(command)
            AssistantCommandType.COMPOSE_SMS -> composeSms(command)
            AssistantCommandType.OPEN_ALARMS -> openAlarms()
            AssistantCommandType.SET_ALARM -> setAlarmFromCommand(command)
            AssistantCommandType.SET_TIMER -> setTimerFromCommand(command)
            AssistantCommandType.CREATE_REMINDER -> createReminder(command)
            AssistantCommandType.OPEN_MAPS -> openIntent(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")), localized("Opening Maps.", "Maps खोल रहा हूँ।"))
            AssistantCommandType.INFO_TIME -> {
                val time = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(java.util.Date())
                respond(localized("The time is $time.", "अभी $time बजे हैं।"))
            }
            AssistantCommandType.INFO_DATE -> {
                val date = java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL).format(java.util.Date())
                respond(localized("Today is $date.", "आज $date है।"))
            }
            AssistantCommandType.GREETING -> respond(TimeAwareGreeting.reply(responseLanguageTag == "hi-IN"))
            AssistantCommandType.SCROLL_UP -> runAccessibilityAction(PkAccessibilityService.Action.SCROLL_BACKWARD)
            AssistantCommandType.SCROLL_DOWN -> runAccessibilityAction(PkAccessibilityService.Action.SCROLL_FORWARD)
            AssistantCommandType.GO_BACK -> runAccessibilityAction(PkAccessibilityService.Action.BACK)
            AssistantCommandType.GO_HOME -> runAccessibilityAction(PkAccessibilityService.Action.HOME)
            AssistantCommandType.INFO_BATTERY -> {
                val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
                val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                respond(if (level in 0..100) localized("Your battery is at $level percent.", "आपकी बैटरी $level प्रतिशत है।")
                    else localized("I cannot read the battery level right now.", "अभी बैटरी का स्तर नहीं पढ़ पा रहा हूँ।"))
            }
            AssistantCommandType.HELP -> respond(localized(
                "I can open apps, prepare calls and SMS, show time, date and battery level, open Maps and Settings.",
                "मैं ऐप खोल सकता हूँ, कॉल और SMS तैयार कर सकता हूँ, समय, तारीख और बैटरी बता सकता हूँ तथा Maps और Settings खोल सकता हूँ।"
            ))
            AssistantCommandType.UNKNOWN ->
                respond(localized("I heard: $command. This command is not supported yet.", "मैंने सुना: $command। यह कमांड अभी उपलब्ध नहीं है।"))
        }
    }

    private fun runAccessibilityAction(action: PkAccessibilityService.Action) {
        if (!PkAccessibilityService.perform(action)) {
            respond(localized("Cross-app controls are not enabled, or this screen cannot scroll.", "दूसरे ऐप के नियंत्रण चालू नहीं हैं या यह स्क्रीन स्क्रॉल नहीं हो सकती।"))
        } else {
            val message = when (action) {
                PkAccessibilityService.Action.SCROLL_BACKWARD -> localized("Scrolling up.", "ऊपर स्क्रॉल कर रहा हूँ।")
                PkAccessibilityService.Action.SCROLL_FORWARD -> localized("Scrolling down.", "नीचे स्क्रॉल कर रहा हूँ।")
                PkAccessibilityService.Action.BACK -> localized("Going back.", "पीछे जा रहा हूँ।")
                PkAccessibilityService.Action.HOME -> localized("Going to Home.", "होम स्क्रीन पर जा रहा हूँ।")
            }
            respond(message)
        }
    }

    private fun adjustMediaVolume(direction: Int) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            direction,
            AudioManager.FLAG_SHOW_UI
        )
        val response = when (direction) {
            AudioManager.ADJUST_RAISE -> localized("Media volume increased.", "मीडिया की आवाज़ बढ़ा दी है।")
            AudioManager.ADJUST_LOWER -> localized("Media volume decreased.", "मीडिया की आवाज़ कम कर दी है।")
            else -> localized("Media audio muted.", "मीडिया की आवाज़ म्यूट कर दी है।")
        }
        respond(response)
    }

    private fun dispatchMediaKey(keyCode: Int) {
        when (val result = MediaSessionControls.dispatch(this, keyCode)) {
            is MediaSessionControls.Result.Sent -> {
                val app = result.packageName
                val action = when (keyCode) {
                    android.view.KeyEvent.KEYCODE_MEDIA_PLAY -> localized("Play", "चलाएँ")
                    android.view.KeyEvent.KEYCODE_MEDIA_PAUSE -> localized("Pause", "रोकें")
                    android.view.KeyEvent.KEYCODE_MEDIA_NEXT -> localized("Next track", "अगला गाना")
                    else -> localized("Previous track", "पिछला गाना")
                }
                respond(localized("$action command sent to $app.", "$action command $app को भेजा गया।"))
            }
            MediaSessionControls.Result.AccessRequired -> {
                respond(localized(
                    "Allow PK access to media notifications to discover and control the active media session. Then repeat your command.",
                    "Active media session पहचानने और नियंत्रित करने के लिए PK को notification access दें। फिर command दोहराएँ।"
                ))
                runCatching {
                    startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            }
            MediaSessionControls.Result.NoActiveSession ->
                respond(localized("No active media session was found. Start playback in a supported media app and try again.",
                    "कोई active media session नहीं मिला। किसी supported media app में playback शुरू करके फिर प्रयास करें।"))
            MediaSessionControls.Result.Unsupported ->
                respond(localized("The active media app does not support this control.",
                    "Active media app इस control को support नहीं करता।"))
        }
    }

    private fun openInstalledApp(command: String) {
        val requestedName = InstalledAppCommandParser.extractAppName(command)
        if (requestedName.isBlank()) {
            respond(localized("Tell me which app to open.", "कौन-सा ऐप खोलना है, उसका नाम बताइए।"))
            return
        }
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val matches = packageManager.queryIntentActivities(launcherIntent, 0)
            .mapNotNull { info ->
                val label = info.loadLabel(packageManager)?.toString()?.trim().orEmpty()
                val launch = packageManager.getLaunchIntentForPackage(info.activityInfo.packageName)
                if (label.isBlank() || launch == null) null else Triple(label, launch, info.activityInfo.packageName)
            }
            .distinctBy { it.third }
        val normalized = requestedName.trim().lowercase(Locale.ROOT)
        val exact = matches.filter { it.first.lowercase(Locale.ROOT) == normalized }
        val candidates = if (exact.isNotEmpty()) exact else matches.filter {
            it.first.lowercase(Locale.ROOT).contains(normalized) ||
                normalized.contains(it.first.lowercase(Locale.ROOT))
        }
        when (candidates.size) {
            0 -> respond(localized("I couldn't find an installed app named $requestedName.", "$requestedName नाम का इंस्टॉल ऐप नहीं मिला।"))
            1 -> openIntent(candidates.first().second, localized("Opening ${candidates.first().first}.", "${candidates.first().first} खोल रहा हूँ।"))
            else -> {
                val labels = candidates.map { it.first }.toTypedArray()
                AlertDialog.Builder(this)
                    .setTitle(localized("Choose an app", "ऐप चुनें"))
                    .setItems(labels) { _, which ->
                        openIntent(candidates[which].second, localized("Opening ${candidates[which].first}.", "${candidates[which].first} खोल रहा हूँ।"))
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun openApp(packageNames: List<String>, label: String) {
        val launch = packageNames.firstNotNullOfOrNull {
            packageManager.getLaunchIntentForPackage(it)
        }
        if (launch == null) {
            respond(localized("$label is not installed.", "$label इंस्टॉल नहीं है।"))
        } else {
            runCatching { startActivity(launch); respond(localized("Opening $label.", "$label खोल रहा हूँ।")) }
                .onFailure { respond(localized("Could not open $label.", "$label नहीं खुल पाया।")) }
        }
    }

    private fun openIntent(intent: Intent, successMessage: String) {
        runCatching { startActivity(intent); respond(successMessage) }
            .onFailure { respond(localized("That action is unavailable on this phone.", "यह सुविधा इस फोन पर उपलब्ध नहीं है।")) }
    }

    /**
     * OEM clock apps do not all implement ACTION_SHOW_ALARMS.
     * Try the standard list intent first, then the standard set-alarm
     * intent, and finally known Clock app launch activities.
     */
    private fun spokenTimeMinutes(command: String): Int? = AlarmTimeParser.parseMinutes(command)

    private fun setAlarmFromCommand(command: String) {
        val minutes = spokenTimeMinutes(command)
        if (minutes == null) {
            respond(localized("Please say a time, for example: set alarm at 7:30 AM.", "कृपया समय बोलें, जैसे: सुबह 7:30 बजे अलार्म लगाओ।"))
            return
        }
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, minutes / 60)
            putExtra(AlarmClock.EXTRA_MINUTES, minutes % 60)
            putExtra(AlarmClock.EXTRA_MESSAGE, "PK alarm")
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
        }
        openIntent(intent, localized("Opening the Clock app to confirm your alarm.", "अलार्म की पुष्टि के लिए Clock ऐप खोल रहा हूँ।"))
    }

    private fun setTimerFromCommand(command: String) {
        val seconds = TimerDurationParser.parseSeconds(command)
        if (seconds == null) {
            respond(localized("Say a timer duration, for example: set timer for 5 minutes.", "टाइमर की अवधि बोलें, जैसे: 5 मिनट का टाइमर लगाओ।"))
            return
        }
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, "PK timer")
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
        }
        openIntent(intent, localized("Opening the Clock app to confirm your timer.", "टाइमर की पुष्टि के लिए Clock ऐप खोल रहा हूँ।"))
    }

    private fun createReminder(command: String) {
        val title = command
            .replace(Regex("(?i)^\\s*(hey pk[, ]*)?(remind me to|remind me|create reminder|set reminder)\\s*"), "")
            .replace(Regex("^(मुझे|मुझको)?\\s*(याद दिलाना|याद दिला दो|रिमाइंडर लगाओ|रिमाइंडर बनाओ)\\s*"), "")
            .trim()
            .ifBlank { "PK reminder" }
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = android.provider.CalendarContract.Events.CONTENT_URI
            putExtra(android.provider.CalendarContract.Events.TITLE, title)
            putExtra(android.provider.CalendarContract.Events.DESCRIPTION, "Created with PK PersonalAI")
        }
        openIntent(intent, localized(
            "Opening Calendar to review and set the reminder date and time.",
            "रिमाइंडर की तारीख और समय तय करने के लिए Calendar खोल रहा हूँ।"
        ))
    }

    private fun openAlarms() {
        val intents = listOf(
            Intent(AlarmClock.ACTION_SHOW_ALARMS),
            Intent(AlarmClock.ACTION_SET_ALARM),
            Intent.makeMainActivity(android.content.ComponentName(
                "com.android.deskclock", "com.android.deskclock.DeskClock"
            )),
            Intent.makeMainActivity(android.content.ComponentName(
                "com.google.android.deskclock", "com.android.deskclock.DeskClock"
            )),
            packageManager.getLaunchIntentForPackage("com.miui.clock")
        ).filterNotNull()

        for (intent in intents) {
            try {
                startActivity(intent)
                respond(localized("Opening your Clock app. Please select Alarms if needed.", "घड़ी ऐप खोल रहा हूँ। जरूरत हो तो अलार्म चुनें।"))
                return
            } catch (_: Exception) {
                // Try the next supported Clock entry point.
            }
        }
        respond(localized("I could not find a Clock app. Please check that a Clock app is installed.", "घड़ी ऐप नहीं मिला। कृपया जाँचें कि ऐप इंस्टॉल है।"))
    }

    private fun callContact(command: String) {
        val query = command
            .replace(Regex("(?i)hey assistant|call|karo|karna|lagao|lagana|phone|please|कॉल|करो|करना|लगाओ|लगाना|फोन|\\bko\\b|को"), " ")
            .trim()
        val directNumber = Regex("\\+?[0-9][0-9\\s-]{7,}").find(query)
            ?.value?.replace(Regex("[\\s-]"), "")
        if (directNumber != null) {
            openDialer(directNumber)
            return
        }
        findContactMatches(query)
    }

    private fun openDialer(phone: String) {
        openIntent(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")),
            localized("Opening dialer for $phone. Confirm the call on your phone.", "$phone के लिए डायलर खोल रहा हूँ। कॉल की पुष्टि फोन पर करें।"))
    }

    private fun findContactMatches(name: String, onSelected: (ContactEntry) -> Unit = { openDialer(it.number) }) {
        if (name.isBlank()) {
            respond(localized("Please say the contact name or phone number.", "कृपया संपर्क का नाम या फोन नंबर बोलें।"))
            return
        }
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionWithRationale(
                Manifest.permission.READ_CONTACTS,
                REQUEST_CONTACTS,
                "Contacts access",
                "PersonalAI reads contact names and phone numbers only when you ask it to call or prepare an SMS to a saved contact."
            )
            return
        }

        val matches = runCatching {
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                arrayOf("%$name%"),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                buildList {
                    while (cursor.moveToNext()) {
                        val contactName = cursor.getString(nameIndex).orEmpty()
                        val number = cursor.getString(numberIndex).orEmpty()
                        if (contactName.isNotBlank() && number.isNotBlank()) {
                            add(ContactEntry(contactName, number))
                        }
                    }
                }
            }.orEmpty()
        }.getOrElse {
            respond(localized("I could not read your contacts. Check Contacts permission and try again.", "संपर्क नहीं पढ़ पाया। Contacts अनुमति जाँचकर फिर कोशिश करें।"))
            return
        }

        val bestMatches = ContactMatcher.bestMatches(name, matches)
        when (bestMatches.size) {
            0 -> respond(localized("No matching contact found. Check the name or speak a phone number.", "कोई मिलता-जुलता संपर्क नहीं मिला। नाम जाँचें या फोन नंबर बोलें।"))
            1 -> onSelected(bestMatches.first())
            else -> {
                val labels = bestMatches.map { "${it.name} — ${it.number}" }.toTypedArray()
                AlertDialog.Builder(this)
                    .setTitle(localized("Choose a contact", "संपर्क चुनें"))
                    .setItems(labels) { _, which -> onSelected(bestMatches[which]) }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                    .show()
                respond(localized("I found more than one matching contact. Choose the correct person on screen.", "एक से अधिक संपर्क मिले हैं। स्क्रीन पर सही व्यक्ति चुनें।"))
            }
        }
    }

    private fun composeSms(command: String) {
        val request = SmsCommandParser.parse(command)
        if (request == null) {
            respond(localized("Say a contact name followed by ko SMS bhejo, or say SMS followed by a phone number.", "संपर्क का नाम बोलकर SMS भेजें कहें, या SMS के साथ फोन नंबर बोलें।"))
            return
        }
        if (!request.phoneNumber.isNullOrBlank()) {
            openSmsComposer(request.phoneNumber, request.body)
            return
        }
        val contactName = request.contactName
        if (contactName.isNullOrBlank()) {
            respond("Please say the contact name or phone number.")
            return
        }
        findContactMatches(contactName) { contact ->
            openSmsComposer(contact.number, request.body)
        }
    }

    private fun composeWhatsApp(command: String) {
        val body = WhatsAppMessageParser.extractBody(command)
        if (body.isBlank()) {
            openApp(listOf("com.whatsapp", "com.whatsapp.w4b"), "WhatsApp")
            return
        }
        val packages = listOf("com.whatsapp", "com.whatsapp.w4b")
        val targetPackage = packages.firstOrNull { packageManager.getLaunchIntentForPackage(it) != null }
        if (targetPackage == null) {
            respond(localized("WhatsApp is not installed.", "WhatsApp इंस्टॉल नहीं है।"))
            return
        }
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, body)
            setPackage(targetPackage)
        }
        openIntent(
            Intent.createChooser(share, localized("Choose a WhatsApp chat", "WhatsApp चैट चुनें")),
            localized("Message prepared. Choose a chat, review it, and send it yourself.", "संदेश तैयार है। चैट चुनें, जाँचें और स्वयं भेजें।")
        )
    }

    private fun openSmsComposer(number: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number")).apply {
            if (body.isNotBlank()) putExtra("sms_body", body)
        }
        openIntent(intent, if (body.isBlank()) {
            localized("Messages opened. Review and send your message there.", "Messages खुल गया है। अपना संदेश जाँचकर वहीं भेजें।")
        } else {
            localized("Message prepared. Review and send it in your Messages app.", "संदेश तैयार है। Messages ऐप में जाँचकर भेजें।")
        })
    }

    private fun containsAny(text: String, vararg terms: String) = terms.any(text::contains)

    private fun localized(english: String, hindi: String): String =
        if (responseLanguageTag == "hi-IN") hindi else english

    private fun respond(message: String) {
        transcript.text = message
        speak(message)
    }

    private fun speak(message: String) {
        val engine = tts ?: return
        val spokenLanguage = VoiceLanguage.detect(message)
        val languageResult = engine.setLanguage(Locale.forLanguageTag(spokenLanguage))
        if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
            languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.setLanguage(Locale.ENGLISH)
        }
        val result = engine.speak(message, TextToSpeech.QUEUE_FLUSH, null, "assistant_reply")
        if (result == TextToSpeech.ERROR) resumeKwsAfterSpeech()
    }

    private fun resumeKwsAfterSpeech() {
        if (!restartKwsAfterSpeech) return
        restartKwsAfterSpeech = false
        if (!isFinishing && getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(KEY_KWS_ENABLED, false)) {
            window.decorView.postDelayed({ if (!isFinishing) startKwsService() }, 350)
        }
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) {
            tts?.language = Locale.ENGLISH
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit
                override fun onDone(utteranceId: String?) {
                    if (utteranceId == "assistant_reply") runOnUiThread { resumeKwsAfterSpeech() }
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (utteranceId == "assistant_reply") runOnUiThread { resumeKwsAfterSpeech() }
                }
                override fun onError(utteranceId: String?, errorCode: Int) {
                    if (utteranceId == "assistant_reply") runOnUiThread { resumeKwsAfterSpeech() }
                }
            })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_AUDIO) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                status.text = "Microphone permission granted"
                respond("Microphone permission granted. Tap Start listening when ready.")
            } else {
                respond("Microphone permission was not granted. Voice commands need microphone access; text commands remain available.")
            }
        }
        if (requestCode == REQUEST_NOTIFICATIONS) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                startKwsService()
            } else {
                kwsSetupStatus.text = "Notification permission is needed for listener controls."
            }
        }
        if (requestCode == REQUEST_CONTACTS) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                respond("Contacts permission granted. Repeat your command.")
            } else {
                respond("Contacts permission was not granted. You can still use a phone number for calls or SMS.")
            }
        }
    }

    override fun onDestroy() {
        hindiAsrCancelled = true
        wakeMode = false
        recognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_AUDIO = 101
        private const val REQUEST_CONTACTS = 102
        private const val REQUEST_NOTIFICATIONS = 103
        private const val REQUEST_ASSISTANT_ROLE = 104
        private const val PREFS_NAME = "pk_voice_preferences"
        private const val KEY_KWS_ENABLED = "background_kws_enabled"
    }
}
