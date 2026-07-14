# 14 — Act 3: The Board of Directors & The Founder Mirror (cap → 100)

> Area key: `board_and_founder` • Act 3 • Post-Royal-League climax • The payoff of the whole amnesia arc.
>
> **SHOWRUNNER RULINGS BAKED IN (2026-07-06):** the Founder is a **TRUE MIRROR** of the
> player's actual party (ruling 1 — jar-verified feasible, see §8); the HQ is **one
> skyscraper in Cyber City** — first-floor entry, basement evil-HQ, corporate upper
> floors, penthouse on top (ruling 4); the **Deep Dark is fully optional** and hosts no
> story beat, so nothing in Act 3 lives there anymore (ruling 5).
>
> **State of the build:** unusually far along on paper. The five boss *characters*
> (`board_madeline/matt/micah/lauren`, `villain_final_boss`), both reveal functions
> (`reveal/board_fell`, `reveal/founder_defeated`), the cap-100 wiring
> (`PlayerProgressManager.board_cleared`) and the config teams
> (`villain_team.json`) **already exist** — and the Founder's shipped dialog already
> says *"You climbed the whole tower to get to me"* and *"walk back up the stairs"*,
> so the tower-ascent geometry lands with **zero dialog rewrites**. What is missing is
> *placement in the tower*, the *rctmod team files* TBCS actually reads, and the
> *mirror bridge* (small Java, spec in §8). Every coordinate I invent is marked
> **PROPOSED**.

---

## 1. Concept & fantasy

**One-line pitch:** You return to the skyscraper you once raided from below, ride the
executive lift up through four board members who voted to erase you, and in the
penthouse — your own home — the chair at the desk turns around wearing your own face,
fielding *your own team*. Then the game prints *your username* on the win screen.

The marquee stream moments, in order:

- **The ascent.** After the Champion, the arrow on the HUD points back to the tower on
  Cyber City's eastern seam — but this time the lift goes **up**. Act 2 went down into
  the basement to unseat the usurper; Act 3 climbs past the corporate floors to the
  people who actually signed the motion. The shipped Founder dialog already narrates
  this exact climb.
- **The four §k nameplates.** The board sit under static — `M§kaaaaaaaaa`,
  `M§kaaa`, `M§kaaaa`, `L§kaaaaa`. Each one talks like they *made* you — the one who
  proposed the motion, the protégé you trained, the accountant who reserved against
  your return, the Communications chief who wrote you out of the story. Chat spends
  four fights arguing about the redacted names.
- **Each empty chair tightens the static.** Every board defeat fires a title beat
  that circles the name without ever printing it — 1 seat, 2 seats, 3 seats, then
  *"The room is cleared. The static holds one name, and it is waiting for you."*
- **The mirror.** `§kfounder` waits in the **penthouse** (venue **RESOLVED** — showrunner
  ruling 2026-07-06: *"yes we fight them in the penthouse"*) — the floor where your Lopunny
  assistants kept the chair warm through all of Act 2. He **wears the player's own skin**
  (ruling: *"make sure the skin is the player"* — Easy NPC `SkinType.PLAYER_SKIN`, verified
  in `docs/EASY_NPC_REFERENCE.md` §SkinType: Mojang skin resolved from `SkinData.Name` +
  `UUID`; the mirror Java bridge stamps it, §8). He sends out an **identical copy of
  whatever party you walked in with**, normalized to level 100, under §k-static shadow
  nicknames. Not a themed "dark team" — *your* team. The dark styling is presentation; the
  roster is you. Pokémon-Red mirror, mechanised (ruling 1; feasibility jar-verified, §8).
