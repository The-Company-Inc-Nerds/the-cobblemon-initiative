# Quest Unit 10 — Act 2: The HQ Raid → Acting CEO DJ

`slug: 10_hq_raid`

> Implementation-ready plan. This is a **design doc**, not code. Nothing here has been
> written into `dialog-src/`, `src/`, or the datapack — copy the JSON blocks out of here
> when building. Read alongside `dialog-src/schema/README.md` and `docs/LORE_BIBLE.md §4`.

---

## 1. Overview

**The set-piece.** Company HQ at `[1590 51 1028]`, hard against the eastern edge of Cyber
City (gym 7). It is the tower everyone in the region has felt in their wallet but never
walked into. The building is the physical body of the Wheat War economy — the desk every
occupied field clears through (Acting CEO DJ's own line: *every wheat field between here
and the coast clears through my desk*). Zone `The Company, Inc.` (`dev/updated-zones.json`,
type `VILLAIN`, `mobsSpawn:false`, `announce:true`) already covers the footprint.

**Band context (the moment on the route).**

| Axis | Value at raid entry |
|------|--------------------|
| Gyms beaten (`memory_fragment`) | **7** (frag_7 — *You signed this charter*) |
| Level cap | **62** (Cyber, gym 7) |
| `cd_instability` | **56** — the PEAK of Act 1 (money feels most broken here) |
| Recognition band | **late** (`memory_fragment gte 7`) |
| Gate to enter the fight | `memory_fragment gte 7` **AND** `fields_liberated gte 4` |

**The arc job.** This is the **Act-2 climax** and the single biggest earned beat before the
Royal League. It does four things:
1. **Pays off the field campaign.** DJ *refuses the meeting* until 4 fields are liberated
   — *the monopoly holds; starve it first*. The player already learned this from DJ's own
   `monopoly_holds` line (shipped in `acting_ceo_dj.json`) and from the main-line sidebar
   stage *Liberate wheat fields, then raid HQ*.
2. **Runs the recognition arc to its absolute peak.** Four fights up a ladder, each rung a
   higher rank who knew the founder better and breaks harder: Shade (half-recognises) →
   Vex (*a line item we zeroed out years ago; I balanced that column myself*) → Noir
   (*I knew it from the gait on the security feed three floors down*) → DJ (*so the rumour
   walks*). The three management dialogs already ship this recognition ladder verbatim;
   this unit adds the **floor-by-floor discovery structure** and the **scrubbing-artifact
   set-pieces** between them.
3. **Stabilises the currency.** DJ's defeat clamps `cd_instability` to 25 and fires the
   streamable **CURRENCY STABILIZED** title card. This is already wired
   (`economy/hq_stabilize` → `economy/stabilized`); this unit ties it to the floor beats.
4. **Plants the next hook.** Post-DJ, Volt/Cyber already tease the Stadium; the raid exit
   points the player at Ryujin Keep (gym 8) and the League.

**Place on the route.** After gym 7 (Cyber / Volt) and after ≥4 field liberations.
Clearing it opens the gym-8–10 stretch at `cd_instability 25`. It is the last villain
content before the Royal League; the Board and The Founder are Act 3.

**Dark Urge tier 3 — NOT a raid trigger (engine fact, corrected).** The Dark-Urge whisper
tier is **derived from the current level cap**, not from a scoreboard or a tag. `NuzlockeConfig`
sets `darkUrgeTier3LevelCap = 73` — tier 3 unlocks the moment the cap reaches 73, which is
**gym 8 (Ryujin Keep)**, one gym *after* this raid. So the raid does not (and cannot) flip
Dark Urge to tier 3; there is nothing to latch here. This matches LORE_BIBLE §7 (*tier 3
only post-gym-8*). The narrative pairing still lands on stream — the currency stabilises at
DJ, and the colder inner voice deepens one gym later as the cap climbs — but it is two
separate, already-wired systems, and **this unit adds no Dark-Urge plumbing.**

**What already exists (do NOT duplicate — EXTEND).**
- `dialog-src/characters/villain/acting_ceo_dj.json` — DJ, the throne fight + `monopoly_holds`
  refusal + `hq_stabilize` onwin + `despawn_on_win`. **Needs:** placement anchor only, plus
  the optional raid-entry cutscene hook (see §3.6).
- `.../villain_admin.json` (Shade), `.../villain_admin_2.json` (Vex),
  `.../villain_admin_commander.json` (Noir) — all three ship full recognition dialogs +
  management battles + `despawn_on_win`. **Needs:** placement anchors inside HQ, and a
  floor-discovery latch appended to each management dialog (a button that reads the floor's
  scrubbing artifact and sets `hq_floor_N` — §3).
