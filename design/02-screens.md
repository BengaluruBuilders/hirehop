# Phase 2 — Screen prompts (HireHop)

**Goes to:** the **same Claude Design project** as the Phase 1 foundations board. Keep
`Design system: None`. The approved board is the system.
**Run:** only after you approve the board. Send one flow prompt per message. Start with flow 1,
because it has the highest stakes. Flow 1 becomes the reference that the other flows match.
**Output:** each flow prompt gives 4 to 7 screens in light and dark, plus interaction-state frames.

**Source:** [`docs/PRD.md`](../docs/PRD.md), Draft 1, 2026-09-30, and
[`01-foundations.md`](01-foundations.md). The template is `prompts/02-flow.md` in
`claude-app-design-prompts`.

## Placeholders

The flow prompts below already hold the values from the approved board "HireHop Board.dc.html"
(2026-09-30). The table keeps the names, so you can trace each value. Each color is the light value,
then the dark value.

| Placeholder | Board item |
|---|---|
| `{{FOUNDATIONS_FILE_NAME}}` | File name of the approved board in the project — **HireHop Board.dc.html** |
| `{{DISPLAY_FONT}}` | Display and headline face, also the hero numerals — **Bricolage Grotesque 600** |
| `{{UI_FONT}}` | Title, body, and label face — **Anek Latin 400 / 600** |
| `{{MONO_FONT}}` | Optional mono for fact IDs, source tags, and file names — **JetBrains Mono 400** |
| `{{PRIMARY_HEX}}` | Primary, for routine actions — **#353A78 / #B7BBF2** |
| `{{MET_HEX}}`, `{{PARTIAL_HEX}}`, `{{GAP_HEX}}` | Status roles `met`, `partial`, `gap` — **#276B45 / #86CC9E, #8A5A0B / #E2B45A, #A8542F / #EE9B78** |
| `{{EVIDENCE_HEX}}` | `evidence` highlighter — **#F8E36F / #5A4C12** |
| `{{ERROR_HEX}}` | `error` — **#A3352B / #F08A7D** |
| `{{SPECIAL_HEX}}` | `special`, the pack purchase accent — **#F2B31C** |
| `{{PROOF_SPRING}}` | Motion register `proof` — **dampingRatio 0.95, stiffness 1400; tween 200ms cubic-bezier(0.2, 0, 0, 1)** |
| `{{HOP_SPRING}}` | Motion register `hop` — **dampingRatio 0.62, stiffness 380** |

Content placeholders are not board values. Fill them before you send the flow that uses them:
`{{PRIVACY_POLICY_URL}}`, `{{GRIEVANCE_CONTACT}}`, `{{HELP_CONTACT}}`, `{{REFUND_POLICY_SUMMARY}}`,
`{{DELETE_ACCOUNT_URL}}`, `{{FREE_TAILOR_DAILY_CAP}}` (the PRD 10.1 daily cap on free model calls),
`{{OPENAI_RETENTION_LINE}}`.

> **Caution:** Write `{{OPENAI_RETENTION_LINE}}` only after the Zero Data Retention result and the
> legal review (PRD 10.4). `store: false` does not stop OpenAI abuse-monitoring retention. Do not
> write "OpenAI does not store it" until Zero Data Retention is approved.

## Prep

1. Keep the cleaned resume and JD from Phase 1 in the project. Do not upload real personal data.
   The DPDP rules in PRD section 10.4 apply.
2. The prompts use one fictional candidate, Priya Deshmukh. If your cleaned samples differ, change
   the sample content in each block to match them.

## How these prompts read the PRD

- Flow 1 includes resume import and profile confirmation. The first gap analysis needs confirmed
  facts (F2.3, section 7 steps 4 and 5), so these screens are on the 3-minute path.
- Section 7 step 3 puts sign-in "after first value and before any data leaves the device". The
  prompts put sign-in at the first tap that sends data: "Analyse this JD" or "Upload resume".
- The prompts spend a credit at the first download, because the paywall sits at the export preview
  (section 7 step 7, F4.4). The Decisions section records the evidence.
- `:app` in the screen map means that no feature module exists yet for that screen. Choose the
  module in Phase 3.

---

## Screen map

A screen is a place where the user sees something new or makes a main decision. A sheet or a
dialog is a short choice that returns. A state is the same job with different data.

Release and priority come from PRD 6.1 and 12.2. R1 is the closed beta. R2 is the public MVP.

