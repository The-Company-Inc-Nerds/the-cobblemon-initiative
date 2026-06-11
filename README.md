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

Server-side sight detection for [Easy NPC](https://www.curseforge.com/minecraft/mc-mods/easy-npc) entities. Each tick, every registered NPC raycasts toward nearby players. A player is "seen" when they are:

- inside the NPC's **forward 120° field of view** (relative to the NPC's facing direction),
- within the NPC's configured **range**, and
- in **unobstructed line of sight** (no solid blocks between the NPC's eyes and the player).

On detection, the NPC acts according to its **mode** (`/npcsight mode`):

- **`dialog`** (default) — fire an Easy NPC dialog (a per-NPC dialog if one is set, otherwise the global default), once per detection session.
- **`pursue`** — walk toward the player while it can see them, by toggling an Easy NPC `FOLLOW_PLAYER` objective on (and off when sight is lost). Combined with a preset `ON_DISTANCE_TOUCH` battle action, this is the classic "trainer spots you, walks up, and battles on contact" (also used for unavoidable Company route-blockers).
- **`approach_once`** — the first time it ever sees the player, walk up and open its dialog once, optionally tagging the player (`meettag`), then never auto-approach again (persisted). Used for one-time story moments (e.g. Mom).

Register and tune NPCs with the `/npcsight` commands; when typing a UUID, tab-complete suggests the entity you are currently looking at. See the full syntax in the [Command Reference](#command-reference), and `presets/README.md` for the matching preset snippets/recipes (`battle_spotter`, `battle_villain_forced`, `dialog_first_meeting`).

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

A dev-only tool for tracing polygon zone boundaries in-world and turning them into `install.json` zones. Tracing happens in two stages: you build a **live session** by walking the boundary and dropping vertices, then `finish` **saves** that session into a persistent saved-zones store, and `export` emits all saved zones as JSON to paste into `install.json`.

**Workflow:**

1. **Start a trace** — `/cobblemon-initiative zone-trace begin <name>`. You receive a tracing wand and an in-memory session is opened for you.
2. **Mark the corners** — walk the zone's perimeter and drop a vertex at each corner with `/cobblemon-initiative zone-trace point`, or simply **right-click a block with the wand** (same effect). Use `zone-trace undo` to drop the last vertex and `zone-trace status` to review the session (vertex list + current settings) at any time.
3. **Set metadata** — `zone-trace type <TOWN|ROUTE|SHRINE|VILLAIN|BATTLE_FRONTIER|LANDMARK>`, `zone-trace color <hex>`, `zone-trace subtitle <text>`, `zone-trace announce <true|false>`, `zone-trace hostile <true|false>`. These can be set in any order before finishing.
4. **Save the trace** — `/cobblemon-initiative zone-trace finish` (needs **≥ 3 vertices**). This moves the session into the saved-zones store and removes the wand. Use `zone-trace list` to see all saved zones and `zone-trace delete <name>` to remove one.
5. **Export to JSON** — `/cobblemon-initiative zone-trace export` writes every saved zone as an `install.json`-ready JSON array to the **server console/log** (not chat). The shape matches `InstallZone`, with explicit `vertices`; `centerX`/`centerZ`/`radius` are intentionally omitted because `install run` derives them from the polygon.
6. **Add to `install.json`** — copy the JSON from the console into the `"zones"` array of `data/cobblemon_initiative/install.json`, then apply it in-game with `/cobblemon-initiative install run`.

> The live session (steps 1–3) is per-player and transient; `finish` is what actually persists a zone. Saved zones survive until you `delete` them, so you can trace several zones, then `export` them all at once.

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

## Command Reference

A complete list of every command the mod registers. Unless noted otherwise, admin commands require **OP level 2**. Arguments in `<angle brackets>` are required; `[square brackets]` are optional. Booleans are `true`/`false`.

### `/cobblemon-initiative` (alias `/ca`) — OP 2

The root command for progression, trainers, shrines, install, and the dev tools. `/ca` is a full alias — anything below also works as `/ca …`.

**Progression & info**

| Command | Description |
| ------- | ----------- |
| `/cobblemon-initiative progress` | Show your gym badges, defeated trainers, and current level cap. Player-only. |
| `/cobblemon-initiative levelcap` | Show your current Pokémon level cap. Player-only. |
| `/cobblemon-initiative reset` | Reset *your* progress — badges, trainer defeats, and level cap. Player-only. |
| `/cobblemon-initiative info <trainer>` | Show a trainer's details: category, location, type, group, prerequisites, and whether you've defeated them. `<trainer>` is a trainer ID (tab-completes). |
| `/cobblemon-initiative list gyms` | List all gym trainers. |
| `/cobblemon-initiative list shrines` | List all shrine trainers. |
| `/cobblemon-initiative list groups` | List all trainer groups. |
| `/cobblemon-initiative list all` | List every registered trainer. |
| `/cobblemon-initiative defeat <trainer>` | Manually mark a trainer as defeated (admin/testing — also runs the normal unlock logic). |

**Shrine challenges**

| Command | Description |
| ------- | ----------- |
| `/cobblemon-initiative shrine <id> start` | Start the shrine challenge `<id>` (e.g. `fairy`, `ground`, `dragon`, `ice`, `fire`). |
| `/cobblemon-initiative shrine <id> stop` | Stop/abort the active challenge for shrine `<id>`. |
| `/cobblemon-initiative shrine <id> test <name>` | Run a specific test step. Fairy shrine uses `friendship`, `fullness`, `nickname`, `shiny`, `resolve`. |
| `/cobblemon-initiative shrine <id> complete` | Mark the challenge complete — typically fired by the finish-line command block: `execute as @a[distance=..3] run cobblemon-initiative shrine <id> complete`. |

Players can also abort any active shrine challenge with **`/shrine-abort`** (no permission required).

**Install / setup**

| Command | Description |
| ------- | ----------- |
| `/cobblemon-initiative install check` | Report gamerule/difficulty drift vs. `install.json`, how many NPC preset mappings are registered, and the zones defined. Changes nothing. |
| `/cobblemon-initiative install run` | Apply everything from `install.json` in one step: gamerules + difficulty, register zones as safe zones, create Map Frontiers frontiers (if Map Frontiers is present), and apply all stored NPC presets. |

**NPC preset mapping** (`npc-map`)

| Command | Description |
| ------- | ----------- |
| `/cobblemon-initiative npc-map add <uuid> <preset> [label]` | Register a UUID → Easy NPC preset mapping. UUID tab-completes to the entity you're looking at. |
| `/cobblemon-initiative npc-map remove <uuid>` | Remove a mapping. |
| `/cobblemon-initiative npc-map list` | List all stored mappings. |
| `/cobblemon-initiative npc-map apply` | Apply every stored preset to its in-world NPC (also run automatically by `install run`). |

**Zone tracing** (`zone-trace`) — dev tool for authoring `install.json` polygon zones

| Command | Description |
| ------- | ----------- |
| `/cobblemon-initiative zone-trace begin <name>` | Start a trace and receive a tracing wand item. |
| `/cobblemon-initiative zone-trace point` | Record your current foot position as a vertex (right-clicking a block with the wand does the same). |
| `/cobblemon-initiative zone-trace undo` | Remove the last recorded vertex. |
| `/cobblemon-initiative zone-trace type <value>` | Set the zone category (`TOWN`, `ROUTE`, `SHRINE`, `VILLAIN`, `BATTLE_FRONTIER`, `LANDMARK`). |
| `/cobblemon-initiative zone-trace color <hex>` | Set the map display color, e.g. `#7AAAD0`. |
| `/cobblemon-initiative zone-trace subtitle <text>` | Set the zone subtitle line. |
| `/cobblemon-initiative zone-trace announce <bool>` | Toggle whether entering the zone announces to the player. |
| `/cobblemon-initiative zone-trace hostile <bool>` | Toggle hostile-only spawn suppression for the zone. |
| `/cobblemon-initiative zone-trace status` | Show the active trace session (vertex count + settings). |
| `/cobblemon-initiative zone-trace finish` | Save the current trace (minimum 3 vertices). |
| `/cobblemon-initiative zone-trace list` | List all saved traced zones. |
| `/cobblemon-initiative zone-trace delete <name>` | Delete a saved zone by name. |
| `/cobblemon-initiative zone-trace export` | Export all saved zones as `install.json`-ready JSON to the server log. |

### `/npcsight` — OP 2 (`reload` requires OP 3)

| Command | Description |
| ------- | ----------- |
| `/npcsight add <uuid> [range] [dialog]` | Register an NPC for sight detection. `range` defaults to the config default (omit, or pass `-1`); `dialog` is the Easy NPC dialog to open on detection. UUID tab-completes to the entity you're looking at. |
| `/npcsight remove <uuid>` | Unregister an NPC. |
| `/npcsight range <uuid> <blocks>` | Set detection range (`-1`–`512`; `-1` = use the config default). |
| `/npcsight dialog <uuid> <name\|clear>` | Set the dialog fired on detection, or `clear` to remove it. |
| `/npcsight mode <uuid> <dialog\|pursue\|approach_once>` | Set the sight behaviour. `dialog` = fire a dialog once per session (default); `pursue` = walk toward the player while in sight (Easy NPC `FOLLOW_PLAYER` toggled on/off), for trainers/forced villains that battle on contact; `approach_once` = walk up and open the dialog the first time it ever sees the player, then never again. |
| `/npcsight meettag <uuid> <tag\|clear>` | Player tag added when an `approach_once` NPC completes its one-time approach (e.g. `met_mom`, to switch its dialog afterward). |
| `/npcsight stoptag <uuid> <tag\|clear>` | Stand-down tag: when the nearest player has it, the NPC stops pursuing/engaging (e.g. `defeated_<trainer_id>` so a beaten trainer stops chasing). |
| `/npcsight reset <uuid>` | Clear the `approach_once` one-shot latch so the one-time approach can fire again (testing). |
| `/npcsight list` | List all registered NPCs (with mode). |
| `/npcsight info <uuid>` | Show one NPC's settings. |
| `/npcsight reload` | Reload the NPC Sight config from disk. **OP 3.** |

### `/nuzlocke`

| Command | Description |
| ------- | ----------- |
| `/nuzlocke deathscreen` | (Testing) Trigger the Pokéball death screen. Player-accessible. |
| `/nuzlocke sacrifice` | (Testing) Trigger the sacrifice-selection screen. Player-accessible. |
| `/nuzlocke reload` | Reload the Nuzlocke config from disk. **OP 2.** |

Nuzlocke *mechanics* (player damage, release, sacrifice-on-flee, duplicate handling, etc.) are toggled in the **Mod Menu config screen**, not via command.

### `/safezone` — OP 2

| Command | Description |
| ------- | ----------- |
| `/safezone add <name> <radius> <hostileOnly> <cylindrical>` | Create a safe zone centered on your current position. `radius` is `1`–`500`; `hostileOnly` and `cylindrical` are booleans. |
| `/safezone remove <name>` | Delete a safe zone. |
| `/safezone list` | List all defined safe zones. |

### `/shrine-abort`

| Command | Description |
| ------- | ----------- |
| `/shrine-abort` | Abort your own active shrine challenge. No permission required (player-facing). |

---

## Developer Scripts

Out-of-game tooling lives in `scripts/`. The Python scripts target Python 3; the Nix dev shell (`nix develop`) provides a suitable interpreter. Run them from the repository root.

### `generate_npc_function`

Generates the `update_npc_presets.mcfunction` that re-applies Easy NPC presets to already-placed in-world NPCs by UUID. Reads `npc_presets.json` and writes a function an admin runs with `/function cobblemon_initiative:update_npc_presets`.

```bash
scripts/generate_npc_function                       # read ./npc_presets.json, write the default function
scripts/generate_npc_function path/to/map.json      # use a specific mapping file
scripts/generate_npc_function --map FILE --out FILE  # explicit input/output paths
scripts/generate_npc_function --dry-run             # print the function without writing
```

Options: `--map/-m FILE`, `--out/-o FILE`, `--world/-w DIR`, `--namespace NS`, `--entity-type TYPE`, `--dry-run`. Preset values resolve to Easy NPC resource locations (e.g. `cyber_leader` → `cobblemon_initiative:humanoid/cyber_leader`; fully-qualified IDs are used as-is).

### `npc_preset_builder`

Interactive composer that splices `presets/snippets/` fragments into a template (or an in-world NPC export), prompts for any `%%PLACEHOLDER%%` values, writes the preset, regenerates the index, and optionally maps the preset to an in-world NPC UUID. See `presets/README.md` for the full snippet/recipe catalog.

```bash
scripts/npc_preset_builder                           # fully interactive — pick a recipe and answer prompts
scripts/npc_preset_builder --list                    # list available templates and snippets
scripts/npc_preset_builder --list-recipes            # list pre-bundled archetype recipes
scripts/npc_preset_builder --recipe shopkeeper --name gaviota_clerk
scripts/npc_preset_builder --recipe gym_leader --name takehara_leader_npc \
    --set TRAINER_ID=takehara_leader --set BADGE_TIER=1 --defaults
scripts/npc_preset_builder \
    --target _battle_trainer_base \
    --snippets battle/battle_basic,ambient/ambient_stationary_look \
    --name takehara_bug_catcher \
    --set TRAINER_ID=takehara_trainer_1 --set PRIZE=200 --defaults
```

Options: `--recipe/-r NAME`, `--target/-t PRESET`, `--snippets/-s LIST`, `--name/-n NAME`, `--display-name TEXT`, `--description TEXT`, `--set KEY=VALUE` (repeatable), `--defaults` (accept placeholder defaults non-interactively), `--out/-o FILE`, `--map QUERY` (map to a UUID from the dev NPC CSV), `--no-index` (skip index regen), `--dry-run`, `--list`, `--list-recipes`.

Placeholder forms: `%%KEY%%` (free text), `%%KEY=default%%` (free text w/ default), `%%KEY:opt1|opt2%%` (choice menu), `%%KEY:opt1|opt2=opt1%%` (choice w/ default). The same `KEY` is asked once and substituted everywhere.

### `update_preset_index`

Regenerates `default_preset.index` for the `cobblemon_initiative` Easy NPC namespace by scanning all `*.npc.snbt` files. Run it after adding or removing any preset file.

```bash
scripts/update_preset_index
```

### `snbt_merge.py`

Splices a keyed section from one SNBT file into another — used for surgical edits to Easy NPC presets and world SNBT (e.g. replacing a preset's `DialogData` with a snippet).

```bash
# Replace data.DialogData in a full preset with a fragment snippet
scripts/snbt_merge.py dialog_snippet.snbt data.DialogData full_preset.snbt

# Deep-merge a source's ActionData into the target's ActionData
scripts/snbt_merge.py snippet.snbt data.ActionData preset.snbt --mode merge

# Print to stdout instead of overwriting in place
scripts/snbt_merge.py snippet.snbt data.DialogData preset.snbt -o -
```

Positional args: `SOURCE KEY TARGET`. Options: `-o/--out FILE` (default: overwrite `TARGET`; `-` for stdout), `-m/--mode replace|merge` (default `replace`), `--src-key PATH` (key path inside `SOURCE`; `.` = whole file), `--indent N` (default 4), `--compact` (single-line output).

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
