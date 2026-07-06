# 09 — Act 2: The Company HQ Raid (the skyscraper)

> Area key: `company_hq` · Act 2 · Hard gate: **gym 7 cleared (badge count ≥ 7)** AND **majority of the wheat-field network liberated: `fields_liberated ≥ 6` of 10** (SHOWRUNNER RULING 2026-07-06 — majority, not all; shipped files still gate ≥ 4 and must be edited, see §8).
> Zone: `install.json` "The Company, Inc." (VILLAIN, `#455A64`), boardroom core `[1590 51 1028]`.
> **GEOMETRY IS CANON (SHOWRUNNER RULING 2026-07-06):** the HQ is a **skyscraper in Cyber City**. You enter at the **first-floor lobby**. **Down** → basement levels styled as the evil HQ (the Act-2 raid path, DJ at the bottom). **Up** → ordinary corporate flavor floors, topped by the **penthouse** — the player's own old penthouse.
> STATUS: cast + TrainerConfig registry already exist (`dialog-src/characters/villain/*`, `trainers/villain_team/villain_team.json`). This section wires them into the basement-descent raid, adds the keycard gate + penthouse scene, and retunes the roster to the gym-7 cap window.

---

## 1. Concept & fantasy

**One-line pitch:** *From the street it is just another Cyber City tower — glass, badge readers, a lobby fern. You clone a stolen keycard, ride the executive elevator DOWN into the basement where the real Company lives — Regional Manager to COO to the usurper in your own chair — stabilise the currency you once backed, then ride all the way UP past the cubicle floors to a penthouse your Lopunny assistants still keep warm for you.*

The HQ raid is the Act-2 dungeon: the first time the villain plot stops being scattered checkpoints and becomes a **building you storm**. The skyscraper split IS the theme made architecture: everything above ground is the scrubbed, re-verified corporate face — fresh paint, brighter rectangles where portraits hung, staff who only know the impostor memo. Everything below ground is what the scrubbing was hiding: older security, complete portraits, and people who remember. **Descending = digging past the scrubbing.** The deeper you go, the worse the moment when someone places your face.

