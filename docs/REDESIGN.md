# HireHop redesign

## Current direction — 2026-10-07

The user requested a full Avvio-inspired restyle. `docs/AVVIO_REDESIGN.md` and the current
`docs/DESIGN_SYSTEM.md` replace the visual rules below, including the Jade palette, typography,
decorations, and hero layouts. The remaining behavior, navigation, accessibility, and build rules
still apply. The sections below preserve the previous design reference.

This document binds every agent that works on the redesign. `docs/CONSTITUTION.md` wins a conflict.
`docs/ARCHITECTURE.md` comes second. This document comes third.

## 0. Restyle of 2026-10-06 (wins over sections 2 to 4)

The user kept Jade and added the playful layout of a job-app reference. The source is
`design/jade-restyle/` (read its `INDEX.md`). The build plan is `docs/MVP_PLAN.md`.

| Topic | Rule |
|---|---|
| Source | `design/jade-restyle/` for foundations, Flows 1 and 2. `design/claude-design/flow3` to `flow5` for structure and states of S12 to S24, drawn with the restyle tokens and components |
| Accents | Coral #B94C37 (white text) and marigold #FFC94D (ink text) beside Jade #0B7A5C. Blue #2B5FA8 only for "Partly met" |
| Shapes | Cards 28 dp. Coloured hero header with 48 dp bottom corners. Pill buttons 60 dp tall. Round icon buttons 48 dp |
| Colour cards | Solid coral, jade or marigold cards with a white round monogram, for applications and choices |
| Pill rows | Full pill list rows, 72 dp tall, 36 dp radius, solid colour |
| Bottom bar | A round 60 dp secondary button next to a 60 dp primary pill |
| Dock | Floating ink pill inset from the screen edges, three icons with labels. The active item is bright jade |
| Decoration | Thin hand-drawn squiggles, rings and dots in coral, marigold and jade around illustrations and in headers |
| Status | Unchanged: shape plus word plus colour. Met, Partly met, To prepare |

## 1. Goal

1. Replace the Evidence-Editorial look with the "Friendly hero, Jade" design in every screen.
2. Make the app work end to end with no real API. A mock backend sits behind stable interfaces.
3. Leave no dead end in navigation.

## 2. Design source

| Need | File |
|---|---|
| Tokens: type, colour, surface, shape, spacing, elevation | `design/claude-design/foundations/Main.dc.html`, `Colour.dc.html`, `Surface.dc.html` |
| Motion registers `proof` and `hop` | `design/claude-design/foundations/Motion.dc.html` |
| Characters and the 6 spot poses | `design/claude-design/foundations/Illustration.dc.html` |
| Components, light and dark | `design/claude-design/foundations/ComponentsLight.dc.html`, `ComponentsDark.dc.html` |
| Screens of flow N | `design/claude-design/flow<N>/INDEX.md`, then the `f<N>-*.dc.html` frames it lists |

Read a `.dc.html` file as text. Each frame is 360 x 800 dp. Inline `style` attributes hold the exact
sizes, colours, and radii. One CSS `px` equals one `dp`, and a font size in `px` equals `sp`.

The design is the acceptance criterion. If the design and the old code disagree on layout or copy,
the design wins. If the design does not show a state, keep the behaviour of the old code and use the
new components.

## 3. Layout patterns

| Pattern | Use | Rule |
|---|---|---|
| Home header | Welcome and the three top-level tabs | Jade header, two soft circles, greeting, credits pill, bold headline, outline pill button. The woman hero pose stands in front of the sheet |
| Sheet | Below a home header | Surface with 28 dp top corners that overlaps the header |
| Inner header | Every pushed screen | Shorter jade header, 48 dp tinted circular back button with a white chevron (`HhBackButton`), centred title and subtitle. Hero cards (24 dp radius) overlap it |
| Dock | The three top-level tabs only | Floating `tool` pill, inset from the screen edges. Each item is an icon above a label; the active item is `onToolSelected`. Never on onboarding or pushed screens |
| Bottom action bar | Screens with 1 to 3 main actions | `tool` bar. Content has an inset, so the bar never covers content |
| Status | Met, partly met, to prepare | Shape plus word plus colour. The gap word in the UI is "To prepare" |

Do not use stock Material chrome: no centred top app bar, no pill-indicator navigation bar, no FAB.

## 4. Token migration

`core:designsystem` holds the new tokens. Old token names stay as aliases until every feature is
migrated. A feature agent must use only the new names. The cleanup step deletes each alias that has
no caller.

