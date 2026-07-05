# The Cobblemon Initiative — Implementation TODO

Living checklist for everything still outstanding before **1.0.0**. Edit freely and tick
items off (`- [x]`). Claude references this file to know what's left; when an item is
done, Claude removes it **and** any release-removal it unblocks.

**Legend:** 🧱 = you (in-world / map authoring) · 💻 = Claude (code/data) · 🔍 = verify in-game

**Already landed** (not tracked here): level caps, Memory Fragments + re-reader, Dark Urge
whispers, Wheat War economy core (P3), villain recognition dialogue, quest-tracker HUD,
field-mark dev tool, the **per-badge CobbleDollars shop** (`scripts/generate_shop_tiers` →
`cobbledollars_tiers/*.json`, `ShopTierManager`, `/cobblemon-initiative shop <tier>`, wired into all
10 gym leaders + DJ, seeded `badge_0` at install), the **bank re-theme** (`nether_star` 25k backing
+ `hay_block`/`wheat` commons), and the docs set (the GitHub wiki, the `publish-wiki` dev command,
and the slimmed README + UPM 2 disclaimer). See `GIT_COMMIT_MSG` / `docs/LORE_BIBLE.md` / the wiki.

---

## 0. GAME PLAN — the road to 1.0.0 (sequenced 2026-07-02)

The critical path is **verify → author → wire → balance → ship**. Code is largely ahead
of the map now: most remaining 💻 work is blocked on 🧱 authoring or 🔍 verification,
so the phases alternate between us. Sections below (§1-2) hold the item-level detail.

### Phase 0 — Verification gate 🔍 (first session at the new location; ~1 sitting)
The whole session's code is compile-verified but not runtime-verified. One `run-client`
session, in this order (each failure comes back to Claude as a bug report):
1. Boot to menu on the real GPU (the Kotlin fix landed; this confirms it end-to-end).
2. Fresh test world → check the log for **datapack load errors** (liberation/granary/
   wheat_trader functions + macros all parse?).
3. `/cobblemon-initiative install run` → §1.C checklist top-to-bottom (HUD, fragments,
   Dark Urge, economy beats, **shop-tier GATE test**, level caps).
4. New systems: `/cobblemon-initiative shop badge_3` → granary lockstep fires (log line);
   `scoreboard players set @s fields_liberated 4` → wheat-trader hostile dialog + battle;
   `function cobblemon_initiative:liberation/free_field {field:"test_1"}` → −6 idx +
   HUD wheat line; a FARM zone with `mobsSpawn`/`activeWhen*` behaves occupied→liberated.
5. While the client is open: capture **Sodium + BSL settings** → `mrpack/overrides/`
   (§1.F) and run the **`cobbledollars give @s` smoke-test** (§1.B).

### Phase 0.5 — Round 9–10 follow-ups (2026-07-04, v0.4.3-alpha.8)

**WORKFLOW CHANGE 🧱→💻:** NPCs no longer require in-world placement + UUID recording —
a `placement: {x,y,z}` field in the character JSON auto-spawns them once per world
(compiler-generated proximity latches; see ENGINE_FINDINGS §3/§4). In-world placement
is still fine (uuid wins over placement); use whichever is easier per NPC. 30 latches
already live (10 companions + 20 authored-coordinate NPCs incl. the roof agents,
Harvest Road villains, Deng camp, garden stations).

- [ ] 🧱 **Coordinates needed** (compile warns until authored; give Claude coords or
  place bodies): **hua_zhan_leader (gym 2 has NO leader body!)**, hz_greenhouse_docent,
  apiarist_sumi, courier_mio, field_researcher_ume, forewoman_tetsu, company_surveyor,
  doc_ledger_barrel, doc_portrait_crate, notice_post_1–3, sq_kyc_agent,
  villain_grunt_2 + villain_grunt_field_agent (checkpoint tent pair).
- [ ] 🧱💻 **Gym interior casting**: no dialog carriers exist for takehara/hua_zhan
  trainer_1–4 + hz jr_apprentice/apprentice — decide bodies (CSV or placement) and
  Claude wires the battle dialogs.
- [ ] 💻 **Sight arming after latch spawns**: latch-spawned villains get random uuids —
  the authored `npcsight add <uuid>` registrations (route pair, checkpoint pair,
  yield officer/analyst) need a manual pass per world, or a future auto-register hook.
