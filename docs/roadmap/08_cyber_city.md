# 08 — Cyber City (Gym 7, Electric, cap → 62): The Hard Turn & HQ Unlock

> **Area key:** `cyber_city`
> **Status of what exists:** `dialog-src/characters/gym/cyber_leader.json` + `cyber_guide.json`
> (authoring stubs), `dialog-src/dialog/gym_leader_cyber.json` (full leader tree, already seeds
> the HQ hand-off), and a COMPLETE RCT/registry ladder in
> `src/main/resources/data/cobblemon_initiative/trainers/gyms/cyber_city.json`
> (cyber_trainer_1..4, jr_apprentice, apprentice-DOUBLE, leader). The gym leader reward already
> fires `memory/gym/frag_7`, `shop badge_7`, and `economy/gym_destabilize`. **What is missing is the
> town skin: interior-trainer NPC bodies, side quests, the ZAPDOS noble defense event (RULING
> 2026-07-06), and the marquee — the KEYCARD leak that flips Act 1 → Act 2.**
> **GEOMETRY CANON (SHOWRUNNER RULING 2026-07-06):** the Company HQ is a **skyscraper in Cyber
> City** — first-floor lobby entry, evil-HQ basement below (the raid), corporate flavor floors +
> penthouse above. The keycard opens the tower's **basement elevator** (PROPOSED mechanics —
> `company_hq` owns the door). See `09_company_hq.md`.

---

## 1. Concept & fantasy

**One-line pitch:** *Cyber City is the Company's neon capital, and it is the town where the mask
finally slips — win the Circuit Badge, and a defecting insider presses a keycard into your hand for
the tower with your own signature on its charter.*

The fun:
- **The hard turn, made physical.** Every town so far has been ambient dread — prices "adjusting,"
  brighter rectangles where portraits hung. Cyber City is where the Company stops pretending. The
  propaganda boards downtown are **glitching** (`§k`), the exchange rate is **openly broken**
  (`cd_instability` peaks here at **56** — money has never felt this fake), and grey suits are
  visibly *packing the tower down* rather than trading. Volt says it out loud in his existing
  after-defeat line: *"the towers downtown are crawling with grey suits… their headquarters is
  somewhere in the heart of it."*
- **Speed-gym fantasy.** Volt's whole identity is *the half-second before you know what hit you*
  (his shipped dialog). Electric-type glass cannons that out-speed and paralyze — a brutal Nuzlocke
  wall right as the story detonates.
- **Marquee stream moment — THE KEYCARD.** After Volt falls, a burned-out Company access-control
  admin who **took the founder's portrait down with her own hands** recognizes the amnesiac, has a
  small breakdown, and *defects* — she does not fight, she hands over the keycard and points you at
  the **Company skyscraper's street lobby** (the tower on the downtown's north block; the lobby is
  public — the card is for the **elevator that goes down**). It is the first Company person to
  **stand down** and the literal door-opener from gym-crawl to corporate raid. The frag_7 sting —
  *"You signed this charter. Your hand. Your seal."* — lands in the same visit. This is the
  episode's cold open cliffhanger.
- **Second marquee — STORMFRONT (Zapdos noble defense, RULING 2026-07-06).** A grid warden asks the
  question out loud on stream: *"Will you help us defend Cyber City from Zapdos?"* Accept → a
  substation defense gauntlet → and then Zapdos itself lands on the transmission mast as a **real
  wild spawn**, full hardcore-Nuzlocke stakes. See §5.4.

---

## 2. Narrative role

| Field | Value | Source |
|-------|-------|--------|
| Act | **1 → 2 pivot** (the unlock beat itself is Act 2) | brief; `cyber_leader.json` `act:"2"` |
| `cd_instability` | **→ 56 (series PEAK)** — fired by the leader's `economy/gym_destabilize` (+8) | `economy/gym_destabilize.mcfunction`, LORE_BIBLE §8 |
| Memory fragment | **frag_7 — "You signed this charter. Your hand. Your seal." / "…why does the word verifies feel like a knife?"** | `function/memory/gym/frag_7.mcfunction` (already built) |
| Recognition tier | **mid → late.** Entering with 6 badges = mid; the instant Volt falls the player is at **7 badges = LATE** (`badges_gte_7`). The leak beat is a **late-recognition** scene by construction. | `dialog/schema/README.md` §6 (late = 7+); `band_tags.mcfunction` L25 |
| Level cap | Entry cap **56** (from Kalahar); Cyber unlocks **62** on Volt's defeat | CLAUDE.md ladder (authoritative) |
| Canon ties | HQ raid gate (gym 7 + **6-of-10 fields majority** — RULING 2026-07-06; shipped files still say 4, `company_hq` owns the edits), Acting CEO DJ, the scrubbing artifacts, the wheat-monopoly "starve it first" gate | `quest/render.mcfunction` L34-38, `acting_ceo_dj.json`, LORE_BIBLE §4 |

