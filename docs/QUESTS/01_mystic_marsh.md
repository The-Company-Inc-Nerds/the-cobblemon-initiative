# Mystic Marsh — Gym 3 Content Plan (slug `01_mystic_marsh`)

> IMPLEMENTATION-READY spec. This is a PLAN. Build later by pasting the character/dialog
> blocks into `dialog-src/`, adding the datapack functions, and appending the register
> stages, then running `scripts/content_compile`. Nothing here has been written to
> `dialog-src/`, `src/`, or the register — this doc is the only artifact.

---

## 1. Overview

**Mystic Marsh** is Gym 3 (Fairy, Leader **Titania** / `mystic_leader`). **Entry level cap
30** (Hua Zhan's unlock); beating Titania *unlocks* cap **37**. Leader ace = entry-cap+2 =
**32** (matches the shipped `mystic_leader.json` — Clefable 30 / Mawile 31 / Gardevoir 32).
Approached from Hua Zhan City down **R3 Willowmire Path**; the exit south is **R5 Quarry
Road** toward **Deepcore City** (Gym 4, Leader **Bruno**). Zone centroid **[1072 64 2459]**
(`dev/updated-zones.json`). Nearby set-pieces: **Fairy Shrine [947 2651]** (Shrine Leader
Aurora) and the corporate-owned **Mirebloom Paddies farm [1229 2820]** (field id **`farm_2`**
— the canonical id in `install.json` `activeWhenHolder` and roadmap `03_wheat_war_farms`;
NOT `farm_3`).

> **CAP NOTE:** all side/guard/wager battles below are tuned **≤ leader ace 32** (entry cap
> 30 + the ace tolerance), so nothing here fights above the marsh ladder. The one *above-cap*
> beat (a friendly wager) is still opt-in, printed, and decline-able as a courtesy.

**Band context** (LORE_BIBLE §8 / progression bands):

| Knob | Value at Mystic Marsh |
|------|------------------------|
| Level cap (entry) | **30** (leader ace = 32) — beating Titania *unlocks* 37 |
| Memory fragment | **frag_3** (`memory_fragment` scoreboard = 2 on arrival, 3 after Titania) |
| `cd_instability` target | **→ 24** — the *ceiling of the stable feel*; Act 1 tail bleeding into Act 2 |
| Recognition band | **EARLY→MID boundary** (badges 2–3): grunts still confused-hostile, but the first *management* alarm starts here |
| Wheat recognition | `fields_liberated` 0–1 (trade-only) — traders here are NOT yet hostile |
| Mirebloom field id | **`farm_2`** (`install.json` / roadmap canon; frees via `liberation/free_field {field:farm_2}`) |

**The arc job this town does:** Mystic Marsh is where the glossy corporate cheer *first
sounds thin*. It is a town that already trades in charms, luck, and superstition, so a
population primed to distrust "verified value" is the perfect place to plant the first
crack. Four beats carry it:

1. A **rumor-hub healer** (the fen-witch nurse) who points at the town's quests and delivers
   the first "the money feels wrong" civilian murmur — she reads it as *bad luck in the water*,
   not economics, because civilians never see the plot (only feel the decay).
2. The **first "prices are adjusting" nervous-economy beat** — a Company exchange-board
   clerk over-explaining a recalibration (`cd_instability` gte 24 flavor), the Act-1→Act-2
   register shift made audible.
3. A **wheat-trader introduction** on the marsh causeway — the alternative-currency pitch,
   trade-only here (not hostile: `fields_liberated` still low), planting the Wheat War thread
   that Deepcore will escalate.
4. A **Mirebloom field-liberation hook** — the corporate-owned paddy south of town, the
   first *marsh* field the player can wrest back (`farm_2`), advancing `fields_liberated`
   toward the HQ gate (the raid gate is **6 of 10** fields per the 2026-07-06 ruling; the
   shipped `render.mcfunction` label may still read 4 — do not re-assert a number this doc
   does not own, defer the count to `liberation/*` / `quest/render`).

Forward hook planted throughout: **Deepcore City** and **R5 Quarry Road**. Back-echoes
reference the **Hua Zhan greenhouse / wheat naming beat** (`wheat_named`), the earned
**badges** so far, and the wider **field campaign** (`wheat_war_active`).

**Existing cast — DO NOT duplicate** (interior gym, already shipped under
`dialog-src/characters/gym/`): Leader **Titania** (`mystic_leader`), **Mystic Marsh Gym
Guide** (`mystic_guide`), Apprentice **Faye** (`mystic_apprentice`), Jr. Apprentice **Fae**
(`mystic_jr_apprentice`), Fairy Tale Girl **Luna** (`mystic_trainer_1`), Hex Maniac
**Stella** (`mystic_trainer_2`), Pokémon Ranger **Lyra** (`mystic_trainer_3`), Artist
**Viola** (`mystic_trainer_4`). All new NPCs below are *town/route/farm* cast around the gym,
none reuse those roles or names.

---

## 2. Cast

New characters authored by this unit. `mystic/` is the proposed new
`dialog-src/characters/mystic/` area folder. Placement coords are **PLACEHOLDER** anchors
inside/near the Mystic Marsh zone (centroid 1072/64/2459), Fairy Shrine approach (947/·/2651),
Willowmire Path (1372/·/2329), and Mirebloom Paddies (1229/·/2820) — confirm every `y` and
exact tile at the placement pass; the `x/z` are authoritative zone reference only.

| id | display_name | role | one-line concept | placement anchor (PLACEHOLDER) |
|----|--------------|------|------------------|--------------------------------|
| `mm_nurse` | Fen-Nurse Wisteria | `healer` | Rumor-hub marsh healer; heals via `heal_paid`; points at town quests; first "money feels cursed" murmur (reads it as bad luck, not economics). | Marsh Pokémon Center counter — [1068 64 2465] |
| `mm_charm_seller` | Charm-Weaver Marigold | `quest_giver` | Superstition merchant who restrings will-o-wisp charms; the town's fetch quest giver. | Charm stall, town square — [1076 64 2452] |
| `mm_exchange_clerk` | Verified Clerk Osric | `merchant` | Company exchange-board clerk over-explaining a "recalibration"; the first prices-adjusting beat. | Exchange board kiosk, town edge — [1082 64 2448] |
| `mm_wheat_trader` | Sedge | `wheat_trader` | Marsh causeway wheat pitch; trade-only introduction of the alternative currency. | Willowmire causeway into town — [1058 64 2478] |
| `mm_will_o_wisp_child` | Marsh-Child Bryn | `civilian` | A kid chasing wisps at dusk who wants a Fairy-type shown to them; tiny flavor errand + Fairy Shrine forward-nudge. | Boardwalk over the fen — [1064 64 2470] |
| `mm_field_guard` | Steward Halvard | `wheat_trader` | Company field steward holding the Mirebloom Paddies; the field-liberation fight that frees `farm_2`. | Mirebloom Paddies gate — [1229 64 2820] |

Roster note: `mm_wheat_trader` and `mm_field_guard` both take `role: wheat_trader`
(the schema's grain-buyer bundle) but do different jobs — Sedge is the *pitch/trade*
introduction, Halvard is the *forced field fight*. Both wear plain names per the
2026-07-06 ruling ("company members wear normal names; the title lives in dialog").

---

## 3. Quests

Five town/route quests. Each is fail-soft, plants the Deepcore/Quarry-Road forward hook or
a back-echo, and reuses the shipped helper functions (`economy/payout`, `economy/heal_paid`,
`liberation/free_field`, the `ci_sq_scratch` count-check idiom from Genji/museum).

---

### 3.1 The Water Remembers (rumor-hub + heal + first "money feels wrong" murmur)

**Quest giver:** Fen-Nurse Wisteria (`mm_nurse`). **Concept:** the town's healer and rumor
hub. She heals for the posted `heal_paid` rate, points the player at Mystic Marsh's other
quests (each button gated on the quest's not-done tag), and delivers the *civilian* version
of the Act-1→Act-2 turn: the marsh water tastes wrong, coins come up short, she blames the
fen's luck because she cannot see the ledger. **Forward hook:** names **R5 Quarry Road** and
**Deepcore** as the road out. **Back-echo:** references the earned badges and the wheat talk
drifting up **Willowmire Path** from Hua Zhan (`heard_wheat_pitch`).

This is a hub, not a fetch — it has no turn-in of its own; it is the signpost. It carries a
gated `restock`-style button only insofar as it opens Marigold's and Sedge's threads by
flavor. Recognition: **civilian, ZERO recognition of the founder** (LORE_BIBLE §4).

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "mm_nurse",
  "display_name": "Fen-Nurse Wisteria",
  "role": "healer",
  "act": "1",
  "location": "Mystic Marsh - Pokemon Center",
  "recognition_tier": "early",
  "recipe": "civilian",
  "dialog": "dialog:mm_nurse",
  "movement": { "objective": "ambient_stationary_look" },
  "_comment": "RUMOR HUB + paid healer. Heals via the shipped economy/heal_paid (the ONLY sanctioned fee driver; do NOT fork it). Civilian, zero recognition of the founder. Rumor buttons point at the town quests, each gated on the quest not-done tag: Marigold's charms (not_tag mm_charms_done -> nudge charm stall), Sedge's causeway (nudge), the Mirebloom paddy (not_tag farm_2_free -> gestures south). First money-feels-wrong civilian murmur lives in the post_badge_2 entry gated cd_instability gte 24 - she reads it as fen bad-luck, NOT economics. Sets met_mm_nurse on the default buttons so rumor lines auto-open on the second visit. PLACEMENT: behind the Center counter - confirm no free Cobblemon Healing Machine block undercuts the paid nurse (Sango/Takehara/Hua Zhan precedent). uuid omitted - latch-place via placement at build.",
  "placement": { "x": 1068, "y": 64, "z": 2465 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "mm_nurse",
  "type": "CUSTOM",
  "_comment": "Fen-Nurse Wisteria, Mystic Marsh Center. Heal buttons ride economy/heal_paid (posted rate). Rumor buttons are pure signposting (open a flavor page + close), each gated on the target quest not-done tag so they vanish as the town empties. post_badge_2 (prio 25) is the first-money-feels-wrong murmur, further gated cd_instability gte 24. met_mm_nurse set on default buttons. Civilian: no founder recognition anywhere in this tree.",
  "entries": [
    {
      "label": "post_badge_2",
      "name": "Fen-Nurse Wisteria - the water tastes wrong",
      "priority": 25,
      "gate": { "badges": { "op": "gte", "value": 2 }, "cd_instability": { "op": "gte", "value": 24 } },
      "say": [
        "Sit them down, I will mend them. Fair warning though - the fen has a mood on it lately. Coin comes up light in my drawer, milk sours a day early, and the will-o-wisps burn blue instead of gold. Old wives would say the marsh is holding a grudge. I only know my count keeps coming up short and I did not miscount.",
        "Rest your team, it is on the posted rate same as always. And if a merchant tells you the price simply adjusted while you stood there - well. Round here we do not call that an adjustment. We call it the bog taking its tithe."
      ],
      "buttons": [
        { "label": "heal_button", "text": "Heal my team - posted rate", "actions": [ { "do": "command", "cmd": "function cobblemon_initiative:economy/heal_paid", "as_player": true }, { "do": "open_dialog", "label": "healed" } ] },
        { "label": "rumor_charms_button", "text": "Anyone need a hand in town?", "gate": { "not_tag": "mm_charms_done" }, "actions": [ { "do": "open_dialog", "label": "rumor_charms" } ] },
        { "label": "rumor_field_button", "text": "What is south of here?", "gate": { "not_tag": "farm_2_free" }, "actions": [ { "do": "open_dialog", "label": "rumor_field" } ] },
        { "label": "leave_button", "text": "Just passing through", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Fen-Nurse Wisteria",
      "priority": 10,
      "default": true,
      "say": [
        "Welcome to the Mystic Marsh Center - mind the boardwalk, it weeps at the joints. Full restore is the posted rate, no account card needed, and the tea by the door is free whether you can pay or not. Marsh hospitality. Ask me anything, the fen makes for slow afternoons.",
        "You look like the road chewed you up. Willowmire Path does that. Set your team down, the schedule is on the post, and I will point you somewhere useful if you have a spare hour in town."
      ],
      "buttons": [
        { "label": "heal_button", "text": "Heal my team - posted rate", "actions": [ { "do": "command", "cmd": "function cobblemon_initiative:economy/heal_paid", "as_player": true }, { "do": "command", "cmd": "tag @s add met_mm_nurse", "as_player": true }, { "do": "open_dialog", "label": "healed" } ] },
        { "label": "rumor_charms_button", "text": "Anyone need a hand in town?", "gate": { "not_tag": "mm_charms_done" }, "actions": [ { "do": "command", "cmd": "tag @s add met_mm_nurse", "as_player": true }, { "do": "open_dialog", "label": "rumor_charms" } ] },
        { "label": "rumor_field_button", "text": "What is south of here?", "actions": [ { "do": "command", "cmd": "tag @s add met_mm_nurse", "as_player": true }, { "do": "open_dialog", "label": "rumor_field" } ] },
        { "label": "leave_button", "text": "Not right now", "actions": [ { "do": "command", "cmd": "tag @s add met_mm_nurse", "as_player": true }, { "do": "close" } ] }
      ]
    },
    {
      "label": "rumor_charms",
      "name": "Fen-Nurse Wisteria - the charm-weaver",
      "priority": -1,
      "say": [
        "Marigold, at the charm stall in the square, has been in a state. Her will-o-wisp charms went dark - the little glimmer inside gutters out without spider silk to hold it. If you have been through the reeds after dark you have probably got some on your boots already. She pays, and she will read your luck for free, which is worth more than the coin the way things are going."
      ],
      "buttons": [ { "label": "back_button", "text": "Good to know", "actions": [ { "do": "open_default" } ] }, { "label": "leave_button", "text": "I will look in on her", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "rumor_field",
      "name": "Fen-Nurse Wisteria - the paddies",
      "priority": -1,
      "say": [
        "South, past the shrine track, the Mirebloom Paddies. Used to be marsh-rice the whole town grew together. The Company fenced it last spring, put a steward on the gate, and now the grain goes somewhere and the water tastes of iron. Beyond that it is Quarry Road, and Deepcore City past the stone - a hard town for a hard badge. If you mean to take the paddy back, mind the steward. He does not read as the friendly sort."
      ],
      "buttons": [ { "label": "back_button", "text": "Back", "actions": [ { "do": "open_default" } ] }, { "label": "leave_button", "text": "I will keep it in mind", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "healed",
      "name": "Fen-Nurse Wisteria - rested",
      "priority": -1,
      "say": [
        "There - rested and mended, billed at the posted rate, same as everyone who crosses my boards. Mind the wisps on the way out. They lead the tired ones in circles.",
        "Good as new. Quarry Road south is long and Deepcore at the end of it is longer. Rest here while the fen lets you."
      ],
      "buttons": [ { "label": "leave_button", "text": "Thank you, Nurse", "actions": [ { "do": "close" } ] } ]
    }
  ]
}
```

**DATAPACK NEEDS:** none new — reuses shipped `economy/heal_paid`. (`met_mm_nurse` is a
plain tag set by dialog, no function required.)

**QUEST_TARGETS entry:** none (a hub, not a tracked quest — it points at the quests below,
which are tracked individually).

**REWARD/BALANCE:** heal fee = `100 + 2 × cd_instability` (≈148 at idx 24), balance-gated,
soft-fails when broke. No battle, no cap concern.

---

### 3.2 Wisps in the Reeds (Marigold's charm fetch — the count-check turn-in idiom)

**Quest giver:** Charm-Weaver Marigold (`mm_charm_seller`). **Concept:** the town's
fetch/turn-in quest in the exact Genji idiom — bring **8× `minecraft:string`** (spider silk
from the marsh reeds after dark) and she restrings the will-o-wisp charms; you keep one that
wards Fairy-luck. Then an **opt-in, decline-able, above-flavor** friendly charm-reading
**wager** against her two low charm-mons — printed stake, fail-soft. **Forward hook:** her
luck-reading names **Deepcore** as "stone and fists ahead — carry something that laughs at
both." **Back-echo:** references the wheat-pitch drifting up **Willowmire** and the player's
badges. **Fetch mechanic:** turn-in via a datapack count-check that clears+counts (NO
`has_item` gate — broken per HARD RULE 4), mirroring `sidequest/genji/turn_in_rod`.

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "mm_charm_seller",
  "display_name": "Charm-Weaver Marigold",
  "role": "quest_giver",
  "act": "1",
  "location": "Mystic Marsh - Charm Stall",
  "recognition_tier": "early",
  "recipe": "quest_fetch",
  "trainer": "sq_marigold_charm",
  "dialog": "dialog:sq_marigold_wisps",
  "movement": { "objective": "ambient_stationary_look" },
  "_comment": "WISPS IN THE REEDS (Mystic Marsh fetch, Genji idiom). Bring 8x minecraft:string (vanilla spiders in the marsh reeds after dark drop it; Cobblemon bug-line drops UNVERIFIED - keep the flavor on vanilla spiders). turn_in checks the count via clear-with-0 (NO has_item gate) then charm_success clears 8, gives a fairy-warding charm item + pays face 300 via economy/payout, latches mm_charms_done. Then an opt-in friendly 200 CD charm-reading wager vs sq_marigold_charm (two Fairy/charm-mon lv 28-29, CAP-LEGAL at entry cap 30 / ace 32) - printed stake, decline_fee 100, loss_fee 200, fail-soft. Civilian giver: zero founder recognition. CHARM ITEM: use a shipped gift loot table (npc_gift/training_standard) - do NOT invent a custom item id; if a keepsake item is wanted, JAR-VALIDATE the id first. uuid omitted - latch-place at build.",
  "battle": {
    "trainer": "sq_marigold_charm",
    "type": "wager",
    "format": "GEN_9_SINGLES",
    "prize": 200,
    "loss_fee": 200,
    "decline_fee": 100,
    "defeat_tag": "defeated_sq_marigold_charm",
    "win_line": "Ha - the cards said you would win and I still bet against them. The marsh keeps me humble. Two hundred, and a reading thrown in for free.",
    "lose_line": "The stake stays with the weaver, as the old rule goes. No hard feelings - luck is a loan, and today the fen called it in.",
    "already_beaten_line": "One reading a visit, love. The wisps get jealous if I favor a face."
  },
  "placement": { "x": 1076, "y": 64, "z": 2452 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "sq_marigold_wisps",
  "type": "STANDARD",
  "_comment": "WISPS IN THE REEDS - Charm-Weaver Marigold. Fetch 8x minecraft:string -> charm restrung, keep one + 300 CD (turn_in_charm checks the count via clear-with-0, charm_success clears 8 and pays). Then opt-in 200 CD wager (trainer sq_marigold_charm, loss_fee 200, decline_fee 100). Priority: reading_flavor (40, post-wager) > wager (30, after charms) > default (10, the string request). Civilian: no founder recognition.",
  "entries": [
    {
      "label": "reading_flavor",
      "name": "Marigold - a free reading",
      "priority": 40,
      "gate": { "defeated": "sq_marigold_charm" },
      "say": [
        "Sit, sit - the reading is free now, you have earned my curiosity. The cards say stone ahead. Quarry Road, and Deepcore past it, where they settle everything with fists. Carry something that laughs at a punch - a fairy thing, a charm thing. The marsh gave you one. Do not lose it in the dark.",
        "Funny - three cards came up upside down and every one of them was about you. A face that ought to be forgotten and is not. The fen loves a riddle. So do I. Come back when you have walked a little further into it."
      ],
      "buttons": [ { "label": "leave_button", "text": "I will remember the charm", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "wager",
      "name": "Marigold - a wager on the wisps",
      "priority": 30,
      "gate": { "tag": "mm_charms_done", "not_tag": "defeated_sq_marigold_charm" },
      "say": [
        "That charm settle right on you? Good. Then a proposition, weaver to a lucky one: a friendly reading-wager. Two hundred CobbleDollars says my two wisp-dancers put yours in the reeds. Lose and the stake is mine; decline and we stay friends and I still read your cards.",
        "No pressure at all. The fen is patient and so am I. But the wisps do so love an audience."
      ],
      "buttons": [ { "label": "wager_button", "text": "Take the wager - 200 CD on the line", "actions": [ { "do": "battle" } ] } ],
      "no_goodbye": true
    },
    {
      "label": "default",
      "name": "Charm-Weaver Marigold",
      "priority": 10,
      "default": true,
      "gate": { "not_tag": "mm_charms_done" },
      "say": [
        "Marigold, charm-weaver - I catch the will-o-wisps and bottle a little of their glow into a ward. Only my charms have gone dark. The glimmer needs silk to hold it, real silk, and mine rotted through. Bring me eight lengths of string and I will restring the lot - one ward for me, one for you, warded against fairy-mischief and worse.",
        "Eight lengths of string, love. The reed-spiders spin it thick after dark, out past the boardwalk where the sensible folk do not go. Bring it and you keep a charm that laughs at a hard hit. You will want that where the road is taking you."
      ],
      "buttons": [
        { "label": "turn_in_button", "text": "Hand over 8 string", "actions": [ { "do": "command", "cmd": "function cobblemon_initiative:sidequest/marigold/turn_in_charm", "as_player": true }, { "do": "close" } ] },
        { "label": "leave_button", "text": "I will go find some", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:**
- `function/sidequest/marigold/turn_in_charm.mcfunction` — run as player. Count via
  `execute store result score @s ci_sq_scratch run clear @s minecraft:string 0`; if `8..`
  and not `mm_charms_done`, call `charm_success`; if `..7`, tellraw the "not eight yet /
  reed-spiders after dark" line. (Direct copy of `sidequest/genji/turn_in_rod`, swapping
  the tag/success target.) Reuses the shared `ci_sq_scratch` objective (created in
  `museum/load`).
- `function/sidequest/marigold/charm_success.mcfunction` — run as player. `clear @s
  minecraft:string 8`; `function cobblemon_initiative:economy/payout {amount:300}`;
  `loot give @s loot cobblemon_initiative:npc_gift/training_standard` (the "kept charm" is
  represented by the standard gift table — do NOT invent a custom charm item id unless one is
  jar-validated); `tag @s add mm_charms_done`; `title @s actionbar` sting
  (e.g. `Restrung. A little marsh-luck, bottled.` — no `"`/`'`/`%`).
- Decline function `function/route/decline_sq_marigold_charm.mcfunction` is
  **auto-generated** by `content_compile` from `battle.decline_fee` (same as
  `decline_sq_genji_wager`) — no hand authoring.

**QUEST_TARGETS entry** (append to `registers/quest_targets.json`, new holder):
```json
{
  "holder": "q.side_wisps",
  "name": "Wisps in the Reeds",
  "slot": 56,
  "stages": [
    {
      "if_tags": ["met_mm_nurse"],
      "not_tags": ["mm_charms_done"],
      "label": "Bring 8 string to the charm-weaver",
      "target": { "npc": "mm_charm_seller" },
      "note": "Fetch leg. String is gathered in the marsh reeds after dark; the charm stall is the fixed objective point. Gate on met_mm_nurse so it lights after the rumor hub introduces the town (mirror hz_nurse -> restock)."
    }
  ]
}
```
> Slot 56 is proposed (below `q.side_clinic` 57); confirm it is free at synthesis or shift.

**REWARD/BALANCE:** fetch pays **300 CD** (skewed via `economy/payout`) + a training gift.
Wager **200 CD** prize / **200 CD** loss / **100 CD** decline. Wager team is **lv 28-29
(≤ entry cap 30, under ace 32) — CAP-LEGAL** and a *friendly* battle, so the opt-in stake
print is a courtesy, not a cap-exception. Declining pays 100 CD, fail-soft (broke → the
decline still resolves, same as Genji); fairness floor holds because it is not a Company
forced fight and cannot whiteout a starter-only player (it is opt-in and cap-legal).

---

### 3.3 Verified Weather (Clerk Osric — the first "prices are adjusting" beat)

**Quest giver / prop-NPC:** Verified Clerk Osric (`mm_exchange_clerk`) at the town exchange
board. **Concept:** the first *nervous-reassurance* economy beat made interactive. Osric is a
Company exchange clerk over-explaining a "recalibration" of the marsh conversion rate. A tiny
**observe-and-report** micro-quest: the player is asked to watch the board flicker three
times (three actionbar reads at the kiosk) and Osric pays a token "witness fee" for
confirming the board is *totally fine* — the joke being that the receipt is short. **Forward
hook:** Osric mutters that Deepcore's exchange is "under review too — the whole southern
corridor is recalibrating." **Back-echo:** references the Hua Zhan price checks
(`hz_prices_done`) if the player did them — the same recalibration, one town later, worse.
Economy voice gated on `cd_instability gte 16` (nervous) → `gte 24` (over-explaining).

This is a light beat (no combat). It uses a simple 3-count reads latch rather than a fetch.

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "mm_exchange_clerk",
  "display_name": "Verified Clerk Osric",
  "role": "merchant",
  "act": "1",
  "location": "Mystic Marsh - Exchange Board",
  "recognition_tier": "early",
  "recipe": "shopkeeper",
  "dialog": "dialog:mm_exchange_board",
  "movement": { "objective": "ambient_stationary_look" },
  "_comment": "VERIFIED WEATHER (first prices-adjusting beat). Company exchange-board clerk. NOT a real shop merchant - recipe shopkeeper only for the stall body; no trade block. Micro-quest: read the flickering board 3x (price_read increments ci_mm_reads; at 3 -> witness_pay latches mm_board_done and pays a deliberately-short token witness fee via economy/payout). Economy VOICE gated cd_instability: default glossy (Act1), gte 16 nervous, gte 24 over-explaining. Civilian-tier: he is Company staff but low and clueless - confused-loyal, NOT recognition of the founder (line-selection only; a clerk this junior never saw the portrait). Back-echo hz_prices_done. Forward hook: Deepcore corridor under review. uuid omitted - latch-place at build.",
  "placement": { "x": 1082, "y": 64, "z": 2448 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "mm_exchange_board",
  "type": "STANDARD",
  "_comment": "Verified Clerk Osric at the Mystic Marsh exchange board. First prices-adjusting beat. Economy register climbs with cd_instability: over_explaining (prio 30, gte 24) > nervous (prio 20, gte 16) > glossy default (prio 10). done_flavor (prio 40, mm_board_done) tombstones it. read_button runs price_read (increments ci_mm_reads, actionbar the flicker; at 3 -> witness_pay pays short + latches mm_board_done). No founder recognition - clerk too junior.",
  "entries": [
    {
      "label": "done_flavor",
      "name": "Clerk Osric - all verified",
      "priority": 40,
      "gate": { "tag": "mm_board_done" },
      "say": [
        "Thank you for witnessing. The board is stable, the rate is verified, and your fee was processed at the standard witness schedule. If it felt light, that is the rounding, and the rounding is in the Company favor - as posted. Everything is fine. Everything is verified. Do enjoy the marsh.",
        "The southern corridor is fully recalibrated, per the notice. Deepcore is under review, Quarry Road is under review, the fen is under review. All perfectly routine. Move along and trade with total confidence."
      ],
      "buttons": [ { "label": "leave_button", "text": "Right. Fine.", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "over_explaining",
      "name": "Clerk Osric - a healthy recalibration",
      "priority": 30,
      "gate": { "cd_instability": { "op": "gte", "value": 24 } },
      "say": [
        "Do not be alarmed by the board. It flickers because it is WORKING. What you are seeing is a healthy, temporary, entirely planned recalibration of value. Prices are simply adjusting. That is normal. That is healthy. Please do not hoard. The CobbleDollar is fine. The CobbleDollar has always been fine. Who told you otherwise.",
        "I will need a witness, actually - policy, since the corridor went under review. Watch the board settle three times and confirm to head office that the rate is stable. There is a small witness fee. Small, and getting smaller, but a fee. Would you."
      ],
      "buttons": [
        { "label": "read_button", "text": "Watch the board flicker", "gate": { "not_tag": "mm_board_done" }, "actions": [ { "do": "command", "cmd": "function cobblemon_initiative:sidequest/exchange_board/price_read", "as_player": true }, { "do": "close" } ] },
        { "label": "leave_button", "text": "It looks broken to me", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "nervous",
      "name": "Clerk Osric - a temporary adjustment",
      "priority": 20,
      "gate": { "cd_instability": { "op": "gte", "value": 16 } },
      "say": [
        "The board is adjusting. A temporary recalibration of value - the Company is on top of it, the Company is always on top of it. My payout came up short again this week, which is, ah, also being recalibrated. Nothing to see. Verified trust, verified value.",
        "Policy says I need a witness on the rate now. Watch it settle a few times and sign off that it is stable? There is a witness fee in it - modest, honestly quite modest these days. Would you mind."
      ],
      "buttons": [
        { "label": "read_button", "text": "Watch the board flicker", "gate": { "not_tag": "mm_board_done" }, "actions": [ { "do": "command", "cmd": "function cobblemon_initiative:sidequest/exchange_board/price_read", "as_player": true }, { "do": "close" } ] },
        { "label": "leave_button", "text": "Maybe later", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Verified Clerk Osric",
      "priority": 10,
      "default": true,
      "say": [
        "Welcome to the Verified Exchange, marsh branch. Ten years of stability and counting - the Company keeps the ledgers honest so you do not have to. The rate is posted, the rate is sound, and the tea is not free but it is fairly priced. Verified trust, verified value.",
        "Best conversion rate on the southern corridor, all backed by nether-star reserves and double-signed on my honor. Trade with confidence. The board never lies - it only, very occasionally, blinks."
      ],
      "buttons": [ { "label": "leave_button", "text": "Just looking", "actions": [ { "do": "close" } ] } ]
    }
  ]
}
```

**DATAPACK NEEDS:**
- `function/exchange_board/load.mcfunction` (or fold into an existing `load`) — create the
  `ci_mm_reads` scoreboard objective (`scoreboard objectives add ci_mm_reads dummy`).
- `function/sidequest/exchange_board/price_read.mcfunction` — run as player. `scoreboard
  players add @s ci_mm_reads 1`; actionbar a flicker line per read (e.g.
  `The board blinks: 41 ... 38 ... 41 CD to the star. Verified.` — vary by read count with
  `execute if score @s ci_mm_reads matches 1/2/3`); at `matches 3..` and not `mm_board_done`
  → `function .../witness_pay`.
- `function/sidequest/exchange_board/witness_pay.mcfunction` — run as player. Deliberately
  short token fee: `function cobblemon_initiative:economy/payout {amount:60}` (the skew makes
  the *received* number visibly less than 60 — the joke lands on the receipt); `tag @s add
  mm_board_done`; `title @s actionbar` receipt (`ADJUSTMENT: rounding, in the Company favor.`
  — no `"`/`'`/`%`).

**QUEST_TARGETS entry** (append; new holder):
```json
{
  "holder": "q.side_verified",
  "name": "Verified Weather",
  "slot": 55,
  "stages": [
    {
      "if_tags": [],
      "not_tags": ["mm_board_done"],
      "scores": [ { "objective": "cd_instability", "op": "gte", "value": 16 } ],
      "label": "Witness the exchange board at the marsh kiosk",
      "target": { "npc": "mm_exchange_clerk" },
      "note": "Only lights once the money starts feeling wrong (cd_instability gte 16 - the nervous register). Ends on mm_board_done."
    }
  ]
}
```

**REWARD/BALANCE:** token **~60 CD face** (skewed *down* on purpose — thematic, the whole
gag). No battle, no cap concern, no decline needed. Purely a flavor/receipt beat.

---

### 3.4 The Alternative (Sedge — wheat-trader introduction)

**NPC:** Sedge (`mm_wheat_trader`) on the Willowmire causeway into town. **Concept:** the
**first wheat-trader the player meets since Hua Zhan**, and the introduction of the
alternative-currency pitch *in the fen* — where a town of charm-buyers is exactly the crowd
the Company's grain-cult wants. Reuses the shipped `dialog:wheat_trader` tree verbatim (it is
generic, gated on `wheat_trader:suspicious/hostile` bands the `wheat_trader/tick` poller
maintains from `fields_liberated`). Here `fields_liberated` is still 0–1, so only the
**default pitch** (trade-only) shows; he is *not* hostile yet — recognition comes later at
Kalahar-band (`≥4` fields). **Forward hook:** the pitch's own lines gesture at the coming
collapse ("before the paper money learns it is worthless"). **Back-echo:** if the player
liberated Firstfurrow already, `wheat_war_active` is set — Sedge's suspicious tier (if it
ever fires from a prior field) references "a face from the old company," the recognition seed.

Because the tree is shared, this is a **reuse**, not a new dialog file — only a new character.

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "mm_wheat_trader",
  "display_name": "Sedge",
  "role": "wheat_trader",
  "act": "1",
  "location": "Mystic Marsh - Willowmire Causeway",
  "recognition_tier": "early",
  "recipe": "grain_buyer",
  "dialog": "dialog:wheat_trader",
  "_comment": "THE ALTERNATIVE (wheat-trader introduction at Mystic Marsh). Reuses the shared dialog:wheat_trader tree (default pitch / suspicious / hostile, gated on wheat_trader bands the wheat_trader/tick poller derives from fields_liberated). At the Mystic band fields_liberated is 0-1 so only the trade-only PITCH shows; NOT yet hostile. The ON_DISTANCE_VERY_CLOSE forced-battle trigger only fires under wheat_trader:hostile (>=4 fields, a later band) - fail-soft here. trade snippet trade_wheat_trader. Company member wears a plain name (Sedge); the title lives in dialog. Ambush trainer wheat_trader_ambush is the SHARED forced-fight trainer (already used by wheat_trader_1); defeat_tag is per-character so beating one does not clear another. uuid omitted - latch-place at build.",
  "triggers": [
    { "on": "ON_DISTANCE_VERY_CLOSE", "actions": [ { "do": "battle", "gate": { "wheat_trader": "hostile" } } ] }
  ],
  "trade": { "snippet": "trade_wheat_trader", "open_label": "shop" },
  "battle": {
    "trainer": "wheat_trader_ambush",
    "type": "villain_forced",
    "format": "GEN_9_SINGLES",
    "prize": 400,
    "defeat_tag": "defeated_mm_wheat_trader",
    "despawn_on_win": false,
    "win_line": "Fine. Fine! Take the causeway. Take the reeds. Grain was a bad bet in a bog anyway.",
    "lose_line": "Filed. The Company thanks you for your final contribution.",
    "already_beaten_line": "No. We are not doing that twice. Word went out about you."
  },
  "placement": { "x": 1058, "y": 64, "z": 2478 }
}
```

**Dialog:** `reuse dialog:wheat_trader` (shared). No new dialog file.

**DATAPACK NEEDS:** none new — the `wheat_trader/tick` band poller and
`route/decline_*`/battle wiring already exist. The forced-battle path is dormant at this
band (only arms at `wheat_trader:hostile`).

**QUEST_TARGETS entry:** none dedicated — Sedge feeds the **existing** `q.side_wheat`
("The Wheat War") thread: talking to him sets `heard_wheat_pitch` (via the shared tree's
buttons), which is one of that holder's activation tags. No new stage needed.

**REWARD/BALANCE:** trade-only at this band (no fee, no fight). The dormant ambush fight
(`wheat_trader_ambush`, **400 CD** prize) only arms once `fields_liberated ≥ 4` (a later
town) — at Mystic Marsh it cannot whiteout anyone. `defeat_tag` is per-character
(`defeated_mm_wheat_trader`) so future recognition-ambushes stay independent.

---

### 3.5 The Mirebloom Paddy (field-liberation hook — frees `farm_2`)

**NPC:** Steward Halvard (`mm_field_guard`) at the Mirebloom Paddies gate [1229 2820].
**Concept:** the **first marsh field the player can reclaim** — the Mirebloom Paddies, a
town-communal marsh-rice field the Company fenced and now guards. Beating Halvard **liberates
`farm_2`**: pushes `cd_instability` back down (−6), advances `fields_liberated` toward the HQ
gate, eases the CobbleDollar shop tier, and worsens Granary wheat prices — the tug-of-war
made physical (LORE_BIBLE §7). This is a **direct field-guard fight** using the shipped
`liberation/free_field {field:farm_2}` lever in `on_win` (**unquoted SNBT**, matching the
shipped `villain_site_manager` `{field:farm_1}` form — no double-quotes in an onwin command).
**Forward hook:** Halvard's loss line spits toward Deepcore ("the corridor holds without one
paddy — Deepcore does not fall so easy"). **Back-echo:** the liberation ceremony references
the wider campaign (`wheat_war_active`, the running `fields_liberated` counter); if Firstfurrow
(`farm_1`) is already free, this is field #2 and the counter shows it.

Halvard is a **must-fight Company field guard** — but per the fairness floor it is only
reachable by walking up to the fenced gate (opt-in by geography), and the fight is
**CAP-LEGAL at ≤ entry cap 30 / ace 32**, so a player with a legal team can take it. He does
not pursue, and a starter-only player who never approaches the gate is never forced into it.

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "mm_field_guard",
  "display_name": "Steward Halvard",
  "role": "wheat_trader",
  "act": "1",
  "location": "Mirebloom Paddies",
  "recognition_tier": "early",
  "recipe": "grain_buyer",
  "trainer": "mm_field_guard",
  "dialog": "dialog:mm_field_guard",
  "movement": { "objective": "ambient_stationary_look" },
  "_comment": "THE MIREBLOOM PADDY (field liberation, frees farm_2 - canonical id from install.json activeWhenHolder + roadmap 03_wheat_war_farms; NOT farm_3). Company field steward on the Mirebloom Paddies gate [1229 2820]. Battle type villain_forced (no decline, no despawn) but reachable ONLY by walking to the fenced gate - opt-in by geography, does NOT pursue, cannot whiteout a player who never approaches. CAP-LEGAL: team ace <= entry cap 30 (roadmap proposes 29). on_win runs liberation/free_field {field:farm_2} UNQUOTED AS the player (the shipped field lever: -6 cd_instability, +1 fields_liberated, wheat_war_active, shop refresh, ceremony). REQUIRES: (1) a real RCT trainer id mm_field_guard under data/rctmod/trainers/; (2) farm_2 -> MIREBLOOM PADDIES added to liberation/load names map + the dispatch-board WIRING RULE. Recognition: a mid-band steward who has served long enough to have SEEN the portrait - his pre-fight say gets a recognition entry (early confused / mid alarm). No founder NAME spoken (Act 1). uuid omitted - latch-place at build.",
  "battle": {
    "trainer": "mm_field_guard",
    "type": "villain_forced",
    "format": "GEN_9_SINGLES",
    "prize": 500,
    "defeat_tag": "defeated_mm_field_guard",
    "despawn_on_win": false,
    "win_line": "The fence comes down. The corridor holds without one paddy - Deepcore does not fall so easy, whoever you are.",
    "lose_line": "The paddy stays Company ground. Verified, fenced, and filed. Try the shrine, pilgrim - it wants nothing from you.",
    "already_beaten_line": "The fence is down and the water runs free. There is nothing here to guard now. Go on.",
    "on_win": [
      "execute as @1 run function cobblemon_initiative:liberation/free_field {field:farm_2}"
    ]
  },
  "placement": { "x": 1229, "y": 64, "z": 2820 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "mm_field_guard",
  "type": "STANDARD",
  "_comment": "Steward Halvard, Mirebloom Paddies gate. RECOGNITION arc (rank x proximity): mid (prio 30, recognition mid) = long-serving steward who saw the portrait come down and is alarmed; default (prio 10) = confused-hostile early. Post-liberation (prio 40, farm_2_free) = tombstone. fight_button opens the battle (villain_forced). No decline (field guard - the paddy is the stake, not a fee). No founder NAME spoken (Act 1). CAP-LEGAL fight (<= entry cap 30 / ace 32); opt-in by geography so a starter-only player is never force-battled.",
  "entries": [
    {
      "label": "liberated",
      "name": "Steward Halvard - fence down",
      "priority": 40,
      "gate": { "tag": "farm_2_free" },
      "say": [
        "The water runs where it likes now. Marsh-rice for the town again, not for a ledger. I stood the gate because it was the job. I am not sorry it is over.",
        "Nothing to guard here anymore, pilgrim. The fen took its field back. Mind Quarry Road south - the stone is Company ground too, and Deepcore does not give an inch."
      ],
      "buttons": [ { "label": "leave_button", "text": "The marsh remembers", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "mid",
      "name": "Steward Halvard - a filed face",
      "priority": 30,
      "gate": { "recognition": "mid" },
      "say": [
        "Hold. I know that walk. I have stood this gate long enough to have seen the portraits come down - and yours was one of them, before they papered over the wall. You are supposed to be filed. You are supposed to be nothing. And here you are counting my rows with the old eyes.",
        "The memo said a saboteur. The memo did not say YOU. I do not know which of us the Company lied to worse. But the paddy is my orders, and orders are what is left when the face on the wall is gone. Come take it, then. Prove the memo wrong."
      ],
      "buttons": [ { "label": "fight_button", "text": "Take the paddy", "actions": [ { "do": "battle" } ] } ],
      "no_goodbye": true
    },
    {
      "label": "default",
      "name": "Steward Halvard",
      "priority": 10,
      "default": true,
      "say": [
        "Far enough. Mirebloom Paddies are verified Company ground - fenced, filed, and none of yours. The grain goes where the grain goes. You match a description we were told to forget, but a description is not a permit. Turn around.",
        "This water is Company water now, whatever the town says it used to be. Move along, or make it a matter for the ledger. Your choice, and the ledger always wins."
      ],
      "buttons": [ { "label": "fight_button", "text": "Take the paddy", "actions": [ { "do": "battle" } ] } ],
      "no_goodbye": true
    }
  ]
}
```

**DATAPACK NEEDS:**
- **Trainer JSON** `data/rctmod/trainers/mm_field_guard.json` (or the project's trainer dir)
  — a real RCT trainer, team ace **≤ 30 (CAP-LEGAL; roadmap proposes 29)**, marsh/grain-guard
  flavor.
- **`liberation/load.mcfunction` — add the field name + WIRING RULE dispatch note.** Extend
  the names map: `data merge storage cobblemon_initiative:liberation
  {names:{farm_2:"MIREBLOOM PADDIES"}}` so the ceremony renders the field name (a missing
  entry falls back to `THE PARCEL`). This is the one required edit to a shipped function
  (the shipped map already seeds `farm_1:"FIRSTFURROW"`; append the `farm_2` line).
- Reuses `liberation/free_field` / `free_field_apply` verbatim (the `on_win` calls it with
  the **unquoted** `{field:farm_2}`) — sets `farm_2_free` latch (`field_freed` holder `farm_2`),
  `fields_liberated +1`, `cd_instability −6`, `wheat_war_active`, `shop refresh`, and the
  ceremony title/fireworks. No new liberation function needed.

> Note: the `on_win` value is authored **unquoted** (`{field:farm_2}`) — the exact SNBT the
> shipped `villain_site_manager.json` uses for `{field:farm_1}`. No double-quotes appear in the
> onwin command (HARD RULE 1 / §13.4), and the macro value `farm_2` contains no `'` or `%`.
> The compiler lowers it into the tbcs onwin list unchanged; verify it matches the shipped
> field-guard `on_win` form at compile.

**QUEST_TARGETS entry** (append; new holder — mirrors `q.side_watch`/`q.side_deng` field beats):
```json
{
  "holder": "q.side_mirebloom",
  "name": "The Mirebloom Paddy",
  "slot": 54,
  "stages": [
    {
      "if_tags": ["met_mm_nurse"],
      "not_tags": ["farm_2_free"],
      "label": "Free the Mirebloom Paddies from the Company",
      "target": { "npc": "mm_field_guard" },
      "note": "Field-liberation hook. Lights after the rumor hub points south (met_mm_nurse). Waypoint = the paddy gate steward; his defeat runs liberation/free_field {farm_2} and latches farm_2_free (field_freed holder farm_2), closing the line."
    }
  ]
}
```

**REWARD/BALANCE:** prize **500 CD** (flat tbcs literal) + the field-liberation payload
(−6 `cd_instability`, +1 `fields_liberated`, shop relief, ceremony). Team is **CAP-LEGAL
≤ entry cap 30 / ace 32** (roadmap proposes ace 29). No decline_fee — the paddy itself is the
stake (a field guard, not a toll). Opt-in by geography (must walk to the fenced gate; Halvard
does not pursue). Fairness floor: a player with no legal team simply does not approach the
gate; nothing forces contact, so no starter-only whiteout is possible.

---

## 4. Recognition & economy beats

**Recognition band = EARLY→MID boundary (badges 2–3).** This town is where the *first
management-tier alarm* surfaces. Applied per the gradient (rank × proximity):

- **Grunts / low staff (early, confused-hostile):** Clerk Osric is Company staff but far too
  junior to have seen the portrait — he does **not** recognize the founder (line-selection
  never reaches a recognition entry for him; he is confused-*loyal*, not confused-*hostile*).
  Sedge the wheat-trader is trade-only at this band; his shared tree's `suspicious` tier
  ("a face from the old company, walking the routes. Probably nothing.") only fires if a prior
  field pushed him there — the recognition *seed*, not the payoff.
- **First management alarm (mid — Steward Halvard):** the long-serving field steward is the
  band's recognition beat. His `mid` entry (gated `recognition: mid`) is the "you are supposed
  to be *filed*" moment — a veteran who watched the portrait come down. His `default` (early)
  entry is the memo-made-flesh confused hostility ("You match a description we were told to
  forget"). **No founder NAME is spoken** (Act 1 rule).
- **Civilians never recognize the founder:** Fen-Nurse Wisteria and Marsh-Child Bryn have
  **zero** recognition — they only *feel* the decay. Wisteria reads the short coin as the fen's
  bad luck; Bryn does not register it at all.

**Economy voice (Act-1 tail → first Act-2 slip), gated on `cd_instability`:**

- **Glossy default (Act 1, idx < 16):** Osric's default entry — "Ten years of stability…
  verified trust, verified value." Wisteria's default — hospitable, unbothered.
- **Nervous (idx ≥ 16):** Osric's `nervous` entry — "a temporary recalibration… the Company
  is on top of it." Wisteria's `post_badge_2` murmur — coin comes up light, milk sours early.
- **Over-explaining (idx ≥ 24 — the Mystic target):** Osric's `over_explaining` entry — the
  full `register:economy#reassurance` cadence ("Prices are simply adjusting. That is normal.
  That is healthy. Please do not hoard."). This is the beat that makes the cheer *sound thin*.
- **Company-on-money receipt gag:** the exchange witness fee pays *short* by design
  (`economy/payout` skew), with the `ADJUSTMENT: rounding, in the Company favor.` actionbar —
  the streamable receipt moment.
- **Field liberation eases it:** freeing the Mirebloom Paddy (`farm_2`) knocks `cd_instability`
  −6 and refreshes the shop tier — the player can *watch the numbers ease* and the ceremony
  title-cards it (`◆ Field liberated — the commodity currency loses ground.`).

**Back-echoes (the world talks backward):** Wisteria references Willowmire Path and the wheat
talk drifting up from Hua Zhan; Osric references the Hua Zhan price-check corridor
(`hz_prices_done`) and Deepcore "under review"; Marigold's reading names Deepcore; the
liberation ceremony references the running `fields_liberated` counter (label owned by
`quest/render` — 6-of-10 per the current ruling, do not hardcode here) and (if `farm_1` is
already free) shows this as field #2.

---

## 5. New tags / scores introduced

| tag / score | set by | gated by (read where) |
|-------------|--------|------------------------|
| `met_mm_nurse` | `mm_nurse` dialog default buttons (`tag @s add`) | Wisteria rumor lines; `q.side_wisps` + `q.side_mirebloom` activation |
| `mm_charms_done` | `sidequest/marigold/charm_success` | Marigold `wager`/`reading_flavor` gates; Wisteria rumor button gate; `q.side_wisps` end |
| `defeated_sq_marigold_charm` | Marigold wager win (`defeat_tag`) | `reading_flavor` entry; wager not-repeat |
| `declined_sq_marigold_charm` | auto-gen `route/decline_sq_marigold_charm` | stand-down after paying decline_fee |
| `mm_board_done` | `sidequest/exchange_board/witness_pay` | Osric `done_flavor` gate; read-button gates; `q.side_verified` end |
| `ci_mm_reads` (score, `dummy`) | `sidequest/exchange_board/price_read` | witness pay threshold (`matches 3..`) |
| `defeated_mm_wheat_trader` | Sedge ambush win (`defeat_tag`, dormant band) | `already_beaten_line`; future recognition independence |
| `defeated_mm_field_guard` | Halvard field-guard win (`defeat_tag`) | (record only — liberation gates on `farm_2_free`) |
| `farm_2_free` (score `field_freed` holder `farm_2`) | `liberation/free_field_apply` (via Halvard `on_win`) | Halvard `liberated` entry; Wisteria rumor gate; `q.side_mirebloom` end |
| `heard_wheat_pitch` | **existing** (shared `wheat_trader` tree via Sedge) | **existing** `q.side_wheat` activation — no new tag, reused |

Shared/existing objectives reused (no re-declaration by this unit): `ci_sq_scratch`
(Genji/museum `load`), `cd_instability`, `fields_liberated`, `field_freed`, `memory_fragment`.
`ci_mm_reads` is the one new scoreboard — declare it in an `exchange_board/load` (or fold into
an existing load) and add it to `#load` (mirror `museum/load` / `wheat_trader/load`).

---

## 6. Build checklist

Ordered, copy-paste-compile:

1. **Create `dialog-src/characters/mystic/`** (new area folder) and drop **6 character files**:
   `mm_nurse.json`, `mm_charm_seller.json`, `mm_exchange_clerk.json`, `mm_wheat_trader.json`,
   `mm_will_o_wisp_child.json` (see Open Q3 — spec it or cut), `mm_field_guard.json`.
   (If Bryn/`mm_will_o_wisp_child` is cut, drop 5.)
2. **Drop 4 new dialog files** into `dialog-src/dialog/`: `mm_nurse.json`,
   `sq_marigold_wisps.json`, `mm_exchange_board.json`, `mm_field_guard.json`.
   (Sedge **reuses** `dialog:wheat_trader` — no file. Bryn needs a 5th tiny dialog if kept.)
3. **Add the RCT trainers** under the trainer dir: `sq_marigold_charm` (wager, 2×
   Fairy/charm-mon lv 28-29) and `mm_field_guard` (field guard, ace ≤ 30 — roadmap proposes
   the Mirebloom manager ace at 29). CAP-LEGAL both (≤ entry cap 30 / ace 32).
4. **Add datapack functions:**
   - `function/sidequest/marigold/turn_in_charm.mcfunction` (Genji copy, string count)
   - `function/sidequest/marigold/charm_success.mcfunction` (clear 8, payout 300, gift, latch)
   - `function/sidequest/exchange_board/load.mcfunction` — `scoreboard objectives add
     ci_mm_reads dummy` (+ add to `#load` tick registrar)
   - `function/sidequest/exchange_board/price_read.mcfunction` (increment + flicker actionbar)
   - `function/sidequest/exchange_board/witness_pay.mcfunction` (short payout 60, latch, receipt)
5. **Edit `function/liberation/load.mcfunction`:** add `farm_2:"MIREBLOOM PADDIES"` to the
   `names` merge (the shipped map already has `farm_1:"FIRSTFURROW"`) and note it on the
   dispatch-board WIRING RULE list. (Only edit to a shipped function.)
6. **Append 4 register holders** to `registers/quest_targets.json`: `q.side_wisps` (56),
   `q.side_verified` (55), `q.side_mirebloom` (54) — confirm slots free at synthesis. Sedge
   feeds the **existing** `q.side_wheat` (no new stage).
7. **Confirm the auto-gen decline** for `sq_marigold_charm` (compiler emits
   `route/decline_sq_marigold_charm` from `battle.decline_fee`).
8. **Run `scripts/content_compile`** — expect 6 (or 5) new presets, 4 (or 5) dialog trees,
   3 new register stages, and no `has_item`-gate or `"`/`'`/`%` validation errors. Then
   `generate_npc_function` + `update_preset_index` per the standard pipeline.
9. **Placement pass:** latch-spawn all six via `placement` (verify every `y`/tile at the
   fenced paddy gate, the Center counter — fence/remove any free Cobblemon Healing Machine so
   the paid nurse is the healing path — the charm stall, the exchange kiosk, the causeway, the
   boardwalk). Content appears after the install run + walk-up (no auto-appear on rebuild).

---

## 7. Open questions for showrunner

1. **Field id for the Mirebloom Paddy — RESOLVED to `farm_2`.** Canon is `farm_2`
   (`install.json` `activeWhenHolder: farm_2` for the Mirebloom zone; roadmap
   `03_wheat_war_farms` field table; `04_mystic_marsh` line 45). An earlier draft used
   `farm_3` (gym-3 = 3rd field intuition) — that is wrong; `farm_3` is **Westwind Fields**.
   All references corrected to `farm_2` / `farm_2_free`. Open sub-question: the HQ-raid field
   gate is **6 of 10** per the 2026-07-06 ruling but the shipped `render.mcfunction` label may
   still read 4 — confirm which is live before wiring any count-dependent copy (this doc defers
   the label to `quest/render` and does not hardcode it).
2. **Halvard as a `villain_forced` field guard vs. a decline-able toll.** I made the paddy a
   must-fight (the field is the stake, no decline_fee) but opt-in by geography (no pursuit). If
   the showrunner wants a *pay-to-skip* on field guards, add a `decline_fee` and a
   `paid_decline`-style bow-out — but that reads against "liberating a field," so I left it as
   a fight. Ruling?
3. **Marsh-Child Bryn (`mm_will_o_wisp_child`) — keep or cut?** Listed in the cast as a tiny
   civilian flavor errand (show a Fairy-type, get a wisp-lantern trinket, nudge toward the
   Fairy Shrine). It is the lightest beat and has no combat/economy weight. Kept it in the
   cast table for texture; **cut it** if five town NPCs is the ceiling for a gym town. Not
   fully specced (no JSON blocks) pending this ruling.
4. **Charm keepsake item.** The Marigold reward uses the `training_standard` gift table rather
   than a bespoke "will-o-wisp charm" item (no such id is jar-validated). If a real keepsake
   item is wanted (a held item, a curio), name a jar-validated `1.7.3` id and I will swap the
   `give` line — otherwise the gift table stands.
5. **Register slot numbers.** I proposed `q.side_wisps` 56, `q.side_verified` 55,
   `q.side_mirebloom` 54 (below the Sango/Hua-Zhan block, above nothing that collides). Confirm
   these are free, or hand me the vacant slots for the Mystic block.
6. **Exchange witness fee amount.** The short token is **60 CD face** (skews to ~45 at idx 24)
   purely for the receipt gag. If that reads as *too* small to be worth a click, bump the face
   to ~120 (still visibly short). Tuning call.
