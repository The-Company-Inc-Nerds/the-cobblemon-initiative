# 15 — Battle Frontier (post-League proving grounds)

> Area key: `battle_frontier`. Act 3 side-content, played in the level-85→100 window.
> Source files studied: `data/cobblemon_initiative/trainers/battle_frontier/battle_frontier.json`
> (all 24 trainer configs already exist), `install.json` (the `Battle Frontier` TOWN zone +
> `Frontier Causeway` route), `trainers/royal_league/royal_league.json`,
> `trainers/gyms/hua_zhan_city.json`, `docs/LORE_BIBLE.md`, `docs/ENGINE_FINDINGS.md`,
> the dialog/character schemas, and `hua_zhan_leader` rctmod team + graph node.

---

## 1. Concept & fantasy

**One-line pitch:** *Seven towers, one throne — the Champion's victory lap becomes a hunt
for their own scrubbed record, each facility a different way to lose everything.*

The Frontier is the endgame **battle sandbox**: "just battles," as the showrunner says —
but each of the seven doors is a *different way to die* in a hardcore Nuzlocke, so the
sameness is the point being subverted. The player is already Champion (cap 85) and walks
into a plaza of specialist arenas where the region's elite battlers hold court. There is
no story gate to open here and no town to save; the reward is **mastery, flex loot, and
one quiet, load-bearing recognition beat**.

Marquee stream moments:
- **The no-heal ascent (Pyramid).** Every other facility sells a between-battle heal.
  The Pyramid does not. A back-to-back gauntlet with permadeath stakes and no safety net.
- **All-in at the Castle.** A real `wager` battle: bet CobbleDollars, win double or forfeit.
  Peak stream tension when the chat has been begging the player to bet the whole wallet.
- **Spin the Arcade wheel.** A `/random` roulette rolls the opponent's opening condition
  (rain / sun / sand / snow / trick room / tailwind) — the player commits *before* they know.
- **The Hall of Fame ghost.** The Frontier's all-time streak record is held by a name
  scrubbed to `§k` static. As the player climbs, the board reports *you have matched the
  record held by ████████.* They are chasing their own erased self — the mirror-battle
  theme, one act early, name never spoken (canon-safe).
- **The grand brain in the dark.** The eighth, capstone brain (Cave Warden Selene, Darkrai
  lead) sits in the Deep Dark and only opens once all seven facility brains have fallen —
  the true "you cleared the Frontier" beat, the last thing before the Board.

---

## 2. Narrative role

| Field | Value |
|---|---|
| **Act** | 3 (post-Royal-League; runs *parallel to / before* the Board of Directors clearout) |
| **`cd_instability`** | **25** (the stabilised post-DJ index; the Frontier inherits it, never moves it) |
| **Memory fragment** | **None new.** The 10-fragment drip closes at Scorchspire (`frag_10`). The Frontier is post-fragment; its recognition beat is the **Hall of Fame ghost record**, which *circles* the reveal without closing it — consistent with "never name the Founder before the mirror." |
| **Recognition tier** | **late** — some elites *stand down*, some *double down*. Civilians/spectators never recognise him. |
| **Economy voice** | Post-HQ **stabilised-then-exposed** register: any Company-branded signage in the plaza glitches (`§k`) and leaks cover-up lines; the Frontier itself is *neutral prestige ground* (not a Company holding). |

**Canon ties.** The Frontier is the region's proof-of-mastery institution — older than the
current Company regime, which is *why* the Founder once held records here. That gives one
diegetic recognition hook (the record board) and one optional NPC beat (an old brain who
battled the Founder years ago) without turning the Frontier into another villain map. The
brains are **not** Company; keep them clean so the plaza reads as a breather between the
League and the Board.

**Why it is gated where it is.** Entry opens on `royal_league_champion` (cap 85). The
first-clear tier is tuned to be beatable *underleveled* at 85 (brutal, on-brand). A second
**record/rematch tier at level 100** is gated on `board_cleared` (cap 100) so the level-100
super-brain teams already sitting in the config become a *fair* fight, not an impossible
wall. See §4 and §6.

