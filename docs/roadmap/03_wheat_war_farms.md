# 03 — Wheat War / Farm Liberation Network

> **Area key:** `wheat_war_farms`
> **Owns:** all **ten** occupied wheat-field set-pieces (the `fields_liberated` backbone — every
> FARM zone in `install.json` counts; SHOWRUNNER RULING 2026-07-06), their perimeter-guard →
> site-manager mini-beat, the `liberation/free_field` reward loop, the Granary keepers, the Wheat
> Traders' trade→recognise→turn-hostile arc, and the HQ-gate field math (**`fields_liberated >= 6`
> — a MAJORITY of the ten** — alongside gym 7). This is the plot **you feel in your wallet**: the
> alternative wheat economy made physical, and the lever the player pulls to claw the CobbleDollar
> back.
> **Does NOT own:** the `cd_instability` gym-destabilise steps (that is `mainline_spine` /
> gym leaders), the HQ interior gauntlet or DJ's fight (`company_hq`), or the per-badge
> CobbleDollars shop catalog (`scripts/shop_tiers`, owned by mainline economy). This doc owns the
> **fields, the field cast, and the two commerce-NPC arcs**, and the contract the HQ gate hangs on.

**SHOWRUNNER RULING (2026-07-06, bakes out an old §9 question):** the field network is **TEN**
fields and the HQ gate is a **majority — 6 of 10**, not all. The four zones the previous draft
treated as non-counting regional flavour (Mirebloom `farm_2`, Westwind `farm_3`, Coldfurrow
`farm_7`, Frostfallow `farm_8`) are **promoted into the counting backbone**: they get the full
perimeter-guard → site-manager beat and point at the one true `free_field`. There is **no**
`free_field_regional` variant — that alternative is dead.

The first field (**Firstfurrow / `farm_1`**) is fully built and authoritative — every heading
below treats it as the reference implementation and specifies the remaining **nine** fields
(`farm_2/3/4/5/6/7/8/9/10`) plus the two commerce arcs as **parallel copies of that proven
pattern**.

> **BLOCKER (verified — ENGINE_FINDINGS "Badge-7 era"):** only `farm_1` is wired today, so
> `fields_liberated` maxes at **1** and the HQ gate (now **6**) is **impassable**. Under the
> majority rule, **at least five more fields must be placed + wired** before Act 1→2 can be
> crossed. This remains the single biggest spine blocker, and the ruling *raises* the minimum
> wiring bar from 3 extra fields to 5.

---

## 1. Concept & fantasy

**One-line pitch:** *The villain's new money is wheat, and the wheat is behind a fence with a
guy holding a clipboard — so you kick the fence in one field at a time, and the whole region's
prices lurch back toward honest as you go.*

The fun is that the economy is a **tug-of-war you can win with your fists.** Every gym leader you
beat quietly *raises* `cd_instability` (+8, the Company tampering with the money). Every field you
liberate *lowers* it (−6) and steps the shop toward a **relief** catalog. The player watches two
numbers fight — the yellow instability rate on every payout, and the `n/10` fields line on the
HUD — and they are personally the counterweight. It is the rare Nuzlocke side-content that is
*optional field-by-field* but **mechanically load-bearing in aggregate**: you cannot meet Acting
CEO DJ until a **majority of the fields (6 of 10)** stop feeding him — and because seven fields
sit in the Act-1 window, the player gets to *choose* which six, with one field of slack.

Why it is streamable:
- **Fields are not safe zones.** FARM zones are `mobsSpawn: true, hostileOnly: true` — Nuzlocke
  is **live** while you fight the Site Manager. Liberating a field means a trainer battle with the
  permadeath rules on and creepers potentially in the wheat. That is the tension the audience shows
  up for. (One verified exception: **Frostfallow `farm_8` ships `mobsSpawn: false`** in
  `install.json` — see §9, flagged for showrunner/builder.)
- **The commerce curdle.** The Wheat Trader and Granary Keeper *sell to you* for the first few
  fields — friendly, chatty, corporate-warm. Then, because **you** emptied their fields, they start
  eyeing you. Then they place the face and try to file you mid-transaction. The recognition arc is
  **earned by the player's own actions**, not a timer — the more of the monopoly you dismantle, the
  more the monopoly's shopkeepers want you dead.
