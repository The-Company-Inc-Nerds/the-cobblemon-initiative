# 02 — Reusable PvP Gym Structure + Double-Battle Conventions

> **This is the single source every gym agent (gyms 3–10) copies.** It defines the
> canonical interior PvP ladder, where the double battle slots in, the exact `tbcs`
> onwin command shape, the rctmod-team + `gyms/<town>.json` + registry wiring, and a
> copy-paste checklist to stamp a new gym. It is grounded in the two fully-built gyms —
> **Gym 1 Takehara Falls** and **Gym 2 Hua Zhan City** — and in
> `docs/ENGINE_FINDINGS.md`. Where a gym's own content (villain latch, flavor NPCs)
> varies, that lives in the per-gym area doc; the *mechanical spine* lives here.

---

## 1. Concept & fantasy

**One-line pitch:** *Every gym is a five-rung boss rush that peaks on a chaotic
double battle and ends on a leader you are forced to fight underleveled — a scripted
Nuzlocke pressure-cooker the audience learns to read.*

The fun is **rhythm and escalation**, identical in shape across all ten gyms so the
audience internalises it and can feel the difficulty ramp:

1. **Two floor grunts** — warm-up, type-flavored, ~entry-cap −5/−4. Chip damage, the
   streamer scouts the type matchup live.
2. **Jr. Apprentice** — a single at ~cap −3 that punishes a bad lead.
3. **Apprentice — the DOUBLE.** The marquee mid-gym spike: a 4-mon `GEN_9_DOUBLES`
   set at ~cap. Spread moves, target selection, protect mind-games — the format the
   audience *doesn't* get in the wild, so it always reads as an event. In a hardcore
   Nuzlocke a double is where runs die: two threats a turn, no room to stall.
4. **Leader — the ace at ENTRY-CAP + 2.** You physically cannot out-level it (the cap
   is enforced). Fought *underleveled, on purpose*. This is the stream's recurring
   heartbeat: badge fights are the death-clock moments the whole VOD builds toward.

Marquee moments the structure manufactures for free, every gym: the double-battle
"oh no both of them" turn; the leader-ace reveal at +2; the badge-get economy sting
(shop tier unlock + a memory fragment cutscene). Because the *shape* never changes,
callbacks land — "remember what the apprentice double did to us in Hua Zhan" is a
thing the chat can say nine gyms later.

---

## 2. Narrative role

Gyms are the **spine of Act 1 infiltration (gyms 1–7)** and the mid/late progression
gates (gyms 8–10). Individually a gym is type-fantasy neutral; the *villain* pressure
latches **beside** it (see §5, the roof-suits pattern). Each **leader defeat** fires the
same three narrative payloads (verified in `gyms/takehara_falls.json` &
`gyms/hua_zhan_city.json` leader `rewards`):

