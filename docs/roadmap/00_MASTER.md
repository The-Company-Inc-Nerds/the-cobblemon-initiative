# 00 — MASTER Content Roadmap (synthesis)

> Synthesis of the 18 design sections in `docs/roadmap/`. This is the index + build plan +
> the end-to-end implementation playbook that ties every section to the real pipeline
> (`scripts/content_compile`, `dialog-src/`, `docs/ENGINE_FINDINGS.md`, `CLAUDE.md`).
> Read a section's own file for the full design; read this for **order, contracts, and how to stamp it**.

Sections (all under `docs/roadmap/`):

| NN | key | effort | one-liner |
|----|-----|--------|-----------|
| 01 | `mainline_spine` | L | The `memory_fragment` clock that silently repoints the HUD act→act toward the mirror reveal |
| 02 | `gym_system_pvp_doubles` | L | The canonical 5-rung gym stamp (2 floors → jr → apprentice DOUBLE → underleveled leader) + tbcs onwin shape |
| 03 | `wheat_war_farms` | L | Ten-field liberation backbone; a majority (6 of 10) gates the HQ raid — **the current hard blocker** |
| 04 | `mystic_marsh` | L | Fairy bog gym 3, the only town where mobs still spawn at night |
| 05 | `deepcore_city` | L | Fighting gym 4; the nether-star currency backing is being mined out on paper |
| 06 | `gaviota_port` | XL | Water gym 5 + a Company fishing marquee + offshore noble Kyogre |
| 07 | `kalahar_reach` | L | Ground gym 6; a boundary stone bears the founder's seal; caravan first names the founder |
| 08 | `cyber_city` | L | Electric gym 7; the keycard hand-off that flips Act 1 → the Act 2 HQ raid |
| 09 | `company_hq` | L | Cyber City skyscraper — keycard-gated basement descent to Acting CEO DJ (B5); CURRENCY STABILIZED (idx→25); corporate upstairs + the player's old penthouse |
| 10 | `ryujin_keep` | L | Dragon gym 8 (post-HQ); frag_8 "You built it"; Rayquaza shrine tease |
| 11 | `nifl_town` | L | Ice gym 9; the cold kept your name on file; frag_9 "They emptied you" |
| 12 | `scorchspire` | XL | Fire gym 10; forge-lord Vulcan + noble Groudon (monument → PvP → wild); frag_10 |
| 13 | `royal_league` | L | Underleveled E4 + Champion gauntlet; cap → 85 |
| 14 | `board_and_founder` | L | Empty four §k board chairs (cap → 100), then the level-100 TRUE MIRROR of the player's live party — in the penthouse, wearing the player's skin, spawned post-Champion (all RESOLVED 2026-07-06) |
| 15 | `battle_frontier` | XL | Post-league sandbox: 6 facilities + hub, Frontier Points, §k Hall-of-Fame ghost record |
| 16 | `legendaries_nobles` | L | Two-archetype legendary system (noble: prompt/monument → PvP → wild spawn; event: click-to-join) + 10-legendary roster (L10 = the Deep-Dark vault noble) |
| 17 | `deep_dark_cave` | XL | OPTIONAL sculk-stealth mega-dungeon to the hollowed reserve vault — open, no lock-in, free exit |
| 18 | `shrines_audit` | M | Five optional element-guardian gauntlets; machinery coded, overlays orphaned, teams empty |

---

> **Main-loop spot-check (2026-07-05) — two headline claims verified before trusting the synthesis:**
> - ✅ **Farm blocker CONFIRMED.** Only `farm_1` is wired anywhere in `function/liberation/` +
>   `dialog-src/` (12 refs; no `farm_2..6`). The HQ gate (`fields_liberated >= 4`) is genuinely
>   impassable today. `wheat_war_farms` at build-order #3 is correct and is the single highest
>   unblock.
> - ⚠️ **"24 missing gym team files" is relative to the PROPOSED 5-rung ladder, not a gap in the
>   current design.** All 8 gyms (3–10) already ship `<town>_{trainer_1,trainer_2,apprentice,leader}`
>   (32 files present). The 24 "missing" (`trainer_3/trainer_4/jr_apprentice ×8`) only exist if you
>   adopt the 5-rung expansion in §02. The base **4-rung** ladder is fully teamed — contents +
>   SINGLES/DOUBLES format still need the audit §02/§Decisions.A call out. Treat the 5-rung ladder as
>   an *option*, not a prerequisite.
> - Other synthesis claims (empty `{}` teams, apprentice-out-levels-leader, two-source leader
>   conflicts, `BattleVictoryEvent name==displayName` cap-100 gotcha) are **plausible but unverified**
>   — confirm against the files before acting.

## 1. Executive summary

**What is left to build.** The mod's engine is code-complete and content is authored+compiled only
through **Hua Zhan City (gym 2)**. Everything after that is *plumbed but unwritten*: gyms 3–10, the
Wheat War, the HQ raid, the Royal League, the Board/Founder, the Battle Frontier, the legendary/noble
set-pieces, the Deep Dark descent, and the five shrines. The roadmap covers all of it across **18
areas** (14× L, 3× XL — `gaviota_port`, `scorchspire`, `battle_frontier`, `deep_dark_cave`; 1× M —
`shrines_audit`). Most areas need **no new Java** — they are dialog-src content, rctmod team JSON, and
generated functions — but four have hard **builder-geometry** dependencies (HQ interior, Deep Dark
sculk build, Gaviota docks/offshore, Scorchspire/Groudon crater) and three need genuinely new
datapack systems (multi-round derby, the noble two-archetype chains, Frontier Points).

**The mainline spine, in one paragraph.** One score, `memory_fragment` (0..10), is both "badges
earned" and "how close you are to remembering you are the Founder"; it silently repoints the quest
HUD act→act with zero quest-accept menus. Sango prologue → **gyms 1–7** (Act 1, `cd_instability`
climbs +8/gym to a peak of 56, `frag_1..7`) → at **frag_7 "You signed this charter"** the sidebar
flips red to **Raid Company HQ [1590 51 1028]** (which outranks gyms 8–10, so the pivot happens
next-door) → the **HQ hard gate**: Acting CEO DJ refuses to fight until **a majority of the wheat
fields (6 of 10) are liberated**, and his defeat fires **CURRENCY STABILIZED** (`cd_instability`→25) → **gyms 8–10** (Act 2
tail, `frag_8,9,10`, idx held at 25) → **Royal League** at `memory_fragment==10` (Champion → cap 85)
→ **Board clearout** (four `defeated_board_*` beats circling a §k name → cap 100) → the **level-100
Founder mirror** that renders the player's own username live (`company_overthrown`) → post-game
**Hunt the Ender Dragon** off the custom map, still hardcore+Nuzlocke.

