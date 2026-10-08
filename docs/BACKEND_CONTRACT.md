# TailorMyResume backend contract, v1

Status: proposed 2026-10-07. Owner: the TailorMyResume app. The backend is `BengaluruBuilders/apps-backend`.
The app decides the shapes in this file. The backend may change an internal detail. A change to a
request, a response, a status, or an error code needs a change to this file first.

This file replaces the mock gateways of `docs/MOCK_BACKEND.md` with real routes. Read that file for
the interfaces that each route backs.

## 1. Decisions

| # | Decision | Why |
|---|---|---|
| D1 | The device keeps the profile and the applications (Room). The server stores no profile, no application, no resume, and no JD after a request ends. Only tailored bullets wait 24 hours for the app to collect them (section 6). Sync is after the MVP. | Smallest DPDP surface. A lost profile is re-imported in 3 minutes. Credits, the money part, live on the server. |
| D2 | The device reads the text of the PDF or DOCX (it does this today). The app uploads no file. TailorMyResume does not use `/v1/files`. | PRD F1.6 ("delete the file after extraction") holds by design. No R2 object to delete. |
| D3 | TailorMyResume gets typed routes under `/v1/tailormyresume/`. It does not call `/v1/ai/extract` directly. The routes reuse the core reserve and settle code. | The server checks every cited id against the facts it got, counts the daily allowance in the same transaction, and runs the verifier model. A generic `input` string cannot do this. |
| D4 | The server owns the counts: credits, daily analyses, the daily free tailoring, unlocked applications, purchases. The device stops counting. | A reinstall must not reset a count. Purchased credits must survive a lost phone. |
| D5 | One credit unlocks one application, once. Every later export of that application is free (PDF, DOCX, again). | PRD 8.1 sells "5 full applications". Today the app spends a credit on every export, so PDF plus DOCX costs 2. |
| D6 | Tailoring is a job: start, then poll. The other AI routes answer in the same request. | Tailoring runs up to 4 model calls (generate, verify, repair, verify) and can pass 60 s. A dropped mobile connection must not bill twice. |
| D7 | Two fabrication defences. The server checks cited ids, keyword grounding, and claim support (a second model, one repair). The device then runs the existing `FabricationGuard` as the last gate. | Constitution I.1 keeps its CI gate on the device. PRD 6.4 item 4 needs a different model as verifier. |
| D8 | AI routes never get the name, the email, or the phone. They get confirmed facts only (except resume parse, which gets the resume text). | Data minimisation, DPDP. The app adds the name to the cover letter and the export. |
| D9 | The server refuses AI routes until the user has granted `ai-processing` and `age-18-plus` for the current notice version. | Processing needs consent first (DPDP). A grant of an old notice does not count. The check is one query. |
| D10 | The TailorMyResume day is the calendar day in `Asia/Kolkata`. | India-only product. "Today" must mean the user's day. The core AI quota keeps its UTC day. |
| D11 | The server checks each Google Play purchase and consumes it. The purchase is bound to the user through `obfuscatedAccountId`. | A replayed token from another account gets no credit. The client has one less step that can fail. |
| D12 | The paywall is enforced on the device. The server limits cost, not access. | The device renders the PDF and DOCX, so the server never sees an export. A modified client can skip the unlock. The per-user AI quota and the app cost cap bound what such a client can spend. Binding unlocks to content would not close the export path. |

### Backend prerequisites

The current `apps-backend` has none of these yet. TailorMyResume issue 1 and issue 2 add them.

- Status `402` in `ApiStatus` and the new codes of section 2 in `ApiErrorCode` (`src/core/errors.ts`).
- Model tiers `extraction`, `generation`, `verifier`, each with its own price. Today `src/apps.ts` has
  `models.default` and an optional `premium`, and `extract()` always uses `default`.
- A function that app routes call to run one structured model call through `reserveAiCall` and
  `settleAiCall`, and that returns the `ai_usage` row id.

## 2. Conventions