- `economy/hq_stabilize.mcfunction`, `economy/stabilized.mcfunction` — the stabilization
  beat. Reused verbatim, **unchanged**.
- `registers/scrubbing.json` — `artifacts` / `the_memo` / `veterans` / `retired_to_never`
  entries. The floor discoveries pull tone from here.
- `registers/economy.json` — `reassurance` (Act 2) and `corrupted` (post-HQ) entries, plus
  the `instability` tier gated `cd_instability gte 56`.

**This unit adds:** 1 new NPC (a lobby receptionist / rumor-hub), 1 new dialog tree, 3
scrubbing-artifact discovery announces, an optional raid-entry cutscene, and the SIDE
quest_targets holder for the floor-by-floor climb (the main line already handles *Raid
Company HQ*).

---

## 2. Cast

HQ is a vertical dungeon: lobby → floor 1 (Shade) → floor 2 (Vex) → floor 3 (Noir) →
throne (DJ). Placement anchors are **inside the tower footprint** (zone centred near
`x≈1590 z≈1028`, `centerY 64`, but the raid climbs, so Y steps up per floor). Coordinates
below are **PLACEHOLDER-BUILD** stubs consistent with the `[1590 51 1028]` canon anchor and
a 4-floor stack — the world-builder must confirm each against the actual interior and drop
the confirmed coord into each character's `placement`.

| id | display_name | role | concept | placement anchor (PLACEHOLDER) |
|----|-------------|------|---------|-------------------------------|
| `hq_receptionist` | Front Desk (Company) | `civilian` | Lobby greeter. A civilian clerk who has drunk the propaganda; NEVER recognises the founder, only feels the money curdling. Points the player up the tower / at the refusal. Rumor-hub for the raid. | `1590, 51, 1020` (lobby, just inside the doors) |
| `villain_admin` (Shade) | Regional Manager Shade | `villain_management` | **Floor 1.** Half-recognises you, per-policy blocks the stairs. *(exists — add placement)* | `1590, 55, 1028` (floor-1 landing) |
| `villain_admin_2` (Vex) | Senior Director Vex | `villain_management` | **Floor 2.** *You are supposed to be dead… a line item we zeroed out years ago.* *(exists — add placement)* | `1590, 60, 1028` (floor-2 operations centre) |
| `villain_admin_commander` (Noir) | COO Noir | `villain_management` | **Floor 3.** *I knew it from the gait on the security feed.* The last door. *(exists — add placement)* | `1590, 65, 1028` (floor-3, before the throne door) |
| `acting_ceo_dj` (DJ) | Acting CEO DJ | `villain_boss` | **Throne.** The usurper keeping the chair warm. Refuses below the gate, fights above it, despawns on defeat. *(exists — add placement + optional cutscene hook)* | `1590, 70, 1028` (throne floor, behind the desk) |

> Only **`hq_receptionist` is a new character file.** The four villains already exist and
> only need a `placement` block (they are currently unplaced `[0,0,0]`) plus the small
> dialog extensions in §3. The trainer JSONs (`villain_admin`, `villain_admin_2`,
> `villain_admin_commander`, `villain_boss`) all exist under
> `src/main/resources/data/rctmod/trainers/`.

---

## 3. Quests

The raid is **one continuous quest** with five beats. The main sidebar line (`q.main`)
already carries *Raid Company HQ [1590 51 1028]*; the SIDE holder `q.side_raid` (§3.7)
steps the marker floor-by-floor **once inside**, so the player is never staring at a stale
building anchor mid-climb. Each management fight already ships its recognition dialog — the
new authoring here is: (a) the lobby/gate beat, (b) a **floor-discovery button** appended
to each management dialog that reveals that floor's scrubbing artifact and latches
`hq_floor_N`, (c) the DJ throne stabilization beat (mostly wired), (d) the raid-entry
cutscene.

**Cap-legality (READ FIRST).** DJ's team tops at **level 64** (`villain_boss.json`; team is
59→64), i.e. **entry-cap+2** (cap 62 at gym 7) — exactly the ladder-canon *ace at
entry-cap+2* spirit, fought underleveled like a gym leader. The three management fights sit
below that. All four are **boss-type villain fights that already despawn on win** and are
the canon Act-2 climax — they are the intended difficulty spike, not an opt-in wager, so
they run at their authored levels. The **fairness floor still applies**: the raid must not
force a whiteout on a player with no caught Pokémon. Two safeguards already cover this:
(1) DJ's `monopoly_holds` refusal fail-softs anyone below the field gate, and (2) **no fight
in the raid is a forced `ON_DISTANCE_VERY_CLOSE` battle** — every management fight and DJ
are **opt-in dialog buttons** the player chooses to press (with a `leave_button` on every
entry), so a starter-only player is never dropped into an unavoidable whiteout. No
decline_fee on the boss ladder (this is the story climax, not a route toll) — the lobby and
every floor let you leave freely before you commit.

