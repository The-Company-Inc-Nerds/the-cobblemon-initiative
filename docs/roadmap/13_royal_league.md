# 13 — Royal League (Elite Four + Champion, cap → 85)

> Area key: `royal_league` · Act 3 (pre-reveal apex) · Zone: **Royal League** TOWN, Ironwave Tower
> One-line pitch: **A five-fight underleveled gauntlet — four type-specialist walls and a Champion who already knows your name and will not say it — that ends the gym road and opens the cap to 85, the last door before the mirror.**

Status anchors (all file-verified):
- Character files exist: `dialog-src/characters/royal/royal_elite_1..4.json`, `royal_champion.json`, `helena_gatewarden.json`, `roland_badgekeeper.json`, `aurelius_medcrest.json` (Dr. Asha), `lucian_scrollkeeper.json` (moved to Sango).
- Dialog trees exist: `dialog-src/dialog/royal_aria|marcus|luna|drake|cynthia|badgekeeper|flavor|healer.json`.
- Presets already compiled: `src/main/resources/data/easy_npc/preset/humanoid/royal_elite_1..4.npc.snbt`, `royal_champion.npc.snbt`.
- **BLOCKER:** the authoritative team files `data/rctmod/trainers/royal_elite_1..4.json` and `royal_champion.json` are all **empty `{}`** (ENGINE_FINDINGS §"Post-league era: 20 empty {} trainer teams"). The TBCS battle button resolves the team from these files, so **every League battle is currently non-functional.** The old `trainers/royal_league/royal_league.json` holds a legacy 4-mon embedded roster; it is NOT what TBCS reads.
- Cap wiring is **live**: `data/cobblemon_initiative/levelcaps.json` order 11 → `royal_league_champion` → `levelCap 85`. `band_tags.mcfunction` already maintains `defeated_royal_elite_1..4` / `no_defeated_royal_elite_1..4` inverse tags for gating.

---

## 1. Concept & fantasy

The gym road is over. Ten badges are in the case, the wheat war is quiet (Acting CEO DJ is down, `cd_instability` pinned at 25), and the world funnels the amnesiac up **Ironwave Tower** to the seat that decides who is real. This is the classic Pokémon Elite Four made **brutal**: five back-to-back fights, no gym-trainer chaff to warm up on, the player hard-capped at **80** while every wall sits above them. It is the last "just battle well" test before Act 3 stops being subtext.

The fun is the wall itself and the slow-burn recognition. Each of the four is a single-type purist who reads the challenger and lets a line slip about a ghost from the founding days. Then **Champion Cynthia** — who has kept the seat warm "waiting to see who would come and remember themselves on the way up" — looks straight through the amnesia, tells him his signature is on documents older than the League, and **refuses to say the name.** She makes him earn it from the road ahead (the Board, the mirror).

Marquee stream moments:
- **The Badgekeeper reveal-gate.** Roland counts all ten badges on camera ("Cicada through Vulcan") and opens the stairs — a payoff for the entire gym grind the audience watched.
- **The underleveled climb.** Four type walls at ace 82 → 85 vs a capped-80 team, permadeath live. Ace-heavy: each fight escalates and the ace can 6-0 a sloppy squad.
- **Cynthia's non-name.** The single highest-tension pre-reveal beat: she knows exactly who he is, says his name is redacted "over a black bar," and sends him on. Chat loses its mind; the name still is not spoken.
- **The 85 unlock.** "Become the Champion" — the level cap finally breaks past 80. The very next content the audience knows about is the Board and the Founder.

---

## 2. Narrative role

| Field | Value |
|---|---|
| Act | **3** (opening — post-Royal-League Board/Founder is the *end* of Act 3; the League is its gate) |
| `cd_instability` | **25** (stable — DJ was toppled after gym 7 + 4 fields; the League does not move the index) |
| Memory fragment | **None new.** frag_10 ("face your own signature") already fired at Scorchspire. The reveal is held for `reveal/board_fell` and `reveal/founder_defeated` (LORE_BIBLE §5). The League *foreshadows*, never spends the reveal. |
| Recognition tier | **late** (all six royal characters carry `recognition_tier: late`) — this is the **apex of pre-reveal recognition**: the E4 alarm-then-stand-down; Cynthia fully recognises the CEO and withholds the name |
| Canon ties | Cynthia is the recognition mirror before the literal mirror. Roland's line "not for whoever the grey suits keep asking after" confirms the Company is still hunting the Founder even here. Champion defeat grants `royal_league_champion` → cap 85 → the **only** gate before the Board (`board_cleared` → 100 → the Founder). |

