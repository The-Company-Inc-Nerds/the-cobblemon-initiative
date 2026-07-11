# 07 — Nifl Town (Gym 9, Ice) — Quest Plan

> Slug `07_nifl_town`. Implementation-ready spec for the **town / side-quest layer** of Nifl
> Town. The gym PvP ladder (`nifl_leader`, `nifl_guide`, `nifl_trainer_1..4`,
> `nifl_jr_apprentice`, `nifl_apprentice`, the three passive `nifl_sentry_*`) **already exists**
> in `dialog-src/characters/gym/` — this plan does **not** re-author it. Everything below is the
> rumor hub, three side quests, and the town-service NPCs that dress the coldest gym on the
> mountain. Design source: `docs/roadmap/11_nifl_town.md` (this plan supersedes it as the
> copy-paste-compile version); gold-standard idiom copied from `fisherman_genji` /
> `sq_beekeeper_tomo` / `agent_yield_lead`.

---

## 1. Overview

**The town/set-piece.** Nifl is a frozen, isolated town where *nothing rots* — the loneliest
stretch of the route, reached from the south-west via **R14 Frostveil Pass**. The town is a
**preservation vault**: the one place the Company could never scrub the founder's face, because
the cold keeps everything on file. Boreas already says it to the player's face in his
already-beaten line (*the cold remembers what the warm world chose to forget about you*) and in
his post-defeat dialog (*it kept your name long before Nifl ever saw your face; the warm world chose
to forget you on purpose*). The marquee side quest turns those lines into a screen — a Company
**cold-storage archive** where the deletion never took, and the player thaws out a Verified Trust
portrait of themselves with the face burned to a hole. **frag_9 They emptied you** rendered as an
artifact.

**Band context.**

| Field | Value |
|-------|-------|
| Gym / type | Gym 9, Ice; leader **Boreas** (`nifl_leader`) |
| Act window | **3** (post-HQ raid / post-Acting-CEO-DJ, pre-Royal-League) |
| Level cap on **arrival** | **68** (Ryujin's unlock) — Nifl leader defeat unlocks the **74** cap. NOTE — if the showrunner runs Nifl at **entry-cap 74** (some briefs read it that way), the arrival cap is 74 and side bands 66–68 stay legal either way; author to the conservative 68 floor |
| Leader ace | **70** (entry-cap 68 + 2 — fought underleveled). Any side battle **must be ≤ 68** (arrival cap) or opt-in |
| `cd_instability` | **25** (flat, stabilized after DJ fell) — economy voice is *late / propaganda-decay*, not Act-1 cheer |
| `memory_fragment` | **9** on leader defeat — frag_9 canon is **They emptied you** (LORE_BIBLE §canon table). SQ1 renders it as the burned-face portrait; the actionbar sting rhymes it (*They emptied you. The cold kept the shape of what they took.*) (leader wiring already exists) |
| Recognition tier | **LATE** — some Company personnel **stand down** rather than fight the founder; civilians never recognize; Boreas recognizes *eerily* (prophecy-of-the-cold, not Company alarm) |

**The arc job.** Nifl is the bleak hinge of Act 3. frag_9 is the most explicit circling of the
Founder reveal in the whole run — *the amnesia and the coup become one thing: they took the memory
ON PURPOSE.* The side layer makes that legible three ways: an **archive** that proves the deletion
was deliberate and preserved (SQ1), a **defector** who recognizes the founder and grieves rather
than fights (SQ2, the first on-screen stand-down), and a **memory-thaw** atmosphere piece that
lets the ice "give back" small kept things (SQ3). None of it names the Founder — it circles, it
never closes (LORE §"fragments 7–9 circle it, never close it"; that reveal is reserved for
post-Royal-League).

**Place on the route.** In from **R14 Frostveil Pass** (Ryujin behind you). Nearby: **Ice Shrine**
[3729 1980] and **Frostfallow farm** [3066 2478]. Out toward gym 10 **Scorchspire** via **R15
Cinderfall Descent** — with the **Royal League** now close on the far side of the last badge.
Whiteout Approach gimmick (3 passive-sight Frost Sentinels) lives on the gym ladder, already built.

---

## 2. Cast

Town/side-layer NPCs only (gym ladder already exists). Coords are **PROPOSED** — builder-confirm
against walkable blocks. Vertical split: town/lake floor **y≈64–70**, gym palace **y≈112**.
`nifl_town` zone polygon x∈[3432,3706] z∈[1842,2169]. All new bodies are **placement-latched**
(`placement:{}`, no `uuid`) unless a CSV body is adopted at the placement pass.

| id | display_name | role | one-line concept | placement anchor (x,y,z) |
|----|--------------|------|------------------|--------------------------|
| `nifl_nurse` | Nurse Sabra | healer | Lower-town Center; paid heal on the posted rate (`economy/heal_paid`); ZERO recognition | 3470, 66, 2000 |
| `nifl_martkeeper` | Kestrel Vane | merchant | Default CobbleDollars shop; tier `badge_9` after leader; ZERO recognition | 3480, 66, 2010 |
| `nifl_keeper_vetra` | Keeper Vetra | civilian / elder | **RUMOR HUB** + SQ3 giver; frozen-lake elder in Boreas' cold tradition; ZERO recognition of the founder (feels only the propaganda decay) | 3500, 65, 1960 |
| `nifl_cold_auditor` | Auditor Corvin | quest_giver | SQ1 giver; burned-out Company records clerk who stayed at his post after the scrub — still files everything, no longer believes it; recognizes late | 3520, 68, 1982 |
| `nifl_records_officer` | Records Officer Halden | villain_grunt | SQ1 gate battle **or** stand-down; a late-tier archive guard who may place the founder's face and step aside | 3540, 68, 1975 |
| `nifl_warrant_officer` | Warrant Officer Dain | villain_grunt | SQ2 giver + branching battle; a 20-year Company veteran freezing at the Frostgate who recognizes the player mid-sentence | 3450, 70, 2030 |
| `nifl_civilian_frost` | Old Halla | civilian | Optional cold-town color; back-echo line; ZERO recognition | 3462, 66, 2016 |

> `nifl_records_officer` and `nifl_warrant_officer` use `role:"villain_grunt"` (→ recipe
> `villain_grunt`) but their SQ battles are **opt-in / decline-able** below (see per-quest
> REWARD/BALANCE). Their trainer teams are RCT files to CREATE (§6).

---

## 3. Quests

Three quests, deliberately varied in pacing: one heavy (archive), one branching-combat with a
stand-down branch (Frostgate), one battle-free atmospheric walk (lanterns). Plus the rumor-hub
wiring on Vetra. All macro-delivered text avoids `"` and `'` (ENGINE RULE).

Common gate: every side quest is **available on arrival**, i.e. gated on `defeated_ryujin_leader`
(you enter Nifl from Ryujin). No quest gates on the Nifl leader — you can do them before Boreas.

---

### 3.0 Rumor Hub — Keeper Vetra points at the town

**Concept.** House-style rumor hub. Vetra is the frozen-lake elder; her default entry surveys the
three side quests and points at each one *only while it is not yet done* (gated on each quest's
not-done tag). She is also the SQ3 giver (below), so her tree carries both.

**Forward hook it plants.** Names **Scorchspire** and **R15 Cinderfall Descent**, and that the
**Royal League** waits just past the last badge.

**Back-echo it references.** The stabilized currency (`cd_instability=25`) and the fall of the
**Acting CEO** — "the money went quiet a while back, they say someone in the capital lost their
chair." (Post-HQ back-echo; civilian voice, no founder recognition.)

READY-TO-PASTE character JSON — see **3.3** (Vetra carries both the hub and SQ3 in one tree).
The hub entries live as the highest-priority non-lantern entries of `dialog:nifl_keeper_vetra`.

---

### 3.1 SQ1 — Cold Storage *(marquee; the frag_9 payoff)*

**Concept.** The Company cold-storage vault preserved what head office deleted. Auditor Corvin —
who still files everything and no longer believes a word of it — wants three frozen **ledger cores**
thawed and read before "Retention" purges the room. Core 1 is a liberated-field ledger; core 2 is
two stage-managed lies in one folder; core 3 is **the portrait** — a "Verified Trust" founder photo,
posture unmistakable, face a burned hole. Reading it fires an actionbar line that rhymes with frag_9.
The **Records Officer** stands between the player and core 3: fight him **or** talk him into placing
the face and stepping aside (late-tier stand-down). The cores are a **gated interaction chain on
Corvin's own entity** (beekeeper `sting_seal_*` pattern) — zero prop bodies, zero block edits.

**Forward hook.** Corvin files the portrait under *unresolved* and tells the player the cold will
still be holding it when they come back **with a name** — and that the next furnace-town, Scorchspire,
will not keep anything this well. (Plants the Founder reveal without naming it; plants gym 10.)

**Back-echo.** Core 1 shows a **liberated field** still printing pre-monopoly wheat prices (references
the Wheat War field-liberation arc), and core 2's memos are the same *"the founder retired" / "there
was never a founder"* propaganda the player has been reading on scrubbing artifacts since Act 1.

#### Character — `nifl_cold_auditor`

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "nifl_cold_auditor",
  "display_name": "Auditor Corvin",
  "role": "quest_giver",
  "act": "3",
  "location": "Nifl Town - Cold Storage Vault",
  "recognition_tier": "late",
  "_comment": "COLD STORAGE (SQ1, frag_9 payoff). Burned-out Company records clerk who stayed after the scrub; still files, no longer believes it. The three ledger cores are a gated interaction chain ON THIS ENTITY (beekeeper sting_seal_* walk) - zero prop bodies, zero block edits. Core 3 (the portrait) is gated behind nifl_archive_open, set by clearing OR talking down nifl_records_officer. NO Pokemon gift (endgame budget). PLACEMENT at the vault mouth 3520/68/1982 - builder confirm the vault read exists. Latch-placed, no uuid.",
  "recipe": "civilian",
  "dialog": "dialog:nifl_cold_storage",
  "movement": {
    "objective": "ambient_stationary_look"
  },
  "placement": {
    "x": 3520,
    "y": 68,
    "z": 1982
  }
}
```

#### Character — `nifl_records_officer` (SQ1 gate: fight OR stand down)

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "nifl_records_officer",
  "display_name": "Records Officer Halden",
  "role": "villain_grunt",
  "act": "3",
  "location": "Nifl Town - Cold Storage Vault, inner stacks",
  "recognition_tier": "late",
  "_comment": "SQ1 gate guard. Late-tier: he can PLACE the founder's face and step aside (stand-down) instead of fighting. Battle is OPT-IN and CAP-LEGAL (team band 66-68, under the 68 arrival cap). Declining costs a decline_fee (paid-decline probe). Win OR talk-past both set nifl_archive_open (via on_win + the dialog cite button). despawn_on_win removes his body so the stacks open. RCT team rctmod:nifl_records_officer to CREATE (Bronzong/Klinklang/Porygon2/Beartic, 66-68 SINGLES).",
  "trainer": "nifl_records_officer",
  "recipe": "villain_grunt",
  "dialog": "dialog:nifl_records_officer",
  "movement": {
    "objective": "ambient_stationary_look"
  },
  "battle": {
    "trainer": "nifl_records_officer",
    "type": "villain_grunt",
    "format": "GEN_9_SINGLES",
    "prize": 480,
    "decline_fee": 120,
    "defeat_tag": "defeated_nifl_records_officer",
    "despawn_on_win": true,
    "win_line": "Retention will note the loss. Filed under - well. There is no line for what you are.",
    "lose_line": "The room is Company property. So is the face on the wall, and so, once, were you.",
    "already_beaten_line": "The stacks are open. I have nothing left to guard and less to say.",
    "on_win": [
      "tag @1 add nifl_archive_open"
    ]
  },
  "placement": {
    "x": 3540,
    "y": 68,
    "z": 1975
  }
}
```

