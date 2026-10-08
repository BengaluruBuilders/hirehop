# TailorMyResume research 05: technical feasibility, cost, and compliance

Date of research: 2026-09-30.
Scope: native Android app (Kotlin, Jetpack Compose), backend, OpenAI pipeline, export, compliance.
Author: research agent 5 of 6. This file feeds the PRD.

## 0. How to read this report

- Source grades: A = official documentation. B = reputable engineering blog, press, law firm, or academic preprint. C = forum, vendor marketing, or anecdote.
- "VERIFIED" means an A or B source confirmed the fact on 2026-09-30.
- "UNVERIFIED" means I could not confirm the fact for 2026. Treat it as an assumption and check it before you commit.
- Web fetch tools returned summaries, not raw pages. I cross-checked the key numbers with a second source where I could.
- The web search budget ran out near the end of the session. Some items in section 13 stay UNVERIFIED for that reason.

## 1. Executive summary

1. The MVP is technically feasible for one experienced Android engineer. The effort estimate is 20 to 30 engineer-weeks, most likely about 24 (section 12).
2. The LLM cost per full application is small. A hybrid model mix costs about USD 0.07 to 0.10 (about INR 6 to 9). An all-small-model mix costs under USD 0.01 (section 5).
3. LLM cost dominates the bill at scale. At 100k MAU, the LLM is more than 95% of the monthly cost (section 8).
4. The hardest technical problem is fabrication control, not cost. A 2026 preprint measured 96.7% of baseline LLM resume rewrites with unsupported claims. Prompt guardrails alone cut that to 50%. TailorMyResume needs a layered design (section 6).
5. Recommended stack: Kotlin + Compose + Room on the phone. Supabase (Auth, Postgres, Edge Functions) as backend. OpenAI Responses API with Structured Outputs. PDF export on device. DOCX export on the server.
6. India DPDP substantive duties become enforceable about May 2027. Build them in from day 1: notice, consent, deletion, breach process, 18+ age gate.
7. Launch in India only at first. This avoids GDPR scope, and it keeps the compliance work small.

## 2. Recommended architecture

```
+-----------------------------------------------------------------+
| ANDROID APP (Kotlin, Compose, Room)                             |
|  - Sign in: Credential Manager (Google ID token)                |
|  - File pick: Storage Access Framework (PDF, DOCX)              |
|  - Room DB = source of truth for profile + applications         |
|  - PDF export on device (PdfDocument)                           |
|  - WorkManager: queued jobs, sync                               |
|  - Play Integrity (standard request) before each LLM job        |
+---------------------------+-------------------------------------+
                            | HTTPS, Supabase JWT, integrity token
                            v
+-----------------------------------------------------------------+
| SUPABASE                                                        |
|  Auth (Google ID token)                                         |
|  Edge Functions = LLM gateway                                   |
|    1. verify JWT + Play Integrity verdict                       |
|    2. check quota (usage ledger in Postgres)                    |
|    3. moderation + input size limits                            |
|    4. call OpenAI (store:false, safety_identifier)              |
|    5. validate JSON schema                                      |
|    6. deterministic fact-diff check                             |
|    7. LLM verifier pass                                         |
|    8. write job result row                                      |
|  Postgres (RLS): profile, applications, jobs, usage_ledger      |
|  Storage: temp resume upload (auto-delete), DOCX output         |
+---------------------------+-------------------------------------+
                            | server-side API key only
                            v
+-----------------------------------------------------------------+
| OPENAI API                                                      |
|  Luna: extraction, JD parse, prep questions                     |
|  Sol: tailoring, cover letter (and verifier if needed)          |
+-----------------------------------------------------------------+
```

Key design rules:

- The OpenAI key lives only on the server. The app never sees it.
- The app calls one job endpoint. The job runs asynchronously. The app polls or subscribes for the result. A multi-step pipeline can take longer than one HTTP request should stay open.
- The model IDs live in server config. A model change needs no app release.
- Room is the source of truth on the phone. The server keeps a copy of the profile and application records for restore on a new phone.

## 3. Resume import

### 3.1 File selection on Android

- Use the Storage Access Framework (`ACTION_OPEN_DOCUMENT`) with MIME types `application/pdf` and the DOCX type. It needs no storage permission. (Source A1)
- Call `takePersistableUriPermission` only if the app must re-read the file later. The MVP does not need it. Read the file once and upload it.
- Use `ACTION_CREATE_DOCUMENT` for "Save as" export. It picks the folder and filename. (A1)
- Use the photo picker only for the later profile-photo feature. It needs no manifest permission. It gives read-only access to the chosen image. It supports Android 11+ natively and older versions through a backport. (A2)
- A phone photo of a paper resume is a later feature. If you add it, use the camera or photo picker, then OCR.

### 3.2 Where to parse: on device or on the backend

| Option | Pros | Cons | Verdict |
|---|---|---|---|
| Send the file to the backend, then to OpenAI file input | Least code. One path for PDF and DOCX. The API extracts text and page images for PDF. (A3) | The raw resume leaves the phone (needs consent and notice). PDF input costs more tokens because the API sends text and images. (A3) | Recommended for MVP |
| Extract text on device (pdfbox-android, Apache License 2.0), send text only | Cheaper tokens. Less data leaves the device. | Extra library, APK size, and layout bugs. DOCX on Android is hard: Apache POI has known Android problems (XML parser, graphics). (B1) | Not for MVP |
| On-device OCR (ML Kit Text Recognition v2) | Works offline. No network needed after the model loads. Supports Latin and Devanagari scripts. (A4) | Only for scans and photos. About 4 MB per script if bundled. | Later, for scanned resumes |

Recommended import flow:

1. The user picks a PDF or DOCX. The app checks size (limit 5 MB) and page count (limit 5).
2. The app uploads the file to the backend over TLS.
3. The backend sends the file to OpenAI as a file input with Structured Outputs. The backend sets `store: false`.
4. The backend deletes the uploaded file after extraction. Do not keep the original unless the user opts in.
5. The app shows an editable profile. Each field shows the source text snippet. The user must confirm the profile. The confirmed profile is the "ground truth" for all later steps.

Details:

- OpenAI file input accepts PDF, DOC, DOCX, RTF, ODT and more in the Responses API. Each file must be under 50 MB. (A3) Chat Completions accepts PDF only. (A3)
- Use the Responses API, not Chat Completions, so DOCX works without a conversion step.
- For a scanned PDF (no text layer), the PDF path sends page images to a vision-capable model. This works, but accuracy drops on low-quality scans. Add a "text quality" check: if the extracted text is short, warn the user and ask for a better file.
- UNVERIFIED: exact token cost per PDF page in 2026. The docs say text and images both count. (A3) Measure it in a spike.

### 3.3 Accuracy for Indian resume formats

- Evidence is thin. No public benchmark covers Indian fresher resumes. (gap)
- Public benchmarks report high field-level F1 for basic fields (about 0.96 to 0.97) and lower F1 for skills (about 0.84 to 0.88) on GPT-4o and Gemini 2.5 Flash. (B2) These are older models, and the data is mostly English and standard layouts.
- Multi-column layouts, tables, and scans lower parse accuracy across the industry. (C1, B3)
- Indian fresher resumes often include: career objective, declaration line, date of birth, photo, father's name, marital status, and multi-column "template" designs. (C2)
- Design consequences:
  - Add profile fields for objective, projects, internships, certifications, and extracurriculars. Fresher resumes rely on these.
  - Do not extract or store DOB, photo, religion, caste, marital status, or father's name by default. This is data minimisation (DPDP) and it avoids bias risk.
  - Build a golden set of 50+ real Indian-format resumes (with consent) before launch. Measure field-level F1. Set a gate (target: 95% on name, contact, education, employers, dates; 85% on skills and projects).

## 4. LLM pipeline design

### 4.1 Steps

| Step | Input | Output (JSON schema) | Model tier |
|---|---|---|---|
| P0 Profile extraction (one time per resume) | resume file | Profile: contact, education[], experience[], projects[], skills[], certifications[], extras[]. Every item has a stable `id`. | Luna |
| P1 JD analysis | JD text | title, company, requirements[] (text, type: skill / experience / education / tool / soft, priority: must-have / nice-to-have, keywords[]) | Luna |
| P2 Gap match | Profile + P1 | per requirement: status (met / partial / gap), evidence_ids[], note | Luna (Sol if quality fails) |
| P3 Tailoring | Profile + P1 + P2 | summary, reordered sections, bullets[]: text, source_ids[], edit_types[], keywords_used[] | Sol |
| P4 Verifier | Profile + P3 output | per bullet: verdict (supported / partial / unsupported / contradicted), reason | Sol or Luna, plus deterministic checks |
| P5 Cover letter | Profile + P1 + P3 | paragraphs[], each with source_ids[] | Sol |
| P6 Prep questions | Profile + P1 + P2 | questions[]: text, why_asked, answer_hint_source_ids[] | Luna |

### 4.2 Structured output support

- OpenAI Structured Outputs enforce a JSON schema. The API returns a `refusal` field when the model refuses. Set `additionalProperties: false` and declare all fields required. (A5)
- UNVERIFIED for 2026: exact limits on property count, nesting depth, and string size. Keep schemas flat and small. Test each schema early.
- Claude structured outputs are GA. The schema subset excludes recursive schemas and numeric or string length constraints. (A6)
- Gemini supports `response_format` with a JSON Schema subset. It warns that very large or deeply nested schemas may be rejected. (A7)
- Design rule: validate every model output in your own code, even with constrained decoding. Schema validity does not mean the content is true.

### 4.3 Current OpenAI models and prices (checked 2026-09-30)

Model naming changed twice in 2026. Older third-party pages still list "GPT-5.6" names. The official docs page shows the GPT-6 family. I use the official page. (A8, A9)

| Model ID | Input / MTok | Cached input / MTok | Output / MTok | Note |
|---|---|---|---|---|
| gpt-6-astra | USD 10.00 | 1.00 | 50.00 | Most capable. Launched 2026-09-03 per one source. (B4) |
| gpt-6.1-sol | USD 2.00 | 0.10 | 10.00 | Balanced. Near-Astra quality per OpenAI. |
| gpt-6-luna | USD 0.10 | 0.01 | 0.50 | Most efficient. 1.05M context. |
| GPT-5.6 Sol (old) | 5.00 (promo 4.00) | 0.50 | 30.00 (promo 20.00) | Older price, now superseded. (B4) |
| GPT-5.4 Nano (old) | 0.20 | n/a | 1.25 | Older price. (B4) |

Other price facts:

- Batch API: 50% off input and output for jobs done within 24 hours. (B4) TailorMyResume jobs are interactive, so batch does not apply. Use it for offline evals.
- Prompt caching: automatic. Minimum 1,024 tokens. Cached input costs 0.1x on most GPT-5.6+ models. GPT-6.1 Sol shows 0.05x. Put the stable prefix (instructions, schema, profile) first. (A10)
- Cache writes on GPT-5.6 and later bill at 1.25x the input rate. (B4) UNVERIFIED for GPT-6.
- Long-context requests above about 272K tokens cost more. (B4) TailorMyResume stays far below this.
- Data residency adds a 10% uplift for models released on or after 2026-03-05. (A11)
- Sol and Luna prices are described as permanent, not promotional. (B5) The price fell by half or more in one release. Expect more change. Keep the model ID and price in config.
- Reasoning models bill hidden reasoning tokens as output tokens. My estimate includes a reasoning allowance. UNVERIFIED: the real reasoning token count per step. Measure it.

### 4.4 Alternatives (cost and structured output only)

| Provider and model | Input / MTok | Output / MTok | Structured output | Note |
|---|---|---|---|---|
| Google Gemini 3.8 Flash | USD 0.75 | 3.75 | Yes, JSON Schema subset (A7) | Price rises to 1.50 / 7.50 on 2027-01-01. (A12) |
| Google Gemini 3.5 Flash-Lite | 0.30 | 2.50 | Yes | Budget tier. (A12) |
| Google Gemini 2.5 Flash-Lite | 0.10 | 0.40 | Yes | Older. May be retired. |
| Anthropic Claude Sonnet 5.5 | 2.00 | 10.00 | Yes, GA (A6) | New tokenizer makes about 30% more tokens for the same text. (A13) |
| Anthropic Claude Haiku 4.5 | 1.00 | 5.00 | Yes | Old tokenizer. (A13) |
| Anthropic Claude Opus 5.5 | 4.00 | 20.00 | Yes | Too costly for this use. (A13) |

