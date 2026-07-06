# 17 — Deep Dark Cave (OPTIONAL endgame mega-dungeon): The Last Audit

> **Area key:** `deep_dark_cave`
> **SHOWRUNNER RULING (2026-07-06):** the Deep Dark is **fully optional** — no story beat
> requires entering, nothing locks the player in, and the exit is always available ("it's
> just open"). The Founder mirror does **NOT** live here — it sits in the Company HQ
> skyscraper penthouse in Cyber City (`board_and_founder` owns it; see
> `14_board_and_founder.md`). This doc is rewritten to that ruling.
> **Owns:** the map's optional endgame dungeon — the huge sculk zone where the Warden
> hunts. Designs what the player *does* here: a dread-heavy, hardcore-SURVIVABLE
> sculk-stealth descent for treasure, lore, and an optional legendary. Owns the entrance
> NPC, the warning monument, the descent traversal, its safety valves, and the
> reserve-vault lore payoff. **Does NOT own:** the Founder mirror battle (that is
> `board_and_founder`, at the HQ penthouse — see `14_board_and_founder.md`), the
> legendary's canon slot (`legendaries_nobles`), or the identically-named Battle-Frontier
> *facility* (see the name-collision warning in §8 — that is `battle_frontier`, a
> different thing entirely).

> **Status of what exists:** the zone is real in `install.json` (line 355, "Deep Dark Cave").
> **No NPCs, dialog, or trainers reference the real zone yet.** The only pre-existing thing
> wearing this name is the Battle-Frontier `deep_dark_cave` *facility group*
> (`trainers/battle_frontier/battle_frontier.json` L1119-1306: `cave_challenger_1/2`,
> `frontier_brain_cave` "Cave Warden Selene") — but those bodies live at `~[3812,159,2996]`
> **inside the Battle Frontier hub**, not in this zone. This doc builds the real zone from zero.

---

## 1. Concept & fantasy

**One-line pitch:** *The lightless pit The Company dug to reach the money itself is just…
open. No quest sends you down. No door closes behind you. But everyone who looks at the
mouth of it knows there is treasure at the bottom, a truth nobody was ever supposed to
audit — and one enemy in this run that levels cannot beat.*

The fun:
- **The one enemy levels don't beat.** The whole run has been a level-cap power fantasy — you win
  by out-preparing. The Warden inverts it: a level-100 team means *nothing* to a sonic boom. For
  one area, the game stops being about your party and starts being about *you*, sneaking, in the
  dark. That it is entirely the player's choice to be down here is the point — classic
  optional-superdungeon energy (the Battle Tower basement, the bonus dungeon after the credits).
- **"DON'T RUN." — the chat-scream traversal.** Sculk sensors and shriekers turn the descent into a
  vibration-management puzzle played live. Every viewer who knows the deep dark is white-knuckled;
  every viewer who doesn't learns the rule the hard way *with* the streamer. The marquee stream
  moment is the first shrieker chase: the darkness fills with the warning pulse, the streamer
  sprints for the nearest lit booth, and the sculk goes silent one beat before the Warden lands.
- **The reserve is hollow (the corporate-dread payoff).** CobbleDollars are nether-star-backed
  (LORE §3). Down here is *the reserve* — the literal vault the whole economy trusts is real. The
  descent's lore beat: **the vault was mined out years ago.** The money was never fully backed. The
  Company could destabilise the currency because *they knew it was already a lie* — and the first
  withdrawal slip carries a signature you are about to recognise. It recontextualises the entire
  economy plot in one dark room, without ever printing a name. **Optional lore, deliberately:**
  the mainline reveal is complete without it (the Founder fight at the HQ penthouse carries the
  canon reveal — `14_board_and_founder.md`); this is the deep-cut for the player who goes looking.
- **The door is never locked — in either direction.** "You can leave whenever" is a design
  pillar, not a footnote: the Recall Beacon, the booth checkpoints, and the no-one-way-drops
  build rule (§4) make walking away free at every step. The dungeon's tension is 100% opt-in.

---

## 2. Narrative role

| Field | Value | Source |
|-------|-------|--------|
| Act | **None required — fully optional** (showrunner ruling 2026-07-06). Mechanically open from the moment the player can walk there; *tuned* for the post-`board_cleared` window (cap 100), which is the recommended and signposted time to dive | ruling; CLAUDE.md ladder (Board cleared → 100) |
| Story gate | **NONE.** No mainline beat routes through the cave; the Founder is reached via the HQ skyscraper penthouse (`14_board_and_founder.md`), never through here | ruling 2026-07-06 |
| `cd_instability` | **held at 25** (Act 3 is flat post-DJ; nothing here destabilises or stabilises — the number only moves again on `company_overthrown`) | LORE §8 (idx 25 through Act 3); `mainline_spine` §7 |
| Memory fragment | **No numbered fragment.** frag_1..10 end at Scorchspire. The cave delivers an **optional, wordless memory beat** (the withdrawal-slip signature) that circles the reveal without closing it — and is never required; the reveal itself belongs to the Founder fight | `mainline_spine` §5.2 (ten frags end at gym 10); LORE §5 |
| Recognition tier | **late / terminal.** There are no living Company staff to recognise the face down here — only the sculk's *recordings* of them (optional echoes, §5.3) and the signature. The world's recognition arc has run out; only self-recognition is left | LORE §4 gradient; §5 |
| Level context | The **zone** is open at any level; the **content band** is 100 (the legendary and echo shades are L100-band). The gatekeeper warns underleveled parties in dialog — a warning, **never a block**. The Warden itself is level-agnostic: sneaking works the same at cap 15 as at cap 100 | ruling; ENGINE_FINDINGS §5 |
| Canon ties | nether-star-backed currency (the reserve vault); the scrubbing (the withdrawal signature is the last un-scrubbed record); PROPOSED flavour: the cave's deepest eastern door opens toward generated terrain — a natural "past the edge of the map" mouth for the post-story Ender-Dragon leg (showrunner-optional colour, not a mechanic) | LORE §3, §4 scrubbing; CLAUDE.md post-story |

**Canon guardrails honored:** the protagonist is **never named** the Founder here (the signature is
`§k`-obfuscated exactly like `villain_final_boss.display_name` "§kfounder"; the name is only ever
spoken at `reveal/founder_defeated`). No illness angles. Corporate-dread comedy is *sparse* down
here on purpose — the comedy register (glitching slogans, HR-speak) belongs to the towns; the cave
is where the joke stops and the menace is just menace. Civilians never appear this deep.
**Spoiler ordering:** because the cave is open early, every lore prop that references a beaten
villain or the endgame gates on the matching `defeated_*` tag (§5.3, §8) — an early spelunker
gets dread and treasure, never a spoiler.

---

## 3. Layout & placements

**Zone (real, `install.json` L355-447):** `Deep Dark Cave`, TYPE `BATTLE_FRONTIER`, `centerY 64`,
`hostileOnly true`, **`mobsSpawn false`**, `cylindrical true`, priority 1, color `#FFD700`,
subtitle *"The dark down here is load-bearing."* The polygon (20 vertices) spans roughly
**x ∈ [4267, 5168], z ∈ [2210, 3238]** — a large lobed cavern-mouth footprint in the far east of the
map, well **east of the Battle Frontier hub** (`~x3787-3857, z2925-3006`, `install.json` L2006) and
the Royal League (`3528 166 2773`, CLAUDE.md). The player arrives from the west via the frontier
region (`Road to Royal League`, `Frontier Causeway`, `install.json` L4778/L3635).

**Vertical fact:** `centerY 64` is the *surface mouth*. The set-piece is the **descent** from there
to deepslate/deep-dark depth (`y ≈ -30 to -52`), where sculk, shriekers, and the Warden live. All
sub-surface coords below are **PROPOSED (needs builder confirm)** — I cannot see or build terrain;
these are latch marks the builder relocates to fit the real dig.

| NPC / prop (proposed id) | Role | Coord | Confidence |
|---|---|---|---|
| **`dd_monument`** — the warning monument | The ruling-2 monument-warning trigger at the mouth: fires the area warning (title/sound) and flags the legendary thread (§5.4) — the cave's version of *"warning: the sea seems violent in these parts"* | at the mouth, beside the gatekeeper | **PROPOSED (align with `16_legendaries_nobles.md` noble model)** |
| **`dd_gatekeeper`** — *The Last Auditor* | Entrance NPC: warns, teaches the go-quiet rule, issues the safety kit. **Warns — never gates.** | surface mouth `~[4342, 64, 2706]` (a REAL zone vertex, `install.json` L367-370; west/Royal-League-facing side) | vertex-grounded stand; exact stand PROPOSED |
| **`dd_booth_1`** — *Audit Booth I* (prop + heal) | Checkpoint / SafeZone / heal station, top of descent | `~[4360, 40, 2740]` | **PROPOSED** |
| **`dd_booth_2`** — *Audit Booth II* | Checkpoint / SafeZone / heal, mid-descent | `~[4520, 8, 2760]` | **PROPOSED** |
| **`dd_booth_3`** — *Audit Booth III* | Checkpoint / SafeZone / heal, deep | `~[4680, -24, 2740]` | **PROPOSED** |
| **`dd_ledger_1/2/3`** — withdrawal-slip props | The three reserve-ledger reveal pages (disguised-prop pattern) | one per descent stage, near each booth | **PROPOSED** |
| **`dd_reserve_vault`** — the hollow reserve chamber | Lore + treasure capstone: the drained nether-star vault, the signed slip, the loot cache, and the legendary spawn point (§5.4). The dungeon's terminus — with stairs back up | deepest chamber `~[4720, -44, 2740]` (near polygon centroid, at depth) | **PROPOSED** |
| **Legendary spawn point** — wild static | The optional marquee catch — **GIRATINA (RESOLVED**, ruling 2026-07-06**)**; spawns wild after the descent per the monument archetype, outside every SafeZone hull | off the deep vault `~[4700, -40, 2700]` | **coord PROPOSED; species RESOLVED** |
| **`dd_echo_*`** (optional) — sculk echoes | Optional fan-service re-fights of fallen Company men (§5.3) | in booths only (never Warden zones) | **PROPOSED (optional)** |

All bodies are **NPC/prop latches only — no terrain assumed.** Booths, ledger slips, the monument,
and the vault are disguised props (copy `station_moss`: `role:"civilian"`,
`movement.objective:"ambient_stationary_look"`, `placement` + no `uuid` → spawns once per world).
The **critical unbuilt dependency** is the sculk level itself (sensor spacing, shrieker placement,
lit safe alcoves, the descent path, **and the always-available return route** — §4 valve 6) — that
is a **builder task this doc specifies but cannot place** (§8 gotchas). Every coord above is a
marker for that build, not a claim it exists.

---

## 4. Core structure — "The Last Audit" (NOT a gym: a survivable OPTIONAL stealth descent)

The core loop is a **three-stage sculk descent**, each stage = a Warden-threat traversal followed
by a **safe Audit Booth** (a `SafeZone` — heals, suspends Nuzlocke, no vibrations). Battles, if any,
happen **only in booths**, never under Warden threat. **There is no entry gate and no lock-in**
(showrunner ruling 2026-07-06): the mouth is open, progress persists across visits via the
`dd_stage_N` latches, and leaving — by foot or Recall Beacon — is free at every step.

### The descent ladder

| Stage | Traversal (Warden-threat) | Ends at | Player collects | Gate to proceed |
|---|---|---|---|---|
| 0 | — (surface) | `dd_monument` warning + `dd_gatekeeper` at the mouth | safety kit (§4 valves) + tag `dd_entered` | **none — open** (warning only) |
| 1 | The Approach — sparse sensors, teaches "sneak, don't sprint, don't place near sensors" | `dd_booth_1` (SafeZone) | `dd_ledger_1` — *the reserve ledger, page one* | reach booth → tag `dd_stage_1` |
| 2 | The Throat — dense sensors + first shriekers; the marquee chase | `dd_booth_2` (SafeZone) | `dd_ledger_2` — *the withdrawal record* | reach booth → tag `dd_stage_2` |
| 3 | The Foundation — a live shrieker field; the Warden *will* manifest if you are loud | `dd_booth_3` → `dd_reserve_vault` | `dd_ledger_3` — *the signed slip (`§k` signature)* | reach vault → tag `dd_reached_vault` |
| Vault | SafeZone — the hollow reserve: lore capstone, treasure cache, Auditor's restock (§7), and the wild legendary (§5.4) | the surface, whenever you like | payout + loot + the catch | `dd_reached_vault`; exit stairs always open |

**Leaving and returning:** every visit resumes from the latches. Walk out at stage 1, come back
three gyms later, and booths 1's SafeZone is still yours — no reset, no penalty, no re-lock. The
dungeon never holds state hostage.

### THE HARDCORE-SAFETY DESIGN (the load-bearing part of this whole area)

The Warden **one-shots** (sonic boom ≈ 10 hearts, bypasses most armour; melee ≈ 15). Under
hardcore, one hit = world over; under Nuzlocke, the whole run's grief is at stake. Optional
content does not get to be unfair content — a run-ender in a *bonus* dungeon is the worst-feel
death possible on a live show. So the rule is: **the Warden is a pure ENVIRONMENTAL threat —
never a battle, never a required kill, never a DPS check, and never between the player and the
exit.** Fairness comes from six stacked valves:

1. **The player controls the threat.** Vanilla deep-dark rules are the mechanic: the Warden only
   emerges after enough shrieks, and it **burrows away/despawns after ~60 s of silence**. Sneaking,
   not sprinting, and not placing blocks near sensors means it **never appears.** The gatekeeper
   teaches this out loud (§5.2). The tension is opt-in; the death is opt-in.
2. **`SafeZone` Audit Booths.** Each booth is a `/safezone` region (mod system) — full heal, mob
   spawns suppressed, **Nuzlocke suspended** (per the towns/shrines rule, CLAUDE.md Safe Zones). The
   booths are the checkpoint spine: you never re-cross a Warden stretch you already cleared. ⚠ **A
   SafeZone does NOT stop the Warden** (it is shrieker-summoned, not a natural spawn — see §8); the
   booth's safety is the *builder's* sealed geometry (no sensors, lit, walled), which this doc
   specifies but cannot place.
3. **Totems of Undying (the hard valve).** `dd_gatekeeper` issues **Totems** with the kit. A totem
   eats one lethal Warden hit and pops the player to safety — turning a run-ender into a scare. This
   is the vanilla-legal hardcore mercy; the design **assumes the player descends holding a totem.**
4. **The Recall Beacon (the panic button).** A given item (or the gatekeeper's dialog) runs
   `function .../deep_dark/recall` → teleports the player to the last booth reached
   (`dd_stage_N`), or the mouth if none. Directly models the **`/shrine-abort`** pattern
   (`ShrineChallengeManager.stopChallenge`, `ShrineChallengeManager.java:237` — "No penalty…
   restart whenever") and the `dev goto` teleport (`CobblemonInitiativeCommands.java:782`).
   No penalty, no lost progress. This is what makes "leave whenever" *mechanically* true, not
   just narratively true.
5. **Sculk Muffle Wool.** The kit includes a stack of **wool** (vanilla: dampens vibrations —
   place/stand on it to mute sensors). A tangible, teachable tool the audience watches the streamer
   deploy. (`mobGriefing:false` in `install.json` L97 does not block player placement.)
6. **Free exit is a BUILD REQUIREMENT (the ruling, made geometry).** No one-way drops, no
   lock-behind doors, no flooded or sealed chokepoints: every descent stretch must have a walkable
   return route, and the Recall Beacon must function from anywhere in-zone. The vault has open
   stairs back up. If a proposed layout ever needs a drop the player can't climb back out of, the
   builder adds a ladder/water column beside it. A struggling run walks out the way it came in —
   always.

**Nuzlocke note:** the Warden threatens the **player** (hardcore death), not Pokémon (Nuzlocke
faints). The two systems don't compound *if* battles stay in SafeZone booths (Nuzlocke suspended
there). Keep every down-here battle inside a booth or the vault — a faint mid-Warden-zone must be
impossible by construction.

---

## 5. Quests & side quests

### 5.1 "The Last Audit" — the descent itself (optional marquee, the core loop)
- **Giver:** `dd_gatekeeper` — *The Last Auditor* (a burned-out ex-Company reserve auditor who came
  down here to count what was left and never went back up; **civilian/quest_giver, NOT a battler**).
- **Hook:** entirely player-initiated — no mainline thread points here. The monument warns; the
  Auditor stands at the mouth and makes the offer plain: the boardroom books were never in a
  boardroom. "Nobody sends you down there. Nobody ever went down there on orders. You go because
  you want to know what the money was standing on. Most people decide they don't."
- **Steps:** (1) take the safety kit + the go-quiet lesson (§5.2); (2) descend stages 1-3, reaching
  each booth (`dd_stage_1/2/3`), reading `dd_ledger_1/2/3` — the three-page reveal that **the
  nether-star reserve was mined hollow years ago and the first withdrawal slip is `§k`-signed**;
  (3) reach `dd_reserve_vault` (`dd_reached_vault`) → the hollow-reserve lore capstone, the treasure
  payout, the Auditor's restock, and the wild legendary (§5.4).
- **Gate:** **none** (ruling 2026-07-06). Latches only: each ledger gated on the prior `dd_stage_N`.
- **Rewards:** the **optional wordless memory beat** (the signature, no name — LORE §5), the marquee
  legendary (§5.4), the largest one-time CD payout in the run (§7), and the treasure cache. **No
  combat with the Auditor. No handoff to anything** — the quest ends where it started: at the mouth,
  in daylight, with the player knowing something nobody else knows.
- **Resolution:** the Auditor stays below to keep counting. The player climbs out whenever they
  like — mid-quest, post-vault, anytime. Nothing upstairs is waiting on this.

### 5.2 "Go Quiet" — the safety-kit onboarding (teaches the Warden rule; part of 5.1)
- **Giver:** `dd_gatekeeper`, first interaction.
- **Hook / steps:** pure dialog + a `give`. The Auditor explains the deep dark in plain, dread-flat
  terms ("It hears you. Not your team — *you*. Do not run. Do not build. When it wakes, go dark and
  wait, and it forgets you, same as they forgot me."), then hands the kit and sets `dd_entered`. If
  the party looks underleveled for the loot band, the Auditor says so — **a warning line, never a
  block** (the ruling: the cave is open; the kit is what makes an open door fair).
- **Kit (via `loot give` — copy the gift pattern):** **Totem(s) of Undying**, a stack of **wool**
  (Sculk Muffle), a **Recall Beacon** item (runs `deep_dark/recall`), and consumable **Ender
  Pearls** (traversal). Optionally an **Echo Shard / Recovery Compass** as flavor.
- **Gate:** none. One-time (`dd_entered` latch; the vault's Auditor's restock re-issues totems so a
  re-descent is never under-equipped).
- **Reward:** survival tools + the rule. This is the *fairness contract* made a dialog.

### 5.3 "The Cave Remembers" — sculk echoes (OPTIONAL fan-service gauntlet)
- **Givers / hook:** in the **booths only** (SafeZone — safe to fight), the sculk plays back
  *recordings* of the men The Company buried down here: an **Echo of COO Noir**, an **Echo of Acting
  CEO DJ** — re-tuned copies of villains the player already beat, reconstituted as level-100 shades.
  Each drops one line foreshadowing the mirror waiting in the penthouse ("You climbed all this way
  down to fight a recording. The one that never stops playing is still up there, in the light.").
- **Steps:** interact with an echo prop in a booth → optional battle (winner-first `onwin`); each
  win sets `dd_echo_<name>` for a small reward. Purely optional colour.
- **Gate:** `dd_stage_N` (must have reached that booth) **AND the matching `defeated_<villain>` tag**
  — because the cave is open early, each echo only manifests for a player who has actually beaten
  the original (no spoiler shades for an early spelunker). Skippable entirely.
- **Rewards:** rare candies / a CD tip; the DJ echo can re-drop a nostalgia line without spoiling the
  reveal. **Never on a Warden stretch; never a Nuzlocke risk outside a booth.**
- **Resolution:** the echoes go silent as you pass — the Company's men, filed at last. (Ship-or-defer
  is an open question, §9; the descent works fully without them.)

### 5.4 "What The Dark Kept" — the optional legendary (monument archetype; coordinate `legendaries_nobles`)

**PROPOSED — follows the showrunner's monument-warning noble model (ruling 2, 2026-07-06) and the
`16_legendaries_nobles.md` archetypes; `legendaries_nobles` owns the slot and the final call.**

- **Shape (the ruling-2 monument pattern):** *warning monument → beat the challenge → the legendary
  spawns as a normal wild you catch/battle.* Here that maps to:
  1. **The warning** — `dd_monument` at the mouth fires the monument warning on approach
     (title + sound, the *"the sea seems violent in these parts"* register): *"WARNING: something
     under here is still listening."*
  2. **The challenge** — the descent **itself** is the challenge: this area's "PvP part" is
     environmental (the Warden traversal, stages 1-3), which is truer to the zone's identity than a
     trainer fight. (Open variant, §9: require one booth echo-fight (§5.3) as a PvP-ish leg —
     `legendaries_nobles` + showrunner call.)
  3. **The spawn** — on `dd_reached_vault`, the legendary **spawns wild**
     (`spawnpokemon <properties> [pos]` — **UNVERIFIED (jar-verify)**, status per
     `16_legendaries_nobles.md` §4.0) and the player battles/catches it like any wild: the
     noble archetype (16 §4.1).
- **Stakes (ruling 2026-07-06 + 16 §4.1 — supersedes an earlier draft's safe-zone idea):** nobles
  spawn as **normal wilds at FULL Nuzlocke stakes** — no safe-zone spectacle, no guaranteed-catch or
  gift fallback. The spawn point therefore sits **outside the vault booth's SafeZone hull**, in the
  live corridor off the vault (`~[4700, -40, 2700]` is already off-vault); the vault SafeZone stays
  a rest point, never the arena. **Build-time VERIFY:** Nuzlocke must actually be live at the spawn
  point — the install zone is `mobsSpawn:false`, but Nuzlocke suspension is governed by the mod's
  `/safezone` regions, not install.json; confirm no SafeZone region covers the spawn.
- **Lore:** the Warden did not dig the vault out — it **moved in after** The Company left, and it
  guards the one thing they could not carry up: a legendary the darkness kept.
- **Species (RESOLVED — ruling 2026-07-06):** **GIRATINA** — *"Giratina will be in the deep
  dark."* Distortion, the shadow side of creation; it quietly foreshadows the shadow-self
  mirror waiting at the HQ penthouse without touching it.
- **Gate:** `dd_reached_vault`; one-time.
- **Reward:** the legendary + `dd_legendary_caught`. Coordinate the canon slot with
  `legendaries_nobles` so it isn't double-placed.

---

## 6. Trainers & teams needed

**The core descent needs ZERO new battle trainers** — it is environmental (the Warden) + lore. Battles
are all optional (§5.3) or the wild legendary catch (§5.4). This is deliberate: the area's identity is
the *absence* of a battle ladder.

| Need | File(s) | Format / level | Status |
|---|---|---|---|
| Optional echo re-fights (§5.3) | reuse existing `rctmod/trainers/` teams (Noir = `villain_admin_commander`, DJ = `villain_boss`) **re-tuned to L100**, registered under a NEW config `trainers/deep_dark/deep_dark.json` with **distinct ids** (`dd_echo_noir`, `dd_echo_dj` — NOT `cave_*`) and `prerequisites` including the matching `defeated_*` tags | GEN_9_SINGLES, L100 | **PROPOSED, optional** |
| Wild legendary (§5.4) | scripted wild spawn — `function/deep_dark/spawn_legendary.mcfunction` using `spawnpokemon` (**UNVERIFIED (jar-verify)** per `16_legendaries_nobles.md` §4.0); coordinate `legendaries_nobles` | L100-band wild static | **PROPOSED; coordinate** |

*(The Founder mirror is deliberately absent from this table: `villain_final_boss` lives at the HQ
skyscraper penthouse and belongs to `board_and_founder` — see `14_board_and_founder.md`. Nothing in
this zone references it.)*

**⚠ ID-collision warning (critical):** `battle_frontier.json` already owns `cave_challenger_1`,
`cave_challenger_2`, `frontier_brain_cave` ("Cave Warden Selene"), group `deep_dark_cave`, location
"Deep Dark Cave" (L1119-1306) — those are the **Frontier hub facility**, unrelated to this zone.
**Every id, group, and file this area creates MUST use a `dd_` / `deepdark_` prefix** to avoid
clobbering the frontier registry. Do not name anything `cave_challenger`, `frontier_brain_cave`, or
group it `deep_dark_cave`.

---

## 7. Economy & rewards

Act 3 is economically **flat at `cd_instability = 25`** (LORE §8; `mainline_spine` §7 — the index
only moves again on `company_overthrown`). Nothing in the cave destabilises or stabilises the index;
the reserve-hollow reveal is **lore, not a lever** (it explains *why* the currency was fragile all
along — it does not change the number). As optional content, everything below is bonus-tier: the
run's economy balances **without** any of it.

| Source | Payout / effect | Wiring |
|---|---|---|
| "The Last Audit" completion (`dd_reached_vault`) | one-time **`payout {amount:12000}`** — the largest single payout in the run: you audited the reserve nobody else would. Optional-content treasure — a capstone, not a farm, and nothing downstream assumes the player has it | `economy/payout` (skew-aware; ENGINE_FINDINGS §3 — never `pay_macro`) |
| Vault treasure cache (`dd_reached_vault`) | **PROPOSED (builder):** physical loot chests in the vault — echo shards, netherite-tier salvage, rare candies. The "dungeon treasure" the mouth promises | builder placement + vanilla loot tables |
| Auditor's restock (`dd_reached_vault`) | **full party heal + fresh Totem(s)** at the vault — keeps re-descents and the walk out equipped; a hardcore mercy, not currency | `function/deep_dark/vault_restock` (heal + `loot give` totems) |
| Legendary (§5.4) | the catch itself; `dd_legendary_caught` | wild spawn (16 §4.0) |
| Echo wins (§5.3, optional) | small CD tip + rare candies | `economy/payout`, onwin |
| CD **sink** | **none down here** (no shop — the Auditor sells nothing). The 12000 payout is spendable back in the Frontier/Royal shops, not here | — |
| Shop tier | **untouched** — the cave fires **no** `shop <tier>` reward (Act 3 shop is `post_hq`-derived; no new tier). Do not add one | LORE §9 (stepped tiers end at `post_hq`) |
| Training pack | **NONE** — the descent's completion payout is a story capstone, and per ENGINE_FINDINGS §3 endgame/one-shot story beats are not training-farm sources. No `training_*` loot here | ENGINE_FINDINGS §3 |

---

## 8. Implementation notes / FUTURE-ME HOOKS

**Files to CREATE** (author under `dialog-src/`, then run the pipeline):
- `dialog-src/characters/deep_dark/` (new group folder, matching `hua_zhan/` / `cyber/` convention):
  - `dd_gatekeeper.json` — *The Last Auditor* (`role:"quest_giver"`, `recognition_tier:"late"`,
    `act:"3"`, **no `trainer` block**, `placement` + no `uuid`). Inline `STANDARD` dialog OR a tree
    in `dialog-src/dialog/dd_gatekeeper.json`. Copy the leak/defector inline-recognition shape from
    `cyber_access_admin` (see `08_cyber_city.md` §8) / `villain_admin.json`.
  - `dd_monument.json`, `dd_booth_1/2/3.json`, `dd_ledger_1/2/3.json`, `dd_reserve_vault.json` —
    disguised props (copy `station_moss`: `role:"civilian"`,
    `movement.objective:"ambient_stationary_look"`, `placement`, no `uuid` → spawns once per world
    via the placement-latch generator, ENGINE_FINDINGS §3 "PLACEMENT LATCHES").
  - (optional) `dd_echo_noir.json`, `dd_echo_dj.json` — echo battlers (`battle.trainer` = the new
    `dd_echo_*` config ids; `battle.type` a villain-lite type; `format:"GEN_9_SINGLES"`).
- `dialog-src/dialog/dd_gatekeeper.json` (+ any non-inline echo trees).
- **Trainer registry (only if echoes ship):** `trainers/deep_dark/deep_dark.json` with `dd_echo_*`
  entries (`prerequisites` on `dd_stage_N` **plus the matching `defeated_*` villain tags** — spoiler
  ordering, §2), plus matching `data/rctmod/trainers/dd_echo_*.json` team files (re-tune
  `villain_admin_commander`/`villain_boss` to L100). **`dd_`-prefixed ids only** (§6).
- **Safety-valve functions** (`data/cobblemon_initiative/function/deep_dark/`):
  - `enter.mcfunction` — issue the kit (`loot give @s loot .../npc_gift/dd_safety_kit`: totems, wool,
    recall beacon, pearls), set `dd_entered`. Fire from the gatekeeper "Go Quiet" button. **No gate
    check** — anyone who asks gets the kit (the ruling: open door, fair door).
  - `recall.mcfunction` — teleport the player to the last-reached booth (branch on `dd_stage_N` tags;
    fall back to the mouth if none; model on `ShrineChallengeManager.stopChallenge`, `.java:237`, and
    the `dev goto` teleport, `CobblemonInitiativeCommands.java:782`). Bound to the Recall Beacon
    item / a gatekeeper button. Must work from anywhere in-zone (§4 valve 6).
  - `vault_restock.mcfunction` — full heal (`cobblemon heal @s`) + re-issue totems, fire on
    `dd_reached_vault`.
  - `monument_warning.mcfunction` — the ruling-2 warning beat (title + `playsound`) on first
    approach to `dd_monument`; sets `dd_warned`. **PROPOSED — align copy with the
    `16_legendaries_nobles.md` noble dressing (§4.3).**
  - `spawn_legendary.mcfunction` — wild static via `spawnpokemon` (**UNVERIFIED (jar-verify)** per
    16 §4.0), fired once on `dd_reached_vault` (coordinate `legendaries_nobles`).
- **Loot tables:** `data/cobblemon_initiative/loot_table/npc_gift/dd_safety_kit.json` (copy
  `npc_gift/transition_order` / `hq_keycard` shape from `08_cyber_city.md` §8).
- **SafeZone regions:** register each booth + the vault rest area via the `/safezone` system
  (suspends Nuzlocke + suppresses spawns — CLAUDE.md, and the SafeZone data pattern). Booths must be
  SafeZones so any optional echo battle down here is Nuzlocke-safe. **The legendary spawn point must
  sit OUTSIDE every SafeZone hull** — the catch is full-stakes per the 2026-07-06 ruling (§5.4).
- **Band tags:** the descent itself gates on **nothing** — latches only (`dd_entered`,
  `dd_stage_1/2/3`, `dd_reached_vault`, `dd_legendary_caught`). Echo prereqs ride the existing
  `defeated_*` tags. Any `not_tag` rides an inverse `no_<X>` band tag (ENGINE_FINDINGS §3 — Easy
  NPC 6.25 ignores NOT_EQUALS).
- **HUD:** **the mainline HUD never points here** (the Founder line, `quest/render.mcfunction`
  branch 3b, points at the HQ penthouse — `mainline_spine` §4 / `14_board_and_founder.md`). At most,
  add an optional `q.side_descent` slot (copy a `q.side_*` block) that appears while `dd_entered`
  and not `dd_reached_vault` — a side-quest tracker for a dive in progress, never a mainline
  directive. Coordinate with `mainline_spine` so it doesn't fight branch 3b.

**Pipeline (run in order after authoring):**
`scripts/content_compile` → `scripts/generate_granary_tiers` → `scripts/update_preset_index` →
`scripts/generate_npc_function`. Confirm `npc/preset_map.json` + `update_npc_presets.mcfunction`
regen and the no-`uuid` bodies spawn once (proximity latch). After first spawn, if any NPC needs
NPC-Sight, do the manual `npcsight add <uuid>` pass (ENGINE_FINDINGS §3 KNOWN GAP — latch uuids are random).

**GOTCHAS (verified):**
- **`mobsSpawn:false` + `SafeZone` do NOT stop the Warden.** The Warden is **shrieker-summoned**, not
  a natural mob spawn, and `doWardenSpawning:true` (`install.json` L93). So the mod's spawn
  suppression is irrelevant to Warden safety — **totems, recall, muffle wool, and the builder's
  sealed booth geometry are the only real valves.** Do not assume the zone flags protect the player.
- **The Warden must never gate a required battle** — and in this area *nothing* is required at all.
  Every battle in-zone stays inside a SafeZone booth or the vault.
- **FREE EXIT is a build requirement, not a suggestion** (ruling 2026-07-06): no one-way drops, no
  lock-behind doors, recall functional from anywhere in-zone, vault stairs back up. Audit the
  finished build against §4 valve 6 before shipping — a single unclimbable drop silently breaks the
  ruling.
- **ID collision** with `battle_frontier.json`'s `deep_dark_cave` facility — `dd_`-prefix everything (§6).
- **Macro text has no escaping** — ledger lines, gatekeeper macro text, monument warning, echo
  `on_win`, the `§k` signature: **no `"`, avoid `'`/`%`** (ENGINE_FINDINGS §3; LORE §9). The
  signature is `§k` static, never letters (mirror `villain_final_boss.display_name` "§kfounder").
- **`onwin` tokens are winners-first** (`1:` = player won; ENGINE_FINDINGS §2) — only relevant if echoes ship.
- **No new boss bar reusing `cobblemon_initiative:objective`** — `quest/load` deletes it; use a fresh
  bossbar id if you want a "hold your breath" meter (ENGINE_FINDINGS §3).
- **Terrain is a builder task.** The sensor spacing, shrieker density, lit safe alcoves, the return
  routes, and the descent geometry are the difference between fair-tense and unfair-lethal. This doc
  specifies the intent; the builder must confirm and place. No terrain is assumed by any latch here.

**Patterns to copy:** gatekeeper/defector inline dialog → `cyber_access_admin` (`08_cyber_city.md` §8)
/ `villain_admin.json`; disguised props → `station_moss`; gift/loot → `hq_keycard`/`transition_order`;
abort/recall teleport → `ShrineChallengeManager.stopChallenge` (`.java:237`) + `dev goto`
(`CobblemonInitiativeCommands.java:782`); monument warning + wild legendary →
`16_legendaries_nobles.md` §4.0-4.3 (`spawnpokemon` grammar UNVERIFIED — jar-verify; noble dressing).

---

## 9. Dependencies & open questions

### Resolved by showrunner ruling (2026-07-06)
- **The cave is fully optional** — no story gate, no lock-in, free exit at all times ("it's just
  open"). The former hard-gate-vs-abortable debate is closed: neither — it is simply open, and free
  exit is a build requirement (§4 valve 6). No mainline beat routes through the zone.
- **The Founder mirror is NOT here.** It lives at the Company HQ skyscraper penthouse in Cyber City
  (`14_board_and_founder.md` owns it, per the HQ-geometry and mirror rulings). All former
  mirror-antechamber / handoff / mirror-boon framing is removed from this doc; the vault is now the
  dungeon's own terminus (treasure + lore + legendary).

### Depends on (other area keys)
- **`legendaries_nobles`** — **TIGHTEST coupling now.** They own the canon legendary slots and the
  noble/monument encounter model; §5.4's Giratina (species RESOLVED) level band and the
  monument-warning dressing must be their call so nothing is double-placed and the archetype
  language stays uniform (16 §4.0-4.3; slot registered there as **L10**). Stakes are settled: full
  Nuzlocke stakes, no fallback (ruling 2026-07-06).
- **`board_and_founder`** — light coupling only (was tightest; resolved above). Two touchpoints:
  the echoes' penthouse-foreshadow line (§5.3) must not contradict their mirror staging, and the
  ledger signature beat must stay subordinate to their reveal (`reveal/founder_defeated`).
- **`mainline_spine`** — the mainline HUD must **never** point here; at most the optional
  `q.side_descent` side slot (§8), coordinated so it doesn't fight branch 3b. Provides the
  `defeated_*` tags the echo spoiler-gates ride.
- **`battle_frontier`** — NAME/ID collision source (its `deep_dark_cave` facility, "Cave Warden
  Selene") and the physical neighbour to the west; coordinate the `Frontier Causeway` / `Road to
  Royal League` approach and keep registries disjoint (`dd_` prefix).
- **`shrines_audit`** — reuse its proven safety scaffolding: the `dark_gauntlet` blindness/teleport
  tick model, the no-penalty `/shrine-abort` (`ShrineChallengeManager.stopChallenge`), and the
  SafeZone pattern. The cave's recall valve is a straight lift of that abort.
- *(Tuning context only, not a gate:* the loot/legendary band assumes the post-`board_cleared`
  cap-100 window — `royal_league` / `board_and_founder` progression sets when the dive is
  *sensible*, never when it is *allowed*.)*

### Open questions (showrunner / cross-area decisions)
1. ~~Which legendary~~ → **RESOLVED: GIRATINA** (ruling 2026-07-06); registered as L10 in
   `16_legendaries_nobles.md`.
2. **Monument challenge shape:** is the descent alone the "beat the challenge" leg (recommended —
   environmental is this zone's identity), or does the legendary also require one booth echo-fight
   as a PvP-ish leg (ties to Q3)? Align with `legendaries_nobles`' monument model.
3. **Ship the sculk echoes (§5.3) or keep the descent purely environmental?** Fan-service re-fights
   vs. a leaner, purer dread beat. Effort vs. payoff; the descent is complete without them.
4. **Safety-kit contents & counts** — how many totems, how much wool, does the Recall Beacon have a
   cooldown? Needs balance + builder confirm on sensor spacing (the kit and the level are tuned together).
5. **Underleveled-warning copy & tuning window** — the cave is open at any level (ruling); confirm
   the gatekeeper's dialog-only warning is enough signposting, and whether the vault payout/loot
   band stays flat L100 or scales.
6. **Post-game east-exit flavour (PROPOSED):** does the vault's deepest eastern door open toward
   generated terrain as the "past the edge of the map" mouth for the Ender-Dragon leg, or is that
   exit staged elsewhere? Pure colour; showrunner call.
7. **Builder confirm:** the entire sub-surface sculk build (descent path, sensor/shrieker layout, lit
   sealed booths, the vault, the treasure cache, **and the always-available return routes** — §4
   valve 6). Every coord in §3 is PROPOSED pending this.
