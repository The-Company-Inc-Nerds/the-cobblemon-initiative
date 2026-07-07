# NPC Preset Authoring

This directory holds the **authoring assets** for Easy NPC presets — partial
SNBT snippets that get spliced into full presets. Nothing in here ships with
the mod; the shipped presets live in
`src/main/resources/data/easy_npc/preset/` (Easy NPC 6.25 only accepts DATA imports from `easy_npc:preset/…​.npc.snbt` — PresetSecurity).

Snippets are **self-contained**: behavior is implemented inline via dialog
button actions, NPC objectives, distance/interaction action events, and vanilla
merchant trades, using existing commands (`tbcs`, `cobbledollars`,
`healpokemon`, `openstarterscreen`, `openpc`, and vanilla
`execute`/`tag`/`scoreboard`/`loot`/`give`/`tellraw`). No datapack functions are
required except the existing `rewards/badge_reward_combo` used by gym/boss
battles. The optional reward tables under `loot_table/npc_gift/` are plain loot
tables consumed by `loot give` — any other loot table id works in their place.

## Workflow

The map builders placed ~400 Easy NPCs (see `dev/UPM 2_ Full NPC List…csv`).
To give one of them behavior without losing their skin/pose/position:

1. **Export** the NPC in-game: `/easy_npc preset export <uuid>` — this writes a
   full `.npc.snbt` containing everything the builders set up.
2. **Compose**: run `scripts/npc_preset_builder`. Choose either:
   - an **archetype recipe** (fast — pick "Shopkeeper", "Gym leader",
     "Melee enforcer", … and the base + snippets are bundled for you), or
   - **manual** — pick the export (or a `_*` template) as the target and
     splice individual snippets over it.
   Then answer the placeholder prompts; a summary is shown before anything is
   written.
3. **Apply**: the builder writes the preset into the shipped `humanoid/`
   directory, refreshes `preset.index`, and can map the NPC UUID in
   `npc_presets.json`. Then run `scripts/generate_npc_function` and, in-game,
   `/function cobblemon_initiative:update_npc_presets`.

Batch mode (no prompts) is available too — see `scripts/npc_preset_builder --help`.

## Recipes

A **recipe** is a named bundle of `(base template + snippets)` for a kind of
NPC, so you answer placeholder prompts instead of hand-picking parts:

```bash
scripts/npc_preset_builder --list-recipes              # see them all
scripts/npc_preset_builder --recipe shopkeeper --name gaviota_clerk
scripts/npc_preset_builder --recipe gym_leader --name takehara_leader_npc \
    --set TRAINER_ID=takehara_leader --set BADGE_TIER=1 --defaults
```

Recipes are grouped: **Townsfolk** (civilian, rumor_monger, lore_keeper,
statue, first_meeting), **Trainers** (trainer_basic / one_time / paid_decline /
wager / rematch, type_specialist, gym_leader, gym_guide, royal_league,
trainer_spotter), **Villains** (villain_grunt / management / boss /
villain_forced, town_crier, threatener), **Duelists**
(melee_enforcer, armored_brute), **Merchants** (shopkeeper, apothecary,
berry_vendor, black_market, grain_buyer), **Services** (healer, professor,
pc_keeper, shop_cobbledollars), and **Quests & gifts** (daily_gift, gift_once /
gift_item_once, quest_fetch / quest_defeat / quest_bounty / quest_delivery,
resistance_donor).

`--target` / `--snippets` always override a recipe's choices. Each recipe
bundles **at most one** dialog-bearing snippet — see Conventions.

## Placeholder syntax

Snippets and templates may contain placeholders anywhere (string values or
bare SNBT positions):

| Form | Meaning |
|------|---------|
| `%%KEY%%` | free-text prompt |
| `%%KEY=default%%` | free text with a default |
| `%%KEY:opt1\|opt2\|opt3%%` | numbered choice menu |
| `%%KEY:opt1\|opt2=opt1%%` | choice menu with a default |

Rules: keys are `[A-Z][A-Z0-9_]*`; the same key is prompted once and
substituted everywhere; **every `%%` must be closed** (`%%KEY=text%%`, never
`%%KEY=text`); nothing inside a placeholder may contain `%`; a `=` inside a
*choice-list* default breaks parsing (plain `=default` text may contain further
`=` and `:`); **never nest placeholders inside another placeholder's default**.
A choice-menu placeholder (it contains `:`/`|`) must live **inside a quoted
string** — bare positions like an NBT number may only use `%%KEY%%` or
`%%KEY=default%%`. Values substituted into `Cmd:` strings that use tbcs `onwin`
lists must not contain apostrophes (they would close the `'...'` quoting), and
no value may contain a double quote.

