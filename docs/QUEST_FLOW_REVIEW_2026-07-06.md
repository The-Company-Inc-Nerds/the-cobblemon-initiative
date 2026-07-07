# Towns 1–3 Quest-Design Review — Decision Plan
*Synthesis of five lens reviews (flow / challenge / randomness / stream / cohesion), deduped and adjudicated. Load-bearing claims spot-verified against source this session: `quest/render.mcfunction` file-line gate lacks `!sold_docs`; `sq_canvasser.json` still 13/14, `sq_genji_wager.json` 13/13, `sq_headcount_wager.json` 12/12; `economy/heal_paid.mcfunction` hardcodes 100 with the pay-probe pattern; `derby/begin.mcfunction` uses raw un-probed `cobbledollars remove`; zero `firework` usage in all of `function/`.*

---

## 1. VERDICT

Towns 1–3 are structurally excellent and presentationally lopsided: the forward pull is genuinely strong (every arc plants its next hook — the memo says "proceed to Hua Zhan," the mayor names the water delegation, Masumi names Harvest Road), the battle curve is honest (leader ace = cap+2 lands at both gyms, above-cap fights are opt-in, fail-soft everywhere), and the receipt/title-card house style is the best streamable mechanic in the pack. Three systemic gaps recur across all five lenses. **First, the world never talks backward:** Lucian's hub is one-directional and inconsistently honored (RZ-7 pays zero, Takehara contributes no paper, eight filing latches produce zero reaction), `census_signed` and `fields_liberated` echo nowhere behind the player, and the arc's biggest beats (liberation, badge, courier betrayal) whisper while the fishing derby sings. **Second, money cannot say no:** every fee and stake except the nurse uses un-probed `cobbledollars remove`, the only recurring sink is a flat 100 CD against ~20k of one-time payouts, and three stale wager teams (13/14, 13/13, 12/12) pay real purses — so the villain plot's entire economic premise exerts zero mechanical pressure. **Third, discovery and continuity decay with distance from spawn:** Takehara's 12 quests are all walk-up finds, the run's flagship sidequest goes dark for the whole badge-1-to-3 window behind a stale sidebar line, and the replayed-most content (permadeath resets of Sango→Takehara) has no variance at all. One genuinely unfair beat exists — the no-decline spotters can whiteout a solo-starter run in minute 25 — and it must be resolved inside the incoming battle-engagement matrix, not around it.

---

## 2. TIER A — QUICK WINS (implement without further sign-off)

*All trivial/small effort, medium+ impact, no game-feel change beyond presentation/consistency (one exception noted at A15, which has shipped precedent). Ordered by impact-per-effort. Merged sources in brackets.*

**A1. Courier SELL aftermath — fix the zombie sidebar and make the betrayal land** [stream; verified bug]
- **What:** Add `tag=!sold_docs` to the two `q.side_file` lines in `render.mcfunction` (confirmed: gate is only `file_opened,!notices_filed`); add a `sold_docs`-gated replacement line `• The record is Company property now` (dark_red, permanent); fire an `ASSET LIQUIDATED` title + low sculk/anvil sting on the sell button; one `sold_docs`-gated Elder Sentinel cold-shoulder line.
- **Why:** A seller currently keeps a forever-stale objective pointing at a quest they killed, and Act 1's biggest chat-vote fork resolves as one gray chat line.
- **Files:** `src/main/resources/data/cobblemon_initiative/function/quest/render.mcfunction` (~lines 168–172); `dialog-src/dialog/sq_personnel_courier.json` (sell button); `dialog-src/characters/sango/elder_sentinel.json`.

**A2. The census file actually arrives in Takehara** [cohesion + stream, merged]
- **What:** Kazuo gets `census_signed` ("Sango forwarded a provisional profile. The photograph resolves. The name does not.") and `census_refused` ("Declines get their own folder") entries; Femi's KYC survey gets a `census_signed` variant opener ("Pre-verified — except the last page"). Both forks converge on existing content; zero balance change.
- **Why:** "Forwarded to the Takehara branch" is spoken on screen and never pays off — `census_signed` has zero consumers outside its own quest (grep-verified). Cheapest "the system is tracking you" beat available, on both forks.
- **Files:** `dialog-src/dialog/sq_canvasser_patrol.json`, `dialog-src/dialog/sq_kyc_agent.json` — three gated entries, recompile.

