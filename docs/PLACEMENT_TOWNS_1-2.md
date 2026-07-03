# NPC Placement — Sango · Blossom Path · Takehara Falls · Gym 1

Working checklist for placing the Act 1 cast. Everything below is compiled and ready; this
is the in-world pass.

## How this works

1. **Place** the NPC in-world (Easy NPC). Give it the right **model/skin** (your call) and,
   for props, the disguise noted below.
2. **Send me** the mapping as `preset = uuid` (one line each — e.g. `angler_bess = 1a2b3c4d-...`).
   I add the UUIDs, re-run `content_compile`, and the preset + sight registrations regenerate.
3. **In-world, run** (OP 2), in order:
   - `/function cobblemon_initiative:update_npc_presets` — imports dialog/preset onto every mapped NPC.
   - `/function cobblemon_initiative:dialog/register_sight` — registers the sight NPCs (auto-built from UUIDs).
4. **Apply entity tags by hand** for the ones marked **TAG** below — the preset import can't do this:
   `tag @e[type=easy_npc:humanoid,limit=1,sort=nearest] add <tag>` (stand next to it).

Anything you place that has a **sight** entry only starts working after step 3; anything with a
**TAG** only works after step 4.

## Already placed (mapped) — just needs a re-import

Run `update_npc_presets` after the next compile and these pick up their new/updated dialog:

| Preset | UUID | Action |
|---|---|---|
| `nalia_mom` (Mom) | `74b1c524…` | done — opening chain live |
| `professor_acacia` | `92d33528…` | done — Pokédex/starter live |
| `aurelius_medcrest` | `0caa3fce…` | re-import (now carries Pending Review + Preferred Provider) |
| `elder_nuru` | `306a81a0…` | re-import (Sango lore) |
| `elder_sentinel` | `277f717f…` | re-import (Pending Review refuse-fork reward) |
| `lucian_scrollkeeper` | `bdc36dd5…` | **MOVE him** to the Sango lane (lab→west gate) + re-import. He now runs The Incomplete File, the memo/dead-letter turn-ins, **and** Off the Record. Sight already registers (approach_once). |

## Zone anchors (rough centers / bboxes)

| Zone | Center (x,y,z) | BBox |
|---|---|---|
| Sango Town | ~(2601, 64, 2862) | x2505–2713 · z2742–2993 |
| Blossom Path (Route 1) | ~(2185, 64, 2786) | x1923–2530 · z2564–2925 |
| Takehara Falls | ~(1915, 64, 2467) | x1776–2123 · z2262–2634 |
| Harvest Road (Route 2) | ~(1570, 64, 2422) | x1456–1831 · z2286–2521 |

---

## Sango Town — new NPCs (24)

**Sight NPCs — place + TAG + they register via `register_sight`:**

