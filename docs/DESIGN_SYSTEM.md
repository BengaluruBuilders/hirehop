# HireHop design system catalogue

Module: `core:designsystem`. Package root: `com.hirehop.core.designsystem`.
Board: `design/claude-design/foundations/`. Board names: Main (type), Colour, Surface, Motion,
ComponentsLight, ComponentsDark.

Read tokens with `HhTheme.colors`, `HhTheme.typography`, `HhTheme.spacing`, `HhTheme.shapes`,
`HhTheme.elevation`, `HhTheme.motion`, `HhTheme.isDark`. Dark mode follows the system.
Pass all user text as parameters. Components have no string resources.
Use only the names in this file. The old alias names are gone.

## Changes for feature adoption

A feature must change these things when it adopts the integration changes.

1. Bottom bar zone. `HhScreen` now ends the content area at the top of the bar zone. Remove any
   spacer, extra bottom padding, or `Modifier.padding(bottom = ...)` that you added to keep content
   clear of the bar. Use the `PaddingValues` that `HhScreen` gives you and nothing else.
2. Notice above the bar. Move a notice card or reason text that sits above the bar out of the
   scrolling content. Pass it as `bottomBarNotice = { ... }`. Keep `creditDisclosure` inside
   `HhBottomActionBar` for the credit line only.
3. Stacked bar. Do nothing. `HhBottomActionBar` stacks its actions when the font scale is 1.5 or
   more, in the reverse order of the row. Write the actions as in the row (outline action first, primary
   action last). The primary action is then first in the stack. Give each action `Modifier.weight(1f)` as
   before. Remove any local stacked layout.
4. Toasts. Replace local snackbar copies with `rememberHhToastState()`, `HhToastHost(state)` in
   `HhScreen(snackbarHost = ...)`, and `state.show(message, actionLabel)`. Remove every
   `androidx.compose.material3.Snackbar*` import from the feature.
5. Icons. Replace substitute icons with `HhIcons.Lock`, `Link`, `Info`, `Download`, `ExpandMore`,
   `ExpandLess`, `Description`, `Flag`, `Share`, `OpenInNew`, `Search`.
6. Coverage block. For a block with no partial count, pass `partial = null, partialLegend = null`.
   `KeywordCoverageMeter` already does this. `HhCoverageBlock(summary = ...)` adds one line between the
   fraction and the bar (Gap analysis: "You cover 9 of 14 key terms"). `HhFitShareCard` takes the same two nullable
   values.
7. Home header. Settings uses `HhCompactHomeHeader`. A home screen with a list uses
   `HhCollapsingHomeHeader`. Do not swap two headers on a scroll position. The swap moves the list by
   the height difference in one frame, and the list flickers.
8. Dock constants. Use `HhDockDefaults.height`, `ballOverhang`, and `inset`. Do not repeat the arithmetic.
9. Motion. `HhTheme.motion` has `proofSpecs`, `hopSpecs`, and `reduced`. The old Int durations and easings are gone.
10. Fact ids. Show `FactDisplayIds` (module `core:domain`) values, not raw bullet ids. See
    `docs/MOCK_BACKEND.md`.

## Tokens

### Colour (`HhTheme.colors`)
New roles: `header`, `headerShape`, `onHeader`, `onHeaderVariant`, `onHeaderControl`, `primary`,
`onPrimary`, `primaryContainer`, `onPrimaryContainer`, `background`, `surface`, `card`, `ground`,
`document`, `tool`, `onTool`, `onToolVariant`, `outline`, `outlineVariant`, `outlineSoft`,
`onSurface`, `onSurfaceVariant`, `body`, `inverseSurface`, `inverseOnSurface`, `inversePrimary`,
`met`, `metContainer`, `onMetContainer`, `partial`, `partialContainer`, `onPartialContainer`,
`gap`, `gapContainer`, `onGapContainer`, `neutralContainer`, `onNeutralContainer` (a quiet notice, same fill as
`gapContainer`), `evidence`,
`evidenceLine`, `error`, `onError`, `errorContainer`, `onErrorContainer`, `special`, `onSpecial`,
`scrim`.
Rules: `gap` is never red. `special` appears only on the pack purchase button.

