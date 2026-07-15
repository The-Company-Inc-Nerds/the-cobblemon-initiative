# 14 — The Five Elemental Shrines: connective quests + cultist gauntlets

> **Unit slug:** `14_shrines` · **Type:** SET-PIECE (5 sub-locations, cross-act) ·
> **Depends on:** `13_nobles_gating` (crystal-launch contract — see §Overview, RESOLVED), the
> five adjacent gym units (`mystic`, `kalahar`, `ryujin`, `nifl`, `scorchspire`), `royal_league`
> (Fire gate). **Companion audit:** `docs/roadmap/18_shrines_audit.md` — this plan is the
> *content authoring* pass that closes its §6 (cultist bodies) / §5 (breadcrumb + capstone) /
> §8 (challenge wiring) gaps. Invented coords are labelled **PROPOSED**.

This unit is the **quest tissue** around the five shrines. The *machinery* already ships:
the Java `ShrineChallengeManager` (types `timed_parkour` / `dark_gauntlet` / `hydra_gauntlet`
/ `fairy_tests`), the five leader character+dialog files, the full 4-cultist trainer ladders
(`trainers/shrines/*.json`, teams + prereq chains + coords), the crystal items
(`ShrineCrystalItem`, `ModItems`), the install.json SHRINE zones, and the leader placement
anchors (`devtest/placement_plan.json`). What is **missing** and what this unit authors:

1. **Cultist bodies** — the trainer *configs* for `<type>_shrine_cultist_1..4` exist with
   full teams, but there are **no `dialog-src/characters/**` or `dialog-src/dialog/**` files**
   for any cultist (leaders only). 20 acolytes need character+dialog stubs, or the ladder
   must be trimmed. **Decision (spec below): AUTHOR all four per shrine** — the configs already
   gate `leader ← cultist_3 AND cultist_4`, so trimming would orphan the leader prereq. We ship
   thin, per-rung `shrine_cultist` bodies (four tiny dialog files per shrine, gated by rung).
2. **Breadcrumbs** — no town currently points at its shrine. Each of the five host towns
   gets a **rumor line** wired into its arrival hub (nurse/guide), gated on the shrine
   being unopened.
3. **Challenge overlays** — the parkour/dark/hydra/fairy-tests gauntlets are coded but
   **orphaned** (nothing calls `shrine <id> start`). This unit adds the begin-the-trial
   button to each leader dialog and specs the finish-line props (§Quests, per shrine).
4. **The payoff** — a `memory/shrine/*` recognition fragment on first clear, the `all_shrines`
   capstone payout, and the **crystal-launch contract** now owned by `13_nobles_gating`
   (crystals raise the type-matched noble; see §Overview).

---

## Overview

### What it is

Five **optional element-guardian trials**, each an independent sub-location, each a
*4-acolyte PvP ladder → 1 ancient High Priest(ess)* overlaid with a **signature challenge
type**. The keepers are the one NPC tier that reads the amnesiac as an **ancient presence**
(old gravity, a long shadow already cast), NOT a Company face — dread-poetry that seeds
Act 3 tonally without ever naming the Founder. Corporate-dread comedy is **off** here; the
shrines play the mystery straight, and that contrast is the feature.

### Route placement & band context

Each shrine gates on its **adjacent gym leader** (Fire on the Royal champion) and is balanced
to that town's entry cap. Coords are config-declared and placement-confirmed.

| Shrine | Leader | Type | Gate tag | Cap in force | Adjacent town (gym) | Leader anchor (placement_plan) | Cultist ladder anchor |
|---|---|---|---|---|---|---|---|
| **Fairy** | Aurora | `fairy_tests` | `defeated_mystic_leader` | **37** | Mystic Marsh (g3) | `[957, -7, 2715]` | `[947..953, -7, 2715/2718]` |
| **Ground** | Terran | `dark_gauntlet` | `defeated_kalahar_leader` | **56** | Kalahar Reach (g6) | `[1910, 83, 4049]` | `[1899..1910, 83, 4049]` |
| **Dragon** | Draconis | `hydra_gauntlet` | `defeated_ryujin_leader` | **68** | Ryujin Keep (g8) | `[2008, 66, 921]` | `[1998..2008, 66, 921]` |
| **Ice** | Glacius | `timed_parkour` (ice floor) | `defeated_nifl_leader` | **74** | Nifl Town (g9) | `[3644, 68, 1960]` | `[3634..3644, 68, 1960]` |
| **Fire** | Ignis | `timed_parkour` (speed) | `royal_league_champion` | **85** | Scorchspire (g10) | `[3510, 51, 4702]` | `[3500..3510, 51, 4702]` |

Instability-neutral: shrines do **not** touch `cd_instability` or `fields_liberated`, and this
unit keeps it that way (audit §2, confirmed no band-tag writes in `shrine_*`).

### The arc job it does

- **Diegetic legendary gate** for `13_nobles_gating`. Clearing a shrine unlocks the
  type-matched noble and hands the player a **crystal that raises it** — see the crystal-launch
  contract below.
- **Ancient-recognition tier** — the only place the world recognises the protagonist as
  something *older than* the Company, foreshadowing the mirror without spoiling it.
- **Signature stream set-pieces** — Fairy shiny/solo/bonded lead only, Ground blind +
  earthquakes at half HP, Ice/Fire parkour-against-a-clock, Dragon three-headed hydra.

### Crystal-launch contract (RESOLVED — owned by `13_nobles_gating`)

`13_nobles_gating` §Build-step-5 **already resolved** the old crystal collision. Both docs must
read the same way, and this is the canonical statement for both:

- **The shrine crystal is a portable noble launcher, not a plain-wild spawner.** The Java
  `ShrineCrystalItem` is repointed from its legacy `spawnpokemon <species> level=70` to
  `function cobblemon_initiative:noble/<id>/crystal_launch`, whose one line is
  `execute as @p[distance=..8] at @s run noble start <id>`. So the Fire/Ground/Ice/Dragon
  crystal **raises the LoA noble boss** (gated, cap-legal, one arena), not a raw L70 wild.
  This is the audit fix for the flat-L70 and double-grant problems in one move.
