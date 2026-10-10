# Release

Release builds are signed with the Play upload key. The key is never in the repo.

## Owner setup (once)

1. Create the upload keystore on your machine and keep a backup:

   ```
   keytool -genkeypair -v -keystore tmr-upload.jks -alias tmr-upload \
     -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Add these GitHub Actions secrets (Settings, Secrets and variables, Actions):
   - `TMR_UPLOAD_KEYSTORE_BASE64`: output of `base64 -i tmr-upload.jks`
   - `TMR_UPLOAD_STORE_PASSWORD`
   - `TMR_UPLOAD_KEY_ALIAS`: `tmr-upload` if you used the command above
   - `TMR_UPLOAD_KEY_PASSWORD`

3. Optional repository variables: `TMR_VERSION_NAME` (default `0.1.0`) and `TMR_VERSION_CODE_OFFSET` (default `0`).

## Build a bundle

Run the "Release bundle" workflow from the Actions tab on `main`. It uploads `prod-release-aab` (kept 3 days).

Locally, set the four variables below (environment or `~/.gradle/gradle.properties`) and run `./gradlew :app:bundleProdRelease`:
`TMR_UPLOAD_KEYSTORE_FILE`, `TMR_UPLOAD_STORE_PASSWORD`, `TMR_UPLOAD_KEY_ALIAS`, `TMR_UPLOAD_KEY_PASSWORD`.

Without them `assembleRelease` still works and produces an unsigned APK. Any `bundle*Release` task fails with a message naming the missing values.

## Versions

- `TMR_VERSION_NAME`: `MAJOR.MINOR.PATCH` with an optional `-suffix`. Default `0.1.0`.
- `TMR_VERSION_CODE`: integer from 1 to 2100000000. Default `1`. The workflow sets it to `github.run_number + TMR_VERSION_CODE_OFFSET`, so it only grows.

Invalid values fail the build. `tools/ci/test-release-config.sh` checks this.
