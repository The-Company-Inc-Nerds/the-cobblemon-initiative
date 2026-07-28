# Quest & NPC Drift Audit — 2026-07-27

Cross-reference of every `wiki/Quests-*.md` page (18 pages + guidebooks) against the live
tree (`dialog-src` register/characters/dialogs, `function/**`, rctmod trainer defs,
`docs/NPC_ROSTER.md`). Method: 21-agent sweep — one verifier per wiki page + three
reverse sweeps (58 UNPLACED roster rows, the 87 dialog-lint errors, register-vs-wiki
diff). ~55 of 70 register quests verify end-to-end; the findings below are the drift.

**Nothing was deleted.** Fixes applied during the audit are listed in §5; everything
else is a finding awaiting a showrunner call (decision list in §6).

---

## 1. NPCs not used anymore (the question asked)

### 1a. Genuinely dead — zero references, removal candidates
| Who | What it was | Evidence |
|---|---|---|
| **Wen** `hz_greenhouse_docent` | Greenhouse Tour docent (tour spiel, visitor-kit stamp) | Greenhouse de-quested a23; UNPLACED; stamp gated on `wheat_named`, which nothing can set anymore; 0 inbound refs. The wiki itself says "place him at the Company house **or cut him**" |
| **7 gym guides** `cyber/deepcore/gaviota/kalahar/nifl/ryujin/scorchspire_guide` | Per-town gym-guide info pages | All UNPLACED, STEVE-default, 0 refs; the guide role was superseded by the nurse rumor-hubs (register note: "replacing the spec defeated_scorchspire_guide placeholder"). Placed guides exist only for towns 1-3 (Takehara, Hua Zhan=Lanying/Tao Shen, Mystic=Veyric a26) |
| Dialog files `dialog/royal_scrollkeeper.json`, `dialog/royal_healer.json` | Pre-move Royal League support trees | Both explicitly "left in place, now unreferenced" — roles moved to Lucian (Sango) and Dr. Asha (Sango clinic) |

### 1b. Dead machinery (functions/entries wired to nothing)
- **`sidequest/greenhouse/reveal.mcfunction`** — "THE WORD IS WHEAT" title beat: **zero
  callers** (Shu's dispatch-board button was dropped in her a26 wool re-role; header
  comment still credits her). Consequence: `wheat_named` + `toured_greenhouse` are
  unsettable → Bo's post-reveal band, Wen's stamp, and half of Blossom's `traders_word`
  gate are unreachable. The wheat word still lands via the wheat-trader pitch
  (`heard_wheat_pitch`) — the story beat survives, the greenhouse route to it is dead.
- **`sidequest/noncompliance/attempt_one/two/three + scold`** — the retired Kazuo
  moth-print stealth loop; no dialog references remain.
- **Old Off the Record errand chain** — `sidequest/off_record/{deliver_ledger,
  deliver_basket,finish}` + Oma's `off_ledger` entry + Fara's basket leg + the auditors'
  `stand_and_be_counted`: nothing grants `carrying_ledger` since the 2026-07-21 4-agent
  rework. NOTE: `off_record/tick.mcfunction` is **still tick-registered** and dead-runs
  its ledger line every tick. A comment in `deliver_basket` names turn-in NPC "Sarii",
  who no longer exists anywhere.
- **Badge ceremony** — `rewards/gym/badge_1..3` → `rewards/badge_ceremony` (fireworks +
  EARNED title): authored, never called from any gym reward row. (Matches the old
  "authored-but-dead" memory; confirmed still dead.)
- **Fire/Ice/Dragon shrine trainer-defeat reward configs** — `trainers/shrines/
  fire_shrine.json` still promises Master Ball + Netherite on a battle that can never
  fire (those keepers are dialog-only since the 2026-07-19 structure ruling).
- **`sidequest/memo/` eavesdrop chain** ("Per My Last Memo") — gated on `memo_loiter`,
  whose granting poller was never written; also depends on the unplaced checkpoint pair.

### 1c. Placed bodies with hollowed roles (re-role or trim candidates)
- **Bo** `hz_greenhouse_greeter` (1482 87 2166) — a26 re-voiced him into a fine south-gate
  greeter, but he still grants `hz_arrived` (0 readers) and carries an unreachable
  `wheat_named` band.
- **Rong** `hz_greenhouse_overseer` (branch office loft, a26) — sole surviving mechanic is
  `greenhouse/exit_fee`, a *greenhouse* escorted-exit fee now offered from a branch
  office; `_comment` still cites the retired catwalk reveal.
