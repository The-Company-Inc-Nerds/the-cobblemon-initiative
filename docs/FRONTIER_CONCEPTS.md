# Battle Frontier — Design Concepts (2026-07-13)

Design notes for turning the shipped-but-flat Battle Frontier (8 facilities, all currently
"walk up → click battle → level-100 fight") into a set of distinct, stream-fun set-pieces.
The teams + placements are already shipped and champion-gated; this is the **gimmick +
stakes layer** on top.

---

## 1. The stakes model — "keep the danger, drop the attrition"

**Showrunner ruling (2026-07-13):** the Frontier should **keep death risk** but **not permanently
forfeit Pokémon** — it is the endgame *sandbox*, not another attrition grind.

Resolve it as a **wager, not a graveyard**:

- Inside a Frontier facility, a faint does **not** release the Pokémon, and a whiteout does **not**
  end the hardcore run. (Mechanically: the same `NuzlockeInit` guard the Stadium already uses —
  `isStadiumActive`-style flag — extended to frontier battles, so faint-release and the whiteout
  kill are suspended while a facility run is active. Your team heals on exit.)
- The danger is the **wager and the streak**, not your box. Every facility run costs an **entry
  stake** (CobbleDollars, or a "Frontier Pass"), and a whiteout / loss **forfeits the stake, the
  streak, and that run's prize**. You lost the *bet*, not a partner.
- This makes each facility infinitely **retryable** (good for a grind sandbox and for a streamer
  who wants to keep trying a hard brain) while still stinging to lose — you're out the buy-in and
  the progress, and the brain's taunt lands.

> This also finally makes the shipped "House rules: my team, not yours — nothing you love dies on
> this floor" flavor **true**. Keep those lines. (Today they are a dangerous lie; see the review.)
> The one exception is the **Factory**, which leans into "the Frontier lends the team" literally
> (rentals — see below).

**Engine note:** this needs one Java pass — a `FrontierManager` mirroring `StadiumManager`
(detect a facility run, set the guard flag, clear on exit) — and it must be **runtime-verified**
before shipping, because it touches the run-ending death path. Until it ships, the champion gate +
the wiki hardcore warning are the interim guard.

---

## 2. Per-facility concepts

The eight shipped facilities + brains, each with a signature gimmick that fits its canon flavor and
reuses engines we already have where possible.

### 🗼 Battle Tower — Tower Tycoon Palmer *(Dragonite / Milotic / Rhyperior)*
**The Climb (streak + greed).** The classic escalating ladder: two challengers, then Palmer, each
win compounding the purse. After any win you may **bank and walk** or **push for the next rung at
double-or-nothing**. Palmer at the summit is the cash-out. Pure nerve — the tension is *when to
stop*, and it's the "default" facility that teaches the wager model.

### 🏰 Battle Castle — Castle Lord Percival *(Empoleon / Slowbro / Dusknoir / Steelix)*
**The Ledger (resource management).** You enter with a fixed "war chest." Between rounds you can
**spend** it on a heal, a held-item swap, or **intel** on the next foe's lead — but every purchase
comes straight out of the prize you'll walk away with. Win frugally for a fat payout; over-spend and
you win but leave broke. Percival's bulky stall team punishes the impatient — the facility *about*
patience.