| # | Screen | Flow | Feature module | One job | PRD IDs covered | States | ONE premium move |
|---|---|---|---|---|---|---|---|
| S1 | Welcome · R1 · P0 | 1 | `:app` | Show the value and the "never invents" promise before any ask | 7.1, 1, 8.1 rule 2, F1.4 | default, launched from share sheet (skips to S2) | A real resume line rewrites in place, and a provenance thread draws to its source fact |
| S2 | Paste JD · R1 · P0 | 1 | `:feature:analysis` | Get the JD text in with no friction | F2.1, 7.2, 8.1 (3 per day), 10.1 abuse cap | empty, pasted, shared in, link only, too short, offline queued, daily limit reached | Pasted text lands as a paper sheet on the document layer, with a tabular word count |
| S3 | Sign in · R1 · P0 (code field R2 · P1) | 1 | `:app` | Sign in at the first moment data would leave the phone | 7.3, 10.1 sign-in, 10.4.2, 3.3, F10.3 | default, 18+ not ticked, under 18 stop, sign-in failed, offline | The "what leaves your phone" note sits directly above the Google button |
| S4 | Consent notice · R1 · P0 | 1 | `:app` | Get itemised consent before any upload | 10.4.1, 10.4.6, 10.4 OpenAI 3, F1.6, F1.7, Play 1 | default, rows acknowledged, declined, read-only (from S22) | Each purpose row is a two-part ledger line: what we do, what we keep |
| S5 | Import resume · R1 · P0 | 1 | `:feature:profile` | Get the resume in with no storage permission | F1.1, F1.6, F1 AC (scan), 10.2 (30 s) | picking, reading (named steps), scanned PDF, unsupported file, failed, offline queued | Real resume lines lift off the page into fact cards, in step with the named steps |
| S6 | Confirm your facts · R1 · P0 | 1 | `:feature:profile` | Confirm or edit every extracted item | F1.2, F1.3, F1.7, 10.4.9 | all pending, partly confirmed, all confirmed, empty section, details removed banner, offline | Confirm stamps the `confirmed` chip in one `proof` stroke, and the counter ticks up |
| S7 | Gap analysis · R1 · P0 (share card R2 · P1) | 1 | `:feature:analysis` | Show what the JD asks for and where the user stands, free | F2.2–F2.6, F2 AC, 7.5, 8.1 rule 5, F10.1, 10.3 | loading (named steps), result, many gaps, all met, gap closed, error, offline cached, daily limit | Gap-analysis reveal: must-have gaps first, and the "9 / 14" fraction counts up |
| S8 | Tailored resume review · R1 · P0 | 2 | `:feature:tailor` | Show what changed, what is left to decide, and what was left out | F3.3, F3.4.5, F3.6, F3 AC, 7.6 | loading in background, ready, partly reviewed, all reviewed, regenerations used, job failed, offline | Margin citation marks: each changed line carries its fact ID in the margin |
| S9 | Bullet review · R1 · P0 | 2 | `:feature:tailor` | Accept a change or keep the original, with its source in view | F3.1, F3.4.3–F3.4.5, F3.5, 10.3 | normal, flagged verb, flagged scale word, repair failed (original kept), accepted, original kept by the user, user-edited, merged sources | Original-to-new diff writes in, then Accept files the card into the document |
| S10 | Cover letter · R2 · P1 | 2 | `:feature:tailor` | Offer an optional short letter where every paragraph has a source | F7.1–F7.4, F3.7, 7.9, 10.3 | offer, generating, ready, paragraph flagged, user-edited, error, offline | Tap a paragraph's margin citation, and its source facts light in `evidence` |
| S11 | Prep questions · R2 · P1 | 2 | `:feature:applications` | Give 8 to 12 likely questions, each tied to facts, and honest gap answers | F6.1–F6.4, F3.7, 7.9, 10.3 | generating, ready, error, offline cached | The "why they may ask" line highlights the exact JD phrase in `evidence` |
| S12 | Export preview · R1 · P0 (DOCX R2) | 3 | `:feature:tailor` | Show the full resume as it will export, before any payment | F4.1–F4.5, F4 AC, 7.7, 8.1 rule 2, 10.2 (3 s) | rendering, ready, credit available, no credit, R1 beta, DOCX, render error, offline | Template switch cross-fades the page setting while the text stays still |
| S13 | Application pack · R2 · P0 | 3 | `:app` | Let the user buy credits, only when they choose to download | F8, 8.1 rules 1–4, F4.4 | default, Play purchase sheet, pending, success, cancelled, failed, offline, single ₹49 test variant | The only `special` accent in the app: the pack CTA with a hero-numeral price |
| S14 | Exported · R1 · P0 (referral R2 · P1) | 3 | `:feature:tailor` | Confirm the file, share it, and save the application | F4.5, F4.6, F5.1, 7.8, 9.1, F10.2 | free credit used, paid credit used, share sheet open, saved | The credit counter drops by one in the `hop` register |
| S15 | Credits and help · R2 · P0 | 3 | `:app` | Show credits, purchases, and the refund path in plain words | 8.1 rule 3, 8.1 tiers, F10.2 | no purchases, purchases, pending purchase, offline cached | Hero-numeral "4 left" beside the plain promise "Credits never expire" |
| S16 | Profile · R1 · P0 | 4 | `:feature:profile` | See and correct every fact HireHop may use | F1.2, F1.3, F1.6, 10.4.3 | empty, partly confirmed, full, offline | Each section card shows its facts' provenance chips in a row |
| S17 | Fact editor · R1 · P0 | 4 | `:feature:profile` | Correct, add, or delete one fact | F1.3, F1.7, 10.4.3, 10.4.9 | new, edit, validation error, delete dialog, offline queued | A live paper line under the form shows how the fact reads |
| S18 | Guided form · R1 · P0 | 4 | `:feature:profile` | Build a profile without a resume | F1.4, F1 AC (scan route) | step 1 to 4, arrived from a scanned PDF, saved for later, offline | A finished step folds into fact cards that file into the profile |
| S19 | Evidence path · R1 · P0 | 4 | `:feature:profile` | Turn projects, internships, coursework, competitions, and roles into facts | F1.5, 5 (fresher path), R2 | category picker, question, answered, skipped, all done | Each answer folds into a fact card stamped `user-stated` |
| S20 | Applications · R1 · P0 | 5 | `:feature:applications` | List every application by last update | F5.2, F5.3, F5.4 | empty, list, offline, sync pending | Staggered list-enter, with status chips and coverage fractions in tabular figures |
| S21 | Application workspace · R1 · P0 (prep and letter R2) | 5 | `:feature:applications` | Hold everything for one application | F5.1, F5.2, F5.4, F2.5, 10.4.3 | full, not exported yet, offline read, notes saved, delete dialog | Shared-element header from the list row into the workspace |
| S22 | Settings · R1 · P0 | 5 | `:app` | Reach account, privacy, help, and data rights | F9, 10.4.4, 10.4.6, Play 1, 8.1 rule 3 | default, offline | The grievance contact prints in full, not behind a link |
| S23 | Your data · R1 · P0 | 5 | `:app` | Access, correct, export, and erase the user's data | 10.4.3, 10.4.5, F1.6 | default, preparing export, export ready, delete dialog, offline | A ledger of what HireHop holds, with tabular counts |
| S24 | Delete account · R1 · P0 | 5 | `:app` | Delete the account and all data, with no tricks | 10.4.4 | default, deleting, done, error, offline | Exact counts of what goes, and `error` only on the final button |

### Sheets, dialogs, and snackbars

| # | Sheet or dialog | Opens from | Job | PRD IDs | Release |
|---|---|---|---|---|---|
| D1 | "I have this" sheet | S7 gap row | Answer one short question to add a `user-stated` fact | F2.5, F1.5 | R1 · P0 |
| D2 | Source fact sheet | S7, S8, S9, S10, S11 | Show the profile fact behind a line, with its ID and chip | F2.3, F3.4.5 | R1 · P0 |
| D3 | Edit this line sheet | S9 | Hand-edit a bullet and mark it `user-edited` | F3.5 | R1 · P0 |
| D4 | Regenerate section dialog | S8 | State the included regenerations left, then confirm | F3.6 | R1 · P0 |
| D5 | Report inaccurate content sheet | Every generated item | Report a wrong, invented, or offensive item | 10.3, 10.4 Play 2 | R1 · P0 |
| D6 | JD fit card sheet | S7 | Preview and share the fit card, with no personal data | F10.1 | R2 · P1 |
| D7 | Status sheet | S14, S20, S21 | Set Saved, Applied, Interview, Offer, Rejected, or No response | F5.2 | R1 · P0 |
| D8 | "Added to your prep plan" snackbar | S7 gap row | Confirm the new task, with Undo | F2.5 | R1 · P0 |
| D9 | Delete item dialog | S17, S21, S23 | Delete one fact or one application | 10.4.3 | R1 · P0 |
| D10 | Notification priming sheet | S8 loading, first time | Ask to notify when the tailored resume is ready | F3 AC | R1 · P0 |
| D11 | Google Play purchase sheet (system UI) | S13 | Pay. Design only the states around it | 8.1 rule 1 | R2 · P0 |
| D12 | Google account picker (system UI) | S3 | Pick an account. Design only the states around it | 10.1 sign-in | R1 · P0 |

---

## Coverage check

### (a) PRD requirement to UI