- **Hilda Frostmother** (Nifl, uuid body) — dialog still promises "walk the lanterns with
  me" but the whole Long Memory chain lives on Vetra; Hilda has zero mechanics.
- **Kazuo** `company_canvasser` (Takehara) — stealth-sentry role retired 2026-07-22;
  lives on as an opt-in battle; his and Ume's `_comment`s still describe the old design.
- **Gaviota Open quartet** (Enzo/Gianna/Rosa/Marco) — `champion` entries gated on
  `gaviota_open_champion` from a derby that was never built. Permanently locked content.
- **Tunde** `sango_company_liaison` — re-cast as Off-the-Record agent #1, **but his old
  Invitational `podium` entry is still live** → see §4 bug list (double purse).

### 1d. Authored-but-never-placed ghosts (backlog, NOT dead — wiki oversells them)
Fully wired quests whose bodies do not exist in the world; the wiki presents them as
playable:
- **Blossom Path checkpoint**: Sani `villain_grunt_field_agent` + Haruki `villain_grunt_2`
  (checkpoint fee/battle forks, the dead-letter interception stage of q.side_letter).
- **Femi** `sq_kyc_agent` — the whole Know Your Customer quest is unencounterable.
- **Incomplete File stage-2 cast**: Binta `company_surveyor` (the stealth watcher — its
  sight checks match nothing) + `notice_post_1/2/3` (⇒ stages 2-3, 600 + 4000 CD,
  are uncompletable). `doc_ledger_barrel`/`doc_portrait_crate` are *deliberate*
  non-places (Java DocPropManager handles the real blocks).
