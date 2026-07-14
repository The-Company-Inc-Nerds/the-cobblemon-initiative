# Content Review — 2026-07-13 (alpha.14)

Full-corpus review of lore, mainline, quests, dialog, side systems, trainers, wiki, and
player-facing strings. Method: a 12-domain multi-agent sweep, each finding adversarially
re-verified against the actual tree; the two engine-heavy domains (`trainers_battles`,
`player_strings`) were hand-verified for this doc after the automated verifier hit a rate
limit. Counts below are **verified** findings only; refuted claims are listed at the end so
they are not re-raised.

This is an internal (spoiler-full) doc, per the `docs/` vs `wiki/` split.

---

## TL;DR

The build is **much further along than the tracking docs admit** — the "single biggest
Act-1→Act-2 blocker" (farm liberation) is fully wired, the Frontier is fully cast and placed,
gyms 3–10 are fully fielded, and every boss team is populated. The remaining risk is no longer
authoring; it is **(a) a handful of genuinely game-breaking or on-air-dangerous bugs, (b) a
large layer of "built-but-not-wired" seams that break the moment the build order reaches them,
and (c) two design forks and a stale canon table that must be closed before the next content
batch compiles.**

The prose corpus (669 character files, ~2,988 lines) is release-quality; its problems are
**systemic, not local** — four mechanical/structural issues (monologue rotation, a copy-pasted
recognition couplet, name collisions, a double-cast summit) touch the whole cast at once.

---

## P0 — Fix before the next stream (game-breaking or on-air-dangerous)

1. **The cap-100 chain is broken — a level-100 Founder against a level-85-capped player.**
   `InitiativeInit` records a trainer defeat by name-matching the battle loser against the
   config. The four Board members battle as `"Board Member the First Seat"` (rctmod `name`) but their
   config carries `name: "The Company, Inc. Board Member"` / `displayName: "M§kaaaaaaaaa"` — none
   of the three match clauses can ever fire, so no Board defeat is recorded, `board_cleared` is
   never granted, and cap 100 (levelcaps order 12) never unlocks. The Founder's *dialog* gate
   (the `defeated_board_*` tags, set independently by TBCS onwin) **does** open, so the player is
   sent into a six-mon flat-100 mirror while hard-capped at 85, in hardcore. *(Verified: the
   Founder's own `name` matches, so only the Board link is broken — it is the climactic one.)*
   **Fix:** bridge `board_cleared` off the four `defeated_board_*` tags in `reveal/board_fell`
   (function-run commands execute at perm 2), or rename the four rctmod `name` fields to the
   unique obfuscated nameplates so `onTrainerDefeated` fires naturally. Then verify the full
   chain: 4 Board wins → "Cap raised to 100" → Founder fight legal.

2. **Battle Frontier is live at badge 0 with level-90–100 teams and no gate — and four NPCs
   promise a rental safety net that does not exist under hardcore Nuzlocke.** All 24 frontier
   bodies latch-spawn for any player within 40 blocks (`@a`, no progression tag), and Cave Warden
   Selene offers a six-mon L100 fight (incl. Darkrai) gated only on "not yet beaten." Meanwhile
   `tower_challenger_1` and ~others say *"House rules: my team, not yours. Nothing you love dies
   on this floor."* — but TBCS fights the player's **real** party and Nuzlocke faint/removal
   applies everywhere (no zone suspension). This is the single most dangerous text in the mod.
   **Fix:** gate every brain's battle entry on `royal_league_champion` (+ challenger-ladder
   gates), and either generalize the Stadium's proven cloned-party/`isStadiumActive` guard to a
   frontier "exhibition" flag (makes the fiction true — strongly preferred) or rewrite the four
   promise lines to print real stakes. Ship an existing-world repair for already-latched bodies.

