# 15 — Battle Frontier

> **SLUG** `15_battle_frontier` · **SET-PIECE** (post-Royal-League proving ground) ·
> **Band:** cap **85→100**, `memory_fragment` 10, `cd_instability` **25 (stabilised)**,
> recognition **late**, `company_overthrown` **may or may not be set** (see §Overview).
>
> **BUILT (0.5.0-alpha.15).** Implemented from this plan with three deliberate deviations
> (all better fits for the "safe exhibition + soft passes" showrunner rulings):
> 1. **Kept the 24 existing inline dialogs** (rich per-facility flavor) and upgraded them in
>    place — did NOT flatten to the shared `frontier_challenger_generic`/`frontier_brain_generic`
>    trees this doc proposed. Brains: `gauntlet_boss`→`gym_leader` (no despawn), defeat_tag →
>    `frontier_<facility>_cleared`, + prize + `on_win`→`frontier/hall_cleared`. Selene gets the
>    full 9-tag prereq gate. Challengers: + prize 800.
> 2. **Dropped the paid-decline economy** (§3.2 `decline_challenger/brain`). A friendly opt-in
>    facility with a free `leave_button` makes a pay-to-decline button strictly dominated and
>    contradicts "no shame either way"; it also sidesteps the auto-gen `decline_fee`
>    must-fight-on-broke footgun. The CD sink is the **passes** (soft, never consumed — Open Q7
>    ruled soft). Fairness floor holds trivially: `leave_button` = free close, never a forced fight.
> 3. **Safe exhibition (Open Q3) via a datapack region tag**, not a rental-party swap: the
>    `frontier/region_tick` AABB maintains `frontier_active`, which NuzlockeInit's three
>    faint/flee/forfeit guards honor (mirrors the Stadium clone-party guard) — nothing you love
>    dies on the Frontier floor. Grand purse (Open Q6) kept at 20000 CD via `economy/payout`.
>
> Remaining open: coords vs atlas (Open Q1/Q2 — using shipped RCT cluster), Arcade modifiers
> (Open Q4 — flavor-only), runtime smoke test (needs a GPU session). Original plan preserved below.

---

## ⚠️ Read first — the frontier is already half-built (RCT layer only)

`src/main/resources/data/cobblemon_initiative/trainers/battle_frontier/battle_frontier.json`
**already ships 24 fully-teamed RCT trainers** (2 challengers + 1 brain per facility, 8
groups) with levels, movesets, held items, coords, prerequisites, and
`achievementOnDefeat` clears. **Those ids are canon — never rename them, never invent a
new one.** What is UNCAST is the *authoring layer*: no `characters/**` files, no
`dialog/**` trees, no lore, no quest-sidebar wiring, no economy loop. That is this doc.

**Every id, name, coordinate, `battleFormat`, and `achievementOnDefeat` below is copied
verbatim from the shipped RCT file (verified id-by-id).** Two facts about that file drive
this doc:

1. **8 groups, not 7.** The prompt's 7 facilities are Tower / Factory / Castle / Arcade /
   Port / Pyramid / **Deep Dark Cave**. The shipped file has **8 groups**: `battle_castle`,
   `battle_tower`, `battle_factory`, `battle_arcade`, `battle_pyramid`, `the_port`,
   **`the_market`**, `deep_dark_cave`. The extra **The Market** group (Sterling + Vance +
   Fiona) has no prompt subtitle. This doc casts **all 8** (24 ids) and folds `the_market`
   in as the frontier's **exchange concourse / entry hub** — a diegetic home for the
   entry-fee economy and the Frontier Registrar. Nothing is left uncast.
2. **Shipped coords are the anchor.** The prompt's atlas intent is **[4096 2965]** with
   facility coords in the 4000s; the shipped RCT file places every trainer around
   **x≈3782–3818, y159, z≈2959–2999**. **This doc uses the shipped RCT coords as placement
   anchors** (they are what actually spawns today) and lists the atlas intent alongside as
   `ATLAS-INTENT` so a re-survey is a one-line edit. All placement stubs are marked
   `PLACEHOLDER — verify vs atlas`.

---

## 1. Overview

**The town/set-piece.** The Battle Frontier is the endgame arena the run earns *after*
toppling the Royal League Champion (Cynthia → `royal_league_champion`). Seven battle
facilities plus an exchange concourse (`the_market`) cluster on the frontier plateau.
Every facility is a self-contained challenge with a **Frontier Brain** at its summit; the
whole thing is a level-85→100 sandbox the player grinds toward the vanilla-Minecraft
send-off.

**Band context.** By the time the frontier opens: all 10 badges (`memory_fragment` 10),
level cap **85** (rising to **100** on any Board clear / at the frontier ceiling), the
currency is **stabilised at `cd_instability` 25** (Acting CEO DJ fell in Act 2), and
recognition is **late** — but *muted*. The Company that ran the recognition arc has been
gutted; the frontier is a place where the founder is barely remembered at all.

**The arc job.** The frontier is the **"after."** It is post-`company_overthrown` in
spirit (played in the 85–100 window that straddles the Board/Founder clearout). Its job is
**decompression + mastery + a light thematic coda**: the Company's abandoned proving
ground, repurposed. It does NOT advance the villain plot. It gives the stream a
long-tail, replayable, high-level battle campaign and one quiet lore note — *what the
Company built when it still built things for people to enjoy, not to extract from.*

**Its place on the route.** Strictly post-Royal-League, parallel to / after Act 3 (Board →
The Founder). Gate on `royal_league_champion` (matches the shipped RCT `prerequisites`,
whose token is `royal_champion`). It is the last curated destination before the player
leaves for generated terrain and the Ender Dragon (the `company_overthrown` → "Hunt the
Ender Dragon" main-line stage).

**Lore stance (the frontier as the Company post-fall).** Neutral proving ground,
*reclaimed*. The Company built the Frontier in its glory days as a public showpiece — the
one wing that was about spectacle, not the ledger. After the fall it has no owner; the
Brains run it themselves as a guild of caretakers. The scrubbing never reached here
(nobody bothered erasing a portrait from a place the coup considered a toy). So the
frontier holds a few **un-scrubbed artifacts** — the founder's dedication plaque, an
original charter with the signature intact — the only place in the world where the name
was never taken down, because nobody thought it mattered. **Challengers (civilians) never
recognise the founder** (public scrubbing held — LORE_BIBLE §4). The **Brains** and the
**Caretaker** are old Company-era figures (`frontier_brain` / `lore_keeper` roles, not
civilians); they half-recognise and **choose grace** — and **the name is never spoken
here** (Rule 7; reserved for the mirror).

### Hardcore + single-player + Nuzlocke constraint (call this out — design-critical)

A hardcore-Nuzlocke player **cannot risk their permadeath team** on an optional grind. The
frontier must therefore run on **borrowed / set-mode teams**, not the player's box:

- **The rule the frontier advertises:** *challenge teams are provided or set — you do not
  field your caught Pokemon here.* Diegetically: *House rules. The Frontier lends the team;
  your own stay in the box. Nothing you love dies on our floor.*
- **Mechanical reality (NOTE for showrunner — engine work, not authorable in dialog):**
  RCT battles pit the player's *actual party* against the trainer. There is no shipped
  rental-party swap. Until a set-mode/rental swap exists, the honest framings are:
  (a) **treat frontier fights as fully OPT-IN, above-context risk, with the stake printed
  and a decline that costs CD** (same pattern as the Genji wager / paid-decline), and
  (b) **make the Battle Factory the diegetic home of "rental" flavor** even if mechanically
  it is still the player's team — OR wire a party-stash helper later (see §Open questions
  Q3). **No frontier fight may be forced**; all are hailed opt-ins, so a griever can walk.