---

## 3. Layout & placements

The `Battle Frontier` zone is a large `TOWN` region (`install.json` line ~2006:
`type:"TOWN"`, `mobsSpawn:false`, `hostileOnly:true`, color `#FFD700`), spanning roughly
**x 3787–4360, z 2748–3134** — far bigger than the built plaza. The seven facilities cluster
in the **south-west corner** (x 3782–3818, z 2959–2999, y 159), reached via the **Frontier
Causeway** route (Route 16, `install.json` line ~3635) which mouths in near x 3790, z 2965.

**All 24 battle-NPC coordinates below are builder-confirmed** (they ship in
`battle_frontier.json`). Hub/prop NPCs are **PROPOSED** (no bodies exist yet).

| NPC / prop | Role | Coord | Source |
|---|---|---|---|
| Knight Aldric | Castle challenger 1 | `3785,159,2962` | config ✔ |
| Guard Captain Elara | Castle challenger 2 | `3791,159,2962` | config ✔ |
| **Castle Lord Percival** | **Castle brain** (DOUBLES) | `3788,159,2968` | config ✔ |
| Climber Jasper | Tower challenger 1 | `3797,159,2959` | config ✔ |
| Contender Mira | Tower challenger 2 | `3803,159,2959` | config ✔ |
| **Tower Tycoon Palmer** | **Tower brain** (SINGLES) | `3800,159,2962` | config ✔ |
| Technician Rex | Factory challenger 1 | `3809,159,2962` | config ✔ |
| Engineer Lydia | Factory challenger 2 | `3815,159,2962` | config ✔ |
| **Factory Head Noland** | **Factory brain** (SINGLES) | `3812,159,2968` | config ✔ |
| Gambler Fritz | Arcade challenger 1 | `3782,159,2976` | config ✔ |
| Player Suki | Arcade challenger 2 | `3788,159,2976` | config ✔ |
| **Arcade Star Dahlia** | **Arcade brain** (DOUBLES) | `3785,159,2982` | config ✔ |
| Explorer Marco | Pyramid challenger 1 | `3812,159,2976` | config ✔ |
| Archaeologist Priya | Pyramid challenger 2 | `3818,159,2976` | config ✔ |
| **Pyramid King Brandon** | **Pyramid brain** (SINGLES) | `3815,159,2982` | config ✔ |
| Captain Stern | Port challenger 1 | `3785,159,2990` | config ✔ |
| Sailor Crest | Port challenger 2 | `3791,159,2990` | config ✔ |
| **Port Admiral Horatio** | **Port brain** (DOUBLES) | `3788,159,2996` | config ✔ |
| Merchant Vance | Market challenger 1 | `3797,159,2993` | config ✔ |
| Trader Fiona | Market challenger 2 | `3803,159,2993` | config ✔ |
| **Market Mogul Sterling** | see note → **fold into Hub** | `3800,159,2999` | config ✔ |
| Spelunker Dirk / Cave Diver Luna / **Cave Warden Selene** | Deep Dark grand-brain gauntlet | `3809/3815/3812, 159, 2990/2990/2996` | config ✔ — **belongs to `deep_dark_cave` area** |
| **Frontier Registrar** (reception) | hub greeter, rules, gate | `3800,159,2955` | **PROPOSED** (causeway mouth, N of plaza) |
| **Frontier Exchange** (FP counter) | spend Frontier Points | `3802,159,2957` | **PROPOSED** — reuse Sterling here |
| **Hall of Fame board** (record prop) | ghost-record recognition beat | `3798,159,2955` | **PROPOSED** (lore-keeper NPC or sign) |
| **Frontier Nurse** (paid heal) | between-battle heal service | `3806,159,2955` | **PROPOSED** |

**Scoping note.** The assignment names **6 facilities + hub = 7**. The shipped config also
carries **`the_market`** (Sterling) and **`deep_dark_cave`** (Selene). Recommendation:
- Fold **the Market** into the **Frontier Hub** — Sterling becomes the **FP Exchange keeper**
  (his optional battle survives as a hub-side high-roller `wager` match). This keeps the
  economy wing in one place instead of a redundant 7th battle door.
