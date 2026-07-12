# Testing Toolkit

## Status — BUILT & verified 2026-07-11

All three layers are implemented and run green against the real map.

| Component | File | Run | Gate? |
|-----------|------|-----|-------|
| Static ref validator | `dev/cobblemon_validation/content_integrity.py` | `python3 dev/cobblemon_validation/content_integrity.py` (add `--no-packs` to prove the catch) | yes (exit≠0 on unresolved model/texture) |
| Headless harness | `scripts/test_harness` | `scripts/test_harness [--static\|--client]` | yes (static + server phases) |
| Offline placement | `scripts/npc_placement_audit.py` | `python3 scripts/npc_placement_audit.py [--source trainers\|dialog\|both]` | no (informational) |
| In-mod test cmds | `command/TestCommands.java` | `/cobblemon-initiative test reload\|registry\|data\|placement` | reload/registry/data yes; placement info |
| Fake player | `build.gradle.kts` (Carpet, dev-only) | `player Tester spawn` then `execute as Tester run …` | enabler |

**Setup gotchas found & fixed:**
- A dedicated server reads `run/<level-name>/`, **not** `run/saves/<level-name>/` — without a symlink it generates a *fresh* world and every terrain check is meaningless. The harness now auto-links it; `level-name=The Cobblemon Initiative` in `run/server.properties`.
- Modrinth deps must be scoped (`content { includeGroup("maven.modrinth") }`) or cursemaven's 500s abort resolution.

**Confirmed findings:**
- `content_integrity` proves the `growlithe_hisui` resolver is hard-dependent on the AllTheMons resourcepack — drop it and the client reload hangs (the original bug). PASS with packs, FAIL `--no-packs`.
- Placement (real map, 318 configured coords): grounded=139, floating=45, sunk=98, unplaced=36 (`[0,0,0]` roaming). The "sunk" are the nominal-Y convention (config feet ≈1 block into the surface); latch-spawn snaps NPCs to ground — confirmed live (`Deka` etc. spawn grounded). So config-coord placement is a data screen; the **fake player** gives authoritative live positions.
- Fake player works end to end: bot spawns at real world spawn, fires the opening cutscene, runs player-scoped commands (`levelcap`→15, `progress`), and its presence latch-spawns the town NPCs so their **live** positions are readable.

---

## Original design

Goal: let an automated agent (or CI) verify as much of the mod as possible **without a
human clicking through the game**, and give a human a much shorter "only you can test
this" list. Grounded in the 2026-07-11 session where the dev client was fixed and driven
headlessly (see [[reference-runclient-nixos-gl-fix]], [[reference-headless-runserver-harness]]).

## Why (the problem)

Two testing channels exist today, each half-blind:

| Channel | Drives via | Sees via | Blind to |
|---------|-----------|----------|----------|
| Headless `runServer` | console commands piped to a FIFO (`/tmp/mc_in`) | server log | anything needing a player; rendering |
| GUI `run-client` | *nothing* — no input-injection tool (xdotool/ydotool/wtype all absent) | `grim` screenshots | interaction; camera control |

The session proved the high-value target: **content/reload integrity**. The client hung
indefinitely because `growlithe_hisui`'s resolver referenced a model AllTheMons ships and
the pack wasn't enabled → Cobblemon's reload threw an NPE that never surfaced to a human as
anything but a frozen loading screen. That entire class of bug is catchable by *booting and
reading the log*. Most of the session's time went into finding it by hand; the toolkit's job
is to make that instant and automatic.

## Build on what exists (do not reinvent)