| Payload | Command (leader `rewards[]`) | Effect |
|---|---|---|
| Memory fragment | `execute as {player} run function cobblemon_initiative:memory/gym/frag_<N>` | +1 `memory_fragment` score → HUD quest slot advances, next-gym town resolves |
| Shop tier step | `cobblemon-initiative shop badge_<N>` | Pokémart stock steps up one tier |
| Currency destabilize | `execute as {player} run function cobblemon_initiative:economy/gym_destabilize` | nudges `cd_instability` upward (the Company's plan is *working* as you climb) |

- **cd_instability:** rises across the gym journey (each gym clear = one destabilize
  tick). It is only *pulled back down* at the HQ raid (`hq_stabilize`, Acting CEO DJ →
  idx 25). Gyms never lower it. (ENGINE_FINDINGS §economy: gym destabilize skews up;
  `hq_stabilize` clamps downward only.)
- **memory_fragment** doubles as the **badge counter (0..9)** the HUD reads
  (`function/quest/render.mcfunction`, `function/quest/gym_town.mcfunction`).
- **Recognition tier** is a *derived band* off badge count, not a per-gym flag:
  `early` = unconditional, `mid` = badges ≥ 3, `late` = badges ≥ 7 (compiler
  `RECOG_BAND` in `scripts/content_compile`). So gym leaders' recognition dialog
  escalates automatically as you clear gyms — grunts stay confused-hostile early,
  management alarms mid, some stand down late. **Civilians never recognise the
  protagonist; leaders never name the Founder before Act 3** (canon rule).
- Post-badge leader lines may reference `fields_liberated` once the wheat war opens
  (e.g. Blossom's `fields_thanks` entry, gated `defeated:hua_zhan_leader` +
  `fields_liberated ≥ 1`). That tie is **optional flavor**, owned by the per-gym doc,
  not the mechanical spine.

---

## 3. Layout & placements

**No terrain building. Placement only.** Every gym config *already ships builder-set
per-rung coordinates* — treat them as builder-confirmed and read them straight from
`data/cobblemon_initiative/trainers/gyms/<town>.json`. The table below is the
apprentice(DOUBLE) + leader anchor for each unbuilt gym (from the shipped configs) so a
gym agent has the marquee coords at a glance; the four floor rungs + jr sit within ~10
blocks (also in the config).

| # | Town (type) | Apprentice = DOUBLE (coords) | Leader (coords) | `achievementOnDefeat` |
|---|---|---|---|---|
| 1 | Takehara Falls (Bug) | Aiko `1904 109 2521` | Cicada `1910 109 2524` | `badge_bug` |
| 2 | Hua Zhan City (Grass) | Sakura `1495 86 2056` | Blossom `1501 86 2054` | `badge_grass` |
| 3 | Mystic Marsh (Fairy) | Faye `1067 65 2438` | Titania `1073 65 2441` | `badge_fairy` |
| 4 | Deepcore City (Fighting) | Ken `1039 129 3183` | Bruno `1045 129 3186` | `badge_fighting` |
| 5 | Gaviota Port (Water) | Marina `618 82 3533` | Neptune `624 82 3536` | `badge_water` |
| 6 | Kalahar Reach (Ground) | Terra `2079 126 4047` | Gaia `2085 126 4050` | `badge_ground` |
| 7 | Cyber City (Electric) | Surge `1456 89 1182` | Volt `1462 89 1185` | `badge_electric` |
| 8 | Ryujin Keep (Dragon) | Tatsu `2150 201 881` | Ryujin `2156 201 884` | `badge_dragon` |
| 9 | Nifl Town (Ice) | Glacier `3602 112 2028` | Skadi `3608 112 2031` | `badge_ice` |
| 10 | Scorchspire (Fire) | (apprentice) `3694 100 4508` | (leader) `3700 100 4511` | `badge_fire` |

Coords above are **builder-confirmed** (shipped in the gym configs). Any *new* prop or
villain-latch NPC a gym agent adds is **PROPOSED (needs builder confirm)** and must be
grounded against the town's `install.json` zone polygon (e.g. Mystic Marsh zone verts
span X≈858–1275, Z≈2310–2579, `centerY 64`).

**Body-vs-spawn note (ENGINE_FINDINGS §4):** rctmod trainer *spawning is off*
(`globalSpawnChance=0`, forced at runtime). Interior trainers are therefore realised as
**placed Easy NPC bodies running `do:battle`**, not RCT wild spawns. Today only Takehara
jr/apprentice/leader are fully bodied; **every gym-2..10 leader and all floor trainers
still lack a uuid/placement** — that is the build work these area docs schedule.

---

## 4. Gym / core structure — the canonical ladder

### 4a. The five-rung PvP ladder (authoritative shape)

Every gym config is an array of trainer entries in
`data/cobblemon_initiative/trainers/gyms/<town>.json`. The ladder is enforced by the
`prerequisites` chain (each rung lists the `defeated_*`-equivalent ids that must be
cleared first). Verified identical in Takehara, Hua Zhan, and all pre-scaffolded gyms
3–10:

