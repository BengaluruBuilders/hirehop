# Phase 1 — Foundations prompt (HireHop)

**Goes to:** a **new Claude Design chat**. Keep `Design system: None`. HireHop gets its own system.
**Output:** a style-tile board in light and dark. It is *not* a set of screens.
**Approve the board before you run any Phase 2 flow prompt.**

**Source:** [`docs/PRD.md`](../docs/PRD.md), Draft 1, 2026-09-30. The current theme in
`:core:designsystem` is the Now in Android scaffold (teal and slate, system font). This prompt replaces it.

Prep:

1. Remove all personal data from one real fresher resume and one real JD. Do not upload a real
   person's name, phone number, or email. The DPDP rules in PRD section 10.4 apply.
2. Upload the two cleaned files to the chat. The evidence illustration language must use real
   resume lines and real JD text, not lorem ipsum.

---

```
HireHop is a native Android app (Kotlin, Jetpack Compose, Material 3) for Indian final-year
students and graduates with 0 to 2 years of experience. The core loop: the candidate imports a
resume once and confirms each fact. For each job, they paste a job description (JD). HireHop
shows which requirements they meet, partly meet, or do not meet. It then rewrites the resume for
that job using ONLY confirmed facts. Every tailored line links to the profile fact behind it. The
candidate accepts or rejects each change, then exports a clean PDF or DOCX. The brand promise is
"We never invent anything about you." Tagline: "Your next move, better prepared."

Constraints that shape the design:
- Trust is the product. Raw AI text is free (ChatGPT, Naukri). HireHop sells the workflow and the
  honesty. Every visual choice must make provenance visible: which fact supports which line.
- No "ATS score", no gauges, no dials, no percentages that look like a pass/fail score. Show
  "keyword coverage" only, as a plain fraction ("You cover 9 of 14 key terms").
- No job, interview, or ATS guarantees anywhere. No fake urgency. No paywall at install. The price
  shows before any paid action. Packs, not subscriptions: "5 applications, ₹149, credits never
  expire".
- Gaps are preparation tasks, not failures. A gap must never look like an error or a scolding.
- Users: tier-2 and tier-3 college students on low-end Android phones (3 GB RAM, cold start 2 s or
  less), working-level English readers. Plain words. 48 dp touch targets, TalkBack labels, dynamic
  text size up to 200%. Status must never rely on colour alone.
- The UI is English now. Hindi UI is a later option, so the type system must extend to Devanagari
  without a redesign. Every font must include the rupee sign (₹, U+20B9).
- The exported resume is NOT a brand surface. Export templates stay single-column, plain, standard
  headings, and ATS-parser-safe. The brand lives in the app, the preview frame, and the share card.
Target platform tokens: Material 3 + Jetpack Compose (color roles, type scale, shapes, elevation,
motion specs). The brand palette leads. Dynamic (wallpaper) color stays OFF by default, because the
met / partial / gap status colors must stay fixed.

The current execution is the stock Now in Android scaffold: NiA teal and slate palette, the system
font everywhere, default Material components, and no motion system. It has no identity. Three
failure modes to avoid:
1. Job-board corporate: busy blue lists and badges (the Naukri / LinkedIn look).
2. AI-magic: purple gradients, sparkles, "generate" wands. This look contradicts "we never invent".
3. Scareware: red warnings, score dials, "your resume will be rejected" energy.

Aesthetic direction for THIS app, chosen fresh: EVIDENCE-EDITORIAL. The feel of a sharp editor's
proof on a clean desk: warm paper, confident ink, highlighter marks on the JD, and small margin
citations that tie each rewritten line to its source. It is precise and calm, but young, not a
law firm. The "hop" in the name gives the system ONE energetic register, used only for progress
moments (a gap closed, an application exported). Premium comes from craft and coherence within
that direction. Brand feeling in one line: "a sharp senior who checks every line with you and
never lets you lie."

TASK: Do NOT design any screens yet. Design the FOUNDATIONS: a single style-tile / design-system
board, rendered in light AND dark, that locks the visual language we will then apply flow by flow.
Deliver, with the actual token values labeled on the board:

1. TYPOGRAPHY — a distinctive, OFL / Google Fonts pairing that we can bundle in the APK. Maximum 2
   families plus 1 optional mono, and 4 weights or fewer in total (APK size and cold start). My
   lean: a characterful editorial display face for hero lines (e.g. Bricolage Grotesque or
   Newsreader) + a clear UI/body face that has a Devanagari sibling (e.g. Anek Latin or IBM Plex
   Sans) + an optional mono for source tags and fact IDs. Or propose better. No Roboto anywhere
   visible. Show real specimens, using the uploaded resume and JD text, and the full type scale
   (display, headline, title, body, label) with size / line-height / weight. Give hero numerals an
   oversized TABULAR treatment distinct from the scale: coverage fractions ("9 / 14"), met /
   partial / gap counts, credits left ("4 left"), and prices ("₹149").

2. COLOR — warm paper and deep ink neutrals with real tonal depth (light and dark roles). One
   confident primary for routine actions that is NOT job-board blue. Define these semantic roles
   as named tokens:
   - `met`, `partial`, `gap`: three status roles. Each one pairs with an icon shape and a text
     label. `gap` is a warm "to-do" tone, never error red.
   - `evidence`: a highlighter family for marking JD keywords and linked source facts. This is the
     brand's signature mark.
   - `error`: reserved for real errors and destructive actions (delete account) only.
   - `special`: the ONE reserved accent for the pack purchase moment. Pick a hue far from the
     primary. It must never appear on a routine button.
   All roles must map to Material 3 color roles (primary / secondary / tertiary / surface tiers /
   outline / error), light and dark, and pass WCAG AA contrast.

3. SURFACE & ELEVATION — paper-like surface tints and a small, deliberate elevation ladder
   (hairline borders welcome). Three layers must read apart at a glance: the app background, the
   document (resume and JD text, which should feel like paper), and the tools on top (sheets,
   review cards, bottom bars).

4. ILLUSTRATION DIRECTION — the biggest lever. My lean: the DOCUMENT IS THE ILLUSTRATION. Use real
   resume lines and real JD snippets, highlighted, annotated, and linked by thin provenance threads
   or margin citation marks. Add a small vector spot set (line-drawn, ink-and-highlighter style)
   for empty, error, offline, and "scanned PDF, use the guided form" states. Vector only: no heavy
   raster art and no large Lottie files (low-end devices). Show this as the recommendation and
   sketch ONE alternative. Whatever you pick must feel like ONE family across the app screens AND
   the shareable "JD fit card" image.

5. MOTION — a named motion language (spring specs, durations, easings) for these key moments:
   - the gap-analysis reveal (met / partial / gap groups arrive, must-have gaps first)
   - the per-bullet review: original to new text diff, then accept or reject
   - the source-link reveal: tap a tailored line, the linked profile fact highlights
   - honest waiting for background jobs (up to 60 s): named real steps ("Reading the JD", "Matching
     your facts", "Checking every line"), no fake percentage bar
   - export render, and the credit count going down by one
   - list enter/exit and screen transitions
   Character: crisp, precise, low overshoot, like a confident pen stroke (register `proof`). The
   ONE springy register (`hop`) is for progress moments only. Everything must hold 60 fps on a 3 GB
   device: prefer transform and alpha animations. Honor the system "Remove animations" setting with
   named fallbacks.

6. CORE COMPONENTS — the primitives in the new system, light and dark:
   - primary / secondary / text buttons, and the `special` pack CTA ("5 applications · ₹149 ·
     credits never expire"), with an equal-weight "Not now"
   - the requirement row: requirement text, status (met / partial / gap with icon and label), a
     must-have / nice-to-have tag, and the gap actions "I have this" and "Add to my prep plan"
   - the keyword coverage meter: a plain fraction with a segmented bar, never a gauge
   - the bullet review card: original text, new text, linked source fact, edit-type tags (reword,
     reorder, shorten, emphasise, merge), accept / reject, and a flag for a changed verb or scale word
   - fact provenance chips: `confirmed`, `user-stated`, `user-edited`
   - the "Never invents" trust chip, the credits counter, and the "Report inaccurate content" action
   - application status chips: Saved, Applied, Interview, Offer, Rejected, No response
   - the itemised consent notice row (one purpose per row, with a clear toggle or acknowledgement)
   - the export preview frame (it shows a plain template inside the branded frame)
   - the JD fit share card (role + met / partial / gap counts, no personal data)

Constraint: everything must map cleanly to Material 3 / Jetpack Compose tokens. This will be built
for real in the `:core:designsystem` module (HhTheme). Express the system as a type scale, color
roles, shape scale, spacing grid, elevation steps, and motion specs, not one-off CSS. Output the
foundations board in light AND dark.
```