Canon rule honoured: **the name is never spoken in the League.** Cynthia's `recognise` entry says "I will not say the name out loud… Beat me, and the rest of the road will say it for you." That hand-off to the Board/Founder area is deliberate.

---

## 3. Layout & placements

Zone: **Royal League** (`install.json` line 757) — TYPE `TOWN`, `centerY: 64`, `mobsSpawn: false`, `hostileOnly: true`, polygon roughly **x 3425–3662, z 2752–2994**. Approached via the **Road to Royal League** ROUTE (line 4778, `mobsSpawn: true`) from the south (~z 2166 → 2575). The legacy config lands the fight line at **z 2773**, x stepping 3528 → 3544.

> **Coordinate caveat (builder confirm):** every entry in `trainers/royal_league/royal_league.json` uses **`y: 166`**, but the zone `centerY` is **64**. `y:166` is almost certainly a stale placeholder — the interior floor Y must be confirmed against the actual Ironwave Tower build. All fight-line coords below are labelled PROPOSED pending that check.

| NPC / prop | Body source | Coord | Confirmed? |
|---|---|---|---|
| **Roland Badgekeeper** (10-badge gate) | builder body `uuid e5146ec2-ddc4-4b94-9469-1a8d95ccd497` | entrance / foot of stairs, PROPOSED `~[3528, 64, 2758]` (south zone edge) | body confirmed; coord PROPOSED |
| **Helena Gatewarden** (flavor, `dialog:royal_flavor`) | builder body `uuid 408f3e6d-921d-4891-a800-c1e70b0c7d30` | entrance atrium, PROPOSED near Roland | body confirmed; coord PROPOSED |
| **Dr. Asha** (heal station, `aurelius_medcrest`) | builder body `uuid a9eeba58-7f9e-4fdc-803a-a4d12c9eb890` | **PLACEMENT UNRESOLVED** — her file comment says the Preferred Provider quest treats the clinic as *Sango-side*; the League needs its own heal tap between fights (see §5 / §9 decision) | UNRESOLVED |
| **E4 Aria** (Ghost) | preset exists, **no uuid, no placement** → needs `placement:{}` | PROPOSED `[3528, 64, 2773]` | needs placement added |
| **E4 Marcus** (Rock) | preset exists, no placement | PROPOSED `[3532, 64, 2773]` | needs placement added |
| **E4 Luna** (Dark) | preset exists, no placement | PROPOSED `[3536, 64, 2773]` | needs placement added |
| **E4 Drake** (Dragon) | preset exists, no placement | PROPOSED `[3540, 64, 2773]` | needs placement added |
| **Champion Cynthia** | preset exists, no placement | PROPOSED `[3544, 64, 2773]` | needs placement added |
| **Hall of Redactions plaque** (SQ-2 prop) | PROPOSED — reuse the notice-board prop pattern (`hua_zhan/rezoning_notice_board.json`) | PROPOSED along the stair wall between Roland and Aria | PROPOSED (needs builder) |

**Placement gap (must fix):** the five fight characters have **neither `uuid` nor `placement`** in `dialog-src/characters/royal/`, so no proximity-spawn function is generated (grep of `function/` finds only `band_tags` referencing them) and `npc/preset_map.json` has no royal entry. Each needs a `placement:{x,y,z}` block (once-per-world spawn, bypasses the builder world — the pattern the brief describes) OR builder bodies with UUIDs. Recommend **placement blocks** using the coords above, so the League populates from the compiled presets without hand-placing five bodies.

---

## 4. Core structure — the Gauntlet (not a gym)

