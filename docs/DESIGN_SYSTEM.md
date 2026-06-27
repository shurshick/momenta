# Momenta Design System

Current visual standard: **Momenta Design Concept v1**.

Reference image:

`docs/design/momenta_visual_concept.png`

## Direction

Momenta is a dark-first social camera app. The UI should feel premium, calm, and alive: deep dark surfaces, neon green action, a warm yellow moment dot, circular time/camera metaphors, large CTAs, soft cards, and short Russian interface text.

Avoid:

- ECG/pulse/medical style.
- Fitness or wellness associations.
- TikTok clone layouts.
- Light theme as the primary experience.
- Random acidic colors outside the palette.

## Palette

| Token | Hex | Usage |
|---|---|---|
| Deep Background | `#0B0E12` | App background |
| Surface | `#141820` | Cards, panels |
| Surface Alt | `#1C222D` | Elevated blocks, placeholders |
| Momenta Green | `#29F08A` | Primary action, active nav |
| Warm Accent | `#FFC46B` | Moment dot, emotional accent |
| Sky Accent | `#5CA8FF` | Secondary accent |
| Text | `#FFFFFF` | Primary text |
| Secondary Text | `#A3AAB4` | Supporting text |
| Dividers | `#232733` | Borders |
| Error | `#FF5C7A` | Errors, danger |

## Core Components

Android design components live in:

`android/app/src/main/java/com/bghitech/momenta/core/design/`

- `MomentaTheme.kt`
- `MomentaColors.kt`
- `MomentaTypography.kt`
- `MomentaShapes.kt`
- `MomentaButton.kt`
- `MomentaCard.kt`
- `MomentaBottomBar.kt`
- `MomentaLogo.kt`
- `MomentaLoading.kt`
- `MomentaScaffold.kt`
- `MomentaIcons.kt`

## Logo

The temporary Compose logo is a Canvas mark:

- green circular arc;
- warm yellow dot;
- no ECG or medical pulse metaphor;
- used on Splash, Onboarding, Auth, Settings/About, loading and empty states.

## Navigation

Bottom navigation has four user-facing items:

- Момента
- Мир сейчас
- Создать
- Профиль

The Create action is the central highlighted circular button.

## Screens

The primary visual flow is:

1. Splash: logo, app name, slogan.
2. Onboarding/Auth: dark background, logo, card forms, large green CTA.
3. Today: "Момента дня", daily topic card, timer, examples, CTA.
4. Camera: full-screen camera, dark overlay controls, large capture button.
5. Publish: full photo preview, bottom/card controls, green publish CTA.
6. Success: glowing brand mark/check and "Смотреть мир сейчас".
7. Feed: "Мир сейчас", large social cards, reactions/report/share.
8. Profile: avatar, stats cards, recent moments.
9. Settings: grouped cards for account, server, app and support.

## Text

Default interface language is Russian. User-facing strings should live in Android resources where practical.