**Canon guardrails honored here:** the protagonist is never *named* as the Founder (frag_7 circles it, the
admin recognizes the *face*, not the name); civilians never recognize him (the money-changer only knows
the propaganda decayed); no illness angles; corporate-dread comedy played straight (burned-out grunts on a
union break) against the genuine menace of the peak-instability tower.

---

## 3. Layout & placements

**Zone (real, `install.json`):** `Cyber City`, TYPE `TOWN`, priority 2, color `#FFEE58`, subtitle
*"Gym 7 — Electric Type"*. Polygon spans roughly **x ∈ [1229, 1798], z ∈ [888, 1472]** (a large,
lobed downtown).

**Key geography fact (point-in-polygon VERIFIED against `install.json`, 2026-07-06):** the
`The Company, Inc.` VILLAIN zone (`x 1559–1678, z 983–1130`, `#455A64`, `announce:true`, subtitle
*"Something stirs in your memory."*) **does NOT overlap** the Cyber City polygon — the two zones
**adjoin along a shared seam** (shared vertices `1564/1064 → 1587/1073 → 1596/1125 → 1626/1130 →
1642/1125 → 1646/1096 → 1652/1087`). The Company block hangs off the **north edge of Cyber City's
eastern downtown lobe**, and the HQ core **`[1590 51 1028]` tests INSIDE the Company zone, OUTSIDE
the town polygon.**
**⚠ DISCREPANCY — builder/showrunner confirm:** the RULING (2026-07-06) makes the HQ *a skyscraper
in Cyber City*; the zone data puts the tower block in its own adjoining VILLAIN carve-out. In play
that may be the desired read — cross the plaza, the VILLAIN banner fires, the tower announces
itself; Volt's "in the heart of it" still lands because the block is part of the downtown continuum.
But if "in Cyber City" should mean *inside the town polygon*, the zones need redrawing. Flagged in
`09_company_hq.md` §3a / Q1b — do not silently resolve here. (y note: the core's y=51 sits 13 blocks
under the zone's `centerY 64` street level — it is the **basement boardroom**, not the entrance.)

| NPC / prop | Role | Coord | Confidence |
|-----------|------|-------|-----------|
| **Volt** (`cyber_leader`) | Gym leader | `~[1462, 89, 1185]` | **Config-grounded** (`cyber_city.json`); builder-confirm exact stand |
| **Cyber City Gym Guide** (`cyber_guide`) | Type-tip / heal-adjacent | gym entrance, `~[1450, 89, 1178]` | PROPOSED (needs builder confirm) |
| `cyber_trainer_1` **Guitarist Amp** | Ladder #1 | `[1450, 89, 1182]` | **Config-grounded** |
| `cyber_trainer_2` **Engineer Watt** | Ladder #2 | `[1453, 89, 1188]` | **Config-grounded** |
| `cyber_trainer_3` **Rocker Static** | Ladder #3 | `[1447, 89, 1185]` | **Config-grounded** |
| `cyber_trainer_4` **Mechanic Gigabyte** | Ladder #4 | `[1454, 89, 1191]` | **Config-grounded** |
| `cyber_jr_apprentice` **Jr. Apprentice Voltz** | Jr. gate | `[1455, 89, 1185]` | **Config-grounded** |
| `cyber_apprentice` **Apprentice Surge** | **DOUBLE** gate | `[1456, 89, 1182]` | **Config-grounded** |
| **cyber_access_admin** (leak / defector) | **KEYCARD hand-off** | `~[1545, 66, 1072]` — street doorstep just W of the Company block (PIP-verified inside the town polygon), facing E toward the **tower lobby** (street level ~y64; `[1590 51 1028]` is the basement core below it) | PROPOSED (needs builder + `company_hq` confirm) |
| **cyber_exchange_teller** (`Verified Value Teller`) | Side-quest giver (Exchange Rate) | downtown exchange kiosk `~[1500, 65, 1120]` | PROPOSED |
| **cyber_comms_tech** (`Signal Tech Rell`) | Side-quest giver (Signal Integrity) | comms van, plaza `~[1470, 65, 1140]` | PROPOSED |
| 3× **glitching billboard** props (`cyber_board_1/2/3`) | Signal Integrity targets | scattered downtown, PROPOSED spots near `[1490,1130]`, `[1560,1090]`, `[1610,1110]` | PROPOSED |
| **cyber_noodle_grunts** (`Off-Duty Contractor` ×2) | Side-quest / comedy | neon noodle bar `~[1420, 65, 1210]` | PROPOSED |
| **cyber_grid_warden** (`Grid Warden Ohm`) | **STORMFRONT giver — the Zapdos prompt** (§5.4) | substation yard, E downtown `~[1720, 65, 1150]` | PROPOSED |
| `sq_cyber_storm_1/2/3` (grid defenders) | Stormfront PvP gauntlet | pylons `~[1730, 65, 1180]`, `~[1690, 65, 1210]`, `~[1750, 65, 1230]` | PROPOSED |
| **Zapdos wild-spawn point** | Stormfront phase 2 | transmission mast `~[1720, ~80, 1200]` (rooftop/mast height — builder) | PROPOSED |

All PROPOSED coords sit inside the Cyber City polygon (point-in-polygon verified against
`install.json` vertices, incl. the Stormfront spots); the leak admin sits on the shared Cyber/HQ
seam so it reads as "outside your own tower." **No terrain is assumed** — these are NPC/prop latches
only (props via the `station_moss` disguised-plaque pattern).

---

## 4. Gym / core structure

The PvP ladder **already exists** in `trainers/gyms/cyber_city.json`; this section documents it and the
dialog bodies to author around it. Each battle button's line-select gate mirrors the config
`prerequisites` (the config enforces the *real* battle gate; dialog gates only pick the line).

**Interior ladder (each gated on the previous `defeated_*`):**

| Order | NPC (id) | Format | Team (shipped) | Line-select gate |
|-------|----------|--------|----------------|------------------|
| 1 | Guitarist Amp (`cyber_trainer_1`) | SINGLES | Voltorb 48, Magnemite 48 | `defeated_kalahar_leader` |
| 2 | Engineer Watt (`cyber_trainer_2`) | SINGLES | Pikachu 48, Shinx 48 | `defeated_kalahar_leader` |
| 3 | Rocker Static (`cyber_trainer_3`) | SINGLES | Elekid 48, Pachirisu 48 | `defeated_kalahar_leader` |
| 4 | Mechanic Gigabyte (`cyber_trainer_4`) | SINGLES | Emolga 49, Luxio 49 | `defeated_kalahar_leader` |
| 5 | Jr. Apprentice Voltz (`cyber_jr_apprentice`) | SINGLES | Electrode 52, Luxray 52 | `defeated_cyber_trainer_1` + `_2` |
| 6 | **Apprentice Surge (`cyber_apprentice`) — THE DOUBLE** | **GEN_9_DOUBLES** | Raichu 54, Magneton 54, Jolteon 54, Pachirisu 55 (Follow-Me/Volt-Absorb redirection core) | `defeated_cyber_jr_apprentice` |
| 7 | **Leader Volt (`cyber_leader`)** | SINGLES | see below | `defeated_cyber_apprentice` |

**The DOUBLE** is Apprentice Surge (`GEN_9_DOUBLES` in the shipped config) — a Pachirisu *Follow Me +
Volt Absorb* redirection team, the "read the surge" thematic mirror of Volt himself. Copy the
`hua_zhan_apprentice` doubles pattern for the dialog body.

**Leader team sketch — ace = entry-cap(56) + 2 = 58.** The shipped file ships **4 mons topping at
Raichu 58**, which is already correctly balanced to the rule. **Recommended retune to a 6-mon
marquee wall** (levels stay ≤ 58 so the fight is fought *underleveled* at cap 56):

| Slot | Species | Lvl | Set (theme) |
|------|---------|-----|-------------|
| Lead | Magnezone | 56 | Choice Specs, Magnet Pull — traps + deletes Steels/other Electrics |
| 2 | **Lanturn** (new) | 56 | Volt Absorb bulk — the sponge that makes the timer bleed |
| 3 | Jolteon | 57 | Life Orb, +spe — the "half-second" cleaner |
| 4 | **Rotom-Wash** (new) | 57 | Levitate pivot — Volt Switch + coverage, dodges Ground |
| 5 | Electivire | 57 | Motor Drive, Ice Punch / Earthquake / Cross Chop — breaks your Ground answer |
| **Ace** | **Raichu** | **58** | Lightning Rod + **Nasty Plot** sweeper (Focus Blast / Grass Knot) — the setup-sweep finish |

Ace = **58** (entry-cap 56 + 2). **⚠ The assignment header says "ace 64"; that is `62 + 2` (this
gym's *unlock* + 2), which contradicts the CLAUDE.md balance rule and the shipped file. Recommend
holding 58 — see Open Questions.**

**Gate wiring summary (all confirmed tags in `band_tags.mcfunction`):**
`defeated_kalahar_leader` → trainers 1-4 → `defeated_cyber_trainer_1/2` → jr → `defeated_cyber_jr_apprentice`
→ apprentice(DOUBLE) → `defeated_cyber_apprentice` → Volt → `defeated_cyber_leader` → **leader reward fires
`frag_7` + `shop badge_7` + `gym_destabilize` (idx → 56)** → **opens the leak beat.**

---

## 5. Quests & side quests

Four: the keycard marquee, two colour quests on the "the money is openly breaking / the mask slips"
theme, and the **ZAPDOS noble defense event** (RULING 2026-07-06). All are **Act-2 flavored**;
gate their *availability* on `badges_gte_7` (true the moment Volt falls) so they read as the hard turn.

### 5.1 THE KEYCARD — "Access Control" (marquee; the Act 1 → Act 2 hand-off)
- **Giver:** `cyber_access_admin` — *Access Control Admin* (a defector; **civilian/quest_giver, NOT a
  battler** — the beat is that a Company person will not raise a hand against the founder).
- **Hook:** Volt's after-line points downtown; the admin is at the tower's street doorstep. She
  recognizes the face she personally scrubbed from the lobby wall.
- **Gate:** entry `reveal` gated on **`defeated_cyber_leader`** (primary) + recognition-late flavor.
- **Steps:** (1) talk → she recognizes you, panics, then decides; (2) she gives the **Company Keycard**
  (`loot give` from `npc_gift/hq_keycard`, mirroring `villain_site_manager`'s `transition_order` gift)
  + sets tag **`hq_keycard`**; (3) she `announce`s the pointer to the **skyscraper lobby** — the lobby
  is public; **the card is for the basement elevator** (PROPOSED mechanics — `company_hq`'s
  `hq_lobby_guard` checks the tag; ruling 4 geometry); (4) she delivers the **"starve it first"**
  warning — the fields still feed the tower, so the elevator will take you down but DJ will refuse
  until a **majority of the ten fields (6 of 10, RULING 2026-07-06)** are free (dovetails with DJ's
  shipped `monopoly_holds` refusal — shipped value is 4, `company_hq` owns the 4→6 edit).
- **Rewards:** Company Keycard item + `hq_keycard` tag; a mid CD tip (`payout {amount:1000}`); the frag_7
  beat lands in the same visit; **no combat.**
- **Resolution:** she walks off shift for good. The main quest HUD (already in `render.mcfunction`)
  swaps to *"▶ Liberate wheat fields, then raid HQ"* / *"▶ Raid Company HQ [1590 51 1028]"* (shipped
  HUD text still prints the basement-core coord + the 4-field threshold; `company_hq` §8 owns
  reconciling both). **This is the only marquee; the quests below are optional colour + the noble event.**

### 5.2 "Exchange Rate" — the currency peak, made a quest
- **Giver:** `cyber_exchange_teller` — *Verified Value Teller* (civilian; a CobbleDollar money-changer
  at a downtown kiosk, sweating the peak index).
- **Hook:** the exchange board is nonsense at idx 56; he begs you to *"re-verify"* 3 nether-star reserve
  tags posted around downtown — the one job the Company is supposed to do and has stopped doing.
- **Steps:** interact with 3 reserve-tag props (`cyber_reserve_1/2/3`); each is **re-signed under a new
  name over a sanded-off older signature** (the scrubbing artifact, canon LORE_BIBLE §9). Count via
  scratch holder + band tag `ci_reserves` (mirror the Hua Zhan `hz_price_check` counter pattern).
- **Gate:** `badges_gte_7`; turn-in gated on all three reserve tags.
- **Rewards:** `payout {amount:900}`; unlocks a persistent **town exchange board** prop that reads the
  live `cd_instability` (this is the "town exchange board" the LORE_BIBLE §9 already promises for the
  fine-grained index). **On-theme nudge:** the teller, defeated by the numbers, mutters that "someone is
  offering an alternative south of here" — soft pointer toward field liberation / the wheat pitch.
- **Resolution:** he stops trusting the Company's signatures — a civilian feeling the plot in his wallet
  without ever knowing the founder's face.

### 5.3 "Off the Clock" — burned-out grunts (corporate-dread comedy)
- **Givers:** `cyber_noodle_grunts` — two **Off-Duty Contractors** at a neon noodle bar who **refuse to
  fight** ("we're on break, union hours, take it up with scheduling") and gossip that the *Acting* CEO is
  "a seat-warmer" and there's "a face on the memo the old-timers won't shut up about."
- **Hook / steps:** pure dialog; an optional micro-battle unlocks only if the player picks the *"The
  Acting CEO is a joke"* line (they defend DJ out of reflex, not loyalty). One reused/new low villain
  team (`sq_cyber_offduty`, ~lv 54).
- **Gate:** `badges_gte_7`.
- **Rewards:** comedy + **HQ intel** (a line confirming the tower entrance and that DJ is *acting*, not
  real — seeding the DJ reveal); small CD (`payout {amount:600}`); optional-battle prize if fought.
- **Resolution:** they clock back in grumbling. Foreshadows the "some stand down" late-recognition tier
  and DJ-as-usurper without spoiling it.

### 5.4 "STORMFRONT" — the Zapdos noble defense event (RULING 2026-07-06)

**Archetype:** PROMPT-STARTED noble defense — the showrunner's canonical example is literally this
event (*"will you help us defend cyber city from zapdos"*). The noble/event framework (prompt-started
vs monument-triggered nobles, event Pokémon, post-PvP wild spawn) is owned by
**`16_legendaries_nobles.md`** — this section is the Cyber City instantiation; defer archetype-level
policy (respawn/KO rules, noble roster) to that doc.

- **Giver:** `cyber_grid_warden` — *Grid Warden Ohm* (PROPOSED name), municipal power authority, NOT
  Company. Substation yard, E downtown `~[1720 65 1150]` (PIP-verified in-polygon). Civilian
  quest-giver, no battle of her own.
- **The PROMPT (canonical per ruling 2):** a storm cell has parked itself over the eastern grid and
  it is not weather. She asks, on an explicit button: **"Will you help us defend Cyber City from
  Zapdos?"** — accept starts the event; decline closes politely and the offer stays available
  (re-openable; no punishment for walking away).
- **Gate:** `defeated_cyber_leader` (post-Volt — the event is tuned to the gym-7 window, cap 62).
  Side content: never blocks the keycard/HQ mainline.
- **Phase 1 — the PvP defense gauntlet (2–3 battles; ruling: beat the PvP part first).** Three
  storm-struck pylons, each held by a battle (design intent: rolling-blackout looters raiding the
  dark grid — "grid raiders"; framing PROPOSED, showrunner may prefer Company opportunists instead):
  | # | Trainer id | Format | Band (gym-7 window, cap 62) |
  |---|---|---|---|
  | 1 | `sq_cyber_storm_1` | SINGLES | 58–59 |
  | 2 | `sq_cyber_storm_2` | SINGLES | 59–60 |
  | 3 | `sq_cyber_storm_3` | **GEN_9_DOUBLES** (PROPOSED finale spice) | 60–61 |
  Count wins via scratch holder + band tag `ci_storm` (mirror the Hua Zhan `hz_price_check` /
  Exchange-Rate `ci_reserves` counter pattern).
- **Phase 2 — Zapdos spawns as a NORMAL WILD Pokémon (ruling 2).** All three pylons held → return to
  Ohm → she grounds the mast → **Zapdos lands at the transmission mast (`~[1720 ~80 1200]`,
  PROPOSED) as a real wild spawn — catch it or battle it, FULL hardcore-Nuzlocke stakes** (it can
  faint your party members for keeps; no scripted mercy). PROPOSED level **60** (usable under the
  post-Volt cap 62). Whether a KO'd/fled Zapdos is gone forever or re-offerable is an
  archetype-level call — defer to `16_legendaries_nobles.md`.
- **Spawn wiring — UNVERIFIED (jar-verify):** the intended command shape is
  `spawnpokemon zapdos level=60` at the mast. This grammar is NOT in `docs/ENGINE_FINDINGS.md`; the
  only in-repo precedent is a *comment* idiom in
  `function/sidequest/work_orders/fetch_success.mcfunction` (`spawnpokemon combee level=8
  gender=female`). Decompile the pinned Cobblemon jar to confirm the command, its args, and any
  legendary/persistence/despawn flags **before** wiring — do not ship on the comment alone.
- **Rewards:** gauntlet completion pays `payout {amount:1200}` (PROPOSED) whether or not the catch
  lands — **the catch is the real prize.** No training pack (noble events are their own reward;
  keeps the training-pack economy clean — confirm, Open Q).
- **Resolution:** the grid re-lights (flavor line only — no world edits); Ohm's dialog flips to an
  after-state. One-shot latch tag `storm_defended`.

**RCT / registry (mostly DONE):**
- ✅ `trainers/gyms/cyber_city.json` — full ladder exists. **Action: retune the leader to the 6-mon
  team in §4** (add Lanturn 56, Rotom-Wash 57; keep ace Raichu 58). Never touch the cap ladder.
- ✅ `data/rctmod/trainers/cyber_leader.json`, `cyber_apprentice.json`, `.../mobs/trainers/single/cyber_leader.json`,
  `.../single/cyber_apprentice.json` exist. **Action:** add matching `rctmod/trainers/` team files for
  any leader mon you add, plus the two new side-quest trainers below.
- 🧱 **New (side quests):** `sq_cyber_offduty` (one villain-lite team ~lv 54, `battle.type:villain_grunt`,
  optional). The exchange teller, comms tech, reserve/billboard props, and the access admin are
  **non-battlers** (no RCT team).
- 🧱 **New (Stormfront, §5.4):** `sq_cyber_storm_1` (58–59 singles), `sq_cyber_storm_2` (59–60
  singles), `sq_cyber_storm_3` (60–61, PROPOSED doubles) — electric/flying "grid raider" flavor
  teams; TrainerConfig + rctmod team files + graph nodes, same shape as the other side-quest
  trainers. The grid warden is a **non-battler**.

**Battle formats vs the cap ladder (entry cap 56):** trainers 48-49 (under cap, standard gym fodder),
jr 52, **apprentice DOUBLE 54-55**, leader 56-58 (ace = 58 = cap+2), **Stormfront gauntlet 58-61
(post-Volt content — played in the cap-62 window)**. All consistent with the shipped file — no
ladder edits.

---

## 7. Economy & rewards

| Source | Payout / effect | Wiring |
|--------|-----------------|--------|
| Volt (leader) | **4300 CD** prize; `frag_7`; `shop badge_7`; `gym_destabilize` (**idx → 56 peak**) | already in `cyber_city.json` leader `rewards[]` |
| Shop tier | `badge_7` = **peak prices (~+28%)**, new category unlocks | LORE_BIBLE §9; `scripts/shop_tiers` |
| Access Control (leak) | `payout {amount:1000}` + Company Keycard item + `hq_keycard` | new (§8) |
| Exchange Rate | `payout {amount:900}` + exchange-board prop | new |
| Off the Clock | `payout {amount:600}` (+ optional battle prize) | new |
| Stormfront (§5.4) | 3× gauntlet battle prizes + `payout {amount:1200}` completion (PROPOSED); **the Zapdos catch is the real prize** | new |
| **CD sink** | badge_7 peak-price Pokémart is the sink; the leak beat pushes the player toward **field
liberation** (nearest = **Fenceline Acres / `farm_6`**, just S of town at z≈1730) to hit the
**6-of-10 majority raid gate** (RULING 2026-07-06) and trigger relief tiers | `install.json` (Fenceline `farm_6`), `wheat_war_farms` area |

**Instability is at its worst here on purpose** — every payout line shows the skew, the exchange board
reads 56, propaganda glitches. The *only* way the number comes down is DJ (idx → 25), which is gated
behind starving the fields. The economy pressure IS the call to the raid.

---

## 8. Implementation notes / FUTURE-ME HOOKS

**Files to CREATE** (author under `dialog-src/`, then compile):
- `dialog-src/characters/cyber/` (new group folder, matching `hua_zhan/` / `takehara/` convention):
  - `cyber_trainer_1.json` … `cyber_trainer_4.json`, `cyber_jr_apprentice.json`, `cyber_apprentice.json`
    (interior bodies for the shipped RCT ids; copy `station_*`/`hua_zhan_trainer_*` bodies; each
    `battle.trainer` = the matching config id, `battle.type:"gym_trainer"`, doubles one uses
    `format:"GEN_9_DOUBLES"`). **Placement = the config coords in §3, `skin.type:custom`, NO `uuid`
    → spawns once per world.**
  - `cyber_access_admin.json` — the leak (see below).
  - `cyber_exchange_teller.json`, `cyber_comms_tech.json`, `cyber_noodle_grunts.json`, prop bodies
    `cyber_board_1/2/3.json`, `cyber_reserve_1/2/3.json` (disguised-prop pattern — copy `station_moss`:
    `role:"civilian"`, `movement.objective:"ambient_stationary_look"`, `placement` + no `uuid`).
  - **Stormfront (§5.4):** `cyber_grid_warden.json` (prompt-started giver — the accept/decline
    buttons ARE the noble-prompt archetype, cross-ref `16_legendaries_nobles.md`),
    `sq_cyber_storm_1/2/3.json` (battler bodies at the pylons), plus a completion function
    `function/sidequest/storm/…` for the counter + the Zapdos spawn (spawn command **UNVERIFIED
    (jar-verify)** — see §5.4; do not wire until the Cobblemon jar confirms the grammar).
  - **Note:** shipped `cyber_leader.json` + `cyber_guide.json` currently live in `characters/gym/`
    (alongside `station_moss`). Leave them or move into `characters/cyber/` — pick one and be consistent.
- `dialog-src/dialog/` — one tree per non-inline NPC (or use `dialog_inline` for one-offs like the admin,
  copying `villain_admin.json`'s inline recognition ladder).
- **Keycard item/gift:** `src/main/resources/data/cobblemon_initiative/loot_table/npc_gift/hq_keycard.json`
  (mirror `npc_gift/transition_order`). Decide the item base (see Open Q3) — a renamed
  `minecraft:paper`/`iron_nugget` "Company Keycard" is cheapest.
- **Side-quest HUD lines:** append `q.side_*` blocks to `function/quest/render.mcfunction` (copy the
  Hua Zhan `q.side_prices` / `q.side_survey` blocks: reset holder, set slot, name only while
  started-and-not-done). Keycard/raid main line ALREADY exists (L34-38) — **do not duplicate it.**
- **Counter for Exchange Rate:** `function/sidequest/exchange/set_reserves.mcfunction` (copy
  `sidequest/price_check/set_prices`), scratch `#reserves quest_hud`, band-render via storage macro.

**The leak NPC (`cyber_access_admin`) — exact shape:**
- `role:"quest_giver"` (or `civilian`), `recognition_tier:"late"`, `act:"2"`, **no `trainer` block**.
- Inline `STANDARD` dialog, entry `reveal` `priority:20` `gate:{ "defeated":"cyber_leader" }`; a
  `default` fallback for pre-Volt ("come back when you have made your name in the gym").
- Reveal button actions, in order:
  1. `{do:"give"}` → `loot give @s loot cobblemon_initiative:npc_gift/hq_keycard` (as player)
  2. `{do:"command","cmd":"tag @s add hq_keycard","as_player":true}`
  3. `{do:"command","cmd":"function cobblemon_initiative:economy/payout {amount:1000}","as_player":true}`
  4. `{do:"announce","text":"The Company tower fronts the north plaza. The lobby is public. The card is for the elevator that goes down. Starve the fields first — while most of them still feed the tower, the man in your chair will not even see you.","as":"chat","color":"gold"}`
  5. `{do:"close"}`
- **Recognition text is the payoff** — she took the portrait down herself; write it warm, guilty,
  relieved. She names the *charter* and the *face*, **never** "Founder."

**Pipeline (run in order after authoring):**
`scripts/content_compile` → `scripts/generate_granary_tiers` → `scripts/update_preset_index` →
`scripts/generate_npc_function`. Confirm `npc/preset_map.json` + `function/update_npc_presets.mcfunction`
regen and the placement functions spawn the no-`uuid` bodies once.

**GOTCHAS (verified against `band_tags.mcfunction` / ENGINE_FINDINGS):**
- **`hq_keycard` is NOT a mechanical gate for DJ** — it is the **basement-elevator door check**
  (ruling 4 geometry; `company_hq`'s `hq_lobby_guard` owns the check). DJ's battle gate is
  `fields_liberated >= 4` as shipped (`acting_ceo_dj.json`, `render.mcfunction`) — **raised to
  >= 6 of 10 by RULING 2026-07-06; `company_hq` §8 owns those edits** (incl. the new
  `fields_liberated_gte_6` band-tag pair if a dialog ever gates on it). Do not wire DJ's entry to
  `hq_keycard`.
- If any dialog gates `not_tag:"hq_keycard"`, the compiler must add **`no_hq_keycard`** to
  `band_tags.mcfunction` — Easy NPC 6.25 **ignores NOT_EQUALS** (`contains()` only), so every
  "does-not-have" gate rides `EQUALS no_<X>`.
- **Macro text safety:** frag/economy/announce/onwin text = **no `"`**, and no `'` / `%` in any
  `cmd`/`win_line`/`on_win`. The keycard `announce` above is already clean.
- `onwin` tokens are **winners-first** (`1:` = player won) — only relevant if you add the optional
  Off-the-Clock battle.
- Recognition at gym 7 = **late** automatically (`badges_gte_7`); gate the leak on `defeated_cyber_leader`
  so it fires post-Volt, not on badge count alone.
- **Pattern to copy:** interior trainers → `hua_zhan_trainer_*` + `station_moss`; the leak's inline
  recognition ladder → `villain_admin.json` (Regional Manager Shade) / `villain_site_manager.json`;
  the doubles body → `hua_zhan_apprentice`; gift/loot → `villain_site_manager` `transition_order`.

---

## 9. Dependencies & open questions

**Depends on (other area keys):**
- **`company_hq`** — TIGHTEST coupling. They own the skyscraper interior (lobby, basement stack,
  flavor floors, penthouse), the `hq_lobby_guard` **basement-elevator door check on `hq_keycard`**,
  and every villain body inside (Shade/Vex/Noir/DJ). I own the **leak beat + the lobby pointer + the
  keycard tag/item.** They also own the zone-discrepancy confirm (their §3a/Q1b) and the 4→6 gate edits.
- **`wheat_war_farms`** — the raid's real gate is **`fields_liberated >= 6` of 10** (RULING
  2026-07-06; shipped files still say 4); nearest field is **Fenceline Acres / `farm_6`** (S of
  town). The leak's "starve it first" line assumes their liberation loop exists.
- **`legendaries_nobles`** (`16_legendaries_nobles.md`) — Stormfront (§5.4) is an instantiation of
  their prompt-started noble-defense archetype; respawn/KO policy and noble-roster canon live there.
- **`mainline_spine`** — frag_7, `cd_instability` peak, `quest/render.mcfunction` main line, shop tiers.
- **`gym_system_pvp_doubles`** — the shared ladder/DOUBLE pattern + leader `frag/shop/destabilize` reward
  hooks this gym already uses.
- **`kalahar_reach`** (prior gym; `defeated_kalahar_leader` gates the cyber ladder) and **`ryujin_keep`**
  (next gym; post-HQ frag_8 assumes DJ has fallen).

**RESOLVED by showrunner ruling (2026-07-06) — removed from the open list:**
- ~~Is `hq_keycard` a real entry gate or pure flavor?~~ → **Real door: the card opens the skyscraper's
  basement elevator** (ruling 4; mechanics PROPOSED, `company_hq` owns the guard/door check).
- ~~Raid gate count~~ → **6 of 10 fields (majority)**, was 4 (ruling 3; edits owned by `company_hq`).
- The HQ entrance is the **street-level lobby of a skyscraper in Cyber City** (ruling 4) — the
  *pointer* target is settled; only the exact door coordinate still needs the builder.

**Decisions the showrunner must make:**
1. **Leader ace level — 58 or 64?** Balance rule + shipped file = **58** (entry-cap 56 + 2). Brief header
   = 64. Recommend **58**; if 64 is wanted, the whole Cyber ladder must climb ~6 levels and the "fought
   underleveled" identity weakens. *(Recommend hold 58.)*
2. **Expand the shipped 4-mon leader to the 6-mon marquee team?** (Recommend yes for a gym-7 wall.)
3. **Keycard item base** — renamed `minecraft:paper`/`iron_nugget`, a CobbleDollars-themed item, or
   flavor-only (tag + no item)? Affects the loot table.
4. **Exact placement** of the leak admin doorstep (`[1545, 66, 1072]` PROPOSED) and the tower's
   street-lobby doors (needs builder + `company_hq`; ties into their zone-discrepancy confirm Q1b).
5. **Do the side quests (Exchange Rate / Off the Clock) ship for the Cyber pass, or defer** as post-launch
   colour? The keycard marquee is the only hard requirement for the Act 1 → Act 2 flip.
6. **Stormfront (§5.4) tuning:** Zapdos level (PROPOSED 60 vs pushing the cap at 62); gauntlet
   finale as DOUBLES or all-singles; "grid raiders" vs Company-opportunists framing; completion
   payout 1200; training pack (recommend none); and the KO'd/fled-Zapdos policy (defer to
   `16_legendaries_nobles.md`). Spawn command itself is **UNVERIFIED (jar-verify)** — blocker for
   wiring, not for the decision.
