# 07 — Kalahar Reach (Gym 6, Ground, cap → 56): The Dust-Bowl Ledger & the Grain-Buyer Ambush

> **Area key:** `kalahar_reach`
> **Status of what exists (verified in-tree):**
> - Authoring stubs `dialog-src/characters/gym/kalahar_leader.json` (Leader Gaia, recognition `mid`) +
>   `kalahar_guide.json`, and the full leader tree `dialog-src/dialog/gym_leader_kalahar.json`
>   (challenge + after-defeat, both written).
> - A COMPLETE RCT/registry ladder in
>   `src/main/resources/data/cobblemon_initiative/trainers/gyms/kalahar_reach.json`
>   (kalahar_trainer_1..4, jr_apprentice, apprentice-**DOUBLE**, leader) with embedded teams, plus
>   RCT-format teams `rctmod/trainers/kalahar_leader.json`, `kalahar_apprentice.json`,
>   `kalahar_trainer_1.json`, `kalahar_trainer_2.json` and spawn defs under `rctmod/mobs/trainers/`.
> - Leader reward already fires `memory/gym/frag_6`, `cobblemon-initiative shop badge_6`, and
>   `economy/gym_destabilize` (+8 → idx 48). Achievement `badge_ground`.
> - **The ambush is ALREADY wired:** `wheat_trader_2.json` is a builder-placed body (uuid
>   `45240216-2c97-4a78-af13-37ed427040ee`, `location:"Kalahar Reach"`, `act:"2"`, `mid`) that flips to
>   `wheat_trader_hostile` at 4 liberated fields via `function/wheat_trader/tick.mcfunction`, and the
>   `dialog/wheat_trader.json` `hostile` entry forces the fight.
> - The **Ground Shrine** exists as a zone + `dialog-src/characters/shrine/ground_shrine_leader.json`
>   (High Priest Terran) + cultists in `rctmod/trainers/` (placement owned by `shrines_audit`).
>
> **What is MISSING (this doc's scope):** the town skin (interior-trainer bodies + civilian/service
> cast), **2–3 side quests**, and the marquee — a *properly levelled* Grain-Buyer ambush (the shipped
> `wheat_trader_ambush` team is lv 38–39, trivial against a lv-50 party). Plus two balance
> reconciliations (leader ace, and the shared ambush defeat-tag).

---

## 1. Concept & fantasy

**One-line pitch:** *Kalahar Reach is the dust bowl where the wheat war stops being an abstraction —
Ground-type patience against a corporate land-grab, an unearthed boundary stone that bears the
founder's own seal, and a grain-buyer caravan that finally says the quiet part out loud: "You are the
one the memos warned us about."*

The fun:
- **Stall-and-hold made a whole town.** Every previous gym has been momentum. Gaia's Ground-types
  *wait* — Hippowdon sandstream chip, Slack Off, Whirlwind, a Garchomp that punishes greed. In brutal
  Nuzlocke this is the first true "attrition wall": you don't out-tempo it, you dig in. Gaia's shipped
  line — *"Ground-types do not chase. They wait, they hold their ground, and they let the world break
  against them"* — is the whole design in one breath.
- **The wheat war you can stand in.** Kalahar sits between the seized **Crossroads Granary** (farm_5,
  corporate-owned, to the north) and a **dry town** whose wells the Company drained to irrigate that
  field. This is the first gym where the monopoly is *terrain*, not rumor — you can see the pump line.
- **Marquee A — the boundary stone.** A side quest unearths three buried survey stones proving the
  northern fields were common land. The third carries **two** seals: the Company's, and an older
  personal mark the town elder has only ever seen on one charter — the founder's. Mid-recognition lore
  drop, the kind the long-form audience screenshots.
- **Marquee B — the Grain-Buyer caravan ambush.** If the player has leaned into field liberation
  (≥4 fields), the traders here don't sell — they *recognize*. It is the **first place a Company voice
  openly names the founder** on stream, and it's a forced desert-road **double** against a caravan of
  farm-beasts (the commodity currency made flesh). Emergent, earned, and a clean cliff for an episode.