| PRD requirement | Screen, sheet, or state |
|---|---|
| F1.1 system file picker, no storage permission | S5 |
| F1.2 structured profile, stable fact IDs | S6, S16 (fact ID in mono) |
| F1.3 confirm or edit every item; only confirmed items are facts | S6, S16 unconfirmed banner, S17 |
| F1.4 start without a resume | S1 link, S5 scan state, S16 empty state, S18 |
| F1.5 fresher evidence path, `user-stated` | S19, D1 |
| F1.6 delete the uploaded file | S4 row, S5 note, S16 note, S23 ledger |
| F1.7 no DOB, photo, religion, caste, or marital status; tell the user | S4 row, S6 details removed banner, S17 and S18 field rules |
| F1 AC scanned PDF message and guided-form route | S5 scanned state, S18 arrival state |
| F2.1 paste or share in; no URL scraping | S2 pasted, shared-in, and link-only states |
| F2.2 requirement type, priority, keywords | S7 row tags |
| F2.3 met, partial, gap with fact citations | S7, D2 |
| F2.4 groups, must-have gaps first | S7 |
| F2.5 "I have this" and "Add to my prep plan" | D1, D8, S21 prep plan |
| F2.6 keyword coverage, no ATS score | S7 coverage meter |
| F2 AC 20 s; free tier includes gap analysis | S7 loading state, S7 bottom-bar disclosure |
| F3.1 bullets with sources, edit types, keywords | S9 |
| F3.3 unsupported requirements go to the gap list, never the resume | S8 "Not added" panel |
| F3.4.4 failed repair keeps the original bullet | S9 repair-failed state |
| F3.4.5 per-bullet review, verb and scale flags | S9, S8 |
| F3.5 hand edit, `user-edited` | D3, S9 user-edited state |
| F3.6 regenerate a section, 2 included | S8, D4 |
| F3.7 source IDs bind F6 and F7; gaps never a premise | S10, S11 |
| F3 AC 60 s, background job, notification | S8 loading state, D10 |
| F4.2 single column, standard headings, real text | S12 templates |
| F4.3 3 templates | S12 |
| F4.4 full preview before the paywall | S12, S13 |
| F4.5 file name `Name_Company_Role.pdf` | S12, S14 |
| F4.6 Android share sheet | S14 |
| F4 AC "readable by common ATS parsers" wording | S12, S14 |
| F5.1 stored items per application | S21 |
| F5.2 status values | D7, S20, S21 |
| F5.3 list by last update; no kanban, no reminders | S20 |
| F5.4 offline reading | S20 and S21 offline states |
| F8 tiers: free, 5-pack, single test | S7 daily limit, S12 credit line, S13, S15 |
| 8.1 rule 1 Play consumable | D11, S13 |
| 8.1 rule 2 price before a paid action; no paywall at install | S1, S7 disclosure, S12 disclosure, S13 |
| 8.1 rule 3 refund and help in the app | S13 link, S15 |
| 8.1 rule 4 no job, interview, or ATS guarantee | Required rule in every flow |
| 8.1 rule 5 gap analysis stays free | S7 |
| F9 account, consent, compliance | S3, S4, S22, S23, S24 |
| 7.1 to 7.9 core flow steps | S1; S2, S5; S3, S4; S5, S6; S7; S8, S9; S12, S13; S14; S10, S11 |
| 10.3 "Report inaccurate content" on every generated item | D5 from S7, S9, S10, S11, S21 |
| 10.4.1 standalone itemised consent before upload | S4 |
| 10.4.2 18+ confirmation at sign-up | S3 |
| 10.4.3 access, correction, erasure | S23 (access, erasure), S16 and S17 (correction), D9 |
| 10.4.4 account deletion in the app, plus a web link | S24, S22 |
| 10.4.5 export of all data | S23 |
| 10.4.6 grievance contact in the app | S22, S4 footer |
| 10.4.9 data minimisation | S6, S17, S18 |
| 10.4 Play 1 privacy policy | S4 footer, S22 |
| 10.4 Play 2 report offensive or inaccurate AI content | D5 |
| 10.4 OpenAI 3 OpenAI as processor | S4 row |
| 10.4 Security 1 delete uploaded files | S4 row, S5 note |
| 10.5 TalkBack, 48 dp, dynamic text; English UI | Required rules in every flow |

These requirements have no UI. The reason is in the second column.

| Requirement | Reason there is no UI |
|---|---|
| F1.2 extraction itself | The backend does it. The user sees only the result in S6. |
| F2.2 and F2.3 extraction and matching logic | The backend does it. S7 shows the result. |
| F3.2, F3.4.1 to F3.4.3 schema, fact ledger, deterministic checks | The backend enforces them. S9 shows only the verb and scale flags. |
| F3.4.6 evaluation and monitoring | Internal release gate and logs. |
| F4.1 one approved JSON document | Internal data model. |
| F5.4 Room and sync | Internal. S20 and S21 show only the offline and sync-pending states. |
| 8.2 unit economics and cost logging | Server config and logs. |
| 10.1 architecture, model tiers, Play Integrity | Internal. The user sees only the daily-limit state in S2 and S7. |
| 10.2 performance targets | They set the length of the loading states. They add no screen. |
| 10.3 evaluation sets and release gates | Internal process. |
| 10.4.7 breach response plan | Operations process. The PRD names no in-app channel for the user notice. |
| 10.4.8 security logs for 1 year | Backend retention. |
| 10.4 Play 1 Data safety form, Play 3 API 36, Play 4 closed test | Play Console and build settings. |
| 10.4 OpenAI 1, 2, 4 `store: false`, Zero Data Retention, data residency | Backend and contract work. S4 states the processor role only. |
| 10.4 Security 2 to 4 encryption, row-level security, prompt-injection handling | Backend. |

### (b) Screen to PRD

Every row in the screen map and the sheet table cites at least one PRD ID or section. No screen
adds a non-goal from PRD 4.2: no ATS score, no auto-apply, no job listings, no voice interview, no
kanban, no reminders, no photos, and no subscriptions.

---

## Flow prompts

### Flow 1 — First run: the free 3-minute hook

Send this flow first. It sets the reference for every later flow.