| Rung | `id` | `trainerType` | `battleFormat` | `prerequisites` | Level target (vs entry cap C) |
|---|---|---|---|---|---|
| Floor 1 | `<town>_trainer_1` | `trainer` | `GEN_9_SINGLES` | `[<prevLeader>]`¹ | ~C−5 |
| Floor 2 | `<town>_trainer_2` | `trainer` | `GEN_9_SINGLES` | `[<prevLeader>]`¹ | ~C−5 |
| Floor 3 | `<town>_trainer_3` | `trainer` | `GEN_9_SINGLES` | `[<prevLeader>]`¹ | ~C−4 (off-ladder, optional) |
| Floor 4 | `<town>_trainer_4` | `trainer` | `GEN_9_SINGLES` | `[<prevLeader>]`¹ | ~C−4 (off-ladder, optional) |
| Jr. | `<town>_jr_apprentice` | `jr_apprentice` | `GEN_9_SINGLES` | `[<prevLeader>, <town>_trainer_1, <town>_trainer_2]` | ~C−3 |
| **Apprentice — DOUBLE** | `<town>_apprentice` | `apprentice` | **`GEN_9_DOUBLES`** | `[<town>_jr_apprentice]` | ~C (ace = C) |
| **Leader** | `<town>_leader` | `leader` | `GEN_9_SINGLES` | `[<town>_apprentice]` + `achievementOnDefeat: badge_<type>` | **ace = C + 2** |

¹ Gym 1 floor/jr prereqs are `[]` (nothing precedes it). Gyms 2–10 gate their floor +
jr on the **previous gym's leader** (`takehara_leader`, `hua_zhan_leader`, …). This is
the *only* correct edge direction — see the cycle-crash rule in §8.

**Key facts about the ladder:**
- The **critical path is 4 rungs**: `trainer_1 + trainer_2 → jr → apprentice → leader`.
  Floors 3 & 4 are **off-ladder bonus battles** (XP/loot only; nothing depends on
  them). Do not add them to any prereq chain.
- The **DOUBLE is always the apprentice rung** — a 4-mon `GEN_9_DOUBLES` set with ace
  ≈ entry cap. (Takehara: Butterfree/Beedrill/Yanma/Ninjask 15–16. Hua Zhan:
  Roselia/Bayleef/Jumpluff/Leafeon 25–26.) This is the single canonical in-gym double.
- The **leader is `GEN_9_SINGLES`, 4 mons, ace at entry-cap + 2** (brutal Nuzlocke,
  fought underleveled). ENGINE_FINDINGS aces: **17 / 24 / 32 / 39 / 46 / 52 / 58 / 64
  / 70 / 76** for gyms 1–10. Whole roster shifts uniformly with the gym.

### 4b. Leader team sketch template (fill per gym)

Six-slot sketch (leaders currently ship 4 — expand toward 6 as balance allows). Ace =
entry cap C + 2; support 1–2 below; hazards/status on a wall; one held item per mon:

```
1. <type pivot>        C-1   defensive spread lead (screens / hazard)
2. <type sweeper>      C-1   setup + STAB
3. <type utility>      C     status/leech/spikes, held Big Root / Leftovers
4. ACE <signature>     C+2   held Choice Scarf / Miracle Seed / type-boost; the win-con
(5. <coverage>)        C     PROPOSED expansion slot
(6. <coverage>)        C+1   PROPOSED expansion slot
```

(See `data/rctmod/trainers/hua_zhan_leader.json` /
`gyms/takehara_falls.json` leader block for the exact IV/EV/moveset/heldItem shape.)

### 4c. Gate wiring — TWO independent systems (do not conflate)

There are **two "prereq" concepts** and they live in different files. A gym agent must
touch both, correctly:

| System | File | Field | What it gates | Cycle-crash risk |
|---|---|---|---|---|
| **A. InitiativeInit progression ladder** | `data/cobblemon_initiative/trainers/gyms/<town>.json` | `prerequisites` | Badge award, level-cap unlock, reward payout, HUD (server-side, by loser *displayName* match) | No |
| **B. rctmod series graph** | `data/rctmod/mobs/trainers/{single,groups}/*.json` | `requiredDefeats` | Level-cap **bookkeeping** only (`rctmod player add progress after <id>`). **Recurses with NO cycle guard** | **YES — a cycle = StackOverflow crash at world start** |

- **B pattern (mandatory):** each gym's floor/apprentice gate on **leader(N−1)**; gym 1
  gates on nothing. Group file `<town>_trainer.json` applies to all `<town>_trainer_N`
  ids. (See `mobs/trainers/groups/mystic_trainer.json` = `requiredDefeats
  [[hua_zhan_leader]]`; `single/takehara_leader.json` = `[[takehara_apprentice]]`.)