- **He is not there until you are worthy.** The Founder's body **does not exist in the
  world until after the Champion falls** (ruling: *"the founder doesn't spawn until after
  champion"*) — the penthouse chair sits empty all run; post-Champion, the body appears
  in it (spawn latch gated on `royal_league_champion`, §8).
- **The name.** `reveal/founder_defeated` renders `{"selector":"@s"}` live: *"The
  Company is overthrown. The name on the chair was always ⟨YourStreamerName⟩."*
  And it lands **in the room with your name on the deed** — you face yourself in your
  own home. Whoever wins the run, the reveal is theirs — nothing baked into a shipped
  file.
- **The hand-off.** The HUD flips to `▶ Hunt the Ender Dragon`. Curtain on the arc;
  the reclaimed founder walks off the edge of the curated map into raw generated
  terrain, still hardcore + Nuzlocke.

---

## 2. Narrative role

| Field | Value | Source |
|---|---|---|
| Act | **3** (post-Royal-League) | character files `"act":"3"`; LORE_BIBLE §4 |
| `cd_instability` | **25 (held)** — DJ's Act-2 defeat already stabilised it (idx→25); Act 3 does not move the index. The economy plot is *resolved*; Act 3 is the *identity* plot. | LORE_BIBLE §4/§8 |
| Memory fragment | **None new.** All 10 fragments land by Scorchspire (frag_10 "face your own signature"). Act 3 *is* the fragment resolution — the reveal replaces the drip. The Act-2 penthouse fragment (owned by `company_hq`) is the *plant*; the penthouse mirror is the *payoff*. | LORE_BIBLE §8; `quest/render`; `09_company_hq` §5c |
| Recognition tier | **late** (all five characters `"recognition_tier":"late"`). Some board members **stand down emotionally** (the Fourth Seat, the Second Seat) rather than raging; the Third Seat doubles down; the First Seat is proud of the coup. | §4 gradient |
| Canon flags set | Board: `defeated_board_{madeline,matt,micah,lauren}` → derived `board_cleared` achievement → **cap 100**. Founder: `company_overthrown` (canon) + alias `defeated_villain_final_boss` (HUD/Mom). | §3 story-flag canon; `levelcaps.json` order 12 |
| Canon rules honoured | Never name the protagonist as the Founder before this beat; the §k static never resolves to letters early; the name is spoken **once**, at the mirror's death, and it is the *player's* own; Mom never learns the truth (she is not in this scene). | LORE_BIBLE §5, §9 |

---

## 3. Layout & placements

**Design decision — RESOLVED by ruling 4: there is ONE corporate site.** The HQ is a
**skyscraper at Cyber City's eastern seam** (`The Company, Inc.` VILLAIN zone,
install.json L592, adjacent to the Cyber City TOWN polygon at L1467). You enter on the
first floor; the **basement** is the evil HQ (the Act-2 raid — DJ's battle coord
`[1590 51 1028]` sits ~13 blocks below the zone's `centerY:64`, i.e. it *already is*
the basement); the **upper floors** read as an ordinary corporate building; the
**penthouse** is on top. The old two-site plan (west HQ / east Deep-Dark boardroom) is
**dead**: ruling 5 makes the Deep Dark fully optional with no story-required beat, so
the boardroom cannot live there. Act 3 returns to the same tower Act 2 raided — and
goes **up** instead of down.

**Real zones (grounded):**

| Zone | install.json | Extent | Role here |
|---|---|---|---|
| Royal League | L757, `TOWN` | polygon ~x[3425,3662] z[2752,2994], center `[3528 166 2773]` | Champion win = the Act-3 gate |
| The Company, Inc. | L592, `VILLAIN` | polygon ~x[1559,1678] z[983,1130] | **The tower.** Basement = Act 2 (DJ, grounded `[1590 51 1028]`); executive floors + penthouse = Act 3 |
| Cyber City | L1467, `TOWN` (Gym 7) | polygon ~x[1229,1401] z[959,1202] | The city the tower stands over; the seam between the two zones is the front door |

**Tower vertical program** (floor Y-values follow `09_company_hq`'s proposals; all
Act-3 interiors are **PROPOSED — needs builder confirm**; the multi-floor interior
itself is a builder dependency — showrunner rule: NPC placement only, no terrain):

| Floor | Y (PROPOSED) | Act | Role |
|---|---|---|---|
| Basement core | 51 (grounded) | 2 | Evil HQ — DJ's usurper boardroom (owned by `company_hq`) |
| Lobby / first floor | ~64 | 2+3 | Entry; keycard-gated executive lift |
| Corporate floors | ~66–82 | flavor | "Just a building" — desks, departments, scrubbing artifacts |
| **Executive floor — the boardroom** | **~85** | **3** | The four board seats; the door guard |
| **Penthouse** | **~95** | **3** | Your old home (Lopunnys, `company_hq` §5c) — **the mirror is fought here** (**RESOLVED** — ruling 2026-07-06; interior Y still builder-confirm) |

**Placements** (all Act-3 coords are **PROPOSED — needs builder confirm**; the column
core is `[1590 z1028]` per the grounded Act-2 coord):

| NPC / prop | Role | Coord | Status |
|---|---|---|---|
| `villain_grunt_11` "Company Boardroom Elite Agent" | last door guard at the executive-lift landing (already authored as a boardroom body; `npc_map_template.json` L188) | `[1590, 85, 1034]` | **PROPOSED** |
| `board_madeline` "the vote" | seat 1 (arc facing the door) | `[1586, 85, 1024]` | **PROPOSED** |
| `board_matt` "the protégé" | seat 2 | `[1589, 85, 1023]` | **PROPOSED** |
| `board_micah` "the accountant" | seat 3 | `[1592, 85, 1023]` | **PROPOSED** |
| `board_lauren` "the storyteller" | seat 4 | `[1595, 85, 1024]` | **PROPOSED** |
| `villain_final_boss` `§kfounder` | the chair at the penthouse desk — the one the Lopunnys "kept exactly how you left it". **Spawns only after the Champion falls** (latch gated `royal_league_champion`); wears the **player's own skin** (`PLAYER_SKIN`, stamped by the mirror bridge, §8) | `[1590, 95, 1024]` | **PROPOSED coord; timing+skin RESOLVED** |
| (prop) scrubbing artifact: brighter rectangle where a portrait hung | behind the penthouse desk (already planted by `company_hq` §5c) | co-locate w/ chair | **PROPOSED (builder art)** |

