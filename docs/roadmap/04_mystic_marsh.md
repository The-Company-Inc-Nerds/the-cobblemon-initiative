# 04 — Mystic Marsh (Gym 3, Fairy, cap → 37)

> Area key: `mystic_marsh` · Act 1 · entry-cap 30 → unlocks 37 · leader ace = 30 + 2 = **32**
> Status of what already exists (verified in-tree):
> - NPCs: `dialog-src/characters/gym/mystic_leader.json` (Leader Titania), `dialog-src/characters/gym/mystic_guide.json`
> - Leader dialog: `dialog-src/dialog/gym_leader_mystic.json`
> - Full PvP ladder registry: `data/cobblemon_initiative/trainers/gyms/mystic_marsh.json` (trainers 1–4, jr, apprentice, leader)
> - Runtime teams (partial): `data/rctmod/trainers/mystic_{trainer_1,trainer_2,apprentice,leader}.json` + `mobs/trainers/single/mystic_{leader,apprentice}.json` + `mobs/trainers/groups/mystic_trainer.json`
> - Adjacent content: `dialog-src/characters/shrine/fairy_shrine_leader.json` (High Priestess Aurora) + `dialog-src/dialog/shrine_fairy.json`
> - Zones (`install.json`): **Mystic Marsh** (TOWN), **Fairy Shrine** (SHRINE), **Mirebloom Paddies** (FARM `farm_2`)
>
> This town is ~40% built at the config layer and 0% built at the townsfolk / side-quest / atmosphere layer. This doc is the plan for the other 60%, plus three file reconciliations that are currently silent bugs.

---

## 1. Concept & fantasy

**One-line pitch:** *A fog-drowned fairy bog that spits out the Company's spreadsheets — the last calm town of Act 1, and the only town where the dark still bites at night.*

Mystic Marsh is a boardwalk-and-stilt town sunk into a luminous bog: will-o'-wisp lanterns, mist that eats the horizon, frog-song, pink marsh-light (zone color `#F06292`). It is the **end of the "stable" feel** — the last town before prices start visibly slipping — so the design deliberately makes "stable" feel *uneasy* rather than cozy.

The marquee mechanical hook is already baked into `install.json`: **Mystic Marsh is the only TOWN with `mobsSpawn: true`** (every other town — Sango, Takehara, Hua Zhan, Deepcore, etc. — is `mobsSpawn: false`). The marsh does not fully suppress spawns. That means the "safe town" is not safe after dark: on a hardcore + Nuzlocke stream, a creeper in the gym plaza is genuine tension, and the whole town reads as *the safety net beginning to fray* — a perfect literalisation of the Act-1-ending beat.

Fairy-type fantasy carries the theme: the leader (Titania) and the adjacent Fairy Shrine keeper (Aurora) both **recognise the protagonist through the environment, not through a memo** — the marsh "whispers your name," the shrine-light "flickers when it touches you." This is the recognition arc delivered *supernaturally and obliquely*, which lets us hit the "the world knows you" note hard without an NPC ever breaking the "never name the Founder before Act 3" rule.

**Marquee stream moments**
- **Charm, then break.** Titania's gate line is *"Charm me, and then beat me. Both, or neither."* — the fairy gym as a two-part test.
- **The bog at night.** A town where the streamer must actually watch the tree line. First creeper-in-a-safe-zone moment of the run.
- **The Company drowning in favors.** A corporate "market penetration" survey team that literally cannot sell CobbleDollars to a town that runs on superstition and favors — corporate-dread comedy played straight.
- **The names in the water.** A diviner asks you to help her hear whose name the marsh keeps repeating. It is yours. She never says so; the audience gets there first.
- **Follow the wisps.** A lantern-relighting round down the boardwalk that doubles as the town's only guaranteed-safe night route.

---

## 2. Narrative role

