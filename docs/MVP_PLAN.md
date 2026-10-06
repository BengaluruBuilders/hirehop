# MVP build plan and orchestrator runbook

Last update: 2026-10-06. This file lets a new agent take over as orchestrator and build the HireHop
MVP. Read it from top to bottom before you act. When this file and `docs/CONSTITUTION.md` disagree,
the constitution wins. Update section 2 and the chunk table each time a chunk changes state.

## 1. Sources of truth, in order

1. `docs/CONSTITUTION.md`: rules and their CI gates.
2. `docs/PRD.md`, section 6.1.1: the locked MVP scope.
3. `design/jade-restyle/` (see its `INDEX.md`): the new look, for foundations, Flow 1 and Flow 2.
4. `design/claude-design/flow<N>/`: screen structure, states and copy for Flows 3 to 5, and for
   states that the restyle export does not show. Apply the restyle tokens and components to them.
5. `docs/ARCHITECTURE.md`, `docs/DESIGN_SYSTEM.md`, `docs/MOCK_BACKEND.md`, `docs/REDESIGN.md`.
6. `AGENTS.md`: commands and motion rules.

## 2. Current state

- `main` has all 24 screens on the on-device mock backend. PDF and DOCX export, the share card
  (`HhFitShareCard`), prep questions and the cover letter exist. Two export templates exist
  (`ExportTemplate` in `feature/tailor/impl/.../document/`); the MVP keeps one.
- The app has no network code. Constitution I.5 forbids it until an amendment.
- Not committed yet on `main`: `docs/PRD.md` (MVP lock), this file, `design/jade-restyle/`.
- C1 finished on 2026-10-06 in the worktree
  `.claude/worktrees/agent-a1e7062fb357f0b2f` (branch `worktree-agent-a1e7062fb357f0b2f`). Nothing
  is committed. About 500 files: theme, 17 edited and 3 new `Hh*` components (`HhSolidCard`,
  `HhPillRow`, `HhDecoration`, plus `HhIconActionBar`, `HhStatusRow`, `HhInkButton`), 501 PNG
  baselines, `docs/DESIGN_SYSTEM.md`. The agent reports that all gates passed, including
  `tools/ci/verify-local.sh`. Sonnet wrote it, so only an Opus `final-review:` agent reviews it.
  Points for that review:
  1. `error` is now coral #B94C37, the same as the new `coral` accent. Check that error and
     destructive states still read as errors.
  2. Buttons are 60 dp everywhere, also inside cards. Screen chunks need a compact size; add it in
     C1's fix pass or as the first step of C2.
  3. The dock keeps its animated notch; the raised white-ringed circle is approximated.
  4. Not done: pressed jade #064D3A, dark sheet-item border #3A404A, the fonts (no download).
  5. `verifyRoborazziDebug` ran before the last header-corner fix; `verify-local.sh` ran after.

## 3. Locked decisions

