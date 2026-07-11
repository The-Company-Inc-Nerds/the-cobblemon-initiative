# 00 — Master Backlog: The Cobblemon Initiative Quest Set

> **Ties together the 14 unit docs in `docs/QUESTS/`.** This is the sequencing + integration
> layer over the individual plans. It does NOT re-author any quest — it audits the tag graph,
> consolidates the register additions (and resolves the slot collisions the units created
> independently), sanity-checks the recognition/economy arcs across the whole run, maps the
> cross-unit dependencies, and orders the build into `content_compile`-sized batches.
>
> **Scope of the covered units:** gym towns 3–10 (`01_mystic_marsh` → `08_scorchspire`), the
> HQ raid (`10_hq_raid`), the Board hunt + Founder mirror (`11_board_founder`), the route
> backbone R3–R16 (`12_routes_backbone`), noble gating (`13_nobles_gating`), the five elemental
> shrines (`14_shrines`), and the Battle Frontier (`15_battle_frontier`). Towns 1–2 (Sango,
> Takehara, Hua Zhan) and R1/R2 are already shipped and are the template these mimic.

---

## 1. Overview

This backlog covers **everything the unit docs add on top of the shipped Act-1-beat-2 baseline**:
the town-side layer around gyms 3–10, the Act-2 climax (HQ → Acting CEO DJ), the Act-3 identity
payoff (Board hunt → the Founder mirror), the connective route tissue and — most load-bearing —
the **field-guard → route mapping** that unblocks the wheat-war liberation campaign, the seven
noble encounters' gates/givers, the five elemental shrine trials, and the post-Royal-League
Battle Frontier.

**How it slots into the pipeline.** Every unit authors the same three artifact classes and feeds
the same compiler, exactly as towns 1–2 already do:

```
dialog-src/characters/<area>/<id>.json   (character: who + battle/trade/loot/service + placement)
dialog-src/dialog/<id>.json              (dialog tree; STANDARD gated+prioritised, BASIC/CUSTOM props)
dialog-src/registers/quest_targets.json  (the sidebar/waypoint holder + stages — ONE shared file)
data/cobblemon_initiative/function/**    (turn-in count-checks, payouts, ceremonies, receipts)
data/rctmod/trainers/**                  (RCT battle teams for any {do:battle})
   → scripts/content_compile             (lowers dialog-src → *.npc.snbt + quest_waypoints.json + band tags)
   → scripts/generate_granary_tiers      (only if trade tiers touched)
   → scripts/update_preset_index → scripts/generate_npc_function → gradle build
```

**How to implement fast (the one-liner).** For each unit: drop its character + dialog JSON blocks
verbatim into `dialog-src/`, append its `quest_targets.json` holders (using the **de-collided slots
in §3 of this doc, NOT the per-unit-proposed slots**), add the listed `function/**` files, add the
RCT team files, then run `content_compile` → `update_preset_index` → `generate_npc_function`. The
units are copy-paste-compile by design; the only integration work this backlog exists to catch is
the shared-namespace collisions (register slots, `q.side_offrec`-style holder-name clashes) and the
cross-unit dependency ordering.

**Totals.** ~**44 tracked quests** across the units (plus rumor hubs and untracked spotters/flavor),
authored over roughly **185–195 new + reused NPC bodies** (see §8 count breakdown). Cross-doc facts
verified against the live repo where load-bearing (HQ gate = **4** fields, live `render.mcfunction`;
`liberation/load` names map today seeds **only** `farm_1`; character area folders `mystic/…/frontier`
are all genuinely new).

---

## 2. Tag / flag graph

Master table of every NEW tag and scoreboard the units introduce, plus the load-bearing reused
primitives they gate on. `set by` names the function/dialog/engine that writes it; `gated by` names
who reads it. **DEAD-GATE / DEAD-SET flags are called out inline.**

### 2.1 Scoreboards (new)

| score | unit | set by | read by | notes |
|---|---|---|---|---|
| `ci_mm_reads` (dummy) | 01 mystic | `exchange_board/price_read` | witness-pay threshold | needs its own `add … dummy` in a `load` + `#load` tag |
| `frontier_passes` (dummy) | 15 frontier | `frontier/register` (+1 free), `frontier/buy_pass` | flavor sink; future set-mode gate | **currently a DEAD-SET soft counter** — nothing consumes a pass (open Q: hard-gate the battle button on it). Harmless but note it. |
| `#manifests` (`quest_hud`, `#`-holder) | 03 gaviota | `sidequest/manifest/tick` | Kaito turn-in gate + dynamic sidebar | render scratch, per Gaviota manifest audit |
| `#halls` (`quest_hud`, `#`-holder) | 15 frontier | `frontier/hall_cleared` | `$(halls)/7` sidebar macro | render scratch |
| `#shrines` (`quest_hud`, `#`-holder) | 14 shrines | sidebar render tick (counts 5 leader tags) | capstone at-least-one stage | render scratch; **must be added to the render tick** (mirror `#board`) |
| `#rumor` / `#rumor_hit` (`quest_hud`) | 03 gaviota | `sidequest/rumors/coralie` | transient rumor roll | clone of `rumors/lila` scratch |
| `ci_papers_held`, `ci_sq_scratch` | reused | shipped (`museum/load`) | all count-check turn-ins (marigold/deepcore/rui/tempering/the_hand/scales) | **DO NOT re-declare** — every fetch reuses the shipped `ci_sq_scratch` |

Reused, never re-declared: `memory_fragment`, `cd_instability`, `fields_liberated`, `field_freed`
(holder-per-farm), `quest_hud`, `dex_caught`.

### 2.2 Field-liberation tags (the Act1→Act2 spine)

| tag | unit | set by | gated by | notes |
|---|---|---|---|---|
| `farm_2_free` … (via `field_freed` holder `farm_N`) | 01/03/04/12 | `liberation/free_field_apply {field:farm_N}` | field-guard `liberated` entries, town rumor gates, field quest lines | `field_freed` is a **scoreboard**, not a tag — dialog gates on it must use the `score` form OR the mirror tag below |
| `field_2_liberated` … `field_10_liberated` | 12 routes | **`liberation/mirror_field_tags`** (NEW tick fn) | route "talks-back" spotter echo entries | **This is the fix for the scoreboard/tag mismatch:** the mirror tick reads the `field_freed` scoreboard each tick and writes a player TAG dialog gates can read. Ship this one function or every route echo is a dead gate. |
| `wheat_war_active` | shipped | first liberation (`free_field_apply`) | `q.side_wheat`, many town back-echoes, field quest activation | reused |
| `wheat_trader_suspicious` / `_hostile` | shipped | `wheat_trader/tick` (off `fields_liberated`) | all wheat-trader recognition tiers (mystic/deepcore/gaviota/kalahar) | reused; the ambush trigger arms at `hostile` (≥4 fields) |
| `defeated_wheat_ambush_kalahar` | 04 kalahar | Kalahar caravan doubles win | ambush already-beaten | **SPLIT tag** — Kalahar gets its own so beating Hua Zhan's `wheat_trader_1` does not pre-clear it. Requires repointing shipped `wheat_trader_2.json` |

### 2.3 Per-unit story tags (representative — full lists live in each doc's §5)

The units each introduce a self-contained tag family; all follow the same shape (`*_active` →
`*_done`, `defeated_*`, `declined_*`) and all auto-derive their `no_*` inverses via
`content_compile`'s `band_tags` regen. Key families:

| unit | tag family (abridged) | notable coordination flag |
|---|---|---|
| 01 mystic | `met_mm_nurse`, `mm_charms_done`, `mm_board_done`, `defeated_mm_field_guard`, `farm_2_free` | `heard_wheat_pitch` (reused, feeds `q.side_wheat`) |
| 02 deepcore | `deepcore_restructure_started/…_done`, `deepcore_ledger_seen`, `iron_ladder_active/…_cleared`, `seen_deep_office`, `took_deep_manifest` | `defeated_deepcore_trainer_2` (reused shipped gate) |
| 03 gaviota | `bosun_net_done`, `odessa_crate_started/…_recovered`, `gaviota_manifest_check_active/…_filed`, `defeated_gaviota_wheat_sea`, `farm_3` | `farm_3` liberation via Sable ambush win |
| 04 kalahar | `dry_season_active/…_done`, `boundary_stones_active/…_done`, `seal_stone_1/2/3`, `oasis_pump_off`, `oasis_restored`, `defeated_wheat_ambush_kalahar` | `kalahar_ambush_cleared` (HUD flavor only) |
| 05 cyber | `ci_reserves_active/…_done`, `ci_signal_active/…_done`, `ci_file_active/…_done`, `hq_pointer_done` | reads `hq_keycard` (owned by 10) |
| 06 ryujin | `ryujin_defector_met`, `ryujin_ledger_taken`, `ryujin_charter_read`, `ryujin_heritage_settled`, `ryujin_mail_done`, `ryujin_oath_told` | gates whole town on `defeated_villain_boss` |
| 07 nifl | `nifl_core_1/2`, `nifl_archive_open/…_read`, `nifl_frostgate_clear`, `nifl_warrant_stood_down`, `nifl_lantern_1..4`, `nifl_lanterns_done` | gates whole town on `defeated_ryujin_leader` |
| 08 scorchspire | `forge_order_1/2/3`, `forge_order_agent_clear/…_done`, `temper_blade_done`, `the_hand_started/…_plate/…_done`, `severance_met`, `asset_recovery_clear`, `retirement_memo_taken`, **`scrubbing_artifact_plate`**, **`scrubbing_artifact_memo`** | the two `scrubbing_artifact_*` breadcrumbs claim to "feed the Board/Founder reveal chain" — **see DEAD-SET note below** |
| 10 hq | `hq_raid_active`, `hq_raid_seen`, `hq_floor_1/2/3` | reads `fields_liberated ≥ 4` + `badges ≥ 7` |
| 11 board | `read_signature_1..4`, `read_all_signatures` (optional) | reuses shipped `defeated_board_*`, `#board`, `company_overthrown` |
| 12 routes | `field_2..10_liberated` (mirror), per-route `defeated_r*_spotter/trainer/route_agent` | REF field-guard tags authored by `wheat_war_farms` |
| 13 nobles | `defeated_noble_{mew,articuno,moltres,zapdos,kyogre,groudon,rayquaza}`, `heard_mew_rumor` (optional) | reads shrine-leader tags + `mem_gte_5/7/8/10` band tags |
| 14 shrines | `defeated_<type>_shrine_cultist_1..4`, `<type>_shrine_trial_clear`, `<type>_shrine_complete`, `shrine_frag_seen`, `five_keepers_paid`, `#shrines` | leader tags are the **noble handshake flag** shared with 13 |
| 15 frontier | `frontier_registered`, `frontier_plaque_read`, `defeated_<facility>_challenger_1/2`, `frontier_<facility>_cleared` (×7), `frontier_all_cleared` | brain defeat tags == shipped `achievementOnDefeat` |

### 2.4 DEAD-GATE / DEAD-SET / lowering-risk flags (the audit)

1. **`field_freed` is a scoreboard, not a tag — every naive `tag: field_N_free` gate is DEAD.**
   Docs 01/03 gate rumor/field lines on `farm_2_free` / `farm_3` as if they were tags. They are
   `field_freed` scoreboard entries. **FIX (already specced in 12 routes):** ship
   `liberation/mirror_field_tags` and gate on the mirror `field_N_liberated` tags, OR use the
   `{score:{objective:"field_freed", holder:"farm_N", …}}` form. Lock ONE approach at synthesis.

2. **`defeated_noble_<id>` may be scoreboard-only (13 nobles Open Q1).** The noble JSON writes a
   `rewards.storyFlag` scoreboard; every dialog after-entry and sidebar `not_tags` reads it as a
   TAG. If it does not also lower to a tag, all seven noble after-entries + sidebar done-states are
   **DEAD GATES.** FIX: a one-line tick `execute as @a[scores={defeated_noble_<id>=1..}] run tag @s
   add defeated_noble_<id>`, or switch every gate to the `score` form. Decide uniformly.

3. **`scrubbing_artifact_plate` / `scrubbing_artifact_memo` are SET but never READ (08 scorchspire).**
   The Scorchspire SQ3/SQ4 rewards set these "to feed the Board/Founder reveal chain," but
   `11_board_founder` reads none of them — its `reveal/board_fell` recount keys only on
   `defeated_board_*`. **Either wire them** (e.g. an optional Founder pre-battle `any_tags` line that
   acknowledges the player assembled the cover-up) **or drop the two `tag @s add` lines.** Currently
   dead sets. Low-cost to keep as future hooks; flag so they are a deliberate choice.

4. **`frontier_passes` is SET but never CONSUMED (15 frontier, Open Q7).** Passes are bought/granted
   but no battle button decrements or gates on them. Dead soft-counter until the hard-gate ruling.

5. **`hq_raid_active` is never cleared (10 hq, Open Q4).** Harmless — the side holder also requires
   `not defeated_villain_boss`, so it self-moots. Leave, or tidy in `hq_stabilize`.

6. **Numeric gates all lower to BAND TAGS** (`badges_gte_N`, `cd_instability_gte_N`,
   `fields_liberated_gte_N`, `recognition:early/mid/late`, `mem_gte_N`, `dex_gte_N`). These are
   compiler-maintained by the `band_tags` tick — confirmed pattern, no dead risk, but every unit
   assumes the tick runs. The nobles doc uses `mem_gte_5/7/8/10` and the schema uses `badges_gte_N`
   for the SAME concept (`memory_fragment` count) — **reconcile the band-tag name** (`mem_gte_N` vs
   `badges_gte_N`) so both spellings resolve to one maintained tag.

---

## 3. Consolidated quest_targets additions

**This is the load-bearing integration deliverable. Every unit chose slots independently and they
COLLIDE massively.** The live register occupies slots **57–81 + 100** (contiguous, per Gaviota's
verified audit). Below-57 (44–56) and above-81 (82–99) are the free bands. Multiple units all
grabbed the same low numbers (50–56) because they audited in isolation.

### 3.1 The collision matrix (as proposed by the units — DO NOT ship as-is)