---

## 2. Cross-area dependency graph + BUILD ORDER

### 2.1 How to read the dependency data

Each section lists `dependencies`. These are **not** strict topological build-blockers — the graph is
densely cyclic (e.g. `mainline_spine` lists its own downstream gates; `cyber_city ↔ company_hq` each
list the other). They encode two relationships:

- **Contract dependencies** — "this area must satisfy a rail the spine/template owns." The spine and
  the gym template are contracts; you build the contract first, then everything that satisfies it.
- **Coupling dependencies** — "these two areas share a tag/coord/hand-off and must be designed
  together" (the keycard, the Cyber-City tower shared by Acts 2–3, the shared noble contract, the
  Zapdos prompt-noble staged in `cyber_city` but contract-owned by `legendaries_nobles`).

So the build order below is a **pragmatic layering**, not a raw topo-sort: foundations first, then
the Act-2 hinge, then the gym road in play order, then the shared engine, then the endgame.

### 2.2 Dependency layers (what unblocks what)

```
FOUNDATION (contracts everything hangs on)
  mainline_spine ......... the rails: HUD gates, frag drip, act hand-offs (mostly DONE; needs tissue)
  gym_system_pvp_doubles . the 5-rung stamp + tbcs onwin shape + cycle-safety (template for gyms 3-10)
  wheat_war_farms ........ THE unblocker: only farm_1 is wired -> fields_liberated maxes at 1 ->
                           the Act1->Act2 HQ gate (>=6, a 6-of-10 majority) is IMPASSABLE until
                           5+ more gate fields land
        |
        v
ACT-2 HINGE (coupled pair, build together)
  cyber_city ............. gym 7; hands the HQ keycard (opens the tower's basement elevator);
                           the Act1->Act2 flip; stages the Zapdos prompt-noble (contract owned by 16)
  company_hq ............. the Cyber City skyscraper (lobby -> B1..B5 basement / corporate upstairs
                           + penthouse); consumes keycard + fields>=6 (of 10); DJ at B5;
                           CURRENCY STABILIZED (idx->25); Dark Urge t3
        |
        v
GYM ROAD (Act 1 gyms 3-6, then Act 2 tail 8-10) -- each is a `gym_system` stamp
  mystic_marsh (3) -> deepcore_city (4) -> gaviota_port (5) -> kalahar_reach (6)
  ryujin_keep (8) -> nifl_town (9) -> scorchspire (10)
        |
        v
SHARED CONTRACT (consumed by gaviota/kalahar/ryujin/nifl/scorchspire set-pieces + shrine crystals)
  legendaries_nobles ..... the two-archetype contract: noble = trigger gate (prompt/monument) ->
                           PvP ladder -> latched wild spawn -> cleanup; event = click-to-join claim
                           (shipped starter stand-in pattern). Build once, drop a noble into each
                           gym's marquee. NEW Java backlog: a capture-event listener for
                           caught_<id> tags (advancement pkg has only TrainerDefeatedCriterion) —
                           blocks the weather-trinity delta capstone only.
        |
        v
ENDGAME LADDER
  royal_league (cap 85) -> board_and_founder (cap 100 + runtime true mirror) — Act 3 stays IN the
                           company_hq tower (executive floor + penthouse), so board_and_founder has
                           a hard builder-geometry dependency on company_hq's multi-floor interior
  battle_frontier ........ post-game sandbox (opens on royal_league_champion; L100 tier gated on board)
        |
        v
CROSS-CUTTING / OPTIONAL (any time after its gate towns exist)
  deep_dark_cave ......... OPTIONAL endgame mega-dungeon (ruling 5): no story gate, no lock-in,
                           free exit; off the Act-3 critical path — tightest remaining coupling is
                           legendaries_nobles (monument archetype + species/slot call)
  shrines_audit .......... 5 optional gauntlets; needs the gym towns + 16's latched wild-spawn
                           pattern for shrine crystals
```

### 2.3 Recommended BUILD ORDER (single ordered list)

1. `mainline_spine`
2. `gym_system_pvp_doubles`
3. `wheat_war_farms`
4. `cyber_city`
5. `company_hq`
6. `mystic_marsh`
7. `deepcore_city`
8. `legendaries_nobles`
9. `gaviota_port`
10. `kalahar_reach`
11. `ryujin_keep`
12. `nifl_town`
13. `scorchspire`
14. `shrines_audit`
15. `royal_league`
16. `board_and_founder`
17. `deep_dark_cave`
18. `battle_frontier`

**Rationale for the non-obvious placements.**
- **`wheat_war_farms` at #3, before any town gym:** the spine's own blocker note is unambiguous — with
  only `farm_1` wired, `fields_liberated` maxes at 1, so the HQ gate (≥6 of 10), trader escalation
  (3/5 PROPOSED), relief tiers (3/6 PROPOSED) and the granary ambush are *all* unreachable. The
  6-of-10 ruling raised the bar: **5+ more fields to wire**, not 3+. Nothing in Act 2 works until this
  lands.
- **`cyber_city` before `company_hq`:** cyber is the *giver* of the HQ keycard and the Act1→Act2 flip;
  HQ is the *consumer*. Build them as one coupled pass, giver first.
- **`legendaries_nobles` at #8, before Gaviota (Kyogre):** it is a reusable two-archetype contract
  (much lighter than the old boss-engine pitch) consumed by the marquee set-pieces of
  gaviota/kalahar/ryujin/nifl/scorchspire and by shrine crystals. Lock the contract once so each gym
  can drop in its noble as it is stamped. Gym *cores* (ladder + leader + badge) do **not** need it —
  if you want to stamp a gym before the contract exists, ship the core and backfill the noble later.
- **`shrines_audit` at #14 (flexible):** optional side content; the machinery is coded but the
  challenge overlays are orphaned and the RCT teams are empty. It can slot in any time after its gate
  towns (marsh/kalahar/ryujin/nifl/scorchspire) and 16's latched wild-spawn pattern exist.
- **`deep_dark_cave` at #17 is now fully deferrable (ruling 5):** it is off the Act-3 critical path —
  nothing downstream depends on it (the mirror lives in the tower penthouse, not the cave), so it can
  slip behind `battle_frontier` or ship post-launch without blocking the Founder.

### 2.4 Suggested SESSION grouping