- **Fairy is the exception — Xerneas has no noble config.** There is no
  `noble_encounters/xerneas.json`, so the Fairy crystal keeps its legacy `spawnpokemon` path
  (a plain catchable Xerneas). Its spawn level is retuned to the cap in force at the gate
  (see Open Q2), not a flat L70. Fairy is the sole Xerneas path either way.
- **The shared unlock flag is `defeated_<type>_shrine_leader`** (the leader battle-win tag,
  set by the standard `defeat_tag` convention). `13_nobles_gating` gates each type-matched
  noble button on that exact tag (`defeated_ice_shrine_leader` → Articuno button,
  `defeated_fire_shrine_leader` → Moltres, `defeated_dragon_shrine_leader` → Rayquaza).
  **This unit does NOT invent a `<type>_shrine_complete` gate for the noble handshake** —
  `defeated_<type>_shrine_leader` is the single coordination flag, and it matches what
  `13_nobles_gating` §5 already consumes.
- **`<type>_shrine_complete` is this unit's own advancement/capstone flag** (§Q4), NOT part of
  the noble handshake. Fairy uses `fairy_shrine_complete` per its config `achievementOnDefeat`;
  confirm the other four emit `ground/dragon/ice/fire_shrine_complete` (grep step, §New tags).
  It drives the `all_shrines` capstone and the `#shrines` render count — nothing cross-unit.

### The double-crystal grant (must fix before ship — audit §7, verified)

`PlayerProgressManager.onTrainerDefeated` grants the leader `rewards` array **and**
`grantShrineCrystal(...)` — so a shrine-leader win currently yields **two crystals**.
**Fix:** strip the `{"item":"cobblemon-initiative:<type>_shrine_crystal"}` line from each
`trainers/shrines/*.json` leader `rewards` array (keep the Java grant, which prints the
place-to-summon message). Listed in the Build Checklist. Combined with the crystal-launch
repoint above, one clean crystal → one gated noble.

---

## Cast

20 cultists + 5 leaders (leaders already authored — listed for completeness). Per-rung dialog
(four tiny files per shrine, one per rung) is the recommended shape: it gives each acolyte its
own after-line and avoids the `any_tags` priority-collision risk (§13.3). Authoring cost is
**20 thin character files + 20 tiny cultist dialog files**.

| id | display_name | role | concept | placement anchor |
|---|---|---|---|---|
| `fairy_shrine_cultist_1` | Fae Acolyte Pixie | shrine_cultist | first rung, wide-eyed devotee | `[947, -7, 2715]` |
| `fairy_shrine_cultist_2` | Fae Zealot Sprite | shrine_cultist | second rung, fervent | `[950, -7, 2718]` |
| `fairy_shrine_cultist_3` | Fae Heretic Veil | shrine_cultist | third rung, the light showed her something | `[953, -7, 2715]` |
| `fairy_shrine_cultist_4` | Fae Warden Glimmer | shrine_cultist | fourth rung, gatekeeper of the inner light | `[953, -7, 2718]` |
| `ground_shrine_cultist_1..4` | Stone Acolyte / Zealot / Warden / Keeper | shrine_cultist | buried-order acolytes, half-blind by choice | `[1899..1910, 83, 4049]` |
| `dragon_shrine_cultist_1..4` | Wyrm Acolyte / Zealot / Fang / Scale | shrine_cultist | fire-tenders, three of them run the hydra | `[1998..2008, 66, 921]` |
| `ice_shrine_cultist_1..4` | Frost Acolyte / Zealot / Mirror / Stillness | shrine_cultist | motionless keepers of the frozen path | `[3634..3644, 68, 1960]` |
| `fire_shrine_cultist_1..4` | Ember Acolyte / Zealot / Cinder / Ash | shrine_cultist | endgame ash-priests, post-league | `[3500..3510, 51, 4702]` |
| `fairy_shrine_leader` | High Priestess Aurora | shrine_leader | *exists* — `characters/shrine/fairy_shrine_leader.json` | `[957, -7, 2715]` |
| `ground_shrine_leader` | High Priest Terran | shrine_leader | *exists* | `[1910, 83, 4049]` |
| `dragon_shrine_leader` | High Priest Draconis | shrine_leader | *exists* | `[2008, 66, 921]` |
| `ice_shrine_leader` | High Priest Glacius | shrine_leader | *exists* | `[3644, 68, 1960]` |
| `fire_shrine_leader` | High Priest Ignis | shrine_leader | *exists* | `[3510, 51, 4702]` |

> Exact per-cultist coords are in each `trainers/shrines/<type>_shrine.json` `coordinates`
> field. Use those as the `placement` stub. UUIDs intentionally omitted (auto-spawn latch,
> same convention as the leaders).

**Rumor-hub NPCs** (breadcrumb owners — reuse existing town NPCs, add one button each):

| Town | Hub NPC (existing) | Add |
|---|---|---|
| Mystic Marsh | `mystic_guide` (`dialog:gym_guide`) or town nurse | rumor line → Fairy shrine |
| Kalahar Reach | `kalahar_guide` | rumor line → Ground shrine |
| Ryujin Keep | `ryujin_guide` | rumor line → Dragon shrine |
| Nifl Town | `nifl_guide` | rumor line → Ice shrine |
| Scorchspire | `scorchspire_guide` | rumor line → Fire shrine |

---

## Quests

Six quest-shaped beats. Q1 (cultist ladders) and Q2 (challenge overlays) repeat per shrine;
Q3–Q6 are cross-shrine connective tissue.

### Q1 — The Cultist Gauntlets (per shrine ×5): author the 20 acolyte bodies

