# 05 — Deepcore City (Gym 4, Fighting, cap → 44)

> **Area key:** `deepcore_city`
> **Owns:** the fourth gym town — the Fighting gym interior PvP ladder + double + Leader Bruno
> (Iron Badge), the Deepcore town service NPCs and civilian flavour, 3 side quests, and the
> **first town where Act-2 economic dread and wheat-trader recognition bite** (`cd_instability`
> hits 32; the "deep core" corporate pun made literal). **Does NOT own:** the `cd_instability`
> curve math, the memory-fragment engine, band-tag derivation, shop-tier catalogs, field
> liberation, or the HQ plot — those are `mainline_spine` / `wheat_war_farms` / `company_hq`.
> This doc owns what lives inside the Deepcore zone and how it hangs on those rails.

**Build status: skeleton exists, needs a real body.** The gym ladder, Bruno, and a guide are
already registered (`trainers/gyms/deepcore_city.json`, `dialog-src/characters/gym/deepcore_{leader,guide}.json`,
`dialog/gym_leader_deepcore.json`, and rct team files for `deepcore_leader` / `_trainer_1` / `_trainer_2` /
`_apprentice`). This section (a) documents that skeleton as canon, (b) **retunes it to the balance
rule** (two team sources currently disagree — see §4/§8), and (c) adds the town, the 3 side quests,
and the recognition weave the brief asks for.

---

## 1. Concept & fantasy

**One-line pitch:** *A vertical mining city where the gym is a knock-down brawl at the pit-head and
the villain plot is finally literal — the nether-star vein that backs the entire CobbleDollar is
being quietly mined out from under everyone's feet, "re-verified" by grey suits who now flinch when
they see your face.*

Deepcore is the run's **first "the horror is real" town.** Gyms 1-3 the Company was a rumour and a
short payout. Here the money visibly wobbles (idx 32, the Act-2 *reassurance* register takes over the
propaganda), the wheat traders start eyeing the player, and — if the player has been liberating
fields — a **grain buyer recognises them mid-trade.** The Fighting gym is the tonal counterweight:
Bruno is the one honest institution in a town of accountants, and his gym is pure "stay standing,"
no gimmicks. Corporate-dread comedy played straight against a genuine punch to the face.

The **"deep core" pun** carries the whole town: it is a deep-core *mine*, and the Company keeps
talking about its "deep core values" and a "deep restructuring." The literal core (a nether-star ore
vein — the physical backing of the currency) is being hollowed while the memos praise "core values."

Marquee stream moments:
- **The Iron Ladder** — an optional 3-in-a-row knockout gauntlet down a played-out shaft, **no heals
  between rounds** (brutal-Nuzlocke spice the chat will scream about).
- **The re-verified ledger** — the player reads a scrubbing artifact: the mine's nether-star count
  quietly halved on paper, the old auditor's signature scratched out and "verified" under a new name.
  The plot the audience has been theorising about, in black and white, at the exact halfway mark.
- **"A face from the old company"** — a wheat trader at the mine commissary stops mid-pitch, goes
  wary, keeps trading anyway ("coin is coin, for now"). The recognition arc turns interactive.
- **Bruno's after-line** (already written): *"The grey suits asked about you before you ever got
  here. Said you were important once."* The gym leader unknowingly hands the audience another clue.

---

## 2. Narrative role

| Field | Value | Source |
|---|---|---|
| Villain-structure act | **Act 1 (Infiltration)** — gyms 1-7 | LORE §4; Bruno + villain chars `act:"1"` |
| Economy *voice* register | **Act 2 (slipping / reassurance)** — "prices are adjusting" | LORE §3; `economy.json` `reassurance` |
| `cd_instability` at exit | **32** (24 after Mystic → `gym_destabilize` +8) | `economy/gym_destabilize` (+8); already fired by Bruno's reward |
| Memory fragment | **frag_4** — *"I am signing something. My hand knows the motion better than I do. The pen is heavy. The room is very quiet and very pleased."* | `registers/memory_fragments.json`; fired by Bruno reward `memory/gym/frag_4` |
| Level cap | enter at **37** (Mystic unlock); Bruno defeat unlocks **44** | CLAUDE.md ladder |
| World recognition of Company NPCs | **MID** — `recognition:"mid"` compiles to `badges_gte_3`, and the player holds 3 badges on arrival, so grunts/management show alarm ("you are supposed to be filed") | `scripts/content_compile` `RECOG_BAND = {mid:(badges,gte,3)}` |
| Wheat-trader recognition | **SUSPICIOUS** if `fields_liberated >= 2` (`wheat_trader_suspicious`); AMBUSH (`wheat_trader_hostile`) only at `>= 4` (payoff lands at Kalahar) | `function/wheat_trader/tick.mcfunction` |

