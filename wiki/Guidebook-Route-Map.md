_The journey in order — every town, route, and shrine from Sango to the Founder, in the sequence you walk them._

> **Part of the campaign guide.** This is the **linear path**; for the *what-to-expect* detail at each stop see [[Guidebook Act I]], [[Guidebook Act II]], [[Guidebook Act III]], and [[Guidebook Shrines]]. Story framing lives in [[Guidebook Overview]].

> ⚠️ Hardcore + Nuzlocke: the gym order is **fixed** and the world scales to it. Fight *at* your level cap, treat each town's safe zone as your only breather, and remember the **five shrines are optional detours** — worth the loot, capable of ending your run. The Quest HUD (`/ca quest show`) always points at your current objective.

---

## Route at a glance

```mermaid
flowchart TD
  S["Sango Town — start<br/>cap 20"] --> G1["1. Takehara Falls — Bug<br/>~1910, 109, 2524 · cap 30"]
  G1 --> G2["2. Hua Zhan City — Grass<br/>~1500, 86, 2053 · cap 38"]
  G2 --> G3["3. Mystic Marsh — Fairy<br/>~1073, 65, 2441 · cap 45"]
  G3 --> G4["4. Deepcore City — Fighting<br/>~1045, 129, 3186 · cap 52"]
  G4 --> G5["5. Gaviota Port — Water<br/>~624, 82, 3536 · cap 58"]
  G5 --> G6["6. Kalahar Reach — Ground<br/>~2085, 126, 4050 · cap 63"]
  G6 --> G7["7. Cyber City — Electric<br/>~1462, 89, 1185 · cap 68"]
  G7 --> HQ["HQ Raid — Acting CEO DJ<br/>1590, 51, 1028 · CURRENCY STABILIZED"]
  HQ --> G8["8. Ryujin Keep — Dragon<br/>~2156, 201, 884 · cap 73"]
  G8 --> G9["9. Nifl Town — Ice<br/>~3608, 112, 2031 · cap 78"]
  G9 --> G10["10. Scorchspire — Fire<br/>~3700, 100, 4511 · cap 85"]
  G10 --> RL["Royal League<br/>3528, 166, 2773 · cap 100"]
  RL --> BD["The Board of Directors"]
  BD --> FD["The Founder — mirror battle"]
  FD --> ED["Leave the map → Ender Dragon"]

  G3 -. "optional" .-> SF["Fairy Shrine"]
  G6 -. "optional" .-> SG["Ground Shrine (Buried Maze)"]
  G8 -. "optional" .-> SD["Dragon Shrine (Hydra Gauntlet)"]
  G9 -. "optional" .-> SI["Ice Shrine (Frozen Path)"]
  G10 -. "optional" .-> SR["Fire Shrine (Trial by Flame)"]
```

*Coordinates are gym-leader positions (≈ the town); they show direction of travel, not exact entrances. The HQ raid becomes available **after gym 7** — see the note in step 7.*

---

## The sequence

| # | Stop | Coords (≈) | Type | Cap after | Optional shrine | Act |
|:-:|------|-----------|------|:--------:|-----------------|:---:|
| — | **Sango Town** (start) | — | — | 20 | — | I |
| 1 | Takehara Falls | 1910, 109, 2524 | Bug 🐞 | 30 | — | I |
| 2 | Hua Zhan City | 1500, 86, 2053 | Grass 🌿 | 38 | — | I |
| 3 | Mystic Marsh | 1073, 65, 2441 | Fairy ✨ | 45 | **Fairy Shrine** | I |
| 4 | Deepcore City | 1045, 129, 3186 | Fighting 🥋 | 52 | — | II |
| 5 | Gaviota Port | 624, 82, 3536 | Water 🌊 | 58 | — | II |
| 6 | Kalahar Reach | 2085, 126, 4050 | Ground 🏜️ | 63 | **Ground Shrine** | II |
| 7 | Cyber City | 1462, 89, 1185 | Electric ⚡ | 68 | — | II |
| ★ | **Company HQ Raid** | 1590, 51, 1028 | Acting CEO DJ | — | — | II |
| 8 | Ryujin Keep | 2156, 201, 884 | Dragon 🐉 | 73 | **Dragon Shrine** | III |
| 9 | Nifl Town | 3608, 112, 2031 | Ice ❄️ | 78 | **Ice Shrine** | III |
| 10 | Scorchspire | 3700, 100, 4511 | Fire 🔥 | 85 | **Fire Shrine** | III |
| ☆ | **Royal League** | 3528, 166, 2773 | Elite Four → Champion | 100 | — | III |
| ☆ | **The Board → The Founder** | — | Board of Directors, then the mirror | — | — | III |
| ∞ | **Post-game** | beyond the map | Ender Dragon (still hardcore + Nuzlocke) | — | — | III |