```
Design the FIRST RUN flow for HireHop, from install to the first free gap analysis, drawn
strictly against this project's "HireHop Board.dc.html" (the locked design system). Consume
the system exactly — invent no new type, color, motion, or components. Direction:
EVIDENCE-EDITORIAL.

Text in {{DOUBLE_BRACES}} is copy that is not written yet. Draw it as a clearly marked placeholder slot. Do not invent it.

Reminder of the locked tokens you must use:
- Type: Bricolage Grotesque 600 (display/headline + hero numerals, tabular) · Anek Latin 400 / 600
  (title/body/label) · JetBrains Mono 400 (fact IDs and source tags only).
- Color: #353A78 / #B7BBF2 (ink indigo) as the one routine accent. Warm paper and deep ink neutrals on the
  three-layer ladder: app background, document paper (resume and JD text), tools on top (sheets,
  bottom bars). Status roles `met` #276B45 / #86CC9E, `partial` #8A5A0B / #E2B45A, `gap` #A8542F / #EE9B78
  always pair with their icon and text label. `evidence` #F8E36F / #5A4C12 is the highlighter for
  JD keywords and linked facts. `error` #A3352B / #F08A7D is for real failures only. `special`
  #F2B31C is RESERVED for the pack purchase moment — it does not appear in this flow.
- Motion: `proof` (spring dampingRatio 0.95, stiffness 1400; tween fallback 200ms cubic-bezier(0.2, 0, 0, 1)) for everything here: staggered list-enter, the gap-analysis
  reveal, the source-link reveal, and honest named-step waiting. `hop` (spring dampingRatio 0.62, stiffness 380) appears
  ONCE: when "I have this" closes a gap. Honor "Remove animations" with the board's named
  fallbacks.
- Illustration: the document is the illustration. Use the real resume and JD samples in this
  project, highlighted and tied by thin provenance threads and margin citation marks. Use the
  board's ink-and-highlighter spot set only for empty, error, offline, and scanned-PDF states.

Sample content (use it; no lorem ipsum):
- Candidate: Priya Deshmukh, B.Tech Computer Science, 2026, CGPA 8.1, a tier-2 college in Pune.
- JD: "Associate Analyst, Business Intelligence" at Northwind Global Capability Centre (GCC),
  Bengaluru, 0 to 1 years. Must-haves: SQL, Advanced Excel, Power BI or Tableau, a cloud data
  warehouse (Snowflake or BigQuery). Nice-to-haves: Python, statistics, Agile / JIRA,
  stakeholder communication.
- Facts: project "Placement Stats Dashboard" (P-02: Power BI, 3 batches of placement data, built
  for the college T&P cell); internship "Data intern, Kiran Agro Exports, Nashik, May to Jul
  2025" (I-01: cleaned 12,000 rows of sales data in Excel, built weekly pivot reports);
  coursework DBMS (C-01: SQL) and Probability & Statistics (C-02); Smart India Hackathon 2024,
  internal-round finalist (X-01).

SCREENS (7), each in LIGHT and DARK:
1. Welcome — show the value and the promise before any ask — headline "We never invent anything
   about you." Subline: "Paste a job description. See what it asks for and where you stand.
   Free." Hero: the real line "Made a dashboard for placement data" rewrites into "Built a Power
   BI dashboard of 3 batches of placement data for the college T&P cell", and a thin provenance
   thread draws from the new line to the fact card "P-02 · Placement Stats Dashboard ·
   confirmed". Two actions at equal weight: "Paste a job description" and "Import my resume"
   (either order works). A low-emphasis link: "No resume? Build your profile step by step." No
   sign-in, no price, no pack on this screen. Premium move: the rewrite-then-thread sequence
   plays once in `proof`, then the "Never invents" trust chip settles under it. Also give the
   reduce-motion frame (the final state, static).

2. Paste JD — get the JD in with no friction — a paper text area "Paste the job description
   text", a Paste button, Company ("Northwind GCC") and Role ("Associate Analyst") fields that
   the user types or corrects, and the primary "Analyse this JD". Next to the button: "2 of 3
   free analyses left today" and "Your JD stays on this phone until you tap Analyse." State
   frames: empty; pasted (the sample JD); shared in from WhatsApp through the Android share sheet
   (text already filled); link only ("Paste the JD text, not the link. HireHop does not open
   links."); too short ("This looks shorter than a job description. Paste the full text.");
   offline ("You're offline. We'll analyse this when you're back."); daily limit reached
   ("You've used today's 3 free analyses. You get 3 more tomorrow."). Premium move: the pasted
   text lands as a sheet of paper on the document layer in one `proof` stroke, with a tabular
   word count ("312 words").

3. Sign in — sign in at the first moment data would leave the phone — reached from "Analyse this
   JD" or "Upload resume". Directly above the button: "Next, your JD and resume go to HireHop's
   server to be read. They stay private to your account." One "Continue with Google" button, a
   required checkbox "I am 18 or older" (unticked by default), a text button "I'm under 18" at
   equal weight, and an optional low-emphasis field "College or group code" (R2). Show Android's
   Google account picker as a neutral system frame. States: default; box not ticked (the button
   shows why it is off, in words, not only a grey fill); under 18 — a calm stop: "HireHop is for
   people 18 and over. Nothing from this phone was sent."; sign-in failed; offline. Premium move:
   the "what leaves your phone" note is a document-layer note next to the action, not fine print.

4. Consent notice — get itemised consent before any upload — its own screen, title "Before
   anything leaves your phone". One item per row, as a two-part ledger line (what we do / what
   we keep). Purpose rows, each with its own acknowledgement: (a) "Read your resume and JDs to
   build your profile and analyses." (b) "Send the text to OpenAI, which processes it for us."
   plus {{OPENAI_RETENTION_LINE}}. (c) "Delete your uploaded file after reading it. We keep only
   the facts you confirm." Commitment rows, as plain statements: (d) "We never ask for or keep
   your date of birth, photo, religion, caste, or marital status." (e) "See, correct, download,
   or delete your data at any time in Settings." Footer: Privacy policy ({{PRIVACY_POLICY_URL}})
   and Grievance contact ({{GRIEVANCE_CONTACT}}). Actions at equal weight: "Agree and continue"
   and "Not now". States: default; all purposes acknowledged; declined ("Nothing has left your
   phone. You can come back to this at any time."); read-only mode opened later from Settings
   ("You agreed on 14 Feb 2027", no actions). Premium move: each acknowledgement mark inks in
   with one `proof` stroke.

5. Import resume — get the resume in with no storage permission — "Choose your resume (PDF or
   DOCX)" opens Android's system file picker (neutral system frame). Note: "HireHop asks for no
   storage permission." Honest waiting, up to 30 s, with named real steps: "Reading your resume"
   → "Finding your sections" → "Removing details we never keep". No percentage bar. States:
   picking; reading; scanned or image-only PDF — spot illustration and "This PDF is a scan, so we
   can't read its text. Build your profile step by step instead." with "Start the guided form"
   and "Choose another file" at equal weight; unsupported file; failed; offline ("We'll read it
   when you're back."). Premium move: while reading, the resume's real lines lift off the page
   one by one into small fact cards, in step with the named steps.

6. Confirm your facts — confirm or edit every extracted item; only confirmed items feed
   tailoring — sections Contact, Education, Experience, Projects, Skills, Certifications, Extras.
   Each item is a fact card with its mono fact ID ("P-02"), "Confirm", and "Edit". A tabular
   counter in the top bar: "12 of 18 confirmed". Banner at the top: "We removed your date of
   birth and your photo from this import. HireHop never keeps these." Primary "Continue to my
   analysis" is always available; if items are open, it shows "6 items not confirmed. HireHop
   won't use them until you confirm them." States: all pending; partly confirmed; all
   confirmed; empty section ("No certifications found. Add one, or skip."); offline (edits
   queue). Premium move: Confirm stamps the `confirmed` provenance chip onto the card in one
   `proof` stroke, and the counter ticks up.

7. Gap analysis — show what the JD asks for and where Priya stands, free — header "Associate
   Analyst · Northwind GCC". Keyword coverage meter: "You cover 9 of 14 key terms" (plain
   fraction and segmented bar), with a one-line explainer: "Key terms from this JD that appear
   in your confirmed facts. It is not a score." Groups in this order: must-have gaps ("Cloud
   data warehouse (Snowflake or BigQuery)"), partial ("Advanced Excel — pivots yes, macros not
   yet"), met ("SQL — C-01, I-01"; "Power BI or Tableau — P-02"), then nice-to-have gaps
   ("Agile / JIRA"). Each row shows its type and a must-have / nice-to-have tag. Met and partial
   rows show source fact chips; a tap opens the source fact sheet with the fact highlighted in
   `evidence`. Each gap row has two actions at equal weight: "I have this" and "Add to my prep
   plan" (a snackbar confirms, with Undo). Each row has "Report inaccurate content" in its
   overflow. Bottom bar: primary "Tailor my resume", with the disclosure next to it: "Free to
   tailor and preview. Downloading uses 1 credit. You have 1 free." Secondary: "Share JD fit
   card" (R2; the card shows role and met / partial / gap counts, no personal data). States:
   honest waiting up to 20 s ("Reading the JD" → "Matching your facts" → "Sorting must-haves
   first"); result; many gaps for a thin profile ("7 gaps. Each one can become a prep task.");
   all met; gap closed; error; offline (last result readable); daily limit; free tailoring
   limit (0 credits and {{FREE_TAILOR_DAILY_CAP}} free tailors used today: "You've used today's
   free tailoring. Your analysis is saved. You can tailor again tomorrow."). Premium move: the
   gap-analysis reveal — must-have gaps arrive first, then partial, then met, staggered in
   `proof`, while the "9 / 14" hero fraction counts up.

Apply these required rules (they're trust invariants, not polish):
- No paywall, no pack, and no price at install or anywhere in this flow. The only cost line is
  the credit disclosure next to "Tailor my resume".
- Equal visual weight for every decline or second path: the two Welcome actions, "I'm under 18",
  "Not now" on consent, "Choose another file", and the two gap actions.
- `gap` is never red and never an error. Gap rows use the to-do tone, the gap icon, and the label
  "Gap". Copy frames each gap as a task, never as a failure.
- No "ATS score", no gauge, no dial, no percentage. Only "You cover 9 of 14 key terms". Never say
  the resume will pass an ATS, and never promise an interview or a job.
- The consent notice is its own screen before the first upload, not a checkbox on sign-in. The
  18+ box starts unticked.
- Put each disclosure next to the action it describes: what leaves the phone, what the file
  picker does, and what "Tailor" costs.
- Honest waiting: named real steps only, no fake progress bar.
- Status never relies on color alone: icon, text label, and a TalkBack label, e.g. "Cloud data
  warehouse. Must-have. Gap. Actions: I have this, Add to my prep plan."
- 48 dp touch targets. At 200% text size, requirement text never truncates, rows reflow, and the
  bottom-bar actions stack.

Tone: a sharp senior who checks every line with you — plain words, calm, direct, and encouraging
for a fresher with a thin profile. Build trust, don't just explain. No dark patterns, no fake
urgency. Everything maps 1:1 to Jetpack Compose / Material 3 using the foundation tokens. Output
all 7 screens, light + dark, and include these interaction-state frames: the Welcome hero
(original line → rewrite → thread drawn); the gap-analysis reveal (waiting → must-have gaps
arriving → settled); the gap closed ("I have this" sheet with the question "Where have you used
Snowflake or BigQuery?" → answer saved as `user-stated` → the row settles into met in `hop` and
the fraction ticks to "10 / 14"); and the source fact sheet open over a met row.
```

