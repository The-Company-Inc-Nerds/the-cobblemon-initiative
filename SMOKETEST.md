# Smoke Test — The Cobblemon Initiative

In-game verification checklist. For each point, mark **one** box and use the `comment:`
line under it for anything you notice.

| Box | Meaning |
|-----|---------|
| ✅ | **Approved** — works as intended |
| 💬 | **Works, with comment** — functions but see note |
| ❌ | **Not working** — broken / regressed |

**How to fill:** put an `x` in the one box that applies (`[ ]`), then type on the
`comment:` line beneath it. Deeper detail / root causes live in `docs/VERIFICATION_RUNBOOK.md`.

- **Build under test:** `0.4.3-alpha.15`  (note the `dev/log-<version>` here when you capture a log)
- **Tester / date:** Cole J. Calamos / 07-05-2026
- **World:** [ ] fresh mrpack install  ☐ dev run-client  ☐ bare-mod standalone

> Fresh world → **no manual setup**. Install self-runs, NPCs spawn on approach, and the
> rctmod series/cap is forced at startup. (The only per-world command you might use is
> `/cobblemon-initiative install run` if auto-install didn't fire — item 0.2.)

> **Fixed in alpha.15 (re-verify):** cap now **15** on a fresh player (was 20) · placed
> NPCs now show their real name + an rctmod skin (were "humanoid" + Steve) · chosen
> starter stand-in now despawns (per-species tag) · route/gym trainers get the fresh
> dialog (stale preset re-imported by the content-version bump) · roof agents at
> 2014/169/2464 & /2466, Firstfurrow at 1586/90/2487 & 1603/89/2488 · Marlow nameplate
> (was "Taya") · mom Goodbye off the first page · quieter rctmod join log.
>
> **Still open / needs your input:** spawn puts you on a roof — send me the bed coords ·
> dex-ladder-as-side-quest redesign (talk to prof first) — confirm the flow · Sango
> "docks" flavor is actually a savanna (Invitational re-theme — larger) · gym interior
> trainer_1–4 still need bodies (casting).

---

## 0. Boot, install & datapack

- **0.1** — World boots; no `Failed to load function` / `Couldn't parse data file` in log
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **0.2** — Fresh pack world: auto-install runs ~2s after first join (or `/cobblemon-initiative install run` is clean)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **0.3** — Log shows `[Easy NPC compat] Patched … security.cfg`
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **0.4** — Log shows `[rctmod compat] Healed server config: allowOverLeveling=true, initialSeries=cobblemon-initiative, spawning off`
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **0.5** — Difficulty Hard, hardcore ON, survival gamemode, **no** speed buff on the player
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **0.6** — `/cobblemon-initiative install check` → allowlist OK + bundled-map report
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 

## 1. Opening chain — starter trio, dex ladder, mom, pokédex, shoes

- **1.1** — **No** vanilla starter toast/screen at any point
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.2** — Open party → **no** "You have not yet selected a starter…" message
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.3** — Mom **walks up** once; never re-approaches after (relog / walk away)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.4** — Mom's dialog has a working **Goodbye**; no "Give me a moment"
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.5** — Accept errand → main line "Visit Professor Acacia" + side line "Choose a partner" within ~1s
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.6** — Professor "Choose a partner" → **3 starter NPCs spawn** rendered as the actual Pokémon (Skiddo / Totodile / Hisuian Growlithe)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.7** — Hisuian Growlithe stand-in shows the **Hisuian** model (needs AllTheMons active)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.8** — Click "Choose a partner" **again** → no duplicate spawns
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.9** — Each offer has a **"Keep looking"** button that closes without choosing
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.10** — Choose one → Pokémon (Lv5) lands in party + `chose_starter` set; **chosen stand-in despawns**
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.11** — Growlithe chosen → summary shows **Hisuian** Growlithe (fire/rock)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.12** — Other two stand-ins → **cry-only** dialog (+ Goodbye)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.13** — Professor re-talk → "Take the Pokédex" → pokédex item + `got_pokedex` + HUD flip
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.14** — **Dex ladder:** 15 unique CAUGHT entries → 2nd starter claimable @ Lv25
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.15** — 30 entries → 3rd starter claimable @ Lv40
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **1.16** — Mom → Running Shoes → side line clears, main line = "Defeat Takehara Gym"; shoes **+30%**, no other speed source
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 

## 2. Level caps (ladder 15 → 22 → 30 → … → 80 → 85 → 100)

- **2.1** — XP stops **exactly at 15** pre-badge (actionbar "Level cap 15 — the next badge raises it")
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **2.2** — A Totodile **cannot** reach Lv18/Croconaw before gym 1
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **2.3** — Rare candy at cap is **refused but NOT consumed**
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **2.4** — rctmod's own "level cap" actionbar warning does **NOT** appear
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **2.5** — **Beat gym-1 leader → cap raises to 22** ("Level cap increased to 22") — the linchpin fix; if it stays 15 this regressed
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **2.6** — Cicada's ace is **Lv17** (cap+2 — fought underleveled)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **2.7** — `/rctmod player get series` → cobblemon-initiative; `get progress` reflects the gym win
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **2.8** — ENDGAME (forced tags): grant champion achievement → cap 85; defeat all 4 `board_member` → "Board has fallen. Cap raised to 100"
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 

## 3. Economy — payouts, training packs, heal gate, battle money

- **3.1** — `economy/payout {amount:N}` → ~N CD + gold "Company Verified Rate" actionbar
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **3.2** — Small errand (≤260 CD) → payout **+ minor training pack** (3× Exp Candy XS + 1× S)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **3.3** — 300–400 CD completion → **standard pack** (2× S + 1× M)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **3.4** — 500–600 CD finale → **major pack** (1× Exp Candy L + one random vitamin)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **3.5** — Repeatable (derby re-entry / cascade gold-time / daily sprint / clinic rx) → **money/potion only, NO items**
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **3.6** — Nurse heal while broke → red "Payment declined", no heal, balance unchanged; with funds → heal + fee
  - ✅ x ]   💬 [ ]   ❌ [ ]
  - comment: 
