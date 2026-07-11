# 02 — Deepcore City (Gym 4, Fighting) — Quest Pack

> **Slug:** `02_deepcore_city` · **Town:** Deepcore City · **Gym 4, Fighting, Leader Bruno**
> (`deepcore_leader`, Iron Badge) · **Entry cap 44** · **frag_4** · **cd_instability → 32**
> · **Recognition band: MID** (`badges gte 3`, true on arrival).
>
> This is a **PLAN**, implementation-ready. Every character/dialog block below is
> schema-valid and copy-paste-compilable. The only edits at build time are (a) confirming
> PLACEHOLDER coords against the builder/atlas, (b) creating the RCT trainer team files
> named in DATAPACK NEEDS, and (c) running the pipeline (§Build checklist). It deliberately
> **reuses** the shipped shared dialogs (`dialog:wheat_trader`, `dialog:grunt_recognition`),
> the shipped functions (`economy/payout`, `economy/heal_paid`), the Genji fetch idiom
> (`sidequest/genji/turn_in_rod` → `rod_success`), and the shipped compiler-generated
> `decline_<id>` pattern — see §"Don't reinvent".

---

## 1. Overview

Deepcore City is the run's **first the-horror-is-real town** and the exact halfway hinge
of Act 1. Gyms 1–3 the Company was a rumour and a short payout; at Deepcore the money
visibly wobbles (`cd_instability` steps 24 → 32, the Act-2 *reassurance* register takes
over the propaganda), and — for the first time — **the world starts recognising the
protagonist**:

- **Company staff** show the MID recognition tier automatically (the shipped
  `dialog:grunt_recognition` mid entry fires on `badges gte 3`, which is already true) —
  *you are supposed to be a closed file.*
- **Wheat traders recognise you mid-trade** at `fields_liberated >= 2` (the shipped
  `dialog:wheat_trader` suspicious tier). **This is the mandated trader-recognition beat
  for this unit** — Deepcore is where commerce first curdles into the cover-up.

The town is a **vertical Company mining town** built on a nether-star vein — the *physical
backing of the CobbleDollar* — and the Company is quietly hollowing that vein on paper
(re-verified reserves) while praising its deep core values. Against that, Leader Bruno
runs the one honest institution in a town of accountants: a knock-down brawl at the
pit-head, no gimmicks. Corporate-dread comedy played straight against a punch to the face.
It has a **dojo-of-miners-turned-fighters** culture and a **blue-collar grievance** the
quests hang on.

**The arc job this town does:**
1. Lands **frag_4** (the signing dream — the body remembering before the mind) via Bruno.
2. Fires the **MID recognition** beat for Company staff (memo-made-flesh alarm).
3. Fires the **trader recognition** beat (`fields_liberated >= 2`) — the interactive
   recognition arc's first payoff.
4. Delivers the run's strongest **scrubbing artifact before frag_7**: an intact founder
   portrait in a forgotten deep-mine branch office (oblique recognition seed).
5. **Forward hook:** names **Gaviota Port** and the **water delegation** (gym 5, cap 50).
6. **Back-echoes:** liberated fields (the trader wariness + shop relief), the courier fork,
   and the Mystic end-of-stable-feel beat now cashed out into felt instability.

**Place on the route:** approached via **R5 Quarry Road / R10 Old Caravan Road** from Mystic
Marsh (gym 3). Nearby set-dressing: **Dryrow Steading farm [1536 3867]**. Exit hands off to
**Gaviota Port** (Bruno's Iron Badge unlocks cap 44, which is Gaviota's entry gate).

**Band context table:**

