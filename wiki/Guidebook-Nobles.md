_Seven legends walk the map in a frenzy. Dodge them, wear them down with your own two hands, and the calm that follows is your one chance to catch a perfect Pokémon._

> **Part of the campaign guide.** See [[Guidebook Overview]] for the full route, [[Guidebook Shrines]] for the other optional trial system, and [Architecture Overview](https://github.com/The-Company-Inc-Nerds/the-cobblemon-initiative/blob/main/docs/ARCHITECTURE_OVERVIEW.md) for how the noble engine fits the rest of the mod.

---

## What the nobles are

**Noble encounters** are Legends-Arceus-style real-time boss fights — the only content in the mod where *you*, not your Pokémon, do the fighting. A frenzied noble spawns into a ring arena and attacks you in real time with element-themed projectiles, telegraphed ground strikes, beams, and hazard fields. You dodge, and you wear it down with **melee**. When its strength breaks, the frenzy lifts — and the noble becomes a **real, catchable wild Pokémon** with **all six IVs perfect**.

The shipped roster is the weather trio (**Groudon, Kyogre, Rayquaza**), the legendary birds (**Articuno, Zapdos, Moltres**), and one friendly outlier: **Mew**, who doesn't fight you at all — it plays tag.

> [!NOTE]
> **Content status (0.5.0):** the engine and all seven encounters are live code, started via `/noble start <id>` (OP). What's *not* placed yet: the arenas have no map locations (an encounter currently opens wherever you're standing — showrunner placement is pending), no in-world NPC or item triggers are wired, and the per-noble difficulty numbers are untuned first-pass values. A second tier of smaller "mini-noble" encounters is fully designed (`docs/NOBLE_EPIC_DESIGN.md`) but not built.
>
> **Difficulty is tunable in ModMenu** (Noble Encounters category): arena size (default ≈ double the authored area), boss health, boss melee damage, attack damage, boss bar, and SFX volume/pitch — without touching the per-noble JSONs.

---

## The fight, phase by phase

### Phase 1 — the frenzy (real-time)

The noble manifests in a burst; the sky itself answers (Kyogre fights under real rain, Zapdos under a thunderstorm, Groudon locks the sun to a harsh noon), a boss track replaces the overworld music, and after a four-second overture the fight goes live with the noble's own roar.