---

### 3.1 The Refusal & The Ascent (entrance beat)

**Concept.** The player walks into the HQ lobby. Two states: **below the gate** (badges <7
OR fields <4) the desk stonewalls and DJ upstairs refuses; **at the gate** (badges ≥7 AND
fields ≥4) the desk is nervous, the propaganda is glitching, and the stairs are open. This
is the opt-in commit point for the whole raid, and the **rumor-hub** for it.

**Forward hook:** names the climb — *four floors, four signatures, and the man in your
chair at the top*. **Back-echo:** references the liberated fields — *the fields stopped
answering our memos; that is why the lights up there are flickering*.

**Character — `hq_receptionist` (NEW).**

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "hq_receptionist",
  "display_name": "Front Desk (Company)",
  "role": "civilian",
  "act": "2",
  "location": "Company HQ - Lobby",
  "recognition_tier": "late",
  "recipe": "civilian",
  "dialog": "dialog:hq_lobby_desk",
  "movement": {
    "objective": "ambient_stationary_look"
  },
  "placement": { "x": 1590, "y": 51, "z": 1020 }
}
```

**Dialog — `hq_lobby_desk` (NEW).** STANDARD; the ascent gate is line-selection only (the
real fight gate is the management NPCs). A civilian NEVER recognises the founder — this desk
only feels the money breaking, per canon.

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "hq_lobby_desk",
  "type": "STANDARD",
  "entries": [
    {
      "label": "raided",
      "name": "Front Desk - after the raid",
      "priority": 40,
      "gate": { "defeated": "villain_boss" },
      "say": [
        "The elevators run again. The numbers on the board stopped shaking. I do not know what you did up there and I have decided not to ask.",
        "Everyone downstairs is pretending it was always this calm. That is the job. I am good at the job."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Take care of yourself", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "open",
      "name": "Front Desk - the stairs are open",
      "priority": 30,
      "gate": { "fields_liberated": { "op": "gte", "value": 4 }, "badges": { "op": "gte", "value": 7 } },
      "say": [
        "You are not on the visitor log and I am not going to add you. Four floors of people upstairs are having a very bad morning and I would rather you were their problem than mine.",
        "The fields stopped answering our memos, so the acting CEO will actually see you now. He is at the top. He has your chair. Go up, and mind the ones who go quiet when they see your face."
      ],
      "buttons": [
        { "label": "ascend_button", "text": "Take the stairs", "actions": [ { "do": "command", "cmd": "tag @initiator add hq_raid_active", "as_player": true }, { "do": "close" } ] },
        { "label": "leave_button", "text": "Not yet", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Front Desk - restructuring in progress",
      "priority": 10,
      "say": [
        "Welcome to the Company. All meetings above this floor are by verified appointment, and the acting CEO is not taking new business. Prices are simply adjusting. That is normal. That is healthy.",
        "The wheat fields feed this building. While the grain still clears through his desk, there is nothing up there for you but a locked elevator and a man who will not blink."
      ],
      "say_ref": "register:economy#reassurance",
      "buttons": [
        { "label": "leave_button", "text": "I will take the fields first", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:**
- *(none required for the desk)* — `hq_raid_active` is a plain tag latched by the ascend
  button; the SIDE quest_targets holder reads it (§3.7). If the showrunner wants a **RAID
  BEGUN** card + cutscene on ascend, add `function cobblemon_initiative:hq/raid_begun` to
  the ascend button (spec in §3.6). That is the only optional function here.

**QUEST_TARGETS entry:** the lobby is covered by the main line (*Raid Company HQ*) and the
new SIDE holder in §3.7 — no separate stage for the desk.

**REWARD/BALANCE:** no battle, no prize. Pure gate + narration. Fully fail-soft: leaving
costs nothing; the fight gates are on the management NPCs.

---

### 3.2 Floor 1 — Regional Manager Shade (EXTEND existing)

**Concept.** First rung. Shade half-recognises the founder (*you stand like someone who
used to own a chair like mine*) and per-policy blocks the stairs. Fight already ships. The
new authoring is the **scrubbing-artifact discovery** on this floor: a **brighter rectangle
where a portrait hung**, latched `hq_floor_1`, readable after Shade falls.

**Forward hook:** Shade's `after` entry already says *up the ladder, the way you always
did… I will tell the next floor you are coming* → points at Vex. **Back-echo:** the discovery
references the public scrubbing the player has seen on the routes (`scrubbing#artifacts`).