### Type (`HhTheme.typography`)
`displayL` 40/48 800, `displayM` 32/40 800, `headlineL` 26/32 800, `headlineM` 22/28 800,
`titleL` 20/26 700, `titleM` 16/22 700, `titleS` 14/20 700, `bodyL` 16/24 400, `bodyM` 14/21 400,
`labelL` 14/20 600, `labelM` 12/16 600, `bodyS` 12/16 400 (small meta lines, as the frames show them),
`numeralHero` 44/48 800, `numeralM` 18/24 700, `factId` 12/16 mono. Figures are tabular. Fonts are set in
`HhFontFamilies.sans` and `HhFontFamilies.mono` (`theme/Type.kt`). Today they point to Anek Latin and
JetBrains Mono. Plus Jakarta Sans and IBM Plex Mono replace them when the font files are added.

### Shape (`HhTheme.shapes`)
`sheet` (28 top), `heroCard` 24, `card` 20, `field` 12, `pill`, `tag` 8, `banner` 16.

### Spacing (`HhTheme.spacing`)
`gutter` 16, `cardPadding` 14, `sectionGap` 24, `touch` 48, plus `xxs` 2, `xs` 4, `sm` 8, `md` 12,
`lg` 16, `xl` 20, `xxl` 24, `xxxl` 32, `d2` to `d64`.

### Elevation (`HhTheme.elevation`)
`level0` to `level4`, `hero` (level 2), `dock` (level 3), `modal` (level 4).
Apply with `Modifier.hhShadow(shadow, shape)`.

### Motion (`HhTheme.motion`)
`proofSpecs`: `spatial`, `spatialFast`, `travel`, `offset`, `size`, `fade`, `color`, `staggerMs`, `staggerMax`.
`hopSpecs`: `spatial`, `scale`. Use `hopSpecs` only for gap closed, exported, pack purchased,
first fact confirmed. `reduced` is true when animations are off; the spatial specs then use `snap()`.
There are no duration fields and no easing fields. `size` and `color` repeat the board values of
`spatial` and `fade` for the `IntSize` and `Color` value types.
`travel` is a 240 ms eased tween for the dock notch. Only the dock uses it.
A spring ends with a long tail, so the notch did not hand over to the ball rise at a clear moment.

The primitives are in `component/HhMotion.kt`. A feature calls a primitive. A feature never calls
`tween(`, `spring(`, or a numeric duration.

| Need | Primitive | Token |
|---|---|---|
| Show or hide a part | `HhVisibility(visible)` | `fade` |
| Show or hide a part with a 12 dp rise | `HhVisibility(visible, rise = true)` | `fade`, `offset` |
| Switch between loading, content, empty, and error | `HhContentSwitch(targetState, contentKey = { it::class })` | `fade` |
| Press feedback, scale 0.97 | `Modifier.hhPressScale(interactionSource)`. `Hh*` buttons and cards with `onClick` have it. | `spatialFast` |
| Expand and collapse a block | `HhExpandable(expanded)` | `size`, `fade` |
| List enter: 12 dp rise, 30 ms stagger, first 6 items | `rememberHhListEnterState()`, then `Modifier.hhListEnter(state, index)` on each item | `spatial`, `staggerMs`, `staggerMax` |
| Navigation forward: the new screen rises 48 dp over the old screen | `rememberHhNavTransitions().forward(scope, hierarchical = true)` | `fade`, `offset` |
| Navigation back and predictive back: the screen sinks 48 dp | `rememberHhNavTransitions().back(hierarchical = true)` | `fade`, `offset` |
| Dock tab switch: the new screen fades in and shifts 24 dp from the side of the tapped tab | `tab(scope, direction, pop)`. `direction` is the sign of the tab index change | `fade`, `offset` |
| A tab screen reached from a pushed screen: fade only | `forward(scope, hierarchical = false)` and `back(hierarchical = false)` | `fade` |
| Selected colour of a chip or a checkbox | Built into `HhFilterChip`, `HhCheckbox` | `color` |
| Dock selection: the ball sinks into the bar, the notch moves to the selected item, the ball rises with the icon | Built into `HhDock` and `HhDockItem` | `fade`, `travel`, `spatial`, `spatialFast` |
| Progress moment | `Animatable` with a `hopSpecs` value | `hopSpecs.scale`, `hopSpecs.spatial` |

