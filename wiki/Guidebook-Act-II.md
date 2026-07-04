# Guidebook Act II — "The Company Closes In"

> **Gyms 4–7** · Deepcore City → Gaviota Port → Kalahar Reach → Cyber City → **the HQ Raid**
>
> This is the middle of the run, and it is where the campaign turns. The economy you've been living in starts to **feel wrong on purpose**, the people of The Company start to **recognize your face**, and the memory fragments stop being vague — by Gym 7 one of them puts your own signature in front of you. Act II ends not at a gym but in an office tower, with the **HQ Raid** and the first thing in this whole story you get to actually *fix*.

**Continues from:** [[Guidebook Act I]] · **Leads into:** [[Guidebook Act III]]
**See also:** [[Guidebook Overview]] · [[Guidebook Shrines]] · [[Commands]] · [[Architecture Data Flows]]

---

## What Act II is about

| Beat | Where it lands |
|------|----------------|
| **Recognition turns sharp** | NPCs go from "have we met?" to "you're supposed to be dead." Gated on your badge count (`memory_fragment`). |
| **CobbleDollars feel broken** | `cd_instability` climbs +8 per gym to its **peak (~56) after Gym 7**. Payouts come up short; the propaganda gets *nervous*. |
| **Wheat traders sour** | The "alternative currency" salesmen escalate from friendly trades → suspicious → **hostile ambush** as you liberate fields. |
| **The amnesia closes in** | Fragments 4–7 walk you from *the vault* → *the second signature* → *the boardroom* → **"You signed this charter."** |
| **The HQ Raid (climax)** | After Gym 7, storm `[1590 51 1028]`, fight up the org chart to **Acting CEO DJ**, and trigger the **CURRENCY STABILIZED** payoff. |

```mermaid
flowchart LR
  A["Gym 4<br/>Deepcore (Fighting) · cap 52"] --> B["Gym 5<br/>Gaviota (Water) · cap 58"]
  B --> C["Gym 6<br/>Kalahar (Ground) · cap 63"]
  C --> D["Gym 7<br/>Cyber City (Electric) · cap 68"]
  D --> E["HQ RAID<br/>1590 51 1028"]
  E --> F["Acting CEO DJ defeated<br/>CURRENCY STABILIZED (idx 25)"]
  F --> G["Act III →<br/>Royal League"]

  C -. "Ground Shrine<br/>The Buried Maze" .-> C
  A -. "cd_instability climbs +8 / gym<br/>peaks ~56 after Gym 7" .-> D
```

