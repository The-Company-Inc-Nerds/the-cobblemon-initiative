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


## Round 2 results (2026-07-03 evening, log-0.4.0-alpha.1) + the fix wave → 0.4.1-alpha.1

The full PrismLauncher log explained everything:

| Finding | Root cause | Fix |
|---|---|---|
| No Dr. Asha rename / no dialog changes | **`update_npc_presets` never loaded** — `easy_npc preset import data <uuid> <preset>` is the WRONG argument order; Easy NPC 6.25 wants **`<preset_location> <uuid>`** (decompiled from PresetCommand). Install printed "refreshed" but dispatched a function that did not exist. | Both generators + the Java legacy path flipped; function regenerates clean. |
| All 34 `granary/apply_*` failed to load | Same wrong argument order | Same fix |
| `payout` did nothing | Two causes: it was run **without its required argument** (macro functions hard-fail argless — the log shows the bare call), and every dialog path was moot since presets never imported | None needed — retest as `function cobblemon_initiative:economy/payout {amount:100}` **with the braces** |
| 7 loot tables failed to parse (`provisional_id`, `memo_44c`, `dead_letter`, `quarterly_minutes`, `rezoning_memo`, `silent_stakeholder`, `supper_pail`) | 1.21.1 `set_lore` **requires `mode`** | `"mode": "replace_all"` added to all; this also un-breaks the 3 functions that loot-give them (`ghost_reward`, `hear_minutes`, `recover_memo`) |
| `derby/load` failed to load | `bossbar … color aqua` — aqua is not a bossbar color | → `blue` |
| `right_of_way/arm` failed to load | `%%UUID%%` placeholders break function parse | placeholder lines commented; uncomment at placement |

**Round 3 (0.4.1-alpha.1) — the same canary list, now expected to pass:**
1. World load: **zero** `Failed to load function` / `Couldn't parse element` lines (was ~40).
2. `/cobblemon-initiative install run` → walk to Asha → nameplate **Dr. Asha** + heal button.
   ⚠ If she does NOT rename: the one remaining unknown is whether `preset import data`
   applies onto an EXISTING uuid — report exactly what chat says and I'll switch strategies.
3. `function cobblemon_initiative:economy/payout {amount:100}` (with braces!) → ~100 CD + gold rate line.
4. Census SIGN → +500 + the ID paper (now that both the loot table and @initiator paths are fixed).

## Round 4 results (2026-07-03 night, log-0.4.1-alpha.1 + log-0.4.1-alpha.2) → 0.4.2

Round 3's flip was still wrong — this time the grammar was pulled from the **decompiled
6.25.0 jar** (PresetCommand bytecode), not guessed from an error cursor:

| Finding | Root cause | Fix |
|---|---|---|
| `update_npc_presets` + all 32 `granary/apply_*` STILL failed to parse (identical errors in both alpha.1 and alpha.2 — the alpha.2 commit only touched configs) | Easy NPC 6.25 grammar is `preset import data <location> [<x y z> [<uuid>]]` — a UUID is **only accepted after a position**. `<location> <uuid>` was never a valid form. | Command template is now `execute as <uuid> at @s run easy_npc preset import data <loc> ~ ~ ~ <uuid>` — in-place update when loaded, safe no-op when not. |
| Even a valid import would have been **blocked**: `Blocked invalid DATA preset resource` | `PresetSecurity.validateResourceLocation` (bytecode-verified) requires DATA presets in namespace `easy_npc`, path prefix `preset/`, extension `.npc.snbt`. Ours shipped as `cobblemon_initiative:easy_npc/default_preset/…` with no extension in the reference. | All 177 presets moved to `data/easy_npc/preset/<type>/<name>.npc.snbt`; generators emit `easy_npc:preset/…npc.snbt`. |
| A one-shot install refresh can never reach the whole map | `preset import` updates in place only for **loaded** entities (`LivingEntityManager` tracks loaded NPCs only); an unloaded UUID would spawn a duplicate at 0,0,0. | New `NpcPresetRefreshManager`: per-NPC content-version tracking in the world save; re-imports each mapped NPC as its chunk loads. `install run` arms it and reports honestly. |
| Every Easy NPC dialog button command would be **silently blocked** | 6.25.0 blocks all `ExecAsUser` commands whose root is not in `executeAsUserCommandAllowList.*` — and the shipped security.cfg had them **empty**. | Allowlists populated with the pack's command roots (execute, function, cobbledollars, tag, tbcs, …). |
| `payout` paid nothing even when invoked correctly | CobbleDollars 2.0.0-Beta-5.1 has **no `add` subcommand** (verified from the jar: pay/query/give/remove/set/reload/leaderboard). Every `cobbledollars add` in the repo — pay_macro, granary ambush, and **all 143 battle-prize onwin strings** — was a dead command. | Global `cobbledollars add` → `cobbledollars give` (same target-first arg order). |
| 8 dialog buttons paid nothing | They called `economy/pay_macro {paid:N}` directly — the macro also needs `rate`/`raw`, and a macro invoked with missing keys fails entirely. | Rerouted to `economy/payout {amount:N}` (dialog-src + compiled presets). |
| 16 advancements failed to parse (`Unknown registry key … cobblemon_initiative:badge_icon`) | Item registers under the **hyphen** mod id (`cobblemon-initiative:badge_icon`); advancements referenced the underscore datapack namespace. | Advancement icons → `cobblemon-initiative:badge_icon`; base model fallback texture → `all_badges` (kills the missing-texture warn). |

