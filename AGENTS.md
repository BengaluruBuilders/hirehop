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

CI runs the same gates as `tools/ci/verify-local.sh`, plus the PR title check and a secret scan.
CI compiles Kotlin with warnings as errors. Local builds do not, so run `tools/ci/verify-local.sh`.
The workflows are in `.github/workflows`.
