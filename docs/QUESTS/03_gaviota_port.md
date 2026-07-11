# Gaviota Port — Unit Content Plan (03_gaviota_port)

> **Unit:** Gaviota Port · **Gym 5 (Water)** · Leader **Neptune** (`gaviota_leader`)
> **Slug:** `03_gaviota_port` · **Band:** entry cap **50**, memory fragment **frag_5**, `cd_instability → 40`
> **Recognition tier:** **MID** (badges 3–6) · **Approach:** R7 Gullwing Coast · **Forward hook:** Kalahar Reach / the desert / R8 Dunewind Trail
>
> This is a PLAN. Nothing here is wired yet. All JSON blocks are schema-valid and copy-paste-ready; the datapack specs are one-line-per-function. Read `dialog-src/schema/README.md` before building.

---

## 1. Overview

Gaviota Port is the region's **water-trade harbour** — the maritime commerce hub the Takehara mayor's water delegation was pointing at. It is the fifth gym on the fixed route, reached from the north along **R7 Gullwing Coast**, and it sits squarely in the **Act 2 slipping** economy register (`cd_instability` climbs to **40** on the Neptune win): the CobbleDollar is visibly wobbling, dock pays come up light, and sea-freight tariffs adjust in the Company's favour. Neptune himself flags it in his already-beaten line (the dock pays come up light — watch your coin near the water), so this unit's side content is the harbour proving him right.

**The arc job this town does:**
- Escalate the **economy-decay** register from Deepcore's prices adjusting to **overt sea-freight tariff gouging** — money you can feel at the wharf.
- Introduce the **maritime black market**: goods the Company over-taxes now move by tide, off the ledger. A smuggler/fence beat that is *sympathetic* (the Company created the shortage) but not clean.
- Introduce **wheat-by-sea**: the monopoly is now shipping grain in hulls, and one nearby field feeds those hulls. This unit carries the **first Gaviota-adjacent liberation hook** — **Westwind Fields** (`farm_3`, polygon ~[660, 3280], the farm the unit context flags at [657, 3279]).
- Surface the **harbour-union** tension: dockhands whose pay was quietly restructured, a union rep who half-remembers a founder who signed the port's original charter.
- Carry the **MID recognition band**: veteran Company handlers at the port place the face (you are supposed to be *filed*); civilians never do; the union rep recognises the *charter*, not the man.
- Plant the **forward hook to Kalahar Reach** (gym 6, Ground) via a freight manifest bound for the **Dunewind Trail** across the desert.

**Route position:** gym 4 Deepcore → **R7 Gullwing Coast → Gaviota Port (gym 5)** → R8 Dunewind Trail → Kalahar Reach (gym 6).

**Band constants (for gate authoring):**
- `recognition: "mid"` (badges 3–6) is the default band here.
- `memory_fragment == 4` before the Neptune win (Deepcore beaten, Gaviota not) → this is the score the sidebar gym line already uses for Gaviota (`quest_targets.json`).
- `cd_instability` sits at **40** post-Neptune; wheat-trader/black-market beats gate on `fields_liberated` and `cd_instability`, not badges.
- Westwind Fields liberation gates the port's wheat-by-sea payoff and pushes `cd_instability` **−6** (via the shared `liberation/free_field` core).

---

## 2. Cast

New NPCs introduced by this unit (the gym cast — Neptune, guide, 4 trainers, 2 apprentices — already exists under `dialog-src/characters/gym/gaviota_*` and is **not** re-authored here). **Verified additive:** there is no `dialog-src/characters/gaviota/` area folder yet, and the gym folder holds only leader/guide/trainers/apprentices — no town nurse, fence, dockmaster, fisher, wheat-handler, or union-hand body — so all six below are new.

| id | display_name | role | one-line concept | placement anchor |
|----|--------------|------|------------------|------------------|
| `gaviota_nurse` | Nurse Coralie | `healer` | Port Center nurse + **rumor hub**; paid heal (shared `economy/heal_paid`), points at all 4 side quests | Port Pokémon Center counter — **PLACEHOLDER** ~[560, 65, 3540] |
| `gaviota_dockmaster` | Dockmaster Kaito | `quest_giver` | Harbour-union foreman; the **sea-freight tariff / manifest audit** economy quest; half-remembers the port charter's signature | Harbourmaster's shack on the main pier — **PLACEHOLDER** ~[566, 64, 3560] |
| `gaviota_fence` | Fence Odessa | `merchant` | The **maritime black market** — a fence moving Company-overtaxed goods by tide; opt-in fetch (recover a seized crate) unlocks her stock | Under the fish-market boardwalk, a shuttered slip — **PLACEHOLDER** ~[552, 63, 3552] |
| `gaviota_fisher` | Netmender Bosun Rui | `quest_giver` | Fishing-fleet elder; **fetch → turn-in → opt-in wager** (the Genji gold idiom) restringing a deep-sea net; friendly above-cap wager, decline-able | End of the deep pier past the gym slips — **PLACEHOLDER** ~[605, 64, 3660] |
| `gaviota_smuggler` | Tidewatch Sable | `wheat_trader` | Company **grain-by-sea** handler at Westwind Fields' loading dock; sells the alternative currency, then **recognises the founder mid-trade and ambushes** (MID band) | Westwind Fields sea-dock, field edge — **PLACEHOLDER** ~[700, 65, 3255] |
| `gaviota_union_hand` | Dock Hand Mattias | `civilian` | Restructured-pay dockhand; back-echo civilian; never recognises the founder | Wharf crates, near the gym approach — **PLACEHOLDER** ~[580, 64, 3600] |

> **PLACEMENT RULE:** every anchor above is a `placement:{x,y,z}` **auto-spawn latch** (schema §2, OMIT `uuid`). All coords are PLACEHOLDER pending atlas/in-world confirmation, EXCEPT they are pinned to real fixtures: gym slips are baked at ~[593–605, 87, 3643–3660]; the Gaviota zone centroid is [570, 64, 3529]; Westwind Fields polygon centroid is ~[660, 3280]. Confirm y and exact x/z in-world before shipping; the compiler resolves quest waypoints from these `placement` coords.

---

## 3. Quests

Six beats: a **rumor hub**, a **fishing fetch→wager** (the Genji gold idiom), a **black-market fence** unlock, a **sea-freight economy** audit, a **wheat-by-sea liberation** hook, and one **civilian back-echo** greeter. Recognition and forward-hook lines are woven per beat.

---

### 3.1 Rumor Hub — Nurse Coralie (Port Center)

**Concept:** The one NPC every hardcore run visits repeatedly (paid heal via the shared `economy/heal_paid`). Doubles as the town's **arrival hub**: a word-on-the-docks button rolls one of the unit's open side quests (Genji-hub idiom — `sidequest/rumors/*` pattern, cloned from `rumors/lila`), not-done gated, with a static three-spot fallback. **Forward hook** lives in her fallback (names Kalahar / Dunewind). **Back-echo:** a liberated-field line and a Deepcore-price line.

