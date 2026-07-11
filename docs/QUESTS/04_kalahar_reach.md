# 04 — Kalahar Reach (Gym 6, Ground) — Quest Build Plan

> **Slug:** `04_kalahar_reach` · **Town:** Kalahar Reach · **Gym 6, Ground, Leader Gaia (`kalahar_leader`)**
> **Band:** entry cap **50** (from Gaviota) → unlocks **56** on Gaia; `frag_6`; `cd_instability → 48`; recognition **mid → late** (enter at 5 badges, mid; some beats read late as fields fall).
> **Companion roadmap:** `docs/roadmap/07_kalahar_reach.md` (atlas + design intent — this doc is the copy-paste-compile authoring layer for the **town/quest** cast; the gym ladder, leader team, Ground Shrine, and the shipped `wheat_trader_2` ambush body already exist and are **not** re-authored here).

This doc adds ONLY new files under `dialog-src/` (town quest cast) + supporting datapack functions + `quest_targets` stages. It does not touch the gym ladder, the leader, the shrine, or the shipped `wheat_trader_2` body.

---

## 1. Overview

Kalahar Reach is the **dust bowl where the wheat war stops being abstract**. It sits between a **dry town** (wells drained overnight) and the seized **Crossroads Granary** (`farm_5`) to the north, with the **Oasis** landmark to the SW whose aquifer the Company is pumping to irrigate that field. Approached from the west via **Route 8, Dunewind Trail**. This is the gym where the monopoly becomes **terrain** — you can see the pump line — and where **recognition curdles into the cover-up**: at `fields_liberated ≥ 4` the grain-buyer caravan stops selling and *names the founder* (the loudest recognition beat before Act 3, framed as memo-paranoia, not narration).

**Band context**
- **Cap:** enter at 50, leave with 56 unlocked. Every side battle here is ≤ 51 (entry-cap+1); the only above-cap opt-in is the caravan ambush ace (51, still under leader ace 52) — printed, decline-able.
- **`cd_instability`:** ≈ **48** on arrival (Gaia pushes it +8 via the already-shipped leader reward). The economy voice is deep **Act 2 (slipping)** — nervous over-explaining, wheat "alternative" pushed hard. Field liberation claws idx back (−6/field).
- **Recognition:** **mid** for the leader and quest-givers; **late** fan-out on the caravan and Warden Ossa once fields fall. Civilians (Well-Keeper Marisol, prospector kids) **never** recognize the founder — they only feel the thirst and the price decay.

**The arc job this town does**
1. Make the wheat war *standable* — water rights vs monopoly (Dry Season).
2. Drop the **mid-recognition lore bomb** — a boundary stone bearing the founder's own personal seal (The Reach Remembers).
3. Fire the **ambush-at-4-fields** marquee (the Grain-Buyer Caravan) — the first open founder-naming in the field.
4. Plant the **Crossroads Granary liberation hook** and the **forward pull to Cyber City** (the hard turn, HQ near there).

**Place on the route:** Gym 5 Gaviota → **R8 Dunewind Trail** → Kalahar Reach (Gym 6) → Cyber City (Gym 7, the hard turn; HQ raid unlocks nearby).

---

## 2. Cast

New town/quest cast (all under a NEW `dialog-src/characters/kalahar/` folder; the compiler globs `characters/**`, folder is cosmetic). Surface **Y≈126** in town; Oasis/granary props sit at Y≈64–66. All town coords are **PROPOSED** — showrunner/builder confirms in-world before ship (latch spawns once).

| id | display_name | role | one-line concept | placement anchor |
|----|--------------|------|------------------|------------------|
| `kalahar_nurse` | Nurse Sabine | `healer` | Paid Center nurse; her Company-refused shipments arrive "adjusted" — short two crates, nothing actionable. | (2055, 126, 4070) PROPOSED |
| `kalahar_rumor_marisol` | Well-Keeper Marisol | `quest_giver` | **RUMOR HUB.** Stands by a dead well; points at every Kalahar quest; oblivious civilian, knows only the thirst. | (2040, 126, 4100) PROPOSED |
| `warden_ossa` | Warden Ossa | `quest_giver` | Town elder, keeper of the old land records; half-recognizes the founder off a charter seal and *deflects*. Mid→late. | (2050, 126, 4085) PROPOSED |
| `kalahar_survey_stone_1` | Basalt Survey Stone | `civilian` (prop) | Buried boundary stone at the dune line; sets `seal_stone_1`. | (1980, 126, 3960) PROPOSED |
| `kalahar_survey_stone_2` | Basalt Survey Stone | `civilian` (prop) | Second stone, dune edge; sets `seal_stone_2`. | (2140, 120, 3900) PROPOSED |
| `kalahar_survey_stone_3` | Guarded Survey Stone | `quest_giver` (prop+battle) | Third stone by the granary road; carries **two** seals (Company + founder). Company Land Surveyor guards it — opt-in wager OR cite the counter-charter. | (2318, 66, 3542) PROPOSED |
| `oasis_pump_manifold` | Pump Manifold | `civilian` (prop) | Shut-valve prop at the Oasis; gated on both pump crew down; sets `oasis_pump_off`. | (1735, 66, 4260) PROPOSED |
| `agent_pump_officer` | Yield Officer | `villain_grunt` | Company pump crew #1 at the Oasis; diverting the aquifer, filed as "yield optimization." | (1730, 66, 4265) PROPOSED |
| `agent_pump_foreman` | Site Foreman | `villain_grunt` | Pump crew #2; alarmed mid-recognition ("memo said reroute AROUND you"); stands down on loss. | (1728, 66, 4268) PROPOSED |
| `grain_factor_kalahar` | Grain Factor | `wheat_trader` | Caravan's **second buyer** (new body); joins the shipped `wheat_trader_2` for the ≥4-field DOUBLES ambush. | Old Caravan Road ≈ (1972, 126, 3945) PROPOSED |

**Reused / already-shipped (NOT re-authored, referenced only):**
- `kalahar_leader` (Leader Gaia), `kalahar_guide`, gym ladder trainers — shipped.
- `wheat_trader_2` (the shipped ambush body, uuid `45240216-…`, `dialog:wheat_trader`) — the caravan lead. We only add its **partner** (`grain_factor_kalahar`) and split the ambush trainer/tag.
- Ground Shrine / High Priest Terran — owned by `shrines_audit`.
- Poké-Mart: **reuse** an existing martkeeper pattern (`hz_martkeeper`) if the map lacks a station body — flagged as an open question, not authored here.

---

## 3. Quests

Four quests + the rumor hub. Combat is capped: pump crew 50–51, surveyor 50–51, caravan ambush ace 51 (all ≤ leader ace 52). Above-cap-adjacent fights are opt-in, printed, decline-able.

---

### Q0 — Rumor Hub: Well-Keeper Marisol (the greeter)

