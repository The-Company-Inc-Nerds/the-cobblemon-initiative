# PokéPhone driver (registered in #minecraft:tick). Delivers remote story beats as calls on the
# mod's OWN full-screen call UI (0.7.0-a20 — replaces the invisible Easy-NPC caller + dialog-open
# retry): each ring_<id> runs `cobblemon-initiative phone ring <id>`, and PhoneCallManager owns
# the ringtone, the flashing actionbar, the answer keybind, missed calls, and the re-ring. CORE
# system, always on (showrunner 2026-07-20: the calls carry onboarding beats — Mom's care, the
# beacon nudge — so they are no longer behind a disable toggle).
# ONE-SHOT MODEL (a20): each script's done_tag (call_<id>_done) is granted by the manager on
# NORMAL COMPLETION, not at ring time — so an ignored/declined call stays un-done and this tick
# keeps re-firing its ring until the player actually takes it (no story beat is ever lost). The
# manager dedups a ring for a call that is already ringing/pending, so the ~2s cadence is safe.
# Throttled so the affordability probe below is not a per-tick cost. (#phone_tick rides ci_dawn,
# declared in economy/load.)
scoreboard players add #phone_tick ci_dawn 1
execute unless score #phone_tick ci_dawn matches 40.. run return 0
scoreboard players set #phone_tick ci_dawn 0
# Mom — "I want to watch your Pokémon" (after the 3rd badge, Mystic Marsh).
execute as @a[tag=defeated_mystic_leader] unless entity @s[tag=call_mom_watch_done] run function cobblemon_initiative:phone/ring_mom
# Mayor Liang — "a beacon came in" (freed the first field, and you can now afford the next beacon).
execute as @a[tag=farm_1_free] unless entity @s[tag=call_beacon_stock_done] run function cobblemon_initiative:phone/beacon_check
# ── Professor Acacia — starter ladder (2nd @15 dex, 3rd @30 dex) + dex-50 research grant ──
execute as @a[tag=second_starter_unlocked] unless entity @s[tag=call_acacia_second_done] run function cobblemon_initiative:phone/ring_acacia_second
execute as @a[tag=third_starter_unlocked] unless entity @s[tag=call_acacia_third_done] run function cobblemon_initiative:phone/ring_acacia_third
execute as @a[scores={dex_caught=50..}] unless entity @s[tag=call_acacia_dex_done] run function cobblemon_initiative:phone/ring_acacia_dex
# ── Mom — proud (5 badges) + worried (first Nuzlocke loss) ──
execute as @a[scores={memory_fragment=5..}] unless entity @s[tag=call_mom_proud_done] run function cobblemon_initiative:phone/ring_mom_proud
execute as @a[tag=nuzlocke_lost_one] unless entity @s[tag=call_mom_worry_done] run function cobblemon_initiative:phone/ring_mom_worry
# Mom — the Latios appearance (a20). HomeBaseManager fires the FIRST ring the moment the home-base
# threshold lands (home_base_built, granted at trigger time); this loop is the CRASH HEAL — the
# manager's requeue is in-memory, so a quit between ring and answer would otherwise lose the call
# forever while Latios stands there unexplained. Same owed-call shape as every line above.
execute as @a[tag=home_base_built] unless entity @s[tag=call_mom_latios_done] run function cobblemon_initiative:phone/ring_mom_latios
# ── The Company — escalating threat arc (DJ @7 -> the Board @Champion). The anonymous @3 beat was
# CUT in 0.7.0-a3 (under the OLD Easy-NPC delivery its deliver collided with the badge-3 Mom call;
# the manager now queues concurrent rings, so that bug class is gone). The script stays authored
# but dormant (phone_calls/unknown.json) — the beat may return at gym 4.
execute as @a[scores={memory_fragment=7..},tag=!defeated_villain_boss] unless entity @s[tag=call_dj_threat_done] run function cobblemon_initiative:phone/ring_dj_threat
execute as @a[tag=royal_league_champion] unless entity @s[tag=call_board_gloat_done] run function cobblemon_initiative:phone/ring_board_gloat
# The Founder — pre-finale call. DORMANT until act-3 emits a `board_cleared` player tag (Board content unbuilt).
execute as @a[tag=board_cleared] unless entity @s[tag=call_founder_done] run function cobblemon_initiative:phone/ring_founder