| Decision | Value |
|---|---|
| Look | Jade (#0B7A5C) stays the brand. The reference adds coral #B94C37, marigold #FFC94D, 28 dp cards, 48 dp hero bottom corners, 60 dp pill buttons, pill rows, squiggles |
| Home | Applications home with colour cards for saved applications. No job listings, no job search |
| MVP features | F1 to F10 as in PRD 6.1.1: import, gap check, tailoring, PDF and DOCX export with 1 template, applications, prep questions, optional cover letter, free tier plus a 5-application pack at ₹149, account and consent, share card only |
| Cut | Templates 2 and 3, referral credit, group code, ₹49 single application, reminders, kanban |
| Order | Restyle on the mock backend first (C0 to C6), R0 spikes in parallel (C7), then the I.5 amendment and the real backend (C8 to C10) |
| Copy | Never "student", "college", "campus", "fresher", "graduate". Never an ATS score or a guarantee. ASD-STE100 for all prose |

## 4. Design work still open

Flows 3, 4 and 5 have no restyle frames. A screen chunk for these flows can start from the old
frames plus the C1 components. If the user wants restyle frames first, run Claude Design:

1. Open the canvas https://claude.ai/artifact/766P2hFnsULQbnXVeKj3rM chat in the built-in browser
   pane. The chat is "HireHop Android app design" in https://claude.ai/artifacts/design.
2. Type in the reply box. Shift+Enter makes a new line. Enter sends. A run takes 25 to 35 minutes.
   The browser pane cannot upload images, so describe references in words.
3. Send one flow per message. Use this text, changed for the flow:
   "Design Flow 3, export and payment, light mode, same canvas, same tokens and components, one
   artboard per screen state, in a new row titled Flow 3. S12 Export preview: rendering, ready
   with 1 free credit, PDF selected, DOCX selected, no credit, making the file, render error.
   One template only, no template switch. The paywall covers the download, not the preview.
   S13 Application pack: 5 applications for ₹149, credits never expire, Google Play sheet,
   pending, success, cancelled, failed. S14 Exported: credit used, share sheet, mark as Applied.
   S15 Credits and help: no purchases, purchases, refund and help in plain words.
   Same rules: no ATS score, no job listings, never the words student, college, campus, fresher
   or graduate."
   Flow 4: S16 Profile, S17 Fact editor, S18 Guided form, S19 Evidence path (states in
   `design/02-screens.md`). Flow 5: S20 Applications home (colour cards for in-progress
   applications, pill rows for exported ones, dock), S21 Application workspace, S22 Settings,
   S23 Your data, S24 Delete account.
4. Export: read each new file with the Artifact tool (`read`, `paths: ["project/<file>"]`), copy it
   into `design/jade-restyle/`, and add its rows to `INDEX.md`.
5. Review the frames before code: the copy rules in section 3, PRD scope, shape plus word plus
   colour for every status, 48 dp touch targets.

The user's Claude Design plan showed 75% of the weekly limit used on 2026-10-06.

## 5. How to orchestrate

The ready-to-paste prompt for a new orchestrator chat is `docs/orchestrator-prompt.md`. It runs
the screen chunks through the `/parallel-issues-minimax` skill.

### 5.1 Roles

- The orchestrator (Opus) plans, writes each brief, reviews every diff, runs the gates, and
  reports. It does not write bulk code.
- Implementation: an in-harness subagent with `model: sonnet` and `isolation: worktree` for
  multi-file Kotlin work. Use `~/.claude/bin/delegate impl --out <file> "<brief>"` (MiniMax) for a
  single new file or a test file. Use `delegate tests --out` for test authoring.
- Review: Opus reviews code that Sonnet wrote. A Sonnet subagent may review code that MiniMax
  wrote. The last review before merge may run on an Opus subagent whose description starts with
  `final-review:`.
- Security: any diff in C8, C9 or C10 that touches sign-in, tokens, the database, billing or
  personal data stays in-harness and gets the `security-reviewer` agent.

### 5.2 Rules for every chunk

1. One chunk is one branch and one PR (Constitution VI.3). Branch names: `feat/c<N>-<slug>`.
2. Only one Gradle build runs at a time on this Mac, because it also runs a shared CI runner pool.
   Serialise lanes with `lockf -k <scratchpad>/gradle.lock <command>`. Run `./gradlew --stop`
   through the same lock. Never start CPU loops. Keep `org.gradle.workers.max=3`.
3. A new worktree has no `local.properties`. Copy it, or set
   `ANDROID_HOME=/Users/aibuilders/Library/Android/sdk`.
4. Screenshot captures go through `captureScreenHh` in `core/screenshot`. It fixes a Robolectric
   4.16 rounded-corner race. Never raise thresholds to hide drift.
5. A visual change re-records the baselines of each module it changes. A motion change does not.
6. No code comments (Constitution III.2). No mocking library (IV.2). Features use `Hh*` only (II.5).
7. Gates before a PR: `./gradlew spotlessApply`, the module tests, `verifyRoborazziDebug`,
   `./gradlew lintRelease`, then `tools/ci/verify-local.sh` (warnings are errors there).
8. Ask the user before a push, a PR, or a merge. GitHub auto-merge is not allowed here.
   PR title: `type(scope): summary`. PR body: what, why, how verified, in simple English.
9. Verify UI on the emulator `emulator-5554`. Launch `com.hirehop.app/.MainActivity` by name
   (the debug build has two launcher entries). Mobile MCP `type_keys` does not type into Compose
   fields; use `adb shell input text`.

### 5.3 Brief template for an implementation subagent

Give each brief: the goal in one sentence; the files to read first (this file, the constitution,
the frames by path); the exact module scope; what must not change; the acceptance criteria from
the chunk card; the gate commands; the Gradle lock and `ANDROID_HOME` notes; "do not commit or
push"; and the report format (files changed, gate results, open questions, under 400 words).

## 6. Chunks

| # | Chunk | Depends on | Size | State |
|---|---|---|---|---|
| C0 | One export template | none | 2 h | Ready |
| C1 | Design system restyle | foundations boards | 2 to 3 days | Built, uncommitted; needs PR and Opus review (section 2) |
| C2 | Flow 1 screens | C1 | 2 days | Ready after C1 |
| C3 | Flow 2 screens | C1 | 2 days | Ready after C1 |
| C4 | Flow 3 screens | C1 | 1.5 days | Ready after C1 (old frames) |
| C5 | Flow 4 screens | C1 | 2 days | Ready after C1 (old frames) |
| C6 | Flow 5 screens | C1 | 2 days | Ready after C1 (old frames) |
| C7 | R0 spikes | none | 3 days | Ready, needs user input |
| C8 | I.5 amendment and backend | C7, user decision | 8 to 10 weeks | Blocked |
| C9 | Play Billing | C8 | 1 week | Blocked |
| C10 | Release readiness | C8, C9 | 3 weeks elapsed | Blocked |

C2 to C6 touch different feature modules, so they can run in parallel worktrees after C1 merges.
Only the Gradle builds are serialised.

### C0 · One export template

- Goal: PRD 6.5 item 3 says 1 template. Remove `ExportTemplate.COMPACT`, the template switch in
  `ExportPreviewScreen`, its action, its strings and its tests. Keep PDF and DOCX.
- Scope: `feature/tailor/impl` (`document/`, `exportpreview/`, `export/`), tailor tests and baselines.
- Check `ExportRecord.templateName` callers and the dev menu in `:app` for template references.
- Accept: no template choice on screen; both formats export; tests and gates pass.

### C1 · Design system restyle

- Goal: `core:designsystem` matches `design/jade-restyle/` foundations, light and dark.
- Work: token values (colour with a new coral role, shapes, spacing, type); restyle the `Hh*`
  components; add only what has no equivalent: solid colour card with monogram, pill list row
  (72 dp, 36 dp radius), squiggle set, bottom action bar (60 dp round secondary + 60 dp pill
  primary); dock as a full-width ink bar with 28 dp top corners, active icon in a raised jade
  circle. Keep public APIs; if one changes, fix every caller in the same PR.
- Do not touch motion. Update `docs/DESIGN_SYSTEM.md`.
- Accept: all gates pass; designsystem and feature baselines re-recorded; Opus reviewed the diff.

### C2 · Flow 1 screens (S1 to S7)

- Frames: `design/jade-restyle/S1*` to `S7*`. Modules: `feature/onboarding/impl` (welcome,
  pastejd, signin, consent, importresume, confirmfacts), `feature/analysis/impl`.
- New behaviour: S1 career choice ("Just starting out", "1 to 2 years in") with the line "We use
  this to pick which questions to ask about your projects and internships". Store it and use it to
  order the evidence path questions (S19). Step chips "Step 1 of 5" to "Step 5 of 5".
- S4 copy names OpenAI as the AI provider. S5 has no file size limit text.
- S3 "Continue with Google" needs Google's official mark before release (placeholder until C8).
- Keep existing states that the restyle does not show (offline, daily limit, errors).
- Accept: each frame matches; dev menu forced states still work; gates pass; emulator walk-through
  from Welcome to Gap check with share card.

### C3 · Flow 2 screens (S8 to S11)

- Frames: `design/jade-restyle/S8*` to `S11*`. Module: `feature/tailor/impl`.
- Every changed bullet shows its fact chip. Flagged verb keeps the user's verb. 2 regenerations
  per application. The cover letter is offered after the resume, with "Not now" of equal weight.
- Accept: as C2, walk-through from Tailor to all bullets reviewed, cover letter, prep questions.

### C4, C5, C6 · Flows 3, 4, 5

- Frames: `design/claude-design/flow3`, `flow4`, `flow5` for structure and states; restyle with
  C1 components. If section 4 produced restyle frames, those win.
- C4 module: `feature/tailor/impl` (`exportpreview`, `exported`, `packpurchase`, `credits`). One template only.
- C5 module: `feature/profile/impl` (profile, facteditor, guidedform, evidencepath).
- C6 modules: `feature/applications/impl`, `feature/settings/impl`, dock wiring in `:app`.
  Applications home uses colour cards for in-progress applications and pill rows for exported
  ones, as in the concept board https://claude.ai/artifact/E7cZe1woz5T2ZdM32skJLf.
- Accept: as C2, plus a full emulator walk-through of every flow at the end of C6.

### C7 · R0 spikes (PRD 12.2)

1. PDF text: export a resume with `AndroidPdfResumeRenderer`, run `pdftotext` on it, and compare the
   text with the approved JSON. Record the result in PRD open question 5.
2. Extraction: the user supplies 10 real resumes with consent. Measure field-level F1 of the
   extraction prompt. Do not commit the resumes.
3. Model cost: measure tokens per full application for each pipeline step and convert to ₹ with
   the rates in server config. Target ₹7 median.
- Output: `docs/research/07-r0-spikes.md`.

### C8 · Constitution amendment and backend (PRD 10.1)

Each item is its own PR, in this order. All stay in-harness with a security review.

1. Amendment PR: change I.5 to allow network access to the HireHop backend only, name the
   consent screen S4, keep backups off and analytics off unless the optional switch is on.
2. Google sign-in with Credential Manager; the ID token goes to Supabase Auth.
3. Supabase schema with row-level security on every table. Commit migrations.
4. Edge Functions as the model gateway, one async job per step: extract resume, analyse JD,
   tailor, verify, prep questions, cover letter. `store: false`. Model IDs and prices in config.
5. Guardrails: the deterministic checks of PRD 6.4 item 4.3 on the server, a verifier on a
   different model, one repair pass, fall back to the original bullet.
6. App side: replace each mock binding in `core:domain` with a backend binding (Constitution
   II.6), WorkManager for queued jobs and sync, Room stays the local source of truth.
7. Evaluation set of PRD 10.3 and its release gate (0 critical fabrications).
8. Server-side data export and deletion, the web deletion link, security logs for 1 year.

### C9 · Play Billing

Consumable product for the 5-application pack. Server-side purchase verification. Credits never
expire. Refund and help inside the app.

### C10 · Release readiness

Data safety form, privacy policy, terms, grievance contact, support email, web delete-account URL
(the user must supply the email and URLs), target API 36, Play Integrity, closed test with 12 or
more testers for 14 days, legal check of the consent copy.

## 7. Open items for the user

1. Real resumes for C7 step 2, with consent.
2. Support email, privacy policy URL, delete-account URL.
3. Personal or organisation Play developer account.
4. Restyle frames for Flows 3 to 5 now (section 4), or build them from the old frames.
5. Fonts: the app still ships Anek Latin, Bricolage Grotesque and JetBrains Mono
   (`core/designsystem/src/main/res/font`). Plus Jakarta Sans and IBM Plex Mono need the user's
   approval to download (OFL licence). C1 keeps the current files until then.