- **Fairness floor:** no frontier battle can whiteout a player with no caught Pokemon
  (none here are forced, so this holds trivially; the friendly decline never falls through
  to a must-fight — see §3.2 DATAPACK NEEDS).

---

## 2. Cast

Every NPC below is **new authoring** over an **existing RCT id** (except the two pure-lore
NPCs, the Registrar and the Caretaker, who have no RCT id). Placement anchors are the
**shipped RCT coords** (authoritative today); `ATLAS-INTENT` is the prompt's 4000s layout.

| id | display_name | role | RCT id | concept | placement anchor (shipped) |
|----|--------------|------|--------|---------|----------------------------|
| `frontier_registrar` | Frontier Registrar Odette | `merchant` | — | Runs the concourse; sells facility **entry passes** (the CD sink); rumor-hub for the frontier | `3800 159 2997` (concourse, PLACEHOLDER) |
| `frontier_caretaker` | Caretaker Anselm | `lore_keeper` | — | Old Company-era groundskeeper; guards the un-scrubbed dedication plaque; the one late-recognition grace beat | `3806 159 2999` (concourse, PLACEHOLDER) |
| **Tower** — *Climb. Every floor is a promotion.* | | | | | |
| `frontier_tower_challenger_1` | Climber Jasper | `duelist` | `tower_challenger_1` | Floor 1 of the promotion ladder | `3797 159 2959` |
| `frontier_tower_challenger_2` | Contender Mira | `duelist` | `tower_challenger_2` | Floor 2 | `3803 159 2959` |
| `frontier_brain_tower` | Tower Tycoon Palmer | `frontier_brain` | `frontier_brain_tower` | The top-floor "promotion"; escalating-streak boss | `3800 159 2962` |
| **Factory** — *Rental assets only.* | | | | | |
| `frontier_factory_challenger_1` | Technician Rex | `duelist` | `factory_challenger_1` | Runs a borrowed-team bench | `3809 159 2962` |
| `frontier_factory_challenger_2` | Engineer Lydia | `duelist` | `factory_challenger_2` | Swaps rentals between rounds | `3815 159 2962` |
| `frontier_brain_factory` | Factory Head Noland | `frontier_brain` | `frontier_brain_factory` | The rental scheme's designer | `3812 159 2968` |
| **Castle** — *Old money. Older rules.* | | | | | |
| `frontier_castle_challenger_1` | Knight Aldric | `duelist` | `castle_challenger_1` | Formal-duel gatekeeper | `3785 159 2962` |
| `frontier_castle_challenger_2` | Guard Captain Elara | `duelist` | `castle_challenger_2` | Enforces the house forms | `3791 159 2962` |
| `frontier_brain_castle` | Castle Lord Percival | `frontier_brain` | `frontier_brain_castle` | Lord of the old rules | `3788 159 2968` |
| **Arcade** — *The house always spins.* | | | | | |
| `frontier_arcade_challenger_1` | Gambler Fritz | `duelist` | `arcade_challenger_1` | Spins the modifier wheel | `3782 159 2976` |
| `frontier_arcade_challenger_2` | Player Suki | `duelist` | `arcade_challenger_2` | High-roller regular | `3788 159 2976` |
| `frontier_brain_arcade` | Arcade Star Dahlia | `frontier_brain` | `frontier_brain_arcade` | The house incarnate | `3785 159 2982` |
| **Port** — *Fresh challengers daily.* | | | | | |
| `frontier_port_challenger_1` | Captain Stern | `duelist` | `port_challenger_1` | Today's rotating docker | `3785 159 2990` |
| `frontier_port_challenger_2` | Sailor Crest | `duelist` | `port_challenger_2` | Today's rotating rival | `3791 159 2990` |
| `frontier_brain_port` | Port Admiral Horatio | `frontier_brain` | `frontier_brain_port` | Harbormaster of the daily gauntlet | `3788 159 2996` |
| **Pyramid** — *Bring your own light.* | | | | | |
| `frontier_pyramid_challenger_1` | Explorer Marco | `duelist` | `pyramid_challenger_1` | Torch-run guide, level 1 | `3812 159 2976` |
| `frontier_pyramid_challenger_2` | Archaeologist Priya | `duelist` | `pyramid_challenger_2` | Deeper chamber | `3818 159 2976` |
| `frontier_brain_pyramid` | Pyramid King Brandon | `frontier_brain` | `frontier_brain_pyramid` | King of the dark climb (Regis) | `3815 159 2982` |
| **Market / Concourse** — *Exchange floor (folds in; §warning 1)* | | | | | |
| `frontier_market_challenger_1` | Merchant Vance | `duelist` | `market_challenger_1` | Concourse duel-for-hire | `3797 159 2993` |
| `frontier_market_challenger_2` | Trader Fiona | `duelist` | `market_challenger_2` | Concourse duel-for-hire | `3803 159 2993` |
| `frontier_brain_market` | Market Mogul Sterling | `frontier_brain` | `frontier_brain_market` | The concourse's own brain | `3800 159 2999` |
| **Deep Dark Cave** — *The dark down here is load-bearing.* | | | | | |
| `frontier_cave_challenger_1` | Spelunker Dirk | `duelist` | `cave_challenger_1` | First sculk chamber | `3809 159 2990` |
| `frontier_cave_challenger_2` | Cave Diver Luna | `duelist` | `cave_challenger_2` | Deeper sculk chamber | `3815 159 2990` |
| `frontier_brain_cave` | Cave Warden Selene | `frontier_brain` | `frontier_brain_cave` | Final brain; gates on ALL 7 other brains (shipped prereqs) | `3812 159 2996` |

> **Naming convention (casting scheme):** character id = `frontier_<group-short>_<role>` —
> `frontier_<facility>_challenger_1/2` and `frontier_brain_<facility>` — mirroring the
> shipped RCT `id`s but namespaced with the `frontier_` prefix so the character files never
> collide with anything else in `characters/**`. `display_name` = the shipped RCT
> `displayName` **verbatim** (Palmer, Noland, Percival, Dahlia, Horatio, Brandon, Sterling,
> Selene — canon Frontier-Brain names, keep them). The two lore NPCs (Registrar, Caretaker)
> are new and have no RCT id.
>
> **Level bands (casting scheme, from the shipped teams):** the shipped rosters run
> **level 90–100** across all 8 facilities — challengers lower in the band, brains at the
> ceiling, Selene at 100. There is no per-facility level ramp to invent; the bands are
> baked. This is *above* the entry cap 85, which is exactly why every fight is opt-in
> above-context (see §3.2 cap-legality).

---

## 3. Quests

Aim: the frontier is one big campaign, so it ships **6 quest units** — one **registration
hub** quest, one **per-facility clear** meta-quest (rolled into a single tracked line that
retargets), one **brain-gauntlet capstone** (the Warden), one **entry-economy sink**, one
**light-lore artifact** side quest, and one **rumor-hub** wiring quest. The 24 battles
themselves are RCT-defeat-gated (the shipped `prerequisites`), so the dialog layer's job is
hails, opt-in framing, entry-fee sink, receipts, and lore — **not** re-implementing the
battles.

