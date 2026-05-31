# The Cobblemon Initiative

A Fabric mod for Minecraft 1.21.1 that adds structured progression, Nuzlocke-style challenge rules, and NPC line-of-sight detection on top of [Cobblemon](https://cobblemon.com).

---

## Features

### Gym Badge Progression & Level Caps

Defeat gym leaders to unlock higher Pokémon level caps. Progress is tracked per player and persists across sessions.

| Badge | Gym | Level Cap |
|-------|-----|-----------|
| Grass | Hua Zhan City | 25 |
| Bug | Takehara Falls | 30 |
| Fairy | Mystic Marsh | 35 |
| Fighting | Deepcore City | 40 |
| Water | Gaviota Port | 45 |
| Ground | Kalahar Reach | 50 |
| Electric | Cyber City | 55 |
| Dragon | Ryujin Keep | 60 |
| Ice | Nifl Town | 65 |
| Fire | Scorchspire | 70 |
| — | Royal League Champion | 100 |

Shrine battles (Fire, Ground, Ice, Dragon, Fairy) and Royal League / Battle Frontier / Villain Team encounters are also tracked.

### Nuzlocke Death Mechanics

Optional Nuzlocke-style rules applied when Pokémon faint in battle:

- **Player damage** when a Pokémon faints (scales with party size or uses max health)
- **Permanent release** of fainted Pokémon
- **Sacrifice on flee** — must release a Pokémon when escaping a battle
- **Mystery sacrifice** — Pokémon names obfuscated when choosing who to release
- **Duplicate handling** — automatically release species already owned or ever caught
- **Safe zones** — configurable world regions where mechanics are suspended

All rules are individually toggleable in the Mod Menu config screen.

### NPC Line-of-Sight Detection

Server-side sight detection for [Easy NPC](https://www.curseforge.com/minecraft/mc-mods/easy-npc) entities, replacing the old datapack implementation.

**Registering an NPC:**
```
/npcsight add <uuid> [stationary|tracking] [range] [dialog]
```
When typing the UUID, tab-complete suggests the entity you are currently looking at.

**Other commands:**
```
/npcsight remove <uuid>
/npcsight mode <uuid> <stationary|tracking>
/npcsight range <uuid> <blocks>
/npcsight dialog <uuid> <name|clear>
/npcsight list
/npcsight info <uuid>
/npcsight reload
```

**Modes:**
- `stationary` — NPC looks in a fixed direction; detects players within its forward 120° field of view
- `tracking` — NPC head rotates to follow the nearest player in range

**Active mode:** set a dialog name to have the NPC open an Easy NPC dialog when it sees a player within close range. Easy NPC is a soft dependency — the mod works without it, dialog triggers are simply skipped.

**Scoreboard interop:** the `can_see_player` scoreboard objective is updated each cycle so commands and other datapacks can check `@e[scores={can_see_player=1}]`.

**Mod Menu config (NPC Sight category):**
- Default sight range (1–256 blocks)
- Debug mode — colored dust particles along raycasts (green = clear, red = blocked)
- Dialog trigger range

---

## Requirements

- Minecraft 1.21.1
- [Fabric Loader](https://fabricmc.net) 0.16+
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Cobblemon](https://cobblemon.com) 1.7.0+

**Optional / recommended:**
- [Mod Menu](https://modrinth.com/mod/modmenu) — in-game config screen
- [Cloth Config](https://modrinth.com/mod/cloth-config) — required for the config screen
- [Radical Cobblemon Trainers API](https://www.curseforge.com/minecraft/mc-mods/radical-cobblemon-trainers) — trainer spawning
- [Easy NPC](https://www.curseforge.com/minecraft/mc-mods/easy-npc) — NPC dialog integration

---

## Building

```bash
gradle build
```

Output jar is placed in `build/libs/`.

---

## AI Assistance Disclaimer

Development of this mod was assisted by [Claude Code](https://claude.ai/claude-code) (Anthropic). AI was used to help design, implement, and iterate on mod features — including the NPC sight system, Nuzlocke mechanics, and config integration.

All generated code was reviewed, tested, and intentionally integrated by the developer. AI assistance does not imply that the code is untested or unreviewed — it is a development tool, not a replacement for judgment.

---

*The Cobblemon Initiative is not affiliated with Cobblemon, Mojang, or Nintendo.*
