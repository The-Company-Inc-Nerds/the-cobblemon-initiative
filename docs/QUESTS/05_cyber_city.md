# 05 — Cyber City (Gym 7, Electric) — Town Quest Plan

> **Slug:** `05_cyber_city` · **Area key:** `cyber_city` · **Act:** 1 → 2 pivot
> **Companion:** `docs/roadmap/08_cyber_city.md` (gym ladder / STORMFRONT event / marquee context)
> and `docs/QUESTS/10_hq_raid.md` (the forward hook this town builds toward — the KEYCARD hand-off,
> lobby pointer, and DJ gate are OWNED there; this doc plants them and stops at the door).
>
> This file is a **plan**, implementation-ready. It authors the **town skin around the shipped gym**:
> a rumor hub, four side quests (surveillance / data / whistleblower / scrubbing-artifact flavor),
> the frag_7 charter beat's town-side echo, the peak-recognition beats, and the quest that literally
> points the player at the HQ gate. It does **not** re-author the gym ladder, the leader, the KEYCARD
> marquee, or STORMFRONT (Zapdos) — those are covered in `08_cyber_city.md` and `10_hq_raid.md`.

---

## 1. Overview

**The town.** Cyber City is the Company's neon capital and the **home turf** — the HQ skyscraper
`[1590 51 1028]` sits on the north plaza, one zone-seam away. This is the town where the mask slips:
propaganda boards downtown are **glitching (`§k`)**, the exchange rate is **openly broken**, and grey
suits are visibly *packing the tower down* rather than trading. The **scrubbing is at its most
visible** anywhere in the run — brighter rectangles all over downtown where portraits hung, re-verified
ledgers under new signatures, revised org charts.

**Band context (the hard turn):**