Because the 24 challenger/brain hails are structurally identical (opt-in high-level duel,
printed as above-cap risk, decline costs CD), this section gives **one canonical
challenger stub + one canonical brain stub** and a **fill table** for the other 22, rather
than 24 near-duplicate blocks. Everything is copy-paste-ready.

> **say[] rule reminder (HARD RULE 2):** every multi-line `say[]` array below is a set of
> **interchangeable alternatives** — Easy NPC renders exactly ONE at random per open. No
> `say[]` block below is a two-part monologue; each line stands alone. Anything that MUST
> be seen every time (a forward hook, a receipt, a title-card) is delivered as a **button
> line, a persistent default entry, or a `{do:announce}` / function title-card**, never as
> "line 2 of a say[]."

---

### 3.1 — Registration: *Sign the Frontier Ledger* (hub quest)

**Concept.** The player arrives; the Frontier Registrar signs them in, explains the house
rules (borrowed teams, opt-in fights, entry passes), and sells **facility entry passes**
(the CD sink). This is the frontier's rumor-hub + shop + tutorial in one NPC.
**Forward hook:** names the Deep Dark Cave as the sealed capstone (delivered as the
*persistent* `signed` default line + a rumor button, so it is guaranteed, not random).
**Back-echo:** references `royal_league_champion` (the League seat that bought the ledger
name) and the stabilised currency (the passes are cheap now — the money finally holds
still).

**Character JSON** (`characters/frontier/frontier_registrar.json`):

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "frontier_registrar",
  "display_name": "Frontier Registrar Odette",
  "role": "merchant",
  "act": "3",
  "location": "Battle Frontier - Concourse",
  "recognition_tier": "late",
  "recipe": "shopkeeper",
  "dialog": "dialog:frontier_registrar",
  "movement": { "objective": "ambient_stationary_look" },
  "service": { "kind": "shop_cobbledollars" },
  "placement": { "x": 3800, "y": 159, "z": 2997 }
}
```

**Dialog JSON** (`dialog/frontier_registrar.json`) — `STANDARD`, gated arrival vs
returning; the entry-pass buttons call the pay-probe functions from §DATAPACK NEEDS. Each
`say[]` line is a self-contained alternative; the Warden forward-hook and the two rumor
pointers live in **buttons** so they are always reachable (§3.5 folds its lines here):

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "frontier_registrar",
  "type": "STANDARD",
  "entries": [
    {
      "label": "signed",
      "name": "Registrar - the ledger keeper",
      "priority": 30,
      "gate": { "tag": "frontier_registered" },
      "say": [
        "Back for more. Passes are at the window, house rules on the wall - our teams, not yours. Nothing you love dies on this floor.",
        "The Warden in the Deep Dark keeps her door shut until the other seven Brains bow. Bring me a pass and pick a hall.",
        "Seven halls and a sealed cave. Sign is done, so the floors are yours. The window is open."
      ],
      "buttons": [
        {
          "label": "buy_pass_button",
          "text": "Buy a facility entry pass - 200 CD",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:frontier/buy_pass", "as_player": true },
            { "do": "close" }
          ]
        },
        {
          "label": "shop_button",
          "text": "Frontier exchange",
          "actions": [ { "do": "trade" } ]
        },
        {
          "label": "warden_hook_button",
          "text": "Where do I end up?",
          "gate": { "not_tag": "frontier_all_cleared" },
          "actions": [
            { "do": "open_dialog", "label": "warden_hook" }
          ]
        },
        {
          "label": "plaque_pointer_button",
          "text": "Who is that old fellow by the brass?",
          "gate": { "not_tag": "frontier_plaque_read" },
          "actions": [
            { "do": "open_dialog", "label": "plaque_pointer" }
          ]
        }
      ]
    },
    {
      "label": "warden_hook",
      "name": "The Warden waits",
      "priority": -1,
      "say": [ "Seven halls, then the Warden of the Deep Dark - and only her. Clear the Brains and her door opens on its own." ],
      "buttons": [ { "label": "back_button", "text": "Understood", "actions": [ { "do": "open_default" } ] } ]
    },
    {
      "label": "plaque_pointer",
      "name": "About Anselm",
      "priority": -1,
      "say": [ "Old Anselm keeps the founding plaque by the concourse wall. Ask him about the dedication if you want the frontier history straight." ],
      "buttons": [ { "label": "back_button", "text": "I will", "actions": [ { "do": "open_default" } ] } ]
    },
    {
      "label": "default",
      "name": "Frontier Registrar Odette",
      "priority": 10,
      "default": true,
      "gate": { "not_tag": "frontier_registered" },
      "say": [
        "Champion. The seat you took at the League bought your name on this ledger - sign, and the halls are yours.",
        "House rules before you sign: the Frontier lends the team, your own stay in the box. Nothing you love dies here.",
        "The passes are cheap now - the money finally holds still. First one is on the house. Just put your name down."
      ],
      "buttons": [
        {
          "label": "sign_button",
          "text": "Sign the ledger",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:frontier/register", "as_player": true },
            { "do": "close" }
          ]
        }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:**
- `function/frontier/register.mcfunction` — sets `frontier_registered` tag; gives **1 free
  pass** (`scoreboard players add @s frontier_passes 1`); fires a title sting (title-card
  `FRONTIER LEDGER SIGNED`). Idempotent (guard on the tag).
- `function/frontier/buy_pass.mcfunction` — **pay-probe** (Genji `decline` pattern, verified
  in `route/decline_sq_genji_wager.mcfunction`): `store result` of `cobbledollars pay @s
  200` into a scratch (0 = broke, 1 = paid); on 1.. run `cobbledollars remove @s 200` +
  `scoreboard players add @s frontier_passes 1` + gold actionbar receipt (`Verified Charge:
  200 CD. One hall pass issued.`); on 0 gray actionbar (`The window is closed to the short
  of funds. Come back with 200.`). Never partial-charges.
- `scoreboard objective add frontier_passes dummy` (add to `economy/load` or a
  `frontier/load` hooked into it).

> **Receipt text lives in the mcfunction, not in dialog `say[]`.** The receipt `tellraw`/
> `title actionbar` may use double-quotes freely (it is a function file, like the shipped
> Genji receipt on line 12 of `decline_sq_genji_wager.mcfunction`). Only dialog `say[]` and
> macro-delivered `win_line`/`on_win`/`announce` are quote/apostrophe-restricted.

**QUEST_TARGETS entry** (append to `registers/quest_targets.json` `quests[]`; **slot 82** —
57–60 are already taken by clinic/manifest/deng/census, so the frontier block uses the free
endgame band 82–85, sitting just under `q.main` at 100):

```json
{
  "holder": "q.side_frontier",
  "name": "The Battle Frontier",
  "slot": 82,
  "stages": [
    {
      "if_tags": ["frontier_registered"],
      "not_tags": ["frontier_all_cleared"],
      "label": "Clear the seven halls, then the Warden",
      "target": { "npc": "frontier_brain_cave" },
      "note": "Once signed, the tracked line points at the sealed capstone (Cave Warden Selene). The per-hall retarget lives in q.side_frontier_hall (3.2)."
    },
    {
      "if_tags": ["royal_league_champion"],
      "not_tags": ["frontier_registered"],
      "label": "Sign the Frontier ledger",
      "target": { "npc": "frontier_registrar" },
      "note": "Frontier unlocks on royal_league_champion (the shipped RCT prerequisites token is royal_champion). Points at the Registrar until signed."
    }
  ]
}
```

**REWARD/BALANCE.** Signing is free (first pass gratis). Passes cost **200 CD** each — a
pure sink, priced to be trivial at 85+ but non-zero (a receipt beat). No battle here; no
cap concern. Decline = simply do not buy (fail-soft, no fee, it is a shop).

---

### 3.2 — *The Seven Halls* (per-facility clear, one retargeting tracked line)

**Concept.** The player clears each facility (2 challengers → brain, per the shipped
`prerequisites` chain). This quest is a **single sidebar line that retargets** to the next
un-cleared brain, with a live `$(halls)/7` counter — the frontier's spine.
**Forward hook:** the counter itself (`6/7 → the Warden stirs`, fired as a title-card, not
a random say line). **Back-echo:** each brain win fires a title-card that name-drops the
facility subtitle, echoing the concourse rules.

The 24 hails are **fill-table** driven. Below is the **canonical challenger stub** and the
**canonical brain stub**; the fill table lists what changes per NPC.

**Canonical CHALLENGER character JSON** (example: `frontier_tower_challenger_1.json`):

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "frontier_tower_challenger_1",
  "display_name": "Climber Jasper",
  "role": "duelist",
  "act": "3",
  "location": "Battle Frontier - Battle Tower",
  "recognition_tier": "late",
  "trainer": "tower_challenger_1",
  "recipe": "trainer_one_time",
  "dialog": "dialog:frontier_challenger_generic",
  "movement": { "objective": "ambient_stationary_look" },
  "battle": {
    "trainer": "tower_challenger_1",
    "type": "one_time",
    "format": "GEN_9_SINGLES",
    "prize": 800,
    "decline_fee": 100,
    "defeat_tag": "defeated_tower_challenger_1",
    "win_line": "Floor cleared. You move up. That is how this hall works.",
    "lose_line": "Down a floor. No shame - the climb resets, come again.",
    "already_beaten_line": "You already have this floor. The next one is waiting above."
  },
  "placement": { "x": 3797, "y": 159, "z": 2959 }
}
```