> [!NOTE]
> Instability numbers below assume you have **not** liberated wheat fields. Liberating fields pushes the index back down — Act II is a tug-of-war between the Company destabilizing and you fighting it back. See [Wheat War](#the-wheat-war-economy-in-act-ii).

---

## GYM 4 — Deepcore City *(Fighting 🥋 → cap 52)*

**Town center / battle area:** ~`[1033 129 3183]` · **Leader Bruno:** `[1045 129 3186]`

### What to expect
- **Type:** Fighting. The gym trainer ladder runs **Black Belt Ryu → Battle Girl Mika → Martial Artist Kenji → Capoeira Rio → Jr. Apprentice Striker → Apprentice Ken → Leader Bruno.**
- **Reward of victory:** the **Fighting Badge** (`badge_fighting`) raises your **level cap to 52** — your party can now train into the low 50s. The badge also fires **memory fragment 4** and advances the Company's plot by one notch (`gym_destabilize`).
- A deep-cut mining city built into stone. Hardcore note: it's a vertical town — fall damage and lava are as dangerous as the gym.

### Story beat — the verifier
Defeating Bruno fires **Memory Fragment 4**, your first concrete glimpse of who you were:

> *A vault of nether stars. Cold. Humming.*
> *People trusted someone honest had counted them. They called you the verifier.*

This is the moment the economy plot stops being background flavor. You are starting to remember that you were the one everyone *trusted to count the money*. The town Archivist NPC can re-read this fragment any time.

### Recognition & villains here
By now the Company's people have shifted from polite confusion to unease. Grunts you meet around Act II towns are **Operatives / Compliance Officers / Market Analysts** (the corporate ladder is the difficulty curve). The first regional managers are already in play behind you — **Regional Manager Shade** unlocks after Gym 2, **Senior Director Vex** after Gym 3 — so the management tier is something you can be cleaning up *during* this stretch.

### Economy reading
After Bruno: **`cd_instability` → ~32.** Your battle payouts are being skewed by `rate = 100 − min(idx/4, 25)%` — at idx 32 that's roughly an **8% haircut** on prize money. The per-payout line and the per-gym actionbar will say so out loud: *"the Company's ledgers waver."*

---

## GYM 5 — Gaviota Port *(Water 🌊 → cap 58)*

**Town center / battle area:** ~`[612 82 3533]` · **Leader Neptune:** `[624 82 3536]`

### What to expect
- **Type:** Water. Ladder: **Sailor Marco → Swimmer Coral → Fisherman Ivan → Surfer Paz → Jr. Apprentice Tide → Apprentice Marina → Leader Neptune.**
- **Reward of victory:** the **Water Badge** (`badge_water`) raises your **level cap to 58.** Fires **memory fragment 5** and another `gym_destabilize` tick.
- A coastal trading port — appropriately, the most commerce-heavy stop in Act II, and a good place to *feel* the currency wobble at the shops.

### Story beat — the second signature
**Memory Fragment 5** is the unsettling one:

> *Two signatures on every star-note.*
> *Double-signed, double-trusted. You cannot recall the second hand. You fear it was also yours.*

This is the lore engine spelled out: CobbleDollars are trusted because **two signatures** vouch for every nether-star reserve. You're remembering that you may have held *both pens* — the seed of the betrayal that's coming in Fragment 6.

### Economy reading
After Neptune: **`cd_instability` → ~40.** The propaganda has fully shifted into its **Act 2 "nervous reassurance"** register — spokes-NPCs over-explain why prices are "adjusting," and **wheat traders** are now openly pitching an "alternative" currency. Payout haircut is now ~10%.

---

## ROUTES & THE GROUND SHRINE

### Between the towns — what to expect
There are **no safe respawns out here.** Towns and shrine sites are **safe zones** (mob spawns suppressed, Nuzlocke faint penalties suspended); everything between them is full-spawn wilderness. Treat every route as the most dangerous part of the run — this is hardcore, and a route mob can end the world as surely as a gym leader can.

This is also where you'll keep running into the **villain encounter web**: scattered Operatives and Senior Agents, and the **wheat traders** whose mood depends on how much of the monopoly you've broken (below).

### The Buried Maze — Ground Shrine
The **Ground Shrine** sits in the orbit of Kalahar Reach and the desert stretch. It is a **`dark_gauntlet`** challenge — start it from its altar NPC or pressure plate (`/cobblemon-initiative shrine ground start`).

| Property | Value |
|----------|-------|
| Name | **The Buried Maze** |
| Type | `dark_gauntlet` |
| Effect | **Blindness** the whole way through — *"Half-strength. Half-sight."* |
| Earthquakes | every **45 seconds**, in a **20-block radius** |
| Objective | navigate blind, survive the quakes, and find the **High Priest** (`ground_shrine_leader`) at the end |
| Escape | `/shrine-abort` at any time, no penalty |

> [!TIP]
> Shrines are **optional** elemental trials, not part of the gym gate. Full mechanics for all five live on [[Guidebook Shrines]]. Bring a Pokémon you can fight blind with — you won't see the terrain, and the earthquakes don't care.

---

## GYM 6 — Kalahar Reach *(Ground 🏜️ → cap 63)*

**Town center / battle area:** ~`[2073 126 4047]` · **Leader Gaia:** `[2085 126 4050]`

### What to expect
- **Type:** Ground. Ladder: **Ruin Maniac Dustin → Hiker Boulder → Archaeologist Juno → Prospector Vince → Jr. Apprentice Dune → Apprentice Terra → Leader Gaia.**
- **Reward of victory:** the **Ground Badge** (`badge_ground`) raises your **level cap to 63.** Fires **memory fragment 6** and `gym_destabilize`.
- A sun-bleached ruin-and-desert reach. The Ground Shrine's Buried Maze is thematically (and geographically) its neighbor.

### Story beat — the boardroom
**Memory Fragment 6** is the betrayal, half-seen:

> *A boardroom. Grey suits, all nodding.*
> *You held both keys to the ledger. They smiled. You should not have let them smile.*

This is the second signature from Fragment 5 paying off: **you held both keys.** You handed a circle of smiling executives the means to corrupt the very trust system you built. You're remembering being usurped — but not yet that it was *you* at the top.

### Recognition & villains here
This is roughly where the recognition arc tips into **alarm.** Where early grunts double-took, mid-arc Company staff are visibly rattled — *"you're supposed to be dead."* You are also at the point where the **wheat-trader ambush** can trigger: with **4+ fields liberated**, traders that used to deal with you turn **hostile** mid-trade (see below).

### Economy reading
After Gaia: **`cd_instability` → ~48.** One gym from the peak. The shops feel actively unreliable now.

---

## GYM 7 — Cyber City *(Electric ⚡ → cap 68)* — **the hard turn**

**Town center / battle area:** ~`[1450 89 1182]` · **Leader Volt:** `[1462 89 1185]`

### What to expect
- **Type:** Electric. Ladder: **Guitarist Amp → Engineer Watt → Rocker Static → Mechanic Gigabyte → Jr. Apprentice Voltz → Apprentice Surge → Leader Volt.**
- **Reward of victory:** the **Electric Badge** (`badge_electric`) raises your **level cap to 68** — and, critically, **unlocks the HQ Raid.**
- A neon corporate-tech city. Fitting: this is where The Company's grip on the narrative becomes literal.

### Story beat — "You signed this charter."
**Memory Fragment 7** is the inflection point of the entire amnesia arc:

> **You signed this charter. Your hand. Your seal.**
> *The Company verifies. Why does the word verifies feel like a knife?*

This is the beat the whole first half builds to. The audience now knows what the protagonist still won't quite let themselves know: **you didn't just work for The Company — you founded it.** The reveal itself is held until after the Royal League (see [[Guidebook Act III]]); Fragment 7 only puts the knife in your hand.

> [!IMPORTANT]
> If you're a returning player or watching the stream: **Gym 7 is the spoiler line.** Everything before it is breadcrumbs; everything after it is the protagonist walking back toward a throne they don't remember leaving.

### Economy reading — the peak
After Volt: **`cd_instability` → ~56 (peak).** This is the worst the currency ever gets. Payouts are taking the **maximum 25% haircut** the skew formula allows (`100 − min(56/4, 25)` clamps to a floor of 75% rate). The propaganda is at its most strained. This is by design — and it's about to be the setup for the only beat in Act II where you *fix* something.

### Tier 3 unlocks soon
The **Dark Urge whisper** system escalates with your progress. After the HQ raid (and Gym 8), it reaches **tier 3** — the shadow-self voice stops being vague unease and starts speaking in the founder's own cold logic. Act II is its last quiet stretch. (Mechanics: 12% chance on a faint **outside** a safe zone, 5-minute cooldown — see [[Architecture Data Flows]].)

---

## THE HQ RAID — climax of Act II

> **Location:** `[1590 51 1028]` · **Unlocks:** after Gym 7 + **4 liberated wheat fields** · **Boss:** Acting CEO DJ

This is not a gym. After the Electric Badge — and once you've **liberated 4 wheat fields** (DJ won't take the meeting while the fields still feed the Company; the quest HUD reads *"Liberate wheat fields, then raid HQ"* until then) — the HQ becomes assailable, and Act II resolves by **storming the office tower** and fighting up the org chart.

