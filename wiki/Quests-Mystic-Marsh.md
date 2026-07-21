# Quests: Mystic Marsh

The third town on the road, and the last place the CobbleDollar still *feels* honest. Mystic
Marsh is a bog settlement built on stilts and superstition — no real Pokémon Center, a Fairy Gym
that fights with mirrors, and a shrine on its edge that asks something stranger than a battle.

**Status:** ✅ Done · 🚧 WIP (partial) · ❌ Not yet implemented — as of the 2026-07-21 audit.

> [!NOTE]
> **Content status:** the **gym** (Leader Titania, her ladder, and the Mirror-Match mechanic),
> the **Fairy Shrine** trial, and the town's three tracked side-quests — **Wisps in the Reeds**,
> **Verified Weather**, and **The Mirebloom Paddy** — are all shipped and playable. The two
> untracked town beats (**The Water Remembers** and **The Alternative**) are signpost/trade
> encounters with no turn-in.

---

## Quest index

| Quest | Type | Giver / gate | Status |
|-------|------|--------------|:------:|
| **Gym 3 — Titania (Fairy Badge)** | Gym | Marsh gym | ✅ shipped |
| **Fairy Shrine — "Five Tests of the Heart"** | Shrine (optional) | Shrine edge of town | ✅ trial shipped |
| Town side-quests | Side | see **Side Quests** below | ✅ shipped |

---

## Gym 3 — The Mirror Gym (Fairy Badge ✨)

> **Leader Titania** · marsh arena at `[1073 65 2441]` · **cap → 37** · **memory fragment 3**

**Entry cap: 30** (from the Grass Badge). Titania's ace sits at **32** — two over your cap, as
every leader does. You fight her underleveled; the gimmick is how you tilt the odds.

### The gimmick — Mirror Match

Titania reads your **lead Pokémon** and answers with one of several **illusion-variant teams**
built to punish that choice. Declare your opener carefully: the team she brings is a reaction to
yours, so the "right" lead is the one you're prepared to fight *around*, not the one you think is
strongest. (The weakened-ladder rule below still applies — softening her first makes whichever
variant she picks more survivable.)

### The ladder

Beat the rank-and-file and Titania's team is drained a step at a time — the marsh trainers are
spread through the gym, not stacked at the door.

| Rung | Who | Team |
|------|-----|------|
| Fairy Tale Girl Luna | trainer_1 | Clefairy 24, Snubbull 25 |
| Hex Maniac Stella | trainer_2 | Spritzee 25, Swirlix 25 |
| Pokémon Ranger Lyra | trainer_3 | Marill 25, Ralts 25 |
| Artist Viola | trainer_4 | Cottonee 25, Spritzee 26 |
| Jr. Apprentice Nixie | jr_apprentice | Kirlia 26, Granbull 26 |
| Apprentice Faye | apprentice | Kirlia 28, Granbull 28, Togetic 27 |
| **Leader Titania** | leader | **Clefable 30, Mawile 31, Wigglytuff 31, Gardevoir 32 (ace)** |

> Steel and Poison are the marsh's fear; a solid Steel-type walls most of this room. Mawile (Steel/Fairy)
> is the wrinkle — don't lead your Steel into her.

### Rewards

The standard badge packet: a **flat battle prize** (never haircut by the instability index), the
**level cap → 37**, **memory fragment 3**, and the **next Pokémart tier**. See
[[Quests Main Story]] for the full badge-reward table and the level-cap ladder.

---

## Fairy Shrine — "Five Tests of the Heart" ✨

On the edge of town sits the **Fairy Shrine**, guarded by **High Priestess Aurora**. It's the one
shrine that isn't a fight first: you bring your **lead Pokémon** to the altar and prove its bond
through **five tests** (`/cobblemon-initiative shrine fairy test <name>`), then a solo battle.

Optional and **not on the gym route** — but a Fairy Shrine Crystal + a loot stack waits at the end.
Full rules, the hardcore warning, and the other four shrines are on **[[Guidebook Shrines]]**.

---

## Side Quests

> [!NOTE]
> **Shipped.** Three of the marsh's town beats are tracked side-quests you can follow with the quest tracker — **Wisps in the Reeds**, **Verified Weather**, and **The Mirebloom Paddy**. The other two (**The Water Remembers** and **The Alternative**) are untracked: a rumor-hub signpost and a trade-only pitch, with no turn-in.

The marsh is a town of charm-sellers and luck-readers, so it's the first place the Company's "verified value" starts to *sound* thin. The civilians can feel the money going wrong even though none of them can see why — and a long-serving Company steward south of town does a double-take he can't explain.