| Axis | Value | Source |
|------|-------|--------|
| Level cap | Entry **62** (Kalahar unlocked it; Cyber's own unlock is 62). **All side battles here are tuned to the cap-62 window.** | CLAUDE.md ladder |
| `cd_instability` | **→ 56 — SERIES PEAK** (Volt's `gym_destabilize`, +8). Money has never felt this fake. | `08_cyber_city.md` §2, `economy/instability` register |
| Memory fragment | **frag_7 — You signed this charter.** (fires on Volt's defeat) | `registers/memory_fragments.json#fragment_7` |
| Recognition tier | Entering = **mid** (6 badges); the instant Volt falls = **late** (7 badges = `badges_gte_7`). Every post-Volt quest here is a **late-recognition** scene by construction. This is the Company home turf and recognition is at its **pre-HQ PEAK**. | schema §6, `08_cyber_city.md` §2 |
| Economy voice | **Act-2 slipping → corrupting.** `economy#reassurance` (nervous over-explaining), `economy#instability` tier `gte 56` (vaults echoing, folk paying in grain). | `registers/economy.json` |

**The arc job.** Cyber City **SETS UP THE HQ RAID.** Three jobs, all done in-town:
1. **Establish the gate** — the player learns the raid needs **badges ≥ 7 AND `fields_liberated ≥ 4`**
   (of 10). DJ's off-screen refusal — *the monopoly holds* — is echoed town-side. **The gate number is
   OWNED by `10_hq_raid.md` (canon = 4); this doc mirrors it and must not re-litigate it.**
2. **The push to liberate the last fields** — a quest literally points the player south to **Fenceline
   Acres (`farm_6`, `[1565 65 1732]`)** and east to **Coldfurrow (`farm_7`, `[1925 64 963]`)**, the
   nearest un-liberated feeders.
3. **Point at the HQ door** — the "Door Downtown" quest ends with an `announce` pointer to the tower
   lobby `[1590 51 1028]` and hands the mainline off to the KEYCARD marquee (owned by `10_hq_raid.md`).

**Place on the route.** Seventh and final Act-1 gym town. Prior beat: Kalahar Reach (Ground, gym 6,
`defeated_kalahar_leader` gates the Cyber ladder). Next beat: the **HQ raid** (`10_hq_raid.md`), then
Ryujin Keep (gym 8, post-HQ frag_8 assumes DJ has fallen). Nearby: **Fenceline Acres `farm_6`**
`[1565 65 1732]`, **Coldfurrow `farm_7`** `[1925 64 963]`, **Safari Zone** `[1396 1593]`, **R12 Pylon Path**.

---

## 2. Cast

All NPCs below are **self-spawn latches**: `placement:{x,y,z}` + a skin, **no `uuid`** → they spawn
once per world via the generated proximity function. Coords are drawn from `08_cyber_city.md` §3
(PROPOSED, PIP-verified in-polygon) or marked `PLACEHOLDER` where that doc did not fix one. None of
these duplicate the shipped gym cast (`cyber_leader`, `cyber_guide`, `cyber_trainer_1..4`,
`cyber_jr_apprentice`, `cyber_apprentice`) or the roadmap's marquee/STORMFRONT NPCs
(`cyber_access_admin`, `cyber_grid_warden`, `sq_cyber_storm_*`).

| id | display_name | role | one-line concept | placement anchor |
|----|--------------|------|------------------|------------------|
| `cyber_nurse_rumor` | Nurse Ampere | healer | **RUMOR HUB** — the Pokemon Center nurse; heals, and points at every open town quest (gated on each quest not-done tag). Civilian: never recognizes the founder, only feels the propaganda decay. | Pokemon Center, `~[1478, 65, 1150]` (PLACEHOLDER — near gym) |
| `cyber_exchange_teller` | Verified Value Teller | civilian | **DATA quest** — a CobbleDollar money-changer sweating the peak index; begs you to re-verify 3 nether-star reserve tags the Company stopped verifying. | Exchange kiosk `~[1500, 65, 1120]` |
| `cyber_signal_tech` | Signal Tech Rell | civilian | **SURVEILLANCE quest** — municipal comms tech; three downtown propaganda billboards are glitching (`§k`) and one is broadcasting a *scrubbing* memo in the clear. Scrub the three. | Comms van, plaza `~[1470, 65, 1140]` |
| `cyber_defector_maren` | Off-Records Clerk Maren | quest_giver | **WHISTLEBLOWER quest** — a burned-out access-control clerk (NOT the keycard defector; a *different*, earlier defector) who wants the scrubbed personnel file recovered from three archive drops before the Company incinerates it. **She recognizes the FACE (late), never the name.** | Records annex doorstep `~[1520, 66, 1100]` (PLACEHOLDER) |
| `cyber_grid_broker` | Grid Broker Ohmond | quest_giver | **HQ-POINTER + optional wager** — a downtown fixer who read the field ledgers; tells you the raid gate math out loud (badges 7, fields 4), points you at Fenceline/Coldfurrow, and offers an **opt-in above-cap wager** on his hoarded electric team (decline-able, stake printed). | Downtown fixer stall `~[1555, 65, 1108]` (PLACEHOLDER — near HQ seam) |
| `cyber_reserve_1/2/3` | Reserve Tag (prop) | civilian | **DATA targets** — three re-signed nether-star reserve placards (disguised-prop `station_moss` pattern). Interact to re-verify. | scattered downtown, `~[1490,64,1128]` / `~[1560,64,1092]` / `~[1610,64,1112]` (PLACEHOLDER) |
| `cyber_board_1/2/3` | Glitching Billboard (prop) | civilian | **SURVEILLANCE targets** — three glitching propaganda boards (disguised-prop pattern). Interact to scrub. | `~[1490,66,1132]` / `~[1560,66,1090]` / `~[1610,66,1110]` (PLACEHOLDER) |
| `cyber_archive_1/2/3` | Archive Drop (prop) | civilian | **WHISTLEBLOWER targets** — three archive boxes flagged for quality review (disguised-prop pattern). Interact to recover a file page. | records annex, `~[1518,65,1096]` / `~[1524,65,1104]` / `~[1512,65,1092]` (PLACEHOLDER) |

> **Note on `cyber_access_admin` (the KEYCARD defector).** She is the marquee, **owned by
> `10_hq_raid.md`** — do NOT author her here. Maren (`cyber_defector_maren`) is a *distinct, earlier*
> defector so the whistleblower quest does not step on the keycard beat. If the showrunner wants only
> one defector, fold Maren into a pre-Volt phase of the access-admin (Open Question 3).

---

## 3. Quests

Four quests. Two are **available pre-Volt** (mid-recognition, they read as the town's dread) and gate
their late-recognition *escalation* on `defeated_cyber_leader`; two are **post-Volt only** (the
whistleblower and the HQ-pointer, which are the hard turn made physical). Every quest is fail-soft,
opt-in where it costs, and plants the HQ forward hook. All fetch/count logic uses the **Genji
count-check idiom** (a datapack function that counts prop-tags, never a `has_item` gate).

The **rumor hub** (Quest 0) is not a quest of its own — it is the nurse that surfaces the other four.

> **Authoring note — `say[]` is a random rotation, not a script.** Per schema HARD RULE 2, each line in
> a `say[]` array renders as **one random alternative** on open. Every multi-line `say[]` below is
> authored so each line **stands alone** (same beat, different phrasing) — never a two-part sequence.
> Where a scene needs a genuine second beat, it is delivered on a **button** or an `{do:"announce"}`,
> not a second `say[]` line.

---

### Quest 0 — Rumor Hub: Nurse Ampere (the Pokemon Center)

**Concept.** House-style rumor hub. The Center nurse heals your team and, in a `CUSTOM` menu, gives a
one-line pointer to each open town quest (gated on that quest's not-done tag) plus a **back-echo** and
the **forward hook**. She is a civilian — she never recognizes the founder; she only feels the money
breaking and the boards glitching.

**Forward hook planted:** *The grey suits are packing the tower down on the north plaza, not trading.
Something is ending over there.* (points at HQ). **Back-echo:** *You liberated a field on the way in —
the exchange board twitched down half a point when you did. First good number I have seen in a season.*
(references `fields_liberated` / `wheat_war_active`).

**Character JSON** — `dialog-src/characters/cyber/cyber_nurse_rumor.json`:

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "cyber_nurse_rumor",
  "display_name": "Nurse Ampere",
  "role": "healer",
  "act": "2",
  "location": "Cyber City",
  "recognition_tier": "late",
  "recipe": "healer",
  "dialog": "dialog:cyber_rumor_hub",
  "movement": { "objective": "ambient_stationary_look" },
  "service": { "kind": "heal" },
  "placement": { "x": 1478, "y": 65, "z": 1150 }
}
```

**Dialog JSON** — `dialog-src/dialog/cyber_rumor_hub.json` (CUSTOM: heal + gated rumor buttons). Each
`say[]` line is a standalone alternative; the pointer lines are `{do:"announce"}` (plain text, no raw
JSON, no `"`):

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "cyber_rumor_hub",
  "type": "CUSTOM",
  "entries": [
    {
      "label": "default",
      "name": "Nurse Ampere",
      "priority": 10,
      "default": true,
      "say": [
        "Welcome to Cyber City Center. I will patch your team up - the road in is rough and the town is rougher lately.",
        "Half my regulars come in just to sit where the lights do not flicker. Nobody at the Company will say why the boards outside keep glitching."
      ],
      "buttons": [
        { "label": "heal_button", "text": "Rest my team", "actions": [ { "do": "heal" }, { "do": "close" } ] },
        { "label": "rumor_button", "text": "What is the word downtown?", "actions": [ { "do": "open_dialog", "label": "rumors" } ] }
      ]
    },
    {
      "label": "rumors",
      "name": "Nurse Ampere - the word downtown",
      "priority": -1,
      "say": [
        "The grey suits are packing the tower down on the north plaza, not trading. Something is ending over there, and it is not the workday.",
        "First good number I have seen in a season came in with you - the exchange board twitched down half a point. Somebody has been liberating fields. Keep it up and maybe the coin stops lying."
      ],
      "buttons": [
        { "label": "tip_exchange", "text": "The exchange board is nonsense", "gate": { "not_tag": "ci_reserves_done" }, "actions": [ { "do": "announce", "as": "chat", "text": "Nurse Ampere: The teller at the kiosk is beside himself. He needs somebody to re-verify the reserve tags the Company stopped checking. Three of them, posted downtown.", "color": "gray" }, { "do": "close" } ] },
        { "label": "tip_signal", "text": "The billboards are glitching", "gate": { "not_tag": "ci_signal_done" }, "actions": [ { "do": "announce", "as": "chat", "text": "Nurse Ampere: Signal Tech Rell is chasing the fault. Three boards are broadcasting scrubbed memos in the clear. He wants them scrubbed before management notices they are readable.", "color": "gray" }, { "do": "close" } ] },
        { "label": "tip_records", "text": "Who is the clerk on the annex step?", "gate": { "tag": "defeated_cyber_leader", "not_tag": "ci_file_done" }, "actions": [ { "do": "announce", "as": "chat", "text": "Nurse Ampere: Maren. She quit access control the day they boxed the archives. She has been asking after you by a description, not a name. Best you hear it from her.", "color": "gray" }, { "do": "close" } ] },
        { "label": "tip_hq", "text": "The tower on the north plaza", "gate": { "tag": "defeated_cyber_leader", "not_tag": "hq_pointer_done" }, "actions": [ { "do": "announce", "as": "chat", "text": "Nurse Ampere: Ohmond works the stall by the seam and reads the field ledgers for sport. If you mean to walk into that tower, he can tell you the number that opens the door.", "color": "gray" }, { "do": "close" } ] },
        { "label": "leave_button", "text": "Thanks", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

- **DATAPACK NEEDS:** none. All pointer lines are `{do:"announce","as":"chat"}` — the compiler builds
  the quoted `tellraw`, so no raw JSON and no `"` are ever authored (schema §3.5, Open Q7).
- **QUEST_TARGETS entry:** none (rumor hub does not own a sidebar line).
- **REWARD/BALANCE:** heal only. No combat. Always available.

---

### Quest 1 — "Exchange Rate" (DATA / scrubbing-artifact) — the currency peak, made a quest

**Concept.** The exchange board reads 56 and the teller is unravelling. He begs you to **re-verify
three nether-star reserve tags** posted downtown — the one job the Company is supposed to do and has
stopped doing. Each tag is **re-signed under a new name over a sanded-off older signature** (the
scrubbing artifact, LORE_BIBLE §9). Count via a props-counter (the `price_check` idiom), turn in at the
kiosk.

**Forward hook:** on turn-in the teller mutters that *someone is offering an alternative south of here*
— a soft pointer toward field liberation / Fenceline. **Back-echo:** references the Hua Zhan price-check
beat — *You checked the tickets in wheat country. This is the same rot, three towns further gone.*

**Character JSON** — `dialog-src/characters/cyber/cyber_exchange_teller.json`:

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "cyber_exchange_teller",
  "display_name": "Verified Value Teller",
  "role": "civilian",
  "act": "2",
  "location": "Cyber City - Exchange Kiosk",
  "recognition_tier": "late",
  "recipe": "civilian",
  "dialog": "dialog:cyber_exchange_rate",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 1500, "y": 65, "z": 1120 }
}
```

**Reserve-tag prop** — `dialog-src/characters/cyber/cyber_reserve_1.json` (×3, ids `_1/_2/_3`,
placement per §2; `_2`/`_3` identical but for id/placement and the note number in the button cmd):

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "cyber_reserve_1",
  "display_name": "Reserve Tag",
  "role": "civilian",
  "act": "2",
  "location": "Cyber City - Downtown",
  "recipe": "civilian",
  "dialog_inline": {
    "kind": "dialog",
    "id": "cyber_reserve_1_inline",
    "type": "BASIC",
    "entries": [
      {
        "label": "default",
        "name": "Reserve Tag",
        "priority": 10,
        "default": true,
        "say": [
          "A nether-star reserve placard. The current signature is fresh ink over a sanded-off older one. The Company says it was always this one signature.",
          "A reserve placard for the stars that back the coin. Somebody sanded a name off and re-signed it. The stamp is new. The paper is old."
        ],
        "buttons": [
          { "label": "verify_button", "text": "Re-verify this tag", "actions": [ { "do": "command", "cmd": "function cobblemon_initiative:sidequest/exchange/note_1", "as_player": true }, { "do": "close" } ] },
          { "label": "leave_button", "text": "Leave it", "actions": [ { "do": "close" } ] }
        ]
      }
    ]
  },
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 1490, "y": 64, "z": 1128 }
}
```

**Dialog JSON** — `dialog-src/dialog/cyber_exchange_rate.json` (STANDARD; paid_out > accepted > default).
Each `say[]` line is a standalone alternative:

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "cyber_exchange_rate",
  "type": "STANDARD",
  "entries": [
    {
      "label": "paid_out",
      "name": "Teller - the numbers do not add",
      "priority": 40,
      "gate": { "tag": "ci_reserves_done" },
      "say": [
        "Three tags, re-verified. Every one re-signed over somebody older. That is not accounting, that is a cover story with a stamp on it.",
        "Someone is offering an alternative south of here. Grain, they say. After what those tags showed me, maybe I should listen."
      ],
      "buttons": [ { "label": "leave_button", "text": "Take care of yourself", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "accepted",
      "name": "Teller - re-verify the tags",
      "priority": 30,
      "gate": { "tag": "ci_reserves_active", "not_tag": "ci_reserves_done" },
      "say": [
        "Three reserve tags, posted downtown. Re-verify each one and bring me the count. If the star reserves are short, at least we will know it and not just feel it.",
        "You checked the tickets in wheat country, did you not. This is the same rot, three towns further gone. Bring me the count when you have all three."
      ],
      "buttons": [
        { "label": "turnin_button", "text": "Report the count", "actions": [ { "do": "command", "cmd": "function cobblemon_initiative:sidequest/exchange/turn_in", "as_player": true }, { "do": "close" } ] },
        { "label": "leave_button", "text": "Still counting", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Verified Value Teller",
      "priority": 10,
      "default": true,
      "say": [
        "Verified Trust, Verified Value - that is the slogan on the wall behind me, and I cannot look at it. The board reads fifty-six. That is not a rate, that is a scream.",
        "The Company is supposed to verify the nether-star reserves that back every coin. They stopped. Would you walk downtown and re-verify three reserve tags for me. I need to know how bad it is."
      ],
      "buttons": [
        { "label": "accept_button", "text": "I will re-verify them", "actions": [ { "do": "command", "cmd": "function cobblemon_initiative:sidequest/exchange/start", "as_player": true }, { "do": "close" } ] },
        { "label": "leave_button", "text": "Not now", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

- **DATAPACK NEEDS** (all under `function/sidequest/exchange/`, mirror `sidequest/price_check/`):
  - `start.mcfunction` — set `ci_reserves_active` tag; title sting *THE EXCHANGE RATE*; `quest/refresh`.
  - `note_1/2/3.mcfunction` — idempotent per-prop: `execute unless entity @s[tag=ci_reserve_1] run` an
    actionbar count `(n/3)` (no `"` — deliver via the announce/actionbar rail, not a raw tellraw);
    `tag @s add ci_reserve_1`; `quest/refresh`.
  - `turn_in.mcfunction` — the **count-check** (Genji idiom, prop-tag flavor): only pays if all three
    prop tags present: `execute if entity @s[tag=ci_reserve_1,tag=ci_reserve_2,tag=ci_reserve_3] unless entity @s[tag=ci_reserves_done] run function .../reward`; else an actionbar *not three yet*.
  - `reward.mcfunction` — `function cobblemon_initiative:economy/payout_company {amount:900}` (branded
    rate line — an attribution receipt); `loot give @s loot cobblemon_initiative:npc_gift/training_standard`;
    `tag @s add ci_reserves_done`; title *ADJUSTMENT: rounding, in the Company favor*; `quest/refresh`.
  - **Band tags:** none new — the props use plain tags. (No numeric dialog gate here.)
- **QUEST_TARGETS entry** (append to `registers/quest_targets.json`; slot 55 — verified free):

```json
{
  "holder": "q.side_exchange",
  "name": "Exchange Rate",
  "slot": 55,
  "stages": [
    {
      "if_tags": [ "ci_reserves_active" ],
      "not_tags": [ "ci_reserves_done" ],
      "label": "Re-verify 3 reserve tags downtown",
      "target": { "x": 1500, "y": 64, "z": 1120 },
      "note": "Three reserve-tag props scattered downtown (cyber_reserve_1/2/3); the kiosk centroid is the area anchor, y nominal. Point at {npc: cyber_exchange_teller} once placed if a single waypoint is preferred over the area."
    }
  ]
}
```

- **REWARD/BALANCE:** 900 CD (skewed via `payout_company` so the shortfall reads on camera). No combat,
  no decline cost (a civilian favor). Available **pre-Volt** (mid); the `paid_out` line names the sanded
  charter signature obliquely — a late-recognition-adjacent beat that does not require the founder to be
  present and never names him.

---

### Quest 2 — "Signal Integrity" (SURVEILLANCE) — scrub the glitching boards

**Concept.** Three downtown propaganda billboards are glitching (`§k`) and **broadcasting scrubbed
memos in the clear** — one is showing the *retired then never existed* propaganda-decay text
mid-corruption. Signal Tech Rell (municipal, not Company) wants them scrubbed before management notices
they are readable. The surveillance theme flipped: the Company's own surveillance apparatus is leaking
the cover-up.

**Forward hook:** Rell's payoff line — *The clean feed all routes through the north tower. If you want
the boards to stop lying, that is where the signal comes from.* (points at HQ). **Back-echo:** *Same
fault they had in Kalahar, the techs say. It started spreading the week the coin went soft.* (references
Kalahar / `cd_instability`).

**Character JSON** — `dialog-src/characters/cyber/cyber_signal_tech.json`:

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "cyber_signal_tech",
  "display_name": "Signal Tech Rell",
  "role": "civilian",
  "act": "2",
  "location": "Cyber City - Comms Van",
  "recognition_tier": "late",
  "recipe": "civilian",
  "dialog": "dialog:cyber_signal_integrity",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 1470, "y": 65, "z": 1140 }
}
```

**Billboard prop** — `dialog-src/characters/cyber/cyber_board_1.json` (×3, ids `_1/_2/_3`; the say lines
use the `economy#corrupted` glitch register so all three read as the decaying propaganda). Each line is
a standalone alternative:

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "cyber_board_1",
  "display_name": "Glitching Billboard",
  "role": "civilian",
  "act": "2",
  "location": "Cyber City - Downtown",
  "recipe": "civilian",
  "dialog_inline": {
    "kind": "dialog",
    "id": "cyber_board_1_inline",
    "type": "BASIC",
    "entries": [
      {
        "label": "default",
        "name": "Glitching Billboard",
        "priority": 10,
        "default": true,
        "say": [
          "The board stutters between slogans. Verified tru§kvalue verified§ktrust we told them the founder retired.",
          "The feed corrupts mid-loop. The founder retired§kthere was never a founder§kverified trust verified value."
        ],
        "buttons": [
          { "label": "scrub_button", "text": "Scrub the feed", "actions": [ { "do": "command", "cmd": "function cobblemon_initiative:sidequest/signal/scrub_1", "as_player": true }, { "do": "close" } ] },
          { "label": "leave_button", "text": "Watch it glitch", "actions": [ { "do": "close" } ] }
        ]
      }
    ]
  },
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 1490, "y": 66, "z": 1132 }
}
```

**Dialog JSON** — `dialog-src/dialog/cyber_signal_integrity.json` (STANDARD; done > accepted > default).
Each `say[]` line stands alone:

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "cyber_signal_integrity",
  "type": "STANDARD",
  "entries": [
    {
      "label": "done",
      "name": "Rell - the feed is clean",
      "priority": 40,
      "gate": { "tag": "ci_signal_done" },
      "say": [
        "Three boards, scrubbed. Every one of them was leaking the same story - the founder retired, then the founder never existed, both memos on the same loop. That is somebody editing the truth in public and getting sloppy.",
        "The clean feed all routes through the north tower. If you want the boards to stop lying, that is where the signal comes from."
      ],
      "buttons": [ { "label": "leave_button", "text": "Understood", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "accepted",
      "name": "Rell - scrub the boards",
      "priority": 30,
      "gate": { "tag": "ci_signal_active", "not_tag": "ci_signal_done" },
      "say": [
        "Three boards, downtown. Get to each one and scrub the corrupted feed before a suit notices it is readable. I will keep the comms van between you and the cameras.",
        "Same fault they had in Kalahar, the techs say. It started spreading the week the coin went soft. Scrub all three and come back."
      ],
      "buttons": [
        { "label": "turnin_button", "text": "Report the boards scrubbed", "actions": [ { "do": "command", "cmd": "function cobblemon_initiative:sidequest/signal/turn_in", "as_player": true }, { "do": "close" } ] },
        { "label": "leave_button", "text": "Still working", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Signal Tech Rell",
      "priority": 10,
      "default": true,
      "say": [
        "Rell, city signals. I keep the boards lit. Lately they are lit with things I was never given to broadcast - a memo, plain as day, about a man who was retired and then unretired into never existing.",
        "Management thinks nobody reads a glitch. Somebody should scrub the three worst boards before that stops being true. No cameras on you if you move quick."
      ],
      "buttons": [
        { "label": "accept_button", "text": "I will scrub them", "actions": [ { "do": "command", "cmd": "function cobblemon_initiative:sidequest/signal/start", "as_player": true }, { "do": "close" } ] },
        { "label": "leave_button", "text": "Not my problem", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

- **DATAPACK NEEDS** (`function/sidequest/signal/`, mirror `exchange/` above):
  - `start.mcfunction` — `tag @s add ci_signal_active`; title *SIGNAL INTEGRITY*; `quest/refresh`.
  - `scrub_1/2/3.mcfunction` — idempotent per-board; actionbar a scrub line + `(n/3)`; sound
    `minecraft:block.beacon.deactivate`; `tag @s add ci_board_1`; `quest/refresh`.
  - `turn_in.mcfunction` — count-check on all three `ci_board_*` tags → `reward`, else actionbar *not
    three yet*.
  - `reward.mcfunction` — `function cobblemon_initiative:economy/payout {amount:700}`; `loot give`
    training_standard; `tag @s add ci_signal_done`; title *FEED SCRUBBED* subtitle *the cover-up leaks
    slower now*; `quest/refresh`.
- **QUEST_TARGETS entry** (slot 54 — verified free):

```json
{
  "holder": "q.side_signal",
  "name": "Signal Integrity",
  "slot": 54,
  "stages": [
    {
      "if_tags": [ "ci_signal_active" ],
      "not_tags": [ "ci_signal_done" ],
      "label": "Scrub 3 glitching billboards downtown",
      "target": { "x": 1550, "y": 66, "z": 1110 },
      "note": "Three board props (cyber_board_1/2/3) scattered downtown, doable in any order; downtown centroid is the area anchor, y nominal."
    }
  ]
}
```

- **REWARD/BALANCE:** 700 CD (skewed). No combat, no decline cost. Available **pre-Volt** (mid). The
  scrubbing-artifact flavor (`economy#corrupted` register on the props) is the payload.

---

### Quest 3 — "Off the Records" (WHISTLEBLOWER) — recover the scrubbed file

**Concept.** The **peak recognition beat, made a quest.** Off-Records Clerk Maren quit access control
the day the Company boxed the archives. She has been asking after the player *by description, not name*
— she took a portrait down herself and now recognizes the face on the annex step. She wants a
**scrubbed personnel file** recovered from three quality-review archive drops before it is incinerated.
She never speaks the word *Founder*; she names the **charter** and the **face**. This is the town-side
echo of **frag_7** — *You signed this charter.*

**Post-Volt only** (`defeated_cyber_leader` = late recognition). **Forward hook:** her closing line
hands the file's meaning toward the HQ — *The signature under every transition order is the same one. It
is on the charter in that tower. You would know it anywhere, I think you already do.* (circles frag_7,
points at HQ). **Back-echo:** references the liberated fields / transition orders — *The eviction papers
you have been tearing up out on the farms — they were all signed by the same hand.*

**Character JSON** — `dialog-src/characters/cyber/cyber_defector_maren.json`:

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "cyber_defector_maren",
  "display_name": "Off-Records Clerk Maren",
  "role": "quest_giver",
  "act": "2",
  "location": "Cyber City - Records Annex",
  "recognition_tier": "late",
  "recipe": "quest_fetch",
  "dialog": "dialog:cyber_off_records",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 1520, "y": 66, "z": 1100 }
}
```

**Archive-drop prop** — `dialog-src/characters/cyber/cyber_archive_1.json` (×3, ids `_1/_2/_3`). Each
`say[]` line stands alone:

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "cyber_archive_1",
  "display_name": "Archive Drop",
  "role": "civilian",
  "act": "2",
  "location": "Cyber City - Records Annex",
  "recipe": "civilian",
  "dialog_inline": {
    "kind": "dialog",
    "id": "cyber_archive_1_inline",
    "type": "BASIC",
    "entries": [
      {
        "label": "default",
        "name": "Archive Drop",
        "priority": 10,
        "default": true,
        "say": [
          "A banker box stamped for quality review. Inside, a page of a personnel file with the name column razored out.",
          "A box bound for the furnace tonight. A photo is still clipped to the corner - a face you keep almost remembering."
        ],
        "buttons": [
          { "label": "recover_button", "text": "Pocket the page", "gate": { "tag": "ci_file_active", "not_tag": "ci_file_done" }, "actions": [ { "do": "command", "cmd": "function cobblemon_initiative:sidequest/off_records/recover_1", "as_player": true }, { "do": "close" } ] },
          { "label": "leave_button", "text": "Leave it boxed", "actions": [ { "do": "close" } ] }
        ]
      }
    ]
  },
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 1518, "y": 65, "z": 1096 }
}
```

**Dialog JSON** — `dialog-src/dialog/cyber_off_records.json` (STANDARD; done > filed > accepted >
default). The default `prelock` entry (pre-Volt) tells the player to make their name in the gym first.
Each `say[]` line stands alone:

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "cyber_off_records",
  "type": "STANDARD",
  "entries": [
    {
      "label": "done",
      "name": "Maren - the file is safe",
      "priority": 50,
      "gate": { "tag": "ci_file_done" },
      "say": [
        "It is out of the building. Whatever they do to the paint on the walls, this one page they do not get to burn. I have wanted to do one honest thing since the day I carried that portrait to the incinerator.",
        "The signature under every transition order is the same one. It is on the charter in that tower. You would know it anywhere - I think you already do."
      ],
      "buttons": [ { "label": "leave_button", "text": "I think I might", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "filed",
      "name": "Maren - bring me the pages",
      "priority": 40,
      "gate": { "tag": "ci_file_active", "not_tag": "ci_file_done" },
      "say": [
        "Three drops in the annex, all stamped for quality review, all headed for the furnace tonight. Recover the pages and bring me the count. We keep the file, we keep the man.",
        "The eviction papers you have been tearing up out on the farms - they were all signed by the same hand. That hand is why I am doing this and not sleeping."
      ],
      "buttons": [
        { "label": "turnin_button", "text": "I have the pages", "actions": [ { "do": "command", "cmd": "function cobblemon_initiative:sidequest/off_records/turn_in", "as_player": true }, { "do": "close" } ] },
        { "label": "leave_button", "text": "Not all three yet", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "accepted",
      "name": "Off-Records Clerk Maren",
      "priority": 30,
      "gate": { "tag": "defeated_cyber_leader", "not_tag": "ci_file_active" },
      "say": [
        "I know your face. Not your name - they took the name off everything, I helped them do it. But I dusted that face off a portrait the size of a door and I have not slept right since.",
        "I am not going to fight you. Nobody in that tower who remembers the charter is going to raise a hand to you. I am asking a favor instead - there is a file they are about to burn, and it is yours, though you do not know it. Get me the pages before the furnace does."
      ],
      "buttons": [
        { "label": "accept_button", "text": "Where are the pages?", "actions": [ { "do": "command", "cmd": "function cobblemon_initiative:sidequest/off_records/start", "as_player": true }, { "do": "close" } ] },
        { "label": "leave_button", "text": "This is a lot", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "prelock",
      "name": "A burned-out clerk",
      "priority": 10,
      "default": true,
      "say": [
        "You are new. Make your name in the gym before you go poking around the annex - a stranger reading the archives gets filed, and I am done filing people.",
        "Come find me after you beat Volt. There is something I want to say to a face, and I want to be sure it is the right one."
      ],
      "buttons": [ { "label": "leave_button", "text": "Later, then", "actions": [ { "do": "close" } ] } ]
    }
  ]
}
```

- **DATAPACK NEEDS** (`function/sidequest/off_records/`):
  - `start.mcfunction` — `tag @s add ci_file_active`; title *OFF THE RECORDS*; the frag_7 town-echo
    delivered via `{do:"announce"}`-style title/actionbar in the founder-voice register (*You signed
    this charter.* — clean text, no `"`/`'`); `quest/refresh`.
  - `recover_1/2/3.mcfunction` — idempotent per-drop; actionbar recovery line + `(n/3)`; sound
    `minecraft:item.book.page_turn`; `tag @s add ci_page_1`; `quest/refresh`.
  - `turn_in.mcfunction` — count-check on `ci_page_1/2/3` → `reward`, else actionbar *not all three*.
  - `reward.mcfunction` — `function cobblemon_initiative:economy/payout {amount:1200}`; `loot give @s loot
    cobblemon_initiative:npc_gift/training_standard`; `tag @s add ci_file_done`; **title sting**
    *ASSET RECLAIMED* subtitle *one page they do not get to burn*; `quest/refresh`.
- **QUEST_TARGETS entry** (slot 53 — verified free):

```json
{
  "holder": "q.side_offrecords",
  "name": "Off the Records",
  "slot": 53,
  "stages": [
    {
      "if_tags": [ "ci_file_active" ],
      "not_tags": [ "ci_file_done" ],
      "label": "Recover 3 file pages from the archive drops",
      "target": { "npc": "cyber_defector_maren" },
      "note": "Three archive-drop props (cyber_archive_1/2/3) clustered at the records annex; Maren anchors the line since the drops sit at her doorstep. Distinct holder from the shipped q.side_offrec (Hua Zhan Off the Record errand chain)."
    }
  ]
}
```

- **REWARD/BALANCE:** 1200 CD (skewed). **No combat** — the whole point is a Company person who will
  **not** raise a hand against the founder (the some-stand-down late-recognition tier). No decline
  cost. **Post-Volt only** (`defeated_cyber_leader`). This is the peak-recognition + frag_7 echo beat.

---

### Quest 4 — "The Door Downtown" (HQ POINTER + opt-in wager) — the raid gate, spoken aloud

**Concept.** The quest that **literally points the player at the HQ gate.** Grid Broker Ohmond reads
the field ledgers for sport. He tells the player the raid-gate math out loud — **badges ≥ 7 AND
`fields_liberated ≥ 4`** — echoes **DJ's off-screen refusal** (*the monopoly holds*), and points at the
nearest un-liberated feeders (**Fenceline `farm_6`** south, **Coldfurrow `farm_7`** east) and at the
tower lobby `[1590 51 1028]`. As a corporate-dread flourish he offers an **opt-in, decline-able wager**
on his hoarded electric team (above-cap → stake printed, fail-soft, decline costs CD). This is the
mainline hand-off to the KEYCARD marquee (owned by `10_hq_raid.md`).

**Forward hook:** the whole quest IS the forward hook — it names Fenceline, Coldfurrow, and the tower.
**Back-echo:** *Every field you have already flipped is a field not answering their memos. Do the math
the acting man in the chair is praying you cannot.* (references `fields_liberated` and DJ's monopoly
stance in spirit — the boss is never named beyond *the acting man in the chair*).

**Character JSON** — `dialog-src/characters/cyber/cyber_grid_broker.json` (the wager is an opt-in
above-cap battle — trainer `sq_cyber_broker_wager`, decline charged via the paid-decline rail). Both
`on_win` and `on_lose` route the pointer through `door/deliver_pointer` (idempotent):

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "cyber_grid_broker",
  "display_name": "Grid Broker Ohmond",
  "role": "quest_giver",
  "act": "2",
  "location": "Cyber City - Downtown Seam",
  "recognition_tier": "late",
  "trainer": "sq_cyber_broker_wager",
  "recipe": "quest_fetch",
  "dialog": "dialog:cyber_door_downtown",
  "movement": { "objective": "ambient_stationary_look" },
  "battle": {
    "trainer": "sq_cyber_broker_wager",
    "type": "wager",
    "format": "GEN_9_SINGLES",
    "prize": 1500,
    "loss_fee": 1500,
    "decline_fee": 300,
    "defeat_tag": "defeated_sq_cyber_broker_wager",
    "win_line": "Ha. Fast hands, faster head. The stake is yours, fair and audited. Now go spend it kicking fences.",
    "lose_line": "The house keeps the stake. Call it a data-processing fee. No refunds - I read the ledgers, I do not run a charity.",
    "already_beaten_line": "One wager a visit. My whole edge is not letting the same trainer read me twice.",
    "on_win": [ "execute as @1 run function cobblemon_initiative:sidequest/door/deliver_pointer" ],
    "on_lose": [ "execute as @1 run function cobblemon_initiative:sidequest/door/deliver_pointer" ]
  },
  "placement": { "x": 1555, "y": 65, "z": 1108 }
}
```

**Dialog JSON** — `dialog-src/dialog/cyber_door_downtown.json` (STANDARD; pointer_done > wager_offer >
default). The `wager_offer` entry fires post-Volt and before the pointer is delivered; the wager is a
separate opt-in button and the paid-decline still delivers the free pointer. Each `say[]` line stands
alone:

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "cyber_door_downtown",
  "type": "STANDARD",
  "entries": [
    {
      "label": "pointer_done",
      "name": "Ohmond - do the math",
      "priority": 40,
      "gate": { "tag": "hq_pointer_done", "not_tag": "defeated_sq_cyber_broker_wager" },
      "say": [
        "You have the math now. Seven badges, four fields flipped, and the tower door stops being a wall. The acting man upstairs is praying you cannot count that high.",
        "Fenceline Acres is south of town, Coldfurrow is east on the Ryujin road. Both still feed his desk. Empty them and the number opens. Fancy a wager while you decide."
      ],
      "buttons": [
        { "label": "wager_button", "text": "Take the wager - 1500 CD on the line", "actions": [ { "do": "battle" } ] },
        { "label": "leave_button", "text": "I have fences to kick", "actions": [ { "do": "close" } ] }
      ],
      "no_goodbye": true
    },
    {
      "label": "wager_offer",
      "name": "Ohmond - a friendly line",
      "priority": 30,
      "gate": { "tag": "defeated_cyber_leader", "not_tag": "hq_pointer_done" },
      "say": [
        "You want the door math, I will give you the door math - but a broker never gives advice for free. A friendly wager first. My team is above your cap, so this is a real risk, printed plain.",
        "Fifteen hundred CobbleDollars if you win, fifteen hundred if you lose, three hundred to walk away and just hear the math. No pressure. Either way you leave knowing the number."
      ],
      "buttons": [
        { "label": "wager_button", "text": "Take the wager - 1500 CD on the line", "actions": [ { "do": "battle" } ] },
        { "label": "hear_button", "text": "Pay 300 and just tell me the door math", "actions": [ { "do": "command", "cmd": "function cobblemon_initiative:sidequest/door/decline_wager", "as_player": true }, { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Grid Broker Ohmond",
      "priority": 10,
      "default": true,
      "say": [
        "Ohmond, grid broker. I buy blackout futures and sell the truth cheap. Make your name in the gym and come back - I only tell the door math to trainers the tower is scared of.",
        "That tower on the north plaza. There is a way in, and a number that opens it. Beat Volt and I will spell it out."
      ],
      "buttons": [ { "label": "leave_button", "text": "Soon", "actions": [ { "do": "close" } ] } ]
    }
  ]
}
```

- **DATAPACK NEEDS** (`function/sidequest/door/`):
  - `deliver_pointer.mcfunction` — the one-shot pointer payload: guarded `execute unless entity
    @s[tag=hq_pointer_done] run ...`; deliver the gate math + the two field pointers + the lobby coord
    via `{do:"announce"}`-style title/chat (gold; **mirror the roadmap keycard `announce` text so both
    land the same line — clean text, no `"`/`'`/`%`**); `tag @s add hq_pointer_done`; **title sting**
    *THE DOOR DOWNTOWN* subtitle *badges seven, fields four, then the tower*; swap the main HUD toward
    *Liberate the fields, then raid HQ* (defer HUD ownership to `10_hq_raid.md`/`mainline_spine`);
    `quest/refresh`.
  - `decline_wager.mcfunction` — the paid-decline: **charge 300 CD via the pay-probe rail** (balance-safe,
    cannot go negative; NOTE: charge via pay-probe — engine work), print the `economy#decline_receipt`
    line, then call `deliver_pointer`.
  - **Wager on_win / on_lose:** both are wired on the character `battle` block (above) to call
    `deliver_pointer` so the pointer lands whether the player wins, loses, or declines the wager. The
    deliver call is idempotent via the `hq_pointer_done` guard, so it never double-fires.
- **RCT NEEDS:** `sq_cyber_broker_wager` — a small hoarded-electric team **above cap 62** (e.g.
  Electivire/Magnezone/Raichu ~64-65) so the risk is real; team file `rctmod/trainers/sq_cyber_broker_wager.json`
  + registry entry (`category: sidequest` or `villain_team`), `prerequisites: []` (the dialog gate
  handles ordering). **Fairness floor (HARD RULE 5):** the wager is opt-in and only offered post-Volt;
  loss just charges the printed `loss_fee` — no `player.kill`, no despawn-lock, so a player with a
  small or empty party cannot be whited out by it (same shape as `sq_genji_wager`). Confirm the
  loss path never routes through the Nuzlocke whiteout (Open Question 2).
- **QUEST_TARGETS entry** (slot 52 — verified free; highest-priority side line here, the raid on-ramp):

```json
{
  "holder": "q.side_door",
  "name": "The Door Downtown",
  "slot": 52,
  "stages": [
    {
      "if_tags": [ "hq_pointer_done" ],
      "not_tags": [ "hq_keycard" ],
      "label": "Liberate the last fields, then find the tower door",
      "target": { "x": 1565, "y": 65, "z": 1732 },
      "note": "Points at Fenceline Acres (farm_6) as the nearest un-liberated feeder; once hq_keycard lands (owned by 10_hq_raid.md) the mainline HUD takes over. Coldfurrow (farm_7) at [1925 64 963] is the alternate feeder."
    },
    {
      "if_tags": [ "defeated_cyber_leader" ],
      "not_tags": [ "hq_pointer_done" ],
      "label": "Hear the door math from Ohmond",
      "target": { "npc": "cyber_grid_broker" }
    }
  ]
}
```

- **REWARD/BALANCE:** wager **1500 CD** on win, **1500 CD** loss fee, **300 CD** decline fee — all
  opt-in with the stake printed (HARD RULE 5/6). The **pointer payload is free and always delivered**
  (win, lose, or decline) — the raid on-ramp is never gated behind a fight. The wager team is
  **above cap 62 by design**, which is legal *because it is opt-in, decline-able, and fail-soft*
  (loss just charges the fee). **Post-Volt only.**

---

## 4. Recognition & economy beats

**Recognition (band: entering mid → post-Volt late = PEAK, pre-HQ home turf).** Cyber City is the
Company home turf, so recognition is at its **highest pre-HQ intensity**. The gradient here:

- **Maren (Quest 3) — the stand-down tier, made central.** A veteran who *personally took the
  portrait down* recognizes the **face**, refuses to fight, and does one honest thing. She names the
  **charter** and the **face**, never *Founder* (HARD RULE 7). This is the town's marquee recognition
  moment and the frag_7 town-echo. Line seed (dialog above): *I dusted that face off a portrait the
  size of a door.*
- **The teller & Rell (Quests 1-2) — civilians, no recognition of the founder.** They feel the
  **propaganda decay** and the **broken money**, never the man. The teller's `paid_out` line reacts to
  the sanded signatures on the *reserve tags* — a scrubbing artifact — but never places the founder or
  the player; it stays in the money (HARD RULE: civilians never recognize the founder). Rell's boards
  leak the *retired then never existed* cover-up.
- **Ohmond (Quest 4) — half-knows.** He reads ledgers, not portraits; he knows the tower is *scared
  of* a specific trainer without knowing why. He treats the player as a live threat and sells the
  door math — he never claims to recognize the founder.

**Villain-recognition line for this band** (drop into any grunt/veteran ambient in-town, `late` tier,
`recognition:"late"` gate, from `scrubbing#veterans` / `scrubbing#the_memo`): *You are supposed to be
filed. I saw the portrait come down. I helped carry it, and it was heavier than it should have been.*

**Economy voice (gated on `cd_instability` — PEAK here at 56).** Cyber City is where the money is at
its most broken. Sources:
- `economy#reassurance` (Act-2 nervous over-explaining) on any Company spokes-NPC / shop signage:
  *Prices are simply adjusting. That is normal. That is healthy. Please do not hoard.*
- `economy#instability` tier **`gte 56`** on civilians/ambient: *The vaults are echoing. Somebody
  emptied the nether stars and hoped no one would look.* / *Some folk are paying in grain now.*
- **Every payout in this town reads the skew** — Exchange Rate uses `payout_company` (branded rate
  line: *ADJUSTMENT: rounding, in the Company favor*) so the peak shortfall lands on camera.
- **Field-liberation easing:** as the player frees Fenceline/Coldfurrow (pushed by Quest 4),
  `cd_instability` claws down (−6/field via `free_field_apply`), CobbleDollar shop prices ease
  (relief tier), Granary wheat prices worsen — the plot in the price tags. The rumor hub's back-echo
  line (*the board twitched down half a point*) is the diegetic read of this.

**DJ's off-screen refusal, echoed town-side.** Ohmond (Quest 4) and Maren both reference DJ's monopoly
stance without naming him beyond *the acting man in the chair* / *the man upstairs* (HARD RULE 7 —
Act-2 boss is only ever **DJ**, and not named further here). The player learns the gate rule *from the
town*, then goes and does the thing the boss dared them to.

---

## 5. New tags/scores introduced

| tag / score | set by | gated by (who reads it) |
|-------------|--------|-------------------------|
| `ci_reserves_active` | `sidequest/exchange/start` | Quest 1 `accepted` entry; `q.side_exchange` HUD |
| `ci_reserve_1/2/3` | `sidequest/exchange/note_1/2/3` (props) | `exchange/turn_in` count-check |
| `ci_reserves_done` | `sidequest/exchange/reward` | Quest 1 `paid_out` entry; rumor-hub `tip_exchange` not-gate |
| `ci_signal_active` | `sidequest/signal/start` | Quest 2 `accepted`; `q.side_signal` HUD |
| `ci_board_1/2/3` | `sidequest/signal/scrub_1/2/3` (props) | `signal/turn_in` count-check |
| `ci_signal_done` | `sidequest/signal/reward` | Quest 2 `done`; rumor-hub `tip_signal` not-gate |
| `ci_file_active` | `sidequest/off_records/start` | Quest 3 `filed`; archive-prop recover buttons; `q.side_offrecords` HUD |
| `ci_page_1/2/3` | `sidequest/off_records/recover_1/2/3` (props) | `off_records/turn_in` count-check |
| `ci_file_done` | `sidequest/off_records/reward` | Quest 3 `done`; rumor-hub `tip_records` not-gate |
| `hq_pointer_done` | `sidequest/door/deliver_pointer` | Quest 4 `pointer_done` entry; `q.side_door` HUD; rumor-hub `tip_hq` not-gate |
| `defeated_sq_cyber_broker_wager` | wager win (`defeat_tag`) | Quest 4 line-select (post-wager flavor) |

**Reused, not introduced:** `defeated_cyber_leader` (shipped, gates all post-Volt content),
`fields_liberated` / `wheat_war_active` (Wheat War, read by back-echoes), `hq_keycard` (owned by
`10_hq_raid.md`, read by `q.side_door` stage-1 not-gate), `cd_instability` (economy voice), the
`badges_gte_7` band tag (recognition-late, auto-maintained). No new numeric dialog gates → **no new
band tags** needed from `content_compile` (all quest state is plain tags).

---

## 6. Build checklist

1. **Create the folder** `dialog-src/characters/cyber/` (matches `hua_zhan/` / `takehara/` convention;
   the roadmap already recommends migrating the shipped `cyber_*` gym bodies here — coordinate, do not
   duplicate them).
2. **Drop 5 giver/hub character files** into `characters/cyber/`: `cyber_nurse_rumor.json`,
   `cyber_exchange_teller.json`, `cyber_signal_tech.json`, `cyber_defector_maren.json`,
   `cyber_grid_broker.json`.
3. **Drop 9 prop character files** (disguised-prop `station_moss` pattern, inline BASIC dialog):
   `cyber_reserve_1/2/3.json`, `cyber_board_1/2/3.json`, `cyber_archive_1/2/3.json`.
4. **Drop 4 dialog trees** into `dialog-src/dialog/`: `cyber_rumor_hub.json`, `cyber_exchange_rate.json`,
   `cyber_signal_integrity.json`, `cyber_off_records.json`, `cyber_door_downtown.json` (5 files —
   the props use `dialog_inline`).
5. **Author datapack functions** under `src/main/resources/data/cobblemon_initiative/function/sidequest/`:
   - `exchange/` — `start`, `note_1/2/3`, `turn_in`, `reward` (mirror `price_check/`).
   - `signal/` — `start`, `scrub_1/2/3`, `turn_in`, `reward`.
   - `off_records/` — `start`, `recover_1/2/3`, `turn_in`, `reward`.
   - `door/` — `deliver_pointer`, `decline_wager`.
   - Reuse `economy/payout`, `economy/payout_company`, `quest/refresh`, plain prop-tags (no scratch
     objective needed). `npc_gift/training_standard` loot table id is confirmed present (used by Genji).
6. **Add the RCT wager trainer:** `data/rctmod/trainers/sq_cyber_broker_wager.json` (above-cap electric
   team ~64-65) + a registry entry with `prerequisites: []`. Copy `sq_genji_wager`'s shape (including
   its no-whiteout loss path).
7. **Append 4 QUEST_TARGETS stage blocks** to `registers/quest_targets.json` at the **verified-free
   slots 52-55**: `q.side_door` (52), `q.side_offrecords` (53), `q.side_signal` (54), `q.side_exchange`
   (55). (Slots 57-81 and 100 are all taken; the door/raid on-ramp sits highest of the four.)
8. **Append the side-HUD render blocks** to `function/quest/render.mcfunction` (copy the Hua Zhan
   `q.side_prices` block: reset holder, set slot, set name only while active-and-not-done). The main
   HQ line already exists — do not duplicate it; `q.side_door` is a *side* line pointing at fields.
9. **Compile & regen** (roadmap §8 pipeline): `scripts/content_compile` → `scripts/update_preset_index`
   → `scripts/generate_npc_function`. Confirm the 14 no-`uuid` bodies self-spawn once via the generated
   proximity function and the preset map regenerates.
10. **Macro-safety lint:** confirm no `"` in any `say[]` / `announce` / `text`, and no `'`/`%` in any
    `cmd`/`win_line`/`lose_line`/`on_win`/`on_lose`/`announce` value (the `§k` in the billboard say
    line is fine; it is a color code, not a quote). The wager win/lose lines above are already clean
    (no apostrophes: *do not run a charity*, *call it a data-processing fee*). Confirm every `say[]`
    array reads as independent alternatives, never a sequence (HARD RULE 2).
11. **Coordinate the hand-off with `10_hq_raid.md`:** the `door/deliver_pointer` announce text should
    MATCH the keycard admin's lobby pointer verbatim (same coord `[1590 51 1028]`, same starve-the-fields
    line) so the two beats read as one continuous instruction. **The gate number is `fields_liberated
    ≥ 4` — owned by `10_hq_raid.md`; do not diverge from it here.**

**Totals to build:** 5 giver/hub characters + 9 props = **14 character files**; **5 dialog trees**
(4 standalone + the rumor hub); **4 side quests** + rumor hub; ~15 datapack functions; 1 RCT wager
trainer; 4 register stages; 4 HUD render blocks.

---

## 7. Open questions for showrunner

1. **Placement coords.** Only the exchange teller / signal tech / reserve+board props have roadmap-fixed
   PROPOSED coords; the nurse, Maren, Ohmond, and the archive props are **PLACEHOLDER** (needs builder
   confirm inside the Cyber City polygon `x∈[1229,1798], z∈[888,1472]`, PIP-verified). Ohmond sits near
   the HQ seam (`~[1555,65,1108]`) so his door-pointer reads as *right at the wall*.
2. **Wager fairness floor (HARD RULE 5).** Ohmond's wager team is intentionally **above cap 62**. Confirm
   the opt-in + printed-stake + decline-fee treatment satisfies the rule, and confirm the wager's loss
   path never routes through the Nuzlocke whiteout (`player.kill`) — it should only charge `loss_fee`,
   the same as `sq_genji_wager`.
3. **Two defectors, or one?** Maren (`cyber_defector_maren`, whistleblower, this doc) is deliberately
   distinct from `cyber_access_admin` (the KEYCARD marquee, `10_hq_raid.md`). Both are a Company person
   who stands down. Is two defectors in one town the right density (Maren = *file*, admin = *door*), or
   should Maren fold into a pre-Volt phase of the access-admin? Recommend **keep two** — the file quest
   is optional colour, the keycard is the mainline, and they hit different frag_7 facets.
4. **Slot numbers.** `q.side_door/offrecords/signal/exchange` are proposed at the **verified-free slots
   52-55** (the register's used band runs 57-81 + 100; slots 52-56 are free). Confirm the sidebar order
   reads sensibly (the door/raid on-ramp sits highest at 52). Note also there is a **distinct** shipped
   `q.side_offrec` (Hua Zhan Off the Record errand chain, slot 66) — the new `q.side_offrecords` holder
   name is deliberately different so the two do not collide.
5. **Does The Door Downtown (Quest 4) ship, or is the keycard admin sufficient?** Quest 4 exists to make
   the gate math *legible in dialog* and give the fields a diegetic push; the keycard admin
   (`10_hq_raid.md`) also delivers a pointer. If they overlap too much, Quest 4 could drop the pointer
   and keep only the wager + field-nudge. Recommend **ship both** — the admin fires on Volt's defeat as
   the marquee; Ohmond is the re-visitable remind-me-the-math NPC + optional wager. Both mirror the same
   `fields_liberated ≥ 4` gate text.
