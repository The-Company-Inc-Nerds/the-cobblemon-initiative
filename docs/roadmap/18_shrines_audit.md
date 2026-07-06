# 18 — Shrines: Audit + Polish

> **Scope:** LIGHT section — the 5 elemental shrines (dragon / fairy / fire / ground / ice)
> already exist as a Java subsystem + configs + leader characters + install.json zones.
> This is a *verify + enrich* pass, not a redesign. Every factual claim below is grounded
> in a file that was read; invented coordinates are labelled **PROPOSED**.
> Audited against source on 2026-07-05.

---

## 1. Concept & fantasy

**One-line pitch:** Five optional element-guardian trials — each a *signature gauntlet*
(not just another gym) that ends in a legendary-wielding High Priest — where the land
itself recognises the amnesiac's "old gravity," and clearing all five arms the player with
crystals that summon catchable box-legendaries.

The fun / the marquee stream moments:

- **The crystal payoff loop is the headline.** Every shrine leader drops a
  `<type>_shrine_crystal` (EPIC, foil, fire-resistant, stacks to 1). Right-clicking it on
  the ground plays an End-portal-spawn + dragon-breath particle burst and **spawns a
  level-70 box legendary you can then catch** (`ShrineCrystalItem.useOn` →
  `spawnpokemon <species> level=70`). Registered pairings (`ModItems`):
  Fire→**Moltres**, Ground→**Groudon**, Ice→**Articuno**, Dragon→**Rayquaza**,
  Fairy→**Xerneas**. That is a "deferred loot box" the streamer controls the timing of —
  perfect delayed gratification for a long-form audience.
- **Five distinct challenge fantasies**, not five reskins (see §4). The Dragon *Hydra
  Gauntlet* (three heads, heal between), the Ground *Buried Maze* (blindness + half health
  + random earthquakes that fling you), the Ice *Frozen Path* (timed parkour where the
  wrong ice cracks and yanks you to start), the Fairy *Five Tests of the Heart* (bring a
  **shiny, solo, nicknamed, well-fed, bonded** lead and win with only it), and Fire's
  *Trial by Flame* (fastest timed run).
- **"The world remembers you" beat.** The shrine keepers are the one NPC tier who react to
  the protagonist as an *ancient* presence rather than a Company face — dread poetry that
  seeds Act-3 without naming it.

---

## 2. Narrative role

| Property | Value |
|---|---|
| Act | **Cross-act optional** — each shrine unlocks with its adjacent gym/league milestone (§4), spanning Act 1→3. Not on the mainline critical path. |
| `cd_instability` | **No change.** Shrines do not touch the index (verified: no `band_tags` / instability writes in `shrine_*` dialog or `ShrineChallengeManager`). Recommend keeping them instability-neutral — they are *outside* the Company economy story. |
| Memory fragment | **None wired.** Shrine leader defeat grants an advancement + crystal, not a `memory/*` fragment. **Opportunity** (§5) — the "land remembers" dialog is the natural place for a fragment. |
| Recognition tier | **Special "elemental / land recognises him" tier** — distinct from the Company recognition gradient. The keepers sense an *old gravity* / *long shadow already cast* (grounded in dialog, §4), never Company allegiance. Civilians-never / Mom-never rules are untouched. |
| Canon ties | **Currently zero.** `docs/LORE_BIBLE.md` has **no** shrine mention (grepped). Shrines are self-contained folklore. The keeper lines already lean into the amnesia-recognition motif, so they enrich the mystery *tonally* without contradicting canon. |

`docs/ENGINE_FINDINGS.md` §"Content frontier" already lists shrines as **staged-but-not-authored**
future content ("20 empty `{}` trainer teams … 5 shrine leaders, 10 shrine cultists";
"code plumbing but no authored content yet"). This audit is consistent with that: the
*machinery* is complete, the *content wiring* is stubbed.

---

## 3. Layout & placements

