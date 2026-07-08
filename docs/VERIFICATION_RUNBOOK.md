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

☐ K. ROUND-12c FIXES (0.5.0-alpha.1 — smoke-round-4 responses; SMOKETEST R1-R10 mirrors this)
  ☐ THE BATTLE CANARY, finally: WIN a dialog battle end-to-end (any Blossom Path regular).
    Every `tbcs attach/battle` now passes `rctmod:<id>` (TBCS mirrors rctmod's registry
    under namespace-prefixed keys, exact-match lookup — bare ids NEVER worked, in any
    build). Tab-complete `/tbcs battle GEN_9_SINGLES @p vs ` — suggestions must list
    `rctmod:takehara_leader` etc. Zero `No such trainer registered` in the log.
  ☐ ACTION GATES LIVE: beaten lines / one-time gives now actually gate (compiler emits the
    doubled `ConditionDataSet:{ConditionDataSet:[…]}` on ACTIONS; the old bare `Conditions`
    was silently ignored — the round-4 "everyone acts already-battled" second root cause).
    Talk to an UNDEFEATED trainer: no beaten line before the battle; after the win: beaten
    line, no re-battle, prize paid once.
  ☐ EXISTING-WORLD REPAIRS (any world ≤ alpha.17; fresh mrpack worlds skip this): update
    pack + `/reload` FIRST, then kill + latch-reset the six placement bodies so they
    respawn with skins/coords (kill first, THEN reset the score — reversed order can
    duplicate while standing in latch range):
      kill @e[type=easy_npc:humanoid,name="Bug Catcher Koji",limit=1]
      scoreboard players set #amb_takehara_trainer_1 ci_ambient 0
      (repeat for "Entomologist Yuki"/_2, "Bug Maniac Shin"/_3, "Youngster Taro"/_4,
       "Old Deng"/#amb_old_deng, "Granny Yun"/#amb_granny_yun)
    If the roof doubles were already won pre-0.5.0-alpha.1, Chiyo's body is orphaned
    (nothing despawned her — her partner Noboru carried the battle): stand on the roof, run
      kill @e[type=easy_npc:humanoid,name=Chiyo,limit=1]
    Then RE-TAG the fresh Deng/Yun bodies (showrunner-applied tags die with the old body;
    the homecoming teleport silently no-ops without them):
      tag @e[type=easy_npc:humanoid,name="Old Deng",limit=1] add deng_old
      tag @e[type=easy_npc:humanoid,name="Old Deng",limit=1] add deng_camp
      tag @e[type=easy_npc:humanoid,name="Granny Yun",limit=1] add deng_granny
      tag @e[type=easy_npc:humanoid,name="Granny Yun",limit=1] add deng_camp
  ☐ TAKEHARA LADDER GATES: Sora locked until 2/4 tower wins (takehara_tower score →
    band tag takehara_tower_gte_2), Aiko until Sora, Cicada until Aiko — locked entries
    show flavor, no battle button.
  ☐ SPAWN: fresh mrpack world → login line reads exactly (2612.5, 109.0, 2841.5), clean
    inventory/XP/no effects (minimal Data.Player bake). Dev/bare worlds: the JOIN snap
    (`ci_spawn_snapped` tag) lands the same point.
  ☐ DERBY RETUNE: 3 fish / 120s bar / pufferfish + tropical fish count / first win adds a
    Poké Rod. LUCIAN: stage-1 filing clears the three papers; dead letter + memo cleared
    on hand-in; her lines read she/her. CLINIC: sidebar line at slot 57 after accept.
  ☐ TONE PASS (round-4 narrative fear — details in docs/NARRATIVE_AUDIT_2026-07-06.md):
    civilian payouts print unbranded "Verified Rate"; ONLY census sign / courier sell /
    Invitational purse / Adjusted Retail keep "Company Verified Rate"; first-join message
    unlettered; "A checkpoint ahead."; battle UI "Site Assessors" / "Survey Canvasser".
  ☐ LOG: fresh mrpack build → zero `Model validation failure` (build strips UPM2's 76
    stale data.zip trainers); dev worlds still show them (harmless, registration proceeds).

☐ L. QUEST TRACKER (0.5.0-alpha.1, round 12e — SMOKETEST R11)
  ☐ Keys: `]` selects the top sidebar quest (actionbar "Tracking: <name>", aqua);
    repeat cycles down the list and past the end turns tracking off; `[` reverse.
    Rebindable under Controls → "The Cobblemon Initiative".
  ☐ The tracked side line gets an aqua "▶ " prefix (q.main already carries ▶ — no
    visual change when tracking main, expected). Macro lines (prices 0/3 etc.) keep
    their live numbers while highlighted.
  ☐ JourneyMap waypoint appears at the objective (minimap + fullscreen + in-world
    beacon, aqua), MOVES when the quest stage advances, and vanishes on untrack/
    completion. It is session-only — check the JM waypoint manager after relog:
    no permanent "cobblemon-initiative" waypoints accumulate.
  ☐ Stage advance mid-track: memo quest tent-stage has no coords (unplaced tent) —
    tracking it says "(no waypoint for this objective)"; once memo_heard, the
    waypoint appears on Lucian.
  ☐ Relog: tracking persists (world file cobblemon_initiative_quest_tracking.json);
    dimension hop + return re-creates the waypoint (MAPPING_STARTED resync).
  ☐ Un-OPed player can run /cobblemon-initiative track … (perm 0); admin subcommands
    still require OP 2 (the root gate moved down to each admin literal — spot-check
    /cobblemon-initiative reset still refuses without OP).
  ☐ Bare-mod world (no JM): an END_ROD particle column marks the target instead.
  ☐ CHESTS: open ~8 unplaced map chests — roughly 3 in 4 open plain-empty (claimed
    either way: re-opening an empty one never stocks it later); ModMenu → Loot
    Chests → "Empty Chest Chance" 0% restores pre-0.5.0 always-stock.
  ☐ RUMBLE: ground-shrine earthquake rumble audibly plays (pitch 0.5 — the old 0.4
    default was below the engine floor; a pre-0.5.0 saved shrine config self-heals
    on read, no file edit needed).

☐ M. ROUND-13 (0.5.0-alpha.1 late batch — SMOKETEST R3/R13)
  ☐ TAKEHARA WEAKENING: with 0 tower wins Sora battles immediately (no lock line);
    beat 1 tower trainer → Sora's say acknowledges the tower + her team is the _weak
    variant; 2 → Aiko weak; all 4 → Cicada's drained-drills say + weak team. Weak wins
    still set defeated_takehara_* / badge / cap 22 (the linchpin — verify cap raises).
  ☐ SHOP: fresh tier-0 shop = 3 ball rows only; badge_1 restores the six.
  ☐ DERBY: carry 3 salmon in, pay entry → bucket reads 0 of 3 (baseline snapshot).
  ☐ CASCADE: two gold runs same day → 300 CD once, second run title-only.
  ☐ SPRINT: first run 120s bar; daily 100s bar (fail = walk back, no pay loss).
  ☐ GRANARY: arm the ambush (trade + linger) → battle STARTS (was silent pre-13).

☐ POST-SESSION LOG SWEEP: grep the log for `Blocked execute-as-user`, `Failed to load function`, `Unknown or incomplete command`, `[NPC Refresh] Import failed` — all four should be absent. Any failure: save the full log to dev/ (log-0.4.3-alpha.6) as usual.

## Phase 0 — Boot & wiring (5 min)

For a run-client session, `scripts/dev_sync --world` once syncs run/ to the .mrpack
stack (pack mods incl. tbcs + rctmod — dev is battles-capable — configs, resourcepacks,
bundled world with the build bakes); `--fresh-world` resets the save, `--lean` skips
cosmetics for faster boots.

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
11. 🐞 Nurse heal: click **Heal my team — posted rate** on Medcrest (Asha) →
    party healed AND balance −(100 + 2×`#idx cd_instability`) — 100 flat pre-gym-1
    (§9 TIER-B: the fee rides the instability index since review B3).

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
20. **Nurse Lila** charges the posted rate (100 + 2×idx — 116 with gym 1's instability
    banked; the actionbar receipt prints the live fee). **Canvasser stealth** (Mei's
    prints): paste while seen → voided; unseen ×3 → ✅ pay + heal_ball.
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
33. **Out of Network**: Anong-nurse heals at the posted rate (100 + 2×idx — ~132 with
    both gyms banked); berry restock quest pays ~240 CD.
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

## §7 ROUND-13b — Battle-Engagement Matrix smoke checks (2026-07-06)

Ships: `engage:"touch"` trigger synthesis, `battle.decline_fee` bow-outs (14 generated
`function/route/decline_*` + 6 hand probe functions), the `open_dialog` gate-drop fix,
grunt/checkpoint free-exit removal, wheat-trader hostile touch. Fee table:
meadow 80 / spotter 100 / sq_regular_4 110 / mirek 120 / typetip 130 / haoran 130 /
xu_jianyu (kite) 150; wager bow-outs luo 60 / genji 100 / hz_analyst 80 / ume 150;
fee-forks canvasser 150 / KYC 150 / Yan 150 / checkpoint 120 + 250 handling.

1. **Route hail**: approach Bird Keeper Ayo (Blossom Path) undefeated to ~8 blocks →
   exactly ONE chat hail (`say`); leave the band and re-enter → it repeats (ActionGroup
   resets when the radius empties). No battle fires at 8 blocks.
2. **Touch-force**: step inside 1.25 blocks → attach + battle start with NO dialog.
   Win → `defeated_*`; re-touching a beaten (or bought-off) trainer does nothing and
   prints nothing (the trigger carries no beaten-line say by design).
3. **Bow-out, funded**: with ≥110 CD open Zola's dialog → "Pay 110 CD to bow out" →
   balance −110, gold actionbar receipt *"Verified Charge: 110 CD."* (UNBRANDED — tone
   rule), tag `declined_sq_regular_4`. Afterwards: no hail, no touch battle, battle
   button hidden, bow-out button hidden. Permanent.
4. **Bow-out, broke**: `cobbledollars set @s 0`, click another trainer's bow-out →
   NO charge, NO receipt, the battle starts immediately (must-fight). Clicking it
   again post-defeat does nothing (function guards).
5. **Stake probes**: Lan (branch mezzanine) with 0 CD → red *"Stake declined"*, NO
   battle (a wager can no longer run stakeless); with ≥150 → *"Stake logged"*, battle,
   win pays 300 + billable_hours. Ume's 300-CD field wager: same pattern, purse 900.
6. **Grunt force**: villain_grunt_3..11 + route escort Lei — recognition dialog entries
   have NO Goodbye and no free exit; pursue → touch at 1.25 forces the battle; grunts
   still despawn on defeat and never re-trigger.
7. **Fee-or-fight forks**: checkpoint fee buttons (Sani/Haruki 120; contraband 250)
   with 0 CD → red payment-declined + THAT agent battles you; funded → the shared
   `paid_checkpoint_fee`/`paid_handling_fee` tag, no double-charge on re-click, the
   surrender fork still free. KYC sketch and Yan's survey lost their free walk-aways
   (fee 150 or fight). Yan REGRESSION: the ON_DISTANCE_NEAR eavesdrop now opens ONLY
   with `audit_loiter` (its gate was silently dropped before this round — verify a
   fresh walk-up does NOT get the overheard dialog).
8. **Wheat traders**: at `fields_liberated` ≥ 4 the hostile entry has no *Stand down*
   and TOUCHING a trader starts `wheat_trader_ambush`; beating either stands both down
   (shared defeat tag). Below 4 fields: browse/leave unchanged, no ambush.
9. **Exemptions/regressions**: Takehara gym ladder untouched (incl. the weakened
   variants) and the Invitational bracket keeps its free declines; quest-confrontation
   OUTER hails (fence/corridor/quota) stay leaveable while their recognition battle
   entries are exit-free; site manager pre-officer players still get the gated
   "About the officer" close instead of a dead-end.

**Live-world caveat**: placement-latched villains (escort Lei, Lan, Yan, the Harvest
Road trio, checkpoint pair, …) spawned copies are NOT in `preset_map.json` — content
updates do not propagate. To pick up round-13b behavior: kill the body +
`scoreboard players set #amb_<key> ci_ambient 0` (latch key = character id) and walk
back into the 40-block latch radius. UUID-mapped route trainers (Zola, Ayo, Jabari,
Kwame, Mirek, Haoran, Xu, Luo) refresh normally via `update_npc_presets` /
NpcPresetRefreshManager. `dialog/band_tags` (tick) now also maintains `no_declined_*`.

## §8 ROUND-14 — Quest-flow quick wins (Tier A, 2026-07-06)

Ships: courier-SELL aftermath (sidebar tombstone + ASSET LIQUIDATED sting + Sentinel
cold shoulder), census callbacks (Kazuo/Femi), Lucian badge-1→3 bridge + FILING DAY
aggregate (`ci_papers_filed/held`, slot 75), Takehara guide fork (`dialog/
takehara_guide.json` — sq_perf_review_guide merge file retired), deferred ghost reveal
(+5s SILENT STAKEHOLDER title), liberation ceremony (`<FIELD> — LIBERATED` + fireworks
+ 3 back-echoes), two-stage badge ceremony (`rewards/gym/badge_1..3`, frag title +4s),
Lila rumor board, paper-hub filings (RZ-7 + Field Memo 7-12 pay 200 + minor), bracket
round cards + DISBURSEMENT sting + crier fix (three rounds), discovery breadcrumbs
(Mei/Lucian/Mio/Kele), gossip micro-pass (Lan outlier, stall sign-down ×3, Ning,
Marlow, Ume↔Imani, Asha), hygiene sweep (Shou/Sayuri names, Sayuri pages, adjunct
erasure rewrite + Raan 8 coal/4 iron), Miller Walk survey loggers, Kazuo 19/20.

**REMOVED 2026-07-07 — Takehara Performance Review stealth:** per showrunner, the
gym-1 tower is a plain battle ladder with no stealth. The `takehara_sentry` sight tags,
the `sidequest/perf_review/*` functions, the SILENT STAKEHOLDER ghost cache, and the
per-world sentry tagging/npcsight setup step are all gone. Beating all four tower
trainers still pays the tower-clear bonus at the guide; the tower-drain (skip the
ladder → Cicada at full strength) is unchanged. No world-setup step for this anymore.

Round-14 canary list:
1. **Courier SELL**: sell → ASSET LIQUIDATED title + sculk/anvil sting; sidebar flips
   to dark-red `• The record is Company property now` (never the stale rebuild line);
   Elder Sentinel opens on the cold shoulder; Lucian still opens stage1_sold.
2. **Filing day**: hold 2+ Lucian-bound papers → individual deliver lines vanish, one
   `• File with Lucian: n papers` (slot 75) appears and tracks/waypoints to her desk;
   file everything → 3+ filings flips Lucian's default greeting (papers_growing).
3. **Badge 1 two-stage**: beat Cicada → Falls Badge — EARNED + fireworks/toast, then
   ~4s later the purple memory-fragment title lands alone; a ghost run adds SILENT
   STAKEHOLDER ~1s after that (5s), never under the badge card.
4. **Liberation**: free Firstfurrow → FIRSTFURROW — LIBERATED (wheat-gold) + bell +
   fireworks + `The Company will notice.` subtitle; Tunde/Sentinel/Masumi all gossip
   it back afterward.
5. **Bridge window**: docs filed, badges < 3 → Lucian opens on `stage_bridge` (third
   badge line) and the sidebar reads `• Lucian waits on a third badge`.
6. **Rumor board**: Lila's word-around-town button prints one gated rumor per click
   (page_turn sound); with all five quests done it prints the static three-spot page.
7. **Invitational**: each bracket win shows its round card (winners-first onwin, as
   the player); podium accept fires DISBURSEMENT COMPLETE *before* the verified-rate
   receipt; Kofi barks three-rounds copy pre-bracket, go-collect after round 3,
   champion bark post-podium.
8. **Retune**: Kazuo now fields Meowth 19 / Koffing 20 (payday/sludge sets) — battle
   gate + 280 CD prize + 150 decline fee unchanged.
9. **Regression**: Sango→Takehara opening unchanged except THE ROAD WEST send-off card
   on the shoes; nurse heal buttons/prices untouched (superseded by §9: buttons read
   "posted rate" since review B3); the five suppressed deliver lines return the moment
   held papers drop back to ≤ 1.

## §9 TIER-B — Fairness floor, indexed heal fee, visible randomness, replay variance (2026-07-06)

Ships (showrunner-approved B1/B3/B6/B7; B4 shipped earlier inline): dex_gte_2 forced-
battle floor on every ON_DISTANCE_TOUCH battle; heal fee = 100 + 2×`#idx
cd_instability` (macro `economy/heal_paid` → `heal_paid_fee`); the visible-randomness
set (branded-receipt line-item roll, derby Record Species, Company Morning Memo +
East Market ware roll on the new `economy/dawn` tick, clinic_rx weighted pool, hamper
side-item rolls, wager purse sweetener — STAKES STAY FIXED per the §3 invariants);
tower A/B rosters via the placement-latch roll (`spawn_variants: 2`, presets
`takehara_trainer_N[_b]`, teams `takehara_trainer_N_b.json`). Spotter stand rolls were
SKIPPED: Ayo AND Zola are uuid-anchored world bodies (npcsight pursue is uuid-keyed;
placement latches never run for uuid'd characters), so a spawn-point roll cannot move
them without orphaning their sight registration — re-scope with the engagement matrix.

Tier-B canary list:
1. **Fairness floor (B1)**: fresh character, starter only (`dex_caught` = 1 — the
   starter DOES register as CAUGHT, jar-verified): walk into Ayo/Zola at TOUCH range →
   the CLOSE-band hail still fires, NO battle starts. Catch anything (`dex_caught` 2,
   ≤2 s lag) → leave the band, re-approach → forced battle fires. Same check on a
   wheat-trader hostile touch.
2. **Heal fee (B3)**: `scoreboard players set #idx cd_instability 8` → heal button →
   healed, receipt reads "posted rate: 116"; broke → "The Center does not extend
   credit. (116 CD required)", NO heal. Reset #idx to 0 → 100 flat. Buttons on all
   four nurses read **Heal my team - posted rate**.
3. **Branded line-item roll (B6)**: census SIGN fork (or any payout_company) → one
   gray zero-value ledger line under the actionbar receipt, different across payouts;
   `set #idx cd_instability 40` → lines come from the nervous pool (re-verification /
   PLEASE RETAIN). Paid amount NEVER moves with the roll.
4. **Record Species (B6)**: derby entry prints the CHALKBOARD species; land one of
   that species in the winning three → RECORD SPECIES LANDED + a skew-aware +75 CD
   Verified Rate receipt (fires on repeat wins too — money only); win without it →
   no bonus, no line.
5. **Dawn roll (B6)**: sleep through a night (or `time add 24000`) → exactly TWO chat
   lines at dawn: EAST MARKET STREET (orchard/hill day) + one COMPANY MORNING MEMO
   line (propaganda pool at idx<16, reassurance 16..55, corrupted at 56+ or post-HQ).
   First-ever load also fires once (latch seeding).
6. **Ware rotation (B6)**: on an orchard day Auntie Song stocks Leppa (no Lum), Qiu's
   forward drawer opens (steady unreachable), Bo Huan hangs red/yellow/pink; hill day
   is the mirror (Lum; steady drawer; blue/green/white). Oran/Sitrus, cures, curios,
   black jar, tumblestones, basket: always. Prices identical both days.
7. **Wager sweetener (B6)**: win any of the four wagers (Genji / Ume / Luo / Lan,
   button AND broke-decline paths) → base purse + "The purse runs heavy — +25/50/75/
   100 CD over the posted terms." Stakes, decline fees, loss fees: unchanged numbers,
   printed before the click.
8. **Drips (B6)**: clinic_rx daily now rolls potion(4)/antidote(2)/paralyze_heal(2)/
   super_potion(1); night-watch + homecoming hampers add one announced rolled side
   item on top of their fixed contents.
9. **Tower A/B (B7)**: fresh world, enter the tower → `scoreboard players get
   #var_takehara_trainer_1 ci_ambient` reads 1 or 2; a 2-roll body battles
   `rctmod:takehara_trainer_1_b` (Wurmple/Scatterbug) but still credits
   `defeated_takehara_trainer_1` — takehara_tower count, Sora's gate, Performance
   Review, and the badge flow all behave identically. Nameplates read the same
   (Koji/Yuki/Shin/Taro). The §8 sentry-tag + npcsight world setup is UNCHANGED and
   still release-blocking. Re-roll: kill body + `scoreboard players set
   #amb_takehara_trainer_N ci_ambient 0`.
10. **Regression**: decline/stake literals across §7 unchanged; unbranded `economy/
    payout` receipts carry NO line-item roll; `_weak` overrides (jr/apprentice/leader
    only) never intersect the tower A/B files.
