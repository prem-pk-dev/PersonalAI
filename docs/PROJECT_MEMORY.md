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
- Wake phrase: "Hey Assistant".
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
- The MVP's current wake phrase recognition works only while the app is open and the user has started listening. Do not describe it as always-on.
- Offline speech recognition depends on the device's installed recognition provider and language packs.
- Do not bypass lock-screen protections, record secretly, auto-send messages, or automate emergency calling.

## Current repository state (2026-11-14)

- Default branch: `main`; keep it stable.
- Active branch: `development/mvp-foundation`.
- Pull request: #1, draft, targets `main`.
- Initial Android project and first MVP Activity are committed to the development branch.
- CI workflow exists at `.github/workflows/android-ci.yml`.
- The first CI attempt failed at Kotlin compilation because the Activity property named `background` shadowed the View background property. This was corrected by renaming it to `backgroundColorValue`; a new CI result must be checked before claiming the build passes.
- A follow-up fix added two-step wake handling (wake phrase first, command in the next recognition result) and additional Hindi/English call-command filler-word cleanup.
- No physical Android device test has yet been performed.
- No release APK has yet been validated or published.

## MVP scope

- Voice recognition using Android SpeechRecognizer.
- Text command input.
- TextToSpeech responses.
- Wake phrase detection during an explicitly user-started listening session while the app is open.
- Launch WhatsApp / WhatsApp Business, YouTube, Chrome, and Android Settings.
- Contact lookup after Contacts permission and open system dialer for user confirmation.
- Open SMS composer for user review and send.
- Alarm screen intent.
- Visible command history.

## Next development sequence

1. Fix and verify the CI build; preserve the full logs and fix every compile error.
2. Add a downloadable debug APK artifact to successful CI runs.
3. Add unit-testable command parsing and tests for Hindi/English phrases.
4. Add explicit language selection (Hindi India / English India) for speech recognition and TTS.
5. Improve app-launch, contact, SMS-composer, and alarm actions with error handling.
6. Implement a user-started foreground wake-word service as a separate, permission-aware milestone; test Android 13, 14, 15, and 16 separately.
7. Evaluate an offline wake-word engine that can run at ₹0; document battery and model-size tradeoffs.
8. Add optional AI reasoning only after deterministic phone actions are reliable.
9. Investigate cellular call controls only through a compliant default dialer; document WhatsApp limitations.
10. Merge to `main` only after CI is green, review is complete, and device testing is recorded.

## Working protocol

- Inspect the latest branch and PR before every continuation.
- Work in a dedicated `development/*` branch; do not directly change `main`.
- Preserve existing features when editing files; inspect the current file and patch narrowly.
- After each logical change: commit, inspect CI, inspect logs on failure, fix, and re-run.
- Do not claim an APK exists unless the workflow artifact or a local build has been verified.
- Do not merge automatically. Keep the PR draft until validation criteria are met.
- Update this file and the roadmap whenever milestones, limitations, or architecture decisions change.
