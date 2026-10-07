Design all the screens of HireHop, an Android app.

## What the app is

HireHop turns a person's confirmed background and a job description into a tailored application: a resume, an optional cover letter, and interview prep. It never invents facts. Every rewritten line traces back to a fact the user confirmed. Users are early-career and 1-to-2-years-in job seekers in India. Payments are in rupees through Google Play.

## Features

1. **Resume import and profile** — import a PDF or DOCX resume and read it into facts. Or build the profile with a step-by-step guided form, or answer one question per missing fact. The user confirms each fact. Facts the user typed are marked "user-stated".
2. **Job description analysis** — paste a JD, or share it from another app such as WhatsApp. The app matches each requirement to the user's facts as Met, Partly met, or To prepare, and shows keyword coverage. "I have this" lets the user add a missing fact. Gaps can go into a prep plan.
3. **Tailored resume** — rewrites the resume for the JD using only confirmed facts. The user reviews each line against its source fact: accept it, keep the original, or edit it by hand. Risky wording is flagged. 2 regenerations per application.
4. **Export** — preview the resume, then download it as PDF or DOCX. One template. The preview is free. The download uses a credit. Share the file.
5. **Applications** — a list of saved applications. Each one holds the JD, the resume, the cover letter, prep questions, a status (Saved, Applied, Interview, Offer, Closed), notes, and prep tasks.
6. **Prep questions** — interview questions from the JD and from the user's gaps.
7. **Cover letter** — optional, offered after the resume. Each paragraph cites the facts it uses.
8. **Free tier and paid pack** — free credits, then a pack of 5 applications for ₹149. Credits never expire.
9. **Account and privacy** — Google sign-in, 18+ check, consent per purpose, data export, data deletion, account deletion.
10. **JD-fit share card** — a shareable card of the match result with no personal data.

Not in the product: job listings or job search, an ATS score, more than one template, reminders, a kanban board, referral credits, a single-application purchase.

## Structure

- **First run:** Welcome, Paste JD, Sign in, Consent, Import resume, Confirm facts, Gap analysis.
- **Main app:** three sections, Applications, Profile, and Settings. Applications is the home after onboarding.

## Screens and states

### Flow 1 · First run

1. **S1 Welcome** — shows the value and the honesty promise before any ask. Three entry points: Paste a job description, Import my resume, Build your profile step by step. States: default; original line, rewritten line, and the link between them and the source fact.
2. **S2 Paste JD** — States: empty; pasted; shared from WhatsApp; link only; too short; offline; daily limit reached.
3. **S3 Sign in** — Google sign-in with an 18+ confirmation. States: default; box not ticked; ready; Google account picker (system); under 18; sign-in failed; offline.
4. **S4 Consent notice** — consent for each purpose. States: default; acknowledging; all purposes acknowledged; declined; read-only (opened from Settings).
5. **S5 Import resume** — States: choose file; picking (system); reading, step 2; reading, step 3; scanned PDF (offer guided form); unsupported file; failed; offline.
6. **S6 Confirm your facts** — facts by section. States: all pending; partly confirmed; fact just confirmed; all confirmed; empty section; offline.
7. **S7 Gap analysis** — the JD (role, company, requirements), then Met, Partly met, or To prepare for each requirement, keyword coverage, and "Tailor my resume". States: waiting; result; result scrolled; row menu; source fact sheet; "I have this" question sheet; saved as user-stated; gap closed; added to prep plan; many gaps; all met; JD-fit share card; error; offline with last result; daily limit; free tailoring limit used.

### Flow 2 · Tailoring

8. **S8 Tailored resume review** — the rewritten resume. Each changed line links to its source fact. States: loading; notification permission ask; running in background; background notification (system); ready; partly reviewed; scrolled to "not added"; source link tapped; source fact sheet; all reviewed; section menu; regenerate confirm; regenerations used up; job failed; offline.
9. **S9 Bullet review** — one changed line: accept, keep original, or edit. States: before and after; accepted; kept original; flagged verb; flagged scale word; repair failed; edit-by-hand sheet; user-edited; merged sources.
10. **S10 Cover letter** — States: offer; generating; ready; citation tapped, source fact sheet; paragraph flagged; editing; user-edited; error; offline.
11. **S11 Prep questions** — States: generating; ready; questions from your gaps; error; offline saved list.

### Flow 3 · Export and payment

12. **S12 Export preview** — States: rendering; ready with 1 free credit; PDF selected; DOCX selected; scrolled to end; no credit left; making the file; render error; offline.
13. **S13 Application pack** — 5 applications for ₹149. States: default; Google Play sheet (system); purchase pending; success; cancelled; payment failed; offline.
14. **S14 Exported** — confirms the export and offers the next step. States: paid credit used; free credit used; share sheet (system); status sheet; marked Applied.
15. **S15 Credits and help** — credits left, purchases, refunds and help. States: no purchases; purchases; pending purchase; offline.

### Flow 4 · Profile

16. **S16 Profile** — all facts by section, with entry points to add evidence, the guided form, and import. States: empty; partly confirmed; full; full scrolled; section expanded; offline.
17. **S17 Fact editor** — add or correct one fact. States: new; editing; editing with a live preview line; validation error; delete dialog; offline.
18. **S18 Guided form** — build the profile step by step. States: started from a scanned PDF; Contact; Education; finished step becomes fact cards; Skills; Experience; hand-off to evidence path; saved for later; offline.
19. **S19 Evidence path** — one question per missing fact. States: category picker; question; typed answer; fact saved as user-stated; filed into Projects; skipped; all done.

### Flow 5 · Applications and settings

20. **S20 Applications** — home of saved applications: company, role, match, status, and a "New application" action. States: empty; list; scrolled; offline; sync pending on one row.
21. **S21 Application workspace** — one application: JD, status, resume, cover letter, prep questions, export, notes, prep tasks. States: full; scrolled; not exported yet; notes saved; offline read-only; status sheet open; Interview chosen; menu; delete dialog.
22. **S22 Settings** — account, credits and help, your data, consent notice, delete account, sign out, about. States: default; scrolled; sign-out confirm; offline.
23. **S23 Your data** — export or delete data. States: default; scrolled; export preparing; share sheet (system); delete dialog; offline.
24. **S24 Delete account** — States: warning; deleting; done, with "Back to Welcome"; error; offline.

"System" states are standard Android sheets: the Google account picker, the Play billing sheet, the share sheet, and the notification.
