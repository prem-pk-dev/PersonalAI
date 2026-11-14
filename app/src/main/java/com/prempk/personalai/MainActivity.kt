package com.prempk.personalai

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.BatteryManager
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
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "Speech recognition unavailable on this device"
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
            }

        window.statusBarColor = Color.rgb(5, 10, 22)
        window.navigationBarColor = Color.rgb(5, 10, 22)
        window.decorView.systemUiVisibility = 0
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(22))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xFF050A16.toInt(), 0xFF0B1730.toInt(), 0xFF07101F.toInt())
            )
        }
        val scroll = ScrollView(this).apply { isFillViewport = true; clipToPadding = false }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brand.addView(label("PK", 22f, white, true))
        brand.addView(label("YOUR PERSONAL VOICE ASSISTANT", 9f, muted, true))
        val languageButton = makeButton("AUTO LANGUAGE") { }
        languageButton.textSize = 11f
        languageButton.isAllCaps = false
        languageButton.isClickable = false
        languageButton.background = surface(0x332F9DCE, 22, 0x6658D8F2)
        languageButton.setPadding(dp(14), dp(9), dp(14), dp(9))
        header.addView(brand, LinearLayout.LayoutParams(0, dp(52), 1f))
        header.addView(languageButton)

        val greeting = label("Hello. How can I help?", 25f, white, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, dp(4))
        }
        val subtitle = label("Say “PK” to wake me  •  Hindi + English", 13f, muted).apply {
            gravity = Gravity.CENTER
        }
        orb = SiriOrbView(this).apply { contentDescription = "PK voice assistant" }
        val orbFrame = FrameLayout(this).apply {
            addView(orb, FrameLayout.LayoutParams(dp(224), dp(224), Gravity.CENTER))
        }
        status = label("●  READY", 12f, 0xFF72E8F4.toInt(), true).apply {
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(10), dp(18), dp(10))
            background = surface(0x3324C6D8, 30, 0x5543D9E8)
        }
        val statusWrap = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            addView(status)
        }
        transcript = label("I’m here. Ask me to open an app, call a contact, or prepare a message.", 15f, white).apply {
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = surface(0xCC111F35.toInt(), 22, 0x553D628A)
        }
        commandInput = EditText(this).apply {
            hint = "Message PK…"
            setHintTextColor(0xFF8295B0.toInt())
            setTextColor(white)
            textSize = 15f
            isSingleLine = true
            setPadding(dp(16), dp(13), dp(16), dp(13))
            background = surface(0xFF15243A.toInt(), 26, 0x6654AFC7)
        }
        val sendButton = makeButton("➤") {
            val command = commandInput.text.toString().trim()
            if (command.isNotEmpty()) { handleCommand(command); commandInput.text.clear() }
        }.apply {
            background = surface(0xFF168FA7.toInt(), 24)
            setPadding(dp(18), dp(12), dp(18), dp(12))
        }
        val inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(commandInput, LinearLayout.LayoutParams(0, dp(54), 1f).apply { marginEnd = dp(8) })
            addView(sendButton, LinearLayout.LayoutParams(dp(56), dp(54)))
        }
        val listenRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val listenButton = makeButton("🎙  Start listening") { startWakeListening() }.apply {
            background = surface(0xFF187FAE.toInt(), 26)
            setPadding(dp(18), dp(13), dp(18), dp(13))
            isAllCaps = false
        }
        val stopButton = makeButton("Stop") { stopListening() }.apply {
            background = surface(0xFF1A2940.toInt(), 26, 0x665B7897)
            setPadding(dp(18), dp(13), dp(18), dp(13))
            isAllCaps = false
        }
        listenRow.addView(listenButton, LinearLayout.LayoutParams(0, dp(50), 1f).apply { marginEnd = dp(8) })
        listenRow.addView(stopButton, LinearLayout.LayoutParams(dp(90), dp(50)))

        val footer = label("Private by design  •  Actions stay under your control", 10f, 0xFF7387A4.toInt()).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, 0)
        }

        listOf<View>(header, greeting, subtitle, orbFrame, statusWrap, transcript,
            inputRow, listenRow, footer).forEachIndexed { index, view ->
            val top = when (index) {
                1 -> dp(10); 3 -> dp(4); 4 -> dp(0); 5 -> dp(16)
                6 -> dp(10); 7 -> dp(10); else -> dp(0)
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

        when (CommandParser.parse(command)) {
            AssistantCommandType.OPEN_WHATSAPP ->
                openApp(listOf("com.whatsapp", "com.whatsapp.w4b"), "WhatsApp")
            AssistantCommandType.OPEN_YOUTUBE ->
                openApp(listOf("com.google.android.youtube"), "YouTube")
            AssistantCommandType.OPEN_CHROME ->
                openApp(listOf("com.android.chrome"), "Chrome")
            AssistantCommandType.OPEN_SETTINGS ->
                openIntent(Intent(Settings.ACTION_SETTINGS), localized("Opening Settings.", "सेटिंग्स खोल रहा हूँ।"))
            AssistantCommandType.CALL_CONTACT -> callContact(command)
            AssistantCommandType.COMPOSE_SMS -> composeSms(command)
            AssistantCommandType.OPEN_ALARMS -> openAlarms()
            AssistantCommandType.OPEN_MAPS -> openIntent(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")), localized("Opening Maps.", "Maps खोल रहा हूँ।"))
            AssistantCommandType.INFO_TIME -> {
                val time = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(java.util.Date())
                respond(localized("The time is $time.", "अभी $time बजे हैं।"))
            }
            AssistantCommandType.INFO_DATE -> {
                val date = java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL).format(java.util.Date())
                respond(localized("Today is $date.", "आज $date है।"))
            }
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
        val spokenLanguage = VoiceLanguage.detect(message)
        val languageResult = tts?.setLanguage(Locale.forLanguageTag(spokenLanguage))
        if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
            languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.setLanguage(Locale.ENGLISH)
        }
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "assistant_reply")
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) tts?.language = Locale.ENGLISH
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
        if (requestCode == REQUEST_CONTACTS) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                respond("Contacts permission granted. Repeat your command.")
            } else {
                respond("Contacts permission was not granted. You can still use a phone number for calls or SMS.")
            }
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