**Canonical BRAIN character JSON** (example: `frontier_brain_tower.json`):

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "frontier_brain_tower",
  "display_name": "Tower Tycoon Palmer",
  "role": "frontier_brain",
  "act": "3",
  "location": "Battle Frontier - Battle Tower",
  "recognition_tier": "late",
  "trainer": "frontier_brain_tower",
  "recipe": "gym_leader",
  "dialog": "dialog:frontier_brain_generic",
  "movement": { "objective": "ambient_stationary_look" },
  "battle": {
    "trainer": "frontier_brain_tower",
    "type": "gym_leader",
    "format": "GEN_9_SINGLES",
    "prize": 3000,
    "decline_fee": 200,
    "defeat_tag": "frontier_tower_cleared",
    "win_line": "Top floor. There is no promotion above this one - only the next hall.",
    "lose_line": "The summit holds. Take the lift down and try the climb again.",
    "already_beaten_line": "You own this tower. Spend your passes elsewhere.",
    "on_win": [ "execute as @1 run function cobblemon_initiative:frontier/hall_cleared" ]
  },
  "placement": { "x": 3800, "y": 159, "z": 2962 }
}
```

> **Note on `defeat_tag` for brains:** use the shipped `achievementOnDefeat` value as the
> tag (`frontier_<facility>_cleared`) so the character-layer tag and the RCT-clear line up
> and the `q.side_frontier` counter can read them directly. The Cave brain's tag is
> `frontier_all_cleared`.
>
> **`type: gym_leader` vs `frontier_brain` battle type.** The schema `battle.type` enum has
> no `frontier_brain` value; `gym_leader` is the closest existing behaviour (no despawn,
> repeatable-until-won, prize + defeat tag) and is what the `frontier_brain` **role**
> already maps its recipe to (§13.1). If a distinct frontier-brain battle type is added
> later, swap it in one line. Do not use `villain_*` / `gauntlet_boss` types — those imply
> no-decline + despawn, which breaks the opt-in fairness rule.

**FILL TABLE — the other 22 NPCs** (each is the canonical stub with these swaps; `format`
follows the shipped RCT `battleFormat` — DOUBLES rows ship `"format": "GEN_9_DOUBLES"`;
prizes and formats below are cross-checked against the shipped file):

| character id | display_name | RCT id | defeat_tag | prize | format |
|--------------|--------------|--------|------------|-------|--------|
| `frontier_tower_challenger_2` | Contender Mira | `tower_challenger_2` | `defeated_tower_challenger_2` | 800 | SINGLES |
| `frontier_factory_challenger_1` | Technician Rex | `factory_challenger_1` | `defeated_factory_challenger_1` | 800 | SINGLES |
| `frontier_factory_challenger_2` | Engineer Lydia | `factory_challenger_2` | `defeated_factory_challenger_2` | 800 | SINGLES |
| `frontier_brain_factory` | Factory Head Noland | `frontier_brain_factory` | `frontier_factory_cleared` | 3000 | SINGLES |
| `frontier_castle_challenger_1` | Knight Aldric | `castle_challenger_1` | `defeated_castle_challenger_1` | 800 | SINGLES |
| `frontier_castle_challenger_2` | Guard Captain Elara | `castle_challenger_2` | `defeated_castle_challenger_2` | 800 | SINGLES |
| `frontier_brain_castle` | Castle Lord Percival | `frontier_brain_castle` | `frontier_castle_cleared` | 3000 | **DOUBLES** |
| `frontier_arcade_challenger_1` | Gambler Fritz | `arcade_challenger_1` | `defeated_arcade_challenger_1` | 800 | SINGLES |
| `frontier_arcade_challenger_2` | Player Suki | `arcade_challenger_2` | `defeated_arcade_challenger_2` | 800 | SINGLES |
| `frontier_brain_arcade` | Arcade Star Dahlia | `frontier_brain_arcade` | `frontier_arcade_cleared` | 3000 | **DOUBLES** |
| `frontier_port_challenger_1` | Captain Stern | `port_challenger_1` | `defeated_port_challenger_1` | 800 | SINGLES |
| `frontier_port_challenger_2` | Sailor Crest | `port_challenger_2` | `defeated_port_challenger_2` | 800 | SINGLES |
| `frontier_brain_port` | Port Admiral Horatio | `frontier_brain_port` | `frontier_port_cleared` | 3500 | **DOUBLES** |
| `frontier_pyramid_challenger_1` | Explorer Marco | `pyramid_challenger_1` | `defeated_pyramid_challenger_1` | 800 | SINGLES |
| `frontier_pyramid_challenger_2` | Archaeologist Priya | `pyramid_challenger_2` | `defeated_pyramid_challenger_2` | 800 | SINGLES |
| `frontier_brain_pyramid` | Pyramid King Brandon | `frontier_brain_pyramid` | `frontier_pyramid_cleared` | 3000 | SINGLES |
| `frontier_market_challenger_1` | Merchant Vance | `market_challenger_1` | `defeated_market_challenger_1` | 800 | SINGLES |
| `frontier_market_challenger_2` | Trader Fiona | `market_challenger_2` | `defeated_market_challenger_2` | 800 | SINGLES |
| `frontier_brain_market` | Market Mogul Sterling | `frontier_brain_market` | `frontier_market_cleared` | 3000 | SINGLES |
| `frontier_cave_challenger_1` | Spelunker Dirk | `cave_challenger_1` | `defeated_cave_challenger_1` | 800 | SINGLES |
| `frontier_cave_challenger_2` | Cave Diver Luna | `cave_challenger_2` | `defeated_cave_challenger_2` | 800 | SINGLES |
| `frontier_brain_cave` | Cave Warden Selene | `frontier_brain_cave` | `frontier_all_cleared` | 5000 | **DOUBLES** |

> **Per-facility flavor:** each challenger/brain's `win_line`/`lose_line`/`already_beaten_line`
> should lean into the subtitle (Factory = rental jokes; Castle = formal "the forms are
> observed"; Arcade = "the wheel picks a modifier" — flavor only, RCT cannot roll modifiers,
> see §Open questions Q4; Pyramid/Cave = "bring your own light" / sculk). Keep every line
> **free of `"`, and free of `'` / `%`** (win/lose/already lines flow through the macro
> layer). Provide these at build time from the shipped team's identity (e.g. Palmer =
> dragons, Brandon = Regis trio, Selene = Darkrai / sculk-dark).

