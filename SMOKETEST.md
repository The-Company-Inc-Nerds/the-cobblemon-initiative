# Smoke Test — The Cobblemon Initiative

In-game verification checklist. For each point, mark **one** box and use the `comment:`
line under it for anything you notice.

| Box | Meaning |
|-----|---------|
| ✅ | **Approved** — works as intended |
| 💬 | **Works, with comment** — functions but see note |
| ❌ | **Not working** — broken / regressed |

**How to fill:** put an `x` in the one box that applies (`[ ]`), then type on the
`comment:` line beneath it. Deeper detail / root causes live in `docs/VERIFICATION_RUNBOOK.md`.

- **Build under test:** `0.5.0-alpha.2`  (note the `dev/log-<version>` here when you capture a log)
- **Tester / date:** Cole J. Calamos / 07-06-2026
- **World:** [x] fresh mrpack install  ☐ dev run-client  ☐ bare-mod standalone

> Fresh world → **no manual setup**. (The only per-world command you might use is
> `/cobblemon-initiative install run` if auto-install didn't fire.)

> **alpha.16 verbal results (2026-07-06):** skins ✅ · grunts spawn ✅ · spawn ❌ (off by a
> few blocks — root cause `spawnRadius 10`, fixed) · gym interior trainers ❌ (no bodies
> existed — 8 created) · design change: Company nameplates → normal names (applied).
> Unreported alpha.16 items (cap 15, starter despawn, dex quest, Marlow/mom) carry over below.

## ★ Re-verify (0.5.0-alpha.1) — just these; assume everything else still works

> **alpha.17 round-4 results (2026-07-06):** R2 names ✅ · C1-C4 ✅ · sections 0/1/2/3/5/6/7/9
> largely ✅ · **R1 spawn ❌ (y:122 roof — root cause: vanilla ignores SpawnY for new players,
> fixed via Data.Player bake + join snap)** · **R4/4.1/4.3 battles ❌ (TBCS keys its registry
> `rctmod:<id>`; our bare ids NEVER matched in any build — fixed; beaten-lines also fired
> unconditionally: action gates need the doubled ConditionDataSet key — fixed)** · R3 💬 default
> skins (authored) · plus tester notes: derby retuned, Lucian handoff, clinic sidebar, Y nudges.

- **R1** — Spawn is EXACTLY 2612.5/109/2841.5 inside the house, clean inventory/no effects
  (Data.Player bake; requires a freshly built mrpack — dev worlds use the JOIN snap instead)
  - ✅ [X]   💬 [ ]   ❌ [ ]
  - comment:
- **R2** — **Battles START everywhere**: a Blossom Path regular (Jabari/Ayo) hails, battles once,
  pays once, shows the beaten line ONLY after the win, no re-battle. Log has zero
  `No such trainer registered`
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment:
- **R3** — **Takehara ladder (round-13 redesign)**: tower is OPTIONAL (Sora battles with zero tower wins); 1 tower win → Sora's team drained (IVs/EVs 0); 2 → Aiko drained; 4 → Cicada drained (her say calls out all four names); chain Sora→Aiko→Cicada + badge + cap 22 unchanged; weak wins credit progression identically
  until Sora; Cicada locked until Aiko; leader win → badge + cap 22 (the 2.5/2.6 linchpin)
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment:
- **R4** — **Skins**: tower trainers ×4 wear the takehara group skin, Cicada the leader skin;
  Old Deng + Granny Yun have skins and stand at y=90 (not sunken); Manager Jun at y=90.
  EXISTING WORLDS ONLY: kill + latch-reset the six placement bodies first (see runbook), then
  re-tag Deng/Yun (`deng_old`/`deng_granny`/`deng_camp`) or the homecoming walk breaks
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment:
- **R5** — **Derby**: 3 fish, 2-minute bar, pufferfish/tropical fish count, first win pays a
  Poké Rod on top of the old purse; repeat wins unchanged
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R6** — **Lucian** (she/her lines): stage-1 filing TAKES the three papers; dead letter and
  Memo 44-C leave the inventory on hand-in
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R7** — Dr. Asha's clinic list shows in the sidebar after accepting ("Clinic list: 8 oran,
  4 pecha, 2 cheri")
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R8** — **Tone pass**: payout actionbar reads "Verified Rate" (NO "Company") on civilian
  quests; branded "Company Verified Rate" appears ONLY on census sign / courier sell /
  Invitational purse / Adjusted Retail. First-join message has no `[The Company, Inc.]` prefix;
  checkpoint warn says "A checkpoint ahead."; battle UI shows "Site Assessors" / "Survey Canvasser"
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment:
- **R9** — Fresh mrpack build only: log has zero `Model validation failure` (UPM2's stale
  trainers stripped from data.zip at build)
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment:
- **R10** — Roof doubles win → BOTH suits leave (Noboru via onwin delete, Chiyo killed by the
  same onwin — round-4 catch). Existing worlds where she already stands: one-time
  `kill @e[type=easy_npc:humanoid,name=Chiyo,limit=1]` while on the roof
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R11** — **Quest tracker**: `]`/`[` cycle active quests (actionbar "Tracking: …", off past
  the end); tracked side line turns aqua with ▶; a JourneyMap waypoint (map + beam) sits on the
  objective and MOVES when the stage advances (e.g. memo: tent → Lucian); relog keeps tracking;
  finishing the quest auto-clears it; `/cobblemon-initiative track status` works un-OPed
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R12** — **Chests + rumble**: unplaced chests now roll EMPTY 75% of the time (ModMenu →
  Loot Chests → "Empty Chest Chance"; empty ones stay claimed, no re-roll). Ground-shrine
  earthquake rumble pitch defaults 0.5 (was invalid 0.4; old saved configs self-heal on read)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R13** — **Round-13 batch**: shop tier 0 sells ONLY Poké/Slate/Premier (2000 CD each; heal ball+cosmetics from badge 1); derby ignores pre-carried fish (carry 3 salmon in → bucket 0 of 3); cascade gold pays 300 once/day (repeat golds = title only); sprint 120s first run / 100s daily; HZ wardens 17-19 + Jr 19-20 + KYC Femi 19-20; granary ambush battle STARTS (rctmod: prefix)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R14** — **Round-13c batch**: GARDEN STATUES — four wardens render as posed stone monuments
  (showrunner skins: nature/marble/knight/fallen-angel), each a SOLO themed mon (Shroomish 18 /
  Applin 18 / Cottonee 19 / Lombre 20); defeat = particle burst + beacon chime + 60s glow; seal
  press chimes. TRADERS — Auntie Song berries / Bo Huan apricorns / Madam Qiu mints at the HZ
  market; broke purchase = red decline. BATTLE MATRIX — route trainers hail at 8 blocks and
  force-battle at touch; bow-out fees (meadow 80 … kite 150) charge once + permanent stand-down;
  BROKE = must fight; Company grunts have no free exit; checkpoint/Yan/KYC = fee-or-fight.
  DENG ESCORT — walk button → all three genuinely follow (teleport catch-up), arrival at the
  gate pays once; sidebar flips to "Lead the Dengs". EAVESDROP — silent resets now speak
  (Out of earshot / LOGGED / CLIPBOARD UP); armed eavesdrops open immediately. LUCIAN sits.
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R15** — **Quest-flow Tier A** (visible spot-checks): sell the courier the docs → ASSET
  LIQUIDATED sting + permanent dark-red sidebar tombstone + Elder Sentinel cold shoulder;
  sign the census → Kazuo in Takehara knows ("the photograph resolves"); badge 1 →
  fireworks THEN the purple fragment title 4 seconds later, alone; liberate Firstfurrow →
  FIRSTFURROW — LIBERATED + bell + Tunde/Sentinel/Masumi react; file 3 papers with Lucian →
  her greeting changes ("the desk saves the good clips"); carry 2+ papers → sidebar shows
  "File with Lucian: n papers" + waypoint; Lila "word around town" rolls different rumors;
  Invitational rounds get title cards
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R16** — **Tier B**: fresh starter-only player walks Blossom Path → trainers HAIL but do
  NOT force-battle (floor = starter + 1 real catch; after one wild catch they pounce); nurse
  fee reads the posted rate (116 CD after badge 1, decline line quotes it); dawn → one Company
  memo line + HZ market day announce (Leppa vs Lum day at Auntie Song, mint drawers rotate,
  prices identical); derby chalkboard names a RECORD SPECIES (catch it in-quarter → +75 CD);
  wager wins sometimes run +25..100 heavy (announced); two fresh worlds → tower trainers can
  differ (Koji: Caterpie/Weedle vs Wurmple/Scatterbug) with identical credit/weakening
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R17** — **New mods**: battle UI shows Battle Extras QoL (type effectiveness etc.) in a
  trainer fight; SimpleTMs items exist (/give a TR, teach a legal move) — NOTE its drop/craft
  config is UNCURATED until the TODO balance pass; flag anything that trivializes movesets
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R18** — **TM economy**: beat a gym leader → a type-signature TM drops (Takehara → TM
  U-turn); Torn-Label Tadashi in the Sango back lane sells 8 TRs (broke → declined, no stock);
  Machine Counter Mika at the Takehara mart shows the TR rack unlocked at 1 badge, TM cabinet
  LOCKED until 2, glass cabinet LOCKED until 3; a bought TM teaches a legal move and burns a
  use (8 total, no repair); no TMs/TRs drop from wild Pokemon in the overworld
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R19** — **Playtest batch 1**: track a quest, relog → its waypoint reappears; change or clear
  tracking → the old marker is GONE (no permanent orphans, no leftover after tracking changes);
  Elder Nuru and Elder Sentinel say DIFFERENT things; Fara mentions the market exchange + her
  tally book (no noticeboard); "The Lane Looks After Its Own" shows a sidebar line + waypoint;
  Machine Counter Mika shows NO locked buttons (only earned tiers appear per badge count); the
  two square auditors read Bomani/Jelani (not Kesi/Mosi); companion Pokemon show nicknames
  (Deka's Magikarp is "Jackpot")
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R20** — **Playtest batch 2**: walk near Jabari/Ayo on Blossom Path → they hail at ~8
  blocks then the battle STARTS on approach (no right-click needed); the trainer does NOT
  keep chasing you during the battle; there is NO free "Enjoy the picnic" exit — only fight or
  Pay-to-bow-out; beating Jabari shows the gracious "have a snack" line (not the boast);
  Takehara tower sentries stand at the 4 new posts; the two Sango square auditors are new
  bodies (Bomani/Jelani), Kesi/Mosi untouched; unplaced chest with items gets overwritten
  (60% empty / else ~2x loot), and the ModMenu shows "Overwrite Existing Content"
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R21** — **Automatic NPC sight**: on a FRESH world (no manual npcsight commands) the
  Sango square auditors (Bomani/Jelani) detect you during Off the Record — cross their sight
  cones → "OBSERVATION LOGGED"; the checkpoint agents / surveyor / other placement watchers
  work the same with zero setup. Log at startup: "NPC Sight loaded 7 tag-keyed profile(s)".
  No `npcsight add <uuid>` step needed anywhere. (Takehara tower stealth was removed — those
  are plain battle trainers now.)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R22** — **NPC noter dev tool**: `/cobblemon-initiative npcnote stick` → whack a statue/NPC
  (it selects, no damage) → `npcnote note <text>` + `npcnote move` (or right-click a block) →
  `npcnote log` pastes all notes to chat; relog then `npcnote log` still lists them
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R23** — **In-world smoke-test command**: `/cobblemon-initiative smoke list` shows all
  R-items with status glyphs; `smoke next` gives the first unmarked one; `smoke pass|comment|fail
  <id> [note]` records a result; `smoke log` dumps them copy-pasteable; results survive a relog
  (stored in npc_notes.json); startup log reads "DevNote loaded N smoke-test item(s)"
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R24** — **Sango secret, Victor → Victini**: Kesi at the granary talks up his silent
  apprentice (his "Lucky how?" line sets the hook). Walk to Victor at the tower top — he is
  SILENT (narration only, no transform) until you have all **five** conditions: heard of him
  from Kesi (`victor_hint`) AND filed the founder's papers with Lucian (`docs_filed`) AND
  finished Down the Lane (`lane_done`) AND refused the Company census (`census_refused`) AND
  **bought Deka's 500 CD Magikarp** (`bought_magikarp` — faith in the worthless fish). With all
  five, talking to Victor → "Reach out" → he despawns in an end-rod/beacon flourish and a
  Victini NPC appears → talk to it → "Take my hand" → **Victini joins at level 15**, no
  duplicate. Elder Nuru, for the anti-Company **trio only** (papers/lane/census, not the fish),
  gives **3 oran berries once**. Dev aid: `/cobblemon-initiative debug victini` prints ✔/✗ per
  condition + verdict — confirm all five land. Also: Kele's lane gift is now a free **Eevee
  with a 1/20 shiny chance** (not a Magikarp).
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R25** — **Route entry titles** (needs an install run to repopulate zone subtitles/type):
  walk into a route (e.g. Blossom Path) — the mod's title toast shows ONLY the flavor line
  ("Petals drift over the first road you will ever remember."), NOT the route name repeated,
  and never a bare "Route 1". Towns/shrines still show name + subtitle as before.
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R26** — **Item hand-ins require the items** (Easy NPC has_item was broken): with an
  EMPTY inventory, the hand-in buttons must NOT complete — Miri (16 kelp), Raan (8 coal /
  4 raw iron), Dr. Asha (8 oran / 4 pecha / 2 cheri), Masumi (8 honeycomb → no Combee
  without it), Kenji fossil revivals (no fossil → no Kabuto/Anorith). With the items,
  each clears them and pays out exactly once. Also: auditors Bomani/Jelani now wear a
  Company grunt skin; the "Lane Looks After Its Own" marker steps Fara → Kele → Dakarai;
  the Invitational cast (Pondwarden Tayo / Reedhand Lumo / Net-Mender Kima) reads savanna,
  not harbour.
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:
- **R27** — **Sefu trade + Victor skin + derby**: with a Magikarp anywhere in your party,
  trade with Old Sefu → the Magikarp is REMOVED (any slot) and a Feebas lv 10 is added;
  with NO Magikarp → "you have no magikarp to trade", nothing given, traded_sefu unset.
  Victor (human apprentice, tower top) wears the MKS skin. Taking Deka's spare rod does
  NOT make the Sango Classic appear in the sidebar/tracker — it only shows once a quarter
  is live (after paying entry).
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment:

> **Fishing (resolved 2026-07-05):** Sango already has TWO fishing events — the Sango
> Classic derby (Deka) and the Shorefront Invitational (Tayo/Kofi/Lumo/Kima, lv 8–13).
> The Invitational is level-locked to Act 1 and staked to the Sango wheat-field paymaster,
> so it CANNOT move to Gaviota (cap 58). Plan: (1) retheme the Invitational's "shorefront/
> docks/harbour" dressing to a **savanna pond/lakeside** — dialog-text reskin only, no world
> building — to fix the biome mismatch; (2) build a NEW cap-58 marquee "**Gaviota Open**" at
> the actual port as the primary fishing hub. Both DEFERRED to a post-smoke-test round;
> awaiting greenlight on the Gaviota Open sketch. See memory `fishing-content-map`.
> ~~Gym interior trainer_1–4 still need bodies~~ — DONE in alpha.17 (all 8 bodied,
> placement-latched at the gym-config coords; skin polish deferred).
>
> Full checklist below is reference/first-run only — you don't need to re-run it.

---
- Chiyo does not despawn on the roof after defeat

## 0. Boot, install & datapack

- **0.1** — World boots; no `Failed to load function` / `Couldn't parse data file` in log
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **0.2** — Fresh pack world: auto-install runs ~2s after first join (or `/cobblemon-initiative install run` is clean)
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **0.3** — Log shows `[Easy NPC compat] Patched … security.cfg`
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **0.4** — Log shows `[rctmod compat] Healed server config: allowOverLeveling=true, initialSeries=cobblemon-initiative, spawning off`
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **0.5** — Difficulty Hard, hardcore ON, survival gamemode, **no** speed buff on the player
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **0.6** — `/cobblemon-initiative install check` → allowlist OK + bundled-map report
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 

## 1. Opening chain — starter trio, dex ladder, mom, pokédex, shoes

- **1.1** — **No** vanilla starter toast/screen at any point
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.2** — Open party → **no** "You have not yet selected a starter…" message
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.3** — Mom **walks up** once; never re-approaches after (relog / walk away)
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.4** — Mom's dialog has a working **Goodbye**; no "Give me a moment"
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.5** — Accept errand → main line "Visit Professor Acacia" + side line "Choose a partner" within ~1s
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.6** — Professor "Choose a partner" → **3 starter NPCs spawn** rendered as the actual Pokémon (Skiddo / Totodile / Hisuian Growlithe)
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.7** — Hisuian Growlithe stand-in shows the **Hisuian** model (needs AllTheMons active)
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.8** — Click "Choose a partner" **again** → no duplicate spawns
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.9** — Each offer has a **"Keep looking"** button that closes without choosing
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.10** — Choose one → Pokémon (Lv5) lands in party + `chose_starter` set; **chosen stand-in despawns**
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.11** — Growlithe chosen → summary shows **Hisuian** Growlithe (fire/rock)
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.12** — Other two stand-ins → **cry-only** dialog (+ Goodbye)
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.13** — Professor re-talk → "Take the Pokédex" → pokédex item + `got_pokedex` + HUD flip
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.14** — **Dex ladder:** 15 unique CAUGHT entries → 2nd starter claimable @ Lv25
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.15** — 30 entries → 3rd starter claimable @ Lv40
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.16** — Mom → Running Shoes → side line clears, main line = "Defeat Takehara Gym"; shoes **+30%**, no other speed source
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 

## 2. Level caps (ladder 15 → 22 → 30 → … → 80 → 85 → 100)

- **2.1** — XP stops **exactly at 15** pre-badge (actionbar "Level cap 15 — the next badge raises it")
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **2.2** — A Totodile **cannot** reach Lv18/Croconaw before gym 1
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **2.3** — Rare candy at cap is **refused but NOT consumed**
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **2.4** — rctmod's own "level cap" actionbar warning does **NOT** appear
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **2.5** — **Beat gym-1 leader → cap raises to 22** ("Level cap increased to 22") — the linchpin fix; if it stays 15 this regressed
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: [auto 2026-07-12, alpha.13 dev] manager layer verified headless: badge 1 → cap 22 (dev badges); the BattleVictory→badge event edge still needs a real win 
- **2.6** — Cicada's ace is **Lv17** (cap+2 — fought underleveled)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **2.7** — `/rctmod player get series` → cobblemon-initiative; `get progress` reflects the gym win
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **2.8** — ENDGAME (forced tags): grant champion achievement → cap 85; defeat all 4 `board_member` → "Board has fallen. Cap raised to 100"
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: [auto 2026-07-12, alpha.13 dev] verified headless via dev grant: 10 badges→80, royal_league_champion→85, board_cleared→100 

## 3. Economy — payouts, training packs, heal gate, battle money
> NOTE (alpha.14): training packs now SCALE with badge count (economy/reward/<tier>) — 3.2-3.4 amounts below are the EARLY-game (badge 0-2) contents; candy tier/count grow by era.

- **3.1** — `economy/payout {amount:N}` → ~N CD + gold "Company Verified Rate" actionbar
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **3.2** — Small errand (≤260 CD) → payout **+ minor training pack** (3× Exp Candy XS + 1× S)
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **3.3** — 300–400 CD completion → **standard pack** (2× S + 1× M)
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **3.4** — 500–600 CD finale → **major pack** (1× Exp Candy L + one random vitamin)
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **3.5** — Repeatable (derby re-entry / cascade gold-time / daily sprint / clinic rx) → **money/potion only, NO items**
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **3.6** — Nurse heal while broke → red "Payment declined", no heal, balance unchanged; with funds → heal + fee
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **3.7** — Win a dialog battle vs undefeated trainer → prize credited **once**; re-talk = already-beaten line
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **3.8** — Lose a fee battle → fee deducted **from YOU**; the **NPC** speaks the taunt
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **3.9** — Gym leader reward has **no emeralds** — CD + badge/shop only
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 

## 4. Gym battles & wiring

- **4.1** — Battle **starts** vs an undefeated gym trainer (not silently refused)
  - ✅ [ ]   💬 [ ]   ❌ [x]
  - comment: All trainers on Blosom Path act like I already battled them when I challenge them to the battle, but there dialog and they chase me down so seems like an rctmod side isnt working?
    [auto 2026-07-12, alpha.13 dev] battle STARTS on the current tree: engage_takehara_leader (attach + tbcs battle) opened a real battle UI vs Cicada with a quick-play client — screenshot-verified. Gotcha found: tbcs battle fails "not attached to an entity" if the trainer NPC's chunk isn't loaded when attach runs.
- **4.2** — Correct trainer credited on win (esp. **Kalahar** trainer_1/2 — name-swap fixed)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **4.3** — Takehara jr/apprentice/leader all beatable in order; leader unlocks badge + cap
  - ✅ [ ]   💬 [ ]   ❌ [x]
  - comment: They all act like they battled me already.
- **4.4** — Hua Zhan apprentice's Roselia has **no** Model-validation warn (poisonstingspore→stunspore)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 

## 5. Quests — towns 1-3

- **5.1** — Census taker's **first** dialog opens; accept → HUD line; finish → +500 + paper
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **5.2** — Dead-letter: take from Marlow → "deliver" side line within 1s → deliver to Lucian
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **5.3** — Price check accept → "0/3", each note increments
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **5.4** — Sidebar HUD shows active quest lines (main + side), updates on progress
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **5.5** — Spot-check 4–5 random NPCs: every dialog has a working exit (Goodbye or native close)
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 

## 6. NPC placement, ambient life, companions & skins

- **6.1** — **Company Courier** spawns at its cart (~2592/111/2815) wearing the **rctmod skin** (team_galactic_grunt), separate from Lumo
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **6.2** — Lumo still at the docks (kept the CSV body)
  - ✅ [ ]   💬 [x]   ❌ [ ]
  - comment: No docks but he is there properly 
- **6.3** — Roof: both yield agents flank Mayor Suzune; Harvest Road: surveyor/escort/wagon + Firstfurrow officer/site-manager + Deng camp + watch lantern
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **6.4** — Hua Zhan gym gate: yield analyst + rezoning board; four garden stations; branch office pair
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **6.5** — Companions render as real Pokémon: Mr. Mime (Mom), **Magikarp beside Deka (not in a wall)**, Hoothoot (Nuru), Sentret (Oma), Chansey (both nurses), Combee, Psyduck, Meowth, Wooloo
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **6.6** — Wanderers drift & return (Kofi, Taya, Elder Nuru, museum Sayuri); **anchors stay put** (lane doors, stalls, nurses, Mom, Acacia)
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **6.7** — Relog → **no duplicate** companions / placed NPCs
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **6.8** — No `Unknown Cobblemon species` lines in log
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **6.9** — Any placed NPC clipping a wall/furniture — note name + coords in comment
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 

## 7. NPC refresh & shop tiers

- **7.1** — Walk into a mapped NPC's chunk → `[NPC Refresh] Applied …` once; casting correct (nameplate/skin)
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **7.2** — `/cobblemon-initiative shop badge_1` → "Applied shop tier" log; `shop refresh` error-free
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 

## 8. Standalone (bare-mod instance, no mrpack overrides)

- **8.1** — Fresh bare instance (mods only, no config folder) boots
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **8.2** — Log: security.cfg patched **and** `[rctmod compat] Healed …` both appear
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **8.3** — An NPC command button works on the **first** press
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **8.4** — Level cap is 15 pre-badge here too; custom skins absent → default Steve (cosmetic, expected)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 

## 9. Log sweep (post-session)

- **9.1** — `Blocked execute-as-user` — absent
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **9.2** — `Failed to load function` — absent
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **9.3** — `Unknown or incomplete command` — absent
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **9.4** — `[NPC Refresh] Import failed` — absent
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 
- **9.5** — Full log saved to `dev/log-0.4.3-alpha.15` if anything failed
  - ✅ [x]   💬 [ ]   ❌ [ ]
  - comment: 

---

## General notes / anything else found

- The Dr. Asha quest for berries does not appear in the quest log 
- The fishing derby in Sango, I think it should be 3 fish and like1/3 of the time, the prize at the end should be a pokerod as well. Also it is not accepting the other unique fish like pufferfish and stuff.
- The rebuild for lucian (who is female) she does not take the papers at the end?
- Trainers in gym properly placed but have the default skins, cicadas skin was not changed, but all the company members skins changed
- Harvest hand wager said it was done today
- Manager Jun needs his Y level up by 1
- Old Deng and Granny Yun need Y levels adjust, both by 2. They also have default skins so we need to make them as to be added.
- Mei Lin in the Hua Zhan City is the nurse
- [DONE 2026-07-12] Wheat sellers now trade in COMPANY WHEAT SCRIP (renamed paper + custom_data{ci_scrip}); plain paper rejected as currency (jar-verified). Human: eyeball the trade GUI.
- Groundskeeper Aya is the Hua Zhan Gym Leader
- Alot of work needed in Hua Zhan City
- Unplaced chests should roll whether or not its empty (with a setting in mod menu to set the odds)