| Field | Value | Source |
|---|---|---|
| Act | 1 (Infiltration) | brief / LORE §4 |
| `cd_instability` on leader defeat | **→ 24** ("end of stable feel") | LORE §8 beat map; leader reward already fires `economy/gym_destabilize` |
| Memory fragment | **frag_3** (fired by leader defeat via `function cobblemon_initiative:memory/gym/frag_3`) | `trainers/gyms/mystic_marsh.json` leader reward (already wired) |
| Badge / achievement | **Charm Badge** / `badge_fairy` | `mystic_marsh.json` `achievementOnDefeat` |
| Level cap unlocked | **37** (CLAUDE.md authoritative; LORE table "45" is stale — ignore) | CLAUDE.md |
| Recognition tier | **early** (grunts = confused hostility; "a face we were told to forget") + **environmental** recognition (marsh/shrine know him) | `mystic_leader.json` `recognition_tier: early`; LORE §4 gradient |
| Canon ties | Fairy Shrine (Aurora) is the town's south neighbour; Mirebloom Paddies (`farm_2`) is the town's liberatable wheat field; last town before traders begin recognising the player (Deepcore, ≥2 fields) | `install.json`; LORE §8 |

**Recognition rules honoured here:** Titania and Aurora recognise the *shape/name* through the marsh's magic, never the office — they are not Company. The Company **survey team are low-rank grunts**: memo-tier confused hostility, zero reverence, no Founder naming. **Civilians never recognise him** (the diviner hears a name in the water; she does not connect it to the man in front of her). Fragment `frag_3` stays **vague** — water/reflection dread that circles the shadow-self motif the Fairy Shrine also plays, and never closes it.

**Proposed `frag_3` flavour** (macro-delivered — no double-quotes, avoid apostrophes; PROPOSED, hand to `mainline_spine`):
`A still pool. A face in it a half second before I lean over. I know the face. I do not know the name it answers to. The water does.`

---

## 3. Layout & placements

**Town polygon** (`install.json` "Mystic Marsh"): x `858 → 1275`, z `2310 → 2556`, TOWN, `mobsSpawn: true`, `hostileOnly: true`, color `#F06292`. The **gym interior cluster is builder-confirmed** at ~`[1058–1073, 65, 2438–2447]` (coords baked into `mystic_marsh.json`). Approach is **Willowmire Path (Route 3)** from Hua Zhan (south gate ~`[1477,2275]`) down into the marsh east edge ~`[1243,2388]`.

Neighbours (confirmed zones): **Fairy Shrine** just south (x `881→1015`, z `2591→2726`, center ≈ `[945,64,2660]`); **Mirebloom Paddies** `farm_2` to the SE (x `1171→1278`, z `2772→2879`, center ≈ `[1225,64,2825]`).