Zones are **builder-confirmed** (`install.json`, `type:SHRINE`, cylindrical, `mobsSpawn:false`,
`hostileOnly:true`, `announce:true`). Trainer coordinates are **config-declared** in
`data/cobblemon_initiative/trainers/shrines/*.json` — but the leader **NPC bodies have no
`uuid`/`placement`** in their character files (same convention as every gym leader — builder-placed /
adopted), so "body present" still needs builder confirm.

### Shrine → adjacent gym / league milestone (mapping CONFIRMED by prereq tag **and** coords)

| Shrine | Zone (install.json) | Trainer cluster coord | Adjacent town (gym leader coord) | Gate (prereq tag) | Level cap in force at gate |
|---|---|---|---|---|---|
| **Fairy** | "Fairy Shrine" `#…` `centerY` per zone | `[947,-7,2715]`–`[957,-7,2718]` | **Mystic Marsh** (gym 3, `[1073,65,2441]`) ~300 blk | `mystic_leader` | **37** |
| **Ground** | "Ground Shrine" `color #D4A94E` `centerY 64` | `[1899,83,4049]`–`[1910,83,4049]` | **Kalahar Reach** (gym 6, `[2085,126,4050]`) ~185 blk | `kalahar_leader` | **56** |
| **Dragon** | "Dragon Shrine" `color #7C4DFF` `centerY 64` | `[1998,66,921]`–`[2008,66,921]` | **Ryujin Keep** (gym 8, `[2156,201,884]`) ~160 blk | `ryujin_leader` | **68** |
| **Ice** | "Ice Shrine" `centerY` per zone | `[3634,68,1960]`–`[3644,68,1960]` | **Nifl Town** (gym 9, `[3608,112,2031]`) ~76 blk | `nifl_leader` | **74** |
| **Fire** | "Fire Shrine" `centerY` per zone | `[3500,51,4702]`–`[3510,51,4702]` | **Scorchspire** (gym 10, `[3700,100,4511]`) ~275 blk | `royal_champion` | **85** |

> **Note:** Fire's gate is **post-Royal-League** (`royal_champion`), *not* Scorchspire —
> it sits near the gym-10 town but is a post-league superboss.

### NPCs / props per shrine

| Entity | Role | Coord | Source / status |
|---|---|---|---|
| `<type>_shrine_leader` (×5) | High Priest(ess) boss + dialog | leader coord in `trainers/shrines/*.json` (e.g. Fire `[3510,51,4702]`) | Character file exists; **no uuid/placement** → builder body TBD |
| `<type>_shrine_cultist_1..4` (×20) | PvP ladder acolytes | coords in `trainers/shrines/*.json` | **Config only — no character/dialog/body files exist** |
| `dragon_hydra_1/2/3` | Hydra-gauntlet stage trainers | `[1998,66,921]` / `[2001,66,924]` / `[2004,66,921]` | Config only (`trainers/shrine_challenges/dragon_hydra.json`); **no body, not wired to a challenge trigger** |
| Fairy **altar** prop + trigger | `shrine fairy test resolve` interaction | **PROPOSED** — at fairy leader cluster `~[957,-7,2715]` | Does not exist; needed to wire the Five Tests (§8) |
| Parkour **start/finish** command blocks (Ice, Fire) | fire `shrine <id> complete` | **PROPOSED** — builder placement | Does not exist; challenge is un-triggered (§8) |

---

## 4. Core structure (per shrine — the "gym-equivalent" loop)

Each shrine is architecturally **a 4-acolyte PvP ladder → 1 High Priest**, mirroring a gym's
interior ladder (prereq chain = the PvP gate), **overlaid** with a signature *challenge type*
from the Java `ShrineChallengeManager`. The ladder gating is real and shipped; the challenge
overlay is coded but **not yet triggered by anything** (see §8 gap).

### 4a. The PvP ladder (all five shrines, `trainers/shrines/*.json`)

Gate chain per shrine (each battle gated on the prior `defeated_*` via `prerequisites`):