Conclusion: OpenAI Luna is the cheapest option by a wide margin. Sol and Claude Sonnet 5.5 share the same list price. Use Claude Sonnet 5.5 as a second-family verifier if you want model separation. The cost is small.

## 5. Cost per full application

Assumption: the profile is already stored. One "full application" = JD analysis + gap match + tailored resume + verifier + cover letter + prep questions + cover-letter check.

Token assumptions (my estimates, UNVERIFIED until measured):

- Fresher profile JSON: 1,500 tokens.
- Pasted JD: 800 tokens.
- Instructions and schema per call: 800 to 1,200 tokens.
- Total input across all calls: about 23,400 tokens.
- Visible output across all calls: about 5,750 tokens.
- Hidden reasoning tokens: 0 to 6,000 depending on model choice.

| Scenario | Model mix | Input tokens | Output tokens (incl. reasoning) | Cost per application (USD) | INR at 90/USD (assumed) |
|---|---|---|---|---|---|
| A. Cheapest | Luna for all steps | 23,400 | about 8,750 | about 0.007 | about 0.6 |
| B. Recommended hybrid | Luna for P1, P2, P6; Sol for P3, P4, P5 | 23,400 | about 11,000 | about 0.10 | about 9 |
| B2. Hybrid with Luna verifier | Luna for P1, P2, P4, P6; Sol for P3, P5 | 23,400 | about 10,000 | about 0.075 | about 7 |
| C. Premium | Sol for all steps | 23,400 | about 11,750 | about 0.16 | about 14 |
| D. Gemini 3.8 Flash for all | Flash | 23,400 | about 11,750 | about 0.06 | about 5 |
| E. Claude Sonnet 5.5 for all | Sonnet 5.5 (+30% tokens) | about 30,400 | about 15,000 | about 0.21 | about 19 |

Working for scenario B (Sol for P3, P4, P5):

- P3 tailoring: 4,000 in + 3,800 out = 0.008 + 0.038 = about USD 0.046.
- P4 verifier: 3,500 in + 1,600 out = 0.007 + 0.016 = about USD 0.023.
- P5 cover letter: 4,400 in + 1,450 out = 0.009 + 0.0145 = about USD 0.023.
- Luna steps (P1, P2, P6, letter check): about USD 0.0035 in total.
- Sum: about USD 0.096. Caching can cut the Sol input cost slightly (a few tenths of a cent).

Other cost lines:

- Profile extraction (one time): about 3,500 input and 1,800 output tokens. Luna: about USD 0.0013. Sol: about USD 0.025.
- Each user regeneration adds about 40% of the cost of that step. Budget 2 regenerations per application: multiply scenario B by about 1.6, giving about USD 0.15.
- Data residency in India needs an OpenAI amendment, and it adds 10%. (A11)
- Prep questions and cover-letter checks are cheap. They do not change the plan.

Read-across for the PRD: the LLM cost per application (INR 1 to 20) is far below any plausible price point in India. Cost is not the constraint. Abuse and free-tier design are (section 9).

## 6. Hallucination and fabrication guardrails

### 6.1 What the evidence says

- A 2026 preprint tested a multi-stage hiring pipeline (resume rewrite, then interview questions, then feedback) on 10 synthetic resumes, 2 JDs, 180 runs. (B6)
  - Baseline: 96.7% of outputs contained unsupported claims (6.8 findings per output).
  - Prompt guardrails: 50% of outputs (0.92 findings per output), 86% fewer findings.
  - Human checkpoint after the rewrite: 75% item-level fabrication. It removed invented identities and cut "trap" JD requirements copied into the resume from 47% to 2%.
  - Human reviewers missed subtle qualifier changes about 45.5% of the time.
  - A newer model still showed 90% baseline fabrication in a supplementary run.
  - Fabrications from the resume step spread into later steps (interview questions, feedback).
- A second 2026 preprint proposed a five-layer defence for resume rewriting. (B7)
  - Layers: temporal validation, deterministic contamination detection, structural invariants, prompt-level grounding, and an evaluator agent.
  - Detected hallucinations fell from 2.48 to 5.36 per resume (undefended) to 0.04 to 0.24 (defended).
  - Prompt grounding alone reached zero detected hallucinations with capable models at low temperature. The deterministic layers mattered for higher temperature and weaker models.
  - Failure types: anachronistic technology injection, cross-domain term contamination, structural mutation, and content fabrication.
- Caveats: both papers are preprints, both use synthetic resumes, and sample sizes are small. Treat the numbers as directional. Grade B.
- Anthropic's guidance lists the same building blocks: allow "I don't know", extract quotes first, make the model cite a quote for each claim and retract claims without support, use best-of-N consistency checks, and restrict the model to supplied documents. (A14)
- Atomic-claim evaluation (FActScore) splits text into atomic facts and scores the share that a source supports. (B8) NLI-style metrics (SummaC, AlignScore) test whether each output sentence is entailed by the source. (B9)

### 6.2 Recommended guardrail design (six layers)

1. **Ground-truth ledger.**
   - The user-confirmed profile is the only source of facts.
   - Every experience, project, skill, and education item has a stable ID.
   - The user can add a new fact by answering a question ("Do you have Docker experience?"). That answer becomes a profile fact tagged "user-stated". The model never adds facts.

2. **Constrained edit schema.**
   - The model does not write free text. It returns a list of bullets. Each bullet has `source_ids` (at least one) and `edit_types` from a fixed list: reword, reorder, shorten, emphasise, merge.
   - Each bullet also lists `keywords_used`. A JD keyword may appear only if the cited source facts contain it, or a known alias of it.
   - JD requirements with no evidence go to the gap list. They never go into the resume text.

3. **Deterministic checks (no LLM, cheap, run first).**
   - Numbers: every number, percentage, and currency amount in the output must appear in the cited source facts.
   - Names: every employer, institution, certification, and product name must match the profile allowlist (with alias normalisation).
   - Tools and skills: every technology term must be in the profile skills or in the cited source text.
   - Dates: no date outside the ranges in the profile. No overlapping-date changes.
   - Structure: same count of employers, same order, same titles.
   - Ownership verbs: keep a verb-strength ladder (assisted < contributed < developed < led < owned). Block any upward move against the source verb. This targets the "subtle qualifier" failure that human reviewers missed 45.5% of the time. (B6)
   - Scale words: block "team of", "users", "revenue" claims that the source lacks.

