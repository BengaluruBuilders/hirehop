# HireHop Constitution

This document binds every contributor: people, Claude, and delegated agents.
If this document and another document disagree, this document wins.
`docs/ARCHITECTURE.md` comes second. `docs/PRD.md` comes third.

Each rule names its **gate**. A gate is the check that fails when someone breaks the rule.
"Review" means that no machine check exists yet. The [ledger](#ledger) lists the plan for that debt.

Run all gates on your machine with one command:

```bash
tools/ci/verify-local.sh
```

## Article I — Product integrity

These rules come from the core promise in `docs/PRD.md`: "We never invent anything about you."

| # | Rule | Gate |
|---|---|---|
| I.1 | Tailored text uses only facts that the candidate confirmed. Every tailored bullet cites its source IDs. The fabrication guard checks every bullet. If a bullet has a violation, the original text replaces it. | `OfflineFabricationGuardTest`, `TailorResumeUseCaseTest` |
| I.2 | The candidate decides. Every suggestion starts as `PENDING`. Nothing reaches an export without an explicit accept. | `UpdateBulletDecisionUseCaseTest`, `TailorViewModelTest` |
| I.3 | UI copy never claims an ATS score, an ATS pass, or a job, interview, or placement guarantee. Say "keyword coverage" and "readable by common ATS parsers" (PRD 6.4, 7). | Policy I.3 on `res/values*/*.xml` |
| I.4 | No API key, token, or private key goes into the app or the repository. Model credentials live on the backend only. | Policy I.4; gitleaks |
| I.5 | Candidate data stays on the device. Backups stay off. Network access, analytics, and crash SDKs need an amendment first. The amendment must name the consent screen (PRD 10.4). | Policy I.5: no `INTERNET` permission, no `allowBackup="true"`, no network or analytics library in the catalog |

## Article II — Architecture

HireHop follows Now in Android (NiA). `docs/ARCHITECTURE.md` gives the detail.

| # | Rule | Gate |
|---|---|---|
| II.1 | Module graph: a feature `impl` module depends only on `core:*`, its own `api`, and other features' `api` modules. An `api` module never depends on an `impl` module. A `core` module never depends on a feature. `core:model` is pure Kotlin/JVM. | Policy II.1 |
| II.2 | Unidirectional data flow. A `ViewModel` exposes one `StateFlow` of a sealed `UiState` through `stateIn(WhileSubscribed(5_000))`. A screen composable is stateless. A `...Route` wrapper collects state with `collectAsStateWithLifecycle`. | Review |
| II.3 | No `GlobalScope`. No `runBlocking` in production code. No `!!`, not even in string literals: put UI text in resources. | Policy II.3 |
| II.4 | Inject every dispatcher with `@Dispatcher`. Only `core:common` refers to `Dispatchers.IO` or `Dispatchers.Default` directly. | Policy II.4 |
| II.5 | Features use `Hh*` components from `core:designsystem`, not raw Material components. | Review |
| II.6 | Each "AI" step sits behind an interface in `core:domain`. A backend implementation replaces the offline one through a Hilt binding. | Review |
| II.7 | Debug builds run StrictMode. If StrictMode logs disk or network access on the main thread, fix it. | `HireHopApplication`; review |

## Article III — Code quality

| # | Rule | Gate |
|---|---|---|
| III.1 | Spotless with ktlint formats all Kotlin and Gradle Kotlin files. Run `./gradlew spotlessApply` to fix. | `spotlessCheck` |
| III.2 | No code comments. KDoc counts as a comment. A comment may stay only if it cites an external ground: a URL, `issue 123`, `bug 123`, `b/123`, `KT-123`, a CVE, or a spec `line 123`. | Policy III.2 |
| III.3 | Zero warnings. CI compiles Kotlin with warnings as errors. Lint treats warnings as errors. A `lint-baseline.xml` can only get smaller. A new baseline file is an amendment (VII.4). | `.github/ci-gradle.properties`; `lintRelease`; policy III.3 on PRs |
| III.4 | One responsibility per class. Keep functions under about 40 lines. | Review |
| III.5 | Build configuration lives in `build-logic` convention plugins. A module `build.gradle.kts` declares its plugins, namespace, and dependencies. Only `:app` adds build types and packaging. | Review |

## Article IV — Testing

| # | Rule | Gate |
|---|---|---|
| IV.1 | Every change to behaviour ships with a test. Domain logic gets unit tests. ViewModels get unit tests with fakes and Turbine. DAOs get instrumented tests. | `testDebugUnitTest`; weekly `android-test.yml`; review |
| IV.2 | Use fakes from `core:testing`. Do not use mocking libraries. | Policy IV.2 |
| IV.3 | Tests are deterministic. Use `runTest`, virtual time, and `MainDispatcherRule`. Never use `Thread.sleep`. | Policy IV.3 |
| IV.4 | The test stack is JUnit4, Truth, Turbine, and `kotlinx-coroutines-test`. | Review |

## Article V — Build, dependencies, and release

| # | Rule | Gate |
|---|---|---|
| V.1 | Every dependency version lives in `gradle/libs.versions.toml`. A PR that adds a dependency says why. | Policy V.1; review |
| V.2 | Dependabot opens monthly update PRs. Kotlin and KSP update together. | `.github/dependabot.yml` |
| V.3 | CI validates the Gradle wrapper checksum. | `gradle/actions/setup-gradle` |
| V.4 | Keep the build cache, the configuration cache (problems fail the build), and isolated projects on. Keep `org.gradle.workers.max=3`, because this Mac also runs a shared CI runner pool. | Policy V.4 |
| V.5 | JDK 17 toolchain. `minSdk` 26. `compileSdk` and `targetSdk` 36. | `build-logic` |
| V.6 | The Room schema JSON in `core/database/schemas` is committed. If an entity changes, bump the database version, commit the new schema, and add a migration. | "Check Room schema is committed" step in `build.yml` |
| V.7 | The release build uses R8 with resource shrinking. CI builds it on every PR. Release uses the debug signing key until a Play upload key exists. Replace the key before the first Play upload. | `assembleRelease` in `build.yml` |

## Article VI — Change process

| # | Rule | Gate |
|---|---|---|
| VI.1 | Changes reach `main` only through a pull request with green CI. Use squash merge. | Review. See the note below. |
| VI.2 | The PR title uses Conventional Commits: `type(scope): summary`. Squash merge makes it the commit message. | "Check PR title" step in `build.yml` |
| VI.3 | One concern per PR. Say what changed, why, and how you verified it. | `.github/pull_request_template.md`; review |
| VI.4 | Write prose in ASD-STE100 Simplified Technical English: PR bodies, commit messages, docs, and UI strings. | Review |
| VI.5 | Run `tools/ci/verify-local.sh` before you open a PR. Run `tools/setup.sh` once. It links a pre-push hook that runs the policy check. | `tools/pre-push` (policy check only) |

Note on VI.1: GitHub blocks branch protection on private repositories on the Free plan.
Until the repository is public or on GitHub Pro, nothing stops a direct push to `main`.
When protection is available, require the `Constitution policy` and `Build, lint, and unit tests` checks.

## Article VII — Amendments

1. Change a rule only through a PR that edits this file.
2. A new rule must name its gate. If no gate exists, write "Review" and add a ledger row.
3. If you change a gate, change the rule text in the same PR.
4. A lint baseline entry, a suppression, or an exception is an amendment. Say why in the PR.

## Ledger

The ledger compares HireHop with the NiA production setup (commit a49ed25).
HireHop already has: convention plugins, a version catalog, Spotless, warnings as errors, lint with
baselines, Dependabot, gitleaks, an R8 release build, a Room schema check, and weekly instrumented tests.

| Next | Why | Trigger |
|---|---|---|
| Fabrication test set as a CI gate | PRD 9 makes "0 critical fabrications" a release gate. This is the core promise. | Next PR |
| Custom lint module | Turns II.2, II.5, and III.2 into real detectors, as NiA's `DesignSystemDetector` does. | After the fabrication set |
| Coverage report (JaCoCo or Kover) | NiA requires 40% overall and 60% on changed files. | After the fabrication set |
| Dependency Guard | Locks the release classpath, which makes I.5 airtight. | Before the backend work starts |
| Instrumented tests on every PR | Catches DAO and UI regressions before merge. | When the repository has free minutes (public or paid plan) |

Deferred until a trigger occurs: Roborazzi screenshot tests (when the UI stabilises), baseline
profiles (after the first Play release), Firebase (needs an I.5 amendment), and `demo`/`prod`
flavors (when the backend exists).
