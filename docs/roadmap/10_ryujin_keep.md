# 10 — Ryujin Keep (Gym 8, Dragon, cap → 68)

> Area key: `ryujin_keep`. Authoring pass: design-only (no game files touched). All
> coordinates are grounded in `install.json` / existing character+config files, or
> labelled **PROPOSED (needs builder confirm)**. Levels follow the **CLAUDE.md
> authoritative ladder**, not the stale LORE_BIBLE table.

---

## 1. Concept & fantasy

**One-line pitch:** A sky-keep where power is measured by *bearing*, not paperwork —
the one door the Company never got through — and its dragon-keeper looks at the
amnesiac and all but calls him a king.

Ryujin Keep is a feudal dragon fortress on a spire: the town sits at ground level
(`centerY 64`) and the gym is a vertical climb up a tower whose battle floor is at
**y ≈ 201** (the interior-trainer coords in the shipped config are all y201 — this is a
*sky keep*, and the ascent itself is the set-piece). It is the first gym **after the HQ
raid**: the currency just stabilised (idx→25, "CURRENCY STABILIZED" already fired), the
Company is bloodied, and here — in the one place their memos never reached — the mask
of "there was never a founder" starts to crack in the player's favour. Old-timers see
the founder's bearing; a defector hides in the keep; a Heritage Acquisitions envoy is
still, absurdly, trying to *buy the keep's founding charter as a brand asset*, and the
dragons find that funny.

**Marquee stream moments**
- **The climb.** Ascending the tower ladder-of-battles to a leader who fights you
  underlevelled and still hits like a landslide (four outrage-clickers, brutal Nuzlocke).
- **The double.** Apprentice Tatsu's paired **GEN_9_DOUBLES** "Sky Wardens" tag-team —
  the ledge fight before the throne.
- **frag_8 — "You built it."** The heaviest memory fragment yet fires on the badge:
  *"You did not just sign it. You built it."* The keeper's dialogue frames it without
  ever saying the word.
- **The storm on the spire.** Win the badge and a storm gathers over the mountain to the
  west — the **Dragon Shrine wakes** and the sky-dragon (Rayquaza, already on the shrine
  boss team) is teased as answering "only a true sovereign."
- **The defector.** A terrified ex-HQ records officer recognises the founder's face,
  stands down, and hands over a *scrubbing artifact* — the cover-up leaking in real time.

---

## 2. Narrative role

