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
   (§1.F) and run the **`cobbledollars add @s` smoke-test** (§1.B).

### Phase 1 — Map authoring sprint 🧱 (parallelizable with Phase 2 wiring)
Author in batches; each batch unblocks Claude wiring the same day:
1. **Zones** via `zone-mapper` (draw towns/routes/shrines/HQ/frontier; FARM zones get
   their "Liberation field id"). Export → install.json. *(§1.A zones)*
2. **Wheat fields** via `field-mark` (6 set-piece) → send Claude the JSON. *(§1.A)*
3. **NPC placement waves**: wheat traders + Granary keeper (record UUID!) → archivist +
   civilians (Nalia is placed; rumor mill/propaganda use the scrubbing register) →
   villains act-by-act (grunts → management → DJ → Board/Founder). *(§1.A)*

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
- [ ] **Zones** — trace/finalize every zone with `/cobblemon-initiative zone-trace begin <name>` → `export` → paste into `install.json` (towns, routes, shrines, VILLAIN HQ, Battle Frontier, landmarks), then a fresh `install run`. → *unblocks removing Zone Trace (§2)*
- [ ] **Villain NPC placement** — every villain in `villain_team.json` is at `[0,0,0]`. Place each with real coords, composed from the battle + recognition-dialogue snippets:
  - [ ] 11 grunts (Field Agent → Elite Agent), gym-gated on routes
  - [ ] 3 management (Regional Manager Shade, Senior Director Vex, COO Noir)
  - [ ] Acting CEO DJ at HQ `[1590 51 1028]`
  - [ ] 4 Board members + The Founder (post-Royal-League, The Boardroom)
- [ ] **Wheat fields** — mark with `/cobblemon-initiative field-mark add <id> <region>` (6 set-piece + scattered minor), `export`, send Claude the JSON. → *unblocks P4 and removing Field Mark (§2)*
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
- [ ] **P4 — Field liberation** (needs marked coords): guard trainers, liberate → restore wheat price + unlock safe-farm + advance HQ gate; relog-safe one-way latches; `liberation/fields.json`
  - [x] **Option A core** — `liberation/free_field`(+`_apply`): −6 `cd_instability` (floor 0, tunable) + `fields_liberated`++ + per-field `field_freed` latch + actionbar beat. *Remaining for A:* wire a field-guard `command` reward `execute as {player} run function cobblemon_initiative:liberation/free_field {field:"<id>"}` (blocked on marked field coords 🧱)
  - [x] **Option B** — conditional safe-zones: `SafeZone.activeWhenObjective`/`activeWhenHolder`/`activeWhenMin` (+ `isZoneActive` + server-aware `isInSafeZone`/`getSafeZoneAt`/`getAnnouncedZoneAt`, threaded into MobSpawnMixin + Dark Urge + zone-announce). Occupied field = hostile until its `field_freed` latch trips → then safe farmland (world-data, relog-safe). compileJava verified. Zone-mapper exposes the gate (FARM "Liberation field id" → activeWhen*). *Remaining:* set it per field when coords are marked.
  - [x] ~~Granary `sell_wheat` datapack~~ — **dropped (design confirmed):** the default economy is CobbleDollars + its built-in bank (handles wheat→CD), and wheat trading is the Easy NPC "wheat traders" (paper). No custom CD sell-back / Granary datapack.
  - [x] `wheat_war_active` flag — set by the first field liberation (`free_field_apply` adds the player tag; `quest/render` shows the wheat-fields HUD line)
  - [x] Wire `wheat_trader/load` + `wheat_trader/tick` into the function tags (+ new `liberation/load`)
  - [x] **Balance decisions (resolved 2026-07-02):** pushback stays **−6/field**; liberation **swaps tiers** — every 2 fields upgrades the active shop+granary tier to a pre-baked relief catalog (`<tier>_relief1/2`, −12 idx each; `ShopTierManager.resolveRelief` reads `fields_liberated` live; `shop refresh` fired by `free_field_apply`; gym rewards unchanged); HQ raid is **hard-gated on 4 liberated fields** (DJ's battle entry gated, "monopoly holds" refusal below it, quest HUD shows "Starve the monopoly" until 4). Thresholds (2/level, −12, gate=4) tunable 🔍.
  - [ ] *(Option C, deferred)* stateful `farmzone/` subsystem (soil/growth/patrols/HQ-difficulty)
- [ ] **P5 — Wheat traders full wiring** — _done: the trade→recognize→ambush dialogue, `minecraft:paper` currency, 2/4 thresholds, **ambush trainers** (`wheat_trader_ambush` L38-39 farm team / `granary_ambush` L43-44, in villain_team.json, species+items jar-validated), **wheat-trader hostile tier now offers the battle** ("Stand and fight" → tbcs vs wheat_trader_ambush), and the **granary post-trade poller** (`granary/tick`: hostile trade arms `granary_ambush_armed`, ~15s countdown → "Asset located. Initiating retrieval." → battle, one-shot via defeated_granary_ambush)._ Remaining: in-world placement (Easy NPC traders) 🧱 + in-game verify the tbcs battle/onwin path 🔍. (`wheat_ambush_armed` is now superseded — wheat traders battle directly from dialog; objective left declared, unused.)
- [ ] **Smoke-test `cobbledollars add @s`** inside `execute as` before P4 relies on the payout macro
- [x] **Founder reveal (redesigned per decision)** — the Founder's nameplate stays fully `§k`-obfuscated all run (`§kfounder`); each Board defeat fires `reveal/board_fell` (4 oblique beats that circle the name); the name is only spoken at the mirror's defeat — `reveal/founder_defeated` renders **the defeating player's own name** live via selector ("The name on the chair was always ⟨you⟩"). No name baked anywhere. *(Propaganda-decay register: done — `dialog-src/registers/scrubbing.json`.)*

### C. Verify in-game 🔍 (can't be tested without the mod loader)
- [ ] **PLAYER_TAG dialog conditions** fire correctly (re-reader, wheat-trader tiers, grunt/management recognition). Fallback if not: `execute if entity @s[tag=...]` command branch.
- [ ] **Quest HUD** renders (boss bar + sidebar; numbers hidden; advances on gym defeat; `/ca quest hide` clears)
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
- [ ] Capture **max Sodium + BSL shader settings** via `run-client` and bundle `config/sodium-options.json` + `shaderpacks/BSL_v10.1.3.zip.txt` into `mrpack/overrides/` — can't be done headless (needs a display); do on a workstation 🔍
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
- [ ] Delete `src/main/java/.../npcmap/{NpcMapInit,NpcMapCommand,NpcMapEntry,NpcMapStorage}.java`
- [ ] Remove entrypoint `...npcmap.NpcMapInit` from `fabric.mod.json`
- [ ] Remove the NPC apply block in `InstallCommand.cmdRun()` + the NPC count line in `cmdCheck()` + the 3 `npcmap` imports

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