**Round 5 canary list (0.4.2):**
1. World load: **zero** `Failed to load function` and zero advancement parse errors.
2. `/cobblemon-initiative install run` → chat says "NPC preset refresh armed for N mapped NPC(s); M loaded now…" (no more unconditional "refreshed").
3. Walk to Asha → nameplate **Dr. Asha** within a second of her chunk loading (watch the log for `[NPC Refresh] Applied …`).
4. `function cobblemon_initiative:economy/payout {amount:100}` (as a player, with braces) → ~100 CD credited (`cobbledollars query` to confirm) + gold rate actionbar.
5. Win any trainer battle → prize money actually lands (was dead in every build so far).
6. NPC dialog buttons execute (no `Blocked execute-as-user command root …` lines in the log) — needs the new security.cfg in the instance's `config/easy_npc/` (fresh mrpack install, or copy it into the existing instance).

## Round 6 results (2026-07-04, showrunner 0.4.3-alpha.1 session) → 0.4.3-alpha.2

The showrunner's pokedex experiment (removing one condition made the entry open) exposed
the round's headline bug, bytecode-confirmed in the 6.25.0 jar:

| Finding | Root cause | Fix |
|---|---|---|
| pokedex entry / Running Shoes entry / census taker "unreachable" | **Easy NPC's PLAYER_TAG condition IGNORES the Operation field** — `PlayerTagCondition.evaluate` is just `tags.contains(name)`, so NOT_EQUALS behaves as "player HAS the tag" (the opposite of intended). Every not-gate in the pipeline was dead, including the round-5 battle-button gates. | All negations lowered to `EQUALS no_<tag>` — inverse tags maintained every tick by the generated band_tags function (`tag @a[tag=!X] add no_X` / `tag @a[tag=X] remove no_X`). Snippets + template updated. Full recompile. |
| Starter screen "did not work" | Unchanged from Round 5: the command executes and Cobblemon refuses (`starterSelected: true` in the test save, refusal suppressed). On the mid-progress world the (broken) post_starter gate ALSO fell back to the default entry, re-showing the starter button — consistent with the report. | Retest on a fresh world, or set `"starterSelected": false` in `cobblemonplayerdata/<uuid>.json` with the world closed. |
| Missions not on the sidebar (mom / dead letter) | Mom's errand only rendered on the MAIN line pre-badge-1; the dead-letter tag was likely never set (accepted during the allowlist-blocked era). | Opening chain now ALSO renders as a side line (slot 81) on any world state the moment `mom_sent_to_lab` is set; re-accept the letter from Marlow to light its line. |
| Top "Objective" boss bar unwanted | Showrunner call. | Removed everywhere (load/show/hide/render/set_gym); `bossbar remove` on load clears it from existing worlds. Sidebar main line carries the story. |
| Dialogs missing a close button | "Give me a moment" was removed round-5; other entries never had one. | content_compile auto-appends a **Goodbye** close button to any entry without one (`"no_goodbye": true` opts out for forced encounters). |
| Paid heal heals a broke player | By design pre-round-6; showrunner wants a hard gate. | `heal_paid` gates on `execute store result … cobbledollars pay @s 100` (bytecode-verified: pay checks the source balance before mutating, self-pay is net-zero, fail returns 0 — NOTE `store success` would NOT work, pay soft-fails). |
| JEI still enabled | `.jar.disabled` path was not honored in the launcher install test. | Shelved — flag removed from modpack.json; build_mrpack.py keeps `"disabled": true` support if revisited. |