- **The majority before HQ.** DJ literally refuses to fight ("Come back when the fields stop
  answering our memos"). The audience learns the rule from the villain, then the player goes and
  *does the thing the boss dared them to*, and the boss door opens. Clean, legible, satisfying —
  and "six of ten" reads on stream as *breaking the monopoly's majority stake*.

Marquee moments: the **first fence kicked in** at Firstfurrow (`◆ Field liberated`, wheat-gold
title, instability visibly drops); the **Wheat Trader placing your face** and swinging from sales
pitch to "you are supposed to be filed"; and the **sixth flag flipping to Liberated** the same run
the HQ marker goes red.

---

## 2. Narrative role

| Axis | Value |
|------|-------|
| **Act** | Primarily **Act 1** — **seven** fields threaded through gyms 1→7 (`farm_1/5/2/3/4/6/7`, the HQ-gate feeder pool; any 6 of the 7 opens the gate). Three **Act 2** bonus fields (`farm_8/9/10`, post-HQ east/southeast) that keep the currency clawing down after DJ. |
| **`cd_instability`** | Each field = **−6** (`free_field_apply`, floored at 0). Ten fields = −60 of counter-pressure against the +8/gym destabilise. Six pre-HQ liberations = −36 against the gym-7 peak (idx 56) → **idx 20 at the HQ door, already below DJ's `hq_stabilize` clamp (25)** — see §9 open question on re-tuning the per-field magnitude so the "CURRENCY STABILIZED" beat still lands. |
| **Relief tiers** | **PROPOSED re-map:** the relief ladder steps every **3** fields (`<tier>_relief1` at 3, `<tier>_relief2` at 6; each ≈ −12 instability in shop-price terms, floor 0). Verified current wiring is every-2/cap-2 (`ShopTierManager.RELIEF_FIELDS_PER_LEVEL = 2`, `RELIEF_MAX_LEVEL = 2`; `scripts/shop_tiers/master_shop.json` `relief:{step:12, levels:2, fieldsPerLevel:2}`) — both the Java constant and the master must change together (§8). CD prices ease; Granary wheat rates simultaneously **worsen** — the wheat currency losing its manufactured edge. `relief2` landing exactly at 6 ties the cheapest pre-HQ shop to the gate crossing. |
| **Memory fragment** | **No dedicated fragment** (those are gym-gated, `frag_1..10`). The field arc is the **connective tissue that makes `frag_7` — "You signed this charter." — land**: the "Transition Orders" that evicted every farm bear the founder's signature. The Firstfurrow Transition Order memo (already built, filed at Lucian) is the physical seed of that reveal. |
| **Recognition — grunts (field cast)** | Badge-based (engine: `early` = unconditional, `mid` = `badges_gte_3`, `late` = `badges_gte_7`). Firstfurrow/Crossroads/Mirebloom cast = early(→mid); Westwind/Dryrow = mid; Fenceline/Coldfurrow = mid→late; Frostfallow/Highfield/Ashloam = late. Same verbatim recognition block as `villain_yield_officer` (§4). |
| **Recognition — commerce (traders/keepers)** | **Fields-based, not badges** — `fields_liberated`: **PROPOSED re-map** `>=3` → suspicious (was 2), `>=5` → hostile (was 4), scaled for the ten-field backbone. This is the key thematic split: the shopkeepers recognise you because **you personally emptied their fields**, not because time passed. Their hostility is your own doing. |
| **Canon ties** | The wheat-backed-currency monopoly (LORE_BIBLE §3); "the family was transitioned to alternative outcomes" corporate-dread register; the scrubbed-founder recognition gradient (§4 of LORE_BIBLE); the HQ hard gate (LORE_BIBLE §4 Act 2 — field count now 6-of-10 per ruling). **Rule kept:** field cast never says "wheat/grain/yield" as a *crop* — it is "the parcel, the asset, performance, the rows." Civilians (displaced tenants) **never** recognise the player. |

---

## 3. Layout & placements

All **ten** FARM zones are **confirmed in `install.json`** (I read them; no append needed). Each
already carries its banner wiring: `activeWhenObjective: "field_freed"`, `activeWhenHolder:
"farm_N"`, `activeWhenMin: 1` — so the zone subtitle flips from **"Corporate owned."** to
Liberated the moment `free_field` latches that holder. **No terrain work is proposed** — every
placement is an NPC body dropped inside an existing polygon.

### The ten-field backbone (holders, zones, regions)

| Holder | Zone (install.json) | Approx. centre | Region / nearest gym | Act | Manager ace lv |
|--------|--------------------|---------------:|----------------------|:---:|:--:|
| `farm_1` | **Firstfurrow Farm** *(BUILT)* | (1580, 2477) | Takehara Falls (G1) | 1 | 21 |
| `farm_5` | **Crossroads Granary** | (2309, 3540) | Central artery, Sango↔Kalahar | 1 | 28 |
| `farm_2` | **Mirebloom Paddies** | (1229, 2820) | Mystic Marsh (G3) doorstep | 1 | 29 (PROPOSED) |
| `farm_3` | **Westwind Fields** | (657, 3279) | Gaviota Port (G5) approach | 1 | 43 (PROPOSED) |
| `farm_4` | **Dryrow Steading** | (1535, 3872) | Kalahar Reach (G6) doorstep | 1 | 54 |
| `farm_6` | **Fenceline Acres** | (1570, 1737) | Cyber City (G7) approach | 1 | 60 |
| `farm_7` | **Coldfurrow Farm** | (1925, 963) | HQ / Ryujin road, east of Cyber | 1 | 61 (PROPOSED) |
| `farm_8` | **Frostfallow Farm** | (3066, 2478) | East, Nifl / Royal-League side | 2 | 67 (PROPOSED) |
| `farm_9` | **Highfield Estate** | (3300, 3354) | East, Royal-League-adjacent | 2 | 72 |
| `farm_10` | **Ashloam Fields** | (3293, 4006) | Scorchspire / Fire Shrine SE | 2 | 84 |

Centres for `farm_2/3/7/8` are **zone-polygon centroids computed from `install.json` vertices**
(grounded); ace levels for the four promoted fields are **PROPOSED** per the brutal-Nuzlocke rule
(fought roughly at the *previous* gym's ceiling — see §6).

The **seven Act-1 feeders** (`farm_1/5/2/3/4/6/7`, aces 21→61) are all beatable at or below the
gym-7 cap (62), so a player on the natural route reaches **`fields_liberated >= 6`** by the time
they have seven badges — the HQ gate opens organically, **with one field of slack** (skip any one
Act-1 field and the majority still lands). `farm_8/9/10` are **Act-2 bonus** fields for players
who want the deepest relief and to floor the instability index.

### NPC / prop table

`(C)` = confirmed body already in a file; `(P)` = **PROPOSED — needs builder confirm.** All new
field trainers follow the Firstfurrow convention: `placement:{x,y,z}` + `skin:{type:custom,uuid}`
and **no `uuid`** → they self-spawn once via the generated proximity function (bypasses the builder
world). Perimeter guards additionally get a `sight` block (fence hail). Y values marked `~` need
ground-match confirmation (all four promoted zones declare `centerY: 64` in `install.json`).

| Field | Role | Character id | Coord | Src |
|-------|------|--------------|-------|:---:|
| farm_1 | perimeter | `villain_yield_officer` | (1586, 90, 2487) | C |
| farm_1 | site manager | `villain_site_manager` | (1603, 89, 2488) | C |
| farm_5 | perimeter | `villain_yield_officer_5` | (2262, ~66, 3500) | P |
| farm_5 | site manager | `villain_site_manager_5` | (2309, ~66, 3540) | P |
| farm_2 | perimeter | `villain_yield_officer_2` | (1205, ~64, 2790) | P |
| farm_2 | site manager | `villain_site_manager_2` | (1229, ~64, 2820) | P |
| farm_3 | perimeter | `villain_yield_officer_3` | (640, ~64, 3310) | P |
| farm_3 | site manager | `villain_site_manager_3` | (657, ~64, 3279) | P |
| farm_4 | perimeter | `villain_yield_officer_4` | (1548, ~78, 3822) | P |
| farm_4 | site manager | `villain_site_manager_4` | (1535, ~78, 3872) | P |
| farm_6 | perimeter | `villain_yield_officer_6` | (1520, ~72, 1762) | P |
| farm_6 | site manager | `villain_site_manager_6` | (1570, ~72, 1735) | P |
| farm_7 | perimeter | `villain_yield_officer_7` | (1870, ~64, 985) | P |
| farm_7 | site manager | `villain_site_manager_7` | (1925, ~64, 963) | P |
| farm_8 | perimeter | `villain_yield_officer_8` | (3045, ~64, 2500) | P |
| farm_8 | site manager | `villain_site_manager_8` | (3066, ~64, 2478) | P |
| farm_9 | perimeter | `villain_yield_officer_9` | (3245, ~66, 3360) | P |
| farm_9 | site manager | `villain_site_manager_9` | (3300, ~66, 3354) | P |
| farm_10 | perimeter | `villain_yield_officer_10` | (3245, ~66, 4020) | P |
| farm_10 | site manager | `villain_site_manager_10` | (3293, ~66, 4005) | P |
| — | Granary Keeper (wheat country) | `granary_keeper` | Hua Zhan City | C (builder uuid) |
| — | Granary Keeper (the silo) | `granary_keeper_crossroads` | (2320, ~66, 3550) inside farm_5 | P |
| — | Granary Keeper (east) | `granary_keeper_east` | Royal-League approach, near farm_9 | P |
| — | Wheat Trader (early) | `wheat_trader_1` | Hua Zhan City | C (builder uuid) |
| — | Wheat Trader (mid) | `wheat_trader_2` | Kalahar Reach | C (builder uuid) |
| — | Wheat Trader (late) | `wheat_trader_3` | East / Highfield approach | P |

Promoted-field guard coords are **PROPOSED**: manager at the zone centroid, guard offset toward
the town-facing edge (inside the polygon per the `install.json` bounding boxes). Builder confirms
exact tiles + Y.

**Note on Granary placement:** "Crossroads Granary" is *named for a silo* — dropping
`granary_keeper_crossroads` inside `farm_5` doubles the field's flavour (you liberate the parcel the
company store sits on). The keeper trades until the hostile tier (**PROPOSED `>=5` fields**), then
he is a hostile who *still trades* and then files you (§5). Builder should place all three keepers
on **flat interior tiles**; they are trade/dialog NPCs with no sight hail.

---

## 4. Core structure — the field mini-beat (this is not a gym)

Every field is the **same two-step loop**, proven at Firstfurrow. It is deliberately *not* a PvP
ladder — it is **clear-the-gate, then flip-the-field**, so it reads instantly on stream and reuses
one wiring pattern ten times.

```
Perimeter Guard (sight hail at the fence gap) ── beat ──▶ despawns, clears the gate
        │                                                        │
        │ (until then, Manager refuses:                          ▼
        │  "Clear my perimeter first, resident.")        Site Manager (mid-field, by the barn)
        │                                                        │  battle gated on
        └───────────────────────── not_tag defeated_<guard> ─────┘  defeated_<guard>
                                                                 │ beat ──▶ on_win:
                                                                 │   free_field {field:farm_N}
                                                                 │   -6 cd_instability
                                                                 │   +1 fields_liberated
                                                                 │   shop refresh (relief)
                                                                 ▼   zone flips → "Liberated"
```

### Gate wiring (per field, copied from Firstfurrow)

- **Perimeter guard** hails via `sight:{mode:dialog, range:10, dialog_label:fence_hail}` (needs
  `npcsight add <uuid> 10 fence_hail` + `npcsight mode <uuid> dialog` after import). Battle is
  **opt-in — walking away is free, no decline fee** (fields do not charge; fees live on the Sango
  approach). `despawn_on_win: true` — the gate is physically gone once beaten.
- **Site Manager** dialog carries all four recognition entries (`late` prio 30 → `mid` 20 →
  `quota`/`fence` 15 → `default` 10). **Every fight button is gated `defeated: villain_yield_officer_N`**;
  a separate `blocked_button` gated `not_tag: defeated_villain_yield_officer_N` prints the
  "clear my perimeter first" refusal and closes. This is the whole gate — one defeat tag.
- **Liberation fires in `on_win`** (unquoted SNBT — no double quotes in onwin commands):
  ```
  execute as @1 run function cobblemon_initiative:liberation/free_field {field:farm_5}
  ```
  ENGINE FINDING (must respect): `onwin` tokens are **winners-first** — key `1` = player won, key
  `2` = player lost. Put the liberation + tag under `1`.

### Which battle is the "double"?

Fields are **`GEN_9_SINGLES`** by default (the guard/manager are lone corporate functionaries — a
single clipboard, a single fence). To echo the gym-interior variety without inventing a ladder,
**exactly one field is a doubles set-piece**: **Fenceline Acres (`farm_6`)** runs its Site Manager
as **`GEN_9_DOUBLES`** — a "Regional Operations" pair (Manager + a co-signed Auditor) so the
toughest Cyber-approach field has the meatiest fight. This mirrors the `agent_yield_lead`
GEN_9_DOUBLES tag-team pattern already used in Gym 1's interior. All other fields — including the
promoted four — stay singles; in particular **Coldfurrow (`farm_7`) stays singles** so the HQ
doorstep does not stack a set-piece fight directly in front of the interior gauntlet.

### Recognition escalation across the ten fields

Because grunt recognition is **badge-gated** (`mid = badges_gte_3`, `late = badges_gte_7`), the
managers naturally escalate as the player advances — no per-field tuning needed, the shared verbatim
block does it:

| Field | Player badge window | Dominant recognition line |
|-------|--------------------|---------------------------|
| farm_1 Firstfurrow | 1–2 | early ("Do I know you from somewhere?") |
| farm_5 Crossroads | 1–3 | early → first `mid` flickers |
| farm_2 Mirebloom | 2–3 | early → `mid` flickers |
| farm_3 Westwind | 4–5 | `mid` |
| farm_4 Dryrow | 5–6 | `mid` ("You are supposed to be a closed file.") |
| farm_6 Fenceline | 6–7 | `mid` → `late` ("It is you. The founder.") |
| farm_7 Coldfurrow | 7 | `late` — spoken on HQ's own doorstep |
| farm_8 Frostfallow | 8–9 | `late` |
| farm_9 Highfield | 7–9 | `late` (some stand down, then fight anyway) |
| farm_10 Ashloam | 9–10 | `late`, the most rattled — closest to the mirror |

---

## 5. Quests & side quests

### 5a. The Wheat Trader arc (recognition → ambush) — the interactive cover-up

- **Givers:** `wheat_trader_1` (Hua Zhan, early), `wheat_trader_2` (Kalahar, mid), **`wheat_trader_3`
  (PROPOSED, east/act-2)**. All share `dialog:wheat_trader` + `trade_wheat_trader` snippet + the
  `wheat_trader_ambush` battle (team already in `rctmod/trainers/wheat_trader_ambush.json`, lv ~38).
- **Hook:** "Back your savings in something you can hold. Something you can eat." The wheat pitch —
  the audience hears the villain's economic thesis from a friendly face.
- **Steps / gates (fields-based, engine-wired; thresholds PROPOSED re-map for /10 — current
  wiring is 2/4 in `wheat_trader/tick.mcfunction`, §8):**
  - `fields_liberated 0–2` → **default** pitch, trade freely (tag `heard_wheat_pitch` set, which lights
    the HUD `n/10` line).
  - `fields_liberated >= 3` (`wheat_trader_suspicious`) → **wary** — "a number that will not add," still
    trades.
  - `fields_liberated >= 5` (`wheat_trader_hostile`) → **hostile** — places the face, **direct
    "Stand and fight"** battle (`do: battle`). This is the confrontational variant, and it now fires
    *one field before* the HQ gate — the monopoly lashes out just as its majority slips.
- **Rewards / resolution:** winning the ambush → prize 400 CD, `defeated_wheat_trader_ambush`. Beaten
  traders **do not despawn** and refuse re-trade ("Word went out about you.").

### 5b. The Granary Keeper arc (trade → file-you) — the sneakier variant

- **Givers:** `granary_keeper` (Hua Zhan, BUILT), **`granary_keeper_crossroads`**, **`granary_keeper_east`**
  (both PROPOSED). All share `dialog:granary_keeper` + the badge-tiered trade presets
  (`granary_keeper_badge_N` + `_relief1/2` + `_post_hq`, already generated).
- **Hook:** "Company operated, community minded. We do not take CobbleDollars here — grain in, goods
  out." The wheat-backed *retail* store: the alternative economy you can actually shop at.
- **The twist (distinct from the trader):** at hostile tier (**PROPOSED `fields_liberated >= 5`**;
  currently `gte 4` in `dialog:granary_keeper` — must move in lockstep with the trader poller, §8)
  the keeper **still trades** — "greed beats caution" — but the trade button arms
  `granary_ambush_armed=1`. The **`granary/tick` poller** (already built) counts ~15s, prints the
  menace beat ("Asset located. Initiating retrieval."), then fires **`granary_ambush`** (team in
  `villain_team.json`, ~600 CD onwin). One-shot on `defeated_granary_ambush`. This gives two
  different curdle flavours: the trader *fights*, the keeper *files you*.
- **Resolution:** the granary is a **wheat sink** the plot wants you to reject — trading there props
  up the currency you are trying to kill. Beating the ambush is the game telling you to stop shopping
  at the monopoly.

### 5c. Firstfurrow's emotional payload (BUILT — referenced, not re-authored)

`farm_1` already carries the **human** cost, gated on the `farm_1_free` bridge tag: **Tenants of
Record** (the displaced Deng family walks home on camera — `sidequest/tenants_of_record/homecoming`),
**First Night Watch** (light the gate lantern at dusk — `sidequest/night_watch`), and the **Transition
Order** memo → Lucian archivist (`transition_filed`). These are the *template* for the field's human
beat; the later fields deliberately stay lighter (one recurring capstone below) to avoid ten Deng
families.

### 5d. NEW capstone — "The Granary Ledger" (PROPOSED)

- **Giver:** a defected granary clerk (PROPOSED, at Crossroads `farm_5`, civilian — never recognises
  the player). **Gate:** `fields_liberated >= 6` (PROPOSED re-map — parity with the HQ gate: the
  ledger surfaces the moment the monopoly loses its majority) → offers the **doctored ledger**
  proving the Company engineered the wheat monopoly (the double-signed pages LORE_BIBLE §3 hinges on).
- **Steps:** take ledger (tag `granary_ledger_taken`) → carry it to **Lucian** in Sango (reuse the
  `transition_filed` archivist pattern) → he files it, pays a **records fee reward** and mutters a
  line that *circles* `frag_7` without closing it ("These transition orders. The signature underneath
  is the same on every one. You would know it anywhere, I think.").
- **Reward:** ~2500 CD (skewed via `economy/payout`), a `heal_ball`/`full_restore` hamper, story tag
  `wheat_racket_exposed` (a flavour flag the HQ area may read as a telegraph).
- **Resolution:** the paperwork proof the audience needs to *understand* what DJ is protecting, handed
  to the one archivist who has been quietly building the case since Firstfurrow.

### 5e. NEW capstone — "Homecoming Convoy" (PROPOSED, all ten fields)

- **Trigger:** `fields_liberated == 10` (all managers down — the completionist clear, well past the
  6-of-10 gate). A one-time celebratory beat at the Crossroads hub — the displaced families' carts
  roll back onto the freed parcels (NPC placement + particles only, **no build**).
- **Reward:** a large one-shot CD payout (~5000, skewed), a unique cosmetic held item
  (`npc_gift/harvest_wreath` PROPOSED), and the `wheat_war_cleared` tag. Optionally trips a deepest
  **relief3** shop tier (§7, showrunner call).
- **Resolution:** the wheat racket is *physically* over. The region's farms answer to no one's memos.
  The currency has been clawed as far back as the player can push it.

---

## 6. Trainers & teams needed

All levels are **brutal-Nuzlocke on-or-under the local cap** (fields are optional obstacles, fought
roughly at the *previous* gym's ceiling). `RCTAPI` team files live in `data/rctmod/trainers/`;
registry entries (id, category `villain_team`, prize, prerequisites) live in
`trainers/villain_team/villain_team.json` alongside the existing `villain_site_manager` /
`villain_yield_officer` entries. Teams for the four promoted fields (`farm_2/3/7/8`) are
**PROPOSED — needs showrunner balance confirm** alongside the rest.

### rctmod team files to CREATE (`src/main/resources/data/rctmod/trainers/<id>.json`)

| Trainer id | Field | Format | Team sketch (species / lv) |
|------------|-------|:------:|----------------------------|
| `villain_yield_officer_5` | farm_5 | SINGLES | Poochyena 26 · Nuzleaf 27 |
| `villain_site_manager_5` | farm_5 | SINGLES | Linoone 27 · Watchog 27 · **Mightyena 28 (ace)** |
| `villain_yield_officer_2` | farm_2 | SINGLES | Purrloin 27 · Poochyena 28 |
| `villain_site_manager_2` | farm_2 | SINGLES | Liepard 28 · Watchog 28 · **Mightyena 29 (ace)** |
| `villain_yield_officer_3` | farm_3 | SINGLES | Mightyena 41 · Gumshoos 42 |
| `villain_site_manager_3` | farm_3 | SINGLES | Liepard 42 · Diggersby 42 · **Mightyena 43 (ace)** |
| `villain_yield_officer_4` | farm_4 | SINGLES | Mightyena 52 · Diggersby 53 |
| `villain_site_manager_4` | farm_4 | SINGLES | Krookodile 53 · Gumshoos 53 · **Mightyena 54 (ace)** |
| `villain_yield_officer_6` | farm_6 | SINGLES | Liepard 58 · Bouffalant 59 |
| `villain_site_manager_6` | farm_6 | **DOUBLES** | Krookodile 59 · Bisharp 59 · Watchog 59 · **Obstagoon 60 (ace)** |
| `villain_yield_officer_7` | farm_7 | SINGLES | Bisharp 59 · Liepard 60 |
| `villain_site_manager_7` | farm_7 | SINGLES | Krookodile 60 · Bouffalant 60 · **Obstagoon 61 (ace)** |
| `villain_yield_officer_8` | farm_8 | SINGLES | Obstagoon 65 · Bouffalant 66 |
| `villain_site_manager_8` | farm_8 | SINGLES | Krookodile 66 · Bisharp 66 · **Kingambit 67 (ace)** |
| `villain_yield_officer_9` | farm_9 | SINGLES | Bisharp 70 · Bouffalant 71 |
| `villain_site_manager_9` | farm_9 | SINGLES | Krookodile 71 · Grafaiai 71 · **Kingambit 72 (ace)** |
| `villain_yield_officer_10` | farm_10 | SINGLES | Kingambit 82 · Obstagoon 83 |
| `villain_site_manager_10` | farm_10 | SINGLES | Grimmsnarl 83 · Krookodile 83 · **Kingambit 84 (ace)** |

Species theme: **Dark / "muscle" corporate-enforcer mons** (Poochyena→Mightyena, Watchog line,
Krookodile, Bisharp→Kingambit, Obstagoon) — the same "assets that intimidate" register as the built
`villain_site_manager` (Mightyena/Watchog). Keep IVs modest (10–20) and `maxSelectMargin` ~0.4–0.5:
these are grunts, not gym leaders. Copy `rctmod/trainers/villain_site_manager.json` verbatim and
swap species/levels.

### `villain_team.json` registry entries to CREATE

One entry per trainer above (**18 total**): `category: "villain_team"`, `prerequisites: []` (the
*dialog* gate handles ordering, not the registry), `prize` per §7, `battleFormat` matching the
table. Follow the existing `villain_site_manager` / `villain_yield_officer` entries as the template.

### wheat_trader_3 battle

Reuses the existing `wheat_trader_ambush` trainer/team — **no new team file** needed, only the new
character file (§8).

---

## 7. Economy & rewards

### Per-field payouts (flat literal via `onwin` — NOT skewed; only mod-routed payouts skew)

Rows for the four promoted fields are **PROPOSED** interpolations on the existing curve.

| Field | Guard prize | Manager prize | Loss fee (guard) |
|-------|:-----------:|:-------------:|:----------------:|
| farm_1 *(built)* | 280 | 520 | 100 |
| farm_5 | 350 | 650 | 120 |
| farm_2 | 380 | 700 | 130 |
| farm_3 | 520 | 950 | 175 |
| farm_4 | 600 | 1100 | 200 |
| farm_6 | 750 | 1400 (doubles) | 250 |
| farm_7 | 800 | 1500 | 275 |
| farm_8 | 900 | 1700 | 300 |
| farm_9 | 1000 | 1900 | 350 |
| farm_10 | 1300 | 2400 | 450 |

Battle prizes stay **flat literals** in `onwin` (ENGINE finding — only `economy/payout` applies the
instability haircut). The capstone quests (5d/5e) **do** route through `economy/payout {amount:N}` so
their yellow rate line reads on camera.

### Instability + relief loop (the tug-of-war)

- Each `free_field_apply`: **−6 `cd_instability`** (floor 0), then `cobblemon-initiative shop refresh`.
  The −6 magnitude is explicitly marked TUNABLE in `free_field_apply.mcfunction` ("revisit alongside
  the field count") — **the field count just changed**; see §9 for the re-tune question (six pre-HQ
  liberations now under-run DJ's 25-clamp).
- **Relief steps (PROPOSED re-map for /10 — every 3 fields):** `relief1` at **3** fields, `relief2`
  at **6** fields. Verified current wiring steps every **2** with cap **2**
  (`ShopTierManager.RELIEF_FIELDS_PER_LEVEL = 2` / `RELIEF_MAX_LEVEL = 2` in
  `src/main/java/.../economy/ShopTierManager.java`, mirrored by `relief:{fieldsPerLevel:2, levels:2,
  step:12}` in `scripts/shop_tiers/master_shop.json`) — the Java constants and the master JSON must
  change **together** (§8), and the Java side is a mod rebuild (candidate for a ModMenu tunable per
  project preference). CobbleDollars prices ease (~−12 idx-equivalent per relief level); the
  **Granary wheat bell curve** simultaneously worsens (wheat buys most at the gym-7 instability
  peak, less as you claw it back — `scripts/generate_granary_tiers`). Freeing fields makes the
  Company's wheat racket *less* attractive: the plot in the price tags.
- **The gate crossing and `relief2` coincide at 6 fields** — the run to DJ happens at the cheapest
  pre-HQ catalog; DJ's `hq_stabilize` then clamps idx down to 25 (downward-only).
- **PROPOSED `relief3` at 9 fields:** third relief level for the Act-2 completionist push
  (`relief:{levels:3}` + `RELIEF_MAX_LEVEL = 3` + regen — small `scripts/shop_tiers` +
  `scripts/generate_shop_tiers` + Java change; showrunner decision, §9). On the every-3 ladder it
  lands at 9, one field before the Homecoming Convoy at 10.

### Sinks

- **The Granary is a wheat sink**, not a CD sink — the point is that shopping there props up the enemy
  currency. It is designed to be *tempting then rejected*.
- Fields charge **no decline fee** (opt-in battles). CD sinks stay in the Pokemart / shop tiers
  (out of area).

---

## 8. Implementation notes / FUTURE-ME HOOKS

**Golden rule:** copy Firstfurrow. `villain_yield_officer.json` + `villain_site_manager.json` are the
two files to duplicate for each of the nine remaining fields. Do not invent new plumbing —
`liberation/free_field`, `wheat_trader/tick`, `granary/tick`, `quest/set_wheat`, and the zone banners
already exist and are authoritative. **There is no `free_field_regional` and none should be built**
(ruling): every field points at the one `free_field`.

### The denominator migration (NEW — the ruling's mechanical fallout)

The old `/6` + `>=4` numbers are baked into specific files. They must all move **in one pass** (a
half-migrated state silently breaks the gate or the HUD):

| # | File (verified) | Today | Becomes | Owner |
|---|---|---|---|---|
| 1 | `function/quest/set_wheat.mcfunction` — hard-coded `$(fields)/6` literal | `/6` | `/10` | mainline_spine (HUD) |
| 2 | `function/quest/render.mcfunction` lines 37–38 — HQ branch `fields_liberated matches 4..` (and the `unless … 4..` twin) | `4..` | `6..` | mainline_spine (HUD) |
| 3 | `dialog-src/characters/villain/acting_ceo_dj.json` — `default` entry gate `fields_liberated {op:gte, value:4}` | 4 | 6 | company_hq |
| 4 | `function/wheat_trader/tick.mcfunction` lines 11/13 — suspicious `2..` / hostile `4..` | 2 / 4 | 3 / 5 (PROPOSED) | wheat_war_farms |
| 5 | `dialog-src/dialog/granary_keeper.json` — suspicious gate `gte 2` / hostile gate `gte 4` | 2 / 4 | 3 / 5 (PROPOSED) | wheat_war_farms |
| 6 | `ShopTierManager.java` `RELIEF_FIELDS_PER_LEVEL` **and** `scripts/shop_tiers/master_shop.json` `relief.fieldsPerLevel` (+ regen `generate_shop_tiers`, `generate_granary_tiers`) | 2 | 3 (PROPOSED) | mod + shop_tiers (mainline economy) |

Band-tag note (verified, `scripts/content_compile`): numeric `fields_liberated` dialog gates
compile to threshold PLAYER_TAGs maintained by the auto-generated
`function/dialog/band_tags.mcfunction` (today: `gte_1/2/4`). Changing DJ's gate to `gte 6` makes
the compiler emit **`fields_liberated_gte_6`** (plus its `no_` inverse) automatically on the next
`content_compile` — no hand-wiring, but rows 3–5 **require the recompile** or the new tags never
tick.

### Files to CREATE (authoring source — these compile down)

For each field `N ∈ {2, 3, 5, 4, 6, 7, 8, 9, 10}`:
- `dialog-src/characters/villain/villain_yield_officer_N.json` — copy `villain_yield_officer.json`;
  change `id`, `trainer`, `placement`, `battle.trainer`/`defeat_tag`/prizes, keep the verbatim
  recognition entries and the `sight:{mode:dialog,range:10,dialog_label:fence_hail}` block.
- `dialog-src/characters/villain/villain_site_manager_N.json` — copy `villain_site_manager.json`;
  change ids/placement/prizes, set every fight button's `gate.defeated` and the `blocked_button`'s
  `gate.not_tag` to `villain_yield_officer_N` / `defeated_villain_yield_officer_N`, and set
  `on_win[0]` to `execute as @1 run function cobblemon_initiative:liberation/free_field {field:farm_N}`
  (unquoted). For `farm_6`, set `battle.format: "GEN_9_DOUBLES"` and give the team 4 mons.
- `src/main/resources/data/rctmod/trainers/villain_yield_officer_N.json` + `villain_site_manager_N.json`
  — teams per §6.

Commerce NPCs:
- `dialog-src/characters/villain/wheat_trader_3.json` — copy `wheat_trader_2.json`; new `location`,
  `act: "2"`, `recognition_tier: "mid"`, reuse `dialog:wheat_trader` + `trade_wheat_trader` + the
  `wheat_trader_ambush` battle. If **builder-placed**, give it a `uuid`; if **self-spawn**, give it
  `placement` + `skin`.
- `dialog-src/characters/villain/granary_keeper_crossroads.json` + `granary_keeper_east.json` — copy
  `granary_keeper.json`. **Critical:** each must be added to **`npc_presets.json`** with a preset that
  starts `humanoid/granary_keeper` so `generate_granary_tiers` emits import lines into every
  `granary/apply_<tier>.mcfunction` — otherwise the new keepers never re-tier.
- Capstone quests (5d/5e): new civilian character(s) + `function/sidequest/granary_ledger/*` and
  `function/sidequest/homecoming_convoy/*` (copy the `tenants_of_record` tick/gate-bridge shape), loot
  `loot_table/npc_gift/harvest_wreath.json`.

### `install.json` — CONFIRMED, minimal touch

All **ten** FARM zones already exist with correct `activeWhenHolder` (`farm_1..10`) and the
`field_freed` banner wiring — **the promotion of `farm_2/3/7/8` needs zero install.json changes.**
Only append if adding the capstone reward flags needs a new banner (it does not). Do **not**
renumber holders — `farm_1..10` are canon and referenced by `free_field`, the HUD, and the banners.
One flag: `farm_8` (Frostfallow) has `mobsSpawn: false` unlike every other FARM zone (§9).

### Pipeline (run in this exact order after authoring)

```
scripts/content_compile          # lowers dialog-src → easy_npc presets + preset.index (+ band_tags regen: gte_6)
scripts/generate_shop_tiers      # if the relief re-map (fieldsPerLevel 3) is greenlit
scripts/generate_granary_tiers   # rebuilds granary_keeper_<tier> presets + apply fns (for new keepers)
scripts/update_preset_index      # rebuild Easy NPC preset index
scripts/generate_npc_function    # npc/preset_map.json + function/update_npc_presets.mcfunction
gradle build                     # required anyway if ShopTierManager relief constants change
```

Then, per field, **after in-world import**, wire the perimeter guard's sight:
`npcsight add <uuid> 10 fence_hail` + `npcsight mode <uuid> dialog` (self-spawn NPCs get their UUID at
first spawn — capture it, or pre-assign a custom `skin.uuid` and read it back).

### Gotchas (do not relearn these the hard way)

- **`onwin` is winners-first** — `{1:[...player won...], 2:[...player lost...]}`. Liberation goes in `1`.
- **`free_field` takes UNQUOTED SNBT** in an onwin/command context: `{field:farm_5}`, no double quotes.
  (The standalone runbook example `{field:"hua_zhan_1"}` is quoted because it is typed at the console;
  inside `on_win` the compiler forbids double quotes.)
- **Macro-delivered text has no escaping** — any line routed through `economy/payout`, memory
  fragments, or `onwin` must contain **no double-quotes and avoid apostrophes**. Field cast *dialog*
  (compiled to NBT, not macro) is fine, but keep the apostrophe discipline anyway.
- **`fields_liberated` is the shared, single-player counter** — `@a` == the player and tags re-assert
  on relog. The `/10` denominator is **hard-coded** in `quest/set_wheat.mcfunction` (currently still
  `/6` — migration row 1); the denominator, the render-branch `6..`, DJ's `gte 6`, and the
  trader/keeper/relief thresholds must all move **together** (§8 migration table).
- **Fields are live-Nuzlocke zones** (`mobsSpawn:true` — except `farm_8`, flagged §9). Do not add
  safe-zone suppression — the danger is the feature. Do confirm the Site Manager body sits where a
  creeper cannot trivially grief the fight before the player arrives.
- **Idempotency is already handled** — `free_field` latches `field_freed[farm_N]=1`; re-liberating is a
  no-op. Do not add your own guard.

---

## 9. Dependencies & open questions

### Depends on (other area keys — the contracts I consume)

- **`mainline_spine`** — owns `cd_instability` (+8/gym destabilise), the `frag_1..10` drip, the
  wheat HUD render (`quest/render` + `quest/set_wheat` — the `/10` denominator and the `6..` branch
  literals, migration rows 1–2), and the HQ-gate telegraph. I feed its counter and its gate; it owns
  the numbers' authoring. **My −6/field and the `>=6` gate must stay consistent with its ladder.**
- **`company_hq`** — owns Acting CEO DJ, `hq_stabilize` (idx→25), the HQ interior, and DJ's dialog
  file (the `gte` gate literal, migration row 3). My contract to it: **`fields_liberated >= 6` is
  guaranteed reachable by the gym-7 window** (seven Act-1 feeders, aces ≤61 — one field of slack).
  DJ's `monopoly_holds` refusal branch (already built) is the gate's front door.
- **`scripts/shop_tiers` / economy (mainline)** — owns the CobbleDollars catalog and the relief
  tiers ShopTierManager resolves from `fields_liberated`. The every-3 re-map and any `relief3` (§7)
  are changes *there* plus the `ShopTierManager` constants (mod rebuild).
- **`gym_system_pvp_doubles`** — the Fenceline `GEN_9_DOUBLES` field fight should match the doubles
  conventions (item clauses, `maxSelectMargin`) that area sets.
- **`mystic_marsh` / `gaviota_port` / `kalahar_reach` / `cyber_city` / `nifl_town` / `royal_league`** —
  the fields sit on their doorsteps (now including Mirebloom→Marsh, Westwind→Gaviota,
  Coldfurrow→Cyber/Ryujin road, Frostfallow→Nifl side); placement Y and exact interior tiles need
  each region builder's confirm (all promoted-field coords `PROPOSED`).

### Open questions / showrunner decisions

1. **Per-field instability magnitude (−6) under the ten-field rule.** With six pre-HQ liberations
   the index reads 56 − 36 = **20 at the HQ door — already under DJ's downward-only 25 clamp**, so
   the "CURRENCY STABILIZED" beat risks being a visual no-op. The magnitude is marked TUNABLE in
   `free_field_apply.mcfunction` precisely for a field-count change. **PROPOSED: −5/field**
   (6 fields → −30 → idx 26 at the door; DJ's clamp still visibly drops it; all ten → −50 of total
   counter-pressure). Showrunner/mainline call.
2. **Relief threshold re-map (every 3, PROPOSED §7).** Confirm `fieldsPerLevel: 3`
   (relief1@3 / relief2@6) + the Java constant change, and whether to ship **`relief3` at 9** for
   the Act-2 completionist push. (Java constants are jar-baked today — consider the ModMenu-tunable
   route per project preference.)
3. **`farm_8` (Frostfallow) ships `mobsSpawn: false`** in `install.json` — the only FARM zone that
   is not a live-Nuzlocke field. Bug or intent? If the "fields are dangerous" rule is canon for all
   ten counting fields, the zone flag should flip to `true` (install.json change, builder/showrunner
   confirm); if intent (a becalmed frost field), the doc's danger rule gets one written exception.
4. **Promoted-field tuning.** Aces 29/43/61/67 and the §6 team sketches / §7 payouts for
   `farm_2/3/7/8` are PROPOSED interpolations on the existing curve — balance confirm needed,
   especially Coldfurrow at 61 (one over Fenceline, right at the cap-62 window).
5. **Wheat Trader `wheat_ambush_armed` latch.** The trader's hostile tier is currently a **direct**
   "Stand and fight" battle (already in `dialog:wheat_trader`), while `wheat_ambush_armed` (declared in
   `wheat_trader/load`) has **no poller** — the trade-then-file path was only built for the *Granary*.
   **Recommendation:** keep the trader as a direct confrontation (two distinct curdle flavours) and
   treat `wheat_ambush_armed` as vestigial, or build the mirror poller in `wheat_trader/tick`. Confirm
   which.
6. **Granary keeper count & placement.** Three proposed (Hua Zhan built + Crossroads + east). With
   ten counting fields, is a fourth keeper wanted on the promoted-west side (Westwind/Gaviota)?
   Confirm the new bodies and register their UUIDs in `npc_presets.json` before regen.
7. **Capstone quests (5d/5e).** Green-light "The Granary Ledger" (Lucian tie-in, circles `frag_7`,
   gate re-mapped to `>=6`) and "Homecoming Convoy" (all-**ten** payoff)? Both are
   NPC-placement-only, no build.