Rules of the primitives:
- If `reduced` is true, nothing moves. A fade stays, and the state change stays visible.
- The list enter plays one time, on first composition. It does not play again when the person comes
  back to the screen.
- The 48 dp rise is the sheet rise of the board. It moves the whole pushed screen, because a pushed
  screen has no sheet surface and its content can load after the first frame.
- Give `HhContentSwitch` a `contentKey` for a state that carries data. Without it, each data change fades.
- Every spec is a spring, a 150 ms fade, or the 240 ms `travel`, so a new target interrupts the old one. Input never waits.
- A settled frame is the same as the frame without motion. Screenshot baselines do not change.

Do not animate:
- The size, the padding, or the position in the layout of an item in a lazy list. Use `graphicsLayer`
  alpha, translation, and scale only. Do not put `HhExpandable` in a lazy list item.
- Blur, `RenderEffect`, or a shadow.
- A second stagger chain while one chain plays.
- The multi-frame sequences of the board: gap-analysis reveal, bullet diff wipe, source-link connector.
- Navigation, an error, or text that the person must read, with `hopSpecs`.
- `HhBottomSheet` and `HhSwitch`. Material moves them with its standard motion scheme.

## Screen chrome

```kotlin
fun HhScreen(modifier, header: (@Composable () -> Unit)? = null, sheet: Boolean = true,
    bottomBar: (@Composable () -> Unit)? = null, snackbarHost: @Composable () -> Unit = {},
    floatingAction: (@Composable () -> Unit)? = null,
    bottomBarNotice: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit)
```
Compose header, overlapping content, and a bar. Use `sheet = true` under a home header. Use
`sheet = false` with hero cards. Board: Home header, Inner header, Sheet, Bottom action bar.

The bar zone. If there is a `bottomBar`, the zone is the notice plus the bar plus the navigation
bar inset (the bar measures its own inset). The content area ends at the top of the zone. Content
is clipped there and never scrolls behind the notice or the bar. The sheet and the screen
background still fill the area behind the zone.

`PaddingValues` that `content` receives:

| Case | top | bottom |
|---|---|---|
| No header | status bar inset | see below |
| Header and `sheet = true` | 24 dp | see below |
| Header and `sheet = false` | 0 dp | see below |
| `bottomBar` or `bottomBarNotice` set | as above | 16 dp (`spacing.gutter`), the gap above the last item |
| Neither set, no dock | as above | navigation bar inset |
| Neither set, dock inset provided (`LocalHhBottomInset`) | as above | navigation bar inset plus `HhDockDefaults.inset` (96 dp) |

The old bottom padding was the bar height plus 16 dp. It is now 16 dp, because the content area
no longer reaches under the bar. A feature that passed the `PaddingValues` to its list needs no
change. A feature that added its own bar clearance must remove it, or the clearance is doubled.

`bottomBarNotice` is placed directly above the bar with a 16 dp side gutter and an 8 dp gap to the
bar. Put a notice card or a reason line in it. Without a bar, it sits above the dock inset. The
`snackbarHost` and `floatingAction` lift above the notice.

The dock path keeps content running under the dock ball and behind the notch. It is not clipped. The bottom padding
above lets the last item scroll clear of the dock.

```kotlin
fun HhHomeHeader(greeting: String, headline: String, modifier, trailing: @Composable RowScope.() -> Unit = {},
    action: (@Composable () -> Unit)? = null, illustration: (@Composable BoxScope.() -> Unit)? = null)
```
Jade home header with the hero illustration slot (174 x 200 dp, drawn above the sheet). Board: Home header.

```kotlin
fun HhCompactHomeHeader(title: String, subtitle: String, modifier, trailing: @Composable RowScope.() -> Unit = {})
```
Compact: a tab screen with no illustration. Title first (`headlineL`), then one subtitle line, 200 dp
plus the status bar, sheet overlap 28 dp. Use it for Settings. It works as `HhScreen(header = ...)`
with `sheet = true`.