---

## Walk it, step by step

Each gym is its own small climb — rank-and-file trainers → Jr. Apprentice → Apprentice → **Leader**. The leader's defeat unlocks the next cap and fires a **memory fragment**. Detail for each leg is on the linked act page.

1. **Sango Town → Takehara Falls.** Leave the starting town and beat **Leader Cicada** (Bug). First Company grunts appear on the routes with confused "have we met?" double-takes. → cap **30**.
2. **→ Hua Zhan City.** Beat **Leader Blossom** (Grass). Wheat country — the first **Company wheat traders** surface, offering their "alternative" currency. → cap **38**.
3. **→ Mystic Marsh.** Beat **Leader Titania** (Fairy). **Optional:** the **Fairy Shrine** cult unlocks here (a raised, nicknamed, *shiny* lead + a solo battle — a late, deliberate project). → cap **45**. *End of the "still stable" feel.* See [[Guidebook Act I]].
4. **→ Deepcore City.** Beat **Leader Bruno** (Fighting). The CobbleDollar starts feeling *off*; payouts drift. → cap **52**. *Act II begins —* see [[Guidebook Act II]].
5. **→ Gaviota Port.** Beat **Leader Neptune** (Water). Recognition sharpens toward "you're supposed to be dead." → cap **58**.
6. **→ Kalahar Reach.** Beat **Leader Gaia** (Ground). **Optional:** the **Ground Shrine — Buried Maze** unlocks (half-HP, blind, random teleports — the single most run-ending shrine). → cap **63**.
7. **→ Cyber City.** Beat **Leader Volt** (Electric). The seventh badge is the **inflection point** — the "you signed this charter" memory fragment lands, instability nears its peak, and wheat traders are turning hostile. → cap **68**.
   - ★ **The HQ Raid opens.** With seven badges the trail leads to **Company HQ `[1590 51 1028]`**. Fight up to **Acting CEO DJ**; his defeat triggers **"CURRENCY STABILIZED"** (instability snaps to 25). This is the Act II climax — do it around now (a few of its prerequisites expect you to have pressed on toward gym 8). Full beat: [[Guidebook Act II]].
8. **→ Ryujin Keep.** Beat **Leader Ryujin** (Dragon). **Optional:** the **Dragon Shrine — Hydra Gauntlet** (three battles, full heal between — the safest shrine). → cap **73**. *Act III —* see [[Guidebook Act III]].
9. **→ Nifl Town.** Beat **Leader Boreas** (Ice). **Optional:** the **Ice Shrine — Frozen Path** (timed parkour, generous 180s). → cap **78**.
10. **→ Scorchspire.** Beat **Leader Vulcan** (Fire), the final gym. **Optional:** the **Fire Shrine — Trial by Flame** (tight 120s parkour, but the **best-paying shrine**: Master Ball + Netherite). → cap **85**.
11. **→ Royal League `[3528 166 2773]`.** The Elite Four (Aria · Marcus · Luna · Drake) then **Champion Cynthia**. Victory unlocks the level-**100** cap.
12. **→ The Board of Directors → The Founder.** The post-League gauntlet, ending in the **mirror battle** that pays off the whole amnesia arc. *(Spoiler-light by design — let it land in play.)*
13. **→ Beyond the map.** With the Company overthrown, leave the curated world for generated terrain and go after the **Ender Dragon** — still hardcore, still Nuzlocke.

---

## Reminders for the road

- **Track the goal:** `/ca quest show` (boss bar + sidebar) always names your current objective and progress. Toggle off for overlays with `/ca quest hide`.
- **Shrines are detours, not steps** — none are required. Match a shrine to its element's gym, and read [[Guidebook Shrines]] before you commit; `/shrine-abort` is your panic button.
- **Safe zones are towns + shrine grounds only.** The routes between are full wilderness — that's where Nuzlocke faints, Dark Urge whispers, and ambushes happen.
- **Don't over-level.** The cap is a ceiling *and* a target; the world is tuned for an at-cap team.

> **See also:** [[Guidebook Overview]] · [[Guidebook Act I]] · [[Guidebook Act II]] · [[Guidebook Act III]] · [[Guidebook Shrines]] · [[Commands]]
