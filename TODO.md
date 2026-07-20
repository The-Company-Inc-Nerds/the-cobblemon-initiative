# The Cobblemon Initiative — Implementation TODO

Living checklist for everything still outstanding before **1.0.0**. Edit freely and tick
items off (`- [x]`). Claude references this file to know what's left; when an item is
done, Claude removes it **and** any release-removal it unblocks.

**Legend:** 🧱 = you (in-world / map authoring) · 💻 = Claude (code/data) · 🔍 = verify in-game

---

## 2026-07-20 — Dialog cohesion pass (alpha.10): all-cast punch-up

**357 files punched / 444 already strong / 57 new interactive touches** (one-time gifts,
turn-in gags, lore pages, announce beats — no new battles/payouts/economy). Mr. Mime is
now deniably More Than Just A Friend to Nalia; penthouse cast authored (3 Lopunny
assistants + bedside Incineroar, `characters/villain/companion_*`). Compile 746→750
errors:0; mech-diff zero drift; all 315 e2e-matched strings intact; lint at the
pre-existing 81-error baseline. **repairs_a10** (171 latch bodies, 6 batches, typed
kills) wired — existing worlds refresh on install run.
- [ ] 🧱 Penthouse cast placement — blocked on the HQ tower interior build; proposed
      coords in the four `companion_lopunny_*`/`companion_incineroar_bed` `_comment`s
      (roadmap 09: ~[1588/1592 95 1030], volume [1590 95 1028]). Add `placement` when built.
- [ ] 🔍 Walk-up spot-check after install run: Mr. Mime / a gym trainer / a turnin gag
      (e.g. Juno Pixel, Cyber) / a rumor pair (Volt generator chain).
- [ ] 💻 Showrunner rulings queued: 4 remaining "trains blindfolded" regional rumor seeds
      (kept as a traveling archetype — thin them?); Tayo Harbourmaster-vs-Pondwarden title
      split (pre-existing — texture or stale copy?); Scorch's badge-crate line undercuts
      Vulcan's forge mystique (kept deniable, as his claim).
- [ ] 💻 `royal_scrollkeeper.json` is dead content (unreferenced) — cleanup-pass candidate.
- [ ] 💻 Lint baseline: 81 pre-existing errors (43→41 DEAD_ENTRY, 40 ORPHAN_TAG) predate
      this pass; auditor_a/b MANUAL_ONLY errors are a lint blind spot (opener lives in
      npcsight_profiles.json, which dialog_lint does not trace) — teach lint the profile file.
- [x] 💻 Wiki + docs re-audited against alpha.3–.10 shipped reality (2026-07-20):
      Shrines page (cultists removed, keeper/trial/crystal two-beat, crystal→own guardian
      roster Ho-Oh/Landorus/Glastrier/Kyurem/Xerneas, hydra Alpha/Beta doubles + Omega
      singles), Battle Frontier page (real per-hall mechanics: custody/wheel/points/
      listings/crew/no-heal/climb), Nobles page (crystal≠bird-launcher fix), Home
      (nickname ritual), Hua Zhan garden-statue merge (Act-I + Quests-Hua-Zhan),
      ENGINE_FINDINGS §5, FRONTIER_CONCEPTS shipped table, roadmap/09 penthouse cast
      (3 Lopunny + Incineroar), QUESTS 13/14/15 status banners. Confirmed no stale dialog
      quotes, 0 broken [[links]], guardian roster consistent across pages.
- [ ] 🧱 **Re-run `publish-wiki`** to push the 2026-07-20 wiki edits live (last synced
      2026-07-18; local `wiki/` is now ahead by the pages above).

## 2026-07-19 — Skin dress pass (alpha.9): 99 placed Steve-default NPCs dressed

**Audit (3-agent sweep): of 747 chars, only 99 PLACED latch NPCs still rendered literal
Steve** (187 explicit skins / 330 world-body builder looks / 96 cobblemon_model / 6
visuals covered the rest; 0 dangling textures, 0 unprovisioned uuid5 PNGs — the
"groups/frontier missing blocks compile" memory was stale, resolved since alpha.14).
- **12 new textures** fetched via skin_scout/MineSkin (contact-sheet-reviewed; sheets +
  credit manifests in `dev/skins/<target>/` for sign-off): `nurse_center` (all 6
  nurses — one clinic look), `wheat_factor` (EVERY wheat trader shares the silhouette —
  viewers learn to spot the conspiracy), `town_shopkeeper`, prop kit
  `prop_stone_marker`/`prop_wood_post`/`prop_crate`/`prop_screen` (~21 object NPCs:
  survey stones, noble monuments, notice boards/lecterns, manifests/wagon, Cyber
  billboards/archives), and 5 shrine keepers (Draconis=samurai, Aurora=druid,
  Ignis=fire monk, Glacius=ice mage, Terran=desert nomad).
