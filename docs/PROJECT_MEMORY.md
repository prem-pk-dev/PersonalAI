# PersonalAI — Project Memory & Non-Negotiable Decisions

This document is the continuity record for future development sessions. Read it before changing architecture or scope.

## Product goal

Build a personal Android AI assistant that accepts both voice and text and performs useful phone actions from natural-language commands. It must do more than chat: understand a request, select a supported action, execute it through Android's approved APIs/intents, and report the result by voice and text.

## User-approved requirements

- Project/repository: `prem-pk-dev/PersonalAI`.
- Platform: native Android application.
- Supported device range: Android 13–16 (API 33–36).
- Primary language: Kotlin.
- Input: voice + text.
- Wake word: "PK"; Hindi speech transcript variants "पीके" and "पी के" are accepted.
- Budget: ₹0; prefer Android platform APIs and free/open-source tooling.
- Delivery order: first make a working MVP, then add advanced capabilities incrementally.
- The user expects progress updates and continuity; do not silently discard agreed features or constraints.

## Core user examples

- "WhatsApp kholo"
- "Call [contact]"
- "Call disconnect karo"
- "YouTube kholo"
- "Chrome kholo"
- "Send a message"
- Device settings, alarm, and other supported phone actions.

## Architecture principles

1. Native Kotlin Android app; do not replace it with a website, PWA, Flutter, or React Native without explicit approval.
2. No paid API, paid hosting, or backend dependency for the initial MVP.
3. Prefer local command routing for deterministic device actions.
4. Add online AI only as an optional adapter later; basic commands must remain usable without it.
5. Separate speech input, command understanding, action execution, and response/TTS into testable components as the project grows.
6. Keep permissions minimal and request them only when a feature needs them.
7. Every high-impact action must be transparent and user-confirmed where Android or safety requires it.
8. Never store credentials, API keys, contact contents, or private user data in source control.
9. Maintain a clear README, roadmap, and this project memory file when scope or architecture changes.

## Android platform constraints — do not promise impossible behavior

- Third-party apps cannot freely control another app's private UI or internal calling state.
- Cellular call disconnect requires a compliant default-dialer implementation and the user's explicit role approval; it is not a normal app permission.
- WhatsApp does not expose general public APIs for arbitrary call control or silently sending messages. Use supported deep links/intents and user confirmation only.
- A hotword that listens with the screen off requires a carefully designed, user-started foreground service, visible notification, microphone permissions/service declarations, and compliance with Android background-start restrictions. Device OEM battery policies may still affect reliability.
- The app declares ACTION_ASSIST, requests Android ROLE_ASSISTANT only after user action, and registers VoiceInteractionService plus VoiceInteractionSessionService with binding permissions and service metadata. The session opens the full app.
- The wake word is "PK" (Hindi STT variants "पीके" / "पी के"). SpeechRecognizer-based PK recognition still requires the app to be open and listening started by the user. The registered assistant services are not an always-on hotword detector. Screen-off detection requires a compatible hotword engine, keyphrase enrollment, device SoundTrigger/DSP support, and foreground/background lifecycle validation.
- Offline speech recognition depends on the device's installed recognition provider and language packs.
- Do not bypass lock-screen protections, record secretly, auto-send messages, or automate emergency calling.

## Current repository state (2026-11-14)

