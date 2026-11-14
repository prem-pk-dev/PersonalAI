package com.prempk.personalai

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
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
import android.view.View
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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
    private var awaitingCommand = false
    private var speechLanguageTag = "en-IN"
    private lateinit var status: TextView
    private lateinit var transcript: TextView
    private lateinit var commandInput: EditText
    private lateinit var history: TextView
    private lateinit var orb: SiriOrbView

    private val backgroundColorValue = 0xFF08111F.toInt()
    private val panel = 0xFF14243A.toInt()
    private val cyan = 0xFF45D6E8.toInt()
    private val white = 0xFFF3F7FB.toInt()
    private val muted = 0xFFA9B8C9.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speechLanguageTag = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getString(KEY_SPEECH_LANGUAGE, "en-IN") ?: "en-IN"
        tts = TextToSpeech(this, this)
        buildUi()
        ensureAudioPermission()
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "Speech recognition unavailable on this device"
        }
    }

    private fun buildUi() {
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()
        fun rounded(color: Int, radius: Int, strokeColor: Int? = null) =
            GradientDrawable().apply {
                setColor(color)
                cornerRadius = dp(radius).toFloat()
                if (strokeColor != null) setStroke(dp(1), strokeColor)
            }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(18))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.rgb(8, 10, 22), Color.rgb(13, 18, 38), Color.rgb(5, 8, 18))
            )
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
        }

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = TextView(this).apply {
            text = "PersonalAI"
            textSize = 21f
            letterSpacing = 0.02f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(white)
        }
        val languageButton = makeButton(if (speechLanguageTag == "hi-IN") "हिंदी  ▾" else "English  ▾") { }
        languageButton.textSize = 12f
        languageButton.background = rounded(0x332D9CDB, 22, 0x6659C9F2)
        languageButton.setPadding(dp(14), dp(7), dp(14), dp(7))
        languageButton.setOnClickListener {
            speechLanguageTag = if (speechLanguageTag == "hi-IN") "en-IN" else "hi-IN"
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putString(KEY_SPEECH_LANGUAGE, speechLanguageTag).apply()
            languageButton.text = if (speechLanguageTag == "hi-IN") "हिंदी  ▾" else "English  ▾"
            runCatching { tts?.language = Locale.forLanguageTag(speechLanguageTag) }
            respond(if (speechLanguageTag == "hi-IN") "भाषा हिंदी चुनी गई है।" else "English selected.")
        }
        topBar.addView(brand, LinearLayout.LayoutParams(0, dp(48), 1f))
        topBar.addView(languageButton)

        val headline = TextView(this).apply {
            text = "How can I help?"
            textSize = 27f
            setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL))
            setTextColor(white)
            gravity = Gravity.CENTER
            setPadding(0, dp(25), 0, dp(4))
        }
        val subhead = TextView(this).apply {
            text = "Your personal assistant"
            textSize = 14f
            setTextColor(0xFF9AA8C5.toInt())
            gravity = Gravity.CENTER
        }

        orb = SiriOrbView(this).apply {
            contentDescription = "Voice assistant animated orb"
        }
        val orbFrame = FrameLayout(this).apply {
            addView(orb, FrameLayout.LayoutParams(dp(232), dp(232), Gravity.CENTER))
        }

        status = TextView(this).apply {
            text = "●  Ready when you are"
            textSize = 13f
            setTextColor(0xFFB9C8E8.toInt())
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(10), dp(16), dp(10))
            background = rounded(0x33202B49, 24, 0x334C6B9A)
        }
        val statusWrap = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            addView(status)
        }

        transcript = TextView(this).apply {
            text = "Tap the orb to start listening, or type a command below."
            textSize = 15f
            setTextColor(0xFFE6ECFA.toInt())
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(17), dp(18), dp(17))
            background = rounded(0x44212B48, 20, 0x334B6590)
        }

        commandInput = EditText(this).apply {
            hint = "Ask me anything..."
            setHintTextColor(0xFF8794B0.toInt())
            setTextColor(white)
            textSize = 15f
            singleLine = true
            setPadding(dp(16), dp(13), dp(16), dp(13))
            background = rounded(0xFF18223A.toInt(), 18, 0x66516A91)
        }

        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val listenButton = makeButton("●  Start listening") { startWakeListening() }.apply {
            textSize = 14f
            background = rounded(0xFF277BE8.toInt(), 28)
            setPadding(dp(18), dp(13), dp(18), dp(13))
        }
        val stopButton = makeButton("Stop") { stopListening() }.apply {
            textSize = 13f
            background = rounded(0xFF263149.toInt(), 28, 0x665B6D8D)
            setPadding(dp(16), dp(13), dp(16), dp(13))
        }
        actionRow.addView(listenButton, LinearLayout.LayoutParams(0, dp(50), 1f).apply {
            marginEnd = dp(8)
        })
        actionRow.addView(stopButton, LinearLayout.LayoutParams(dp(92), dp(50)))

        val sendButton = makeButton("Send  ➜") {
            val command = commandInput.text.toString().trim()
            if (command.isNotEmpty()) {
                handleCommand(command)
                commandInput.text.clear()
            }
        }.apply {
            background = rounded(0xFF277BE8.toInt(), 18)
            setPadding(dp(18), dp(12), dp(18), dp(12))
        }
        val inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(commandInput, LinearLayout.LayoutParams(0, dp(52), 1f).apply {
                marginEnd = dp(8)
            })
            addView(sendButton, LinearLayout.LayoutParams(dp(88), dp(52)))
        }

        history = TextView(this).apply {
            text = "RECENT ACTIVITY"
            textSize = 12f
            letterSpacing = 0.08f
            setTextColor(0xFF8FA3C8.toInt())
            setPadding(0, dp(20), 0, dp(8))
        }
        val examples = TextView(this).apply {
            text = "Try: “Rahul ko SMS bhejo”  ·  “YouTube kholo”  ·  “Papa ko call karo”"
            textSize = 12f
            setTextColor(0xFF8794B0.toInt())
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(12), dp(4), dp(10))
        }

        listOf<View>(topBar, headline, subhead, orbFrame, statusWrap, transcript,
            actionRow, inputRow, examples, history).forEachIndexed { index, view ->
            val top = when (index) {
                1 -> dp(14)
                3 -> dp(5)
                4 -> dp(0)
                5 -> dp(20)
                6 -> dp(14)
                7 -> dp(10)
                else -> dp(0)
            }
            root.addView(view, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
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

    private fun languageButtonLabel() =
        if (speechLanguageTag == "hi-IN") "VOICE LANGUAGE: HINDI (INDIA)" else "VOICE LANGUAGE: ENGLISH (INDIA)"

    private fun languageButtonTextRefresh(button: Button) {
        button.text = languageButtonLabel()
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
        awaitingCommand = false
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
                    status.text = "●  Listening… speak now"\n                    orb.setListening(true)
                }
                override fun onBeginningOfSpeech() {
                    status.text = "●  I’m listening"\n                    orb.setListening(true)
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
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLanguageTag)
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
        if (wake != null) {
            val command = normalized.replace(wake, "").trim(' ', ',', '.', '!')
            if (command.isBlank()) {
                awaitingCommand = true
                respond("Yes, tell me.")
            } else {
                awaitingCommand = false
                handleCommand(command)
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
        status.text = "●  Ready when you are"\n        orb.setListening(false)
    }

    private fun handleCommand(raw: String) {
        val command = raw.trim()
        addLog("You: $command")

        when (CommandParser.parse(command)) {
            AssistantCommandType.OPEN_WHATSAPP ->
                openApp(listOf("com.whatsapp", "com.whatsapp.w4b"), "WhatsApp")
            AssistantCommandType.OPEN_YOUTUBE ->
                openApp(listOf("com.google.android.youtube"), "YouTube")
            AssistantCommandType.OPEN_CHROME ->
                openApp(listOf("com.android.chrome"), "Chrome")
            AssistantCommandType.OPEN_SETTINGS ->
                openIntent(Intent(Settings.ACTION_SETTINGS), "Opening Settings.")
            AssistantCommandType.CALL_CONTACT -> callContact(command)
            AssistantCommandType.COMPOSE_SMS -> composeSms(command)
            AssistantCommandType.OPEN_ALARMS -> openAlarms()
            AssistantCommandType.UNKNOWN ->
                respond("I heard: $command. This command is not supported yet.")
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

    /**
     * OEM clock apps do not all implement ACTION_SHOW_ALARMS.
     * Try the standard list intent first, then the standard set-alarm
     * intent, and finally known Clock app launch activities.
     */
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
                respond("Opening your Clock app. Please select Alarms if needed.")
                return
            } catch (_: Exception) {
                // Try the next supported Clock entry point.
            }
        }
        respond("I could not find a Clock app. Please check that a Clock app is installed.")
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
            "Opening dialer for $phone. Confirm the call on your phone.")
    }

    private fun findContactMatches(name: String, onSelected: (ContactEntry) -> Unit = { openDialer(it.number) }) {
        if (name.isBlank()) {
            respond("Please say the contact name or phone number.")
            return
        }
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CONTACTS)
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
            respond("I could not read your contacts. Check Contacts permission and try again.")
            return
        }

        val bestMatches = ContactMatcher.bestMatches(name, matches)
        when (bestMatches.size) {
            0 -> respond("No matching contact found. Check the name or speak a phone number.")
            1 -> onSelected(bestMatches.first())
            else -> {
                val labels = bestMatches.map { "${it.name} — ${it.number}" }.toTypedArray()
                AlertDialog.Builder(this)
                    .setTitle("Choose a contact")
                    .setItems(labels) { _, which -> onSelected(bestMatches[which]) }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                    .show()
                respond("I found more than one matching contact. Choose the correct person on screen.")
            }
        }
    }

    private fun composeSms(command: String) {
        val request = SmsCommandParser.parse(command)
        if (request == null) {
            respond("Say a contact name followed by ko SMS bhejo, or say SMS followed by a phone number.")
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

    private fun openSmsComposer(number: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number")).apply {
            if (body.isNotBlank()) putExtra("sms_body", body)
        }
        openIntent(intent, if (body.isBlank()) {
            "Messages opened. Review and send your message there."
        } else {
            "Message prepared. Review and send it in your Messages app."
        })
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
            val selectedLocale = Locale.forLanguageTag(speechLanguageTag)
            val result = tts?.setLanguage(selectedLocale)
            if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                tts?.language = Locale.ENGLISH
            }
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
            respond("Contacts permission granted. Repeat your command.")
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
        private const val PREFS_NAME = "personalai_preferences"
        private const val KEY_SPEECH_LANGUAGE = "speech_language"
    }
}
