# 12 — Scorchspire (Gym 10, Fire, cap →80) + Groudon volcano + Fire Shrine

> Area key: `scorchspire`. The tenth and **final** gym. The last gate before the Royal
> League. A town welded onto a live volcano, a forge-lord leader, a wild Groudon noble in
> the crater (monument-warned, warden-guarded, caught or fought at full Nuzlocke stakes),
> and a Company skeleton crew whose scrubbing is finally failing on its own
> face. `frag_10 — "face your own signature."`
>
> **Grounding note:** the gym config, both gym-leader dialog entries, `frag_10`, the
> Scorchspire TOWN + Fire Shrine + Volcano Peak zones, and the rctmod leader/apprentice
> team files already exist. This plan retunes and *populates* around that skeleton — it
> does not invent it. Every coordinate is either lifted from a file (cited) or marked
> **PROPOSED (needs builder confirm)**.

---

## 1. Concept & fantasy

**One-line pitch:** *You climb a burning spire to earn the last badge, wake the god
sleeping in its heart, and watch the people the Company sent to erase you recognise your
face in the firelight — right before the League asks you the one question you cannot
answer yet.*

The fun / marquee stream moments:

- **The forge-lord.** Leader Vulcan is a smith, not a pyromaniac. His whole voice is
  *"I know the difference between a tool and the hand that made it."* He looks at the
  player and clocks that they *built something* — the closest any NPC has come to the
  truth without saying it. It lands harder because it is gym 10 and the audience already
  knows.
- **Groudon in the crater.** The single biggest set-piece before the League: the volcano's
  heart is rising. A **monument at the crater rim warns you** — *"WARNING: the mountain is
  not asleep"* — the crater wardens test you (the PvP phase), and then the titan itself
  surfaces as a **normal wild Pokémon** at cap level: catchable, killable, and fully
  Nuzlocke-live (Volcano Peak is `mobsSpawn:true`). One god, one hardcore run, one throw —
  the player chooses on camera whether to risk it. Resolving it settles the mountain — a
  visible, earned "THE MOUNTAIN QUIETS" beat. (Noble archetype per the showrunner ruling
  2026-07-06; contract canonical in `16_legendaries_nobles.md` §4.1.)
- **Recognition, curdled.** This is the **late** tier of the scrubbing arc. The Company is
  economically toppled (DJ fell after gym 7). The agents still here are a skeleton crew,
  and the higher-rank ones **stand down** or **flee** rather than raise a hand — because
  they remember the portrait that came down. One clerk has *defected to the last town
  before the League* carrying proof, and Asset Recovery came to collect her.
- **Signature theme, everywhere.** Forged steel is signed steel. The Company's leftover
  requisition here is literally *a contract with the founder's void where a signature used
  to be*. `frag_10` is "face your own signature." The gym, the quest, and the fragment all
  rhyme on the same word.

---

## 2. Narrative role

| Field | Value | Source |
|---|---|---|
| Act | **Late Act 2 → doorstep of Act 3.** Post-HQ-raid, pre-Royal-League. (`scorchspire_leader.json` tags `act:3` loosely — it is the League-adjacent last gym.) | char file |
| `cd_instability` | **25 (flat, stabilised).** DJ's defeat set idx→25; gyms 8/9/10 all sit at 25. Scorchspire does **not** fire `economy/gym_destabilize` (that reward is Act-1 only — the current config correctly omits it). | LORE_BIBLE §8; gym config |
| Memory fragment | **`frag_10` — "Everything points one direction now. Inward." / "Beat the League. Then face the only signature you have not yet read: your own."** Granted by leader defeat (`function .../memory/gym/frag_10`). Circles the reveal; never closes it. | `frag_10.mcfunction` |
| Recognition tier | **late** — some stand down; others panic and double down on "there was never a founder." Civilians never recognise him. | char file; LORE_BIBLE §4 |
| Level cap | Enter at **74** (Nifl unlock). Beating Vulcan unlocks **80**. Next gate after that is the Champion → 85. | CLAUDE.md ladder |
| Canon ties | Vulcan's "your own face is waiting past the League" line is the last pre-mirror foreshadow. The defector's memo is a **scrubbing artifact** that feeds the Board/Founder reveal chain. Groudon's buried rage mirrors the shadow-self motif (Ignis's "it leaned toward yours"). | LORE_BIBLE §4–5 |

