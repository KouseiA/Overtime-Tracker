### Overtime Tracker v1.4.0 - Neo-Brutalist Redesign & High-Density Bento Dashboard

This major release elevates Overtime Tracker into a bespoke, tactile Neo-Brutalist design language featuring monospace tabular typography, official Philippine DOLE overtime rate rubber stamps, physical perforated voucher cards, and an asymmetric high-density Bento dashboard.

#### ⚡ What's New in v1.4.0:

- **🏛️ Tactile Neo-Brutalist Navigation**:
  - **NeoTopAppBar**: High-contrast top bar with branded lightning badge (`⚡`), monospaced section subtitles, a solid 2.dp divider, and active cycle status indicator.
  - **Arcade Pill NeoNavigationBar**: 3D tactile pill tabs with solid 2.5.dp hard drop shadows, bouncy squish spring animations, and signature Gold accent selection.

- **📟 Zero-Jitter Monospace Typography (P2)**:
  - Financial numbers across the dynamic money counter, DTR ledger table, cutoff summaries, and period cards now render in `FontFamily.Monospace`.
  - Digit widths are completely uniform, preventing text jumping or horizontal jitter during animated ticker rolls.

- **🏷️ DOLE Statutory Overtime Rubber-Stamp Badges (P2)**:
  - Dynamic rubber-stamp components with an authentic `-1.5°` industrial inspection tilt.
  - Distinct DOLE statutory classifications: `REGULAR 125%`, `REST DAY 130%`, `SPECIAL HOLIDAY 130%`, `REGULAR HOLIDAY 200%`, and `NIGHT DIFF +10%`.

- **🎟️ Perforated Payroll Voucher Cards (`NeoReceiptCard`, P3)**:
  - Card components styled as physical punch-card tickets and payroll vouchers with solid hard drop shadows, semicircular ticket punch notches, and a jagged dashed tear line (`PerforatedTearDivider`).
  - Integrated into the **Cutoff Hero Voucher** and the **Formal DTR Cutoff Summary Sheet** with verified DOLE compliance stamps.

- **🍱 High-Density Asymmetric Neo-Bento Dashboard (P4)**:
  - **Focal Today Tile**: Dedicated prominent daily tracker with live green activity pulse dot (`#22C55E`), bold monospace hours, and daily earnings.
  - **Stacked Weekly & Monthly Tiles**: Compact companion bento blocks for quick cycle context.
  - **Side-by-Side Historical Benchmarks**: Previous cutoff and all-time career totals aligned horizontally to maximize screen density without wasted vertical space.