**Shared CHALLENGER dialog** (`dialog/frontier_challenger_generic.json`) — one reused tree,
opt-in with printed stake + decline. Each `say[]` line is a self-contained alternative:

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "frontier_challenger_generic",
  "type": "STANDARD",
  "entries": [
    {
      "label": "default",
      "name": "Frontier Challenger",
      "priority": 10,
      "default": true,
      "say": [
        "House rules: our team, not yours - nothing of yours dies on this floor. Ready to climb? Step off and you keep your pass.",
        "This is above your comfort and you know it. Opt in, or walk - no shame either way.",
        "The stake is one hall pass. Take the floor, or step off and keep it. Your call."
      ],
      "buttons": [
        {
          "label": "battle_button",
          "text": "Take the floor - opt in",
          "actions": [ { "do": "battle" } ]
        },
        {
          "label": "decline_button",
          "text": "Step off (pay 100 CD)",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:frontier/decline_challenger", "as_player": true },
            { "do": "close" }
          ]
        }
      ]
    }
  ]
}
```

> **Already-beaten line is handled by the battle block, not a dialog entry.** The character
> `already_beaten_line` fires automatically when its `defeat_tag` is present (per §9), so
> the shared tree carries **only** the default opt-in entry — no separate `cleared` entry
> (which would have needed a per-NPC gate the shared tree cannot carry). This matches the
> schema's mechanic-sugar model; confirm the auto-injection idiom in §Open questions Q5. If
> the compiler does NOT auto-inject, add a per-character inline `already_beaten` entry, not
> a shared one.

**Shared BRAIN dialog** (`dialog/frontier_brain_generic.json`) — same shape, higher stake
language, gated on the facility's two challengers being cleared (flavor gate; real gating
is the RCT `prerequisites`). Each `say[]` line is a self-contained alternative:

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "frontier_brain_generic",
  "type": "STANDARD",
  "entries": [
    {
      "label": "default",
      "name": "Frontier Brain",
      "priority": 10,
      "default": true,
      "say": [
        "You cleared my hall to reach me. Good. The Brain fight is the promotion - our team against yours, house rules, nothing of yours at risk.",
        "I am the top floor. There is no forcing this - decline and keep your pass, or take the seat and earn the summit.",
        "Opt in when you are ready. The team is provided; your own stay in the box, safe as they came."
      ],
      "buttons": [
        {
          "label": "battle_button",
          "text": "Challenge the Brain - opt in",
          "actions": [ { "do": "battle" } ]
        },
        {
          "label": "decline_button",
          "text": "Not yet (pay 200 CD)",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:frontier/decline_brain", "as_player": true },
            { "do": "close" }
          ]
        }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:**
- `function/frontier/hall_cleared.mcfunction` — fired from every brain's `on_win`.
  Recount cleared halls into a scratch and refresh the sidebar `$(halls)/7`; fire the
  matching facility title-card sting (e.g. `TOWER CONQUERED`). Spec: derive
  `#halls quest_hud` = count of the 7 `frontier_<facility>_cleared` tags (exclude
  `frontier_all_cleared`); on reaching 7, actionbar `THE WARDEN STIRS`.
- **`function/frontier/decline_challenger.mcfunction` (friendly-decline variant — NOT the
  auto-generated `decline_fee` function).** The compiler normally auto-generates a
  `route/decline_<id>` from `battle.decline_fee`, and that generated form is a *Company*
  paid-decline: broke → the battle fires (must-fight). **The frontier is friendly, so it
  needs a bespoke override:** pay-probe `cobbledollars pay @s 100`; on paid → `remove 100`
  + gold receipt (`Verified Charge: 100 CD. Floor skipped.`); on **broke → gray actionbar
  and just close, NEVER fight** (fairness floor — a starter-only griever must be able to
  walk). Sets no permanent tag (the challenger can be re-approached; decline is
  per-interaction). Because this deviates from the auto-gen, author the challenger/brain
  `decline_button` to call `frontier/decline_*` **explicitly** (as the JSON above does)
  rather than relying on `decline_fee` auto-generation, and either omit `decline_fee` from
  the battle block or document that its generated function is superseded.
- `function/frontier/decline_brain.mcfunction` — same, 200 CD, same broke → close (no
  fight).
- Sidebar render: add the `q.side_frontier_hall` block to `function/quest/render.mcfunction`
  (or the delegated set-line) with the `$(halls)` macro, mirrored in quest_targets below.

**QUEST_TARGETS entry** — the retargeting per-hall line (append to `quests[]`; **slot 83**):

```json
{
  "holder": "q.side_frontier_hall",
  "name": "The Seven Halls",
  "slot": 83,
  "stages": [
    {
      "if_tags": ["frontier_registered"],
      "not_tags": ["frontier_tower_cleared"],
      "label": "Clear Battle Tower  $(halls)/7",
      "dynamic": true,
      "target": { "npc": "frontier_brain_tower" },
      "note": "$(halls) = #halls quest_hud counting the 7 frontier_<facility>_cleared tags (frontier/hall_cleared). First-match retargets down this list as each hall falls; each stage not_tags its own clear."
    },
    {
      "if_tags": ["frontier_registered"],
      "not_tags": ["frontier_factory_cleared"],
      "label": "Clear Battle Factory  $(halls)/7",
      "dynamic": true,
      "target": { "npc": "frontier_brain_factory" },
      "note": "See tower stage."
    },
    {
      "if_tags": ["frontier_registered"],
      "not_tags": ["frontier_castle_cleared"],
      "label": "Clear Battle Castle  $(halls)/7",
      "dynamic": true,
      "target": { "npc": "frontier_brain_castle" },
      "note": "See tower stage."
    },
    {
      "if_tags": ["frontier_registered"],
      "not_tags": ["frontier_arcade_cleared"],
      "label": "Clear Battle Arcade  $(halls)/7",
      "dynamic": true,
      "target": { "npc": "frontier_brain_arcade" },
      "note": "See tower stage."
    },
    {
      "if_tags": ["frontier_registered"],
      "not_tags": ["frontier_port_cleared"],
      "label": "Clear Battle Port  $(halls)/7",
      "dynamic": true,
      "target": { "npc": "frontier_brain_port" },
      "note": "See tower stage."
    },
    {
      "if_tags": ["frontier_registered"],
      "not_tags": ["frontier_pyramid_cleared"],
      "label": "Clear Battle Pyramid  $(halls)/7",
      "dynamic": true,
      "target": { "npc": "frontier_brain_pyramid" },
      "note": "See tower stage."
    },
    {
      "if_tags": ["frontier_registered"],
      "not_tags": ["frontier_market_cleared"],
      "label": "Clear the Exchange Concourse  $(halls)/7",
      "dynamic": true,
      "target": { "npc": "frontier_brain_market" },
      "note": "the_market group folded in as the seventh hall (§warning 1). See tower stage."
    }
  ]
}
```

