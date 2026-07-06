# 11 — Nifl Town (Gym 9, Ice, cap → 74)

> **One-line pitch:** A frozen town where *nothing rots* — so the one place the Company
> could never scrub the founder's face is the coldest gym on the mountain, and the ice has
> been keeping your name on file the whole time.

Area key: `nifl_town`. Zone (`install.json`): **Nifl Town** — TOWN, polygon x∈[3432,3706] z∈[1842,2169],
`centerY:64`, `mobsSpawn:false` (safe zone), color `#80DEEA`, subtitle "Gym 9 — Ice Type".
The gym interior sits *elevated* on the glacier at **y≈112** (existing gym-NPC coords), while
town services and the frozen lake read at **y≈64–70**. Approached from the south-west via
**Frostveil Pass** (Route 14, cylindrical, `hostileOnly`, `mobsSpawn:true`).

---

## 1. Concept & fantasy

**The fun.** Nifl is the bleak, beautiful hinge of Act 3. The gym is a straightforward
snow-veil grind — but the *town* is a preservation vault. Boreas already tells the player the
truth to their face ("the ice keeps things… faces, names… I have seen yours in here long before
you arrived"). The marquee side quest turns that line into a screen: a Company **cold-storage
archive** where the deletion never took, and the player thaws out a "Verified Trust" portrait of
themselves with the face burned away — *frag_9 "They emptied you"* rendered as an artifact.

**Marquee stream moments.**
1. **The thawed portrait** (SQ1 "Cold Storage") — a scrubbed founder photo surfaces from the
   ice; the posture is unmistakable, the face is a hole. Circles the reveal without closing it.
2. **The first on-screen defector** (SQ2 "Stand Down at the Frostgate") — a Company Warrant
   Officer recognizes the player and can be talked into *saluting and walking into the snow*
   rather than fighting. Late-tier recognition made playable.
3. **Boreas' post-battle line** — the Glacier Badge win text lands as a horror beat, not a
   trophy: the cold "kept your name long before Nifl saw your face."
4. **The Lantern Walk** (SQ3) — four frozen memory-lanterns around the lake, each a tiny
   preserved vignette; a light, atmospheric palate-cleanser between the two heavy beats.

**Tone:** corporate-dread comedy played straight against genuine menace. The archive is filed,
stamped, and audited; the menace is that it *worked* and the player is the deleted line item.

---

## 2. Narrative role

| Field | Value | Source |
|-------|-------|--------|
| Act | **3** window (post-HQ raid / post-DJ, pre-Royal-League) | `nifl_leader.json` `act:"3"`; LORE §"Three acts" |
| `cd_instability` | **25** (stabilized after Acting CEO DJ falls) | LORE_BIBLE beat map row 9 |
| Memory fragment | **frag_9 — "They needed you gone, not dead. So they emptied you."** sub: *"A founder with no name cannot reclaim a throne. That was the plan. Was it yours too?"* | `function/memory/gym/frag_9.mcfunction` (already built) |
| Recognition tier | **late** — *some Company personnel stand down*; civilians never recognize; the leader recognizes *eerily* (prophecy-of-the-cold, not Company alarm) | `nifl_leader.json` `recognition_tier:"late"`; LORE §"recognition gradient" |
| Canon ties | frag_9 is the bleakest, most explicit circling of the founder reveal; the archive **seeds** the Board/Founder payoff but must NOT name the player as the Founder (reveal reserved post-Royal-League) | LORE §"DON'T name the protagonist as the Founder before Act 3" / "fragments 7–9 circle it, never close it" |

**Wiring already in place:** defeating `nifl_leader` fires `frag_9` (sets `memory_fragment=9`
and PLAYER_TAG `memory_fragment_9`) plus `cobblemon-initiative shop badge_9`, and grants
achievement `badge_ice` → LevelCapManager unlocks **cap 74**. The quest HUD reads
`memory_fragment` (=9) and `function/quest/gym_town.mcfunction` maps badge-9 → next town
**Scorchspire**. No new mainline plumbing required for the fragment.

---

## 3. Layout & placements

Town polygon and the gym cluster are real (from files); everything else is **PROPOSED** and needs
builder confirm against actual walkable blocks. Note the vertical split: **gym palace y≈112**,
**town/lake floor y≈64–70**.

### Confirmed-from-files (already placed)

| NPC / config | Coord (x,y,z) | Source |
|---|---|---|
| `nifl_trainer_1` Skier Frost | 3596,112,2028 | `trainers/gyms/nifl_town.json` |
| `nifl_trainer_2` Boarder Chill | 3599,112,2034 | ″ |
| `nifl_trainer_3` Snowboarder Blizzard | 3593,112,2031 | ″ |
| `nifl_trainer_4` Cold Researcher Aurora | 3600,112,2037 | ″ |
| `nifl_jr_apprentice` Jr. Apprentice Frost | 3601,112,2031 | ″ |
| `nifl_apprentice` Apprentice Glacier **(DOUBLE)** | 3602,112,2028 | ″ |
| `nifl_leader` Leader Boreas | 3608,112,2031 | ″ |
| Ice Shrine ladder (5 trainers) | 3634–3644, **68**, 1960–1963 | `trainers/shrines/ice_shrine.json` — **NB: sits west/below the install.json Ice Shrine zone (x≥3667, centerY 64) → coordinate audit, see §9** |

### PROPOSED new placements (needs builder confirm)

| NPC | Role | Coord (x,y,z) PROPOSED | Notes |
|---|---|---|---|
| `nifl_guide` (exists, no coord) | gym guide | 3586,112,2025 | Threshold of the ice-palace, west of the trainer cluster; dialog `dialog:gym_guide` (shared) already assigned |
| `nifl_nurse` | paid healer | 3470,66,2000 | Lower-town Center; rides `economy/heal_paid` (flat 100 CD) |
| `nifl_martkeeper` | shop_cobbledollars | 3480,66,2010 | Default CobbleDollars shop; tier `badge_9` active after leader |
| `nifl_cold_auditor` (SQ1 giver) | quest_giver | 3520,68,1982 | At the cold-storage vault mouth; carries the ledger-core walk on its own entity |
| `nifl_records_officer` (SQ1 battle) | villain, single | 3540,68,1975 | Inside the vault; late-tier, may stand down in dialogue |
| `nifl_warrant_officer` (SQ2 giver+battle) | villain veteran | 3450,70,2030 | Frostgate at the SW town/Frostveil boundary; hosts the DOUBLE (partner grunt) OR the stand-down branch |
| `nifl_keeper_vetra` (SQ3 giver) | civilian elder | 3500,65,1960 | Frozen-lake shore; walks the 4-lantern chain on its own entity (zero prop bodies) |
| `nifl_civilian_a/b` (flavor) | civilian | 3462,66,2016 / 3492,67,1994 | Optional cold-town color; ZERO recognition |

No terrain/structure is assumed — the "vault", "Frostgate", and "lanterns" are **flavor framing
of existing map geometry**; interaction chains live on the NPC entities (beekeeper pattern), not
on new blocks. Confirm the vault/lake read exists in the builder world before authoring.

---

## 4. Gym / core structure

The PvP ladder **already exists** in `trainers/gyms/nifl_town.json` with a `prerequisites`
defeat-chain. It only needs: (a) a level **retune** to the confirmed balance, (b) a 6th leader mon,
(c) **dialog-src character bodies** for the 6 interior trainers (only `nifl_leader` + `nifl_guide`
exist today), and (d) reconciliation with the out-of-sync `rctmod/trainers/nifl_leader.json`.

### Ladder & gate wiring (existing prerequisite chain)

```
ryujin_leader (gym 8 gate)
  → nifl_trainer_1  (single)
  → nifl_trainer_2  (single)
  → nifl_trainer_3  (single)
  → nifl_trainer_4  (single)
  → nifl_jr_apprentice   (single;  prereq trainer_1 + trainer_2)
  → nifl_apprentice **(GEN_9_DOUBLES)**  (prereq jr_apprentice)
  → nifl_leader     (single;  prereq apprentice)  → badge_ice, frag_9, shop badge_9, cap 74
```

The **DOUBLE** is `nifl_apprentice` (Apprentice Glacier), `battleFormat:"GEN_9_DOUBLES"` — already
configured. In dialog-src, each interior body's `battle` block sets `defeat_tag:"defeated_<id>"`
and the next body gates its challenge button on the previous `defeated_*` tag (Takehara/Hua Zhan
interior pattern). No new gate tags needed beyond the `defeated_*` set.

### Balance — CONFIRMED convention: **ace = entry-cap + 2 = 70** (NOT 76)

Nifl entry cap = Ryujin's unlock = **68** → ace **70**. Verified against built neighbours:
Ryujin ace **64** (entry 62+2), Scorchspire ace **76** (entry 74+2). Nifl at 70 sits cleanly
between them (64 → **70** → 76, +6 steps). The brief's "ace 76" **collides with Scorchspire** and
is treated as an arithmetic slip — see §9 Open Q1. Current files disagree with each other:

| File | Current ace | Mons | Status |
|---|---|---|---|
| `trainers/gyms/nifl_town.json` (leader) | Aurorus **66** | 5 | STALE — retune ↑ |
| `rctmod/trainers/nifl_leader.json` | Glaceon **70** | **3** | partly-retuned, under-sized |

**Retune targets (interior ladder):**

| Trainer | Species (levels → retuned) |
|---|---|
| trainer_1 | Swinub, Snover → **64/64** |
| trainer_2 | Sealeo, Sneasel → **64/65** |
| trainer_3 | Cubchoo, Cryogonal → **65/65** |
| trainer_4 | Bergmite, Glaceon → **65/66** |
| jr_apprentice | Dewgong, Weavile → **66/67** |
| apprentice **(DOUBLE)** | Mamoswine, Jynx, Cloyster, Abomasnow → **67/67/67/68** |

**Leader Boreas — target team (ace 70, 6 mons; snow / Aurora Veil core):**

| # | Species | Lvl | Set sketch | Item |
|---|---|---|---|---|
| 1 | Weavile | 68 | Icicle Crash / Knock Off / Ice Shard / Low Kick (lead pressure) | Life Orb |
| 2 | Cloyster | 68 | Shell Smash / Icicle Spear / Rock Blast / Hydro Pump | Focus Sash |
| 3 | Mamoswine | 69 | Earthquake / Icicle Crash / Ice Shard / Stealth Rock (Thick Fat) | Assault Vest |
| 4 | Froslass | 69 | Blizzard / Shadow Ball / Spikes / Destiny Bond (Cursed Body) | Focus Sash |
| 5 | Glaceon | 69 | Blizzard / Shadow Ball / Water Pulse (Ice Body) | Choice Specs |
| 6 | **Aurorus (ACE)** | **70** | **Blizzard / Thunder / Aurora Veil / Ancient Power (Snow Warning)** | Leftovers |

Aurora Veil under Snow Warning is the gimmick the underleveled Nuzlocke player must break — the
whole team leans on the screen. Keep `full_restore ×3` in the leader bag (existing).

---

## 5. Quests & side quests

Three quests, deliberately varied in pacing: one heavy (archive), one branching-combat
(Frostgate), one battle-free atmospheric (lanterns). All macro-delivered text avoids
double-quotes and apostrophes (ENGINE RULE).

### SQ1 — **Cold Storage** *(marquee; frag_9 payoff)*
- **Giver:** `nifl_cold_auditor` (a burned-out Company records clerk who stayed at his post after
  the scrub — corporate-dread comedy: he still files everything, he just no longer believes it).
- **Hook:** The vault preserves what head office deleted. Three **ledger cores** are frozen in the
  stacks; the Auditor wants them thawed and read before "Retention" purges the room.
- **Steps (interaction chain on the Auditor entity — beekeeper `sting_seal_*` pattern):**
  1. `nifl_core_1` — the ledger of a liberated field, still showing pre-monopoly wheat prices.
  2. `nifl_core_2` — a memo: *the founder retired* (stage-managed lie #1) stamped over an older
     memo: *there was never a founder* (stage-managed lie #2). Two lies, one folder.
  3. `nifl_core_3` — **the portrait.** "Verified Trust" founder photo, posture unmistakable, face
     a burned hole. Reading it fires an actionbar line that rhymes with frag_9.
- **Gate to the last core:** the **Records Officer** stands between the player and core 3 →
  `defeated_nifl_records_officer` **OR** a talk-past (he can stand down, late tier). Either path
  sets `nifl_archive_open`.
- **Gates:** giver active on `defeated_ryujin_leader` (in-town from arrival); core buttons chain
  `nifl_core_1 → _2 → _3`; portrait requires `nifl_archive_open`.
- **Rewards:** `economy/payout {amount:600}` (≈564 CD at idx25) + `rare_candy ×5` +
  `loot npc_gift/training_standard`; story tag `nifl_archive_read` (a re-reader hook for the Board
  arc). NO Pokémon gift (endgame budget).
- **Resolution:** the Auditor files the portrait under "unresolved" and tells the player the cold
  will still be holding it when they come back with a name. Plants the Founder reveal; never states it.

### SQ2 — **Stand Down at the Frostgate** *(recognition / moral branch)*
- **Giver / opponent:** `nifl_warrant_officer` — a Company veteran freezing at the Frostveil/Nifl
  boundary who *recognizes the player mid-sentence* (veteran alarm → late-tier wobble).
- **Hook:** He has orders to hold the gate. He also has twenty years of loyalty to a face he was
  told to forget. He gives the player the choice out loud.
- **Branch A — Fight:** **GEN_9_DOUBLES**, the Officer + a Frostgate grunt (hosted on his entity,
  agent_yield_lead pattern). Win → `defeated_nifl_warrant_officer`, prize + CD.
- **Branch B — Stand down:** cite his own recognition back at him → he salutes and walks into the
  snow. Sets `nifl_warrant_stood_down`; grants a **Frostgate token** (flavor key) + smaller CD,
  no battle. First on-screen defector.
- **Gates:** active on `defeated_ryujin_leader`; the two branch buttons are mutually exclusive and
  both terminal (either tag closes the encounter).
- **Rewards:** A → 480 CD via payout + `hyper_potion ×3`; B → 260 CD + `ice_stone ×1` +
  the token (unlocks a one-line Frostveil Pass shortcut flavor). Both set `nifl_frostgate_clear`.
- **Resolution:** a rare place the player's *unknown* past buys mercy instead of a fight — the
  recognition arc paying a dividend the player does not understand.

### SQ3 — **The Long Memory (Lantern Walk)** *(battle-free atmosphere)*
- **Giver:** `nifl_keeper_vetra`, a Nifl elder in Boreas' mystic tradition (the cold remembers).
- **Hook:** Four memory-lanterns around the frozen lake have gone dark. Relight them and the ice
  "gives back" the small memories it has been keeping — a wedding, a lost dog, a first snow, and
  one that is not a townsperson's at all.
- **Steps:** interaction chain on Vetra (walks the player lantern to lantern), each sets
  `nifl_lantern_1..4` and plays a short vignette line; lantern 4 whispers a founder-adjacent
  fragment (a name half-heard, then gone) — a *light* echo of frag_9, not a reveal.
- **Gates:** `nifl_lantern_1 → _2 → _3 → _4`; completion `nifl_lanterns_done`.
- **Rewards:** `economy/payout {amount:300}` + `nether_star ×0` (NO — see econ) + a cosmetic
  heirloom (`cobblemon:pretty_feather ×2` flavor) + `ever_stone`-style keepsake; mainly the vignettes.
- **Resolution:** Vetra tells the player the ice is patient, and it is holding one more memory for
  them than for anyone else in town. Streamable, quiet, foreboding.

---

## 6. Trainers & teams needed

### Existing (retune only)
- `data/cobblemon_initiative/trainers/gyms/nifl_town.json` — **retune** all 7 battle entries to §4
  targets; **add 6th leader mon (Cloyster 68)**; leave prerequisites/coords/rewards intact.
- `data/rctmod/trainers/nifl_leader.json` — **reconcile** to the same 6-mon / ace-70 team (currently
  3 mons, ace 70). Same for `nifl_apprentice/_trainer_1/_trainer_2` if RCT spawn is used.
- `data/rctmod/mobs/trainers/single/nifl_leader.json`, `.../single/nifl_apprentice.json`,
  `.../groups/nifl_trainer.json` — spawn/series configs already present; verify identities after retune.

### New rctmod team files to CREATE
| File | Format | Level band | Team sketch (late-tier corporate ice) |
|---|---|---|---|
| `rctmod/trainers/nifl_records_officer.json` (SQ1) | GEN_9_SINGLES | 68–69 | Bronzong, Klinklang, Porygon2, Beartic — "records & machines" |
| `rctmod/trainers/nifl_warrant_officer.json` (SQ2) | **GEN_9_DOUBLES** | 68 | Officer: Weavile + Walrein; grunt partner: Glalie + Sneasel (hosted on one entity) |

### New registry entry
- `data/cobblemon_initiative/trainers/side_quests/act3.json` **(NEW file)** — register
  `nifl_records_officer` and `nifl_warrant_officer` (mirror `side_quests/act1.json` schema:
  id, coordinates, battleFormat, prerequisites `["ryujin_leader"]`, rewards, inline team or RCT ref).
- SQ3 (Lantern Walk) needs **no trainer** — pure interaction chain.

All side-quest battles are one-time (`type:"one_time"` / villain), under the entry cap (68) so they
are winnable but not free.

---

## 7. Economy & rewards

| Source | Payout / effect | Route |
|---|---|---|
| Leader Boreas prize | **5400** (flat literal) | existing `nifl_town.json` `prize` |
| Interior ladder | potions/super/hyper potions (existing item rewards) | `nifl_town.json` rewards |
| SQ1 Cold Storage | ~600 face (≈**564** at idx25) + rare_candy ×5 + training gift | `economy/payout {amount:600}` |
| SQ2-A fight | ~480 face + hyper_potion ×3 | `economy/payout {amount:480}` |
| SQ2-B stand down | ~260 face + ice_stone + token | `economy/payout {amount:260}` |
| SQ3 Lantern Walk | ~300 face + cosmetic keepsake | `economy/payout {amount:300}` |
| **CD sinks** | paid nurse (100 flat, `economy/heal_paid`), paid mart (default shop), decline-battle fees | — |

**Skew note:** at `cd_instability=25`, `economy/payout` rate = 100 − min(25/4,25) = **94%** of face
(quest payouts skew; the flat 5400 battle prize does not). **Shop:** leader fires
`cobblemon-initiative shop badge_9` → tier `badge_9` catalog (exists in `scripts/shop_tiers/master_shop.json`).
Because Nifl is post-HQ (≥4 fields liberated for the raid gate), the **relief** tier may already be
active — confirm which catalog is live so the mart stock reads correctly (see §9). No new liberation
here (all fields freed pre-Royal-League); Nifl only *spends* the stabilized economy.

---

## 8. Implementation notes / FUTURE-ME HOOKS

**Files to CREATE (dialog-src authoring layer):**
```
dialog-src/characters/nifl/nifl_trainer_1.json      + dialog-src/dialog/nifl_trainer_1.json (or inline)
dialog-src/characters/nifl/nifl_trainer_2.json      + …_2
dialog-src/characters/nifl/nifl_trainer_3.json      + …_3
dialog-src/characters/nifl/nifl_trainer_4.json      + …_4
dialog-src/characters/nifl/nifl_jr_apprentice.json  + …
dialog-src/characters/nifl/nifl_apprentice.json     + …  (battle block format GEN_9_DOUBLES)
dialog-src/characters/nifl/nifl_nurse.json          (recipe civilian, service shop? no — healer)
dialog-src/characters/nifl/nifl_martkeeper.json     (service.kind shop_cobbledollars)
dialog-src/characters/nifl/sq_cold_auditor.json     + inline dialog (ledger-core chain)
dialog-src/characters/nifl/sq_warrant_officer.json  + inline dialog (branch A/B)  battle GEN_9_DOUBLES
dialog-src/characters/nifl/sq_keeper_vetra.json     + inline dialog (lantern chain)
dialog-src/characters/nifl/nifl_civilian_a.json / _b.json  (optional flavor)
```
> `nifl_leader` and `nifl_guide` characters already exist — do NOT re-create; the guide only needs
> a placement coord and the leader needs its retuned team reflected in `battle` flavor (win/lose
> lines are already written and good).

**Files to CREATE (resources):**
```
data/rctmod/trainers/nifl_records_officer.json
data/rctmod/trainers/nifl_warrant_officer.json          (GEN_9_DOUBLES)
data/cobblemon_initiative/trainers/side_quests/act3.json (NEW registry — copy act1.json shape)
```

**Files to EDIT (retune, at build time — NOT in this design pass):**
```
data/cobblemon_initiative/trainers/gyms/nifl_town.json   (levels ↑ to §4; +6th leader mon)
data/rctmod/trainers/nifl_leader.json                    (reconcile to 6-mon ace-70)
```

**Pipeline (run in order after authoring):**
```
scripts/content_compile           # lowers dialog-src → data/easy_npc/preset/humanoid(_slim)/<id>.npc.snbt
scripts/generate_granary_tiers    # (no-op for Nifl unless a granary tier touched)
scripts/update_preset_index       # rebuild Easy NPC preset index
scripts/generate_npc_function     # writes npc/preset_map.json + function/update_npc_presets.mcfunction
```

**Patterns to COPY (verbatim structure):**
- Interior trainer body + battle + "sends you deeper" dialog → `dialog-src/characters/takehara/jr_apprentice_sora.json`.
- DOUBLE hosted on one entity (Officer + partner) → `dialog-src/characters/takehara/agent_yield_lead.json`
  (`format:"GEN_9_DOUBLES"`, `despawn_on_win` optional; the second body stands down via its own gate).
- Multi-step interaction chain with resume/`open_dialog` labels and per-step tags (SQ1 cores, SQ3
  lanterns) → `dialog-src/characters/takehara/sq_beekeeper_tomo.json` (the `sting_seal_*` walk).
- Paid nurse / paid mart / civilian recipes and `service.kind` → `dialog-src/characters/hua_zhan/hz_nurse.json`,
  `hz_martkeeper.json`, `hz_stall_mei.json`.
- Placement-latched (no-uuid, spawn-once) vs uuid-adopted bodies → `companion_combee.json` (placement)
  vs `agent_yield_lead.json` (uuid). The 6 gym interior trainers already have coords in the gym config;
  give the *character* bodies matching `placement:{}` (no uuid) OR adopt CSV bodies at those coords.

**Gotchas (VERIFIED):**
- `frag_9` and `shop badge_9` and `badge_ice` are **already wired** on `nifl_leader` — do not
  double-fire them from the new bodies.
- Macro-delivered text (payout receipts, fragment lines, lantern vignettes, onwin) must contain
  **NO double-quotes and avoid apostrophes** (ENGINE RULE).
- TBCS `onwin` tokens are **winners-first** (`1:` = player won). Winning sets `defeated_<trainerId>`.
- Numeric conditions can't gate dialog directly — only PLAYER_TAG. Nifl's design uses plain
  `defeated_*` / story tags only, so **no new band-tag entries** are required.
- Skin override: if adopting CSV bodies, a `skin:{type:custom,uuid:[...]}` block now fully overrides
  the builder skin+variant (use it to dress the ice trainers consistently).
- The two leader team files are **out of sync** — reconcile both or the RCT-spawned Boreas and the
  gym-config Boreas will differ (see §9 Open Q2).

---

## 9. Dependencies & open questions

### Depends on (other area keys)
| Key | Why |
|---|---|
| `ryujin_keep` | Every Nifl trainer gates on `defeated_ryujin_leader`; player enters from Ryujin/Frostveil |
| `scorchspire` | Next gym; frag_9 → `gym_town.mcfunction` routes badge-9 → Scorchspire |
| `mainline_spine` | Act-3 window, frag_9 fragment, `memory_fragment` score, `cd_instability=25`, recognition gradient, HUD render |
| `company_hq` | Recognition tier "late" and idx=25 are *consequences* of DJ's fall; Company is on the back foot here |
| `gym_system_pvp_doubles` | Shared interior-ladder gating + the `nifl_apprentice` GEN_9_DOUBLES battle |
| `wheat_war_farms` | Recognition/stand-down arc; the SQ1 field-ledger core references a liberated field; economy already stabilized |
| `board_and_founder` | SQ1's scrubbed portrait + frag_9 **seed** the Founder reveal; must not pre-empt it |
| `shrines_audit` | Ice Shrine ladder coords sit outside their own install.json zone (see below) |
| `legendaries_nobles` | Ice Shrine High Priest Glacius runs **Kyurem lv 78** — legendary encounter policy |

### Open questions / showrunner decisions
1. **ACE LEVEL — 70 vs 76.** Convention (Ryujin 64, Scorchspire 76) and CLAUDE.md "entry-cap+2"
   give **70**; the brief's "ace 76" duplicates Scorchspire's ace. **Recommend 70.** Confirm.
2. **Leader team files out of sync.** `gyms/nifl_town.json` = ace 66 / 5 mons; `rctmod/…/nifl_leader.json`
   = ace 70 / 3 mons. Which is authoritative, and should Boreas be RCT-spawned or gym-config-spawned?
   Reconcile to one 6-mon ace-70 team.
3. **Ice Shrine coordinate mismatch.** `ice_shrine.json` trainers at x 3634–3644 / **y68** sit
   *west of and below* the install.json "Ice Shrine" zone (x≥3667, `centerY:64`). Move trainers into
   the zone, or move the zone? → `shrines_audit`.
4. **Kyurem policy.** High Priest Glacius' ace is a box legendary (Kyurem 78). Is it boss-only, or
   does clearing the shrine make it catchable? → `legendaries_nobles`.
5. **act3 side-quest registry.** Confirm creating `trainers/side_quests/act3.json` (vs extending
   `act1.json`) for the two new SQ battle trainers.
6. **Interior-trainer bodies.** The 6 gym trainers need dialog-src character bodies at their existing
   y112 coords — adopt CSV bodies (need UUIDs) or spawn-once via `placement:{}` (no uuid)? Builder call.
7. **How explicit can SQ1's portrait get?** Nifl is labeled act 3 in the schema but LORE reserves the
   *named* reveal for post-Royal-League. Designed to the "burned-out face / unmistakable posture"
   line so it **circles, never closes** — confirm that read is acceptable this early.
8. **Live shop tier at Nifl.** Post-HQ the relief catalog may already be active; confirm which
   `badge_9` variant (`badge_9` vs `badge_9_relief*`) the mart shows so stock/prices read correctly.
