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
- The app declares an ACTION_ASSIST activity entry point and a user-consented ROLE_ASSISTANT request flow. A real VoiceInteractionService/session service is still not implemented; do not describe the app as a complete system assistant.
- The wake word is "PK" (Hindi STT variants "पीके" / "पी के"). Background KWS is a separate, user-started foreground service with a visible notification. Wake detection currently requires tapping the notification to open PK before command recognition; seamless hands-free wake-to-command and screen-off behavior remain unverified.
- Android 14+ exposes `SpeechRecognizer` language detection callbacks and allowed-language constraints, but provider implementations may ignore the extras. Android 12 (the user's Redmi Note 9) does not expose this API; the current app falls back to transcript-script/Romanized-Hindi heuristics and still needs device verification.
- sherpa-onnx is a viable offline Android candidate for keyword spotting and speaker verification. Its official docs describe open-vocabulary KWS (custom keywords without retraining) and list English/Chinese pretrained KWS models; this does not establish Hindi wake-word accuracy or Redmi Note 9 performance.
- Do not bypass lock-screen protections, record secretly, auto-send messages, or automate emergency calling.

## Current repository state (2026-11-16)

- Default/stable branch: `main`; keep it stable and do not modify directly.
- Active implementation branch: `feature/sherpa-onnx-kws-spike`.
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

## sherpa-onnx packaging review (2026-11-14)

- The upstream Android KWS sample source alone is not a complete dependency: its checked-in `assets` and `jniLibs` folders are placeholders; its sample calls `System.loadLibrary("sherpa-onnx-jni")` and expects model files under app assets. Do not copy the sample Kotlin files without providing the native runtime and model assets.
- The official sherpa-onnx v2.0.1 Android AAR release asset is published with a SHA-256 checksum in the official release notes. It is a large all-ABI AAR (about 47.2 MB); official Android packaging instructions also describe ABI-specific native libraries. Evaluate APK size and ABI filtering before adding it.
- Candidate English GigaSpeech mobile KWS model is separate from the runtime. It must be pinned, checksum-verified, licence-reviewed, and provisioned for offline first-run behavior. Do not make wake-word detection depend on a network download after the app is already running.
- Decision: no native KWS dependency or foreground service has been added yet. First create a small isolated integration branch/CI proof that verifies the exact v2.0.1 AAR API, JNI loading, arm64-v8a packaging, model loading, and custom keyword decoding. Only then wire it into the production assistant lifecycle. This avoids shipping a nonfunctional native wrapper or falsely claiming screen-off support.
- Research references: https://github.com/k2-fsa/sherpa-onnx/releases/tag/v2.0.1 ; https://k2-fsa.github.io/sherpa/onnx/android/build-sherpa-onnx.html ; https://github.com/k2-fsa/sherpa-onnx/tree/master/android/SherpaOnnxKws


## Current verified implementation snapshot (2026-11-16)

- Latest inspected CI run: Build #287, commit `89a208af4b2c57a5fec776ea976955ec5c59af5d`, passed clean build, unit tests, ARM JNI packaging checks, APK signature/package metadata verification, and uploaded artifact `personalai-debug-apk-device-test` (artifact ID `14211223756`, SHA-256 `78aac31b88141a77ea5ae124cad551eb9a842d7052b45589425ae11b548643de`). This is a device-test artifact, not a final release.
- Hindi offline one-shot ASR is now integrated in `HindiOfflineAsrEngine.kt`, `HindiOfflineModelInstaller.kt`, and `HindiPcmAudio.kt`. It downloads a pinned IndicConformer int8 model on explicit user action, verifies model SHA-256, stores files privately, captures mono 16 kHz PCM and transcribes locally through Sherpa-ONNX. CI verifies code/build only; the large model download and transcription accuracy have not been tested on Redmi Note 9.
- User must install the optional ~198 MB model and tap Speak Hindi offline. This is not a continuous offline Hindi listener and does not provide Hindi hotword detection.
- Active-media-session commands for play/pause/next/previous are integrated with explicit Notification Access and passed CI; real media app behavior remains a device test.
- Installed-app discovery, user-confirmed WhatsApp share composition, alarm/timer parsing, call/SMS composers, calendar composer and limited AccessibilityService actions are implemented; several app handoffs and gestures remain device tests.
- Remaining major work: real VoiceInteractionService/session integration; seamless KWS-to-command handoff and automatic KWS resume; Redmi Note 9 tests for Hindi model install/transcription, wake word, media, accessibility, permissions, battery/thermal/memory; broader Hinglish offline recognition; regression coverage for all commands; privacy review; signed release build.
- Do not report a numeric overall progress percentage unless it is calculated from a defined feature checklist. Keep CI-verified and physical-device-verified status separate.


## KWS speech lifecycle correction (2026-11-16)

- Source review found that media play/pause/next/previous and volume up/down/mute commands speak a TTS response but were omitted from the deferred-KWS-resume command set. The app could restart microphone KWS before the reply finished, risking self-capture and unnecessary CPU/microphone contention.
- Added `KwsSpeechLifecycle.shouldResumeAfterSpeech` and wired it into `MainActivity.handleCommand`. The policy defers listener restart until TTS completion for the media/volume commands and preserves the existing spoken-command behavior; it does not schedule a restart when the user opt-in is off.
- Added `KwsSpeechLifecycleTest` covering media/volume, existing spoken commands, opt-out, and app-launch behavior.
- Commits: helper `bce97d3d5b5f13fb7f70531746904966bf5d1f30`; tests `8b54e079a77b20d5e084d908b0e37cac2d11d01c`; Activity integration / current head `52e0c5824806c2ab6a25183da1567b77f41215e1`.
- CI for the new head is pending verification. Do not count the new tests as passed until a matching GitHub Actions run confirms them.


## Hindi PCM / voice-activity regression coverage (2026-11-16)

- Strengthened `HindiPcmAudioTest` to assert exact PCM16 normalization at signed extrema and half-scale values, requested-length truncation, invalid lengths for both conversion and RMS helpers, silence RMS, and a known-signal RMS value.
- These tests exercise deterministic audio-frame preprocessing used before offline Hindi transcription; they do not claim microphone capture or model inference has been physically verified.
- Commit: `31bc2f8bd4007b9e55c46fcb25264f3ed3075d95`. CI for the exact current head remains pending.


## Hindi offline model installation safety (2026-11-16)

- Code review found the Hindi model installer deleted the current destination directory immediately before renaming the verified staging directory. A rename failure at that point could leave the user with no working model.
- Added `HindiModelDirectoryCommit` to promote the verified staging directory while preserving the previous model as a temporary backup. It rolls back on promotion failure and restores the backup if a previous process was interrupted after moving the destination.
- Added JVM tests for successful replacement, first installation, missing staging preservation, and interrupted-promotion recovery.
- The tests are committed but not yet CI-verified; this is filesystem transaction coverage, not a physical-device download test.
- Code commits: installer integration `0731d176cb30043bedcdc237ebf277b317fdccbd`; helper `e6172a6474f68759f66b9c17219203cf12992510`; tests `eeec301dc696406a8bf9948ce99b58e0c4b7a1e4`, recovery fix `79dfe428818ee2b08462474746f4f6c4a3910988`, recovery test `2ccdd62176f82e8df9c9efe3f049ce041a64ff69`.


## KWS capture failure shutdown (2026-11-16)

- Fixed a foreground-service failure mode: an unexpected AudioRecord/model/native exception previously updated the notification but could leave the service foregrounded with the persisted KWS opt-in still true, even though capture had stopped.
- The worker now distinguishes a requested stop from a real capture failure. After releasing recorder, stream, spotter, and CPU WakeLock resources, it posts main-thread cleanup that clears the persisted opt-in, updates a non-ongoing error notification, detaches foreground state, and stops the service.
- This is code-level lifecycle hardening only. Android 12 / MIUI service restart, permission revocation, and notification behavior remain physical-device checks; latest CI is not yet verified.
- Commit: `affe477eba8557b29c1511d3aaac6e0b7703eca5`.


## Wake-to-command failure recovery (2026-11-16)

- Review found a handoff edge case: after KWS detects the keyword, its foreground service stops. If command speech recognition is unavailable or fails to start, the listener could remain opted-in but stopped.
- Added a pure KWS handoff failure policy. The Activity now schedules listener restoration after its spoken error response only when KWS remains opted-in and RECORD_AUDIO permission is granted. If microphone permission is granted through the permission prompt, listener restoration is likewise deferred until the permission response TTS completes. If permission is denied, persisted KWS opt-in is cleared.
- Added a unit test covering all opt-in / microphone permission combinations.
- Device behavior and exact-head CI remain unverified.
- Commits: policy `ac695bfc8e9fcfcef6007d9bd250b5c56e8a2319`; test `924e3f7652e3aeb4004086bc30efc0817e3733fc`; Activity integration `35cf3b6f77a04b583f5e779e05bbb15b638c17ca`.


## Hindi offline listening cancellation (2026-11-16)

- Added a visible Cancel button to the Hindi offline ASR controls. It marks the one-shot capture cancelled; the worker checks the flag during capture, before native recognizer creation/transcription, and after transcription so a cancelled result is not sent to command handling.
- The cancel window closes immediately before a completed transcript is posted to the UI thread, preventing a last-moment cancel from suppressing command dispatch while also blocking KWS restoration.
- On cancellation or failed capture, microphone/native resources are released before an opted-in KWS listener is scheduled to resume. Activity teardown is checked before touching UI or restarting the service.
- Device cancellation and native-transcription interruption behavior remain unverified; Sherpa decode itself is not forcibly interrupted mid-call.
- Commits: UI and lifecycle `db7eabfc43e332800dad3f9077b1239382e78833`; teardown guard `5599ebb9ffb77f2ac33894df21c9bf5a09cfdf87`; cancel race guard `94edfb967a031565c91dac67bb0c864987011d45`.


## TTS failure recovery for KWS handoff (2026-11-16)

- Fixed a wake-to-command edge case where a missing/uninitialized Android TextToSpeech engine returned early from `speak()`, potentially leaving an opted-in KWS listener stopped after keyword detection. TTS initialization failure now also triggers the pending handoff recovery.
- KWS restart is now gated by an explicit pending restart request, persisted listener opt-in, and RECORD_AUDIO permission; permission and opt-in are checked again immediately before delayed service start, and a destroyed/finishing Activity or active Hindi ASR blocks restart.
- Added unit coverage for the TTS failure policy. Android TTS-disabled / missing voice-data behavior remains a device test.
- Commits: TTS recovery `5c805d8f3d700752f83c7c007c5c7a384a0d917b`; policy `8f8ace288c3509644e0bee67b56ceca7476a8f76`; Activity integration `b247839c51ba675e64d65f3dc0799f4a11c31ffa`; test `5621411deac604aae751967766cfcef9533b8181`.


## SpeechRecognizer retry-loop protection (2026-11-16)

- Replaced the unbounded wake-mode SpeechRecognizer.onError restart with a finite four-retry backoff: 700 ms, 1.4 s, 2.8 s, and 5 s. A fifth consecutive error stops the recognizer, turns off the listening orb, and displays a clear retry instruction.
- A failed wake-to-command handoff schedules KWS recovery through the existing opt-in and microphone-permission gate. Ordinary foreground listening failures do not silently enable background listening.
- Delayed callbacks re-check that the Activity is alive and wake mode remains active. The failure counter resets for a new listening session and after a results callback, not merely when the provider says it is ready.
- Added JVM tests for the retry sequence, exhaustion boundary, and invalid counts. Exact-head CI and Redmi Note 9 behavior remain unverified.


## Accessibility scroll app allowlist (2026-11-16)

- Added an explicit in-app multi-select allowlist for scroll gestures in installed YouTube, WhatsApp, WhatsApp Business and Chrome packages. The default is deny-all; users must select each app before PK can scroll it.
- Accessibility service reads only the active window package identifier to enforce the allowlist, then traverses scrollable nodes without reading node text. Back/Home remain separately disclosed global navigation actions.
- Added JVM tests for default deny, allowed package, unselected package and missing active package. Exact-head CI and Redmi Note 9 interaction checks remain pending.


## Wake acknowledgement microphone handoff (2026-11-16)

- Review found that a wake-only SpeechRecognizer result ("PK") spoke an acknowledgement but the result callback still scheduled another recognizer start after 500 ms. That could reopen the microphone while TTS was speaking.
- The Activity now marks a pending recognition restart only when both wake mode and awaiting-command are active, suppresses the immediate result-loop restart, and starts command recognition after the assistant utterance completes. Missing/failed TTS uses the same guarded recovery path.
- Added a unit-testable lifecycle predicate. Redmi Note 9 microphone/TTS overlap behavior and exact-head CI remain unverified.


## KWS foreground notification persistence (2026-11-16)

- Corrected the foreground notification builder so a user-started active KWS session is explicitly marked ongoing at the moment the service enters foreground mode. Previously, the notification was constructed before the capture worker set its running flag, so it could be created as dismissible even though the service remained foreground.
- Detection and error notifications retain non-ongoing behavior after capture stops. Redmi Note 9 notification shade behavior and exact-head CI remain to be verified.


## Alarm time parser ambiguity guard (2026-11-16)

- Hardened `AlarmTimeParser`: it no longer interprets an arbitrary number as a clock time. A candidate must have an explicit AM/PM or बजे suffix, an at/around cue, an alarm prefix, or a Hindi daypart cue.
- Relative durations such as "set alarm in 5 minutes" and "set alarm at 5 minutes" are rejected. Multiple explicit clock times are rejected as ambiguous instead of silently choosing the first.
- Added deterministic English/Hindi regression cases including 12 AM, afternoon/evening conversion, duration rejection and multiple-time ambiguity. CI and physical Redmi Note 9 alarm behavior remain pending.


## Constrained local conversation context (2026-11-16)

- Added a narrow in-memory follow-up resolver for explicit reopen phrases in English, Hindi and Hinglish. It remembers only the label of the last successfully launched app for the current Activity session.
- Phrases such as "open it again", "reopen it", "उसे फिर से खोलो", "वही ऐप खोलो" and "same app kholo" resolve to a normal app-launch command only when a valid recent app label exists.
- Unrelated instructions, destructive phrases, missing context and oversized labels are rejected. No generated text or arbitrary prior conversation is executed. Added JVM tests. Physical Redmi Note 9 verification and exact-head CI remain pending.


## All-command TTS microphone gate (2026-11-16)

- Found that app-launch commands were excluded from the TTS completion gate even though their launch helpers speak a confirmation. The handler therefore restarted the opted-in background KWS service before the spoken "Opening ..." response completed.
- Changed the lifecycle policy so every dispatched command defers KWS restart until its TTS completion callback (or the existing TTS-failure fallback). Added an exhaustive test over all `AssistantCommandType` values so new commands cannot silently bypass the gate.
- Physical microphone/TTS overlap verification on Redmi Note 9 and exact-head CI remain pending.
