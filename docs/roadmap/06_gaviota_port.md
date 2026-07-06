# 06 — Gaviota Port (Water, cap →50) · The Gaviota Open · Kyogre

> Area key: `gaviota_port` · Gym 5 · Act 1 (infiltration) · `cd_instability` → 40 · `frag_5`
> Deliverables: (a) **The Gaviota Open** — mid-game marquee fishing event; (b) **Kyogre** — offshore noble legendary; (c) the retuned **PvP water gym** (double + Leader Neptune).
>
> Grounding note: the gym CONFIG already exists and is wired (`trainers/gyms/gaviota_port.json` fires `frag_5` + `shop badge_5` + `gym_destabilize`). This section RETUNES that ladder to the brutal rule and BUILDS the two new pillars (Open + Kyogre) as NPC-placement-only content. No terrain.

---

## 1. Concept & fantasy

**One-line pitch:** *Earn the Current Badge, then the port throws its Company-sponsored fishing spectacle — and out past the last pier, the tide gives up something the Company can never put on a ledger.*

Gaviota is the **water hub** and the run's first real "resort town" beat after four grind gyms. It sells three distinct fun-shapes stacked in one port:

- **The wear-you-down gym.** Leader Neptune runs **rain** (Drizzle Pelipper → Swift-Swim sweepers → a Milotic that will *not die*). His thesis line already ships: *"Water-types do not crash into a problem. They wear it down, wave after wave."* Brutal-Nuzlocke rain stall is a genuinely scary wall for a capped-44 party.
- **The Gaviota Open — the marquee stream moment.** A 3-round CD-sink fishing tournament, sponsored by **The Company** with maximum corporate-dread satire ("Every catch verified. Every value verified."). The grand purse is skimmed on the way out — the audience *watches the payout adjust* on screen, the plot you feel in your wallet. A 5,000 CD chase with a par-time final sprint that is pure clip material.
- **The violent sea (Kyogre).** After the badge, a warning buoy appears at the north pier's end — **"WARNING: the sea seems violent in these parts"** (canon monument line, showrunner ruling 2026-07-06). Beat the storm-watch guardians holding the pier (the PvP phase), and the deep answers: a wild **Kyogre** surfaces offshore at cap level as a **normal wild Pokémon** — full hardcore + Nuzlocke stakes, one legendary, one Poké Ball's worth of nerve, and the player *chooses* whether to risk engaging at all. The port reveres it as the one thing the Company can't audit; claiming it is a power move, leaving it keeps the port's guardian. Massive live-audience tension either way.

**Marquee moments to cut for the channel:** the Milotic Recover-stall wall breaking (or breaking *you*); the Open purse visibly haircut at the weigh-in ("provisional pending audit"); the record-sprint final round with the clock on the bossbar; the Kyogre surfacing cinematic; the throw of the ball.

---

## 2. Narrative role

| Field | Value | Source / wiring |
|---|---|---|
| Act | **1 — Infiltration** (gyms 1–7) | `gaviota_leader.json` `act:"1"`; LORE_BIBLE §4 |
| `cd_instability` after leader | **→ 40** | leader reward runs `economy/gym_destabilize` (+8; 5×8=40); confirmed `trainers/gyms/gaviota_port.json` L256 |
| Memory fragment | **`frag_5`** — "Two signatures on every star-note. … You fear it was also yours." | `function/memory/gym/frag_5.mcfunction` (already authored); leader reward L254 |
| Recognition tier | **mid → alarm/awe** | `gaviota_leader.json` `recognition_tier:"mid"`; Neptune's after-line already teases *"a face come back from the deep … the tide drags up when it is good and ready"* |
| Level cap unlocked | **50** (Current Badge / `badge_water`) | CLAUDE.md ladder; `achievementOnDefeat:"badge_water"` |

