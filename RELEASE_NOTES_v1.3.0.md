### Overtime Tracker v1.3.0 - DOLE-Compliant 3-Tier Overtime Rates

This release introduces comprehensive Philippine Department of Labor and Employment (DOLE) statutory overtime rate tiers, giving workers precise, dispute-free calculations for regular workdays and all Philippine holiday types.

#### 🇵🇭 What's New in v1.3.0:
- **⚖️ DOLE Statutory Rate Categories**:
  - **Regular OT (×1.00)**: Standard overtime computed directly at your configured base OT rate (e.g. ₱93.75/hr) with no extra multipliers.
  - **Special Non-Working Holiday OT (×1.30)**: Statutory +30% holiday overtime premium (e.g. ₱93.75 × 1.30 = ₱121.88/hr).
  - **Regular Holiday OT (×2.00)**: Statutory double-pay (+100%) premium (e.g. ₱93.75 × 2.00 = ₱187.50/hr).
- **🎨 Interactive 3-Card Rate Selector**:
  - Modern Neo-styled selector in the shift logger with color-coded badges, multipliers, and live hourly rate previews.
  - Live formula preview bar dynamically calculates and displays the exact math equation (e.g. `₱93.75 × 2.00 = ₱187.50/hr`).
- **🌙 Seamless Night Differential Stacking**:
  - Night Shift Differential (+20% for 10:00 PM – 6:00 AM) stacks transparently on top of whichever DOLE holiday category is active.
- **📅 Smart Calendar & Ledger Badges**:
  - Itemized category indicators (`🏖️ SNWH +30%`, `🎉 RH +100%`) on shift cards and monthly calendar views.
  - Formal DTR ledger clearly differentiates regular, special, and double-pay shifts with stacked NSD markers (`+30%+NSD`, `+100%+NSD`).
- **🔄 100% Backward Compatible**:
  - All existing shift logs from previous versions are automatically migrated with zero data loss.