- **C. In-world dialog gate (Easy NPC).** `tbcs battle` **bypasses all rctmod + config
  gating** (ENGINE_FINDINGS: it calls rctapi `startBattle` directly). So *nothing stops
  the physical battle from starting* except an Easy NPC **action Condition**. The
  canonical realisation, verified in `sq_jr_sora.json` / `sq_apprentice_aiko.json`:
  - Each rung NPC's dialog has a **`beaten` entry** gated `defeated: <that rung's id>`
    (post-win flavor, higher priority so it outranks `default`).
  - The battle rung compiles a `not_tag: defeated_<id>` action Condition around
    `tbcs attach` + `tbcs battle` automatically (the `do:battle` lowering does this).
  - **Recommended hardening (design decision, see §9):** put a
    `gate: {defeated: <town>_apprentice}` on the **leader's** battle button so the
    badge fight cannot be cheesed before the gym is cleared. Gyms 1–2 currently leave
    the in-world ladder *soft* (only the config prereq is hard); document per gym.

### 4d. The exact `tbcs` command shape (winners-first onwin)

The `do:battle` action in a rung's dialog compiles (via `scripts/content_compile
battle_actions`) to this fixed sequence. **Author it declaratively** as the character's
`battle` block; the compiler emits the raw commands. What it emits:

```
# entity path, gated not_tag defeated_<trainer> — binds body to the RCTAPI trainer:
tbcs attach <trainer> @s