- Base URL: the Fly.io host of `apps-backend`. All paths start with `/v1`.
- Every request sends `X-App-Id: tailormyresume` and `Authorization: Bearer <Firebase ID token>`.
  An unknown `X-App-Id` gets the core `400 APP_ID_INVALID` or `400 APP_NOT_FOUND`. A `/v1/tailormyresume/*`
  route called with another registered app id answers `404 NOT_FOUND`.
- JSON bodies, UTF-8, `camelCase` keys. Enum values are the Kotlin enum names (`MET`, `EXPERIENCE`).
- Times are ISO 8601 in UTC (`2026-10-07T18:30:00Z`). A TailorMyResume day is `YYYY-MM-DD` in `Asia/Kolkata`.
- An unknown field in a request body is `400 INVALID_INPUT`. A response can get new fields; the app ignores unknown fields.
- Errors use the existing envelope: `{"error":{"code":"...","message":"..."}}`. The app branches on `code`, never on `message`.
- The server never logs a request body, a response body, a resume, a JD, or a fact.
- Sync AI routes stop after 45 s on the server. The app waits 60 s.

### Error codes

The codes in the first table exist in `src/core/errors.ts`. The second table is new.

| Status | Code | When |
|---|---|---|
| 400 | `INVALID_INPUT` | Bad body, a limit in this file is broken |
| 401 | `UNAUTHENTICATED`, `INVALID_TOKEN` | No token, bad token. The app gets a fresh Firebase token and tries once more |
| 403 | `CROSS_APP_TOKEN`, `FORBIDDEN` | Token of another app; a resource of another user |
| 404 | `NOT_FOUND` | Unknown id, or a TailorMyResume route called with another `X-App-Id` |
| 409 | `ACCOUNT_DELETED` | Deletion pending or done |
| 413 | `PAYLOAD_TOO_LARGE` | Body over the route limit |
| 429 | `RATE_LIMITED` | Request rate. `Retry-After` header |
| 429 | `QUOTA_EXCEEDED`, `BUDGET_EXCEEDED` | Core AI quota (per user per UTC day) or app cost cap |
| 502 | `AI_PROVIDER_ERROR` | The model failed or gave output that fails the schema |
| 405, 500 | `METHOD_NOT_ALLOWED`, `HTTP_ERROR`, `INTERNAL_ERROR` | Core. The app shows a general error |

| Status | Code | When |
|---|---|---|
| 402 | `NO_CREDIT` | Unlock or tailoring with no credit, no unlock, and no free tailoring left |
| 403 | `CONSENT_REQUIRED` | AI route without a grant of `ai-processing` and `age-18-plus` for the current notice version. The app opens the Consent screen |
| 409 | `PURCHASE_PENDING` | Play reports the purchase as pending |
| 400 | `PURCHASE_INVALID` | Play reports the purchase as cancelled, or answers 400 or 404 for the token |
| 429 | `ALLOWANCE_EXHAUSTED` | No TailorMyResume daily analysis left for a new JD |
| 502 | `PLAY_UNAVAILABLE` | The Google Play Developer API did not answer, or answered 401, 403, 429, or 5xx. The app keeps the token and tries again |

## 3. Shared shapes

### `ProfileFacts`

The confirmed part of the profile. The app sends only confirmed entries and their bullets. It never
sends `fullName`, `email`, `phone`, or `headline`.

```json
{
  "skills": ["SQL", "Excel", "Power BI"],
  "entries": [
    {
      "id": "EXP-01",
      "category": "EXPERIENCE",
      "title": "Data Operations Associate",
      "organization": "Saffron Retail",
      "startDate": "Jul 2024",
      "endDate": "Present",
      "source": "IMPORTED",
      "bullets": [{ "id": "EXP-01-1", "text": "Cleaned weekly sales data for 40 stores in Excel." }]
    }
  ]
}
```

| Field | Rule |
|---|---|
| `category` | `EDUCATION`, `EXPERIENCE`, `PROJECT`, `CERTIFICATION`, `ACHIEVEMENT` |
| `source` | `IMPORTED`, `USER_STATED`, `USER_EDITED` |
| ids | Entry and bullet ids are opaque, `^[A-Za-z0-9:_.-]{1,64}$`, and unique over all entries and bullets |
| sizes | At most 40 entries, 15 bullets per entry, 200 bullets in total, 400 characters per bullet, 100 skills, 60 characters per skill, 200 characters per other text field. This fits in the 256 KB body limit of the routes that take it, also for text in Indian scripts (3 bytes per character) |

