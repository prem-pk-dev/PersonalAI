# PersonalAI — Native Android Voice Assistant

PersonalAI is a native Kotlin Android assistant for Android 13–16. The initial MVP uses Android speech and phone intents so core commands do not require a paid AI API or backend.

## Current MVP features

- Voice and text command entry.
- User-started wake listening while the app remains open.
- Two-step interaction: say "Hey Assistant", then speak the command; combined wake phrase + command is also supported.
- Persisted speech-language selector: English (India) and Hindi (India).
- Android SpeechRecognizer and TextToSpeech.
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

GitHub Actions builds a debug APK and uploads it as the `personalai-debug-apk` artifact after a successful run. Artifacts are temporary (14-day retention).

## Branch strategy

- `main`: stable reviewed code.
- `development/*`: active implementation branches.
- Changes merge through pull requests only after build and device-test review.

## Project continuity

Read `docs/PROJECT_MEMORY.md`, `docs/ROADMAP.md`, and `docs/COMMANDS.md` before making architectural changes or adding new device actions.

## Current validation status

A CI run for commit `d191288e62089edb00b388caf51a571f042cba19` successfully built the debug APK and uploaded the artifact. Subsequent speech-language-selector changes are being revalidated; the latest branch head must pass CI before it is considered green. No physical Android device test has yet been recorded.