---

## Answering Claude Design's fork questions (HireHop leans)

- **Type pairing:** the display face must feel sharp and young, and the UI face must stay very
  readable at small sizes on cheap screens. Check the ₹ glyph and the Devanagari sibling before you
  accept a pairing. No Roboto.
- **Special accent:** use it for the pack purchase moment only. If the primary lands in ink green
  or ink indigo, put the special accent in warm territory (marigold, coral), and the reverse.
- **Status colors:** reject any proposal that makes `gap` red. Gaps lead to "Add to my prep plan".
- **Illustration:** the document-as-illustration lean is strong. Real uploaded resume and JD text
  beats abstract shapes. Say no to mascots and AI sparkles.
- **Motion character:** two named registers, `proof` (default, crisp) and `hop` (progress moments
  only). "Decide for me" is fine for exact spring values.
- **Add-ons:** say yes to iconography, spacing and grid tokens, and accessibility notes. Ask for
  contrast checks on the `evidence` highlighter over body text, in light and dark.

## Next phases

After you approve the board, run Phase 2 one flow at a time. Use `prompts/02-flow.md` in
`claude-app-design-prompts`. Suggested order, highest stakes first:

1. First run: install, value screen, JD paste, gap analysis (the free hook, 3 minutes or less).
2. Truth-locked tailoring: per-bullet review and source links.
3. Export preview, packs, and payment.
4. Profile import, confirm, and the fresher evidence path.
5. Application workspace, prep questions, cover letter, settings, and consent.

The build is gated on the Phase 0 validation test (PRD section 12). Hold Phase 3 handoffs until the
gate result is green.
