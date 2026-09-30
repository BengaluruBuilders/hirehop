# PR 0 — UI foundations

> Planning artifact for the HireHop UI implementation task. Written during the planning phase, before any production code.

## Purpose

Establish the shared visual language, testing infrastructure, and offline-substitution seams that all five design flows depend on. No flow ships before this PR.

## Decisions taken during planning

| Question | Decision |
| --- | --- |
| Sign in with Google (Flow 1, screen 3) | Runtime fake behind a `SignInGateway` interface. No network, no `INTERNET` permission, no Constitution amendment. |
| Application pack purchase (Flow 3, screen 13) | Runtime fake behind a `PaymentGateway` interface. Same constraints. |
| Coverage and screenshot tooling | Amend the `docs/CONSTITUTION.md` adoption ledger inside PR 0 to move Kover and Roborazzi forward, with the justification recorded. |
| Fixture data | Set A is canonical: Priya Deshmukh, Pune, Northwind GCC, 18 facts = 15 confirmed + 3 user-stated. |
| DOCX export | Built and tested offline in the Flow 3 PR. |

## Flow order

Design order is 1 to 5, but Flow 1 depends on Flow 4, so the implementation order is:

1. **PR 0** — Foundations
2. **PR 1** — Flow 4: Profile and fact correction
3. **PR 2** — Flow 1: First run
4. **PR 3** — Flow 2: Tailoring review
5. **PR 4** — Flow 3: Export
6. **PR 5** — Flow 5: Workspace and account

Flow 1 lands after Flow 4 because the flow-1 "confirm your facts" screen needs the fact editor and evidence path that only Flow 4 specifies. Building Flow 1 first would make Welcome and Confirm-facts navigate to destinations that do not exist yet.

## Risks

1. **Kover on AGP 9.3.2 / Gradle 9.8.0 / isolated projects.** Unproven combination. Fallback is the AGP built-in `enableUnitTestCoverage`.
2. **Font vendoring.** The Board specifies Bricolage Grotesque 600, Anek Latin 400/600, and JetBrains Mono 400. Without those four files committed, no Roborazzi baseline can match the design.
3. **`gradlew.bat` line endings.** Commit `0a7405c` left a CRLF blob that conflicts with `*.bat text eol=crlf`. Out of scope for this PR.

## Known documentation drift

- `docs/ARCHITECTURE.md` still says Gradle 9.7.1. The wrapper is 9.8.0.
- `docs/ARCHITECTURE.md` section 6 lists five navigation keys. The design adds settings, your data, delete account, credits, and consent, so section 6 must be extended before the flow PRs land.
- `feature/tailor/impl/.../TailorApplicationStatus.kt` discards the result of `Navigator.goBack()` at the root of the back stack.
- `Result` is declared and never used in `HireHopApplication.kt`.
- `DemoProfileProvider` is a test double in the release main source set.
- `core:data` and `core:testing` form a test-scope cycle.