**Concept:** each shrine's four acolytes form a clean `1→2→3→4→leader` ladder. The trainer
configs + teams + prereqs already exist; this quest adds the **bodies** so the ladder is
walkable. **Forward hook:** each rung's flavor points up the line (the Veil waits past me
→ and past her, the High Priest(ess)). **Back-echo:** rung-1 references the adjacent gym
just cleared (you carry Titania of the Marsh and think that is enough — the light here is older).

Ladder gating is **enforced by the DIALOG battle-button gates**, not the trainer-JSON
`prerequisites`. (CORRECTED 2026-07-13: TBCS dialog-button battles bypass rctmod entirely —
`prerequisites` never gate a `tbcs battle`, and `canBattleTrainer` has no callers. So each rung's
battle button must gate on the previous rung's `defeated_<id>` tag, and the leader's button on
`<type>_shrine_trial_clear`.) A beaten rung shows its after-line; an un-beaten rung shows its
challenge + battle button. No `has_item` gate anywhere. The `<type>_shrine_trial_clear` tag is
now set by `ShrineChallengeManager.completeChallenge` (2026-07-13 — it previously latched nothing).

**READY-TO-PASTE character (Fairy cultist 1 — the other 19 are this file with id / display_name
/ trainer / placement swapped; `battle.trainer` = the id, which is the RCT trainer id):**

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "fairy_shrine_cultist_1",
  "display_name": "Fae Acolyte Pixie",
  "role": "shrine_cultist",
  "act": "n/a",
  "location": "Fairy Shrine",
  "trainer": "fairy_shrine_cultist_1",
  "recipe": "trainer_one_time",
  "dialog": "dialog:shrine_fairy_cultist_1",
  "movement": { "objective": "ambient_stationary_look" },
  "battle": {
    "trainer": "fairy_shrine_cultist_1",
    "type": "one_time",
    "format": "GEN_9_SINGLES",
    "prize": 400,
    "defeat_tag": "defeated_fairy_shrine_cultist_1",
    "win_line": "The light does not flinch for you. It only leans. Go on, then - the Veil waits past me.",
    "lose_line": "You are turned back at the first rung. The radiance keeps its own counsel.",
    "already_beaten_line": "I have shown you my measure. The Veil is your next reckoning, not me."
  },
  "placement": { "x": 947, "y": -7, "z": 2715 }
}
```

> Per-shrine swaps: `type` stays `one_time`; `format` = `GEN_9_SINGLES` for ALL cultists
> (only Ground/Dragon **leaders** and Dragon hydra stages 1–2 are Doubles — cultists are all
> Singles per config). `prize` = **400** (cap-band consolation; leader prize is 2500 flat).
> `placement` = that cultist's config `coordinates`. `dialog` = `dialog:shrine_<type>_cultist_<N>`.

**READY-TO-PASTE dialog (one tiny per-rung tree — Fairy rung 1 shown; the other 19 are this
file with id / rung tag / win-flavor swapped).** STANDARD, two entries: a beaten after-line and
a default challenge. The `{do:battle}` action reads THIS NPC's own `battle` block:

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "shrine_fairy_cultist_1",
  "type": "STANDARD",
  "entries": [
    {
      "label": "beaten",
      "name": "Fae Acolyte - after defeat",
      "priority": 40,
      "gate": { "tag": "defeated_fairy_shrine_cultist_1" },
      "say": [
        "You passed this rung. The light remembers your shape - it does not test the same soul twice at the same step.",
        "Climb on. What waits above does not grow patient."
      ],
      "buttons": [
        { "label": "leave_button", "text": "I climb on", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Fae Acolyte",
      "priority": 10,
      "default": true,
      "say": [
        "Halt. You carry Titania of the Marsh on your belt and think the charm is yours. The light here is older than any badge, and it leans toward you the way it leans toward things it has known.",
        "Prove your footing on this rung, or turn back. The radiance is not for the unproven.",
        "Who were you, that the light should flicker when you enter. Face me, and perhaps the shrine will say."
      ],
      "buttons": [
        { "label": "yes_button", "text": "Face the acolyte", "actions": [ { "do": "battle" } ] },
        { "label": "leave_button", "text": "Not yet", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

> **Why per-rung (not one shared tree):** a single shared dialog would need an `any_tags`
> fan-out on the beaten entry (schema §13.3, AND-only conditions), which risks a priority
> collision when the four fan-out siblings share a priority. Four tiny per-rung files sidestep
> that entirely and let each acolyte carry its own after-line — recommended and cheap. Ladder
> ORDER is enforced by the trainer-JSON `prerequisites`, not by these gates. Back-echo swaps
> per shrine: Fairy→Titania of the Marsh, Ground→Gaia of Kalahar, Dragon→Ryujin of the Keep,
> Ice→Boreas of Nifl, Fire→the crown you earned at the League.

**DATAPACK NEEDS:** none. Cultist battles are stock `{do:"battle"}`; the `defeated_*` tags and
prereq gating are handled by RCT + `PlayerProgressManager` (existing). No turn-in, no payout.

**QUEST_TARGETS entry:** covered by the per-shrine quest in Q6 (one sidebar quest per shrine
walks the ladder → leader). No standalone cultist quest.

**REWARD/BALANCE:** each cultist `prize` 400 CD (flat, cap-band). Config `rewards` already give
`3× ultra_ball` per cultist — keep. Cap-legality: cultist teams top at gate-cap +2 target
(Fairy rung-4 currently tops L43 vs cap 37 — **+6 over**; the audit §9 flags shrine teams run
hot and the leader retune pass (§Q2, Open Q4) brings the ladder back to the +2 cultist band).
**No decline** (shrines are not Company encounters; a player who is not ready walks away via the
Not-yet button — fail-soft, no fee). No forced battle: every cultist is interaction-initiated,
so the fairness floor (never whiteout a player with no mons) holds trivially.

---

### Q2 — Wire the Challenge Overlay (per shrine ×5)

**Concept:** the shrine's signature trial (parkour / dark / hydra / fairy-tests) is coded but
**never started** — this quest adds the begin-the-trial button to each leader's `default`
dialog entry and specs the finish-line prop. **Forward hook:** the trial framing names the
prize (clear the frozen path and the frost will seal you — and what it seals, you may carry).
**Back-echo:** the leader references the ladder just climbed (you passed my acolytes; the
shrine itself is not so easily flattered).

Per the audit §8, the wiring is a one-line command action before the battle button. The command
grammar is `cobblemon-initiative shrine <type> start` (and `... complete` at the finish). Below
is the **edit to each existing** `dialog-src/dialog/shrine_<type>.json` `default` entry — shown
for Ice (the ice-floor parkour). The leader `battle` button is **gated behind the challenge
completion tag** so you cannot skip the trial. Note: the shipped `shrine_ice.json` `default`
entry currently has **no `default: true` flag** — add it while editing, per §3.2 (exactly one
default entry). Keep the existing `after` entry untouched.

```json
{
  "label": "default",
  "name": "Glacius - challenge",
  "priority": 10,
  "default": true,
  "say": [
    "Do not move so quickly. This is the Ice Shrine, where everything is kept exactly as it was, untouched by time or mercy. I am Glacius, its still keeper.",
    "There is a path of ice below. Cross it before the frost takes you back to the start. What is frozen does not hurry, and neither may you afford to.",
    "The frost is not given to the warm of heart. It is earned by those who can be still in the face of their own reflection. Cross the path, then face me."
  ],
  "buttons": [
    {
      "label": "begin_trial_button",
      "text": "Begin the frozen path",
      "gate": { "not_tag": "ice_shrine_trial_clear" },
      "actions": [
        { "do": "command", "cmd": "cobblemon-initiative shrine ice start", "as_player": true },
        { "do": "close" }
      ]
    },
    {
      "label": "yes_button",
      "text": "Face the High Priest",
      "gate": { "tag": "ice_shrine_trial_clear" },
      "actions": [ { "do": "battle" } ]
    },
    { "label": "leave_button", "text": "I am not ready", "actions": [ { "do": "close" } ] }
  ]
}
```

> **Per-type command + finish wiring:**
> - **Ice / Fire** (`timed_parkour`): `shrine ice start` / `shrine fire start`. Finish line =
>   a builder-placed command block / pressure plate at the summit running
>   `execute as @a[distance=..3] run cobblemon-initiative shrine <type> complete`. That
>   `complete` grammar (from `ShrineChallengeManager.completeParkour`) sets
>   `<type>_shrine_trial_clear`. **PROPOSED** plate coords: Ice summit near `[3644, 78, 1960]`,
>   Fire summit near `[3510, 61, 4702]` — builder confirms.
> - **Ground** (`dark_gauntlet`) / **Dragon** (`hydra_gauntlet`): start button only; they
>   **complete on the leader's defeat** (`onTrainerDefeated` already wired). For these two the
>   button starts the hazard/hydra and the leader battle button is NOT gated behind a separate
>   clear tag — the trial *is* the leader fight. Ground: `shrine ground start` applies
>   blindness + half-HP + earthquakes; Dragon: `shrine dragon start` runs the three hydra
>   stages (`dragon_hydra_1/2/3`) then the trial completes and the Draconis battle is the
>   capstone.
> - **Fairy** (`fairy_tests`): `shrine fairy start` at an **altar prop** (PROPOSED
>   `~[957, -7, 2715]`, at Aurora's feet), then a second interaction `shrine fairy test resolve`
>   registers the lead mon's UUID; the tests (friendship 160 / fullness 50 / nickname / shiny /
>   solo party) are individual feedback commands. Aurora's battle button then gates on the
>   resolve registration so you must beat her with that exact bonded shiny solo lead. This is
>   the marquee hardcore-Nuzlocke clip.

**DATAPACK NEEDS:**
- `function/sidequest/shrine/<type>_trial_help.mcfunction` *(optional, ×5)* — a one-line
  `tellraw` reminder of the trial rules, fired from the begin-trial button before `start`.
  Prints the type-specific hazard warning (ice cracks / earthquakes / hydra heals-between /
  bring-a-bonded-shiny-solo). Macro-safe: no `"` / `'` / `%` in the delivered string.