Keys whose values feed scoreboard/tag names (`GIFT_KEY`, `QUEST_KEY`,
`TRAINER_ID`, `TARGET_TRAINER_ID`, `PREREQ_TAG`, `CLEAR_TAG`, `GATE_TAG`) must
be snake_case identifiers. `TRAINER_ID` values must match an RCT trainer JSON id
(see `src/main/resources/data/rctmod/trainers/`).

## Snippet catalog

Each snippet is an SNBT fragment whose top-level keys (`DialogData`,
`ActionData`, `ObjectiveData`, `EntityAttribute`, `attributes`, `Offers`,
`TradingData`, `HandItems`, `ArmorItems`, …) replace `data.<Key>` in the target
preset. A `PresetMetadata` key is deep-merged instead. A snippet may carry
several keys (e.g. a trade snippet sets both `Offers` and `TradingData`).

### battle/ — RCT trainer battles (tbcs)

| Snippet | Behavior |
|---------|----------|
| `battle_basic` | Repeatable challenge; prize + `defeated_<id>` tag on win |
| `battle_one_time` | Same, but refuses rematches once `defeated_<id>` is set |
| `battle_paid_decline` | Decline by paying a fee (CobbleDollars) |
| `battle_wager` | Win the stake / lose the stake |
| `battle_gym_leader` | One-time formal match; pays badge-tier loot via `rewards/badge_reward_combo` |
| `battle_villain_grunt` | Company grunt: fight (no buy-out — decline removed); loss also costs you; **despawns on defeat** |
| `battle_villain_management` | One-time mid-boss; sets `CLEAR_TAG`, half-recognizes you; no decline; **despawns on defeat** |
| `battle_gauntlet_boss` | One-time boss gated behind `PREREQ_TAG`; sets `CLEAR_TAG` + badge loot; no decline; **despawns on defeat** |
| `battle_elite` | One-time; gated behind all ten `defeated_*_leader` tags (Royal League) |
| `battle_rematch` | Unlimited sparring, **only** after `defeated_<id>` is set |
| `battle_type_tip` | Offers a type-matchup tip page, then a repeatable battle |
| `battle_spotter` | **Sight-gated trainer.** `LOOK_AT_PLAYER` + `ON_DISTANCE_TOUCH` battle (gated by `defeated_<id>`). Register the NPC `/npcsight … mode pursue` so it walks up when it sees you and battles on contact (≤1.25 blocks). No decline. |
| `battle_villain_forced` | **Unavoidable Company blocker.** Shouts a threat on approach (`ON_DISTANCE_CLOSE`) and forces the battle on contact (`ON_DISTANCE_TOUCH`); **despawns on defeat**. Pair with `/npcsight … mode pursue`. No dialog, no decline. |

All battle snippets tag the player `defeated_<TRAINER_ID>` on victory, which
`quest_defeat`/`quest_bounty`/`battle_rematch` can check.

> **Hand-builder only (round 13b):** `battle_spotter` and `battle_villain_forced` are
> NOT part of the `dialog-src` → `content_compile` pipeline — compiled characters get
> touch battles from the `engage: "touch"` character key instead. These two snippets
> were also fixed in round 13b: action-level gates must use the **doubled**
> `ConditionDataSet:{ConditionDataSet:[…]}` key (a bare `Conditions` list on an ACTION
> is silently ignored — Easy NPC reads that key only on dialog entries/buttons, see
> `docs/ENGINE_FINDINGS.md` §3).

The `battle_spotter` and `battle_villain_forced` snippets are **sight-driven**:
the contact battle (`ON_DISTANCE_TOUCH`, ≤1.25 blocks) is native Easy NPC, but the
*approach* comes from registering the NPC in NPC Sight in `pursue` mode — the mod
toggles a `FOLLOW_PLAYER` objective on while the NPC can see the player (FOV + LOS)
and removes it when it loses sight. See **NPC Sight behaviours** below. The three
**villain dialog** snippets (`battle_villain_grunt/management/gauntlet_boss`) no
longer offer a pay/decline button — the only option is to fight.

The three **villain** battle snippets (`battle_villain_grunt`,
`battle_villain_management`, `battle_gauntlet_boss`) additionally run
`easy_npc delete @2` as the last `onwin` step, so the Company NPC silently
despawns once it has delivered its defeat line. (Inside a `tbcs … onwin` list
`@2` expands to the trainer NPC's UUID, which `easy_npc delete` accepts;
`discard()` bypasses the NPC's `IsInvulnerable` flag and plays no death
sound/animation, and the removal is permanent — the villain does not return on
world reload. Use `easy_npc despawn @2` instead if a villain must be
restorable later.)