#### Dialog — `dialog:nifl_cold_storage` (Corvin's core-walk chain)

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "nifl_cold_storage",
  "type": "STANDARD",
  "_comment": "COLD STORAGE (SQ1). Chain on Corvin: cores 1->2->3 via open_dialog, per-step tags nifl_core_1/_2/_3. Core 3 gated on nifl_archive_open (set by defeating OR talking down nifl_records_officer). Core 3 read fires the frag_9-rhyme actionbar + reward (nifl_archive_read). Priorities: post_read(40) > resume gates > default(10). Manual-only pages (-1) reached via open_dialog. No double-quotes, no apostrophes in any macro-delivered line.",
  "entries": [
    {
      "label": "post_read",
      "name": "Corvin - filed under unresolved",
      "priority": 40,
      "gate": { "tag": "nifl_archive_read" },
      "say": [
        "I filed the portrait under unresolved. The cold will keep it. Come back when you have a name to put where the face was - Scorchspire will not keep anything this well, and the League is closer than the warmth in your fingers.",
        "Thirty years I stamped what head office told me was true. This is the first drawer I believe."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Let the cold hold it", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "resume_three",
      "name": "Corvin - the last core",
      "priority": 22,
      "gate": { "tag": "nifl_core_2", "not_tag": "nifl_archive_read" },
      "say": [
        "Two cores read. The third is deeper in the stacks, behind the officer. Get past him and I will thaw it for you."
      ],
      "buttons": [
        { "label": "resume_button", "text": "Go to the third core", "actions": [ { "do": "open_dialog", "label": "core_three" } ] },
        { "label": "leave_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "resume_two",
      "name": "Corvin - the second core",
      "priority": 20,
      "gate": { "tag": "nifl_core_1", "not_tag": "nifl_core_2" },
      "say": [
        "One core read. The second is a folder someone rewrote twice and never threw out. Thaw it and see what the story used to be."
      ],
      "buttons": [
        { "label": "resume_button", "text": "Go to the second core", "actions": [ { "do": "open_dialog", "label": "core_two" } ] },
        { "label": "leave_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Auditor Corvin",
      "priority": 10,
      "default": true,
      "gate": { "not_tag": "nifl_core_1" },
      "say": [
        "Corvin. I audit a room that head office swears was emptied years ago. It was not. The cold does not delete, it only files - and it has been filing since before the memos told me to stop. Three cores are frozen in these stacks. Thaw them with me before Retention comes to purge the room, and read what the warm world decided you should not."
      ],
      "buttons": [
        { "label": "walk_button", "text": "Walk the stacks", "actions": [ { "do": "open_dialog", "label": "core_one" } ] },
        { "label": "later_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "core_one",
      "name": "The first core - a field ledger",
      "priority": -1,
      "say": [
        "First core. A field ledger, iced solid mid-page. It still prints the old wheat price - the honest one, from before the intake fences went up. Someone liberated this field and the cold kept the proof. The numbers do not lie the way the memos do."
      ],
      "buttons": [
        {
          "label": "read_one_button",
          "text": "Thaw and read the field ledger",
          "actions": [
            { "do": "command", "cmd": "tag @s add nifl_core_1", "as_player": true },
            { "do": "announce", "text": "Core one read. A field the Company lost, priced the way it was before they came.", "as": "actionbar" },
            { "do": "open_dialog", "label": "core_two" }
          ]
        },
        { "label": "step_back_button", "text": "Step back", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "core_two",
      "name": "The second core - two lies, one folder",
      "priority": -1,
      "say": [
        "Second core. A memo stamped over an older memo. The top one says the founder retired. The one underneath, older, says there was never a founder. Two stories, one folder, both in the same hand. Somebody could not decide which lie to keep, so the cold kept both."
      ],
      "buttons": [
        {
          "label": "read_two_button",
          "text": "Thaw and read the folded memos",
          "actions": [
            { "do": "command", "cmd": "tag @s add nifl_core_2", "as_player": true },
            { "do": "announce", "text": "Core two read. Retired, then never real. The story was managed in stages.", "as": "actionbar" },
            { "do": "open_dialog", "label": "core_three" }
          ]
        },
        { "label": "step_back_button", "text": "Step back", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "core_three",
      "name": "The third core - the portrait",
      "priority": -1,
      "say": [
        "Third core - deepest in the stacks, and Records Officer Halden is standing over it. He will not let a stray trainer thaw that drawer. Whatever is in it, head office wanted it kept and unseen at the same time.",
        "The officer has stepped aside. The last core thaws slow. A framed portrait, a Verified Trust plate along the bottom, the posture unmistakable - shoulders you have seen in a mirror. Where the face should be, the ice holds a burned hole. They did not lose this man. They emptied him."
      ],
      "buttons": [
        {
          "label": "face_officer_button",
          "text": "Deal with the Records Officer first",
          "gate": { "not_tag": "nifl_archive_open" },
          "actions": [
            { "do": "announce", "text": "Halden is between you and the last drawer. Settle it, then come back.", "as": "actionbar" },
            { "do": "close" }
          ]
        },
        {
          "label": "read_three_button",
          "text": "Thaw and read the portrait",
          "gate": { "tag": "nifl_archive_open", "not_tag": "nifl_archive_read" },
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/nifl/archive_reward", "as_player": true },
            { "do": "open_dialog", "label": "post_read" }
          ]
        }
      ],
      "no_goodbye": true
    }
  ]
}
```

#### Dialog — `dialog:nifl_records_officer` (fight or stand down)

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "nifl_records_officer",
  "type": "STANDARD",
  "_comment": "SQ1 gate. Late-tier stand-down: the cite button places the founder's face and steps him aside (sets nifl_archive_open, no battle). Battle button is OPT-IN and CAP-LEGAL (team 66-68 under the 68 cap). Decline button routes the paid-decline probe (decline_fee 120, charge via pay-probe - fail-soft). All three terminal once nifl_archive_open is set.",
  "entries": [
    {
      "label": "stood_aside",
      "name": "Halden - the guard who placed the face",
      "priority": 30,
      "gate": { "tag": "nifl_archive_open" },
      "say": [
        "The drawer is yours. I placed the face the moment you walked in - the plate, the shoulders. I was told there was never a founder. I stopped being able to say it out loud somewhere around the third winter here."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Say nothing", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Records Officer Halden",
      "priority": 10,
      "default": true,
      "say": [
        "Records Officer, cold detail. This drawer is retained under Company seal and you are not on the access list. I could recite the paragraph. Or you could look at me and tell me you do not already know whose face is in it.",
        "You match a portrait I am not allowed to have seen. Twenty years I filed the impossible. Give me a reason to keep filing, or give me a reason to stop."
      ],
      "buttons": [
        {
          "label": "cite_button",
          "text": "Meet his eyes and let him place the face",
          "actions": [
            { "do": "command", "cmd": "tag @s add nifl_archive_open", "as_player": true },
            { "do": "announce", "text": "STOOD DOWN", "as": "title", "color": "aqua" },
            { "do": "announce", "text": "A records officer unlatches the drawer and steps into the cold.", "as": "subtitle" },
            { "do": "open_dialog", "label": "stood_aside" }
          ]
        },
        {
          "label": "battle_button",
          "text": "Make him move - challenge the officer",
          "actions": [ { "do": "battle" } ]
        },
        {
          "label": "decline_button",
          "text": "Buy your way past - pay to skip the guard",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:route/decline_nifl_records_officer", "as_player": true },
            { "do": "close" }
          ]
        }
      ],
      "no_goodbye": true
    }
  ]
}
```

**DATAPACK NEEDS (SQ1)**
- `function/sidequest/nifl/archive_reward.mcfunction` — run as player from the portrait button: pay
  `function economy/payout {amount:600}`, `give @s cobblemon:rare_candy 5`,
  `loot give @s loot cobblemon_initiative:npc_gift/training_standard`, `tag @s add nifl_archive_read`,
  and a `title @s actionbar` frag_9-rhyme sting (e.g. gold "They emptied you." + gray "The cold kept
  the shape of what they took."). NO Pokémon gift. Guard with
  `unless entity @s[tag=nifl_archive_read]` so it pays once.
- `function/route/decline_nifl_records_officer.mcfunction` — **auto-generated by `content_compile`
  from `battle.decline_fee`** (mirror `route/decline_sq_genji_wager`): `store result` on
  `cobbledollars pay @s 120` (0 = broke → the battle fires as fallback; 100 → charge + set
  `declined_nifl_records_officer` + `nifl_archive_open` + a Verified Charge: 120 CD receipt). Broke
  path lets the battle fire as fallback — set `nifl_archive_open` on the paid branch only. Fairness
  floor is satisfied structurally: this is Act 3 (8+ badges, so the player provably has a team — no
  starter-only case exists this late), the fight is the same opt-in `battle_button` the player could
  always choose, and the stand-down (cite) path is a free non-combat gate open, so no one is ever
  cornered into an unwinnable forced fight.

> NOTE — decline semantics: the decline **opens the archive gate** (paid path adds `nifl_archive_open`),
> so paying is a real skip, not a dead end. Charge via **pay-probe** (balance-gated; fail-soft).

**QUEST_TARGETS entry (SQ1)** — add to `registers/quest_targets.json`:

```json
{
  "holder": "q.side_archive",
  "name": "Cold Storage",
  "slot": 58,
  "stages": [
    {
      "if_tags": ["nifl_core_2"],
      "not_tags": ["nifl_archive_read"],
      "label": "Get past Halden to the last core",
      "target": { "npc": "nifl_records_officer" },
      "note": "Core 3 is gated behind the Records Officer; point at his post until nifl_archive_open."
    },
    {
      "if_tags": ["nifl_core_1"],
      "not_tags": ["nifl_core_2"],
      "label": "Thaw the ledger cores with Corvin",
      "target": { "npc": "nifl_cold_auditor" },
      "note": "Escorted core-walk on Corvin's entity; his vault mouth anchors the line."
    },
    {
      "if_tags": ["defeated_ryujin_leader"],
      "not_tags": ["nifl_core_1", "nifl_archive_read"],
      "label": "Ask Auditor Corvin about the vault",
      "target": { "npc": "nifl_cold_auditor" },
      "note": "Available from arrival; drops off once the walk starts (nifl_core_1) or is read."
    }
  ]
}
```

**REWARD/BALANCE (SQ1).** Face **600 CD** via `economy/payout` (≈**564** landed at idx25) +
`rare_candy ×5` + training gift. Battle is **opt-in and cap-legal** (Records Officer team band
**66–68**, under the 68 arrival cap — winnable underleveled, not free). **Decline** costs **120 CD**
(pay-probe, fail-soft) and opens the gate; **stand-down** (cite button) opens the gate for free — the
late-tier mercy beat. No Pokémon gift (endgame budget).

---

### 3.2 SQ2 — Stand Down at the Frostgate *(recognition / moral branch — the defector)*

**Concept.** The **LATE recognition beat** the brief asks for. Warrant Officer Dain is a 20-year
Company veteran freezing at the Frostveil/Nifl boundary who **recognizes the player mid-sentence** —
veteran alarm collapsing into grief. He has orders to hold the gate and twenty years of loyalty to a
face he was told to forget, and he gives the player the choice out loud. **Branch A (fight):**
GEN_9_DOUBLES, Dain + a Frostgate grunt hosted on his entity (`agent_yield_lead` pattern).
**Branch B (stand down):** cite his own recognition back at him — he **salutes and walks into the
snow rather than raise a hand against the founder**. The first on-screen defector; recognition made
playable; grief, not a fight.

**Forward hook.** Dain's stand-down line hands the player a **Frostgate token** and names the way
forward — the pass out the far side climbs to **Scorchspire down R15 Cinderfall Descent**, and past
that "the ones in the capital wear crowns" (the Royal League).

**Back-echo.** Dain served long enough to have **saluted the founder's portrait before it came down**
and to have watched the **currency go quiet when the acting man lost his chair** (post-HQ back-echo).

#### Character — `nifl_warrant_officer`

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "nifl_warrant_officer",
  "display_name": "Warrant Officer Dain",
  "role": "villain_grunt",
  "act": "3",
  "location": "Nifl Town - Frostgate (Frostveil boundary)",
  "recognition_tier": "late",
  "_comment": "STAND DOWN AT THE FROSTGATE (SQ2). 20-year veteran who recognizes the player mid-sentence. Branch A: GEN_9_DOUBLES (Dain + Frostgate grunt hosted on THIS entity via the battle block, agent_yield_lead pattern), OPT-IN and CAP-LEGAL (band 66-68 under the 68 cap). Branch B: stand down - he salutes and walks into the snow (sets nifl_warrant_stood_down + Frostgate token, no battle - the first on-screen defector). Both branches set nifl_frostgate_clear (terminal). Decline (pay to bow out) via decline_fee probe. despawn_on_win removes his body; the branch-B walk-off is a title beat + tag (body stays until placement pass relocates or removes it). RCT team rctmod:nifl_warrant_officer to CREATE (DOUBLES: Dain Weavile/Walrein + grunt Glalie/Sneasel, 66-68).",
  "trainer": "nifl_warrant_officer",
  "recipe": "villain_grunt",
  "dialog": "dialog:nifl_frostgate",
  "movement": {
    "objective": "ambient_stationary_look"
  },
  "battle": {
    "trainer": "nifl_warrant_officer",
    "type": "villain_grunt",
    "format": "GEN_9_DOUBLES",
    "prize": 480,
    "decline_fee": 140,
    "defeat_tag": "defeated_nifl_warrant_officer",
    "despawn_on_win": true,
    "win_line": "Gate is yours. Twenty years and I lose it to a face I was ordered to forget. Fitting.",
    "lose_line": "Hold the line, they said. I held it. It did not hold me.",
    "already_beaten_line": "The gate is open and my post is closed. Go on.",
    "on_win": [
      "tag @1 add nifl_frostgate_clear"
    ]
  },
  "placement": {
    "x": 3450,
    "y": 70,
    "z": 2030
  }
}
```

#### Dialog — `dialog:nifl_frostgate` (branch A / B / decline)

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "nifl_frostgate",
  "type": "STANDARD",
  "_comment": "SQ2 Frostgate. Branch A battle (DOUBLES, opt-in, cap-legal) -> defeated + nifl_frostgate_clear via on_win. Branch B stand down -> nifl_warrant_stood_down + token + payout + nifl_frostgate_clear (no battle). Decline (pay to bow out) via the paid-decline probe. Priorities: clear(30, either branch done) > default(10). No double-quotes, no apostrophes in macro lines.",
  "entries": [
    {
      "label": "cleared",
      "name": "Dain - post closed",
      "priority": 30,
      "gate": { "tag": "nifl_frostgate_clear" },
      "say": [
        "The gate is open behind me and I am done standing at it. Whatever you are walking toward - Scorchspire is down the Cinderfall, and the ones in the capital wear crowns - it is warmer than here. Go be warm.",
        "I saluted a portrait once, before they took it down. I will not tell anyone I saluted the man. The cold keeps secrets. So do I."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Walk on", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Warrant Officer Dain",
      "priority": 10,
      "default": true,
      "say": [
        "Hold there. This gate is Company-held and my orders are to - to hold it, yes, hold it against - I know that walk. I saluted that walk for twenty years before they took the picture off the wall and told us there had never been anyone in it. So. I have orders. And I have eyes. You choose which one I follow, because I no longer can."
      ],
      "buttons": [
        {
          "label": "fight_button",
          "text": "Take the gate by force - double battle",
          "actions": [ { "do": "battle" } ]
        },
        {
          "label": "standdown_button",
          "text": "Tell him you know the face he saluted",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/nifl/frostgate_standdown", "as_player": true },
            { "do": "open_dialog", "label": "cleared" }
          ]
        },
        {
          "label": "decline_button",
          "text": "Pay the toll and slip past quietly",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:route/decline_nifl_warrant_officer", "as_player": true },
            { "do": "close" }
          ]
        }
      ],
      "no_goodbye": true
    }
  ]
}
```

**DATAPACK NEEDS (SQ2)**
- `function/sidequest/nifl/frostgate_standdown.mcfunction` — run as player from the stand-down button:
  `tag @s add nifl_warrant_stood_down`, `tag @s add nifl_frostgate_clear`, grant the Frostgate token
  (`give @s cobblemon:ice_stone 1` as the keepsake), `function economy/payout {amount:260}`, and a
  `title @s subtitle` / actionbar beat — gold "STOOD DOWN" + gray "A veteran salutes and walks into
  the snow." Also `easy_npc delete @2` is **not** available here (dialog runs as player, not onwin) —
  instead leave the body for the placement pass to relocate/remove, or add an optional
  `kill @e[type=easy_npc:humanoid,name=Warrant Officer Dain,distance=..6,limit=1]` if a clean walk-off
  is wanted (builder call; keep out of the paid/fight paths). Guard once with
  `unless entity @s[tag=nifl_frostgate_clear]`.
- `function/route/decline_nifl_warrant_officer.mcfunction` — **auto-generated from `decline_fee`**
  (mirror `decline_sq_genji_wager`): pay-probe 140 CD; paid → `declined_nifl_warrant_officer` +
  `nifl_frostgate_clear` + receipt; broke → the DOUBLES battle fires as fallback. Fairness floor is
  structural (same as SQ1): Act 3 guarantees a team, the fallback is the identical opt-in
  `fight_button`, and the stand-down path is a free non-combat clear — no unwinnable forced fight.

**QUEST_TARGETS entry (SQ2)** — add to `registers/quest_targets.json`:

```json
{
  "holder": "q.side_frostgate",
  "name": "Stand Down at the Frostgate",
  "slot": 57,
  "stages": [
    {
      "if_tags": ["defeated_ryujin_leader"],
      "not_tags": ["nifl_frostgate_clear"],
      "label": "Settle the Frostgate with Warrant Officer Dain",
      "target": { "npc": "nifl_warrant_officer" },
      "note": "Single objective: fight, stand him down, or pay past. Dain's Frostgate post anchors the line; drops off on nifl_frostgate_clear."
    }
  ]
}
```

**REWARD/BALANCE (SQ2).** **Branch A (fight):** prize **480 CD** (flat, tbcs onwin), DOUBLES team band
**66–68** (opt-in, cap-legal under 68). **Branch B (stand down):** **260 CD**
via `economy/payout` (≈244 landed) + `ice_stone ×1` (the Frostgate token keepsake) — smaller, because
you skip the fight. **Decline (pay past):** **140 CD** pay-probe, fail-soft, sets clear. All three
paths set `nifl_frostgate_clear`. No forced whiteout (opt-in battle; decline fallback only for the
paid button).

---

### 3.3 SQ3 — The Long Memory (Lantern Walk) *(battle-free; the memory-thaw motif; rumor hub)*

**Concept.** Vetra, the frozen-lake elder in Boreas' cold tradition, walks the player lantern to
lantern around the frozen lake. Four memory-lanterns have gone dark; relight them and the ice
**gives back** the small memories it has been keeping — a wedding, a lost dog, a first snow, and one
that is **not a townsperson's at all**. Lantern 4 whispers a founder-adjacent fragment (a name
half-heard, then gone) — a *light* echo of frag_9, never a reveal. Pure interaction chain on Vetra's
entity (no props, no battle). Vetra's tree **also carries the rumor hub** (§3.0): her top entries
point at SQ1/SQ2 while each is not-done.

**Forward hook.** Vetra tells the player the ice is patient and holds **one more memory for them than
for anyone else in town** — and that they will not find that in the fire-country ahead
(**Scorchspire / Cinderfall**), where nothing keeps.

**Back-echo.** Lantern 1 (the wedding) is dated by the **badges** the player wears; the hub lines
reference the **liberated fields** and the **quiet money** since the capital lost its acting chair.

#### Character — `nifl_keeper_vetra` (rumor hub + SQ3 giver, one tree)

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "nifl_keeper_vetra",
  "display_name": "Keeper Vetra",
  "role": "elder",
  "act": "3",
  "location": "Nifl Town - Frozen Lake Shore",
  "recognition_tier": "late",
  "_comment": "RUMOR HUB + LANTERN WALK (SQ3). Vetra is the frozen-lake elder in Boreas cold tradition. Her tree does double duty: hub entries (prio 50/48/46) point at SQ1/SQ2/SQ3 while each is not-done; the lantern chain (nifl_lantern_1..4 via open_dialog) runs battle-free with per-step vignettes. Lantern 4 is a founder-adjacent whisper (light echo of frag_9, NEVER a reveal). CIVILIAN/ELDER: she recognizes the KID-in-the-cold nobody, not the founder - zero CEO recognition (public scrubbing worked; she only feels the propaganda decay). Latch-placed, no uuid. PLACEMENT 3500/65/1960 - lake shore, builder confirm.",
  "recipe": "civilian",
  "dialog": "dialog:nifl_keeper_vetra",
  "movement": {
    "objective": "ambient_stationary_look"
  },
  "placement": {
    "x": 3500,
    "y": 65,
    "z": 1960
  }
}
```

#### Dialog — `dialog:nifl_keeper_vetra` (hub + lantern chain)

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "nifl_keeper_vetra",
  "type": "STANDARD",
  "_comment": "Rumor hub (prio 50/48/46, each gated on a quest not-done tag) + SQ3 lantern walk (default 10 -> chain -1 pages, nifl_lantern_1..4, completion nifl_lanterns_done). Lantern 4 is a founder-adjacent whisper, NOT a reveal. Hub lines plant Scorchspire/Cinderfall/Royal League and back-echo liberated fields + the quiet money. No double-quotes, no apostrophes in macro lines.",
  "entries": [
    {
      "label": "hub_archive",
      "name": "Vetra - the vault still hums",
      "priority": 50,
      "gate": { "tag": "defeated_ryujin_leader", "not_tag": "nifl_archive_read" },
      "say": [
        "You came down the Frostveil, so you have not been to the cold-storage vault yet. Auditor Corvin still keeps it, poor man - files a room head office swears is empty. Ask him what the ice remembers. It remembers more than the living care to."
      ],
      "buttons": [
        { "label": "lantern_button", "text": "Ask about the lake lanterns", "actions": [ { "do": "open_dialog", "label": "lantern_intro" } ] },
        { "label": "leave_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "hub_frostgate",
      "name": "Vetra - the man at the gate",
      "priority": 48,
      "gate": { "all_tags": ["defeated_ryujin_leader", "nifl_archive_read"], "not_tag": "nifl_frostgate_clear" },
      "say": [
        "There is a Company man frozen half to the Frostgate, would not come in for soup nor sense. Warrant Officer, old one. He has been holding that post like it will thaw if he waits. Somebody should tell him the money went quiet when the acting man lost his chair, and nobody is coming to relieve him."
      ],
      "buttons": [
        { "label": "lantern_button", "text": "Ask about the lake lanterns", "actions": [ { "do": "open_dialog", "label": "lantern_intro" } ] },
        { "label": "leave_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "hub_done",
      "name": "Vetra - the patient ice",
      "priority": 46,
      "gate": { "all_tags": ["nifl_archive_read", "nifl_frostgate_clear", "nifl_lanterns_done"] },
      "say": [
        "You have thawed the vault, moved the man at the gate, and walked my lanterns. Nifl has given you what it keeps. Scorchspire is next, down the Cinderfall, and it keeps nothing - fire is honest that way. Past it the capital wears crowns. Go warm, stranger. The ice will hold your one memory until you come back for it."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Thank you, keeper", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Keeper Vetra",
      "priority": 10,
      "default": true,
      "gate": { "not_tag": "nifl_lanterns_done" },
      "say": [
        "Vetra. I keep the lake, or the lake keeps me - hard to say which, this far into the cold. Four memory-lanterns ring the ice and all four have gone dark. Walk them with me and relight them. The ice gives back the small things it holds when the light returns. Some of them are ours. One of them, I think, is yours."
      ],
      "buttons": [
        { "label": "walk_button", "text": "Walk the lantern ring", "actions": [ { "do": "open_dialog", "label": "lantern_intro" } ] },
        { "label": "later_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "lantern_intro",
      "name": "The lantern ring",
      "priority": -1,
      "say": [
        "Four lanterns, four small memories the ice kept. Relight them in any order your feet find. Come, the first is nearest the shore."
      ],
      "buttons": [
        { "label": "l1_button", "text": "Relight the shore lantern", "gate": { "not_tag": "nifl_lantern_1" }, "actions": [ { "do": "open_dialog", "label": "lantern_one" } ] },
        { "label": "l2_button", "text": "Relight the north lantern", "gate": { "tag": "nifl_lantern_1", "not_tag": "nifl_lantern_2" }, "actions": [ { "do": "open_dialog", "label": "lantern_two" } ] },
        { "label": "l3_button", "text": "Relight the deep-ice lantern", "gate": { "tag": "nifl_lantern_2", "not_tag": "nifl_lantern_3" }, "actions": [ { "do": "open_dialog", "label": "lantern_three" } ] },
        { "label": "l4_button", "text": "Relight the far lantern", "gate": { "tag": "nifl_lantern_3", "not_tag": "nifl_lantern_4" }, "actions": [ { "do": "open_dialog", "label": "lantern_four" } ] },
        { "label": "leave_button", "text": "Step back", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "lantern_one",
      "name": "Shore lantern - a wedding",
      "priority": -1,
      "say": [
        "The shore lantern catches. Inside the flame the ice shows a wedding - furs and steam and two people laughing at the cold. It was the winter of your first badges, Vetra says, though how she counts by your badges she does not explain."
      ],
      "buttons": [
        { "label": "light_one_button", "text": "Let the wedding warm and go", "actions": [
          { "do": "command", "cmd": "tag @s add nifl_lantern_1", "as_player": true },
          { "do": "announce", "text": "Shore lantern lit. A wedding, kept in ice, warm again for a moment.", "as": "actionbar" },
          { "do": "open_dialog", "label": "lantern_intro" }
        ] },
        { "label": "step_back_button", "text": "Step back", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "lantern_two",
      "name": "North lantern - a lost dog",
      "priority": -1,
      "say": [
        "The north lantern flares. A dog runs across the old ice and does not come back, and a child stands calling a name the wind took. The ice kept the calling, not the dog. Vetra says the child is grown now and still listens at the shore on quiet nights."
      ],
      "buttons": [
        { "label": "light_two_button", "text": "Let the calling fade kindly", "actions": [
          { "do": "command", "cmd": "tag @s add nifl_lantern_2", "as_player": true },
          { "do": "announce", "text": "North lantern lit. A name called across old ice, and let go.", "as": "actionbar" },
          { "do": "open_dialog", "label": "lantern_intro" }
        ] },
        { "label": "step_back_button", "text": "Step back", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "lantern_three",
      "name": "Deep-ice lantern - a first snow",
      "priority": -1,
      "say": [
        "The deep-ice lantern glows blue. A very small person meets snow for the first time and decides, on the spot and forever, that the world is a good place. The ice kept the deciding. It is the warmest thing in Nifl and it lives at the bottom of the coldest lantern."
      ],
      "buttons": [
        { "label": "light_three_button", "text": "Let the first snow fall again", "actions": [
          { "do": "command", "cmd": "tag @s add nifl_lantern_3", "as_player": true },
          { "do": "announce", "text": "Deep-ice lantern lit. Someone very small, deciding the world is good.", "as": "actionbar" },
          { "do": "open_dialog", "label": "lantern_intro" }
        ] },
        { "label": "step_back_button", "text": "Step back", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "lantern_four",
      "name": "Far lantern - not a townsperson at all",
      "priority": -1,
      "say": [
        "The far lantern is slow to take. When it does, the ice shows nothing you can hold - a coat with a Verified plate, a chair too large, a name half-said in a voice you almost know, and then the wind. This one is not a townsperson at all, Vetra says quietly. The lake has been keeping it for someone who has not come to claim it. Until, perhaps, now."
      ],
      "buttons": [
        { "label": "light_four_button", "text": "Relight it and let the name go by", "actions": [
          { "do": "command", "cmd": "function cobblemon_initiative:sidequest/nifl/lanterns_reward", "as_player": true },
          { "do": "open_dialog", "label": "hub_done" }
        ] },
        { "label": "step_back_button", "text": "Not yet", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS (SQ3)**
- `function/sidequest/nifl/lanterns_reward.mcfunction` — run as player from lantern 4: guard
  `unless entity @s[tag=nifl_lanterns_done]`; `tag @s add nifl_lantern_4`,
  `tag @s add nifl_lanterns_done`, `function economy/payout {amount:300}`,
  `give @s cobblemon:pretty_feather 2` (cosmetic heirloom), and a quiet `title @s actionbar` beat —
  gray "The far lantern holds one more memory for you than for anyone in town." No battle, no
  Pokémon gift, no `nether_star` (economy: nether stars back the currency — never a quest drop).
  JAR-VALIDATE `cobblemon:pretty_feather` at build (swap to `minecraft:snowball ×4` if absent).

**QUEST_TARGETS entry (SQ3)** — add to `registers/quest_targets.json`:

```json
{
  "holder": "q.side_lanterns",
  "name": "The Long Memory",
  "slot": 56,
  "stages": [
    {
      "if_tags": ["nifl_lantern_1"],
      "not_tags": ["nifl_lanterns_done"],
      "label": "Relight the lake lanterns with Vetra",
      "target": { "npc": "nifl_keeper_vetra" },
      "note": "Escorted lantern walk on Vetra's entity; her lake-shore post anchors the line."
    },
    {
      "if_tags": ["defeated_ryujin_leader"],
      "not_tags": ["nifl_lantern_1", "nifl_lanterns_done"],
      "label": "Ask Keeper Vetra about the dark lanterns",
      "target": { "npc": "nifl_keeper_vetra" },
      "note": "Available from arrival; drops off once the walk starts (nifl_lantern_1) or is done."
    }
  ]
}
```

**REWARD/BALANCE (SQ3).** Battle-free. Face **300 CD** via `economy/payout` (≈282 landed at idx25) +
`pretty_feather ×2` cosmetic keepsake. The vignettes are the reward. No cap concerns (no battle).

---

### 3.4 Town services (no quest — rumor-hub support)

**Nurse Sabra (`nifl_nurse`)** — paid healer on the posted rate. Reuse the shipped
`dialog:hz_nurse` idiom (heal button → `function economy/heal_paid`), or a trimmed
`dialog:nifl_nurse` that is just heal + flavor. Civilian, **ZERO recognition**.

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "nifl_nurse",
  "display_name": "Nurse Sabra",
  "role": "healer",
  "act": "3",
  "location": "Nifl Town - Pokemon Center",
  "recognition_tier": "late",
  "_comment": "Paid Nifl nurse. Heal rides the shipped economy/heal_paid (fee = 100 + 2 x cd_instability = 150 flat at idx25; do NOT fork it). Civilian: ZERO recognition of the founder - she reads the propaganda decay only (the money went quiet, shipments arrive re-verified). Reuse dialog:hz_nurse pattern (heal + posted-rate flavor) or author a trimmed dialog:nifl_nurse. Verify no free Cobblemon Healing Machine block stands in the Nifl center (fence/remove per Sango/Takehara precedent) so the paid nurse is the healing path. Latch-placed, no uuid.",
  "recipe": "civilian",
  "dialog": "dialog:nifl_nurse",
  "service": { "kind": "heal" },
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 3470, "y": 66, "z": 2000 }
}
```

**Kestrel Vane (`nifl_martkeeper`)** — default CobbleDollars shop; tier `badge_9` (or its `_relief*`
variant if fields are already liberated — see Open Q). Reuse `dialog:hz_martkeeper` idiom. Civilian,
**ZERO recognition**.

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "nifl_martkeeper",
  "display_name": "Kestrel Vane",
  "role": "merchant",
  "act": "3",
  "location": "Nifl Town - Pokemart",
  "recognition_tier": "late",
  "_comment": "Nifl mart, DEFAULT CobbleDollars shop (tier badge_9 active after Boreas; relief variant if fields already liberated - showrunner confirm which catalog is live). Do NOT give a custom per-merchant shop (reload only reaches the default shop). Civilian: ZERO recognition. Reuse dialog:hz_martkeeper pattern. Latch-placed, no uuid.",
  "recipe": "civilian",
  "dialog": "dialog:hz_martkeeper",
  "service": { "kind": "shop_cobbledollars" },
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 3480, "y": 66, "z": 2010 }
}
```

**Old Halla (`nifl_civilian_frost`)** — optional cold-town color + the town's mandated **back-echo**
line. Civilian, **ZERO recognition**. `dialog_inline` BASIC.

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "nifl_civilian_frost",
  "display_name": "Old Halla",
  "role": "civilian",
  "act": "3",
  "location": "Nifl Town - lake path",
  "recognition_tier": "late",
  "_comment": "Cold-town color + the town back-echo. CIVILIAN: ZERO recognition of the founder (public scrubbing worked) - she feels only the propaganda decay. Multi-line say[] renders ONE RANDOM page per open (each line is an alternative, never a sequence). Latch-placed, no uuid.",
  "recipe": "civilian",
  "dialog_inline": {
    "kind": "dialog",
    "id": "nifl_civilian_frost",
    "type": "BASIC",
    "entries": [
      {
        "label": "default",
        "name": "Old Halla",
        "priority": 10,
        "say": [
          "Prices went strange for a season, then went quiet again - they say a man in the capital lost his chair and the money remembered how to behave. I would not know. I trade in fish and firewood.",
          "You have the look of the roads. Bug country, was it, and the marsh, and that loud water port? The cold flattens all those stories into one long white one out here.",
          "Careful past the Frostgate. There is a Company man frozen to his post out there who has not been relieved in a very long time. Somebody warmer than me should tell him it is over."
        ]
      }
    ]
  },
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 3462, "y": 66, "z": 2016 }
}
```

---

## 4. Recognition & economy beats

**Recognition (band = LATE, 7+ badges).** Nifl is where late-tier recognition **pays a dividend** —
some Company personnel stand down rather than raise a hand against the founder (LORE §"Late: some
stand down"). Delivered by:
- **Records Officer Halden (SQ1)** — *"You match a portrait I am not allowed to have seen… Give me a
  reason to keep filing, or give me a reason to stop."* Can place the face and unlatch the drawer.
- **Warrant Officer Dain (SQ2)** — the first **on-screen defector**: *"I saluted that walk for twenty
  years before they took the picture off the wall… I have orders. And I have eyes."* Salutes and walks
  into the snow. Grief, not a fight.
- **Auditor Corvin (SQ1)** — late recognition as quiet dread: files the burned-face portrait under
  *unresolved*, tells the player the cold will hold it *until they come back with a name.*
- **Boreas (leader, already built)** — recognizes *eerily* (prophecy-of-the-cold, not Company alarm).
- **Civilians (Vetra, Halla, Sabra, Vane) — NEVER recognize the founder.** The public scrub worked;
  they only feel the propaganda decay. Lantern 4's founder-adjacent whisper is the *ice* recognizing
  him, not a person — and it is a half-heard name, never a reveal.

**Economy voice (gated on `cd_instability=25`, post-HQ / late register).** Not Act-1 corporate cheer.
The money went **quiet** after Acting CEO DJ fell; the propaganda **decayed** rather than glitching
loud. Voice beats: Halla's *"the money remembered how to behave"*, Vetra's *"the money went quiet when
the acting man lost his chair"*, Corvin's cores (the two-lie memo folder = cover-up leaking). Payouts
run through the skew-aware `economy/payout` (rate 94% at idx25 → 600 lands ≈564, 300 lands ≈282); the
flat 5400 leader prize does not skew. No new field liberation here (all fields freed pre-Royal-League);
Nifl only **spends** the stabilized economy — CD **sinks** are the paid nurse (150 flat at idx25 via
`heal_paid`), the mart, and the two decline fees (120 / 140).

**Streamable receipt / title-card beats (house style).** Every payout and reveal throws an on-screen
card:
- **SQ1 stand-down** — `title` **STOOD DOWN** + subtitle *A records officer unlatches the drawer and
  steps into the cold* (the cite button).
- **SQ1 portrait read** — the frag_9 sting: gold **They emptied you.** + gray *The cold kept the shape
  of what they took.* (from `archive_reward`).
- **SQ2 stand-down** — gold **STOOD DOWN** + gray *A veteran salutes and walks into the snow* (from
  `frostgate_standdown`).
- **Decline fees** — the Company-voice charge receipt *Verified Charge: 120 CD / 140 CD. Non-engagement
  processed.* (auto-emitted by the decline fns, `decline_sq_genji_wager` idiom).
- **Core reads** — per-core actionbar receipts (cores 1/2, and the lantern relights) narrate the thaw.

---

## 5. New tags/scores introduced

| tag | set by | gated by |
|-----|--------|----------|
| `nifl_core_1` | Corvin core-one button (SQ1) | SQ1 resume/target stages |
| `nifl_core_2` | Corvin core-two button (SQ1) | SQ1 resume/target stages |
| `nifl_archive_open` | Records Officer win (`on_win`), stand-down cite button, or paid-decline fn | core-three portrait button (SQ1) |
| `defeated_nifl_records_officer` | Records Officer battle win (default `defeat_tag`) | flavor / already-beaten line |
| `declined_nifl_records_officer` | `route/decline_nifl_records_officer` paid branch | decline stand-down |
| `nifl_archive_read` | `sidequest/nifl/archive_reward` (SQ1 payout, once) | Corvin post_read entry, Vetra hub, SQ1 sidebar off |
| `nifl_frostgate_clear` | Dain win (`on_win`), stand-down fn, or paid-decline fn (SQ2) | SQ2 cleared entry, Vetra hub, SQ2 sidebar off |
| `defeated_nifl_warrant_officer` | Dain battle win (default `defeat_tag`) | flavor / already-beaten line |
| `nifl_warrant_stood_down` | `sidequest/nifl/frostgate_standdown` (SQ2 branch B) | defector flavor / receipt |
| `declined_nifl_warrant_officer` | `route/decline_nifl_warrant_officer` paid branch | decline stand-down |
| `nifl_lantern_1` | Vetra lantern-one button (SQ3) | SQ3 chain + sidebar |
| `nifl_lantern_2` | Vetra lantern-two button (SQ3) | SQ3 chain |
| `nifl_lantern_3` | Vetra lantern-three button (SQ3) | SQ3 chain |
| `nifl_lantern_4` | `sidequest/nifl/lanterns_reward` (SQ3, via lantern-four button) | (implied by done) |
| `nifl_lanterns_done` | `sidequest/nifl/lanterns_reward` (SQ3 payout, once) | Vetra hub_done, SQ3 sidebar off |

No new scoreboards. All gating is plain PLAYER_TAG (`defeated_*` + story tags) — **no band-tag
entries required** (Nifl uses no numeric dialog gates; the only numeric reference is `cd_instability`
inside `economy/payout` / `heal_paid`, which are shipped and read the scoreboard themselves).
Existing tags reused: `defeated_ryujin_leader` (arrival gate), `memory_fragment` (=9, HUD, already
wired by the leader).

---

## 6. Build checklist

Author under `dialog-src/` (a `nifl/` area folder mirrors `takehara/`), resources under
`data/`, then compile. Ordered:

1. **Create character files** in `dialog-src/characters/nifl/`:
   `nifl_cold_auditor.json`, `nifl_records_officer.json`, `nifl_warrant_officer.json`,
   `nifl_keeper_vetra.json`, `nifl_nurse.json`, `nifl_martkeeper.json`, `nifl_civilian_frost.json`
   (7 new bodies — copy the JSON blocks above verbatim). Do **NOT** touch the existing gym cast.
2. **Create dialog files** in `dialog-src/dialog/`:
   `nifl_cold_storage.json`, `nifl_records_officer.json`, `nifl_frostgate.json`,
   `nifl_keeper_vetra.json` (4 new trees; blocks above). `nifl_nurse` may reuse `dialog:hz_nurse` or
   get a trimmed tree; `nifl_martkeeper` reuses `dialog:hz_martkeeper`; `nifl_civilian_frost` is
   inline.
3. **Create RCT trainer teams** in `data/rctmod/trainers/`:
   `nifl_records_officer.json` (GEN_9_SINGLES, band 66–68: Bronzong / Klinklang / Porygon2 / Beartic),
   `nifl_warrant_officer.json` (GEN_9_DOUBLES, band 66–68: Dain Weavile+Walrein / grunt Glalie+Sneasel).
   Register both in a side-quest trainer registry — extend or create
   `data/cobblemon_initiative/trainers/side_quests/act3.json` (copy `act1.json` shape;
   `prerequisites:["ryujin_leader"]`). **Verify all species/level bands are ≤ 68 (arrival cap).**
4. **Add datapack functions** under `data/cobblemon_initiative/function/`:
   `sidequest/nifl/archive_reward.mcfunction`, `sidequest/nifl/frostgate_standdown.mcfunction`,
   `sidequest/nifl/lanterns_reward.mcfunction` (specs in §3). The two
   `route/decline_nifl_*_officer.mcfunction` files are **auto-generated by `content_compile`** from the
   `decline_fee` fields — do not hand-write them; edit the character JSON.
5. **Add QUEST_TARGETS stages** to `dialog-src/registers/quest_targets.json`: the four holder blocks
   `q.side_archive` (slot 58), `q.side_frostgate` (57), `q.side_lanterns` (56) — confirm slot values do
   not collide with existing side holders; adjust to free slots if they do.
6. **Compile & wire** (pipeline order):
   ```
   scripts/content_compile        # lowers dialog-src -> easy_npc preset SNBT + decline fns + quest_waypoints
   scripts/update_preset_index    # rebuild Easy NPC preset index
   scripts/generate_npc_function  # npc/preset_map.json + update_npc_presets.mcfunction
   ```
7. **JAR-VALIDATE item ids** before ship: `cobblemon:rare_candy`, `cobblemon:ice_stone`,
   `cobblemon:pretty_feather`, `cobblemon:poke_ball` and the `npc_gift/training_standard` loot table.
   Swap `pretty_feather` → `minecraft:snowball ×4` if absent (per §3.3 note).
8. **Runtime verify** (no automated tests): `install run` repaints/latches the 7 bodies within ~40b of
   their placements; walk SQ1 (fight AND stand-down AND decline paths), SQ2 (all three branches),
   SQ3 (four lanterns → done); confirm each sidebar line appears on `defeated_ryujin_leader` and clears
   on its done tag; confirm no free healing machine in the center.

---

## 7. Open questions for showrunner

1. **Placements are all PROPOSED.** The vault mouth (3520/68/1982), inner stacks (3540/68/1975),
   Frostgate (3450/70/2030), lake shore (3500/65/1960), and lower-town center/mart (3470–3480/66) need
   builder confirmation against walkable blocks and that the "vault / Frostgate / lake" reads exist in
   the world. Latch-place or adopt CSV bodies at the pass?
2. **How explicit can SQ1's portrait get?** Written to *burned-out face / unmistakable posture* so it
   **circles, never closes** the Founder reveal (reserved post-Royal-League). Confirm that read is
   acceptable this early in Act 3. (Nifl is `act:"3"` in the gym cast already.)
3. **Records Officer team band.** Proposed 66–68 (≤ arrival cap 68) so the opt-in fight is winnable
   underleveled. Confirm the roster (Bronzong/Klinklang/Porygon2/Beartic — "records & machines").
4. **Frostgate DOUBLES team.** Confirm Dain Weavile+Walrein / grunt Glalie+Sneasel at 66–68, hosted on
   one entity (agent_yield_lead pattern), and whether the branch-B walk-off should physically `kill`
   the body or leave it for the placement pass.
5. **Decline fees.** SQ1 120 CD / SQ2 140 CD — both open the gate on the paid branch, both fail-soft to
   the battle when broke. Confirm the amounts and that "decline opens the archive/gate" is intended
   (vs. decline = leave without progress).
6. **Live shop tier at Nifl.** Post-HQ the relief catalog may already be active. Confirm whether the
   mart shows `badge_9` or a `badge_9_relief*` variant so stock/prices read correctly.
7. **Slot numbers.** SQ sidebar slots 58/57/56 are placeholders — assign to free slots that do not
   collide with existing side holders in `quest_targets.json`.
8. **`act3.json` side-quest registry.** Confirm creating `trainers/side_quests/act3.json` (vs.
   extending an existing act file) for the two new SQ battle trainers.
