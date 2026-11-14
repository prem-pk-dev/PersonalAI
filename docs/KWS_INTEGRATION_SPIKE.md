# Offline KWS integration spike

## Goal

Verify the official sherpa-onnx Android runtime package before adding any native dependency to the PK application. This spike intentionally does not change the production app's Gradle dependencies, microphone lifecycle, or APK.

## Pinned upstream runtime

- Project: https://github.com/k2-fsa/sherpa-onnx
- Release: v2.0.1
- Official AAR: https://github.com/k2-fsa/sherpa-onnx/releases/download/v2.0.1/sherpa-onnx-2.0.1.aar
- Published asset size: 49,441,341 bytes (47.2 MiB)
- Published SHA-256: `a8d09d1d32f3eb8520482c6d3d43fae674681c5b050385967c70a3c2c9bf0ee7`
- Expected device ABI: arm64-v8a (must be checked on the Redmi Note 9; do not assume based on chipset alone)
- The release AAR is a prebuilt runtime package, separate from the KWS ONNX model files. Hosted CI uses an x86_64 emulator, so native JNI initialization is intentionally reserved for ARM device validation rather than falsely treating a class-loading test as native execution.

## Automated checks

`.github/workflows/kws-runtime-verification.yml` downloads the exact official release asset, checks its SHA-256, verifies the AAR contains compiled classes and native JNI libraries for arm64-v8a and armeabi-v7a, and uploads the verification evidence. It does not copy third-party binaries into the repository.

## Official English KWS model verification

- Official archive: https://github.com/k2-fsa/sherpa-onnx/releases/download/kws-models/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01-mobile.tar.bz2
- Archive SHA-256 verified in CI: `2e6ac2577310bfa2f4b6b5fab0478b868c9d0b2cb2c51b3e13b50581b588864d`
- CI run: 43603948126 (success); workflow checks the archive digest, extracts the model, confirms required int8/float32 ONNX files, tokens and keywords, and prints the bundled README.
- The bundled README identifies Apache License 2.0. The model documentation says it is trained on GigaSpeech XL and supports English only. This is not Hindi wake-word support.
- Mobile archive includes int8 encoder and joiner with float32 decoder; total download is about 15 MiB. Do not bundle model files in the Git repository. The intended product flow is a user-visible one-time model download into app-private storage, followed by offline inference.
- CI inspected the model BPE vocabulary: token IDs include `▁P` and `K`; the app's custom keyword file uses `▁P K` for the spoken letter sequence “P-K”. This is a token-level hypothesis, not a measured detection result; false accepts/rejects and Indian-accent performance still require audio evaluation on-device.
- The KWS model is a separate asset from the sherpa-onnx Android AAR. Pin and verify both independently.

## Integration gates

1. <strong>Passed:</strong> Runtime download and checksum verification pass; CI confirms the AAR contains native JNI payloads for arm64-v8a and armeabi-v7a (run 43600986575, artifact 14115548372).
2. CI confirms the KWS Java classes are packaged and the APK contains sherpa JNI libraries for arm64-v8a and armeabi-v7a. Native loading still requires an ARM64 device test; the hosted x86_64 emulator must not attempt to load an ARM-only JNI library.
3. The official English GigaSpeech KWS model is pinned separately, with its own source, checksum, licence, and offline asset strategy.
4. The custom spoken phrase "P-K" is evaluated for false accepts/rejects and accent robustness; the model is not represented as Hindi wake-word capable.
5. AudioRecord processing runs in a user-started microphone foreground service with a persistent notification and clear stop control.
6. Command handoff stops KWS capture cleanly, runs speech recognition, then returns to KWS only while the user-enabled listening session remains active.
7. Physical Redmi Note 9 / Android 12 / MIUI 13 tests cover foreground, background, screen-off, battery, and OEM restrictions.
8. Speaker verification is evaluated as a separate model and enrollment flow. Keyword spotting alone is not owner authentication.

## Explicit non-goals

- No continuous SpeechRecognizer restart loop as a substitute for KWS.
- No hidden microphone capture, automatic permission bypass, or lock-screen bypass.
- No claim of screen-off wake detection until real-device tests pass.
- No claim of owner-only activation until speaker verification and replay/false-accept evaluation pass.

## Upstream references

- https://github.com/k2-fsa/sherpa-onnx/releases/tag/v2.0.1
- https://k2-fsa.github.io/sherpa/onnx/android/build-sherpa-onnx.html
- https://github.com/k2-fsa/sherpa-onnx/tree/master/android/SherpaOnnxKws