# ExecAsUser (source = player), gated not_tag defeated_<trainer>:
tbcs battle GEN_9_DOUBLES @s vs <trainer> onwin {1: [ <winner cmds> ], 2: [ <loser cmds> ]}
```

**onwin is WINNERS-FIRST** (ENGINE_FINDINGS §136): key `1` = *player won*, key `2` =
*NPC won*. In the win list `@1`=player, `@2`=NPC; in the lose list the tokens **mirror**
(`@1`=NPC, `@2`=player). The compiler builds both sides from the `battle` block:

| `battle` field | Win list (key 1) emits | Lose list (key 2) emits |
|---|---|---|
| `prize: N` | `cobbledollars give @1 N` | — |
| (always) | `tag @1 add defeated_<trainer>` | — |
| `win_line` | `@2 say <win_line>` (NPC speaks) | — |
| `lose_line` | — | `@1 say <lose_line>` (NPC speaks) |
| `loss_fee: N` | — | `cobbledollars remove @2 N` |
| `despawn_on_win` / villain types | `easy_npc delete @2` | — |

**Character `battle` block template** (from `hua_zhan_leader.json` character):
```json
"battle": {
  "trainer": "<town>_apprentice",
  "type": "one_time",
  "format": "GEN_9_DOUBLES",
  "defeat_tag": "defeated_<town>_apprentice",
  "win_line": "<no double-quotes, avoid apostrophes>",
  "lose_line": "...",
  "already_beaten_line": "..."
}
```
> **GOTCHA — no `prize` on gym-rung bodies.** The gym config `rewards[]` pay via
> `PlayerProgressManager`; adding a `prize` to the Easy NPC body **double-pays**.
> Apprentice/leader bodies omit `prize` (see `apprentice_aiko.json`: *"NO prize key —
> the gym config rewards pay"*). Villain side-battles (§5) **do** carry `prize`.

---

## 5. Quests & side quests — reusable gym-adjacent modules

These are the *repeatable content templates* a gym agent drops next to a gym. Per-gym
flavor is owned by the gym's own area doc; the wiring pattern is fixed here.

### 5a. The villain **tag-team double** (the second double convention)

The gym-internal double is the apprentice. The *other* canonical double is a **villain
tag-team**, built verified in Takehara as the roof-suits scene (`agent_yield_lead` +
`agent_yield_second`, trainer `sq_mayor_suits`, `GEN_9_DOUBLES`).

- **Two placed Easy NPC bodies, ONE shared `GEN_9_DOUBLES` battle.** The *lead* body
  carries the whole `battle` block (trainer `sq_<name>`, `format: GEN_9_DOUBLES`,
  `prize`, `defeat_tag`, `despawn_on_win: true`, villain win/lose lines). The *partner*
  body has **no battle block** — it **stands down in dialog** via an entry gated
  `defeated_sq_<name>` (priority 30). (Easy NPC can't self-delete from a teammate's
  onwin without a placed uuid, so the partner resolves data-only in dialog.)
- **Hook:** a pre-badge villain scene near the gym (roof, market row, dock). Act-1
  canon: *confused memo-tier hostility, zero reverence, never names the Founder.* The
  pitch always foreshadows the wheat plot as "agricultural yield optimization" — never
  the w-word early.
- **Reward:** `prize` CD in onwin (villain battles pay directly; not gym-progression).
  Winning sets `defeated_sq_<name>`, which can gate a leader's *retained/thanks* entry
  (Blossom's `retained_after` gates on `defeated_villain_yield_analyst`).

### 5b. Gym-guide breadcrumb (navigation)

Each gym ships a `<town>_guide` civilian (`dialog-src/characters/gym/<town>_guide.json`)
that points challengers up the ladder ("the apprentice tends the greenhouse — find her
first"). Reuse the `takehara_guide` / `hua_zhan_guide` pattern. Pure signposting, no
battle, `ambient_stationary_look`.

### 5c. Optional pilgrimage / seal-station chain (Hua Zhan pattern)

Hua Zhan's "Four Gardens" chain (`station_moss/orchard/pond/terrace`, dialog
`garden_pilgrimage`) is a **reusable optional module**: prop-style plaque NPCs
(`dialog_inline`, no uuid, `placement` latch) whose "press the seal" button gates
`defeated: <town>_trainer_N` + `not_tag: seal_<x>`, calls a per-town
`sidequest/<chain>/update` function, and unlocks a bespoke leader dialog entry
(`pilgrimage_done`) + a small reward. Recommend **one optional flavor chain per gym**,
authored in the gym's own doc — it turns the off-ladder floor trainers (3 & 4) into
something worth fighting.

---

## 6. Trainers & teams needed (per-gym manifest)

To make one gym fully playable, the following must all exist. **Bold = currently
missing for gyms 3–10** (ENGINE_FINDINGS §4: 24 team files missing —
`{town}_{trainer_3,trainer_4,jr_apprentice} × 8`; every gym-2..10 leader + all floor
bodies unbuilt).

| Artifact | Path | Count / gym | Status (gyms 3–10) |
|---|---|---|---|
| RCT team files (the actual `tbcs` teams) | `data/rctmod/trainers/<id>.json` | 7 (`trainer_1..4`, `jr_apprentice`, `apprentice`, `leader`) | trainer_1/2 + apprentice + leader exist; **trainer_3, trainer_4, jr_apprentice MISSING** |
| RCT series-graph spawn stubs | `data/rctmod/mobs/trainers/single/<apprentice,leader>.json` + `groups/<town>_trainer.json` | 3 | present (verify `requiredDefeats` = prev leader) |
| Gym config (ladder + rewards + coords) | `data/cobblemon_initiative/trainers/gyms/<town>.json` | 1 (7 entries) | **present & scaffolded** (ladder + DOUBLE + achievement all set) |
| Easy NPC bodies (character JSON) | `dialog-src/characters/**/<id>.json` w/ `battle` block | ≥3 (jr, apprentice, leader); +2 for floor_1/floor_2 if bodied | **only leader stub dialogs exist; bodies unbuilt** |
| Leader dialog tree | `dialog-src/dialog/gym_leader_<town>.json` | 1 | stub exists (`gym_leader_mystic.json` etc.) |

**Level targets vs the enforced cap ladder** (never move the ladder — retune teams):

| # | Town | Entry cap C | Floor 1–4 | Jr. | Apprentice DOUBLE (ace) | Leader ace (C+2) | Unlocks cap |
|---|---|---|---|---|---|---|---|
| 3 | Mystic Marsh | 30 | 25–26 | 27 | 30 | **32** | 37 |
| 4 | Deepcore | 37 | 32–33 | 34 | 37 | **39** | 44 |
| 5 | Gaviota | 44 | 39–40 | 41 | 44 | **46** | 50 |
| 6 | Kalahar | 50 | 45–46 | 47 | 50 | **52** | 56 |
| 7 | Cyber | 56 | 51–52 | 53 | 56 | **58** | 62 |
| 8 | Ryujin | 62 | 57–58 | 59 | 62 | **64** | 68 |
| 9 | Nifl | 68 | 63–64 | 65 | 68 | **70** | 74 |
| 10 | Scorchspire | 74 | 69–70 | 71 | 74 | **76** | 80 |

> **Empty-team trap:** rctapi refuses to start a battle for an empty/`{}` team
> (`insufficientPokemon`) — a missing `data/rctmod/trainers/<id>.json` is a battle
> button that *silently does nothing*. `content_compile` warns on every battle
> reference to a missing/empty team; treat those warnings as the missing-file checklist.

---

## 7. Economy & rewards

| Rung | Reward mechanism | Payload |
|---|---|---|
| Floor 1–4 | gym config `rewards[]` + `bag[]` | `cobblemon:potion ×2` (item, flat) |
| Jr. / Apprentice | gym config `rewards[]` | `cobblemon:super_potion ×2` |
| **Leader** | gym config `rewards[]` (3 commands) | memory frag `frag_<N>` + `shop badge_<N>` (tier step) + `economy/gym_destabilize` (idx↑). Prize CD via `PlayerProgressManager` payout, **not** an onwin `prize`. |
| Villain tag-team (§5a) | onwin `prize` | direct `cobbledollars give @1 N` (e.g. `sq_mayor_suits` prize 460). Villain battles are the CD *faucet* around a gym. |

- **CD sinks** near a gym: the Pokémart (whose stock tier just stepped up on the badge),
  and the *decline-battle* fee on any refusable trainer encounter. Leaders are not
  refusable.
- **Payout hygiene:** economy text is macro-delivered — **no double-quotes, avoid
  apostrophes** in any `win_line`/`lose_line`/fragment/economy string (compiler
  `check_onwin` hard-fails on `" ' %`).