**Evidence ids.** The evidence ids of a request are: every entry id, every bullet id, and
`skill:<skill>` for each skill, written exactly as sent (a skill id may hold spaces and symbols, as
in `skill:C++`; the id regex does not apply to it). This is the format the device uses today
(`EvidenceSources.SKILL_ID_PREFIX`). Any id that a response cites must be an
evidence id of the same request. The server removes every other id before it answers.

### `Job` and `Match`

```json
{
  "title": "Associate Analyst",
  "company": "Northwind GCC",
  "requirements": [
    { "id": "req-1", "text": "Must have strong SQL", "type": "SKILL", "priority": "MUST_HAVE", "keywords": ["sql"] }
  ]
}
```

```json
{ "requirementId": "req-1", "status": "MET", "evidenceIds": ["EXP-01-1", "skill:SQL"] }
```

| Field | Rule |
|---|---|
| `type` | `SKILL`, `TOOL`, `EXPERIENCE`, `EDUCATION`, `SOFT_SKILL` |
| `priority` | `MUST_HAVE`, `NICE_TO_HAVE` |
| `status` | `MET`, `PARTIAL`, `GAP`. `GAP` has no evidence ids. `MET` and `PARTIAL` have at least one |
| sizes | At most 40 requirements, 10 keywords each |

The server sets the requirement ids (`req-1`, `req-2`, ...). The app sends `Job` and the matches back
unchanged in later requests.

### `generationId`

Every AI answer carries a `generationId`: the id of the first `ai_usage` row of that request. The app
stores it with the content and sends it with a content report. The `ai_usage.task` value of a
TailorMyResume call names the prompt version (`tailormyresume.tailor.v1`), so a report links to the model and
the prompt version with no new column.

## 4. Routes

### 4.1 Account (exists in core)

| Route | Use in TailorMyResume |
|---|---|
| `GET /v1/me` | Call once after each Firebase sign-in. It creates the user row |
| `POST /v1/me/consents` | One call per purpose on the Consent screen. Body `{"purpose","granted","policyVersion"}` |
| `DELETE /v1/me` | Delete account. It must also delete every TailorMyResume table in this file (section 6) |

TailorMyResume consent purposes (one row each):

| Purpose | Screen text it records |
|---|---|
| `read-and-build` | Read my resume and build my profile |
| `ai-processing` | Send my resume text, confirmed facts, and job descriptions to TailorMyResume servers and our AI provider to analyse and tailor. Replaces today's "analyse on device" |
| `keep-confirmed-facts` | Keep my confirmed facts on this device |
| `age-18-plus` | I am 18 or older |

`policyVersion` is `ConsentRecord.CURRENT_NOTICE_VERSION` in lower case (today `2026-10-b`). It must
change when the notice text changes. The server holds the current version in TailorMyResume config
(`noticeVersion`). The AI routes need the newest row of `ai-processing` and of `age-18-plus` to be
`granted = true` with `policyVersion` equal to `noticeVersion`. A new notice version makes every
user consent again; the app reacts to `403 CONSENT_REQUIRED` by opening the Consent screen.
A withdrawal does not stop a tailoring job that already runs.

### 4.2 `POST /v1/tailormyresume/resume/parse`

Backs `ResumeTextParser`. Sync. Counts against the core AI quota. Does not use a daily analysis.
Needs consent. Body limit 128 KB.

Request:

```json
{ "text": "Priya Deshmukh\nB.Tech Computer Science ..." }
```

`text`: 50 to 30,000 characters, after the device read the PDF or DOCX.

Response `200`:

```json
{
  "generationId": "6f1c...",
  "profile": {
    "fullName": "Priya Deshmukh",
    "email": "priya@example.com",
    "phone": "+91 98xxxxxx10",
    "headline": "Data analyst, B.Tech CS 2024",
    "skills": ["SQL", "Excel"],
    "entries": [
      {
        "ref": "e1",
        "category": "EXPERIENCE",
        "title": "Data Operations Associate",
        "organization": "Saffron Retail",
        "startDate": "Jul 2024",
        "endDate": "Present",
        "bullets": [{ "ref": "e1b1", "text": "Cleaned weekly sales data for 40 stores in Excel." }]
      }
    ]
  },
  "droppedSensitive": ["DATE_OF_BIRTH", "MARITAL_STATUS"]
}
```

