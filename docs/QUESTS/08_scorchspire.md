# 08 — Scorchspire (Gym 10, Fire) — TOWN quest unit

> **Slug:** `08_scorchspire` · **Town:** Scorchspire · **Gym:** 10 (Fire, Leader Vulcan) ·
> **Entry cap:** 80 (Nifl unlock) · **Unlock cap:** 80→ *the last town before the Royal League*.
> **Band:** `frag_10` "face your own signature" · `cd_instability = 25` (flat, stabilised) ·
> **Recognition tier:** **late (7+)**. Approached via **R15 Cinderfall Descent**. Nearby:
> **Fire Shrine [3534 4687]**, **Ashloam farm [3304 4004]**, **Volcano Peak [3805 64 3746]**.
> Forward gate: **Road to Royal League [3545 2460]** → **Royal League [3528 166 2773]** → Battle Frontier tease.

This unit designs the **town-side** layer of Scorchspire: a rumor hub, four side quests
(forge/tempering/a smith who knew the founder/finality), the `frag_10` town-side echo, a
**late** recognition beat that all but names the founder without closing it, and a forward
hook to the League/Frontier. It does **not** re-author the gym ladder or Vulcan's dialog —
those already exist (`characters/gym/scorchspire_*`, `dialog/gym_leader_scorchspire.json`).
It also does **not** own the Groudon noble (owned by `legendaries_nobles`) or the Fire
Shrine internals (owned by `shrines_audit`); those are referenced as hooks only.

---

## 1. Overview

Scorchspire is a **volcanic forge town** welded onto a live spire. It is the **last exhale
before the climb** — the tenth badge, then the Descent to the League, then the answer the
whole run has circled. The town's craft is **forging/tempering steel**, and every quest
here rhymes on the same word the fragment does: **signature**. Forged steel is *signed*
steel; the Company's leftover paperwork here is *a charter with the founder's signature
scrubbed to a blank*; `frag_10` is "face your own signature."

**Band context.** DJ fell after gym 7; the currency is **stabilised at idx 25** (this town
fires **no** `gym_destabilize`). The Company is a **skeleton crew** here — the recognition
tier is **late**, so agents either **stand down** rather than raise a hand against the
founder, or **panic and double down** on the official line ("there was never a founder").
Civilians never recognise him; they only feel the propaganda decay and the *quiet* of a
Company that used to run this forge and now cannot even cancel its own dead contracts.

**Arc job.** This is the **finality** town. Its themes — forging, tempering, banking the
last coal, a smith who *knew the founder's hand* — all point the player inward at the coming
mirror. The `frag_10` town-echo says it plainly: everything points one direction now, and
it is inward. The unit's forward pull is relentless: every arc names the **Cinderfall
Descent → Road to Royal League → the League**, and one quest teases the **Battle Frontier**
as the thing waiting *after* the answer.

**Route placement.** Enter from **R15 Cinderfall Descent** (from Nifl). Leave down the
**Road to Royal League [3545 2460]** to the **Royal League [3528 166 2773]**. Scorchspire
is the terminal town of the gym spine — after it there is no more town before the climb.

---

## 2. Cast

All town NPCs are **new**; the gym-interior ladder (Vulcan, guide, 4 trainers, jr, apprentice)
already exists in `characters/gym/` and is **not** re-authored here. Coords marked
**PLACEHOLDER** need a builder pass; the atlas gives the town centroid ≈ **(3677, 68, 4573)**
and the north gate toward Volcano Peak ≈ **(3700, 70, 4470)**.

| id | display_name | role | concept | placement anchor |
|---|---|---|---|---|
| `scorchspire_healer` | Nurse Ember | healer | **RUMOR HUB.** Town clinic nurse; points at every open Scorchspire quest, echoes `frag_10`, and gives the League-road forward hook. | (3672, 68, 4576) **PLACEHOLDER** (clinic, town center) |
| `sq_forge_sena` | Forgemaster Sena | quest_giver | Vulcan's forge-hand. Runs the staged **burn-the-dead-requisition** walk; the third ledger is the founder's blanked charter. | (3670, 68, 4560) **PLACEHOLDER** (town forge) |
| `sq_recovery_agent` | Asset Recovery Agent Kessler | villain_grunt | Late-tier agent posted to collect the last forge shipment; recognises the *stance*, then stands down or flees. Carries SQ1's opt-in fight. | (3665, 68, 4552) **PLACEHOLDER** (by the forge) |
| `sq_temper_hollis` | Bladesmith Hollis | quest_giver | Old bladesmith running a **tempering** trial: bank the coals, cool at the right beat. A wager on his own edge, decline-able. | (3684, 68, 4588) **PLACEHOLDER** (forge annex) |
| `sq_oldsmith_marren` | Old Marren | elder | The **smith who knew the founder** — remembers the *hand*, not the name. The late recognition set-piece that all but names it. | (3660, 68, 4600) **PLACEHOLDER** (hearth at the spire base) |
| `sq_severance` | Clerk Severance | villain_management | Defector clerk carrying the "the founder retired" memo trail; Asset Recovery followed her. | (3620, 66, 4660) **PLACEHOLDER** (shrine road) |
| `sq_asset_recovery_lead` | Recovery Lead Vance | villain_grunt | Carries the SQ4 **joint doubles** ambush; despawns on win. | (3620, 66, 4658) **PLACEHOLDER** (flanks Severance) |
| `sq_asset_recovery_second` | Recovery Agent Doss | villain_grunt | Stands down in dialog on the shared defeat tag (no battle block of its own). | (3620, 66, 4662) **PLACEHOLDER** (flanks Severance) |

> The **Groudon** hook NPC (`sq_cinderwatch_aya`) + crater monument + wardens are **owned
> by `legendaries_nobles`** per the roadmap; Nurse Ember only *points* at the crater as a
> rumor. The **Fire Shrine** signpost is owned by `shrines_audit`; Ember points at the
> shrine road as a rumor too. Neither is authored in this unit.

---

## 3. Quests

Four town quests: **SQ1 forge** (signature), **SQ2 tempering** (bank the coals), **SQ3 the
old smith** (late recognition), **SQ4 retirement package** (the marquee tag-team double +
memo). All are fail-soft; the only above-nothing fights are opt-in with printed stakes.

Reusable helper commands used below (per memory + Genji gold example):
- `function cobblemon_initiative:economy/payout {amount:N}` — skewed CD payout + receipt.
- `loot give @s loot cobblemon_initiative:npc_gift/<table>` — gift latch (`training_major` exists).
- Item **count-check via clear-with-0** (never a `has_item` gate) — the Genji `turn_in_rod` idiom.

---

### SQ1 — The Last Forge Order  *(forge / signature)*

