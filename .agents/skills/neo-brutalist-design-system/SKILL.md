---
name: neo-brutalist-design-system
description: Design principles, Jetpack Compose tokens, haptics, audio cues, and component patterns for Overtime Tracker's tactile Neo-Brutalist (Monochrome + Gold) UI/UX.
---

# Overtime Tracker Neo-Brutalist Design System

Use this skill whenever designing, modifying, or creating UI components, dialogs, widgets, animations, or sound effects in Overtime Tracker.

## 1. Visual Aesthetics & Philosophy

Overtime Tracker uses a **Tactile Neo-Brutalist & Arcade** design aesthetic tailored for hard-working industrial, warehouse, and BPO employees:
- **No blurry soft drop shadows**: Hard, crisp, high-contrast black borders and solid drop shadows.
- **Physical tactile squish**: Interactive cards and buttons physically displace diagonally into their shadow when pressed, simulating a real mechanical arcade micro-switch.
- **Hero Gold Accent**: Gold (`#FFD700` / `#FFC107`) is reserved exclusively for money, wage earnings, hero numbers, and active selected state badges.
- **Deep OLED Dark Mode & High-Contrast Light Mode**: Sharp legibility even under harsh outdoor or warehouse warehouse lighting.

---

## 2. Core Color & Typography Tokens

| Token | Light Theme | Dark Theme | Purpose |
|---|---|---|---|
| `GoldAccent` | `#FFD700` | `#FFD700` | Peso values, active badges, earnings |
| `GoldAccentDark` | `#FFC107` | `#FFC107` | Secondary gold gradient accent |
| `DangerRed` | `#CC0000` | `#CC0000` | Destructive actions, delete, warnings |
| `Surface` | `#FFFFFF` | `#1A1A1A` | Cards, modal sheets |
| `SurfaceElevated` | `#F4F4F4` | `#252525` | Input fields, preview boxes |
| `SurfaceBorder` | `#0A0A0A` | `#333333` | Hard borders |

---

## 3. The `NeoCard` & `NeoButton` Interactive Pattern

### Card Implementation Pattern:
```kotlin
NeoCard(
    modifier = Modifier.fillMaxWidth(),
    shadowOffset = 4.dp,
    shape = RoundedCornerShape(16.dp),
    onClick = { /* action */ }
) {
    // Content with proper padding (typically 16.dp to 18.dp)
}
```

### Squish Physics Mechanism:
- Uses `MutableInteractionSource` + `collectIsPressedAsState()`.
- Animate offset using `spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)`.
- When pressed, moves `offset(x = 4.dp, y = 4.dp)` directly onto the shadow layer.

---

## 4. Multi-Sensory Feedback (Audio + Haptics)

1. **Tactile Vibration**:
   - Call `VibrationHelper.vibrateTick(context)` on standard button clicks.
   - Call `VibrationHelper.vibrateSuccess(context)` on shift save or cash calculation.
2. **Synthesized Cash / Money Sounds**:
   - `MoneySound.playCashRegister()` or synthesized waveforms on earning milestones.
   - Always check `settings.soundEnabled` before playing.

---

## 5. Home Screen Widget Design Rules

- Keep widgets dark, high-contrast, and glanceable.
- Use `RemoteViews` with vector backgrounds with crisp outlines (`widget_card_bg.xml`, `widget_btn_bg.xml`).
- Include direct touch targets (`PendingIntent`) for quick launch.