| Field | Value | Source |
|---|---|---|
| Act | **Late Act 2 (post-HQ tail)** | brief; LORE_BIBLE §8 places Gym 8 after the HQ→DJ beat |
| Gate to enter tone | `defeated_villain_boss` (Acting CEO DJ down) | ENGINE_FINDINGS §3 story-flag canon (HQ = `defeated_villain_boss`) |
| `cd_instability` | **holds 25** (DJ's defeat clamped it down; `hq_stabilize` clamps downward only) | LORE_BIBLE §8; ENGINE_FINDINGS §3 |
| Memory fragment | **frag_8 — "You did not just sign it. You built it."** | `function/memory/gym/frag_8.mcfunction` (already authored + wired) |
| Recognition tier | **late** — some stand down; the keeper all but names it, never closes it | `characters/gym/ryujin_leader.json` `recognition_tier:"late"` |
| Dark Urge | **tier 3** (unlocked at HQ) | LORE_BIBLE §7 ("tier 3 only post-gym-8"/post-HQ) |
| Economy voice | **post-HQ "corrupted propaganda"** register: `§k`-glitched slogans, leaks ("we told them the founder retired") | LORE_BIBLE §3 late register |
| Continuity guard | **DO NOT name the protagonist as the Founder.** frag_8 circles it. The name only breaks at `reveal/founder_defeated` post-Board. | LORE_BIBLE §5, §9; `function/reveal/*` |

Canon ties: the keep is the **anti-Company** — a sovereign institution the erasure
never penetrated, so recognition runs hottest here (rank × proximity gradient,
LORE_BIBLE §4). The Company's move is *heritage acquisition* — buying the keep's founding
document to keep scrubbing history — which is the same weaponised-trust logic as the
currency, now aimed at memory itself.

---

## 3. Layout & placements

**Confirmed zones (`data/cobblemon_initiative/install.json`):**

| Zone | Type | Key geometry | Colour |
|---|---|---|---|
| **Ryujin Keep** (subtitle "Gym 8 — Dragon Type") | TOWN, `centerY 64`, `cylindrical`, `mobsSpawn:false` | polygon ~ **x 2072–2423, z 811–1150** | `#7C4DFF` |
| **Dragon Shrine** (subtitle "The air crackles with ancient power.") | SHRINE, `centerY 64` | polygon ~ **x 1943–2092, z 872–959** (west of the keep) | `#7C4DFF` |

The keep interior battle floor is **y 201** (all shipped interior coords are y201 — a
tower). The town ground hub (guide, shops, side-quest NPCs) sits at **y ≈ 64** at the
tower base; those ground coords are not in any file yet → **PROPOSED**.

**Interior gym ladder — coords CONFIRMED in `trainers/gyms/ryujin_keep.json`:**

| Trainer id | Display | Coord (y201) | Status |
|---|---|---|---|
| `ryujin_trainer_1` | Dragon Tamer Ryu | `[2144, 201, 881]` | builder-confirmed |
| `ryujin_trainer_2` | Ace Trainer Drake | `[2147, 201, 887]` | builder-confirmed |
| `ryujin_trainer_3` | Lorekeeper Shen | `[2141, 201, 884]` | legacy (see §4/§9) |
| `ryujin_trainer_4` | Dragoneer Riko | `[2148, 201, 890]` | legacy |
| `ryujin_jr_apprentice` | Jr. Apprentice Scales | `[2149, 201, 884]` | legacy |
| `ryujin_apprentice` | Apprentice Tatsu (**DOUBLE**) | `[2150, 201, 881]` | builder-confirmed |
| `ryujin_leader` | Leader Ryujin | `[2156, 201, 884]` | builder-confirmed |

**Characters/props to place (NPC-only, no terrain):**

| NPC / prop | Role | Coord | Status |
|---|---|---|---|
| `ryujin_leader` (exists, `characters/gym/`) | Gym leader body | `[2156, 201, 884]` | **GAP: file has NO `placement` and NO `uuid`** → needs builder-body uuid (see §8) |
| `ryujin_guide` (exists, `characters/gym/`) | Gym guide (`dialog:gym_guide`) | tower base, PROPOSED `~[2150, 64, 895]` | **GAP: no `placement`/`uuid`** |
| `ryujin_trainer_1/2` + `ryujin_apprentice` | Interior duelists | y201 coords above | **GAP: no `dialog-src` character files yet** (only teams+config exist) |
| Warden Kaida (prop partner for the double) | Decorative co-belligerent standing beside Tatsu | PROPOSED `~[2151, 201, 882]` | PROPOSED |
| Keepwarden Orin (SQ1 giver) | Keep steward at the charter lectern | PROPOSED ground `~[2146, 64, 900]` | PROPOSED |
| Heritage Acquisitions envoy (SQ1 villain) | Company envoy trying to buy the charter | PROPOSED ground `~[2143, 64, 902]` | PROPOSED |
| "Sovereign's Charter" lectern (prop NPC) | Scrubbing-artifact focus for SQ1/SQ3 (pattern: `rezoning_notice_board`) | PROPOSED `~[2145, 64, 901]` | PROPOSED |
| Skywatcher Rei (SQ2 giver) | Points to the storm/Dragon Shrine | PROPOSED overlook `~[2120, 66, 900]` (keep's west parapet, toward shrine) | PROPOSED |
| Defector "Records Officer" (SQ3 giver) | Ex-HQ hider, stands down on recognition | PROPOSED `~[2160, 64, 890]` (a side alcove) | PROPOSED |

> All PROPOSED coords sit inside the confirmed Ryujin Keep polygon and are showrunner
> marks; the builder should nudge them onto real ledges/rooms. The ground-hub cluster
> assumes the tower base is roughly under the y201 column (x2141–2156, z881–890).

---

## 4. Gym / core structure

**PvP ladder (each fight gated on the previous being defeated).** The *canonical*
retuned teams live in `data/rctmod/trainers/` (source of truth per the pipeline &
memory "retune teams, never the ladder"); the shipped set is a **lean 4-fight ladder**.
Mob-spawn gating is in `data/rctmod/mobs/trainers/`.

| Step | Trainer | Format | Team (canonical `rctmod/trainers/`) | Gate (defeated_*) |
|---|---|---|---|---|
| 1 | Dragon Tamer **Ryu** (`ryujin_trainer_1`) | SINGLES | Dratini 56 / Axew 57 | `defeated_cyber_leader` |
| 1 | Ace Trainer **Drake** (`ryujin_trainer_2`) | SINGLES | Bagon 57 / Gible 57 | `defeated_cyber_leader` |
| 2 | Apprentice **Tatsu** (`ryujin_apprentice`) — **THE DOUBLE** | **GEN_9_DOUBLES** | *retune target:* Dragonair 60 / Fraxure 60 / Shelgon 59 / +1 (see below) | `defeated_ryujin_trainer_1` **AND** `defeated_ryujin_trainer_2` |
| 3 | Leader **Ryujin** (`ryujin_leader`) | SINGLES | Dragonite 62 / Haxorus 63 / **Salamence 64 (ACE)** | `defeated_ryujin_apprentice` |

Gate wiring is already correct in the mob files:
`mobs/trainers/groups/ryujin_trainer.json` → `requiredDefeats:[[cyber_leader]]`;
`mobs/trainers/single/ryujin_apprentice.json` → `[[cyber_leader],[ryujin_trainer_1],[ryujin_trainer_2]]`;
`mobs/trainers/single/ryujin_leader.json` → `[[ryujin_apprentice]]`. TBCS victory sets
the `defeated_<id>` tags the dialog gates read.

**Which battle is the DOUBLE:** Apprentice **Tatsu** (`ryujin_apprentice`). The original
`trainers/gyms/ryujin_keep.json` already flagged this tier `GEN_9_DOUBLES`, but the
retuned `rctmod/trainers/ryujin_apprentice.json` is currently `GEN_9_SINGLES` — **this is
the concrete retune to make** (bump to 4 mons, GEN_9_DOUBLES, e.g. Dragonair 60 /
Fraxure 60 / Flygon 61 / Shelgon 59). Author it single-NPC-carries-doubles like Takehara's
`agent_yield_lead` (one Easy NPC entity runs the doubles trainer; **Warden Kaida** is a
decorative partner body beside it). Ace of the double = 61 (under leader's 64).

**Leader team — ACE = entry-cap + 2 = 64** (entry cap 62 = Gym 7 Cyber unlock, per
CLAUDE.md authoritative ladder). The shipped canonical is a **lean 3** (Dragonite 62 /
Haxorus 63 / Salamence 64). The brief asks for ~6; below is the full-roster proposal that
**keeps the canonical core and the ace at 64** — recommend trimming to 4–5 to match the
brutal-Nuzlocke convention (Takehara/Hua Zhan/Cyber leaders all run 4):

| # | Species | Level | Note |
|---|---|---|---|
| 1 | Kingdra | 61 | PROPOSED add — rain/steel-coverage lead |
| 2 | Flygon | 61 | PROPOSED add — levitate, ground pivot |
| 3 | Dragonite | 62 | canonical (multiscale, lum berry) |
| 4 | Garchomp | 63 | PROPOSED add (was in legacy config, rough skin) |
| 5 | Haxorus | 63 | canonical (mold breaker) |
| 6 | **Salamence** | **64** | **canonical ACE** (intimidate, sitrus/life orb) |

> The legacy `trainers/gyms/ryujin_keep.json` embedded leader team still tops at
> **Garchomp 62** (4 mons) — **stale**; align it (or retire it in favour of the rctmod
> team) to ace 64. See §9 open questions.

**Leader dialog** (`dialog-src/dialog/gym_leader_ryujin.json`, exists — `after`/`default`
entries already do late recognition: *"I know the bearing of someone who once gave the
orders… you wear it like you forgot you put it on"*). ADD a **post-HQ variant** gated on
`defeated_villain_boss` (priority between `after`=20 and `default`=10, following the
Hua Zhan pattern of stacked gated entries), e.g. *"The lowlands say the acting seat fell.
Someone unseated it. The dragons felt the air change before I heard the name."* — escalates
recognition without closing the reveal.

---

## 5. Quests & side quests

Post-HQ, late-recognition, corporate-dread comedy. All gates are **tag / defeated_**
gates (numeric gates would need band-tag derivation; keep it simple). Payouts route
through `function economy/payout {amount:N}` (skew-aware); no double-quotes / apostrophes
in macro text.

### SQ1 — "Heritage Acquisitions" (villain rebuff, on-theme marquee)
- **Giver:** Keepwarden **Orin** (PROPOSED) at the Sovereign's Charter lectern.
- **Hook:** A Company **Heritage Acquisitions envoy** is at the keep with a padded offer
  to buy the keep's founding charter — "to preserve it" (i.e. keep scrubbing history).
  Orin needs a witness who the keep will actually listen to.
- **Steps:** (1) Talk to Orin → tag `ryujin_heritage_seen`. (2) Read the lectern prop
  (the charter, half its signatures re-verified under new names — a **scrubbing
  artifact**) → tag `ryujin_charter_read`. (3) Confront the envoy. He recognises the
  founder's face mid-pitch (**late tier: alarm, then panic**), and it goes one of two ways:
  - **Battle** (villain-management-tier, single or optional double) → win sets
    `defeated_ryujin_heritage_envoy`; or
  - **Cite the charter back at him** (a talk-past button, gated `not_tag
    defeated_ryujin_heritage_envoy`) → he "withdraws pending clarification" and sets the
    same resolution tag. (Pattern: `sq_beekeeper_tomo` seal-four "cite his own notice.")
- **Gates:** offer chain gated `defeated_villain_boss` (only appears post-HQ) +
  `defeated_ryujin_leader` optional for the fully-recognised variant.
- **Rewards:** `economy/payout {amount:1200}` + `loot give` training loot (major band) +
  a keep-crest keepsake item + tag `ryujin_charter_kept`.
- **Resolution:** the charter stays; Orin remarks the keep "has turned away worse buyers
  than the Company, and older thrones than this one." A **CobbleDollars decline-to-fight**
  toll can be offered on the envoy's optional battle (pay to skip — canon CD use).

### SQ2 — "The Storm on the Spire" (legendary/shrine tease → `shrines_audit`/`legendaries_nobles`)
- **Giver:** Skywatcher **Rei** (PROPOSED) on the keep's west parapet facing the shrine.
- **Hook:** After the badge, a storm sits over the mountain west of the keep. Rei tells
  the old story: the keep and the **Dragon Shrine** were raised together, and the
  sky-dragon (Rayquaza) "answers only a sovereign."
- **Steps:** (1) Win the badge → `defeated_ryujin_leader`. (2) Rei's storm dialog opens →
  tag `ryujin_storm_told`. (3) Walk to the shrine overlook (a placed marker/prop, or Rei's
  "point the way" button sets a JourneyMap waypoint) → tag `ryujin_shrine_pointed` — this
  is the **hand-off flag** into `shrines_audit` (the Dragon Shrine cultists already gate on
  `ryujin_leader`; this quest is the wayfinding + lore, it does NOT own the shrine build).
- **Gates:** `defeated_ryujin_leader`.
- **Rewards:** 3× `cobblemon:rare_candy` + `economy/payout {amount:600}` + tag. The
  Rayquaza payoff itself lives on the shrine boss (`High Priest Draconis`, Rayquaza 75).
- **Resolution:** Rei: *"Climb it if you think the storm knows your name. It knew the last
  one who built here."* (circles frag_8 without naming it).

### SQ3 — "The Defector's Ledger" (recognition + scrubbing artifact, cheap & high-flavour)
- **Giver:** an ex-HQ **Records Officer** hiding in a keep alcove (PROPOSED) — a `civilian`/
  `defector` recipe, terrified and over-polite (comedy).
- **Hook:** He fled HQ after DJ fell and has been sleeping in the keep. He recognises the
  founder's face (**late tier: stands down**), assumes he is about to be "filed," and
  instead hands over a re-verified ledger page as a peace offering.
- **Steps:** (1) Talk → he panics, then confesses → tag `ryujin_defector_met`. (2) Accept
  the ledger page (**scrubbing artifact**: signatures re-verified under new names, a memo
  reading *the founder retired → there was never a founder*) → `ryujin_ledger_taken`.
- **Gates:** `defeated_villain_boss` (only post-HQ). Optional escalated line if
  `defeated_ryujin_leader`.
- **Rewards:** `economy/payout {amount:400}` + a **scrubbing-artifact keepsake item** +
  tag. No battle (he will not raise a hand against the founder — canon "some stand down").
- **Resolution:** feeds the reveal gradient; his last line deflects toward frag_8
  (*"Whoever you were, the paperwork says you are no one now. It has never been wrong about
  anything except this."*).

---

## 6. Trainers & teams needed

**`data/rctmod/trainers/` (team source of truth):**

| File | Action | Format / levels |
|---|---|---|
| `ryujin_trainer_1.json` | exists ✓ (Dratini 56 / Axew 57) | SINGLES |
| `ryujin_trainer_2.json` | exists ✓ (Bagon 57 / Gible 57) | SINGLES |
| `ryujin_apprentice.json` | **RETUNE** → `GEN_9_DOUBLES`, 4 mons (Dragonair 60 / Fraxure 60 / Flygon 61 / Shelgon 59) | DOUBLES |
| `ryujin_leader.json` | exists ✓ (Dragonite 62 / Haxorus 63 / Salamence 64) — **optionally expand to 5–6, keep ace 64** | SINGLES |
| `ryujin_heritage_envoy.json` | **NEW** (SQ1 villain) — management-tier, lv ~62–64 (e.g. Krookodile/Bisharp corporate-menace flavour) | SINGLES (or DOUBLES if paired) |
| *(optional)* `ryujin_trainer_3.json`, `ryujin_trainer_4.json`, `ryujin_jr_apprentice.json` | **NEW only if restoring the full 7-fight ladder** (config + `npc_map_template` still reference them) | see §9 |

**`data/rctmod/mobs/trainers/` gating:** already correct for the lean ladder. If the
full ladder is restored, add `single/ryujin_trainer_3/4` + `single/ryujin_jr_apprentice`
mob files and thread `requiredDefeats` (Jr. Apprentice → after trainer_1/2; Apprentice →
after Jr. Apprentice), matching the legacy config's `prerequisites` chain.

**`data/cobblemon_initiative/trainers/gyms/ryujin_keep.json`:** keep `id`,
`prerequisites`, `battleFormat`, `coordinates`, `rewards`; **update the embedded leader
team's ace 62 → 64** (or drop the embedded team and rely on the rctmod team). Rewards
already fire `memory/gym/frag_8` + `cobblemon-initiative shop badge_8` and
`achievementOnDefeat: badge_dragon` — leave as-is.

**Side-quest trainer registry:** there is only `trainers/side_quests/act1.json` today.
Ryujin is Act 2/3 → **create `trainers/side_quests/act2.json`** (or register the envoy
under `trainers/villain_team/`) for `ryujin_heritage_envoy`. Decision in §9.

**Battle formats vs cap:** entry cap **62**, everything fought **underlevelled**:
ladder 56–61, apprentice-double ace 61, leader ace **64**, Heritage Envoy ~62–64.
(Dragon Shrine post-gym content already sits 64–75 with Rayquaza 75 — owned by
`shrines_audit`.)

---

## 7. Economy & rewards

| Source | Payout | Mechanism |
|---|---|---|
| Leader Ryujin | **4900 CD** prize (flat) | TBCS `onwin` / PlayerProgressManager (`characters/gym/ryujin_leader.json` `prize:4900`) |
| Leader reward cmds | frag_8 + shop tier step | `memory/gym/frag_8`, `cobblemon-initiative shop badge_8` (already wired) |
| Interior duelists | potions / super potions only (NO CD) | config `rewards` — keeps prizes flat, avoids inflation (ENGINE_FINDINGS §3) |
| SQ1 Heritage | **1200 CD** + training loot (major) + keepsave | `economy/payout {amount:1200}` + `loot give … npc_gift/training_*` |
| SQ2 Storm | **600 CD** + 3× rare candy | `economy/payout {amount:600}` |
| SQ3 Defector | **400 CD** + scrubbing keepsake | `economy/payout {amount:400}` |

**Shop / instability:** `cd_instability` **holds 25** post-HQ, so the economy voice is the
**corrupted-propaganda** register (`§k` glitch, leaks). Shop tier steps to **`badge_8`**
on leader defeat; because the HQ gate required **≥4 liberated fields**, the player is
already at a **relief tier** (`ShopTierManager` resolves `<tier>_relief1/2` live from
`fields_liberated`; every 2 fields = −12 idx, floor 0). Prices have eased from the Gym-7
peak (+28%) toward +12.5% post-DJ. **CD sinks:** the keep shop (`badge_8` catalog),
rare-candy/consumable spend before the shrine gauntlet, and the SQ1 envoy
**decline-to-fight toll** (canon CobbleDollars pay-to-walk).

---

## 8. Implementation notes / FUTURE-ME HOOKS

**Author these files (full paths):**

1. **Interior duelist characters** (currently missing — only leader+guide exist):
   `dialog-src/characters/gym/ryujin_trainer_1.json`, `…/ryujin_trainer_2.json`,
   `…/ryujin_apprentice.json` (+ `warden_kaida.json` decorative partner). **Copy pattern:**
   `dialog-src/characters/takehara/apprentice_aiko.json` (battle block, `type:"one_time"`,
   `defeat_tag`, win/lose/already lines) and `…/agent_yield_lead.json` for the
   **single-NPC-carries-DOUBLES** trick (`format:"GEN_9_DOUBLES"`, trainer = the doubles
   trainer id). Give each a `uuid` (builder body) **or** a `placement:{x,y,z}` (latch-spawn)
   — the shipped interior coords (§3) are y201.
2. **Town/side-quest NPCs** in a new dir `dialog-src/characters/ryujin/`: `keepwarden_orin.json`,
   `ryujin_heritage_envoy.json`, `sq_skywatcher_rei.json`, `sq_records_officer.json`, and
   prop `sovereign_charter_lectern.json`. **Copy pattern:** `characters/hua_zhan/hz_stall_linh.json`
   (merchant/`dialog_inline` staged gates), `characters/takehara/sq_beekeeper_tomo.json`
   (multi-stage seal chain w/ `economy/payout` + `loot give` + tag sets + "cite it back"
   talk-past), and `characters/hua_zhan/rezoning_notice_board.json` for the lectern prop.
3. **Dialog trees** (if not inline): `dialog-src/dialog/sq_ryujin_heritage.json`,
   `…/sq_ryujin_storm.json`, `…/sq_ryujin_defector.json`. ADD a post-HQ entry to the
   existing `dialog-src/dialog/gym_leader_ryujin.json` gated `defeated_villain_boss`.
4. **Teams:** retune `data/rctmod/trainers/ryujin_apprentice.json` → GEN_9_DOUBLES; add
   `data/rctmod/trainers/ryujin_heritage_envoy.json`; (optional) restore trainer_3/4/jr.
   Register the envoy in a **new** `data/cobblemon_initiative/trainers/side_quests/act2.json`.
5. **Fix stale leader team:** `trainers/gyms/ryujin_keep.json` embedded ace 62 → 64 (or
   delete embedded team, defer to rctmod).
6. **Placement gap:** `characters/gym/ryujin_leader.json` and `…/ryujin_guide.json` have
   **neither `placement` nor `uuid`** — add builder-body uuids (leader at y201 `[2156,201,884]`;
   guide at the ground hub). Every other placed gym leader (e.g. `takehara_leader.json`)
   carries a `uuid`.

**Pipeline (run in order — brief §Content pipeline):**
`scripts/content_compile` (lowers `dialog-src/**` → `data/easy_npc/preset/humanoid(_slim)/*.npc.snbt`)
→ `scripts/generate_granary_tiers` → `scripts/update_preset_index`
→ `scripts/generate_npc_function` (writes `npc/preset_map.json` + `function/update_npc_presets.mcfunction`).
Then rebuild (`gradle build`).

**Gotchas (ENGINE_FINDINGS):**
- **Gates:** only `PLAYER_TAG EQUALS` is proven. `not_tag X` → auto inverse `no_X`.
  Numeric (`cd_instability`, `fields_liberated`, badges) → **derived band-tags** via
  `function/dialog/band_tags.mcfunction`. Design with `tag`/`defeated_*` gates (this doc does).
- **`onwin` tokens are WINNERS-FIRST** (key 1 = player won, key 2 = loser). Easy NPC
  **ignores NOT_EQUALS**. TBCS win sets `defeated_<trainerId>`.
- **Macro text** (frag_8, `economy/payout`, `onwin`, actionbars) = **no double-quotes, avoid
  apostrophes** (no escaping layer).
- **npcsight is NOT auto-armed** for latch-spawned NPCs (random uuids). If the Heritage
  Envoy is meant to line-of-sight-trigger, a manual `npcsight add <uuid>` pass is needed
  after first spawn. Prefer a dialog-driven battle to avoid this.
- **Do NOT touch the cap ladder.** Retune teams only (memory: "retune teams under
  `data/rctmod/trainers`, never the ladder").
- **Reveal stays closed:** no founder-naming in any Gym-8 text; the name only breaks at
  `function/reveal/founder_defeated` post-Board.

---

## 9. Dependencies & open questions

**Depends on (area keys):**
- **`company_hq`** *(HARD)* — Gym 8 is post-HQ; `defeated_villain_boss` gates the tone,
  SQ1/SQ3, and the corrupted-propaganda economy voice; `cd_instability` = 25 comes from DJ.
- **`cyber_city`** *(HARD)* — `defeated_cyber_leader` gates the whole ladder; entry cap 62.
- **`mainline_spine`** *(HARD)* — frag_8, `memory/*`, band-tag derivation, recognition
  gradient, shop-tier stepping.
- **`shrines_audit`** — owns the Dragon Shrine build/placement; SQ2 only points into it.
- **`legendaries_nobles`** — owns the Rayquaza payoff (already on the shrine boss team,
  lv 75); SQ2 teases it as the sky-dragon "sovereign" hook.
- **`gym_system_pvp_doubles`** — shared ladder/double conventions and gate wiring this gym
  reuses.
- **`board_and_founder`** *(continuity)* — the reveal must stay closed at Gym 8; frag_8
  circles it; `reveal/board_fell`/`founder_defeated` are Act-3.
- **`nifl_town`** *(soft)* — the leader/guide point north to Gym 9.
- **`wheat_war_farms`** *(soft)* — `fields_liberated` drives shop relief tiers and the HQ gate.

**Showrunner decisions:**
1. **ACE LEVEL — resolve the conflict.** The assignment header says "ace 70," but that
   traces to the **stale LORE_BIBLE** ladder (gym-8 entry 68). CLAUDE.md (authoritative) →
   entry cap **62**, so **ace = 64**, which is exactly what the shipped
   `rctmod/trainers/ryujin_leader.json` already runs (Salamence 64). **Recommend: ace 64.**
   Confirm before anyone "fixes" it upward.
2. **Ladder depth.** Ship the **lean 4-fight** ladder (t1, t2, apprentice-double, leader —
   the retuned rctmod set) or **restore the full 7** (t3 Shen, t4 Riko, jr Scales still
   exist in `ryujin_keep.json` + `npc_map_template.json` but have **no retuned rctmod
   teams**)? Restoring = 3 new team files + 3 mob-gate files; retiring = prune config +
   npc_map. **Recommend lean 4** (matches brutal-Nuzlocke pacing of shipped gyms).
3. **Leader roster size.** Lean 3 (shipped) vs the 6-mon proposal in §4. **Recommend 4–5**,
   ace fixed at 64.
4. **The double's shape.** Single-NPC-carries-doubles (Tatsu, `agent_yield_lead` pattern) vs
   a genuine two-body tag-team (Tatsu + Kaida)? And confirm the apprentice-tier retune to
   GEN_9_DOUBLES.
5. **Placement bodies.** Confirm builder bodies/uuids for `ryujin_leader` + `ryujin_guide`
   (currently unplaced) and the real ground-hub coords at the tower base (all §3 ground
   coords are PROPOSED).
6. **Heritage Envoy identity.** New `ryujin_heritage_envoy` character+team, or re-skin an
   under-used existing villain id? And register under new `side_quests/act2.json` vs
   `villain_team/`?
7. **Shrine hand-off.** Does SQ2 just point to the Dragon Shrine (cultists already gate on
   `ryujin_leader`), or should it set an explicit "shrine woke" tag that `shrines_audit`
   consumes to arm the storm/legendary beat?