- Default branch: `main`; keep it stable.
- Active branch: `development/mvp-foundation`.
- Pull request: #1, draft, targets `main`.
- Android Gradle project, native Activity, CI workflow, and continuity documentation are committed to the development branch.
- The first CI attempt failed because an Activity property named `background` shadowed the View background property. It was renamed to `backgroundColorValue`.
- CI for commit `d191288e62089edb00b388caf51a571f042cba19` passed: debug APK build and artifact upload both succeeded.
- CI for commit `ecc07952859a23384cb7ac36f540e365d42187e1` passed on push and pull-request workflows after the speech-language state fix.
- Commit `65b6ec0843319fde15d464fd1f4f2cabf00742fb` passed GitHub Actions Build #32: `testDebugUnitTest`, `assembleDebug`, and APK artifact upload all succeeded. Artifact: `personalai-debug-apk`, SHA-256 `a510f5f35a617ff6569e7a2ecd07558f91459e40aeb0587d7d2f972bea6b3a58`.
- A UI inspection found that the Run Text Command button was created but omitted from the displayed view list. It is now included in the layout after the text input.
- Command classification has been extracted from MainActivity into Android-independent `CommandParser.kt` and `AssistantCommandType`.
- JUnit tests cover Hindi/English app launches, calls, SMS, alarms, whitespace, and unknown commands.
- A standalone `ContactMatcher.kt` now ranks exact-name matches before prefix and partial matches, and deduplicates equivalent phone-number formatting.
- Contact lookup now presents a chooser when multiple equally ranked contacts match; it never silently picks the first result.
- `ContactMatcherTest.kt` covers exact ranking, ambiguity, number deduplication, and empty/unmatched queries. This newest contact-matching change still requires CI validation.
- CI runs `gradle testDebugUnitTest assembleDebug` before uploading the APK.
- Two-step wake handling is implemented: say "PK" first, then command in the next recognition result; combined "PK + command" also works. Hindi speech transcript variants "पीके" / "पी के" are supported.
- English (India) / Hindi (India) speech language selector is persisted in SharedPreferences.
- SMS composer supports phone-number and contact-name recipients, optional dictated body prefill, and manual user review/send. Contact-name SMS shares the exact-first contact matcher and ambiguity chooser with dialing.
- No physical Android device test has yet been performed.
- CI run 43578152900 passed for commit 20a849eef89bf702ef0267efe471d003a8292913: unit tests, debug APK build, and artifact upload succeeded. Artifact personalai-debug-apk (artifact ID 14107681808, SHA-256 43d31f51f3954aa66d0bf74b37257848701be4f96c9b8f2f3a064373168771d1) expires 2026-11-28. This artifact predates the subsequent VoiceInteraction registration changes and is not the latest APK.
- The manifest now registers PersonalAIVoiceInteractionService and PersonalAIVoiceInteractionSessionService; the session surface launches the full app. These later changes require CI verification.
- Latest branch head: `4e92118e59623b110317c04d176c24c0f06b426f` (2026-11-14), commit message `Render assistant session text with proper line breaks`.
- CI run `43578918662` passed for that exact commit: unit tests, debug APK build, and artifact upload succeeded. Artifact `personalai-debug-apk`, ID `14107658281`, size 877,405 bytes, SHA-256 `fd7e5a505f178281dcb00280826096632b8073ea3792a81161e110733b99f01d`, expires 2026-11-28.
- The successful CI run currently executes `testDebugUnitTest assembleDebug`; static Android lint is not yet part of the workflow.
- No physical Android device test has yet been performed; do not claim screen-off PK behavior.

## UI direction

- The native Android interface uses a Siri-inspired dark assistant experience: animated gradient orb, tap-to-listen interaction, listening state, language toggle, response panel, and compact text composer.
- This is a visual design reference only; the app remains a native Android Kotlin app and does not claim to be Apple Siri or iOS.

## Assistant role integration

- MainActivity requests `RoleManager.ROLE_ASSISTANT` after explicit user action and registers an `android.intent.action.ASSIST` handler.
- Android may allow PersonalAI to be selected as the default assistant only if the device exposes the role and accepts the app's qualification; the user must approve the system prompt.
- `PersonalAIVoiceInteractionService` and `PersonalAIVoiceInteractionSessionService` are registered with the required binding permissions and metadata.
- The minimal assistant session opens the full app when tapped. It does not yet start speech recognition or a hotword detector.
- PK listening remains app-open and user-started. Screen-off PK requires separate engine, enrollment, hardware, and device validation.

## MVP scope

- Voice recognition using Android SpeechRecognizer.
- Text command input and visible Run Text Command control.
- TextToSpeech responses.
- Explicit English (India) / Hindi (India) speech language selection.
- Unit-testable deterministic Hindi/English command classification.
- Exact-first, ambiguity-safe contact matching and user-selected dialer handoff.
- Wake phrase detection during an explicitly user-started listening session while the app is open.
- Launch WhatsApp / WhatsApp Business, YouTube, Chrome, and Android Settings.
- Contact lookup after Contacts permission and open system dialer for user confirmation.
- Open SMS composer with optional prefilled body for user review and send.
- Alarm screen intent.
- Visible command history.

## Next development sequence

1. Add Android static lint to CI and resolve any actionable findings.
2. Complete source-level review of permissions, assistant services, intents, and user-confirmation boundaries.
3. Improve permission rationale, error states, and persistent command history.
4. Research a free offline PK hotword engine, licence, keyphrase enrollment, and device SoundTrigger/DSP compatibility before foreground microphone implementation.
5. Add optional AI reasoning only after deterministic phone actions are reliable.
6. Install and test the final candidate APK on the user's physical Android device; record Android version and observed behavior.
7. Merge to main only after CI is green, review is complete, and device testing is recorded.

## Working protocol

- Inspect the latest branch and PR before every continuation.
- Work in a dedicated `development/*` branch; do not directly change `main`.
- Preserve existing features when editing files; inspect the current file and patch narrowly.
- After each logical change: commit, inspect CI, inspect logs on failure, fix, and re-run.
- Do not claim an APK exists unless the workflow artifact or a local build has been verified.
- Do not merge automatically. Keep the PR draft until validation criteria are met.
- Update this file and the roadmap whenever scope or architecture changes.
