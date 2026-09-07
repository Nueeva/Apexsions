---
name: Apexsions
description: The Peak Civilizations — Monolithic Obsidian, Celestial Ivory & Royal Gold Design System
colors:
  # ✨ Extended Gold Palette
  gold-radiant: "#F1D58A"
  gold-light: "#E8C878"
  primary: "#C9A45C"           # Royal Gold (Primary Accent)
  gold-antique: "#9E7B3E"
  gold-dark: "#72552A"
  
  # 🤍 Extended Celestial Ivory Typography
  ivory-light: "#FFFDF8"
  text-main: "#F4EFE6"         # Celestial Ivory (Primary Text / WCAG AAA > 17:1)
  text-sub: "#DDD7CC"          # Marble (Body Copy)
  text-muted: "#B8B1A5"        # Stone (Secondary / Muted)
  text-dim: "#8E877D"
  
  # 🖤 Extended Obsidian & Void Black Surfaces
  void-black: "#090A0D"        # Foundational Background
  neutral-abyss: "#090A0D"
  neutral-deep: "#111318"      # Deep Obsidian
  neutral-surface: "#181A20"   # Obsidian (Primary Cards / Panels)
  neutral-raised: "#292C33"    # Soft Obsidian (Inputs / Hover)
  
  # Accents & Borders
  status-online: "#10b981"
  border-subtle: "rgba(244, 239, 230, 0.10)"
  border-gold: "rgba(201, 164, 92, 0.30)"
typography:
  display:
    fontFamily: "Cinzel, Georgia, serif"
    fontSize: "clamp(1.9rem, 3.3vw, 2.85rem)"
    fontWeight: 800
    lineHeight: 1.25
    letterSpacing: "0.01em"
  headline:
    fontFamily: "Cinzel, Georgia, serif"
    fontSize: "1.5rem"
    fontWeight: 700
    lineHeight: 1.3
    letterSpacing: "0.02em"
  title:
    fontFamily: "Plus Jakarta Sans, -apple-system, BlinkMacSystemFont, sans-serif"
    fontSize: "1.15rem"
    fontWeight: 700
    lineHeight: 1.4
    letterSpacing: "-0.01em"
  body:
    fontFamily: "Plus Jakarta Sans, -apple-system, BlinkMacSystemFont, sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.65
    letterSpacing: "normal"
  label:
    fontFamily: "Cinzel, Georgia, serif"
    fontSize: "0.72rem"
    fontWeight: 700
    lineHeight: 1
    letterSpacing: "0.25em"
rounded:
  xs: "2px"
  sm: "4px"
  md: "6px"
  lg: "8px"
spacing:
  xs: "4px"
  sm: "8px"
  md: "16px"
  lg: "24px"
  xl: "32px"
  2xl: "48px"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "#05070a"
    rounded: "{rounded.sm}"
    padding: "12px 26px"
  button-primary-hover:
    backgroundColor: "{colors.primary-light}"
    textColor: "#05070a"
    rounded: "{rounded.sm}"
    padding: "12px 26px"
  button-outline:
    backgroundColor: "rgba(255, 255, 255, 0.04)"
    textColor: "{colors.text-main}"
    rounded: "{rounded.sm}"
    padding: "12px 24px"
  button-outline-hover:
    backgroundColor: "rgba(212, 163, 89, 0.12)"
    textColor: "{colors.primary-light}"
    rounded: "{rounded.sm}"
    padding: "12px 24px"
  card-monolith:
    backgroundColor: "{colors.neutral-surface}"
    textColor: "{colors.text-main}"
    rounded: "{rounded.md}"
    padding: "24px"
  input-field:
    backgroundColor: "{colors.neutral-input}"
    textColor: "{colors.text-main}"
    rounded: "{rounded.sm}"
    padding: "12px 16px"
---

# Design System: Apexsions

## Overview

**Creative North Star: "The Monolithic Obsidian Citadel"**

Apexsions exists as the architectural zenith of civilization — unyielding, monumental, and ancient. Every surface is sculpted from abyssal volcanic obsidian layered over dark starry voids, bonded by precise veins of forged royal gold. The interface does not emulate flashy modern software or saturated casual gaming hubs; it commands solemn reverence through restraint, symmetry, and architectural permanence.

