# Engine Findings & Session Reference

Living reference for Claude sessions (and humans) working on this repo. Everything in
§2 was verified from the DECOMPILED pinned jars or vanilla bytecode — trust it over
intuition, wiki pages, or mod documentation. When something here names a file or flag,
re-verify it still exists before relying on it.

Companion documents: `docs/VERIFICATION_RUNBOOK.md` (round-by-round bug history +
canary lists), `TODO.md` (work plan), `CLAUDE.md` (project overview).

---

## 1. The prime directive: verify from jars, never guess

Two release rounds were wasted "fixing" the Easy NPC preset-import argument order by
guessing from parse-error cursors. Every real fix in rounds 4–7 came from bytecode.

**How to inspect (base system has NO unzip/python/curl):**

```bash
cd "<repo>" && nix develop -c python3   # zipfile / urllib / json
nix develop -c javap -p -c <extracted>.class
```

- Runtime mod jars: `.gradle/caches/modules-2/files-2.1/` (curse.maven + maven.modrinth
  coordinates pinned in `build.gradle.kts`).
- Mods NOT compile-pinned (TBCS, rctmod, JEI…): pinned filename in `mrpack/modpack.json`
  → resolve via `https://api.modrinth.com/v2/project/<slug>/version` → download to /tmp.
- Mojmap-named vanilla jar (for MC API checks):
  `.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/1.21.1-*/…jar`
  (the `-intermediary` sibling has obfuscated names — wrong jar).
- Kotlin mods (CobbleDollars): commands live in a kotlin `object`; read the constant
  pool with `re.findall(rb"[\x20-\x7e]{4,}", data)` first, then javap the handler.

## 2. Bytecode-verified engine facts

### Easy NPC 6.25.0 (`Epm6R3P2:pxt6JAIU`)

