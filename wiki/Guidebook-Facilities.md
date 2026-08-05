# Guidebook: Facilities

Three optional, single-player facilities — each a self-contained loop you opt into. None of
them can end your run by themselves (see the per-facility notes), but they all charge
**CobbleDollars**. The Stadium and Daycare are run by The Company, Inc., with the Company
voice exactly as warm as you'd expect; the Preserve starts out Company-occupied — see below.

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

## The Safari — the Ridgewatch Preserve

The map calls it the **Safari Zone**; the family that runs it calls it the **Ridgewatch
Preserve**. When you first arrive (any time after **Badge 3**) the Company is squatting the
grounds. **Ranger Nova Circuit** at the east fence runs the **clear-out**: deal with the
Company guns on the fence line, report back, and the Preserve reopens under its owner,
**Helga Ridgewatch** — **Darik** on the intake kiosk, **Varek** briefing the rules.

- **Rounds, not visits.** A round is **500 CD** at the posted rate. At the gate the Preserve
  takes **custody of your items *and* your party** — inside you carry only the round kit:
  **10 Preserve Balls, 6 bait, 16 snowballs** — against a **clock of about three minutes**.
  Your gear and your partners come back at the bell, plus whatever you caught; the kit does
  not, and **bait is Preserve property** — spent in the round or surrendered at the gate.
- **You don't battle here.** **Crouch** and work through the **grass** — they bolt the moment
  they spot you. **Scatter bait** to lure them out to investigate (warm spots build where
  you've had luck), **offer bait** to the one in front of you to calm it into forgiving a
  wobbly throw, and a soft **snowball** hit takes the fight out of a catch without starting
  one. (Party in custody + no battles = **hardcore-safe by construction**.)
- **Placement matters.** Skewers and grit go on **open ground** — but the **honey smear is
  smeared on tree bark**, and the bug prizes come down the trunk for it. And the Preserve is
  **dry ground**: there's no water inside the fence and **no fishing** — every catch here
  comes to bait.
- **Some of them live nowhere else.** Several kinds keep to the Preserve — you won't run into
  them in the wild anywhere outside the fence. If one's on your list, book a round.
- **A round ends when the clock runs out or the last ball flies.** Two round types at the gate:
  - **Capture round** — what you catch is yours to keep.
  - **Contest round** — your catches are appraised at the bell and released home to the
    Preserve; you're playing for the score, not the keeps.
- Lifetime catch milestones still pay out packs — the ledger survives between rounds.
- `/cobblemon-initiative safari enter capture|contest`, `status`, `exit` — see [[Commands]].

> The kit is stamped **"Property of the Ridgewatch Preserve."** — it doesn't leave the
> grounds, and neither does anything you *don't* catch before the clock runs out.
