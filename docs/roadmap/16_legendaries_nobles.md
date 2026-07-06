# 16 — Legendaries: Nobles & Events (cross-cutting)

> Area key: `legendaries_nobles`. This is the **reference doc** other area agents point
> to for legendary mechanics. **SHOWRUNNER RULING (2026-07-06) is baked in:** every
> legendary is one of exactly **two archetypes** — a **NOBLE** (battle legendary: prompt
> or monument trigger → PvP trainer phase → the legendary spawns as a **normal wild
> Pokémon** you catch or battle, full Nuzlocke stakes) or an **EVENT** (join legendary —
> Mew, Celebi, Manaphy: finish the event, then **click** the Pokémon to have it join your
> party). Town-local staging for a legendary that sits inside a gym zone lives in that
> gym's roadmap file (Groudon's crater dressing → `12_scorchspire.md`; Kyogre's pier
> staging → `06_gaviota_port.md`; Zapdos's city defense → `08_cyber_city.md`); the
> **trigger/PvP/spawn wiring spec is canonical here.**
>
> **Grounding note:** the battle pipeline (TBCS `do:battle` → onwin tags), the rctmod
> team-file format, the placement-latch spawn system, band-tag gates, and the
> **starter stand-in claim pattern** (the EVENT engine) are all **shipped and cited
> below**. No legendary trainer/team/character files exist yet — this plan is net-new
> content on proven rails. Every coordinate is either lifted from `install.json` (cited)
> or marked **PROPOSED (needs builder confirm)**. `spawnpokemonat`/`givepokemonother`
> grammar is **UNVERIFIED (jar-verify)** — it is used in shipped dialog content but every
> shipped use carries the same UNVERIFIED annotation and it is not recorded in
> `docs/ENGINE_FINDINGS.md` (see §8 gotchas).

---

## 1. Concept & fantasy

**One-line pitch:** *Between the badges, the run stops being about trainers and remembers
the world has gods in it. Some of them are storms you are warned about and then have to
face — beat the people defending the ground, and the god itself steps out of the weather
as a real wild Pokémon, one hardcore run, one throw. Others are gifts: play their game to
the end, and they walk over and ask to come along.*

**The ruling's two shapes, in stream terms:**

- **Nobles are earned in two beats.** First a human beat — *"will you help us defend
  Cyber City from Zapdos?"*, or a monument that reads **"WARNING: the sea seems violent
  in these parts"** — followed by a PvP gauntlet of defenders/guardians. Then the wild
  beat: the legendary spawns as a **normal wild Pokémon**. No boss engine, no scripted
  mercy. The player chooses on camera whether to throw a ball at a god under full
  hardcore + Nuzlocke stakes. That choice *is* the content.
- **Events are joy.** Mew makes you chase her across the Safari Zone and then decides you
  were fun. Celebi takes an offering of berries in a blossom grove. Manaphy waits at the
  heart of a drowned temple for anyone stubborn enough to reach it. Finish the event,
  **click them, they join** — zero RNG, zero stakes, pure delight.

Marquee stream moments:

- **The mountain wakes (Groudon).** The crater-rim monument warns you; the crater wardens
  test you; then the volcano's heart surfaces as a *catchable, killable, run-endangering*
  wild titan. "THE MOUNTAIN QUIETS" — or the clip where it didn't.
- **The violent sea (Kyogre).** The canonical monument noble: a warning buoy off Gaviota,
  a storm-watch gauntlet on the pier, and a leviathan surfacing in open non-safe water.
- **The defense of Cyber City (Zapdos).** The canonical prompt noble: an NPC asks for
  help, the city fields its defenders alongside you, and the storm-bird comes down to the
  grid as a wild spawn when the line holds. (Staged in `08_cyber_city.md`.)
- **The chase (Mew).** Pure comedy-of-menace relief — hide-and-seek across the Safari
  Zone, ending with a click, not a throw.
- **The weather trio delta.** Groudon, Kyogre and Rayquaza remain a **set** — but the
  capstone now honours actual *catches* (see §7 and the capture-detection gap in §8).

---

## 2. Narrative role

| Field | Value |
|---|---|
| Act | **Cross-cutting, spread by level-cap gate** (see §3). Encounters unlock as the ladder opens the surrounding regions; they are optional spectacle, not spine beats. |
| `cd_instability` | **Index-neutral by default.** Legendaries are not economy beats and do not move the index. *Optional hook:* a Company "asset-extraction crew" framing can supply the **PvP phase** of a noble (fight the drillers, not just guardians) — a showrunner decision (§9), not a baseline. |
| Memory fragment | **None by default** — fragments are gym-gated 1..10 and canon (LORE_BIBLE §8; do not add an 11th). *Optional:* **Uxie** (memory sprite) may fire a single **Dark-Urge-tier echo whisper** on encounter — reuses the existing whisper system, grants no fragment. Showrunner decision. |
| Recognition tier | **n/a for the Pokémon** (wild legendaries do not recognise the founder; civilians never do — LORE_BIBLE §4). Prompt-givers and PvP defenders use the era-appropriate tier of the gate they sit behind (early/mid/late). |
| Canon ties | The **weather trio** (Groudon/Kyogre/Rayquaza) is a self-contained "delta" arc. **Uxie** rhymes with the amnesia motif (a mind that lost itself). The Company *covets raw power it could weaponise* — the extraction-crew option lets a noble's PvP phase touch the villain plot without becoming it. Kyogre stays "the un-auditable asset" (see `06_gaviota_port.md` §2). Groudon's buried rage echoes the shadow-self motif already seeded in the Scorchspire fragment. |

---

## 3. Layout & placements — the roster

