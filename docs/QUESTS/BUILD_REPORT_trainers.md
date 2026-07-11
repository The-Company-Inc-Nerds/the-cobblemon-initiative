# Build Report — Route + Frontier Trainer Integration

_Integration audit of the newly authored route backbone + Battle Frontier trainers._
Validation harness: `python3 dev/cobblemon_validation/validate_trainers.py` — **0 errors, 138 warnings** (all warnings are intentional identity-vs-filename or documented tag divergences; exit code 0).

## 1. Summary Counts

| Deliverable | Count |
|---|---|
| **New RCT team files** (`src/main/resources/data/rctmod/trainers/*.json`) | **59** |
| — Battle Frontier RCT teams | 24 |
| — Route own-team trainers/spotters (`rN_*`) | 11 |
| — Villain-arc RCT teams (field guards + admins + grunts + board + bosses) | 24 |
| **Frontier character files** (`dialog-src/characters/frontier/`) | **24** |
| **Route character files** (`dialog-src/characters/routeN`, new-trainer) | **24** |
| **Battle Frontier facility entries** (`battle_frontier.json`) | 24 |
| Route agents reusing existing grunt teams (no new RCT file) | 7 |

All 24 Battle Frontier ids (from `npc_map_template.json` **and** `battle_frontier.json` — the two sets are identical) now have **BOTH** an rctmod team file and a `dialog-src/characters/frontier/<id>.json`. **None missing.**

Integrity checks passed:
- Every new character with a `battle` block resolves `battle.trainer` to an existing rctmod team file (no `[NO-RCT]`).
- Every new rctmod file: team non-empty, no namespace prefixes in team species. `identity` equals filename except the 9 named leaders/bosses (`board_*`, `villain_admin*`, `villain_boss`→`dj`, `villain_final_boss`→`founder`) — intentional (RCT resolves by filename; `identity` is the canonical character name, matching the shipped-leader convention).
- **No id collisions**: diffing rctmod ids HEAD→working tree shows 59 additions and **0 removals/overwrites** — no new file clobbered a shipped id.

## 2. Trainers Created

### Battle Frontier (24) — all gated behind `royal_champion`, brains chain challenger prereqs

| id | facility | role | band | team |
|---|---|---|---|---|
| castle_challenger_1 | Battle Castle | duelist | 90 | 3 |
| castle_challenger_2 | Battle Castle | duelist | 90 | 3 |
| frontier_brain_castle | Battle Castle | frontier_brain | 100 | 4 |
| tower_challenger_1 | Battle Tower | duelist | 90 | 3 |
| tower_challenger_2 | Battle Tower | duelist | 90 | 3 |
| frontier_brain_tower | Battle Tower | frontier_brain | 100 | 3 |
| factory_challenger_1 | Battle Factory | duelist | 90 | 3 |
| factory_challenger_2 | Battle Factory | duelist | 90 | 3 |
| frontier_brain_factory | Battle Factory | frontier_brain | 100 | 4 |
| arcade_challenger_1 | Battle Arcade | duelist | 90 | 3 |
| arcade_challenger_2 | Battle Arcade | duelist | 90 | 3 |
| frontier_brain_arcade | Battle Arcade | frontier_brain | 100 | 4 |
| pyramid_challenger_1 | Battle Pyramid | duelist | 90 | 3 |
| pyramid_challenger_2 | Battle Pyramid | duelist | 90 | 3 |
| frontier_brain_pyramid | Battle Pyramid | frontier_brain | 100 | 4 |
| port_challenger_1 | The Port | duelist | 90 | 3 |
| port_challenger_2 | The Port | duelist | 90 | 3 |
| frontier_brain_port | The Port | frontier_brain | 100 | 4 |
| market_challenger_1 | The Market | duelist | 90 | 3 |
| market_challenger_2 | The Market | duelist | 90 | 3 |
| frontier_brain_market | The Market | frontier_brain | 100 | 4 |
| cave_challenger_1 | Deep Dark Cave | duelist | 90 | 3 |
| cave_challenger_2 | Deep Dark Cave | duelist | 90 | 3 |
| frontier_brain_cave | Deep Dark Cave (final; gated behind all 7 other brains) | frontier_brain | 100 | 6 |

### Route Backbone — own-team trainers, spotters & type-tips (11)

