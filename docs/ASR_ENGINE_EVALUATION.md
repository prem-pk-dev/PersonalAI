# Offline Hindi + English command recognition evaluation

## Objective

Provide offline command recognition after the existing PK wake-word detector, including while the PK Activity is not visible. This is separate from keyword spotting: the current GigaSpeech model only detects the English spoken-letter wake phrase and is not a Hindi/English general-purpose recognizer.

## Candidate review (15 November 2026)

### Candidate A — Vosk small language models

Official model catalogue: https://alphacephei.com/vosk/models

- Hindi `vosk-model-small-hi-0.22`: 42 MB; published WER 20.89 (IITM Challenge), 24.72 (MUCS Challenge); Apache-2.0.
- Indian English `vosk-model-small-en-in-0.4`: 36 MB; published WER 49.05 (NPTEL Pure); Apache-2.0.
- Vosk describes small models as mobile/Raspberry Pi capable and estimates roughly 300 MB runtime memory.
- Vosk has an Android offline demo and Java API: https://alphacephei.com/vosk/android

Assessment: Hindi model is a plausible compact offline candidate. The listed Indian-English small model has a weak published benchmark, so command recognition quality must be measured with a PK-specific Indian-English/Hinglish phrase set. Do not load both models simultaneously on a memory-constrained phone; evaluate one-at-a-time model switching and RAM peaks. Model WER is not a direct command success rate.

### Candidate B — sherpa-onnx streaming ASR

Official project and Android examples:
- https://github.com/k2-fsa/sherpa-onnx
- https://k2-fsa.github.io/sherpa/onnx/android/index.html
- https://k2-fsa.github.io/sherpa/onnx/pretrained_models/

Assessment: PK already bundles the sherpa-onnx 2.0.1 Android runtime and uses it for keyword spotting, so this avoids adding another native runtime. However, the reviewed official streaming model catalogue did not establish a suitable Hindi + Indian-English streaming model pair for this target device. The commonly documented streaming bilingual example is Chinese/English, not Hindi/English. Do not claim Hindi support based on the runtime alone.

## Current engineering decision

Do not integrate an unverified ASR model into the always-on service yet. Prototype the Hindi Vosk small model and compare it with a verified sherpa-onnx Hindi model if one becomes available. For English, test Indian-English command accuracy before choosing Vosk small en-in; its published benchmark warns that it may not meet the product bar.

## Required prototype gates

1. Pin model URLs, licences, archive hashes, and extracted file manifests independently of the application runtime.
2. Download on explicit user action into app-private storage; never bundle large models in Git or silently download.
3. Build a fixed test corpus covering Hindi, Indian English, Romanized Hindi, code-switching, noisy rooms, and common PK commands.
4. Measure command exact-match, false action rate, latency, CPU, peak RAM, battery drain, and model switch cost on Redmi Note 9 / Android 12 / MIUI 13.
5. Only one speech recognizer may own the microphone at a time. KWS must release AudioRecord before ASR starts; ASR must release it before KWS resumes.
6. Keep the foreground service notification visible with a working Stop action. If Android denies microphone access or re-creation, show an actionable state; do not bypass platform restrictions.
7. Until the above passes, keep the current notification-tap handoff and label it honestly as an incomplete wake-to-command flow.

## Known constraints

- Current KWS service stops on wake detection and posts a notification; it does not yet run general ASR in the service.
- Android's platform SpeechRecognizer may use a device-provided service and is not proof of offline operation unless on-device availability is confirmed and the recognizer is explicitly created as on-device.
- Wake-word spotting is not speaker verification or owner authentication.