| Session | Areas | Theme / exit criterion |
|---------|-------|------------------------|
| **S1 — Foundations** | `mainline_spine`, `gym_system_pvp_doubles`, `wheat_war_farms` | Rails + stamp template locked; ≥6 wheat fields wired so the majority (6-of-10) HQ gate is passable. **Unblocks all of Act 2.** |
| **S2 — Act-2 hinge** | `cyber_city`, `company_hq` | The keycard opens the tower's basement elevator; the B5 DJ fight works end-to-end; CURRENCY STABILIZED fires; idx→25. |
| **S3 — Act-1 gyms** | `mystic_marsh`, `deepcore_city`, `gaviota_port`, `kalahar_reach` | Gyms 3–6 stamped (gaviota's Kyogre deferred to S4 if the contract isn't up yet). |
| **S4 — Shared contract + Act-2 tail** | `legendaries_nobles`, `ryujin_keep`, `nifl_town`, `scorchspire` | Two-archetype noble contract live; gyms 8–10 stamped with their nobles dropped in. |
| **S5 — Endgame** | `royal_league`, `board_and_founder` | Cap 85 → 100; the runtime mirror snapshots the live party and prints the username; needs the tower's executive floor + penthouse (builder geometry). |
| **S6 — Post-game & optional** | `battle_frontier`, `shrines_audit`, `deep_dark_cave` | Sandbox + optional gauntlets + the optional Deep Dark mega-dungeon (off the critical path per ruling 5). All optional-scale; ship last. |

---

## 3. FUTURE-ME IMPLEMENTATION PLAYBOOK

This is how each section actually gets built. The authoring layer is `dialog-src/` JSON; everything
else is generated. **Never hand-edit** `data/easy_npc/preset/`, `band_tags.mcfunction`,
`register_sight.mcfunction`, `preset.index`, `update_npc_presets.mcfunction`, `npc/preset_map.json`,
`ambient/placements*` — they are compiler output.

### 3.1 The pipeline, in order

```
dialog-src/**.json
   │  scripts/content_compile
   ▼
data/easy_npc/preset/humanoid/<id>.npc.snbt        (compiled presets)
function/dialog/band_tags.mcfunction               (derived numeric + inverse PLAYER_TAGs)
function/dialog/register_sight.mcfunction           (sight NPC registration)
function/ambient/placements*.mcfunction             (placement latches for no-uuid NPCs)
npc_presets.json                                    (uuid → preset, MERGED never clobbered)
   │  (uuid'd chars merge builder world NBT + dialog-src/visuals/<id>.npc.snbt identity)
   ▼
scripts/generate_granary_tiers        ← ONLY if a Granary Keeper / wheat trade tier changed
   ▼
scripts/update_preset_index           (preset.index — must include any new granary presets)
   ▼
scripts/generate_npc_function         (update_npc_presets.mcfunction + npc/preset_map.json;
                                        its version hash covers the mapping AND all preset bytes)
```

**The critical ordering rule (`ENGINE_FINDINGS §3`):** `content_compile` auto-runs the last two steps
(`regenerate_index` then `generate_npc_function`) at the end. **But granary tiers must be emitted
BEFORE the final hash.** So:

- **No granary change:** just run `scripts/content_compile`. Done.
- **Granary/wheat-tier change:** run `scripts/content_compile --no-generate`, then
  `scripts/generate_granary_tiers`, then `scripts/update_preset_index`, then
  `scripts/generate_npc_function` — in that exact order, so the preset_map hash covers the granary
  preset bytes.

In-game after a compile (OP 2): `/reload` → `/function cobblemon_initiative:update_npc_presets` →
`/function cobblemon_initiative:dialog/register_sight`.

### 3.2 Where each file type lives

| Thing you author | Path | Notes |
|---|---|---|
| Character (NPC) | `dialog-src/characters/<town>/<id>.json` | id, display_name, act, uuid **or** placement, battle/loot/movement/sight blocks |
| Dialog tree | `dialog-src/dialog/<id>.json` (or inline on the character) | entries with `gate` + `priority`; auto-`Goodbye` unless close/`no_goodbye` |
| Visual override | `dialog-src/visuals/<id>.npc.snbt` | in-world export; adopts render/skin/sound/attributes (COBBLEMON_ENTITY etc.); behaviour still from pipeline |
| rctmod team | `src/main/resources/data/rctmod/trainers/<trainer>.json` | `team[]` — **empty `{}` = a battle button that silently does nothing** |
| rctmod graph node | `data/rctmod/{series,mobs/trainers/single,mobs/trainers/groups}/*.json` | `requiredDefeats` — **any cycle = StackOverflow crash at world start** |
| Gym / shrine config | `data/cobblemon_initiative/trainers/{gyms,shrines,royal_league,battle_frontier,villain_team}/*.json` | the ladder + rewards; **never move the cap ladder** |
| Side-quest registry | `data/cobblemon_initiative/trainers/side_quests/act{1,2,3}.json` | only `act1.json` exists; ryujin needs `act2.json`, nifl needs `act3.json` |
| Functions (HUD/economy/quests) | `data/cobblemon_initiative/function/**` | `quest/render`, `economy/payout`, `granary/apply_<tier>`, `reveal/*`, etc. |
| Loot / gifts | `data/cobblemon_initiative/loot_tables/npc_gift/*.json` | includes `training_{minor,standard,major,grand}` packs |

### 3.3 Placement-latch vs uuid (how an NPC gets a body)

- **uuid (builder-placed body):** character has `"uuid"`. The compile merges the builder's world NBT
  (`mrpack/.../easy_npc/npcs/<uuid>.npc.nbt` — skin/model/variant/equipment/sounds/home/Navigation)
  and any `dialog-src/visuals/<id>.npc.snbt` identity into the preset, writes `uuid → preset` into
  `npc_presets.json` → `preset_map.json`. `NpcPresetRefreshManager` re-imports on chunk load, so
  **content updates propagate** to the placed body. Use for anchored, curated bodies (leaders,
  quest anchors, sight NPCs).
- **placement (once-per-world spawn):** character has `"placement": {x,y,z}` and **no uuid**.
  `write_placements` emits a proximity latch (`#amb_<key>` on `ci_ambient`, gate
  `@a[x,y,z,distance=..40]`, spawn via `easy_npc preset import_new`, latch set *before* import).
  Redo: kill the body + `scoreboard players set #amb_<key> ci_ambient 0`. **Spawned copies are NOT in
  `preset_map.json` → content updates do NOT propagate**; and they get **random uuids**, so any sight
  registration needs a manual `npcsight add <uuid>` after first spawn. Use for route villains,
  couriers, floor trainers, ambient life.
- **The compiler WARNS** on every act-1 character with neither uuid nor placement (it will never spawn).

### 3.4 The tbcs battle command shape (winners-first `onwin`)

Authored as a `battle` block on the character + a `{"do":"battle"}` dialog action. The compiler emits
two guarded actions (`ENGINE_FINDINGS §2`, `content_compile battle_actions`):

