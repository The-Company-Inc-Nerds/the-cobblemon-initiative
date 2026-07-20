# 13 — Noble Encounters: The Gating Quests

> **⚠ STATUS 2026-07-20:** the noble roster shipped as **Groudon / Kyogre / Rayquaza**,
> **Articuno / Zapdos / Moltres**, **Mew** (tag), plus **Manaphy** and the wandering
> pseudo mini-nobles (`AmbientNobleManager` daily rolls + the three birds as post-HQ-raid
> proximity ambushes, gate `defeated_villain_boss`). **The old "crystal launches the
> shrine's noble" contract is DEAD** — shrine crystals now summon a *separate* guardian set
> (Ho-Oh / Landorus / Glastrier / Kyurem / Xerneas via `ShrineCrystalItem`); Articuno and
> Moltres launch from a dedicated **Ice/Fire keeper dialog button** (`noble start …`), which
> is independent of the crystal. Givers with dialog: Mew/Kyogre/Groudon/Rayquaza/Zapdos.
> Player-facing truth: [`wiki/Guidebook-Nobles.md`](../../wiki/Guidebook-Nobles.md).

> **UNIT SLUG:** `13_nobles_gating`. This is the **quest / gate / giver layer** that leads a
> player to each Legends-Arceus-style **noble** encounter. The `noble/` combat engine is
> **already shipped** (`docs/NOBLE_ENCOUNTERS.md`): body → attacks → stagger → catchable wild
> Cobblemon. What is missing per noble is exactly four things, and this doc supplies all four:
> **(1)** a thematic SITE (arena `center` coord), **(2)** the GATE (badge band / shrine cleared
> / shrine-crystal item / story flag), **(3)** the GIVER + trigger (a story-flag-gated dialog
> button running `noble start <id>`, or a shrine-crystal item), **(4)** the reward flag +
> arena center. It **dovetails with `14_shrines`**: clearing an elemental shrine is what unlocks
> the type-matched bird / trio noble (see `14_shrines §Crystal↔noble handshake`).
>
> **DO NOT confuse this with `docs/roadmap/16_legendaries_nobles.md`.** That roadmap describes a
> *different, superseded* "noble = PvP-ladder → wild-spawn" design that pre-dates the combat
> engine. The engine that actually shipped is the LoA boss in `docs/NOBLE_ENCOUNTERS.md`, and
> **that** is what this unit gates. Where the two disagree, the shipped engine wins.

---

## 1. Overview

**Set-piece, not a town.** The seven shipped nobles (`groudon, kyogre, rayquaza, articuno,
zapdos, moltres, mew` — `NobleEncounterManager.NOBLE_IDS`) are optional spectacle scattered
across the route by cap band. Each is an authored `noble_encounters/<id>.json` LoA boss that
today has `arena.center = [0,0,0]` (falls back to player pos) and **no unlock gate** — `/noble
start` is OP-2 only. This unit gives each one a home coordinate, a lore-consistent unlock, and
a giver NPC whose dialog button fires the launch.

**Band context.** Nobles are **index-neutral** — they do not move `cd_instability` and grant no
`memory_fragment` (fragments are gym-gated 1..10 and canon; do not add an 11th). Their gates are
tied to the **cap ladder** so the `battleSpecies` (currently a flat `level=70` in every JSON) is
retuned per noble to sit **at or under the cap in force at the gate** — a noble is meant to be
*caught and used*, not admired. The birds tie to the **elemental shrines** (Articuno→Ice,
Moltres→Fire, Zapdos→Cyber/Electric); the weather trio ties to the late Ground/Water/Dragon
band; Mew is a hidden open-field chase.

**Recognition tier.** The Pokemon do not recognise the founder (wild legendaries never do). The
**giver** NPCs are **civilians / lore-keepers**, so they do **not** run the Company recognition
gradient — civilians never recognise the founder, and Mom is never involved. Where they use the
`recognition` sugar it is only the badge-count **band** the schema defines (early <3 / mid 3-6 /
late 7+), used as a cap gate, never as founder-recognition. The **shrine keepers** (Glacius,
Ignis, Draconis) carry their own oblique "the land remembers you" tier from `14_shrines` — that
is the only place the recognition arc touches this unit, and it never names the Founder.

**Route placement (gate → cap band):**

| Noble | Element | Gate era (cap in force) | Site region |
|---|---|---|---|
| Mew | psychic | post-gym-3 band (cap 37) | Safari Zone (hidden, safe) |
| Articuno | ice | Ice Shrine cleared (cap 74) | Ice Shrine / Nifl |
| Moltres | fire | Fire Shrine cleared (post-league, cap 85) | Fire Shrine / Scorchspire |
| Zapdos | electric | post-gym-7 (cap 62) | Cyber City edge |
| Kyogre | water | post-gym-5 (cap 50) | Gullwing Coast, offshore |
| Groudon | ground/fire | post-gym-10 (cap 80) | Volcano Peak crater |
| Rayquaza | dragon | Dragon Shrine cleared + gym 8 (cap 68) | Ryujin spire sky-altar |

---

## 2. Cast

Every giver is a **placement-latch prop or civilian** (no `uuid` — spawns once within 40 blocks
of its coord). Coords are lifted from `install.json` zone hulls / `trainers/shrines/*.json` /
CLAUDE.md where cited, or marked **PROPOSED** (builder confirms a standable, visible spot).

| id | display_name | role | one-line concept | placement anchor |
|---|---|---|---|---|
| `noble_giver_mew_wisp` | A Giggle in the Grass | civilian | A wisp-prop in the Safari that hums; interacting starts the chase. | Safari Zone `[1300, 64, 1450]` **PROPOSED** |
| `noble_monument_kyogre` | Warning Buoy | lore_keeper | A storm-lashed buoy off Gaviota: *WARNING — the sea seems violent in these parts.* | Gullwing Coast pier-end `[655, 63, 3300]` **PROPOSED** |
| `noble_monument_groudon` | Crater Warding Stone | lore_keeper | A basalt monument on the crater rim; the mountain is not asleep. | Volcano Peak rim `[3805, 110, 3746]` **PROPOSED** |
| `noble_monument_rayquaza` | Sky-Altar | lore_keeper | A weather-scoured altar atop the Ryujin spire; the sky has a name here. | Ryujin spire top `[2156, 240, 884]` **PROPOSED** |
| `noble_giver_zapdos_warden` | Grid Warden Cass | quest_giver | A Cyber City tech asking the founder to help hold the grid against the storm-bird. | Cyber City edge `[1393, 66, 1065]` **PROPOSED** |
| *(Ice — Articuno)* | High Priest Glacius | shrine_leader | **reuse** `ice_shrine_leader` — his post-defeat entry gains the Articuno button. | Ice Shrine `[3644, 68, 1960]` (existing) |
| *(Fire — Moltres)* | High Priest Ignis | shrine_leader | **reuse** `fire_shrine_leader` — post-defeat entry gains the Moltres button. | Fire Shrine `[3510, 51, 4702]` (existing) |

