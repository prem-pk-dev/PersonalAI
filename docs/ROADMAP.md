# PersonalAI Development Roadmap

## Working rules
- All development remains on `feature/sherpa-onnx-kws-spike`; do not modify `main/live`.
- Preserve the existing PK brand, command behavior, permissions rationale, and safe user-confirmed communication flows.
- Every feature must have unit tests where practical, CI build/lint, documented device test steps, and explicit status (implemented / CI verified / device verified).
- Do not claim device behavior until tested on the Redmi Note 9 (Android 12 / MIUI 13).
- No paid APIs or backend are required by default. Prefer Android platform APIs and open-source offline components.
- Android security restrictions must be respected. No hidden microphone capture, permission bypass, silent calls/messages, or lock-screen security bypass.

## Milestone 0 — Existing APK and device validation
- [x] Native Kotlin Android project, CI workflow, debug APK artifact.
- [x] Premium dark PK home UI.
- [x] WAKE_LOCK manifest permission added for the partial WakeLock implementation.
- [x] CI lint, unit tests, clean APK assembly, ARM JNI packaging checks and APK signature/package metadata verification.
- [x] Build #284 passed for exact branch head `acdd9fd9f800d2bda39ec47a36fd4787b3261712`; artifact `personalai-debug-apk-device-test` (ID `14204122024`, SHA-256 `c242bdf1c00c3931cbb1b882e4bbf645f98fdcd41f1b9ba1f4799736e9bd44a0`) is a device-test build only, not a final release.
- [x] Build #255 passed all workflow steps for commit `e3d05498fc28b6619e11ce137e7c02d1e7c480f9`; artifact uploaded for physical-device validation, not final release.
- [ ] Validate APK install/signature on Redmi Note 9; prior install attempt reported “App not installed” and Play Protect warning.
- [ ] Verify new ARM-only package, APK signature, manifest package/minSdk and installability before final APK handoff.
- [ ] Install a current branch build on Redmi Note 9 and record UI, launch, crash, permissions, model-install, and command test results.
- [ ] Validate wake-word false accepts/rejects and microphone lifecycle on device.

## Milestone 1 — Time-aware assistant behavior
- [x] Add offline local-device-clock greeting selection for morning / afternoon / evening / night.
- [x] Recognize Hindi and English greeting phrases and answer using the current device time and timezone.
- [x] Add unit tests for hour boundaries and greeting phrase classification.
- [ ] Verify TTS pronunciation and locale formatting on Redmi Note 9.
- [ ] Add robust date/time formatting and explicit unsupported response for unavailable speech providers.

## Milestone 2 — Reliable wake-word and command handoff
- [x] Sherpa-ONNX Android runtime and pinned English mobile KWS model installer.
- [x] User-started microphone foreground service, persistent notification, CPU partial WakeLock and stop action.
- [ ] Validate "PK" keyword tokenization, Indian accent performance, false accepts/rejects, and CPU/battery cost on device.
- [ ] Fix lifecycle so after wake detection the user can speak a command, then return to KWS without illegal background microphone-service starts.
- [ ] Support a robust one-utterance wake + command handoff where feasible.
- [ ] Add clear service states: model missing, permission missing, starting, listening, detected, error, stopped.
- [ ] Test background operation while WhatsApp, YouTube, Chrome and other apps are foreground.
- [ ] Test screen-off operation under Android 12 / MIUI 13; document OS/OEM limitations.
- [ ] Add a user-visible setting to resume the listener after reboot only where Android policy and foreground-start rules permit.
- [x] Add user-consented Android `ROLE_ASSISTANT` request flow and `ACTION_ASSIST` activity entry point; CI lint/build verified in #254.
- [ ] Implement a real `VoiceInteractionService` plus session service and metadata; validate eligibility on Android 12 / MIUI 13.
- [ ] Validate wake-to-command handoff, KWS tokenization, Indian accent performance, false accepts/rejects, CPU/battery cost and screen-off behavior on device.

## Milestone 3 — Cross-app accessibility actions
- [x] Implement a dedicated AccessibilityService with no event-content processing and limited scroll/back/home actions.
- [x] Provide in-app disclosure and a button that opens Android Accessibility Settings; user must explicitly enable PK.
- [x] Detect enabled/disabled service state when the app resumes and display its current status.
- [ ] Add app allowlist controls before expanding the action surface.
- [x] Add Hindi/English voice commands for play, pause, next track, and previous track through Android `MediaSessionManager` / `MediaController`; Notification Access is explicitly required, and unsupported/no-session cases are reported.
- [x] CI verification of active-media-session integration passed in Builds #283 and #284 (including clean build, lint, unit tests, ARM native packaging and APK metadata checks).
- [ ] Validate play/pause/next/previous against real media apps on Redmi Note 9; app-specific support and playback-state behavior remain device tests.
- [ ] Test scroll/back/home behavior in YouTube, WhatsApp, Chrome and Settings on the physical device.
- [ ] Respect secure windows, protected screens, and apps that block accessibility gestures.