**Ten encounters** — seven NOBLES, three EVENTS. Zones and anchor coords are cited from
`install.json`; trigger/spawn points inside those zones are **PROPOSED** (need a builder
to confirm a standable/visible spot). Nothing here assumes new terrain — **Manaphy's
temple already exists on the map** (ruling 2026-07-06: *"there is a temple already on
the map I will just need to provide cords"*); its coords are TBD from the showrunner.

**Noble stakes rule (from the ruling):** the wild spawn is a **normal wild Pokémon** —
full Nuzlocke stakes, standard wild rules; the player chooses whether to risk it. So
noble **spawn points must sit in `mobsSpawn:true` (non-safe) zones** where Nuzlocke is
live: Gullwing Coast, Volcano Peak and Mystic Marsh are all `mobsSpawn:true`
(`install.json`). Event claims carry no stakes, so safe zones (Safari Zone is
`mobsSpawn:false`) are fine for them.

| # | Legendary | Archetype · trigger | Zone (source) | Anchor / spawn coord | Cap-gate era |
|---|---|---|---|---|---|
| L1 | **Groudon** (Ground) | NOBLE · **monument** at the crater rim ("WARNING: the mountain is not asleep") | Volcano Peak `LANDMARK` (center `[3805,64,3746]`, r=87, `mobsSpawn:true`) | rim monument **PROPOSED `[3805, ~110, 3746]`**; wild spawn in the crater bowl | post-gym-10 (cap 80) — see `12_scorchspire.md` §5 SQ2 for the gate fork |
| L2 | **Kyogre** (Water) | NOBLE · **monument** — the warning buoy: *"WARNING: the sea seems violent in these parts"* (canon line) | Gullwing Coast `ROUTE 7` (`mobsSpawn:true`), offshore N of Gaviota | buoy **PROPOSED `[630,63,3436]`** pier-end; wild spawn **PROPOSED `[700,62,3330]`** open water | post-gym-5 (cap 50) — staging in `06_gaviota_port.md` §5B |
| L3 | **Zapdos** (Electric/Flying) | NOBLE · **prompt** — *"will you help us defend Cyber City from Zapdos?"* (canon line) | Cyber City (gym 7) — **staged in `08_cyber_city.md`** (that file is being updated in this same pass; cross-reference only here) | defense line + wild spawn at the city edge, **outside the safe hull** (coords owned by `08_cyber_city.md`) | post-gym-7 (cap 62) |
| L4 | **Rayquaza** (Dragon/Flying) | NOBLE · **monument** — the sky-altar atop the keep | Ryujin Keep spire (leader `[2156,201,884]`) + Dragon Shrine `[2008,66,921]` | altar **PROPOSED `[2230, ~240, 970]`**; wild spawn on the spire top | post-Dragon-Shrine / gym-8 (cap 68) |
| L5 | **Regirock** (Rock) | NOBLE · **monument** — the braille pillar ("WARNING: the door in the desert is patient") | Oasis `LANDMARK` (center `~[1735,64,4255]`) + Ground Shrine `[1910,83,4049]` | pillar **PROPOSED `[1735, ~64, 4255]`**; wild spawn in the buried chamber **PROPOSED `[1735, ~40, 4255]`** | post-gym-6 (cap 56) |
| L6 | **Uxie** (Psychic) | NOBLE · **monument** — the still-water stone ("WARNING: the pond is too still here") | Mystic Marsh (`TOWN` but `mobsSpawn:true` — stakes are live; leader `[1073,65,2441]`) | islet stone **PROPOSED `[1050, 63, 2470]`**; wild spawn on the islet | post-gym-3 (cap 37) |
| L7 | **Mew** (Psychic) | EVENT · **chase** → click to join | Safari Zone `LANDMARK` (center `~[1370,64,1590]`, `mobsSpawn:false`) | 5 chase nodes across the Safari (**PROPOSED**, §5) | post-gym-7 (cap 62) |
| L8 | **Celebi** (Psychic/Grass) | EVENT · **berry offering** → click to join | **PROPOSED grove clearing off Blossom Path** (`ROUTE 1`, `install.json`: hull x 1923–2303, z 2564–2876) | grove shrine **PROPOSED `[2050, ~64, 2700]`** | **PROPOSED post-gym-4 (cap 44)** — era is open tuning (§9) |
| L9 | **Manaphy** (Water) | EVENT · **water-temple traversal** → click to join | **EXISTING in-map underwater temple** (ruling 2026-07-06) — **coords TBD from showrunner** | temple heart chamber | post-gym-5 (cap 50) |
| L10 | **GIRATINA** (Ghost/Dragon — **RESOLVED**, ruling 2026-07-06: *"Giratina will be in the deep dark"*) | NOBLE · **monument** (`dd_monument` at the cave mouth) — the challenge leg is the **Warden descent itself** (environmental; no trainer ladder) | Deep Dark Cave `BATTLE_FRONTIER` zone — **staged in `17_deep_dark_cave.md` §5.4** (that file owns all coords) | wild spawn off the vault, **outside every SafeZone hull** (full stakes) | optional endgame, L100-band; gate `dd_reached_vault` |

**Props/NPCs table** (all use the shipped placement-latch or stand-in patterns — no
terrain except the Manaphy temple):

| Key | Kind | Coord | Confirmed? | Role |
|---|---|---|---|---|
| `monument_groudon` | placement prop (warning + PvP + call) | `[3805,~110,3746]` | PROPOSED | Crater-rim warning stone; runs the Groudon noble chain. |
| `warden_ashkeeper` (+1–2 crater wardens) | Easy NPC PvP defenders (`do:battle`) | crater rim, near monument | PROPOSED | Groudon's PvP phase. |
| `monument_kyogre` | placement prop — the **warning buoy** | `[630,63,3436]` | PROPOSED | *"WARNING: the sea seems violent in these parts."* Runs the Kyogre chain. |
| `stormwatch_1..2` | Easy NPC PvP defenders | Gaviota north pier | PROPOSED | Kyogre's PvP phase (see `06_gaviota_port.md` §5B). |
| *(Zapdos cast)* | prompt NPC + defense gauntlet | Cyber City | owned by `08_cyber_city.md` | The prompt noble. Cross-reference only. |
| `monument_rayquaza` | placement prop — sky-altar | `[2230,~240,970]` | PROPOSED | Warning + call; Rayquaza chain. |
| `keepwatch_1..2` | Easy NPC PvP defenders | keep top | PROPOSED | Rayquaza's PvP phase. |
| `monument_regirock` | placement prop — braille pillar | `[1735,~64,4255]` | PROPOSED | Warning + wait-puzzle + call; Regirock chain. |
| `oasis_warden_1..2` | Easy NPC PvP defenders | Oasis | PROPOSED | Regirock's PvP phase. |
| `monument_uxie` | placement prop — still-water stone | `[1050,63,2470]` | PROPOSED | Warning + call; Uxie chain (single light guardian). |
| `mew_node_1..5` | COBBLEMON_ENTITY stand-ins (chase chain) | Safari (PROPOSED) | PROPOSED | Chase waypoints; node 5 = claim stand-in (§5). |
| `celebi_shrine` + `celebi_standin` | placement prop + COBBLEMON_ENTITY stand-in | Blossom Path grove (PROPOSED) | PROPOSED | Berry-offering gate + claim body. |
| `manaphy_standin` | COBBLEMON_ENTITY stand-in | temple heart — **coords TBD from showrunner** (temple exists in-map) | awaiting coords | Claim body at the end of the traversal. |
| *(Deep-Dark cast)* | monument prop + wild spawn | Deep Dark Cave | owned by `17_deep_dark_cave.md` | The L10 monument noble (environmental challenge). Cross-reference only. |

---

## 4. Core structure — the two-archetype engine

### 4.0 Feasibility status

An earlier design-pass decompile of the pinned Cobblemon jar
(`.gradle/loom-cache/remapped_mods/…/mod-*-1.7.3+1.21.1.jar`) reported: no native
noble/totem/boss AI (`legendary`/`mythical`/`totem` are dex **labels**, not battle
mechanics); `spawnpokemon <properties> [pos]` (aliases `pokespawn`, `spawnpokemonat`,
`pokespawnat`) and `givepokemon`/`givepokemonother` at perm level 2; `PokemonProperties`
parsing `species level shiny nature ability ivs evs moves …`. **None of this is recorded
in `docs/ENGINE_FINDINGS.md` yet**, and every shipped use of these commands carries an
UNVERIFIED annotation (e.g. `function/sidequest/work_orders/fetch_success.mcfunction`:
"UNVERIFIED on Cobblemon 1.7.3: the givepokemonother command AND the gender property
key"). **Status: UNVERIFIED (jar-verify)** — re-verify and land the findings in
ENGINE_FINDINGS before building the first noble chain (§8 gotchas).

**The good news of the ruling:** the absence of a native boss system no longer matters.
There is **no custom boss-stat engine**. The noble engine reduces to:

```
trigger gate  →  PvP trainer ladder  →  one-shot latched wild spawn  →  despawn/cleanup rules
```

and the event engine reduces to the **shipped starter stand-in claim pattern**. Both are
rails this pack already runs.

### 4.1 Archetype A — NOBLE (battle legendaries)

Groudon, Kyogre, Zapdos, Rayquaza, Regirock, Uxie. Three beats:

**Beat 1 — trigger (one of exactly two forms, per the ruling):**

- **PROMPT** — an NPC asks for help. Canon example: *"will you help us defend Cyber City
  from Zapdos?"* A normal Easy NPC dialog with an accept button that sets the arming tag
  (`noble_<id>_armed`). Used for **Zapdos** (the defense fantasy needs a person asking).
- **MONUMENT** — a placed prop (placement-latch, dialog-only, zero block edits — the
  Tomo/notice-board pattern) whose first line is a **warning**. Canon example:
  *"WARNING: the sea seems violent in these parts."* Reading it / pressing the button
  sets the arming tag. Used for **Kyogre, Groudon, Rayquaza, Regirock, Uxie** — the
  lonely-warning shape fits remote set-pieces with no one around to ask.

**Beat 2 — the PvP phase.** Trainer battles through the shipped `do:battle`/TBCS
pipeline — either a **defense gauntlet** (waves framed as holding a line: Zapdos) or a
**guardian ladder** (sequential defenders gated on each other's `defeated_*` tags, same
shape as a gym ladder: everything else). 1–3 trainers per noble, real rctmod team files
(never empty `{}` stubs), prereq-gated on the arming tag + the era's `mem_gte_N` band.
This phase is where all the boss *dressing* now lives — bossbar, weather, titles (§4.3).

**Beat 3 — the wild spawn.** When the last PvP tag lands, a function fires **one
latched** spawn:

```
# UNVERIFIED (jar-verify) — grammar per §4.0; every shipped use is annotated UNVERIFIED
execute unless entity @a[tag=noble_<id>_called] run spawnpokemonat <x y z> <species> level=<N>
tag @a add noble_<id>_called
```

The legendary is now a **normal wild Pokémon**. Standard wild rules. **Full Nuzlocke
stakes** — the spawn point is in a `mobsSpawn:true` zone, Nuzlocke is live, a faint in
the catch attempt is a real death, and the player chooses whether to risk engaging at
all. *(This closes the old safe-zone-vs-full-stakes question: it is a normal wild
encounter; the risk decision belongs to the player, on camera.)* No gift fallback, no
guaranteed catch, no boss stats.

**Despawn/cleanup rules (the last piece of the engine):**

- Tag the spawned entity if reachable (**UNVERIFIED (jar-verify)** whether
  `spawnpokemon` output can be tagged in the same function via
  `tag @e[type=cobblemon:pokemon,distance=..R,sort=nearest,limit=1] add noble_<id>_mon`
  — and whether command-spawned `PokemonEntity` is persistence-flagged or subject to
  natural despawn timers).
- **KO rule (RESOLVED — ruling 2026-07-06):** a KO'd noble is **gone forever**. In the
  showrunner's words: *"that's the challenge — making sure you don't crit them during
  the catch phase."* No re-call after a KO; the crit risk IS the content. Implement:
  the KO path sets `noble_<id>_lost` and the monument's post-loss entry reads as an
  epitaph (the sea calms / the mountain sleeps), never a retry offer.
- **Re-call on non-KO exits (kept, PROPOSED):** a "call again" button gated on
  `noble_<id>_called` **AND** no `noble_<id>_mon` entity present **AND** no
  `caught_<id>` **AND** no `noble_<id>_lost` — so a natural despawn or an unresolved
  walk-away does not strand the beat. Only the KO is final.
- `caught_<id>` latching after a real wild catch needs a **capture-event hook that does
  not exist yet** — the mod's advancement package ships only `TrainerDefeatedCriterion`
  (`src/main/java/com/thecompanyinc/cobblemoninitiative/advancement/`). See §8.

### 4.2 Archetype B — EVENT (join legendaries: Mew, Celebi, Manaphy)

Finish the event, then **click the Pokémon and it joins your party**. Implementation is
the **shipped starter stand-in claim pattern**, verbatim (cited from
`dialog-src/characters/sango/starter_skiddo.json` + `dialog-src/dialog/starter_skiddo.json`
+ `function/ambient/tick.mcfunction`):

1. **The body** — an Easy NPC `cobblemon_npc`-model entity (COBBLEMON_ENTITY renderer,
   species-only model — ENGINE_FINDINGS renderer entry) with `entity_tags:
   ["ci_standin_<id>"]` in its character file, spawned by placement-latch or by the
   event's completion function.
2. **The claim dialog** — gated on the event-completion tag; the claim button runs (all
   bare `as_player` commands — ENGINE_FINDINGS: as_player commands must be BARE):
   `givepokemonother @s <species> level=<N>` *(UNVERIFIED (jar-verify), §4.0)* +
   `tag @s add claimed_<id>` + an actionbar announce + close. Exactly the Skiddo
   `choose_button` action list.
3. **The despawn** — `function/ambient/tick.mcfunction` already runs
   `execute if entity @a[tag=claimed_starter_skiddo] run kill @e[type=easy_npc:cobblemon_npc,tag=ci_standin_skiddo]`
   per stand-in; add one line per event legendary
   (`claimed_mew`/`ci_standin_mew`, etc.).

Zero RNG, zero stakes, no ball throw. The three events per the ruling:

- **MEW — the chase** (§5 Q-L7): multi-waypoint pursuit; she relocates on approach N
  times; the final node is the claim stand-in.
- **CELEBI — the berry offering** (§5 Q-L8): a grove shrine gated on offering berries;
  the offering opens the claim.
- **MANAPHY — the water temple** (§5 Q-L9): get through the temple; the claim stand-in
  waits in the heart chamber.

### 4.3 Streamable dressing (all datapack/command — no Java)

Now attached to the **PvP phase and the spawn moment**, not to a boss battle:

- **Bossbar** — a **dedicated new** id `cobblemon_initiative:noble_<id>` (ENGINE_FINDINGS:
  countdown bossbars need fresh dedicated ids — never reuse `ci_quest`). Red,
  `notched_10`, name "§cDEFEND — <PLACE>" during a gauntlet or "§cENRAGED — <SPECIES>"
  from arming to spawn; hide on resolution.
- **Arena reaction** — `weather thunder` + `title` "THE SEA TURNS VIOLENT" on arming;
  `playsound minecraft:entity.wither.spawn` at the wild spawn; on resolution: clear
  weather, "THE MOUNTAIN QUIETS", `playsound …ui.toast.challenge_complete`.
- **Signature theme (optional, UNVERIFIED (jar-verify))** — `battleTheme` exists as a
  field on species/NPC classes per the §4.0 scan; whether rctmod trainer JSON surfaces it
  for the PvP-phase trainers is unverified. Fallback: `playsound` loop from arming to
  resolution.
- ~~Giant scale~~ — `scaleModifier` NBT work is **dropped from scope**: the noble is a
  normal wild Pokémon by ruling; do not engineer a boss silhouette.

### 4.4 Per-encounter loop + tuning

Levels are set against the **CLAUDE.md ladder** (entry caps 15/22/30/37/44/50/56/62/68/74/80).
**New tuning rule (consequence of the ruling):** the wild noble must be **at or under the
player's current cap** — it is meant to be *caught and used*, not admired. Target =
**era cap − 0..2**. (The old +2..+4 overleveled-boss tuning applied to a warden battle
that no longer exists; the difficulty now lives in the PvP phase + the catch risk.) PvP
defenders follow the normal side-content band (ace ≈ cap − 1..0).

| # | Legendary | Archetype · trigger | PvP phase | Wild/claim level | Latch tags |
|---|---|---|---|---|---|
| L1 | Groudon | NOBLE · monument | Ashkeeper + 1–2 crater wardens (ladder) | **Lv 78** (post-badge cap 80; ~72 if pre-badge — gate fork in `12_scorchspire.md`) | `noble_groudon_armed/_called`, `caught_groudon` |
| L2 | Kyogre | NOBLE · monument (buoy) | storm-watch ×2 (pier ladder) | **Lv 50** (cap 50 — matches `06_gaviota_port.md` §5B) | `noble_kyogre_armed/_called`, `caught_kyogre` |
| L3 | Zapdos | NOBLE · prompt | city defense gauntlet (owned by `08_cyber_city.md`) | **Lv 60** (cap 62) — PROPOSED, reconcile with `08` | `noble_zapdos_armed/_called`, `caught_zapdos` |
| L4 | Rayquaza | NOBLE · monument (altar) | keep-watch ×2 (ladder) | **Lv 66** (cap 68) | `noble_rayquaza_armed/_called`, `caught_rayquaza` |
| L5 | Regirock | NOBLE · monument (pillar + wait-puzzle) | oasis wardens ×2 | **Lv 55** (cap 56) | `noble_regirock_armed/_called`, `caught_regirock` |
| L6 | Uxie | NOBLE · monument (still-water stone) | 1 light guardian | **Lv 36** (cap 37) | `noble_uxie_armed/_called`, `caught_uxie` |
| L7 | Mew | EVENT · chase | none | **Lv 60** claim | `mew_chase_stage` score, `claimed_mew` |
| L8 | Celebi | EVENT · berry offering | none | **Lv 40** claim — PROPOSED with the era (§9) | `celebi_offering_done`, `claimed_celebi` |
| L9 | Manaphy | EVENT · temple traversal | none | **Lv 50** claim | `manaphy_temple_done`, `claimed_manaphy` |
| L10 | **Giratina** (RESOLVED) | NOBLE · monument (challenge = the Warden descent — environmental, no ladder; `17_deep_dark_cave.md` §5.4) | none | **L100-band** (optional endgame; exempt from the cap−0..2 rule — the area is post-cap-100 content) | `dd_reached_vault` gate, `noble_giratina_called`, `caught_giratina` |

---

## 5. Quests & side quests

Gates use **tag/band-tag** only (numeric caps → derived band tags via
`band_tags.mcfunction`, ENGINE_FINDINGS §3): `mem_gte_N` (badge-count band) rather than a
raw cap; every "does-not-have" gate rides `EQUALS no_<x>` (Easy NPC ignores NOT_EQUALS).

**Q-L1 · "The Mountain Holds Its Breath" (Groudon — noble, monument).** The crater-rim
monument reads the warning; local staging + the SQ2 "Heat Death" hook (Aya *points* you
at the mountain — flavour, not the trigger) live in `12_scorchspire.md` §5. Chain:
monument (`mem_gte_10` recommended, fork in that file) → arm → guardian ladder
(Ashkeeper last) → wild Groudon Lv 78 spawns in the crater bowl (`mobsSpawn:true` —
full stakes) → catch it, KO it, or walk away. Resolution dressing "THE MOUNTAIN QUIETS"
fires when the entity is resolved. **Optional Company hook (§9):** the guardian ladder
can be *replaced* by an extraction-crew gauntlet (drive the drillers off the rim).

**Q-L2 · "Under the Storm" (Kyogre — noble, monument).** The warning buoy at the
pier-end: *"WARNING: the sea seems violent in these parts."* (canon line). Chain:
`mem_gte_5` → arm → storm-watch pier ladder ×2 → wild Kyogre Lv 50 surfaces at the
offshore point in Gullwing Coast (`mobsSpawn:true` — full stakes). Local staging, cast
and the Tide-Caller flavour NPC: `06_gaviota_port.md` §5B.

**Q-L3 · "The Defense of Cyber City" (Zapdos — noble, PROMPT).** The canon prompt:
*"will you help us defend Cyber City from Zapdos?"* An NPC asks; accepting arms the
defense gauntlet; holding the line spawns wild Zapdos at the city edge outside the safe
hull. **Entire staging owned by `08_cyber_city.md`** (being updated in this same pass) —
this doc owns only the archetype contract: prompt → gauntlet → latched wild spawn →
cleanup, tags `noble_zapdos_*`.

**Q-L4 · "What Falls From the Sky" (Rayquaza — noble, monument).** Sky-altar warning
atop Ryujin Keep. Chain: `mem_gte_8` **and** `defeated_dragon_shrine_leader` → arm →
keep-watch ladder ×2 → wild Rayquaza Lv 66 on the spire top. **Delta check:** resolving
any weather-trio noble runs `legendary/delta/check` (§7) — now keyed on real
`caught_*` flags (capture-detection gap, §8).

**Q-L5 · "The Buried Titan" (Regirock — noble, monument + puzzle).** The braille pillar
warns; the wait-puzzle (stand still N seconds — PROPOSED mechanic) opens the chamber;
oasis-warden ladder ×2; wild Regirock Lv 55 in the buried chamber. Opens the optional
**Regi-trio** thread (§9).

**Q-L6 · "Still Water" (Uxie — noble, monument, light).** The still-water stone on the
marsh islet warns that the pond is too still. One light guardian battle, then wild Uxie
Lv 36 on the islet — Mystic Marsh is `mobsSpawn:true`, so even the gentlest noble
carries real stakes. **Optional echo (§9):** one Dark-Urge-tier whisper on arming.

**Q-L7 · "Catch Me" (Mew — EVENT: the chase).** Giver: a giggle-in-the-grass prop,
Safari Zone (`mobsSpawn:false` — no stakes, by design). **Design (mechanics PROPOSED):**
5 relocating COBBLEMON_ENTITY stand-ins — approach/click node N → it giggles
(`playsound`), the node kills itself via its `ci_standin_mew_nodeN` entity tag, a
breadcrumb line lands, and the next node's placement-latch arms off the advanced
`mew_chase_stage` score (a per-stage gated chain on the shipped ambient placement
machinery — the per-stage gating is the PROPOSED part; fallback is one function per node
doing kill + summon-next explicitly). Node 5 **is the claim stand-in**: gate
`mew_chase_stage >= 5` (as a band tag), claim button = the Skiddo pattern → Mew Lv 60
**joins on click** ("it decided you were fun"). Gate: `mem_gte_7`. Achievement
`mew_befriended`. Tone: the one purely joyful legendary — corporate dread nowhere in
sight, on purpose.

**Q-L8 · "The Grove Remembers" (Celebi — EVENT: the berry offering; NEW).** Location:
a grove clearing off **Blossom Path** (Route 1, `install.json` — hull x 1923–2303,
z 2564–2876), shrine at **PROPOSED `[2050, ~64, 2700]`** (needs builder confirm; any
prettier wooded pocket on the route works). **Design (PROPOSED):** a mossy grove shrine
prop whose dialog asks for an **offering of berries** — proposed set: **3× each of five
Cobblemon berries** (e.g. Oran, Sitrus, Leppa, Lum, Pecha — **item ids UNVERIFIED,
verify from the Cobblemon jar** like the Gaviota bait item). **Offering gate mechanism:**
primary = dialog `has_item` gates on the offer button (EXPERIMENTAL —
`dialog-src/schema/README.md` §G25); hardened fallback (recommended) = the offer button
runs a bare `function …:legendary/event/celebi_offer` which counts via the shipped derby
idiom (`execute store result score … run clear @s <berry> 0` —
`function/sidequest/derby/turnin.mcfunction`), consumes only on success, and sets
`celebi_offering_done`. That tag reveals the **Celebi claim stand-in** at the shrine →
click → Celebi Lv 40 joins (Skiddo pattern). Era gate **PROPOSED `mem_gte_4`** (berries
are farmable early; a mythical join wants at least mid-game — §9). Flavour: the grove is
the one place the Company's surveys keep "losing" — time does not file correctly here.

**Q-L9 · "The Sunken Heart" (Manaphy — EVENT: the water temple).** The ruling: *get
through the water temple, then click Manaphy to join.* **The underwater temple already
exists on the map** (ruling 2026-07-06: *"there is a temple already on the map I will
just need to provide cords"*) — **await the showrunner's coords**, then anchor
everything to the real build. The event is the traversal itself — a breath-managed
dive gauntlet through the existing rooms (zero terrain work). Reaching the heart
chamber sets `manaphy_temple_done` (a placement prop / dialog latch at the door —
PROPOSED mechanism) and the **Manaphy claim stand-in** waits there → click → Manaphy
Lv 50 joins. *(The old egg-altar reward is superseded — the claim is the mon itself,
no egg, no hatch timer.)* Gate: `mem_gte_5`.

---

## 6. Trainers & teams needed

**The legendaries themselves need NO trainer files** — nobles are wild spawns, events
are claims. All trainer work is the **noble PvP phases**: human defenders/guardians on
the standard side-content recipe (ENGINE_FINDINGS §4 wiring: rctmod team file +
TrainerConfig entry + graph node; `name` MUST equal `displayName`; never ship empty `{}`
teams; cycle-check singles+groups after graph edits).

New registry folder: `data/cobblemon_initiative/trainers/legendaries/` (PvP-phase
trainers only). `category:"legendary"` is a new value — **UNVERIFIED (jar-verify)**
whether `ConfigLoader` accepts an unknown category (else reuse an existing one).

| Team file (`data/rctmod/trainers/`) | Role | Format | Band | Notes |
|---|---|---|---|---|
| `noble_groudon_warden_1.json`, `_2.json`, `noble_ashkeeper.json` | Groudon guardian ladder | GEN_9_SINGLES | 72–74 (ace ≈ 74) | Ashkeeper last; ground/fire flavour |
| `noble_kyogre_stormwatch_1.json`, `_2.json` | Kyogre pier ladder | GEN_9_SINGLES | 48–50 | water flavour; see `06_gaviota_port.md` |
| *(Zapdos gauntlet)* | city defense | — | ~60–62 | **owned by `08_cyber_city.md`** — do not double-author |
| `noble_rayquaza_keepwatch_1.json`, `_2.json` | Rayquaza ladder | GEN_9_SINGLES | 64–66 | dragon flavour |
| `noble_regirock_warden_1.json`, `_2.json` | Regirock wardens | GEN_9_SINGLES | 53–55 | rock/ground flavour |
| `noble_uxie_guardian.json` | Uxie light guardian | GEN_9_SINGLES | 35–36 | single battle, low `maxSelectMargin` aggression |
| *(Mew / Celebi / Manaphy)* | — | — | — | **no teams** — events have no battles |

Plus per-noble **character + dialog files**: the monument/prompt prop
(`dialog-src/characters/legendary/monument_<id>.json`, placement-latch, dialog-only) and
the PvP defenders (`battle.trainer` → the rctmod id). Event legendaries need the
**stand-in character** (COBBLEMON_ENTITY visuals via an in-world export to
`dialog-src/visuals/`, `entity_tags: ["ci_standin_<id>"]`) + claim dialog — copy
`sango/starter_skiddo.json` wholesale.

---

## 7. Economy & rewards

Legendaries are **not** a CD faucet — the mon *is* the reward. Keep prize money out of
the noble PvP onwin lists (defenders pay no CobbleDollars; they are a gate, not a
grind).

- **CD sinks (optional):** a revive/potion **restock merchant** near each noble trigger
  (the catch attempt is the expensive part now — balls and revives ARE the noble
  economy). No entry fees: the ruling's flow is warn → fight → spawn; a paywall in the
  middle reads wrong.
- **No training-pack loot** (ENGINE_FINDINGS §3: training packs are quest-completion
  payouts; a legendary is the reward).
- **Ball economy note (new, consequence of the ruling):** a wild-catch noble at full
  stakes makes **ball supply the real difficulty dial**. Make sure the era's shop tier
  stocks Ultra Balls by gym 5+ (coordinate with the shop-tier catalogs; no code change).
- **Delta capstone:** catching Groudon + Kyogre + Rayquaza fires `legendary/delta/check`
  → achievement `weather_trinity` + a one-time grand reward (showrunner to price it,
  §9). **Blocked on capture detection** (§8) — `caught_*` cannot be latched by datapack
  alone now that catches are real wild catches.

---

## 8. Implementation notes / FUTURE-ME HOOKS

**Exact files per NOBLE (Groudon shown):**

```
dialog-src/characters/legendary/monument_groudon.json        # warning monument prop (placement-latch, dialog-only)
dialog-src/characters/legendary/noble_ashkeeper.json         # PvP defender, battle.trainer=noble_ashkeeper
dialog-src/dialog/noble_groudon.json                         # warning entry + arm button + call-again entry
data/rctmod/trainers/noble_ashkeeper.json (+ warden_1/_2)    # PvP teams (copy takehara_leader.json shape); name==displayName
data/cobblemon_initiative/trainers/legendaries/groudon_wardens.json   # TrainerConfig entries
data/rctmod/mobs/trainers/single/noble_ashkeeper.json        # graph nodes; CYCLE CHECK after edit
…function/legendary/noble/groudon/arm.mcfunction             # tag + bossbar + weather dressing
…function/legendary/noble/groudon/spawn.mcfunction           # latched spawnpokemonat (UNVERIFIED jar-verify) + entity tag
…function/legendary/noble/groudon/cleanup.mcfunction         # resolution dressing + re-call latch logic
```

**Exact files per EVENT (Celebi shown):**

```
dialog-src/characters/legendary/celebi_shrine.json           # grove shrine prop (offering dialog)
dialog-src/characters/legendary/celebi_standin.json          # COBBLEMON_ENTITY stand-in, entity_tags ci_standin_celebi
dialog-src/visuals/celebi_standin.npc.snbt                   # in-world export (COBBLEMON_ENTITY renderer)
dialog-src/dialog/celebi_shrine.json + celebi_standin.json   # offering gate + claim (copy starter_skiddo.json)
…function/legendary/event/celebi_offer.mcfunction            # derby count idiom; consume on success; set tag
function/ambient/tick.mcfunction                             # +1 line: claimed_celebi → kill ci_standin_celebi
```

**Pipeline order (ENGINE_FINDINGS §3 — run in this order):**
`scripts/content_compile` → `scripts/generate_granary_tiers` → `scripts/update_preset_index`
→ `scripts/generate_npc_function` → `gradle build`, bump alpha suffix, `GIT_COMMIT_MSG`
(no Co-Authored-By), user runs `gcommit`.

**Patterns to copy:**
- **Claim (events):** `dialog-src/characters/sango/starter_skiddo.json` +
  `dialog-src/dialog/starter_skiddo.json` (claim button action list) +
  `function/ambient/tick.mcfunction` (claimed-tag → kill stand-in). This trio is the
  entire event engine.
- Dialog + `do:battle` (PvP phases): `dialog-src/dialog/shrine_dragon.json`;
  character battle block: `dialog-src/characters/shrine/dragon_shrine_leader.json`.
- Team file: `data/rctmod/trainers/takehara_leader.json`.
- Monument prop (dialog-only, zero block edits): the Tomo staged tag-chain /
  `takehara/rezoning_notice_board.json` notice-prop shape.
- Placement latch (no uuid → spawns once): any `placement:{x,y,z}` character
  (`station_pond.json`).
- Item-count gate: `function/sidequest/derby/turnin.mcfunction` (store-result `clear … 0`
  counts without consuming; clear only on success).

**Gotchas (do not relearn the hard way):**
- **`spawnpokemonat` / `givepokemonother` are UNVERIFIED (jar-verify).** Not in
  ENGINE_FINDINGS; every shipped use is annotated UNVERIFIED with a `spawnpokemon`
  fallback (`fetch_success.mcfunction`, `sq_deka.json`, `curator_tamiko.json`). Per the
  verify-from-jars rule: decompile the pinned Cobblemon 1.7.3 jar, confirm both commands
  + the property keys used, and **land the findings in ENGINE_FINDINGS** before building
  the first chain. Also jar-verify: can the spawned entity be tagged/persistence-flagged
  (despawn-timer question, §4.1)?
- **`caught_<id>` needs Java.** The advancement package has only
  `TrainerDefeatedCriterion` — no capture criterion. Latching a per-species caught flag
  off a real wild catch needs a small capture-event listener (Cobblemon capture event →
  player tag), or the delta capstone/cleanup lines that depend on it get deferred.
  **Flag to the mod-side backlog.**
- **Empty `{}` team = silent no-op battle** (rctapi `insufficientPokemon`). Author PvP
  teams before wiring buttons.
- **`name` (rctmod team) must equal `displayName` (TrainerConfig)** or wins miscredit.
- **onwin is winners-first** — key `1` / `@1` = the player who won. Arm/advance on key 1.
- **Numeric gates are illegal as raw conditions** — compile to band tags (`mem_gte_N`);
  negated gates ride `EQUALS no_<x>` (Easy NPC ignores NOT_EQUALS).
- **as_player commands must be BARE** (ENGINE_FINDINGS: ExecAsUser rejects redirect
  chains) — claim buttons run bare `givepokemonother`/`tag`/`function` commands, exactly
  like the Skiddo buttons.
- **Macro-delivered text has no escaping** — warning lines, actionbar receipts and onwin
  strings must contain **no double-quotes and avoid apostrophes**. Note: the canon
  warning line *"the sea seems violent in these parts"* is apostrophe-free as ruled;
  keep it that way in all variants.
- **Bossbar id must be fresh** — mint `cobblemon_initiative:noble_<id>`; never touch
  `ci_quest`.
- **Noble spawn points must be in `mobsSpawn:true` zones** so the ruling's full-stakes
  wild encounter is real (safe zones suspend Nuzlocke — CLAUDE.md). Verify each spawn
  coord against the zone hull, not the town name.
- **Latch-spawned NPCs get random UUIDs** → any NpcSight arming needs a manual
  `npcsight add <uuid>` pass after first spawn.
- **`ci_standin_*` tags live in the preset NBT** via the character `entity_tags` field —
  the ambient tick kill line matches on them; keep tag names in lockstep.

---

## 9. Dependencies & open questions

**Depends on (other area keys):**
- `mainline_spine` — band-tag/story-flag canon (`mem_gte_N`, `memory_fragment`), the
  whisper system Uxie's optional echo reuses.
- `gym_system_pvp_doubles` — the `do:battle`/TBCS/onwin pipeline and rctmod team format
  for all PvP phases.
- `scorchspire` — Groudon's crater staging + the SQ2 gate fork (`12_scorchspire.md`).
- `gaviota_port` — Kyogre's buoy/pier staging (`06_gaviota_port.md` §5B). Manaphy's
  underwater temple **already exists in-map** (coords TBD from showrunner).
- `cyber_city` — **owns the entire Zapdos prompt-noble staging** (`08_cyber_city.md`,
  updated in this same pass); this doc owns only the archetype contract + roster row.
- `deep_dark_cave` — **owns the entire L10 vault-legendary staging** (`17_deep_dark_cave.md`
  §5.4: `dd_monument` warning at the mouth, the Warden descent as the challenge leg, wild
  spawn off the vault **outside every SafeZone hull**, full stakes per the ruling). Species
  **RESOLVED: Giratina** (ruling 2026-07-06); this doc owns the L10 roster row, so the slot
  is not double-placed.
- `ryujin_keep` + `shrines_audit` — Rayquaza gate (`defeated_dragon_shrine_leader`);
  Regi thread touches Ground/Ice Shrines.
- `mystic_marsh` — Uxie's islet + the memory-echo thread.
- `kalahar_reach` — Regirock at the Oasis / Ground Shrine.
- `wheat_war_farms` + `company_hq` — **only if** the optional extraction-crew PvP-phase
  variant is greenlit.
- **Mod-side backlog** — the capture-event listener for `caught_<id>` (§8).

**RESOLVED by showrunner ruling (2026-07-06) — do not reopen:**
- ~~Nuzlocke stance~~ → **normal wild encounter, full stakes**; the player chooses
  whether to risk it. No safe-zone spectacle, no guaranteed-gift fallback for nobles.
- ~~Reward model per encounter~~ → **nobles are caught/battled as normal wilds; events
  (Mew, Celebi, Manaphy) join on click** after the event completes.
- ~~Warden-boss engine / boss stats / healing-bag phases / giant-scale models~~ → moot;
  the noble engine is trigger → PvP ladder → latched wild spawn → cleanup.
- ~~Manaphy egg~~ → superseded; the temple ends in a click-to-join claim.
- ~~Manaphy temple venue~~ → **the temple already exists on the map** (ruling
  2026-07-06); showrunner will provide coords. No terrain work, no fallback needed.
- ~~KO'd noble~~ → **gone forever** (ruling 2026-07-06: *"that's the challenge —
  making sure you don't crit them during the catch phase"*). Non-KO exits (walk-away,
  natural despawn) stay re-callable, §4.1.
- ~~L10 species~~ → **Giratina in the Deep Dark** (ruling 2026-07-06).

**Still-open decisions:**
1. **Manaphy temple coords:** awaiting the showrunner's coordinates for the existing
   in-map temple (anchor the latch, stand-in, and gate once they land).
3. **Company extraction hook:** may an individual noble's PvP phase be an
   extraction-crew gauntlet (villain-plot flavoured), or do all PvP phases stay
   civilian guardians?
4. **Uxie memory echo:** allow one Dark-Urge-tier whisper on the still-water monument, or
   keep legendaries fully out of the amnesia arc?
5. **Celebi era + berry set:** confirm `mem_gte_4`/Lv 40 (or push later), and the exact
   five-berry offering (ids to jar-verify).
6. **Zapdos level & timing** vs the `08_cyber_city.md` defense design — reconcile after
   that file lands.
7. **Regi-trio scope:** ship Regirock alone, or commit to Regice (Ice Shrine) +
   Registeel + a Regigigas gate as a long-form collect-a-thon?
8. **Delta capstone prize:** what does `weather_trinity` grant, and does it justify the
   capture-listener Java work it depends on?
9. **Which exact species** for the Mystic sprite — single **Uxie** (memory fit) vs the
   full Lake trio roaming the marsh (each as a light monument noble).
10. ~~L10 Deep-Dark species~~ → **RESOLVED: Giratina** (ruling 2026-07-06 — see the
    resolved block above). `17_deep_dark_cave.md` §5.4 stages it.