### The climb
The raid is a gauntlet up the Company's corporate ladder. The exact roster you face depends on your progress, but the structure is the ascent itself:

```mermaid
flowchart TD
  G["Senior / Elite Agents<br/>(Company Headquarters floors)"] --> A1["Regional Manager Shade"]
  A1 --> A2["Senior Director Vex"]
  A2 --> COO["COO Noir<br/>(Bisharp · Weavile · Hydreigon)"]
  COO --> DJ["ACTING CEO DJ<br/>6 Pokemon · top level 72"]
  DJ --> WIN["Defeat → economy/hq_stabilize"]
  WIN --> STAB["CURRENCY STABILIZED (idx 25)<br/>+ shiny Entei breaks free"]
```

### Acting CEO DJ — what to expect
- A **6-Pokémon** ground-and-dark powerhouse, topping out around **level 72** (Persian, Nidoking, Golem, Rhyperior, **Tyranitar**, and a **shiny Entei** with Sacred Fire). Sand will be up; your cap is 68 — this is a real fight.
- DJ is the **Acting** CEO — a usurper keeping the seat warm. He is the public face of the destabilization, *not* the true power behind it. Beating him doesn't end the story; it ends the *bleeding*.

> [!WARNING]
> **Hardcore reality check.** DJ's team is above your level cap, runs sandstorm chip damage, and a Choice-Band shiny Entei outspeeds and one-shots frail mons. Come full, scout safe-zone heals, and remember a faint here is permanent.