**Round 7 canary list (0.4.3-alpha.2):**
1. Fresh world (or reset `starterSelected`): Mom runs up once → "I am ready" → errand accepted → sidebar gains "• Choose a partner at the Sango lab" within a second; no boss bar anywhere.
2. Professor: Choose a partner → starter screen OPENS; close dialog, re-open → "take the Pokedex" entry (no locked buttons); take it → sidebar line advances to "Show Mom your Pokedex".
3. Mom: Running Shoes entry now opens (was the same NOT_EQUALS deadlock); every dialog has a Goodbye.
4. Census taker's first dialog opens and its accept button works.
5. Accept the dead letter from Marlow → "• Deliver it" side line lights.
6. Paid heal with <100 CD → red "Payment declined." actionbar, NO heal; with ≥100 CD → heal + fee.
7. Dialog battle vs an undefeated trainer STARTS (the round-5 gate was inverted by the same condition bug — battles were only offered AFTER defeat).

## Round 7 results (2026-07-04, full-logic deep dive, pre-release audit) → 0.4.3-alpha.2 (same build)

A four-lens walkthrough of every flow (battle stack from the decompiled TBCS/rctmod/rctapi
jars, opening chain, economy, HUD/refresh) — the showrunner's rctmod question answered and
five more latent breaks found and fixed:

| Finding | Root cause | Fix |
|---|---|---|
| **rctmod-native battle locking: NOT usable.** | `tbcs` is a third mod (TBCS 0.14.1-beta) that calls rctapi's BattleManager directly — it BYPASSES all rctmod requirement/defeat tracking; rctmod's own locks + fail dialogs only exist in its spawned-TrainerMob flow, which this pack disables (globalSpawnChance=0, initialSeries empty), and enabling it would fight LevelCapManager. | Keep the Easy NPC Condition gates + onwin defeat tags — they are the only working lock for tbcs battles. |
| The quest sidebar could NEVER show any lines | Vanilla 1.21.1 hides `#`-prefixed score holders from the sidebar (`PlayerScoreEntry.isHidden`, bytecode-verified) — every displayed row rode `#main`/`#side_*`. | All displayed rows renamed to `q.*` fake players; `#` stays scratch-only. (15-row sidebar cap noted; slot scores rank survivors.) |
| Loss fees never charged; the PLAYER spoke the trainer's defeat taunt | TBCS substitutes `@1/@2` WINNERS-FIRST — in the lose list @1=NPC, @2=player; our lose branches assumed the win-branch order. | Lose lists mirrored (`remove @2`, `@1 say`) in the compiler, all snippets, and the template; recompiled. |
| HUD dead past the Royal League | Champion latches `royal_league_champion` (defeat_tag override), render gated on never-set `defeated_royal_champion`; Founder latches `company_overthrown`, render + Mom's homecoming gated on never-set `defeated_villain_final_boss`. | Render re-gated on `royal_league_champion`; `reveal/founder_defeated` now also grants `defeated_villain_final_boss`; new "▶ Face The Founder" branch covers Board-cleared-Founder-alive. |
| Opening-chain main line dead on a fresh world | `memory_fragment` is only ever SET by badge grants; unset scores fail `matches 0`. | Render defines it to 0 first (unset-guard). |
| ESC on the starter screen desynced the chain | `chose_starter` was granted by the button CLICK, not the selection. | Java `STARTER_CHOSEN` hook grants the tag on the actual pick; the button action was removed. |
| "Stabilizing" could RAISE instability | `hq_stabilize` hard-set idx to 25 — a full-liberation player could arrive below 25. | Downward-only clamp (`matches 26..` → set 25); idempotent under its known double-fire. |
| Mom's shoes unreachable at 4+ badges | `pokedex_return` (28) was outranked by `warming` (30). | Priority 45 (self-hides via not_tag). |
| 28 shipped battle buttons target empty/missing trainer teams | 20 `{}` trainer files (Royal League + shrines) + 18 missing files (DJ, Board, admins, grunts 3-11); rctapi refuses empty teams (insufficientPokemon). | content_compile now WARNS per affected battle; casting tracked in TODO §1.B — content work, not code. |

