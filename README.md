<div align="center">

# Overtime Tracker

**An offline-first Android overtime ledger and cutoff earnings calculator engineered for Philippine statutory labor standards (PD 442).**

[![Latest Release](https://img.shields.io/github/v/release/KouseiA/Overtime-Tracker?style=flat-square&color=FFD700&label=Release)](https://github.com/KouseiA/Overtime-Tracker/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-3DDC84?style=flat-square&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Binary Size](https://img.shields.io/badge/APK_Size-7.89_MB-success?style=flat-square)](https://github.com/KouseiA/Overtime-Tracker/releases/latest)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

<br/>

[**Download Latest Release (v1.4.2 APK)**](https://github.com/KouseiA/Overtime-Tracker/releases/download/v1.4.2/OvertimeTracker-v1.4.2.apk) • [**Report an Issue**](https://github.com/KouseiA/Overtime-Tracker/issues) • [**Changelog**](RELEASE_NOTES_v1.4.2.md)

</div>

---

## Interface Preview

| Bento Dashboard | Shift Calendar | Formal DTR Ledger | Rates & Settings |
|:---:|:---:|:---:|:---:|
| <img src="docs/screenshots/01_dashboard.jpg" width="220" alt="Bento Dashboard" /> | <img src="docs/screenshots/02_calendar.jpg" width="220" alt="Shift Calendar" /> | <img src="docs/screenshots/03_dtr_sheet.jpg" width="220" alt="Formal DTR Ledger" /> | <img src="docs/screenshots/04_settings.jpg" width="220" alt="Warehouse OT Settings" /> |
| *Hero voucher & live stats* | *Visual monthly hour tags* | *Transcribe-ready table* | *DOLE NSD & rate formulas* |

---

## Overview

Overtime Tracker simplifies payroll tracking for industrial, warehouse, BPO, and shift workers across the Philippines. It automates statutory overtime rates, detects night shifts, synchronizes semi-monthly cutoff cycles, and generates clean, transcribable logs to dispute or verify physical payslips.

- **100% Offline & Private**: Zero external network calls, accounts, or telemetry. All shift records and configuration parameters remain entirely on the local device.
- **DOLE Statutory Compliance**: Implements the legal premium formulas defined in Presidential Decree No. 442 (The Labor Code of the Philippines).
- **Tactile Neo-Brutalist Design**: High-contrast OLED dark theme with zero-jitter monospace financial figures, mechanical squish buttons, and native tactile vibration cues.

---

## Statutory Pay Calculation Matrix

Calculations strictly mirror Department of Labor and Employment (DOLE) rules:

| Shift Classification | Statutory Reference | Standard Multiplier | With Night Shift Differential (+20%) |
|---|---|:---:|:---:|
| **Regular Workday OT** | Labor Code Art. 87 | **125%** (`× 1.25`) | **150%** (`× 1.50`) |
| **Rest Day OT** | Labor Code Art. 93 | **169%** (`× 1.69`) | **202.8%** (`× 2.028`) |
| **Special Non-Working Holiday (SNWH)** | Labor Code Art. 93 | **130%** (`× 1.30`) | **156%** (`× 1.56`) |
| **Regular Holiday (RH)** | Labor Code Art. 94 | **200%** (`× 2.00`) | **240%** (`× 2.40`) |
| **Night Shift Differential (NSD)** | Labor Code Art. 86 | **+10% to +25%** *(Default: 20%)* | Applied to all hours worked between 10:00 PM – 6:00 AM |

---

## Core Capabilities

### 1. Semi-Monthly Cutoff Engine
- Pre-configured presets for common Philippine corporate schedules:
  - **Standard 15th/End**: 1st–15th (Payday: 20th) & 16th–End (Payday: 5th).
  - **Mid-Month Cutoff**: 11th–25th (Payday: 30th) & 26th–10th (Payday: 15th).
  - **Custom Bounds**: User-defined cycle start and end days via visual date-picker dialogs.
- Active countdown ticker indicating remaining cycle days.
- Live earnings counter with low-latency synthesized cash sounds (`SoundPool`).

### 2. Transcribe-Ready DTR Ledger
- Structured shift ledger formatted to match physical company OT authorization forms (`Date`, `In`, `Out`, `Hours`, `Amount`).
- Filters for **This Cutoff**, **Previous Cutoff**, or **Complete History**.
- Displays combined cumulative cutoff totals and shift-by-shift Day vs. Night breakdowns.

### 3. Automatic Night Shift Differential Detection
- Automatically identifies hours falling within the statutory **10:00 PM – 6:00 AM** window.
- Splits shift duration into Day OT and Night OT sub-totals without requiring manual calculation.
- Stacks onto Special Holiday and Regular Holiday overtime rates.

### 4. Home Screen Widgets
Four real-time synchronized AppWidgets:
- **Payroll Voucher (4×2)**: Perforated ticket layout with cycle bounds, live cutoff pay, and quick-add trigger.
- **Rubber Stamp (2×2)**: High-contrast compact badge displaying hours and days remaining.
- **Circular Goal Gauge (2×2)**: Dynamic progress ring tracking cutoff hours against user-defined goals (15h–50h).
- **Cutoff Summary (4×2)**: Complete summary card with hero earnings readout.

### 5. Local Data Sovereignty
- Single-tap export to standardized JSON format.
- Safe import supporting **Merge with Existing** or **Clean Overwrite**.

---

## Technical Stack & Architecture

- **Language**: Kotlin 2.0.21
- **UI Toolkit**: Jetpack Compose + Material 3 (Custom Neo-Brutalist theme tokens)
- **Architecture**: Single-Activity, Clean MVVM
- **State Management**: Reactive `StateFlow` backed by atomic shared preferences
- **Audio Engine**: Native Android `SoundPool` waveform synthesis (zero external audio asset dependencies)
- **Widgets**: Multi-receiver Android `AppWidgetProvider` with `RemoteViews`
- **Compiler Optimizations**: ProGuard / R8 code and resource shrinking (< 8 MB release binary)

---

## Building from Source

### Prerequisites
- Android Studio Ladybug (2024.2+) or newer
- JDK 17
- Android SDK 36 (compileSdk 36, minSdk 24)

### Build Commands
```bash
# Clone the repository
git clone https://github.com/KouseiA/Overtime-Tracker.git
cd Overtime-Tracker

# Build unsigned debug APK
./gradlew assembleDebug

# Build optimized production release APK
./gradlew assembleRelease
```
The output APK is generated at:
```
app/build/outputs/apk/release/app-release.apk
```

---

## Disclaimer

> **Paalala:** The amounts and totals calculated by this application are estimates intended for personal monitoring and cross-referencing. Final compensation may vary according to company policy, tax withholding, and internal payroll adjustments.

---

## License

This project is licensed under the [MIT License](LICENSE) — free to inspect, modify, and distribute.