Canon ties this town carries:
- **frag_4 is the "signing" dream** — the protagonist's hand remembers signing a charter it does not
  consciously know. Deepcore is where the *body* starts remembering before the mind does. This rhymes
  with frag_7 ("You signed this charter") three gyms later.
- **The scrubbing made visible** (LORE §4.1, §9 "DO litter the world with scrubbing artifacts"): the
  re-verified ledger is a first-person scrubbing artifact — old signature struck, new name "verified."
- **The reserve plot literalised** (LORE §3): CobbleDollars are nether-star-backed; the Deepcore mine
  is (PROPOSED) a physical nether-star vein. Hollowing it out on paper *is* the destabilization.
- Bruno is **early recognition_tier** (he is not Company; he only relays that the suits asked after
  the player) — civilians/leaders never recognise the founder, only the propaganda decay. Correct.

---

## 3. Layout & placements

Deepcore City zone (install.json): polygon **x 907–1272, z 3075–3342**, `centerY 64`, colour `#FF7043`,
label *"Gym 4 — Fighting Type"*. Spine centroid `1108 ~64 3205`. **Note the gym cluster sits at y≈129,
~65 blocks above the zone `centerY` of 64** — the mine/city is on a raised pit-head shelf; treat y≈129
as the walkable surface near the gym and confirm with the builder for any town-square placement
(Open Question 4).

### Builder-confirmed coords (already in `trainers/gyms/deepcore_city.json`)

| Body | id | Coord | Role |
|---|---|---|---|
| Black Belt Ryu | `deepcore_trainer_1` | `1033 129 3183` | interior ladder (single) |
| Battle Girl Mika | `deepcore_trainer_2` | `1036 129 3189` | interior ladder (single) |
| Martial Artist Kenji | `deepcore_trainer_3` | `1030 129 3186` | interior ladder (single) |
| Capoeira Rio | `deepcore_trainer_4` | `1037 129 3192` | interior ladder (single) |
| Jr. Apprentice Striker | `deepcore_jr_apprentice` | `1038 129 3186` | interior ladder (single) |
| Apprentice Ken | `deepcore_apprentice` | `1039 129 3183` | **the DOUBLE** (`GEN_9_DOUBLES`) |
| Leader Bruno | `deepcore_leader` | `1045 129 3186` | Leader (single) — Iron Badge |

### PROPOSED placements (town + quests + recognition weave — needs builder confirm)

All coords below are **PROPOSED**, sited inside the zone polygon at the y≈129 pit-head band unless
noted. Grounded anchors: the gym cluster (above) and the zone centroid `1108 3205`.

| Body | Suggested id | Recipe / kind | Suggested coord | Purpose |
|---|---|---|---|---|
| Pokémon Center Nurse | `deepcore_nurse` | `nurse` | `~1092 129 3208` PROPOSED | heals; copy `hz_nurse` |
| Poké Mart keeper | `deepcore_martkeeper` | `martkeeper` | `~1100 129 3215` PROPOSED | badge_4 shop; copy `hz_martkeeper` |
| Deepcore Gym Guide | `deepcore_guide` (EXISTS) | `gym_guide` | `~1050 129 3181` PROPOSED | already authored; **needs a `placement` block added** |
| Pit Foreman Kang | `deepcore_foreman_kang` | `civilian` | `~1120 129 3300` PROPOSED | SQ1 giver (retired union foreman) |
| Sparring-pit barker | `deepcore_ladder_barker` | `civilian` | `~1060 129 3200` PROPOSED | SQ2 giver (Iron Ladder) |
| Grain Buyer (Deepcore) | `wheat_trader_deepcore` | `grain_buyer` | `~1180 129 3260` PROPOSED (commissary/mine gate) | SQ3 recognition weave; reuses `dialog:wheat_trader` |
| Company Restructuring Officer | `sq_deepcore_assessor` | `villain_grunt` | `~1150 129 3282` PROPOSED (Company field office) | SQ1 battle; MID recognition |
| **Prop:** re-verified ledger board | `deepcore_ledger_board` | interact-only board (like `rezoning_notice_board`) | at the field office `~1152 129 3284` PROPOSED | SQ1 scrubbing artifact |
| **Prop:** clean rectangle where a portrait hung | scenery only (no NPC) | — | field office wall | scrubbing artifact (LORE §9) |
| Civilian miners ×2-3 | `deepcore_miner_*` | `civilian` | around the pit-head | flavour: "deep core values" gag, instability rumours |