### Flow 2 — Truth-locked tailoring, cover letter, and prep questions

This flow holds every generated item. The same source-ID rules bind all of them (F3.7).

```
Design the TRUTH-LOCKED TAILORING flow for HireHop — the tailored resume review, the per-bullet
review, the optional cover letter, and the prep questions — drawn strictly against this
project's "HireHop Board.dc.html" (the locked design system). Consume the system exactly —
invent no new type, color, motion, or components. Match the approved first-run flow.

Text in {{DOUBLE_BRACES}} is copy that is not written yet. Draw it as a clearly marked placeholder slot. Do not invent it.

Reminder of the locked tokens you must use:
- Type: Bricolage Grotesque 600 (display/headline + hero numerals, tabular) · Anek Latin 400 / 600
  (title/body/label) · JetBrains Mono 400 (fact IDs in margin citations and source chips).
- Color: #353A78 / #B7BBF2 (ink indigo) as the one routine accent, on the three-layer ladder: app background,
  document paper (the resume and letter), tools on top (review cards, sheets, bottom bars).
  `evidence` #F8E36F / #5A4C12 marks JD keywords used and linked facts. `met` #276B45 / #86CC9E,
  `partial` #8A5A0B / #E2B45A, `gap` #A8542F / #EE9B78 appear only where a JD requirement is named, with
  icon and label. `error` #A3352B / #F08A7D is for real failures only — never for a flag. `special`
  #F2B31C is RESERVED for the pack purchase moment — it does not appear in this flow.
- Motion: `proof` (spring dampingRatio 0.95, stiffness 1400; tween fallback 200ms cubic-bezier(0.2, 0, 0, 1)) for the diff, accept / reject, the source-link reveal, and
  honest waiting. `hop` (spring dampingRatio 0.62, stiffness 380) appears ONCE: when the last change is decided ("7 of 7
  reviewed"). Honor "Remove animations" with the board's named fallbacks.
- Illustration: the document is the illustration — the tailored resume on paper with margin
  citation marks and thin provenance threads to the profile facts. Spot illustrations only for
  error and offline states.

Sample content: Priya Deshmukh, B.Tech CS 2026, applying for "Associate Analyst, Business
Intelligence" at Northwind GCC, Bengaluru. Facts: P-02 Placement Stats Dashboard (Power BI, 3
batches, T&P cell); I-01 Data intern, Kiran Agro Exports, Nashik, May to Jul 2025 (12,000 rows
of sales data in Excel, weekly pivot reports); C-01 DBMS coursework (SQL); X-01 Smart India
Hackathon 2024, internal-round finalist. Gaps with no evidence: cloud data warehouse, Agile / JIRA.

SCREENS (4), each in LIGHT and DARK:
1. Tailored resume review — show what changed, what is left to decide, and what was left out —
   the tailored resume on the document layer, sections Experience, Projects, Skills, Education.
   Each changed line has a margin citation mark with its fact ID ("P-02") and a decision state:
   to review, accepted, original kept, or `user-edited`. Top: tabular "3 of 7 changes reviewed" and
   "1 flagged". A "Not added to your resume" panel: "2 JD needs have no evidence in your profile,
   so we left them out: cloud data warehouse, Agile / JIRA. They are in your prep plan." Each
   section's overflow: "Regenerate this section · 2 of 2 included left" (a dialog confirms).
   Primary "Preview export"; while changes are open it reads "4 changes left to review", with
   the line "Decide on every change before you export. We never export a line you haven't
   reviewed." Loading
   state (up to 60 s, runs in the background): "Picking your facts for this JD" → "Rewriting for
   this JD" → "Checking every line", with "You can leave. We'll notify you when it's ready." The
   first time, a sheet asks for the notification permission, with "Not now" at equal weight.
   States: loading; ready; partly reviewed; all reviewed; regenerations used ("You've used both
   included regenerations for this application. You can still edit any line by hand."); job
   failed ("We couldn't finish. Your profile and analysis are safe. Try again."); offline
   (review the saved result; decisions sync later). Premium move: margin citation marks — tap
   any line and a provenance thread draws to its fact (the source-link reveal).

2. Bullet review — accept or reject one change with its source in view — a review card on the
   tools layer. "Original": "Made a dashboard for placement data using Power BI." "New": "Built
   a Power BI dashboard of 3 batches of placement data for the college T&P cell." Edit-type
   tags: reword, emphasise. JD keywords used, marked in `evidence`: "Power BI", "dashboard".
   Source: "P-02 · Placement Stats Dashboard · confirmed" (tap opens the source fact sheet).
   Actions: "Accept" and "Keep original" at equal weight; "Edit by hand" (opens a sheet with the note
   "Your own words. HireHop does not check hand edits."); overflow "Report inaccurate content".
   Pager: "Change 3 of 7". State frames: normal; flagged verb ("Verb changed: made → built.
   Check that this is true for you."); flagged scale word ("Scale: '3 batches' — from P-02");
   repair failed ("We kept your original line. The rewrite added something we couldn't match to
   your facts." — no decision needed, the card counts as reviewed); accepted; original kept by
   the user (the original text restored);
   `user-edited`; merged sources ("Cleaned 12,000 rows of sales data in Excel and built weekly
   pivot reports", sources I-01 and C-01). Premium move: the diff — changed words mark on the
   original, the new text writes in over it in `proof`; Accept files the card into the document
   in one stroke, and "Keep original" snaps the original back.

3. Cover letter (optional, R2) — offer a short letter only after the resume, where every
   paragraph has a source — offer state: "Want a short cover letter? It's optional." with
   "Write one" and "No thanks" at equal weight. Letter on paper: "Dear Hiring Team at Northwind
   GCC," then 3 paragraphs, 150 to 220 words, each with a margin citation (paragraph 1: C-01,
   P-02; paragraph 2: I-01; paragraph 3: X-01). Tabular word count "184 words". The user edits
   in place; an edited paragraph shows `user-edited`. Each paragraph has "Report inaccurate
   content". Primary "Preview export". States: offer; generating (named steps, same as the
   resume); ready; paragraph flagged; user-edited; error; offline. Premium move: tap a
   paragraph's margin citation and its source facts light in `evidence`.

4. Prep questions (R2) — give 8 to 12 likely interview questions, each tied to facts — a list of
   question cards, count "10 questions". Example: "Walk me through your Placement Stats
   Dashboard. Where did the data come from?" — "Why they may ask: the JD asks for Power BI and
   reporting to stakeholders." — "Facts you can use: P-02, I-01" (chips). A separate "Your gaps"
   group gives one honest way to handle each gap: "If they ask about Snowflake or BigQuery: say
   you haven't used them yet. Link it to what you have — SQL from DBMS coursework and the Kiran
   Agro internship." Each card has "Report inaccurate content". No voice, no recording, no
   scores. States: generating; ready; error; offline (saved list readable). Premium move: the
   "why they may ask" line highlights the exact JD phrase in `evidence`.

Apply these required rules (they're trust invariants, not polish):
- Never invent. Every new line, paragraph, and question shows at least one source fact. A JD need
  with no evidence appears only in "Not added" and the prep plan — never in resume text, never as
  a letter sentence, never as the premise of a question.
- "Accept" and "Keep original" have equal weight. Do not design an "Accept all" button.
- Flags inform; they do not alarm. Use the board's flag icon and neutral ink, never `error`.
- Hand edits always show `user-edited` and the note that HireHop does not check them.
- Regenerate states the included count before the tap.
- The cover letter is optional. "No thanks" has equal weight, and the offer appears only after
  the resume review.
- No guarantees: never "this will get you shortlisted" or "ATS-ready". No readiness percentage.
- "Report inaccurate content" is on every generated item.
- TalkBack reads a review card as one unit: "Change 3 of 7. Original: … New: … Source P-02,
  confirmed. Flag: verb changed from made to built." Accept and Keep original labels name the change
  number.
- 48 dp touch targets. At 200% text size, the review card stacks Original above New, tags wrap,
  and the actions stack.

Tone: a careful editor at your side — precise, calm, and honest about what it could not prove.
Build trust, don't just explain. No dark patterns, no fake urgency. Everything maps 1:1 to
Jetpack Compose / Material 3 using the foundation tokens. Output all 4 screens, light + dark, and
include these interaction-state frames: the bullet review (resting → diff writing in → accepted
and filed → "Keep original" and snapped back); the source-link reveal (tap a line → thread draws → source
fact sheet); and the background job (waiting → you left → notification → ready).
```

