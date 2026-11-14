package com.prempk.personalai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var wakeMode = false
    private lateinit var status: TextView
    private lateinit var transcript: TextView
    private lateinit var commandInput: EditText
    private lateinit var history: TextView

    private val background = 0xFF08111F.toInt()
    private val panel = 0xFF14243A.toInt()
    private val cyan = 0xFF45D6E8.toInt()
    private val white = 0xFFF3F7FB.toInt()
    private val muted = 0xFFA9B8C9.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        buildUi()
        ensureAudioPermission()
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "Speech recognition unavailable on this device"
        }
    }

    private fun buildUi() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 30, 22, 22)
            setBackgroundColor(background)
        }
        val scroll = ScrollView(this)
        val title = TextView(this).apply {
            text = "PERSONAL AI ASSISTANT"
            textSize = 22f
            setTextColor(cyan)
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "Voice + Text • Hindi + English"
            textSize = 13f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 22)
        }
        status = TextView(this).apply {
            text = "● Ready"
            textSize = 15f
            setTextColor(cyan)
            gravity = Gravity.CENTER
            setPadding(12, 14, 12, 14)
            setBackgroundColor(panel)
        }
        transcript = TextView(this).apply {
            text = "Tap Start Listening, then say: Hey Assistant, WhatsApp kholo"
            textSize = 16f
            setTextColor(white)
            gravity = Gravity.CENTER
            setPadding(12, 24, 12, 24)
        }
        commandInput = EditText(this).apply {
            hint = "Type a command, e.g. YouTube kholo"
            setHintTextColor(muted)
            setTextColor(white)
            textSize = 15f
            setSingleLine(true)
            setPadding(14, 12, 14, 12)
            setBackgroundColor(panel)
        }
        history = TextView(this).apply {
            text = "COMMAND HISTORY"
            textSize = 13f
            setTextColor(cyan)
            setPadding(0, 18, 0, 8)
        }

        val controls = listOf(
            makeButton("START LISTENING  🎙") { startWakeListening() },
            makeButton("STOP LISTENING") { stopListening() },
            makeButton("RUN TEXT COMMAND  ➜") {
                val command = commandInput.text.toString().trim()
                if (command.isNotEmpty()) {
                    handleCommand(command)
                    commandInput.text.clear()
                }
            }
        )
        val examples = TextView(this).apply {
            text = "TRY THESE\n• Hey Assistant, WhatsApp kholo\n• YouTube kholo\n• Chrome kholo\n• Rahul ko call karo\n• SMS 9876543210\n• Settings kholo\n• Alarm kholo"
            textSize = 13f
            setTextColor(muted)
            setPadding(4, 18, 4, 18)
        }

        listOf(title, subtitle, status, transcript, controls[0], controls[1],
            commandInput, controls[2], examples, history).forEach { view ->
            content.addView(view, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 10 })
        }
        scroll.addView(content)
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

    private fun ensureAudioPermission() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_AUDIO)
        }
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
        transcript.text = "Listening for the wake phrase: Hey Assistant"
        startRecognition()
    }

    private fun startRecognition() {
        if (!wakeMode || isFinishing) return
        try {
            recognizer?.destroy()
            recognizer = if (android.os.Build.VERSION.SDK_INT >= 31 &&
                SpeechRecognizer.isOnDeviceRecognitionAvailable(this)) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(this)
            } else {
                SpeechRecognizer.createSpeechRecognizer(this)
            }
            recognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    status.text = "● Listening — say Hey Assistant"
                }
                override fun onBeginningOfSpeech() {
                    status.text = "● Hearing speech…"
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
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            }
            recognizer?.startListening(intent)
        } catch (exception: Exception) {
            wakeMode = false
            status.text = "Speech error: ${exception.localizedMessage ?: "unknown"}"
        }
    }

    private fun processVoice(raw: String) {
        transcript.text = "You said: $raw"
        val normalized = raw.lowercase(Locale.ROOT)
        val wakeWords = listOf("hey assistant", "hello assistant", "हे असिस्टेंट", "अरे असिस्टेंट")
        val wake = wakeWords.firstOrNull { normalized.contains(it) }
        if (wake == null) return
        val command = normalized.replace(wake, "").trim(' ', ',', '.', '!')
        if (command.isBlank()) {
            respond("Yes, tell me.")
        } else {
            handleCommand(command)
        }
    }

    private fun stopListening() {
        wakeMode = false
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
        status.text = "● Listening stopped"
    }

    private fun handleCommand(raw: String) {
        val command = raw.trim()
        val c = command.lowercase(Locale.ROOT)
        addLog("You: $command")

        when {
            containsAny(c, "whatsapp", "व्हाट्सएप") ->
                openApp(listOf("com.whatsapp", "com.whatsapp.w4b"), "WhatsApp")
            containsAny(c, "youtube", "यूट्यूब") ->
                openApp(listOf("com.google.android.youtube"), "YouTube")
            containsAny(c, "chrome", "क्रोम", "browser", "ब्राउज़र") ->
                openApp(listOf("com.android.chrome"), "Chrome")
            containsAny(c, "settings", "सेटिंग") ->
                openIntent(Intent(Settings.ACTION_SETTINGS), "Opening Settings.")
            containsAny(c, "call", "कॉल", "फोन लगाओ", "फोन करो") -> callContact(command)
            containsAny(c, "sms", "message", "मैसेज", "संदेश") -> composeSms(command)
            containsAny(c, "alarm", "अलार्म") ->
                openIntent(Intent(AlarmClock.ACTION_SHOW_ALARMS), "Opening alarms.")
            else -> respond("I heard: $command. This command is not supported yet.")
        }
    }

    private fun openApp(packageNames: List<String>, label: String) {
        val launch = packageNames.firstNotNullOfOrNull {
            packageManager.getLaunchIntentForPackage(it)
        }
        if (launch == null) {
            respond("$label is not installed.")
        } else {
            runCatching { startActivity(launch); respond("Opening $label.") }
                .onFailure { respond("Could not open $label.") }
        }
    }

    private fun openIntent(intent: Intent, successMessage: String) {
        runCatching { startActivity(intent); respond(successMessage) }
            .onFailure { respond("That action is unavailable on this phone.") }
    }

    private fun callContact(command: String) {
        val query = command
            .replace(Regex("(?i)hey assistant|call|कॉल|करो|करना|लगाओ|फोन|please|\\bko\\b|को"), " ")
            .trim()
        val directNumber = Regex("\\+?[0-9][0-9\\s-]{7,}").find(query)
            ?.value?.replace(Regex("[\\s-]"), "")
        val phone = directNumber ?: findContactNumber(query)
        if (phone == null) {
            respond("Contact not found. Allow Contacts permission or speak a phone number.")
            return
        }
        openIntent(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")),
            "Opening dialer for $phone. Confirm the call on your phone.")
    }

    private fun findContactNumber(name: String): String? {
        if (name.isBlank()) return null
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CONTACTS)
            return null
        }
        return runCatching {
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                arrayOf("%$name%"),
                null
            )?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
        }.getOrNull()
    }

    private fun composeSms(command: String) {
        val number = Regex("\\+?[0-9][0-9\\s-]{7,}").find(command)
            ?.value?.replace(Regex("[\\s-]"), "")
        if (number == null) {
            respond("For now, say SMS followed by a phone number.")
            return
        }
        openIntent(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number")),
            "Messages opened. Review and send your message there.")
    }

    private fun containsAny(text: String, vararg terms: String) = terms.any(text::contains)

    private fun respond(message: String) {
        transcript.text = message
        addLog("Assistant: $message")
        speak(message)
    }

    private fun speak(message: String) {
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "assistant_reply")
    }

    private fun addLog(message: String) {
        if (::history.isInitialized) history.append("\n$message")
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_AUDIO && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            status.text = "Microphone permission granted"
        }
        if (requestCode == REQUEST_CONTACTS && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            respond("Contacts permission granted. Repeat the call command.")
        }
    }

    override fun onDestroy() {
        wakeMode = false
        recognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_AUDIO = 101
        private const val REQUEST_CONTACTS = 102
    }
}