- No new turn-in/count functions — the challenge state is owned by `ShrineChallengeManager`.

**QUEST_TARGETS entry:** folded into Q6's per-shrine quest (stage advances climb-ladder →
clear-the-trial → face-the-keeper using the `<type>_shrine_trial_clear` /
`defeated_<type>_shrine_leader` tags).

**REWARD/BALANCE:** no CD for the trial itself; the payoff is the leader unlock + crystal.
Cap-legality: trials are mechanical (parkour/blindness), not battles, so no cap concern. The
leader battles run hot on purpose (audit §4b) but are **opt-in** — the I-am-not-ready button is
always present and fail-soft; a whiteout inside a shrine is a normal Nuzlocke faint, not a
forced encounter. **Retune leaders per audit §9 Open-Q4** (Fairy +11 over cap is too high; Fire
−3 under is too soft) — teams only, never the ladder.

---

### Q3 — The Land Remembers (memory-fragment recognition, ×5)

**Concept:** on the **first** shrine leader defeated, fire a one-time recognition beat — a
shrine-flavored `memory/shrine/*` fragment that circles the amnesia mystery from the
*elemental* angle (something in you is older than the badges). Reuses the fragment idiom
without touching `cd_instability` or the canon gym fragments. **Forward hook:** it names the
*other* shrines (four more keepers will know your weight). **Back-echo:** it references the
gym badge the player is wearing when they cleared the shrine. This is the audit §5 S1 hook.

**No new character** — the beat fires from the leader `battle` block's `on_win`, once, gated by
a not-tag. Edit each shrine leader **character** `battle` block (all five leaders share the one
`first_keeper` function — the function's own `shrine_frag_seen` latch makes only the first clear
show it):

```json
"on_win": [
  "execute as @1 unless entity @1[tag=shrine_frag_seen] run function cobblemon_initiative:memory/shrine/first_keeper"
]
```