- **Facilities**: `stadium_clerk` (no arena built; Volt's stadium gate unwired),
  `safari_concierge`.
- Known-blocked casting (unchanged): HQ ladder (grunts 3-11, admins, DJ), Board ×4,
  Founder, Royal League five, penthouse companions.

---

## 2. Quests removed or renamed (wiki still documents the old form)
| Quest as wiki tells it | Reality |
|---|---|
| "Verified Growth — the Greenhouse Tour" (Overview, Guidebook-Act-I) | De-quested a23; no register holder; cast scattered (Shu→wool, Rong→branch office, Wen dead) |
| "Performance Review" gym-1 stealth meta (Overview) | REMOVED 2026-07-07 (render comment); only the full-tower-clear bonus survives |
| Mirek "Scarecrow Wrangler" row + battle (Harvest-Road) | Re-themed a26 to Sporeherd Maslen, Willowmire corridor, new trainer/levels; `sq_rt2_scarecrow` retired in place |
| "Notice of Non-Compliance" (Takehara) | Renamed **Market Forces** 2026-07-22, bee-swarm defense at Mei's house |
| Canvasser "administrative clearance 150 CD" + its exploit warning | Button removed 2026-07-20 — the interaction and the documented bug are both gone |
| Cascade record board ("RECORD UNDER AUDIT") | Prop + thread retired 2026-07-22; replaced by the masked-watchers thread |
| Hua Zhan human wardens (Lin/Mei/Fang/Xiu) + Jr. Lian/Sakura ladder (Main-Story) | a23: gym = 4 battle statues, no interior ladder, 6-mon Blossom |
| "Per My Last Memo", "Head Count", roadside work orders, bell-sprint (Guidebook-Act-I) | Exist **only** in that file — no live implementation |
| Old Off the Record stealth errands + Lucian debrief payout | Reworked 2026-07-21 to the 4-agent clearout; debrief unreachable, no payout |

Wiki additionally does not know about: `q.side_nets`, `q.side_lamps` (a26),
`q.side_papers` (since 2026-07-06), and the **7 noble quests** (deep/grid/sky/gale/
mountain/ember/wisp) which appear on no Quests page at all.

---

## 3. Wiki-stale highlights (full per-page details in the sweep reports)
- **Arena coordinates are stale on ~7 pages** — they quote the vestigial Java gym-def
  coords, not the placed bodies (Titania 944 69 2444, Bruno 992 129 3192, Neptune 596 87
  3646, Gaia 1978 131 4092, Volt 1306 100 1190, Ryujin 2264 210 1090, Boreas 3628 119
  1900, Vulcan 3660 95 4668, Cicada 2056 138 2460).
- "Gyms 3-10 unbuilt / no NPCs placed" warnings (Main-Story) — all eight are fully
  placed with complete ladders and intro cutscenes.
- Nurse heals are **not** flat 100 CD anywhere: fee = 100 + 2×instability.
- Shops table wrong (no Cherish Ball, no apricorn balls, badge-1 adds Net/Nest).
- Fairy shrine = a26 drowned descent (five allay vows + fairy-type Aurora at
  947.5 0 2703.8); Mystic page still has the altar version and "three tracked quests".
- Fire Shrine has no leader fight/loot; trial starts at the Acolyte, Ignis is dialog-only.
- Team retunes not reflected: KYC 19/20, Genji wager 16/17, HQ ladder (Shade 25-30 …
  DJ 59-64), Royal E4 73-81/Cynthia 79-83, Board 6× lv81-87.
- Missing new layers: gym MC-gates (gyms 1/2/5/9 verified), homestead/beacon buttons,
  Sedge perch gift, Victor descends instead of transforming at the tower top.
- Founder victory loot is elytra + rockets (not Master Ball/Netherite/wheat); E4/Board
  item payouts from the wiki are wired nowhere (CD + tags only).

---

## 4. Live-tree bugs surfaced (not wiki problems)
1. **HQ raid-gate split — needs a canon ruling (4 vs 6 fields):** DJ's dialog gate,
   `quest/render.mcfunction` marquee flip, and `liberation/ceremony` all say **6**;
   the q.main register stages, the Cyber door pointer beat, and `docs/QUESTS/10_hq_raid`
   say **4**. Sidebar currently flips at a different count than the boss actually
   accepts.
2. **Double Invitational purse:** Tunde's leftover `podium` entry pays a second
   champion purse (600 CD + heal ball + candy) on top of Tayo's — live dupe.
3. **Dead tick line:** `off_record/tick.mcfunction` still registered in tick.json,
   dead-runs the ledger check every tick.
4. **Shrine rumor buttons never retire:** dragon/fire/ice keepers are dialog-only, so
   `defeated_<x>_shrine_leader` is never granted and the `no_defeated_*` inverse gates
   are constant-true.
5. **Stale in-game TM promises:** onwin lines still say "the machine in your bag holds
   Earthquake/Flamethrower/Moonblast" (Gaia, Vulcan, Titania) — gym TMs were dropped
   a18/19; nothing is granted.
6. Minor: Sefu's `seal` entry can never open (trade cmd opens no dialog); Ariana's
   `late` entry permanently masked by `the_blank_plate` (same gate, higher prio);
   Kwame's first-win Super Potion rides only the InitiativeInit rewards list (worth a
   live-fire check); register `_comment`s are a wave behind ("leaders uncast", "six
   farms", Yan "at the gym gate").

---

## 5. Fixed during this audit (mechanical, no content deleted)
- `quest/render.mcfunction` is **hand-maintained** (not compiler output — corrected
  belief): added the missing sidebar blocks for `q.side_nets` (60) + `q.side_lamps`
  (62); re-keyed the fairy-shrine ladder off the dead `defeated_fairy_shrine_cultist_2`
  tag onto the Java-granted trial tags; relabeled Five Keepers claim to Keeper Aurora.
- `q.side_lane` slot collision (rendered at 78 = q.side_pilgrim's slot; register had no
  slot) → re-slotted to 69 in both layers.
- `q.side_bones` waypoint retargeted `curator_tamiko` → `museum_sayuri` (the actual
  donation desk, ~30 blocks off).
- Full `content_compile` re-run: 760 chars, errors: 0.

---

## 6. Decisions needed (nothing acted on)
1. **Cut or keep**: Wen (`hz_greenhouse_docent`), the 7 unplaced gym guides, the two
   orphaned royal dialog files, `noncompliance/*` + old `off_record/*` +
   `greenhouse/reveal` + badge-ceremony functions, `sq_rt2_scarecrow` remnants.
2. **Tunde's podium entry** — remove the second purse (recommend: yes; it is a dupe).
3. **HQ gate**: 4 or 6 fields — then align DJ gate, render flip, ceremony, door beat,
   register, and the two wiki numbers to the winner.
4. **Place or cut** the ghost cast: checkpoint pair + Femi, Binta + 3 notice posts
   (Incomplete File stages 2-3 are dead until then), stadium/safari fronts.
5. **Hilda Frostmother** — re-role (she reads as a broken promise next to Vetra).
6. **Gaviota Open** — build the derby someday, or strip the four champion entries.
7. **Wiki sync pass** — §2/§3 are effectively the punch-list; the last `publish-wiki`
   was 2026-07-18 and pages drift per-wave. (Beware name collisions when grepping: two
   Kaitos, two Corvins, two Veyrics, two unrelated Selenes.)