- **Shop tiers** are regenerated by `scripts/generate_shop_tiers` / `scripts/shop_tiers`
  keyed off badge count; a new gym's `shop badge_<N>` step is already in the ladder — no
  per-gym shop authoring unless the gym adds bespoke stock.

---

## 8. Implementation notes / FUTURE-ME HOOKS

### 8a. Files to create/edit to stamp ONE gym (exact paths)

```
data/rctmod/trainers/<town>_trainer_3.json        # NEW team (ace ≈ C-4)
data/rctmod/trainers/<town>_trainer_4.json        # NEW team (ace ≈ C-4)
data/rctmod/trainers/<town>_jr_apprentice.json    # NEW team (ace ≈ C-3)  [ladder-critical]
   # (<town>_trainer_1/2, _apprentice, _leader teams already ship — retune levels to §6)
data/rctmod/mobs/trainers/single/<town>_apprentice.json   # verify requiredDefeats=[[<prevLeader>]]
data/rctmod/mobs/trainers/single/<town>_leader.json       # verify requiredDefeats=[[<town>_apprentice]]
data/rctmod/mobs/trainers/groups/<town>_trainer.json      # verify requiredDefeats=[[<prevLeader>]]
data/cobblemon_initiative/trainers/gyms/<town>.json       # EXISTS — verify ladder + levels + achievement
dialog-src/characters/gym/<town>_leader.json      # NEW body: role gym_leader, trainer=<town>_leader,
                                                  #   battle block (NO prize), uuid OR placement, dialog ref
dialog-src/characters/<town>/apprentice_<x>.json  # NEW body: DOUBLE, format GEN_9_DOUBLES, NO prize
dialog-src/characters/<town>/jr_<x>.json          # NEW body: jr rung
dialog-src/dialog/gym_leader_<town>.json          # EXISTS as stub — flesh out (after/default + flavor)
dialog-src/dialog/<apprentice/jr dialogs>.json    # NEW: default(battle) + beaten(gated defeated_<id>)
```

### 8b. Pipeline (run in order — this is the whole build step)