## Milestone 4 — Phone, apps and productivity
- [x] Launch WhatsApp, YouTube, Chrome, Settings and Maps.
- [x] Generic installed-app discovery and safe launch for English/Hindi commands; parser unit tests, clean CI build, ARM JNI, APK signature/metadata and artifact upload passed in build #270 (commit `bf8b93c34d5ef8400f46ad5b0e181b22e3864f8b`). Physical Redmi app-launch verification remains pending.
- [x] Contact lookup with exact-first matching and ambiguity chooser.
- [x] Call preparation through Android dialer; SMS composition with manual send.
- [x] Add Hindi/English WhatsApp message-body extraction and user-confirmed Android share chooser; automated parser tests and CI build #264 passed. Physical WhatsApp handoff test remains pending; never silently send.
- [x] Parse English/Hindi alarm times and timer durations using Android AlarmClock intents (code + unit tests; current revision CI verified; Redmi device confirmation pending).
- [x] Add Calendar event/reminder composer intent with user review; date/time scheduling is completed in Calendar, not silently by PK.
- [x] Add media controls where Android media-session/platform APIs permit: play/pause/next/previous route to the active session; Notification Access is user-granted; no-session and unsupported-action responses are implemented and CI verified.
- [x] Add Hindi/English system media volume up/down/mute commands through Android `AudioManager` on `STREAM_MUSIC`, with visible system volume UI and parser unit tests; CI verification pending.
- [ ] Validate media volume behavior, mute semantics and OEM audio policy on Redmi Note 9; per-app volume is not generally exposed by MediaSession.
- [ ] Add explicit unsupported-action responses for commands that cannot be completed.

## Milestone 5 — Offline language and local intelligence
- [x] Integrate an optional offline Hindi IndicConformer int8 CTC recognizer using Sherpa-ONNX `OfflineRecognizer`; add explicit user-triggered model download (about 198 MB), pinned immutable model revision, SHA-256 verification, app-private storage, 16 kHz mono PCM capture, voice-activity timeout, and one-shot local transcription in the assistant flow. Builds #286 and #287 passed CI including clean build, unit tests, ARM native packaging, APK signature/metadata and artifact upload.
- [ ] Download and run the model on Redmi Note 9; benchmark transcription accuracy for Hindi/Hinglish, memory, CPU, latency, thermal and battery. The model is not bundled in the APK and has not yet been physically device-tested.
- [x] Keep KWS and one-shot Hindi command transcription as separate subsystems; the Hindi offline action stops KWS while it owns the microphone.
- [ ] Verify microphone handoff and listener resume on Redmi Note 9, including permission denial, cancellation, app backgrounding and low-memory paths.
- [ ] Expand deterministic Hindi, Hinglish and English intent coverage with unit tests; current offline recognizer integration is Hindi one-shot and does not yet provide robust Hinglish/English offline recognition.
- [ ] Add a lightweight local conversation context for follow-up commands; do not execute arbitrary generated code.
- [ ] Optional local LLM research only after device RAM, storage, latency, thermal and license evaluation; no paid API requirement.
- [ ] Add owner voice enrollment / speaker verification as a separate opt-in subsystem; evaluate false accepts, false rejects and replay attacks before describing it as secure.
- [ ] Keep time-aware greeting and time/date responses fully offline using the Android device clock.

## Milestone 6 — Quality, privacy and release
- [x] Accessibility disclosure and limited-scope data handling documented; full privacy review remains open.
- [ ] Regression tests for every existing command and permission-denied path.
- [ ] Battery, thermal, memory and long-running microphone tests.
- [ ] Physical-device test matrix and issue log.
- [ ] Signed release build and installation guide.
- [ ] Merge to main only after CI, code review, and physical-device acceptance.

## Release gates
1. CI lint, unit tests and APK build pass for the exact branch head.
2. APK artifact is present and tied to the exact commit SHA.
3. Device test results are recorded; never infer them from CI.
4. README, command contract and project memory are current.
5. Android and third-party app limitations are documented.


## Milestone 3 — KWS speech handoff correction (2026-11-16)
- [x] Add a testable KWS speech lifecycle policy so media and volume command TTS replies complete before the microphone listener resumes.
- [x] Add unit tests for deferred resume, opt-out behavior, and app-launch behavior.
- [ ] Verify the exact branch-head CI run for this change; do not mark tests/build as passed until the matching run is confirmed.


## Hindi offline audio preprocessing regression tests (2026-11-16)
- [x] Strengthen PCM16-to-float normalization, requested frame-length, invalid-input, and RMS unit coverage for the Hindi offline ASR preprocessing helpers.
- [ ] Verify these tests and the latest KWS lifecycle tests in GitHub Actions for the exact latest feature-branch head.
- [ ] Verify real microphone capture, VAD threshold, model inference and memory/latency on Redmi Note 9; unit tests do not replace device validation.


