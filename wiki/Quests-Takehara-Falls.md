# Quests: Takehara Falls

> *Record under audit. Plinth pending re-verification. Asset under valuation. Every notice in this town is stapled over something older — and the paper underneath is always more interesting.*

**Takehara Falls** is the first gym town of the run — a waterfall town with a bug gym, a museum with an empty plinth, an apiary the Company already bought, and a print stall that refuses to stay covered. You arrive with a level cap of **15**; the **Falls Badge** raises it to **22**.

This page lists every quest and paid encounter in and around Takehara Falls. For where the town sits in the campaign, see **[[Guidebook Act I]]**. Coming from Sango? The road quests are on **[[Quests Blossom Path]]**; the road *out* of town is **[[Quests Harvest Road]]**.

**Status:** ✅ Done · 🚧 WIP (partial) · ❌ Not yet implemented — as of the 2026-07-21 audit.

> [!WARNING]
> **Spoilers — Act I.** This page documents The Company's presence around Takehara Falls — field agents, a canvasser, and an audit or two. Nothing beyond Act I is spoiled.

> [!NOTE]
> **How rewards are listed.** Battle prize money is paid **flat** — trainers pay exactly what they promise. Quest payouts route through a paymaster that prints a receipt and pays the **Verified Rate**: 75–100% of face value, depending on how unstable the CobbleDollar index currently is. Amounts below are face value. Most receipts are unbranded; the handful that arrive on Company letterhead are worth reading twice.
> **Training packs:** *minor* = 3× Exp. Candy XS + 1× Exp. Candy S · *standard* = 2× Exp. Candy S + 1× Exp. Candy M · *major* = 1× Exp. Candy L + 1 random vitamin (HP Up / Protein / Iron / Calcium / Zinc / Carbos).

---

## Quest index