| Preset | Place | Sight | TAG |
|---|---|---|---|
| `auditor_a` | patrol loop around the census desk + mouth of the lane (never Nalia's porch) | dialog r12 | `auditor` |
| `auditor_b` | opposite half of the square loop, so the two cones cross | dialog r12 | `auditor` |
| `company_surveyor` | Blossom Path patrol passing all three notice posts | dialog r12 | `surveyor` |

**Prop NPCs — place + disguise (small/invisible model against real set-dressing), no sight/tag:**

| Preset | Place |
|---|---|
| `doc_portrait_crate` | behind the town hall, under a repainted "bright rectangle" wall |
| `doc_ledger_barrel` | in/beside the shore warehouse on the waterline |
| `notice_post_1` | Blossom Path wall inside the surveyor patrol |
| `notice_post_2` | farther along, where pulling it means standing in the open |
| `notice_post_3` | Harvest Road end of the surveyor loop (the exposed far pull) |

**Battle NPCs — the Shorefront Invitational bracket (place at the docks):**

| Preset | Trainer | Place |
|---|---|---|
| `lumo` | `sq_bracket_1` | round 1 — dockside |
| `kima` | `sq_bracket_2` | round 2 — dockside |
| `tayo` | `sq_bracket_3` | final — dockside |

**Plain quest NPCs:**

| Preset | Quest | Place |
|---|---|---|
| `angler_bess` | Record Quarter (fishing derby) | the pier |
| `census_taker` | Pending Review | census desk in the square (never on Nalia's lane) |
| `company_courier` | The Incomplete File st.1 | north road, on Blossom Path just past the gate |
| `oma` | The Lane / Off the Record | her fence on Nalia's lane, near the west gate |
| `sarii` | The Lane door 1 / Off the Record | by the town noticeboard, two doors from Oma |
| `dakarai` | The Lane door 3 | the end house of the lane |
| `kele` | The Lane door 2 (Magikarp) | scouted water spot in Sango (x2505–2713 z2742–2993) |
| `kofi` · `miri` · `raan` | Adjunct Faculty | three ordinary Sango houses (assistants) |
| `sefu` · `taya` | In-Kind Exchange (trades) | two spots in town |
| `sq_uncle_marlow` | No Such Recipient | 2–3 doors from Nalia's, by his door with the mailbag |
| `sango_dock_crier` | Invitational (crier) | the docks |
| `sango_company_liaison` | Invitational (Company rep) | the podium |

---

## Blossom Path / Route 1 — new NPCs (8)

| Preset | Trainer / Quest | Place | Sight |
|---|---|---|---|
| `sq_regular_meadow` | `sq_regular_meadow` (Picnicker Ren) | first pinch point west of Sango | — |
| `sq_regular_spotter` | `sq_regular_spotter` (Bird Keeper Hoku) | mid-route, **long clear sightline** (walk-up ambush) | **pursue r8** — register_sight, no tag |
| `sq_regular_typetip` | `sq_regular_typetip` (Bird Keeper Hina) | Takehara-side end, before the arch (~1923,2584) | — |
| `field_researcher_ume` | `sq_headcount_wager` (Head Count) | folding table mid-meadow | — |
| `asset_liquidator` | Long-Term Growth Vehicle | roadside "COMPANY SURPLUS (unaffiliated)" stall | — |
| `courier_mio` | Quarterly Sprint | Sango-side mouth (~x2505) at a start line; bell prop at the Takehara arch | — |
| `forewoman_tetsu` | Roadside Work Orders | mid-route waystation shack + work-order board | — |
| `apiarist_sumi` | Work Orders fork B (Combee) | meadow by her apiary — needs a few placed bee hives | — |

---

## Takehara Falls — new NPCs (11)

**Sight NPC — place + TAG + register:**

| Preset | Trainer | Place | Sight | TAG |
|---|---|---|---|---|
| `company_canvasser` | `sq_canvasser` | patrol loop: gym entrance → falls overlook → bridge, sweeping past Ume's stall | dialog r10 | `ci_canvasser` |

**The mayor-roof scene (place all three on the gym roof balcony):**

| Preset | Trainer | Place |
|---|---|---|
| `mayor_suzune` | — | center of the rail, falls behind him |
| `agent_yield_lead` | `sq_mayor_suits` | mayor's left, clipboard-forward |
| `agent_yield_second` | — | mayor's right, holding the silence |

**Other quest NPCs:**

| Preset | Trainer / Quest | Place |
|---|---|---|
| `fisherman_genji` | `sq_genji_wager` (Out of Office) | rocks below the falls at the plunge pool |
| `sq_beekeeper_tomo` | `sq_sting_agent` (Sting Operation) | Takehara end of Blossom Path, among the hive trees |
| `beekeeper_masumi` | Sweetwater Futures | terrace apiary beside the falls |
| `curator_tamiko` | Sediment & Acquisitions (museum) | in the museum by the empty "PENDING RE-VERIFICATION" plinth |
| `falls_warden_ayame` | Cascade Ascent | plunge-pool base by her sign-up board |
| `printmaker_ume` | Notice of Non-Compliance | print stall near the gym, within eyeshot of the canvasser loop |
| `trader_mayu` | Assets in Kind | one-desk exchange by the falls bridge |

---

## Gym 1 (Takehara Bug Gym)

**Easy NPC — place + send me `preset = uuid`:**

| Preset | Place |
|---|---|
| `takehara_leader` (Leader Cicada) | the arena |
| `takehara_guide` | gym entrance |

**RCT trainers — these are NOT Easy NPC presets; place them via RCT (the "not placed" ones you mentioned):**

| RCT id | Name |
|---|---|
| `takehara_trainer_1` | Bug Catcher Koji |
| `takehara_trainer_2` | Entomologist Yuki |
| `takehara_trainer_3` | Bug Maniac Shin |
| `takehara_trainer_4` | Youngster Taro |
| `takehara_jr_apprentice` | Jr. Apprentice Hachi |
| `takehara_apprentice` | Apprentice Hana |

✅ **Battle teams done (2026-07-03):** all 7 Takehara RCT files now mirror the gym config —
a clean cap-20 ladder (Koji/Yuki 10 → Shin/Taro 11 → Hachi 13 → Hana 15–16 → **Cicada
17–18**). This also fixed a live bug where the RCT files had Cicada at Scyther 24 / Volcarona
26 (over cap, wrong species). Every side-quest battle also has an RCT file now, so battles
resolve.

- **Performance Review** (if kept) needs the four ladder trainers registered as sight sentries: after RCT placement, `/npcsight add <uuid> <range> dialog` on each of Koji/Yuki/Shin/Taro and `tag <entity> add takehara_sentry`.

---

## Villain NPCs that live in these beats (2)

| Preset | Trainer | Quest | Place | Sight / TAG |
|---|---|---|---|---|
| `sq_kyc_agent` | `sq_kyc_agent` | Know Your Customer (post-badge) | Sango approach of Blossom Path, survey table | — |
| checkpoint agents | `villain_grunt_1/2` | Per My Last Memo | a checkpoint tent on Blossom Path | `tag checkpoint_agent` + `/npcsight add` (both) |

✅ `villain_grunt_1` minted (2026-07-03) as the left-flank checkpoint enforcer; both
`villain_grunt_1/2` now have RCT battle files, so the memo fight path resolves. Place both,
`tag … add checkpoint_agent`, and `npcsight add <uuid> 12 checkpoint_hail`.

---

## Set-dressing that pairs with placement (your build, via snbt_merge / in-world)

- **Sango:** census desk; bright-rectangle wall + crate behind town hall; shore-warehouse barrel; three Blossom Path notice posts; the docks (Invitational); Kele's water spot; the pier (derby).
- **Blossom Path:** Liquidator's stall + sign; Mio's start line + delivery bell at the arch; Tetsu's waystation + board; Sumi's apiary + wild bee hives; Ume's meadow table.
- **Takehara:** gym-roof balcony; museum interior + dig site + "PENDING RE-VERIFICATION" plinth; Cascade parkour course + record board; three notice boards + the painted-out mural; Masumi's terrace apiary; Mayu's exchange desk; Genji's plunge-pool rocks.

## What to send me

Just the mappings, e.g.:

```
angler_bess = <uuid>
oma = <uuid>
company_surveyor = <uuid>     # I'll also remind you: tag it `surveyor`
takehara_leader = <uuid>
...
```

You can send them in any batch (per zone is easiest). After each batch I compile, and you run
`update_npc_presets` + `register_sight`, then hand-apply the **TAG** commands.
