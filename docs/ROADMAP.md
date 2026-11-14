# PersonalAI Development Roadmap

## Milestone 0 — Repository foundation
- [x] Create native Android Kotlin project.
- [x] Add Android manifest and Gradle configuration.
- [x] Add initial voice/text Activity.
- [x] Add GitHub Actions Android build workflow.
- [ ] Verify green CI after Kotlin compile fix.
- [ ] Upload debug APK as a workflow artifact.
- [ ] Validate install and launch on a physical Android 13–16 device.

## Milestone 1 — Reliable MVP command engine
- [x] Text command input.
- [x] Android speech recognition integration.
- [x] Text-to-speech response integration.
- [x] Start/stop visible listening controls.
- [x] Wake phrase + command in one utterance.
- [x] Wake phrase and command in two successive utterances.
- [x] Launch WhatsApp, YouTube, Chrome, Settings.
- [x] Contact lookup and system dialer confirmation.
- [x] SMS composer for a phone number.
- [x] Open alarm screen.
- [ ] Unit-test command parsing.
- [ ] Explicit Hindi/English language selector.
- [ ] Improve SMS body prefill and contact-name SMS.
- [ ] Improve error states, permission rationale, and command history persistence.

## Milestone 2 — Background wake phrase
- [ ] User-started microphone foreground service.
- [ ] Android 13 notification permission handling.
- [ ] Correct foreground service type and microphone permissions for supported API levels.
- [ ] Persistent, clear listening notification with stop action.
- [ ] Battery and OEM restriction guidance.
- [ ] Test screen-off behavior on Android 13, 14, 15, and 16.
- [ ] Evaluate offline wake-word detection engine and document tradeoffs.

## Milestone 3 — Phone actions
- [ ] Open contact detail and dialer reliably.
- [ ] SMS compose with recipient and prefilled body.
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
1. CI build passes.
2. Unit tests pass where applicable.
3. Device test results are recorded.
4. README and project memory are updated.
5. Known Android/WhatsApp limitations are clearly documented.
