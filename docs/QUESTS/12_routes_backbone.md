# 12 — Routes 3–16: The Connective Backbone

> **Slug:** `12_routes_backbone`
> **Owns:** the connective route content threaded *between* the towns and the wheat fields —
> the cap-legal spotter/trainer beats, the flavor NPCs (travelers, stranded merchants, lore
> keepers), the **route-facing villain-grunt placements** (a road agent per band), and — most
> importantly — the **field-guard → route mapping** that names *which farm sits on which route
> and which grunt guards it*. This is the single biggest Act1→Act2 blocker per the TODO: the
> HQ gate needs `fields_liberated >= 6`, and today only `farm_1` is placed. This doc makes the
> field-guard placements **concrete and route-anchored** so the wheat-war cast can be dropped
> in one pass.
> **Does NOT own:** the field mini-beat *wiring* itself (the perimeter-guard → site-manager
> loop, `liberation/free_field`, the trader/keeper commerce arcs, the field trainer teams) —
> that is `wheat_war_farms` / `docs/roadmap/03_wheat_war_farms.md`, the authoritative source
> for the twenty field-cast bodies. This doc **references** those ids, positions each field on
> its route, and adds the *route-side* connective tissue (spotters, flavor, road agents, the
> "route talks back" echoes). It also does **not** own town hubs (each town's rumor-hub NPC
> lives in that town's doc) or the gym interiors.

**Reading contract:** the field-guard/site-manager characters (`villain_yield_officer_N`,
`villain_site_manager_N`) are authored under `wheat_war_farms`. This doc's §**Field-Guard →
Route Map** is the *placement index* the showrunner uses to confirm each field's route + band
+ grunt, and its "route talks back" beats reference `field_freed[farm_N]` (the same latch
`free_field` sets). Everything new *this doc introduces* is route-side: spotters, flavor NPCs,
road agents.

---

## 1. Overview

R1 (Blossom Path / Meadow) and R2 (Harvest Road) are **DONE** — this unit mimics their
density, scaled per band. Their proven idioms are the template:

- **Spotter beat** = `sq_regular_spotter` (Bird Keeper Ayo): `recipe: trainer_spotter`,
  `engage: touch`, `sight:{mode:pursue,...,stop_tag}`, `battle.type: spotter`, has a
  `hail_line`, `decline_fee` (spotter is *walk-up*, so decline is the fairness escape).
- **Type-tip beat** = `sq_regular_typetip` (Bird Keeper Kwame): `recipe: type_specialist`,
  `battle.type: type_tip`, teaches a matchup, first-win item via the RCT registry (never a
  doubled `on_win give`).
- **Flavor NPC** = `granny_yun` / `old_deng`: `role: elder`/`civilian`, `recipe: civilian`,
  `movement: ambient_stationary_look`, dialog-driven, **never recognizes the player**, never
  says "wheat/grain" as a crop.
- **Road agent** = `villain_route_surveyor` (Corridor Assessor Yong): `role: villain_grunt`,
  the four-entry recognition block (late 30 → mid 20 → hail 15 → default 10) copied verbatim,
  `despawn_on_win`, opt-in fight (the road physically opens).

### Band context (this unit spans gyms 1→10 + Royal-League approach)

Each route inherits the band of the town it *leads to*. The route content is cap-legal for
that band; any above-band wager is opt-in + printed + decline-able (fairness floor).

| Route | Band town → | Level cap | frag | cd_instability | Grunt recognition | Adjacent farm |
|-------|-------------|:---------:|:----:|:--------------:|-------------------|---------------|
| R3 Willowmire | Mystic Marsh (G3) | 37 | frag_3 | →24 (end of "stable") | early→mid flickers | `farm_2` Mirebloom |
| R5 Quarry | Deepcore (G4) approach | 44 | frag_4 | →32 (prices "adjusting") | mid | — (Deepcore fighting country) |
| R7 Gullwing Coast | Gaviota (G5) | 50 | frag_5 | →40 | mid | `farm_3` Westwind |
| R8 Dunewind | Kalahar (G6) | 56 | frag_6 | →48 (traders may ambush ≥4) | mid | `farm_4` Dryrow |
| R10 Old Caravan | Central artery (Sango↔Kalahar) | 44–56 | — | 32–48 | mid | `farm_5` Crossroads |
| R12 Pylon Path | Cyber (G7) | 62 | frag_7 "You signed this charter" | →56 PEAK | mid→late | `farm_6` Fenceline |
| R13 Dragonspine | Ryujin (G8) / HQ road | 68 | frag_8 "You built it" | 25 (post-HQ) | late | `farm_7` Coldfurrow |
| R14 Frostveil | Nifl (G9) | 74 | frag_9 "They emptied you" | 25 | late | `farm_8` Frostfallow |
| R15 Cinderfall | Scorchspire (G10) | 80 | frag_10 "face your own signature" | 25 | late | `farm_10` Ashloam |
| R16 Frontier Causeway | Battle Frontier / post-RL | 85–100 | — | 25 | late (stand-downs) | `farm_9` Highfield |
| Road to Royal League | Royal League | 85 | — | 25 | late | `farm_9` Highfield (shared) |

**The arc job:** the routes are the *walk* between set-pieces — where the world talks back
(a spotter notes a liberated field), where the propaganda decays in real time (economy voice
gated on `cd_instability`), and where the recognition gradient plays on open ground (a road
agent who places your face mid-route). Every route plants the **next** town's hook and
**back-echoes** a prior beat. The field-guards are the load-bearing content: each guard's win
fires `free_field {field:farm_N}`, and this doc pins each to its route so the six pre-HQ
liberations are reachable on the natural path.

**Place on the route:** R3 opens the mid-game (post the R1/R2 opener); R12 is the hard turn
(frag_7, instability peak, HQ unlocks); R13–R15 are post-HQ, cd clamped at 25, recognition
mostly `late`; R16 / Road-to-RL are the endgame approach where some agents **stand down**.

---

## 2. Cast

`(NEW)` = introduced by this doc (route-side). `(REF)` = authored by `wheat_war_farms`, this
doc only positions/echoes it. Coords marked `PLACEHOLDER` need builder confirm against the
route polygons in `dev/updated-zones.json` / `install.json`; where the wheat-war doc already
proposed a farm coord, that literal is carried through.

| id | display_name | role | concept | anchor coord | src |
|----|--------------|------|---------|--------------|:---:|
| `r3_spotter_marsh` | Marsh Scout Reeta | duelist | R3 spotter, cap37, walk-up on the boardwalk pinch | PLACEHOLDER (Willowmire boardwalk) | NEW |
| `r3_lorekeeper_bittern` | Bittern-Watcher Odui | lore_keeper | R3 flavor, a bird-counter who reads the marsh like a ledger | PLACEHOLDER | NEW |
| `villain_yield_officer_2` | Ming-adjacent (Officer) | villain_grunt | R3 field guard, farm_2 perimeter | (1205, ~64, 2790) | REF |
| `villain_site_manager_2` | (Site Manager) | villain_grunt | R3 field guard, farm_2 site manager | (1229, ~64, 2820) | REF |
| `r5_trainer_quarry` | Rockbreaker Dala | duelist | R5 cap44 trainer, decline-able quarry duelist | PLACEHOLDER (Quarry rim) | NEW |
| `r5_merchant_stranded` | Stranded Hauler Poe | merchant | R5 flavor, cart-broke ore hauler; small trade, forward hook | PLACEHOLDER | NEW |
| `villain_route_agent_5` | Contractor (road) | villain_grunt | R5 road agent, refs `villain_grunt_3` team, mid band | PLACEHOLDER (Quarry road) | NEW |
| `r7_spotter_coast` | Gull-Keeper Hano | duelist | R7 cap50 coastal spotter, long clean sightline | PLACEHOLDER (Gullwing Coast) | NEW |
| `r7_traveler_tidepool` | Tidepool Walker Sena | civilian | R7 flavor, a beachcomber; back-echoes Gaviota | PLACEHOLDER | NEW |
| `villain_yield_officer_3` | (Officer) | villain_grunt | R7 field guard, farm_3 perimeter | (640, ~64, 3310) | REF |
| `villain_site_manager_3` | (Site Manager) | villain_grunt | R7 field guard, farm_3 site manager | (657, ~64, 3279) | REF |
| `r8_trainer_dune` | Duneracer Kito | duelist | R8 cap56 trainer, sand-sledder | PLACEHOLDER (Dunewind) | NEW |
| `r8_typetip_dune` | Sandreader Mafu | duelist | R8 type-tip (ground matchup) before Kalahar | PLACEHOLDER | NEW |
| `r8_flavor_caravan` | Waystone Keeper Idi | lore_keeper | R8 flavor, tends the desert waystones; scrubbing-artifact line | PLACEHOLDER | NEW |
| `villain_yield_officer_4` | (Officer) | villain_grunt | R8 field guard, farm_4 perimeter | (1548, ~78, 3822) | REF |
| `villain_site_manager_4` | (Site Manager) | villain_grunt | R8 field guard, farm_4 site manager | (1535, ~78, 3872) | REF |
| `r10_spotter_caravan` | Caravan Guard Boro | duelist | R10 mid-band spotter on the artery | PLACEHOLDER (Old Caravan road) | NEW |
| `r10_merchant_wagon` | Peddler Ndulu | merchant | R10 flavor, a road peddler; the artery hub, points at Crossroads | PLACEHOLDER | NEW |
| `villain_route_agent_10` | Operative (road) | villain_grunt | R10 road agent, refs `villain_grunt_4`, mid band | PLACEHOLDER (artery) | NEW |
| `villain_yield_officer_5` | (Officer) | villain_grunt | R10 field guard, farm_5 perimeter (Crossroads) | (2262, ~66, 3500) | REF |
| `villain_site_manager_5` | (Site Manager) | villain_grunt | R10 field guard, farm_5 site manager | (2309, ~66, 3540) | REF |
| `r12_spotter_pylon` | Line Tech Volta | duelist | R12 cap62 spotter under the power pylons | PLACEHOLDER (Pylon Path) | NEW |
| `r12_lorekeeper_pylon` | Grid-Reader Chike | lore_keeper | R12 flavor, reads the Company grid; frag_7 telegraph | PLACEHOLDER | NEW |
| `villain_route_agent_12` | Senior Agent (road) | villain_grunt | R12 elite road agent, refs `villain_grunt_5`, mid→late | PLACEHOLDER (Pylon Path) | NEW |
| `villain_yield_officer_6` | (Officer) | villain_grunt | R12 field guard, farm_6 perimeter (Fenceline) | (1520, ~72, 1762) | REF |
| `villain_site_manager_6` | (Site Manager, DOUBLES) | villain_grunt | R12 field guard, farm_6 site manager | (1570, ~72, 1735) | REF |
| `r13_trainer_spine` | Dragonrider Kaen | duelist | R13 cap68 trainer on the Dragonspine ridge | PLACEHOLDER (Dragonspine) | NEW |
| `r13_traveler_pilgrim` | Ashen Pilgrim Uzo | civilian | R13 flavor, on the HQ/Ryujin road; post-HQ cover-up leak | PLACEHOLDER | NEW |
| `villain_route_agent_13` | Elite Agent (road) | villain_grunt | R13 elite road agent, refs `villain_grunt_6`, late | PLACEHOLDER (Dragonspine) | NEW |
| `villain_yield_officer_7` | (Officer) | villain_grunt | R13 field guard, farm_7 perimeter (Coldfurrow) | (1870, ~64, 985) | REF |
| `villain_site_manager_7` | (Site Manager) | villain_grunt | R13 field guard, farm_7 site manager | (1925, ~64, 963) | REF |
| `r14_spotter_frost` | Frostwarden Neve | duelist | R14 cap74 spotter, Frostveil pass | PLACEHOLDER (Frostveil) | NEW |
| `r14_flavor_hermit` | Snowline Hermit Bran | lore_keeper | R14 flavor, a hermit who remembers the old face (stand-down seed) | PLACEHOLDER | NEW |
| `villain_route_agent_14` | Elite Agent (road) | villain_grunt | R14 elite road agent, refs `villain_grunt_7`, late | PLACEHOLDER (Frostveil) | NEW |
| `villain_yield_officer_8` | (Officer) | villain_grunt | R14 field guard, farm_8 perimeter (Frostfallow) | (3045, ~64, 2500) | REF |
| `villain_site_manager_8` | (Site Manager) | villain_grunt | R14 field guard, farm_8 site manager | (3066, ~64, 2478) | REF |
| `r15_trainer_ember` | Emberwalker Tavi | duelist | R15 cap80 trainer, Cinderfall approach | PLACEHOLDER (Cinderfall) | NEW |
| `r15_traveler_ashwalker` | Ash-Walker Odita | civilian | R15 flavor, a wanderer at the volcano skirt | PLACEHOLDER | NEW |
| `villain_route_agent_15` | Elite Agent (road) | villain_grunt | R15 road agent, refs `villain_grunt_8`, late (rattled) | PLACEHOLDER (Cinderfall) | NEW |
| `villain_yield_officer_10` | (Officer) | villain_grunt | R15 field guard, farm_10 perimeter (Ashloam) | (3245, ~66, 4020) | REF |
| `villain_site_manager_10` | (Site Manager) | villain_grunt | R15 field guard, farm_10 site manager | (3293, ~66, 4005) | REF |
| `r16_spotter_causeway` | Frontier Scout Rhea | duelist | R16 endgame spotter, Frontier Causeway | PLACEHOLDER (Frontier Causeway) | NEW |
| `r16_lorekeeper_causeway` | Causeway Chronicler Set | lore_keeper | R16 flavor, records who crosses; back-echoes the whole run | PLACEHOLDER | NEW |
| `villain_route_agent_16` | Elite Agent (stand-down) | villain_grunt | R16/RL road agent, refs `villain_grunt_9`, late STAND-DOWN | PLACEHOLDER (Royal-League approach) | NEW |
| `villain_yield_officer_9` | (Officer) | villain_grunt | R16/RL field guard, farm_9 perimeter (Highfield) | (3245, ~66, 3360) | REF |
| `villain_site_manager_9` | (Site Manager) | villain_grunt | R16/RL field guard, farm_9 site manager | (3300, ~66, 3354) | REF |

**Grunt-id reconciliation (READ — feeds Open Question 1):** the canonical `villain_grunt_3..11`
bodies are *already placed inside Company buildings* (Regional Office / Ops Center / HQ /
Boardroom — verified in `dialog-src/characters/villain/`). Only `villain_grunt_field_agent`
(=trainer `villain_grunt_1`) and `villain_grunt_2` sit on a route (Blossom Path, R1/R2). So the
route agents this doc introduces are **new character bodies that reference the existing villain
trainer teams** (`villain_grunt_3..9`) for their battles, *not* duplicates of the placed
building bodies. This gives the open road the escalating corporate ladder (Contractor →
Operative → Senior/Elite Agent) without moving the interior grunts. See Open Question 1 for the
ruling on whether to instead relocate some building grunts to routes.

---

## 3. Reusable Per-Route Template

Every route is the same four-slot pattern, band-scaled. This is the shape each `## Route`
subsection fills; author once, stamp eleven times.

```
ROUTE <id> (band = town it leads to)
  1. SPOTTER/TRAINER BEAT  — cap-legal, DECLINE-able (spotter = walk-up + decline_fee;
                             plain trainer = interaction + decline_fee; NEVER forced whiteout).
  2. FLAVOR NPC            — traveler | stranded merchant | lore keeper. civilian/elder/
                             lore_keeper/merchant. NEVER recognizes the founder. Plants the
                             NEXT town hook; carries a scrubbing-artifact line late.
  3. ROAD AGENT (grunt)    — villain_grunt, 4-entry recognition block (late 30 > mid 20 >
                             hail 15 > default 10), despawn_on_win, opt-in. references an
                             existing villain trainer id by band. NO pre-badge sight (armed
                             post-badge, R2 convention) — a fresh player is never ambushed.
  4. FIELD GUARD(S)        — the farm_N perimeter+manager (authored by wheat_war_farms). This
                             doc PINS which farm sits on this route and the "route talks back"
                             echo a nearby spotter gains once field_freed[farm_N] is set.
```

**Template rules (shared, non-negotiable):**
- Spotter battles carry a `hail_line` + `decline_fee` (walk-up has no free decline; the fee is
  the fairness escape). Plain-trainer beats are `engage`-less, interaction-driven, and also
  carry `decline_fee`. **No route battle whiteouts a starter-only player** — every one is
  decline-able or opt-in.
- Any wager/battle *above* the current cap is opt-in, the stake is **printed in the button
  text**, and declining costs CD (charge via pay-probe — engine work; specs only NOTE it).
- Flavor NPCs use `recipe: civilian` (or `trainer_one_time`-free lore keepers), never say
  "wheat/grain/yield" as a crop, and civilians NEVER recognize the founder.
- Road agents copy the `villain_route_surveyor` recognition block **verbatim** (late/mid
  entries are the shared canon text) and add one route-specific `*_hail` entry. `despawn_on_win`.
- "Route talks back" = a `STANDARD` entry on the route's spotter gated `tag:
  field_<N>_liberated` (see §5 tag table) that comments on the freed field. First match wins,
  so list it above the default.

---

## 4. Field-Guard → Route Map (THE Act1→Act2 unblock)

This is the deliverable's spine: **which farm, which route, which grunt, which band.** The
guard bodies are authored by `wheat_war_farms`; this table is the placement index that pins
each to its route so the six pre-HQ liberations land on the natural path. Seven Act-1 feeders
(`farm_1/2/3/4/5/6/7`) give six-of-seven slack; three Act-2 bonus fields
(`farm_8/9/10`) keep clawing cd down after DJ.

| Farm | Route | Band cap | Perimeter guard (opt-in, despawn) | Site manager (gated on guard, fires `free_field`) | Manager ace lv | Act |
|------|-------|:--------:|-----------------------------------|--------------------------------------------------|:--:|:--:|
| `farm_1` Firstfurrow | R2 Harvest Road *(BUILT)* | 30 | `villain_yield_officer` | `villain_site_manager` → `free_field {field:farm_1}` | 21 | 1 |
| `farm_2` Mirebloom | **R3 Willowmire** | 37 | `villain_yield_officer_2` | `villain_site_manager_2` → `free_field {field:farm_2}` | 29 | 1 |
| `farm_5` Crossroads | **R10 Old Caravan** | 44–56 | `villain_yield_officer_5` | `villain_site_manager_5` → `free_field {field:farm_5}` | 28 | 1 |
| `farm_3` Westwind | **R7 Gullwing Coast** | 50 | `villain_yield_officer_3` | `villain_site_manager_3` → `free_field {field:farm_3}` | 43 | 1 |
| `farm_4` Dryrow | **R8 Dunewind** | 56 | `villain_yield_officer_4` | `villain_site_manager_4` → `free_field {field:farm_4}` | 54 | 1 |
| `farm_6` Fenceline | **R12 Pylon Path** | 62 | `villain_yield_officer_6` | `villain_site_manager_6` (DOUBLES) → `free_field {field:farm_6}` | 60 | 1 |
| `farm_7` Coldfurrow | **R13 Dragonspine** | 68 | `villain_yield_officer_7` | `villain_site_manager_7` → `free_field {field:farm_7}` | 61 | 1 |
| `farm_8` Frostfallow | **R14 Frostveil** | 74 | `villain_yield_officer_8` | `villain_site_manager_8` → `free_field {field:farm_8}` | 67 | 2 |
| `farm_9` Highfield | **R16 / Road to RL** | 85+ | `villain_yield_officer_9` | `villain_site_manager_9` → `free_field {field:farm_9}` | 72 | 2 |
| `farm_10` Ashloam | **R15 Cinderfall** | 80 | `villain_yield_officer_10` | `villain_site_manager_10` → `free_field {field:farm_10}` | 84 | 2 |

**Natural-path check:** a player on the standard route hits `farm_2`(R3), `farm_5`(R10),
`farm_3`(R7), `farm_4`(R8), `farm_6`(R12), `farm_7`(R13) — **six Act-1 fields by the gym-7
window** — and the HQ gate (`fields_liberated >= 6`) opens organically. `farm_1` (R2) is a
seventh feeder for slack. This unit's job is done when those six route positions carry the
wheat-war guard bodies. **No field-guard battle whiteout risk:** each is opt-in (walk away
free, no decline fee — fields do not charge), so a weak player can retreat.

---

## Route Subsections

Each subsection: concept + hook + back-echo; a ready-to-paste NEW character block; a
ready-to-paste dialog block (or reuse note); DATAPACK NEEDS; QUEST_TARGETS stage; REWARD/BALANCE.
Field guards are **REF** (see `wheat_war_farms` for their full JSON — copy `villain_yield_officer.json`
/ `villain_site_manager.json`, swap id/trainer/placement/prize per §4 above).

---

## R3 Willowmire (band: Mystic Marsh, cap 37, frag_3, cd→24)

**Concept:** the boardwalk marsh route before Mystic. The "stable feel" ends here — the last
route where the propaganda is still glossy. A marsh spotter, a bird-counting lore keeper who
reads the wetland like an audit, and the `farm_2` Mirebloom guards.
**Forward hook:** the lore keeper names Mystic Marsh and Titania (Fairy) ahead.
**Back-echo:** the spotter, once `farm_1`/`farm_2` is freed, notes the freed parcel ("the
Deng rows stand straight again — word travels the boardwalk").

### R3 — Spotter: Marsh Scout Reeta

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "r3_spotter_marsh",
  "display_name": "Marsh Scout Reeta",
  "role": "duelist",
  "act": "1",
  "location": "Willowmire Boardwalk",
  "_comment": "R3 SPOTTER (band cap 37). Walk-up on a LONG clear boardwalk pinch so the ambush reads fair on camera. Register with NPC Sight after placement: npcsight add <uuid> mode pursue range 8 stoptag defeated_r3_spotter_marsh (uuid omitted here; sight needs it). Keep movement look-only so FOLLOW_PLAYER prio 7 wins. Team ~lv33-35 (under cap 37). PLACEHOLDER coord = builder confirm on the Willowmire polygon.",
  "trainer": "r3_spotter_marsh",
  "recipe": "trainer_spotter",
  "engage": "touch",
  "dialog": "dialog:r3_spotter_marsh",
  "sight": {
    "mode": "pursue",
    "range": 8,
    "stop_tag": "defeated_r3_spotter_marsh"
  },
  "battle": {
    "trainer": "r3_spotter_marsh",
    "type": "spotter",
    "format": "GEN_9_SINGLES",
    "hail_line": "Eyes on you three planks back. The marsh has no cover and neither do you.",
    "prize": 420,
    "decline_fee": 200,
    "defeat_tag": "defeated_r3_spotter_marsh",
    "win_line": "Sharp footwork on wet wood. You have earned the crossing.",
    "lose_line": "The bog does not forgive a slow step. Dry off and come back.",
    "already_beaten_line": "You have crossed once. My watch is only good the first time."
  },
  "placement": { "x": 0, "y": 0, "z": 0 }
}
```

### R3 — Spotter dialog (with the route-talks-back echo)

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "r3_spotter_marsh",
  "type": "STANDARD",
  "entries": [
    {
      "label": "field_echo",
      "name": "Reeta - the rows came back",
      "priority": 25,
      "gate": { "tag": "field_2_liberated" },
      "say": [
        "You are the one who cleared the paddies, are you not. Word travels the boardwalk faster than a heron.",
        "The rows at Mirebloom stand straight again and nobody in a Company vest has come to argue. Cross on.",
        "The marsh owes you a dry plank today. You freed the Mirebloom parcel and the whole wetland heard it."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Keep moving", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Marsh Scout Reeta",
      "priority": 10,
      "default": true,
      "say": [
        "Willowmire boardwalk. One rotten plank and the bog keeps you. I watch the crossing so the trainers who reach Mystic reach it whole.",
        "Titania keeps the Fairy gym past the reeds. Charming until she is not. Step careful.",
        "Mind your footing. Mystic Marsh and the Fairy leader wait past the last plank."
      ],
      "buttons": [
        { "label": "cross_button", "text": "Cross the boardwalk", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

- **DATAPACK NEEDS:** none new. `field_2_liberated` is set by the §5 tag-mirror function
  (`liberation/mirror_field_tags`, spec in §5) — one tick function shared by all route echoes.
- **QUEST_TARGETS:** spotters are not tracked quests (no sidebar line). No entry.
- **REWARD/BALANCE:** prize 420 CD, team ~lv33–35 (under cap 37). Walk-up spotter → `decline_fee`
  200 CD is the fairness escape (charge via pay-probe). Cap-legal.

### R3 — Flavor: Bittern-Watcher Odui (lore keeper)

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "r3_lorekeeper_bittern",
  "display_name": "Bittern-Watcher Odui",
  "role": "lore_keeper",
  "act": "1",
  "location": "Willowmire - reed blind",
  "recognition_tier": "early",
  "recipe": "civilian",
  "_comment": "R3 FLAVOR lore keeper. A bird-counter who reads the marsh like a ledger and distrusts the new one. NEVER recognizes the founder (civilian rule). Plants the Mystic Marsh hook; late in the run carries a scrubbing-artifact line. Ambient, interaction-driven, no sight. PLACEHOLDER coord.",
  "movement": { "objective": "ambient_stationary_look" },
  "dialog": "dialog:r3_lorekeeper_bittern",
  "placement": { "x": 0, "y": 0, "z": 0 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "r3_lorekeeper_bittern",
  "type": "STANDARD",
  "entries": [
    {
      "label": "late_decay",
      "name": "Odui - the count does not add",
      "priority": 20,
      "gate": { "cd_instability": { "op": "gte", "value": 40 } },
      "say": [
        "Forty years I have counted the bitterns. The number never lies. This season a man in a vest asked me to revise it. Revise a count. As if the birds could be re-verified.",
        "Something in the money is counting wrong too. I hear it in how the merchants talk. Nervous. Over-explaining. A true number never needs a speech.",
        "A count is a count. You do not revise a heron. Yet a man in a vest asked me to, this season, and would not say why."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Leave the blind", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Bittern-Watcher Odui",
      "priority": 10,
      "default": true,
      "say": [
        "Quiet. There is a bittern in the reeds and you are scaring my count. Sit if you like, but sit still.",
        "You are bound for Mystic Marsh and Titania, the Fairy leader. The marsh will test your footing before she tests your team.",
        "Both the marsh and Titania are honest tests, which is more than I can say for some tests lately. Mind the reeds on the way to Mystic."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Step away quietly", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

- **DATAPACK NEEDS:** none (`cd_instability` gate lowers to the band-tag maintained by the
  existing datapack tick — the compiler emits `cd_instability_gte_40`).
- **QUEST_TARGETS:** none (ambient flavor).
- **REWARD/BALANCE:** none (no battle).

### R3 — Field guards: `farm_2` Mirebloom (REF — `wheat_war_farms`)

Perimeter `villain_yield_officer_2` (1205,~64,2790) → Site manager `villain_site_manager_2`
(1229,~64,2820), gated on `defeated_villain_yield_officer_2`, `on_win` fires
`execute as @1 run function cobblemon_initiative:liberation/free_field {field:farm_2}`.
Full JSON: copy `villain_yield_officer.json` / `villain_site_manager.json` per `wheat_war_farms`
§8. **This doc's contribution:** it is on **R3**, band cap 37, guard opt-in (no decline fee).

**Route-side receipt / title-card (the streamable liberation beat this doc owns).** The
manager's `on_win` already fires `free_field {field:farm_2}`. Add two `announce` beats after it
so the liberation lands on camera as a title card (plain `text`, no double-quote / apostrophe /
percent — the compiler quotes it). This is the shared receipt pattern every field guard reuses,
swapping only the field name:

```json
{
  "on_win": [
    "execute as @1 run function cobblemon_initiative:liberation/free_field {field:farm_2}"
  ],
  "_win_button_actions_note": "The Site Manager win button chains three actions: {do:battle}, then {do:announce, text:MIREBLOOM LIBERATED, as:title, color:gold}, then {do:announce, text:ADJUSTMENT reversed - the parcel is off the ledger and back on the map, as:subtitle, color:gray}. The battle result already ran the on_win free_field; the two announces are the on-camera title card. Company voice on the reversal; no double-quote, apostrophe, or percent in any of it (the compiler quotes the tellraw/title)."
}
```

- **DATAPACK NEEDS:** none new (reuses `liberation/free_field`; the title card is an inline
  `announce`, not a function). Manager ace lv 29 (PROPOSED, `wheat_war_farms` §6).
- **QUEST_TARGETS entry (the liberation is a tracked sidebar line while `wheat_war_active`):**

```json
{
  "holder": "q.side_field_r3",
  "name": "Clear the Mirebloom Paddies",
  "slot": 72,
  "stages": [
    {
      "if_tags": ["wheat_war_active", "defeated_villain_yield_officer_2"],
      "not_tags": ["field_2_liberated"],
      "label": "Take back Mirebloom - face the Site Manager",
      "target": { "npc": "villain_site_manager_2" },
      "note": "Gate cleared (officer down): the manager battle is unlocked. Waypoint flips to the manager mid-field. Liberation latches field_2_liberated via free_field {field:farm_2}."
    },
    {
      "if_tags": ["wheat_war_active"],
      "not_tags": ["field_2_liberated", "defeated_villain_yield_officer_2"],
      "label": "Clear the fence at Mirebloom Paddies",
      "target": { "npc": "villain_yield_officer_2" },
      "note": "Perimeter leg: the fence guard blocks the manager. First-match-wins lists this last (lower priority than the manager stage)."
    }
  ]
}
```

- **REWARD/BALANCE:** guard prize 380 / manager prize 700, loss fee 130 (guard), no decline
  fee (opt-in field). Flat literals in `onwin`. Cap-legal at 37. (`wheat_war_farms` §7.)

### R3 — Road agent (grunt): none dedicated

R3 carries its Company presence through the `farm_2` field guards (early-band). A separate open-road
agent here would over-stack R3; the road-agent slot re-enters at R5. This is a deliberate density
choice — early routes lean on the field beat, mid/late routes add the open-road agent.

---

## R5 Quarry (band: Deepcore G4 approach, cap 44, frag_4, cd→32)

**Concept:** the stone-cut quarry route to the Fighting gym. Prices are "adjusting" now
(Act-2 economy voice begins). A quarry duelist, a cart-broke ore hauler (stranded merchant),
and the **first open-road agent** (Contractor tier, refs `villain_grunt_3`).
**Forward hook:** the hauler names Deepcore City and Bruno (Fighting) ahead.
**Back-echo:** the road agent's `mid` recognition line references the memo ("your face is on a
memo") — the escalation the player earned by clearing early fields. No adjacent counting farm
(Deepcore is fighting country).

### R5 — Trainer: Rockbreaker Dala

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "r5_trainer_quarry",
  "display_name": "Rockbreaker Dala",
  "role": "duelist",
  "act": "1",
  "location": "The Quarry - lower bench",
  "_comment": "R5 TRAINER (band cap 44). Interaction-driven (NOT walk-up) so it is skippable + decline-able; team ~lv40-42 under cap 44. No sight. PLACEHOLDER coord on the Quarry polygon.",
  "trainer": "r5_trainer_quarry",
  "recipe": "type_specialist",
  "dialog": "dialog:r5_trainer_quarry",
  "battle": {
    "trainer": "r5_trainer_quarry",
    "type": "basic",
    "format": "GEN_9_SINGLES",
    "prize": 520,
    "decline_fee": 240,
    "defeat_tag": "defeated_r5_trainer_quarry",
    "win_line": "Solid. You hit like the quarry hits back. Deepcore will like you.",
    "lose_line": "Stone teaches patience. Come swing again.",
    "already_beaten_line": "One bout a visit. My arms are quarried out."
  },
  "placement": { "x": 0, "y": 0, "z": 0 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "r5_trainer_quarry",
  "type": "STANDARD",
  "entries": [
    {
      "label": "default",
      "name": "Rockbreaker Dala",
      "priority": 10,
      "default": true,
      "say": [
        "You look like you are headed for Deepcore. Bruno does not go easy on soft hands. Warm up on me first - one bout, no shame in declining.",
        "The quarry pays in bruises and lessons. Both are cheaper than a Nuzlocke funeral. Take a bout or walk on, your call.",
        "Deepcore and Bruno wait past the rim. A quarry bout now beats a surprise later - but declining is free enough."
      ],
      "buttons": [
        { "label": "fight_button", "text": "Take the bout", "actions": [ { "do": "battle" } ] },
        { "label": "leave_button", "text": "Not today", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

- **DATAPACK NEEDS:** none.
- **QUEST_TARGETS:** none (optional trainer, not a tracked quest).
- **REWARD/BALANCE:** prize 520, decline fee 240 (charge via pay-probe). Team lv40–42, cap-legal
  at 44. Interaction-driven, fully skippable.

### R5 — Flavor: Stranded Hauler Poe (merchant)

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "r5_merchant_stranded",
  "display_name": "Stranded Hauler Poe",
  "role": "merchant",
  "act": "1",
  "location": "The Quarry - broken cart",
  "recognition_tier": "early",
  "recipe": "shopkeeper",
  "_comment": "R5 FLAVOR - a cart-broke ore hauler; a small pokemart-style trade while he waits for a wheel. NEVER recognizes the founder. Plants the Deepcore hook. Trade via trade_pokemart snippet. PLACEHOLDER coord.",
  "movement": { "objective": "ambient_stationary_look" },
  "dialog": "dialog:r5_merchant_stranded",
  "trade": { "snippet": "trade_pokemart", "open_label": "shop" },
  "placement": { "x": 0, "y": 0, "z": 0 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "r5_merchant_stranded",
  "type": "STANDARD",
  "entries": [
    {
      "label": "default",
      "name": "Stranded Hauler Poe",
      "priority": 10,
      "default": true,
      "say": [
        "Wheel cracked on the quarry road and here I sit. Might as well sell down the cart while I wait.",
        "You headed to Deepcore? Bruno country. Stock up before the stone gym eats your potions.",
        "The prices I paid for this stock keep changing under me. Same crate, new number every week. So I sell at what I paid and eat the difference. Somebody has to hold a line."
      ],
      "buttons": [
        { "label": "shop_button", "text": "Browse the cart", "actions": [ { "do": "trade" } ] },
        { "label": "leave_button", "text": "Safe travels", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

- **DATAPACK NEEDS:** none (trade is the `trade_pokemart` snippet).
- **QUEST_TARGETS:** none.
- **REWARD/BALANCE:** trade only (offers from the snippet; prices not author-tunable in v1).

### R5 — Road agent (grunt): Contractor Chen (refs `villain_grunt_3`)

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "villain_route_agent_5",
  "display_name": "Chen",
  "role": "villain_grunt",
  "act": "1",
  "location": "The Quarry - toll ledge",
  "recognition_tier": "early",
  "trainer": "villain_grunt_3",
  "recipe": "villain_grunt",
  "_comment": "R5 ROAD AGENT (Contractor tier). NEW route body referencing the existing villain_grunt_3 team (NOT the placed Regional Office body - see doc grunt-id reconciliation + Open Question 1). INTERACTION-ONLY: no sight block, so the fight is entered by talking to him at the toll ledge, never a walk-up ambush - a pre-band player is never cornered and no per-route arm function is needed (§6, Open Q2). despawn_on_win - the ledge opens. Recognition entries (late/mid/default) copied VERBATIM from the shipped villain_route_surveyor block. Canon: nameplate Chen; title lives in dialog. PLACEHOLDER coord.",
  "dialog_inline": {
    "kind": "dialog",
    "id": "route_agent_5",
    "type": "STANDARD",
    "entries": [
      {
        "label": "late",
        "name": "Contractor - late recognition",
        "priority": 30,
        "gate": { "recognition": "late" },
        "say": [
          "It is you. The founder, walking the routes like a rookie. They swore you were a story told to scare new hires.",
          "I have orders. I have a family paid in Company scrip. Do not make me do this. But I am going to."
        ],
        "buttons": [ { "label": "yes_button", "text": "Face them", "actions": [ { "do": "battle" } ] } ],
        "no_goodbye": true
      },
      {
        "label": "mid",
        "name": "Contractor - mid recognition",
        "priority": 20,
        "gate": { "recognition": "mid" },
        "say": [
          "Your face is on a memo. The kind with a black bar over the name and the words do not engage, report immediately.",
          "You are supposed to be a closed file. Closed files do not get to walk away."
        ],
        "buttons": [ { "label": "yes_button", "text": "Fight", "actions": [ { "do": "battle" } ] } ],
        "no_goodbye": true
      },
      {
        "label": "quarry_hail",
        "name": "Contractor - the toll",
        "priority": 15,
        "say": [
          "Hold, resident. This quarry road is under a Company haulage contract. Foot traffic is unlicensed tonnage. You may pay tonnage, or you may become an obstruction, which we clear.",
          "The stone here has a grade now. So does everyone who walks past it. Do not ask for yours.",
          "Unlicensed tonnage on a Company road. Refuse the toll and we settle it the hard way, or step off the ledge and we forget you."
        ],
        "buttons": [
          { "label": "fight_button", "text": "Refuse the toll - face the Contractor", "actions": [ { "do": "battle" } ] },
          { "label": "leave_button", "text": "Step off the ledge", "actions": [ { "do": "close" } ] }
        ]
      },
      {
        "label": "default",
        "name": "Contractor - early (no recognition)",
        "priority": 10,
        "say": [
          "Company business. This quarry road is under contract, and contracts do not appreciate audiences.",
          "We settle this the old way. Wait. Do I know you from somewhere?"
        ],
        "buttons": [ { "label": "yes_button", "text": "Fight", "actions": [ { "do": "battle" } ] } ],
        "no_goodbye": true
      }
    ]
  },
  "battle": {
    "trainer": "villain_grunt_3",
    "type": "villain_grunt",
    "format": "GEN_9_SINGLES",
    "prize": 480,
    "loss_fee": 150,
    "defeat_tag": "defeated_villain_route_agent_5",
    "despawn_on_win": true,
    "win_line": "Contract suspended. The quarry road is downgraded to scenic.",
    "lose_line": "Tonnage collected. The Company thanks you for your cooperation.",
    "already_beaten_line": "The contract is void. The wheel-ruts are the road maintenance department problem now."
  },
  "placement": { "x": 0, "y": 0, "z": 0 }
}
```

- **DATAPACK NEEDS:** none. This agent is **interaction-only** (no `sight` block), so no
  `route/*_arm.mcfunction` is needed — the fight is entered by talking to him. The recognition
  tier still self-gates the dialog line via the compiler-lowered band tags (`badges` → early/mid/
  late), and the `despawn_on_win` latch runs from the `battle` block. (If a walk-up ambush is
  ever wanted on a marquee route, add the R2 `right_of_way/arm` post-badge sight arm then — see
  Open Q2 — but the shipped design is interaction-only across all route agents.)
- **QUEST_TARGETS:** none (open-road grunt, not a tracked quest; the fight is opt-in flavor).
- **REWARD/BALANCE:** prize 480, loss fee 150. Opt-in (the `quarry_hail` `leave_button` walks
  away free — no decline fee, matching the field-guard opt-in convention for open-road agents).
  Team = existing `villain_grunt_3` (Contractor tier), cap-legal for the mid band. `defeat_tag`
  is `defeated_villain_route_agent_5` (distinct from the placed `villain_grunt_3` body's tag).

### R5 — Field guards: none (no counting farm on the Deepcore approach).

---

## R7 Gullwing Coast (band: Gaviota G5, cap 50, frag_5, cd→40)

**Concept:** the sea-cliff coastal route to the Water gym. A gull-keeper spotter with a long
clean sightline, a tidepool beachcomber (traveler), and the `farm_3` Westwind guards.
**Forward hook:** the traveler names Gaviota Port and Neptune (Water) ahead.
**Back-echo:** the traveler mentions the courier fork / a liberated field ("the coast road is
quieter since the fences came down inland").

### R7 — Spotter: Gull-Keeper Hano

Copy the R3 `r3_spotter_marsh` character + dialog shape; change: `id: r7_spotter_coast`,
`display_name: Gull-Keeper Hano`, `location: Gullwing Coast`, `trainer: r7_spotter_coast`,
`stop_tag`/`defeat_tag: defeated_r7_spotter_coast`, `hail_line`: "The gulls marked you a
headland back. On this coast there is nowhere to not be seen." `prize: 560`, `decline_fee:
260`, team ~lv46–48 (cap 50). Route-talks-back echo entry gated `tag: field_3_liberated`
("the Westwind fields answer to no memo now — the whole coast heard").

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "r7_spotter_coast",
  "type": "STANDARD",
  "entries": [
    {
      "label": "field_echo",
      "name": "Hano - the coast heard",
      "priority": 25,
      "gate": { "tag": "field_3_liberated" },
      "say": [
        "You are the one who cleared Westwind. The gulls carried it up the coast before you did.",
        "The Westwind fields answer to no memo now, and a lot of quiet people are grateful in a way they cannot say out loud. Cross the headland.",
        "Gaviota is just past the spray. You freed Westwind and the whole coast heard it - safe crossing, founder or not."
      ],
      "buttons": [ { "label": "leave_button", "text": "Keep to the cliff path", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "default",
      "name": "Gull-Keeper Hano",
      "priority": 10,
      "default": true,
      "say": [
        "Gullwing Coast. The wind will steal your footing if you let it. My flock and I watch the cliff so the trainers bound for Gaviota do not feed the tide.",
        "Neptune holds the Water gym in the port below. Deep team, deeper patience. Do not rush the sea.",
        "Gaviota Port is past the spray. Watch your step on the cliff and mind Neptune when you reach the water."
      ],
      "buttons": [ { "label": "cross_button", "text": "Take the cliff path", "actions": [ { "do": "close" } ] } ]
    }
  ]
}
```

### R7 — Flavor: Tidepool Walker Sena (traveler/civilian)

Copy the R3 `r3_lorekeeper_bittern` character shape; change: `id: r7_traveler_tidepool`,
`display_name: Tidepool Walker Sena`, `role: civilian`, `location: Gullwing Coast - tidepools`,
`dialog: dialog:r7_traveler_tidepool`. Civilian → NEVER recognizes the founder; back-echo the
liberated inland fields + forward-hook Gaviota.

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "r7_traveler_tidepool",
  "type": "STANDARD",
  "entries": [
    {
      "label": "late_decay",
      "name": "Sena - the coin does not shine",
      "priority": 20,
      "gate": { "cd_instability": { "op": "gte", "value": 40 } },
      "say": [
        "I trade shells with the port folk. Used to be a CobbleDollar was a CobbleDollar. Now every merchant quotes me a different number and each one apologizes for it. A coin that needs an apology is not a coin.",
        "You are bound for Gaviota and Neptune, I would wager. Mind the tide charts. The sea keeps better books than the Company does lately.",
        "A coin that needs an apology is not a coin. The merchants slide me a new number every low tide and none will say why."
      ],
      "buttons": [ { "label": "leave_button", "text": "Wander on", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "default",
      "name": "Tidepool Walker Sena",
      "priority": 10,
      "default": true,
      "say": [
        "Low tide. Best hour on the whole coast. The pools give up their secrets and ask nothing back - which is more than most trades these days.",
        "Gaviota Port is around the headland. Neptune runs the Water gym there. Tell the tide I said hello.",
        "Around the headland lies Gaviota and Neptune of the Water gym. Walk the pools first if you have the hour."
      ],
      "buttons": [ { "label": "leave_button", "text": "Walk the pools", "actions": [ { "do": "close" } ] } ]
    }
  ]
}
```

### R7 — Field guards: `farm_3` Westwind (REF)

Perimeter `villain_yield_officer_3` (640,~64,3310) → `villain_site_manager_3` (657,~64,3279),
`on_win` → `free_field {field:farm_3}`. **On R7**, band cap 50, manager ace lv 43. Opt-in.

- **QUEST_TARGETS entry:** copy the R3 `q.side_field_r3` block; change holder →
  `q.side_field_r7`, name → "Clear the Westwind Fields", npc targets →
  `villain_yield_officer_3` / `villain_site_manager_3`, tags → `defeated_villain_yield_officer_3`
  / `field_3_liberated`, slot 71.
- **REWARD/BALANCE:** guard 520 / manager 950, loss fee 175, opt-in. Cap-legal at 50.

### R7 — Road agent: none dedicated (the field guards carry the mid-band presence).

---

## R8 Dunewind (band: Kalahar G6, cap 56, frag_6, cd→48)

**Concept:** the desert route to the Ground gym. Wheat traders may **ambush** now (≥4 fields).
A dune duelist, a ground-type-tip trainer, a waystone-keeper lore keeper (scrubbing-artifact
line), and the `farm_4` Dryrow guards.
**Forward hook:** the type-tip names Kalahar Reach and Gaia (Ground) ahead.
**Back-echo:** the waystone keeper references the revised org charts / an "impostor" memo blown
in on the wind (scrubbing artifact).

### R8 — Trainer: Duneracer Kito

Copy R5 `r5_trainer_quarry` character+dialog shape; `id: r8_trainer_dune`, `display_name:
Duneracer Kito`, `location: Dunewind`, `trainer: r8_trainer_dune`, `defeat_tag:
defeated_r8_trainer_dune`, `prize: 700`, `decline_fee: 300`, team ~lv52–54 (cap 56). Interaction,
decline-able.

### R8 — Type-tip: Sandreader Mafu

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "r8_typetip_dune",
  "display_name": "Sandreader Mafu",
  "role": "duelist",
  "act": "1",
  "location": "Dunewind - shifting flats",
  "_comment": "R8 TYPE-TIP (ground matchup, before Kalahar). recipe type_specialist, battle.type type_tip. First-win item via the RCT registry rewards (NOT an on_win give here). Walk-up so it teaches BEFORE the gym; decline_fee for the fairness escape. Register sight after placement. Team ~lv52-53 (cap 56). PLACEHOLDER coord.",
  "trainer": "r8_typetip_dune",
  "recipe": "type_specialist",
  "engage": "touch",
  "dialog": "dialog:r8_typetip_dune",
  "sight": { "mode": "pursue", "range": 8, "stop_tag": "defeated_r8_typetip_dune" },
  "battle": {
    "trainer": "r8_typetip_dune",
    "type": "type_tip",
    "format": "GEN_9_SINGLES",
    "hail_line": "The sand read your gait a dune ago. Ground answers to Ground - let me show you before Gaia does.",
    "prize": 650,
    "decline_fee": 300,
    "defeat_tag": "defeated_r8_typetip_dune",
    "win_line": "Now you feel it. Earthquake finds every set of feet. Gaia will not surprise you.",
    "lose_line": "The dune wins when you fight it standing still. Rest and read the sand again.",
    "already_beaten_line": "The lesson took. Walk light on the flats."
  },
  "placement": { "x": 0, "y": 0, "z": 0 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "r8_typetip_dune",
  "type": "STANDARD",
  "entries": [
    {
      "label": "default",
      "name": "Sandreader Mafu",
      "priority": 10,
      "default": true,
      "say": [
        "Kalahar is over the flats and Gaia fights Ground the way the desert fights - patient, then all at once. Let me teach you the matchup before it costs you a teammate.",
        "The sand keeps no secrets. Neither will your footing once Earthquake lands. Learn it here or learn it from Gaia the hard way.",
        "Gaia and Kalahar wait over the flats. One bout with me and Ground will not surprise you - or decline and read the sand yourself."
      ],
      "buttons": [
        { "label": "fight_button", "text": "Learn the matchup", "actions": [ { "do": "battle" } ] },
        { "label": "leave_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

### R8 — Flavor: Waystone Keeper Idi (lore keeper, scrubbing artifact)

Copy R3 `r3_lorekeeper_bittern` character shape; `id: r8_flavor_caravan`, `display_name:
Waystone Keeper Idi`, `location: Dunewind - waystone line`, `dialog: dialog:r8_flavor_caravan`.

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "r8_flavor_caravan",
  "type": "STANDARD",
  "entries": [
    {
      "label": "late_decay",
      "name": "Idi - the chart came apart",
      "priority": 20,
      "gate": { "cd_instability": { "op": "gte", "value": 48 } },
      "say": [
        "The wind brought me paper today. A Company org chart, revised, then revised again over the first revision. Names crossed out until the page was mostly ink, and one name had a black bar where a face should be.",
        "I tend the waystones so travelers do not lose the road. Somebody up the chain is doing the opposite - moving the stones so people lose who they were.",
        "A black bar where a face should be, on a chart revised past reading. I do not envy whoever they are trying to erase."
      ],
      "buttons": [ { "label": "leave_button", "text": "Follow the stones", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "default",
      "name": "Waystone Keeper Idi",
      "priority": 10,
      "default": true,
      "say": [
        "Waystone line runs true to Kalahar Reach. Follow the stones and the flats will not swallow you. Gaia holds the Ground gym at the end of them.",
        "A waystone is just a promise that someone walked here before you and wanted you to make it too. Cheapest honest thing in the region.",
        "Kalahar Reach and Gaia sit at the end of the stone line. Keep the waystones on your right hand and the desert will not take you."
      ],
      "buttons": [ { "label": "leave_button", "text": "Walk the line", "actions": [ { "do": "close" } ] } ]
    }
  ]
}
```

### R8 — Field guards: `farm_4` Dryrow (REF)

Perimeter `villain_yield_officer_4` (1548,~78,3822) → `villain_site_manager_4` (1535,~78,3872),
`on_win` → `free_field {field:farm_4}`. **On R8**, cap 56, manager ace lv 54. Opt-in.
QUEST_TARGETS: copy R3 block, holder `q.side_field_r8`, name "Clear the Dryrow Steading",
npc targets `villain_yield_officer_4`/`villain_site_manager_4`, tags → `_4`, slot 70. Guard 600
/ manager 1100, loss fee 200.

### R8 — Road agent: none dedicated (field guards carry it).

---

## R10 Old Caravan (band: central artery Sango↔Kalahar, cap 44–56)

**Concept:** the long central artery — the hub route the whole map hangs off. Carries the
**Crossroads Granary (`farm_5`)** and its road agent (Operative tier, refs `villain_grunt_4`).
A caravan-guard spotter and a road peddler (traveler/hub NPC that points at the Crossroads
liberation).
**Forward hook:** the peddler points at Crossroads Granary (`farm_5`) and the wider field war.
**Back-echo:** the peddler references the courier fork (R1/R2 opener) and the first liberated
field.

### R10 — Spotter: Caravan Guard Boro

Copy R3 spotter shape; `id: r10_spotter_caravan`, `display_name: Caravan Guard Boro`,
`location: Old Caravan road`, `trainer: r10_spotter_caravan`, tags → `defeated_r10_spotter_caravan`,
`hail_line`: "A caravan guard sees the whole road at once. You have been in my eyes since the
last bend." `prize: 520`, `decline_fee: 240`, team ~lv48–50. Route-talks-back echo gated `tag:
field_5_liberated` ("the granary silo answers to no memo - the whole caravan breathes easier").

### R10 — Flavor: Peddler Ndulu (traveler / artery hub)

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "r10_merchant_wagon",
  "display_name": "Peddler Ndulu",
  "role": "merchant",
  "act": "1",
  "location": "Old Caravan - the crossroads",
  "recognition_tier": "early",
  "recipe": "shopkeeper",
  "_comment": "R10 FLAVOR + artery hub NPC. A road peddler at the crossroads; small trade, and the pointer to the Crossroads Granary field (farm_5) + the wider Wheat War. NEVER recognizes the founder. Back-echoes the courier fork (R1/R2). Trade via trade_pokemart. PLACEHOLDER coord.",
  "movement": { "objective": "ambient_stationary_look" },
  "dialog": "dialog:r10_merchant_wagon",
  "trade": { "snippet": "trade_pokemart", "open_label": "shop" },
  "placement": { "x": 0, "y": 0, "z": 0 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "r10_merchant_wagon",
  "type": "STANDARD",
  "entries": [
    {
      "label": "granary_pointer",
      "name": "Ndulu - the silo squats on a field",
      "priority": 20,
      "gate": { "tag": "wheat_war_active", "not_tag": "field_5_liberated" },
      "say": [
        "You are the one kicking fences in, are you not. Then here is a tip worth the browse: the Crossroads Granary down the artery is not a store. It is a silo the Company planted on top of a working field.",
        "Free the Crossroads parcel and the whole caravan road stops paying their toll. That is the tip; the browse is extra.",
        "I remember when the first courier fork on Blossom Path still went two honest ways. Feels like a long time and it was not. That is how fast they move."
      ],
      "buttons": [
        { "label": "shop_button", "text": "Browse the wagon", "actions": [ { "do": "trade" } ] },
        { "label": "leave_button", "text": "Roll on", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Peddler Ndulu",
      "priority": 10,
      "default": true,
      "say": [
        "Old Caravan road. Everyone crosses it eventually - Sango to Kalahar, the whole map hangs off this dust. I set up where the crossing is thickest and sell to whoever passes.",
        "Prices? Do not ask. I quote what I paid and pray it still means something by sundown.",
        "This artery runs Sango to Kalahar and the Crossroads Granary squats halfway along it. Browse the wagon or roll on, traveler."
      ],
      "buttons": [
        { "label": "shop_button", "text": "Browse the wagon", "actions": [ { "do": "trade" } ] },
        { "label": "leave_button", "text": "Roll on", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

### R10 — Road agent: Operative Soo (refs `villain_grunt_4`)

Copy the R5 `villain_route_agent_5` shape; `id: villain_route_agent_10`, `display_name: Soo`,
`location: Old Caravan - toll straight`, `trainer: villain_grunt_4`, `defeat_tag:
defeated_villain_route_agent_10`, hail entry `caravan_hail` ("This artery is under a Company
transit levy. Every wagon that rolls owes tonnage."). `prize: 560`, `loss_fee: 160`, Operative
tier team. Keep the verbatim late/mid recognition entries. Opt-in `leave_button`.

### R10 — Field guards: `farm_5` Crossroads Granary (REF)

Perimeter `villain_yield_officer_5` (2262,~66,3500) → `villain_site_manager_5` (2309,~66,3540),
`on_win` → `free_field {field:farm_5}`. **On R10**, manager ace lv 28. Opt-in.
QUEST_TARGETS: copy R3 block, holder `q.side_field_r10`, name "Clear the Crossroads Granary",
targets `_5`, tags → `_5`, slot 69. Guard 350 / manager 650, loss fee 120.
**Note:** the Granary Keeper commerce NPC (`granary_keeper_crossroads`) sits inside `farm_5` —
authored by `wheat_war_farms`, not this doc; freeing the parcel double-hits (you liberate the
ground the company store squats on).

---

## R12 Pylon Path (band: Cyber G7, cap 62, frag_7 "You signed this charter", cd→56 PEAK)

**Concept:** THE hard turn. Under the Company power pylons on the approach to the Electric gym
— instability peaks, HQ unlocks, frag_7 lands. A line-tech spotter, a grid-reader lore keeper
(frag_7 telegraph — circles the charter without closing it), an **elite road agent** (Senior
Agent, refs `villain_grunt_5`, recognition mid→late), and the `farm_6` Fenceline DOUBLES guard.
**Forward hook:** the grid-reader names Cyber City and Volt (Electric); Volt teases the Stadium.
**Back-echo:** the grid-reader references the signature under the transition orders — the
Firstfurrow memo the archivist filed (`transition_filed`) — the physical seed of frag_7.

### R12 — Spotter: Line Tech Volta

Copy R3 spotter shape; `id: r12_spotter_pylon`, `display_name: Line Tech Volta`, `location:
Pylon Path`, `trainer: r12_spotter_pylon`, tags → `defeated_r12_spotter_pylon`, `hail_line`:
"The pylons caught your silhouette two towers back. I read the grid; the grid read you."
`prize: 700`, `decline_fee: 320`, team ~lv58–60 (cap 62). Route-talks-back echo gated `tag:
field_6_liberated`.

### R12 — Flavor: Grid-Reader Chike (lore keeper, frag_7 telegraph)

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "r12_lorekeeper_pylon",
  "display_name": "Grid-Reader Chike",
  "role": "lore_keeper",
  "act": "1",
  "location": "Pylon Path - substation shadow",
  "recognition_tier": "mid",
  "recipe": "civilian",
  "_comment": "R12 FLAVOR lore keeper. Reads the Company power grid; the frag_7 TELEGRAPH - circles the charter/signature without closing it (per LORE_BIBLE frag_7 rule: never states you are the Founder). NEVER recognizes the founder (civilian rule - he reads paper, not faces). Back-echoes the transition orders / the archivist filing. Plants the Cyber/Volt/Stadium hook. Ambient, no sight. PLACEHOLDER coord.",
  "movement": { "objective": "ambient_stationary_look" },
  "dialog": "dialog:r12_lorekeeper_pylon",
  "placement": { "x": 0, "y": 0, "z": 0 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "r12_lorekeeper_pylon",
  "type": "STANDARD",
  "entries": [
    {
      "label": "charter_telegraph",
      "name": "Chike - one signature under all of it",
      "priority": 20,
      "gate": { "cd_instability": { "op": "gte", "value": 56 } },
      "say": [
        "Every pylon on this path carries a charter plate at its base. Company property, authorized under founding charter, and one signature. Same signature on all of them. I have read a thousand and it never changes.",
        "A man came through filing the plates for removal. Founder retired, he said, then caught himself and said there was never a founder. Both cannot be true.",
        "A signature is a fact, and someone is very frightened of this one. The same hand signed a thousand plates and now they want the plates gone."
      ],
      "buttons": [ { "label": "leave_button", "text": "Read on", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "default",
      "name": "Grid-Reader Chike",
      "priority": 10,
      "default": true,
      "say": [
        "Pylon Path feeds Cyber City. Volt holds the Electric gym past the substation - and he has been talking about some Stadium he wants to fill. Ask him yourself; he never stops once he starts.",
        "I read the grid the way others read weather. Right now it reads overloaded. Something upstream is drawing more than it should.",
        "Cyber City and Volt are past the substation. He keeps naming a Stadium he means to fill - you will hear about it the moment you arrive."
      ],
      "buttons": [ { "label": "leave_button", "text": "Move along the line", "actions": [ { "do": "close" } ] } ]
    }
  ]
}
```

### R12 — Road agent: Senior Agent Rho (refs `villain_grunt_5`)

Copy R5 road-agent shape; `id: villain_route_agent_12`, `display_name: Rho`, `location: Pylon
Path - checkpoint`, `trainer: villain_grunt_5`, `defeat_tag: defeated_villain_route_agent_12`,
hail entry `pylon_hail` ("This corridor is a restricted Company transmission asset. Your
presence is a fault the grid will correct."). `prize: 720`, `loss_fee: 180`, Senior Agent team.
Recognition mid→late plays here (cap-62 window). Keep verbatim late/mid entries. Opt-in.

### R12 — Field guards: `farm_6` Fenceline Acres (REF — DOUBLES manager)

Perimeter `villain_yield_officer_6` (1520,~72,1762) → `villain_site_manager_6` (1570,~72,1735)
**GEN_9_DOUBLES** (Regional Operations pair), `on_win` → `free_field {field:farm_6}`. **On
R12**, cap 62, manager ace lv 60. Opt-in. This is the sixth pre-HQ feeder on the natural path —
**freeing it commonly opens the HQ gate.**
QUEST_TARGETS: copy R3 block, holder `q.side_field_r12`, name "Clear Fenceline Acres", targets
`_6`, tags → `_6`, slot 68. Guard 750 / manager 1400 (doubles), loss fee 250.

---

## R13 Dragonspine (band: Ryujin G8 / HQ road, cap 68, frag_8 "You built it", cd=25 post-HQ)

**Concept:** the ridge road to the Dragon gym, running past the HQ turnoff. Post-HQ: currency
stabilized (idx 25), propaganda now *corrupts* (slogans glitch, cover-up leaks). A dragonrider
duelist, an ashen pilgrim (traveler — cover-up-leak line), an **elite road agent** (Elite Agent,
refs `villain_grunt_6`, recognition `late`), and the `farm_7` Coldfurrow guard.
**Forward hook:** the pilgrim names Ryujin Keep and Ryujin (Dragon) ahead.
**Back-echo:** the pilgrim references DJ's fall / "CURRENCY STABILIZED" ("the money steadied
the day the acting one fell - I felt my purse stop shrinking").

### R13 — Trainer: Dragonrider Kaen

Copy R5 trainer shape; `id: r13_trainer_spine`, `display_name: Dragonrider Kaen`, `location:
Dragonspine ridge`, `trainer: r13_trainer_spine`, `defeat_tag: defeated_r13_trainer_spine`,
`prize: 850`, `decline_fee: 380`, team ~lv64–66 (cap 68). Interaction, decline-able.

### R13 — Flavor: Ashen Pilgrim Uzo (traveler, cover-up leak)

Copy R3 lore-keeper character shape; `id: r13_traveler_pilgrim`, `display_name: Ashen Pilgrim
Uzo`, `role: civilian`, `location: Dragonspine - HQ turnoff`, `dialog:
dialog:r13_traveler_pilgrim`. Civilian → never recognizes the founder.

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "r13_traveler_pilgrim",
  "type": "STANDARD",
  "entries": [
    {
      "label": "post_hq",
      "name": "Uzo - the money steadied",
      "priority": 20,
      "gate": { "tag": "defeated_villain_boss" },
      "say": [
        "You feel it too, I think. The money steadied. One day the numbers just stopped sliding out from under us.",
        "They say the acting one fell. They do not say who to. The Company does not do subtraction in public.",
        "I walk to Ryujin Keep to ask the dragons an old question. Ryujin holds the gym there. The dragons keep better counsel than the ledgers ever did."
      ],
      "buttons": [ { "label": "leave_button", "text": "Walk on the ridge", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "default",
      "name": "Ashen Pilgrim Uzo",
      "priority": 10,
      "default": true,
      "say": [
        "The Dragonspine runs above the clouds and past a road nobody names. I keep my eyes on the ridge and my questions to myself.",
        "Ryujin Keep is at the end of the spine. Ryujin holds the Dragon gym. A hard climb and a harder leader - fitting, at this height.",
        "Follow the ridge to Ryujin Keep. Ryujin and the Dragon gym wait at the top, past the road no one names."
      ],
      "buttons": [ { "label": "leave_button", "text": "Continue the pilgrimage", "actions": [ { "do": "close" } ] } ]
    }
  ]
}
```

### R13 — Road agent: Elite Agent Ige (refs `villain_grunt_6`, late recognition)

Copy R5 road-agent shape; `id: villain_route_agent_13`, `display_name: Ige`, `location:
Dragonspine - HQ turnoff`, `trainer: villain_grunt_6`, `defeat_tag:
defeated_villain_route_agent_13`, hail entry `spine_hail` ("This is the service road to a
restricted asset. You do not have clearance. Nobody has clearance now."). `prize: 780`,
`loss_fee: 200`, Elite Agent team. Post-HQ, recognition `late` dominates. Opt-in.

### R13 — Field guards: `farm_7` Coldfurrow (REF)

Perimeter `villain_yield_officer_7` (1870,~64,985) → `villain_site_manager_7` (1925,~64,963)
(stays SINGLES — do not stack a set-piece on the HQ doorstep), `on_win` → `free_field
{field:farm_7}`. **On R13**, cap 68, manager ace lv 61. Opt-in.
QUEST_TARGETS: copy R3 block, holder `q.side_field_r13`, name "Clear Coldfurrow Farm", targets
`_7`, tags → `_7`, slot 67. Guard 800 / manager 1500, loss fee 275.

---

## R14 Frostveil (band: Nifl G9, cap 74, frag_9 "They emptied you", cd=25)

**Concept:** the ice pass to Nifl. Recognition is `late` — some agents begin to **stand down**.
A frost-warden spotter, a snowline hermit lore keeper (who remembers the old face — a stand-down
seed), an **elite road agent** (Elite Agent, refs `villain_grunt_7`, late), and the `farm_8`
Frostfallow guard.
**Forward hook:** the hermit names Nifl Town and Boreas (Ice) ahead.
**Back-echo:** the hermit references the portraits coming down ("I saw your face in a branch
office once, before they took it down - do not ask me whose it was, I am too old to be filed").

### R14 — Spotter: Frostwarden Neve

Copy R3 spotter shape; `id: r14_spotter_frost`, `display_name: Frostwarden Neve`, `location:
Frostveil pass`, `trainer: r14_spotter_frost`, tags → `defeated_r14_spotter_frost`, `hail_line`:
"Tracks in fresh snow. You were seen the moment you set foot on the pass." `prize: 850`,
`decline_fee: 380`, team ~lv70–72 (cap 74). Route-talks-back echo gated `tag:
field_8_liberated`.

### R14 — Flavor: Snowline Hermit Bran (lore keeper, stand-down seed)

Copy R3 lore-keeper shape; `id: r14_flavor_hermit`, `display_name: Snowline Hermit Bran`,
`location: Frostveil - snowline`, `dialog: dialog:r14_flavor_hermit`.
**IMPORTANT canon nuance:** Bran is a civilian and by the hard rule civilians NEVER recognize
the founder. He remembers *a face* — the public portrait — but does not connect it to the player
in front of him. His late line brushes the scrubbing without ever saying "it is you." This
keeps the rule while seeding the stand-down mood.

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "r14_flavor_hermit",
  "type": "STANDARD",
  "entries": [
    {
      "label": "old_face",
      "name": "Bran - the portrait they took down",
      "priority": 20,
      "gate": { "cd_instability": { "op": "gte", "value": 25 } },
      "say": [
        "I wintered in a branch office once, decades back, when they still let an old man in from the cold. There was a portrait on the wall - the face everyone trusted. Then one spring the wall was brighter where it had hung, and no one would say the name.",
        "Funny thing about a bright rectangle on a faded wall. It tells you exactly the shape of what they removed. You cannot un-see a missing thing.",
        "The face everyone trusted came down one spring and left a brighter square behind. I am too old to be filed and too cold to care who tries."
      ],
      "buttons": [ { "label": "leave_button", "text": "Leave him to the snow", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "default",
      "name": "Snowline Hermit Bran",
      "priority": 10,
      "default": true,
      "say": [
        "Frostveil takes the unprepared. Boreas takes the overconfident. Nifl Town is past the pass, and the Ice gym is past the town. Dress warm and think colder.",
        "I keep to the snowline where nobody files paperwork. Best decision I ever made.",
        "Past the pass lies Nifl Town, and past the town, Boreas and the Ice gym. Dress warm and think colder."
      ],
      "buttons": [ { "label": "leave_button", "text": "Press through the pass", "actions": [ { "do": "close" } ] } ]
    }
  ]
}
```

### R14 — Road agent: Elite Agent Sable (refs `villain_grunt_7`, may stand down)

Copy R5 road-agent shape; `id: villain_route_agent_14`, `display_name: Sable`, `location:
Frostveil - waypost`, `trainer: villain_grunt_7`, `defeat_tag: defeated_villain_route_agent_14`,
hail `frost_hail`. **Add a `late` stand-down variant** (priority 30) where the agent lowers his
hand rather than fight the founder — but still gate a fight button for players who force it:

```json
{
  "label": "late",
  "name": "Elite Agent - late stand-down",
  "priority": 30,
  "gate": { "recognition": "late" },
  "say": [
    "I placed your face the moment you crested the pass. Everyone above me swears there was never a founder. But I served long enough to have shaken the hand of a story that supposedly never existed.",
    "I am not raising a hand against you in this cold. Walk on. If anyone asks, the pass was empty. It usually is.",
    "I know that face. I will not lift a hand for a memo that erased it. Walk on, and the pass was empty when they ask."
  ],
  "buttons": [
    { "label": "walk_button", "text": "Accept the stand-down", "actions": [ { "do": "close" } ] },
    { "label": "fight_button", "text": "Test him anyway", "actions": [ { "do": "battle" } ] }
  ],
  "no_goodbye": true
}
```

`prize: 800`, `loss_fee: 200`. Opt-in either way (stand-down closes free; the fight is a choice).

### R14 — Field guards: `farm_8` Frostfallow (REF, Act 2)

Perimeter `villain_yield_officer_8` (3045,~64,2500) → `villain_site_manager_8` (3066,~64,2478),
`on_win` → `free_field {field:farm_8}`. **On R14**, cap 74, manager ace lv 67. Opt-in. (Note:
`farm_8` ships `mobsSpawn:false` — `wheat_war_farms` Open Q3, showrunner call.)
QUEST_TARGETS: copy R3 block, holder `q.side_field_r14`, name "Clear Frostfallow Farm", targets
`_8`, tags → `_8`, slot 66. Guard 900 / manager 1700, loss fee 300.

---

## R15 Cinderfall (band: Scorchspire G10, cap 80, frag_10 "face your own signature", cd=25)

**Concept:** the volcano-skirt route to the Fire gym — the last gym approach. Recognition
`late`, agents most rattled (closest to the mirror). An emberwalker duelist, an ash-walker
traveler, an **elite road agent** (refs `villain_grunt_8`, rattled/double-down variant), and
the `farm_10` Ashloam guard.
**Forward hook:** the ash-walker names Scorchspire and Vulcan (Fire) — the last badge before the
League.
**Back-echo:** the ash-walker references frag_10's "your own signature" mood obliquely — the
propaganda now fully corrupted.

### R15 — Trainer: Emberwalker Tavi

Copy R5 trainer shape; `id: r15_trainer_ember`, `display_name: Emberwalker Tavi`, `location:
Cinderfall`, `trainer: r15_trainer_ember`, `defeat_tag: defeated_r15_trainer_ember`, `prize:
1000`, `decline_fee: 450`, team ~lv76–78 (cap 80). Interaction, decline-able.

### R15 — Flavor: Ash-Walker Odita (traveler)

Copy R3 lore-keeper shape; `id: r15_traveler_ashwalker`, `display_name: Ash-Walker Odita`,
`role: civilian`, `location: Cinderfall - ash flats`, `dialog: dialog:r15_traveler_ashwalker`.

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "r15_traveler_ashwalker",
  "type": "STANDARD",
  "entries": [
    {
      "label": "corrupt_slogan",
      "name": "Odita - the slogans came apart",
      "priority": 20,
      "gate": { "tag": "defeated_villain_boss" },
      "say": [
        "The Company banners at the last waystation had gone strange. Verified Trust, the letters flickering like a bad flame, as if the slogan could no longer keep its own promise.",
        "I walk the ash to Scorchspire. Vulcan holds the Fire gym at the top - the last badge before the League.",
        "They say the mountain shows you your own face in the heat haze. I try not to look. Vulcan and the last badge wait at the summit."
      ],
      "buttons": [ { "label": "leave_button", "text": "Cross the flats", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "default",
      "name": "Ash-Walker Odita",
      "priority": 10,
      "default": true,
      "say": [
        "Cinderfall. The ground is warm even in winter and the air tastes of struck matches. Scorchspire is up the skirt of the volcano - Vulcan and the Fire gym, the last one.",
        "Walk light. The ash remembers every footstep for exactly as long as the next wind.",
        "Up the skirt of the volcano lies Scorchspire, Vulcan, and the last badge before the League. Walk light on the flats."
      ],
      "buttons": [ { "label": "leave_button", "text": "Onward to the mountain", "actions": [ { "do": "close" } ] } ]
    }
  ]
}
```

### R15 — Road agent: Elite Agent Vane (refs `villain_grunt_8`, doubles-down)

Copy R5 road-agent shape; `id: villain_route_agent_15`, `display_name: Vane`, `location:
Cinderfall - ash road`, `trainer: villain_grunt_8`, `defeat_tag: defeated_villain_route_agent_15`,
hail `ember_hail`. Give the `late` entry the **double-down** flavor (the panicked official line
rather than a stand-down):

```json
{
  "label": "late",
  "name": "Elite Agent - late double-down",
  "priority": 30,
  "gate": { "recognition": "late" },
  "say": [
    "No. There was never a founder. The file says so and the file is the truth. So you are no one. A no one who keeps interfering, which makes you a problem, which makes this my job.",
    "I say it again so it is true. There was never a founder. Now hold still while I make the memo accurate.",
    "You are no one. The file is clear on that, and the file does not lie. Stand still and let me close it properly."
  ],
  "buttons": [
    { "label": "yes_button", "text": "Face them", "actions": [ { "do": "battle" } ] },
    { "label": "leave_button", "text": "Step back off the ash road", "actions": [ { "do": "close" } ] }
  ],
  "no_goodbye": true
}
```

`prize: 900`, `loss_fee: 220`. Opt-in end to end — the hail entry has a `leave_button` and the
late double-down entry now carries its own `leave_button` too (fairness floor: even a rattled
agent screaming the official line cannot corner a player into a whiteout; you can always step
back off the ash road). Fail-soft: the whole encounter is optional to walk into.

### R15 — Field guards: `farm_10` Ashloam (REF, Act 2)

Perimeter `villain_yield_officer_10` (3245,~66,4020) → `villain_site_manager_10` (3293,~66,4005),
`on_win` → `free_field {field:farm_10}`. **On R15**, cap 80, manager ace lv 84 (fought roughly
at the League window). Opt-in.
QUEST_TARGETS: copy R3 block, holder `q.side_field_r15`, name "Clear the Ashloam Fields", targets
`_10`, tags → `_10`, slot 65. Guard 1300 / manager 2400, loss fee 450.

---

## R16 Frontier Causeway + Road to Royal League (band: post-RL / RL, cap 85–100, cd=25)

**Concept:** the endgame approaches — the causeway to the Battle Frontier and the road to the
Royal League. Recognition fully `late`; the strongest stand-down mood. A frontier scout spotter,
a causeway chronicler lore keeper (back-echoes the WHOLE run), a stand-down road agent (refs
`villain_grunt_9`), and the shared `farm_9` Highfield guard (Royal-League-adjacent).
**Forward hook:** the chronicler names the Royal League (Aria/Marcus/Luna/Drake → Cynthia) and
the Frontier.
**Back-echo:** the chronicler recites the run — the courier fork, the first field, census_signed,
the badges — the payoff-of-remembering line for a long-form audience.

### R16 — Spotter: Frontier Scout Rhea

Copy R3 spotter shape; `id: r16_spotter_causeway`, `display_name: Frontier Scout Rhea`,
`location: Frontier Causeway`, `trainer: r16_spotter_causeway`, tags → `defeated_r16_spotter_causeway`,
`hail_line`: "The causeway is a straight line and you are the only thing moving on it. Of course
I saw you." `prize: 1100`, `decline_fee: 500`, team ~lv82–85 (cap 85). Route-talks-back echo
gated `tag: field_9_liberated`.

### R16 — Flavor: Causeway Chronicler Set (lore keeper, whole-run back-echo)

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "r16_lorekeeper_causeway",
  "display_name": "Causeway Chronicler Set",
  "role": "lore_keeper",
  "act": "2",
  "location": "Frontier Causeway - the arch",
  "recognition_tier": "late",
  "recipe": "civilian",
  "_comment": "R16 FLAVOR lore keeper - records who crosses to the Frontier/League. NEVER recognizes the founder (civilian rule); he chronicles DEEDS, not the identity. His late entry recites the run for the long-form audience (courier fork, first field, census, badges) WITHOUT closing the founder reveal (that waits for Act 3). Plants the Royal League / Frontier hook. Ambient, no sight. PLACEHOLDER coord.",
  "movement": { "objective": "ambient_stationary_look" },
  "dialog": "dialog:r16_lorekeeper_causeway",
  "placement": { "x": 0, "y": 0, "z": 0 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "r16_lorekeeper_causeway",
  "type": "STANDARD",
  "entries": [
    {
      "label": "the_ledger_of_deeds",
      "name": "Set - I have been writing you down",
      "priority": 20,
      "gate": { "champion": true },
      "say": [
        "Champion. I keep the crossing ledger and I have been writing you down the whole way: a courier fork on Blossom Path, a first fence kicked in at Firstfurrow, a census signed, ten badges, a currency that stopped sliding the day an acting man fell.",
        "I do not write down who you are - that is not a chronicler job, and I suspect it is not settled yet. I only write what you did, and it is already the longest entry in the book.",
        "The book remembers it all. Blossom Path, Firstfurrow, the census, the badges, the day the money steadied. Yours is the longest entry I have ever kept."
      ],
      "buttons": [ { "label": "leave_button", "text": "Cross the causeway", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "default",
      "name": "Causeway Chronicler Set",
      "priority": 10,
      "default": true,
      "say": [
        "The Frontier Causeway carries the ones who are nearly done to the ones who decide it. The Royal League waits at the far arch - Aria, Marcus, Luna, Drake, and Cynthia at the end of them.",
        "I write down everyone who crosses. Most entries are one line. A few are longer. Come back when you have given me something worth the ink.",
        "Across the arch wait the Royal League - Aria, Marcus, Luna, Drake, and Cynthia last of all. I will be writing down whoever reaches them."
      ],
      "buttons": [ { "label": "leave_button", "text": "Toward the League", "actions": [ { "do": "close" } ] } ]
    }
  ]
}
```

### R16 — Road agent: Elite Agent Corr (refs `villain_grunt_9`, stand-down)

Copy the R14 stand-down road-agent shape; `id: villain_route_agent_16`, `display_name: Corr`,
`location: Frontier Causeway - Royal-League approach`, `trainer: villain_grunt_9`, `defeat_tag:
defeated_villain_route_agent_16`, hail `causeway_hail`. Use the R14 `late` stand-down variant
(lowers the hand; optional fight button). `prize: 900`, `loss_fee: 250`. Opt-in.

### R16 / Road-to-RL — Field guards: `farm_9` Highfield (REF, Act 2)

Perimeter `villain_yield_officer_9` (3245,~66,3360) → `villain_site_manager_9` (3300,~66,3354),
`on_win` → `free_field {field:farm_9}`. **On R16 / Road-to-RL**, cap 85+, manager ace lv 72.
Opt-in.
QUEST_TARGETS: copy R3 block, holder `q.side_field_r16`, name "Clear the Highfield Estate",
targets `_9`, tags → `_9`, slot 64. Guard 1000 / manager 1900, loss fee 350.

---

## 5. Recognition & economy beats (per band, gated)

**Villain recognition (grunts, badge-gated — the shared verbatim block, compiler-lowered):**
- **early** (`badges < 3`, R3): default entry — confused hostility, "Do I know you from
  somewhere?" The memo made flesh.
- **mid** (`badges 3–6`, R5/R7/R8/R10/R12): "Your face is on a memo. The kind with a black bar
  over the name... You are supposed to be a closed file." Alarm.
- **late** (`badges >= 7`, R13/R14/R15/R16): "It is you. The founder, walking the routes like a
  rookie." Two flavors introduced this unit: **stand-down** (R14/R16 — lowers the hand) and
  **double-down** (R15 — "there was never a founder, so you are no one").

**Economy voice (gated on `cd_instability`, matched to flavor NPCs):**
- **Act 1 stable (idx 0–24, R3):** glossy — merchants confident; the marsh keeper still trusts
  numbers.
- **Act 2 slipping (idx 25–56, R5/R7/R8/R10/R12):** nervous reassurance — the hauler and
  peddler over-explain "prices adjusting"; each `late_decay`/`late` entry (gated `cd_instability
  gte 40/48/56`) has the merchant apologizing for a coin that needs an apology.
- **Post-HQ / corrupted (idx clamped 25, R13/R14/R15/R16):** slogans glitch (the ash-walker's
  "Verified Trust, the letters flickering"); the cover-up leaks (the pilgrim's "they say the
  acting one fell, they do not say who to"; the grid-reader's "founder retired, then there was
  never a founder"). Scrubbing artifacts (bright rectangle on the wall, revised org chart blown
  on the wind) carried by the waystone keeper (R8) and the hermit (R14).

**Field-liberation economy pushback:** each freed field (`free_field`) drops `cd_instability`
−6 and lights the route-talks-back echoes (`field_N_liberated`), so the routes visibly *ease*
as the player claws the currency back — the spotters and flavor NPCs comment on the change.

---

## 6. New tags / scores introduced (synthesis pass)

| Tag / score | Set by | Gated by (used in) |
|-------------|--------|--------------------|
| `defeated_r3_spotter_marsh` | R3 spotter win (`defeat_tag`) | spotter `already_beaten_line`; sight `stop_tag` |
| `defeated_r5_trainer_quarry` | R5 trainer win | `already_beaten_line` |
| `defeated_r7_spotter_coast` | R7 spotter win | sight `stop_tag` |
| `defeated_r8_trainer_dune` / `defeated_r8_typetip_dune` | R8 trainer/type-tip wins | `stop_tag` / `already_beaten` |
| `defeated_r10_spotter_caravan` | R10 spotter win | sight `stop_tag` |
| `defeated_r12_spotter_pylon` | R12 spotter win | sight `stop_tag` |
| `defeated_r13_trainer_spine` | R13 trainer win | `already_beaten` |
| `defeated_r14_spotter_frost` | R14 spotter win | sight `stop_tag` |
| `defeated_r15_trainer_ember` | R15 trainer win | `already_beaten` |
| `defeated_r16_spotter_causeway` | R16 spotter win | sight `stop_tag` |
| `defeated_villain_route_agent_5/10/12/13/14/15/16` | route-agent wins (`defeat_tag`) | agent `already_beaten_line`; despawn latch |
| `field_2_liberated` … `field_10_liberated` | **`liberation/mirror_field_tags`** (NEW tick fn, §Datapack) | route-talks-back echo entries (spotters) |

**Note:** `field_N_liberated` are *tags mirrored from* the existing `field_freed[farm_N]`
scoreboard latch (set by `free_field_apply`). They exist because dialog gates read player TAGs,
not the `field_freed` fake-player scoreboard. The `farm_1` echo can reuse the existing
`farm_1_free`/bridge tag if one already exists; otherwise `field_1_liberated` joins the mirror.
The route-agent `defeat_tag`s are deliberately distinct from the placed building grunts'
`defeated_villain_grunt_N` (the route agents only *borrow the trainer team*, not the identity).

### Datapack needs (consolidated)

- **`function/liberation/mirror_field_tags.mcfunction`** (NEW, tick — runs in the liberation
  load/tick fn family): for each `N` in 2..10, `execute if score farm_N field_freed matches 1
  run tag @s add field_N_liberated`. One shared function feeds every route-talks-back echo. ~9
  lines, idempotent (tags re-assert; harmless to re-add). Alternatively fold these lines into
  the existing `free_field_apply.mcfunction` (add `tag @s add field_$(field)_liberated` — but
  that needs the macro to expand the tag name; the standalone mirror tick is simpler and
  relog-safe).
- **`function/route/<route>_arm.mcfunction`** (OPTIONAL, per road agent that uses walk-up
  sight): arms the agent's `npcsight` registration only after its band tag (R2 `right_of_way/arm`
  convention), so a pre-band player is never ambushed. **Recommendation:** author all route
  agents as **interaction-only** (no `sight` block) so no arm function is needed — the R5
  example is interaction-only; the spotters are the only walk-up beats and they self-gate on
  band via team level, not sight timing.
- **No new turn-in / count-check functions** — this unit has no fetch quests (the field
  liberations reuse `liberation/free_field`; the spotters/trainers are direct battles; the
  merchants use `trade_pokemart`). The Genji `clear-with-0` count-check pattern is **not
  needed** here.
- Spotter/trainer RCT team files (`data/rctmod/trainers/<id>.json`) + `villain_team`/registry
  entries for each NEW trainer id — teams per the band-cap sketch in each subsection.

---

## 7. Build checklist (ordered)

1. **Field-guard bodies first (the unblock).** Confirm with `wheat_war_farms` that the six
   Act-1 field guards (`villain_yield_officer_2/3/4/5/6/7` + matching `villain_site_manager_*`)
   are authored and placed at the §4 coords. This doc's job is done for the Act1→Act2 gate once
   these six sit on R3/R7/R8/R10/R12/R13. (The three Act-2 guards `_8/_9/_10` follow on
   R14/R16/R15.)
2. **Drop NEW route character files** (`dialog-src/characters/route<N>/` — create the dirs;
   mirror the existing `route1/` `route2/` layout): 11 spotters/trainers/type-tips, 11 flavor
   NPCs, 7 route agents. Total ~29 new character files (some routes share slots per §2).
3. **Drop NEW dialog files** (`dialog-src/dialog/`): one per NEW character that is not
   `dialog_inline`. Spotters/trainers/flavor use referenced `dialog:<id>` files; road agents use
   `dialog_inline` (copy the `villain_route_surveyor` block + a route-specific hail). ~22 dialog
   files.
4. **Create RCT team files** (`data/rctmod/trainers/<id>.json`) + `villain_team`/registry entries
   for each NEW trainer id (spotters/trainers/type-tips/route-agent defeat tags). Route agents
   reuse existing `villain_grunt_3..9` teams (no new team file for those battles) but need their
   own registry `defeat_tag` mapping.
5. **Add the mirror tick function** `function/liberation/mirror_field_tags.mcfunction` and wire
   it into the liberation tick family (§6).
6. **Add QUEST_TARGETS stages** to `dialog-src/registers/quest_targets.json`: seven field-clear
   holders (`q.side_field_r3/r7/r8/r10/r12/r13` for Act-1 + `_r14/_r15/_r16` for Act-2), copied
   from the §R3 template with holder/name/npc/tags/slot swapped. Spotters/trainers/flavor NPCs
   are **not** tracked (no sidebar line).
7. **Register spotter sight** after in-world import: `npcsight add <uuid> mode pursue range 8
   stoptag defeated_<id>` for each spotter (uuid captured at first self-spawn or pre-assigned
   via `skin.uuid`). Flavor NPCs and interaction-only road agents need no sight.
8. **Compile:** `scripts/content_compile` (lowers dialog-src → presets + band tags + quest
   waypoints), then `scripts/update_preset_index`, then `scripts/generate_npc_function`. Then
   `gradle build`.

---

## 8. Open questions for showrunner

1. **Route grunt-id policy (the big one).** Canon `villain_grunt_3..11` are placed inside
   Company *buildings* (Regional Office / Ops Center / HQ / Boardroom). This doc introduces NEW
   route-agent bodies that **borrow those grunt teams** for open-road battles (distinct
   `defeat_tag`s). Ruling needed: (a) keep it as designed — new road bodies referencing existing
   teams (clean, no interior disruption; my recommendation); or (b) relocate some building grunts
   to the routes and re-place them (fewer total bodies, but re-scatters the HQ/Ops interior
   cast). The prompt's "spread villain_grunt_1..11 across routes by band" reads as (b), but the
   built canon is (a)-compatible only. **Recommend (a).**
2. **Route agent walk-up vs interaction.** I recommend all route agents be **interaction-only**
   (no `sight`) so no per-route arm function is needed and no pre-band player is ambushed. The
   R2 `right_of_way` precedent armed a walk-up post-badge — do we want that drama on any of these
   routes (e.g. R12 Pylon at the hard turn), or keep them all interaction-only?
3. **Spotter density per route.** I placed one spotter/trainer beat per route (some with a
   second type-tip on R8). R1/R2 had a spotter *and* a type-tip. Scale up to two combat beats on
   the meatier routes (R12/R13/R15), or keep one to avoid Nuzlocke-fatigue on the long endgame
   approaches?
4. **`field_N_liberated` tag source.** Confirm the mirror-tick approach (§6) vs. folding tag-set
   into `free_field_apply` via macro. Also confirm whether `farm_1` already exposes a liberation
   *tag* (for R2's own echo) or needs to join the mirror.
5. **Coords.** Every NEW route NPC coord is `PLACEHOLDER` — the route polygons in
   `dev/updated-zones.json` / `install.json` need builder confirm for spotter sightlines (long,
   clean, on-camera) and flavor-NPC anchors. Field-guard coords are carried from
   `wheat_war_farms` (§4) and are themselves `PROPOSED` there.
6. **R10 double-duty.** R10 (Old Caravan) is both a route *and* the artery hub. Its peddler Ndulu
   doubles as a mini rumor-hub pointing at the wheat war. Is that the right home for the field-war
   pointer, or should it live in a town hub instead?
7. **Above-cap wagers.** This unit kept all battles at-or-under cap (no above-cap wagers were
   needed — the fairness floor is satisfied by decline fees + opt-in). If a marquee above-cap
   wager is wanted on an endgame route (a Frontier-brain-adjacent gambler on R16), specify the
   route and I will add an opt-in, printed-stake, decline-able beat.
