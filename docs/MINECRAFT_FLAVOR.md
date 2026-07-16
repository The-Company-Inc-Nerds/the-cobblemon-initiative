# Making the Adventure Feel More Minecraft — Design Spec

> **This is a PLAN, not an implementation.** Nothing here is wired yet (except the already-shipped
> post-story reframe + Founder elytra send-off, noted in §0). Build later from these blocks.

## The insight this leans on
The adventure is *already* secretly a Minecraft story — it just keeps it as subtext:
- The villain plot is **money vs. money**: CobbleDollars backed by **nether stars** vs. a **wheat
  (farming) commodity monopoly**. That is Minecraft economics (the Nether and the farm).
- The one overworld boss is the **Ender Dragon** (the Ryujin rift, gym 8).
- Travel is **survival** (routes spawn mobs + phantoms; towns are safe zones).
- Sidequests already trade in Minecraft items (string, paper, iron, dragon scales) and activities
  (fishing derbies, the Deepcore mine, wheat farming).

The goal below is to make that Minecraft layer **visible and interactive** instead of subtext —
without asking the showrunner to hand-build structures on the UPM map.

---

## §0 — Already done (post-story reframe)
The Ender Dragon is now the **mid-game Ryujin rift**, so the post-story goal was reframed from
"hunt the Ender Dragon later" to an **open Minecraft sandbox** send-off, and beating the Founder now
hands the player an **elytra + fireworks** ("the map ends; the world does not. Fly."). This is the
seam the rest of these ideas build on: the endgame IS open-world Minecraft.

---

## §1 — Homestead: *Claim the Field* (the build thread) ★ your favorite

**The payoff you want:** Minecraft is build-focused, so the Wheat War's reward should be **a place to
build**, not just a number going down. Each field you liberate becomes **yours to settle** — a
mid-game home base and the first foothold of the post-story sandbox.

### Solving the world-editing concern (the whole point)
**The showrunner builds nothing. The player does all the building.** Here is why this needs no map edits:
1. The ten fields **already exist** on the map as regions (`install.json` FARM zones, gated
   `field_freed`/`farm_N`) — they are placed Company wheat farms today.
2. Liberating a field today just flips economy numbers. The change is a **region-RULE flip**, not new
   geometry: an occupied field → a **claimed, griefing-safe, buildable homestead**.
3. `mobGriefing=false` is already set globally (gamerules), so player builds are **already protected**.
4. The player supplies the blocks and the creativity — vanilla Minecraft building. No prefab structures.

So the only "authoring" is toggling rules on regions that already exist.

### Mechanics (all datapack + the existing SafeZone/region system)
On `liberation/free_field {field:farm_N}`, in addition to the current effects, mark `farm_N` as a
**liberated homestead**:
- **Safe zone:** flip the `farm_N` zone from Company-occupied to a SafeZone (mob spawns suppressed +
  Nuzlocke death suspended there — reuse the exact town/shrine SafeZone rule). The field you bled for
  becomes a place your team can't die. Reuses `install.json` zones + `NuzlockeConfig.SafeZone`.
- **Deed + starter kit (first liberation only):** give a named **Homestead Deed** item (a written
  book or filled map) + a nudge kit — wheat seeds, saplings, a stone hoe, a few torches — so the
  player immediately *can* farm/build. One `loot`/`give` block, guarded on a one-shot tag.
- **Fanfare:** the existing liberation ceremony already fires (title + fireworks); add one line —
  "This ground is yours now. Build on it." — so the intent reads.
- **Optional claim marker:** the player may place a lectern/banner as a "homestead sign"; purely cosmetic.