---

## 3. Layout & placements

### Zones (all from `data/cobblemon_initiative/install.json`)

| Zone | Type | Coords | Notes |
|---|---|---|---|
| **Scorchspire** | TOWN | polygon x 3579–3776, z 4413–4734, centerY 64, color `#e61e46` | Town centroid ≈ **(3677, ~68, 4573)**. Gym interior is UP the spire at **y≈100**. |
| **Fire Shrine** | SHRINE | polygon x 3459–3595, z 4632–4768, centerY 64, `#FF5722`, `hostileOnly`, `mobsSpawn:false` | Centroid ≈ **(3528, 64, 4700)**. Directly SW of town — the "nearby" shrine. |
| **Volcano Peak** | LANDMARK | center **(3805, 64, 3746)**, radius **87**, `mobsSpawn:TRUE` | The only true volcano crater zone; ~800 blocks N of town center. **Proposed Groudon arena.** `mobsSpawn:true` = it is deliberately dangerous, unlike the safe town. |

### NPC / prop placements

Gym-interior coords are lifted from `trainers/gyms/scorchspire.json` (registry coords, y=100
spire interior). Per ENGINE_FINDINGS gym-2..10 leaders/trainers **have no bodies yet** — these
are registry marks; the body is placed by adding `placement:{}` (latch-spawn) or `uuid`.

| NPC / prop | Role | Coord | Confidence |
|---|---|---|---|
| `scorchspire_leader` — Leader Vulcan | Gym leader (forge-lord) | (3700, 100, 4511) | registry coord; body TBD |
| `scorchspire_guide` — Gym Guide | Ladder hint NPC | (3690, 100, 4500) | **PROPOSED** |
| `scorchspire_trainer_1` — Kindler Blaze | Floor trainer | (3688, 100, 4508) | registry coord |
| `scorchspire_trainer_2` — Fire Breather Ember | Floor trainer | (3691, 100, 4514) | registry coord |
| `scorchspire_trainer_3` — Pyrotechnician Scorch | Floor trainer | (3685, 100, 4511) | registry coord |
| `scorchspire_trainer_4` — Flame Dancer Cinder | Floor trainer | (3692, 100, 4517) | registry coord |
| `scorchspire_jr_apprentice` — Jr. Apprentice Ember | Jr. apprentice | (3693, 100, 4511) | registry coord |
| `scorchspire_apprentice` — Apprentice Inferno | Apprentice (**DOUBLE**) | (3694, 100, 4508) | registry coord |
| `sq_forge_sena` — Forgemaster Sena (SQ1 giver) | Quest giver | (3670, 68, 4560) | **PROPOSED** (town forge) |
| `sq_cinderwatch_aya` — Cinder-Watch Aya (SQ2 giver) | Vulcanologist / Groudon hook | (3700, 70, 4470) | **PROPOSED** (north gate toward Volcano Peak) |
| `sq_severance` — Clerk "Severance" (SQ3 giver) | Defector | (3620, 66, 4660) | **PROPOSED** (hidden by the shrine road) |
| `sq_asset_recovery_lead` / `_second` (SQ3 duo) | Tag-team double | flank Severance ±2 | **PROPOSED** |
| `sq_recovery_agent` (SQ1 collector) | Late-tier agent | (3665, 68, 4552) | **PROPOSED** (by the forge) |
| Crater-rim monument (Groudon trigger) | Warning prop, dialog-only ("WARNING: the mountain is not asleep") | Volcano Peak rim, **(3805, ~110, 3746)** | **PROPOSED**; zone confirmed |
| Crater wardens ×2–3 (Groudon PvP phase) | Guardian ladder (Ashkeeper last) | near the rim monument | **PROPOSED** (teams owned by `legendaries_nobles`) |
| Groudon (wild noble spawn) | Legendary set-piece — spawns as a **normal wild Pokémon** after the PvP phase | Volcano Peak crater bowl, **(3805, ~40–64, 3746)** | **PROPOSED** interior anchor; zone confirmed (`mobsSpawn:true` — full stakes) |
| `fire_shrine_leader` — High Priest Ignis | Shrine leader | shrine centroid (3528, 64, 4700) | **PROPOSED** exact; zone confirmed (owned by `shrines_audit`) |
| "Requisition post" prop (SQ1 ledgers) | Staged tag-chain anchor (no block edits) | on Sena's entity | dialog-only, per Tomo pattern |