**Character JSON** — `dialog-src/characters/gaviota/gaviota_nurse.json`:
```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "gaviota_nurse",
  "display_name": "Nurse Coralie",
  "role": "healer",
  "act": "1",
  "location": "Gaviota Port - Pokemon Center",
  "recognition_tier": "mid",
  "recipe": "healer",
  "dialog": "dialog:gaviota_nurse",
  "movement": { "objective": "ambient_stationary_look" },
  "service": { "kind": "heal" }
}
```

> **Recipe note:** `role: healer` infers `recipe: healer` (schema §13.1). Author it explicitly (matching `takehara/nurse_lila`) rather than borrowing `civilian`, so the Center attributes/objectives are the healer bundle.

**Dialog JSON** — `dialog-src/dialog/gaviota_nurse.json`:
```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "gaviota_nurse",
  "type": "STANDARD",
  "entries": [
    {
      "label": "default",
      "name": "Nurse Coralie",
      "priority": 10,
      "default": true,
      "say": [
        "Welcome to Gaviota Center. One hundred CobbleDollars and your team comes back whole - the port rate, same as everywhere now, revised last quarter by nobody who will sign for it.",
        "The salt gets into everything down here, even the invoices. Heal your team while I can still tell you the price out loud - and if you want work, ask what the word on the docks is."
      ],
      "buttons": [
        {
          "label": "heal_button",
          "text": "Heal my team - posted rate",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:economy/heal_paid", "as_player": true },
            { "do": "open_dialog", "label": "healed" }
          ]
        },
        {
          "label": "rumor_button",
          "text": "What is the word on the docks?",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/rumors/coralie", "as_player": true },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "Not right now", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "healed",
      "name": "Nurse Coralie - back to full",
      "priority": -1,
      "say": [
        "There. Fighting fit. Try to keep them that way - the fee rides the tide these days, and the tide is coming in.",
        "Rested and ready. Someone upstream counts every one of these visits. Down here we just wave the boats through."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Thank you", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:**
- `function/sidequest/rumors/coralie.mcfunction` — **clone of `sidequest/rumors/lila`** (verified: rolls `random value 1..5` on `#rumor quest_hud`, serves one rumor per its own not-done tag gate, sets `#rumor_hit`, static three-spot fallback when `#rumor_hit == 0`, ends on `item.book.page_turn`). Spots (with the not-done gate tag):
  1. Netmender Bosun Rui's deep-net wager (`!bosun_net_done`) — Rui hung his nets to dry and will not fish. Bring the man deep-sea line and he may remember the fleet owes you a lesson.
  2. Fence Odessa's seized crate (`!odessa_crate_recovered`) — There is a woman under the fish-market boards who sells what the tariff took. She wants a crate back off the customs float first.
  3. Dockmaster Kaito's manifest audit (`!gaviota_manifests_filed`) — Kaito on the main pier is short three manifests and shorter three pays. He is counting barrels the Company already counted for him.
  4. Westwind Fields liberation (`!defeated_gaviota_wheat_sea`) — They are barging grain out of Westwind Fields now, east along the coast. Wheat by sea. Somebody should cut the ropes.  **Gate primitive (resolved, see Open Q3):** gate on `!defeated_gaviota_wheat_sea` (the ambush-win tag), NOT on `field_freed` — `field_freed` is a *scoreboard* and the rumor idiom gates on tags. The win tag fires on the actual liberation, so the rumor retires exactly when the field is freed.
  5. **Back-echo** spot (always eligible; no not-done gate) — The dock pays came up light again. Same as Deepcore, same story - the money is not the money it was. You feel it in the till before you feel it anywhere else.
- **Forward-hook line** baked into the `#rumor_hit == 0` fallback text (Kalahar / Dunewind), e.g.: Slow tide for gossip. The freight talk is all one word - Dunewind. The desert road east to Kalahar Reach, and everything the port cannot sell here goes that way now.
  - **Macro safety:** all rumor `tellraw` bodies are hand-written JSON components in the datapack file (like `rumors/lila`), so they follow that file's quoting; keep apostrophes out of the plain-text runs (the examples above are already apostrophe-free) per HARD RULE 1 on macro-delivered text.