**Marquee stream moments:**
- **The badge reader flags you.** The lobby scanner reads a face that was zeroed out of the ledger years ago — the building itself dislikes you (Shade's authored line already sells this).
- **CURRENCY STABILIZED.** DJ falls at the bottom of the basement, `cd_instability → 25`, a green title card, the shop visibly eases (post-HQ tier), the shiny Entei breaks free from Company control. The plot the audience felt in the wallet all season pays off on screen.
- **The penthouse.** Ride the private lift from the deepest basement to the top of the tower — the single biggest "this was my life" gut-punch of Act 2, played as dread-comedy: your **Lopunny assistants** greet the boss home — slippers, a fussed-over desk, a brighter rectangle on the wall where a portrait used to hang — while a memory fragment surfaces and *almost* names you. Held: the reveal is Act 3's.

---

## 2. Narrative role

| Field | Value |
|---|---|
| Act | **2** (the HQ Raid) — and the tower serves all three acts (see Canon ties) |
| Entry gate | badge count ≥ 7 (`recognition:late` == `memory_fragment ≥ 7`) **AND** `fields_liberated ≥ 6` of 10 (majority — RULING 2026-07-06; shipped gate is ≥ 4, edit list in §8) |
| `cd_instability` on clear | clamps **DOWN to 25** via `economy/hq_stabilize` (already wired to DJ's `on_win` + config reward; idempotent double-fire) |
| Memory fragment | **Penthouse fragment** — a NON-badge fragment (must NOT increment the `memory_fragment` HUD score). Vague, first-person, dread-comedy. Never names the founder. |
| Recognition tier | **late** throughout (badges ≥ 7). Basement grunts = confused/angry ("stand down, nobody said it"), management = alarm/placing the face, COO Noir = *knows exactly who you are*, DJ = contemptuous ("you look smaller than the legend"). Upper-floor staff, by contrast, are pure re-verified normalcy — they know nothing and it is unsettling. |
| Dark Urge | reaches **tier 3** on DJ's defeat (per LORE_BIBLE §8; post-gym-8 whisper band unlocks here). |
| Canon ties | DJ is the **acting** CEO / usurper (`villain_boss`, `defeated_villain_boss`). His defeat *stabilises*, it does not *end* the plot. **Act 3 relocates to this same tower's UPPER floors** — the Board in the boardroom levels and the Founder mirror in/above the penthouse (PROPOSED, owned by section `14_board_and_founder`) — so the skyscraper serves all three acts: **basement = Act-2 raid; flavor floors = anytime; penthouse = memory tease pre-Act-3, mirror at the end.** The penthouse plants that reveal: the chair the Founder claims in Act 3 is the chair DJ was keeping warm in the basement boardroom. |

Tone guardrails: corporate-dread comedy played straight (LORE_BIBLE §6). No sick-Pokémon angles. The Lopunny gag is *décor + loyalty*, never crude. Civilians never appear inside HQ — everyone here is Company staff, so recognition can run hot (upper floors run it cold on purpose: nobody up there was cleared to know).

---

## 3. Layout & placements

### 3a. Zone geometry — VERIFIED, with one discrepancy to confirm

**Grounded facts (point-in-polygon check run against `install.json`, 2026-07-06):**
- The **"The Company, Inc."** VILLAIN zone (`#455A64`, priority 1, `announce:true`, subtitle *"Something stirs in your memory."*) spans roughly **x 1559–1678, z 983–1130**, `centerY 64`.
- The boardroom core **`[1590 51 1028]` (CLAUDE.md + LORE_BIBLE §4) tests INSIDE "The Company, Inc." and OUTSIDE the "Cyber City" TOWN polygon.** The two zones **do not overlap** — they **adjoin along a long shared seam** (shared vertices `1564/1064 → 1587/1073 → 1596/1125 → 1626/1130 → 1642/1125 → 1646/1096 → 1652/1087`). The Company block hangs off the **north edge of Cyber City's eastern downtown lobe**; the town polygon wraps its south flank.
- **⚠ DISCREPANCY — builder/showrunner confirm (do not silently resolve):** the RULING says the skyscraper is *in Cyber City*, but in zone data the tower block sits in its **own VILLAIN zone carved out of (not contained by) the town polygon**. In play this may be exactly right — you cross the plaza, the banner fires (*"The Company, Inc. — Something stirs in your memory."*) and the tower announces itself — or the showrunner may want the polygons redrawn so the lobby/street front reads as town and only the tower interior is VILLAIN. Decide before placement (Open Q1b).
- **y = 51 is consistent with a basement level:** the core sits 13 blocks below the zone's `centerY 64` (street level). The ruling's geometry and the shipped coordinate agree — DJ's boardroom is **underground, at the bottom of the basement**.

### 3b. Building shape (CANON per ruling; interior floors PROPOSED)

Enter at the **first-floor lobby** at street level (~y64), on the Cyber City seam — the leak admin in `cyber_city` points you here. From the lobby:
- **DOWN — the raid.** A keycard-gated **basement elevator** (PROPOSED mechanics: door NPC checks tag `hq_keycard`) descends through five basement levels, styled progressively more evil-HQ the deeper you go: B1 still has carpet, B5 is portraits-never-scrubbed, ledger-vault, war-room. DJ sits at the bottom (**B5 boardroom, `[1590 51 1028]`**).
- **UP — the flavor.** Public lifts serve ordinary corporate floors — open-plan verification pods, break rooms, scrubbing artifacts (brighter rectangles, re-signed ledgers), on-shift **Lopunny assistants** — no battles, walkable anytime the building is open. Topped by the **penthouse**: the player's own old penthouse (memory trigger, Lopunnys, no founder naming pre-Act-3).

> ⚠ **BUILDER DEPENDENCY (critical):** only the zone polygon + core `[1590 51 1028]` are grounded. Every floor/elevator/penthouse coordinate below is **PROPOSED (needs builder confirm)** — and the *skyscraper interior itself* (lobby + basement stack + upper floors + penthouse) must exist in the builder world (showrunner rule: NPC placement only, no terrain). If the HQ is currently a single shell, the basement-stack + tower geometry is a builder task that blocks placement. Props are name-tagged item frames / signs / plaque-NPCs only (no blocks placed by us).

| # | Floor (proposed y) | NPC / prop | id | Coord (all PROPOSED — builder confirm) | Role |
|---|---|---|---|---|---|
| L1 | Street lobby (y64) | Keycard elevator guard | `hq_lobby_guard` *(new)* | `[1590 64 1035]` | gates the **basement elevator** on tag `hq_keycard`; flavor scanner beat; no battle |
| L1 | Street lobby | Scrubbing artifacts (prop) | — | lobby wall | brighter rectangles where portraits hung; re-verified ledger page |
| B1 | Regional Office (y61) | Contractor (grunt) | `villain_grunt_3` | `[1585 61 1024]` | floor guard, `dialog:grunt_recognition` |
| B1 | Regional Office | Contractor (grunt) | `villain_grunt_4` | `[1594 61 1024]` | floor guard |
| B1 | Regional Office | **Regional Manager Shade** | `villain_admin` | `[1590 61 1018]` | floor boss, **GEN_9_DOUBLES** |
| B2 | Operations Center (y58) | Operative (grunt) | `villain_grunt_5` | `[1585 58 1024]` | floor guard, `dialog:grunt_recognition` |
| B2 | Operations Center | Compliance Officer (grunt) | `villain_grunt_6` | `[1594 58 1024]` | floor guard |
| B2 | Operations Center | **Senior Director Vex** | `villain_admin_2` | `[1590 58 1018]` | floor boss, **GEN_9_DOUBLES** |
| B3 | Senior Vault (y55) | Market Analyst (grunt) | `villain_grunt_7` | `[1585 55 1024]` | floor guard, `dialog:grunt_market_analyst` |
| B3 | Senior Vault | Senior Agent (grunt) | `villain_grunt_8` | `[1594 55 1024]` | floor guard, `dialog:grunt_elite_agent` |
| B3 | Senior Vault | Senior Agent (grunt) | `villain_grunt_9` | `[1590 55 1030]` | floor guard |
| B4 | Executive Sublevel (y53) | Elite Agent (grunt) | `villain_grunt_10` | `[1585 53 1024]` | last-guard, `dialog:grunt_elite_agent` *(see Q6)* |
| B4 | Executive Sublevel | Elite Agent (grunt) | `villain_grunt_11` | `[1594 53 1024]` | last-guard *(see Q6)* |
| B4 | Executive Sublevel | **COO Noir** ("the last door") | `villain_admin_commander` | `[1590 53 1018]` | floor boss, **GEN_9_SINGLES** |
| B5 | Boardroom / Core (y51) | **Acting CEO DJ** | `acting_ceo_dj` (trainer `villain_boss`) | **`[1590 51 1028]`** (core — grounded) | act-2 boss, GEN_9_SINGLES, **bottom of the basement** |
| U | Flavor floors (y ~70–90) | Scrubbing artifacts + desk dressing (props) | — | per floor | re-signed ledgers, brighter rectangles, an org chart with a sanded-off top box |
| U | Flavor floors | Lopunny floor assistant (optional) | `companion_lopunny_desk` *(new, optional)* | mid floor, PROPOSED | on-shift décor/loyalty gag; cry + flavor, no battle |
| P | Penthouse (y ~95, top) | Memory-trigger volume | `hq/penthouse_watch` fn | bbox around `[1590 95 1028]` | fires the penthouse fragment |
| P | Penthouse | Lopunny assistant | `companion_lopunny_left` *(new)* | `[1588 95 1030]` | décor/loyalty gag, cry+flavor, no battle |
| P | Penthouse | Lopunny assistant | `companion_lopunny_right` *(new)* | `[1592 95 1030]` | décor/loyalty gag |
| P | Penthouse | Your old nameplate (prop) | — | desk | `§k`-obfuscated nameplate (matches Founder canon); scrubbing rectangle behind it |

All battle bodies are **builder-placed** (add `uuid` after export) or **placement-latched** (add `placement:{x,y,z}`, no uuid → generated proximity spawn, per ENGINE_FINDINGS placement-latch rule). Lopunnys + lobby guard are latch-spawnable. Sight-registered pursue grunts need a manual `npcsight add <uuid>` pass after first spawn (known gap).

---

## 4. Core structure — the basement ladder

Not a gym, but built like one: a **descending PvP ladder** through the basement levels, where each floor boss is gated on that floor's grunts, and each deeper level is gated on the previous boss. The "which battle is a DOUBLE" role is filled by the two management doubles (Shade, Vex). The upper floors and penthouse have **no battles** — the fight is all below street level.

**Access model (per ruling):** the lobby and upper flavor floors read as an ordinary corporate building — walkable once the tower opens. The **basement elevator is the gated door**: `hq_keycard` (from the `cyber_city` leak). DJ's boardroom door at B5 carries the fields gate on top.

**Ladder + gate wiring** (defeat tags are the character `defeat_tag` fields; recognition:late is auto-true here):

| Level | Battle | Format | Gate to START this battle (dialog `gate`) | Sets on win |
|---|---|---|---|---|
| L1 | Basement elevator | (no battle) | `tag: hq_keycard` | opens descent |
| B1 | `villain_grunt_3` / `_4` | SINGLES | keycard (physical floor) | `defeated_villain_grunt_3/4` |
| B1 | **Shade** `villain_admin` | **DOUBLES** | `all_tags:[defeated_villain_grunt_3, defeated_villain_grunt_4]` | `defeated_villain_admin` |
| B2 | `villain_grunt_5` / `_6` | SINGLES | `tag: defeated_villain_admin` | `defeated_villain_grunt_5/6` |
| B2 | **Vex** `villain_admin_2` | **DOUBLES** | `all_tags:[defeated_villain_grunt_5, defeated_villain_grunt_6]` | `defeated_villain_admin_2` |
| B3 | `villain_grunt_7/8/9` | SINGLES | `tag: defeated_villain_admin_2` | `defeated_villain_grunt_7/8/9` |
| B4 | `villain_grunt_10/11` | SINGLES | `all_tags:[defeated_villain_grunt_8, defeated_villain_grunt_9]` | `defeated_villain_grunt_10/11` |
| B4 | **COO Noir** `villain_admin_commander` | SINGLES | `all_tags:[defeated_villain_grunt_10, defeated_villain_grunt_11]` | `defeated_villain_admin_commander` |
| B5 | **Acting CEO DJ** `villain_boss` | SINGLES | `defeated: villain_admin_commander` **AND** `fields_liberated ≥ 6` | `defeated_villain_boss` → `hq_stabilize` |

**Hard gate (DJ) — shipped at ≥ 4, RULING raises it to ≥ 6 of 10:** `acting_ceo_dj.json` `default` entry currently gates `fields_liberated {op:gte, value:4}` and the `monopoly_holds` fallback (priority 10) refuses the meeting otherwise ("come back when the fields stop answering our memos"). **EDIT the value 4 → 6** (majority of the ten-field network — see `wheat_war_farms`). ALSO add the `defeated: villain_admin_commander` condition to the `default` entry so DJ cannot be skipped past Noir.

**Gate rewiring needed** (the management dialogs currently challenge unconditionally at priority 10): add the floor-grunt `all_tags` gate to each management challenge entry, plus a "clear the floor first" fallback entry (priority 5, no battle button). Shade already has `after` (priority 30, `defeated:villain_admin`) + `recognition` (mid) + `default` entries — insert the grunt gate on the `recognition`/`default` battle entries. Copy the gym-ladder gate idiom from `trainers/gyms/hua_zhan_city.json` prerequisites (the PvP-ladder pattern) — here expressed as dialog `defeated_*` gates rather than RCT prereqs (tbcs bypasses RCT gating; ENGINE_FINDINGS §2 TBCS).

### DJ team sketch (retuned — ace = boss premium over the gym-7 window)

Entry cap after gym 7 is **62**. Gym leaders' aces sit at cap+2; a raid boss earns a small premium. Recommended DJ ace **~65 (cap+3)**, roster **60–65**. **Current `villain_team.json` DJ team is 68–72 (ace Tyranitar 72, +10 over cap) — that overshoots the brutal-but-fair line and must be retuned DOWN** (Q2).

| # | Species | Lvl (proposed) | Note |
|---|---|---|---|
| 1 | Persian | 60 | Technician lead, pivot |
| 2 | Golem | 61 | Sturdy + Stealth Rock / Explosion |
| 3 | Nidoking | 62 | Sheer Force + Life Orb, coverage |
| 4 | Rhyperior | 63 | Solid Rock wall, Assault Vest |
| 5 | Tyranitar | 64 | Sand + Dragon Dance, Choice Scarf |
| 6 | **Entei (shiny)** | **65** | Ace / signature. Also the `spawnOnDefeat` gift (shiny Entei lv55) — the "breaks free from Company control" beat. |

Keep the "Company controls a legendary" motif — Entei is the marquee. `spawnOnDefeat` (shiny Entei, message already authored) makes the raid worth the wall.

---

## 5. Quests & side quests

### 5a. THE KEYCARD LEAK (entry quest) — coordinate with `cyber_city`
- **Giver:** a disillusioned Company insider in Cyber City. `cyber_city` §5.1 owns the body/placement (their `cyber_access_admin` at the tower's street doorstep); **this section owns the keycard grant + what the card opens: the basement elevator.**
- **Hook:** after gym 7, `frag_7` lands ("You signed this charter. Your hand. Your seal."). The defector recognizes the face she personally scrubbed from the lobby wall, stands down, and clones an executive keycard — the first Company person who will not raise a hand against you.
- **Gate:** `defeated_cyber_leader` / `recognition:late` (badges ≥ 7). **The fields gate lives on DJ's boardroom door (B5), not on the card** — so the stream can scout basement levels B1–B4 between field runs while DJ's `monopoly_holds` refusal holds the bottom door (matches the authored `acting_ceo_dj.json` pattern). *(Alternative — card also fields-gated — still open: Q4b.)*
- **Steps:** talk → she confirms the tower and hands over the card → **grants tag `hq_keycard`** via `function cobblemon_initiative:hq/grant_keycard` + actionbar receipt. Optional **paid express** fork (CD sink, see §7) and a "not yet" close.
- **Reward:** `hq_keycard` (opens the basement elevator at the lobby guard). No training pack (entry key, not a battle completion — per the training-pack rule, one-time COMPLETION payouts only).
- **Resolution:** she walks off shift for good (she has burned her badge). One-way latch `hq_keycard` (world-persistent tag).

### 5b. THE LADDER (the raid itself)
Covered in §4 — the level-by-level descent IS the core quest. Each floor boss defeat is a story beat (Shade escorts you down; Vex tells you to "tell DJ the dead came back to read his numbers"; Noir holds the last door). All "after defeat" lines already authored.

### 5c. THE PENTHOUSE (post-DJ scene) — the fun beat
- **Trigger:** on `defeated_villain_boss`, DJ's executive override unlocks the **private penthouse lift** — ride it from the deepest basement to the top of the tower (recommended: penthouse sealed until DJ falls, so the memory beat lands as the Act-2 closer; timing confirm Q5). Entering the penthouse volume fires the scene ONCE.
- **Steps:** enter → `hq/penthouse_watch` (tick poller) detects a player with `defeated_villain_boss` and without `saw_penthouse` inside the bbox → runs `function cobblemon_initiative:memory/hq_penthouse` (title + sound + set `saw_penthouse`) → the two Lopunny assistants open their flavor dialog.
- **Lopunny gag (make it land, keep it tasteful):** they are your former executive assistants, still on-shift, still loyal — they greet the boss home without alarm (the ONLY warm welcome in the building), fuss about your calendar, mention the chair has been "kept exactly how you left it." Corporate-assistant comedy against the dread of realising *this was your home.* Cry + flavor dialog, auto-Goodbye, no battle (`cobblemon_model:"lopunny"` companions).
- **Memory fragment (canon-safe):** first-person, vague, dread-comedy; **never names the founder** (Act-3 hold — the mirror comes back to this room, see `14_board_and_founder`). Example register (quote/apostrophe-free per macro rule): *"The chair remembers the shape of you. The desk knows your hand. Nobody scrubbed this floor because nobody was ever allowed up here but you."*
- **Reward/resolution:** `saw_penthouse` tag (re-read-safe, no re-fire). A cosmetic keepsake is optional (PROPOSED). The penthouse + flavor floors stay open as a between-acts hub; **when Act 3 begins, the Board takes the tower's upper floors and the Founder mirror plays out at the top** (PROPOSED, owned by `14_board_and_founder` — cross-reference, do not wire from here).

---

## 6. Trainers & teams needed

**TrainerConfig registry** (`trainers/villain_team/villain_team.json`) — **already exists** for every HQ id (grunts 3–11, `villain_admin`, `villain_admin_2`, `villain_admin_commander`, `villain_boss`). Retune levels (below) and confirm `name` == displayName (BATTLE_VICTORY match).

**rctapi team files** (`data/rctmod/trainers/<id>.json`) — **the file tbcs actually loads for the battle team** (ENGINE_FINDINGS §2). Only `villain_grunt_2` + a few field trainers exist today; the HQ interior roster's rctmod team files are **MISSING and must be created** (11 files), retuned to the cap-62 window:

| id | Format | Level | Retuned band (ace) | Current stale band |
|---|---|---|---|---|
| `villain_grunt_3` / `_4` | SINGLES | B1 | 58–59 | 22 / 28 |
| `villain_admin` (Shade) | DOUBLES (4 mons) | B1 | 59–60 | 35–36 |
| `villain_grunt_5` / `_6` | SINGLES | B2 | 59–60 | 34–35 / 39–40 |
| `villain_admin_2` (Vex) | DOUBLES (4 mons) | B2 | 60–61 | 47–49 |
| `villain_grunt_7/8/9` | SINGLES | B3 | 60–61 | 44–45 / 49–50 / 54–55 |
| `villain_grunt_10/11` | SINGLES | B4 | 61–62 | 60–61 / 65–66 |
| `villain_admin_commander` (Noir) | SINGLES | B4 | 62–63 (ace) | 62–65 |
| `villain_boss` (DJ) | SINGLES | B5 | 60–65 (ace 65) | 68–72 |

**Graph nodes** (`data/rctmod/mobs/trainers/single/<id>.json`, `requiredDefeats`): re-align so the HQ ladder is reachable **after gym 7** and is **cycle-free** (ENGINE_FINDINGS §2 StackOverflow crash: any requiredDefeats cycle across singles+groups crashes world start). The current `villain_team.json` prereq chain routes DJ through `villain_grunt_10` (gated `ryujin_leader` = gym 8) and `_11` (`nifl_leader` = gym 9) — that pushes DJ past gym 7 (Q6/Q7). Note: these RCT prereqs only affect the series graph + level-cap achievement chain; the **playable** gate is the dialog `defeated_*` ladder in §4 (tbcs bypasses RCT gating), so re-gating is bookkeeping + reachability, not battle-locking.

**Dialog trees** to reuse (exist): `dialog:grunt_recognition`, `dialog:grunt_market_analyst`, `dialog:grunt_elite_agent` (all carry late/mid/early recognition entries). Management + DJ dialogs are inline in their character files.

**New NPCs to author:** `hq_lobby_guard` (basement-elevator door), `companion_lopunny_left/right` (penthouse, cobblemon_model décor), optional `companion_lopunny_desk` (upper flavor floor), keycard leaker (owned by `cyber_city` as `cyber_access_admin`).

---

## 7. Economy & rewards

| Beat | Payout / effect | Mechanism |
|---|---|---|
| Basement grunts (3–11) | flat CD in `onwin`, upper grunt band (**~500–900** rising by depth) | character `battle.prize` (flat onwin, per ENGINE_FINDINGS economy rule — battle prizes stay flat, not `economy/payout`) |
| Shade / Vex / Noir | **3000 / 4500 / 6000** CD (already authored) + item rewards | character `battle.prize` + config `rewards` |
| **Acting CEO DJ** | **8000** CD + `cobblemon:master_ball` + 64 wheat + **`cobblemon-initiative shop post_hq`** (shop eases) + **`economy/hq_stabilize`** (idx→25) + shiny Entei gift | already in `villain_team.json` config `rewards` + `acting_ceo_dj.json` `on_win` |
| Keycard | **CD SINK** (paid express fork, PROPOSED ~2000 CD) *or* free-if-you-earned-it (Q4) | `cobbledollars remove @s <n>` (no balance check — soft-fails; use the affordability probe if it must be a hard sink, ENGINE_FINDINGS CobbleDollars) |
| Currency stabilised | `cd_instability → 25`; shop tier `post_hq` overwrites `default_shop.json` + `cobbledollars reload` → live GUI eases (~+12.5% vs +28% peak) | `hq_stabilize.mcfunction` + `shop post_hq` (both wired) |
| Liberation tie | the **6-of-10-field majority gate** (RULING 2026-07-06) is the direct link to `wheat_war_farms`; relief shop tiers already step per 2 liberated fields | `fields_liberated` band tags (needs new `gte_6` pair, §8) |

Training-pack rule: DJ's one-time completion may carry a `training_grand`/`major` loot table (`loot give @s loot cobblemon_initiative:npc_gift/training_*` before the payout) — grand is reserved; recommend **`training_major`** for DJ. Basement grunts/management get NONE beyond CD prizes (no farm loops in hardcore Nuzlocke). Confirm (Q9).

---

## 8. Implementation notes / FUTURE-ME HOOKS

**Pipeline (run in order — ENGINE_FINDINGS §3):** edit `dialog-src/**` → `scripts/content_compile` (auto-runs sight/band_tags/preset merge + the final steps) → `scripts/generate_granary_tiers` → `scripts/update_preset_index` → `scripts/generate_npc_function` → `gradle build`.

**RULING-DRIVEN EDITS (gate 4 → 6, geometry):**
- `dialog-src/characters/villain/acting_ceo_dj.json` — `default` entry gate `fields_liberated {op:gte, value:4}` → **`value:6`**; ALSO add `defeated: villain_admin_commander` (belt-and-braces on top of the fields gate). The `monopoly_holds` refusal copy still works verbatim at 6.
- `function/dialog/band_tags.mcfunction` — currently maintains `fields_liberated_gte_1/2/4` only (verified). **ADD the `fields_liberated_gte_6` add/remove pair** (copy the gte_4 idiom) if any dialog gates on the band tag.
- `function/quest/render.mcfunction` L34–38 — the main-quest HUD lines gate on `fields_liberated matches 4..` and print "▶ Raid Company HQ [1590 51 1028]" (verified). **EDIT `4..` → `6..`** and reconcile the printed coordinate with the final tower-entrance decision (the lobby is at street level ~y64; `[1590 51 1028]` is the basement boardroom — a lobby-door coordinate reads better on the HUD once the builder confirms it).
- Coordinate ownership of the field count with **`wheat_war_farms`** (`03`) — the network is TEN fields (`install.json` FARM zones `farm_1`–`farm_10`, verified holders exist) and the majority gate is **6**.

**Files to CREATE:**
- `dialog-src/characters/villain/hq_lobby_guard.json` — basement-elevator door NPC in the L1 lobby, `placement` (no uuid), dialog gate `{tag: hq_keycard}` → open message / lift; ungated fallback = "Executive access only. That badge does not scan." Copy the desk-anchor pattern (no wander) from a Hua Zhan station NPC.
- `dialog-src/characters/villain/companion_lopunny_left.json` + `_right.json` (+ optional `companion_lopunny_desk.json` for an upper flavor floor) — `cobblemon_model:"lopunny"` + cry/flavor dialog + `placement`; copy the townsfolk-companion recipe (ENGINE_FINDINGS wiring recipes: "New townsfolk Pokémon / companion"). NO wander (scene anchors).
- `src/main/resources/data/cobblemon_initiative/function/hq/grant_keycard.mcfunction` — `tag @s add hq_keycard` + actionbar (quote/apostrophe-free). *(Leaker body itself is `cyber_city`'s `cyber_access_admin`.)*
- `src/main/resources/data/cobblemon_initiative/function/hq/penthouse_watch.mcfunction` — tick-wired proximity poll (copy `function/wheat_trader/tick.mcfunction` idiom): `execute as @a[tag=defeated_villain_boss,tag=!saw_penthouse, x=..,y=..,z=..,dx=..] at @s run function …/memory/hq_penthouse`. Register in the tick load (mirror `wheat_trader/load`).
- `src/main/resources/data/cobblemon_initiative/function/memory/hq_penthouse.mcfunction` — **bespoke title fragment; do NOT call `memory/grant_fragment`** (that sets the `memory_fragment` score → corrupts the badge-count HUD / next-gym mapping). Set `saw_penthouse`, play the `#7A5CA8` title + `sculk_sensor.clicking`/`soul_escape` sounds like `grant_fragment`, tellraw echo. Quote/apostrophe-free.
- **11 rctapi team files** `data/rctmod/trainers/{villain_admin,villain_admin_2,villain_admin_commander,villain_boss,villain_grunt_3..11}.json` — copy the `villain_grunt_2.json` rctmod shape (`identity`, `team[]`, `ai`, `bag`), retuned to the §6 bands. `name` MUST equal the TrainerConfig `displayName`.
- **Graph nodes** `data/rctmod/mobs/trainers/single/<id>.json` — cycle-safe `requiredDefeats` (re-run the cycle check over singles+groups after editing).

**Files to EDIT:**
- `dialog-src/characters/villain/villain_admin.json`, `_2.json`, `_commander.json` — add floor-grunt `all_tags` gates to the challenge entries + a "clear the floor" priority-5 fallback (§4).
- `trainers/villain_team/villain_team.json` — retune the config team levels to match the rctmod files; re-align `prerequisites` (Q6/Q7).
- `dialog-src/characters/villain/villain_grunt_3..11.json` — set `placement` (or `uuid` after builder export) + confirm the dialog ref / sight mode (pursue grunts need a manual `npcsight add` pass, known gap).

**Gotchas (ENGINE_FINDINGS):** onwin `@1`=player / `@2`=NPC, winners-first; management `lose_line` mirrors must not use `remove @1` / `@2 say`. Every compiled NPC needs a movement objective — scene/door/Lopunny anchors get `ambient_stationary_look` (never wander on quest anchors or sight NPCs). `NOT_EQUALS` never negates (use `not_tag` → `no_*` band tags). Macro text (fragment, keycard actionbar, stabilize) = NO double-quotes, avoid apostrophes. Latch-spawned NPCs get random UUIDs → arm sight/attach manually after first spawn.

---

## 9. Dependencies & open questions

**Depends on (area keys):**
- **`wheat_war_farms`** — the **6-of-10 majority gate** (RULING 2026-07-06) is DJ's entry condition; without `fields_liberated` reaching 6, DJ is unreachable. (Also the relief-shop tie. The ten FARM zones `farm_1`–`farm_10` are verified in `install.json`.)
- **`cyber_city`** — the keycard leak: `cyber_city` places `cyber_access_admin`'s body at the tower's street doorstep and points the player at the lobby; this section owns the grant/gate + the basement-elevator door. Gym-7 clear is the entry badge gate.
- **`mainline_spine`** — `cd_instability` model, memory-fragment HUD (which the penthouse fragment must NOT touch), Dark Urge tier-3 unlock, recognition band-tag maintenance, `quest/render.mcfunction` gate edit (4→6).
- **`gym_system_pvp_doubles`** — the basement ladder copies the gym interior-trainer ladder + the GEN_9_DOUBLES tag-team pattern (Shade/Vex are the doubles).
- **`board_and_founder`** — downstream: **Act 3 relocates to this tower's upper floors** (Board in the boardroom levels, Founder mirror at the top — PROPOSED, owned by `14`); the penthouse scene here plants that reveal.

**RESOLVED by showrunner ruling (2026-07-06) — removed from the open list:**
- ~~Building orientation~~ → **first-floor lobby entry; basement descent = the raid (DJ at the bottom); upstairs = flavor floors + penthouse.** (Was Q5.)
- ~~Fields gate count~~ → **majority: 6 of 10** (was 4; ruling 3).
- ~~Scout-before-complete~~ (was Q8) → largely resolved by the geometry: the building above ground is an ordinary walkable corporate tower; the **basement elevator** is the keycard door; DJ's B5 door holds the fields gate. Residual scope question folded into Q4b.

**Showrunner decisions (DECIDE before build):**
1. **Builder geometry:** does the skyscraper exist with a street lobby + a five-level basement stack + upper flavor floors + penthouse, or is the HQ a shell? All interior coords are PROPOSED and blocked on this. (Highest-priority.)
   **1b. Zone discrepancy:** `[1590 51 1028]` verifies inside the **"The Company, Inc." VILLAIN zone and outside the Cyber City TOWN polygon** (zones adjoin along a shared seam, no overlap — §3a). Ruling says the tower is "in Cyber City." Confirm: keep the adjoining VILLAIN carve-out (tower announces itself on approach) or redraw polygons so the block sits inside the town. Builder/showrunner call — do not silently pick one.
2. **Roster retune:** confirm retuning Shade/Vex/Noir/HQ-grunts UP and DJ DOWN into the **cap-62 window** (§6). As-shipped levels (Shade 35, Vex 47, DJ 72) make the raid either trivial or a +10 wall.
3. **DJ ace level:** ace 65 (cap+3 boss premium) as proposed, or higher for brutality? Keep shiny Entei as the ace + `spawnOnDefeat` gift?
4. **Keycard delivery:** free (a defector's gift) vs a CD sink (paid express fork, ~2000)?
   **4b. Keycard scope:** card gated on gym-7 only (recommended — basement scoutable, DJ's door holds the ≥6 fields gate) or also fields-gated (building opens as one unlockable beat)?
5. **Penthouse timing:** sealed until DJ falls (recommended — the memory beat closes Act 2) or open with the rest of the upper floors? Are the non-penthouse flavor floors open from the first visit?
6. **grunt_10 / grunt_11 (Elite Agents):** use them as B4 Executive-Sublevel guards (this design) or reserve them as Act-3 Board guards on the upper floors (their `location:"The Boardroom"` + `14`'s relocation makes this MORE plausible now)? If HQ-basement, re-gate off their gym-8/9 RCT prereqs so they're reachable after gym 7.
7. **RCT prereq chain / cycle:** re-align `villain_boss`'s series prereqs so DJ sits at the gym-7 tier without a graph cycle (crash risk).
8. **Lopunny tone/count:** two penthouse assistants (+ optional one on a flavor floor), comedic-but-tasteful, no battle — confirm the "assistant" framing lands as loyalty/décor, not crude.
9. **DJ training pack:** `training_major` at DJ's one-time completion, or none?
