# 11 — Act 3: The Board Hunt + The Founder (mirror)

> SLUG: `11_board_founder` • Act 3 endgame • Unlocks on `royal_league_champion` (cap → 85,
> then 100 on `board_cleared`). This unit **reimagines the Board as a scattered map-wide
> HUNT** (per the assignment brief) rather than four seats in the HQ tower. Each of the four
> Board members hides at a different thematic site; finding and beating all four unlocks the
> Founder mirror at HQ `[1590 51 1028]`. **See Open Question 1 — this is a live design fork
> with roadmap `14_board_and_founder.md`, which seats the four in the boardroom.**
>
> **EXTEND the five boss characters; REWRITE the four-beat reveal.** The five boss character
> files, the `board_micah`/`madeline`/`matt`/`lauren` battle blocks, the Founder dialog, the
> `#board` render scratch, and the `q.main` HUD ladder all ALREADY EXIST. This unit adds:
> (a) `placement` stubs scattering the four across the atlas; (b) a short find-quest /
> recognition beat per member (four pointer NPCs, four new side quests); (c) the Founder
> approach choreography. It also **replaces `reveal/board_fell.mcfunction`** — the shipped
> version writes tower-seated beats (*upstairs*, *the table*, *the room*) that are false for a
> scattered hunt. This unit ships four **venue-neutral** oblique beats that circle the Founder
> name without closing it, working for either the scattered or the seated placement. The
> `reveal/founder_defeated` function is untouched (the live-name reveal is venue-independent).

---

## 1. Overview

The set-piece is the **payoff of the whole amnesia arc** — the identity plot, not the
economy plot. It runs **after the Royal League Champion falls** (`royal_league_champion`),
in the level 85 → 100 window (Battle Frontier plays out in the same window; the two are
parallel post-game content).

| Band field | Value |
|---|---|
| Act | **3** |
| Level cap | **85** entering (Champion just unlocked it). The Board are fought in the 85→100 window; the **shipped rctmod teams span level 82–87** (verified in `data/rctmod/trainers/board_*.json` — Madeline is 82/83/84/85/86/87). So the Board sit right at and just above the entry cap — the last legal cruelty. `board_cleared` → **100** *before* the Founder, who is a flat level-100 mirror. |
| `cd_instability` | **25 (held)**. DJ's Act-2 defeat already stabilised it. Act 3 does **not** move the index — no `economy/gym_destabilize` or `hq_stabilize` calls fire here. The economy plot is resolved; this is the identity plot. |
| Recognition tier | **late** (all five bosses `"recognition_tier":"late"`). The gradient's terminal state: some Board members **stand down** (Micah intellectually, Lauren emotionally) rather than raging; Matt doubles down; Madeline is proud she proposed the coup. |
| Memory fragment | **None new.** All 10 land by Scorchspire (frag_10 "face your own signature"). Act 3 *is* the fragment resolution — the reveal replaces the drip. |
| Canon flags set | Board → `defeated_board_{madeline,matt,micah,lauren}` → derived `board_cleared` (Java, `PlayerProgressManager`, cap 100). Founder → `company_overthrown` + alias `defeated_villain_final_boss`. |

**The arc job:** the Board hunt makes the audience *earn* the four oblique title beats that
circle the Founder's name (`reveal/board_fell`), while sending the player back across the
whole liberated map to sites that echo the run's themes — a shuttered branch office, a
granary, a frontier facility, a shrine outskirt. Each is a mini set-piece with a
recognition/ideology beat: the four people who signed your removal, each hiding in the part
of the region their department ruined. Clearing all four opens the mirror at HQ; the mirror
prints the winning player's own name and grants `company_overthrown`, then the HUD flips to
`▶ Hunt the Ender Dragon` and the reclaimed founder walks off the curated map into raw
generated terrain — still hardcore + Nuzlocke.

**Route placement:** terminal. After the Royal League `[3528 166 2773]`, before the vanilla
Ender-Dragon post-story. The four hunt sites span the map (Cyber-City fringe, Kalahar's
granary belt, the eastern Battle Frontier, the western Fairy Shrine outskirts), so the hunt
is literally a victory lap across everything the player liberated.

**The four scattered sites (grounded in zone centroids — builder to confirm exact anchors):**

| Board member | Site | Why this site (theme) | Anchor coord |
|---|---|---|---|
| `board_madeline` — the vote | **Fenceline Acres** (shuttered branch office / corporate farm on the Cyber-City approach) | She *proposed the motion*; she waits at the corporate field nearest the tower she voted you out of — a branch office gone dark. | `[1565 65 1732]` |
| `board_micah` — the accountant | **Crossroads Granary** (the ledger site) | He *reserved against your return*; a granary is where the region's numbers are kept — his spreadsheet made flesh. | `[2310 65 3538]` |
| `board_matt` — the protégé | **Battle Factory** outskirts (frontier facility, rental-assets ethos) | He learned *people are positions*; the Factory's rental ethos is his whole worldview — assets in, assets out. | `[4060 66 2774]` |
| `board_lauren` — the storyteller | **Fairy Shrine outskirts** (remote branch / narrative-control site) | Communications; she *wrote you out of the story*. A shrine on the far western edge is where a story goes to become myth — the perfect place to end one. | `[947 66 2651]` |

All four coords are **PROPOSED — needs builder confirm** (zone centroids; y is nominal 64+offset,
x/z authoritative). Adjust to a specific building / outcropping at each site during placement.

---

## 2. Cast

All four Board characters and the Founder ALREADY EXIST under
`dialog-src/characters/villain/`. This unit adds **one new pointer NPC per site** (a
recognition-beat civilian / keeper who points at the hidden Board member — the "rumor-hub"
house-style pointer for a map with no town here), plus the four `placement` stubs on the
existing bosses and the Founder.