3. **Royal League has zero enforced order — Cynthia is challengeable without fighting a single
   Elite.** All five battle buttons are ungated; beating Cynthia fires `royal_league_champion` →
   cap 85 → the whole Board/Founder/Frontier chain. (The five fight bodies also have no
   placement/uuid, so the League can't spawn at all yet.) The gate table is designed in
   `docs/roadmap/13_royal_league.md §4a` but the QUESTS unit was never authored (the 08→10
   numbering hole), and the backlog wrongly calls the League "(shipped)."
   **Fix:** author `09_royal_league.md` (or promote roadmap 13 §§3–4), add the five placements,
   and wire the `defeated_royal_elite_N` inverse-band-tag gate ladder so it is a gauntlet.

4. **`/nuzlocke deathscreen` and `/nuzlocke sacrifice` are perm-0 kill switches with debug text.**
   Only `reload` is gated (`.requires(hasPermission(2))`); both of the others call `player.kill()`
   with no `.requires`, so in single-player they are one-keystroke run-enders that tab-complete
   under `/nuzlocke`, and print "Triggering death screen…" on stream. Not on the dev-strip list.
   **Fix:** add `.requires(source -> source.hasPermission(2))` to both and drop the "Triggering…"
   voice.

5. **The run's tent-pole monologues play as one random paragraph.** Per the documented engine
   contract (`ENGINE_FINDINGS.md:58` — a `say[]` list is a *rotation*, not a monologue), and
   confirmed against `content_compile:721` (no page-chain conversion), the climactic multi-page
   speeches are authored as sequences inside a single `say[]`: **DJ**, all four **Board** members,
   the **Founder** mirror (4 pages — incl. the single best-written paragraph in the arc), the
   Founder's post-defeat farewell, **Cynthia**'s recognition, and the **wheat-trader** ambush.
   Even the gym guide's hardcore-death tutorial is a 1-in-3 roll. On stream, each delivers one
   random page of its monologue.
   **Fix:** convert each to the `open_dialog` page-chain pattern already used correctly by
   `sq_beekeeper_tomo`'s seal walk and Lucian's lore menu. Pure dialog-src restructuring; zero
   engine risk; these files are mostly awaiting placement so the cost is nil now.

6. **The published wiki spoils "you are The Founder" on the pages it calls spoiler-safe.**
   `Quests-Main-Story.md:8` routes blind viewers to the Act I/II/III guidebooks; `Guidebook-Act-I`
   (no banner) names the whisper as "the voice of the founder you used to be," `Guidebook-Overview`
   says "the founder you were" 44 lines after promising "never the reveal itself," `Home.md`'s
   landing tagline says "amnesiac-founder mystery," and `Guidebook-Act-III:113` prints the Board's
   four real §k-hidden names. One curious chatter clicking "the safe page" mid-run spoils the
   mirror for everyone.
   **Fix:** repoint the "spoiler-safe" link at Overview + Route Map only; scrub founder-identity
   language from Home/Overview/Act-I/Route-Map; keep the Board's real names in `docs/` only (call
   them Seats One–Four on the wiki).

7. **Every wiki boss table is 1–3 versions stale — dangerous for hardcore prep.** Viewers and the
   player prep against published numbers, and nearly every table is wrong: DJ listed at ~72 (live
   59–64), Board "4 mons 83–85" (live six-mon 81–87), Founder "88–90 incl. Mewtwo/Darkrai" (live
   six flat-100, no Mewtwo/Darkrai), League/shrine-cult teams called "empty placeholders" (all
   real), gyms 3–10 "field 4 of 7 with no bodies placed" (fully fielded + placed).
   **Fix:** regenerate every battle table from `data/rctmod/trainers/*.json` at publish time so it
   can't drift again, and re-audit every `[!NOTE]`/`[!WARNING]` content-status callout.

---

## Cross-cutting themes (each touches many files at once)

- **The tracking docs lag the tree in both directions.** TODO.md and the backlog call finished
  work "the biggest blocker" and call shipped content "uncast." Every planning session that starts
  from them inherits stale priorities. A **truth-pass over TODO.md, the backlog, CLAUDE.md
  (`26 quests/57 stages` → `28/69`), and CONTENT_ROADMAP.md** is the cheapest high-leverage edit
  in this review. (Details in *Positive surprises* + *Consolidation*.)