| # | NPC / prop | Role | Coord | Confirmed? |
|---|---|---|---|---|
| 1 | **Leader Titania** (`mystic_leader`) | Fairy gym leader | `[1073,65,2441]` | **builder-confirmed** (in config) |
| 2 | Mystic Marsh Gym Guide (`mystic_guide`) | guide / verification bonus | `[1069,65,2450]` | PROPOSED (needs builder confirm) — nudge to gym door |
| 3 | Fairy Tale Girl Luna (`mystic_trainer_1`) | ladder | `[1061,65,2438]` | **builder-confirmed** |
| 4 | Hex Maniac Stella (`mystic_trainer_2`) | ladder | `[1064,65,2444]` | **builder-confirmed** |
| 5 | Pokémon Ranger Lyra (`mystic_trainer_3`) | ladder | `[1058,65,2441]` | **builder-confirmed** |
| 6 | Artist Viola (`mystic_trainer_4`) | ladder | `[1065,65,2447]` | **builder-confirmed** |
| 7 | Jr. Apprentice Fae (`mystic_jr_apprentice`) | ladder | `[1066,65,2441]` | **builder-confirmed** |
| 8 | Apprentice Faye (`mystic_apprentice`) | **DOUBLE** | `[1067,65,2438]` | **builder-confirmed** |
| 9 | Marsh Nurse (new) | heal | `[1010,64,2460]` | PROPOSED |
| 10 | Marsh Martkeeper (new) | Pokémart (tiered) | `[1000,64,2470]` | PROPOSED |
| 11 | **Bog-Diviner Nimue** (new) | Q3 giver ("Names in the Water") | `[960,64,2500]` | PROPOSED |
| 12 | **Lamplighter Miho** (new) | Q2 giver ("Lamplighter's Round") | `[1030,64,2498]` | PROPOSED |
| 13 | Boardwalk lanterns ×4 (props) | Q2 relight nodes | boardwalk pts (see Q2) | PROPOSED |
| 14 | **Ferry-Auntie Sedge** (new) | Q?/shrine bridge, ambient | `[905,64,2555]` (south, toward shrine) | PROPOSED |
| 15 | **Company Onboarding Lead** (new grunt) | Q1 giver/boss | `[1120,64,2402]` (NE, Willowmire mouth) | PROPOSED |
| 16 | Company Onboarding Rep ×2 (new grunts) | Q1 battle | flanking #15 | PROPOSED |
| 17 | Stilt-house Auntie (ambient civilian) | flavour, never recognises | `[985,64,2440]` | PROPOSED |
| 18 | Superstitious kid (ambient civilian) | flavour hint | `[1035,64,2465]` | PROPOSED |
| 19 | Wheat Trader (existing `dialog:wheat_trader`) | economy hook at the paddy | `[1225,64,2820]` (in `farm_2`) | PROPOSED — coordinate `wheat_war_farms` |
| — | High Priestess Aurora (`fairy_shrine_leader`) | shrine leader (exists) | Fairy Shrine ≈ `[945,64,2660]` | zone confirmed; owned by `shrines_audit` |

All new townsfolk use **`placement:{x,y,z}` with NO `uuid`** so they spawn once per world via the generated proximity functions (bypassing the builder world) — same pattern as `station_pond` and `agent_yield_lead`. If the builder later stages bodies, swap to `uuid` adoption. `y=64` is the marsh floor; the gym cluster sits at `y=65`.

---

## 4. Gym / core structure — the Charm Badge ladder

The PvP ladder **already exists** in `data/cobblemon_initiative/trainers/gyms/mystic_marsh.json` with a correct prerequisite chain. It needs (a) **team retune to the entry-cap+2 rule**, (b) the **double-battle format reconciled**, and (c) **three missing rctmod runtime team files created**. Gate wiring below is verified from the config.

**Ladder & gate wiring** (each battle gated on the previous `defeated_*`; winning a `tbcs`/character battle sets `defeated_<trainerId>`):

| Order | trainerId | Display | `prerequisites` (gate) | Format | Retuned levels (ace) |
|---|---|---|---|---|---|
| entry | — | (must hold Bloom Badge) | `hua_zhan_leader` | — | cap 30 |
| 1 | `mystic_trainer_1` | Fairy Tale Girl Luna | `hua_zhan_leader` | SINGLES | Clefairy 26 / Snubbull 27 |
| 2 | `mystic_trainer_2` | Hex Maniac Stella | `hua_zhan_leader` | SINGLES | Jigglypuff 27 / Togetic 27 |
| 3 | `mystic_trainer_3` | Ranger Lyra | `hua_zhan_leader` | SINGLES | Marill 27 / Ralts 27 |
| 4 | `mystic_trainer_4` | Artist Viola | `hua_zhan_leader` | SINGLES | Cottonee 27 / Spritzee 28 |
| 5 | `mystic_jr_apprentice` | Jr. Apprentice Fae | `hua_zhan_leader`,`mystic_trainer_1`,`mystic_trainer_2` | SINGLES | Kirlia 29 / Granbull 29 |
| 6 | `mystic_apprentice` | **Apprentice Faye** | `mystic_jr_apprentice` | **DOUBLES** | Kirlia 30 / Granbull 30 / Togetic 30 / Clefable 31 |
| 7 | `mystic_leader` | **Leader Titania** | `mystic_apprentice` | SINGLES | see below (ace **32**) |