```
# attach (raw, NPC context @s stays the NPC; gated so a beaten NPC won't re-attach)
tbcs attach <trainer> @s                          Condition: not_tag defeated_<trainer>

# battle (as_player; @s -> player; gated on the same inverse tag)
tbcs battle <FORMAT> @s vs <trainer> onwin {1: ['<win…>'], 2: ['<lose…>']}
```

**`onwin` is winners-first: `{1: [win branch], 2: [lose branch]}`.**
- Key **1** (player won): `@1` = player, `@2` = NPC. Typical: `cobbledollars give @1 <prize>`,
  `tag @1 add defeated_<trainer>`, `@2 say <win_line>`, optional `easy_npc delete @2` (despawn villains).
- Key **2** (NPC won): `@1` = NPC, `@2` = player — the **mirror**. Typical:
  `cobbledollars remove @2 <loss_fee>`, `@1 say <taunt>`.
- A command that *starts* with `@N` executes **as** that entity; elsewhere `@N` → the player's
  name/UUID.
- **`onwin` lists are single-quoted — no `"` , `'`, or `%`** (`check_onwin`). Raw text-component
  `[{…}]` inside onwin warns — use a `do:announce` action or a wrapper function instead.
- **Empty team = dead button.** rctapi refuses to start a battle for an empty `{}` trainer JSON
  (`insufficientPokemon`). `content_compile` warns on every battle referencing a missing/empty team.
- **tbcs bypasses rctmod gating** entirely — battle locking lives in Easy NPC action Conditions +
  `onwin` defeat tags, never in rctmod prereqs. Our `BATTLE_VICTORY` handler dispatches
  `rctmod player add progress <playerName> after <id>` for gym apprentice/leader wins (targets go
  **before** the `after` literal — the reverse is unparseable and fails silently).

### 3.5 Gate / band-tag lowering

The only proven dialog Condition is `PLAYER_TAG`, and Easy NPC 6.25 **ignores the Operation field**
(`contains()` only), so:
- **`not_tag X` → `PLAYER_TAG EQUALS no_X`.** `band_tags.mcfunction` maintains `no_<X>` every tick for
  every negated tag **plus** `no_defeated_<id>` for every file in `data/rctmod/trainers/`.
- **Numeric gates** (`badges`, `cd_instability`, `fields_liberated`, `dex`) → threshold tags
  `<label>_<op>_<value>` maintained by `band_tags` (`LOGICAL_OBJ`: `badges→memory_fragment`,
  `dex→dex_caught`). `recognition` tiers: `early`=none, `mid`=`badges gte 3`, `late`=`badges gte 7`.
- **`any_tags` → fanout** (entry duplication) — this is how the **Plan-B fallback** Founder mirror
  (starter-keyed) would pick a shadow team off `claimed_starter_*`; the shipping plan is the runtime
  true mirror (§4.B, 14 §8), which needs no fanout.
- Editing any `requiredDefeats` means **re-running the cycle check over singles *and* groups** — a
  cycle StackOverflows at world start. Correct pattern: gym-N floor/apprentice gate on leader(N-1);
  gym 1 gates on nothing.

### 3.6 HUD render

- Sidebar objective `ci_quest`; displayed holders `q.main` + `q.side_*` (slots 100, 81..58; 75
  vacant); scratch holders stay `#…` on `quest_hud` (vanilla hides `#`-prefixed rows). **No quest boss
  bar** — `quest/load` deletes `cobblemon_initiative:objective`; a countdown pitch needs a fresh
  dedicated bossbar id.
- Main-story routing lives in `quest/render.mcfunction`; the **HQ raid line outranks gyms 8–10** so the
  Act-2 pivot renders next-door. Board/founder routing coord `[1590 51 1028]` goes on render lines
  ~46/51. `memory_fragment` is zero-init in render.
- A field-count line (`n/10`, gate line "Liberate 6 of 10") mirrors the `q.side_wheat` `set_wheat`
  macro.

### 3.7 Economy / liberation macros

- Sidequest payouts route through **`economy/payout {amount:N}`** (skew + actionbar receipt) — never
  call `pay_macro` directly. Battle prizes stay flat in the `onwin` list.
- Field liberation flips `fields_liberated`, claws `cd_instability` **down**; `hq_stabilize` clamps
  **downward only** (idx→25). The +8/gym destabilise pushes the other way.
- **Training packs:** every *one-time* quest completion also grants `npc_gift/training_{minor≤260,
  standard 300–400, major 500–600, grand=4000-only}` via a bare `loot give @s loot …` action *before*
  the payout. **Never** on repeatables/dailies/stages, villain SELL forks, or consolation forks. Gym
  leaders pay CD prize + config command rewards only (no off-economy emeralds).

### 3.8 The NO-double-quotes rule (hard compile errors)

- `check_text`: no `"` in any dialog text.
- `check_command`: no `"` in any command — use a `do:announce` action for tellraw/title text
  components.
- `check_onwin`: no `"`, `'`, or `%` in `onwin` lists (they are single-quoted); raw `[{` warns.

If you need a text component (tellraw/title with JSON), author it as a **`do:announce`** action or a
**wrapper `.mcfunction`** the dialog calls — never inline the quotes.

---

### 3.9 COPY-PASTE CHECKLISTS

#### ▸ Stamp a new gym (the `gym_system_pvp_doubles` 5-rung ladder)

```
[ ] rctmod teams under data/rctmod/trainers/  (aces per the CANON ladder, entry-cap + 2):
      <town>_trainer_1, _2 ....... floor grunts (below cap)
      <town>_jr_apprentice ....... single (~cap-2)          ← ladder-critical, often MISSING
      <town>_apprentice .......... GEN_9_DOUBLES, >=4 mons (~cap)  ← format MUST be DOUBLES
      <town>_leader .............. ace = entry-cap + 2       ← the ONE source of truth
    (verify none is an empty {} team; verify requiredDefeats has NO cycle over singles+groups)
[ ] RESOLVE the two-source conflict: delete/retune the embedded team in
      trainers/gyms/<town>.json so it matches rctmod/trainers/<town>_leader.json (never both live)
[ ] dialog-src/characters/<town>/  bodies for leader + apprentice + jr (+ floors if bodied)
      - leader/apprentice/jr: uuid (builder body)  OR  placement:{x,y,z} (once-per-world spawn)
      - each battle block: trainer, format, prize, win_line, defeat_tag; gate the leader button on
        not_tag defeated_<town>_apprentice (tbcs bypasses config prereqs)
[ ] dialog-src/dialog/gym_leader_<town>.json  (flesh out the 4-line stub; frag cutscene on win)
[ ] on leader defeat: fire memory frag (memory/gym/frag_N), step the shop tier, +8 cd_instability
[ ] scripts/content_compile   (check: no empty-team / no-body warnings for this town)
[ ] confirm walkable Y for every placement (many configs use centerY 64 vs a raised gym shelf)
```

