# The Cobblemon Initiative

A single-player **Fabric** mod for **Minecraft 1.21.1** that layers structured gym-badge
progression, level caps, Nuzlocke-style death mechanics, an amnesiac-founder storyline, a
destabilizing in-world economy, and elemental shrine challenges on top of
[Cobblemon](https://cobblemon.com).

It is built exclusively for **[UPM 2](https://store.varunabuilds.com/p/upm-2/)** by Varuna
Studios, played as a curated **hardcore + Nuzlocke** campaign — see the disclaimer below.

## At a glance

- 🏅 Gym-badge progression with **earned level caps** across ten gyms, a Royal League, and a Battle Frontier.
- 💀 **Hardcore + Nuzlocke** death mechanics — faint damage, flee-sacrifice, and a custom Pokéball death screen.
- 🧠 An **amnesiac-founder storyline** told through badge-gated memory fragments and an intrusive "Dark Urge."
- 💱 A **destabilizing CobbleDollar economy** and a three-act villain arc against The Company, Inc.
- 👁️ NPC **line-of-sight** detection, five elemental **shrine** trials, and an on-screen **objective HUD**.

## 📖 Documentation

Full documentation lives in the
**[Wiki](https://github.com/The-Company-Inc-Nerds/the-cobblemon-initiative/wiki)**:

- **[Guidebook](https://github.com/The-Company-Inc-Nerds/the-cobblemon-initiative/wiki/Guidebook-Overview)** — the campaign by town/route/shrine, plus a sequential **[Route Map](https://github.com/The-Company-Inc-Nerds/the-cobblemon-initiative/wiki/Guidebook-Route-Map)**.
- **[Architecture](https://github.com/The-Company-Inc-Nerds/the-cobblemon-initiative/wiki/Architecture-Overview)** — subsystems and mermaid data-flow diagrams.
- **[Commands](https://github.com/The-Company-Inc-Nerds/the-cobblemon-initiative/wiki/Commands)** — the complete command reference.

## Requirements

- Minecraft 1.21.1 · [Fabric Loader](https://fabricmc.net) 0.16+ · [Fabric API](https://modrinth.com/mod/fabric-api) · [Cobblemon](https://cobblemon.com) 1.7.0+

**Recommended (runtime integrations, not hard dependencies):** [Radical Cobblemon Trainers](https://www.curseforge.com/minecraft/mc-mods/radical-cobblemon-trainers), [Easy NPC](https://www.curseforge.com/minecraft/mc-mods/easy-npc), [CobbleDollars](https://modrinth.com/mod/cobbledollars), [JourneyMap](https://modrinth.com/mod/journeymap), [Mod Menu](https://modrinth.com/mod/modmenu) + [Cloth Config](https://modrinth.com/mod/cloth-config).

## Building

The Nix dev shell provides JDK 21 + Gradle:

```bash
nix develop        # or: direnv allow
gradle build       # output jar -> build/libs/
```

## Disclaimers

### UPM 2 — Varuna Studios

The Cobblemon Initiative is an **independent, unofficial** companion mod built exclusively for
**[UPM 2 (Ultimate Pokémon Map 2)](https://store.varunabuilds.com/p/upm-2/)**, a hand-crafted
Minecraft world by [**Varuna Studios**](https://varunabuilds.com). The map must be obtained
from Varuna Studios separately; this mod ships none of their content.

> **This project is not affiliated with, endorsed by, approved by, or sponsored by Varuna
> Studios.** "UPM 2" / "Ultimate Pokémon Map" and all related names and assets are the
> property of Varuna Studios.

### Trademarks

Not affiliated with or endorsed by Cobblemon, Mojang, Nintendo, Game Freak, or The Pokémon
Company. Pokémon and all related names are trademarks of their respective owners.

### AI Assistance

Development was assisted by [Claude Code](https://claude.com/claude-code) (Anthropic). All
generated code and content was reviewed, tested, and intentionally integrated by the
developer — AI is a tool here, not a substitute for judgment.