- **3.7** — Win a dialog battle vs undefeated trainer → prize credited **once**; re-talk = already-beaten line
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **3.8** — Lose a fee battle → fee deducted **from YOU**; the **NPC** speaks the taunt
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **3.9** — Gym leader reward has **no emeralds** — CD + badge/shop only
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 

## 4. Gym battles & wiring

- **4.1** — Battle **starts** vs an undefeated gym trainer (not silently refused)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **4.2** — Correct trainer credited on win (esp. **Kalahar** trainer_1/2 — name-swap fixed)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **4.3** — Takehara jr/apprentice/leader all beatable in order; leader unlocks badge + cap
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **4.4** — Hua Zhan apprentice's Roselia has **no** Model-validation warn (poisonstingspore→stunspore)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 

## 5. Quests — towns 1-3

- **5.1** — Census taker's **first** dialog opens; accept → HUD line; finish → +500 + paper
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **5.2** — Dead-letter: take from Marlow → "deliver" side line within 1s → deliver to Lucian
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **5.3** — Price check accept → "0/3", each note increments
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **5.4** — Sidebar HUD shows active quest lines (main + side), updates on progress
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **5.5** — Spot-check 4–5 random NPCs: every dialog has a working exit (Goodbye or native close)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 

## 6. NPC placement, ambient life, companions & skins

- **6.1** — **Company Courier** spawns at its cart (~2592/111/2815) wearing the **rctmod skin** (team_galactic_grunt), separate from Lumo
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **6.2** — Lumo still at the docks (kept the CSV body)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **6.3** — Roof: both yield agents flank Mayor Suzune; Harvest Road: surveyor/escort/wagon + Firstfurrow officer/site-manager + Deng camp + watch lantern
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **6.4** — Hua Zhan gym gate: yield analyst + rezoning board; four garden stations; branch office pair
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **6.5** — Companions render as real Pokémon: Mr. Mime (Mom), **Magikarp beside Deka (not in a wall)**, Hoothoot (Nuru), Sentret (Oma), Chansey (both nurses), Combee, Psyduck, Meowth, Wooloo
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **6.6** — Wanderers drift & return (Kofi, Taya, Elder Nuru, museum Sayuri); **anchors stay put** (lane doors, stalls, nurses, Mom, Acacia)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **6.7** — Relog → **no duplicate** companions / placed NPCs
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **6.8** — No `Unknown Cobblemon species` lines in log
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **6.9** — Any placed NPC clipping a wall/furniture — note name + coords in comment
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 

## 7. NPC refresh & shop tiers

- **7.1** — Walk into a mapped NPC's chunk → `[NPC Refresh] Applied …` once; casting correct (nameplate/skin)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **7.2** — `/cobblemon-initiative shop badge_1` → "Applied shop tier" log; `shop refresh` error-free
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 

## 8. Standalone (bare-mod instance, no mrpack overrides)

- **8.1** — Fresh bare instance (mods only, no config folder) boots
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **8.2** — Log: security.cfg patched **and** `[rctmod compat] Healed …` both appear
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **8.3** — An NPC command button works on the **first** press
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **8.4** — Level cap is 15 pre-badge here too; custom skins absent → default Steve (cosmetic, expected)
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 

## 9. Log sweep (post-session)

- **9.1** — `Blocked execute-as-user` — absent
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **9.2** — `Failed to load function` — absent
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **9.3** — `Unknown or incomplete command` — absent
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **9.4** — `[NPC Refresh] Import failed` — absent
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 
- **9.5** — Full log saved to `dev/log-0.4.3-alpha.15` if anything failed
  - ✅ [ ]   💬 [ ]   ❌ [ ]
  - comment: 

---

## General notes / anything else found

- 
- 
- 