### duel/ — melee PVP-style brawls (the map is single-player, so this is the
player vs. a hostile NPC, not literal PvP)

| Snippet | Behavior |
|---------|----------|
| `duel_melee` | Makes the NPC attackable + hostile: `EntityAttribute`, `attributes` (health/speed/attack), `ObjectiveData` (attack + chase + return-home + look) |
| `duel_taunt` | `ActionData` — taunts on approach (`ON_DISTANCE_NEAR`) and on interaction |

### equipment/ — visible gear

| Snippet | Behavior |
|---------|----------|
| `equipment_weapon` | Main-hand weapon (`HandItems`) — pair with `duel_melee` |
| `equipment_armor` | Full armor set (`ArmorItems`) |

### trade/ — vanilla-merchant shops (`Offers` + `TradingData`)

| Snippet | Behavior |
|---------|----------|
| `trade_pokemart` | Buy balls/potions for `CURRENCY` (default emerald) |
| `trade_apothecary` | Buy healing items |
| `trade_berry_stand` | Buy berries/apricorns |
| `trade_black_market` | Buy rare contraband at a premium (default diamond) |
| `trade_buyer` | The Company **buys** the player's wheat/produce for currency |

Pair a trade snippet with `service/shop_trade_dialog` (or set the NPC's
`ON_INTERACTION` to `OPEN_TRADING_SCREEN`) so the trade screen opens.

### dialog/ — ambient flavor & lore

| Snippet | Behavior |
|---------|----------|
| `dialog_flavor` | 1-3 rotating idle lines |
| `dialog_lore_pages` | 3-page lore conversation (founder mystery) |
| `dialog_amnesia_hint` | NPC half-recognises the protagonist |
| `dialog_company_propaganda` | Wheat-futures corporate messaging |
| `dialog_rumor_mill` | Rotating plot/economy rumors |
| `dialog_threat` | A Company higher-up recognises and threatens the protagonist |
| `dialog_progress_gate` | **Native dialog conditions**: shows `AFTER_LINE` once the player has `GATE_TAG`, else `BEFORE_LINE` (see Conventions — verify in-game) |
| `dialog_first_meeting` | Two-state dialog: an *intro* (default) until the player has `MEET_TAG`, then *post-meeting* lines once they do (`PLAYER_TAG`/`EQUALS` condition). Pair with `/npcsight … mode approach_once` + `meettag <MEET_TAG>` (e.g. Mom). |

### ambient/ — making the world feel alive (`ObjectiveData` + distance actions)

| Snippet | Behavior |
|---------|----------|
| `ambient_wander` | Strolls around `Navigation.Home`, looks at the player |
| `ambient_stationary_look` | Tracks the player but stays put (shopkeepers/guards) |
| `ambient_guard_patrol` | Water-avoiding patrol around home |
| `ambient_crier` | Shouts a line when the player approaches; keeps interaction dialog |

`ObjectiveData` movement objectives anchor on `Navigation.Home` — set it
(`/easy_npc navigation set home`) or the NPC strolls around `0,0,0`.

### NPC Sight behaviours — sight-gated pursuit & one-time approaches

NPC Sight raycasts a forward 120° FOV + line-of-sight from each registered NPC to
the nearest player a few times a second. Beyond the default dialog trigger, an NPC
can be given a **behaviour mode** that drives movement while it can see the player:

| Mode | What it does | Use with |
|------|--------------|----------|
| `dialog` (default) | Opens the configured dialog once per "seen session" (re-arms when it loses sight). | any dialog snippet |
| `pursue` | While it can see the player, the mod toggles an Easy NPC `FOLLOW_PLAYER` objective so the NPC walks toward you; it is removed (+ `navigation reset`) when sight is lost. The battle itself is the preset's `ON_DISTANCE_TOUCH`. | `battle_spotter`, `battle_villain_forced` |
| `approach_once` | The first time it *ever* sees you it walks up, opens its dialog once, optionally tags you (`meettag`), then never auto-approaches again (persisted). | `dialog_first_meeting` |