- **Commands**: `command/CobblemonInitiativeCommands.java` is the Brigadier registrar
  (`Commands.literal("cobblemon-initiative").then(...)`). Subcommands live in dedicated
  classes (`devtools/DevCommands`, `devtools/GymMarkCommand`, `install/InstallCommand`,
  `npcmap/NpcMapCommand`, `npcsight/NpcSightCommand`). Output is
  `source.sendSuccess(() -> Component.literal("§a[Tag] ..."), false)` / `sendFailure(...)`.
  World access works console-side (no player) via `source.getLevel()` → `ServerLevel`,
  `getBlockState(BlockPos)`, and `ServerLevel.setChunkForced(cx, cz, true)` for programmatic
  force-loading — so a console-run `test` command can load its own chunks *before* probing
  (the force-load-timing trap I hit by hand is solved in-code).
- **Static validation**: `dev/cobblemon_validation/validate_trainers.py` already validates
  trainer teams against the Cobblemon 1.7.3 jar using `species_index.json`, `items.txt`,
  `moves.txt`, `abilities.txt`. Extend this, don't clone it.
- **NBT**: `scripts/nbt_read.py` / `nbt_write.py` → an **offline** save/entity audit is feasible
  (no running game).
- **Checklists**: `SMOKETEST.md`, `docs/VERIFICATION_RUNBOOK.md` — the human-facing scenarios
  these tools automate.

## Layer 1 — `test` command surface (in-mod, machine-readable)

New `command/TestCommands.java`, registered as one `.then(...)` node under
`cobblemon-initiative`, mirroring the `devtools/` classes. Every line uses a stable,
greppable contract (no color codes) so a harness can parse it:

```
[TEST] <category> <subject> <PASS|FAIL|WARN> [key=val ...]
[TEST] SUMMARY category=<c> pass=<n> fail=<n> warn=<n>
```

Subcommands (all console-runnable, no player):

- `test reload` — re-read every config (trainers, level caps, shrines, Nuzlocke, noble,
  loot-chest, progression, shop tiers); emit `PASS`/`FAIL <file> <exception>` per group. The
  server-side analogue of the client reload gate.
- `test assets` — walk mod resolvers/models/species references and report any that don't
  resolve against loaded resource/data packs (`FAIL asset=<id> ref=<species>`). This is the
  growlithe catcher, generalized. (Client-side reload is still the ground truth for *client*
  models; see Layer 2.)
- `test placement [all|<id>|<group>]` — for each NPC with config coords
  (`dialog-src/characters/**` → 175 today), `setChunkForced` its chunk, read blocks at
  feet / feet-1 / feet+1, classify `GROUNDED` / `SUNK` / `FLOATING gap=<n>` / `HEAD_BLOCKED`.
  Reliable because the command controls its own chunk loading.
- `test data` — graph integrity: every quest stage → NPC id → coords → trainer reference
  resolves; prereq graphs are acyclic; `quest_waypoints.json` targets exist.
- `test trainer <id>` — team legality at runtime (species/move/ability/item exist in the
  loaded registry), complementing the static `validate_trainers.py`.
- `test registry` — counts vs expectations (trainer count, npc-map size, sight profiles).

## Layer 2 — headless harness (turn manual FIFO work into a runner)

`scripts/test_harness` (shell + python asserts):

1. Point `run/server.properties` at the real map (`level-name=The Cobblemon Initiative`),
   boot `gradle runServer --console=plain --no-daemon` with stdin from a FIFO.
2. Wait for `Done (` (fail fast on startup exception).
3. Pipe a command script (the `test *` suite above) into the FIFO.
4. Capture the log; assert **zero** `[TEST] ... FAIL`, zero reload stack traces, and diff the
   `Unable to load model|Missing textures|does not exist` lines against an allowlist baseline.
5. Also run the **client** once in the lean config (shaders off, AllTheMons only) and assert
   the reload completes (no `CompletionException` / `StitcherException` / hang) — the direct
   growlithe/stitcher gate. Detect "reached title" via log, not screenshot.
6. Exit non-zero on any failure → usable as a pre-`build` / pre-commit gate.

Plus a pure-static pass (no game) reusing `validate_trainers.py` + a new
`content_integrity.py` (validate every `assets/**/resolvers` model/texture/poser id against
the jar + AllTheMons resourcepack zip).

