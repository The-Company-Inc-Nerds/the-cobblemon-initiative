# The Cobblemon Initiative

A Fabric mod for Minecraft 1.21.1 that adds structured progression, Nuzlocke-style challenge rules, and NPC line-of-sight detection on top of [Cobblemon](https://cobblemon.com).

Built for the **UPM Custom Map** by [Varuna Studios](https://varunabuilds.com) — a hand-crafted open world with distinct biome regions, purpose-built towns, and a continuous overworld adventure loop.

---

## World Overview

The adventure begins at **Sango Town** and travels across ten gym towns before culminating at the Royal League. All coordinates are given as X Y Z.

### Towns & Locations

| #   | Town           | Biome         | Coordinates   | Notes                 |
| --- | -------------- | ------------- | ------------- | --------------------- |
| —   | Sango Town     | Savanna       | 2573 105 2900 | Spawn town            |
| 1   | Takehara Falls | Jungle        | 1898 109 2524 | Bug Gym               |
| 2   | Hua Zhan City  | Cherry Grove  | 1489 86 2053  | Grass Gym             |
| 3   | Mystic Marsh   | Swamp         | 1061 65 2441  | Fairy Gym             |
| 4   | Deepcore City  | Badlands      | 1033 129 3186 | Fighting Gym          |
| 5   | Gaviota Port   | Coast         | 612 82 3536   | Water Gym             |
| 6   | Kalahar Reach  | Desert        | 2073 126 4050 | Ground Gym            |
| 7   | Cyber City     | Plains        | 1450 89 1185  | Electric Gym          |
| 8   | Ryujin Keep    | Taiga         | 2144 201 884  | Dragon Gym            |
| 9   | Nifl Town      | Jagged Peaks  | 3596 112 2031 | Ice Gym               |
| 10  | Scorchspire    | Nether Wastes | 3688 100 4511 | Fire Gym              |
| —   | Royal League   | Taiga         | 3528 166 2773 | Elite Four + Champion |

### Other Key Locations

| Location         | Coordinates   | Notes                   |
| ---------------- | ------------- | ----------------------- |
| Battlebay Port   | 3800 159 2979 | Battle Frontier hub     |
| The Daycare      | 567 84 3491   | Pokémon breeding        |
| Safari Zone      | 1511 78 1617  | Rare encounters         |
| Team Rocket Base | 1590 51 1028  | Villain team stronghold |

### Routes

| Route             | From → To                      | Terrain                                      |
| ----------------- | ------------------------------ | -------------------------------------------- |
| Verdant Trail     | Sango Town → Takehara Falls    | Savanna into jungle canopy                   |
| Blossom Path      | Takehara Falls → Hua Zhan City | Jungle to cherry grove highlands             |
| Misty Passage     | Hua Zhan City → Mystic Marsh   | Forest into wetlands                         |
| Badlands Crossing | Mystic Marsh → Deepcore City   | Swamp into canyon badlands                   |
| Cliffside Road    | Deepcore City → Gaviota Port   | Badlands cliffs to coastal harbor            |
| Scorched Expanse  | Gaviota Port → Kalahar Reach   | Coast to arid desert interior                |
| Northern Highway  | Kalahar Reach → Cyber City     | Desert to open plains                        |
| Taiga Trek        | Cyber City → Ryujin Keep       | Plains into frozen taiga peaks               |
| Frostspine Pass   | Ryujin Keep → Nifl Town        | Taiga through jagged icy ridges              |
| Ember Road        | Nifl Town → Scorchspire        | Ice peaks descending to volcanic wastes      |
| Champion's Ascent | Scorchspire → Royal League     | Nether wastes into highland taiga stronghold |

---

## Gym Badge Progression & Level Caps

Defeat gym leaders to unlock higher Pokémon level caps. Progress is tracked per player and persists across sessions. Each gym requires defeating at least two trainers inside before you can challenge the Jr. Apprentice, then the Apprentice (doubles battle), then the Leader.

| Badge # | Type     | Leader  | Town                  | Level Cap |
| ------- | -------- | ------- | --------------------- | --------- |
| 1       | Bug      | Cicada  | Takehara Falls        | 20        |
| 2       | Grass    | Blossom | Hua Zhan City         | 30        |
| 3       | Fairy    | Titania | Mystic Marsh          | 38        |
| 4       | Fighting | Bruno   | Deepcore City         | 45        |
| 5       | Water    | Neptune | Gaviota Port          | 52        |
| 6       | Ground   | Gaia    | Kalahar Reach         | 58        |
| 7       | Electric | Volt    | Cyber City            | 63        |
| 8       | Dragon   | Ryujin  | Ryujin Keep           | 68        |
| 9       | Ice      | Boreas  | Nifl Town             | 73        |
| 10      | Fire     | Vulcan  | Scorchspire           | 78        |
| —       | —        | —       | Royal League Champion | 100       |

### Gym Leader Teams

<details>
<summary>Badge 1 — Leader Cicada (Bug · Takehara Falls)</summary>

| Pokémon   | Level | Notable Moves                                     |
| --------- | ----- | ------------------------------------------------- |
| Scolipede | 17    | Megahorn, Poison Jab, Swords Dance, Protect       |
| Heracross | 17    | Megahorn, Close Combat, Night Slash, Swords Dance |
| Vespiquen | 18    | Attack Order, Defend Order, Heal Order, Toxic     |
| Yanmega   | 18    | Bug Buzz, Air Slash, Protect, Ancient Power       |

</details>

<details>
<summary>Badge 2 — Leader Blossom (Grass · Hua Zhan City)</summary>

| Pokémon  | Level | Notable Moves                                     |
| -------- | ----- | ------------------------------------------------- |
| Tropius  | 27    | Razor Leaf, Synthesis, Dragon Dance, Air Cutter   |
| Leafeon  | 27    | Leaf Blade, Quick Attack, Swords Dance, Synthesis |
| Roserade | 28    | Sludge Bomb, Giga Drain, Sleep Powder, Spikes     |
| Venusaur | 29    | Solar Beam, Sludge Bomb, Sleep Powder, Synthesis  |

</details>

<details>
<summary>Badge 3 — Leader Titania (Fairy · Mystic Marsh)</summary>

| Pokémon   | Level | Notable Moves                                   |
| --------- | ----- | ----------------------------------------------- |
| Sylveon   | 34    | Moonblast, Shadow Ball, Calm Mind, Hyper Voice  |
| Gardevoir | 35    | Moonblast, Psychic, Shadow Ball, Calm Mind      |
| Togekiss  | 35    | Air Slash, Dazzling Gleam, Nasty Plot, Roost    |
| Clefable  | 36    | Moonblast, Soft-Boiled, Calm Mind, Thunder Wave |

</details>

<details>
<summary>Badge 4 — Leader Bruno (Fighting · Deepcore City)</summary>

| Pokémon    | Level | Notable Moves                                      |
| ---------- | ----- | -------------------------------------------------- |
| Breloom    | 41    | Mach Punch, Bullet Seed, Spore, Swords Dance       |
| Machamp    | 42    | Dynamic Punch, Knock Off, Bullet Punch, Ice Punch  |
| Lucario    | 42    | Aura Sphere, Flash Cannon, Nasty Plot, Vacuum Wave |
| Conkeldurr | 43    | Drain Punch, Mach Punch, Knock Off, Ice Punch      |

</details>

<details>
<summary>Badge 5 — Leader Neptune (Water · Gaviota Port)</summary>

| Pokémon  | Level | Notable Moves                                     |
| -------- | ----- | ------------------------------------------------- |
| Cloyster | 47    | Icicle Spear, Rock Blast, Shell Smash, Hydro Pump |
| Gyarados | 47    | Waterfall, Dragon Dance, Crunch, Earthquake       |
| Milotic  | 48    | Scald, Ice Beam, Recover, Toxic                   |
| Kingdra  | 49    | Hydro Pump, Draco Meteor, Ice Beam, Focus Energy  |

</details>

<details>
<summary>Badge 6 — Leader Gaia (Ground · Kalahar Reach)</summary>

| Pokémon   | Level | Notable Moves                                     |
| --------- | ----- | ------------------------------------------------- |
| Hippowdon | 52    | Earthquake, Stealth Rock, Slack Off, Whirlwind    |
| Excadrill | 53    | Earthquake, Iron Head, Rock Slide, Swords Dance   |
| Rhyperior | 53    | Earthquake, Stone Edge, Ice Punch, Megahorn       |
| Garchomp  | 54    | Earthquake, Dragon Claw, Stone Edge, Swords Dance |

</details>

<details>
<summary>Badge 7 — Leader Volt (Electric · Cyber City)</summary>

| Pokémon    | Level | Notable Moves                                        |
| ---------- | ----- | ---------------------------------------------------- |
| Magnezone  | 56    | Thunderbolt, Flash Cannon, Volt Switch, Thunder Wave |
| Jolteon    | 57    | Thunderbolt, Shadow Ball, Volt Switch, Quick Attack  |
| Electivire | 57    | Wild Charge, Ice Punch, Earthquake, Cross Chop       |
| Raichu     | 58    | Thunderbolt, Focus Blast, Nasty Plot, Grass Knot     |

</details>

<details>
<summary>Badge 8 — Leader Ryujin (Dragon · Ryujin Keep)</summary>

| Pokémon   | Level | Notable Moves                                    |
| --------- | ----- | ------------------------------------------------ |
| Haxorus   | 60    | Outrage, Earthquake, Poison Jab, Swords Dance    |
| Salamence | 61    | Outrage, Earthquake, Fire Blast, Dragon Dance    |
| Dragonite | 61    | Outrage, Extreme Speed, Earthquake, Dragon Dance |
| Garchomp  | 62    | Earthquake, Outrage, Stone Edge, Swords Dance    |

</details>

<details>
<summary>Badge 9 — Leader Boreas (Ice · Nifl Town)</summary>

| Pokémon   | Level | Notable Moves                                     |
| --------- | ----- | ------------------------------------------------- |
| Weavile   | 64    | Icicle Crash, Knock Off, Low Kick, Ice Shard      |
| Glaceon   | 64    | Blizzard, Shadow Ball, Water Pulse, Hail          |
| Mamoswine | 65    | Earthquake, Icicle Crash, Ice Shard, Stealth Rock |
| Froslass  | 65    | Blizzard, Shadow Ball, Spikes, Destiny Bond       |
| Aurorus   | 66    | Blizzard, Thunder, Aurora Veil, Ancient Power     |

</details>

<details>
<summary>Badge 10 — Leader Vulcan (Fire · Scorchspire)</summary>

| Pokémon   | Level | Notable Moves                                         |
| --------- | ----- | ----------------------------------------------------- |
| Arcanine  | 68    | Flare Blitz, Extreme Speed, Wild Charge, Close Combat |
| Charizard | 68    | Fire Blast, Air Slash, Solar Beam, Focus Blast        |
| Heatran   | 69    | Magma Storm, Earth Power, Flash Cannon, Stealth Rock  |
| Volcarona | 70    | Fiery Dance, Bug Buzz, Quiver Dance, Giga Drain       |

</details>

---

## Royal League

Located in the Taiga highlands at **3528 166 2773**. Requires all 10 gym badges.

### Elite Four

| Member | Specialty       | Ace       | Levels |
| ------ | --------------- | --------- | ------ |
| Aria   | Psychic / Ghost | Metagross | 72–74  |
| Marcus | Rock            | Rhyperior | 74–76  |
| Luna   | Dark            | Hydreigon | 76–78  |
| Drake  | Dragon          | Haxorus   | 78–80  |

<details>
<summary>Elite Four Teams</summary>

**Aria** — Gengar, Alakazam, Espeon, Metagross

**Marcus** — Tyranitar, Aggron, Aerodactyl, Rhyperior

**Luna** — Umbreon, Honchkrow, Weavile, Hydreigon

**Drake** — Dragonite, Salamence, Garchomp, Haxorus

</details>

### Champion Cynthia

| Pokémon   | Level | Notable Moves                                      |
| --------- | ----- | -------------------------------------------------- |
| Spiritomb | 82    | Dark Pulse, Shadow Ball, Will-O-Wisp, Calm Mind    |
| Lucario   | 82    | Aura Sphere, Flash Cannon, Nasty Plot, Vacuum Wave |
| Togekiss  | 83    | Air Slash, Dazzling Gleam, Nasty Plot, Roost       |
| Milotic   | 83    | Scald, Ice Beam, Recover, Toxic                    |
| Roserade  | 84    | Sludge Bomb, Giga Drain, Spikes, Sleep Powder      |
| Garchomp  | 85    | Earthquake, Outrage, Stone Edge, Swords Dance      |

---

## Shrines

Five elemental shrines scattered across the world. Each requires defeating a specific gym leader before the shrine cultists will engage. Clear all four cultists to challenge the High Priest.

| Shrine        | Coordinates  | Entry Requirement     | High Priest          | Challenge                  |
| ------------- | ------------ | --------------------- | -------------------- | -------------------------- |
| Fairy Shrine  | 947 -7 2715  | Mystic Marsh cleared  | High Priest Mab      | Five Tests of the Heart    |
| Ground Shrine | 1899 83 4049 | Kalahar Reach cleared | High Priest Terran   | The Buried Maze            |
| Dragon Shrine | 1998 66 921  | Ryujin Keep cleared   | High Priest Draconis | Hydra Gauntlet             |
| Ice Shrine    | 3634 68 1960 | Nifl Town cleared     | High Priest Boreal   | The Frozen Path            |
| Fire Shrine   | 3500 51 4702 | Royal League cleared  | High Priest Ignis    | Trial by Flame             |

Shrine leaders reward Rare Candies and Diamonds on defeat. The Fire Shrine leader drops a Master Ball and a Netherite Ingot.

### Shrine Challenge Types

Each shrine's final challenge is a unique mechanic that must be completed before the High Priest will battle.

**Five Tests of the Heart (Fairy Shrine)**
Present your lead Pokémon at the shrine altar to pass five sequential tests:
1. **Bond of Friendship** — lead's friendship must be ≥ 160
2. **Nourishment** — lead's fullness must be ≥ 50
3. **Name of the Heart** — lead must have a custom nickname
4. **Radiance** — lead must be shiny
5. **Trial of Resolve** — all four conditions met, and the lead must be your only party Pokémon; then defeat the shrine leader

Commands:
- `/cobblemon-initiative shrine fairy test friendship|fullness|nickname|shiny|resolve`

**The Buried Maze (Ground Shrine)**
You are reduced to half health, afflicted with permanent Blindness, and must navigate the maze to find the High Priest. Every 45 seconds an earthquake teleports you randomly within 20 blocks and applies confusion.

**Hydra Gauntlet (Dragon Shrine)**
Three sequential trainer battles against increasingly powerful Dragon-type opponents. Your party is fully healed between each stage.

**The Frozen Path / Trial by Flame (Ice & Fire Shrines)**
A timed parkour run. The Ice Shrine gives 3 minutes; the Fire Shrine gives 2 minutes. Warning messages are sent at 60 s, 30 s, 10 s, and each second of the final countdown. The finish is triggered by a command block at the end of the course:
```
execute as @a[distance=..3] run cobblemon-initiative shrine <id> complete
```

All shrine challenges are started with `/cobblemon-initiative shrine <id> start` and can be abandoned with `/shrine-abort`.

---

## Villain Team — The Company, Inc.

The antagonist organization operating throughout the region. Their stated goal: destabilize the CobbleDollar currency (backed by nether stars, which they helped mint) and replace it with a wheat-backed commodity economy that they monopolize through total agricultural control.

| Rank                | Name           | Encounter Requirement                       |
| ------------------- | -------------- | ------------------------------------------- |
| Field Agent         | Various Grunts | Early game                                  |
| Contractor          | Various Grunts | After Takehara / Hua Zhan                   |
| Operative / Analyst | Various Grunts | Mid-game gyms                               |
| Regional Manager    | Shade          | After Grunt encounters (Doubles)            |
| Senior Director     | Vex            | After Shade (Doubles)                       |
| COO                 | Noir           | After Vex + late-game gyms                  |
| Acting CEO          | DJ             | After Noir — drops Master Ball + shiny Entei |
| Board of Directors  | Four members   | After DJ + Royal League (see below)         |
| The Founder         | ???            | After all four Board members (Final Boss)   |

Their base of operations is at **The Company, Inc. HQ — 1590 51 1028**.

### Board of Directors

After defeating Acting CEO DJ and clearing the Royal League, four Board Members await in The Boardroom. Their identities are concealed — their names appear as obfuscated glitch text until defeated.

| Name (in-game) | Speciality     | Ace        | Levels |
| -------------- | -------------- | ---------- | ------ |
| M▒▒▒▒▒▒▒▒▒    | Psychic/Fairy  | Alakazam   | 83–85  |
| M▒▒▒           | Fighting       | Rhyperior  | 83–85  |
| M▒▒▒▒          | Electric/Steel | Electivire | 83–85  |
| L▒▒▒▒▒         | Normal/Fairy   | Porygon-Z  | 83–85  |

All four can be challenged in any order. Each drops 5 Rare Candies and 3 Diamonds.

### The Founder

Requires: all four Board members defeated + Acting CEO DJ + Royal League Champion.

A six-Pokémon team at Lv. 88–90: Zoroark, Weavile, Tyranitar, Hydreigon, Mewtwo, Darkrai. Defeating The Founder unlocks the `company_overthrown` achievement and drops a Master Ball, 2 Netherite Ingots, and 64 Wheat.

---

## Battle Frontier

Located at **Battlebay Port — 3800 159 2979**. Unlocks after defeating Champion Cynthia. Eight independent zones, each with two high-level challengers (Lv. 90) and one Frontier Brain (Lv. 100).

| Zone              | Frontier Brain        | Signature Pokémon  | Format  |
| ----------------- | --------------------- | ------------------ | ------- |
| 🏰 Battle Castle  | Castle Lord Percival  | Empoleon           | Doubles |
| 🗼 Battle Tower   | Tower Tycoon Palmer   | Dragonite          | Singles |
| 🏭 Battle Factory | Factory Head Noland   | Porygon-Z          | Singles |
| 🎰 Battle Arcade  | Arcade Star Dahlia    | Blaziken / Zoroark | Doubles |
| 🔺 Battle Pyramid | Pyramid King Brandon  | Regigigas          | Singles |
| ⚓ The Port       | Port Admiral Horatio  | Lugia              | Doubles |
| 🛒 The Market     | Market Mogul Sterling | Chansey / Alakazam | Singles |
| 🌑 Deep Dark Cave | Cave Warden Selene    | Darkrai            | Doubles |

**Cave Warden Selene** requires all seven other Frontier Brains to be defeated before she will accept a challenge. Her full six-Pokémon team is the hardest battle in the game.

---

## Mod Features

### Gym Badge Progression

Defeat gym leaders to unlock higher Pokémon level caps. Progress is tracked per player and persists across sessions. Each gym follows the structure:

- **4 trainers** — accessible after the previous gym's leader is defeated
- **Jr. Apprentice** — requires any 2 of the 4 trainers
- **Apprentice** — doubles battle; requires the Jr. Apprentice
- **Leader** — requires the Apprentice

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

Server-side sight detection for [Easy NPC](https://www.curseforge.com/minecraft/mc-mods/easy-npc) entities.

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

**Scoreboard interop:** the `can_see_player` scoreboard objective is updated each cycle so commands and other datapacks can check `@e[scores={can_see_player=1}]`.

### Install Zones & Area Announcements

Zones are named world regions defined in `install.json`. Each zone can:

- **Suppress mob spawning** (hostile-only or all mobs)
- **Announce entry/exit** to the player with a configurable display style
- **Appear as a labeled frontier** on the JourneyMap minimap via Map Frontiers integration

Three announcement styles are available (configurable in `config/cobblemon-initiative.json`):
- `TITLE` — large centered title with optional subtitle and fade in/out timing
- `ACTIONBAR` — text at the bottom of the screen
- `CHAT` — system message in chat

Run `/cobblemon-initiative install run` to apply all zones, gamerules, and NPC presets in one step.

### Zone-Trace Tool

A dev-only command for tracing polygon zone boundaries to add to `install.json`.

```
/cobblemon-initiative zone-trace begin <name>   — start tracing; receive a wand item
/cobblemon-initiative zone-trace point          — record player's foot position as a vertex
/cobblemon-initiative zone-trace undo           — remove last vertex
/cobblemon-initiative zone-trace type <value>   — set zone category (TOWN, ROUTE, SHRINE, …)
/cobblemon-initiative zone-trace color <hex>    — set hex color for map display
/cobblemon-initiative zone-trace subtitle <text>
/cobblemon-initiative zone-trace announce <bool>
/cobblemon-initiative zone-trace hostile <bool>
/cobblemon-initiative zone-trace finish         — save trace (min. 3 vertices required)
/cobblemon-initiative zone-trace export         — export all saved zones as JSON to server log
```

Right-clicking blocks with the wand also records vertices. Exported JSON matches the `InstallZone` structure and can be pasted directly into `install.json`.

### Map Frontiers Integration

When the [Map Frontiers](https://www.curseforge.com/minecraft/mc-mods/map-frontiers) mod is present, running `/cobblemon-initiative install run` automatically creates one labeled frontier region per install zone on JourneyMap — named, colored, and shaped from the zone's polygon vertices. The mod loads safely without Map Frontiers installed.

### NPC Map System

Maps Easy NPC entity UUIDs to preset names so NPC appearances can be applied in bulk.

```
/cobblemon-initiative npc-map add <uuid> <preset> [label]   — register a UUID → preset mapping
/cobblemon-initiative npc-map remove <uuid>
/cobblemon-initiative npc-map list
/cobblemon-initiative npc-map apply                          — run all stored presets
```

Tab-completing the UUID field suggests the entity you are looking at. `npc-map apply` is called automatically by `install run`.

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

## Credits & Acknowledgments

### Varuna Studios — [UPM Custom Map](https://store.varunabuilds.com/p/upm-2/)

The world that The Cobblemon Initiative is built for was created by **Varuna Studios**. Their **UPM (Ultimate Pokémon Map)** is a hand-crafted, purpose-built Minecraft world featuring:

- Distinct biome-matched towns with thematic architecture
- A continuous overworld designed for Pokémon-style adventure progression
- Gym locations, shrine structures, and landmark placements that form the backbone of this mod's progression design

The Cobblemon Initiative was developed in direct collaboration with Varuna Studio's UPM map as a companion mod to bring trainer battles, badge progression, Nuzlocke mechanics, and structured challenge content to their map. We are grateful for their creative vision, their world-building work, and their support throughout development.

---

## AI Assistance Disclaimer

Development of this mod was assisted by [Claude Code](https://claude.ai/claude-code) (Anthropic). AI was used to help design, implement, and iterate on mod features — including the NPC sight system, Nuzlocke mechanics, config integration, and trainer data.

All generated code and content was reviewed, tested, and intentionally integrated by the developer. AI assistance does not imply that the code is untested or unreviewed — it is a development tool, not a replacement for judgment.

---

_The Cobblemon Initiative is not affiliated with Cobblemon, Mojang, or Nintendo._
