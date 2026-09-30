# HireHop

HireHop is an Android app. It turns a candidate's confirmed background and a job description into a
tailored application. It never invents facts about the candidate.

## Read these first

1. `docs/CONSTITUTION.md` — the rules and the CI gate for each rule. It wins every conflict.
2. `docs/ARCHITECTURE.md` — modules, models, interfaces, and navigation.
3. `docs/PRD.md` — product scope and release gates.

## Design source

If a task changes UI, read the design for that flow before you write code.

| Need | File |
|---|---|
| Screens and states for flow N | `design/claude-design/HH Flow<N> *.dc.html` |
| Color, type, spacing, and motion tokens | `design/claude-design/HireHop Board.dc.html` |
| Screen map and the prompt for each flow | `design/02-screens.md` |
| Rules for the foundations board | `design/01-foundations.md` |

| Flow | File |
|---|---|
| 1. First run | `HH Flow1 Screen.dc.html` |
| 2. Tailoring | `HH Flow2 Tailoring.dc.html` |
| 3. Export and payment | `HH Flow3 Export.dc.html` |
| 4. Profile | `HH Flow4 Profile.dc.html` |
| 5. Workspace and account | `HH Flow5 Workspace.dc.html` |

Read the `.dc.html` source as text. Each screen shows light on the top row and dark on the row below.
To see the screens, run `python3 -m http.server 8766` in `design/claude-design`, then open
`http://127.0.0.1:8766/` in a browser. `support.js` must stay next to the `.dc.html` files.
The files are a snapshot of the Claude Design project "Resume and design specs". Do not edit them
by hand. If the design changes, export the files again and replace them in one PR.

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