Rules:

1. `ref` values are unique in the answer and mean nothing after it. The app gives each entry and bullet
   its own id with `FactIdAllocator`, sets `source = IMPORTED` and `isConfirmed = false`.
2. Extraction is verbatim. The server drops any bullet, title, or organization with a word that is not
   in `text` (case-insensitive, punctuation and bullet glyphs ignored).
3. The model never returns date of birth, photo, religion, caste, marital status, or gender. When the
   resume has one, the answer names it in `droppedSensitive`: `DATE_OF_BIRTH`, `PHOTO`, `RELIGION`,
   `CASTE`, `MARITAL_STATUS`, `GENDER`. The app tells the user (PRD F1.7).
4. A resume with no entries gives `200` with empty `entries`. The app opens the guided form.

### 4.3 `POST /v1/tailormyresume/analyses`

Backs `AnalyzeJobUseCase` (JD analysis and gap match in one route). Sync. Needs consent. Body limit
256 KB.

Request:

```json
{ "jobText": "About the role ...", "profile": { "skills": [], "entries": [] } }
```

`jobText`: at least 20 words, at most 20,000 characters.

Response `200`:

```json
{
  "generationId": "a81d...",
  "job": { "title": "Associate Analyst", "company": "Northwind GCC", "requirements": [] },
  "matches": [{ "requirementId": "req-1", "status": "MET", "evidenceIds": ["EXP-01-1"] }],
  "allowance": { "analysesLeftToday": 2, "day": "2026-10-07", "resetsAt": "2026-10-07T18:30:00Z" }
}
```

Rules:

1. One match per requirement, in requirement order.
2. The server removes evidence ids that are not evidence ids of the request. A `MET` or `PARTIAL`
   match with no id left becomes `GAP`.
3. Daily allowance: 3 distinct JDs per TailorMyResume day. The key is SHA-256 of `jobText` after trim and
   whitespace collapse. A second analysis of the same key on the same day is free. A failed analysis
   counts nothing. With 0 left and a new key: `429 ALLOWANCE_EXHAUSTED`, and the model is not called.
4. The count is a claim, because the model call cannot be inside a database transaction. Before the
   call, under the per-user advisory lock, the server checks the count and inserts a `PENDING` claim
   (unique on user, day, kind, key). Pending claims count as used. On success the claim becomes
   `USED`. On failure the server deletes it. A pending claim older than 10 minutes counts as failed.
5. Keyword coverage is not in the answer. The device counts it from `requirements[].keywords` and
   the profile with its existing alias rules.
6. The server stores only the key hash and the day, to count. Never the text.

### 4.4 Tailoring jobs

Backs `ResumeTailor`. Needs consent. Body limit 256 KB.

#### `POST /v1/tailormyresume/tailorings`

```json
{
  "requestId": "0b6c4c1e-2f7a-4b7e-9d8f-3a1e5b2c9d10",
  "applicationId": "4f0c...",
  "job": { "title": "...", "company": "...", "requirements": [] },
  "matches": [],
  "profile": { "skills": [], "entries": [] },
  "section": null
}
```

| Field | Rule |
|---|---|
| `requestId` | UUID from the app, unique per user. A repeat with the same `requestId` returns the same job and starts nothing. The server ignores the body of a repeat |
| `applicationId` | `^[A-Za-z0-9_-]{1,64}$`. Device id of the application |
| `section` | `null` for the full resume. An `EntryCategory` to regenerate one section |

Response `202`: `{"tailoring":{"id":"tl_...","status":"RUNNING"}}`. Repeat of a `requestId`: `200` with the current job.