- **The arena is a hard ring.** A dust circle marks the edge; cross it and a barrier flash shoves you back inside. A bright curtain arc warns you when you're drifting too close.
- **Every attack telegraphs.** Warning rings pulse with an accelerating tick; in the **final half-second they flash white-hot and bigger** — that is the *move now* cue. Beams draw their firing line before they sweep it.
- **Melee lands with feedback.** Crit-sparks and a pitch-scaled crunch confirm every hit; the boss bar flashes white. Big hits hit louder.
- **The boss bar talks.** `⚠` = an attack is arming · **yellow "Weakened!"** = the stagger is close · **red `‼` ENRAGED** = a rage band crossed · **white "GROUNDED, STRIKE!"** = a flyer's punish window is open.
- **Rage bands.** At roughly 60% and 30% health the noble roars (you'll feel the shove), attacks come faster, and held-back moves unlock — **Rayquaza only starts dive-bombing once enraged; Moltres gains a fourth attack below a third health.**
- **Flyers have a rhythm.** Articuno, Zapdos, Moltres, and Rayquaza spend most of the fight out of melee reach. A particle spiral telegraphs the descent, the landing thud is followed by a **bell — bell means GO**: the grounded punish window is open, with an action-bar countdown until it takes back off.
- **Your hearts are the real boss bar.** Below 40% HP a heartbeat starts thumping; below 20% it doubles. That sound is the run telling you to disengage.

### The stagger — don't panic, watch

At the threshold (~15% of its health) the fight *stops*. The music cuts, the frenzy visibly breaks, and over a few seconds of cinematic the collapsed body gives way to the **real** noble rising in its place, crying on its actual model. No attacks can hit you during this window — but the **arena barrier still holds**, so don't sprint for the hills: the catch battle opens the moment the cinematic ends. Two nobles script it differently — **Moltres fakes its death** and reignites from the embers, and **Mew's** ends in a freeze-frame *GOTCHA!*.

### Phase 2 — the catch battle

A genuine Cobblemon wild battle against the calmed noble, under its own battle theme. **Your party is fully healed the instant it opens** — you enter fresh. Catch it or KO it; either way completes the encounter, pays the reward pack, grants the achievement, and sets the story flag. But the *point* is the catch:

> [!TIP]
> **Every noble's battle form has `min_perfect_ivs=6` — all six IVs are 31.** A caught noble is the statistically best Pokémon a hardcore Nuzlocke run can ever obtain, and the level cap does not block using it: an over-cap catch simply earns no XP until your cap passes it. **KO it and that mon is gone.** One crit too many in the catch battle destroys the prize — deciding how long to keep weakening it is the real wager.

> [!IMPORTANT]
> **Caught Pokémon arrive fainted at 0 HP and go to the PC** (Nuzlocke capture rule). Revive it before you celebrate on stream with an empty party slot.

### Mew — the game of tag

Mew never attacks and can't be hurt. It flees, blink-teleports around the arena, and giggles at you. **Tag it six times** (get within touching distance) to tire it out. The chase escalates — each tag makes it faster and blinkier — and the audio tells you how you're doing: a hot/cold chime as you close in, a rising jingle per tag, hearts trailing when one tag remains. Corner it against the ring; chasing it in the open is a footrace you'll barely win.

---

## The seven nobles

| Noble | Element | Style | Signature | Arena weather / music |
|---|---|---|---|---|
| **Groudon** | Fire | Grounded bruiser | The stomp — a rear-up, then a shockwave; jump or back off | Drought noon · the dragon-fight theme |
| **Kyogre** | Water | Grounded, zone control | A whirlpool that **pulls you in** + a line of tidal strikes | Real downpour · dragon-fight theme |
| **Rayquaza** | Dragon | Flyer, the flagship (lv 75) | Dive-charge — *only once enraged*; beam + meteor columns | Dragon-fight theme |
| **Articuno** | Ice | Flyer | The blizzard **creeps frost across your screen** as it rages — the vignette is the danger meter | Blizzard · *Ward* |
| **Zapdos** | Electric | Flyer, fastest windows | Tracking bolt columns with real thunder | Thunderstorm · *Precipice* |
| **Moltres** | Fire | Flyer | The **false death**: it collapses at the stagger… wait for the ignition | Drought · *Pigstep* |
| **Mew** | Psychic | Friendly chase | Blink-teleport tag, 6 touches, GOTCHA | *Creator (Music Box)* |

---

## Hardcore safety checklist

- **Phase 1 is real player damage in a hardcore world.** Fire burns, ice slows and freezes, impacts knock you around. Dying to a noble **ends the run** — this is the single most dangerous optional content in the mod, and none of it is required.
- **`/noble-abort` is your panic button.** No permission, no penalty, works any time in Phase 1 — the arena tears down cleanly and you can come back stronger. Retreating is a strategy, not a failure.
- **Phase 2 is a real Nuzlocke wild battle.** Every faint chips *your* hearts and **releases the fainted Pokémon**; losing your last party member is a whiteout — in hardcore, the end. The pre-battle full heal is generous, but a level-70 legendary is still a level-70 legendary.
- **Fleeing the catch battle costs you.** The encounter itself ends penalty-free ("the noble slipped away"), but the global Nuzlocke flee rule still **sacrifices one party member** — and fleeing with only one is death.
- **Watch the white flash and listen for the bell.** The telegraph language is consistent across all seven fights: white ring = move *now*, bell = attack *now*, heartbeat = leave *now*.
- **Don't fight enraged nobles at low HP.** Rage bands tighten attack timing precisely when the fight has already been chipping you. The pre-stagger warning roar (just above the threshold) is your cue to save hearts for the finish.

---

## Related pages

- [[Guidebook Overview]] — the campaign route these encounters will slot into
- [[Guidebook Shrines]] — the other optional trial system (and the shared "flex, not checkbox" philosophy)
- [[Commands]] — `/noble start|stop|list` (OP) and the no-permission `/noble-abort`
- [Architecture Overview](https://github.com/The-Company-Inc-Nerds/the-cobblemon-initiative/blob/main/docs/ARCHITECTURE_OVERVIEW.md) · [Architecture Data Flows](https://github.com/The-Company-Inc-Nerds/the-cobblemon-initiative/blob/main/docs/ARCHITECTURE_DATA_FLOWS.md) — the noble engine: the two-phase body-swap, the attack primitives, and the config schema
