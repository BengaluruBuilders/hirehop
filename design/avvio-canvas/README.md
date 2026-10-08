# Avvio canvas — current design source

Claude Design project, requested 2026-10-07:
https://claude.ai/design/p/f5d63256-9ace-489c-963c-a0a6622aaf96

Five files, one per flow: `Flow 1 First Run.dc.html`, `Flow 2 Tailoring.dc.html`,
`Flow 3 Export and Payment.dc.html`, `Flow 4 Profile.dc.html`, `Flow 5 Applications and Settings.dc.html`.
Each state is drawn twice: a dark row, and a light row with the same layout. Frames are 360 x 800 dp.
1 CSS px = 1 dp, font px = sp. This canvas wins over `design/jade-restyle/` and `design/claude-design/`
for look and layout. The old frames still list states and copy that the canvas leaves out.

`flow<N>.md` holds a text outline of every dark state, made with `outline.js` (see below). Each line is
one element: `"text" bg=<token> r=<radius> h=<height> pad=<padding> <size>/<weight> <colour token>`.
`AB` means Archivo Black, `UP` means uppercase, `icon` means the element holds a line icon.

## Tokens

| Token | Dark | Light | `TmrColors` |
|---|---|---|---|
| bg | #000000 | #FFFFFF | `background`, `ground` |
| card | #161817 | #F4F6F1 | `card`, `surface` |
| card2 | #232524 | #EBEEE7 | `primaryContainer`, `headerControl` |
| line | #3A3D3B | #D5D8D2 | `outlineVariant` |
| text | #FFFFFF | #000000 | `onSurface` |
| mute | #8C918E | #525251 | `onSurfaceVariant` |
| acc | #AEFF00 | #3C6208 | `primary`, `met` |
| LIME fill | #AEFF00, black text | #AEFF00, black text | `brand`, `onBrand` |
| err / errBg | #FF7A7A / #2A1416 | #B3261E / #FCEBEA | `error`, `errorContainer` |
| warn / warnBg | #FFC83D / #262009 | #7A5200 / #FFF3D3 | `partial`, `partialContainer` |
| okBg | #18230A | #EEF7DC | `metContainer` |
| sheet | #121413 | #FFFFFF | `sheet` |

Light mode only: progress bars, step bars, spinners and rings are #3C6208 on a #C9CDC5 track;
placeholder and disabled text is #646762 (4.5:1 on white and both light cards). Every disabled
button has a short line under it, with an info icon, that says why it is off.

Type: Manrope (UI) and Archivo Black (uppercase headlines, through `TmrHeadline`). Scale in `Type.kt`.

## Components

| Canvas part | Spec | `Tmr*` |
|---|---|---|
| Main button | min 56, radius 28, 16/800, LIME fill, black text | `TmrButton` Large Primary |
| Secondary button | card2 fill, text colour | `TmrButton` Secondary |
| Outline button | transparent, 1.5 line outline | `TmrButton` Outline |
| Small button | 48 high, radius 24, 14/800, icon 17 | `TmrButton` Compact |
| Icon button | 48 circle, card2, icon 22 text colour | `TmrIconButton`, `TmrBackButton` |
| Top bar | 64 high, padding 0 12, back button, title 18/800 | `TmrInnerHeader` |
| Screen headline | Archivo Black uppercase 30 (28 on messages, 34 on Welcome) | `TmrHeadline` |
| Body | padding 16 sides, gap 12; footer padding 12 16, gap 8 | `TmrScreen` |
| Card | card, radius 20, padding 16, gap 10 | `TmrCard` |
| Banner | tone fill, radius 18, padding 14 16, icon 22, 14.5/700 text colour | `TmrOfflineBanner`, `TmrErrorCallout` |
| Note | icon 18 mute, 14/600 mute | — |
| Fact ID chip | 24 high, radius 8, card2, acc text 12/800 tracking .05em | `TmrFactId` |
| Status chip | 26 high, radius 13, card2, icon 16 and 13/800 in status colour | `TmrStatusChip`, `TmrProvenanceChip` |
| Status colours | Met and Confirmed acc; Partly met warn; To prepare text; Pending mute; Failed err | `statusColor` |
| Checkbox | 24, radius 7; on LIME with black check; off 2 mute border; row min 48, 15.5/700 | `TmrCheckbox` |
| Progress | 8 high, radius 4, card2 track, LIME fill | — |
| Step row | card, radius 18, padding 14 16, min 56; done LIME disc with black check; now ring; waiting dim; trailing 13/700 mute word | `TmrStepProgress` |
| Text area | card, radius 22, padding 16; error 2 outline | `TmrTextField` |
| Field | card, radius 18, padding 12 16, min 64; label 13/700 mute; value 16/700 | `TmrTextField` |
| Section label | 13/800 uppercase tracking .06em mute | — |
| Sheet | sheet fill, radius 28 top, handle 36 x 4 line colour | `TmrBottomSheet` |
| Message state | 88 circle tone fill, icon 40; headline 28; text 16 | `TmrSpotIllustration` |
| Logo tile | 44, radius 14, colour fill, 18/800 black letter | `TmrMonogram` |

## outline.js

Run it in the built-in browser on the open Claude Design project tab with `javascript_exec`: paste the
file content and call it, for example `(<content>)('Flow 1 First Run.dc.html')`. It returns the outline
of every dark state. Read only. Never type into the Claude Design chat and never edit its files.