Not a gym: no interior PvP chaff ladder, no "which battle is a double." The core loop is a **five-node linear gauntlet**, each node a dialog battle gated on the previous node's defeat tag. Player is capped at **80** for the entire climb; **every ace is above cap** (the brutal-Nuzlocke "ace = entry-cap + 2" rule, entry cap 80, taken as the *floor* and escalated to the Champion).

### 4a. Gate wiring (the ladder)

The config already encodes the prerequisite chain; the **dialog challenge entries must be gated to enforce it in-conversation** (they currently are not). Use the inverse band-tags `band_tags.mcfunction` already maintains.

| Node | Config prerequisite (`royal_league.json`) | Dialog challenge gate to ADD | Locked-fallback entry gate | Win tag |
|---|---|---|---|---|
| Roland (gate) | — | `badges gte 10` → `cleared` entry (exists) | `badges gte 7` "almost" / default (exist) | (opens stairs) |
| Aria | `scorchspire_leader` | `gate:{defeated: scorchspire_leader}` (i.e. 10 badges) | if not 10 badges: "Roland has not cleared you" | `defeated_royal_elite_1` |
| Marcus | `royal_elite_1` | `gate:{defeated: royal_elite_1}` | `gate:{tag: no_defeated_royal_elite_1}` → "Aria first" | `defeated_royal_elite_2` |
| Luna | `royal_elite_2` | `gate:{defeated: royal_elite_2}` | `gate:{tag: no_defeated_royal_elite_2}` → "Marcus first" | `defeated_royal_elite_3` |
| Drake | `royal_elite_3` | `gate:{defeated: royal_elite_3}` | `gate:{tag: no_defeated_royal_elite_3}` → "Luna first" | `defeated_royal_elite_4` |
| Cynthia | `royal_elite_4` | `recognise` entry `gate:{recognition: late}` (exists, prio 20) + default challenge `gate:{defeated: royal_elite_4}` | `gate:{tag: no_defeated_royal_elite_4}` → "the four are not done" | **`royal_league_champion`** (defeat_tag override, NOT `defeated_royal_champion` — ENGINE_FINDINGS) |

Each E4 already has an `after` entry gated on its own `defeated_*` tag (prio 20) and a `default` challenge (prio 10). The only authoring gap is the **locked-fallback** entry + moving the challenge behind the previous-defeat gate, so a challenger cannot skip a wall by walking up to Drake first.

### 4b. Battle format

All five are **`GEN_9_SINGLES`** (canon E4 feel; matches every character `battle.format`). One optional spice decision in §9: making **Drake a `GEN_9_DOUBLES` "dragon storm"** as the marquee double (mirrors the gym-double pattern). Default recommendation: **keep all singles** — the wall is the team quality and the underleveling, not a format gimmick.

### 4c. Team sketches (ace = escalating; player capped 80)

Design intent: **6 mons each** (up from the legacy 4), competitive sets, held items, hazards/weather, one snowballing ace per member. Aces climb **82 → 83 → 84 → 85 → 85**. AI `maxSelectMargin` 0.1 (E4) / 0.05 (Champion). Bag: E4 `full_restore x2`, Champion `full_restore x4`.

**E4 #1 — Aria · Ghost · ace 82** ("I battle in the dark so I can watch the nerve")

| # | Species | Lv | Ability | Set (moves / item) |
|---|---|---|---|---|
| 1 | Mismagius | 80 | Levitate | nastyplot / shadowball / dazzlinggleam / mysticalfire — *focus sash* |
| 2 | Dusknoir | 80 | Pressure | poltergeist / earthquake / icepunch / painsplit — *leftovers* |
| 3 | Aegislash | 81 | Stance Change | shadowball / shadowsneak / kingsshield / sacredsword — *weakness policy* |
| 4 | Chandelure | 81 | Infiltrator | shadowball / fireblast / energyball / calmmind — *choice specs* |
| 5 | Gengar | 81 | Cursed Body | shadowball / sludgewave / focusblast / nastyplot — *life orb* |
| **A** | **Dragapult** | **82** | Infiltrator | dragondarts / shadowball / uturn / dracometeor — *choice scarf* |

**E4 #2 — Marcus · Rock (sand) · ace 83** ("I am rock, and rock does not move")