Who may tailor. The server checks, in this order: the application is unlocked; the wallet has a
credit; a free tailoring of this application is already `USED` (any day); a free tailoring is left
today. If none is true: `402 NO_CREDIT`. When only the last check allows a full (not a section)
tailoring, the server inserts a `PENDING` free-tailoring claim keyed by `applicationId` before the job
starts, as in 4.3 rule 4. It becomes `USED` when the job succeeds and is deleted when the job fails.
A section regeneration never makes a claim. The device keeps the 2-regeneration limit
(`TailoringReviewStateRepository`); the core AI quota bounds the cost (D12).

At most 4 jobs run at the same time on one API machine. A start over that limit gets
`429 RATE_LIMITED` with `Retry-After: 10`.

#### `GET /v1/tailormyresume/tailorings/{id}`

```json
{
  "tailoring": {
    "id": "tl_...",
    "status": "SUCCEEDED",
    "result": {
      "generationId": "c2e0...",
      "bullets": [
        {
          "id": "t-1",
          "entryId": "EXP-01",
          "sourceIds": ["EXP-01-1"],
          "proposedText": "Cleaned and checked weekly sales data for 40 stores in Excel.",
          "editTypes": ["REWORD"],
          "keywordsUsed": ["excel"],
          "verification": "PASSED"
        }
      ]
    }
  }
}
```

| Field | Rule |
|---|---|
| `status` | `RUNNING`, `SUCCEEDED`, `FAILED`. While `RUNNING` the answer has `Retry-After: 2` |
| `failureCode` | Only when `FAILED`: `AI_PROVIDER_ERROR`, `QUOTA_EXCEEDED`, `BUDGET_EXCEEDED`, `INTERRUPTED` |
| `editTypes` | `REWORD`, `REORDER`, `SHORTEN`, `EMPHASISE`, `MERGE` |
| `verification` | `PASSED`: verifier found every claim supported. `REPAIRED`: passed after the one repair. `REVERTED`: failed after repair, `proposedText` is the text of the first source bullet and `editTypes` is empty. `UNCHANGED`: the model left out a source bullet, so the server added it back as is |

Rules:

1. Pipeline: generate, check, verify with the verifier model (a different model from the generator),
   repair once the bullets that fail, verify again, revert the bullets that still fail.
2. Checks before the verifier: every `sourceIds` value is a bullet id of the request; all sources of a
   bullet belong to `entryId`; every `keywordsUsed` value is in `proposedText` and in the text of a
   cited source. A failed check counts as a verifier failure.
3. Claim retention: every bullet in scope (all confirmed bullets, or the bullets of `section`) is a
   source of at least one answer bullet. The server adds a missing one as `UNCHANGED`.
4. Employer count, order, and titles do not change: the answer has no bullet for an entry outside the request.
5. The job runs in the API process. Each job row stores the boot id of the process that runs it. A
   `RUNNING` job of an older boot id reads as `FAILED` with `INTERRUPTED` at once (for example after
   a deploy), and so does a `RUNNING` job older than 5 minutes. Its free-tailoring claim is deleted.
   The app starts a new job with a new `requestId`. Model calls that the kill cut off stay `pending`
   in `ai_usage` and count until the UTC day ends, as the core rules say.
6. The server keeps the result (the tailored bullets) for 24 hours after the job ends, so the app can
   collect it, then deletes the row. It never stores the request body. A job of another user answers `404`.
7. The app polls every 2 s while the screen is open, and from a WorkManager job when it is not, then
   notifies the user (PRD F3 acceptance).

### 4.5 `POST /v1/tailormyresume/prep-questions`

Backs `PrepQuestionSource`. Sync. Needs consent. Body limit 256 KB. No credit check: the core AI
quota bounds the cost (D12).

Request: `{"job": Job, "matches": [Match], "profile": ProfileFacts, "limit": 8}`. `limit` is 1 to 12,
default 8 (`PrepQuestionSource.limit`).

Response `200`:

```json
{
  "generationId": "9b2a...",
  "questions": [
    {
      "id": "q-1",
      "kind": "STRENGTH",
      "requirementId": "req-1",
      "prompt": "Tell me about a time you cleaned messy sales data.",
      "why": "The role asks for strong SQL and data cleaning.",
      "backingFactIds": ["EXP-01-1"],
      "gapAdvice": null
    }
  ]
}
```

