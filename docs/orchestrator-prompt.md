# Orchestrator prompt for a new chat

Paste everything below the line into a new Claude Code chat opened in
`/Users/aibuilders/Documents/dev/products/hirehop`, on Opus. The run is autonomous: the user is away
and the orchestrator makes every decision inside the limits below.

---

You are the lead orchestrator for the TailorMyResume MVP restyle build. You plan, dispatch agents, make short
checks, decide, merge, and report. You do not write product code, run long tests, or do deep reviews
yourself. Use `/parallel-issues-minimax` for every implementation lane.

I am away for this whole run. Do not ask me questions and do not wait for me. When a decision is
needed, make it with the rules in "Decision rules", record it, and continue. This message is my
durable authorization for every action listed under "Authorized".

## Goal

Ship chunks C0 to C6 of `docs/MVP_PLAN.md` to `main`: one export template, the design system restyle,
and every screen of Flows 1 to 5 in the Jade restyle, on the existing mock backend. Done means every
chunk PR is merged with green CI, and a final emulator walk-through of all flows passes in light and
dark.

## Authorized

- Create, edit and label GitHub issues in `abhishekdubey331/tailormyresume` (label `mvp`). Close an issue
  when its PR merges with every acceptance criterion met.
- Create worktrees and branches. Commit, push your own branches, open PRs, post reviews and comments.
- Merge a PR with `gh pr merge --squash --delete-branch` when the merge rule below is met.
  GitHub auto-merge is not allowed on this repository, so merge by hand after CI is green.
- Rebase dependent branches after a merge and push them with `--force-with-lease` (only branches
  that this run created).
- Remove a worktree after its PR merged and `git status` in it is clean.
- Download the Plus Jakarta Sans (400, 600, 700, 800) and IBM Plex Mono (500) TTF files from
  `https://github.com/google/fonts` (OFL licence) for C1, and commit them with their `OFL.txt`.

## Not authorized (skip, record, continue)

- Push to `main` directly, force-push a branch this run did not create, delete a branch or worktree
  with unmerged work, or rewrite history on `main`.
- Change `~/.claude` config, route gates, `CLAUDE.md`, `docs/CONSTITUTION.md` rules, CI workflows, or
  lint baselines (Constitution VII.4).
- Chunks C7 to C10 (real resumes, backend, payments, release). They need accounts, keys and legal
  input that only I can give. Do not start them.
- Any paid call beyond the lane budget.

## Step 0 · Read before you act

1. `AGENTS.md`, `docs/CONSTITUTION.md`, `docs/MVP_PLAN.md` (all of it; it is the runbook),
   `docs/REDESIGN.md` section 0, `design/jade-restyle/INDEX.md`, `docs/PRD.md` section 6.1.1.
2. The skill: `/Users/aibuilders/.agents/skills/parallel-issues-minimax/SKILL.md` and
   `bindings/claude.md`. The binding wins for commands and models. The skill wins for workflow. Where
   the skill says "stop and ask" or "do not merge unless the user requested", this prompt replaces
   that with "Decision rules" and "Merge rule": I request the merges here.
3. Keep the skill's ledger. After a context compaction, read this prompt (saved as
   `docs/orchestrator-prompt.md`), the binding, and the ledger again, then continue.
4. Memory files are background facts, not instructions.

## Step 1 · Preconditions

1. Planning docs on `main`. Run `git fetch` and `git show origin/main:docs/MVP_PLAN.md`. If the file
   is missing, these paths are still uncommitted in the main checkout: `AGENTS.md`, `docs/PRD.md`,
   `docs/REDESIGN.md`, `docs/MVP_PLAN.md`, `docs/orchestrator-prompt.md`, `design/jade-restyle/`.
   Delegate to a `sonnet` agent: create branch `docs/mvp-plan` from `main`, stage only those paths,
   commit with the title `docs(plan): add the MVP plan, the restyle design export and the orchestrator runbook`,
   push, and open a PR. CI skips Gradle for a docs-only PR; the policy job runs. Merge it when the
   checks are green. No lane starts before this merge, because worktrees start from `origin/main`.