**Concept.** The Company put a standing **commodity requisition** on Scorchspire's forge —
all its steel contracted to build granary silos and field fences. DJ fell; the contract is
void; the forge still runs the dead order because nobody dared cancel a Company seal.
Forgemaster Sena walks you the forge line and you **burn three requisition ledgers** in the
crucible (staged tag chain on Sena's entity, ZERO block edits, per the Tomo seal-walk). The
**third ledger is the founder's own charter page — his signature line scrubbed to a blank.**
Then Asset Recovery Agent Kessler steps in to collect the last shipment: an **opt-in** fight
or a talk-past.

- **Forward hook:** Sena reforges the blanked charter into a nameless ingot — "a signature
  you can hold instead of read" — and tells you the only place left to read your own hand is
  *past the League, down the Cinderfall Descent.*
- **Back-echo:** references DJ's fall ("the seal on this order is the Acting CEO's, and the
  Acting CEO is filed now") and the wheat war ("granary silos this steel never has to build").

**Character JSON** (`dialog-src/characters/scorchspire/sq_forge_sena.json`):

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "sq_forge_sena",
  "display_name": "Forgemaster Sena",
  "role": "quest_giver",
  "act": "3",
  "location": "Scorchspire - The Forge",
  "recognition_tier": "late",
  "recipe": "civilian",
  "dialog": "dialog:sq_forge_order",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 3670, "y": 68, "z": 4560 }
}
```

**Character JSON** (`dialog-src/characters/scorchspire/sq_recovery_agent.json`) — carries the opt-in fight:

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "sq_recovery_agent",
  "display_name": "Recovery Agent Kessler",
  "role": "villain_grunt",
  "act": "3",
  "location": "Scorchspire - The Forge",
  "recognition_tier": "late",
  "trainer": "sq_recovery_agent",
  "recipe": "villain_grunt",
  "dialog": "dialog:sq_recovery_agent",
  "movement": { "objective": "ambient_stationary_look" },
  "battle": {
    "trainer": "sq_recovery_agent",
    "type": "villain_grunt",
    "format": "GEN_9_SINGLES",
    "prize": 600,
    "decline_fee": 250,
    "defeat_tag": "defeated_sq_recovery_agent",
    "despawn_on_win": true,
    "win_line": "Shipment lost. Note for the file: the subject sets their feet like someone who used to sign these. I am reassigning myself before I have to write that down.",
    "lose_line": "Order collected. The Company records this cooperation and thanks the forge.",
    "already_beaten_line": "The shipment is a writeoff and so, apparently, am I. Nothing left here to recover.",
    "on_win": [ "tag @1 add forge_order_agent_clear" ]
  },
  "placement": { "x": 3665, "y": 68, "z": 4552 }
}
```

