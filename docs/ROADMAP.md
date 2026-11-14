# PersonalAI Development Roadmap

## Milestone 0 — Repository foundation
- [x] Create native Android Kotlin project.
- [x] Add Android manifest and Gradle configuration.
- [x] Add initial voice/text Activity.
- [x] Add GitHub Actions Android build workflow.
- [x] Build debug APK successfully on earlier verified commits.
- [x] Upload debug APK as a workflow artifact on a successful run.
- [ ] Verify tests, green CI, and APK artifact for the latest contact-matching branch head.
- [ ] Validate install and launch on a physical Android 13–16 device.

## Milestone 1 — Reliable MVP command engine
- [x] Text command input and visible Run Text Command button.
- [x] Siri-inspired dark assistant UI with animated voice orb and tap-to-listen control.\n- [x] Request Android ROLE_ASSISTANT with user consent and handle ACTION_ASSIST launch.
- [x] Android speech recognition integration.
- [x] Text-to-speech response integration.
- [x] Start/stop visible listening controls.
- [x] Wake phrase + command in one utterance.
- [x] Wake phrase and command in two successive utterances.
- [x] Persisted Hindi/English speech-language selector.
- [x] Deterministic command parser extracted into a standalone Kotlin class.
- [x] JUnit tests for Hindi/English command classification.
- [x] Exact-first contact matcher with normalized phone-number deduplication.
- [x] User chooser for multiple equally ranked contact matches.
- [x] Contact matcher unit tests for exact, partial, ambiguous, duplicate, and empty cases.
- [x] Launch WhatsApp, YouTube, Chrome, Settings.
- [x] Contact lookup and system dialer confirmation.
- [x] SMS composer for a phone number.
- [x] Optional SMS body prefill for user review.
- [x] Open alarm screen.
- [x] Support contact-name SMS composition with shared ambiguity-safe contact matching and manual-send composer handoff.
- [ ] Improve error states, permission rationale, and command history persistence.

## Milestone 2 — Background wake phrase
- [ ] Implement VoiceInteractionService and VoiceInteractionSessionService for system-level assistant sessions and supported hotword behavior.\n- [ ] User-started microphone foreground service.
- [ ] Android 13 notification permission handling.
- [ ] Correct foreground service type and microphone permissions for supported API levels.
- [ ] Persistent, clear listening notification with stop action.
- [ ] Battery and OEM restriction guidance.
- [ ] Test screen-off behavior on Android 13, 14, 15, and 16.
- [ ] Evaluate offline wake-word detection engine and document tradeoffs.

## Milestone 3 — Phone actions
- [ ] Open contact detail and dialer reliably.
- [ ] WhatsApp supported share/deep-link flows with user confirmation.
- [ ] Alarm/timer creation with time parsing.
- [ ] App discovery and launching beyond a fixed package list.
- [ ] Settings shortcuts where public Android intents exist.
- [ ] Explicitly unsupported-action responses.

## Milestone 4 — AI command understanding
- [ ] Local deterministic intent parser first.
- [ ] Optional free/local AI model research; no paid API requirement.
- [ ] Action allowlist and confirmation policy.
- [ ] Conversation context and short-term session memory.
- [ ] Graceful offline fallback.

## Milestone 5 — Advanced telephony and release
- [ ] Research compliant default-dialer role and Telecom APIs.
- [ ] Implement cellular call controls only if the app qualifies and the user opts in.
- [ ] Keep WhatsApp calling control marked unsupported unless a documented public interface exists.
- [ ] Accessibility and privacy review.
- [ ] Automated tests and manual device test matrix.
- [ ] Signed release build and installation guide.

## Release gates
Do not merge a milestone to `main` until:
1. CI tests and build pass for the exact head commit.
2. Device test results are recorded.
3. README and project memory are updated.
4. Known Android/WhatsApp limitations are clearly documented.