- Leave **`deep_dark_cave`** to the `deep_dark_cave` roadmap area; this section only *depends
  on* its grand-brain gate (`frontier_all_cleared`, see §4/§9).

---

## 4. Core loop (not a gym — a facility sandbox)

Each facility is a **3-node ladder**: 2 challengers (each gated on `royal_champion`) → 1
brain (gated on both its challengers). Winning a node sets `defeated_<id>`; the brain's
`achievementOnDefeat` fires `frontier_<facility>_cleared`. This wiring **already exists** in
`battle_frontier.json` — the work is authoring bodies, dialog, rctmod teams, and the
per-facility gimmick layer.

### 4.1 Per-facility gimmicks (engine-honesty flagged)

| Facility | Format | Gimmick | Engine-expressible? |
|---|---|---|---|
| **Tower** | SINGLES | **Streak singles.** Scoreboard `ff_tower_streak`, +1 per brain-tier clear, reset on loss; milestone FP at 5/10. | ✅ scoreboard via `battle.on_win`/`on_lose` (onwin winners-first) |
| **Port** | DOUBLES | **Streak doubles.** Identical, `ff_port_streak`, the Lugia doubles brain. | ✅ same, `GEN_9_DOUBLES` |
| **Castle** | DOUBLES | **Wager / resource.** Bet CD (win doubles, lose forfeits); paid heal + paid item-buy between rounds. | ✅ `battle.type:"wager"` + `loss_fee` are schema primitives; heal via `service.kind:"heal"` / `do:heal` gated on CD |
| **Arcade** | DOUBLES | **Random modifier.** `/random` roll picks the opponent's opening condition; 6 tag-gated battle buttons; optional paid reroll. | ✅ via a roll **function** + tag gates (see §8) |
| **Factory** | SINGLES | **Spec-swap draft.** Opponent team is **randomly drawn** each attempt; pick 1 of 3 "consoles" (type-cores). *True rental of the PLAYER's team is not expressible.* | ⚠️ opponent-side randomness ✅; **player-side rental → JAR-VERIFY** (see §4.2) |
| **Pyramid** | SINGLES | **No-safety-net gauntlet.** The only facility with **no heal service**; 2 challengers + brain must be cleared back-to-back (streak reset on loss). Regigigas brain. | ✅ (it is the *absence* of a service, not a lockout) — **item-lock is honor-system**, see §4.2 |
| **Hub** | — | FP exchange, Hall-of-Fame ghost record, reception/gating, paid heal. | ✅ scoreboard `ff_points` + band-tags + functions |

### 4.2 Gimmicks that need mechanics we cannot express — FLAG FOR JAR-VERIFICATION