- [x] 💻 **Cap ladder re-space + gym retune** (round 10e, alpha.10): start cap 15
  (gates pre-gym-1 evos), ladder 15/22/30/37/44/50/56/62/68/74/80/100; leader ace =
  entry-cap **+2** (aces 17/24/32/39/46/52/58/64/70/76), roster shifted in step.
  levelcaps.json + ProgressionConfig + CLAUDE.md table + docs updated. See
  ENGINE_FINDINGS §5.
- [ ] 💻 **Gyms 3–10 roster COMPLETION** (future act): 24 missing team files
  ({town}_{trainer_3,trainer_4,jr_apprentice} × 8) — author teams following the same
  ace=cap+2 curve (scale off the existing leader/trainer levels for that gym).
- [ ] 🔍 Existing-world one-time repairs (runbook §J): magikarp respawn, stale
  takehara defeat tags, `/rctmod player set series cobblemon-initiative`.

### Phase 1 — Map authoring sprint 🧱 (parallelizable with Phase 2 wiring)
Author in batches; each batch unblocks Claude wiring the same day:
1. **Zones** via `zone-mapper` (draw towns/routes/shrines/HQ/frontier; FARM zones get
   their "Liberation field id"). Export → install.json. *(§1.A zones)*
2. **Wheat fields** via `field-mark` (6 set-piece) → send Claude the JSON. *(§1.A)*
3. **NPC placement waves**: wheat traders + Granary keeper (record UUID!) → archivist +
   civilians (Nalia is placed; rumor mill/propaganda use the scrubbing register) →
   villains act-by-act (grunts → management → DJ → Board/Founder). *(§1.A)*
4. **Act 1 side-quest cast** (Sango / Blossom Path / Takehara / Gym 1) — the full
   `dialog-src` layer is authored + compiled (106 chars, 0 errors); every side-quest NPC
   has a preset in `default_preset/humanoid/`. Remaining is pure placement + UUID mapping
   (record each UUID → re-run `content_compile` → `update_npc_presets`), then `npcsight add`
   for the sight NPCs (surveyor `surveyor` tag, canvasser `ci_canvasser` tag, perf-review
   sentries `takehara_sentry`, checkpoint agents `checkpoint_agent`). See
   `docs/QUEST_OPTIONS_TOWNS_1-2.md` Status for the selected list.
   - [x] **The Incomplete File / The Lane Looks After Its Own / Notice of Non-Compliance**
     finished 2026-07-03 (the 8 orphaned dialog trees + `personnel_file/*` and
     `noncompliance/*` functions); all sidequest `load`/`tick` entrypoints registered in
     the `#minecraft:load` / `#minecraft:tick` tags.
   - [x] **Off the Record** (Sango) built 2026-07-03 — `sidequest/off_record/*` stealth
     loop (obs_count + off_record_blown, auditor-sight tick), Lucian offer/debrief + Oma
     (errand 1) + Sarii (errand 2) entries; errand 3 + conclusion use the pre-authored
     auditor dialog. Clean-sweep bonus is a heal_ball + praise line (advancement deferred).
   - [x] **Out of Office** (Genji, Takehara) built 2026-07-03 — `fisherman_genji` + dialog
     (8 string → cobblemon:poke_rod + 300 CD via `sidequest/genji/*`) + opt-in 200 CD wager
     (trainer sq_genji_wager, loss_fee 200).
   - [ ] **Fair Market Value** — shelved (no spec; cut for now).
   - [ ] 🔍 **Batch smoke-tests** these side quests lean on: `givepokemonother`
     (Kele Magikarp, trades, gifts), `cobbledollars give/remove @s` inside `execute as`,
     the `can_see_player` stealth branches (surveyor/canvasser), `cobblemon:poke_rod` +
     fossil item ids, renamed `writable_book`/`paper` component shape, **the world-merge
     import** (one NPC: preset import keeps builder skin + our dialog + renames), and the
     **zone-mapper NPC overlay** rendering in a browser.
