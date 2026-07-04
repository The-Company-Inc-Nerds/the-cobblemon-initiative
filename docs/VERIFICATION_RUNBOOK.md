# Verification Runbook — Everything Through Hua Zhan

One ordered in-game session (OP 2, a throwaway copy of the world) that proves every system
and quest built through gym 2. Each step lists the **command(s)**, the **expected result**,
and what to report if it fails. Steps are ordered so later ones reuse earlier state.
Force-states are provided so you never have to actually play 10 hours to test.

> Legend: ✅ = pass criteria · 🛠 = force-state shortcut · 🐞 = known-unverified (the reason
> this step exists)

## Round 1 results (2026-07-03) + the fix wave

Tested by the showrunner; outcomes and what changed:

| Finding | Status |
|---|---|
| `givepokemonother` **works** | ✅ VERIFIED — but dialog context needs `@initiator`, not `@s` (below) |
| Dialog commands need **`@initiator`**, not `@s` | 🔧 **FIXED GLOBALLY** — `content_compile` now rewrites every dialog `Cmd` (`@s`/`@p` → `@initiator`; bare `function …` calls wrapped in `execute as @initiator run …`; TBCS `onwin` payloads untouched). All 143 presets + granary tiers regenerated — **re-run `update_npc_presets` before retesting anything.** |
| `economy/payout` "did nothing" | 🔧 Most likely the same root cause (called from a dialog button, `@s` never bound to you). Fixed by the wrap. **Retest two ways:** (a) chat: `function cobblemon_initiative:economy/payout {amount:100}` — should pay ~100 with the gold rate line; (b) any dialog payout (census SIGN → +500). If (a) still does nothing, report — that's a different bug. |
| `cobbledollars remove` **clamps at 0**, never negative | ✅ VERIFIED — decline fees/stakes/purchases are fail-soft by engine behavior; no balance gates needed anywhere. |
| Item ids correct | ✅ VERIFIED |
| Shop (badge_0 + Medicine row) | ✅ VERIFIED |
| Paid heal function heals + charges 100 | ✅ VERIFIED. "No Heal my team button" is expected pre-import: the casting presets (Asha=Medcrest, Lila, Anong) only appear after `update_npc_presets` runs with the new mappings. Underfunded heal: covered by the clamp finding — the fee takes what's there (to 0) and the heal still fires, which is the designed fail-soft. Re-check the button after import. |

## Phase 0 — Boot & wiring (5 min)

1. **Datapack parse** — start the world, check the log.
   ✅ zero `Failed to load function` / `Couldn't parse data file` errors (the beat-2 build
   added ~40 functions + loot tables; a single quote error would print here).
2. `/reload` → same check.
3. `/function cobblemon_initiative:update_npc_presets`
   ✅ ~55 imports run; spot-check three NPCs: **Lani** is renamed *Lucian Scrollkeeper*
   (purple name kept, savanna-princess skin kept — 🐞 this proves the world-merge),
   **Deka** offers both derby + fish, **Dashan** is *Leader Cicada*.
4. `/function cobblemon_initiative:dialog/register_sight`
   ✅ no errors; Nalia approach fires when walking out of the house.
5. **Entity tags** (stand next to each; slim bodies use `easy_npc:humanoid_slim`):
   `tag @e[type=easy_npc:humanoid,limit=1,sort=nearest] add <tag>` —
   Zari+Kiano→`auditor` · Kaito→`ci_canvasser` · checkpoint pair→`checkpoint_agent` ·
   surveyor→`surveyor` · Takehara juniors→`takehara_sentry` · branch-office staff→
   `hz_office_staff` · Chen Bao→`hz_branch_manager` · watch lantern→per its note.

## Phase 1 — The standing command smoke-tests (10 min)

These commands are used by dozens of quests but are **unverified in this Cobblemon build**:

6. 🐞 `givepokemonother @s magikarp level=5` → a Magikarp enters your party/PC.
   *(If it fails: report the exact error — every gift/trade quest falls back to
   spawnpokemon + free ball and I rewire in one pass.)*
