# PersonalAI — Native Android Voice Assistant

PK is a native Kotlin Android assistant for Android 12–16 (device validation is still pending). The ₹0 MVP uses Android speech APIs and system intents; it requires no backend or paid AI API.

## Current features

- Siri-inspired dark interface with animated orb, tap-to-listen, transcript, text command entry, and activity log.
- The only configured wake phrase is "PK" (Hindi speech transcripts "पीके" / "पी के" are accepted); "Hey Assistant" is not used. Listening is currently user-started while the app is open.
- Combined "PK + command" and two-step "PK", then command.
- Android SpeechRecognizer and TextToSpeech, with automatic response-language selection: Hindi-script and common Romanized-Hindi input select Hindi (India) TTS; English/other Latin input defaults to English (India). Android 14+ language-detection callbacks are enabled when supported; Android 12 uses the script/phrase heuristic fallback. Actual recognition depends on the installed provider and language models.
- ACTION_ASSIST activity entry point for compatible system handoff; default-assistant role registration is deferred until a valid recognition-service provider is integrated.
- Deterministic command parser and JUnit coverage.
- Contact lookup with contextual permission rationale, exact-first matching, duplicate-number normalization, and chooser for ambiguous matches.
- Persistent local activity history (last 30 entries); microphone and Contacts permission denials have clear feedback.
- Launch WhatsApp / WhatsApp Business, YouTube, Chrome, and Android Settings.
- Contact dialing through the system dialer; user confirms the call.
- SMS composer for phone-number or contact-name recipients, optional body prefill, and manual review/send.
- Local offline utilities: current time, date, battery percentage, Maps launch, and command help.
- Refreshed PK-branded original interface with animated voice orb, quick-action grid, compact composer, and persistent conversation panel.

## Wake-word and platform limitations

- PK listening currently requires the app to be open and the user to start listening. The app does not currently register as Android's default VoiceInteractionService; ACTION_ASSIST alone does not replace the system assistant.
- Screen-off hotword detection depends on a compatible hotword engine, keyphrase enrollment, device SoundTrigger/DSP support, Android background-microphone rules, and OEM behavior. It is not safe to claim universal support or emulate it with continuous SpeechRecognizer loops.
- Offline recognition depends on the installed speech-recognition provider and language packs.
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