5. **Act 1 beat 2 — Harvest Road + Hua Zhan City (gym 2, the wheat reveal)** — BUILT
   2026-07-03 (11 showrunner-selected quests + both route backbones + gym ladder
   plumbing; 143 chars compile clean). Design menu: `docs/QUEST_OPTIONS_HARVEST_HUAZHAN.md`;
   placement pass: `docs/PLACEMENT_BEAT2.md` (16 import-only castings + 16 new bodies);
   test script: `docs/VERIFICATION_RUNBOOK.md`. Skipped by selection: Boundary
   Adjustments, the dojo, Pest Control, Bloom Festival, Two Botanies. Guidebook
   (wiki/Guidebook-Act-I.md) updated through Hua Zhan.

### Phase 2 — Wiring on authoring output 💻 (Claude; fast turnaround per batch)
- Field guards: per-field `command` rewards firing `liberation/free_field {field:"<id>"}`;
  FARM zones in install.json get `activeWhenObjective: field_freed` per field id.
- Granary UUID recorded → re-run `generate_granary_tiers` (fills `apply_<tier>` fns).
- Fold zone exports into install.json; regenerate presets/functions; `install run` cycle.

### Phase 3 — Balance + polish (joint; needs Phase 0-2 done)
- **Open balance decisions** (Claude needs answers, §1.B): field pushback −6 × ~6 fields?
  liberation swap a shop tier or stay narrative? liberation gate/soften the HQ raid?
- Tune granary pool/prices, ambush thresholds, instability tug-of-war from playtest feel.
- Optional narrative systems: Founder name de-obfuscation as the Board falls (next
  unblocked 💻 item), exchange boards, reserved farm plots; Option C (`farmzone/`) go/no-go.

### Phase 4 — Release pass (§2; mostly 💻 after authoring bakes)
- Strip dev tooling (field-mark → zone-trace → npc-map) as each authoring stream closes.
- Debug-command audit, docs/wiki sync + `publish-wiki`, version bump **1.0.0**,
  `build-mrpack --with-map`, and a clean-launcher **install test** of the final pack.

---

## 1. BUILD — still to land

### A. In-world authoring 🧱 (blocks the systems in §B)
- [x] **Zones** — the zone-mapper export (dev/zones.json) is baked into `install.json`: **58 zones** — 13 towns, **all 5 shrines**, **7 Battle Frontier facilities** (flavor subtitles), 19 route segments named in journey order ("Route N" subtitles; user-named "Road to Royal League"; Route 16 = "Frontier Causeway"), **10 farms gated `field_freed`/`farm_N`**, 3 landmarks, canonical HQ preserved. Array priority-sorted so nested zones (facilities/shrines) announce over their surroundings. *Optional:* route gaps 4/6/9/11 if intended. 🔍 fresh `install run`. → *unblocks removing Zone Trace (§2)*
- [ ] **Villain NPC placement** — every villain in `villain_team.json` is at `[0,0,0]`. Place each with real coords, composed from the battle + recognition-dialogue snippets:
  - [ ] 11 grunts (Field Agent → Elite Agent), gym-gated on routes
  - [ ] 3 management (Regional Manager Shade, Senior Director Vex, COO Noir)
  - [ ] Acting CEO DJ at HQ `[1590 51 1028]`
  - [ ] 4 Board members + The Founder (post-Royal-League, The Boardroom)