## Hindi offline model installation safety (2026-11-16)
- [x] Stage and verify the Hindi model before promoting it over an existing installation.
- [x] Preserve the previous installed model until the new directory is promoted; restore the previous directory if promotion fails.
- [x] Recover a previous-model backup after an interrupted promotion.
- [x] Add JVM tests for replacement, empty destination, missing staging, and interrupted-promotion recovery.
- [ ] Verify the new tests and clean APK build in CI for the exact branch head.
- [ ] Validate interrupted download/install behavior and available storage on Redmi Note 9.


## KWS capture failure shutdown (2026-11-16)
- [x] Distinguish an unexpected capture failure from an intentional user stop.
- [x] On unexpected failure, release audio/native resources and the CPU WakeLock, clear the persisted listener opt-in, publish a non-ongoing error notification, detach foreground state, and stop the failed service on the main thread.
- [ ] Verify service restart/error notification behavior and permission-revocation paths on Redmi Note 9 / Android 12 MIUI 13.
- [ ] Verify exact branch-head lint, unit tests, and APK assembly in GitHub Actions.


## Wake-to-command failure recovery (2026-11-16)
- [x] Add a testable policy for restoring an opted-in KWS listener after a wake-to-command speech-recognition failure, but only when microphone permission remains granted.
- [x] Restore KWS after TTS reports speech recognition unavailable or SpeechRecognizer startup throws.
- [x] After microphone permission is granted, defer KWS restart until the permission response finishes speaking; clear stale KWS opt-in when permission is denied.
- [x] Add regression coverage for opt-in and microphone-permission combinations.
- [ ] Verify wake detection, permission prompts, SpeechRecognizer failures, and listener restart on Redmi Note 9 / Android 12 MIUI 13.
- [ ] Verify exact branch-head CI and APK assembly.


## Hindi offline listening cancellation (2026-11-16)
- [x] Add an explicit Cancel control while one-shot Hindi offline listening is active.
- [x] Stop microphone capture on cancellation, check cancellation before/after native transcription, and prevent dispatch of a cancelled result.
- [x] Restore an opted-in KWS listener after a user cancellation or failed one-shot capture, after microphone resources are released; do not restart from a destroyed Activity.
- [x] Close the cancel window before posting a completed transcript to avoid a cancel/dispatch race.
- [ ] Verify cancellation during capture and transcription, microphone release, and KWS restart on Redmi Note 9.
- [ ] Verify exact branch-head CI, lint, unit tests and APK assembly.


## TTS failure recovery for KWS handoff (2026-11-16)
- [x] Resume an opted-in KWS listener if Android TextToSpeech is not initialized when a response is requested.
- [x] Resume after TextToSpeech initialization reports failure.
- [x] Gate recovery on the pending restart request, current KWS opt-in, and microphone permission; re-check permission/opt-in before delayed service start.
- [x] Add JVM regression tests for TTS-failure recovery policy.
- [ ] Verify no microphone overlap and correct listener restart with Android TTS disabled / voice data missing on Redmi Note 9.
- [ ] Verify exact branch-head CI and APK assembly.


## SpeechRecognizer retry-loop protection (2026-11-16)
- [x] Replace unbounded wake-mode SpeechRecognizer error restarts with a finite four-attempt retry policy (700 ms, 1.4 s, 2.8 s, 5 s).
- [x] Stop recognition safely after repeated provider failures, show a visible status, and restore an opted-in KWS listener after a failed wake-to-command handoff.
- [x] Reset the consecutive-failure counter for a new listening session and after a successful recognition result; delayed retries re-check Activity and listening state.
- [x] Add JVM tests for backoff sequence, retry exhaustion, and invalid failure counts.
- [ ] Verify SpeechRecognizer error codes, recovery and KWS handoff on Redmi Note 9 / Android 12 MIUI 13.
- [ ] Verify exact branch-head lint, unit tests and APK assembly in GitHub Actions.


## Accessibility scroll app allowlist (2026-11-16)
- [x] Add an explicit user-managed allowlist for scroll gestures in supported installed apps (YouTube, WhatsApp, WhatsApp Business, Chrome).
- [x] Deny scroll gestures by default when no app is selected; enforce the allowlist against the active window package before traversing scrollable nodes.
- [x] Keep Back/Home as explicit global navigation actions and disclose the distinction in the Accessibility setup prompt.
- [x] Add unit tests for default-deny, selected-app allow, unselected-app deny, and missing active package.
- [ ] Verify app switching, package identification, scroll gestures and Back/Home on Redmi Note 9 / Android 12 MIUI 13.
- [ ] Verify exact branch-head CI, lint, unit tests and APK assembly.


## Wake acknowledgement microphone handoff (2026-11-16)
- [x] Prevent the command recognizer from restarting immediately after hearing the wake-only phrase "PK" while the spoken acknowledgement is still playing.
- [x] Resume one-shot command recognition after the TTS completion callback; use the same guarded path when TTS is unavailable or fails.
- [x] Add a testable policy for restarting recognition only while wake mode and awaiting-command state are both active.
- [ ] Verify wake phrase, acknowledgement audio, command capture and microphone non-overlap on Redmi Note 9 / Android 12 MIUI 13.
- [ ] Verify exact branch-head CI and APK assembly.