#### ▸ Add a side quest

```
[ ] register in trainers/side_quests/act{1,2,3}.json  (create act2/act3 if missing)
[ ] dialog-src/characters/<town>/<sq_id>.json  (giver + any villains; placement or uuid)
[ ] gate the quest on story tags (defeated_*, met_*, fields_liberated, recognition) via `gate`
[ ] villain battle: battle block, GEN_9_DOUBLES uses the agent_yield_lead/_second pattern
      (battle on the lead NPC; partner stands down in dialog after the win)
[ ] rewards: `do:give` training pack (one-time only) THEN economy/payout {amount:N}
      — NOT on repeatables/dailies/staged loops
[ ] append a q.side_* HUD block to quest/render.mcfunction (copy an existing side line)
[ ] any text component -> do:announce or a wrapper function (never inline quotes)
[ ] scripts/content_compile
```

#### ▸ Place a new NPC

```
DECIDE: curated/anchored body?  -> uuid   |   route/floor/ambient spawn? -> placement
[ ] uuid path:
      - character "uuid": "<uuid>"; builder places the body; drop the world NBT at
        mrpack/.../easy_npc/npcs/<uuid>.npc.nbt (skin/model/home merged in)
      - optional dialog-src/visuals/<id>.npc.snbt for a non-humanoid / Cobblemon-model render
      - register uuid in npc_presets.json (content_compile MERGES it, never clobbers)
      - sight NPCs: add "sight" block; it emits into register_sight.mcfunction
[ ] placement path:
      - character "placement": {x,y,z}  (ints auto-centered +0.5); NO uuid
      - compiler emits the #amb_<key> latch; redo = kill body + set #amb_<key> ci_ambient 0
      - KNOWN GAP: spawned copies get random uuids -> sight needs a manual `npcsight add <uuid>`
[ ] scripts/content_compile  (if this touched a Granary Keeper, run the granary 4-step, §3.1)
[ ] in-game: /reload -> /function …:update_npc_presets -> /function …:dialog/register_sight
```

---

## 4. Decisions for the showrunner (aggregated, deduped, grouped)

### A. Balance & levels (the pervasive one)
- **Enforce `ace = entry-cap + 2` for every leader; reject the assignment-brief "+8" spikes.**
  Recommended aces: mystic **32**, deepcore **39**, gaviota **46**, kalahar **52**, cyber **58**,
  ryujin **64**, nifl **70**, scorchspire **76** (matches the shipped rctmod teams + CLAUDE.md ladder).
  Brief headers that say 46/52/58/64/76/82 are stale-ladder artifacts.
- **Collapse the two conflicting leader team sources per town** (`rctmod/trainers/<town>_leader.json`
  vs the embedded team in `trainers/gyms/<town>.json`) to one source of truth. Affects mystic,
  deepcore, kalahar, cyber, nifl, scorchspire, royal. Also fix deepcore's apprentice out-leveling the
  leader.
- **Reconcile the "apprentice DOUBLE" rung to GEN_9_DOUBLES + ≥4 mons** — `mystic_apprentice`,
  `ryujin_apprentice`, `scorchspire_apprentice` are shipped as SINGLES.
- **Ladder depth:** create the **24 missing** gym team files (`<town>_{trainer_3,trainer_4,jr_apprentice}`
  ×8, jr is ladder-critical) or trim to a lean 4-fight and repurpose trainer_3/4 as optional targets.
- **Leader roster size:** ship 4-mon convention or expand to a 6-mon marquee (ace unchanged either way).
- **Endgame sizing:** E4 6 vs legacy 4; Champion ace 85 vs 86; Drake SINGLES vs marquee DOUBLES; Board
  members 4→6 (aces 86–87, recommend yes — still open). Founder = flat **100** is RESOLVED
  (2026-07-06, ruling 1 — the mirror normalizes every mon to 100).
- **DJ (HQ hinge):** retune the roster UP/DJ DOWN into the cap-62 window (as-shipped DJ ace 72 is a
  +10 wall); proposed DJ ace 65 (cap+3 boss premium), shiny Entei as ace + `spawnOnDefeat` gift.

### B. The Founder / mirror reveal — **RESOLVED (2026-07-06): true mirror, in the tower**
- **RESOLVED — mirror mechanic (ruling 1):** the Founder is a **TRUE MIRROR of the player's actual
  live party**, snapshotted at battle-button press, every mon normalized to level 100; dark styling
  (§k shadow nicknames, `§kfounder` nameplate, live `@s` reveal) is **presentation only**, not a
  different roster. Jar-verified **FEASIBLE-WITH-WORK** (rctapi `TrainerRegistry.registerNPC` +
  `PokemonModel(Pokemon)` copy-ctor; TBCS resolves `vs <trainerId>` from the LIVE registry at
  command-execution time — full spec + citations in `14_board_and_founder.md` §8). Fallbacks: Plan B
  = the old starter-keyed `any_tags` fanout; Plan C = the fixed `villain_team.json` roster at flat
  100 — same-id registration makes Plan C the automatic fallback if the runtime bridge fails.