**Concept.** Kalahar's rumor-hub NPC (house-style: every town has one). Marisol stands at a dead well and points at each open quest, gated on each quest's not-done tag. She is a **civilian — zero founder recognition** — she only knows the wells died and the caravans got mean. Plants the Cyber City forward hook and a back-echo to a prior liberated field.

- **Forward hook:** *"The caravans keep talking about Cyber City — the wired town past the eastern gap. Whatever the Company is really building, they build it there."*
- **Back-echo:** references `fields_liberated` — *"Word came a field went quiet under someone. The grain barons did not take it well."*

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "kalahar_rumor_marisol",
  "display_name": "Well-Keeper Marisol",
  "role": "quest_giver",
  "act": "1",
  "location": "Kalahar Reach - The Dry Well",
  "recognition_tier": "mid",
  "recipe": "civilian",
  "_comment": "RUMOR HUB for Kalahar. Civilian: ZERO founder recognition (she only knows the thirst). Points at each town quest gated on its not-done tag; also the Dry Season giver (Q2). Priority: dry_season report/thanks > board of open quests > default greet. Forward hook -> Cyber City; back-echo -> fields_liberated. PLACEMENT PROPOSED (2040,126,4100) beside a dry well; builder-confirm, latch spawns once.",
  "movement": { "objective": "ambient_stationary_look" },
  "dialog": "dialog:kalahar_rumor_marisol",
  "placement": { "x": 2040, "y": 126, "z": 4100 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "kalahar_rumor_marisol",
  "type": "STANDARD",
  "entries": [
    {
      "label": "dry_done",
      "name": "Marisol - the water came back",
      "priority": 40,
      "gate": { "tag": "oasis_restored" },
      "say": [
        "You shut their pump. The well spat mud, then sand, then water. I had forgotten the sound. Whatever you are, stranger, the Reach drinks tonight because of you.",
        "They filed our thirst as yield optimization. You filed it back. Go on to Cyber City if you are going - the caravans say the Company builds its real work in the wired town past the gap."
      ]
    },
    {
      "label": "board",
      "name": "Marisol - what needs doing",
      "priority": 20,
      "gate": { "not_tag": "oasis_restored" },
      "say": [
        "Kalahar Reach. Half a town and a whole lot of dust. The wells died in one night - all of them - and the caravans went from rude to dangerous the same week.",
        "If you are the helping kind: my wells. And ask Warden Ossa about the boundary stones - she thinks the northern fields were never the Company to sell. Word is a field already went quiet under someone out there. The grain barons did not take it well."
      ],
      "buttons": [
        {
          "label": "water_button",
          "text": "Tell me about the wells",
          "actions": [ { "do": "open_dialog", "label": "dry_brief" } ]
        },
        {
          "label": "leave_button",
          "text": "Later",
          "actions": [ { "do": "close" } ]
        }
      ]
    },
    {
      "label": "default",
      "name": "Well-Keeper Marisol",
      "priority": 10,
      "default": true,
      "say": [
        "Marisol. I keep the wells - kept them, past tense, until the ground went dry as a ledger.",
        "You want water in Kalahar, you buy it from a Company cart now. Funny how that happened the same week the aquifer forgot how to rise."
      ],
      "buttons": [
        {
          "label": "water_button",
          "text": "Why did the wells die?",
          "actions": [ { "do": "open_dialog", "label": "dry_brief" } ]
        },
        {
          "label": "leave_button",
          "text": "Move on",
          "actions": [ { "do": "close" } ]
        }
      ]
    },
    {
      "label": "dry_brief",
      "name": "Marisol - the pump at the Oasis",
      "priority": -1,
      "gate": { "not_tag": "oasis_restored" },
      "say": [
        "The Oasis, southwest of town, out in the open sand where the mobs still roam. There is a Company pump crew there running a line north to water the granary they took. They drained us to grow their wheat.",
        "Shut their pump and the aquifer finds us again. It is a walk through live country and they will not thank you for it. But the Reach will."
      ],
      "buttons": [
        {
          "label": "accept_button",
          "text": "I will shut the pump",
          "actions": [
            { "do": "command", "cmd": "tag @s add dry_season_active", "as_player": true },
            { "do": "announce", "text": "Dry Season: find the Company pump crew at the Oasis, southwest.", "as": "actionbar", "color": "gold" },
            { "do": "close" }
          ]
        },
        {
          "label": "later_button",
          "text": "Not yet",
          "actions": [ { "do": "close" } ]
        }
      ]
    }
  ]
}
```

- **DATAPACK NEEDS:** none new (uses `tag`/`announce` sugar).
- **QUEST_TARGETS entry:** covered by Q2 (`q.side_kalahar_water`) — Marisol is the giver/turn-in.
- **REWARD/BALANCE:** none directly (hub). Cap-legal: no battle. Decline: every prompt has a leave/later button.

---

### Q1 — "The Reach Remembers" (Boundary Stones) — lore + recognition, low-combat

**Concept.** Warden Ossa (town elder, land-records keeper) sends you to unearth **three basalt survey stones** proving the northern fields were common land — buried when the dunes shifted. Stone 3 is **guarded** by a Company Land Surveyor (opt-in wager OR cite Ossa's counter-charter to make him step aside) and bears **two** seals: the Company's, and an **older personal seal** Ossa has seen on exactly one charter — the founder's hand. Mid-recognition lore bomb; Ossa **deflects**, the player learns nothing certain.

- **Forward hook:** Ossa's filed claim becomes leverage on the granary — *"File this and the Crossroads Granary was never theirs to hold. Someone should go take it back."* (Crossroads Granary / `farm_5` liberation hook.)
- **Back-echo:** references a prior gym/route — Ossa reads the player's signature on her log and echoes the boundary-stone seal (*"the same stroke"*); also ties to the running field campaign.
- **Pattern copied:** `sq_beekeeper_tomo` seal-chain (`open_dialog` stages `seal_stone_1..3`, `battle`-or-cite fork on the guarded stone, `on_win` `@1` hook). Stones are placement props (`survey_wagon` pattern).

**Warden Ossa (giver):**

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "warden_ossa",
  "display_name": "Warden Ossa",
  "role": "quest_giver",
  "act": "1",
  "location": "Kalahar Reach - Old Records Post",
  "recognition_tier": "mid",
  "recipe": "quest_fetch",
  "_comment": "Boundary Stones quest giver + mid/late recognition. Sends the player to unearth 3 survey stones (props kalahar_survey_stone_1..3 set seal_stone_1..3). Turn-in gated on all three; pays 600 face via economy/payout + npc_gift/kalahar_ground, sets kalahar_claim_filed + boundary_stones_done. Recognition: mid entry half-names the founder off the stone-3 seal and DEFLECTS; a late fan-out (fields_liberated_gte_4) reads as someone standing near the truth. PLACEMENT PROPOSED (2050,126,4085) at the town records post; latch spawns once.",
  "movement": { "objective": "ambient_stationary_look" },
  "dialog": "dialog:warden_ossa",
  "placement": { "x": 2050, "y": 126, "z": 4085 }
}
```