```kotlin
fun HhCollapsingHomeHeader(collapse: HhHeaderCollapseState, title: String, greeting: String, headline: String,
    modifier, trailing, action, illustration)
fun rememberHhHeaderCollapseState(): HhHeaderCollapseState
```
The home header for a screen with a list. The scroll sets the height. Fully expanded, it is the same
frame as `HhHomeHeader`. Fully collapsed, it is a short jade bar (96 dp including the 28 dp overlap)
with the title and the trailing slot. Put `Modifier.nestedScroll(collapse.connection)` on the list.
A scroll up collapses the header before the list moves. A scroll down expands the header after the
list is at the top. Parallax: the headline, the action, and the hero move up at half the speed of
the sheet, the sheet covers them, and they fade out in the first half of the collapse. The greeting
fades out in the first half. The title fades in during the second half, so the two texts never show
together. The trailing slot stays visible. If `HhTheme.motion.reduced` is true, there is no parallax:
the headline, the action, and the hero stay in place while the sheet covers them.
The scroll drives the collapse, so there is no motion token.

```kotlin
fun HhInnerHeader(title: String, modifier, subtitle: String? = null, onBack: (() -> Unit)? = null,
    backContentDescription: String = "", backIcon: ImageVector = HhIcons.ArrowBack,
    trailing: (@Composable () -> Unit)? = null, belowTitle: (@Composable () -> Unit)? = null,
    extended: Boolean = true, overlap: Dp = ...)
```
Header of every pushed screen. `extended = true` is 200 dp for hero cards (overlap 96).
Board: Inner header + hero card.

```kotlin
fun HhSheet(modifier, contentPadding: PaddingValues = ..., content: @Composable ColumnScope.() -> Unit)
```
Surface with 28 dp top corners. Board: Sheet.

```kotlin
fun HhDock(modifier, content: @Composable RowScope.() -> Unit)
fun HhDockItem(selected: Boolean, onClick: () -> Unit, contentDescription: String, modifier,
    icon: @Composable () -> Unit)
fun HhDockIcon(icon: ImageVector)
```
Full-width `tool` bar on the bottom edge, for the three top-level tabs only. Icons only, no labels.
The top edge has one notch. A `primary` ball sits in the notch and holds the selected icon.
`HhDock` draws the bar, the notch, and the ball, and it owns the motion of a tab change:
the ball sinks behind the bar (`fade`), the notch moves (`travel`), the ball rises with the icon (`spatial`).
The notch starts when the ball is half sunk. The rise starts when the notch is one notch half-width from the item.
A new tap cancels the sequence and starts it again from the current values.
`HhDock` applies the navigation bar inset. Do not add it in `:app`.
The frames in `design/claude-design` still show the floating pill. Export them again to match.
Put each `HhDockItem` inside `HhDock`. An item outside `HhDock` throws an error.

```kotlin
fun HhBottomActionBar(modifier, contentPadding: PaddingValues? = null,
    creditDisclosure: (@Composable () -> Unit)? = null, actions: @Composable RowScope.() -> Unit)
```
Floating tool bar for 1 to 3 actions. Give each action `Modifier.weight(1f)`. Outline buttons
inside it switch to the on-tool style. Board: Bottom action bar.

```kotlin
fun HhBottomActionBar(modifier, contentPadding: PaddingValues? = null,
    creditDisclosure: (@Composable () -> Unit)? = null,
    stacked: Boolean = LocalDensity.current.fontScale >= 1.5f,
    actions: @Composable RowScope.() -> Unit)
```
`stacked` puts one action per line, each at full width, in the reverse of the order given. The primary
action comes last in the row, so it comes first in the stack. The default turns it on at
a font scale of 1.5 or more. Pass `stacked = true` or `false` to force a mode. Pass `primaryLast = false` when
the first action is the primary one (Welcome); the stack then keeps the order given.

