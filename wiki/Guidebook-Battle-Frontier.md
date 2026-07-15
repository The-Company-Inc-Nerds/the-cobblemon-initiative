# Guidebook: Battle Frontier

_The post-Champion sandbox — eight halls, eight Frontier Brains, one storm of level-100 mastery content for the 85→100 window._

> **Part of the campaign guide.** This is the *what-each-hall-is* detail; the plain who-and-where roster lives in **[[Quests Battle Frontier]]**. The other three Company facilities (Stadium / Daycare / Safari) are in **[[Guidebook Facilities]]**. Story framing is in **[[Guidebook Overview]]** and **[[Guidebook Act III]]**.

> [!CAUTION]
> **Endgame — post-Royal-League.** The Frontier only opens to a **Champion**. Everything here fights at **level 100**; treat it as the deep end.

> [!NOTE]
> **Content status (0.5.0):** all eight facilities — challengers + Brains — are cast, placed, and gated on the Champion title. The **gimmick + stakes layer described below is the locked design**, not yet all wired: teams and placements ship; the wager/no-death guard, the per-facility loops (streaks, stores, the Wheel, rentals, the deals), and the two dungeons' builds are in progress. Where a hall isn't fully wired yet it's a straight walk-up level-100 fight. **Hardcore note:** until the no-death guard ships (see below), treat Frontier battles as your **real** party against level-100 teams — Nuzlocke stakes apply. Do not risk a Pokémon you can't afford to lose.

---

## The wager, not the graveyard

The Frontier is the endgame **sandbox**, not another attrition grind. The design keeps the **danger** but drops the **permanent forfeits**:

- Inside a facility, a faint does **not** release the Pokémon and a whiteout does **not** end your hardcore run. Your party is **cloned** for the run and heals on exit — the same suspension the **Stadium** already uses (see [[Guidebook Facilities]]), extended across the Frontier.
- What you *can* lose is the **bet**: every run costs an **entry stake** (CobbleDollars or a Frontier Pass), and a loss forfeits **the stake, the streak, and that run's prize**. You lost the wager — not a partner.
- That makes each hall infinitely **retryable** (good for a grind sandbox and a live stream) while still stinging to lose. The Brains' *"my team, not yours — nothing you love dies on this floor"* flavor is finally **true**.

> **The one shared prerequisite** for the whole set is the `FrontierManager` no-death/wager guard — a code change that touches the run-ending death path, so it must be runtime-verified before any hall's stakes go live. Until then the **Champion gate + this warning** are the guard. The one hall that leans into rentals *literally* (the Factory) is risk-free by construction — you never field your own team there.

---

## The eight halls at a glance

Every hall tests a **different** skill — no two are the same fight twice.

| Hall | Frontier Brain | Signature team | Gimmick | It tests |
|------|----------------|----------------|---------|----------|
| 🗼 **Battle Tower** | Tower Tycoon **Palmer** | Dragonite · Milotic · Rhyperior | **The Climb** — endless bank-or-climb streak | **Nerve** |
| 🏰 **Battle Castle** | Castle Lord **Percival** | Empoleon · Slowbro · Dusknoir · Steelix | **The Ledger** — spend real CD to survive | **Discipline** |
| 🏭 **Battle Factory** | Factory Head **Noland** | Magnezone · Scizor · Metagross · Porygon-Z | **Rental Roulette** — fight with borrowed teams | **Knowledge** |
| 🎰 **Battle Arcade** | Arcade Star **Dahlia** | Blaziken · Togekiss · Garchomp · Zoroark | **The Wheel** — a random rule every round | **Flexibility** |
| 🕳️ **Deep Dark Cave** | Cave Warden **Selene** | …Darkrai *(+ Giratina below)* | **The Descent** — a real sculk dungeon | **Nerve / exploration** |
| 🔺 **Battle Pyramid** | Pyramid King **Brandon** | Regirock · Registeel · Regice · Regigigas | **The Shifting Maze** — a dark maze that moves | **Navigation** |
| 🏪 **Battle Market** | Market Mogul **Sterling** | Zoroark · Chansey · Alakazam · Porygon-Z | **The Auction** — deals, some of them cons | **Distrust** |
| ⚓ **Battle Port** | Port Admiral **Horatio** | Pelipper · Gyarados · Kingdra · Lugia | **Storm Doubles** — doubles in a turning storm | **Command** |