| Fact | Consequence |
|---|---|
| `preset import data <loc> [<x y z> [<uuid>]]` — UUID only accepted AFTER a position | The one valid update-in-place template: `execute as <uuid> at @s run easy_npc preset import data <loc> ~ ~ ~ <uuid>` (no-op when unloaded) |
| `PresetSecurity.validateResourceLocation`: DATA presets must be namespace `easy_npc`, path prefix `preset/`, extension `.npc.snbt/.npc.nbt/.npc.json` — HARDCODED, no config | Presets ship at `data/easy_npc/preset/<type>/<name>.npc.snbt` |
| Import updates in place ONLY for loaded entities (`LivingEntityManager` = loaded set); unloaded UUID → NEW entity at preset Pos / 0,0,0 | Bulk install-time refresh is impossible → `NpcPresetRefreshManager` re-imports per NPC on chunk load |
| ALL `execute`-rooted ExecAsUser commands silently blocked (`isParseSuccessful` rejects redirect chains) | as_player commands must be BARE; guards go in action `Conditions` |
| ExecAsUser source IS the player (`player.createCommandSourceStack()`, elevated to action `PermLevel`, default 2) | `@s` binds to the player; no wrapper needed |
| Entity path (no ExecAsUser): `@initiator` is substituted with the player NAME | `@initiator[tag=…]` NEVER parses (name+selector-args invalid); `tag <Name> add x` works |
| `PlayerTagCondition.evaluate` = `tags.contains(name)` — the Operation field is IGNORED | `NOT_EQUALS` means "HAS the tag". Negations ride derived inverse tags `no_<X>` (see §3) |
| Empty `executeAsUserCommandAllowList.*` in security.cfg blocks EVERY ExecAsUser command | The mrpack ships populated allowlists; existing instances need the file copied in |
| An NPC with an EMPTY ObjectiveDataSet can NEVER receive its first objective at runtime (`FollowObjective.getObjectiveData` null-returns on `!hasObjectives()`) — and preset import REPLACES the live objective set, even with an empty one | Every compiled character gets a movement objective (default `ambient_stationary_look`); never ship `HasObjectives: 0b`. This silently killed Mom's walk-up AND all 5 pursue trainers |
| `FollowLivingEntityGoal` TELEPORTS the NPC to the player at ≥12 blocks instead of walking | Keep approach/pursue sight ranges ≤ 12 |
| Easy NPC always defers CLOSE_DIALOG to run LAST regardless of action order, and it maps to `closeContainer()` — vanilla's client handler then `setScreen(null)` UNCONDITIONALLY | A dialog button that opens another screen (e.g. Cobblemon's starter UI) has that screen destroyed in the same tick. Fix: drop the close action (a replacing screen emits no close packet) — or defer the open by a tick via a tag + tick function |
| Client DEFAULT-preset index is classpath `/data/easy_npc/default_preset/default_preset.index` | Our `preset.index` is repo bookkeeping only; Easy NPC never reads it for DATA presets |
| `COBBLEMON_ENTITY` renderer (`easy_npc:cobblemon_npc` entity type, `RenderData.EntityModel` = species id): species-only, PERMANENTLY — RenderDataEntry serializes exactly Type/EntityType/EntityModel; `ResourceLocation.tryParse` rejects `growlithe hisuian=true`; the client ghost PokemonEntity's aspect entity-data is never written (server delegate only), so even set aspects would not display. 6.25.0 is the newest upstream build ANYWHERE (Modrinth-checked 2026-07-04); no version has aspect support. Resolution = `PokemonSpecies.getByIdentifier` — ANY namespace, datapack species included, no `implemented` filter; unknown ids poison a static invalid cache until client restart | Forms need a RENDER-ONLY CLONE SPECIES (shipped 2026-07-04 for the Hisuian Growlithe stand-in): `data/cobblemon_initiative/species/custom/growlithe_hisui.json` (Hisui stats hoisted, `implemented:false` = out of random/giveall/dex-GUI; no dex_entries; bare-name lookups are hardcoded to the `cobblemon` namespace so nothing else can ever hit it) + `assets/cobblemon_initiative/bedrock/pokemon/resolvers/growlithe_hisui/` binding its empty-aspects variation to the hisuian model/texture. CAVEAT: those assets ship in AllTheMons, NOT base Cobblemon — without the pack the stand-in falls back to the gray substitute model (cosmetic only). Authoring: in-world exports (`dialog-src/visuals/<id>.npc.snbt`) or the species-only `cobblemon_model` character key |
| Movement objectives (all verified in `ObjectiveType`/`ObjectiveUtils` + custom goals; work identically for `cobblemon_npc` — it extends PathfinderMob): `RANDOM_STROLL_AROUND_HOME` (radius 10/7, ~12s interval, 50% of picks biased toward Home — SOFT tether, no hard leash) silently no-ops without `Navigation:{Home:}`; `MOVE_BACK_TO_HOME` (StopDistance, exact-BlockPos path-back) sets NO goal flags so it coexists/jitters with the stroll goal; `RANDOM_STROLL` + `WATER_AVOIDING_RANDOM_STROLL` ignore Home entirely (unbounded drift); village types + FLEE_* are unusable here (vanilla POIs absent / flees players). SNBT: `Type` = exact UPPERCASE enum (typos silently become NONE), `Prio` MUST be written (missing = 0 = outranks everything), `SpeedModifier` double default 0.7 | Wander = the `ambient_wander` snippet (stroll@2 + back-home@3 + looks@9/10) and the preset MUST carry the NPC's real `Navigation.Home` — preset import REPLACES Navigation, so a template Home would slowly migrate NPCs. A strolling NPC opens dialog fine but KEEPS WALKING while the screen is open — never put wander on quest anchors (door/desk/scene NPCs) or NpcSight-registered NPCs |

### CobbleDollars 2.0.0-Beta-5.1 (`curse.maven:cobbledollars-859232:6604561`)

- Grammar: `pay | query | give | remove | set | reload | leaderboard [update]` + `/cd`
  alias. **There is NO `add`.** `give <targets:players> <amount:bigInt(min 1)>` —
  selector-first, accepts `@s` under execute-as/functions.
- `pay` checks the SOURCE balance BEFORE mutating; self-pay is net-zero (deduct →
  re-read → add on one live field). The fail path **soft-fails** (sendFailure +
  `return 0`, no throw) → `store success` reads 1 either way; **gate on `store result`**
  (0 = broke, amount = paid). This is the paid-heal affordability probe.
- `remove` clamps at 0 (fail-soft), verified in-game round 1.

### TBCS 0.14.1-beta (`tbcs-fabric`) + rctmod 0.18.1-beta + rctapi 0.15.2-beta