| slot | proposed by (unit → holder) |
|---|---|
| 50 | 11 board (`q.side_signatures`), 13 nobles (`q.side_wisp`) |
| 51 | 11 board (`q.side_board_story`), 14 shrines (`q.side_shrines_capstone`) |
| 52 | 05 cyber (`q.side_door`), 11 board (`q.side_board_position`), 14 shrines (`q.side_shrine_fairy`) |
| 53 | 02 deepcore (`q.side_office`), 05 cyber (`q.side_offrecords`), 11 board (`q.side_board_ledger`), 14 shrines (`q.side_shrine_ground`) |
| 54 | 02 deepcore (`q.side_rate`), 05 cyber (`q.side_signal`), 11 board (`q.side_board_vote`), 14 shrines (`q.side_shrine_dragon`) |
| 55 | 02 deepcore (`q.side_ladder`), 04 kalahar (`q.side_kalahar_water`), 05 cyber (`q.side_exchange`), 14 shrines (`q.side_shrine_ice`) |
| 56 | 01 mystic (`q.side_wisps`), 02 deepcore (`q.side_reserve`), 04 kalahar (`q.side_kalahar_stones`), 07 nifl (`q.side_lanterns`), 14 shrines (`q.side_shrine_fire`) |
| 57 | 07 nifl (`q.side_frostgate`) — **COLLIDES WITH SHIPPED `q.side_clinic` (57)** |
| 58 | 07 nifl (`q.side_archive`) — **COLLIDES WITH SHIPPED `q.side_manifest` (58)** |
| 64–72 | 12 routes (`q.side_field_r3..r16`) — **ALL COLLIDE WITH SHIPPED `q.side_sprint/watch/…/classic` (64–72)** |
| 82 | 08 scorchspire (`q.side_forgeorder`), 10 hq (`q.side_raid`), 15 frontier (`q.side_frontier`) |
| 83 | 08 scorchspire (`q.side_tempering`), 15 frontier (`q.side_frontier_hall`) |
| 84 | 08 scorchspire (`q.side_thehand`), 15 frontier (`q.side_frontier_done`) |
| 85 | 08 scorchspire (`q.side_retirement`), 15 frontier (`q.side_frontier_plaque`) |

> **Whether a slot COLLISION is fatal depends on the render engine.** `render.mcfunction` is
> last-write-wins top-to-bottom; two holders sharing a `slot` VALUE tie-order by holder name in the
> sidebar (the shipped register already shares 79 between `q.side_partner`/`q.side_green` and 72
> across `q.side_classic` deliberately). So two holders at the same slot is not a compile error —
> BUT `content_compile` may error on a *duplicate holder* and the units that reuse **shipped** slots
> 57/58/64–72 will render **on top of live Act-1 side quests**. The safe move is to spread everyone
> across the genuinely free 44–56 and 86–99 bands.

### 3.2 Recommended de-collided allocation

Assign by act order so the sidebar reads chronologically. Free bands: **44–56** (early/mid, 13
slots) and **86–99** (endgame, 14 slots). Keep the shipped 57–81 untouched.

**Act 1 / mid-game towns (gyms 3–7) → band 44–56 (13 slots for ~14 lines — tighten by cutting one
optional line, e.g. Deepcore `q.side_office` folds into `q.side_reserve`):**

| slot | holder | unit |
|---|---|---|
| 56 | `q.side_wisps` | 01 mystic |
| 55 | `q.side_verified` | 01 mystic |
| 54 | `q.side_mirebloom` | 01 mystic |
| 53 | `q.side_reserve` | 02 deepcore |
| 52 | `q.side_ladder` | 02 deepcore |
| 51 | `q.side_rate` | 02 deepcore |
| 50 | `q.side_office` | 02 deepcore |
| 49 | `q.side_kalahar_stones` | 04 kalahar |
| 48 | `q.side_kalahar_water` | 04 kalahar |
| 47 | `q.side_door` | 05 cyber |
| 46 | `q.side_offrecords` | 05 cyber |
| 45 | `q.side_signal` | 05 cyber |
| 44 | `q.side_exchange` | 05 cyber |

> Gaviota (03) correctly claimed **82–85** for its four lines (`q.side_freight/odessa/rui/westwind`)
> — those are in the endgame band; move them down into the mid band too (e.g. **86–89**) OR keep at
> 82–85 and push the endgame units up. Given the sheer count, the cleanest split is: **early/mid
> town lines 44–56, everything post-HQ 86–99.** Reassign Gaviota's four to **56-adjacent** vacancies
> after the mystic/deepcore block, or accept a second sub-band.

**Post-HQ / Act 3 / endgame → band 86–99 (14 slots):**

| slot | holder | unit |
|---|---|---|
| 99 | `q.side_frontier` | 15 frontier |
| 98 | `q.side_frontier_hall` | 15 frontier |
| 97 | `q.side_frontier_done` | 15 frontier |
| 96 | `q.side_frontier_plaque` | 15 frontier |
| 95 | `q.side_raid` | 10 hq |
| 94 | `q.side_forgeorder` | 08 scorchspire |
| 93 | `q.side_tempering` | 08 scorchspire |
| 92 | `q.side_thehand` | 08 scorchspire |
| 91 | `q.side_retirement` | 08 scorchspire |
| 90 | `q.side_archive` | 07 nifl |
| 89 | `q.side_frostgate` | 07 nifl |
| 88 | `q.side_lanterns` | 07 nifl |
| 87 | `q.side_clerk8`/`heritage`/`mail`/`oath` | 06 ryujin (4 lines — needs 4 slots) |
| 86 | `q.side_board_*` (4) + `q.side_signatures` | 11 board (5 lines) |