The visual experience balances dark monolithic weight with high-contrast legibility. Content floats on stratified obsidian planes (`#090A0D` Void Black to `#181A20` Obsidian and `#292C33` Soft Obsidian), illuminated by disciplined hairline gold borders (`rgba(201, 164, 92, 0.30)`) and celestial ivory typography designed for effortless scanning.

**Key Characteristics:**
- **Solemn Monumentality:** Void black and deep obsidian tones, generous breathing room, and architectural discipline replace chaotic neon clutter.
- **Imperial Restraint:** Royal gold is treated as an imperial privilege, deployed strictly for sovereign actions, hierarchy progression, and active focus.
- **Architectural Geometry:** Crisp 2px to 6px radii create chisel-cut stone planes rather than soft pillowy bubbles.
- **Uncompromised Contrast:** Celestial Ivory (`#F4EFE6`, >17:1 ratio) and marble tones preserve strict WCAG AAA readability against midnight backdrops.

## Colors

The official extended palette is anchored in abyssal obsidian layers, accented by imperial gold and mythic telemetry emerald.

### ✨ Extended Gold Hierarchy
- **Radiant Gold** (`#F1D58A`): Luminous high-tier glow and apex achievement badges.
- **Light Gold** (`#E8C878`): Hover highlights, active focus states, and warm forge glow.
- **Royal Gold** (`#C9A45C` — *Primary Accent*): Reserved for sovereign actions, active tab indicators, rank crest highlights, and primary CTAs. Signifies authority and civilizational achievement.
- **Antique Gold** (`#9E7B3E`): Deep shadow and gradient base foundation for metallic gold buttons.
- **Deep Gold** (`#72552A`): Deep structural shadow and warm contrast border.

### 🤍 Extended Celestial Ivory Typography (WCAG AAA > 17:1)
- **Ivory Light** (`#FFFDF8`): Maximum brightness highlight for apex elements.
- **Celestial Ivory** (`#F4EFE6` — *Primary Text*): Highest-tier headings, brand titles, and active navigation links. Replaces sterile pure white with warm ancient marble elegance.
- **Marble** (`#DDD7CC`): Narrative body copy and primary descriptions (14:1 contrast ratio).
- **Stone** (`#B8B1A5`): Secondary metadata, timestamps, and inactive nav items (8.5:1 contrast ratio).
- **Dim Telemetry** (`#8E877D`): Kickers, sub-labels, axis markers, and table metadata (6:1 contrast ratio).

### 🖤 Extended Obsidian & Void Black Architecture
- **Void Black** (`#090A0D` — *Background Utama*): Deepest void backdrop and global body foundation.
- **Deep Obsidian** (`#111318`): Deep container substrates, input chasms, and elevated base layers.
- **Obsidian** (`#181A20` — *Primary Surface*): Primary container, card body, and panel backgrounds.
- **Soft Obsidian** (`#292C33`): Raised interactive elements, hover states, and elevated modal headers.

### Status & Telemetry
- **Mythic Emerald** (`#10b981`): Real-time online telemetry, operational cluster indicators, and live ping pulses. Represents the thriving life of the civilization.

### Named Rules
**The Imperial Rarity Rule.** Royal gold is an imperial privilege; it must never coat more than 10% of any viewport. If everything glows gold, nothing feels sovereign.

**The Celestial Warmth Rule.** Continuous body copy and headings use the Celestial Ivory & Marble system (`#F4EFE6` / `#DDD7CC`), preserving comfortable optical warmth while maintaining full WCAG AAA compliance.

## Typography

**Display Font:** Cinzel (with Georgia, serif fallback)  
**Body Font:** Plus Jakarta Sans (with -apple-system, BlinkMacSystemFont, Roboto, sans-serif fallback)  
**Code / Telemetry Font:** JetBrains Mono (with Consolas, monospace fallback)

**Character:** The ancient gravitas of chisel-carved imperial Latin inscriptions (Cinzel) paired with the clean, razor-sharp technical precision of contemporary geometric sans-serif (Plus Jakarta Sans).