**REWARD/BALANCE.** Challengers pay **800 CD** each; brains **3000** (Port **3500**, Cave
**5000** — bigger 4-mon DOUBLES rosters, incl. legendaries). Plus the shipped RCT item
rewards (rare candies / ability capsules / diamonds / master balls). **Cap-legality:** all
frontier teams are **level 90–100**, above the 85 entry cap — hence **every fight is
OPT-IN, printed as above-context, decline-able** (§Overview constraint). By the time a
sane player clears the frontier they will have hit cap 100 (Board/Founder), so the fights
are legal *for a topped-out player*; the opt-in framing protects anyone attempting it
mid-85–100. Decline costs 100/200 CD, no permanent lockout, **broke never forces a fight**.

---

### 3.3 — *The Warden Opens Her Door* (capstone)

**Concept.** Cave Warden Selene (Deep Dark Cave) is the gauntlet's summit — the shipped RCT
`frontier_brain_cave` **prerequisites list ALL seven other brains**, so she is literally
sealed until the frontier is cleared. Her fight is the frontier's true final: a sculk-dark
DOUBLES team capped by Darkrai. **Forward hook:** her defeat (`frontier_all_cleared`) is
the frontier's completion beat and points the player at the vanilla send-off. **Back-echo:**
she references the un-scrubbed plaque (3.4) and the founder directly — the frontier's one
grace-note recognition.

Selene reuses the opt-in mechanics but gets a **bespoke recognition entry** layered on top
(STANDARD, higher priority) that fires only once all seven other halls are cleared. **She
never speaks the name (Rule 7)** — she gestures at the face and the plaque and then *lets
it go*. Each `say[]` line is a self-contained alternative:

**Selene dialog override** (`dialog/frontier_brain_cave.json`) — replaces the generic ref
in her character file:

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "frontier_brain_cave",
  "type": "STANDARD",
  "entries": [
    {
      "label": "recognition",
      "name": "The Warden knows the face",
      "priority": 30,
      "gate": {
        "all_tags": [
          "frontier_tower_cleared", "frontier_factory_cleared", "frontier_castle_cleared",
          "frontier_arcade_cleared", "frontier_port_cleared", "frontier_pyramid_cleared",
          "frontier_market_cleared"
        ],
        "not_tag": "frontier_all_cleared"
      },
      "say": [
        "Seven halls, seven bows. So the door opens. I am old enough to have shaken the hand that dedicated this place - I know the face, even if the world was told to forget it.",
        "Down here we never took the plaque down. Nobody thought the dark was worth scrubbing. Come as you are. In my dark the name does not matter - only whether you can see in it.",
        "The Company built one wing for wonder instead of the ledger, and this was it. You walked all seven halls to reach me. Step in when you are ready - our team, house rules, nothing of yours at risk."
      ],
      "buttons": [
        {
          "label": "battle_button",
          "text": "Face the Warden - opt in",
          "actions": [ { "do": "battle" } ]
        },
        {
          "label": "decline_button",
          "text": "Not yet (pay 200 CD)",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:frontier/decline_brain", "as_player": true },
            { "do": "close" }
          ]
        }
      ]
    },
    {
      "label": "default",
      "name": "Cave Warden Selene",
      "priority": 10,
      "default": true,
      "say": [
        "My door stays shut. Seven Brains stand between you and this dark - bow to all of them, then come back with a lamp and a spine.",
        "The Deep Dark keeps its own. Clear the other halls; the Warden does not open early.",
        "Not yet. Seven bows first. The dark down here is load-bearing - do not enter it half-earned."
      ],
      "buttons": [ { "label": "leave_button", "text": "Come back later", "actions": [ { "do": "close" } ] } ]
    }
  ]
}
```

> **Battle wiring:** Selene's `battle` block lives on her **character file** (fill-table
> row `frontier_brain_cave`, `defeat_tag: frontier_all_cleared`, prize 5000, DOUBLES,
> `on_win` → `frontier_complete`). Her character `dialog` field points at
> `dialog:frontier_brain_cave` (this override) instead of the generic brain tree.

**DATAPACK NEEDS:**
- Reuses `frontier/decline_brain`.
- `function/frontier/frontier_complete.mcfunction` fired from Selene's `on_win` (in place
  of / after `hall_cleared`): a title-card `FRONTIER CLEARED` + a one-time grand payout
  (`execute as @1 run function cobblemon_initiative:economy/payout {amount:20000}` — the
  verified skewed-payout function; at idx 25 it pays rate 100 - min(idx/4, 25) = 75%
  minimum, i.e. face is haircut per the stabilised instability) and confirm
  `frontier_all_cleared` is latched (it is the shipped `achievementOnDefeat`, so likely
  redundant — this function then only does the title-card + payout + forward-hook actionbar
  pointing at the vanilla send-off).

**QUEST_TARGETS entry** — the capstone line (append; **slot 84**). The `q.side_frontier` hub
line in 3.1 already retargets to Selene while sealed; this sibling holder fires only in the
*door-open* window so the sidebar reads differently once she is available:

```json
{
  "holder": "q.side_frontier_done",
  "name": "The Warden",
  "slot": 84,
  "stages": [
    {
      "if_tags": [
        "frontier_tower_cleared", "frontier_factory_cleared", "frontier_castle_cleared",
        "frontier_arcade_cleared", "frontier_port_cleared", "frontier_pyramid_cleared",
        "frontier_market_cleared"
      ],
      "not_tags": ["frontier_all_cleared"],
      "label": "The Deep Dark door is open - face the Warden",
      "target": { "npc": "frontier_brain_cave" },
      "note": "Fires only when all seven halls are cleared and the Warden fight is available; disappears on frontier_all_cleared."
    }
  ]
}
```

**REWARD/BALANCE.** 5000 CD + shipped master ball + 3 netherite ingots, plus the one-time
20000 CD `frontier_complete` grand purse routed through `economy/payout` (skewed to the
stabilised idx 25). Opt-in, above-cap, decline 200 CD, broke never forces a fight.

---

### 3.4 — *The Un-scrubbed Plaque* (light-lore side quest)

**Concept.** Caretaker Anselm tends the frontier's founding plaque — the **one artifact
the scrubbing missed** because the coup considered the Frontier a toy not worth erasing.
The player can inspect it; the plaque bears the founder's signature *intact* — the only
un-`§k`'d instance of the name in the world **before** the Founder mirror. To hold Rule 7
(do not name the Founder before Act 3), the plaque is authored so the **signature is
legible in-world as set dressing but the dialog and the read-out never speak it** — Anselm
reads *around* it, and the `read_plaque` tellraw describes the signature without printing a
name. **Forward hook:** points at the vanilla send-off (some faces come back to their own
doorstep — a doorstep on no map he keeps). **Back-echo:** the scrubbing artifacts seen all
run (§scrubbing register) — here, the *absence* of scrubbing is the beat.

> **Caretaker is a `lore_keeper`, NOT a civilian.** LORE_BIBLE §4: *civilians never
> recognise the founder.* Anselm is an old Company-era groundskeeper who dedicated-era
> *shook the hand* — a lore-bearer, exactly the "late: some keep it to themselves" grace
> beat. His `role` is `lore_keeper`; his recipe defaults to `civilian` behaviour only for
> the movement/attribute bundle (§13.1 maps `lore_keeper` → `civilian` recipe), which is
> presentation, not narrative category. He recognises the face and **keeps it to himself,
> never speaking the name** — so no civilian recognises the founder, and Rule 7 holds.

**Character JSON** (`characters/frontier/frontier_caretaker.json`):

```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "frontier_caretaker",
  "display_name": "Caretaker Anselm",
  "role": "lore_keeper",
  "act": "3",
  "location": "Battle Frontier - Concourse",
  "recognition_tier": "late",
  "recipe": "civilian",
  "dialog": "dialog:frontier_caretaker",
  "movement": { "objective": "ambient_stationary_look" },
  "placement": { "x": 3806, "y": 159, "z": 2999 }
}
```

**Dialog JSON** (`dialog/frontier_caretaker.json`) — `STANDARD`, late-recognition grace,
never speaks the name. Each `say[]` line is a self-contained alternative:

```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "frontier_caretaker",
  "type": "STANDARD",
  "entries": [
    {
      "label": "cleared_echo",
      "name": "Anselm - after the frontier",
      "priority": 30,
      "gate": { "tag": "frontier_all_cleared" },
      "say": [
        "You cleared every hall. The Brains will talk about it for years. Funny - you dust a plaque for forty years, and one day the name on it walks up and clears the place it dedicated.",
        "I never scrubbed a thing down here. Some faces come back to their own doorstep. There is a doorstep waiting for you too, and it is not on any map I keep.",
        "Every floor bowed to you. I keep my rag on the brass and my opinions to myself - but I saw. Go on, then. The frontier is yours, and so is whatever comes after it."
      ],
      "buttons": [ { "label": "leave_button", "text": "Thank you, Anselm", "actions": [ { "do": "close" } ] } ]
    },
    {
      "label": "default",
      "name": "Caretaker Anselm",
      "priority": 10,
      "default": true,
      "say": [
        "Careful of the plaque, stranger. Founding dedication, original signature, never taken down - the folk who scrubbed the branch offices thought the Frontier was a toy.",
        "It is the last honest signature left in the region, and I am the fellow who keeps the dust off it. Read it if you like.",
        "I shook a hand once, when this place opened. Long time ago. I would know the face anywhere - and a caretaker keeps his opinions to himself and his rag on the brass."
      ],
      "buttons": [
        {
          "label": "read_button",
          "text": "Read the plaque",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:frontier/read_plaque", "as_player": true },
            { "do": "close" }
          ]
        }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:**
- `function/frontier/read_plaque.mcfunction` — sets a one-time `frontier_plaque_read` tag
  (guard on it, idempotent); fires a `tellraw` set-dressing block describing the plaque
  **WITHOUT speaking the founder's name** (gray italic, e.g. *A brass plaque. Founding
  dedication. The signature at the bottom is worn but unbroken - the only one in the region
  no hand ever painted over.*). The `tellraw` is a function file, so it may use
  double-quotes for the JSON text components, but the rendered STRING must contain **no
  name and no apostrophe** (write "does not" not the contraction) so it is safe if later
  migrated to `{do:announce}`. No name, ever.

**QUEST_TARGETS entry** (append; **slot 85**):

```json
{
  "holder": "q.side_frontier_plaque",
  "name": "The Last Honest Signature",
  "slot": 85,
  "stages": [
    {
      "if_tags": ["frontier_registered"],
      "not_tags": ["frontier_plaque_read"],
      "label": "Read the founding plaque (Caretaker Anselm)",
      "target": { "npc": "frontier_caretaker" },
      "note": "One-shot lore beat; clears on frontier_plaque_read. The one un-scrubbed name in the world before the mirror - authored so dialog and read-out never speak it (Rule 7)."
    }
  ]
}
```

**REWARD/BALANCE.** Lore-only, no battle, no fee, no CD. Fail-soft (reading is optional).

---

### 3.5 — Rumor-hub wiring (house-style requirement)

**Concept.** Per house style, every town / set-piece needs a rumor-hub NPC pointing at its
quests, gated on each quest's not-done tag. The **Frontier Registrar (3.1)** IS the
rumor-hub — her `signed` entry already carries the pointer **buttons** (`warden_hook_button`
gated `not_tag: frontier_all_cleared`, and `plaque_pointer_button` gated `not_tag:
frontier_plaque_read`), each opening a manual-only (`priority -1`) pointer sub-entry. This
is **wiring already folded into the 3.1 dialog**, not a new NPC or a new file.

- **Warden pointer** (`warden_hook` sub-entry, gated by button `not_tag:
  frontier_all_cleared`): points at 3.2 / 3.3.
- **Plaque pointer** (`plaque_pointer` sub-entry, gated by button `not_tag:
  frontier_plaque_read`): points at 3.4.
- **Back-echo:** the Registrar's `default` line 1 (`The seat you took at the League bought
  your name on this ledger`) echoes `royal_league_champion`; her `signed` alternatives echo
  the stabilised currency. Both are in the guaranteed default/persistent text, not a
  50/50 random tail.

**DATAPACK NEEDS:** none (pure dialog buttons + gates).
**QUEST_TARGETS:** none (the pointed-at quests own their own lines).
**REWARD/BALANCE:** none.

---

## 4. Recognition & economy beats

**Recognition (late, muted — the "after").** The Company that ran the recognition arc is
gone, so the frontier's recognition is **quiet grace, not alarm** — and **the name is never
spoken** (Rule 7; reserved for the mirror):
- Challengers (**civilians / `duelist`**) **never recognise** the founder (public scrubbing
  held, LORE_BIBLE §4) — they see a topped-out champion, nothing more. Pure competitive
  banter.
- The **Brains** (`frontier_brain` — old, senior Company-era figures, not civilians)
  half-recognise and **choose grace** — they gesture at the old company, at a face they
  half-remember, never accusing, never bowing, never naming. Selene (3.3) says it plainest,
  and even she frames it as *in my dark the name does not matter.*
- The **Caretaker** (`lore_keeper`, 3.4) is the sharpest recognition in the unit — he
  *knows* the face and *keeps it to himself*. This is the late-tier "some keep it to
  themselves / stand down" beat rendered as a groundskeeper's discretion. He is a
  lore-bearer, not a civilian, so this does not break the civilian rule.
- **Rule 7 held everywhere:** the name is never spoken here. The plaque bears it as
  set-dressing; no dialog and no read-out closes it. That is reserved for the mirror.

**Economy voice (stabilised, idx 25).** The frontier is **post-stabilisation**, so the
Company voice is neither the glossy Act-1 cheer nor the Act-2 nervous over-explaining — it
is **absent / neutral**. The Registrar notes the passes are cheap now — the money finally
holds still (a back-echo of `CURRENCY STABILIZED`), and the pass/purse receipts keep the
Company money-voice cadence (`Verified Charge: 200 CD.`). All frontier payouts route through
the existing skew (`economy/payout` at idx 25 → rate 100 - min(idx/4, 25) = 75% floor) so
the wallet still *feels* the residual 25-point instability, but nobody is selling propaganda
anymore. If the player has liberated fields, the skew is even gentler (relief tiers) — no
new economy text needed; reuse the existing per-payout rate line.

---

## 5. New tags / scores introduced

| tag / score | set by | gated by (readers) |
|-------------|--------|--------------------|
| `frontier_registered` (tag) | `frontier/register` (Registrar sign button) | all frontier quest lines; challenger/brain hail availability (flavor) |
| `frontier_passes` (score, dummy) | `frontier/register` (+1 free), `frontier/buy_pass` (+1 per 200 CD) | flavor / future set-mode gate; sidebar exchange |
| `frontier_plaque_read` (tag) | `frontier/read_plaque` | Caretaker `cleared_echo` gate; plaque quest not-done; Registrar plaque-pointer button gate |
| `defeated_<facility>_challenger_1/2` (tags, ×14) | each challenger `defeat_tag` on RCT win | already-beaten line |
| `frontier_<facility>_cleared` (tags, ×7) | each brain `defeat_tag` = shipped `achievementOnDefeat` | `q.side_frontier_hall` retarget; Selene recognition `all_tags`; `#halls` count; Registrar warden-pointer gate |
| `frontier_all_cleared` (tag) | Cave brain `defeat_tag` = shipped `achievementOnDefeat` | frontier hub done; Caretaker echo; capstone line hidden |
| `#halls` (quest_hud scratch fake-player) | `frontier/hall_cleared` recount | `$(halls)/7` sidebar macro |

> **No new numeric BAND tags** are needed — the frontier gates on plain tags + the existing
> `royal_league_champion` / `champion` sugar. The `frontier_passes` score is read as a raw
> `score` gate (§6 escape hatch) only if a set-mode gate is added later.

---

## 6. Build checklist

1. **Confirm coords + roster with showrunner** (§Open questions Q1/Q2): lock whether the
   frontier lives at the shipped x≈3800 cluster or the atlas [4096 2965], and whether
   `the_market` group folds in as the concourse (this doc assumes yes). Fix all `placement`
   stubs accordingly before dropping files.
2. **Drop 26 character files** under `dialog-src/characters/frontier/`: 24 over the shipped
   RCT ids + 2 new lore NPCs (`frontier_registrar` + `frontier_caretaker`) — use the
   canonical stubs + fill table (§3.2). Set per-NPC `win/lose/already` flavor from each
   shipped team's identity (no `"`, `'`, or `%`).
3. **Drop 5 dialog files** under `dialog-src/dialog/`: `frontier_registrar`,
   `frontier_challenger_generic`, `frontier_brain_generic`, `frontier_brain_cave` (Selene
   override), `frontier_caretaker`. Point each character's `dialog` field at the right one
   (challengers + non-cave brains share the two generics).
4. **Add functions** under `function/frontier/`: `register`, `buy_pass`,
   `decline_challenger`, `decline_brain`, `hall_cleared`, `read_plaque`, and
   `frontier_complete` — all following the Genji pay-probe / `economy/payout` /
   count-check idioms. The `decline_*` functions are the **friendly** variant (broke →
   close, never fight). Add `scoreboard objective add frontier_passes dummy` to
   `economy/load` (or a `frontier/load` hooked into it).
5. **Wire the sidebar:** add `q.side_frontier` (82), `q.side_frontier_hall` (83),
   `q.side_frontier_done` (84), `q.side_frontier_plaque` (85) render blocks to
   `function/quest/render.mcfunction` (+ a `frontier/set_line` macro for `$(halls)`), then
   append the matching stage blocks to `registers/quest_targets.json` (§3 blocks above).
   **Slots 82–85 are confirmed free** (57–60 are taken; the frontier sits in the endgame
   band just under `q.main` at 100).
6. **Register achievements** if not already present: the shipped RCT `achievementOnDefeat`
   values (`frontier_<facility>_cleared`, `frontier_all_cleared`) must exist as
   advancement/tag latches so the character `defeat_tag`s line up.
7. **Compile:** run `scripts/content_compile` → presets + `quest_waypoints.json`; verify
   in-sync; `generate_npc_function` / `update_preset_index` as usual.
8. **Runtime smoke test** (post-install run, walk-up, track): sign at the Registrar,
   buy/decline a pass, opt into one challenger + one brain, confirm the `$(halls)` counter
   ticks, decline while broke and confirm NO fight fires, clear all seven, confirm Selene
   unseals, read the plaque, confirm **no name is spoken anywhere**.

---

## 7. Open questions for showrunner

1. **Coords: shipped cluster vs atlas.** The RCT file spawns everything at x≈3782–3818 /
   z≈2959–2999; the prompt anchors [4096 2965] with 4000s facility coords. Which is the
   real build location? This doc uses the shipped coords as placement anchors — confirm or
   hand me the atlas coords and I will swap every `placement` stub.
2. **`the_market` fold-in.** The shipped file has 8 groups; the prompt lists 7 facilities
   (no Market). I folded `the_market` (Sterling/Vance/Fiona) in as the **exchange concourse
   / seventh counted hall** so no id is left uncast. OK, or should Market be dropped/renamed
   to one of the prompt's subtitles (e.g. is `the_port` the intended "Battle Port" and "The
   Market" surplus)?
3. **Rental / set-mode teams (the Nuzlocke-critical one).** RCT fights the player's real
   party; there is no shipped rental swap. This doc makes every frontier fight opt-in +
   decline-able as the safety valve, and gives the Factory "rental" flavor. Do you want me
   to spec a real **party-stash helper** (box the player's team → give a fixed frontier
   loadout → restore on exit) as engine work, or is "opt-in above-cap risk" acceptable?
4. **Arcade modifiers.** The subtitle promises RNG battle modifiers ("the house always
   spins"). RCT/TBCS cannot roll per-battle rule modifiers from a datapack. Is Arcade
   flavor-only (roulette is set-dressing), or do you want a pre-battle `random`-driven
   buff/debuff applied to the player (a status potion, a bag item) as the "spin"?
5. **Already-beaten entry vs `already_beaten_line`.** Does the compiler auto-inject an
   already-beaten dialog entry from the character `already_beaten_line` when `defeat_tag`
   is present? If yes, the shared challenger/brain trees stay single-entry (as authored). If
   no, I add a per-character inline `already_beaten` entry — confirm the idiom.
6. **Grand purse.** Is the one-time 20000 CD `frontier_complete` payout on Selene's defeat
   welcome, or should the frontier stay item-reward-only (shipped master balls / netherite)
   to avoid flooding the late-game economy?
7. **Pass as a real gate.** `frontier_passes` is currently a soft/flavor sink. Do you want
   entry to a facility to actually *consume* a pass (hard-gate the battle button on
   `frontier_passes >= 1` and decrement on entry), or keep passes as pure CD-sink flavor?
8. **Friendly decline vs auto-gen `decline_fee`.** This doc's `decline_*` functions
   intentionally deviate from the compiler's auto-generated `decline_fee` behaviour
   (Company must-fight-on-broke) to hold the fairness floor (broke → close, never fight).
   Confirm the compiler will honour an explicit `decline_button → function ...` and not
   also emit a conflicting `route/decline_<id>` from `decline_fee`; if it always emits,
   I will drop `decline_fee` from the frontier battle blocks and rely on the explicit call.
```