**DATAPACK NEEDS:**
- `function/memory/shrine/first_keeper.mcfunction` — one-time recognition fragment. Spec:
  `tag @s add shrine_frag_seen`, then a macro-safe `tellraw` first-person flash (NO `"`/`'`/`%`
  in the delivered string), then `playsound` a low chime + `title @s actionbar` a gray subtitle.
  Content (macro-safe, apostrophe-free): *The keeper called it old gravity. Something in you is
  older than the badges you have earned. Four more keepers will know your weight before this is
  done.* Fires from ANY shrine leader `on_win`, latched by `shrine_frag_seen` so only the first
  clear shows it.

**QUEST_TARGETS entry:** none (it is a sting, not a tracked objective).

**REWARD/BALANCE:** narrative only, no CD. Instability-neutral (rule per audit §2). Civilian /
Mom rules untouched — this is the *keeper* tier, not a Company or civilian NPC.

---

### Q4 — Five Keepers capstone (all-shrines meta-collection)

**Concept:** defeating all five leaders grants the existing `all_shrines` advancement (coded in
`PlayerProgressManager`) which currently pays **nothing**. Attach a capstone: a title-card sting
+ a relic/CD payout. **Forward hook:** points at the endgame (five crystals in one hand — and
one mirror left to face). **Back-echo:** names all five keepers by title. Audit §5 S2.

**New character** — a **wandering pilgrim** near the Fairy shrine approach who acknowledges the
collection and hands the capstone. Diegetic and low-placement (one latch prop):

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "shrine_pilgrim",
  "display_name": "The Last Pilgrim",
  "role": "lore_keeper",
  "act": "n/a",
  "location": "Fairy Shrine approach",
  "recipe": "civilian",
  "dialog": "dialog:shrine_pilgrim",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 945, "y": -7, "z": 2712 }
}
```

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "shrine_pilgrim",
  "type": "STANDARD",
  "entries": [
    {
      "label": "all_five",
      "name": "The Last Pilgrim - all five",
      "priority": 30,
      "gate": { "all_tags": ["defeated_fairy_shrine_leader", "defeated_ground_shrine_leader", "defeated_dragon_shrine_leader", "defeated_ice_shrine_leader", "defeated_fire_shrine_leader"] },
      "say": [
        "Five keepers. Aurora, Terran, Draconis, Glacius, Ignis - all of them leaned toward you, and all of them let you pass. That has not happened in my lifetime, nor my mother before me.",
        "The crystals answer to one hand now. Yours. I do not know whose hand it was before, but the shrines do, and they are not afraid of it. Be worthy of that."
      ],
      "buttons": [
        {
          "label": "claim_button",
          "text": "I carry all five",
          "gate": { "not_tag": "five_keepers_paid" },
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/shrine/five_keepers_reward", "as_player": true },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "I will be worthy", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "counting",
      "name": "The Last Pilgrim - counting",
      "priority": 20,
      "gate": { "any_tags": ["defeated_fairy_shrine_leader", "defeated_ground_shrine_leader", "defeated_dragon_shrine_leader", "defeated_ice_shrine_leader", "defeated_fire_shrine_leader"] },
      "say": [
        "You have knelt at one shrine, or two. The land is beginning to know your tread. Find the others - Aurora in the marsh deep, Terran under Kalahar, Draconis near the keep, Glacius past Nifl, Ignis beyond the last flame.",
        "Bring all five crystals to one hand and the shrines will have said all they can say without saying the thing they will not."
      ],
      "buttons": [
        { "label": "leave_button", "text": "I will find them", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "The Last Pilgrim",
      "priority": 10,
      "default": true,
      "say": [
        "I walk the five shrines and kneel and am turned away, kindly, every time. The keepers say the shrines wait for a particular tread. Not mine. Perhaps yours - you have the old gravity about you, if you will forgive an old pilgrim saying so.",
        "Five elements, five keepers, five crystals. Clear them and you will hold something no living trainer has held. Whether that is a gift is not for me to say."
      ],
      "buttons": [
        { "label": "leave_button", "text": "I will walk them", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:**
- `function/sidequest/shrine/five_keepers_reward.mcfunction` — one-time capstone payout. Spec:
  `tag @s add five_keepers_paid`, `function cobblemon_initiative:economy/payout {amount:5000}`
  (skewed CD, the only shrine CD faucet), `loot give @s loot
  cobblemon_initiative:npc_gift/<capstone_relic>` (a Master Ball or a stack of rare candy —
  showrunner picks), then `title @s title` a streamable card **FIVE KEEPERS ANSWER** +
  `title @s subtitle` *The crystals are yours*. Macro-safe: no `"`/`'`/`%` in delivered strings.
  `playsound` a triumphant chime.

**QUEST_TARGETS entry (the capstone quest — first-match-wins, done state first):**

```json
{
  "holder": "q.side_shrines_capstone",
  "name": "Five Keepers",
  "slot": 51,
  "note": "Meta-collection over all five elemental shrines. Only visible once at least one shrine is cleared; resolves when all five defeated_<type>_shrine_leader tags are held and the pilgrim payout is claimed. Instability-neutral.",
  "stages": [
    {
      "if_tags": ["five_keepers_paid"],
      "not_tags": [],
      "label": "Five Keepers - the crystals answer to one hand",
      "target": null,
      "note": "Done state - flavor line, no waypoint."
    },
    {
      "if_tags": ["defeated_fairy_shrine_leader", "defeated_ground_shrine_leader", "defeated_dragon_shrine_leader", "defeated_ice_shrine_leader", "defeated_fire_shrine_leader"],
      "not_tags": ["five_keepers_paid"],
      "label": "Five Keepers - claim the crystals from the Last Pilgrim",
      "target": { "npc": "shrine_pilgrim" },
      "note": "All five cleared, payout unclaimed - point at the pilgrim."
    },
    {
      "if_tags": [],
      "not_tags": ["five_keepers_paid"],
      "scores": [ { "objective": "quest_hud", "holder": "#shrines", "op": "gte", "value": 1 } ],
      "label": "Five Keepers - clear all five elemental shrines",
      "target": null,
      "note": "At least one shrine cleared (render scratch #shrines counts the five defeated_<type>_shrine_leader tags) - highlight line, no single waypoint since five sites."
    }
  ]
}
```