### Hierarchy
- **Display** (weight 800, `clamp(1.9rem, 3.3vw, 2.85rem)`, line-height 1.25): Monumental hero titles, apex brand headers, and chapter proclamations.
- **Headline** (weight 700, 1.5rem, line-height 1.3): Section headers, modal titles, and caste tier designations.
- **Title** (weight 700, 1.15rem, line-height 1.4): Card names, package titles, and table group headings.
- **Body** (weight 400/500, 1rem (16px), line-height 1.65): Narrative lore, gameplay explanations, and wiki documentation. Kept to max line length 70ch.
- **Label** (weight 700, 0.72rem, letter-spacing 0.25em, uppercase): Telemetry kickers, category badges, and caste hierarchy milestones.

### Named Rules
**The Roman Inscription Rule.** All kicker badges, category headers, and brand marks must be set in uppercase Cinzel with wide letter-spacing (`0.18em` to `0.28em`).

**The No-Script Rule.** Script fonts, handwritten cursive, or playful rounded comic typefaces are strictly prohibited across all surfaces.

## Layout

The spatial model relies on a symmetrical 12-column architectural grid with generous vertical intervals (48px to 96px) between major monoliths.

- **Container Bounds:** Fixed at `1280px` maximum width with fluid lateral gutters (`clamp(16px, 4vw, 32px)`) to guarantee responsive containment across all viewports.
- **Vertical Rhythm:** 64px to 96px section padding establishes cathedral-like breathing space; compact intra-card padding (16px to 24px) retains density where information needs to be scanned quickly.
- **Mobile Reflow:** Cards and ladder tiers stack vertically below 992px (`lg`), with sticky floating navigation collapsing gracefully into a full-bleed glass drawer.

## Elevation & Depth

Apexsions avoids diffuse drop shadows and faux-3D bevels in favor of architectural obsidian layering. Depth is communicated through tonal luminance shifts and chiseled hairline borders.

### Shadow Vocabulary
- **Monolithic Base Card** (`box-shadow: 0 10px 28px -6px rgba(0, 0, 0, 0.7)`): Structural downward shadow anchoring cards into the viewport floor.
- **Elevated Citadel Modal** (`box-shadow: 0 18px 40px -8px rgba(0, 0, 0, 0.85)`): Deep separation shadow for modals, floating drawers, and inspection overlays.
- **Sovereign Action Halos** (`box-shadow: 0 4px 14px rgba(212, 163, 89, 0.2)`): Tight, subtle amber-gold halo restricted to active gold buttons.

### Named Rules
**The Chiseled Edge Rule.** Cards and containers must define their boundaries through 1px hairline borders (`rgba(212, 163, 89, 0.14)` or `rgba(255, 255, 255, 0.12)`) rather than heavy blurred drop shadows alone.

## Shapes

- **Form Language:** Chisel-cut stone slabs. Sharp, authoritative corners dominate.
- **Corner Radii:**
  - Micro tags, status chips, code pills: `2px` (`--apx-radius-xs`)
  - Buttons, inputs, user badges: `4px` (`--apx-radius-sm`)
  - Cards, panels, list containers: `6px` (`--apx-radius-md`)
  - Large modals, hero banners: `8px` (`--apx-radius-lg`)
- **Prohibition:** Fully pill-shaped (`50px` / `9999px`) rounded bubbles are forbidden except for live status pulse dots (`8px` circular dots).

## Components

### Buttons
- **Shape:** Architectural rectangle with subtle `4px` radius (`--apx-radius-sm`). Minimum height 44px for effortless touch interaction.
- **Primary Sovereign Action (`btn-apx-sovereign`, `btn-apx-gold`):** Forged noble gold gradient (`linear-gradient(135deg, #9e7534 0%, #d4a359 100%)`), deep dark text (`#07090e`), bold Cinzel font, 1px gold outline.
- **Hover / Focus:** Transitions to brighter gold (`#f3c37a`), elevates slightly, and projects an amber-gold halo (`0 6px 20px rgba(212, 163, 89, 0.35)`).
- **Secondary Ghost Action (`btn-apx-outline`):** Translucent obsidian glass (`rgba(255, 255, 255, 0.04)`), white text, 1px subtle border (`rgba(255, 255, 255, 0.12)`). Hovers to gold hairline with subtle fill.

