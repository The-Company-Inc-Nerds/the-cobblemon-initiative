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
- [ ] **Granary buyer NPC** per town (sell wheat → CobbleDollars; P5). The default **bank** now
  buys wheat at a deliberate Company lowball (`wheat` 25 / `hay_block` 225 CD); granaries should
  **beat** that rate so liberating fields feels like escaping the monopoly.
- [ ] **Memory re-reader (Archivist) NPC** per town (`dialog_memory_rereader`)
- [ ] **Reserved farm plots** — griefing-safe plots for liberated-field safe-farm conversion
- [ ] *(Optional)* civilian NPCs: Mom in Sango (`dialog_first_meeting`), rumor mill, Company propaganda
- [ ] *(Optional)* per-town exchange-board sign/lectern (economy instability display)

### B. Remaining systems 💻
- [ ] **P4 — Field liberation** (needs marked coords): guard trainers, liberate → restore wheat price + unlock safe-farm + advance HQ gate; relog-safe one-way latches; `liberation/fields.json`
  - [x] **Option A core** — `liberation/free_field`(+`_apply`): −6 `cd_instability` (floor 0, tunable) + `fields_liberated`++ + per-field `field_freed` latch + actionbar beat. *Remaining for A:* wire a field-guard `command` reward `execute as {player} run function cobblemon_initiative:liberation/free_field {field:"<id>"}` (blocked on marked field coords 🧱)
  - [x] **Option B** — conditional safe-zones: `SafeZone.activeWhenObjective`/`activeWhenHolder`/`activeWhenMin` (+ `isZoneActive` + server-aware `isInSafeZone`/`getSafeZoneAt`/`getAnnouncedZoneAt`, threaded into MobSpawnMixin + Dark Urge + zone-announce). Occupied field = hostile until its `field_freed` latch trips → then safe farmland (world-data, relog-safe). compileJava verified. *Remaining:* expose activeWhen* in the zone-mapper for FARM zones; set it per field when coords are marked.
  - [ ] **Option B** — Granary `sell_wheat` above the Company lowball (reuses `economy/payout`; needs the `cobbledollars add @s` smoke-test first, and Granary NPC placement 🧱)
  - [ ] `wheat_war_active` flag (lights up the HUD wheat-fields line + the trader poller)
  - [x] Wire `wheat_trader/load` + `wheat_trader/tick` into the function tags (+ new `liberation/load`)
  - [ ] **Balance decision (before B):** field pushback magnitude (−6?) + count (~6 fields?); whether liberating swaps a shop tier (prices are static-baked, so instability alone won't re-price) or stays narrative/Granary-driven; whether liberation gates the HQ raid
  - [ ] *(Option C, deferred)* stateful `farmzone/` subsystem (soil/growth/patrols/HQ-difficulty)
- [ ] **P5 — Wheat traders full wiring** — _done: the trade→recognize→ambush dialogue, `minecraft:paper` currency, and 2/4 recognition/ambush thresholds._ Remaining: in-world placement + the post-trade ambush trigger; Granary `sell_wheat` (batches-first, CD); per-region `wheat_price_<region>`
- [ ] **Smoke-test `cobbledollars add @s`** inside `execute as` before P4 relies on the payout macro
- [ ] *(Designed, optional)* Founder name de-obfuscation as the Board falls; propaganda-decay dialog registers

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

### General release pass
- [ ] Audit for debug-only commands (e.g. `/cobblemon-initiative shrine <id> test <name>`) — keep or strip
- [ ] Confirm no lingering `field-mark` / `zone-trace` / `npc-map` references in docs
- [ ] Bump version to `1.0.0`

---

*Removal details mirror the dev-only cleanup notes. `install/`, `mapfrontiers/` packages and
`install.json` (with baked vertex data) all STAY in the shipped mod.*
