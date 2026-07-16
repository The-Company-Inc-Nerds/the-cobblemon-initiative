_Seven legends walk the map in a frenzy. Dodge them, wear them down with your own two hands, and the calm that follows is your one chance to catch a perfect Pokémon._

> **Part of the campaign guide.** See [[Guidebook Overview]] for the full route, [[Guidebook Shrines]] for the other optional trial system (two of these birds hatch straight out of the shrines), and [Architecture Overview](https://github.com/The-Company-Inc-Nerds/the-cobblemon-initiative/blob/main/docs/ARCHITECTURE_OVERVIEW.md) for how the noble engine fits the rest of the mod.

---

## What the nobles are

**Noble encounters** are Legends-Arceus-style real-time boss fights — the only content in the mod where *you*, not your Pokémon, do the fighting. A frenzied noble manifests at a fixed arena on the map and attacks you in real time with element-themed projectiles, telegraphed ground strikes, beams, and hazard fields. You dodge, and you wear it down with **melee**. When its strength breaks, the frenzy lifts — and the noble becomes a **real, catchable wild Pokémon** with **all six IVs perfect**.

The shipped roster is seven: the weather trio (**Groudon, Kyogre, Rayquaza**), the legendary birds (**Articuno, Zapdos, Moltres**), and one friendly outlier — **Mew**, who doesn't fight you at all — it plays tag.

Each noble has a **fixed arena site** on the map and a **giver** that launches it. Subduing one grants its advancement and marks it defeated.

> [!NOTE]
> **The per-noble levels are tuned to be beatable *underleveled* at their gate** (see the table below), so each is a fair fight when you reach it. The exact numbers are pending a final showrunner balance sign-off and may shift between builds.

---

## How to find each one

Nobles aren't wandering — each is launched from a specific **giver** the moment you're gated in.

- **Mew, Kyogre, Groudon, Rayquaza, Zapdos** each have a field giver whose dialog opens the encounter:
  | Noble | Giver | Where |
  |---|---|---|
  | **Mew** | A Giggle in the Grass (a hidden wisp) | Safari Zone |
  | **Kyogre** | Warning Buoy | offshore, Gullwing Coast |
  | **Groudon** | Crater Warding Stone | Volcano Peak crater rim |
  | **Rayquaza** | Sky-Altar | atop the Ryujin spire |
  | **Zapdos** | Grid Warden Cass | Cyber City grid edge |
- **Articuno and Moltres** have **no separate giver** — they launch from the **Ice** and **Fire shrine leaders'** after-battle dialog, once you've cleared that shrine (see [[Guidebook Shrines]]). Each shrine's crystal doubles as a portable launcher for the same bird.

Each giver's button only appears once you've earned the gate — a badge count, a shrine cleared, or (for Rayquaza) both. Town rumor hubs point you toward the nearby ones.

---

## The fight, phase by phase

### Phase 1 — the frenzy (real-time)

The noble manifests in a burst; the sky itself answers (Kyogre fights under real rain, Zapdos under a thunderstorm, Groudon locks the sun to a harsh noon), a boss track replaces the overworld music, and after a four-second overture the fight goes live with the noble's own roar.

- **The arena is a hard ring.** Cross the edge and a barrier shoves you back inside.
- **Every attack telegraphs.** Warning rings pulse with an accelerating tick; in the **final half-second they flash white-hot and bigger** — that is the *move now* cue. Beams draw their firing line before they sweep it.
- **Melee lands with feedback.** Crit-sparks and a pitch-scaled crunch confirm every hit; the boss bar flashes white.
- **The boss bar talks.** `⚠` = an attack is arming · **yellow "Weakened!"** = the stagger is close · **red `‼` ENRAGED** = a rage band crossed · **white "GROUNDED, STRIKE!"** = a flyer's punish window is open.
- **Rage bands.** As its health falls the noble roars, attacks speed up, and held-back moves unlock — **Rayquaza only starts dive-bombing once enraged; Moltres gains a fourth attack once deeply hurt.**
- **Flyers have a rhythm.** Articuno, Zapdos, Moltres, and Rayquaza spend most of the fight out of melee reach. A particle spiral telegraphs the descent, the landing thud is followed by a **bell — bell means GO**: the grounded punish window is open, with an action-bar countdown until it takes back off.
- **Your hearts are the real boss bar.** Below 40% HP a heartbeat starts thumping; below 20% it doubles. That sound is the run telling you to disengage.

### The stagger — don't panic, watch

At roughly **15% of its health** the fight *stops*. The music cuts, the frenzy visibly breaks, and over a few seconds the collapsed body gives way to the **real** noble rising in its place, crying on its actual model. No attacks can hit you during this window — but the **arena barrier still holds**, so don't sprint for the hills: the catch battle opens the moment the cinematic ends. Two nobles script it differently — **Moltres fakes its death** and reignites from the embers, and **Mew's** chase ends in a freeze-frame *GOTCHA!*.

### Phase 2 — the catch battle

A genuine Cobblemon wild battle against the calmed noble, under its own battle theme. **Your party is fully healed the instant it opens** — you enter fresh. Catch it or KO it; either way completes the encounter, pays the reward pack, grants the achievement, and sets the story flag. But the *point* is the catch:

> [!TIP]
> **Every noble's battle form has all six IVs at 31.** A caught noble is the statistically best Pokémon a hardcore Nuzlocke run can ever obtain, and the level cap doesn't block using it: an over-cap catch simply earns no XP until your cap passes it. **KO it and that mon is gone.** One crit too many in the catch battle destroys the prize — deciding how long to keep weakening it is the real wager.

> [!IMPORTANT]
> **Caught Pokémon arrive fainted at 0 HP and go to the PC** (Nuzlocke capture rule). Revive it before you celebrate on stream with an empty party slot.

### Mew — the game of tag

Mew never attacks and can't be hurt. It flees, blink-teleports around the arena, and giggles at you. **Tag it six times** (get within touching distance) to tire it out. The chase escalates — each tag makes it faster and blinkier — and the audio tells you how you're doing: a rising jingle per tag, hearts trailing when one tag remains. Corner it against the ring; chasing it in the open is a footrace you'll barely win.

---

## The seven nobles

Levels below are the **cap-under** tuning — each is set to be winnable underleveled at its gate. Gates are the era of the map you've reached, not a hard requirement to walk in.

| Noble | Element | Level | Gate (when it opens) | Arena / launch |
|---|---|---|---|---|
| **Mew** | Psychic | ~35 | mid-game (post-Gym-3 band) | Safari Zone — the wisp |
| **Kyogre** | Water | ~50 | post-Gym-5 | offshore, Gullwing Coast — the buoy |
| **Zapdos** | Electric | ~60 | post-Gym-7 | Cyber City grid edge — Grid Warden Cass |
| **Rayquaza** | Dragon | ~66 | Gym 8 **and** Dragon Shrine cleared | Ryujin spire top — the Sky-Altar |
| **Articuno** | Ice | ~72 | Ice Shrine cleared | Ice Shrine — Glacius' after-battle |
| **Groudon** | Ground/Fire | ~78 | post-Gym-10 | Volcano Peak crater — the warding stone |
| **Moltres** | Fire | ~82 | Fire Shrine cleared (post-league) | Fire Shrine — Ignis' after-battle |

Signature moves worth knowing: **Groudon's** rear-up stomp shockwave, **Kyogre's** whirlpool that *pulls you in*, **Rayquaza's** dive-charge (enraged only), **Articuno's** blizzard that creeps frost across your screen as it rages, **Zapdos'** tracking bolt columns, and **Moltres'** false-death rebirth at the stagger.

---

## Hardcore safety checklist

- **Phase 1 is real player damage in a hardcore world.** Fire burns, ice slows and freezes, impacts knock you around. Dying to a noble **ends the run** — this is the single most dangerous optional content in the mod, and none of it is required.
- **`/noble-abort` is your panic button.** No permission, no penalty, works any time in Phase 1 — the arena tears down cleanly and you can come back stronger. Retreating is a strategy, not a failure.
- **Phase 2 is a real Nuzlocke wild battle.** Every faint chips *your* hearts and **releases the fainted Pokémon**; losing your last party member is a whiteout — in hardcore, the end. The pre-battle full heal is generous, but a level-70+ legendary is still a level-70+ legendary.
- **Fleeing the catch battle costs you.** The encounter itself ends penalty-free, but the global Nuzlocke flee rule still **sacrifices one party member** — and fleeing with only one is death.
- **Watch the white flash and listen for the bell.** The telegraph language is consistent across all seven fights: white ring = move *now*, bell = attack *now*, heartbeat = leave *now*.
- **Don't fight enraged nobles at low HP.** Rage bands tighten attack timing precisely when the fight has already been chipping you. The pre-stagger warning roar is your cue to save hearts for the finish.

---

## Related pages

- [[Guidebook Overview]] — the campaign route these encounters slot into
- [[Guidebook Shrines]] — the other optional trial system; the Ice and Fire shrine keepers launch Articuno and Moltres
- [[Commands]] — `/noble start|stop|list` (OP) and the no-permission `/noble-abort`
- [Architecture Overview](https://github.com/The-Company-Inc-Nerds/the-cobblemon-initiative/blob/main/docs/ARCHITECTURE_OVERVIEW.md) · [Architecture Data Flows](https://github.com/The-Company-Inc-Nerds/the-cobblemon-initiative/blob/main/docs/ARCHITECTURE_DATA_FLOWS.md) — the noble engine: the two-phase body-swap, the attack primitives, and the config schema
