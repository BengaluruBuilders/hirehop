# Screens

This file maps the 24 screens of the current design. Each flow folder in `design/claude-design/`
has an `INDEX.md` with the full list of frames and the unique copy of each state.
Each state has a light frame and a dark frame.

## Screen map

| Screen | Flow | Feature module | One job | States that the design draws |
|---|---|---|---|---|
| S1 Welcome | 1 | `onboarding` | Show the value and the promise before any ask | default; hero original line; hero rewrite; hero thread drawn; reduce motion static final |
| S2 Paste JD | 1 | `onboarding` | Get the JD in with no friction | empty; pasted; shared from WhatsApp; link only; too short; offline; daily limit reached |
| S3 Sign in | 1 | `onboarding` | Sign in at the first moment data would leave the phone | default; box not ticked; ready; Google account picker (system); under 18; sign-in failed; offline |
| S4 Consent notice | 1 | `onboarding` | Record consent for each purpose | default; acknowledging; all purposes acknowledged; declined; read only from Settings |
| S5 Import resume | 1 | `onboarding` | Read a resume into facts | choose; picking (system); reading step 2; reading step 3; scanned PDF; unsupported file; failed; offline |
| S6 Confirm your facts | 1 | `onboarding` | Let the user confirm each fact | all pending; partly confirmed; confirm stamps; all confirmed; empty section; offline |
| S7 Gap analysis | 1 | `analysis` | Show met, partly met, and to prepare for each requirement | honest waiting; reveal; result settled; result scrolled; row overflow menu; source fact sheet; I have this question sheet; saved as user-stated; gap closed; added to prep plan; many gaps; all met; error; offline last result; daily limit; free tailoring limit; text at 200% |
| S8 Tailored resume review | 2 | `tailor` | Review the rewritten resume line by line | loading; notification permission; background; background notification (system); ready; partly reviewed; scrolled to not added; source link tapped; thread drawn; source fact sheet; all reviewed; section overflow; regenerate confirm; regenerations used; job failed; offline |
| S9 Bullet review | 2 | `tailor` | Accept, keep, or edit one rewritten line | resting diff; diff writing in; accepted; keep original; flagged verb; flagged scale word; repair failed; edit by hand sheet; user-edited; merged sources; text at 200% |
| S10 Cover letter | 2 | `tailor` | Draft a cover letter from confirmed facts | offer; generating; ready; margin citation tapped; paragraph flagged; editing in place; user-edited; error; offline |
| S11 Prep questions | 2 | `tailor` | List interview questions from the JD and the gaps | generating; ready; your gaps; error; offline saved list |
| S12 Export preview | 3 | `tailor` | Preview the resume and start the download | rendering; ready; template switch; Compact template; scrolled to end; DOCX selected; no credit; free downloads (R1 beta); download tapped; render error; offline |
| S13 Application pack | 3 | `tailor` | Buy a pack of application credits | default; Google Play sheet (system); purchase pending; success; cancelled; payment failed; offline; test variant; text at 200% |
| S14 Exported | 3 | `tailor` | Confirm the export and offer the next step | paid credit used; free credit used; share sheet (system); status sheet; marked Applied |
| S15 Credits and help | 3 | `tailor` | Show credits and purchases | no purchases; purchases; pending purchase; offline |
| S16 Profile | 4 | `profile` | Show all facts by section | empty; partly confirmed; full; full scrolled; section expanded; offline |
| S17 Fact editor | 4 | `profile` | Add or correct one fact | new; editing; editing with live line; validation error; delete dialog; offline; text at 200% |
| S18 Guided form | 4 | `profile` | Build a profile step by step | from scanned PDF; Contact; Education; step folds into fact cards; Skills; Experience; hand-off to evidence path; saved for later; offline |
| S19 Evidence path | 4 | `profile` | Ask one question per missing fact | category picker; question; typed answer; card stamped user-stated; filed into Projects; skipped; all done |
| S20 Applications | 5 | `applications` | List the saved applications | empty; list; list enter; scrolled compact header; offline; sync pending on one row; text at 200% |
| S21 Application workspace | 5 | `applications` | Open the documents and status of one application | shared element row pressed; mid-transition; full; scrolled; not exported yet; notes saved; offline read; status sheet open; Interview chosen; chip updated; overflow menu; delete dialog |
| S22 Settings | 5 | `settings` | Hold account, privacy, and about | default; scrolled; sign-out confirm; offline |
| S23 Your data | 5 | `settings` | Export or delete the user's data | default; scrolled; export preparing; share sheet (system); delete dialog; offline |
| S24 Delete account | 5 | `settings` | Delete the account after a clear warning | default; deleting; done; error; offline |

A frame marked "system" shows Android UI. Do not build it.

## How to add or change a screen

1. Open the canvas. The link is in `design/claude-design/README.md`.
2. Ask Claude Design for a new frame in the same canvas. Name the flow, the screen, and the state.
3. Tell Claude Design to use the foundations boards and the existing frames of that flow.
4. Make a light frame and a dark frame for each new state.
5. Check the frames against `design/01-foundations.md`: tokens, status rules, audience rule, and illustration rule.
6. Export the canvas. Replace `design/claude-design/` in one pull request.
7. Update the `INDEX.md` of the flow. Add the screen or the state to the table above.
8. Implement the change by `docs/REDESIGN.md`, section 8.
9. Record new screenshot baselines for the module, then run the verify task.