- **Existing assets reused:** villain grunts→thecompany_grunt_1..4 +
  admin (management), station_*→their purpose-built hz_statue_* 1:1 (finally wired!),
  town extras→groups/* (hua_zhan.png referenced at last), deepcore/kalahar/mystic
  named NPCs→town trainer singles.
- content_compile 747/751 errors:0; **repairs_a9** (99 kill+re-arm) chained after a8 —
  existing worlds repaint on install run. Any pick is a one-line dialog-src swap if it
  reads wrong on stream (sheets in dev/skins/).
- [x] **RULED 2026-07-19 — props are STATIC**: new `ambient_static` movement objective
  (LOOK_AT_RESET only; objective container stays non-empty per the NpcSight invariant)
  on all 33 prop-skinned chars incl. the 4 Hua Zhan living-statue gym trainers (frozen
  until they wake). Statues no longer track the player. Production repaints via a9.
- [ ] 🔍 Dev world only: a9's repair flag is already consumed there — re-arm the 33
  prop latches at the next stack session so the dev bodies pick up the static presets
  (production/user worlds unaffected; a9 ships with the final presets).
- [ ] 🧱 Review the 12 contact sheets when convenient (`dev/skins/<target>/sheet.png`) —
  esp. prop_wood_post (slim-arm scarecrow) and the unnamed fire-keeper pick.
- [ ] 🧱 **IN-GAME PROP REVIEW (showrunner walk, 2026-07-19)** — eyeball all 33 prop
  bodies in-world (skin + the new frozen pose + facing). Any bad pick = one-line
  dialog-src skin swap; a bad facing = placement yaw. Tour by area:
  - **Hua Zhan** (densest stop): gym gate Notice of Preliminary Rezoning [1503 86 2041]
    (scarecrow), then the four garden statues — Moss [1450 93 2052], Orchard
    [1432 85 1964], Terrace [1478 87 2098], Pond [1484 87 2160] — REWORKED 2026-07-19
    per ruling: plaques MERGED into the living-statue wardens (one NPC per garden;
    dormant plaque-read → guide/Wei hint sets `pilgrimage_started` → "Ready a
    Pokéball" challenge → seal press on the statue → sealed epilogue). Walk the whole
    staged flow. "A Clean Square of Wall" DELETED per ruling.
  - **Cyber City**: Archive Drops ×3 (Records Annex [1518 89 1096] area), Glitching
    Billboards + Reserve Tags ×6 (Downtown [1490-1610, 1090-1132]) — all screen-bots.
  - **Gaviota Port**: Tally Clerks Pell [566 85 3564], Odile [588 86 3600], Bram
    [605 87 3650] — now PEOPLE (groups/gaviota) per the crate-retirement ruling.
  - **Harvest Road**: The Gate Lantern [1550 88 2470] (scarecrow), Survey Wagon
    [1560 88 2380] (now wood-post — was crate).
  - **Kalahar**: Basalt Survey Stones [1980 120 3960] + [2140 132 3900], Guarded
    Survey Stone [2318 83 3542], Pump Manifold [1740 116 4190] (now screen-bot —
    Company machinery read; was crate).
  - **Deepcore**: Re-Verified Reserve Ledger [1152 146 3284] (scarecrow-as-ledger — judge).
  - **Ryujin**: The Sovereign Charter lectern [2145 64 901].
  - **Noble monuments**: Warning Buoy (Mystic shore [234 65 2347]), Sky-Altar (Sky Ring
    [744 77 4589]), Crater Warding Stone (Volcano south rim [3805 302 3806] — the 🧱
    set-dressing stop you already owe the rim).
  NOTE: in the DEV world these still show old look-tracking presets until its 33
  latches are re-armed (a9 flag consumed there); your real world repaints on install
  run, so review AFTER updating the pack.
- [ ] 💻 **Still Steve but UNPLACED (32, blocked on casting):** royal_champion +
  elite_1-4, acting_ceo_dj, board_×4, villain_final_boss (check the player_double
  SkinData-patch mechanism first), daycare keeper, 8 gym guides, misc. Dress when placed.

## 2026-07-19 — The nickname ritual (alpha.9)

**Every new acquisition — capture, gift, trade, starter — prompts the player to name
the teammate** (`nickname/` + `NicknamePromptScreen`; ModMenu → Capture Rules toggle;
ESC keeps the species name; offers queue client-side and HOLD during live battles).
Coverage: POKEMON_CAPTURED@LOWEST + the giveProperties choke point (givemon is the only
live gift mechanism — zero `givepokemonother` call sites). Loaners/custody returns/
mirror deliberately excluded. Fire-and-forget contract: the server never waits.
- [x] **PRE-EXISTING DUPES-CLAUSE BUG fixed** (surfaced by the adversarial review):
  full-party catches overflow to the PC, where the PC dupe scan matched the mon against
  ITSELF (no self-exclusion → every full-party capture read as duplicate) and
  `party.remove()` no-opped — chat announced a release that never happened. Release now
  removes via StoreCoordinates; send-to-PC only re-files when the mon left the party.
- [x] entei_return givemon moved to the end of entei_show (prompt vs ASSET REASSIGNED
  title collision — the naming ritual now closes the reunion beat).
- [x] Harness: `ci_harness` send-time suppression tagged per scenario in e2e_run +
  removed at suite teardown; isolation close → drain loop; `nickname_modal.json`
  scenario drives the real flow (types via screen.key — first use of that op).
- [x] 🔍 **LIVE-VERIFIED 2026-07-19: nickname_modal 13/13 first try** — modal opened,
  driver typed "Scrambles" into the focused EditBox (first live use of screen.key char),
  C2S set the name, server confirm line matched; ESC beat walked away clean. A human
  capture/gift feel-check on stream remains nice-to-have, not blocking.

## 2026-07-19 — Frontier REAL mechanics (alpha.7 build + alpha.8 friction wave)

**Showrunner directive "I want this stuff to actual gameplay mechanics" — all seven
halls now cash their fiction mechanically via `frontier/FrontierManager` (shipped in
alpha.7; the friction wave below is alpha.8). Forks ruled: Factory full-park custody /
Port crew MULTI / Arcade conditions-only / Tower nine floors incl. the main floor.**
- **Factory** — party PARKED in hall custody (daycare persistence, write-through to
  `cobblemon_initiative_factory.json`), 3 Company-issue loaners granted; `factory return`
  claws loaners + restores; join reminder while parked.
- **Arcade** — `arcade spin` arms a wheel condition (level lock 50/75/100, doubles flip,
  purse ×2/×3 — conditions only, never the wallet); next `arcade fight` rides it.
- **Castle** — 60-point ledger per run (heal 20 / scout 15 — scout reads the next team's
  species from the bundled rctmod JSON); leftover points pay ×15 CD after the Lord falls.
- **Market** — priced opponents (bargain 200/fair 500/premium 1000 CD) via the deferred
  `#fr_ok` pay-probe; purse 400/1200/3000.
- **Port** — GEN_9_MULTI 2v2 crew battle: player + Deckhand Maru (`port_deckhand` team
  authored) vs the rival pair; one fight grants BOTH challenger tags.
- **Pyramid** — 3-stage no-heal gauntlet (door-heal once, chained dispatches, loss resets).
- **Tower** — 8-fight climb at escalating level locks {40..88} + the Tycoon authored-strength
  at the top; floor tracked on `frontier_tower_floor`; loss resets to the main floor.
- All halls: anchored tbcs dispatch + AWAITING-capture adjustLevel reset timing (Stadium
  idioms), Java-side purse pay + `defeated_*` tag grants on BATTLE_VICTORY (dialog gates
  unchanged), brains still fire `frontier_<hall>_cleared` + `hall_cleared`. Hall dialogs
  re-cut to command buttons; 7 hall scenarios re-cut (lint errors:0; compile 747/751
  errors:0).
- [x] **FRICTION WAVE (alpha.8) — hall-agent reports closed:** castle enter now REFUSES
  post-clear (enter→claim was an infinite 900-CD faucet); pyramid start strips stale stage
  defeat tags + refuses mid-gauntlet re-entry (free door-heal exploit) + RESUMES an
  interrupted gauntlet (relog/failed dispatch) with no heal; pyramid exempted from
  dispatch's alreadyBeaten (gauntlet restarts re-fight the roster; the wedge refused
  stage 0 after a loss); pyramid pays the authored purses 800/800/3000 (was the only
  zero-payout hall); market refuses BEFORE the fee once the brain is beaten (fee was
  charged, then dispatch refused — CD eaten); portCrew one-fight-at-a-time guard
  (re-clicking the muster during the capture window overwrote the live dispatch); port
  muster gate not_tag challenger_2 (was _1 — a legacy half-pair save lost the button);
  factory return-command copy; dead local. Champion gate on the raw command surface left
  OPEN per the post-game-gating-waived ruling.
- [x] 🔍 **LIVE-VERIFIED 2026-07-19 (three-round stack session):** arcade (wheel spins +
  locked fights + ×2 brain purse), factory (loan round-trip, custody file empty after),
  market (fee ladder), port (MULTI crew), tower (8-floor climb + Tycoon), castle (points
  economy after the service-close scenario fix) ALL GREEN vs the real manager — round 3
  scored 1037/1041 steps. Pyramid chained both ancients green (giant hit the TBCS stall
  window; final re-run rides the new `pyramid abandon` reset). Fixes the sweep forced:
  **FrontierManager dispatch SELF-HEAL** (a battle that vanishes without an event —
  stopbattle/wedge/abort — cleared its dispatch in ~2s instead of wedging the player out
  of every hall), **`frontier pyramid abandon`** (walk out of a held gauntlet; the
  in-memory stage map survives across runs), and the **LATCH LAW extension: a
  dialog-recut wave needs kill+re-arm for EVERY hall body, not just brains** (round 1
  ran entirely against stale-preset bodies). Scenario-side: battle-GUI close idiom
  restored, castle service buttons are command-only (menu stays open — close
  explicitly), warden title waits must PRECEDE the purse wait (Java emits
  hall_cleared before the purse line; the wait-window cursor made title-after-purse
  unreachable), Draconis tp ground-probed (old approach dropped the driver to y64
  under the trial floor — plus a stale y63 manual-placement body deleted).
- [x] **RULED 2026-07-19 — Dahlia fights under the wheel's roll** (doubles only on a
  DOUBLES slice; the wheel IS the Arcade's identity). Current Java already does this —
  no change. Optional dev nicety someday: `frontier arcade clear` wheel reset.

**Shrine structure rework (same session, ruled):** cultists exist ONLY where a trial
calls for them; the leader is the end-of-trial dialog + ALWAYS the crystal giver
(cleared@30 entries: crystal give + `<el>_crystal_claimed`); ice/fire/dragon leader
battle blocks removed; shrine_pilgrim retargeted to crystal_claimed; the 10 standalone
cultists deleted (repair a8 removes placed bodies). Shrine quests rebuilt on the
cleared→claim→trial stage pattern.

**Landed + live-verified on the headless stack (server + driver client):**
- **Hydra gauntlet is now PLAYABLE e2e** — the dragon shrine's stage battles had no
  battle source (no rctmod teams, no dispatcher). Authored `rctmod/trainers/dragon_hydra_1..3`
  (names exact-match the Initiative displayNames so defeats resolve to stage ids) +
  a stage dispatcher in ShrineChallengeManager (anchored tbcs, Stadium pattern: 2-3s
  delay, silent-refusal retry→abort watchdog, anchor sweep on every exit). Verified:
  stages 1→2→3 dispatch/advance/heal, completion latches `dragon_shrine_trial_clear`,
  RE-RUN works — which caught a real engine bug: **PlayerProgressManager.onTrainerDefeated
  early-returned on already-recorded defeats BEFORE notifying the shrine manager**, so any
  second trial attempt stranded at stage 1; shrine notify now runs before the dedup.
  Nuzlocke note: hydra faints removed 3 mons + whiteout-killed the probe bot on attempt 1 —
  shrine trials are brutal-by-design, but scenario runs need a safe-zone site or big teams.
- **Cultist_3/4 trimmed** (10 dead ids): prerequisites are runtime-inert (`canBattleTrainer`
  has ZERO call sites) and the ids had no teams/bodies/placements; shrine leaders now
  prereq cultist_1/2, npc_map_template rows dropped.
- **Safari bait is charged** — 60 CD commons / 250 executive_blend (`safari.json`
  baitFee/baitFeeExecutive), `safari/bait_fee` pay-probe (#sfb_ok), issue deferred one
  tick; concierge buttons show prices. e2e 56/56 incl. the broke path.
- **partyhas quest helper** — `/cobblemon-initiative partyhas <type> <tag>` (party
  elemental-type probe, tags synchronously so dialog functions branch in-place).
- **Marsh-Child Bryn built** (doc 01 §2 cast 6/6, Open Q3 resolved KEEP): show-a-Fairy-type
  errand → Wisp-Lantern (soul lantern, Lysira tie-in) + fairy-shrine nudge; child scale
  visuals. e2e 54/54 (incl. the no-fairy path). Boot log caught set_lore needing 1.21's
  `mode` key.
- **villain_grunt_2 ladder fixed** — contraband 18→25 (outranks mid recognition at 3+
  badges), dead `default` entry deleted. e2e 58/58.
- **NpcSight fired-reset** — `remove`/`reset` now clear the tag-profile session cache
  (the APPROACH_ONCE latch survived remove+re-add); `reload` clears all sessions.
- **Founder party-mirror (runtime)** — `founder/FounderMirrorManager`: rctapi registry
  swap (public API via reflection: unregisterById + registerNPC; TBCS auto-mirrors on
  the registry events; teams are read at battle-build so NO reload). Party cloned,
  level 100, healed. Triggers: board_cleared grant, join-while-board_cleared, the
  Founder's "Face myself" button (prepended `mirror refresh`), `/ca mirror refresh`.
  Live-verified: swap executes clean against rctapi 0.15.2. 🔍 remaining: a full battle
  vs the mirrored team (needs the Founder placed).
- **Daycare/MomCare picker is payload-driven** — `network/InitiativePayloads` (S2C
  picker_open w/ real free-slot count, C2S picker_deposit); screen + client poll reworked,
  singleplayer static bridges deleted. **daycare_board UNPARKED: 58/58 on the two-process
  harness.**
- **Showdown wedge trap SHIPPED** — GraalShowdownServiceMixin wraps sendFromShowdown
  (bytecode-verified one-liner re-impl in try/catch): a Java exception mid-interpretMessage
  is now CONTAINED (logged w/ battleId+raw message+stack via compat/ShowdownWedgeTrap,
  one-line player toast, `dev showdown trap` reader) instead of unwinding into the JS
  engine and wedging every later battle. The organic trigger auto-captures on next
  occurrence; 0 faults in ~10 battles this session. Remaining showrunner call: `stopbattle`
  paying the trainer's win branch (unchanged).
- **GROUND-PROBE PASS + repair wave a6 — only 4/48 gyms-3-7 spec-cast placements were
  valid.** Live block-probed every alpha.17/20 town-pack placement: 34 buried 1-26 blocks
  (whole cyber cast ~25 deep, gaviota ~20), 10 with no pocket at authored Y (Deepcore's
  real floor is y≈109-123, not 129; the deep office y114 not y40). All 44 dialog-src
  placements re-based to probed feet-Y, `install/repairs_a6_*` kills stale bodies +
  re-arms latches (shipped, auto-applies), scenario tp coords re-based. Bryn found at
  1.0b post-repair = the pattern works.
- **Stale-claim cleanup**: mew giver location Safari Zone→Kalahar Oasis + `q.side_wisp`/
  `q.side_deep` labels + kyogre buoy notes (alpha.4/5 relocations), safari concierge
  bait comment, wiki §1.E + town-pages claims below.

**Scenario wave 3 AUTHORED (workflow, 13 agents): 33 scenarios, `scenario_lint --all`
errors:0** — town packs gyms 3-7 (17), shrine_dragon + shrine_wiring, Frontier (registrar +
7 halls + complete), noble_giver_smoke, safari_bait_fee, bryn_wisp, memo_checkpoint_recognition,
plus safari_yards funding + daycare_board un-park edits.

**VERIFICATION LOOP RUN 2026-07-18/19 — 22 of 32 FULLY GREEN:** all five town packs
(mystic ×3 + bryn, deepcore ×4 incl. the full Iron Ladder 87/87, gaviota ×3, kalahar ×2,
cyber ×4), memo_checkpoint (soften ruling verified 60/60), shrine_wiring 53/53 (incl. the
mandatory-fairy gate), frontier_registrar 64/64, daycare_board 58/58, safari_bait_fee 56/56.
Loop fixes that landed (all scenario/harness-side): battle dispatch ANCHORING for 7
function-dispatched trainer ids (see below — real content bug), between-fight `healpokemon`
in every battle scenario (a fainted lead makes autobattle pass-loop to a slow loss),
frontier residue strips (failed runs skip cleanup → cleared/defeat tags leak), battle-GUI
`screen.close`+win-line pacing idiom, win-line windows 360-600s (Cobblemon paces battle
messages — fights run minutes), tp-off-the-import-point offsets in 22 scenarios (an NPC
import at the player's exact spot SHOVES them into adjacent blocks = hardcore suffocation;
killed the driver twice).
- [x] **CLOSED 2026-07-19 — WONDER TEAM (showrunner idea): 31 of 33 wave-3 scenarios now
  FULLY GREEN.** The brain-fight autobattle ceiling fell to
  `givemon raikou level=100 ability=wonderguard held_item=cobblemon:air_balloon
  moves=icebeam,surf,thunderbolt,flashcannon` — pure Electric's only weakness is Ground,
  the balloon negates it, Wonder Guard blocks the rest and the balloon never pops (nothing
  ever lands); slot-1 Ice Beam because NO type is immune (an Electric lead vs an enemy
  Ground-type would zero-damage-loop under first-legal-move). Solo'd a frontier brain on
  the first live test; `moves=`/`ability=`/`held_item=` all work in givemon property
  strings. All 7 halls green (114-128 steps each), shrine_dragon 94/94 (a7 applied via
  direct function call — `/reload` eats scheduled applies), noble_giver_smoke 73/73.
  **SHRINE LEADERS PLACED**: all five had NO body source at all (alpha.18 authored their
  dialogs, never their bodies) — placed at the trainer-config altar coords with live-probed
  ground (fairy re-placed INSIDE the underground shrine at [951,3,2715]; Draconis on the
  y69-71 trial floor). Harness law added: a consumed fight leaves the brain body gone with
  #amb=1 — hall preambles now kill+re-arm the BRAIN latch too (stale hud-buffer win-line
  matches made three scenarios false-pass while all seven brains were bodiless).
- [x] **RULED 2026-07-19 — Warden early-open ACCEPTED (post-game gating/order waived by
  showrunner).** The 9-condition `all_tags` early-open is documented in ENGINE_FINDINGS
  §2 as an Easy NPC caveat (never rely on wide ANDs for story-critical gates; lower to a
  band tag instead — no shipped story-critical gate uses >2). frontier_complete's
  shut-door beat removed per the ruling; expected green on its next live run.
- [ ] 🧱 **Parkour timer calibration (fire + ice)** — fully self-serve now:
  **ModMenu → Trial Timers** has 10-200s sliders for both trials that override the
  baked values LIVE (adjust → save → run again, no rebuild); the finish line prints
  RAW ELAPSED ("Finished in 47s — with 1m 13s to spare") and the timeout line prints
  elapsed too, so even a busted walk yields the number. Guidance: **fire = clean run
  × 2** (the timer IS the trial — endgame, no floor hazard), **ice = careful clean
  run × 2.5** (the hazard floor is the real challenge; timer as backstop). When the
  numbers settle, hand them to me (or leave them in the slider config) and I bake
  them into `shrine_challenges/{fire,ice}.json` so fresh installs ship them.
- [x] **RULED 2026-07-19 — shrine crystals are working AS DESIGNED**: the crystal is
  placed anywhere → raw-spawns the shrine guardian as a REAL WILD Cobblemon to fight and
  capture, all lv 70. It is NOT a noble launcher — the old "crystal-launch Java repoint"
  deferred note is retired. **Guardians RE-PICKED same day** (the first picks collided
  with the noble roster): fire→Ho-Oh, ground→Landorus, ice→Glastrier, dragon→Kyurem,
  fairy keeps Xerneas.
- **GROUND-PROBE WAVE 2 (shrine + noble-giver casts — same placeholder-Y class):** 12 of
  17 probed placements were buried/floating — fairy shrine cast authored at y=-7 (real
  floor y4-9!), Manaphy giver y33→63, ice cultist_2 68→74, dragon cultists ±4. All
  re-based + repair wave a7 shipped. The five noble monuments/givers probed OK (alpha.5's
  work held).
- **Wedge trap: ZERO faults trapped all session** across ~40 battles — today's stall
  reports were all pacing/attrition/attach, a different family than the 07-17 wedge. The
  containment stays armed.

**Content findings from the authoring wave — ALL TRIAGED + FIXED 2026-07-18 (showrunner
rulings applied; runtime re-verify rides the scenario loop):**
- [x] `wheat_trader.json` §k bleed → `§r` reset added after the scrambled fragment
- [x] `heard_wheat_pitch` now set by ALL tiers (suspicious buttons + hostile chain), not
  just the default pitch — `q.side_rate` can always light
- [x] `kalahar_pump_crew` recognized_late fight_button gated `not_tag
  defeated_sq_pump_foreman` (the pair's last fight closes both — no more prize spigot)
- [x] `cyber_door_downtown` — the reported dead-end was FALSE (ungated default catches);
  the real issue was the stale pre-Volt pitch: added a `settled` epilogue @20
- [x] **RULING: fairy trial MANDATORY** — resolve-pass latches `fairy_resolve_ready`
  (Java), Aurora's battle button gates on it, a not-ready button explains the Trial of
  Resolve. Yes, that includes the shiny. Brutal as intended. 🔍 live-verify the gate
- [x] **RULING: checkpoint softened** — all four fee functions latch `checkpoint_settled`;
  BOTH agents' mid-recognition entries gate `not_tag checkpoint_settled` (paid players get
  the civil band; LATE recognition still overrides). Sani's contraband also raised 18→25
  (same shadow bug as Haruki)
- [x] **RULING: frontier bleed fixed, names kept** — Sterling re-voiced/re-located to the
  Market; roster fiction re-voiced to one-time on all three lines; Palmer's duplicate
  beaten line re-voiced
- [x] **RULING: Nurse Rurik → Nurse Rilka** (miner keeps Rurik Deepdelve); dialog +
  character + scenario + docs updated
- [x] **RULING: grain-factor dig ambush KEPT** (thematic — late-campaign Kalahar is
  hostile ground)
- [x] survey stones 1/2 got ungated `locked` fallbacks (pre-accept rendered NO entry);
  stone 3's fallback no longer name-drops Ossa pre-meeting
- [x] `met_cyber_nurse` wired for real — q.side_signal/q.side_exchange stages now gate on
  it (the documented mm_nurse pattern; was write-only)
- [x] Marisol's report_water Q+A merged to one deterministic line (rotation showed the
  answer without the question); "Bruno students"→"the Bruno students" ×2; "Kaito
  problem"→"a problem for Kaito"
- [x] Odessa's customs crate wired into DocPropManager at the register placeholder
  [540 63 3530] — 🧱 REMAINING: a clickable container must exist AT that pos (or export
  the real crate coord)
- [x] `dev badges N` now mirrors `memory_fragment` (recognition band tags follow dev grants)
- [x] scenario_lint: escape-aware text extractors (mcfunction + Java) + win/lose/
  already_beaten lines in the corpus (warnings 221→194); safari_yards hostile sweep
  re-anchored `execute as {player} at @s` (was measuring from world spawn)
- [x] **TBCS ATTACH BUG (live-caught in the loop, REAL content bug): 7 function-dispatched
  trainer ids battled UNATTACHED** — "X is not attached to an entity", the battles could
  never fire in-world: granary_ambush, sq_deepcore_assessor (stake + decline),
  sq_headcount_wager, sq_hz_analyst (stake + decline each), sq_ladder_1/2/3. All 10
  dispatch functions now carry the armor-stand anchor idiom (summon→attach→battle,
  anchor swept in both onwin branches). Dialog-launched battles were always fine (the
  compiler emits `tbcs attach ... @s` in the preset button chain).

---

## 2026-07-16 — client test driver + E2E scenarios (docs/TESTING_TOOLKIT.md § Client driver)

**Landed + live-verified 2026-07-16:** driver + runner shipped (alpha.21, strip list §A) and
`walkup_smoke` passed 8/8 against the live stack — dialog prose dumped via reflection scan
(`DialogScreenWrapper`), `screen.click "Goodbye"` closed the dialog, framebuffer screenshot
landed in `dev/evidence/`. Field notes: interacts silently no-op beyond the ~3b server
interaction range (walk within tol ≤2.5 first); the test-zone "Fairy"/"Humanoid" bodies
have no dialog, so `find_npc` nearest can pick a dud there.

**Landed + live-verified 2026-07-16 (late): mob-grade A\* pathing + the opening chain E2E.**
`dev path <player> <x> <y> <z>` (PathProbe: GroundPathNavigation on a throwaway zombie, x8
visited-node budget, particle trail) + Walker path mode (`move.path`) + `path_to` runner step
with hop-chaining AND stuck→re-probe self-healing (mobs recompute; so do we).
`scenarios/first_steps.json` **69/69 PASS**: spawn house → Mom → A\* across town and UP the
lab's north-face switchback staircase (145 nodes/2 hops) → Totodile → Pokedex → back → Running
Shoes → THE ROAD WEST card. Lab-entrance mystery SOLVED: ground (2669,106,2888) → two flights
along z=2888 with a z≈2889–90 landing → third floor (2680,128,2888) → east walkway to the door.
Walker fidelity rules that made it survivable (each verified against a live failure): 3D node
distances (switchback flights stack in X/Z), Y-gate (never credit/skip a node still above you),
corner-cut locality ≤2b (consecutive nodes are the only guaranteed connectivity), jump only
when the node is above (flat-segment hops mount roof rims), NODE_REACH 0.9.

**Landed 2026-07-17 — the regression suite: 15 quest scenarios + scenario_lint + a content
bug wave the suite caught on its first night.** 16 quests fanned out to author agents
(digest dialog-src → author → lint); 15 authored, verified serially against the live stack.
`scripts/scenario_lint` (offline pre-flight: labels/tags/NPCs vs dialog-src, rotation-flaky
detection, structural checks; `--fix` auto-expands rotation expects) gates authoring.
Runner robustness earned live: interact_npc now A*-chases strolling NPCs (find→interact is
the canonical talk pattern); wait_screen retries once after an interact; scenario start
closes leftover dialogs; wait_chat windows are emitter-anchored; rcon auto-reconnects;
Walker gained a door reflex + persistent-collision fallback hop.

**Content bugs the suite caught (fixed):**
- **entity_tags never applied to uuid'd bodies** — Easy NPC `preset import data` ignores
  vanilla Tags on existing entities (only `import_new` spawns carry them). 8 authored tags
  across 7 characters were silently missing (hz_granary/hz_wheat_trader → Miller Walk
  UNCOMPLETABLE, deng_camp, ci_canvasser, hz_branch_manager, aya_groundskeeper…). Fixed:
  generate_npc_function surfaces preset Tags per-uuid in preset_map.json (version-bumped);
  NpcPresetRefreshManager applies them after each confirmed import.
- 🧱 **q.side_census blocked** — giver field_researcher_ume has no uuid/placement (Blossom
  Path meadow table pending set dressing); quest untestable + unplayable until placed.
- 🧱 **q.side_posters waypoint at y=64** (1915/64/2467) — placeholder-style coord ~100b from
  the poster row; the known "buried at y=64" authoring smell (waypoint only; quest passes).

**Landed 2026-07-17 (showrunner field-report wave):** Takehara gym trainers relocated to
the four greenhouses (Koji 2055.5/138/2502.6, Yuki 2055.5/151/2502.6, Shin 2055.3/151/
2428.6, Taro 2055.3/138/2428.6 — latch-placed, all four live-verified at coords); Cicada
premature descent fixed (descend trigger is now a y137-141 arena-floor box, not a 14b
sphere that reached the y151 walkway above — descend/rise/hover-dialog live-verified);
Fight or Flight move-indicator HUD hidden (`enable_move_indicator:false` in
fightorflight_visual_effect.json5) and cobblemon-battle-extras gimmick panel disabled
(`enableGimmickUsageDisplay:false` — its Gmax slot hard-references the absent Mega
Showdown mod's texture = the checkerboard) — both flipped in run/config AND shipped as
full files in mrpack/overrides/config/. Scenario rule from the 5-stacked-Mings
postmortem: tp BEFORE kill/latch-reset in preambles (kill silently no-ops in unloaded
chunks, then the latch respawns a duplicate; wheat_war preamble reordered). Existing
worlds: kill + re-latch the 4 Takehara trainers + the 2 Sango auditors.

**Relocation-repair pattern (alpha.3):** when an NPC's coords change after worlds already
latched the old body, add a wave to `install/repairs` (self-guarded by a `#repair_*` flag,
dispatched from `InstallCommand.cmdRun` + shipped in the jar so mrpack installs auto-apply):
forceload the site → kill the stale body → reset its `#amb_*` latch → it re-spawns at the
new coords on next visit. Wave a2 covers the Takehara/auditor/mew-wisp/Oasis-pump moves.
DON'T hand-write per-world kill commands in the runbook anymore — add a repair wave.

**Small bug noticed (alpha.3, low priority):** ~~NpcSight APPROACH_ONCE `fired` survives
`npcsight remove`+re-add / `reload`~~ — FIXED alpha.6 (2026-07-18): `remove`+`reset` clear
the tag-profile session cache, `reload` clears all sessions.

**Open findings:**
- [ ] 🔍 **FancyMenu × Cobblemon battle crash (POTENTIAL STREAM-BREAKER)** — FancyMenu's
  entity-spawn broadcast mixin Gson-serializes a NaN double when Cobblemon sends out a
  trainer's next Pokémon mid-battle (SwitchInstruction → sendOut → FancyMenu PacketHandler →
  `IllegalArgumentException: NaN is not a valid double value`), killing Cobblemon's battle
  scheduler — the battle HANGS with the player locked in the GUI. Reproduced on the dev
  server (removed from run/mods_disabled/; it's in dev_sync's --lean skip list anyway).
  VERIFY on the real single-player pack: if it reproduces, mid-battle trainer switches can
  freeze the stream — consider dropping/updating FancyMenu in the .mrpack.
- [x] 💻 **Bomani (auditor_a) drifts off his post** — FIXED 2026-07-17: `movement.home`
  authored for auditor_a AND auditor_b (compiles to Navigation.Home, giving the patrol
  snippet's MOVE_BACK_TO_HOME prio-3 goal a real anchor). Audit: exactly 4 of 47
  sight-gated characters combined sight + `ambient_guard_patrol` with no home — the two
  auditors (fixed), company_surveyor (unplaced; add home when placed), company_canvasser
  (uuid world body — check its builder Navigation live before overriding). Existing
  worlds: kill + re-latch the two auditors to pick up the leash.
- [ ] 🧱 **Walker-hostile pinches** (scenarios tp-hop past them, commented in-file):
  Hua Zhan market-lane entrance x≈1529–31/z2060 (stall counter blocks the lane mouth — check
  a real player can actually enter the market row); glass-tower mezzanine stair at
  (1541,94,2106). Both are also spots real players may fumble.
- [ ] 💻 **Showdown-engine wedge (was: TBCS battle stall / wheat_war battle beat)** —
  ROOT-CAUSED + RECOVERY SHIPPED 2026-07-17. The stall is an ENGINE-STATE wedge, not
  content and not per-battle RNG alone: ~1 wedge event per ~10 battles (bred on demand
  three sessions running: rounds 10/11/13/10), after which EVERY subsequent battle
  freezes — send-outs complete, then all actors sit mustChoose=false/request=true
  forever (`dev bot battlestate` reads it in one shot). The Graal CONTEXT still evals
  (`dev showdown status` → context=ALIVE) — the poison is JS-side battle state.
  Exceptions unwinding through the JS bridge are the crash-class culprit (zero
  try/catch in sendToShowdown/sendFromShowdown; the FancyMenu NaN kill is the same
  family). **RECOVERY VERIFIED LIVE:** `/cobblemon-initiative dev showdown revive` —
  (1) blinds rctapi's static battleToManager + battleStates + BATTLE_QUERY_TO_CANCEL
  by reflection (MANDATORY FIRST: rctapi's tick forceEnd on a stale id against a fresh
  context = "TypeError: Cannot read property 'write' of undefined" = SERVER CRASH,
  reproduced), (2) closes registered Cobblemon battles, (3) dispatches Cobblemon's own
  `/reloadshowdown` (context rebuild + full registry re-push — a bare close/open leaves
  the fresh JS with no species data and battles still stall, verified). Post-revive:
  two consecutive battles WON, server stable. **UPDATE alpha.6 (2026-07-18): (a) is SHIPPED
  as containment** — GraalShowdownServiceMixin wraps sendFromShowdown in try/catch; a Java
  exception mid-interpretMessage is trapped (ShowdownWedgeTrap: battleId + raw message +
  full stack in the log, one-line player toast, `dev showdown trap` reader) instead of
  unwinding into the JS loop — the engine no longer wedges AND the organic trigger
  self-captures on next occurrence (0 faults in ~10 battles so far). This also largely
  settles (b): containment ships, revive stays a dev tool. REMAINING: (c) `stopbattle` on
  a hung battle pays the TRAINER'S win branch — showrunner call.
- [ ] 💻 **Quest scenarios, next waves** — FIRST WAVE LANDED 2026-07-17 (workflow-authored,
  live-verified): `mystic_mirror` 51/51 (Fairy Mirror-Match dialog routing — declare →
  variant entry, two types; closes that gimmick's runtime-verify), `pay_probes` 107/107
  (all 3 purchase probes broke+funded — closes the §1.B 🔍), `safari_yards` 47/47 (full
  permit loop: gate/fee/balls/lure/clawback; cost THREE hardcore driver deaths to a
  buried-at-y64 site — fixed, see below), `stadium_wave` 33/33 (bracket 25 register →
  autobattle wins wave 1 → purse line → abort/second-run hygiene; NOTE a lone lv-25
  lucario LOSES wave 1 under first-legal-move autobattle — scenarios need attrition
  headroom, 2× garchomp works). `daycare_board` authored but PARKED: its picker beat is
  architecturally single-player-only (DaycareManager.triggerPicker + the screen's
  confirm both ride same-JVM singleplayer bridges — a dedicated-server client can never
  see the picker; needs an S2C payload to be e2e-able), and the last-mon refusal line
  didn't surface in chat on the harness (unresolved, low stakes). NEW DRIVER OP:
  `screen.click_at {x,y}` (coordinate click for slot-based GUIs). ~~Still to author:
  shrines/nobles cluster, Battle Frontier halls, per-town packs gyms 3–7~~ — AUTHORED
  2026-07-18 (wave 3, 33 files, lint errors:0 — see the alpha.6 section at the top;
  ~28 still need their serial live runs).
  **Field find:** the Safari Zone clearing's real surface is y≈89 — the y64 latch
  placement (noble_giver_mew_wisp) and noble mew.json center were BURIED (both fixed
  → y89; the giver needs a kill+re-latch on existing worlds). Two of the three driver
  deaths were suffocation INSIDE the terrain at y64; scenario rule: probe ground
  (feet-air + head-air + solid-below) before any tp coordinate enters a scenario.

---

## 2026-07-13 — review + fix pass (see `docs/CONTENT_REVIEW_2026-07-13.md`)

**Landed this pass (💻, compiled clean — `content_compile` errors:0, 673 presets; needs `gradle
build` for the Java/rctmod half):**
- **Cap-100 chain fixed** — Board defeats now register (rctmod board `name` → the §k nameplates,
  which also kills the full-name battle leak; `InitiativeInit` now exact-matches before `contains()`,
  so the §k-prefix / "Senior Agent" shadow bugs are gone; `board_cleared` also accepts the
  `defeated_board_*` tags as a fallback). Founder cfg displayName `C§kaaa`→`§kfounder`.
- **Trainer name drift** — `villain_team.json` displayNames synced to the authored rctmod names
  (grunts 3–11 Bello/Naoko/…/Osamu/Hui/Ivo; grunt_2 = Field Agent Dembe); kalahar_trainer_1/2 config
  displayNames un-swapped.
- **Battle-order gates** (dialog-src, compiled): every Battle Frontier brain + challenger gated on
  `royal_league_champion` (brains also on both facility challengers); Royal League E4→Champion ladder
  (`royal_elite_N` chain, Aria on badges≥10 + `defeated_villain_boss`); Board gated on
  `defeated_villain_boss`; DJ gated on `badges_gte_7` (+ the existing fields≥4).
- **Advancements** — shrine grant ids fixed (`shrines/shrine_<type>`); authored `gyms/all_badges`,
  `shrines/all_shrines`, and a hidden `villain/` branch (acting_ceo / board_cleared /
  company_overthrown, wired into `grantAdvancementForTrainer` + the board block); gym 1/2 advancement
  parents un-swapped; "Pokemon League" → "Royal League"; champion title + root desc re-voiced.
- **Java/config** — Dark Urge tiers 30/52/73 → **22/44/68** (live ladder); `/nuzlocke deathscreen` +
  `sacrifice` now perm-2; `/ca reset` cap 20 → base 15; level-cap clamp prints the real next
  requirement; sacrifice screen header neutral; whiteout now layers a tiered shadow line.
- **Datapack** — `field_N_liberated` mirror added to the compiler's `band_tags` (fixes the 7 dead
  route liberation-echoes); `fields_liberated` zero-init in `render.mcfunction` (fixes the wrong
  tracked waypoint at the Act-2 pivot); `big_root`→`black_sludge` on Blossom's Roserade.
- **Dialog mechanics** — 4 sensor `entity_tags` added (`hz_wheat_trader`/`hz_granary`/
  `hz_branch_manager`/`ci_canvasser` — unblocks Grain-In-Goods-Out + Minutes + Non-Compliance on a
  fresh install); tent-pole monologues (Founder mirror + farewell, DJ, all 4 Board) converted from
  rotating `say[]` to `open_dialog` page chains.
- **Docs/wiki** — Board first names scrubbed everywhere (docs + wiki) → "First/Second/Third/Fourth
  Seat"; wiki founder-identity spoilers scrubbed off the "safe" pages (Home/Overview/Act-I) + the
  spoiler pointer repointed at Overview/Route-Map; `LORE_BIBLE §8` cap ladder corrected; Act-III
  board team data destaled; Home version unstamped; CLAUDE quest count 26/57 → 28/69.

**Still open from the review (💻, deliberately deferred — not started):**
- [x] **Purchase pay-probes** — DONE (commit b04981f; re-verified in-repo 2026-07-17): all 3
  buttons ride balance-gated probe fns (`sidequest/deka/buy_karp`, `invitational/pay_entry`,
  `greenhouse/exit_fee`) mirroring `route/decline_*`. Remaining 🔍: one broke-player E2E each.
- [x] **Remaining monologue chains** — DONE (re-verified in-repo 2026-07-17): Cynthia
  recognise/default/after and the wheat-trader hostile/ambush band are page-chained. Only the
  wheat-trader ambient suspicious/default bands still rotate — plausibly intentional flavor
  (showrunner call whether to chain those too).
- [x] **Frontier "exhibition" no-death guard** — DONE (alpha.15). Datapack-maintained `frontier_active`
  tag (`frontier/region_tick` AABB over the plateau, self-cleaning) read by NuzlockeInit's three
  faint/flee/forfeit guards → suspends damage/removal/whiteout on the Frontier floor. Region box uses
  the shipped RCT cluster; re-survey if the atlas [4096 2965] layout is confirmed.
- **Boss roster width, gym-crew voice de-duplication, name collisions, League summit double-cast,**
  and the fuller TODO/backlog status truth-pass — all catalogued in the review doc.

**Design forks RULED (showrunner, 2026-07-13) — now wired/tracked:**
- **Wheat fields:** ten exist; **liberating any 6 opens the HQ raid.** ✅ gate wired to **6**
  (`render.mcfunction` + DJ dialog `fields_liberated ≥ 6`; HUD `/6` now matches). *Follow-up 💻:*
  extend the relief-tier / trader-escalation thresholds (still max at 4) up toward 6 if desired.
- **HQ geometry:** the raid **descends** to DJ in the **basement**; the player's old **penthouse**
  is the **top floor** → lore + a **Master Ball** pickup, and the **post-game Founder mirror** arena.
  ✅ recorded in `LORE_BIBLE §4/§5`; dev-tool slots added (`hq_basement_dj`, `hq_penthouse_lore`,
  `hq_penthouse_masterball`, `hq_penthouse_mirror`). *Remaining 🧱:* build the basement + penthouse
  floors, mark the slots (`/ca gym-mark`), then Claude wires the Master Ball chest + Founder spawn
  in the penthouse (champion-gated spawn latch).

**New 🧱→💻 (this round):**
- **Gym-trainer relocation** — the interior cast latches in a default cluster around each leader.
  New `gym-mark` POINT slots `gym_<town>_<trainer_1..4|jr_apprentice|apprentice>` (each note names
  that gym's GIMMICK) let the showrunner spread them through the room; `gym-mark export` → Claude
  folds the coords into each character's `placement` + recompiles.
- **Wiki (💻, this round):** added a **Facilities** page (Stadium/Daycare/Safari) and a **Gym
  Mechanics** page (all 10 gimmicks + the trainer-layout note); gate references corrected to 6.
  *~~Still missing~~ — verified 2026-07-18: ALL 11 town/HQ/League/Frontier pages exist in
  `wiki/` as the intended slim stubs (published + synced); flesh out as content live-verifies.*

---

**Already landed** (not tracked here): level caps, Memory Fragments + re-reader, Dark Urge
whispers, Wheat War economy core (P3), villain recognition dialogue, quest-tracker HUD,
field-mark dev tool, the **per-badge CobbleDollars shop** (`scripts/generate_shop_tiers` →
`cobbledollars_tiers/*.json`, `ShopTierManager`, `/cobblemon-initiative shop <tier>`, wired into all
10 gym leaders + DJ, seeded `badge_0` at install), the **bank re-theme** (`nether_star` 25k backing
+ `hay_block`/`wheat` commons), and the docs set (the GitHub wiki, the `publish-wiki` dev command,
and the slimmed README + UPM 2 disclaimer). See `GIT_COMMIT_MSG` / `docs/LORE_BIBLE.md` / the wiki.

---

## Gym gimmicks + cutscene rig (design program, in progress)

Per-gym signature mechanics (feasibility all verified against the real engine — see the
design workflows). Guardrails: never gate a badge behind a death-prone bonus; soft/telegraphed
ramps; opt-in forced movement/camera.

- [x] 💻 **Cutscene rig — reusable `cutscene/` subsystem (alpha.9, runtime-verified: demo plays).**
      Data-driven JSON scenes (`cutscenes/<id>.json`), `/cutscene play|stop|list` + `/cutscene-skip`.
      Body-double now renders the PLAYER's skin (alpha.10): player_double.npc.snbt preset +
      a deferred /data SkinData patch keyed on the player UUID (found by tag one tick after
      import_new; ~40-tick give-up → Steve). 🔍 verify the double appears + skin pop-in is brief.
- [x] 💻 **Cutscene authoring + story scenes (alpha.11):** scenes load from
      `config/cobblemon-initiative/cutscenes/` first (edit + `/cutscene reload` = no rebuild);
      `/cutscene record add|undo|clear|status|save <id>` captures a flown camera path as a
      playable scene. FOUR scenes shipped: `opening` (first-join Sango flyover via AutoInstall),
      `rift_intro` (Ryujin dragon reveal, summon-first so skips are safe), `blossom_reveal` +
      `victini_reveal` (wired into the transform functions). 🔍 verify each + record round-trip.
      REMAINING scene wishlist (record in-game with the new tool): HQ raid entry / Acting CEO DJ,
      Board of Directors clearout, THE FOUNDER MIRROR REVEAL (use doublePreset — the player-skin
      double IS the Founder), Royal League entrance, noble arrivals, badge-milestone stingers.
- [x] 💻 **Leader challenge intros (alpha.11):** every gym leader's Challenge button now plays the
      shared facing-relative `leader_intro` scene and STAGES THE CHALLENGER BACK 7 blocks before
      the battle opens (battle.intro_scene → compiler-emitted gym/engage_<trainer> functions, 23
      targets incl. weakened + mirror variants; skip still engages). 🔍 verify at one leader:
      scene plays at any approach angle, stage-back lands on arena floor (flat-arena assumption —
      no ground probe), battle opens from the staged spot, weakened/mirror variants route right.
- [x] 💻 **Map Frontiers no-relog (alpha.10):** build_mrpack + dev_sync bake install.json's 58
      zones into the bundled world's mapfrontiers/frontiers.dat (validated field-identical to a
      live bridge file); install run skips creation when frontiers exist (reflective count) so
      nothing duplicates. 🔍 fresh pack: zones visible on very first join, install chat says
      "already present (pre-baked)".
- [x] 💻 **Bug (Cicada) floating leader (alpha.10):** `float: true` compiler field (root
      NoGravity + EntityAttribute, post-world-merge), gym/cicada_lift (import-then-tp to y173
      perch) + gym/cicada_float tick (glide to y139 hover when a challenger is within 14 of the
      floor, rise when empty). 🔍 verify: lifts on chunk load, descends to click range, battle
      opens, rises after.
- [x] 💻 **GEN_9_MULTI 2v1 emitter (alpha.10):** battle.trainer_2/partner/defeat_tag_2/
      trainer_2_body_tag → `tbcs battle GEN_9_MULTI @s rctmod:<partner> vs rctmod:<a> rctmod:<b>`
      (exactly 2 per side — the player gets an AI partner), both defeat tags in onwin, multi
      token-shift handled, guards + schema + smoke tests. ⚠ runtime-verify winners token order
      (@1=player on win) before shipping the finale.
- [x] 💻 **ALL PER-GYM GIMMICKS BUILT (alpha.11)** — every gym now has its signature mechanic:
      Bug=floating Cicada (perch y173→y139 glide); Grass=vine walls drop per warden (leaf-filtered
      fills, hz_wall_1..4 + hz_walls_check); Fairy=Mirror Match (declare your lead → 5 illusion
      variant teams, weakened fight stays canonical); Fighting=Gauntlet (Marshal Osei + 2v1
      GEN_9_MULTI finale: Ken+Striker vs player+Sparring Second Rocky); Ground=6 click-to-poof
      mirages (ci_mirage, ambient/tick sweep); Water=Tide Clock (4-min high/low, 4 rain-variant
      teams); Electric=Stadium tease only (battle NOT gated until the Stadium ships); Ice=Whiteout
      Approach (3 PASSIVE-sight Frost Sentinels, seen=debuff, unseen=Boreas respect line);
      Dragon=THE RIFT (overworld Ender Dragon via noble/RiftDragonManager — /riftdragon,
      crystal-heal loop, dragon_slain gates the leader); Fire=Banked Coals (heat bossbar, wardens
      vent -30, heat≥60 forces full Vulcan). Compile: 229 chars → 233 presets, 0 errors.
- [ ] 🧱 **Gimmick coordinate pass (showrunner)** — grab THE WAND: `/cobblemon-initiative
      gym-mark wand` (alpha.11). Its item name always shows what you are marking; right-click a
      block once to preview, the SAME block again to confirm; it renames itself to the next of
      the 33 slots automatically (incl. the 10 stage_<gym> LEADER STAGE SPOTS — stand where the
      challenger should open the battle, facing the leader; lands in battle.stage_pos and
      overrides the generic 7-back stage). Boxes (vine walls / whiteout corridor / heat box) take two
      confirmed corners; right-click AIR to mark your own feet (fly up for the rift origin +
      crystal spots); sneak+click skips/cycles. `gym-mark list` shows progress; when done run
      `gym-mark export` and hand back {world}/data/gym_marks.json (or the log block) — Claude
      integrates the coords into the placeholders (gym/hz_wall_1..4, rift_dragon.json,
      nifl_whiteout, gaviota_tide, scorchspire_heat, the marshal/mirage/sentinel placements)
      and recompiles. Also report the vine-wall BLOCK IDS if the walls are not vine/leaves.
      (Typed fallback: `set <slot>` / `start <slot>` + `stop <slot>` still work.)
- [ ] 🔍 **Gimmick runtime-verify list**: MULTI finale (confirm @1=player on win before trusting
      prize/tags), rift dragon (spawn, circles fightOrigin not 0,0, crystals heal, teardown on
      logout, dragon_slain grant), Cicada lift+descend+battle, tide flip + rain teams, whiteout
      sight triggers, heat gauge + banked full-Vulcan gate, mirror declare→variant, wall drops,
      face-the-NPC camera turn (Mom walk-up, a DIALOG greeter, a pursue spotter run-down;
      now a SMOOTH tween as of alpha.3, server-verified — 🔍 remaining is the FEEL on a
      real client: mouse contention + reads-well-on-camera).
- [x] 💻 **Stadium BUILT (0.5.0-alpha.14)** — `stadium/` package: `/cobblemon-initiative
      stadium start <25|50|75|100>|abort|status` (perm 0); bracket level-lock via Cobblemon
      `BattleFormat.adjustLevel` (set around each dispatch, ALWAYS reset — the static leaks
      into gym/noble battles otherwise); cloned parties = attrition-free; NuzlockeInit
      faint/flee/victory guarded by `StadiumManager.isStadiumActive` (whiteouts no-op in a
      run); 5 Company exhibition waves (data/rctmod/trainers/stadium_wave_1..5, NOT in the
      TrainerConfig db), flat purses 200-1000 + 1500 completion; `stadium_challenged`
      objective increments per completed run. RUNTIME-VERIFIED same day (RCON + quick-play
      client): start→wave announce→battle OPENS (armor-stand anchor added — TBCS refuses
      unattached trainers), flee/faint guard keeps the player at full health with a 1-mon
      party, `stopbattle` no longer strands the run (IN_BATTLE registry liveness check),
      anchors swept on every endRun. **Bracket level lock FIXED + VERIFIED** (headless
      bot run: Eevee 20→25, Mailroom 50→25 both ways): TBCS's `rules` arg round-trips
      SNBT→Gson so bare booleans die (`true`→`1b`→false) — flags must be QUOTED strings
      (ENGINE_FINDINGS TBCS block); player participant dispatches as `@s` (bare names
      go through the trainer registry and miss bots). Remaining 🔍 (needs real turns):
      win a wave → purse pays once → next wave dispatches → completion bonus +
      stadium_challenged increments. **Clerk NPC AUTHORED (alpha.14):** `stadium_clerk`
      (Exhibition Registrar, Cyber City, Company admin skin) — dialog buttons run
      `stadium start 25|50|75|100` + status/abort; compiled, awaiting **placement coords
      in Cyber City** 🧱 (no `placement`/`uuid` yet = not in-world). 🧱 stadium BUILDING
      (optional — waves fight where the player stands) + Cyber gate flip
      (`stadium_challenged_gte_5`) stay TODO — Volt's tease unchanged.

---

## Noble Pokémon encounters (new subsystem, 0.5.0-alpha.6 — code-complete, build-verified)

Legends-Arceus-style encounters: Easy NPC `cobblemon_npc` body → task → body-swap to a real,
catchable wild Cobblemon. Config-driven engine in `noble/`; two `type`s — **`boss`** (combat
wear-down) and **`chase`** (friendly flee-and-tag, e.g. Mew). Shipped: 6 combat legendaries
(Groudon, Kyogre, Rayquaza, Articuno, Zapdos, Moltres) + 1 friendly (Mew). See
`docs/NOBLE_ENCOUNTERS.md`.

- [x] 💻 Engine: attack primitives, element themes, ambient themes, flyer mechanic, arena
      ring, stagger boss bar, phase-2 `BattleBuilder.pve` swap + capture/victory matching.
- [x] 💻 Content: 6 encounter JSONs + `cobblemon_npc` presets + advancements; `/noble` + `/noble-abort`.
- [x] 💻 **Trio no-spawn FIXED (alpha.5, live-verified 18/18)** — "Groudon/Kyogre/
      Rayquaza won't spawn" was a monument↔arena coordinate desync (alpha.4 moved the
      arenas, not the monuments → body imported into unloaded chunks → 4s manifest
      timeout), NOT AllTheMons. Monuments moved to their arenas (kyogre buoy →
      Mystic Island shore, rayquaza altar → Sky Ring deck, groudon stone → the REAL
      south rim — terrain probe found the caldera is a lava lake at y295, rim y296-312;
      old y110/y70 site was ~200b inside the mountain; arena recentered [3790,313,3813]).
      Repair wave a5 re-latches existing worlds + sweeps leaked phase-1 bodies. Phase-2
      guard added: unregistered battleSpecies now fails loudly instead of silently
      substituting a random mon (protects datapack-less worlds — groudon/kyogre are
      implemented:false in base Cobblemon).
- [ ] 🧱 Noble arena THEMED SITES (set dressing only) — the centers are AUTHORED since
      alpha.20 (all 7 encounter JSONs carry real coords; the [0,0,0]→player-position
      fallback never triggers on shipped data). Remaining is dressing the sites if the
      raw terrain disappoints — a screenshot survey of all 7 can pre-grade them.
      **Groudon south rim is the priority**: the shelf is raw slope and the arena-ring
      edge nears the lava lake on N/NE — wants a built fight platform / rim guard rail
      before the endgame set-piece ships.
- [ ] 🧱 Per-noble balance pass: Phase-1 `max_health` + `staggerAtHealthFraction`, body
      `Root.Scale`, attack damage/cooldowns, Phase-2 `battleSpecies` level (catchable under the cap ladder).
- [ ] 🧱 Decide the unlock gate/trigger per noble (story-flag-gated `/noble start`, an Easy NPC
      dialog button — add `noble` to the `EasyNpcSecurityConfig` allowlist if dialog-launched — or a shrine-crystal item).
- [ ] 🔍 Runtime verify (per `docs/NOBLE_ENCOUNTERS.md`): boss-sized model, bossbar tracks
      health, native chase+melee, themed attacks telegraph+land, ring teleport-back, stagger→catchable
      battle, rewards/flag/advancement, clean teardown on abort/logout/death, no block damage.
      Confirm the Easy NPC `import_new` spawn + `Root.Scale` + `max_health` render as intended.
- [ ] 💻 (future) Pseudo-legendaries (Tyranitar/Garchomp/Dragonite/Metagross/Salamence/Hydreigon)
      — data-only once the showrunner picks the set + species presets.
- [ ] 💻 (future) More friendly/task nobles — the `chase` type is the pattern; new task types
      (hide-and-seek, fetch, riddle) add a `tick<Type>` branch, all reusing the Phase-2 catch.
- [x] 💻 **Epic-ness pass, build-order steps 1–5** (design: `docs/NOBLE_EPIC_DESIGN.md`,
      2026-07-09) — cry-gate fix + species cries everywhere; per-attack/element sound
      language + telegraph metronome + white-hot final flash; melee hit-confirm, directional
      hurt tilt, hardcore heartbeat, edge curtain, talking boss bar; rage bands (roar/nova/
      cadence/`minRageTier`-held moves); overture (fake weather/time via `NobleSkyFx`, horn);
      Phase-1 music loops + Phase-2 battle themes (`species_additions`); `Phase.STAGGERED`
      collapse cinematics (default / Moltres "rebirth" / Mew "gotcha"); `min_perfect_ivs=6`
      prizes + capture-vs-KO reward split. 20 adversarial-review findings fixed (incl.
      pre-existing: inverted whirlpool pull, undodgeable tracking impacts, 4× chase flee,
      orphanable Phase-2 legendary on save-and-quit mid-battle).
- [ ] 🔍 Runtime-verify the epic-ness pass (cries audible, rage bands, stagger scripts,
      music duck/handoff, sky restore on every exit, capture/KO reward split, Mew chase
      catchable in open field).
- [ ] 📐 **Mini-noble "Asset Recovery Program"** (design doc Part 2): two tiers (Wardens
      gyms 1-6 / Executive Assets 7-10+), deterministic IV/EV/nature/item prize strings,
      Field Surveyor gating, flee bond, Salamence roamer, Zorua chase. **Blocked on the
      §2.7 showrunner rulings** (prize levels at-cap vs cap-minus-2, site conflicts with
      roadmap 16, Articuno/Moltres roster status, Safari Zone ownership).

---

## 0. GAME PLAN — the road to 1.0.0 (sequenced 2026-07-02)

The critical path is **verify → author → wire → balance → ship**. Code is largely ahead
of the map now: most remaining 💻 work is blocked on 🧱 authoring or 🔍 verification,
so the phases alternate between us. Sections below (§1-2) hold the item-level detail.

### Phase 0 — Verification gate 🔍 (first session at the new location; ~1 sitting)
The whole session's code is compile-verified but not runtime-verified. One `run-client`
session, in this order (each failure comes back to Claude as a bug report):
1. Boot to menu on the real GPU (the Kotlin fix landed; this confirms it end-to-end).
2. Fresh test world → check the log for **datapack load errors** (liberation/granary/
   wheat_trader functions + macros all parse?).
3. `/cobblemon-initiative install run` → §1.C checklist top-to-bottom (HUD, fragments,
   Dark Urge, economy beats, **shop-tier GATE test**, level caps).
4. New systems: `/cobblemon-initiative shop badge_3` → granary lockstep fires (log line);
   `scoreboard players set @s fields_liberated 4` → wheat-trader hostile dialog + battle;
   `function cobblemon_initiative:liberation/free_field {field:"test_1"}` → −6 idx +
   HUD wheat line; a FARM zone with `mobsSpawn`/`activeWhen*` behaves occupied→liberated.
5. While the client is open: capture **Sodium + BSL settings** → `mrpack/overrides/`
   (§1.F) and run the **`cobbledollars give @s` smoke-test** (§1.B).

### Phase 0.4 — Round 12c follow-ups (2026-07-06, v0.5.0-alpha.1 — minor bump per showrunner)

Round 12c fixed the two systemic engine bugs (TBCS `rctmod:` registry keys; action gates
need the doubled `ConditionDataSet` key — see ENGINE_FINDINGS §2/§3) plus spawn-Y, skins,
derby retune, Lucian handoff, clinic sidebar, tower gating, and the narrative Tier-1 pass.
Left open:

- [ ] 🔍 **0.5.0-alpha.1 re-verify** — SMOKETEST R1-R11 (battles starting AT ALL is the linchpin;
  the runbook's "WIN a dialog battle" canary has never been checked in any build).
- [ ] 💻 **Narrative Tier 2/3** (showrunner call, plan in `docs/NARRATIVE_AUDIT_2026-07-06.md`):
  checkpoint → "Resident Verification Drive" front; gate the Sango square occupation;
  ungated townsfolk defaults (Dakarai/Kele/Fara/Marlow); gate `sango_lore` founder pages;
  **Ume decision** (faceless client vs branded temptation via `payout_company` — pick one);
  Takehara naming diet; `grunt_recognition` per-front variants.
- [x] 💻 **SimpleTMs balance + move economy** (2026-07-07): curated
  `mrpack/overrides/config/simpletms/main.json` (drops off, rare TR-only in-battle,
  no blank crafting, TMs finite 8-use unrepairable); 10 gym-leader signature TMs;
  Machine Counter Mika (Takehara badge-gated TMs/TRs; Torn-Label Tadashi was cut
  2026-07-07 per showrunner — Sango is savanna). Remaining polish 🧱: verify Mika's
  **placement in-world** (1904/113/2606 Takehara mart — latch spawns once, finalize
  before shipping a world) + badge-tiered ENTRIES not locked buttons; **skin dress
  pass** (defaults to Steve).
- [x] 💻 **DAYCARE CENTER BUILT (0.5.0-alpha.14)** — `daycare/` package per the spec
  (API jar-verified first): 2 slots/player custody (JSON in world dir, write-through so
  a crash loses ≤1 drip interval); deposit via party-picker screen (sacrifice-split
  reuse, multi-select ≤2, never the last mon); pen stand-ins = `clone(true)` real
  PokemonEntity hardened UNBATTLEABLE+uncatchable+invulnerable+persistent, tagged
  `ci_daycare_standin`, custody-JSON-is-truth reconcile respawns/sweeps them lazily;
  XP drip (40xp/1200t default) **self-clamped** via `getExperienceToLevel(cap)` — the
  global clamps key off getOwnerPlayer() which is NULL for custody mons (trap recorded);
  withdraw fee 100+100/level via the pay-probe (`daycare/pickup_fee.mcfunction`, gated
  on store RESULT), `party.add` has built-in PC fallback; fee-paid-but-both-full →
  refund + stays boarded. `/cobblemon-initiative daycare deposit|withdraw <slot>|status`
  perm 0. 🔍 runtime-verify: deposit picker opens + boards, stand-in renders + is
  unbattleable/uncatchable, XP stops at the cap, withdraw fee + PC fallback, custody
  survives relog. **Keeper NPC AUTHORED (alpha.14):** `daycare_keeper` (Gaviota Port —
  MOVED from Sango per showrunner) — dialog buttons run `daycare deposit` (closes →
  party-picker), `withdraw 1|2`, `status`; compiled, awaiting **who the keeper is
  (display_name + placement coords or a reused-body uuid) + real PEN coords** in
  `config/cobblemon-initiative-daycare.json` (0,0,0=unset → stand-ins spawn at the
  depositor) 🧱. **ModMenu category DONE (alpha.14)** — Daycare/Safari/Stadium all tunable in-game (+ area-overlay content mode). Keeper skin still Steve until the dress pass.
- [ ] 🧱💻 **Hua Zhan pass** (tester notes round 4 — "a lot of work needed"): DONE in
  0.6.0-alpha.1 — **Groundskeeper Aya → Leader Blossom transform** (the a9ed3a64 body reveals
  as the gym-2 leader after all four garden wardens are beaten + a talk; Victor→Victini
  body-swap via `hua_zhan/aya_transform` + ambient/tick guard, skin single/hua_zhan_leader,
  RCT gym fight rctmod:hua_zhan_leader). Gym 2 uses the transform gate INSTEAD of the
  Takehara 1/2/4 weakening ladder (no jr/apprentice bodies; the four warden statues stay;
  Wei's pilgrimage + seals unchanged). REMAINING: recast **Mei Lin as the Hua Zhan nurse**;
  ~~Tau + wheat sellers deal in a custom scrip item~~ **DONE (alpha.14):** Company Wheat
  Scrip = renamed paper + `custom_data{ci_scrip:1b}` in the `trade_wheat_trader` snippet;
  jar-verified Easy NPC rejects plain paper (component-exact ItemCost predicate). granary
  wheat-canon leak; Jun's master-plan line; survey wagon unmark; minutes approach_warn;
  `sq_hz_analyst` displayName rename (sync team file).
- [x] 💻 **Act-2/3 trainer casting (0.5.0-alpha.12)** — all 38 missing/empty teams
  authored + jar-validated (18 missing: grunts 3-11, 3 admins, DJ, board ×4, Founder;
  20 empty: royal_champion + elite_1-4, 5 shrine leaders, 10 cultists). Ladder-true
  (DJ ace 64, E4 78-81, Cynthia 83, Board 86-87, Founder 100×6 static shadow team).
  🔍 runtime-verify one battle per group. Follow-ups: **runtime Founder party-mirror**
  (regenerate villain_final_boss from the live party; overrides the static file) 💻;
  **{element}_shrine_cultist_3/4** — 10 ids referenced as shrine-leader prereqs with no
  team file and no NPC (author them or trim the leader prereqs) 💻; dragon_hydra_1..3
  remain uncast. **Battle Frontier (24 ids) — CAST + authored (alpha.15):** all 24 characters
  upgraded to the opt-in model (gym_leader, `frontier_<facility>_cleared` tags, prizes, brain
  on_win → hall_cleared), + Registrar/Caretaker economy+lore NPCs, `frontier/` functions,
  safe-exhibition guard, and the four-holder quest line (slots 82-85). RCT teams already shipped.
- [ ] 🔍 **Existing-world repair** (any world created ≤ alpha.17): kill + latch-reset the six
  placement bodies (tower ×4, Old Deng, Granny Yun — commands in the runbook), then re-tag
  Deng/Yun (`deng_old`/`deng_granny`/`deng_camp`) or the homecoming walk no-ops.

### Phase 0.5 — Round 9–10 follow-ups (2026-07-04, v0.4.3-alpha.8)

**WORKFLOW CHANGE 🧱→💻:** NPCs no longer require in-world placement + UUID recording —
a `placement: {x,y,z}` field in the character JSON auto-spawns them once per world
(compiler-generated proximity latches; see ENGINE_FINDINGS §3/§4). In-world placement
is still fine (uuid wins over placement); use whichever is easier per NPC. 30 latches
already live (10 companions + 20 authored-coordinate NPCs incl. the roof agents,
Harvest Road villains, Deng camp, garden stations).

- [ ] 🧱 **Coordinates needed** (compile warns until authored; give Claude coords or
  place bodies — since 0.5.0 placing these ALSO lights their quest-tracker waypoints
  automatically on recompile, incl. the 8 currently-beamless stages: Ume/census, Tetsu/
  night pay, checkpoint tent/memo, the four board members): hz_greenhouse_docent,
  apiarist_sumi, courier_mio, field_researcher_ume, forewoman_tetsu, company_surveyor,
  doc_ledger_barrel, doc_portrait_crate, notice_post_1–3, sq_kyc_agent,
  villain_grunt_2 + villain_grunt_field_agent (checkpoint tent pair).
- [x] 🧱💻 **Gym interior casting** (0.6.0-alpha.1): all gyms now have interior cast —
  gyms 3–10 got trainer_1–4 + jr_apprentice + apprentice bodies (latch-placed around the
  leader, trainer_textures skins, dialog + weakening); Hua Zhan keeps its four warden
  statues and adds the groundskeeper→Leader Blossom transform instead of jr/apprentice.
- [ ] 💻 **Sight arming after latch spawns**: latch-spawned villains get random uuids —
  the authored `npcsight add <uuid>` registrations (route pair, checkpoint pair,
  yield officer/analyst) need a manual pass per world, or a future auto-register hook.
- [x] 💻 **Cap ladder re-space + gym retune** (round 10e, alpha.10): start cap 15
  (gates pre-gym-1 evos), ladder 15/22/30/37/44/50/56/62/68/74/80/100; leader ace =
  entry-cap **+2** (aces 17/24/32/39/46/52/58/64/70/76), roster shifted in step.
  levelcaps.json + ProgressionConfig + CLAUDE.md table + docs updated. See
  ENGINE_FINDINGS §5.
- [x] 💻 **Gyms 3–10 roster COMPLETION** (0.6.0-alpha.1): 24 missing team files
  ({town}_{trainer_3,trainer_4,jr_apprentice} × 8) authored under the cap; PLUS the
  24 _weak variants (leader/apprentice/jr, IV+EV=0) with the per-gym `<gym>_tower`
  1/2/4 weakening; PLUS all six interior bodies per gym (skins + placement + dialog);
  PLUS leader migration (authored name/skin repaint onto the placed CSV body) and the
  3 TM refinements. See GIT_COMMIT_MSG.
- [ ] 🔍 Existing-world one-time repairs (runbook §J): magikarp respawn, stale
  takehara defeat tags, `/rctmod player set series cobblemon-initiative`.
- [ ] 💻 **PRUNING PASS — after the alpha.13 smoke test** (showrunner 2026-07-05):
  once smoke results are in (so we know what's actually live), sweep the **git-tracked**
  tree for stale / unused / needs-updating — code AND files. Scope: dead classes/methods
  + unreferenced functions/commands; orphaned data (presets/dialogs/loot/trainer configs
  not referenced by any character or function); superseded approaches; docs drifted from
  code; build/config cruft. NOT the gitignored maps/cache/dist/build. This is DISCOVERY,
  broader than §2 (the pre-identified dev-tool strip) — good fit for a workflow (finders
  → verify each candidate is truly unused → report before deleting). **Candidates already
  spotted** (statuses re-verified 2026-07-18): `dialog-src/dialog/sq_lucian_deliveries.json`
  DELETED alpha.6 (triple-confirmed orphan merge file; marlow comment repointed);
  `sq_perf_review_guide.json` already retired 2026-07-06 (lives in dialog/takehara_guide.json);
  `level.dat.bak` KEEP — build_mrpack deliberately bakes + ships it as vanilla's corruption
  fallback; JEI `.disabled` support in build_mrpack (shelved — still a candidate);
  `docs/QUEST_OPTIONS_TOWNS_1-2.md` KEEP until Phase 1 item 4 (its Status section is the
  live placement list); the 28 empty/missing `{}` trainer teams (future-act, NOT stale —
  pre-wired, leave).

### Phase 1 — Map authoring sprint 🧱 (parallelizable with Phase 2 wiring)
Author in batches; each batch unblocks Claude wiring the same day:
1. **Zones** via `zone-mapper` (draw towns/routes/shrines/HQ/frontier; FARM zones get
   their "Liberation field id"). Export → install.json. *(§1.A zones)*
2. **Wheat fields** via `field-mark` (6 set-piece) → send Claude the JSON. *(§1.A)*
3. **NPC placement waves**: wheat traders + Granary keeper (record UUID!) → archivist +
   civilians (Nalia is placed; rumor mill/propaganda use the scrubbing register) →
   villains act-by-act (grunts → management → DJ → Board/Founder). *(§1.A)*
4. **Act 1 side-quest cast** (Sango / Blossom Path / Takehara / Gym 1) — the full
   `dialog-src` layer is authored + compiled (106 chars, 0 errors); every side-quest NPC
   has a preset in `default_preset/humanoid/`. Remaining is pure placement + UUID mapping
   (record each UUID → re-run `content_compile` → `update_npc_presets`), then `npcsight add`
   for the sight NPCs (surveyor `surveyor` tag, canvasser `ci_canvasser` tag, perf-review
   sentries `takehara_sentry`, checkpoint agents `checkpoint_agent`). See
   `docs/QUEST_OPTIONS_TOWNS_1-2.md` Status for the selected list.
   - [x] **The Incomplete File / The Lane Looks After Its Own / Notice of Non-Compliance**
     finished 2026-07-03 (the 8 orphaned dialog trees + `personnel_file/*` and
     `noncompliance/*` functions); all sidequest `load`/`tick` entrypoints registered in
     the `#minecraft:load` / `#minecraft:tick` tags.
   - [x] **Off the Record** (Sango) built 2026-07-03 — `sidequest/off_record/*` stealth
     loop (obs_count + off_record_blown, auditor-sight tick), Lucian offer/debrief + Oma
     (errand 1) + Sarii (errand 2) entries; errand 3 + conclusion use the pre-authored
     auditor dialog. Clean-sweep bonus is a heal_ball + praise line (advancement deferred).
   - [x] **Out of Office** (Genji, Takehara) built 2026-07-03 — `fisherman_genji` + dialog
     (8 string → cobblemon:poke_rod + 300 CD via `sidequest/genji/*`) + opt-in 200 CD wager
     (trainer sq_genji_wager, loss_fee 200).
   - [ ] **Fair Market Value** — shelved (no spec; cut for now).
   - [ ] 🔍 **Batch smoke-tests** these side quests lean on: `givepokemonother`
     (Kele Magikarp, trades, gifts), `cobbledollars give/remove @s` inside `execute as`,
     the `can_see_player` stealth branches (surveyor/canvasser), `cobblemon:poke_rod` +
     fossil item ids, renamed `writable_book`/`paper` component shape, **the world-merge
     import** (one NPC: preset import keeps builder skin + our dialog + renames), and the
     **zone-mapper NPC overlay** rendering in a browser.
5. **Act 1 beat 2 — Harvest Road + Hua Zhan City (gym 2, the wheat reveal)** — BUILT
   2026-07-03 (11 showrunner-selected quests + both route backbones + gym ladder
   plumbing; 143 chars compile clean). Design menu: `docs/QUEST_OPTIONS_HARVEST_HUAZHAN.md`;
   placement pass: `docs/PLACEMENT_BEAT2.md` (16 import-only castings + 16 new bodies);
   test script: `docs/VERIFICATION_RUNBOOK.md`. Skipped by selection: Boundary
   Adjustments, the dojo, Pest Control, Bloom Festival, Two Botanies. Guidebook
   (wiki/Guidebook-Act-I.md) updated through Hua Zhan.

### Phase 2 — Wiring on authoring output 💻 (Claude; fast turnaround per batch)
- Field guards: per-field `command` rewards firing `liberation/free_field {field:"<id>"}`;
  FARM zones in install.json get `activeWhenObjective: field_freed` per field id.
- Granary UUID recorded → re-run `generate_granary_tiers` (fills `apply_<tier>` fns).
- Fold zone exports into install.json; regenerate presets/functions; `install run` cycle.

### Phase 3 — Balance + polish (joint; needs Phase 0-2 done)
- **Open balance decisions** (Claude needs answers, §1.B): field pushback −6 × ~6 fields?
  liberation swap a shop tier or stay narrative? liberation gate/soften the HQ raid?
- Tune granary pool/prices, ambush thresholds, instability tug-of-war from playtest feel.
- Optional narrative systems: Founder name de-obfuscation as the Board falls (next
  unblocked 💻 item), exchange boards, reserved farm plots; Option C (`farmzone/`) go/no-go.

### Phase 4 — Release pass (§2; mostly 💻 after authoring bakes)
- Strip dev tooling (field-mark → zone-trace → npc-map) as each authoring stream closes.
- Debug-command audit, docs/wiki sync + `publish-wiki`, version bump **1.0.0**,
  `build-mrpack --with-map`, and a clean-launcher **install test** of the final pack.

---

## 1. BUILD — still to land

### A. In-world authoring 🧱 (blocks the systems in §B)
- [x] **Zones** — the zone-mapper export (dev/zones.json) is baked into `install.json`: **58 zones** — 13 towns, **all 5 shrines**, **7 Battle Frontier facilities** (flavor subtitles), 19 route segments named in journey order ("Route N" subtitles; user-named "Road to Royal League"; Route 16 = "Frontier Causeway"), **10 farms gated `field_freed`/`farm_N`**, 3 landmarks, canonical HQ preserved. Array priority-sorted so nested zones (facilities/shrines) announce over their surroundings. *Optional:* route gaps 4/6/9/11 if intended. 🔍 fresh `install run`. → *unblocks removing Zone Trace (§2)*
- [ ] **Villain NPC placement** — every villain in `villain_team.json` is at `[0,0,0]`. Place each with real coords, composed from the battle + recognition-dialogue snippets:
  - [ ] 11 grunts (Field Agent → Elite Agent), gym-gated on routes
  - [ ] 3 management (Regional Manager Shade, Senior Director Vex, COO Noir)
  - [ ] Acting CEO DJ at HQ `[1590 51 1028]`
  - [ ] 4 Board members + The Founder (post-Royal-League, The Boardroom)
- [ ] **Wheat fields** — zones are DONE (10 farms in install.json, gated on `field_freed`/`farm_1`..`farm_10` — those ids are now canonical). **As shipped (2026-07-04) only `farm_1` is wired to `liberation/free_field` — `fields_liberated` maxes at 1**, so the HQ-raid gate (4), the wheat-trader escalation (2/4), the relief shop tiers (2/4), and the granary ambush (4) are all unreachable. **This is the single biggest Act-1→Act-2 blocker.** **PLACED (0.5.0-alpha.14):** the field-guard cast is the 23 `villain_{site_manager,yield_officer}_N` (farm staff, one pair per farm at its zone center) + `villain_route_agent_N` (route patrols) — authored alpha.13 with dialog+battles but at placeholder Y=64 (buried in stone); repositioned to real surface Y per-farm/route, Highfield pair de-stacked, in-world verified grounded+separated. *DONE (re-verified in-repo 2026-07-17):* every guard's battle onwin already fires `liberation/free_field {field:"farm_N"}` in the compiled presets; derived mapping 1=Firstfurrow 2=Mirebloom 3=Westwind 4=Dryrow 5=Crossroads 6=Fenceline 7=Coldfurrow 8=Frostfallow 9=Highfield 10=Ashloam. Remaining 🔍: clone the wheat_war scenario per farm (fight beats blocked on the TBCS battle-stall item above).
- [ ] 🧱 **Place the compiled-but-unplaced NPCs** — the dead-letter checkpoint agent (`checkpoint_agent` tag), the Per My Last Memo courier, and the Head Count census wagon: compiled presets exist for all three, but there is no UUID mapping / import line yet.
- [ ] **Wheat-trader NPCs** — place (trade→recognize→ambush) from `wheat_trader_gate` + `trade_wheat_trader` + `dialog_wheat_pitch`
- [ ] **Granary trader NPC** — Company Inc. member selling items **for wheat**. **Infrastructure landed:**
  - [x] `granary_keeper` character + 3-tier recognition dialog (default → suspicious ≥2 fields → hostile ≥4, hostile trade arms `granary_ambush_armed`); compiled via content_compile.
  - [x] Badge-tiered offers + **wheat bell curve**: `scripts/granary_tiers/master_granary.json` (+ `generate_granary_tiers`) bakes 12 tier presets (`granary_keeper_<tier>.npc.snbt`) — wheat cost = base × (1+(56−idx)×0.012), e.g. rare_candy 20→12(peak)→16 wheat. No restocks (stock baked, no reset). Item IDs validated against the Cobblemon 1.7.3 jar.
  - [x] Lockstep retier: `ShopTierManager.applyTier` also fires `function cobblemon_initiative:granary/apply_<tier>` (stubs until UUIDs recorded).
  - [ ] 🧱 Place Granary NPC(s), map UUID → `humanoid/granary_keeper` in npc_presets.json, re-run `generate_granary_tiers` (fills apply functions), `install run`
  - [x] Post-trade ambush poller + ambush battles — `granary/tick` fires the one-shot post-trade battle (`granary_ambush` L43-44); wheat traders battle directly from hostile dialog (`wheat_trader_ambush` L38-39). Trainers in villain_team.json, jar-validated.
  - [ ] Tune the item pool / prices / ambush thresholds after an in-game pass 🔍
- [ ] **Memory re-reader (Archivist) NPC** per town (`dialog_memory_rereader`)
- [ ] **Reserved farm plots** — griefing-safe plots for liberated-field safe-farm conversion
- [ ] *(Optional)* civilian NPCs: **Mom (Nalia) full arc authored + compiled** (first meeting → warming ≥4 badges → worry post-HQ → homecoming post-Founder; never learns the truth — LORE_BIBLE §2); her UUID is already mapped. Remaining: rumor mill + Company propaganda NPCs (scrubbing register lines are written and waiting in `dialog-src/registers/scrubbing.json`)
- [ ] *(Optional)* per-town exchange-board sign/lectern (economy instability display)

### B. Remaining systems 💻
- [ ] **STANDALONE: capture the CobbleDollars bank re-theme into the mod** 💻 — the
  `nether_star` 25k backing + `hay_block`/`wheat` commons re-theme (listed above as
  "already landed") exists **nowhere in the repo or the mrpack** (verified 2026-07-04:
  the `cobbledollars_tiers/*.json` resources carry only `defaultShop`; no bank file
  anywhere). It apparently lives only in the live instance's
  `config/cobbledollars/bank.json` — **lost on every fresh install, pack or standalone**.
  Export that file from the instance into `src/main/resources/cobbledollars_tiers/`
  (or a `cobbledollars_bank.json` resource) and have `ShopTierManager.applyTier` /
  `install run` seed it the same way the shop is seeded (then `cobbledollars reload`).
  Same standalone rule as the Easy NPC security.cfg patcher (see docs/ENGINE_FINDINGS §3).
- [ ] **P4 — Field liberation** (needs marked coords): guard trainers, liberate → restore wheat price + unlock safe-farm + advance HQ gate; relog-safe one-way latches; `liberation/fields.json`
  - [x] **Option A core** — `liberation/free_field`(+`_apply`): −6 `cd_instability` (floor 0, tunable) + `fields_liberated`++ + per-field `field_freed` latch + actionbar beat. *Remaining for A:* wire a field-guard `command` reward `execute as {player} run function cobblemon_initiative:liberation/free_field {field:"<id>"}` (blocked on marked field coords 🧱)
  - [x] **Option B** — conditional safe-zones: `SafeZone.activeWhenObjective`/`activeWhenHolder`/`activeWhenMin` (+ `isZoneActive` + server-aware `isInSafeZone`/`getSafeZoneAt`/`getAnnouncedZoneAt`, threaded into MobSpawnMixin + Dark Urge + zone-announce). Occupied field = hostile until its `field_freed` latch trips → then safe farmland (world-data, relog-safe). compileJava verified. Zone-mapper exposes the gate (FARM "Liberation field id" → activeWhen*). *Remaining:* set it per field when coords are marked.
  - [x] ~~Granary `sell_wheat` datapack~~ — **dropped (design confirmed):** the default economy is CobbleDollars + its built-in bank (handles wheat→CD), and wheat trading is the Easy NPC "wheat traders" (paper). No custom CD sell-back / Granary datapack.
  - [x] `wheat_war_active` flag — set by the first field liberation (`free_field_apply` adds the player tag; `quest/render` shows the wheat-fields HUD line)
  - [x] Wire `wheat_trader/load` + `wheat_trader/tick` into the function tags (+ new `liberation/load`)
  - [x] **Balance decisions (resolved 2026-07-02):** pushback stays **−6/field**; liberation **swaps tiers** — every 2 fields upgrades the active shop+granary tier to a pre-baked relief catalog (`<tier>_relief1/2`, −12 idx each; `ShopTierManager.resolveRelief` reads `fields_liberated` live; `shop refresh` fired by `free_field_apply`; gym rewards unchanged); HQ raid is **hard-gated on 4 liberated fields** (DJ's battle entry gated, "monopoly holds" refusal below it, quest HUD shows "Starve the monopoly" until 4). Thresholds (2/level, −12, gate=4) tunable 🔍.
  - [ ] *(Option C, deferred)* stateful `farmzone/` subsystem (soil/growth/patrols/HQ-difficulty)
- [ ] **P5 — Wheat traders full wiring** — _done: the trade→recognize→ambush dialogue, **Company Wheat Scrip currency** (alpha.14 — renamed paper + `custom_data{ci_scrip:1b}`, jar-verified Easy NPC rejects plain paper via the component-exact ItemCost predicate; was plain `minecraft:paper`), 2/4 thresholds, **ambush trainers** (`wheat_trader_ambush` L38-39 farm team / `granary_ambush` L43-44, in villain_team.json, species+items jar-validated), **wheat-trader hostile tier now offers the battle** ("Stand and fight" → tbcs vs wheat_trader_ambush), and the **granary post-trade poller** (`granary/tick`: hostile trade arms `granary_ambush_armed`, ~15s countdown → "Asset located. Initiating retrieval." → battle, one-shot via defeated_granary_ambush)._ Remaining: in-world placement (Easy NPC traders) 🧱 + in-game verify the tbcs battle/onwin path 🔍. (`wheat_ambush_armed` is now superseded — wheat traders battle directly from dialog; objective left declared, unused.)
- [x] ~~Smoke-test `cobbledollars add @s`~~ — **moot (2026-07-03):** CobbleDollars 2.0.0-Beta-5.1 has **no `add` subcommand at all** (jar-verified grammar: pay/query/give/remove/set/reload/leaderboard). Every `cobbledollars add` in the repo (pay_macro, granary ambush, all 143 battle-prize onwin strings) was a dead command — all replaced with `cobbledollars give <targets> <amount>` (selector-first, accepts @s under execute-as). Verify in-game that a battle prize actually lands (runbook Round 5 canary #5).
- [x] **Cast the 20 empty + author the 18 missing trainer teams (0.5.0-alpha.12)** — see
  the Phase 0.4 entry; content_compile team warnings 28 → 0. Remaining casting debt:
  ~~shrine cultist_3/4 (10)~~ trimmed alpha.6 (dead ids, prereqs inert), ~~dragon_hydra_1..3~~
  authored alpha.6 (+ the stage dispatcher — gauntlet e2e-verified), battle_frontier done (alpha.15).
- [ ] **villain_grunt_2 checkpoint dialog ladder** 💻 (minor polish): the `default` entry is dead code, and `contraband` is overshadowed at 3+ badges.
- [x] **RCT trainer data cleanup** 💻 — RESOLVED (2026-07-17): `validate_trainers.py` runs
  clean over all 255 shipped team files (0 errors; the named bad entries — lorelei/terry/zeph
  mega_showdown items, skadi ninetales_alola — are gone since the alpha.10/0.6.0 retunes).
  The remaining boot-log "Model validation failure" lines (e.g. `elite_four_bruno_0051`
  `mega_showdown:rusted_sword`) come from rctmod's ~1500 BUILT-IN trainers, which never
  spawn in this pack (globalSpawnChance=0) — log noise; allowlist in the log baseline.
- [x] **Founder reveal (redesigned per decision)** — the Founder's nameplate stays fully `§k`-obfuscated all run (`§kfounder`); each Board defeat fires `reveal/board_fell` (4 oblique beats that circle the name); the name is only spoken at the mirror's defeat — `reveal/founder_defeated` renders **the defeating player's own name** live via selector ("The name on the chair was always ⟨you⟩"). No name baked anywhere. *(Propaganda-decay register: done — `dialog-src/registers/scrubbing.json`.)*

### C. Verify in-game 🔍 (can't be tested without the mod loader)
- [ ] **PLAYER_TAG dialog conditions** fire correctly (re-reader, wheat-trader tiers, grunt/management recognition). Bytecode-settled 2026-07-04: Easy NPC 6.25 **ignores the Operation field** (`contains()` only), so every gate is EQUALS on a tag — "not_tag" gates ride the derived inverse tags `no_<X>`, maintained each tick by `function/dialog/band_tags.mcfunction` (auto-generated; also `no_defeated_<id>` for all 95 shipped trainers). Remaining 🔍 is the in-game confirm.
- [ ] **Quest HUD** renders (**sidebar-only** since 2026-07-04 — the top "Objective" boss bar was removed; `quest/load` runs `bossbar remove cobblemon_initiative:objective` to clear old worlds; numbers hidden; the main line advances on gym defeat; side lines ride `q.side_*` slots; `/ca quest hide` clears). See `docs/VERIFICATION_RUNBOOK.md` **Round 7 canaries**.
- [ ] **Memory fragment** title fires once per leader; no re-fire on relog
- [ ] **Dark Urge** whisper fires outside safe zones, silent inside
- [ ] **Economy beats**: gyms 1-7 tick `cd_instability` up; Acting CEO → "CURRENCY STABILIZED"
- [ ] **Shop-tier smoke-test (GATE)**: edit `config/cobbledollars/default_shop.json`, `/cobbledollars reload` with the shop GUI open → prices change live? If a Pokémart merchant does NOT update, it uses a **custom per-entity shop** (re-provision it to the default shop, or the swap won't reach it). Then confirm `/cobblemon-initiative shop badge_3` swaps + reloads, and a gym-leader defeat advances the tier.
- [ ] **Level caps** applied correctly (30 → 85 → 100)

### D. Per-run hardcore setup 🧱 (every fresh world — see `docs/HARDCORE_RUNBOOK.md`)
- [ ] Wipe/empty `config/cobblemon-initiative.json` `safeZones`, then `/cobblemon-initiative install run`
- [ ] Confirm hardcore flag + relog

### E. Docs & wiki 💻/🧱
- [x] **Publish the wiki** — DONE (verified 2026-07-18: GitHub wiki live, synced 2026-07-17, byte-identical to local `wiki/`, 33 pages).
- [ ] After editing `wiki/` pages, re-run `publish-wiki` to keep the live wiki in sync.

### F. Zone-mapper & dev environment 💻
- [x] Zone-mapper: offline (vendored OpenLayers UMD + polygon-clipping), FARM zone type, per-zone mob-spawning control (`mobsSpawn`), priority-based overlap clipping, route→corridor buffer + retroactive width-adjust
- [x] mrpack: the 2 new resource packs default-on; BSL shader on by default (`config/iris.properties`); video maxed for the Sodium/Iris stack (graphicsMode kept Fancy — Fabulous breaks Iris)
- [~] **Sodium + BSL settings seeded** (2026-07-03), pending in-game verify 🔍: `config/sodium-options.json` sets Quality → Weather + Leaves = **Fancy**; `shaderpacks/BSL_v10.1.3.zip.txt` sets Material → Advanced Materials = **On** (`ADVANCED_MATERIALS=true`). These are best-effort schema (Sodium 0.6.13 + Reese's/Sodium-Extra key names, and the BSL define name) — confirm they take on a `run-client` session with a display, then capture the full generated files to overwrite these stubs.
- [x] **run-client fixed** — added `fabric-language-kotlin:1.13.12+kotlin.2.4.0` (Cobblemon's Kotlin runtime/adapter) to `build.gradle.kts`; it was crashing at launch with `NoClassDefFoundError: kotlin/jvm/internal/Intrinsics`. Verified headless: 69 mods + Cobblemon + **all cobblemon-initiative subsystems init**; only the software-GL window step fails in the sandbox. → 🔍 confirm full boot to menu on a real GPU
- **Known limitation: `gradle runServer` can't host the full companion stack.** Cobblemon's production jar does intermediary-name reflection (`class_2960`) that can't resolve in a mojmap dev server, and JourneyMap additionally wants its separate API jar. night-config 3.8.1 was added as a dev runtime dep (fixed one layer). Consequence: datapack runtime verification (liberation/granary functions, shop→granary lockstep) is 🔍 in-game via run-client, not headless.
- [ ] *(mod)* `mobsSpawn` per-zone flag — verify in-game a `Spawn freely` FARM/ROUTE zone actually keeps spawns while a town suppresses 🔍

---

## 2. REMOVE BEFORE 1.0.0 — dev-only tooling

**CONSOLIDATED 2026-07-11:** all surviving dev tooling now lives in ONE package
(`devtools/`) behind ONE entrypoint (`DevToolsInit`) — GymMark wand (gym-gimmick
coordinate pass), NPC Noter stick + `pos` capture + `smoke` checklist, and the whole
`/ca dev` subtree (goto/badges/grant/kit + `team`/`stage` test harness + `place` guided
placement walk). Command surface unchanged. Already deleted outright (superseded):
`zonetrace/` (browser zone-mapper won — 58 zones baked), `fieldmark/`'s field-mark half
(farm polygons canonical in install.json), and the `function/dev/npc_tour_*` datapack
tour (`dev place` replaced it). `NpcPresetRefreshManager.init()` already moved to
`InitiativeInit` (idempotent guard added), so the npcmap dev classes are freely
deletable.

### The strip (do in order; each step compiles on its own)
- [ ] **Wait-gates:** gym-mark coordinate pass exported + integrated; placement walk
  exported + integrated; smoke rounds done (the smoke checklist is the release-verify
  loop — strip LAST).
- [ ] Delete `src/main/java/.../devtools/` (all 13 classes incl. DevWandTool — the
  Producer's Tool; STRIP GATE: land + unhold the tool once first so the flight/invuln
  grant revokes — plus the 2026-07-12 bot harness `DevBotCommand`/`AutoBattler`, which
  have no resources/mixins/deps of their own) + remove the `devtools.DevToolsInit` entrypoint from
  `fabric.mod.json`; delete `mixin/DevWandInputMixin.java` + its line in
  `cobblemon-initiative.mixins.json`; drop the `fabric-message-api-v1` dep line in
  `build.gradle.kts` (only the tool's chat-note capture uses it).
- [ ] Delete the two bundled resources `data/cobblemon_initiative/devtest/
  {counter_teams,placement_plan}.json`; the `smoketest_items.json` compiler stage in
  `content_compile` can stay (harmless resource) or go.
- [ ] npcmap dev classes: delete `npcmap/{NpcMapCommand,NpcMapEntry,NpcMapStorage,
  NpcMapInit}.java` + the `npcmap.NpcMapInit` entrypoint (KEEP `NpcPresetRefreshManager`
  — shipping, already init'd from `InitiativeInit`). Remove the LEGACY npc-map replay
  block in `InstallCommand.cmdRun()` + the NPC count line in `cmdCheck()` (the
  armed-refresh block stays).
- [ ] `cutscene/CutsceneRecorder.java` + the `/cutscene record` subtree in
  `CutsceneCommands` (the rig + playback SHIP; only the recorder is dev) — once the
  scene wishlist (HQ raid, Board clearout, Founder mirror, Royal League entrance) is
  recorded.
- [ ] Keep-or-strip decision: `shrine <id> test <name>` (fairy shrine test runner in
  `CobblemonInitiativeCommands`) and the `shrine <id> path record` authoring subtree.
- [ ] Doc scrub: `wiki/Commands.md` + `docs/ARCHITECTURE_OVERVIEW.md` dev-tool sections
  (both carry "removed at 1.0.0" flags; already updated for the consolidation).
  (Architecture pages moved wiki → docs/ 2026-07-13 — wiki is now strictly player-facing.)
- [ ] Bump `build.gradle.kts` version → `1.0.0`.
- **NOTE:** the `fabric-events-interaction-v0` dep in build.gradle.kts STAYS — shipping
  code uses `UseBlockCallback` (InitiativeInit, LootChestManager, DocPropManager).

---

*`install/`, `mapfrontiers/`, `npcmap/NpcPresetRefreshManager` and `install.json`
(with baked vertex data) all STAY in the shipped mod.*

---

## 3. FUTURE / SHOWRUNNER DESIGN IDEAS (not on the 1.0 critical path)

- [x] 💻 **More achievements + "global achievements" (stream-aware, backfill-silent)** (showrunner
  2026-07-20). Two linked pieces:
  - **More achievements.** Grow the set well past the current ~49 `advancement/*.json`. Fill the
    thin arcs — more per-town/gym beats, per-shrine + all-shrines already exist so extend to
    Frontier halls (per-Brain + all-halls), nobles (have per-noble; add roster milestones),
    Wheat War (fields-liberated tiers), the Minecraft-flavor/mirror set, quest-count and
    dex-count milestones, Nuzlocke "survived N gyms deathless" style beats. Each is a normal
    vanilla advancement JSON (icon = hyphen-namespace `cobblemon-initiative:<item>`, wired via
    `grantAdvancementForTrainer` / the criteria in `advancement/CobblemonInitiativeCriteria` +
    `TrainerDefeatedCriterion`, granted state mirrored in `PlayerProgress.earnedAchievements`).
  - **Global achievements = auto-grant + display split.** The problem this solves: when a new
    achievement batch ships **mid-run** (a live streamed hardcore save is always already deep in),
    the client would toast-spam every achievement the player *retroactively* qualifies for. Design:
    - **Backfill silently.** On join / content-version bump, run a backfill pass that auto-grants
      every "global" achievement the player's **existing** `PlayerProgress` already satisfies —
      with the client toast + chat **suppressed** (advancement `display.show_toast:false` +
      `announce_to_chat:false`, matching how `root.json` already ships, OR grant via
      `AdvancementProgress` without the visual). These are the "non-new" ones: earned by past
      progress, so no toast. (Reuse the `modVersion` latch pattern from AutoInstall / the repairs
      waves so a given batch backfills exactly once per world.)
    - **Auto-grant global ones.** "Global" = achievements keyed on global/derived state (badge
      count, dex count, fields liberated, board cleared, …) rather than a single live event — the
      backfill grants them automatically the moment the state qualifies, no player action needed.
    - **Stream toast for genuinely NEW unlocks.** When an achievement is earned **live** (during
      play, not a backfill), fire a dedicated **stream-facing alert toast** — route it through the
      existing `streamsync/StreamSyncManager` overlay bus (it already ships a "toast + snapshot"
      event pair to the OBS overlay via `markDirty`), so the OBS overlay shows a New-Achievement
      alert on stream. Optionally still show the normal client toast for live earns; the key
      distinction is **backfill = silent, live-earn = stream alert.**
  - Open questions for the showrunner: (a) do live earns ALSO show the vanilla client toast, or
    stream-overlay-only? (b) is "global" a per-advancement flag (e.g. a custom tag/field) or a
    naming convention/folder? (c) should the backfill announce a single summary line in chat
    ("Caught up N achievements") or be fully silent?
  - **SHIPPED (2026-07-20).** New `achievement/` package: `AchievementManager` holds a code
    manifest of 25 derived-state ("global") achievements, each an `(id, advancementPath, title,
    icon, Predicate<AchievementContext>)`. `buildContext()` snapshots the derived reads once per
    pass (badges, shrines, frontier halls, noble roster, fields liberated, dex-caught, run
    deaths, quests done). **Silent backfill on join** (grant-once, mirrored into
    `PlayerProgress.earnedAchievements`, per-world `modVersion` batch latch at
    `data/cobblemon_initiative_achievements.json`); **live re-eval** every 40 ticks + right after
    a trainer defeat + on quest completion, gated on a per-player `liveReady` set so a backfill
    can never toast. New advancement JSONs (all `show_toast:false` + `announce_to_chat:false`, so
    the grant itself is silent and the live/silent split lives entirely in Java): `frontier/`
    (8 halls + `all_halls`), `nobles/roster_{5,10,15}`, `wheat/fields_{1,3,6}`,
    `dex/caught_{25,50,100,151}`, `nuzlocke/{flawless_5,flawless_10,deathless_champion}`,
    `quests/quests_{10,30,60}`.
  - **Showrunner rulings resolving the open questions:** (a) live earns pop the **vanilla client
    toast** (new S2C `AchievementToastPayload` → client builds the real `AdvancementToast` from
    the synced display, so `show_toast:false` never blocks it) **plus** the streamsync overlay
    alert (`StreamSyncEvents.achievementEarned`) **plus** the `§6[Achievement Unlocked]` chat
    line. (b) "global" = the **code manifest** (a predicate is required for auto-grant anyway),
    folders are display-only. (c) backfill emits **one** "the ledger has been reconciled — N …"
    line, but ONLY on a genuine batch-version bump — silent on ordinary rejoins.
  - **Supporting infra:** `PlayerProgress` gained a persisted `pokemonLost` counter (incremented
    at the single loss choke point `StreamSyncEvents.pokemonLost`, so it is tracked even with the
    overlay off — the "deathless" tiers need a truthful zero; on a deep save it is **seeded up to
    the streamsync stats total on join** so flawless never false-positives) and a persisted
    `completedQuests` set (fed by a completion-transition detector in `QuestTrackManager` — a
    `ci_quest` holder that drops out of the active set = one completion, idempotent + first-pass
    seeded). `compileJava` green.
  - REMAINING (optional polish): the OBS overlay's New-Achievement alert widget lives in the
    antlers `cobblemon-overlay` service (separate repo) — the mod now emits the `achievement`
    event on the streamsync bus; the overlay UI to render it is not in this repo. MC-flavor /
    mirror advancement set (self-triggering `inventory_changed` + reward functions) not extended
    here — the derived-state arcs were the meat.

- [x] 💻 **Safari Zone BUILT (0.5.0-alpha.14) — "The Baiting Yards"** (concept menu in
  docs/SAFARI_ZONE_CONCEPTS.md; showrunner selected the lure-game direction, badge-3
  opening, Acquisition = end-of-visit chat vote, NO death-lifeline ever). `safari/`
  package: 1,500 CD Day Permit (pay-probe) → 20 marked Preserve-issue balls + 15:00
  clock; scatter typed bait (5 kiosk items → 57-species typed tables, lv 25-35) →
  suspense → 1-3 spawns → ~75s window; warm spots; battle creation CANCELLED in-session
  (hardcore-safe by construction); exit = clawback + catch ledger for the chat vote;
  lifetime 10/25 milestones. HEADLESS-VERIFIED: gate, fee, balls, honey scatter →
  Cherubi/Butterfree at band, cancel guard, clawback + sweep. 🔍 remaining (human):
  throw a ball at a lure (capture ledger + warm-spot bump + Nuzlocke PC routing),
  bossbar/timer feel, eject teleport, milestone packs. **Concierge NPC AUTHORED
  (alpha.14):** `safari_concierge` (Preserve Intake Concierge, Company admin skin) —
  one NPC does intake + bait kiosk: buttons run `safari enter` (closes), `status`,
  `exit`, and `safari bait <type>` for all 5 baits; compiled, awaiting **placement
  coords AT THE ZONE ENTRANCE** 🧱 (enter starts the clock where the player stands — no
  teleport-in). ~~OPEN 💻: bait is currently FREE~~ — CHARGED since alpha.6 (60/250 CD via
  `safari/bait_fee` pay-probe, ModMenu-tunable; e2e 56/56 incl. broke path). 🧱 REMAINING: paddock
  dressing on the shipped zone polygon, post-DJ liberation flip (designed, not built).

- [ ] 🧱💻 **Legends-style legendary boss battles** (showrunner idea 2026-07-05 — I'll
  scope this later). Pokémon Legends: Arceus "noble/frenzied" flavour: an aggressive
  **Easy NPC Cobblemon** (`easy_npc:cobblemon_npc`, COBBLEMON_ENTITY renderer, so it
  shows the actual legendary model) that ATTACKS the player in real time; on defeat it
  **spawns the real catchable/battleable legendary** (a genuine Cobblemon Pokémon) so
  the player then does a normal capture/battle. Two-phase encounter: dodge/fight the
  boss NPC → earn the actual mon.
  - Fits the 5 elemental shrine challenges (already gate legendaries) and/or standalone
    world encounters; boss NPC placed via the `placement:{x,y,z}` latch system.
  - CONFIRMED engine hooks (jar-verified — see `docs/EASY_NPC_REFERENCE.md`):
    - COBBLEMON_ENTITY render is **species-only** (no aspects/forms) — fine for most
      legendaries; a regional/form legendary needs the render-only **clone-species**
      recipe (ENGINE_FINDINGS §2, the growlithe_hisui pattern).
    - AGGRESSION EXISTS: objective `ATTACK_PLAYER` (targeting) + a melee/ranged goal
      (`MELEE_ATTACK` / `ZOMBIE_ATTACK` / `BOW_ATTACK` / `CROSSBOW_ATTACK` / `GUN_ATTACK`,
      with SpeedModifier / AttackInterval / AttackRadius) makes the boss actually chase
      and hit the player. `EntityAttribute` toggles hittability (IsAttackableByPlayers,
      IsInvulnerable, IsKnockbackResistant); `BaseAttributes` sets HP/attack/etc.
    - ON-DEFEAT SPAWN: Easy NPC HAS an **`ON_DEATH` action event** (ActionEventSet) — put
      the real-legendary spawn there (`spawnpokemon`/`pokespawn`/`givepokemon`,
      as-player/ExecAsUser per the command-lowering rules). Latch once-per-legendary
      (respawn only via admin/shrine reset).
  - STILL TO VERIFY when scoped: whether the COBBLEMON_NPC entity's AI actually issues
    melee damage as configured (attack objectives are registered for PathfinderMob — the
    cobblemon_npc is one — but confirm damage output in-world), and tune HP/attack to the
    badge-era cap. Everything else is documented in EASY_NPC_REFERENCE.md.