**The required double is battle 6 (`mystic_apprentice`).** The config already declares it `GEN_9_DOUBLES` — but the runtime file `data/rctmod/trainers/mystic_apprentice.json` currently declares `GEN_9_SINGLES` with only **3 mons** (Kirlia/Granbull/Togetic, 27–28). **This is a live mismatch** and a doubles battle needs ≥4 mons: fix by rewriting the rctmod file to `GEN_9_DOUBLES` with a 4-mon team (levels above).

**Leader team sketch — Leader Titania (Fairy, ace = 32).** `data/rctmod/trainers/mystic_leader.json` is already correctly tuned to the balance rule (Clefable 30 / Mawile 31 / Gardevoir 32 — ace 32). The embedded team in `trainers/gyms/mystic_marsh.json` is **stale/over-levelled (Sylveon 34 / Gardevoir 35 / Togekiss 35 / Clefable 36 — ace 36)** and must be retuned or stripped (see §8 gotchas). Recommended ~6-mon marquee roster (ship 4 if keeping project convention; ace stays 32):

| Slot | Species | Lvl | Ability | Set (short) | Item |
|---|---|---|---|---|---|
| lead | Clefable | 30 | Magic Guard | moonblast / softboiled / calm mind / flamethrower | leftovers |
| 2 | Mawile | 31 | Intimidate | play rough / iron head / sucker punch / swords dance | — |
| 3 | Togekiss | 31 | Serene Grace | air slash / dazzling gleam / nasty plot / roost | — |
| 4 | Whimsicott | 31 | Prankster | moonblast / u-turn / tailwind / encore | — |
| 5 | Sylveon | 32 | Pixilate | hyper voice / mystical fire / calm mind / wish | — |
| **ace** | **Gardevoir** | **32** | Trace | moonblast / psychic / shadow ball / calm mind | sitrus berry (or life orb) |