```kotlin
object HhDockDefaults { val height: Dp /* 64 */; val ballOverhang: Dp /* 24 */; val inset: Dp /* 96 */ }
```
`inset` is `LocalHhBottomInset` for a tab screen. `:app` uses these values.

## Actions
All buttons are 48 dp pills.

```kotlin
fun HhPrimaryButton(label: String, onClick: () -> Unit, modifier, enabled = true, leadingIcon: ImageVector? = null, trailingIcon: ImageVector? = null)
fun HhSecondaryButton(...same...)   // tint
fun HhOutlineButton(...same...)
fun HhTextButton(...same...)
fun HhDestructiveButton(label, onClick, modifier, enabled = true, leadingIcon = null)
fun HhHeaderButton(label: String, onClick: () -> Unit, modifier, trailingIcon: ImageVector? = null) // outline on jade
fun HhButton(onClick, modifier, enabled, contentPadding, content: RowScope.() -> Unit)      // primary, slot form
fun HhButton(onClick, modifier, enabled, text: @Composable () -> Unit, leadingIcon: ...)
fun HhOutlinedButton(...same two forms...)
```
Board: Buttons.

```kotlin
fun HhSpecialButton(label: String, onClick: () -> Unit, modifier, caption: String? = null)
fun HhSpecialDeclineButton(label: String, onClick: () -> Unit, modifier)
fun HhSpecialOffer(label, declineLabel, onAccept, onDecline, modifier, caption: String? = null)
```
Marigold pack purchase. `HhSpecialOffer` pairs it with an equal-size "Not now" button. Board: Pack purchase.

```kotlin
fun HhIconButton(icon: ImageVector, contentDescription: String, onClick, modifier, enabled = true, tint, containerColor, borderColor, shape)
fun HhHeaderIconButton(icon, contentDescription, onClick, modifier)   // white 48 dp circle
```

## Surfaces
```kotlin
fun HhCard(modifier, contentPadding: PaddingValues? = null, trailingAction: (@Composable () -> Unit)? = null, onClick: (() -> Unit)? = null, content: ColumnScope.() -> Unit)
fun HhHeroCard(modifier, contentPadding, onClick: (() -> Unit)? = null, content: ColumnScope.() -> Unit)
fun HhSectionCard(modifier, contentPadding, trailingAction, content)
fun HhMonogram(text: String, modifier, size: Dp = 40.dp)
```
Card: card fill, 1 dp border, 20 dp. Hero card: document fill, 24 dp, hero shadow. Board: Application card, Bullet review card.

## Status, chips, tags
```kotlin
enum HhStatusKind { Met, Partial, Gap }
fun HhStatusDisc(kind, modifier, size: Dp = 18.dp, contentDescription: String? = null)
fun HhStatusChip(kind, modifier, label: String? = null)          // disc and word
fun HhApplicationStatusChip(kind: HhApplicationStatusKind, modifier, label: String? = null, dotSize: Dp)
fun HhProvenanceChip(kind: HhProvenanceKind, modifier, label: String? = null)
fun HhTrustChip(kind: HhTrustKind, modifier, label: String? = null)
fun HhCreditsPill(count: String, label: String, modifier, contentDescription: String? = null)
fun HhEditTypeTag(label: String, modifier)
fun HhRequirementTag(label: String, mustHave: Boolean, modifier)
fun HhTermChip(label: String, modifier)
fun HhFactId(id: String, modifier)
```
Always pass `label`. The default label is the enum name and is not user copy.
Board: Chips, pills and the report action; Requirement rows.