> **Dovetail note:** Articuno and Moltres have **no new giver** — their trigger is bolted onto
> the *existing* shrine-leader dialog (post-defeat entry) plus the shrine-crystal item as an
> alternate launch. Zapdos, Kyogre, Groudon, Rayquaza get a dedicated giver prop. Mew gets a
> whimsical wisp. This keeps the shrine keepers as the single "the land remembers" voice, and
> reuses the canonical shrine-leader ids (`ice_shrine_leader`, `fire_shrine_leader`,
> `dragon_shrine_leader` — Glacius, Ignis, Draconis) rather than inventing new ones.

---

## 3. Quests

Each subsection: concept + forward hook + back-echo; a ready-to-paste **character** block; a
ready-to-paste **dialog** block (or reuse note); **DATAPACK NEEDS**; a **QUEST_TARGETS** stage;
**REWARD/BALANCE**.

**Shared launch idiom.** Every noble button runs, as the NPC (not `as_player` — `noble start`
resolves the nearest player and needs OP context, which the NPC dispatch supplies through the
allowlist), a single command:

```
noble start <id>
```

The engine handles intro/arena/stagger/reward. Because the arena `center` is authored per noble
(§6 build step), the boss spawns at the SITE, not at the player. The dialog **gate** is what makes
the button appear only once the unlock is earned; the button is **fail-soft** (if `noblesEnabled`
is off in `NobleConfig`, `noble start` no-ops with a chat line — no crash).

**`say[]` is alternatives, not a script.** Every `say[]` line below is authored as a
**self-contained alternative** — one is picked at random per open. None is a page-1/page-2
sequence (hard rule: multi-line `say[]` renders ONE random page). The button text carries the
call-to-action so no single line depends on another to make sense.

**Cap-legality.** Every noble JSON currently ships `battleSpecies "<id> level=70 …"`. Per the
cap-under rule, retune `level=` per noble (§6, §REWARD blocks) to at or under the cap in force
at the gate. No side/wager framing is needed — the noble is **not a trainer battle**, cannot
whiteout via decline, and the launch button is fully opt-in (the player pressed it). The
*catch attempt* under Nuzlocke is the risk, and that risk is the content. There is no forced
above-cap fight anywhere in this unit, so the starter-only fairness floor is never touched.

---

### 3.1 Mew — "Catch Me If You Can" (open-field chase)

