# HireHop Constitution

This document binds every contributor: people, Claude, and delegated agents.
If this document and another document disagree, this document wins.
`docs/ARCHITECTURE.md` comes second. `docs/PRD.md` comes third.

Each rule names its **gate**. A gate is the check that fails when someone breaks the rule.
"Review" means that no machine check exists yet. Each review-only rule is automation debt.
The [adoption ledger](#adoption-ledger) lists the plan to remove that debt.

Run all gates on your machine with one command:

```bash
tools/ci/verify-local.sh
```

## Article I — Product integrity

These rules come from the core promise in `docs/PRD.md`: "We never invent anything about you."

| # | Rule | Gate |
|---|---|---|
| I.1 | Tailored text uses only facts that the candidate confirmed. Every tailored bullet cites its source IDs. The fabrication guard checks every bullet. If a bullet has a violation, the original text replaces it. | `core:domain` unit tests (`OfflineFabricationGuardTest`, `TailorResumeUseCaseTest`) in the `build` job |
| I.2 | The candidate decides. Every suggestion starts as `PENDING`. Nothing reaches an export without an explicit accept. | `UpdateBulletDecisionUseCaseTest`, `TailorViewModelTest`; review |
| I.3 | UI copy never claims an ATS score, an ATS pass, or a job, interview, or placement guarantee. Say "keyword coverage" and "readable by common ATS parsers" (PRD 6.4, 7). | `check-constitution.sh` I.3 on `strings.xml` |
| I.4 | No API key, token, or private key goes into the app or the repository. Model credentials live on the backend only. | `check-constitution.sh` I.4; gitleaks in the `policy` job |
| I.5 | Candidate data stays on the device. Network access, analytics, and crash SDKs need an amendment first. The amendment must name the consent screen (PRD 10.4). | `check-constitution.sh` I.5: no `INTERNET` permission, no network or analytics library in the catalog |

## Article II — Architecture

HireHop follows Now in Android (NiA). `docs/ARCHITECTURE.md` gives the detail.

| # | Rule | Gate |
|---|---|---|
| II.1 | Module graph: a feature `impl` module depends only on `core:*`, its own `api`, and other features' `api` modules. An `api` module never depends on an `impl` module. A `core` module never depends on a feature. `core:model` is pure Kotlin/JVM. | `check-constitution.sh` II.1 |
| II.2 | Unidirectional data flow. A `ViewModel` exposes one `StateFlow` of a sealed `UiState` through `stateIn(WhileSubscribed(5_000))`. A screen composable is stateless. A `...Route` wrapper collects state with `collectAsStateWithLifecycle`. | Review |
| II.3 | No `GlobalScope`. No `runBlocking` in production code. No `!!`. | `check-constitution.sh` II.3 |
| II.4 | Inject every dispatcher with `@Dispatcher`. Only `core:common` refers to `Dispatchers.*` directly. | `check-constitution.sh` II.4 |
| II.5 | Features use `Hh*` components from `core:designsystem`, not raw Material components. | Review |
| II.6 | Each "AI" step sits behind an interface in `core:domain`. A backend implementation replaces the offline one through a Hilt binding, not through edits to callers. | Review |

## Article III — Code quality

| # | Rule | Gate |
|---|---|---|
| III.1 | Spotless with ktlint formats all Kotlin and Gradle Kotlin files. Run `./gradlew spotlessApply` to fix. | `spotlessCheck` |
| III.2 | No code comments. A comment may stay only if it cites an external ground: a spec line, a bug ID, a URL, a CVE, or a measured number with its source. | `check-constitution.sh` III.2 |
| III.3 | Zero warnings. CI compiles Kotlin with warnings as errors. Lint treats warnings as errors. A module's `lint-baseline.xml` can only get smaller. | `.github/ci-gradle.properties`; `lintRelease`; baseline growth is review |
| III.4 | One responsibility per class. Keep functions under about 40 lines. No god classes. | Review |
| III.5 | Build configuration lives in `build-logic` convention plugins. A module `build.gradle.kts` declares its plugins, namespace, and dependencies only. | `:build-logic:convention:check`; review |

## Article IV — Testing

| # | Rule | Gate |
|---|---|---|
| IV.1 | Every change to behaviour ships with a test. Domain logic gets unit tests. ViewModels get unit tests with fakes and Turbine. DAOs get instrumented tests. | `testDebugUnitTest`; `connectedDebugAndroidTest` in `android-test.yml`; review |
| IV.2 | Use fakes from `core:testing`. Do not use mocking libraries. | `check-constitution.sh` IV.2 |
| IV.3 | Tests are deterministic. Use `runTest`, virtual time, and `MainDispatcherRule`. Never use `Thread.sleep`. | `check-constitution.sh` IV.3 |
| IV.4 | The test stack is JUnit4, Truth, Turbine, and `kotlinx-coroutines-test`. | Review |

## Article V — Dependencies and build

| # | Rule | Gate |
|---|---|---|
| V.1 | Every dependency version lives in `gradle/libs.versions.toml`. A PR that adds a dependency says why. | `check-constitution.sh` V.1; review |
| V.2 | Dependabot opens weekly update PRs. Kotlin and KSP update together in one group. | `.github/dependabot.yml` |
| V.3 | CI validates the Gradle wrapper checksum. | `wrapper-validation` in the `build` job |
| V.4 | The build cache, the configuration cache (problems fail the build), and isolated projects stay on. | `check-constitution.sh` V.4 |
| V.5 | JDK 17 toolchain. `minSdk` 26. `compileSdk` and `targetSdk` 36. | `build-logic` |

## Article VI — Change process

| # | Rule | Gate |
|---|---|---|
| VI.1 | Changes reach `main` only through a pull request with green CI. Use squash merge. | Review. See the note below. |
| VI.2 | The PR title uses Conventional Commits: `type(scope): summary`. | `pr_title` job |
| VI.3 | One concern per PR. Fill in the PR template checklist. | `.github/pull_request_template.md`; review |
| VI.4 | Write prose in ASD-STE100 Simplified Technical English: PR bodies, commit messages, docs, and UI strings. | Review |
| VI.5 | Run `tools/ci/verify-local.sh` before you push. Run `tools/setup.sh` once to install the pre-push hook. | `tools/pre-push` |

Note on VI.1: GitHub blocks branch protection on private repositories on the Free plan.
Until the repository is public or on GitHub Pro, nothing stops a direct push to `main`.
When protection is available, require the `PR title`, `Constitution policy`, and
`Build, lint, and unit tests` checks.

## Article VII — Shared build host

This Mac also runs a CI runner pool. An overloaded host stops other teams' CI.

| # | Rule | Gate |
|---|---|---|
| VII.1 | Keep `org.gradle.workers.max=3`. | `check-constitution.sh` V.4 |
| VII.2 | Run `./gradlew --stop` when you finish a session of builds. | Review |
| VII.3 | Do not start unbounded background processes or CPU loops. | Host hooks |

## Article VIII — Amendments

1. Change a rule only through a PR that edits this file.
2. A new rule must name its gate. If no gate exists, write "Review" and add a ledger row.
3. If you change a gate, change the rule text in the same PR.
4. A lint baseline entry, a suppression, or an exception is an amendment. Say why in the PR.

## Adoption ledger

This table compares HireHop with the NiA production setup (commit a49ed25).
It shows what HireHop uses now and what comes next.

| Practice | NiA | HireHop | Next step |
|---|---|---|---|
| Convention plugins, version catalog, Spotless | Yes | Yes | — |
| Kotlin warnings as errors in CI | Off | **On** | — |
| Lint warnings as errors, with baselines | No | **On** (15 baselined issues in 5 modules) | Burn down the baselines |
| Gradle wrapper validation | Yes (inside `setup-gradle`) | Yes | — |
| Dependency updates | Renovate | Dependabot | — |
| Instrumented tests on an emulator matrix | Every PR | `main`, weekly, and on demand; API 26 and 34 | Move to every PR when minutes allow |
| Secret scanning | No | gitleaks | — |
| Fabrication evaluation set as a release gate (PRD 9) | — | No | **Phase 2.** A seeded-fabrication corpus in `core:domain` tests. Recall above 95%, 0 critical misses. |
| Custom lint rules (`DesignSystemDetector`, test names) | Yes | No | **Phase 2.** Add a `:lint` module. Move II.2, II.5, and the comment rule to real detectors. |
| JaCoCo coverage with a PR report | Yes (40% overall, 60% changed files) | No | **Phase 2** |
| Dependency Guard baselines | Yes | No | **Phase 3.** This also locks rule I.5. |
| R8 release build and badging check | Yes | No | **Phase 3** |
| Roborazzi screenshot tests | Yes | No | **Phase 3** |
| Baseline profiles and macrobenchmarks | Yes | No | Phase 4 |
| Firebase Crashlytics and Performance | Yes | No | Needs an I.5 amendment and a consent screen |
| `demo` and `prod` flavors | Yes | No | When the backend exists |
| Module graph generation | Yes | No | Optional. II.1 already enforces the rules. |