| id | display_name | role | concept | anchor coord |
|---|---|---|---|---|
| `board_madeline` *(EXISTS — add `placement`)* | `M§kaaaaaaaaa` | villain_board | Proposed the motion; proud of the coup. Waits at the dark branch office. | `[1565 65 1732]` |
| `board_micah` *(EXISTS — add `placement`)* | `M§kaaaa` | villain_board | The accountant who reserved against your return. At the granary. | `[2310 65 3538]` |
| `board_matt` *(EXISTS — add `placement`)* | `M§kaaa` | villain_board | The protégé you trained; doubles down. At the Factory. | `[4060 66 2774]` |
| `board_lauren` *(EXISTS — add `placement`)* | `L§kaaaaa` | villain_board | Communications; wrote you out. At the shrine outskirts. | `[947 66 2651]` |
| `villain_final_boss` *(EXISTS — add champion-gated `placement`)* | `§kfounder` | villain_final_boss | The mirror. Spawns at HQ only after Champion; wears the player's skin. | `[1590 51 1028]` |
| `hunt_clerk_fenceline` **(NEW)** | Shuttered Clerk | civilian | A laid-off branch clerk sweeping the dark office; recognizes nothing of the CEO, only that the portrait wall is a brighter rectangle now. Points at Madeline. | `[1560 65 1740]` |
| `hunt_keeper_odei` **(NEW)** | Granary Keeper Odei | civilian | A liberated-field granary keeper (back-echo: the wheat war). Recognizes a face on a memo mid-count and turns wary — points you to the auditor pacing the silos. | `[2305 65 3545]` |
| `hunt_bystander_factory` **(NEW)** | Frontier Bystander | civilian | A Frontier hanger-on outside the Factory; saw a suited man filing someone. Points at Matt. | `[4055 66 2780]` |
| `hunt_pilgrim_wren` **(NEW)** | Shrine Pilgrim Wren | civilian | A pilgrim at the Fairy Shrine edge; speaks the story Lauren wrote (the founder retired). Points at Lauren without knowing what she is pointing at. | `[950 66 2658]` |

> **No-duplication check.** `dialog-src/characters/villain/granary_keeper.json` already exists
> (display_name **Feng**, role `granary`, at Hua Zhan City, `uuid`-placed). The new keeper is a
> **different NPC** — id `hunt_keeper_odei`, role `civilian`, at Crossroads Granary, `placement`-
> spawned — so the ids do not collide. Renamed the four pointers to unambiguous ids
> (`hunt_keeper_odei` / `hunt_bystander_factory` / `hunt_pilgrim_wren`) to keep them distinct
> from any existing keeper/registrar/pilgrim NPC.

The four NEW pointer NPCs are `civilian` (recipe `civilian`, inferred from role §13.1),
stationary (`ambient_stationary_look`), no battle. They are the house-style **rumor hubs** for
a hunt that has no town — each gated to point only while its Board member is unbeaten, and to
fall silent after. **Civilians NEVER recognize the Founder** (canon, LORE_BIBLE §4): they react
to propaganda decay and scrubbing artifacts, not to the CEO. Odei (a liberated-field keeper) is
the one allowed a wary mid-count beat — she recognizes *a face on a memo*, never the founder,
and never learns who you are.

---

## 3. Quests

Six quest subsections: the four Board mini set-pieces (one per site), the Founder approach,
and a light collectible tying them together.

---

### 3.1 The Vote (Board Hunt — Madeline, Fenceline Acres)

**Concept:** the shuttered branch office on the Cyber-City approach. A laid-off clerk sweeps
a floor where a brighter rectangle marks a vanished portrait. Behind the frosted-glass
office, `M§kaaaaaaaaa` sits at a dead desk — she proposed the motion to erase you and stayed
to watch the branch go dark. **Forward hook:** the clerk names the other three sites — the
granary out east, the eastern arenas, the old shrine road west. **Back-echo:** the
scrubbing-artifact portrait wall (the run-long cover-up made physical); the clerk gestures at
the tower on the seam the player raided as Acting CEO DJ (Act 2). **Rumor hub:** the clerk IS
the house-style rumor hub for this town-less site, gated to point only while Madeline stands.

The Board battle itself is **already fully authored** in `board_madeline.json`
(`gate:{champion:true}`, `type:gauntlet_boss`, `on_win → reveal/board_fell`). This unit only
adds the `placement` stub and the pointer NPC. The pointer NPC has no battle — pure
recognition/flavor + a waypoint hand-off.

> **Dialog venue caveat (Open Q1).** The shipped `board_madeline.json` `default`/`after`
> dialog is written **seated** (*Sit at the door of your own boardroom*, *there is a seat at
> the head waiting*). Those lines read as boardroom geography. If the scattered hunt ships,
> the *battle wiring* is venue-independent and works unchanged, but the seated flavor lines
> want a light pass so the branch-office desk does not narrate a boardroom. This unit does
> **not** rewrite the shipped boss dialog (per "extend, do not rebuild"); the fork ruling in
> Open Q1 decides whether those flavor lines get a scattered rewrite.

**READY-TO-PASTE — extend `board_madeline.json` (add one field):**

```json
  "placement": { "x": 1565, "y": 65, "z": 1732 }
```

> Add the `"placement"` key at top level (sibling of `"battle"`). Everything else in
> `board_madeline.json` stays exactly as shipped. `placement` (no uuid) generates a
> once-per-world spawn latch; the compiler also resolves it as the quest-waypoint coord.

