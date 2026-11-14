# PersonalAI — Native Android Voice Assistant

PersonalAI is a privacy-conscious, zero-budget-first Android assistant built with Kotlin. The first milestone is a reliable on-device command shell; advanced AI and background wake-word behavior are added only after the core app builds and is tested.

## MVP scope

- Android 13–16 target devices (API 33–36).
- Voice and text command entry.
- User-started listening session with a visible listening state.
- Hindi/English speech recognition using Android speech services.
- Spoken responses using Android TextToSpeech.
- Launch supported apps (WhatsApp, YouTube, Chrome, Settings).
- Open the system dialer with a resolved contact/number for user confirmation.
- Open the SMS composer for user review and send.
- Local command history; no server and no paid API required.

## Honest platform limitations

- A normal app cannot silently answer, originate, or disconnect arbitrary calls. The MVP opens the system dialer; later cellular-call controls require implementing a compliant default dialer and obtaining the user's role approval.
- WhatsApp does not expose general third-party controls for its calls or arbitrary message sending. We will use supported intents/deep links where available and require user review.
- "Hey Assistant" always-on hotword detection is not guaranteed for third-party apps. The first milestone detects a wake phrase only during a user-started, visible listening session. A later foreground-service experiment must be explicitly enabled and comply with Android microphone/background restrictions.
- Speech recognition and offline language availability depend on the device's installed recognition service and language packs.
- Emergency calling, lock-screen bypass, stealth recording, and silent message/call sending are out of scope.

## Development

Open the repository in Android Studio with JDK 17. The Gradle project is under `app/`. Install Android SDK Platform 36 and run the `app` configuration on a physical Android device or emulator. Grant microphone permission; Contacts permission is requested only when needed.

## Branch strategy

- `main`: stable reviewed code.
- `development/*`: active implementation branches.
- Changes merge through pull requests after build/test review.

## Current status

Initial repository foundation. CI and physical-device validation are pending.