---

## 4. Gym / core structure — the PvP ladder

The prerequisite chain already exists in `trainers/gyms/scorchspire.json` and forms the PvP
ladder (each battle gated on the previous `defeated_*`). It matches the Hua Zhan template
1:1. **The apprentice is the ladder DOUBLE** (`GEN_9_DOUBLES`), same as every other gym.

```
nifl_leader (gym 9 handoff)
  ├─ scorchspire_trainer_1  ┐
  ├─ scorchspire_trainer_2  ├─ (all four gate only on nifl_leader — parallel floor)
  ├─ scorchspire_trainer_3  │
  └─ scorchspire_trainer_4  ┘
        │ (trainer_1 + trainer_2)
        ▼
  scorchspire_jr_apprentice   [prereq: nifl_leader, trainer_1, trainer_2]
        ▼
  scorchspire_apprentice      [prereq: jr_apprentice]  ← ★ GEN_9_DOUBLES
        ▼
  scorchspire_leader          [prereq: apprentice]     ← Vulcan, GEN_9_SINGLES
```

### Balance — entry cap **74**, ace = **76** (canon), whole roster shifts in step

> ⚠ **DECISION FLAG.** The assignment brief says "leader (ace 82)". The **canonical
> balance rule** (CLAUDE.md / ENGINE_FINDINGS round-10e) is **ace = entry-cap + 2 = 74 + 2
> = 76**, and the round-10e ace list literally ends `…70/76` for gym 10. The **shipped
> rctmod team already sits at ace 76** (Charizard 76). I design to **76** and treat 82 as a
> transcription slip of "unlock-cap(80)+2". If the showrunner genuinely wants gym 10 to
> break the rule and run +8 over cap, that is a deliberate exception to confirm — do not
> silently ship 82.

**Current state to reconcile (two competing team sources — a real gotcha):**
- `data/rctmod/trainers/scorchspire_leader.json` = the team TBCS/rctapi actually fights:
  **only 3 mons** (magmortar 74 / typhlosion 75 / charizard 76).
- `trainers/gyms/scorchspire.json` embedded team = **4 mons, stale ace 70** (Arcanine 68 /
  Charizard 68 / Heatran 69 / Volcarona 70). Vestigial vs the rctmod file.
- Also: `scorchspire_apprentice.json` (rctmod) has `battleFormat: GEN_9_SINGLES` while the
  gym config marks the apprentice `GEN_9_DOUBLES` — **fix the rctmod file to DOUBLES** and
  give it 4 mons. The actual format is set by the dialog `tbcs battle GEN_9_DOUBLES` token,
  but keep the team file honest.

**Leader team sketch — Leader Vulcan (ace 76, "the signature"):** 6 mons, cap-74 fight.

| Slot | Species | Lvl | Set / role |
|---|---|---:|---|
| 1 | Ninetales | 73 | Drought lead — Will-O-Wisp, Nasty Plot, Fire Blast, Solar Beam |
| 2 | Arcanine | 74 | Intimidate + Choice Band — Flare Blitz, Extreme Speed, Wild Charge, Close Combat |
| 3 | Houndoom | 74 | Flash Fire — Nasty Plot, Fire Blast, Dark Pulse, Sludge Bomb |
| 4 | Typhlosion | 75 | Blaze — Eruption, Flamethrower, Focus Blast, Extrasensory |
| 5 | Heatran | 75 | Flash Fire + Leftovers — Magma Storm, Earth Power, Flash Cannon, Stealth Rock |
| 6 ★ | **Volcarona** | **76** | Flame Body + Life Orb — **Quiver Dance**, Fiery Dance, Bug Buzz, Giga Drain (the sweep; his "crown of embers") |

Sub-tier levels (shift the whole ladder into the 74 band, floor below cap):
- trainer_1 ≈ 68–69 · trainer_2 ≈ 69 · trainer_3 ≈ 70 · trainer_4 ≈ 70–71
- jr_apprentice ≈ 72–73 · apprentice (DOUBLE) ≈ 73–74 (mid-staff ace ≈ cap 74) · leader ace 76.