- **"Built but not wired" is the dominant failure mode.** Placement latches, dialog gates, and
  progression bridges that were authored on different days don't yet connect: `field_N_liberated`
  mirror tags (7 route NPCs gate on tags nothing sets), the badge ceremony (built, called by
  nothing), four sensor entity-tags (live only in the streamer's instance), the shrine ladder
  (prereqs/trials never invoked), and the Board name-match. None are authoring problems; all are
  one-wire fixes — but each is invisible until a player walks into it on stream.

- **The re-space of the cap ladder was never fully propagated.** The old ladder still lives in
  `LORE_BIBLE §8`, in `NuzlockeConfig`'s Dark Urge tiers (30/52/73 = old g1/g4/g8), and in three
  QUESTS docs that compute their level bands one gym ahead. The Dark Urge drift alone pushes the
  shadow-self escalation 1–2 gyms late and has already been "documented as intended" on the wiki.

- **6 vs 10 wheat fields is unreconciled across every layer.** The greenhouse card says ten, Jun
  says six, the ceremony counts n/6, the HUD clamps at 6, the wiki says six total, and the only
  real gate is 4. Needs one canon ruling (recommend: 10 exist, any 6 = campaign goal, 4 = raid
  gate) written into LORE_BIBLE and anchored in-fiction via Jun.

- **A handful of copy-paste patterns flatten otherwise-strong prose.** The act-2 recognition
  couplet is verbatim in 18–32 files; 8/10 gym crews run one madlib skeleton; the name generator
  collided (4 Lunas, 3 Taros, two Embers in one gym) and already broke a breadcrumb line.

---

## By area

### Lore & canon consistency
- **Founder-naming rule needs a written ruling.** `LORE_BIBLE §9` forbids naming the protagonist
  as the Founder before Act 3, but the shared `grunt_recognition` "late" line (*"It is you. The
  founder…"*, lowered to `badges_gte_7`, priority 30) is verbatim in 18 shipped characters and
  fires the moment gym 7 falls — still Act 1, before frag_7's "you signed this charter" reveal.
  Kalahar's doc already treats enemy naming as intended "memo-paranoia." Rule it once in §9
  ("enemies may name him as paranoia from tier X; narration/fragments/allies never confirm"),
  then either rewrite the register line to panic-denial or gate the affirming variant on the real
  `defeated_villain_boss` tag, and re-sync the 18 copies.
- **`LORE_BIBLE §8` still carries the retired cap ladder** (30/38/…/85). It is the upstream cause
  of the Dark Urge drift and the scout had to warn reviewers not to cite it. Fix to the live
  ladder; add a footer pointing at CLAUDE.md as canon.
- **Two competing identity registers** (`memory_fragments.json`, `dark_urge.json`) are consumed by
  nothing and have fully diverged from the shipped `frag_*` functions and `NuzlockeConfig` pools.
  The tracked re-reader NPC will be built from them and recite fragments the player never saw.
  Overwrite with the live text (make them the re-reader's source) or mark SUPERSEDED.
- **Smaller:** an "emerald coin" line in Ryujin (`akira_flamecrest`) resurrects the retired
  emerald-currency canon; two named Auroras (fairy shrine leader vs Nifl gym trainer); no
  chronology anchors in LORE_BIBLE (shipped lines are inventing dates — "ten years of stability"
  vs "fifteen years I verified" is already tight); the "75% payout floor" the wiki/docs quote is
  unreachable (real floor ~86–94%).

### Mainline arc (Act 2 / Act 3)
- **Two rival Act-2 raid designs.** `QUESTS/10` is an upward climb (lobby→throne, gate 4, new
  receptionist, no keycard); `roadmap/09` carries a 2026-07-06 "GEOMETRY IS CANON" ruling for a
  basement **descent** (keycard elevator down to DJ at y51 — which is what the shipped `[1590 51
  1028]` waypoint points at — grunt-gated floors, penthouse memory tease above). TODO adds a third
  grunt model. The backlog builds Batch 4 from doc 10 alone. **Record one superseding ruling
  before Batch 4** (the descent matches the shipped waypoint/zones/Act-3 tower reuse); port doc
  10's good additions onto the winner; pick one grunt-placement model (grunts 3–7's mid-band
  recognition only fires if they're meetable on routes at badges 3–6).
- **Close the Board scattered-vs-seated fork as SEATED.** The weight of record already is: roadmap
  14's baked ruling, every shipped Board/Founder dialog ("you climbed the whole tower"), the
  shipped `board_fell` ("something shifts in its chair"), and the published wiki all say seated.
  Leaving it open blocks Batch 6. Rule seated, keep the boardroom coords, salvage doc 11's pointer
  NPCs as executive-floor texture.
- **Two act-order gates are missing at runtime.** DJ's battle entry gates only on `fields≥4` (not
  `badges≥7`); the Board gates only on `champion` (not DJ's defeat) despite the wiki + TrainerConfig
  documenting both. The moment farm wiring lands, a gym-5–6 player can trigger DJ at cap 50–56
  against his ace 64 (hardcore whiteout) and skip the whole HQ beat. Both fixes are one added band
  tag each.
- **The penthouse "this was my life" tease is on no build path** — roadmap 09's strongest
  pre-Act-3 emotional plant (the chair DJ was keeping warm) isn't in doc 10 or TODO. Carry it into
  the winning raid unit or it silently ceases to exist.
- **Founder identity string drift:** TrainerConfig `displayName "C§kaaa"` bakes a literal initial
  (every other layer uses `§kfounder`) and risks an unrecorded Founder defeat if the battle actor
  reports the nameplate. Set it to `§kfounder`. Also delete the stale TODO "Founder name
  de-obfuscation as the Board falls" item — it contradicts the shipped reveal ruling.

### Quests — early (Sango → Deepcore)
Early game is in excellent shape (Sango is dense, onboarding teaches the systems, villain plot is
seeded on schedule). The risks are downstream:
- **Unit docs 01/02 predate the alpha.13 ambient pass** and now collide with it: a third "Rurik"
  in Deepcore, a Mystic "Pokémon Center" the ambient canon says doesn't exist, a duplicated
  failing-Company-clerk bit, and overlapping register slots. **Reconcile against the live area
  folders before building** — map quest roles onto existing ambient bodies, rename duplicates,
  assign distinct slots.
- **Quest tracking is undiscoverable in-game** — the `]` keybind is taught only on the wiki and
  inside `track status` output. Add a one-time Java hint when the sidebar first gains a side line.
- Quest density falls off a cliff after Hua Zhan (0 town quests at gyms 3–4). Known; see below.

### Quests — mid (Gaviota → Ryujin + HQ)
- **`field_N_liberated` liberation echoes are dead** — 7 placed route NPCs gate on tags nothing
  produces (`free_field_apply` sets only the `field_freed` scoreboard). Ship the 9-line mirror
  tick in `band_tags` so the world talks backward as designed.
- **farm_3 has two competing liberators** — the shipped R7 site-manager (Sora, at the exact
  Westwind anchor) and the Gaviota doc's Sable ambush. Rule the R7 guard the owner; re-gate
  Gaviota's rumor on field state.
- **Three town docs compute their band one gym ahead** (entry = the town's own unlock, not the
  previous gym's), misprinting wager stakes and authorizing one forced fight up to +6 over cap.
  Re-base 03/05/06 (entry 44/56/62) before their RCT files are authored.
- **Kalahar's marquee ≥4-fields ambush shares a trainer/tag with Hua Zhan's** and is a lv-38 team
  in a cap-50 band — it will silently pre-clear and skip the town's loudest recognition beat. Pull
  the doc-04 split forward (2 JSON edits + 1 team).
- **The keycard marquee that hands Cyber to the raid is owned by neither QUESTS doc** (it lives in
  roadmap 08); `q.side_door`'s `hq_keycard` gate dangles forever. Assign one owner + add
  `defeated_villain_boss` as a self-moot terminator.
- **Ryujin's whole town layer is dark pre-raid** (4 of 5 NPCs gate their default on
  `defeated_villain_boss`) — a plausible gym-first player finds unresponsive NPCs at the hottest
  recognition town. Give each an ungated pre-raid fallback entry.
- **Paid-heal is missing in 3 of 4 mid towns** (Gaviota/Cyber/Ryujin) — the hardcore lifeline and
  the most-repeated economy receipt both vanish for gyms 5–8. Cheapest: add `service:{kind:heal}`
  to `mei_stormscale`.

### Quests — late / endgame (Nifl → Founder, Frontier, nobles)
- Nifl (07) and Scorchspire (08) are strong designed-not-compiled docs, marred by a wrong cap
  baseline in 08 (`Entry cap 80` — Nifl unlocks 74) and the wrong-75%-payout copy.
- **The 85→100 window's risk is inverted:** the fights mostly exist (5 Board + 24 Frontier +
  Moltres/Groudon + Fire Shrine), but the connective tissue that makes them an arc (registrar
  economy, pointer NPCs, Four Signatures, frontier sidebar, prize fields — frontier wins pay 0 CD)
  is 0% built, and the backlog schedules that tissue **last** while the fights are already live and
  ungated. Re-sequence: pull frontier gating + economy forward into/after Batch 6.
- **`15_battle_frontier.md` + Batch 8 describe a frontier that no longer exists** — executing them
  double-casts 24 NPCs and wires a HUD to tags never set. Rewrite from CREATE to EXTEND.
- the Third Seat's Board-hunt site is anchored to the atlas-intent Factory (4000s) but the frontier actually
  ships at the x≈3800 cluster — resolve the coord fork in both docs together.
- Nifl's paid-decline buttons are strictly-dominated dead options (a free stand-down sits on the
  same menu); differentiate or cut.
- **Post-story is bare** (one null-target "Hunt the Ender Dragon" line) — acceptable given the
  seed-procedural stronghold, but worth a small "the world reacts to the Company's fall" pass.
  *(Note: the "Mom homecoming is missing" claim was refuted — it exists, gated on
  `villain_final_boss`.)*

### Dialog — voice/quality
- **The act-2 recognition couplet is one line copy-pasted onto ~27 villains, 18 hard-inlined**
  where the tracked shared-dialog fix won't reach — the wheat-war grind delivers the identical
  recognition beat dozens of times. Write a 4–6 variant matrix keyed to front × tier, then
  de-inline the 18 copies. (The `win_line`s prove distinct voices already exist per NPC.)
- **8/10 gym crews run one madlib skeleton** (same apprentice speech, rung banter, idioms with the
  element swapped). One distinguishing pass per crew; Takehara's crew is the exemplar bar.
- **Name collisions** already broke a breadcrumb (two Embers in Scorchspire, one male/one female —
  `scorchspire_trainer_3`'s "mind *her* firepower" points at the male junior). Plus 4 Lunas, 3
  Taros, two Rens who reference each other, Elite-Four names pre-collided by ambient NPCs. Rename
  pass in priority order (same-town dupes first; keep the lampshaded pairs).
- **The League summit is double-cast** — `royal_town/` and `ironwave/` both ship a full Hall of
  Champions cast, and two different NPCs each claim to have personally erased the founder's plate,
  plus two "Lucian Scrollkeeper"s. One-owner pass on the summit's sacred beats.
- **Amend, don't neutralize:** `LORE_BIBLE §143`'s absolute "civilians never recognise him"
  contradicts the *excellent* Cyber record-keeper beats (Nora Vault, Rico Ledger) — they recognise
  the *artifact*, not the public face. Reword the canon clause; add them to the PRESERVE list.

### Dialog — mechanical integrity
The core is strong (register ↔ compiled waypoints perfectly in sync at 28/69, no `has_item`/
`kill @2`/`@s`-in-Cmds anti-patterns, decline-fee pay-probe rail is *built*). The seams:
- **The tracker resolves the wrong main-quest waypoint at the Act-2 pivot** — `fields_liberated`
  is never zero-initialized, so a badge-7 player with 0 fields fails the score check and the
  tracked `]` waypoint points at the Ryujin gym while the sidebar says "Liberate wheat fields."
  One-line `render.mcfunction` init fix.
- **Four sensor entity-tags exist only in the streamer's live instance** (`hz_wheat_trader`,
  `hz_granary`, `hz_branch_manager`, `ci_canvasser`) — absent from dialog-src, compiled presets,
  and the bundled world's regions. On a fresh install, *Grain In Goods Out* and *Minutes of the
  Quarterly Review* are unfinishable and *Non-Compliance* stealth always succeeds. Add
  `entity_tags` to the four character JSONs and recompile.
- **Three purchase buttons charge via bare `cobbledollars remove`** (clamps at 0) — a broke player
  gets the goods free, worst a free Magikarp. The proven pay-probe rail already exists in-repo.
- Raw `$(seals)/$(prices)/$(papers)` macro tokens leak verbatim into JourneyMap waypoint names.
- Auto-untrack toast says "objective complete" even when a timed quest fails.

### Side systems
- **The shrine chain is dead end-to-end** — prereqs never enforced (`canBattleTrainer` has zero
  callers), trials never started (nothing invokes `shrine <id> start`), `completeChallenge` sets no
  tag/achievement, and unit 14's build plan rests on two engine assumptions that are false in the
  current Java. **Land a small Java pass before authoring the 20 cultist bodies** or they compile
  into a ladder that doesn't gate + a leader button gated on a tag that's never set.
- **Dark Urge cadence (12%/faint) is too rare for a live show** — the "thesis mechanic" can go
  whole sessions unseen and the whiteout usually passes in silence. Raise to ~0.35–0.40, add a
  first-faint-of-run guarantee and a guaranteed whisper on the whiteout path.
- **Wheat war saturates at 4 while the UI sells 6** — "THE MONOPOLY BREAKS" is a title card over
  nothing and farms 7–10 pay only fireworks. Give 6/6 a real payoff (tag + advancement + traders
  stand down) and a 10/10 completionist sting.
- **Seraphine Crownseer's blank-plate promise has no payoff entry** — she explicitly says "come
  find me as Champion and the wall will have carved it back," but has no `royal_league_champion`
  entry. Free, already-set-up stream moment; add the priority-40 entry.
- Memory-fragment sub-line is never clip-able (chat echo drops it; a disconnect in the 4s window
  loses the beat) — append the sub to the chat echo.

### Trainers & battles
Battle content is in far better shape than the last snapshot — all 236 files parse, teams are
full, the ace = entry-cap+2 convention is perfectly implemented, types are pure, the villain
escalation is clean. The defects:
- **The Board name-match bug** (P0 #1 above) — the one severe break in the cap chain.
- **Grunt/kalahar name drift:** grunts 3–8 got real names in the rctmod files (Bello, Naoko, …)
  but `villain_team.json` still has title-only names, so those defeats never register and rewards
  are skipped; the `contains()` clause cross-fires (Osamu credits Sade's slot; one of Hui/Ivo is
  never marked; grunt_1/2 both "Field Agent"); and the two Kalahar route trainers have swapped
  displayNames. Sync the names and make `InitiativeInit` prefer exact match before `contains()`.
- **108 stale config team blocks** diverge from the live rctmod teams (pre-re-space levels,
  config-only Mewtwo/Darkrai/Xerneas/Rayquaza/Kyurem) — display-only today but `/ca info` lies to
  the showrunner mid-stream. Strip the team blocks or regenerate them from rctmod at build time.
- **Boss rosters are inverted:** gyms 1–2 field 4 mons, gyms 3–10 field 3, while optional shrine
  priests field 4 with full items (Ignis outguns Vulcan). Add mons at/below current mid-levels
  (never touch the ace); give every leader mon an item from gym 5 on.
- `hua_zhan_leader`'s Roserade holds `cobblemon:big_root`, which isn't a Cobblemon 1.7.3 item
  (validator's sole warning) — swap to `black_sludge`.
- 16 frontier challengers are itemless at L90 with stray box-legendaries on casual bodies; the
  Founder's static fallback reads as "another dark specialist" not a mirror (shares 3/6 with E4
  Luna) — retile toward the journey's greatest hits, keep Zoroark as the lead.
- Unit 12's route agents reuse interior grunt teams 15–40 levels below their route bands — mint
  dedicated `route_agent_*` teams at band-cap −2..−4 before compiling.

### Player-facing strings (Java + resources)
- **Shrine advancement grants use wrong ids** — code grants `shrines/fire_shrine`, files are
  `shrine_fire.json` → the toasts never fire (masked by the chat flavor). **`all_badges` /
  `all_shrines` don't exist at all** → the two biggest aggregate milestones silently never appear.
  Also "Pokemon League Qualified" is unaccented and off-canon (it's the Royal League).
- **Gym advancement tree swaps gyms 1 and 2** — chain renders root→Grass→Bug→Fairy but the route
  is Bug(1)→Grass(2). Swap the two parents (and fix Fairy's parent).
- **The whiteout/permadeath moment — the most-replayed clip of the production — ships on generic
  text** ("You Blacked Out!", "You have no Pokémon left!") with zero corporate-amnesiac flavor. The
  tiered-message infrastructure is right there. Layer a run-tier voice over the mechanical line.
- **No advancements for the entire villain arc** — the tab dead-ends at the Champion; DJ, the
  Board, and `company_overthrown` (the Founder) have none. Author a hidden branch.
- Level-cap clamp says "the next badge raises it" in the Champion/Board windows where no badge
  exists (and `getNextLevelCapRequirement` — which would say the right thing — has zero callers);
  the sacrifice screen always says "You fled" even on the forfeit path; `/ca reset` sets the cap to
  20 (not the base 15); advancement copy is boilerplate against a strongly-voiced world.

### Wiki / player docs
Beyond the P0 spoiler-leak and stale-table issues:
- **Quest pages stop at Hua Zhan (gym 2).** No page for Mystic Marsh (next town, gym fully
  fielded), gyms 4–10, HQ, League, routes, Frontier, or the three new facilities. Ship a slim
  Mystic Marsh page + a Facilities page now; add a single Wheat-War hub page that owns the
  field-count canon so the 6-vs-10 contradiction stops breeding across pages.
- **`Commands.md` (claims "verified against the mod source") omits the entire alpha.14 surface** —
  stadium, daycare, safari (all perm-0 player-facing) and cutscene. Stadium/Daycare/Safari appear
  nowhere in the wiki at all despite being runtime-verified and drenched in the corporate-satire
  voice the production sells.
- The removed "Performance Review" stealth meta is still advertised on 3 pages; the tower-gate
  rule contradicts itself (Sora ungated, Aiko needs 2 wins — not "entirely optional"); gym coords
  come from stale config guesses (Cyber ~150 blocks off the real interior); the `_weak`-variant and
  Gaviota hightide gym mechanics are documented for Takehara only; the Archivist re-reader is
  advertised live in 5 places but is authored-dead; Home.md announces alpha.6 (eight behind).

---

## Positive surprises (done, but tracked as not-done — update the docs)

These are safe to check off / re-point; the *new information* is that they are finished:

1. **Farm-guard liberation is fully wired for all 10 farms** — every `villain_site_manager_N`
   preset carries `on_win → free_field {field:farm_N}`, all 10 farm names are seeded, all guards
   have real surface coords. TODO's "single biggest Act-1→Act-2 blocker" is done in source (only a
   fresh-world runtime verify + an existing-world latch-refresh for the 23 guard bodies remain).
2. **The Battle Frontier is fully cast AND placed** (real L100 brain teams at real coords) — TODO
   says "24 uncast." (Gating/economy still unbuilt — see P0 #2.)
3. **Gyms 3–10 are fully fielded and placed** (all 7 roster slots, `_weak` variants, latches) —
   the wiki still says "4 of 7, no bodies."
4. **Every boss team is populated** — the old "39 missing/empty teams" is fully resolved (0 jar
   errors).
5. **The decline-fee pay-probe rail is built and correct** (31 `route/decline_*` functions) — the
   scout digest called it unbuilt.
6. **The register ↔ compiled waypoints are perfectly in sync** (28/69, zero semantic diffs) —
   CLAUDE.md's "26/57" is the stale number, not the data.

**The true critical-path blocker is now body placement:** DJ, the 3 admins, grunts 3–11, the
Board, the Founder, the Royal League five, and the 5 shrine leaders are all authored/validated/
compiled/onwin-wired and placed *nowhere* (45 bodies queued in `placement_plan.json`). Before the
placement walk reaches the Boardroom, build the Founder `spawn_gate` (champion-gated) and rule the
Board fork.

---

## Remove / cut candidates

- **108 stale `cobblemon_initiative/trainers` team blocks** — strip the team/bag/ai blocks (keep
  the fields Java actually reads) or regenerate from rctmod at build time.
- **`npc_map_template.json` phantom `cultist_3/4` rows** (10) point at presets that never existed —
  delete with the cultist ruling, or a template consumer imports a nonexistent preset.
- **`TrainerDefeatedCriterion`** (registered, never triggered) + the orphaned
  `NuzlockeConfig.damageMessage` (unreachable in removal mode) — wire or delete.
- **Stale TODO "Founder name de-obfuscation"** item — contradicts the shipped reveal (delete).
- Two dead registers (`dark_urge.json`, `memory_fragments.json`) — repurpose as the re-reader
  source or delete.
- `sq_lucian_deliveries.json`, `sq_perf_review_guide.json` — already on the pruning list, confirmed
  still dead.

---

## Non-issues (raised, then refuted — do not re-open)

- **Mom's homecoming "missing"** — it exists (`mom_first_meeting.json` homecoming entry, gated on
  `villain_final_boss`); the claim came from a grep for a literal tag that dialog gates never store.
- **Tomo's "Wheat — APPROVED" memo** — deliberate designed foreshadow (`QUEST_OPTIONS_TOWNS_1-2`),
  a celebrated pre-gym-2 stream moment, not a canon break.
- **Rumor "prices change while you stand in the shop"** — the engine *can* deliver a stepped live
  change (`/cobbledollars reload` refreshes an open shop GUI); it's an authored villain-boast echo.
- **Lucian's ungated lore menu** — the 2026-07-06 narrative audit sanctioned her as the first
  founder-lore source; gating her would contradict her own badge-0 quest.
- **Vetra's rumor-hub "masking"** — inherent to any priority-ladder hub; the proposed one-line fix
  changes nothing observable.
- **Shrine "double crystal payout"** — refuted; `grantShrineCrystal` is message-only (no item), so
  there's exactly one crystal. (The flat-L70 crystal spawn is real but design-tracked in doc 14.)
- **Raid escalation "inverts" (grunt_11 > DJ)** — the grunt gate is a stale unshipped proposal row;
  grunt_11 is gym-9-gated where its level is coherent.

---

## Suggested sequence

1. **P0 bug sweep** (data + tiny Java): Board name bridge, gate `/nuzlocke` kill switches, gate the
   Frontier + League battle buttons, fix the shrine/all-badges/all-shrines advancement ids, swap
   the gym 1/2 advancement parents.
2. **Convert the tent-pole monologues to page chains** and scrub the wiki spoiler leak + regenerate
   the boss tables (both pure text, both high on-air impact).
3. **Truth-pass** over TODO.md / backlog / CLAUDE.md / CONTENT_ROADMAP.md; **close the two forks**
   (raid geometry, Board seated) and fix `LORE_BIBLE §8` + the Dark Urge tiers + the 6/10 field
   canon — this unblocks the content batches.
4. **The placement walk** (now the real blocker) with the Founder spawn-gate built first.
5. Then the wiring seams (`field_N_liberated` mirror, badge ceremony, sensor entity-tags, purchase
   probes) and the quality passes (recognition variants, gym-crew voices, name renames).