**Character:** `villain_admin.json` **exists.** Only change: add a `placement` block and
append the discovery button to its inline dialog `after` entry.

```json
// ADD to villain_admin.json (top level, alongside "battle"):
"placement": { "x": 1590, "y": 55, "z": 1028 }
```

The artifact is delivered with the schema's **`announce` action** (§3.5), not a raw
`tellraw` — the compiler emits the correctly-quoted command, and the copy carries **no
apostrophe and no double-quote**, so it is safe through the macro layer.

```json
// ADD as a second button in the villain_admin_inline "after" entry buttons[]:
{
  "label": "artifact_button",
  "text": "Look at the wall behind the desk",
  "gate": { "not_tag": "hq_floor_1" },
  "actions": [
    { "do": "command", "cmd": "tag @initiator add hq_floor_1", "as_player": true },
    { "do": "announce", "as": "chat", "color": "gray", "text": "[HQ] A brighter rectangle of paint marks the wall behind the desk. Something large hung here for a long time. There is no plaque, no name - only the clean square where a face used to be." }
  ]
}
```

**DATAPACK NEEDS:**
- *(none new)* — the button latches `hq_floor_1` with a plain `tag` command and delivers the
  line via `{do:announce}`; the compiler builds the quoted `tellraw`. The `not_tag` gate on
  the button makes it one-shot (it locks after the first read). If the showrunner wants a
  page-turn sound sting, add a second `{do:command, cmd:"playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 0.9", as_player:true}` — no apostrophes, safe.

**QUEST_TARGETS:** covered by the `q.side_raid` floor stage (§3.7, `hq_floor_1` not-set →
point at Shade).

**REWARD/BALANCE:** Shade's fight is unchanged — `villain_management`, `GEN_9_DOUBLES`,
prize 3000, `despawn_on_win`, below cap, opt-in (dialog button + `leave_button`). The
artifact is flavor + a latch, no reward.

---

### 3.3 Floor 2 — Senior Director Vex (EXTEND existing)

**Concept.** Second rung, recognition sharpens to alarm: the shipped line is *you are
supposed to be dead… a line item we zeroed out years ago. I balanced that column myself*.
Fight ships. Discovery: **re-verified ledgers** — pages with the second signature scratched
out and re-signed under a new name (`scrubbing#the_memo` / `#artifacts` tone).

**Forward hook:** Vex's `after` already says *go to the top floor… tell DJ the dead came
back to read his numbers* → points past Noir at DJ. **Back-echo:** the ledger discovery
echoes the double-signed-ledger lore the player met in Act 1 (nether-star reserves,
double signatures — LORE_BIBLE §3).

```json
// ADD to villain_admin_2.json (top level):
"placement": { "x": 1590, "y": 60, "z": 1028 }
```

```json
// ADD as a second button in villain_admin_2_inline "after" entry buttons[]:
{
  "label": "artifact_button",
  "text": "Read the open ledger",
  "gate": { "not_tag": "hq_floor_2" },
  "actions": [
    { "do": "command", "cmd": "tag @initiator add hq_floor_2", "as_player": true },
    { "do": "announce", "as": "chat", "color": "gray", "text": "[HQ] Every reserve page has two signature lines. On every one, the second name has been scraped away and re-signed in a fresh hand. The Company says it was always one signature. The scraped grooves say otherwise." }
  ]
}
```

**DATAPACK NEEDS:**
- *(none new)* — same pattern as floor 1: `tag` latch + `{do:announce}`. One-shot via the
  button's `not_tag` gate.

**QUEST_TARGETS:** `q.side_raid` floor stage (§3.7).

**REWARD/BALANCE:** Vex unchanged — prize 4500, `GEN_9_DOUBLES`, `despawn_on_win`, below cap,
opt-in (dialog button + `leave_button`).

---

### 3.4 Floor 3 — COO Noir (EXTEND existing)

**Concept.** Third rung, peak recognition from a subordinate: Noir knew you from the *gait
on the security feed three floors down* and stands the last door anyway — discipline, not
loyalty. Fight ships. Discovery: **revised org charts + impostor memos** — the chart with
the top box whited out, and DJ's own memo warning of *a face from the old company*
(`scrubbing#the_memo`).