**QUEST_TARGETS entry:** none of its own — the rumor hub is a service NPC, not a tracked quest. (It *feeds* the other quests' sidebar lines by pointing at them in dialog.)

**REWARD/BALANCE:** paid heal only (shared `economy/heal_paid`, fee = 100 + 2×`cd_instability`, balance-gated pay-probe). No battle. No cap concern.

---

### 3.2 Fishing Fetch → Wager — Netmender Bosun Rui (deep pier)

> **This is the GOLD-IDIOM beat** — a direct structural mirror of Fisherman Genji (`sq_genji_out_of_office`): fetch an item → **turn-in via a count-check datapack function that clears+counts** (never a `has_item` gate, which is BROKEN per HARD RULE 4) → then an **opt-in, decline-able, above-cap wager with the stake PRINTED**.

**Concept:** Rui is the retired boatswain of Gaviota's deep-sea fleet; his big nets rotted and he will not fish until they are restrung. Bring **8× `minecraft:string`** (vanilla source; validate any Cobblemon deep-fish drop before promising it in-line) and he restrings two nets — keeps one, hands you a **fishing reward** + CD. Then, one boatswain to a promising deckhand, he offers a **friendly 200 CD wager** against his two lv **52** water types (above the cap-50 entry — **OPT-IN, decline-able, fail-soft**). **Forward hook:** his post-wager line names the Dunewind freight. **Back-echo:** he references the Takehara falls-fisher (Genji) — another river-man taught you the rod; let a sea-man teach you the tide.

**Wager cap note:** Rui's aces at **52** sit **+2 over the cap-50 entry** — the same fought-slightly-over friendly convention as Genji's. Because it is above the current cap it MUST be opt-in, the stake printed, decline-able with a fee, and fail-soft. Per HARD RULE 5, a player with **no caught Pokémon** cannot be whited-out by it (the wager button only appears after the rod/net turn-in and is player-initiated).

**Character JSON** — `dialog-src/characters/gaviota/gaviota_fisher.json`:
```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "gaviota_fisher",
  "display_name": "Netmender Bosun Rui",
  "role": "quest_giver",
  "act": "1",
  "location": "Gaviota Port - The Deep Pier",
  "recognition_tier": "mid",
  "trainer": "sq_rui_wager",
  "recipe": "civilian",
  "dialog": "dialog:sq_rui_deep_net",
  "movement": { "objective": "ambient_stationary_look" },
  "battle": {
    "trainer": "sq_rui_wager",
    "type": "wager",
    "format": "GEN_9_SINGLES",
    "prize": 200,
    "loss_fee": 200,
    "decline_fee": 100,
    "defeat_tag": "defeated_sq_rui_wager",
    "win_line": "Ha - you read the tide, not the wave. Two hundred, counted on the barrelhead. The fleet could use a hand like that.",
    "lose_line": "The sea keeps the stake, boy. It keeps everything eventually. No hard feelings and no refunds - I stopped giving those out the day the pay got restructured.",
    "already_beaten_line": "One wager a tide. My knees cannot audit two before the water turns.",
    "on_win": [
      "execute as @1 run function cobblemon_initiative:economy/wager_sweetener"
    ]
  }
}
```

**Dialog JSON** — `dialog-src/dialog/sq_rui_deep_net.json`:
```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "sq_rui_deep_net",
  "type": "STANDARD",
  "entries": [
    {
      "label": "net_flavor",
      "name": "Rui - the count",
      "priority": 40,
      "gate": { "defeated": "sq_rui_wager" },
      "say": [
        "Good tide today. I log the catch, force of habit - three runs, one keeper, one that broke the line and my pride with it. A cleaner tally than the dock master ever gets to keep.",
        "The sea does not restate its earnings. That is the difference between the water and the Company - one of them still tells you the truth about what you hauled in. The rest of the region ships east now, to Dunewind and the desert - but the tide keeps honest books."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Fish a while", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "wager",
      "name": "Rui - a friendly tide",
      "priority": 30,
      "gate": { "tag": "bosun_net_done", "not_tag": "defeated_sq_rui_wager" },
      "say": [
        "That net will hold now. So - one boatswain to a promising deckhand - a wager. Two hundred CobbleDollars says my two deep-water pair put yours on the sand. They run a shade over your cap, so it is a stretch, not a mugging. Lose and the stake is mine. Decline and we still share the pier.",
        "No shame in waving it off. The tide waits, and so, it seems, do I. Two hundred on the line if you take it - their team runs over your cap, so eyes open."
      ],
      "buttons": [
        {
          "label": "wager_button",
          "text": "Take the wager - 200 CD on the line (their team runs level 52, over your cap)",
          "actions": [ { "do": "battle" } ]
        },
        {
          "label": "decline_button",
          "text": "Wave it off (100 CD non-engagement)",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:route/decline_sq_rui_wager", "as_player": true },
            { "do": "close" }
          ]
        }
      ],
      "no_goodbye": true
    },
    {
      "label": "default",
      "name": "Netmender Bosun Rui",
      "priority": 10,
      "default": true,
      "gate": { "not_tag": "bosun_net_done" },
      "say": [
        "Rui. Boatswain of the Gaviota deep fleet, forty years, until the big nets rotted through and the yard stopped ordering line - budget adjustment, they called it. Bring me eight lengths of string and I will restring two deep-sea nets, one for the fleet and one for you.",
        "You have the look of a river-hand. Somebody taught you a rod once. Bring me eight lengths of string and I will restring two deep-sea nets - one for the fleet, one for you - and I will teach you what the tide teaches. String comes off the water, off the wharf, off any spider that dares the boardwalk after dark."
      ],
      "buttons": [
        {
          "label": "turn_in_button",
          "text": "Hand over 8 string",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/rui/turn_in_net", "as_player": true },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "I will go find some", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

> **HARD RULE 2 note:** each `say[]` here is authored so **both** pages stand alone — the default entry now states the eight-string ask in *both* lines (a random page can never leave the player without the task), and the turn-in button + rumor hub + sidebar all carry the ask redundantly. No page is a continuation of another.

**DATAPACK NEEDS:**
- `function/sidequest/rui/turn_in_net.mcfunction` — **clone of `sidequest/genji/turn_in_rod`** (verified idiom): `execute store result score @s ci_sq_scratch run clear @s minecraft:string 0`; `execute if score @s ci_sq_scratch matches 8.. unless entity @s[tag=bosun_net_done] run function …/rui/net_success`; `execute if score @s ci_sq_scratch matches ..7 run tellraw @s` a not-eight-yet line (string comes off the wharf after dark). Reuses the shared `ci_sq_scratch` objective.
- `function/sidequest/rui/net_success.mcfunction` — **clone of `sidequest/genji/rod_success`**: `clear @s minecraft:string 8`; hand a fishing reward (**recommend CD + a net-themed loot table, not a second `cobblemon:poke_rod`** — Genji already grants the rod, and the id is UNVERIFIED in 1.7.3; see Open Question 1); `function cobblemon_initiative:economy/payout {amount:300}`; `tag @s add bosun_net_done`; actionbar title sting (Restrung. The deep nets remember the fleet - and so, apparently, do you.).
- `function/route/decline_sq_rui_wager.mcfunction` — **compiler auto-generates this from `battle.decline_fee`** (verified: `content_compile` `register_decline` / `write_decline_functions`, `route/decline_<base_trainer>`). Do NOT hand-write; listed only so the build checklist accounts for it. Pay-probe 100 CD; on paid → receipt actionbar + permanent `declined_sq_rui_wager`; on broke → the wager fires (must-fight, same terms). NOTE: since this wager is **fully opt-in via a button** (not a forced ON_DISTANCE battle), the decline is a courtesy alternative, not a stand-down gate; the broke-then-battle branch matters only if the player clicks decline while broke.

**QUEST_TARGETS entry** — add to `dialog-src/registers/quest_targets.json` (new holder `q.side_rui`, **slot 85** — verified free; see §6 slot audit):
```json
{
  "holder": "q.side_rui",
  "name": "Mending the Deep Nets",
  "slot": 85,
  "stages": [
    {
      "if_tags": ["bosun_net_done"],
      "not_tags": ["defeated_sq_rui_wager"],
      "label": "Take Bosun Rui up on his wager",
      "target": { "npc": "gaviota_fisher" }
    },
    {
      "if_tags": [],
      "not_tags": ["bosun_net_done"],
      "label": "Bring Bosun Rui 8 string for the deep nets",
      "target": { "npc": "gaviota_fisher" }
    }
  ]
}
```

**REWARD/BALANCE:** Turn-in pays **300 CD** (via skewed `economy/payout`, so the haircut reads on stream) + a fishing/training reward. Wager purse **200 CD** flat (+`wager_sweetener` roll of +25..100 on win). Loss fee **200 CD**, decline fee **100 CD**. **Cap-legality:** wager aces at **52** = entry-cap-50 + 2; **above cap → opt-in, stake printed in the button text, decline-able, fail-soft** (compliant with HARD RULE 5). One-time (defeat-tag gated), no loop.

---

### 3.3 The Maritime Black Market — Fence Odessa (under the boardwalk)

**Concept:** Odessa fences goods the Company **over-taxes** — the tariff created a shortage, and the tide fills it off the ledger. She is guarded until you prove you are not customs: an opt-in **fetch** (recover a crate the customs float seized) unlocks her **black-market stock** (`trade_black_market` snippet). She is sympathetic (the Company made the shortage) but not clean — a grey beat. **Forward hook:** her stock talk mentions desert goods routed to Dunewind. **Back-echo:** she references the port charter and the revised tariff schedule (ties to Kaito's beat and the Deepcore price-adjust register). **Civilian recognition:** Odessa is a fence, not Company — she **never recognises the founder** (HARD RULE 7); she reads the economy decay, not the face.

**Character JSON** — `dialog-src/characters/gaviota/gaviota_fence.json`:
```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "gaviota_fence",
  "display_name": "Fence Odessa",
  "role": "merchant",
  "act": "1",
  "location": "Gaviota Port - Under the Boardwalk",
  "recognition_tier": "mid",
  "recipe": "shopkeeper",
  "dialog": "dialog:gaviota_fence",
  "movement": { "objective": "ambient_stationary_look" },
  "trade": { "snippet": "trade_black_market", "open_label": "shop" }
}
```

**Dialog JSON** — `dialog-src/dialog/gaviota_fence.json`:
```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "gaviota_fence",
  "type": "STANDARD",
  "entries": [
    {
      "label": "open_shop",
      "name": "Odessa - the tide market",
      "priority": 30,
      "gate": { "tag": "odessa_crate_recovered" },
      "say": [
        "You got the crate back off the float. Good. Then we are past introductions. This is what the tariff took and the tide returned - same goods, honest weight, no verified surcharge stapled on top.",
        "Buy what you like. Half of it is bound for the desert road anyway - Dunewind takes everything the port is not allowed to sell here. Funny how the things they overtax always find the water."
      ],
      "buttons": [
        { "label": "shop_button", "text": "See the tide market", "actions": [ { "do": "trade" } ] },
        { "label": "leave_button", "text": "Later", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Fence Odessa",
      "priority": 10,
      "default": true,
      "gate": { "not_tag": "odessa_crate_recovered" },
      "say": [
        "Easy. You are not customs - customs does not come down here alone. I move what the Company prices out of reach. That is not theft. Theft was the tariff. This is just the water correcting a bad ledger.",
        "Want to trade? Prove you are on the tide side. Customs floated a seized crate off the far slip and left it bobbing as a warning. Bring it back and my slip is open to you."
      ],
      "buttons": [
        {
          "label": "start_button",
          "text": "I will get your crate",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/odessa/start_crate", "as_player": true },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "Not my business", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:**
- `function/sidequest/odessa/start_crate.mcfunction` — latch `odessa_crate_started`; actionbar sting (Customs float, far slip. The crate is bobbing where they left it as a warning.). Sets up the fetch. (Waypoint moves to the crate prop coord via the register stage.)
- `function/sidequest/odessa/recover_crate.mcfunction` — **the turn-in.** Triggered by the crate prop / a barrier-interaction or a proximity `sidequest/odessa/tick` (design choice — simplest: a right-click block command block at the crate calling this, OR a small `tick` proximity check at the customs-float coord). On fire: `tag @s add odessa_crate_recovered`, `tag @s remove odessa_crate_started`, `function economy/payout {amount:150}` finder token (optional), title sting via `{do:announce}`-equivalent tellraw (CRATE RECOVERED - the tide market opens.). This unlocks the `open_shop` entry.
  - **Prop/coord note:** the customs float is a set-piece prop (barrel/chest on water). Use a literal `{x,y,z}` waypoint (PLACEHOLDER ~[540, 63, 3530], off the far slip) — no character file.
- No new trade prices (the `trade_black_market` snippet owns offers, per schema §10 / Open Question 5).

**QUEST_TARGETS entry** — new holder `q.side_odessa`, **slot 84** (verified free):
```json
{
  "holder": "q.side_odessa",
  "name": "The Tide Market",
  "slot": 84,
  "stages": [
    {
      "if_tags": ["odessa_crate_started"],
      "not_tags": ["odessa_crate_recovered"],
      "label": "Recover the seized crate off the customs float",
      "target": { "x": 540, "y": 63, "z": 3530 },
      "note": "Customs-float prop coord (PLACEHOLDER) - no character file. Confirm in-world off the far slip."
    },
    {
      "if_tags": [],
      "not_tags": ["odessa_crate_started"],
      "label": "Find the fence under the boardwalk",
      "target": { "npc": "gaviota_fence" }
    }
  ]
}
```

**REWARD/BALANCE:** No battle. Fetch reward: **shop access** (the point) + optional 150 CD finder token via skewed payout. No cap concern. Fail-soft: declining the fetch just leaves the shop locked; the market is a bonus, not a gate.

---

### 3.4 Economy Beat — Sea-Freight Tariff Audit — Dockmaster Kaito (main pier)

**Concept:** The Act-2 prices-adjusting register made physical at the wharf. Kaito, the harbour-union foreman, is being told the barrels he counts are worth less than the manifest says — sea-freight tariffs adjusted, in the Company's favour. He asks you to **cross-check three freight manifests** against the actual barrel counts at three wharf points; each discrepancy is a rounding-in-the-Company-favour receipt. Turn-in pays a union honorarium. **Recognition (MID):** Kaito is a veteran who remembers the port's founding charter — he half-recognises the *signature*, not the man (the charter that built this pier had a name on it they scraped off last spring; you have the look of that name, which is a foolish thing to say to a stranger). **Forward hook:** the third manifest is bound for Dunewind / Kalahar. **Back-echo:** the tariff schedule was revised the same quarter as Deepcore's prices and the port heal fee.

**Character JSON** — `dialog-src/characters/gaviota/gaviota_dockmaster.json`:
```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "gaviota_dockmaster",
  "display_name": "Dockmaster Kaito",
  "role": "quest_giver",
  "act": "1",
  "location": "Gaviota Port - Harbourmaster Shack",
  "recognition_tier": "mid",
  "recipe": "civilian",
  "dialog": "dialog:gaviota_dockmaster",
  "movement": { "objective": "ambient_stationary_look" }
}
```

**Dialog JSON** — `dialog-src/dialog/gaviota_dockmaster.json` (STANDARD, with a MID-recognition entry that only *selects a line*, plus the manifest quest states):
```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "gaviota_dockmaster",
  "type": "STANDARD",
  "entries": [
    {
      "label": "filed",
      "name": "Kaito - the union thanks you",
      "priority": 50,
      "gate": { "tag": "gaviota_manifests_filed" },
      "say": [
        "Three manifests, three shortfalls, all rounding in one direction - theirs. I filed it with the union. It will change nothing, but now it is written down, and written down is how you fight these people. Ask the founder, if you could find one.",
        "The dock owes you a pay it can barely make. Take it, and take my thanks - both are lighter than they should be, and we both know whose fault that is."
      ],
      "buttons": [
        {
          "label": "turn_in_button",
          "text": "Collect the union honorarium",
          "gate": { "score": { "objective": "quest_hud", "op": "gte", "value": 3, "holder": "#manifests" } },
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/manifest/turn_in", "as_player": true },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "Fair tides", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "counting",
      "name": "Kaito - count the barrels",
      "priority": 40,
      "gate": { "tag": "gaviota_manifest_check_active", "not_tag": "gaviota_manifests_filed" },
      "say": [
        "Three manifests, three wharf points. Read what the paper claims, count what the barrels say. Every gap is money that left the port without a boat. When you have all three, bring the count back to me.",
        "The last one is bound east - Dunewind, the desert road to Kalahar Reach. If they are shorting us on the way out of the region entirely, I want it on the record before it sails. Bring all three counts back to me here."
      ],
      "buttons": [
        {
          "label": "turn_in_button",
          "text": "Report the three shortfalls",
          "gate": { "score": { "objective": "quest_hud", "op": "gte", "value": 3, "holder": "#manifests" } },
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/manifest/turn_in", "as_player": true },
            { "do": "close" }
          ]
        },
        { "label": "leave_button", "text": "On it", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "recognise_mid",
      "name": "Kaito - a foolish thing to say",
      "priority": 30,
      "gate": { "recognition": "mid", "not_tag": "gaviota_manifest_check_active" },
      "say": [
        "The charter that built this pier had a name signed at the bottom. They scraped it off the plaque last spring - said it was never there, said there was never a founder, just the Company. You have the look of that name. Foolish thing to say to a stranger, so forget I said it.",
        "Dockmaster Kaito. Union foreman, when the union still means anything. You want work? I have a paper problem the Company would rather I did not solve."
      ],
      "buttons": [
        {
          "label": "start_button",
          "text": "What is the paper problem?",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/manifest/start", "as_player": true },
            { "do": "open_dialog", "label": "counting" }
          ]
        },
        { "label": "leave_button", "text": "Some other tide", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Dockmaster Kaito",
      "priority": 10,
      "default": true,
      "gate": { "not_tag": "gaviota_manifest_check_active" },
      "say": [
        "Dockmaster Kaito. I run the wharf, or I run what the wharf tells me to run. Lately the manifests and the barrels do not agree, and the disagreement is always the port losing.",
        "You want work? I have three manifests that will not add up. Count the barrels against them and I will pay you what the union can still afford."
      ],
      "buttons": [
        {
          "label": "start_button",
          "text": "I will count your barrels",
          "actions": [
            { "do": "command", "cmd": "function cobblemon_initiative:sidequest/manifest/start", "as_player": true },
            { "do": "open_dialog", "label": "counting" }
          ]
        },
        { "label": "leave_button", "text": "Not right now", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:**
- `function/sidequest/manifest/start.mcfunction` — latch `gaviota_manifest_check_active`; init `#manifests quest_hud` scratch to 0; actionbar sting (Three manifests, three wharf points. Read the paper, count the barrels.). **Function-path note:** name the folder `sidequest/manifest/*`; the tags it sets are Gaviota-scoped (`gaviota_manifest_*`) — no collision with the unrelated Lucian route-manifest quest (`took_route_manifest` / `manifest_paid`).
- `function/sidequest/manifest/tick.mcfunction` — **proximity logger** (clone of `sidequest/price_check/tick` idiom): while active and not filed, at each of the three wharf-point coords set a per-point tag (`gaviota_manifest_1/2/3`) once, print a rounding-in-the-Company-favour receipt actionbar per point, and recompute `#manifests` = count of the three tags. Wire into the mod's per-player tick.
- `function/sidequest/manifest/set_manifests.mcfunction` — sidebar macro (clone of `price_check/set_prices`): renders the dynamic sidebar line with `$(manifests)/3`.
- `function/sidequest/manifest/turn_in.mcfunction` — **the payout, run on returning to Kaito** (the dialog turn_in_button on both the `counting` and `filed` entries, gated on `#manifests >= 3`; mirrors the Genji turn_in_button placement). Guard `execute if score #manifests quest_hud matches 3.. run …`; `tag @s add gaviota_manifests_filed`; `function economy/payout {amount:400}` union honorarium; **title sting** via `{do:announce, as:title}` at authoring level — text ADJUSTMENT LOGGED, subtitle Rounding, in the Company favour - now on the record. (announce action builds the quoted command; no apostrophe/quote/percent per HARD RULE 1).
- **Three wharf-point props** — literal coords for the tick logger and waypoint (PLACEHOLDERS): P1 ~[566, 64, 3560] (main pier), P2 ~[588, 64, 3600] (wharf crates), P3 ~[605, 64, 3650] (deep pier, the Dunewind-bound load).

**QUEST_TARGETS entry** — new holder **`q.side_freight`**, **slot 83** (verified free). *Renamed from the doc's earlier `q.side_manifest`, which COLLIDES with the existing `q.side_manifest` holder at slot 58 ("Right of Way", the Lucian route-manifest quest). Do not reuse that holder id.*
```json
{
  "holder": "q.side_freight",
  "name": "Adjusted Freight (The Manifest Audit)",
  "slot": 83,
  "stages": [
    {
      "if_tags": ["gaviota_manifest_check_active"],
      "not_tags": ["gaviota_manifests_filed"],
      "scores": [ { "objective": "quest_hud", "holder": "#manifests", "op": "gte", "value": 3 } ],
      "label": "Report the manifest shortfalls to Dockmaster Kaito",
      "target": { "npc": "gaviota_dockmaster" }
    },
    {
      "if_tags": ["gaviota_manifest_check_active"],
      "not_tags": ["gaviota_manifests_filed"],
      "label": "Cross-check the freight manifests  $(manifests)/3",
      "dynamic": true,
      "target": { "x": 566, "y": 64, "z": 3560 },
      "note": "$(manifests) = #manifests quest_hud scratch (manifest/set_manifests macro). Three wharf-point props (PLACEHOLDER coords); main pier anchors the walk. Report back at Kaito when all three are logged."
    }
  ]
}
```

**REWARD/BALANCE:** No battle. Union honorarium **400 CD** via skewed `economy/payout` (the haircut is thematically perfect here — the audit itself gets shorted). No cap concern. Fail-soft: proximity logger, no way to soft-lock; sidebar always points at the next unlogged point, and the turn-in button only unlocks at 3/3.

---

### 3.5 Wheat-by-Sea Liberation Hook — Tidewatch Sable (Westwind Fields dock)

**Concept:** The monopoly is now **shipping grain by sea** — Westwind Fields (`farm_3`, ~[660, 3280]) has a coastal loading dock, and Sable is the Company handler running the hulls. She is a **wheat trader**: she first sells the alternative-currency pitch (Act-2 nervous-reassurance register), and — because this is the **MID recognition band and the port is a `fields_liberated`-gated trader beat** — once the player has liberated enough fields she **recognises the founder mid-trade and turns hostile** (an ambush, forced at `ON_DISTANCE_VERY_CLOSE`, per the `wheat_trader_1` gold pattern). **Beating her ambush LIBERATES Westwind Fields** (`farm_3`) via the shared `liberation/free_field {field:farm_3}` core — pushing `cd_instability` −6 and flipping the zone banner to Liberated. **Forward hook:** the liberated field's grain was bound for Dunewind. **Back-echo:** Westwind is on the Hua Zhan greenhouse dispatch board (the archivist recites it) — the world named this field two towns ago; now the player reaches it.

> **Field id is `farm_3`** — CONFIRMED against `install.json` (`activeWhenHolder: "farm_3"` at line ~5943 is the Westwind Fields zone), NOT the dispatch-board recitation order. The liberation name-map in `function/liberation/load.mcfunction` currently only has `farm_1:"FIRSTFURROW"`; this unit **adds the `farm_3` name entry** (see datapack needs). `free_field` / `free_field_apply` / `ceremony` handle `farm_3` generically once the name-map entry exists.

**Character JSON** — `dialog-src/characters/gaviota/gaviota_smuggler.json` (mirrors `wheat_trader_1`: `grain_buyer` recipe, `trade_wheat_trader`, forced ambush trigger gated `wheat_trader: hostile`, a `villain_forced` battle whose `on_win` liberates the field):
```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "gaviota_smuggler",
  "display_name": "Sable",
  "role": "wheat_trader",
  "act": "1",
  "location": "Gaviota Port - Westwind Fields Sea Dock",
  "recognition_tier": "mid",
  "recipe": "grain_buyer",
  "dialog": "dialog:gaviota_wheat_sea",
  "triggers": [
    {
      "on": "ON_DISTANCE_VERY_CLOSE",
      "actions": [
        { "do": "battle", "gate": { "wheat_trader": "hostile" } }
      ]
    }
  ],
  "trade": { "snippet": "trade_wheat_trader", "open_label": "shop" },
  "battle": {
    "trainer": "wheat_trader_ambush",
    "type": "villain_forced",
    "format": "GEN_9_SINGLES",
    "prize": 500,
    "defeat_tag": "defeated_gaviota_wheat_sea",
    "despawn_on_win": false,
    "win_line": "Fine - take the hulls, take the field. It was never grain to me anyway, just a number that floated.",
    "lose_line": "Logged. The Company thanks you for the tonnage.",
    "already_beaten_line": "No. The tide already took me once. Word went down the coast about you.",
    "on_win": [
      "execute as @1 run function cobblemon_initiative:liberation/free_field {field:farm_3}"
    ]
  }
}
```

**Dialog JSON** — `dialog-src/dialog/gaviota_wheat_sea.json` (STANDARD; the wheat-trader recognition arc: hostile > suspicious > default; note this reuses the SAME shape as `dialog:wheat_trader` — **consider `"dialog:wheat_trader"` reuse** if its lines already fit, otherwise author this Gaviota-specific sea variant. The existing `dialog:wheat_trader` hostile line embeds a `§k` glitch mid-word; this sea variant is written plain — either is compiler-legal, pick one voice):
```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "gaviota_wheat_sea",
  "type": "STANDARD",
  "entries": [
    {
      "label": "hostile",
      "name": "Sable - the tide turns",
      "priority": 30,
      "gate": { "wheat_trader": "hostile" },
      "say": [
        "Wait. I know that face. It came down off every wall in every branch and here it is, buying grain off my dock like a stranger. You are supposed to be filed. You are supposed to be gone.",
        "There was never a founder - that is the line, that is what they told us to say. So you are no one. And no one is going to walk off this dock knowing what sails from it."
      ],
      "buttons": [
        {
          "label": "fight_button",
          "text": "Stand and fight",
          "actions": [ { "do": "battle" } ]
        }
      ],
      "no_goodbye": true
    },
    {
      "label": "suspicious",
      "name": "Sable - a better ledger",
      "priority": 20,
      "gate": { "wheat_trader": "suspicious" },
      "say": [
        "The CobbleDollar is having a hard quarter - you have felt it, everyone has. There is an alternative. Something you can hold, something that grows, something the Company stands behind by the hull-load. Ask me how the grain trade works.",
        "Prices adjusting got you nervous? Grain does not adjust. Grain is grain. Trade with me and you trade in something real - for now, while it is still on offer."
      ],
      "buttons": [
        { "label": "shop_button", "text": "See the grain trade", "actions": [ { "do": "trade" } ] },
        { "label": "leave_button", "text": "Not today", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Tidewatch Sable",
      "priority": 10,
      "default": true,
      "say": [
        "Westwind Fields loads here now. Grain in from the barns, hulls out on the tide - the whole harvest by sea, bound east for Dunewind and the desert markets. Clean, quiet, off the road tolls entirely.",
        "You look like buying weather. The Company backs a grain note these days, hull-verified. Ask me about it, or watch the barges and wonder where the money really went."
      ],
      "buttons": [
        { "label": "shop_button", "text": "Ask about the grain note", "actions": [ { "do": "trade" } ] },
        { "label": "leave_button", "text": "Just watching the tide", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

> **IMPORTANT — one-NPC wiring.** Per the `wheat_trader_1` gold pattern, Sable's own ambush (`wheat_trader_ambush`, `type: villain_forced`) carries the `on_win` that runs `liberation/free_field {field:farm_3}` — so **defeating Sable in the ambush liberates Westwind Fields**. If production prefers the Firstfurrow split (a distinct perimeter guard cleared first, then the field flips, Sable as trade+recognition only), split into a `gaviota_field_guard` character carrying the `free_field` on_win and demote Sable's ambush to a plain recognition fight. Either wiring uses the same `liberation/free_field {field:farm_3}` core. **Recommend the single-NPC wiring** (Sable = trade → recognise → ambush → liberate) for a tighter port beat unless the field build already has a separate perimeter (Open Question 2).

**DATAPACK NEEDS:**
- **`function/liberation/load.mcfunction` name-map addition** — the ONLY edit this unit needs to an existing datapack file: add `farm_3` to the ceremony name map so the LIBERATED title card prints the real name instead of THE PARCEL:
  `data merge storage cobblemon_initiative:liberation {names:{farm_3:"WESTWIND FIELDS"}}`
  (append alongside the existing `data merge storage cobblemon_initiative:liberation {names:{farm_1:"FIRSTFURROW"}}` merge; `data merge` is additive.)
- **No new liberation function** — `liberation/free_field` / `free_field_apply` / `ceremony` already handle the `farm_3` case generically once the name-map entry exists. The `on_win` above wires it. `wheat_trader_ambush` is an EXISTING RCT trainer id (reused from `wheat_trader_1`); if a Gaviota-specific ambush team is wanted, add a new trainer JSON under `data/rctmod/trainers/` — not required for the beat.
- **`fields_liberated` band tags** — `wheat_trader: "suspicious"`/`"hostile"` lower to the `wheat_trader_suspicious`/`wheat_trader_hostile` tags maintained by the existing `wheat_trader/tick` poller off the `fields_liberated` score. No new work; verify the poller runs at the Gaviota dock (it is a per-player tick, so it does).

**QUEST_TARGETS entry** — this beat rides the **existing** `q.side_wheat` ("The Wheat War", slot 80) holder (the map-wide liberation counter), so **no new holder is required**. Optionally add a Gaviota-specific pointer stage on a new low-priority holder `q.side_westwind` if production wants a dedicated waypoint at the sea-dock; if so, use **slot 82** (verified free):
```json
{
  "holder": "q.side_westwind",
  "name": "Wheat by Sea (Westwind)",
  "slot": 82,
  "stages": [
    {
      "if_tags": ["wheat_war_active"],
      "not_tags": ["defeated_gaviota_wheat_sea"],
      "label": "Cut the grain barges at Westwind Fields",
      "target": { "npc": "gaviota_smuggler" },
      "note": "Clears on the ambush win (defeated_gaviota_wheat_sea, which fires free_field {field:farm_3}). Tag-gated to match the rumor idiom, since field_freed is a scoreboard not a tag (Open Q3). wheat_war_active latches on the first map liberation, so this lights once the wheat war is live and Westwind is still occupied."
    }
  ]
}
```

**REWARD/BALANCE:** Ambush battle prize **500 CD** (villain-tier). The real reward is the **field liberation**: `−6 cd_instability`, +1 to `fields_liberated` (advances the HQ gate + relief shop tier), Westwind zone banner → Liberated, LIBERATED title card (WESTWIND FIELDS). **Cap-legality:** the ambush is a `villain_forced` fight at/near the cap-50 band — set the `wheat_trader_ambush` team **at or below 50** so it is cap-legal as a forced encounter (HARD RULE 5: no forced fight above cap). **Fairness floor:** the `ON_DISTANCE_VERY_CLOSE` trigger only fires once the player is at the dock trading (present and playing with a party); the ambush is gated on `wheat_trader_hostile`, which requires enough liberated fields — a brand-new starter-only player is not in that band and cannot be forced into it. **Decline:** the trade is always declinable (walk away); only the recognition ambush is forced, and only in-band.

---

### 3.6 Civilian Back-Echo — Dock Hand Mattias (wharf)

**Concept:** A restructured-pay dockhand — the human face of the tariff. He **never recognises the founder** (civilian scrubbing worked; HARD RULE 7) but he *feels* the propaganda decay. Pure flavor greeter, no battle, no reward — a **back-echo** node that references a liberated field and the revised pay the whole town runs on, and softly plants the **Dunewind/Kalahar** forward hook. Uses a STANDARD dialog whose *only* variation is a post-liberation line (selected by `fields_liberated`, line-selection only).

**Character JSON** — `dialog-src/characters/gaviota/gaviota_union_hand.json`:
```json
{
  "$schema": "../../schema/character.schema.json",
  "kind": "character",
  "id": "gaviota_union_hand",
  "display_name": "Dock Hand Mattias",
  "role": "civilian",
  "act": "1",
  "location": "Gaviota Port - The Wharf",
  "recognition_tier": "mid",
  "recipe": "civilian",
  "dialog": "dialog:gaviota_union_hand",
  "movement": { "objective": "ambient_wander", "home": { "x": 580, "y": 64, "z": 3600 } }
}
```

**Dialog JSON** — `dialog-src/dialog/gaviota_union_hand.json`:
```json
{
  "$schema": "../schema/dialog.schema.json",
  "kind": "dialog",
  "id": "gaviota_union_hand",
  "type": "STANDARD",
  "entries": [
    {
      "label": "post_liberation",
      "name": "Mattias - the barges stopped",
      "priority": 20,
      "gate": { "fields_liberated": { "op": "gte", "value": 1 } },
      "say": [
        "Word came down the coast - somebody cut the grain barges loose upcountry. Wheat prices twitched, the till breathed easier for a day. First good news the wharf has had since the pay got revised. Nobody signed for that either.",
        "You hear the freight talk? Everything not nailed down goes east now - Dunewind, the desert road to Kalahar Reach. If the port cannot sell it here, it sails. I just load the hulls and try not to do the math."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Keep your back straight", "actions": [ { "do": "close" } ] }
      ]
    },
    {
      "label": "default",
      "name": "Dock Hand Mattias",
      "priority": 10,
      "default": true,
      "say": [
        "Twenty years on this wharf. Same barrels, same tide, half the pay - restructured, they call it, like my rent restructured itself to match. I do not know who decides these things. I just know it is never me and never you.",
        "You feel it too? The coins do not spend like they used to. Same number on them, less on the far side of the counter. The whole port is quietly getting shorter, and everybody smiles about it."
      ],
      "buttons": [
        { "label": "leave_button", "text": "Fair tides", "actions": [ { "do": "close" } ] }
      ]
    }
  ]
}
```

**DATAPACK NEEDS:** none (pure dialog; `fields_liberated` gate lowers to the compiler band tag).

**QUEST_TARGETS entry:** none (civilian flavor node, not a tracked quest).

**REWARD/BALANCE:** none. No battle, no cap concern.

---

## 4. Recognition & Economy Beats

**Recognition band: MID (badges 3–6).** Rank × proximity gradient for this unit:
- **Veterans / management (Sable, the wheat handler):** the alarm tier. Once `fields_liberated` pushes her to `wheat_trader_hostile`, she recognises the face **mid-trade** and ambushes — you are supposed to be *filed* / there was never a founder — so you are no one. (LORE_BIBLE §4 gradient, verbatim intent; the Founder's *name* is never spoken — this is common-noun recognition, canon-safe.)
- **Union veteran (Kaito):** the oblique tier — he recognises the **charter signature**, not the man (the charter that built this pier had a name they scraped off the plaque; you have the look of that name). MID-band, line-selection only (`recognition: "mid"` entry).
- **Civilians (Mattias, Nurse Coralie, Fence Odessa):** **never** recognise the founder (HARD RULE 7 / scrubbing worked). They feel the economy decay only.
- **No LATE stand-down beats here** — those are gym 7+ / late band. This unit stays in confused-alarm / oblique-recognition territory.

**Economy voice: Act-2 slipping register (`cd_instability` ~40).** Nervous reassurance, over-explaining adjustments:
- **Nurse Coralie:** the port rate, revised last quarter by nobody who will sign for it — the heal fee rides the tide.
- **Dockmaster Kaito:** the manifest shortfalls, rounding in one direction — theirs; the union files it because written down is how you fight these people.
- **Fence Odessa:** the tariff *created* the black market — theft was the tariff; this is the water correcting a bad ledger.
- **Sable (wheat trader):** the Act-2 alternative-currency pitch — the CobbleDollar is having a hard quarter; grain does not adjust.
- **Streamable receipts / title cards:** manifest turn-in fires `{do:announce, as:title}` ADJUSTMENT LOGGED; the Westwind ambush win fires the shared LIBERATED title card WESTWIND FIELDS; crate recovery fires CRATE RECOVERED - the tide market opens.
- **Field-liberation economy tug-of-war:** freeing Westwind (`farm_3`) is `−6 cd_instability` and advances the relief shop tier (CobbleDollar prices ease, Granary wheat prices worsen) via the shared `liberation/free_field` core — no new work, just the wiring above.

**Forward hooks planted (Kalahar Reach / desert / R8 Dunewind Trail):** Nurse Coralie's rumor fallback, Rui's post-wager line, Odessa's stock talk, Kaito's third manifest, Sable's bound-east-for-Dunewind, and Mattias's freight talk — six independent plants, so the pull to gym 6 lands regardless of which beats the player touches.

**Back-echoes (world talks backward):** Westwind Fields is on the Hua Zhan greenhouse dispatch board (archivist recites it two towns earlier); Rui references the Takehara river-fisher (Genji) who taught the rod; the revised-last-quarter fee/tariff schedule echoes Deepcore's price-adjust and Takehara's paid heal; Mattias's post-liberation line reacts to any field the player has freed.

---

## 5. New Tags / Scores Introduced

| tag / score | set by | gated by (read where) |
|-------------|--------|-----------------------|
| `bosun_net_done` | `sidequest/rui/net_success` (turn-in) | Rui dialog wager/flavor entries; `q.side_rui` register; Coralie rumor #1 |
| `defeated_sq_rui_wager` | wager battle win (`defeat_tag`) | Rui `net_flavor` entry; `q.side_rui` |
| `declined_sq_rui_wager` | `route/decline_sq_rui_wager` (pay-probe, compiler-generated) | decline stand-down |
| `odessa_crate_started` | `sidequest/odessa/start_crate` | Odessa dialog; `q.side_odessa` |
| `odessa_crate_recovered` | `sidequest/odessa/recover_crate` | Odessa `open_shop` entry; `q.side_odessa`; Coralie rumor #2 |
| `gaviota_manifest_check_active` | `sidequest/manifest/start` | Kaito `counting` entry; `q.side_freight` |
| `gaviota_manifest_1` / `_2` / `_3` | `sidequest/manifest/tick` (per wharf point) | `#manifests` count; `manifest/set_manifests` |
| `gaviota_manifests_filed` | `sidequest/manifest/turn_in` | Kaito `filed` entry; `q.side_freight`; Coralie rumor #3 |
| `defeated_gaviota_wheat_sea` | Sable ambush win (`defeat_tag`) | Sable `already_beaten_line`; `q.side_westwind`; Coralie rumor #4 not-done gate |
| `#manifests` (scoreboard `quest_hud`, `#`-holder) | `sidequest/manifest/tick` recompute | `q.side_freight` dynamic sidebar; turn-in button gate; turn-in guard |
| `#rumor` / `#rumor_hit` (scoreboard `quest_hud`, `#`-holders) | `sidequest/rumors/coralie` | rumor roll (transient) |
| `farm_3` on `field_freed` (score) | `liberation/free_field_apply {field:farm_3}` | Westwind zone banner (install.json); Mattias post-liberation line (via `fields_liberated`) |

**Reused (no new state):** `wheat_trader_suspicious` / `wheat_trader_hostile` (existing poller off `fields_liberated`), `wheat_war_active`, `fields_liberated`, `cd_instability`, `memory_fragment`, `ci_sq_scratch`, `recognition: mid` → compiler band tag.

---

## 6. Build Checklist (ordered)

1. **Create the area folder:** `dialog-src/characters/gaviota/` (new — verified it does not exist; the gym cast lives under `dialog-src/characters/gym/gaviota_*` and is untouched).
2. **Drop 6 character files** into `dialog-src/characters/gaviota/`: `gaviota_nurse`, `gaviota_dockmaster`, `gaviota_fence`, `gaviota_fisher`, `gaviota_smuggler`, `gaviota_union_hand` (JSON blocks §3.1–3.6). Confirm/replace all PLACEHOLDER placement coords in-world (§2).
3. **Drop 6 dialog files** into `dialog-src/dialog/`: `gaviota_nurse`, `gaviota_dockmaster`, `gaviota_fence`, `sq_rui_deep_net`, `gaviota_wheat_sea` (or reuse `dialog:wheat_trader`), `gaviota_union_hand`.
4. **RCT trainer:** add `sq_rui_wager` trainer JSON under `data/rctmod/trainers/` (two water types, level 52). Reuse `wheat_trader_ambush` for Sable (or add a Gaviota-tuned ambush team **≤ 50** for cap-legality).
5. **Datapack — turn-ins / logic (new functions):**
   - `sidequest/rui/turn_in_net` + `sidequest/rui/net_success` (clone Genji rod pair; use CD + net loot, not a duplicate `cobblemon:poke_rod`, per Open Q1).
   - `sidequest/odessa/start_crate` + `sidequest/odessa/recover_crate`.
   - `sidequest/manifest/start` + `tick` + `set_manifests` + `turn_in`.
   - `sidequest/rumors/coralie` (clone `rumors/lila`).
   - `route/decline_sq_rui_wager` (compiler auto-generates from `battle.decline_fee` — verify it emits; do not hand-write).
6. **Datapack — existing-file edit (one line):** append `farm_3:"WESTWIND FIELDS"` to the `names` merge in `function/liberation/load.mcfunction` (§3.5).
7. **Wire the tickers:** register `sidequest/manifest/tick` (and, if used, `sidequest/odessa/tick`) into the per-player tick chain, matching how `price_check/tick` is wired.
8. **Register stages:** add `q.side_freight` (slot 83), `q.side_odessa` (slot 84), `q.side_rui` (slot 85), optional `q.side_westwind` (slot 82) to `dialog-src/registers/quest_targets.json` (§3.2/3.3/3.4/3.5). **Slot audit (verified against the live register):** slots **57–81 are ALL occupied** and **100 is the main quest** — the doc's earlier claim that 70–73 were free was WRONG (70=`q.side_bones`, 71=`q.side_shift`, 72=`q.side_classic`, 73=`q.side_ascent`), and `q.side_manifest` is already taken (slot 58, "Right of Way"). The clean free band above the side cluster is **82–99**; this unit takes 82–85.
9. **Compile:** run `scripts/content_compile` → presets + `quest_waypoints.json` in sync; then `generate_npc_function` + `update_preset_index` per the schema lowering. Validate no `"`/`'`/`%` in macro lines (compiler enforces).
10. **Runtime:** `/cobblemon-initiative install run` to latch-spawn the 6 new NPCs (within 40b), walk up, press `]` to track; verify the four side sidebar lines, the manifest counter, the crate recovery, the Westwind LIBERATED card + banner flip + `−6 cd_instability`, and each forward-hook line reads on stream.

---

## 7. Open Questions for Showrunner

1. **Rui's turn-in reward vs. the Poké Rod.** Genji already hands `cobblemon:poke_rod`. Does Rui hand a *second* rod (fine — nets ≠ rods, and by gym 5 the player has one), a **different fishing reward** (a bait/lure item, a rare-water encounter token), or just CD + a training loot table? **Recommend CD + net-themed loot** to avoid a duplicate rod. (Also: `cobblemon:poke_rod` id is UNVERIFIED in 1.7.3 — the Genji `rod_success` file already flags this; jar-check if reused.)
2. **Sable single-NPC vs. split field guard.** Should the Westwind liberation be a **single NPC** (Sable: trade → recognise → ambush → `free_field`) or the Firstfurrow pattern (a **separate perimeter guard** cleared first, then the field flips, Sable as trade+recognition only)? **Recommend single-NPC** for a tighter port beat unless the in-world Westwind build already has a fenced perimeter with a guard body.
3. **Rumor #4 / westwind waypoint gate primitive — RESOLVED in this doc.** `field_freed` is a **scoreboard** (`farm_3`), not a tag, and both the rumor idiom and the register stage gate on tags. This doc gates rumor #4 and the optional `q.side_westwind` stage on **`!defeated_gaviota_wheat_sea`** (the ambush-win tag, which fires on the actual liberation) — clean and correct. Confirm the choice.
4. **Manifest turn-in mechanism — RESOLVED in this doc.** Uses the **dialog turn-in button** (present on both the `counting` and `filed` entries, gated on `score #manifests quest_hud >= 3`), mirroring the Genji turn_in_button placement, rather than an auto-fire in the tick. Confirm.
5. **Westwind Fields exact anchor + wharf-point coords.** All §2/§3 coords are PLACEHOLDER pinned to real fixtures (gym slips, zone centroids, the Westwind polygon ~[660,3280]). Need in-world confirmation of: the Center counter, harbourmaster shack, boardwalk fence slip, customs-float prop, the three manifest wharf points, and Sable's field-edge sea dock. (`dev/updated-zones.json` gives x/z; y is nominal.)
6. **Gaviota gym paid nurse / rumor hub — CONFIRMED additive.** The gym cast under `dialog-src/characters/gym/gaviota_*` is leader + guide + 4 trainers + 2 apprentices only — no Center nurse — so `gaviota_nurse` is new, not a double. (Verified: no `dialog-src/characters/gaviota/` folder exists yet.)
</content>
</invoke>