7. 🐞 `execute as @a run function cobblemon_initiative:economy/payout {amount:100}`
   → CobbleDollars +~100 with the gold "Company Verified Rate" actionbar.
8. 🐞 `cobbledollars remove @s 999999` with a low balance → note what happens
   (negative? clamps? errors?). Decline fees + stakes lean on this.
9. 🐞 `give @s cobblemon:poke_rod 1` · `give @s cobblemon:exp_candy_m 1` →
   items exist (ids validated offline, one live confirm).
10. 🐞 Open the shop (Cobble Trader): **badge_0 tier shows the Medicine row**
    (potion 300 / super 700 / antidote 250 / paralyze_heal 250).
11. 🐞 Nurse heal: click **Heal my team — 100 CD** on Medcrest (Asha) →
    party healed AND balance −100.

## Phase 2 — Sango + Blossom Path quests (20 min)

12. **Opening chain**: leave the house → Nalia intercept → lab → Pokédex button →
    starter → Running Shoes (✅ boots have +10% speed — 🐞 attribute syntax).
13. **Lucian chain**: talk to Lani-Lucian → *open the file* → ✅ writable book + 3 balls +
    ~300 CD. Then the pickups: stand at the **cart chest (2591,111,2815)** → portrait
    paper auto-grants; **farm-fountain barrel (2584,107,2925)** → ledger page.
14. **Census**: Sarii's desk → SIGN → ✅ 500 CD (skewed) + Provisional Resident ID paper
    (🐞 the new loot table).
15. **Stealth (Off the Record)**: take Lucian's satchel; walk past a tagged auditor →
    ✅ OBSERVATION LOGGED actionbar, throttled ~3s.
    🛠 no auditors placed? `tag @e[...,limit=1] add auditor` on any NPC and stand in view.
16. **Deka**: enter the derby (150 CD) → ✅ SANGO CLASSIC bossbar 6:00; buy the fish →
    ✅ −500 CD + Magikarp.
17. **Route trainers**: walk Blossom Path past Jabari/Ayo/Zola/Kwame →
    ✅ each pursues on sight and battles on touch (after their `npcsight` registration).
18. **Checkpoint (Per My Last Memo)**: approach the tent pair → EYES ON YOU meter;
    loiter unseen 8s → memo beat. Fight both → ✅ they despawn, `easy_npc delete` style.

## Phase 3 — Takehara + Gym 1 (15 min)

19. **Gym ladder**: Koji→Yuki→Shin→Taro (RCT spawns at the four coords) → **Sora** →
    **Aiko in the greenhouse** (✅ Sora's win line points to her) → Cicada.
    ✅ Cicada's team is **Scolipede/Heracross 17, Vespiquen/Yanmega 18** (🐞 proves the
    RCT regeneration; if you see a lv-24+ Scyther the old files are cached somewhere).
    ✅ badge → cap 30, `frag_1`, shop tier `badge_1`, instability → 8.
20. **Nurse Lila** charges 100 CD. **Canvasser stealth** (Mei's prints): paste while seen →
    voided; unseen ×3 → ✅ pay + heal_ball.
21. Mayor Liang roof scene (once roof grunts are placed at 2015,169,2466/2463).

## Phase 4 — Harvest Road (the new backbone) (15 min)

22. **Right of Way arming**: pre-badge the detail must NOT ambush.
    🛠 `tag @s add defeated_takehara_leader` → within a tick ✅ chat line *"a survey detail
    unfolds a wagon of paperwork"* (proves `right_of_way/arm` fired; needs the two UUIDs
    filled in `arm.mcfunction` first).
23. Beat the Assessor + Escort → ✅ both despawn; the **wagon** now yields the Route
    Manifest book. File it at Lucian → ✅ ~250 CD.
24. **Unauthorized Harvest**: the Officer hails at the fence; the Manager refuses battle
    until the Officer falls (✅ gate line). Beat the Manager →
    ✅ **zone banner flips to *Firstfurrow Farm — Liberated***, actionbar *"the commodity
    currency loses ground"*, `cd_instability` −6, Transition Order book drops.
    🛠 direct test: `function cobblemon_initiative:liberation/free_field {field:farm_1}`.
    ✅ HUD side line **"Liberate the occupied fields 1/6"** (no wheat word!).