**Ossa dialog** (STANDARD; priority: done > late-recognition > stone-3-return > resume stages > default):

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "warden_ossa",
  "type": "STANDARD",
  "entries": [
    {
      "label": "filed",
      "name": "Ossa - the claim stands",
      "priority": 40,
      "gate": { "tag": "boundary_stones_done" },
      "say": [
        "Three stones, three rubbings, one counter-claim filed in ink older than the Company. The northern fields were common land. Paper says so now, and paper is testimony.",
        "If you find yourself out by the Crossroads Granary - it was never theirs to hold. Someone should go take it back. I filed the reason; you can be the argument."
      ]
    },
    {
      "label": "return_stone3",
      "name": "Ossa - the second seal",
      "priority": 30,
      "gate": { "all_tags": ["seal_stone_1", "seal_stone_2", "seal_stone_3"], "not_tag": "boundary_stones_done" },
      "say": [
        "You brought all three rubbings. Common land, marked and dated - the Company title is paper over paper.",
        "This third stone. It bears two seals. The Company mark, yes. But under it, older, a personal cut - and I have seen that cut on exactly one charter in my life. The founders own hand. And it is the same stroke as the way you just signed my log. Funny thing, the desert. It keeps what people bury.",
        "No. I am an old woman reading marks in bad light. Let it lie. Take your reward and my thanks."
      ],
      "buttons": [
        {
          "label": "file_button",
          "text": "File the counter-claim with Ossa",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/kalahar/file_claim", "as_player": true },
            { "do": "close" }
          ]
        }
      ]
    },
    {
      "label": "resume_stone3",
      "name": "Ossa - one stone left",
      "priority": 22,
      "gate": { "all_tags": ["seal_stone_1", "seal_stone_2"], "not_tag": "seal_stone_3" },
      "say": [
        "Two stones read. The third is out on the Old Caravan Road toward the granary - and the Company parked a Land Surveyor right on top of it. He is paper too. Either fight him or read his own boundary law back to him."
      ]
    },
    {
      "label": "resume_stone2",
      "name": "Ossa - two stones left",
      "priority": 21,
      "gate": { "tag": "seal_stone_1", "not_tag": "seal_stone_2" },
      "say": [
        "One stone read. The next sits half-buried at the dune edge east of here - follow the shimmer, not the map. The map lies out here; the stones do not."
      ]
    },
    {
      "label": "default",
      "name": "Warden Ossa",
      "priority": 10,
      "default": true,
      "say": [
        "Ossa. I keep the old land records - what the dunes have not eaten. The Company holds title to the northern fields. Their title is paper.",
        "The real boundary was cut into three basalt survey stones a lifetime ago - common land, all of it - then the sand took them. Unearth the three and I can file a counter-claim that the granary they seized was never theirs to sell. Walk the dune line. The stones remember."
      ],
      "buttons": [
        {
          "label": "accept_button",
          "text": "I will dig up the stones",
          "actions": [
            { "do": "command", "cmd": "tag @s add boundary_stones_active", "as_player": true },
            { "do": "announce", "text": "The Reach Remembers: unearth three survey stones along the dune line.", "as": "actionbar", "color": "gold" },
            { "do": "close" }
          ]
        },
        {
          "label": "later_button",
          "text": "Later",
          "actions": [ { "do": "close" } ]
        }
      ]
    }
  ]
}
```

**Stone props** (placement + `dialog_inline`, `role:"civilian"`, copy `survey_wagon`). Stone 1 shown in full; **stone 2 is identical with `_1`→`_2`, `seal_stone_1`→`seal_stone_2`, and its own placement**:

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "kalahar_survey_stone_1",
  "display_name": "Basalt Survey Stone",
  "role": "civilian",
  "act": "1",
  "location": "Kalahar Reach - Dune Line",
  "recognition_tier": "mid",
  "recipe": "civilian",
  "_comment": "Boundary Stones prop #1 (survey_wagon pattern). Gated interaction sets seal_stone_1. PLACEMENT PROPOSED (1980,126,3960) half-buried at the dune line; flush against a real basalt build. Latch spawns once; no uuid.",
  "movement": { "objective": "ambient_stationary_look" },
  "dialog_inline": {
    "kind": "dialog",
    "id": "kalahar_survey_stone_1",
    "type": "STANDARD",
    "entries": [
      {
        "label": "taken",
        "name": "Survey Stone - read",
        "priority": 20,
        "gate": { "tag": "seal_stone_1" },
        "say": [ "The first stone, cleared of sand. Common land, cut plain and dated. You have your rubbing." ],
        "buttons": [ { "label": "leave_button", "text": "Move on", "actions": [ { "do": "close" } ] } ]
      },
      {
        "label": "default",
        "name": "Basalt Survey Stone",
        "priority": 10,
        "default": true,
        "gate": { "tag": "boundary_stones_active", "not_tag": "seal_stone_1" },
        "say": [ "A basalt slab, most of it under the dune. Brush it clear and a boundary mark shows - older than any Company charter. Ossa will want a rubbing of this." ],
        "buttons": [
          {
            "label": "dig_button",
            "text": "Clear the sand and take a rubbing",
            "actions": [
              { "do": "command", "cmd": "tag @s add seal_stone_1", "as_player": true },
              { "do": "announce", "text": "First survey stone read. Two more along the dune line.", "as": "actionbar", "color": "gold" },
              { "do": "close" }
            ]
          },
          { "label": "leave_button", "text": "Step away", "actions": [ { "do": "close" } ] }
        ]
      }
    ]
  },
  "placement": { "x": 1980, "y": 126, "z": 3960 }
}
```