**A3. Bridge the Incomplete File's badge-1-to-3 dark window** [flow]
- **What:** (1) Lucian bridge entry gated `docs_filed + !notices_filed + badges<3` (inverse band tag): "Come back wearing a third badge." (2) Sidebar variant for the same band: `• Lucian waits on a third badge` instead of `• Rebuild the record`.
- **Why:** The run's spine quest points at a silent NPC for two full gym arcs — the single worst hand-off in the act.
- **Files:** `dialog-src/dialog/sq_personnel_file.json` (new entry ~prio 44); `function/quest/render.mcfunction` (badge-banded display variant); pattern per `docs/ENGINE_FINDINGS.md` inverse band tags.

**A4. Close the Performance Review wiring + give the ghost reveal its screen** [challenge + stream, merged]
- **What:** Fork a takehara-only guide dialog carrying the `verification_bonus` entry (+ missing house_rules button); promote the 4× sentry tagging (`npcsight add` + `takehara_sentry`) to a **release-blocking** world-setup step; add an op-only warning in `perf_review/tick` when zero tagged sentries exist while a player is in the tower; schedule `ghost_reward` 5s after badge time and upgrade it to a `SILENT STAKEHOLDER` title; add one Lucian line gated `perf_review_ghost` (currently consumed by nothing).
- **Why:** As shipped, every player silently "ghosts" and collects the cache unearned, the Sweep bonus is unreachable, and the reveal fires under the badge title. The gym-1 hidden meta is decorative until this lands.
- **Files:** `dialog-src/dialog/sq_perf_review_guide.json` → new `dialog:takehara_guide`; takehara guide character pointer; `function/sidequest/perf_review/tick.mcfunction` + `resolve`/`ghost_reward.mcfunction`; `sq_personnel_file.json`; VERIFICATION_RUNBOOK checklist entry.
- **Note:** the new tower-optional `_weak` directive makes this *better*: ghosting now means fighting a full-strength Cicada — the risk/reward is emergent. Keep.

**A5. Make field liberation land — ceremony forward, gossip backward** [stream + cohesion, merged]
- **What:** Title ceremony per liberation (`<FIELD> — LIBERATED` in wheat-gold, subtitle `The commodity loses ground — n of 6`, bell, fireworks over the barn; special first-liberation subtitle `The Company will notice.`), plus three `fields_liberated>=1` back-echo lines: Tunde at the Sango fence, Elder Sentinel, Masumi.
- **Why:** The n/6 counter is the villain arc's scoreboard and currently advances via one actionbar line; nothing east of Harvest Road ever acknowledges the Wheat War.
- **Files:** `function/liberation/free_field_apply.mcfunction` (extend the existing `{field:...}` macro with `display:`); `dialog-src/dialog/sq_invitational_rep.json`, `characters/sango/elder_sentinel.json`, `dialog/sq_sweetwater_futures.json` (compiler `fields_liberated` numeric gate is first-class).

**A6. Two-stage badge ceremony — fireworks, then the purple silence** [stream; verified: zero fireworks in the datapack]
- **What:** Instant triumph layer (badge title + cap subtitle, toast sound, 4–6 fireworks, villager celebrate), then `schedule` the existing memory-fragment title 4s later so the eerie beat lands alone.
- **Why:** The show's recurring climax currently reads quieter than the fishing derby; the fireworks→silence cut is the show's thesis in ten seconds.
- **Files:** New `function/rewards/badge_ceremony.mcfunction` (macro: name + cap); one command row per gym config (`trainers/gyms/takehara_falls.json`, `hua_zhan_city.json`, `mystic_marsh.json` — same pattern as `memory/gym/frag_N`).

**A7. Lucian's ledger gets heavier + one-line "filing day" sidebar** [cohesion + flow, merged — shared infrastructure]
- **What:** One tick function maintaining two recompute-from-tags scores: `ci_papers_filed` (8 filing latches) driving two new Lucian default-tier entries at ≥3 and ≥6 ("Boxes are labeled by name. Yours is labeled by an absence."); `ci_papers_held` (unfiled-held tags) driving sidebar aggregation — at ≥2 held, suppress individual deliver lines and render `• File with Lucian: n papers`, waypointed at her.
- **Why:** Eight filings currently produce zero reaction, and a Hua Zhan player can hold 3–4 sidebar lines all pointing 1000 blocks east. One trip, one ritual, one desk that remembers.
- **Files:** New `function/sidequest/personnel_file/papers_tick.mcfunction` (registered in tick tag); `dialog-src/dialog/sq_personnel_file.json` (two score-gated entries — compiler lowers to band tags); `function/quest/render.mcfunction` + macro `set_papers.mcfunction` (pattern: `quest/set_wheat.mcfunction`); `quest_waypoints.json` aggregate stage.