- **RESOLVED — location (rulings 4+5, venue CONFIRMED 2026-07-06):** the east/west two-site split is
  **dead**. Act 3 stays in the Company skyscraper at Cyber City's eastern seam and goes **UP**: board
  gauntlet on the executive floor (~y85, coord PROPOSED), and **the mirror is fought IN THE
  PENTHOUSE** (*"yes we fight them in the penthouse"*). The shipped Founder dialog ("climbed the
  whole tower", "walk back up the stairs") already matches the ascent — no rewrite needed. The Deep
  Dark Cave hosts nothing (fully optional side content). Sync 09's "Board + Founder re-populate the
  boardroom" lines → "Board in the boardroom; Founder in the penthouse."
- **RESOLVED — the Founder wears the PLAYER'S OWN SKIN** (*"make sure the skin is the player"*):
  Easy NPC `SkinType.PLAYER_SKIN` (verified, EASY_NPC_REFERENCE §SkinType) stamped at runtime by the
  same Java mirror bridge (14 §8 item 2b); preset ships `Type:"NONE"` so Steve never flashes.
- **RESOLVED — the Founder does NOT spawn until after the Champion** (*"the founder doesn't spawn
  until after champion"*): champion-gated spawn latch (`royal_league_champion` selector on the
  placement latch, or a hand-authored `reveal/founder_spawn` on the champion-win chain — 14 §8
  item 2). The penthouse chair sits empty all run.
- **Still open (from 14):**
  - Builder geometry: do the executive floor (~y85) and penthouse (~y95) interiors exist? All Act-3
    coords are PROPOSED against the `[1590 z1028]` column and 09's floor-Y proposals.
  - Board placement (builder-placed uuid bodies, recommended, vs placement latches); board fullness
    4→6 (aces 86–87, recommend yes); optional doubles beat on a pre-room lift-guard pair (never the
    board itself).
  - Mirror edge-case flavor: copy the party exactly as brought (1-mon party → 1v1; fainted mons
    mirrored healthy at 100, recommended) vs refusing the button below N mons.
  - Snapshot command name + `executeAsUserCommandAllowList` entry wording (needs the usual
    security.cfg self-heal + mrpack override pair).
  - SMOKETEST additions: mirror species/moves/level-100/shadow-nicknames; re-press re-snapshot after
    a party change; `/reload` between fights doesn't strand a stale registration.
- **VERIFY (keep on SMOKETEST):** `board_cleared` cap-100 needs the `BattleVictoryEvent` loser-name to
  match each board config `displayName` — separate from the `onwin defeated_board_*` tag. Mismatch =
  HUD advances but cap won't unlock. **Extended (third leg):** the runtime-registered mirror's
  `TrainerModel.name` must equal the file team's name to preserve the Founder's `company_overthrown`
  name-match.
- **How explicit can the early scrubbed-portrait beats get** (nifl SQ1, scorchspire, ryujin) given the
  *named* reveal is reserved post-Royal-League? Design intent is "circles, never closes" — confirm
  acceptable this early. Same question for kalahar's wheat_trader line that already says "the founder"
  (read as enemy memo-paranoia, not narration).

### C. Wheat War structure — **RESOLVED (2026-07-06): ten fields, majority gate**
- **RESOLVED (ruling 3):** the backbone is **all TEN install.json FARM zones** and the HQ gate is a
  **majority — `fields_liberated >= 6` of 10**, not all. (Verified: `farm_1..10` all carry
  `field_freed` banner wiring, so promoting Mirebloom/Westwind/Coldfurrow/Frostfallow needs zero
  install.json changes.) HUD shows `n/10`, gate line "Liberate 6 of 10"; seven Act-1 feeders (aces
  21–61, promoted aces PROPOSED) give one field of slack before gym 7. The `free_field_regional`
  alternative is deleted as moot. Shipped files still say ≥4 — the one-pass migration table is
  `03_wheat_war_farms.md` §8 (`set_wheat /6`, `render.mcfunction 4..`, DJ dialog `gte 4`,
  `wheat_trader`/tick `2../4..`, `granary_keeper gte 2/4`, relief constants); `content_compile`
  auto-emits the `fields_liberated_gte_6` band tag on recompile.
- **BLOCKER acknowledgement (bar raised):** only `farm_1` is wired today; the ruling raises the
  minimum wiring bar to **5+ more fields** before the gate is passable. Still the single
  highest-priority unblock in the whole roadmap.
- **Still open (from 03):**
  - Per-field instability magnitude: −6 × 6 pre-HQ fields = idx 20 at the HQ door, *below*
    `hq_stabilize`'s downward-only 25 clamp, muting the CURRENCY STABILIZED beat — retune to −5/field
    (PROPOSED)?
  - Relief re-map: confirm `fieldsPerLevel` 3 (relief1@3, relief2@6 — jar-baked `ShopTierManager`
    constants + `master_shop.json` must change together; ModMenu-tunable candidate) and greenlight
    relief3@9 (every-3 ladder)?
  - ~~`farm_8` Frostfallow `mobsSpawn:false`~~ → **RESOLVED (2026-07-06): "that was a mistake" —
    FIXED in install.json** (flipped to `true`; all 10 FARM zones now uniform live-Nuzlocke fields).
  - Promoted-field tuning: aces 29/43/61/67, team sketches and payouts for farm_2/3/7/8 are PROPOSED
    interpolations — balance confirm, especially Coldfurrow at 61 in the cap-62 window.
  - Trader/keeper hostile thresholds 3/5 are PROPOSED re-maps — confirm before touching
    `wheat_trader`/tick and `granary_keeper` gates.
  - Wheat Trader hostile tier: **direct confrontation** (recommended, vestigial `wheat_ambush_armed`
    latch) vs build the trade-then-file mirror poller.
  - Fourth granary keeper on the promoted west side (Westwind/Gaviota) now that ten fields count?
  - Capstones: greenlight Granary Ledger→Lucian (gate re-mapped to ≥6) and the all-**ten** Homecoming
    Convoy? Distinct Kalahar wheat-ambush trainer/tag so it's not pre-cleared by the shared
    `defeated_wheat_trader_ambush`. Confirm/register new Granary Keeper + Wheat Trader UUIDs and all
    field-trainer coords before regen.

### D. Legendaries / nobles — **RESOLVED (2026-07-06): two archetypes**
- **RESOLVED (ruling 2):** exactly two archetypes. **NOBLES** (Groudon, Kyogre, Zapdos, Rayquaza,
  Regirock, Uxie) start via a **prompt** (canon: the Zapdos "defend Cyber City" ask, staged in 08) or
  a **monument with a warning line** (canon: the Kyogre buoy "the sea seems violent in these parts"),
  then a **PvP trainer phase** on the shipped do:battle/TBCS rails, then the legendary spawns via a
  one-shot latch as a **NORMAL WILD Pokémon** — full Nuzlocke stakes, the player chooses the risk.
  This closes the old safe-zone-vs-full-stakes AND gift-vs-catch questions (nobles = wild catch;
  Kyogre + Groudon both catchable wilds; spawn points verified against `mobsSpawn:true` zone hulls).
  **EVENTS** (Mew chase, Celebi berry offering, Manaphy water-temple traversal — supersedes the egg)
  end with **clicking the Pokémon to join**, via the shipped starter stand-in claim pattern. The old
  warden-boss engine, healing-bag phases, boss stats, giant-scale and gift rewards are moot. The
  +2..+4-over-cap tuning question is moot — wild spawns are tuned cap-0..2 so they are usable.
  Roster is now **10** (adds Zapdos + Celebi + the Deep-Dark L10); PvP-phase guardian trainer files
  (2–3 per noble) replace legendary team files.
- **RESOLVED (2026-07-06) — KO'd noble is GONE FOREVER:** *"that's the challenge — making sure you
  don't crit them during the catch phase."* No re-call after a KO (`noble_<id>_lost` epitaph entry);
  non-KO exits (walk-away, natural despawn) stay re-callable so the beat can't strand.
- **RESOLVED (2026-07-06) — Manaphy temple EXISTS on the map:** *"there is a temple already on the
  map I will just need to provide cords"* — no terrain work, no sea-cave fallback; awaiting coords.
- **RESOLVED (2026-07-06) — Deep-Dark L10 species = GIRATINA** (*"Giratina will be in the deep
  dark"*); full stakes, spawn outside every SafeZone hull (the 16-vs-17 stakes conflict is fixed in
  both files).
- **Still open (from 16):**
  - Company extraction-crew variant: may a noble's PvP phase be a villain gauntlet, or do all phases
    stay civilian guardians? Allow one Dark-Urge memory echo on Uxie's still-water monument?
  - Scope: Regi-trio (Regirock alone vs Regice/Registeel/Regigigas); single Uxie vs full Lake trio;
    weather-trinity delta capstone prize — now **blocked on a new Java capture-event listener** for
    `caught_<id>` tags (advancement package has only `TrainerDefeatedCriterion`) — approve the
    mod-side work or defer the capstone.
  - Celebi: era gate (PROPOSED `mem_gte_4`, Lv 40), the five-berry offering set (Cobblemon berry item
    ids UNVERIFIED), Blossom Path grove coordinate (builder confirm).
  - Zapdos level/timing (PROPOSED Lv 60, post-gym-7) to reconcile with 08's defense design.
  - Groudon gate fork in 12: post-badge (wild Lv 78, cap-80 window, recommended) vs pre-badge (~72).
- **JAR-VERIFY before building:** Cobblemon 1.7.3 `spawnpokemonat` / `givepokemonother` / property
  keys — not in ENGINE_FINDINGS; every shipped use is annotated UNVERIFIED; also whether a
  command-spawned PokemonEntity can be tagged / persistence-flagged against despawn timers; and
  whether ConfigLoader accepts a new trainer category value `legendary` for the PvP-phase
  TrainerConfig entries.
- **Shrine-crystal overlap:** the crystal spawns a flat L70 wild — a cap-37 Fairy clearer gets an
  unusable Xerneas. Make crystals hold-until-cap trophies or scale the spawn level (ties to E).

### E. Shrines
- **Ship the coded challenge overlays** (parkour / dark gauntlet / hydra / fairy-tests) by wiring the
  dialog command-action, or ship shrines as **ladder+boss only**?
- Cultist ladder depth **4 vs 2** (configs say 4, RCT stubs+ENGINE_FINDINGS assume 2) — then create or
  trim. Set a consistent shrine-superboss balance floor (Fairy Xerneas is +11 over gate cap; Fire
  Heatran is −3 under). Crystal legendary ≠ leader ace for Ground/Ice/Fire — align or intentional? Fix
  the double-crystal grant (rewards array + Java `grantShrineCrystal`). Weave shrines into canon (a
  "the land remembers" fragment) or keep them instability-neutral folklore?

### F. Battle Frontier (largest new-systems ask)
- Adopt **Frontier Points** as a real soft-currency vs reward in CobbleDollars + items only.
- **Two-tier level split** (first-clear ~87–90 at cap 85, L100 record tier gated on `board_cleared`)
  vs a single champion-gated L90–100 clear.
- Accept the **engine-honest reframes** for Battle Factory (opponent-side random draft) and Battle
  Pyramid (no-heal-service) — no verified TBCS team-substitution / bag-lock primitive — or budget
  JAR-verification for a real hook. Fold `the_market` into the hub FP Exchange (6+hub) vs a 7th door.
  Streak depth (3-node rotation vs +8 filler teams). Keep legendary brain leads or reserve for
  `legendaries_nobles`. Approve the §k Hall-of-Fame ghost-record beat (nods at the mirror one act
  early). Open on champion vs hold until Board progress.

### G. Company HQ interior & hinge — **RESOLVED (2026-07-06): skyscraper geometry**
- **RESOLVED (ruling 4):** the HQ is a **skyscraper in Cyber City** — enter at the street-level
  first-floor lobby; **DOWN** = five basement levels styled as the evil HQ (B1 Shade DOUBLES → B2
  Vex DOUBLES → B3 grunts → B4 Noir → B5 boardroom, DJ at `[1590 51 1028]` — y51 verified as
  basement depth vs zone centerY 64); **UP** = ordinary corporate flavor floors topped by the
  player's old **penthouse** (memory tease, Lopunnys, founder never named pre-Act-3). The tower
  serves all three acts (Act-3 relocation to the upper floors is PROPOSED, owned by 14). The
  keycard is canonically the **basement-elevator door check** (a real gate; mechanics PROPOSED);
  the fields gate (**≥6 of 10**, ruling 3) stays on DJ's B5 door — shipped files still say `gte 4`,
  edit list in 09 §8 (incl. the new `fields_liberated_gte_6` band-tag pair).
- **ZONE DISCREPANCY (builder/showrunner confirm — do not silently resolve):** `[1590 51 1028]`
  point-in-polygon-verifies INSIDE the "The Company, Inc." VILLAIN zone and OUTSIDE the Cyber City
  TOWN polygon (adjoining seam, no overlap). Keep the carve-out (the tower announces itself on
  approach) or redraw so the block reads as in-town?
- **BUILDER GEOMETRY (blocking):** does the skyscraper exist with street lobby + five-level basement
  stack + upper flavor floors + penthouse, or is the HQ a shell? All interior coords PROPOSED.
- **Keycard scope + delivery (still open):** free vs ~2000 CD express sink; card gated on gym-7 only
  (recommended — basement scoutable, DJ's door holds the ≥6 gate) or also fields-gated? Penthouse
  timing: sealed until DJ falls (recommended, closes Act 2) vs open with the other upper floors?
- `grunt_10/11`: B4 Executive-Sublevel guards vs reserve as Act-3 Board guards on the upper floors
  (more plausible now that 14 relocates Act 3 into the tower; re-gate their gym-8/9 RCT prereqs if
  reused at gym 7). Lopunny assistants tone/count (2, no battle). HQ roster retune + DJ ace 65 and
  DJ completion `training_major` — unchanged, still open.

### H. Deep Dark Cave (optional endgame dungeon) — **RESOLVED (2026-07-06): open, free exit**
- **RESOLVED (ruling 5):** fully **optional** — no story gate (the former `board_cleared` entry gate
  is tuning context only), no lock-in, and **free exit is a build requirement** (no one-way drops,
  no lock-behind doors, Recall Beacon functional anywhere in-zone, vault stairs back up — audit the
  finished build against this). The Founder/mirror is **not** here (rulings 1+4 — HQ penthouse); the
  mirror-boon question is moot; the vault is the dungeon's own terminus (treasure cache, 12000 CD,
  Auditor's restock, hollow-reserve lore as an explicitly optional deep-cut). Its legendary reshapes
  to 16's monument-warning archetype (`dd_monument` at the mouth → the descent as the challenge leg →
  static wild in the vault — all PROPOSED; see the cross-section conflicts flagged in 16 vs 17).
- **Still open:** ~~which legendary~~ → **GIRATINA (RESOLVED 2026-07-06**, registered as L10 in 16 —
  full stakes, spawn outside every SafeZone hull**)**; monument challenge shape (descent-only environmental leg, recommended, vs requiring
  one booth echo-fight as a PvP-ish leg); ship the sculk echoes or keep the descent purely
  environmental (echoes gate on `defeated_*` tags since the cave opens early); safety-kit balance
  (totem count, wool amount, Recall Beacon cooldown — tuned with the builder's sensor spacing);
  underleveled-warning copy + flat-L100 vs scaled loot band; post-game east-exit flavour toward
  generated terrain for the Ender-Dragon leg (PROPOSED, pure colour). **BUILDER:** the entire
  sub-surface sculk build — descent path, sensor/shrieker layout, sealed lit booths, vault, treasure
  cache, and always-available return routes; every sub-surface coord is PROPOSED.

### I. Placement, geometry & convention
- **Walkable-Y confirmations** are pending across the board (deepcore gym y129 vs centerY64; royal
  y166 vs 64; gaviota y64 docks vs y82 boardwalk; nifl ice-shrine trainers below their zone; kalahar).
- **Placement vs uuid for currently-unplaced bodies** (deepcore leader/guide, ryujin leader/guide,
  royal fight characters, board bosses, gym-2..10 leaders): add `placement:{}` once-per-world spawns
  vs adopt builder bodies by uuid.
- **Folder convention:** move gym NPCs into per-town folders (`characters/gaviota/`, `characters/cyber/`)
  vs leave them in `characters/gym/`. Service-NPC parity: does the map already provide Nurse/Martkeeper
  stations (kalahar, nifl) or do they need bodies? Villain density per town (deepcore/scorchspire read
  villain-quiet — add a second grunt?).

### J. Story pacing & atmosphere
- **Dark Urge tier-3 trigger:** re-key `NuzlockeConfig` breakpoints (currently 30/52/73 on the stale
  ladder → fires post-gym-9) to the canon ladder so tier 3 fires at the **HQ pivot** (post-gym-7, cap
  62) — or hard-gate on `defeated_villain_boss`.
- **Mystic Marsh safe-zone status:** confirm it is intentionally NOT a full safe zone
  (`mobsSpawn:true`) and whether Nuzlocke suspends inside town — underpins the whole atmosphere pitch.
- **Deepcore nether-star vein:** is the mine literally the physical currency backing? SQ1 leans on it;
  needs a canon OK that doesn't contradict `company_hq`.
- Town-gate signposts vs rely on JourneyMap/Map Frontiers labels. Memory re-reader journal home:
  Mom's house vs Sango lab. Royal League heal convention (free-inside-walls vs 100 CD Dr. Asha, whose
  file currently points at Sango) + optional "Sealed Hall" heal-lock hard mode. Which optional side
  quests ship now vs defer as post-launch colour (cyber Exchange Rate / Off the Clock).

---

## 5. Effort table + phasing

| Area | Effort | Biggest risk / unknown | Session |
|------|--------|------------------------|---------|
| `mainline_spine` | **L** | Dark Urge tier re-key to canon ladder (mirror mechanic RESOLVED 2026-07-06 — see 14) | S1 |
| `gym_system_pvp_doubles` | **L** | requiredDefeats cycle = world-start crash; 24 missing team files + un-bodied leaders | S1 |
| `wheat_war_farms` | **L** | Only farm_1 wired → majority gate (6 of 10) impassable until 5+ more fields land; scope grew to 9 field pairs (18 rctmod team files + 18 registry entries) | S1 |
| `cyber_city` | **L** | Keycard-as-gate vs flavor; leak-admin + HQ street-entrance placement | S2 |
| `company_hq` | **L** | **BUILDER: skyscraper interior (lobby + 5-level basement + upper floors + penthouse) unconfirmed**; zone-seam discrepancy at [1590 51 1028] | S2 |
| `mystic_marsh` | **L** | Is it truly not-a-safe-zone (mobsSpawn:true)? 3 conflicting/absent leader-team sources | S3 |
| `deepcore_city` | **L** | Nether-star-vein canon OK; two-source leader conflict + apprentice out-levels leader | S3 |
| `gaviota_port` | **XL** | New multi-round derby function tree; Kyogre buoy monument + 2 storm-watch guardian teams + latched wild spawn; all dock/offshore coords proposed | S3 |
| `kalahar_reach` | **L** | Distinct wheat-ambush tag; two-source leader conflict; open founder-naming pacing | S3 |
| `legendaries_nobles` | **L** | Two-archetype contract + 10 roster (7 noble, 3 claim events); Manaphy temple exists in-map (awaiting showrunner coords); the Java `caught_<id>` capture listener is its only new-code item; jar-verify spawn grammar | S4 |
| `ryujin_keep` | **L** | Un-bodied leader/guide; new act2.json registry; apprentice SINGLES→DOUBLES | S4 |
| `nifl_town` | **L** | Two out-of-sync leader teams; new act3.json; ice-shrine trainers below their zone | S4 |
| `scorchspire` | **XL** | Groudon crater geometry unconfirmed (rim monument + 2–3 crater-warden teams); fire_shrine team empty; two-source leader conflict | S4 |
| `royal_league` | **L** | Interior Y (config 166 vs zone 64); heal convention; champion win-tag verification | S5 |
| `board_and_founder` | **L** | Runtime true mirror **FEASIBLE-WITH-WORK** (jar-verified 2026-07-06): small-Java bridge (compileOnly rctapi/tbcs pins, snapshot command, content_compile `mirror` flag, ExecAsUser allowlist entry, 4 SMOKETEST items); **BattleVictoryEvent name==displayName** cap-100 gotcha; tower executive-floor/penthouse geometry | S5 |
| `deep_dark_cave` | **XL** | **BUILDER: entire sub-surface sculk build** unbuilt + legendaries_nobles coordination; now optional (ruling 5) — nothing downstream depends on it, can slip | S6 (flex) |
| `battle_frontier` | **XL** | Factory/Pyramid have no verified TBCS primitive; Frontier-Points soft-currency + two-tier scope | S6 |
| `shrines_audit` | **M** | 15 empty RCT teams + orphaned overlays; Fairy +11-over-cap balance floor; double-crystal grant | S6 (flex) |

Phasing note: **S1 is the unblocker session** — until ≥6 wheat fields are wired and the gym template +
spine tissue are locked, nothing in Act 2+ is testable. XL areas (`gaviota_port`, `scorchspire`,
`deep_dark_cave`, `battle_frontier`) each carry a builder-geometry or new-datapack-system risk and
should be scheduled with builder time reserved; their gym *cores* can ship ahead of their XL
set-pieces if needed. `deep_dark_cave` is now optional side content (ruling 5) — it can defer
indefinitely without blocking the Founder ship.