| id | route | role | band | team |
|---|---|---|---|---|
| r3_spotter_marsh | R3 Willowmire Boardwalk | duelist (sight/pursue) | 33–35 | 3 |
| r5_trainer_quarry | R5 The Quarry lower bench | duelist | 40–42 | 3 |
| r7_spotter_coast | R7 Gullwing Coast | duelist (sight/pursue) | 46–48 | 3 |
| r8_trainer_dune | R8 Dunewind flats | duelist | 52–54 | 3 |
| r8_typetip_dune | R8 Dunewind flats | duelist (sight/pursue) | 52–53 | 2 |
| r10_spotter_caravan | R10 Old Caravan road | duelist (sight/pursue) | 48–50 | 3 |
| r12_spotter_pylon | R12 Pylon Path | duelist (sight/pursue) | 58–60 | 2 |
| r13_trainer_spine | R13 Dragonspine ridge | duelist | 64–66 | 3 |
| r14_spotter_frost | R14 Frostveil pass | duelist (sight/pursue) | 70–72 | 3 |
| r15_trainer_ember | R15 Cinderfall Descent | duelist | 76–79 | 6 |
| r16_spotter_causeway | R16 Frontier Causeway | duelist (sight/pursue) | 82–85 | 4 |

### Route Villain Field-Guards & Agents

| id | route | role | band | team | team source |
|---|---|---|---|---|---|
| villain_yield_officer_3 | R7 Westwind Coast Fence | grunt (sight/dialog) | 41–42 | 2 | own |
| villain_site_manager_3 | R7 Westwind Silo | grunt | 42–43 | 2 | own |
| villain_yield_officer_4 | R8 Dryrow Sand Fence | grunt (sight/dialog) | 51–52 | 2 | own |
| villain_site_manager_4 | R8 Dryrow Granary | grunt | 52–54 | 2 | own |
| villain_yield_officer_5 | R10 Crossroads Perimeter Gate | grunt (sight/dialog) | 26 | 2 | own |
| villain_site_manager_5 | R10 Crossroads Silo | grunt | 27–28 | 2 | own |
| villain_route_agent_5 | R5 Quarry toll ledge | grunt | 26–28 | 2 | reuses `villain_grunt_3` |
| villain_route_agent_10 | R10 Old Caravan toll | grunt | 18–20 | 2 | reuses `villain_grunt_4` |
| villain_route_agent_12 | R12 Pylon checkpoint | grunt | 32–34 | 2 | reuses `villain_grunt_5` |
| villain_route_agent_13 | R13 Dragonspine HQ turnoff | grunt | 39–41 | 2 | reuses `villain_grunt_6` |
| villain_route_agent_14 | R14 Frostveil waypost | grunt | 45–47 | 2 | reuses `villain_grunt_7` |
| villain_route_agent_15 | R15 Cinderfall ash road | grunt | 51–53 | 3 | reuses `villain_grunt_8` |
| villain_route_agent_16 | Road to Royal League | grunt | 57–59 | 3 | reuses `villain_grunt_9` |

_(The 7 route agents deliberately carry their own `defeat_tag` — `defeated_villain_route_agent_N` — distinct from the placed Regional-Office grunt body they borrow a team from, so their dialog gating is independent. This is the documented grunt-id reconciliation, flagged as a WARN by the validator but intentional.)_

### Villain-Arc RCT teams also created this pass (supporting content, not route/frontier)
`villain_admin` (shade), `villain_admin_2` (vex), `villain_admin_commander` (noir), `villain_boss` (dj / Acting CEO), `villain_final_boss` (founder, lv100 mirror), `board_lauren/madeline/matt/micah` (lv81–87), `villain_grunt_3`–`11`.

## 3. FIELD-GUARD → farm_N Liberation Wiring (the wheat-war unblock)

The liberation lever is `function cobblemon_initiative:liberation/free_field {field:"farm_N"}` (idempotent via the `field_freed` latch; pushes `cd_instability` down and advances `fields_liberated n/6`). Each farm is a **two-guard chain**: a perimeter **yield-officer** (has NPC-sight, hails at the fence) must be beaten to set `defeated_villain_yield_officer_N`, which unlocks the **site-manager's** battle; beating the site manager fires `free_field`.

| farm | perimeter guard (sight) | fires free_field | site manager (unblocks) | display name | wired? |
|---|---|---|---|---|---|
| farm_1 | villain_yield_officer (shipped) | villain_site_manager (Harvest Rd) | via `defeated_villain_yield_officer` | FIRSTFURROW | ✅ |
| farm_2 | — | — | — | _(unnamed)_ | ❌ **NOT wired** |
| farm_3 | villain_yield_officer_3 (R7) | villain_site_manager_3 | `defeated_villain_yield_officer_3` | WESTWIND (Westwind Fields) | ✅ |
| farm_4 | villain_yield_officer_4 (R8) | villain_site_manager_4 | `defeated_villain_yield_officer_4` | DRYROW (Dryrow Steading) | ✅ |
| farm_5 | villain_yield_officer_5 (R10) | villain_site_manager_5 | `defeated_villain_yield_officer_5` | CROSSROADS GRANARY | ✅ |
| farm_6 | — | — | — | _(unnamed)_ | ❌ **NOT wired** |