---

## 2. Narrative role

| Field | Value | Source |
|-------|-------|--------|
| Act | **1** (infiltration ladder); the ambush is Act-2-flavored (`wheat_trader_2.json` `act:"2"`) | `kalahar_leader.json` `act:"1"`; brief |
| `cd_instability` | **→ 48** — the leader's `economy/gym_destabilize` (+8) pushes it there; field liberation claws it back −6/field | `economy/gym_destabilize.mcfunction`, `liberation/free_field_apply.mcfunction`, LORE_BIBLE §8 (row 6 → 48) |
| Memory fragment | **frag_6 — "A boardroom. Grey suits, all nodding." / "You held both keys to the ledger. They smiled. You should not have let them smile."** | `function/memory/gym/frag_6.mcfunction` (already built) |
| Recognition tier | **mid.** Enter with 5 badges (`badges_gte_5` era); leader + wheat traders are `mid`. This is the tier of *alarm* — veterans/management sound alarms, traders half-name the founder, civilians stay oblivious. | `kalahar_leader.json`/`wheat_trader_2.json` `recognition_tier:"mid"`; LORE_BIBLE §5 |
| Level cap | Entry cap **50** (from Gaviota); Kalahar unlocks **56** on Gaia's defeat | CLAUDE.md ladder (authoritative); confirmed by `08_cyber_city.md` ("entry cap 56 from Kalahar") |
| Canon ties | The Wheat War terrain (farm_5 Crossroads Granary), the recognition arc's first open founder-naming, the "starve the monopoly" pressure that feeds the HQ gate (`company_hq`) | LORE_BIBLE §4/§5; `wheat_trader/tick.mcfunction` |