- [ ] **Wheat fields** — zones are DONE (10 farms in install.json, gated on `field_freed`/`farm_1`..`farm_10` — those ids are now canonical). **As shipped (2026-07-04) only `farm_1` is wired to `liberation/free_field` — `fields_liberated` maxes at 1**, so the HQ-raid gate (4), the wheat-trader escalation (2/4), the relief shop tiers (2/4), and the granary ambush (4) are all unreachable. **This is the single biggest Act-1→Act-2 blocker.** *Remaining:* place the field-guard trainers at the farms 🧱 → Claude wires each guard's reward `execute as {player} run function cobblemon_initiative:liberation/free_field {field:"farm_N"}` 💻. (Field Mark tool likely obsolete now — zones came from the zone-mapper.)
- [ ] 🧱 **Place the compiled-but-unplaced NPCs** — the dead-letter checkpoint agent (`checkpoint_agent` tag), the Per My Last Memo courier, and the Head Count census wagon: compiled presets exist for all three, but there is no UUID mapping / import line yet.
- [ ] **Wheat-trader NPCs** — place (trade→recognize→ambush) from `wheat_trader_gate` + `trade_wheat_trader` + `dialog_wheat_pitch`
- [ ] **Granary trader NPC** — Company Inc. member selling items **for wheat**. **Infrastructure landed:**
  - [x] `granary_keeper` character + 3-tier recognition dialog (default → suspicious ≥2 fields → hostile ≥4, hostile trade arms `granary_ambush_armed`); compiled via content_compile.
  - [x] Badge-tiered offers + **wheat bell curve**: `scripts/granary_tiers/master_granary.json` (+ `generate_granary_tiers`) bakes 12 tier presets (`granary_keeper_<tier>.npc.snbt`) — wheat cost = base × (1+(56−idx)×0.012), e.g. rare_candy 20→12(peak)→16 wheat. No restocks (stock baked, no reset). Item IDs validated against the Cobblemon 1.7.3 jar.
  - [x] Lockstep retier: `ShopTierManager.applyTier` also fires `function cobblemon_initiative:granary/apply_<tier>` (stubs until UUIDs recorded).
  - [ ] 🧱 Place Granary NPC(s), map UUID → `humanoid/granary_keeper` in npc_presets.json, re-run `generate_granary_tiers` (fills apply functions), `install run`
  - [x] Post-trade ambush poller + ambush battles — `granary/tick` fires the one-shot post-trade battle (`granary_ambush` L43-44); wheat traders battle directly from hostile dialog (`wheat_trader_ambush` L38-39). Trainers in villain_team.json, jar-validated.
  - [ ] Tune the item pool / prices / ambush thresholds after an in-game pass 🔍
- [ ] **Memory re-reader (Archivist) NPC** per town (`dialog_memory_rereader`)
- [ ] **Reserved farm plots** — griefing-safe plots for liberated-field safe-farm conversion
- [ ] *(Optional)* civilian NPCs: **Mom (Nalia) full arc authored + compiled** (first meeting → warming ≥4 badges → worry post-HQ → homecoming post-Founder; never learns the truth — LORE_BIBLE §2); her UUID is already mapped. Remaining: rumor mill + Company propaganda NPCs (scrubbing register lines are written and waiting in `dialog-src/registers/scrubbing.json`)
- [ ] *(Optional)* per-town exchange-board sign/lectern (economy instability display)

### B. Remaining systems 💻
- [ ] **STANDALONE: capture the CobbleDollars bank re-theme into the mod** 💻 — the
  `nether_star` 25k backing + `hay_block`/`wheat` commons re-theme (listed above as
  "already landed") exists **nowhere in the repo or the mrpack** (verified 2026-07-04:
  the `cobbledollars_tiers/*.json` resources carry only `defaultShop`; no bank file
  anywhere). It apparently lives only in the live instance's
  `config/cobbledollars/bank.json` — **lost on every fresh install, pack or standalone**.
  Export that file from the instance into `src/main/resources/cobbledollars_tiers/`
  (or a `cobbledollars_bank.json` resource) and have `ShopTierManager.applyTier` /
  `install run` seed it the same way the shop is seeded (then `cobbledollars reload`).
  Same standalone rule as the Easy NPC security.cfg patcher (see docs/ENGINE_FINDINGS §3).