> **The endgame band (14 slots) is over-subscribed** — Ryujin alone wants 4, Board wants 5, Shrines
> want 6, Nobles want 7. **Total endgame/shrine/noble/board/frontier lines ≈ 33** against 14 free
> high slots + 13 free low slots = 27. **This is the single biggest integration finding: there are
> more proposed tracked side-quest lines (~44) than there are free register slots (~27).** Resolution
> options, in order of preference:
> 1. **Slot-share deliberately** (the shipped register already does — two holders per slot value,
>    tie-ordered by name). Nobles (7), shrines (6), board (5) are all mutually-exclusive-by-band or
>    rarely-simultaneous, so pairing them on shared slots is safe.
> 2. **Cut the "highlight-only, `target:null`" lines** that add no waypoint (`q.side_signatures`,
>    the shrine capstone at-least-one stage, Mew's optional line) — these are pure texture.
> 3. **Expand the sidebar slot range** in `render.mcfunction` if the HUD can show more lines.
>
> Nobles (13) and Shrines (14) already coordinated with each other (nobles took 44–50, shrines took
> 51–56) — but that pair COLLIDES with the mid-game-town block above. **Since nobles/shrines are
> mostly late-game, move them into the 86–99 band and slot-share with the act-3 lines.** Lock the
> final map at synthesis; the principle is: **early towns 44–56, everything post-HQ shares 86–99.**

### 3.3 Holder-NAME collisions (distinct from slot collisions — these ARE fatal)

- **`q.side_offrec` (shipped, Hua Zhan) vs `q.side_offrecords` (05 cyber).** Different holders,
  deliberately distinct ids — **OK**, confirmed by the Cyber doc. Do not "fix" them to match.
- **`q.side_manifest` (shipped slot 58, "Right of Way") vs Gaviota's original `q.side_manifest`.**
  Gaviota RENAMED its holder to `q.side_freight` to avoid this — **honor the rename**; never
  reintroduce a second `q.side_manifest`.
- No other holder-id collisions found; every unit namespaces its holders (`q.side_board_*`,
  `q.side_shrine_*`, `q.side_frontier_*`, `q.side_field_r*`, `q.side_kalahar_*`).

---

## 4. Recognition gradient audit

Cross-checked against LORE_BIBLE §4 / the canon gradient (grunts confused → management alarmed →
late stand-downs; civilians NEVER recognize; Mom never learns). One row per town/set-piece.

| Town / set-piece | Badge band | Recognition tier | Sample beat (who + what) | Canon check |
|---|---|---|---|---|
| g3 Mystic | 2–3 (early→mid boundary) | early→mid | Steward Halvard (field guard, `mid`): *you are supposed to be filed*. Clerk Osric = junior, no recognition. Nurse/child = civilian, none. | ✅ first management alarm; civilians clean |
| g4 Deepcore | 3+ (mid) | mid | Restructuring Officer Roderick (`grunt_recognition` mid): *you are supposed to be a closed file*. Wheat trader Corliss recognizes mid-trade at ≥2 fields. Deep-office portrait = oblique seed (no NPC names him). | ✅ mid alarm + trader-recognition beat; portrait circles, never closes |
| g5 Gaviota | 3–6 (mid) | mid | Sable (wheat handler, `hostile` ≥ fields): *there was never a founder, so you are no one*. Kaito (union): recognizes the CHARTER signature, not the man. Civilians (Coralie/Odessa/Mattias) none. | ✅ mid alarm + oblique charter recognition |
| g6 Kalahar | 5 (mid → late fan-out) | mid→late | Site Foreman (`mid`): *the memo said reroute AROUND you*. Warden Ossa: half-recognizes the founder's SEAL and DEFLECTS. Caravan (`hostile` ≥4): the loudest open founder-naming in the field (enemy memo-paranoia, not narration). | ✅ **loudest pre-Act-3 naming** — verify it reads as enemy paranoia not narration |
| g7 Cyber | 6→7 (mid→late, PEAK) | mid→late | Maren (whistleblower, STAND-DOWN): *I dusted that face off a portrait the size of a door* — refuses to fight. Ohmond half-knows the tower is scared of you. Civilians (teller/Rell) none. frag_7 town-echo. | ✅ **peak, pre-HQ**; first central stand-down; civilians clean |
| **HQ raid** | 7 (late) | late (the ladder) | Shade → Vex (*a line item I zeroed out myself*) → Noir (*I knew the gait on the security feed*) → DJ (*so the rumour walks. The founder…*). Lobby desk = civilian, none. | ✅ **canonical high point** of the gradient; DJ names "the founder" as the usurped man |
| g8 Ryujin | 7+ (late) | late | Nervous Clerk (defector STAND-DOWN): *I would sooner file my own resignation with my teeth*. Heritage envoy: alarm→panic. Rei (elder) reacts to the WORD "built," not the CEO. | ✅ hottest town; first clean on-screen stand-down |
| g9 Nifl | 7+ (late) | late | Records Officer Halden (STAND-DOWN option). Warrant Officer Dain (first on-screen defector, salutes + walks into snow). Civilians (Vetra/Halla/Sabra) none — lantern-4 whisper is the ICE recognizing, not a person. | ✅ late stand-downs; the cold "keeps the founder on file" without a civilian naming him |
| g10 Scorchspire | 7+ (late) | late | Kessler/Vance stand down or double down (*there was never a founder, so no one is standing there*). Old Marren (elder) knows the HAND off a plate and DELIBERATELY refuses to name it. | ✅ late; Marren is the sanctioned elder-edge case (knows the hand, not the name) |
| **Board hunt** | champion (late, terminal) | late | Madeline proud; Matt doubles down; Micah stands down intellectually; Lauren emotionally. Pointer civilians none — except Odei (liberated-field keeper) gets the wary *face on a memo* beat, never learns who. | ✅ terminal gradient; the one allowed wary-keeper beat is correctly scoped |
| **Founder mirror** | board_cleared (Act 3) | THE reveal | The nameplate resolves to the PLAYER'S live name only on defeat. | ✅ the name is spoken exactly once, at the mirror |

**Drift called out:**
- **No drift in the arc shape** — grunts→management→late stand-downs is continuous and monotonic;
  civilians are clean in every town; Mom is untouched by every unit (correct).
- **Kalahar's `≥4 fields` caravan "the founder" line and Ossa's seal line are the loudest pre-Act-3
  recognition** — both docs flag this as intentional (enemy memo-paranoia). Verify on stream it does
  not read as a narration reveal; it is the closest the run comes to naming him early, and it is a
  deliberate crescendo, not drift.
- **Deepcore's "deep office portrait" (SQ4) is a full un-scrubbed founder portrait pre-frag-7.** It
  is authored as set-dressing + a Dark-Urge stir with NO name — canon-legal (the world's *face*, not
  the world's *words*). Same device as Nifl's burned-face portrait and the Frontier plaque. Three
  such artifacts is a lot of "his face survives here" — deliberate (each is sealed/forgotten for a
  different reason), but confirm the density is wanted.
- **Nobles/shrines correctly sit OUTSIDE the Company gradient** — the shrine keepers' "old gravity /
  the land remembers you" is an ancient-presence tier, never Company recognition, never names the
  founder. No drift.

---

## 5. Economy-voice ladder

Table across the whole `cd_instability` arc, confirming glossy → nervous → corrupted is continuous.

| Band | `cd_instability` | Voice register | Carried by (units) | Gap check |
|---|---|---|---|---|
| Act 1 stable | 0–24 (idx settles ~24 end of g3) | Glossy corporate cheer (*Verified Trust, Verified Value*; *ten years of stability*) | 01 mystic (Osric default), shipped towns 1–2, 12 routes R3 (still glossy) | ✅ opens the ladder |
| Act 2 slipping (early) | 24→32 | Nervous reassurance, over-explaining "adjustments"; wheat traders offer an "alternative" | 01 mystic (Osric `nervous`/`over_explaining` gte 16/24), 02 deepcore (idx 32, *prices adjusting*), 12 routes R5 (*prices change under me*) | ✅ continuous with Act 1 |
| Act 2 slipping (deep) | 32→48 | Over-explaining hardens; traders may AMBUSH ≥4 fields; grain pushed hardest | 03 gaviota (idx 40, tariff gouging), 04 kalahar (idx 48, *legacy allocation under review*), 12 routes R7/R8 (*a coin that needs an apology*) | ✅ |
| Act 2 PEAK | 48→56 | Money "never felt this fake"; propaganda GLITCHES (§k); cover-up leaks in the clear | 05 cyber (idx 56, `economy#instability` gte 56, boards leaking *we told them the founder retired*), 12 routes R12 (frag_7 telegraph) | ✅ the crescendo |
| — HQ RAID → DJ falls — | clamps to **25** | **CURRENCY STABILIZED** title card (green, beacon) | 10 hq (`hq_stabilize`, shipped, unchanged) | ✅ the single biggest economy receipt |
| Post-HQ / late (corrupted) | 25 (held) | Propaganda CORRUPTED not glossy; cover-up leaks slowly; "the money went quiet" | 06 ryujin (ledger page *founder retired → never a founder*), 07 nifl (cold-storage cores, *the money remembered how to behave*), 08 scorchspire (aftermath — skeleton crew can't cancel its own dead order), 11 board (*flew Company colors till we tore them down*), 12 routes R13–R16 (late/stand-down) | ✅ continuous down from PEAK |
| Endgame (silent) | 25 (held) | Neutral / absent — the Company that sold propaganda is gone | 14 shrines (deliberate silence — the contrast is the feature), 15 frontier (*passes are cheap now, the money holds still*) | ✅ the silence is the intended coda |

**No gaps.** The ladder is continuous 0 → 24 → 32 → 40 → 48 → 56 → (clamp) 25 → held-25. Field
liberation pushes idx **down −6/field** at any point, easing CobbleDollar shop tiers (relief
catalogs) and worsening Granary wheat prices — this tug-of-war is felt in every Act-1/2 town and
narrated by the field-guard ceremonies. **One reconciliation:** every unit routes payouts through
the shipped skewed `economy/payout` (rate ≈ `100 − min(idx/4, 25)`), so the on-screen haircut is
consistent — but the **exact haircut % is quoted differently across docs** (Deepcore says ~92% at
idx 32; Nifl says 94% at idx 25; Scorchspire says "75% floor" at idx 25; Frontier says "75% floor"
at idx 25). The formula gives ~94% at idx 25 and ~92% at idx 32 — **the "75% floor" phrasings are
wrong** (they read the `min(idx/4,25)` cap as a flat 25% haircut). Cosmetic (the function is shipped
and correct), but the doc copy that quotes "75%" should be corrected to avoid a wrong receipt claim
on stream.

---

## 6. Cross-unit dependencies + back-echo web

### 6.1 Dependency list (what must exist / land first for a unit to pay off)

- **12 routes → EVERYTHING wheat-war.** The field-guard→route map is the spine: it pins each
  `farm_N` to a route so the six pre-HQ liberations land on the natural path, and it ships the
  `liberation/mirror_field_tags` tick that turns `field_freed` (scoreboard) into `field_N_liberated`
  (tag) that 01/03/04 and every route echo gate on. **Nothing that reads a liberated-field tag works
  until this ships.** Also depends on `wheat_war_farms` (the REF guard bodies) existing.
- **05 cyber → 10 hq.** Cyber's "Door Downtown" quest plants the HQ gate math (badges ≥7 AND fields
  ≥4), the KEYCARD hand-off, and the lobby pointer. HQ (10) OWNS the gate number (canon = **4**,
  verified in live `render.mcfunction`) and the keycard; Cyber must mirror, not re-litigate it.
- **10 hq → 06/07/08 (post-HQ towns).** Ryujin/Nifl/Scorchspire gate their whole town tone on
  `defeated_villain_boss` and back-echo CURRENCY STABILIZED. They are false if played before DJ
  falls — build/enable them after HQ.
- **Royal League (shipped) → 11 board + 13 nobles (Moltres) + 15 frontier + 14 (Fire shrine).** All
  gate on `royal_league_champion`. Board unlocks the Founder; Fire shrine + Moltres are post-league;
  the Frontier is post-league.
- **11 board → the Founder mirror.** Four `defeated_board_*` → `#board`=4 → Founder spawns. Board
  also REWRITES `reveal/board_fell` to venue-neutral beats (the shipped version narrates a seated
  boardroom that is false for the scattered hunt — **live design fork with roadmap `14`, Open Q1**).
- **14 shrines ⇄ 13 nobles (handshake).** Clearing a shrine sets `defeated_<type>_shrine_leader`,
  which is the SHARED flag: it unlocks the type-matched noble button (Ice→Articuno, Fire→Moltres,
  Dragon→Rayquaza) AND repoints the shrine crystal to a `noble start` launcher (the fix for the
  double-crystal / flat-L70 bug). These two units MUST be built together or the handshake dangles.
- **13 nobles → the `noble/` engine (shipped) + a small Java edit** (crystal repoint) + a per-noble
  arena-center/level retune (the JSONs ship `[0,0,0]` + flat L70 today).
- **08 scorchspire → 11 board** (weak): the `scrubbing_artifact_plate/memo` breadcrumbs are set for
  the Board/Founder chain but currently unread (§2.4 dead-set). If wired, Scorchspire must land first.
- **Every town → its own rumor hub.** House-style requirement; each town's nurse/greeter points at
  that town's not-done quests. Nobles/shrines fold their pointers into existing town hubs (13 adds a
  Zapdos line to Cyber's `cyber_nurse_rumor`; 14 adds shrine lines to the five gym guides).

### 6.2 Back-echo web (the world talks backward)

Each town references a prior beat, forming a chain the player feels: Mystic echoes Hua Zhan's wheat
naming (`wheat_named`) + Willowmire; Deepcore echoes Mystic's end-of-stable-feel + the courier fork;
Gaviota echoes Deepcore's price-adjust + Takehara's Genji rod (Rui) + Hua Zhan's greenhouse dispatch
(Westwind was named two towns earlier); Kalahar echoes the Takehara falls-redirection scam + the
running field campaign; Cyber echoes Hua Zhan price-checks + Kalahar; the HQ raid pays off every
`cd_instability` payout since gym 1; Ryujin/Nifl/Scorchspire all echo CURRENCY STABILIZED + the HQ
raid + their fragment; the Board echoes the whole liberated map + the shrine pilgrimage; the Frontier
echoes the League seat + stabilization. **The chain is unbroken** — every unit plants both a forward
hook (names the NEXT town/route/set-piece) and at least one back-echo, per house style.

### 6.3 Build-ordering implication

The dependency graph forces this spine: **routes/field-guards (unblocks liberation) → mid-town
liberations (01/03/04) → Cyber (plants HQ) → HQ (pays field campaign, stabilizes) → post-HQ towns
(06/07/08) → Royal League (shipped) → Board+Founder+Fire-shrine+Frontier (all post-league) →
shrines+nobles (handshake, mostly late).** See §7 for the batched version.

---

## 7. Prioritized build order

Batched against TODO.md's phases (Phase 1 map-authoring / Phase 2 wiring / Phase 3 balance). Each
batch is sized to ONE `content_compile` run and notes what it UNBLOCKS. **Highest-leverage /
most-blocking first.**

### BATCH 1 — Field-guard liberation wiring (12 routes §4 + `liberation/mirror_field_tags`) 🔓 THE BLOCKER
**Why first:** TODO.md names the field-guard liberation as *"the single biggest Act-1→Act-2
blocker"* — today only `farm_1` is wired, so `fields_liberated` maxes at 1 and the HQ gate (4), the
wheat-trader escalation (2/4), the relief shop tiers, and the granary ambush are ALL unreachable.
This batch lands **where in the sequence: first, before any town side-content that reads a field
tag.** Ship: the 12 field-guard/site-manager REF bodies positioned per route (§4 map), the
`farm_2..10:"NAME"` additions to `liberation/load` (only `farm_1` exists today), the
`liberation/mirror_field_tags` tick (scoreboard→tag), and the per-route field quest lines.
**Unblocks:** every liberation-dependent gate in 01/03/04/05 + the HQ raid + the wheat-trader
recognition arc + the relief economy. **Also ship in this batch:** the route spotters/flavor/road
agents (they are cheap and share the route files).

### BATCH 2 — Mid-game town side layer (01 mystic + 02 deepcore + 04 kalahar)
Drop three area folders (`mystic/`, `deepcore/`, `kalahar/`), their dialog + RCT teams + turn-in
functions, and the de-collided register lines (44–56 band). Mystic's `farm_2` guard and Kalahar's
Oasis pump / boundary stones / caravan-split-tag ride on Batch 1's liberation plumbing.
**Unblocks:** the felt Act-1→Act-2 economy turn (glossy→nervous) and three of the six pre-HQ fields.

### BATCH 3 — Gaviota + Cyber (03 + 05) — the HQ on-ramp
Gaviota (`farm_3` liberation, the maritime economy) + Cyber (the HQ pointer, the field-liberation
push toward `farm_6`/`farm_7`, frag_7 recognition PEAK). Cyber's "Door Downtown" plants the exact
gate text HQ owns. **Unblocks:** the HQ raid is now reachable (4 fields on the natural path) and
narratively set up.

### BATCH 4 — The HQ raid (10) — the Act-2 climax
Mostly EXTEND (add `placement` to the 4 shipped villains + `hq_receptionist` + floor-discovery
buttons). Small surface. **Unblocks:** `defeated_villain_boss` → CURRENCY STABILIZED → the entire
post-HQ town tone (06/07/08 gate on it) + the main-line advance to the Royal League.

### BATCH 5 — Post-HQ towns (06 ryujin + 07 nifl + 08 scorchspire)
Three area folders, the late-recognition stand-down beats, the corrupted-propaganda scrubbing
artifacts, the last field liberations (`farm_7/8/10` on R13/R14/R15). All gate on
`defeated_villain_boss`. **Unblocks:** the run reaches the Royal League gate with the identity plot
fully seeded (frag_8/9/10). Decide the `scrubbing_artifact_*` dead-set here (§2.4).

### BATCH 6 — Board + Founder (11) — the identity payoff
EXTEND the 5 shipped bosses (add `placement` + `spawn_gate`), 4 new pointer NPCs, REWRITE
`reveal/board_fell` to venue-neutral. **Resolve the scattered-vs-seated fork (Open Q1) FIRST** — it
decides whether the four Board dialogs need a seated-flavor rewrite. **Unblocks:** the Founder mirror
+ `company_overthrown` + the Ender-Dragon send-off.

### BATCH 7 — Shrines + Nobles (14 + 13) — build together (the handshake)
20 cultist bodies + 20 tiny dialogs + 5 leader-dialog edits + the crystal→noble repoint (Java) + the
5 noble givers + 7 arena-center/level retunes + the shared `defeated_<type>_shrine_leader` handshake.
These two MUST compile together. Mostly late-game, so their register lines slot-share the 86–99 band.
**Unblocks:** the seven noble catches + the five shrine trials + the Five-Keepers capstone.

### BATCH 8 — Battle Frontier (15) — the "after"
26 character files (24 over shipped RCT ids + 2 lore NPCs), 5 dialogs, the entry-fee economy, the
friendly-decline functions (broke → close, never fight). Post-Royal-League. Pure long-tail content;
last because it depends on nothing downstream. **Unblocks:** the endgame grind + the final lore
grace-note.

> **Batches 1–4 are the critical path** (they gate the whole midgame + Act 2). Batches 5–8 are
> parallelizable once Batch 4 (HQ) lands. Within TODO.md: Batch 1 = the Phase 2 "field guards" +
> Phase 1 §A "wheat fields" items; Batches 2–5 = Phase 1 map-authoring sprint + Phase 2 wiring;
> Batches 6–8 = Phase 3 optional/endgame + the noble/shrine TODO items already tracked.

---

## 8. Duplication + canon-violation audit

Everything a unit invented that collides with existing content or risks a canon/compile break.

### 8.1 Register slot / holder collisions (the big one — fully detailed in §3)
- **13 units independently chose overlapping slots 50–56 and 82–85; three units reused SHIPPED slots
  57/58/64–72.** Fix: adopt the §3.2 de-collided map (early 44–56, endgame 86–99, slot-share the
  over-subscribed late band). **~44 proposed tracked lines vs ~27 free slots → must slot-share or
  cut `target:null` texture lines.**
- Holder-NAME clashes: none fatal (Gaviota renamed `q.side_manifest`→`q.side_freight`; Cyber's
  `q.side_offrecords` is deliberately distinct from shipped `q.side_offrec`). **Do not "fix" the
  distinct-by-design ones.**

### 8.2 Canon roster / id fidelity — CLEAN
- Every unit uses the exact canon ids/names: leaders (`<town>_leader`, Cicada/Titania/…/Vulcan),
  villains (`villain_grunt_1..11`, `villain_admin`/`_2`/`_commander`, `villain_boss`=DJ, `board_*`
  §k names, `villain_final_boss`=Founder §k all run), royals (Aria/Marcus/Luna/Drake/Cynthia →
  `royal_league_champion` OVERRIDE tag), shrine leaders (Draconis/Aurora/Ignis/Terran/Glacius),
  frontier brains (Palmer/Noland/…/Selene verbatim from the shipped RCT file). **No invented
  leader/villain/royal ids found.**
- Side-quest villains correctly use BESPOKE ids (`sq_deepcore_assessor`, `sq_pump_officer`,
  `villain_route_agent_N`) rather than consuming a numbered `villain_grunt_N` roster slot — and
  route agents REFERENCE the existing `villain_grunt_3..9` teams without duplicating the placed
  building bodies. **Correct.** (Open Q1 in routes: whether to relocate building grunts vs new
  bodies — a design choice, not a violation.)
- **No-duplication checks passed:** `hunt_keeper_odei` ≠ shipped `granary_keeper` (Feng);
  `cyber_defector_maren` ≠ `cyber_access_admin`; `gaviota_nurse`/`nifl_nurse` are additive (no gym
  nurse exists); frontier ids are `frontier_`-prefixed to avoid collision.

### 8.3 Civilian recognition / Founder-name / Mom — CLEAN
- **No civilian recognizes the founder** in any unit. The three edge cases are all correctly scoped:
  Odei (liberated-field keeper, wary *face on a memo*, never learns), Old Marren (elder who knows the
  HAND not the name), Anselm (lore_keeper, keeps it to himself). Shrine keepers use an
  ancient-presence tier, not Company recognition.
- **The Founder is never named before Act 3** in any unit. Deepcore/Nifl/Frontier show his un-scrubbed
  PORTRAIT/plaque as set-dressing with no name; the reveal function prints the name (live selector)
  only at the mirror. **Correct.** DJ names "the founder" as the usurped man (canon — he's the Act-2
  boss, and it's recognition of the person, not the Founder-nameplate reveal).
- **Mom is untouched by every unit.** Correct.

### 8.4 Above-cap forced-battle / fairness-floor — CLEAN, with items to verify
- No unit ships a FORCED above-cap battle. Above-cap fights (Rui/Marigold/Ohmond/Ryujin-envoy/
  Tetsu wagers, the shrine leaders, the frontier brains, the nobles) are ALL opt-in with the stake
  printed + decline-able. Field guards + villain grunts are cap-legal (ace ≤ entry-cap+2) and opt-in
  by geography/dialog (no `ON_DISTANCE_VERY_CLOSE` forced touch except the wheat-trader ambush, which
  only arms at `hostile`/≥4 fields when the player is provably mid-campaign with a party).
- **Verify:** (a) the frontier's FRIENDLY decline functions must NOT fall through to the auto-gen
  `decline_fee` (which is a Company must-fight-on-broke) — 15 Open Q8 flags this; broke must → close,
  never fight. (b) The Kalahar caravan `villain_forced` doubles ambush — confirm the fairness guard
  (no whiteout on a caught-mon-less player) applies. (c) The Board `gauntlet_boss` fights are forced
  no-decline, but a starter-only player can't reach Act 3 (can't beat the Champion), so the floor
  holds trivially.

### 8.5 Macro-safety (`"` / `'` / `%`) — CLEAN in the authored blocks
- Every `say[]`, `win_line`, `lose_line`, `on_win`, `announce` reviewed in the docs avoids `"` and
  avoids `'`/`%` in macro-delivered strings (contractions written out: *does not*, *cannot*, *the
  Company*). The `§k` glitch codes in billboard/board/founder text are color codes, not quotes —
  legal. **The units are macro-clean as written.**
- **One migration debt (not a violation):** the HQ raid + Board + a few onwin lines still show raw
  `title …{"text":…}` component JSON in the DATAPACK NEEDS specs. Per schema Open Q7 these must be
  migrated to the `{do:announce}` path (or authored as function-file `tellraw`/`title` where
  double-quotes ARE legal) before they compile. Flagged in each doc; not an authoring error, a
  build-time lowering choice.

### 8.6 Cross-doc factual discrepancies (verified against the live repo)
- **HQ field gate = 4, not 6.** Live `render.mcfunction` uses `fields_liberated matches 4..`. The
  Mystic doc and the routes doc hedge "4 vs 6 (2026-07-06 ruling)"; **the live number is 4** — use
  it and stop deferring. (Routes provisions 7 Act-1 feeders for slack, which is fine either way.)
- **`liberation/load` names map seeds ONLY `farm_1` today** — every field-guard unit that runs
  `free_field {farm_N}` MUST add its `farm_N:"NAME"` line or the ceremony prints "THE PARCEL". Batch
  1 consolidates all nine additions.
- **Payout haircut "75% floor"** quoted by Scorchspire/Frontier is wrong (§5) — the shipped formula
  gives ~92–94% in the idx-25–32 range. Cosmetic doc-copy fix.

---

## Executive summary

The 14 unit docs together define roughly **44 tracked quests** authored over **~185–195 NPC bodies**
(≈120 new town/route/set-piece characters + 20 shrine cultists + 26 frontier + the reused/extended
gym leaders, villains, board, and shrine leaders), spanning gyms 3–10, the HQ raid, the Board hunt +
Founder mirror, the routes, nobles, shrines, and Battle Frontier. **The recognition gradient and the
economy-voice ladder are both continuous and canon-clean** — grunts→management→late-stand-downs with
civilians never recognizing and Mom never learning, and glossy→nervous→peak-56→clamp-25→corrupted with
no gaps — and the canon roster ids, Founder-name discipline, and macro-safety are all respected as
authored. **The biggest risk is integration, not authoring: the units chose register slots in
isolation and collide catastrophically** (13 units piled onto slots 50–56, three reused live Act-1
slots 57/58/64–72, and there are ~44 proposed sidebar lines against only ~27 free slots — they must
be spread across 44–56 / 86–99 and deliberately slot-shared per §3). **The second risk is a set of
scoreboard-vs-tag lowering traps** (`field_freed`, `defeated_noble_*`) that are dead gates unless the
`liberation/mirror_field_tags` tick and a noble-flag mirror ship — and two dead-set breadcrumbs
(`scrubbing_artifact_*`, `frontier_passes`) that are wired to nothing. **Recommended first batch: the
field-guard liberation wiring (Batch 1 — routes §4 map + the farm-name additions + the mirror tick),
because it is TODO.md's single biggest Act-1→Act-2 blocker and unblocks every liberation-dependent
gate, the HQ raid, the wheat-trader recognition arc, and the relief economy in one compile.**

---

## Completeness and Risk Review (final pass)

Final adversarial sweep for the specific failure classes in the review brief — unreachable gates,
cap-illegal/forced-whiteout battles, macro-unsafe text, invented roster ids, Founder-name leaks,
civilians recognizing the founder, quests missing a waypoint/stage, and promises of engine behavior
that does not exist. Spot-read the four representative units (05 cyber, 10 hq, 11 board, 12 routes)
against the live datapack. **Load-bearing facts below were verified against the repo**, not just the
docs: `quest/render.mcfunction` (HQ gate = `fields_liberated matches 4..`), `dialog/band_tags.mcfunction`
(the live band tags are `badges_gte_N` / `fields_liberated_gte_N` — **no `mem_gte_N`**), `liberation/`
(only `free_field*`, `ceremony`, `load` exist — **no `mirror_field_tags`**; `field_freed` is a
`$scoreboard players set` fake-player, NOT a tag; `load` seeds only `farm_1`), and a grep for
`defeated_noble` across the whole datapack (**zero hits** — the noble flags are entirely unshipped).

### Ranked issues / risks (top 15)

| # | Severity | Issue | Doc(s) | Fix | needs-code? |
|---|----------|-------|--------|-----|:-----------:|
| 1 | **BLOCKER** | **`liberation/mirror_field_tags` does not exist** (verified: `liberation/` has no such file). Every route "talks-back" echo in 12 gates on `field_N_liberated` **tags**, but the only thing produced is the `field_freed` **scoreboard**. Docs 01/03 also gate on `farm_2_free`/`farm_3` as if tags. **~10 dialog gates are DEAD as written.** | 12 (§5/§6), 01, 03 | Ship the ~9-line mirror tick (`for N in 2..10: execute if score farm_N field_freed matches 1 run tag @s add field_N_liberated`) in the liberation tick family, OR rewrite every gate to the `{score:{objective:field_freed,holder:farm_N}}` form. Lock ONE. | **needs code** (new function) |
| 2 | **BLOCKER** | **HQ gate cross-doc contradiction: live number is 4, doc 12 says 6.** Verified `render.mcfunction` = `fields_liberated matches 4..`. Doc 05 and doc 10 correctly use **4**; **doc 12 says `fields_liberated >= 6` in §1, the §4 header, and the §4 "natural-path check"** and builds its whole feeder argument around 6. A builder following 12 will provision/verify against the wrong gate and the §4 prose ("six pre-HQ fields … the gate opens") is internally confused. | 12 (§1/§4) vs 10/05 | Change doc 12's three `>= 6` mentions to `>= 4`. Keep the 7-feeder slack (harmless). Note the live `ceremony.mcfunction` comment also still says "n/6 counter" — cosmetic, but reconcile so nothing on stream claims 6. | data-only (doc + comment) |
| 3 | **BLOCKER** | **`liberation/load` seeds only `farm_1`** (verified). Any `free_field {field:farm_N}` for N≥2 prints the fallback ("THE PARCEL") instead of the farm name. Six pre-HQ liberations rely on this. | 12 (§4), backlog §8.6 | Add the eight `farm_2..10:"NAME"` seed lines to `liberation/load.mcfunction` in Batch 1. | data-only (datapack) |
| 4 | **HIGH** | **`mem_gte_N` band tag is invented — the live name is `badges_gte_N`** (verified: band_tags produces `badges_gte_1..10`, no `mem_*`). Doc 13 nobles gates on `mem_gte_5/7/8/10`; doc 11 Odei uses `fields_liberated_gte_4` (which DOES exist). Every `mem_gte_*` gate is **DEAD** until reconciled. | 13 (via backlog §2.4), band_tags | Pick one spelling and make the compiler emit it; simplest is to author noble gates against the existing `badges_gte_N`. Do NOT ship a second parallel band-tag family. | **needs code** (compiler band-tag) OR data-only if docs switch to `badges_gte_N` |
| 5 | **HIGH** | **`defeated_noble_*` flags produce nothing today** (verified: zero `defeated_noble` refs in the datapack; the noble subsystem writes a `rewards.storyFlag` **scoreboard**). All 7 noble after-entries + sidebar done-states in 13 read them as **tags** → dead gates. | 13 (backlog §2.4 item 2) | One-line tick mirroring the scoreboard→tag, or switch all noble gates to the `score` form. Decide uniformly with #1's ruling. | **needs code** |
| 6 | **HIGH** | **`spawn_gate` is a proposed schema extension that does not exist** — the Founder MUST NOT spawn pre-Champion, but the generated `ambient/place` latch is proximity-only. If `spawn_gate` is not built, the Founder body appears at HQ before the League is beaten. | 11 (§3.5, Open Q3) | Either add `spawn_gate` to `content_compile`, or ship the hand-authored `reveal/founder_spawn.mcfunction` champion-gated import latch. This is a hard prerequisite, not optional flavor. | **needs code** (compiler or function) |
| 7 | **HIGH** | **Pay-probe decline / above-cap-wager charge rail is called "engine work" but unbuilt.** Doc 05 `decline_wager` and every route `decline_fee` "charge via pay-probe" — flagged as engine work in-line. If the rail does not exist, declines either no-op (free) or error. | 05 (§3 Q4), 12 (§3 template) | Confirm the pay-probe/CobbleDollars charge helper exists before shipping any decline_fee; if not, build it once (balance-safe, cannot go negative). | **needs code** (verify/build) |
| 8 | **MEDIUM** | **Register slot collisions across units + oversubscription** — 13 units piled onto 50–56, three reuse SHIPPED 57/58/64–72 (fatal: renders on top of live Act-1 lines / duplicate-holder compile error), and ~44 proposed tracked lines vs ~27 free slots. This is the single biggest integration item. | all units (§3) | Adopt the §3.2 de-collided map (early 44–56, endgame 86–99), slot-share the late band deliberately, and cut the `target:null` texture lines. | data-only |
| 9 | **MEDIUM** | **11 board vs roadmap 14: SCATTERED-vs-SEATED fork is unresolved AND the shipped Board/Founder dialog is written SEATED.** Doc 11 scatters the four across the map; the shipped `board_*`/`villain_final_boss` flavor says *boardroom*, *the table*, *walk up the stairs*. Placing Madeline at a granary while she narrates a boardroom is a visible break. | 11 (Open Q1) | Rule the fork first. If scattered ships, a light rewrite pass of the four seated flavor lines is mandatory (not just placement). The venue-neutral `board_fell` rewrite is safe either way. | data-only (dialog rewrite) |
| 10 | **MEDIUM** | **`scrubbing_artifact_plate`/`_memo` (08) and `frontier_passes` (15) are SET but never READ.** Dead-set breadcrumbs wired to nothing — 08's are claimed to "feed the Board/Founder chain," but 11's `reveal/board_fell` keys only on `defeated_board_*`. | 08, 15 (backlog §2.4) | Either wire an optional Founder pre-battle `any_tags` acknowledgment line, or drop the `tag @s add` lines. Deliberate choice, not a silent dead set. | data-only |
| 11 | **MEDIUM** | **`q.side_raid` (10) slot 82 collides with 08 `q.side_forgeorder` and 15 `q.side_frontier`** (all three grabbed 82 independently; backlog §3.1 confirms). Duplicate-holder compile risk / stacked sidebar. | 10 (§3.7), 08, 15 | Assign per the §3.2 endgame band (10→95, 08→91-94, 15→96-99). Re-verify no duplicate slot at compile. | data-only |
| 12 | **LOW-MED** | **`hq_raid_active` (10) is never cleared.** Self-moots (side holder also requires `not defeated_villain_boss`), so harmless — but it is a set-with-no-clear latch. | 10 (Open Q4) | Leave, or add `tag @s remove hq_raid_active` to `hq_stabilize` (weigh against keeping that function verbatim). | data-only |
| 13 | **LOW-MED** | **Kalahar `≥4 fields` caravan "the founder" line + Ossa's seal line are the loudest pre-Act-3 recognition** — deliberate crescendo (enemy memo-paranoia), but it is the closest the run comes to naming the founder early. Adjacent: the R15 double-down agent screams *there was never a founder, so you are no one* — canon-legal (denial, not a nameplate reveal) but dense. | 04, 12 (§5), backlog §4 | No fix required; confirm on stream these read as enemy paranoia, not narration. Founder-name discipline is otherwise clean — the name is spoken exactly once, at the mirror (`reveal/founder_defeated`, live `@s` selector). | data-only (review only) |
| 14 | **LOW** | **Payout "75% floor" copy is wrong** — the shipped `economy/payout` gives ~92–94% at idx 25–32; Scorchspire/Frontier quote a flat 75% floor (misreading the `min(idx/4,25)` cap). Wrong receipt claim if quoted on stream. | 08, 15 (backlog §5) | Correct the doc copy to ~92–94%; the function is shipped and correct. | data-only |
| 15 | **LOW** | **Migration debt: raw `title …{"text":…}` component JSON in some DATAPACK-NEEDS specs** (HQ raid_begun, board_fell, a few onwin). Legal in a function-file `tellraw`/`title`, but NOT through the `{do:announce}` macro path. If authored via a macro string it breaks on the double-quotes. | 10 (§3.6), 11 (§3.5) | Author those as function-file `title`/`tellraw` (double-quotes legal there) OR via `{do:announce}` plain text — never as a macro-delivered raw-JSON string. The authored `say[]`/`announce` blocks themselves are macro-clean (no `"`/`'`; `§k` is a color code). | data-only (build-time lowering) |

### Clean bills (checked, no issue found)

- **Invented roster ids:** none. 05/10/11/12 all use exact canon ids — `villain_admin`/`_2`/`_commander`,
  `villain_boss`=DJ, `board_{madeline,matt,micah,lauren}`, `villain_final_boss`=Founder, and route agents
  correctly *borrow* `villain_grunt_3..9` teams with distinct `defeat_tag`s rather than duplicating placed
  bodies. New civilians (`hunt_keeper_odei` vs shipped `granary_keeper`/Feng; `cyber_defector_maren` vs
  `cyber_access_admin`) are verified distinct.
- **Founder-name leaks before Act 3:** none. DJ names *the founder* as recognition of the usurped person
  (canon Act-2 boss, not the nameplate). Portraits/plaques are set-dressing with no name. The name resolves
  live only at `reveal/founder_defeated`.
- **Civilians recognizing the founder:** none. The three scoped edge cases (Odei wary *face on a memo*,
  the Cyber teller/Rell reacting to money not the man, Maren naming the *charter/face* not *Founder*) all
  stay inside the rule. Mom untouched.
- **Cap-illegal / forced-whiteout battles:** none. HQ ladder + Board are forced `gauntlet_boss` set-pieces
  but every one is an **opt-in dialog button with a `leave_button`** (no `ON_DISTANCE_VERY_CLOSE` forced
  touch), DJ tops at entry-cap+2 (ladder-canon), and the fairness floor holds (a starter-only player cannot
  reach Act 3). Ohmond's wager and all route beats are opt-in + printed-stake + decline-able. **Verify only:**
  the wager/frontier loss paths never route through the Nuzlocke whiteout (must charge fee → close, never
  `player.kill`).
- **Quests missing a waypoint/stage:** none fatal. Every tracked quest in the four units has a
  `quest_targets` holder + stage; the intentionally-null lines (`q.side_signatures`, `q.main` Board stage)
  are documented `target:null` texture, not omissions. Untracked spotters/props correctly carry no line.

### Ready to build first

The cleanest units to implement immediately — small surface, mostly EXTEND, few unbuilt-engine
dependencies, and they sit early on the critical path:

1. **`10_hq_raid`** — the smallest, highest-leverage unit. Only **1 new character + 1 dialog**; the rest is
   adding `placement` to four shipped villains and a one-shot `tag`+`{do:announce}` discovery button per
   floor. Stabilization is shipped and untouched. Only cautions: fix the slot-82 collision (→95) and note
   there are no decline fees on the boss ladder (so the unbuilt pay-probe rail is irrelevant here). It gates
   the entire post-HQ arc, so it clears the most downstream content per unit of work.
2. **`05_cyber_city`** — self-contained town skin, all plain-tag quest state (no new band tags), macro-clean
   as authored, and it mirrors (does not own) the HQ gate. The one dependency to verify first is the
   pay-probe decline charge (#7) for Ohmond's wager; the other three quests are civilian favors with no
   engine gaps. Build it right after HQ so the on-ramp and the climax land together.
3. **`11_board_founder`** — mostly EXTEND (five `placement` stubs, four pointer NPCs, one `board_fell`
   rewrite), and the reveal beats were deliberately authored venue-neutral so the SCATTERED/SEATED fork does
   not block the paste. **Do NOT start until two rulings land:** the fork (#9) and `spawn_gate` (#6) — with
   those decided, it is a clean batch.

**Do NOT start first:** `12_routes_backbone` — despite being the critical-path BLOCKER, it depends on three
unbuilt pieces (the `mirror_field_tags` tick #1, the `load` farm-name seeds #3, and the `wheat_war_farms` REF
bodies) and carries the wrong HQ gate number (#2). Land those datapack fixes as a dedicated Batch-1 wiring
pass *before* dropping the route content, or every route echo ships dead.