### The returning farmer (activation via an NPC) — extends a shipped pattern
Rather than a silent region-flip, a **returning tenant** shows up after you free the field and *grants*
the homestead — agency + a face + a story beat. **This already exists for farm_1:** the Deng family
homecoming (`old_deng.json` / `deng_haoran.json` / `homecoming.mcfunction`, the "Tenants of Record"
quest) walks the displaced tenants back to Firstfurrow once it's freed. Generalize that:
- On liberation, a **Farmer / returning-tenant NPC** appears at (or walks back to) the freed field:
  *"This was our ground before the Company fenced it. You took it back. It is yours to work now —
  build, plant, make it a home. We will keep the soil turned for you."*
- A dialog button is the **activation**: it flips the field to a safe-build homestead + hands the deed
  + starter kit (so claiming is a choice, not automatic). The farmer then persists as a neighbor
  (flavor, maybe a small produce trade or a heal).
- Ties directly into §the passive income below — the farmer is the one who "pays you the field's yield."

### Passive income via a HOMESTEAD BEACON — *your farm pays you*, and you mine to grow it ★
The Wheat War's whole point is the Company extracting from these fields; the payoff is the field
producing **for you**. Since it's survival and you can build anywhere, the reward keys on a deliberate,
tiered build the player raises on the field: a **beacon**. This fuses **building + mining + income + the
currency plot** into one self-reinforcing loop, and it scales across multiple fields.
- **The quartermaster — Mayor Suzune of Takehara Falls.** After you free the **first field** (farm_1
  Firstfurrow) *and* finish helping her (the existing `sq_mayor_roof` / `sq_mayor_suits` quest), Suzune
  gives you your **first beacon** — but NOT the pyramid. Thereafter she **sells more beacons for
  CobbleDollars** — a reinvestment loop: homestead income buys more beacons → more homesteads → more
  income (the per-beacon price can escalate so it stays a real choice). She **rings you on the PokéPhone**
  when a new beacon is in stock and you can afford it (see `PHONE_AND_CARE.md` §1). The per-field **returning
  farmer** (above) still greets you at each freed field and grants the safe-build claim; Suzune is the
  beacon *economy hub*, unlocked early (gym 1–2 band) so the mine/build/earn loop runs the whole journey.
