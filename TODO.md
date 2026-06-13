# The Cobblemon Initiative — Implementation TODO

Living checklist for everything still outstanding before **1.0.0**. Edit freely and tick
items off (`- [x]`). Claude references this file to know what's left; when an item is
done, Claude removes it **and** any release-removal it unblocks.

**Legend:** 🧱 = you (in-world / map authoring) · 💻 = Claude (code/data) · 🔍 = verify in-game

**Already landed** (not tracked here): level caps, Memory Fragments + re-reader, Dark Urge
whispers, Wheat War economy core (P3), villain recognition dialogue, quest-tracker HUD,
field-mark dev tool. See `GIT_COMMIT_MSG` / `docs/LORE_BIBLE.md`.

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
- [ ] **Granary buyer NPC** per town (sell wheat → CobbleDollars; P5)
- [ ] **Memory re-reader (Archivist) NPC** per town (`dialog_memory_rereader`)
- [ ] **Reserved farm plots** — griefing-safe plots for liberated-field safe-farm conversion
- [ ] *(Optional)* civilian NPCs: Mom in Sango (`dialog_first_meeting`), rumor mill, Company propaganda
- [ ] *(Optional)* per-town exchange-board sign/lectern (economy instability display)

### B. Remaining systems 💻
- [ ] **P4 — Field liberation** (needs marked coords): guard trainers, liberate → restore wheat price + unlock safe-farm + advance HQ gate; relog-safe one-way latches; `liberation/fields.json`
  - [ ] `NuzlockeConfig.SafeZone.activeWhenScore` (world-data-gated safe farm — required for the config-leak)
  - [ ] `wheat_war_active` flag (lights up the HUD wheat-fields line + the trader poller)
  - [ ] Wire `wheat_trader/load` + `wheat_trader/tick` into the function tags
- [ ] **P5 — Wheat traders full wiring**: placement + post-trade ambush trigger; Granary `sell_wheat` (batches-first, CD); per-region `wheat_price_<region>`
- [ ] **Smoke-test `cobbledollars add @s`** inside `execute as` before P4 relies on the payout macro
- [ ] *(Designed, optional)* Founder name de-obfuscation as the Board falls; propaganda-decay dialog registers

### C. Verify in-game 🔍 (can't be tested without the mod loader)
- [ ] **PLAYER_TAG dialog conditions** fire correctly (re-reader, wheat-trader tiers, grunt/management recognition). Fallback if not: `execute if entity @s[tag=...]` command branch.
- [ ] **Quest HUD** renders (boss bar + sidebar; numbers hidden; advances on gym defeat; `/ca quest hide` clears)
- [ ] **Memory fragment** title fires once per leader; no re-fire on relog
- [ ] **Dark Urge** whisper fires outside safe zones, silent inside
- [ ] **Economy beats**: gyms 1-7 tick `cd_instability` up; Acting CEO → "CURRENCY STABILIZED"
- [ ] **Level caps** applied correctly (30 → 85 → 100)

### D. Per-run hardcore setup 🧱 (every fresh world — see `docs/HARDCORE_RUNBOOK.md`)
- [ ] Wipe/empty `config/cobblemon-initiative.json` `safeZones`, then `/cobblemon-initiative install run`
- [ ] Confirm hardcore flag + relog

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
