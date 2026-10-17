# Changelog

All notable changes to **Overtime Tracker** are documented here. Releases are published as signed binaries on the [GitHub Releases Page](https://github.com/KouseiA/Overtime-Tracker/releases).

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
