# Claude Design export

This folder holds the HireHop design "Friendly hero, Jade". It is an export of a Claude Design canvas.
Canvas: https://claude.ai/artifact/MuQyV6hk8yvQvwpDj1iHaZ

## Content

| Path | Content |
|---|---|
| `foundations/` | 7 boards: `Main`, `Colour`, `Surface`, `Illustration`, `Motion`, `ComponentsLight`, `ComponentsDark` |
| `flow1/` to `flow5/` | One file per frame (376 frames in total). Each folder has an `INDEX.md` |
| `canvas.json` | The canvas structure |

## File names

A frame file is named `f<flow>-s<screen>-<name>-<state number>-<light|dark>.dc.html`.
Example: `f1-s2-paste-jd-03-dark.dc.html` is Flow 1, screen S2, state 3, dark theme.
The `INDEX.md` of a flow lists every state and its two files.

## Read a frame

1. Read the file as text. Inline `style` attributes hold the exact sizes, colours, and radii.
2. Each frame is 360 x 800 dp. One CSS `px` equals one `dp`. A font size in `px` equals `sp`.

## View a frame

1. Run `python3 -m http.server 8766` in `design/claude-design`.
2. Open `http://127.0.0.1:8766/flow1/<file>` in a browser.

A frame is plain HTML. It needs no runtime file.

## Change the design

Do not edit these files by hand. To change the design, change the canvas.
Export the frames again. Replace the whole folder in one pull request.
For the steps, read `design/02-screens.md`.