- **Grow it by mining.** You supply each beacon's pyramid by **mining mineral blocks** (iron → gold →
  diamond/emerald → netherite) and stacking the layers on the field. **Both** how big the pyramid is
  *and* what it is made of feed the yield — so digging deeper for rarer blocks pays off (the "mine to
  find more powerful material" loop).
- **Currency tie-in — fork (c) CHOSEN (star unlocks the top, not the whole thing).** Because the
  `HomesteadManager` (below) reads the **pyramid directly** (size + material), income does NOT require the
  vanilla beacon to be lit — so the homestead works **early** (Suzune's beacon + CD + a mined pyramid),
  no Wither needed to start. A **nether star** then gates the **top tier / max output** (and lights the
  vanilla beam for the Haste bonus). Nether stars ARE the CobbleDollar backing and drop from **Withers**
  (rare, deadly in hardcore), so *maxing* a homestead is the endgame commitment while the loop is
  accessible from the gym 1–2 band. Accessible early **and** the currency thread stays meaningful — this
  is why (c) works: reading the pyramid ourselves lets us decouple "earns something" from vanilla
  star-activation, and reserve the star for the top rung. This is the cleanest of the three forks.
- **Tier → production:** the field's daily CobbleDollar yield scales with the beacon's pyramid tier
  (1→4). Tier 1 (9 blocks) = a trickle; tier 4 (164 blocks) = the field's max. The mining loop **is** the
  economy engine — the more you dig and build, the more it pays.
- **Vanilla effects are a bonus:** the beacon grants Haste / Regen / Speed in range — Haste means you
  mine the next layer faster, so the loop reinforces itself and the homestead becomes a genuine base.
- **Multiple fields:** raise a beacon on each liberated field; each pays by its own tier. Total is
  **capped** (per-field and/or aggregate) so it rewards real mining investment without becoming a faucet
  in a hardcore economy.
- **Tracking is feasible (YES — this is the crux).** A small **`HomesteadManager`** (Java) tracks each
  homestead beacon per player: **register** its position when Suzune grants/sells it or when it's placed
  on a liberated field, then read its power. The mod **already reads block entities in Java**
  (`LootChestManager`), so both halves reuse an existing pattern:
  - **Size** = the beacon's pyramid tier (1→4): `BeaconBlockEntity` exposes its level directly. Trivial.
  - **Material** = scan the pyramid base blocks under the beacon and tally by type
    (iron/gold/diamond/emerald/netherite) → a quality multiplier. A bounded volume scan (≤164 blocks).
  - **Yield = base(size) × quality(material)**, summed over the player's beacons, capped. Pay out at the
    existing **`economy/dawn`** day-latch, framed by the farmer/mayor as "your cut of the harvest."
  (Datapack-only fallback if you want zero Java: count the pyramid layers via `if block` checks — works,
  just verbose. The Java path is cleaner and the precedent already exists in this codebase.)
- **Simpler v1 fallback:** if the beacon-tier read is deferred, a **bed/respawn-point-inside-the-field**
  check (`PlayerProgressManager` already reads `spawnX/Y/Z`) is a low-effort proxy for "you live here"
  paying a flat stipend — ship that first, upgrade to the beacon-tier engine later.

### Open questions (showrunner)
1. **All 10 fields buildable, or designate 1–2 as *the* homestead** to avoid sprawl? (Recommend: all
   liberated fields safe-buildable, but Firstfurrow is *home* — deed + kit + the farmer; the rest are
   safe-build bonus land that still pays the stipend if you set spawn there.)
2. Deed item: a **written book** (lore), a **filled map** centered on the field, or both?
3. **Beacon income curve + cap:** CD/day per pyramid tier (T1→T4) and a per-field and/or aggregate cap —
   generous enough that mining is worth it, never a faucet in hardcore. Does the player supply the
   activating **nether star** (recommended — it closes the currency loop and paces multi-field), or is
   the beacon granted pre-activated?
4. Starter kit contents / whether the field keeps its Company wheat to harvest / do you also seed a few
   starter mineral blocks so tier 1 is immediate and tiers 2–4 are the mining goal?
5. Should liberated fields also **suppress the Dark Urge whisper** (they're home now, not wilderness)?

### Effort
Medium-low. No world edits. ~1 function edit (`free_field_apply`) + a deed/kit function + a SafeZone
flag per field (data). The riskiest part is confirming the SafeZone rule applies cleanly to a
mid-map field polygon — a one-time test.

---

## §2 — Advancement Interleave (the systemic thread) ★ selected

**Concept:** vanilla Minecraft advancement toasts already fire as the player plays. Hook the key ones
to fire **in-story flavor** so the Minecraft milestones and the Company/currency plot acknowledge each
other — the crossover feels intentional, not incidental. **No world edits.**

### Mechanics
You cannot attach a reward to a *vanilla* advancement, but you can ship a **mirror advancement** that
copies the vanilla criteria/trigger and give *it* a `rewards.function`. So: one hidden mirror
advancement per hook → a small flavor function (tellraw/title, one-shot guarded).

### The hooks (each ties a Minecraft milestone to the story)
| Minecraft milestone | Mirror trigger | Flavor beat (macro-safe, one-shot) |
|---|---|---|
| **Enter the Nether** | `minecraft:nether/root` | "Nether stars back every CobbleDollar in your pocket. This is where the Company's money is really minted — and why they guard it." |
| **Mine a diamond** | `story/mine_diamond` | "Diamonds. The honest backing, before the Company decided wheat was easier to *control* than to dig for." |
| **Summon / kill a Wither** | `nether/summon_wither` | "A nether star. The Company would pay a fortune for it — it *is* their fortune. Careful who sees you holding one." |
| **Get an elytra / go to the End** | `end/root` | "The rift you closed over Ryujin was a tear from *here*. You already fought the dragon. This is just the rest of the sky." (post-story) |
| **First night / bed** | `adventure/sleep` or a night-survived trigger | survival-flavor: the road is Minecraft; the towns are the only refuge. |
| **Balanced diet / eat** | `husbandry/balanced_diet` | light hunger/survival flavor. |

Gate the currency ones on `wheat_war_active` / badge count so they land after the plot is legible.

### Open questions
1. Which hooks to ship (recommend the 3 currency ones — Nether, diamond, Wither — they're the thesis).
2. Do these fire as **toasts** (a matching custom advancement toast) or **chat/title** flavor, or both?

### Effort
Medium. Per hook: one mirror advancement JSON (copy the vanilla criteria) + one flavor function.
No new engine code; the mod already ships custom advancements.

---

## §3 — Milestone Minecraft Loot (cheap texture)
**Concept:** the Pokémon journey should pay out in **recognizable Minecraft treasure**. Add iconic MC
items to the existing reward chain (badge grants, champion, board, founder):
- A **diamond** per badge (badge_grant functions) — a visible "you leveled up in Minecraft too."
- A **beacon** (or netherite gear) on **Royal League Champion**.
- **Netherite** on Board cleared (the Frontier already gives netherite ingots per battle).
- **Elytra** on Founder — **done** (§0).

**Effort:** trivial (add `give`/`loot` lines to reward functions). **Open Q:** which items at which beat,
and whether a beacon is too strong for hardcore mid-run (recommend beacon at Champion / near-endgame).

---

## §4 — Nether-Star Reserve (the currency made physical) — world-edit-light option
**Concept:** *show* why the money is nether-star-backed. Two ways, in order of build cost:
- **(a) Lore beat, no build (recommended for now):** a Company document / defector dialog reveals the
  **reserve** — the Company hoards nether stars to back CobbleDollars, and the wheat monopoly is the
  plan to make that backing worthless. Pure dialog + a scrubbing-artifact item. Zero world editing.
- **(b) Player-summoned reserve raid, minimal build:** a marked spot (a beacon base already on the
  map, or none) where the player **summons and fights a Wither** to claim a nether star that
  destabilizes the currency further (like liberating a field). The player builds the Wither; no prefab.
- **(c) Full Nether act (big, deferred):** a proper Nether excursion into the Company's star mine.

**Effort:** (a) low, (b) medium, (c) large. Recommend (a) now, (b) as an optional set-piece later.

---

## §5 — Minecraft advancements gating gym progression (analysis)
**You already have the marquee version, and it works:** the **Ender Dragon** gates gym 8 (the Ryujin
rift — you must slay it to challenge the Dragon leader). That is the model: a huge Minecraft achievement
on the critical path, thematically perfect (a dragon gates the *Dragon* gym), and late enough that the
player has a strong team and a fighting chance in hardcore.

**The risk with adding MORE mandatory mainline gates:** pacing + hardcore safety. Nuzlocke runs are
tense and the Pokémon journey is the spine; forcing a *dangerous* Minecraft detour onto the critical
path (e.g. "clear the Nether before you may challenge gym 5") can end a run on a Minecraft chore and
break the flow. One marquee gate reads as an epic set-piece; several read as busywork tax — and a
deadly one mid-run is a hardcore trap.

**Where advancement-gating fits well instead:**
- **Gym gimmicks that ARE Minecraft** (not hard gates) — the mainline already does this: Ryujin = the
  dragon, Deepcore = mining (the Iron Ladder), Scorchspire = fire/Nether-adjacent. Lean into *gimmicks*
  for the crossover feel without a blocking gate. This is the safest, highest-flavor lever.
- **Gate SIDE / ENDGAME content on advancements** (optional — no mainline friction): a shrine that
  needs *Enter the Nether*, a noble that needs a diamond, a Frontier hall themed to an advancement.
  Rewards Minecraft engagement without ever blocking the story.
- **If you gate a mainline gym anyway, use a LOW-RISK natural milestone** the player hits in survival
  regardless (mine iron, sleep through a night, craft a tool) — never a deadly Nether/End run.

**Recommendation:** keep the single big mandatory gate (the dragon); do **not** add mandatory deadly
ones; express the rest through gym *gimmicks* and *optional* advancement gates on side/endgame content.
This pairs naturally with §2 (advancement interleave) — the same triggers that fire flavor can gate a
shrine/noble as a bonus.

## §6 — Minecraft requirement per gym (the escalating ladder)
Each gym already has a gimmick; this maps a **type-matched Minecraft task** onto each, escalating from
trivial to the marquee dragon. Design rule (from §5): early = trivial/low-risk survival you'd do anyway;
the **one hard mandatory gate is the dragon (gym 8)**; late gyms escalate; nothing else forces a deadly
detour. Most are best as **soft** (fire flavor / a light nudge when you do the thing, or an optional
gate on the gym's side-content) rather than a hard block. "Trigger" = the vanilla advancement to mirror
(per §2's mirror-advancement mechanic).

| # | Gym (type, cap) | Existing gimmick | Minecraft requirement | Trigger | Risk / kind |
|---|---|---|---|---|---|
| 1 | Takehara Falls (Bug, 22) | Cicada lift | **Harvest honey** from a hive (bug ↔ bees; ties to Beekeeper Tomo's Sting quest) | `husbandry/safely_harvest_honey` | trivial · soft |
| 2 | Hua Zhan (Grass, 30) | Vine walls | **Plant + harvest a wheat crop** (grass ↔ farming; lands right as the Wheat War is revealed here) | `husbandry/plant_seed` → wheat | trivial · soft |
| 3 | Mystic Marsh (Fairy, 37) | Mirror match | **Enchant an item** (fairy ↔ magic) — or brew a potion (marsh ↔ witch hut) | `story/enchant_item` | low · soft |
| 4 | Deepcore (Fighting, 44) | Gauntlet | **Mine a diamond** — a mining town should make you prove you can dig | `story/mine_diamond` | low-med · **soft gate** (fits the mine) |
| 5 | Gaviota Port (Water, 50) | Tide clock | **Reel in a fish** (or a fishing treasure) — the harbor | `husbandry/fishy_business` | trivial · soft |
| 6 | Kalahar Reach (Ground, 56) | Mirage rings | **Mine obsidian** (ground/deep digging; quietly preps a Nether portal) | (obtain `minecraft:obsidian`) | low · soft |
| 7 | Cyber City (Electric, 62) | Stadium gate | **Wire redstone** — place a working redstone device or a lightning rod (electric ↔ tech) | (craft/use `lightning_rod`/redstone) | low · soft |
| 8 | **Ryujin Keep (Dragon, 68)** | **Rift dragon** | **Slay the Ender Dragon** (the overworld rift) — SHIPPED | `dragon_slain` (RiftDragonManager) | high · **HARD GATE ✅ (the one mandatory)** |
| 9 | Nifl Town (Ice, 74) | Whiteout | **Cross powder snow with leather boots** or obtain packed/blue ice (ice-native) | `husbandry/…` / obtain ice | low · soft |
| 10 | Scorchspire (Fire, 80) | Banked coals | **Forge netherite** / bring back a blaze rod (fire ↔ the Nether) — endgame, strong team | `nether/obtain_ancient_debris` | med (late) · soft gate / big |

**How to wire cheaply:** each row = a hidden mirror advancement (copies the vanilla trigger) whose
reward function sets a `mc_<gym>_done` tag + a flavor line. Then the gym's guide/leader dialog can
either just *acknowledge* it (soft) or, for #4 and #10 if you want teeth, gate the leader's challenge
button on the tag. The dragon (8) already hard-gates via `dragon_slain`.

## §7 — More Minecraft flavor to add (fresh threads)
- **Beacon = the homestead engine (promoted to the core of §1).** No longer a side capstone — the
  beacon *is* the homestead income mechanic: raise it on a liberated field, mine the pyramid to raise its
  tier, and its tier drives your CobbleDollar yield, all powered by a nether star (the Company's own
  currency backing). See §1 for the full mechanic; it ties directly to §4's nether-star plot.
- **Villager tenants.** The returning farmers (§1) can be resettled **villagers** — your homestead
  grows a real village with **villager trading**, a player-run economy standing against the Company's.
  Minecraft-native, and it makes "you rebuilt what they took" tangible.
- **Pillager "Asset Recovery" raids.** The Company's asset-recovery squads as **pillager-style raids**
  on your settled homestead — defend the field you freed. Ongoing villain pressure with vanilla raid
  mechanics. (Optional; can be intense in hardcore — make it opt-in or telegraphed.)
- **The Deep Dark Warden.** The Frontier's **Deep Dark Cave** (Cave Warden Selene) invites a real
  **sculk / Warden** encounter — Minecraft's scariest mob as endgame-frontier texture.
- **Cartography + lodestone compass.** The quest tracker hands out **filled maps** (homestead deeds,
  waypoints) and a **lodestone compass** pointing home — Minecraft navigation as UI flavor.
- **The End, post-story.** The Ryujin rift was a *tear from the End*; with the elytra in hand, the
  post-story sandbox can open the **actual End dimension** — closing the dragon-rift loop.
- **Design trap to avoid — Totems of Undying.** Tempting as a "second chance," but a totem **undercuts
  Nuzlocke permadeath.** If used at all, make it a **trophy only** (non-consumable display), never a
  usable revive.

## Suggested order
1. **§3 milestone loot** (trivial, immediate texture) — a few `give` lines.
2. **§2 advancement interleave** (the 3 currency hooks) — systemic, no world edits, high "crossover" feel.
3. **§1 homestead** (the build thread) — the biggest payoff; needs the SafeZone-on-a-field test but no map edits.
4. **§4(a) nether-star lore beat** — deepens the economy thesis with pure dialog.

## §8 — Config & defaults (ModMenu-tunable)
House rule: ship a sensible **default** for every number/toggle, and expose it in the config (loaded by
`ConfigLoader`) + the **ModMenu** screen (`InitiativeConfigScreen`), exactly like the Nuzlocke knobs. So
the showrunner can retune live without a rebuild. Proposed starting defaults (hardcore economy — passive
income is a trickle, never a faucet):

| Knob | Default | Notes |
|---|---|---|
| `homestead_income_multiplier` | `1.0` | global scale on all homestead income |
| `beacon_yield_tier` (T1/T2/T3/T4) | `25 / 50 / 100 / 175` CD/day | base per pyramid tier |
| `beacon_material_mult` (iron/gold/diamond/emerald/netherite) | `1.0 / 1.25 / 1.6 / 1.8 / 2.5` | weighted by the pyramid's block mix |
| `beacon_field_cap` | `450` CD/day | max per single homestead |
| `beacon_total_cap` | `1500` CD/day | max across all homesteads combined |
| `beacon_price_base` / `beacon_price_growth` | `2000` CD / `×1.5` | Suzune's 2nd beacon onward, escalating |
| `beacon_star_for_top_tier` | `true` | fork (c): a nether star unlocks T4 + the beam |
| `homestead_safe_zone` | `true` | liberated fields become no-death build zones |
| `gym_mc_requirements_enabled` | `true` | the per-gym Minecraft tasks (§6) |
| `gym_mc_hard_gate` (per gym) | `false` (all) | soft/acknowledge by default; flip #4/#10 to block the challenge |
| `milestone_loot_enabled` | `true` | diamond/beacon/netherite at story beats (§3) |

Same idea downstream: the phone + Mom knobs live in `PHONE_AND_CARE.md` §Config. Any "multiplier" defaults
to `1.0` so the base numbers are the single source of truth and multipliers are pure showrunner overrides.
