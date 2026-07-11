# 06 — Ryujin Keep (Gym 8, Dragon) — Town Quest Unit

> Implementation-ready quest plan for the town of **Ryujin Keep**. This is copy-paste-compile:
> every character/dialog block below is schema-valid against `dialog-src/schema/README.md`,
> every macro-delivered line is free of `"` `'` `%`, and every above-cap fight is opt-in,
> decline-able, and fail-soft. Idiom follows the GOLD example (Genji `turn_in_rod`
> count-check + opt-in printed-stake wager) and the Tomo multi-stage cite-back chain.
>
> Design-source companion: `docs/roadmap/10_ryujin_keep.md` (gym ladder, coords, open Qs).
> This file owns the **town side-quest layer** (rumor hub + 4 quests); it does NOT touch the
> gym ladder, the rift gimmick, or the shrine build.

---

## 1. Overview

**The set-piece.** Ryujin Keep is a feudal dragon fortress on a sky-spire — the town sits
at ground level (`centerY 64`, polygon ~ x 2072–2423, z 811–1150) and the gym is a vertical
climb to a battle floor at **y ≈ 201**. It is approached from the south via **R13 Dragonspine
Pass**. This is the **one door the Company never got through**: a sovereign institution with a
dragon-honor culture older than any charter, whose keeper looks at the amnesiac and all but
calls him a king. The **Rift dragon** (overworld Ender Dragon, `dragon_slain` tag) gates the
leader — owned by the gym unit, not here.

**Band context (the arc job this town does).**

| Field | Value |
|---|---|
| Gym | 8 (Dragon), leader **Ryujin** (`ryujin_leader`) |
| Entry cap | **68** (leader ace = **70**, fought underlevelled) |
| Memory fragment | **frag_8 — "You did not just sign it. You built it."** (already wired: `memory/gym/frag_8`) |
| `cd_instability` | **holds 25** — DJ fell at HQ, `hq_stabilize` clamped it down; **CURRENCY STABILIZED** already fired |
| Recognition tier | **late** (7+ badges) — some Company remnants **stand down** rather than raise a hand against the founder |
| Villain act | **late Act 2 / post-HQ tail** — gate the town tone on `defeated_villain_boss` (DJ down) |
| Economy voice | **corrupted propaganda** register (`§k` glitch, cover-up leaking: "we told them the founder retired") |
| Frag | frag_8 — **the amnesia gets specific**: not signed, *built*. The world reacts to the raid landing. |

