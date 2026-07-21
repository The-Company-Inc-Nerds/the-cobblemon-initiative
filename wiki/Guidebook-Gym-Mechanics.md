# Guidebook: Gym Mechanics

Every gym is a real fight *and* a set-piece. Each has a **signature gimmick** — a room mechanic
that changes how you approach the leader — layered on top of two rules that hold in all ten:

- **The ladder softens as you clear it.** A gym's rank-and-file trainers are spread through the
  room, not clustered at the door. Beating them **drains the leader's team** (weakened variants) —
  so optional trainers are a real, decision-worthy way to make the leader fight easier. In a
  hardcore Nuzlocke, that trade (risk a rank-and-file battle now vs. a tougher leader later) is
  yours to make.
- **Leaders are fought underleveled.** Every leader's ace sits **two levels above your entry cap**.
  That is the design — you are meant to out-think a stronger team, not out-level it.

> [!NOTE]
> Gimmicks are being tuned and their coordinates finalized in-world. Behaviour below is the design;
> exact positions may shift between builds.

---

## The ten gimmicks

| # | Town (type) | Signature gimmick |
|---|-------------|-------------------|
| 1 | **Takehara Falls** (Bug 🐞) | **Floating leader.** Cicada perches high and glides down into click range when you approach — beat the tower trainers first to soften the ace. |
| 2 | **Hua Zhan City** (Grass 🌿) | **Living-statue wardens + seal pilgrimage.** Four stone garden wardens stand watch over the gardens; wake and beat each one to set its seal in your hand. Complete the four-warden pilgrimage and the groundskeeper by the west stair sets down her broom and reveals herself as Leader Blossom. |
| 3 | **Mystic Marsh** (Fairy ✨) | **Mirror Match.** Declare your lead, and the leader answers with one of several illusion-variant teams built to counter it. Choose your opener carefully. |
| 4 | **Deepcore City** (Fighting 🥋) | **The Gauntlet.** A marshal-run pit that ends in a **2-vs-1 double** — you fight the leader's pair with an AI sparring partner at your side. |
| 5 | **Gaviota Port** (Water 🌊) | **Tide Clock.** The arena cycles high/low tide on a ~4-minute timer; the leader fields a different rain-variant team depending on the tide when the battle opens. |
| 6 | **Kalahar Reach** (Ground 🏜️) | **Heat mirages.** Six shimmering duplicate trainers scattered across the dunes — most poof when you reach them; find the real ladder among the mirages. |
| 7 | **Cyber City** (Electric ⚡) | **Stadium tease.** Volt runs the exhibition circuit next door — the gym itself is a straight fight, with the Stadium (see [[Guidebook Facilities]]) as the flavor hook. |
| 8 | **Ryujin Keep** (Dragon 🐉) | **The Rift.** An overworld **Ender Dragon** guards the keep; you must bring it down (a crystal-heal loop, `/riftdragon`) before the leader will face you. |
| 9 | **Nifl Town** (Ice ❄️) | **Whiteout Approach.** Three **Frost Sentinels** watch the corridor — being *seen* debuffs you; cross unseen and the leader respects it. Stealth, not speed. |
| 10 | **Scorchspire** (Fire 🔥) | **Banked Coals.** A heat gauge climbs during the fight; wardens vent it to cool the room — let it max out and the leader forces a full-power Vulcan. |

---

## For contributors — placing the interior cast

The rank-and-file trainers (`trainer_1..4`, plus `jr_apprentice` / `apprentice` on gyms 3–10) latch
in a default cluster around the leader. To spread them through the actual gym layout, use the dev
wand: `/cobblemon-initiative gym-mark wand` (or `set gym_<town>_<slot>`) — each slot's item name
calls out **which trainer** and that gym's **gimmick**, so you place with the mechanic in mind.
Boxes (the whiteout corridor, the heat volume) and points (rift crystals, mirages, garden wardens,
sentinels, the leader stage-back spot) are all in the same registry. `gym-mark export` hands the
coordinates back for wiring into each character's placement.