Do **not** invent terrain — every PROPOSED body is a standing NPC/prop inside the existing zone.
If the pit-head interior does not exist as walkable space, fall back to surface-level placement near
the zone centroid and flag for the builder.

---

## 4. Gym / core structure — the Fighting ladder

The gym is a **linear-with-a-fork PvP ladder** the player clears to reach Bruno. It already exists in
`trainers/gyms/deepcore_city.json`; the design below keeps the shape and **retunes levels to the
balance rule** (entry cap 37, so the ladder climbs 32→39 and Bruno is the hard peak).

### Ladder & gate wiring

| Order | Battle (id) | Format | Gate (`prerequisites` = defeated-dependency) | Target levels |
|---|---|---|---|---|
| enter | — | — | `defeated_mystic_leader` (3rd badge) | player capped at 37 |
| 1 | `deepcore_trainer_1` Ryu | Single | `mystic_leader` | 32–33 |
| 1 | `deepcore_trainer_2` Mika | Single | `mystic_leader` | 32–33 |
| 1 | `deepcore_trainer_3` Kenji | Single | `mystic_leader` | 33 |
| 1 | `deepcore_trainer_4` Rio | Single | `mystic_leader` | 33 |
| 2 | `deepcore_jr_apprentice` Striker | Single | `mystic_leader` + `deepcore_trainer_1` + `deepcore_trainer_2` (fork converges) | 34–35 |
| 3 | `deepcore_apprentice` Ken | **DOUBLE** (`GEN_9_DOUBLES`) | `deepcore_jr_apprentice` | 35–37 |
| 4 | `deepcore_leader` **Bruno** | Single | `deepcore_apprentice` | 36–39 (ace 39) |

- **The double is the Apprentice (Ken)** — mirrors Hua Zhan's ladder (its apprentice is the
  `GEN_9_DOUBLES` battle too). This is the town's format variety beat.
- The interior trainers are **RCT-spawned trainer mobs**, gated by the registry `prerequisites` chain
  (they are not fightable until their defeated-deps are met). Bruno is an **Easy NPC** who runs his
  battle through the dialog button → TBCS (`dialog:gym_leader_deepcore`, action `{do:"battle"}`).

### Leader team — Bruno (ace **39**, Fighting mono)

Retune `data/rctmod/trainers/deepcore_leader.json` to a 6-mon final-battle team on the **entry-cap-37
+ 2 = 39** scale (matches all four built gyms: Takehara ace 17, Hua Zhan 24, Mystic 32, and the
current shipped Deepcore rct team's machamp 39). Sketch:

| Slot | Species | Lvl | Ability | Item | Role |
|---|---|---|---|---|---|
| Lead | Medicham | 37 | Pure Power | — | Fake Out + High Jump Kick, fast opener |
| 2 | Breloom | 37 | Technician | Focus Sash | Spore + Mach Punch — the Nuzlocke status trap |
| 3 | Hariyama | 38 | Thick Fat | Assault Vest | bulky special sponge |
| 4 | Gurdurr | 38 | Guts | Eviolite | pivot wall, Drain Punch |
| 5 | Lucario | 38 | Inner Focus | Life Orb | Nasty Plot / Aura Sphere break |
| **Ace** | **Machamp** | **39** | **No Guard** | Assault Vest | **Dynamic Punch (100% confuse) + Knock Off + Bullet Punch + Ice Punch** |

Bag: 3× Full Restore (already on the leader). `achievementOnDefeat: badge_fighting`. Rewards
(already wired, keep): `memory/gym/frag_4`, `cobblemon-initiative shop badge_4`,
`economy/gym_destabilize` (idx → 32). Prize 2800 CD (already set).

> **⚠ Two conflicting team sources today (must reconcile — see §8 gotchas):** the **authoritative**
> battle team is `data/rctmod/trainers/deepcore_leader.json` (currently 3 mons, ace machamp **39** —
> on-curve). The `team` block **inside** `trainers/gyms/deepcore_city.json` is a *second*, higher
> team (conkeldurr **43** ace) and its `deepcore_apprentice` double is **L39–40 — higher than that
> file's own leader is meaningfully hard, and higher than the rct leader entirely** (an apprentice
> that out-levels the leader). Pick the rct file as canon, expand it to the 6 above, and drop/align
> the gym-config team block. Ace stays 39, not 43, not 46.