**A8. Nurse Lila becomes Takehara's rumor board** [flow]
- **What:** "What's the word around town?" button serving rumor pages gated on each quest's not-done tag; rotate via `/random value 1..5` into a `ci_rumor` score (fallback: one static page naming three spots).
- **Why:** Takehara's 12 quests are all walk-up discoverables and the paid-heal nurse is the one NPC every hardcore player visits repeatedly. Fixes the town's missing arrival hub with zero new NPCs.
- **Files:** `dialog-src/dialog/nurse_lila.json`; tiny rotation mcfunction. **Coordinate:** if the heal-fee indexing (B3) is adopted, touch this dialog once, not twice.

**A9. Complete the paper hub — Takehara files in, Hua Zhan acknowledges out** [flow + cohesion, merged]
- **What:** (1) RZ-7 filing pays like its siblings — 200 CD (skew-aware) + `training_minor`, Lucian's joke becomes "the fee files itself too"; (2) Field Memo 7-12 files for 200 + minor, with Tomo's turn-in pointing at "the archivist in Sango"; (3) read-and-return flavor for the Firstfurrow deed and Provisional ID (no items taken); (4) one archivist Shu entry gated `sting_reward_paid + !wheat_named` that refuses to say "wheat" while pointing at the dispatch board — and converts the honey-rejected-vs-perpetuity contradiction into deliberate doublespeak.
- **Why:** RZ-7 paying zero teaches "the archive stiffs you" exactly when the ritual is being established; Takehara currently produces zero archive traffic; the memo that names Hua Zhan gets no reaction there.
- **Files:** New `function/sidequest/personnel_file/file_report.mcfunction` (mirror `file_docs.mcfunction`); `dialog-src/dialog/sq_personnel_file.json` (filing + flavor entries); `characters/takehara/sq_beekeeper_tomo.json` (one line); `characters/hua_zhan/hz_greenhouse_archivist.json` (one entry).

**A10. The Shorefront Invitational becomes a televised bracket — and the crier does his job** [stream + cohesion, merged]
- **What:** `onwin` round title cards on all three bracket fights (winners-first token rules per ENGINE_FINDINGS), `DISBURSEMENT COMPLETE / pre-counted for your convenience` sting before the 97% receipt; fix Kofi's "two rounds" → three; add his `invit_podium_done` champion bark (and optional `defeated_sq_bracket_3 + !podium` variant).
- **Why:** The best-shaped Sango content after the derby is currently invisible as a format, and the town's designated announcer never announces anything.
- **Files:** `data/rctmod/trainers/sq_bracket_1/2/3.json` (onwin arrays); 4 tiny mcfunctions; `dialog-src/dialog/sq_invitational_rep.json` (accept button); `dialog-src/dialog/sq_invitational_crier.json`.

**A11. Discovery pointer pass — five one-line breadcrumbs** [flow, bundled]
- **What:** (1) Printmaker Mei gated `!defeated_sq_mayor_suits`: "Two grey suits took the gym stairs… the mayor has not come down" (the 1060 CD roof scene is otherwise permanently missable); (2) Lucian gated `memo_delivered + badges≥1 + !hz_minutes_heard` museing about the Eastern District branch's missing minutes (makes the hub bidirectional); (3) Mio's race offer warns about the mid-route spotters ("eye contact is a contract — settle first, or run wide"); riders: (4) Kele → Deka's derby line; (5) Bo/Kaito → Guo the Miller line (fold into HZ pass).
- **Why:** Discovery decays with distance from spawn; a streamed run verifiably misses the roof scene and the branch office on the natural path. The Mio warning is also a hardcore-optics fix: "the game warned me" is content, an unwarned forced battle mid-timer is a rant clip.
- **Files:** `sq_ume_notices.json`, `sq_personnel_file.json`, `sq_sprint.json`, `sq_kele_lane.json`, HZ greeter/martkeeper (via HZ pass).

