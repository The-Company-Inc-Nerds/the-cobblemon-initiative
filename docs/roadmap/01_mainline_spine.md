# 01 — Mainline Quest Spine (the critical path)

> **Area key:** `mainline_spine`
> **Owns:** the through-line that carries the player act→act — Sango prologue → gyms 1-7 (Act 1)
> → HQ gate → Acting CEO DJ → gyms 8-10 (Act 2 tail) → Royal League → Board clearout →
> Founder mirror → post-game Ender Dragon. Specifically: how the quest HUD points the player
> at the next objective, the **exact gate between each act**, the memory-fragment drip
> (`frag_1..10`), the Dark Urge tier escalation, and where control **hands off** between areas.
> **Does NOT own:** individual gym interiors, the Wheat War field content, the HQ interior
> gauntlet, the Royal League roster, or the Board/Founder rooms — those are other agents. This
> doc owns the **rails those areas hang on** and is the contract they must satisfy.

Almost all of the spine plumbing already exists and is authoritative. This section (a) documents
it as the canonical design, (b) fills the connective-tissue gaps (act-curtain beats, destination
signposting, the HQ-gate telegraph, the memory re-reader, the champion→board→founder→endgame
routing), and (c) flags the retunes the spine needs from the ladder move.

---

## 1. Concept & fantasy

**One-line pitch:** *One score — your recovered memories — silently drives every "where do I go
next," and the same number is quietly counting down to the moment you realise the villain you have
been hunting all game is the reflection in the boardroom mirror.*

The fun of the spine is that **the player never touches a quest-accept dialog for the main story.**
The critical path is emergent: beat a gym → a memory surfaces → the sidebar repoints itself → the
map already knows where the town is. The player is always oriented with zero menu friction, which is
exactly what a long-form stream needs — no "wait, where am I supposed to go?" dead air. Underneath
that convenience is the horror engine: `memory_fragment` is both "badges earned" and "how close you
are to remembering you are the Founder." The HUD line and the memory whisper are the **same clock**.

Marquee stream moments (all spine-owned transitions):
- **frag_7 "You signed this charter."** — mid-run after Cyber City. The sidebar flips from a tidy
  gym checklist to a blood-red `▶ Raid Company HQ [1590 51 1028]`. The vibe of the whole show pivots
  on one badge.
- **The HQ hard gate** — the player marches to DJ *too early*, and the acting CEO literally will not
  fight them ("You are foot traffic"). The audience learns the rule (starve the monopoly first) from
  the villain's own mouth. Then the payoff walk-back after 6 of the 10 fields (majority rule —
  SHOWRUNNER RULING 2026-07-06).
- **"CURRENCY STABILIZED"** — green title card, beacon hum, the sidebar exhales back to the gym
  ladder. The one time in the run the economy visibly heals.