**Stone 3 (guarded prop + battle-or-cite fork)** — carries the two-seal reveal, hosts the Land Surveyor battle on its own body:

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "kalahar_survey_stone_3",
  "display_name": "Guarded Survey Stone",
  "role": "quest_giver",
  "act": "1",
  "location": "Kalahar Reach - Old Caravan Road",
  "recognition_tier": "mid",
  "trainer": "sq_boundary_surveyor",
  "recipe": "quest_fetch",
  "_comment": "Boundary Stones prop #3 + guard. Two-seal reveal (Company + founder). Company Land Surveyor is HOSTED ON THIS BODY via the battle block (like Tomo seal-four): opt-in wager battle sq_boundary_surveyor (lv 50-51, ABOVE-CAP-ADJACENT, printed + decline_fee), OR cite Ossas counter-charter (no-battle fork, sets stone3_guard_clear). Either clears the guard -> pry sets seal_stone_3. PLACEMENT PROPOSED (2318,66,3542) by the granary road; no uuid, latch spawns once.",
  "movement": { "objective": "ambient_stationary_look" },
  "battle": {
    "trainer": "sq_boundary_surveyor",
    "type": "wager",
    "format": "GEN_9_SINGLES",
    "prize": 400,
    "loss_fee": 200,
    "decline_fee": 150,
    "defeat_tag": "stone3_guard_clear",
    "win_line": "The survey is - revised. Fine. I never verified this stretch anyway. Take your rock.",
    "lose_line": "Boundary upheld. The Company thanks you for the trespass fee.",
    "already_beaten_line": "You already made your point. The stone is yours; I am filing a complaint about the dust."
  },
  "dialog_inline": {
    "kind": "dialog",
    "id": "kalahar_survey_stone_3",
    "type": "STANDARD",
    "entries": [
      {
        "label": "taken",
        "name": "Survey Stone - the second seal",
        "priority": 30,
        "gate": { "tag": "seal_stone_3" },
        "say": [ "The third stone, cleared. Two seals cut into it - the Company mark, and under it an older personal one. Ossa should see this. She will know the hand." ],
        "buttons": [ { "label": "leave_button", "text": "Back to Ossa", "actions": [ { "do": "close" } ] } ]
      },
      {
        "label": "cleared",
        "name": "Survey Stone - unguarded",
        "priority": 25,
        "gate": { "tag": "stone3_guard_clear", "not_tag": "seal_stone_3" },
        "say": [ "The surveyor has stepped aside. The stone is clear to read. Under the sand: two seals, one older than the Company itself." ],
        "buttons": [
          {
            "label": "pry_button",
            "text": "Take the rubbing - both seals",
            "actions": [
              { "do": "command", "cmd": "tag @s add seal_stone_3", "as_player": true },
              { "do": "announce", "text": "Third stone read. Two seals - one you cannot place, and neither can you look away from it. Return to Ossa.", "as": "actionbar", "color": "gold" },
              { "do": "close" }
            ]
          },
          { "label": "leave_button", "text": "Step away", "actions": [ { "do": "close" } ] }
        ]
      },
      {
        "label": "default",
        "name": "Guarded Survey Stone",
        "priority": 10,
        "default": true,
        "gate": { "all_tags": ["seal_stone_1", "seal_stone_2"], "not_tag": "seal_stone_3" },
        "say": [
          "A Land Surveyor from the Company stands squarely on the third stone, clipboard out, boots on the boundary mark. He informs you this stretch is surveyed, verified, and closed pending review.",
          "You can fight him for the ground, or read his own boundary law back at him and watch the clipboard win the argument for you."
        ],
        "buttons": [
          {
            "label": "fight_button",
            "text": "Wager battle - 400 CD purse, 200 CD if you lose, above the cap",
            "gate": { "not_tag": "stone3_guard_clear" },
            "actions": [ { "do": "battle" } ]
          },
          {
            "label": "cite_button",
            "text": "Cite Ossas counter-charter back at him - 150 CD filing fee",
            "gate": { "not_tag": "stone3_guard_clear" },
            "actions": [
              { "do": "command", "cmd": "function cobblemon_initiative:sidequest/kalahar/cite_surveyor", "as_player": true },
              { "do": "close" }
            ]
          },
          { "label": "leave_button", "text": "Come back later", "actions": [ { "do": "close" } ] }
        ]
      },
      {
        "label": "locked",
        "name": "Guarded Survey Stone - dig the first two",
        "priority": 5,
        "say": [ "The stone is guarded, and you have not yet read the first two. Ossa said follow the dune line before the road." ],
        "buttons": [ { "label": "leave_button", "text": "Step away", "actions": [ { "do": "close" } ] } ]
      }
    ]
  },
  "placement": { "x": 2318, "y": 66, "z": 3542 }
}
```

- **DATAPACK NEEDS:**
  - `sidequest/kalahar/cite_surveyor.mcfunction` — pay-probe the 150 CD filing fee (copy `economy/heal_paid` balance-gate idiom); on success set `stone3_guard_clear` + `announce`; on insufficient funds, fail-soft with a "cannot afford the filing fee" tellraw and no tag. *(NOTE: charge via pay-probe — engine work; spec only.)*
  - `sidequest/kalahar/file_claim.mcfunction` — the Q1 turn-in/reward. Verify `seal_stone_1..3` present (redundant safety re-check via `execute if entity @s[tag=…]`), then `function cobblemon_initiative:economy/payout {amount:600}`, `loot give @s loot cobblemon_initiative:npc_gift/kalahar_ground`, `tag @s add kalahar_claim_filed`, `tag @s add boundary_stones_done`, a title sting `CLAIM FILED`, and a Company-voice money receipt on the payout — `{do:announce, as:subtitle}` reading `ADJUSTMENT: rounding, in the Company favor` (glossy-nervous economy voice; no apostrophe/percent).
  - Loot table `loot_tables/npc_gift/kalahar_ground.json` — Hard Stone + Smooth/Soft Sand (jar-validate `cobblemon:hard_stone` / sand item ids before ship).
- **QUEST_TARGETS entry:** `q.side_kalahar_stones` (below).
- **REWARD/BALANCE:** turn-in **600 CD face** (~88% net at idx 48) + Hard Stone/Sand. Guard battle: **opt-in wager, 400 purse / 200 loss / 150 decline**, all printed in the button labels; lv 50–51 (above-cap-adjacent — decline-able). Cite fork = pay-probe 150 CD, fail-soft. No forced whiteout (wager, not `villain_forced`).

---

### Q2 — "Dry Season" (Water Rights) — villain raid, on-theme combat

**Concept.** Marisol's wells died overnight. A Company **pump crew at the Oasis** (SW wild zone) diverts the aquifer north to water the seized granary — filed as *"agricultural yield optimization."* Trek to the Oasis (live Nuzlocke), beat the **Yield Officer** then the **Site Foreman** (gated), shut the **pump manifold**, return to Marisol. This is the **water-rights vs Company-monopoly** beat made physical.

- **Forward hook:** the Foreman's parting alarm points onward — *"reroute AROUND you… corporate is out of memos for you. Take it up with Cyber City."*
- **Back-echo:** deliberate callback to the **Takehara falls-redirection scam** (the Company draining water is a repeated MO) — Marisol's turn-in line names it.
- **Recognition beat (mid → alarm):** the Foreman is *alarmed, not reverent* ("You are on no manifest. That face — the memo said reroute AROUND you, not through you."); stands down after the loss. Marisol (civilian) never recognizes the CEO.

**Yield Officer (villain grunt, Kalahar-band Market Analyst tier — bespoke side trainer `sq_pump_officer`, NOT one of the numbered `villain_grunt_N` roster ids):**

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "agent_pump_officer",
  "display_name": "Yield Officer",
  "role": "villain_grunt",
  "act": "1",
  "location": "Kalahar Reach - The Oasis",
  "recognition_tier": "mid",
  "trainer": "sq_pump_officer",
  "recipe": "villain_grunt",
  "_comment": "Dry Season pump crew #1. First fight of the Oasis raid; gates the Foreman. Ground/Normal desert-crew mons lv 50-51 (<= cap+1). Not despawned (crew stays as a beaten line). PLACEMENT PROPOSED (1730,66,4265) Oasis center (wild zone). Latch spawns once; no uuid.",
  "movement": { "objective": "ambient_stationary_look" },
  "battle": {
    "trainer": "sq_pump_officer",
    "type": "villain_grunt",
    "format": "GEN_9_SINGLES",
    "prize": 300,
    "defeat_tag": "defeated_sq_pump_officer",
    "despawn_on_win": false,
    "win_line": "Yield disruption logged. Escalating to the site foreman - talk to him, if you can get past him.",
    "lose_line": "Optimization proceeds on schedule. The Reach is a rounding error.",
    "already_beaten_line": "I already filed the incident. The foreman runs the valve, not me."
  },
  "dialog": "dialog:kalahar_pump_crew",
  "placement": { "x": 1730, "y": 66, "z": 4265 }
}
```