**A12. "The world gossips back" micro-line pass** [stream + cohesion, bundled — all pure gated dialog]
- **What:** (1) Lan's reachable recognition beat gated `hz_minutes_heard OR billable_hours_done` ("The confidence model flagged an outlier… it has your face") — restores the motif's missing Hua Zhan vertebra; (2) three `hz_prices_done` stall reactions to Kaito's flipped sign; (3) Ning at the branch office gated `defeated_sq_mayor_suits` ("alignment deferred… Compliance is appreciated" — the checkpoint catchphrase at rung 5); (4) Mio gated both checkpoint agents down + Marlow gated `memo_delivered`; riders: Ume↔Imani cross-references, Dr. Asha disowning the Field Liability Policy ("Section 4 is not medicine — it is arithmetic wearing my coat").
- **Why:** Outcomes almost never echo behind the player; each line is trivial and the set converts three short-story towns into one gossip network.
- **Files:** `characters/hua_zhan/hz_analyst.json`, `hz_stall_{linh,wei,mei}.json`, `hz_receptionist.json`; `characters/route1/courier_mio.json`, `characters/sango/sq_uncle_marlow.json`; `sq_headcount.json`, `sq_pending_review.json`, `sq_preferred_provider.json`. **Constraint:** Mei Lin's stall line must be authored against the price-check stop-3 dialog block, not the uuid (nurse recast in flight — HZ pass wins).

**A13. False-clue hygiene sweep** [cohesion + stream, merged]
- **What:** Ayame→Shou in cascade `expire`/`win` subtitles; Tamiko→Sayuri in `donate_bones.mcfunction`; break the Mayor Liang / Liang Yue collision; fix or repoint Sayuri's dangling `revive`/`dig_directions` buttons; rewrite the Adjunct Faculty completion texts to close the dead Kofi-ledger pointer as erasure texture ("the grant ledger lists a coordinator the annex insists never existed"), with optional joint "Department of Two" beat + `DISBURSEMENT WITHHELD, PAYEE UNRESOLVED` keepsake and Raan's ask trimmed to 8 coal/4 iron.
- **Why:** The show trains chat to treat name glitches as founder-erasure clues; accidental ones poison the real motif, and a player-facing pointer to a recast NPC is a visible dead end.
- **Files:** `function/sidequest/cascade/{expire,win}.mcfunction`, `function/sidequest/museum/donate_bones.mcfunction`; `characters/takehara/mayor_suzune.json`, `museum_sayuri.json`; `dialog/sq_adjunct_{miri,raan}.json`. Recompile + preset regen for touched characters.

**A14. Silent-progress feedback: Miller Walk survey logging (+ micro-sting riders)** [stream]
- **What:** Route the two `grain_survey/tick` tag-adds through logger functions — actionbar `MARKET SURVEY — logged (1/2)` + page_turn, sidebar refresh at 2/2 (the proven `Noted (n/3)` pattern from price-check). Riders while in the ceremony toolbox: waiver SIGN/REFUSE title stings on Pending Review ("the desk notes your handwriting anyway"), the Sango send-off card on `got_running_shoes`, a sound on the derby's RECORD QUARTER title.
- **Files:** `function/sidequest/grain_survey/tick.mcfunction` + 2 new log functions; `dialog/sq_pending_review.json`, `dialog/mom_first_meeting.json`, `function/sidequest/derby/win_common.mcfunction`.

**A15. Kazuo retune 13/14 → 19/20** [flow + challenge, adjudicated]
- **What:** `sq_canvasser.json` (verified still Meowth 13/Koffing 14) to 19/20, movesets bumped; sync stale `_comment` level mentions.
- **Why & why Tier A:** He's gated behind `defeated_takehara_leader` and pays 280 CD — currently a stomp. Challenge lens said 18/19, flow said 19/20; **19/20 wins** — it matches the *already-shipped* KYC Femi 19/20 retune for the identical post-badge-1 gate (same band, same rationale, precedent decided).
- **Files:** `data/rctmod/trainers/sq_canvasser.json`; `dialog-src/characters/takehara/company_canvasser.json` comment.