**Dialog JSON** (`dialog-src/dialog/sq_forge_order.json`) — staged burn-walk on Sena, Tomo idiom:

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "sq_forge_order",
  "type": "STANDARD",
  "entries": [
    {
      "label": "done_flavor",
      "name": "Sena - the nameless ingot",
      "priority": 30,
      "gate": { "tag": "forge_order_done" },
      "say": [
        "The dead order is ash and the ingot is yours. A signature you can hold instead of read. Whatever hand it belonged to, it points the same way you do now - down the Cinderfall Descent, past the League.",
        "Steel remembers who tempered it long after the name burns off. Yours will too."
      ],
      "buttons": [ { "label": "leave_button", "text": "Down the Descent, then", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "agent_wait",
      "name": "Sena - one collector left",
      "priority": 25,
      "gate": { "all_tags": [ "forge_order_3" ], "not_tags": [ "forge_order_done" ] },
      "say": [
        "Three ledgers, three fires. But there is a man by the quench posted to collect the last shipment - Asset Recovery. He will not start it. Head office privilege, starting things. Settle him and the order dies for good."
      ],
      "buttons": [
        { "label": "resume_button", "text": "Deal with the collector", "actions": [ { "do": "open_dialog", "label": "burn_three" } ] },
        { "label": "leave_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "resume_two",
      "name": "Sena - one ledger left",
      "priority": 14,
      "gate": { "all_tags": [ "forge_order_2" ], "not_tags": [ "forge_order_3" ] },
      "say": [ "One ledger left, and it is the ugly one - a charter page. There is a signature line on it, and someone has scrubbed the name to a clean white blank. Burn it with me." ],
      "buttons": [
        { "label": "resume_button", "text": "The last ledger", "actions": [ { "do": "open_dialog", "label": "burn_three" } ] },
        { "label": "leave_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "resume_one",
      "name": "Sena - two ledgers left",
      "priority": 13,
      "gate": { "all_tags": [ "forge_order_1" ], "not_tags": [ "forge_order_2" ] },
      "say": [ "Two to go. The second is the silo schedule - granary steel we were never going to forge. Into the fire." ],
      "buttons": [
        { "label": "resume_button", "text": "Second ledger", "actions": [ { "do": "open_dialog", "label": "burn_two" } ] },
        { "label": "leave_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Forgemaster Sena",
      "priority": 10,
      "default": true,
      "gate": { "not_tag": "forge_order_1" },
      "say": [
        "Sena. I keep the Vulcan forge fed. The Company put a standing requisition on us years back - all our steel, contracted to silos and field fences. The seal on it belongs to the Acting CEO, and the Acting CEO is filed now. The order is dead and the forge still runs it because nobody dared cancel a Company seal. Burn the ledgers with me and it ends."
      ],
      "buttons": [
        { "label": "walk_button", "text": "Burn the order", "actions": [ { "do": "open_dialog", "label": "burn_one" } ] },
        { "label": "later_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "burn_one",
      "name": "First ledger - the intake",
      "priority": -1,
      "say": [ "First ledger. Weekly intake quotas, stamped Verified. The crucible does not verify anything. Good." ],
      "buttons": [
        { "label": "burn_one_button", "text": "Feed it to the crucible", "actions": [
          { "do": "command", "cmd": "tag @s add forge_order_1", "as_player": true },
          { "do": "announce", "text": "First ledger burned. The forge draft steadies.", "as": "actionbar" },
          { "do": "open_dialog", "label": "burn_two" }
        ] },
        { "label": "step_back_button", "text": "Step back", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "burn_two",
      "name": "Second ledger - the silo schedule",
      "priority": -1,
      "say": [ "Second ledger. Silo build schedule - steel for the granaries that were meant to replace the money. We forged none of it. Watch it curl." ],
      "buttons": [
        { "label": "burn_two_button", "text": "Feed it to the crucible", "actions": [
          { "do": "command", "cmd": "tag @s add forge_order_2", "as_player": true },
          { "do": "announce", "text": "Second ledger burned. The granary steel that never was.", "as": "actionbar" },
          { "do": "open_dialog", "label": "burn_three" }
        ] },
        { "label": "step_back_button", "text": "Step back", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "burn_three",
      "name": "The charter page - and the collector",
      "priority": -1,
      "say": [
        "Third ledger. A charter page, foundational. There is a signature line and the name has been scrubbed to a clean blank - like they took a hot iron to it. And there, by the quench, is the man they sent to collect the last shipment. He will not start anything. Your move."
      ],
      "buttons": [
        { "label": "face_agent_button", "text": "Turn to the collector", "gate": { "not_tag": "forge_order_agent_clear" }, "actions": [
          { "do": "announce", "text": "The Asset Recovery agent is by the quench. Speak to Recovery Agent Kessler.", "as": "actionbar" },
          { "do": "close" }
        ] },
        { "label": "burn_charter_button", "text": "Burn the blank charter", "gate": { "tag": "forge_order_agent_clear" }, "actions": [
          { "do": "command", "cmd": "tag @s add forge_order_3", "as_player": true },
          { "do": "command", "cmd": "function cobblemon_initiative:sidequest/forge_order/finish", "as_player": true },
          { "do": "close" }
        ] }
      ],
      "no_goodbye": true
    }
  ]
}
```

**Dialog JSON** (`dialog-src/dialog/sq_recovery_agent.json`) — opt-in fight or talk-past, late recognition:

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "sq_recovery_agent",
  "type": "STANDARD",
  "entries": [
    {
      "label": "default",
      "name": "Recovery Agent Kessler",
      "priority": 10,
      "default": true,
      "gate": { "not_tag": "forge_order_agent_clear" },
      "say": [
        "Asset Recovery. There is one shipment on the Scorchspire forge unaccounted for and I am here to collect it. You are standing in front of it. I am not permitted to start anything, but I am permitted to finish it.",
        "You set your feet like someone who used to sign these orders. I do not want to think about who does that. So do not make me. Walk, and I will call the shipment lost."
      ],
      "buttons": [
        { "label": "fight_button", "text": "Face him - opt in, no fee to win", "actions": [ { "do": "battle" } ] },
        { "label": "decline_button", "text": "Pay him off and walk - 250 CD", "actions": [
          { "do": "command", "cmd": "function cobblemon_initiative:route/decline_sq_recovery_agent", "as_player": true },
          { "do": "close" }
        ] }
      ],
      "no_goodbye": true
    },
    {
      "label": "cleared",
      "name": "Kessler - reassigned",
      "priority": 20,
      "gate": { "tag": "forge_order_agent_clear" },
      "say": [ "The shipment is a writeoff. So am I, probably. There is nothing left to recover here - go finish the fire." ]
    }
  ]
}
```

**DATAPACK NEEDS:**
- `function/sidequest/forge_order/finish.mcfunction` — latch: `tag @s add forge_order_done`;
  `function .../economy/payout {amount:600}`; `loot give @s loot .../npc_gift/training_major`;
  `give @s minecraft:charcoal 8`; `title @s actionbar` receipt "THE LAST ORDER BURNS".
- `function/route/decline_sq_recovery_agent.mcfunction` — pay-probe decline (mirror the
  existing `route/decline_sq_genji_wager` idiom): charge 250 CD only if affordable, set
  `forge_order_agent_clear`, actionbar "Shipment written off. He does not look sorry."; if
  unaffordable, actionbar "You cannot cover the writeoff - face him or come back." (fail-soft).

**QUEST_TARGETS** (`registers/quest_targets.json` → `quests[]`):

```json
{
  "holder": "q.side_forgeorder",
  "name": "The Last Forge Order",
  "slot": 82,
  "stages": [
    { "if_tags": ["forge_order_3"], "not_tags": ["forge_order_done"], "label": "Burn the blank charter with Sena", "target": { "npc": "sq_forge_sena" } },
    { "if_tags": ["forge_order_agent_clear"], "not_tags": ["forge_order_3"], "label": "Return to Sena and burn the charter", "target": { "npc": "sq_forge_sena" } },
    { "if_tags": ["forge_order_2"], "not_tags": ["forge_order_agent_clear"], "label": "Settle the Asset Recovery collector", "target": { "npc": "sq_recovery_agent" } },
    { "if_tags": ["forge_order_1"], "not_tags": ["forge_order_2"], "label": "Burn the requisition ledgers with Sena", "target": { "npc": "sq_forge_sena" } },
    { "if_tags": ["defeated_scorchspire_guide"], "not_tags": ["forge_order_1"], "label": "The forge runs a dead Company order - see Forgemaster Sena", "target": { "npc": "sq_forge_sena" }, "note": "Activation is loose - swap the if_tag to whatever town-entry breadcrumb the spine uses; the intent is available on arrival." }
  ]
}
```

**REWARD/BALANCE:** win **600 CD** (payout) + `training_major` latch + charcoal x8 keepsake.
No above-cap battle: Kessler's team ≈ **73 (ace, under cap 80)**, `GEN_9_SINGLES`. Fight is
**opt-in**; declining costs **250 CD** (pay-probe, fail-soft — a player who cannot pay is
prompted to fight or leave, never soft-locked). Fairness floor honored (no forced whiteout).

---

### SQ2 — The Tempering  *(banked coals / patience)*

**Concept.** Bladesmith Hollis runs the town's **tempering** trial: heat is easy, *timing*
is everything — you bank the coals, then cool the edge at the right beat. He teaches it as a
metaphor for the coming climb ("temper too fast and the blade shatters at the first strike;
that is what the League is for — it tempers you"). The player brings **8 iron ingots** (the
raw stock) via a **count-check turn-in**, Hollis quenches a blade with them and gifts a
**tempered keepsake**; then he offers a **decline-able, cap-legal wager** on his own edge —
his two fire types against yours, stake printed.

- **Forward hook:** Hollis: "Temper is what stands between raw and ready. So does the League.
  Down the Descent, then up the Royal Road." (Names the Descent + Royal League.)
- **Back-echo:** references the **Nifl** cold ("you came down off the ice — good; cold
  makes brittle steel, and heat makes it whole again") and the **Banked Coals** gym gimmick
  ("Vulcan banks his coals; so do I").

**Character JSON** (`dialog-src/characters/scorchspire/sq_temper_hollis.json`):

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "sq_temper_hollis",
  "display_name": "Bladesmith Hollis",
  "role": "quest_giver",
  "act": "3",
  "location": "Scorchspire - Forge Annex",
  "recognition_tier": "late",
  "trainer": "sq_temper_hollis",
  "recipe": "civilian",
  "dialog": "dialog:sq_tempering",
  "movement": { "objective": "ambient_stationary_look" },
  "battle": {
    "trainer": "sq_temper_hollis",
    "type": "wager",
    "format": "GEN_9_SINGLES",
    "prize": 500,
    "loss_fee": 500,
    "decline_fee": 0,
    "defeat_tag": "defeated_sq_temper_hollis",
    "win_line": "Ha. Tempered. The edge held and so did you. Five hundred, fairly quenched.",
    "lose_line": "The blade cracked at the strike. No shame in it - come back cooled and try the edge again. The stake stays with the forge.",
    "already_beaten_line": "One quench a visit. Bank your coals and come see me after the League."
  },
  "placement": { "x": 3684, "y": 68, "z": 4588 }
}
```

**Dialog JSON** (`dialog-src/dialog/sq_tempering.json`) — turn-in then opt-in wager, Genji idiom:

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "sq_tempering",
  "type": "STANDARD",
  "entries": [
    {
      "label": "post_wager",
      "name": "Hollis - the tempered edge",
      "priority": 40,
      "gate": { "defeated": "sq_temper_hollis" },
      "say": [
        "You quench well. Raw steel and raw trainers both go in loud and come out quiet - if the timing is kind. Down the Descent the League will temper the rest of you.",
        "Vulcan banks his coals on the stair. I bank mine in the quench. Same craft, different fire."
      ],
      "buttons": [ { "label": "leave_button", "text": "To the Descent", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "wager",
      "name": "Hollis - test the edge",
      "priority": 30,
      "gate": { "tag": "temper_blade_done", "not_tag": "defeated_sq_temper_hollis" },
      "say": [
        "That keepsake took the heat well. Now the other kind of tempering: a friendly wager, smith to challenger. Five hundred CobbleDollars says my two fire-hearts crack your line before the second strike. Lose and the stake stays with the forge; decline and we part cooled and friendly. No fee to walk.",
        "No pressure. The quench is patient and so am I."
      ],
      "buttons": [
        { "label": "wager_button", "text": "Take the wager - 500 CD on the edge", "actions": [ { "do": "battle" } ] },
        { "label": "decline_button", "text": "Not this quench", "actions": [ { "do": "close" } ] }
      ],
      "no_goodbye": true
    },
    {
      "label": "default",
      "name": "Bladesmith Hollis",
      "priority": 10,
      "default": true,
      "gate": { "not_tag": "temper_blade_done" },
      "say": [
        "Hollis. I temper blades - heat is the easy half, timing is the craft. You came down off the Nifl ice, and cold makes brittle steel; heat makes it whole. Bring me eight iron ingots and I will quench you a keepsake to carry into the League. Temper too fast and the blade shatters at the first strike. That is what the League is for. It tempers you.",
        "Iron off the Kalahar deeps, or any ore you have banked. Eight ingots, and the forge does the rest."
      ],
      "buttons": [
        { "label": "turn_in_button", "text": "Hand over 8 iron ingots", "actions": [
          { "do": "command", "cmd": "function cobblemon_initiative:sidequest/tempering/turn_in", "as_player": true },
          { "do": "close" }
        ] },
        { "label": "leave_button", "text": "I will go bank some ore", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:**
- `function/sidequest/tempering/turn_in.mcfunction` — count-check via clear-with-0 (Genji
  idiom): `execute store result score @s ci_sq_scratch run clear @s minecraft:iron_ingot 0`;
  if `matches 8..` and not `temper_blade_done` → `function .../tempering/quench`; else a gray
  "Hollis counts the ingots - not eight yet" tellraw prompt.
- `function/sidequest/tempering/quench.mcfunction` — `clear @s minecraft:iron_ingot 8`;
  `function .../economy/payout {amount:400}`; `loot give @s loot .../npc_gift/training_standard`;
  `give @s minecraft:netherite_scrap 1` **OR** a Charcoal held item (keepsake — pick in build);
  `tag @s add temper_blade_done`; actionbar "Tempered. Ready for the strike."

**QUEST_TARGETS:**

```json
{
  "holder": "q.side_tempering",
  "name": "The Tempering",
  "slot": 83,
  "stages": [
    { "if_tags": ["temper_blade_done"], "not_tags": ["defeated_sq_temper_hollis"], "label": "Test the tempered edge against Hollis (optional wager)", "target": { "npc": "sq_temper_hollis" } },
    { "if_tags": ["defeated_scorchspire_guide"], "not_tags": ["temper_blade_done"], "label": "Bring Bladesmith Hollis 8 iron ingots to temper a keepsake", "target": { "npc": "sq_temper_hollis" }, "note": "Swap the entry if_tag for the town-entry breadcrumb the spine uses." }
  ]
}
```

**REWARD/BALANCE:** turn-in pays **400 CD** + `training_standard` + tempered keepsake. The
wager pays **500 CD** on win / **500 CD** stake on loss, `GEN_9_SINGLES`, ace **≈76 (under
cap 80)**. Wager is **opt-in** with the stake printed; **decline is free** (`decline_fee:0`) —
it is a friendly-theme wager, not a Company encounter (§5 exempts friendly wagers from the
decline-fee rule; the Genji wager sets a nonzero fee but that was a for-profit auditor —
Hollis is a townsman, free to walk). Cap-legal.

---

### SQ3 — The Hand That Signed It  *(the smith who knew the founder — late recognition)*

**Concept.** Old Marren is the town's oldest smith. Decades ago he forged **branch-office
door plates** for the Company — and every plate carried the **founder's countersignature
stamped into the steel**. He remembers the *hand*, not the face, not the name (the scrubbing
took those). He asks the player to help him find the **last surviving plate** — buried in the
forge slag heap — because the Company's crew came through and pulled every one they could.
When the player brings it (a **count-check turn-in** of the plate item, spawned as a findable
prop-drop, or simply an in-dialog recovery), Marren sets it against the player's stance and
goes quiet: the hand that signed the plate and the hand in front of him **move the same**.
He **all but names it** — and then deliberately does not.

- **Forward hook:** Marren: "Whatever hand this was, it went past the League and never came
  back. If you mean to read it, that is the road - down the Descent." (Names the Descent.)
- **Back-echo:** references **`frag_7` "you signed this charter"** ("charters, plates, deeds -
  the same hand signed all of them, and you flinch every time I say sign") and the HQ raid
  ("the crew that pulled the plates worked for a man who is filed now").

**Character JSON** (`dialog-src/characters/scorchspire/sq_oldsmith_marren.json`):

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "sq_oldsmith_marren",
  "display_name": "Old Marren",
  "role": "elder",
  "act": "3",
  "location": "Scorchspire - The Hearth",
  "recognition_tier": "late",
  "recipe": "civilian",
  "dialog": "dialog:sq_the_hand",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 3660, "y": 68, "z": 4600 }
}
```

**Dialog JSON** (`dialog-src/dialog/sq_the_hand.json`) — recovery + the late recognition beat:

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "sq_the_hand",
  "type": "STANDARD",
  "entries": [
    {
      "label": "post_reveal",
      "name": "Marren - the same hand",
      "priority": 40,
      "gate": { "tag": "the_hand_done" },
      "say": [
        "I have set steel against steel for sixty years, and I know when two came off the same hand. The plate, and you. I am not going to say the name they scrubbed. I am old, not foolish, and it is not mine to hand you. But you already know which way to look. Inward. Down the Descent, past the League.",
        "Carry the plate. When you are ready to read the hand that made it, you will not need me to tell you whose it is."
      ],
      "buttons": [ { "label": "leave_button", "text": "I will read it myself", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "recovered",
      "name": "Marren - set it down",
      "priority": 30,
      "gate": { "tag": "the_hand_plate", "not_tag": "the_hand_done" },
      "say": [ "You found one. Set it on the anvil. Let me see the mark. ...Ah. There it is. Stand where you are - do not move your feet. Look at the plate. Now look at how you are standing." ],
      "buttons": [
        { "label": "look_button", "text": "Look at the plate", "actions": [
          { "do": "command", "cmd": "function cobblemon_initiative:sidequest/the_hand/reveal", "as_player": true },
          { "do": "open_dialog", "label": "post_reveal" }
        ] }
      ],
      "no_goodbye": true
    },
    {
      "label": "searching",
      "name": "Marren - the slag heap",
      "priority": 20,
      "gate": { "tag": "the_hand_started", "not_tag": "the_hand_plate" },
      "say": [ "The crew pulled every plate they could reach and melted them for scrap - erasing the countersignature, plate by plate. But one went into the slag heap by mistake, half-slagged, the mark still legible. Dig it out and bring it here." ],
      "buttons": [
        { "label": "turn_in_button", "text": "Hand over the door plate", "actions": [
          { "do": "command", "cmd": "function cobblemon_initiative:sidequest/the_hand/turn_in", "as_player": true },
          { "do": "close" }
        ] },
        { "label": "leave_button", "text": "Still digging", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Old Marren",
      "priority": 10,
      "default": true,
      "gate": { "not_tag": "the_hand_started" },
      "say": [
        "Marren. Oldest bellows in Scorchspire. I forged door plates for every Company branch office there ever was, and every plate carried the founder countersignature stamped right into the steel. I never met the man. I knew the hand. Charters, plates, deeds - the same hand signed all of them, and you flinch every time I say sign.",
        "The crew came through and pulled the plates. Melting the mark off, one by one. One went into my slag heap half-slagged, the stamp still readable. Find it for me. I want to hold the hand one more time before the League swallows the last of it."
      ],
      "buttons": [
        { "label": "accept_button", "text": "Help him find the plate", "actions": [
          { "do": "command", "cmd": "tag @s add the_hand_started", "as_player": true },
          { "do": "command", "cmd": "function cobblemon_initiative:sidequest/the_hand/place_plate", "as_player": true },
          { "do": "announce", "text": "Dig the half-slagged door plate out of the forge slag heap and bring it to Marren.", "as": "actionbar" },
          { "do": "close" }
        ] },
        { "label": "later_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:**
- `function/sidequest/the_hand/place_plate.mcfunction` — spawn/reveal the findable plate
  item at the slag-heap prop coord (a dropped item entity or a loot latch the player picks
  up); one-shot per `the_hand_started`. Keep it ZERO block-edit — use a placed item entity
  or a `loot give` on approach, showrunner's pick. Plate item id = a renamed
  `minecraft:iron_ingot`/`minecraft:heavy_core` (JAR-VALIDATE; pick a stand-in in build).
- `function/sidequest/the_hand/turn_in.mcfunction` — count-check via clear-with-0 for the
  plate item (1x); on success `tag @s add the_hand_plate`; else a gray "Marren shakes his
  head - no plate in hand yet" prompt.
- `function/sidequest/the_hand/reveal.mcfunction` — clear the plate item; `tag @s add the_hand_done`;
  `function .../economy/payout {amount:500}`; `loot give @s loot .../npc_gift/training_major`;
  set breadcrumb `tag @s add scrubbing_artifact_plate` (feeds the Board/Founder reveal chain);
  title sting: `title @s title` "THE SAME HAND" (subtitle "you flinch every time I say sign").
  **No name, no `§k` reveal — this is a circle, not a close.**

**QUEST_TARGETS:**

```json
{
  "holder": "q.side_thehand",
  "name": "The Hand That Signed It",
  "slot": 84,
  "stages": [
    { "if_tags": ["the_hand_plate"], "not_tags": ["the_hand_done"], "label": "Set the door plate on the Marren anvil", "target": { "npc": "sq_oldsmith_marren" } },
    { "if_tags": ["the_hand_started"], "not_tags": ["the_hand_plate"], "label": "Dig the half-slagged door plate from the forge slag heap", "target": { "x": 3656, "y": 66, "z": 4604 }, "note": "Literal slag-heap prop coord PLACEHOLDER - set to the placed plate item entity." },
    { "if_tags": ["defeated_scorchspire_guide"], "not_tags": ["the_hand_started"], "label": "Old Marren remembers the hand that signed the plates - hear him out", "target": { "npc": "sq_oldsmith_marren" }, "note": "Swap the entry if_tag for the town-entry breadcrumb the spine uses." }
  ]
}
```

**REWARD/BALANCE:** **500 CD** + `training_major` latch + the plate keepsake (lore) + story
breadcrumb `scrubbing_artifact_plate`. **No battle** — this is a recognition/lore beat. No
cap concerns. The reveal **circles the founder identity** (same hand) and explicitly refuses
to name it (reveal is post-League, Act 3). Civilian-adjacent elder: Marren recognises the
*hand/stance*, never states the CEO identity — consistent with the canon that only the world,
not civilians, half-knows, and the truth waits for the mirror.

---

### SQ4 — Retirement Package  *(late recognition + the marquee tag-team double)*

**Concept.** Clerk Severance fled to the last town before the League after DJ fell, carrying
the **internal memo trail** proving "the founder retired" was a fabrication — a scrubbing
artifact. She wants to hand it off and vanish. **Asset Recovery** followed her: a **joint
`GEN_9_DOUBLES` tag-team** — Recovery Lead Vance carries the battle, Recovery Agent Doss
stands down in dialog on the shared defeat tag (the `agent_yield_lead`/`_second` pattern).
Win → she hands over the memo and disappears east.

- **Forward hook:** Severance: "I hear there is a dragon out east that does not file anyone -
  past the League, past the map. That is where I am going. You are going the other way. Down."
  (Teases the **post-game vanilla / Ender Dragon** + names the Descent — the Battle Frontier
  tease is carried by Nurse Ember's rumor hub, below.)
- **Back-echo:** references the **HQ raid** ("after your people stormed HQ, half of us just...
  stopped showing up") and **`frag_9` "they emptied you"** ("the memo says the founder retired;
  the truth is closer to the word they used for you at Nifl - emptied").

**Character JSON** (`dialog-src/characters/scorchspire/sq_severance.json`):

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "sq_severance",
  "display_name": "Clerk Severance",
  "role": "villain_management",
  "act": "3",
  "location": "Scorchspire - Shrine Road",
  "recognition_tier": "late",
  "recipe": "management",
  "dialog": "dialog:sq_retirement_package",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 3620, "y": 66, "z": 4660 }
}
```

**Character JSON** (`dialog-src/characters/scorchspire/sq_asset_recovery_lead.json`) — carries the double:

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "sq_asset_recovery_lead",
  "display_name": "Recovery Lead Vance",
  "role": "villain_grunt",
  "act": "3",
  "location": "Scorchspire - Shrine Road",
  "recognition_tier": "late",
  "trainer": "sq_asset_recovery",
  "recipe": "villain_grunt",
  "dialog": "dialog:sq_asset_recovery_lead",
  "battle": {
    "trainer": "sq_asset_recovery",
    "type": "villain_grunt",
    "format": "GEN_9_DOUBLES",
    "prize": 700,
    "defeat_tag": "defeated_sq_asset_recovery",
    "despawn_on_win": true,
    "win_line": "Recovery failed. Filing under - there was never a founder, so there is no one to recover from. That is the line. I am sticking to the line.",
    "lose_line": "The clerk is reassigned. The memo is Company property. Nothing here was ever real.",
    "already_beaten_line": "The account is closed. Do not append yourself to it.",
    "on_win": [ "tag @1 add asset_recovery_clear" ]
  },
  "placement": { "x": 3620, "y": 66, "z": 4658 }
}
```

**Character JSON** (`dialog-src/characters/scorchspire/sq_asset_recovery_second.json`) — stands down, no battle block:

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "sq_asset_recovery_second",
  "display_name": "Recovery Agent Doss",
  "role": "villain_grunt",
  "act": "3",
  "location": "Scorchspire - Shrine Road",
  "recognition_tier": "late",
  "recipe": "villain_grunt",
  "dialog": "dialog:sq_asset_recovery_second",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 3620, "y": 66, "z": 4662 }
}
```

**Dialog JSON** (`dialog-src/dialog/sq_retirement_package.json`) — Severance giver:

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "sq_retirement_package",
  "type": "STANDARD",
  "entries": [
    {
      "label": "done",
      "name": "Severance - going east",
      "priority": 40,
      "gate": { "tag": "retirement_memo_taken" },
      "say": [
        "You have the memo. That is the last of me the Company gets to lose. I hear there is a dragon out east that does not file anyone - past the League, past the map. That is my retirement. You are going the other way. Down the Descent, into whatever is waiting with your face on it.",
        "For what it is worth - the memo says the founder retired. Nobody who wrote it believed it. Read it when you are ready to."
      ],
      "buttons": [ { "label": "leave_button", "text": "Go safe, then", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "handoff",
      "name": "Severance - take the memo",
      "priority": 30,
      "gate": { "tag": "asset_recovery_clear", "not_tag": "retirement_memo_taken" },
      "say": [ "They are down. You bought me the door. Here - the whole trail. Every memo from retired to there was never a founder, in order, dated. It is a scrubbing job caught in the act. Take it and let me disappear." ],
      "buttons": [
        { "label": "take_memo_button", "text": "Take the memo trail", "actions": [
          { "do": "command", "cmd": "function cobblemon_initiative:sidequest/retirement/take_memo", "as_player": true },
          { "do": "open_dialog", "label": "done" }
        ] }
      ],
      "no_goodbye": true
    },
    {
      "label": "ambush",
      "name": "Severance - they found me",
      "priority": 20,
      "gate": { "tag": "severance_met", "not_tag": "asset_recovery_clear" },
      "say": [ "Too late - Asset Recovery followed me down the shrine road. Two of them, and they came to collect the memo and me with it. I cannot fight. If you can, the memo is yours the second they are down." ],
      "buttons": [ { "label": "leave_button", "text": "Stand between them and her", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "default",
      "name": "Clerk Severance",
      "priority": 10,
      "default": true,
      "gate": { "not_tag": "severance_met" },
      "say": [
        "Keep your voice down. I am Company - was Company - Records. After your people stormed HQ, half of us just stopped showing up. I took something with me on the way out. The memo trail on the founder. Retired, then reassigned, then there was never a founder - the whole cover-up, in dated order. The word they used internally for what happened to him is closer to emptied than retired.",
        "I want it in hands that are not theirs, and then I want to be nobody, somewhere east. Take it - if Asset Recovery has not already caught up with me."
      ],
      "buttons": [
        { "label": "accept_button", "text": "I will take the memo", "actions": [
          { "do": "command", "cmd": "tag @s add severance_met", "as_player": true },
          { "do": "announce", "text": "Asset Recovery is closing on the shrine road. Speak to Recovery Lead Vance.", "as": "actionbar" },
          { "do": "close" }
        ] },
        { "label": "later_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**Dialog JSON** (`dialog-src/dialog/sq_asset_recovery_lead.json`) — the ambush battle (late recognition):

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "sq_asset_recovery_lead",
  "type": "STANDARD",
  "entries": [
    {
      "label": "default",
      "name": "Recovery Lead Vance",
      "priority": 10,
      "default": true,
      "gate": { "not_tag": "defeated_sq_asset_recovery" },
      "say": [
        "Two-agent recovery. The clerk and the paper both come with us, and I know that stance - I filed the memo about that stance. You are supposed to be filed. You are supposed to be a description we were told to forget. So I am going to forget you again, loudly. Step aside or do not.",
        "Recovery Lead, two suits, one clerk to collect. But you stand like someone who used to sign the orders I carry out, and the memo said that face does not exist. I am going to prove the memo right. Step aside or do not."
      ],
      "buttons": [
        { "label": "fight_button", "text": "Fight them - protect Severance", "actions": [ { "do": "battle" } ] }
      ],
      "no_goodbye": true
    },
    {
      "label": "beaten",
      "name": "Vance - the line holds",
      "priority": 20,
      "gate": { "tag": "defeated_sq_asset_recovery" },
      "say": [ "There was never a founder, so there is no one standing there. That is the line. I am keeping the line, all the way back to nothing." ]
    }
  ]
}
```

**Dialog JSON** (`dialog-src/dialog/sq_asset_recovery_second.json`) — stands down on shared tag:

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "sq_asset_recovery_second",
  "type": "STANDARD",
  "entries": [
    {
      "label": "stand_down",
      "name": "Doss - reassigned",
      "priority": 30,
      "gate": { "tag": "defeated_sq_asset_recovery" },
      "say": [ "I am not raising a hand. Whatever the memo says, I saw the portrait come down. I am requesting reassignment to somewhere that does not involve you. Take the clerk. Take the paper. I was never here." ]
    },
    {
      "label": "default",
      "name": "Recovery Agent Doss",
      "priority": 10,
      "default": true,
      "say": [ "I am with Recovery. Vance handles the talking, and the rest. I just hold the corner and try not to look at your face too long." ]
    }
  ]
}
```

**DATAPACK NEEDS:**
- `function/sidequest/retirement/take_memo.mcfunction` — latch: `tag @s add retirement_memo_taken`;
  `function .../economy/payout {amount:700}`; `loot give @s loot .../npc_gift/training_major`;
  give the memo lore item (renamed paper — reuse `.../npc_gift/dead_letter` or a memo table,
  showrunner's pick); breadcrumb `tag @s add scrubbing_artifact_memo` (feeds Board/Founder chain);
  title sting `title @s actionbar` "RETIREMENT PACKAGE SECURED".
- **No battle function needed** — the double is a normal `{do:"battle"}` off the lead's block;
  `despawn_on_win` removes Vance, and Doss stands down via its `defeated_sq_asset_recovery`
  gate. Doss's body may be manually removed on stream after the scene (per the `agent_yield`
  precedent — Easy NPC cannot self-delete a teammate without its own onwin).

**QUEST_TARGETS:**

```json
{
  "holder": "q.side_retirement",
  "name": "Retirement Package",
  "slot": 85,
  "stages": [
    { "if_tags": ["asset_recovery_clear"], "not_tags": ["retirement_memo_taken"], "label": "Take the memo trail from Severance", "target": { "npc": "sq_severance" } },
    { "if_tags": ["severance_met"], "not_tags": ["asset_recovery_clear"], "label": "Beat the Asset Recovery tag-team on the shrine road", "target": { "npc": "sq_asset_recovery_lead" } },
    { "if_tags": ["defeated_scorchspire_guide"], "not_tags": ["severance_met"], "label": "A Company clerk is hiding on the shrine road - find her", "target": { "npc": "sq_severance" }, "note": "Swap the entry if_tag for the town-entry breadcrumb the spine uses." }
  ]
}
```

**REWARD/BALANCE:** win **700 CD** (payout) + `training_major` latch + memo lore item +
breadcrumb `scrubbing_artifact_memo`. The double is `GEN_9_DOUBLES`, ace **≈74 (under cap
80)**. It is a **story ambush** — it only arms after `severance_met`, and the player opts in
by walking to Vance; there is no forced-touch battle and no decline fee (this is a rescue,
not a Company toll). Fairness floor honored. Memo text **circles** the reveal (retired →
emptied → there was never a founder), never closes it.

---

### RUMOR HUB — Nurse Ember (town clinic)

**Concept.** The town's healer doubles as the **rumor hub**: heal service, plus a
STANDARD-gated pointer that names each open quest (gated on its not-done tag), delivers the
**`frag_10` town-side echo**, and plants the **forward hooks** — Cinderfall Descent, Road to
Royal League, and the **Battle Frontier tease** ("some trainers do not stop at the League —
there is a whole Frontier past it, they say, for people who cannot put the game down"). She
also carries a **back-echo** to a liberated field / the badge count.

**Character JSON** (`dialog-src/characters/scorchspire/scorchspire_healer.json`):

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "scorchspire_healer",
  "display_name": "Nurse Ember",
  "role": "healer",
  "act": "3",
  "location": "Scorchspire - Clinic",
  "recognition_tier": "late",
  "recipe": "healer",
  "dialog": "dialog:scorchspire_rumor_hub",
  "movement": { "objective": "ambient_stationary_look" },
  "service": { "kind": "heal" },
  "placement": { "x": 3672, "y": 68, "z": 4576 }
}
```

**Dialog JSON** (`dialog-src/dialog/scorchspire_rumor_hub.json`) — heal + gated rumor pointers + frag_10 echo:

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "scorchspire_rumor_hub",
  "type": "STANDARD",
  "entries": [
    {
      "label": "frag_echo",
      "name": "Ember - the last exhale",
      "priority": 35,
      "gate": { "defeated": "scorchspire_leader" },
      "say": [
        "Ember Badge. The last one. Whole town felt the spire cool a notch when you came down. There is nothing above us now but the Cinderfall Descent, and at the bottom of it the Road to the Royal League. Some trainers do not even stop there - there is a whole Battle Frontier past the League, they say, for people who cannot put the game down. But first the League. First the answer you keep almost remembering.",
        "Heal up. Then go down the Descent and read your own signature, whatever that means to you. We will keep a bed warm for whoever comes back."
      ],
      "buttons": [
        { "label": "heal_button", "text": "Rest my team", "actions": [ { "do": "heal" }, { "do": "close" } ] },
        { "label": "leave_button", "text": "To the Descent", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "rumors",
      "name": "Nurse Ember",
      "priority": 10,
      "default": true,
      "say": [
        "Welcome to the Scorchspire clinic - last stop before the climb. Fire burns hardcore trainers fast; let me patch you up.",
        "This town runs on the forge, and the forge runs hot with unfinished business right now. If your feet are itching, there is work to hand."
      ],
      "buttons": [
        { "label": "heal_button", "text": "Rest my team", "actions": [ { "do": "heal" }, { "do": "close" } ] },
        { "label": "forge_tip", "text": "Ask about the forge", "gate": { "not_tag": "forge_order_done" }, "actions": [
          { "do": "announce", "text": "Forgemaster Sena is burning a dead Company requisition down at the forge - she could use a second pair of hands.", "as": "chat", "color": "gold" }, { "do": "close" } ] },
        { "label": "temper_tip", "text": "Ask about the bladesmith", "gate": { "not_tag": "temper_blade_done" }, "actions": [
          { "do": "announce", "text": "Bladesmith Hollis tempers keepsakes in the forge annex - bring him iron and he will quench you something for the League.", "as": "chat", "color": "gold" }, { "do": "close" } ] },
        { "label": "marren_tip", "text": "Ask about Old Marren", "gate": { "not_tag": "the_hand_done" }, "actions": [
          { "do": "announce", "text": "Old Marren at the hearth is chasing a door plate he lost to the slag heap. Sixty years at the bellows - humor him.", "as": "chat", "color": "gold" }, { "do": "close" } ] },
        { "label": "shrineroad_tip", "text": "Ask about the shrine road", "gate": { "not_tag": "retirement_memo_taken" }, "actions": [
          { "do": "announce", "text": "There is a jumpy clerk hiding down the shrine road, and some hard-looking company came asking after her. The Fire Shrine is that way too, if you have the nerve.", "as": "chat", "color": "gold" }, { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:** none (heal is a `service`/mechanic action; the rumor pointers are pure
dialog announces). Optional: a `frag_10` town-echo could reuse the existing memory HUD line
rather than duplicate — showrunner may point `frag_echo` at the canonical `frag_10` text.

**QUEST_TARGETS:** none (rumor hub is discovery, not a tracked quest). The frag echo and
Frontier/League forward hooks live in dialog, matching the house-style hub pattern.

**REWARD/BALANCE:** heal service only; no CD, no battle.

---

## 4. Recognition & economy beats

**Recognition tier: LATE (badges 7+, post-HQ).** Every Company body in this unit is written
to the **late** gradient (LORE_BIBLE §4): they place the founder and either **stand down**
(Doss: "I saw the portrait come down"; Kessler: "I do not want to think about who does that,
so do not make me") or **panic and double down on the official line** (Vance: "there was
never a founder, so there is no one standing there"). None of them **name** him — the reveal
is Act 3, post-League. `frag_10`'s "face your own signature" is echoed civilian-side by Nurse
Ember ("read your own signature, whatever that means to you") and, most sharply, by Old
Marren, who recognises the *hand* off a forged plate and **deliberately refuses** to close
the loop ("I am not going to say the name they scrubbed").

**Civilian voice.** Sena, Hollis, Marren, and Ember are townsfolk/elders — per canon they
**never recognise the founder as CEO.** Marren is the edge case the lore explicitly allows:
an old-timer who knew the *hand/portrait*, not the name (the public scrubbing worked; the
deep-craft memory did not). He gestures at the truth and stops short, which is exactly the
sanctioned late-tier civilian beat.

**Economy voice: idx 25 (stabilised, post-DJ).** The wheat war is effectively won and the
currency is steady, so this town does **not** run glossy Act-1 cheer or nervous Act-2
over-explaining. Its economic flavor is **aftermath**: a Company skeleton crew that cannot
even cancel its own dead requisition (SQ1), agents who "just stopped showing up" after the HQ
raid (SQ4), and a forge whose contracted steel "never has to build" the granaries anymore.
All CD payouts route through `economy/payout {amount:N}`, which at idx 25 pays the **floor
rate (75% of face)** — the visible haircut is the last echo of the destabilisation the player
already reversed. No `gym_destabilize` fires here.

**Back-echoes planted** (house rule — the world talks backward):
- SQ1 → the **HQ raid / DJ** ("the seal is the Acting CEO's, and the Acting CEO is filed now").
- SQ1 → the **wheat war** ("granary silos this steel never has to build").
- SQ2 → **Nifl** ("you came down off the ice") and the **Banked Coals** gym gimmick.
- SQ3 → **`frag_7` "you signed this charter"** ("you flinch every time I say sign").
- SQ4 → the **HQ raid** ("after your people stormed HQ, half of us stopped showing up") and
  **`frag_9` "they emptied you"** ("closer to emptied than retired").

**Forward hooks planted** (every arc pulls to the next beat):
- All four arcs + the hub name the **Cinderfall Descent**.
- SQ2, SQ3, the hub name the **Royal League / Road to Royal League [3545 2460]**.
- The hub teases the **Battle Frontier** (post-League).
- SQ4 teases the **post-game vanilla / east / the dragon** (the Ender Dragon aftermath).

---

## 5. New tags/scores introduced

| tag / score | set by | gated by (reads it) |
|---|---|---|
| `forge_order_1` | SQ1 `burn_one` button | SQ1 resume/target stages |
| `forge_order_2` | SQ1 `burn_two` button | SQ1 resume/target stages |
| `forge_order_agent_clear` | `sq_recovery_agent` win onwin **or** decline fn | SQ1 `burn_three` charter button; Kessler `cleared` entry |
| `defeated_sq_recovery_agent` | `sq_recovery_agent` battle win (defeat_tag) | (audit; win path) |
| `forge_order_3` | SQ1 `burn_charter` button | SQ1 `agent_wait` / target stages |
| `forge_order_done` | `forge_order/finish.mcfunction` | SQ1 `done_flavor`; Ember `forge_tip` gate |
| `temper_blade_done` | `tempering/quench.mcfunction` | SQ2 `wager`/`post_wager`; Ember `temper_tip` gate |
| `defeated_sq_temper_hollis` | `sq_temper_hollis` wager win (defeat_tag) | SQ2 `post_wager` |
| `the_hand_started` | SQ3 `accept` button | SQ3 `searching`/target stages |
| `the_hand_plate` | `the_hand/turn_in.mcfunction` | SQ3 `recovered` |
| `the_hand_done` | `the_hand/reveal.mcfunction` | SQ3 `post_reveal`; Ember `marren_tip` gate |
| `scrubbing_artifact_plate` | `the_hand/reveal.mcfunction` | Board/Founder reveal chain (synthesis) |
| `severance_met` | SQ4 `accept` button | SQ4 `ambush`/target stages |
| `asset_recovery_clear` | `sq_asset_recovery` win onwin | SQ4 `handoff`; Severance stages |
| `defeated_sq_asset_recovery` | `sq_asset_recovery` battle win (defeat_tag) | SQ4 lead/second stand-down entries |
| `retirement_memo_taken` | `retirement/take_memo.mcfunction` | SQ4 `done`; Ember `shrineroad_tip` gate |
| `scrubbing_artifact_memo` | `retirement/take_memo.mcfunction` | Board/Founder reveal chain (synthesis) |
| `ci_sq_scratch` (score) | reused (museum/load) | SQ2 + SQ3 count-checks (clear-with-0) |

> `defeated_scorchspire_guide` is used above only as a **placeholder town-entry breadcrumb**
> for quest activation/first-stage gating — swap it in the build for whatever "arrived in
> Scorchspire" tag the mainline spine already sets (see Open Questions).

---

## 6. Build checklist

1. **Create dir** `dialog-src/characters/scorchspire/` and drop **9 character files:**
   `scorchspire_healer`, `sq_forge_sena`, `sq_recovery_agent`, `sq_temper_hollis`,
   `sq_oldsmith_marren`, `sq_severance`, `sq_asset_recovery_lead`, `sq_asset_recovery_second`
   (blocks above). Set real `placement` coords with a builder pass (all currently PLACEHOLDER).
2. **Drop 8 dialog files** in `dialog-src/dialog/`: `sq_forge_order`, `sq_recovery_agent`,
   `sq_tempering`, `sq_the_hand`, `sq_retirement_package`, `sq_asset_recovery_lead`,
   `sq_asset_recovery_second`, `scorchspire_rumor_hub` (blocks above).
3. **Add rctmod team files** `data/rctmod/trainers/`: `sq_recovery_agent.json`
   (`GEN_9_SINGLES`, ~73, 2–3 fire mons), `sq_temper_hollis.json` (`GEN_9_SINGLES`, ~76, 2
   fire mons), `sq_asset_recovery.json` (`GEN_9_DOUBLES`, 4 mons ~74 — both suits fight this
   one team). All **cap-legal (≤ ace-relative under cap 80)**. Run the **rctmod cycle check**
   (singles + groups) after adding.
4. **Register side trainers:** create `data/cobblemon_initiative/trainers/side_quests/act3.json`
   (parallels `act1.json`) registering `sq_recovery_agent`, `sq_temper_hollis`,
   `sq_asset_recovery`. `name` must equal `displayName` for BATTLE_VICTORY name-match.
5. **Add functions** under `data/cobblemon_initiative/function/`:
   - `sidequest/forge_order/finish.mcfunction`
   - `route/decline_sq_recovery_agent.mcfunction`
   - `sidequest/tempering/turn_in.mcfunction`, `sidequest/tempering/quench.mcfunction`
   - `sidequest/the_hand/place_plate.mcfunction`, `.../turn_in.mcfunction`, `.../reveal.mcfunction`
   - `sidequest/retirement/take_memo.mcfunction`
   (specs in each quest's DATAPACK NEEDS; reuse `economy/payout`, `ci_sq_scratch` clear-with-0,
   and the `route/decline_sq_genji_wager` decline idiom.)
6. **Add loot/memo item tables** if using dedicated lore items for the plate/memo (or reuse
   `dead_letter`/an existing memo table). JAR-VALIDATE any keepsake item id (charcoal,
   netherite_scrap, the plate stand-in) before ship.
7. **Add 5 quest_targets stages** to `registers/quest_targets.json`: `q.side_forgeorder`
   (slot 82), `q.side_tempering` (83), `q.side_thehand` (84), `q.side_retirement` (85). (Hub =
   no register entry.) Confirm slots 82–85 are free (current max non-100 slot is 81).
8. **Compile:** `scripts/content_compile` → `scripts/update_preset_index` →
   `scripts/generate_npc_function` → `gradle build`. (Pipeline per ENGINE_FINDINGS §3;
   `content_compile` auto-runs the final step.) Then in-world: `cobblemon-initiative install run`
   to latch-spawn the placed NPCs; walk up + press `]` to track.

---

## 7. Open questions for showrunner

1. **Town-entry breadcrumb tag.** The quest activation/first-stage gates use
   `defeated_scorchspire_guide` as a placeholder for "player is in Scorchspire." What tag does
   the mainline spine actually set on town arrival (an `arrived_scorchspire` / R15-descent
   tag)? Swap it in so the intents light up on arrival, not on beating the gym guide.
2. **SQ2 keepsake item.** Netherite scrap vs a named Charcoal held item vs a cosmetic
   "Tempered Ingot" — pick the tempered keepsake (and JAR-VALIDATE the id). Same question for
   SQ1's charcoal keepsake and SQ3's door-plate stand-in item.
3. **SQ3 plate acquisition.** Prefer a placed **item entity** in the slag heap (ZERO
   block-edit, matches the no-block-edit rule) or a `loot give`-on-approach? Confirm the
   slag-heap prop coord (currently `(3656, 66, 4604)` PLACEHOLDER).
4. **Doss cleanup.** SQ4's second agent stands down in dialog (no self-delete). OK to leave
   the body and remove/relocate it manually on stream (the `agent_yield_second` precedent), or
   should Vance's onwin `kill @e[...name=Recovery Agent Doss...]` it (the `agent_yield_lead`
   round-12d fix)? Recommend the onwin-kill for a clean on-camera resolution.
5. **Villain density at gym 10.** This unit ships two Company beats (Kessler collector +
   Vance/Doss ambush), both resolvable by stand-down/opt-in. Is that the right amount for a
   skeleton-crew endgame town, or should Kessler be pure stand-down (no fight offered)?
6. **frag_10 echo source.** Should Nurse Ember's `frag_echo` reuse the canonical `frag_10`
   HUD text verbatim, or is the paraphrase here (kept apostrophe-free) fine as town flavor?
7. **Battle Frontier tease depth.** The hub names the Frontier as a rumor only. Is a light
   tease correct here, or does the showrunner want a named frontier-brain hook planted this
   early?
