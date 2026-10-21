# Changelog

All notable changes to **Overtime Tracker** are documented here. Releases are published as signed binaries on the [GitHub Releases Page](https://github.com/KouseiA/Overtime-Tracker/releases).

---

## [v1.6.2] - 2026-10-21
### Fixed
- **4x2 Cutoff Widget Inflation & Instant Updates**:
  - Fixed an `InflateException` in launcher `RemoteViews` caused by disallowed generic `<View>` spacers in `widget_overtime_cutoff.xml`. Replaced with RemoteViews-compliant `<FrameLayout>` elements to ensure clean, error-free inflation on all launchers.
  - Assigned distinct `appWidgetId` request codes to widget `PendingIntent`s to avoid intent collision across multiple widget sizes.

---

## [v1.6.1] - 2026-10-21
### Fixed & Improved
- **100% Instant Home Screen Widget Sync via Native RemoteViews**:
  - Migrated the 4x2 Cutoff Terminal Widget to native Android `AppWidgetProvider` + `RemoteViews`, completely eliminating WorkManager delays, Jetpack Glance session closure caching, and OEM battery background throttling (Vivo / FuntouchOS / Samsung / Xiaomi).
  - All overtime additions, edits, and deletions now refresh home screen widgets in real time (< 10ms) via direct Binder IPC to `AppWidgetManager.updateAppWidget()`.
  - Preserved the full Concept 1 Dark Gold Terminal aesthetic (gold-bordered terminal card, dynamic tip-aligned progress bar bitmap, gold pill badge, and `+ LOG OT` quick-launch action).

---

## [v1.6.0] - 2026-10-21
### Fixed & Improved
- **Instant Home Screen Widget Updates**:
  - Fixed an issue where adding and quickly deleting or editing overtime shifts left widgets showing stale values.
  - Implemented Jetpack Glance state definition (`PreferencesGlanceStateDefinition`) with active timestamp mutation to bypass Glance's internal 45-second session lock and force immediate recomposition.
  - Upgraded repository SharedPreferences commits from asynchronous `.apply()` to synchronous `.commit()`, guaranteeing disk persistence before widget refresh triggers.
  - Added OS broadcast dispatch via `AppWidgetManager.ACTION_APPWIDGET_UPDATE` to ensure launcher RemoteViews update without delay.

---

## [v1.5.9] - 2026-10-21
### Restored & Improved
- **Full Computation of All Overtime Hours (Including Leftover .50)**:
  - Brought back complete computation for every `.50` half-hour in totals and pay calculations.
  - Leftover `.50` hours (e.g. 1.5h, 3.5h, 7.5h) are fully calculated and paid without deductions across Dashboard, DTR Sheet, Calendar, and Widgets.

---

## [v1.5.8] - 2026-10-21
### Fixed & Improved
- **Preserved Individual .50 Record Keeping**:
  - Shifts now retain their exact `.50` duration (e.g. 1.5 hrs, 2.5 hrs) when logged.
  - When two `.50` shifts pair up in a cutoff (e.g., 1.5h + 2.5h = 4.0h), both are fully computed as whole hours.
- **Excess .50 Drop on Total Calculation**:
  - In cutoff totals (Dashboard, DTR Table, Calendar, and Widgets), any odd leftover/sobrang `.50` hour is dropped from the Total Pay calculation so you only get paid for full hours (e.g. 7.5 hrs pays for 7.0 hrs).
  - Quick presets in the shift dialog restored to 30-minute intervals (`0.5h`, `1h`, `1.5h`, `2h`...).

---

## [v1.5.7] - 2026-10-21
### Changed & Simplified
- **Whole-Hour Overtime Computation**:
  - Overtime hours are now computed strictly in whole 1-hour increments (`1h`, `2h`, `3h`...).
  - Half-hour fractions (`.50` / 30-minute blocks) and minutes under 1 full hour are dropped from calculations, matching company cutoff overtime policies.
  - Shift logging dialog quick presets updated to 1-hour intervals (`1h`, `2h`, `3h`...).
- **Removed Night Shift Differential (NSD)**:
  - Completely streamlined calculation formulas: total pay is now directly `durationHours × effectiveHourlyRate`.
  - Removed night differential split calculation, night differential settings toggle and rate chips, `+NSD` badges, and dialog night formula card for a faster, simpler experience.
  - Cleaned DOLE rate table across the DTR ledger and documentation.

---

## [v1.5.6] - 2026-10-20
### Performance & Size Optimization
- **Zero-Allocation Overtime Computations**:
  - Converted `OvertimeRecord` day/night hour split calculation to a cached `by lazy(LazyThreadSafetyMode.NONE)` property.
  - Transformed `effectiveDayRate`, `effectiveNightRate`, `categoryMultiplier`, and `categoryLabel` to direct vals, making `dayPay`, `nightPay`, and `totalPay` instant O(1) lookups.
  - Eliminated repetitive `Calendar.getInstance()` allocations during list scrolling in the Shift Calendar and DTR Sheet.
- **Compose Stability & Smart Recomposition Skipping**:
  - Annotated core data models (`OvertimeRecord`, `AppSettings`, `CutoffPeriod`) and UI states (`CutoffInfo`, `ChartBarData`, `DashboardStats`, `MainUiState`) with `@Immutable`.
  - Enabled Compose compiler to skip recompositions on list items and stats cards when unrelated state changes.
- **Memoized Calculations**:
  - Wrapped `totalHours` and `totalPay` in `remember(displayedRecords)` within `CalendarTab` to prevent iterative sum recalculations on every recomposition.
- **R8 Dead Code Elimination & APK Size Reduction**:
  - Removed blanket ProGuard `-keep class androidx.compose.** { *; }` rule, enabling R8 to aggressively tree-shake unused `material-icons-extended` icons and Compose framework classes.

---

## [v1.5.5] - 2026-10-19
### Changed & Improved
- **Integrated Detekt & Compose Static Analysis**:
  - Embedded `detekt` static code analysis and `io.nlopez.compose.rules` directly into Gradle build lifecycle.
  - Tailored Compose ruleset to eliminate false positives on PascalCase composables and canvas math.
- **Architectural Compose Refactoring**:
  - Reordered Composable parameter signatures across 11 key components (`MainScreen`, `NeoTopAppBar`, `CutoffScheduleCard`, `TimeRangeOvertimeDialog`, `OtFormTab`, `SettingsTab`, etc.) following official Compose API guidelines (`modifier: Modifier = Modifier` as primary optional parameter).
  - Fixed `LambdaParameterInRestartableEffect` in `CashConfettiEffect` using `rememberUpdatedState` to prevent stale animation callbacks.
  - Added root `modifier` handling to `PerforatedTearDivider`, `MainNavigation`, and `OnboardingScreen`.

---

## [v1.5.4] - 2026-10-18
### Fixed
- **Dynamic Tip Alignment for Progress Bar (Option 1)**:
  - Replaced static-centered milestone text with `GaugeDrawer.drawHorizontalProgressBar`, dynamically anchoring the current hours label (`4.5 hrs`) directly to the end-tip of the gold progress bar.
  - The milestone number now levels and glides smoothly with actual progress (e.g. at 15%, the label sits exactly at 15% under the bar tip, eliminating the detached look).
  - Cleanly bounds the label between `0` and `30+` milestones to prevent horizontal edge clipping.

---

## [v1.5.3] - 2026-10-18
### Fixed
- **Pixel-Perfect Alignment with Concept 1 Mockup**:
  - **Golden Outline Border**: Added 1.8dp gold perimeter stroke with 24dp rounded corners (`widget_terminal_gold_border.xml`).
  - **Massive 44sp Hero Payout**: Boosted hero currency readout to 44sp bold gold for immediate glanceability matching the mockup.
  - **Thick Capsule Progress Bar**: Increased progress bar height to 11dp with 5.5dp rounded ends and distinct milestone markers (`0`, `current hrs`, `target+`).
  - **Dynamic Vertical Spacing**: Replaced top-heavy clumping with balanced vertical weights, eliminating all blank voids across any launcher cell height.
  - **Bottom Row Pairing**: Symmetrically balanced gold `hrs rendered` text on the bottom-left with the pill `+ LOG OT` button on the bottom-right.

---

## [v1.5.2] - 2026-10-18
### Fixed
- **4×2 Cutoff Terminal Visual Layout**: Fully restructured `OvertimeGlanceWidget` to match Concept 1 mockups:
  - Giant hero gold payout readout (`35sp`) centered across the top section.
  - Full-width goal progress bar placed directly below the payout with `0`, `current hrs`, and `target+` milestone markers.
  - Re-anchored `+ LOG OT` action button to the bottom-right row, paired with gold `hrs rendered` readout on the left, completely eliminating the empty void in the center.
  - Upgraded cutoff pill badge to gold background with high-contrast dark text.

---

## [v1.5.1] - 2026-10-18
### Redesigned
- **4×2 Cutoff Terminal (Jetpack Glance)**:
  - Eliminated dead space with a balanced 2-column layout (hero gold earnings and rendered hours on left, tactile `+ LOG OT` button on right).
  - Integrated active horizontal cutoff goal progress bar with real-time target hours (`CUTOFF GOAL · 4.5 / 30 hrs (15%)`).
- **2×2 Circular Gauge Widget**:
  - Harmonized background color tokens from slate navy to deep `#141414` matte charcoal, eliminating launcher color mismatches.
  - Enhanced center payout readout with high-contrast gold (`#FFD700`) and dark graphite track ring.

---

## [v1.5.0] - 2026-10-18
### Added
- **Jetpack Glance Modern Widget Framework**: Rebuilt the primary home screen widget using **Jetpack Glance (`glance-appwidget`)**, Google's official Compose-for-AppWidgets framework.
- **Tactile Neo-Brutalist Glance Card (4×2)**:
  - Declarative Compose UI with `#141414` deep charcoal card and rounded borders.
  - Live cutoff badge, days remaining counter, hero gold payout readout (`#FFD700`), and hours rendered.
  - Interactive tactile `+ LOG OT` quick-action button launching the app directly.
- **Coroutine-Based State Synchronization**: Direct reactive updates triggered via `OvertimeGlanceWidget().updateAll(context)` whenever shifts or settings are updated in `OvertimeRepository`.

### Removed
- **Legacy XML Widget Layouts**: Removed legacy `PayrollVoucherWidgetProvider` and `RubberStampWidgetProvider` alongside deprecated RemoteViews XML templates, simplifying widget maintenance and reducing APK footprint.

---

## [v1.4.3] - 2026-10-17
### Fixed
- **Hero Voucher Header**: Fixed duplicate `"PAYDAY: "` prefix and text collision in `NeoReceiptCard`. Separated card badges and cutoff date/payday into two well-spaced rows to prevent wrapping on narrow screens.
- **DTR Payday Label**: Cleaned redundant `"Payday: "` prefix in the Official Record Sheet header under the `"PAYROLL DATE"` label.

### Optimized
- **Compose Recomposition & Animation**: Replaced `modifier.scale` with `modifier.graphicsLayer` in `AnimatedMoneyCounter` to evaluate scale bounces purely in the Draw phase.
- **Memory & Allocation**: Wrapped date formatters in `remember` across `CalendarTab`, `SettingsTab`, and `TimeRangeOvertimeDialog` to prevent high-frequency object allocations during interactions.
- **Lazy Layout Performance**: Added `contentType` to shift items in `CalendarTab` for efficient Compose slot table recycling.
- **Chart Aggregations**: Cached total, peak, and average calculations in `EarningsBarChart` with `remember(currentList)`.

---

## [v1.4.2] - 2026-10-17
### Fixed
- **Widget RemoteViews Inflation**: Replaced raw `<View>` tags with standard `RemoteViews`-compliant `<FrameLayout>` and `<ImageView>` containers in `widget_payroll_voucher.xml` and `widget_rubber_stamp.xml`, resolving launcher crash (`Class not allowed to be inflated android.view.View`) on Xiaomi HyperOS/MIUI and other OEM launchers.
- **Color Token Standardization**: Bound all widget shape drawables strictly to standard `@color` resources in `colors.xml` for uniform launcher compatibility.

---

## [v1.4.1] - 2026-10-17
### Added
- **Payroll Voucher Widget (4×2)**: Physical punch-card payroll receipt layout with live cutoff status header, cutoff range subtitle, hero gold earnings readout, and a direct `+ LOG OT` action button.
- **Rubber Stamp Widget (2×2)**: Ultra-compact, high-contrast lock screen and home screen companion with `[ APPROVED OT ]` stamp badge, days-left counter, and quick add button.
- **4-Widget Multi-Provider Pipeline**: Live parallel data synchronization across all 4 widgets whenever shifts or cutoff preferences change.

---

## [v1.4.0] - 2026-10-16
### Added
- **Tactile Neo-Brutalist Navigation**:
  - `NeoTopAppBar`: High-contrast header with section subtitles, solid 2dp divider, and active cycle status indicator.
  - `NeoNavigationBar`: 3D tactile pill tabs with solid 2.5dp hard drop shadows, bouncy spring animations, and gold selection.
- **Zero-Jitter Monospace Typography**: Monospaced tabular digits across cash ticker, DTR ledger, and cutoff summaries preventing horizontal jumping during ticker rolls.
- **DOLE Overtime Rubber-Stamp Badges**: Dynamic stamps with industrial inspection tilt (`REGULAR 125%`, `REST DAY 130%`, `SPECIAL HOLIDAY 130%`, `REGULAR HOLIDAY 200%`, `NIGHT DIFF +10%`).
- **Perforated Payroll Voucher Cards (`NeoReceiptCard`)**: Physical punch-card tickets with semicircular notches and jagged perforated tear lines.
- **Asymmetric Neo-Bento Dashboard**:
  - Prominent "Today" focal tile with live activity pulse dot.
  - Stacked weekly and monthly companion blocks.
  - Side-by-side historical cutoff and career total benchmarks.

---

## [v1.3.1] - 2026-10-16
### Added
- **Interactive Cutoff Calendar Visualizer**: Monthly calendar visualizer color-coding Cycle 1 (Gold) and Cycle 2 (Slate) with closing day boundary markers.
- **1-Tap Schedule Presets**: Instant selection for Standard (1st–15th & 16th–End) and BPO/Mid-Month (11th–25th & 26th–10th).
- **Day-Grid Picker Dialog**: Tap-to-pick calendar day dialog eliminating manual number typing.

---

## [v1.3.0] - 2026-10-15
### Added
- **DOLE Statutory 3-Tier Rate Categories**:
  - Regular Workday OT (125%).
  - Special Non-Working Holiday OT (130%).
  - Regular Holiday OT (200%).
- **Interactive 3-Card Rate Selector**: Shift logger selector with live formula previews (`₱93.75 × 1.30 = ₱121.88/hr`).
- **Night Shift Differential Stacking**: Night differential (+20%) stacks directly onto active DOLE holiday rates.
- **DTR Ledger Badges**: Explicit category badges on shift entries (`SNWH +30%`, `RH +100%`, `+NSD`).

---

## [v1.2.0] - 2026-10-15
### Added
- **Automatic Night Shift Differential (+20%)**: Automatic detection of hours between 10:00 PM – 6:00 AM, splitting shifts into Day OT and Night OT.
- **Formal DTR Cutoff Attendance Sheet**: Ledger table matching physical company OT authorization slips.

---

## [v1.1.0] - 2026-10-15
### Added
- Dynamic cash ticker roll animation with native `SoundPool` audio synthesis.
- JSON backup export and import.

---

## [v1.0.0] - 2026-10-14
### Initial Release
- Core overtime logging, rate calculation, and cutoff tracking.
