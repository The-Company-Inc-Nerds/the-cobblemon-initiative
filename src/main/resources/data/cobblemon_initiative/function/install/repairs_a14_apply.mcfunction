# repairs wave a14 — apply: kill EVERY cobblemon-model body near each companion home
# (name-agnostic proximity sweep), then re-arm that companion's single latch so exactly one
# fresh body re-imports on the next visit. type=easy_npc:cobblemon_npc pins the query to
# Easy NPC companion bodies — never a player's real Cobblemon, never a humanoid NPC — and the
# 24-block radius keeps each sweep local to its own home (nobles at the far arenas untouched).
execute positioned 2605.5 109 2846.5 run kill @e[type=easy_npc:cobblemon_npc,distance=..24]
scoreboard players set #amb_mrmime ci_ambient 0
execute positioned 2568.5 111 2855.5 run kill @e[type=easy_npc:cobblemon_npc,distance=..24]
scoreboard players set #amb_magikarp ci_ambient 0
execute positioned 2588.5 107 2957.5 run kill @e[type=easy_npc:cobblemon_npc,distance=..24]
scoreboard players set #amb_sentret ci_ambient 0
execute positioned 1543.5 88 2109.5 run kill @e[type=easy_npc:cobblemon_npc,distance=..24]
scoreboard players set #amb_meowth ci_ambient 0
execute positioned 1513.5 84 1988.5 run kill @e[type=easy_npc:cobblemon_npc,distance=..24]
scoreboard players set #amb_wooloo ci_ambient 0
execute positioned 1893.5 105 2470.5 run kill @e[type=easy_npc:cobblemon_npc,distance=..24]
scoreboard players set #amb_psyduck ci_ambient 0

# Hua Zhan decorative statue props RELEASED (2026-07-21): despawn all 4 (Knight/Traveller/Climber/
# Gardener) — the person-warden statues are the real wardens; these were redundant. Tight radius
# (nearest warden is 14+ blocks off), so distance=..3 catches only the prop. Flags pinned to 1 so
# the ambient loop never re-places them (their place fns + presets are deleted too).
execute positioned 1479.9 87.0 2112.6 run kill @e[type=easy_npc:humanoid,distance=..3]
execute positioned 1451.8 90.0 2026.3 run kill @e[type=easy_npc:humanoid,distance=..3]
execute positioned 1456.5 103.0 2098.1 run kill @e[type=easy_npc:humanoid,distance=..3]
execute positioned 1538.0 85.0 2026.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_hz_statue_knight ci_ambient 1
scoreboard players set #amb_hz_statue_gatepost ci_ambient 1
scoreboard players set #amb_hz_statue_bough ci_ambient 1
scoreboard players set #amb_hz_statue_garden ci_ambient 1

forceload remove 2576 2816
forceload remove 2576 2832
forceload remove 2576 2848
forceload remove 2592 2816
forceload remove 2592 2832
forceload remove 2592 2848
forceload remove 2608 2816
forceload remove 2608 2832
forceload remove 2608 2848
forceload remove 2544 2832
forceload remove 2544 2848
forceload remove 2544 2864
forceload remove 2560 2832
forceload remove 2560 2848
forceload remove 2560 2864
forceload remove 2560 2928
forceload remove 2560 2944
forceload remove 2560 2960
forceload remove 2576 2928
forceload remove 2576 2944
forceload remove 2576 2960
forceload remove 2592 2928
forceload remove 2592 2944
forceload remove 2592 2960
forceload remove 1520 2096
forceload remove 1536 2096
forceload remove 1552 2096
forceload remove 1536 2112
forceload remove 1552 2112
forceload remove 1488 1984
forceload remove 1504 1984
forceload remove 1520 1984
forceload remove 1504 2000
forceload remove 1520 2000
forceload remove 1872 2464
forceload remove 1888 2464
forceload remove 1904 2464
forceload remove 1888 2448
forceload remove 1888 2480
forceload remove 1472 2096
forceload remove 1472 2112
forceload remove 1440 2016
forceload remove 1456 2096
forceload remove 1536 2016
execute if score #debug ci_ambient matches 1 run tellraw @a [{"text":"[Initiative] ","color":"gold"},{"text":"Repair a14: cobblemon-model companion dupes swept by proximity — Mimi/Jackpot & co. re-latch as singles.","color":"gray"}]