Rules:

1. The server asks for `limit` questions. It drops each invalid question and does not call again.
2. `STRENGTH` and `CLARIFY`: the requirement is `MET` or `PARTIAL`, `backingFactIds` is not empty and
   holds evidence ids only, `gapAdvice` is `null`.
3. `GAP`: the requirement is `GAP`, `backingFactIds` is empty, `gapAdvice` is one honest way to talk
   about the gap. A gap is never the premise of a `STRENGTH` or `CLARIFY` question (PRD F3.7).

### 4.6 `POST /v1/tailormyresume/cover-letters`

Backs `CoverLetterSource`. Sync. Needs consent. Body limit 256 KB. No credit check (D12).

Request: `{"job": Job, "matches": [Match], "profile": ProfileFacts}`.

Response `200`:

```json
{
  "generationId": "e7d1...",
  "letter": {
    "greeting": "Dear Hiring Manager,",
    "paragraphs": [
      { "role": "OPENING", "text": "...", "sourceIds": [] },
      { "role": "EVIDENCE", "text": "...", "sourceIds": ["EXP-01-1"] },
      { "role": "CLOSING", "text": "...", "sourceIds": [] }
    ],
    "wordCount": 186
  }
}
```

Rules:

1. 150 to 220 words. No name, no signature. The app adds the candidate's name.
2. An `EVIDENCE` paragraph cites at least one evidence id. Every claim about the candidate in any
   paragraph is checked against the cited facts by the verifier model, with one repair.
3. A paragraph that still fails is removed. If no `EVIDENCE` paragraph is left: `502 AI_PROVIDER_ERROR`.
4. A `GAP` requirement is never stated as a strength.

### 4.7 Wallet and unlock

Backs `UsageAllowance` and the credit part of `PaymentGateway`.

#### `GET /v1/tailormyresume/wallet`

The first call creates the wallet with 1 free credit (insert, `ON CONFLICT DO NOTHING`).

```json
{
  "wallet": {
    "freeCredits": 1,
    "purchasedCredits": 0,
    "analysesLeftToday": 3,
    "freeTailoringsLeftToday": 1,
    "day": "2026-10-07",
    "resetsAt": "2026-10-07T18:30:00Z",
    "unlockedApplicationIds": []
  }
}
```

Daily values: 3 analyses, 1 free tailoring. They are server config, not app constants.

#### `POST /v1/tailormyresume/applications/{applicationId}/unlock`

No body. Spends one credit, free first, once per application.

- `201` first time, `200` on a repeat (no second spend). Body:
  `{"unlock":{"applicationId","creditKind":"FREE","unlockedAt"},"wallet":{...}}`.
- `402 NO_CREDIT` when no credit is left. The app opens the pack screen.

The app calls it before the first export of an application. It records `creditKind` in the
`ExportRecord` of that first export, and `null` for every later export.

#### `GET /v1/tailormyresume/packs`

```json
{ "packs": [{ "productId": "application_pack_5", "credits": 5, "creditsExpire": false }] }
```

Name and price come from Play Billing `ProductDetails` (localised, GST included). The server owns
the credit count of each product.

### 4.8 Purchases

Backs the purchase part of `PaymentGateway`.

The app sets `obfuscatedAccountId` in `BillingFlowParams` to the lower-case hex SHA-256 of the
Firebase uid (64 characters).

#### `POST /v1/tailormyresume/purchases`

```json
{ "productId": "application_pack_5", "purchaseToken": "opaque token from Play" }
```

The app sends it after Play reports `PURCHASED`, and again at each start for every purchase that
`queryPurchasesAsync` still returns (restore).

Server steps:

1. `productId` must be in the pack list, else `400 INVALID_INPUT`. `purchaseToken` at most 4,096 characters.
2. A token already stored for this user returns the stored purchase, `200`, with no Play call except
   a consume that is still due (step 6).
3. Call `purchases.products.get` for package `com.tailormyresume.app` and the given `productId`.
   `purchaseState` 0 goes on, 2 is `409 PURCHASE_PENDING`, 1 is `400 PURCHASE_INVALID`. Play 400 or
   404 is `400 PURCHASE_INVALID`. No answer, or Play 401, 403, 429, or 5xx, is `502 PLAY_UNAVAILABLE`.