**Canon guardrails honored here:** the protagonist is never *named-as-Founder by the narration*. The
boundary stone and the hostile trader **accuse** the face (mid-tier paranoia from "Verified Trust"
memos); the player still does not know. The town elder half-recognizes and *deflects*. Mom is not
present here. Civilians (well-keeper, prospector kids) never recognize the CEO — only the thirst. No
illness angles; corporate-dread comedy (a pump crew filing "agricultural yield optimization" over a
town's drinking water) played straight against real menace.

---

## 3. Layout & placements

**Zones (all builder-confirmed in `install.json`):**

| Zone | Type | Extent (X / Z) | Notes |
|------|------|----------------|-------|
| **Kalahar Reach** | TOWN | X 1882–2248, Z 3883–4265 | `mobsSpawn:false` (safe zone), `hostileOnly`, color `#D4A94E`, subtitle "Gym 6 — Ground Type". Surface **Y≈126** (all gym interior coords are Y126; `centerY:64` is only the cylinder param). |
| **Ground Shrine** | SHRINE | X 1796–1914, Z 3994–4109 | Abuts the town's **west** edge. Center ≈ (1855, ?, 4050). Placement owned by `shrines_audit`; High Priest Terran + 2 cultists already have teams. |
| **Oasis** | LANDMARK | X 1635–1834, Z 4169–4369 | **SW** of town, `mobsSpawn:TRUE` (wild). Center ≈ (1730, 64, 4265). The water-rights quest site. |
| **Crossroads Granary** | FARM (`farm_5`) | X 2246–2373, Z 3482–3557 | **North**, corporate-owned (`activeWhenHolder:farm_5`), reached via Old Caravan Road. The nearest liberatable field; the ambush's economic backdrop. |
| **Old Caravan Road** | ROUTE | town-edge vtx (1928, 3939) → granary vtx (2338, 3495) | The wheat caravan artery; the ambush's natural stage. |
| **Dunewind Trail** | ROUTE | west vtx (1571, 3887) | Desert approach from the west. |

**NPC / prop placements:**

| Entity | Role | Coord | Confirmed? |
|--------|------|-------|-----------|
| Ruin Maniac Dustin (`kalahar_trainer_1`) | interior PvP | (2073, 126, 4047) | **builder-confirmed** (gym config) |
| Hiker Boulder (`kalahar_trainer_2`) | interior PvP | (2076, 126, 4053) | **builder-confirmed** |
| Archaeologist Juno (`kalahar_trainer_3`) | interior PvP | (2070, 126, 4050) | **builder-confirmed** |
| Prospector Vince (`kalahar_trainer_4`) | interior PvP | (2077, 126, 4056) | **builder-confirmed** |
| Jr. Apprentice Dune (`kalahar_jr_apprentice`) | interior PvP | (2078, 126, 4050) | **builder-confirmed** |
| Apprentice Terra (`kalahar_apprentice`, **DOUBLE**) | interior PvP | (2079, 126, 4047) | **builder-confirmed** |
| **Leader Gaia** (`kalahar_leader`) | gym leader | (2085, 126, 4050) | **builder-confirmed** |
| Kalahar Gym Guide (`kalahar_guide`) | service | ≈ (2062, 126, 4050) | **PROPOSED** (needs builder confirm) — near the gym mouth |
| **Warden Ossa** (new) | quest giver (Boundary Stones) + recognition | ≈ (2050, 126, 4085) | **PROPOSED** — town center, by the old records post |
| **Well-Keeper Marisol** (new) | quest giver (Dry Season) | ≈ (2040, 126, 4100) | **PROPOSED** — beside a dry well |
| **Kalahar Nurse** (new) | service (Pokécenter body) | ≈ (2055, 126, 4070) | **PROPOSED** — parity with Takehara/Hua Zhan |
| **Kalahar Martkeeper** (new) | service (Poké-Mart / `shop badge_6`) | ≈ (2060, 126, 4066) | **PROPOSED** |
| Boundary Stone dig-props ×3 (new) | interaction props | ≈ (1980,126,3960) / (2140,120,3900) / (2318,66,3542) | **PROPOSED** — dune edge & Old Caravan Road toward the granary |
| Company pump crew ×2 (new) | villain fights (Dry Season) | ≈ (1730, 66, 4265) | **PROPOSED** — Oasis center |
| Pump-manifold prop (new) | interaction prop (shut valve) | ≈ (1735, 66, 4260) | **PROPOSED** — beside the crew |
| **wheat_trader_2** (Grain Buyer, ambush) | villain (existing body) | uuid-adopted; suggest builder park on Old Caravan Road ≈ (1970, 126, 3945) | **builder-placed** (has uuid); ambush caravan partner PROPOSED beside it |

---

## 4. Gym / core structure

**Type fantasy:** Ground — *stall, hold, and let the world break against you.* Sandstorm chip +
recovery + hazard control. The interior is a rising desert switchback (Y≈126 mesa); each fight gates
on the previous being defeated.

**Interior PvP ladder (already configured in `trainers/gyms/kalahar_reach.json`; levels vs entry cap 50):**

| Order | Trainer | Team (species @ lvl) | Format | Gate (prereq) |
|-------|---------|----------------------|--------|---------------|
| 1 | Ruin Maniac Dustin | Sandshrew 44 / Diglett 44 | SINGLES | `gaviota_leader` |
| 1 | Hiker Boulder | Geodude 44 / Rhyhorn 44 | SINGLES | `gaviota_leader` |
| 1 | Archaeologist Juno | Cubone 44 / Baltoy 44 | SINGLES | `gaviota_leader` |
| 1 | Prospector Vince | Trapinch 45 / Stunfisk 45 | SINGLES | `gaviota_leader` |
| 2 | **Jr. Apprentice Dune** | Sandslash 48 / Marowak 48 | SINGLES | `gaviota_leader` + `kalahar_trainer_1` + `kalahar_trainer_2` |
| 3 | **Apprentice Terra** | Donphan 50 / Flygon 50 / Quagsire 50 / Claydol 51 | **GEN_9_DOUBLES** | `kalahar_jr_apprentice` |
| 4 | **Leader Gaia** | *see below* | SINGLES | `kalahar_apprentice` |

- **The DOUBLE is Apprentice Terra** (already `GEN_9_DOUBLES`). Terra's spread (Donphan `rapid spin`
  + Claydol `earth power`/`rapid spin`, Flygon pivot, Quagsire `unaware`) is a legitimate doubles
  puzzle — the pre-leader gut-check.
