# PersonalAI — Project Memory & Non-Negotiable Decisions

This document is the continuity record for future development sessions. Read it before changing architecture or scope.

## Product goal

Build a personal Android AI assistant that accepts both voice and text and performs useful phone actions from natural-language commands. It must do more than chat: understand a request, select a supported action, execute it through Android's approved APIs/intents, and report the result by voice and text.

## User-approved requirements

- Project/repository: `prem-pk-dev/PersonalAI`.
- Platform: native Android application.
- Target test range: Android 12–16 (API 31–36); user test device is Redmi Note 9 on Android 12 / MIUI 13.
- Primary language: Kotlin.
- Input: voice + text.
- The only configured wake word is "PK"; "Hey Assistant" and "Hey PersonalAI" are not wake phrases. Hindi speech transcript variants "पीके" and "पी के" are accepted.
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
- The app declares an ACTION_ASSIST activity entry point. Default-assistant ROLE_ASSISTANT registration and VoiceInteractionService registration are currently deferred because the required RecognitionService provider has not been integrated.
- The wake word is "PK" (Hindi STT variants "पीके" / "पी के"). SpeechRecognizer-based PK recognition still requires the app to be open and listening started by the user. The registered assistant services are not an always-on hotword detector. Screen-off detection requires a compatible hotword engine, keyphrase enrollment, device SoundTrigger/DSP support, and foreground/background lifecycle validation.
- Android 14+ exposes `SpeechRecognizer` language detection callbacks and allowed-language constraints, but provider implementations may ignore the extras. Android 12 (the user's Redmi Note 9) does not expose this API; the current app falls back to transcript-script/Romanized-Hindi heuristics and still needs device verification.
- sherpa-onnx is a viable offline Android candidate for keyword spotting and speaker verification. Its official docs describe open-vocabulary KWS (custom keywords without retraining) and list English/Chinese pretrained KWS models; this does not establish Hindi wake-word accuracy or Redmi Note 9 performance.
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
- The UI explicitly prompts users to say "PK"; a regression test confirms "Hey Assistant" does not trigger the wake parser.
- Two-step wake handling is implemented: say "PK" first, then command in the next recognition result; combined "PK + command" also works. Hindi speech transcript variants "पीके" / "पी के" are supported.
- Auto Language Mode replaces the manual language selector: recognized Devanagari and common Romanized-Hindi input selects Hindi (India) TTS; Latin input otherwise defaults to English (India). On Android 14+, enable recognizer language detection constrained to `en-IN` and `hi-IN`; Android 12 uses the heuristic fallback. Both require physical-device verification.
- SMS composer supports phone-number and contact-name recipients, optional dictated body prefill, and manual user review/send. Contact-name SMS shares the exact-first contact matcher and ambiguity chooser with dialing.
- Permission flow now explains microphone use (only after user starts listening) and Contacts use (only for requested call/SMS lookup), and reports denials with available alternatives.
- Quick Actions and Recent Conversation were removed from the home screen at the user's request; command-history persistence was removed as well.
- The home screen uses PK branding, a dark cyan-accented layout, animated orb, quick action grid, compact text composer, and conversation history. A custom cyan/purple PK adaptive launcher icon and app label were added.
- New offline utility intents provide device time, date, battery percentage, Maps launch, and help. Parser tests cover these intents.
- No physical Android device test has yet been performed. Auto language recognition and TTS language-pack behavior remain unverified on the Redmi Note 9.
- CI run 43578152900 passed for commit 20a849eef89bf702ef0267efe471d003a8292913: unit tests, debug APK build, and artifact upload succeeded. Artifact personalai-debug-apk (artifact ID 14107681808, SHA-256 43d31f51f3954aa66d0bf74b37257848701be4f96c9b8f2f3a064373168771d1) expires 2026-11-28. This artifact predates the subsequent VoiceInteraction registration changes and is not the latest APK.
- VoiceInteractionService/session-service registration was briefly added, then safely removed because the required RecognitionService metadata/provider was not integrated. The app retains ACTION_ASSIST but is not currently registered as the default assistant.
- Latest branch head: `4e92118e59623b110317c04d176c24c0f06b426f` (2026-11-14), commit message `Render assistant session text with proper line breaks`.
- CI run `43578918662` passed for that exact commit: unit tests, debug APK build, and artifact upload succeeded. Artifact `personalai-debug-apk`, ID `14107658281`, size 877,405 bytes, SHA-256 `fd7e5a505f178281dcb00280826096632b8073ea3792a81161e110733b99f01d`, expires 2026-11-28.
- CI runs `lintDebug testDebugUnitTest assembleDebug`. Run `43586183849` passed for exact commit `ebe804b4084d399696cf320dc0fee07f667fc968`: lint, unit tests, APK build, and artifact upload all succeeded. Artifact `personalai-debug-apk`, ID `14109918243`, size 874,250 bytes, SHA-256 `74699af9196568e009381b0ce5f8d14b19a214d0cab81dfb3d7f1f059ea08474`, expires 2026-11-28.
- No physical Android device test has yet been performed; do not claim screen-off PK behavior.

## UI direction

- The native Android interface uses a Siri-inspired dark assistant experience: animated gradient orb, tap-to-listen interaction, listening state, language toggle, response panel, and compact text composer.
- This is a visual design reference only; the app remains a native Android Kotlin app and does not claim to be Apple Siri or iOS.

## Assistant role integration

- MainActivity declares an `android.intent.action.ASSIST` handler, but does not currently request `RoleManager.ROLE_ASSISTANT`.
- The incomplete VoiceInteractionService and session-service registrations were removed to avoid advertising an assistant provider without the required RecognitionService metadata/provider.
- A valid RecognitionService provider is a prerequisite before restoring ROLE_ASSISTANT and VoiceInteractionService integration.
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

1. Keep the lint-enabled CI gate green for each code change.
2. Integrate a valid RecognitionService provider compatible with the Android support range before restoring default-assistant registration.
3. Complete source-level review of permissions, intents, and user-confirmation boundaries.
4. Improve permission rationale, error states, and persistent command history.
5. Integrate and validate sherpa-onnx open-vocabulary KWS (Apache-2.0 code; verify model licence) with a custom PK keyword; implement a foreground microphone service using AudioRecord, not a SpeechRecognizer restart loop.
6. Integrate local speaker verification with enrollment and anti-replay caveats; then install and test the candidate APK on the user's physical Android device.
7. Merge to main only after CI is green, review is complete, and device testing is recorded.

## Working protocol

- Inspect the latest branch and PR before every continuation.
- Work in a dedicated `development/*` branch; do not directly change `main`.
- Preserve existing features when editing files; inspect the current file and patch narrowly.
- After each logical change: commit, inspect CI, inspect logs on failure, fix, and re-run.
- Do not claim an APK exists unless the workflow artifact or a local build has been verified.
- Do not merge automatically. Keep the PR draft until validation criteria are met.
- Update this file and the roadmap whenever scope or architecture changes.

## Wake-word integration decision (2026-11-14)

- Reviewed the upstream sherpa-onnx Android KWS sample and current KWS documentation. The official Android demo uses `AudioRecord` at 16 kHz mono PCM, a native `KeywordSpotter`, a streaming `OnlineStream`, and a worker thread; it is not a SpeechRecognizer retry loop.
- sherpa-onnx documents open-vocabulary keyword lists, so custom phrases can be supplied without retraining. Its published KWS model families currently documented here are English GigaSpeech, Chinese Wenetspeech, and Chinese+English; none establishes Hindi wake-word support. The English GigaSpeech model is English-only. A phonetic English phrase for spoken “P-K” may be prototyped, but false accepts/rejects and Hindi-accent performance must be measured on the Redmi before enabling it by default.
- Do not integrate an unofficial Maven mirror or copy native binaries/models from an unverified source. The upstream Android demo source tree packages native libraries and model assets directly; dependency, ABI, licence, model checksum, APK size, and offline-install behavior must be pinned and reviewed before adding it to this app.
- A KWS foreground service is not yet implemented. Do not advertise screen-off/background wake detection until service lifecycle, Android 12+ microphone FGS rules, notification/consent, MIUI autostart/battery settings, and physical-device tests are complete.
- Speaker verification is a separate capability from keyword spotting. It needs a supported embedding/verification model, explicit voice enrollment, locally protected templates, threshold evaluation, and replay/false-accept testing. No owner-only voice gate is implemented yet.
- Automatic language resolution now prefers a supported recognizer language tag (hi/en), then falls back to transcript heuristics. Unit tests cover provider precedence, unsupported tags, and transcript fallback.