> **`#shrines` render scratch** = a per-render count of held
> `defeated_{fairy,ground,dragon,ice,fire}_shrine_leader` tags, same pattern as `#board` in
> `q.main` (verified in the quest_targets `q.main` sample). The sidebar render tick must
> populate it; noted in New tags/scores.

**REWARD/BALANCE:** 5000 CD (skewed via `economy/payout`) + one capstone relic, one-time. This
is the single shrine CD faucet and it is fine — shrines are a reward faucet, not a sink (audit
§7). No decline (not a battle).

---

### Q5 — Town Breadcrumbs (rumor hubs, ×5)

**Concept:** each host town's arrival hub (nurse/guide) gains a **rumor line** pointing at its
shrine, gated on the shrine being unopened (`not_tag defeated_<type>_shrine_leader`). Matches
the Nurse-Lila rumor-board idiom exactly (`sidequest/rumors/lila.mcfunction`). **Forward hook:**
IS the hook — it is the only thing in town that names the shrine. **Back-echo:** the line
references the gym leader just beaten (now that you carry Titania of the Marsh, the marsh-deep
keeper will see you).

**No new character** — add a button to each existing town guide/nurse dialog. Example edit to
Mystic's hub (add to the `default` entry `buttons`, after the existing ones):

```json
{
  "label": "shrine_rumor_button",
  "text": "Is there anything older than the gym here?",
  "gate": { "not_tag": "defeated_fairy_shrine_leader" },
  "actions": [
    { "do": "command", "cmd": "function cobblemon_initiative:sidequest/rumors/shrine_fairy", "as_player": true },
    { "do": "close" }
  ]
}
```

**DATAPACK NEEDS (×5):**
- `function/sidequest/rumors/shrine_<type>.mcfunction` — a single `tellraw` rumor (gray/aqua,
  matching Lila's format) + `playsound item.book.page_turn`. Spec per town, macro-safe (no
  `"`/`'`/`%` in delivered lines):
  - **shrine_fairy** (Mystic): *The marsh runs deeper than the gym. Below the water there is a
    light that does not warm - a shrine older than the town, and a priestess who turns away all
    but one tread. They say she has been waiting.* Anchor hint: below Mystic, `~[957, -7, 2715]`.
  - **shrine_ground** (Kalahar): *There is an order under the dunes that puts out its own eyes to
    hear the earth. A dark trial, and a keeper named Terran at the bottom of it. Kalahar does not
    speak of them, which is how you know they are there.*
  - **shrine_dragon** (Ryujin): *Past the keep, the oldest fire still coils in a shrine no map
    marks. A hydra guards it, and a High Priest who says the wyrm remembers a tread from before
    the towns. Bring hardened scales, and a party built for two-on-two - the keeper fights in
    pairs.*
  - **shrine_ice** (Nifl): *West of Nifl the ice runs a path that cracks under the unworthy and
    throws them back to the start. Glacius keeps it. He does not move, they say, not once in a
    lifetime of watching the door.*
  - **shrine_fire** (Scorchspire, post-league only): *Beyond the last flame there is a caldera the
    league does not test you against - a fire-shrine and a priest, Ignis, who only opens to a
    champion. When you wear that crown, the flame will want its measure of you.*

> Fire's rumor should additionally gate `not_tag royal_league_champion` → show a not-yet teaser
> variant, since the shrine is post-league. Two-line variant handled inside the mcfunction
> (`execute unless entity @s[tag=royal_league_champion]` → teaser; else → the full breadcrumb).
> The Ground and Dragon rumors carry the **Doubles warning** in-line (Terran and Draconis fight
> `GEN_9_DOUBLES` — surface it so the player fields a doubles-legal party; audit Open Q6).