```
<gym>_leader → cultist_1 ┐          cultist_1 → cultist_3 ┐
             → cultist_2 ┘   then   cultist_2 → cultist_4 ┘  then both → leader
```

- `cultist_1/2` unlock on the adjacent-gym clear; `cultist_3` needs `cultist_1`,
  `cultist_4` needs `cultist_2`; the **leader** needs `cultist_3` **and** `cultist_4`.
- **The leader battle is the DOUBLE** where flagged: **Ground** (`GEN_9_DOUBLES`, High Priest
  Terran) and **Dragon** (`GEN_9_DOUBLES`, High Priest Draconis). Fairy / Ice / Fire leaders
  are `GEN_9_SINGLES`. Every cultist is `GEN_9_SINGLES` except the Dragon *hydra* stages.
- Each cultist reward = `3× cobblemon:ultra_ball`; each leader = crystal + `10× rare_candy` +
  `5× diamond` (Fire upgrades to `1× master_ball` + `1× netherite_ingot`).

### 4b. Leader team sketches (ace / full roster, vs the cap-at-gate)

Aces are **way above the +2 gym rule** because shrines are optional superbosses wielding
legendaries — but the premium is **inconsistent** (see §9 balance finding).

| Shrine (fmt) | Leader | Roster (levels) | Ace | Ace vs gate-cap |
|---|---|---|---|---|
| Fairy (Singles) | High Priestess **Aurora** | Clefable 44, Gardevoir 44, Primarina 45, **Xerneas 48** | Xerneas 48 | **+11** over cap 37 ⚠ |
| Ground (Doubles) | High Priest **Terran** | Garchomp 58, Hippowdon 57, Swampert 58, **Landorus 60** | Landorus 60 | +4 over cap 56 |
| Dragon (Doubles) | High Priest **Draconis** | Dragonite 70, Salamence 70, Garchomp 72, **Rayquaza 75** | Rayquaza 75 | +7 over cap 68 |
| Ice (Singles) | High Priest **Glacius** | Lapras 74, Glaceon 74, Walrein 75, **Kyurem 78** | Kyurem 78 | +4 over cap 74 |
| Fire (Singles) | High Priest **Ignis** | Volcarona 80, Arcanine 80, Charizard 82, **Heatran 82** | Heatran 82 | **−3 under** cap 85 ⚠ |

### 4c. The five challenge overlays (`ShrineChallengeManager`, `shrine_challenges/*.json`)

The three challenge *types* the brief asked to confirm are present **plus two more** — the
Java doc-comment's list is authoritative: `hydra_gauntlet`, `fairy_tests`, `timed_parkour`,
`dark_gauntlet`.

| Shrine | `type` | Config knobs | Java behaviour |
|---|---|---|---|
| **Dragon** | `hydra_gauntlet` | `stageTrainerIds:[dragon_hydra_1,_2,_3]` | 3 sequential battles; **full party heal between stages** (`healParty`); complete on stage-3 win. Stages 1–2 are `GEN_9_DOUBLES`, stage-3 "Omega" is `GEN_9_SINGLES` w/ **Latios + held items** (life orb / choice scarf / choice specs). |
| **Fairy** | `fairy_tests` | `friendshipThreshold 160`, `fullnessThreshold 50`, `cultistLeaderTrainerId fairy_shrine_leader` | 5 checks on lead mon: friendship / fullness / nickname / shiny / **resolve** (all four **+ solo party**); resolve registers the mon's UUID, then you must **beat Aurora with that exact mon still leading**. |
| **Ice** | `timed_parkour` | `timeLimitSeconds 180`, `iceFloorEnabled true`, hazard blocks = ice/packed/blue/frosted | Reach finish before timer; **ice-floor hazard** — stepping on un-recorded hazard ice deals `freeze` damage + teleports to start (`punishIce`), tuned in ModMenu `ShrineConfig`. Countdown warnings at 60/30/10/5/3/2/1s. |
| **Fire** | `timed_parkour` | `timeLimitSeconds 120` (no ice floor) | Pure speed run; finish line fires `shrine fire complete`. |
| **Ground** | `dark_gauntlet` | `targetTrainerId ground_shrine_leader`, `earthquakeIntervalSeconds 45`, `earthquakeRadius 20` | Start at **half health** (ModMenu fraction) + **permanent refreshing blindness**; every 45s an **earthquake** flings you up to 20 blocks with nausea; complete on Terran's defeat. |