Registration (admin, OP 2 — UUID tab-completes to the entity you're looking at):

```
# trainer / forced villain (pursue + ON_DISTANCE_TOUCH battle):
/npcsight add <uuid> <range>               # register (no dialog needed)
/npcsight mode <uuid> pursue
/npcsight stoptag <uuid> defeated_<id>     # stop pursuing once that tag is set (post-defeat stand-down)

# one-time approacher (e.g. Mom):
/npcsight add <uuid> <range> default       # pass the first-meeting dialog's LABEL as <dialog>
/npcsight mode <uuid> approach_once        # walk up + open that dialog once, then never again
/npcsight meettag <uuid> met_mom           # player tag added when the approach fires (drives post-meeting dialog)
/npcsight reset <uuid>                     # clear the approach_once latch (to re-test)
```

For `approach_once` the mod opens the NPC's configured `dialog` (the `add`/`dialog`
arg) when it reaches the player, so pass the first-meeting dialog's label
(`dialog_first_meeting`'s intro entry is labelled `default`).

The runtime follow uses Easy NPC's default `Prio 7`. Don't give a pursuer a
movement idle objective with a **lower** `Prio` number (higher priority) or it
would outrank the chase — the `trainer_spotter` / `villain_forced` /
`first_meeting` recipes use look-only objectives, so the follow always wins
while the NPC has sight.

### loot/ — gifts (inline `loot give`/`give`, gated by tags/scoreboards)

| Snippet | Behavior |
|---------|----------|
| `loot_daily` | Loot table once per Minecraft day (`gift_<key>` vs `time query day`) |
| `loot_one_time` | Loot table once ever (`gift_<key>_claimed` tag) |
| `loot_one_time_item` | Specific item stack once ever |

### quest/ — simple quests (inline `clear`-count / tag checks)

| Snippet | Behavior |
|---------|----------|
| `quest_fetch` | Bring N × item, get a reward table; items taken on turn-in |
| `quest_defeat` | Beat trainer `<target>` (tagged by battle snippets), get a reward table |
| `quest_bounty` | Beat trainer `<target>` for a CobbleDollar bounty |
| `quest_delivery` | Hands the player a package once (pair recipient with `quest_fetch`) |
| `quest_donate` | One-time CobbleDollar donation to the resistance for a reward |

### service/

| Snippet | Behavior |
|---------|----------|
| `healer` | Heals the party (`healpokemon`) |
| `gym_guide` | Explains the local gym, type, and level cap |
| `shop_cobbledollars` | Opens the CobbleDollars shop (command) |
| `shop_trade_dialog` | Greeting dialog → `OPEN_TRADING_SCREEN` (pair with a `trade/` snippet) |
| `starter_selection` | Opens the Cobblemon starter screen (`openstarterscreen`) |
| `pc_access` | Opens the player's PC (`openpc`, run as player) |

### attributes/

| Snippet | Behavior |
|---------|----------|
| `attributes_protected` | Invulnerable / non-pushable `EntityAttribute` block for raw builder NPCs |

## Conventions

- **One dialog per NPC.** Two snippets that both define `DialogData` deep-merge
  into a tangle. Combine at most one of {battle, quest, loot, `service/*` dialog,
  `dialog/*`} per preset. `ObjectiveData`, `Offers`/`TradingData`, equipment, and
  `ambient_crier`'s `ActionData` are orthogonal and stack freely.
- Battle outcome sides in `tbcs … onwin {…}`: side `1` = the player,
  side `2` = the attached trainer NPC; `@1`/`@2` reference them inside the
  command lists, and a leading `@2 say …` makes the NPC speak.
- Commands in dialog/action lists run **as the NPC** unless `ExecAsUser: 1b` —
  use `@p` to target the player from NPC context (single-player map, so
  `@p` is always the player).
- Conditional gift/quest chains put their state-mutating command (`tag @s
  add …`, `scoreboard players operation …`) **last** in the action list, so the
  earlier conditional lines in the same click still see the pre-claim state.
- Player-state tracking: tags `defeated_<trainer>`, `gift_<key>_claimed`,
  `quest_<key>_done`, story flags like `company_management_cleared`; scoreboard
  objectives `npc_gift` (+ per-gift `gift_<key>`) and `npc_quest`
  (fake players `#today`, `#have`).
- **Trading NBT** (`trade/`): the wrapper is `data.TradingData =
  {TradingDataSet:{Type:"BASIC", MaxUses, RewardedXP, ResetsEveryMin, LastReset}}`
  plus `data.Offers = {Inventory:{}, Recipes:{Recipes:[ … ]}}`. Each recipe is a
  vanilla 1.21 merchant offer: `{buy:{id,count}, sell:{id,count}, maxUses,
  rewardExp:0b, priceMultiplier:0.0f, …}` (1.20.5+ uses lowercase int `count`).
  Easy NPC's preset import may strip trading under a strict security config;
  on this single-player map (OP) it imports fine. Verify the shop opens in-game.
- **Native dialog conditions** (`dialog_progress_gate`): `DialogData` entries may
  carry `Conditions:[{Type:"PLAYER_TAG", Operation:"EQUALS", Name:"<tag>"}]` so a
  higher-priority entry is shown only when the player has the tag. This is the
  one mechanism not yet exercised elsewhere in the repo — verify the gating
  behaves in-game before relying on it for story beats.
