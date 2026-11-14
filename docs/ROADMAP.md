# PersonalAI Development Roadmap

## Milestone 0 — Repository foundation
- [x] Native Kotlin Android project, Gradle configuration, and CI workflow.
- [x] Debug APK build and artifact upload on successful CI.
- [x] Verify unit tests, CI, and APK artifact for current tested commit.
- [ ] Install and validate on a physical Android 12–16 device.

## Milestone 1 — Reliable MVP command engine
- [x] PK-branded dark UI, custom adaptive launcher icon, animated orb, tap-to-listen, transcript, and text composer.
- [x] Android SpeechRecognizer and TextToSpeech integration.
- [x] Visible start/stop listening controls.
- [x] PK wake phrase + command in one utterance and two-step utterances.
- [x] Hindi speech-to-text variants पीके / पी के.
- [x] Automatic response-language selection from the recognized transcript script (Hindi Devanagari → hi-IN; Latin → en-IN).
- [x] Enable Android 14+ recognizer language-detection callback constrained to `en-IN` and `hi-IN` where supported.
- [x] Preserve a supported Android recognizer language result ahead of transcript heuristics; fall back safely for missing/unsupported tags, with unit-test coverage.
- [ ] Verify Android 12 fallback behavior and mixed Hindi-English (Hinglish) on Redmi Note 9; provider support remains device-dependent.
- [x] Deterministic command parser and JUnit tests.
- [x] Exact-first contact matcher, duplicate-number normalization, and ambiguity chooser.
- [x] Launch WhatsApp, YouTube, Chrome, and Android Settings.
- [x] Contact dialing via system dialer confirmation.
- [x] Contact-name and number SMS composer with optional body and manual send.
- [x] Add contextual microphone/contacts permission rationale and explicit denial feedback.
- [x] Refresh assistant home UI with an animated orb and compact command composer; Quick Actions and Recent Conversation are intentionally hidden.
- [x] Add offline time/date/battery responses, Maps launch, and help command with parser tests.

## Milestone 2 — Android assistant integration and wake-word research
- [x] ACTION_ASSIST activity entry point.
- [ ] Implement or select a valid RecognitionService provider compatible with the supported Android range.
- [ ] Register VoiceInteractionService metadata only after recognitionService and sessionService contracts are satisfied.
- [ ] Restore user-consented ROLE_ASSISTANT request after provider integration and Android 13–16 device verification.
- [ ] Integrate sherpa-onnx open-vocabulary KWS (Apache-2.0 code) with an English model/custom `PK` keyword; validate model licence, size, CPU/battery cost, and Hindi transcript variants.
- [ ] Confirm keyphrase enrollment and SoundTrigger/DSP support on target devices.
- [ ] Implement user-started microphone foreground service backed by KWS AudioRecord (not continuous SpeechRecognizer) after model integration and Android lifecycle review.
- [ ] Test screen-off behavior on physical Android 12 MIUI 13 (Redmi Note 9) and additional supported Android versions.
- [ ] Add local speaker verification enrollment and matching; validate false accepts/rejects and replay limitations.
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
