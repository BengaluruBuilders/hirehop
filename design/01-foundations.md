# Design foundations

This file summarises the current design. The source is the Claude Design canvas.
The exported boards are in `design/claude-design/foundations/`. The boards win if this file differs.

## Direction

The direction is "Friendly hero, Jade". The app feels like a calm, kind guide for a person who applies for a job.
A jade header and a white sheet carry the screens. A hand-drawn character greets the user at key moments.
Documents (resume and job description) sit on white sheets. Tools (dock, action bar) sit on top in near-black.
Flat fills, solid shadows, and Compose springs make the motion. The design never uses blur, glass, or 3D.

## Tokens

All values are named tokens. `docs/DESIGN_SYSTEM.md` maps them to `HhTheme`.

### Fonts

| Font | Use |
|---|---|
| Plus Jakarta Sans 400, 600, 700, 800 | All UI text. Tabular figures wherever numbers appear |
| IBM Plex Mono 500 | Fact IDs only |

Body text is 14 sp or larger. Label text is 12 sp or larger.

### Colour roles

| Role | Light | Dark |
|---|---|---|
| `header` | `#0B7A5C` | `#0D3F31` |
| `headerShape` | `#15836A` | `#135240` |
| `primary` | `#0B7A5C` | `#5ED3AA` |
| `primaryContainer` | `#DDF2EA` | `#12463A` |
| `background` | `#FFFFFF` | `#0E1013` |
| `surface` | `#FFFFFF` | `#15181C` |
| `card` | `#F6F7F9` | `#1E2228` |
| `ground` | `#F1F3F5` | `#0E1013` |
| `document` | `#FFFFFF` | `#1C2127` |
| `tool` | `#16181D` | `#2B313A` |
| `outline` | `#7C8491` | `#7D8592` |
| `outlineVariant` | `#E6E8EC` | `#2C313A` |
| `outlineSoft` | `#D5D9E0` | `#3C4350` |
| `onSurface` | `#16181D` | `#ECEEF1` |
| `onSurfaceVariant` | `#5F6672` | `#A3AAB5` |
| `body` | `#3E434C` | `#C8CCD3` |
| `inverseSurface` | `#16181D` | `#E6E8EC` |
| `met` | `#0B7A5C` | `#5ED3AA` |
| `partial` | `#2B5FA8` | `#8FB8F2` |
| `gap` | `#4A5263` | `#B4BAC5` |
| `evidence` | `#DDF2EA` | `#17473A` |
| `evidenceLine` | `#0B7A5C` | `#5ED3AA` |
| `error` | `#B3261E` | `#F2B8B5` |
| `errorContainer` | `#F9DEDC` | `#8C1D18` |
| `special` | `#FFC94D` | `#FFC94D` |

Text pairs pass 4.5:1. UI lines pass 3:1. The Colour board lists each measured ratio.

### Shape and spacing

| Token | Value |
|---|---|
| Radii | `xs` 8, `sm` 12, `md` 16, `card` 20, `hero` 24, `sheet` 28 (top corners), `full` pill |
| Spacing unit | 8 dp, with a 4 dp half-step |
| Screen gutter | 16 dp |
| Card padding | 14 dp |
| Section gap | 24 dp |
| Touch target | 48 dp |

### Motion

| Register | Use |
|---|---|
| `proof` | The default. Crisp, low overshoot. Navigation, sheets, lists, diffs, waiting |
| `hop` | Springy. Progress moments only: gap closed, exported, pack purchased, first fact confirmed |

If the system animation scale is 0, every spring becomes a `snap()`. Content and step text still change.

## Layout patterns

| Pattern | Rule |
|---|---|
| Home header | Jade header, two soft circles, greeting, credits pill, bold headline. The hero character stands in front of the sheet |
| Inner header | Shorter jade header, white 48 dp circular back button, centred title and subtitle. Hero cards overlap it |
| Sheet | Surface with 28 dp top corners that overlaps the header |
| Dock | Full-width `tool` bar on the bottom edge, on the three tab screens only. The active item is an icon in a primary ball in the notch of the bar |
| Bottom action bar | `tool` bar for 1 to 3 main actions. Content has an inset, so the bar never covers content |
| Hero card | 24 dp radius `document` card that overlaps the inner header |

## Illustration

Two hand-drawn characters share one family: same faces, same 2.5 to 3.5 dp ink line, flat fills.

- The woman is the hero. She appears in the empty, exported, and goodbye spots and on the home header.
- The man appears in the error, offline, and scanned spots.
- Each pose is one vector drawable slot in `core:designsystem`.
- The characters are placeholders. The owner plans to replace them with commissioned art.
- Characters never appear on working screens: gap analysis, bullet review, editors, and data lists.

## Audience rule

The app serves students, fresh graduates, and people with 1 to 2 years of experience.
UI copy never uses "student", "college", "campus", "fresher", or "graduate".
Sample facts in the design and in seed data may use these words.

## Hard rules

1. Show no ATS score, gauge, dial, percentage ring, or match badge. Coverage is a plain fraction with a segmented bar.
2. Status is shape plus word plus colour. The gap word is "To prepare". A gap never uses red.
3. Use `special` (marigold) only on the pack purchase button.
4. Use `error` only for real failures and destructive actions.
5. Use no stock Material chrome: no centred top app bar, no pill navigation bar, no FAB.
6. Write no job, interview, or ATS guarantee in copy.
7. Put no brand colour inside an exported resume.
8. Every rewritten line links to a confirmed fact.
