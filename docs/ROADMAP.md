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
- [x] CI lint, unit tests, APK assembly, ARM JNI packaging checks for build #217.\n- [x] Build #237 CI passed with APK artifact tied to commit `f278858`; this artifact is retained for diagnostics, not final delivery.\n- [ ] Validate APK install/signature on Redmi Note 9; prior install attempt reported “App not installed” and Play Protect warning.\n- [ ] Verify new ARM-only package, APK signature, manifest package/minSdk and installability before final APK handoff.
- [ ] Install build #217 on Redmi Note 9 and record UI, launch, crash, permissions, model-install, and command test results.
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
- [ ] Research valid Android assistant role / VoiceInteractionService integration; do not register an invalid RecognitionService stub.

## Milestone 3 — Cross-app accessibility actions
- [ ] Implement a dedicated, clearly disclosed AccessibilityService with minimal event access and only required gesture capability.
- [ ] Provide an in-app explanation and button that opens Android Accessibility Settings; user must explicitly enable PK.
- [ ] Implement scroll up/down, page scroll, back, home, and media play/pause only through supported global actions/gestures.
- [ ] Add confirmation and app allowlist controls; never type/send messages or trigger payments without user confirmation.
- [ ] Detect service-disabled state and explain how to enable it.
- [ ] Test gestures in YouTube, WhatsApp, Chrome and Settings on the physical device.
- [ ] Respect secure windows, protected screens, and apps that block accessibility gestures.

## Milestone 4 — Phone, apps and productivity
- [x] Launch WhatsApp, YouTube, Chrome, Settings and Maps.
- [x] Contact lookup with exact-first matching and ambiguity chooser.
- [x] Call preparation through Android dialer; SMS composition with manual send.
- [ ] Expand installed-app discovery and safe launch aliases.
- [ ] Add user-confirmed WhatsApp share/deep-link composition; never silently send.
- [x] Parse English/Hindi alarm times and timer durations using Android AlarmClock intents (code + unit tests; CI verification pending for current revision; Redmi device confirmation pending).
- [ ] Add reminders/calendar only with explicit user-visible confirmation and suitable Android intents.
- [ ] Add media controls where Android media-session/platform APIs permit.
- [ ] Add explicit unsupported-action responses for commands that cannot be completed.

## Milestone 5 — Offline language and local intelligence
- [ ] Evaluate an Apache-2.0 or otherwise compatible Hindi/English offline speech recognition model for ARM64 and Redmi memory/CPU constraints.
- [ ] Keep wake-word recognition and command speech recognition as separate documented subsystems.
- [ ] Add deterministic Hindi, Hinglish and English intent coverage with unit tests.
- [ ] Add a lightweight local conversation context for follow-up commands; do not execute arbitrary generated code.
- [ ] Optional local LLM research only after device RAM, storage, latency, thermal and license evaluation; no paid API requirement.
- [ ] Add owner voice enrollment / speaker verification as a separate opt-in subsystem; evaluate false accepts, false rejects and replay attacks before describing it as secure.
- [ ] Keep time-aware greeting and time/date responses fully offline using the Android device clock.

## Milestone 6 — Quality, privacy and release
- [ ] Accessibility disclosure, privacy review and data minimization.
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
