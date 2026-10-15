# Night Shift Differential (NSD) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement automatic Night Shift Differential (+20% for 10:00 PM – 6:00 AM) with transparent formula breakdown, optimized dark-gold UI, and formal DTR integration.

**Architecture:** Extend `OvertimeRecord` with clock-boundary hour splitting between Day OT (06:00–22:00) and Night OT (22:00–06:00), compute itemized pay with transparent formula formatting, and surface this in `CalendarTab` (Log dialog + shift cards), `DtrTab` (transcription table), and `SettingsTab` (DataStore persistence).

**Tech Stack:** Kotlin 2.0, Jetpack Compose, Material 3, AndroidX DataStore, JUnit 4, Kotlin Coroutines Flow.

**Spec:** [docs/superpowers/specs/2026-10-15-night-differential-design.md](file:///c:/Users/RDC/Pictures/OT%20APP/docs/superpowers/specs/2026-10-15-night-differential-design.md)

## Global Constraints
- Target Android SDK 36, Min SDK 24.
- 100% offline; zero external network requests.
- Backward compatibility: old JSON records without night differential fields must deserialize without error and compute correctly.
- Signature luxury dark OLED design system (`#121212`, `#1E1E1E`, gold accents `#FFD700`, `#FFC107`, night card `#171926` with indigo border `#312E81`).
- Preserves 30-minute block rounding logic for overtime calculation.

## Review Focus
1. **Shifts crossing midnight (e.g. 8:00 PM to 2:00 AM)**: 8 PM–10 PM allocated to Day OT (2.0 hrs); 10 PM–2 AM allocated to Night OT (4.0 hrs).
2. **Shifts entirely within day hours (e.g. 5:00 PM to 9:00 PM)**: 4.0 hrs Day OT, 0.0 hrs Night OT, no night card shown.
3. **Shifts entirely within night hours (e.g. 10:00 PM to 2:00 AM)**: 0.0 hrs Day OT, 4.0 hrs Night OT, 100% night rate.
4. **Holiday overtime stacking (+30% Holiday, +20% Night Diff)**: Base rate multiplied by 1.30, then night hours multiplied by 1.20 (`93.75 * 1.30 = 121.875`; `121.875 * 1.20 = 146.25`).
5. **Backwards compatibility**: Existing records in JSON format missing `nightDiffRate` load with default 0.20 rate without crashing.

---

### Task 1: Mathematical Engine & Unit Tests in `OvertimeRecord`

**Files:**
- Create: `app/src/test/java/com/example/ottracker/data/OvertimeRecordTest.kt`
- Modify: `app/src/main/java/com/example/ottracker/data/OvertimeRecord.kt:9-70`

**Interfaces:**
- Produces:
  - `OvertimeRecord.nightDiffRate: Double = 0.20`
  - `OvertimeRecord.dayOtHours: Double`
  - `OvertimeRecord.nightOtHours: Double`
  - `OvertimeRecord.hasNightDifferential: Boolean`
  - `OvertimeRecord.effectiveDayRate: Double`
  - `OvertimeRecord.effectiveNightRate: Double`
  - `OvertimeRecord.dayPay: Double`
  - `OvertimeRecord.nightPay: Double`
  - `OvertimeRecord.formattedFormula: String` (e.g., `"₱93.75 × 1.20 = ₱112.50/hr"`)

- [ ] **Step 1: Write the failing unit tests in `OvertimeRecordTest.kt`**

```kotlin
package com.example.ottracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class OvertimeRecordTest {
    private fun createTimestamp(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Test
    fun testEveningShiftCrossingTenPm() {
        // 5:00 PM to 11:00 PM (6.0 hours total: 5h Day, 1h Night)
        val start = createTimestamp(2026, Calendar.OCTOBER, 15, 17, 0)
        val end = createTimestamp(2026, Calendar.OCTOBER, 15, 23, 0)
        val record = OvertimeRecord(
            dateMillis = start,
            startTimeMillis = start,
            endTimeMillis = end,
            hourlyRate = 93.75,
            isHoliday = false,
            nightDiffRate = 0.20
        )
        assertTrue(record.hasNightDifferential)
        assertEquals(5.0, record.dayOtHours, 0.001)
        assertEquals(1.0, record.nightOtHours, 0.001)
        assertEquals(468.75, record.dayPay, 0.001)
        assertEquals(112.50, record.nightPay, 0.001)
        assertEquals(581.25, record.totalPay, 0.001)
    }

    @Test
    fun testPureDayShift() {
        // 5:00 PM to 9:00 PM (4.0 hrs Day, 0h Night)
        val start = createTimestamp(2026, Calendar.OCTOBER, 15, 17, 0)
        val end = createTimestamp(2026, Calendar.OCTOBER, 15, 21, 0)
        val record = OvertimeRecord(
            dateMillis = start,
            startTimeMillis = start,
            endTimeMillis = end,
            hourlyRate = 93.75
        )
        assertFalse(record.hasNightDifferential)
        assertEquals(4.0, record.dayOtHours, 0.001)
        assertEquals(0.0, record.nightOtHours, 0.001)
        assertEquals(375.0, record.totalPay, 0.001)
    }

    @Test
    fun testHolidayShiftWithNightDifferential() {
        // 5:00 PM to 11:00 PM on Holiday (+30%)
        val start = createTimestamp(2026, Calendar.OCTOBER, 15, 17, 0)
        val end = createTimestamp(2026, Calendar.OCTOBER, 15, 23, 0)
        val record = OvertimeRecord(
            dateMillis = start,
            startTimeMillis = start,
            endTimeMillis = end,
            hourlyRate = 93.75,
            isHoliday = true,
            nightDiffRate = 0.20
        )
        assertEquals(121.875, record.effectiveDayRate, 0.001)
        assertEquals(146.25, record.effectiveNightRate, 0.001)
        assertEquals(609.375, record.dayPay, 0.001)
        assertEquals(146.25, record.nightPay, 0.001)
        assertEquals(755.625, record.totalPay, 0.001)
    }

    @Test
    fun testJsonBackwardCompatibility() {
        val start = createTimestamp(2026, Calendar.OCTOBER, 15, 17, 0)
        val end = createTimestamp(2026, Calendar.OCTOBER, 15, 23, 0)
        val legacyJson = org.json.JSONObject().apply {
            put("id", "legacy-id")
            put("dateMillis", start)
            put("startTimeMillis", start)
            put("endTimeMillis", end)
            put("hourlyRate", 93.75)
            put("isHoliday", false)
            put("notes", "")
        }
        val deserialized = OvertimeRecord.fromJsonObject(legacyJson)
        assertEquals(0.20, deserialized.nightDiffRate, 0.001)
        assertEquals(581.25, deserialized.totalPay, 0.001)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat test --tests com.example.ottracker.data.OvertimeRecordTest`
Expected: FAIL due to missing `nightDiffRate`, `dayOtHours`, `nightOtHours`.

- [ ] **Step 3: Implement algorithm in `OvertimeRecord.kt`**

- Add `nightDiffRate: Double = 0.20` to constructor.
- Implement 30-minute slice allocation based on calendar hour midpoint ($\ge 22$ or $< 6$).
- Calculate `dayOtHours`, `nightOtHours`, `effectiveDayRate`, `effectiveNightRate`, `dayPay`, `nightPay`, and `totalPay`.
- Update `toJsonObject()` to serialize `nightDiffRate`.
- Update `fromJsonObject()` with fallback `optDouble("nightDiffRate", 0.20)`.

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat test --tests com.example.ottracker.data.OvertimeRecordTest`
Expected: PASS with 4 tests passed.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/ottracker/data/OvertimeRecord.kt app/src/test/java/com/example/ottracker/data/OvertimeRecordTest.kt
git commit -m "feat(core): implement automatic night differential calculation in OvertimeRecord"
```

---

### Task 2: Optimized UI in Shift Dialog & Calendar Cards

**Files:**
- Modify: `app/src/main/java/com/example/ottracker/ui/tabs/CalendarTab.kt:350-550`

**Interfaces:**
- Consumes: `OvertimeRecord` properties (`hasNightDifferential`, `dayOtHours`, `nightOtHours`, `effectiveDayRate`, `effectiveNightRate`, `dayPay`, `nightPay`).

- [ ] **Step 1: Build the Night Shift Differential Breakdown Card Composable**
- In `CalendarTab.kt`, create `@Composable fun NightDifferentialBreakdownCard(record: OvertimeRecord)`.
- Use the dark night-sky styling: surface color `#171926`, border `#312E81`, moon badge `🌙 Night Shift Differential Applied (+20%)` in `#A5B4FC`.
- Display explicit formula:
  `₱${String.format("%.2f", record.effectiveDayRate)} × 1.20 = ₱${String.format("%.2f", record.effectiveNightRate)}/hr`
- Display itemized lines:
  - `Day OT:   ${record.dayOtHours} hrs @ ₱${record.effectiveDayRate} = ₱${record.dayPay}`
  - `Night OT: ${record.nightOtHours} hrs @ ₱${record.effectiveNightRate} = ₱${record.nightPay}`
- High-contrast gold total: `Total: ₱${String.format("%.2f", record.totalPay)}`.

- [ ] **Step 2: Integrate into `LogShiftDialog`**
- Show `NightDifferentialBreakdownCard` smoothly when the selected start/end times have `tempRecord.hasNightDifferential`.

- [ ] **Step 3: Update Calendar Day Shift List Cards**
- If a shift has `hasNightDifferential`, display a subtle badge `🌙 ${record.nightOtHours}h Night OT` next to the hours text.
- Expanded details show the itemized breakdown.

- [ ] **Step 4: Run Kotlin compilation check**

Run: `.\gradlew.bat compileReleaseKotlin`
Expected: BUILD SUCCESSFUL (0 errors).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/ottracker/ui/tabs/CalendarTab.kt
git commit -m "feat(ui): add optimized night shift breakdown card to CalendarTab"
```

---

### Task 3: DTR Formal Sheet Itemized Display

**Files:**
- Modify: `app/src/main/java/com/example/ottracker/ui/tabs/DtrTab.kt:100-300`

**Interfaces:**
- Consumes: `OvertimeRecord.dayOtHours`, `OvertimeRecord.nightOtHours`, `OvertimeRecord.hasNightDifferential`.

- [ ] **Step 1: Update DTR Table Rows**
- In `DtrTab.kt`, inside the record row items, when `record.hasNightDifferential` is true, show sub-label:
  `Day: ${record.dayOtHours}h | Night: ${record.nightOtHours}h` below the total hours.

- [ ] **Step 2: Update DTR Summary Header / Card**
- Add cumulative cutoff totals:
  - Total Hours
  - Regular Day Hours
  - Night Differential Hours
  - Total Estimated Payout

- [ ] **Step 3: Run Kotlin compilation check**

Run: `.\gradlew.bat compileReleaseKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/ottracker/ui/tabs/DtrTab.kt
git commit -m "feat(dtr): add day vs night OT breakdown to formal DTR sheet"
```

---

### Task 4: SettingsTab Night Differential Configuration

**Files:**
- Modify: `app/src/main/java/com/example/ottracker/data/OvertimeRecord.kt` (Add DataStore key `NIGHT_DIFF_RATE` and `NIGHT_DIFF_ENABLED`)
- Modify: `app/src/main/java/com/example/ottracker/ui/tabs/SettingsTab.kt:150-250`

**Interfaces:**
- Produces: DataStore keys for `NIGHT_DIFF_RATE` (default `0.20f`) and `NIGHT_DIFF_ENABLED` (default `true`).

- [ ] **Step 1: Define DataStore keys in `OvertimeRecord.kt` / `DataStore`**
- `val NIGHT_DIFF_ENABLED = booleanPreferencesKey("night_diff_enabled")`
- `val NIGHT_DIFF_RATE = floatPreferencesKey("night_diff_rate")`

- [ ] **Step 2: Add Night Differential Section in `SettingsTab.kt`**
- Place under Payroll Settings:
  - Switch: "Enable Night Differential (10PM - 6AM)"
  - Quick preset buttons: `10%`, `15%`, `20% (Default)`, `25%`
  - Real-time persistence to DataStore.

- [ ] **Step 3: Connect Settings to `CalendarTab` shift logger**
- Use user's configured night diff rate when saving new records.

- [ ] **Step 4: Run unit tests and compilation**

Run: `.\gradlew.bat test`
Run: `.\gradlew.bat compileReleaseKotlin`
Expected: All tests pass, build succeeds.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/ottracker/data/OvertimeRecord.kt app/src/main/java/com/example/ottracker/ui/tabs/SettingsTab.kt
git commit -m "feat(settings): add night differential toggle and rate presets"
```

---

### Task 5: Full Release Build, Graphify Update & Verification

**Files:**
- Modify: `app/build.gradle.kts` (Bump version to 1.2.0, versionCode 3)
- Modify: `README.md` (Document Night Differential feature)

- [ ] **Step 1: Bump version in `app/build.gradle.kts`**
- `versionCode = 3`, `versionName = "1.2.0"`

- [ ] **Step 2: Run complete release build**

Run: `.\gradlew.bat assembleRelease`
Expected: `app-release.apk` generated successfully in `app/build/outputs/apk/release/`.

- [ ] **Step 3: Update knowledge graph**

Run: `python -m graphify update .`
Expected: Graph updated without errors.

- [ ] **Step 4: Commit and push**

```bash
git add app/build.gradle.kts README.md
git commit -m "release: v1.2.0 with automatic night shift differential"
git push origin main
```
