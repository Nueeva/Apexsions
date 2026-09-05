# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

- **Prospective & Existing Players:** Gamers seeking a high-stakes, civilizational Minecraft experience. They visit to copy the server IP, check live server status, read kingdom rules, understand rank perks, and browse the community wiki.
- **Supporters & Donors:** Active community members seeking to upgrade their in-game prestige and support the server through the purchase of ranks (Ascendant through Sions), battlepasses, and keys.
- **Staff & Guild Leaders:** Server administrators, moderators, and kingdom rulers monitoring community reports, event schedules, and network announcements.

## Product Purpose

Serve as the digital citadel and all-in-one web portal for the Apexsions ecosystem. It exists to onboard new players seamlessly, showcase live network activity, document civilizational mechanics (Kingdoms, Atomic Multi-Currency Economy, Battlepass, Custom Media), and provide a secure, prestigious store experience.

## Positioning

"The Peak Civilizations" — Apexsions is not a generic casual SMP. It is an unapologetically monolithic, hardcore medieval-mythic civilization server where nine distinct rank tiers, player-driven kingdoms, and atomic economic systems converge. The web presence reflects this unyielding prestige through an ancient obsidian and noble gold aesthetic.

## Operating Context

Accessed via desktop and mobile web browsers by players before launching Minecraft, during active play sessions, or while engaging with community hubs on Discord. Key actions include:
- One-click server IP copy (`play.apexsions.com`)
- Real-time online player count and ping status check
- Exploration of in-game rank caste cards and progression requirements
- Seamless navigation between the Home Portal, Store, Wiki, and Community Discord

## Capabilities and Constraints

- **Platform Architecture:** Built on the Azuriom CMS platform utilizing Laravel Blade templates, custom CSS stylesheets, and responsive vanilla JavaScript.
- **Live Integration:** Connected to the Paper 1.21.4 backend cluster for real-time status query and player authentication.
- **Hierarchy Structure:** Authoritative 9-tier rank system (`ancestor` [100], `warden` [90], `herald` [80], `sions` [70], `emperor` [60], `sovereign` [50], `archon` [40], `ascendant` [30], `wanderer` [10]).
- **Workflow Path:** Configured as `comp-first` (visual compositions and aesthetic alignment precede code modifications).

## Brand Commitments

- **Official Name:** Exclusively **Apexsions**. Suffixes such as "Kingdom", "Network", or "SMP" are strictly forbidden.
- **Tagline:** **The Peak Civilizations**.
- **Visual Tone:** Monolithic Obsidian & Noble Gold. Deep cosmic blacks (`#08080c`, `#0e0e15`), lustrous brushed golds (`#c89b3c`, `#dfb75c`), subtle gold halos, glassmorphic card borders, and ancient classical typography (Cinzel headers paired with Outfit body text).

## Evidence on Hand

- Core server rank hierarchy defined in [`ranks.yml`](file:///c:/Users/Friel/Documents/Rifqi%20Ariansyah/Apexsions/ranks.yml).
- Active customized theme codebase in [`Website/themes/apexsions/`](file:///c:/Users/Friel/Documents/Rifqi%20Ariansyah/Apexsions/Website/themes/apexsions/).
- Complete database seeders reflecting all six core Minecraft plugins in [`Website/database/seed_minecraft_systems.php`](file:///c:/Users/Friel/Documents/Rifqi%20Ariansyah/Apexsions/Website/database/seed_minecraft_systems.php).
- Live server deployment running at `http://89.144.53.100/` and local development server at `http://127.0.0.1:8000/`.

## Product Principles

1. **Atmosphere of Grandeur:** Every page and component must evoke the majesty of an ancient civilization at its peak. Avoid playful, generic, or cartoonish gaming tropes.
2. **Instant Player Onboarding:** The journey from visiting the website to joining the Minecraft server must take fewer than five seconds (prominent server IP, instant copy action, live server ping).
3. **Fidelity to In-Game Systems:** Web content, rank cards, and market representations must faithfully match the true Minecraft plugin mechanics rather than placeholder descriptions.
4. **Prestige & Progression:** Rank tiers must visually radiate their weight and prestige, giving players clear aspirational goals within the civilization.

## Accessibility & Inclusion

- Color contrast between gold accents, body text, and dark obsidian backdrops must achieve WCAG 2.1 AA minimums (4.5:1 for normal text, 3:1 for large display headers).
- Interactive tap targets (buttons, links, copy triggers) must maintain a minimum bounding box of 44×44px for effortless mobile touch interaction.