| Area | New names (see the board for values) |
|---|---|
| Colour | `header`, `headerShape`, `onHeader`, `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer`, `background`, `surface`, `card`, `ground`, `document`, `tool`, `onTool`, `outline`, `outlineVariant`, `outlineSoft`, `onSurface`, `onSurfaceVariant`, `body`, `inverseSurface`, `inverseOnSurface`, `met`, `partial`, `gap`, `evidence`, `evidenceLine`, `error`, `onError`, `errorContainer`, `onErrorContainer`, `special`, `onSpecial`, `scrim` |
| Type | `displayL`, `displayM`, `headlineL`, `headlineM`, `titleL`, `titleM`, `titleS`, `bodyL`, `bodyM`, `labelL`, `labelM`, `numeralHero`, `numeralM`, `factId` |
| Shape | `sheet` 28, `heroCard` 24, `card` 20, `field` 12, `pill` |
| Motion | `proof` and `hop` spring specs, with the reduced-motion fallback |

Fonts: Plus Jakarta Sans 400, 600, 700, 800 and IBM Plex Mono 500. Until the font files are in
`res/font`, the font families are defined in one place and point to the old files.

## 5. Mock backend

The app has no network code (Constitution I.5). The "offline" implementations are the mock backend.

1. Every backend concern sits behind an interface in `core:domain` or `core:data`. A screen or a
   ViewModel never builds display data inline.
2. A Hilt module binds each interface to its mock implementation. A real implementation replaces
   the binding later. No UI code changes.
3. Every stateful mock is `@Singleton` and stores its state on the device, so all screens see the
   same credits, account, and consent after a process restart.
4. The mock backend has a seeded sample dataset: one candidate profile, four applications with
   analysis and tailored resume, credits, and one purchase. The debug developer menu loads and
   resets it.
5. `DebugScenario` stays the only way to force loading, empty, error, and offline states.
6. Each mock has a contract test in `core:testing` that a real implementation can reuse.

## 6. Navigation contract

| From | Action | To |
|---|---|---|
| App start | Onboarding not complete | Welcome |
| App start | Onboarding complete | Applications |
| Welcome | Paste a job description | Paste JD |
| Welcome | Import my resume | Sign in, then Consent, then Import resume |
| Welcome | Build your profile step by step | Guided form |
| Paste JD | Analyse this JD | Next onboarding step with the JD kept. Signed out: Sign in. No consent: Consent. No confirmed fact: Import resume. Else Gap analysis |
| Sign in | Success | Next onboarding step |
| Consent | Agree and continue | Record the consent, then the next onboarding step |
| Import resume | Review facts | Confirm your facts |
| Confirm your facts | Continue to my analysis | Gap analysis if a JD is kept, else Paste JD |
| Gap analysis | Tailor my resume | Create the application, mark onboarding complete, open Tailored resume review |
| Tailored resume review | A changed line | Bullet review |
| Tailored resume review | Preview export | Export preview |
| Export preview | Download with a credit | Exported |
| Export preview | Download with no credit | Application pack, then back to Export preview |
| Exported | Done | Application workspace |
| Applications | A row | Application workspace |
| Applications | New application | Paste JD |
| Application workspace | Resume, cover letter, prep questions, export | Tailored resume review, Cover letter, Prep questions, Export preview |
| Profile | A fact | Fact editor |
| Profile | Add evidence, guided form, import | Evidence path, Guided form, Import resume |
| Settings | Credits and help | Credits. Credits opens Application pack |
| Settings | Your data, consent notice, delete account | Your data, Consent (read only), Delete account |
| Settings | Sign out | Sign out in the mock backend, then Welcome |
| Delete account | Last step done | Account deleted (Done), on top of Welcome. "Back to Welcome" and back lead to Welcome |

Every screen has a working back action. No click handler is empty.

Two roots. The app shell shows one of two navigation roots, and it switches when the session's
"onboarding complete" flag or the signed-in account changes (Main needs both):

- **First-run root**: one back stack that starts at Welcome. No dock.
- **Main root**: the three tabs (Applications, Profile, Settings) with the dock on the tab screens only.

Each root has its own `ViewModelStore` (`RootViewModelStores` in `app`). The store of a root is cleared when the app switches to the other root. A configuration change keeps it. So Paste JD after a sign-out and a new sign-in starts with a new ViewModel. `PendingNavigation` also serves the first-run root: Delete account sets `AccountDeletedNavKey` before it runs, and `HhFirstRunRoot` pushes it on top of Welcome.

A feature never resets a back stack itself. To leave the first-run root, call
`SessionRepository.markOnboardingComplete()`. To return to it, sign out or delete the account
(sign out keeps the consent and the onboarding flag, so the next sign-in returns to Applications; delete account clears the whole session). When the main root must open with screens on top of Applications, call
`PendingNavigation.set(listOf(TailorNavKey(applicationId)))` (in `core:navigation`) just before
`markOnboardingComplete()`. The shell pushes these keys when it builds the main root. Inside the main
root, the same screens (Paste JD, Gap analysis, Import resume, Guided form) are pushed on the current
tab, and `NextOnboardingStepUseCase` still decides the next screen.

## 7. Rules for every agent