```
scripts/content_compile                 # lowers dialog-src → easy_npc presets, band_tags, placements
scripts/generate_granary_tiers          # (economy tiers; harmless if untouched)
scripts/update_preset_index             # rebuild Easy NPC preset index
scripts/generate_npc_function           # npc/preset_map.json + function/update_npc_presets.mcfunction
# in-game (OP 2):  /reload  →  /function cobblemon_initiative:update_npc_presets
#                            →  /function cobblemon_initiative:dialog/register_sight   (if any sight NPCs)
```
`gradle build` to package. **Never `git commit`** — write `GIT_COMMIT_MSG`, user runs
`gcommit` (omit the Co-Authored-By trailer per memory).

### 8c. Which existing pattern to copy

- **Leader body + dialog:** copy `dialog-src/characters/gym/hua_zhan_leader.json` +
  `dialog-src/dialog/gym_leader_hua_zhan.json` (richest: gated flavor entries + `after`
  + `default`, priorities 10–26).
- **Apprentice DOUBLE:** copy `dialog-src/characters/takehara/apprentice_aiko.json` (the
  reference `GEN_9_DOUBLES` body with no-prize note) + `sq_apprentice_aiko.json`.
- **Villain tag-team double:** copy `agent_yield_lead.json` (+ `sq_agent_yield_lead.json`)
  and `agent_yield_second.json` (+ its stand-down dialog).
- **Gym config:** `gyms/hua_zhan_city.json` is the fullest reference (previous-leader
  prereqs + DOUBLE apprentice + 3-command leader rewards).

### 8d. Gotchas (each has bitten a prior round — see ENGINE_FINDINGS)

1. **Series-graph cycle = crash at world start.** `requiredDefeats` edges come from
   BOTH `single/*.json` AND `groups/*.json`. Only edge direction that is safe:
   gym-N floor/apprentice → **leader(N−1)**; gym 1 → nothing. **Re-run a cycle check
   over singles+groups after any `requiredDefeats` edit.**
2. **`tbcs` ignores config/rctmod gating** — the physical battle is stoppable only by an
   Easy NPC action Condition (`not_tag: defeated_<id>`, auto-emitted) + your explicit
   leader-button gate. The config `prerequisites` only gate the *reward/unlock*.
3. **Two defeat systems fire on one win.** onwin `tag @1 add defeated_<id>` (the
   PLAYER_TAG dialog gates read) **and** Cobblemon `BattleVictoryEvent` → InitiativeInit
   name-match → progression + `rctmod player add progress after <id>`. The name match is
   on the trainer **displayName** — keep the gym config `displayName` and the body's
   `CustomName`/battle identity consistent.
4. **Grammar trap:** `rctmod player add progress <name> after <id>` — targets go BEFORE
   `before/after`. `add progress after <id> <name>` fails **silently**.
5. **onwin is winners-first** (`{1:player-won, 2:npc-won}`); lose list tokens mirror.
   Author the `battle` block, not raw onwin, and the compiler gets it right.
6. **No `prize` on gym-rung bodies** (double-pays); villain side-battles **do** carry it.
7. **No `"` in macro text**; avoid `'` and `%`. `check_onwin` hard-fails the build.
8. **`Prio` must be written on movement objectives** (missing = 0 = outranks all); gym
   NPCs use `ambient_stationary_look` (never wander — a strolling NPC keeps walking with
   the dialog open).

### 8e. COPY-PASTE CHECKLIST — stamp a new gym (run top to bottom)