**Canon ties this area carries:**
- **The recognition motif is *the deep tide*.** Neptune already frames it (*"I know a thing the tide drags up"*). The Kyogre content literalises it: the sea surfaces two things it was holding — the noble, and the founder's face. The Company Open Liaison (management-tier) plays the **alarm** register; civilians (Weighmaster, Tide-Caller) never place the CEO — they only read *"the tide brought you back too."*
- **The skimmed purse is `cd_instability` made diegetic.** At idx 40 the `economy/payout` haircut is ~10%; the Open's grand prize is routed through it so the audience literally watches the value drift. Neptune's `already_beaten_line` pre-seeds this: *"the dock pays come up light. Watch your coin near the water."*
- **Kyogre = the un-auditable asset.** The Company's whole plot is monopolising what it can *verify* and *back* (nether stars → wheat). The deep noble is beyond the ledger — the thematic counter-image to the wheat monopoly. Play it as corporate-dread comedy against genuine menace: the Liaison would love to "verify" the legend and cannot.

---

## 3. Layout & placements

**Zone (builder-confirmed, `install.json`):**
- **Gaviota Port** — `TOWN`, `mobsSpawn:false` (safe zone), `centerY:64`, color `#2495e5`. Bounding hull ≈ **x 386–759, z 3393–3730** (`install.json` L2967–3067).
- **Gullwing Coast** — `ROUTE 7`, `mobsSpawn:true` (NOT safe), `centerY:64`. The open sea/coast **north of the port**, hull ≈ **x 614–829, z 3264–3429** (`install.json` L4909+). This is the offshore water for Kyogre.
- Gym-ladder bodies are anchored in config at **≈[609–624, 82, 3533–3542]** (`trainers/gyms/gaviota_port.json`) — note **y82**, ~18 above sea, i.e. a raised boardwalk/lighthouse deck. The Leader stands at **[624, 82, 3536]**.

| # | NPC / prop | Role | Coord | Status |
|---|---|---|---|---|
| — | Leader Neptune | gym leader | [624, 82, 3536] | **builder-confirmed** (config) |
| — | Gym Guide | gym guide | (in gym deck cluster) | **builder-confirmed** (`gaviota_guide.json`) |
| — | 4 sailors / jr / apprentice | gym ladder | [609–618, 82, 3533–3542] | **builder-confirmed** (config) |
| N1 | **Weighmaster Odalys** | Gaviota Open host (signup, rounds, turn-in) | **[660, 63, 3600]** waterfront | **PROPOSED (needs builder confirm)** |
| N2 | **Company Open Liaison** | sponsor / weigh-in "verifier" / paymaster (skims purse) | **[664, 63, 3596]** beside the podium, under the banner | **PROPOSED (needs builder confirm)** |
| N3 | **Titleholder Marlin** | reigning champ; sets the round-3 record; colour + optional PvP | **[652, 63, 3606]** on the pier | **PROPOSED (needs builder confirm)** |
| N4 | **Tide-Caller Mira** | port elder, Kyogre **flavour/context** NPC (the trigger is the buoy, not her) | **[628, 63, 3438]** north pier, near the buoy | **PROPOSED (needs builder confirm)** |
| N5 | **Storm-Watch Guardians ×2** | Kyogre **PvP phase** — pier guardian ladder | **[632, 63, 3432]** and **[636, 63, 3428]** along the pier's end | **PROPOSED (needs builder confirm)** |
| P1 | **Open sponsor banner** (prop/doc NPC) | satire text ("Every catch verified…") | at podium | **PROPOSED** |
| P2 | **Provisional leaderboard** (prop/doc NPC) | standings "pending audit" gag | at podium | **PROPOSED** |
| P3 | **Warning buoy** (monument prop, dialog-only) | Kyogre **trigger** — "WARNING: the sea seems violent in these parts" | **[630, 63, 3436]** end of the north pier, facing the open sea | **PROPOSED (needs builder confirm)** |
| X1 | **Kyogre** spawn point | wild noble spawn (normal wild Pokémon) | **[700, 62, 3330]** open water in Gullwing Coast (`mobsSpawn:true` — full stakes) | **PROPOSED (needs builder confirm)** |

> **Builder decisions:** confirm the **waterfront y-level** (y64 sea-level docks vs y82 boardwalk to match the gym deck) and the **offshore spawn point / surface y** for Kyogre. All N-, P-, X- coords are placeholders inside real zones — snap them to actual dock geometry.

---

## 4. Gym — PvP water ladder (retune)

