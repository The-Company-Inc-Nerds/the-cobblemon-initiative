# Quests: Battle Frontier

**Status:** ✅ Done · 🚧 WIP (partial) · ❌ Not yet implemented — as of the 2026-07-21 audit.

The post-Champion sandbox: eight facilities, each with two challengers and a **Frontier Brain** at
the top, running level ~90–100. It's the mastery grind of the level-85→100 window — decompression
after the story climax, and where you sharpen a team for the last fight. And unlike everywhere else
on the map, **nothing you love dies here.**

> This page is the **roster** (who / where / gated on what). For how each hall *plays* — the safe
> exhibition rule, the ledger and hall passes, the seven-halls counter, and the Warden capstone —
> see **[[Guidebook Battle Frontier]]**.

> [!CAUTION]
> **Endgame — post-Royal-League.** The Frontier only opens to a **Champion** (`royal_league_champion`).
> Everything here fights at **level 90–100**; treat it as the deep end.

> [!NOTE]
> **Safe exhibition — this is not a wager, and it is not real-stakes.** The entire Frontier plateau
> is a **no-death zone**: while you stand on it, a `frontier_active` region tag suppresses **all**
> Nuzlocke faint-damage, party-removal, and whiteout (the same suspension the **Stadium** uses — see
> **[[Guidebook Facilities]]**). You can lose a fight and lose nothing. Every battle is **opt-in** —
> walk away any time, free. There is **no paid-decline** and **no wager**; the old "risk your real
> party against level 100" framing is gone.

---

## How it works

Each facility is a small ladder: beat **both challengers** to open its **Frontier Brain**. Clear a
Brain and its hall is yours. All 24 fights are gated on **royal_league_champion** — you cannot stumble
into a level-100 fight before you've earned the throne. Beyond that gate, the only sequencing is inside
the Deep Dark Cave (the Warden's capstone gate, below).

Two concourse NPCs run the show:

- **Frontier Registrar Odette** — the ledger, the hall-pass window, and the frontier's rumor hub. Sign
  the ledger and you're in.
- **Caretaker Anselm** — old Company-era groundskeeper who tends the frontier's founding plaque.

### Hall passes are prestige, not a gate

Signing the ledger grants your **first hall pass free**; more cost **200 CD** each at Odette's window.
Passes are a pure **CobbleDollars sink** — a receipt beat and a prestige counter. They **never block or
gate a battle.** Every fight is opt-in whether you're holding ten passes or none.

---

## The eight facilities

| Facility | Frontier Brain | Signature team | Challengers |
|----------|----------------|----------------|-------------|
| 🗼 **Battle Tower** | Tower Tycoon **Palmer** | Dragonite · Milotic · Rhyperior *(the Platinum homage)* | Climber Jasper · Contender Mira |
| 🏭 **Battle Factory** | Factory Head **Noland** | Magnezone · Scizor · Metagross · Porygon-Z | Technician Rex · Engineer Lydia |
| 🏰 **Battle Castle** | Castle Lord **Percival** | Empoleon · Slowbro · Dusknoir · Steelix | Knight Aldric · Guard Captain Elara |
| 🎰 **Battle Arcade** | Arcade Star **Dahlia** | Blaziken · Togekiss · Garchomp · Zoroark | Gambler Fritz · Player Suki |
| ⚓ **Battle Port** | Port Admiral **Horatio** | Pelipper · Gyarados · Kingdra · **Lugia** | Captain Stern · Sailor Crest |
| 🔺 **Battle Pyramid** | Pyramid King **Brandon** | Regirock · Registeel · Regice · **Regigigas** | Explorer Marco · Archaeologist Priya |
| 🏪 **Exchange Concourse** | Market Mogul **Sterling** | Zoroark · Chansey · Alakazam · Porygon-Z | Merchant Vance · Trader Fiona |
| 🕳️ **Deep Dark Cave** | Cave Warden **Selene** | Mismagius · Sableye · Gengar · Hydreigon · Tyranitar · **Darkrai** | Spelunker Dirk · Cave Diver Luna |

Each Brain fields a top-of-band team; several carry a signature legendary (Lugia, Regigigas, Darkrai)
as their prestige piece. The two challengers per facility are the warm-up — beat them first. That's
**24 trainers** in all.

---

## The capstone — Cave Warden Selene

The Deep Dark Cave's Brain is **sealed** until you have cleared the rest of the Frontier. Her door
opens only once you hold a **nine-way gate**: **all seven other Frontier Brains** *and* **both cave
challengers** (Spelunker Dirk and Cave Diver Luna) beaten. Until then she won't fight — the dark
stays shut.

Beat her and the whole Frontier is yours: this fires `frontier_all_cleared`, awards the **FRONTIER
CLEARED** title, and pays a one-time **20,000 CD** grand purse. It's the frontier's true finale.

---

## The quest line

The Frontier ships as a set of tracked sidebar lines that hand off to one another (press `]` to
track — see **[[Commands]]**):

1. **✅ Done — The Battle Frontier** — "Sign the Frontier ledger." Points at **Registrar Odette
   @ 3800 159 2997** until you sign, then hides.
2. **✅ Done — The Seven Halls** — "Clear the Frontier halls **$(halls)/7**," a live counter that
   retargets to the next uncleared Brain as each hall falls (all seven Brains cast on the concourse —
   see the table above). Hides once all seven are down.
3. **✅ Done — The Warden** — "The Deep Dark door is open — face the Warden." Fires only in the
   door-open window (all seven halls *and* both cave challengers cleared, Selene still standing) and
   points at **Cave Warden Selene @ 3812 159 2996**.
4. **✅ Done — The Last Honest Signature** *(lore side-beat)* — "Read the founding plaque." Visit
   **Caretaker Anselm @ 3806 159 2999** and read the frontier's dedication plaque. Optional, no battle.

### The plaque

The Frontier is the one place the Company's scrubbing never reached — nobody thought a proving ground
was worth erasing a name from. So it holds the **founder's un-scrubbed dedication plaque**, the only
signature in the region that was never painted over. Caretaker Anselm half-recognizes the founder and
keeps it to himself; both his dialog and the read-out describe the signature **without ever speaking
the name.** (That's held for the Act-3 mirror — see **[[Guidebook Act III]]**.)

---

## Where it sits

The Frontier is the **"after."** It opens only to a Royal League Champion (**[[Quests Royal League]]**)
and is played in the **level 85→100** band as post-story decompression and mastery. It depends on
nothing downstream, gates nothing, and **does not advance the villain plot** — pure long-tail content
for the training window before (and after) the final boss. Other endgame content in the same window:
the **[[Guidebook Nobles]]** and the post-league shrines (**[[Guidebook Shrines]]**). For where the
Frontier sits in the story, see **[[Guidebook Act III]]**.