```
[ ] 1. Read gyms/<town>.json — confirm 7 entries, ladder prereqs, DOUBLE at apprentice,
       achievementOnDefeat=badge_<type>. Fix levels to §6 (ace=C+2, uniform shift).
[ ] 2. Author 3 missing RCT team files: trainer_3, trainer_4, jr_apprentice
       (data/rctmod/trainers/). Retune trainer_1/2, apprentice, leader to §6 levels.
       Apprentice = 4 mons; leader = 4–6, ace held-item + C+2.
[ ] 3. Verify series graph: single/<apprentice,leader>.json + groups/<town>_trainer.json
       requiredDefeats point at previous leader / apprentice ONLY. Run cycle check.
[ ] 4. Author leader body (characters/gym/<town>_leader.json): role gym_leader,
       trainer=<town>_leader, battle block (format SINGLES, defeat_tag, win/lose/
       already_beaten lines, NO prize), dialog:gym_leader_<town>, ambient_stationary_look,
       placement OR uuid at the config leader coords.
[ ] 5. Author apprentice body (the DOUBLE): battle format GEN_9_DOUBLES, NO prize,
       dialog with default(do:battle) + beaten(gate defeated_<town>_apprentice).
[ ] 6. Author jr body: same shape, GEN_9_SINGLES.
[ ] 7. Flesh out dialog/gym_leader_<town>.json: default(10)+after(20 gate defeated_leader);
       optional recognition/villain-tie flavor entries (priorities 14–26). NO double-quotes.
[ ] 8. (Recommended) add gate {defeated:<town>_apprentice} to the leader's battle button.
[ ] 9. (Optional) drop the §5a villain tag-team double + §5b guide + §5c pilgrimage.
[ ] 10. Run pipeline (§8b): content_compile → generate_granary_tiers →
        update_preset_index → generate_npc_function. Resolve all content_compile warnings
        (esp. missing/empty team + act-1-no-placement).
[ ] 11. gradle build. In-world: /reload → update_npc_presets → register_sight.
[ ] 12. Smoke test: fight the ladder in order; confirm badge, cap unlock, memory frag,
        shop tier, cd_instability tick, HUD next-town advance. Log in SMOKETEST.md.
[ ] 13. Write GIT_COMMIT_MSG (no Co-Authored-By). Bump alpha suffix.
```

---

## 9. Dependencies & open questions

### Depends on (other area keys)
- **`mainline_spine`** — owns the shared systems every gym leverages: `memory_fragment`
  score + `memory/gym/frag_<N>` functions, `cd_instability` + `economy/gym_destabilize`,
  shop-tier stepping (`shop badge_<N>`), the recognition band definitions, and the HUD
  (`quest/render`, `quest/gym_town`). This doc *consumes* those; it does not define them.
- **`wheat_war_farms`** — provides `fields_liberated` and the `liberation/free_field`
  hook that optional post-badge leader dialog (e.g. `fields_thanks`) references.
- **Consumed BY (every gym area doc):** `mystic_marsh`, `deepcore_city`, `gaviota_port`,
  `kalahar_reach`, `cyber_city`, `ryujin_keep`, `nifl_town`, `scorchspire` — each stamps
  this pattern. `company_hq` depends on gyms 1–7 clears (badge-7 + 4 fields) as its gate.
  `royal_league` / `board_and_founder` continue the same ladder mechanics past gym 10.

### Open questions — DECISIONS the showrunner must make
1. **Floor trainers: bodied or cut?** With RCT spawning off, floors 1–4 need Easy NPC
   bodies to be fightable. Options: (a) body **all 4** (most content, most build time);
   (b) body only **trainer_1 + trainer_2** (the ladder-critical pair) and cut 3 & 4 from
   the world; (c) body 1+2 and repurpose 3 & 4 as the optional pilgrimage seal-gate
   targets (§5c). **Recommend (c).** Affects the 24-missing-team-file scope.
2. **Hard-gate each rung's battle button, or only the leader?** Gyms 1–2 leave the
   in-world ladder soft (config-only). Recommend at minimum gating the **leader** button
   on `defeated_<town>_apprentice` so the badge can't be skipped. Gate every rung? (more
   linear, less exploit surface, slightly more authoring).
3. **Expand leaders 4 → 6 mons?** Current leaders ship 4. Six makes the marquee fight
   beefier but risks over-tuning a brutal-Nuzlocke leader. Per-gym call; keep ace at C+2
   regardless.
4. **One villain tag-team double per gym, or only at story-beat gyms?** The §5a pattern
   is reusable everywhere but the Act-1 recognition arc (gyms 1–7) is where it earns its
   narrative keep; gyms 8–10 are past the HQ raid. Decide which gyms get a villain latch.
5. **Whether any gym's leader dialog should reference `fields_liberated`** (wheat tie) —
   only meaningful for gyms fought after the wheat war opens. Owned per-gym doc; flag
   here so the dependency on `wheat_war_farms` band-tags is declared.
