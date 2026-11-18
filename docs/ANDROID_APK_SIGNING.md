# Persistent PK Assistant debug APK signing

## Why this is required

Android only accepts an in-place APK update when the installed package and new APK are signed by the same signing certificate. GitHub-hosted runners generate a fresh Gradle debug key for each run unless a persistent key is configured. That caused the prior "package conflicts with an existing package" installation error.

## One-time GitHub Actions setup

This project uses a dedicated **debug/device-test** key. It is not a production release key and must never be reused for Play Store or production signing.

In GitHub, open the repository Settings -> Secrets and variables -> Actions -> New repository secret. Add these four secrets using the matching files generated for the project:

- `PK_SIGNING_KEYSTORE_BASE64`: complete one-line Base64 content from `PK_SIGNING_KEYSTORE_BASE64.txt`.
- `PK_SIGNING_STORE_PASSWORD`: value in `PK_SIGNING_CREDENTIALS.txt`.
- `PK_SIGNING_KEY_ALIAS`: value in `PK_SIGNING_CREDENTIALS.txt`.
- `PK_SIGNING_KEY_PASSWORD`: value in `PK_SIGNING_CREDENTIALS.txt`.

After all four secrets are saved, rerun the Android CI workflow or push a commit. The workflow verifies the APK signer certificate fingerprint before uploading the APK. It deliberately does not publish an APK artifact if the persistent signing secrets are absent.

## Expected signer

- SHA-256 certificate fingerprint: `766ff50e0519533e76e40f52801d5848d72b50581154642feba583b69067772b`
- Alias: `pkassistant`
- Keystore format: JKS
- APK application ID: `com.prempk.personalai`

## Important migration note

The already-installed APKs were signed by earlier ephemeral keys. They cannot be updated in place using this new persistent key. Uninstall the existing PK Assistant one final time, install the first APK signed with this persistent key, and keep that app installed for future updates. Uninstalling can remove local app data.

## Release security

This key is strictly for debug/device-test builds. Never publish it, commit it to the repository, or use it for production/release signing. Keep its Base64 and password values only in GitHub Actions repository secrets and a secure private backup.