The ladder, gates, and the DOUBLE **already exist** in `trainers/gyms/gaviota_port.json`. The work here is a **brutal-rule RETUNE of levels only** (memory: *retune teams, never the ladder*). Entry cap at Gaviota = **44** (unlocked by beating Deepcore/gym 4), so **ace = entry-cap + 2 = 46**.

### Ladder & gate wiring (unchanged structure)

| Order | id | Trainer | Format | `prerequisites` (defeated-ladder) | Current lv | **Retune → (rule)** |
|---|---|---|---|---|---|---|
| 1 | `gaviota_trainer_1` | Sailor Marco | SINGLES | `[deepcore_leader]` | 38–39 | 41 |
| 2 | `gaviota_trainer_2` | Swimmer Coral | SINGLES | `[deepcore_leader]` | 38 | 41 |
| 3 | `gaviota_trainer_3` | Fisherman Ivan | SINGLES | `[deepcore_leader]` | 39 | 42 |
| 4 | `gaviota_trainer_4` | Surfer Paz | SINGLES | `[deepcore_leader]` | 39 | 42 |
| 5 | `gaviota_jr_apprentice` | Jr. Apprentice Tide | SINGLES | `[deepcore_leader, gaviota_trainer_1, gaviota_trainer_2]` | 43 | 43 |
| 6 | **`gaviota_apprentice`** | Apprentice Marina | **GEN_9_DOUBLES** | `[gaviota_jr_apprentice]` | 45–46 (4 mons) | 44–45 |
| 7 | `gaviota_leader` | **Leader Neptune** | SINGLES | `[gaviota_apprentice]` | 47–49 (4 mons) | **ace 46 (6 mons, below)** |

- **The DOUBLE is battle #6, Apprentice Marina** (already `GEN_9_DOUBLES`, 4 mons: Starmie / Vaporeon / Seadra / Pelipper). Keep her as the double; retune her band to lv 44–45 (co-equal to the cap so she reads as a "warm-up doubles" before Neptune). This mirrors Hua Zhan (`hua_zhan_apprentice` doubles) exactly — copy that shape.
- Dialog gating already flows through `defeated_<trainerId>` tags; the Leader after-defeat dialog is gated `defeated:gaviota_leader` (`dialog/gym_leader_gaviota.json` L11). No gate rewiring needed.

### Leader Neptune — retuned team (rain core, ace 46)

Flavour = attrition + rain. Six mons, ace **Kingdra 46** (= entry-cap 44 + 2). Retune in `data/rctmod/trainers/gaviota_leader.json` (RCT team) **and** the mirror block in `trainers/gyms/gaviota_port.json` (gym-config team drives the tbcs battle).

| # | Species | Lv | Ability | Role / kit |
|---|---|---|---|---|
| 1 | Pelipper | 42 | Drizzle | rain setter — Hurricane / Surf / Roost / Tailwind |
| 2 | Cloyster | 43 | Skill Link | Shell Smash / Icicle Spear / Rock Blast / Hydro Pump |
| 3 | Gyarados | 44 | Intimidate | Dragon Dance / Waterfall / Crunch / Earthquake |
| 4 | Ludicolo | 44 | Swift Swim | rain abuser + **grass STAB** (patches the 4× nothing but eases the Grass-lead cheese) — Surf / Giga Drain / Ice Beam / Rain Dance |
| 5 | Milotic | 45 | Marvel Scale | **the thesis wall** — Scald / Ice Beam / Recover / Toxic |
| 6 | **Kingdra (ACE)** | **46** | Swift Swim | Hydro Pump / Draco Meteor / Ice Beam / Rain Dance |

> Keep held items in the config team only (RCT team format tolerates `heldItem`; the tbcs battle reads the gym-config team). Existing config already gives Neptune a `full_restore ×3` bag and `maxSelectMargin 0.1` (near-perfect AI) — keep both; that's the brutal knob.

---

## 5. Quests & side quests

### 5A. THE GAVIOTA OPEN (marquee — NEW)

