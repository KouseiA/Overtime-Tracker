---
trigger: always_on
description: Mandatory HTML/CSS preview mockup creation before implementing or redesigning any UI component, screen, or widget.
---

## UI Preview Mockup Workflow

Whenever proposing, creating, or redesigning any UI screen, component, dialog, or home screen widget in Overtime Tracker:

Rules:
1. **Never write Android/Kotlin UI code before user visual review**:
   - Do NOT begin modifying or creating Jetpack Compose code or XML layouts without first presenting an interactive visual preview to the user.
2. **Create High-Fidelity HTML/CSS Mockups**:
   - Build an interactive HTML preview in `docs/mockups/<feature_name>.html`.
   - Adhere strictly to `.agents/skills/neo-brutalist-design-system/SKILL.md` (`#141414` deep charcoal cards, `#FFD700` gold accents, `#252525` pill badges, hard tactile borders, and monospace tabular numbers).
   - Display mockups inside a realistic mobile/widget viewport container.
3. **Launch Preview Automatically**:
   - Launch the HTML mockup in the user's default browser using `powershell -Command "Start-Process 'docs\mockups\<feature_name>.html'"` so the user can inspect it instantly.
4. **Implement Only After Explicit User Approval**:
   - Only translate the layout into Jetpack Compose / Android code once the user confirms they are happy with the visual design.