**Ship-4 recommendation** (matches Hua Zhan's 4-mon leader convention): Clefable 30, Mawile 31, Togekiss 31, **Gardevoir 32 (ace)**. Bag: `full_restore ×3` (already set). Note the pure-Fairy skew gives the streamer a clean type check (Steel/Poison answers) — intentional for a teaching gym; Mawile/Gardevoir add the Steel/Psychic wrinkles so it is not a free sweep.

---

## 5. Quests & side quests

All three are **on-theme corporate-dread comedy / marsh-mystic** and reuse proven patterns. Side-quest CD payouts sit in the established **150–900 "side band"** (per `groundskeeper_aya` comment) via `function cobblemon_initiative:economy/payout {amount:N}`.

### Q1 — "Market Penetration" (villain comedy) — giver: Company Onboarding Lead
- **Hook:** A Company survey team has set up a folding table at the Willowmire mouth to "onboard the marsh onto Verified CobbleDollars." It is going terribly: the marsh runs on favors and superstition, and the locals keep sending them into the bog with cursed directions. The Lead mistakes the player for a competing auditor — **memo-tier confused hostility** ("You match a description we were told to forget. Are you here to file, or to be filed?").
- **Steps:** (1) Talk to the Lead (ungated, Act-1 grunt). (2) The stilt-house auntie (#17) confirms the joke: the town will not sign. (3) Battle the two Onboarding Reps (a `GEN_9_DOUBLES` "joint survey" carried on **one** grunt entity — copy `agent_yield_lead`'s tag-team pattern with `despawn_on_win`, second rep stands down via its own gate on the defeat tag).
- **Gates:** giver ungated; battle button `not_tag: defeated_sq_marsh_survey`. Resolution entry `gate: { defeated: sq_marsh_survey }`.
- **Rewards:** `payout {amount:520}` + `super_potion ×2` + a **scrubbing-artifact lore beat** (they leave behind a re-verified ledger page and a memo about "the impostor from the old company") — reads on stream as cover-up foreshadowing.
- **Resolution:** the survey folds; the marsh keeps its favors economy. Ties `company_hq` (early grunt roster) and `wheat_war_farms` (the paddy is the Company's real play here).

### Q2 — "The Lamplighter's Round" (marsh atmosphere) — giver: Lamplighter Miho
- **Hook:** Because the marsh does not suppress mobs at night, the boardwalk lamps are the only safe route to the shrine and the paddy. The fog put four of Miho's wisp-lanterns out; travellers are getting lost to *things* in the dark.
- **Steps:** Relight four boardwalk lanterns in order — prop NPCs using the **`station_pond` plaque/seal pattern** (interact → `tag @s add lamp_N` → actionbar → open next). Suggested lantern nodes (PROPOSED): `[1015,64,2500]`, `[985,64,2520]`, `[955,64,2545]`, `[925,64,2558]` (a line down toward the shrine).
- **Gates:** each lantern `gate: { not_tag: lamp_N }`, lantern N+1 gated on `lamp_N`; completion latch `lamplighter_round_done` when `all_tags: [lamp_1..lamp_4]`.
- **Rewards:** `payout {amount:400}` + a **Fairy-relevant evolution item** (`cobblemon:shiny_stone` or `cobblemon:moon_stone`) + flavour: the lit boardwalk is now the safe night path. Optional QoL: while `lamplighter_round_done`, an actionbar reminder that the boardwalk is safe after dusk.
- **Resolution:** the town gets its one reliable safe corridor — a small mercy the streamer earns, reinforcing that safety here is *worked for*, not given.

### Q3 — "The Names in the Water" (recognition, streamable) — giver: Bog-Diviner Nimue
- **Hook:** Nimue reads names the marsh repeats back from the deep pools. Lately it keeps returning **one name she cannot place** — and it goes quiet the moment the player stands at the water. She asks the player to help her hear it (do a favour, win a "listening duel," or fetch a marsh-lily from near the shrine approach).
- **Steps:** (1) Nimue explains the marsh has been saying one name for weeks. (2) The player completes her favour (recommend a **one-time charm-duel**, `groundskeeper_aya` one-off pattern, or a fetch gated behind Q2's safe path). (3) At the pool, the name surfaces — rendered as `§k` static / a blank the marsh "swallows." Nimue: *"It answers to you and it will not say why. The water is kinder than I am — it did not tell me the rest."*
- **Gates:** favour `not_tag: names_water_done`; resolution `gate: { tag: names_water_done }`. Keep the reveal **oblique** — no Founder naming (Act-1 rule).
- **Rewards:** `payout {amount:600}` + a memory-fragment-adjacent flavour beat (NOT a real fragment — `frag_3` belongs to the leader defeat; this only *rhymes* with it) + a keepsake (`cobblemon:pretty_feather` / marsh trinket).
- **Resolution:** the "ooh" quest. Sets up the Fairy Shrine's mirror/shadow motif (Aurora: "I saw a long, long shadow already cast") and the eventual mirror reveal, without spending the reveal.

**Optional — Guide Verification Bonus (pattern only).** Mirror `sq_perf_review_guide`: pay a marsh-flavoured bonus for clearing the whole PvP ladder (`all_tags: defeated_mystic_trainer_1..4`, `not_tag: mystic_verify_paid`). **GOTCHA:** `mystic_guide` currently points at the SHARED `dialog:gym_guide` — merging entries there **leaks the bonus to all ten gym guides** (gates are global player tags). Fork a marsh-specific guide dialog first (exactly the warning in `sq_perf_review_guide.json`).

---

## 6. Trainers & teams needed

**rctmod runtime team files** (`data/rctmod/trainers/`):

| File | Action | Notes |
|---|---|---|
| `mystic_trainer_1.json` | exists — verify vs retune levels | Luna 26/27 |
| `mystic_trainer_2.json` | exists — verify vs retune levels | Stella 27/27 |
| `mystic_trainer_3.json` | **CREATE** | Ranger Lyra — Marill 27 / Ralts 27 (team already in gym config) |
| `mystic_trainer_4.json` | **CREATE** | Artist Viola — Cottonee 27 / Spritzee 28 |
| `mystic_jr_apprentice.json` | **CREATE** | Jr. Apprentice Fae — Kirlia 29 / Granbull 29 |
| `mystic_apprentice.json` | **REWRITE** | change `GEN_9_SINGLES`→`GEN_9_DOUBLES`, expand to 4 mons (30/30/30/31) |
| `mystic_leader.json` | keep (ace 32) or expand to 4–6 | already tuned; do NOT raise ace above 32 |

**mob/series wiring** (`data/rctmod/mobs/trainers/`): `single/mystic_leader.json` + `single/mystic_apprentice.json` exist; add `single/` entries if the missing trainers need standalone spawn control; `groups/mystic_trainer.json` exists (the four ladder trainers). Confirm the new `_3/_4/jr` ids are members of the group or spawn as intended.

**Gym registry** (`data/cobblemon_initiative/trainers/gyms/mystic_marsh.json`): structure is correct (ids, prerequisites ladder, coordinates, rewards, `achievementOnDefeat: badge_fairy`). Retune embedded team levels to match the table in §4; **reconcile the leader team down to ace 32**; leave reward commands (`memory/gym/frag_3`, `shop badge_3`, `economy/gym_destabilize`) untouched.

**Side-quest trainers** (register in `data/cobblemon_initiative/trainers/side_quests/act1.json` + create matching `data/rctmod/trainers/*.json`):

| trainerId | Display | Format | Levels (vs cap 30) | For |
|---|---|---|---|---|
| `sq_marsh_survey` | Company Onboarding Reps | `GEN_9_DOUBLES` | 4 mons ~27–29 (Company-standard, e.g. Golbat/Mightyena/Watchog-tier grunt mons) | Q1 |
| `sq_marsh_favor` | (Nimue's listening-duel, optional) | `GEN_9_SINGLES` | 2 mons ~28 Fairy | Q3, if duel variant chosen |

Battle formats/levels respect the ladder: all interior + side content stays **≤ leader ace 32**, entry-cap 30. Grunt teams are **under** the ladder (confused, failing) — mechanically easy, narratively the joke.

---

## 7. Economy & rewards

| Source | Reward | Mechanism |
|---|---|---|
| Leader Titania | **2400 CD** prize; `badge_fairy`; `frag_3`; shop→`badge_3`; instability→**24** | existing `mystic_marsh.json` leader block (prize on `battle.prize` in `mystic_leader.json` char; reward commands in registry) |
| Ladder trainers 1–4 | `potion ×2` each | existing |
| Jr. Apprentice / Apprentice | `super_potion ×2` / `hyper_potion ×2` | existing |
| Q1 Market Penetration | **520 CD** + `super_potion ×2` + lore drop | `economy/payout {amount:520}` |
| Q2 Lamplighter's Round | **400 CD** + Fairy evo stone | `economy/payout {amount:400}` |
| Q3 Names in the Water | **600 CD** + keepsake | `economy/payout {amount:600}` |
| Optional guide bonus | ~600 CD + candy (copy `training_major` loot) | forked guide dialog |

**Shop tier:** leader defeat fires `cobblemon-initiative shop badge_3` (already wired) — the marsh Martkeeper (#10) is the local face of the tiered shop; prices step up with `cd_instability` per the shop-tiers system.

**Field / liberation tie:** **Mirebloom Paddies = `farm_2`.** Its liberation (`function cobblemon_initiative:liberation/free_field {field:farm_2}`) is owned by **`wheat_war_farms`**, not this doc — but the marsh is where the player *first sees* a Company-occupied field pressed right up against a town, and the Wheat Trader (#19) at the paddy is the economy's "alternative currency made flesh." Every 2 liberated fields steps the shop to a **relief** tier (CD prices ease, Granary wheat prices worsen) — flag the paddy as an early candidate.

**CD-sink comedy:** the marsh "does not take CobbleDollars — only favors." Use this as the town's running gag (the Martkeeper apologises that he *has* to take them; the ferry-auntie refuses money and asks for a favour instead). Reinforces the thesis that the Company's money is losing its grip exactly where superstition is strongest.

---

## 8. Implementation notes / FUTURE-ME HOOKS

**Build order (copy these exact patterns):**
1. **Reconcile the three silent bugs first** (see gotchas) — do this before authoring, or you will balance against stale numbers.
2. **Author new NPCs** under `dialog-src/characters/gym/` (townsfolk/quest) and `dialog-src/characters/villain/` (survey grunts) with matching trees in `dialog-src/dialog/`. Patterns:
   - Prop/seal interactions (lanterns, diviner pool) → copy `dialog-src/characters/gym/station_pond.json` (`dialog_inline`, `tag @s add …`, `open_dialog` chaining, `placement` no-uuid).
   - Company grunt carrying a `GEN_9_DOUBLES` tag-team → copy `dialog-src/characters/takehara/agent_yield_lead.json` (`battle` block, `despawn_on_win`, second body stands down on the defeat tag).
   - One-time optional duel + registry + rctmod team → copy `groundskeeper_aya.json` (character) + its `act1.json` entry + `data/rctmod/trainers/groundskeeper_aya.json`.
   - Guide verification bonus → copy `dialog-src/dialog/sq_perf_review_guide.json` **but fork the guide dialog** (do not touch shared `gym_guide`).
3. **Register** side-quest trainers in `data/cobblemon_initiative/trainers/side_quests/act1.json`; **create** their `data/rctmod/trainers/*.json`.
4. **Compile pipeline** (in order): `scripts/content_compile` → `scripts/generate_granary_tiers` → `scripts/update_preset_index` → `scripts/generate_npc_function`. This lowers characters to `src/main/resources/data/easy_npc/preset/humanoid(_slim)/<id>.npc.snbt` and rewrites `npc/preset_map.json` + `function/update_npc_presets.mcfunction`.
5. **Build:** `gradle build`. Runtime-test the gym ladder gates and the doubles battle.

**Files to create (full paths):**
- `dialog-src/characters/gym/mystic_nurse.json`, `mystic_martkeeper.json`, `bog_diviner_nimue.json`, `lamplighter_miho.json`, `marsh_lantern_1.json`…`_4.json`, `ferry_auntie_sedge.json`, `marsh_auntie.json` (ambient), `marsh_kid.json` (ambient)
- `dialog-src/characters/villain/marsh_survey_lead.json`, `marsh_survey_rep.json`
- `dialog-src/dialog/` trees for each of the above that need custom text; **fork** `dialog-src/dialog/gym_guide_mystic.json` if adding the guide bonus
- `data/rctmod/trainers/mystic_trainer_3.json`, `mystic_trainer_4.json`, `mystic_jr_apprentice.json`, `sq_marsh_survey.json` (+ `sq_marsh_favor.json` if used)
- rewrite `data/rctmod/trainers/mystic_apprentice.json` (→ DOUBLES, 4 mons)

**Gotchas (all verified in-tree — do not skip):**
1. **Leader team fork.** `trainers/gyms/mystic_marsh.json` embeds an over-levelled leader (ace **36**); `data/rctmod/trainers/mystic_leader.json` is the tuned one (ace **32**). Decide which the runtime battle actually reads and make them agree at **ace 32**. Do not "fix" by raising the ladder — retune teams, never the cap (CLAUDE.md / gym-balance memory).
2. **Apprentice format mismatch.** Config says `mystic_apprentice` is `GEN_9_DOUBLES`; the rctmod file says `GEN_9_SINGLES` with 3 mons. The **required double** breaks unless you rewrite the rctmod file to DOUBLES with ≥4 mons.
3. **Missing rctmod files.** `mystic_trainer_3/4` and `mystic_jr_apprentice` are in the gym config but have **no** `data/rctmod/trainers/` team file — the mid-ladder will not spawn/battle correctly until created.
4. **`mobsSpawn: true` in a TOWN.** Mystic Marsh is the *only* town that lets hostiles spawn. This contradicts CLAUDE.md's blanket "towns suppress spawns / suspend Nuzlocke." **Confirm intent** (§9) before treating the town as a safe zone — the whole atmosphere pitch depends on it staying `true`.
5. **Shared guide dialog.** `mystic_guide` → `dialog:gym_guide` (shared by all 10 guides). Any tag-gated bonus added there **leaks to every town**. Fork first.
6. **Macro text hygiene.** `frag_3`, `economy/payout`, and any `tbcs … onwin {…}` payload must contain **no double-quotes** and avoid apostrophes (no macro escaping). Sample say-lines above already avoid apostrophes to match house style.
7. **onwin token order.** If wiring battles via raw `tbcs battle … onwin {1:[…],2:[…]}` instead of the character `battle` block, key **1 = player won** (winners-first, ENGINE_FINDINGS). Winning must `tag @1 add defeated_<trainerId>`.

---

## 9. Dependencies & open questions

**Depends on (other area keys):**
- **`shrines_audit`** — Fairy Shrine (Aurora) is the town's south neighbour and shares the marsh's shadow/mirror motif; align gating (is the shrine open on arrival, or gated behind a marsh favour/ferry?) and the frag_3 ↔ shrine tone handoff.
- **`wheat_war_farms`** — Mirebloom Paddies (`farm_2`) is this town's liberatable field; owns the Wheat Trader placement, farm-agent battles, and the `liberation/free_field {field:farm_2}` flow. Q1 (Market Penetration) is the town-side comedy face of that occupation.
- **`gym_system_pvp_doubles`** — the doubles standard and the apprentice format reconciliation (gotcha #2) should be resolved consistently with the other gyms.
- **`mainline_spine`** — owns `frag_3` text, `cd_instability → 24`, the recognition gradient, and band-tag gating; this doc proposes flavour, spine ratifies.
- **`company_hq`** — the survey grunts are early rungs of the grunt roster (Field Agent tier); keep titles/tone continuous with the HQ ladder.
- **`deepcore_city`** — next town; Mystic Marsh is the last "stable" beat before Deepcore's "prices adjusting" + trader-recognition (≥2 fields). Hand off the tonal shift.

**Showrunner decisions needed:**
1. **Is Mystic Marsh intentionally not a full safe zone?** (`mobsSpawn: true`.) If yes (recommended — it is the whole hook), confirm Nuzlocke mechanics do / don't suspend inside the town, and whether the Lamplighter's Round should grant a real safe corridor vs. pure flavour. **Highest-stakes call in this doc.**
2. **Leader team size:** ship the 4-mon convention (Clefable/Mawile/Togekiss/Gardevoir, ace 32) or expand to a 6-mon marquee Fairy roster? Ace stays 32 either way.
3. **Which leader file is canonical** — retune the embedded gym-config team to 32, or strip it and rely on `rctmod`? (Pick one to end gotcha #1.)
4. **Q3 favour variant:** listening-duel (needs `sq_marsh_favor` team) vs. fetch-quest (needs a marsh-lily item + safe-path gate). Duel is more streamable; fetch is cheaper to build.
5. **Fairy Shrine access gating** — does the ferry-auntie / a marsh favour gate the shrine approach, or is it walk-up? (Coordinate with `shrines_audit`.)