- **rctmod level caps (rounds 9-10)**: rctmod's `EXPERIENCE_GAINED_EVENT_PRE` clamp
  derives its cap from the SERIES graph — under the old `initialSeries="empty"` it was
  FROZEN at `initialLevelCap` (15) forever; there is NO command and NO API to set a
  per-player cap (`level_cap` exists only under `/rctmod player get`). Round-10 wiring:
  `initialSeries="cobblemon-initiative"` (the series + 20-node apprentice→leader graph
  under `data/rctmod/{series,mobs/trainers}` always existed — players just never
  entered it), and because tbcs battles NEVER register defeats in rctmod (verified
  twice: defeat recording REQUIRES a TrainerMob-initiated entry in
  TrainerManager.trainerBattles; rctmod ignores rctapi BATTLE_ENDED; tbcs has no
  bridge; no mob-free rctmod battle command exists), our BATTLE_VICTORY handler
  dispatches `rctmod player add progress <playerName> after <id>` for gym
  apprentice/leader wins. GRAMMAR TRAP (cost one silent round): targets go BEFORE
  the before/after literal; `add progress after <id> <name>` is unparseable and
  fails silently under withSuppressedOutput. `after` marks the id + all its graph
  prerequisites (covers trainer_1/2); it writes progress ONLY (no
  TrainerBattleMemory counts, no DefeatCountTrigger) and silently no-ops for ids
  outside the player's current series. ENFORCEMENT STAYS OURS
  (`allowOverLeveling=true` + the InitiativeInit clamp): rctmod's native cap model is
  "next undefeated key trainer's level" (~15-16 pre-gym-1), which cannot express the
  badge ladder (15/22/30…) — re-enabling its clamp would recreate the frozen-cap bug.
  STANDALONE SELF-HEAL (round 11): rctmod's config is a PER-WORLD serverconfig, so the
  bundled map bakes `allowOverLeveling=true`+`initialSeries="cobblemon-initiative"` but
  fresh/dev/bare worlds get rctmod DEFAULTS (`allowOverLeveling=false`,
  `initialLevelCap=15`, `initialSeries="empty"`) → the frozen-cap bug returns.
  `compat/RctmodServerConfig` heals it at SERVER_STARTED by reflection: rctmod's
  `ServerConfig` getters read CACHED primitives (`allowOverLevelingCached`,
  `initialSeriesCached`, `initialLevelCapCached`), NOT the live ConfigValue — so we
  write those `*Cached` fields directly (a `ConfigValue.set()` would be a no-op without
  a following `reload()`). SERVER_STARTED is after the per-world config loads + before
  any join/XP event. `ensurePlayerSeries` (on JOIN) migrates players saved under the
  wrong series; new players are auto-placed by the healed initialSeries. Re-applied
  every server start, so no disk write needed (the bundled toml stays correct as
  belt-and-braces). All field/method names bytecode-verified present. Install command
  does NOT touch rctmod (can't — file-patch is too late in the load order).
- `tbcs` is its OWN mod; `tbcs battle` calls rctapi `BattleManager.startBattle` directly
  and **bypasses all rctmod gating** (requirements, defeat memory, cooldowns, level
  caps). rctmod's native locks + fail dialogs exist only in its spawned-TrainerMob flow,
  which this pack disables (`globalSpawnChance=0`; `initialSeries` is
  `"cobblemon-initiative"` since round 10, for graph bookkeeping only) — and
  enabling it would fight our `LevelCapManager`. → Battle locking stays in Easy NPC
  action Conditions + onwin defeat tags. tbcs wins never register in rctmod.
- **onwin `@1/@2` substitute WINNERS FIRST** (`concat(getWinners(), getLosers())`).
  Win list (key 1): `@1`=player, `@2`=NPC. Lose list (key 2): `@1`=NPC, `@2`=player —
  lose-side commands are the MIRROR (`cobbledollars remove @2`, `@1 say <taunt>`).
  A command starting with `@N` executes AS that entity; elsewhere `@N` becomes the
  player's name (or raw UUID for non-players).
- rctapi refuses to start a battle for an empty team (`insufficientPokemon`) — an
  empty `{}` trainer JSON = a battle button that silently does nothing.
- **`tbcs attach <id> <entity>` = rctapi `TrainerNPC.setEntity(entity)`** (bytecode,
  round 10c) — binds the physical body onto the RCTAPI trainer object (battle
  facing/animation), nothing more. It CANNOT make wins count in rctmod: rctmod's
  ledger (`TrainerManager.trainerBattles`) is typed `List<TrainerMob>`, written only
  by `TrainerMob.startBattleWith` (its own right-click path), and rctmod has zero
  references to rctapi's battle events. Binding happens on the layer rctmod ignores —
  the explicit `rctmod player add progress` dispatch is the only bridge, period.

### Cobblemon 1.7.3

- `/openstarterscreen <player>` is the ONLY form (no no-arg self), perm node
  `cobblemon.command.openstarterscreen` at level 2 (LaxPermissionValidator = vanilla
  levels when no permission mod). If the player already has `starterSelected: true`
  (cobblemonplayerdata/<uuid>.json), it "fails" via sendSuccess-red — invisible under
  suppressed output. `/clearparty` does NOT reset the flag.
- `CobblemonEvents.STARTER_CHOSEN` fires on the actual selection → grants
  `chose_starter` (InitiativeInit) — the ESC-proof latch.
- Pokédex API: `Cobblemon.playerDataManager.getPokedexData(player)` →
  `getSpeciesRecords()` → per-species `SpeciesDexRecord.getKnowledge()`;
  `PokedexEntryProgress.CAUGHT` = registered-as-caught. `DexScoreManager` (dex pkg)
  mirrors the caught COUNT into the `dex_caught` dummy objective every 40 ticks —
  the datapack-visible dex progress signal.
- `hisuian` is a FLAG species feature (`growlithe hisuian=true` in Pokémon
  properties), NOT a `region_bias` value — jar-verified.
- **Join sync order** (the starter-overlay bug class): Cobblemon syncs
  `ClientGeneralPlayerData` during `SYNC_DATA_PACK_CONTENTS` — BEFORE Fabric's
  `ENTITY_LOAD` — via its NORMAL-priority `DATA_SYNCHRONIZED` subscriber
  (`syncAllToPlayer`), and nothing re-syncs afterward. Server-side flag writes for
  join-time client state MUST ride `CobblemonEvents.DATA_SYNCHRONIZED` at
  `Priority.HIGHEST` (+ `playerDataManager.saveSingle(data, GENERAL)`); mid-session
  re-sync = `data.sendToPlayer(player)`. The party "You have not yet selected a
  starter…" message gates on the CLIENT's `starterSelected`/`starterLocked`.
- **Experience events**: every XP path (battle, ALL candies incl. rare candy,
  commands, sidemods) funnels through `Pokemon.addExperience` →
  `EXPERIENCE_GAINED_EVENT_PRE` (mutable `setExperience`, cancelable). Clamp with
  `min(gain, pokemon.getExperienceToLevel(cap))`, never cancel — Cobblemon refunds a
  candy when the applied gain is 0. `LEVEL_UP_EVENT.setNewLevel` is honored (not
  cancelable). Direct `Pokemon.setLevel` bypasses everything (admin only).

### Vanilla 1.21.1

- **Sidebar hides `#`-prefixed score holders** (`PlayerScoreEntry.isHidden`); cap 15
  visible rows. Displayed rows must ride non-`#` fake players → we use `q.*`.
- `execute store success` = thrown-CommandSyntaxException only; soft-fail (return 0)
  still stores 1. Use `store result` when the command returns a meaningful int.
- Unset scoreboard scores FAIL every `matches` test — zero-init before `matches 0`.
- `performPrefixedCommand` is void and swallows command errors (suppressed source);
  the honest success signal is `CommandSourceStack.withCallback((success, result) -> …)`.
- Bash `sort` is locale-dependent — generators use `LC_ALL=C` to match Python `sorted()`.

## 3. Project invariants (don't regress these)

- **Negation = inverse band tags.** `not_tag X` compiles to `PLAYER_TAG EQUALS no_X`;
  `function/dialog/band_tags.mcfunction` (auto-generated, tick-wired) maintains
  `no_<X>` for every negated tag PLUS `no_defeated_<id>` for every file in
  `data/rctmod/trainers/` (so hand-built NPCs can gate on any trainer).
- **Command lowering** (`scripts/content_compile dialog_cmd`): as_player → bare, `@p→@s`,
  legacy `execute as … run` prefixes stripped, execute-rooted = CompileError; entity
  path → `@s/@p→@initiator`, bare `function` wrapped, `@initiator[` = CompileError.
  Generated NPC-context commands (attach) use `command_action_raw` to skip substitution.
- **Every dialog entry gets an auto-`Goodbye` close button** unless it has a close
  action or sets `"no_goodbye": true` (forced encounters).
- **Sidebar**: objective `ci_quest`, displayed holders `q.main` + `q.side_*` (slots
  100, 81..58; 75 vacant), scratch stays `#…` on `quest_hud`. No quest boss bar —
  `quest/load` actively deletes `cobblemon_initiative:objective`; countdown pitches
  need fresh dedicated bossbar ids.
- **Story-flag canon**: gym progress = `memory_fragment` score (1..10, zero-init in
  render); HQ = `defeated_villain_boss`; champion = `royal_league_champion` (defeat_tag
  override — NOT defeated_royal_champion); Board = `defeated_board_{madeline,matt,micah,lauren}`;
  Founder = `company_overthrown` + alias `defeated_villain_final_boss` (granted by
  `reveal/founder_defeated`). Opening chain: `mom_sent_to_lab → chose_starter →
  got_pokedex → got_running_shoes` (+ `met_mom` from sight).
- **NPC content pipeline**: `dialog-src/` → `scripts/content_compile` → presets under
  `data/easy_npc/preset/` + `band_tags` + `register_sight` + npc_presets.json merge →
  (a character with `dialog-src/visuals/<id>.npc.snbt` adopts that export's
  identity/render/sound/attribute fields via `VISUAL_ADOPT` — how non-humanoid,
  e.g. Cobblemon-model, NPCs are authored) →
  `generate_granary_tiers` → `update_preset_index` → `generate_npc_function` (emits
  `update_npc_presets.mcfunction` + `npc/preset_map.json` whose version hash covers
  the mapping AND all preset bytes). **Run in that order**; content_compile auto-runs
  the last step but granary tiers must precede the final hash.
- **NpcPresetRefreshManager** (npcmap pkg — survives the 1.0.0 npcmap strip): bundled
  map + per-world `data/npc_preset_refresh.json`; re-imports each mapped NPC on chunk
  load when the content version changes; success confirmed via withCallback; granary
  tier overrides are STICKY (survive armFullRefresh; refreshes re-import the CURRENT
  tier, never base). `install run` arms it and reports honestly.
- **Dialog entry selection**: highest Priority whose conditions pass; single-grant =
  gated ENTRY + ungated button (never a condition-locked button); `open_default`
  re-evaluates the ladder. Mom's shoes entry rides priority 45 (> warming 30/worry 40).
- **Nalia approach**: NpcSight `approach_once` (range 12 — the walk/teleport threshold)
  + persistent `fired` latch + `stop_tag met_mom` (storage-reset-proof). `npcsight add`
  never resets `fired`. `handleDialog` mode is NOT a ranged auto-open (hard-capped at
  dialogRange+1≈4 blocks, ignores stopTag, transient latch).
- **Starters (showrunner design 2026-07-04)**: NO vanilla starter screen — InitiativeInit
  marks every joining player starterPrompted+starterSelected (Cobblemon
  `playerDataManager.getGenericData(player)`), and Acacia's "Choose a partner" button
  spawns three stand-in starter NPCs at fixed lab-side coords (entity-path
  `preset import_new … 2675.5 128 2899.5` etc., once via `ci_starters_spawned`).
  Trio (showrunner-picked): Skiddo / Totodile / Hisuian Growlithe (`growlithe
  hisuian=true`). Stand-ins render via COBBLEMON_ENTITY visuals adopted from the
  showrunner's in-world exports (`dialog-src/visuals/starter_*.npc.snbt`,
  `apply_visuals`/`VISUAL_ADOPT` in content_compile) — the Growlithe stand-in shows
  the BASE form (renderer limitation, §2), the given Pokémon is genuinely Hisuian.
  **Dex-unlock ladder** (5-entry dialog priority ladder per starter): first pick
  Lv5 (+`chose_starter`+`claimed_starter_<id>`); second partner Lv25 at
  `dex ≥ 15` (+`second_starter_claimed`); last partner Lv40 at `dex ≥ 30`
  (+`third_starter_claimed`); claimed → cry-only; waiting → cry + hint. `dex` is a
  compiler numeric gate (`LOGICAL_OBJ` → `dex_caught`, maintained by DexScoreManager,
  band tags `dex_gte_15/30`). Acacia's post-pokedex dialog carries the 15/30 hint.
  (`dialog-src/{dialog,characters/sango,visuals}/starter_*`).