### Marquee double (owned by SQ3, not the ladder)

The ladder double (Inferno) is a standard 4-mon `GEN_9_DOUBLES`. The **stream-marquee**
double is the **Asset Recovery tag-team** in Side Quest 3 — two placed NPCs fighting as one
joint `GEN_9_DOUBLES` trainer, exactly the `agent_yield_lead` + `agent_yield_second`
pattern (battle on the lead entity, second stands down in dialog). See §5.

---

## 5. Quests & side quests

Three quests, on-theme (forge/volcano/late-recognition), all fun and streamable. Training
packs ride the one-time completion latch only (showrunner rule); the villain SELL/consolation
forks and the legendary reward get none.

### SQ1 — "The Last Forge Order" (forge / signature)

| | |
|---|---|
| **Giver** | Forgemaster Sena, Vulcan's forge-hand (`sq_forge_sena`, PROPOSED (3670,68,4560)). |
| **Hook** | The Company put a standing **commodity requisition** on Scorchspire's forge — all its steel contracted to build granary silos and field fences. DJ fell; the contract is void; the forge still runs the dead order because nobody dared cancel a Company seal. |
| **Steps** | Walk the forge line with Sena and **burn three requisition ledgers** in the crucible — a staged tag chain on Sena's entity (`forge_order_1..3`), copying the Tomo-seal dialog pattern (per-stage confirm buttons, actionbar receipts, ZERO block edits). The third ledger is the founder's own charter page — his signature line **scrubbed to a blank**. Then **`sq_recovery_agent`** (a late-tier Asset Recovery agent, posted to collect the last shipment) steps in. |
| **Gates** | Entry: `defeated_nifl_leader` (in town). Fork resolves on `defeated_sq_recovery_agent` OR a talk-past (`forge_order_stand_down`). |
| **Recognition** | The agent is **late** tier: he recognises the *stance*, not just the steel — "you set your feet like someone who used to sign these orders" — then either **stands down** (talk-past) or fights half-heartedly and flees on loss. No founder naming. |
| **Rewards** | Battle win/talk-past → **600 CD** (`economy/payout {amount:600}`) + `loot .../npc_gift/training_major` (one-time latch) + a forged keepsake item (charcoal ×8 / a Charcoal held item). Tag `forge_order_done`. |
| **Resolution** | Sena reforges the blanked charter page into a nameless ingot — "a signature you can hold instead of read." A quiet setup for the mirror. |

### SQ2 — "Heat Death" (the Groudon marquee — staging owned here; noble contract in `16_legendaries_nobles.md`)

Groudon is a **NOBLE with a MONUMENT trigger** (showrunner ruling 2026-07-06): warning
monument → PvP guardian ladder → Groudon spawns as a **normal wild Pokémon**, full
Nuzlocke stakes. Aya is the *signpost*, not the trigger — the monument fits the crater
design (nobody lives on the rim to ask for help; the mountain warns for itself).