## Content
```kotlin
fun HhCoverageBlock(met: Int, partial: Int?, gap: Int, caption: String, metLegend: String, partialLegend: String?, gapLegend: String, modifier, summary: String? = null)
fun HhCoverageBar(met: Int, partial: Int, gap: Int, modifier)
fun HhEvidenceMark(text: String, modifier, style: TextStyle? = null, tint: Color? = null)
fun evidenceMarkSpanStyle(): SpanStyle
fun HhEvidenceText(text: AnnotatedString, modifier, style, color)
fun HhHeroNumeral(value: String, modifier, caption: String? = null, contentDescription: String? = null)
fun HhStepProgress(stepNames: List<String>, currentStepIndex: Int, modifier, ordinalLabel: String? = null, stepDetails: List<String?> = emptyList(), footnote: String? = null)
fun HhSegmentedCounter(current: Int, total: Int, modifier)
fun HhPageDots(count: Int, selectedIndex: Int, modifier, onHeader: Boolean = true)
fun HhListRow(modifier, onClick, showDivider, leading, trailing, content)
fun HhFilterChip(label, selected, onClick, modifier, leadingIcon, count, colors)
fun HhDivider(modifier, style, thickness, color)
```
Coverage block shows `met / total` as plain text plus one segment per key term. There is no gauge.
Pass `partial = null` to hide the partial legend and to count no partial segment.
For the evidence underline inside running text use `HhEvidenceText` with spans from
`evidenceMarkSpanStyle()`. A plain `Text` shows only the fill. Board: Coverage block, Bullet review card, Step progress.

## Inputs and overlays
```kotlin
fun HhTextField(value, onValueChange, modifier, label, placeholder, supportingText: (@Composable () -> Unit)?, errorText: String?, trailingSlot, enabled, singleLine, minLines, visualTransformation, keyboardOptions, keyboardActions)
fun HhConsentRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier, supportingText: String? = null, linkLabel: String? = null, onLinkClick: (() -> Unit)? = null)
fun HhCheckbox(checked, onCheckedChange, modifier, enabled = true)
fun HhSwitch(checked, onCheckedChange, modifier, enabled = true)
fun HhBottomSheet(onDismissRequest, modifier, sheetState, contentPadding, title: String? = null, subtitle: String? = null, content)
fun HhSheetActionRow(icon: ImageVector, title: String, onClick, modifier, subtitle: String? = null)
fun HhConfirmDialog(title, confirmLabel, cancelLabel, onConfirm, onCancel, modifier, message, destructive)
fun HhConfirmSheet(title, confirmLabel, cancelLabel, onConfirm, onCancel, modifier, message, destructive, content)
fun rememberHhToastState(): HhToastState
suspend fun HhToastState.show(message: String, actionLabel: String? = null, duration: HhSnackbarDuration = Standard): HhToastResult
fun HhToastState.dismiss()
fun HhToastHost(state: HhToastState, modifier)
enum class HhToastResult { Dismissed, ActionPerformed }
fun HhOfflineBanner(message, modifier, supportingText, visible = true, actionLabel: String? = null, onAction: (() -> Unit)? = null)
fun HhErrorCallout(title, modifier, supportingText, actionLabel, onAction)
fun HhLoadingWheel(contentDesc: String, modifier)
```
Board: Text field, Consent row, Bottom sheet, Dialog, Offline banner and snackbar.

Toast. `HhToastHost(state)` shows one toast at a time as the inverse-surface snackbar of the board.
Pass it as `HhScreen(snackbarHost = { HhToastHost(state) })`. Call `state.show(...)` from a coroutine.
`show` returns `ActionPerformed` when the person taps the action label (for example "Undo").
The public API has no `androidx.compose.material3.Snackbar*` type, so a feature can use it under
Constitution II.5. `HhSnackbar` and `showHhSnackbar` are the parts of the toast. A feature must not call them.

## Frames
```kotlin
fun HhExportPreviewFrame(meta: String, caption: String, modifier, badge: (@Composable () -> Unit)? = null, paper: ColumnScope.() -> Unit)
object HhPaperColors { Page, Ink, Body, Rule }
fun HhFitShareCard(eyebrow, headline, met, partial: Int?, gap, caption, metLegend, partialLegend: String?, gapLegend, matchedTerms: List<String>, footer, modifier)
```
The paper is black on white in both themes. The share card is always light. Board: Export preview frame, JD fit share card.

## Icons
`HhIcons` also has `Lock`, `Link`, `Info`, `Download`, `ExpandMore`, `ExpandLess`, `Description`, `Flag`, `Share`, `OpenInNew`, `Search`.

## Illustration
`HhSpotIllustration` is owned by the ILLUS package. Pass it into `HhHomeHeader.illustration`.