**Concept.** A giggling wisp-prop hidden in the Safari Zone. Interacting launches the `mew`
noble (a `chase`-type: tag it 6× → it tires → catchable). Pure joy, zero corporate dread — the
one legendary the Company plot never touches.
**Forward hook:** the wisp murmurs about *a keep to the north where the sky itself has a temper*
(points at Rayquaza / Ryujin).
**Back-echo:** it giggles that *the marsh went still once, and something old woke up* — a safe
generic back-reference to the early Mystic beat (no invented character named).

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "noble_giver_mew_wisp",
  "display_name": "A Giggle in the Grass",
  "role": "civilian",
  "act": "n/a",
  "location": "Safari Zone",
  "_comment": "Mew chase giver. Hidden wisp-prop in the Safari (mobsSpawn:false, no stakes to reach). Interact -> noble start mew. Gate: mid recognition band (badges 3-6) so it is not accessible before the marsh. PLACEMENT PROPOSED - builder confirms a pretty grassy pocket in the Safari hull (vertices ~x1212-1420 z1344-1527). No uuid (latch spawns once).",
  "recipe": "civilian",
  "dialog": "dialog:noble_mew_chase",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 1300, "y": 64, "z": 1450 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "noble_mew_chase",
  "type": "STANDARD",
  "entries": [
    {
      "label": "after",
      "name": "the wisp - befriended",
      "priority": 20,
      "gate": { "tag": "defeated_noble_mew" },
      "say": [
        "It still visits, you know - on the days you are kind to yourself. Some friends only come when you are not chasing them.",
        "The grass is quiet now. It remembers being chased and it did not mind at all - come back and it may play again."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Rest a while", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "the wisp",
      "priority": 10,
      "default": true,
      "gate": { "recognition": "mid", "not_tag": "defeated_noble_mew" },
      "say": [
        "Hee. Something pink and impossible flits where the tall grass hides it. It will not stand and fight - it will run, and blink, and giggle. Touch it enough times and it tires itself silly, and then it is yours if you are gentle.",
        "You felt that too, did you. It wants to play, not to fight. Chase it, corner it, be kind - and mind the keep up north where the sky has a temper, for that is no game at all."
      ],
      "buttons": [
        {
          "label": "chase_button",
          "text": "Chase the wisp",
          "actions": [
            { "do": "command", "cmd": "noble start mew" },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "Maybe later", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "locked",
      "name": "the wisp - too soon",
      "priority": 5,
      "gate": { "recognition": "early" },
      "say": [
        "Hee. Not yet, little one. Come back when you have walked a badge or three - the grass only plays with someone it has seen around.",
        "The tall grass keeps its secret from strangers. Earn a badge or two and it may forget you are one."
      ]
    }
  ]
}
```

**DATAPACK NEEDS:** none beyond the compiler's band-tag tick for the `mid` recognition band
(already maintained). The `defeated_noble_mew` flag is set by the noble engine reward block — no
function needed. (If the flag lowers as scoreboard-only, gate the after-entry with
`{ "score": { "objective": "defeated_noble_mew", "op": "gte", "value": 1 } }` — see §7 Q1.)

**QUEST_TARGETS entry** (append to `registers/quest_targets.json`, new side holder):

```json
{
  "holder": "q.side_wisp",
  "name": "A Giggle in the Grass",
  "slot": 50,
  "stages": [
    {
      "if_tags": ["heard_mew_rumor"],
      "not_tags": ["defeated_noble_mew"],
      "label": "Chase the wisp in the Safari Zone",
      "target": { "npc": "noble_giver_mew_wisp" },
      "note": "heard_mew_rumor is set by a Safari rumor-hub line (see §4). Optional - the wisp is a hidden discovery; omit the register entry entirely if it should stay unlisted (see §7 Q5)."
    }
  ]
}
```

**REWARD/BALANCE.** Retune `mew.json` `battleSpecies` to **`mew level=35`** (cap 37 at gate).
Safari is `mobsSpawn:false` (safe) — but the mew engine Phase-2 wild battle opens regardless of
zone, so the catch is a real Nuzlocke moment. Chase is friendly/invulnerable (no whiteout risk
in Phase 1). No decline needed. Reward: the shipped `training_grand` loot + `defeated_noble_mew`.

---

### 3.2 Articuno — "Winter Takes Wing" (Ice Shrine dovetail)

**Concept.** Clearing the **Ice Shrine** (High Priest Glacius, `ice_shrine_leader`) is the key.
His post-defeat entry gains an Articuno button; the **Ice Shrine Crystal** is the portable
alternate launch. The frozen gale answers the same cold the shrine keeps.
**Forward hook:** Glacius names *the fire-keeper to the south who guards a bird of the opposite
temper* (Moltres / Fire Shrine).
**Back-echo:** references the player having *hardened their scales* in the wyrm-shrine (Draconis /
Dragon Shrine) if `defeated_dragon_shrine_leader`.

**Character — reuse `ice_shrine_leader`** (no new character file). Add the button to its dialog.
Below is the **edited** `dialog:shrine_ice` `after` entry (paste over the existing post-defeat
entry; keep the rest of the tree). Each `say[]` line stands alone; the existing frost-token line
is preserved so the shrine reward flavour is not lost:

```json
{
  "label": "after",
  "name": "Glacius — after defeat",
  "priority": 20,
  "gate": { "defeated": "ice_shrine_leader" },
  "say": [
    "The frost token is yours. It will preserve what you give it and let nothing rot, not even the things you would beg it to let go. That is the gift, and the price.",
    "There is a wing in the blizzard that has never let a soul near it. The cold leans toward you the way the wyrm did - call the gale down and face what winter keeps, if you would.",
    "The ice took your measure and did not flinch. Few leave this shrine breathing warm; you did. The frozen gale will answer a hand like that, if you dare to raise it."
  ],
  "buttons": [
    {
      "label": "noble_button",
      "text": "Call the frozen gale",
      "gate": { "not_tag": "defeated_noble_articuno" },
      "actions": [
        { "do": "command", "cmd": "noble start articuno" },
        { "do": "close" }
      ]
    },
    { "label": "leave_button", "text": "I will keep the frost", "actions": [ { "do": "close" } ] }
  ]
}
```

**DATAPACK NEEDS:**
- `function/noble/articuno/crystal_launch.mcfunction` (optional, crystal path) — one line:
  `execute as @p[distance=..8] at @s run noble start articuno`. Fired by re-pointing the Ice
  Shrine Crystal at this function instead of its legacy `spawnpokemon`. **This resolves the
  double-crystal / flat-L70 problem** documented in `14_shrines §Crystal↔noble handshake` and
  `docs/roadmap/18_shrines_audit.md §7`: the crystal launches the *engine* noble (gated,
  cap-legal, one arena) instead of dumping a raw L70 wild.

**QUEST_TARGETS entry:**

```json
{
  "holder": "q.side_gale",
  "name": "Winter Takes Wing",
  "slot": 49,
  "stages": [
    {
      "if_tags": ["defeated_ice_shrine_leader"],
      "not_tags": ["defeated_noble_articuno"],
      "label": "Call the frozen gale at the Ice Shrine",
      "target": { "npc": "ice_shrine_leader" },
      "note": "Ice Shrine leader cluster ~[3644,68,1960]. Also launchable via the Ice Shrine Crystal item anywhere."
    }
  ]
}
```

**REWARD/BALANCE.** Retune `articuno.json` `battleSpecies` to **`articuno level=72`** (cap 74 at
the Nifl/Ice-Shrine band). Arena `center` = **Ice Shrine platform PROPOSED `[3644, 68, 1960]`**
(`mobsSpawn:false` — but Phase-2 catch is still a real Nuzlocke throw). Reward: `training_grand`
+ `defeated_noble_articuno`. No decline. Crystal path is fail-soft (function no-ops if disabled).

---

### 3.3 Moltres — "Rebirth in Ember" (Fire Shrine dovetail)

**Concept.** The **Fire Shrine** is a post-Royal-League superboss (gated on
`royal_league_champion`, per `docs/roadmap/18_shrines_audit.md`). Ignis (`fire_shrine_leader`)
gains the Moltres button on his post-defeat entry; the **Fire Shrine Crystal** is the alternate
launch. `moltres.json` already uses the `rebirth` stagger script (ember-collapse fake-out) — the
arena is the payoff for the hardest shrine.
**Forward hook:** Ignis warns the ember *is the mirror-fire — the one that shows a man his own
face* (oblique Founder seed; never names it — legal, no Founder name before Act 3).
**Back-echo:** *You called winter down and lived — now stand in the fire that made it* (references
`defeated_noble_articuno` if present, a trio-of-birds echo).

**Character — reuse `fire_shrine_leader`.** Edited `dialog:shrine_fire` `after` entry (the ember
reward flavour is preserved; each line stands alone):

```json
{
  "label": "after",
  "name": "Ignis — after defeat",
  "priority": 20,
  "gate": { "defeated": "fire_shrine_leader" },
  "say": [
    "The ember is yours now. It will not warm you. It will demand of you, the way fire always demands, until there is nothing of you left to give.",
    "One fire remains, the oldest - a bird that dies and is not dead, that burns and is reborn. They say it shows a soul its own face in the coals. Call it down, if you would look.",
    "You walked through the whole of it and came out wearing ash like a crown. The mirror-fire waits still - raise your hand to it and see what the embers already know of you."
  ],
  "buttons": [
    {
      "label": "noble_button",
      "text": "Call the reborn flame",
      "gate": { "not_tag": "defeated_noble_moltres" },
      "actions": [
        { "do": "command", "cmd": "noble start moltres" },
        { "do": "close" }
      ]
    },
    { "label": "leave_button", "text": "I accept the ember", "actions": [ { "do": "close" } ] }
  ]
}
```

**DATAPACK NEEDS:**
- `function/noble/moltres/crystal_launch.mcfunction` — `execute as @p[distance=..8] at @s run
  noble start moltres` (Fire Shrine Crystal repoint, same fix as Articuno).

**QUEST_TARGETS entry:**

```json
{
  "holder": "q.side_ember",
  "name": "Rebirth in Ember",
  "slot": 48,
  "stages": [
    {
      "if_tags": ["defeated_fire_shrine_leader"],
      "not_tags": ["defeated_noble_moltres"],
      "label": "Call the reborn flame at the Fire Shrine",
      "target": { "npc": "fire_shrine_leader" },
      "note": "Fire Shrine leader cluster ~[3510,51,4702]. Post-Royal-League gate (Fire Shrine itself needs royal_league_champion)."
    }
  ]
}
```

**REWARD/BALANCE.** `moltres.json` `battleSpecies` → **`moltres level=82`** (post-league, cap 85
band; matches Ignis own roster premium). Arena `center` = **Fire Shrine platform PROPOSED
`[3510, 51, 4702]`**. Reward: `training_grand` + `defeated_noble_moltres`.

---

### 3.4 Zapdos — "The Defense of Cyber City" (prompt noble)

**Concept.** Grid Warden Cass asks the founder to help hold Cyber City grid against the
storm-bird — the one noble triggered by a *person asking* rather than a monument. Post-gym-7.
**Forward hook:** Cass mentions *the Company men have gone quiet since the tower fell* (points at
the HQ raid / Acting CEO DJ, which unlocks in this same band — LORE_BIBLE §8, gated
`badges gte 7` AND `fields_liberated gte 4`).
**Back-echo:** Cass thanks the founder *for the fields you cleared — the grain-lights came back
on out east* (references `fields_liberated`, the Wheat War).

> Cass is a **civilian tech, not Company** — she recognises the player as *the one who cleared the
> fields* (public heroism), **never** as the founder. That distinction is legal: civilians never
> know the founder identity. The `recognition:"late"` gate here is only the badge-count band
> (≥7), used as the post-gym-7 cap gate, not founder-recognition.

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "noble_giver_zapdos_warden",
  "display_name": "Grid Warden Cass",
  "role": "quest_giver",
  "act": "1",
  "location": "Cyber City - Grid Edge",
  "_comment": "Zapdos prompt-noble giver. Post-gym-7 (cap 62). Interact -> noble start zapdos. Cass is a civilian tech, NOT Company - she does not recognise the founder; the recognition:late gate is only the badge>=7 cap band. Reconcile the site coord with 05_cyber_city.md (its NPC cluster sits ~[1470-1610,65,1100-1150]); [1393,66,1065] is the western grid edge, outside the safe town hull center. Builder confirms a rooftop/pylon standable. No uuid (latch).",
  "recipe": "civilian",
  "dialog": "dialog:noble_zapdos_defense",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 1393, "y": 66, "z": 1065 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "noble_zapdos_defense",
  "type": "STANDARD",
  "entries": [
    {
      "label": "after",
      "name": "Cass - storm broken",
      "priority": 20,
      "gate": { "tag": "defeated_noble_zapdos" },
      "say": [
        "Grid is green across the board. You brought the storm-bird down and the whole city stopped holding its breath. Drinks are on the substation crew tonight.",
        "The Company men have been quiet since the tower fell - quiet the way a held breath is quiet. Watch yourself out there, and thank you for the roof."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Keep the lights on", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Grid Warden Cass",
      "priority": 10,
      "default": true,
      "gate": { "recognition": "late", "not_tag": "defeated_noble_zapdos" },
      "say": [
        "You are the one who cleared the fields - the grain-lights came back on out east because of you. So I will ask it straight: help us defend Cyber City from Zapdos. It has circled the grid for days and the pylons cannot take another strike. Open the roof and I will make it angry enough to come down.",
        "It comes down when it is angry enough, and we can make it angry. What we cannot do is stand in front of it - you can. Say the word and I open the roof to the storm-bird."
      ],
      "buttons": [
        {
          "label": "defense_button",
          "text": "Open the roof - I will hold the line",
          "actions": [
            { "do": "command", "cmd": "noble start zapdos" },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "Not yet", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "locked",
      "name": "Cass - too soon",
      "priority": 5,
      "gate": { "recognition": "mid" },
      "say": [
        "Storm-bird is a gym-seven problem, friend, and you are not there yet. Come back when you have the Circuit badge and we will talk about the roof.",
        "The pylons will hold a while longer. Earn Volt badge first - I am not sending a rookie onto that roof."
      ]
    }
  ]
}
```

**DATAPACK NEEDS:** none — `defeated_noble_zapdos` is engine-set; the `late` recognition band tag
is compiler-maintained (`recognition:"late"` = badges ≥ 7 per the schema band).

**QUEST_TARGETS entry:**

```json
{
  "holder": "q.side_grid",
  "name": "The Defense of Cyber City",
  "slot": 47,
  "stages": [
    {
      "if_tags": ["mem_gte_7"],
      "not_tags": ["defeated_noble_zapdos"],
      "label": "Help Grid Warden Cass hold the grid",
      "target": { "npc": "noble_giver_zapdos_warden" },
      "note": "mem_gte_7 band tag (post-gym-7). Cyber City edge, PROPOSED [1393,66,1065]. Reconcile with 05_cyber_city.md staging."
    }
  ]
}
```

**REWARD/BALANCE.** `zapdos.json` `battleSpecies` → **`zapdos level=60`** (cap 62). Arena
`center` = **Cyber City grid edge PROPOSED `[1393, 66, 1065]`** — outside the safe town hull so
the catch is full-stakes. `zapdos.json` is a flyer (grounded-window mechanic already authored).
Reward: `training_grand` + `defeated_noble_zapdos`.

---

### 3.5 Kyogre — "Under the Storm" (monument buoy)

**Concept.** A warning buoy off Gaviota: *WARNING — the sea seems violent in these parts* (the
canon monument line). Reading the buoy and pressing the button launches the `kyogre` noble in the
open water. Post-gym-5.
**Forward hook:** the buoy log warns of *the mountain to the far south-east that drank the sea
dry once* (Groudon / Volcano Peak — the trio rivalry).
**Back-echo:** the log is *signed by a harbourmaster who does not work for the Company anymore*
(the defector motif; safe generic, no invented character named).

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "noble_monument_kyogre",
  "display_name": "Warning Buoy",
  "role": "lore_keeper",
  "act": "n/a",
  "location": "Gullwing Coast - Offshore",
  "_comment": "Kyogre monument-noble giver. Dialog-only prop (a storm buoy). Post-gym-5 (cap 50). Interact -> noble start kyogre. Gullwing Coast is mobsSpawn:true (full stakes at the offshore arena). PLACEMENT PROPOSED at the pier-end / first offshore buoy; builder confirms a standable float. No uuid (latch).",
  "recipe": "civilian",
  "dialog": "dialog:noble_kyogre_buoy",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 655, "y": 63, "z": 3300 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "noble_kyogre_buoy",
  "type": "STANDARD",
  "entries": [
    {
      "label": "after",
      "name": "the buoy - sea calmed",
      "priority": 20,
      "gate": { "tag": "defeated_noble_kyogre" },
      "say": [
        "The water lies flat now, the way it has not in a generation. The buoy still swings its lantern, out of habit more than warning.",
        "Whatever the log-keeper feared, you met it and it went quiet. The tide will remember the day it was made to."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Let the sea rest", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Warning Buoy",
      "priority": 10,
      "default": true,
      "gate": { "recognition": "mid", "not_tag": "defeated_noble_kyogre" },
      "say": [
        "A lantern buoy, chained to the seabed. The log bolted to its side reads: WARNING - the sea seems violent in these parts. Below that, in a harbourmaster hand that no longer draws Company pay: it surfaces for those who ask. Ring the bell only if you mean it.",
        "The chain groans against something enormous down there, patient as a debt. Ring the buoy bell and it will rise - do not ring it unless you are ready for the deep."
      ],
      "buttons": [
        {
          "label": "call_button",
          "text": "Ring the bell - call the deep",
          "actions": [
            { "do": "command", "cmd": "noble start kyogre" },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "Leave it chained", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "locked",
      "name": "the buoy - too soon",
      "priority": 5,
      "gate": { "recognition": "early" },
      "say": [
        "The log is water-stained past reading, and the chain will not budge for you. Come back when you have walked further - the sea knows who has earned the deep.",
        "The bell rope is slick and it slips your hand. Not yet. The deep does not answer the unproven."
      ]
    }
  ]
}
```

**DATAPACK NEEDS:** none — `defeated_noble_kyogre` engine-set; the `mid` band tag is
compiler-maintained (`recognition:"mid"` covers badges 3–6; if a strict ≥5 gate is wanted, swap
the `default` entry gate to `{ "badges": { "op": "gte", "value": 5 } }`, which lowers to a
`badges_gte_5` band tag).

**QUEST_TARGETS entry:**

```json
{
  "holder": "q.side_deep",
  "name": "Under the Storm",
  "slot": 46,
  "stages": [
    {
      "if_tags": ["mem_gte_5"],
      "not_tags": ["defeated_noble_kyogre"],
      "label": "Ring the warning buoy off Gaviota",
      "target": { "npc": "noble_monument_kyogre" },
      "note": "Gullwing Coast offshore, PROPOSED [655,63,3300]. mobsSpawn:true - full stakes."
    }
  ]
}
```

**REWARD/BALANCE.** `kyogre.json` `battleSpecies` → **`kyogre level=50`** (cap 50). Arena
`center` = **Gullwing Coast open water PROPOSED `[700, 62, 3330]`** (offshore, `mobsSpawn:true`).
Reward: `training_grand` + `defeated_noble_kyogre`.

---

### 3.6 Groudon — "The Mountain Holds Its Breath" (crater monument)

**Concept.** A warding stone on the Volcano Peak crater rim: the mountain is not asleep. Post-
gym-10 (the last cap band before the League). The crater bowl is the arena.
**Forward hook:** the stone speaks of *a sky-serpent that only the highest altar can call* (Rayquaza
— completes the trio references).
**Back-echo:** *the sea to the west already met its match; the mountain will not be outdone*
(references `defeated_noble_kyogre` — the Groudon/Kyogre rivalry).

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "noble_monument_groudon",
  "display_name": "Crater Warding Stone",
  "role": "lore_keeper",
  "act": "n/a",
  "location": "Volcano Peak - Crater Rim",
  "_comment": "Groudon monument-noble giver. Dialog-only basalt monument on the crater rim. Post-gym-10 (cap 80). Interact -> noble start groudon. Volcano Peak is mobsSpawn:true. PLACEMENT PROPOSED on the rim overlooking the bowl; builder confirms a standable ledge. No uuid (latch).",
  "recipe": "civilian",
  "dialog": "dialog:noble_groudon_stone",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 3805, "y": 110, "z": 3746 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "noble_groudon_stone",
  "type": "STANDARD",
  "entries": [
    {
      "label": "after",
      "name": "the stone - mountain quiet",
      "priority": 20,
      "gate": { "tag": "defeated_noble_groudon" },
      "say": [
        "The mountain sleeps again, deeper than before. You went down into its heart and it let you climb back out. Few stones get to record that.",
        "The heat leans toward the rim where you stand, the way a hound leans toward a hand it knows. Strange, for a mountain to know a person."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Let it sleep", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Crater Warding Stone",
      "priority": 10,
      "default": true,
      "gate": { "recognition": "late", "not_tag": "defeated_noble_groudon" },
      "say": [
        "Basalt, carved and re-carved by hands long gone. The warning is worn but plain: THE MOUNTAIN IS NOT ASLEEP. Beneath it, freshly cut: the sea to the west already met its match, and the mountain will not be outdone. Strike the stone and a titan rises at full weight - do not do this lightly.",
        "The bowl below breathes heat. Something vast turns in its sleep down there. Strike the stone and it will rise to meet you. Only strike it when you are ready to answer the mountain."
      ],
      "buttons": [
        {
          "label": "call_button",
          "text": "Strike the stone - wake the mountain",
          "actions": [
            { "do": "command", "cmd": "noble start groudon" },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "Let it sleep", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "locked",
      "name": "the stone - too soon",
      "priority": 5,
      "gate": { "recognition": "mid" },
      "say": [
        "The stone will not answer a hand that has not yet earned the tenth badge. Come back when you have walked the whole route - the mountain does not stir for the unproven.",
        "You put your palm to the basalt and it stays cold. Not yet. Earn the last badge and the heat may notice you."
      ]
    }
  ]
}
```

**DATAPACK NEEDS:** for a strict post-gym-10 gate, swap the `default` entry gate to
`{ "badges": { "op": "gte", "value": 10 }, "not_tag": "defeated_noble_groudon" }` (lowers to a
`badges_gte_10` band tag). Otherwise `recognition:"late"` (≥7) is the loosest acceptable gate.
No new function; `defeated_noble_groudon` is engine-set. (See §7 Q4 for gate-depth sign-off.)

**QUEST_TARGETS entry:**

```json
{
  "holder": "q.side_mountain",
  "name": "The Mountain Holds Its Breath",
  "slot": 45,
  "stages": [
    {
      "if_tags": ["mem_gte_10"],
      "not_tags": ["defeated_noble_groudon"],
      "label": "Strike the warding stone on the crater rim",
      "target": { "npc": "noble_monument_groudon" },
      "note": "Volcano Peak center [3805,64,3746] r87; rim monument PROPOSED [3805,110,3746]. mobsSpawn:true. If the looser recognition:late gate is used instead of badges>=10, change if_tags to mem_gte_7."
    }
  ]
}
```

**REWARD/BALANCE.** `groudon.json` `battleSpecies` → **`groudon level=78`** (cap 80). Arena
`center` = **crater bowl PROPOSED `[3805, 70, 3746]`** (center of Volcano Peak, in the bowl below
the rim). Reward: `training_grand` + `defeated_noble_groudon`.

---

### 3.7 Rayquaza — "What Falls From the Sky" (Dragon Shrine + spire dovetail)

**Concept.** Double gate — the sky-altar atop the Ryujin spire is sealed until the player has
**gym 8 AND the Dragon Shrine cleared** (`defeated_dragon_shrine_leader`, Draconis). Rayquaza is
the trio capstone. The Dragon Shrine crystal is the alternate launch.
**Forward hook:** the altar warns that *even the sky-serpent kneels to the mirror-fire that waits
past the League* (oblique Moltres/Founder seed; never names it — legal, no Founder name before
Act 3).
**Back-echo:** *the mountain and the sea have already answered you; only the sky remained*
(references `defeated_noble_groudon` / `defeated_noble_kyogre` — the trio triple-echo, the
streamable "you have all three" beat).

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "noble_monument_rayquaza",
  "display_name": "Sky-Altar",
  "role": "lore_keeper",
  "act": "n/a",
  "location": "Ryujin Keep - Spire Top",
  "_comment": "Rayquaza monument-noble giver. Weather-scoured altar atop the Ryujin spire. Gate: mem_gte_8 AND defeated_dragon_shrine_leader (double gate - dovetails 14_shrines). Interact -> noble start rayquaza. PLACEMENT PROPOSED on the spire top above the Draconis leader coord ~[2156,201,884]. No uuid (latch).",
  "recipe": "civilian",
  "dialog": "dialog:noble_rayquaza_altar",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 2156, "y": 240, "z": 884 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "noble_rayquaza_altar",
  "type": "STANDARD",
  "entries": [
    {
      "label": "after",
      "name": "the altar - sky answered",
      "priority": 30,
      "gate": { "tag": "defeated_noble_rayquaza" },
      "say": [
        "The sky is empty and clean. You called the serpent of the high air down to the stone and it went where you led. The altar has nothing left to guard.",
        "Mountain, sea, and sky - all three have knelt to one hand now. The old stories had a word for someone who could do that. The altar has forgotten it, and perhaps that is a mercy."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Descend the spire", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "ready",
      "name": "Sky-Altar - ready",
      "priority": 20,
      "gate": { "all_tags": ["mem_gte_8", "defeated_dragon_shrine_leader"], "not_tag": "defeated_noble_rayquaza" },
      "say": [
        "A ring of scoured stone, open to the whole sky. The inscription is half worn: WHAT FALLS FROM THE SKY answers only the one who has hardened their scales below. You have, and the wyrm-token is warm in your pack. Raise your hand and the serpent of the high air will come down to meet it.",
        "The mountain and the sea have already answered you. Only the sky remained. Raise your hand to the altar and call the serpent down - the wind up here has been waiting for a hand like yours."
      ],
      "buttons": [
        {
          "label": "call_button",
          "text": "Raise your hand to the sky",
          "actions": [
            { "do": "command", "cmd": "noble start rayquaza" },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "Not yet", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "locked",
      "name": "Sky-Altar - sealed",
      "priority": 10,
      "default": true,
      "say": [
        "The altar is sealed to you. Its inscription reads: only one who has hardened their scales in the wyrm-shrine below, and earned the keep above, may call the sky. Come back when the Dragon Shrine has weighed you.",
        "The wind up here is a warning, not an invitation. Not yet. Clear the Dragon Shrine and take the keep, and the stone may open to your hand."
      ]
    }
  ]
}
```

**DATAPACK NEEDS:**
- `function/noble/rayquaza/crystal_launch.mcfunction` — `execute as @p[distance=..8] at @s run
  noble start rayquaza` (Dragon Shrine Crystal repoint; same audit fix as the birds).

Note the `all_tags` gate (`mem_gte_8` AND `defeated_dragon_shrine_leader`) is the schema
AND-only multi-tag form — both must be present for the `ready` entry to win over `locked`.

**QUEST_TARGETS entry:**

```json
{
  "holder": "q.side_sky",
  "name": "What Falls From the Sky",
  "slot": 44,
  "stages": [
    {
      "if_tags": ["mem_gte_8", "defeated_dragon_shrine_leader"],
      "not_tags": ["defeated_noble_rayquaza"],
      "label": "Raise your hand at the Ryujin sky-altar",
      "target": { "npc": "noble_monument_rayquaza" },
      "note": "Double gate (gym 8 + Dragon Shrine). Spire top PROPOSED [2156,240,884] above the Draconis leader coord ~[2156,201,884]."
    },
    {
      "if_tags": ["mem_gte_8"],
      "not_tags": ["defeated_dragon_shrine_leader", "defeated_noble_rayquaza"],
      "label": "Clear the Dragon Shrine to unseal the sky-altar",
      "target": { "npc": "dragon_shrine_leader" },
      "note": "Points the player at the shrine prerequisite first (Draconis). FIRST MATCH WINS puts the double-gated stage above this one."
    }
  ]
}
```

**REWARD/BALANCE.** `rayquaza.json` `battleSpecies` → **`rayquaza level=66`** (cap 68). Arena
`center` = **spire top PROPOSED `[2156, 240, 884]`**. `rayquaza.json` is a flyer (authored).
Reward: `training_grand` + `defeated_noble_rayquaza`.

---

## 4. Recognition & economy beats

Nobles are **outside** the Company economy — `cd_instability` is untouched and no
`fields_liberated` gates apply, with two deliberate exceptions used as *flavour back-echoes*
(not mechanics):

- **Zapdos giver (Cass)** name-checks `fields_liberated` in prose (*the grain-lights came back on
  out east*). This is a **line, not a gate** — she reads the same to a player who liberated zero
  fields; it just lands harder for the audience that did the Wheat War. Optional hardening: gate
  the phrase behind `{ "fields_liberated": { "op": "gte", "value": 1 } }` as a fan-out variant.
  Crucially she recognises the player as *the field-clearer*, **never** as the founder.
- **Villain recognition:** the giver props are **civilians / lore-keepers**, so they do **not**
  run the Company recognition gradient — none of them can recognise the founder. The shrine
  keepers (Glacius, Ignis, Draconis) carry the special "the land remembers you / old gravity"
  tier from `14_shrines` — that is where the recognition arc touches this unit, and it is oblique
  (never names the Founder; no Founder name surfaces before Act 3).

**Rumor-hub hooks (house style — one per set-piece):**
- **Safari (Mew):** the Safari gatekeeper civilian gains a line gated on
  `not_tag defeated_noble_mew` + `recognition:"mid"`: *Folks swear something pink flits through
  the tall grass out here - never stands still long enough to prove it.* Sets `heard_mew_rumor`
  on read via a one-line command action (`tag @initiator add heard_mew_rumor`, run as NPC) —
  optional; Mew can also stay a pure hidden discovery with no sidebar line (see §7 Q5).
- **Gaviota (Kyogre):** the Gaviota nurse/greeter gains a line gated on `mem_gte_5` +
  `not_tag defeated_noble_kyogre`: *The old warning buoy off the north pier has been swinging its
  lantern again - sailors give it a wide berth.*
- **Cyber City (Zapdos):** Cass is both giver and hub. **Reconcile with `05_cyber_city.md`**,
  which already declares `cyber_nurse_rumor` (Nurse Ampere) as the town RUMOR HUB — add the
  Zapdos pointer as one more gated line on Nurse Ampere (`mem_gte_7` + `not_tag
  defeated_noble_zapdos`: *Grid Warden Cass is out on the western pylons, cursing the sky - she
  says the storm-bird is one strike from taking the whole grid.*) so the town hub also points at
  it, not only Cass herself.
- **Volcano/Ryujin:** the crater stone and sky-altar are their own hubs (monuments).

**Streamable receipt beats.** The engine already fires `completeTitle`/`completeSubtitle` on each
noble — set them for a punchier title-card where wanted, e.g. in `groudon.json`
`completeTitle: "§6§lTHE MOUNTAIN QUIETS"`, `kyogre.json` `completeTitle: "§3§lTHE SEA LIES
STILL"`. No extra `announce` actions are needed; the launch button is the only authored beat and
the engine owns the payoff card. (No `%` or apostrophe appears in any title string.)

---

## 5. New tags / scores introduced

| tag / score | set by | gated by |
|---|---|---|
| `defeated_noble_mew` | noble engine reward (`mew.json` storyFlag) | Mew after-entry, sidebar done-state |
| `defeated_noble_articuno` | noble engine (`articuno.json`) | Ice after-entry, sidebar |
| `defeated_noble_moltres` | noble engine (`moltres.json`) | Fire after-entry, sidebar |
| `defeated_noble_zapdos` | noble engine (`zapdos.json`) | Cass after-entry, sidebar |
| `defeated_noble_kyogre` | noble engine (`kyogre.json`) | buoy after-entry, sidebar |
| `defeated_noble_groudon` | noble engine (`groudon.json`) | stone after-entry, sidebar |
| `defeated_noble_rayquaza` | noble engine (`rayquaza.json`) | altar after-entry, sidebar |
| `heard_mew_rumor` (optional) | Safari gatekeeper rumor line | Mew sidebar visibility |
| `mem_gte_5/7/8/10` | **existing** compiler band-tag tick (`memory_fragment`) | giver quest stages |
| `defeated_ice_shrine_leader` | **existing** (shrine engine) | Articuno button |
| `defeated_fire_shrine_leader` | **existing** | Moltres button |
| `defeated_dragon_shrine_leader` | **existing** | Rayquaza altar |

> Every `defeated_noble_<id>` flag already exists as the noble JSON `rewards.storyFlag.objective`
> (a scoreboard `defeated_noble_<id> = 1`). Whether it is *also* a player tag the dialog gate can
> read depends on the band-tag/scoreboard lowering — **see Open Question 1**. If it is
> scoreboard-only, the dialog gates and sidebar `not_tags` above must use `{ "score": {
> "objective": "defeated_noble_<id>", "op": "gte", "value": 1 } }` instead of `{ "tag":
> "defeated_noble_<id>" }`. Flagged for the synthesis pass to lock uniformly.

---

## 6. Build checklist

1. **Set arena coords + retune levels** in the 7 `noble_encounters/*.json` (the load-bearing
   change — without this the boss spawns at player pos and at L70):
   - `mew`: `center` (Safari, safe interior is fine — chase) `[1300,64,1450]`, `battleSpecies mew level=35`.
   - `articuno`: `center [3644,68,1960]`, `articuno level=72`.
   - `moltres`: `center [3510,51,4702]`, `moltres level=82`.
   - `zapdos`: `center [1393,66,1065]`, `zapdos level=60`.
   - `kyogre`: `center [700,62,3330]`, `kyogre level=50`.
   - `groudon`: `center [3805,70,3746]`, `groudon level=78`.
   - `rayquaza`: `center [2156,240,884]`, `rayquaza level=66`.
   (`"dimension":"minecraft:overworld"` is already present.) **Builder confirms each PROPOSED
   coord is standable/visible before final.**
2. **Drop 5 new character files** under `dialog-src/characters/legendary/`:
   `noble_giver_mew_wisp`, `noble_giver_zapdos_warden`, `noble_monument_kyogre`,
   `noble_monument_groudon`, `noble_monument_rayquaza` (§2, §3 blocks). Confirmed **none of these
   ids exist** in `dialog-src/characters/**` today (no duplication).
3. **Drop 5 new dialog files** under `dialog-src/dialog/`: `noble_mew_chase`,
   `noble_zapdos_defense`, `noble_kyogre_buoy`, `noble_groudon_stone`, `noble_rayquaza_altar`.
   Confirmed **none exist** in `dialog-src/dialog/**` today.
4. **Edit 2 existing shrine dialogs** — replace the `after` entry of
   `dialog-src/dialog/shrine_ice.json` (Articuno) and `dialog-src/dialog/shrine_fire.json`
   (Moltres) with the §3.2 / §3.3 blocks. These **preserve the existing frost/ember reward
   lines** and only add the noble button + a third self-contained `say` variant. Do **not** touch
   the `default` challenge entries.
5. **Add 3 crystal-launch functions** (optional but recommended — resolves the shrine
   double-crystal / flat-L70 bug in `14_shrines §Crystal↔noble handshake` +
   `docs/roadmap/18_shrines_audit.md §7`): `function/noble/{articuno,moltres,rayquaza}/
   crystal_launch.mcfunction`, each one line `execute as @p[distance=..8] at @s run noble start
   <id>`. Then repoint the Ice / Fire / Dragon `ShrineCrystalItem` to run that function instead
   of `spawnpokemon <species> level=70` (a small Java edit in `ShrineCrystalItem` — swap the
   `spawnpokemon` command for `function cobblemon_initiative:noble/<id>/crystal_launch`).
   **Java change — flag to mod-side backlog if this is an authoring-only pass.**
6. **Add 7 side-holder stages** to `dialog-src/registers/quest_targets.json` (§3 blocks:
   `q.side_wisp/gale/ember/grid/deep/mountain/sky`). Slots **44–50 are free** — verified the
   existing register uses slots 57–100 only, so 44–50 collide with nothing.
7. **Add rumor-hub lines** (§4) to the Safari gatekeeper civilian dialog + the Gaviota
   nurse/greeter dialog + the Cyber City `cyber_nurse_rumor` (Nurse Ampere) dialog in
   `05_cyber_city.md` — one gated entry each.
8. **Verify `noble` is in the security allowlist** — it already is
   (`EasyNpcSecurityConfig.REQUIRED_ROOTS`, alongside `riftdragon`/`cutscene`). No config change.
9. **Compile:** `scripts/content_compile` → `scripts/update_preset_index` →
   `scripts/generate_npc_function` → `gradle build`, bump the alpha suffix, write
   `GIT_COMMIT_MSG`, user runs `gcommit`.
10. **Runtime verify** (per `docs/NOBLE_ENCOUNTERS.md` Verify): walk to each site, confirm the
    giver latch-spawns, the gated button appears only when the unlock is held, pressing it opens
    the noble at the SITE (not player pos), the cap-legal battleSpecies catches,
    `defeated_noble_<id>` latches, and the after-entry flips. Test one flyer (Rayquaza/Zapdos)
    and the chase (Mew).

---

## 7. Open questions for showrunner

1. **`defeated_noble_<id>` — tag or scoreboard-only?** The noble JSON writes a *scoreboard*
   (`rewards.storyFlag`), but the dialog gates here read it as a *tag*. Confirm the lowering so
   every after-entry / sidebar `not_tags` / crystal gate reads the flag the same way (see §5
   note). If scoreboard-only, add a one-line tick `execute as @a[scores={defeated_noble_<id>=1..}]
   run tag @s add defeated_noble_<id>`, or switch all gates to the `score` form.
2. **Should the shrine crystals be repointed to `noble start` (build step 5)?** This is the clean
   fix for the `14_shrines`/`18_shrines_audit` double-crystal + flat-L70 bug — the crystal becomes
   a *portable noble launcher* (gated, cap-legal, one arena) instead of a raw L70 wild dump. It
   requires a small Java edit. Approve, or keep crystals as legacy raw spawns and gate nobles by
   dialog only?
3. **Level retune sign-off.** Proposed caps-under levels: Mew 35 / Kyogre 50 / Zapdos 60 /
   Rayquaza 66 / Articuno 72 / Groudon 78 / Moltres 82. Confirm — especially Kyogre at exactly cap
   50 (a cap-*equal* catch is legal but tight) and Moltres 82 (post-league, cap 85).
4. **Groudon gate depth.** `mem_gte_10` (strict post-gym-10) or the looser `recognition:"late"`
   (≥7)? The crater is deep south-east; a ≥10 gate keeps it a genuine endgame set-piece. (If the
   looser gate wins, change the §3.6 quest stage `if_tags` from `mem_gte_10` to `mem_gte_7`.)
5. **Mew sidebar visibility.** Ship the `heard_mew_rumor` rumor line + sidebar stage, or keep Mew
   a pure hidden Safari discovery with no waypoint (higher delight, lower discoverability)?
6. **Kyogre / Groudon / Rayquaza trio capstone.** All three back-echo each other; do you want a
   `weather_trinity` achievement + a one-time grand reward when all three `defeated_noble_*` flags
   are set? (Needs one tiny check function — trivial once Q1 is resolved.)
7. **Missing nobles.** Only 7 are shipped (`NOBLE_IDS`); the docs mention future pseudo-legendaries
   (Tyranitar/Garchomp/Dragonite/Metagross/Salamence/Hydreigon) and other legendaries. Out of
   scope here — this unit gates the shipped seven. Confirm no others need gating this pass.
8. **Zapdos site vs `05_cyber_city.md`.** The Cyber City edge coord `[1393,66,1065]` and Cass role
   should reconcile with `05_cyber_city.md` (whose NPC cluster sits ~[1470–1610,65,1100–1150] and
   which already owns the STORMFRONT/Zapdos marquee reference and the `cyber_nurse_rumor` hub).
   Confirm no double-placement and that Cass sits on the western pylon edge, not on top of an
   existing Cyber NPC.
