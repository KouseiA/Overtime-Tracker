# Warehouse Overtime Management App (Team Leader Edition)

An Android application built with **Kotlin**, **Jetpack Compose**, and **Material 3**, custom-designed for a **Warehouse Team Leader** to track overtime hours at **₱93.75 / hr**, manage semi-monthly cutoffs (1st–15th and 16th–end of month), and provide a formal, numbers-only DTR sheet for transcribing onto physical OT Forms.

---

## 🎯 Clean, Focused Structure

### 1. 📊 Statistics (Dashboard)
* **Pure Statistics Only**:
  * **Current Cutoff Card**: Active period (e.g. *Oct 1–15 • Payday: Oct 20*), Total Hours, and Expected Overtime Payout (₱).
  * **Period Stats**: Today, This Week, and This Month hours and earnings.
  * **Previous Cutoff Card**: Past cutoff totals to cross-check with your physical payslip.
  * **All-Time Summary**: Accumulated OT compensation and total shifts rendered.

### 2. 📅 Calendar — Add & Edit Overtime
* **Visual Calendar**: Days with overtime show a green badge with total hours worked.
* **Tap Any Date**:
  * **Start Time**: (defaults to 5:00 PM, or tap to adjust).
  * **End Time**: Pick when you finished (e.g. 9:00 PM, 6:00 PM, 5:30 PM).
  * **Instant Live Preview**: Shows total duration (e.g. `4.00 hrs`) and money earned (e.g. `₱375.00`).
  * Tap **Save Overtime** to log.
  * *(Zero notes or task boxes — pure numbers only).*

### 3. 📝 OT Form Sheet — Formal DTR Table
* **Formal, high-contrast, official ledger layout**:
  * **Cutoff Selector**: View *This Cutoff (1st–15th or 16th–30th)*, *Previous Cutoff*, or *All*.
  * **Official Header**: Cutoff Period, Payroll Date, Standard Shift (8AM–5PM), and OT Rate (₱93.75/hr).
  * **Table Columns**:
    * `#` (Row number in chronological order)
    * `DATE` (e.g., Oct 03, Oct 06, Oct 10)
    * `IN` (Time In, e.g., 5:00 PM)
    * `OUT` (Time Out, e.g., 9:00 PM)
    * `HOURS` (e.g., 4.00 hrs, 1.00 hr, 0.50 hr)
    * `AMOUNT` (e.g., ₱375.00, ₱93.75, ₱46.88)
  * **Summary Footer**: Total Hours Rendered and Total Amount Payable.
  * *(No copy buttons, no tasks, no notes — clean and formal for reading while writing onto your paper OT Form).*

### 4. ⚙️ Settings
* **OT Hourly Rate**: Pre-set to **₱93.75 / hr**.
* **Regular Shift End Time**: Pre-set to **5:00 PM**.
* **Cutoff Reference**: Explains the 1st–15th (Paid on 20th) and 16th–30th (Paid on 5th) schedule.
* **100% Offline & Private**: Data is stored securely on your phone.

---

## 📱 Pre-Built APK Location

The compiled and signed debug APK is ready:
```
c:\Users\RDC\Pictures\OT APP\app\build\outputs\apk\debug\app-debug.apk
```

### Installation:
```powershell
$env:PATH = "$env:PATH;C:\Users\RDC\AppData\Local\Android\Sdk\platform-tools"
adb install -r "c:\Users\RDC\Pictures\OT APP\app\build\outputs\apk\debug\app-debug.apk"
```
Or transfer `app-debug.apk` to your phone and tap to update.