| Field | Value | Note |
|---|---|---|
| Level cap | enter **44** (unlock); ace tuned to entry **37+2 = 39** | CLAUDE.md ladder: Deepcore *unlocks* 44. The shipped Bruno ace sits at **39** (Mystic's unlock 37, +2). All side/wager battles below sit **≤37** so they are cap-legal under either reading. |
| Memory fragment | **frag_4** | fired by Bruno reward `memory/gym/frag_4` (already wired) |
| `cd_instability` | 24 → **32** | fired by Bruno reward `economy/gym_destabilize` (+8) |
| Recognition band | **MID** (`badges gte 3`) | Company NPCs auto-show the mid tier |
| Trader recognition | **suspicious** at `fields_liberated >= 2` | ambush (`hostile`) only at `>= 4` (pays off at Kalahar) |

> **Cap note for the showrunner:** the unit brief and CLAUDE.md ladder both give Deepcore an
> *unlock* cap of 44, entered with Mystic's unlock (37) as the standing ceiling, so Bruno's
> ace sits at **39** (entry 37 + 2). This pack tunes **every side/wager battle at or below
> 37** so it is cap-legal on arrival regardless. See Open Question 1.

---

## 2. Cast

All quest/service bodies are **new latch-spawn NPCs** (`placement`, no `uuid`) unless they
already exist. Coords marked PLACEHOLDER are PROPOSED inside the Deepcore zone polygon
(**x 907–1272, z 3075–3342**, pit-head walkable band **y≈129**, gym cluster centroid
`~1045 129 3186`) and must be confirmed against the builder/atlas at the placement pass
(Open Question 4). Existing gym cast (Bruno, guide, ladder trainers) is out of scope here.

| id | display_name | role | one-line concept | placement anchor |
|---|---|---|---|---|
| `deepcore_nurse` | Nurse Rurik | healer | paid Center nurse; short re-verified shipments; rumour hub | `~1092 129 3208` PLACEHOLDER |
| `deepcore_martkeeper` | Sten Vale | merchant | badge_4 Pokemart; default CobbleDollars shop | `~1100 129 3215` PLACEHOLDER |
| `deepcore_foreman_kang` | Foreman Kang | quest_giver | retired union pit-foreman; SQ1 giver, smells fraud in the ore ledger | `~1120 129 3300` PLACEHOLDER |
| `deepcore_ledger_board` | Re-Verified Reserve Ledger | civilian (prop) | scrubbing-artifact board at the Company field office | `~1152 129 3284` PLACEHOLDER |
| `sq_deepcore_assessor` | Roderick | villain_grunt | Company Restructuring Officer guarding the ledger vault; MID recognition | `~1150 129 3282` PLACEHOLDER |
| `deepcore_ladder_barker` | Old Dun | quest_giver | sparring-pit barker; SQ2 Iron Ladder giver | `~1060 129 3200` PLACEHOLDER |
| `wheat_trader_deepcore` | Corliss | wheat_trader | grain buyer at the mine commissary; the recognition-at-2-fields beat | `~1180 129 3260` PLACEHOLDER |
| `deepcore_deep_office` | Abandoned Branch Office | civilian (prop) | SQ4 set-piece: intact founder portrait deep in the mine (oblique recognition seed) | `~985 40 3120` PLACEHOLDER (deep-mine level, y≈40) |
| `deepcore_miner_rill` | Miner Rill | civilian | second flavour voice; deep-core-values gag, instability grumble, Gaviota hook | `~1110 129 3230` PLACEHOLDER |

> The **rumour hub** is the **nurse** (`deepcore_nurse`) — she carries the town's quest
> pointers, gated on each quest's not-done tag, per house style. `deepcore_miner_rill` is a
> second flavour voice for the deep-core-values gag and the Gaviota forward hook.

---

## 3. Quests

Four quests: a rumour-hub service NPC that points at them; the marquee plot quest (the
reserve reveal, with the mandated Genji-idiom fetch→turn-in→opt-in wager); a fun no-heal
gauntlet; the mandated trader-recognition beat; and the scrubbing-artifact set-piece.

> **say[] authoring note (hard rule 2):** every `say[]` array below renders **one random
> line per open** — the lines are *alternatives*, never a sequential monologue. Any
> single-instance content that must always land (a plot reveal, the Gaviota hook, a stake
> figure) is delivered through a **button `announce`** or is **repeated across every
> alternative**, not stranded on a second `say[]` line the player might never roll.

---

### SQ0 — Rumour Hub: Nurse Rurik (the pointer NPC)

**Concept:** the town's paid Center nurse *is* the rumour hub (house style). Her heal rides
the shipped `economy/heal_paid`. Her dialog carries gated pointers to the three active
quests (each shown only while its not-done tag is absent), a **back-echo** to liberated
fields, and the **forward hook** to Gaviota (both delivered as button `announce`, so they
always land regardless of which `say[]` alternative rolled). Civilian: **zero recognition**
of the founder.

**Forward hook planted:** the *any news off the road* button announces the Gaviota water
delegation asking after the mine's reserve numbers.
**Back-echo referenced:** liberated fields / the courier fork (the supply road runs quieter
since the fields came back) — carried on **every** greeting alternative so it never misses.

#### Character — `dialog-src/characters/deepcore/deepcore_nurse.json`

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "deepcore_nurse",
  "display_name": "Nurse Rurik",
  "role": "healer",
  "act": "1",
  "location": "Deepcore City - Pit-Head Center",
  "recognition_tier": "early",
  "recipe": "healer",
  "service": { "kind": "heal" },
  "dialog": "dialog:deepcore_nurse",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 1092, "y": 129, "z": 3208 }
}
```

#### Dialog — `dialog-src/dialog/deepcore_nurse.json`

Each `say[]` line is a self-contained greeting alternative that *independently* carries the
back-echo (quieter supply road), so the field-liberation callback lands on any roll. The
quest pointers and the Gaviota hook are **buttons** (`announce`), gated on each quest's
not-done tag, so they never depend on which greeting rolled.

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "deepcore_nurse",
  "type": "CUSTOM",
  "entries": [
    {
      "label": "default",
      "name": "Nurse Rurik",
      "priority": 10,
      "default": true,
      "say": [
        "Rurik. I set the bones this town breaks on itself and the ones the Company breaks quieter. The fee is posted on the wall, same for a miner as for a champion. The supply road runs easier lately - somebody has been giving the fields back, they say. Sit your team down.",
        "Half my waiting room is dojo bruises, half is people who read their pay slip too closely. I keep the posted rate and I do not editorialize. My crates arrive on time again, mostly - the road runs quieter since the fields came back. Whatever you did out there, it reaches in here.",
        "Sit them down. I restock, I heal, I keep the rate. The road up from the fields runs cleaner than it did a badge ago - somebody clawed that ground back. Good. My shipments notice even if the ledgers pretend not to."
      ],
      "buttons": [
        {
          "label": "heal_button",
          "text": "Heal my team - posted rate",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:economy/heal_paid", "as_player": true },
            { "do": "close" }
          ]
        },
        {
          "label": "kang_button",
          "text": "Anyone need a hand around here?",
          "gate": { "not_tag": "deepcore_restructure_done" },
          "actions": [
            { "do": "announce", "text": "Foreman Kang, if you can stand his grumbling. Old union man, retired to a stool by the pit-head. Says the new ore ledger reads like a lie he cannot spell.", "as": "chat", "color": "gold" },
            { "do": "close" }
          ]
        },
        {
          "label": "ladder_button",
          "text": "What is the dojo shouting about?",
          "gate": { "not_tag": "iron_ladder_cleared" },
          "actions": [
            { "do": "announce", "text": "Old Dun runs the Iron Ladder down a played-out shaft. Three fights, no heals between them. Bring me the pieces after and I will not ask how it went.", "as": "chat", "color": "gold" },
            { "do": "close" }
          ]
        },
        {
          "label": "gaviota_button",
          "text": "Any news off the road?",
          "actions": [
            { "do": "announce", "text": "A water delegation came up from Gaviota Port asking after this mine - after the reserve numbers, specifically. Leader Neptune does not send people to ask about weather. Somebody down the coast is worried too.", "as": "chat", "color": "aqua" },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "Just passing", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:** none new. Heal rides shipped `cobblemon_initiative:economy/heal_paid`.

**QUEST_TARGETS entry:** none (rumour hub is not a tracked quest line).

**REWARD/BALANCE:** heal fee = shipped `100 + 2 × cd_instability` (≈164 CD at idx 32),
balance-gated by `heal_paid`. No battle. Fully fail-soft.

---

### SQ1 — Deep Restructuring (the marquee — GENJI IDIOM: fetch → turn-in → opt-in wager)

**Concept:** Foreman Kang, a retired union man, smells fraud in the mine's new re-verified
ore ledger but cannot read the doctored columns. He asks the player to **recover the struck
signature pages** the Restructuring Officer shredded — a **fetch** (bring 6× paper the
player scrounges from the shredded stacks around the field office) that Kang **turns in via
a datapack count-check** (the Genji `clear @s … 0` idiom, never a `has_item` gate). Turn-in
pays and reveals the plot; then Kang offers an **opt-in, decline-able wager** against the
Restructuring Officer at the vault — the stake **printed**, fail-soft, cap-legal.

This is the exact Genji shape: **fetch → count-check turn-in with printed pay → opt-in
printed-stake wager**. The wager target is the Company **Restructuring Officer**
(`sq_deepcore_assessor`), a side-quest villain body (not a canon roster slot) reusing the
shipped `dialog:grunt_recognition` (MID fires at `badges gte 3`).

**Forward hook planted:** Kang's debrief button names Gaviota's water delegation wanting the
same numbers (delivered as an `announce`, so it always lands).
**Back-echo referenced:** the Mystic end-of-stable-feel — every `default` alternative says
the money stopped feeling real about the third badge.

#### Character A — `dialog-src/characters/deepcore/deepcore_foreman_kang.json`

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "deepcore_foreman_kang",
  "display_name": "Foreman Kang",
  "role": "quest_giver",
  "act": "1",
  "location": "Deepcore City - Pit-Head",
  "recognition_tier": "early",
  "recipe": "quest_fetch",
  "dialog": "dialog:deepcore_foreman_kang",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 1120, "y": 129, "z": 3300 }
}
```

#### Dialog — `dialog-src/dialog/deepcore_foreman_kang.json`