**Site Foreman (villain grunt, gated on the Officer; the recognition-alarm body):**

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "agent_pump_foreman",
  "display_name": "Site Foreman",
  "role": "villain_grunt",
  "act": "1",
  "location": "Kalahar Reach - The Oasis",
  "recognition_tier": "mid",
  "trainer": "sq_pump_foreman",
  "recipe": "villain_grunt",
  "_comment": "Dry Season pump crew #2 - the mid-recognition ALARM body (memo said reroute AROUND you). Gated on defeated_sq_pump_officer via dialog. Stands down on loss. Ace lv 51 (<= cap+1). Unlocking the manifold shut-off needs BOTH crew down. PLACEMENT PROPOSED (1728,66,4268). Latch spawns once; no uuid.",
  "movement": { "objective": "ambient_stationary_look" },
  "battle": {
    "trainer": "sq_pump_foreman",
    "type": "villain_grunt",
    "format": "GEN_9_SINGLES",
    "prize": 400,
    "defeat_tag": "defeated_sq_pump_foreman",
    "despawn_on_win": false,
    "win_line": "You are on no manifest. That face - the memo said reroute AROUND you, not through you. Take the valve. I was never here.",
    "lose_line": "Line pressure holds. The granary drinks tonight. You do not.",
    "already_beaten_line": "The valve is yours to shut. Corporate is out of memos for you. Take it up with Cyber City."
  },
  "dialog": "dialog:kalahar_pump_crew",
  "placement": { "x": 1728, "y": 66, "z": 4268 }
}
```

**Shared pump-crew dialog** (STANDARD; both bodies use it — the Foreman's battle button gates on the Officer being down):

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "kalahar_pump_crew",
  "type": "STANDARD",
  "_comment": "Shared by agent_pump_officer + agent_pump_foreman. Each body reads its own battle block on {do:battle}. Foreman fight gates on defeated_sq_pump_officer so the crew falls in order. Mid recognition: confused/alarmed hostility, not reverence.",
  "entries": [
    {
      "label": "recognized_late",
      "name": "Pump Crew - stand down",
      "priority": 30,
      "gate": { "fields_liberated": { "op": "gte", "value": 4 } },
      "say": [
        "We had orders about you. Reroute around, do not engage, do not confirm the face. The face from the memo with the black bar over the name.",
        "You keep drying up our fields and now you are at our pump. I am paid to move water, not to be right about who you are. State your business."
      ],
      "buttons": [
        { "label": "fight_button", "text": "Shut them down", "actions": [ { "do": "battle" } ] },
        { "label": "leave_button", "text": "Back off", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Pump Crew",
      "priority": 10,
      "default": true,
      "say": [
        "This is a licensed extraction site. Agricultural yield optimization, filed and verified. The water goes where the Company needs it.",
        "The town wells are a legacy allocation under review. Your concern is noted and will be processed. Now step back from the manifold."
      ],
      "buttons": [
        { "label": "fight_button", "text": "Make them stop pumping", "actions": [ { "do": "battle" } ] },
        { "label": "leave_button", "text": "Not yet", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**Pump manifold prop (shut valve; gated on both crew down):**

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "oasis_pump_manifold",
  "display_name": "Pump Manifold",
  "role": "civilian",
  "act": "1",
  "location": "Kalahar Reach - The Oasis",
  "recognition_tier": "mid",
  "recipe": "civilian",
  "_comment": "Dry Season shut-off prop (survey_wagon pattern). Valve unlocks only when BOTH crew are down; interacting sets oasis_pump_off and runs the -3 instability clawback + restore. PLACEMENT PROPOSED (1735,66,4260) beside the crew, flush against a real manifold build. Latch spawns once; no uuid.",
  "movement": { "objective": "ambient_stationary_look" },
  "dialog_inline": {
    "kind": "dialog",
    "id": "oasis_pump_manifold",
    "type": "STANDARD",
    "entries": [
      {
        "label": "off",
        "name": "Pump Manifold - shut",
        "priority": 30,
        "gate": { "tag": "oasis_pump_off" },
        "say": [ "The valve is closed. The line groans, empties, and the desert takes its water back. Tell Marisol the wells will remember how to rise." ],
        "buttons": [ { "label": "leave_button", "text": "Back to town", "actions": [ { "do": "close" } ] } ]
      },
      {
        "label": "ready",
        "name": "Pump Manifold - unguarded",
        "priority": 20,
        "gate": { "all_tags": ["defeated_sq_pump_officer", "defeated_sq_pump_foreman"], "not_tag": "oasis_pump_off" },
        "say": [ "The crew is down and the manifold is yours. A single wheel valve holds the whole diversion. Turn it and give Kalahar its aquifer back." ],
        "buttons": [
          {
            "label": "shut_button",
            "text": "Shut the valve",
            "actions": [
              { "do": "command", "cmd": "function cobblemon_initiative:sidequest/kalahar/shut_pump", "as_player": true },
              { "do": "close" }
            ]
          },
          { "label": "leave_button", "text": "Leave it running", "actions": [ { "do": "close" } ] }
        ]
      },
      {
        "label": "default",
        "name": "Pump Manifold - guarded",
        "priority": 10,
        "default": true,
        "say": [ "The pump manifold thrums, drawing the Oasis dry. Two Company crew stand over it. You will not touch this valve while they are watching." ],
        "buttons": [ { "label": "leave_button", "text": "Step away", "actions": [ { "do": "close" } ] } ]
      }
    ]
  },
  "placement": { "x": 1735, "y": 66, "z": 4260 }
}
```

