# The Cobblemon Initiative — Lore Bible

The single source of truth for the narrative. Everything player-facing — trainer
flavour, NPC dialogue, memory fragments, Dark Urge whispers, economy text,
achievement names — should be consistent with this document. When in doubt, the
rule is: **the protagonist does not know who they are, but the world does.**

This is a **live Twitch + YouTube production**, played **hardcore + Nuzlocke**. Beats
should read on stream and reward a long-form audience that remembers earlier hints.

---

## 1. Logline

An amnesiac walks a fixed gym route through a curated land, slowly recovering the
memory that they are the founder of **The Company, Inc.** — the very organisation now
strip-mining the region's trust to seize its economy. The journey to "become the
champion" is, underneath, a man unknowingly walking back toward his own throne, and
toward the shadow of who he was. The final battle is a mirror.

Inspirations: the **Dark Urge** (Baldur's Gate 3) for the protagonist; **Pokémon Red**
for the final mirror battle.

---

## 2. The Protagonist

- Former **CEO / founder** of The Company, Inc. They **built the CobbleDollar economy** —
  designed the trust system that the whole region now runs on.
- They were **usurped, left for dead, and stripped of memory.** They wake with nothing
  and take up the gym challenge like any trainer would.
- They do **not** know any of this. The audience figures it out before the character does.
- **The shadow self:** a colder version of the protagonist — the founder who treats
  people as line items — still lives in them. It speaks on Pokémon faints (the Dark Urge
  whisper system). It is the voice of who they were, and a preview of **The Founder**.

Writing the protagonist's internal voice (memory fragments, Dark Urge): first person,
spare, unsettled. Early = confusion and unease. Late = dawning, dreadful recognition.
**Never** state "you are the Founder" outright before the post-Royal-League beat.

### Mom (Sango Town) — the quiet tragedy
- They were **slightly estranged**. The founder compartmentalised everything: Mom knew a
  distant, evasive kid who never explained the work, never wrote, never brought anyone
  home. She has **no idea** her child ran The Company — and **she never finds out.**
- The amnesia is invisible to her at first — her kid was always vague — but she notices
  the *wrong* gaps: they do not remember the kitchen, the recipes, her birthday. She
  decides not to push. That choice is the tragedy.
- **The arc she does get:** across the run, the visits keep happening. The kid who never
  wrote now shows up between badges. By the end, **she gets her child back into her
  life** — not the CEO, not the Founder, just her kid at her table. She never learns who
  they were out there, and in her kitchen it does not matter.
- Writing her: warm, unhurried, a little guarded early. Never expository. She is the one
  room in the story where the plot is not allowed in.

---

## 3. The Company, Inc.

The villain organisation **shares its name with the real production company on purpose** —
a deliberate meta-joke that the audience is in on.

### What it is
Not a crime syndicate — a **business**. The Company is the trusted
**third-party verifier** of the region's currency. It is the auditor / central bank, the
neutral party everyone relied on to keep the money honest. That trust is its weapon.

### The economy (get this right — it's the engine of the plot)
- **CobbleDollars** are the region's currency, **backed by nether stars**.
- Each nether-star reserve is recorded in **double-signed ledgers** — and **The Company
  verifies those signatures.** They are the reason anyone trusts the money.
- **The plot:** The Company weaponises that trusted position. By tampering with what they
  "verify," they **destabilise CobbleDollars** — values drift, prices feel *off*, payouts
  come up short, the money feels less and less real (mechanically: the `cd_instability`
  index, 0–100, rising across the gym journey).
- **The replacement:** into that engineered chaos they push a **wheat-backed currency they
  alone control** — because **they occupy and monopolise the wheat fields.** Whoever owns
  the new commodity owns the region.
- This is a deliberate nod to the **YouTube community debate about the best item to anchor
  a Minecraft economy** (diamonds vs emeralds vs nether stars vs wheat, etc.). The Company's
  answer is "whatever *we* control."

The protagonist built this system to control it, now he is part of this system. The Company is corrupting their
creation. Reclaiming the org is reclaiming the economy.

### Player-facing economy voice — three registers (gate on `cd_instability` / villain progress)
- **Act 1 (stable, idx ~0–24):** glossy corporate cheer. "The Company: Verified Trust,
  Verified Value." Propaganda confident and warm.
- **Act 2 (slipping, idx ~25–60):** nervous reassurance. Spokes-NPCs over-explain why
  prices are "adjusting." Wheat traders appear offering an "alternative."
- **Post-HQ / late (stabilised then exposed):** the propaganda **corrupts** — slogans
  glitch (`§k`), and the cover-up leaks. "We told them the founder retired."

---

## 4. The Villain Structure (three acts)

| Act | When | Content |
|-----|------|---------|
| 1 — Infiltration | Throughout gyms 1–7 | Grunts & management scattered on routes/towns; wheat traders appear; the CobbleDollar visibly destabilises (`cd_instability` rises ~+8/gym). |
| 2 — The HQ Raid | After ~gym 7 (HQ at `[1590 51 1028]`) | Fight up through the org to **Acting CEO DJ**. His defeat **stabilises the currency** (idx → 25): a visible, earned "CURRENCY STABILIZED" beat. |
| 3 — The Board & The Founder | Post-Royal-League | Clear the **Board of Directors**, then the true final boss: **The Founder** — the player's shadow self. |

### Roster (from `villain_team.json` — names are canon)
- **Grunts:** Field Agent → Contractor → Operative → Compliance Officer / Market Analyst →
  Senior Agent → Elite Agent (escalating corporate-ladder titles; gym-gated).
- **Management:** Regional Manager Shade → Senior Director Vex → **COO Noir**.
- **Act-2 boss:** **Acting CEO DJ** (`villain_boss`). *("DJ" is canon — earlier drafts/docs
  said "Midas"; that name is retired.)* He is **acting** CEO — a usurper keeping the seat
  warm, not the true power.
- **Board of Directors:** four members with `§k`-obfuscated names (Madeline, Matt, Micah,
  Lauren under the static). Post-Royal-League gauntlet.
- **The Founder** (`villain_final_boss`): name shown as `§k` static until earned. The
  player's shadow self. Beating them grants `company_overthrown`.

### The scrubbing (why nobody recognises the founder) — canon
The founder **was a known public figure** — portraits in every branch office, a signature
on every charter, ribbon-cuttings, the face of "Verified Trust." Non-recognition is not
because he was hidden; it is because **the coup is actively erasing him**:

1. **The erasure.** After the usurping, DJ and the Board scrubbed the record — portraits
   taken down, ledger signatures re-verified under new names, org charts revised, the
   story managed in stages: *"the founder retired"* → *"there was never a founder."*
   (Already seeded in the corrupted propaganda register.) The scrubbing is **incomplete**
   — old-timers remember the face, deep offices still have the portrait — and that
   incompleteness is what the recognition arc runs on.
2. **The slander.** Internally, the Company brands the interfering amnesiac trainer a
   **saboteur / impostor** — DJ's memos warn of "a face from the old company." Low-rank
   grunts never met the founder and believe the memo: they are **angry**, not reverent.
   The higher someone sits (or the longer they served), the more likely they knew the
   real face — and the worse the moment when they place it.
3. **The amnesia.** The protagonist cannot assert an identity he does not remember. The
   world forgot him as he forgot himself; the run is both remembering at once.

**The gradient (rank × proximity, gated by badge/liberation progress):**
- Grunts (early): the memo made flesh. Confused hostility. "You match a description we
  were told to forget."
- Veterans / management (mid): alarm — they saw the portrait come down. "You are supposed
  to be *filed*."
- Late: some **stand down** rather than raise a hand against the founder; others panic
  and double down on the official line ("there was never a founder — so you are no one").
- The wheat traders / Granary keepers, after enough fields are liberated, **recognise him
  mid-trade and turn hostile** — commerce curdling into the cover-up.
- **Civilians never recognise him** (the public scrubbing worked); they only know the
  propaganda decayed. Mom recognises her *kid* — never the CEO.

---

## 5. The Founder (the mirror)

The climax is a **Pokémon-Red-style mirror battle** against the player's shadow self. It
should feel like fighting *yourself*, not slaying a monster. The "it was you all along"
reveal is the payoff of the whole amnesia arc — held back until **after the Royal League /
Board clearout**. **Implemented:** the Founder's nameplate is **fully `§k`-obfuscated for
the entire run** (`§kfounder` — no letters ever surface early, and no name is baked into
any shipped file). Each Board member's defeat fires `reveal/board_fell` — four oblique
title beats that circle the name without closing it. The name is only spoken at the
mirror's defeat: `reveal/founder_defeated` renders **the defeating player's own name
live** via a selector component — *"The name on the chair was always ⟨you⟩."* Whoever
wins the run, the reveal is theirs.

Aftermath / post-story: with the Company overthrown, the player leaves the curated map for
generated terrain and attempts to **beat vanilla Minecraft (the Ender Dragon)** — still
hardcore + Nuzlocke. The reclaimed founder, finally themselves, walks into the unknown.

---

## 6. Themes & Tone

- **Reclamation, not heroism.** The protagonist isn't an outsider saving the land — he's
  the inside man taking back what he built and lost. Morally smudged.
- **Trust as a weapon.** The horror is financial: the institution everyone relied on is
  the threat. Verification becomes violation.
- **The cost of treating people as inventory.** The Dark Urge whispers are the thesis: the
  founder's old logic ("assets fail, you replace them") vs. the Nuzlocke griever the player
  has become. Every fainted Pokémon is the argument.
- **Corporate-dread comedy.** Titles, slogans, and HR-speak played straight against
  genuine menace. The Company name gag keeps it self-aware.

---

## 7. How the Systems Carry the Lore

| System | Narrative job |
|--------|---------------|
| **Level caps** (badge-gated) | The disciplined ascent; the world's level matches the journey's stakes. |
| **Nuzlocke + Dark Urge whispers** | Permadeath gives the shadow-self something to comment on; the founder's cold voice vs. the player's grief. Tiered by progress (tier 3 only post-gym-8). |
| **Memory Fragments** (per badge) | The amnesia drip. 10 first-person flashes, vague → "you signed this charter" (gym 7) → "face your own signature" (gym 10). The reveal itself waits for Act 3. |
| **Wheat War economy** (`cd_instability`) | The plot you can *feel in your wallet* — money destabilises across Act 1, stabilises when you topple Acting CEO DJ. |
| **Field liberation** (Wheat War) | Physically pushing back the monopoly: take fields → restore wheat prices, win safe-farm ground, advance the HQ gate. |
| **Wheat traders** | The alternative currency made flesh; the recognition arc made interactive (trade → recognise → ambush). |

---

## 8. Narrative Beat Map (gym by gym)

| Gym | Town (type) | Lvl cap | Memory fragment | `cd_instability` | Notes |
|-----|-------------|:------:|-----------------|:----------------:|-------|
| 1 | Takehara Falls (Bug) | 30 | frag_1 — formless unease | → 8 | First grunts; propaganda is glossy. |
| 2 | Hua Zhan City (Grass) | 38 | frag_2 | → 16 | Wheat country; traders begin. |
| 3 | Mystic Marsh (Fairy) | 45 | frag_3 | → 24 | End of "stable" feel. |
| 4 | Deepcore City (Fighting) | 52 | frag_4 | → 32 | Prices "adjusting"; traders recognise you (≥2 fields). |
| 5 | Gaviota Port (Water) | 58 | frag_5 | → 40 | |
| 6 | Kalahar Reach (Ground) | 63 | frag_6 | → 48 | Traders may ambush (≥4 fields). |
| 7 | Cyber City (Electric) | 68 | **frag_7 — "You signed this charter."** | → 56 (peak) | The hard turn. HQ raid unlocks. |
| — | **HQ Raid → Acting CEO DJ** | — | — | **→ 25** | "CURRENCY STABILIZED." Dark Urge reaches tier 3. |
| 8 | Ryujin Keep (Dragon) | 73 | frag_8 — "You built it." | 25 | |
| 9 | Nifl Town (Ice) | 78 | frag_9 — "They emptied you." | 25 | |
| 10 | Scorchspire (Fire) | 85 | frag_10 — "face your own signature" | 25 | League stands between you and the answer. |
| — | **Royal League → Board → The Founder** | 100 | — | — | The mirror. `company_overthrown`. Real name spoken. |

*(Instability values assume the player hasn't yet liberated fields, which push the index
back down — the tug-of-war. Numbers are the current tuning, not gospel.)*

---

## 9. Continuity Rules (do / don't)

- **DO** let NPCs recognise the protagonist with escalating intensity.
- **DO** keep CobbleDollars feeling subtly broken in Act 2 (it's the plot, not a bug).
- **DON'T** name the protagonist as the Founder before Act 3. Fragments 7–9 circle it;
  they never close it.
- **DO** swing the native CobbleDollars shop prices — and grow its stock — across the journey.
  The mod ships a per-badge shop catalog (`scripts/shop_tiers/master_shop.json` →
  `cobbledollars_tiers/*.json`); each gym leader and Acting CEO DJ fire a
  `cobblemon-initiative shop <tier>` reward that overwrites `config/cobbledollars/default_shop.json`
  and runs `cobbledollars reload`, which updates the live shop GUI (verified against CobbleDollars
  2.0.0+Beta-5.1). Prices climb with `cd_instability` (≈+4%/gym, peak +28% at gym 7) and ease to
  +12.5% after DJ falls. New item categories unlock as badges are earned, Pokémart-style.
- **DON'T** promise the shop prices drift *continuously* with the instability index, or that they
  swing on a *custom-shop* merchant. The swing is **stepped** between authored tiers (config files
  can't be written by datapacks, only by the mod at badge boundaries), and the live reload only
  reaches merchants using the **default** shop. The fine-grained instability is still sold via the
  per-payout rate line, the per-gym actionbar, and (later) a town exchange board.
- **DON'T** call the name the act-2 boss until act-2, It's **DJ**.
- **DO** litter the world with **scrubbing artifacts**: brighter rectangles where portraits
  hung, re-verified ledger pages, revised org charts, memos about "the impostor." The
  erasure being *visible* is how the audience reads the cover-up before the reveal.
- **DON'T** ever let Mom learn the truth. No NPC tells her, no letter, no overheard scene.
  Her arc resolves on *presence* (her kid keeps coming back), not *revelation*. If a line
  gestures at it, she deflects it — deliberately or obliviously, writer's choice per scene.
- Macro-delivered text (memory fragments, economy lines) must contain **no double-quotes**
  and avoid apostrophes — the datapack macro layer has no escaping.
