# Night Shift Differential (NSD) Design Specification

## 1. Intent & Purpose
In accordance with DOLE (Department of Labor and Employment - Philippines) guidelines and standard warehouse/industrial labor practices, work rendered during the night window between **10:00 PM and 6:00 AM** entitles workers to an additional **Night Shift Differential (NSD)** premium (default: +20% for overtime hours).

Workers currently have to manually compute fractional hours crossing the 10:00 PM threshold and often lack transparency into why their payslip numbers differ from their standard overtime hours. This feature automatically detects shifts crossing the night window, computes the exact hourly split, and provides full, clear formula transparency (`₱93.75 × 1.20 = ₱112.50/hr`) directly on the UI and DTR forms.

---

## 2. Goals & Non-Goals

### Goals
- **100% Automatic Detection**: Whenever a shift touches 10:00 PM to 6:00 AM, the app automatically splits hours into Day OT and Night OT without requiring the user to flip any manual switches.
- **Formula Transparency**: Display explicit arithmetic breakdowns rather than just a percentage (e.g., `₱93.75 × 1.20 = ₱112.50/hr`, with Day OT and Night OT amounts itemized).
- **Optimized UI/UX Design**:
  - Consistent with the app's signature dark luxury OLED aesthetic (`#121212`, `#1E1E1E`, amber/gold accents `#FFD700`, `#FFC107`).
  - Subtle luminous night-sky card styling (`#171926` with indigo border `#312E81` and gold highlights) indicating night shift differential.
  - Zero recomposition lag during time selection sliders.
- **Formal DTR Alignment**: Clear columns/subtext on the DTR table separating regular OT hours from night differential OT hours for easy transcription onto company log sheets.
- **Configurable Settings**: A dedicated Night Differential setting under Payroll Settings (toggle ON/OFF, default rate 20%, customizable).
- **Backward Compatibility**: Fully compatible with existing saved records.

### Non-Goals
- Changing the standard base rate logic for shifts that do not cross 10:00 PM.
- Requiring an internet connection or external payroll API.

### Anti-Goals
- UI clutter: Never hide or bury the primary shift times or primary total pay; the breakdown must enhance readability, not overwhelm the screen.
- Inaccurate time-zone calculations: Calculation must rely on local device day boundaries.

---

## 3. Mathematical & Business Logic Specification

### Time Window
- **Day OT Window**: 06:00 AM to 10:00 PM (16.0 hours window)
- **Night OT Window**: 10:00 PM to 06:00 AM next day (8.0 hours window)

### Calculation Algorithm
For any shift defined by `startTimeMillis` and `endTimeMillis`:
1. Calculate overall duration rounded down to 30-minute increments (`floor(durationMinutes / 30.0) * 0.5`).
2. Evaluate each 30-minute block from start to end:
   - If the block's midpoint hour is $\ge 22$ (10 PM) or $< 6$ (6 AM), allocate to `nightOtHours`.
   - Otherwise, allocate to `dayOtHours`.
3. Rates:
   - `baseRate` = `hourlyRate` (e.g., ₱93.75)
   - If `isHoliday`: `effectiveDayRate` = `baseRate * 1.30`
   - `effectiveNightRate` = `effectiveDayRate * (1.0 + nightDiffRatePercent)` (e.g., `93.75 * 1.20 = 112.50` on regular days; `121.88 * 1.20 = 146.25` on holidays)
4. Total Pay:
   - `dayPay` = `dayOtHours * effectiveDayRate`
   - `nightPay` = `nightOtHours * effectiveNightRate`
   - `totalPay` = `dayPay + nightPay`

---

## 4. UI/UX Surface Architecture

### 4.1 Log / Edit Shift Dialog (`CalendarTab.kt`)
- When start/end time touches the 10:00 PM – 6:00 AM window:
  - An animated **Night Shift Breakdown Card** expands smoothly.
  - **Visual Elements**:
    - Moon icon (`🌙`) + `Night Shift Differential Applied (+20%)` pill.
    - Explicit rate equation: `₱93.75 × 1.20 = ₱112.50/hr`.
    - Compact two-line tabular breakdown:
      - `Day OT:   X.X hrs @ ₱93.75  = ₱XXX.XX`
      - `Night OT: Y.Y hrs @ ₱112.50 = ₱YYY.XX`
    - High-contrast gold total.

### 4.2 Calendar Day Sheet & History List
- Shifts containing night differential display a compact indigo/gold pill: `🌙 1.0h Night OT`.
- Tapping displays the full transparent arithmetic card.

### 4.3 DTR Sheet Tab (`DtrTab.kt`)
- Under the hours column or in the record detail row, display: `5.0h Day + 1.0h Night`.
- Summary footer shows total Day OT hours and total Night OT hours for the cutoff period.

### 4.4 Settings Tab (`SettingsTab.kt`)
- **Night Differential Setting Card**:
  - Switch: Enable Night Differential (default: ON).
  - Rate percentage input / presets: `10%`, `15%`, `20% (Default)`, `25%`.

---

## 5. Data Migration & Persistence
- Add optional fields to JSON serialization in `OvertimeRecord`:
  - `nightDiffHours: Double` (calculated getter, backwards compatible)
  - `nightDiffRate: Double` (saved per record to preserve historic calculation)
- Older JSON records load seamlessly with default computation.

---

## 6. Review & Approval Gate
Upon human partner review and approval of this design document, implementation will be planned and executed using `superpowers:writing-plans`.