4. `obfuscatedExternalAccountId` must equal the hex SHA-256 of the caller's uid. A missing or other
   value is `403 FORBIDDEN`. This also stops a token of another user, so no separate code exists.
5. One transaction: insert the purchase (unique on the token), add the credits. Store
   `purchaseType` too: a licence-tester purchase (`purchaseType` 0) grants credits, so the closed
   test works, and is marked `test` in the row.
6. Call `purchases.products.consume`. On failure, keep the grant and answer as normal. The next call
   with the same token, and the hourly backend loop, try the consume again until it succeeds. Play
   refunds a purchase that is not acknowledged within 3 days.

Response `201` (first grant) or `200` (repeat):

```json
{
  "purchase": {
    "orderId": "GPA.3301-...",
    "productId": "application_pack_5",
    "creditsGranted": 5,
    "purchasedAt": "2026-10-07T09:12:00Z",
    "state": "COMPLETED"
  },
  "wallet": {}
}
```

The app does not call `consumeAsync`. A pending purchase stays on the device (Play state) and shows
as pending; the server does not know it.

#### `GET /v1/tailormyresume/purchases`

`{"purchases":[Purchase]}`, newest first.

### 4.9 `POST /v1/tailormyresume/content-reports`

Backs `ContentReportRepository.report`. Body limit 8 KB.

```json
{ "applicationId": "4f0c...", "itemKind": "RESUME_BULLET", "itemId": "t-1", "generationId": "c2e0...", "itemText": "..." }
```

| Field | Rule |
|---|---|
| `applicationId` | Device id of the application, or the draft key of Gap analysis (`analysis-draft-<hash>`). `^[A-Za-z0-9_-]{1,80}$` |
| `itemKind` | `REQUIREMENT`, `RESUME_BULLET`, `SECTION`, `COVER_LETTER`, `PREP_QUESTION` |
| `generationId` | The id from the answer that made the item, or `null` |
| `itemText` | 1 to 2,000 characters. The reported text, so the team can check it. It is personal data and is kept as section 6 says |

`201 {"report":{"id","reportedAt"}}`. The same user, `applicationId`, kind, and id again: `200` with
the first report. No consent check: a report must always work.

### 4.10 `GET /v1/tailormyresume/me/export`

Backs the server part of `AccountDataExporter`. The app adds it to the zip as `server.json`.

```json
{
  "export": {
    "generatedAt": "...",
    "user": { "id": "...", "createdAt": "..." },
    "consents": [],
    "wallet": {},
    "unlocks": [],
    "purchases": [],
    "contentReports": []
  }
}
```

## 5. Limits that bind the app

| Limit | Value | Owner |
|---|---|---|
| AI calls per user per UTC day | 50 (core). One tailoring is up to 4 calls | Core config |
| AI cost per app per day | $5 (core). Raise before launch | Core config |
| Analyses per TailorMyResume day | 3 distinct JDs | TailorMyResume config |
| Free tailorings per TailorMyResume day | 1, only with no credit and no unlock | TailorMyResume config |
| Free credits for a new user | 1 | TailorMyResume config |
| Requests per user per minute | 60 (core) | Core config |

## 6. Server storage and deletion

| Table | Holds | Kept until |
|---|---|---|
| `tailormyresume_wallets` | user id, free and purchased credits | Account deletion |
| `tailormyresume_usage_claims` | user id, day, kind (`ANALYSIS`, `FREE_TAILORING`), key (JD hash or application id), status, time | 30 days, or account deletion. A `USED` free-tailoring claim is kept until account deletion, because it allows the regenerations of that application |
| `tailormyresume_unlocks` | user id, application id, credit kind, time | Account deletion |
| `tailormyresume_purchases` | user id, token, order id, product, purchase type, credits, times, consumed time | Account deletion |
| `tailormyresume_tailorings` | user id, request id, boot id, status, result (tailored bullets), times | 24 hours after the job ends, or account deletion |
| `tailormyresume_content_reports` | user id, application id, kind, item id, generation id, item text, time | 180 days, or account deletion |

