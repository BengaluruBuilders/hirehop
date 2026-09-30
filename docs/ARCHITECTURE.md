# HireHop prototype — architecture spec

This spec binds every implementation agent. The reference is Now in Android (NiA) at
`/Users/aibuilders/Documents/dev/products/_ref/nowinandroid` (commit a49ed25). Read it locally. Copy its
patterns, build logic, and versions. Do not fetch it from the web.

Product context: `docs/PRD.md` in the repo. The prototype covers PRD features F1, F2, F3, F4 (PDF only), F5.
The prototype has no backend. All "AI" steps run as deterministic, offline Kotlin code behind interfaces, so a
backend implementation can replace them later (dependency inversion).

## 1. Coding standards

- Follow NiA: Kotlin, Jetpack Compose, Material 3, Hilt, Room, Coroutines/Flow, Navigation 3, unidirectional
  data flow, `UiState` sealed interfaces, `StateFlow` from `ViewModel` via `stateIn(WhileSubscribed(5_000))`,
  `collectAsStateWithLifecycle`, stateless screen composables with a stateful `...Route`/entry wrapper.
- SOLID: one responsibility per class; depend on interfaces; small focused interfaces; no god classes; no god
  functions (keep functions under ~40 lines).
- **No code comments.** No KDoc, no license headers, no section dividers, no commented-out code. Use names.
  The only allowed comment names an external ground (bug ID, spec line). This rule is enforced by a hook.
- No `!!`. No `GlobalScope`. Inject dispatchers with the `@Dispatcher(IO)` qualifier as NiA does.
- Tests: JUnit4 + Truth + Turbine + kotlinx-coroutines-test, as in NiA. Use test fakes from `core:testing`,
  not mocking libraries.
- Package root: `com.hirehop`. Namespaces: `com.hirehop.core.model`, `com.hirehop.feature.profile.impl`, etc.
  Application ID: `com.hirehop.app`.
- Convention plugin IDs: `hirehop.android.application`, `hirehop.android.application.compose`,
  `hirehop.android.library`, `hirehop.android.library.compose`, `hirehop.android.feature.api`,
  `hirehop.android.feature.impl`, `hirehop.android.room`, `hirehop.hilt`, `hirehop.jvm.library`.
- minSdk 26, compileSdk/targetSdk 36. JDK 17 toolchain (host has JDK 17 only).

## 2. Modules

| Module | Type | Content |
|---|---|---|
| `:app` | application | `HireHopApplication` (@HiltAndroidApp), `MainActivity`, `HhApp`, `HhAppState`, top-level destinations, Nav3 wiring |
| `:core:model` | jvm library | Pure Kotlin data models (section 3) |
| `:core:common` | android library | `Dispatcher` qualifier, `HhDispatchers`, dispatchers + application-scope DI modules, `Result` wrapper |
| `:core:designsystem` | library compose | `HhTheme`, colors, typography, components (`HhButton`, `HhTopAppBar`, `HhNavigationSuiteScaffold`, `HhBackground`, `HhLoadingWheel`) |
| `:core:ui` | library compose | Shared UI pieces used by more than one feature |
| `:core:navigation` | library | NiA `Navigator` + `NavigationState` pattern |
| `:core:database` | library + room + hilt | Room DB, entities, DAOs, type converters |
| `:core:data` | library + hilt | Repository interfaces and offline-first implementations |
| `:core:domain` | library + hilt | JD analysis, gap match, tailoring, fabrication guard, resume text parser, use cases |
| `:core:testing` | library | Test repositories, `MainDispatcherRule`, test data |
| `:feature:applications:{api,impl}` | feature | Workspace list + detail + status (top-level, start destination) |
| `:feature:profile:{api,impl}` | feature | View, edit, confirm profile entries; paste-resume import; demo profile (top-level) |
| `:feature:analysis:{api,impl}` | feature | Paste JD, see gap analysis, save as application |
| `:feature:tailor:{api,impl}` | feature | Per-bullet diff review (accept/reject), PDF export and share |

Dependency rules: features depend on `core:*` and on other features' `api` only. `core:domain` depends on
`core:data` and `core:model`. `core:model` depends on nothing Android.

## 3. Shared models (`:core:model`, package `com.hirehop.core.model`)

Use these names and shapes exactly. Timestamps use `kotlin.time.Instant` (Kotlin 2.3, stable).

