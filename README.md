# PersonalAI — Native Android Voice Assistant

PersonalAI is a native Kotlin Android assistant for Android 13–16. The initial MVP uses Android speech and phone intents so core commands do not require a paid AI API or backend.

## Current MVP features

- Voice and text command entry with a Siri-inspired dark interface, animated gradient voice orb, tap-to-listen interaction, and compact command composer.
- User-started wake listening while the app remains open.\n- Android default assistant role request through the system consent screen; the app handles the ACTION_ASSIST entry point.
- Two-step interaction: say "Hey Assistant", then speak the command; combined wake phrase + command is also supported.
- Persisted speech-language selector: English (India) and Hindi (India).
- Android SpeechRecognizer and TextToSpeech.
- Deterministic command classification extracted into an Android-independent Kotlin parser.
- Unit tests for Hindi/English app, call, SMS, alarm, unknown, and whitespace cases.
- Contact matching prioritizes exact names, then prefix and partial matches; duplicate numbers are collapsed.
- If multiple equally ranked contacts match, the app asks the user to choose instead of guessing.
- Launch WhatsApp / WhatsApp Business, YouTube, Chrome, and Android Settings.
- Permission-gated contact lookup and system dialer handoff for user confirmation.
- SMS composer supports phone-number or contact-name recipients, optional dictated message-body prefill, ambiguity chooser, and manual user review/send.
- Alarm screen shortcut and visible command history.

## Honest platform limitations

- The current default-assistant integration handles the system assistant invocation and starts app-level listening. It is not yet a full VoiceInteractionService/hotword implementation, so always-on screen-off "Hey Assistant" is not available. A proper VoiceInteractionService and session implementation is a separate milestone.
- A normal third-party app cannot silently answer, originate, or disconnect arbitrary calls. Cellular call disconnect requires a compliant default-dialer implementation and the user's role approval.
- WhatsApp does not expose general third-party controls for arbitrary call control or silent message sending. Supported intents/deep links and user confirmation are the only planned approach.
- Speech recognition and offline language availability depend on the device's installed recognition service and language packs.
- Emergency calling, lock-screen bypass, stealth recording, and silent message/call sending are out of scope.

## Build

1. Open this repository in Android Studio.
2. Use JDK 17 and Android SDK Platform 36.
3. Sync the Gradle project and run the `app` configuration on an Android 13–16 device/emulator.
4. Grant microphone permission. Contacts permission is requested only when needed.

GitHub Actions runs `testDebugUnitTest` before `assembleDebug`, then uploads the debug APK as the `personalai-debug-apk` artifact after a successful run. Artifacts are temporary (14-day retention).

## Branch strategy

- `main`: stable reviewed code.
- `development/*`: active implementation branches.
- Changes merge through pull requests only after build and device-test review.

## Project continuity

Read `docs/PROJECT_MEMORY.md`, `docs/ROADMAP.md`, and `docs/COMMANDS.md` before making architectural changes or adding new device actions.

## Current validation status

The contact-name SMS parser and composer handoff are implemented on `development/mvp-foundation`. The Siri-inspired visual redesign is now in the same branch and requires CI validation for its latest head. No physical Android device test has yet been recorded.