| # | Species | Lv | Ability | Set |
|---|---|---|---|---|
| 1 | Tyranitar | 81 | Sand Stream | stealthrock / stoneedge / crunch / earthquake — *smooth rock* (sets sand) |
| 2 | Aggron | 81 | Rock Head | headsmash / heavyslam / earthquake / thunderwave — *weakness policy* |
| 3 | Aerodactyl | 82 | Unnerve | stoneedge / earthquake / firefang / aquatail — *life orb* |
| 4 | Rhyperior | 82 | Solid Rock | earthquake / stoneedge / megahorn / rockpolish — *leftovers* |
| 5 | Gigalith | 82 | Sand Force | stoneedge / earthquake / heavyslam / superpower — *assault vest* |
| **A** | **Garganacl** | **83** | Purifying Salt | saltcure / earthquake / recover / irondefense — *leftovers* (stall ace — punishes a whittled Nuzlocke squad) |

**E4 #3 — Luna · Dark · ace 84** ("dark-types refuse to be predicted")

| # | Species | Lv | Ability | Set |
|---|---|---|---|---|
| 1 | Umbreon | 82 | Synchronize | foulplay / wish / protect / toxic — *leftovers* |
| 2 | Grimmsnarl | 82 | Prankster | reflect / lightscreen / spiritbreak / thunderwave — *light clay* (screens support) |
| 3 | Weavile | 82 | Pressure | knockoff / iciclecrash / iceshard / lowkick — *choice band* |
| 4 | Honchkrow | 83 | Moxie | bravebird / suckerpunch / superpower / roost — *life orb* |
| 5 | Hydreigon | 83 | Levitate | dracometeor / darkpulse / fireblast / flashcannon — *choice specs* |
| **A** | **Kingambit** | **84** | Supreme Overlord | kowtowcleave / suckerpunch / ironhead / swordsdance — *leftovers* (gets STRONGER as its team dies — the wall that worsens) |

**E4 #4 — Drake · Dragon · ace 85** ("dragons answer to strength and nothing softer")

| # | Species | Lv | Ability | Set |
|---|---|---|---|---|
| 1 | Kingdra | 83 | Sniper | dracometeor / hydropump / flipturn / raindance — *choice specs* |
| 2 | Kommo-o | 83 | Bulletproof | clangoroussoul / clangingscales / closecombat / earthquake — *throat spray* |
| 3 | Flygon | 83 | Levitate | earthquake / dragonclaw / uturn / firepunch — *choice band* |
| 4 | Salamence | 84 | Intimidate | dragondance / dragonclaw / earthquake / roost — *life orb* |
| 5 | Haxorus | 84 | Mold Breaker | swordsdance / outrage / earthquake / poisonjab — *focus sash* |
| **A** | **Dragonite** | **85** | Multiscale | dragondance / outrage / extremespeed / earthquake — *lum berry* (Multiscale + priority = classic brutal closer) |

**Champion — Cynthia · mixed · ace 85** (canon Sinnoh six; deepest team, tightest AI, 4 full restores)

| # | Species | Lv | Ability | Set |
|---|---|---|---|---|
| 1 | Spiritomb | 82 | Pressure | darkpulse / shadowball / willowisp / calmmind — *leftovers* (no-weakness lead) |
| 2 | Lucario | 82 | Inner Focus | nastyplot / aurasphere / flashcannon / vacuumwave — *life orb* |
| 3 | Togekiss | 83 | Serene Grace | nastyplot / airslash / dazzlinggleam / roost — *choice scarf* (flinch hax = streamable menace) |
| 4 | Milotic | 83 | Marvel Scale | scald / icebeam / recover / toxic — *leftovers* |
| 5 | Roserade | 84 | Natural Cure | sludgebomb / gigadrain / spikes / sleeppowder — *black sludge* (sleep + spikes = Nuzlocke killer) |
| **A** | **Garchomp** | **85** | Rough Skin | swordsdance / earthquake / outrage / stoneedge — *sitrus berry* |

This is essentially the already-authored `royal_league.json` champion roster preserved (it is good) — it just needs to live in `rctmod/trainers/royal_champion.json` where TBCS can read it.

---

## 5. Quests & side quests

Kept light per the showrunner note ("could just be tough battles"). Three beats, only one mandatory.