| Quest | Giver | Kind | Tracked on HUD | Headline reward |
|-------|-------|------|:--:|-----------------|
| [The Brood Tower (Gym 1)](#gym-1--the-brood-tower-falls-badge) | Leader Cicada | gym ladder | main quest | **Falls Badge → cap 22**, 1200 CD |
| [Full Tower Clear](#full-tower-clear--the-tower-clear-bonus) | Gym Guide | beat all 4 tower trainers | no | 600 CD + Exp Candy + training pack |
| [Cascade Ascent](#cascade-ascent) | Falls Warden Shou | timed climb | yes | 500 CD + major pack |
| [Notice of Non-Compliance](#notice-of-non-compliance) | Printmaker Mei | swarm defense | yes | 400 CD |
| [Natural History](#natural-history-museum-bones--fossil-revival) | Curator Kenji + Sayuri | dig & donate | yes | 400 CD + one fossil revival |
| [Stakeholder Alignment](#stakeholder-alignment-the-mayors-roof) | *(roof scene)* | doubles battle | no | 460 + 600 CD |
| [Sweetwater Futures](#sweetwater-futures-beekeeper-masumi) | Beekeeper Masumi | fetch | no | 300 CD + a queen Combee |
| [The De-Acquisition Desk](#the-de-acquisition-desk-trader-mayu) | Trader Mayu | trade | no | Elekid Lv 12 |
| [Out of Office](#out-of-office-fisherman-genji) | Fisherman Genji | fetch + wager | no | Poké Bait + 300 CD |
| [Canvasser Patrol](#canvasser-patrol-kazuo) | Kazuo | optional battle | no | 280 CD |
| [Nurse Lila](#nurse-lila--paid-heal) | Nurse Lila | service | — | full heal, costs 100 CD |

> [!TIP]
> **Level context.** Everything in town fights at Lv 9–17 against your starting cap of **15** — Cicada's ace is Lv 17, so the leader is fought *over* your cap. That is by design: every leader on the route sits at your entry cap + 2.

---

## Gym 1 — The Brood Tower (Falls Badge)

|  |  |
|---|---|
| **Giver** | Leader Cicada; the Takehara Gym Guide explains the rules |
| **Location** | Gym tower floors 1–2 at [2055 138 2428]–[2055 151 2501]; garden ladder outdoors — Sora [1903 109 2521], Aiko [1904 109 2521], Cicada [1910 109 2524] |
| **Start** | Talk to any of them — Cicada's own greeting spells out the ladder: *the tower, then Sora, then Aiko, then me* |
| **Repeatable** | One-time |
| **Tracker** | Main quest line — *Defeat the Takehara Falls Gym* |

```mermaid
flowchart LR
    F1["Tower floor 1<br/>Koji · Yuki"] --> N["Optional: each win<br/>softens the ladder"]
    F2["Tower floor 2<br/>Shin · Taro"] --> N
    N --> S["Jr. Apprentice Sora<br/>(flowerbeds)"]
    S --> A["Apprentice Aiko<br/>(greenhouse · doubles)"]
    A --> G["Falls-cave offerings<br/>Glow Berry + Spore Blossom"]
    G --> C["Leader Cicada<br/>Falls Badge → cap 22"]
```

### The ladder

| Stage | Trainer | Team | Levels |
|-------|---------|------|:------:|
| Tower, floor 1 | Bug Catcher Koji [2055 138 2428] | Caterpie, Weedle | 9 |
| Tower, floor 1 | Entomologist Yuki [2055 138 2501] | Spinarak, Ledyba | 9 |
| Tower, floor 2 | Bug Maniac Shin [2055 151 2428] | Nincada, Surskit | 10 |
| Tower, floor 2 | Youngster Taro [2055 151 2501] | Burmy, Combee | 10 |
| Jr. Apprentice | Sora — among the flowerbeds | Beedrill, Beautifly | 12 |
| Apprentice | Aiko — greenhouse center *(doubles: four partners, two at a time)* | Butterfree, Beedrill, Yanma, Ninjask | 14–15 |
| **Leader** | **Cicada** — the arena up the falls | **Scolipede, Heracross (Lum Berry), Vespiquen (Leftovers), Yanmega (Choice Scarf)** | **16–17** |

### Walkthrough

1. *(Optional)* Talk to the **Gym Guide** for the badge / level-cap / hardcore briefing.
2. *(Optional)* Climb the gym tower. Every tower trainer you beat **drains the ladder above**: one win and Sora's team fights with its training potential zeroed; two and Aiko's does; all four and Leader Cicada's own team comes in drained. Skip the tower entirely if you want the ladder at full strength — or clear all four for the [tower-clear bonus](#full-tower-clear--the-tower-clear-bonus).
3. The apprentices are **not in the gym** — beat **Jr. Apprentice Sora** in the flowerbeds (singles), then **Apprentice Aiko** in the greenhouse center (doubles).
4. **Gather the falls-cave offerings.** After you beat the apprentice and talk to Cicada, she logs a small errand: bring her a **Glow Berry** and a **Spore Blossom** from the falls cave (the mouth is at [2125 136 2703]). She will not battle until you hand both over. (Cocoa beans off Blossom Path are optional flavor, not part of the requirement.)
5. Challenge **Leader Cicada** at the arena up the falls. She carries **3× Full Restore** — chip damage alone will not close it, and her Lv 17 Yanmega on a Choice Scarf outspeeds anything at your cap.

### Rewards

- **Tower trainers:** 2× Potion each. **Sora & Aiko:** 2× Super Potion each.
- **Cicada:** **1200 CD** prize (flat) + the **Falls Badge**:
  - Level cap rises to **22**.
  - The Poké Mart's badge-1 shelf opens (Great Balls and better stock).
  - Your first **memory fragment** fires — *"...have we met before?"*
  - The CobbleDollar index takes its first knock (**instability +8**) — the Company reacts to losing a town. Watch the yellow rate line on your next quest receipt.

### Forks

- The tower gate is **any 2 of 4** — you may skip two tower trainers entirely.
- No other branches; the ladder order (tower → Sora → Aiko → gather the falls-cave offerings → Cicada) is enforced by each trainer's willingness to fight.

---

## Full Tower Clear — the tower-clear bonus

|  |  |
|---|---|
| **Giver** | The Gym Guide |
| **Location** | The gym tower (fight the four floor trainers), then the Gym Guide |
| **Start** | Automatic — clearing the tower unlocks the bonus |
| **Repeatable** | One-time |
| **Tracker** | None |

Beat **all four** tower trainers (Koji, Yuki, Shin, Taro) and the Gym Guide pays a **tower-clear bonus** — **600 CD** (Verified Rate) + 4× Exp. Candy XS + a major training pack — on top of the [ladder-drain](#the-gym--leader-cicada) effect each win already has on Cicada's team. Skipping the tower leaves the ladder at full strength; clearing it weakens the leader **and** pays out.

> [!NOTE]
> **Reverted in 0.5.0-alpha.4:** the tower's old "Performance Review" stealth meta — the sight-sentry watchers, the *EYES ON YOU* warnings, the GHOST/Silent Stakeholder cache — was **removed** per showrunner ("there is no reason for a stealth mission here"). The four tower trainers are now plain battle trainers. Only the drain mechanic and this flat tower-clear bonus remain.

---

## Cascade Ascent — ✅ Done

|  |  |
|---|---|
| **Giver** | Falls Warden Shou |
| **Location** | The Plunge Pool at the base of the falls; five marker rings run up to the crest |
| **Start** | Talk to Shou **@ VERIFY** (plunge pool base beside the sign-up board) → *Attempt the Ascent — fifty seconds* |
| **Repeatable** | First clear one-time; a **gold run** repeats after it |
| **Tracker** | Yes — *Pass the five rings before the clock dies* |

### Walkthrough

1. Talk to Shou at the plunge pool. She grants you the **Cascade Boots** — blue Depth-Strider boots for the climb. (Ask about the record board — the standing record is marked **RECORD UNDER AUDIT**, and its holder apparently *never existed*.)
2. Start the **50-second climb**. A countdown bar runs with audio pings at 30/10/5/3/2/1s. The course is built so every missed jump lands in water — no fall damage.
3. Pass through the **five marker rings in order** up the falls before the clock dies.
4. Time out = free retry. No teleport, no damage, no cost — walk back down and go again.
5. After your first clear (and with at least one badge), Shou offers the **gold time**: the same five-ring climb on a tighter clock, repeatable.

### Rewards

- **First clear:** **500 CD** (Verified Rate receipt) + major training pack + 2× Super Potion + 1× Emerald (*"the color of the Takehara badge"*).
- **Gold run (repeatable):** **300 CD** (Verified Rate), money only — no repeat items.

> [!NOTE]
> The gold-time victory line congratulates you on behalf of "Ayame" — the warden's nameplate reads **Shou**. Known naming bug. Also, the five marker rings are placed per-world by the showrunner; if the start warns loudly that no course exists, that world isn't set up yet.

---

## Notice of Non-Compliance — ✅ Done

|  |  |
|---|---|
| **Giver** | Printmaker Mei |
| **Location** | Print stall by the gym entrance, in eyeshot of the canvasser's patrol loop |
| **Start** | Talk to Mei **@ VERIFY** (print stall by the gym entrance — persisted body, no coords in JSON) → *Stand with Mei* |
| **Repeatable** | One-time |
| **Tracker** | Yes — *Protect Mei's stall from the swarm* |

The Company doesn't argue with Mei's moth-wing prints — it sabotages her stall. A swarm of angry bees descends on her counter, and she needs someone standing between them and her.

### Walkthrough

1. Talk to Mei at her stall. When the sabotage swarm drops, the fight is on.
2. **Kill the five angry bees** attacking the stall. Keep them off Mei until the last one is down.
3. Swarm cleared → collect pay from Mei. Look over her counter afterward — the old bridge mural nearby shows a **face painted out twice**, and the print guild's ledger page for that year is missing. File that away.

### Rewards

- **400 CD** (Verified Rate) + 3× Potion + standard training pack.

> [!NOTE]
> **As shipped:** the swarm spawns when Mei's scene is armed in your world; in an unprepared world the stall stands quiet. The tracker waypoint for this quest also currently points at ground level (y 64) rather than the town terraces; trust the descriptions, not the marker.

---

## Natural History (museum bones + fossil revival) — ✅ Done

|  |  |
|---|---|
| **Giver** | Curator Kenji (brush, directions, revival bench); **Sayuri** takes the donation by the bone statue [~1870 114 2330] |
| **Location** | Takehara Museum of Natural History [1902 114 2337]; dig site on the strata shelf below the falls |
| **Start** | Talk to Kenji **@ 1902 114 2337** → *Take the field brush* (donate six bones to Sayuri **@ 1870 114 2330**) |
| **Repeatable** | One-time (donation and revival each) |
| **Tracker** | Yes — *Six bones complete the exhibit* |

The museum's centerpiece plinth was relabeled **PENDING RE-VERIFICATION** by head office. The curator would like a skeleton that head office cannot repossess.

### Walkthrough

1. Take the **field brush** from Kenji; he points you to the soft pale gravel on the strata shelf below the falls.
2. **Brush the dig site** — a ring of 10 suspicious gravel. Most blocks yield bones or pottery sherds; four fossil types (Dome, Claw, Cover, Armor) are the rare pulls. Blocks do **not** regenerate.
3. Bring **six bones** to **Sayuri** by the bone statue → *Donate six specimen bones*. Short of six, she counts twice and sends you back — nothing is consumed.
4. *(Optional)* **Fossil revival** at Kenji's bench: bring a **Dome** or **Claw** fossil. *"The board authorizes one de-extinction per fiscal era."*

### Forks

- **Dome Fossil → Kabuto Lv 10** or **Claw Fossil → Anorith Lv 10** — mutually exclusive, one revival per run.
- **Cover/Armor fossils:** hold them. The lab that reads those plates is in Cyber City, six badges up the road.

### Rewards

- **Donation:** **400 CD** (Verified Rate) + standard training pack + 2× Poké Ball.
- **Revival:** the fossil is consumed; Kabuto **or** Anorith joins at Lv 10.

> [!NOTE]
> The dig site is placed per-world by the showrunner (and can be refreshed with a new ring once mined out). Two small shipped quirks: the short-count line calls the curator "Tamiko" (his nameplate is Kenji), and a couple of Sayuri's *ask-about* buttons may open nothing — the answers live with Kenji.

---

## Stakeholder Alignment (the mayor's roof)

|  |  |
|---|---|
| **Giver** | Nobody — you walk in on it. Two grey suits, **Noboru** [2014 169 2466] and **Chiyo** [2014 169 2464], flanking the Mayor |
| **Location** | The gym roof balcony |
| **Start** | Climb to the roof and talk to Noboru → *Step between the suits and the mayor*. Available from the moment you arrive — no badge needed |
| **Repeatable** | One-time |
| **Tracker** | No |

A *closed stakeholder alignment session*: an eleven-page proposal to divert the falls into "certified irrigation channels" for agricultural yield optimization, and a mayor being talked into signing it.

### Walkthrough

1. Opt into the **doubles battle** against the two Site Assessors — they fight as one team: Zubat 13 / Meowth 14 / Grimer 14 / Drowzee 15.
2. Win, and both suits clear out of town for good.
3. Talk to the Mayor → *Accept the thanks of Takehara Falls*. His parting line is worth hearing: the delegation heading to the next city *"signed their water over months ago."*

### Forks

- *Back down the stairs* — declining leaves the scene waiting. No fail state.
- The Mayor's *Keep it for the town* defers his thanks — claimable any time later.

### Rewards

- **Battle:** **460 CD** flat.
- **Mayor's thanks:** **600 CD** (Verified Rate) + major training pack + 2× Super Potion + 1× Great Ball.

> [!TIP]
> Noboru does a double-take mid-negotiation — *"Do I know you from somewhere? ...never mind what the memo said."* The recognition flickers start here and only get louder. See **[[Guidebook Act I]]**.

---

## Sweetwater Futures (Beekeeper Masumi)

|  |  |
|---|---|
| **Giver** | Beekeeper Masumi |
| **Location** | The Terrace Apiary [~1835 105 2483] — her Combee companion marks the spot |
| **Start** | Talk — the turn-in unlocks once you're holding 8 honeycomb |
| **Repeatable** | One-time |
| **Tracker** | No |

Two Field Agents forward-purchased Masumi's **entire hive yield in perpetuity**. Nobody contracted the *wild* nests.

### Walkthrough

1. Shear **8 honeycomb** — easiest from the two smoke-calmed nests over her terrace campfires; more hang wild along Blossom Path (spiders after dark).
2. Hand them over. The town's honey orders go out under the table.
3. Then the real offer: her contracted hives *"can no longer legally house a second queen."* → **Take in the queen** — a **female Combee, Lv 12**, joins your team. Deferrable with *Not yet — my team is full*; the offer keeps until taken.

### Rewards

- **Turn-in:** consumes 8× Honeycomb; pays **300 CD** (Verified Rate) + standard training pack.
- **The queen:** female Combee Lv 12 — the **Vespiquen** line, precious in a hard run. (Cicada's own Vespiquen hatched on this terrace.)

---

## The De-Acquisition Desk (Trader Mayu)

|  |  |
|---|---|
| **Giver** | Trader Mayu — ex-Company procurement |
| **Location** | The Falls Bridge |
| **Start** | Talk — one listing today: your Paras for her Elekid |
| **Repeatable** | One-time |
| **Tracker** | No |

Mayu names ex-contract Pokémon for the line item that bought them. The Elekid is called **Surcharge** — *"he came with hidden costs"* — collateral on a power contract out of Cyber City.

### Walkthrough

1. Agree to the exchange. She doesn't take handoffs — she *witnesses departures*: open your party and **release your Paras** back to the falls yourself.
2. Confirm the release → **Elekid Lv 12** joins.

### Rewards

- Elekid Lv 12. No money.

> [!NOTE]
> The Paras side of the trade is **honor-system** — nothing verifies you owned or released one. It's your run; the audience is watching.

---

## Out of Office (Fisherman Genji)

|  |  |
|---|---|
| **Giver** | Fisherman Genji — ex-Company auditor, *"made redundant by the water"* (his Psyduck idles nearby, [~1893 105 2470]) |
| **Location** | The Plunge Pool |
| **Start** | Talk — bring him 8 string and he mixes up a batch of bait, a share for you |
| **Repeatable** | Bait one-time; the wager repeats until you win it |
| **Tracker** | No |

### Walkthrough

1. Farm **8 string** from Blossom Path spiders after dark.
2. Hand them in (short counts get a recount) → your bait + pay.
3. With the bait handed over, his **200 CD friendly wager** opens: his two river-dwellers (Poliwag 13 / Goldeen 13) vs. your team. *"One wager a visit."*

### Forks

- **Take the wager** — 200 CD at risk both ways. Lose and he keeps the stake (*"a consulting fee. No hard feelings, and no refunds"*) — but you can re-wager on a later visit. **Win once** and the offer permanently retires into ledger talk.
- Decline and stay friends.

### Rewards

- **Bait turn-in:** consumes 8× String; gives **Poké Bait** + **300 CD** (Verified Rate) + standard training pack.
- **Wager win:** **200 CD** flat.

> [!NOTE]
> The Poké Rod comes from the **Sango Classic** first-win reward now, not from Genji — see **[[Quests Sango Town]]**.

---

## Canvasser Patrol (Kazuo)

|  |  |
|---|---|
| **Giver** | Kazuo — the Company canvasser posting notices around town |
| **Location** | Patrol loop: gym entrance → falls overlook → bridge, sweeping past Mei's stall |
| **Start** | Talk to him |
| **Repeatable** | Battle one-time; the "fee" is not gated |
| **Tracker** | No |

### Forks

- ***Answer for the postings*** — an opt-in battle, only offered **after you hold the Falls Badge**. Meowth 13 / Koffing 14, prize **280 CD** flat, one win only. He does **not** leave town after losing — the patrol continues.
- ***Purchase administrative clearance — 150 CD*** — a fee for a polite line. Pure flavor.
- Say nothing and move on.

> [!WARNING]
> The 150 CD clearance is a **cost with no gate**: the button can be pressed repeatedly, and it "succeeds" even if your wallet can't cover it. There is nothing to buy here. Spend it on Potions.

---

## Nurse Lila — paid heal

|  |  |
|---|---|
| **Giver** | Nurse Lila, Pokémon Center (a Chansey wanders the lobby, [~1900 113 2609]) |
| **Location** | Takehara Falls Pokémon Center |
| **Start** | Talk → *Heal my team — 100 CD* |
| **Repeatable** | Always |
| **Tracker** | — |

A full party heal for a flat **100 CD**. The charge is real and checked — a broke player gets *"Payment declined. The Center does not extend credit."* and no heal. Her small talk is the whole economy in miniature: *"it is per-visit now... adding up is rather the point these days."*

---

## Ambient town life

Not quests — but worth a hello. Three resident Pokémon appear around town as you explore, each with a keeper and one line: the **Chansey** in the Pokémon Center lobby, **Masumi's Combee** at the terrace apiary, and **Genji's Psyduck** at the plunge pool (*"audit season never really ended for either of them"*). Sayuri wanders the museum between donations.

---

## Not in this town

A few nearby quests are often mistaken for Takehara content:

- The **Voluntary Verification Checkpoint** belongs to the road — see **[[Quests Blossom Path]]**.
- **Adjusted Retail** (the price check) and **Greenspace 7, Under-Performing** (the gym-gate audit) belong to the next city — see **[[Quests Hua Zhan City]]**.

---

⬅️ **[[Quests Blossom Path]]** · ➡️ **[[Quests Harvest Road]]** · **[[Guidebook Act I]]** · **[[Home]]**