## Placement audit — two implementations (recommend both)

- **In-mod** `test placement` (Layer 1) — authoritative (real world, real terrain, force-load
  safe), needs the map loaded.
- **Offline** `scripts/npc_placement_audit` (`nbt_read.py`) — reads the save's
  `entities/*.mca` (persisted NPCs) + config coords, checks terrain from region files. No
  server, no timing traps, covers all 175 in seconds. Best for a fast pre-commit check;
  cross-checks the in-mod result.

Both must account for the **nominal-vs-runtime Y** question found in-session (config Y often
sits above terrain because Easy NPC latch-spawns snap to ground) — the audit should report the
*measured gap* and flag only implausible ones, not every non-zero gap. Resolving "does it
actually float when spawned" needs Layer 3.

## Layer 3 — player context (the big unlock; has a dependency)

Add a **fake-player** mod (Carpet-style `/player Tester spawn`) to the **dev-only** runtime
(`build.gradle.kts` `modRuntimeOnly`, like the other dev companions). A fake player is a real
`ServerPlayer`, which unlocks, all driven from the console I already control:

- **latch-NPC spawns** — `/tp Tester <coords>` loads the area and triggers latch placement →
  `test placement` can then read *live* spawned positions (resolves nominal-vs-runtime Y).
- **player-scoped commands** — `progress`, `levelcap`, `dev goto|grant|team|badges`, `smoke`,
  `track`, `gym-mark` run against the bot.
- **NPC Sight** — put the bot in an NPC's cone, assert `can_see_player` flips.
- **camera screenshots** — with the GUI client spectating the bot (or the bot's own view via a
  connected quick-play client), `/tp Tester <x y z> <yaw> <pitch>` frames any NPC/build/arena
  → `grim`. Visual *observation* becomes scriptable.

**Decision needed**: which fake-player mod for Fabric 1.21.1 + Cobblemon (Carpet + a 1.21.1
build, or an alternative), and confirm no conflict with the pack's mixins. I'd verify before
committing.

## Capability matrix (after each layer)

| Test category | Today | +L1 | +L2 | +L3 |
|---|---|---|---|---|
| Config/JSON integrity | manual grep | ✅ auto | ✅ gate | — |
| Reload / missing-asset (growlithe class) | by hand | partial | ✅ gate | — |
| Trainer/team legality | static only | ✅ runtime | ✅ gate | — |
| Data graph (quest→npc→trainer) | — | ✅ | ✅ gate | — |
| NPC placement (sunk/float) | manual, error-prone | ✅ reliable | ✅ +offline | ✅ live-spawn |
| Player-scoped commands | ❌ | ❌ | ❌ | ✅ |
| Latch-NPC spawns | ❌ | ❌ | ❌ | ✅ |
| NPC sight cones | log count only | — | — | ✅ |
| Framed screenshots | ❌ | ❌ | ❌ | ✅ observe |
| Dialog *branches* (data) | — | ✅ gate-check | ✅ | ✅ |
| Dialog *clicks* / battle turns | ❌ | ❌ | ❌ | ❌ (human) |

## Phasing & effort

- **P1 — L2 harness + static `content_integrity.py`** (scripts only, no mod code). Highest
  value/effort ratio; catches the growlithe class immediately. ~half-day.
- **P2 — `test placement` + offline `npc_placement_audit`** (the original "in wall/floor"
  ask). Mod command (Java) + one python script. ~1 day.
- **P3 — `test reload|assets|data|trainer|registry`** (rest of Layer 1). Incremental Java.
- **P4 — fake-player (Layer 3)** after the mod-compat check. Unlocks the player tier.

## The honest boundary (still a human)

Even fully built: I can position, observe, assert, and screenshot — I cannot **click**.
Right-click dialog trees, dialog-button UX, walking a shrine, and playing battle turns stay
yours. What shrinks is the list: instead of "test everything," it becomes "these N specific
interactions need a human," with the data/placement/loading around them already green.
