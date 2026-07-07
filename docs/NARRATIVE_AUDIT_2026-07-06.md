# Narrative Audit — "Are we working FOR The Company?" (2026-07-06)

Showrunner's fear (smoke round 4): *"every NPC and quest makes it seem like we are working
for The Company versus against it, and everyone seems to be part of it or know about them —
they should be a secretive org pulling the strings in the background."*

Audited: every speaking NPC + quest in Sango / the routes (Blossom Path, Harvest Road) /
Takehara, plus lore, registers, act-1 villain cast, and all system-level player-facing
strings. 4-agent sweep + synthesis; quotes verified verbatim against dialog-src.

## Verdict — justified, but it is TWO problems, and one of them was a single function

**Problem A — "everyone knows about them": CONFIRMED.** Of 87 audited speaking NPCs
(Sango 36 / routes 27 / Takehara 24): 20% open Company agents, 24% aware-and-resenting,
16% overt references, 17% subtle hints, only 23% genuinely oblivious (mostly Pokémon and
gym trainers, not townsfolk). 161 player-visible "Company" mentions across 72 dialog
files. Six open Company staff operate in the STARTING town; both elders narrated the full
founder-erasure conspiracy to a day-one player ungated.

**Problem B — "we work FOR them": JUSTIFIED IN PRESENTATION, NOT IN DESIGN.** Only 4/87
NPCs actually pay the player Company money, and 3 of those are deliberate
villain-temptation forks (census sign, courier sell, Invitational purse). But
`economy/pay_macro` stamped **"Company Verified Rate"** on EVERY payout in the game
(20+ civilian quests) — one function converted a ~10% designed employment rate into a
~100% perceived one. Add "The Company does not extend credit" on failed heals, "the
Company's ledgers waver" on stability ticks, "• Tour the Company greenhouse" in the
sidebar, "Company checkpoint ahead" narrator warns, and the `[The Company, Inc.]`
first-join letterhead at minute zero.

## Five structural causes

1. **Single Company-branded payout rail** (the big one — fixed in alpha.18).
2. **Villain bodies self-identify out loud** — the shared `grunt_recognition.json`
   template opens nearly every act-1 encounter with the literal words "Company business";
   four act1.json battle displayNames blew covers the dialog carefully built.
3. **Ungated townsfolk defaults volunteer conspiracy talk** (Dakarai, Kele, Fara, Marlow,
   Sefu, both elders) before any quest.
4. **Open-agent density in the opening zones** (census desk + 2 patrolling auditors
   permanent in the Sango square; 4 agent presences in Takehara).
5. **Named-Company grievance monologues on ordinary NPCs** (Nurse Lila, Tomo ~5x, Genji
   3x, ambient Combee) — the target register ("the polite men", "head office", never the
   brand) is already achieved by Masumi, Mayor Liang, and the Deng camp.

## Shipped in 0.4.3-alpha.18 (Tier 1 — system text + cheapest swaps)

- `economy/pay_macro` default receipt → unbranded **"Verified Rate N%"**. New
  `economy/payout_company` (branded) fires ONLY at the four deliberate sites: census sign
  fork, courier sell fork, Invitational purse, Adjusted Retail (the attribution moment).
- First-join message: letterhead dropped ("This world has been provisioned. Welcome back
  to the ledger." — same chill, no brand at minute zero).
- Stability tick → "the ledgers waver." · Heal decline → "The Center does not extend
  credit." · Sidebar → "• Tour the Verified Growth greenhouse". · Blossom Path narrator →
  "A checkpoint ahead." (the tent, slogans, and Memo 44-C do the attribution).
- Battle displayNames de-branded with synchronized team-file renames: "Site Assessors",
  "Survey Canvasser" ("Company Canvasser" kept — posting branded notices IS his job).
- Kofi's barker line drops "verified by The Company, Inc"; Nurse Lila resents the fee
  without naming its source ("the invoice arrives pre-verified").

## Tier 2 — proposed, NOT yet applied (showrunner call)

1. **Blossom Path checkpoint (Sani + Haruki)** → rebrand as the route arm of the existing
   "Resident Verification Drive" front; kill the shared "Company business…" opener; fee
   actionbars → "Your fee has been processed with verified gratitude." Memo 44-C eavesdrop
   becomes the moment the name first attaches to the tent.
2. **Gate the Sango square occupation** — auditors spawn on quest activity; census desk
   packs up after `census_processed`; liaison Tunde stays as the ONE public Company face.
3. **Neutralize ungated defaults** (Dakarai, Kele, Fara, Marlow) — move grey-suit lines
   into quest-gated entries.
4. **Gate `sango_lore` founder pages** behind `badges>=1` or `met_lucian`.
5. **Ume (Head Count)** — either recast the funder faceless ("The client pays per verified
   data point — do not ask me which client.") or keep her Company-paid as a deliberate
   temptation and route her payout through `payout_company`. **Pick one.**
6. **Takehara naming diet** (~12 lines, 7 files): Tomo keeps only the seal-three memo
   read-back; Genji 3→1; one-word fixes for Mei/Shou/Kenji/Combee.
7. **Blossom Path thinning**: Sumi drops "Company" before "surveyors"; Tetsu's sign-off
   softened.
8. **`grunt_recognition.json`** → 3-4 per-front default variants (checkpoint / corridor /
   audit / farm voice), keeping the shared hook "Wait. Do I know you from somewhere?" —
   fixes brand-fronting AND on-stream verbatim repeats.

## Tier 3 — optional deeper rework

Takehara agent-density reduction; erasure-prop budget trim; **Hua Zhan canon leaks for the
next pass**: `granary_keeper` ungated default prints "wheat" pre-reveal AND declares "The
Company believes in wheat" (double break of the `heard_wheat_pitch` canon); site manager
Jun's "Everything the Company plans to do, it practiced here" ("→ Everything head office
wants from a field, it learned here."); Survey Wagon → unmarked, brand discovered on the
manifest; `sq_hz_analyst` "Company Market Analyst" rename; minutes `approach_warn`.

## PRESERVE — already on-target, do not flatten

- **The recognition motif is the crown jewel**: Mom's unbranded "grey-suited ones came
  by… I told them I had never seen you" (the tonal gold standard), Acacia's "polite in
  the way a closing door is polite," the KYC sketch beat, Shade's chair monologue, the
  roof suit's memo double-take, Cicada's post-badge "a face from the old days," tiered
  dark-urge whispers, badge-tiered scrubbing propaganda.
- **The prop layer**: brighter-rectangle portrait crate, double-signature ledger, branded
  Revision Notices, the PENDING RE-VERIFICATION plinth, Memo 44-C.
- **Resentment models**: Masumi, Mayor Liang, the Deng camp — resentment without the name.
- **Deliberate employment temptations** (census sign / courier sell / Invitational purse)
  keep the BRANDED receipt as the tell.
- Wheat trader Ping's unbranded grain-evangelist front is the model every front should
  copy. Achievements, shrine flavor, and the rest of the sidebar are clean.
