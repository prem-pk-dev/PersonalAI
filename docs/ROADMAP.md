# PersonalAI Development Roadmap

## Milestone 0 — Repository foundation
- [x] Native Kotlin Android project, Gradle configuration, and CI workflow.
- [x] Debug APK build and artifact upload on successful CI.
- [x] Verify unit tests, CI, and APK artifact for current tested commit.
- [ ] Install and validate on a physical Android 13–16 device.

## Milestone 1 — Reliable MVP command engine
- [x] Siri-inspired dark UI, animated orb, tap-to-listen, transcript, and text composer.
- [x] Android SpeechRecognizer and TextToSpeech integration.
- [x] Visible start/stop listening controls.
- [x] PK wake phrase + command in one utterance and two-step utterances.
- [x] Hindi speech-to-text variants पीके / पी के.
- [x] Persisted English (India) / Hindi (India) language selector.
- [x] Deterministic command parser and JUnit tests.
- [x] Exact-first contact matcher, duplicate-number normalization, and ambiguity chooser.
- [x] Launch WhatsApp, YouTube, Chrome, and Android Settings.
- [x] Contact dialing via system dialer confirmation.
- [x] Contact-name and number SMS composer with optional body and manual send.
- [ ] Improve permission rationale, error states, and persistent command history.

## Milestone 2 — Android assistant integration and wake-word research
- [x] Register VoiceInteractionService with Android binding permission and service metadata.
- [x] Register VoiceInteractionSessionService and a minimal assistant session surface.
- [x] Assistant session can open the full PersonalAI app.
- [x] ACTION_ASSIST activity entry point and user-consented ROLE_ASSISTANT request.
- [ ] Select and integrate a compatible PK hotword engine with a documented licence and offline model.
- [ ] Confirm keyphrase enrollment and SoundTrigger/DSP support on target devices.
- [ ] Implement foreground microphone listening only after the detection engine and Android lifecycle design are validated.
- [ ] Test screen-off behavior on physical Android 13, 14, 15, and 16 devices.
- [ ] Document battery and OEM restrictions from real device testing.

## Milestone 3 — Phone actions
- [ ] Broader app discovery and launching.
- [ ] Supported WhatsApp share/deep-link flows with user confirmation.
- [ ] Alarm/timer creation and time parsing (existing alarm shortcut remains separately controlled).
- [ ] Explicit unsupported-action responses.

## Milestone 4 — Local intelligence
- [ ] Expand deterministic Hindi/English intent coverage.
- [ ] Optional free/local AI model research; no paid API requirement.
- [ ] Action allowlist, confirmation policy, short-term conversation context, and offline fallback.

## Milestone 5 — Release
- [ ] Accessibility and privacy review.
- [ ] Physical-device test matrix and regression tests.
- [ ] Signed release build and installation guide.
- [ ] Merge to main only after CI, review, and device testing.

## Release gates
1. CI tests and build pass for the exact branch head.
2. APK artifact is verified.
3. Device test results are recorded; do not claim device validation before it occurs.
4. README and project memory are current.
5. Android and third-party app limitations are documented.
