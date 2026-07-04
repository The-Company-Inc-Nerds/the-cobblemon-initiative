# NPC Placement — Beat 2: Harvest Road · Hua Zhan City · Gym 2

Companion to `PLACEMENT_TOWNS_1-2.md`. Everything below is compiled and wired (143
characters, 0 errors); this is the in-world pass. Same routine: place → send
`preset = uuid` → I recompile → `/reload` → `update_npc_presets` → `register_sight` →
hand-apply the entity tags.

## Already cast onto existing bodies (import-only — no placement)

| Body (CSV) | Role | Where |
|---|---|---|
| Liang Yue | wheat_trader_1 — the Grain Buyer | market row (tag `hz_wheat_trader`) |
| Tang Yushu | wheat_trader_2 | market (tag `hz_wheat_trader`) |
| Guo Tian | granary_keeper (badge-tier retiers now live) | granary building (tag `hz_granary`; VERIFY which building reads as the granary) |
| Mei Lanying | hua_zhan_guide (greenhouse pointer + pilgrimage hint inlined) | gym cluster |
| Han Mingxiu | hz_miller — Grain In, Goods Out giver | silo/grain district |
| Zhou Yiren | hz_greenhouse_greeter (sight approach_once) | greenhouse south approach |
| Narin Chen | hz_greenhouse_archivist | greenhouse mezzanine |
| Xiao Mei | hz_greenhouse_overseer (battle CUT — non-combat) | catwalk (VERIFY the reveal box x1535 dx15 / y98 dy8 / z2101 dz15) |
| Kaito Zhang | hz_martkeeper — Adjusted Retail | nudge to the Pokémart counter ~(1530,84,2076) |
| Linh Hua / Wei Shun / Mei Lin | the 3 price-check stalls (Mei Lin also carries the clinic-runner entry) | their CSV spots |
| Anong Rattana | hz_nurse — paid heals + Out of Network | the Center |
| Chen Bao | hz_branch_manager — Minutes (tag `hz_branch_manager`) | branch tower top floor |
| Master Ruang Wei | garden_master_wei — Four Gardens Pilgrimage | west hill |
| Aya Lian | groundskeeper_aya — exhibition battle | west stair |
| Mirek / Xu Jianyu / Luo Shiming | Harvest Road Regulars (Xu = spotter: npcsight pursue r8) | their CSV spots |
| Deng Haoran | the Tenants of Record trainer beat | roadside (tag `deng_haoran` + `deng_camp`) |

## NEW bodies to place (send `preset = uuid` for each)

| Preset | At (x, y, z) | Notes |
|---|---|---|
| `villain_yield_officer` | 1579, 88, 2459 | Firstfurrow north fence; npcsight dialog r10 (auto via register_sight) |
| `villain_site_manager` | 1585, 88, 2480 | mid-field by the barn |
| `villain_route_surveyor` | 1558, 88, 2378 | mid-road; sight armed POST-badge-1 via arm.mcfunction |
| `villain_route_escort` | 1563, 88, 2382 | long clear sightline; sight armed via arm.mcfunction |
| `survey_wagon` | 1560, 88, 2380 | disguised prop flush against a wagon/cart |
| `old_deng` | 1478, 88, 2440 | camp west of the farm; tag `deng_old` + `deng_camp` |
| `granny_yun` | 1476, 88, 2437 | beside him; tag `deng_granny` + `deng_camp` |
| `watch_lantern` | 1550, 88, 2470 | First Night Watch anchor, just INSIDE the west fence |
| `station_moss / orchard / terrace / pond` | (1450,93,2052) · (1432,85,1964) · (1478,87,2098) · (1484,87,2160) | 4 pilgrimage plaque props beside the re-staged gym wardens |
| `hua_zhan_leader (Blossom)` | 1501, 86, 2054 | gym hall terminus — interior coords approximate, adjust to the floor plan |
| `villain_yield_analyst` | 1505, 86, 2043 | gym gate; tag `yield_analyst`; npcsight dialog r10 |
| `rezoning_notice_board` | 1503, 86, 2041 | prop beside the analyst |
| `greenspace_plaque_square` | 1498, 86, 2050 | the clean bright square inside the gym entrance (prop) |
| `hz_receptionist` | 1540, 86, 2001 | branch tower lobby; tag `hz_office_staff`; npcsight dialog r10 |
| `hz_analyst` | 1532, 93, 2005 | branch tower mezzanine; tag `hz_office_staff`; npcsight dialog r10 |
| `hz_greenhouse_docent` | 1544, 88, 2112 | greenhouse lobby (NEW body — Kaito Zhang is taken by the mart) |

## Special steps

- **`sidequest/right_of_way/arm.mcfunction`**: after placing the two road agents, send me
  their UUIDs — I fill the `%%...%%` placeholders (the detail arms itself at badge 1).
- **Gym-2 wardens are RCT spawns**: their station coordinates are already baked into the
  gym config (moss/orchard/terrace/pond) along with Lian/Sakura/Blossom interior spots —
  verify the interiors against the real floor plan.
- **Buildings to verify in-world** (castings assume them): the granary building, the
  greenhouse glass tower (the (1540,2110) Y-stack), the branch-office tower (the
  (1535,2000) Y-stack). If any reads differently, the castings move with the real one.
- **Remove/fence free healing machines** in Hua Zhan (paid-nurse rule, same as gym 1).
- Full quest-by-quest test script: `docs/VERIFICATION_RUNBOOK.md`.
