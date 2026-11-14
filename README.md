# PersonalAI — Native Android Voice Assistant

PersonalAI is a native Kotlin Android assistant for Android 13–16. The initial MVP uses Android speech and phone intents so core commands do not require a paid AI API or backend.

## Current MVP features

- Voice and text command entry with a visible Run Text Command button.
- User-started wake listening while the app remains open.
- Two-step interaction: say "Hey Assistant", then speak the command; combined wake phrase + command is also supported.
- Persisted speech-language selector: English (India) and Hindi (India).
- Android SpeechRecognizer and TextToSpeech.
- Deterministic command classification extracted into an Android-independent Kotlin parser.
- Unit tests for Hindi/English app, call, SMS, alarm, unknown, and whitespace cases.
- Contact matching prioritizes exact names, then prefix and partial matches; duplicate numbers are collapsed.
- If multiple equally ranked contacts match, the app asks the user to choose instead of guessing.
- Launch WhatsApp / WhatsApp Business, YouTube, Chrome, and Android Settings.
- Permission-gated contact lookup and system dialer handoff for user confirmation.
- SMS composer with optional dictated message-body prefill; user reviews and sends.
- Alarm screen shortcut and visible command history.

## Honest platform limitations

- The wake phrase is not always-on. Screen-off/background listening is a separate foreground-service milestone and must be explicitly started by the user.
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

The latest fully verified build before the contact-matching enhancement is commit `65b6ec0843319fde15d464fd1f4f2cabf00742fb` (GitHub Actions Build #32): unit tests, debug APK build, and artifact upload all passed. The new contact-matching and ambiguity-selection changes require CI validation for the new head. No physical Android device test has yet been recorded.