### The payoff — CURRENCY STABILIZED
Defeating DJ runs `economy/hq_stabilize`, which **resets `cd_instability` from its ~56 peak down to 25** — the single biggest, most *earned* swing in the game. You get a beacon-chime title splash:

> **CURRENCY STABILIZED**
> *The Company's grip on the ledger loosens — for now.*
> `[Economy] CobbleDollar stability restored to 25/100.`

Mechanically your **payouts immediately recover** (the haircut drops from 25% toward ~6%). Narratively, this is the first thing in the whole story the protagonist gets to *fix* rather than just survive. DJ's defeat also releases a **shiny Entei** that "breaks free from The Company's control."

> [!NOTE]
> Stabilized to **25, not 0.** The Company is wounded, not beaten. The index holds at 25 through Gyms 8–10 — a reminder that the real rot is still in the Boardroom, waiting for [[Guidebook Act III]].

---

## The Wheat War economy in Act II

The Company's endgame is to **replace** CobbleDollars with a **wheat-backed currency they monopolize** — they occupy the wheat fields. Act II is where that conflict becomes interactive.

### Wheat traders — trade → recognize → ambush
Roaming **wheat traders** are the alternative currency made flesh. Their behavior escalates off a shared **`fields_liberated`** counter:

| Fields liberated | Trader behavior | Tag |
|:---:|------|------|
| **0–1** | Normal trades — they're just salesmen | *(none)* |
| **2–3** | **Recognition** — they start eyeing you, suspicious | `wheat_trader_suspicious` |
| **4+** | **Hostile ambush** — they recognize the founder mid-trade and attack | `wheat_trader_hostile` |

The traders are wired to NPC Sight (`can_see_player`) and these tags; the more of the monopoly you break, the more the Company's own field operatives realize *who you are* — and the more dangerous a "trade" becomes.

### Field liberation
**Liberating wheat fields** is how you fight the destabilization back:
- Each liberated field **restores wheat prices**, pushes the instability index back **down**, and unlocks **safe-farm ground**.
- A **set-piece** field plus scattered **minor** fields make up the war; clearing them gates the HQ raid and feeds the trader escalation above.

> [!NOTE]
> The Wheat War's field-liberation scoring (the `fields_liberated` counter and wheat-price restoration) is the part of Act II still being wired in (**P4**, in progress). The trader tier logic and `cd_instability` swings are live; the granary buyer and full liberation set-piece are designed but landing across patches. Treat field counts here as the intended tuning.

---

## Act II progression at a glance

| # | Town | Type | Cap | Badge ID | Memory Fragment | `cd_instability` |
|:-:|------|------|:---:|----------|-----------------|:---:|
| 4 | Deepcore City | Fighting 🥋 | 52 | `badge_fighting` | frag_4 — *the verifier / nether-star vault* | → ~32 |
| 5 | Gaviota Port | Water 🌊 | 58 | `badge_water` | frag_5 — *two signatures, the second hand* | → ~40 |
| 6 | Kalahar Reach | Ground 🏜️ | 63 | `badge_ground` | frag_6 — *the boardroom, both keys* | → ~48 |
| 7 | Cyber City | Electric ⚡ | 68 | `badge_electric` | **frag_7 — "You signed this charter."** | → ~56 (peak) |
| — | **HQ Raid → Acting CEO DJ** | — | — | — | *(no fragment)* | **→ 25** |

*Level cap unlocks the moment you defeat that gym's leader (the badge advancement drives `LevelCapManager`); the cap is always the highest you've achieved. Fragments fire once per badge and are re-readable via the town Archivist. Instability values assume no field liberation.*

---

## Quick reference

- **Check your standing:** `/cobblemon-initiative progress` (badges, defeated trainers, cap) and `/cobblemon-initiative levelcap`.
- **Quest HUD:** `/ca quest show|hide|refresh` — the sidebar's main line tracks your current objective; through Act II it walks you toward the HQ.
- **Start the Ground Shrine:** `/cobblemon-initiative shrine ground start` · **bail out:** `/shrine-abort`.
- Full command list: [[Commands]]. How these systems wire together: [[Architecture Data Flows]].

---

**◀ Previous:** [[Guidebook Act I]] — gyms 1–3, the journey begins, the economy still feels stable.
**▶ Next:** [[Guidebook Act III]] — the Royal League, the Board of Directors, and the mirror battle against **The Founder**.