The `villain_team.json` entries for all five currently carry `coordinates:[0,0,0]`
(unplaced); these become the real tower coords, or the bodies are builder-placed and
adopted by `uuid` (see §8).

**Deep Dark Cave:** no longer referenced by this area at all. It stays in the world as
fully optional side content (open entry, free exit, no story lock — ruling 5), owned
entirely by `deep_dark_cave`.

---

## 4. Core structure — the boardroom gauntlet (not a gym; a linear elimination)

Not a PvP ladder in the gym sense (no interior trainers). It is a **four-seat
elimination on the executive floor followed by the mirror in the penthouse**, all
forced encounters, gated so you cannot skip to the top of the tower.

**Entry gate (all four board members):** dialog `gate:{champion:true}` →
compiles to `PLAYER_TAG EQUALS royal_league_champion` (content_compile L196-198).
So the executive floor only opens to the Champion. (The `villain_team.json`
prerequisites `["villain_boss","royal_champion"]` are the rctmod graph edges — inert,
spawning is off — the *real* lock is the dialog condition + the onwin tags.)

**The four seats — order is player's choice, difficulty escalates by intent, not by
gate.** Each is `GEN_9_SINGLES`, `recipe: gauntlet_boss`, `despawn_on_win`, and fires
`reveal/board_fell` on win. Recommended identities + ~6-mon sketches. Board is fought
**at cap 85** (Champion just unlocked 85; `board_cleared` won't raise it to 100 until
all four are down), so **aces sit at 86-87 — the brutal "+2 over the entry cap"
rule**, the last legal cruelty before the mirror.

| Seat | §k name (canon) | Identity / archetype | Team sketch (ace = 86-87) |
|---|---|---|---|
| the First Seat | `M§kaaaaaaaaa` | *"I proposed the motion"* — Fairy/Psychic control, reads the figures aloud | Sylveon 84 · Togekiss 84 · Slowking 85 · Hatterene 85 · Gardevoir 86 · **Alakazam 86 (Magic Guard, Focus Sash)** |
| the Third Seat | `M§kaaa` | *"You taught me people are positions"* — the protégé, Dark offensive mirror-of-you | Mightyena 84 (grunt callback) · Krookodile 85 · Weavile 85 · Houndoom 85 · Hydreigon 86 · **Kingambit 86 (Supreme Overlord)** |
| the Second Seat | `M§kaaaa` | *"a low-probability liability"* — Steel/defensive, calculated, confidence-interval | Bronzong 84 · Ferrothorn 85 · Magnezone 85 · Excadrill 85 · Aegislash 86 · **Metagross 86 (Tough Claws)** |
| the Fourth Seat | `L§kaaaaa` | *"my department wrote you out"* — Communications, Ghost/illusion/narrative control (fought last if the player reads the room; slightly hardest) | Cofagrigus 85 · Gengar 85 · Chandelure 86 · Mimikyu 86 (Disguise = the mask) · Hatterene 86 · **Dragapult 87 (Choice Specs)** |

**The mirror — `villain_final_boss` (`§kfounder`), in the penthouse.** Gated
`all_tags:[defeated_board_madeline, defeated_board_matt, defeated_board_micah,
defeated_board_lauren]` (already in the character file → four `PLAYER_TAG EQUALS`
conditions). A lower-priority `not_ready` entry (priority 5) already turns him away
while chairs are full. `GEN_9_SINGLES`, `prize:0` (you are taking *everything*, not
cash), `on_win → reveal/founder_defeated`, sets `company_overthrown`.

**Mirror mechanic (RESOLVED — ruling 1, Plan A):** the moment the *Face myself* button
is pressed, the party is **snapshotted** and registered as the Founder's live team —
same species, same moves, levels normalized to **100**, shadow-styled nicknames. See
§6 for the plan ladder and §8 for the jar-verified implementation.

**No double battle in this gauntlet.** The board are individual votes; forcing two
into a `GEN_9_DOUBLES` tag-team would read as "henchmen," which undercuts "each of
these people decided, alone, to erase you." Keep them sequential singles. (If the
showrunner wants a doubles beat, the cleanest spot is a *pre-room* pair — two
`villain_grunt_11`-tier lift guards — not the board itself. See §9 Q5.)

**Gate wiring summary (all proven Condition types):**

```
Champion win ──> royal_league_champion tag
   └─ opens all four board dialogs (gate: champion)
board_X win  ──> defeated_board_X tag  +  reveal/board_fell (title beat by seat count)
   └─ 4/4 tags ──> Founder dialog "the mirror" entry unlocks (all_tags)
              ──> PlayerProgressManager sees all board_member configs defeated
                    ──> grants board_cleared achievement ──> LevelCapManager cap 100
Founder button ──> mirror snapshot (party copied @ press-time, levels → 100)
              ──> tbcs battle vs the registered mirror team
Founder win  ──> company_overthrown + defeated_villain_final_boss
              ──> reveal/founder_defeated (live @s name)  ──> HUD: Hunt the Ender Dragon
```

---

## 5. Quests & side beats