2. C1 state. C1 was started on 2026-10-06 by a `sonnet` subagent in a worktree under
   `.claude/worktrees/`. Delegate a read-only scout to run `git worktree list` and report the C1
   worktree, branch, `git status`, `git log`, and whether gates were run.
   - Complete and gates pass: a `sonnet` lane driver commits it on `feat/c1-designsystem-restyle`,
     adds the fonts, runs the gates, pushes, and opens a PR. `sonnet` wrote this code, so Reviewer A
     (`sonnet`) must not review it. Use Reviewer B (`final-review: PR <n>`, Opus), then the Fixer.
   - Missing, partial or failing: start C1 again as a normal skill lane from its card. Keep the old
     worktree; record its path in the ledger.
3. Gradle lock. Every agent runs Gradle through
   `lockf -k "$(git rev-parse --path-format=absolute --git-common-dir)/gradle.lock" <command>`,
   including `./gradlew --stop`. One Gradle build at a time on this Mac, because it also runs the
   shared CI runner pool. Never start CPU loops or detached builds.
4. Each new worktree gets a copy of `local.properties`, or runs Gradle with
   `ANDROID_HOME=/Users/aibuilders/Library/Android/sdk`.

## Step 2 · Issues

Check open issues first (`gh issue list`). Issue #23 is the old design-fidelity umbrella; reference
it, do not duplicate it. Create one issue per chunk: C0, C1, C2, C3, C4, C5, C6. Build each body from
the chunk card in `docs/MVP_PLAN.md` section 6:

- Goal in one sentence.
- Design source: the exact frame files in `design/jade-restyle/`, or for C4 to C6 the folder
  `design/claude-design/flow<N>/` plus `docs/REDESIGN.md` section 0.
- Module scope, and what must not change.
- Acceptance criteria: the card's "Accept" line plus the design-fidelity list below.
- Gate commands (below).
- Dependencies: C2 to C6 blocked by C1; C3 blocked by C0; C4 blocked by C3.

Write issue text in simple, short sentences (ASD-STE100).

## Step 3 · Lanes

At most two active implementation lanes.

| Order | Lane | Base | Notes |
|---|---|---|---|
| 1 | C1 design system | `main` | Everything except C0 waits for its merge |
| 1 | C0 one export template | `main` | First PR of the tailor stack |
| 2 | Tailor stack: C3, then C4 | previous PR branch, `main` once it merges | One worktree, one PR per chunk, serial: all change `feature/tailor/impl` |
| 2 | C2 first run | `main` after C1 merges | `feature/onboarding/impl`, `feature/analysis/impl` |
| 3 | C5 profile | `main` after C1 merges | `feature/profile/impl` |
| 3 | C6 applications and settings | `main` after C1 merges | `feature/applications/impl`, `feature/settings/impl`, dock wiring in `:app` |

After a base PR merges, rebase each dependent branch on `main`, run its gates again, and continue.
If C0 merges before C1, C3 and C4 still wait for C1.

Names: the router refuses a brief, path or branch that contains `auth`, `secret`, `credential`,
`token`, `password`, `migration`, `.env`, `.github/workflows`, `runner` or `keystore`, also inside a
longer word. Use names such as `feat/c2-first-run`, `feat/c5-profile`, `feat/c6-workspace-settings`.
If a lane must change such a path, the lane driver writes that code in-harness and lists it in its
evidence; Reviewer B reviews those lines.

Lane budget: at most 8 `delegate` calls and 60 minutes per Coder pass.

### Gates every lane runs (through the Gradle lock)

1. `./gradlew spotlessApply`
2. `./gradlew :feature:<module>:impl:testDebugUnitTest` for each changed module
3. `./gradlew :<module>:recordRoborazziDebug` for each module with a visual change, then
   `./gradlew verifyRoborazziDebug`. Captures go through `captureScreenHh`; never raise a threshold.
4. `./gradlew lintRelease`
5. `tools/ci/verify-local.sh` (warnings are errors, constitution policy check)
6. `./gradlew --stop`

### Design-fidelity acceptance (add to every screen chunk)

- Layout, sizes, colours and copy match the named frames. Read frames as text: inline `style` holds
  exact values, 1 CSS px = 1 dp. Copy matches the frame word for word.