**READY-TO-PASTE character — `dialog-src/characters/villain/hunt_clerk_fenceline.json` (NEW):**

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "hunt_clerk_fenceline",
  "display_name": "Shuttered Clerk",
  "role": "civilian",
  "act": "3",
  "location": "Fenceline Acres - shuttered branch office",
  "recognition_tier": "late",
  "recipe": "civilian",
  "dialog": "dialog:hunt_clerk_fenceline",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 1560, "y": 65, "z": 1740 }
}
```

**READY-TO-PASTE dialog — `dialog-src/dialog/hunt_clerk_fenceline.json` (NEW):**

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "hunt_clerk_fenceline",
  "type": "STANDARD",
  "entries": [
    {
      "label": "done",
      "name": "Clerk - after",
      "priority": 30,
      "gate": { "defeated": "board_madeline" },
      "say": [
        "The lady in the corner office packed up. Left the door open for once. Feels like the whole branch can breathe again.",
        "Three more like her out there, they say. Someone should go and turn all of their lights off."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Someone will", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Shuttered Clerk",
      "priority": 10,
      "default": true,
      "gate": { "champion": true },
      "say": [
        "Branch is closed. Has been for a while. See that brighter square on the wall? A portrait hung there my whole time here. One morning it just did not. Nobody signed for it.",
        "The lady still comes in. Corner office. Says she proposed something once and likes to sit with it. Gives me the shivers. If you have business with her, it is that door. There are three more of her kind scattered off - the granary east of Kalahar, the arenas past the League, the old shrine road west. Someone is walking the whole map turning their lights off. Might as well be you."
      ],
      "buttons": [
        { "label": "leave_button", "text": "I will see her", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "pre_champ",
      "name": "Shuttered Clerk - idle",
      "priority": 5,
      "say": [
        "Branch is closed. Nothing here but dust and a bright square where a picture used to be. Come back when you have earned the right to ask about it."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:** none new. The Board battle, `defeated_board_madeline` latch, and
`reveal/board_fell` are already wired in `board_madeline.json` / the reveal function (which
this unit rewrites once, §3.5 — the same call site fires it).

**QUEST_TARGETS entry — new side holder `q.side_board_vote` (slot 54, verified free):**

```json
{
  "holder": "q.side_board_vote",
  "name": "The Vote",
  "slot": 54,
  "stages": [
    {
      "if_tags": ["royal_league_champion"],
      "not_tags": ["defeated_board_madeline"],
      "label": "Turn off the lights at the shuttered branch",
      "target": { "npc": "board_madeline" },
      "note": "Board hunt site 1 (Fenceline Acres branch office). Points at board_madeline's placement coord [1565 65 1732]. Falls off the sidebar on defeated_board_madeline. Slot 54 confirmed free (live quest_targets.json uses 57 for q.side_clinic and 58 for q.side_manifest)."
    }
  ]
}
```

**REWARD/BALANCE:** unchanged from the shipped `board_madeline.json` — `prize: 9000` CD +
config loot. **Cap note:** the shipped rctmod team is levels **82–87** (ace 87). The Founder
fight only opens after `board_cleared` raises the cap to 100, so the Board are the run's last
above-nominal-cap fights, fought while the cap is 85. They are **forced `gauntlet_boss`
set-pieces (no decline)** — but the **fairness floor holds**: a player with no caught Pokémon
cannot reach Act 3 (they cannot beat the Royal Champion), so no forced whiteout of a
starter-only player is possible here. The pointer NPC is never a battle.

---

### 3.2 The Ledger (Board Hunt — Micah, Crossroads Granary)

**Concept:** the granary where the region's numbers are kept. `M§kaaaa` — the accountant who
modelled your return as a low-probability liability and reserved against it — paces the silos,
still auditing a monopoly that lost. **Forward hook:** keeper Odei names the Battle Factory as
where the cold one went. **Back-echo:** Odei is a *liberated-field* keeper — she name-drops the
wheat war (fields freed, prices eased), and the recognition gradient's "wheat-adjacent keepers
recognize you mid-trade" beat lands here on a granary keeper. She recognizes a *face on a memo*,
not the CEO (canon: only wheat-adjacent keepers get the wary beat; she never learns who you are).
**Economy voice:** the post-HQ stabilised-then-exposed register — she says the granary flew
Company colors until they tore them down. **Rumor hub:** Odei points while Micah stands.

**READY-TO-PASTE — extend `board_micah.json`:**

```json
  "placement": { "x": 2310, "y": 65, "z": 3538 }
```

**READY-TO-PASTE character — `dialog-src/characters/villain/hunt_keeper_odei.json` (NEW):**

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "hunt_keeper_odei",
  "display_name": "Granary Keeper Odei",
  "role": "civilian",
  "act": "3",
  "location": "Crossroads Granary",
  "recognition_tier": "late",
  "recipe": "civilian",
  "dialog": "dialog:hunt_keeper_odei",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 2305, "y": 65, "z": 3545 }
}
```