- **Gate wiring is done** and every `defeated_kalahar_*` tag + `no_*` inverse already exists in
  `band_tags.mcfunction` (L169–176). No new gate plumbing for the ladder.
- **Early-trainer tune (optional):** lv 44 is 6 under the 50 cap — a touch soft for brutal Nuzlocke.
  Recommend nudging the four openers to **46–48** to keep the ramp tight (44 → 48 → 50/51 → 52).

**Leader Gaia — team sketch (ACE = entry-cap 50 + 2 = 52).** The shipped `rctmod/trainers/kalahar_leader.json`
already tops at **Garchomp 52** ✅ (only 3 mons); expand to a proper 6 for gym 6 while *keeping the ace at 52*:

| Slot | Species | Lvl | Set (theme) |
|------|---------|-----|-------------|
| Lead | **Hippowdon** ♀ | 50 | sand stream / Earthquake, Stealth Rock, Slack Off, Whirlwind (the attrition engine) |
| 2 | **Gliscor** ♂ | 50 | poison heal / Earthquake, Toxic, Protect, Roost (stall pivot) |
| 3 | **Excadrill** ♂ | 51 | sand rush / Earthquake, Iron Head, Rock Slide, Swords Dance (sand sweeper) |
| 4 | **Flygon** ♂ | 51 | levitate / Earthquake, Dragon Claw, U-turn, Fire Blast (Levitate breaks your EQ answer) |
| 5 | **Rhyperior** ♂ | 51 | solid rock / Earthquake, Stone Edge, Ice Punch, Megahorn (assault-vest wall) |
| **Ace** | **Garchomp** ♀ | **52** | rough skin / Earthquake, Dragon Claw, Stone Edge, Swords Dance — held Rocky Helmet / Sitrus |

Bag: `hyper_potion`/`full_restore` ×2–3. Reward block (already in config): `memory/gym/frag_6`,
`shop badge_6`, `economy/gym_destabilize`, prize **3700 CD**, achievement `badge_ground`,
defeat tag `defeated_kalahar_leader` → cap **56**.

> **Balance reconciliation (decision needed):** the RCT team ships ace **52** (rule-correct); the
> registry duplicate in `trainers/gyms/kalahar_reach.json` ships ace **54**; the assignment brief says
> **58**. Per CLAUDE.md's authoritative rule (ace = entry-cap+2) and the shipped RCT team, **52 is
> correct** and 58 is almost certainly a typo (it would make Kalahar the only gym at entry+8). Pick one
> and collapse the two team definitions (see §9).

---

## 5. Quests & side quests

### Q1 — "The Reach Remembers" (Boundary Stones) — lore + recognition, low-combat
- **Giver:** Warden Ossa (new; town elder, keeper of the old land records). Mid recognition.
- **Hook:** The Company's title to the northern fields "is paper." Ossa says the *common-land*
  boundary was marked by three basalt survey stones, buried when the dunes shifted. Unearth them and
  she can file a counter-claim that Crossroads Granary was never the Company's to sell.
- **Steps (mirror the `sq_beekeeper_tomo` seal-chain — zero terrain edits):**
  1. Ossa walks you to the dune line; three **dig-site props** (placement + inline dialog, the
     `survey_wagon` pattern) each gate an interaction that sets `seal_stone_1..3`.
  2. Stone 3 is buried by the **Old Caravan Road near the granary** (2318,66,3542) and is guarded — a
     Company **Land Surveyor** stands on it (optional wager battle `sq_boundary_surveyor`, lv 50–51),
     OR cite Ossa's counter-charter to make him step aside (a `no-battle` fork, like Tomo's seal-four).
  3. Return the rubbings to Ossa.
