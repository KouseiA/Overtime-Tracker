---
name: philippine-labor-dole
description: Authoritative Philippine Department of Labor and Employment (DOLE) statutory overtime, holiday pay, and night shift differential rules, formulas, and compliance standards based on the Labor Code of the Philippines (Presidential Decree No. 442).
---

# Philippine Labor Standards & DOLE Overtime Calculations

Use this skill whenever adding, reviewing, or calculating wages, overtime hours, night shift differentials, holiday multipliers, or payroll cutoff schedules in Overtime Tracker.

## 1. Statutory Legal Basis (Labor Code of the Philippines)

- **Normal Hours of Work**: Maximum 8 hours per day (Article 83).
- **Overtime Work**: Work rendered beyond 8 hours on ordinary working days (Article 87).
- **Night Shift Differential (NSD)**: Work performed between 10:00 PM and 6:00 AM (Article 86).
- **Weekly Rest Day**: Every employee is entitled to a rest period of not less than 24 consecutive hours after every 6 consecutive normal work days (Article 91).
- **Holiday Pay**: Articles 93 & 94, plus annual Proclamations issued by the President.

---

## 2. Multipliers & Rate Formula Cheatsheet

Let `HourlyRate` = employee's base regular hourly rate (e.g. ₱93.75).

| Work Category | Rate Multiplier | Effective Day Rate (Sample ₱93.75) | Formula |
|---|---|---|---|
| **Regular Overtime** | `×1.00` (or `+25%` of basic) | `₱93.75` | `HourlyRate × 1.00` |
| **Special Non-Working Holiday (SNWH)** | `×1.30` (`+30%`) | `₱121.875` | `HourlyRate × 1.30` |
| **Special Holiday on Rest Day** | `×1.50` (`+50%`) | `₱140.625` | `HourlyRate × 1.50` |
| **Regular Holiday (RH)** | `×2.00` (`+100%`, Double Pay) | `₱187.50` | `HourlyRate × 2.00` |
| **Regular Holiday on Rest Day** | `×2.60` (`+160%`) | `₱243.75` | `HourlyRate × 2.60` |

---

## 3. Night Shift Differential (NSD) Rules (10:00 PM – 6:00 AM)

- **Statutory Minimum**: Not less than `10%` of the regular wage for each hour of work.
- **Warehouse / BPO Industry Standard**: Commonly configured as `+20%` (0.20), as supported in Overtime Tracker settings.
- **Stacking Rule**: Night Differential is computed on top of the already-adjusted category rate:
  $$\text{Effective Night Rate} = \text{Effective Day Rate} \times (1 + \text{NightDiffRate})$$

### Examples (with 20% NSD and base ₱93.75):
1. **Regular OT Night**:
   $$93.75 \times 1.20 = \text{₱112.50 / hr}$$
2. **Special Holiday Night**:
   $$93.75 \times 1.30 \times 1.20 = 121.875 \times 1.20 = \text{₱146.25 / hr}$$
3. **Regular Holiday Night**:
   $$93.75 \times 2.00 \times 1.20 = 187.50 \times 1.20 = \text{₱225.00 / hr}$$

---

## 4. Split Shift Calculations (Day Hours vs. Night Hours)

When a shift spans across the 10:00 PM boundary (e.g., 5:00 PM to 1:00 AM):
1. Split into **Day Hours** (5:00 PM – 10:00 PM = 5.0 hrs).
2. Split into **Night Hours** (10:00 PM – 1:00 AM = 3.0 hrs).
3. Compute separately:
   $$\text{Total Pay} = (\text{Day Hours} \times \text{Day Rate}) + (\text{Night Hours} \times \text{Night Rate})$$

---

## 5. Philippine Payroll Cutoff Conventions

- **Semi-Monthly (Most Common)**:
  - *Standard*: 1st–15th (Payday around 20th) & 16th–End of Month (Payday around 5th of next month).
  - *Mid-Month / BPO*: 11th–25th (Payday around 30th) & 26th–10th (Payday around 15th).
- **Dispute Prevention Rule**: Always state whether cutoff boundaries are *inclusive*.