| | |
|---|---|
| **Giver** | Cinder-Watch Aya, town vulcanologist (`sq_cinderwatch_aya`, PROPOSED (3700,70,4470), by the north gate toward Volcano Peak) — flavour hook + directions only. The **trigger** is the crater-rim monument (§3). |
| **Hook** | The mountain's heart is **rising**. Aya's tremor readings spike; the town's heat-vents are venting; something in the crater is *awake and furious*. She cannot climb it. You can. |
| **Steps** | (1) Aya reads the tremors → flavour tag `volcano_roused`, waypoint to the rim. (2) Player ascends to **Volcano Peak** (3805,64,3746). (3) The **rim monument** warns — *"WARNING: the mountain is not asleep"* — arm button sets `noble_groudon_armed` + bossbar/weather dressing. (4) **PvP phase:** the crater-warden guardian ladder (2–3 battles, Ashkeeper last — teams/wiring owned by `legendaries_nobles` §6). (5) On the last `defeated_*` tag, a one-shot latched function spawns **wild Groudon** in the crater bowl (`noble_groudon_called`). (6) Catch it, KO it, or walk away — **standard wild rules, full stakes** (Volcano Peak is `mobsSpawn:true`); resolution sets `groudon_settled` dressing. |
| **Gates** | Entry: `defeated_scorchspire_leader` (recommend the badge as the price of the climb — endgame set-piece; wild Groudon **Lv 78** in the cap-80 window) **OR** `defeated_nifl_leader` pre-badge (then drop the spawn to ~Lv 72 under cap 74). Monument gated on `volcano_roused`; one-shot via `noble_groudon_called`; re-call policy per `16_legendaries_nobles.md` §4.1/§9. |
| **Rewards** | **The wild Groudon encounter itself** — catchable, no gift fallback, no CD attached to the legendary. Warden-ladder onwin pays nothing (gate, not grind); Aya pays **500 CD** + a fire item (Fire Stone / Charcoal) on `groudon_settled` for closing the survey. **No training pack** — the legendary is the reward. Aya: "The mountain quiets." Title card beat. |
| **Resolution** | The crater settles; Volcano Peak's ambient danger eases (narratively). Groudon's rage mirrors the buried shadow-self — Aya notes it "was not angry at the mountain. It was angry at being *forgotten*." The last thematic nudge before the League. |

### SQ3 — "Retirement Package" (late recognition + the marquee tag-team double)

| | |
|---|---|
| **Giver** | Clerk "Severance" (`sq_severance`, PROPOSED (3620,66,4660), hiding on the shrine road). A mid-rank Company clerk who fled to the last town before the League after DJ fell. |
| **Hook** | She carries the **internal memo trail** proving "the founder retired" was a fabrication — a scrubbing artifact. She wants to hand it off and vanish. Asset Recovery followed her. |
| **Steps** | (1) Talk → she explains, sets `severance_met`. (2) **Asset Recovery** ambushes: a **joint `GEN_9_DOUBLES` tag-team** — `sq_asset_recovery_lead` (carries the battle) + `sq_asset_recovery_second` (stands down in dialog on `defeated_sq_asset_recovery`), per the `agent_yield_lead`/`_second` pattern. (3) Win → she hands over the **memo item** and disappears. |
| **Gates** | Entry: `defeated_nifl_leader`. Ambush gated on `severance_met` + `not_tag defeated_sq_asset_recovery`. Payout on `defeated_sq_asset_recovery`. |
| **Recognition** | **Late.** The recovery lead: "You are supposed to be *filed*." On loss he doubles down on the official line — "there was never a founder, so you are no one" — the canon panic response. The memo text **circles** the truth (retirement/erasure), never closes it (reveal is post-League). |
| **Rewards** | **600 CD** + `training_major` (one-time latch) + memo item (lore) + story tag `retirement_memo_taken` (a breadcrumb the Board/Founder reveal chain can reference). |
| **Resolution** | Severance leaves for the generated-terrain unknown ("I hear there is a dragon out east that does not file anyone"). A wink at the post-game. The memo joins the scrubbing-artifact set that pays off at the mirror. |

---

## 6. Trainers & teams needed

### rctmod team files — `data/rctmod/trainers/`

| File | State | Action |
|---|---|---|
| `scorchspire_leader.json` | exists, **3 mons ace 76** | **Expand to 6 mons** per §4 sketch (ace 76). |
| `scorchspire_apprentice.json` | exists, `GEN_9_SINGLES`, 3 mons | **Set `GEN_9_DOUBLES`, 4 mons**, ace ≈ 74. |
| `scorchspire_trainer_1.json` | exists | Retune into 68–69 band. |
| `scorchspire_trainer_2.json` | exists | Retune into 69 band. |
| `scorchspire_trainer_3.json` | **MISSING** (ENGINE_FINDINGS §5.4 — gyms 3–10 lack trainer_3/4/jr) | **Create**, 70 band. Team exists embedded in gym config (Houndoom/Magmar) — hoist it out. |
| `scorchspire_trainer_4.json` | **MISSING** | **Create**, 70–71 band (Ponyta/Ninetales). |
| `scorchspire_jr_apprentice.json` | **MISSING** | **Create**, 72–73 band (Magmar/Arcanine). |
| `sq_recovery_agent.json` | new | SQ1 collector, `GEN_9_SINGLES`, ~72–74, 2–3 fire mons. |
| `sq_asset_recovery.json` | new | SQ3 joint double — `GEN_9_DOUBLES`, 4 mons ~73–74 (both suits fight as this one team). |
| `fire_shrine_leader.json` | exists but **`{}` empty** | Team owned by `shrines_audit`; empty = a battle button that no-ops. Flag for that area. |