- **Ambient life (showrunner pass 2026-07-04)**: towns get life two ways. (1) WANDER
  FLIPS: `movement.objective: ambient_wander` on non-anchor civilians only (kofi,
  sq_uncle_marlow, elder_nuru, museum_sayuri) — NEVER on door/desk/scene quest anchors
  (lane doors 1–3, stall merchants, nurses, mayor scene) or sight NPCs; Home comes from
  merge_world (world NBT Navigation.Home == placement for all placed NPCs). (2)
  TOWNSFOLK COMPANIONS: 10 owned Pokémon (`companion_*` characters, `cobblemon_model`
  key → COBBLEMON_ENTITY render via `dialog-src/visuals/_cobblemon_template.npc.snbt`;
  cry+flavor dialog, auto-Goodbye).
- **PLACEMENT LATCHES (generalized 2026-07-04, round 9)**: ANY character with a
  `placement: {x,y,z}` field and NO uuid is spawned once per world by GENERATED
  functions (`write_placements` in content_compile → `ambient/placements_init`,
  `ambient/placements`, `ambient/place/<key>`): `#amb_<key>` on `ci_ambient`
  (key = id minus `companion_`, preserving pre-generator world latches), proximity
  gate `@a[x,y,z,distance=..40]` (guarantees loaded chunks), latch set BEFORE
  import_new (never re-fires). Redo one: kill the body +
  `scoreboard players set #amb_<key> ci_ambient 0`. Compiler WARNS on every act-1
  character with neither uuid nor placement. Ints are auto-centered (+0.5 x/z).
  Spawned NPCs get CustomName from display_name in apply_visuals; they are NOT in
  preset_map.json → content updates don't propagate to already-spawned copies.
  KNOWN GAP: latch-spawned entities get RANDOM uuids, so npcsight registrations
  ("uuids filled at placement" in villain route/checkpoint comments) still need a
  manual `npcsight add <uuid>` pass after first spawn — sight arming is NOT automatic.
  Coords are showrunner marks — nudge the `placement` field if one clips furniture.