- **The four oblique board-fall beats** circling a `§k`-static name, then **`founder_defeated`
  rendering the streamer's own username live** as the answer. The final HUD line: `▶ Hunt the Ender
  Dragon`.

---

## 2. Narrative role

The spine is the **spine of all three acts** — it does not sit in one act, it is the through-line.

| Spine node | Act | `cd_instability` at node | Memory fragment | Recognition tier the world is at | Canon tie |
|---|---|---|---|---|---|
| Sango prologue | pre-Act-1 | 0 | none | early (civilians; Mom knows her kid) | opening chain, LORE §2 |
| Gyms 1-7 | Act 1 (Infiltration) | 0 → 56 (peak, +8/gym) | frag_1..7 | grunts confused-hostile → veterans alarmed | LORE §4, §8 |
| HQ gate + DJ | Act 2 pivot | 56 → **25** (DJ falls) | (no frag) | late (some stand down) | LORE §4 row 2; hard gate = gym7 + 6 of 10 fields (majority, ruling 2026-07-06) |
| Gyms 8-10 | Act 2 tail | 25 (held) | frag_8,9,10 | late | LORE §8; frags circle the truth, never close it |
| Royal League | Act 3 open | 25 | (no frag) | — | cap → 85 |
| Board clearout | Act 3 | 25 | (board_fell beats) | late (removed twice) | cap → 100; §k names circle |
| Founder mirror | Act 3 climax | 25 | reveal | **the name spoken — the player's own** | LORE §5; `company_overthrown` |
| Ender Dragon | post-game | 25 | — | reclaimed self | LORE §5 aftermath |

Canon rules the spine enforces (do not regress):
- **Never name the protagonist as the Founder before Act 3.** frag_7/8/9/10 *circle* it
  ("You signed this", "You built it", "They emptied you", "face your own signature") and the
  board_fell beats *circle* it; the name is spoken **only** at `reveal/founder_defeated`, and it is
  the player's own (selector component). No name is baked into any shipped file (`§kfounder`).
- **The word "wheat" never prints before its reveal** — the side line gates on `heard_wheat_pitch`.
- **Mom never learns the truth.** Her arc resolves on presence; the spine's only Mom endpoint is the
  `homecoming` entry (priority 50, gated `defeated: villain_final_boss`) — her kid comes home after
  it is all over. No line in the spine tells her who they were.

---

## 3. Layout & placements

The spine has almost no bodies of its own — it is functions + the HUD. Its "geography" is the ordered
list of **hand-off anchors** (where control passes from one area to the next) and a small number of
**PROPOSED spine props** (a memory re-reader, town-gate signposts) that make the rails legible on
stream. Coordinates below are grounded in `install.json` zone centroids (labelled *centroid*) or
existing files (labelled with the source); anything I invent is **PROPOSED (needs builder confirm)**.

### Hand-off anchor chain (grounded coords)

| Order | Node | Zone / anchor | Coord | Source |
|---|---|---|---|---|
| 0 | Player spawn (prologue) | Sango Town | `2615 109 2843` | `ENGINE_FINDINGS` (baked world spawn) |
| 0 | Starter lab (opening chain) | Sango lab-side | `2675.5 128 2899.5` | `ENGINE_FINDINGS` (starter stand-in spawn) |
| 1 | Gym 1 | Takehara Falls | centroid `1915 ~64 2467` | install.json |
| 2 | Gym 2 | Hua Zhan City | centroid `1467 ~64 2083` | install.json |
| 3 | Gym 3 | Mystic Marsh | centroid `1072 ~64 2459` | install.json |
| 4 | Gym 4 | Deepcore City | centroid `1108 ~64 3205` | install.json |
| 5 | Gym 5 | Gaviota Port | centroid `570 ~64 3529` | install.json |
| 6 | Gym 6 | Kalahar Reach | centroid `2042 ~64 4076` | install.json |
| 7 | Gym 7 | Cyber City | centroid `1500 ~64 1163` | install.json |
| **HQ** | **HQ raid entry** | The Company, Inc. (VILLAIN zone, centroid `1617 1061`) | **`1590 51 1028`** | render.mcfunction (baked in HUD line) |
| 8 | Gym 8 | Ryujin Keep | centroid `2249 ~64 953` | install.json |
| 9 | Gym 9 | Nifl Town | centroid `3590 ~64 2018` | install.json |
| 10 | Gym 10 | Scorchspire | centroid `3662 ~64 4542` | install.json |
| RL | Royal League | Royal League zone (centroid `3536 2823`) | **`3528 166 2773`** | CLAUDE.md |
| Board + Founder | Boardroom (top of HQ) | The Company, Inc. zone | `1590 51 1028` vicinity (interior/upper floor) | render + board_and_founder owns exact |
| End | Ender Dragon | leave the custom map → generated terrain | n/a | LORE §5 |

**Note:** Cyber City (gym 7, `1500 1163`) and the HQ zone (`1617 1061`) are neighbours — this is
deliberate: the Act 2 pivot happens right where gym 7 leaves you. The map does not send the player
back across the world to raid HQ; it is next door.

### PROPOSED spine props (needs builder confirm)

| Prop / NPC | Purpose | Suggested coord | Status |
|---|---|---|---|
| `spine_memory_journal` (Sango, Mom's house) | Re-reader: reads back all recovered `frag_N` on demand — a stream-recap device. Latent support already exists (`memory_fragment_<n>` PLAYER_TAGs set by `grant_fragment`). | Inside Sango, near Mom `2601 ~64 2862` (centroid) | **PROPOSED** |
| `signpost_next_town` (×10, one per town exit) | Optional low-cost "Road to <next town>" placement prop so navigation reads on camera even with the map closed. Map Frontiers already draws every town zone on JourneyMap, so this is polish, not required. | one at each town's outbound road head | **PROPOSED (map already covers routing)** |
| `spine_hq_courier` (Cyber City, fires at frag_7) | A fleeing Company courier who drops the "come back when the fields stop answering our memos" telegraph *before* the player reaches DJ, so the 6-of-10-field gate is not a surprise wall. | Cyber City edge toward HQ, ~`1560 ~64 1090` | **PROPOSED (telegraph; DJ already teaches it as backstop)** |

Everything else the spine touches (DJ, board, founder, gym leaders) is owned & bodied by other areas;
the spine only wires their **defeat tags** into the HUD and the reveal.

---

## 4. Core structure — the spine state machine (NOT a gym)

The spine is a single derived state machine. There is **no new persistent state**: `quest/render`
recomputes the whole objective every ~second (`quest/tick` polls at 20-tick throttle when the HUD is
shown) purely from `memory_fragment` (score 0..10), a handful of `defeated_*`/story tags, and
`fields_liberated`. This is the master ladder, evaluated **low→high priority so the highest branch
wins** (source: `function/quest/render.mcfunction`):

| # | Branch | Fires when | Main-line text (`q.main`, slot 100) |
|---|---|---|---|
| 0 | Gym | `memory_fragment ..9` | `▶ <next town>` via `quest/gym_town` (badge N → town name) |
| 0b | Opening chain | `memory_fragment 0` + opening tags | Talk to Mom → Visit Acacia → Take Pokedex → Show Mom |
| 1 | HQ raid (pre-gate) | `memory_fragment 7..`, not `defeated_villain_boss`, `fields_liberated <6` | `▶ Liberate 6 of 10 wheat fields, then raid HQ` (gold) |
| 1 | HQ raid (armed) | `memory_fragment 7..`, not `defeated_villain_boss`, `fields_liberated 6..` | `▶ Raid Company HQ  [1590 51 1028]` (red) |

**RULING NOTE (2026-07-06):** the gate is **6 of 10** (majority of the ten-field network — see
`wheat_war_farms` §8 migration table). `render.mcfunction` lines 37–38 still ship the old
`matches 4..` literals and the old gold-line text — spine-owned literals to change in the same
pass as `set_wheat`'s denominator.
| 2 | Royal League | `memory_fragment 10`, `defeated_villain_boss`, not `royal_league_champion` | `▶ Challenge the Royal League` |
| 3 | Board | `royal_league_champion`, `#board ..3`, not `defeated_villain_final_boss` | `▶ Hunt the Board of Directors` |
| 3b | Founder | `royal_league_champion`, `#board 4..`, not `defeated_villain_final_boss` | `▶ Face The Founder` |
| 4 | Done | `defeated_villain_final_boss` | `▶ Hunt the Ender Dragon` (dark_green) |

