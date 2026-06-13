# Guidebook Overview

> A field guide for one trainer, one world, one run. This is a **single-player**, **hardcore + Nuzlocke** campaign played for a live Twitch + YouTube audience. There are no second chances — and the land knows more about you than you do.

This Overview frames the whole campaign: who you are, the route you walk, the discipline that keeps you alive, and the systems you will meet along the way. The act-by-act pages go deeper:

- [[Guidebook Act I]] — Infiltration: gyms 1–7, the destabilizing economy, the wheat traders.
- [[Guidebook Act II]] — The HQ Raid and the turn of the currency.
- [[Guidebook Act III]] — The Royal League, the Board, the mirror, and what lies past the map.
- [[Guidebook Shrines]] — The five elemental trials, optional but pointed.

> **Note on detail.** This Overview and the act pages tell you *what to expect* at each town, route, and shrine — the story beats, the gym type and cap, the systems you will trip over there. **Detailed, turn-by-turn pathing will be added later.** For now, treat this as the map of the journey, not the footstep-by-footstep route.

---

## The Through-Line (no heavy spoilers)

You wake with nothing. No memory, no name you trust, no reason you can name — so you do what any trainer does: you start walking the gym route. From the starting town of **Sango**, ten gyms lie ahead in a fixed order, and beating their leaders is the whole journey on the surface.

Underneath it is a mystery. An organization called **The Company, Inc.** runs the region's money — it is the trusted verifier that everyone relies on to keep **CobbleDollars** honest. As you travel, that money starts to feel *off*: payouts come up short, prices drift, the numbers stop adding up. The Company's people keep doing double-takes when they see your face, as if they know you. They do. You don't — not yet.

The campaign is a slow recovery of who you were, delivered in **memory fragments** as you earn badges. The audience will piece it together before your character does. The truth is held back until late on purpose; this guide will not spoil it here. Walk the route, mind the stakes, and let the world tell you.

> **Tone:** reclamation, not heroism. You are not an outsider saving the land. You are someone taking back something you built and lost — morally smudged, hunted by an institution wearing the face of trust. Inspired by the **Dark Urge** (Baldur's Gate 3) and the **Pokémon Red** mirror finale.

---

## The Rules of Survival

Two disciplines define every minute of play. Internalize them before you leave Sango.

### Hardcore
One life. When you go down, the world ends and a new run begins. Hardcore is set on the world itself; treat every decision as final.

### Nuzlocke
Permadeath for your team, enforced by the mod:

- **On a Pokémon faint outside a safe zone**, *you* take damage (scaled by party size or max health), and the fainted Pokémon may be removed from your party.
- **If you flee a battle**, you pay a **sacrifice** — the game prompts you to select a Pokémon to give up.
- **If a faint would kill you**, a custom **Pokéball death screen** replaces the vanilla one. Hardcore being hardcore, that is the end of the run.
- **Safe zones** — towns and shrine grounds — suspend these penalties and suppress hostile mob spawns. Routes and wilderness do not. Know where the line is before a battle, not after.

### The Dark Urge
When a Pokémon faints **outside** a safe zone, there is a small chance (around 12%, with a 5-minute cooldown) that a cold, first-person whisper surfaces — the voice of who you used to be, commenting on the loss as if your team were inventory. The whispers **escalate** as your level cap climbs; the darkest tier is reserved for the late game. Every fainted partner is the argument between the griever you have become and the founder you were.

---

## Level-Cap Discipline

Your team is capped, and the cap is **earned**. Each gym leader you defeat unlocks a higher level cap (the cap is the highest you have achieved — it never drops). The world scales with your journey, so the discipline is to fight *at* the cap, not over-level past it. The route, in order:

| # | Town | Type | Gym Leader | Level Cap Unlocked |
|:-:|------|------|------------|:------------------:|
| — | **Sango Town** | — (start) | — | 20 (starting cap) |
| 1 | Takehara Falls | Bug 🐞 | Cicada | 30 |
| 2 | Hua Zhan City | Grass 🌿 | Blossom | 38 |
| 3 | Mystic Marsh | Fairy ✨ | Titania | 45 |
| 4 | Deepcore City | Fighting 🥋 | Bruno | 52 |
| 5 | Gaviota Port | Water 🌊 | Neptune | 58 |
| 6 | Kalahar Reach | Ground 🏜️ | Gaia | 63 |
| 7 | Cyber City | Electric ⚡ | Volt | 68 |
| 8 | Ryujin Keep | Dragon 🐉 | Ryujin | 73 |
| 9 | Nifl Town | Ice ❄️ | Boreas | 78 |
| 10 | Scorchspire | Fire 🔥 | Vulcan | 85 |
| — | **Royal League** | Elite Four + Champion | Aria · Marcus · Luna · Drake → Cynthia | 100 |

Each gym is a small climb of its own — rank-and-file trainers, then a Jr. Apprentice, an Apprentice, and finally the Leader — with a reward and a **memory fragment** waiting on the leader's defeat. Check your standing any time with `/ca progress` and `/ca levelcap`; see the full [[Commands]] reference.

---

## Systems You Will Meet

The campaign is carried by a handful of interlocking systems. Each is documented in depth on the [[Architecture Overview]] and [[Architecture Data Flows]] pages; here is what they *feel* like in play.

### Badges & Memory Fragments
Beating a gym leader unlocks the next level cap and fires a **memory fragment** — a short first-person flash of who you were. They start as formless unease and sharpen over the journey; the seventh ("you signed this charter") is the hard turn. A **town Archivist** NPC will re-read past fragments for you. Internally, a `memory_fragment` scoreboard tracks how many badges you have earned and gates the lore as it deepens.

### The Destabilizing CobbleDollar Economy
The Company is the region's trusted currency verifier, and it is **abusing that trust**. Across the gym journey an instability index (`cd_instability`, 0–100) climbs — roughly **+8 per gym** through gym 7 — and your battle payouts get skewed downward (the rate falls as instability rises). You will *feel* the plot in your wallet long before you understand it. This is the engineered chaos The Company uses to push its own **wheat-backed** money. (It is a deliberate nod to the long-running community debate over the best item to anchor a Minecraft economy — The Company's answer is "whatever *we* control.")

