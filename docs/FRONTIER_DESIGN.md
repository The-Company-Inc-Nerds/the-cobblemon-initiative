# Battle Frontier — Detailed Facility Designs

Companion to [`FRONTIER_CONCEPTS.md`](FRONTIER_CONCEPTS.md) (the pitch + stakes model). This doc
fleshes out each facility into a buildable spec, one at a time. Shared prerequisite for all eight:
the `FrontierManager` no-death/wager guard (see CONCEPTS §1).

**Progress:** 🟢 done · 🟡 drafted, awaiting sign-off · ⚪ not started

| Facility | Brain | Gimmick | Status |
|----------|-------|---------|:------:|
| Tower | Palmer | The Climb (greed streak) | 🟢 |
| Castle | Percival | The Ledger (real-CD attrition) | 🟢 |
| Factory | Noland | Rental Roulette (true rentals) | 🟢 |
| Arcade | Dahlia | The Wheel (modifiers) | 🟢 |
| Cave | Selene | The Descent (built cave + Giratina) | 🟢 |
| Pyramid | Brandon | The Shifting Maze | 🟢 |
| Market | Sterling | The Auction (con) | 🟢 |
| Port | Horatio | Storm Doubles | 🟢 |

---

## 🗼 Battle Tower — "The Climb" — Tower Tycoon Palmer

*Team: Dragonite / Milotic / Rhyperior (L100, the Platinum homage).*

### Fiction
The Tower is the Company's showpiece and the **one honest hall** in the Frontier — Palmer built it
as a real competitor's proving ground, not a hustle. No cons, no rigged wheel, no fine print: just
you, a team, and a ladder that never ends. It's the facility that teaches the whole Frontier — **bank
your winnings, or bet them on the next floor.**

### The loop — endless streak, bank-or-climb
1. **Register** at the base desk (a Registrar NPC). Pay the entry **stake** (e.g. 500 CD *or* 1
   Frontier Pass). Your party is **cloned** for the run — nothing you love dies (the wager model).
2. **Fight** an opponent (single, L100). Win → your **purse** grows by a compounding step and your
   **streak** ticks up.
3. **After every win, choose:** **Bank** (leave, keep the purse, streak recorded) or **Climb** (next
   floor, bigger pot).
4. Every **7th win** is a **Floor Boss** — a tougher, fully-itemed "salaried" team from a curated
   set (weather / trick-room / hyper-offense / stall), so the difficulty keeps changing shape.
5. **Palmer** is the Floor Boss at **milestone streaks** (21, then 49) — the marquee cash-out, and
   the only place his signature reward drops.
6. A **loss / whiteout** ends the run: you forfeit the **current purse + streak** (the bet), but not
   your team. Retry from the base.

### The gimmick — greed
The one decision that defines the Tower is **when to stop.** The purse compounds, so climbing is
always tempting, but a single loss zeroes the run. It's the cleanest expression of "wager, not
attrition": the thing you can lose is the *pot*, and it grows the longer you stay brave. On stream,
every rung is a chat vote — *bank it, or ONE MORE FLOOR?*

### Opponents
A **rotating, randomized pool** of L100 teams, escalating in AI aggression + synergy as the streak
climbs. Seed the pool from the existing frontier-challenger + stadium-wave teams, plus ~12 new
"Tower Regulars." Floor Bosses draw from a small curated themed set.

### Stakes & rewards
- **Stake:** small entry (CD or a Pass) so a run costs something.
- **Purse:** compounds per win (e.g. base 300 CD × a streak multiplier); paid **only** on Bank or on
  beating Palmer.
- **Milestones:** streak 7 / 14 / 21 … drop escalating loot (battle items, rare candies, vitamins),
  and Palmer drops a **signature prize** (open Q below).
- **Records:** a persistent **best-streak** scoreboard — bragging rights and a live stream stat.

### The brain — Palmer
Dragonite / Milotic / Rhyperior at L100, fought at your best — **no gimmick, the pure skill check.**
His pre-fight line respects the climb; his defeat is the Tower's true summit. He's the honest man in
a corrupt building, which is its own quiet character note.

### Stream moments
The bank-or-climb vote every round · the milestone Palmer fights · the greed-punished whiteout at
streak 20 · the 1-mon comeback · the record chase.