**A16. Write the invariants down — ENGINE_FINDINGS additions** [randomness]
- **What:** (1) The bytecode-verified Easy NPC fact: multi-text `say[]` renders ONE uniformly random page per dialog open — plus the rule "never author sequential monologues in one say[]"; (2) "no random prices anywhere — the instability index is the only price driver"; (3) "numbers a player commits money against are never rolled" (stakes/loss fees stay fixed and printed).
- **Why:** 398 of ~500 entries already rotate and nothing documents it; the two rejections (price jitter, rolled stakes) must be codified so a future variety pass doesn't reintroduce them.
- **Files:** `docs/ENGINE_FINDINGS.md` §2 + economy invariants.

---

## 3. TIER B — DESIGN CALLS (showrunner picks)

*Ordered by impact. Each changes game feel; recommendation given.*

**B1. Forced-battle fairness floor — no forced engagement until the player owns a caught Pokémon.**
The one genuinely unfair beat in towns 1–3: Ayo/Zola's ON_DISTANCE_TOUCH no-decline battles can whiteout a solo-starter run in minute 25 (verified: `handleBattleFainted` has no safe-zone skip; whiteout = unconditional `player.kill()`). Proposal: `dex_gte_1` band tag (compiler's existing dex-gate machinery) gates the touch-battle entry; below it, the touch becomes a decline-able warning hail that *teaches* the contract rule. **Recommendation: ADOPT — but as a global precondition written into the incoming battle-engagement matrix spec, not a per-spotter patch.** The matrix multiplies forced proximity battles; the floor must be a matrix-wide rule or you'll re-create this bug on every new route trainer. Cost: one compiler threshold + gated entries; zero Java.

**B2. Money can say no — one shared `economy/charge.mcfunction` + stake ceremony.**
Every fee/stake except the nurse uses un-probed `cobbledollars remove` (clamps at 0): broke players enter the derby free, wager free, buy past checkpoints free. Merge the challenge lens's balance-probe macro with the stream lens's stake ceremony into ONE function: pay-probe (proven in `heal_paid.mcfunction`), `#charge_ok` branch to "Payment declined — the Company does not extend credit", plus `STAKE POSTED — n CD riding` actionbar + coin sound on success. Rewire ~9 call sites. **Recommendation: ADOPT — it is also prerequisite infrastructure for the matrix directive that declining always costs CD.** Sub-decision the matrix spec must answer: what does a broke player's decline do? Recommend: decline still succeeds but posts a `billed to account` debt tag deducted from the next payout — Company-flavored, and it avoids a broke→forced-fight→whiteout chain that would violate the fairness floor (B1).

**B3. Heal fee rides the instability index.**
Flat 100 CD forever while Lila's own dialog says "adding up is rather the point these days." Proposal: fee = 100 + 2×`cd_instability` (116 after gym 1, ~212 at act-2 peak, visible relief when liberations claw it back), keep the balance gate, print the live fee on the receipt. **Recommendation: ADOPT** — the cheapest recurring proof that the Company is squeezing the region, and it makes liberation mechanically *felt*. Risk (rising cost in permadeath) is negligible against ~20k earnings. Coordinate the `hz_nurse` dialog touch with the Mei Lin recast, and the Lila touch with A8.

**B4. Stale wager retunes — Genji 13/13 → 16/17; headcount Doduo/Bidoof 12/12 → 14/15 (keep the 900 purse).**
Both verified against source this session. Genji's symmetric 200/200 "wager" is currently a guaranteed +200; Ume's 3:1 field wager is the largest low-risk payout in Sango, pre-gym-1, where money should be tightest. **Recommendation: ADOPT the level bumps, not purse cuts** — an at-cap 3:1 gamble is a better on-camera beat than a smaller vending machine, and the loss side stays the opt-in stake (fair). Kazuo is already Tier A (A15) on shipped precedent.

**B5. Starter claims gated by badges — offer_second needs badges≥2, offer_third badges≥4.**
Verified: a diligent catcher can claim a Lv25 partner while capped at 22. The gate fix makes both claims cap-legal by construction, and deliberately lands the second-partner beat in the post-badge-2 lull where towns 1–3 run out of road. **Recommendation: ADOPT** — it's a fairness-consistency fix wearing a pacing win; Acacia's hint gains "…and the standing to train it." Files: three starter dialogs + `professor_acacia.json` (compiler `badges` gate proven).