- **Recognition beat:** stone 3 bears **two** seals — the Company mark and an older personal seal.
  Ossa: *"This older cut... I have seen it on exactly one charter. The founder's own hand. And it is
  the same stroke as the way you just signed my log. Funny thing, the desert."* She deflects; the
  player learns nothing certain.
- **Gates:** open on arrival (`defeated_gaviota_leader`); stage gates `seal_stone_1..3`,
  `not_tag boundary_stones_done`.
- **Rewards:** `economy/payout {amount:600}`, a **Hard Stone** + **Smooth/Soft Sand** (loot
  `npc_gift/kalahar_ground`), story tag `kalahar_claim_filed`. Sets `boundary_stones_done`.
- **Payoff hook:** `kalahar_claim_filed` can grant a **bonus CD tip** later when farm_5 is liberated
  (a one-line callback: *"Your stones held up in filing. The Reach owes you a round."*).

### Q2 — "Dry Season" (Water Rights) — villain raid, on-theme combat
- **Giver:** Well-Keeper Marisol (new; stands by a dry well). Civilian, oblivious to the CEO — she
  only knows the thirst.
- **Hook:** Kalahar's wells died overnight. The Company runs an **unlicensed pump crew at the Oasis**
  (SW, wild zone) diverting the aquifer north to water the seized granary — filed, of course, as
  *"agricultural yield optimization."* (Deliberate callback to the Takehara falls-redirection scam.)
- **Steps:**
  1. Travel to the Oasis (1730,66,4265) — a genuine wild-zone trek (`mobsSpawn:true`, Nuzlocke live).
  2. Defeat the crew: **Yield Officer** (`sq_pump_officer`, SINGLES, lv 50–51) then **Site Foreman**
     (`sq_pump_foreman`, SINGLES, lv 51), the second gated on the first.
  3. Interact the **pump-manifold prop** to shut the valve (`oasis_pump_off`).
- **Recognition beat (mid):** the Foreman is *alarmed*, not reverent — *"You are on no manifest. That
  face — corporate memo said reroute AROUND you, not through you."* Stands down after the loss.
- **Gates:** open on arrival; `defeated_sq_pump_officer` → `defeated_sq_pump_foreman` → prop
  `oasis_pump_off` → return to Marisol (`not_tag dry_season_done`).
- **Rewards:** `economy/payout {amount:750}`, **Fresh Water ×8** + `super_potion ×2`, story tag
  `oasis_restored`. **Decision:** optionally have the shut valve fire a small liberation-flavored
  `cd_instability` clawback (−3) to make "starving the field's water" mechanically felt (see §9).

### Q3 — "The Grain-Buyer Caravan" (the AMBUSH beat — MARQUEE) — emergent, ≥4 fields
- **Trigger:** latent. When `fields_liberated ≥ 4`, `wheat_trader/tick.mcfunction` sets
  `wheat_trader_hostile`; the shipped `wheat_trader_2` body at Kalahar flips into its `hostile` dialog
  (`dialog/wheat_trader.json`, priority 30) and forces a fight. **No new poller needed.**