### Graph nodes — `data/rctmod/mobs/trainers/single|groups/`

- `groups/scorchspire_trainer.json` exists (`requiredDefeats:[["nifl_leader"]]`, covers
  trainer_1..4). Good — do **not** add per-trainer cross-edges (cycle risk).
- `single/scorchspire_leader.json` + `single/scorchspire_apprentice.json` exist. **Add
  `single/scorchspire_jr_apprentice.json`** (requiredDefeats → `["scorchspire_trainer_1","scorchspire_trainer_2"]`
  plus `nifl_leader`) so the apprentice prereq chain runs through it.
- **CYCLE CHECK REQUIRED** after any edit (ENGINE_FINDINGS §2 rctmod — a cycle = StackOverflow
  crash at world start). Correct shape: gym-10 floor/jr gate on `nifl_leader`; nothing gates
  back onto scorchspire. Re-run the check across **singles AND groups**.

### Registry config — `data/cobblemon_initiative/trainers/`

- `gyms/scorchspire.json` — exists (full 7-role ladder + rewards + prizes + coords). Retune
  its embedded teams to match §4 or accept them as vestigial and treat rctmod files as the
  balance source (recommend the latter; keep prize/reward/coord metadata). `name` must equal
  `displayName` for BATTLE_VICTORY name-match (ENGINE_FINDINGS §4 wiring recipe).
- **New:** `trainers/side_quests/act3.json` (parallels `act1.json`) registering
  `sq_recovery_agent`, `sq_asset_recovery`, and any SQ side trainers.

### Battle formats vs cap ladder (entry cap 74)

| Battle | Format | Ace lvl | Δ vs cap |
|---|---|---:|---|
| Floor trainer_1..4 | SINGLES | 68–71 | −6…−3 |
| Jr. apprentice | SINGLES | 72–73 | −2…−1 |
| Apprentice Inferno | **DOUBLES** | 74 | 0 (mid-staff = cap) |
| **Leader Vulcan** | SINGLES | **76** | **+2 (canon)** |
| SQ1 recovery agent | SINGLES | ~73 | −1 |
| SQ3 Asset Recovery duo | **DOUBLES** | ~74 | 0 |

---

## 7. Economy & rewards

| Source | Payout | Sink / tie |
|---|---|---|
| Leader Vulcan (prize) | **6000 CD** (existing config prize) + `frag_10` + `cobblemon-initiative shop badge_10` | `badge_10` = the top pre-League catalog (Full Restores, Revives, League-prep TMs) — a deliberate CD sink before the Champion. **No `gym_destabilize`** (idx stays 25). |
| SQ1 forge | 600 CD + `training_major` + charcoal keepsake | one-time latch `forge_order_done`. |
| SQ2 Groudon | 500 CD + Fire Stone/Charcoal + the encounter | no training pack (legendary IS the reward). |
| SQ3 retirement | 600 CD + `training_major` + memo | one-time latch `retirement_memo_taken`. |
| Fire Shrine (Ignis) | 2500 CD prize (existing char) | owned by `shrines_audit`. |

Route all sidequest CD through `function .../economy/payout {amount:N}` (skew + actionbar
receipt), never `pay_macro` directly. Battle prizes stay flat in `onwin`. Shop-tier /
liberation ties: `badge_10` is the last badge tier; field-liberation relief tiers (`_relief1/2`)
are resolved live by `ShopTierManager` from `fields_liberated` and are independent of this area
(the wheat war is settled by now, but the relief plumbing still applies if fields remain).

---

## 8. Implementation notes / FUTURE-ME HOOKS

**Copy these patterns:** Hua Zhan City interior ladder (`characters/hua_zhan/station_*`,
`garden_master_wei`, `groundskeeper_aya`) for floor-trainer bodies; `takehara/sq_beekeeper_tomo.json`
for the **staged tag-chain quest** (SQ1 forge ledgers = the seal-walk, ZERO block edits);
`takehara/agent_yield_lead.json` + `agent_yield_second.json` for the **joint doubles tag-team**
(SQ3) — battle on the lead, partner stands down in dialog gated on the shared `defeated_*`.