- **DATAPACK NEEDS:**
  - `sidequest/kalahar/shut_pump.mcfunction` — set `oasis_pump_off`, apply a **−3 `cd_instability` clawback** (`scoreboard players remove @s cd_instability 3`, floor at 0 with `execute if score @s cd_instability matches ..-1 run scoreboard players set @s cd_instability 0` — verify the runtime holder/idiom against `economy/gym_destabilize`), title sting `THE REACH DRINKS`.
  - `sidequest/kalahar/water_reward.mcfunction` — Marisol turn-in: verify `oasis_pump_off`, `function economy/payout {amount:750}`, `give @s cobblemon:fresh_water 8` (jar-validate id) + `super_potion ×2`, `tag @s add oasis_restored` + `dry_season_done`, title sting `THE REACH DRINKS`, then a Company-voice money receipt on the payout — `{do:announce, as:subtitle}` reading `ADJUSTMENT: rounding, in the Company favor` (glossy-nervous economy voice on money; no apostrophe/percent in the line).
- **QUEST_TARGETS entry:** `q.side_kalahar_water` (below).
- **REWARD/BALANCE:** turn-in **750 CD face** + Fresh Water ×8 + super_potion ×2. Fights are **villain_grunt** at 50–51 (≤ cap+1) — no decline (grunts stand their ground), but the **fairness floor holds** (they are `villain_grunt`, not `villain_forced` engaging on distance; the player initiates via dialog, so no whiteout is forced on a caught-mon-less player). Crew do **not** despawn. Optional `−3` instability clawback makes "starving the field's water" mechanically felt.

---

### Q3 — "The Grain-Buyer Caravan" (AMBUSH at ≥4 fields) — MARQUEE, emergent

**Concept.** The **ambush-at-4-fields** beat and the town's loudest recognition line. When `fields_liberated ≥ 4`, the already-shipped `wheat_trader/tick.mcfunction` sets `wheat_trader_hostile`; the shipped `wheat_trader_2` body at Kalahar flips into its `hostile` dialog and forces a fight — **no new poller needed.** This doc's work: (a) add the caravan's **second buyer** (`grain_factor_kalahar`) so the ambush is a Kalahar-scaled **DOUBLES**, and (b) **split the shared ambush trainer/tag** so beating the Hua Zhan trader does not pre-clear Kalahar.