**SQ-1 · "The Full Case" (mandatory entry ritual)** — *exists, dialog `royal_badgekeeper`.*
- Giver: **Roland Badgekeeper**. Hook: the stairs are sealed until the badge case is full.
- Steps: arrive with `badges gte 10` → `cleared` entry fires → stairs open.
- Gates: `badges gte 10` (band-tag). Reward: League access (no item). Resolution: the on-camera badge count ("Cicada through Vulcan"). No new work beyond the placement.

**SQ-2 · "Hall of Redactions" (optional lore, on-theme) — PROPOSED**
- Giver: **PROPOSED plaque prop** (Hall of Champions wall) + Roland flavor branch.
- Hook: a wall of past-champion portraits; one plate is scratched blank. Roland: "Above my pay grade. Some names the League is told to forget." (Mirrors the `§k`-redacted Founder nameplate.)
- Steps: read the plaque (gate `recognition: late`) → optional "ask Roland about the blank plate" branch.
- Gates: `recognition: late` (so it only surfaces at the League tier; civilians never recognise). Rewards: a small CD tip (`economy/payout {amount:400}`), a cosmetic "Redacted Ribbon" gift via `npc_gift` loot, and a story-flavor tag `saw_redacted_champion` (pure foreshadow — spends no reveal).
- Resolution: the blank plate is the Founder's, obviously, but nobody says so. Payoff lands later at `reveal/founder_defeated`.

**SQ-3 · "Preferred Provider — League Tier" (heal convention, dressed as a beat)** — *ties to existing `aurelius_medcrest` / `sq_preferred_provider`.*
- Giver: **Dr. Asha**. Hook: her clinic is the between-fight heal tap; the Company's memo has flagged her "out of network" now the currency stabilised, and she keeps healing anyway ("the League pays me in trust, not their paper").
- Steps: heal between E4 fights. Gate: inside the League walls. Reward: full team heal.
- **Decision (see §9):** free-inside-walls (original `royal_healer` line "No charge inside these walls") vs the 100 CD paid tap (`economy/heal_paid`, the `sq_preferred_provider` extension). Recommend **free inside the League** — paid + underleveled + Nuzlocke is punishing enough; the CD sink already exists everywhere else.

---

## 6. Trainers & teams needed

**Create / fill (authoritative teams — TBCS reads these):**
- `data/rctmod/trainers/royal_elite_1.json` — fill (Aria, §4c), `battleFormat: GEN_9_SINGLES`, ai margin 0.1, bag full_restore x2
- `data/rctmod/trainers/royal_elite_2.json` — fill (Marcus)
- `data/rctmod/trainers/royal_elite_3.json` — fill (Luna)
- `data/rctmod/trainers/royal_elite_4.json` — fill (Drake)
- `data/rctmod/trainers/royal_champion.json` — fill (Cynthia), ai margin 0.05, bag full_restore x4

**Create (spawn/series — currently MISSING, no royal files under `single/`):**
- `data/rctmod/mobs/trainers/single/royal_elite_1.json` — `type:"elite"`, `series:["cobblemon-initiative"]`, `requiredDefeats:[["scorchspire_leader"]]`, `maxTrainerDefeats:1`, `spawnWeightFactor:0`
- `…/single/royal_elite_2.json` — `requiredDefeats:[["royal_elite_1"]]`
- `…/single/royal_elite_3.json` — `requiredDefeats:[["royal_elite_2"]]`
- `…/single/royal_elite_4.json` — `requiredDefeats:[["royal_elite_3"]]`
- `…/single/royal_champion.json` — `type:"champion"`, `requiredDefeats:[["royal_elite_4"]]`, `spawnWeightFactor:0`
  (Copy the shape of `single/hua_zhan_leader.json`. `spawnWeightFactor:0` because these are dialog-placed, not wild-spawned.)

**Config registry (already exists — reconcile):** `data/cobblemon_initiative/trainers/royal_league/royal_league.json` already carries the five entries with `prerequisites` chain, `coordinates`, `battleFormat`, `rewards`, and `achievementOnDefeat` (`royal_league_champion` on the Champion, `null` on the E4). Keep it as the **progression registry** (BattleVictory name-match → achievement/cap). **DECISION (§9):** either (a) sync its embedded teams to the new 6-mon rosters to avoid drift, or (b) strip the embedded teams and treat `rctmod/trainers/` as the single source of truth. Recommend (b) if the config team is provably unread by TBCS; otherwise (a).