### Flow 3 — Export preview, packs, and payment

```
Design the EXPORT AND PAYMENT flow for HireHop — export preview, the application pack, the
exported file, and credits and help — drawn strictly against this project's
"HireHop Board.dc.html" (the locked design system). Consume the system exactly — invent no
new type, color, motion, or components. Match the approved first-run flow.

Text in {{DOUBLE_BRACES}} is copy that is not written yet. Draw it as a clearly marked placeholder slot. Do not invent it.

Reminder of the locked tokens you must use:
- Type: Bricolage Grotesque 600 (display/headline + hero numerals, tabular: "₹149", "4 left") ·
  Anek Latin 400 / 600 (title/body/label) · JetBrains Mono 400 (file names and order IDs).
- Color: #353A78 / #B7BBF2 (ink indigo) as the one routine accent, on the three-layer ladder. `special`
  #F2B31C appears in this flow on ONE element only: the pack CTA on the Application pack
  screen. `error` #A3352B / #F08A7D is for a failed payment or render only. The export template
  itself carries NO brand color.
- Motion: `proof` (spring dampingRatio 0.95, stiffness 1400; tween fallback 200ms cubic-bezier(0.2, 0, 0, 1)) for the template switch, render, and sheets. `hop`
  (spring dampingRatio 0.62, stiffness 380) appears ONCE: the credit count changes (down by one on export, up to 5 on a
  purchase). Honor "Remove animations" with the board's named fallbacks.
- Illustration: the export preview frame from the board, holding a plain template. Spot
  illustrations only for error and offline states.

Sample content: Priya Deshmukh's tailored resume for "Associate Analyst" at Northwind GCC. File
name "Priya-Deshmukh_Northwind-GCC_Associate-Analyst.pdf". Pack: "5 applications · ₹149 ·
credits never expire". Purchase: 14 Apr 2027, Google Play order "GPA.3318-4402-1187-55210".

SCREENS (4), each in LIGHT and DARK:
1. Export preview — show the full resume exactly as it will export, before any payment — the
   plain single-column template inside the branded preview frame: standard headings Education,
   Experience, Projects, Skills; real text; no tables, icons, photo, or color in the template.
   Scroll through every page. Switcher for 3 templates with plain names (for example Plain,
   Compact, Spacious). Format toggle PDF / DOCX (DOCX is R2). File name in mono. The line
   "Readable by common ATS parsers." Primary "Download PDF" with the disclosure next to it: "Uses
   1 credit. You have 1 free." States: rendering (up to 3 s, "Setting your resume in Plain");
   ready with a credit; no credit ("You have 0 credits left." → the Application pack screen);
   R1 beta ("Downloads are free during the beta test." — no price, no credit line, and no
   purchase control of any kind, not even disabled); DOCX selected; render error; offline
   ("You can preview. Downloading needs a connection."). Premium move: switching templates
   cross-fades the page setting while every word stays in place.

2. Application pack — let the user buy credits, only when they choose to download — appears only
   after "Download" with 0 credits. The preview stays visible and dimmed behind it. Headline:
   "Your Northwind resume is ready. Download it with an application pack." Hero numeral "₹149".
   The `special` CTA "5 applications · ₹149 · credits never expire". Plain lines next to the CTA:
   "One-time payment through Google Play. Price includes GST." "No subscription. Nothing
   renews." "Each application: a tailored resume, its export, and prep questions." A "Refunds and
   help" link. "Not now" at equal weight — it returns to the preview, and the tailored resume
   stays saved in Applications. Show Google Play's purchase sheet as a neutral system frame.
   States: default; purchase pending ("Payment pending. We'll add your credits when Google Play
   confirms it."); success (counter 0 → 5, then back to Download); cancelled (calm, no guilt);
   failed; offline; a test variant with a second option "1 application · ₹49" at lower emphasis.
   Premium move: the app's only `special` accent — the pack CTA with a hero-numeral price —
   beside an equal-weight "Not now".

3. Exported — confirm the file, share it, and save the application — a file card: mono file
   name, "1 page · PDF · Plain". "Share" opens the Android share sheet (email, WhatsApp, Drive,
   Files; neutral system frame). "Saved to Applications · Associate Analyst · Northwind GCC". A
   status prompt "Did you apply? Mark as Applied" opens the status sheet. Credit line: "4 left"
   (or "Free application used"). Next steps at low emphasis: "Get prep questions" and "Write a
   cover letter (optional)" (R2). Referral line (R2): "Invite a friend. When they finish their
   first application, you both get 1 free credit." States: free credit used; paid credit used;
   share sheet open; marked Applied. Premium move: the credit counter drops by one in `hop` as
   the file card settles.

4. Credits and help — show credits, purchases, and the refund path in plain words — hero numeral
   "4 left" and the line "Credits never expire." Purchase history rows: "5 applications · ₹149 ·
   14 Apr 2027 · Google Play" with the order ID in mono. "Refunds and help": the plain summary
   {{REFUND_POLICY_SUMMARY}}, "Ask for a refund", and "Contact help" ({{HELP_CONTACT}}). Referral
   (R2): the user's invite code and credits earned. States: no purchases (free application
   only); purchases; pending purchase; offline (saved history readable). Premium move: the
   hero-numeral count sits beside "never expire" — a plain promise, with no countdown or expiry
   date anywhere.

Apply these required rules (they're trust invariants, not polish):
- The full preview comes before any paywall. The paywall covers the download only.
- Show the price and the credit use next to every paid action, before the tap.
- `special` appears only on the pack CTA. Never on a routine button, a badge, or a banner.
- "Not now" has equal weight, and declining keeps all work saved.
- No subscription, no timer, no "offer ends", no crossed-out price, no "most popular" badge.
- No job, interview, or ATS guarantee. Say "Readable by common ATS parsers." Never "beats ATS"
  or "ATS-proof".
- Say that the price includes GST. Refund and help are one tap from the pack screen and from
  Credits and help.
- The export template is not a brand surface: single column, no brand color, no photo.
- TalkBack: "Buy 5 applications for 149 rupees. Credits never expire. Button." The credit counter
  announces its new value ("4 credits left").
- 48 dp touch targets. At 200% text size, the pack CTA wraps to two lines and never cuts the
  price; "Not now" keeps the same size.

Tone: straight and fair — the opposite of a surprise charge. Build trust, don't just explain. No
dark patterns, no fake urgency. Everything maps 1:1 to Jetpack Compose / Material 3 using the
foundation tokens. Output all 4 screens, light + dark, and include these interaction-state
frames: the template switch (Plain → mid cross-fade → Compact); the purchase (pack screen → Play
sheet → pending → success with the counter at 5); and the export (Download tapped → rendering →
file card with the counter dropping from 5 to 4).
```