**QUEST_TARGETS entry:** none for the rumor itself (it feeds Q6's per-shrine quest).

**REWARD/BALANCE:** none (info only). Read-only, no state written beyond the roll scratch.

---

### Q6 — Per-Shrine Sidebar Quest (×5): the trackable objective

**Concept:** one tracked quest per shrine so the sidebar/waypoint walks the player from
breadcrumb → ladder → trial → keeper → crystal. First-match-wins, done-state first.
**Forward hook:** the done stage names the crystal and points at the noble it raises.
**Back-echo:** the opening stage references the adjacent gym gate.

**No character/dialog** — pure register entry. **READY-TO-PASTE (Fairy; the other four mirror
it with type/leader/coords/gate swapped):**

```json
{
  "holder": "q.side_shrine_fairy",
  "name": "The Fairy Shrine",
  "slot": 52,
  "note": "Optional elemental shrine near Mystic Marsh. Opens on defeated_mystic_leader; walks the 4-cultist ladder -> fairy_tests trial -> High Priestess Aurora -> fairy shrine crystal. Instability-neutral. Cultist coords from trainers/shrines/fairy_shrine.json.",
  "stages": [
    {
      "if_tags": ["defeated_fairy_shrine_leader"],
      "not_tags": [],
      "label": "Fairy Shrine cleared - the crystal raises Xerneas",
      "target": null,
      "note": "Done. Points nowhere; the crystal is a place-to-summon item (Fairy = legacy spawn, no noble config)."
    },
    {
      "if_tags": ["defeated_mystic_leader", "defeated_fairy_shrine_cultist_4"],
      "not_tags": ["defeated_fairy_shrine_leader"],
      "label": "Face High Priestess Aurora",
      "target": { "npc": "fairy_shrine_leader" },
      "note": "Ladder cleared - point at Aurora. The fairy_tests trial fires from her dialog."
    },
    {
      "if_tags": ["defeated_mystic_leader"],
      "not_tags": ["defeated_fairy_shrine_leader"],
      "label": "Climb the Fairy Shrine cultist ladder",
      "target": { "npc": "fairy_shrine_cultist_1" },
      "note": "Shrine open, ladder unfinished - point at the first acolyte. (Once mystic is cleared this stage carries the whole walk; no separate literal-coord fallback needed since the cultist latch-spawns at the shrine mouth.)"
    }
  ]
}
```

> Per-shrine swaps: Ground `q.side_shrine_ground` slot 53, gate `defeated_kalahar_leader`,
> leader `ground_shrine_leader` `[1910,83,4049]`, done-line names Groudon; Dragon
> `q.side_shrine_dragon` slot 54, gate `defeated_ryujin_leader`, `[2008,66,921]`, names
> Rayquaza; Ice `q.side_shrine_ice` slot 55, gate `defeated_nifl_leader`, `[3644,68,1960]`,
> names Articuno; Fire `q.side_shrine_fire` slot 56, gate `royal_league_champion`,
> `[3510,51,4702]`, names Moltres. **Slots 52–56 + capstone 51 — deliberately chosen to sit
> BELOW `13_nobles_gating`'s noble side-quests (slots 44–50) so the two units do not collide.**
> (Existing register slots run 57–81 + 100; verify 44–56 stay free at compile.)

**DATAPACK NEEDS:** none (register-only; the render tick already resolves `{npc:id}` and
`{x,y,z}` targets).

**REWARD/BALANCE:** the quest is the tracker; rewards are the crystal + leader prize (2500 CD).

---

## Recognition & economy beats

**Recognition — the ancient/elemental tier (NOT the Company gradient).** The keepers and
cultists react to the protagonist as an *old presence*, never a Company face. This tier is
gated on **presence at the shrine**, not on badges/`cd_instability`/`fields_liberated` — so it
reads identically early and late (the land's memory does not care about badges). The lines
already shipped on the leaders (old gravity; I saw two faces and one was older, and colder, and
smiling — `shrine_ice.json`) set the register; the cultist lines above match it. This is the one
place that foreshadows the mirror **without** using Company recognition sugar and **without
naming the Founder** (legal per §7 continuity — the shrine voice is oblique). Do NOT add
`recognition:` gates to shrine NPCs — keep them band-agnostic.

**Economy — deliberately silent.** Shrines do not move `cd_instability` (audit §2) and are not
wheat-economy actors. The **only** CD flavor is the skew on the payout function
(`economy/payout` haircuts the Five-Keepers 5000) — the ambient the-Company-skims-everything
texture, not a shrine-specific beat. **No wheat trader, no propaganda, no field-liberation
interaction** at any shrine. The corporate voice is intentionally absent; that silence is the
tonal contrast the audit calls a feature. (The economy-voice house-style box is satisfied by
this deliberate-silence statement + the payout skew line — the absence is the beat.)

**Civilian/Mom rules:** untouched. The Last Pilgrim (Q4) is a `lore_keeper`, not a civilian who
recognises the founder — he recognises the *tread* / *old gravity* / the elemental motif, never
the CEO. He never names the Company or the Founder. No civilian in this unit recognises the
founder; Mom is not involved at any shrine.

---

## New tags/scores introduced

| tag / score | set by | gated by / read by |
|---|---|---|
| `defeated_<type>_shrine_cultist_1..4` | RCT win on each cultist (existing `defeat_tag` convention) | leader prereqs (trainer JSON); Q1 cultist after-line; Q6 ladder stage |
| `<type>_shrine_trial_clear` | `ShrineChallengeManager` `... complete` (parkour) / trial completion | Q2 leader battle-button gate (Ice/Fire); Q6 |
| `defeated_<type>_shrine_leader` | shrine leader defeat (standard `defeat_tag`) | Q4 capstone; Q6 done-state; `#shrines` count; **`13_nobles_gating` noble-button gate (the shared handshake flag)** |
| `<type>_shrine_complete` | shrine leader defeat (`achievementOnDefeat` → the `*_complete` name; Fairy = `fairy_shrine_complete`) | this unit's advancement + `all_shrines` capstone only — NOT the noble handshake |
| `shrine_frag_seen` | `memory/shrine/first_keeper` (Q3) | Q3 leader `on_win` one-shot latch |
| `five_keepers_paid` | `sidequest/shrine/five_keepers_reward` (Q4) | Q4 pilgrim claim button; Q4 capstone quest done-state |
| `#shrines` (quest_hud scratch) | sidebar render tick — counts held `defeated_<type>_shrine_leader` (×5) | Q4 capstone quest at-least-one stage |

> **Naming reconciliation:** the noble handshake with `13_nobles_gating` runs on
> `defeated_<type>_shrine_leader` (confirmed against `13_nobles_gating` §5, which gates Articuno
> on `defeated_ice_shrine_leader`, Moltres on `defeated_fire_shrine_leader`, Rayquaza on
> `defeated_dragon_shrine_leader`). Do **not** route the noble gate through
> `<type>_shrine_complete` — that string is this unit's advancement flag only. Separately confirm
> each leader's `achievementOnDefeat` emits `ground/dragon/ice/fire_shrine_complete` (grep
> `achievementOnDefeat` in each `trainers/shrines/*.json`; Fairy = `fairy_shrine_complete`
> verified). If any leader uses a different string, rename it or add a tag-alias latch.

---

## Build checklist

Ordered so each step compiles/validates before the next.

1. **Fix the double-crystal grant** (audit §7, must-do): strip the
   `{"type":"item","item":"cobblemon-initiative:<type>_shrine_crystal","count":1}` line from the
   leader `rewards` array in all five `src/main/resources/data/cobblemon_initiative/trainers/shrines/*.json`.
   Keep the Java `grantShrineCrystal` path.
2. **Confirm `achievementOnDefeat` names** in the five `trainers/shrines/*.json` leaders =
   `<type>_shrine_complete`. Rename or alias any mismatch (New tags/scores note).
3. **Drop 20 cultist character files** → `dialog-src/characters/shrine/<type>_shrine_cultist_1..4.json`
   (Q1 stub × 20; swap id / display_name / trainer / placement from each shrine's config coords).
4. **Drop 20 per-rung cultist dialog files** → `dialog-src/dialog/shrine_<type>_cultist_1..4.json`
   (Q1 tiny tree × 20; one after-line + one default challenge each, gated on that rung's tag).
5. **Edit the 5 leader dialogs** `dialog-src/dialog/shrine_<type>.json`: add the begin-trial
   button + gate the leader battle button behind the trial; add `default: true` to the
   `default` entry where it is missing (Ice confirmed missing) (Q2). Ground/Dragon = start
   button only (trial completes on leader defeat), Fairy = altar-start + resolve, Ice/Fire =
   start + finish-plate.
6. **Edit the 5 leader character files** `dialog-src/characters/shrine/<type>_shrine_leader.json`:
   add the Q3 `on_win` fragment line (`memory/shrine/first_keeper`, latched by `shrine_frag_seen`).
7. **Add 1 pilgrim character + dialog** (Q4): `dialog-src/characters/shrine/shrine_pilgrim.json` +
   `dialog-src/dialog/shrine_pilgrim.json`.
8. **Edit the 5 town hubs** (Q5): add the `shrine_rumor_button` to each of
   `mystic_guide` / `kalahar_guide` / `ryujin_guide` / `nifl_guide` / `scorchspire_guide`
   dialog (or the town nurse if that is the arrival hub).
9. **Add datapack functions:**
   - `function/memory/shrine/first_keeper.mcfunction` (Q3)
   - `function/sidequest/shrine/five_keepers_reward.mcfunction` (Q4)
   - `function/sidequest/rumors/shrine_{fairy,ground,dragon,ice,fire}.mcfunction` (Q5, ×5)
   - *(optional)* `function/sidequest/shrine/<type>_trial_help.mcfunction` (Q2, ×5)
   All macro-safe: no `"` in text, no `'`/`%` in delivered lines.
10. **Add register stages** to `dialog-src/registers/quest_targets.json`: `q.side_shrine_<type>`
    ×5 (Q6, slots 52–56) + `q.side_shrines_capstone` (Q4, slot 51). Ensure `#shrines` render
    scratch is populated by the sidebar tick (mirror the `#board` pattern). **Verify slots
    44–56 are free** (13_nobles_gating claims 44–50; this unit takes 51–56 — no overlap).
11. **Confirm the crystal-launch repoint** (owned by `13_nobles_gating` §Build-step-5): the
    Fire/Ground/Ice/Dragon `ShrineCrystalItem` runs `function
    cobblemon_initiative:noble/<id>/crystal_launch` (raises the noble) instead of a flat L70
    `spawnpokemon`. Fairy keeps its `spawnpokemon` path (no Xerneas noble) but retunes the level
    to the gate cap (Open Q2). **Java change — flag to mod-side backlog if authoring-only pass.**
12. **Place finish-line props** (builder): Ice/Fire parkour finish plate/command block; Fairy
    altar prop at `~[957,-7,2715]`. PROPOSED coords in Q2 — builder confirms.
13. **Retune leader teams** (audit §9 Open-Q4) — Fairy down from +11, Fire up from −3; teams
    only, never the cap ladder. Bring cultist rung-4 to the +2 cultist band in the same pass.
14. **Compile:** `scripts/content_compile` → `scripts/update_preset_index` →
    `scripts/generate_npc_function`. Empty-`{}` warnings on the RCT cultist stubs are expected
    staged-content flags (ENGINE_FINDINGS) unless you also populate
    `data/rctmod/trainers/<type>_shrine_cultist_*.json` from the inline teams (audit §6).

---

## Open questions for showrunner

1. **Crystal-launch repoint (RESOLVED, confirm sign-off).** `13_nobles_gating` §Build-step-5
   already made the ruling: Fire/Ground/Ice/Dragon crystals are repointed to
   `function cobblemon_initiative:noble/<id>/crystal_launch` (portable noble launchers), Fairy
   keeps a plain Xerneas spawn (no noble config). This closes the old double-crystal / flat-L70
   collision. The only open item is **the Java edit sign-off** (swap `spawnpokemon` for the
   function in `ShrineCrystalItem.scheduleSpawn`) — approve for the mod-side backlog.
2. **Fairy crystal spawn level vs cap.** The Fairy crystal (legacy spawn path) currently spawns a
   flat L70 Xerneas regardless of the player's cap-37 gate. Clamp the spawn to a cap-appropriate
   level (fine, matches the noble cap-under rule the other four now follow) or ship it as a
   hold-until-cap trophy? Recommend clamp for consistency. (Audit §8 gotcha; only affects Fairy
   now that the other four go through the gated noble engine.)
3. **Leader retune targets.** Set the shrine-superboss rule (proposed ace = gate-cap **+4..+6**,
   cultists at gate-cap **+2**). Fairy (+11) and Fire (−3) both violate it. Confirm the band so
   the retune (leaders + cultist rung-4) is one pass. (Audit §9 Open-Q1.)
4. **Fairy tests strictness.** Ship the full shiny + solo + nicknamed + well-fed + bonded-lead,
   win-with-only-it gauntlet (peak stream content, brutal on a Nuzlocke) or soften to a subset?
   The `fairy_tests` code supports all five checks; the question is difficulty for a hardcore run.
5. **Ground/Dragon leader Doubles.** Confirmed `GEN_9_DOUBLES` for Terran + Draconis (and hydra
   stages 1–2). The Kalahar and Ryujin breadcrumbs (Q5) now carry the doubles warning in-line —
   confirm that is the surfacing you want, or add a guide line too.
6. **Slot allocation.** This unit takes register slots **51–56** (capstone 51, five shrines
   52–56) to sit below `13_nobles_gating`'s **44–50**. Confirm no third unit is queued for 51–56
   before locking.