**Levels vs cap ladder:** entry cap **80** (Scorchspire → 80). Aces 82/83/84/85/85 = +2…+5 over cap → fought underleveled, per the brutal rule. Champion defeat grants `royal_league_champion` → **cap 85** (levelcaps.json order 11, verified). Board clear later → 100.

---

## 7. Economy & rewards

Prizes are the legacy authored ladder (carried in the character `battle.prize` fields, lowered into onwin **winner** commands — ENGINE_FINDINGS: onwin tokens are winners-first, key 1 = player won; macro text must contain no double-quotes/apostrophes):

| Fight | CD prize | Item reward (config) |
|---|---|---|
| Aria | 5000 | rare_candy x5 |
| Marcus | 6500 | rare_candy x5 |
| Luna | 8000 | rare_candy x5 |
| Drake | 9500 | rare_candy x5 |
| **Champion** | **12000** | master_ball x1 + netherite_ingot x3 |

- **CD sink:** Dr. Asha between-fight heals (100 CD each) *if* the paid variant is chosen (§5 decision). At underleveled difficulty the player will heal repeatedly → a meaningful late sink; the free-walls variant removes it.
- **Shop tier:** the League is post-badge-10, so `scripts/shop_tiers` / `cobblemon-initiative shop badge_10` is already the ceiling — no new tier. Relief catalogs are irrelevant (idx pinned at 25).
- **Liberation ties:** none direct — the wheat war is resolved before the League. Flavor only (Roland's "grey suits" line, Cynthia's "the one the Company buried").
- SQ-2 pays a small `economy/payout {amount:400}` + a cosmetic loot gift (no CD-inflation risk at this scale).

---

## 8. Implementation notes / FUTURE-ME HOOKS

**Exact files to create/edit:**
1. Fill 5 team files: `src/main/resources/data/rctmod/trainers/royal_elite_1..4.json` + `royal_champion.json` (currently `{}`). Shape = `rctmod/trainers/hua_zhan_leader.json` (top-level `name/identity/ai/battleFormat/bag/team[]`, species un-prefixed, `heldItem` cobblemon-prefixed).
2. Create 5 spawn files: `src/main/resources/data/rctmod/mobs/trainers/single/royal_elite_1..4.json` + `royal_champion.json`. Shape = `single/hua_zhan_leader.json`.
3. Add `placement:{x,y,z}` to the 5 fight characters in `dialog-src/characters/royal/royal_elite_1..4.json` + `royal_champion.json` (they have neither uuid nor placement → no spawn is generated today). Use §3 coords once the builder confirms interior Y.
4. Add gauntlet gating to the 5 dialog trees `dialog-src/dialog/royal_aria|marcus|luna|drake|cynthia.json`: gate each `default` challenge behind the previous `defeated_*` (see §4a table) + add a locked-fallback entry keyed off the **inverse** band-tag `no_defeated_royal_elite_N` (Easy NPC ignores NOT_EQUALS — must use the inverse tag; `band_tags.mcfunction` already maintains all five).
5. Reconcile `trainers/royal_league/royal_league.json` teams (§6 decision).
6. SQ-2 (optional): `dialog-src/characters/royal/hall_of_redactions.json` (prop, pattern = `hua_zhan/rezoning_notice_board.json`) + a dialog tree; add a Roland branch.