| Status | Quest | Giver | What you do | Reward |
|:------:|-------|-------|-------------|--------|
| ✅ Done | **The Water Remembers** | Fen-Nurse Wisteria `@ 1068 65 2465` | Heal at the marsh Center; hear the rumors that point you at the town's other quests | Paid healing + the town's quest board |
| ✅ Done | **Wisps in the Reeds** | Charm-Weaver Marigold `@ 1076 66 2452` | Bring 8 string from the reeds; keep a warding charm, then an optional friendly wager | ~300 CD + training pack (+ 200 CD wager) |
| ✅ Done | **Verified Weather** | Verified Clerk Osric `@ 1082 66 2448` | Witness the exchange board "recalibrate" three times and sign off | A deliberately short "witness fee" (~60 CD) |
| ✅ Done | **The Alternative** | Sedge `@ 1058 78 2478` | Meet the marsh's wheat trader and hear the alternative-currency pitch | Trade goods; feeds the Wheat War thread |
| ✅ Done | **The Mirebloom Paddy** | Steward Halvard `@ 1229 90 2820` | Fight the guard at the fenced paddy and free the field | 500 CD + the field liberated |

### The Water Remembers — ✅ Done

> Giver: **Fen-Nurse Wisteria** `@ 1068 65 2465` (behind the marsh Center counter).

There's no free healing machine in the marsh — **Fen-Nurse Wisteria** heals your team for the posted rate, same as every nurse on the route (price drifts up with the instability index). She's also the town's rumor hub: ask what needs doing and she'll point you at Marigold's charm stall and the fenced paddy south of town, and name the road out to Deepcore. Later she delivers the first civilian "the money feels wrong" murmur — coin comes up light in her drawer — but she reads it as the fen's bad luck, not anything she could name. This is a signpost, not a fetch: no turn-in, just the town's quest board.

### Wisps in the Reeds — ✅ Done

> Giver: **Charm-Weaver Marigold** `@ 1076 66 2452` (the charm stall).

**Charm-Weaver Marigold**'s will-o-wisp charms have gone dark for want of real silk. Gather **8× string** from the marsh reeds after dark and hand it over; she restrings the lot and you keep one that wards against a hard hit. The turn-in pays out around **300 CD** plus a training pack. After that she offers an **optional, decline-able friendly wager** (200 CD on the line) against her two low-level charm Pokémon — printed stake, no penalty for walking away, and she reads your luck for free either way. Her reading names Deepcore as "stone and fists ahead," and she notes, unsettled, that the cards keep coming up about *you*.

### Verified Weather — ✅ Done

> Giver: **Verified Clerk Osric** `@ 1082 66 2448` (the exchange board).

**Verified Clerk Osric** mans the Company's exchange board and is very keen to reassure you that the flickering rate is a "healthy, temporary, entirely planned recalibration." His micro-quest: watch the board "settle" three times and confirm to head office that the rate is stable, for a small **witness fee**. The joke is that the fee pays out visibly *short* — the receipt reads *ADJUSTMENT: rounding*, in the Company's favor. No fight, no risk. This beat only surfaces once the instability index has climbed enough for the cheer to sound thin, and it echoes the price-check work you may have done back in Hua Zhan.

### The Alternative — ✅ Done

> Giver: **Sedge** `@ 1058 78 2478` (the Willowmire causeway).

**Sedge** is the first wheat trader you've met since Hua Zhan, set up on the marsh causeway. At this point in the run he's **trade-only and not hostile** — he just makes the alternative-currency pitch, selling the Company's grain economy to a town of charm-buyers primed to distrust "verified value." Hearing him out feeds the ongoing **Wheat War** thread ([[Guidebook Wheat War]]). The wheat traders keep books on faces, though — the more Company fields you free across the region, the more pointed their small talk becomes.

### The Mirebloom Paddy — ✅ Done

> Giver: **Steward Halvard** `@ 1229 90 2820` (the fenced paddy gate, south of town).

The Mirebloom Paddies were a town-communal marsh-rice field until the Company fenced them and put **Steward Halvard** on the gate. Walk up and he blocks you — but he's served long enough to react to you as though you're someone the Company told him to forget, someone who's "supposed to be filed." He fights you for the paddy (no way to pay him off — the field itself is the stake), and beating him **frees the field**: prize **500 CD**, the instability index eases, the shop tier improves, and the wheat economy loses ground. He doesn't pursue, so the fight is opt-in by geography. His parting line points you on toward Deepcore.

---

## Where it sits on the road

Mystic Marsh is the **end of the "stable" feel** — the instability index ticks toward 24 here, and
the Company's people start doing double-takes. It's the third of ten gyms; the route continues to
**Deepcore City** (Fighting, gym 4), where the money begins to visibly drift. The gym's mechanic and
all ten gimmicks are collected on **[[Guidebook Gym Mechanics]]**; the full route order is on
**[[Guidebook Route Map]]**.