- **Level caps are enforced by US (round 9)**: `EXPERIENCE_GAINED_EVENT_PRE` clamp +
  `LEVEL_UP_EVENT` floor in InitiativeInit, cap = `LevelCapManager.getLevelCap`
  (badge ladder from levelcaps.json, base 20). rctmod's competing frozen-15 clamp is
  disabled via `allowOverLeveling=true` in the world's rctmod-server.toml — keep
  those two in the same release (lower cap wins while both are live).
- **Starter stand-ins (round 9)**: every offer entry carries a "Keep looking" close
  button (the choose button's close action suppresses the auto-Goodbye, so without
  it players couldn't browse the trio); the choose click also tags the NPC
  entity-path (`ci_claimed_standin`) and `ambient/tick` kills tagged stand-ins ONE
  TICK LATER (immediate kill would race the deferred CLOSE_DIALOG packet). The
  hisuian stand-in renders via the `cobblemon_initiative:growlithe_hisui` clone
  species (see §2) — the GIVEN starter stays `growlithe hisuian=true`.
- **Map speed buff**: the map author baked an infinite Speed I into level.dat's
  `Data.Player` (inherited by the host because playerdata/ is empty). Stripped at
  build time (`_strip_player_speed` in build_mrpack — also forces survival), staged map
  cleaned 2026-07-04 (incl. level.dat.bak — a re-staged map export would reintroduce
  it, the bake covers that), and `install run` runs `effect clear @a minecraft:speed`
  for already-shipped copies. Running Shoes (+30% movement_speed, walking ≈ vanilla
  sprint) are the only sanctioned speed source.
- Economy: sidequest payouts route through `economy/payout {amount:N}` (skew +
  actionbar receipt) — never call `pay_macro` directly (needs paid+rate+raw). Battle
  prizes stay flat in onwin. `hq_stabilize` clamps DOWNWARD only.
- **Sidequest training packs (showrunner 2026-07-04): every one-time quest COMPLETION
  payout also grants a training loot table** (`npc_gift/training_{minor≤260,
  standard 300–400, major 500–600, grand=refile-4000-only}` — exp candies; vitamins
  enter the economy ONLY via major/grand). Rules: NEVER on repeatables/dailies/stages
  (derby win_repeat, cascade win_gold, sprint win_daily, ledger stages, rx_button) —
  no farm loops in hardcore Nuzlocke; fork mirrors get equal tiers (perf-review
  ghost=sweep, pending-review SIGN stipend = elder_sentinel refuse kit); the villain
  SELL fork (courier 900) and consolation forks (Marlow 150) deliberately get NONE.
  Dialog-side = bare `loot give @s loot …` action (allowlisted root) before the payout
  action. Gym leaders pay CD prize + config command rewards only — the legacy
  5×emerald config entries were REMOVED (off-economy currency; CobbleDollars is the
  point of the plot). `sq_lucian_deliveries.json` + `sq_perf_review_guide.json` are
  MERGE FILES (not compiled standalone — nothing references them); perf_review_guide
  carries its training_major for whenever the Takehara guide dialog gets forked.
- Advancement icons use the HYPHEN namespace (`cobblemon-initiative:badge_icon`) —
  items live under the mod id, datapack under `cobblemon_initiative`.
- **The mod must work WITHOUT the mrpack.** Anything the mod's correctness depends on
  cannot live only in `mrpack/overrides/`. Enforced case: `EasyNpcSecurityConfig`
  (compat pkg) merge-patches `config/easy_npc/security.cfg`'s ExecAsUser allowlists at
  `onInitialize()` (Easy NPC reads the file lazily on the first button press, so
  mod-init patching precedes the first read — no restart). `install check` reports the
  allowlist state. The mrpack override remains as belt-and-braces + the showrunner's
  extra relaxations. If a future feature needs a foreign mod's config, patch it the
  same way — never assume the pack delivered it. KNOWN GAP (TODO §1.B): the
  CobbleDollars bank re-theme (nether_star backing) exists only in the live instance's
  `config/cobbledollars/bank.json` — not in the repo, not in the mrpack; needs
  capturing + install-time seeding like the shop tiers.

## 4. Workflows

- **Release**: fixes → regenerate (order above) → `gradle build` → bump `version` in
  `build.gradle.kts` (0.4.x-alpha.N) → `GIT_COMMIT_MSG` (NO Co-Authored-By trailer)
  → user runs `gcommit` → `build-mrpack` → fresh-instance install test (config
  overrides like `easy_npc/security.cfg` only reach FRESH installs).
  **Versioning (showrunner rule 2026-07-04): EVERY build-verified change round bumps
  the alpha suffix (or patch) and rebuilds under the new name — one jar name per
  round so dev/log-<version> maps to exactly one change set. The MINOR version bumps
  only after a 100% smoke-test pass (showrunner's call).**
- **Install-command pre-baking** (`build-mrpack --with-map`, since 2026-07-04): the
  bundled world COPY gets install.json's gamerules + difficulty + hardcore baked
  straight into `level.dat` (scripts/nbt_write.py, round-trip byte-verified), and the
  badge_0 shop catalog ships as `config/cobbledollars/default_shop.json`. Combined with
  the mod's self-healing (NPC preset refresh auto-arms on fresh worlds; NPC Sight
  auto-seeds register_sight when its storage is empty; security.cfg patcher), a fresh
  pack install opens READY. `install run` stays idempotent and safe to re-run any time.
- **Pack-only first-join auto-install** (the marker pattern — NOT a companion mod):
  build_mrpack drops `config/cobblemon-initiative-autoinstall.json` into the pack
  overrides; `AutoInstall` (install pkg) sees the marker at SERVER_STARTED and, once
  per fresh world (latch `data/cobblemon_initiative_autoinstall.json`, written BEFORE
  dispatch so it can never loop), runs the full `install run` ~2s after the first join
  — covering the remaining live-server pieces (zones → safeZones, Map Frontiers, which
  needs a player as frontier owner). Bare-mod installs have no marker → nothing
  auto-runs, preserving the standalone contract. The install kick is hardcoreFlipped-
  gated (pre-baked worlds are already hardcore → no first-join kick). Use this marker
  pattern for any future "pack behaves differently" need — never a second jar or a
  second build variant of the mod.
- **WIRING RECIPES (how to add more of what shipped 2026-07-04):**
  - *New placed NPC (no in-world body needed)*: character JSON with
    `placement: {x,y,z}` (+ `dialog`, movement, optional `battle`) → compile → done.
    The generated latch spawns it on first approach. If the showrunner later places a
    real body instead, add `uuid` — placement is then ignored automatically.
  - *New townsfolk Pokémon / companion*: same, plus `cobblemon_model: "<species>"`
    (bare registry id — `mrmime` not `mr_mime`) + a cry/flavor dialog. Wander =
    `movement.objective: ambient_wander` + `home` (never on quest anchors).
  - *New FORM-rendered NPC (hisuian/galarian/etc.)*: clone-species recipe (§2 Easy NPC
    renderer row): species JSON under `data/cobblemon_initiative/species/custom/`
    (`implemented:false`, stats hoisted from the form) + a resolver under
    `assets/cobblemon_initiative/bedrock/pokemon/resolvers/<id>/` pointing at the
    form's model/texture (check whether base Cobblemon or AllTheMons ships them) +
    `cobblemon_model: "<clone id with namespace>"`… NOTE the character key takes the
    bare id only — for a namespaced clone use a `dialog-src/visuals/` file with
    `EntityModel:"cobblemon_initiative:<id>"` (how starter_hisuian_growlithe does it).
  - *New gym/battle NPC*: rctapi team file (`data/rctmod/trainers/<id>.json`, `name`
    MUST equal the TrainerConfig displayName — the kalahar swap miscredited wins) +
    TrainerConfig entry + graph node (`mobs/trainers/single/<id>.json`, requiredDefeats
    = previous chain link) + character with `battle.trainer` + body (uuid/placement).
    Series progress + badge + prize wire themselves (BATTLE_VICTORY name match →
    onTrainerDefeated + `rctmod player add progress <name> after <id>`).
  - *New quest reward*: money via `economy/payout {amount:N}`; add a training pack
    (`loot give @s loot cobblemon_initiative:npc_gift/training_<tier>`) ONLY at the
    one-time completion latch — tier by payout (≤260 minor / 300–400 standard /
    500–600 major); grand is reserved.
- **Verification**: every round appends a results table + next canary list to
  `docs/VERIFICATION_RUNBOOK.md`. Current: **Round 8 canaries** (sidebar shows lines;
  lose-fee charged + NPC speaks taunt; ESC-proof starter; nurse declines broke
  players; champion/Board/Founder ladder via forced tags).
- **Debugging in-game reports**: get the full PrismLauncher log (dev/log-*), grep past
  the data-fixer noise, check `Failed to load function` + `Blocked …` lines first.
  Remember Easy NPC suppresses dialog-command feedback (enableDebug in dialog_options
  turns it on) — "nothing happened" often means "refused silently".
- **Audits that caught real bugs** (rerun after big changes): every `no_*` used in
  presets ⊆ band_tags maintained set; zero `NOT_EQUALS` / `@initiator[` / execute-
  rooted-ExecAsUser in presets+snippets; lose lists contain no `remove @1`/`@2 say`;
  world-bundle NPC nbt scan (`mrpack/maps/*/easy_npc/npcs/`) for stale patterns.

## 5. Current progress & blockers (as of 2026-07-04, v0.4.3-alpha.2 unreleased)

**Working (code-complete, runtime-verified through round 6):** preset import + refresh
pipeline, dialog buttons (allowlist + bare commands), payouts (`give`), battle button
gating, pokedex/opening chain logic, install flow + branded disconnect, chest-announce
toggle, sidebar HUD wiring for ~22 quests (needs the Round 8 visual check).

**Content frontier: authored + compiled through Hua Zhan City (gym 2 — Act 1 beats
1–2, first three towns).** Everything past that (Mystic Marsh onward, wheat war
escalation, HQ raid, Royal League, shrines, Board/Founder) has code plumbing but no
authored content yet — the items below are staged by WHEN their act arrives, not
today's to-do list:

1. Near-term (in-scope now): Round 8 runtime verify of towns 1–3; place the three
   compiled-but-unplaced NPCs (dead-letter checkpoint agent, memo courier, census
   wagon); then beat-3 quest design (next towns per TODO §0 Phase 1).
2. Badge-7 era: **field guards** — only farm_1 wired → `fields_liberated` maxes at 1;
   HQ-raid gate (4), trader escalation (2/4), relief tiers (2/4), granary ambush (4)
   all unreachable until placed + wired. **villain_boss (Acting CEO DJ) trainer JSON**
   + admins/grunts 3-11 (18 missing files).
3. Post-league era: **20 empty `{}` trainer teams** (royal_champion, royal_elite_1..4,
   5 shrine leaders, 10 shrine cultists) + board_×4 files.
   `content_compile` warns on all 28 battle references to missing/empty teams — those
   warnings are EARLY flags on pre-wired future-act presets, not current breakage.
4. Gyms 3–10 rosters (audited round 10b): each ships only 4/7 roles — **24 team files
   missing** ({town}_{trainer_3,trainer_4,jr_apprentice} × 8), all referenced by their
   gym configs + npc_map_template, and each gym's apprentice prereq chain runs through
   the missing jr_apprentice. Towns 1–2 are 7/7 complete. Bodies:
   gym-2..10 LEADERS and every gym's trainer_1–4 have no uuid/placement yet
   (takehara jr/apprentice/leader are the only fully-bodied gym battles).

- **GYM BALANCE RULE (showrunner 2026-07-05, "brutal" hardcore Nuzlocke): leader
  ace = entry-cap + 2** (fight every leader UNDERLEVELED — entry cap is our enforced
  cap, ace sits 2 above it). Whole roster scales in step (uniform level shift preserves
  internal spread; mid-staff apprentice ace ≈ cap, floor trainers below). Applied round
  10e to all 10 rosters against the re-spaced ladder — aces
  17/24/32/39/46/52/58/64/70/76. The 24 future-authored team files MUST follow the same
  rule for their gym. Never re-tune by moving the cap ladder — the enforced ladder
  **15 (start) / 22 / 30 / 37 / 44 / 50 / 56 / 62 / 68 / 74 / 80 / 100 (champ)** is
  canon across levelcaps.json + ProgressionConfig.baseLevelCap(15) + HUD + wiki +
  CLAUDE.md — always move the teams. Start cap 15 gates pre-gym-1 evolutions
  (Totodile→Croconaw @18); this is why 15 not 20.

**Shelved**: JEI disable-by-default (`.jar.disabled` not honored in the launcher
install test; `build_mrpack.py` keeps `"disabled": true` support).

**Known-cosmetic**: 86 RCT trainer "Model validation failure" warns (bad genders/
abilities/moves), mega_showdown items on 3 Royal League trainers, `ninetales_alola`
invalid species on gym-9 Skadi (may silently drop a team member).