Priorities: `debrief` (40, post-turn-in flavour + wager offer) > `turn_in` (30, has-started,
not-done) > `default` (10, the ask). The plot reveal and the Gaviota hook live on **buttons**
(`announce`), never stranded on a second `say[]` alternative. The wager offer is a **separate
manual-only entry** (`priority -1`, reached only via `open_dialog`) that prints the stake;
declining here is free, the *battle* decline costs a fee.

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "deepcore_foreman_kang",
  "type": "STANDARD",
  "entries": [
    {
      "label": "debrief",
      "name": "Foreman Kang - the count that lies",
      "priority": 40,
      "gate": { "tag": "deepcore_restructure_done" },
      "say": [
        "You brought back the struck pages and I read them twice. The vein count was halved on paper and re-signed under a name nobody in this pit has ever met. That is not accounting - that is a robbery with a fountain pen.",
        "I read your pages out loud to the whole shift and not one man argued. The vein got halved on paper and re-signed by a stranger. We always knew. Now we can point at the line."
      ],
      "buttons": [
        {
          "label": "coast_button",
          "text": "So who needs to see this?",
          "actions": [
            { "do": "announce", "text": "Send it downstream. Gaviota is asking after these same numbers - a water delegation came up the caravan road last week. Neptune down at the port smells it too. Let the coast have this one.", "as": "chat", "color": "aqua" },
            { "do": "close" }
          ]
        },
        {
          "label": "wager_button",
          "text": "That Officer at the vault - is he still there?",
          "gate": { "not_tags": ["defeated_sq_deepcore_assessor", "declined_sq_deepcore_assessor"] },
          "actions": [
            { "do": "announce", "text": "Still guarding a lie. He fancies himself a fighter - this is a dojo town, everyone does. His team sits under your cap. Beat him and the vault door is a formality.", "as": "chat", "color": "gold" },
            { "do": "open_dialog", "label": "wager_offer" }
          ]
        },
        { "label": "leave_button", "text": "The coast can have it", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "wager_offer",
      "name": "Foreman Kang - a dojo wager",
      "priority": -1,
      "say": [
        "Two hundred and fifty CobbleDollars says you put the Restructuring Officer on the mat. Win and the stake doubles back with a pit-crew belt. Lose and it is his to keep. Decline and it costs you nothing here - but the Officer takes a fee to let you walk his vault. No shame in walking. This is a dojo town though. Somebody always wants to see the swing.",
        "The stake is two hundred and fifty CobbleDollars, on the mat, on the Officer. Win it back doubled with a belt. Lose it and he pockets it. Walking away is free from me, though the man himself charges a toll at the vault door. Your call, challenger."
      ],
      "buttons": [
        {
          "label": "take_wager_button",
          "text": "Take the wager - 250 CD on the line",
          "gate": { "not_tags": ["defeated_sq_deepcore_assessor", "declined_sq_deepcore_assessor"] },
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/deepcore/stake_assessor", "as_player": true },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "Not today", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "turn_in",
      "name": "Foreman Kang - the pages",
      "priority": 30,
      "gate": { "tag": "deepcore_restructure_started", "not_tag": "deepcore_restructure_done" },
      "say": [
        "You back already? Six struck pages is what I need - the ones the Officer fed the shredder. They pile up in the bins around the field office. Bring me six and I will read the fraud out loud.",
        "Paper does not shred as clean as a signature. Six of them, from the bins by the field office. Hand me what you found and I will do the reading."
      ],
      "buttons": [
        {
          "label": "turn_in_button",
          "text": "Hand over 6 struck pages",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/deepcore/turn_in_pages", "as_player": true },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "Still gathering", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Foreman Kang",
      "priority": 10,
      "default": true,
      "gate": { "not_tag": "deepcore_restructure_started" },
      "say": [
        "Kang. Forty years down this pit, twenty as union foreman, until the Company deep-restructured me onto this stool. The money stopped feeling real about the time you took your third badge - I know a hollowed number when I see one. Get me six struck pages from the bins by the field office and I will show you a robbery.",
        "Deep core values, the memo said, right before they hollowed me out and left the stool. The new ore ledger reads like a lie I cannot spell - the whole vein halved on paper about the time your third badge landed. Six struck pages from the field-office bins. Read the board there first, then bring them to me."
      ],
      "buttons": [
        {
          "label": "start_button",
          "text": "I will get your pages",
          "actions": [
            { "do": "command", "cmd": "tag @s add deepcore_restructure_started", "as_player": true },
            { "do": "announce", "text": "Six struck pages from the shredder bins by the Company field office. Read the ledger board while you are there.", "as": "chat", "color": "gold" },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

#### Character B — `dialog-src/characters/deepcore/sq_deepcore_assessor.json` (the wager battle body)

Reuses the shipped `dialog:grunt_recognition` (MID entry fires on `badges gte 3`). The wager
stake is charged at accept by the SQ1 stake function; the battle block's `decline_fee` gives
the compiler-generated `decline_sq_deepcore_assessor` bow-out path (fail-soft; a broke player
is forced to fight rather than pay — the fairness floor holds because the Officer team is
in-cap and beatable).

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "sq_deepcore_assessor",
  "display_name": "Roderick",
  "role": "villain_grunt",
  "act": "1",
  "location": "Deepcore City - Company Field Office",
  "recognition_tier": "mid",
  "trainer": "sq_deepcore_assessor",
  "recipe": "villain_grunt",
  "dialog": "dialog:grunt_recognition",
  "movement": { "objective": "ambient_stationary_look" },
  "battle": {
    "trainer": "sq_deepcore_assessor",
    "type": "wager",
    "format": "GEN_9_SINGLES",
    "prize": 500,
    "decline_fee": 120,
    "defeat_tag": "defeated_sq_deepcore_assessor",
    "despawn_on_win": false,
    "win_line": "Adjustment reversed. The vault is yours to read. I was told you were a closed file - the file appears to be open.",
    "lose_line": "Non-compliance is billable. The stake reconciles in the Company favor. Do enjoy the receipt.",
    "already_beaten_line": "One reconciliation per visitor. The vault door is already a formality to you."
  },
  "placement": { "x": 1150, "y": 129, "z": 3282 }
}
```

> **Note on `dialog:grunt_recognition`'s battle button:** the shipped tree's buttons call
> `{do:"battle"}` directly, with **no stake charged**. For SQ1 the wager stake must be charged
> first, so the *canonical* entry point is **Kang's `wager_offer`** → `stake_assessor` function
> (which runs the `tbcs` battle itself, the `sq_hz_analyst` pattern). If we want the Officer's
> own dialog buttons to *also* charge the stake (talking to him directly), fork a
> `deepcore_assessor_recognition` dialog copying `grunt_recognition` with the button actions
> swapped to `function …/stake_assessor` + a `…/decline_sq_deepcore_assessor` bow-out (exactly
> as `hz_analyst` did — see its `stake_sq_hz_analyst` precedent). **Recommended:** fork it, so
> every path pays the same stake. Kept as `dialog:grunt_recognition` above for the minimal
> build; flag in Open Question 3.

**DATAPACK NEEDS:**
- `function/sidequest/deepcore/turn_in_pages.mcfunction` — Genji-idiom count-check, copied
  from `sidequest/genji/turn_in_rod`. Runs as player.
  `execute store result score @s ci_sq_scratch run clear @s <PAGE_ITEM> 0`; if
  `matches 6..` and not already done → call `restructure_success`; else `tellraw` the
  not-six-yet, check-the-field-office-bins nudge. (`ci_sq_scratch` objective already created
  by `museum/load`; reuse it — do **not** fork, exactly as Genji reuses it.)
- `function/sidequest/deepcore/restructure_success.mcfunction` — runs as player, copied from
  `sidequest/genji/rod_success`. `clear @s <PAGE_ITEM> 6`;
  `function …/economy/payout {amount:900}`; `loot give @s loot
  cobblemon_initiative:npc_gift/training_standard`; give a Black Belt held item
  (`give @s cobblemon:black_belt 1` — JAR-VALIDATE id before ship); `tag @s add
  deepcore_restructure_done`; `title @s actionbar` receipt: **RESERVE FRAUD FILED** (gold) +
  subtitle. No `cd_instability` change (the mine is not a *field* — only field liberation
  moves the meter; this quest **reveals** the plot).
- `function/sidequest/deepcore/stake_assessor.mcfunction` — copy `route/stake_sq_hz_analyst`
  exactly, retargeted: stake **250** via the `cobbledollars pay`/`remove` broke-probe; on
  paid, `title @s actionbar` Stake logged: 250 CD then `tbcs battle GEN_9_SINGLES @s vs
  rctmod:sq_deepcore_assessor onwin {1:['cobbledollars give @1 500', 'tag @1 add
  defeated_sq_deepcore_assessor', '@2 say <win_line>', 'execute as @1 run function
  cobblemon_initiative:economy/wager_sweetener'], 2:['@1 say <lose_line>']}`; broke → no
  battle, branch-does-not-extend-credit (250 CD required) actionbar. The onwin lists are
  single-quoted SNBT (required there); **no apostrophe or percent inside any onwin string, no
  double-quote anywhere**.
- `function/sidequest/deepcore/decline_sq_deepcore_assessor.mcfunction` — **auto-generated**
  by `content_compile` from `battle.decline_fee: 120` (the shipped `decline_<id>` pattern —
  do not hand-write). Broke player → forced fight (fairness floor).
- `<PAGE_ITEM>` acquisition: the struck pages are a scrounge item. Simplest cap-safe choice:
  reuse a vanilla `minecraft:paper` count (player mines/loots it near the office), matching
  Genji's `minecraft:string`. If a bespoke named item is wanted, add a loot table on the
  shredder bins; **paper is the low-risk default** — flag in Open Question 2.

**QUEST_TARGETS entry** (`registers/quest_targets.json`, new holder `q.side_reserve`,
slot **56**, highest-priority stage first):

```json
{
  "holder": "q.side_reserve",
  "name": "Deep Restructuring",
  "slot": 56,
  "note": "Deepcore SQ1. fetch 6 struck pages -> Kang turn-in reveals the reserve fraud -> optional printed-stake wager vs the Restructuring Officer. First-match highest-progress first: return-to-Kang for the wager, then gather, then read the board, then the giver.",
  "stages": [
    {
      "if_tags": ["deepcore_restructure_done"],
      "not_tags": ["defeated_sq_deepcore_assessor", "declined_sq_deepcore_assessor"],
      "label": "Optional: wager the Restructuring Officer",
      "target": { "npc": "deepcore_foreman_kang" }
    },
    {
      "if_tags": ["deepcore_ledger_seen"],
      "not_tags": ["deepcore_restructure_done"],
      "label": "Bring 6 struck pages back to Foreman Kang",
      "target": { "npc": "deepcore_foreman_kang" }
    },
    {
      "if_tags": ["deepcore_restructure_started"],
      "not_tags": ["deepcore_ledger_seen"],
      "label": "Read the re-verified ledger at the Company field office",
      "target": { "npc": "deepcore_ledger_board" }
    },
    {
      "if_tags": [],
      "not_tags": ["deepcore_restructure_started"],
      "label": "Ask Foreman Kang about the ore ledger",
      "target": { "npc": "deepcore_foreman_kang" }
    }
  ]
}
```

**REWARD/BALANCE:** turn-in pays **900 CD** (skewed via `economy/payout` — feels short at
idx 32, per plot) + Black Belt + training_standard gift. Wager: stake **250**, win returns
**500** + wager_sweetener rider + the vault; loss costs the 250 stake only (`loss_fee`
unset). Officer team **≤ L35** (under cap under either cap reading). Decline: **120 CD**
bow-out (compiler `decline_` fn), broke → forced fight (no whiteout of a player with no mons
— the fairness floor is the engine's; the Officer team is beatable in-cap). All opt-in and
printed.

---

### SQ2 — The Iron Ladder (the dojo gauntlet, no heals between rounds)

**Concept:** the dojo culture beat. Old Dun, a pit-head barker, runs a knock-down gauntlet
down a played-out shaft: **3 back-to-back fights, no heals between rounds** — brutal-Nuzlocke
spice for the chat. Entry is **opt-in** and gated on some skill shown
(`defeated_deepcore_trainer_2`, a real shipped gym-trainer defeat tag); the barker prints
the terms. Fully fail-soft (you can walk away before starting; losing a round just ends the
gauntlet, no fee).

**Forward hook planted:** Dun's cleared entry names Gaviota (Neptune's port keeps a ladder
too — theirs is underwater) — carried on **every** cleared alternative.
**Back-echo referenced:** the badge count / Bruno's gym (you climbed the Iron Ladder before
you took the Iron Badge — Bruno will hear about that).

#### Character — `dialog-src/characters/deepcore/deepcore_ladder_barker.json`

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "deepcore_ladder_barker",
  "display_name": "Old Dun",
  "role": "quest_giver",
  "act": "1",
  "location": "Deepcore City - Played-Out Shaft",
  "recognition_tier": "early",
  "recipe": "quest_fetch",
  "dialog": "dialog:deepcore_ladder_barker",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 1060, "y": 129, "z": 3200 }
}
```

#### Dialog — `dialog-src/dialog/deepcore_ladder_barker.json`

Priorities: `cleared` (40) > `in_progress` (30, ladder active, not cleared) > `default` (10).
Each round is a `tbcs` battle chained by defeat tags; the barker's buttons just *start* the
next available round (opt-in). **No heal action anywhere** — that is the whole point. The
Gaviota hook rides **both** `cleared` alternatives so it always lands.

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "deepcore_ladder_barker",
  "type": "STANDARD",
  "entries": [
    {
      "label": "cleared",
      "name": "Old Dun - laddered",
      "priority": 40,
      "gate": { "tag": "iron_ladder_cleared" },
      "say": [
        "Three in a row, no water between them. You are laddered, and this pit does not hand that out. They say Neptune keeps a ladder down at Gaviota too - theirs is underwater. Go find out how you hold your breath.",
        "Laddered. Bruno will hear you climbed the Iron Ladder before you took his badge - he respects that more than the badge, between us. When you reach the coast, ask about Neptune ladder under the water at Gaviota. Not everyone comes back up."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Laddered", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "in_progress",
      "name": "Old Dun - keep climbing",
      "priority": 30,
      "gate": { "tag": "iron_ladder_active", "not_tag": "iron_ladder_cleared" },
      "say": [
        "You are on the ladder. No heals, that is the deal - the pit does not stop to bandage you and neither do I. Face the next one when you are ready. Backing out costs you nothing but the bragging.",
        "Down here we fight the way the shaft was worked - one after another, no water between, until you cannot stand or the shaft runs out. Step up when you are ready, or step off - no fee for a wise retreat."
      ],
      "buttons": [
        {
          "label": "round_1_button",
          "text": "Fight Round 1",
          "gate": { "not_tag": "defeated_sq_ladder_1" },
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/deepcore/ladder_round_1", "as_player": true },
            { "do": "close" }
          ]
        },
        {
          "label": "round_2_button",
          "text": "Fight Round 2",
          "gate": { "tag": "defeated_sq_ladder_1", "not_tag": "defeated_sq_ladder_2" },
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/deepcore/ladder_round_2", "as_player": true },
            { "do": "close" }
          ]
        },
        {
          "label": "round_3_button",
          "text": "Fight Round 3 - the last swing",
          "gate": { "tag": "defeated_sq_ladder_2", "not_tag": "defeated_sq_ladder_3" },
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/deepcore/ladder_round_3", "as_player": true },
            { "do": "close" }
          ]
        },
        {
          "label": "claim_button",
          "text": "Claim the Iron Ladder",
          "gate": { "tag": "defeated_sq_ladder_3", "not_tag": "iron_ladder_cleared" },
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/deepcore/ladder_claim", "as_player": true },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "Step off the ladder", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Old Dun",
      "priority": 10,
      "default": true,
      "gate": { "not_tag": "iron_ladder_active" },
      "say": [
        "Old Dun. I worked this shaft till it ran out, now I run the ladder in it. Three fighters, back to back, no heals between them. Beat all three and you are laddered - a pit-crew word, worth more than coin down here.",
        "It is a dojo town. Everyone here traded a pick for a fist eventually. Three fights, no water between, and you carry every bruise into the next. You want the ladder, or you want to watch?"
      ],
      "buttons": [
        {
          "label": "start_button",
          "text": "Start the Iron Ladder",
          "gate": { "tag": "defeated_deepcore_trainer_2" },
          "actions": [
            { "do": "command", "cmd": "tag @s add iron_ladder_active", "as_player": true },
            { "do": "announce", "text": "The Iron Ladder is live. Three fights, no heals between them. Face them at Old Dun when you are ready.", "as": "chat", "color": "gold" },
            { "do": "close" }
          ]
        },
        {
          "label": "locked_button",
          "text": "I need to prove myself first",
          "gate": { "not_tag": "defeated_deepcore_trainer_2" },
          "actions": [
            { "do": "announce", "text": "Come back when you have put one of Bruno students on the mat inside the gym. The ladder is for people who can already swing.", "as": "chat", "color": "gray" },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "Just watching", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:**
- `function/sidequest/deepcore/ladder_round_1.mcfunction` (and `_2`, `_3`) — run as player.
  Each: `tbcs battle GEN_9_SINGLES @s vs rctmod:sq_ladder_1 onwin {1:['tag @1 add
  defeated_sq_ladder_1', '@2 say <round line>'], 2:['@1 say <loss line>']}`. **No heal is
  ever run between rounds** — the player's party carries damage forward. Onwin lists are
  single-quoted SNBT (required); no apostrophe/percent inside them, no double-quote anywhere.
  `_2`/`_3` gate their own start via the button's tag gate (already handled in dialog).
- `function/sidequest/deepcore/ladder_claim.mcfunction` — run as player, guard `unless
  entity @s[tag=iron_ladder_cleared]` and require `defeated_sq_ladder_3`. `function
  …/economy/payout {amount:700}`; give an Expert Belt (`give @s cobblemon:expert_belt 1` —
  JAR-VALIDATE id; fallback Muscle Band); `tag @s add iron_ladder_cleared`; `title @s
  actionbar` **IRON LADDER CLEARED** (gold) receipt.

**QUEST_TARGETS entry** (new holder `q.side_ladder`, slot **55**):

```json
{
  "holder": "q.side_ladder",
  "name": "The Iron Ladder",
  "slot": 55,
  "note": "Deepcore SQ2. Opt-in 3-round no-heal gauntlet at Old Dun. First-match highest-progress first; all stages point at the barker (the whole gauntlet is at his shaft).",
  "stages": [
    {
      "if_tags": ["defeated_sq_ladder_3"],
      "not_tags": ["iron_ladder_cleared"],
      "label": "Claim the Iron Ladder from Old Dun",
      "target": { "npc": "deepcore_ladder_barker" }
    },
    {
      "if_tags": ["iron_ladder_active"],
      "not_tags": ["iron_ladder_cleared", "defeated_sq_ladder_3"],
      "label": "Climb the Iron Ladder - no heals between rounds",
      "target": { "npc": "deepcore_ladder_barker" }
    },
    {
      "if_tags": ["defeated_deepcore_trainer_2"],
      "not_tags": ["iron_ladder_active", "iron_ladder_cleared"],
      "label": "Ask Old Dun about the Iron Ladder",
      "target": { "npc": "deepcore_ladder_barker" }
    }
  ]
}
```

**REWARD/BALANCE:** ladder mons **L34 / L35 / L37** (Fighting/Rock/Steel dojo flavour, all
≤37 under cap). Final claim: **700 CD** (via `economy/payout`) + Expert Belt, one-time
(`iron_ladder_cleared`). **Opt-in** (gated on `defeated_deepcore_trainer_2`); losing a round
ends the run with no fee; walking off costs nothing. No forced battle — fairness floor safe.

---

### SQ3 — The Better Rate (MANDATED trader-recognition-at-2-fields beat)

**Concept:** the mandated beat. Corliss, a grain buyer at the mine commissary, pitches the
Company's wheat-backed alternative currency to miners nervous about their pay slips. He
**reuses the shipped `dialog:wheat_trader`** — no new dialog. At `fields_liberated >= 2` the
tree's **suspicious entry fires**: *a face from the old company, walking the routes. Probably
nothing. Probably. Wares are open.* — he **recognises you mid-trade** and turns wary but still
trades. This is Deepcore's recognition beat. At `>= 4` the tree's hostile ambush would fire —
but that is the **forward hook that pays off at Kalahar (gym 6)**, not here.

**Forward hook planted:** the `hostile` tier (already in the shared tree) seeds the trader
ambush arc for Kalahar (`>= 4` fields).
**Back-echo referenced:** field liberation — the very same `>= 2` fields that make Corliss
wary also trip the **shop relief tier** (Bruno's badge_4 shop eases); one town, both sides of
the tug-of-war.

#### Character — `dialog-src/characters/deepcore/wheat_trader_deepcore.json`

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "wheat_trader_deepcore",
  "display_name": "Corliss",
  "role": "wheat_trader",
  "act": "1",
  "location": "Deepcore City - Mine Commissary",
  "recognition_tier": "mid",
  "recipe": "grain_buyer",
  "dialog": "dialog:wheat_trader",
  "trade": { "snippet": "trade_wheat_trader", "open_label": "shop" },
  "triggers": [
    {
      "on": "ON_DISTANCE_VERY_CLOSE",
      "actions": [
        { "do": "battle", "gate": { "wheat_trader": "hostile" } }
      ]
    }
  ],
  "battle": {
    "trainer": "wheat_trader_ambush",
    "type": "villain_forced",
    "format": "GEN_9_SINGLES",
    "prize": 400,
    "defeat_tag": "defeated_wheat_trader_ambush",
    "despawn_on_win": false,
    "win_line": "Fine. Fine. Take the fields. Take the ledgers. I never liked grain anyway.",
    "lose_line": "Filed. The Company thanks you for your final contribution.",
    "already_beaten_line": "No. We are not doing that twice. Word went out about you."
  },
  "placement": { "x": 1180, "y": 129, "z": 3260 }
}
```

> Copies `wheat_trader_1` verbatim except id/name/location/placement. The `wheat_trader_ambush`
> trainer + `defeated_wheat_trader_ambush` tag already ship (used by `wheat_trader_1/2`); the
> `ON_DISTANCE_VERY_CLOSE` hostile-gated battle only ever triggers at `>= 4` fields, so at
> gym 4 Corliss is a **suspicious-tier trader**, not an ambusher. **No new dialog and no new
> trainer required** for the Deepcore beat itself. The forced `villain_forced` ambush is the
> engine's fairness floor concern, but it is out-of-band for gym 4 and identical to the
> shipped `wheat_trader_1/2` bodies, so no new fairness exposure here.

**DATAPACK NEEDS:** none new. The three tiers (`default` / `suspicious` / `hostile`) come
from the shipped `dialog:wheat_trader`; `wheat_trader_suspicious` / `_hostile` band tags are
maintained by the shipped `function/wheat_trader/tick.mcfunction`; trade offers from
`trade_wheat_trader`.

**QUEST_TARGETS entry** (new holder `q.side_rate`, slot **54** — a light highlight, no
hard-waypoint line that only shows once the trader has been met, so it is not a dead-end):

```json
{
  "holder": "q.side_rate",
  "name": "The Better Rate",
  "slot": 54,
  "note": "Deepcore SQ3. The mandated wheat-trader recognition beat at the mine commissary. Line only shows once the pitch is heard and while the trader is not yet hostile; points at the commissary trader. No hard reward - the beat is the recognition + optional trade.",
  "stages": [
    {
      "if_tags": ["heard_wheat_pitch"],
      "not_tags": ["wheat_trader_hostile", "defeated_wheat_trader_ambush"],
      "label": "Weigh the grain buyer at the Deepcore commissary",
      "target": { "npc": "wheat_trader_deepcore" }
    }
  ]
}
```

**REWARD/BALANCE:** no cash reward and no forced fight at gym 4 — the beat *is* the
recognition + optional wheat-rate trade. Declining/leaving is free. The only battle is the
`>= 4` hostile ambush, which is out-of-band for this town (payoff at Kalahar).

---

### SQ4 — The Deep Office (the scrubbing-artifact set-piece — oblique recognition seed)

**Concept:** the mandated scrubbing-artifact discovery. Deep in a flooded, forgotten wing of
the mine sits an **abandoned Company branch office** — pre-coup, never scrubbed because the
shaft was sealed off. On the wall hangs an **intact founder portrait**: the *only* place in
the run (before Act 3) where the founder's face survives on a wall. The player interacts with
the office prop; it is a **civilian prop** (zero forced recognition, no NPC names the player),
but the audience reads it. It plants an **oblique recognition seed**: the portrait's brass
plate is scratched blank, and the player's Dark-Urge inner voice stirs — *the face is
familiar and I cannot say why*. **The founder is never named** (Act 3 rule).

**Forward hook planted:** an unshredded shipping manifest in the office routes reserves to
the coast facility (Gaviota), reinforcing the water-delegation hook — delivered as the
`manifest_button` `announce`.
**Back-echo referenced:** frag_4's signing dream — the desk holds an unsigned charter, and
the `read_button` announce echoes *my hand knows this motion* without closing it.

#### Character (prop) — `dialog-src/characters/deepcore/deepcore_deep_office.json`

Copies the `rezoning_notice_board` prop idiom (interact-only civilian, disguised body). A
STANDARD tree: a `revisited` entry (prio 20, after first read) and a `default` read entry.
Every `say[]` line is a self-contained description that independently names *a founder
portrait with a scratched-blank plate*, so the scrubbing-artifact reading lands on any roll;
the Dark-Urge stir and the Gaviota manifest are delivered on **buttons** (`announce`). **No
name, no recognition, no battle.**

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "deepcore_deep_office",
  "display_name": "Abandoned Branch Office",
  "role": "civilian",
  "act": "1",
  "location": "Deepcore City - Sealed Deep Shaft",
  "recognition_tier": "early",
  "recipe": "civilian",
  "movement": { "objective": "ambient_stationary_look" },
  "dialog_inline": {
    "kind": "dialog",
    "id": "sq_deepcore_deep_office",
    "type": "STANDARD",
    "entries": [
      {
        "label": "revisited",
        "name": "Abandoned Branch Office - the portrait",
        "priority": 20,
        "gate": { "tag": "seen_deep_office" },
        "say": [
          "The office is as you left it. Sealed air, a desk, an unsigned charter, and the portrait nobody came back to take down. The brass plate under it is scratched to bare metal. Whoever this was, they were somebody once.",
          "Still sealed, still airless, still hung with the one portrait the scrubbers never reached - full colour, the plate beneath it scratched blank. The face in the frame keeps your eyes longer than a stranger should. You do not know why. You do not like that you do not know why."
        ],
        "buttons": [
          { "label": "leave_button", "text": "Seal it again", "actions": [ { "do": "close" } ] }
        ]
      },
      {
        "label": "default",
        "name": "Abandoned Branch Office",
        "priority": 10,
        "default": true,
        "say": [
          "A Company branch office, sealed behind a played-out shaft before anyone thought to scrub it. Everywhere else the portraits came down. Here one still hangs - full colour, gilt frame, the founder of it all - because nobody living remembered this room existed. The brass nameplate beneath it has been scratched to bare metal by someone in a hurry, long ago.",
          "Sealed air and old dust, and on the far wall a portrait the coup never got to - gilt frame, full colour, the founder of the whole Company, gazing out. The nameplate under it is gouged down to raw metal. A shipping manifest still sits unshredded on the desk."
        ],
        "buttons": [
          {
            "label": "read_button",
            "text": "Look at the portrait",
            "gate": { "not_tag": "seen_deep_office" },
            "actions": [
              { "do": "command", "cmd": "tag @s add seen_deep_office", "as_player": true },
              { "do": "announce", "text": "You stand in front of the portrait a long time. Your hand rises, unbidden, as if to sign something. My hand knows this motion better than I do. You cannot say why, and no one here can tell you.", "as": "chat", "color": "dark_gray" },
              { "do": "close" }
            ]
          },
          {
            "label": "manifest_button",
            "text": "Take the shipping manifest",
            "gate": { "not_tag": "took_deep_manifest" },
            "actions": [
              { "do": "command", "cmd": "tag @s add took_deep_manifest", "as_player": true },
              { "do": "announce", "text": "The manifest routes the reserve down to Gaviota Port. The water delegation was not asking idle questions.", "as": "chat", "color": "aqua" },
              { "do": "close" }
            ]
          },
          { "label": "leave_button", "text": "Step back into the dark", "actions": [ { "do": "close" } ] }
        ]
      }
    ]
  },
  "placement": { "x": 985, "y": 40, "z": 3120 }
}
```

**DATAPACK NEEDS:**
- None strictly required (tags are set by the dialog buttons). **Optional stinger:** a
  `function/sidequest/deepcore/deep_office_whisper.mcfunction` if we want the Dark-Urge line
  delivered through the existing whisper system instead of a plain `announce` — recommend
  routing through the shipped Dark-Urge whisper channel if one exists so the tone matches
  faint-whispers (flag as Open Question 5). The `announce` above is the fail-soft default.

**QUEST_TARGETS entry** (new holder `q.side_office`, slot **53** — a discovery line that
only appears once SQ1 has started so it does not spoil the deep shaft cold):

```json
{
  "holder": "q.side_office",
  "name": "The Deep Office",
  "slot": 53,
  "note": "Deepcore SQ4. Scrubbing-artifact set-piece deep in the sealed shaft (y~40). Line appears once the restructure quest is underway (a reason to be down the shaft) and clears once the portrait is seen. Oblique recognition seed - never names the founder.",
  "stages": [
    {
      "if_tags": ["deepcore_restructure_started"],
      "not_tags": ["seen_deep_office"],
      "label": "Something is sealed deep in the played-out shaft",
      "target": { "npc": "deepcore_deep_office" }
    }
  ]
}
```

**REWARD/BALANCE:** no cash, no battle, no forced recognition. The reward is narrative (the
strongest scrubbing artifact before frag_7) + the manifest (Gaviota hook item, flavour tag
only). Fully fail-soft, civilian prop — cannot recognise or fight the player.

---

## 4. Recognition & economy beats

### Villain recognition (band = MID, `badges gte 3`, true on arrival)

- **Company staff** (`sq_deepcore_assessor` / any Company face here) reuse
  `dialog:grunt_recognition`'s **mid** entry, which fires automatically at 3+ badges:
  > *Your face is on a memo. The kind with a black bar over the name and the words do not
  > engage, report immediately. You are supposed to be a closed file. Closed files do not get
  > to walk away.*
  The Officer's `win_line` cashes the same beat: *I was told you were a closed file - the
  file appears to be open.* Veterans/management alarm, not reverence — canon MID gradient.
- **Wheat trader** (`wheat_trader_deepcore`) recognition is **separate** (keyed on
  `fields_liberated`, not badges). At `>= 2` fields the shipped **suspicious** tier fires:
  > *Something about you sits wrong, like a number that will not add… A face from the old
  > company, walking the routes. Probably nothing. Probably. Wares are open.*
  This is the mandated Deepcore trader-recognition beat.
- **Civilians** (nurse Rurik, miner Rill, both props) **never** recognise the founder — they
  only feel the propaganda decay (deep core values, short pay slips). Canon.
- **The deep-office portrait** (SQ4) is the **oblique recognition seed**: the world's face,
  not the world's words — no NPC names the player, the Dark Urge stirs, the name stays blank.

### Economy voice (gate on `cd_instability` = 32, Act-2 reassurance register)

- At idx 32 payouts feel **short** (the shipped `economy/payout` haircut,
  `rate = 100 − min(idx/4, 25)` → ~92% of face; the per-payout rate line narrates it).
  Nurse/mart and miner Rill carry the reassurance register: *prices are adjusting,* *my pay
  slip reads short again.*
- The wheat trader's **default pitch** (for players below 2 fields) is the Act-2
  alternative-currency sell already in the shared tree (back your savings in something you
  can hold).
- **Field-liberation tug-of-war:** at `fields_liberated >= 2` the shipped shop-relief tier
  (`badge_4_relief1`) eases Bruno's badge_4 CobbleDollar prices **and** the same threshold
  makes the trader wary — the town where your clawed-back fields visibly help the currency is
  the same town where the cover-up first eyes you. No new wiring; both key on the shipped
  `fields_liberated` bands.
- **Streamable receipts** (title/subtitle/actionbar): SQ1 turn-in **RESERVE FRAUD FILED**;
  SQ2 claim **IRON LADDER CLEARED**; wager stake **Stake logged: 250 CD**; all payouts print
  the yellow rate receipt via the shipped `pay_macro`.

---

## 5. New tags/scores introduced

| tag / score | set by | gated by (read where) |
|---|---|---|
| `deepcore_restructure_started` | Kang `start_button` (SQ1) | Kang `turn_in`/`default` entries; SQ4 office line; `q.side_reserve`/`q.side_office` stages |
| `deepcore_ledger_seen` | ledger-board `read_button` (SQ1 prop) | `q.side_reserve` bring-pages stage (gates the turn-in pointer) |
| `deepcore_restructure_done` | `restructure_success.mcfunction` (SQ1 turn-in) | Kang `debrief`; nurse `kang_button` hides; `q.side_reserve` wager stage |
| `defeated_sq_deepcore_assessor` | `stake_assessor` onwin (SQ1 wager) | Kang wager buttons; `sq_deepcore_assessor` `already_beaten`; `q.side_reserve` |
| `declined_sq_deepcore_assessor` | `decline_sq_deepcore_assessor` (compiler-gen) | Kang/officer wager gates; `q.side_reserve` |
| `iron_ladder_active` | Old Dun `start_button` (SQ2) | Old Dun `in_progress`/`default` entries; `q.side_ladder` |
| `defeated_sq_ladder_1` / `_2` / `_3` | `ladder_round_1/2/3` onwin (SQ2) | Old Dun round-button gates; `q.side_ladder` |
| `iron_ladder_cleared` | `ladder_claim.mcfunction` (SQ2) | Old Dun `cleared`; nurse `ladder_button` hides; `q.side_ladder` |
| `seen_deep_office` | office `read_button` (SQ4 prop) | office `revisited`; `q.side_office` |
| `took_deep_manifest` | office `manifest_button` (SQ4 prop) | office `manifest_button` self-gate (one-time) |
| `heard_wheat_pitch` | shipped `dialog:wheat_trader` default buttons | **shipped** — `q.side_rate` stage (reused, not new) |
| `wheat_trader_suspicious` / `_hostile` | **shipped** `wheat_trader/tick.mcfunction` | **shipped** — SQ3 tiers (reused) |
| `defeated_deepcore_trainer_2` | **shipped** gym trainer (SQ2 unlock gate) | **shipped** — Old Dun `start_button`/`locked_button`; `q.side_ladder` |
| `ci_sq_scratch` (score) | **shipped** `museum/load` | **reused** by `turn_in_pages` count-check (do not fork) |

Band-tag inverses (`no_deepcore_restructure_started`, etc.) are auto-derived by
`content_compile` for any story tag used in a gate — no manual list needed.

---

## 6. Build checklist (ordered)

1. **Create the character folder** `dialog-src/characters/deepcore/` and drop the **7 character
   files:** `deepcore_nurse.json`, `deepcore_martkeeper.json` (copy `hz_martkeeper`, service
   `shop_cobbledollars`, badge_4 shop, minimal dialog), `deepcore_foreman_kang.json`,
   `sq_deepcore_assessor.json`, `deepcore_ladder_barker.json`, `wheat_trader_deepcore.json`,
   `deepcore_deep_office.json`. Optionally `deepcore_miner_rill.json` (civilian flavour) and
   `deepcore_ledger_board.json` (copy `rezoning_notice_board`, sets `deepcore_ledger_seen`).
2. **Drop the 4 non-inline dialog files:** `dialog/deepcore_nurse.json`,
   `dialog/deepcore_foreman_kang.json`, `dialog/deepcore_ladder_barker.json`, and (if martkeeper
   gets its own) `dialog/deepcore_martkeeper.json`. (SQ3 reuses `dialog:wheat_trader`; SQ1
   Officer reuses `dialog:grunt_recognition` or a fork per Open Question 3; SQ4 is inline.)
3. **Confirm placements** against the builder/atlas (all coords PLACEHOLDER; verify the y≈129
   pit-head band for town NPCs and the y≈40 deep-shaft level for `deepcore_deep_office` — Open
   Question 4). A character with `placement` and no `uuid` latch-spawns once per world.
4. **Create the RCT trainer team files** under `data/rctmod/trainers/` + spawn defs under
   `data/rctmod/mobs/trainers/single|groups/`: `sq_deepcore_assessor.json` (single, ≤L35,
   management flavour — e.g. Watchog/Mightyena/Bisharp-lite), `sq_ladder_1/2/3.json`
   (Fighting/Rock/Steel, L34/35/37). Register their side-quest entries in
   `trainers/side_quests/act1.json` (copy the `sq_hz_analyst` entry shape).
5. **Write the datapack functions** under `function/sidequest/deepcore/`:
   `turn_in_pages` (copy `sidequest/genji/turn_in_rod`), `restructure_success` (copy
   `sidequest/genji/rod_success`), `stake_assessor` (copy `route/stake_sq_hz_analyst`),
   `ladder_round_1/2/3`, `ladder_claim`. (`decline_sq_deepcore_assessor` is auto-generated
   from `battle.decline_fee`.) JAR-VALIDATE all item ids (`cobblemon:black_belt`,
   `cobblemon:expert_belt`, the page item) before ship. **No `"` in any text; no `'`/`%` in any
   cmd/onwin/announce content.**
6. **Add the 4 QUEST_TARGETS holders** to `registers/quest_targets.json`: `q.side_reserve`
   (slot 56), `q.side_ladder` (55), `q.side_rate` (54), `q.side_office` (53). Slots ≤56 are free
   (existing side quests occupy 57–81; slot 52 spare for miner flavour if tracked).
7. **Run the pipeline in order:** `scripts/content_compile` → `scripts/generate_granary_tiers`
   (if trade tiers touched) → `scripts/update_preset_index` → `scripts/generate_npc_function`.
   This lowers characters to `data/easy_npc/.../<id>.npc.snbt`, derives the new story tags +
   `no_*` inverses, and rebuilds `quest_waypoints.json` + `update_npc_presets.mcfunction`.
8. **Runtime smoke test** (mod loader only, no `/reload` — install-once): walk up to each latch
   NPC within 40b, press `]` to track each quest, verify: heal fee prints, SQ1 fetch→turn-in
   count-check fires and pays, wager stake charges/refunds, ladder runs with no mid-round heal,
   trader shows suspicious tier at 2 fields, office portrait announces and never names the
   founder.

---

## 7. Open questions for showrunner

1. **Entry cap 37 vs 44.** The brief/CLAUDE.md give Deepcore an *unlock* cap of 44, entered
   with Mystic's unlock (37) standing, so Bruno's ace sits at 39 (entry 37 + 2). This pack
   tunes every side/wager ≤37 so it is legal either way — but confirm the standing entry
   ceiling so the ladder RCT teams (SQ2) are tuned to the right cap.
2. **Struck-pages item.** Use vanilla `minecraft:paper` (Genji-parallel, zero-risk) for SQ1's
   fetch, or a bespoke named Struck Ledger Page item with a shredder-bin loot table? Paper is
   the low-risk default; a named item reads better on stream. Ruling needed before writing
   `turn_in_pages`/`restructure_success`.
3. **Officer dialog: reuse vs fork.** SQ1's wager stake is charged by Kang's `wager_offer` →
   `stake_assessor`. If the player can also *start the fight by talking to the Officer directly*,
   his `dialog:grunt_recognition` buttons call `{do:"battle"}` with no stake. Recommend forking a
   `deepcore_assessor_recognition` dialog (copy grunt_recognition, swap buttons to
   `stake_assessor` + bow-out) so every path pays the same stake (the `hz_analyst` precedent).
   Confirm.
4. **Walkable Y.** Confirm the pit-head surface Y (roadmap notes gym cluster at y≈129 vs zone
   `centerY` 64) and that a **sealed deep shaft at y≈40** exists as walkable space for SQ4. If
   the deep office has no world location, either the builder carves the sealed wing or SQ4
   relocates to a surface old-assay-office (loses some of the buried-secret punch).
5. **Dark-Urge delivery for SQ4.** Deliver the office portrait's inner-voice line via a plain
   `announce` (default, fail-soft) or route it through the existing Dark-Urge whisper channel so
   it matches faint-whisper tone? Confirm whether a callable whisper function exists.
6. **Miner Rill / ledger board scope.** Both are optional flavour/prop bodies. Ship them for the
   deep-core-values gag + the black-and-white ledger reveal (recommended — the ledger board is
   the scrubbing artifact SQ1's `deepcore_ledger_seen` gate expects), or fold the ledger read
   into the Officer/office and cut the extra bodies?
```