Every Brain fields a level-100 team, and several carry a signature legendary (Darkrai, Regigigas, Lugia) as their prestige piece — plus **Giratina** waiting at the bottom of the Cave.

---

## 🗼 Battle Tower — "The Climb" (Palmer)

The **one honest hall** — Palmer built it as a real proving ground, no cons, no fine print. It's the facility that teaches the whole Frontier: after every win, **bank your purse or bet it on the next floor.** The pot compounds the longer you stay brave, and a single loss zeroes the run.

- **Phase 1 — the capped Climb:** a defined ladder (three sets of seven, Floor Bosses at 7 and 14) topping out at **Palmer at streak 21**. No checkpoints — a whiteout drops you to the ground. Beating Palmer means you've *beaten the Tower.*
- **Phase 2 — Endless:** unlocked after that first Palmer clear. Same loop with no cap; Palmer recurs as a milestone Floor Boss, and a persistent **best-streak** record is the live stream stat.
- **Rewards:** vitamins + rare candy along the Climb; a rare held item + the **"Tower Ace"** title for the summit; escalating loot on the endless milestones. The banked CD pays out on every Bank.
- **Palmer:** Dragonite / Milotic / Rhyperior at L100 — **no gimmick, the pure skill check.** The honest man in a corrupt building.

## 🏰 Battle Castle — "The Ledger" (Percival)

The mirror of the Tower: where the Tower asks *how brave*, the Castle asks *how disciplined.* Everything is for sale — **even survival** — and you pay in your **real CobbleDollars.**

- Between every round the **Castle store** opens: buy a **heal**, an **item**, or **intel** on the next foe — or push in as you are. **Prices rise with depth**, so you can never simply heal forever.
- The gauntlet is **escalating / endless**, with **Percival recurring as a milestone boss.** Your true score is **net profit** — winnings minus everything you burned staying alive. A deep run can end *net-negative*: the Company wins.
- A whiteout forfeits the payout entirely, and you've already spent real coin to get there — the Castle can leave you **poorer than you came.**
- **Percival:** Empoleon / Slowbro / Dusknoir / Steelix at L100 — a deliberate **stall wall** that drags fights long to milk one more heal out of you.

## 🏭 Battle Factory — "Rental Roulette" (Noland)

The one hall where you **don't use your own team at all** — so it's risk-free by construction, and the honest home of "the Frontier lends the team."

- You're handed a **random rental trio** and fight **3v3** with unfamiliar Pokémon. After each round you may **swap one of your rentals for one the opponent just used** — steal the threat you just beat, or keep your synergy?
- Pure **type-knowledge and adaptation** — no leaning on a comfort team. A **capped ladder → Noland** is the clearable summit and the trophy; a persistent best rental streak is the flex.
- **Noland:** Magnezone / Scizor / Metagross / Porygon-Z at L100 — a tech/steel specialist who drafts a mean rental himself; the engineer who kept the one part of the Frontier that's still *fun.*

## 🎰 Battle Arcade — "The Wheel" (Dahlia)

The Company's **entertainment wing** — chance dressed up as a game show. Before every round a **roulette spins** and drops a random rule on the field; you don't get to plan, you get to *react.*

- The Wheel is **pure random** and its rolls are **field-wide / double-edged** — weather, terrain, Trick Room, Gravity, a random status or stat swing, item-vanish, sudden-death, or a **jackpot** heal/boost. They change the field for *both* sides, so the chaos is fair.
- Skill becomes **adaptation speed** — read the roll, re-plan on the fly. The most **stream-native** hall of the eight.
- **Dahlia:** Blaziken / Togekiss / Garchomp / Zoroark at L100. Her twist — her **Zoroark hides which rule the Wheel dropped**, so you fight her half-blind and deduce the active rule from its effects. The house's edge is **information.**