**Giver:** Weighmaster Odalys (N1) at the podium. **Sponsor/paymaster:** Company Open Liaison (N2). **Colour:** Titleholder Marlin (N3).
**Hook:** *"The Gaviota Open. Company-sponsored, Company-verified, and the purse is real money — well, it's real until they weigh it."* Odalys runs the rounds; the Liaison stamps every catch "verified" and hands the (skimmed) envelope.
**Gate:** signup entry gated on **`defeated_gaviota_leader`** (badge = the port license → cap 50 → the lv 46–50 window makes sense). Pre-badge, Odalys says come back with the Current Badge.
**CD entry sink:** **1,000 CD** per attempt (`cobbledollars remove @s 1000` — same UNVERIFIED empty-wallet caveat as the Sango Classic; doc fallback = free entry, smaller purse).

**Rounds** (single continuous run, one bossbar timer per round, vanilla-fish-item counting exactly like `derby/`):

| Round | Name | Objective | Timer | Advance |
|---|---|---|---|---|
| 1 | **The Cast Call** | land **8** fish (cod/salmon) | 6:00 | ≥8 → round 2 |
| 2 | **The Weigh-In** | land **2 "trophy fish"** (`minecraft:pufferfish` or `tropical_fish` — rarer pulls) **+ 6** regular | 6:00 | trophy≥2 & total≥8 → round 3 |
| 3 | **The Open Final** | **10** fish before the clock — beating **Marlin's record time** | 5:00 | ≥10 → win |

**Rewards:**
- **First win:** **5,000 CD** routed through `economy/payout {amount:5000}` (skew-aware → visibly ~4,500 at idx 40 — the on-screen skim) + a **marquee bundle**: `cobblemon:water_stone` ×1, premium fishing bait ×5 (verify Cobblemon bait item id from jar), `cobblemon:poke_ball` ×3, and the latch tag **`gaviota_open_champion`**.
- **Repeat win:** **800 CD** flat (net −200 vs entry — ritual, not exploit; mirrors the Classic's 200-flat repeat).
- **Story:** `gaviota_open_champion` seeds the Tide-Caller's stronger line (she only offers the deep to someone who's "proven they can read water"), and is a clean prereq if the showrunner later wants an angler's-discount beat.
**Resolution:** Odalys crowns you; the Liaison hands the envelope and over-explains the "verification adjustment" (Act-2 nervous-reassurance register is arriving — idx 40). Marlin concedes the title and drops the first rumour of *"the big one that never gets weighed"* — the Kyogre bridge.

### 5B. THE VIOLENT SEA — Kyogre (NEW · noble, MONUMENT trigger)

Per the showrunner ruling (2026-07-06), Kyogre is a **NOBLE**: monument warning → PvP
phase → the legendary spawns as a **normal wild Pokémon** (archetype spec canonical in
`16_legendaries_nobles.md` §4.1; this section owns the local staging).