### Cards / Containers
- **Corner Style:** `6px` radius (`--apx-radius-md`).
- **Background:** Monolithic obsidian surface (`#0f1523`).
- **Border:** 1px hairline gold border (`rgba(212, 163, 89, 0.14)`).
- **Internal Padding:** `24px` (`1.5rem`).
- **Hover:** Shifts border to `rgba(212, 163, 89, 0.45)`, background lightens to `#19243a`, with seamless 250ms cubic-bezier transition.

### Inputs / Fields
- **Style:** Deep chasm background (`#0b101b`), 1px white border (`rgba(255, 255, 255, 0.16)`), `4px` radius, white text.
- **Focus:** Border snaps to Imperial Gold (`#d4a359`) with a 3px amber halo (`rgba(245, 158, 11, 0.25)`).

### Navigation
- **Header:** Sticky obsidian glass (`rgba(6, 8, 13, 0.98)` with 16px backdrop-filter blur).
- **Brand Mark:** Crest shield alongside uppercase Cinzel title with 0.16em letter-spacing.
- **Links:** Crisp slate typography (`#cbd5e1`), hovering to pure white. Active state displays crisp white typography with a 16px wide, 2px thick white hairline indicator directly below.

### Signature Components
- **The Living Caste Ladder (`.apx-social-ladder`):** Tier-by-tier physical hierarchy cards from Wanderer to Ancestor, featuring rank weight badges, privilege lists, and gilded tier markers.
- **Live Telemetry Strip (`.apx-hero-infrastructure`):** Horizontal telemetry metadata bar showing server operational state, live player count, and interactive copy-on-click IP pill with heartbeat emerald dot.

## Do's and Don'ts

### Do:
- **Do** maintain a minimum 4.5:1 WCAG contrast ratio for all secondary and body text against dark backgrounds.
- **Do** use the official brand name **Apexsions** and tagline **The Peak Civilizations** exclusively.
- **Do** preserve the chisel-cut architectural form language with 2px to 6px radii across all UI components.
- **Do** pair uppercase Cinzel display headers with Plus Jakarta Sans body typography.
- **Do** provide immediate visual feedback (such as "COPIED" badge) when interactive telemetry elements like the server IP are clicked.

### Don't:
- **Don't** add suffixes like "Kingdom", "Network", or "SMP" to the Apexsions name.
- **Don't** use neon purple, cyan, or rainbow gradient text effects.
- **Don't** use pill-shaped (`border-radius: 9999px`) buttons or cards.
- **Don't** use bouncy, cartoonish spring animations (keep transitions to smooth 200–250ms cubic-beziers).
- **Don't** flood entire container backgrounds with gold; gold is strictly an accent and sovereign marker.

---

## Official Visual Asset Archive

Arsip aset grafis resmi server dan web Apexsions tersimpan di `Website/themes/apexsions/assets/img/`:

| Nama Berkas | Tipe | Deskripsi & Rekomendasi Penggunaan |
| :--- | :---: | :--- |
| `logo-ornate-gold.png` | PNG | Tipografi 3D emas berukir bintang transparan. Digunakan untuk master navbar logo, preloader transition, dan footer. |
| `banner-celestial-night.png` | PNG | Citadel istana langit malam dengan cincin orbit kosmik dan logo utama. Digunakan untuk OpenGraph social preview (`og:image`, `twitter:image`). |
| `banner-celestial-panoramic.png` | PNG | Banner ultrawide air terjun awan surga. Digunakan untuk hero panoramic background dan header sub-halaman. |
| `crest-celestial-citadel.png` | PNG | Segel lambang katedral melayang dengan cincin orbital. Digunakan untuk favicon browser, segel login/register, dan watermark hierarki. |
| `logo-monolith-horizontal.png` | PNG | Logo monolitik horizontal huruf "A" bersayap dengan watermark istana. Digunakan untuk header dokumen wiki, invoice, dan checkout. |