The climax is deliberately lean on side quests — it should feel like a corridor (a
vertical one now), not a hub. Three optional beats add texture without diluting the
ascent:

### Q1 — "The Elevator That Only Goes Up" (approach gauntlet)
- **Giver:** environmental / `villain_grunt_11` (existing boardroom door guard).
- **Hook:** the executive lift is the only way past the corporate floors. A short
  chain of already-authored late-game villain bodies line the lift landings as a
  warm-up gauntlet (reuse, don't invent: `villain_admin_commander` — his file already
  says *"I have watched that walk in old boardroom recordings"* — and
  `villain_grunt_11`).
- **Steps:** fight up the landings → the last guard (`villain_grunt_11`) blocks the
  boardroom door with *"They told me nobody gets past me. They told me a lot of
  things."*
- **Gates:** `champion:true` to enter; each guard `not_tag defeated_<id>` (standard
  onwin latch).
- **Rewards:** CD prize per guard (flat onwin, existing pattern); no training loot
  (repeatable-safe rule doesn't apply, but these are pure gates — keep them lean).
- **Resolution:** door guard despawns → the table is exposed; the penthouse lift
  (unlocked since Act 2's DJ override, `company_hq` §5c) waits above.

### Q2 — "The Minutes" (collectible lore, optional)
- **Giver:** scrubbing artifacts along the corporate + executive floors (LORE_BIBLE §9
  "DO litter the world with scrubbing artifacts").
- **Hook:** re-verified ledger pages and revised org charts show the erasure
  *in progress* — the same name whited out four times, each by a different department.
- **Steps:** read 4 lecterns/item-frames (one per board member's department, one per
  corporate floor).
- **Gates:** none (ambient). Sets cosmetic `read_minutes_n` tags for a HUD flourish.
- **Rewards:** flavour only — the audience assembling the cover-up before the reveal.
- **Resolution:** reading all four before the Founder unlocks one extra Founder
  pre-battle line (optional `any_tags` fanout). **PROPOSED, low priority.**

### Q3 — "Mom, After" (post-story, in Sango — cross-references area `mainline_spine`)
- **Giver:** Nalia (Mom) in Sango Town, gated `tag:defeated_villain_final_boss`.
- **Hook:** the reclaimed founder comes home before walking off the map. She does not
  learn the truth (canon §9); her arc resolves on *presence*.
- **Steps:** talk to Mom → she remarks her kid keeps coming back → she deflects any
  line that gestures at the Company.
- **Gates:** `defeated_villain_final_boss` (the reveal alias Mom's homecoming entry
  already keys on, per `reveal/founder_defeated` comment).
- **Rewards:** a single home-cooked-meal item + the emotional button on the arc.
- **Resolution:** the HUD's `▶ Hunt the Ender Dragon` line is now the only thing left.
- **Owner note:** this beat is authored under `mainline_spine`, referenced here so the
  reveal alias stays consistent. Do not duplicate the character.

---

## 6. Trainers & teams needed

**Characters — EXIST, no new authoring:** `board_madeline`, `board_matt`,
`board_micah`, `board_lauren`, `villain_final_boss` (all with full dialog + battle
blocks + reveal hooks). The Founder's dialog already reads tower-shaped ("climbed the
whole tower", "walk back up the stairs") — no rewrite needed for the new geometry.

**rctmod team files — MUST CREATE** (they do **not** exist under
`data/rctmod/trainers/`; TBCS reads *these*, and an absent/empty `{}` team = a battle
button that silently no-ops — ENGINE_FINDINGS §2). The team *data* already exists,
authored, inside the `villain_team.json` aggregate; the build step is **splitting it
into per-id rctmod files** in the RCT `"team":[...]` shape (see
`rctmod/trainers/takehara_leader.json` for the exact schema: top-level `name`,
`identity`, `ai`, `battleFormat`, `bag`, `team` as a bare array).

| Create file | Source team | Format | Levels (recommended) |
|---|---|---|---|
| `data/rctmod/trainers/board_madeline.json` | villain_team.json L1530 (Fairy/Psychic, currently 4 mons @83-85) | GEN_9_SINGLES | expand to 6, ace 86 |
| `data/rctmod/trainers/board_matt.json` | villain_team.json L1681 | GEN_9_SINGLES | 6 mons, ace 86 |
| `data/rctmod/trainers/board_micah.json` | villain_team.json L1832 | GEN_9_SINGLES | 6 mons, ace 86 |
| `data/rctmod/trainers/board_lauren.json` | villain_team.json L1983 | GEN_9_SINGLES | 6 mons, ace 87 |
| `data/rctmod/trainers/villain_final_boss.json` | villain_team.json L2134 (Zoroark/Weavile/Tyranitar/Hydreigon/Mewtwo/Darkrai @88-90) | GEN_9_SINGLES | **flat 100** — this file is the Plan-C fallback *and* the pre-registered base whose `ai`/`bag` the runtime mirror reuses (§8) |

**The mirror roster — RESOLVED (ruling 1): TRUE MIRROR, with a fallback ladder.**

- **(Plan A — SHIPPING TARGET) True mirror, runtime snapshot.** The Founder fields an
  **identical copy of the player's actual party**, snapshotted **at the moment the
  battle button is pressed**: same species, same movesets, levels normalized to 100.
  Dark styling is *presentation layered on top*, not a different roster — §k-static
  shadow nicknames on each copied mon, the `§kfounder` nameplate, and the reveal line
  printing the player's own name. Feasibility is **jar-verified FEASIBLE-WITH-WORK**
  — every required API exists in the pinned rctapi/tbcs jars; the work is a small,
  well-defined Java bridge + one content_compile flag (full spec + citations in §8).
- **(Plan B — fallback if Plan A hits an unexpected runtime wall) Starter-keyed
  archetype mirror.** Read the starter the player claimed
  (`claimed_starter_{skiddo,totodile,growlithe_hisui}` tags — ENGINE_FINDINGS §2;
  trio is Skiddo/Totodile/Hisuian Growlithe) and fan the Founder's battle button via
  `any_tags` (content_compile fanout, L190-191) to three static trainer ids
  `founder_shadow_{grass,water,fire}`, each **led by a Zoroark whose Illusion
  disguises it as the player's starter's final evolution** (Gogoat / Feraligatr /
  Hisuian Arcanine) — order the disguise target last in the team array so Illusion
  picks it. First send-out reads as the player's own ace, then the Illusion breaks.
  Remaining five keep the cold-logic legendaries.
- **(Plan C — last resort, ships today) Fixed cold-logic shadow.** The existing
  `villain_team.json` roster (Zoroark · Weavile · Tyranitar · Hydreigon · Mewtwo ·
  Darkrai) bumped to flat 100. Under Plan A this team is what the
  `villain_final_boss.json` rctmod file carries anyway — so if the snapshot command
  ever fails, the button still starts *a* Founder fight instead of no-oping.

**Config registry — EXISTS:** the five entries live in
`trainers/villain_team/villain_team.json` with rewards, `achievementOnDefeat`
(`company_overthrown` on the Founder; `null` on the board — the cap rides the derived
`board_cleared`, see §8), and `board_member` trainerType (the key
`PlayerProgressManager` filters on). Only the `coordinates` need filling (or the bodies
placed by uuid).

---

## 7. Economy & rewards

Act 3 is a **CD sink and a status payout**, not a farm. The index is already stable
(25), so no `economy/*` calls fire here.

| Beat | Reward | Notes |
|---|---|---|
| Each board member | `prize: 9000` CD (onwin flat) + 5× rare candy + 3× diamond (config rewards) | already in the character files / config. 9000×4 = 36k CD — the endgame windfall that funds Battle Frontier entry. |
| The Founder | `prize: 0` + Master Ball + 2× netherite ingot + **64× wheat** | the wheat is the thematic mic-drop — the monopoly's own commodity handed back. `prize:0` because the reward is the *name*, not cash. |
| Cap unlock | `board_cleared → 100` | not an item; the systemic reward. |
| Shop tier | **no new tier.** Shop tiers step per badge and ease per liberated field; Act 3 introduces no new catalog. The Battle Frontier (area `battle_frontier`) owns any post-100 economy. |

CD sink alignment: the 36k board windfall is deliberately the on-ramp to Battle
Frontier buy-ins (that area's problem to price), not spent in Act 3 itself. No CD is
required to *enter* the boardroom — the gate is the Champion badge, not a toll.

---

## 8. Implementation notes / FUTURE-ME HOOKS

**What already exists (do NOT rebuild):**
- Characters: `dialog-src/characters/villain/board_{madeline,matt,micah,lauren}.json`,
  `villain_final_boss.json` — full dialog, `recipe:gauntlet_boss`/`villain_boss`,
  battle blocks with reveal hooks.
- Reveal: `.../function/reveal/board_fell.mcfunction` (recounts `defeated_board_*`,
  fires a title beat per emptied seat: 1→"a seat empties" … 4→"the room is cleared /
  go and read the name") and `.../reveal/founder_defeated.mcfunction` (live
  `{"selector":"@s"}` name, sets `defeated_villain_final_boss` alias). **Both stay
  exactly as they are** — the four oblique board beats and the live-name reveal are
  venue-independent.
- Cap: `PlayerProgressManager.java` L478-487 grants `board_cleared` when **all
  `board_member`-type configs** are defeated → `LevelCapManager` → 100
  (`levelcaps/levelcaps.json` order 12). **No datapack grant needed — it's Java.**
- HUD: `function/quest/render.mcfunction` already has the Act-3 ladder
  (`Hunt the Board` → `Face The Founder` → `Hunt the Ender Dragon`), keyed on
  `#board quest_hud matches 4..` and `defeated_villain_final_boss`.
- Config + teams (as data): `trainers/villain_team/villain_team.json`.

### The mirror bridge (Plan A) — JAR-VERIFIED FEASIBILITY + spec

**Verdict: FEASIBLE-WITH-WORK.** Every load-bearing API exists in the pinned jars;
none of it is speculative. Evidence (decompiled 2026-07-06 from the pack-pinned jars,
`mrpack/modpack.json` → content-addressed `mrpack/cache/`; rctapi 0.15.2-beta =
`cache/a75bb662e5…`, tbcs 0.14.1-beta = `cache/4e559f60e9…`; `nix develop` javap):

1. **We can already enumerate the player's party** — proven in our own shipped code:
   `NuzlockeInit.java` (L98, L230, L257, L302, L343, L405, L491) calls
   `Cobblemon.INSTANCE.getStorage().getParty(player)` →
   `com.cobblemon.mod.common.api.storage.party.PlayerPartyStore`, iterated directly as
   `Pokemon` (species, level, moves all readable; null slots already handled by
   `countPartySize`).
2. **RCTAPI has a full programmatic team-registration API**
   (`com.gitlab.srcmc.rctapi.api.*`, jar-verified signatures):
   - `trainer.TrainerRegistry.registerNPC(String id, models.TrainerModel)` → `TrainerNPC`,
     an overload `registerNPC(String id, T extends TrainerNPC)` for pre-built trainers,
     plus `unregisterById(String)` and `getById(String[, Class])`.
   - `models.PokemonModel` has a **copy-constructor `PokemonModel(Pokemon)`** — built
     straight from a live Cobblemon party member (it even maps the live moveset) — and
     a full-arg constructor taking `(species, nickname, gender, level, nature, ability,
     moveset, ivs, evs, shiny, heldItems, aspects, gimmicks)`. `PokemonModel` has **no
     setters**, so the level-100 normalization uses the full-arg constructor populated
     from the live mon (level overridden to 100, nickname overridden to the §k shadow
     styling).
   - `models.TrainerModel(Text name, JTO<BattleAI> ai, List<BagItemModel> bag,
     List<PokemonModel> team)` — the same model class TBCS deserializes trainer JSON
     files into.
   - `trainer.TrainerNPC(Text, Pokemon[], TrainerBag, BattleAI, LivingEntity)` even
     accepts a live `Pokemon[]` directly and defensively copies it (private static
     `copyTeam`) — an alternative construction path that never mutates the real party.
3. **TBCS resolves `vs <trainerId>` from the LIVE registry at command-execution
   time** — not from baked datapack state. Bytecode
   (`com.gitlab.srcmc.tbcs.commands.CommandsContext.battle()`/`getAsTrainer()`):
   `RCTApi.getInstance(getPrefix()) → getTrainerRegistry() → getById(id)` runs inside
   the command executor, then `getBattleManager().start…`. The file pipeline is the
   *same* registry: `api.TBCS.loadTrainers()` scans `trainers/*.json` → Gson →
   `TrainerModel` → `registerNPC`. A trainer registered programmatically into that
   registry (`TBCS.getInstance().getTrainerRegistry()` — public API, jar-verified) is
   indistinguishable from a file-loaded one at battle time.

**Work to build (the "WITH-WORK" part):**
- **Dependency pins:** add the pinned rctapi (and tbcs, for `TBCS.getInstance()`)
  jars as `compileOnly` deps in `build.gradle.kts`; guard all calls behind
  `FabricLoader.isModLoaded(...)` (ENGINE_FINDINGS bare-mod rule: the mod must not
  crash without the pack).
- **Snapshot command:** `/cobblemon-initiative mirror @s` (name TBD) — on execution:
  read the party (API above), build level-100 shadow `PokemonModel`s, assemble a
  `TrainerModel` reusing the **`ai` + `bag` of the already-registered
  `villain_final_boss`** (readable via `getById(id, TrainerNPC.class).getBattleAI()` /
  `.getBag()`), set the model `name` to **exactly the file team's name** (so
  BattleVictoryEvent reporting is unchanged — see gotchas), then
  `unregisterById("villain_final_boss")` + `registerNPC("villain_final_boss", model)`.
  **Same-id override is deliberate:** attach, `vs`, onwin, and the Plan-C fallback all
  keep working with zero dialog changes, and TBCS's own `loadTrainers()` restores the
  file team on any reload.
- **Button lowering:** content_compile already lowers `do:battle` to an action
  *sequence* (`tbcs attach` raw → ExecAsUser `tbcs battle … onwin {…}` → CLOSE_DIALOG;
  scripts/content_compile L423-473). Add a `"mirror": true` battle-block flag that
  **prepends** the snapshot command as one more ExecAsUser action (same `not_tag`
  condition). Snapshot-at-button-press is exactly the recommended timing — the party
  copied is precisely the party carried into the fight, and re-registering on every
  press self-heals any datapack-reload wipe of the runtime registration.
- **Allowlist:** the snapshot command runs ExecAsUser, so it must be added to Easy
  NPC's `executeAsUserCommandAllowList` (ENGINE_FINDINGS: an empty/missing allowlist
  entry silently blocks EVERY ExecAsUser command; ship it in the mod's self-healed
  security config + the mrpack override, same as the existing entries).
- **SMOKETEST items:** (a) mirror team matches party species/moves, levels all 100,
  shadow nicknames render; (b) Founder win still grants `company_overthrown` (name
  match, below); (c) press → flee → re-press re-snapshots after a party change;
  (d) `/reload` between board fights doesn't strand a stale mirror registration.
- **Edge cases (flavor calls, §9 Q6):** a 1-mon party mirrors as a 1v1 (rctapi only
  refuses *empty* teams — `insufficientPokemon`, ENGINE_FINDINGS §2); fainted mons
  copy as healthy level-100s on the mirror's side. Recommended read: the mirror
  copies **exactly what you bring** — bring six or face yourself honestly either way.

**Other build steps (unchanged in substance):**
1. **Split teams into rctmod files.** Create the five files in §6 from the
   `villain_team.json` team blocks, converted to the RCT `"team":[...]` array shape
   (copy `rctmod/trainers/takehara_leader.json` as the template). Verify no `{}`
   ships — empty team = silent no-battle (ENGINE_FINDINGS §2). The
   `villain_final_boss.json` file ships the Plan-C roster at flat 100.
2. **Place the five bodies in the tower.** Two options (ENGINE_FINDINGS §3 PLACEMENT
   LATCHES): (a) add `placement:{x,y,z}` to each character file (no uuid) → generated
   `ambient/place/<key>` spawns once per world; or (b) builder places the bodies on
   the executive floor / penthouse and each character adopts by `uuid:[...]`. Given
   both interiors are *built spaces* (art, not just a zone), **prefer (b)
   builder-placed uuid for the FOUR BOARD members** so they sit exactly on their
   chairs. Either way fill the real coords (replace the `[0,0,0]` in
   `villain_team.json`). **Latch-spawned NPCs get random uuids**, so if any is a
   sight/approach trigger it needs a manual `npcsight add <uuid>` pass.
   **EXCEPTION — the Founder CANNOT be a pre-placed body** (ruling 2026-07-06: *"the
   founder doesn't spawn until after champion"*). His spawn is a **champion-gated
   latch**: the standard generated proximity latch fires only for
   `@a[tag=royal_league_champion, distance=..40]` — note the generated
   `ambient/place/<key>` gate is proximity-only today, so either extend
   `content_compile` with a `spawn_gate` field (cleanest; one selector-condition
   splice in `write_placements`) or hand-author
   `function/reveal/founder_spawn.mcfunction` on the same latch pattern
   (`#amb_founder` on `ci_ambient`, champion-tag selector, `easy_npc preset
   import_new`, latch-before-import) and call it from the champion-win reward chain.
2b. **The Founder wears the player's skin** (ruling: *"make sure the skin is the
   player"*). Verified mechanism (`docs/EASY_NPC_REFERENCE.md` §SkinType):
   `SkinData{Type:"PLAYER_SKIN", Name:<player name>, UUID:[I;<player uuid>]}` resolves
   the Mojang skin. The player's name/UUID are unknowable at authoring time, so the
   **mirror Java bridge stamps SkinData at runtime** — once when the champion-gated
   spawn fires (the bridge already exists for the party snapshot; single-player = the
   one player), and re-stamped at *Face myself* button press so a renamed account
   never shows a stale skin. Ship the preset with `Type:"NONE"`/blank so no default
   Steve frame ever flashes pre-stamp.
3. **Run the pipeline, in order:** `scripts/content_compile` (lowers dialog-src →
   `data/easy_npc/preset/humanoid(_slim)/<id>.npc.snbt`, rebuilds `band_tags`) →
   `scripts/generate_granary_tiers` → `scripts/update_preset_index` →
   `scripts/generate_npc_function` (writes `npc/preset_map.json` +
   `function/update_npc_presets.mcfunction`). content_compile auto-runs the last step;
   granary tiers must precede the final hash.

**Patterns to copy:** the whole boss loop mirrors **Gym 2 Hua Zhan City's leader
block** for the gate→battle→onwin shape, and **`acting_ceo_dj.json`** (the Act-2 boss)
for a `gauntlet_boss`/`villain_boss` with a two-entry dialog (a "not ready" gate + the
fight) — the Founder file is already built exactly on that shape.

**Gotchas (load-bearing):**
- **onwin tokens are WINNERS-FIRST** (`@1` = player on a win). The reveal calls already
  correctly use `execute as @1 at @1 run function …`. Don't "fix" them to `@2`.
- **Macro-safe text.** Any text delivered through a macro/`title`/`say` must contain
  **no double-quotes and avoid apostrophes** (datapack macro layer has no escaping).
  The reveal functions and dialogs already comply; keep new lines clean.
- **The §k static must never resolve early.** Nameplates stay fully obfuscated the
  whole run; no letters surface; **no name is baked into any shipped file.** The only
  name ever printed is `{"selector":"@s"}` at the mirror's death. The mirror's shadow
  nicknames are §k-styled copies generated at snapshot time — never persisted.
- **Two defeat signals must both land per board member:** the onwin `tag add
  defeated_board_X` (HUD/Founder gate) *and* the Cobblemon `BattleVictoryEvent`
  config-name match that `PlayerProgressManager` uses for `board_cleared`. TBCS
  battles are real Cobblemon battles so the event fires — **verify the board config
  `displayName`/name matches what BattleVictoryEvent reports**, or the cap won't
  unlock even though the HUD advances. **For the Founder this now has a third leg:**
  the runtime-registered mirror's `TrainerModel.name` must equal the file team's name
  so `company_overthrown` still matches. (SMOKETEST.md lines for all three.)
- **Registry lifecycle:** TBCS's `loadTrainers()` (datapack reload) re-registers the
  file team over the runtime mirror. That is a *feature* under the same-id design —
  the file team is the standing fallback — but it means the snapshot MUST run on every
  button press, never once-per-world.
- **Cap ordering:** `board_cleared` → 100 fires *before* the Founder, by design — the
  player grinds to 100, then fights the level-100 mirror (which is level-100
  regardless, per ruling 1's normalization). Do not gate the cap on
  `company_overthrown`.

---

## 9. Dependencies & open questions

**Depends on (other area keys):**
- `royal_league` — the Champion win (`royal_league_champion` tag) is the Act-3 gate.
- `company_hq` — **now the hard geometry dependency.** Owns the tower (ruling 4
  vertical program: lobby / basement / corporate floors / penthouse), the Act-2 raid,
  the keycard lift, and the penthouse Lopunny scene whose "the chair has been kept
  exactly how you left it" line is the plant this area pays off. Act 3 cannot place
  bodies until the executive floor + penthouse interiors are builder-real.
- `mainline_spine` — owns the HUD ladder, `memory_fragment` score, the cap ladder, and
  Mom's post-story homecoming (Q3). Reveal aliases must stay in sync.
- `battle_frontier` — owns the post-100 economy the board windfall feeds.
- `legendaries_nobles` — the Plan-B/C legendary rosters (Mewtwo/Darkrai) should not
  collide with wild-legendary balance/availability there. (Plan A sidesteps this
  entirely — the mirror only fields what the player owns.)
- `wheat_war_farms` — supplies the resolved-economy backdrop (idx 25) and the wheat
  mic-drop reward.
- ~~`deep_dark_cave`~~ — **dependency removed** (ruling 5). Fully optional side
  content; no Act-3 asset lives there.

**Resolved by showrunner rulings (2026-07-06) — folded into the body above:**
- Mirror roster → **true mirror of the player's actual party** (Plan A), dark styling
  as presentation only; Plan B (starter-keyed fanout) and Plan C (fixed shadow team)
  demoted to fallbacks. *(was Q1)*
- Founder level → **flat 100** (ruling 1 normalizes the mirror to 100; matches
  CLAUDE.md's "single level-100 mirror"). *(was Q3)*
- East/west two-site split → **dead.** One skyscraper in Cyber City; Act 2 goes down,
  Act 3 goes up. *(was Q6)*
- **Mirror venue → PENTHOUSE (ruling 2026-07-06:** *"yes we fight them in the
  penthouse"*). You face yourself in your own home; *"the name on the chair was always
  yours"* lands in the room with your name on the deed. `09_company_hq`'s "Board +
  Founder re-populate the boardroom" line (its §1/§5c) needs the one-line sync to
  "Board in the boardroom; Founder in the penthouse." *(was Q1)*
- **Founder skin = the player's own** (ruling: *"make sure the skin is the player"*) —
  `PLAYER_SKIN` stamp via the mirror bridge, §8 item 2b.
- **Founder spawn timing = post-Champion** (ruling: *"the founder doesn't spawn until
  after champion"*) — champion-gated latch, §8 item 2; the penthouse chair sits empty
  until then.

**Decisions the showrunner must still make:**
2. **Builder geometry:** confirm the tower's executive floor + penthouse interiors
   exist (or get built) — every Act-3 coordinate in §3 is PROPOSED against the
   `[1590 z1028]` column and `09_company_hq`'s floor Y proposals.
3. **Boardroom placement: builder-placed uuid bodies, or `placement`-latched spawns?**
   Recommend builder uuid so the five sit exactly on their chairs.
4. **Board fullness/aces:** expand each board member from 4 → 6 mons and set aces at
   86-87 (entry-cap 85 +2, the brutal rule)? (Recommend yes.)
5. **Any doubles beat?** Keep the four board as sequential singles (recommended, for
   the "each decided alone" read); if a `GEN_9_DOUBLES` is wanted, put it on a
   *pre-room* pair of lift guards, never the board.
6. **Mirror edge-case flavor:** copy the party *exactly as brought* (including a 1-mon
   party → 1v1, fainted mons mirrored healthy at 100)? Recommended yes — "the only
   honest mirror." Alternative: refuse the button below N mons (extra gate, not
   recommended).

**Standing verification item (keep on SMOKETEST.md):** `board_cleared` cap-100 needs
the `BattleVictoryEvent` loser-name to match each board config `displayName`/name —
separate from the `onwin defeated_board_*` tags — and, new under Plan A, the
runtime-registered mirror's `TrainerModel.name` must keep the Founder's
`company_overthrown` name-match intact.