## 🕳️ Deep Dark Cave — "The Descent" (Selene)

**Not a battle hall** — an actual **gigantic sculk cavern** the builders carved, and you simply go down it. No wager loop, no store, no streak: an exploration space with two encounters and a lot of dark between them.

- **Descend** through ore veins and ambient sculk with a **Warden roaming the depths** (it spawns naturally — creep, don't sprint). The dark itself is the danger.
- **Cave Warden Selene** guards a chamber **partway down** — her team closes with **Darkrai** (sleep in the dark). She's the *keeper*, not the finale.
- The **deepest chamber holds Giratina** — a **Legends-Arceus-style noble encounter** (dodge and wear it down → real, catchable), the distortion-world legendary that belongs in this pit. *The cave itself is the prize.* See [[Guidebook Nobles]] for how noble fights play.
- The most **dungeon** of the eight: exploration, stealth, mining, and a legendary at the bottom.

## 🔺 Battle Pyramid — "The Shifting Maze" (Brandon)

The last wing the Company built *for wonder instead of the ledger* — and it refuses to hold still.

- A big, **dark**, multi-level maze you climb by **pure navigation — no node battles.** Every ~30 seconds the pyramid **SHIFTS**: it reshuffles and teleports you elsewhere, so nothing is memorizable and the home stretch can vanish out from under you. Bring light — a torch is a real resource.
- **You "progress" by reaching the apex.** Getting there *is* the trial.
- **Brandon** waits at the top with the three ancient **Regis + Regigigas** at L100 — the golem-king of the last wonder in the building. The Regis are **his team**, not maze guardians.

## 🏪 Battle Market — "The Auction" (Sterling)

Market Mogul Sterling is the Company in miniature: a **grifter** who has never made an honest offer in his life. The one hall you beat with **suspicion.**

- Before and between fights Sterling **offers deals** — a full-heal for your held item, a stat boost if you lock out a move, intel for coin, a Pokémon trade. Some are genuinely good; some are **cons** with a hidden downside, and each con has a **subtle tell** you can learn to read (a reading skill, not luck or memory). Deals can wager **CD, items, Pokémon trades, or battle handicaps** — everything's on the table.
- Take the honest deals, refuse the cons; the score is how much you finessed out of him vs how badly he took you. A **capped ladder → Sterling.**
- **Sterling:** Zoroark / Chansey / Alakazam / Porygon-Z at L100. Chansey stalls to buy him time to talk; Zoroark keeps you unsure what's real. **His twist — the deals you struck quietly BREAK during his own fight.** Never trust the house.

## ⚓ Battle Port — "Storm Doubles" (Horatio)

The Frontier's **open water** — the only hall fought **two-on-two on a heaving deck**, in weather that never sits still. It tests the one skill the other seven don't: **doubles command under chaos.**

- The **weather rolls random every few turns** (rain / hail / fog / calm — the Arcade's roll pointed at the sky), and periodically a **wave** forces a random switch on **both** sides — nobody keeps their footing, and a wave can rescue your board as often as wreck it.
- You fight **you + an AI "first mate"** (2v2 tag) against Horatio's two — the one hall where you don't fully command your own board. Structure mirrors the Tower: a **capped ladder → Horatio, then an endless storm** unlocks.
- **Horatio:** Pelipper / Gyarados / Kingdra / Lugia at L100 — a rain-abusing doubles crew with a legendary flagship. The storm is his home field; you win by fighting it as well as him.

---

## Where it sits

The Frontier is the **"after."** It depends on nothing downstream and gates nothing — pure long-tail mastery content for the training window before (and after) the final boss, and a place to sharpen a team for the last fight. Other endgame content in the same 85→100 window: the **[[Guidebook Nobles]]** and the post-league shrines (**[[Guidebook Shrines]]**).

> **See also:** [[Quests Battle Frontier]] · [[Guidebook Facilities]] · [[Guidebook Act III]] · [[Guidebook Nobles]] · [[Guidebook Shrines]] · [[Guidebook Route Map]] · [[Commands]]