### Flow 4 — Profile, guided form, and the fresher evidence path

```
Design the PROFILE flow for HireHop — the profile, the fact editor, the guided form, and the
fresher evidence path — drawn strictly against this project's "HireHop Board.dc.html" (the
locked design system). Consume the system exactly — invent no new type, color, motion, or
components. Match the approved first-run flow, which already holds Import resume and Confirm
your facts.

Text in {{DOUBLE_BRACES}} is copy that is not written yet. Draw it as a clearly marked placeholder slot. Do not invent it.

Reminder of the locked tokens you must use:
- Type: Bricolage Grotesque 600 (display/headline + hero numerals, tabular: "18 facts", "2 of 4") ·
  Anek Latin 400 / 600 (title/body/label) · JetBrains Mono 400 (fact IDs).
- Color: #353A78 / #B7BBF2 (ink indigo) as the one routine accent, on the three-layer ladder. Facts sit on
  document paper. Provenance chips `confirmed` and `user-stated` use the board's chip styles.
  `error` #A3352B / #F08A7D only on "Delete this fact" and field validation. `special`
  #F2B31C is RESERVED for the pack purchase moment — it does not appear in this flow.
- Motion: `proof` (spring dampingRatio 0.95, stiffness 1400; tween fallback 200ms cubic-bezier(0.2, 0, 0, 1)) for every move in this flow: step changes, cards filing
  into sections, the chip stamp. `hop` (spring dampingRatio 0.62, stiffness 380) does not appear here. Honor "Remove
  animations" with the board's named fallbacks.
- Illustration: the document is the illustration — facts as paper cards with mono IDs. Spot
  illustrations for the empty profile, the scanned-PDF arrival, and offline.

Sample content: Priya Deshmukh, B.Tech Computer Science, 2026, a tier-2 college in Pune. 18
facts: 15 `confirmed`, 3 `user-stated`. Projects: P-02 Placement Stats Dashboard (Power BI and
Excel, 3 batches of placement data, used by the T&P cell for the 2024 placement report).
Internship: I-01 Data intern, Kiran Agro Exports, Nashik, May to Jul 2025. Coursework: C-01 DBMS
(SQL joins and GROUP BY in a library database project). Competition: X-01 Smart India Hackathon
2024, internal-round finalist, team of 6. Position: R-01 Treasurer, college coding club,
2024–25, managed a ₹40,000 event budget.

SCREENS (4), each in LIGHT and DARK:
1. Profile — see and correct every fact HireHop may use — header "Priya Deshmukh · B.Tech CS
   2026" and hero numeral "18 facts", with chip counts "15 confirmed · 3 user-stated". Section
   cards on paper: Education 2, Experience 1, Projects 3, Skills 9, Certifications 1, Extras 2.
   A calm banner when items are open: "2 items are not confirmed. HireHop won't use them." A note:
   "Your uploaded file was deleted after reading. Only these facts are kept." Actions: "Add
   evidence" (the evidence path) and "Add a fact". States: empty (spot illustration, "Import my
   resume" and "Build it step by step" at equal weight); partly confirmed; full; offline.
   Premium move: each section card shows its facts' provenance chips in a row, so the mix of
   `confirmed` and `user-stated` reads at a glance.

2. Fact editor — correct, add, or delete one fact — fields by fact type. For a project: Title
   "Placement Stats Dashboard", What you did, Tools (chips: Power BI, Excel), Dates, Numbers ("3
   batches"). Hint: "HireHop can use only the numbers, names, tools, and dates you put here."
   Mono fact ID "P-02" and its provenance chip at the top. "Save" and "Cancel" at equal weight.
   "Delete this fact" at the bottom (a dialog confirms). States: new; editing; validation error
   ("The end date is before the start date."); delete dialog; offline (saves queue). Premium
   move: a live paper line under the form shows how the fact reads as the user types.

3. Guided form — build a profile without a resume — one topic per step: Contact, Education,
   Skills, Experience. Tabular step counter "2 of 4". The Experience step says: "No work
   experience yet? That's normal. Next, we'll ask about your projects." Then it hands off to the
   evidence path. "Save and finish later" on every step. Entries become facts tagged
   `user-stated`. States: each step; arrived from a scanned PDF ("Your PDF is a scan, so we
   couldn't read it. Let's build your profile here."); saved for later; offline. Premium move:
   a finished step folds into fact cards that file into the profile.

4. Evidence path — turn a fresher's projects, internships, coursework, competitions, and
   positions of responsibility into facts — category chips: Projects · Internships · Coursework ·
   Competitions · Positions of responsibility. One short question card at a time: "What did you
   build? Which tools? Who used it?" Example answer: "Placement Stats Dashboard. Power BI and
   Excel. The T&P cell used it for the 2024 placement report." Coursework: "DBMS — SQL joins and
   GROUP BY in a library database project." Competition: "Smart India Hackathon 2024 — internal
   round finalist, team of 6." Position: "Treasurer, college coding club, 2024–25 — managed a
   ₹40,000 event budget." A note: "Write only what you did. HireHop uses your words as facts."
   "Skip" on every question. States: category picker; question; answered; skipped; all done
   ("5 new facts added"). Premium move: each answer folds into a fact card stamped
   `user-stated`, which then files into its profile section.

Apply these required rules (they're trust invariants, not polish):
- No field, slot, or prompt for date of birth, photo, religion, caste, or marital status,
  anywhere.
- Provenance chips are always visible. Unconfirmed items are visibly marked and not used.
- "Skip" or "Save and finish later" on every guided step. Never force completion.
- Never shame a thin profile. No "you lack experience". Freshers get plain, warm guidance.
- `error` sits only on the destructive button and on real validation errors. The delete dialog
  names what goes: "Delete P-02 Placement Stats Dashboard from your profile?"
- No storage permission ask and no photo picker.
- TalkBack reads each fact card as "Project. Placement Stats Dashboard. Confirmed. Fact P-02.
  Actions: Edit." Step counters announce "Step 2 of 4".
- 48 dp touch targets. At 200% text size, fields stack full width and chips wrap.

Tone: a patient senior who helps you find the evidence you already have. Build confidence and
trust, don't just explain. No dark patterns. Everything maps 1:1 to Jetpack Compose / Material 3
using the foundation tokens. Output all 4 screens, light + dark, and include these
interaction-state frames: the evidence path (question → typed answer → card stamped
`user-stated` → filed into Projects); and the guided form handing off from Experience to the
evidence path.
```

### Flow 5 — Application workspace, settings, and data rights

