### Overtime Tracker v1.4.2 — Fix Widget RemoteViews Inflation & Compatibility

This hotfix resolves a critical layout inflation issue affecting the new Neo-Brutalist home screen widgets.

#### 🐛 Fixes & Improvements in v1.4.2:

- **🩹 Fixed Widget Inflation Crash (`Class not allowed to be inflated android.view.View`)**:
  - Replaced raw `<View>` tags with standard RemoteViews-compatible `<FrameLayout>` and `<ImageView>` containers in both `widget_payroll_voucher.xml` and `widget_rubber_stamp.xml`.
  - Android RemoteViews prohibits `<View>` directly, which caused the launcher to fail widget inflation and render blank white boxes.
- **🎨 Color Token Clean-Up**:
  - Bound stamp and voucher drawables strictly to standard `@color` resources for universal compatibility across all OEM Android launchers (HyperOS / MIUI / OneUI / Pixel Launcher).
- **✅ Verified Rendering**:
  - **Concept 1: Payroll Voucher (4×2)** and **Concept 4: Rubber Stamp (2×2)** now inflate and render with full Neo-Brutalist contrast, tactile gold shadows, and live data synchronization.
