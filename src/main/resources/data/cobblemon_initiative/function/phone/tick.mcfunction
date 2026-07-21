# PokéPhone driver (registered in #minecraft:tick). Delivers remote story beats as PokéGear-style
# "calls" (Pokémon Gold/Silver): an actionbar "☎ ringing", a chime, then the caller's lines. Text
# delivery is deliberate over a live Easy NPC host — a call then NEVER fails on an unloaded chunk,
# it rings anywhere. CORE system, always on (showrunner 2026-07-20: the calls carry onboarding
# beats — Mom's care, the beacon nudge — so they are no longer behind a disable toggle).
# Each call fires ONCE via a call_<id>_done tag. Throttled to ~2s so the affordability probe below
# is not a per-tick cost. (#phone_tick rides ci_dawn, declared in economy/load.)
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
# ── The Company — escalating recognition/threat arc (anonymous @3 -> DJ @7 -> the Board @Champion) ──
execute as @a[scores={memory_fragment=3..}] unless entity @s[tag=call_company_watch_done] run function cobblemon_initiative:phone/ring_company_watch
execute as @a[scores={memory_fragment=7..},tag=!defeated_villain_boss] unless entity @s[tag=call_dj_threat_done] run function cobblemon_initiative:phone/ring_dj_threat
execute as @a[tag=royal_league_champion] unless entity @s[tag=call_board_gloat_done] run function cobblemon_initiative:phone/ring_board_gloat
# The Founder — pre-finale call. DORMANT until act-3 emits a `board_cleared` player tag (Board content unbuilt).
execute as @a[tag=board_cleared] unless entity @s[tag=call_founder_done] run function cobblemon_initiative:phone/ring_founder