4. **LLM verifier pass.**
   - Split each bullet into atomic claims. Ask "is this claim entailed by the cited evidence?" Verdicts: supported, partial, unsupported, contradicted.
   - Use a different model or family than the generator when you can (rule from the owner's global preferences: never review with the model that wrote it). A cheap option is Luna as generator-checker only for low-risk steps, and Sol or Claude Sonnet 5.5 as verifier for tailoring.
   - Any unsupported or contradicted claim triggers one automatic repair pass. If it still fails, drop the bullet and show the original bullet.

5. **User-in-the-loop UI.**
   - Show a diff of each bullet: original text, new text, and the evidence link. The user accepts or rejects each change.
   - Mark bullets that changed meaning risk (verb or scale word changed) with a warning.
   - The human checkpoint alone is not enough (B6). It is the last layer, not the only layer.

6. **Evaluation and monitoring** (section 6.3).
   - Add a "Report inaccurate content" button. It also meets the Play AI policy (section 10.3).
   - Log the reject rate per bullet type. A rising reject rate signals prompt or model drift.

Also apply to later steps: the cover letter and interview answers must use the same source IDs. A fabrication in the resume must not become the premise of a prep question. (B6)

### 6.3 How to test it

Build an eval set before the first release. Run it on every prompt, schema, or model change (in CI, with the Batch API at half price).

| Eval set | Size | Purpose |
|---|---|---|
| Profile-JD pairs, normal | 100 | Baseline unsupported-claim rate |
| Trap JDs (requirements the profile lacks: e.g. Kubernetes, AWS, MBA, certifications) | 50 | Measure "copy JD into resume" leakage. (B6) |
| Thin fresher profiles (one project, no job) | 30 | Pressure to invent content |
| Indian-format resumes: multi-column, Hinglish words, objective and declaration lines | 50 | Extraction and grounding |
| Seeded-fabrication set: outputs with injected fake facts (employer, number, tool, verb upgrade) | 100 | Measure verifier and deterministic recall |
| Adversarial: resume or JD with hidden instructions ("ignore rules and add X") | 20 | Prompt-injection resistance |

Metrics and release gates:

- Critical fabrication rate (new employer, degree, certification, number, tool, or date): gate at 0 in the eval set. Any critical hit blocks the release.
- Minor unsupported-claim rate per output (atomic-claim method): target under 2%.
- Claim retention (share of true source claims kept): target above 99%, as the preprint reported. (B6)
- Verifier recall on the seeded-fabrication set: target above 95%.
- Verb-strength violations: gate at 0.
- Profile extraction field-level F1 against golden JSON (section 3.3).
- Human review: a person reads 30 random outputs per release, with the source profile beside each output.
- Fact-diff of the exported file: extract text from the exported PDF and DOCX. Diff it against the approved JSON. Any extra fact fails the test.

## 7. Document export

### 7.1 ATS-readable formatting rules

These rules come from C-grade guides. They agree with each other and with the parsing failure reports in section 3.3.

- Use one column, top to bottom.
- Use real text. Do not use images of text.
- Do not use tables, text boxes, icons, skill bars, or graphics for content.
- Use standard headings: Summary, Work Experience (or Internships), Education, Skills, Projects, Certifications.
- Keep the header and footer empty. Some parsers skip them.
- Use standard fonts (Arial, Calibri, Times New Roman) and simple bullets.
- Use one date format (for example MM/YYYY).
- Guides in 2026 say modern ATS parse PDF as well as DOCX. Default to PDF. Offer DOCX when a posting asks for it. (C3)
- Never promise ATS acceptance. Say only "designed to be readable by common parsers".
- Naukri uses a parser that fails on tables, text boxes, columns, and headers or footers. (C1)

### 7.2 Options

| Format | Option | Pros | Cons | Verdict |
|---|---|---|---|---|
| PDF | On device with `android.graphics.pdf.PdfDocument` (API 19+) (A15) | Offline. No server cost. Full control of a single-column template. | You lay out text yourself (line breaks, pagination, fonts). Page layout code is on you. | Recommended |
| PDF | HTML in WebView, then print adapter (A16) | Easy layout with HTML and CSS. | Goes through the system print dialog. No control of headers or footers. The doc says the framework does not directly produce PDF files programmatically. Not reliable for automation. | Fallback only |
| PDF | Server-side HTML to PDF (Playwright, Gotenberg, WeasyPrint) | Best layout quality. One template for web and app. (C4) | Chromium needs a real container. It does not fit in Supabase Edge Functions. Chromium uses 6 to 11 times more CPU and RAM than a light PDF tool. (C4) Adds a service to run. | Later, if templates grow |
| DOCX | Server-side `docx` library (npm) in an Edge Function | Simple, single-purpose. | UNVERIFIED: Deno compatibility and license in 2026. I know it is MIT and supports Node and browsers. Test it. | Recommended |
| DOCX | Server-side python-docx or docx4j | Mature. | Needs a separate service (Python or JVM). | If you choose a small Kotlin or Python backend |
| DOCX | On device with Apache POI | Offline. | Known Android problems (XML parser setup, graphics calls). Large size. (B1) | Avoid |
| DOCX | On device, hand-built OOXML zip | Small. No library. | Needs care with styles and numbering. | Possible if you want offline DOCX |

Risk on PDF text: the Android doc page I fetched did not state whether `PdfDocument` output keeps selectable text. A tool summary claimed it does not. That claim is UNVERIFIED and may be wrong. Run a one-day spike: export a sample resume, extract text with a PDF text tool, and run it through two public ATS checkers. Decide the PDF path from that result. If text extraction fails, use the server-side HTML-to-PDF path.

Design rule: one approved JSON resume drives both renderers. This keeps PDF and DOCX consistent. It also makes the fact-diff test simple.

## 8. Backend options and cost

### 8.1 Options compared

| Criterion | Supabase (recommended) | Firebase | Small custom service (Kotlin or Node) |
|---|---|---|---|
| Auth with Google | Native: Credential Manager gets a Google ID token, then `signInWithIdToken`. You need a Web client ID and an Android client ID. Use a hashed nonce. (A17) | Firebase Auth. Well known. | You write ID-token verification yourself. |
| Data | Postgres with Row Level Security. Relational fit for profile, applications, documents. | Firestore. Document model is fine. | You pick a database. |
| LLM gateway | Edge Functions. Limits: 150 s (Free) or 400 s (paid) wall clock. 2 s CPU. 256 MB. (A18) | Cloud Functions (Blaze plan). | Any timeouts you set. |
| Abuse control | Rate limit with Postgres ledger or Upstash Redis. (A19) Play Integrity check in a function. | App Check integrates Play Integrity directly. (A20) | You build all of it. |
| Auth price | 100,000 MAU included on Pro. USD 0.00325 per MAU above that. (A21) | 50,000 MAU free. Then USD 0.0055 per MAU (50k to 100k), 0.0046 (100k to 1M). (B10) | Depends on provider. |
| Lock-in | Low. Postgres and open source. | Higher. | Lowest, highest effort. |
| Ops effort | Low | Low | High |

Recommendation: Supabase for a solo or small team. Postgres and RLS suit the data. Native Android Google sign-in works with a documented flow. The Pro plan includes 100,000 MAU. Firebase is a good second choice. Its App Check with Play Integrity is a real advantage. Pick Firebase if the team already knows it.

Edge Function fit:

- The CPU limit is 2 seconds. Waiting for OpenAI is async I/O and does not count. (A18)
- One LLM step takes seconds. The full pipeline can take 30 to 90 seconds. That fits inside the 150 s idle timeout only if each step runs as its own request or as a background task. Use `EdgeRuntime.waitUntil` for work after the response. (A22) The wall-clock cap still applies.
- Safer design: one function per pipeline step. The app (or a job table trigger) runs the steps in order. Store each step result in the job row.

### 8.2 Cost by monthly active users

Assumptions (all UNVERIFIED until you measure real usage):

- 2 full applications per MAU per month on average. Many users will do 0. Some will do 10.
- Recommended hybrid model mix, about USD 0.08 per application (between B2 and B).
- Supabase Pro at USD 25 per month. It includes about USD 10 of compute credit. (A21) Larger compute sizes cost more. My figures for compute (about USD 15 to 210 per month) come from memory and are UNVERIFIED.
- Play Integrity: no fee. The default quota is 10,000 requests per day per app. You can ask for more. (A23)
- Upstash Redis or Postgres for rate limits: free or a few USD.
- Egress and storage stay small (documents are small text files).

| MAU | Applications per month | LLM cost | Supabase (Pro plus compute) | Other (monitoring, email, etc.) | Total per month (approx.) |
|---|---|---|---|---|---|
| 1,000 | 2,000 | USD 160 | USD 25 to 40 | USD 0 to 20 | about USD 190 to 220 |
| 10,000 | 20,000 | USD 1,600 | USD 60 to 150 | USD 20 to 50 | about USD 1,700 to 1,800 |
| 100,000 | 200,000 | USD 16,000 | USD 150 to 700 | USD 100 to 300 | about USD 16,300 to 17,000 |

Notes:

- At 100,000 MAU the LLM is more than 95% of the cost. The model mix is the main cost lever. Scenario A (all Luna) would cut the 100,000 MAU LLM cost to about USD 1,400 per month, but it raises fabrication risk on tailoring.
- Edge Function calls: 200,000 applications x about 6 calls = about 1.2 million per month. The Pro plan includes 2 million. (A21)
- Firebase alternative at 100,000 MAU: Auth alone costs about USD 275 per month above the free 50,000 (B10). It costs USD 0 at 1,000 and 10,000 MAU (both under 50,000). Functions and Firestore add a small amount.
- Play Integrity at 100,000 MAU: 200,000 job requests per month is about 6,700 per day. That is under the 10,000 default. A peak day or a per-step check can exceed it. Ask Google for a quota increase early, and only check integrity on job creation, not on every call.
- Revenue check: at USD 0.15 per MAU per month in LLM cost, and about INR 13 per MAU, any paid plan above INR 99 per month covers the LLM cost many times. The free tier is the risk (section 9).

## 9. Auth, rate limiting, and abuse prevention

- Sign-in: Google only for MVP, through Credential Manager. Use `GetGoogleIdOption` or `GetSignInWithGoogleOption`. Verify the ID token on the server. Check signature, audience, expiry, and nonce. (A24)
- Rate limits, per user: a daily and monthly job quota in a Postgres `usage_ledger`. Per IP: a short-window limit. Global: a daily spend cap with a circuit breaker.
- Free tier design: cap by number of full applications (for example 3 per month), not by requests. Count the verifier and repair calls against the same application.
- Input limits: resume 5 MB and 5 pages. JD 6,000 characters. Cover-letter notes 1,000 characters. Max output tokens per step.
- Play Integrity: use the standard request. Send a `requestHash` of the job request. The server decodes the token with the Play Integrity server API (service account) and checks app recognition, licensing, and device integrity. (A25) Do not cache verdicts. Use tiered enforcement: block on clear failure, throttle on weak signals. (A23)
- Play Integrity does not stop a determined abuser with a real device. Combine it with per-account quotas.
- OpenAI safety controls: call the free moderation endpoint on user text. Send a hashed user ID as `safety_identifier`. Limit input and output tokens. Red-team for prompt injection. (A26)
- Treat resume and JD text as untrusted data. A resume can hide instructions. Use no tools in the LLM calls. Use Structured Outputs. Run the deterministic checks on all output.
- Log every LLM call: user ID (hashed), step, model, token counts, cost, verdicts. This also supports the DPDP log duty (section 10.1).

## 10. Compliance

### 10.1 India: DPDP Act 2023 and DPDP Rules 2025

Status and timeline:

- The Government notified the Rules in November 2025. PIB says 14 November 2025. Other sources say 13 November. (A27, B11)
- The Rules give 18 months for phased compliance. The main duties (notice, security, breach, children, erasure) take effect about 13 or 14 May 2027. (A27, B11, B12)
- Consent manager registration opens about 13 or 14 November 2026. (B11, B12)
- In January 2026, MeitY discussed cutting the window to 12 months, mainly for Significant Data Fiduciaries. I found no confirmed final change. UNVERIFIED. (B13)
- Penalties: up to INR 250 crore for failing to keep reasonable security safeguards. Up to INR 200 crore for failing to notify a breach or for breaching children's-data duties. Up to INR 50 crore for other violations. (A27)
- TailorMyResume is a Data Fiduciary. OpenAI and Supabase are Data Processors. Sign a data processing agreement with each.

Obligation checklist:

| Duty | What TailorMyResume must do |
|---|---|
| Notice | Show a standalone, plain-language notice before you collect data. List the data items and the purposes. A generic privacy policy is not enough. (A27, B12) |
| Consent | Ask for specific, free, informed consent. Do not bundle it with other terms. The user must be able to withdraw as easily as they gave it. (A27) |
| Purpose limit | Use resume data only to build profile, analysis, and documents. Do not use it for ads or model training. |
| Data minimisation | Do not collect DOB, photo, religion, caste, marital status, or family names by default. |
| Access, correction, erasure, nomination | Build in-app screens for these. Respond within 90 days (aim for 7 days). (A27) |
| Erasure on withdrawal | Delete profile, applications, documents, and uploads when the user deletes the account or withdraws consent. Keep only what the law requires. |
| Retention | Delete raw resume uploads right after extraction. Delete data of inactive accounts on a schedule (for example 24 months, with notice). |
| Security safeguards | Encrypt in transit and at rest. Use access controls and RLS. Keep logs of processing for at least one year. (B12) |
| Breach handling | Tell the Data Protection Board without delay, and send a detailed report within 72 hours. Tell each affected user without delay in plain language. (B14) Write the incident runbook before launch. |
| Children | Under 18 needs verifiable parental consent. (A27) TailorMyResume targets adults. Add an 18+ gate. Set the Play target audience to 18+. Do not build a parental-consent flow in the MVP. |
| Grievance and contact | Publish the contact of a grievance officer or Data Protection Officer inside the app. (A27) |
| Cross-border transfer | Transfer is allowed unless the Government restricts a country by notification. (B12) Monitor the notification list. |
| Significant Data Fiduciary duties | Not likely to apply at MVP scale. Check if you grow. |

Design consequence: the account-deletion flow and the data-export flow are core features. They are also required by Google Play.

### 10.2 GDPR (only if you serve the EU)

- Recommendation: do not target the EU at launch. Use Play Console country targeting to limit to India first. Add other markets later.
- If you add the EU: identify a lawful basis (contract or consent), give an Article 13 notice, sign a DPA with OpenAI and Supabase, cover transfers with standard contractual clauses, support access and erasure, report breaches to the authority within 72 hours, and appoint an EU representative if required.
- Article 22 (solely automated decisions with significant effects) is unlikely to apply. TailorMyResume does not decide about the candidate. It helps the candidate write documents. (B15) UNVERIFIED for your final design: recheck if you add job matching or ranking.
- EU AI Act: UNVERIFIED. I did not research it. Candidate-side writing help is unlikely to be "high-risk", but transparency duties may apply. Check before any EU launch.

### 10.3 Google Play

- **Data safety form**: mandatory for every published app, including testing tracks (except internal). Declare all data collected or sent off device, including data sent by SDKs. Declare encryption in transit and whether users can request deletion. A privacy policy link is required. A wrong form can lead to blocked updates or removal. (A28)
  - Data sent to a "service provider" (a processor working on your behalf) has a separate rule. Read the definition. You still must declare the collection. (A28)
- **Account deletion**: an app with account creation must offer an in-app deletion path and a web link for deletion requests. Delete the associated personal data. Say what you keep and why. (A29)
- **AI-generated content policy**: TailorMyResume generates text with AI. The developer must make sure the app does not generate offensive content, and must test the model. The app must include in-app reporting or flagging so users can report offensive content without leaving the app. Use the reports to improve filters. (A30, B16)
  - The official page I fetched did not show the reporting text in the summary. Two other sources confirm it. (B16) The policy scope note says it excludes "productivity tools with minimal AI features". TailorMyResume uses AI heavily, so treat it as in scope.
- **Target API level**: since 2026-08-31, new apps and updates must target Android 16 (API 36) or higher. Extension to 2026-11-01 was possible for some apps. (B17)
- **New personal developer accounts**: closed test with at least 12 testers for 14 days before production access. Organisation accounts with D-U-N-S verification are exempt. (A31, B18) Plan at least 3 weeks for this in the schedule.
- I did not research the 2026 Android developer verification program for sideloaded apps. UNVERIFIED. It does not block Play distribution.

### 10.4 OpenAI data handling

- OpenAI does not train on API data unless you opt in. Abuse-monitoring logs keep data up to 30 days by default. (A32)
- Zero Data Retention (ZDR) is available for eligible customers after approval. With ZDR, `store` is treated as false. The Responses API and Chat Completions are ZDR-eligible with limits. (A32)
- Data residency: India is a supported region for storage only. It does not support regional processing. India needs approval for abuse-monitoring controls (Modified Abuse Monitoring or ZDR) and a Modified Retention amendment. It adds a 10% uplift on new models. (A11)
- Recommended settings: set `store: false` on every call. Send the file inline (or delete any uploaded file right after use). Apply for Modified Abuse Monitoring or ZDR once you have volume. Disclose OpenAI as a processor in the notice and in the Data safety form.
- UNVERIFIED: the exact retention default for Responses API stored objects in 2026. `store: false` avoids the question.

## 11. Local storage and sync

- Use Room as the source of truth on the phone. The UI reads only from Room. Android's offline-first guidance says the same. (A33)
- Write path: "lazy writes". Save to Room first, then queue a sync with WorkManager (network constraint, exponential backoff). (A33)
- Conflicts: last-write-wins per record, using `updated_at`. This is enough for one user on one or two devices. (A33)
- What must live on the server:
  - LLM keys and the LLM gateway.
  - Quotas and the usage ledger.
  - Play Integrity verification.
  - DOCX rendering (if server-side).
  - The account-deletion pipeline.
  - A backup copy of the profile and application records, so the user can restore on a new phone. Phone churn is high in India. A backup is worth the small storage cost.
- What can stay only on the device:
  - Exported PDF and DOCX files (re-generate on demand from stored JSON).
  - Draft edits not yet approved.
  - UI state and cache.
- What works offline: view and edit profile, notes, status, saved documents, and prep questions. What needs network: import, JD analysis, tailoring, cover letter.
- Security: rely on Android file-based encryption. Add SQLCipher only if the threat model requires it. Exclude the database from unencrypted cloud backup, or document it in the Data safety form. UNVERIFIED: choose after a security review.
- MVP shortcut: if schedule is tight, ship without sync (local only, with manual export). Add sync in the next release. This saves about 1 engineer-week but loses restore-on-new-phone. Not recommended if you have the time.

## 12. Effort estimate (one experienced Android engineer, MVP)

Assumptions: one engineer does app and backend. A designer supplies screens. Estimates include unit tests. The engineer uses AI coding tools. They exclude the Play closed-test wait.

| Component | Engineer-weeks |
|---|---|
| Project setup, architecture, CI, theme, navigation | 1.0 |
| Auth (Credential Manager + Supabase), account deletion, Play Integrity | 1.5 |
| Backend foundation (schema, RLS, gateway, quotas, job model, logging) | 1.5 |
| Resume import (picker, upload, extraction, schema, source-snippet review) | 2.0 |
| Profile editor UI (all sections, validation, add-fact flow) | 1.5 |
| JD analysis (prompt, schema, results and gap UI) | 1.5 |
| Grounded tailoring (pipeline, deterministic checks, verifier, diff UI) | 3.0 |
| Cover letter (generation, editor, checks) | 1.0 |
| Export (PDF on device, DOCX on server, share and save) | 2.0 |
| Application tracker (Room, list, detail, notes, status, prep questions) | 2.0 |
| Sync (WorkManager push, restore) | 1.0 |
| Compliance UX (notice, consent, privacy policy, deletion, data export, AI report button, Data safety form) | 1.0 |
| Eval harness and eval set (CI, seeded-fabrication tests) | 1.5 |
| QA (low-end devices), analytics, crash reporting, Play listing | 2.0 |
| **Subtotal** | **22.5** |
| Risk buffer (about 20%) | 4.5 |
| **Total** | **about 27** |

Range: 20 to 30 engineer-weeks. Most likely about 24.

Scope levers if you must cut: drop DOCX (save about 0.5), drop sync (save 1.0), drop prep questions (save 0.5), drop cover-letter checks (save 0.3). The grounding work (tailoring plus evals, 4.5 weeks) is the product's core. Do not cut it.

## 13. Open risks

| # | Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|---|
| 1 | Fabrication or subtle meaning drift (for example "assisted" becomes "led") harms users and the brand. | High without layers | High | Six-layer guardrail (section 6.2). Release gates. Verb-strength check. |
| 2 | Resume parsing fails on Indian formats, scans, and multi-column layouts. | Medium | High (bad profile poisons every later step) | Golden set. Source-snippet review UI. Text-quality warning. Later OCR path. |
| 3 | Free-tier abuse or scripted use runs up the LLM bill. | Medium | Medium | Per-user quotas, spend cap, Play Integrity, input limits. |
| 4 | Model and price churn: names changed twice in 2026, Gemini prices double on 2027-01-01. | High | Medium | Model IDs in server config. Keep eval suite. Avoid deprecated models. |
| 5 | Pipeline time exceeds Edge Function limits. | Medium | Medium | Async job, one function per step, store step results. |
| 6 | Exported PDF text does not parse in ATS tools. `PdfDocument` text behaviour is UNVERIFIED. | Medium | Medium | One-day spike in week 1. Fallback to server-side HTML-to-PDF. |
| 7 | DPDP duties or dates change. The Government may shorten the window. | Medium | Medium | Build the duties now. Track MeitY notices. |
| 8 | Play policy rejection (AI content, data safety, target API). | Low to medium | Medium | Report button, accurate form, API 36 target, 12-tester test plan. |
| 9 | Vendor lock-in and single-vendor outage. | Low | Medium | Schema-first prompts. Provider adapter in gateway. |
| 10 | Prompt injection through resume or JD text. | Medium | Medium | Treat as data. No tools. Structured output. Deterministic checks. |

Items I could not verify (check before you commit):

- OpenAI structured-output schema limits (property count, depth, size) for 2026.
- Token cost per PDF page in the file-input path.
- Hidden reasoning token counts for Luna and Sol on these tasks.
- Supabase compute add-on prices and Mumbai region availability in 2026.
- Whether `docx` (npm) runs in Supabase Edge Functions (Deno).
- Whether `PdfDocument` output keeps selectable text.
- Final DPDP compliance date if MeitY changed it.
- EU AI Act duties, Android developer verification, INR per USD rate (I assumed 90).
- Reasoning-model behaviour: the cost table depends on my token estimates.

## 14. Implications for the PRD

1. Add a hard requirement: "Every generated bullet cites profile facts. The app never adds a fact that the user did not confirm." Add release gates for critical fabrication (target 0) in the eval set.
2. Make the confirmed profile a first-class step. The import screen must show source snippets and require the user to confirm.
3. Add an "answer a question to add a fact" flow. It turns gaps into honest, user-stated facts.
4. Show a per-bullet diff with accept and reject. Do not auto-apply tailoring.
5. Define a free tier by number of full applications (for example 3 per month). LLM cost is low, so the limit is for abuse control, not for margin.
6. Launch India-only on Android 16+ targeting. Set the audience to 18+. Skip GDPR in the MVP.
7. Add these MVP screens: privacy notice and consent, delete account, export my data, report inaccurate content, grievance contact.
8. Do not collect DOB, photo, religion, or marital status in the MVP. Keep the profile-photo feature for later, with its own consent.
9. Use plain claims. Say "designed to be readable by common ATS parsers". Do not say "beats ATS" or "guaranteed".
10. Plan the schedule: about 24 engineer-weeks, plus 3 weeks for Play closed testing and review.
11. Keep the voice mock interview, job discovery, and profile photos out of the MVP. Each adds new data types (voice, photos) that need new consent and Data safety entries.

## 15. Sources

All sources were read on 2026-09-30. Fetch tools returned summaries, so I marked cross-checked facts.

### A: official documentation

| ID | Topic | URL |
|---|---|---|
| A1 | Storage Access Framework | https://developer.android.com/training/data-storage/shared/documents-files |
| A2 | Photo picker | https://developer.android.com/training/data-storage/shared/photopicker |
| A3 | OpenAI file inputs | https://developers.openai.com/api/docs/guides/file-inputs |
| A4 | ML Kit text recognition v2 | https://developers.google.com/ml-kit/vision/text-recognition/v2/android |
| A5 | OpenAI structured outputs | https://developers.openai.com/api/docs/guides/structured-outputs |
| A6 | Claude structured outputs | https://platform.claude.com/docs/en/build-with-claude/structured-outputs |
| A7 | Gemini structured output | https://ai.google.dev/gemini-api/docs/structured-output |
| A8 | OpenAI pricing | https://developers.openai.com/api/docs/pricing |
| A9 | OpenAI models | https://developers.openai.com/api/docs/models |
| A10 | OpenAI prompt caching | https://developers.openai.com/api/docs/guides/prompt-caching |
| A11 | OpenAI data residency | https://developers.openai.com/api/docs/guides/your-data#data-residency-controls |
| A12 | Gemini API pricing | https://ai.google.dev/gemini-api/docs/pricing |
| A13 | Claude pricing | https://platform.claude.com/docs/en/about-claude/pricing |
| A14 | Claude: reduce hallucinations | https://platform.claude.com/docs/en/test-and-evaluate/strengthen-guardrails/reduce-hallucinations |
| A15 | Android PdfDocument (page content was not returned in full) | https://developer.android.com/reference/android/graphics/pdf/PdfDocument |
| A16 | Android: print HTML documents | https://developer.android.com/training/printing/html-docs |
| A17 | Supabase Google sign-in (native Android) | https://supabase.com/docs/guides/auth/social-login/auth-google |
| A18 | Supabase Edge Function limits | https://supabase.com/docs/guides/functions/limits |
| A19 | Supabase rate limiting example | https://supabase.com/docs/guides/functions/examples/rate-limiting |
| A20 | Firebase App Check | https://firebase.google.com/docs/app-check |
| A21 | Supabase pricing | https://supabase.com/pricing |
| A22 | Supabase background tasks | https://supabase.com/docs/guides/functions/background-tasks |
| A23 | Play Integrity overview | https://developer.android.com/google/play/integrity/overview |
| A24 | Sign in with Google (Credential Manager) | https://developer.android.com/identity/sign-in/credential-manager-siwg |
| A25 | Play Integrity standard requests | https://developer.android.com/google/play/integrity/standard |
| A26 | OpenAI safety best practices | https://developers.openai.com/api/docs/guides/safety-best-practices |
| A27 | PIB backgrounder: DPDP Rules 2025 notified (read as PDF) | https://static.pib.gov.in/WriteReadData/specificdocs/documents/2025/nov/doc20251117695301.pdf |
| A28 | Play Data safety form | https://support.google.com/googleplay/android-developer/answer/10787469 |
| A29 | Play account deletion | https://support.google.com/googleplay/android-developer/answer/13327111 |
| A30 | Play AI-generated content policy | https://support.google.com/googleplay/android-developer/answer/14094294 |
| A31 | Play testing rule for new personal accounts | https://support.google.com/googleplay/android-developer/answer/14151465 |
| A32 | OpenAI your data (retention, ZDR) | https://developers.openai.com/api/docs/guides/your-data |
| A33 | Android offline-first guide | https://developer.android.com/topic/architecture/data-layer/offline-first |
| A34 | Firebase pricing | https://firebase.google.com/pricing |
| A35 | Play target API requirements (from search result, not fetched) | https://support.google.com/googleplay/android-developer/answer/11926878 |
| A36 | Google Cloud Identity Platform pricing (from search result, not fetched) | https://cloud.google.com/identity-platform/pricing |

### B: reputable blogs, press, law firms, preprints

| ID | Topic | URL |
|---|---|---|
| B1 | Apache POI on Android (issues) | https://github.com/SUPERCILEX/poi-android |
| B2 | Resume extraction benchmarks (F1 by model) | https://www.scitepress.org/Papers/2025/138379/138379.pdf |
| B3 | Layout-aware resume parsing with LLMs | https://arxiv.org/abs/2510.09722 |
| B4 | OpenAI pricing table (third-party, older names) | https://www.cloudzero.com/blog/openai-pricing/ |
| B5 | GPT-6 Sol and Luna release and price cut (press) | https://thenewstack.io/openai-gpt-6-sol-luna-release/ |
| B6 | Fabrication in multi-stage LLM hiring pipelines (preprint) | https://arxiv.org/abs/2608.26171 |
| B7 | Grounded Optimization: layered framework for resume rewriting (preprint) | https://arxiv.org/abs/2607.01457 |
| B8 | FActScore | https://arxiv.org/abs/2305.14251 |
| B9 | SummaC and AlignScore overview (from search results) | https://arxiv.org/html/2505.04847v2 |
| B10 | Firebase Auth MAU tiers (secondary summary of A36) | https://supertokens.com/blog/firebase-pricing |
| B11 | DPDP Rules 2025 roadmap (law firm) | https://www.lexology.com/library/detail.aspx?g=bbd416e3-04a5-4f77-a83b-76a01aeda951 |
| B12 | DPDP Rules practical guide and checklist | https://www.scrut.io/post/dpdp-rules |
| B13 | MeitY plan to shorten timeline (law firm article) | https://chambers.com/articles/meity-plans-to-cut-short-dpdp-compliance-timeline-and-notify-cross-border-restrictions-for-sdfs |
| B14 | DPDP Rule 7 text (unofficial mirror) | https://www.dpdpa.com/dpdparules/rule7.html |
| B15 | GDPR Article 22 | https://gdpr-info.eu/art-22-gdpr/ |
| B16 | Play AI content policy: in-app reporting (press) | https://9to5google.com/2023/10/25/google-play-gen-ai-policy/ |
| B17 | Play target API 36 deadline | https://support.google.com/googleplay/android-developer/answer/11926878 |
| B18 | 12 testers rule explained | https://www.testerscommunity.com/blog/google-play-12-testers-policy |
| B19 | GPT-6.1 Sol coverage (press) | https://thenextweb.com/news/openai-gpt-6-1-sol-price-astra-devday |
| B20 | Gemini pricing table, September 2026 | https://benchlm.ai/google/api-pricing |
| B21 | OpenAI pricing table, September 2026 | https://benchlm.ai/openai/api-pricing |

### C: forums, vendor marketing, anecdote

| ID | Topic | URL |
|---|---|---|
| C1 | Naukri parser and resume failure causes | https://www.naukri.com/naukri360/ats-resume-checker |
| C2 | Indian fresher resume structure | https://www.rezup.in/resume-format/fresher |
| C3 | ATS format guide 2026 (single column, PDF vs DOCX) | https://scale.jobs/blog/ats-resume-format-2026-design-guide |
| C4 | HTML-to-PDF benchmark 2026 | https://pdf4.dev/blog/html-to-pdf-benchmark-2026 |
| C5 | Resume parsing accuracy (vendor blog) | https://www.thehirehub.ai/blog/ai-resume-parsing-in-2026-how-it-works-how-accurate-it-actually-is-and-what-breaks-it |
