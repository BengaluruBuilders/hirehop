# Jade restyle · frame index

Export of the Claude Design canvas "HireHop" on 2026-10-06:
https://claude.ai/artifact/766P2hFnsULQbnXVeKj3rM. Light mode only for screens; the foundations
boards have light and dark. 360 x 800 dp. Read the files as text: inline `style` holds the exact
values. 1 CSS px = 1 dp, font px = sp. `/_blob/...` images are the placeholder characters; use the
existing character vectors in `core:designsystem`.

Do not edit these files by hand. If the design changes, export the canvas again and replace this folder.

## Foundations

| File | Content |
|---|---|
| `Main.dc.html` | Tokens, light: colour with contrast, type scale, spacing, radii, squiggles, characters |
| `Tokens-Dark.dc.html` | Tokens, dark |
| `Components-Light.dc.html` | Buttons, chips, status rows, inputs, colour cards, pill rows, headers, sheet, dock |
| `Components-Dark.dc.html` | The same, dark |

## Flow 1 · First run (module `feature:onboarding`, S7 in `feature:analysis`)

| Screen | Files |
|---|---|
| S1 Welcome | `S1-Welcome` |
| S2 Paste JD | `S2a-Paste-Empty`, `S2b-Paste-Pasted`, `S2c-Paste-TooShort` |
| S3 Sign in | `S3a-SignIn`, `S3b-SignIn-Under18` |
| S4 Consent | `S4-Consent` |
| S5 Import resume | `S5a-Import-Choose`, `S5b-Import-Reading`, `S5c-Import-Scanned` |
| S6 Confirm facts | `S6a-Facts-Pending`, `S6b-Facts-Partly`, `S6c-Facts-Confirmed` |
| S7 Gap check | `S7a-Gap-Waiting`, `S7b-Gap-Result`, `S7c-Gap-IHaveThis`, `S7d-Gap-ShareCard` |

## Flow 2 · Tailoring (module `feature:tailor`)

| Screen | Files |
|---|---|
| S8 Resume review | `S8a-Resume-Loading`, `S8b-Resume-Ready`, `S8c-Resume-Partly`, `S8d-Resume-AllReviewed`, `S8e-Resume-RegenConfirm`, `S8f-Resume-RegenUsed`, `S8g-Resume-Failed` |
| S9 Bullet review | `S9a-Bullet-Resting`, `S9b-Bullet-Accepted`, `S9c-Bullet-FlaggedVerb`, `S9d-Bullet-EditByHand`, `S9e-Bullet-UserEdited` |
| S10 Cover letter | `S10a-Cover-Offer`, `S10b-Cover-Generating`, `S10c-Cover-Ready`, `S10d-Cover-FactSheet`, `S10e-Cover-Editing` |
| S11 Prep questions | `S11a-Prep-Generating`, `S11b-Prep-Ready`, `S11c-Prep-Gaps` |

## Not in this export

Flows 3, 4 and 5 (S12 to S24), dark screen frames, and the offline, error and limit states of
Flows 1 and 2. For those, use the states in `design/claude-design/flow<N>/` and apply this
folder's tokens and components. `docs/MVP_PLAN.md` section 4 says how to design them.