```kotlin
enum class EntryCategory { EDUCATION, EXPERIENCE, PROJECT, CERTIFICATION, ACHIEVEMENT }
enum class FactSource { IMPORTED, USER_STATED, USER_EDITED }

data class EvidenceBullet(val id: String, val text: String)

data class ProfileEntry(
    val id: String,
    val category: EntryCategory,
    val title: String,
    val organization: String,
    val startDate: String,
    val endDate: String,
    val bullets: List<EvidenceBullet>,
    val source: FactSource,
    val isConfirmed: Boolean,
)

data class CandidateProfile(
    val fullName: String,
    val email: String,
    val phone: String,
    val headline: String,
    val skills: List<String>,
    val entries: List<ProfileEntry>,
)

enum class RequirementType { SKILL, TOOL, EXPERIENCE, EDUCATION, SOFT_SKILL }
enum class RequirementPriority { MUST_HAVE, NICE_TO_HAVE }

data class JobRequirement(
    val id: String,
    val text: String,
    val type: RequirementType,
    val priority: RequirementPriority,
    val keywords: List<String>,
)

data class JobDescription(
    val title: String,
    val company: String,
    val rawText: String,
    val requirements: List<JobRequirement>,
)

enum class MatchStatus { MET, PARTIAL, GAP }

data class RequirementMatch(
    val requirement: JobRequirement,
    val status: MatchStatus,
    val evidenceIds: List<String>,
)

data class KeywordCoverage(val covered: Int, val total: Int)

data class GapAnalysis(
    val matches: List<RequirementMatch>,
    val keywordCoverage: KeywordCoverage,
)

enum class EditType { REWORD, REORDER, SHORTEN, EMPHASISE, MERGE }
enum class BulletDecision { PENDING, ACCEPTED, REJECTED }

sealed interface GuardrailViolation {
    data object MissingSource : GuardrailViolation
    data class UnsupportedNumber(val value: String) : GuardrailViolation
    data class UnsupportedTerm(val term: String) : GuardrailViolation
    data class VerbEscalation(val from: String, val to: String) : GuardrailViolation
    data class UnsupportedScaleClaim(val phrase: String) : GuardrailViolation
}

data class TailoredBullet(
    val id: String,
    val entryId: String,
    val originalText: String,
    val proposedText: String,
    val sourceIds: List<String>,
    val editTypes: List<EditType>,
    val keywordsUsed: List<String>,
    val violations: List<GuardrailViolation>,
    val decision: BulletDecision,
)

data class TailoredResume(val bullets: List<TailoredBullet>)

enum class ApplicationStatus { SAVED, APPLIED, INTERVIEW, OFFER, REJECTED, NO_RESPONSE }

data class JobApplication(
    val id: String,
    val job: JobDescription,
    val status: ApplicationStatus,
    val notes: String,
    val gapAnalysis: GapAnalysis?,
    val tailoredResume: TailoredResume?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
```

## 4. Repository interfaces (`:core:data`, package `com.hirehop.core.data.repository`)

```kotlin
interface ProfileRepository {
    fun observeProfile(): Flow<CandidateProfile?>
    suspend fun saveProfile(profile: CandidateProfile)
    suspend fun clearProfile()
}

interface ApplicationRepository {
    fun observeApplications(): Flow<List<JobApplication>>
    fun observeApplication(id: String): Flow<JobApplication?>
    suspend fun upsertApplication(application: JobApplication)
    suspend fun updateStatus(id: String, status: ApplicationStatus)
    suspend fun updateNotes(id: String, notes: String)
    suspend fun deleteApplication(id: String)
}
```

Test fakes in `:core:testing`: `TestProfileRepository`, `TestApplicationRepository` (backed by
`MutableSharedFlow`/`MutableStateFlow`, like NiA's test repositories), plus `MainDispatcherRule` and
`com.hirehop.core.testing.data` sample objects (`sampleProfile`, `sampleApplication`).

## 5. Domain interfaces (`:core:domain`, package `com.hirehop.core.domain`)

```kotlin
interface JobDescriptionAnalyzer { fun analyze(rawText: String): JobDescription }
interface GapMatcher { fun match(profile: CandidateProfile, job: JobDescription): GapAnalysis }
interface ResumeTailor { fun tailor(profile: CandidateProfile, job: JobDescription, gap: GapAnalysis): TailoredResume }
interface FabricationGuard { fun check(proposedText: String, sources: List<EvidenceBullet>, profile: CandidateProfile): List<GuardrailViolation> }
interface ResumeTextParser { fun parse(rawText: String): CandidateProfile }
```

Use cases (each a class with `operator fun invoke`, injected with `@Inject constructor`):
`AnalyzeJobUseCase` (profile + raw JD -> `JobDescription` + `GapAnalysis`),
`TailorResumeUseCase` (tailor, then run the guard on every bullet; a bullet with violations falls back to the
original text with an empty `editTypes`), `CreateApplicationUseCase`.

Offline implementations use a keyword dictionary (skills, tools, degrees, soft skills) plus alias
normalisation (`"js" -> "javascript"`, `"k8s" -> "kubernetes"`). Hilt `@Binds` modules bind interfaces to
the offline implementations.

## 6. Navigation

Copy NiA Navigation 3: `NavKey` objects/data classes in each feature `api` module; `EntryProvider`
extension in each `impl` module; `core:navigation` `Navigator`/`NavigationState`. Top-level destinations:
Applications (start) and Profile. Analysis and Tailor are pushed on top:

- `ApplicationsNavKey`, `ApplicationDetailNavKey(applicationId: String)`
- `ProfileNavKey`
- `AnalysisNavKey` (new application from a pasted JD)
- `TailorNavKey(applicationId: String)`

## 7. Build and host rules

- Gradle wrapper version: same as NiA (`gradle-9.7.1`). Put `org.gradle.workers.max=3` in
  `gradle.properties`. Other agents build at the same time on this shared CI host.
- Run `./gradlew --stop` when you finish.
- Do not add Firebase, benchmarks, baseline profiles, screenshot tests, jacoco, dependency-guard, flavors, or
  the NiA lint module. Add Spotless with ktlint, as NiA does, but with no license header step.
- Verify before you push: `./gradlew assembleDebug testDebugUnitTest spotlessCheck` must pass.
