# Android CI: manual trigger from mobile

The Android CI workflow on `feature/sherpa-onnx-kws-spike` runs on pushes to this branch and supports `workflow_dispatch`.

If GitHub's mobile Actions page does not show the **Run workflow** control, a documentation-only commit to this branch can trigger the normal push workflow without changing application code. The APK is published only when all four persistent signing secrets are configured and the signing fingerprint matches the documented key.

After a successful run, verify the `personalai-debug-apk-device-test` artifact and its signing certificate before installing it. The first install signed with the persistent key requires removing any previously installed build signed with a different key.
