# NPC Placement — Sango · Blossom Path · Takehara Falls · Gym 1

**Status 2026-07-03: the casting is WIRED.** All 40 placed NPCs from the builders' CSV are
mapped to quest presets, and `content_compile` now **merges each body's builder data**
(remote-URL skin, model, slim/classic variant, equipment, sounds, home anchor) from
`mrpack/maps/.../easy_npc/npcs/<uuid>.npc.nbt` into the compiled preset — so a preset
import applies our dialog/quests **and** keeps their look, renaming the NPC to its role
(name colors preserved). Skins came along automatically (the builders already use
minecraftskins.com URLs); only Lumo's was overridden per the showrunner
(masked-cultist URL).

## In-game steps after every compile (OP 2)

1. `/reload`
2. `/function cobblemon_initiative:update_npc_presets` (41 imports; slim bodies import from `humanoid_slim/`)
3. `/function cobblemon_initiative:dialog/register_sight` (9 sight NPCs)
4. **Entity tags, by hand** (stand next to each, `tag @e[type=easy_npc:humanoid,limit=1,sort=nearest] add <tag>` — use `easy_npc:humanoid_slim` for slim bodies):
   - Zari + Kiano (the auditors) → `auditor`
   - Kaito (canvasser) → `ci_canvasser`
   - the checkpoint grunts (when placed) → `checkpoint_agent`
   - the surveyor (when placed) → `surveyor`
   - the 4 gym juniors (RCT spawns, Performance Review) → `takehara_sentry` + `/npcsight add`

## The cast as wired (CSV body → role)

**Sango:** Nalia=Mom · Acacia=Professor · **Lani=Lucian** (archive hub ×4 quests) ·
**Asha=Dr. Medcrest** (paid heals + Preferred Provider) · **Deka=derby + Magikarp salesman**
(one tree) · **Sarii=Census Taker** · **Zari/Kiano=Auditors** (Pending Review + Off the
Record) · **Lumo=Company Courier** (Incomplete File; masked-cultist skin) · Oma/Fara(was
Sarii-role)/Kele/Dakarai=the Lane · Miri+Raan=Adjunct Faculty (now 2 errands) ·
**Kofi=Invitational crier** · Kima=round 1 · Tayo=signup+final (2-round bracket) · Sefu=the
trade · **Taya=the dead letter** (was Uncle Marlow) · Elder Nuru/Sentinel=lore/reward ·
Mara=untouched flavor.

**Blossom Path:** Jabari/Ayo/Zola/Kwame = the four eye-contact route trainers
(meadow/spotter/4th/type-tip), levels 9–13.

**Takehara:** **Dashan=Leader Cicada** · Lina=gym guide · **Aiko=Apprentice,
Sora=Jr. Apprentice** (outside the gym — Sora's dialog sends challengers to find Aiko in
the greenhouse; gym config + RCT renamed to match) · Nurse Lila=paid healing · Kaori=Trader
Mayu · **Mei=Printmaker Mei** (Notice quest) · **Shou=Falls Warden** (Cascade Ascent) ·
**Kenji=Curator** (museum brush/dig/revival) + **Sayuri=the bone donation** ·
Kaito=Canvasser · **Mayor Liang**=the roof scene · **Mika=Fisherman Genji** (Out of
Office) · **Eiji=Beekeeper Masumi** (Sweetwater Futures) · **Haruto=Beekeeper Tomo**
(Sting Operation, at the Blossom arch) · Ren/Hana/Taro/Kito/Elder Sefu/Hiro=flavor
(untouched).

## Remaining placements (new bodies — none exist in the world yet)

| Role (preset) | Where | Notes |
|---|---|---|
| `agent_yield_lead` | **(2015, 169, 2466)** gym roof | mayor-scene grunt (doubles battle `sq_mayor_suits`) |
| `agent_yield_second` | **(2015, 169, 2463)** gym roof | second grunt |
| `sango_company_liaison` | **(2581, 111, 2822)** wheat-field fence | facing the field; also pays the Invitational envelope |
| `field_researcher_ume` | **(2192, ~100, 2835)** mid-meadow | Head Count *(coords verified inside the route corridor)* |
| `courier_mio` | **(2486, ~100, 2914)** Sango mouth | Sprint; bell prop at the Takehara arch (~1923, 2584) |
| `forewoman_tetsu` | **(2319, ~100, 2899)** waystation | Work Orders |
| `apiarist_sumi` | **(2290, ~100, 2894)** by the waystation | Work Orders fork B + wild hives |
| `villain_grunt_field_agent` + `villain_grunt_2` | **(2039/2043, ~100, 2704/2708)** path pinch | checkpoint tent; tag `checkpoint_agent` + npcsight both |
| `sq_kyc_agent` | **(2467, ~100, 2913)** Sango approach | post-badge survey table |
| `company_surveyor` | **(2230, ~100, 2866)** patrol anchor | loop past all three posts; tag `surveyor` + npcsight |
| `notice_post_1..3` | **(2343, 2903)** · **(2074, 2736)** · **(1750, 2470 — Harvest Road)** | tiny disguised prop-NPCs on her loop |

All coordinates above are point-in-polygon-verified inside their zone corridors (the routes
are narrow traced corridors, not boxes). They're also on the map: run
`scripts/generate_npc_overlay` and open the zone-mapper — orange dots are these planned
spots, green are wired bodies, gray are flavor NPCs; hover shows name/Y/role.

For each: place → send `preset = uuid` → I recompile (the merge picks up their look
automatically) → rerun the in-game steps.

## Standing notes

- **The merge reads `mrpack/maps/.../easy_npc/npcs/`.** If builders edit NPCs in the live
  instance, refresh that world copy before recompiling, or the merge uses stale data.
- **Royal League healer body (`0caa3fce`) is unbound** for now: the Medcrest preset merged
  onto Asha's slim body, and the royal body is classic-type. Re-bind at the Act-3 league
  pass (needs a type-matched preset variant).
- The old royal Lucian body (`bdc36dd5`) also keeps its builder identity — give it a new
  role or remove it in-world.
- **Paid healing**: remove/fence any free Cobblemon Healing Machine blocks in Sango +
  Takehara so Medcrest/Lila (100 CD via `economy/heal_paid`) are the healing path.
- Set-dressing that pairs with the above: census desk, doc chest/barrel (wired at
  (2591,111,2815) / (2584,107,2925)), Invitational dock dressing, museum dig site,
  Cascade course + record board, three notice boards + mural, apiaries, waystation,
  checkpoint tent, greenhouse arena space for Aiko/Sora.

## Geometry audit notes (2026-07-03, via zone polygons)

- **Elder Nuru (2524,106,2914)** and **Raan (2680,111,2802)** stand just OUTSIDE the Sango
  polygon — their houses are clipped by the zone edge. Quests work regardless, but they
  miss safe-zone/announce coverage; consider a small zone-mapper nudge of the Sango
  polygon to swallow both houses.
- **Lumo (the Company Courier) already stands beside his cart** — the doc-chest pickup at
  (2591,111,2815) is ~10 blocks from him, so the charter courier camps next to the very
  cart the player searches. Dialog updated to match (no move needed).
- **The auditors' bodies are far apart** (Zari at the windmill, Kiano by the lab — ~180
  blocks): their sight cones cover different halves of town, which makes the Off the
  Record runs a two-cone gauntlet. Playable as-is; move them closer to the square if the
  stealth should be tighter.
- **The canvasser (Kaito, 1940,105,2470)** patrols between the gym and Mei's house —
  place the three moth-print notice boards inside his loop (roughly x1900–1990,
  z2440–2520) so the stealth checks read his sightline.
