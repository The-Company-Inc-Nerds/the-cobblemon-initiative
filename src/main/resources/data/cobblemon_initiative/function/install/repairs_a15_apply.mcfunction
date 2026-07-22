# repairs wave a15 — apply: kill EVERY cobblemon-model body within 24 of each companion
# home (name/tag-agnostic proximity sweep — clears 1/2/3 copies alike), then reset that
# companion's latch to 0 so the recompiled dedup guard re-imports exactly ONE fresh body
# on the next visit. type=easy_npc:cobblemon_npc pins the query to companion bodies — never
# a player's real Cobblemon (cobblemon:pokemon), never a humanoid NPC — and the 24-block
# radius keeps each sweep local to its own home (homes are 37+ blocks apart; nobles far off).
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

# Victor relocation (grain tower -> reveal site 2536/106/2900). Only touch an UN-revealed
# Victor: skip entirely if the Victini form already exists OR the player has taken it into
# their party — a completed transform is left alone. Otherwise kill the stale tower body,
# re-arm his latch so he re-spawns at his new placement coords, and clear any stuck transform
# trigger from a save that hit the old infinite-restart (the player just re-clicks the
# now-working transform on the relocated apprentice — no progress is lost, Victini is only
# earned on capture).
execute unless entity @e[tag=victor_victini,type=!minecraft:player] unless entity @a[tag=victini_joined] positioned 2522.6 131 2815.5 run kill @e[type=easy_npc:humanoid,tag=victor_apprentice,distance=..8]
execute unless entity @e[tag=victor_victini,type=!minecraft:player] unless entity @a[tag=victini_joined] run scoreboard players set #amb_victor ci_ambient 0
execute unless entity @e[tag=victor_victini,type=!minecraft:player] unless entity @a[tag=victini_joined] run tag @a remove victor_transformed
execute unless entity @e[tag=victor_victini,type=!minecraft:player] unless entity @a[tag=victini_joined] run tag @a remove victor_transform_fired

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
forceload remove 2512 2800
forceload remove 2512 2816
forceload remove 2528 2800
forceload remove 2528 2816
# Clean up any reveal-site forceload leaked by the OLD victor_transform — it added
# `forceload add 2536 2900` on every tick of the infinite-restart and may never have cleared.
forceload remove 2536 2900
execute if score #debug ci_ambient matches 1 run tellraw @a [{"text":"[Initiative] ","color":"gold"},{"text":"Repair a15: companion dupes swept for good, Victor moved to his reveal site — Mimi/Jackpot re-latch as singles, Victor transforms in place.","color":"gray"}]
