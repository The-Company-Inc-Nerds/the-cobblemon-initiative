# Testing Toolkit

## Status — BUILT & verified 2026-07-11

All three layers are implemented and run green against the real map.

| Component | File | Run | Gate? |
|-----------|------|-----|-------|
| Static ref validator | `dev/cobblemon_validation/content_integrity.py` | `python3 dev/cobblemon_validation/content_integrity.py` (add `--no-packs` to prove the catch) | yes (exit≠0 on unresolved model/texture) |
| Headless harness | `scripts/test_harness` | `scripts/test_harness [--static\|--client]` | yes (static + server phases) |
| Offline placement | `scripts/npc_placement_audit.py` | `python3 scripts/npc_placement_audit.py [--source trainers\|dialog\|both]` | no (informational) |
| Dialog lint | `scripts/dialog_lint.py` | `python3 scripts/dialog_lint.py [--quiet]` (see section below) | yes (exit≠0 on DEAD/ORPHAN/NEGATION) |
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

## Dialog lint — offline dead/shadowed/unreachable dialog analyzer

`scripts/dialog_lint.py` (pure static, no server, sub-second) — finds the bug class the
band-tag gating system breeds: dialog entries that can never be shown, entries whose
winning window is clipped by a higher-priority entry on an axis they don't gate on
(e.g. villain_grunt_2's `contraband` only winning below 3 badges), buttons whose
conditions can never pass, tags referenced but granted nowhere, and `NOT_EQUALS`
authoring errors (the engine ignores the Operation field — contains() only).

```bash
python3 scripts/dialog_lint.py             # full report (INFO capped at 60 on stdout)
python3 scripts/dialog_lint.py --quiet     # errors + warnings only
python3 scripts/dialog_lint.py --json PATH # report path (default dev/dialog_lint_report.json)
```

What it models (exact engine semantics, from ENGINE_FINDINGS §2/§3 + EASY_NPC_REFERENCE):

- **Selection ladder**: entries with `Priority >= 0` whose Conditions all pass, sorted
  Priority DESC then Label ASC, first wins. `Priority: -1` = manual-only (must be opened
  by an `OPEN_NAMED_DIALOG` action, `easy_npc dialog open`, or an npcsight profile —
  the linter resolves those references, scoped per NPC via preset_map.json uuids /
  entity tags).
- **PLAYER_TAG = contains() only**; negations ride `no_<X>`, numeric gates ride the
  band tags — both derivation families are parsed live from
  `function/dialog/band_tags.mcfunction`, so the simulation couples `X`/`no_X` and
  drives every `<obj>_gte/lt/eq_N` band from its source objective (badges =
  `memory_fragment` 0..10).
- **Action gates** count only via the doubled `ConditionDataSet:{ConditionDataSet:[…]}`
  key; a bare `Conditions` list on an action is flagged (silently ignored by the engine).
- **Grant discovery**: `tag … add` across functions/presets/configs, onwin strings,
  `npcsight meettag`, quest-helper trailing tags (`cobblemon-initiative turnin/trade …
  <tag>`), and Java string literals.

Severities: ERROR (`DEAD_ENTRY` / `ORPHAN_TAG` / `NEGATION_BUG`) gate the exit code —
usable as a pre-commit/pre-build check like the trainer validator. WARNING
(`UNREACHABLE_BUTTON`, `MISPLACED_ACTION_GATE`, `PRIORITY_ASSUMED`) and INFO
(`SHADOWED_RANGE`, `UNREACHABLE_ACTION`, `AUTO_DEAD_NAMED_ONLY`) don't gate; SHADOWED
findings print the winning-state slice per entry so a human judges intent. Known
approximations (documented in the JSON `semantics` block): per-entry exact enumeration
falls back to pairwise entailment above 300k states (findings then marked
`approximate`); orphan tags simulate as free variables; the bottom-of-ladder
unconditional fallback entry is exempt from SHADOWED (losing to specifics is its job);
the compiler's mirrored beaten-say/battle action pair is exempt from
UNREACHABLE_ACTION.

Run it after any dialog-src compile or hand edit to presets/band_tags. Acceptance
canary: it must flag villain_grunt_2's dead `default` entry (ERROR) and the
`contraband` overshadowing at 3+ badges (SHADOWED, `wins only at 0..2`) — TODO §minor
polish tracks the content fix.

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

---

## Evidence recorder + auto-smoke (added 2026-07-12)

Three attach-only tools on top of the RCON layer (`scripts/mc_rcon.py`). None of them
boot a server — they attach to the running dev server (127.0.0.1:25575/devtest) and
fail/SKIP with a clear message when nothing is up. All output lands under
`dev/evidence/<YYYYMMDD-HHMMSS>/` (gitignored).

### `scripts/evidence_tour` — camera-circuit screenshot galleries

Drives an already-connected GUI client player (op + spectator + `tp … facing …`) through
a JSON list of camera stops and captures each with `grim` (Wayland, whole screen — the
game must be the frontmost window; run `caffeine` first so hypridle's lock doesn't eat
captures).

```bash
# shoot the bundled 3-landmark demo (self-bootstraps dev/evidence/tours/example_tour.json)
scripts/evidence_tour run --player <GuiPlayerName>

# shoot a custom circuit
scripts/evidence_tour run --player Cole --spec dev/evidence/tours/my_tour.json

# auto-generate a spec covering every town's placed NPCs (parses
# ambient/placements.mcfunction latches, clusters them, N camera spots per cluster,
# camera = spot + (-5,+3,-5) looking at the spot)
scripts/evidence_tour derive-npcs --towns 3        # -> dev/evidence/tours/npcs_tour.json
scripts/evidence_tour run --player Cole --spec dev/evidence/tours/npcs_tour.json
```

Spec stops: `{label, camera:[x,y,z], look_at:[x,y,z]|"EntityName"|"@e[...]", wait_s, caption}`.
Output: numbered `.png` shots + `index.md` (caption/coords/timestamp/relative links) +
`report.json`; the folder path prints last. Exit nonzero if any stop failed to capture.

### `scripts/record_clip` — short review clips of cutscenes/gimmicks

Wraps `wf-recorder` (whole output — focus the game window first), optionally fires one
RCON trigger 1s in, records N seconds, SIGINTs the recorder cleanly, verifies the mp4.
Graceful error if wf-recorder is missing (add it to the dev shell).

```bash
scripts/record_clip --seconds 30 --out rift_intro \
    --trigger 'cutscene play rift_intro {player}' --player Cole
# no trigger = plain screen clip, no server needed
scripts/record_clip --seconds 15 --out gym7_gimmick
```

Output: `dev/evidence/clips/<ts>_<name>.mp4` (path printed; duration probed via
ffprobe/ffmpeg when present).

### `scripts/smoke_auto` — every proven headless verification, one command

Eight phases, each PASS/FAIL/SKIP with evidence; continues on failure; exit nonzero if
any FAIL. Report: `dev/evidence/<ts>/smoke_report.md` + `smoke_report.json`.

| Phase | What it asserts | How |
|---|---|---|
| static | content_integrity + validate_trainers | `scripts/test_harness --static` |
| console | install verify/check, test reload/registry/data | RCON; gate on `[TEST] … FAIL` |
| caps | 22→80→85 (champion)→100 (board) ladder | `dev badges/grant` SOURCE-visible "level cap now N" (levelcap itself is player-directed, invisible over RCON) |
| canary | tbcs battle path end-to-end | givemon + invisible armor-stand anchor + `tbcs attach rctmod:stadium_wave_1` + `tbcs battle`; FAIL on "is not attached"/"Failed to validate"; sent-out-mon probe as bonus evidence |
| sweep | live NPC placement (full map) | `scripts/npc_live_sweep.py` subprocess; gate on hard embeds |
| safari | badge gate, 1500 fee, lure spawn, ball confiscation | `cobbledollars set fee+900` → enter → `cobbledollars query` (console-visible; abbreviates ≥1000, so the post-fee remainder is kept exact) → scatter → `data get entity @e[tag=ci_safari_lure…]` → exit → `clear <bot> cobblemon:safari_ball 0` |
| stadium | a wave battle actually starts | `stadium start 25` → poll for a pokemon entity near the bot (≤24s) → `stopbattle` voids the run server-side |
| logsweep | no NEW forbidden log lines | pattern diff vs `--log-baseline` (default: pre-run snapshot); self-inflicted feature-detect "Unknown or incomplete command" probes are discounted |

```bash
scripts/smoke_auto                        # everything (sweep takes a while)
scripts/smoke_auto --skip-sweep           # fast pass
scripts/smoke_auto --only caps,safari     # iterate on specific phases
scripts/smoke_auto --mark                 # + comment results into the in-mod smoke
                                          #   checklist (console→0.6, caps→2.5/2.8,
                                          #   canary→4.1; COMMENT only, never pass/fail)
scripts/smoke_auto --log /tmp/runserver.log --write-log-baseline dev/evidence/log_base.json
```

Gotchas baked in: RCON concatenates output lines WITHOUT newlines (parsers split on
stable prefixes, never line breaks); Carpet canonicalizes bot names (`smokebot` →
`SmokeBot` — matching is case-insensitive); the RCON socket occasionally drops on a busy
dev server (one silent reconnect, then the phase FAILs and later phases SKIP); the bot
is spawned once, reused across phases, and reset + killed at the end of the run.
