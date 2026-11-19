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
- [x] Format spoken time/date using the detected response language and the device timezone, with deterministic locale/timezone regression tests.
- [ ] Provide an explicit spoken response when the requested Android speech-recognition provider is unavailable (the current startup status message is visual only).

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
- [x] Verify exact branch-head CI and APK assembly (Build #439, exact head `8f228f62f797da4432143f8e9b5be506cde55bf3`; clean rebuild, tests, ARM native packaging, APK signature/metadata and artifact upload passed).


## Hindi offline listening cancellation (2026-11-16)
- [x] Add an explicit Cancel control while one-shot Hindi offline listening is active.
- [x] Stop microphone capture on cancellation, check cancellation before/after native transcription, and prevent dispatch of a cancelled result.
- [x] Restore an opted-in KWS listener after a user cancellation or failed one-shot capture, after microphone resources are released; do not restart from a destroyed Activity.
- [x] Close the cancel window before posting a completed transcript to avoid a cancel/dispatch race.
- [ ] Verify cancellation during capture and transcription, microphone release, and KWS restart on Redmi Note 9.
- [x] Verify exact branch-head CI, lint, unit tests and APK assembly (Build #439, exact head `8f228f62f797da4432143f8e9b5be506cde55bf3`; clean rebuild, tests, ARM native packaging, APK signature/metadata and artifact upload passed).


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
- [x] Verify exact branch-head lint, unit tests and APK assembly in GitHub Actions (Build #439, exact head `8f228f62f797da4432143f8e9b5be506cde55bf3`; clean rebuild, tests, ARM native packaging, APK signature/metadata and artifact upload passed).


## Accessibility scroll app allowlist (2026-11-16)
- [x] Add an explicit user-managed allowlist for scroll gestures in supported installed apps (YouTube, WhatsApp, WhatsApp Business, Chrome).
- [x] Deny scroll gestures by default when no app is selected; enforce the allowlist against the active window package before traversing scrollable nodes.
- [x] Keep Back/Home as explicit global navigation actions and disclose the distinction in the Accessibility setup prompt.
- [x] Add unit tests for default-deny, selected-app allow, unselected-app deny, and missing active package.
- [ ] Verify app switching, package identification, scroll gestures and Back/Home on Redmi Note 9 / Android 12 MIUI 13.
- [x] Verify exact branch-head CI, lint, unit tests and APK assembly (Build #439, exact head `8f228f62f797da4432143f8e9b5be506cde55bf3`; clean rebuild, tests, ARM native packaging, APK signature/metadata and artifact upload passed).


## Wake acknowledgement microphone handoff (2026-11-16)
- [x] Prevent the command recognizer from restarting immediately after hearing the wake-only phrase "PK" while the spoken acknowledgement is still playing.
- [x] Resume one-shot command recognition after the TTS completion callback; use the same guarded path when TTS is unavailable or fails.
- [x] Add a testable policy for restarting recognition only while wake mode and awaiting-command state are both active.
- [ ] Verify wake phrase, acknowledgement audio, command capture and microphone non-overlap on Redmi Note 9 / Android 12 MIUI 13.
- [ ] Verify exact branch-head CI and APK assembly.


## KWS foreground notification persistence (2026-11-16)
- [x] Mark the foreground notification ongoing while the user-started KWS microphone listener is active.
- [x] Keep detected and error notifications dismissible/non-ongoing after capture has stopped.
- [ ] Verify notification visibility, stop action and dismissal behavior on Redmi Note 9 / Android 12 MIUI 13.
- [ ] Verify exact branch-head CI and APK assembly.


## Alarm time parser ambiguity guard (2026-11-16)
- [x] Require an explicit time cue (AM/PM, बजे, at/around, alarm prefix, or Hindi daypart) instead of treating any number as an alarm time.
- [x] Reject relative duration phrases such as "set alarm in 5 minutes" rather than interpreting the duration as a wall-clock time.
- [x] Reject multiple explicit clock times rather than silently selecting the first.
- [x] Add English/Hindi regression tests for explicit cues, duration rejection, ambiguity, 12 AM and daypart conversion.
- [ ] Verify exact branch-head lint, unit tests and APK assembly in GitHub Actions.
- [ ] Verify alarm launch and spoken time interpretation on Redmi Note 9 / Android 12 MIUI 13.


## Constrained local conversation context (2026-11-16)
- [x] Remember the label and exact package name of the last successfully launched app in Activity memory only.
- [x] Resolve explicit English, Hindi and Hinglish "open it again / वही ऐप खोलो" follow-ups using the exact package identity, avoiding confusion between similarly named apps.
- [x] Reject follow-up resolution without a valid recent package name or for unrelated free-form requests; never execute generated instructions.
- [x] Add unit tests for supported phrases, exact package resolution, missing/invalid context and unrelated action rejection.
- [ ] Verify follow-up app relaunch and context lifecycle on Redmi Note 9 / Android 12 MIUI 13.
- [x] Verify exact branch-head CI, lint, unit tests and APK assembly (Build #439, exact head `8f228f62f797da4432143f8e9b5be506cde55bf3`; clean rebuild, tests, ARM native packaging, APK signature/metadata and artifact upload passed).


## All-command TTS microphone gate (2026-11-16)
- [x] Keep the opted-in KWS microphone listener stopped for every dispatched command until its spoken confirmation/error finishes.
- [x] Remove the previous app-launch exception, which could restart KWS before the "Opening app" TTS response completed.
- [x] Add an exhaustive enum-based unit test so newly added command types also require the TTS handoff.
- [ ] Verify microphone non-overlap for app launch, media, navigation, alarm and error responses on Redmi Note 9.
- [x] Verify exact branch-head CI, lint, unit tests and APK assembly (Build #439, exact head `8f228f62f797da4432143f8e9b5be506cde55bf3`; clean rebuild, tests, ARM native packaging, APK signature/metadata and artifact upload passed).


## Timer duration ambiguity guard (2026-11-16)
- [x] Require exactly one numeric duration component; reject multi-part inputs such as "5 minutes and 10 seconds" rather than silently ignoring later values.
- [x] Parse explicit English/Hindi hour, minute and second units; preserve the existing single-number default-to-minutes behavior.
- [x] Reject zero, overflow and durations above 24 hours with regression tests.
- [x] Verify exact branch-head CI, lint, unit tests and APK assembly (Build #439, exact head `8f228f62f797da4432143f8e9b5be506cde55bf3`; clean rebuild, tests, ARM native packaging, APK signature/metadata and artifact upload passed).
- [ ] Verify timer handoff and confirmation on Redmi Note 9 / Android 12 MIUI 13.


## Romanized Hinglish intent coverage (2026-11-16)
- [x] Add explicit Romanized-Hinglish aliases for common media playback, volume, scroll/navigation, time and date commands.
- [x] Add deterministic unit tests for the new aliases; keep matching finite and intent-based rather than implying general Hinglish understanding.
- [x] Document the tested examples and their bounded behavior in the command contract.
- [x] Verify the exact branch-head unit tests, lint and clean APK assembly in GitHub Actions (run #393, commit `a078a10958cd52cbd537f224b0894166d5967309`).
- [ ] Validate recognition transcript quality and command execution on Redmi Note 9; parser tests do not validate microphone ASR accuracy.


## Help command capability accuracy (2026-11-16)
- [x] Expand spoken English/Hindi help to cover implemented app, communication, media, volume, accessibility, alarm/timer, reminder and device-information actions.
- [x] State that Android permissions and target-app support can limit some controls.
- [x] Align the command contract documentation with the spoken help response.
- [x] Verify exact branch-head CI, unit tests, lint and clean APK assembly (run #393, commit `a078a10958cd52cbd537f224b0894166d5967309`).
- [ ] Review spoken Hindi/English clarity and discoverability on Redmi Note 9.


## KWS startup permission gate (2026-11-16)
- [x] Centralize microphone, model-installation and Android 13+ notification permission prerequisites in a deterministic startup gate.
- [x] Add JVM regression tests for missing microphone permission, missing model, Android 12 notification-permission exemption, Android 13+ notification requirement, and ready state.
- [x] Verify exact branch-head lint, unit tests and clean APK assembly in GitHub Actions (Build #439, exact head `8f228f62f797da4432143f8e9b5be506cde55bf3`; clean rebuild, tests, ARM native packaging, APK signature/metadata and artifact upload passed).
- [ ] Verify permission grant/denial and KWS startup behavior on Redmi Note 9 / Android 12 MIUI 13.


## Bounded Hindi PCM capture for Redmi Note 9 (2026-11-16)
- [x] Replace boxed per-sample PCM accumulation with a fixed-capacity primitive ShortArray buffer.
- [x] Hard-cap one-shot Hindi capture to nine seconds and stop reading when the buffer is full.
- [x] Add JVM tests for frame ordering, partial frames, capacity truncation, defensive snapshots and invalid input.
- [ ] Verify exact branch-head lint, unit tests and clean APK assembly in GitHub Actions.
- [ ] Measure Hindi ASR peak memory, latency and thermal behavior on Redmi Note 9; this code change alone is not a device benchmark.


## Hindi ASR input validation (2026-11-16)
- [x] Validate 16 kHz mono input, non-empty audio, finite samples and normalized PCM range before passing audio to Sherpa-ONNX.
- [x] Add JVM regression tests for valid input, incorrect sample rate, empty audio, NaN/infinity and out-of-range values.
- [x] Verify exact branch-head unit tests, lint and clean APK assembly in GitHub Actions (Build #439, exact head `8f228f62f797da4432143f8e9b5be506cde55bf3`; clean rebuild, tests, ARM native packaging, APK signature/metadata and artifact upload passed).
- [ ] Confirm real microphone input and Hindi transcription quality on Redmi Note 9.

## Independent native audio resource cleanup (2026-11-16)
- [x] Guard AudioRecord, Sherpa stream and keyword spotter releases independently so one native cleanup exception cannot skip later releases or WakeLock cleanup.
- [ ] Verify exact branch-head unit tests, lint and clean APK assembly in GitHub Actions.
- [ ] Exercise KWS stop, wake detection and forced service shutdown on Redmi Note 9 / Android 12 MIUI 13.

## Testable independent native cleanup (2026-11-16)
- [x] Extract independently guarded JNI cleanup actions into a small reusable helper used by KWS service teardown.
- [x] Add regression tests proving a failed release does not skip later AudioRecord, Sherpa stream or spotter cleanup actions.
- [ ] Verify exact branch-head unit tests, lint and clean APK assembly in GitHub Actions.
- [ ] Validate KWS stop/wake-detection teardown on Redmi Note 9 / Android 12 MIUI 13.

## Just-in-time microphone permission request (2026-11-16)
- [x] Remove the unconditional microphone permission prompt from initial Activity launch; voice features continue to request permission at their point of use, and explicit Android ASSIST launches retain the voice-start path.
- [x] Verify exact branch-head lint, unit tests and clean APK assembly in GitHub Actions (run #414, exact commit `9240b409a8f6cbd4fbc8ab076ef02a70f6d4fcb8`; clean build/test/rebuild, ARM native packaging and APK signature/metadata checks all passed).
- [ ] Verify first launch without granting microphone permission, then permission grant/denial and voice-feature startup on Redmi Note 9 / Android 12 MIUI 13.


## Wi-Fi and Bluetooth settings voice intents (2026-11-16)
- [x] Add explicit English/Hindi parser intents for opening Wi-Fi and Bluetooth settings screens.
- [x] Route these intents to Android's system settings screens; PK does not silently toggle radios or bypass user confirmation.
- [x] Add deterministic parser regression tests and document the supported phrases.
- [ ] Verify exact branch-head CI, lint, unit tests and clean APK assembly.
- [ ] Verify settings screens open correctly on Redmi Note 9 / Android 12 MIUI 13; user performs any radio change.

## Voice orb listening-state animation (2026-11-16)
- [x] Keep the orb animation stopped in the idle/ready state; render a static orb without a continuously running animator.
- [x] Start pulse and rotating ring animation only when the app enters an explicit listening state; stop and reset it when listening ends.
- [x] Stop animation during speech-provider retry gaps and when recognition startup throws an exception.
- [x] Verify exact branch-head CI, lint, unit tests, and clean APK assembly for this change (run #427, exact commit `45f46f6e20e262f4e3b8983bb0f075d3eefac50b`; clean rebuild, tests, ARM native packaging, APK signature/metadata and artifact upload passed).
- [ ] Verify idle stability, Start Listening animation, Stop Listening reset, and permission-denied behavior on Redmi Note 9 / Android 12 MIUI 13.


## Mute and Romanized-Hindi alarm command fixes (2026-11-17)
- [x] Add Romanized-Hindi mute aliases including `mute karo`, `volume mute karo`, and `awaz mute karo`.
- [x] Add Romanized-Hindi alarm setup aliases including `lagao` and `laga do`; ask for a time when missing and open Clock for confirmation when time is supplied.
- [x] Add parser regression tests and update `docs/COMMANDS.md`.
- [x] Verify clean tests, lint/build, ARM native packaging, APK signature/metadata and artifact upload on exact branch head `8f228f62f797da4432143f8e9b5be506cde55bf3` (Build #439).
- [ ] Physical Redmi Note 9 validation remains pending; CI does not establish device behavior.

## Premium usability — one-tap quick actions (2026-11-17)
- [x] Add a compact QUICK ACTIONS panel to the home screen for Time, Battery, YouTube, WhatsApp, Settings and Help.
- [x] Route each shortcut through the same deterministic command handler used by typed and recognized voice commands; do not create a separate action path.
- [ ] Verify exact-head CI, lint, unit tests, APK assembly and artifact upload.
- [ ] Check tap targets, layout and command feedback on Redmi Note 9 / Android 12 MIUI 13.

## Premium usability — bilingual command guide (2026-11-17)
- [x] Replace the spoken-only Help response with a readable English/Hindi command guide.
- [x] Group examples by apps, user-confirmed communication, phone/media controls and daily tools.
- [x] Explain wake phrase and permission/compatible-app limitations in the guide.
- [ ] Verify exact-head CI, lint, unit tests, APK assembly and artifact upload.
- [ ] Review readability and dialog sizing on Redmi Note 9 / Android 12 MIUI 13.

## Premium usability — keyboard command submission (2026-11-17)
- [x] Support the Android keyboard Send/Done action in the text composer.
- [x] Route keyboard and on-screen send actions through one submission method and the existing command handler.
- [ ] Verify exact-head CI, lint, unit tests, APK assembly and artifact upload.
- [ ] Review keyboard behavior and focus handling on Redmi Note 9 / Android 12 MIUI 13.


## Local conversation context — safer app follow-ups (2026-11-17)
- [x] Expand explicit English, Hindi and Romanized-Hindi phrases for reopening the last successfully launched app.
- [x] Keep follow-up resolution restricted to a fixed allowlist of phrases and the exact validated package name from the current Activity session.
- [x] Add JVM regression cases for the new aliases; arbitrary message, delete and send phrases remain non-actions.
- [ ] Verify exact branch-head CI, lint, unit tests and clean APK assembly.
- [ ] Verify follow-up behavior after app switching, Activity recreation and process restart on Redmi Note 9; recent app context is intentionally session-only.


## Expanded natural app-launch phrasing (2026-11-17)
- [x] Accept additional English app-launch verbs such as show/find and polite open requests.
- [x] Accept common Romanized-Hindi and Devanagari launch endings such as khol do, dikhao, खोल दो and दिखाओ.
- [x] Keep app resolution based on installed launcher labels; do not claim an app exists when it is not installed.
- [x] Add parser and app-name extraction regression cases.
- [ ] Verify exact branch-head CI, lint, unit tests and clean APK assembly.
- [ ] Verify app label matching and chooser behavior on Redmi Note 9 / Android 12 MIUI 13.


## Command discovery and recovery (2026-11-17)
- [x] Replace the abrupt unsupported-command response with a bilingual capability summary and a rephrase/Help suggestion.
- [x] Keep the response honest: it does not claim arbitrary natural-language execution or unsupported features.
- [ ] Verify exact branch-head CI, lint, unit tests and clean APK assembly.
- [ ] Review speech clarity and recovery flow on Redmi Note 9 / Android 12 MIUI 13.


## Everyday command phrase coverage (2026-11-17)
- [x] Expand time, date, battery, call and reminder phrase aliases in English, Hindi and Romanized Hindi.
- [x] Add deterministic parser regression tests for representative new phrases.
- [x] Update the command contract with discoverable examples.
- [ ] Verify exact branch-head CI, lint, unit tests and clean APK assembly.
- [ ] Validate speech-recognition transcription variants and command execution on Redmi Note 9 / Android 12 MIUI 13.

## Expanded natural everyday phrases (2026-11-17)
- [x] Add common conversational English and Romanized-Hindi variants for time, date, battery, volume, and media playback intents.
- [x] Add deterministic parser regression tests for the newly supported phrases.
- [x] Update the user-facing command contract with representative examples.
- [ ] Verify exact branch-head CI, lint, unit tests and clean APK assembly.
- [ ] Validate speech transcripts and actual command execution on Redmi Note 9 / Android 12 MIUI 13.

## Background PK wake-to-command handoff clarity (2026-11-17)
- [x] Review the actual KWS service lifecycle and notification PendingIntent path against the user-facing command contract.
- [x] Correct the command contract: background KWS detects PK, posts a notification, stops capture, and requires the user to tap before SpeechRecognizer captures the command.
- [x] Preserve the explicit user-started foreground microphone service and notification stop control; do not attempt a policy-bypassing background Activity launch.
- [x] Verify exact feature-branch head in Build #484: lint, unit tests, clean APK build, ARM native packaging, APK metadata/signature and artifact upload all passed. Physical Redmi Note 9 validation remains pending.
- [ ] Verify notification tap-to-command handoff, command execution, and KWS resume/stop on Redmi Note 9 / Android 12 MIUI 13.
- [ ] Seamless hands-free background wake-to-command remains out of scope until a supported always-on command-recognition design is implemented and device-tested.


## PK listener notification interruption fix (2026-11-17)
- [x] Lower the persistent foreground-listener notification channel from HIGH to LOW importance to prevent unnecessary heads-up interruption.
- [x] Bump the channel ID to v3 so Android creates the corrected channel instead of retaining the user's existing immutable v2 channel settings.
- [ ] Verify notification visibility, Stop action, and wake-detected notification tap on Android 12 / MIUI 13.
- [ ] Verify branch-head CI, lint, JVM tests, clean APK build, ARM native packaging, APK metadata/signature and artifact upload.


## Latest branch-head verification — 2026-11-18

- [x] Android CI Build #485 passed for exact branch head `a9e8b68bab854391a47b16ad935c933063dfba86`: clean lint, unit tests, debug APK assembly, ARM JNI packaging, APK signature and metadata checks, and artifact upload.
- [x] KWS Runtime Verification run #8 passed for the same commit; official sherpa-onnx v2.0.1 AAR checksum and ARM JNI payload checks passed.
- [x] Debug APK artifact `personalai-debug-apk-device-test` uploaded (ID `14443606143`, SHA-256 `84325e63618b79cf9f9d3b5cdca89ea5787087e1840dc379eecb7c99dbf52e05`; expires 2026-12-02).
- [ ] Physical Redmi Note 9 / Android 12 / MIUI 13 acceptance remains outstanding. CI does not validate microphone capture, notification handoff, actual KWS accuracy, screen-off behavior, or OEM battery restrictions.
- [ ] Continue to keep the APK classified as a debug/device-test artifact, not a release APK.


## Latest branch-head verification — 2026-11-19

- [x] Android CI Build #496 passed for exact feature branch head `624059a24a765ff8d21eb1f5cc007cd09f16429d`: clean lint, JVM unit tests, clean debug APK assembly, ARM JNI packaging, APK signature verification, and package metadata checks.
- [ ] The persistently signed device-test APK upload was skipped because the four repository signing secrets are not configured (see `docs/ANDROID_APK_SIGNING.md`). No artifact was produced by Build #496.
- [ ] Configure `PK_SIGNING_KEYSTORE_BASE64`, `PK_SIGNING_STORE_PASSWORD`, `PK_SIGNING_KEY_ALIAS`, and `PK_SIGNING_KEY_PASSWORD` as GitHub Actions repository secrets, then rerun Android CI to produce the update-compatible device-test APK.
- [ ] Physical Redmi Note 9 / Android 12 / MIUI 13 validation remains outstanding; CI does not validate microphone capture, notification handoff, KWS accuracy, screen-off behavior, or OEM battery restrictions.
- [ ] Keep this work on `feature/sherpa-onnx-kws-spike`; do not merge to `main` before device acceptance and review. The current `main` branch does not yet contain the Android app implementation.
- [ ] Keep all APKs classified as debug/device-test builds, not production releases.
