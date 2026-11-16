# PersonalAI — Native Android Voice Assistant

PK is a native Kotlin Android assistant for Android 12–16 (device validation is still pending). The ₹0 MVP uses Android speech APIs and system intents; it requires no backend or paid AI API.

## Current features

- Siri-inspired dark interface with animated orb, tap-to-listen, transcript, text command entry, and activity log.
- The only configured wake phrase is "PK" (Hindi speech transcripts "पीके" / "पी के" are accepted); "Hey Assistant" is not used.
- Foreground speech recognition can listen while the app is open. A separate, explicitly user-started Sherpa-ONNX foreground service can monitor the wake word while other apps are in use; after detection, the user taps its notification to open PK and continue command recognition. This is not a seamless hands-free wake-to-command flow.
- Optional offline Hindi command transcription: user explicitly downloads a pinned ~198 MB IndicConformer int8 CTC model (SHA-256 checked) and taps Speak Hindi offline. The app captures one short mono 16 kHz utterance and transcribes locally with Sherpa-ONNX; after model installation, transcription itself does not require internet. This is a one-shot in-app feature, not always-on Hindi wake-word detection; Redmi Note 9 accuracy, latency, memory and battery remain untested.
- Android SpeechRecognizer and TextToSpeech, with automatic response-language selection: Hindi-script and common Romanized-Hindi input select Hindi (India) TTS; English/other Latin input defaults to English (India). Android 14+ language-detection callbacks are enabled when supported; Android 12 uses the script/phrase heuristic fallback. Actual recognition depends on the installed provider and language models.
- ACTION_ASSIST activity entry point and a user-consented Android default-assistant role request. The role request and API guards pass CI; a real VoiceInteractionService/session implementation is still pending, so this is not yet a complete always-on system assistant.
- Deterministic command parser and JUnit coverage.
- Media playback commands (play/pause/next/previous) route through the active Android media session using `MediaController`; users must grant PK Notification Access first. The app reports when no active session exists or an action is unsupported. This integration is CI-verified, but Redmi Note 9 / app-specific behavior remains untested.
- Contact lookup with contextual permission rationale, exact-first matching, duplicate-number normalization, and chooser for ambiguous matches.
- Microphone and Contacts permission denials have clear feedback.
- Launch WhatsApp / WhatsApp Business, YouTube, Chrome, and Android Settings.
- Contact dialing through the system dialer; user confirms the call.
- SMS composer for phone-number or contact-name recipients, optional body prefill, and manual review/send.
- Local offline utilities: current time, date, battery percentage, Maps launch, and command help.
- PK-branded dark interface with animated voice orb, compact composer and conversation panel.
- Explicitly enabled AccessibilityService supports limited cross-app scroll, Back and Home actions; the app reports whether the service is enabled. It does not read screen text or send messages.
- Calendar reminder/event composer opens for the user to review and set its date/time; PK does not silently schedule reminders.

## Wake-word and platform limitations

- Background KWS requires the user to install the model, grant microphone/notification permissions and start the visible foreground listener. On wake detection the user must tap the notification before command recognition begins.
- PK does not yet register a VoiceInteractionService/session service; requesting the Android assistant role and declaring ACTION_ASSIST do not by themselves provide always-on system hotword behavior.
- Screen-off hotword detection depends on a compatible hotword engine, keyphrase enrollment, device SoundTrigger/DSP support, Android background-microphone rules, and OEM behavior. It is not safe to claim universal support or emulate it with continuous SpeechRecognizer loops.
- General SpeechRecognizer offline recognition still depends on the installed speech-recognition provider and language packs. The separate Hindi offline mode uses the downloaded local model described above.
- Cellular call disconnect requires a compliant default-dialer implementation and user role approval. WhatsApp does not expose general third-party APIs for arbitrary call control or silent message sending.
- No lock-screen bypass, stealth recording, silent SMS/calls, or emergency-call automation.

## Build and validation

1. Open the repository in Android Studio.
2. Use JDK 17 and Android SDK Platform 36.
3. Sync Gradle and run the `app` configuration on an Android 13–16 device/emulator.
4. Grant microphone permission; Contacts permission is requested only when a contact action needs it.

GitHub Actions runs `lintDebug testDebugUnitTest assembleDebug` and uploads `personalai-debug-apk` only when all checks pass (14-day artifact retention).

## Branch strategy

- `main`: stable reviewed code; do not modify directly.
- `development/*`: active implementation branches.
- Merge only through reviewed pull requests after CI and device-test review.

## Project continuity

Read `docs/PROJECT_MEMORY.md`, `docs/ROADMAP.md`, and `docs/COMMANDS.md` before architectural changes.