**Files to create / edit (full paths):**

- Interior bodies (new dir): `dialog-src/characters/scorchspire/`
  - `scorchspire_trainer_1..4.json`, `scorchspire_jr_apprentice.json`, `scorchspire_apprentice.json`
    (each with `battle.trainer` → the rctmod id, `battle.format` matching the ladder;
    apprentice = `GEN_9_DOUBLES`) + `placement:{}` (latch-spawn) or `uuid` (builder body).
  - `sq_forge_sena.json`, `sq_cinderwatch_aya.json`, `sq_severance.json`,
    `sq_asset_recovery_lead.json`, `sq_asset_recovery_second.json`, `sq_recovery_agent.json`.
- Existing, edit in place: `dialog-src/characters/gym/scorchspire_leader.json` (add body),
  `.../scorchspire_guide.json` (add body + guide dialog), `dialog-src/dialog/gym_leader_scorchspire.json`
  (already good — keep Vulcan's forge/signature voice).
- Dialog trees: `dialog-src/dialog/sq_forge_order.json`, `sq_heat_death.json`,
  `sq_retirement_package.json` (or `dialog_inline` on the giver, per Tomo).
- Teams: `data/rctmod/trainers/{scorchspire_trainer_3,scorchspire_trainer_4,scorchspire_jr_apprentice,sq_recovery_agent,sq_asset_recovery}.json`;
  edit `scorchspire_leader.json` (→6 mons), `scorchspire_apprentice.json` (→DOUBLES, 4 mons).
- Graph: `data/rctmod/mobs/trainers/single/scorchspire_jr_apprentice.json` (+ cycle check).
- Registry: `data/cobblemon_initiative/trainers/side_quests/act3.json` (new); retune
  `trainers/gyms/scorchspire.json` teams (or leave vestigial — pick one, document it).
- **Groudon (own the local staging; `legendaries_nobles` owns the archetype contract):** a
  hook NPC (`sq_cinderwatch_aya`, flavour + `volcano_roused`) + the crater-rim **monument
  prop** (`dialog-src/characters/legendary/monument_groudon.json`, dialog-only, notice-board
  pattern) + the warden bodies. Functions per the `legendaries_nobles` layout:
  `function/legendary/noble/groudon/arm.mcfunction` (tag + bossbar + weather),
  `spawn.mcfunction` (latched `spawnpokemonat` in the crater bowl — **UNVERIFIED
  (jar-verify)** grammar, fired from the last warden's onwin, winners-first key 1),
  `cleanup.mcfunction` (re-call latch + "THE MOUNTAIN QUIETS" dressing), plus Aya's
  `groudon_settled` payout (500 CD + item). **Do not** invent noble mechanics here —
  consume the trigger→PvP→wild-spawn→cleanup contract from `16_legendaries_nobles.md` §4.1.
- **Fire Shrine (own the town→shrine hook; `shrines_audit` owns internals):** a signpost/guide
  line pointing at the shrine road; the shrine challenge (`shrine_challenges/fire.json` =
  timed_parkour 120s) and `fire_shrine_leader` team belong to `shrines_audit`.

**Pipeline (run in this order — ENGINE_FINDINGS §3):**
`scripts/content_compile` → `scripts/generate_granary_tiers` → `scripts/update_preset_index`
→ `scripts/generate_npc_function` → `gradle build`. `content_compile` auto-runs the last step,
but granary tiers must precede the final preset-map hash.

**Gotchas (bytecode-verified):**
- **Ace = 76, not 82** (§4 decision flag). The rctmod file already agrees.
- Macro-delivered text (memory fragments, `economy/payout`, `onwin`, actionbar receipts):
  **no double-quotes, avoid apostrophes** — no escaping in the macro layer. Write the founder
  memo / Groudon beats apostrophe-free.
- Numeric gates (badges/instability/recognition) can't be raw conditions — only `PLAYER_TAG`.
  Design SQ gates on **tags** (`defeated_*`, `forge_order_*`, `severance_met`, `volcano_roused`).
- **`onwin` tokens are WINNERS-FIRST** (key 1 = player won). Lose lists are the mirror
  (`cobbledollars remove @2`, `@1 say <taunt>`); never `remove @1` / `@2 say` on the lose side.
- rctmod graph edits → **cycle-check singles + groups** or the world StackOverflows at start.
- Apprentice **must** fight as DOUBLES (fix the rctmod `battleFormat` + the dialog `tbcs battle
  GEN_9_DOUBLES` token both).
- Latch-spawned NPCs get random UUIDs → any NpcSight arming (if a recovery agent uses `pursue`)
  needs a manual `npcsight add <uuid>` pass after first spawn (not automatic).
- Skins: use **CUSTOM local** (rctmod ships 1560 trainer skins — grunt/boss/scientist perfect
  for Company agents) at `config/easy_npc/skin/humanoid/<uuid>.png`, referenced by
  `skin:{type:"custom","uuid":[…]}`; never URL skins (hotlink-blank).

---

## 9. Dependencies & open questions

### Depends on (other area keys)

| Area key | Why |
|---|---|
| **`legendaries_nobles`** | **Hard dep.** Owns the noble archetype contract (monument trigger → PvP guardian ladder → one-shot latched wild spawn → despawn/cleanup, `16_legendaries_nobles.md` §4.1) and the warden team files; SQ2 Groudon consumes it. This area owns only local staging (monument/warden placement, Aya, the settle payout). |
| **`shrines_audit`** | Owns the Fire Shrine challenge internals + `fire_shrine_leader` team (currently `{}` empty). This area owns only the town→shrine hook. |
| **`nifl_town`** | Prereq handoff — the whole Scorchspire ladder gates on `defeated_nifl_leader`. |
| **`royal_league`** | Scorchspire is the last gate before it; Vulcan's outro and `frag_10` point straight at the League. |
| **`mainline_spine`** | `frag_10`, `cd_instability=25`, `shop badge_10`, memory HUD, `economy/payout`. |
| **`gym_system_pvp_doubles`** | The ladder + apprentice-double template this gym instantiates. |
| **`company_hq`** | DJ's defeat (idx→25) is what makes the recognition tier **late** and the Company a skeleton crew here. |
| **`wheat_war_farms`** | SQ1's requisition and SQ3's memo are wheat-war cover-up closure; shop-relief plumbing ties to `fields_liberated`. |
| **`board_and_founder`** | SQ3's retirement memo is a scrubbing artifact feeding the post-League Founder reveal chain. |

### Open questions — showrunner decisions

1. **Leader ace: 76 (canon entry-cap+2) or 82 (brief literal)?** Recommend **76** — the shipped
   rctmod team already sits there and 82 breaks the balance rule by +8-over-cap. Confirm if 82
   is a deliberate final-gym exception.
2. **Which volcano is Groudon's arena?** The existing **Volcano Peak** LANDMARK (3805,64,3746,
   r87, `mobsSpawn`) is the only true crater zone but sits ~800 blocks N of town. Is that the
   intended arena, or should Groudon be inside the **Scorchspire spire crater** (interior
   geometry unknown → need builder coords)? Need a builder-confirmed battle anchor either way.
3. ~~Groudon: catchable or battle-only noble?~~ — **RESOLVED by showrunner ruling
   (2026-07-06):** after the PvP phase Groudon spawns as a **normal wild Pokémon** —
   catchable, standard wild rules, full Nuzlocke stakes, no exemptions and no gift
   fallback. Remaining sub-question (owned by `legendaries_nobles` §9): the KO'd-noble
   re-call policy.
4. **SQ2 gate — pre-badge or post-badge?** Recommend gating the climb on `defeated_scorchspire_leader`
   (endgame set-piece) rather than `nifl_leader`. Confirm.
5. **Villain density at gym 10.** With the Company economically toppled, is the SQ3 tag-team
   ambush + SQ1 collector the right amount of villain presence, or should gym 10 be villain-quiet
   except for stand-downs? (Design leans: two brief agent beats, both resolvable by stand-down.)
6. **Fire Shrine timing/level:** pre-League optional (cap 80 window) or post-game revisit? Sets
   Ignis's team target — hand to `shrines_audit`.