### Wheat Traders & Field Liberation
As money wobbles, **Company wheat traders** appear offering an "alternative" currency. Trade with them and the interaction escalates: trade → recognition → eventually a post-trade ambush as you start **liberating the wheat fields** they monopolize. Reclaiming fields restores prices and earns safe-farm ground. *(The field-liberation layer is still being wired in — see [[Guidebook Act I]] for current status.)*

### Villain Recognition
The Company's people increasingly **recognize your face**. Early grunts manage a confused "have we met?"; mid-journey it sharpens to "you're supposed to be dead"; late, some refuse to raise a hand against you while others panic. The escalation is gated on your badge progress, so it tracks the same arc your memories do.

### The Quest HUD
A boss bar shows your current objective and progress, with a togglable sidebar log — all derived from your existing progress, no separate tracking to maintain. Toggle it with `/ca quest show`, `/ca quest hide`, and `/ca quest refresh`.

### NPC Sight
Certain NPCs actually *see* you — a per-tick line-of-sight check determines whether an NPC can spot the player, which in turn can trigger dialogue, pursuit, or a one-time approach. Practically: line of sight matters. You can be noticed.

### Shrines
Five optional elemental trials — parkour against the clock, a blind gauntlet, a staged boss rush — each a self-contained challenge. They are worth the detour. See [[Guidebook Shrines]].

---

## The Three Acts

The campaign is structured in three acts that map onto the gym route and what comes after.

- **Act I — Infiltration (gyms 1–7).** Walk the route, earn badges and caps, and watch the economy come apart. Grunts and management are scattered across routes and towns; wheat traders surface; the recognition arc warms up. The seventh badge is the inflection point. → [[Guidebook Act I]]
- **Act II — The HQ Raid.** With the journey at a boil, the path leads to **Company HQ** at `[1590 51 1028]`. Fight up through the org to **Acting CEO DJ** — a usurper keeping the seat warm. His defeat **stabilizes the currency** (the index snaps to 25) in a visible, earned "CURRENCY STABILIZED" beat, and the late-game stakes harden. → [[Guidebook Act II]]
- **Act III — The Board & The Mirror.** Clear gyms 8–10, then the **Royal League** (Elite Four → Champion) for the level-100 cap. Beyond that wait the **Board of Directors** and the true final battle — a mirror fight that pays off the whole amnesia arc. Win, and the map is no longer the limit: you leave the curated world for generated terrain and go after the **Ender Dragon**, still hardcore + Nuzlocke. → [[Guidebook Act III]]

---

## The Campaign Arc

```mermaid
flowchart TD
  Start["Sango Town (start, cap 20)"] --> G17["Gyms 1-7\nBug, Grass, Fairy, Fighting,\nWater, Ground, Electric"]
  G17 --> Eco["Economy destabilizes\ncd_instability rises ~+8 per gym"]
  Eco --> HQ["HQ Raid - 1590, 51, 1028\nActing CEO DJ"]
  HQ --> Stab["CURRENCY STABILIZED\nindex to 25"]
  Stab --> G810["Gyms 8-10\nDragon, Ice, Fire"]
  G810 --> League["Royal League\nElite Four then Champion (cap 100)"]
  League --> Board["The Board of Directors"]
  Board --> Founder["The Founder\nmirror battle"]
  Founder --> Ender["Leave the map\nEnder Dragon (still hardcore + Nuzlocke)"]

  subgraph ActI["Act I - Infiltration"]
    G17
    Eco
  end
  subgraph ActII["Act II - The HQ Raid"]
    HQ
    Stab
  end
  subgraph ActIII["Act III - The Board and the Mirror"]
    G810
    League
    Board
    Founder
    Ender
  end
```

---

## Quick Reference

- **Check your progress:** `/ca progress` · **Check your cap:** `/ca levelcap`
- **Quest HUD:** `/ca quest show | hide | refresh`
- **Bail out of a shrine (no penalty):** `/shrine-abort`
- Full command list, including admin tooling: [[Commands]]
- How it all fits together under the hood: [[Architecture Overview]] · [[Architecture Data Flows]]

> **Where to next?** Start with [[Guidebook Act I]] and walk out of Sango. Watch your level cap, mind the safe-zone line, and pay attention to how people look at you.
