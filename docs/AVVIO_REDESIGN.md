# TailorMyResume visual direction — Avvio reference

Requested on 2026-10-07. This direction replaces the Jade visual rules. Product scope,
navigation, candidate-fact integrity, consent, and the offline backend stay as specified in
the constitution and architecture.

## Sources

- [Avvio on Google Play](https://play.google.com/store/apps/details?id=xyz.avvio.app):
  the phone interiors are the color reference for the app. Repeated flat pixels in the resized
  store screenshots show black `#000000` dark grounds, charcoal `#141614` panels, lighter inset
  panels `#252624`, white `#FFFFFF` light grounds, pale `#F4F6F1`, `#EFF1EC`, and `#EBEEE7`
  panels, and lime `#AAFF00` actions. These are screenshot samples, not declared native-app tokens.
  Large uppercase captions and colored fields outside the phones are store artwork.
- [Avvio brand kit](https://avvio.xyz/brand/): Open Sans; website and identity colors Deep
  `#0F1901`, Ink `#121A02`, Olive `#2D3521`, Sage `#5C6350`, Bone `#F1EEE6`, Cream
  `#FAF9F6`, Lime `#B9FA4B`. Use this for type reference, not as a replacement for the
  observed phone UI colors.
- [Avvio Personal](https://avvio.xyz/personal/): a further reference for the contrast between
  restrained neutral surfaces and lime product accents.

The Claude Design canvas (`design/avvio-canvas/README.md`) sets the type: Manrope for UI text and
Archivo Black for uppercase headlines, both bundled for offline use with their OFL licenses.
The TailorMyResume name, product copy, and identity remain its own.

## Application rules

1. Use `TmrTheme` and shared `Tmr*` components throughout all five flows. The current token values
   and component contracts are in `DESIGN_SYSTEM.md`.
2. Light mode uses white and pale neutral surfaces with black text. Dark mode uses black and
   neutral charcoal with white text. App lime marks main actions, selection, and progress.
3. Pair lime fills with black text. Text links on light surfaces use an accessible darker green
   adaptation. Keep semantic error colors separate from decorative accents.
4. Use a restrained text hierarchy, rounded cards, subtle outlines, and compact screen chrome.
   Remove the former full-color hero treatment and decorative collage where it crowds content.
5. Keep status words and shapes as well as color. Keep 48 dp touch areas, font scaling,
   scroll access to content, and reduced-motion behavior.
6. The previous frame exports remain read-only records of screen content and reachable states.
   Their Jade colors, typeface, hero illustrations, and decorative layout do not override this
   direction. Do not rewrite exported HTML by hand.

## Verification

Record and inspect the changed screenshot baselines in light, dark, and 200% font states.
Verify all baselines, run the existing behavior tests and constitution checks, and build debug
and release through `tools/ci/verify-local.sh`. Keep Gradle runs serialized with three workers
or fewer. Inspect the app on the available Android emulator after the build.

Validated on 2026-10-07: `tools/ci/verify-local.sh` passed, including the constitution check,
formatting, unit tests, coverage gates, screenshot verification, release lint, and debug/release
builds. Render review covered light, dark, and 200% text examples. The emulator check covered
Applications in light, dark, and 200% text. The original emulator settings were restored.
