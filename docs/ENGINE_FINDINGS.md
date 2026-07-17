# Engine Findings & Session Reference

Living reference for Claude sessions (and humans) working on this repo. Everything in
§2 was verified from the DECOMPILED pinned jars or vanilla bytecode — trust it over
intuition, wiki pages, or mod documentation. When something here names a file or flag,
re-verify it still exists before relying on it.

Companion documents: `docs/VERIFICATION_RUNBOOK.md` (round-by-round bug history +
canary lists), `TODO.md` (work plan), `CLAUDE.md` (project overview),
`docs/EASY_NPC_REFERENCE.md` (**the full Easy NPC 6.25.0 config grammar** — every enum
+ NBT tag/type/default, decompiled from the pinned jar; consult it before authoring any
new NPC field this doc's §2 doesn't already cover).

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
| Preset import replaces the ObjectiveDataSet WITHOUT unregistering the old set's live goals (`readAdditionalObjectiveData` → `setObjectiveDataSet` + `registerCustomObjectives`; goal instances are cached per entry and never persisted, and vanilla `GoalSelector.removeGoal` is identity-based, so the remove-before-add inside `addOrUpdateCustomObjective` is handed a NEW instance and misses the old one). A mid-follow import therefore ORPHANS the running `FollowLivingEntityGoal`: `objective remove follow player` then soft-fails (sendFailure + return 0, NO throw — brigadier's result callback still reports success for any non-throwing command, so verify with `result > 0`), and the 300-tick `refreshCustomObjectives` only iterates current entries — orphans are NEVER cleaned in-session (goals aren't saved to NBT, so a relog clears them). Same goal class serves FOLLOW_PLAYER / FOLLOW_OWNER / FOLLOW_ENTITY_BY_UUID | This is why Mom kept following after her dialog opened: her first-join walk-up raced the auto-install preset repaint. Fix pattern (0.5.0-alpha.12): detach sight-manager follows BEFORE any preset import (`NpcSightManager.teardownPursuit`), and purge surviving `FollowLivingEntityGoal`s straight from `Mob.goalSelector` (`MobGoalSelectorAccessor` mixin) at stopFollow + after each confirmed `applyNow` import — safe because no shipped preset carries a FOLLOW objective and the escort re-arms on its ~100-tick cadence |
| `FollowLivingEntityGoal` TELEPORTS the NPC to the player at ≥12 blocks instead of walking | Keep approach/pursue sight ranges ≤ 12 |
| Easy NPC always defers CLOSE_DIALOG to run LAST regardless of action order, and it maps to `closeContainer()` — vanilla's client handler then `setScreen(null)` UNCONDITIONALLY | A dialog button that opens another screen (e.g. Cobblemon's starter UI) has that screen destroyed in the same tick. Fix: drop the close action (a replacing screen emits no close packet) — or defer the open by a tick via a tag + tick function |
| Client DEFAULT-preset index is classpath `/data/easy_npc/default_preset/default_preset.index` | Our `preset.index` is repo bookkeeping only; Easy NPC never reads it for DATA presets |
| `COBBLEMON_ENTITY` renderer (`easy_npc:cobblemon_npc` entity type, `RenderData.EntityModel` = species id): species-only, PERMANENTLY — RenderDataEntry serializes exactly Type/EntityType/EntityModel; `ResourceLocation.tryParse` rejects `growlithe hisuian=true`; the client ghost PokemonEntity's aspect entity-data is never written (server delegate only), so even set aspects would not display. 6.25.0 is the newest upstream build ANYWHERE (Modrinth-checked 2026-07-04); no version has aspect support. Resolution = `PokemonSpecies.getByIdentifier` — ANY namespace, datapack species included, no `implemented` filter; unknown ids poison a static invalid cache until client restart | Forms need a RENDER-ONLY CLONE SPECIES (shipped 2026-07-04 for the Hisuian Growlithe stand-in): `data/cobblemon_initiative/species/custom/growlithe_hisui.json` (Hisui stats hoisted, `implemented:false` = out of random/giveall/dex-GUI; no dex_entries; bare-name lookups are hardcoded to the `cobblemon` namespace so nothing else can ever hit it) + `assets/cobblemon_initiative/bedrock/pokemon/resolvers/growlithe_hisui/` binding its empty-aspects variation to the hisuian model/texture. CAVEAT: those assets ship in AllTheMons, NOT base Cobblemon — without the pack the stand-in falls back to the gray substitute model (cosmetic only). Authoring: in-world exports (`dialog-src/visuals/<id>.npc.snbt`) or the species-only `cobblemon_model` character key |
| **Multi-text dialogs show ONE page, uniformly at random, per dialog open** (`DialogData.getText` picks a random entry from `Texts[]`; there is no sequencing, no "seen" memory, no weighting) | A `say[]` list is a ROTATION, not a monologue: every line must stand alone. NEVER author sequential/ordered prose across one entry's `say[]` — a viewer gets exactly one of them per open. Sequences ride `open_dialog` page chains. ~398 of ~500 shipped entries already rotate; this is the documented contract for all of them |
| **`preset import data` onto an EXISTING uuid'd body silently ignores vanilla entity `Tags`** (verified live 2026-07-17: Feng's preset shipped `Tags:["hz_granary"]`, the applied body carried none — the Miller Walk survey sensor could never fire). Only `import_new` spawns from the full NBT, so latch-placed NPCs DO get their tags | entity_tags on uuid'd characters must be applied OUTSIDE Easy NPC: generate_npc_function surfaces preset Tags per-uuid into `preset_map.json` and NpcPresetRefreshManager `addTag`s them after each confirmed import (version hash covers tags, so tag edits roll out like preset edits) |
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
  **SERIES GRAPH CYCLE = StackOverflow CRASH AT WORLD START (round 12b)**: rctmod's
  `SeriesManager$SeriesGraph.initCount` recurses the requiredDefeats graph with NO cycle
  guard — any cycle hard-crashes the server the moment a player in that series loads.
  The graph edges come from BOTH `mobs/trainers/single/*.json` AND `mobs/trainers/
  groups/*.json` (floor-trainer prereqs — the round-10b rotation audit fixed singles but
  MISSED groups; takehara/hua_zhan/mystic groups carried the same one-town rotation,
  closing the loop takehara_apprentice→trainer_1→[group]hua_zhan_leader→…→
  takehara_apprentice). Latent until round 10 wired initialSeries (empty series = graph
  never built). RULE: any requiredDefeats edit must re-run the cycle check over
  singles+groups (group file `<town>_trainer.json` applies to `<town>_trainer_N` ids);
  correct pattern = gym-N floor/apprentice gate on leader(N-1), gym 1 gates on nothing.
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
  ROUND 12 (2026-07-05): the heal ALSO zeroes `globalSpawnChanceCached` +
  `globalSpawnChanceMinimumCached` (rctmod trainer spawning off — already guaranteed by
  our shipped `spawnWeightFactor:0` trainer JSONs, now doubly so). NET: rctmod's
  per-world `serverconfig/rctmod-server.toml` is FULLY IRRELEVANT — every setting we
  care about (over-leveling, series, cap, spawning) is forced at runtime on ANY world.
  This is why serverconfig doesn't need to "move to overrides": it's per-world (Forge
  Config API Port ignores SERVER configs placed in global `config/`), and it no longer
  matters what a map swap resets it to. The other serverconfig files (easy_npc/* render
  support + skin templates, cobblemon/main.json) are stock defaults with no edits we
  depend on. Global `config/` (security.cfg, shop seed) is the layer that ships in
  `mrpack/overrides/` — that split (per-world serverconfig self-healed; global config
  overridden) is the standalone contract.
- `tbcs` is its OWN mod; `tbcs battle` calls rctapi `BattleManager.startBattle` directly
  and **bypasses all rctmod gating** (requirements, defeat memory, cooldowns, level
  caps). rctmod's native locks + fail dialogs exist only in its spawned-TrainerMob flow,
  which this pack disables (`globalSpawnChance=0`; `initialSeries` is
  `"cobblemon-initiative"` since round 10, for graph bookkeeping only) — and
  enabling it would fight our `LevelCapManager`. → Battle locking stays in Easy NPC
  action Conditions + onwin defeat tags. tbcs wins never register in rctmod.
- **TBCS REGISTRY IS NAMESPACE-PREFIXED — tbcs command ids MUST be `rctmod:<id>`**
  (bytecode, round 12c — this is why dialog battles NEVER worked in any build ≤ alpha.17):
  TBCS keeps its OWN rctapi instance (`RCTApi.initInstance("tbcs")`) and populates it in
  `TBCS.loadTrainers()` by (a) registering world-dir `trainers/` files as `tbcs:<file>`
  and (b) MIRRORING every TrainerNPC from the rctmod instance under
  `IdUtils.createTrainerId("rctmod", id, NPC)` = lowercase `rctmod:<id>` (config
  `trainerMods=["rctmod"]`, interface default the same). `CommandsContext`
  tryAttach/battle resolve via `TrainerRegistry.getById` — an EXACT `Map.get`, no
  prefix fallback — so a bare id throws `No such trainer registered` and the dialog
  falls through to the beaten line. Our 95 `data/rctmod/trainers/*.json` register fine
  (mod-jar packs ARE scanned; "Model validation failure" warns do NOT block
  registration — rctapi registers BEFORE `errors.check()` throws, invalid moves are
  just dropped). RULE: `tbcs attach rctmod:<id>` / `tbcs battle … vs rctmod:<id>`;
  tag names (`defeated_<id>`) and `rctmod player add progress` ids stay BARE.
  `content_compile`, all battle snippets and `_battle_trainer_base` emit the prefix
  since round 12c. Loading is tick-gated in `ModCommon.onServerTick` (reload-aware,
  clearNPCs + re-mirror every datapack reload — never register into the "tbcs"
  instance ourselves, it gets wiped; never ship a world-dir `trainers/` folder).
- **TBCS BATTLE PRECONDITIONS + RULES ARG (runtime + bytecode, 2026-07-12):**
  (a) `tbcs battle … vs rctmod:<id>` REFUSES unless the trainer is **attached to a live
  loaded entity** ("X is not attached to an entity") — the compiled gym engage
  functions run `tbcs attach` on the leader's body first; entity-less battles (Stadium
  waves) attach to a summoned invisible armor stand and sweep it afterwards. (b) The
  `rules <snbt>` argument parses via TagParser → `getAsString()` → Gson, so **bare
  booleans DIE in transit**: `true` → ByteTag `1b` → `Boolean.parseBoolean("1b")` =
  false, silently. **Quote them**: `rules {adjustPlayerLevels:"true",…}` survives the
  round-trip (this is how the Stadium bracket level-lock works — `BattleFormat`'s
  GEN_9_SINGLES singleton `setAdjustLevel(n)` + string-quoted rules flags; reset the
  singleton after the battle registers, it backs every gym battle). (c) A PLAYER
  participant given as a bare name goes through TBCS's trainer NAME registry (misses
  Carpet fake players) — dispatch as the player and use `@s` (entity resolution).
  (d) rctmod's `stopbattle <player>` force-ends a battle WITHOUT firing
  BATTLE_VICTORY/BATTLE_FLED — anything tracking battles by event needs a
  BattleRegistry liveness check (StadiumManager's IN_BATTLE tick does this).
- **EASY NPC BASIC TRADE MATCHES ITEM COMPONENTS (jar-verified 2026-07-12, TradingUtils.getItemCost):**
  the trade recipe `buy`/`sell` items are read as full 1.21 ItemStacks (`components:{…}`
  preserved; legacy `tag` auto-migrated). `getItemCost(stack)` → if the stack's component
  patch is EMPTY it builds `new ItemCost(item, count)` (matches ANY stack of that item),
  but if the stack has components it builds `ItemCost(item, count, DataComponentExactPredicate.allOf(stack.getComponents()))`
  — so the buy cost then requires those EXACT components and a bare item is REJECTED.
  ⇒ a physical "scrip" currency = plain item + `minecraft:custom_data`/`custom_name`
  components in the recipe; vanilla paper won't satisfy it. Recipe NBT is the new codec
  (`{id, count, components:{"minecraft:custom_data":{…}, …}}`), not `{Count, tag}`. This is
  how the wheat-trader scrip works (snippet `presets/snippets/trade/trade_wheat_trader.snbt`).
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

### JourneyMap 6.0.0 (`journeymap-fabric-1.21.1-6.0.0`) — client waypoint API (round 12e)

- The v2 API ships INSIDE the mod jar (`META-INF/jars/journeymap-api-fabric-2.0.0-1.21.1.jar`);
  the official maven artifact `info.journeymap:journeymap-api-fabric:2.0.0-1.21.1` on
  `https://maven.blamejared.com` is byte-identical to it (verified). `modCompileOnly` +
  loom remap; never shade; JM stays in `suggests` (mod id for isModLoaded: `journeymap`).
- PLUGIN DISCOVERY on Fabric: `FabricLoader.getEntrypoints("journeymap",
  IJourneyMapPlugin.class)` — a fabric.mod.json entrypoint key. The class must ALSO be
  annotated `@journeymap.api.v2.common.JourneyMapPlugin(apiVersion="2.0.0")` (the
  common one — `v2.client.JourneyMapPlugin` logs a deprecation warn) and implement
  `journeymap.api.v2.client.IClientPlugin`. JM RE-INSTANTIATES the class reflectively
  (public no-arg ctor, side-effect-free — it is constructed twice). Lazy entrypoints =
  clean soft dep, but NO other class may reference the plugin class (classloading).
- WAYPOINTS: never implement the `Waypoint` interface (PluginWrapper hard-casts to
  ClientWaypointImpl) — always `WaypointFactory.createClientWaypoint(modId, BlockPos,
  name, "minecraft:overworld", persistent)`. `persistent=false` = session-only, never
  written to the player's waypoint file. Factory defaults: random color (setColor!),
  beacon+map+in-world+label all ON. `IClientAPI.addWaypoint(modId, wp)` is create AND
  update; identity key = `wp.getGuid()` (random; `getId()` is derived, not a key).
- LIFECYCLE: do NOT create waypoints in `initialize()` (the client waypoint factory is
  installed after plugin init); safe window = after `MAPPING_STARTED`
  (`ClientEventRegistry.MAPPING_EVENT`). Every dimension change/world exit WIPES all
  non-persistent waypoints and re-fires MAPPING_STARTED — re-create desired waypoints
  in that handler, no exit cleanup needed. WaypointStore is unsynchronized: call the
  API only on the client main thread.

### Vanilla 1.21.1

- **Sidebar hides `#`-prefixed score holders** (`PlayerScoreEntry.isHidden`); cap 15
  visible rows. Displayed rows must ride non-`#` fake players → we use `q.*`.
- `execute store success` = thrown-CommandSyntaxException only; soft-fail (return 0)
  still stores 1. Use `store result` when the command returns a meaningful int.
- Unset scoreboard scores FAIL every `matches` test — zero-init before `matches 0`.
- `performPrefixedCommand` is void and swallows command errors (suppressed source);
  the honest success signal is `CommandSourceStack.withCallback((success, result) -> …)`.
- Bash `sort` is locale-dependent — generators use `LC_ALL=C` to match Python `sorted()`.

### Verified 2026-07-10 (alpha.9–.11 round: gimmicks, rift dragon, cutscene, MF bake)

- **TBCS GEN_9_MULTI takes EXACTLY two participants per side** (builderParticipants
  recurses per `actorsPerSide`, no optional slots) — a lone player vs two trainers is
  NOT expressible; the player's second slot must be an AI partner trainer id:
  `tbcs battle GEN_9_MULTI @s rctmod:<partner> vs rctmod:<a> rctmod:<b> onwin {…}`.
  Token numbering SHIFTS across four actors (winners-first): WIN @1=player @2=partner
  @3=trainer @4=trainer_2; LOSS @1=trainer @2=trainer_2 @3=player @4=partner. The
  compiler's battle emitter handles the shift; author via battle.trainer_2/partner.
- **Easy NPC EntityAttribute tag keys are PascalCase** (`NoGravity`, `CanFloat` —
  `getTagName()` = convertToPascalCase; EASY_NPC_REFERENCE's camelCase claim is wrong)
  AND `EntityAttribute.NoGravity` is a display/record field only on the preset-import
  path — the FUNCTIONAL flag is the vanilla ROOT `NoGravity`, applied by `Entity.load`
  at the end of `importPresetData` (not in the volatile-strip list). A floating body
  needs BOTH (`float: true` in the character JSON bakes both, post-world-merge).
  NoGravity holds a body where it is; it never lifts it — tp it up once.
- **Safe zones do NOT suspend Nuzlocke death.** `isInSafeZone()` is only consulted for
  the Dark Urge whisper; faint-damage and the whiteout `player.kill()` fire inside safe
  zones too. A no-death area (the Stadium) needs an explicit flag guarded in
  NuzlockeInit's faint/whiteout paths.
- **Overworld Ender Dragon**: flies fine with a null EndDragonFight; its flight anchor
  (`fightOrigin`) defaults to world 0,0 and is NOT persisted — set it from Java every
  tick (`RiftDragonManager`). End crystals heal it entity-locally (+1 HP/10t from the
  nearest within 32 blocks, any dimension) but grant no invulnerability. mobGriefing
  gates its ONLY block-destroy path (already false). Overworld death spawns no
  portal/egg/gateway; `/kill` is a clean teardown; `checkDespawn` is a no-op (it
  persists forever — always remove it on abort). Immune to ALL status effects.
- **Map Frontiers**: frontiers live in `<world>/mapfrontiers/frontiers.dat` (gzip NBT,
  root name "", `{Version:10, frontiers:[…]}`), loaded at SERVER_STARTING and shipped
  whole in the JOIN handshake — reflective live creation is post-handshake and never
  syncs (the old relog requirement). build_mrpack now GENERATES the file from
  install.json zones (`bake_mapfrontiers_frontiers`, validated field-identical to a
  bridge-created file); `install run` skips creation when any global frontier exists
  (`MapFrontiersBridge.hasExistingFrontiers` — an in-memory count; NEVER file-exists,
  MF writes an empty frontiers.dat on every fresh boot). One malformed frontier entry
  (bad `id` UUID / bad `dimension`) aborts loading everything after it AND re-saves the
  partial set — generators must emit guaranteed-valid values.
- **Dialog gates: one `score` condition per entry.** The gate object is JSON — a second
  `score` key collapses in parsing and unknown keys are CompileErrors. Different keys
  (defeated/tag/not_tag/score) AND together; two numeric thresholds cannot.
- **Live spectator swaps are hardcore-safe** (`changeGameModeForPlayer` has no hardcore
  guard; hardcore only forces spectator in the DEATH path) — the cutscene rig rides
  this. `/spectate` requires the player already be SPECTATOR and drives the camera to
  a MOVING entity; restore game mode (auto-resets camera) then teleport (position is
  NOT restored by setGameMode).
- **Cutscene keyframe y is FEET-level: the rendered camera sits ~1.78 above it**
  (verified 2026-07-16). The client renders a spectated entity from `pos + eyeHeight`
  (`Camera.setup`), and the rig is a full-size armor stand (`EntityType.ARMOR_STAND`
  eye height 1.7775 since the 1.20.5 dimensions consolidation). `CutsceneManager.play`
  spawns the rig AT the player's eye and `CutsceneRecorder.add` also captures
  `getEyeY()`, so record→play rides ~1.78 HIGH — fine for aerial pans (how it slipped
  through), badly high for close-ups of small subjects (E2E framebuffer capture of the
  starter row confirmed: subjects at the very bottom of frame; the showrunner's
  empirically-tuned y=127.6 close-ups → rendered eye 129.4 corroborate the constant).
  AUTHORING RULE: set keyframe y = desired camera eye − 1.78 (≈ where your FEET would
  be), and compute pitch/yaw from the +1.78 eye point. Air-verify the EYE path, not
  the keyframe path. Pre-alpha.22 scenes authored/recorded eye-level (opening,
  leader_intro, shadow_watcher, shrine/blossom/rift) render ~1.78 high — retune on
  next visual pass rather than changing the engine (an engine fix re-frames every
  tuned scene at once).
- **AutoInstall is version-aware** (alpha.9+): the world latch stores `modVersion`; a
  bump re-applies only the idempotent content refresh (NPC repaint + register_sight)
  on rejoin — same version never re-fires (bump `build.gradle.kts` per content release,
  or delete `<world>/data/cobblemon_initiative_autoinstall.json` for dev iteration).

## 3. Project invariants (don't regress these)

- **Negation = inverse band tags.** `not_tag X` compiles to `PLAYER_TAG EQUALS no_X`;
  `function/dialog/band_tags.mcfunction` (auto-generated, tick-wired) maintains
  `no_<X>` for every negated tag PLUS `no_defeated_<id>` for every file in
  `data/rctmod/trainers/` (so hand-built NPCs can gate on any trainer).
- **Command lowering** (`scripts/content_compile dialog_cmd`): as_player → bare, `@p→@s`,
  legacy `execute as … run` prefixes stripped, execute-rooted = CompileError; entity
  path → `@s/@p→@initiator`, bare `function` wrapped, `@initiator[` = CompileError.
  Generated NPC-context commands (attach) use `command_action_raw` to skip substitution.
- **NPC SIGHT is TAG-KEYED for placement NPCs** (round 13e): the old npcsight
  registration was uuid-keyed, so placement-spawned bodies (random uuids) needed a
  manual per-world `npcsight add <uuid>`. Now the compiler emits tag profiles
  (`data/cobblemon_initiative/npcsight_profiles.json`) from characters with a `sight`
  block + `entity_tags` + no uuid; NpcSightManager discovers live entities by tag each
  tick (session cache keyed by runtime uuid, evicted on despawn). New PASSIVE mode =
  raycast + write `can_see_player` only, no action (sentries/auditors — the quest tick
  reads the scoreboard). The sight tag must be UNIQUE to the behaviour (a shared group
  tag like `deng_camp` would match every body carrying it) — use `sight.tag` to pick, or
  it defaults to the first entity_tag. uuid NPCs still use register_sight.mcfunction.
- **ON_DISTANCE triggers execute COMMAND actions per-player** (round 13b bytecode):
  bands NEAR 16 / CLOSE 8 / VERY_CLOSE 4 / TOUCH 1.25 (hard-coded radii); ExecAsUser
  binds the in-range player like a dialog button. ActionGroup de-dupes per band and
  RESETS ONLY WHEN THE RADIUS EMPTIES — a lost forced battle re-forces on re-approach;
  an armed trigger will NOT re-fire without a full leave+re-enter (the eavesdrop
  direct-open in loiter_ready exists because of this). OPEN_NAMED_DIALOG trigger
  actions ignored authored gates until round 13b (compiler now attaches the doubled
  ConditionDataSet).
- **`easy_npc objective <selector> set|remove follow player <name>`** mutates a LOADED
  NPC's goals live (round 13c, Deng escort): Prio 1, speed 0.7, 2-16 engage band,
  NATIVE teleport catch-up past 12 blocks; a relog leaves the goal holding a dead
  player instance — re-arm on a ~100-tick cadence (walk_tick/rearm pattern). A preset
  import mid-follow ORPHANS the live goal (unremovable by command — see the 6.25.0
  facts table); the armed-refresh path purges orphans after each import.
- **Poses** (round 13c bytecode): `ModelData.Pose: CUSTOM` + per-part
  `Rotation:[x,y,z,locked]` (radians, absolute; `locked` is never read — [0,0,0,1.0]
  = freeze-at-neutral idiom), `Position` (px, additive, +y down), `Scale`. Vanilla anim
  cancels only when ALL 5 critical parts (Head/arms/legs) carry a change; leave Head
  unchanged to keep look-tracking. `DefaultPose:"SITTING"` does NOTHING for humanoids —
  sitting = CUSTOM legs -1.6 rad. Body yaw is renderer-level: even frozen statues pivot
  toward players. Compiler: a dialog-src/visuals override now OWNS ModelData through the
  world merge (mirrors the skin rule); character `entity_tags` bake vanilla Tags into
  the preset (replaces every manual tag-at-placement protocol).
- **Every dialog entry gets an auto-`Goodbye` close button** unless it has a close
  action or sets `"no_goodbye": true` (forced encounters).
- **Action gates use the DOUBLED key `ConditionDataSet:{ConditionDataSet:[…]}`** —
  `ActionDataEntry.load` reads ONLY that key (round 12c bytecode + EASY_NPC_REFERENCE
  §ActionDataEntry); a bare `Conditions` list on an ACTION is silently ignored (that
  key exists only on dialog ENTRIES and BUTTONS, which is why entry/button gating
  always worked while every action-level gate — beaten-line `say`s, one-time gives —
  fired unconditionally until round 12c). `content_compile` emits the doubled key for
  action gates since round 12c; entries/buttons keep bare `Conditions`.
- **Sidebar**: objective `ci_quest`, displayed holders `q.main` + `q.side_*` (slots
  100, 81..57; 75 vacant — 57 = Preferred Provider clinic, round 12c). The quest
  TRACKER (round 12e, `questtrack/QuestTrackManager`) re-styles the tracked holder's
  display every 5 ticks via `ScoreAccess#display` (aqua "▶ " prefix, existing component
  preserved — macro-rendered lines keep their live numbers); render.mcfunction rewrites
  win any race and the tracker re-applies. q.main lines already start with "▶ " so
  tracking main shows no visual change (by design). Active-quest list = holders with a
  ci_quest score, sorted DESC — never re-derive quest conditions for the list., scratch stays `#…` on `quest_hud`. No quest boss bar —
  `quest/load` actively deletes `cobblemon_initiative:objective`; countdown pitches
  need fresh dedicated bossbar ids. Slot **75** = the FILING DAY aggregate
  (`q.side_papers`, 2026-07-06): at `ci_papers_held ≥ 2` (recomputed by
  `sidequest/personnel_file/papers_tick`) the five individual "…to Lucian" deliver
  lines suppress themselves and one `• File with Lucian: n papers` line replaces them.
- **Randomness invariants (2026-07-06, codified after the quest-flow review):**
  (1) **No random prices anywhere** — the instability index (`cd_instability`) is the
  ONLY price driver (shop tiers, heal fee proposals, stall quotes all derive from it);
  never add price jitter. (2) **Numbers a player commits money against are never
  rolled** — stakes, decline fees, and loss fees stay fixed and are printed on the
  button/receipt before the click. Randomness is welcome ONLY on bonus/wares/flavor
  surfaces (rumor rotation, say[] pages, drip loot), never on punishment or committed
  amounts.
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
- **Map speed buff / host-player reset / SPAWN Y**: the map author baked an infinite
  Speed I into level.dat's `Data.Player` (inherited by the host because playerdata/ is
  empty). Round 12c: `bake_install_into_level_dat` REPLACES `Data.Player` with a minimal
  sanitized host tag (`Pos:[SpawnX+.5, SpawnY, SpawnZ+.5]`, Rotation from SpawnAngle,
  zero Motion, overworld, survival, DataVersion — deliberately NO UUID/inventory/XP/
  effects, so the builder state still never ships). WHY A TAG AND NOT DELETION (the
  round-8 approach): vanilla 1.21.1 IGNORES SpawnY for a brand-new player —
  `ServerPlayer.<init>` → `adjustSpawnLocation` → `PlayerRespawnLogic.
  getOverworldRespawnPos(level, x, z)` scans DOWN from the MOTION_BLOCKING heightmap
  top of the spawn column (even with spawnRadius=0), which put the host on the house
  ROOF at y=122. `PlayerList.load()` applies level.dat's `Data.Player` verbatim to the
  singleplayer host BEFORE the first tick — the tag is the only clean way to land an
  exact interior spawn. Belt-and-braces: `InitiativeInit` snaps un-latched players
  (`ci_spawn_snapped` tag) to shared spawn on JOIN for dev/bare-mod worlds. build also
  strips UPM2's 76 stale `data/rctmod/trainers/` from the bundled data.zip (~83 startup
  warns). `install run` still runs `effect clear @a minecraft:speed` for already-shipped
  copies. Running Shoes (+30% movement_speed) are the only sanctioned speed source.
- Economy: sidequest payouts route through `economy/payout {amount:N}` (skew +
  UNBRANDED "Verified Rate" receipt — tone rule 2026-07-06) — never call `pay_macro`
  directly (needs paid+rate+raw). The Company-branded receipt is `economy/
  payout_company {amount:N}` and fires ONLY where taking Company money is the point:
  census sign fork, courier sell fork, Invitational purse, Adjusted Retail. Battle
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
  point of the plot). `sq_lucian_deliveries.json` is a MERGE FILE (not compiled
  standalone — nothing references it). `sq_perf_review_guide.json` was RETIRED
  2026-07-06: its entries live in `dialog/takehara_guide.json`, the Takehara-only
  guide fork (the other nine guides keep the shared `gym_guide`).
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
- **Pack settings** (`mrpack/settings.json`, tracked, 2026-07-05): human-facing build
  config — `packName`, `packSummary`, `packSlug` (→ `.mrpack` filename), `packVersion`
  (null = mod version from build.gradle.kts), `mapName`, and optional
  `minecraft`/`fabricLoader`. Precedence: CLI flag > settings.json > modpack.json >
  gradle files > default. `mapName` renames the bundled world's save folder AND sets its
  in-game display name (`level.dat` `LevelName`, set in `bake_install_into_level_dat`) —
  only when exactly one world is bundled. Keeps display config out of the mods manifest;
  `modpack.json` stays the mods/packs list.
- **Dep cache + map updates** (`build-mrpack`, 2026-07-05): `--cache` downloads every
  dependency jar once into `mrpack/cache/` (content-addressed by sha1, gitignored) and
  BUNDLES them into `overrides/` with an empty manifest `files[]` — the installed pack
  re-downloads nothing but our own jar (rebuild+reinstall test loop stops re-pulling
  ~93 mods + AllTheMons). Map updates: a bundled world's per-player state
  (`playerdata/stats/advancements/cobblemon*playerdata/pokedex/session.lock`) is now
  STRIPPED at build (`WORLD_USERDATA_STRIP`); `level.dat` is REBAKED from `install.json`
  (never hand-preserve it — add new level.dat settings to install.json + extend the
  bake); NPCs travel in the export's `entities/` + `easy_npc/` (411 `*.npc.nbt`), and
  preset content is applied at RUNTIME, not baked — so dropping a builder's full export
  in wholesale + rebuild is the whole workflow (see `mrpack/maps/README.md`). Block-only
  merge (terrain-only export, keep current NPCs) = copy `region/poi/DIM*` across.
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
  - *New NPC skin* (jar-verified round 11b): SkinData types — `DEFAULT` (built-in
    `HumanoidSkinVariant` enum: STEVE/ALEX/SECURITY_01/KNIGHT_01/PROFESSOR_01/…),
    `PLAYER_SKIN` (Mojang name/UUID), `CUSTOM` (LOCAL FILE), `SECURE/INSECURE_REMOTE_URL`.
    REMOTE URL is limited to a hardcoded 4-domain allowlist (`UrlValidator`:
    minecraftskins.com / novaskin.me / mcskins.top / skinmc.net) AND the host must
    actually serve the raw 64×64 PNG — minecraftskins.com hotlink-protects and serves a
    ~300-byte blank stub to the game, so URL skins from it silently render blank (this
    is why the courier URL "didn't load"). RELIABLE = **CUSTOM local skin**: PNG at
    `config/easy_npc/skin/<skinmodel>/<dashed-uuid>.png` (skinmodel = lowercased
    `SkinModel` enum, e.g. `humanoid`), keyed by UUID; preset carries
    `SkinData{Type:"CUSTOM", UUID:[I;i0,i1,i2,i3]}` (the same UUID as int-array). The
    `Content` base64 field is NOT rendered in 6.25 — must be a file. **rctmod ships 1560
    ready 64×64 trainer skins** at `assets/rctmod/textures/trainers/single/*.png`
    (grunt/scientist/gentleman/worker/boss/team_* — perfect Company villains): extract
    one, drop it at `config/easy_npc/skin/humanoid/<uuid>.png`, reference by UUID
    (`skin:{type:"custom","uuid":[…]}`). Skins ship via `mrpack/overrides/config/…`
    (cosmetic → mrpack-only is acceptable; bare-mod shows DEFAULT). content_compile
    `skin_node` handles `type:"custom"`/`"url"`; world merge: an explicit character `skin` FULLY wins over the builder — skips both the world SkinData AND VariantType (showrunner rule 2026-07-06), so a specified skin is never overridden by a builder variant.
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
  **15 (start) / 22 / 30 / 37 / 44 / 50 / 56 / 62 / 68 / 74 / 80 (gym10) / 85 (Champion)
  / 100 (Board cleared → the Founder)** is canon across levelcaps.json +
  ProgressionConfig.baseLevelCap(15) + HUD + wiki + CLAUDE.md — always move the teams.
  Start cap 15 gates pre-gym-1 evolutions (Totodile→Croconaw @18). Endgame: Champion
  ace target ~85 (its team is unauthored — empty stub); the Founder is a SINGLE level
  **100** mirror; Battle Frontier is post-game in the 85–100 band.
- **LEVEL-CAP LINCHPIN (round 11 bug fix)**: `updateLevelCap` keys on
  `progress.hasAchievement(<levelcaps achievementId>)`, but `onTrainerDefeated` never
  wrote a trainer's `achievementOnDefeat` to `earnedAchievements` — so EVERY badge/
  champion cap step was dead (cap frozen at base). Now granted in `onTrainerDefeated`
  (Set, idempotent, before `updateLevelCap`). The 100 rung rides a derived achievement
  `board_cleared`, granted in `checkMultiTrainerAchievements` when all `board_member`
  trainers are defeated (board members carry no per-trainer achievement, and the
  Founder's own `company_overthrown` fires too late — after the fight). Only ids in
  levelcaps.json move the cap, so granting achievementOnDefeat broadly is safe.

**Shelved**: JEI disable-by-default (`.jar.disabled` not honored in the launcher
install test; `build_mrpack.py` keeps `"disabled": true` support).

**Known-cosmetic**: 86 RCT trainer "Model validation failure" warns (bad genders/
abilities/moves), mega_showdown items on 3 Royal League trainers, `ninetales_alola`
invalid species on gym-9 Skadi (may silently drop a team member).