- [ ] **P4 — Field liberation** (needs marked coords): guard trainers, liberate → restore wheat price + unlock safe-farm + advance HQ gate; relog-safe one-way latches; `liberation/fields.json`
  - [x] **Option A core** — `liberation/free_field`(+`_apply`): −6 `cd_instability` (floor 0, tunable) + `fields_liberated`++ + per-field `field_freed` latch + actionbar beat. *Remaining for A:* wire a field-guard `command` reward `execute as {player} run function cobblemon_initiative:liberation/free_field {field:"<id>"}` (blocked on marked field coords 🧱)
  - [x] **Option B** — conditional safe-zones: `SafeZone.activeWhenObjective`/`activeWhenHolder`/`activeWhenMin` (+ `isZoneActive` + server-aware `isInSafeZone`/`getSafeZoneAt`/`getAnnouncedZoneAt`, threaded into MobSpawnMixin + Dark Urge + zone-announce). Occupied field = hostile until its `field_freed` latch trips → then safe farmland (world-data, relog-safe). compileJava verified. Zone-mapper exposes the gate (FARM "Liberation field id" → activeWhen*). *Remaining:* set it per field when coords are marked.
  - [x] ~~Granary `sell_wheat` datapack~~ — **dropped (design confirmed):** the default economy is CobbleDollars + its built-in bank (handles wheat→CD), and wheat trading is the Easy NPC "wheat traders" (paper). No custom CD sell-back / Granary datapack.
  - [x] `wheat_war_active` flag — set by the first field liberation (`free_field_apply` adds the player tag; `quest/render` shows the wheat-fields HUD line)
  - [x] Wire `wheat_trader/load` + `wheat_trader/tick` into the function tags (+ new `liberation/load`)
  - [x] **Balance decisions (resolved 2026-07-02):** pushback stays **−6/field**; liberation **swaps tiers** — every 2 fields upgrades the active shop+granary tier to a pre-baked relief catalog (`<tier>_relief1/2`, −12 idx each; `ShopTierManager.resolveRelief` reads `fields_liberated` live; `shop refresh` fired by `free_field_apply`; gym rewards unchanged); HQ raid is **hard-gated on 4 liberated fields** (DJ's battle entry gated, "monopoly holds" refusal below it, quest HUD shows "Starve the monopoly" until 4). Thresholds (2/level, −12, gate=4) tunable 🔍.
  - [ ] *(Option C, deferred)* stateful `farmzone/` subsystem (soil/growth/patrols/HQ-difficulty)
- [ ] **P5 — Wheat traders full wiring** — _done: the trade→recognize→ambush dialogue, `minecraft:paper` currency, 2/4 thresholds, **ambush trainers** (`wheat_trader_ambush` L38-39 farm team / `granary_ambush` L43-44, in villain_team.json, species+items jar-validated), **wheat-trader hostile tier now offers the battle** ("Stand and fight" → tbcs vs wheat_trader_ambush), and the **granary post-trade poller** (`granary/tick`: hostile trade arms `granary_ambush_armed`, ~15s countdown → "Asset located. Initiating retrieval." → battle, one-shot via defeated_granary_ambush)._ Remaining: in-world placement (Easy NPC traders) 🧱 + in-game verify the tbcs battle/onwin path 🔍. (`wheat_ambush_armed` is now superseded — wheat traders battle directly from dialog; objective left declared, unused.)
- [x] ~~Smoke-test `cobbledollars add @s`~~ — **moot (2026-07-03):** CobbleDollars 2.0.0-Beta-5.1 has **no `add` subcommand at all** (jar-verified grammar: pay/query/give/remove/set/reload/leaderboard). Every `cobbledollars add` in the repo (pay_macro, granary ambush, all 143 battle-prize onwin strings) was a dead command — all replaced with `cobbledollars give <targets> <amount>` (selector-first, accepts @s under execute-as). Verify in-game that a battle prize actually lands (runbook Round 5 canary #5).
- [ ] **Cast the 20 empty trainer teams** 💻/🧱 (HIGH PRIORITY): `data/rctmod/trainers/{royal_champion, royal_elite_1..4, dragon/fairy/fire/ground/ice_shrine_leader, *_shrine_cultist_1..2}.json` are literally `{}` — rctapi refuses to start those battles (`insufficientPokemon`), and the shipped presets already wire battles against the Royal League + shrine leaders, so those buttons dead-end today. `content_compile` now WARNS on every battle that references an empty/missing team (28 warnings currently).
- [ ] **Author the 18 missing trainer JSONs** 💻 (blocks the Act-2 arc): `villain_boss` (Acting CEO DJ — the badge-7 HQ-raid HUD target and every `defeated_villain_boss` gate depend on it), `board_lauren`/`board_madeline`/`board_matt`/`board_micah`, `villain_admin`/`villain_admin_2`/`villain_commander`, and `villain_grunt_3..11`.
- [ ] **villain_grunt_2 checkpoint dialog ladder** 💻 (minor polish): the `default` entry is dead code, and `contraband` is overshadowed at 3+ badges.
- [ ] **RCT trainer data cleanup** 💻 (from the log-0.4.1-alpha.2 review): 86 trainers log "Model validation failure" — invalid gender/ability/move entries throughout; 3 Royal League trainers hold **mega_showdown items that are not in the pack** (elite_four_lorelei `blue_orb`, champion_terry `red_orb`, title_defense_zeph `steel_memory` — those items will simply be missing in the fights); gym-9 leader `skadi_gymleader1` references invalid species `cobblemon:ninetales_alola` (regional forms are species aspects in Cobblemon) — **she may silently drop that team member on stream**.
- [x] **Founder reveal (redesigned per decision)** — the Founder's nameplate stays fully `§k`-obfuscated all run (`§kfounder`); each Board defeat fires `reveal/board_fell` (4 oblique beats that circle the name); the name is only spoken at the mirror's defeat — `reveal/founder_defeated` renders **the defeating player's own name** live via selector ("The name on the chair was always ⟨you⟩"). No name baked anywhere. *(Propaganda-decay register: done — `dialog-src/registers/scrubbing.json`.)*

### C. Verify in-game 🔍 (can't be tested without the mod loader)
- [ ] **PLAYER_TAG dialog conditions** fire correctly (re-reader, wheat-trader tiers, grunt/management recognition). Bytecode-settled 2026-07-04: Easy NPC 6.25 **ignores the Operation field** (`contains()` only), so every gate is EQUALS on a tag — "not_tag" gates ride the derived inverse tags `no_<X>`, maintained each tick by `function/dialog/band_tags.mcfunction` (auto-generated; also `no_defeated_<id>` for all 95 shipped trainers). Remaining 🔍 is the in-game confirm.
- [ ] **Quest HUD** renders (**sidebar-only** since 2026-07-04 — the top "Objective" boss bar was removed; `quest/load` runs `bossbar remove cobblemon_initiative:objective` to clear old worlds; numbers hidden; the main line advances on gym defeat; side lines ride `q.side_*` slots; `/ca quest hide` clears). See `docs/VERIFICATION_RUNBOOK.md` **Round 7 canaries**.
- [ ] **Memory fragment** title fires once per leader; no re-fire on relog
- [ ] **Dark Urge** whisper fires outside safe zones, silent inside
- [ ] **Economy beats**: gyms 1-7 tick `cd_instability` up; Acting CEO → "CURRENCY STABILIZED"
- [ ] **Shop-tier smoke-test (GATE)**: edit `config/cobbledollars/default_shop.json`, `/cobbledollars reload` with the shop GUI open → prices change live? If a Pokémart merchant does NOT update, it uses a **custom per-entity shop** (re-provision it to the default shop, or the swap won't reach it). Then confirm `/cobblemon-initiative shop badge_3` swaps + reloads, and a gym-leader defeat advances the tier.
- [ ] **Level caps** applied correctly (30 → 85 → 100)

### D. Per-run hardcore setup 🧱 (every fresh world — see `docs/HARDCORE_RUNBOOK.md`)
- [ ] Wipe/empty `config/cobblemon-initiative.json` `safeZones`, then `/cobblemon-initiative install run`
- [ ] Confirm hardcore flag + relog

### E. Docs & wiki 💻/🧱
- [ ] **Publish the wiki** — initialize the GitHub wiki once (create any page in the repo's *Wiki* tab), then run `publish-wiki` to push `wiki/` (it link-checks first; URL defaults to `<origin>.wiki.git`).
- [ ] After editing `wiki/` pages, re-run `publish-wiki` to keep the live wiki in sync.

### F. Zone-mapper & dev environment 💻
- [x] Zone-mapper: offline (vendored OpenLayers UMD + polygon-clipping), FARM zone type, per-zone mob-spawning control (`mobsSpawn`), priority-based overlap clipping, route→corridor buffer + retroactive width-adjust
- [x] mrpack: the 2 new resource packs default-on; BSL shader on by default (`config/iris.properties`); video maxed for the Sodium/Iris stack (graphicsMode kept Fancy — Fabulous breaks Iris)
- [~] **Sodium + BSL settings seeded** (2026-07-03), pending in-game verify 🔍: `config/sodium-options.json` sets Quality → Weather + Leaves = **Fancy**; `shaderpacks/BSL_v10.1.3.zip.txt` sets Material → Advanced Materials = **On** (`ADVANCED_MATERIALS=true`). These are best-effort schema (Sodium 0.6.13 + Reese's/Sodium-Extra key names, and the BSL define name) — confirm they take on a `run-client` session with a display, then capture the full generated files to overwrite these stubs.
- [x] **run-client fixed** — added `fabric-language-kotlin:1.13.12+kotlin.2.4.0` (Cobblemon's Kotlin runtime/adapter) to `build.gradle.kts`; it was crashing at launch with `NoClassDefFoundError: kotlin/jvm/internal/Intrinsics`. Verified headless: 69 mods + Cobblemon + **all cobblemon-initiative subsystems init**; only the software-GL window step fails in the sandbox. → 🔍 confirm full boot to menu on a real GPU
- **Known limitation: `gradle runServer` can't host the full companion stack.** Cobblemon's production jar does intermediary-name reflection (`class_2960`) that can't resolve in a mojmap dev server, and JourneyMap additionally wants its separate API jar. night-config 3.8.1 was added as a dev runtime dep (fixed one layer). Consequence: datapack runtime verification (liberation/granary functions, shop→granary lockstep) is 🔍 in-game via run-client, not headless.
- [ ] *(mod)* `mobsSpawn` per-zone flag — verify in-game a `Spawn freely` FARM/ROUTE zone actually keeps spawns while a town suppresses 🔍

---

## 2. REMOVE BEFORE 1.0.0 — dev-only tooling

Map-authoring aids. Strip each once its authoring is baked in.

### Field Mark tool (`fieldmark/`) — once all wheat fields are marked + baked
- [ ] Delete `src/main/java/.../fieldmark/{FieldMarkInit,FieldMarkCommand,FieldMarkStorage}.java`
- [ ] Remove entrypoint `com.thecompanyinc.cobblemoninitiative.fieldmark.FieldMarkInit` from `fabric.mod.json`
- [ ] *(No build.gradle dep to remove)*

### Zone Trace tool (`zonetrace/`) — once all zones are traced + committed to `install.json`
- [ ] Delete `src/main/java/.../zonetrace/{ZoneTraceInit,ZoneTraceCommand,ZoneTraceSession,ZoneTraceStorage}.java`
- [ ] Remove entrypoint `...zonetrace.ZoneTraceInit` from `fabric.mod.json`
- [ ] Remove `build.gradle.kts` dep `fabricApi.module("fabric-events-interaction-v0", ...)` (only if nothing else uses `UseBlockCallback`)

### NPC Map system (`npcmap/`) — once NPC presets are finalized + `update_npc_presets.mcfunction` is baked
> ⚠ **Scope changed 2026-07-04:** `npcmap/` now also hosts `NpcPresetRefreshManager` — the SHIPPING
> per-chunk-load preset refresh that `NpcMapInit.onInitialize()` wires and that `InstallCommand`
> + `ShopTierManager` depend on. Only the dev-tool classes below may be stripped at 1.0.0;
> `NpcPresetRefreshManager` must stay (move its `init()` call into `InitiativeInit` when
> `NpcMapInit` goes).
- [ ] Delete `src/main/java/.../npcmap/{NpcMapCommand,NpcMapEntry,NpcMapStorage}.java` (keep `NpcPresetRefreshManager`)
- [ ] Move `NpcPresetRefreshManager.init()` into `InitiativeInit`, then delete `NpcMapInit` + its entrypoint in `fabric.mod.json`
- [ ] Remove the LEGACY npc-map replay block in `InstallCommand.cmdRun()` + the NPC count line in `cmdCheck()` (the armed-refresh block stays)

### Dev datapack functions (`function/dev/`)
- [ ] Review/remove `data/cobblemon_initiative/function/dev/npc_tour_*.mcfunction` + the `npc_tour_idx` objective (keep `function/update_npc_presets` — that ships)

### General release pass *(pre-audited 2026-07-02 — the strip is now a checklist)*
- [ ] Debug-only command surfaces found (keep-or-strip decision each):
  - `CobblemonInitiativeCommands.java:116` — `shrine <id> test <name>` (fairy shrine test runner, ~L595)
  - `CobblemonInitiativeCommands.java:832` — `/cobblemon-initiative dev kit` (shrine crystals + test items)
- [ ] Dev datapack functions: `function/dev/npc_tour_{fetch,goto,init,next,prev}.mcfunction` + `npc_tour_idx` objective
- [ ] Dev-tool doc references to scrub at strip time: `wiki/Commands.md`, `wiki/Architecture-Overview.md`
  (both intentionally document the dev tools today — they carry the "removed at 1.0.0" flags)
- [ ] Bump `build.gradle.kts:8` version `0.2.0-alpha.1` → `1.0.0`

---

*Removal details mirror the dev-only cleanup notes. `install/`, `mapfrontiers/` packages and
`install.json` (with baked vertex data) all STAY in the shipped mod.*