### Build notes
The Tower is the **Stadium's big sibling.** `StadiumManager` already provides: cloned parties, the
no-death guard, wave dispatch, purses, and a challenge counter. The Tower extends it with an endless
streak loop, a compounding purse, the **Bank/Climb** fork (a two-button dialog), a Floor-Boss-every-7
cadence, and a persistent best-streak score. **Lowest-risk facility to build first** — it proves the
wager model and reuses the most existing machinery.

### Locked design (2026-07-13 showrunner)
**Two phases, both fought from the ground, all L100:**

- **Phase 1 — The Climb (capped, the completable challenge).** A defined ladder — three sets of 7
  (streak ~21) with themed Floor Bosses at 7 and 14, and **Palmer at the summit (streak 21).** You
  climb from floor 1 every attempt (bank-or-climb along the way); reaching and beating Palmer means
  you have **"beaten the Tower"** — the trophy. **No checkpoints** — a whiteout drops you to the
  ground and forfeits the run's pot, so the climb to Palmer is a real gauntlet.
- **Phase 2 — Endless (unlocks after you first beat Palmer).** The same loop with **no cap**: the
  streak and pot run forever, Palmer recurs as a milestone Floor Boss (49, 77, …), and a persistent
  **best-streak** record tracks bragging rights. Still always from the ground.

**Level:** straight **L100** (champion-gated endgame apex). The Stadium already owns 25/50/75/100
bracketed sparring — no overlap.

**Rewards (all four types, distributed):**
- *Along the Climb* (milestones 7 / 14): **Vitamins** (EV training) + a **Rare Candy** stack.
- *Beating Palmer / the summit* (first clear): a **rare held item** (signature marquee prize) + the
  **"Tower Ace"** cosmetic title.
- *Endless milestones* (streak 49 / 77 / …): escalating **rare held items**, **Rare Candy** stacks,
  and **Vitamins**; the **Tower Ace** title upgrades with your best streak (a live stream stat).
- The banked **CD purse** pays out on every Bank regardless of phase.

---

## 🏰 Battle Castle — "The Ledger" — Castle Lord Percival

*Team: Empoleon / Slowbro / Dusknoir / Steelix (L100) — a deliberate wall/stall team.*

### Fiction
The Castle is where the Company stops pretending: **everything is for sale, even survival.** Castle
Lord Percival runs it like a noble's exchequer — you're handed a purse at the gate, and every heal,
every item, every scrap of intel comes out of it. The stingy walk out richest. It's the mirror of the
Tower: the Tower asks *how brave are you*, the Castle asks *how disciplined.*

### The loop — spend your own coin to survive
1. **Register** + stake. Party cloned (no attrition). Your **budget is your real CobbleDollars** —
   there is no safety-net currency; everything you buy inside comes straight out of your wallet.
2. Fight an **escalating, open-ended gauntlet** — opponents get harder the deeper you go, with
   **Percival recurring as a milestone boss** (every set of ~5).
3. **Between every round, the Castle store opens.** Spend **CD** on:
   - a **heal** (partial or full party restore),
   - an **item** (a held item, or a one-shot battle item for the next fight),
   - **intel** (reveal the next opponent's lead / team / a weakness),
   - or **buy nothing** and push in as you are.
   **Prices RISE with depth** — the deeper you climb, the more each heal bleeds you, so you can never
   simply heal forever; eventually the drain (or the difficulty) outpaces your bank.
4. **Reward scales with depth.** Clearing depth milestones pays escalating **CD** *and* unlocks a
   better **item tier**. But your true score is **net**: winnings minus everything you spent staying
   alive. Push deep for the big prize, or bank shallow and net-positive.
5. **Bank** any time to cash out your current depth's payout (and keep whatever CD you didn't spend).
   A **whiteout forfeits the payout entirely** — you keep nothing, having already burned real CD to
   get there. That is the sting: the Castle can leave you *poorer than you came.*
6. Your Pokémon are never forfeited — only your money and the run.

### The gimmick — how much will you burn to stay alive?
It's a gamble with your **actual bank.** Every heal keeps you alive but eats your profit, and prices
climb as you go, so a deep run can cost more to survive than it pays. The tension is *when to cash
out net-positive* — and Percival's stall team is engineered to drag fights long and milk one more
heal out of you before you reach the payout. On stream, every purchase is a chat argument — *heal and
stay poor, or risk it?*