**Pipeline to run (in order), per the brief:**
```
scripts/content_compile            # lowers dialog-src → easy_npc presets
scripts/generate_granary_tiers
scripts/update_preset_index
scripts/generate_npc_function      # writes npc/preset_map.json + function/update_npc_presets.mcfunction
```
Then in-world: `/cobblemon-initiative install run` (preset refresh canary; Dr. Asha's nameplate is the documented end-to-end proof). RCT team edits under `rctmod/` are datapack-loaded — `/reload` or world restart.

**Patterns to copy:** interior ladder + leader block = **Gym 2 Hua Zhan** (`gym_leader_hua_zhan.json` for the gate/`after`/`default` priority structure; `hua_zhan_city.json` for the config registry + prerequisite chain; `hua_zhan_leader.json` under `rctmod/trainers` + `single/` for the team+spawn split). The `{ "do": "battle" }` action lowers to `tbcs battle GEN_9_SINGLES @s vs <trainerId>` using the character `trainer` id.

**Gotchas:**
- **`{}` teams = silent broken battles.** `content_compile` already warns on all royal battle refs to empty teams — that warning is the live blocker, not future noise.
- **Champion win tag is `royal_league_champion`, NOT `defeated_royal_champion`** (defeat_tag override, ENGINE_FINDINGS + character file). `band_tags.mcfunction` maintains `no_defeated_royal_champion` for the *inverse of the wrong tag* — verify the Champion `after` gate keys off `champion:true`→`royal_league_champion`, and that nothing depends on `defeated_royal_champion` being set (it never is). Flag if any dialog/HUD reads the wrong tag.
- **`y:166` in the config is stale** vs zone `centerY:64` — do not ship placements at 166 without builder confirm.
- Macro-delivered text (onwin prize commands, any `economy/payout`, memory fragments) must contain **no double-quotes and avoid apostrophes** — the authored royal dialogs already write "do not"/"does not" for this reason; match that style.
- Cap enforcement is ours (`EXPERIENCE_GAINED_EVENT_PRE` clamp to `LevelCapManager`), not rctmod's; TBCS wins never register in rctmod. The 85 step rides `royal_league_champion` being granted in `onTrainerDefeated` (round-11 linchpin fix) — the config `achievementOnDefeat` must stay `royal_league_champion`.

---

## 9. Dependencies & open questions

**Depends on (other area keys):**
- **scorchspire** — gym 10 → cap 80 + `defeated_scorchspire_leader` is Aria's prerequisite and the 10th badge that opens Roland's gate.
- **mainline_spine** — recognition band-tags (`recognition: late`), badge-count band-tag (`badges gte 10`), `cd_instability=25` state, HUD next-objective handoff to the Board.
- **company_hq** — DJ's defeat is what stabilises `cd_instability` to 25; the League's "the Company buried you" tone assumes Act 2 is resolved.
- **board_and_founder** — the League is the *gate before it*: champion → 85, then Board → 100 → the Founder. Cynthia explicitly hands the name-reveal to that area. Tightest downstream coupling.
- **gym_system_pvp_doubles** — reuses its dialog battle-button + gate + rctmod team/spawn split patterns wholesale.
- **battle_frontier** — downstream: plays in the 85–100 window the champion unlocks (sibling post-league content, also blocked on the same empty-team batch).

**Decisions the showrunner must make:**
1. **E4 team size:** 6 mons each (proposed brutal wall) vs the legacy 4? (Recommend 6.)
2. **Champion ace level:** 85 (ENGINE_FINDINGS "~85", keeps within the brief's ~82–85) vs bump to 86 for a clearer peak? (Recommend 85; differentiate via 6th mon + 0.05 AI + 4 restores.)
3. **Heal convention:** free-inside-walls vs 100 CD paid (Dr. Asha). (Recommend free.)
4. **Dr. Asha placement:** does the League get its own healer body, or must the player leave to heal? Her file comment points the clinic at Sango — this must be resolved for a fair gauntlet.
5. **Gauntlet lock:** open (heal freely between fights) vs an optional "Sealed Hall" hard mode (a tag that blocks the heal tap until the Champion falls). (Recommend open; Sealed Hall is a stretch hard-mode toggle.)
6. **Drake format:** keep `GEN_9_SINGLES` vs make it the marquee `GEN_9_DOUBLES` "dragon storm." (Recommend singles.)
7. **Config team reconciliation:** sync embedded `royal_league.json` teams to the new rosters, or strip them and make `rctmod/trainers/` the single source? (Recommend strip if provably unread.)
8. **Fight-character bodies:** add `placement:{}` (once-per-world spawn from the compiled presets) vs adopt builder bodies with UUIDs? (Recommend placement blocks.)