`DELETE /v1/me` deletes all of them in the rows step, with tests for partial failure and retry, as
the backend definition of done requires. That includes purchases: Google Play is the merchant of
record and keeps the order and tax records, so TailorMyResume keeps no copy.

Two tables hold text derived from the user: the tailored bullets for 24 hours, and the text of a
reported item for 180 days. The resume text, the JD text, and the request bodies are never written to
a table or a log. Model calls use `store: false` (already set in `src/core/ai/provider.ts`). Zero
Data Retention with OpenAI is a launch item (PRD 10.4).

## 7. Model use

| Route | Calls | Model tier |
|---|---|---|
| Resume parse | 1 | `extraction` (small) |
| Analysis | 1 | `extraction` |
| Prep questions | 1 | `extraction` |
| Tailoring | up to 4 | `generation` (mid), then `verifier` |
| Cover letter | up to 4 | `generation`, then `verifier` |

The `verifier` model must differ from the `generation` model. The backend picks the models and keeps
them in `src/apps.ts`. The `ai_usage.task` of each call is `tailormyresume.<route>.v<prompt version>`.

Prompt injection. All resume, JD, and fact text is untrusted.

1. The prompt puts it between fixed delimiters and says that text inside them is data, not
   instructions. The server removes the delimiter strings from the input first.
2. The output schemas and the id checks of section 4 limit what an injected instruction can do.
3. The verifier gets only the cited source facts and the proposed text, never the JD.
4. The prompt-injection set of the evaluation (PRD 10.3) is a release gate.

A prompt, schema, or model change needs a run of the TailorMyResume evaluation set before merge.

## 8. Work on the app side

These are not backend issues. They go to this repository.

1. Amend Constitution I.5: `INTERNET` permission, one HTTP client and kotlinx.serialization in the
   catalog, Firebase Auth. Name the Consent screen. Add Dependency Guard first (ledger: "before the backend work starts").
2. Add `demo` and `prod` flavours. `demo` keeps the offline implementations and the developer menu.
3. Make the AI interfaces `suspend` with a typed failure. They are synchronous today.
4. Real `SignInGateway`: Credential Manager, Google, Firebase Auth, then `GET /v1/me`.
5. Consent screen: replace "analyse on device" with `ai-processing`, add the 18+ line, new notice version.
6. Remote implementations of `ResumeTextParser`, `AnalyzeJobUseCase`, `ResumeTailor`, `PrepQuestionSource`, `CoverLetterSource`.
   Model changes they need: `PrepQuestion` gets `why` and `gapAdvice`, and maps `requirementId` to
   `requirementText` and the first `backingFactIds` value to `backingFactId`. `CoverLetterDraft` maps
   the paragraph roles; the device adds the name to the closing. Store each `generationId`.
7. `PaymentGateway` on Play Billing and the purchase routes. `UsageAllowance` reads the wallet.
   Replace `consumeCredit()` at export with unlock once per application (D5). Remove
   `ApplicationPack.SINGLE_APPLICATION` (cut by the MVP lock, PRD 6.1.1).
8. `ContentReportRepository` also posts the report. `AccountDataExporter` adds `server.json`.
   `DeleteAccountUseCase` calls `DELETE /v1/me`.
9. Update PRD 10.1. It names Supabase; the backend is Fly.io, Neon, and Firebase Auth.

## 9. Open items

| Item | Proposal |
|---|---|
| Delete and sign in again gives a new free credit | Accept for the MVP. Watch the count of deletions per Google account later |
| Many Google accounts can use the free tier and fill the app cost cap for everyone | Before public launch (not for the closed test): Play Integrity token on wallet creation and AI routes (PRD 10.1), and a per-user daily cost limit below the app cap |
| The $5 daily app cost cap stops every user at about the same time | Size it from the eval run cost before the closed test |
| Security logs kept for 1 year (PRD 10.4) | Backend compliance item: an audit log of sign-ins, consents, purchases, and deletions with no personal content |
| Refunds and voided purchases | Poll the Play Voided Purchases API daily and take the credits back. After the first release |
| Profile and application sync | After the MVP, as a separate contract |