### Stakes & rewards
- **Budget:** your real CobbleDollars (no safety-net currency).
- **Reward:** depth-milestone **CD payouts + item tiers**; your **net profit** (winnings − survival
  spend) is the real score.
- **Record:** best **net profit** on a run — rewards depth *and* discipline, not just one.

### The brain — Percival
Empoleon / Slowbro / Dusknoir / Steelix at L100 — a deliberate wall, recurring as the milestone boss.
The exchequer who profits from your panic; his stall drags fights long specifically to milk one more
heal before you reach the payout.

### Stream moments
The purchase debates every round · the deep run that ends **net-negative** (the Company *won*) ·
banking shallow-but-rich · the greedy whiteout that costs real money · the record net-profit chase.

### Build notes
Reuses the Stadium wager base + the **CobbleDollars pay-probe / shop** pattern for the store (a priced
dialog menu, each option a pay-probe so a broke player can't over-buy). New: the escalating gauntlet
loop, **depth-scaled prices**, depth-milestone reward tiers, and a **net-profit** tracker. Balance
governor: depth-scaled prices + rising difficulty must ensure a rich player can't trivially heal
forever — the drain has to outpace any wallet eventually.

### Locked design (2026-07-13 showrunner)
Real **CobbleDollars** budget (no run-local currency) · store sells **heals + items + intel** ·
**escalating / endless** gauntlet with Percival as a recurring milestone boss · reward = **CD payout
+ item tier** at depth milestones, scored on **net profit**.

---

## 🏭 Battle Factory — "Rental Roulette" — Factory Head Noland

*Team: Magnezone / Scizor / Metagross / Porygon-Z (L100).*

### Fiction
Noland drew up the rental scheme *"back when the Company still built wings for people to enjoy, not to
bleed."* The Factory is the **honest home of "the Frontier lends the team"** — you fight with borrowed
steel, your own caught Pokémon never set foot on the floor. It's the one hall that is genuinely
**risk-free by construction** (your box literally isn't in the battle), which makes it the purest
**skill + type-knowledge** test of the eight.

### The loop — draft, adapt, win with strangers
1. **Register** + stake. Your own party stays at the door — untouched, un-riskable.
2. You're handed a **random rental team** of unfamiliar L100 Pokémon.
3. **Fight.** Win, and after each round you may **swap one of your rentals for one the opponent just
   used** — the classic Factory draft. Your team is only ever as good as what you've stolen.
4. **Climb the ladder** (a defined set of rounds), winning with tools you didn't pick and drafting a
   better squad from the wreckage as you go.
5. **Noland at the summit** fields the sharpest rentals in the building — beat him and you have
   **cleared the Factory** (the trophy).
6. No team-death risk here at all — the only thing you can lose is the **run + stake** (the wager).

### The gimmick — win with what you're given
Pure adaptation. You don't get to lean on your comfort team; you read matchups live with unfamiliar
mons and make the draft calls (do I steal the threat I just beat, or keep my synergy?). It's the
skill-check counterpart to the Tower's nerve and the Castle's discipline. On stream, chat co-drafts —
*take the Garchomp, drop the Pyukumuku.*

### Stakes & rewards
- **Stake:** entry (CD or a Pass).
- **Reward:** streak-based CD + item milestones (like the Tower, but earned on borrowed steel).
- **Record:** best rental streak — a pure "how good are you *really*" flex.

### The brain — Noland
Magnezone / Scizor / Metagross / Porygon-Z at L100 — a tech/steel specialist. The engineer who kept
the one part of the Frontier that was still *fun*, and drafts a mean rental himself.

### Stream moments
The random draw (groans + hope) · the draft decision each round · winning with a "trash" rental team ·
the run where you steal your way into a monster squad.

### Build notes — ⚠ the biggest engine build of the eight
True rentals are a **committed requirement**: fighting a 3v3 with a team that is NOT the player's
party — temporarily swapping the real party out, loading the 3 rentals in, running the battle, and
restoring on exit (plus the mid-run swap-from-defeated). The Stadium proves party-cloning/level-lock
is possible; this extends it to a full party **substitution**. Build a proof-of-concept **early** to
nail swap-out → restore (the #1 rule: never lose the player's real party, even on a crash/relog
mid-battle), then the draft layer sits on top. Highest engine cost of the eight — but the design is
locked on true rentals, so it's build-it, not decide-it.

### Locked design (2026-07-13 showrunner)
**True rentals — full commit** (party substitution, no fallback) · **3v3** · **swap-from-defeated**
draft (steal one from each beaten opponent) · **capped ladder → Noland** (a clearable summit = the
trophy). No team-death risk; only the run + stake are wagered.

---

## 🎰 Battle Arcade — "The Wheel" — Arcade Star Dahlia

*Team: Blaziken / Togekiss / Garchomp / Zoroark (L100).*

### Fiction
The Arcade is the Company's **entertainment wing** — chance dressed up as a game show, run by Arcade
Star Dahlia with a showman's grin and a con-artist's hands. Before every round a great **roulette
spins** and drops a random rule on the field. You don't get to plan; you get to *react.* Dahlia's
Zoroark is right at home in the confusion.

### The loop — spin, then survive it
1. **Register** + stake. Party cloned (no attrition).
2. **The Wheel spins** → a random **battle modifier** hits the field before the round begins.
3. **Fight under the modifier**, adapt live, win → the Wheel spins again.
4. Some rolls help you, some hurt — the fun is that you *never know* until it lands.
5. **Dahlia at the summit** (a defined ladder to her) — and her Zoroark **hides which rule the Wheel
   dropped**, so you fight her half-blind and read the field from its effects.
6. Loss forfeits the **run + stake**, never your Pokémon.

### The modifier set (a curated wheel)
A tuned set of clean, buildable rolls, e.g.: **weather** (rain/sun/sand/hail) · **terrain**
(electric/grassy/psychic/misty) · **Trick Room** · **Gravity** · a **random status** to one lead ·
a **stat stage** swing · **item vanish** (held items disabled this round) · **sudden death** (both
sides at reduced HP) · **jackpot** (a free stat boost or a heal). Kept to a fixed, tested list so
every roll is fair and reproducible.

### The gimmick — controlled chaos
You cannot bring a fixed game plan — the Wheel breaks it every round. Skill becomes *adaptation
speed*: read the roll, re-plan on the fly. It's the most **stream-native** facility of the eight —
chat calls the spin, cheers the jackpot, groans at Trick Room. The counterpart to the Tower's nerve,
Castle's discipline, Factory's knowledge: the Arcade tests **flexibility.**

### Stakes & rewards
- **Stake:** entry (CD or a Pass).
- **Reward:** streak-based CD + item milestones.
- **Record:** best Arcade streak — surviving the most spins.

### The brain — Dahlia
Blaziken / Togekiss / Garchomp / Zoroark at L100 — a flashy, offensive team. Her twist: her
**Zoroark disguises the Wheel's result** — the modifier still lands each round, but you are *not told
which*, so you have to deduce the active rule from its effects mid-battle. The house's edge is
**information.**

### Locked design (2026-07-13 showrunner)
**Pure random** Wheel (no player control — you take what lands) · rolls are **double-edged /
field-wide** (they change the field for both sides, so the chaos is fair) · **capped ladder →
Dahlia** · Dahlia's twist = **Zoroark hides which rule is active** (fight half-blind to the field).

### Stream moments
Every spin reveal · the disastrous roll you survive anyway · the jackpot at the perfect moment · the
Dahlia fight where the wheel keeps landing in her favor.

### Build notes
Reuse the noble **`NobleSkyFx`** (weather/time) + TBCS **`rules`** arg for field states, and pre-battle
effect commands for status/stat/HP swings. The Wheel is a **weighted random roll → apply one modifier
from the curated map** before the `tbcs battle` dispatch. Scope the modifier list to what's cleanly
applicable (weather/terrain/trick-room/gravity are easy; item-vanish/sudden-death need a check). Low
engine risk if the set is curated to feasible effects.

---

## 🕳️ Deep Dark Cave — "The Descent" — Cave Warden Selene

*Team: … Darkrai (L100). Plus the deepest chamber: **Giratina.***

### Fiction
The one wing the Company **sealed instead of sold** — too old, too dangerous, too dark. A gigantic
**sculk cavern** under the Frontier: ore veins glinting in the black, sculk sensors listening for a
footstep, a **Warden** roaming the depths, and something older curled at the very bottom in the
distortion of the deepest dark — **Giratina.** Cave Warden Selene keeps the threshold; the cave keeps
everything below.

### The loop — descend a built cave
The Cave is **not a facility "system"** like the others — it's a **giant sculk cavern the builders
carved out**, and you simply go down it. No wager loop, no store, no streak: an exploration space with
two encounters.
1. **Descend** through the dark. Sculk, ore veins, and a **Warden already roaming** (ambient — it
   spawns naturally in the deep dark; nothing special wires it) make the descent tense on its own.
2. **Selene** guards a chamber **partway down** — the shipped brain (team + Darkrai), placed as the
   mid-descent gate.
3. The **deepest chamber: Giratina** — a **Legends-Arceus-style noble encounter** (dodge/wear-down →
   real catchable), the distortion-world legendary that belongs in this dark. The finale of the
   descent.
4. The frontier no-death guard still covers the **Selene + Giratina battles** (a faint there doesn't
   forfeit) — but the cave itself is just a *place*, with the Warden as its natural danger.

### The gimmick — a real dungeon
The most **adventure** of the eight: it's an actual crawl into a builder-made deep-dark, not a battle
hall with a twist. The tension is the dark (and the Warden) between the two encounters, and the payoff
is a legendary at the bottom.

### Rewards
Whatever the builders seed in the cave (ores / loot), **Selene's** defeat reward, and — the marquee —
the **Giratina** catch at the bottom.

### The brain — Selene *(+ Giratina)*
Selene's team closes with **Darkrai** (sleep in the dark). She is the *keeper*, not the finale —
Giratina at the bottom is the true prize. Two deep-dark legendaries in one hall: Darkrai on Selene's
team, Giratina in the pit.

### Stream moments
Creeping past the Warden with one heart of tension · the Giratina reveal in the deepest chamber · the
catch attempt on a legendary at the end of a real dungeon.

### Build notes — the LIGHTEST facility on our end
The cave is a **builder job** 🧱 (sculk, ores, layout) and the **Warden is ambient** (spawns on its
own — no wiring). So our deliverable is small: a **new Giratina Legends-Arceus noble config**
(data-only on the shipped `noble/` engine) placed at the bottom, and **Selene placed** as the
mid-descent gate. No wager/store/streak system to build.

### Locked design (2026-07-13 showrunner)
A **builder-made explorable sculk cavern** (Warden spawns ambiently — no wiring); **not** a wager
facility. Deliverable: a **new Giratina noble encounter** (LoA-style, catchable) at the bottom +
**Selene** placed as a mid-descent gate. No stake / streak / store.

---

## 🔺 Battle Pyramid — "The Shifting Maze" — Pyramid King Brandon

*Team: Regirock / Registeel / Regice / Regigigas (L100).*

### Fiction
Brandon woke the three ancient Regis from the stone and fights beside them in *"the last wing the
Company ever built for wonder instead of the ledger."* But the Pyramid does not hold still — it
**reshapes itself**, teleporting climbers and shuffling its own chambers, so no one masters it by
memory. You climb by wits, and the golem-king waits at the apex.

### The loop — race a dark, shifting maze to the top
1. **Enter** the maze at the base — a big, **dark**, multi-level interior (bring or find light).
2. **Navigate upward** through the dark — **pure navigation, no intermediate battles.** Every ~30
   seconds **the pyramid SHIFTS**: it reshuffles and teleports you elsewhere, so the route is never
   memorizable and the home stretch can vanish out from under you.
3. **Reach the apex** and face the one boss: **Brandon** — the golem-king with his three Regis +
   **Regigigas** at L100. Getting there *is* the trial; the fight is the payoff.
4. Frontier no-death guard covers the Brandon battle; the maze itself is the challenge, not a death
   trap.

### The gimmick — disorientation in the dark
No memory, no fixed route — you read a dark maze that keeps rearranging under you, on a timer. It's
the navigational counterpart to the others' battle twists: the Cave is a *place* you descend, the
Pyramid is a *place that fights back by moving.* Great stream chaos — the shift that yanks you off the
home stretch, the lucky warp near the top.

### Rewards
Whatever the builders seed in the maze (loot/supplies) and — the marquee — clearing **Brandon +
Regigigas** at the apex (the "I conquered the Pyramid" trophy).

### The brain — Brandon
Regirock / Registeel / Regice + **Regigigas** at L100 — the golem-king of the last wonder in the
building, at the top of the maze. The Regis are **his team**, not maze guardians.

### Stream moments
The shift that teleports you right off the home stretch · re-finding your bearings in a reshuffled
dark · the torch running low · the Regigigas apex.

### Build notes
The **shift is a periodic function** (every ~30s: random `tp` of the player + rotate which exits/goal
markers are open) — true geometry-rebuilding is unnecessary; teleport-the-player-and-move-the-goal
sells the disorientation. Plus a **light/dark** resource (torches) and the shrine **parkour/dark**
plumbing for the maze body; the interior is an **in-world build** 🧱. It must never strand a player in
an unreachable pocket — a safety recall to the entrance backstops the shuffle.

### Locked design (2026-07-13 showrunner)
A **dark, periodically-shifting maze** (every ~30s it reshuffles + teleports you) — **pure
navigation, no node battles**. Win = reach the **apex** for the one boss, **Brandon** (Regirock /
Registeel / Regice / Regigigas). Limited light (a torch resource). The Regis are his team, not
guardians.

---

## 🏪 Battle Market — "The Auction" — Market Mogul Sterling

*Team: Zoroark / Chansey / Alakazam / Porygon-Z (L100).*

### Fiction
Market Mogul Sterling is the Company in miniature: a **grifter** who has never made an honest offer in
his life. The Market is a hall of **deals** — before and between fights he'll sell you an edge, and
some of them are real and some are cons wrapped in a smile. His Zoroark is right at home where nobody
can tell the genuine article from the fake. It's the one hall you beat with **suspicion.**

### The loop — read the mark
1. **Register** + stake. Party cloned (no attrition).
2. **Sterling makes offers** before and between rounds — e.g. *a full-heal if you surrender your held
   item · a stat boost if you lock out one of your moves · intel on the next foe for coin · trade a
   Pokémon for one of his.* Some are genuinely good. Some are **traps** (the buff has a hidden
   downside; the "gift" is a disguised Zoroark; the intel is a lie).
3. **Fight** — the deals you accepted shape the battle (your buffs, your handicaps).
4. **Read him.** Take the honest deals, refuse the cons; the "score" is how much you finessed out of
   him vs how badly he took you.
5. **Sterling** at the summit — Chansey stalls while he talks, Zoroark lies about what's on the field.
6. Loss forfeits the **run + stake**, never your Pokémon.

### The gimmick — suspicion
It's a social/economy duel: every offer is a gamble on whether Sterling is telling the truth, and the
skill is *reading the con.* The counterpart to the others — Tower nerve, Castle discipline, Factory
knowledge, Arcade flexibility, and here, **distrust.** Peak stream: chat screaming *IT'S A TRAP* vs
*TAKE IT* on every deal.

### Stakes & rewards
- **Stake:** entry (CD or a Pass).
- **Reward:** what you finesse out of the deals + beating Sterling; a "best net finesse" score.

### The brain — Sterling
Zoroark / Chansey / Alakazam / Porygon-Z at L100. Chansey stalls to buy him time to talk; Zoroark
keeps you unsure what's real. **His twist: the deals you struck with him quietly BREAK during his own
battle** — the buff you paid for evaporates, the intel was a lie. Never trust the house. The honest
hall's evil twin: where the Factory *lends* you a team, Sterling *cons* you out of one.

### Stream moments
Every deal a chat vote · the con that burns the run · the good deal that saves it · the moment his
deals renege mid-fight and the room realizes it was rigged.

### Build notes
**Dialog-driven deal offers** (a menu of costed deals) + pre-battle effect commands for the
buffs/handicaps, and a **trap-resolution** layer (some deals resolve to a hidden bad outcome, with a
readable **tell** authored into each). Reuse the CobbleDollars pay-probe (coin deals) + the dialog
engine; the trade/handicap deals reuse party + battle-rule hooks. New: the deal/tell/trap table + the
mid-fight renege. Medium complexity, mostly data/dialog.

### Locked design (2026-07-13 showrunner)
Cons have **subtle tells** (a reading skill, not luck or memorization) · deals can wager **CD, items /
held items, Pokémon trades, and battle handicaps** (everything on the table) · **capped ladder →
Sterling** · Sterling's twist = **he reneges mid-fight** (your deals break during his own battle).

---

## ⚓ Battle Port — "Storm Doubles" — Port Admiral Horatio

*Team: Pelipper / Gyarados / Kingdra / Lugia (L100).*

### Fiction
Port Admiral Horatio runs the Frontier's **open water** — four hulls, one storm, and the only hall
fought two-on-two on a heaving deck. The weather never sits still out here, and every so often a
**wave** sweeps the board and nobody keeps their footing. Horatio's crew are built for it; you have
to learn to fight in weather that keeps turning.

### The loop — doubles on a heaving sea
1. **Register** + stake. Party cloned (no attrition).
2. Fight a **doubles gauntlet.** The **weather shifts** every few turns (rain → hail → calm → …), and
   periodically a **"wave"** forces a switch on the field — you never fully control the board.
3. Adapt your doubles play to the turning weather and the waves; positioning and switch-timing matter
   as much as raw power.
4. **Horatio** at the summit — Pelipper's drizzle, Kingdra's swift-swim, Gyarados, and **Lugia**
   overhead, all riding the storm he thrives in.
5. Loss forfeits the **run + stake**, never your Pokémon.

### The gimmick — chaotic doubles
The only **doubles** hall, layered with a shifting storm and board-sweeping waves — it tests the one
skill the other seven don't: **doubles positioning under chaos.** The counterpart cap on the set:
Tower nerve, Castle discipline, Factory knowledge, Arcade flexibility, Market distrust, Pyramid/Cave
navigation, and here — **command.**

### Stakes & rewards
- **Stake:** entry (CD or a Pass).
- **Reward:** streak/ladder CD + item milestones; a best-clear record.

### The brain — Horatio
Pelipper / Gyarados / Kingdra / Lugia at L100 — a rain-abusing doubles crew with a legendary
flagship. The storm is his home field; you win by fighting it as well as him.

### Stream moments
The wave that splits your combo right before it lands · weather flipping your sweep into a liability ·
the Lugia aeroblast · a clean doubles read against the chaos.

### Build notes
Reuse the gym-4 **`GEN_9_MULTI`** doubles dispatch + the Arcade/noble **weather** plumbing for the
turning storm. New: the **"wave" forced-switch** every N turns (a battle-rule or scripted hook). If a
mid-battle forced-switch can't be driven cleanly in TBCS, fall back to weather-cycling only (still a
distinctive storm-doubles hall). Medium complexity.

### Locked design (2026-07-13 showrunner)
- **Weather — random each shift.** Every N turns the sea rolls a **random** weather (rain / hail /
  fog / calm), *not* a fixed cycle. You can't pre-plan the flip — you adapt live, and chat calls the
  roll. Reuse the **Arcade roulette roll** to pick the next weather; it's the same "controlled chaos"
  engine pointed at the sky instead of the field.
- **The wave — forced switch, BOTH sides.** Every N turns a **wave** forces a random switch on
  *both* the player pair and Horatio's pair — symmetric chaos, nobody keeps their footing. Because it
  hits him too, a wave can rescue your board as often as it wrecks it, which keeps it feeling like
  *weather* rather than a pure penalty. (If a mid-battle forced-switch can't be driven cleanly in
  TBCS, the documented fallback is weather-cycling only — still a storm-doubles hall. See build note.)
- **Format — you + AI "first mate" (2v2 tag).** You pilot **one** slot; an **AI ally** ("first mate")
  pilots the other, against Horatio's two. It's the one hall where you don't fully command your own
  board — fitting the "learn to fight in weather that keeps turning" theme, and it distinguishes the
  Port from a plain gym-4 doubles rerun. **Risk flagged:** AI-ally quality is the make-or-break; if
  the ally AI plays badly enough to feel unfair, fall back to **straight doubles 2v2** (you control
  both slots) — the gimmick survives either way, so this is a tuning call, not a redesign.
- **Structure — capped ladder → Horatio, then endless unlock.** First clear is a **capped** ladder
  (challengers → Horatio) for a clean best-clear record and payout; beating it **unlocks an endless
  storm mode** for the grind sandbox — mirroring the **Tower** lock-in exactly, so the two "streak"
  halls (Tower singles-greed / Port doubles-command) bookend the set with the same capped-then-endless
  shape.

**Net:** the Port is the doubles-command capstone — random skies, board-sweeping waves that cut both
ways, a fallible AI partner, and a Tower-style capped→endless ladder. Weather reuses Arcade; format
reuses gym-4 `GEN_9_MULTI`; the only genuinely new piece is the every-N-turns wave (with a clean
weather-only fallback if TBCS won't drive the switch).

---

*(All eight fleshed out and locked.)*