**B6. Visible-randomness package (adopt as a set — all bonus/wares/flavor variance, never punishment):**
- **Receipt line-item roll** — one rolled zero-value line from an 8–12 pool in the Company voice on every branded payout ("ADJUSTMENT: rounding, in the Company's favor"), optional nervous second pool at index ≥40; includes the salvaged Ume rolled survey column. The single most viewer-visible surface in the game, currently verbatim. `/random value` fanout in `pay_macro_company` + `headcount/receipt`.
- **Derby "Record Species of the Quarter"** — chalkboard roll at entry, +75 CD skew-aware bonus if landed; money-only on the repeatable branch (no-farm-loop rule holds).
- **Company Morning Memo** — one dawn bulletin line near town noticeboards, drawn from the *already-authored* `registers/economy.json` tiers by instability band; the sprint day-latch pattern. Cap at one line to avoid screen noise.
- **Weighted item drips** — `clinic_rx` becomes a weighted pool (potion w4/antidote w2/paralyze w2/super w1); hampers keep current contents as floor + one rolled side item.
**Recommendation: ADOPT ALL FOUR.** Every roll prints its result in the Company's voice; nothing touches amounts a player commits against (per A16 invariants).

**B7. Per-world replay variance — tower B-teams and spotter stand rolls.**
Permadeath makes Sango→Takehara the most-replayed content in the format. B-teams: one per-world roll picks roster A/B for the four tower trainers (same displayNames — progression credits unchanged; the round-13 battle-variant override is the exact primitive). Spotter stands: `placement_choices` compiler extension rolling 2–3 stands for Ayo/Zola only (prop-anchored bodies stay fixed). **Recommendation: ADOPT B-teams; adopt spotter stands narrowly, but sequence BOTH after the battle-engagement matrix and the `_weak` variant work land** — the tower now carries A/B × base/weak variants (up to 16 team files; see Conflicts), and spotter repositioning changes the same routing the matrix governs. Effort: medium; schedule per Tier C.

**B8. Restore the greenhouse overseer battle as a wager.**
"THE WORD IS WHEAT" currently ends on a 150 CD toll booth. Rong's fight returns as a third option — contest the exit fee: stake 150 via the charge probe (B2), team 23–24 (Yield Analyst band), prize 300, one-time. Walk-free and pay-150 stay. **Recommendation: ADOPT, scheduled into the HZ pass** (it needs a team file + registry entry and the pass is already touching this building).

**B9. Notices of Non-Compliance clean-run bonus.**
Clean and scolded currently pay identically, so the patrol-timing loop has no stakes. Additive-only fix (Heal Ball + moth-print keepsake + CLEAN HANDS title on `turn_in_clean`; scolded path untouched) matches the house pattern (Off the Record, Performance Review). **Recommendation: ADOPT** — it's arguably Tier A, listed here only because it touches reward balance.

**B10. Route rematch wagers (day-latched, opt-in).**
Routes pacify permanently after one pass despite forced re-crossings. The lens design is sound (opt-in dialog wager, stake 100/prize 200 under the sprint-daily ceiling, dawn-reset latch). **Recommendation: DEFER pending the battle-engagement matrix** — if the matrix re-arms route trainers on proximity, this is redundant; if routes stay pacified after first defeat, adopt as specced. Do not build both.

---

## 4. TIER C — BIGGER CONTENT (schedule)

**C1. Quick-trader daily crate roll — URGENT: fold into the in-flight quick-trader task NOW.** The HZ berry/mint/apricorn traders are being built this round; if their presets aren't generated variant-ready (2–3 wares presets per trader + granary-style `apply_day_N` re-import functions + a dawn roll), retrofitting means regenerating every preset later. Clone the proven `generate_granary_tiers` machinery (`scripts/generate_quick_trader_days`), prices never roll, quest-critical items never crate-gated. This is the only time-sensitive item in the whole plan.

**C2. Company Care Kiosk (Takehara).** The `hz_nurse` payoff entries for `company_account_holder`/`company_declined` are fully written and verified orphaned — nothing sets either tag. Build the smallest possible setter: a kiosk prop NPC beside Lila (compiler placement-latch), one sign/decline fork, branded 0-CD enrollment receipt. Both payoff scripts are already paid for; this is buying a fork whose consequence is pre-written. Schedule with the narrative Tier 2/3 Takehara touches so the kiosk's copy matches the checkpoint front-branding voice.