---

## 5. Quests & side quests

Three quests: one meaty plot quest (the reserve reveal), one fun gauntlet, one recognition weave.
All Act-1, on the mining/corporate-dread theme. Register the battle trainers in
`trainers/side_quests/act1.json` (same file/pattern as Hua Zhan's `sq_hz_analyst`).

### SQ1 — "Deep Restructuring" (the marquee — the reserve reveal)
- **Giver:** Pit Foreman Kang (retired union foreman; `civilian`).
- **Hook:** the Company "deep-restructured" the mine — the same mine whose nether-star vein backs the
  CobbleDollar. Kang smells fraud in the new ore ledger but cannot read the "re-verified" columns.
- **Steps & gates:**
  1. Talk to Kang → `tag deepcore_restructure_started`.
  2. Read the **re-verified ledger board** at the Company field office (prop, like
     `rezoning_notice_board`) → `tag deepcore_ledger_seen`. Player sees the nether-star reserve count
     quietly halved and the old auditor's signature struck out, "verified" under a new name.
  3. The **Company Restructuring Officer** (`sq_deepcore_assessor`) blocks the ledger vault. Dialog
     reuses `dialog:grunt_recognition` (**MID tier already fires** — `badges_gte_3` — *"You are
     supposed to be a closed file"*). Battle him (single, ~L35, `loss_fee` decline option like
     `villain_grunt_6`). Win → `defeated_sq_deepcore_assessor` + drops a "Re-verified Reserve Page"
     gift + `tag deepcore_ledger_taken`.
  4. Return to Kang → he confirms the vein is being hollowed on paper. `tag deepcore_restructure_done`.
- **Rewards:** `function economy/payout {amount:900}` + a **Black Belt** (Fighting nod / held item)
  + the scrubbing-artifact story flag. No `cd_instability` change (the mine is not a *field* — the
  only idx lever is field liberation; this quest **reveals** the plot, it does not move the meter).
- **Resolution:** Kang cannot fix it — but the player now *knows* the reserve is being emptied. A
  felt, black-and-white confirmation of the whole plot at the run's midpoint. This is the strongest
  scrubbing artifact before frag_7.

### SQ2 — "The Iron Ladder" (the fun gauntlet)
- **Giver:** Sparring-pit barker at a played-out shaft (`civilian`).
- **Hook:** Bruno's students run an old-school knockout gauntlet — 3 back-to-back fights, **no heals
  between rounds** — for Iron Ladder bragging rights.
- **Steps & gates:** enter (`tag iron_ladder_active`; optional entry gate `defeated_deepcore_trainer_2`
  so the player has shown some skill) → `sq_ladder_1` → (gate `defeated_sq_ladder_1`) `sq_ladder_2` →
  (gate `defeated_sq_ladder_2`) `sq_ladder_3` → clear all three → claim.
- **Rewards:** escalating; final = **Expert Belt** (or Muscle Band) + `economy/payout {amount:700}`,
  one-time (`tag iron_ladder_cleared`). Ladder mons ~L34–37 (under cap), Fighting/Rock/Steel flavour.
- **Resolution:** barker grants the bragging tag — a clean, repeatable-looking-but-one-time stream
  set-piece with real Nuzlocke tension (no mid-gauntlet healing).

### SQ3 — "The Better Rate" (the mandated recognition weave)
- **Giver:** Grain Buyer at the mine commissary (`grain_buyer`, reuses `dialog:wheat_trader`).
- **Hook:** the Company's alternative currency has reached the mine — a grain buyer offering to pay
  miners' wages in wheat "before the paper money learns it is worthless."
- **The recognition beat (brief's required weave):** the shipped `dialog:wheat_trader` tree already
  has the three tiers this needs — **no new dialog required, just a placed body**:
  - `fields_liberated < 2` → **default** pitch (glossy alt-currency sell; sets `heard_wheat_pitch`).
  - `fields_liberated >= 2` → **suspicious** (`wheat_trader_suspicious`): *"Something about you sits
    wrong... A face from the old company, walking the routes. Probably nothing. Probably. Wares are
    open."* — still trades (`trade_wheat_trader`), but wary. **This is the Deepcore recognition beat.**
  - `fields_liberated >= 4` → **hostile** ambush (`wheat_trader_hostile`, battle `wheat_trader_ambush`).
    Won't normally trigger at gym 4 — this is the **forward hook** that pays off at Kalahar (gym 6).
- **Rewards:** the beat itself (recognition + optional wheat-rate trade). Declining/leaving is free;
  the trade snippet is the interaction. No forced battle unless `hostile`.
- **Resolution:** seeds the trader ambush arc; the same `>= 2` fields that make the trader wary also
  trip the **shop relief tier** (§7) — so the town where the trader recognises you is the town where
  your clawed-back fields visibly ease CobbleDollar prices. One town, both sides of the tug-of-war.

---

## 6. Trainers & teams needed

### `data/rctmod/trainers/` (team files)
| File | Status | Action |
|---|---|---|
| `deepcore_leader.json` | exists (3 mons, ace 39) | **expand to the 6-mon team in §4**, keep ace 39 |
| `deepcore_trainer_1.json`, `deepcore_trainer_2.json` | exist | keep (retune to 32–33 if needed) |
| `deepcore_apprentice.json` | exists (4 mons, L39–40) | **retune down to 35–37** (must sit below Bruno) |
| `deepcore_trainer_3.json`, `deepcore_trainer_4.json`, `deepcore_jr_apprentice.json` | **missing** (only in gym-config team blocks) | **create** rct team files (33 / 33 / 34–35) |
| `sq_deepcore_assessor.json` | **create** | Company Restructuring Officer, single, ~L35 (e.g. Watchog/Bisharp-lite/Mightyena mgmt flavour) |
| `sq_ladder_1.json` / `_2.json` / `_3.json` | **create** | Iron Ladder gauntlet, ~L34/35/37, Fighting/Rock/Steel |

### `data/rctmod/mobs/trainers/single|groups/` (spawn/series)
- Create spawn defs for `sq_deepcore_assessor`, `sq_ladder_1..3`, and the missing interior
  `deepcore_trainer_3/_4/_jr_apprentice` (copy the existing `single/deepcore_leader.json` /
  `groups/deepcore_trainer.json` shape).

### `data/cobblemon_initiative/trainers/`
- `gyms/deepcore_city.json` (exists) — retune ladder levels to the 32→39 band; **fix the
  apprentice-out-levels-leader inversion**; reconcile/drop the in-file leader `team` block against the
  rct file. Keep `prerequisites`, `battleFormat`, rewards, coordinates.
- `side_quests/act1.json` (exists) — **append** `sq_deepcore_assessor`, `sq_ladder_1`, `sq_ladder_2`,
  `sq_ladder_3` (copy the `sq_hz_analyst` entry shape: category `side_quest`, group `side_quests_act1`,
  coordinates, prerequisites, rewards, `team`).

### Battle formats vs cap ladder (entry cap 37)
| Battle | Format | Ace lvl |
|---|---|---|
| interior singles ×4 | `GEN_9_SINGLES` | 33 |
| jr apprentice | `GEN_9_SINGLES` | 35 |
| apprentice (Ken) | `GEN_9_DOUBLES` | 37 |
| **Leader Bruno** | `GEN_9_SINGLES` | **39** |
| `sq_deepcore_assessor` | `GEN_9_SINGLES` | 35 |
| `sq_ladder_1/2/3` | `GEN_9_SINGLES` | 34 / 35 / 37 |

---

## 7. Economy & rewards

| Lever | Value | Mechanism |
|---|---|---|
| Bruno prize | 2800 CD | gym config `prize` (already set) |
| Bruno destabilize | idx 24 → **32** | reward `economy/gym_destabilize` (+8, already wired) |
| Bruno shop step | catalog `badge_4` | reward `cobblemon-initiative shop badge_4` (already wired) |
| SQ1 payout | 900 CD + Black Belt | `function economy/payout {amount:900}` |
| SQ2 payout | 700 CD + Expert Belt | `economy/payout {amount:700}` (one-time) |
| Interior/ladder item rewards | potions → super/hyper potions | gym-config `rewards` (climb with the ladder) |
| **CD sinks** | mart (badge_4 tier), heals, **decline fees** on the assessor (`loss_fee`), wheat trade | shop tiers + `loss_fee` pattern (`villain_grunt_6`) |
| **Liberation tie-in** | at `fields_liberated >= 2`: shop relief tier `badge_4_relief1` (−instability on prices) **and** `wheat_trader_suspicious` | `ShopTierManager` resolves relief live; `shop refresh` on liberation |

Payout note: at idx 32 the CobbleDollar is in the *reassurance* band — payouts feel slightly short
and the per-payout rate line narrates it. Instability rumour tier `>= 16` is active (`economy.json`),
so Deepcore civilians can carry the "my payout came up short again" lines. The `>= 40` tier is **not**
yet active (that lands around Gaviota/Kalahar).

---

## 8. Implementation notes / FUTURE-ME HOOKS

**Copy Hua Zhan City** — it is the nearest fully-built analog (interior ladder + `GEN_9_DOUBLES`
apprentice + a placed Company `sq_hz_analyst` + civilian merchants with `dialog_inline`). Concrete
build order:

1. **Retune the leader team** in `src/main/resources/data/rctmod/trainers/deepcore_leader.json` to the
   6-mon ace-39 team (§4). Do the same down-tune on `deepcore_apprentice.json` (→ 35–37).
2. **Create the missing rct team files** `deepcore_trainer_3.json`, `deepcore_trainer_4.json`,
   `deepcore_jr_apprentice.json` + their `mobs/trainers/single|groups` spawn defs.
3. **Reconcile `trainers/gyms/deepcore_city.json`** — fix the ladder level band (32→39), fix the
   apprentice/leader inversion, and settle the two-team-source conflict (make the rct file canonical).
4. **Add a `placement` block** to `dialog-src/characters/gym/deepcore_leader.json`
   (`{"x":1045,"y":129,"z":3186}`, from the gym config) and `deepcore_guide.json` (PROPOSED
   `~1050 129 3181`) — both currently have **no `placement` and no `uuid`, so nothing spawns them
   yet.** A character with `placement` and no `uuid` spawns once per world via the generated proximity
   function (bypasses the builder world).
5. **Author the town + quest characters** under `dialog-src/characters/deepcore/` (new folder):
   `deepcore_nurse.json` (`dialog:hz_nurse`-style), `deepcore_martkeeper.json`,
   `deepcore_foreman_kang.json` + `dialog/deepcore_foreman_kang.json`,
   `deepcore_ladder_barker.json` + dialog, `wheat_trader_deepcore.json` (**reuse** `dialog:wheat_trader`,
   set `trade:{snippet:"trade_wheat_trader",open_label:"shop"}`), `sq_deepcore_assessor.json`
   (**reuse** `dialog:grunt_recognition`, `recognition_tier:"mid"`, battle block +
   `defeat_tag:"defeated_sq_deepcore_assessor"`), `deepcore_ledger_board.json` (interact prop,
   copy `rezoning_notice_board`), and 2-3 `deepcore_miner_*` civilians.
6. **Add the SQ battle trainers** to `trainers/side_quests/act1.json` (see §6).
7. **Run the pipeline in order:** `scripts/content_compile` → `scripts/generate_granary_tiers` →
   `scripts/update_preset_index` → `scripts/generate_npc_function`. This lowers the characters to
   `data/easy_npc/preset/humanoid(_slim)/<id>.npc.snbt`, regenerates band_tags (adds
   `defeated_sq_deepcore_assessor`, `defeated_sq_ladder_*`, and any new story tags + their `no_*`
   inverses), and writes `npc/preset_map.json` + `function/update_npc_presets.mcfunction`.

**GOTCHAS (verified from files — do not relearn):**
- **Ace is 39, not 46.** The assignment header said "ace 46," but the balance rule (entry-cap + 2)
  and **all four built gyms** put Deepcore's ace at `37 + 2 = 39` (the shipped rct team already does).
  46 would be **9 levels over the player's cap** — off-curve. Flagged as Open Question 1; recommend 39.
- **Two team sources disagree today** (§4): rct file ace 39 vs gym-config `team` block ace 43, and the
  gym-config apprentice (L39–40) out-levels its own/the rct leader. The rct file is what TBCS/RCT
  actually fights via Bruno's dialog button — make it canonical, align/drop the config block.
- **`recognition:"mid"` = `badges_gte_3`**, which is already true on arrival (3 badges). So Company
  NPCs here show the **mid** tier automatically — no extra wiring; just set `recognition_tier:"mid"`
  and use `gate:{recognition:"mid"}` entries (the compiler lowers it).
- **Wheat recognition is `fields_liberated`, not badges.** `wheat_trader_suspicious` needs `>= 2`
  fields *liberated* — it will **not** fire for a player who skipped the Wheat War. That is intended
  (the recognition is a reward for engaging the field content), but it means SQ3's marquee beat is
  **conditional** — the default pitch is the fallback. Depends on `wheat_war_farms` actually wiring
  `fields_liberated` past 1 (ENGINE_FINDINGS flags only `farm_1` is live today — see §9).
- **Macro-delivered text has no escaping** — any new `economy/payout`, `on_win`, or fragment-style
  line must contain **no double-quotes** and avoid apostrophes.
- **TBCS onwin is winners-first** — if any Deepcore battle uses a dialog-button TBCS battle with
  `onwin {1:[...],2:[...]}`, key `1` = player won, `@1` = the player.
- **`loss_fee` / decline** — the assessor and any Company grunt can take a CobbleDollars fee to walk
  away (copy `villain_grunt_6`'s `loss_fee`), a clean Act-2-flavour sink.

---

## 9. Dependencies & open questions

### Depends on (other area keys)
- **mainline_spine** — owns `memory_fragment` (frag_4), `cd_instability` / `gym_destabilize`, band-tag
  derivation, `economy/payout`, shop tiers, and the HUD `▶ Deepcore City` → `▶ Gaviota Port` routing.
  Deepcore *writes* the badge (badge_fighting → cap 44) and *consumes* the recognition/relief bands.
- **gym_system_pvp_doubles** — the ladder + `GEN_9_DOUBLES` apprentice conventions, and the rule that
  the leader carries `frag_N` + `gym_destabilize` + `shop badge_N` + `achievementOnDefeat`. Deepcore
  is an instance of that pattern.
- **mystic_marsh** (gym 3) — provides `defeated_mystic_leader`, the entry gate for the whole ladder,
  and the cap-37 the ace is tuned against.
- **gaviota_port** (gym 5) — the hand-off: Bruno's defeat (cap 44) is Gaviota's entry gate.
- **wheat_war_farms** — SQ3's recognition beat and the shop-relief tie-in **both** key on
  `fields_liberated >= 2`; if only `farm_1` is wired (ENGINE_FINDINGS), the suspicious tier is
  unreachable and SQ3 silently falls back to the default pitch. Also the narrative source for SQ1's
  "reserve being hollowed" reveal (Deepcore is where that plot is *shown*; the fields are where it is
  *fought*).
- **kalahar_reach** (gym 6) — receives SQ3's forward hook: the `wheat_trader_hostile` ambush (`>= 4`
  fields) is designed to land there, not here.
- **company_hq** — SQ1's reserve reveal is the thematic on-ramp to the HQ raid; keep the "re-verified
  ledger / nether-star count" language consistent with whatever HQ ships.

### Open questions (showrunner decisions)
1. **Confirm ace = 39** (recommended, on-curve) vs the assignment header's "46" (a deliberate
   9-over-cap difficulty spike). All evidence says 39; need an explicit call to override the header.
2. **Reconcile the two leader team sources** — make `rctmod/trainers/deepcore_leader.json` canonical
   and align/drop the `team` block in `trainers/gyms/deepcore_city.json`? (And fix the apprentice
   out-leveling the leader.)
3. **Is the Deepcore mine literally a nether-star vein** (the physical currency backing)? SQ1's whole
   premise leans on it. LORE says reserves are *recorded in ledgers*; a physical vein is a fun
   literalisation but should get a canon OK (and must not contradict company_hq).
4. **Walkable Y for town NPCs** — the gym cluster is y≈129 while the zone `centerY` is 64. Confirm the
   pit-head surface Y so PROPOSED nurse/mart/quest bodies sit on ground, not floating.
5. **How Fighting-heavy should the recognition be?** Bruno is deliberately *not* Company (early tier);
   the only Company faces in town are the assessor (SQ1) and the wheat trader (SQ3). Is one Company
   battle enough for a gym-4 town, or add a second grunt on the mine approach (e.g. a `pursue`-sight
   Compliance Officer) to raise the Act-2 temperature?
6. **Leader/guide bodies** — add `placement` blocks (spawn fresh) or adopt builder-placed bodies via
   `uuid`? Currently neither is set, so they do not spawn.