1. **Battle Factory rental/draft (swap the player's party for a borrowed team).** TBCS
   `tbcs battle GEN_9_SINGLES @s vs <id>` fights with the player's **real** party; there is
   no verified team-substitution primitive in TBCS/rctapi (ENGINE_FINDINGS §2 documents only
   the battle-start + onwin surface). **Action:** decompile TBCS + rctapi for any
   set-temporary-team / draft hook before committing to true rentals. If none exists, ship
   the **Spec-Swap** reframe (opponent randomness) and offer the rental as a **streamer
   house-rule** (self-imposed 3-borrowed-mons), which needs zero code.
2. **Battle Pyramid item scarcity (disable the player's bag mid-battle).** No verified primitive
   disables held items / potions in a Cobblemon battle. **Action:** ship the **no-heal-service**
   mechanical version (real, engine-honest) and make "no held items / no bag" a **streamer
   house-rule**. Do **not** promise an enforced item-lock.
3. **True infinite streak.** A streak *counter* is trivial (scoreboard), but there is no way to
   procedurally generate unlimited opponents — TBCS fights fixed trainer ids. "Streak" is a
   **fixed rotation** of authored teams that the counter walks; the tension (permadeath) is
   real, the opponent pool is finite. Design accordingly (§6 rotation sizing).

### 4.3 Two-tier level split (the fairness fix)

The shipped config mixes challenger L90 and brain L90–100 while the player enters at cap 85.
Recommend splitting into:

| Tier | Gate | Level band | Uses |
|---|---|---|---|
| **First clear** | `defeated_royal_champion` | challengers **~87**, brains **~90** | the visible ladder; brutal-but-fair underleveled at cap 85 |
| **Record / rematch** | `board_cleared` (cap 100) | **100** | re-tuned home for the existing L100 brain teams; the streak/record tier; fair 100-v-100 |

This is the **balance rule applied to post-game** (ace = *effective* cap + 2). Retune in the
**rctmod team files** (§6), never the ladder.

---

## 5. Quests & side quests

The Frontier is battle-first; quests are thin, optional, and on-theme (mastery + one
recognition beat). All are `late`-tier, post-champion.

### 5.1 "Symbols of the Seven" (meta-collection)
- **Giver:** Frontier Registrar (hub). **Hook:** *Clear a brain, earn its Symbol. Seven Symbols
  and the Warden opens the dark.*
- **Steps:** defeat each facility brain → collect `frontier_<facility>_cleared` (already the
  brain achievements). At 7/7, `frontier_all_cleared` prerequisite for Cave Warden Selene is met.
- **Gates:** each step `defeated_frontier_brain_<facility>`; completion `all_tags:[...seven...]`.
- **Rewards:** cosmetic **Frontier Symbol** advancements (one per facility, hub-tracked), a big
  FP bonus at 7/7, and the Hub unlocks the **Deep Dark** waypoint (hand-off to `deep_dark_cave`).
- **Resolution:** Registrar: *Seven doors, seven falls. The last one is not lit. Bring a torch.*

### 5.2 "The Record" (the recognition beat — SHOWRUNNER-CRITICAL, canon-safe)
- **Giver:** Hall of Fame board (a lore-keeper NPC or interactive sign at the hub).
- **Hook:** the all-time streak record is held by a `§k`-obfuscated name. Early reads: *record:
  ████████, streak 100. Retired. Filed.* (the "filed" word ties to the Company scrub memos.)
- **Steps:** build a Tower **or** Port streak. As `ff_tower_streak` / `ff_port_streak` climbs,
  the board narrates: at streak 25 *you approach a record older than the Company;* at streak 50
  *you have matched the hand that signed the ledger you cannot read;* on tie/beat *the name will
  not resolve. It flickers like it is being kept from you.*
- **Gates:** `score:{objective:ff_tower_streak, op:gte, value:N}` (→ band-tag, see §8).
- **Rewards:** no item — this is **story**. It fires a `reveal/record_ghost` flavor beat that
  **circles** the reveal (never closes it; the name is only spoken at the mirror per LORE §5).
- **Resolution:** deliberately unresolved. The audience connects it; the character does not.

### 5.3 "House Edge" (Castle side-loop, repeatable, NO training pack)
- **Giver:** Castle Lord Percival / a croupier NPC. **Hook:** wager CD on doubles; the house
  pays double or keeps it all.
- **Steps:** `battle.type:"wager"`, escalating stakes (buttons for 500 / 2000 / 5000 CD).
- **Gates:** affordability via `store result` CD probe (ENGINE_FINDINGS §2 CobbleDollars — gate
  on `store result`, not `store success`). Loss charges `loss_fee`.
- **Rewards:** CD swing only. **Repeatable → no training-pack loot** (ENGINE_FINDINGS §3 rule:
  never on repeatables — no farm loops in hardcore Nuzlocke).
- **Resolution:** none; it is a sink/faucet, deliberately net-negative expected value.

### 5.4 "Old Ring" (optional recognition micro-beat)
- **Giver:** one veteran brain (Tower Tycoon Palmer suits it) carries a **late-recognition**
  entry: *I battled the founder once, when the Frontier was young. You move like him. You will
  not tell me your name and I will not make you.* Stands down to a respectful challenge.
- **Gate:** `recognition:"late"`. No mechanical reward — tone only. Civilians never get this.

---

## 6. Trainers & teams needed

**Configs already exist** (`battle_frontier.json`, all 24). **Missing:** the rctmod battle
teams (TBCS reads these), the series graph nodes, the characters/dialog, and the retune.

### 6.1 rctmod team files to CREATE — `data/rctmod/trainers/<id>.json`
One per config id. `name` **MUST equal the config `displayName`** (ENGINE_FINDINGS §4 wiring —
the kalahar swap miscredited wins). 24 files:

`castle_challenger_1`, `castle_challenger_2`, `frontier_brain_castle`,
`tower_challenger_1/2`, `frontier_brain_tower`,
`factory_challenger_1/2`, `frontier_brain_factory`,
`arcade_challenger_1/2`, `frontier_brain_arcade`,
`pyramid_challenger_1/2`, `frontier_brain_pyramid`,
`port_challenger_1/2`, `frontier_brain_port`,
`market_challenger_1/2`, `frontier_brain_market`,
`cave_challenger_1/2`, `frontier_brain_cave` *(cave three owned by `deep_dark_cave`)*.

Copy the shape of `data/rctmod/trainers/hua_zhan_leader.json` (flat `team[]`, bare species
ids, `bag`, `ai.maxSelectMargin`). Lift the teams already written in `battle_frontier.json`,
then **retune to the two-tier bands** (§4.3): first-clear challengers L~87, brains L~90; move
the L100 rosters to the record/rematch tier.

### 6.2 series graph nodes to CREATE — `data/rctmod/mobs/trainers/single/<id>.json`
Copy `single/hua_zhan_leader.json`. `series:["cobblemon-initiative"]`, `spawnWeightFactor:0`,
`requiredDefeats` = the config prereq chain:
- challengers → `[["royal_champion"]]`
- brains → `[["<facility>_challenger_1"],["<facility>_challenger_2"]]`
- `frontier_brain_cave` → all 7 facility brains + its 2 cave challengers.

**⚠️ CYCLE CHECK (ENGINE_FINDINGS §2, round 12b — StackOverflow crash at world start).** Every
`requiredDefeats` edit must re-run the singles+groups cycle check. Frontier nodes hang off
`royal_champion` (a leaf of the League DAG), so they extend the graph acyclically — **but only
if `royal_champion` is a real node in the series**; verify it is before wiring, or the frontier
nodes silently no-op (progress writes only for ids in the player's current series).

### 6.3 Brain team sketches (first-clear tier, ace = ~cap-85 + 2 ≈ **90** for brains; retune from config)
Legendary leads noted for the **legendaries_nobles** cross-check (§9) — do not let a Frontier
brain be a player's *only* path to a legendary.

| Brain | Facility | Format | ~6-mon sketch (first-clear L87–90) | Signature |
|---|---|---|---|---|
| Tower Tycoon Palmer | Tower | SINGLES | Dragonite / Milotic / Rhyperior (+Garchomp / Togekiss / Metagross for streak depth) | hyper-offense dragons |
| Port Admiral Horatio | Port | DOUBLES | **Pelipper + Kingdra** (rain) / Gyarados / **Lugia** | rain doubles |
| Castle Lord Percival | Castle | DOUBLES | **Dusknoir + Steelix** (Trick Room) / Empoleon / Slowbro | trick-room bulk |
| Arcade Star Dahlia | Arcade | DOUBLES | Blaziken / Togekiss (Follow Me) / Garchomp / **Zoroark** (Illusion) | chaos / redirection |
| Pyramid King Brandon | Pyramid | SINGLES | **Regirock / Registeel / Regice / Regigigas** | golem wall + Slow Start payoff |
| Factory Head Noland | Factory | SINGLES | Magnezone / Scizor / Metagross / Porygon-Z | steel/tech, randomized draw |
| *Market Mogul Sterling* | Hub (wager) | SINGLES | Zoroark / Chansey / Alakazam / Porygon-Z | trickster high-roller |
| *Cave Warden Selene* | Deep Dark | DOUBLES | Mismagius / Sableye / Gengar / Hydreigon / Tyranitar / **Darkrai** | capstone — owned by `deep_dark_cave` |

### 6.4 Record/rematch tier (L100, gated `board_cleared`)
Reuse the **existing L100 teams** verbatim from `battle_frontier.json` for each brain, exposed
as a second gated battle entry (`gate:{tag:"board_cleared"}`). No new team *design* — just
re-home the numbers the config already carries so they are fought at a fair cap.

### 6.5 Streak-rotation opponents (Tower/Port)
A "streak" needs more than one team to walk. Minimum viable: **rotate the facility's 3 nodes**
(challenger_1 → challenger_2 → brain → repeat) with the counter; ideal: author **+4 filler
challenger teams** each for Tower and Port (8 new configs+teams) so a streak of 7 has variety.
**Decision for showrunner (§9):** 3-node rotation (cheap) vs +8 filler teams (richer).

---

## 7. Economy & rewards

### 7.1 Frontier Points (FP) — proposed new soft-currency
A **new scoreboard objective `ff_points`** (the BP analog), built on the exact pattern already
used for `memory_fragment` / `cd_instability` / `dex_caught`. Earned in `battle.on_win`, spent
at the Hub Exchange. Affordability gates compile to **band-tags** (`ff_points_gte_<N>`) added to
`band_tags.mcfunction` (ENGINE_FINDINGS: only `PLAYER_TAG` is a proven Condition; the `score`
gate escape hatch must be backed by a maintained band-tag, same as `dex_gte_15`).

| Source | FP |
|---|---|
| Challenger win (first time) | +2 |
| Brain win (first clear) | +10 |
| Streak milestone 5 (Tower/Port) | +5 |
| Streak milestone 10 | +15 (+ a bottle cap) |
| 7/7 Symbols | +25 |

### 7.2 FP Exchange spends (CD/loot **sinks**, at the Hub)
Bottle Cap (gold/silver), Ability Patch, Mints, PP Max, an **Ability Capsule**, high-cost
**Master Ball**, cosmetic **Frontier Symbol** advancements, and an **FP → CD** teller (drains
FP for CobbleDollars, keeping FP a closed loop that never inflates the CD supply).

### 7.3 CD sinks (keep the wallet meaningful post-game)
- **Castle wager** (`§5.3`) — variable, net-negative EV.
- **Paid heal** at every facility **except the Pyramid** — flat CD per heal (`service.kind:"heal"`
  gated on a CD `store result` probe; broke players are declined, per the nurse pattern).
- **Arcade reroll** — pay CD/FP to re-roll the modifier.

### 7.4 Existing item rewards (keep)
Challengers already grant `3× rare_candy`; brains grant `ability_capsule + 3× diamond`
(Port/Cave grant a `master_ball`). Layer FP + the cosmetic Symbol on top; **do not** add
training-pack loot to any repeatable (wager, streak grind) per ENGINE_FINDINGS §3.

### 7.5 Shop-tier / liberation ties
None. The `cd_instability`/`fields_liberated` shop swing is an **Act-1/2 mechanic**; by the
Frontier the index is frozen at 25 and all fields are long liberated. The Frontier runs its
**own** FP economy, deliberately decoupled from the wheat war so the post-game does not
re-open a closed plot. (Do **not** wire `shop <tier>` rewards here.)

---

## 8. Implementation notes / FUTURE-ME HOOKS

**Copy these patterns:** the League ladder (`royal_cynthia` dialog + `royal_champion` character
`battle` block for gate/prize/win-lose lines) and the gym ladder
(`trainers/gyms/hua_zhan_city.json` prereq chain; `hua_zhan_leader` rctmod team + graph node).

### 8.1 Files to CREATE (exact paths)
Authoring source (compiles to Easy NPC presets):
- `dialog-src/characters/frontier/<id>.json` — one per NPC (22 facility NPCs + hub). Each battle
  NPC carries a `battle` block: `trainer:"<id>"`, `type` (`"basic"` challengers / `"elite"` brains /
  `"wager"` Castle), `format`, `prize`, `defeat_tag:"defeated_<id>"`, `win_line`/`lose_line`/
  `already_beaten_line`, and `on_win`/`on_lose` arrays for the streak/FP scoreboard writes.
- `dialog-src/dialog/<id>.json` — dialog trees (recognition entries gated `recognition:"late"`;
  the `board_cleared`-gated record-tier rematch button).
- Hub: `dialog-src/characters/frontier/frontier_registrar.json`, `frontier_exchange.json`
  (reuse Sterling), `frontier_record_board.json` (lore-keeper), `frontier_nurse.json` +
  their dialogs.

Battle teams / graph (see §6): 24× `data/rctmod/trainers/<id>.json` + 24×
`data/rctmod/mobs/trainers/single/<id>.json`.

Datapack functions (new):
- `function/frontier/arcade/roll.mcfunction` — `execute store result score <player> ff_arcade_roll
  run random value 1..6` then set one of six `arcade_mod_<n>` tags. **Call it via a bare
  `function` (allowlisted), never an as_player `execute`-rooted command** (ENGINE_FINDINGS §2:
  execute-rooted ExecAsUser is silently blocked). Six battle buttons in the Arcade dialog gate on
  `tag:arcade_mod_<n>`; each points at a modifier-themed opponent lead.
- `function/frontier/streak/tick.mcfunction` (or milestone checks) — read `ff_tower_streak` /
  `ff_port_streak`, award FP at 5/10, fire the record-board flavor. Streak writes happen in
  `battle.on_win` (`scoreboard players add @1 ff_tower_streak 1`) and `on_lose`
  (`scoreboard players set @2 ff_tower_streak 0`) — **@1 = player in the WIN list, @2 = player in
  the LOSE list** (winners-first, ENGINE_FINDINGS §2).
- `function/frontier/fp/award.mcfunction` + `spend_*.mcfunction` — FP grants/sinks; route CD
  through the existing `economy/payout {amount:N}` (never raw `pay_macro`).
- `function/frontier/load.mcfunction` — zero-init `ff_points`, `ff_tower_streak`, `ff_port_streak`,
  `ff_arcade_roll` objectives (ENGINE_FINDINGS §2: unset scores fail every `matches` test).
- Extend `function/dialog/band_tags.mcfunction` to maintain `ff_points_gte_<N>` and
  `ff_tower_streak_gte_<N>` for every threshold the dialogs gate on.

Registry: extend `data/cobblemon_initiative/trainers/battle_frontier/battle_frontier.json` only
if adding streak-filler ids (§6.5) or splitting the record tier into separate config entries.

### 8.2 Pipeline (run in this order — ENGINE_FINDINGS §3)
1. Author `dialog-src/**`.
2. `scripts/content_compile` (lowers to `data/easy_npc/preset/…`, maintains band_tags +
   register_sight + npc_presets.json).
3. `scripts/generate_granary_tiers` → `scripts/update_preset_index` →
   `scripts/generate_npc_function` (writes `npc/preset_map.json` + `update_npc_presets.mcfunction`).
4. Add the rctmod team + graph files (step outside content_compile).
5. Re-run the **series cycle check** over singles+groups.
6. `gradle build`; bump the alpha suffix (version-per-round rule).

### 8.3 Bodies / placement
The 24 battle NPCs have **builder-confirmed coords** but **no uuid/placement yet**. Two options
per ENGINE_FINDINGS §3 wiring: (a) builder places bodies in-world → add `uuid`; (b) add
`placement:{x,y,z}` (the exact config coords) and let the generated once-per-world latch
`import_new`-spawn them. Hub NPCs use `placement` (PROPOSED coords, §3). **Gotcha:** latch-spawned
NPCs get random uuids, so any NpcSight registration needs a manual `npcsight add` pass after first
spawn (ENGINE_FINDINGS §3 KNOWN GAP).

### 8.4 Gotchas checklist
- **onwin winners-first** — streak/FP `@1`/`@2` differ between win and lose lists.
- **Macro-safe text** — record-board / FP / economy lines: **no `"` and no `'`**.
- **L100 vs cap 85** — gate the record tier on `board_cleared` or it is an impossible wall.
- **rctmod `name` == config `displayName`** — or wins miscredit.
- **`store result`, not `store success`** for CD affordability (wager, paid heal).
- **`score` gate needs a band-tag** — FP/streak thresholds ride `band_tags.mcfunction`.
- **CLOSE_DIALOG runs last** — if a button opens a shop/exchange GUI, drop the close action
  (ENGINE_FINDINGS §2) or the exchange screen is destroyed same tick.
- **Rental/item-lock are NOT engine primitives** — ship the reframes (§4.2), flag any real
  mechanic for jar-verification before promising it.

---

## 9. Dependencies & open questions

### 9.1 Depends on (other area keys)
| Area key | Why |
|---|---|
| `royal_league` | Entry gate `royal_league_champion` + cap 85; the `royal_champion` series node must exist for frontier graph nodes to take progress. Copy its ladder/dialog pattern. |
| `board_and_founder` | Provides `board_cleared` (cap 100) — the gate for the record/rematch tier; the Frontier is the 85→100 grind that *fills the wait* before/around the Board. |
| `deep_dark_cave` | Owns the **grand brain** (Cave Warden Selene / Darkrai) whose gate is `frontier_all_cleared` (all 7 facility brains). The 7/7 Symbols quest hands off here. |
| `mainline_spine` | Scoreboard load hooks (`frontier/load`), band-tag generation, `economy/payout`, and the `cd_instability=25` context the Frontier inherits. |
| `company_hq` | Sets `cd_instability → 25` on DJ's defeat — the stabilised economy state the Frontier's voice register assumes. |
| `gym_system_pvp_doubles` | The interior-ladder + `GEN_9_DOUBLES` block pattern to copy for the doubles brains/facilities. |
| `legendaries_nobles` | Cross-check: Frontier brains lead legendaries (Lugia, Darkrai, the Regis, Landorus, Heatran). Ensure a brain is not a player's *only* obtain path, and that using them here does not contradict the noble/legendary encounter design. |

### 9.2 Decisions the showrunner must make
1. **Frontier Points, yes/no?** FP is a small but real new system (scoreboard + band-tags +
   exchange functions). Alternative: skip FP, reward purely in CD + existing items. FP is the
   richer, more "Frontier" answer; CD-only is cheaper. **(Recommend FP.)**
2. **Two-tier level split (§4.3)?** First-clear ~87–90 at cap 85, record tier L100 at
   `board_cleared`. Alternative: single L90–100 clear at champion (harsher, no rematch layer).
3. **Fold the Market into the Hub?** Recommend Sterling → FP Exchange keeper (assignment lists
   6 facilities + hub, not 7). Or keep Market as a distinct 7th battle door.
4. **Rental (Factory) & item-lock (Pyramid):** accept the engine-honest reframes + streamer
   house-rules (zero code), **or** budget jar-verification for a real TBCS team-substitution /
   bag-lock hook? **(Recommend reframe; verify only if a real mechanic surfaces.)**
5. **Streak depth:** 3-node rotation (cheap) vs +8 filler challenger teams (richer streak
   variety, §6.5)?
6. **Legendary brains:** keep the config's legendary leads (Lugia/Darkrai/Regis/etc.) as
   post-game flex, or swap to pseudo-legendaries to reserve legendaries for `legendaries_nobles`?
7. **The Record ghost beat (§5.2):** approve the Hall-of-Fame `§k`-record recognition beat? It is
   canon-safe (circles, never closes the name) and is the Frontier's one story hook — but it *does*
   nod at the mirror one act early; confirm that is desired pacing.
8. **When does the Frontier open?** Immediately on `royal_league_champion`, or hold it until some
   Board progress so the L100 record tier is reachable sooner? (Affects the fairness window.)