```
Design the WORKSPACE AND ACCOUNT flow for HireHop — the applications list, the application
workspace, settings, your data, and account deletion — drawn strictly against this project's
"HireHop Board.dc.html" (the locked design system). Consume the system exactly — invent no
new type, color, motion, or components. Match the approved first-run flow.

Text in {{DOUBLE_BRACES}} is copy that is not written yet. Draw it as a clearly marked placeholder slot. Do not invent it.

Reminder of the locked tokens you must use:
- Type: Bricolage Grotesque 600 (display/headline + hero numerals, tabular: "9 / 14", "18 facts") ·
  Anek Latin 400 / 600 (title/body/label) · JetBrains Mono 400 (fact IDs and file names).
- Color: #353A78 / #B7BBF2 (ink indigo) as the one routine accent, on the three-layer ladder. Application
  status chips (Saved, Applied, Interview, Offer, Rejected, No response) use the board's chip
  styles; "Rejected" is not `error`. `met` #276B45 / #86CC9E, `partial` #8A5A0B / #E2B45A, `gap`
  #A8542F / #EE9B78 appear in the gap summary with icon and label. `error` #A3352B / #F08A7D only on
  "Delete my account" and "Delete application". `special` #F2B31C is RESERVED for the
  pack purchase moment — it does not appear in this flow.
- Motion: `proof` (spring dampingRatio 0.95, stiffness 1400; tween fallback 200ms cubic-bezier(0.2, 0, 0, 1)): staggered list-enter, the shared-element header, sheets.
  `hop` (spring dampingRatio 0.62, stiffness 380) does not appear here. Honor "Remove animations" with the board's named
  fallbacks.
- Illustration: the document is the illustration — JD and resume snippets on paper. Spot
  illustrations for the empty list, offline, and the account-deleted state.

Sample content: Priya Deshmukh's 4 applications —
- "Associate Analyst · Northwind GCC · Applied · 9 / 14 · updated 2 h ago"
- "Data Analyst Intern · Paisa Ledger (start-up) · Interview · 11 / 13 · updated yesterday"
- "Graduate Engineer Trainee · Sahyadri Motors · Saved · 6 / 12 · updated 3 days ago"
- "Business Analyst · Meridian GCC · No response · 8 / 15 · updated 12 Mar"
Credits: 4 left. Profile: 18 facts.

SCREENS (5), each in LIGHT and DARK:
1. Applications — list every application by last update — one simple list, newest update first.
   Each row: role, company, status chip, coverage fraction in tabular figures, updated time. Top
   bar: credit counter "4 left". Primary "New application" (opens Paste JD). No board view, no
   reminders, no job suggestions. States: empty (spot illustration and "Paste a JD to see where
   you stand. It's free."); list; offline banner ("You're offline. You can read everything.
   Changes sync when you're back."); sync pending on one row. Premium move: rows arrive with a
   staggered list-enter in `proof`.

2. Application workspace — hold everything for one application — header "Associate Analyst ·
   Northwind GCC" with the status chip (a tap opens the status sheet). Sections: Resume (file
   card, "Share again", "Open review"); Gap analysis (met / partial / gap counts and "9 / 14",
   opens the full analysis); Prep plan (tasks from gaps, e.g. "Learn the basics of BigQuery with
   a public dataset", each with a done tick and "Report inaccurate content"); Prep questions
   (R2); Cover letter (R2, optional); Notes (autosaves, shows "Saved"); JD text (collapsed,
   readable). "Delete application" in the overflow (a dialog confirms). States: full; not
   exported yet ("Your tailored resume is ready to preview."); offline read; notes saved; delete
   dialog. Premium move: a shared-element header carries the list row into the workspace.

3. Settings — reach account, privacy, help, and data rights — grouped list: Account (Google
   email "priya.d@example.com"); Credits and help; Your data; Privacy — Privacy policy
   ({{PRIVACY_POLICY_URL}}), Consent notice (read-only), Grievance contact printed in full
   ({{GRIEVANCE_CONTACT}}); About — "We never invent anything about you." and the app version;
   Delete account at the bottom, with the line "You can also delete your account on the web:
   {{DELETE_ACCOUNT_URL}}". States: default; offline. Premium move: the grievance contact prints
   in full on the screen, not behind a link.

4. Your data — access, correct, export, and erase — title "What HireHop holds about you". A
   ledger: Profile — 18 facts ("View", "Correct" opens Profile); Applications — 4, with JD text,
   analyses, and resumes ("View", "Delete" per application); Purchases — 1 ("View"); Uploaded
   resume — "Deleted after reading". Primary "Download all my data". States: default; preparing
   ("Preparing your file"); ready (opens the Android share sheet); delete dialog; offline.
   Premium move: the ledger shows tabular counts for each kind of data, so the user sees the
   whole picture at once.

5. Delete account — delete the account and all data, with no tricks — lists exactly what goes:
   "18 profile facts, 4 applications, 4 unused credits". Link: "Download my data first". Line:
   "You can also delete your account on the web: {{DELETE_ACCOUNT_URL}}". Actions: "Delete my
   account" in `error` and "Keep my account" at equal weight. States: default; deleting; done
   (spot illustration, "Your account and data are deleted.", back to Welcome); error; offline
   ("Deleting needs a connection."). Premium move: exact counts, and `error` only on the final
   button — calm, not scary.

Apply these required rules (they're trust invariants, not polish):
- Status changes only through the status sheet. No kanban board, no reminders, no job listings,
  no auto-apply.
- Every workspace screen reads offline, with a calm banner.
- Grievance contact, privacy policy, and the consent notice are one tap or less from Settings.
- Access, correction, export, and erasure are all present and plain.
- Destructive actions: `error` only on the final button, an equal-weight keep option, the exact
  count of what goes, and no guilt copy.
- "Report inaccurate content" on every generated item in the workspace (prep tasks, questions,
  letter).
- No job, interview, or ATS guarantee anywhere, including status copy ("Offer" is the user's
  own label, not a prediction).
- TalkBack reads each row as "Associate Analyst at Northwind GCC. Applied. Covers 9 of 14 key
  terms. Updated 2 hours ago." Status chips carry text, not color alone.
- 48 dp touch targets. At 200% text size, list rows wrap to two or three lines and never cut
  the role name.

Tone: calm and organised — a tidy desk for a stressful search. Build trust, don't just explain.
No dark patterns, no fake urgency. Everything maps 1:1 to Jetpack Compose / Material 3 using the
foundation tokens. Output all 5 screens, light + dark, and include these interaction-state
frames: the shared-element transition (row → mid-transition → workspace header); the status
sheet (open → "Interview" chosen → chip updated); and the data export (tap → preparing → share
sheet).
```

---

## Decisions

A research pass on 2026-09-30 checked each decision against the PRD, the six research reports, and current web sources. Each result keeps the default in the prompts above.

| Decision | Result | Confidence | Evidence | Check again if |
|---|---|---|---|---|
| Credit spend point | Spend the credit at the first download. Cap free tailoring at `{{FREE_TAILOR_DAILY_CAP}}` per day when the user has 0 credits. | Medium | PRD 7.7 and F4.4; R2 section 3.4 (the complaint is a download paywall with no real preview); PRD 10.1 daily cap on free model calls | Beta logs show many tailors that never reach a download |
| R1 beta download | Downloads are free in R1. Show no purchase control of any kind. | High | PRD 12.1 caution; Google Play Deceptive Behavior policy (answer 9888077, grade A); R6 section 8.5 | Never. This is a Play policy constraint. |
| Confirm speed | Per-item Confirm only. Do not add "Confirm all". | Medium | PRD F1.3; R5 section 3.3 (skills extraction F1 0.84 to 0.88, so errors are likely); NN/g bulk actions guidance is for low-stakes lists | Usability tests miss the 3-minute target because of confirm time. Then test "Confirm all" for Skills only. |
| Undecided changes at export | Block "Preview export" until every change has a decision. | Medium | PRD F3.4.5 per-bullet review; R5 section 6.2 layer 5; no direct usability study found | Gap-analysis-to-export conversion falls under 20% (PRD 9.2). Then test "keep the original text" for open changes. |
| Regenerations after 2 | Block the third regeneration. Point the user to hand edits. | Medium to high | PRD F3.5 and F3.6; R5 sections 5 and 9.3 (cost per application); R3 section 10 (few SKUs before WTP is proven) | More than 20% of applications reach the cap and then stop |