Runtime tunables live in ModMenu `ShrineConfig` (ice damage/cooldown/sound, dark-gauntlet
start-health fraction + blindness refresh, earthquake sound/nausea) — consistent with the
"tunables in ModMenu, not jar-baked JSON" preference. Dev safe-path authoring is fully built
(`shrine <id> path record|here|clear|show|export`, particle trails, world-persisted +
config-baked union).

---

## 5. Quests & side quests (enrichment — mostly PROPOSED)

Shrines currently ship **no side quests** — just the ladder + boss. Light, on-theme adds
that reuse existing systems:

| # | Name | Giver | Hook | Steps | Gates (tags) | Reward | Resolution |
|---|---|---|---|---|---|---|---|
| S1 | **"The Land Remembers"** (memory fragment) | each shrine leader, post-defeat | The keeper's after-battle line already says the element "knew you before you came" | On first shrine leader defeat, fire a `memory/shrine/frag_*` | `defeated_<type>_shrine_leader`, `not_tag` frag-claimed | 1 memory fragment (the recognition tier's own line into the mystery) | Wires shrines into the Act-3 breadcrumb without touching cd_instability |
| S2 | **"Five Keepers"** meta-collection | PROPOSED wandering pilgrim near any shrine | Rumour that all five crystals "answer to one hand" | Defeat all 5 leaders → `all_shrines` (already coded in `PlayerProgressManager`) | `defeated_*_shrine_leader ×5` | Already grants `all_shrines` advancement — **add a CD/relic payout on top** | Ties the five into one arc; streamable completion beat |
| S3 | **"Trial of Resolve"** (wire the Fairy tests) | Fairy altar prop (PROPOSED) | Aurora demands you face her with a single bonded shiny | `shrine fairy start` → `test resolve` → beat Aurora solo | active fairy challenge + registered UUID | The catch of your life at cap; bragging rights | The signature "insane Nuzlocke" clip — *if* wired (§8) |

Keep tone: corporate-dread comedy is **off** here — shrines are the one place the game plays
the mystery *straight*. That contrast is a feature.

---

## 6. Trainers & teams needed (concrete gap list)