**The arc job.** Ryujin Keep is the **first town after the HQ raid** — the first place the
world reacts to DJ falling. It does four things: (1) it is where **recognition runs hottest**
(the erasure never reached this keep, so old-timers see the founder's *bearing*); (2) it stages
the **stand-down beat** (a defector who will not raise a hand against the founder); (3) it plays
the **post-HQ corrupted-propaganda beat** (a scrubbing artifact where the cover-up glitches in
real time); and (4) it lands a **back-echo to CURRENCY STABILIZED** (the lowlands felt the air
change; the keep heard the name before the memo could reach it). Forward hook: name **Nifl /
R14 Frostveil Pass** north.

**Route position.** ...Cyber City (gym 7) → **HQ raid → Acting CEO DJ** → **R13 Dragonspine
Pass → RYUJIN KEEP (gym 8)** → **R14 Frostveil Pass → Nifl Town (gym 9)**.

---

## 2. Cast

All coords sourced from `docs/roadmap/10_ryujin_keep.md §3` (ground-hub cluster is PROPOSED —
builder should nudge onto real ledges/rooms; all sit inside the confirmed keep polygon).

| id | display_name | role | one-line concept | placement anchor |
|---|---|---|---|---|
| `ryujin_keeper_hana` | Keepwarden Hana | quest_giver | **Rumor hub.** Keep steward at the gate; points at the town quests, gated on each not-done tag. | `~[2146, 64, 900]` PROPOSED |
| `ryujin_records_officer` | A Nervous Clerk | villain_grunt (defector) | **SQ1 stand-down.** Ex-HQ records officer hiding in an alcove; recognises the founder, stands down, hands over a scrubbing artifact. No battle. Roled `villain_grunt` (a Company person, so recognising the founder is canon-legal) with the non-combat `civilian` recipe. | `~[2160, 64, 890]` PROPOSED |
| `ryujin_heritage_envoy` | Heritage Acquisitions Envoy | villain_management | **SQ2 villain.** Company envoy trying to *buy the keep charter as a brand asset*; recognises the founder mid-pitch; opt-in above-cap fight OR cite-the-charter talk-past. | `~[2143, 64, 902]` PROPOSED |
| `ryujin_charter_lectern` | The Sovereign Charter | civilian (prop) | **SQ2 scrubbing artifact.** Prop lectern (rezoning-board pattern); the founding charter with half its signatures re-verified under new names. | `~[2145, 64, 901]` PROPOSED |
| `ryujin_smith_tetsu` | Dragonsmith Tetsu | quest_giver | **SQ3 fetch (GOLD idiom).** Keep smith; restrings a dragon-scale mail if you bring him dragon-scales, then an opt-in above-cap wager against his two dragons. | `~[2148, 64, 897]` PROPOSED |
| `ryujin_skywatcher_rei` | Skywatcher Rei | elder | **SQ4 lore + back-echo + forward hook.** West-parapet watcher; the ancient oath of the keep, the stabilised currency echo, and the road north to Nifl. | `~[2120, 66, 900]` PROPOSED |

> `ryujin_leader` (Leader Ryujin) and `ryujin_guide` already exist under
> `dialog-src/characters/gym/` and are owned by the gym unit — **not re-authored here.** The
> roadmap notes both currently lack `placement`/`uuid` (gym-unit gap; out of scope for this doc).

---

## 3. Quests

Four quests: **SQ1 The Nervous Clerk** (stand-down + corrupted-propaganda beat, no battle),
**SQ2 Heritage Acquisitions** (villain rebuff, opt-in above-cap fight OR cite-back),
**SQ3 The Broken Mail** (GOLD fetch→turn-in→opt-in wager), **SQ4 The First Oath** (lore hub,
back-echo to CURRENCY STABILIZED, forward hook to Nifl). Plus the **rumor-hub NPC** Keepwarden
Hana who points at all four.

All town-tone quests gate on `defeated_villain_boss` (post-HQ) so the town does not react to
the raid before it happens. Payouts route through `economy/payout {amount:N}` (skew-aware);
`cd_instability` holds 25, so payouts run at full face value here.

---

### RUMOR HUB — Keepwarden Hana

**Concept.** The house-style rumor-hub greeter at the keep gate. One STANDARD entry per open
quest, gated on that quest's not-done tag, prioritised so the most-progressed pointer wins.
Plants the **forward hook** (Nifl / Frostveil Pass) once the badge is won, and carries a
**back-echo** line to the stabilised currency.

- **Forward hook:** names Nifl Town and R14 Frostveil Pass north.
- **Back-echo:** references CURRENCY STABILIZED (the lowlands felt the air change).

**Character** — `dialog-src/characters/ryujin/ryujin_keeper_hana.json`
```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "ryujin_keeper_hana",
  "display_name": "Keepwarden Hana",
  "role": "quest_giver",
  "act": "3",
  "location": "Ryujin Keep - Gate",
  "recognition_tier": "late",
  "recipe": "quest_fetch",
  "dialog": "dialog:ryujin_rumor_hub",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 2146, "y": 64, "z": 900 }
}
```

**Dialog** — `dialog-src/dialog/ryujin_rumor_hub.json`
```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "ryujin_rumor_hub",
  "type": "STANDARD",
  "entries": [
    {
      "label": "badge_done",
      "name": "Hana - the road north",
      "priority": 40,
      "gate": { "defeated": "ryujin_leader" },
      "say": [
        "The Sovereign Badge. Ryujin does not give those to strangers, and yet you did not feel like one to him. Rest, then climb down and go north - Frostveil Pass, and past it Nifl Town, where the cold keeps better records than we do.",
        "They say the lowlands steadied. The money holds again. Up here we felt the air change before any courier arrived - the way the dragons quiet when a storm has finally broken. You broke it."
      ],
      "buttons": [ { "label": "leave_button", "text": "North it is", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "point_oath",
      "name": "Hana - the watcher",
      "priority": 24,
      "gate": { "defeated": "villain_boss", "not_tag": "ryujin_oath_told" },
      "say": [
        "Skywatcher Rei keeps the west parapet and the oldest of our stories. Ask about the first oath, if you want to know why this keep never sold. It is worth the wind."
      ]
    },
    {
      "label": "point_smith",
      "name": "Hana - the smith",
      "priority": 22,
      "gate": { "defeated": "villain_boss", "not_tag": "ryujin_mail_done" },
      "say": [
        "Dragonsmith Tetsu works the forge past the stair. His scale-mail came apart years back and he will not climb without it. Bring him what a dragon sheds and he mends two - one for him, one for the fool who helped."
      ]
    },
    {
      "label": "point_envoy",
      "name": "Hana - the buyer",
      "priority": 20,
      "gate": { "defeated": "villain_boss", "not_tag": "ryujin_heritage_settled" },
      "say": [
        "There is a Company man at our charter lectern with a fat offer and a soft smile. Heritage Acquisitions, he calls it - he wants to buy the paper this keep was raised on. The Charter is not for sale, but the keep listens to the wrong voices these days. It might listen to you."
      ]
    },
    {
      "label": "point_clerk",
      "name": "Hana - the frightened man",
      "priority": 18,
      "gate": { "defeated": "villain_boss", "not_tag": "ryujin_ledger_taken" },
      "say": [
        "There is a man sleeping in the east alcove who flinches at his own shadow. Says he ran from the lowland office after the acting seat fell. He will not talk to the wardens. He might talk to you - he keeps looking at your face like he owes it money."
      ]
    },
    {
      "label": "default",
      "name": "Keepwarden Hana",
      "priority": 10,
      "default": true,
      "say": [
        "Welcome to Ryujin Keep, challenger. We measure people here by bearing, not by the paper they carry - which is why the Company never got through our gate, whatever they got through everyone elses.",
        "Climb when you are ready. The stair is the ladder - wardens, then the apprentice, then the keeper. But look up first. The sky has been wrong since the acting seat fell in the lowlands."
      ]
    }
  ]
}
```

- **DATAPACK NEEDS:** none (pure dialog; gates read existing tags `defeated_villain_boss`,
  `defeated_ryujin_leader`, and quest-done tags set by SQ1–SQ4).
- **QUEST_TARGETS entry:** the rumor hub is not a tracked sidebar line (it is the pointer); no
  register stage.
- **REWARD/BALANCE:** none — informational NPC.

---

### SQ1 — The Nervous Clerk (stand-down + corrupted-propaganda beat)

**Concept.** An ex-HQ records officer — **a Company defector, not a townsperson** — fled to the
keep after DJ fell and has been sleeping in an alcove. Because he is Company personnel, his
recognising the founder is canon-legal (civilians never do). He recognises the founder's face
(**late tier: stands down**), assumes he is about to be "filed," and instead hands over a
re-verified ledger page as a peace offering — a **scrubbing artifact** where the cover-up
glitches in real time (*the founder retired → there was never a founder*). **No battle** (canon:
some stand down; he will not raise a hand against the founder).

- **Forward hook:** his last line gestures at what is north — the cold keeps the founder on file.
- **Back-echo:** references the HQ raid landing (the acting seat fell; the memos stopped coming).
- **Corrupted-propaganda beat:** the ledger page itself is the glitching cover-up
  (`register:scrubbing#retired_to_never` / `#corrupted` voice, inlined so it is stable).

**Character** — `dialog-src/characters/ryujin/ryujin_records_officer.json`
```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "ryujin_records_officer",
  "display_name": "A Nervous Clerk",
  "role": "villain_grunt",
  "act": "3",
  "location": "Ryujin Keep - East Alcove",
  "recognition_tier": "late",
  "_comment": "A Company DEFECTOR, not a civilian - he is ex-HQ personnel, so recognising the founder is canon-legal (civilians NEVER recognise the founder). Roled villain_grunt for the recognition legality; recipe overridden to the non-combat civilian bundle because he stands down and NEVER battles (no battle block). No hostility, no trainer id.",
  "recipe": "civilian",
  "dialog": "dialog:sq_ryujin_clerk",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 2160, "y": 64, "z": 890 }
}
```

**Dialog** — `dialog-src/dialog/sq_ryujin_clerk.json`
```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "sq_ryujin_clerk",
  "type": "STANDARD",
  "entries": [
    {
      "label": "after",
      "name": "Clerk - filed under nobody",
      "priority": 30,
      "gate": { "tag": "ryujin_ledger_taken" },
      "say": [
        "You took the page. Good. I feel lighter, and I have never once in my career felt lighter about paperwork. If you go north, past the cold, the offices up there keep the oldest files - they never purge the frozen ones. Whatever they scrubbed of you in the lowlands, Nifl still has a copy somewhere in the ice.",
        "Whoever you were, the paperwork says you are no one now. It has never been wrong about anything except this."
      ],
      "buttons": [ { "label": "leave_button", "text": "Rest easy", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "offer_page",
      "name": "Clerk - the peace offering",
      "priority": 20,
      "gate": { "tag": "ryujin_defector_met", "not_tag": "ryujin_ledger_taken" },
      "say": [
        "Please. I am not here to file anyone. I file myself now, mostly. Take this - I carried it out of the office the night the acting seat fell. A ledger page. Read what they did to it. Then let me alone, and I will never say I saw you."
      ],
      "buttons": [
        {
          "label": "read_page_button",
          "text": "Read the ledger page",
          "actions": [ { "do": "open_dialog", "label": "the_page" } ]
        },
        { "label": "leave_button", "text": "Keep it for now", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "the_page",
      "name": "The re-verified ledger page",
      "priority": -1,
      "say": [
        "One page, re-verified twice. The top line reads: the founder retired, effective immediately, with gratitude for years of service. The line below it, stamped later, in fresher ink: correction - there was never a founder. Both stamps are official. Both are signed by the same hand, and the ink still glitters where they scratched out the first truth to paste the second over it."
      ],
      "buttons": [
        {
          "label": "take_page_button",
          "text": "Take the page",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/ryujin/take_ledger", "as_player": true },
            { "do": "open_dialog", "label": "after" }
          ]
        },
        { "label": "leave_button", "text": "Leave it with him", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "A Nervous Clerk",
      "priority": 10,
      "default": true,
      "gate": { "defeated": "villain_boss" },
      "say": [
        "No - no, please, I - I know that face. I have seen that face. It came down off the wall of the main office three years ago and I helped carry the frame and I promised myself I would never - and here you are, walking around with it like it is allowed. I am not going to fight you. I would sooner file my own resignation with my teeth.",
        "I am nobody. I processed forms. I never gave an order in my life, which is more than I can say for whoever wore your face before you did."
      ],
      "buttons": [
        {
          "label": "reassure_button",
          "text": "I am not here to file anyone",
          "actions": [
            { "do": "command", "cmd": "tag @s add ryujin_defector_met", "as_player": true },
            { "do": "open_dialog", "label": "offer_page" }
          ]
        },
        { "label": "leave_button", "text": "Steady. I will go", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

- **DATAPACK NEEDS:**
  - `function/sidequest/ryujin/take_ledger.mcfunction` — run as player: `give @s
    minecraft:paper 1` (a keepsake page; optional custom name via a follow-up if the item
    framework supports it) → `function cobblemon_initiative:economy/payout {amount:400}` →
    `loot give @s loot cobblemon_initiative:npc_gift/training_standard` → `tag @s add
    ryujin_ledger_taken` → `title @s actionbar [{"text":"Filed. ","color":"gold"},{"text":"The
    cover-up leaks one page at a time.","color":"gray"}]`.
- **QUEST_TARGETS entry** (add to `dialog-src/registers/quest_targets.json`):
```json
{
  "holder": "q.side_clerk8",
  "name": "The Nervous Clerk",
  "slot": 44,
  "stages": [
    {
      "if_tags": ["ryujin_defector_met"],
      "not_tags": ["ryujin_ledger_taken"],
      "label": "Take the ledger page from the clerk",
      "target": { "npc": "ryujin_records_officer" },
      "note": "Post-HQ stand-down beat; the clerk hands over a scrubbing artifact. No battle."
    }
  ]
}
```
- **REWARD/BALANCE:** 400 CD (full face value; idx 25) + training loot (standard) + keepsake
  page. **No battle** — nothing to be cap-legal about; this is a stand-down by design. No decline
  cost (nothing is demanded of the player).

---

### SQ2 — Heritage Acquisitions (villain rebuff — opt-in above-cap OR cite-back)

**Concept.** A Company **Heritage Acquisitions envoy** is at the keep with a padded offer to buy
the keep's founding charter — "to preserve it" (i.e. keep scrubbing history: the same
weaponised-trust logic as the currency, now aimed at *memory*). The player reads the charter
(a **scrubbing artifact** — half its signatures re-verified under new names), then confronts the
envoy. He recognises the founder mid-pitch (**late tier: alarm, then panic**) and it resolves two
ways: an **opt-in above-cap battle** (villain-management tier, ace **72** > cap 68 — stake
PRINTED, decline-able for a CD toll) **or** a **cite-the-charter talk-past** (the Tomo cite-back
idiom) — both set the same resolution tag, fail-soft.

- **Forward hook:** the envoy, withdrawing, mutters the Company is "reassessing the northern
  assets" (Nifl).
- **Back-echo:** references the currency stabilising (his budget was cut when the acting seat
  fell; heritage is all HQ will still fund).

**Character (envoy)** — `dialog-src/characters/ryujin/ryujin_heritage_envoy.json`
```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "ryujin_heritage_envoy",
  "display_name": "Heritage Acquisitions Envoy",
  "role": "villain_management",
  "act": "3",
  "location": "Ryujin Keep - Charter Lectern",
  "recognition_tier": "late",
  "trainer": "ryujin_heritage_envoy",
  "recipe": "management",
  "dialog": "dialog:sq_ryujin_heritage",
  "movement": { "objective": "ambient_stationary_look" },
  "battle": {
    "trainer": "ryujin_heritage_envoy",
    "type": "villain_management",
    "format": "GEN_9_SINGLES",
    "prize": 1200,
    "decline_fee": 400,
    "defeat_tag": "defeated_ryujin_heritage_envoy",
    "despawn_on_win": true,
    "win_line": "Acquisition failed. The asset is - hostile. I am revising the valuation and the northern portfolio both. This was never sanctioned by anyone whose name I can still say.",
    "lose_line": "The Company thanks the keep for its cooperation. Heritage class. Retained. As it was always going to be.",
    "already_beaten_line": "The offer is withdrawn pending clarification I will never receive. There is no one left to clarify it.",
    "on_win": [ "tag @1 add ryujin_heritage_settled" ]
  },
  "placement": { "x": 2143, "y": 64, "z": 902 }
}
```

**Character (charter lectern prop)** — `dialog-src/characters/ryujin/ryujin_charter_lectern.json`
```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "ryujin_charter_lectern",
  "display_name": "The Sovereign Charter",
  "role": "civilian",
  "act": "3",
  "location": "Ryujin Keep - Charter Lectern",
  "_comment": "Prop-style interaction NPC (rezoning_notice_board pattern) - the keep founding charter on a lectern; a scrubbing artifact. Disguise via Easy NPC model (small scale / against the lectern). Zero world build. Reading it sets ryujin_charter_read, which unlocks the cite-back button on the envoy.",
  "recipe": "civilian",
  "dialog": "dialog:sq_ryujin_charter",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 2145, "y": 64, "z": 901 }
}
```

**Dialog (charter lectern)** — `dialog-src/dialog/sq_ryujin_charter.json`
```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "sq_ryujin_charter",
  "type": "STANDARD",
  "entries": [
    {
      "label": "default",
      "name": "The Sovereign Charter",
      "priority": 10,
      "default": true,
      "say": [
        "THE SOVEREIGN CHARTER OF RYUJIN KEEP. Raised in oath, not in coin. Signed by the founders of the line and by the dragons that consented to be kept. This keep answers to bearing and to no purchase price, in perpetuity, world without end.",
        "The bottom third of the signatures have been scraped and re-verified in a newer hand - the same fresh ink you saw on that ledger page - as though the keep could be re-signed by whoever holds the pen last. The oldest signatures underneath refuse to come off. Paper is testimony, and this paper is stubborn."
      ],
      "buttons": [
        {
          "label": "read_button",
          "text": "Read the charter closely",
          "gate": { "not_tag": "ryujin_charter_read" },
          "actions": [
            { "do": "command", "cmd": "tag @s add ryujin_charter_read", "as_player": true },
            { "do": "announce", "text": "Raised in oath, not in coin. The envoy is trying to buy a thing that was never for sale. Cite it back at him.", "as": "chat", "color": "gold" },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "Step back from the lectern", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**Dialog (envoy)** — `dialog-src/dialog/sq_ryujin_heritage.json`
```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "sq_ryujin_heritage",
  "type": "STANDARD",
  "entries": [
    {
      "label": "settled",
      "name": "Envoy - withdrawn",
      "priority": 30,
      "gate": { "tag": "ryujin_heritage_settled" },
      "say": [
        "The keep is not for acquisition. Filed. I have wired the lowlands that the northern portfolio is - unstable. They wired back that there is no more budget for portfolios. Heritage is all they still fund. There is a lesson in that and I am too senior to learn it."
      ],
      "buttons": [ { "label": "leave_button", "text": "Good day", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "confront",
      "name": "Envoy - the recognition",
      "priority": 20,
      "gate": { "tag": "ryujin_charter_read", "not_tag": "ryujin_heritage_settled" },
      "say": [
        "You again - and reading the charter, how thorough. Heritage Acquisitions is a preservation initiative, you understand. We buy history so that history stays - stays where we can - I am sorry, have we - your face is - it is on a page in my own briefcase with a bar over the - no. No, the founder retired. There was never a - who ARE you.",
        "Name your terms or name your Pokemon. I am authorized for either. I am authorized for so very much less than I was last month."
      ],
      "buttons": [
        {
          "label": "cite_button",
          "text": "Cite the charter back: raised in oath, not in coin",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/ryujin/heritage_cite", "as_player": true },
            { "do": "close" }
          ]
        },
        {
          "label": "battle_button",
          "text": "Face the envoy - his ace is level 72, above your cap",
          "actions": [ { "do": "battle" } ]
        },
        {
          "label": "decline_button",
          "text": "Pay him off to leave - 400 CD",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/ryujin/heritage_decline", "as_player": true },
            { "do": "close" }
          ]
        }
      ],
      "no_goodbye": true
    },
    {
      "label": "default",
      "name": "Heritage Acquisitions Envoy",
      "priority": 10,
      "default": true,
      "gate": { "defeated": "villain_boss" },
      "say": [
        "Heritage Acquisitions, at your service. The Company is preserving culturally significant instruments across the region - charters, founding documents, that sort of legacy paper. This keep has a lovely one. We would hate to see it lost to the elements, so we intend to hold it safe. Forever. In a vault. Where no one can misread it.",
        "Read the charter yourself if you like. Then perhaps you will see the wisdom in letting professionals hold history for you."
      ]
    }
  ]
}
```

- **DATAPACK NEEDS:**
  - `function/sidequest/ryujin/heritage_cite.mcfunction` — run as player (talk-past path):
    `tag @s add ryujin_heritage_settled` → `function cobblemon_initiative:economy/payout
    {amount:800}` (smaller than the battle prize — the peaceful route pays less) → `loot give @s
    loot cobblemon_initiative:npc_gift/training_standard` → `kill @e[type=easy_npc:humanoid,
    tag=ryujin_heritage_envoy_body,limit=1]` *(or leave the body in place and just close — see
    Open Question 2)* → `title @s title [{"text":"WITHDRAWN PENDING CLARIFICATION","color":"gold"}]`
    → `title @s subtitle [{"text":"There was no one left to clarify it.","color":"gray"}]`.
  - `function/sidequest/ryujin/heritage_decline.mcfunction` — run as player (pay-to-skip):
    charge via **pay-probe** (balance-checked `cobbledollars remove @s 400`, fail-soft if broke —
    do not proceed) → on success `tag @s add ryujin_heritage_settled` → `title @s actionbar
    [{"text":"The envoy pockets 400 CD and withdraws. ","color":"gold"},{"text":"Rounding, in the
    Company favor.","color":"gray"}]`. NOTE: charge via pay-probe (engine work — see HARD RULE 6).
  - The **battle** win path is self-contained via the `battle` block (`on_win` sets
    `ryujin_heritage_settled`; `despawn_on_win` removes the body).
- **QUEST_TARGETS entry:**
```json
{
  "holder": "q.side_heritage",
  "name": "Heritage Acquisitions",
  "slot": 45,
  "stages": [
    {
      "if_tags": ["ryujin_charter_read"],
      "not_tags": ["ryujin_heritage_settled"],
      "label": "Send the Heritage envoy packing",
      "target": { "npc": "ryujin_heritage_envoy" },
      "note": "Read the charter first (lectern), then confront the envoy: fight (ace 72, opt-in above-cap), cite-back (peaceful), or pay 400 CD to skip. All set ryujin_heritage_settled."
    },
    {
      "if_tags": ["defeated_villain_boss"],
      "not_tags": ["ryujin_charter_read", "ryujin_heritage_settled"],
      "label": "Read the Sovereign Charter at the lectern",
      "target": { "npc": "ryujin_charter_lectern" },
      "note": "Post-HQ opener (keyed off defeated_villain_boss so it always shows once the town wakes up, never a dead step). Reading the charter arms the cite-back button on the envoy; the stage then advances to the confront-the-envoy step above."
    }
  ]
}
```
- **REWARD/BALANCE:** **Battle:** 1200 CD prize, ace lv **72** (entry cap 68 — **ABOVE CAP,
  opt-in**, stake printed on the battle button, decline-able). **Cite-back:** 800 CD (peaceful,
  no battle). **Decline:** pay 400 CD (`decline_fee`) to skip — fail-soft if the player cannot
  afford it (pay-probe). Fairness floor: the envoy fight is dialog-opted (never a forced
  whiteout), so a player with no caught Pokémon simply cites or pays.

---

### SQ3 — The Broken Mail (GOLD idiom: fetch → count-check turn-in → opt-in wager)

**Concept.** Dragonsmith **Tetsu** works the keep forge. His dragon-scale mail came apart years
back and he will not climb the spire without it. Bring him **8× dragon scales** (dropped by the
keep's own dragons / the shrine's; Cobblemon drop UNVERIFIED — jar-validate the item id) and he
mends two suits, keeps one, and hands the player the other + CD. Then an **opt-in, decline-able
wager** against his two dragons, with the **stake printed** — the exact Genji `turn_in_rod` +
wager idiom. This is the town's *dragon-honor* quest: the smith measures you by whether you help
without being asked twice.

- **Forward hook:** Tetsu notes the mail will matter more in the cold north (Nifl) than here.
- **Back-echo:** the smith stopped taking Company coin when the lowlands money went strange, then
  went back to work when it steadied (CURRENCY STABILIZED echo).

**Character** — `dialog-src/characters/ryujin/ryujin_smith_tetsu.json`
```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "ryujin_smith_tetsu",
  "display_name": "Dragonsmith Tetsu",
  "role": "quest_giver",
  "act": "3",
  "location": "Ryujin Keep - Forge",
  "recognition_tier": "late",
  "trainer": "sq_ryujin_tetsu_wager",
  "recipe": "civilian",
  "dialog": "dialog:sq_ryujin_smith",
  "movement": { "objective": "ambient_stationary_look" },
  "battle": {
    "trainer": "sq_ryujin_tetsu_wager",
    "type": "wager",
    "format": "GEN_9_SINGLES",
    "prize": 700,
    "loss_fee": 700,
    "decline_fee": 200,
    "defeat_tag": "defeated_sq_ryujin_tetsu_wager",
    "win_line": "Ha. The forge respects a hot hand. Seven hundred, and no shame in the losing - a dragon that never loses never learns to bite.",
    "lose_line": "The stake stays on the anvil. Call it tuition. My old wyrms have thrown better climbers than you off this spire, and none of them held it against me either.",
    "already_beaten_line": "One wager a visit. The forge cannot afford my pride twice in a day.",
    "on_win": [ "execute as @1 run function cobblemon_initiative:economy/wager_sweetener" ]
  },
  "placement": { "x": 2148, "y": 64, "z": 897 }
}
```

**Dialog** — `dialog-src/dialog/sq_ryujin_smith.json`
```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "sq_ryujin_smith",
  "type": "STANDARD",
  "entries": [
    {
      "label": "smith_flavor",
      "name": "Tetsu - the forge respects you",
      "priority": 40,
      "gate": { "defeated": "sq_ryujin_tetsu_wager" },
      "say": [
        "Wear the scale-mail north, past Frostveil. The cold up in Nifl bites through anything a lowland smith will sell you - but a dragon sheds armor a dragon cannot freeze. It will keep you when the ice tries otherwise.",
        "I stopped taking Company coin the year the money went strange in the lowlands. Would not touch it. Word came up the spire it steadied again, so I fired the forge back up the same week. A smith needs the money to mean something before he will sweat for it."
      ],
      "buttons": [ { "label": "leave_button", "text": "The forge honors you", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "wager",
      "name": "Tetsu - a climbers wager",
      "priority": 30,
      "gate": { "tag": "ryujin_mail_done", "not_tag": "defeated_sq_ryujin_tetsu_wager" },
      "say": [
        "That mail sit right? Good. Then a climbers wager, smith to challenger: seven hundred CobbleDollars says my two old wyrms put yours on their backs. Win and it is yours; lose and the stake is mine; decline and we are still forge-friends. My dragons run hot - level up past your cap - so go in clear-eyed or do not go in.",
        "No hurry. The anvil is not going anywhere, and neither, at my age, am I."
      ],
      "buttons": [
        {
          "label": "wager_button",
          "text": "Take the wager - 700 CD on the line, his dragons run above your cap",
          "actions": [ { "do": "battle" } ]
        },
        {
          "label": "decline_button",
          "text": "Not today - tip the forge 200 CD and part friends",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/ryujin/tetsu_decline", "as_player": true },
            { "do": "close" }
          ]
        }
      ],
      "no_goodbye": true
    },
    {
      "label": "default",
      "name": "Dragonsmith Tetsu",
      "priority": 10,
      "default": true,
      "gate": { "defeated": "villain_boss", "not_tag": "ryujin_mail_done" },
      "say": [
        "Tetsu. I shoe dragons and mend the mail that lets fools ride them. My own scale-mail came apart on the spire years back and I have not trusted the height since. Bring me eight dragon scales - the wyrms shed them on the high ledges, and the shrine dragon sheds bigger - and I will mend two suits. One for me. One for whoever was decent enough to fetch them.",
        "A keep measures you by whether you help before you are asked twice. Consider yourself asked once."
      ],
      "buttons": [
        {
          "label": "turn_in_button",
          "text": "Hand over 8 dragon scales",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/ryujin/turn_in_scales", "as_player": true },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "I will go find some", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

- **DATAPACK NEEDS:**
  - `function/sidequest/ryujin/turn_in_scales.mcfunction` — run as player. GOLD count-check
    (clear-with-0 counts without removing; reuses the shared `ci_sq_scratch` objective):
    ```
    execute store result score @s ci_sq_scratch run clear @s cobblemon:dragon_scale 0
    execute if score @s ci_sq_scratch matches 8.. unless entity @s[tag=ryujin_mail_done] run function cobblemon_initiative:sidequest/ryujin/mail_success
    execute if score @s ci_sq_scratch matches ..7 run tellraw @s [{"text":"Tetsu counts the scales twice. ","color":"gray"},{"text":"Not eight yet. The wyrms shed them on the high ledges - bring eight.","color":"yellow"}]
    ```
    > JAR-VALIDATE `cobblemon:dragon_scale` (the held-item id) before ship — if the drop id
    > differs, swap it in both the clear-count line and `mail_success`.
  - `function/sidequest/ryujin/mail_success.mcfunction` — run as player: `clear @s
    cobblemon:dragon_scale 8` → `give @s cobblemon:dragon_scale 1` *(the mended keepsake — or a
    dedicated armor item if one exists; jar-validate)* → `function
    cobblemon_initiative:economy/payout {amount:500}` → `loot give @s loot
    cobblemon_initiative:npc_gift/training_standard` → `tag @s add ryujin_mail_done` → `title @s
    actionbar [{"text":"Scale-mail mended. ","color":"gold"},{"text":"A dragon sheds armor a
    dragon cannot freeze.","color":"gray"}]`.
  - `function/sidequest/ryujin/tetsu_decline.mcfunction` — run as player (wager decline): charge
    via **pay-probe** 200 CD (fail-soft if broke) → `title @s actionbar [{"text":"Tossed 200 CD in
    the forge tin. ","color":"gold"},{"text":"Part friends.","color":"gray"}]`. NOTE: charge via
    pay-probe.
- **QUEST_TARGETS entry:**
```json
{
  "holder": "q.side_mail",
  "name": "The Broken Mail",
  "slot": 43,
  "stages": [
    {
      "if_tags": ["ryujin_mail_done"],
      "not_tags": ["defeated_sq_ryujin_tetsu_wager"],
      "label": "Take Tetsu wager, or part friends",
      "target": { "npc": "ryujin_smith_tetsu" },
      "note": "Opt-in above-cap wager (700 CD, decline 200 CD). Fail-soft."
    },
    {
      "if_tags": ["defeated_villain_boss"],
      "not_tags": ["ryujin_mail_done"],
      "label": "Bring Tetsu 8 dragon scales",
      "target": { "npc": "ryujin_smith_tetsu" },
      "note": "Fetch: dragon scales shed on high ledges / the Dragon Shrine. Count-checked via turn_in_scales (clear-with-0), never a has_item gate."
    }
  ]
}
```
- **REWARD/BALANCE:** **Turn-in:** 500 CD + training loot + mended mail keepsake (no battle).
  **Wager:** 700 CD prize / 700 CD `loss_fee` (friendly stake, printed on the button), Tetsu's
  dragons run **above cap** — **OPT-IN**, decline-able for a 200 CD forge tip (`decline_fee`).
  Fail-soft on decline. Fairness floor: the wager is dialog-opted; a player with no caught Pokémon
  declines or walks.

---

### SQ4 — The First Oath (lore hub, back-echo, forward hook)

**Concept.** Skywatcher **Rei** keeps the west parapet and the oldest story of the keep. She is
the town's lore-keeper and its **forward-pull anchor**: the ancient oath that explains why the
keep never sold to the Company, the **back-echo** to CURRENCY STABILIZED (the storm finally
broke), and the **forward hook** naming Nifl / Frostveil Pass. She also carries a *frag_8-adjacent*
oblique line (the keep was raised by a builder, not a buyer) that circles the memory fragment
without closing the reveal. Small reward for hearing the full oath once (a lore-completion beat).

- **Forward hook:** names Nifl Town and R14 Frostveil Pass, and the cold that keeps records.
- **Back-echo:** the storm over the lowlands broke; the money steadied; the dragons felt it.
- **Frag-8 echo:** *the keep answers to the one who built it, not the one who bought it* — circles
  "You built it" without naming the founder.

**Character** — `dialog-src/characters/ryujin/ryujin_skywatcher_rei.json`
```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "ryujin_skywatcher_rei",
  "display_name": "Skywatcher Rei",
  "role": "elder",
  "act": "3",
  "location": "Ryujin Keep - West Parapet",
  "recognition_tier": "late",
  "recipe": "civilian",
  "dialog": "dialog:sq_ryujin_oath",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 2120, "y": 66, "z": 900 }
}
```

**Dialog** — `dialog-src/dialog/sq_ryujin_oath.json`
```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "sq_ryujin_oath",
  "type": "STANDARD",
  "entries": [
    {
      "label": "after_oath",
      "name": "Rei - the road north",
      "priority": 30,
      "gate": { "tag": "ryujin_oath_told" },
      "say": [
        "You have the oath now. Carry it north when you go - past Frostveil Pass, into Nifl, where the ice keeps its own oaths and forgets nothing that was ever written down. Cold country. It will remember you even if you cannot remember yourself yet.",
        "The storm over the lowlands broke the night the acting seat fell. The dragons stopped circling and settled on the parapet for the first time in a season. Money means little to a wyrm - but even they felt the region stop holding its breath."
      ],
      "buttons": [ { "label": "leave_button", "text": "I will carry it north", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "the_oath",
      "name": "The First Oath",
      "priority": -1,
      "say": [
        "The First Oath of the keep, then, since you asked and few do. This keep was not bought. It was RAISED - by hand, in oath, by the one who laid the first stone and swore the dragons to it. The Charter says it plain: the keep answers to the one who built it, never to the one who would buy it. That is why your Company never got through the gate. You cannot acquire a thing that was sworn, only a thing that was sold.",
        "Strange - I say built it and you flinch like the word has a splinter in it. Keep the splinter. Some words are supposed to catch."
      ],
      "buttons": [
        {
          "label": "take_oath_button",
          "text": "Thank her for the oath",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/ryujin/oath_done", "as_player": true },
            { "do": "open_dialog", "label": "after_oath" }
          ]
        },
        { "label": "leave_button", "text": "Sit with it a while", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Skywatcher Rei",
      "priority": 10,
      "default": true,
      "gate": { "defeated": "villain_boss" },
      "say": [
        "Rei. I watch the sky so the keep does not have to. Since the acting seat fell in the lowlands the air up here has been clean for the first time in years - as if something that was leaning on the whole region finally let go of it.",
        "You want to know why this keep never sold, when everything else did? There is an oath older than the money. Ask, and I will tell it. It is the only story up here worth the wind."
      ],
      "buttons": [
        {
          "label": "ask_oath_button",
          "text": "Tell me the oath",
          "actions": [ { "do": "open_dialog", "label": "the_oath" } ]
        },
        { "label": "leave_button", "text": "Another time", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

- **DATAPACK NEEDS:**
  - `function/sidequest/ryujin/oath_done.mcfunction` — run as player: `function
    cobblemon_initiative:economy/payout {amount:300}` → `give @s cobblemon:rare_candy 2`
    *(jar-validate id)* → `tag @s add ryujin_oath_told` → `title @s actionbar [{"text":"The First
    Oath. ","color":"gold"},{"text":"Raised, not bought.","color":"gray"}]`.
- **QUEST_TARGETS entry:**
```json
{
  "holder": "q.side_oath",
  "name": "The First Oath",
  "slot": 42,
  "stages": [
    {
      "if_tags": ["defeated_villain_boss"],
      "not_tags": ["ryujin_oath_told"],
      "label": "Hear the First Oath from Skywatcher Rei",
      "target": { "npc": "ryujin_skywatcher_rei" },
      "note": "Lore hub + back-echo (CURRENCY STABILIZED) + forward hook (Nifl / Frostveil Pass). Circles frag_8 (built, not bought) without closing the reveal."
    }
  ]
}
```
- **REWARD/BALANCE:** 300 CD + 2× rare candy (lore-completion). No battle; no decline cost (a
  free story). Not above cap.

---

## 4. Recognition & economy beats

**Recognition (band = late, 7+ badges; gated `defeated_villain_boss` for the post-HQ tail).**
Ryujin Keep is the **hottest** recognition town in the run — the erasure never reached this keep,
so old-timers see the founder's *bearing* and the **stand-down** beat lands here:

- **Stand-down (SQ1 clerk):** *"I know that face. It came down off the wall three years ago and I
  helped carry the frame... I am not going to fight you. I would sooner file my own resignation
  with my teeth."* — canon "some stand down rather than raise a hand against the founder."
- **Alarm → panic (SQ2 envoy):** *"your face is on a page in my own briefcase with a bar over the
  — no. The founder retired. There was never a — who ARE you."* — management tier: he placed the
  portrait.
- **Leader (owned by gym unit):** `dialog:gym_leader_ryujin` already carries the late line
  (*"I know the bearing of someone who once gave the orders... you wear it like you forgot you put
  it on"*). Roadmap §4 proposes adding a `defeated_villain_boss`-gated post-HQ variant — noted for
  the gym unit, not this doc.
- **CIVILIANS NEVER recognise the founder:** the charter lectern (a prop) is zero-recognition;
  Skywatcher Rei is an *elder* who reacts to the **word "built,"** not to the CEO — she circles
  frag_8 obliquely and never places him as the founder. Correct per canon.

**Economy voice (`cd_instability` holds 25 — post-HQ corrupted-propaganda register).** The money
STABILIZED but the propaganda is now CORRUPTED and the cover-up is leaking:

- The **ledger page** (SQ1) and the **charter** (SQ2 lectern) are the corrupted beat made into
  props: *the founder retired → correction: there was never a founder*, both stamps official, ink
  still glittering where they scratched the first truth to paste the second — direct dramatisation
  of `register:scrubbing#retired_to_never` (`defeated_villain_boss` tier) and
  `register:economy#corrupted`.
- **Back-echo to CURRENCY STABILIZED:** Hana (*"the lowlands steadied, the money holds again... we
  felt the air change"*), Tetsu (*"stopped taking Company coin the year the money went strange...
  it steadied, so I fired the forge back up"*), and Rei (*"the storm broke the night the acting
  seat fell... the region stopped holding its breath"*). All reference the HQ→DJ beat without
  re-explaining it.
- **Payouts** run at **full face value** (idx 25 → `economy/payout` skew ≈ 0). No wheat-trader
  ambush is authored for this town (Ryujin Keep is the anti-Company keep; the Granary/wheat
  recognition beat lives on the farm/trader units, not here).
- **Streamable receipts:** SQ1 *FILED* actionbar, SQ2 *WITHDRAWN PENDING CLARIFICATION* title +
  *ADJUSTMENT / rounding in the Company favor* on the decline toll, SQ3 *SCALE-MAIL MENDED*, SQ4
  *THE FIRST OATH — raised, not bought*.

---

## 5. New tags/scores introduced

| tag | set by | gated / read by |
|---|---|---|
| `ryujin_defector_met` | SQ1 clerk `default` reassure button | SQ1 `offer_page` entry; quest_targets `q.side_clerk8` |
| `ryujin_ledger_taken` | SQ1 `take_ledger.mcfunction` | SQ1 `after` entry; Hana `point_clerk`; quest_targets not_tag |
| `ryujin_charter_read` | SQ2 lectern `read_button` | SQ2 envoy `confront` entry; quest_targets `q.side_heritage` |
| `ryujin_heritage_settled` | SQ2 battle `on_win` / `heritage_cite` / `heritage_decline` | SQ2 `settled` entry; Hana `point_envoy`; quest_targets not_tag |
| `defeated_ryujin_heritage_envoy` | SQ2 battle `defeat_tag` | (defeat record; battle already-beaten line) |
| `ryujin_mail_done` | SQ3 `mail_success.mcfunction` | SQ3 `wager`/`smith_flavor` entries; Hana `point_smith`; quest_targets |
| `defeated_sq_ryujin_tetsu_wager` | SQ3 wager `defeat_tag` | SQ3 `smith_flavor` entry; quest_targets not_tag |
| `ryujin_oath_told` | SQ4 `oath_done.mcfunction` | SQ4 `after_oath` entry; Hana `point_oath`; quest_targets not_tag |

**Scoreboards:** none new. SQ3 reuses the shared `ci_sq_scratch` objective (created by
`museum/load`) for the clear-with-0 count-check. All numeric-band tags already exist upstream.

**Reused canon tags (read-only here):** `defeated_villain_boss` (post-HQ gate on the whole town
tone), `defeated_ryujin_leader` / `defeated_ryujin_apprentice` (Hana forward hook), `dragon_slain`
(rift — gym unit, not read here).

---

## 6. Build checklist

Ordered. All new files under `dialog-src/characters/ryujin/`, `dialog-src/dialog/`,
`data/cobblemon_initiative/function/sidequest/ryujin/`, plus register + team edits.

1. **Create dir** `dialog-src/characters/ryujin/`.
2. **Drop 6 character files:** `ryujin_keeper_hana.json`, `ryujin_records_officer.json`,
   `ryujin_heritage_envoy.json`, `ryujin_charter_lectern.json`, `ryujin_smith_tetsu.json`,
   `ryujin_skywatcher_rei.json` (blocks in §3). Confirm/adjust the PROPOSED ground-hub coords
   against the real tower-base rooms.
3. **Drop 6 dialog files:** `ryujin_rumor_hub.json`, `sq_ryujin_clerk.json`,
   `sq_ryujin_charter.json`, `sq_ryujin_heritage.json`, `sq_ryujin_smith.json`,
   `sq_ryujin_oath.json` (blocks in §3).
4. **Add 1 team file:** `data/rctmod/trainers/ryujin_heritage_envoy.json` — villain-management
   tier, GEN_9_SINGLES, ace **lv 72** (corporate-menace flavour, e.g. Krookodile / Bisharp /
   Kingambit). Register the RCT id + skin (`trainer_textures` grunt/management skin per the
   Easy-NPC skin note). Add its mob-gate file under `data/rctmod/mobs/trainers/single/` if a
   spawn gate is wanted (not required for a latched dialog-battle NPC).
5. **Add 6 datapack functions** under `function/sidequest/ryujin/`: `take_ledger`,
   `heritage_cite`, `heritage_decline`, `turn_in_scales`, `mail_success`, `tetsu_decline`,
   `oath_done` (specs in §3). **JAR-VALIDATE** `cobblemon:dragon_scale` and `cobblemon:rare_candy`
   ids in the pinned 1.7.3 jar before shipping; swap if the drop id differs.
6. **Add 4 quest_targets stages** to `dialog-src/registers/quest_targets.json`: `q.side_clerk8`,
   `q.side_heritage`, `q.side_mail`, `q.side_oath` (blocks in §3). Pick free `slot` values (42–45
   suggested — verify against the render tie-order note in the register; adjust if they collide).
7. **Compile:** run `scripts/content_compile` (lowers `dialog-src/**` → `.npc.snbt` +
   `quest_waypoints.json`), then `scripts/update_preset_index`, then `scripts/generate_npc_function`.
8. **Rebuild:** `gradle build`. In-world: `cobblemon-initiative install run` repaints/latches the
   new NPCs (they spawn within ~40b of their placement); press `]` to track the new quest lines.

---

## 7. Open questions for showrunner

1. **Dragon-scale fetch item.** SQ3 assumes `cobblemon:dragon_scale` is a real, obtainable held
   item that the keep/shrine dragons drop in 1.7.3. If the id differs (or it is not a wild drop),
   swap the fetch item — candidates: `minecraft:phantom_membrane` (dragon-adjacent), a Cobblemon
   evolution stone, or a small count of `cobblemon:dragon_fang`. Confirm the id before authoring
   `turn_in_scales` / `mail_success`.
2. **Envoy body on the peaceful routes.** The **battle** win despawns the envoy
   (`despawn_on_win`). The **cite-back** and **decline** routes currently leave `heritage_cite` /
   `heritage_decline` free to either despawn the body (a `kill @e[...tag=..._body]` line — needs a
   spawn-time body tag) or leave him standing to deliver the `settled` line. Ruling: **despawn on
   all three** (clean stream beat) or **leave him standing, humiliated** (extra flavor)?
3. **Mended-mail reward item.** SQ3 hands back a keepsake — is there a dedicated dragon-scale-mail
   armor/cosmetic item to give, or should it stay a flavor keepsake (a single dragon scale / a
   named paper)? Same question for the SQ1 ledger page keepsake.
4. **Ground-hub coords.** All six placement anchors are PROPOSED (roadmap §3 flags the tower-base
   ground cluster as not-yet-in-any-file). Need builder confirmation of real ledge/room coords, or
   convert to `uuid` builder-bodies if these NPCs should be baked into the save.
5. **Wager team for Tetsu.** SQ3 needs a `sq_ryujin_tetsu_wager` RCT trainer (two dragons, run
   **above cap** by design — e.g. Kingdra 71 / Flygon 71, ace ≤ 72 to sit at/under the envoy).
   Confirm the team and register it under `trainers/side_quests/act2.json` (new file — Act-2/3
   side-quest registry does not exist yet per roadmap §6).
6. **Slot numbers.** SQ slots 42–45 are provisional; verify they do not collide with existing
   render ties in `quest_targets.json` (the register notes some slots are intentionally shared).
