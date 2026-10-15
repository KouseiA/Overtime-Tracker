<div align="center">

# ⏱️ Overtime Tracker
**A modern, offline-first Android application designed for workers to effortlessly log overtime, calculate semi-monthly cutoff earnings, and maintain an accurate, dispute-free shift ledger.**

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Language](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Release](https://img.shields.io/github/v/release/KouseiA/Overtime-Tracker?style=for-the-badge&color=FFD700&label=Release)](https://github.com/KouseiA/Overtime-Tracker/releases/latest)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

<br/>

### 📥 Instant Download

[<img src="https://img.shields.io/badge/⬇️_DOWNLOAD_RELEASE_APK-OvertimeTracker--v1.0.0.apk-FFD700?style=for-the-badge&labelColor=1A1A1A&color=FFD700" height="48" alt="Download APK">](https://github.com/KouseiA/Overtime-Tracker/releases/download/v1.0.0/OvertimeTracker-v1.0.0.apk)

*Compatible with Android 8.0 (Oreo) and above. 100% Free & Open Source.*

---

</div>

## 💡 The Problem & The Solution

At the end of an exhausting 10 or 12-hour shift, doing mental math to calculate fractional overtime hours and overtime pay is a headache. Workers often scribble times on scratch paper, forget days, or get surprised on payday when numbers don't match.

**Overtime Tracker** was created with a single mission: **Every hour counts, every peso matters.**
It gives workers a private, offline tool to track their overtime hours, preview their earnings in real-time, and format shifts into a clean ledger ready to transcribe onto physical company OT forms.

---

## ✨ Features

### 📊 1. Real-Time Earnings Dashboard
- **Current Cutoff Card**: Displays active cutoff period (e.g. *Oct 1–15 • Payday: Oct 20*), total hours rendered, and estimated overtime payout.
- **Dynamic Cash Ticker**: Animated slot-machine style payout roll with synthesized coin-counter sound effects (`SoundPool`) and haptic pulse.
- **Grind Level Badges**: Dynamic badges recognizing your hard work based on hours rendered in the cutoff.
- **Period Breakdown**: Quick overview of hours and pay for **Today**, **This Week**, and **This Month**.
- **Historical Comparison**: Review previous cutoff totals to cross-check with your physical payslip.

### 📅 2. Interactive Overtime Calendar
- **Visual Monthly Grid**: Days with rendered overtime are highlighted with signature gold markers.
- **Tap-to-Log**: Simply tap any day to set start and end times with instant live computation.
- **Smart Rounding**: Automatically calculates 30-minute increment breakdowns (e.g., 35m → 0.5h, 3h35m → 3.5h).
- **Holiday Bonus Support**: Toggle special holiday overtime rate (+30% bonus) with a single tap.

### 📋 3. Formal OT Form DTR Sheet
- **Printable / Transcribable Table**: Formatted to mirror standard company physical OT sheets (`#`, `Date`, `In`, `Out`, `Hours`, `Amount`).
- **Cutoff Filters**: Filter by **Current Cutoff**, **Previous Cutoff**, or **All Records**.
- **Totals Summary**: Instant sum of total hours and total payable amount.

### ⚙️ 4. Universal & Customizable Payroll Cutoff Schedule
- **Flexible Pay Frequencies**: Fully supports **Semi-Monthly (2x/month)**, **Monthly (1x/month)**, and **Weekly** payroll cycles.
- **Custom Cutoff Days**: Set any day configuration (e.g. traditional 1st–15th/16th–End, BPO/corporate 11th–25th/26th–10th, or custom dates).
- **Live Schedule Preview**: Real-time confirmation box showing active cutoff dates, days left, and estimated payday before saving.
- **Whole-App Synchronization**: Dashboard, DTR Sheet filters, Trend Charts, and Widget automatically adapt to your chosen schedule.

### 💾 5. Safe JSON Backup & Restore
- **Export Anywhere**: Export your complete history and settings into a clean JSON file to Google Drive, phone storage, or messaging apps.
- **Import with Confidence**: Restore your data on a new phone with choice of **Merge with Existing** or **Replace All**.
- **100% Offline & Private**: Zero cloud requirements, zero tracking, and no account signup. Your data stays strictly on your device.

### 📲 6. Dual Home Screen Widgets (Compact & Detailed)
- **Compact Gauge (2x2)**: A space-saving square widget featuring a **dynamic glowing gold circular progress ring** that tracks hours rendered against your cutoff target, with bold earnings and a quick `+` log button.
- **Detailed Summary (4x2)**: A full-width dark OLED card displaying active cutoff dates, days remaining, hero gold earnings payout, hours rendered, and a direct `LOG OT +` launch button.
- **Parallel Real-Time Sync**: Both widgets automatically refresh whenever overtime is logged or edited.

---

## 📲 Installation Guide

1. Download the latest APK directly from the [Releases Page](https://github.com/KouseiA/Overtime-Tracker/releases/latest) or click:  
   👉 **[Download OvertimeTracker-v1.0.0.apk](https://github.com/KouseiA/Overtime-Tracker/releases/download/v1.0.0/OvertimeTracker-v1.0.0.apk)**
2. On your Android device, tap the downloaded `.apk` file.
3. If prompted, allow **"Install from unknown sources"** for your browser or file manager.
4. Tap **Install** and launch the app!

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/)
- **State Management**: Android ViewModel + Kotlin `StateFlow`
- **Architecture**: Single-Activity, Clean MVVM
- **Audio Engine**: Android native `SoundPool` with synthesized audio waveforms (zero external `.mp3` dependencies)
- **Data Persistence**: Local `SharedPreferences` with structured JSON serialization
- **Widget**: Android AppWidget Provider with responsive XML RemoteViews
- **Optimization**: ProGuard / R8 code shrinking enabled (< 8MB release APK)

---

## 🏗️ Building from Source

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 35

### Steps
```bash
# Clone the repository
git clone https://github.com/KouseiA/Overtime-Tracker.git
cd Overtime-Tracker

# Build Production Release APK (minified with R8)
./gradlew assembleRelease
```
Compiled APK will be located in:
- `app/build/outputs/apk/release/app-release.apk`

---

## ⚠️ Paalala / Disclaimer

> *Ang mga halaga at total na ipinapakita sa app na ito ay gabay at estimate lamang para sa sarili mong personal monitoring at kopya. Maaaring magkaiba ito sa iyong opisyal na payslip depende sa company policy, tax deductions, at cutoff adjustments.*

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) — free to use, modify, and distribute.

---

<div align="center">
  <sub>Built with ❤️ for hardworking workers everywhere.</sub>
</div>