**READY-TO-PASTE dialog — `dialog-src/dialog/hunt_keeper_odei.json` (NEW):**

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "hunt_keeper_odei",
  "type": "STANDARD",
  "entries": [
    {
      "label": "done",
      "name": "Odei - after",
      "priority": 30,
      "gate": { "defeated": "board_micah" },
      "say": [
        "The one who kept counting my grain is gone. Good. This granary is ours again, and the numbers finally add up in our favor for once.",
        "Whatever you are, you leave the fields lighter than you found them. That is more than the Company ever did."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Keep it that way", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "recognize",
      "name": "Odei - the memo",
      "priority": 20,
      "gate": { "champion": true, "fields_liberated": { "op": "gte", "value": 4 } },
      "say": [
        "Hold on. I know that face. Not from here - from a memo they pinned in the drying shed. A face we were told to forget, they said, filed and gone. And here it is, walking my silos.",
        "I freed this granary from them, so I owe you nothing but a warning. The cold accountant is still here, out among the bins, reserving against a loss that already happened. Go and be his loss. When he is done, they say the cold ones scatter east to the arenas past the League."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Point me at him", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Granary Keeper Odei",
      "priority": 10,
      "default": true,
      "gate": { "champion": true },
      "say": [
        "This granary flew Company colors till we tore them down. Now it is a working store again. Prices even eased, once the fields came back to us.",
        "There is a man in a grey suit pacing the bins, still auditing grain that is not his. Says he ran the projections on everything, even you. If you have business with him, mind you do not become a line item. He is fond of those. After him, they say the cold ones scatter east to the arenas past the League."
      ],
      "buttons": [
        { "label": "leave_button", "text": "I will find him", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:** none new. `fields_liberated gte 4` lowers to a maintained band tag
(`fields_liberated_gte_4`) per the schema (Open Q1 band-tag pattern, already used by wheat
traders). By Act 3 all fields are liberated, so this gate always passes — it is there so the
recognition entry is honest and stays keeper-scoped.

**QUEST_TARGETS entry — `q.side_board_ledger` (slot 53, verified free):**

```json
{
  "holder": "q.side_board_ledger",
  "name": "The Ledger",
  "slot": 53,
  "stages": [
    {
      "if_tags": ["royal_league_champion"],
      "not_tags": ["defeated_board_micah"],
      "label": "Audit the accountant at Crossroads Granary",
      "target": { "npc": "board_micah" },
      "note": "Board hunt site 2. Points at board_micah placement [2310 65 3538]. Off the sidebar on defeated_board_micah. Slot 53 confirmed free."
    }
  ]
}
```

**REWARD/BALANCE:** unchanged — `prize: 9000` CD + config loot. Cap as §3.1 (team 82–87). No
decline (forced `gauntlet_boss`); fairness floor holds. Pointer NPC has no battle.

---

### 3.3 The Position (Board Hunt — Matt, Battle Factory outskirts)

**Concept:** the Battle Factory (rental-assets ethos) — the frontier facility whose whole
worldview is Matt's: people are positions, assets in, assets out. `M§kaaa`, the protégé you
trained, doubles down here, running rented teams like the inventory he thinks people are.
**Forward hook:** the bystander names the far-western shrine road as the last site.
**Back-echo:** the Frontier itself (post-League proving ground the player is grinding in
parallel) and the mirror theme one act early (see `battle_frontier` §5.2). **Rumor hub:** the
bystander points while Matt stands.

**READY-TO-PASTE — extend `board_matt.json`:**

```json
  "placement": { "x": 4060, "y": 66, "z": 2774 }
```

**READY-TO-PASTE character — `dialog-src/characters/villain/hunt_bystander_factory.json` (NEW):**

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "hunt_bystander_factory",
  "display_name": "Frontier Bystander",
  "role": "civilian",
  "act": "3",
  "location": "Battle Factory - outskirts",
  "recognition_tier": "late",
  "recipe": "civilian",
  "dialog": "dialog:hunt_bystander_factory",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 4055, "y": 66, "z": 2780 }
}
```

**READY-TO-PASTE dialog — `dialog-src/dialog/hunt_bystander_factory.json` (NEW):**

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "hunt_bystander_factory",
  "type": "STANDARD",
  "entries": [
    {
      "label": "done",
      "name": "Bystander - after",
      "priority": 30,
      "gate": { "defeated": "board_matt" },
      "say": [
        "The suit is gone. He kept saying loyalty is the kind that survives the person you give it to. Whatever that means. The Factory feels less like a filing cabinet now.",
        "One left, they reckon. Out past the western shrine road. Then it is just you and whatever waits at the tower."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Then west", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Frontier Bystander",
      "priority": 10,
      "default": true,
      "gate": { "champion": true },
      "say": [
        "You battle the Frontier? Good. Just mind the man in the borrowed suit by the Factory doors. He does not rent teams. He rents people. Says he learned it from someone who taught him people are positions and positions get filled.",
        "He looked at me like an entry in a ledger. Filed you too, he said, before anyone made you re-read your own name. If you mean to face him, the doors are there. After him there is one more, out on the old shrine road to the west."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Point me at the Factory", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "pre_champ",
      "name": "Frontier Bystander - idle",
      "priority": 5,
      "say": [
        "Come back when you have beaten the League. The suited one only takes meetings with Champions, and only to lose them."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:** none new.

**QUEST_TARGETS entry — `q.side_board_position` (slot 52, verified free):**

```json
{
  "holder": "q.side_board_position",
  "name": "The Position",
  "slot": 52,
  "stages": [
    {
      "if_tags": ["royal_league_champion"],
      "not_tags": ["defeated_board_matt"],
      "label": "File the protege at the Battle Factory",
      "target": { "npc": "board_matt" },
      "note": "Board hunt site 3. Points at board_matt placement [4060 66 2774] (Battle Factory outskirts). Off the sidebar on defeated_board_matt. Slot 52 confirmed free."
    }
  ]
}
```

**REWARD/BALANCE:** unchanged — `prize: 9000` CD + config loot. Cap as §3.1 (team 82–87). No
decline (forced `gauntlet_boss`); fairness floor holds.

---

### 3.4 The Story (Board Hunt — Lauren, Fairy Shrine outskirts)

**Concept:** the far-western Fairy Shrine outskirts — a story goes to the shrine to become
myth, the right place to end one. `L§kaaaaa`, Communications, wrote you out (the founder
retired). A pilgrim, Wren, speaks Lauren's exact cover-story back at you without knowing it is
a lie or that she is describing you. **Forward hook (final):** with Lauren down, Wren points at
the tower — the room is cleared. **Back-echo:** the corrupted propaganda register (the founder
retired); the shrine pilgrimage side content (`q.side_pilgrim`). **Rumor hub:** Wren points
while Lauren stands. Recommend fighting Lauren last (her rctmod team is the hardest of the
four) — the pointer chain naturally routes the player here fourth.

**READY-TO-PASTE — extend `board_lauren.json`:**

```json
  "placement": { "x": 947, "y": 66, "z": 2651 }
```

**READY-TO-PASTE character — `dialog-src/characters/villain/hunt_pilgrim_wren.json` (NEW):**

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "hunt_pilgrim_wren",
  "display_name": "Shrine Pilgrim Wren",
  "role": "civilian",
  "act": "3",
  "location": "Fairy Shrine - outskirts",
  "recognition_tier": "late",
  "recipe": "civilian",
  "dialog": "dialog:hunt_pilgrim_wren",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 950, "y": 66, "z": 2658 }
}
```

**READY-TO-PASTE dialog — `dialog-src/dialog/hunt_pilgrim_wren.json` (NEW):**

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "hunt_pilgrim_wren",
  "type": "STANDARD",
  "entries": [
    {
      "label": "done",
      "name": "Wren - after",
      "priority": 30,
      "gate": { "defeated": "board_lauren" },
      "say": [
        "The storyteller went quiet. Strange. She had a version of everything. Now the shrine is just the shrine again, and the wind does not sound rehearsed.",
        "That was the last of the four, the way they were counted. The room at the tower is cleared. Whatever is left up there is waiting only for you."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Then I climb", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Shrine Pilgrim Wren",
      "priority": 10,
      "default": true,
      "gate": { "champion": true },
      "say": [
        "You hear the story here too? The founder retired. Tired, content, signed it all away with a smile, and the region kept spending happy. A clean story. I like a clean story.",
        "The woman who tells it best keeps a shrine of her own out past the fairy stones. Communications, she calls it. She is not praying. She is editing. If you go to her, listen close - she will try to write you into something smaller. She has done it before. There are no others after her. She is the last of the four."
      ],
      "buttons": [
        { "label": "leave_button", "text": "I will hear her out", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "pre_champ",
      "name": "Shrine Pilgrim Wren - idle",
      "priority": 5,
      "say": [
        "The story is not finished being told. Come back when you have stood before the League and the region calls you Champion. Then the last chapters open."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:** none new.

**QUEST_TARGETS entry — `q.side_board_story` (slot 51, verified free):**

```json
{
  "holder": "q.side_board_story",
  "name": "The Story",
  "slot": 51,
  "stages": [
    {
      "if_tags": ["royal_league_champion"],
      "not_tags": ["defeated_board_lauren"],
      "label": "End the story at the Fairy Shrine road",
      "target": { "npc": "board_lauren" },
      "note": "Board hunt site 4. Points at board_lauren placement [947 66 2651] (Fairy Shrine outskirts). Off the sidebar on defeated_board_lauren. Slot 51 confirmed free."
    }
  ]
}
```

**REWARD/BALANCE:** unchanged — `prize: 9000` CD + config loot. Cap as §3.1 (Lauren's rctmod
team is the hardest of the four; recommend fighting last). No decline (forced `gauntlet_boss`);
fairness floor holds.

---

### 3.5 Face The Founder (the mirror, HQ)

**Concept:** with all four `defeated_board_*` tags set, `#board quest_hud` hits 4 and the
`q.main` HUD flips to `▶ Face The Founder`, pointing at HQ `[1590 51 1028]`. `§kfounder` — who
did NOT exist in the world until the Champion fell (champion-gated spawn latch) — sits at the
chair, **wearing the player's own skin**, and fields a **level-100 mirror of the player's
actual party** (the mirror bridge; see roadmap §8 for the jar-verified Java spec). The Founder
dialog, the `reveal/founder_defeated` beat, and the win → `company_overthrown` are **ALL
ALREADY AUTHORED and untouched**. This unit adds the champion-gated `placement`, rewrites the
four-beat `reveal/board_fell` (§ below), and confirms the choreography. **Forward hook:** the
win flips the HUD to `▶ Hunt the Ender Dragon` (already wired). **Back-echo:** the entire run —
the Founder's dialog names every partner you lost on the route and the years the coup emptied
out of you.

**The four oblique `reveal/board_fell` beats (REWRITTEN — venue-neutral, escalating).** The
shipped function writes tower-seated beats (*upstairs*, *the table*, *the room*, *walk up the
stairs and read the name*) that are FALSE for a scattered hunt — nobody is upstairs from a
granary. This unit replaces them with four beats that circle the Founder's name WITHOUT closing
it and read true whether the Board is scattered or seated. Each is one `title/subtitle` per
emptied seat, keyed on the recount, escalating gray → gold. No `"` inside the plain-text fields
below is authored by hand — they are delivered via the compiler `{do:"announce"}` path or a
correctly-quoted `title` the function owns; the STRINGS themselves carry no apostrophe:

| #board | Tone | Oblique beat (subtitle text — circles the name, never closes it) |
|:---:|---|---|
| 1 | gray | **One signature lifts off the page. Somewhere a static-wrapped name grows one letter louder.** |
| 2 | gray | **Two names struck through. The static thins where a third should be, and does not yet say who.** |
| 3 | gray | **Three gone. Only one hand is left holding the pen, and it writes the same way yours does.** |
| 4 | gold | **The last name falls. What is left is one figure at the chair, wearing a face you have been not-remembering. Go and read it.** |

Beat 4 also fires the tellraw pointer to the chair. The name is STILL not spoken — that waits
for `reveal/founder_defeated`, where it resolves live to the player's own name.

**Choreography (the streamable beat order):**

1. Board 4/4 down → `reveal/board_fell` fires its terminal (gold) beat + the tellraw pointing
   at the chair.
2. HUD: `▶ Face The Founder`, waypoint at `[1590 51 1028]`.
3. Player reaches HQ. The Founder's `not_ready` entry (priority 5) is bypassed once the
   higher-priority `default` (the mirror) entry passes its `all_tags:[defeated_board_*]` gate.
4. **The mirror.** *Face myself* button → mirror snapshot (party copied at press-time, levels
   → 100, §k shadow nicknames) → `GEN_9_SINGLES` vs the registered mirror team.
5. Win → `reveal/founder_defeated`: the nameplate resolves to `{"selector":"@s"}` — the
   **player's own live name** — and prints "The name on the chair was always ⟨you⟩." Sets
   `company_overthrown` + `defeated_villain_final_boss`.
6. HUD flips to `▶ Hunt the Ender Dragon` (already staged in `q.main`). The Ender-Dragon
   send-off is post-story: leave the curated map for generated terrain, still hardcore +
   Nuzlocke. **No fixed waypoint** — the stronghold is seed-procedural (`target: null`).

**READY-TO-PASTE — extend `villain_final_boss.json` (add champion-gated placement):**

```json
  "placement": { "x": 1590, "y": 51, "z": 1028, "spawn_gate": { "tag": "royal_league_champion" } }
```

> **`spawn_gate` is a PROPOSED schema extension** (roadmap §8 item 2): the generated
> `ambient/place/<key>` latch is proximity-only today; the Founder must NOT exist pre-Champion.
> If `spawn_gate` is not built, hand-author `function/reveal/founder_spawn.mcfunction` on the
> same latch pattern (`#amb_founder` on `ci_ambient`, selector
> `@a[tag=royal_league_champion,distance=..40]`, `easy_npc preset import_new`) and call it from
> the champion-win reward chain. See Open Question 3.

**READY-TO-PASTE dialog:** none new — **reuse the shipped `dialog_inline` in
`villain_final_boss.json`** (the mirror / after / not_ready three-entry tree). Do not rewrite;
its lines already narrate the mirror and the reveal is venue-independent.

**DATAPACK NEEDS:**
- `reveal/board_fell.mcfunction` — **REWRITE** (this unit) to the four venue-neutral beats
  above. The same call site fires it (`execute as @1 at @1 run function
  cobblemon_initiative:reveal/board_fell` in each `board_*` on_win, unchanged). Keep the exact
  recount block (`scoreboard players set #board quest_hud 0` then four `execute if entity
  @s[tag=defeated_board_*]` adds) — only the four `execute if score #board quest_hud matches N`
  title lines change to the beats in the table. All strings apostrophe-free.
- `reveal/founder_defeated.mcfunction` — **EXISTS, no change.** Live `{"selector":"@s"}` name,
  sets `defeated_villain_final_boss`.
- `reveal/founder_spawn.mcfunction` — **NEW, conditional** (only if `spawn_gate` is not added
  to the compiler): champion-gated import latch for the Founder body. One-line spec: on
  `ci_ambient` tick, if a `royal_league_champion` player is within 40b of `[1590 51 1028]` and
  the body is not yet spawned, `easy_npc preset import_new villain_final_boss` at the chair;
  latch a scratch so it fires once. Mirror the existing `ambient/place/*` latch shape.

**QUEST_TARGETS entry:** **none new for the mirror** — the `q.main` `Face The Founder` /
`Hunt the Board of Directors` stages ALREADY EXIST (quest_targets.json q.main stages, keyed on
`#board quest_hud` gte 4 / lte 3). **One improvement:** update the existing
`Hunt the Board of Directors` stage note (keep its `target: null` — with the four
`placement` stubs added, the four new `q.side_board_*` holders carry the per-site waypoints, so
`q.main` stays null and does not fight the side lines for the arrow):

```json
      "note": "#board scratch as above. Four Board members now SCATTERED with placement stubs (board_madeline Fenceline Acres [1565 65 1732], board_micah Crossroads Granary [2310 65 3538], board_matt Battle Factory [4060 66 2774], board_lauren Fairy Shrine outskirts [947 66 2651]); the four q.side_board_* holders (slots 51-54) carry the per-site waypoints. q.main stays null here so it does not fight the side lines for the arrow."
```

**REWARD/BALANCE:** Founder `prize: 0` (the reward is the *name*, not cash) + config loot
(Master Ball, netherite, wheat — the monopoly's own commodity handed back). Cap: the mirror is
flat level 100; `board_cleared` already raised the cap to 100 before this fight, so this fight
is cap-legal. No decline (the whole run funnels here); fairness floor holds (you cannot reach
Act 3 with zero Pokémon).

---

### 3.6 The Four Signatures (optional collectible — the erasure made physical)

**Concept:** a lore-collectible spanning the four hunt sites (LORE_BIBLE §9 — DO litter the
world with scrubbing artifacts). At each site sits a **re-verified ledger page / revised org
chart / impostor memo** — the same name whited out four times, once by each department. Reading
all four before the Founder unlocks nothing mechanical but assembles the cover-up for the
audience *before* the reveal — pure texture, opt-in, no dead-end. **Forward hook:** each page
names the next site's artifact. **Back-echo:** the run-long propaganda decay.

**Implementation is deliberately minimal** — no new NPC, no battle. Four `ON_INTERACTION`
props (item-frames / lecterns) or four tiny `civilian` notice NPCs co-located with the Board
sites. Recommend props (builder art) with a right-click function that sets a cosmetic tag.

> **No `has_item` gate.** Reading is a right-click on a prop that sets a tag — there is no
> item-fetch here, so the broken `has_item` dialog gate is not in play. If a future version
> wants the pages carried and turned in, use a turn-in count-check function (clears + counts
> the items, sets the tag), never a `has_item` gate — the Genji GOLD pattern
> (`sidequest/genji/turn_in_rod`).

**DATAPACK NEEDS:**
- `function/sidequest/signatures/read_<n>.mcfunction` (×4) — one-line spec: on read, set
  `read_signature_<n>` tag, actionbar a scrubbing-artifact flavor line (no `"`/`'`), and if all
  four are set, set `read_all_signatures` for a HUD flourish. Count via the `#board`-style
  scratch pattern if an n/4 HUD line is wanted.
- Optional: keep the signatures decoupled from `reveal/board_fell` (nothing required).

**QUEST_TARGETS entry — `q.side_signatures` (slot 50, verified free; optional):**

```json
{
  "holder": "q.side_signatures",
  "name": "The Four Signatures",
  "slot": 50,
  "stages": [
    {
      "if_tags": ["royal_league_champion", "read_signature_1"],
      "not_tags": ["read_all_signatures"],
      "label": "Read the scrubbed pages at the four sites",
      "target": null,
      "note": "Optional collectible spanning the four Board hunt sites; no single waypoint (the four q.side_board_* lines already point at the sites). Highlight-only line. Slot 50 confirmed free."
    }
  ]
}
```

**REWARD/BALANCE:** flavor only. No CD, no loot, no gate on anything downstream — purely the
audience assembling the cover-up. Fully skippable.

---

## 4. Recognition & economy beats

**Recognition (tier: `late`).** This is the gradient's terminal state. The four Board members
(shipped dialog) each embody a different late reaction: **Madeline** is *proud* (she proposed
the motion), **Matt doubles down** (the protégé who signed your removal), **Micah stands down
intellectually** (concludes you should not exist, without hate), **Lauren stands down
emotionally** (kinder than expected, warns you about the last chair). The **four NEW pointer
civilians NEVER recognize the Founder** (canon — public scrubbing worked); they react to
**scrubbing artifacts** (the brighter-rectangle portrait wall, the whited-out ledgers) and to
Lauren's cover-story (the founder retired) — propaganda decay, not the CEO. The ONE allowed
wary beat is **Odei the granary keeper** (a liberated-field keeper), who recognizes *a face on
a memo* mid-count and turns wary — the wheat-adjacent-keepers gradient rule — but she never
learns who you are.

Sample late-recognition lines (macro-safe, no `"`/`'`):
- Clerk (site 1): *A portrait hung there my whole time here. One morning it just did not.
  Nobody signed for it.* (scrubbing artifact, not recognition)
- Odei (site 2): *I know that face. Not from here - from a memo they pinned in the drying shed.
  A face we were told to forget.* (the memo-made-flesh, wary keeper)
- Wren (site 4): *The founder retired. Tired, content, signed it all away with a smile.*
  (speaking Lauren's exact cover-story, obliviously)

**Economy (`cd_instability` held at 25).** Act 3 does **not** move the index — no
`economy/gym_destabilize` / `hq_stabilize` calls fire. The economy plot is resolved. The
post-HQ **stabilised-then-exposed** register colors the flavor: any Company signage at the four
sites glitches (`§k`) and leaks cover-up lines (Odei: *this granary flew Company colors till we
tore them down*). The 9000×4 = 36k CD board windfall is the endgame faucet that funds Battle
Frontier buy-ins (that area prices it); no CD is required to enter any hunt site — the gate is
the Champion badge, not a toll. The Founder pays `prize: 0` on purpose. The `fields_liberated`
gate on Odei's recognition entry lowers to a band tag; by Act 3 all fields are liberated so it
always passes.

---

## 5. New tags/scores introduced

| Tag / score | Set by | Gated by |
|---|---|---|
| `defeated_board_madeline` *(EXISTS)* | `board_madeline` onwin | `q.side_board_vote` not_tag; clerk `done` entry; `#board` recount |
| `defeated_board_micah` *(EXISTS)* | `board_micah` onwin | `q.side_board_ledger` not_tag; Odei `done` entry; `#board` recount |
| `defeated_board_matt` *(EXISTS)* | `board_matt` onwin | `q.side_board_position` not_tag; bystander `done` entry; `#board` recount |
| `defeated_board_lauren` *(EXISTS)* | `board_lauren` onwin | `q.side_board_story` not_tag; Wren `done` entry; `#board` recount |
| `company_overthrown` / `defeated_villain_final_boss` *(EXISTS)* | Founder onwin + `reveal/founder_defeated` | HUD `Hunt the Ender Dragon`; Mom homecoming |
| `read_signature_1..4` **(NEW, optional)** | `sidequest/signatures/read_<n>` | signatures collectible HUD line |
| `read_all_signatures` **(NEW, optional)** | `sidequest/signatures/read_4` when 4/4 | HUD flourish; optional Founder pre-battle `any_tags` fanout line |
| `fields_liberated_gte_4` (band tag, EXISTS) | wheat-war tick (EXISTS) | Odei `recognize` entry |

**Scores:** none new required. `#board quest_hud` (render scratch) EXISTS and already counts
the four `defeated_board_*` tags — venue-independent, works unchanged for the scattered hunt.
`reveal/board_fell` keeps that recount block verbatim; only its four title lines are rewritten.

---

## 6. Build checklist

1. **Extend 5 existing character files** (add one `placement` key each; the Founder's carries
   `spawn_gate`): `board_madeline.json` `[1565 65 1732]`, `board_micah.json` `[2310 65 3538]`,
   `board_matt.json` `[4060 66 2774]`, `board_lauren.json` `[947 66 2651]`,
   `villain_final_boss.json` `[1590 51 1028]` + `spawn_gate:{tag:royal_league_champion}`. No
   other changes to these files. (If the Open-Q1 fork rules SCATTERED, the seated flavor lines
   in the four Board dialogs get a separate light rewrite pass — not this file's paste blocks.)
2. **Drop 4 NEW pointer character files** under `dialog-src/characters/villain/`:
   `hunt_clerk_fenceline.json`, `hunt_keeper_odei.json`, `hunt_bystander_factory.json`,
   `hunt_pilgrim_wren.json` (all `civilian`, stationary, `placement` stubs). Ids checked
   distinct from the existing `granary_keeper.json` (Feng) at Hua Zhan.
3. **Drop 4 NEW dialog files** under `dialog-src/dialog/`: `hunt_clerk_fenceline.json`,
   `hunt_keeper_odei.json`, `hunt_bystander_factory.json`, `hunt_pilgrim_wren.json`.
4. **Add register stages** to `dialog-src/registers/quest_targets.json`: four new side holders
   `q.side_board_{vote,ledger,position,story}` (slots **54/53/52/51** — verified free; slots
   57/58 that an earlier draft used are TAKEN by q.side_clinic / q.side_manifest), plus the
   optional `q.side_signatures` (slot **50**). Update the existing `q.main`
   `Hunt the Board of Directors` note (keep its `target: null`; the side holders carry the
   waypoints).
5. **Functions:**
   - `function/reveal/board_fell.mcfunction` — **REWRITE** to the four venue-neutral beats
     (§3.5 table). Keep the recount block; swap only the four `matches N` title lines. All
     strings apostrophe-free.
   - `function/reveal/founder_spawn.mcfunction` — ONLY if `spawn_gate` is not added to
     `content_compile` (Open Q3). Champion-gated import latch for the Founder body.
   - `function/sidequest/signatures/read_1..4.mcfunction` + `load.mcfunction` — ONLY if
     shipping the optional collectible (§3.6).
   - **No other functions needed** — `reveal/founder_defeated`, the `#board` recount in
     `quest/render`, and the Board/Founder onwin latches all EXIST unchanged.
6. **Verify the rctmod team files** exist / are non-empty (TBCS reads these):
   `data/rctmod/trainers/board_{madeline,matt,micah,lauren}.json` and
   `villain_final_boss.json`. **CONFIRMED present** (`board_madeline.json` is a full 82–87
   team). **Not this unit's authoring surface** — flagged so the battles do not silently no-op.
7. **Compile:** `scripts/content_compile` → `scripts/update_preset_index` →
   `scripts/generate_npc_function`. Then the rctmod cycle check (roadmap §8). Then
   `gradle build`; bump the alpha suffix.
8. **Verify:** each Board `placement` latch spawns once per world; each pointer NPC appears
   only post-Champion and points at its Board member; each Board defeat advances `#board` and
   fires the rewritten `board_fell` beat; 4/4 flips `q.main` to Face The Founder + spawns the
   Founder body; Founder win prints the player's live name and flips to Hunt the Ender Dragon.

---

## 7. Open questions for showrunner

1. **SCATTERED vs SEATED Board — the load-bearing design fork (and the shipped dialog leans
   SEATED).** This unit's brief specifies the Board **scattered across four thematic sites** (a
   map-wide victory-lap hunt). Roadmap `14_board_and_founder.md` (showrunner rulings
   2026-07-06) seats all four **in the HQ tower boardroom** as a linear elevator-ascent
   elimination, Founder in the penthouse. **These are mutually exclusive placements**, and the
   tension is not cosmetic: the **shipped Board and Founder dialog is written seated** — the
   Board `default`/`after` lines say *sit at the door of your own boardroom*, *the head of the
   table*, *there is one chair left*; the Founder says *you climbed the whole tower*, *walk back
   up the stairs*, *the table is not empty*. Everything mechanical (battle wiring, `#board`
   scratch, cap wiring, the REWRITTEN venue-neutral `board_fell` beats) is venue-independent —
   only the four `placement` coords and the **seated flavor lines** differ. **Ruling needed:**
   ship the scattered hunt (this unit — a cross-map lap that re-uses liberated sites and adds
   four recognition set-pieces, but requires a light rewrite of the four Board dialogs' seated
   flavor so a granary/branch/factory/shrine does not narrate a boardroom) or the tower ascent
   (roadmap — a tight corridor climax the shipped dialog already fits)? If the tower wins,
   discard the four `placement` stubs + four pointer NPCs here and use roadmap §3's boardroom
   coords; the four `q.side_board_*` waypoint holders still apply (pointing into the tower), and
   the rewritten venue-neutral `board_fell` beats improve the seated version too. *(I authored
   the scattered version per the brief and made the reveal beats venue-neutral so the rewrite is
   not wasted whichever way the fork rules. The one honest cost of scattered is the seated Board
   flavor lines needing a pass.)*
2. **Fight order enforcement.** The pointer chain routes the player Madeline → Micah → Matt →
   Lauren (Lauren's team hardest). Order is currently player's choice (all four gated only on
   `champion:true`). Enforce a soft order (each site's pointer teases the next) or leave fully
   open? *(Recommend open — the sidebar shows all four; the flavor suggests a route without
   locking it.)*
3. **`spawn_gate` schema extension.** The Founder must NOT exist pre-Champion. The cleanest fix
   is a `spawn_gate` field on `placement` in `content_compile` (§3.5). Build it, or ship the
   hand-authored `founder_spawn.mcfunction` latch instead? *(Recommend `spawn_gate` — reusable
   for any future champion-gated body; the Founder is the only current consumer.)*
4. **Board cap tuning (the last legal cruelty).** The shipped rctmod teams span level **82–87**
   while the cap is 85 — so the Board top out two above the entry cap, in the underleveled
   spirit of the run. Confirm this is intended, or retune the top members down to ace-86 to
   match the roadmap-cited +2 rule. *(The battles are forced `gauntlet_boss` no-decline
   set-pieces; the fairness floor holds because a starter-only player cannot reach Act 3. Flag
   only so the tuning is a deliberate choice, not a stale team.)*
5. **Pointer NPC bodies.** The four pointer civilians use `placement` (latch-spawn). Confirm
   builder does not want them as hand-placed `uuid` bodies at specific set-dressing (the dark
   office desk, the granary silo, the Factory doors, the shrine steps). *(Recommend
   `placement` — they are simple stationary pointers; builder-place only if the set dressing
   needs exact anchoring.)*
6. **The Four Signatures collectible (§3.6).** Ship the optional scrubbed-page collectible, or
   cut it? It adds texture (the cover-up assembled before the reveal) at the cost of four props
   + four tiny read functions. *(Recommend ship — cheap, on-theme, opt-in, no dead-end; the
   scrubbing-artifact litter is explicit LORE_BIBLE §9 guidance.)*
7. **Coord precision.** All four site coords are zone **centroids** — good for a waypoint, but
   the Board body should sit on a specific feature (the dark office desk, a granary silo, the
   Factory outer gate, the shrine's outer stone). Builder to confirm exact per-site anchors
   during placement.