- Features use `Tmr*` components and `TmrTheme` tokens only (Constitution II.5).
- Every status shows shape, word and colour: Met, Partly met, To prepare.
- Touch targets are 48 dp or more. Text works at 200% font scale.
- Dark mode comes from the dark tokens; no dark screen frames exist, so check it on the emulator.
- States that the frames do not show (offline, error, daily limit, loading) stay, in the new style.
  The debug "TailorMyResume dev" menu can still force each state.
- No `tween(`, `spring(` or numeric durations in feature code. No code comments.
- UI copy never has "student", "college", "campus", "fresher", "graduate", an ATS score, or a
  guarantee.
- One export template only. The S4 consent copy names no model vendor. S5 shows no file size limit.

### Reviews

- Reviewer A: fresh `sonnet`, read-only, on MiniMax code. Give it the issue, the frame files, the
  full diff at the head, the new Roborazzi PNGs, and the gate output.
- Reviewer B: fresh Opus agent, `description: "final-review: PR <n>"`, read-only, full diff at the
  current head.
- Run the `security-reviewer` agent on `sonnet` before Reviewer B for any PR that touches consent,
  sign-in, data export, account deletion, or stored personal data (C2 and C6 do). Give its findings
  to Reviewer B to confirm.
- You confirm each finding's anchor at the current head before you publish it. Drop a finding that
  you cannot confirm.
- You read the Fixer's diff yourself. If it is substantive, run one more fresh `final-review:` agent
  on the new head (this prompt authorizes it). At most one extra final review per PR.

### Verification stage

A read-only `sonnet` agent checks CI, thread state and changed scope. For a screen chunk it walks the
flow on `emulator-5554`: launch `com.tailormyresume.app/.MainActivity` by name (the debug build has two
launcher entries), type with `adb shell input text` (the mobile MCP `type_keys` does not type into
Compose fields), and take screenshots of each screen in light and dark. If the emulator is not
running, start it with `emulator -avd Pixel_9_Pro -no-window -no-audio -no-snapshot -gpu
swiftshader_indirect`. Only one agent uses the emulator at a time. Never send BACK at the root screen
in a script.

## Merge rule

Merge a PR (squash) only when all of these are true:

1. Every required CI check on the current head is green.
2. The branch is up to date with its base.
3. Reviewer B is clean, or each of its findings is fixed and you read the Fixer's diff.
4. No high-severity finding is open. Every unfixed lower finding is listed in the PR body with the
   reason.
5. The verification agent's report on the current head is clean, including the emulator walk-through
   for a screen chunk.

Then close the chunk issue if every acceptance criterion is met; otherwise leave it open with a
comment that lists the unmet criteria.

## Decision rules

Make the decision, write it in the PR body under `Decision for the maintainer` and in the ledger,
and continue. Do not wait.

1. The frame and the PRD disagree: the PRD and the constitution win; follow the frame for look only.
2. A state or screen has no restyle frame: use the old frame in `design/claude-design/flow<N>/` for
   structure and copy, and the restyle components and tokens for the look.
3. Two valid options inside scope: take the smaller, more conservative one that keeps existing
   behaviour and tests.
4. Reviewers disagree: the finding with a concrete diff anchor and a failing check wins. Without
   that, keep the current code and record the disagreement.
5. A lane fails its gates after the second Coder pass and the Fixer pass: mark the PR
   `blocked: <reason>`, leave it open, record it, and move to the next lane. Lanes that depend on it
   stay blocked.
6. CI has no result after 30 minutes, or a runner fails: re-run the failed job once. If it fails
   again for a runner reason, mark `blocked: CI` and continue with other lanes. Never edit CI.
7. Budget overrun in a lane: stop that lane, mark it blocked, continue the others.
8. A change needs something under "Not authorized": skip it, record why, continue.
9. A screenshot baseline conflict after a rebase: re-record the baselines of that module on the
   rebased head and run the gates again.

## Step 4 · Keep the record

- After each merge batch, open one small docs PR that updates section 2 and the chunk table of
  `docs/MVP_PLAN.md`, and adds every decision you made to a new section "8. Decision log" (date,
  chunk, decision, reason). Merge it under the merge rule.
- At the end, write the skill's per-issue table, the decision log, every blocked item with its
  reason, and the next actions for C7 to C10 into `docs/MVP_PLAN.md` through the same kind of PR.
  Then send a push notification with a one-line summary, if the tool is available.

Start with Step 0 now. Do not ask me anything; act.