- **Upgrade (this is the design work):** the shipped `wheat_trader_ambush` team is lv **38–39** —
  trivial against a lv-50 party. Replace it here with a **Kalahar-scaled caravan DOUBLE**:
  a second Grain Buyer joins `wheat_trader_2` on the Old Caravan Road, two buyers vs the player,
  `GEN_9_DOUBLES`, farm-beast theme (the commodity currency made flesh):

  | Buyer | Team (species @ lvl) |
  |-------|----------------------|
  | Grain Buyer (lead) | **Bouffalant** 50 / **Miltank** 50 |
  | Grain Factor (partner) | **Mudsdale** 51 (stamina, draft-beast) / **Tauros** 50 |

  Ace 51 (a hair under the 52 leader — a forced villain fight, tense but fair; it's optional/emergent).
- **Recognition (mid → the loud one):** the shipped hostile lines already name it — *"I know that
  face. Verified tru§kvalue — you are the one the memos warned us about. The founder. You are supposed
  to be filed."* First **open founder-naming in the field**; framed as memo-paranoia, not narration.
- **Gates:** `wheat_trader_hostile` (auto from ≥4 fields). Player may **Stand and fight** or **Stand
  down** (existing buttons). On win the body does **not** despawn (`despawn_on_win:false`) and reverts
  to a beaten line.
- **Rewards:** flat TBCS `onwin` prize + a field-war tag `kalahar_ambush_cleared` (HUD flavor). No CD
  skew (prizes stay flat-literal per the economy rule).
- **Split the shared tag (decision):** today both `wheat_trader_1` (Hua Zhan) and `wheat_trader_2`
  (Kalahar) share `trainer:wheat_trader_ambush` **and** `defeat_tag:defeated_wheat_trader_ambush` —
  beating either closes both. For Kalahar to be its own beat, give it
  `trainer:wheat_trader_ambush_kalahar` + `defeat_tag:defeated_wheat_ambush_kalahar` (see §6/§9).

---

## 6. Trainers & teams needed

**RCT team files — `src/main/resources/data/rctmod/trainers/` (create/retune):**

| File | Status | Action |
|------|--------|--------|
| `kalahar_leader.json` | exists (3 mons, ace 52 ✅) | **expand to 6** per §4, keep ace 52 |
| `kalahar_trainer_1.json`, `kalahar_trainer_2.json` | exist | optional: bump to 46–48 |
| `kalahar_trainer_3.json`, `kalahar_trainer_4.json` | **MISSING in rctmod** (only embedded in the registry JSON) | **create** RCT-format teams (Juno; Vince) if the TBCS path needs them |
| `kalahar_jr_apprentice.json` | **MISSING in rctmod** | **create** (Sandslash/Marowak 48) |
| `kalahar_apprentice.json` | exists (double) | keep |
| `wheat_trader_ambush_kalahar.json` | **new** | GEN_9_DOUBLES caravan (Bouffalant/Miltank/Mudsdale/Tauros 50–51) |
| `sq_pump_officer.json`, `sq_pump_foreman.json` | **new** | Dry Season villain fights (Ground/Normal desert-crew mons, lv 50–51) |
| `sq_boundary_surveyor.json` | **new (optional)** | Boundary Stones stone-3 guard (lv 50–51) |

Also add matching `rctmod/mobs/trainers/single/` (and `groups/` where a shared spawn class fits)
entries for each new id, mirroring the existing `kalahar_leader`/`kalahar_apprentice` single defs.

**Registry / gym config — `src/main/resources/data/cobblemon_initiative/trainers/`:**
- `gyms/kalahar_reach.json`: reconcile the embedded leader team with the RCT team (pick ace 52; either
  make it 6 mons or leave the RCT team authoritative and trim the embed — see §9).
- `side_quests/act1.json`: register the combat side-quest trainers (`sq_pump_officer`,
  `sq_pump_foreman`, optional `sq_boundary_surveyor`) following the existing `sq_*` entry shape
  (id/displayName/group `side_quests_act1`/prerequisites/rewards/battleFormat/ai/bag/team).

**Battle formats & levels vs the ladder (entry cap 50 → unlock 56):** interior 44–51 SINGLES + one
DOUBLES (Terra); leader SINGLES ace 52; side-quest villains 50–51 SINGLES; ambush DOUBLES ace 51.
All at or below entry-cap+2 except the ace — consistent with the brutal-Nuzlocke rule.

---

## 7. Economy & rewards

- **`cd_instability` at Kalahar ≈ 48.** `economy/payout {amount:N}` applies the haircut
  `rate = 100 − min(idx/4, 25)`; at idx 48 that's `min(12,25)=12` → **88% of face**. Quest CD figures
  below are face values; players net ~88% here (and better as they liberate fields, which lower idx).
- **Leader:** 3700 CD prize (flat TBCS onwin) + `shop badge_6` tier step + `frag_6` + `+8` instability.
- **Interior ladder:** potions / super potions / hyper potions (as configured) — Nuzlocke consumable
  economy, not CD.
- **Side quests:** Q1 `payout 600` + Hard Stone/Soft Sand; Q2 `payout 750` + Fresh Water/super potions;
  Q3 flat onwin prize (no skew). Keep totals in the ~600–800 CD band that other Act-1 side quests use
  (cf. Tomo's `payout 350`), scaled up slightly for the higher cap tier.
- **CD sinks nearby:** the Poké-Mart (`shop badge_6` catalog) and the **Crossroads Granary** company
  store (wheat trades) are the money drains; the Granary trades *worsen* as fields are liberated
  (relief tiers), reinforcing "claw the currency back."
- **Liberation tie:** liberating **farm_5 (Crossroads Granary)** is a `wheat_war_farms` beat, but its
  guard fight and the `free_field {field:farm_5}` reward live at the field, not in town. Kalahar's job
  is to *set up* that liberation narratively (Ossa's claim, Marisol's water) and to *react* to it
  (the ambush at ≥4 fields, and Ossa's `kalahar_claim_filed` bonus when farm_5 falls).

---

## 8. Implementation notes / FUTURE-ME HOOKS

**Files to create (exact paths):**
- Town cast (new folder for parity with `takehara/`, `hua_zhan/`):
  `dialog-src/characters/kalahar/warden_ossa.json`, `wellkeeper_marisol.json`, `kalahar_nurse.json`,
  `kalahar_martkeeper.json` (+ optional flavor civilians). *Note:* the two existing bodies live in
  `dialog-src/characters/gym/kalahar_leader.json` / `kalahar_guide.json` — you may move them into
  `kalahar/` for tidiness (the compiler globs `characters/**`, folder is cosmetic).
- Props (placement + `dialog_inline`, `role:"civilian"`, copy `survey_wagon.json`):
  three boundary dig-sites + the Oasis pump-manifold. Placement (no uuid) = spawns once per world via
  the generated proximity function.
- Dialog trees: inline in the character files (like `sq_beekeeper_tomo`) OR standalone
  `dialog-src/dialog/<id>.json`. Ossa's chain should reuse the `open_dialog` label pattern
  (`seal_stone_1..3`) from Tomo.
- RCT teams + mob defs: per §6 under `rctmod/trainers/` and `rctmod/mobs/trainers/single|groups/`.
- Loot: `src/main/resources/data/cobblemon_initiative/loot_tables/npc_gift/kalahar_ground.json`
  (Hard Stone + Soft/Smooth Sand) for Q1.
- Side-quest registry: append to `trainers/side_quests/act1.json`.

**Pipeline (run in order after authoring):**
`scripts/content_compile` → `scripts/generate_granary_tiers` → `scripts/update_preset_index` →
`scripts/generate_npc_function` → `gradle build`. `content_compile` regenerates `band_tags.mcfunction`
with the new tags (`boundary_stones_done`, `seal_stone_1..3`, `oasis_pump_off`, `oasis_restored`,
`kalahar_claim_filed`, `dry_season_done`, `kalahar_ambush_cleared`, `defeated_sq_pump_officer`,
`defeated_sq_pump_foreman`, `defeated_wheat_ambush_kalahar`) and their `no_*` inverses automatically.

**Patterns to copy:**
- Multi-stage lore quest → `dialog-src/characters/takehara/sq_beekeeper_tomo.json` (seal chain,
  `open_dialog` stages, `on_win` `@1` winner hooks, `battle`-or-cite fork).
- Placement prop with gated reveal → `dialog-src/characters/villain/survey_wagon.json`.
- Villain double → `dialog-src/characters/takehara/agent_yield_lead.json` (GEN_9_DOUBLES carried on
  one body, `despawn_on_win`, custom skin uuid).
- Gym leader tree / gates → `dialog-src/dialog/gym_leader_hua_zhan.json` (`fields_liberated` op-gates,
  `defeated` gates, `{do:battle}`).

**Gotchas (from `ENGINE_FINDINGS` + verified here):**
- **onwin tokens are winners-first** (key `1` = player won, `2` = loser).
- **Easy NPC `PLAYER_TAG` only** for dialog conditions; NOT_EQUALS is ignored → every not-gate rides
  `EQUALS no_<X>`. Numeric gates (`fields_liberated`, badges, `cd_instability`) must compile to
  band-tags (`fields_liberated_gte_4` already exists — the ambush uses it).
- **Macro-delivered text** (memory frags, `economy/*`, `onwin`) must contain **no double-quotes** and
  avoid apostrophes — keep Ossa's/Marisol's `announce`/`payout`-adjacent strings clean.
- **`wheat_trader_2` is builder-placed (uuid)** — do NOT give it a `placement` block; the ambush
  partner (the caravan's second buyer) is a *new* body that CAN take `placement` (spawns once).
- **Shared ambush tag** (`defeated_wheat_trader_ambush`) currently couples Hua Zhan + Kalahar — split
  it (§5/§9) or the Kalahar ambush is pre-cleared for any player who fought the Hua Zhan trader.
- **Surface Y is ≈126**, not 64 — ground PROPOSED town coords at Y126 (Oasis/granary props sit lower,
  Y≈64–66).

---

## 9. Dependencies & open questions

**Depends on (other area keys):**
- **`wheat_war_farms`** (STRONG) — owns the `fields_liberated` counter, farm_5 (Crossroads Granary)
  liberation guard, and `free_field {field:farm_5}`. The ambush trigger (≥4 fields) and Ossa's
  `kalahar_claim_filed` payoff both key off it.
- **`gym_system_pvp_doubles`** — shared ladder mechanics, TBCS onwin, `shop badge_N` tiers, the
  DOUBLES convention (Terra + the ambush caravan).
- **`mainline_spine`** — memory fragments (`frag_6`), `cd_instability` schedule, recognition tiers,
  Act 1/2 boundary.
- **`gaviota_port`** (prev gym) — `defeated_gaviota_leader` gates the entire Kalahar ladder; entry
  cap 50 comes from Gaviota.
- **`cyber_city`** (next gym) — Kalahar's `badge_ground` → cap 56 = Cyber's entry cap (already
  asserted in `08_cyber_city.md`).
- **`shrines_audit`** — owns Ground Shrine placement/coords (Terran + cultists already have teams).
- **`company_hq`** — the ambush and the "starve the water/fields" pressure feed the DJ raid's
  4-field gate; the recognition escalation seeds Act 2.

**Decisions the showrunner must make:**
1. **Leader ace level:** 52 (rule + shipped RCT team) vs 54 (registry duplicate) vs **58** (brief).
   Recommend **52** and collapse the two team definitions to one source of truth.
2. **Split the wheat ambush** into `wheat_trader_ambush_kalahar` / `defeated_wheat_ambush_kalahar`
   (recommended) so Kalahar is a distinct, properly-levelled beat — vs leaving the shared lv-38 team
   (which is pre-clearable and trivial). And confirm: **caravan DOUBLE** vs a straight singles rematch.
3. **Open founder-naming pre-Act-3:** the shipped `wheat_trader` `hostile` line already says "the
   founder." Confirm this reads as enemy memo-paranoia (intended) and not a narration reveal — it's the
   loudest recognition line before Act 3.
4. **Q2 mechanical tie-in:** does shutting the Oasis pump actually move `cd_instability` (−3) / pressure
   farm_5, or is it flavor only?
5. **Service-NPC parity:** confirm Kalahar needs placed Nurse + Martkeeper bodies (Takehara/Hua Zhan
   have them), or whether the map build already provides those stations.
6. **Interior-trainer retune:** bump the four lv-44 openers to 46–48, or leave the softer ramp?