**Forward hook:** Noir's `after` already says *the last door is behind you. DJ is through
there, keeping your chair warm* → the throne. **Back-echo:** the impostor memo echoes the
grunt recognition from Act 1 (*a saboteur working the routes… a description we were told to
forget* — `scrubbing#the_memo`).

```json
// ADD to villain_admin_commander.json (top level):
"placement": { "x": 1590, "y": 65, "z": 1028 }
```

```json
// ADD as a second button in villain_admin_commander_inline "after" entry buttons[]:
{
  "label": "artifact_button",
  "text": "Pull the memo off the org chart",
  "gate": { "not_tag": "hq_floor_3" },
  "actions": [
    { "do": "command", "cmd": "tag @initiator add hq_floor_3", "as_player": true },
    { "do": "announce", "as": "chat", "color": "gray", "text": "[HQ] The org chart on the wall has one box painted over at the very top - the box every other line still points up toward. A memo is pinned across it, signed DJ: watch the routes for a face from the old company. Do not engage. Do not name it. File it." }
  ]
}
```

**DATAPACK NEEDS:**
- *(none new)* — same `tag` + `{do:announce}` pattern; one-shot via the `not_tag` gate. The
  announce copy is apostrophe-free (the possessive *Noir's wall* was rewritten to *the wall*)
  so it is safe through the macro/announce layer.

**QUEST_TARGETS:** `q.side_raid` floor stage (§3.7).

**REWARD/BALANCE:** Noir unchanged — prize 6000, `GEN_9_SINGLES`, `despawn_on_win`, below cap,
opt-in (dialog button + `leave_button`).

---

### 3.5 The Throne — Acting CEO DJ & CURRENCY STABILIZED (EXTEND existing)

**Concept.** The climax. Below the field gate DJ **refuses** (the shipped `monopoly_holds`
entry — *come back when the fields stop answering our memos*). At the gate he fights (the
shipped `default` entry, gated `fields_liberated gte 4`). His defeat: `defeated_villain_boss`
+ `hq_stabilize` clamps `cd_instability` to 25 + the **CURRENCY STABILIZED** title card +
DJ despawns (`easy_npc delete @2` via `despawn_on_win`).

**Forward hook:** DJ's win_line (*it was always going to be yours again*) + the stabilization
title turn the player toward the League; Volt already teased the Stadium at gym 7.
**Back-echo:** DJ's opening (*back from wherever the board filed you*) and `hq_stabilize`
resolving the `cd_instability` climb the player has felt in every payout since gym 1 — the
plot they could feel in their wallet, paid off.

**Character:** `acting_ceo_dj.json` **exists** — dialog, battle, `hq_stabilize` onwin, and
`despawn_on_win` all shipped. **The only required addition is `placement`.** The
stabilization is entirely done; do not touch `hq_stabilize` for this unit (see the Dark-Urge
note below).

```json
// ADD to acting_ceo_dj.json (top level, alongside "battle"):
"placement": { "x": 1590, "y": 70, "z": 1028 }
```

**Dark Urge tier 3 — do NOT wire it here (engine fact).** Earlier drafts of this unit tried
to flip Dark Urge to tier 3 inside `hq_stabilize` via a `dark_urge_tier` scoreboard. That
primitive **does not exist**. The tier is computed from the **level cap** in
`config/NuzlockeConfig` (`darkUrgeTier3LevelCap = 73`), so tier 3 unlocks automatically when
the cap reaches 73 — at **gym 8 (Ryujin Keep)**, one gym after this raid — with no tag, no
score, and no function to author. LORE_BIBLE §7 says the same (*tier 3 only post-gym-8*).
Leave `hq_stabilize` exactly as shipped; the currency stabilises at DJ and the whisper tier
deepens one gym later, both from systems already in place.

Optionally, to make the throne moment cinematic, add a raid-victory cutscene hook to DJ's
`on_win` (§3.6). The current `on_win` is just `function …/economy/hq_stabilize`.

**DATAPACK NEEDS:**
- *(none new)* — `economy/hq_stabilize` → `economy/stabilized` already deliver the
  **CURRENCY STABILIZED** title + actionbar + beacon sound, downward-guarded and idempotent
  under the reward/onwin double-fire. Reused verbatim.

**QUEST_TARGETS:** the throne is the top of the `q.side_raid` climb (§3.7) — point at DJ
while `hq_floor_3` / `defeated_villain_admin_commander` is set and `defeated_villain_boss` is
not; the whole side line moots once DJ falls (the main line advances to *Challenge the Royal
League*).

**REWARD/BALANCE:** DJ — `gauntlet_boss`, `GEN_9_SINGLES`, prize **8000**, `despawn_on_win`.
Team is 59→64, topping at **level 64 = entry-cap+2** (cap 62) — cap-legal, the intended
story-climax spike, matching the ladder-canon *ace at entry-cap+2*. No decline/loss fee (the
`monopoly_holds` refusal below the gate is the fail-soft; above the gate this is the
committed, opt-in climax the player chooses via *Take back the chair*).

---

### 3.6 Raid-entry cutscene (WISHLIST → spec)

**Concept.** A short establishing shot when the player commits to the climb — the camera
pulls up the HQ facade / the flickering exchange board, holds on the whited-out portrait
square in the lobby, then hands control back at the foot of the stairs. Matches the shipped
gym-leader `intro_scene` / `blossom_reveal` pattern (cutscenes play via `cutscene play <id>`
from a function; see `function/hua_zhan/aya_transform.mcfunction`).

**How it hangs off the existing rig:**
- New cutscene script `cutscenes/hq_raid_entry.json` (author with the in-game
  `/cutscene record` tools like the other scenes; keyframes are a build-time task).
- New `function/hq/raid_begun.mcfunction`. Title text is delivered as text components; note
  the sting has **no apostrophes or double-quotes in author-facing copy** (the JSON quoting
  here is the component syntax, produced by the compiler if authored via `{do:announce,
  as:title}` instead — prefer that path):
  ```
  # Raid begun - title sting + establishing cutscene. One-shot.
  execute unless entity @s[tag=hq_raid_seen] run title @s times 10 60 20
  execute unless entity @s[tag=hq_raid_seen] run title @s subtitle {"text":"Four floors. Four signatures. The chair at the top.","color":"gray"}
  execute unless entity @s[tag=hq_raid_seen] run title @s title {"text":"COMPANY HQ","color":"#455A64","bold":true}
  execute unless entity @s[tag=hq_raid_seen] run cutscene play hq_raid_entry
  tag @s add hq_raid_seen
  ```
- Wire it into the lobby `ascend_button` (§3.1): add
  `{ "do": "command", "cmd": "function cobblemon_initiative:hq/raid_begun", "as_player": true }`
  before the `close`. The `hq_raid_seen` guard inside the function makes it one-shot so
  re-entering the lobby does not replay it.

**Showrunner ruling needed:** is the entry cutscene in-scope for this unit, or deferred? It
is fully optional — the raid plays without it. Flagged in Open Questions.

---

### 3.7 SIDE quest_targets holder — the floor-by-floor climb

The main line (`q.main`) already carries *Raid Company HQ [1590 51 1028]* (badges ≥7,
fields ≥4, not `defeated_villain_boss`) and, below the field gate, *Liberate wheat fields,
then raid HQ*. This new **side** holder `q.side_raid` steps the waypoint through the four
floors **once the player is inside** (`hq_raid_active` set by the lobby ascend button), so
the marker climbs with them instead of re-pointing at the building anchor. It self-moots
when DJ falls.

Insert this block into `registers/quest_targets.json` `quests[]`. **Slot check (done):**
the side band 57–81 is fully contiguous and taken (57=`q.side_ledger`-ish … 70=`q.side_bones`;
64 is `q.side_sprint`, 65 is `q.side_watch` — the coords in earlier drafts that used 64/65
were **collisions**), and 100 is the champion/main slot. The first genuinely free slot above
the block is **`slot: 82`**; use it and re-confirm no collision at build time (a fresh
`content_compile` will error on a duplicate slot). First-match-wins → highest floor first.

```json
{
  "holder": "q.side_raid",
  "name": "The HQ Raid",
  "slot": 82,
  "note": "Floor-by-floor climb marker, live only while inside HQ (hq_raid_active set by the lobby ascend button, mooted when defeated_villain_boss lands and q.main advances to the Royal League). First-match-wins, top floor first: DJ -> Noir -> Vex -> Shade. Villain placements are the floor anchors in docs/QUESTS/10_hq_raid.md; confirm against the built interior. Slot 82 is the first free value above the 57-81 side band; re-verify no collision at compile.",
  "stages": [
    {
      "if_tags": [ "hq_raid_active", "defeated_villain_admin_commander" ],
      "not_tags": [ "defeated_villain_boss" ],
      "label": "The throne - Acting CEO DJ",
      "target": { "npc": "acting_ceo_dj" }
    },
    {
      "if_tags": [ "hq_raid_active", "defeated_villain_admin_2" ],
      "not_tags": [ "defeated_villain_admin_commander" ],
      "label": "Floor 3 - COO Noir holds the last door",
      "target": { "npc": "villain_admin_commander" }
    },
    {
      "if_tags": [ "hq_raid_active", "defeated_villain_admin" ],
      "not_tags": [ "defeated_villain_admin_2" ],
      "label": "Floor 2 - Senior Director Vex",
      "target": { "npc": "villain_admin_2" }
    },
    {
      "if_tags": [ "hq_raid_active" ],
      "not_tags": [ "defeated_villain_admin" ],
      "label": "Floor 1 - Regional Manager Shade",
      "target": { "npc": "villain_admin" }
    }
  ]
}
```

> The `target:{npc:…}` resolutions depend on the four villains having `placement` blocks
> (§3.2–3.5). Until those land, `target` falls back to `[0,0,0]` — placement is a hard
> prerequisite for this holder to point anywhere useful (memory: *placement fixes quest
> waypoints for latch-placed NPCs*).

**REWARD/BALANCE:** sidebar only, no reward. No dead-end line: every stage has a live
target or is superseded when DJ falls (the main line takes over with *Challenge the Royal
League*).

---

## 4. Recognition & economy beats

**Recognition — the ladder is the arc, peaking here (band = late, `memory_fragment gte 7`).**
The three management NPCs already ship the escalation and are reused verbatim; this is the
canonical high point of the whole recognition gradient (rank × proximity, LORE_BIBLE §4).
Lines below are the **shipped** copy:

- **Shade (floor 1):** *you stand like someone who used to own a chair like mine.* Alarmed
  half-recognition; still follows policy. (Entry gated `recognition: mid`.)
- **Vex (floor 2):** *you are supposed to be dead… a line item we zeroed out years ago. I
  balanced that column myself.* The veteran who did the erasing, watching it undo.
- **Noir (floor 3):** *I knew it from the gait on the security feed three floors down… I
  have no questions about who you are.* A subordinate who **stands down in spirit** but
  follows the last order.
- **DJ (throne):** *so the rumour walks. The founder, back from wherever the board filed
  you.* The usurper, forced to name the man he replaced.

The **scrubbing artifacts** (§3.2–3.4) make the erasure physically visible on-camera between
fights — the bright portrait square, the re-verified ledgers, the whited-out org-chart box +
impostor memo — exactly the litter the LORE_BIBLE mandates for reading the cover-up before
the reveal. All pull tone from `registers/scrubbing.json` (`artifacts`, `veterans`,
`the_memo`, `retired_to_never`).

**Economy voice — the PEAK-to-stabilised swing.** The raid straddles the loudest economy
beat in the game:
- **Before DJ (`cd_instability 56`, Act-2 slipping):** the lobby desk speaks `economy#reassurance`
  (*prices are simply adjusting… the CobbleDollar is fine*) and the exchange board flickers
  ashamed of the numbers (`economy#instability`, tier gated `cd_instability gte 56`, already
  ships those lines). This is nervous, over-explaining Company cheer at its most brittle.
- **On DJ's defeat:** `hq_stabilize` clamps to **25** and fires **CURRENCY STABILIZED** — the
  single biggest streamable economy receipt in Act 2 (green title, beacon sound, actionbar).
  Field liberation can have pushed the player below 25 already; the clamp is downward-only
  (guarded `matches 26..`) so it never raises an earned index.
- **After DJ (late/exposed):** the propaganda **corrupts** — `economy#corrupted` (*we told
  them the founder retired… there was never a founder*) and the `scrubbing#retired_to_never`
  tone. The lobby desk's `raided` entry (§3.1) delivers the civilian version: calm restored,
  everyone pretending it was always calm.

Civilians (the lobby desk) **never recognise the founder** — the desk only ever reacts to
the money and the mood, per canon.

---

## 5. New tags/scores introduced

| tag / score | set by | gated / read by |
|-------------|--------|-----------------|
| `hq_raid_active` | lobby `ascend_button` (§3.1) `tag @initiator add` | `q.side_raid` floor stages (§3.7) — only shows the climb marker once inside |
| `hq_raid_seen` | `hq/raid_begun.mcfunction` one-shot guard (§3.6) | itself (prevents cutscene replay) — only if the cutscene ships |
| `hq_floor_1` | floor-1 discovery button (§3.2) `tag @initiator add` | the floor-1 discovery button `not_tag` (locks it after one read) |
| `hq_floor_2` | floor-2 discovery button (§3.3) `tag @initiator add` | the floor-2 discovery button `not_tag` |
| `hq_floor_3` | floor-3 discovery button (§3.4) `tag @initiator add` | the floor-3 discovery button `not_tag` |

> **No `dark_urge_tier` tag/score is introduced** — corrected from an earlier draft. The
> Dark-Urge tier is derived from the level cap in `NuzlockeConfig` (tier 3 at cap ≥73, i.e.
> gym 8), so the raid touches no Dark-Urge primitive.

**Already-existing, reused (no new authoring):** `defeated_villain_admin`,
`defeated_villain_admin_2`, `defeated_villain_admin_commander`, `defeated_villain_boss`
(battle-win latches); `cd_instability`, `memory_fragment`, `fields_liberated` (scoreboards).

---

## 6. Build checklist

Ordered so each step compiles/validates before the next.

1. **Confirm interior anchors.** World-builder walks HQ and replaces the PLACEHOLDER floor
   coords in §2 (lobby, floors 1–3, throne) with real interior coordinates.
2. **Drop 1 new character file:** `dialog-src/characters/villain/hq_receptionist.json`
   (§3.1). *(Consider `characters/cyber/` if HQ is filed under Cyber City — match the repo's
   area-folder convention at build time.)*
3. **Drop 1 new dialog file:** `dialog-src/dialog/hq_lobby_desk.json` (§3.1).
4. **Add `placement` to the four existing villain files:** `acting_ceo_dj.json`,
   `villain_admin.json`, `villain_admin_2.json`, `villain_admin_commander.json` (§2, §3.2–3.5).
5. **Append the discovery button** to each management inline dialog's `after` entry
   (`villain_admin`, `villain_admin_2`, `villain_admin_commander` — §3.2–3.4). Each is a
   `tag` latch + `{do:announce}`; **no new functions**.
6. **(No `hq_stabilize` edit.)** DJ's stabilization is shipped and unchanged; do NOT add a
   Dark-Urge latch (§3.5 — the tier is level-cap-derived, gym 8).
7. **(Optional) Cutscene:** author `cutscenes/hq_raid_entry.json` via `/cutscene record`,
   add `function/hq/raid_begun.mcfunction`, wire it into the lobby `ascend_button` (§3.6).
8. **Add the SIDE quest holder** `q.side_raid` to `registers/quest_targets.json` (§3.7) at
   `slot: 82`; a fresh compile errors on a duplicate slot — re-confirm.
9. **Compile:** run `scripts/content_compile` (regenerates presets + `quest_waypoints.json`;
   validates no `"` in text and no `'`/`%` in macro fields). Fix any lint.
10. **Regenerate NPC presets:** `generate_npc_function` + `update_preset_index` (the new
    receptionist + the four now-placed villains latch-spawn on the install run).
11. **Runtime smoke test** (single-player, install run first): below-gate refusal at the
    desk and at DJ; ascend at the gate; climb floors 1→3 reading each artifact; DJ fight →
    CURRENCY STABILIZED → DJ despawns → `cd_instability`==25 → main sidebar advances to
    *Challenge the Royal League*; `q.side_raid` marker steps floor to floor then clears.
    (Dark-Urge tier 3 is NOT expected here — verify it arrives at gym 8 instead.)

---

## 7. Open questions for showrunner

1. **Raid-entry cutscene in scope?** §3.6 is fully optional and can ship post-launch.
   Green-light it for this unit, or defer and just keep the title sting on ascend?
2. **Floor discovery UX.** The artifacts are delivered as a **button on each management NPC's
   after-defeat dialog** (reliable, no new entity, no new function). Alternative: place
   readable **lecterns / item frames / signs** in-world at each floor for a more diegetic
   discovery. Dialog-button is the safe default; confirm if you want physical props instead
   (props are a build task, not an authoring one).
3. **HQ area folder.** Do new HQ characters live under `characters/villain/` (with the
   existing villains) or a new `characters/cyber/` / `characters/hq/` area folder? Affects
   only file paths, not ids.
4. **`hq_raid_active` cleanup.** The tag is never cleared (harmless — the side holder also
   requires `not defeated_villain_boss`, so it goes dark on DJ's defeat). Fine to leave, or
   add a `tag @s remove hq_raid_active` to `hq_stabilize` for tidiness? (Note: touching
   `hq_stabilize` at all should be weighed against keeping it verbatim.)
5. **DJ level 64 vs cap 62.** Entry-cap+2 matches the ladder-canon *ace at entry-cap+2* and
   is intentional for a story-climax boss. Confirm you are happy, or retune
   `villain_boss.json` down (retune the trainer JSON, never the ladder).
```