**C3. Tower B-teams build (if B7 adopted).** Four new `takehara_trainer_Nb.json` files + score-banded duplicate battle entries + one per-world roll mirrored per tick (the price-check `#idx` mirror pattern). Open question for the spec: whether B-variants also need `_weak` counterparts (A/B × base/weak = 16 files) or whether the weakening override applies on top of whichever roster rolled — resolve against the `_weak` implementation before authoring. Extend to the Blossom regulars only if the tower version lands well on stream.

**C4. Spotter stand rolls (if B7 adopted, after the matrix).** `placement_choices` compiler extension emitting rolled `import_new` latches for Ayo and Zola. Does not change the known per-world npcsight-registration cost (position-independent). Sequenced last because the matrix may reposition or re-arm these exact bodies.

**C5. Rong overseer wager (B8) — inside the HZ pass.** New `sq_hz_overseer.json` (2 mons, 23–24), wager battle block gated in `hz_greenhouse_overseer.json`, register entry; depends on the charge macro (B2) for the stake.

---

## 5. CONFLICTS + OVERLAPS (what wins)

1. **Battle-engagement matrix (incoming directive) vs B1 / B2 / B10 / C4 / A11-Mio.** The matrix wins on scope everywhere. B1's dex_gte_1 floor and B2's charge macro are *prerequisite infrastructure* for it (decline fees need a balance-checked charge; forced battles need the fairness floor) — build them as part of the matrix spec, not before it in a different shape. B10 (route rematches) is deferred outright; C4 (spotter stands) sequences after. A11's Mio warning line stays valid regardless — the matrix makes it *more* necessary.
2. **Tower-optional `_weak` variants (shipped directive) vs A4 and C3.** No conflict, one synergy and one spec question: ghosting Performance Review now means fighting full-strength Cicada (keep — it strengthens the ghost fantasy; worth one line in the guide's house-rules text), and B-teams must decide their interaction with `_weak` files before authoring (C3).
3. **HZ pass (Mei Lin recast, Aya leader, scrip, granary leak) vs A12-stalls / A11-Guo / B3 / B8 / C1.** The pass wins on sequencing: Mei Lin's stall line is authored against the stop-3 dialog block (never the uuid); the Guo pointer and Rong wager ride the pass; the `hz_nurse` fee-button touch for B3 lands in the same edit as the recast; C1 must land *inside* the in-flight trader task.
4. **Narrative Tier 2/3 rewrites vs randomness say_ref work and A12 riders.** The planned townsfolk-conspiracy rewrite wins; the randomness lens's say_ref-to-register wiring is a *refinement inside it*, not separate work. Grunt template variants are already planned — correctly not re-proposed by any lens. The Ume↔Imani lines are single additive entries so the planned Ume-decision rewrite can absorb them.
5. **In-flight eavesdrop feedback lines vs A14 miller logging.** Distinct systems, no conflict — but use the same actionbar house style so the two ship looking like one pattern.
6. **Lens disagreements, adjudicated:** Kazuo 19/20 (flow) over 18/19 (challenge) — shipped KYC precedent decides. Charge macro (challenge) and stake macro (stream) — one function, both jobs (B2). Census callbacks on Femi (stream) vs Kazuo (cohesion) — both, they're different beats on different forks (A2). Flow's `ci_papers` (unfiled-held) vs cohesion's (filed-count) — two scores, one tick function (A7). Adjunct Faculty: cohesion's text-rewrite-as-erasure wins as the core fix; stream's Raan trim and joint beat ride as optional (A13).
7. **Already shipped — never re-propose:** derby begin-baseline, cascade day-latch + Shou subtitle, sprint 120s/100s, HZ warden/jr + KYC Femi retunes, tier-0 shop cut, granary `rctmod:` prefix. The challenge lens's warnings about the quest DB being stale on KYC/wardens/sprint are confirmed honored — no Tier A/B item above touches those.

**Suggested execution order:** A1–A16 in listed order (one PR-sized batch each for A1–A7, bundles A11–A14 can share a recompile); B1+B2 written into the matrix spec immediately; C1 injected into the in-flight trader task *today*; B3–B9 on showrunner sign-off; B7/C3/C4/B10 after the matrix and `_weak` work stabilize.