25. **Regulars**: Mirek (opt-in), Xu Jianyu (spotter, from the ledge), Luo Shiming
    (wager 120 loss fee). 🐞 Luo's autumn Deerling skin (`aspects` field — first use).
26. **Tenants of Record**: Old Deng camp (once placed) → quest arc → post-liberation
    homecoming tick moves them (🛠 liberation state from step 24).
27. **First Night Watch**: talk to the watch lantern post-liberation → survive till dawn
    → ✅ bossbar + reward. 🛠 `time set 13000`.

## Phase 5 — Hua Zhan + Gym 2 (20 min)

28. **The reveal**: talk to a wheat trader (Liang Yue) → ✅ the word **wheat** appears for
    the first time; HUD wheat line lights only now (🐞 gate: `wheat_war_active` AND
    `heard_wheat_pitch`). Granary keeper (Guo Tian) opens the grain shop.
29. **Pilgrimage**: Garden Master Wei (Ruang Wei) opens it; each garden station: beat the
    RCT warden → press the plaque → seal. ✅ HUD *Garden seals n/4*; 4 seals → blessing
    (🐞 `leaf_stone` id) → Blossom's `pilgrim` line before the fight.
30. **Gym ladder**: 4 wardens at their stations → Lian → Sakura (doubles) → Blossom
    ✅ **Tropius/Leafeon 27, Roserade 28, Venusaur 29** (🐞 proves gym-2 RCT regen).
    ✅ badge → cap 38, `frag_2`, `badge_2` shop tier, instability → 16.
31. **Greenspace 7**: the Yield Analyst at the gate — eavesdrop unseen 8s (🛠 the
    memo-loiter meter) → Yield Report; battle opt-in (✅ *"Yield Analyst"* on the battle,
    NOT *"Market Analyst"* — the name-collision fix). Post-badge → the *retained* line +
    150 CD disbursement.
32. **Adjusted Retail**: Kaito Zhang's price check → 3 stalls → ✅ HUD *Price checks n/3*;
    ✅ stall dialog digits match the shop's real swing (54/216 at idx 16).
    🐞 This quest also ships the `#idx`-mirror fix — check any `cd_instability`-gated
    dialog band now fires.
33. **Out of Network**: Anong-nurse heals for 100 CD; berry restock quest pays ~240 CD.
34. **Minutes of the Quarterly Review**: branch tower — sneak past reception + mezzanine
    cones (`hz_office_staff` tags) → loiter at the top 8s → minutes paper; ✅ closing line
    is *"there was never a founder"* (the erasure must never regress); file at Lucian →
    ✅ 400 CD + the priority-75 entry surfaces before his other turn-ins.

## Phase 6 — Regression sweep (5 min)

35. `/ca quest hide` → `/ca quest show` → HUD ladder intact (no orphan side lines).
36. Relog → ✅ bossbars/objectives re-arm (all `load` functions are idempotent).
37. `scoreboard players set @s memory_fragment 2` + walk past a grunt → recognition still
    memo-tier (bands unbroken).
38. Report **any** failing step with the exact command + log line — one report per line is
    enough for me to patch and re-issue.

## Known-unverified master list (what this session retires)

`givepokemonother` (+gender/level keys) · `cobbledollars add/remove @s` under `execute as` ·
insufficient-balance behavior · `can_see_player` stealth branches (auditor/surveyor/
canvasser/office) · renamed give/loot component shapes (boots, IDs, books, papers) ·
`cobblemon:poke_rod`/`leaf_stone`/fossil ids · RCT `aspects` (autumn Deerling) · the
world-merge preset import (skin+dialog+rename) · shop tier medicine rows · paid-heal
function · sight arming via runtime `npcsight add` (right_of_way) · Easy NPC
sight-dialog fallback when the labeled entry is gated off.