**Status:** 4 of 6 fields are fully wired end-to-end (farm_1 shipped + farm_3/4/5 authored this pass). The ceremony/quest tracker counts to **n/6** (`fields_liberated`, "Liberate the occupied fields n/6"), so **farm_2 and farm_6 remain unwired** — no field-guard chain fires `free_field {field:farm_2}` / `{field:farm_6}`, and neither has a `names.<field>` entry in `liberation/load.mcfunction` (only `farm_1:"FIRSTFURROW"` is seeded there; farm_3/4/5 names live in their site-managers' win title-cards). **Showrunner decision needed: either author two more field-guard pairs (farm_2, farm_6) or lower the ceremony denominator from 6 to 4.** Per `liberation/load.mcfunction`'s WIRING RULE, any newly wired field must also add its dispatch-board name to the `names` map.

## 4. Collisions / Warnings / PLACEHOLDER Placements

- **No id/data collisions.** 59 rctmod additions, 0 shipped ids overwritten.
- **Validator: 0 errors.** All 138 warnings are benign:
  - ~130 `identity != filename` (RCT resolves by filename; identity carries canon character names — the shipped-leader convention).
  - 7 `defeat_tag != defeated_<trainer>` on the route agents (documented distinct-tag reconciliation, §2).
  - 2 canon story-flag tags on **shipped** characters: `royal_champion`→`royal_league_champion`, `villain_final_boss`→`company_overthrown`.
  - 1 `heldItem 'cobblemon:big_root'` on **shipped** `hua_zhan_leader` (real Cobblemon item, just absent from the offline `items.txt` snapshot; not this pass's content).
- **30 PLACEHOLDER placements needing showrunner coords** — every route NPC and 6 frontier NPCs (Battle Tower + Battle Factory groups) carry `"_comment": "PLACEHOLDER..."`. Frontier coords are a shipped RCT anchor near `[3800 159 2962]`; the atlas-intent facility coords (e.g. Battle Tower `[4026 64 3068]`) must be verified against the atlas before final bake.
- **Placement-coordinate overlaps** (co-located NPCs sharing one placeholder anchor — need spacing, NOT id collisions): `[1372 64 2329]` r3_spotter_marsh + r3_lorekeeper_bittern; `[3370 64 3630]` villain_route_agent_15 + r15_trainer_ember + r15_traveler_ashwalker; `[3681 64 3003]` r16_spotter_causeway + r16_lorekeeper_causeway; `[1150 64 2830]` r5_merchant_stranded + r5_trainer_quarry + villain_route_agent_5.
- **19 spotters/guards carry a `sight` block** and must be armed with `/npcsight` after placement (see §5): the 8 route `rN_spotter/typetip` pursue-spotters, the 3 yield-officers (dialog-mode fence hails), and 8 villain-arc grunts (`villain_grunt_3–11`).

## 5. Follow-Up: Making Them Live

**Build pipeline (in order):**

```bash
# 1. Compile dialog-src → Easy NPC presets + quest_waypoints + update_npc_presets.mcfunction
scripts/content_compile

# 2. Rebuild the Easy NPC preset index (registers the new presets)
scripts/update_preset_index

# 3. Regenerate the NPC spawn/skin mcfunction from npc_presets.json
scripts/generate_npc_function

# 4. Build the mod jar
gradle build            # user runs gcommit → gradle build → build-mrpack
```

**In-world placement (per new NPC):**

1. Walk up to each PLACEHOLDER anchor; place the Easy NPC body (latch NPCs spawn within ~40 blocks after `/cobblemon-initiative install run`, or place manually and record the UUID into `npc_map_template.json` → import to the world's `npc_preset_map.json`).
2. For the 30 PLACEHOLDER placements, resolve final coords vs the atlas (frontier facility rings + the 4 shared route anchors above) and update the character `placement` before the final compile.

**NPC-sight arming (19 spotters/guards):** after each body is placed and its UUID recorded, run:

```
# Route pursue-spotters (range 8; stop on their own defeat tag):
/npcsight add <uuid> 8 default
/npcsight mode <uuid> pursue
#   r3_spotter_marsh, r7_spotter_coast, r8_typetip_dune, r10_spotter_caravan,
#   r12_spotter_pylon, r14_spotter_frost, r16_spotter_causeway  (+ villain_grunt_3–11)

# Field-guard fence hails (dialog mode, range 10):
/npcsight add <uuid> 10 fence_hail
/npcsight mode <uuid> dialog
#   villain_yield_officer_3, villain_yield_officer_4, villain_yield_officer_5
```

**Farm liberation loose end:** decide farm_2 / farm_6 — author the two missing yield-officer→site-manager pairs (each firing `free_field {field:farm_2|farm_6}` and adding a `names.<field>` entry to `liberation/load.mcfunction`), or drop the ceremony denominator to 4.
