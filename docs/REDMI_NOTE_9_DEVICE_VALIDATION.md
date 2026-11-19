# Redmi Note 9 device validation checklist

Target: Redmi Note 9 · Android 12 · MIUI 13  
Build under test: `personalai-debug-apk-device-test` · package `com.prempk.personalai` · version `0.3.0 (3)`

Record each result as **Pass / Fail / Not tested**. CI success is not a substitute for these checks.

## 1. Install and first launch

- [ ] Confirm the downloaded artifact is from the latest successful Android CI run.
- [ ] If an older PK build signed with the unavailable certificate is installed, back up anything needed before uninstalling it; uninstalling can erase app-local data.
- [ ] Install the new APK and confirm Android completes installation.
- [ ] Launch PK; confirm the home screen opens without a crash.
- [ ] Confirm microphone permission is requested only when starting voice listening.
- [ ] Confirm denied permissions produce a clear explanation and the app remains usable where possible.

## 2. Basic commands

- [ ] Tap-to-listen: speak an English app-launch command; confirm the correct installed app opens.
- [ ] Speak a Hindi / Romanized-Hindi command; check transcript, action and spoken response.
- [ ] Submit a text command using both the keyboard Send/Done action and the on-screen send control.
- [ ] Try an unsupported command; confirm PK explains the limitation instead of pretending it succeeded.
- [ ] Test a call command; confirm it opens the system dialer and does not place a call automatically.
- [ ] Test an SMS command; confirm it opens a composer for manual review and sending.

## 3. Background PK wake-word service

- [ ] Install the optional KWS model using the in-app flow; confirm model verification succeeds.
- [ ] Grant microphone and notification permissions when prompted.
- [ ] Start the listener explicitly; confirm a persistent foreground-service notification appears.
- [ ] Confirm the notification Stop action stops the listener.
- [ ] Restart the listener, say **PK**, and confirm a wake-detected notification appears.
- [ ] Tap the wake notification; confirm PK opens and only then starts command recognition.
- [ ] Complete one command; confirm the listener resumes only while the user's listener opt-in remains enabled.
- [ ] Stop the listener; confirm saying PK no longer triggers a wake notification.
- [ ] Repeat with screen off and record the result separately; do not assume screen-off reliability from screen-on success.

**Expected behavior:** background KWS does not start command SpeechRecognizer automatically. The user must tap the wake notification. Do not mark seamless hands-free wake-to-command as passed.

## 4. MIUI 13 reliability

- [ ] Check that notification visibility and Stop action work after leaving PK in the background.
- [ ] Test after screen lock and after several minutes idle.
- [ ] If MIUI battery restrictions interrupt the service, record the exact symptom and relevant battery/autostart setting; do not silently weaken Android safeguards.
- [ ] Test listener stop/start repeatedly and watch for duplicate notifications or microphone sessions.

## 5. Optional integrations

- [ ] Hindi offline ASR: download the optional model, run a short Hindi utterance, and record transcription quality and latency. This is separate from KWS.
- [ ] Media controls: grant Notification Access, start a media session in another app, then test play/pause/next/previous.
- [ ] Accessibility: enable PK Accessibility Service only if intentionally testing it; verify scroll, Back and Home, and confirm it does not read screen text or send messages.
- [ ] Alarm/timer/reminder: confirm each opens the relevant Android screen for user review rather than silently scheduling.

## Results

| Area | Result | Notes |
|---|---|---|
| Install / first launch | Not tested | |
| Basic voice / text commands | Not tested | |
| Hindi / Romanized-Hindi | Not tested | |
| KWS foreground notification | Not tested | |
| PK notification tap handoff | Not tested | |
| KWS resume / Stop | Not tested | |
| Screen-off behavior | Not tested | |
| MIUI background reliability | Not tested | |
| Optional integrations | Not tested | |

Do not merge to `main` until the required device checks are recorded, failures are triaged, and the feature branch CI remains green. This APK is for debug/device testing, not production release.
