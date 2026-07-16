# Dev-Tool Strip — 1.0.0 Release Change-List

Verified against the tree on 2026-07-16. This is the ordered, ready-to-execute removal of the
dev-only authoring/test scaffolding before the 1.0.0 release build. Nothing here is done yet —
the tools are **still in use**, so honor the preconditions first.

`zonetrace/` is already gone (no action). The `cutscene/` rig **ships** — only the *recorder* is dev.
`NpcPresetRefreshManager` is **shipping code** (chunk-load preset refresh, used by ShopTierManager /
AutoInstall / InstallCommand / InitiativeInit / DaycareManager) — it lives in `npcmap/` but must survive.

---

## Preconditions (each unblocks a section)

| # | Precondition | Unblocks |
|---|--------------|----------|
| 1 | **Gym-mark coordinate export complete** — GymMark wand coords baked into placements | §A devtools/ |
| 2 | **All NPCs placed** (coords/uuids authored) so the install npc-map replay is unneeded | §B npcmap/ + InstallCommand |
| 3 | **Cutscene wishlist recorded** (HQ raid, Board clearout, Founder mirror, Royal League entrance) + baked | §C CutsceneRecorder |
| 4 | **Headless smoke / release-verify complete** | §F carpet dep |

---

## §A — devtools/ package (13 classes) + entrypoint + mixin + dep
1. **Delete** `src/main/java/com/thecompanyinc/cobblemoninitiative/devtools/` (all 13: AutoBattler,
   DevBotCommand, DevCommands, DevNoteCommand, DevNoteInit, DevNoteStorage, DevPlaceManager,
   DevTestManager, DevToolsInit, DevWandTool, GymMarkCommand, GymMarkStorage, GymMarkWand).
2. `src/main/resources/fabric.mod.json` — remove `"…devtools.DevToolsInit"` from `entrypoints.main`.
3. `src/main/resources/cobblemon-initiative.mixins.json` — remove `"DevWandInputMixin"` from `mixins[]`.
4. **Delete** `src/main/java/…/mixin/DevWandInputMixin.java` (imports the deleted DevWandTool).
5. `build.gradle.kts:65` — remove the `fabric-message-api-v1` `modImplementation` line (used **only** by
   DevWandTool's chat-note capture; verified sole consumer).

## §B — npcmap/ dev classes (4) + InstallCommand decouple
1. **Delete** `npcmap/{NpcMapCommand, NpcMapEntry, NpcMapInit, NpcMapStorage}.java`.
   **KEEP** `npcmap/NpcPresetRefreshManager.java` (shipping — optionally move it to `install/` and update
   its 5 shipping importers; leaving it in `npcmap/` is fine).
2. `fabric.mod.json` — remove `"…npcmap.NpcMapInit"` from `entrypoints.main`.
3. `install/InstallCommand.java` — decouple from the dev classes (it hard-references them):
   - Remove imports **lines 14–16** (`NpcMapEntry`, `NpcMapInit`, `NpcMapStorage`). **KEEP line 17**
     (`NpcPresetRefreshManager`).
   - Remove the npc-map status block in `cmdCheck` — **lines 133–137** (`NpcMapStorage storage = …` +
     the "Legacy npc-map storage: …" `sb.append`).
   - Remove the npc-map replay block in `cmdRun` — **lines 583–618** (the
     `// Legacy: also apply anything registered via the npc-map dev tool` block through its
     `sendSuccess(…)`). The main preset refresh above it stays; `NpcPresetRefreshManager` is still used.

## §C — CutsceneRecorder (KEEP the playback rig)
1. **Delete** `cutscene/CutsceneRecorder.java`.
2. `cutscene/CutsceneCommands.java` — remove the `record` subtree (**≈ lines 46–71**, the
   `.then(Commands.literal("record") … )` chain calling `CutsceneRecorder.add/undo/clear/status/save`).
   **KEEP** the `play`/scene playback (line 24+). CutsceneInit/Manager/Script/State all ship.

## §D — Shrine dev commands (DECISION: strip once shrine paths are final)
`command/CobblemonInitiativeCommands.java` — the `shrine <id> test` (lines 368–383) and
`shrine <id> path record|here|clear|show|export` (lines 385–410) subtrees + their handler methods
(`shrineTest`, `shrinePath*`, ≈ line 1178+). The `path record` tool authored the shrine challenge safe-paths;
strip once those are baked. (Keep if you want runtime shrine debugging.)

## §E — Dev data resources
1. **Delete** `data/cobblemon_initiative/devtest/` (`counter_teams.json`, `placement_plan.json`).
2. `data/cobblemon_initiative/smoketest_items.json` — harmless (compiler-generated). Optional: delete it
   **and** drop the `smoketest_items` emit stage in `scripts/content_compile`, or leave it.

## §F — carpet test dep (after smoke-testing)
`build.gradle.kts:110` — remove the `modRuntimeOnly("maven.modrinth:carpet:…")` line. It is runtime-only
(never bundled in the jar or mrpack), so low urgency; remove once the headless harness is retired.

## §G — Docs / wiki scrub (already annotated with removal flags)
- `wiki/Commands.md` — remove the DEV flowchart node + the dev-tooling section + the npc-map section.
- `docs/ARCHITECTURE_OVERVIEW.md` — drop the `(dev)` annotations, the two dev-only entrypoint rows
  (DevToolsInit / NpcMapInit), and their subsystem-table rows.

## §H — Version bump (LAST)
`build.gradle.kts` — `0.5.0-alpha.N` → `1.0.0`.

---

## Verify after stripping
- `gradle build` must pass (catches any missed dangling import — most likely in InstallCommand or the mixin).
- `scripts/test_harness --static` then a `runServer` boot to `Done` with no ClassNotFound / entrypoint errors.

## Not part of the jar (informational)
`scripts/{smoke_frontier, smoke_towns, smoke_auto, test_harness, mc_rcon.py}` are dev/CI harness scripts,
**not** bundled in the mod jar — keep them as a regression harness or delete from the repo; not a release blocker.