**The load-bearing sequencing trick (document this loudly):** branch **(1) HQ raid outranks (0) Gym
for badges 7, 8, 9.** So the intended flow is **gym 7 → (HUD forces) HQ raid → DJ falls →
`defeated_villain_boss` stops branch 1 → gym line resumes for gyms 8, 9, 10.** The HQ raid is
sequenced *between* gym 7 and gym 8 by HUD override alone — there is no separate "act 2 unlocked"
flag. This matches the LORE beat map (the HQ-raid row sits between gym 7 and gym 8).

### The exact gate between each act (the deliverable)

| Transition | GATE (all conditions) | What fires on cross | Who owns the trigger |
|---|---|---|---|
| **Prologue → Act 1** | `got_running_shoes` (chain: `met_mom`→`mom_sent_to_lab`→`chose_starter`→`got_pokedex`→`got_running_shoes`) | HUD flips to gym line (Takehara) | mainline_spine (chain), sango prologue (bodies) |
| **Act 1 gym→gym** | leader defeat → `memory_fragment` +1 (via `grant_fragment` reward) | frag_N title card; `gym_destabilize` +8 idx; shop tier step | mainline_spine (frag/HUD), gym_system_pvp_doubles (battles) |
| **Act 1 → Act 2 (HQ pivot)** | `memory_fragment >=7` **AND** `fields_liberated >=6` (6 of 10, ruling 2026-07-06) | DJ battleable (dialog `default` gate `fields_liberated gte 6` — file still says `gte 4`, migration row 3 in wheat_war_farms §8); on win → `defeated_villain_boss` + `hq_stabilize` (idx→25, downward-only) + Dark Urge tier 3 (see §gotchas) | mainline_spine (gate + HUD), company_hq (interior), wheat_war_farms (the 6-of-10 fields) |
| **Act 2 tail → Royal League** | `memory_fragment ==10` **AND** `defeated_villain_boss` | HUD → Royal League line | mainline_spine (HUD), royal_league |
| **Royal League → Act 3 (Board)** | `royal_league_champion` (NOTE: override tag, **not** `defeated_royal_champion`) | cap → 85; HUD → board line | mainline_spine (HUD), royal_league (champion) |
| **Board clearout** | all four `defeated_board_{madeline,matt,micah,lauren}` (→ `board_cleared` achievement) | cap → 100; each defeat → `reveal/board_fell` beat; HUD → founder line | mainline_spine (reveal + HUD), board_and_founder (bodies) |
| **Act 3 → post-game** | `defeated_villain_final_boss` (alias of `company_overthrown`, granted by `reveal/founder_defeated`) | name spoken (player's own); HUD → Ender Dragon; Mom `homecoming` unlocks | mainline_spine (reveal + HUD + Mom endpoint), board_and_founder (Founder body) |

---

## 5. Quests & side quests (spine-level missions)

The spine's "quests" are the main-line stages themselves plus the two structural sub-loops it owns:
the **opening chain** and the **memory drip**. (Individual gym interiors, the Wheat War, and the
side-quest board are other areas.)

### 5.1 The Opening Chain (Sango prologue) — the spine's only authored quest
- **Giver:** Mom (Nalia, `nalia_mom.json`, uuid-bodied, `dialog:mom_first_meeting`).
- **Hook:** she runs up when the player leaves their room (NpcSight `approach_once`, range 12 →
  `met_mom`).
- **Steps + gates (already built, spine documents/owns the wiring):**
  1. Talk to Mom, say you are ready → `mom_sent_to_lab`.
  2. Visit Prof. Acacia at the lab, choose a partner → `chose_starter` (ESC-proof latch via
     `STARTER_CHOSEN`).
  3. Take the Pokédex → `got_pokedex`.
  4. Show Mom the Pokédex → she gives Running Shoes → `got_running_shoes` (Mom entry priority 45,
     must outrank warming/worry).
- **HUD:** main line 0b mirrors each step pre-badge-1; also a persistent side line `q.side_opening`
  (slot 81) that survives past badge 1.
- **Resolution:** `got_running_shoes` ends the prologue; the gym line takes over pointing at Takehara.
- **Reward:** Running Shoes (+30% movement — the only sanctioned speed source post the level.dat
  speed strip). No CD (this is the tutorial).

### 5.2 The Memory Drip (run-long) — the amnesia clock
- **Giver:** none — it is a **reward hook on every gym leader.** Each leader config carries a
  `type:command` reward `execute as {player} run function cobblemon_initiative:memory/gym/frag_N`.
- **Payload:** `frag_N` calls `memory/grant_fragment {n,title,sub}` → sets `memory_fragment_<n>`
  PLAYER_TAG + `memory_fragment` score N, plays a cinematic title/subtitle (`#7A5CA8` purple,
  sculk-click + soul-escape sound) and a chat echo `[Memory] <title>`. One-way latch (PlayerProgress
  guards re-defeat).
- **The ten lines (bookends grounded from files; middles per LORE §8):**

| Frag | Gym / town | Title line (first person, no `"`/apostrophes) | Beat |
|---|---|---|---|
| 1 | Takehara (Bug) | `...have we met before?` | formless unease |
| 2 | Hua Zhan (Grass) | (authored) | wheat country begins |
| 3 | Mystic Marsh (Fairy) | (authored) | end of "stable" feel |
| 4 | Deepcore (Fighting) | (authored) | prices adjusting |
| 5 | Gaviota (Water) | (authored) | — |
| 6 | Kalahar (Ground) | (authored) | — |
| 7 | Cyber City (Electric) | `You signed this charter. Your hand. Your seal.` | **the hard turn — HQ unlocks** |
| 8 | Ryujin (Dragon) | `You built it.` | post-HQ |
| 9 | Nifl (Ice) | `They emptied you.` | — |
| 10 | Scorchspire (Fire) | `Everything points one direction now. Inward.` | doorstep of truth, withheld |

- **Reader:** PROPOSED `spine_memory_journal` in Mom's house re-reads recovered fragments on demand
  (gates on `memory_fragment_<n>` tags) — a recap tool for the stream. Design-only.
- **Rule:** frag_7..10 circle the reveal; the name is NEVER in a fragment. The reveal is the Founder
  fight only.

### 5.3 Act-curtain beats (PROPOSED connective tissue — design only)
Three of the four act boundaries already have a "curtain" (frag_7 title; CURRENCY STABILIZED;
founder reveal). The gaps I propose closing:
- **Act 1 complete / Act 2 open:** on gym-7 clear, after the frag_7 card, a short follow-on line
  ("Somewhere a memo is being written about a face from the old company") to name the pivot. Could be
  appended to `frag_7.mcfunction` or fired by the PROPOSED `spine_hq_courier`.
- **Board fully cleared:** already handled by `board_fell` `#board matches 4` (gold subtitle +
  tellraw). Good.
- **Post-Founder → leave the map:** `founder_defeated` ends on `▶ Hunt the Ender Dragon`; PROPOSE a
  one-line send-off tying to Mom's `homecoming` ("go home first, then go past the edge of the map").

---

## 6. Trainers & teams needed (spine-critical only)

The spine does not author gym rosters (those are per-gym agents). It depends on **five boss/ladder
battles** existing and correctly tagged, because the HUD and reveal read their defeat tags:

| Battle | Trainer id | Defeat tag the spine reads | Team status | Level vs ladder |
|---|---|---|---|---|
| Acting CEO DJ | `villain_boss` | `defeated_villain_boss` | **authored** (`villain_team.json`, team starts L68) | fought at cap 62 (after gym 7) — ace should be ~64 per brutal rule; current L68-72 team is HIGH, **retune down** |
| Champion | `royal_champion` | **`royal_league_champion`** (override, NOT `defeated_royal_champion`) | authored (`royal_league.json`, L79-85) | cap 80 → ace ~85 target ✓ |
| Board ×4 | `board_{madeline,matt,micah,lauren}` | `defeated_board_<name>` | authored (`villain_team.json`, L83-85) | played in 85→100 window ✓ |
| The Founder | `villain_final_boss` | `company_overthrown` + alias `defeated_villain_final_boss` | authored (`villain_team.json`, L88-90) | **CLAUDE says single L100 mirror** — current 6-mon L88-90 team CONTRADICTS; retune (open question) |

Wiring the spine relies on (verified against Hua Zhan / DJ configs):
- Gym leaders fire `frag_N` + `gym_destabilize` + `shop badge_N` as `type:command` rewards, and carry
  `achievementOnDefeat: badge_<type>` (the level-cap linchpin — cap steps key on that achievement).
- DJ fires `hq_stabilize` + `shop post_hq` on defeat (both the config reward AND the dialog `on_win`
  call `hq_stabilize`; it is idempotent/downward-only, so the double-fire is safe).
- Board members' `on_win` each call `execute as @1 at @1 run function .../reveal/board_fell`.
- Founder `on_win` calls `execute as @1 at @1 run function .../reveal/founder_defeated`.
- Champion's defeat must set `royal_league_champion` (defeat_tag override) — every HUD consumer keys
  on that exact tag, not the `defeated_<id>` default.

TBCS onwin reminder (ENGINE_FINDINGS §2): key `1` = **player won**; `@1` in the win list is the
player. All spine reveal calls correctly use `@1`.

---

## 7. Economy & rewards

The spine owns the **`cd_instability` curve** and the two act-scale economic beats; it does not own
per-quest payouts. (Source: `economy/*`, `liberation/*`.)

| Lever | Function | Effect | Fired by |
|---|---|---|---|
| Gym destabilize | `economy/gym_destabilize` | `+8` idx (clamp 100), actionbar narration | each gym leader reward |
| Field liberation | `liberation/free_field` → `free_field_apply` | `-6` idx (floor 0), `+1 fields_liberated`, `wheat_war_active` tag, `shop refresh` (relief tier) | wheat_war_farms field guards |
| HQ stabilize | `economy/hq_stabilize` → `stabilized` | clamp idx **down** to 25 (never up), "CURRENCY STABILIZED" beat | DJ defeat (reward + onwin) |

**The tug-of-war (spine-level narrative math):** Act 1 idx climbs +8/gym to a peak of 56 at gym 7,
minus 6 per field the player claws back. The player's field liberation is the counter-pressure the
audience can feel in their wallet (shop relief tiers step every 2 fields). Toppling DJ is the hard
reset to 25, held flat through Act 2 tail (gyms 8-10 do NOT destabilize post-HQ per LORE §8 — those
leaders should NOT carry `gym_destabilize` rewards; **verify when gyms 8-10 are authored**).

Spine-scale prizes (grounded): DJ prize `8000` CD + master ball + 64 wheat; board members `9000` CD
each; Founder `0` CD (the reward is the reveal). Shop tiers step per badge and per HQ (`shop badge_N`,
`shop post_hq`). No main-line CD sink is spine-owned; the sinks are shops/heals/decline-battle.

---

## 8. Implementation notes / FUTURE-ME HOOKS

**The spine is ~90% built. This is a wiring-and-gaps job, not a from-scratch build.** Files that
already carry the spine (do not recreate — extend):

- HUD ladder: `data/cobblemon_initiative/function/quest/render.mcfunction` (the master list),
  `quest/gym_town.mcfunction` (badge→town), `quest/set_gym.mcfunction` / `set_wheat.mcfunction`
  (macro renderers), `quest/load|tick|refresh|show|hide.mcfunction`.
- Memory drip: `function/memory/grant_fragment.mcfunction`, `function/memory/gym/frag_1..10.mcfunction`,
  `function/memory/init.mcfunction`. Fragment 1/7/10 authored; **2-6, 8-9 titles/subs still to write**
  (author in the existing `frag_N.mcfunction` stubs; keep them JSON-safe — no `"`, avoid `'`).
- Reveal: `function/reveal/board_fell.mcfunction`, `function/reveal/founder_defeated.mcfunction`.
- Act-2 economy: `function/economy/{gym_destabilize,hq_stabilize,stabilized,load}.mcfunction`.
- Gate config: DJ dialog `dialog-src/characters/villain/acting_ceo_dj.json` (the `monopoly_holds`
  vs `default` fields-gate); DJ trainer `trainers/villain_team/villain_team.json` id `villain_boss`.

**Concrete TODO to finish the spine:**
1. **Write frag_2..6, 8, 9 lines** in the existing `memory/gym/frag_N.mcfunction` files (LORE §8 gives
   the beats). Copy the `frag_7.mcfunction` shape exactly (`grant_fragment {n,title,sub}`).
2. **Add the field count to the pre-gate HQ line.** The `▶ Liberate wheat fields, then raid HQ` line
   currently omits the count. Mirror the `q.side_wheat` macro pattern (render.mcfunction lines 79-82 →
   `set_wheat` reads `fields_liberated` into storage; its denominator literal is still `/6` and must
   become `/10`) to show `n/10` on the main line, with the gate line reading **"Liberate 6 of 10"**
   (ruling 2026-07-06) — depends on wheat_war_farms wiring `fields_liberated` past 1 (see dependency
   below).
3. **PROPOSED memory re-reader** `spine_memory_journal`: new character JSON with `placement:{x,y,z}` in
   Sango near Mom + a dialog whose entries gate on `tag: memory_fragment_<n>` (priority ladder, highest
   recovered frag wins). Pipeline: `dialog-src/characters/sango/spine_memory_journal.json` +
   `dialog-src/dialog/spine_memory_journal.json` → `scripts/content_compile` →
   `generate_granary_tiers` → `update_preset_index` → `generate_npc_function`. Placement latch spawns
   it (WIRING RECIPE "New placed NPC"). Design-only for now.
4. **PROPOSED HQ telegraph** `spine_hq_courier` in Cyber City (armed at frag_7): a placement NPC or a
   line appended to `frag_7.mcfunction`. Backstop already exists (DJ's `monopoly_holds` entry), so this
   is polish.
5. **Add a routing coord to the board/founder HUD lines** (render.mcfunction 46, 51): append
   `[1590 51 1028]` so the post-league player is pointed back to the boardroom, matching the HQ line's
   format. (Board/Founder placements themselves are board_and_founder's job.)
6. **Dark Urge tier re-tune (bug — see gotcha).**

**GOTCHAS (bytecode/config-verified — do not relearn the hard way):**
- **Dark Urge tier breakpoints are keyed to the STALE ladder.** `NuzlockeConfig` sets
  `darkUrgeTier1LevelCap=30`, `tier2=52`, `tier3=73`. Under the canon ladder (…62/68/74/80), cap 52
  never exists (caps jump 50→56) and cap 73 first appears as gym 9's 74 — so **tier 3 currently
  triggers after gym 9, not after the HQ raid / gym 8** as LORE §8 and CLAUDE promise (52 and 73 are
  the *old* LORE table's Deepcore/Ryujin values). Fix: re-key to the canon ladder so tier 1 lands
  ~gym 2 (cap 30 ✓), tier 2 ~gym 4-5, **tier 3 at the HQ pivot** (cap 62, post-gym-7 — or gate tier 3
  on `defeated_villain_boss` if you want it to fire exactly on the raid). Exposed in ModMenu per the
  tunables preference — verify there, not just in JSON.
- **`memory_fragment` must be zero-init before any `matches`** (render.mcfunction line 10) — an unset
  score fails every `matches` test and would kill the whole pre-badge ladder. Never remove that line.
- **`royal_league_champion` is a defeat_tag override**, NOT `defeated_royal_champion`. Every spine
  consumer (HUD branches 2/3, board gates) uses the override. Do not "fix" it to the default pattern.
- **Founder alias:** the HUD/Mom read `defeated_villain_final_boss`; the canon flag is
  `company_overthrown`. `founder_defeated` grants both. Keep them together.
- **`hq_stabilize` is downward-only + double-fired** (reward AND onwin). Do not make it additive.
- **Macro text has no escaping** — fragment/economy/onwin lines: no `"`, avoid apostrophes.
- **No boss bar for countdowns reuses `cobblemon_initiative:objective`** — `quest/load` actively
  deletes it. Fresh bossbar ids only.
- **`memory_fragment` is the gym counter AND the gate for gyms 8-10 vs HQ.** Because branch (1)
  outranks branch (0) for badges 7-9, anything that reads "current gym" must respect that the HUD
  intentionally hides gyms 8-10 until `defeated_villain_boss`. Do not add a competing gym line.

---

## 9. Dependencies & open questions

### Depends on (other area keys)
- **gym_system_pvp_doubles** — every gym leader must fire `frag_N` + `gym_destabilize` + `shop badge_N`
  as `type:command` rewards and carry `achievementOnDefeat` (the cap linchpin). Gyms 8-10 must **not**
  carry `gym_destabilize` (idx is held at 25 post-HQ). The spine reads `memory_fragment`; the gyms
  write it.
- **wheat_war_farms** — the HQ hard gate needs `fields_liberated` to actually reach 6 (of the
  ten-field network, ruling 2026-07-06). ENGINE_FINDINGS
  flags only `farm_1` is wired today (`fields_liberated` maxes at 1) → the entire Act 1→2 gate is
  **currently impassable**, and the ruling raises the minimum wiring bar to five more fields.
  This is the single biggest spine blocker. The `n/10` HUD line (§8 TODO 2)
  also depends on it.
- **company_hq** — the HQ interior gauntlet up to DJ; the `villain_boss` body/placement at
  `1590 51 1028`. Spine provides the gate + HUD + stabilize; HQ provides the room.
- **royal_league** — champion must set `royal_league_champion`; provides the Act 2→3 hinge and cap 85.
- **board_and_founder** — board member + Founder bodies/placements in the boardroom; their `on_win`
  must call `reveal/board_fell` / `reveal/founder_defeated`. Owns the Founder team retune to a single
  L100 mirror. Spine provides the reveal functions + HUD routing.
- **(Sango prologue — no separate area key in the list)** — Mom + Acacia + starter bodies own the
  opening-chain bodies; spine owns the chain tags + HUD.

### Open questions (showrunner decisions)
1. **Founder = single L100 mirror or a full team?** CLAUDE.md says "a single level 100 mirror";
   `villain_team.json` `villain_final_boss` is a 6-mon L88-90 team. Which is canon for the final gate?
   (Affects cap-100 balance and the "fight yourself" fantasy — a mirror of the *player's own party*
   would be the strongest read but needs a build mechanism.)
2. **When exactly does Dark Urge tier 3 fire?** LORE says "post-gym-8 / after the HQ raid," CLAUDE says
   "post-gym-8," config currently yields post-gym-9. Pick one: cap-62 (post-gym-7, aligns with the HQ
   pivot) or a hard `defeated_villain_boss` gate. Recommend gating tier 3 on the HQ raid for the
   cleanest narrative ("the founder speaks plainly once the seat is in sight").
3. **HQ line field count — number RESOLVED, shipping still open.** The gate number is settled:
   **6 of 10** (majority rule, showrunner ruling 2026-07-06 — supersedes LORE §4's "4"). Remaining
   question is only implementation timing: showing `n/10` on the main line still requires
   wheat_war_farms to wire `fields_liberated` past 1 first. Yes recommended for stream clarity.
4. **Do we ship town-gate signposts, or rely on JourneyMap/Map Frontiers zones alone?** Map already
   labels every town; signposts are camera polish. Showrunner call on effort.
5. **Memory re-reader journal — in Mom's house or the Sango lab?** Mom's house ties it to the quiet-
   home theme; the lab ties it to Acacia/Pokédex. Pick a home for the recap device.
6. **DJ team retune** — DJ is fought at cap 62 but his team is L68-72. Confirm the brutal-Nuzlocke ace
   target (~64) and drop the whole team uniformly (this may be gym_system's or company_hq's retune,
   but the spine flags it because DJ is the Act-2 hinge).
