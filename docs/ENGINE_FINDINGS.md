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
| Client DEFAULT-preset index is classpath `/data/easy_npc/default_preset/default_preset.index` | Our `preset.index` is repo bookkeeping only; Easy NPC never reads it for DATA presets |

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

- `tbcs` is its OWN mod; `tbcs battle` calls rctapi `BattleManager.startBattle` directly
  and **bypasses all rctmod gating** (requirements, defeat memory, cooldowns, level
  caps). rctmod's native locks + fail dialogs exist only in its spawned-TrainerMob flow,
  which this pack disables (`globalSpawnChance=0`, `initialSeries="empty"`) — and
  enabling it would fight our `LevelCapManager`. → Battle locking stays in Easy NPC
  action Conditions + onwin defeat tags. tbcs wins never register in rctmod.
- **onwin `@1/@2` substitute WINNERS FIRST** (`concat(getWinners(), getLosers())`).
  Win list (key 1): `@1`=player, `@2`=NPC. Lose list (key 2): `@1`=NPC, `@2`=player —
  lose-side commands are the MIRROR (`cobbledollars remove @2`, `@1 say <taunt>`).
  A command starting with `@N` executes AS that entity; elsewhere `@N` becomes the
  player's name (or raw UUID for non-players).
- rctapi refuses to start a battle for an empty team (`insufficientPokemon`) — an
  empty `{}` trainer JSON = a battle button that silently does nothing.

### Cobblemon 1.7.3

- `/openstarterscreen <player>` is the ONLY form (no no-arg self), perm node
  `cobblemon.command.openstarterscreen` at level 2 (LaxPermissionValidator = vanilla
  levels when no permission mod). If the player already has `starterSelected: true`
  (cobblemonplayerdata/<uuid>.json), it "fails" via sendSuccess-red — invisible under
  suppressed output. `/clearparty` does NOT reset the flag.
- `CobblemonEvents.STARTER_CHOSEN` fires on the actual selection → grants
  `chose_starter` (InitiativeInit) — the ESC-proof latch.

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
- **Nalia approach**: NpcSight `approach_once` + persistent `fired` latch + `stop_tag
  met_mom` (storage-reset-proof). `npcsight add` never resets `fired`.
- Economy: sidequest payouts route through `economy/payout {amount:N}` (skew +
  actionbar receipt) — never call `pay_macro` directly (needs paid+rate+raw). Battle
  prizes stay flat in onwin. `hq_stabilize` clamps DOWNWARD only.
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

**Shelved**: JEI disable-by-default (`.jar.disabled` not honored in the launcher
install test; `build_mrpack.py` keeps `"disabled": true` support).

**Known-cosmetic**: 86 RCT trainer "Model validation failure" warns (bad genders/
abilities/moves), mega_showdown items on 3 Royal League trainers, `ninetales_alola`
invalid species on gym-9 Skadi (may silently drop a team member).