1. Write no code comments and no KDoc (Constitution III.2).
2. Put every UI string in the module's `strings*.xml` with the module resource prefix. Use plain,
   short sentences. Never write "ATS score", a guarantee, or the words "student", "college",
   "campus", "fresher", or "graduate" in UI copy. Sample resume facts may use them.
3. Features use only `Hh*` components and `HhTheme` tokens. No raw Material component, no
   `MaterialTheme.*`, no `Color(0x...)`, no `RoundedCornerShape` literal, and no raw `dp` or `sp`
   literal where a token exists.
4. Keep unidirectional data flow: stateless `...Screen`, a `...Route` that collects state, one
   sealed `UiState` per ViewModel.
5. No `!!`, no `GlobalScope`, no `runBlocking`, no hard-coded dispatcher, no mocking library.
6. Every behaviour change has a unit test with fakes from `core:testing`. Every ViewModel keeps
   its test class.
7. Run Gradle only through the lock wrapper that your brief names. It serialises builds, because
   this Mac also runs a shared CI runner pool. Run only the tasks of the modules that you own.
8. Edit only the files that your brief assigns to you. Do not commit, push, or stash.
9. If a build fails in a module that another agent owns, wait and try again. Do not edit it.
10. Re-record the screenshot baselines of your module after the UI change, then run the verify task.

## 8. Procedure for a feature work package

1. Read `docs/DESIGN_SYSTEM.md` (components and tokens) and `docs/MOCK_BACKEND.md` (data contracts).
2. Read the `INDEX.md` of your flow. It lists each screen, each state, and the frame files.
   Read the light frame of each state as text. Read a dark frame only to check a colour role.
3. For each screen, rewrite the `...Screen` composable and its parts to match the frames: layout,
   order, spacing, radii, type styles, copy, and every state in the index that the code can reach.
   Use `HhScreen` with `HhInnerHeader` or `HhHomeHeader`. Use the new token names only.
   A frame marked "system" (Google account picker, file picker, share sheet, notification shade,
   Play purchase sheet) is Android UI. Do not build it.
   A `{{PLACEHOLDER}}` in a frame is copy that is not written. Show a string resource with a
   clear neutral value, as the old code does for missing addresses.
4. If the catalogue has no component for a part, build the part in your module from Compose
   foundation primitives and `HhTheme` tokens. List each such part in your report. Do not edit
   `core/designsystem`.
5. Keep the ViewModel logic unless the design, the navigation contract (section 6), or the mock
   backend document needs a change. Remove display data that a ViewModel or a screen builds inline;
   read it from a repository, a gateway, or a use case.
6. Fix the navigation of your screens to match section 6. Every button has an effect.
7. Update the unit tests. Update the screenshot tests so that they cover each state in the index
   that the screen renders. Delete the old baseline images of your module, record new ones, then
   run the verify task. Open at least four recorded images (two screens, light and dark) and
   compare them with the frames. Fix what differs.
8. Motion: use `HhTheme.motion.proofSpecs` for transitions and `hopSpecs` only where the flow
   prompt allows it. Honour the reduced-motion flag. Do not build the multi-frame animations
   ("premium move") beyond a simple enter or state transition.
9. Accessibility: 48 dp touch targets, a content description on each icon-only control, merged
   semantics for a card that TalkBack must read as one unit.

Gradle: builds on this machine run one at a time. Run the lock wrapper with `run_in_background`
and wait for the completion notice. A build can wait a long time for its turn. Write the output to
a log file in your scratchpad and read only the end of it. Run only tasks of your own module, for
example `:feature:tailor:impl:compileDebugKotlin`, `:feature:tailor:impl:testDebugUnitTest`,
`:feature:tailor:impl:recordRoborazziDebug`, `:feature:tailor:impl:verifyRoborazziDebug`,
`:feature:tailor:impl:lintDebug`, and `:feature:tailor:impl:spotlessApply`. Never run `spotlessApply`
or `assembleDebug` for the whole repo, and never run `./gradlew --stop`.

## 9. Work packages

| Package | Owner scope |
|---|---|
| DS | `core/designsystem`: tokens, theme, components, component catalogue in `docs/DESIGN_SYSTEM.md` |
| ILLUS | `core/designsystem` illustration files only: the 7 character poses as vectors |
| MOCK | `core/data`, `core/domain`, `core/database`, `core/testing`, `core/model`: mock backend, seed data, new interfaces |
| SHELL | `app`, `core/navigation`, `core/ui`: dock, start destination, developer menu |
| FLOW 1 | `feature/onboarding`, `feature/analysis` |
| FLOW 2 and 3 | `feature/tailor` |
| FLOW 4 | `feature/profile` |
| FLOW 5 | `feature/applications`, `feature/settings` |
| VERIFY | Whole repo: `tools/ci/verify-local.sh`, alias cleanup, docs |