**Teams exist inline** in `trainers/shrines/*.json` and `trainers/shrine_challenges/dragon_hydra.json`
(full species/level/moves/EVs). The **canonical RCT team files are empty stubs** — this is the
main wiring debt (and matches ENGINE_FINDINGS' "20 empty `{}`" note).

| File set (`data/rctmod/trainers/`) | State | Action |
|---|---|---|
| `<type>_shrine_leader.json` ×5 | `{}` empty | **Populate** from inline `trainers/shrines/*.json` leader teams |
| `<type>_shrine_cultist_1.json`, `_2.json` ×10 | `{}` empty | Populate from inline cultist_1/2 teams |
| `<type>_shrine_cultist_3.json`, `_4.json` ×10 | **Missing entirely** | Create (inline configs define cultist_1–4; RCT only stubs 1–2). **Reconcile** the 2-vs-4 cultist count (ENGINE_FINDINGS says "10 shrine cultists" = 2/shrine). Either the 4-cultist ladders are the intent → create 3/4, or trim ladders to 2. |
| `dragon_hydra_1/2/3.json` | **Missing** | Create if the Hydra Gauntlet ships as a distinct overlay (teams already inline) |

Battle formats vs cap ladder are already correct in config (Ground/Dragon leaders =
`GEN_9_DOUBLES`, rest Singles; hydra stages 1–2 Doubles). No format changes needed — only
**team population** + the balance retune in §9. Registry configs
(`trainers/shrines/*.json`, `trainers/shrine_challenges/dragon_hydra.json`) already carry
`id`, `prerequisites` chain, `battleFormat`, `rewards`, `coordinates`, `achievementOnDefeat`.

---

## 7. Economy & rewards

| Reward | Value | Notes |
|---|---|---|
| Leader `prize` (CD) | **2500** each (character `battle.prize`) | Flat across all 5 — fine; shrines aren't an economy lever |
| Cultist item drops | `3× ultra_ball` each | Ladder consolation |
| Leader item drops | crystal + `10× rare_candy` + `5× diamond` (Fire: crystal + `master_ball` + `netherite_ingot`) | `rare_candy ×10` is a real power spike — intended |
| **Shrine crystal → legendary** | `spawnpokemon <box-legendary> level=70` | The marquee reward; player-timed |
| `all_shrines` advancement | granted when all 5 leaders defeated | Already coded; **no CD/item attached** — recommend a capstone payout (§5 S2) |

**No CD sinks** are shrine-specific and none are needed — shrines are a reward *faucet*, not a
sink. They do **not** interact with shop tiers or field liberation (correct: they're outside
the wheat economy).

> ⚠ **Double-grant risk (concrete):** `PlayerProgressManager.onTrainerDefeated` calls
> `grantRewards(...)` (which iterates the `rewards` array — that array **already contains**
> `<type>_shrine_crystal`) **and** `grantShrineCrystal(...)` (which grants the crystal again by
> trainerId switch). On a shrine-leader win both fire → **two crystals → two legendaries.**
> Pick one path (recommend: drop the crystal from the `rewards` array, keep the Java grant, so
> the "place-to-summon" message is guaranteed).

---

## 8. Implementation notes / FUTURE-ME HOOKS

**The single biggest gap: the challenge overlays are orphaned.** The entire
`ShrineChallengeManager` (parkour timers, ice floor, dark gauntlet, hydra, fairy tests) is
started **only** by `/cobblemon-initiative shrine <id> start`, and **nothing calls it.** The
five `dialog-src/dialog/shrine_*.json` trees only fire `{"do":"battle"}` (the leader fight) —
verified: they contain no `command` action. So today a shrine = ladder + boss; the signature
gauntlet never engages.

To wire each overlay (copy the proven `{"do":"command", ... , "as_player":true}` pattern —
see `dialog-src/dialog/sq_sprint.json` which calls a `function`, and
`dialog-src/dialog/sq_perf_review_guide.json` for multi-command buttons):

1. **Ground / Ice / Fire / Dragon — add a "begin trial" button** to the `default` entry of
   `dialog-src/dialog/shrine_<type>.json` with
   `{"do":"command","cmd":"cobblemon-initiative shrine <type> start","as_player":true}`
   *before* the leader `battle` button (or gate the battle button behind the challenge, per
   type). Recompile: `scripts/content_compile` → `scripts/update_preset_index` →
   `scripts/generate_npc_function`.
2. **Parkour finish (Ice, Fire)** — builder places a plate/command block running
   `execute as @a[distance=..3] run cobblemon-initiative shrine <id> complete` (grammar from
   `ShrineChallengeManager.completeParkour` doc-comment). **PROPOSED** placement at the
   summit; no terrain build implied — a single command block/plate on existing geometry.
3. **Fairy altar** — builder prop at `~[957,-7,2715]` **(PROPOSED)** with a button/NPC firing
   `shrine fairy start`, then a second interaction for
   `shrine fairy test resolve`. The tests are individual feedback commands
   (`friendship|fullness|nickname|shiny|resolve`).
4. **Dark gauntlet / Hydra** need no finish-line block — they complete on `onTrainerDefeated`
   (already wired: `PlayerProgressManager` → `getShrineChallengeManager().onTrainerDefeated`).
   Just the "start" button (step 1) + placing the stage trainers.

**Files to create/edit (exact paths):**

- Teams: populate `src/main/resources/data/rctmod/trainers/<type>_shrine_leader.json`,
  `<type>_shrine_cultist_1..4.json`, `dragon_hydra_1/2/3.json` from the inline configs.
- Character/body files for cultists: `dialog-src/characters/shrine/<type>_shrine_cultist_1..4.json`
  (none exist — copy a gym interior-trainer character, e.g. `dialog-src/characters/hua_zhan/station_*`).
- Dialogs: edit the 5 existing `dialog-src/dialog/shrine_<type>.json`; add cultist dialogs if
  cultists get bodies.
- Crystal dedupe: `src/main/java/…/data/PlayerProgressManager.java` (§7) — or strip the crystal
  line from each `trainers/shrines/*.json` `rewards` array.

**Gotchas:**

- Macro-delivered text rule: any memory-fragment / economy / onwin strings you add must have
  **no double-quotes and avoid apostrophes** (the shrine leader win/lose lines are macro-safe
  already — keep new ones that way).
- Crystal legendary is a **flat L70 wild spawn** with no boss/no-despawn flag — a cap-37 player
  who beats Fairy gets an un-usable L70 Xerneas (over their cap). Decide whether crystals are
  **hold-until-cap** trophies (fine) or should spawn at a cap-appropriate level.
- Empty `{}` team warnings from `content_compile` on shrine refs are the **expected** staged-content
  flags per ENGINE_FINDINGS — not new breakage, but they're the checklist for §6.

---

## 9. Dependencies & open questions

**Depends on (other area keys):**

- `mystic_marsh`, `kalahar_reach`, `ryujin_keep`, `nifl_town`, `scorchspire` — each shrine's
  gate tag is the adjacent gym's `_leader` defeat; balance is pinned to that town's entry cap.
- `royal_league` — **Fire** shrine gates on `royal_champion`, not a gym.
- `mainline_spine` — for the (proposed) memory-fragment hook + `all_shrines` capstone.
- `gym_system_pvp_doubles` — Ground/Dragon leaders + hydra stages 1–2 are `GEN_9_DOUBLES`;
  reuse the doubles battle-button pattern from the reference gyms.
- `legendaries_nobles` — crystals spawn box legendaries (Moltres/Groudon/Articuno/Rayquaza/
  Xerneas); coordinate so shrine crystals don't collide/duplicate a legendary the
  legendaries-nobles system also hands out.

**Decisions the showrunner must make:**

1. **Balance floor.** Fairy is **+11 over gate cap** (Xerneas 48 vs cap 37) while Fire is
   **−3 under** (Heatran 82 vs cap 85). Set a consistent *shrine-superboss* rule (proposed:
   ace = gate-cap **+4..+6**, cultists at gate-cap ±2) and retune — **teams only, never the
   cap ladder.** Fairy especially: either raise its gate (e.g. require a later badge) or lower
   Aurora's roster.
2. **Ladder depth.** Configs define **4 cultists/shrine (20)**; RCT stubs + ENGINE_FINDINGS
   assume **2/shrine (10)**. Confirm 4 vs 2, then create or trim accordingly.
3. **Wire the challenge overlays?** Ship the parkour/dark/hydra/fairy-tests gauntlets (high
   FUN, needs the §8 dialog/altar/finish wiring), or ship shrines as ladder+boss only and
   shelve the (already-coded) overlays? Recommend **wire them** — the code is done and the
   Fairy "shiny solo" and Ground "blind + earthquakes" trials are peak stream content.
4. **Crystal ↔ ace mismatch.** Reward legendary ≠ leader's ace for Ground (Landorus vs
   **Groudon**), Ice (Kyurem vs **Articuno**), Fire (Heatran vs **Moltres**); only Fairy
   (Xerneas) and Dragon (Rayquaza) match. Intentional (partner vs prize) or align them?
5. **Crystal spawn level** at low-cap access (open question in §8 gotchas).
6. **Fix the double-crystal grant** (§7) — pick the rewards-array path or the Java path.