### 🏭 Battle Factory — Factory Head Noland *(Magnezone / Scizor / Metagross / Porygon-Z)*
**Rental Roulette (borrowed teams — literally).** The one facility where you **do not use your own
team at all** (so it's genuinely risk-free by construction). You're handed a **random rental trio**
and must win with unfamiliar Pokémon; after each round you may **swap one of your rentals for one
the opponent just used**. A pure test of type knowledge and adaptation — and the honest home of the
"the Frontier lends the team" line.

### 🎰 Battle Arcade — Arcade Star Dahlia *(Blaziken / Togekiss / Garchomp / Zoroark)*
**The Wheel (roulette modifiers).** Before each round a roulette spins a **random battle modifier**
applied to the field — weather, a stat stage to one side, a random status, halved HP, item disable,
gravity, trick room. You adapt to the roll live; chat calls the spin. Dahlia's Zoroark leans into the
chaos with disguises. Reuse the noble `NobleSkyFx`/rules plumbing for the weather/effect swings.

### 🕳️ Deep Dark Cave — Cave Warden Selene *(… Darkrai; + Giratina)*
**The Descent (a real deep-dark dungeon).** Not an arena — an actual **gigantic sculk cave**: ore
veins to mine for one-use supplies, ambient sculk everywhere, and a lurking **Warden** that hunts by
sound (creep, don't sprint — a genuine deep-dark stealth hazard, reusing the dark-gauntlet blindness
where useful). Selene guards the dark with Darkrai's sleep. The set-piece payoff waits in the deepest
chamber: **Giratina** — a distortion-world legendary that fits the ghost-dragon dark, run as a
Legends-Arceus-style **noble encounter** (dodge/wear-down → real catchable) on the shipped `noble/`
engine, so beating Selene isn't the end of the hall — the *cave itself* is the prize. The most
**dungeon** of the eight: exploration, stealth, mining, and a legendary at the bottom.

### 🔺 Battle Pyramid — Pyramid King Brandon *(the three Regis + Regigigas)*
**The Shifting Maze (disorienting ascent).** A big multi-level **maze** you climb to reach the top —
and it refuses to hold still. Stepping on the wrong tile **randomly teleports** you elsewhere in the
maze, and the **battle nodes shuffle position** run to run, so nothing is memorizable — you navigate
by wits, not by learning the route. Scattered nodes trigger the three Regi-guarded fights and drop
one-use supplies; **you "progress" by working your way up to the top**, where Brandon + Regigigas are
the payoff. (Teleport pads and shuffled encounter/loot spawns are function-driven; reuse the shrine
parkour / dark-gauntlet plumbing for the maze body.) The most **adventure** of the eight.

### 🏪 Battle Market — Market Mogul Sterling *(Zoroark / Chansey / Alakazam / Porygon-Z)*
**The Auction (a con-artist fight).** Sterling is a grifter. Before and during the fight he **offers
deals** — a free full-heal if you surrender your held item; a stat boost if you lock out a move;
intel for CobbleDollars — and some are **traps** (his Zoroark is not always where he says). Read the
mark, take the good deals, refuse the cons. Chansey stalls while he talks. A social/economy duel that
rewards suspicion — very on-brand for a Company facility.

### ⚓ Battle Port — Port Admiral Horatio *(Pelipper / Gyarados / Kingdra / Lugia)*
**Storm Doubles (chaotic doubles).** A **doubles** gauntlet on open water: the weather **shifts every
few turns** (rain → hail → calm → rain), and periodically a **wave** forces a random switch on both
sides — you never quite control the board. Horatio's rain-abusers (Pelipper drizzle, Kingdra swift
swim, Lugia overhead) ride the storm. Reuse the doubles format the gym-4 finale already uses
(`GEN_9_MULTI`) plus the weather plumbing from Arcade.

---

## 3. Build feasibility / reuse map

| Facility | Reuses | New work |
|----------|--------|----------|
| Tower | Stadium wager/streak pattern | streak counter + double-or-nothing UI |
| Castle | CobbleDollars pay-probe + shop | between-round purchase menu |
| Factory | — | rental-team assembly + swap (biggest new system) |
| Arcade | noble `NobleSkyFx` / TBCS `rules` | roulette roll → modifier map |
| Cave | dark-gauntlet blindness + `noble/` engine (Giratina) | sculk-cave build, Warden hazard, Giratina noble |
| Pyramid | shrine parkour + function `tp` | shifting maze: random-tp pads + shuffled node spawns |
| Market | dialog deal-offers + Zoroark | trap/deal resolution |
| Port | gym-4 `GEN_9_MULTI` doubles + weather | wave-switch every N turns |

**Common prerequisite for all eight:** the §1 `FrontierManager` no-death/wager guard (Java, needs
runtime verify). Ship that first; then facilities can land one at a time — start with **Tower**
(smallest, teaches the model) and **Factory** (the risk-free rental showcase), which together prove
both halves of the stakes model.

> Tracked in TODO. None of this is on the 1.0 critical path — it's the post-Champion sandbox — but
> it's the content that makes the 85→100 window a *destination* instead of a grind.
