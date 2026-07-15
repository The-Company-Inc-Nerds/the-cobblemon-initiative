# Guidebook: Facilities

Three optional, single-player facilities run by The Company, Inc. — each a self-contained
loop you opt into. None of them can end your run by themselves (see the per-facility notes),
but they all charge **CobbleDollars**, and the Company voice is exactly as warm as you'd expect.

> [!NOTE]
> All three are commands as well as NPCs — the clerk at each site just runs them for you. The
> player-facing subcommands are in **[[Commands]]**. Coordinates are being finalized in-world;
> ask the site clerk if you can't find a facility yet.

> These three run **throughout** the campaign. The post-Champion **Battle Frontier** — its eight
> level-100 halls and Frontier Brains — is a separate endgame set; see **[[Guidebook Battle Frontier]]**.

---

## The Stadium — Exhibition Circuits (Cyber City)

A wager arena run out of **Cyber City**. Register with the **Exhibition Registrar** and pick a
bracket; the Company fields five exhibition waves of its own trainers against you.

- **Brackets:** 25 / 50 / 75 / 100 — the number is the **level every Pokémon is locked to** for
  the run (yours *and* theirs), so a bracket is a clean, level-flat test regardless of where you
  are in the campaign.
- **No permadeath here.** Stadium battles fight **cloned copies** of your party and the Nuzlocke
  faint/flee/whiteout rules are **suspended for the duration** — you can lose a wave without losing
  a Pokémon or the run. This is the one place in the game you can spar without stakes.
- **Purses:** each wave you clear pays a flat purse (≈200 → 1000 CD across the five), plus a
  completion bonus for taking the whole circuit. A `stadium_challenged` counter tracks your runs.
- `/cobblemon-initiative stadium start 25|50|75|100` · `status` · `abort`.

## The Daycare (Gaviota Port)

Two custody slots run by the **Daycare Keeper** at **Gaviota Port**. Board a Pokémon and it trains
itself while you travel.

- **Deposit** opens a party picker (multi-select, up to 2; it will never take your last Pokémon).
  Boarded Pokémon appear as **stand-ins** in the pen — real, but un-battleable and un-catchable.
- **XP drip:** a boarded Pokémon gains a trickle of experience over time, **clamped at your current
  level cap** (it will never out-level the ladder for you).
- **Withdraw fee:** 100 CD + 100 per level gained. If both your party and PC are somehow full, the
  fee is refunded and the Pokémon stays boarded. Withdrawn Pokémon route to your party, or your PC
  if the party is full.
- Custody survives relog. `/cobblemon-initiative daycare deposit | withdraw <slot> | status`.

## The Safari — "The Baiting Yards" (a Company Preserve)

A lure-and-catch minigame that opens after **Badge 3**. Buy a **Day Permit** (1,500 CD) from the
**Preserve Intake Concierge** and you're issued **20 Preserve-Issue Safari Balls** and a **15-minute
clock**.

- **You don't battle here.** Scatter typed **bait** to draw species, wait through the suspense, and
  **throw balls** at what appears — your party stays holstered the whole visit. (This makes the
  Safari **hardcore-safe by construction**: nothing in the Yards can faint your team.)
- Bait comes in five types from the kiosk, each pulling a different typed table (roughly Lv 25–35).
  Warm spots build where you've had luck.
- **Exit** returns your unused balls (clawback) and prints a **catch ledger** — on a stream, the
  end-of-visit "which one do we keep?" chat vote is the whole point. Lifetime 10/25-catch milestones
  pay out packs.
- `/cobblemon-initiative safari enter | status | exit`, plus `safari bait <type>`.

> The Preserve-Issue balls are **"Company property. Non-transferable."** — they don't leave the
> Yards, and neither does anything you *don't* catch before the clock runs out.
