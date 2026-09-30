# HireHop

HireHop is an Android app. It turns a candidate's confirmed background and a job description into a
tailored application. It never invents facts about the candidate.

## Read these first

1. `docs/CONSTITUTION.md` — the rules and the CI gate for each rule. It wins every conflict.
2. `docs/ARCHITECTURE.md` — modules, models, interfaces, and navigation.
3. `docs/PRD.md` — product scope and release gates.

## Commands

| Task | Command |
|---|---|
| Run every CI gate on your machine | `tools/ci/verify-local.sh` |
| Run the constitution policy check only | `tools/ci/check-constitution.sh` |
| Fix formatting | `./gradlew spotlessApply` |
| Run unit tests | `./gradlew testDebugUnitTest` |
| Run one test class | `./gradlew :core:domain:testDebugUnitTest --tests "com.hirehop.core.domain.TailorResumeUseCaseTest"` |
| Run lint | `./gradlew lintRelease` |
| Build the debug APK | `./gradlew assembleDebug` |
| Install the pre-push hook | `tools/setup.sh` |
| Stop Gradle daemons when you finish | `./gradlew --stop` |

## Continuous integration

- `.github/workflows/build.yml` runs on every PR and on `main`: PR title, constitution policy,
  secret scan, build-logic check, Spotless, unit tests, lint, and APK assembly.
- `.github/workflows/android-test.yml` runs instrumented tests on API 26 and 34. It runs on `main`,
  every Sunday, and on demand.
- CI compiles Kotlin with warnings as errors. Local builds do not, so run `tools/ci/verify-local.sh`.