**Round 8 session script (first session on 0.4.3-alpha.2, ~45-60 min, ordered so later
steps reuse earlier state). Use a FRESH throwaway copy of the world for §B; the old
test save has `starterSelected: true` + stale tags (reset via cobblemonplayerdata JSON
if you must reuse it).**

☐ SETUP (5 min)
  ☐ Open world → log: ZERO `Failed to load function`, ZERO advancement parse errors.
  ☐ `/cobblemon-initiative install check` → "bundled preset map loaded" + "ExecAsUser allowlist: OK".
  ☐ PACK-BUILT INSTANCE (mrpack built with --with-map after 2026-07-04): the world opens
    PRE-INSTALLED — hardcore + gamerules already baked into level.dat, shop already
    badge_0, NPC refresh + sight registrations self-arm (log: "NPC Sight storage was
    empty — seeded N registration(s)").
  ☐ AUTO-INSTALL (pack only): ~2s after first join → "[The Company, Inc.] This world has
    been provisioned…" chat line, log "[Auto-Install] Dispatched…", zones + frontiers
    applied, NO kick (world already hardcore). Second boot: no re-run (world latch).
    Bare-mod instance (§G): NO auto-install line — the marker only ships in the pack.
  ☐ `/cobblemon-initiative install run` (still verify once) → "NPC preset refresh armed
    for 63 mapped NPC(s); N loaded now…" (no unconditional "refreshed"), zones added,
    shop seeded badge_0. On a NOT-yet-hardcore world the disconnect screen shows
    "§6The Cobblemon Initiative / §cHardcore mode enabled." (the word Hardcore is new);
    on a pre-baked world it just reports "already hardcore". Re-open.

☐ A. HUD (2 min)
  ☐ NO gold boss bar anywhere (removed even on old worlds).
  ☐ Sidebar shows "⚜ THE INITIATIVE" WITH LINES (fresh world: "▶ Talk to Mom"). This never worked before — # holders were invisible.
  ☐ `/ca quest hide` clears it; `/ca quest show` brings it back.

☐ B. OPENING CHAIN (10 min, fresh world — REVISED 2026-07-04: starter trio replaces the
     vanilla starter screen entirely; the objective-wipe fix un-bricked Mom's walk-up)
  ☐ NO vanilla starter toast/prompt appears at any point (InitiativeInit marks every
    joining player prompted+selected).
  ☐ Mom WALKS UP once (sight range now 12 — she should cross the room to you); after
    the dialog she never re-approaches (walk away/relog).
  ☐ Her first dialog: no "Give me a moment"; "I am ready…" + auto "Goodbye" present.
  ☐ Accept the errand → main line flips to "Visit Professor Acacia at the lab" AND side line "• Choose a partner at the Sango lab" appears within ~1s.
  ☐ Professor → "Choose a partner" → THREE starter NPCs (Skiddo / Totodile / Hisuian Growlithe
    ) spawn at the lab-side spots (2675/2676 128 2899-2903) rendered as the actual
    Pokémon models (COBBLEMON_ENTITY visuals). Click the button AGAIN →
    no duplicates (ci_starters_spawned latch).
    KNOWN-COSMETIC: the Growlithe stand-in renders as BASE Growlithe (Easy NPC's
    renderer is species-only) — the given Pokémon must still BE Hisuian (next check).
  ☐ Talk to a starter → cry + flavor + "Choose <name>" → Pokémon (level 5) lands in the
    party AND chose_starter is set in the same click; HUD flips to "Take the Pokedex".
    If Growlithe chosen: summary screen shows HISUIAN Growlithe (fire/rock typing).
  ☐ Talk to the OTHER two → cry + "…is waiting" hint mentioning the Pokédex thresholds
    (+ Goodbye). The chosen one → cry only.
  ☐ DEX-UNLOCK LADDER (forced scores OK for smoke): `/scoreboard players set @s
    dex_caught 15` → within 2s talk to an unclaimed starter → Lv25 offer entry; claim →
    second_starter_claimed; third starter still cry+hint. Then `set 30` → last starter
    offers Lv40; claim → all three cry-only. (Live signal: dex_caught mirrors CAUGHT
    entries every 40 ticks — catch a Pokémon, watch `/scoreboard players get @s
    dex_caught` tick up. NOTE: forced scores are OVERWRITTEN by the mirror within 2s —
    do the talk-and-claim inside that window, or catch real Pokémon.)
  ☐ Professor re-talk → "A good match…" entry, UNGATED "Take the Pokedex" → pokédex item + got_pokedex + HUD flips to "Show Mom your Pokedex".
  ☐ Re-talk → post-pokédex entry ("How is the Pokedex coming along?").
  ☐ Mom → Running Shoes entry OPENS → take shoes → side line clears, main line = "▶ Defeat the Takehara Falls Gym". Shoes now +30% (walking ≈ vanilla sprint) — F3 or a timed run; confirm NO other speed source (the map's baked Speed I is stripped at build + `effect clear` in install).
  ☐ PURSUIT REGRESSION CHECK (same fix class): any pursue-mode sight NPC (survey wagon
    spotter etc.) actually chases when it sees you — pursuit was dead for all 5.

☐ C. ECONOMY (10 min)
  ☐ `/cobbledollars query <you>` → then `/function cobblemon_initiative:economy/payout {amount:100}` → ~100 CD credited + gold "Company Verified Rate" actionbar.
  ☐ `/cobbledollars set @s 50` → nurse heal → red "Payment declined.", NO heal, balance still 50. `/cobbledollars set @s 500` → heal + fee (400 left).
  ☐ Any dialog payout button (census SIGN etc.) → money lands with receipt.
  ☐ WIN a dialog battle vs an undefeated trainer → battle STARTS (round-5 gates were inverted), prize credited once, defeat tag set, re-talk = already-beaten line.
  ☐ LOSE a fee battle (wager/villain grunt, weak team) → fee DEDUCTED FROM YOU and the NPC (not you) speaks the taunt. (Both were inverted: fee hit the NPC's UUID, you spoke the line.)

☐ D. TOWNS 1-3 QUESTS (15 min)
  ☐ Census taker's FIRST dialog opens (was condition-blocked); accept → HUD line; finish → +500 + paper.
  ☐ Marlow → take the dead letter → "• deliver" side line lights within 1s; deliver to Lucian.
  ☐ Price check accept → "Price checks noted 0/3"; each note increments.
  ☐ Spot-check 3-4 random NPCs: every dialog has a working exit (Goodbye or native close).

☐ E. NPC REFRESH (5 min)
  ☐ Walk toward any mapped NPC's chunk → log `[NPC Refresh] Applied …` once; casting correct (e.g. Dr. Asha nameplate).
  ☐ `/cobblemon-initiative shop badge_1` → "Applied shop tier" log; `shop refresh` errors-free.

☐ F. ENDGAME LADDER, forced tags (3 min, throwaway world/undo after)
  ☐ `/scoreboard players set @s memory_fragment 10` + `/tag @s add defeated_villain_boss` + `/tag @s add royal_league_champion` → "▶ Hunt the Board of Directors".
  ☐ Add all four `/tag @s add defeated_board_{madeline,matt,micah,lauren}` → "▶ Face The Founder".
  ☐ `/tag @s add defeated_villain_final_boss` → "▶ Hunt the Ender Dragon".

☐ G. STANDALONE, separate bare instance (10 min)
  ☐ New instance: Fabric 0.19.3 + fabric-api + fabric-language-kotlin + Cobblemon + Easy NPC (+config_ui) + CobbleDollars + RCTAPI + rctmod + TBCS + architectury + OUR JAR. NO config folder, no overrides.
  ☐ Launch → log: `[Easy NPC compat] Patched … security.cfg — ensured 18 ExecAsUser command root(s)…`.
  ☐ World + any NPC with a command button → the button works on the FIRST press.
  ☐ `/cobblemon-initiative install check` → "ExecAsUser allowlist: OK".

☐ H. AMBIENT LIFE (5 min, added 0.4.3-alpha.4 — spread across the town visits above)
  ☐ Sango: Mr. Mime pops in near Mom's house on approach (proximity spawn, ~40 blocks),
    Magikarp beside Deka at the pond, Hoothoot near Elder Nuru, Sentret at Oma's lane —
    each renders as the actual Pokémon, nameplate on mouse-over, dialog = cry + flavor
    + Goodbye. NONE clipping into walls/furniture (offsets are best-guess: if one is
    stuck, note coords — one-line fixes in ambient/tick + ambient/spawn/<key>).
  ☐ Wanderers actually wander: Kofi, Taya, Elder Nuru (Sango), Sayuri (museum) drift a
    few blocks and drift BACK (soft home tether); they still face you when you walk up.
  ☐ Anchors did NOT start wandering: lane doors (Fara/Kele/Dakarai), stall merchants,
    both nurses, Mom, Acacia all stay put.
  ☐ Takehara: Chansey inside the Pokemon Center, Combee at Masumi's apiary, Psyduck by
    Genji. Hua Zhan: Chansey in the Center, Meowth at the Pokemart, Wooloo at the mill.
  ☐ Relog → no duplicate companions (#amb_* world latches on ci_ambient hold).
  ☐ Log has no "Unknown Cobblemon species" lines (would mean a bad cobblemon_model id).

☐ I. REWARDS RETUNE (5 min, added 0.4.3-alpha.5 — fold into C/D above)
  ☐ Beat gym leader 1 (Cicada) → CD prize + badge + shop tier, NO emeralds anywhere in
    the reward toast/inventory (legacy 5×emerald config entries removed from all 10 gyms).
  ☐ Complete any small errand (census 250 / adjuncts / clinic beat) → payout receipt PLUS
    a training pack lands: 3× Exp. Candy XS + 1× S (minor tier).
  ☐ Complete a 300–400 quest (sweetwater / museum donation / dead letter) → 2× Exp.
    Candy S + 1× M (standard).
  ☐ Complete a finale (derby first win / cascade first clear / sprint first win / night
    watch / invitational / roof meeting / pending review SIGN) → 1× Exp. Candy L + ONE
    random vitamin (major). Refuse-fork mirror: census_refused path gets the same major
    pack from Elder Sentinel alongside his potion kit.
  ☐ REPEAT the derby / gold-time cascade / daily sprint → money only, NO items (farm-loop
    guard). The preferred-provider rx_button daily stays clinic potions only.
  ☐ Log sweep: no `Unknown loot table` for cobblemon_initiative:npc_gift/training_*.

☐ J. ROUND-9 FIXES (0.4.3-alpha.6 — smoke-test round 2 responses)
  ☐ EXISTING-WORLD REPAIRS FIRST (one-time, current test world only):
    `/kill` the in-wall Magikarp then `/scoreboard players set #amb_magikarp ci_ambient 0`
    (respawns at Deka's other side, 2568.5 111 2855.5). If gym-1 staff show beaten
    lines on an unbeaten save: `/tag @s remove defeated_takehara_apprentice` (+ _jr_apprentice,
    _leader) — stale tags from earlier test rounds; `/tag @s list` to audit.
  ☐ Party open → NO "You have not yet selected a starter" message (DATA_SYNCHRONIZED
    fix; works mid-world after one relog).
  ☐ Starter offers: each shows "Keep looking" → closes without choosing; choose one →
    that stand-in DESPAWNS within a tick; other two remain.
  ☐ Hisuian Growlithe stand-in renders the HISUIAN model (clone species; needs
    AllTheMons active — bare-mod installs show the gray substitute, cosmetic only).
  ☐ LEVEL CAP (re-spaced ladder alpha.10): XP stops exactly at 15 pre-badge (actionbar
    "Level cap 15 — the next badge raises it"); a Totodile CANNOT reach 18/Croconaw
    before gym 1; rare candy at cap refused but NOT consumed. Cicada's ace is 17 (cap+2
    — you fight underleveled). rctmod's own actionbar warning must NOT appear.
  ☐ LINCHPIN REGRESSION (alpha.11 — was silently broken): beating the gym-1 LEADER must
    raise the cap to 22 ("§6Level cap increased to §e22") AND let your team pass 15. If
    the cap stays 15 after the badge, the achievementOnDefeat grant regressed.
  ☐ ENDGAME LADDER (future-act, forced tags OK): `/cobblemon-initiative` grant of the
    champion achievement → cap 85; defeating all four board_member trainers →
    "§6[Level Cap] The Board has fallen. Cap raised to 100" + cap 100. (Champion/Board/
    Founder teams are unauthored stubs; test the cap wiring via forced defeats.)
  ☐ AUDIT FIXES (alpha.8): beat kalahar trainer NPCs (when placed) → the RIGHT id is
    credited (names were swapped); after any gym apprentice/leader win
    `/rctmod player get progress` MUST show the id (the alpha.7 dispatch was silently
    unparseable — targets go before "after"); hua_zhan apprentice's roselia now has
    stunspore (no Model validation warn for it in the log).
  ☐ RCTMOD SELF-HEAL (alpha.10): log shows `[rctmod compat] Healed server config:
    allowOverLeveling=true, initialSeries=cobblemon-initiative` at server start — on
    EVERY world incl. a bare fresh world / dev run-client (this is the fix for the
    per-world-config gap; no /rctmod command needed anymore). `/rctmod player get series`
    → cobblemon-initiative for a fresh player. On a bare-mod standalone instance the same
    log line must appear (proves the invariant holds without the mrpack).
  ☐ RCTMOD SERIES (legacy worlds only): a player saved BEFORE alpha.10 with series
    "empty" is auto-migrated on join (`[rctmod compat] Placed <name> into series …`). After beating the gym-1 apprentice or leader:
    `/rctmod player get series` shows cobblemon-initiative and
    `/rctmod player get progress` reflects the win (our BATTLE_VICTORY dispatches
    `rctmod player add progress after <id>` — tbcs wins never register on their own).
    NOTE: `/rctmod player get level_cap` will show the NEXT-KEY-TRAINER model
    (~15-18 early), NOT our badge ladder — that readout is bookkeeping only;
    enforcement is ours (allowOverLeveling stays true).
  ☐ PLACEMENTS: walk the roof → both yield agents flank Mayor Suzune; Harvest Road →
    route surveyor/escort/wagon + Firstfurrow officer/site manager + Deng camp pair +
    watch lantern; Hua Zhan gym gate → yield analyst + rezoning board; four garden
    stations; branch office → receptionist + mezzanine analyst; Sango wheat field →
    company liaison. Lumo now recast at the docks (uuid wired from dev CSV).
  ☐ STILL NEED SHOWRUNNER COORDS (compile warns, not placed): hua_zhan_leader (!),
    hz_greenhouse_docent, apiarist_sumi, courier_mio, field_researcher_ume,
    forewoman_tetsu, company_surveyor, doc props + notice posts ×3, sq_kyc_agent,
    checkpoint grunt pair. Sight arming for spawned villains (route pair, checkpoint
    pair, yield officer/analyst) is a manual `npcsight add <uuid>` pass — latch
    spawns get random uuids.

☐ POST-SESSION LOG SWEEP: grep the log for `Blocked execute-as-user`, `Failed to load function`, `Unknown or incomplete command`, `[NPC Refresh] Import failed` — all four should be absent. Any failure: save the full log to dev/ (log-0.4.3-alpha.6) as usual.

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

`givepokemonother` (+gender/level keys) · `cobbledollars give/remove @s` under `execute as` ·
insufficient-balance behavior · `can_see_player` stealth branches (auditor/surveyor/
canvasser/office) · renamed give/loot component shapes (boots, IDs, books, papers) ·
`cobblemon:poke_rod`/`leaf_stone`/fossil ids · RCT `aspects` (autumn Deerling) · the
world-merge preset import (skin+dialog+rename) · shop tier medicine rows · paid-heal
function · sight arming via runtime `npcsight add` (right_of_way) · Easy NPC
sight-dialog fallback when the labeled entry is gated off.