- **Forward hook:** on win, the field-war HUD flavor points at Cyber City / HQ — *"Word travels the caravan roads. The wired town knows your face now."*
- **Back-echo:** direct callback to `fields_liberated` (the whole run's field campaign) and the granary — *commerce curdling into the cover-up.*
- **Recognition (mid → the loud one):** the shipped `dialog:wheat_trader` `hostile` entry already delivers the first **open founder-naming in the field** (*"you are the one the memos warned us about. The founder. You are supposed to be filed."*) — framed as enemy memo-paranoia, NOT narration. **Do not author a new name reveal.** This doc only adds the DOUBLES partner + retuned team.

**Split-tag decision (implemented here):** give the Kalahar ambush its own trainer + defeat tag so it is a distinct, properly-levelled beat.

**Grain Factor (the caravan's second buyer — new body, DOUBLES partner):**

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "grain_factor_kalahar",
  "display_name": "Grain Factor",
  "role": "wheat_trader",
  "act": "2",
  "location": "Kalahar Reach - Old Caravan Road",
  "recognition_tier": "mid",
  "trainer": "wheat_trader_ambush_kalahar",
  "recipe": "grain_buyer",
  "_comment": "Caravan ambush PARTNER for the shipped wheat_trader_2 (Kalahar). NEW body (takes placement; wheat_trader_2 is builder-placed/uuid and must NOT get a placement). At fields_liberated>=4 the pair ambush as a DOUBLES: wheat_trader_2 hosts the GEN_9_DOUBLES battle via wheat_trader_ambush_kalahar; this partner stands beside it and carries the same hostile-recognition dialog. SPLIT TAG: trainer wheat_trader_ambush_kalahar + defeat_tag defeated_wheat_ambush_kalahar (NOT the shared defeated_wheat_trader_ambush) so Hua Zhans trader does not pre-clear Kalahar. despawn_on_win false (caravan does not empty). Team = Bouffalant/Miltank/Mudsdale/Tauros lv50-51 (ace 51 <= leader 52). PLACEMENT PROPOSED (1972,126,3945) Old Caravan Road beside wheat_trader_2.",
  "movement": { "objective": "ambient_stationary_look" },
  "triggers": [
    {
      "on": "ON_DISTANCE_VERY_CLOSE",
      "actions": [ { "do": "battle", "gate": { "wheat_trader": "hostile" } } ]
    }
  ],
  "battle": {
    "trainer": "wheat_trader_ambush_kalahar",
    "type": "villain_forced",
    "format": "GEN_9_DOUBLES",
    "prize": 500,
    "defeat_tag": "defeated_wheat_ambush_kalahar",
    "despawn_on_win": false,
    "win_line": "Enough. Take the road. Take the fields. The grain was never worth what they said it was.",
    "lose_line": "Filed. The caravan thanks you for your final contribution.",
    "already_beaten_line": "No. Not twice. Word went out about you down every caravan road.",
    "on_win": [
      "tag @1 add kalahar_ambush_cleared"
    ]
  },
  "dialog": "dialog:wheat_trader"
}
```

- **DATAPACK NEEDS:** none new. Reuses the shipped `wheat_trader/tick.mcfunction` (sets `wheat_trader_hostile` at ≥4 fields) and the shipped `dialog:wheat_trader` `hostile` entry (the founder-naming line). The only additions are the RCT team + registry entry (below), which are `rctmod/`-side, not datapack functions.
- **RCT TEAM (per roadmap §6, `rctmod/`-side — flagged, not authored here since this doc is `dialog-src`-scoped):** `rctmod/trainers/wheat_trader_ambush_kalahar.json` = GEN_9_DOUBLES, Grain Buyer lead Bouffalant 50 / Miltank 50; Grain Factor partner Mudsdale 51 / Tauros 50 (ace 51). Register in `trainers/side_quests/act1.json` (or `villain_team`) with `defeat_tag defeated_wheat_ambush_kalahar`. Also swap the shipped `wheat_trader_2.json`'s `battle.trainer`/`defeat_tag` to the Kalahar-split ids so the pair share one DOUBLES definition (see Build checklist).
- **QUEST_TARGETS entry:** intentionally **none** — the ambush is emergent/latent (no sidebar line; the shipped q.main "Liberate wheat fields" line already covers the field campaign). Adding a waypoint would spoil the ambush. `kalahar_ambush_cleared` is HUD flavor only.
- **REWARD/BALANCE:** flat TBCS `onwin` prize **500 CD** (flat-literal per the economy rule — no skew). Ace **51** (under leader 52; a forced villain fight but tense-fair and emergent). **Decline:** the player may **Stand down** via the existing `dialog:wheat_trader` hostile-entry leave button; the forced `ON_DISTANCE_VERY_CLOSE` battle only arms at ≥4 fields. **Fairness floor:** `villain_forced` — confirm the shared fairness guard (no whiteout on a caught-mon-less player) applies; the player must have engaged the field campaign to reach this state.

---

## 4. Recognition & economy beats

**Recognition (band = mid, entering with 5 badges; late fan-out on fields):**
- **Grunts (Yield Officer / Site Foreman, mid):** confused/alarmed hostility, not reverence. Foreman: *"You are on no manifest. That face — the memo said reroute AROUND you, not through you."* Stands down on loss. Their `fields_liberated_gte_4` entry escalates to "reroute around, do not confirm the face."
- **Warden Ossa (mid, the lore bomb):** half-recognizes the founder's personal seal on stone 3 and **deflects** — *"the same stroke as the way you just signed my log… I am an old woman reading marks in bad light. Let it lie."* The player learns nothing certain.
- **Grain-Buyer Caravan (mid → the loud one, ≥4 fields):** the shipped `dialog:wheat_trader` `hostile` entry is the **first open founder-naming in the field** — *"you are the one the memos warned us about. The founder. You are supposed to be filed."* Commerce curdling into the cover-up. **Not narration — enemy memo-paranoia.**
- **Civilians (Marisol, prospector kids):** **never** recognize the founder. Marisol knows only the thirst and that "you buy water from a Company cart now."

**Economy voice (idx ≈ 48, deep Act 2 "slipping"):**
- Pump crew: over-explaining "price adjustments" made literal — *"a legacy allocation under review,"* *"agricultural yield optimization, filed and verified."*
- Granary (shipped `granary_keeper`, gated on `fields_liberated`): the wheat "alternative" pushed hardest here; trades **worsen** as fields fall (relief tiers) — *"the paper money keeps slipping."*
- Field-liberation feel: shutting the Oasis pump fires an optional **−3 `cd_instability`** clawback; every liberated field is −6. The payout haircut at idx 48 is **~12% (88% of face)** — quest CD figures are face; players net less until they claw idx down.
- **Streamable receipts:** `announce as:title/subtitle` stings on the beats — *THE REACH DRINKS* (pump shut), *CLAIM FILED* (stones), and the caravan's win flavor. Both cash turn-ins fire the Company-voice money receipt as a subtitle — *ADJUSTMENT: rounding, in the Company favor* — so even a payout in the player's favor is skimmed on-screen (glossy-nervous, no apostrophe/percent per the macro rules).

---

## 5. New tags/scores introduced

| tag / score | set by | gated by (read where) |
|-------------|--------|-----------------------|
| `dry_season_active` | Marisol `dry_brief` accept button | (activation) Q2 sidebar |
| `boundary_stones_active` | Ossa `default` accept button | stone props' default entry gate |
| `seal_stone_1` | `kalahar_survey_stone_1` dig button | Ossa resume/turn-in gates; stone 3 default gate |
| `seal_stone_2` | `kalahar_survey_stone_2` dig button | Ossa resume/turn-in gates; stone 3 default gate |
| `seal_stone_3` | `kalahar_survey_stone_3` pry button | Ossa `return_stone3` turn-in gate |
| `stone3_guard_clear` | `sq_boundary_surveyor` win (`defeat_tag`) OR `cite_surveyor.mcfunction` | stone 3 `cleared`/pry gate |
| `boundary_stones_done` | `file_claim.mcfunction` | Ossa `filed` entry; Q1 sidebar not_tag |
| `kalahar_claim_filed` | `file_claim.mcfunction` | later farm_5 liberation bonus callback (roadmap) |
| `defeated_sq_pump_officer` | Yield Officer win (`defeat_tag`) | Foreman fight availability; manifold `ready` gate |
| `defeated_sq_pump_foreman` | Site Foreman win (`defeat_tag`) | manifold `ready` gate |
| `oasis_pump_off` | `shut_pump.mcfunction` (manifold) | manifold `off` entry; Marisol water_reward verify |
| `oasis_restored` | `water_reward.mcfunction` (Marisol) | Marisol `dry_done`; Q2 sidebar not_tag |
| `dry_season_done` | `water_reward.mcfunction` | Q2 sidebar not_tag |
| `defeated_wheat_ambush_kalahar` | `grain_factor_kalahar` / retuned `wheat_trader_2` win | ambush `already_beaten` state (split from shared tag) |
| `kalahar_ambush_cleared` | ambush `on_win` `@1` | HUD flavor only |

**Reused (already maintained — do NOT re-introduce):** `wheat_trader_hostile`, `wheat_trader_suspicious` (from `wheat_trader/tick`), `fields_liberated` (+ band tag `fields_liberated_gte_4`), `cd_instability`, `defeated_gaviota_leader`, `defeated_kalahar_leader`, `frag_6`.

> All new numeric-free tags auto-generate their `no_*` inverses via `content_compile`'s `band_tags.mcfunction` regen (per roadmap §8). Numeric gates (`fields_liberated ≥ 4`) lower to the existing `fields_liberated_gte_4` band tag — already maintained.

---

## 6. Build checklist

Ordered, copy-paste-compile:

1. **Create folder** `dialog-src/characters/kalahar/`.
2. **Drop 10 character files** (this doc §2/§3): `kalahar_nurse.json` (author a `service:{kind:heal}` body mirroring `hz_nurse` — reuse the shipped `economy/heal_paid` fee, do NOT fork it), `kalahar_rumor_marisol.json`, `warden_ossa.json`, `kalahar_survey_stone_1.json`, `kalahar_survey_stone_2.json` (clone of stone_1 with `_2` ids + `seal_stone_2` + its placement), `kalahar_survey_stone_3.json`, `oasis_pump_manifold.json`, `agent_pump_officer.json`, `agent_pump_foreman.json`, `grain_factor_kalahar.json`.
3. **Drop 3 standalone dialog files** under `dialog-src/dialog/`: `kalahar_rumor_marisol.json`, `warden_ossa.json`, `kalahar_pump_crew.json`. (Stone props + manifold use `dialog_inline`; the caravan reuses `dialog:wheat_trader`.)
4. **Add datapack functions** under `src/main/resources/data/cobblemon_initiative/function/sidequest/kalahar/`: `file_claim.mcfunction`, `cite_surveyor.mcfunction` (pay-probe 150 CD), `shut_pump.mcfunction` (−3 instability), `water_reward.mcfunction`. Copy the balance-gate idiom from `economy/heal_paid.mcfunction` and the payout call from `economy/payout`.
5. **Add loot table** `loot_tables/npc_gift/kalahar_ground.json` (Hard Stone + Soft/Smooth Sand; jar-validate item ids).
6. **RCT/registry (rctmod-side, per roadmap §6):** create `rctmod/trainers/{sq_pump_officer,sq_pump_foreman,sq_boundary_surveyor,wheat_trader_ambush_kalahar}.json` + matching `rctmod/mobs/trainers/single/` defs; register `sq_pump_officer/foreman/boundary_surveyor` in `trainers/side_quests/act1.json`. **Split the ambush:** repoint the shipped `wheat_trader_2.json` `battle.trainer` → `wheat_trader_ambush_kalahar` and `defeat_tag` → `defeated_wheat_ambush_kalahar` (so the pair share one DOUBLES team and are decoupled from Hua Zhan's `wheat_trader_1`).
7. **Add `quest_targets` stages** to `dialog-src/registers/quest_targets.json` (§ below): `q.side_kalahar_stones` (**slot 56**), `q.side_kalahar_water` (**slot 55**) — slots 71/72 in the original draft collided with `q.side_shift`/`q.side_classic`; 55/56 are free (see Open Q7). (No stage for the emergent caravan ambush.)
8. **Run the pipeline (per roadmap §8):** `scripts/content_compile` → `scripts/generate_granary_tiers` → `scripts/update_preset_index` → `scripts/generate_npc_function` → `gradle build`. `content_compile` regenerates `band_tags.mcfunction` with all new tags + `no_*` inverses.
9. **Placement pass:** confirm all PROPOSED coords in-world (Y126 town / Y64–66 Oasis+granary props) BEFORE shipping a world (latch spawns once; moving later needs a manual kill + latch-score reset). Confirm the Kalahar Center has a nurse station and no free Cobblemon healing block (Sango/Takehara precedent).

**QUEST_TARGETS stage blocks (ready to paste into `quests[]`):**

```json
{
  "holder": "q.side_kalahar_stones",
  "name": "The Reach Remembers (Boundary Stones)",
  "slot": 56,
  "stages": [
    {
      "if_tags": ["seal_stone_1", "seal_stone_2", "seal_stone_3"],
      "not_tags": ["boundary_stones_done"],
      "label": "File the counter-claim with Warden Ossa",
      "target": { "npc": "warden_ossa" },
      "note": "All three rubbings taken -> line + waypoint flip to Ossa for the payout (file_claim)."
    },
    {
      "if_tags": ["seal_stone_1", "seal_stone_2", "boundary_stones_active"],
      "not_tags": ["seal_stone_3", "boundary_stones_done"],
      "label": "Read the guarded stone on the Old Caravan Road",
      "target": { "npc": "kalahar_survey_stone_3" },
      "note": "Third stone by the granary road; guarded by sq_boundary_surveyor (fight or cite)."
    },
    {
      "if_tags": ["boundary_stones_active"],
      "not_tags": ["boundary_stones_done"],
      "label": "Unearth the survey stones along the dune line",
      "target": { "npc": "kalahar_survey_stone_1" },
      "note": "Stones 1 and 2 at the dune line; stone_1 anchors the row (stone_2 shares the walk). Activated by Ossas accept (boundary_stones_active)."
    }
  ]
}
```

```json
{
  "holder": "q.side_kalahar_water",
  "name": "Dry Season (Water Rights)",
  "slot": 55,
  "stages": [
    {
      "if_tags": ["oasis_pump_off"],
      "not_tags": ["dry_season_done"],
      "label": "Report the shut pump to Well-Keeper Marisol",
      "target": { "npc": "kalahar_rumor_marisol" },
      "note": "Valve shut -> line + waypoint flip back to Marisol for the payout (water_reward)."
    },
    {
      "if_tags": ["dry_season_active", "defeated_sq_pump_officer", "defeated_sq_pump_foreman"],
      "not_tags": ["oasis_pump_off", "dry_season_done"],
      "label": "Shut the pump manifold at the Oasis",
      "target": { "npc": "oasis_pump_manifold" },
      "note": "Both crew down -> point at the manifold prop to close the valve."
    },
    {
      "if_tags": ["dry_season_active"],
      "not_tags": ["oasis_pump_off", "dry_season_done"],
      "label": "Drive off the Company pump crew at the Oasis",
      "target": { "npc": "agent_pump_officer" },
      "note": "Oasis is a wild zone (mobsSpawn:true) SW of town; officer anchors the crew, foreman gated behind him. Activated by Marisols accept (dry_season_active)."
    }
  ]
}
```

---

## 7. Open questions for showrunner

1. **Ambush tag split + DOUBLES (roadmap §9.2):** confirm Kalahar gets its own `wheat_trader_ambush_kalahar` / `defeated_wheat_ambush_kalahar` (recommended — otherwise beating the Hua Zhan trader pre-clears Kalahar) and that the ambush is a **DOUBLES caravan** (this doc assumes both). If not, `grain_factor_kalahar` becomes cosmetic and the fight stays a shared singles rematch.
2. **Founder-naming pre-Act-3 (roadmap §9.3):** the shipped `dialog:wheat_trader` `hostile` line says "the founder" out loud at ≥4 fields. Confirm this reads as enemy memo-paranoia (intended, loudest recognition before Act 3) and not a narration reveal. Ossa's stone-3 line is the second such beat — same intent.
3. **Q2 mechanical clawback (roadmap §9.4):** does shutting the Oasis pump actually move `cd_instability` (−3, as authored in `shut_pump`) or is it flavor-only? Confirm the holder/idiom against `economy/gym_destabilize` and whether it should also pressure `farm_5`.
4. **Above-cap opt-in fights:** the stone-3 Land Surveyor (lv 50–51) is above the entry cap of 50 but a printed, decline-able **wager** (400/200/150). The pump crew (50–51) and caravan ace (51) are also at/above cap. Confirm the fairness floor (no whiteout on a caught-mon-less player) is enforced for the `villain_forced` caravan.
5. **Service-NPC parity (roadmap §9.5):** confirm Kalahar needs a placed Nurse (`kalahar_nurse`, authored as a checklist item) + a Poké-Mart body. If the map build already provides these stations, drop `kalahar_nurse` and reuse an existing martkeeper for `shop badge_6`.
6. **Item id validation:** jar-validate `cobblemon:hard_stone`, the sand item ids, `cobblemon:fresh_water`, and `super_potion` in 1.7.3 before ship (the loot table + reward gives). Substitute vanilla equivalents if any are unverified.
7. **Slots 55/56 (collision-checked):** slots **71/72 were already taken** (`q.side_shift` / `q.side_classic`) and slots 57–81 are a contiguous occupied block, so the Kalahar lines were reassigned to the free, tier-appropriate slots **56** (stones) and **55** (water) — gym-6 town sits below the Sango/early-tier holders. Confirm the render order reads correctly in-run; the next free descending slots are 54 and below if a nudge is needed.