**Trigger (monument):** the **warning buoy** (P3) at the north pier's end. Its dialog
opens on the canon line — *"WARNING: the sea seems violent in these parts"* — and an
arm button sets `noble_kyogre_armed`. Tide-Caller Mira (N4) is flavour/context beside
it: *"The tide has been giving things back lately. Faces. Old debts. The port does not
fish what is down there — we thank it."* (macro-safe, apostrophe-free).
**Gate (tags):** `defeated_gaviota_leader` **AND** `gaviota_open_champion` (recommended
seasoning gate) **AND NOT** armed/called latches — negations ride `EQUALS no_<x>` band
tags.
**PvP phase:** the two **Storm-Watch Guardians** (N5) — a short pier ladder
(`GEN_9_SINGLES`, band 48–50, second gated on the first's `defeated_*` tag), framed as
the port testing whether you can stand in the weather it is warning you about.
**Wild spawn:** when the second guardian falls, a **one-shot latched** function spawns
wild **Kyogre lv 50** at X1 and sets `noble_kyogre_called`. The encounter happens in
**Gullwing Coast (Route 7, `mobsSpawn:true`, not a safe zone)** — standard wild rules,
full Nuzlocke stakes; a fainted mon here is permadeath. Catching it, KO'ing it, or
walking away is entirely the player's risk call — that choice is the content.
**Reward:** the encounter *is* the reward — no CD, no gift fallback. On capture, fire a
one-time beat from Mira (*"the tide gave you what it was holding"*) tying the
deep-surfacing motif to the memory arc.
**Resolution:** claim it (personal power move) or leave the guardian (the port keeps its
noble; Mira respects it). Re-call/cleanup rules (despawn, KO'd-noble policy) follow
`16_legendaries_nobles.md` §4.1 — the buoy carries the call-again button.

---

## 6. Trainers & teams needed

**Gym (RETUNE existing files — no new gym trainers):**
- `data/rctmod/trainers/gaviota_leader.json` — expand to the 6-mon rain core above, ace Kingdra 46.
- `data/cobblemon_initiative/trainers/gyms/gaviota_port.json` — mirror the leader team (this is the team the **tbcs battle** actually reads) + drop trainer/jr/apprentice bands to the retune column in §4. Keep `prerequisites`, `battleFormat`, rewards, coords, bag untouched.
- `data/rctmod/trainers/gaviota_trainer_1.json`, `gaviota_trainer_2.json` (+ 3/4 if present) and `gaviota_apprentice.json` — nudge levels to match. Re-run the **rctmod series cycle check** after any `requiredDefeats`/prereq touch (ENGINE_FINDINGS §2: a graph cycle StackOverflow-crashes world start) — but a level-only retune touches no edges, so this stays safe.

**The Gaviota Open:** **no PvP trainers required** — it's a fishing derby, not battles. (Optional: if the showrunner wants round 3 to be a head-to-head vs Marlin instead of a record-sprint, add `sq_gaviota_open_final` to `trainers/side_quests/act1.json` as a `GEN_9_SINGLES` rematch trainer, lv 48–50, prereq `defeated_gaviota_leader` — model on `sq_bracket_3` / Tayo.)

**Kyogre:** the legendary itself is **not a trainer** — a wild `spawnpokemonat` after the PvP phase. The PvP phase needs **two new guardian teams**: `data/rctmod/trainers/noble_kyogre_stormwatch_1.json` + `_2.json` (`GEN_9_SINGLES`, band 48–50, water flavour) + TrainerConfig entries + graph nodes per the standard wiring recipe (`name`==`displayName`; cycle-check after graph edits). File naming and the archetype contract are owned by `legendaries_nobles` (`16_legendaries_nobles.md` §6).

---

## 7. Economy & rewards

| Lever | Value | Mechanism |
|---|---|---|
| Gym prize | 3,200 CD (flat, tbcs onwin) | `gaviota_leader.json` `prize:3200` — battle prizes stay flat-literal (ENGINE_FINDINGS) |
| Shop tier | `badge_5` catalog | leader reward `cobblemon-initiative shop badge_5` (already wired) |
| `cd_instability` | +8 → **40** | leader reward `economy/gym_destabilize` (already wired) |
| **Open entry (sink)** | **−1,000 CD** / attempt | `cobbledollars remove @s 1000` in `open/begin` |
| **Open grand prize** | **5,000 CD (skew)** ≈ 4,500 net | `economy/payout {amount:5000}` — the visible haircut is the point |
| Open repeat | 800 CD flat | `open/win_repeat` (net −200 vs entry) |
| Open marquee item | Water Stone + bait ×5 + Poké Ball ×3 | `open/win_first` via `give`/`loot` |
| Kyogre | a cap-50 legendary | capture = reward (no CD) |

- **Liberation tie:** Gaviota is not a wheat-field town, so **no direct `free_field` hook** — but the skimmed purse is the port's *evidence* of the wheat-driven instability, and Neptune's coin-warning line is the in-fiction tell. If `wheat_war_farms` puts a liberatable parcel on the Gaviota approach later, the Liaison is the natural recognition-ambush body.
- **Shop caveat (ENGINE_FINDINGS / LORE §9):** don't promise a *live* angler's discount — shop swings are stepped per-badge tiers only. `gaviota_open_champion` gates flavour/bundles, not a continuous price change.

---

## 8. Implementation notes / FUTURE-ME HOOKS

**Content pipeline (every new NPC):** author `dialog-src/characters/gaviota/<id>.json` + `dialog-src/dialog/<id>.json` → `scripts/content_compile` (lowers to `src/main/resources/data/easy_npc/preset/humanoid(_slim)/<id>.npc.snbt`) → `scripts/generate_granary_tiers` → `scripts/update_preset_index` → `scripts/generate_npc_function` (writes `npc/preset_map.json` + `function/update_npc_presets.mcfunction`). Placement-latched = give `placement:{x,y,z}` and **no `uuid`** (spawns once per world, bypasses the builder world) — copy `takehara/agent_yield_lead.json` and `sango/sango_company_liaison.json`.

> **New folder:** create `dialog-src/characters/gaviota/` for the Open + Kyogre cast (matches the `takehara/` / `hua_zhan/` convention). The two existing gym NPCs live under `dialog-src/characters/gym/` (`gaviota_leader.json`, `gaviota_guide.json`) — leave them or move them for tidiness (showrunner call, §9).

**Files to CREATE:**

*Characters* (`dialog-src/characters/gaviota/`): `open_host_odalys.json`, `open_liaison.json`, `open_titleholder_marlin.json`, `tide_caller_mira.json`, plus prop/doc NPCs `open_banner.json`, `open_leaderboard.json` (model the props on `sango/notice_post_1.json` / `takehara/rezoning_notice_board.json`).

*Dialogs* (`dialog-src/dialog/`): `gaviota_open_host.json` (multi-entry tree: signup / round-active / champion / turn-in — **copy `dialog/sq_deka.json`**, which shows the `as_player` command-button pattern and priority-gated entries), `gaviota_open_liaison.json`, `gaviota_titleholder.json`, `gaviota_tide_caller.json`.

*Function tree* — **new marquee derby**, mirror `function/sidequest/derby/` into `function/sidequest/open/`:
- `load.mcfunction` — objectives `ci_open`, `ci_open_time`, `ci_open_round`, `ci_open_fish`, `ci_open_trophy`, `ci_open_total`, `ci_open_win`; bossbar `cobblemon_initiative:gaviota_open`. **Must be added to the load chain by the orchestrator** (do not edit function tags from a quest agent — see `derby/load.mcfunction` note).
- `start.mcfunction` (double-entry guard → `begin`), `begin.mcfunction` (charge 1000, set round 1, arm timer), `second.mcfunction` (countdown loop + per-round targets), `turnin.mcfunction` (count via store-result `clear …/0`, branch by `ci_open_round`), `advance_round.mcfunction`, `win_first.mcfunction`, `win_repeat.mcfunction`, `win_common.mcfunction`, `fail.mcfunction`, `take_fish.mcfunction`.
- **Copy the counting idiom verbatim** from `derby/turnin.mcfunction` (store-result-clear-0 counts without removing; only clear on success) and the resume-on-relog schedule from `derby/load.mcfunction`.

*Kyogre* (`function/legendary/noble/kyogre/`, per the `legendaries_nobles` layout): `arm.mcfunction` (buoy button → `noble_kyogre_armed` + bossbar/weather dressing), `spawn.mcfunction` — `spawnpokemonat <X1> kyogre level=50` guarded `unless` `noble_kyogre_called`, fired by the second storm-watch guardian's onwin (winners-first: grant on key 1 / `@1`) — and `cleanup.mcfunction` (re-call latch logic per `16_legendaries_nobles.md` §4.1). Characters: `dialog-src/characters/gaviota/monument_kyogre_buoy.json` (dialog-only monument prop, notice-board pattern) + `noble_kyogre_stormwatch_1/2.json` (battle blocks). **Grammar is UNVERIFIED (jar-verify)** — `spawnpokemonat`/`spawnpokemon`/`givepokemonother` are all flagged UNVERIFIED in the repo (see `sq_deka` Magikarp gag) and are not in ENGINE_FINDINGS. Per memory *verify-mod-grammar-from-jars*: **decompile the pinned Cobblemon 1.7.3 jar and confirm the spawn command before building this** — do not ship a guessed command.

**Gates → band-tags:** the Open/Kyogre gates use plain `defeated_*` and story tags (all PLAYER_TAG-friendly — the only proven Condition type). If any numeric gate creeps in (e.g. a dex threshold on the Tide-Caller), it must go through `function/dialog/band_tags.mcfunction` as a derived band-tag, and every "does-not-have" gate must ride `EQUALS no_<X>` (Easy NPC ignores NOT_EQUALS — ENGINE_FINDINGS §2, band_tags header).

**Macro-text rule (hard):** any text delivered through a macro (fragments, `economy/*`, tbcs `onwin`, the Open payout tellraws) must contain **no double-quotes and avoid apostrophes** — write the Open/Liaison purse lines accordingly.

**Gotchas:**
- `cobbledollars remove` has **no balance pre-check** (UNVERIFIED on an empty wallet) — same fallback as the Classic (free entry / smaller purse). Design the 1000 sink to degrade gracefully.
- `economy/payout` uses CobbleDollars `give <targets> <amount>` (targets FIRST, no `add` subcommand) — reuse it, don't hand-roll.
- The **tbcs battle reads the GYM-CONFIG team**, not the RCT team — retune **both** or the fight won't match the sheet.
- Kyogre encounter is in a **non-safe route by design** (showrunner ruling 2026-07-06: a noble spawns as a normal wild Pokémon, standard wild rules, full Nuzlocke stakes — the player chooses whether to risk it). Do NOT add exemptions; do verify the X1 spawn coord actually sits inside the Gullwing Coast `mobsSpawn:true` hull, not the safe town hull.

---

## 9. Dependencies & open questions

**Depends on (other area keys):**
- **`mainline_spine`** — owns `cd_instability`, `frag_*`, recognition tiers, the quest HUD, and the `economy/`+`memory/` functions this area calls. Gaviota consumes them; it doesn't define them.
- **`gym_system_pvp_doubles`** — the shared ladder/doubles convention and the entry-cap+2 retune rule this gym follows; the rctmod series-graph invariant.
- **`legendaries_nobles`** — **owns the noble archetype contract** (trigger gate → PvP ladder → one-shot latched wild spawn → despawn/cleanup rules; `16_legendaries_nobles.md` §4.1). This area **owns Kyogre's local staging** (buoy, guardians, spawn point) and consumes that contract. Also the landing site for **Manaphy's underwater temple** (event legendary, that doc's Q-L9 — hard builder dependency).
- **`deepcore_city`** — gym 4; `deepcore_leader` is the prerequisite that opens the entire Gaviota ladder.
- **`kalahar_reach`** — gym 6; the cap-50 window + `gaviota_open_champion` flow into it.
- Soft: **`wheat_war_farms`** (skimmed-purse / recognition-ambush framing; no direct `free_field` here) and **`battle_frontier`** (an install.json **"Battle Port"** zone sits near the coast — possible facility adjacency to reconcile).

**Decisions the showrunner must make:**
1. **Leader ace level.** The entry-cap+2 rule → **46** (my recommendation, and what §4 is built to). The assignment brief says **"ace 52"**; the shipped config is **49**. 52 only holds if "entry cap" means the cap this gym *unlocks* (50)+2 — which contradicts "fought underleveled." **Confirm 46**, or accept Gaviota as a deliberate spike (and note it breaks the stated rule).
2. **Is the Open gated on the Current Badge?** (My design: yes — sets the lv 46–50 window and makes the badge the "port license.") Or open pre-badge with a lower purse?
3. ~~Kyogre: catchable vs static / Nuzlocke treatment~~ — **RESOLVED by showrunner ruling (2026-07-06):** monument warning → storm-watch PvP → Kyogre spawns as a **normal wild Pokémon**, catchable, standard wild rules, full Nuzlocke stakes, no exemptions. Remaining sub-question (owned by `legendaries_nobles` §9): the KO'd-noble re-call policy.
4. **Round-3 format:** solo **record-sprint** (implementable now, no new trainer) vs a **head-to-head PvP** vs Marlin (needs `sq_gaviota_open_final`).
5. **Coords:** all N/P/X coords are **PROPOSED** — builder to snap them to real dock geometry and confirm waterfront y (y64 vs y82) + the offshore Kyogre surface point.
6. **Folder tidiness:** move the two existing gym NPCs from `characters/gym/` into a new `characters/gaviota/`, or leave them?
7. **Verify** the Cobblemon spawn-command grammar from the jar before building the Kyogre trigger (per project rule).
