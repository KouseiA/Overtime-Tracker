### Overtime Tracker v1.2.0 - Automatic Night Shift Differential (+20%)

This release brings full DOLE-compliant support for **Night Shift Differential (NSD)**, automatic clock-boundary hour splitting, and transparent arithmetic breakdown cards.

#### 🌙 What's New in v1.2.0:
- **🌙 Automatic Night Shift Differential (10:00 PM – 6:00 AM)**:
  - Shifts are automatically scanned and split into **Day OT** (06:00–22:00) and **Night OT** (22:00–06:00).
  - Night hours receive an additional **+20% premium** (e.g. 5:00 PM to 11:00 PM yields 5.0h Day OT + 1.0h Night OT).
  - Stacks properly with Holiday OT bonus (+30%).
- **🔍 Transparent Math Formulas in UI**:
  - Live formula breakdown card inside the shift logger showing the exact math:
    `Rate Formula: ₱93.75 × 1.20 = ₱112.50/hr`
  - Itemized rows for Day OT and Night OT pay calculations so workers know exactly where every peso comes from.
- **📋 Formal DTR Sheet Integration**:
  - Shift rows display itemized `Day` and `Night` hours with `+20% NSD` badges.
  - Cutoff summary footer displays cumulative Day OT vs. Night OT totals for effortless copying onto company paper forms.
- **⚙️ Settings Configuration**:
  - Dedicated toggle to enable/disable Night Shift Differential.
  - Rate presets: `10%`, `15%`, `20% (DOLE Standard)`, `25%` with real-time rate formula preview.
- **📲 Whole-App Synchronization**:
  - Calendar markers, Dashboard earnings ticker, and Dual Widgets automatically reflect updated earnings.
  - 100% offline, private, and backward-compatible with all previous records.
